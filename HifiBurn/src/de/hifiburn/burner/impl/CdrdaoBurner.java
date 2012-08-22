/*
 * HifiBurn 2012
 *
 * CdrdaoBurner.java
 */
package de.hifiburn.burner.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;

import de.hifiburn.burner.BurnerException;
import de.hifiburn.burner.IBurner;
import de.hifiburn.i18n.Messages;
import de.hifiburn.logic.ICommandListener;
import de.hifiburn.logic.IPreferenceConstants;
import de.hifiburn.logic.InitializeException;
import de.hifiburn.logic.PreferenceManager;
import de.hifiburn.model.Disc;

/*
 * Disabled because it has problems with not padded wave files in combination with cues.
 */
public class CdrdaoBurner implements IBurner
{
  private Pattern errorMatcher = Pattern.compile("ERROR:[\\s]*(.*)"); //$NON-NLS-1$
  private Pattern progressMatcher = Pattern.compile("Wrote\\s([\\d]*)\\sof\\s([\\d]*)[^\\d]*([\\d]*)%[^\\d]*([\\d]*).*"); //$NON-NLS-1$
  private Pattern progressDoneMatcher = Pattern.compile("Wrote\\s([\\d]*)[^\\d]*([\\d]*)%[^\\d]*([\\d]*).*"); //$NON-NLS-1$
  private Pattern trackMatcher = Pattern.compile("Writing[^\\d]*([\\d]*).*"); //$NON-NLS-1$
  private Pattern versionMathcer = Pattern.compile("Cdrdao\\sversion\\s([^\\.]*)\\.([^\\.])*\\.([^\\s]*).*"); //$NON-NLS-1$
  private Pattern drivesMatcher = Pattern.compile("([A-Z]):\\\\*"); //$NON-NLS-1$
  private Pattern speedMatcher = Pattern.compile("Maximum writing speed: ([\\d]*) kB/s"); //$NON-NLS-1$
  private Pattern busMatcher = Pattern.compile("([^\\s]*)\\s+:\\s+(.*)"); //$NON-NLS-1$
  
  
  private IProgressMonitor monitor = null;
  
  public boolean passVersion = false; 
  
  protected Integer maxSpeed = null; 
  
  @Override
  public void initialize() throws BurnerException, InitializeException
  {
    try
    {
      execCdrdao(Arrays.asList(new String[] { "scanbus" }), new ICommandListener() //$NON-NLS-1$
      {
        
        @Override
        public void newLine(String theLine)
        {
          Matcher _m = versionMathcer.matcher(theLine);
          if (_m.matches() && _m.groupCount()==3)
          {
            int _v1=Integer.parseInt(_m.group(1));
            int _v2=Integer.parseInt(_m.group(2));
            int _v3=Integer.parseInt(_m.group(3));
            
            if (_v1<1 || (_v1>0 && _v2<2) || (_v1>0 && _v2>1 && _v3<3))
              passVersion = false;
            else
              passVersion = true;
          }
        }
      }); //$NON-NLS-1$
    }
    catch (IOException _e)
    {
      throw new InitializeException(
          Messages.CdrdaoBurner_0);
    }
    
    if (!passVersion)
      throw new BurnerException(Messages.CdrdaoBurner_11);
    
    if (PreferenceManager.getInstance().getString(IPreferenceConstants.CDRDAO_DEVICE)==null || 
        PreferenceManager.getInstance().getString(IPreferenceConstants.CDRDAO_DEVICE).trim().length()==0)
    {
      throw new InitializeException(
          Messages.CdrdaoBurner_1);
    }
   
  }

