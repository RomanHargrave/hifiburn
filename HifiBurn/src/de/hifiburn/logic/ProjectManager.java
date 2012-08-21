/*
 * HifiBurn 2012
 * 
 * ProjectManager.java
 */
package de.hifiburn.logic;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import de.hifiburn.burner.BurnerException;
import de.hifiburn.burner.IBurner;
import de.hifiburn.burner.impl.CdrdaoBurner;
import de.hifiburn.converter.ConvertException;
import de.hifiburn.converter.Format;
import de.hifiburn.converter.IConverter;
import de.hifiburn.converter.impl.FFMpegConverter;
import de.hifiburn.converter.impl.JLibAVConverter;
import de.hifiburn.filter.FilterException;
import de.hifiburn.filter.IFilter;
import de.hifiburn.filter.impl.CueFileFilter;
import de.hifiburn.filter.impl.TocFileFilter;
import de.hifiburn.i18n.Messages;
import de.hifiburn.model.Project;
import de.hifiburn.model.Track;
import de.hifiburn.ui.swt.TextWidgetLogHandler;

public class ProjectManager
{
  private static ProjectManager instance = null;
  
  protected Project project = null;

  private File logfile = null;

  private TextWidgetLogHandler logwidget;

  /**
   * @return Returns the project.
   */
  public Project getProject()
  {
    return project;
  }

  /**
   * @param theProject The project to set.
   */
  public void setProject(Project theProject)
  {
    project = theProject;
  }

  /**
   * 
   */
  private ProjectManager()
  {
    super();
    newProject();
  }
  
  public static ProjectManager getInstance()
  {
    if (instance==null)
      instance = new ProjectManager();
    
    return instance;
  }
  
  public void newProject()
  {
    project = new Project();
  }
  
  public void initialize() throws IOException
  {
    // init logging
    Logger _log = Logger.getLogger(""); //$NON-NLS-1$
    for (Handler _h : _log.getHandlers())
      _log.removeHandler(_h);
    
    _log.setLevel(Level.INFO);
    _log.addHandler(new ConsoleHandler());
    logfile = File.createTempFile("hifiburn", ".log"); //$NON-NLS-1$ //$NON-NLS-2$
    FileHandler _fh = new FileHandler(logfile.getAbsolutePath(), 100000, 1, true);
    _fh.setFormatter(new SimpleFormatter());
    _log.addHandler(_fh);
    logwidget = new TextWidgetLogHandler();
    _log.addHandler(logwidget);
    
    // init Preferences
    PreferenceManager.getInstance();
  }
  
  public void postUIInitialize() throws BurnerException,ConvertException,InitializeException
  {
    // register converter
    ConvertManager.getInstance().registerConverter(new FFMpegConverter());
    ConvertManager.getInstance().registerConverter(new JLibAVConverter());
      
    // register filter
    FilterManager.getInstance().registerFilter(new CueFileFilter());
    FilterManager.getInstance().registerFilter(new TocFileFilter());
      
    // register burner
    //BurnerManager.getInstance().registerBurner(new WodimBurner());
    BurnerManager.getInstance().registerBurner(new CdrdaoBurner());
  }
  
  /**
   * @param theLogwidget The logwidget to set.
   */
  public TextWidgetLogHandler getLogwidget()
  {
    return logwidget;
  }

  /**
   * @return Returns the logfile.
   */
  public File getLogfile()
  {
    return logfile;
  }

  public void addTracks(List<File> theFiles) throws IOException
  {
    StringBuilder _errors = new StringBuilder();
    for (File _file : theFiles)
    {
      if (!_file.exists() || !_file.canRead())
      {
        _errors.append(String.format(Messages.ProjectManager_0, _file.getAbsoluteFile()));
        _errors.append("\n"); //$NON-NLS-1$
        continue;
      }
    }
    
    if (_errors.length()>0)
      throw new IOException(_errors.toString());
    
    for (File _file : theFiles)
    {
      if (AudioFileManager.getInstance().isAudioFile(_file))
      {
        Track _t = new Track();
        _t.setFile(_file);
        AudioFileManager.getInstance().readMetaData(_t, _file);
        
        project.getDisc().addTrack(_t);
      }
    }
    
    
  }

