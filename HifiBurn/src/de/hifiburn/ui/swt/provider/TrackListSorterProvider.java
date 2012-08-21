/*
 * TrackListSorterProvider.java
 * Copyright (c) 1999-2012 by Community4you GmbH
 */
package de.hifiburn.ui.swt.provider;

import org.eclipse.jface.viewers.ViewerSorter;

import de.hifiburn.model.Track;

public class TrackListSorterProvider extends ViewerSorter
{

//  /**
//   * {@inheritDoc}
//   */
//  @Override
//  public int compare(Viewer theViewer, Object theE1, Object theE2)
//  {
//    Track _t1 = ((Track)theE1);
//    Track _t2 = ((Track)theE2);
//    
//    return _t2.getNo()-_t1.getNo();
//  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int category(Object theElement)
  {
    Track _t1 = ((Track)theElement);
    if (_t1!=null)
      return _t1.getNo();
    
    return 0;
  }

}
