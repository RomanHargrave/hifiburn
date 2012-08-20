/*
 * HifiBurn 2012
 * 
 * AudioFileManager.java
 */
package de.hifiburn.logic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;

import de.hifiburn.converter.IConverter;
import de.hifiburn.model.Track;

public class AudioFileManager
{
  private static AudioFileManager instance = null;
  
  /**
   * 
   */
  private AudioFileManager()
  {
    super();
  }
  
  public static AudioFileManager getInstance()
  {
    if (instance==null)
      instance = new AudioFileManager();
    
    return instance;
  }

  public List<String> getExtensions()
  {
    Set<String> _ret = new HashSet<String>();
    for (IConverter _conv : ConvertManager.getInstance().getConverter())
    {
      for (String _s : _conv.getExtension())
        _ret.add(_s);
    }
    
    return new ArrayList<String>(_ret);
  }
  
  public boolean isAudioFile(File theFile)
  {
    if (!theFile.exists() || !theFile.canRead())
      return false;
    
    try
    {
      if (AudioFileIO.read(theFile)==null)
        return false;
    }
    catch (Exception _e)
    {
      return false;
    }
    
    return true;
    
  }

  public void readMetaData(Track theTrack, File theFile) throws IOException
  {
    if (!theFile.exists() || !theFile.canRead())
      throw new IOException(String.format("Could not read metadata from file %s.",theFile.getAbsoluteFile()));
    
    AudioFile _af = null;
    try
    {
      _af = AudioFileIO.read(theFile);
    }
    catch (Exception _e)
    {
      new IOException(String.format("Could not read metadata from file %s (%s).",theFile.getAbsoluteFile(), _e.getMessage()));
    }
    
    if (_af==null)
    {
      theTrack.setDuration(0);
      theTrack.setInterpret("");
      theTrack.setTitle("");
      theTrack.setAlbumtitle("");
      theTrack.setAlbuminterpret("");
      theTrack.setSongwriter("");
      return;
    }
    
    Tag _tag = _af.getTag();
    AudioHeader _ah = _af.getAudioHeader();
    
    theTrack.setDuration(_ah.getTrackLength());
    theTrack.setInterpret(getKey(_tag,FieldKey.ARTIST));
    theTrack.setTitle(getKey(_tag,FieldKey.TITLE));
    theTrack.setAlbumtitle(getKey(_tag,FieldKey.ALBUM));
    theTrack.setAlbuminterpret(getKey(_tag,FieldKey.ALBUM_ARTIST));
    theTrack.setSongwriter(getKey(_tag,FieldKey.COMPOSER));
  }

  protected String getKey(Tag theTag, FieldKey theKey)
  {
    try
    {
      return theTag.getFirst(theKey);
    }
    catch (KeyNotFoundException _e)
    {
      return "";
    }
    catch (UnsupportedOperationException _e)
    {
      return "";
    }
  }
}
