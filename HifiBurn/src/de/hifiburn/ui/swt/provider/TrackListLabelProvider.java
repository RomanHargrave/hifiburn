/*
 * HifiForm 2012
 * 
 * TrackListLabelProvider.java
 */
package de.hifiburn.ui.swt.provider;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import de.hifiburn.logic.Util;
import de.hifiburn.model.Track;

public class TrackListLabelProvider extends LabelProvider implements ITableLabelProvider 
{
  @Override
  public String getColumnText(Object theElement, int theColumnIndex)
  {
    // each element comes from the ContentProvider.getElements(Object)
    if (!(theElement instanceof Track))
    {
      return ""; //$NON-NLS-1$
    }

    Track _track = (Track) theElement;
    switch (theColumnIndex)
    {
      case 0:
        return String.valueOf(_track.getNo());
      case 1:
        return _track.getInterpret();
      case 2:
        return _track.getTitle();
      case 3:
      {
        Integer _realduration = _track.getDuration();
        if (_track.getLength()!=null)
          _realduration -= _track.getLength();
        
        return Util.formatTime(_realduration);
      }
      default:
        break;
    }
    return ""; //$NON-NLS-1$
  }

  @Override
  public Image getColumnImage(Object theElement, int theColumnIndex)
  {
    return null;
  }
}
