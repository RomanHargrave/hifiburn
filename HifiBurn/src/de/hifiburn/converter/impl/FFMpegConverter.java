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
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.hifiburn.converter.ConvertException;
import de.hifiburn.converter.Format;
import de.hifiburn.converter.IConverter;
import de.hifiburn.i18n.Messages;
import de.hifiburn.logic.IPreferenceConstants;
import de.hifiburn.logic.InitializeException;
import de.hifiburn.logic.PreferenceManager;

public class FFMpegConverter implements IConverter
{

  @Override
  public void initialize()
      throws ConvertException, InitializeException
  {
    if (canConvert()==false)
      throw new InitializeException(
          Messages.FFMpegConverter_0);
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

    _args.add("-filter"); //$NON-NLS-1$
    _args.add("aresample=" + theBitrate); //$NON-NLS-1$

    _args.add(theOutput.getAbsolutePath());

    try
    {
      int _ret = execFFmpeg(_args);
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
    return true;
  }

  protected int execFFmpeg(List<String> theArguments)
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
    List<String> _ret = new ArrayList<String>();
    _ret.add("*.mp3");  //$NON-NLS-1$
    _ret.add("*.wav");  //$NON-NLS-1$
    _ret.add("*.flac");  //$NON-NLS-1$
    _ret.add("*.ape");  //$NON-NLS-1$
    _ret.add("*.aac"); //$NON-NLS-1$
    
    return _ret;
  }

  @Override
  public boolean canConvert()
  {
    try
    {
      execFFmpeg(Arrays.asList(new String[] { "-version" }));  //$NON-NLS-1$
    }
    catch (IOException _e)
    {
      return false;
    }
    
    return true;
  }
}
