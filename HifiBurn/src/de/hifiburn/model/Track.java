/*
 * HifiBurn 2012
 * 
 * Track.java
 */
package de.hifiburn.model;

import java.io.File;

public class Track extends AbstractModelObject
{
  protected Integer no = null;
  protected String interpret = null;
  protected String songwriter = null;
  protected String title = null;
  protected String albuminterpret = null;
  protected String albumtitle = null;
  protected Integer duration = null;
  protected File file = null;
  protected File wavfile = null;
  protected File cdtextfile = null;
  protected String isrc = null;
  protected Integer pregap = 0;
  
  /**
   * 
   */
  public Track()
  {
    super();
  }
  
  /**
   * 
   */
  public Track(Integer theNo, String theTitle, Integer theDuration, String theFile)
  {
    super();
    no = theNo;
    title = theTitle;
    duration = theDuration;
    file = new File(theFile);
  }

  /**
   * @return Returns the no.
   */
  public Integer getNo()
  {
    return no;
  }

  /**
   * @param theNo The no to set.
   */
  public void setNo(Integer theNo)
  {
    firePropertyChange("no", no, no = theNo); //$NON-NLS-1$
  }

  /**
   * @return Returns the title.
   */
  public String getTitle()
  {
    return title;
  }

  /**
   * @param theTitle The title to set.
   */
  public void setTitle(String theTitle)
  {
    firePropertyChange("title", title, title = theTitle); //$NON-NLS-1$
  }

  /**
   * @return Returns the duration.
   */
  public Integer getDuration()
  {
    return duration;
  }

  /**
   * @param theDuration The duration to set.
   */
  public void setDuration(Integer theDuration)
  {
    firePropertyChange("duration", duration, duration = theDuration); //$NON-NLS-1$
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
   * @return Returns the file.
   */
  public File getFile()
  {
    return file;
  }

  /**
   * @param theFile The file to set.
   */
  public void setFile(File theFile)
  {
    firePropertyChange("file", file, file = theFile); //$NON-NLS-1$
  }

  /**
   * @return Returns the albuminterpret.
   */
  public String getAlbuminterpret()
  {
    return albuminterpret;
  }

  /**
   * @param theAlbuminterpret The albuminterpret to set.
   */
  public void setAlbuminterpret(String theAlbuminterpret)
  {
    firePropertyChange("albuminterpret", albuminterpret, albuminterpret = theAlbuminterpret); //$NON-NLS-1$
  }

  /**
   * @return Returns the albumtitle.
   */
  public String getAlbumtitle()
  {
    return albumtitle;
  }

  /**
   * @param theAlbumtitle The albumtitle to set.
   */
  public void setAlbumtitle(String theAlbumtitle)
  {
    firePropertyChange("albumtitle", albumtitle, albumtitle = theAlbumtitle); //$NON-NLS-1$
  }

  /**
   * @return Returns the wavfile.
   */
  public File getWavfile()
  {
    return wavfile;
  }

  /**
   * @param theWavfile The wavfile to set.
   */
  public void setWavfile(File theWavfile)
  {
    firePropertyChange("wavfile", wavfile, wavfile = theWavfile); //$NON-NLS-1$
  }

  /**
   * @return Returns the cdtextfile.
   */
  public File getCdtextfile()
  {
    return cdtextfile;
  }

  /**
   * @param theCdtextfile The cdtextfile to set.
   */
  public void setCdtextfile(File theCdtextfile)
  {
    firePropertyChange("cdtextfile", cdtextfile, cdtextfile = theCdtextfile); //$NON-NLS-1$
  }

  /**
   * @return Returns the isrc.
   */
  public String getIsrc()
  {
    return isrc;
  }

  /**
   * @param theIsrc The isrc to set.
   */
  public void setIsrc(String theIsrc)
  {
    firePropertyChange("isrc", isrc, isrc = theIsrc); //$NON-NLS-1$
  }

  /**
   * @return Returns the pregap.
   */
  public Integer getPregap()
  {
    return pregap;
  }

  /**
   * @param thePregap The pregap to set.
   */
  public void setPregap(Integer thePregap)
  {
    firePropertyChange("pregap", pregap, pregap = thePregap); //$NON-NLS-1$
  }
  
  
  
}
