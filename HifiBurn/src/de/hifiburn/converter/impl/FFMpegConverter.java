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
          "FFMpeg konnte nicht gefunden werden!\r\n\r\nBitte installieren Sie FFMpeg (http://ffmpeg.org/download.html) und passen den Pfad in den Einstellungen an.");
  }

  @Override
  public void convert(File theInput, File theOutput, Format theFormat, int theBitrate, int theSamplerate)
      throws ConvertException
  {
    List<String> _args = new ArrayList<String>();
    _args.add("-i");
    _args.add(theInput.getAbsolutePath());
    _args.add("-acodec");

    if (theFormat.equals(Format.WAV))
      _args.add("pcm_s16le");
    else
      _args.add("pcm_s16le");

    // overwrite
    _args.add("-y");
    
    // verbose
    _args.add("-loglevel");
    _args.add("info");
    
    _args.add("-sample_fmt");
    _args.add("s"+String.valueOf(theSamplerate));

    _args.add("-filter");
    _args.add("aresample=" + theBitrate);

    _args.add(theOutput.getAbsolutePath());

    try
    {
      int _ret = execFFmpeg(_args);
      if (_ret != 0)
      {
        Logger.getLogger(FFMpegConverter.class.getName()).log(Level.SEVERE, "Fehler bei Konvertierung",
            String.format("FFMpeg hat einen Fehler gemeldet (%d)", _ret));
        throw new ConvertException(String.format("FFMpeg hat einen Fehler gemeldet (%d)", _ret));
      }
    }
    catch (IOException _e)
    {
      Logger.getLogger(FFMpegConverter.class.getName()).log(Level.SEVERE, "Fehler bei Konvertierung", _e);
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
      _tmp.add(_path + File.separatorChar + "ffmpeg");
    else
      _tmp.add("ffmpeg");

    if (theArguments != null)
      _tmp.addAll(theArguments);

    ProcessBuilder _builder = new ProcessBuilder(_tmp);

    Logger.getLogger(FFMpegConverter.class.getName()).log(Level.INFO,String.format("FFMpeg Command: %s",getCommand(_tmp)));
    _builder.redirectErrorStream(true);
    Process _p = _builder.start();

    StringBuffer _sb = new StringBuffer();
    Scanner _s = new Scanner(new BufferedInputStream(_p.getInputStream()));
    while (_s.hasNext())
    {
      String _out = _s.nextLine();
      _sb.append(_out);
      _sb.append("\n");
      
    }
    _s.close();

    Logger.getLogger(FFMpegConverter.class.getName()).log(Level.INFO, String.format("FFMpeg Output: %s",_sb.toString()));
    
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
    return "FFMpeg Konverter";
  }

  @Override
  public String getId()
  {
    return "ffmpeg";
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
          if (s.endsWith("\\"))
          {
            _cmdbuf.append("\\");
          }
          _cmdbuf.append('"');
        }
        else if (s.endsWith("\""))
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
    _ret.add("*.mp3");
    _ret.add("*.wav");
    _ret.add("*.flac");
    _ret.add("*.ape");
    _ret.add("*.aac");
    
    return _ret;
  }

  @Override
  public boolean canConvert()
  {
    try
    {
      execFFmpeg(Arrays.asList(new String[] { "-version" }));
    }
    catch (IOException _e)
    {
      return false;
    }
    
    return true;
  }
}
