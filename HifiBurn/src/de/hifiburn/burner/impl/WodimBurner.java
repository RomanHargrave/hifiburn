/*
 * HifiBurn 2012
 *
 * WodimBurner.java
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
public class WodimBurner implements IBurner
{

  @Override
  public void initialize() throws BurnerException, InitializeException
  {

    try
    {
      execWodim(Arrays.asList(new String[] { "-version" }), null);
    }
    catch (IOException _e)
    {
      throw new InitializeException(
          "Wodim konnte nicht gefunden werden!\r\n\r\nBitte installieren Sie Wodim und passen den Pfad in den Einstellungen an.");
    }
    
    if (PreferenceManager.getInstance().getString(IPreferenceConstants.WODIM_DEVICE)==null || 
        PreferenceManager.getInstance().getString(IPreferenceConstants.WODIM_DEVICE).trim().length()==0)
    {
      throw new InitializeException(
          "Es wurde noch kein Brenner für Wodim ausgewählt. Bitte wählen Sie einen Brenner aus!");
    }
   
  }

  @Override
  public void burn(Disc theDisc)  throws BurnerException
  {
    List<String> _args = new ArrayList<String>();
    
    _args.add("-text");
    _args.add("-dao");
    _args.add("-pad");
    

    _args.add("driver=cdr_simul");
    
    _args.add(String.format("dev='%s'",PreferenceManager.getInstance().getString(IPreferenceConstants.WODIM_DEVICE)));
    
    _args.add(String.format("cuefile=\"%s\"", theDisc.getCuefile().getAbsoluteFile()));
    
    
    
    try
    {
      int _ret = execWodim(_args, null);
      if (_ret != 0)
      {
        Logger.getLogger(WodimBurner.class.getName()).log(Level.SEVERE, "Fehler während des Brennvorgangs",
            String.format("Wodium hat einen Fehler gemeldet (%d)", _ret));
        throw new BurnerException(String.format("Wodim hat einen Fehler gemeldet (%d)", _ret));
      }
    }
    catch (IOException _e)
    {
      Logger.getLogger(WodimBurner.class.getName()).log(Level.SEVERE, "Fehler während des Brennvorgangs", _e);
      throw new BurnerException(_e);
    }
  }
  

  protected int execWodim(List<String> theArguments, ICommandListener theListener)
      throws IOException
  {
    List<String> _tmp = new ArrayList<String>();

    String _path = PreferenceManager.getInstance().getString(IPreferenceConstants.WODIM_PATH);
    if (_path.trim().length() != 0)
      _tmp.add(_path + File.separatorChar + "wodim");
    else
      _tmp.add("wodim");

    if (theArguments != null)
      _tmp.addAll(theArguments);

    ProcessBuilder _builder = new ProcessBuilder(_tmp);

    Logger.getLogger(WodimBurner.class.getName()).log(Level.INFO,String.format("Wodim Command: %s",getCommand(_tmp)));
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

    Logger.getLogger(WodimBurner.class.getName()).log(Level.INFO, String.format("Wodim Output: %s",_sb.toString()));
    
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
    return "Wodim";
  }

  @Override
  public String getId()
  {
    return "wodim";
  }

  @Override
  public List<String> getPreFilters()
  {
    List<String> _ret  = new ArrayList<String>();
    _ret.add("cuefile");
    return _ret;
  }

  @Override
  public List<String> getPostFilters()
  {
    List<String> _ret  = new ArrayList<String>();
    _ret.add("cuefile");
    return _ret;
  }

  @Override
  public Map<String, String> getDevices()
  {
    final Map<String,String> _devs = new LinkedHashMap<String,String>();
    
    try
    {
      execWodim(Arrays.asList(new String[] { "-devices" }), new ICommandListener()
      {
        @Override
        public void newLine(String theLine)
        {
          Pattern _pat = Pattern.compile(".*dev='([^']*)'.*'([^']*)'\\s'([^']*)'");
          Matcher _m = _pat.matcher(theLine);
          if (_m.matches())
          {
            String _dev = _m.group(1);
            String _name = _m.group(2) + " " + _m.group(3) + " ("+_dev+")";
            
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
      execWodim(Arrays.asList(new String[] { "-version" }), null);
    }
    catch (IOException _e)
    {
      return false;
    }
    
    if (PreferenceManager.getInstance().getString(IPreferenceConstants.WODIM_DEVICE)==null || 
        PreferenceManager.getInstance().getString(IPreferenceConstants.WODIM_DEVICE).trim().length()==0)
    {
      return false;
    }
    
    return true;
  }

}
