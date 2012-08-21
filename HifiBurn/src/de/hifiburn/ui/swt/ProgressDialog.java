/*
 * HifiBurn 2012
 *
 * ProgressDialog.java
 */
package de.hifiburn.ui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import de.hifiburn.i18n.Messages;

import swing2swt.layout.BorderLayout;

public class ProgressDialog extends Dialog
{

  protected Object result;

  protected Shell shell;
  private Label lblCurrentAction;

  /**
   * Create the dialog.
   * @param parent
   * @param style
   */
  public ProgressDialog(Shell parent, int style)
  {
    super(parent, style);
    setText(Messages.ProgressDialog_0);
  }

  /**
   * Open the dialog.
   * @return the result
   */
  public Object open()
  {
    createContents();
    shell.open();
    shell.layout();
    Display display = getParent().getDisplay();
    while (!shell.isDisposed())
    {
      if (!display.readAndDispatch())
      {
        display.sleep();
      }
    }
    return result;
  }

  /**
   * Create contents of the dialog.
   */
  private void createContents()
  {
    shell = new Shell(getParent(), getStyle());
    shell.setSize(450, 300);
    shell.setText(getText());
    shell.setLayout(new BorderLayout(0, 0));
    
    Composite composite = new Composite(shell, SWT.NONE);
    composite.setLayoutData(BorderLayout.NORTH);
    composite.setLayout(new GridLayout(2, false));
    
    Label lblAktuelleAktion = new Label(composite, SWT.NONE);
    lblAktuelleAktion.setText(Messages.ProgressDialog_1);
    
    lblCurrentAction = new Label(composite, SWT.NONE);
    lblCurrentAction.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
    
    Label lblFortschritt = new Label(composite, SWT.NONE);
    lblFortschritt.setText(Messages.ProgressDialog_2);
    
    ProgressBar progressBar = new ProgressBar(composite, SWT.NONE);
    progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Group grpActions = new Group(shell, SWT.NONE);
    grpActions.setText(Messages.ProgressDialog_3);
    grpActions.setLayoutData(BorderLayout.CENTER);
    grpActions.setLayout(new FormLayout());

  }
}
