/*
 * HifiBurn 2012
 *
 * CdrdaoBurner.java
 */
package de.hifiburn.burner.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hifiburn.burner.BurnerException;
import de.hifiburn.burner.IBurner;
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

  @Override
  public void initialize() throws BurnerException, InitializeException
  {
    try
    {
      execCdrdao(Arrays.asList(new String[] { "scanbus" }), null);
    }
    catch (IOException _e)
    {
      throw new InitializeException(
          "Cdrdao konnte nicht gefunden werden!\r\n\r\nBitte installieren Sie Cdrdao und passen den Pfad in den Einstellungen an.");
    }
    
    if (PreferenceManager.getInstance().getString(IPreferenceConstants.CDRDAO_DEVICE)==null || 
        PreferenceManager.getInstance().getString(IPreferenceConstants.CDRDAO_DEVICE).trim().length()==0)
    {
      throw new InitializeException(
          "Es wurde noch kein Brenner für Cdrdao ausgewählt. Bitte wählen Sie einen Brenner aus!");
    }
   
  }

  @Override
  public void burn(Disc theDisc)  throws BurnerException
  {
    List<String> _args = new ArrayList<String>();
    
    final StringBuilder _errors = new StringBuilder();
    
    _args.add("write");
    _args.add("--overburn");
    _args.add("--reload");
    
    _args.add("--device");
    _args.add(PreferenceManager.getInstance().getString(IPreferenceConstants.CDRDAO_DEVICE));
    
    _args.add(theDisc.getTocfile().getAbsoluteFile().toString());
    
    try
    {
      int _ret = execCdrdao(_args, new ICommandListener()
      {
        @Override
        public void newLine(String theLine)
        {
          if (theLine.startsWith("ERROR: "))
          {
            _errors.append(theLine.substring("ERROR: ".length()));
            _errors.append("\n");
          }
        }
      });
      if (_ret != 0)
      {
        Logger.getLogger(CdrdaoBurner.class.getName()).log(Level.SEVERE, "Fehler während des Brennvorgangs",
            String.format("Cdrdao hat einen Fehler gemeldet (%d)", _ret));
        if (_errors.length()>0)
        {
          throw new BurnerException(String.format("Cdrdao hat einen Fehler gemeldet:\n\n%s", _errors.toString()));
        }
        else
        {
          throw new BurnerException(String.format("Cdrdao hat einen Fehler gemeldet (%d)", _ret));
        }
        
      }
    }
    catch (IOException _e)
    {
      Logger.getLogger(CdrdaoBurner.class.getName()).log(Level.SEVERE, "Fehler während des Brennvorgangs", _e);
      throw new BurnerException(_e);
    }
  }
  

  protected int execCdrdao(List<String> theArguments, ICommandListener theListener)
      throws IOException
  {
    List<String> _tmp = new ArrayList<String>();

    String _path = PreferenceManager.getInstance().getString(IPreferenceConstants.CDRDAO_PATH);
    if (_path.trim().length() != 0)
      _tmp.add(_path + File.separatorChar + "cdrdao");
    else
      _tmp.add("cdrdao");

    if (theArguments != null)
      _tmp.addAll(theArguments);

    ProcessBuilder _builder = new ProcessBuilder(_tmp);

    Logger.getLogger(CdrdaoBurner.class.getName()).log(Level.INFO,String.format("Cdrdao Command: %s",getCommand(_tmp)));
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
      _sb.append("\n");
    }
    _s.close();

    Logger.getLogger(CdrdaoBurner.class.getName()).log(Level.INFO, String.format("Cdrdao Output: %s",_sb.toString()));
    
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
          if (s.endsWith("\\"))
          {
            _cmdbuf.append("\\");
          }
          _cmdbuf.append('"');
        }
        else if (s.endsWith("\""))
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
    return "Cdrdao";
  }

  @Override
  public String getId()
  {
    return "cdrdao";
  }

  @Override
  public List<String> getPreFilters()
  {
    List<String> _ret  = new ArrayList<String>();
    _ret.add("tocfile");
    return _ret;
  }

  @Override
  public List<String> getPostFilters()
  {
    List<String> _ret  = new ArrayList<String>();
    _ret.add("tocfile");
    return _ret;
  }

  @Override
  public Map<String, String> getDevices()
  {
    final Map<String,String> _devs = new LinkedHashMap<String,String>();
    
    try
    {
      execCdrdao(Arrays.asList(new String[] { "scanbus" }), new ICommandListener()
      {
        @Override
        public void newLine(String theLine)
        {
          Pattern _pat = Pattern.compile("([^\\s]*)\\s+:\\s+(.*)");
          Matcher _m = _pat.matcher(theLine);
          if (_m.matches())
          {
            String _dev = _m.group(1);
            String _name = _m.group(2) + " ("+_dev+")";
            
            if (_dev.trim().length()>0)
              _devs.put(_name, _dev);
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
      execCdrdao(Arrays.asList(new String[] { "scanbus" }), null);
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

}
