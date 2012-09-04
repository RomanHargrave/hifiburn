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

import de.hifiburn.i18n.Messages;
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
    
    for (String _s : ConvertManager.getInstance().getConverter(null,null).getExtension())
        _ret.add(_s);

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

  public void fillMetaData(Track theTrack, File theFile) throws IOException
  {
    if (!theFile.exists() || !theFile.canRead())
      throw new IOException(String.format(Messages.AudioFileManager_0,theFile.getAbsoluteFile()));
    
    AudioFile _af = null;
    try
    {
      _af = AudioFileIO.read(theFile);
    }
    catch (Exception _e)
    {
      new IOException(String.format(Messages.AudioFileManager_1,theFile.getAbsoluteFile(), _e.getMessage()));
    }
    
    if (_af==null)
    {
      theTrack.setDuration(0);
      theTrack.setInterpret(""); //$NON-NLS-1$
      theTrack.setTitle(""); //$NON-NLS-1$
      theTrack.setAlbumtitle(""); //$NON-NLS-1$
      theTrack.setAlbuminterpret(""); //$NON-NLS-1$
      theTrack.setSongwriter(""); //$NON-NLS-1$
      return;
    }
    
    Tag _tag = _af.getTag();
    AudioHeader _ah = _af.getAudioHeader();
    
    theTrack.setDuration(_ah.getTrackLength());

    // maybe some metadata was setup before by cue loading
    if (theTrack.getInterpret()==null)
      theTrack.setInterpret(getKey(_tag,FieldKey.ARTIST));
    if (theTrack.getTitle()==null)
      theTrack.setTitle(getKey(_tag,FieldKey.TITLE));
    if (theTrack.getAlbumtitle()==null)
      theTrack.setAlbumtitle(getKey(_tag,FieldKey.ALBUM));
    if (theTrack.getAlbuminterpret()==null)
      theTrack.setAlbuminterpret(getKey(_tag,FieldKey.ALBUM_ARTIST));
    if (theTrack.getSongwriter()==null)
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
      return ""; //$NON-NLS-1$
    }
    catch (UnsupportedOperationException _e)
    {
      return ""; //$NON-NLS-1$
    }
  }
}
