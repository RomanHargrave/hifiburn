/*
 * HifiBurn 2012
 * 
 * JLibAVConverter.java
 */
package de.hifiburn.converter.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.libav.DefaultMediaDecoder;
import org.libav.DefaultMediaEncoder;
import org.libav.IDecoder;
import org.libav.IEncoder;
import org.libav.IMediaDecoder;
import org.libav.IMediaEncoder;
import org.libav.IMediaReader;
import org.libav.IMediaWriter;
import org.libav.LibavException;
import org.libav.avcodec.CodecWrapperFactory;
import org.libav.avcodec.ICodecContextWrapper;
import org.libav.bridge.LibraryManager;
import org.libav.video.FrameScaler;

import de.hifiburn.converter.ConvertException;
import de.hifiburn.converter.IConverter;
import de.hifiburn.i18n.Messages;

public class JLibAVConverter implements IConverter
{
  
  protected Map<Integer,String> supportedExtension = new HashMap<Integer,String>();

  /**
   * 
   */
  public JLibAVConverter()
  {
    super();
    supportedExtension.put(CodecWrapperFactory.CODEC_ID_AAC, "aac");
    supportedExtension.put(CodecWrapperFactory.CODEC_ID_APE, "ape");
    supportedExtension.put(CodecWrapperFactory.CODEC_ID_EAC3, "ac3");
    supportedExtension.put(CodecWrapperFactory.CODEC_ID_AC3, "ac3");
    supportedExtension.put(CodecWrapperFactory.CODEC_ID_FLAC, "flac");
    supportedExtension.put(CodecWrapperFactory.CODEC_ID_MP3, "mp3");
    supportedExtension.put(CodecWrapperFactory.CODEC_ID_MP2, "mp2");
    supportedExtension.put(CodecWrapperFactory.CODEC_ID_PCM_S16LE, "wav");
    supportedExtension.put(CodecWrapperFactory.CODEC_ID_TTA, "tta");
    supportedExtension.put(CodecWrapperFactory.CODEC_ID_TWINVQ, "tfq");
    supportedExtension.put(CodecWrapperFactory.CODEC_ID_VORBIS, "ogg");
    supportedExtension.put(CodecWrapperFactory.CODEC_ID_WAVPACK, "wv");
    supportedExtension.put(CodecWrapperFactory.CODEC_ID_WMALOSSLESS, "wma");
    supportedExtension.put(CodecWrapperFactory.CODEC_ID_WMAPRO, "wma");
    supportedExtension.put(CodecWrapperFactory.CODEC_ID_WMAV1, "wma");
    supportedExtension.put(CodecWrapperFactory.CODEC_ID_WMAVOICE, "wma");
  }

