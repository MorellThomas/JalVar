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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;

import jalview.api.structures.JalviewStructureDisplayI;
import jalview.gui.ColourMenuHelper.ColourChangeListener;
import jalview.util.ImageMaker.TYPE;
import jalview.util.MessageManager;

@SuppressWarnings("serial")
public abstract class GStructureViewer extends JInternalFrame
        implements JalviewStructureDisplayI, ColourChangeListener
{
  // private AAStructureBindingModel bindingModel;

  protected JMenu savemenu;

  protected JMenu viewMenu;

  protected JMenu colourMenu;

  protected JMenu chainMenu;

  protected JMenu viewerActionMenu;

  protected JMenuItem alignStructs;

  protected JMenuItem fitToWindow;

  protected JRadioButtonMenuItem seqColour;

  protected JRadioButtonMenuItem chainColour;

  protected JRadioButtonMenuItem chargeColour;

  protected JRadioButtonMenuItem viewerColour;

  protected JMenuItem helpItem;

  protected JLabel statusBar;

  protected JPanel statusPanel;

  /**
   * Constructor
   */
  public GStructureViewer()
  {
    try
    {
      jbInit();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private void jbInit() throws Exception
  {
    setFrameIcon(null);
    setName("jalview-structureviewer");

    JMenuBar menuBar = new JMenuBar();
    this.setJMenuBar(menuBar);

    JMenu fileMenu = new JMenu();
    fileMenu.setText(MessageManager.getString("action.file"));

    savemenu = new JMenu();
    savemenu.setActionCommand(
            MessageManager.getString("action.save_image"));
    savemenu.setText(MessageManager.getString("action.save_as"));

    JMenuItem pdbFile = new JMenuItem();
    pdbFile.setText(MessageManager.getString("label.pdb_file"));
    pdbFile.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        pdbFile_actionPerformed();
      }
    });

    JMenuItem png = new JMenuItem();
    png.setText("PNG");
    png.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        makePDBImage(TYPE.PNG);
      }
    });

    JMenuItem eps = new JMenuItem();
    eps.setText("EPS");
    eps.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        makePDBImage(TYPE.EPS);
      }
    });

    JMenuItem viewMapping = new JMenuItem();
    viewMapping.setText(MessageManager.getString("label.view_mapping"));
    viewMapping.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        viewMapping_actionPerformed();
      }
    });

    viewMenu = new JMenu();
    viewMenu.setText(MessageManager.getString("action.view"));

    chainMenu = new JMenu();
    chainMenu.setText(MessageManager.getString("action.show_chain"));

    fitToWindow = new JMenuItem();
    fitToWindow.setText(MessageManager.getString("label.fit_to_window"));
    fitToWindow.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        fitToWindow_actionPerformed();
      }
    });

    JMenu helpMenu = new JMenu();
    helpMenu.setText(MessageManager.getString("action.help"));
    helpItem = new JMenuItem();
    helpItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        showHelp_actionPerformed();
      }
    });
    alignStructs = new JMenuItem();
    alignStructs.setText(
            MessageManager.getString("label.superpose_structures"));
    alignStructs.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        alignStructsWithAllAlignPanels();
      }
    });

    viewerActionMenu = new JMenu(); // text set in sub-classes
    viewerActionMenu.setVisible(false);
    viewerActionMenu.add(alignStructs);
    colourMenu = new JMenu();
    colourMenu.setText(MessageManager.getString("label.colours"));
    fileMenu.add(savemenu);
    fileMenu.add(viewMapping);
    savemenu.add(pdbFile);
    savemenu.add(png);
    savemenu.add(eps);
    viewMenu.add(chainMenu);
    helpMenu.add(helpItem);

    menuBar.add(fileMenu);
    menuBar.add(viewMenu);
    menuBar.add(colourMenu);
    menuBar.add(viewerActionMenu);
    menuBar.add(helpMenu);

    statusPanel = new JPanel();
    statusPanel.setLayout(new GridLayout());
    this.getContentPane().add(statusPanel, BorderLayout.SOUTH);
    statusBar = new JLabel();
    statusPanel.add(statusBar, null);
  }

  protected void fitToWindow_actionPerformed()
  {
    getBinding().focusView();
  }

  protected void highlightSelection_actionPerformed()
  {
  }

  protected void viewerColour_actionPerformed()
  {
  }

  protected abstract String alignStructsWithAllAlignPanels();

  public void pdbFile_actionPerformed()
  {

  }

  public void makePDBImage(TYPE imageType)
  {

  }

  public void viewMapping_actionPerformed()
  {

  }

  public void seqColour_actionPerformed()
  {

  }

  public void chainColour_actionPerformed()
  {

  }

  public void chargeColour_actionPerformed()
  {

  }

  public void background_actionPerformed()
  {

  }

  public void showHelp_actionPerformed()
  {

  }
}
