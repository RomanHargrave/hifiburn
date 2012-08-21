/*
 * HifiBurn 2012
 *
 * FFMpegPreferencePage.java
 */
package de.hifiburn.ui.swt.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class InformationPreferencePage extends PreferencePage
{
  String text = ""; //$NON-NLS-1$

  /**
   * 
   */
  public InformationPreferencePage(String theText)
  {
    super();
    text = theText;
  }

  /**
   * Creates the controls for this page
   */
  protected Control createContents(Composite parent)
  {
    Composite _composite = new Composite(parent, SWT.NONE);
    _composite.setLayout(new FillLayout());

    // path to ffmpeg executable
    Label _l = new Label(_composite, SWT.LEFT | SWT.BOLD);
    _l.setText(text);
    
    return _composite;
  }
}
