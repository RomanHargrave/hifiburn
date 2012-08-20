/*
 * Tools.java
 * Copyright (c) 1999-2012 by Community4you GmbH
 */
package de.hifiburn.ui.swt;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class SwtTools
{
  public static void setEnabledRecursive(final Composite composite, final boolean enabled)
  {
    if (composite == null)
      return;

    Control[] children = composite.getChildren();

    for (int i = 0; i < children.length; i++)
    {
      if (children[i] instanceof Composite)
      {
        setEnabledRecursive((Composite) children[i], enabled);
      }
      else
      {
        children[i].setEnabled(enabled);
      }
    }

    //composite.setEnabled(enabled);
  }
}
