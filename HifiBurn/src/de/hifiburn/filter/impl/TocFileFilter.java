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
public class TocFileFilter implements IFilter
{

  @Override
  public String getId()
  {
    return "tocfile";
  }

  @Override
  public String getName()
  {
    return "TOC File Filter";
  }

  @Override
  public void doPreFiltering(Project theProject) throws FilterException
  {
    try
    {
      File _cue = File.createTempFile("hifiburn", ".toc");
      theProject.getDisc().setTocfile(_cue);
      
      BufferedWriter _w = null;
      try
      {
        _w = new BufferedWriter(
                new OutputStreamWriter(
                      new FileOutputStream(_cue),Charset.forName("utf-8")));
        
        writeTocFile(theProject,_w);
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

  protected void writeTocFile(Project theProject, BufferedWriter theWriter) throws IOException
  {
    if (isSet(theProject.getDisc().getCatalognumber()))
      theWriter.write(String.format("CATALOG \"13%s\"\n",theProject.getDisc().getCatalognumber()));
    
    theWriter.write(String.format("CD_DA\n"));
    
    // cdtext
    theWriter.write(String.format("CD_TEXT {\n"));
    
    theWriter.write(String.format(" LANGUAGE 0 {\n"));
    
    if (isSet(theProject.getDisc().getInterpret()))
      theWriter.write(String.format("    PERFORMER \"%s\"\n",theProject.getDisc().getInterpret()));
    
    if (isSet(theProject.getDisc().getSongwriter()))
      theWriter.write(String.format("    SONGWRITER \"%s\"\n",theProject.getDisc().getSongwriter()));
    
    if (isSet(theProject.getDisc().getAlbum()))
      theWriter.write(String.format("    TITLE \"%s\"\n",theProject.getDisc().getAlbum()));
    
    theWriter.write(String.format("  }\n"));
    theWriter.write(String.format("}\n"));
    
    int _counter=1;
    for (Track _track : theProject.getDisc().getTracks())
    {
      theWriter.write(String.format("TRACK AUDIO\n",_counter));
      theWriter.write(String.format("  COPY\n"));
      
      if (isSet(_track.getIsrc()))
        theWriter.write(String.format("  ISRC \"%12s\"\n", _track.getIsrc()));
      
      // cdtext
      theWriter.write(String.format("  CD_TEXT {\n"));
      
      theWriter.write(String.format("   LANGUAGE 0 {\n"));
      if (isSet(_track.getTitle()))
        theWriter.write(String.format("      TITLE \"%s\"\n",_track.getTitle()));
        
      if (isSet(_track.getInterpret()))
        theWriter.write(String.format("      PERFORMER \"%s\"\n",_track.getInterpret()));
      
      if (isSet(_track.getSongwriter()))
        theWriter.write(String.format("      SONGWRITER \"%s\"\n",_track.getSongwriter()));
      
      theWriter.write(String.format("    }\n"));
      theWriter.write(String.format("  }\n"));

      if (_counter>1 && _track.getPregap()>0)
        theWriter.write(String.format("  PREGAP 00:%02d:00\n", _track.getPregap()));
      
      if (_track.getWavfile()!=null)
        theWriter.write(String.format("  AUDIOFILE \"%s\" 0\n",_track.getWavfile().getAbsoluteFile()));
      
      _counter++;
    }
    
    theWriter.write("\n");
  }

  @Override
  public void doPostFiltering(Project theProject)
  {
    // Delete tmp files 
    if (theProject.getDisc().getTocfile()!=null && theProject.getDisc().getTocfile().exists())
      theProject.getDisc().getTocfile().delete();
  }
  
  public boolean isSet(String theString)
  {
    if (theString!=null && theString.trim().length()>0)
      return true;
    
    return false;
  }

}


