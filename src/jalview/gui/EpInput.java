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
import java.util.Arrays;
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
 * A dialog where a user has to input information necessary for the equivalent position calculation
 * called by CustomChooser
 */
public class EpInput extends JPanel
{
  private static final Font VERDANA_11PT = new Font("Verdana", 0, 11);
  
  private final int numberOfGenes;
  
  private final String[] namesOfGenes;

  AlignFrame af;
  
  AlignViewport av;
  
  int width;

  LinkedHashSet<JRadioButton> forward = new LinkedHashSet<JRadioButton>();	
  
  LinkedHashSet<JRadioButton> reverse = new LinkedHashSet<JRadioButton>();
  
  char[] FoR;
  
  LinkedHashSet<JTextField> startPoint = new LinkedHashSet<JTextField>();
  
  int[] startPosition;
  
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
  public EpInput(AlignFrame alignFrame, int width)
  {
    this.af = alignFrame;
    this.av = alignFrame.getViewport();
    this.width = width;
    
    int gns = 0;
    for (SequenceI s : av.getAlignment().getSequences())
    {
      if (!s.isProtein())
      {
        gns++;
      }
    }
    numberOfGenes = gns;
    
    namesOfGenes = new String[numberOfGenes];
    gns = 0;
    for (SequenceI s : av.getAlignment().getSequences())
    {
      if (!s.isProtein())
      {
        namesOfGenes[gns++] = s.getName();
      }
    }
    
    //check if added (last) sequence in the alignment is protein (needs to be xNA)
    if (numberOfGenes == 0)
    {
      JvOptionPane.showInternalMessageDialog(Desktop.desktop, "No nucleotide sequence added. Aborting.", "No Nucleotide Error", JvOptionPane.ERROR_MESSAGE);
      throw new RuntimeException();
    }

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
    for (int i = 0; i < numberOfGenes; i++)
    {
      JRadioButton frwrd = new JRadioButton(MessageManager.getString("label.forward"));
      frwrd.setOpaque(false);
      frwrd.setSelected(true);
      forward.add(frwrd);
      
      JRadioButton rvrs = new JRadioButton(MessageManager.getString("label.reverse"));
      rvrs.setOpaque(false);
      reverse.add(rvrs);
      
      JTextField strt = new JTextField("1", 10);
      strt.setOpaque(false);
      startPoint.add(strt);
    }

    JPanel[] calcChoicePanel = new JPanel[numberOfGenes];
    for (int i = 0; i < numberOfGenes; i++)
    {
      JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
      p.setOpaque(false);
      JvSwingUtils.createTitledBorder(p, namesOfGenes[i], true);
      calcChoicePanel[i] = p;
    }

    for (int i = 0; i < numberOfGenes; i++)
    {
      JTextField start = new JTextField("1", 10);
      JRadioButton forw = new JRadioButton(MessageManager.getString("label.forward"));
      JRadioButton revr = new JRadioButton(MessageManager.getString("label.reverse"));
      int k = 0;
      for (JTextField strt :startPoint)
      {
        if (k == i)
        {
          start = strt;
          break;
        }
        k++;
      }
      
      k = 0;
      for (JRadioButton frwrd : forward)
      {
        if (k == i)
        {
          forw = frwrd;
          break;
        }
        k++;
      }
      
      k = 0;
      for (JRadioButton rvrs : reverse)
      {
        if (k == i)
        {
          revr = rvrs;
          break;
        }
        k++;
      }
      
      JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      textPanel.setOpaque(false);
      JvSwingUtils.createTitledBorder(textPanel,
          MessageManager.getString("label.start_position"), true);    //set text border

      JPanel strandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      strandPanel.setOpaque(false);

      JvSwingUtils.createTitledBorder(strandPanel,
            MessageManager.getString("label.strand"), true);

      textPanel.add(start);
      strandPanel.add(forw);
      strandPanel.add(revr);
      
      
      calcChoicePanel[i].add(textPanel);
      calcChoicePanel[i].add(strandPanel);
    }
    
   //-- --

    LinkedHashSet<ButtonGroup> forOrRev = new LinkedHashSet<ButtonGroup>();
            
    int i = 0;
    for (JRadioButton frwrd : forward)
    {
      ButtonGroup forBG = new ButtonGroup();
      int j = 0;
      for (JRadioButton rvrs : reverse)
      {
        if (i == j)
        {
          forBG.add(frwrd);
          forBG.add(rvrs);
          forOrRev.add(forBG);
        }
        j++;
      }
      i++;
    }

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

    int cntCols = Math.floorDiv(numberOfGenes, 10) + 1;
    int width = 350 * cntCols;
    int height = (100 * (Math.floorDiv(numberOfGenes, cntCols))) + 70;
    

    JPanel genesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    genesPanel.setPreferredSize(new Dimension(width , height-70));
    genesPanel.setOpaque(false);

    for (int j = 0; j < numberOfGenes; j++)
    {
      genesPanel.add(calcChoicePanel[j], BorderLayout.CENTER);
    }
    
    genesPanel.doLayout();
    this.add(genesPanel, BorderLayout.NORTH);
    this.add(actionPanel, BorderLayout.SOUTH);

    setMinimumSize(new Dimension(325, height - 10));
    String title = MessageManager.getString("label.choose_calculation");
    if (af.getViewport().getViewName() != null)
    {
      title = title + " (" + af.getViewport().getViewName() + ")";
    }

    Desktop.addInternalFrame(frame, title, width, height, false);
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
    FoR = new char[forward.size()];
    startPosition = new int[startPoint.size()];
    
    JRadioButton[] fs = new JRadioButton[forward.size()];
    JRadioButton[] rs = new JRadioButton[reverse.size()];
    JTextField[] ss = new JTextField[startPoint.size()];
    
    int k = 0;
    for (JRadioButton f : forward)
    {
      fs[k++] = f;
    }
    k = 0;
    for (JRadioButton r : reverse)
    {
      rs[k++] = r;
    }
    k = 0;
    for (JTextField s : startPoint)
    {
      ss[k++] = s;
    }

    for ( int i = 0; i < forward.size(); i++)
    {
      boolean F = fs[i].isSelected();
      boolean R = rs[i].isSelected();

      if (F && !R)
      {
        FoR[i] = 'F';
      }
      else
      {
        FoR[i] = 'R';
      }
      startPosition[i] = Integer.parseInt(ss[i].getText());
    }
    
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
  
  protected char[] getFoR()
  {
    return FoR;
  }
  
  protected int[] getStart()
  {
    return startPosition;
  }

}