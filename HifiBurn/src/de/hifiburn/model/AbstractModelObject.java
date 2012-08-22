/*
 * HifiBurn 2012
 * 
 * BaseObservable.java
 */
package de.hifiburn.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;

public class AbstractModelObject
{
  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
  {
    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
  {
    propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
  }

  protected void firePropertyChange(String propertyName, Object oldValue, Object newValue)
  {
    propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
  }
  
  public void reset() 
      throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchMethodException, 
              ClassNotFoundException, InvocationTargetException
  {
    for (Field _f : this.getClass().getDeclaredFields())
    {
      System.out.println("Reset field "+_f.getName()+" of object"+this.getClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$
      Object _obj = _f.get(this);
      
      if (_obj instanceof AbstractModelObject)
      {
        ((AbstractModelObject)_obj).reset();
        continue;
      }
      
      if (_obj instanceof Collection)
      {
        Iterator _it = ((Collection)_obj).iterator();
        while (_it.hasNext())
        {
          Object _listobject = _it.next();
          if (_listobject instanceof AbstractModelObject)
            ((AbstractModelObject)_listobject).reset();
        }
      }
      
      char[] _stringArray = _f.getName().toCharArray();
      _stringArray[0] = Character.toUpperCase(_stringArray[0]);
      String _name = new String(_stringArray);
      
      for (Method _m : this.getClass().getMethods())
      {
        if (_m.getName().startsWith("set") && _m.getName().endsWith(_name)) //$NON-NLS-1$
        {
          _m.invoke(this, new Object[]{null});
          break;
        }
      }
    }
  }
}