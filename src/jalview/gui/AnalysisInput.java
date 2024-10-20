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

import jalview.datamodel.SequenceI;
import jalview.util.MessageManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

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
 * A dialog where a user has to input the specified residue for the residue analysis
 * called by CustomChooser
 */
public class AnalysisInput extends JPanel
{
  private static final Font VERDANA_11PT = new Font("Verdana", 0, 11);

  AlignFrame af;
  
  AlignViewport av;
  
  private final boolean doVars;
  
  private final int numberOfProtein;
  
  private final String[] namesOfProtein;

  int width;

  JTextField residueField;
  
  LinkedHashSet<JRadioButton> names = new LinkedHashSet<JRadioButton>();
  
  int residue;
  
  JButton calculate;

  private JInternalFrame frame;

  final ComboBoxTooltipRenderer renderer = new ComboBoxTooltipRenderer();

  List<String> tips = new ArrayList<>();

  private AnalysisPanel analysisPanel;

  /**
   * Constructor
   * 
   * @param af
   */
  public AnalysisInput(AlignFrame alignFrame)
  {
    this(alignFrame, true);
  }
  public AnalysisInput(AlignFrame alignFrame, boolean doVars)
  {
    this.af = alignFrame;
    this.av = alignFrame.getViewport();
    this.width = this.av.getAlignment().getWidth();
    this.doVars = doVars;
    
    int prts = 0;
    for (SequenceI s : av.getAlignment().getSequences())
    {
      if (s.isProtein())
      {
        prts++;
      }
    }
    numberOfProtein = prts;
    
    namesOfProtein = new String[numberOfProtein];
    prts = 0;
    for (SequenceI s : av.getAlignment().getSequences())
    {
      if (s.isProtein())
      {
        namesOfProtein[prts++] = s.getName();
      }
    }
    
    //check if added (last) sequence in the alignment is protein (needs to be xNA)
    if (numberOfProtein == 0)
    {
      JvOptionPane.showInternalMessageDialog(Desktop.desktop, "No protein sequence added. Aborting.", "No Protein Error", JvOptionPane.ERROR_MESSAGE);
      throw new RuntimeException();
    }

    init();
    af.alignPanel.setAnalysisInput(this);
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
    
    //create the TextField
    residueField = new JTextField("1", 12);
    residueField.setOpaque(false);

    //creates a button for each gene/protein
    for (int i = 0; i < numberOfProtein; i++)
    {
      JRadioButton prot = new JRadioButton(namesOfProtein[i]);
      prot.setOpaque(false);

      if (i == 0)
        prot.setSelected(true);

      names.add(prot);
    }
    
    JPanel namesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    namesPanel.setOpaque(false);
    
    ButtonGroup oneNameOnly = new ButtonGroup();
    
    for (JRadioButton but : names)
    {
      namesPanel.add(but);
      oneNameOnly.add(but);
    }
    
    JPanel calcChoicePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    calcChoicePanel.setOpaque(false);

    //-- --

    JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    textPanel.setOpaque(false);
    
    JvSwingUtils.createTitledBorder(textPanel,
            MessageManager.getString("label.residue"), true); 
    textPanel.add(residueField);

    calcChoicePanel.add(textPanel);
    calcChoicePanel.add(namesPanel);
    
   //-- --

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
        af.alignPanel.setAnalysisInput(null);
      };
    });

    frame.setLayer(JLayeredPane.PALETTE_LAYER);
  }


  /**
   * Open and calculate the selected 
   */
  protected void calculate_actionPerformed()
  {
    residue = Integer.parseInt(residueField.getText());
    
    int index = 0;
    for (JRadioButton name : names)
    {
      if (name.isSelected())
        break;
    }
    
    String sequenceName = namesOfProtein[index];
    
    analysisPanel = new AnalysisPanel(av.getAlignPanel(), residue, doVars, sequenceName);
    new Thread(analysisPanel).start();
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

}