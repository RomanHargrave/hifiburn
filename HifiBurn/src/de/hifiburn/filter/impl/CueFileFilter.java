/*
 * HifiBurn 2012
 *
 * CueFileFilter.java
 */
package de.hifiburn.filter.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import de.hifiburn.filter.FilterException;
import de.hifiburn.filter.IFilter;
import de.hifiburn.model.Project;
import de.hifiburn.model.Track;

/**
 * Filter for creating a cue file for the current project.
 */
public class CueFileFilter implements IFilter
{

  @Override
  public String getId()
  {
    return "cuefile";
  }

  @Override
  public String getName()
  {
    return "CUE File Filter";
  }

  @Override
  public void doPreFiltering(Project theProject) throws FilterException
  {
    try
    {
      File _cue = File.createTempFile("hifiburn", ".cue");
      theProject.getDisc().setCuefile(_cue);
      
      BufferedWriter _w = null;
      try
      {
        _w = new BufferedWriter(
                new OutputStreamWriter(
                      new FileOutputStream(_cue),Charset.forName("utf-8")));
        
        writeCueFile(theProject,_w);
      }
      finally
      {
        if (_w!=null)
        {
          _w.flush();
          _w.close();
        }
      }
      
      
      
    }
    catch (IOException _e)
    {
      throw new FilterException(_e.getMessage());
    }
    
  }

  protected void writeCueFile(Project theProject, BufferedWriter theWriter) throws IOException
  {
    if (isSet(theProject.getDisc().getCatalognumber()))
      theWriter.write(String.format("CATALOG %13s\n",theProject.getDisc().getCatalognumber()));
    
    if (isSet(theProject.getDisc().getInterpret()))
      theWriter.write(String.format("PERFORMER \"%s\"\n",theProject.getDisc().getInterpret()));
    
    if (isSet(theProject.getDisc().getSongwriter()))
      theWriter.write(String.format("SONGWRITER \"%s\"\n",theProject.getDisc().getSongwriter()));
    
    if (isSet(theProject.getDisc().getAlbum()))
      theWriter.write(String.format("TITLE \"%s\"\n",theProject.getDisc().getAlbum()));
    
    int _counter=1;
    for (Track _track : theProject.getDisc().getTracks())
    {
      if (_track.getWavfile()!=null)
        theWriter.write(String.format("FILE \"%s\" WAVE\n",_track.getWavfile().getAbsoluteFile()));
      
      theWriter.write(String.format("  TRACK %02d AUDIO\n",_counter));
      
      theWriter.write(String.format("    FLAGS DCP\n"));
      
      if (isSet(_track.getTitle()))
        theWriter.write(String.format("    TITLE \"%s\"\n",_track.getTitle()));
      
      if (isSet(_track.getInterpret()))
        theWriter.write(String.format("    PERFORMER \"%s\"\n",_track.getInterpret()));
      
      if (isSet(_track.getSongwriter()))
        theWriter.write(String.format("    SONGWRITER \"%s\"\n",_track.getSongwriter()));
      
      if (isSet(_track.getIsrc()))
        theWriter.write(String.format("    ISRC \"%12s\"\n",_track.getIsrc()));
      
      if (_counter>1)
        theWriter.write(String.format("    PREGAP 00:%02d:00\n", _track.getPregap()));
      
      theWriter.write(String.format("    INDEX 01 00:00:00\n"));
      
      _counter++;
    }
    
    theWriter.write("\n");
  }

  @Override
  public void doPostFiltering(Project theProject)
  {
    // Delete tmp files 
    if (theProject.getDisc().getCuefile()!=null && theProject.getDisc().getCuefile().exists())
      theProject.getDisc().getCuefile().delete();
  }
  
  public boolean isSet(String theString)
  {
    if (theString!=null && theString.trim().length()>0)
      return true;
    
    return false;
  }

}


