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
package jalview.jbgui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import jalview.api.AlignmentViewPanel;
import jalview.bin.Cache;
import jalview.gui.APQHandlers;
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;
import jalview.io.FileFormatException;
import jalview.util.MessageManager;
import jalview.util.Platform;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class GDesktop extends JFrame
{

  protected static JMenu windowMenu = new JMenu();

  JMenuBar desktopMenubar = new JMenuBar();

  JMenu FileMenu = new JMenu();

  JMenu HelpMenu = new JMenu();

  JMenuItem inputLocalFileMenuItem = new JMenuItem();

  JMenuItem inputURLMenuItem = new JMenuItem();

  JMenuItem inputTextboxMenuItem = new JMenuItem();

  JMenuItem quit = new JMenuItem();

  JMenuItem aboutMenuItem = new JMenuItem();

  JMenuItem documentationMenuItem = new JMenuItem();

  FlowLayout flowLayout1 = new FlowLayout();

  protected JMenu toolsMenu = new JMenu();

  JMenuItem preferences = new JMenuItem();

  JMenuItem saveState = new JMenuItem();

  JMenuItem saveAsState = new JMenuItem();

  JMenuItem loadState = new JMenuItem();
  
  JMenuItem deleteRefs = new JMenuItem();

  JMenu inputMenu = new JMenu();

  JMenuItem inputSequence = new JMenuItem();

  JMenuItem closeAll = new JMenuItem();

  JMenuItem raiseRelated = new JMenuItem();

  JMenuItem minimizeAssociated = new JMenuItem();

  protected JCheckBoxMenuItem showMemusage = new JCheckBoxMenuItem();

  JMenuItem garbageCollect = new JMenuItem();

  protected JMenuItem groovyShell;

  protected JCheckBoxMenuItem experimentalFeatures;

  protected JCheckBoxMenuItem showConsole = new JCheckBoxMenuItem();

  protected JCheckBoxMenuItem showNews = new JCheckBoxMenuItem();

  protected JMenuItem snapShotWindow = new JMenuItem();

  /**
   * Creates a new GDesktop object.
   */
  public GDesktop()
  {
    super();
    try
    {
      jbInit();
      this.setJMenuBar(desktopMenubar);
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    if (Platform.allowMnemonics())
    {
      // BH was !Platform.isAMacAndNotJS()) i.e. "JS or not Mac"
      // but here we want just not a Mac, period, right?
      FileMenu.setMnemonic('F');
      inputLocalFileMenuItem.setMnemonic('L');
      inputURLMenuItem.setMnemonic('U');
      inputTextboxMenuItem.setMnemonic('C');
      quit.setMnemonic('Q');
      saveState.setMnemonic('S');
      loadState.setMnemonic('L');
      inputMenu.setMnemonic('I');
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @throws Exception
   *           DOCUMENT ME!
   */
  private void jbInit() throws Exception
  {
    /**
     * APQHandlers sets handlers for About, Preferences and Quit actions
     * peculiar to macOS's application menu. APQHandlers will check to see if a
     * handler is supported before setting it.
     */
    try
    {
      APQHandlers.setAPQHandlers((Desktop) this);
    } catch (Exception e)
    {
      System.out.println("Cannot set APQHandlers");
      // e.printStackTrace();
    } catch (Throwable t)
    {
      jalview.bin.Console
              .warn("Error setting APQHandlers: " + t.toString());
      jalview.bin.Console.trace(Cache.getStackTraceString(t));
    }

    setName("jalview-desktop");
    FileMenu.setText(MessageManager.getString("action.file"));
    HelpMenu.setText(MessageManager.getString("action.help"));
    inputLocalFileMenuItem
            .setText(MessageManager.getString("label.load_tree_from_file"));
    inputLocalFileMenuItem
            .setAccelerator(
                    javax.swing.KeyStroke
                            .getKeyStroke(java.awt.event.KeyEvent.VK_O,
                                    jalview.util.ShortcutKeyMaskExWrapper
                                            .getMenuShortcutKeyMaskEx(),
                                    false));
    inputLocalFileMenuItem
            .addActionListener(new java.awt.event.ActionListener()
            {
              @Override
              public void actionPerformed(ActionEvent e)
              {
                inputLocalFileMenuItem_actionPerformed(null);
              }
            });
    inputURLMenuItem.setText(MessageManager.getString("label.from_url"));
    inputURLMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        try
        {
          inputURLMenuItem_actionPerformed(null);
        } catch (FileFormatException e1)
        {
          System.err.println("Error loading from URL: " + e1.getMessage());
        }
      }
    });
    inputTextboxMenuItem
            .setText(MessageManager.getString("label.from_textbox"));
    inputTextboxMenuItem
            .addActionListener(new java.awt.event.ActionListener()
            {
              @Override
              public void actionPerformed(ActionEvent e)
              {
                inputTextboxMenuItem_actionPerformed(null);
              }
            });
    quit.setText(MessageManager.getString("action.quit"));
    quit.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        if (Desktop.instance != null)
          Desktop.instance.desktopQuit();
      }
    });
    aboutMenuItem.setText(MessageManager.getString("label.about"));
    aboutMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        aboutMenuItem_actionPerformed(e);
      }
    });
    documentationMenuItem
            .setText(MessageManager.getString("label.documentation"));
    documentationMenuItem.setAccelerator(javax.swing.KeyStroke
            .getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0, false));
    documentationMenuItem
            .addActionListener(new java.awt.event.ActionListener()
            {
              @Override
              public void actionPerformed(ActionEvent e)
              {
                documentationMenuItem_actionPerformed();
              }
            });
    this.getContentPane().setLayout(flowLayout1);
    windowMenu.setText(MessageManager.getString("label.window"));
    preferences.setText(MessageManager.getString("label.preferences"));
    preferences.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        preferences_actionPerformed(e);
      }
    });
    toolsMenu.setText(MessageManager.getString("label.tools"));
    saveState.setText(MessageManager.getString("action.save_project"));
    saveState.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        saveState_actionPerformed();
      }
    });
    saveAsState.setText(MessageManager.getString("action.save_project_as"));
    saveAsState.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        saveAsState_actionPerformed(e);
      }
    });
    loadState.setText(MessageManager.getString("action.load_project"));
    loadState.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        loadState_actionPerformed();
      }
    });
    
    deleteRefs.setText(MessageManager.getString("action.delete_refs"));
    deleteRefs.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        try
        {
          deleteRefs_actionPerformed(e);
        } catch (ClassNotFoundException e1)
        {
          e1.printStackTrace();
          JvOptionPane.showInternalMessageDialog(Desktop.desktop, e1.getMessage(), "ClassNotFoundException", JvOptionPane.ERROR_MESSAGE);
        } catch (IOException e1)
        {
          e1.printStackTrace();
          JvOptionPane.showInternalMessageDialog(Desktop.desktop, e1.getMessage(), "IOException", JvOptionPane.ERROR_MESSAGE);
        }
      }
    });
    
    inputMenu.setText(MessageManager.getString("label.input_alignment"));
    inputSequence
            .setText(MessageManager.getString("action.fetch_sequences"));
    inputSequence.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        inputSequence_actionPerformed(e);
      }
    });
    closeAll.setText(MessageManager.getString("action.close_all"));
    closeAll.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        closeAll_actionPerformed(e);
      }
    });
    raiseRelated.setText(
            MessageManager.getString("action.raise_associated_windows"));
    raiseRelated.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        raiseRelated_actionPerformed(e);
      }
    });
    minimizeAssociated.setText(
            MessageManager.getString("action.minimize_associated_windows"));
    minimizeAssociated.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        minimizeAssociated_actionPerformed(e);
      }
    });
    garbageCollect
            .setText(MessageManager.getString("label.collect_garbage"));
    garbageCollect.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        garbageCollect_actionPerformed(e);
      }
    });
    showMemusage
            .setText(MessageManager.getString("label.show_memory_usage"));
    showMemusage.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showMemusage_actionPerformed(e);
      }
    });
    showConsole
            .setText(MessageManager.getString("label.show_java_console"));
    showConsole.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showConsole_actionPerformed(e);
      }
    });
    showNews.setText(MessageManager.getString("label.show_jalview_news"));
    showNews.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showNews_actionPerformed(e);
      }
    });
    groovyShell = new JMenuItem();
    groovyShell.setText(MessageManager.getString("label.groovy_console"));
    groovyShell.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        groovyShell_actionPerformed();
      }
    });
    experimentalFeatures = new JCheckBoxMenuItem();
    experimentalFeatures
            .setText(MessageManager.getString("label.show_experimental"));
    experimentalFeatures.setToolTipText(
            MessageManager.getString("label.show_experimental_tip"));
    experimentalFeatures.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showExperimental_actionPerformed(experimentalFeatures.isSelected());
      }
    });

    snapShotWindow.setText(MessageManager.getString("label.take_snapshot"));
    snapShotWindow.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        snapShotWindow_actionPerformed(e);
      }
    });

    desktopMenubar.add(FileMenu);
    desktopMenubar.add(toolsMenu);
    desktopMenubar.add(HelpMenu);
    desktopMenubar.add(windowMenu);
    FileMenu.add(inputMenu);
    FileMenu.add(inputSequence);
    FileMenu.addSeparator();
    // FileMenu.add(saveState);
    FileMenu.add(saveAsState);
    FileMenu.add(loadState);
    FileMenu.add(deleteRefs);
    FileMenu.addSeparator();
    if (!APQHandlers.setQuit)
    {
      FileMenu.add(quit);
    }
    if (!APQHandlers.setAbout)
    {
      HelpMenu.add(aboutMenuItem);
    }
    HelpMenu.add(documentationMenuItem);
    if (!APQHandlers.setPreferences)
    {
      toolsMenu.add(preferences);
    }
    if (!Platform.isJS())
    {
      toolsMenu.add(showMemusage);
      toolsMenu.add(showConsole);
      toolsMenu.add(showNews);
      toolsMenu.add(garbageCollect);
      toolsMenu.add(groovyShell);
    }
    toolsMenu.add(experimentalFeatures);
    // toolsMenu.add(snapShotWindow);
    inputMenu.add(inputLocalFileMenuItem);
    inputMenu.add(inputURLMenuItem);
    inputMenu.add(inputTextboxMenuItem);
    windowMenu.add(closeAll);
    windowMenu.add(raiseRelated);
    windowMenu.add(minimizeAssociated);
    windowMenu.addSeparator();
    // inputMenu.add(vamsasLoad);
  }

  protected void showExperimental_actionPerformed(boolean selected)
  {
  }

  protected void groovyShell_actionPerformed()
  {
  }

  protected void snapShotWindow_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void showConsole_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void showNews_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void showMemusage_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void garbageCollect_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void vamsasStMenu_actionPerformed()
  {
  }

  public void vamsasSave_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void inputLocalFileMenuItem_actionPerformed(
          jalview.gui.AlignViewport av)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   * @throws FileFormatException
   */
  protected void inputURLMenuItem_actionPerformed(
          jalview.gui.AlignViewport av) throws FileFormatException
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void inputTextboxMenuItem_actionPerformed(
          AlignmentViewPanel avp)
  {
  }

  /**
   * DOCUMENT ME!
   */
  protected void quit()
  {
    // System.out.println("********** GDesktop.quit()");
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void aboutMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void documentationMenuItem_actionPerformed()
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void preferences_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void saveState_actionPerformed()
  {
  }

  public void saveAsState_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void loadState_actionPerformed()
  {
  }
  
  public void deleteRefs_actionPerformed(ActionEvent e) throws ClassNotFoundException, IOException
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void loadJalviewAlign_actionPerformed(ActionEvent e)
  {
  }

  public void vamsasStart_actionPerformed(ActionEvent e)
  {

  }

  public void inputSequence_actionPerformed(ActionEvent e)
  {

  }

  public void vamsasStop_actionPerformed(ActionEvent e)
  {

  }

  public void closeAll_actionPerformed(ActionEvent e)
  {

  }

  public void raiseRelated_actionPerformed(ActionEvent e)
  {

  }

  public void minimizeAssociated_actionPerformed(ActionEvent e)
  {

  }

  public void vamsasImport_actionPerformed(ActionEvent e)
  {
  }
}
