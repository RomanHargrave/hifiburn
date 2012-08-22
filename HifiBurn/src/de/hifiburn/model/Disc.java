/*
 * HifiBurn 2012
 * 
 * Disc.java
 */
package de.hifiburn.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Disc extends AbstractModelObject
{
  protected String album = null;
  protected String interpret = null;
  protected String songwriter = null;
  protected File cuefile = null;
  protected File tocfile = null;
  protected String catalognumber = null;
  
  protected List<Track> tracks = null;
 
  /**
   * 
   */
  public Disc()
  {
    super();
    tracks = new ArrayList<Track>();
  }

  /**
   * @return Returns the album.
   */
  public String getAlbum()
  {
    return album;
  }

  /**
   * @param theAlbum The album to set.
   */
  public void setAlbum(String theAlbum)
  {
    firePropertyChange("album", album, album = theAlbum); //$NON-NLS-1$
  }

  /**
   * @return Returns the interpret.
   */
  public String getInterpret()
  {
    return interpret;
  }

  /**
   * @param theInterpret The interpret to set.
   */
  public void setInterpret(String theInterpret)
  {
    firePropertyChange("interpret", interpret, interpret = theInterpret); //$NON-NLS-1$
  }

  public List<Track> getTracks() 
  {
    return tracks;
  }
  
  /**
   * @return Returns the cuefile.
   */
  public File getCuefile()
  {
    return cuefile;
  }

  /**
   * @param theCuefile The cuefile to set.
   */
  public void setCuefile(File theCuefile)
  {
    firePropertyChange("cuefile", cuefile, cuefile = theCuefile); //$NON-NLS-1$
  }

  /**
   * @return Returns the songwriter.
   */
  public String getSongwriter()
  {
    return songwriter;
  }

  /**
   * @param theSongwriter The songwriter to set.
   */
  public void setSongwriter(String theSongwriter)
  {
    firePropertyChange("songwriter", songwriter, songwriter = theSongwriter); //$NON-NLS-1$
  }

  /**
   * @return Returns the catalognumber.
   */
  public String getCatalognumber()
  {
    return catalognumber;
  }

  /**
   * @param theCatalognumber The catalognumber to set.
   */
  public void setCatalognumber(String theCatalognumber)
  {
    firePropertyChange("catalognumber", catalognumber, catalognumber = theCatalognumber); //$NON-NLS-1$
  }

  /**
   * @return Returns the tocfile.
   */
  public File getTocfile()
  {
    return tocfile;
  }

  /**
   * @param theTocfile The tocfile to set.
   */
  public void setTocfile(File theTocfile)
  {
    firePropertyChange("tocfile", tocfile, tocfile = theTocfile); //$NON-NLS-1$
  }

  /**
   * @param theTracks The tracks to set.
   */
  public void setTracks(List<Track> theTracks)
  {
    firePropertyChange("tracks",tracks,tracks = theTracks); //$NON-NLS-1$
    if (tracks==null)
    {
      List<Track> _tmp = new ArrayList<Track>();
      setTracks(_tmp);
    }
  }
  
  public Track getTrack(String theFilename)
  {
    for (Track _t : tracks)
      if (_t.getFile().getAbsoluteFile().toString().equals(theFilename))
        return _t;
    
    return null;
  }
  
  public void exchangeTracks(Track theSource, Track theTarget)
  {
    Collections.swap(tracks,theSource.getNo()-1, theTarget.getNo()-1);
    
    for (int _i=0; _i<tracks.size(); _i++)
      tracks.get(_i).setNo(_i+1);
  }
  
  public void moveTrack(Track theSource, Track theTarget)
  {
    tracks.remove(theSource.getNo()-1);
    tracks.add(theTarget.getNo()-1, theSource);
    
    for (int _i=0; _i<tracks.size(); _i++)
      tracks.get(_i).setNo(_i+1);
  }
  
  public void addTrack(Track theTrack)
  {
    tracks.add(theTrack);
    
    for (int _i=0; _i<tracks.size(); _i++)
      tracks.get(_i).setNo(_i+1);
  }
  
  public void removeTrack(Track theTrack)
  {
    tracks.remove(theTrack.getNo()-1);
    
    for (int _i=0; _i<tracks.size(); _i++)
      tracks.get(_i).setNo(_i+1);
  }
  
    
//  public void reset()
//  {
//    setAlbum(null);
//    setCatalognumber(null);
//    setCuefile(null);
//    setInterpret(null);
//    setSongwriter(null);
//    setTocfile(null);
//  }
}