  public int convertAudioFiles(IProgressMonitor theMonitor)
  {
    int _errors = 0;

    try
    {
      theMonitor.beginTask("", project.getDisc().getTracks().size()*2); //$NON-NLS-1$
      
      // first stage: convert
      for (Track _t : project.getDisc().getTracks())
      {
        if (!_t.getFile().exists() || !_t.getFile().canRead())
        {
          Logger.getLogger(ProjectManager.class.getName()).log(Level.SEVERE, 
              String.format(Messages.ProjectManager_1, _t.getFile().getAbsoluteFile()));
          _errors++;
          continue;
        }
          
        IConverter _c = ConvertManager.getInstance().getConverter(_t.getFile(),Format.WAV);
        if (_c==null)
        {
          Logger.getLogger(ProjectManager.class.getName()).log(Level.SEVERE, 
              String.format(Messages.ProjectManager_2, _t.getFile().getAbsoluteFile()));
          _errors++;
          continue;
        }
        if (!_c.canConvert())
        {
          Logger.getLogger(ProjectManager.class.getName()).log(Level.SEVERE, 
              String.format(Messages.ProjectManager_3));
          return 1;
        }
        
        theMonitor.subTask(String.format(Messages.ProjectManager_4,_t.getFile()));
        
        try
        {
          theMonitor.worked(1);
          if (theMonitor.isCanceled())
          {
            Logger.getLogger(ProjectManager.class.getName()).log(Level.SEVERE,Messages.ProjectManager_5);
            return -1;
          }
          
          File _tmp = File.createTempFile("hifiburn", ".wav"); //$NON-NLS-1$ //$NON-NLS-2$
          Logger.getLogger(ProjectManager.class.getName()).log(Level.INFO, String.format(Messages.ProjectManager_6,
              _c.getName()));
          
          _c.convert(_t.getFile(), _tmp, Format.WAV, 44100, 16);
          _t.setWavfile(_tmp);
          theMonitor.worked(1);
        }
        catch (ConvertException _e)
        {
          Logger.getLogger(ProjectManager.class.getName()).log(Level.SEVERE, 
              String.format(Messages.ProjectManager_7, _t.getFile().getAbsoluteFile(), _e.getMessage()));
          _errors++;
          continue;
        }
        catch (IOException _e)
        {
          Logger.getLogger(ProjectManager.class.getName()).log(Level.SEVERE, 
              String.format(Messages.ProjectManager_8, _t.getFile().getAbsoluteFile(), _e.getMessage()));
          _errors++;
          continue;
        }
      }
    
      // second stage: ask for burning
      if (_errors>0)
      {
        Display.getDefault().asyncExec(new Runnable() 
        {
          public void run() 
          {
            MessageDialog.openError(Display.getDefault().getActiveShell(), 
                Messages.ProjectManager_9, Messages.ProjectManager_10);
            Logger.getLogger(ProjectManager.class.getName()).log(Level.SEVERE,
                Messages.ProjectManager_11);
            return;
          }
        });
        
        return _errors;
      }
      
      return 0;
    }
    finally
    {
      theMonitor.done();
    }
  }

  public void doPreProcessing(IProgressMonitor theMonitor, Project theProject, List<IFilter> theFilters) throws FilterException
  {
    try
    {
      theMonitor.beginTask("", theFilters.size()); //$NON-NLS-1$
      
      for (IFilter _filter : theFilters)
      {
        theMonitor.subTask(String.format(Messages.ProjectManager_13,_filter.getName()));
        try
        {
          Logger.getLogger(ProjectManager.class.getName()).log(Level.INFO, String.format(Messages.ProjectManager_14,
              _filter.getName()));
          _filter.doPreFiltering(theProject);
        }
        catch (FilterException _e)
        {
          Logger.getLogger(ProjectManager.class.getName()).log(Level.SEVERE, 
              String.format(Messages.ProjectManager_15, _filter.getName(), _e.getMessage()));
          throw _e;
        }
        theMonitor.worked(1);
      }
    }
    finally
    {
      theMonitor.done();
    }
  }
  
  public void doPostProcessing(IProgressMonitor theMonitor, Project theProject, List<IFilter> theFilters) throws FilterException
  {
    try
    {
      theMonitor.beginTask("", theFilters.size()); //$NON-NLS-1$
      
      for (IFilter _filter : theFilters)
      {
        theMonitor.subTask(String.format(Messages.ProjectManager_16,_filter.getName()));
        try
        {
          Logger.getLogger(ProjectManager.class.getName()).log(Level.INFO, String.format(Messages.ProjectManager_17,
              _filter.getName()));
          _filter.doPostFiltering(theProject);
        }
        catch (FilterException _e)
        {
          Logger.getLogger(ProjectManager.class.getName()).log(Level.SEVERE, 
              String.format(Messages.ProjectManager_18, _filter.getName(), _e.getMessage()));
          throw _e;
        }
        theMonitor.worked(1);
      }
    }
    finally
    {
      theMonitor.done();
    }
  }
  
  public void doBurn(IProgressMonitor theMonitor, final Project theProject, final IBurner theBurner) throws BurnerException
  {
    try
    {
      theMonitor.beginTask("", 100); //$NON-NLS-1$
      
      theMonitor.subTask(String.format(Messages.ProjectManager_19, theBurner.getName()));
      
      Logger.getLogger(ProjectManager.class.getName()).log(Level.INFO, String.format(Messages.ProjectManager_20,
            theBurner.getName()));
      
      theBurner.setMonitor(theMonitor);

      try
      {
        
        theBurner.burn(theProject.getDisc());
      }
      catch (BurnerException _e)
      {
        Logger.getLogger(ProjectManager.class.getName()).log(Level.SEVERE, 
            String.format(Messages.ProjectManager_21, theBurner.getName(), _e.getMessage()));
        throw _e;
      }

    }
    finally
    {
      theMonitor.done();
    }
  }
}