  @Override
  public void convert(File theInput, File theOutput, de.hifiburn.converter.Format theOutputFormat, int theBitrate, int theSamplerate)
      throws ConvertException
  {
    String _srcUrl = theInput.getAbsolutePath();
    String _dstUrl = theOutput.getAbsolutePath();
    int _audioCodecId = CodecWrapperFactory.CODEC_ID_PCM_S16LE;
    int _videoCodecId = CodecWrapperFactory.CODEC_ID_MPEG2VIDEO; // output video codec
    
    if (theOutputFormat.equals(de.hifiburn.converter.Format.WAV))
      _audioCodecId = CodecWrapperFactory.CODEC_ID_PCM_S16LE;
    else
      throw new ConvertException(Messages.JLibAVConverter_0);

    IMediaDecoder _md = null;
    IMediaEncoder _me = null;
    IMediaReader _mr;
    IMediaWriter _mw;
    FrameScaler _scaler = null;

    try
    {
      _md = new DefaultMediaDecoder(_srcUrl); // open input file/stream
      _me = new DefaultMediaEncoder(_dstUrl, null); // open output file  //$NON-NLS-1$
      _mr = _md.getMediaReader();
      _mw = _me.getMediaWriter();

      IDecoder _dec;
      IEncoder _enc;
      ICodecContextWrapper _cc1,_cc2;
      int _si;

      // init video transcoding of the first video stream if there is at
      // least one video stream
      if (_mr.getVideoStreamCount() > 0) {
          _md.setVideoStreamDecodingEnabled(0, true);
          _dec = _md.getVideoStreamDecoder(0);
          _cc1 = _dec.getCodecContext();
          _si = _mw.addVideoStream(_videoCodecId, _cc1.getWidth(), _cc1.getHeight());
          _enc = _me.getVideoStreamEncoder(_si);
          _cc2 = _enc.getCodecContext();
          _cc2.setPixelFormat(_cc1.getPixelFormat());
          _scaler = new FrameScaler(_cc1.getWidth(), _cc1.getHeight(), _cc1.getPixelFormat(), _cc2.getWidth(), _cc2.getHeight(), _cc2.getPixelFormat());
          _scaler.addFrameConsumer(_enc);
          _dec.addFrameConsumer(_scaler);
      }
      
      // init audio transcoding of the first audio stream if there is at
      // least one audio stream
      if (_mr.getAudioStreamCount() > 0)
      {
        _md.setAudioStreamDecodingEnabled(0, true);
        _dec = _md.getAudioStreamDecoder(0);
        _cc1 = _dec.getCodecContext();
        _si = _mw.addAudioStream(_audioCodecId, _cc1.getSampleRate(), _cc1.getSampleFormat(), _cc1.getChannels());
        _dec.addFrameConsumer(_me.getAudioStreamEncoder(_si));
      }

      _mw.writeHeader(); // write file header
      boolean _hasNext = _mr.readNextPacket();
      while (_hasNext)
      {
        try
        {
          _hasNext = _mr.readNextPacket();
        }
        catch (LibavException ex)
        {
          Logger.getLogger(JLibAVConverter.class.getName()).log(Level.WARNING, Messages.JLibAVConverter_1, ex);
        }
      }
      _md.flush();
      _me.flush();
      _mw.writeTrailer(); // write file trailer
    }
    catch (Exception _e)
    {
      Logger.getLogger(JLibAVConverter.class.getName()).log(Level.SEVERE, Messages.JLibAVConverter_2, _e);
      throw new ConvertException(_e);
    }
    finally
    {
      try
      {
        if (_md != null)
          _md.close();
        if (_me != null)
          _me.close();
      }
      catch (Exception _e)
      {
        Logger.getLogger(JLibAVConverter.class.getName()).log(Level.SEVERE, Messages.JLibAVConverter_3, _e);
        throw new ConvertException(_e);
      }
    }
  }

  @Override
  public void initialize()
      throws ConvertException
  {
    // check libav
    if (canConvert()==false)
      throw new ConvertException(
          Messages.JLibAVConverter_4);
  }

  @Override
  public boolean supportFormat(File theInput, de.hifiburn.converter.Format theOutputFormat)
  {
    return true;
  }

  @Override
  public String getName()
  {
    return Messages.JLibAVConverter_5;
  }

  @Override
  public String getId()
  {
    return "libav";  //$NON-NLS-1$
  }

  @Override
  public List<String> getExtension()
  {
    List<String> _ret = new ArrayList<String>();
    
    final CodecWrapperFactory _f = CodecWrapperFactory.getInstance();
    for (Integer _codec : supportedExtension.keySet())
    {
      try
      {
        if (_f.findDecoder(_codec)!=null)
          _ret.add("*."+supportedExtension.get(_codec));  //$NON-NLS-1$
      }
      catch (LibavException _e)
      {
      }
    }
    
    return _ret;
  }

  @Override
  public boolean canConvert()
  {
    if (LibraryManager.getInstance().getAVCodecLibrary() == null
        || LibraryManager.getInstance().getAVFormatLibrary() == null
        || LibraryManager.getInstance().getAVUtilLibrary() == null)
      return false;
    
    return true;
  }
}
