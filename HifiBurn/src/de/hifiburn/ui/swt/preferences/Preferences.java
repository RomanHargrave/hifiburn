/*
 * HifiBurn 2012
 *
 * PreferenceDialog.java
 */
package de.hifiburn.ui.swt.preferences;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import de.hifiburn.logic.BurnerManager;
import de.hifiburn.logic.ConvertManager;

public class Preferences
{
  public static void showPreferenceDialog(Shell theShell)
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
    _dlg.open();
    
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
  }
  
  private static PreferenceDialog buildPreferencesDialog(Shell theShell)
  {
    PreferenceManager _mgr = new PreferenceManager();
    
    PreferencePage _p = new BasicPreferencePage();
    PreferenceNode _basicNode = new PreferenceNode("basic",_p);
    _mgr.addToRoot(_basicNode);
    
    _p = new InformationPreferencePage("Hier können Sie Optionen für die Brenn-Backends festlegen.");
    _p.setTitle("Brenn-Backends");
    PreferenceNode _burnerNode = new PreferenceNode("burner",_p);
    _mgr.addToRoot(_burnerNode);

    // WodimBurner
    if (BurnerManager.getInstance().getBurner("wodim")!=null)
    {
      PreferenceNode _wodimNode = new PreferenceNode("wodim",new WodimPreferencePage());
      _mgr.addTo(_burnerNode.getId(), _wodimNode);
    }
    
    // CdrdaoBurner
    if (BurnerManager.getInstance().getBurner("cdrdao")!=null)
    {
      PreferenceNode _cdrdaoNode = new PreferenceNode("cdrdao",new CdrdaoPreferencePage());
      _mgr.addTo(_burnerNode.getId(), _cdrdaoNode);
    }
    
    _p = new InformationPreferencePage("Hier können Sie Optionen für die Konvertierung der Tracks festlegen.");
    _p.setTitle("Audiokonverter");
    PreferenceNode _converterNode = new PreferenceNode("converter",_p);
    _mgr.addToRoot(_converterNode);
    
    if (ConvertManager.getInstance().getConverter("ffmpeg")!=null)
    {
      // FFMpegConverter
      PreferenceNode _ffmpegNode = new PreferenceNode("ffmpeg",new FFMpegPreferencePage());
      _mgr.addTo(_converterNode.getId(), _ffmpegNode);
    }
    
    
    
    PreferenceDialog _dlg = new PreferenceDialog(theShell, _mgr);
    return _dlg;
  }
}
