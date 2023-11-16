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
 * A dialog where a user can choose and action Tree or PCA custom options
 */
public class CustomChooser extends JPanel
{
  /*
   * flag for whether gap matches residue in the PID custom for a Tree
   * - true gives Jalview 2.10.1 behaviour
   * - set to false (using Groovy) for a more correct tree
   * (JAL-374)
   */
  private static final Font VERDANA_11PT = new Font("Verdana", 0, 11);

  private static final int MIN_NATURAL_FREQUENCIES_SELECTION = 3; 

  private static final int MIN_EQUIVALENT_POSITIONS_SELECTION = 3; 

  AlignFrame af;

  JRadioButton naturalFrequencies;	
  
  JRadioButton equivalentPositions;

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
    af.alignPanel.setCustomDialog(this);
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
    
    naturalFrequencies = new JRadioButton(MessageManager.getString("label.naturalfrequencies"));
    naturalFrequencies.setOpaque(false);
    
    equivalentPositions = new JRadioButton(MessageManager.getString("label.equivalentpositions"));
    equivalentPositions.setOpaque(false);


    JPanel calcChoicePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    calcChoicePanel.setOpaque(false);

    // first create the Tree custom's border panel
    JPanel customPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    customPanel.setOpaque(false);

    JvSwingUtils.createTitledBorder(customPanel,
            MessageManager.getString("label.tree"), true);

    // then copy the inset dimensions for the border-less PCA panel
    JPanel epPanelBorderless = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JPanel nfPanelBorderless = new JPanel(new FlowLayout(FlowLayout.LEFT));

    Insets b = customPanel.getBorder().getBorderInsets(customPanel);

    nfPanelBorderless.setBorder(BorderFactory.createEmptyBorder(2,b.left,2,b.right));
    nfPanelBorderless.setOpaque(false);
    nfPanelBorderless.add(naturalFrequencies, FlowLayout.LEFT);

    epPanelBorderless.setBorder(
            BorderFactory.createEmptyBorder(2, b.left, 2, b.right));
    epPanelBorderless.setOpaque(false);
    epPanelBorderless.add(equivalentPositions, FlowLayout.LEFT);

    calcChoicePanel.add(nfPanelBorderless, FlowLayout.LEFT);
    calcChoicePanel.add(epPanelBorderless, FlowLayout.LEFT);

    //calcChoicePanel.add(customPanel);

    ButtonGroup calcTypes = new ButtonGroup();
    calcTypes.add(naturalFrequencies);
    calcTypes.add(equivalentPositions);

    ActionListener calcChanged = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        validateCalcTypes();
      }
    };
    naturalFrequencies.addActionListener(calcChanged);
    equivalentPositions.addActionListener(calcChanged);	

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
    boolean checkNf = checkEnabled(naturalFrequencies, size, MIN_NATURAL_FREQUENCIES_SELECTION);
    boolean checkEp = checkEnabled(equivalentPositions, size, MIN_EQUIVALENT_POSITIONS_SELECTION); 

    if (checkNf || checkEp)
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
    boolean doNf = naturalFrequencies.isSelected();
    boolean doEp = equivalentPositions.isSelected();

    if (doNf && !doEp)
    {
      openNfPanel();
    }
    else if (doEp && !doNf)
    {
      openEpPanel();
    }

    closeFrame();
  }

  /**
   * Open a new PCA panel on the desktop
   * 
   * @param modelName
   * @param params
   */
  protected void openNfPanel()
  {
    AlignViewport viewport = af.getViewport();

    /*
     * gui validation shouldn't allow insufficient sequences here, but leave
     * this check in in case this method gets exposed programmatically in future
     */
    if (((viewport.getSelectionGroup() != null)
            && (viewport.getSelectionGroup().getSize() < MIN_NATURAL_FREQUENCIES_SELECTION)
            && (viewport.getSelectionGroup().getSize() > 0))
            || (viewport.getAlignment().getHeight() < MIN_NATURAL_FREQUENCIES_SELECTION))
    {
      JvOptionPane.showInternalMessageDialog(this,
              MessageManager.formatMessage(
                      "label.you_need_at_least_n_sequences",
                      MIN_NATURAL_FREQUENCIES_SELECTION),
              MessageManager
                      .getString("label.sequence_selection_insufficient"),
              JvOptionPane.WARNING_MESSAGE);
      return;
    }

    /*
     * construct the panel and kick off its custom thread
     */
    nfPanel = new NFPanel(af.alignPanel);
    new Thread(nfPanel).start();

  }

  /**
   * Open a new PaSiMap panel on the desktop
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
            && (viewport.getSelectionGroup().getSize() < MIN_EQUIVALENT_POSITIONS_SELECTION)
            && (viewport.getSelectionGroup().getSize() > 0))
            || (viewport.getAlignment().getHeight() < MIN_EQUIVALENT_POSITIONS_SELECTION))
    {
      JvOptionPane.showInternalMessageDialog(this,
              MessageManager.formatMessage(
                      "label.you_need_at_least_n_sequences",
                      MIN_EQUIVALENT_POSITIONS_SELECTION),
              MessageManager
                      .getString("label.sequence_selection_insufficient"),
              JvOptionPane.WARNING_MESSAGE);
      return;
    }

    /*
     * construct the panel and kick off its custom thread
     */
    new EpInput(af);
    //String parentSequenceFile = inputDialog.getParentSequenceFile();
    //int startPosition = inputDialog.getStart();
    //char FoR = inputDialog.getFoR();
    //epPanel = new EPPanel(af.alignPanel, parentSequenceFile, startPosition, FoR);
    //new Thread(epPanel).start();

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
