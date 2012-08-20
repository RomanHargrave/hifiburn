/*
 * HifiBurn 2012
 * 
 * ConvertManager.java
 */
package de.hifiburn.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.hifiburn.burner.BurnerException;
import de.hifiburn.burner.IBurner;
import de.hifiburn.filter.IFilter;


public class BurnerManager
{
  private static BurnerManager instance = null;
  
  protected List<IBurner> burner = new ArrayList<IBurner>();
  
  /**
   * 
   */
  private BurnerManager()
  {
    super();
  }
  
  public static BurnerManager getInstance()
  {
    if (instance==null)
      instance = new BurnerManager();
    
    return instance;
  }
  
  public void registerBurner (IBurner theBurner) throws BurnerException, InitializeException
  {
    if (theBurner!=null)
    {
      Logger.getLogger(BurnerManager.class.getName()).log(Level.INFO,
          String.format("Initialisiere Burner %s",theBurner.getName()));
      try
      {
        theBurner.initialize();
      }
      catch (InitializeException _e)
      {
        burner.add(theBurner);
        throw _e;
      }
      burner.add(theBurner);
    }
  }
  
  public IBurner getBurner()
  {
    String _current = PreferenceManager.getInstance().getString(IPreferenceConstants.BASIC_BURNER);
    
    for (IBurner _c : burner)
      if (_c.getId().equals(_current))
        return _c;
    
    return null;
  }
  

  public IBurner getBurner(String theId)
  {
    for (IBurner _c : burner)
      if (_c.getId().equals(theId))
        return _c;
    
    return null;
  }

  /**
   * @return Returns the converter.
   */
  public List<IBurner> getBurners()
  {
    return burner;
  }

  public List<IFilter> getPreFilters(IBurner theBurner)
  {
    return FilterManager.getInstance().getFilters(theBurner.getPreFilters());
  }
  
  public List<IFilter> getPostFilters(IBurner theBurner)
  {
    return FilterManager.getInstance().getFilters(theBurner.getPostFilters());
  }
}
