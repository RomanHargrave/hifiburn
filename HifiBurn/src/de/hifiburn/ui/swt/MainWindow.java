/*
 * HifiBurn 2012
 * 
 * MainWindow.java
 */
package de.hifiburn.ui.swt;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.SWTResourceManager;

import swing2swt.layout.BorderLayout;
import de.hifiburn.burner.BurnerException;
import de.hifiburn.burner.IBurner;
import de.hifiburn.filter.FilterException;
import de.hifiburn.filter.IFilter;
import de.hifiburn.logic.AudioFileManager;
import de.hifiburn.logic.BurnerManager;
import de.hifiburn.logic.InitializeException;
import de.hifiburn.logic.ProjectManager;
import de.hifiburn.model.Disc;
import de.hifiburn.model.Project;
import de.hifiburn.model.Track;
import de.hifiburn.ui.swt.preferences.Preferences;
import de.hifiburn.ui.swt.provider.TrackListContentProvider;
import de.hifiburn.ui.swt.provider.TrackListLabelProvider;
import de.hifiburn.ui.swt.provider.TrackListSorterProvider;

public class MainWindow
{
  private class TrackViewerUpdateValueStrategy extends UpdateValueStrategy
  {
    protected IStatus doSet(IObservableValue observableValue, Object value)
    {
      Realm.getDefault().asyncExec(new Runnable()
      {
        public void run()
        {
          viewerTracks.refresh();
        }
      });
      return super.doSet(observableValue, value);
    }
  }

  private DataBindingContext m_bindingContext;

  protected Project project = ProjectManager.getInstance().getProject();

  protected Disc disc = ProjectManager.getInstance().getProject().getDisc();

  protected Track track = null;

  protected Shell shell;

  private Text txtDiscAlbum;

  private Text txtDiscInterpret;

  private Table tableTracks;

  private Text txtTrackTitle;

  private TableViewer viewerTracks;

  private Text txtTrackInterpret;

  private TabFolder tabFolder;
  private Composite compTracklist;
  private TabItem tbtmTracks;
  private Composite compTracks;
  private Group grpTrack;
  private Text txtLogFile;
  private Text txtLog;
  private ToolItem tltmBurn;

