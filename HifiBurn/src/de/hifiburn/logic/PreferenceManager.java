/*
 * HifiBurn 2012
 *
 * Preferences.java
 */
package de.hifiburn.logic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.preference.PreferenceStore;


public class PreferenceManager extends Object
{
  public static final String PREFERENCE_FILE =  "hifiburn.properties";

  private static PreferenceManager instance = null;
  private PreferenceStore store = null;
  
  public static Map<String,Object> DEFAULTS = new HashMap<String,Object>();
  
  static 
  {
    DEFAULTS.put(IPreferenceConstants.BASIC_AUDIOCONVERTER, "ffmpeg");
    DEFAULTS.put(IPreferenceConstants.BASIC_BURNER, "cdrdao");
    DEFAULTS.put(IPreferenceConstants.FFMPEG_PATH, "");
    DEFAULTS.put(IPreferenceConstants.WODIM_PATH, "");
    DEFAULTS.put(IPreferenceConstants.CDRDAO_PATH, "");
  }
  
  /**
   * 
   */
  private PreferenceManager()
  {
    super();
    store = new PreferenceStore(PREFERENCE_FILE); 
    setDefaults();
    try
    {
      store.load();
    }
    catch (IOException _e)
    {
      Logger.getLogger(ProjectManager.class.getName()).log(Level.SEVERE, 
          String.format("Einstellungen konnten nicht gelesen werden!"));
    }
  }
  
  public static PreferenceManager getInstance()
  {
    if (instance==null)
      instance = new PreferenceManager();
    
    return instance;
  }
  
  protected void setDefaults()
  {
    for (Map.Entry<String, Object> _entry : DEFAULTS.entrySet())
    {
      Object _v = _entry.getValue();
      if (_v instanceof String)
        store.setDefault(_entry.getKey(), (String)_v);
      if (_v instanceof Integer)
        store.setDefault(_entry.getKey(), (Integer)_v);
      if (_v instanceof Double)
        store.setDefault(_entry.getKey(), (Double)_v);
    }
  }
  
  public String getString(String theProperty)
  {
    return store.getString(theProperty);
  }
  
  public Integer getInt(String theProperty)
  {
    return store.getInt(theProperty);
  }
  
  public Double getDouble(String theProperty)
  {
    return store.getDouble(theProperty);
  }
  
  /**
   * @return Returns the store.
   */
  public PreferenceStore getStore()
  {
    return store;
  }
}


