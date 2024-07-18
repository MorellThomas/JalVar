/*
 * Jalview - A Sequence Alignment Editor and Viewer ($$Version-Rel$$)
 * Copyright (C) $$Year-Rel$$ The Jalview Authors
 * 
 * This file is part of Jalview.
 * 
 * Jalview is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * Jalview is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Jalview.  If not, see <http://www.gnu.org/licenses/>.
 * The Jalview Authors are detailed in the 'AUTHORS' file.
 */
package jalview.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultDesktopManager;
import javax.swing.DesktopManager;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.stackoverflowusers.file.WindowsShortcut;

import jalview.analysis.Analysis;
import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.api.structures.JalviewStructureDisplayI;
import jalview.bin.Cache;
import jalview.bin.Jalview;
import jalview.datamodel.Alignment;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.ImageExporter.ImageWriterI;
import jalview.gui.QuitHandler.QResponse;
import jalview.io.BackupFiles;
import jalview.io.DataSourceType;
import jalview.io.EpReferenceFile;
import jalview.io.FileFormat;
import jalview.io.FileFormatException;
import jalview.io.FileFormatI;
import jalview.io.FileFormats;
import jalview.io.FileLoader;
import jalview.io.FormatAdapter;
import jalview.io.IdentifyFile;
import jalview.io.JalviewFileChooser;
import jalview.io.JalviewFileView;
import jalview.io.exceptions.ImageOutputException;
import jalview.jbgui.GSplitFrame;
import jalview.jbgui.GStructureViewer;
import jalview.project.Jalview2XML;
import jalview.structure.StructureSelectionManager;
import jalview.urls.IdOrgSettings;
import jalview.util.BrowserLauncher;
import jalview.util.ChannelProperties;
import jalview.util.ImageMaker.TYPE;
import jalview.util.LaunchUtils;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.util.ShortcutKeyMaskExWrapper;
import jalview.util.UrlConstants;
import jalview.viewmodel.AlignmentViewport;
import jalview.ws.params.ParamManager;
import jalview.ws.utils.UrlDownloadClient;

/**
 * Jalview Desktop
 * 
 * 
 * @author $author$
 * @version $Revision: 1.155 $
 */
