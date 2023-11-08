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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import jalview.util.ImageMaker.TYPE;
import jalview.util.MessageManager;

@SuppressWarnings("serial")
public class GTreePanel extends JInternalFrame
{
  BorderLayout borderLayout1 = new BorderLayout();

  public JScrollPane scrollPane = new JScrollPane();

  JMenuBar jMenuBar1 = new JMenuBar();

  JMenu fileMenu = new JMenu();

  JMenuItem saveAsNewick = new JMenuItem();

  JMenuItem printMenu = new JMenuItem();

  protected JMenu viewMenu = new JMenu();

  public JMenuItem font = new JMenuItem();

  public JMenuItem sortAssocViews = new JMenuItem();

  public JCheckBoxMenuItem bootstrapMenu = new JCheckBoxMenuItem();

  public JCheckBoxMenuItem distanceMenu = new JCheckBoxMenuItem();

  public JCheckBoxMenuItem fitToWindow = new JCheckBoxMenuItem();

  public JCheckBoxMenuItem placeholdersMenu = new JCheckBoxMenuItem();

  JMenuItem pngTree = new JMenuItem();

  JMenuItem epsTree = new JMenuItem();

  JMenu saveAsMenu = new JMenu();

  JMenuItem textbox = new JMenuItem();

  public JMenuItem originalSeqData = new JMenuItem();

  protected JMenu associateLeavesMenu = new JMenu();

  public GTreePanel()
  {
    try
    {
      jbInit();
      this.setJMenuBar(jMenuBar1);
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception
  {
    setFrameIcon(null);
    setName("jalview-tree");
    this.getContentPane().setLayout(borderLayout1);
    this.setBackground(Color.white);
    this.setFont(new java.awt.Font("Verdana", 0, 12));
    scrollPane.setOpaque(false);
    fileMenu.setText(MessageManager.getString("action.file"));
    saveAsNewick.setText(MessageManager.getString("label.newick_format"));
    saveAsNewick.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        saveAsNewick_actionPerformed(e);
      }
    });
    printMenu.setText(MessageManager.getString("action.print"));
    printMenu.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        printMenu_actionPerformed(e);
      }
    });
    viewMenu.setText(MessageManager.getString("action.view"));
    viewMenu.addMenuListener(new MenuListener()
    {
      @Override
      public void menuSelected(MenuEvent e)
      {
        viewMenu_menuSelected();
      }

      @Override
      public void menuDeselected(MenuEvent e)
      {
      }

      @Override
      public void menuCanceled(MenuEvent e)
      {
      }
    });
    sortAssocViews.setText(
            MessageManager.getString("label.sort_alignment_by_tree"));
    sortAssocViews.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        sortByTree_actionPerformed();
      }
    });
    font.setText(MessageManager.getString("action.font"));
    font.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        font_actionPerformed(e);
      }
    });
    bootstrapMenu.setText(
            MessageManager.getString("label.show_bootstrap_values"));
    bootstrapMenu.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        bootstrapMenu_actionPerformed(e);
      }
    });
    distanceMenu.setText(MessageManager.getString("label.show_distances"));
    distanceMenu.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        distanceMenu_actionPerformed(e);
      }
    });
    fitToWindow.setSelected(true);
    fitToWindow.setText(MessageManager.getString("label.fit_to_window"));
    fitToWindow.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        fitToWindow_actionPerformed(e);
      }
    });
    epsTree.setText("EPS");
    epsTree.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        writeTreeImage(TYPE.EPS);
      }
    });
    pngTree.setText("PNG");
    pngTree.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        writeTreeImage(TYPE.PNG);
      }
    });
    saveAsMenu.setText(MessageManager.getString("action.save_as"));
    placeholdersMenu.setToolTipText(MessageManager.getString(
            "label.marks_leaves_tree_not_associated_with_sequence"));
    placeholdersMenu.setText(
            MessageManager.getString("label.mark_unlinked_leaves"));
    placeholdersMenu.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        placeholdersMenu_actionPerformed(e);
      }
    });
    textbox.setText(MessageManager.getString("label.out_to_textbox"));
    textbox.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        textbox_actionPerformed(e);
      }
    });
    originalSeqData.setText(MessageManager.getString("label.input_data"));
    originalSeqData.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        originalSeqData_actionPerformed(e);
      }
    });
    associateLeavesMenu.setText(
            MessageManager.getString("label.associate_leaves_with"));
    this.getContentPane().add(scrollPane, BorderLayout.CENTER);
    jMenuBar1.add(fileMenu);
    jMenuBar1.add(viewMenu);
    fileMenu.add(saveAsMenu);
    fileMenu.add(textbox);
    fileMenu.add(printMenu);
    fileMenu.add(originalSeqData);
    viewMenu.add(fitToWindow);
    viewMenu.add(font);
    viewMenu.add(distanceMenu);
    viewMenu.add(bootstrapMenu);
    viewMenu.add(placeholdersMenu);
    viewMenu.add(sortAssocViews);
    viewMenu.add(associateLeavesMenu);
    saveAsMenu.add(saveAsNewick);
    saveAsMenu.add(epsTree);
    saveAsMenu.add(pngTree);
  }

  public void printMenu_actionPerformed(ActionEvent e)
  {
  }

  public void font_actionPerformed(ActionEvent e)
  {
  }

  public void distanceMenu_actionPerformed(ActionEvent e)
  {
  }

  public void bootstrapMenu_actionPerformed(ActionEvent e)
  {
  }

  public void fitToWindow_actionPerformed(ActionEvent e)
  {
  }

  public void writeTreeImage(TYPE imageType)
  {
  }

  public void saveAsNewick_actionPerformed(ActionEvent e)
  {
  }

  public void placeholdersMenu_actionPerformed(ActionEvent e)
  {
  }

  public void textbox_actionPerformed(ActionEvent e)
  {
  }

  public void fullid_actionPerformed(ActionEvent e)
  {

  }

  public void originalSeqData_actionPerformed(ActionEvent e)
  {

  }

  public void viewMenu_menuSelected()
  {
  }

  public void sortByTree_actionPerformed()
  {

  }

}
