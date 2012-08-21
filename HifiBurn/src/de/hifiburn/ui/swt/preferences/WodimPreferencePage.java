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

public class WodimPreferencePage extends FieldEditorPreferencePage
{

  protected class WodimDirectoryFieldEditor extends DirectoryFieldEditor
  {

    /**
     * 
     */
    public WodimDirectoryFieldEditor()
    {
      super();
    }

    /**
     * @param theName
     * @param theLabelText
     * @param theParent
     */
    public WodimDirectoryFieldEditor(String theName, String theLabelText, Composite theParent)
    {
      super(theName, theLabelText, theParent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doCheckState()
    {
      setErrorMessage("Value must be an existing directory");//$NON-NLS-1$
      
      String _dir = getTextControl().getText().trim();
      if (_dir.length() == 0 && isEmptyStringAllowed()) 
      {
        return true;
      }
      
      File _file = new File(_dir);
      if (!_file.isDirectory())
        return false;
      
      String _executable = "wodim"; //$NON-NLS-1$
      if (System.getProperty("os.name").contains("win")) //$NON-NLS-1$ //$NON-NLS-2$
        _executable = "wodim.exe"; //$NON-NLS-1$
      
      if (!new File(_file,_executable).exists())
      {
        setErrorMessage(Messages.WodimPreferencePage_0);
        return false;
      }
      
      return true;
    }
    
    
  }
  
  /**
   * 
   */
  public WodimPreferencePage()
  {
    super(FieldEditorPreferencePage.GRID);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getTitle()
  {
    return Messages.WodimPreferencePage_1;
  }

//  //Text fields for user to enter preferences
//  private Text txtFFMpegPath;
//  
//  /**
//   * Creates the controls for this page
//   */
//  protected Control createContents(Composite parent)
//  {
//    // Get the preference store
//    IPreferenceStore preferenceStore = getPreferenceStore();
//    
//    Composite _composite = new Composite(parent, SWT.NONE);
//    _composite.setLayout(new GridLayout(2, false));
//
//    // path to ffmpeg executable
//    new Label(_composite, SWT.LEFT).setText("Pfad zu ffmpeg.exe");
//    txtFFMpegPath = new Text(_composite, SWT.BORDER);
//    txtFFMpegPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//    txtFFMpegPath.setText(preferenceStore.getString(de.hifiburn.logic.PreferenceManager.FFMPEG_PATH));
//
//    return _composite;
//  }

//  /**
//   * Called when user clicks Restore Defaults
//   */
//  protected void performDefaults()
//  {
//    // Get the preference store
//    IPreferenceStore preferenceStore = getPreferenceStore();
//
//    // Reset the fields to the defaults
//    txtFFMpegPath.setText(preferenceStore.getDefaultString(de.hifiburn.logic.PreferenceManager.FFMPEG_PATH));
//  }
//
//  /**
//   * Called when user clicks Apply or OK
//   * 
//   * @return boolean
//   */
//  public boolean performOk()
//  {
//    // Get the preference store
//    IPreferenceStore preferenceStore = getPreferenceStore();
//
//    // Set the values from the fields
//    if (txtFFMpegPath != null)
//      preferenceStore.setValue(de.hifiburn.logic.PreferenceManager.FFMPEG_PATH, txtFFMpegPath.getText());
//
//    // Return true to allow dialog to close
//    return true;
//  }

  @Override
  protected void createFieldEditors()
  {
    DirectoryFieldEditor _dir = new WodimDirectoryFieldEditor(IPreferenceConstants.WODIM_PATH,Messages.WodimPreferencePage_2,
        getFieldEditorParent());
    _dir.setValidateStrategy(DirectoryFieldEditor.VALIDATE_ON_KEY_STROKE);
    addField(_dir);
    
    String[][] _devs = getDevices();
    
    ComboFieldEditor _dev = new ComboFieldEditor(IPreferenceConstants.WODIM_DEVICE, Messages.WodimPreferencePage_3, _devs, getFieldEditorParent());
    addField(_dev);
  }

  private String[][] getDevices()
  {
    IBurner _wodim = BurnerManager.getInstance().getBurner("wodim"); //$NON-NLS-1$
    if (_wodim==null)
      return null;
    
    Map<String,String> _devs = _wodim.getDevices();
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
