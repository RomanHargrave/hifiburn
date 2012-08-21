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

import de.hifiburn.converter.ConvertException;
import de.hifiburn.filter.IFilter;
import de.hifiburn.i18n.Messages;


public class FilterManager
{
  private static FilterManager instance = null;
  
  protected List<IFilter> filter = new ArrayList<IFilter>();
  
  /**
   * 
   */
  private FilterManager()
  {
    super();
  }
  
  public static FilterManager getInstance()
  {
    if (instance==null)
      instance = new FilterManager();
    
    return instance;
  }
  
  public void registerFilter(IFilter theFilter) throws ConvertException
  {
    if (theFilter!=null)
    {
      Logger.getLogger(FilterManager.class.getName()).log(Level.INFO,
          String.format(Messages.FilterManager_0,theFilter.getName()));
      filter.add(theFilter);
    }
  }
  
  public IFilter getFilter(String theId)
  {
    for (IFilter _c : filter)
      if (_c.getId().equals(theId))
      {
        return _c;
      }
    
    return null;
  }
  
  public List<IFilter> getFilters(List<String> theIds)
  {
    List<IFilter> _ret = new ArrayList<IFilter>();
    IFilter _tmp = null;
    for (String _id : theIds)
    {
      _tmp = getFilter(_id);
      if (_tmp!=null)
        _ret.add(_tmp);
    }
      
    return _ret;
  }

  /**
   * @return Returns the converter.
   */
  public List<IFilter> getFilters()
  {
    return filter;
  }
}
