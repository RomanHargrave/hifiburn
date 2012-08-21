/*
 * HifiBurn 2012
 *
 * FFMpegPreferencePage.java
 */
package de.hifiburn.ui.swt.preferences;

import java.io.File;
import java.util.Map;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.resource.JFaceResources;
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
    DirectoryFieldEditor _dir = new CdrdaoDirectoryFieldEditor(IPreferenceConstants.CDRDAO_PATH,Messages.CdrdaoPreferencePage_2,
        getFieldEditorParent());
    _dir.setValidateStrategy(DirectoryFieldEditor.VALIDATE_ON_KEY_STROKE);
    addField(_dir);
    
    String[][] _devs = getDevices();
    
    ComboFieldEditor _dev = new ComboFieldEditor(IPreferenceConstants.CDRDAO_DEVICE, Messages.CdrdaoPreferencePage_3, _devs, getFieldEditorParent());
    addField(_dev);
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
}
