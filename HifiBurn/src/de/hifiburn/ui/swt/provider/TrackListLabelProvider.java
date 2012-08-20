/*
 * HifiForm 2012
 * 
 * TrackListLabelProvider.java
 */
package de.hifiburn.ui.swt.provider;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import de.hifiburn.model.Track;

public class TrackListLabelProvider extends LabelProvider implements ITableLabelProvider 
{
  @Override
  public String getColumnText(Object theElement, int theColumnIndex)
  {
    // each element comes from the ContentProvider.getElements(Object)
    if (!(theElement instanceof Track))
    {
      return "";
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
        int _hours = _track.getDuration() / 3600;
        int _minutes = (_track.getDuration() / 60) % 60;
        int _seconds = _track.getDuration() % 60;
        return String.format("%02d:%02d:%02d",_hours,_minutes,_seconds);
      }
      default:
        break;
    }
    return "";
  }

  @Override
  public Image getColumnImage(Object theElement, int theColumnIndex)
  {
    return null;
  }
}
