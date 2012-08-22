/*
 * HifiBurn 2012
 * 
 * ConvertManager.java
 */
package de.hifiburn.logic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.hifiburn.converter.ConvertException;
import de.hifiburn.converter.Format;
import de.hifiburn.converter.IConverter;
import de.hifiburn.i18n.Messages;


public class ConvertManager
{
  private static ConvertManager instance = null;
  
  protected List<IConverter> converter = new ArrayList<IConverter>();
  
  /**
   * 
   */
  private ConvertManager()
  {
    super();
  }
  
  public static ConvertManager getInstance()
  {
    if (instance==null)
      instance = new ConvertManager();
    
    return instance;
  }
  
  public void registerConverter(IConverter theConverter) throws InitializeException, ConvertException
  {
    if (theConverter!=null)
    {
      Logger.getLogger(ConvertManager.class.getName()).log(Level.INFO,
          String.format(Messages.ConvertManager_0,theConverter.getName()));
      try
      {
        theConverter.initialize();
      }
      catch (InitializeException _e)
      {
        converter.add(theConverter);
        throw _e;
      }
      
      converter.add(theConverter);
    }
  }
  
  public IConverter getConverter(File theInput, Format theOutputFormat)
  {
    String _current = PreferenceManager.getInstance().getString(IPreferenceConstants.BASIC_AUDIOCONVERTER);
    
    IConverter _conv = null;
    for (IConverter _c : converter)
      if (_c.getId().equals(_current))
      {
        _conv = _c;
        break;
      }
    
    // special mode for initialization
    if (theInput==null && theOutputFormat==null)
      return _conv;
    
    if (_conv!=null && _conv.supportFormat(theInput,theOutputFormat))
      return _conv;
    
    return null;
  }
  
  public IConverter getConverter(String theId)
  {
    for (IConverter _c : converter)
      if (_c.getId().equals(theId))
        return _c;
    
    return null;
  }

  /**
   * @return Returns the converter.
   */
  public List<IConverter> getConverter()
  {
    return converter;
  }
}