  /**
   * Launch the application.
   * 
   * @param args
   */
  public static void main(String[] args)
  {
    Display display = Display.getDefault();
    Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable()
    {
      public void run()
      {
        MainWindow window = null;
        try
        {
          ProjectManager.getInstance().initialize();
          window = new MainWindow();
          ProjectManager.getInstance().postUIInitialize();
        }
        catch (InitializeException _e)
        {
          MessageDialog.openError(null, "Konfigurationsfehler", _e.getMessage());
          Preferences.showPreferenceDialog(window.shell);
        }
        catch (IOException e)
        {
          MessageDialog.openError(null, "Initialisierungsfehler", e.getMessage());
          System.exit(2);
        }
        catch (Exception e)
        {
          e.printStackTrace();
          System.exit(1);
        }
        
        if (window!=null)
          window.open();
      }
    });
  }

  /**
   * Open the window.
   */
  public void open()
  {
    Display display = Display.getDefault();
    createContents();
    shell.open();
    shell.layout();
    while (!shell.isDisposed())
    {
      if (!display.readAndDispatch())
      {
        display.sleep();
      }
    }
  }

  /**
   * Create contents of the window.
   */
  protected void createContents()
  {
    shell = new Shell();
    shell.setMinimumSize(new Point(600, 500));
    shell.setImages(
        new Image[] {
            SWTResourceManager.getImage(MainWindow.class, "/de/hifiburn/ui/icons/icon72.png"),
            SWTResourceManager.getImage(MainWindow.class, "/de/hifiburn/ui/icons/icon48.png"),
            SWTResourceManager.getImage(MainWindow.class, "/de/hifiburn/ui/icons/icon32.png"),
            SWTResourceManager.getImage(MainWindow.class, "/de/hifiburn/ui/icons/icon16.png")});
    shell.setSize(600, 500);
    shell.setText("HifiBurn");
    shell.setLayout(new BorderLayout(0, 0));

    tabFolder = new TabFolder(shell, SWT.NONE);
    tabFolder.setLayoutData(BorderLayout.CENTER);

    TabItem tbtmDisc = new TabItem(tabFolder, SWT.NONE);
    tbtmDisc.setImage(SWTResourceManager.getImage(MainWindow.class, "/de/hifiburn/ui/icons/disc.png"));
    tbtmDisc.setText("Disc");

    Composite compDisc = new Composite(tabFolder, SWT.NONE);
    tbtmDisc.setControl(compDisc);
    FillLayout fl_compDisc = new FillLayout(SWT.HORIZONTAL);
    fl_compDisc.marginWidth = 5;
    fl_compDisc.marginHeight = 5;
    compDisc.setLayout(fl_compDisc);

    Group grpCdtext = new Group(compDisc, SWT.NONE);
    grpCdtext.setText("CD-Text");
    grpCdtext.setLayout(new GridLayout(2, false));

    Label lblAlbum = new Label(grpCdtext, SWT.NONE);
    lblAlbum.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblAlbum.setText("Album");

    txtDiscAlbum = new Text(grpCdtext, SWT.BORDER);
    txtDiscAlbum.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    Label lblInterpret = new Label(grpCdtext, SWT.NONE);
    lblInterpret.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblInterpret.setText("Interpret");

    txtDiscInterpret = new Text(grpCdtext, SWT.BORDER);
    txtDiscInterpret.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    tbtmTracks = new TabItem(tabFolder, SWT.NONE);
    tbtmTracks.setImage(SWTResourceManager.getImage(MainWindow.class, "/de/hifiburn/ui/icons/tracks.png"));
    tbtmTracks.setText("Tracks");

    compTracks = new Composite(tabFolder, SWT.NONE);
    tbtmTracks.setControl(compTracks);
    compTracks.setLayout(new BorderLayout(0, 0));

    compTracklist = new Composite(compTracks, SWT.NONE);
    compTracklist.setLayoutData(BorderLayout.CENTER);
    compTracklist.setLayout(new FillLayout(SWT.HORIZONTAL));

    viewerTracks = new TableViewer(compTracklist, SWT.BORDER | SWT.FULL_SELECTION);
    viewerTracks.setSorter(new TrackListSorterProvider());
    tableTracks = viewerTracks.getTable();
    
    tableTracks.addControlListener(new ControlAdapter()
    {
      @Override
      public void controlResized(ControlEvent e)
      {
        Table _t = (Table) e.getSource();
        TableColumn _c1 = _t.getColumn(0);
        TableColumn _c2 = _t.getColumn(1);
        TableColumn _c3 = _t.getColumn(2);
        TableColumn _c4 = _t.getColumn(3);

        _c1.setWidth(75);
        _c4.setWidth(75);

        int _width = Math.max(((_t.getSize().x-_t.getBorderWidth()*2) - 75 - 75) / 2, 10);
        _c2.setWidth(_width);
        _c3.setWidth(_width);
      }
    });
    tableTracks.setLinesVisible(true);
    tableTracks.setHeaderVisible(true);
    tableTracks.addSelectionListener(new SelectionListener()
    {
      @Override
      public void widgetSelected(SelectionEvent theE)
      {
        SwtTools.setEnabledRecursive(grpTrack, true);
      }
      
      @Override
      public void widgetDefaultSelected(SelectionEvent theE)
      {
      }
    });

    DragSource dsTracks = new DragSource(tableTracks, DND.DROP_MOVE | DND.DROP_COPY);
    dsTracks.setTransfer(new Transfer[] { TextTransfer.getInstance() });
    dsTracks.addDragListener(new DragSourceAdapter()
    {
      public void dragSetData(DragSourceEvent event)
      {
        event.data = ((Track) tableTracks.getSelection()[0].getData()).getFile().getAbsoluteFile().toString();
      }
    });

    DropTarget dtTracks = new DropTarget(tableTracks, DND.DROP_MOVE | DND.DROP_COPY);

    dtTracks.setTransfer(new Transfer[] { TextTransfer.getInstance() });
    dtTracks.addDropListener(new DropTargetAdapter()
    {
      public void drop(DropTargetEvent event)
      {
        Track _src = disc.getTrack((String) event.data);
        if (_src == null || event.item == null)
          return;

        Track _dst = (Track) event.item.getData();
        if (_dst == null)
          return;

        if (event.detail == DND.DROP_COPY)
        {
          // exchange
          disc.exchangeTracks(_src, _dst);
        }
        else
        {
          // move
          disc.moveTrack(_src, _dst);
        }
        
        viewerTracks.refresh();
        compTracklist.pack(true);
        tabFolder.setSelection(1);
      }
    });

    TableViewerColumn tableViewerColumn = new TableViewerColumn(viewerTracks, SWT.NONE);
    TableColumn tblclmnNr = tableViewerColumn.getColumn();
    tblclmnNr.setResizable(false);
    tblclmnNr.setWidth(75);

    TableColumn tblclmnInterpret = new TableColumn(tableTracks, SWT.NONE);
    tblclmnInterpret.setResizable(false);
    tblclmnInterpret.setWidth(100);
    tblclmnInterpret.setText("Interpret");

    TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(viewerTracks, SWT.NONE);

    TableColumn tblclmnTitel = tableViewerColumn_1.getColumn();
    tblclmnTitel.setResizable(false);
    tblclmnTitel.setWidth(10);
    tblclmnTitel.setText("Titel");

    TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(viewerTracks, SWT.NONE);
    TableColumn tblclmnLaufzeit = tableViewerColumn_2.getColumn();
    tblclmnLaufzeit.setResizable(false);
    tblclmnLaufzeit.setWidth(75);
    tblclmnLaufzeit.setText("Laufzeit");

    viewerTracks.setLabelProvider(new TrackListLabelProvider());
    viewerTracks.setContentProvider(new TrackListContentProvider());
    tblclmnLaufzeit.setResizable(false);
    tblclmnLaufzeit.setWidth(75);
    viewerTracks.setInput(disc.getTracks());

    grpTrack = new Group(compTracks, SWT.NONE);
    grpTrack.setText("Track");
    grpTrack.setLayoutData(BorderLayout.SOUTH);
    grpTrack.setLayout(new GridLayout(3, false));
    
    Label lblInterpret_1 = new Label(grpTrack, SWT.NONE);
    lblInterpret_1.setText("Interpret");

    txtTrackInterpret = new Text(grpTrack, SWT.BORDER);
    txtTrackInterpret.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Button btnInterpretAll = new Button(grpTrack, SWT.NONE);
    btnInterpretAll.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) 
      {
        for (Track _t : disc.getTracks())
          _t.setInterpret(txtTrackInterpret.getText());
        
        viewerTracks.refresh();
      }
    });
    btnInterpretAll.setImage(SWTResourceManager.getImage(MainWindow.class, "/de/hifiburn/ui/icons/modifyall.png"));

    Label lblTitel = new Label(grpTrack, SWT.NONE);
    lblTitel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblTitel.setText("Titel");

    txtTrackTitle = new Text(grpTrack, SWT.BORDER);
    txtTrackTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Button btnTitleAll = new Button(grpTrack, SWT.NONE);
    btnTitleAll.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        for (Track _t : disc.getTracks())
          _t.setTitle(txtTrackTitle.getText());
        
        viewerTracks.refresh();
      }
    });
    btnTitleAll.setImage(SWTResourceManager.getImage(MainWindow.class, "/de/hifiburn/ui/icons/modifyall.png"));
    
    TabItem tbtmLog = new TabItem(tabFolder, SWT.NONE);
    tbtmLog.setImage(SWTResourceManager.getImage(MainWindow.class, "/de/hifiburn/ui/icons/log.png"));
    tbtmLog.setText("Log");
    
    Composite compLog = new Composite(tabFolder, SWT.NONE);
    tbtmLog.setControl(compLog);
    compLog.setLayout(new BorderLayout(0, 0));
    
    Composite composite = new Composite(compLog, SWT.NONE);
    composite.setLayoutData(BorderLayout.NORTH);
    composite.setLayout(new GridLayout(2, false));
    
    Label lblDatei = new Label(composite, SWT.NONE);
    lblDatei.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblDatei.setText("Datei");
    
    txtLogFile = new Text(composite, SWT.BORDER);
    txtLogFile.setEditable(false);
    txtLogFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    txtLogFile.setText(ProjectManager.getInstance().getLogfile().getAbsolutePath());
    
    Composite composite_1 = new Composite(compLog, SWT.NONE);
    composite_1.setLayoutData(BorderLayout.CENTER);
    FillLayout fl_composite_1 = new FillLayout(SWT.HORIZONTAL);
    fl_composite_1.marginWidth = 5;
    fl_composite_1.marginHeight = 5;
    composite_1.setLayout(fl_composite_1);
    
    txtLog = new Text(composite_1, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
    txtLog.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
    txtLog.setEditable(false);
    Font terminalFont = JFaceResources.getFont(JFaceResources.TEXT_FONT);
    txtLog.setFont(terminalFont);
    
    m_bindingContext = initDataBindings();
    
    ProjectManager.getInstance().getLogwidget().setWidget(txtLog);
    SwtTools.setEnabledRecursive(grpTrack, false);
    
    Composite composite_2 = new Composite(shell, SWT.NONE);
    composite_2.setLayoutData(BorderLayout.NORTH);
        composite_2.setLayout(new BorderLayout(0, 0));
    
        ToolBar toolBar = new ToolBar(composite_2, SWT.FLAT | SWT.RIGHT);
        
            ToolItem tltmNewDisc = new ToolItem(toolBar, SWT.NONE);
            tltmNewDisc.setImage(SWTResourceManager.getImage(MainWindow.class, "/de/hifiburn/ui/icons/newdisc.png"));
            tltmNewDisc.setToolTipText("Neue Disc erstellen");
            
                @SuppressWarnings("unused")
                ToolItem toolItem = new ToolItem(toolBar, SWT.SEPARATOR);
                
                    ToolItem tltmAddTracks = new ToolItem(toolBar, SWT.NONE);
                    tltmAddTracks.addSelectionListener(new SelectionAdapter()
                    {
                      @Override
                      public void widgetSelected(SelectionEvent e)
                      {
                        addTracks();
                      }
                    });
                    tltmAddTracks.setToolTipText("Tracks hinzufügen");
                    tltmAddTracks.setImage(SWTResourceManager.getImage(MainWindow.class, "/de/hifiburn/ui/icons/addtrack.png"));
                    
                    ToolBar toolBar_1 = new ToolBar(composite_2, SWT.FLAT | SWT.RIGHT);
                    toolBar_1.setLayoutData(BorderLayout.EAST);
                    
                    ToolItem toolItem_2 = new ToolItem(toolBar_1, SWT.NONE);
                    toolItem_2.addSelectionListener(new SelectionAdapter() {
                      @Override
                      public void widgetSelected(SelectionEvent e) {
                        Preferences.showPreferenceDialog(shell);
                        setEnabledState();
                      }
                    });
                    toolItem_2.setToolTipText("Einstellungen anzeigen");
                    toolItem_2.setImage(SWTResourceManager.getImage(MainWindow.class, "/de/hifiburn/ui/icons/settings.png"));
                    
                    ToolItem toolItem_1 = new ToolItem(toolBar_1, SWT.SEPARATOR);
                    
                    tltmBurn = new ToolItem(toolBar_1, SWT.NONE);
                    tltmBurn.addSelectionListener(new SelectionAdapter() {
                      @Override
                      public void widgetSelected(SelectionEvent e) 
                      {
                        try 
                        {
                          
                          new ProgressMonitorDialog(shell).run(true, true, new IRunnableWithProgress()
                          {
                            @Override
                            public void run(IProgressMonitor theMonitor)
                                throws InvocationTargetException, InterruptedException
                            {
                              IBurner _burner = BurnerManager.getInstance().getBurner(); 
                              if (_burner==null)
                              {
                                Logger.getLogger(ProjectManager.class.getName()).log(Level.SEVERE, 
                                    String.format("Konnte Brenn-Backend nicht finden. Bitte prüfen Sie die Konfiguration"));
                                throw new InvocationTargetException(new Exception("Konnte Brenn-Backend nicht finden."));
                              }
                              if (!_burner.canBurn())
                              {
                                Logger.getLogger(ProjectManager.class.getName()).log(Level.SEVERE, 
                                    String.format("Das Brenn-Backend ist nicht bereit zum brennen. Bitte prüfen Sie die Konfiguration."));
                                throw new InvocationTargetException(
                                    new Exception("Das Brenn-Backend ist nicht bereit zum brennen. Bitte prüfen Sie die Konfiguration."));
                              }

                              List<IFilter> _preFilters = BurnerManager.getInstance().getPreFilters(_burner);
                              List<IFilter> _postFilters = BurnerManager.getInstance().getPostFilters(_burner);
                              
                              theMonitor.beginTask("Konvertiere Audiodateien",
                                  (project.getDisc().getTracks().size()*2)+100+_preFilters.size()+_postFilters.size());
                              
                              if (ProjectManager.getInstance().convertAudioFiles(
                                  new SubProgressMonitor(theMonitor, project.getDisc().getTracks().size()*2)) == 0)
                              {
                                try
                                {
                                  ProjectManager.getInstance().doPreProcessing(
                                      new SubProgressMonitor(theMonitor,_preFilters.size()),project,_preFilters);
                                  
                                  ProjectManager.getInstance().doBurn(new SubProgressMonitor(theMonitor,100),project,_burner);
                                  
                                  ProjectManager.getInstance().doPostProcessing(
                                      new SubProgressMonitor(theMonitor,_postFilters.size()),project, _postFilters);
                                }
                                catch (FilterException _e)
                                {
                                  throw new InvocationTargetException(_e);
                                }
                                catch (BurnerException _e)
                                {
                                  throw new InvocationTargetException(_e);
                                }
                              }
                              
                              // cleanup
                              for (Track _t : disc.getTracks())
                              {
                                if (_t.getWavfile()!=null && _t.getWavfile().exists())
                                  _t.getWavfile().delete();
                              }
                            }
                          });
                        } 
                        catch (InvocationTargetException _e) 
                        {
                          MessageDialog.openError(shell, "Fehler", 
                              _e.getCause().getMessage() + "\nBitte prüfen Sie das Log.");
                          tabFolder.setSelection(2);
                        } 
                        catch (InterruptedException _e) 
                        {
                        }
                      }
                    });
                    tltmBurn.setImage(SWTResourceManager.getImage(MainWindow.class, "/de/hifiburn/ui/icons/burn.png"));
                    tltmBurn.setText("CD brennen");
                    
                    setEnabledState();
  }

  /**
   * @param tltmBurn
   */
  protected void setEnabledState()
  {
    IBurner _burner = BurnerManager.getInstance().getBurner();
    if (_burner!=null)
      tltmBurn.setEnabled(_burner.canBurn());
  }

  /**
   * 
   */
  protected void addTracks()
  {
    FileDialog _fd = new FileDialog(shell, SWT.OPEN | SWT.MULTI);
    _fd.setText("Tracks hinzufügen");
    _fd.setFilterPath(System.getProperty("user.home"));
    List<String> _exts = new ArrayList<String>(AudioFileManager.getInstance().getExtensions());
    StringBuilder _sb = new StringBuilder();
    for (String _s : _exts)
    {
      _sb.append(_s);
      _sb.append(";");
    }
    _exts.add(0,_sb.toString());
    
    _fd.setFilterExtensions(_exts.toArray(new String[0]));
    String _fn = _fd.open();
    if (_fn != null)
    {
      List<File> _files = new ArrayList<File>();
      String[] _tmp = _fd.getFileNames();
      String _path = _fd.getFilterPath();
      if (_path.charAt(_path.length() - 1) != File.separatorChar)
        _path = _path + File.separatorChar;

      for (int i = 0, n = _tmp.length; i < n; i++)
      {
        _files.add(new File(_path + _tmp[i]));
      }

      try
      {
        ProjectManager.getInstance().addTracks(_files);
      }
      catch (IOException _e)
      {
        MessageDialog.openError(shell, "Fehler beim Hinzufügen von Tracks", _e.getMessage());
      }

      String _albumtitle = null;
      String _albuminterpret = null;
      for (File _f : _files)
      {
        Track _t = disc.getTrack(_f.getAbsolutePath());

        if (_t!=null)
        {
          if (_t.getAlbuminterpret() != null && _albuminterpret == null)
            _albuminterpret = _t.getAlbuminterpret();
  
          if (_t.getAlbumtitle() != null && _albumtitle == null)
            _albumtitle = _t.getAlbumtitle();
        }
      }

      if (_albumtitle != null && (disc.getAlbum() == null || !disc.getAlbum().equals(_albumtitle)))
      {
        if (disc.getAlbum() == null || disc.getAlbum().trim().length() == 0)
        {
          disc.setAlbum(disc.getTracks().get(0).getAlbumtitle());
        }
        else
        {
          if (MessageDialog.openConfirm(shell, "Frage",
              String.format("Soll der Titel der Disc auf \"%s\" geändert werden?", _albumtitle)))
          {
            disc.setAlbum(disc.getTracks().get(0).getAlbumtitle());
          }
        }
      }

      if (_albuminterpret != null && (disc.getInterpret() == null || !disc.getInterpret().equals(_albuminterpret)))
      {
        if (disc.getInterpret() == null || disc.getInterpret().trim().length() == 0)
        {
          disc.setInterpret(disc.getTracks().get(0).getAlbuminterpret());
        }
        else
        {
          if (MessageDialog.openConfirm(shell, "Frage",
              String.format("Soll der Interpret der Disc auf \"%s\" geändert werden?", _albuminterpret)))
          {
            disc.setInterpret(disc.getTracks().get(0).getAlbuminterpret());
          }
        }
      }

      tableTracks.setSelection(0);
      viewerTracks.refresh();
      
      tabFolder.setSelection(1);
    }
  }
  protected DataBindingContext initDataBindings() {
    DataBindingContext bindingContext = new DataBindingContext();
    //
    IObservableValue observeTextTxtDiscAlbumObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtDiscAlbum);
    IObservableValue albumDiscObserveValue = BeanProperties.value("album").observe(disc);
    bindingContext.bindValue(observeTextTxtDiscAlbumObserveWidget, albumDiscObserveValue, null, null);
    //
    IObservableValue observeTextTxtDiscInterpretObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtDiscInterpret);
    IObservableValue interpretDiscObserveValue = BeanProperties.value("interpret").observe(disc);
    bindingContext.bindValue(observeTextTxtDiscInterpretObserveWidget, interpretDiscObserveValue, null, null);
    //
    IObservableValue observeSingleSelectionViewerTracks = ViewerProperties.singleSelection().observe(viewerTracks);
    IObservableValue viewerTracksInterpretObserveDetailValue = BeanProperties.value(Track.class, "interpret", String.class).observeDetail(observeSingleSelectionViewerTracks);
    IObservableValue observeTextTxtTrackInterpretObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtTrackInterpret);
    bindingContext.bindValue(viewerTracksInterpretObserveDetailValue, observeTextTxtTrackInterpretObserveWidget, null, new TrackViewerUpdateValueStrategy());
    //
    IObservableValue observeSingleSelectionViewerTracks_1 = ViewerProperties.singleSelection().observe(viewerTracks);
    IObservableValue viewerTracksTitleObserveDetailValue = BeanProperties.value(Track.class, "title", String.class).observeDetail(observeSingleSelectionViewerTracks_1);
    IObservableValue observeTextTxtTrackTitleObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtTrackTitle);
    bindingContext.bindValue(viewerTracksTitleObserveDetailValue, observeTextTxtTrackTitleObserveWidget, null, new TrackViewerUpdateValueStrategy());
    //
    return bindingContext;
  }
}
