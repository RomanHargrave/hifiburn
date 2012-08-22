/*
 * TextWidgetLogHandler.java
 * Copyright (c) 1999-2012 by Community4you GmbH
 */
package de.hifiburn.ui.swt;

import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

public class TextWidgetLogHandler extends StreamHandler
{
  protected Text widget = null;
  
  protected StringBuffer _buf = new StringBuffer();
  
  /**
   * 
   */
  public TextWidgetLogHandler()
  {
    super();
  }

  /**
   * @param theWidget The widget to set.
   */
  public void setWidget(Text theWidget)
  {
    widget = theWidget;
    if (widget!=null)
    {
      widget.setSelection(widget.getText().length());
      widget.insert(_buf.toString());
      _buf.setLength(0);
    }
  }

  @Override
  public void publish(LogRecord theRecord)
  {
    final String _msg = getFormatter().format(theRecord);

    if (widget!=null)
    {
      Display.getDefault().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          widget.setSelection(widget.getText().length());
          widget.insert(_msg);
        }
      });
    }
    else
    {
      _buf.append(_msg);
//      if (System.getProperty("os.name").contains("win"))
//        _buf.append("\)
    }
  }

  @Override
  public void flush()
  {
  }

  @Override
  public void close()
      throws SecurityException
  {
  }

}
