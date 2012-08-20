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
      setErrorMessage(JFaceResources.getString("DirectoryFieldEditor.errorMessage"));//$NON-NLS-1$
      
      String _dir = getTextControl().getText().trim();
      if (_dir.length() == 0 && isEmptyStringAllowed()) 
      {
        return true;
      }
      
      File _file = new File(_dir);
      if (!_file.isDirectory())
        return false;
      
      String _executable = "cdrdao";
      if (System.getProperty("os.name").contains("win"))
        _executable = "cdrdao.exe";
      
      if (!new File(_file,_executable).exists())
      {
        setErrorMessage("Im angegebenen Verzeichnis konnte Cdrdao nicht gefunden werden.");
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
    return "Cdrdao";
  }

  @Override
  protected void createFieldEditors()
  {
    DirectoryFieldEditor _dir = new CdrdaoDirectoryFieldEditor(IPreferenceConstants.CDRDAO_PATH,"Pfad zu Cdrdao:",
        getFieldEditorParent());
    _dir.setValidateStrategy(DirectoryFieldEditor.VALIDATE_ON_KEY_STROKE);
    addField(_dir);
    
    String[][] _devs = getDevices();
    
    ComboFieldEditor _dev = new ComboFieldEditor(IPreferenceConstants.CDRDAO_DEVICE, "Brenner:", _devs, getFieldEditorParent());
    addField(_dev);
  }

  private String[][] getDevices()
  {
    IBurner _cdrdao = BurnerManager.getInstance().getBurner("cdrdao");
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