  @Override
  public void burn(Disc theDisc)  throws BurnerException
  {
    List<String> _args = new ArrayList<String>();
    
    try
    {
      // for windows and cygwin we need to change the paths at the toc file for cdrdao
      if (System.getProperty("os.name").toLowerCase().indexOf("win")>=0)
      {
        BufferedReader _br = new BufferedReader(new FileReader(theDisc.getTocfile()));
        StringBuilder _sb = new StringBuilder();
        String _line;
        while ((_line = _br.readLine()) != null)   
        {
          _sb.append(_line);
        }
        _br.close();
        
        String _out = _sb.toString();
        Matcher _m = drivesMatcher.matcher(_out);
        String _replaced = _m.replaceAll("/cygdrive/$1/");
        
        BufferedWriter _bw = new BufferedWriter(new FileWriter(theDisc.getTocfile()));
        _bw.write(_replaced);
        _bw.flush();
        _bw.close();
      }
    }
    catch (FileNotFoundException _e1)
    {
      throw new BurnerException(_e1);
    }
    catch (IOException _e1)
    {
      throw new BurnerException(_e1);
    }
    
    final StringBuilder _errors = new StringBuilder();
    
    _args.add("write"); //$NON-NLS-1$
    _args.add("--overburn"); //$NON-NLS-1$
    _args.add("--reload"); //$NON-NLS-1$
    _args.add("-n"); //$NON-NLS-1$
    
    Boolean _simulate = PreferenceManager.getInstance().getBoolean(IPreferenceConstants.CDRDAO_SIMULATION);
    if (_simulate!=null && _simulate==true)
    {
      _args.add("--simulate"); //$NON-NLS-1$
    }
    
    String _speed = PreferenceManager.getInstance().getString(IPreferenceConstants.CDRDAO_SPEED);
    if (_speed!=null && _speed.trim().length()>0)
    {
      _args.add("--speed"); //$NON-NLS-1$
      _args.add(_speed); //$NON-NLS-1$
    }
    
    _args.add("--device"); //$NON-NLS-1$
    _args.add(PreferenceManager.getInstance().getString(IPreferenceConstants.CDRDAO_DEVICE));
    
    _args.add(theDisc.getTocfile().getAbsoluteFile().toString());
    
    try
    {
      int _ret = execCdrdao(_args, new ICommandListener()
      {
        int lastWorked = 0;
        
        @Override
        public void newLine(String theLine)
        {
          Matcher _m = errorMatcher.matcher(theLine);
          if (_m.matches())
          {
            _errors.append(_m.group(1));
            _errors.append("\n"); //$NON-NLS-1$
            return;
          }
          
          _m = progressMatcher.matcher(theLine);
          if (monitor!=null && _m.matches())
          {
            int _x = Integer.parseInt(_m.group(1)); // x MB
            int _x100 = Integer.parseInt(_m.group(2)); // von y MB
            int _bfWriter = Integer.parseInt(_m.group(3)); // Buffer % writer
            int _bfRam = Integer.parseInt(_m.group(4)); // Buffer % ram
            
            int _percent = (int)((_x100/100d)*_x);
            int _diff = _percent - lastWorked;
            if (_diff>0)
            {
              monitor.worked(_diff);
              lastWorked = _percent;
            }
            return;
          }
          _m = progressDoneMatcher.matcher(theLine);
          if (monitor!=null && _m.matches())
          {
            monitor.subTask(Messages.CdrdaoBurner_2);
            return;
          }
          _m = trackMatcher.matcher(theLine);
          if (monitor!=null && _m.matches())
          {
            int _track = Integer.parseInt(_m.group(1));
            monitor.subTask(String.format(Messages.CdrdaoBurner_3,_track));
            return;
          }
        }
      });
      if (_ret != 0)
      {
        Logger.getLogger(CdrdaoBurner.class.getName()).log(Level.SEVERE, Messages.CdrdaoBurner_4,
            String.format(Messages.CdrdaoBurner_5, _ret));
        if (_errors.length()>0)
        {
          throw new BurnerException(String.format(Messages.CdrdaoBurner_6, _errors.toString()));
        }
        else
        {
          throw new BurnerException(String.format(Messages.CdrdaoBurner_7, _ret));
        }
        
      }
    }
    catch (IOException _e)
    {
      Logger.getLogger(CdrdaoBurner.class.getName()).log(Level.SEVERE, Messages.CdrdaoBurner_8, _e);
      throw new BurnerException(_e);
    }
  }
  

