/*
 * HifiBurn 2012
 * 
 * TrackListContentProvider.java
 */
package de.hifiburn.ui.swt.provider;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class TrackListContentProvider implements IStructuredContentProvider
{
  @Override
  public void dispose()
  {
  }

  @Override
  public void inputChanged(Viewer theViewer, Object theOldInput, Object theNewInput)
  {
  }

  @Override
  public Object[] getElements(Object theInputElement)
  {
    if (theInputElement instanceof List)
    {
      return ((List)theInputElement).toArray();
    }

    return new Object[0];
  }

}
