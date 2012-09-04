/*
 * HifiBurn 2012
 *
 * Util.java
 */
package de.hifiburn.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hifiburn.model.Disc;
import de.hifiburn.model.Track;

public class Util
{
  public static String formatTime(Integer theTime)
  {
    int _hours = theTime / 3600;
    int _minutes = (theTime / 60) % 60;
    int _seconds = theTime % 60;
    return String.format("%02d:%02d:%02d", _hours, _minutes, _seconds); //$NON-NLS-1$
  }

  public static String formatTimeMinutes(Integer theTime)
  {
    int _minutes = (theTime / 60) % 60;
    int _seconds = theTime % 60;
    return String.format("%d:%d:00", _minutes, _seconds); //$NON-NLS-1$
  }

  public static Disc createTracksFromCue(File theCue)
      throws IOException
  {
    Disc _disc = new Disc();
    List<Track> _tracks = new ArrayList<Track>();
    
    BufferedReader _br = new BufferedReader(new FileReader(theCue));
    String _line;
    
    Track _t = null;
    String _albumtitle = null;
    String _albuminterpret = null;
    String _file = null;
    Integer _laststart = null;
    Integer _secs = null;
    
    Track _prev = null;
    
    while ((_line = _br.readLine()) != null)
    {
      String _title = null;
      String _performer = null;
      String _songwriter = null;
      String _catalog = null;
      String _no = null;
      String _start = null;
      
      
      String _trim = _line.trim().replace("\"", "");
      if (_trim.toUpperCase().startsWith("TITLE"))
        _title = _trim.substring(_trim.indexOf(' '));
      if (_trim.toUpperCase().startsWith("PERFORMER"))
        _performer = _trim.substring(_trim.indexOf(' '));
      if (_trim.toUpperCase().startsWith("SONGWRITER"))
        _songwriter = _trim.substring(_trim.indexOf(' '));
      if (_trim.toUpperCase().startsWith("FILE"))
        _file = _trim.substring(_trim.indexOf(' ')+1,_trim.lastIndexOf(' '));
      if (_trim.toUpperCase().startsWith("INDEX"))
      {
        if (_trim.indexOf(' ')!=_trim.lastIndexOf(' '))
          _start = _trim.substring(_trim.lastIndexOf(' ')+1);
      }
      if (_trim.toUpperCase().startsWith("TRACK"))
      {
        if (_t!=null)
          _tracks.add(_t);
        
        _no = _trim.substring(_trim.indexOf(' ')+1,_trim.lastIndexOf(' '));
        _prev = _t;
        _t = new Track();
      }
      
      if (_file==null)
      {
        if (_title!=null)
        {
          _disc.setAlbum(_title);
          _albumtitle = _title;
        }
        if (_performer!=null)
        {
          _disc.setInterpret(_performer);
          _albuminterpret = _performer;
        }
        
        if (_songwriter!=null)
          _disc.setSongwriter(_songwriter);
        
        if (_catalog!=null)
          _disc.setCatalognumber(_catalog);
      }
      else
      {
        // try to verify the file
        File _f = new File(_file);
        if (!_f.exists())
        {
          _f = new File(theCue.getParentFile(),_f.getName());
          if (!_f.exists())
          {
            _file = null;
            continue;
          }
        }
        if (_t!=null)
        {
          if (_title!=null)
            _t.setTitle(_title);
          if (_performer!=null)
          _t.setInterpret(_performer);
          if (_songwriter!=null)
          _t.setSongwriter(_songwriter);
          if (_albuminterpret!=null)
            _t.setAlbuminterpret(_albuminterpret);
          if (_albumtitle!=null)
          _t.setAlbumtitle(_albumtitle);
          if (_f!=null)
            _t.setFile(_f);
          if (_start!=null)
          {
            // set of length does not work!
            _secs = Util.calcSeconds(_start);
            _t.setStart(_secs);
            if (_laststart!=null && _prev!=null)
              _prev.setLength(_secs - _laststart);
            
            _laststart = _secs;
          }
          
          if (_no!=null)
            _t.setNo(Integer.parseInt(_no));
        }
      }
    }
    
    if (_prev!=null && _laststart!=null)
      _prev.setLength(_secs - _laststart);
    
    _disc.setTracks(_tracks);
    return _disc;
  }

  private static Integer calcSeconds(String theStart) throws NumberFormatException
  {
    Integer _secs = 0;
    
    if (theStart==null)
      return null;
    
    // TODO Maybe calculate in frames instead of seconds
    String _parts[] = theStart.replace(" ","").split(":");
    if (_parts.length==3) // mm:ss:ff
      _secs = (Integer.parseInt(_parts[0])*60)+Integer.parseInt(_parts[1])+(int)Math.round((Integer.parseInt(_parts[2])*(1/75d)));
    if (_parts.length==2) // ss:ff
      _secs = Integer.parseInt(_parts[1])+(int)Math.round((Integer.parseInt(_parts[0])*(1/75d)));
    if (_parts.length==1) // ss
      _secs = Integer.parseInt(_parts[1]);
      
    return _secs;
  }
}
