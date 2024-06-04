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

import jalview.util.MessageManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;


/**
 * A dialog where a user can choose and action Equivalent Positions, Natural Frequencies or Residue Analysis
 */
public class CustomChooser extends JPanel
{
  private static final Font VERDANA_11PT = new Font("Verdana", 0, 11);

  private static final int MIN_REFERENCE_SELECTION = 3; 

  private static final int MIN_ANALYSIS_SELECTION = 1;

  AlignFrame af;

  JRadioButton reference;
  
  JRadioButton analysis;
  
  JRadioButton yes;
  
  JRadioButton no;

  JButton calculate;

  private JInternalFrame frame;

  final ComboBoxTooltipRenderer renderer = new ComboBoxTooltipRenderer();

  List<String> tips = new ArrayList<>();

  private NFPanel nfPanel;
  
  /**
   * Constructor
   * 
   * @param af
   */
  public CustomChooser(AlignFrame alignFrame)
  {
    this.af = alignFrame;
    init();
    af.alignPanel.setCustomDialog(this);    // method needs to be set in gui/AlignFrame
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
        validateCalcTypes();
      }
    });
    
    //create graphical buttons for Reference and Analysis
    reference = new JRadioButton(MessageManager.getString("label.create_reference"));
    reference.setOpaque(false);
    
    analysis = new JRadioButton(MessageManager.getString("label.analysis"));
    analysis.setOpaque(false);

    yes = new JRadioButton(MessageManager.getString("action.yes"));
    yes.setOpaque(false);

    no = new JRadioButton(MessageManager.getString("action.no"));
    no.setOpaque(false);
    no.setSelected(true);

    JPanel calcChoicePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    calcChoicePanel.setOpaque(false);

    //calcChoicePanel.add(analysis, FlowLayout.LEFT);
    //calcChoicePanel.add(reference, FlowLayout.LEFT);
    
    JPanel referencePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    referencePanel.setOpaque(false);
    
    JvSwingUtils.createTitledBorder(referencePanel, MessageManager.getString("label.include_variants"), true);

    referencePanel.add(no, FlowLayout.LEFT);
    referencePanel.add(yes, FlowLayout.LEFT);

    Insets b = referencePanel.getBorder().getBorderInsets(referencePanel);

    JPanel analysisBorderless = new JPanel(new FlowLayout(FlowLayout.LEFT));
    analysisBorderless.setBorder(BorderFactory.createEmptyBorder(2, b.left, 2, b.right));
    analysisBorderless.add(analysis, FlowLayout.LEFT);
    analysisBorderless.setOpaque(false);
    
    JPanel referenceBorderless = new JPanel(new FlowLayout(FlowLayout.LEFT));
    analysisBorderless.setBorder(BorderFactory.createEmptyBorder(2, b.left, 2, b.right));
    referenceBorderless.add(reference, FlowLayout.LEFT);
    referenceBorderless.setOpaque(false);

    calcChoicePanel.add(analysisBorderless);
    calcChoicePanel.add(referenceBorderless);
    calcChoicePanel.add(referencePanel);

    ButtonGroup calcTypes = new ButtonGroup();
    calcTypes.add(analysis);
    calcTypes.add(reference);
    
    ButtonGroup yesno = new ButtonGroup();
    yesno.add(yes);
    yesno.add(no);

    ActionListener calcChanged = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        validateCalcTypes();
      }
    };
    reference.addActionListener(calcChanged);
    analysis.addActionListener(calcChanged);
    yes.addActionListener(calcChanged);
    no.addActionListener(calcChanged);

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
    this.add(calcChoicePanel, BorderLayout.CENTER);
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
        af.alignPanel.setCustomDialog(null);
      };
    });

    validateCalcTypes();
    frame.setLayer(JLayeredPane.PALETTE_LAYER);
  }

  /**
   * enable customs applicable for the current alignment or selection.
   */
  protected void validateCalcTypes()
  {
    int size = af.getViewport().getAlignment().getHeight();
    if (af.getViewport().getSelectionGroup() != null)
    {
      size = af.getViewport().getSelectionGroup().getSize();
    }

    /*
     * disable calc options for which there is insufficient input data
     * return value of true means enabled and selected
     */
    boolean checkReference = checkEnabled(reference, size, MIN_REFERENCE_SELECTION);
    boolean checkAnalysis = checkEnabled(analysis, size, MIN_ANALYSIS_SELECTION);

    if (checkReference || checkAnalysis)
    {
      calculate.setToolTipText(null);
      calculate.setEnabled(true);
    }
    else
    {
      calculate.setEnabled(false);
    }
  }

  /**
   * Check the input and disable a custom's radio button if necessary. A
   * tooltip is shown for disabled customs.
   * 
   * @param calc
   *          - radio button for the custom being validated
   * @param size
   *          - size of input to custom
   * @param minsize
   *          - minimum size for custom
   * @return true if size >= minsize and calc.isSelected
   */
  private boolean checkEnabled(JRadioButton calc, int size, int minsize)
  {
    String ttip = MessageManager
            .formatMessage("label.you_need_at_least_n_sequences", minsize);

    calc.setEnabled(size >= minsize);
    if (!calc.isEnabled())
    {
      calc.setToolTipText(ttip);
    }
    else
    {
      calc.setToolTipText(null);
    }
    if (calc.isSelected())
    {
      if (calc.isEnabled())
      {
        return true;
      }
      else
      {
        calculate.setToolTipText(ttip);
      }
    }
    return false;
  }

  /**
   * Open and calculate the selected 
   */
  protected void calculate_actionPerformed()
  {
    boolean doRef = reference.isSelected();
    boolean doAnal = analysis.isSelected();
    boolean doVars = yes.isSelected();

    if (doRef && !doAnal && doVars)
    {
      openEpPanel();  //reference with freqs
    } else if (doAnal && !doRef)
    {
      openAnalysisPanel();
    } else if (doRef && !doVars && !doAnal)
    {
      runFrequenciesOnlyReference();
    }

    closeFrame();
  }

  /**
   * Open a new EP panel on the desktop
   * 
   * @param modelName
   * @param params
   */
  protected void openEpPanel()
  {
    AlignViewport viewport = af.getViewport();

    /*
     * gui validation shouldn't allow insufficient sequences here, but leave
     * this check in in case this method gets exposed programmatically in future
     */
    if (((viewport.getSelectionGroup() != null)
            && (viewport.getSelectionGroup().getSize() < MIN_REFERENCE_SELECTION)
            && (viewport.getSelectionGroup().getSize() > 0))
            || (viewport.getAlignment().getHeight() < MIN_REFERENCE_SELECTION))
    {
      JvOptionPane.showInternalMessageDialog(this,
              MessageManager.formatMessage(
                      "label.you_need_at_least_n_sequences",
                      MIN_REFERENCE_SELECTION),
              MessageManager
                      .getString("label.sequence_selection_insufficient"),
              JvOptionPane.WARNING_MESSAGE);
      return;
    }

    /*
     * construct the panel and kick off its custom thread
     */
    new EpInput(af);
  }

  /**
   * Open a new Analysis panel on the desktop
   * 
   * @param modelName
   * @param params
   */
  protected void openAnalysisPanel()
  {
    AlignViewport viewport = af.getViewport();

    /*
     * gui validation shouldn't allow insufficient sequences here, but leave
     * this check in in case this method gets exposed programmatically in future
     */
    if (((viewport.getSelectionGroup() != null)
            && (viewport.getSelectionGroup().getSize() < MIN_ANALYSIS_SELECTION)
            && (viewport.getSelectionGroup().getSize() < 0))
            || (viewport.getAlignment().getHeight() < MIN_ANALYSIS_SELECTION))
    {
      JvOptionPane.showInternalMessageDialog(this,
              MessageManager.formatMessage(
                      "label.you_need_at_least_n_sequences",    
                      MIN_ANALYSIS_SELECTION),
              MessageManager
                      .getString("label.sequence_selection_insufficient"),
              JvOptionPane.WARNING_MESSAGE);
      return;
    }

    /*
     * construct the panel and kick off its custom thread
     */
    new AnalysisInput(af);

  }
  
  protected void runFrequenciesOnlyReference()
  {
    EPPanel epPanel = new EPPanel(af);
    new Thread(epPanel).start();
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
