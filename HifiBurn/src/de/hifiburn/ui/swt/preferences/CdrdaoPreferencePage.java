/*
 * HifiBurn 2012
 *
 * FFMpegPreferencePage.java
 */
package de.hifiburn.ui.swt.preferences;

import java.io.File;
import java.util.Map;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.widgets.Composite;

import de.hifiburn.burner.IBurner;
import de.hifiburn.i18n.Messages;
import de.hifiburn.logic.BurnerManager;
import de.hifiburn.logic.IPreferenceConstants;

public class CdrdaoPreferencePage extends FieldEditorPreferencePage
{
  protected class CdrdaoDirectoryFieldEditor extends DirectoryFieldEditor
  {

    /**
     * 
     */
    public CdrdaoDirectoryFieldEditor()
    {
      super();
    }

    /**
     * @param theName
     * @param theLabelText
     * @param theParent
     */
    public CdrdaoDirectoryFieldEditor(String theName, String theLabelText, Composite theParent)
    {
      super(theName, theLabelText, theParent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doCheckState()
    {
      setErrorMessage("Value must be an existing directory"); //$NON-NLS-1$
      
      String _dir = getTextControl().getText().trim();
      if (_dir.length() == 0 && isEmptyStringAllowed()) 
      {
        return true;
      }
      
      File _file = new File(_dir);
      if (!_file.isDirectory())
        return false;
      
      String _executable = "cdrdao"; //$NON-NLS-1$
      if (System.getProperty("os.name").contains("win")) //$NON-NLS-1$  //$NON-NLS-2$
        _executable = "cdrdao.exe";  //$NON-NLS-1$
      
      if (!new File(_file,_executable).exists())
      {
        setErrorMessage(Messages.CdrdaoPreferencePage_0);
        return false;
      }
      
      return true;
    }
    
    
  }
  
  /**
   * 
   */
  public CdrdaoPreferencePage()
  {
    super(FieldEditorPreferencePage.GRID);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getTitle()
  {
    return Messages.CdrdaoPreferencePage_1;
  }

  @Override
  protected void createFieldEditors()
  {
    // path
    DirectoryFieldEditor _dir = new CdrdaoDirectoryFieldEditor(IPreferenceConstants.CDRDAO_PATH,Messages.CdrdaoPreferencePage_2,
        getFieldEditorParent());
    _dir.setValidateStrategy(DirectoryFieldEditor.VALIDATE_ON_KEY_STROKE);
    addField(_dir);
    
    // device selection
    String[][] _devs = getDevices();
    ComboFieldEditor _dev = new ComboFieldEditor(IPreferenceConstants.CDRDAO_DEVICE, Messages.CdrdaoPreferencePage_3, _devs, getFieldEditorParent());
    addField(_dev);
    
    // writing speed
    String[][] _speeds = getWriteSpeeds();
    ComboFieldEditor _speed = new ComboFieldEditor(IPreferenceConstants.CDRDAO_SPEED, "Brenngeschwindigkeit", _speeds, getFieldEditorParent());
    addField(_speed);
    
    // simulation
    BooleanFieldEditor _bo = new BooleanFieldEditor(IPreferenceConstants.CDRDAO_SIMULATION, "Do not burn, just Simulate!", getFieldEditorParent());
    addField(_bo);
    
    if (_devs.length==0)
    {
      _dev.setEnabled(false, getFieldEditorParent());
      _speed.setEnabled(false, getFieldEditorParent());
      _bo.setEnabled(false, getFieldEditorParent());
    }
  }

  private String[][] getDevices()
  {
    IBurner _cdrdao = BurnerManager.getInstance().getBurner("cdrdao"); //$NON-NLS-1$
    if (_cdrdao==null)
      return null;
    
    Map<String,String> _devs = _cdrdao.getDevices();
    if (_devs!=null)
    {
      String[][] _ret = new String[_devs.size()][2];
      int count = 0;
      for(Map.Entry<String,String> entry : _devs.entrySet()){
          _ret[count][0] = entry.getKey();
          _ret[count][1] = entry.getValue();
          count++;
      }
      
      return _ret;
    };
    
    return new String[0][2];
  }
  
  private String[][] getWriteSpeeds()
  {
    IBurner _cdrdao = BurnerManager.getInstance().getBurner("cdrdao"); //$NON-NLS-1$
    if (_cdrdao==null)
      return null;
    
    Map<String,String> _speeds = _cdrdao.getWriteSpeed();
    if (_speeds!=null)
    {
      String[][] _ret = new String[_speeds.size()][2];
      int count = 0;
      for(Map.Entry<String,String> entry : _speeds.entrySet()){
          _ret[count][0] = entry.getKey();
          _ret[count][1] = entry.getValue();
          count++;
      }
      
      return _ret;
    };
    
    return new String[0][2];
  }
}
