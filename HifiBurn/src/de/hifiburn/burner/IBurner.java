/*
 * HifiBurn 2012
 *
 * IBurner.java
 */
package de.hifiburn.burner;

import java.util.List;
import java.util.Map;

import de.hifiburn.logic.InitializeException;
import de.hifiburn.model.Disc;

public interface IBurner
{
  public void initialize() throws BurnerException, InitializeException;
  
  public void burn(Disc theDisc) 
      throws BurnerException;

  public String getName();
  
  public String getId();
  
  public List<String> getPreFilters(); 
  public List<String> getPostFilters();

  public Map<String, String> getDevices();
  
  public boolean canBurn();
  
}
