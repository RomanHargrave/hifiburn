/*
 * HifiBurn 2012
 *
 * PreferenceDialog.java
 */
package de.hifiburn.ui.swt.preferences;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import de.hifiburn.i18n.Messages;
import de.hifiburn.logic.BurnerManager;
import de.hifiburn.logic.ConvertManager;

public class Preferences
{
  public static boolean showPreferenceDialog(Shell theShell)
  {
    PreferenceDialog _dlg = buildPreferencesDialog(theShell);
    
    // config store
    PreferenceStore _ps = de.hifiburn.logic.PreferenceManager.getInstance().getStore();
    
    try
    {
      _ps.load();
    }
    catch(IOException _e)
    {
      Logger.getLogger(PreferenceManager.class.getName()).log(Level.SEVERE, _e.getMessage());
    }
    _dlg.setPreferenceStore(_ps);
    
    // open
    if (_dlg.open()==Dialog.OK)
    {
      // store
      try
      {
        // Save the preferences
        _ps.save();
      }
      catch(IOException _e)
      {
        Logger.getLogger(PreferenceManager.class.getName()).log(Level.SEVERE, _e.getMessage());
      }
      
      return true;
    }
    else
    {
      return false;
    }
  }
  
  private static PreferenceDialog buildPreferencesDialog(Shell theShell)
  {
    PreferenceManager _mgr = new PreferenceManager();
    
    PreferencePage _p = new BasicPreferencePage();
    PreferenceNode _basicNode = new PreferenceNode("basic",_p); //$NON-NLS-1$
    _mgr.addToRoot(_basicNode);
    
    _p = new InformationPreferencePage(Messages.Preferences_0);
    _p.setTitle(Messages.Preferences_1);
    PreferenceNode _burnerNode = new PreferenceNode("burner",_p); //$NON-NLS-1$
    _mgr.addToRoot(_burnerNode);

    // WodimBurner
    if (BurnerManager.getInstance().getBurner("wodim")!=null) //$NON-NLS-1$
    {
      PreferenceNode _wodimNode = new PreferenceNode("wodim",new WodimPreferencePage()); //$NON-NLS-1$
      _mgr.addTo(_burnerNode.getId(), _wodimNode);
    }
    
    // CdrdaoBurner
    if (BurnerManager.getInstance().getBurner("cdrdao")!=null) //$NON-NLS-1$
    {
      PreferenceNode _cdrdaoNode = new PreferenceNode("cdrdao",new CdrdaoPreferencePage()); //$NON-NLS-1$
      _mgr.addTo(_burnerNode.getId(), _cdrdaoNode);
    }
    
    _p = new InformationPreferencePage(Messages.Preferences_2);
    _p.setTitle(Messages.Preferences_3);
    PreferenceNode _converterNode = new PreferenceNode("converter",_p); //$NON-NLS-1$
    _mgr.addToRoot(_converterNode);
    
    if (ConvertManager.getInstance().getConverter("ffmpeg")!=null) //$NON-NLS-1$
    {
      // FFMpegConverter
      PreferenceNode _ffmpegNode = new PreferenceNode("ffmpeg",new FFMpegPreferencePage()); //$NON-NLS-1$
      _mgr.addTo(_converterNode.getId(), _ffmpegNode);
    }
    
    
    
    PreferenceDialog _dlg = new PreferenceDialog(theShell, _mgr);
    return _dlg;
  }
}
