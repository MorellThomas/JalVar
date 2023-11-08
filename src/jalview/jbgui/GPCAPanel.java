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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import jalview.util.ImageMaker.TYPE;
import jalview.util.MessageManager;

public class GPCAPanel extends JInternalFrame
{
  private static final Font VERDANA_12 = new Font("Verdana", 0, 12);

  protected JComboBox<String> xCombobox = new JComboBox<>();

  protected JComboBox<String> yCombobox = new JComboBox<>();

  protected JComboBox<String> zCombobox = new JComboBox<>();

  protected JMenu viewMenu = new JMenu();

  protected JCheckBoxMenuItem showLabels = new JCheckBoxMenuItem();

  protected JMenu associateViewsMenu = new JMenu();

  protected JLabel statusBar = new JLabel();

  protected JPanel statusPanel = new JPanel();

  protected JMenuItem originalSeqData;

  /**
   * Constructor
   */
  public GPCAPanel()
  {
    try
    {
      jbInit();
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    for (int i = 1; i < 8; i++)
    {
      xCombobox.addItem("dim " + i);
      yCombobox.addItem("dim " + i);
      zCombobox.addItem("dim " + i);
    }
  }
  public GPCAPanel(int dim)
  {
    try
    {
      jbInit();
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    for (int i = 1; i <= dim; i++)
    {
      xCombobox.addItem("dim " + i);
      yCombobox.addItem("dim " + i);
      zCombobox.addItem("dim " + i);
    }
  }

  private void jbInit() throws Exception
  {
    setFrameIcon(null);
    setName("jalview-pca");
    this.getContentPane().setLayout(new BorderLayout());
    JPanel jPanel2 = new JPanel();
    jPanel2.setLayout(new FlowLayout());
    JLabel jLabel1 = new JLabel();
    jLabel1.setFont(VERDANA_12);
    jLabel1.setText("x=");
    JLabel jLabel2 = new JLabel();
    jLabel2.setFont(VERDANA_12);
    jLabel2.setText("y=");
    JLabel jLabel3 = new JLabel();
    jLabel3.setFont(VERDANA_12);
    jLabel3.setText("z=");
    jPanel2.setBackground(Color.white);
    jPanel2.setBorder(null);
    zCombobox.setFont(VERDANA_12);
    zCombobox.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        doDimensionChange();
      }
    });
    yCombobox.setFont(VERDANA_12);
    yCombobox.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        doDimensionChange();
      }
    });
    xCombobox.setFont(VERDANA_12);
    xCombobox.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        doDimensionChange();
      }
    });
    JButton resetButton = new JButton();
    resetButton.setFont(VERDANA_12);
    resetButton.setText(MessageManager.getString("action.reset"));
    resetButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        resetButton_actionPerformed();
      }
    });
    JMenu fileMenu = new JMenu();
    fileMenu.setText(MessageManager.getString("action.file"));
    JMenu saveMenu = new JMenu();
    saveMenu.setText(MessageManager.getString("action.save_as"));
    JMenuItem eps = new JMenuItem("EPS");
    eps.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        makePCAImage(TYPE.EPS);
      }
    });
    JMenuItem png = new JMenuItem("PNG");
    png.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        makePCAImage(TYPE.PNG);
      }
    });
    JMenuItem outputValues = new JMenuItem();
    outputValues.setText(MessageManager.getString("label.output_values"));
    outputValues.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        outputValues_actionPerformed();
      }
    });
    JMenuItem outputPoints = new JMenuItem();
    outputPoints.setText(MessageManager.getString("label.output_points"));
    outputPoints.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        outputPoints_actionPerformed();
      }
    });
    JMenuItem outputProjPoints = new JMenuItem();
    outputProjPoints.setText(
            MessageManager.getString("label.output_transformed_points"));
    outputProjPoints.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        outputProjPoints_actionPerformed();
      }
    });
    JMenuItem print = new JMenuItem();
    print.setText(MessageManager.getString("action.print"));
    print.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        print_actionPerformed();
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
    showLabels.setText(MessageManager.getString("label.show_labels"));
    showLabels.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showLabels_actionPerformed();
      }
    });
    JMenuItem bgcolour = new JMenuItem();
    bgcolour.setText(MessageManager.getString("action.background_colour"));
    bgcolour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        bgcolour_actionPerformed();
      }
    });
    originalSeqData = new JMenuItem();
    originalSeqData.setText(MessageManager.getString("label.input_data"));
    originalSeqData.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        originalSeqData_actionPerformed();
      }
    });
    associateViewsMenu.setText(
            MessageManager.getString("label.associate_nodes_with"));

    statusPanel.setLayout(new GridLayout());
    statusBar.setFont(VERDANA_12);
    // statusPanel.setBackground(Color.lightGray);
    // statusBar.setBackground(Color.lightGray);
    // statusPanel.add(statusBar, null);
    JPanel panelBar = new JPanel(new BorderLayout());
    panelBar.add(jPanel2, BorderLayout.NORTH);
    panelBar.add(statusPanel, BorderLayout.SOUTH);
    this.getContentPane().add(panelBar, BorderLayout.SOUTH);
    jPanel2.add(jLabel1, null);
    jPanel2.add(xCombobox, null);
    jPanel2.add(jLabel2, null);
    jPanel2.add(yCombobox, null);
    jPanel2.add(jLabel3, null);
    jPanel2.add(zCombobox, null);
    jPanel2.add(resetButton, null);

    JMenuBar jMenuBar1 = new JMenuBar();
    jMenuBar1.add(fileMenu);
    jMenuBar1.add(viewMenu);
    setJMenuBar(jMenuBar1);
    fileMenu.add(saveMenu);
    fileMenu.add(outputValues);
    fileMenu.add(print);
    fileMenu.add(originalSeqData);
    fileMenu.add(outputPoints);
    fileMenu.add(outputProjPoints);
    saveMenu.add(eps);
    saveMenu.add(png);
    viewMenu.add(showLabels);
    viewMenu.add(bgcolour);
    viewMenu.add(associateViewsMenu);
  }

  protected void resetButton_actionPerformed()
  {
  }

  protected void outputPoints_actionPerformed()
  {
  }

  protected void outputProjPoints_actionPerformed()
  {
  }

  public void makePCAImage(TYPE imageType)
  {
  }

  protected void outputValues_actionPerformed()
  {
  }

  protected void print_actionPerformed()
  {
  }

  protected void showLabels_actionPerformed()
  {
  }

  protected void bgcolour_actionPerformed()
  {
  }

  protected void originalSeqData_actionPerformed()
  {
  }

  protected void viewMenu_menuSelected()
  {
  }

  protected void doDimensionChange()
  {
  }
}