public class Desktop extends jalview.jbgui.GDesktop
        implements DropTargetListener, ClipboardOwner, IProgressIndicator,
        jalview.api.StructureSelectionManagerProvider
{
  private static final String CITATION;
  static
  {
    URL bg_logo_url = ChannelProperties.getImageURL(
            "bg_logo." + String.valueOf(SplashScreen.logoSize));
    URL uod_logo_url = ChannelProperties.getImageURL(
            "uod_banner." + String.valueOf(SplashScreen.logoSize));
    boolean logo = (bg_logo_url != null || uod_logo_url != null);
    StringBuilder sb = new StringBuilder();
    sb.append(
            "<br><br>Jalview is free software released under GPLv3.<br><br>Development is managed by The Barton Group, University of Dundee, Scotland, UK.");
    if (logo)
    {
      sb.append("<br>");
    }
    sb.append(bg_logo_url == null ? ""
            : "<img alt=\"Barton Group logo\" src=\""
                    + bg_logo_url.toString() + "\">");
    sb.append(uod_logo_url == null ? ""
            : "&nbsp;<img alt=\"University of Dundee shield\" src=\""
                    + uod_logo_url.toString() + "\">");
    sb.append(
            "<br><br>For help, see <a href=\"https://www.jalview.org/faq\">www.jalview.org/faq</a> and join <a href=\"https://discourse.jalview.org\">discourse.jalview.org</a>");
    sb.append("<br><br>If  you use Jalview, please cite:"
            + "<br>Waterhouse, A.M., Procter, J.B., Martin, D.M.A, Clamp, M. and Barton, G. J. (2009)"
            + "<br>Jalview Version 2 - a multiple sequence alignment editor and analysis workbench"
            + "<br>Bioinformatics <a href=\"https://doi.org/10.1093/bioinformatics/btp033\">doi: 10.1093/bioinformatics/btp033</a>");
    CITATION = sb.toString();
  }

  private static final String DEFAULT_AUTHORS = "The Jalview Authors (See AUTHORS file for current list)";

  private static int DEFAULT_MIN_WIDTH = 300;

  private static int DEFAULT_MIN_HEIGHT = 250;

  private static int ALIGN_FRAME_DEFAULT_MIN_WIDTH = 600;

  private static int ALIGN_FRAME_DEFAULT_MIN_HEIGHT = 70;

  private static final String EXPERIMENTAL_FEATURES = "EXPERIMENTAL_FEATURES";

  public static final String CONFIRM_KEYBOARD_QUIT = "CONFIRM_KEYBOARD_QUIT";

  public static HashMap<String, FileWriter> savingFiles = new HashMap<String, FileWriter>();

  private static int DRAG_MODE = JDesktopPane.OUTLINE_DRAG_MODE;

  public static void setLiveDragMode(boolean b)
  {
    DRAG_MODE = b ? JDesktopPane.LIVE_DRAG_MODE
            : JDesktopPane.OUTLINE_DRAG_MODE;
    if (desktop != null)
      desktop.setDragMode(DRAG_MODE);
  }

  private JalviewChangeSupport changeSupport = new JalviewChangeSupport();

  public static boolean nosplash = false;

  /**
   * news reader - null if it was never started.
   */
  private BlogReader jvnews = null;

  private File projectFile;

  /**
   * @param listener
   * @see jalview.gui.JalviewChangeSupport#addJalviewPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void addJalviewPropertyChangeListener(
          PropertyChangeListener listener)
  {
    changeSupport.addJalviewPropertyChangeListener(listener);
  }

  /**
   * @param propertyName
   * @param listener
   * @see jalview.gui.JalviewChangeSupport#addJalviewPropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  public void addJalviewPropertyChangeListener(String propertyName,
          PropertyChangeListener listener)
  {
    changeSupport.addJalviewPropertyChangeListener(propertyName, listener);
  }

  /**
   * @param propertyName
   * @param listener
   * @see jalview.gui.JalviewChangeSupport#removeJalviewPropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  public void removeJalviewPropertyChangeListener(String propertyName,
          PropertyChangeListener listener)
  {
    changeSupport.removeJalviewPropertyChangeListener(propertyName,
            listener);
  }

  /** Singleton Desktop instance */
  public static Desktop instance;

  public static MyDesktopPane desktop;

  public static MyDesktopPane getDesktop()
  {
    // BH 2018 could use currentThread() here as a reference to a
    // Hashtable<Thread, MyDesktopPane> in JavaScript
    return desktop;
  }

  static int openFrameCount = 0;

  static final int xOffset = 30;

  static final int yOffset = 30;

  public static jalview.ws.jws1.Discoverer discoverer;

  public static Object[] jalviewClipboard;

  public static boolean internalCopy = false;

  static int fileLoadingCount = 0;

  class MyDesktopManager implements DesktopManager
  {

    private DesktopManager delegate;

    public MyDesktopManager(DesktopManager delegate)
    {
      this.delegate = delegate;
    }

    @Override
    public void activateFrame(JInternalFrame f)
    {
      try
      {
        delegate.activateFrame(f);
      } catch (NullPointerException npe)
      {
        Point p = getMousePosition();
        instance.showPasteMenu(p.x, p.y);
      }
    }

    @Override
    public void beginDraggingFrame(JComponent f)
    {
      delegate.beginDraggingFrame(f);
    }

    @Override
    public void beginResizingFrame(JComponent f, int direction)
    {
      delegate.beginResizingFrame(f, direction);
    }

    @Override
    public void closeFrame(JInternalFrame f)
    {
      delegate.closeFrame(f);
    }

    @Override
    public void deactivateFrame(JInternalFrame f)
    {
      delegate.deactivateFrame(f);
    }

    @Override
    public void deiconifyFrame(JInternalFrame f)
    {
      delegate.deiconifyFrame(f);
    }

    @Override
    public void dragFrame(JComponent f, int newX, int newY)
    {
      if (newY < 0)
      {
        newY = 0;
      }
      delegate.dragFrame(f, newX, newY);
    }

    @Override
    public void endDraggingFrame(JComponent f)
    {
      delegate.endDraggingFrame(f);
      desktop.repaint();
    }

    @Override
    public void endResizingFrame(JComponent f)
    {
      delegate.endResizingFrame(f);
      desktop.repaint();
    }

    @Override
    public void iconifyFrame(JInternalFrame f)
    {
      delegate.iconifyFrame(f);
    }

    @Override
    public void maximizeFrame(JInternalFrame f)
    {
      delegate.maximizeFrame(f);
    }

    @Override
    public void minimizeFrame(JInternalFrame f)
    {
      delegate.minimizeFrame(f);
    }

    @Override
    public void openFrame(JInternalFrame f)
    {
      delegate.openFrame(f);
    }

    @Override
    public void resizeFrame(JComponent f, int newX, int newY, int newWidth,
            int newHeight)
    {
      if (newY < 0)
      {
        newY = 0;
      }
      delegate.resizeFrame(f, newX, newY, newWidth, newHeight);
    }

    @Override
    public void setBoundsForFrame(JComponent f, int newX, int newY,
            int newWidth, int newHeight)
    {
      delegate.setBoundsForFrame(f, newX, newY, newWidth, newHeight);
    }

    // All other methods, simply delegate

  }

  /**
   * Creates a new Desktop object.
   */
  public Desktop()
  {
    super();
    /**
     * A note to implementors. It is ESSENTIAL that any activities that might
     * block are spawned off as threads rather than waited for during this
     * constructor.
     */
    instance = this;

    doConfigureStructurePrefs();
    setTitle(ChannelProperties.getProperty("app_name") + " "
            + Cache.getProperty("VERSION"));

    /**
     * Set taskbar "grouped windows" name for linux desktops (works in GNOME and
     * KDE). This uses sun.awt.X11.XToolkit.awtAppClassName which is not
     * officially documented or guaranteed to exist, so we access it via
     * reflection. There appear to be unfathomable criteria about what this
     * string can contain, and it if doesn't meet those criteria then "java"
     * (KDE) or "jalview-bin-Jalview" (GNOME) is used. "Jalview", "Jalview
     * Develop" and "Jalview Test" seem okay, but "Jalview non-release" does
     * not. The reflection access may generate a warning: WARNING: An illegal
     * reflective access operation has occurred WARNING: Illegal reflective
     * access by jalview.gui.Desktop () to field
     * sun.awt.X11.XToolkit.awtAppClassName which I don't think can be avoided.
     */
    if (Platform.isLinux())
    {
      if (LaunchUtils.getJavaVersion() >= 11)
      {
        /*
         * Send this message to stderr as the warning that follows (due to
         * reflection) also goes to stderr.
         */
        System.err.println(
                "Linux platform only! You may have the following warning next: \"WARNING: An illegal reflective access operation has occurred\"\nThis is expected and cannot be avoided, sorry about that.");
      }
      final String awtAppClassName = "awtAppClassName";
      try
      {
        Toolkit xToolkit = Toolkit.getDefaultToolkit();
        Field[] declaredFields = xToolkit.getClass().getDeclaredFields();
        Field awtAppClassNameField = null;

        if (Arrays.stream(declaredFields)
                .anyMatch(f -> f.getName().equals(awtAppClassName)))
        {
          awtAppClassNameField = xToolkit.getClass()
                  .getDeclaredField(awtAppClassName);
        }

        String title = ChannelProperties.getProperty("app_name");
        if (awtAppClassNameField != null)
        {
          awtAppClassNameField.setAccessible(true);
          awtAppClassNameField.set(xToolkit, title);
        }
        else
        {
          jalview.bin.Console
                  .debug("XToolkit: " + awtAppClassName + " not found");
        }
      } catch (Exception e)
      {
        jalview.bin.Console.debug("Error setting " + awtAppClassName);
        jalview.bin.Console.trace(Cache.getStackTraceString(e));
      }
    }

    setIconImages(ChannelProperties.getIconList());

    // override quit handling when GUI OS close [X] button pressed
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent ev)
      {
        QuitHandler.QResponse ret = desktopQuit(true, true); // ui, disposeFlag
      }
    });

    boolean selmemusage = Cache.getDefault("SHOW_MEMUSAGE", false);

    boolean showjconsole = Cache.getDefault("SHOW_JAVA_CONSOLE", false);
    desktop = new MyDesktopPane(selmemusage);

    showMemusage.setSelected(selmemusage);
    desktop.setBackground(Color.white);

    getContentPane().setLayout(new BorderLayout());
    // alternate config - have scrollbars - see notes in JAL-153
    // JScrollPane sp = new JScrollPane();
    // sp.getViewport().setView(desktop);
    // getContentPane().add(sp, BorderLayout.CENTER);

    // BH 2018 - just an experiment to try unclipped JInternalFrames.
    if (Platform.isJS())
    {
      getRootPane().putClientProperty("swingjs.overflow.hidden", "false");
    }

    getContentPane().add(desktop, BorderLayout.CENTER);
    desktop.setDragMode(DRAG_MODE);

    // This line prevents Windows Look&Feel resizing all new windows to maximum
    // if previous window was maximised
    desktop.setDesktopManager(new MyDesktopManager(
            Platform.isJS() ? desktop.getDesktopManager()
                    : new DefaultDesktopManager()));
    /*
    (Platform.isWindowsAndNotJS() ? new DefaultDesktopManager()
            : Platform.isAMacAndNotJS()
                    ? new AquaInternalFrameManager(
                            desktop.getDesktopManager())
                    : desktop.getDesktopManager())));
                    */

    Rectangle dims = getLastKnownDimensions("");
    if (dims != null)
    {
      setBounds(dims);
    }
    else
    {
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      int xPos = Math.max(5, (screenSize.width - 900) / 2);
      int yPos = Math.max(5, (screenSize.height - 650) / 2);
      setBounds(xPos, yPos, 900, 650);
    }

    // start dialogue queue for single dialogues
    startDialogQueue();

    if (!Platform.isJS())
    /**
     * Java only
     * 
     * @j2sIgnore
     */
    {
      jconsole = new Console(this, showjconsole);
      jconsole.setHeader(Cache.getVersionDetailsForConsole());
      showConsole(showjconsole);

      showNews.setVisible(false);

      experimentalFeatures.setSelected(showExperimental());

      getIdentifiersOrgData();

      checkURLLinks();

      // Spawn a thread that shows the splashscreen
      if (!nosplash)
      {
        SwingUtilities.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            new SplashScreen(true);
          }
        });
      }

      // Thread off a new instance of the file chooser - this reduces the time
      // it
      // takes to open it later on.
      new Thread(new Runnable()
      {
        @Override
        public void run()
        {
          jalview.bin.Console.debug("Filechooser init thread started.");
          String fileFormat = Cache.getProperty("DEFAULT_FILE_FORMAT");
          JalviewFileChooser.forRead(Cache.getProperty("LAST_DIRECTORY"),
                  fileFormat);
          jalview.bin.Console.debug("Filechooser init thread finished.");
        }
      }).start();
      // Add the service change listener
      changeSupport.addJalviewPropertyChangeListener("services",
              new PropertyChangeListener()
              {

                @Override
                public void propertyChange(PropertyChangeEvent evt)
                {
                  jalview.bin.Console
                          .debug("Firing service changed event for "
                                  + evt.getNewValue());
                  JalviewServicesChanged(evt);
                }
              });
    }

    this.setDropTarget(new java.awt.dnd.DropTarget(desktop, this));

    MouseAdapter ma;
    this.addMouseListener(ma = new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent evt)
      {
        if (evt.isPopupTrigger()) // Mac
        {
          showPasteMenu(evt.getX(), evt.getY());
        }
      }

      @Override
      public void mouseReleased(MouseEvent evt)
      {
        if (evt.isPopupTrigger()) // Windows
        {
          showPasteMenu(evt.getX(), evt.getY());
        }
      }
    });
    desktop.addMouseListener(ma);

    if (Platform.isJS())
    {
      // used for jalviewjsTest
      jalview.bin.Console.info("JALVIEWJS: CREATED DESKTOP");
    }
  }

  /**
   * Answers true if user preferences to enable experimental features is True
   * (on), else false
   * 
   * @return
   */
  public boolean showExperimental()
  {
    String experimental = Cache.getDefault(EXPERIMENTAL_FEATURES,
            Boolean.FALSE.toString());
    return Boolean.valueOf(experimental).booleanValue();
  }

  public void doConfigureStructurePrefs()
  {
    // configure services
    StructureSelectionManager ssm = StructureSelectionManager
            .getStructureSelectionManager(this);
    if (Cache.getDefault(Preferences.ADD_SS_ANN, true))
    {
      ssm.setAddTempFacAnnot(
              Cache.getDefault(Preferences.ADD_TEMPFACT_ANN, true));
      ssm.setProcessSecondaryStructure(
              Cache.getDefault(Preferences.STRUCT_FROM_PDB, true));
      // JAL-3915 - RNAView is no longer an option so this has no effect
      ssm.setSecStructServices(
              Cache.getDefault(Preferences.USE_RNAVIEW, false));
    }
    else
    {
      ssm.setAddTempFacAnnot(false);
      ssm.setProcessSecondaryStructure(false);
      ssm.setSecStructServices(false);
    }
  }

  public void checkForNews()
  {
    final Desktop me = this;
    // Thread off the news reader, in case there are connection problems.
    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        jalview.bin.Console.debug("Starting news thread.");
        jvnews = new BlogReader(me);
        showNews.setVisible(true);
        jalview.bin.Console.debug("Completed news thread.");
      }
    }).start();
  }

  public void getIdentifiersOrgData()
  {
    if (Cache.getProperty("NOIDENTIFIERSSERVICE") == null)
    {// Thread off the identifiers fetcher
      new Thread(new Runnable()
      {
        @Override
        public void run()
        {
          jalview.bin.Console
                  .debug("Downloading data from identifiers.org");
          try
          {
            UrlDownloadClient.download(IdOrgSettings.getUrl(),
                    IdOrgSettings.getDownloadLocation());
          } catch (IOException e)
          {
            jalview.bin.Console
                    .debug("Exception downloading identifiers.org data"
                            + e.getMessage());
          }
        }
      }).start();
      ;
    }
  }

  @Override
  protected void showNews_actionPerformed(ActionEvent e)
  {
    showNews(showNews.isSelected());
  }

  void showNews(boolean visible)
  {
    jalview.bin.Console.debug((visible ? "Showing" : "Hiding") + " news.");
    showNews.setSelected(visible);
    if (visible && !jvnews.isVisible())
    {
      new Thread(new Runnable()
      {
        @Override
        public void run()
        {
          long now = System.currentTimeMillis();
          Desktop.instance.setProgressBar(
                  MessageManager.getString("status.refreshing_news"), now);
          jvnews.refreshNews();
          Desktop.instance.setProgressBar(null, now);
          jvnews.showNews();
        }
      }).start();
    }
  }

  /**
   * recover the last known dimensions for a jalview window
   * 
   * @param windowName
   *          - empty string is desktop, all other windows have unique prefix
   * @return null or last known dimensions scaled to current geometry (if last
   *         window geom was known)
   */
  Rectangle getLastKnownDimensions(String windowName)
  {
    // TODO: lock aspect ratio for scaling desktop Bug #0058199
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    String x = Cache.getProperty(windowName + "SCREEN_X");
    String y = Cache.getProperty(windowName + "SCREEN_Y");
    String width = Cache.getProperty(windowName + "SCREEN_WIDTH");
    String height = Cache.getProperty(windowName + "SCREEN_HEIGHT");
    if ((x != null) && (y != null) && (width != null) && (height != null))
    {
      int ix = Integer.parseInt(x), iy = Integer.parseInt(y),
              iw = Integer.parseInt(width), ih = Integer.parseInt(height);
      if (Cache.getProperty("SCREENGEOMETRY_WIDTH") != null)
      {
        // attempt #1 - try to cope with change in screen geometry - this
        // version doesn't preserve original jv aspect ratio.
        // take ratio of current screen size vs original screen size.
        double sw = ((1f * screenSize.width) / (1f * Integer
                .parseInt(Cache.getProperty("SCREENGEOMETRY_WIDTH"))));
        double sh = ((1f * screenSize.height) / (1f * Integer
                .parseInt(Cache.getProperty("SCREENGEOMETRY_HEIGHT"))));
        // rescale the bounds depending upon the current screen geometry.
        ix = (int) (ix * sw);
        iw = (int) (iw * sw);
        iy = (int) (iy * sh);
        ih = (int) (ih * sh);
        while (ix >= screenSize.width)
        {
          jalview.bin.Console.debug(
                  "Window geometry location recall error: shifting horizontal to within screenbounds.");
          ix -= screenSize.width;
        }
        while (iy >= screenSize.height)
        {
          jalview.bin.Console.debug(
                  "Window geometry location recall error: shifting vertical to within screenbounds.");
          iy -= screenSize.height;
        }
        jalview.bin.Console.debug(
                "Got last known dimensions for " + windowName + ": x:" + ix
                        + " y:" + iy + " width:" + iw + " height:" + ih);
      }
      // return dimensions for new instance
      return new Rectangle(ix, iy, iw, ih);
    }
    return null;
  }

  void showPasteMenu(int x, int y)
  {
    JPopupMenu popup = new JPopupMenu();
    JMenuItem item = new JMenuItem(
            MessageManager.getString("label.paste_new_window"));
    item.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent evt)
      {
        paste();
      }
    });

    popup.add(item);
    popup.show(this, x, y);
  }

  public void paste()
  {
    // quick patch for JAL-4150 - needs some more work and test coverage
    // TODO - unify below and AlignFrame.paste()
    // TODO - write tests and fix AlignFrame.paste() which doesn't track if
    // clipboard has come from a different alignment window than the one where
    // paste has been called! JAL-4151

    if (Desktop.jalviewClipboard != null)
    {
      // The clipboard was filled from within Jalview, we must use the
      // sequences
      // And dataset from the copied alignment
      SequenceI[] newseq = (SequenceI[]) Desktop.jalviewClipboard[0];
      // be doubly sure that we create *new* sequence objects.
      SequenceI[] sequences = new SequenceI[newseq.length];
      for (int i = 0; i < newseq.length; i++)
      {
        sequences[i] = new Sequence(newseq[i]);
      }
      Alignment alignment = new Alignment(sequences);
      // dataset is inherited
      alignment.setDataset((Alignment) Desktop.jalviewClipboard[1]);
      AlignFrame af = new AlignFrame(alignment, AlignFrame.DEFAULT_WIDTH,
              AlignFrame.DEFAULT_HEIGHT);
      String newtitle = new String("Copied sequences");

      if (Desktop.jalviewClipboard[2] != null)
      {
        HiddenColumns hc = (HiddenColumns) Desktop.jalviewClipboard[2];
        af.viewport.setHiddenColumns(hc);
      }

      Desktop.addInternalFrame(af, newtitle, AlignFrame.DEFAULT_WIDTH,
              AlignFrame.DEFAULT_HEIGHT);

    }
    else
    {
      try
      {
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = c.getContents(this);

        if (contents != null)
        {
          String file = (String) contents
                  .getTransferData(DataFlavor.stringFlavor);

          FileFormatI format = new IdentifyFile().identify(file,
                  DataSourceType.PASTE);

          new FileLoader().LoadFile(file, DataSourceType.PASTE, format);

        }
      } catch (Exception ex)
      {
        System.out.println(
                "Unable to paste alignment from system clipboard:\n" + ex);
      }
    }
  }

  /**
   * Adds and opens the given frame to the desktop
   * 
   * @param frame
   *          Frame to show
   * @param title
   *          Visible Title
   * @param w
   *          width
   * @param h
   *          height
   */
  public static synchronized void addInternalFrame(
          final JInternalFrame frame, String title, int w, int h)
  {
    addInternalFrame(frame, title, true, w, h, true, false);
  }

  /**
   * Add an internal frame to the Jalview desktop
   * 
   * @param frame
   *          Frame to show
   * @param title
   *          Visible Title
   * @param makeVisible
   *          When true, display frame immediately, otherwise, caller must call
   *          setVisible themselves.
   * @param w
   *          width
   * @param h
   *          height
   */
  public static synchronized void addInternalFrame(
          final JInternalFrame frame, String title, boolean makeVisible,
          int w, int h)
  {
    addInternalFrame(frame, title, makeVisible, w, h, true, false);
  }

  /**
   * Add an internal frame to the Jalview desktop and make it visible
   * 
   * @param frame
   *          Frame to show
   * @param title
   *          Visible Title
   * @param w
   *          width
   * @param h
   *          height
   * @param resizable
   *          Allow resize
   */
  public static synchronized void addInternalFrame(
          final JInternalFrame frame, String title, int w, int h,
          boolean resizable)
  {
    addInternalFrame(frame, title, true, w, h, resizable, false);
  }

  /**
   * Add an internal frame to the Jalview desktop
   * 
   * @param frame
   *          Frame to show
   * @param title
   *          Visible Title
   * @param makeVisible
   *          When true, display frame immediately, otherwise, caller must call
   *          setVisible themselves.
   * @param w
   *          width
   * @param h
   *          height
   * @param resizable
   *          Allow resize
   * @param ignoreMinSize
   *          Do not set the default minimum size for frame
   */
  public static synchronized void addInternalFrame(
          final JInternalFrame frame, String title, boolean makeVisible,
          int w, int h, boolean resizable, boolean ignoreMinSize)
  {

    // TODO: allow callers to determine X and Y position of frame (eg. via
    // bounds object).
    // TODO: consider fixing method to update entries in the window submenu with
    // the current window title

    frame.setTitle(title);
    if (frame.getWidth() < 1 || frame.getHeight() < 1)
    {
      frame.setSize(w, h);
    }
    // THIS IS A PUBLIC STATIC METHOD, SO IT MAY BE CALLED EVEN IN
    // A HEADLESS STATE WHEN NO DESKTOP EXISTS. MUST RETURN
    // IF JALVIEW IS RUNNING HEADLESS
    // ///////////////////////////////////////////////
    if (instance == null || (System.getProperty("java.awt.headless") != null
            && System.getProperty("java.awt.headless").equals("true")))
    {
      return;
    }

    openFrameCount++;

    if (!ignoreMinSize)
    {
      frame.setMinimumSize(
              new Dimension(DEFAULT_MIN_WIDTH, DEFAULT_MIN_HEIGHT));

      // Set default dimension for Alignment Frame window.
      // The Alignment Frame window could be added from a number of places,
      // hence,
      // I did this here in order not to miss out on any Alignment frame.
      if (frame instanceof AlignFrame)
      {
        frame.setMinimumSize(new Dimension(ALIGN_FRAME_DEFAULT_MIN_WIDTH,
                ALIGN_FRAME_DEFAULT_MIN_HEIGHT));
      }
    }

    frame.setVisible(makeVisible);
    frame.setClosable(true);
    frame.setResizable(resizable);
    frame.setMaximizable(resizable);
    frame.setIconifiable(resizable);
    frame.setOpaque(Platform.isJS());

    if (frame.getX() < 1 && frame.getY() < 1)
    {
      frame.setLocation(xOffset * openFrameCount,
              yOffset * ((openFrameCount - 1) % 10) + yOffset);
    }

    /*
     * add an entry for the new frame in the Window menu (and remove it when the
     * frame is closed)
     */
    final JMenuItem menuItem = new JMenuItem(title);
    frame.addInternalFrameListener(new InternalFrameAdapter()
    {
      @Override
      public void internalFrameActivated(InternalFrameEvent evt)
      {
        JInternalFrame itf = desktop.getSelectedFrame();
        if (itf != null)
        {
          if (itf instanceof AlignFrame)
          {
            Jalview.setCurrentAlignFrame((AlignFrame) itf);
          }
          itf.requestFocus();
        }
      }

      @Override
      public void internalFrameClosed(InternalFrameEvent evt)
      {
        PaintRefresher.RemoveComponent(frame);

        /*
         * defensive check to prevent frames being added half off the window
         */
        if (openFrameCount > 0)
        {
          openFrameCount--;
        }

        /*
         * ensure no reference to alignFrame retained by menu item listener
         */
        if (menuItem.getActionListeners().length > 0)
        {
          menuItem.removeActionListener(menuItem.getActionListeners()[0]);
        }
        windowMenu.remove(menuItem);
      }
    });

    menuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        try
        {
          frame.setSelected(true);
          frame.setIcon(false);
        } catch (java.beans.PropertyVetoException ex)
        {

        }
      }
    });

    setKeyBindings(frame);

    // Since the latest FlatLaf patch, we occasionally have problems showing structureViewer frames...
    int tries=3;
    boolean shown=false;
    Exception last=null;
    do
    {
      try
      {
        desktop.add(frame);
        shown=true;
      } catch (IllegalArgumentException iaex)
      {
        last=iaex;
        tries--;
        jalview.bin.Console.info(
                "Squashed IllegalArgument Exception (" + tries + " left) for "+frame.getTitle(),
                iaex);
        try
        {
          Thread.sleep(5);
        } catch (InterruptedException iex)
        {
        }
        ;
      }
    } while (!shown && tries > 0);
    if (!shown)
    {
      jalview.bin.Console.error("Serious Problem whilst showing window "+frame.getTitle(),last);
    }

    windowMenu.add(menuItem);

    frame.toFront();
    try
    {
      frame.setSelected(true);
      frame.requestFocus();
    } catch (java.beans.PropertyVetoException ve)
    {
    } catch (java.lang.ClassCastException cex)
    {
      jalview.bin.Console.warn(
              "Squashed a possible GUI implementation error. If you can recreate this, please look at https://issues.jalview.org/browse/JAL-869",
              cex);
    }
  }

  /**
   * Add key bindings to a JInternalFrame so that Ctrl-W and Cmd-W will close
   * the window
   * 
   * @param frame
   */
  private static void setKeyBindings(JInternalFrame frame)
  {
    @SuppressWarnings("serial")
    final Action closeAction = new AbstractAction()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        frame.dispose();
      }
    };

    /*
     * set up key bindings for Ctrl-W and Cmd-W, with the same (Close) action
     */
    KeyStroke ctrlWKey = KeyStroke.getKeyStroke(KeyEvent.VK_W,
            InputEvent.CTRL_DOWN_MASK);
    KeyStroke cmdWKey = KeyStroke.getKeyStroke(KeyEvent.VK_W,
            ShortcutKeyMaskExWrapper.getMenuShortcutKeyMaskEx());

    InputMap inputMap = frame
            .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    String ctrlW = ctrlWKey.toString();
    inputMap.put(ctrlWKey, ctrlW);
    inputMap.put(cmdWKey, ctrlW);

    ActionMap actionMap = frame.getActionMap();
    actionMap.put(ctrlW, closeAction);
  }

  @Override
  public void lostOwnership(Clipboard clipboard, Transferable contents)
  {
    if (!internalCopy)
    {
      Desktop.jalviewClipboard = null;
    }

    internalCopy = false;
  }

  @Override
  public void dragEnter(DropTargetDragEvent evt)
  {
  }

  @Override
  public void dragExit(DropTargetEvent evt)
  {
  }

  @Override
  public void dragOver(DropTargetDragEvent evt)
  {
  }

  @Override
  public void dropActionChanged(DropTargetDragEvent evt)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  @Override
  public void drop(DropTargetDropEvent evt)
  {
    boolean success = true;
    // JAL-1552 - acceptDrop required before getTransferable call for
    // Java's Transferable for native dnd
    evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
    Transferable t = evt.getTransferable();
    List<Object> files = new ArrayList<>();
    List<DataSourceType> protocols = new ArrayList<>();

    try
    {
      Desktop.transferFromDropTarget(files, protocols, evt, t);
    } catch (Exception e)
    {
      e.printStackTrace();
      success = false;
    }

    if (files != null)
    {
      try
      {
        for (int i = 0; i < files.size(); i++)
        {
          // BH 2018 File or String
          Object file = files.get(i);
          String fileName = file.toString();
          DataSourceType protocol = (protocols == null)
                  ? DataSourceType.FILE
                  : protocols.get(i);
          FileFormatI format = null;

          if (fileName.endsWith(".jar"))
          {
            format = FileFormat.Jalview;

          }
          else
          {
            format = new IdentifyFile().identify(file, protocol);
          }
          if (file instanceof File)
          {
            Platform.cacheFileData((File) file);
          }
          new FileLoader().LoadFile(null, file, protocol, format);

        }
      } catch (Exception ex)
      {
        success = false;
      }
    }
    evt.dropComplete(success); // need this to ensure input focus is properly
                               // transfered to any new windows created
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void inputLocalFileMenuItem_actionPerformed(AlignViewport viewport)
  {
    String fileFormat = Cache.getProperty("DEFAULT_FILE_FORMAT");
    JalviewFileChooser chooser = JalviewFileChooser.forRead(
            Cache.getProperty("LAST_DIRECTORY"), fileFormat,
            BackupFiles.getEnabled());

    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(
            MessageManager.getString("label.open_local_file"));
    chooser.setToolTipText(MessageManager.getString("action.open"));

    chooser.setResponseHandler(0, () -> {
      File selectedFile = chooser.getSelectedFile();
      Cache.setProperty("LAST_DIRECTORY", selectedFile.getParent());

      FileFormatI format = chooser.getSelectedFormat();

      /*
       * Call IdentifyFile to verify the file contains what its extension implies.
       * Skip this step for dynamically added file formats, because IdentifyFile does
       * not know how to recognise them.
       */
      if (FileFormats.getInstance().isIdentifiable(format))
      {
        try
        {
          format = new IdentifyFile().identify(selectedFile,
                  DataSourceType.FILE);
        } catch (FileFormatException e)
        {
          // format = null; //??
        }
      }

      new FileLoader().LoadFile(viewport, selectedFile, DataSourceType.FILE,
              format);
    });
    chooser.showOpenDialog(this);
  }

  /**
   * Shows a dialog for input of a URL at which to retrieve alignment data
   * 
   * @param viewport
   */
  @Override
  public void inputURLMenuItem_actionPerformed(AlignViewport viewport)
  {
    // This construct allows us to have a wider textfield
    // for viewing
    JLabel label = new JLabel(
            MessageManager.getString("label.input_file_url"));

    JPanel panel = new JPanel(new GridLayout(2, 1));
    panel.add(label);

    /*
     * the URL to fetch is input in Java: an editable combobox with history JS:
     * (pending JAL-3038) a plain text field
     */
    JComponent history;
    String urlBase = "https://www.";
    if (Platform.isJS())
    {
      history = new JTextField(urlBase, 35);
    }
    else
    /**
     * Java only
     * 
     * @j2sIgnore
     */
    {
      JComboBox<String> asCombo = new JComboBox<>();
      asCombo.setPreferredSize(new Dimension(400, 20));
      asCombo.setEditable(true);
      asCombo.addItem(urlBase);
      String historyItems = Cache.getProperty("RECENT_URL");
      if (historyItems != null)
      {
        for (String token : historyItems.split("\\t"))
        {
          asCombo.addItem(token);
        }
      }
      history = asCombo;
    }
    panel.add(history);

    Object[] options = new Object[] { MessageManager.getString("action.ok"),
        MessageManager.getString("action.cancel") };
    Runnable action = () -> {
      @SuppressWarnings("unchecked")
      String url = (history instanceof JTextField
              ? ((JTextField) history).getText()
              : ((JComboBox<String>) history).getEditor().getItem()
                      .toString().trim());

      if (url.toLowerCase(Locale.ROOT).endsWith(".jar"))
      {
        if (viewport != null)
        {
          new FileLoader().LoadFile(viewport, url, DataSourceType.URL,
                  FileFormat.Jalview);
        }
        else
        {
          new FileLoader().LoadFile(url, DataSourceType.URL,
                  FileFormat.Jalview);
        }
      }
      else
      {
        FileFormatI format = null;
        try
        {
          format = new IdentifyFile().identify(url, DataSourceType.URL);
        } catch (FileFormatException e)
        {
          // TODO revise error handling, distinguish between
          // URL not found and response not valid
        }

        if (format == null)
        {
          String msg = MessageManager.formatMessage("label.couldnt_locate",
                  url);
          JvOptionPane.showInternalMessageDialog(Desktop.desktop, msg,
                  MessageManager.getString("label.url_not_found"),
                  JvOptionPane.WARNING_MESSAGE);
          return;
        }

        if (viewport != null)
        {
          new FileLoader().LoadFile(viewport, url, DataSourceType.URL,
                  format);
        }
        else
        {
          new FileLoader().LoadFile(url, DataSourceType.URL, format);
        }
      }
    };
    String dialogOption = MessageManager
            .getString("label.input_alignment_from_url");
    JvOptionPane.newOptionDialog(desktop).setResponseHandler(0, action)
            .showInternalDialog(panel, dialogOption,
                    JvOptionPane.YES_NO_CANCEL_OPTION,
                    JvOptionPane.PLAIN_MESSAGE, null, options,
                    MessageManager.getString("action.ok"));
  }

  /**
   * Opens the CutAndPaste window for the user to paste an alignment in to
   * 
   * @param viewPanel
   *          - if not null, the pasted alignment is added to the current
   *          alignment; if null, to a new alignment window
   */
  @Override
  public void inputTextboxMenuItem_actionPerformed(
          AlignmentViewPanel viewPanel)
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer();
    cap.setForInput(viewPanel);
    Desktop.addInternalFrame(cap,
            MessageManager.getString("label.cut_paste_alignmen_file"), true,
            600, 500);
  }

  /*
   * Check with user and saving files before actually quitting
   */
  public void desktopQuit()
  {
    desktopQuit(true, false);
  }

  public QuitHandler.QResponse desktopQuit(boolean ui, boolean disposeFlag)
  {
    final Runnable doDesktopQuit = () -> {
      Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
      Cache.setProperty("SCREENGEOMETRY_WIDTH", screen.width + "");
      Cache.setProperty("SCREENGEOMETRY_HEIGHT", screen.height + "");
      storeLastKnownDimensions("", new Rectangle(getBounds().x,
              getBounds().y, getWidth(), getHeight()));

      if (jconsole != null)
      {
        storeLastKnownDimensions("JAVA_CONSOLE_", jconsole.getBounds());
        jconsole.stopConsole();
      }

      if (jvnews != null)
      {
        storeLastKnownDimensions("JALVIEW_RSS_WINDOW_", jvnews.getBounds());
      }

      // Frames should all close automatically. Keeping external
      // viewers open should already be decided by user.
      closeAll_actionPerformed(null);

      // check for aborted quit
      if (QuitHandler.quitCancelled())
      {
        jalview.bin.Console.debug("Desktop aborting quit");
        return;
      }

      if (dialogExecutor != null)
      {
        dialogExecutor.shutdownNow();
      }

      if (groovyConsole != null)
      {
        // suppress a possible repeat prompt to save script
        groovyConsole.setDirty(false);
        groovyConsole.exit();
      }

      if (QuitHandler.gotQuitResponse() == QResponse.FORCE_QUIT)
      {
        // note that shutdown hook will not be run
        jalview.bin.Console.debug("Force Quit selected by user");
        Runtime.getRuntime().halt(0);
      }

      jalview.bin.Console.debug("Quit selected by user");
      if (disposeFlag)
      {
        instance.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // instance.dispose();
      }
      instance.quit();
    };

    return QuitHandler.getQuitResponse(ui, doDesktopQuit, doDesktopQuit,
            QuitHandler.defaultCancelQuit);
  }

  /**
   * Don't call this directly, use desktopQuit() above. Exits the program.
   */
  @Override
  public void quit()
  {
    // this will run the shutdownHook but QuitHandler.getQuitResponse() should
    // not run a second time if gotQuitResponse flag has been set (i.e. user
    // confirmed quit of some kind).
    Jalview.exit("Desktop exiting.", 0);
  }

  private void storeLastKnownDimensions(String string, Rectangle jc)
  {
    jalview.bin.Console.debug("Storing last known dimensions for " + string
            + ": x:" + jc.x + " y:" + jc.y + " width:" + jc.width
            + " height:" + jc.height);

    Cache.setProperty(string + "SCREEN_X", jc.x + "");
    Cache.setProperty(string + "SCREEN_Y", jc.y + "");
    Cache.setProperty(string + "SCREEN_WIDTH", jc.width + "");
    Cache.setProperty(string + "SCREEN_HEIGHT", jc.height + "");
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void aboutMenuItem_actionPerformed(ActionEvent e)
  {
    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        new SplashScreen(false);
      }
    }).start();
  }

  /**
   * Returns the html text for the About screen, including any available version
   * number, build details, author details and citation reference, but without
   * the enclosing {@code html} tags
   * 
   * @return
   */
  public String getAboutMessage()
  {
    StringBuilder message = new StringBuilder(1024);
    message.append("<div style=\"font-family: sans-serif;\">")
            .append("<h1><strong>Version: ")
            .append(Cache.getProperty("VERSION")).append("</strong></h1>")
            .append("<strong>Built: <em>")
            .append(Cache.getDefault("BUILD_DATE", "unknown"))
            .append("</em> from ").append(Cache.getBuildDetailsForSplash())
            .append("</strong>");

    String latestVersion = Cache.getDefault("LATEST_VERSION", "Checking");
    if (latestVersion.equals("Checking"))
    {
      // JBP removed this message for 2.11: May be reinstated in future version
      // message.append("<br>...Checking latest version...</br>");
    }
    else if (!latestVersion.equals(Cache.getProperty("VERSION")))
    {
      boolean red = false;
      if (Cache.getProperty("VERSION").toLowerCase(Locale.ROOT)
              .indexOf("automated build") == -1)
      {
        red = true;
        // Displayed when code version and jnlp version do not match and code
        // version is not a development build
        message.append("<div style=\"color: #FF0000;font-style: bold;\">");
      }

      message.append("<br>!! Version ")
              .append(Cache.getDefault("LATEST_VERSION", "..Checking.."))
              .append(" is available for download from ")
              .append(Cache.getDefault("www.jalview.org",
                      "https://www.jalview.org"))
              .append(" !!");
      if (red)
      {
        message.append("</div>");
      }
    }
    message.append("<br>Authors:  ");
    message.append(Cache.getDefault("AUTHORFNAMES", DEFAULT_AUTHORS));
    message.append(CITATION);

    message.append("</div>");

    return message.toString();
  }

  /**
   * Action on requesting Help documentation
   */
  @Override
  public void documentationMenuItem_actionPerformed()
  {
    try
    {
      if (Platform.isJS())
      {
        BrowserLauncher.openURL("https://www.jalview.org/help.html");
      }
      else
      /**
       * Java only
       * 
       * @j2sIgnore
       */
      {
        Help.showHelpWindow();
      }
    } catch (Exception ex)
    {
      System.err.println("Error opening help: " + ex.getMessage());
    }
  }

  @Override
  public void closeAll_actionPerformed(ActionEvent e)
  {
    // TODO show a progress bar while closing?
    JInternalFrame[] frames = desktop.getAllFrames();
    for (int i = 0; i < frames.length; i++)
    {
      try
      {
        frames[i].setClosed(true);
      } catch (java.beans.PropertyVetoException ex)
      {
      }
    }
    Jalview.setCurrentAlignFrame(null);
    jalview.bin.Console.info("ALL CLOSED");

    /*
     * reset state of singleton objects as appropriate (clear down session state
     * when all windows are closed)
     */
    StructureSelectionManager ssm = StructureSelectionManager
            .getStructureSelectionManager(this);
    if (ssm != null)
    {
      ssm.resetAll();
    }
  }

  public int structureViewersStillRunningCount()
  {
    int count = 0;
    JInternalFrame[] frames = desktop.getAllFrames();
    for (int i = 0; i < frames.length; i++)
    {
      if (frames[i] != null
              && frames[i] instanceof JalviewStructureDisplayI)
      {
        if (((JalviewStructureDisplayI) frames[i]).stillRunning())
          count++;
      }
    }
    return count;
  }

  @Override
  public void raiseRelated_actionPerformed(ActionEvent e)
  {
    reorderAssociatedWindows(false, false);
  }

  @Override
  public void minimizeAssociated_actionPerformed(ActionEvent e)
  {
    reorderAssociatedWindows(true, false);
  }

  void closeAssociatedWindows()
  {
    reorderAssociatedWindows(false, true);
  }

  /*
   * (non-Javadoc)
   * 
   * @seejalview.jbgui.GDesktop#garbageCollect_actionPerformed(java.awt.event.
   * ActionEvent)
   */
  @Override
  protected void garbageCollect_actionPerformed(ActionEvent e)
  {
    // We simply collect the garbage
    jalview.bin.Console.debug("Collecting garbage...");
    System.gc();
    jalview.bin.Console.debug("Finished garbage collection.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.jbgui.GDesktop#showMemusage_actionPerformed(java.awt.event.
   * ActionEvent )
   */
  @Override
  protected void showMemusage_actionPerformed(ActionEvent e)
  {
    desktop.showMemoryUsage(showMemusage.isSelected());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.jbgui.GDesktop#showConsole_actionPerformed(java.awt.event.ActionEvent
   * )
   */
  @Override
  protected void showConsole_actionPerformed(ActionEvent e)
  {
    showConsole(showConsole.isSelected());
  }

  Console jconsole = null;

  /**
   * control whether the java console is visible or not
   * 
   * @param selected
   */
  void showConsole(boolean selected)
  {
    // TODO: decide if we should update properties file
    if (jconsole != null) // BH 2018
    {
      showConsole.setSelected(selected);
      Cache.setProperty("SHOW_JAVA_CONSOLE",
              Boolean.valueOf(selected).toString());
      jconsole.setVisible(selected);
    }
  }

  void reorderAssociatedWindows(boolean minimize, boolean close)
  {
    JInternalFrame[] frames = desktop.getAllFrames();
    if (frames == null || frames.length < 1)
    {
      return;
    }

    AlignmentViewport source = null, target = null;
    if (frames[0] instanceof AlignFrame)
    {
      source = ((AlignFrame) frames[0]).getCurrentView();
    }
    else if (frames[0] instanceof TreePanel)
    {
      source = ((TreePanel) frames[0]).getViewPort();
    }
    else if (frames[0] instanceof PCAPanel)
    {
      source = ((PCAPanel) frames[0]).av;
    }
    else if (frames[0].getContentPane() instanceof PairwiseAlignPanel)
    {
      source = ((PairwiseAlignPanel) frames[0].getContentPane()).av;
    }

    if (source != null)
    {
      for (int i = 0; i < frames.length; i++)
      {
        target = null;
        if (frames[i] == null)
        {
          continue;
        }
        if (frames[i] instanceof AlignFrame)
        {
          target = ((AlignFrame) frames[i]).getCurrentView();
        }
        else if (frames[i] instanceof TreePanel)
        {
          target = ((TreePanel) frames[i]).getViewPort();
        }
        else if (frames[i] instanceof PCAPanel)
        {
          target = ((PCAPanel) frames[i]).av;
        }
        else if (frames[i].getContentPane() instanceof PairwiseAlignPanel)
        {
          target = ((PairwiseAlignPanel) frames[i].getContentPane()).av;
        }

        if (source == target)
        {
          try
          {
            if (close)
            {
              frames[i].setClosed(true);
            }
            else
            {
              frames[i].setIcon(minimize);
              if (!minimize)
              {
                frames[i].toFront();
              }
            }

          } catch (java.beans.PropertyVetoException ex)
          {
          }
        }
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  protected void preferences_actionPerformed(ActionEvent e)
  {
    Preferences.openPreferences();
  }

  @Override
  public void analyseFreqs_actionPerformed(ActionEvent e) throws ClassNotFoundException, IOException
  {
    new Analysis(null, 1, false).prepare();
  }
  
  @Override
  public void deleteRefs_actionPerformed(ActionEvent e) throws ClassNotFoundException, IOException
  {
    File f = new File(String.format("%s%s", EpReferenceFile.REFERENCE_PATH, EpReferenceFile.selectReference("label.choose_reference_to_delete")));
    f.delete();
  }

  /**
   * Prompts the user to choose a file and then saves the Jalview state as a
   * Jalview project file
   */
  @Override
  public void saveState_actionPerformed()
  {
    saveState_actionPerformed(false);
  }

  public void saveState_actionPerformed(boolean saveAs)
  {
    java.io.File projectFile = getProjectFile();
    // autoSave indicates we already have a file and don't need to ask
    boolean autoSave = projectFile != null && !saveAs
            && BackupFiles.getEnabled();

    // System.out.println("autoSave="+autoSave+", projectFile='"+projectFile+"',
    // saveAs="+saveAs+", Backups
    // "+(BackupFiles.getEnabled()?"enabled":"disabled"));

    boolean approveSave = false;
    if (!autoSave)
    {
      JalviewFileChooser chooser = new JalviewFileChooser("jvp",
              "Jalview Project");

      chooser.setFileView(new JalviewFileView());
      chooser.setDialogTitle(MessageManager.getString("label.save_state"));

      int value = chooser.showSaveDialog(this);

      if (value == JalviewFileChooser.APPROVE_OPTION)
      {
        projectFile = chooser.getSelectedFile();
        setProjectFile(projectFile);
        approveSave = true;
      }
    }

    if (approveSave || autoSave)
    {
      final Desktop me = this;
      final java.io.File chosenFile = projectFile;
      new Thread(new Runnable()
      {
        @Override
        public void run()
        {
          // TODO: refactor to Jalview desktop session controller action.
          setProgressBar(MessageManager.formatMessage(
                  "label.saving_jalview_project", new Object[]
                  { chosenFile.getName() }), chosenFile.hashCode());
          Cache.setProperty("LAST_DIRECTORY", chosenFile.getParent());
          // TODO catch and handle errors for savestate
          // TODO prevent user from messing with the Desktop whilst we're saving
          try
          {
            boolean doBackup = BackupFiles.getEnabled();
            BackupFiles backupfiles = doBackup ? new BackupFiles(chosenFile)
                    : null;

            new Jalview2XML().saveState(
                    doBackup ? backupfiles.getTempFile() : chosenFile);

            if (doBackup)
            {
              backupfiles.setWriteSuccess(true);
              backupfiles.rollBackupsAndRenameTempFile();
            }
          } catch (OutOfMemoryError oom)
          {
            new OOMWarning("Whilst saving current state to "
                    + chosenFile.getName(), oom);
          } catch (Exception ex)
          {
            jalview.bin.Console.error("Problems whilst trying to save to "
                    + chosenFile.getName(), ex);
            JvOptionPane.showMessageDialog(me,
                    MessageManager.formatMessage(
                            "label.error_whilst_saving_current_state_to",
                            new Object[]
                            { chosenFile.getName() }),
                    MessageManager.getString("label.couldnt_save_project"),
                    JvOptionPane.WARNING_MESSAGE);
          }
          setProgressBar(null, chosenFile.hashCode());
        }
      }).start();
    }
  }

  @Override
  public void saveAsState_actionPerformed(ActionEvent e)
  {
    saveState_actionPerformed(true);
  }

  protected void setProjectFile(File choice)
  {
    this.projectFile = choice;
  }

  public File getProjectFile()
  {
    return this.projectFile;
  }

  /**
   * Shows a file chooser dialog and tries to read in the selected file as a
   * Jalview project
   */
  @Override
  public void loadState_actionPerformed()
  {
    final String[] suffix = new String[] { "jvp", "jar" };
    final String[] desc = new String[] { "Jalview Project",
        "Jalview Project (old)" };
    JalviewFileChooser chooser = new JalviewFileChooser(
            Cache.getProperty("LAST_DIRECTORY"), suffix, desc,
            "Jalview Project", true, BackupFiles.getEnabled()); // last two
                                                                // booleans:
                                                                // allFiles,
    // allowBackupFiles
    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(MessageManager.getString("label.restore_state"));
    chooser.setResponseHandler(0, () -> {
      File selectedFile = chooser.getSelectedFile();
      setProjectFile(selectedFile);
      String choice = selectedFile.getAbsolutePath();
      Cache.setProperty("LAST_DIRECTORY", selectedFile.getParent());
      new Thread(new Runnable()
      {
        @Override
        public void run()
        {
          try
          {
            new Jalview2XML().loadJalviewAlign(selectedFile);
          } catch (OutOfMemoryError oom)
          {
            new OOMWarning("Whilst loading project from " + choice, oom);
          } catch (Exception ex)
          {
            jalview.bin.Console.error(
                    "Problems whilst loading project from " + choice, ex);
            JvOptionPane.showMessageDialog(Desktop.desktop,
                    MessageManager.formatMessage(
                            "label.error_whilst_loading_project_from",
                            new Object[]
                            { choice }),
                    MessageManager.getString("label.couldnt_load_project"),
                    JvOptionPane.WARNING_MESSAGE);
          }
        }
      }, "Project Loader").start();
    });

    chooser.showOpenDialog(this);
  }

  @Override
  public void inputSequence_actionPerformed(ActionEvent e)
  {
    new SequenceFetcher(this);
  }

  JPanel progressPanel;

  ArrayList<JPanel> fileLoadingPanels = new ArrayList<>();

  public void startLoading(final Object fileName)
  {
    if (fileLoadingCount == 0)
    {
      fileLoadingPanels.add(addProgressPanel(MessageManager
              .formatMessage("label.loading_file", new Object[]
              { fileName })));
    }
    fileLoadingCount++;
  }

  private JPanel addProgressPanel(String string)
  {
    if (progressPanel == null)
    {
      progressPanel = new JPanel(new GridLayout(1, 1));
      totalProgressCount = 0;
      instance.getContentPane().add(progressPanel, BorderLayout.SOUTH);
    }
    JPanel thisprogress = new JPanel(new BorderLayout(10, 5));
    JProgressBar progressBar = new JProgressBar();
    progressBar.setIndeterminate(true);

    thisprogress.add(new JLabel(string), BorderLayout.WEST);

    thisprogress.add(progressBar, BorderLayout.CENTER);
    progressPanel.add(thisprogress);
    ((GridLayout) progressPanel.getLayout()).setRows(
            ((GridLayout) progressPanel.getLayout()).getRows() + 1);
    ++totalProgressCount;
    instance.validate();
    return thisprogress;
  }

  int totalProgressCount = 0;

  private void removeProgressPanel(JPanel progbar)
  {
    if (progressPanel != null)
    {
      synchronized (progressPanel)
      {
        progressPanel.remove(progbar);
        GridLayout gl = (GridLayout) progressPanel.getLayout();
        gl.setRows(gl.getRows() - 1);
        if (--totalProgressCount < 1)
        {
          this.getContentPane().remove(progressPanel);
          progressPanel = null;
        }
      }
    }
    validate();
  }

  public void stopLoading()
  {
    fileLoadingCount--;
    if (fileLoadingCount < 1)
    {
      while (fileLoadingPanels.size() > 0)
      {
        removeProgressPanel(fileLoadingPanels.remove(0));
      }
      fileLoadingPanels.clear();
      fileLoadingCount = 0;
    }
    validate();
  }

  public static int getViewCount(String alignmentId)
  {
    AlignmentViewport[] aps = getViewports(alignmentId);
    return (aps == null) ? 0 : aps.length;
  }

  /**
   * 
   * @param alignmentId
   *          - if null, all sets are returned
   * @return all AlignmentPanels concerning the alignmentId sequence set
   */
  public static AlignmentPanel[] getAlignmentPanels(String alignmentId)
  {
    if (Desktop.desktop == null)
    {
      // no frames created and in headless mode
      // TODO: verify that frames are recoverable when in headless mode
      return null;
    }
    List<AlignmentPanel> aps = new ArrayList<>();
    AlignFrame[] frames = getAlignFrames();
    if (frames == null)
    {
      return null;
    }
    for (AlignFrame af : frames)
    {
      for (AlignmentPanel ap : af.alignPanels)
      {
        if (alignmentId == null
                || alignmentId.equals(ap.av.getSequenceSetId()))
        {
          aps.add(ap);
        }
      }
    }
    if (aps.size() == 0)
    {
      return null;
    }
    AlignmentPanel[] vap = aps.toArray(new AlignmentPanel[aps.size()]);
    return vap;
  }

  /**
   * get all the viewports on an alignment.
   * 
   * @param sequenceSetId
   *          unique alignment id (may be null - all viewports returned in that
   *          case)
   * @return all viewports on the alignment bound to sequenceSetId
   */
  public static AlignmentViewport[] getViewports(String sequenceSetId)
  {
    List<AlignmentViewport> viewp = new ArrayList<>();
    if (desktop != null)
    {
      AlignFrame[] frames = Desktop.getAlignFrames();

      for (AlignFrame afr : frames)
      {
        if (sequenceSetId == null || afr.getViewport().getSequenceSetId()
                .equals(sequenceSetId))
        {
          if (afr.alignPanels != null)
          {
            for (AlignmentPanel ap : afr.alignPanels)
            {
              if (sequenceSetId == null
                      || sequenceSetId.equals(ap.av.getSequenceSetId()))
              {
                viewp.add(ap.av);
              }
            }
          }
          else
          {
            viewp.add(afr.getViewport());
          }
        }
      }
      if (viewp.size() > 0)
      {
        return viewp.toArray(new AlignmentViewport[viewp.size()]);
      }
    }
    return null;
  }

  /**
   * Explode the views in the given frame into separate AlignFrame
   * 
   * @param af
   */
  public static void explodeViews(AlignFrame af)
  {
    int size = af.alignPanels.size();
    if (size < 2)
    {
      return;
    }

    // FIXME: ideally should use UI interface API
    FeatureSettings viewFeatureSettings = (af.featureSettings != null
            && af.featureSettings.isOpen()) ? af.featureSettings : null;
    Rectangle fsBounds = af.getFeatureSettingsGeometry();
    for (int i = 0; i < size; i++)
    {
      AlignmentPanel ap = af.alignPanels.get(i);

      AlignFrame newaf = new AlignFrame(ap);

      // transfer reference for existing feature settings to new alignFrame
      if (ap == af.alignPanel)
      {
        if (viewFeatureSettings != null && viewFeatureSettings.fr.ap == ap)
        {
          newaf.featureSettings = viewFeatureSettings;
        }
        newaf.setFeatureSettingsGeometry(fsBounds);
      }

      /*
       * Restore the view's last exploded frame geometry if known. Multiple views from
       * one exploded frame share and restore the same (frame) position and size.
       */
      Rectangle geometry = ap.av.getExplodedGeometry();
      if (geometry != null)
      {
        newaf.setBounds(geometry);
      }

      ap.av.setGatherViewsHere(false);

      addInternalFrame(newaf, af.getTitle(), AlignFrame.DEFAULT_WIDTH,
              AlignFrame.DEFAULT_HEIGHT);
      // and materialise a new feature settings dialog instance for the new
      // alignframe
      // (closes the old as if 'OK' was pressed)
      if (ap == af.alignPanel && newaf.featureSettings != null
              && newaf.featureSettings.isOpen()
              && af.alignPanel.getAlignViewport().isShowSequenceFeatures())
      {
        newaf.showFeatureSettingsUI();
      }
    }

    af.featureSettings = null;
    af.alignPanels.clear();
    af.closeMenuItem_actionPerformed(true);

  }

  /**
   * Gather expanded views (separate AlignFrame's) with the same sequence set
   * identifier back in to this frame as additional views, and close the
   * expanded views. Note the expanded frames may themselves have multiple
   * views. We take the lot.
   * 
   * @param source
   */
  public void gatherViews(AlignFrame source)
  {
    source.viewport.setGatherViewsHere(true);
    source.viewport.setExplodedGeometry(source.getBounds());
    JInternalFrame[] frames = desktop.getAllFrames();
    String viewId = source.viewport.getSequenceSetId();
    for (int t = 0; t < frames.length; t++)
    {
      if (frames[t] instanceof AlignFrame && frames[t] != source)
      {
        AlignFrame af = (AlignFrame) frames[t];
        boolean gatherThis = false;
        for (int a = 0; a < af.alignPanels.size(); a++)
        {
          AlignmentPanel ap = af.alignPanels.get(a);
          if (viewId.equals(ap.av.getSequenceSetId()))
          {
            gatherThis = true;
            ap.av.setGatherViewsHere(false);
            ap.av.setExplodedGeometry(af.getBounds());
            source.addAlignmentPanel(ap, false);
          }
        }

        if (gatherThis)
        {
          if (af.featureSettings != null && af.featureSettings.isOpen())
          {
            if (source.featureSettings == null)
            {
              // preserve the feature settings geometry for this frame
              source.featureSettings = af.featureSettings;
              source.setFeatureSettingsGeometry(
                      af.getFeatureSettingsGeometry());
            }
            else
            {
              // close it and forget
              af.featureSettings.close();
            }
          }
          af.alignPanels.clear();
          af.closeMenuItem_actionPerformed(true);
        }
      }
    }

    // refresh the feature setting UI for the source frame if it exists
    if (source.featureSettings != null && source.featureSettings.isOpen())
    {
      source.showFeatureSettingsUI();
    }

  }

  public JInternalFrame[] getAllFrames()
  {
    return desktop.getAllFrames();
  }

  /**
   * Checks the given url to see if it gives a response indicating that the user
   * should be informed of a new questionnaire.
   * 
   * @param url
   */
  public void checkForQuestionnaire(String url)
  {
    UserQuestionnaireCheck jvq = new UserQuestionnaireCheck(url);
    // javax.swing.SwingUtilities.invokeLater(jvq);
    new Thread(jvq).start();
  }

  public void checkURLLinks()
  {
    // Thread off the URL link checker
    addDialogThread(new Runnable()
    {
      @Override
      public void run()
      {
        if (Cache.getDefault("CHECKURLLINKS", true))
        {
          // check what the actual links are - if it's just the default don't
          // bother with the warning
          List<String> links = Preferences.sequenceUrlLinks
                  .getLinksForMenu();

          // only need to check links if there is one with a
          // SEQUENCE_ID which is not the default EMBL_EBI link
          ListIterator<String> li = links.listIterator();
          boolean check = false;
          List<JLabel> urls = new ArrayList<>();
          while (li.hasNext())
          {
            String link = li.next();
            if (link.contains(jalview.util.UrlConstants.SEQUENCE_ID)
                    && !UrlConstants.isDefaultString(link))
            {
              check = true;
              int barPos = link.indexOf("|");
              String urlMsg = barPos == -1 ? link
                      : link.substring(0, barPos) + ": "
                              + link.substring(barPos + 1);
              urls.add(new JLabel(urlMsg));
            }
          }
          if (!check)
          {
            return;
          }

          // ask user to check in case URL links use old style tokens
          // ($SEQUENCE_ID$ for sequence id _or_ accession id)
          JPanel msgPanel = new JPanel();
          msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.PAGE_AXIS));
          msgPanel.add(Box.createVerticalGlue());
          JLabel msg = new JLabel(MessageManager
                  .getString("label.SEQUENCE_ID_for_DB_ACCESSION1"));
          JLabel msg2 = new JLabel(MessageManager
                  .getString("label.SEQUENCE_ID_for_DB_ACCESSION2"));
          msgPanel.add(msg);
          for (JLabel url : urls)
          {
            msgPanel.add(url);
          }
          msgPanel.add(msg2);

          final JCheckBox jcb = new JCheckBox(
                  MessageManager.getString("label.do_not_display_again"));
          jcb.addActionListener(new ActionListener()
          {
            @Override
            public void actionPerformed(ActionEvent e)
            {
              // update Cache settings for "don't show this again"
              boolean showWarningAgain = !jcb.isSelected();
              Cache.setProperty("CHECKURLLINKS",
                      Boolean.valueOf(showWarningAgain).toString());
            }
          });
          msgPanel.add(jcb);

          JvOptionPane.showMessageDialog(Desktop.desktop, msgPanel,
                  MessageManager
                          .getString("label.SEQUENCE_ID_no_longer_used"),
                  JvOptionPane.WARNING_MESSAGE);
        }
      }
    });
  }

  /**
   * Proxy class for JDesktopPane which optionally displays the current memory
   * usage and highlights the desktop area with a red bar if free memory runs
   * low.
   * 
   * @author AMW
   */
  public class MyDesktopPane extends JDesktopPane implements Runnable
  {
    private static final float ONE_MB = 1048576f;

    boolean showMemoryUsage = false;

    Runtime runtime;

    java.text.NumberFormat df;

    float maxMemory, allocatedMemory, freeMemory, totalFreeMemory,
            percentUsage;

    public MyDesktopPane(boolean showMemoryUsage)
    {
      showMemoryUsage(showMemoryUsage);
    }

    public void showMemoryUsage(boolean showMemory)
    {
      this.showMemoryUsage = showMemory;
      if (showMemory)
      {
        Thread worker = new Thread(this);
        worker.start();
      }
      repaint();
    }

    public boolean isShowMemoryUsage()
    {
      return showMemoryUsage;
    }

    @Override
    public void run()
    {
      df = java.text.NumberFormat.getNumberInstance();
      df.setMaximumFractionDigits(2);
      runtime = Runtime.getRuntime();

      while (showMemoryUsage)
      {
        try
        {
          maxMemory = runtime.maxMemory() / ONE_MB;
          allocatedMemory = runtime.totalMemory() / ONE_MB;
          freeMemory = runtime.freeMemory() / ONE_MB;
          totalFreeMemory = freeMemory + (maxMemory - allocatedMemory);

          percentUsage = (totalFreeMemory / maxMemory) * 100;

          // if (percentUsage < 20)
          {
            // border1 = BorderFactory.createMatteBorder(12, 12, 12, 12,
            // Color.red);
            // instance.set.setBorder(border1);
          }
          repaint();
          // sleep after showing usage
          Thread.sleep(3000);
        } catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
    }

    @Override
    public void paintComponent(Graphics g)
    {
      if (showMemoryUsage && g != null && df != null)
      {
        if (percentUsage < 20)
        {
          g.setColor(Color.red);
        }
        FontMetrics fm = g.getFontMetrics();
        if (fm != null)
        {
          g.drawString(MessageManager.formatMessage("label.memory_stats",
                  new Object[]
                  { df.format(totalFreeMemory), df.format(maxMemory),
                      df.format(percentUsage) }),
                  10, getHeight() - fm.getHeight());
        }
      }

      // output debug scale message. Important for jalview.bin.HiDPISettingTest2
      Desktop.debugScaleMessage(Desktop.getDesktop().getGraphics());
    }
  }

  /**
   * Accessor method to quickly get all the AlignmentFrames loaded.
   * 
   * @return an array of AlignFrame, or null if none found
   */
  public static AlignFrame[] getAlignFrames()
  {
    if (Jalview.isHeadlessMode())
    {
      // Desktop.desktop is null in headless mode
      return new AlignFrame[] { Jalview.currentAlignFrame };
    }

    JInternalFrame[] frames = Desktop.desktop.getAllFrames();

    if (frames == null)
    {
      return null;
    }
    List<AlignFrame> avp = new ArrayList<>();
    // REVERSE ORDER
    for (int i = frames.length - 1; i > -1; i--)
    {
      if (frames[i] instanceof AlignFrame)
      {
        avp.add((AlignFrame) frames[i]);
      }
      else if (frames[i] instanceof SplitFrame)
      {
        /*
         * Also check for a split frame containing an AlignFrame
         */
        GSplitFrame sf = (GSplitFrame) frames[i];
        if (sf.getTopFrame() instanceof AlignFrame)
        {
          avp.add((AlignFrame) sf.getTopFrame());
        }
        if (sf.getBottomFrame() instanceof AlignFrame)
        {
          avp.add((AlignFrame) sf.getBottomFrame());
        }
      }
    }
    if (avp.size() == 0)
    {
      return null;
    }
    AlignFrame afs[] = avp.toArray(new AlignFrame[avp.size()]);
    return afs;
  }

  /**
   * Returns an array of any AppJmol frames in the Desktop (or null if none).
   * 
   * @return
   */
  public GStructureViewer[] getJmols()
  {
    JInternalFrame[] frames = Desktop.desktop.getAllFrames();

    if (frames == null)
    {
      return null;
    }
    List<GStructureViewer> avp = new ArrayList<>();
    // REVERSE ORDER
    for (int i = frames.length - 1; i > -1; i--)
    {
      if (frames[i] instanceof AppJmol)
      {
        GStructureViewer af = (GStructureViewer) frames[i];
        avp.add(af);
      }
    }
    if (avp.size() == 0)
    {
      return null;
    }
    GStructureViewer afs[] = avp.toArray(new GStructureViewer[avp.size()]);
    return afs;
  }

  /**
   * Add Groovy Support to Jalview
   */
  @Override
  public void groovyShell_actionPerformed()
  {
    try
    {
      openGroovyConsole();
    } catch (Exception ex)
    {
      jalview.bin.Console.error("Groovy Shell Creation failed.", ex);
      JvOptionPane.showInternalMessageDialog(Desktop.desktop,

              MessageManager.getString("label.couldnt_create_groovy_shell"),
              MessageManager.getString("label.groovy_support_failed"),
              JvOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Open the Groovy console
   */
  void openGroovyConsole()
  {
    if (groovyConsole == null)
    {
      groovyConsole = new groovy.ui.Console();
      groovyConsole.setVariable("Jalview", this);
      groovyConsole.run();

      /*
       * We allow only one console at a time, so that AlignFrame menu option
       * 'Calculate | Run Groovy script' is unambiguous. Disable 'Groovy Console', and
       * enable 'Run script', when the console is opened, and the reverse when it is
       * closed
       */
      Window window = (Window) groovyConsole.getFrame();
      window.addWindowListener(new WindowAdapter()
      {
        @Override
        public void windowClosed(WindowEvent e)
        {
          /*
           * rebind CMD-Q from Groovy Console to Jalview Quit
           */
          addQuitHandler();
          enableExecuteGroovy(false);
        }
      });
    }

    /*
     * show Groovy console window (after close and reopen)
     */
    ((Window) groovyConsole.getFrame()).setVisible(true);

    /*
     * if we got this far, enable 'Run Groovy' in AlignFrame menus and disable
     * opening a second console
     */
    enableExecuteGroovy(true);
  }

  /**
   * Bind Ctrl/Cmd-Q to Quit - for reset as Groovy Console takes over this
   * binding when opened
   */
  protected void addQuitHandler()
  {
    getRootPane()
            .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke
                            .getKeyStroke(KeyEvent.VK_Q,
                                    jalview.util.ShortcutKeyMaskExWrapper
                                            .getMenuShortcutKeyMaskEx()),
                    "Quit");
    getRootPane().getActionMap().put("Quit", new AbstractAction()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        desktopQuit();
      }
    });
  }

  /**
   * Enable or disable 'Run Groovy script' in AlignFrame calculate menus
   * 
   * @param enabled
   *          true if Groovy console is open
   */
  public void enableExecuteGroovy(boolean enabled)
  {
    /*
     * disable opening a second Groovy console (or re-enable when the console is
     * closed)
     */
    groovyShell.setEnabled(!enabled);

    AlignFrame[] alignFrames = getAlignFrames();
    if (alignFrames != null)
    {
      for (AlignFrame af : alignFrames)
      {
        af.setGroovyEnabled(enabled);
      }
    }
  }

  /**
   * Progress bars managed by the IProgressIndicator method.
   */
  private Hashtable<Long, JPanel> progressBars;

  private Hashtable<Long, IProgressIndicatorHandler> progressBarHandlers;

  /*
   * (non-Javadoc)
   * 
   * @see jalview.gui.IProgressIndicator#setProgressBar(java.lang.String, long)
   */
  @Override
  public void setProgressBar(String message, long id)
  {
    if (progressBars == null)
    {
      progressBars = new Hashtable<>();
      progressBarHandlers = new Hashtable<>();
    }

    if (progressBars.get(Long.valueOf(id)) != null)
    {
      JPanel panel = progressBars.remove(Long.valueOf(id));
      if (progressBarHandlers.contains(Long.valueOf(id)))
      {
        progressBarHandlers.remove(Long.valueOf(id));
      }
      removeProgressPanel(panel);
    }
    else
    {
      progressBars.put(Long.valueOf(id), addProgressPanel(message));
    }
  }
  
  @Override
  public JProgressBar getProgressBar(long id)
  {
    if (progressBars == null)
      return null;
    
    if (progressBars.get(Long.valueOf(id)) == null)
      return null;
    
    for (Component c : progressBars.get(Long.valueOf(id)).getComponents())
    {
      if (c.getClass() == JProgressBar.class)
        return (JProgressBar) c;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.gui.IProgressIndicator#registerHandler(long,
   * jalview.gui.IProgressIndicatorHandler)
   */
  @Override
  public void registerHandler(final long id,
          final IProgressIndicatorHandler handler)
  {
    if (progressBarHandlers == null
            || !progressBars.containsKey(Long.valueOf(id)))
    {
      throw new Error(MessageManager.getString(
              "error.call_setprogressbar_before_registering_handler"));
    }
    progressBarHandlers.put(Long.valueOf(id), handler);
    final JPanel progressPanel = progressBars.get(Long.valueOf(id));
    if (handler.canCancel())
    {
      JButton cancel = new JButton(
              MessageManager.getString("action.cancel"));
      final IProgressIndicator us = this;
      cancel.addActionListener(new ActionListener()
      {

        @Override
        public void actionPerformed(ActionEvent e)
        {
          handler.cancelActivity(id);
          us.setProgressBar(MessageManager
                  .formatMessage("label.cancelled_params", new Object[]
                  { ((JLabel) progressPanel.getComponent(0)).getText() }),
                  id);
        }
      });
      progressPanel.add(cancel, BorderLayout.EAST);
    }
  }

  /**
   * 
   * @return true if any progress bars are still active
   */
  @Override
  public boolean operationInProgress()
  {
    if (progressBars != null && progressBars.size() > 0)
    {
      return true;
    }
    return false;
  }

  /**
   * This will return the first AlignFrame holding the given viewport instance.
   * It will break if there are more than one AlignFrames viewing a particular
   * av.
   * 
   * @param viewport
   * @return alignFrame for viewport
   */
  public static AlignFrame getAlignFrameFor(AlignViewportI viewport)
  {
    if (desktop != null)
    {
      AlignmentPanel[] aps = getAlignmentPanels(
              viewport.getSequenceSetId());
      for (int panel = 0; aps != null && panel < aps.length; panel++)
      {
        if (aps[panel] != null && aps[panel].av == viewport)
        {
          return aps[panel].alignFrame;
        }
      }
    }
    return null;
  }

  public VamsasApplication getVamsasApplication()
  {
    // TODO: JAL-3311 remove remaining code from Jalview relating to VAMSAS
    return null;

  }

  /**
   * flag set if jalview GUI is being operated programmatically
   */
  private boolean inBatchMode = false;

  /**
   * check if jalview GUI is being operated programmatically
   * 
   * @return inBatchMode
   */
  public boolean isInBatchMode()
  {
    return inBatchMode;
  }

  /**
   * set flag if jalview GUI is being operated programmatically
   * 
   * @param inBatchMode
   */
  public void setInBatchMode(boolean inBatchMode)
  {
    this.inBatchMode = inBatchMode;
  }

  /**
   * start service discovery and wait till it is done
   */
  public void startServiceDiscovery()
  {
    startServiceDiscovery(false);
  }

  /**
   * start service discovery threads - blocking or non-blocking
   * 
   * @param blocking
   */
  public void startServiceDiscovery(boolean blocking)
  {
    startServiceDiscovery(blocking, false);
  }

  /**
   * start service discovery threads
   * 
   * @param blocking
   *          - false means call returns immediately
   * @param ignore_SHOW_JWS2_SERVICES_preference
   *          - when true JABA services are discovered regardless of user's JWS2
   *          discovery preference setting
   */
  public void startServiceDiscovery(boolean blocking,
          boolean ignore_SHOW_JWS2_SERVICES_preference)
  {
    boolean alive = true;
    Thread t0 = null, t1 = null, t2 = null;
    // JAL-940 - JALVIEW 1 services are now being EOLed as of JABA 2.1 release
    if (true)
    {
      // todo: changesupport handlers need to be transferred
      if (discoverer == null)
      {
        discoverer = new jalview.ws.jws1.Discoverer();
        // register PCS handler for desktop.
        discoverer.addPropertyChangeListener(changeSupport);
      }
      // JAL-940 - disabled JWS1 service configuration - always start discoverer
      // until we phase out completely
      (t0 = new Thread(discoverer)).start();
    }

    if (ignore_SHOW_JWS2_SERVICES_preference
            || Cache.getDefault("SHOW_JWS2_SERVICES", true))
    {
      t2 = jalview.ws.jws2.Jws2Discoverer.getDiscoverer()
              .startDiscoverer(changeSupport);
    }
    Thread t3 = null;
    {
      // TODO: do rest service discovery
    }
    if (blocking)
    {
      while (alive)
      {
        try
        {
          Thread.sleep(15);
        } catch (Exception e)
        {
        }
        alive = (t1 != null && t1.isAlive()) || (t2 != null && t2.isAlive())
                || (t3 != null && t3.isAlive())
                || (t0 != null && t0.isAlive());
      }
    }
  }

  /**
   * called to check if the service discovery process completed successfully.
   * 
   * @param evt
   */
  protected void JalviewServicesChanged(PropertyChangeEvent evt)
  {
    if (evt.getNewValue() == null || evt.getNewValue() instanceof Vector)
    {
      final String ermsg = jalview.ws.jws2.Jws2Discoverer.getDiscoverer()
              .getErrorMessages();
      if (ermsg != null)
      {
        if (Cache.getDefault("SHOW_WSDISCOVERY_ERRORS", true))
        {
          if (serviceChangedDialog == null)
          {
            // only run if we aren't already displaying one of these.
            addDialogThread(serviceChangedDialog = new Runnable()
            {
              @Override
              public void run()
              {

                /*
                 * JalviewDialog jd =new JalviewDialog() {
                 * 
                 * @Override protected void cancelPressed() { // TODO Auto-generated method stub
                 * 
                 * }@Override protected void okPressed() { // TODO Auto-generated method stub
                 * 
                 * }@Override protected void raiseClosed() { // TODO Auto-generated method stub
                 * 
                 * } }; jd.initDialogFrame(new JLabel("<html><table width=\"450\"><tr><td>" +
                 * ermsg +
                 * "<br/>It may be that you have invalid JABA URLs in your web service preferences,"
                 * + " or mis-configured HTTP proxy settings.<br/>" +
                 * "Check the <em>Connections</em> and <em>Web services</em> tab of the" +
                 * " Tools->Preferences dialog box to change them.</td></tr></table></html>" ),
                 * true, true, "Web Service Configuration Problem", 450, 400);
                 * 
                 * jd.waitForInput();
                 */
                JvOptionPane.showConfirmDialog(Desktop.desktop,
                        new JLabel("<html><table width=\"450\"><tr><td>"
                                + ermsg + "</td></tr></table>"
                                + "<p>It may be that you have invalid JABA URLs<br/>in your web service preferences,"
                                + "<br>or as a command-line argument, or mis-configured HTTP proxy settings.</p>"
                                + "<p>Check the <em>Connections</em> and <em>Web services</em> tab<br/>of the"
                                + " Tools->Preferences dialog box to change them.</p></html>"),
                        "Web Service Configuration Problem",
                        JvOptionPane.DEFAULT_OPTION,
                        JvOptionPane.ERROR_MESSAGE);
                serviceChangedDialog = null;

              }
            });
          }
        }
        else
        {
          jalview.bin.Console.error(
                  "Errors reported by JABA discovery service. Check web services preferences.\n"
                          + ermsg);
        }
      }
    }
  }

  private Runnable serviceChangedDialog = null;

  /**
   * start a thread to open a URL in the configured browser. Pops up a warning
   * dialog to the user if there is an exception when calling out to the browser
   * to open the URL.
   * 
   * @param url
   */
  public static void showUrl(final String url)
  {
    showUrl(url, Desktop.instance);
  }

  /**
   * Like showUrl but allows progress handler to be specified
   * 
   * @param url
   * @param progress
   *          (null) or object implementing IProgressIndicator
   */
  public static void showUrl(final String url,
          final IProgressIndicator progress)
  {
    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          if (progress != null)
          {
            progress.setProgressBar(MessageManager
                    .formatMessage("status.opening_params", new Object[]
                    { url }), this.hashCode());
          }
          jalview.util.BrowserLauncher.openURL(url);
        } catch (Exception ex)
        {
          JvOptionPane.showInternalMessageDialog(Desktop.desktop,
                  MessageManager
                          .getString("label.web_browser_not_found_unix"),
                  MessageManager.getString("label.web_browser_not_found"),
                  JvOptionPane.WARNING_MESSAGE);

          ex.printStackTrace();
        }
        if (progress != null)
        {
          progress.setProgressBar(null, this.hashCode());
        }
      }
    }).start();
  }

  public static WsParamSetManager wsparamManager = null;

  public static ParamManager getUserParameterStore()
  {
    if (wsparamManager == null)
    {
      wsparamManager = new WsParamSetManager();
    }
    return wsparamManager;
  }

  /**
   * static hyperlink handler proxy method for use by Jalview's internal windows
   * 
   * @param e
   */
  public static void hyperlinkUpdate(HyperlinkEvent e)
  {
    if (e.getEventType() == EventType.ACTIVATED)
    {
      String url = null;
      try
      {
        url = e.getURL().toString();
        Desktop.showUrl(url);
      } catch (Exception x)
      {
        if (url != null)
        {
          jalview.bin.Console
                  .error("Couldn't handle string " + url + " as a URL.");
        }
        // ignore any exceptions due to dud links.
      }

    }
  }

  /**
   * single thread that handles display of dialogs to user.
   */
  ExecutorService dialogExecutor = Executors.newFixedThreadPool(3);

  /**
   * flag indicating if dialogExecutor should try to acquire a permit
   */
  private volatile boolean dialogPause = true;

  /**
   * pause the queue
   */
  private Semaphore block = new Semaphore(0);

  private static groovy.ui.Console groovyConsole;

  /**
   * add another dialog thread to the queue
   * 
   * @param prompter
   */
  public void addDialogThread(final Runnable prompter)
  {
    dialogExecutor.submit(new Runnable()
    {
      @Override
      public void run()
      {
        if (dialogPause)
        {
          acquireDialogQueue();
        }
        if (instance == null)
        {
          return;
        }
        try
        {
          SwingUtilities.invokeAndWait(prompter);
        } catch (Exception q)
        {
          jalview.bin.Console.warn("Unexpected Exception in dialog thread.",
                  q);
        }
      }
    });
  }

  private boolean dialogQueueStarted = false;

  public void startDialogQueue()
  {
    if (dialogQueueStarted)
    {
      return;
    }
    // set the flag so we don't pause waiting for another permit and semaphore
    // the current task to begin
    releaseDialogQueue();
    dialogQueueStarted = true;
  }

  public void acquireDialogQueue()
  {
    try
    {
      block.acquire();
      dialogPause = true;
    } catch (InterruptedException e)
    {
      jalview.bin.Console.debug("Interruption when acquiring DialogueQueue",
              e);
    }
  }

  public void releaseDialogQueue()
  {
    if (!dialogPause)
    {
      return;
    }
    block.release();
    dialogPause = false;
  }

  /**
   * Outputs an image of the desktop to file in EPS format, after prompting the
   * user for choice of Text or Lineart character rendering (unless a preference
   * has been set). The file name is generated as
   * 
   * <pre>
   * Jalview_snapshot_nnnnn.eps where nnnnn is the current timestamp in milliseconds
   * </pre>
   */
  @Override
  protected void snapShotWindow_actionPerformed(ActionEvent e)
  {
    // currently the menu option to do this is not shown
    invalidate();

    int width = getWidth();
    int height = getHeight();
    File of = new File(
            "Jalview_snapshot_" + System.currentTimeMillis() + ".eps");
    ImageWriterI writer = new ImageWriterI()
    {
      @Override
      public void exportImage(Graphics g) throws Exception
      {
        paintAll(g);
        jalview.bin.Console.info("Successfully written snapshot to file "
                + of.getAbsolutePath());
      }
    };
    String title = "View of desktop";
    ImageExporter exporter = new ImageExporter(writer, null, TYPE.EPS,
            title);
    try
    {
      exporter.doExport(of, this, width, height, title);
    } catch (ImageOutputException ioex)
    {
      jalview.bin.Console.error(
              "Unexpected error whilst writing Jalview desktop snapshot as EPS",
              ioex);
    }
  }

  /**
   * Explode the views in the given SplitFrame into separate SplitFrame windows.
   * This respects (remembers) any previous 'exploded geometry' i.e. the size
   * and location last time the view was expanded (if any). However it does not
   * remember the split pane divider location - this is set to match the
   * 'exploding' frame.
   * 
   * @param sf
   */
  public void explodeViews(SplitFrame sf)
  {
    AlignFrame oldTopFrame = (AlignFrame) sf.getTopFrame();
    AlignFrame oldBottomFrame = (AlignFrame) sf.getBottomFrame();
    List<? extends AlignmentViewPanel> topPanels = oldTopFrame
            .getAlignPanels();
    List<? extends AlignmentViewPanel> bottomPanels = oldBottomFrame
            .getAlignPanels();
    int viewCount = topPanels.size();
    if (viewCount < 2)
    {
      return;
    }

    /*
     * Processing in reverse order works, forwards order leaves the first panels not
     * visible. I don't know why!
     */
    for (int i = viewCount - 1; i >= 0; i--)
    {
      /*
       * Make new top and bottom frames. These take over the respective AlignmentPanel
       * objects, including their AlignmentViewports, so the cdna/protein
       * relationships between the viewports is carried over to the new split frames.
       * 
       * explodedGeometry holds the (x, y) position of the previously exploded
       * SplitFrame, and the (width, height) of the AlignFrame component
       */
      AlignmentPanel topPanel = (AlignmentPanel) topPanels.get(i);
      AlignFrame newTopFrame = new AlignFrame(topPanel);
      newTopFrame.setSize(oldTopFrame.getSize());
      newTopFrame.setVisible(true);
      Rectangle geometry = ((AlignViewport) topPanel.getAlignViewport())
              .getExplodedGeometry();
      if (geometry != null)
      {
        newTopFrame.setSize(geometry.getSize());
      }

      AlignmentPanel bottomPanel = (AlignmentPanel) bottomPanels.get(i);
      AlignFrame newBottomFrame = new AlignFrame(bottomPanel);
      newBottomFrame.setSize(oldBottomFrame.getSize());
      newBottomFrame.setVisible(true);
      geometry = ((AlignViewport) bottomPanel.getAlignViewport())
              .getExplodedGeometry();
      if (geometry != null)
      {
        newBottomFrame.setSize(geometry.getSize());
      }

      topPanel.av.setGatherViewsHere(false);
      bottomPanel.av.setGatherViewsHere(false);
      JInternalFrame splitFrame = new SplitFrame(newTopFrame,
              newBottomFrame);
      if (geometry != null)
      {
        splitFrame.setLocation(geometry.getLocation());
      }
      Desktop.addInternalFrame(splitFrame, sf.getTitle(), -1, -1);
    }

    /*
     * Clear references to the panels (now relocated in the new SplitFrames) before
     * closing the old SplitFrame.
     */
    topPanels.clear();
    bottomPanels.clear();
    sf.close();
  }

  /**
   * Gather expanded split frames, sharing the same pairs of sequence set ids,
   * back into the given SplitFrame as additional views. Note that the gathered
   * frames may themselves have multiple views.
   * 
   * @param source
   */
  public void gatherViews(GSplitFrame source)
  {
    /*
     * special handling of explodedGeometry for a view within a SplitFrame: - it
     * holds the (x, y) position of the enclosing SplitFrame, and the (width,
     * height) of the AlignFrame component
     */
    AlignFrame myTopFrame = (AlignFrame) source.getTopFrame();
    AlignFrame myBottomFrame = (AlignFrame) source.getBottomFrame();
    myTopFrame.viewport.setExplodedGeometry(new Rectangle(source.getX(),
            source.getY(), myTopFrame.getWidth(), myTopFrame.getHeight()));
    myBottomFrame.viewport
            .setExplodedGeometry(new Rectangle(source.getX(), source.getY(),
                    myBottomFrame.getWidth(), myBottomFrame.getHeight()));
    myTopFrame.viewport.setGatherViewsHere(true);
    myBottomFrame.viewport.setGatherViewsHere(true);
    String topViewId = myTopFrame.viewport.getSequenceSetId();
    String bottomViewId = myBottomFrame.viewport.getSequenceSetId();

    JInternalFrame[] frames = desktop.getAllFrames();
    for (JInternalFrame frame : frames)
    {
      if (frame instanceof SplitFrame && frame != source)
      {
        SplitFrame sf = (SplitFrame) frame;
        AlignFrame topFrame = (AlignFrame) sf.getTopFrame();
        AlignFrame bottomFrame = (AlignFrame) sf.getBottomFrame();
        boolean gatherThis = false;
        for (int a = 0; a < topFrame.alignPanels.size(); a++)
        {
          AlignmentPanel topPanel = topFrame.alignPanels.get(a);
          AlignmentPanel bottomPanel = bottomFrame.alignPanels.get(a);
          if (topViewId.equals(topPanel.av.getSequenceSetId())
                  && bottomViewId.equals(bottomPanel.av.getSequenceSetId()))
          {
            gatherThis = true;
            topPanel.av.setGatherViewsHere(false);
            bottomPanel.av.setGatherViewsHere(false);
            topPanel.av.setExplodedGeometry(
                    new Rectangle(sf.getLocation(), topFrame.getSize()));
            bottomPanel.av.setExplodedGeometry(
                    new Rectangle(sf.getLocation(), bottomFrame.getSize()));
            myTopFrame.addAlignmentPanel(topPanel, false);
            myBottomFrame.addAlignmentPanel(bottomPanel, false);
          }
        }

        if (gatherThis)
        {
          topFrame.getAlignPanels().clear();
          bottomFrame.getAlignPanels().clear();
          sf.close();
        }
      }
    }

    /*
     * The dust settles...give focus to the tab we did this from.
     */
    myTopFrame.setDisplayedView(myTopFrame.alignPanel);
  }

  public static groovy.ui.Console getGroovyConsole()
  {
    return groovyConsole;
  }

  /**
   * handles the payload of a drag and drop event.
   * 
   * TODO refactor to desktop utilities class
   * 
   * @param files
   *          - Data source strings extracted from the drop event
   * @param protocols
   *          - protocol for each data source extracted from the drop event
   * @param evt
   *          - the drop event
   * @param t
   *          - the payload from the drop event
   * @throws Exception
   */
  public static void transferFromDropTarget(List<Object> files,
          List<DataSourceType> protocols, DropTargetDropEvent evt,
          Transferable t) throws Exception
  {

    DataFlavor uriListFlavor = new DataFlavor(
            "text/uri-list;class=java.lang.String"), urlFlavour = null;
    try
    {
      urlFlavour = new DataFlavor(
              "application/x-java-url; class=java.net.URL");
    } catch (ClassNotFoundException cfe)
    {
      jalview.bin.Console.debug("Couldn't instantiate the URL dataflavor.",
              cfe);
    }

    if (urlFlavour != null && t.isDataFlavorSupported(urlFlavour))
    {

      try
      {
        java.net.URL url = (URL) t.getTransferData(urlFlavour);
        // nb: java 8 osx bug https://bugs.openjdk.java.net/browse/JDK-8156099
        // means url may be null.
        if (url != null)
        {
          protocols.add(DataSourceType.URL);
          files.add(url.toString());
          jalview.bin.Console.debug("Drop handled as URL dataflavor "
                  + files.get(files.size() - 1));
          return;
        }
        else
        {
          if (Platform.isAMacAndNotJS())
          {
            System.err.println(
                    "Please ignore plist error - occurs due to problem with java 8 on OSX");
          }
        }
      } catch (Throwable ex)
      {
        jalview.bin.Console.debug("URL drop handler failed.", ex);
      }
    }
    if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
    {
      // Works on Windows and MacOSX
      jalview.bin.Console.debug("Drop handled as javaFileListFlavor");
      for (Object file : (List) t
              .getTransferData(DataFlavor.javaFileListFlavor))
      {
        files.add(file);
        protocols.add(DataSourceType.FILE);
      }
    }
    else
    {
      // Unix like behaviour
      boolean added = false;
      String data = null;
      if (t.isDataFlavorSupported(uriListFlavor))
      {
        jalview.bin.Console.debug("Drop handled as uriListFlavor");
        // This is used by Unix drag system
        data = (String) t.getTransferData(uriListFlavor);
      }
      if (data == null)
      {
        // fallback to text: workaround - on OSX where there's a JVM bug
        jalview.bin.Console
                .debug("standard URIListFlavor failed. Trying text");
        // try text fallback
        DataFlavor textDf = new DataFlavor(
                "text/plain;class=java.lang.String");
        if (t.isDataFlavorSupported(textDf))
        {
          data = (String) t.getTransferData(textDf);
        }

        jalview.bin.Console.debug("Plain text drop content returned "
                + (data == null ? "Null - failed" : data));

      }
      if (data != null)
      {
        while (protocols.size() < files.size())
        {
          jalview.bin.Console.debug("Adding missing FILE protocol for "
                  + files.get(protocols.size()));
          protocols.add(DataSourceType.FILE);
        }
        for (java.util.StringTokenizer st = new java.util.StringTokenizer(
                data, "\r\n"); st.hasMoreTokens();)
        {
          added = true;
          String s = st.nextToken();
          if (s.startsWith("#"))
          {
            // the line is a comment (as per the RFC 2483)
            continue;
          }
          java.net.URI uri = new java.net.URI(s);
          if (uri.getScheme().toLowerCase(Locale.ROOT).startsWith("http"))
          {
            protocols.add(DataSourceType.URL);
            files.add(uri.toString());
          }
          else
          {
            // otherwise preserve old behaviour: catch all for file objects
            java.io.File file = new java.io.File(uri);
            protocols.add(DataSourceType.FILE);
            files.add(file.toString());
          }
        }
      }

      if (jalview.bin.Console.isDebugEnabled())
      {
        if (data == null || !added)
        {

          if (t.getTransferDataFlavors() != null
                  && t.getTransferDataFlavors().length > 0)
          {
            jalview.bin.Console.debug(
                    "Couldn't resolve drop data. Here are the supported flavors:");
            for (DataFlavor fl : t.getTransferDataFlavors())
            {
              jalview.bin.Console.debug(
                      "Supported transfer dataflavor: " + fl.toString());
              Object df = t.getTransferData(fl);
              if (df != null)
              {
                jalview.bin.Console.debug("Retrieves: " + df);
              }
              else
              {
                jalview.bin.Console.debug("Retrieved nothing");
              }
            }
          }
          else
          {
            jalview.bin.Console
                    .debug("Couldn't resolve dataflavor for drop: "
                            + t.toString());
          }
        }
      }
    }
    if (Platform.isWindowsAndNotJS())
    {
      jalview.bin.Console
              .debug("Scanning dropped content for Windows Link Files");

      // resolve any .lnk files in the file drop
      for (int f = 0; f < files.size(); f++)
      {
        String source = files.get(f).toString().toLowerCase(Locale.ROOT);
        if (protocols.get(f).equals(DataSourceType.FILE)
                && (source.endsWith(".lnk") || source.endsWith(".url")
                        || source.endsWith(".site")))
        {
          try
          {
            Object obj = files.get(f);
            File lf = (obj instanceof File ? (File) obj
                    : new File((String) obj));
            // process link file to get a URL
            jalview.bin.Console.debug("Found potential link file: " + lf);
            WindowsShortcut wscfile = new WindowsShortcut(lf);
            String fullname = wscfile.getRealFilename();
            protocols.set(f, FormatAdapter.checkProtocol(fullname));
            files.set(f, fullname);
            jalview.bin.Console.debug("Parsed real filename " + fullname
                    + " to extract protocol: " + protocols.get(f));
          } catch (Exception ex)
          {
            jalview.bin.Console.error(
                    "Couldn't parse " + files.get(f) + " as a link file.",
                    ex);
          }
        }
      }
    }
  }

  /**
   * Sets the Preferences property for experimental features to True or False
   * depending on the state of the controlling menu item
   */
  @Override
  protected void showExperimental_actionPerformed(boolean selected)
  {
    Cache.setProperty(EXPERIMENTAL_FEATURES, Boolean.toString(selected));
  }

  /**
   * Answers a (possibly empty) list of any structure viewer frames (currently
   * for either Jmol or Chimera) which are currently open. This may optionally
   * be restricted to viewers of a specified class, or viewers linked to a
   * specified alignment panel.
   * 
   * @param apanel
   *          if not null, only return viewers linked to this panel
   * @param structureViewerClass
   *          if not null, only return viewers of this class
   * @return
   */
  public List<StructureViewerBase> getStructureViewers(
          AlignmentPanel apanel,
          Class<? extends StructureViewerBase> structureViewerClass)
  {
    List<StructureViewerBase> result = new ArrayList<>();
    JInternalFrame[] frames = Desktop.instance.getAllFrames();

    for (JInternalFrame frame : frames)
    {
      if (frame instanceof StructureViewerBase)
      {
        if (structureViewerClass == null
                || structureViewerClass.isInstance(frame))
        {
          if (apanel == null
                  || ((StructureViewerBase) frame).isLinkedWith(apanel))
          {
            result.add((StructureViewerBase) frame);
          }
        }
      }
    }
    return result;
  }

  public static final String debugScaleMessage = "Desktop graphics transform scale=";

  private static boolean debugScaleMessageDone = false;

  public static void debugScaleMessage(Graphics g)
  {
    if (debugScaleMessageDone)
    {
      return;
    }
    // output used by tests to check HiDPI scaling settings in action
    try
    {
      Graphics2D gg = (Graphics2D) g;
      if (gg != null)
      {
        AffineTransform t = gg.getTransform();
        double scaleX = t.getScaleX();
        double scaleY = t.getScaleY();
        jalview.bin.Console.debug(debugScaleMessage + scaleX + " (X)");
        jalview.bin.Console.debug(debugScaleMessage + scaleY + " (Y)");
        debugScaleMessageDone = true;
      }
      else
      {
        jalview.bin.Console.debug("Desktop graphics null");
      }
    } catch (Exception e)
    {
      jalview.bin.Console.debug(Cache.getStackTraceString(e));
    }
  }

  /**
   * closes the current instance window, disposes and forgets about it.
   */
  public static void closeDesktop()
  {
    if (Desktop.instance != null)
    {
      Desktop.instance.closeAll_actionPerformed(null);
      Desktop.instance.setVisible(false);
      Desktop us = Desktop.instance;
      Desktop.instance = null;
      // call dispose in a separate thread - try to avoid indirect deadlocks
      new Thread(new Runnable() {
        @Override
        public void run()
        {
          ExecutorService dex = us.dialogExecutor;
          if (dex!=null) {
            dex.shutdownNow();
            us.dialogExecutor=null;
            us.block.drainPermits();
          }
          us.dispose();
        }
      }).start();
    }
  }

  /**
   * checks if any progress bars are being displayed in any of the windows managed by the desktop
   * @return
   */
  public boolean operationsAreInProgress()
  {
    JInternalFrame[] frames = getAllFrames();
    for (JInternalFrame frame:frames)
    {
      if (frame instanceof IProgressIndicator)
      {
        if (((IProgressIndicator)frame).operationInProgress())
        {
          return true;
        }
      }
    }
    return operationInProgress();
  }
}