  protected int execCdrdao(List<String> theArguments, ICommandListener theListener)
      throws IOException
  {
    List<String> _tmp = new ArrayList<String>();

    String _path = PreferenceManager.getInstance().getString(IPreferenceConstants.CDRDAO_PATH);
    if (_path.trim().length() != 0)
      _tmp.add(_path + File.separatorChar + "cdrdao"); //$NON-NLS-1$
    else
      _tmp.add("cdrdao"); //$NON-NLS-1$

    if (theArguments != null)
      _tmp.addAll(theArguments);

    ProcessBuilder _builder = new ProcessBuilder(_tmp);

    Logger.getLogger(CdrdaoBurner.class.getName()).log(Level.INFO,String.format("Cdrdao Command: %s",getCommand(_tmp))); //$NON-NLS-1$
    _builder.redirectErrorStream(true);
    Process _p = _builder.start();

    StringBuffer _sb = new StringBuffer();
    Scanner _s = new Scanner(new BufferedInputStream(_p.getInputStream()));
    while (_s.hasNext())
    {
      String _out = _s.nextLine();
      
      if (theListener!=null)
        theListener.newLine(_out);
      
      _sb.append(_out);
      _sb.append("\n"); //$NON-NLS-1$
    }
    _s.close();

    Logger.getLogger(CdrdaoBurner.class.getName()).log(Level.INFO, String.format("Cdrdao Output: %s",_sb.toString())); //$NON-NLS-1$
    
    try
    {
      _p.waitFor();
    }
    catch (InterruptedException _e)
    {
    }
    
    return _p.exitValue();
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
          if (s.endsWith("\\")) //$NON-NLS-1$
          {
            _cmdbuf.append("\\"); //$NON-NLS-1$
          }
          _cmdbuf.append('"');
        }
        else if (s.endsWith("\"")) //$NON-NLS-1$
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
  public String getName()
  {
    return Messages.CdrdaoBurner_9;
  }

  @Override
  public String getId()
  {
    return "cdrdao"; //$NON-NLS-1$
  }

  @Override
  public List<String> getPreFilters()
  {
    List<String> _ret  = new ArrayList<String>();
    _ret.add("tocfile"); //$NON-NLS-1$
    return _ret;
  }

  @Override
  public List<String> getPostFilters()
  {
    List<String> _ret  = new ArrayList<String>();
    _ret.add("tocfile"); //$NON-NLS-1$
    return _ret;
  }

  @Override
  public Map<String, String> getDevices()
  {
    final Map<String,String> _devs = new LinkedHashMap<String,String>();
    
    try
    {
      execCdrdao(Arrays.asList(new String[] { "scanbus" }), new ICommandListener() //$NON-NLS-1$
      {
        @Override
        public void newLine(String theLine)
        {
          Matcher _m = busMatcher.matcher(theLine);
          if (_m.matches())
          {
            String _dev = _m.group(1);
            String _name = _m.group(2) + " ("+_dev+")"; //$NON-NLS-1$ //$NON-NLS-2$
            
            if (_dev.trim().length()>0)
            {
              // check if its a writer
              if (getMaxSpeed(_dev)!=null && getMaxSpeed(_dev)>0)
                _devs.put(_name, _dev);
            }
          }
        }
      });
    }
    catch (IOException _e)
    {
    }
    
    return _devs;
  }

  @Override
  public boolean canBurn()
  {
    try
    {
      execCdrdao(Arrays.asList(new String[] { "scanbus" }), null); //$NON-NLS-1$
    }
    catch (IOException _e)
    {
      return false;
    }
    
    if (PreferenceManager.getInstance().getString(IPreferenceConstants.CDRDAO_DEVICE)==null || 
        PreferenceManager.getInstance().getString(IPreferenceConstants.CDRDAO_DEVICE).trim().length()==0)
    {
      return false;
    }
    
    return true;
  }

  @Override
  public void setMonitor(IProgressMonitor theMonitor)
  {
    monitor = theMonitor;
  }
  
  @Override
  public Map<String, String> getWriteSpeed()
  {
    Map<String,String> _ret = new HashMap<String,String>();
    _ret.put(Messages.CdrdaoBurner_12, ""); //$NON-NLS-2$
    return _ret;
  }
  
  protected Integer getMaxSpeed(String theDev)
  {
    if (maxSpeed==null)
    {
      try
      {
        execCdrdao(Arrays.asList(new String[] { "drive-info" }), new ICommandListener() //$NON-NLS-1$
        {
          @Override
          public void newLine(String theLine)
          {
            Matcher _m = speedMatcher.matcher(theLine);
            if (_m.matches() && _m.groupCount()>0)
            {
              String _speed = _m.group(1);
              
              if (_speed.trim().length()>0)
              {
                maxSpeed = Integer.parseInt(_speed);
              }
            }
          }
        });
      }
      catch (IOException _e)
      {
      }
    }
    
    return maxSpeed;
  }

}
