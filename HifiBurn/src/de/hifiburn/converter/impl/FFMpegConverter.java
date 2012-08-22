/*
 * HifiBurn 2012
 * 
 * FFMpegConverter.java
 */
package de.hifiburn.converter.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hifiburn.converter.ConvertException;
import de.hifiburn.converter.Format;
import de.hifiburn.converter.IConverter;
import de.hifiburn.i18n.Messages;
import de.hifiburn.logic.ICommandListener;
import de.hifiburn.logic.IPreferenceConstants;
import de.hifiburn.logic.InitializeException;
import de.hifiburn.logic.PreferenceManager;

public class FFMpegConverter implements IConverter
{

  protected Pattern patternExtension = Pattern.compile("\\sD.A...\\s([^\\s]*).*");
  
  protected Map<String,String> supportedExtension = new HashMap<String,String>();
  
  protected List<String> detectedExtensions = null;
  

  /**
   * 
   */
  public FFMpegConverter()
  {
    super();
    supportedExtension.put("aac", "aac");
    supportedExtension.put("ape", "ape");
    supportedExtension.put("eac3", "ac3");
    supportedExtension.put("ac3", "ac3");
    supportedExtension.put("flac", "flac");
    supportedExtension.put("mp3", "mp3");
    supportedExtension.put("mp3float", "mp3");
    supportedExtension.put("MP2 (MPEG audio layer 2)", "mp2");
    supportedExtension.put("MP2 (MPEG audio layer 2)", "mp2");
    supportedExtension.put("pcm_s16le", "wav");
    supportedExtension.put("tta", "tta");
    supportedExtension.put("twinvq", "tfq");
    supportedExtension.put("twinvq", "tfq");
    supportedExtension.put("vorbis", "ogg");
    supportedExtension.put("wavpack", "wv");
    supportedExtension.put("wmalossless", "wma");
    supportedExtension.put("wmapro", "wma");
    supportedExtension.put("wmav1", "wma");
    supportedExtension.put("wmavoice", "wma");
  }

  
  @Override
  public void initialize()
      throws ConvertException, InitializeException
  {
    if (canConvert()==false)
      throw new InitializeException(
          Messages.FFMpegConverter_0);
    
    detectedExtensions = getExtensionInternally();
  }

  @Override
  public void convert(File theInput, File theOutput, Format theFormat, int theBitrate, int theSamplerate)
      throws ConvertException
  {
    List<String> _args = new ArrayList<String>();
    _args.add("-i"); //$NON-NLS-1$
    _args.add(theInput.getAbsolutePath());
    _args.add("-acodec"); //$NON-NLS-1$

    if (theFormat.equals(Format.WAV))
      _args.add("pcm_s16le"); //$NON-NLS-1$
    else
      _args.add("pcm_s16le"); //$NON-NLS-1$

    // overwrite
    _args.add("-y"); //$NON-NLS-1$
    
    // verbose
    _args.add("-loglevel"); //$NON-NLS-1$
    _args.add("info"); //$NON-NLS-1$
    
    _args.add("-sample_fmt"); //$NON-NLS-1$
    _args.add("s"+String.valueOf(theSamplerate)); //$NON-NLS-1$

    // !!! dont work under windows
    //_args.add("-filter"); //$NON-NLS-1$
    //_args.add("aresample=" + theBitrate); //$NON-NLS-1$

    _args.add(theOutput.getAbsolutePath());

    try
    {
      int _ret = execFFmpeg(_args, null);
      if (_ret != 0)
      {
        Logger.getLogger(FFMpegConverter.class.getName()).log(Level.SEVERE, Messages.FFMpegConverter_1,
            String.format(Messages.FFMpegConverter_2, _ret));
        throw new ConvertException(String.format(Messages.FFMpegConverter_3, _ret));
      }
    }
    catch (IOException _e)
    {
      Logger.getLogger(FFMpegConverter.class.getName()).log(Level.SEVERE, Messages.FFMpegConverter_4, _e);
      throw new ConvertException(_e);
    }
  }

  @Override
  public boolean supportFormat(File theInput, Format theOutputFormat)
  {
    String _extension = theInput.getName().substring(theInput.getName().lastIndexOf(".")+1);
    if (detectedExtensions.contains(_extension))
      return true;
    
    return false;
  }

