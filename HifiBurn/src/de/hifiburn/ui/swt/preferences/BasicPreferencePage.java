/*
 * HifiBurn 2012
 *
 * FFMpegPreferencePage.java
 */
package de.hifiburn.ui.swt.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import de.hifiburn.burner.IBurner;
import de.hifiburn.converter.IConverter;
import de.hifiburn.i18n.Messages;
import de.hifiburn.logic.BurnerManager;
import de.hifiburn.logic.ConvertManager;
import de.hifiburn.logic.IPreferenceConstants;

public class BasicPreferencePage extends PreferencePage
{

  /**
   * {@inheritDoc}
   */
  @Override
  public String getTitle()
  {
    return Messages.BasicPreferencePage_0;
  }

  //Text fields for user to enter preferences
  private Combo comboConverter;
  private Combo comboBurner;
  
  /**
   * Creates the controls for this page
   */
  protected Control createContents(Composite parent)
  {
    Composite _composite = new Composite(parent, SWT.NONE);
    _composite.setLayout(new GridLayout(2, false));

    
    // converter combo
    new Label(_composite, SWT.LEFT).setText(Messages.BasicPreferencePage_1);
    comboConverter = new Combo(_composite, SWT.BORDER | SWT.READ_ONLY);
    comboConverter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    String _current = getPreferenceStore().getString(IPreferenceConstants.BASIC_AUDIOCONVERTER);
    int _i = 0;
    Integer _select = null;
    for (IConverter _conv : ConvertManager.getInstance().getConverter())
    {
      comboConverter.add(_conv.getName());
      comboConverter.setData(_conv.getName(),_conv.getId());
      
      if (_conv.getId().equals(_current))
        _select = _i;
      
      _i++;
    }
    
    if (_select!=null)
      comboConverter.select(_select);
    
    // Burner combo
    new Label(_composite, SWT.LEFT).setText(Messages.BasicPreferencePage_2);
    comboBurner = new Combo(_composite, SWT.BORDER | SWT.READ_ONLY);
    comboBurner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    _current = getPreferenceStore().getString(IPreferenceConstants.BASIC_BURNER);
    _i = 0;
    _select = null;
    for (IBurner _burner : BurnerManager.getInstance().getBurners())
    {
      comboBurner.add(_burner.getName());
      comboBurner.setData(_burner.getName(),_burner.getId());
      
      if (_burner.getId().equals(_current))
        _select = _i;
      
      _i++;
    }
    
    if (_select!=null)
      comboBurner.select(_select);
    
    return _composite;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid()
  {
    if (super.isValid()==false)
      return false;
    
    if (comboBurner.getSelectionIndex()<0)
      return false;
    
    if (comboConverter.getSelectionIndex()<0)
      return false;
    
    return true;
  }

  /**
   * Called when user clicks Restore Defaults
   */
  protected void performDefaults()
  {
    // Get the preference store
    IPreferenceStore preferenceStore = getPreferenceStore();

    // converter combo
    int _i = 0;
    for (String _item : comboConverter.getItems())
    {
      if (comboConverter.getData(_item).equals(preferenceStore.getDefaultString(IPreferenceConstants.BASIC_AUDIOCONVERTER)))
        break;
      
      _i++;
    }

    comboConverter.select(_i);
    
    // burner comboc
    _i = 0;
    for (String _item : comboBurner.getItems())
    {
      if (comboBurner.getData(_item).equals(preferenceStore.getDefaultString(IPreferenceConstants.BASIC_BURNER)))
        break;
      
      _i++;
    }

    comboBurner.select(_i);
  }

  /**
   * Called when user clicks Apply or OK
   * 
   * @return boolean
   */
  public boolean performOk()
  {
    // Get the preference store
    IPreferenceStore preferenceStore = getPreferenceStore();

    // Set the values from the fields
    if (comboConverter != null)
    {
      if (comboConverter.getData(comboConverter.getText())==null)
      {
        preferenceStore.setValue(IPreferenceConstants.BASIC_AUDIOCONVERTER, 
           ConvertManager.getInstance().getConverter().get(0).getId());
      }
      else
        preferenceStore.setValue(IPreferenceConstants.BASIC_AUDIOCONVERTER, 
            (String)comboConverter.getData(comboConverter.getText()));
    }
    
    if (comboBurner != null)
    {
      if (comboBurner.getData(comboBurner.getText())==null)
      {
        preferenceStore.setValue(IPreferenceConstants.BASIC_BURNER, 
            BurnerManager.getInstance().getBurners().get(0).getId());
      }
      else
        preferenceStore.setValue(IPreferenceConstants.BASIC_BURNER, 
          (String)comboBurner.getData(comboBurner.getText()));
    }

    // Return true to allow dialog to close
    return true;
  }
}
