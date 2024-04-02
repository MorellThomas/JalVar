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

import jalview.bin.Cache;
import jalview.io.DataSourceType;
import jalview.io.FileFormatException;
import jalview.io.FileFormatI;
import jalview.io.FileFormats;
import jalview.io.FileLoader;
import jalview.io.IdentifyFile;
import jalview.io.JalviewFileChooser;
import jalview.io.JalviewFileView;
import jalview.util.MessageManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * A dialog where a user has to input information necessary for the equivalent position calculation
 * called by CustomChooser
 */
public class EpInput extends JPanel
{
  private static final Font VERDANA_11PT = new Font("Verdana", 0, 11);

  AlignFrame af;
  
  AlignViewport av;
  
  int width;

  JRadioButton forward;	
  
  JRadioButton reverse;
  
  char FoR;
  
  JTextField startPoint;
  
  int startPosition;
  
  JButton loadfile;

  JButton calculate;

  private JInternalFrame frame;

  final ComboBoxTooltipRenderer renderer = new ComboBoxTooltipRenderer();

  List<String> tips = new ArrayList<>();

  private EPPanel epPanel;

  /**
   * Constructor
   * 
   * @param af
   */
  public EpInput(AlignFrame alignFrame)
  {
    this.af = alignFrame;
    this.av = alignFrame.getViewport();
    this.width = this.av.getAlignment().getWidth();
    init();
    af.alignPanel.setEpInput(this);
  }
  
  /**
   * Lays out the panel and adds it to the desktop
   */
  void init()
  {
    setLayout(new BorderLayout());
    frame = new JInternalFrame();
    frame.setFrameIcon(null);
    frame.setContentPane(this);
    this.setBackground(Color.white);
    frame.addFocusListener(new FocusListener()
    {

      @Override
      public void focusLost(FocusEvent e)
      {
      }

      @Override
      public void focusGained(FocusEvent e)
      {
      }
    });
    
    //creates the buttons and fields
    forward = new JRadioButton(MessageManager.getString("label.forward"));
    forward.setOpaque(false);
    
    reverse = new JRadioButton(MessageManager.getString("label.reverse"));
    reverse.setOpaque(false);
    
    startPoint = new JTextField("178807423", 10);
    startPoint.setOpaque(false);

    JPanel calcChoicePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    calcChoicePanel.setOpaque(false);

    //-- --

    JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    textPanel.setOpaque(false);
    JvSwingUtils.createTitledBorder(textPanel,
          MessageManager.getString("label.start_position"), true);    //set text border
    textPanel.add(startPoint);

    
    //--
    JPanel strandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    strandPanel.setOpaque(false);

    JvSwingUtils.createTitledBorder(strandPanel,
            MessageManager.getString("label.strand"), true);

    strandPanel.add(forward);
    strandPanel.add(reverse);
    calcChoicePanel.add(textPanel);
    calcChoicePanel.add(strandPanel);
    
    //--
    
    loadfile = new JButton(MessageManager.getString("action.load"));
    loadfile.setFont(VERDANA_11PT);
    loadfile.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
       openFileBrowser(); 
      }
    });
   
    JPanel loadPanel = new JPanel();
    loadPanel.setOpaque(false);
    loadPanel.add(loadfile);
    JvSwingUtils.createTitledBorder(loadPanel,
            MessageManager.getString("label.gene_file"), true);
    
   //-- --

    ButtonGroup forOrRev = new ButtonGroup();
    forOrRev.add(forward);
    forOrRev.add(reverse);

    /*
     * OK / Cancel buttons
     */
    calculate = new JButton(MessageManager.getString("action.calculate"));
    calculate.setFont(VERDANA_11PT);
    calculate.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        calculate_actionPerformed();
      }
    });
    JButton close = new JButton(MessageManager.getString("action.close"));
    close.setFont(VERDANA_11PT);
    close.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        close_actionPerformed();
      }
    });
    JPanel actionPanel = new JPanel();
    actionPanel.setOpaque(false);
    actionPanel.add(calculate);
    actionPanel.add(close);

    boolean includeParams = false;
    this.add(calcChoicePanel, BorderLayout.NORTH);
    this.add(loadPanel, BorderLayout.CENTER);
    this.add(actionPanel, BorderLayout.SOUTH);

    int width = 350;
    int height = includeParams ? 420 : 240;

    setMinimumSize(new Dimension(325, height - 10));
    String title = MessageManager.getString("label.choose_calculation");
    if (af.getViewport().getViewName() != null)
    {
      title = title + " (" + af.getViewport().getViewName() + ")";
    }

    Desktop.addInternalFrame(frame, title, width, height, false);
    calcChoicePanel.doLayout();
    revalidate();
    /*
     * null the AlignmentPanel's reference to the dialog when it is closed
     */
    frame.addInternalFrameListener(new InternalFrameAdapter()
    {
      @Override
      public void internalFrameClosed(InternalFrameEvent evt)
      {
        af.alignPanel.setEpInput(null);
      };
    });

    frame.setLayer(JLayeredPane.PALETTE_LAYER);
  }


  /**
   * Open and calculate the selected 
   */
  protected void calculate_actionPerformed()
  {
    boolean F = forward.isSelected();
    boolean R = reverse.isSelected();

    if (F && !R)
    {
      FoR = 'F';
    }
    else
    {
      FoR = 'R';
    }
    
    startPosition = Integer.parseInt(startPoint.getText());
    
    epPanel = new EPPanel(af, startPosition, FoR, width);
    new Thread(epPanel).start();
    closeFrame();
  }


  /**
   * 
   */
  protected void closeFrame()
  {
    try
    {
      frame.setClosed(true);
    } catch (PropertyVetoException ex)
    {
    }
  }


  /**
   * Closes dialog on Close button press
   */
  protected void close_actionPerformed()
  {
    try
    {
      frame.setClosed(true);
    } catch (Exception ex)
    {
    }
  }
  
  /**
   * opens file browser dialog
   */
  protected void openFileBrowser()
  {
    JalviewFileChooser chooser = new JalviewFileChooser(
            Cache.getProperty("LAST_DIRECTORY"));
    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(
            MessageManager.formatMessage("action.load"));

    int value = chooser.showOpenDialog(null);
    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
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

      new FileLoader().LoadFile(af.getViewport(), selectedFile, DataSourceType.FILE, format);
    }
  }
  
  protected char getFoR()
  {
    return FoR;
  }
  
  protected int getStart()
  {
    return startPosition;
  }

}