  protected int execFFmpeg(List<String> theArguments, ICommandListener theListener)
      throws IOException
  {
    List<String> _tmp = new ArrayList<String>();

    String _path = PreferenceManager.getInstance().getString(IPreferenceConstants.FFMPEG_PATH);
    if (_path.trim().length() != 0)
      _tmp.add(_path + File.separatorChar + "ffmpeg"); //$NON-NLS-1$
    else
      _tmp.add("ffmpeg"); //$NON-NLS-1$

    if (theArguments != null)
      _tmp.addAll(theArguments);

    ProcessBuilder _builder = new ProcessBuilder(_tmp);

    Logger.getLogger(FFMpegConverter.class.getName()).log(Level.INFO,String.format("FFMpeg Command: %s",getCommand(_tmp))); //$NON-NLS-1$
    _builder.redirectErrorStream(true);
    Process _p = _builder.start();

    StringBuffer _sb = new StringBuffer();
    Scanner _s = new Scanner(new BufferedInputStream(_p.getInputStream()));
    while (_s.hasNext())
    {
      String _out = _s.nextLine();
      _sb.append(_out);
      _sb.append("\n"); //$NON-NLS-1$
      
      if (theListener!=null)
        theListener.newLine(_out);
    }
    _s.close();

    Logger.getLogger(FFMpegConverter.class.getName()).log(Level.INFO, String.format("FFMpeg Output: %s",_sb.toString())); //$NON-NLS-1$
    
    try
    {
      _p.waitFor();
    }
    catch (InterruptedException _e)
    {
    }
    
    return _p.exitValue();
  }

  @Override
  public String getName()
  {
    return Messages.FFMpegConverter_5;
  }

  @Override
  public String getId()
  {
    return "ffmpeg"; //$NON-NLS-1$
  }

  public String getCommand(List<String> theCommands)
  {
    StringBuilder _cmdbuf = new StringBuilder(200);
    for (int i = 0; i < theCommands.size(); i++)
    {
      if (i > 0)
      {
        _cmdbuf.append(' ');
      }
      String s = theCommands.get(i);
      if (s.indexOf(' ') >= 0 || s.indexOf('\t') >= 0)
      {
        if (s.charAt(0) != '"')
        {
          _cmdbuf.append('"');
          _cmdbuf.append(s);
          if (s.endsWith("\\"))  //$NON-NLS-1$
          {
            _cmdbuf.append("\\");  //$NON-NLS-1$
          }
          _cmdbuf.append('"');
        }
        else if (s.endsWith("\""))  //$NON-NLS-1$
        {
          /* The argument has already been quoted. */
          _cmdbuf.append(s);
        }
        else
        {
          /* Unmatched quote for the argument. */
          throw new IllegalArgumentException();
        }
      }
      else
      {
        _cmdbuf.append(s);
      }
    }
    return _cmdbuf.toString();
  }

  @Override
  public List<String> getExtension()
  {
    return detectedExtensions;
  }


  /**
   * @return
   */
  protected List<String> getExtensionInternally()
  {
    final Set<String> _ret = new HashSet<String>();
    
    try
    {
      execFFmpeg(Arrays.asList(new String[] { "-codecs" }), new ICommandListener()
      {
        @Override
        public void newLine(String theLine)
        {
          Matcher _m = patternExtension.matcher(theLine);
          if (_m.matches())
          {
            if (supportedExtension.containsKey(_m.group(1)))
              _ret.add(supportedExtension.get(_m.group(1)));
          }
        }
      });
    }
    catch (IOException _e)
    {
      Logger.getLogger(FFMpegConverter.class.getName()).log(Level.INFO, String.format("Could not detect codec support (%s)", //$NON-NLS-1$
          _e.toString()));
    }
    return new ArrayList<String>(_ret);
  }

  @Override
  public boolean canConvert()
  {
    try
    {
      execFFmpeg(Arrays.asList(new String[] { "-version" }), null);  //$NON-NLS-1$
    }
    catch (IOException _e)
    {
      return false;
    }
    
    return true;
  }
}

