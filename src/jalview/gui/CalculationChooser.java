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

import jalview.analysis.TreeBuilder;
import jalview.analysis.scoremodels.ScoreModels;
import jalview.analysis.scoremodels.SimilarityParams;
import jalview.api.analysis.ScoreModelI;
import jalview.api.analysis.SimilarityParamsI;
import jalview.bin.Cache;
import jalview.datamodel.SequenceGroup;
import jalview.util.MessageManager;
import jalview.viewmodel.AlignmentViewport;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import jalview.analysis.TreeBuilder;
import jalview.analysis.scoremodels.ScoreModels;
import jalview.analysis.scoremodels.SimilarityParams;
import jalview.api.analysis.ScoreModelI;
import jalview.api.analysis.SimilarityParamsI;
import jalview.bin.Cache;
import jalview.datamodel.SequenceGroup;
import jalview.util.MessageManager;

/**
 * A dialog where a user can choose and action Tree or PCA calculation options
 */
public class CalculationChooser extends JPanel
{
  /*
   * flag for whether gap matches residue in the PID calculation for a Tree
   * - true gives Jalview 2.10.1 behaviour
   * - set to false (using Groovy) for a more correct tree
   * (JAL-374)
   */
  private static boolean treeMatchGaps = true;

  private static final Font VERDANA_11PT = new Font("Verdana", 0, 11);

  private static final int MIN_TREE_SELECTION = 3;

  private static final int MIN_PCA_SELECTION = 4;

  private static final int MIN_PASIMAP_SELECTION = 8; 

  AlignFrame af;

  JRadioButton pca;

  JRadioButton pasimap;	

  JRadioButton neighbourJoining;

  JRadioButton averageDistance;

  JComboBox<String> modelNames;

  JButton calculate;

  private JInternalFrame frame;

  private JCheckBox includeGaps;

  private JCheckBox matchGaps;

  private JCheckBox includeGappedColumns;

  private JCheckBox shorterSequence;

  final ComboBoxTooltipRenderer renderer = new ComboBoxTooltipRenderer();

  List<String> tips = new ArrayList<>();

  /*
   * the most recently opened PCA results panel
   */
  private PCAPanel pcaPanel;

  private PaSiMapPanel pasimapPanel;

  /**
   * Constructor
   * 
   * @param af
   */
  public CalculationChooser(AlignFrame alignFrame)
  {
    this.af = alignFrame;
    init();
    af.alignPanel.setCalculationDialog(this);
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
    /*
     * Layout consists of 3 or 4 panels:
     * - first with choice of PCA or tree method NJ or AV
     * - second with choice of score model
     * - third with score model parameter options [suppressed]
     * - fourth with OK and Cancel
     */
    pca = new JRadioButton(
            MessageManager.getString("label.principal_component_analysis"));
    pca.setOpaque(false);

    pasimap = new JRadioButton(			// create the JRadioButton for pasimap with label.pasimap as its text
	    MessageManager.getString("label.pasimap"));
    pasimap.setOpaque(false);

    neighbourJoining = new JRadioButton(
            MessageManager.getString("label.tree_calc_nj"));
    neighbourJoining.setSelected(true);
    neighbourJoining.setOpaque(false);

    averageDistance = new JRadioButton(
            MessageManager.getString("label.tree_calc_av"));
    averageDistance.setOpaque(false);

    JPanel calcChoicePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    calcChoicePanel.setOpaque(false);

    // first create the Tree calculation's border panel
    JPanel treePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    treePanel.setOpaque(false);

    JvSwingUtils.createTitledBorder(treePanel,
            MessageManager.getString("label.tree"), true);

    // then copy the inset dimensions for the border-less PCA panel
    JPanel pcaBorderless = new JPanel(new FlowLayout(FlowLayout.LEFT));
    Insets b = treePanel.getBorder().getBorderInsets(treePanel);
    pcaBorderless.setBorder(
            BorderFactory.createEmptyBorder(2, b.left, 2, b.right));
    pcaBorderless.setOpaque(false);

    pcaBorderless.add(pca, FlowLayout.LEFT);
    calcChoicePanel.add(pcaBorderless, FlowLayout.LEFT);

    // create pasimap panel
    JPanel pasimapBorderless = new JPanel(new FlowLayout(FlowLayout.LEFT)); 	// create new JPanel (button) for pasimap
    pasimapBorderless.setBorder(
	    BorderFactory.createEmptyBorder(2, b.left, 2, b.right)); 	// set border (margin) for button (same as treePanel and pca)
    pasimapBorderless.setOpaque(false);		// false -> stops every pixel inside border from being painted
    pasimapBorderless.add(pasimap, FlowLayout.LEFT);	// add pasimap button to the JPanel
    calcChoicePanel.add(pasimapBorderless, FlowLayout.LEFT);	// add button with border and everything to the overall ChoicePanel

    treePanel.add(neighbourJoining);
    treePanel.add(averageDistance);

    calcChoicePanel.add(treePanel);

    ButtonGroup calcTypes = new ButtonGroup();
    calcTypes.add(pca);
    calcTypes.add(pasimap);
    calcTypes.add(neighbourJoining);
    calcTypes.add(averageDistance);

    ActionListener calcChanged = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        validateCalcTypes();
      }
    };
    pca.addActionListener(calcChanged);
    pasimap.addActionListener(calcChanged);	// add the calcChanged ActionListener to pasimap --> <++> idk
    neighbourJoining.addActionListener(calcChanged);
    averageDistance.addActionListener(calcChanged);

    /*
     * score models drop-down - with added tooltips!
     */
    modelNames = buildModelOptionsList();

    JPanel scoreModelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    scoreModelPanel.setOpaque(false);
    scoreModelPanel.add(modelNames);

    /*
     * score model parameters
     */
    JPanel paramsPanel = new JPanel(new GridLayout(5, 1));
    paramsPanel.setOpaque(false);
    includeGaps = new JCheckBox("Include gaps");
    matchGaps = new JCheckBox("Match gaps");
    includeGappedColumns = new JCheckBox("Include gapped columns");
    shorterSequence = new JCheckBox("Match on shorter sequence");
    paramsPanel.add(new JLabel("Pairwise sequence scoring options"));
    paramsPanel.add(includeGaps);
    paramsPanel.add(matchGaps);
    paramsPanel.add(includeGappedColumns);
    paramsPanel.add(shorterSequence);

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
    calcChoicePanel.add(scoreModelPanel);
    if (includeParams)
    {
      scoreModelPanel.add(paramsPanel);
    }
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
        af.alignPanel.setCalculationDialog(null);
      };
    });

    validateCalcTypes();
    frame.setLayer(JLayeredPane.PALETTE_LAYER);
  }

  /**
   * enable calculations applicable for the current alignment or selection.
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
    boolean checkPca = checkEnabled(pca, size, MIN_PCA_SELECTION);
    boolean checkPasimap = checkEnabled(pasimap, size, MIN_PASIMAP_SELECTION); 	// check if pasimap is enabled and min_size is fulfilled
    boolean checkNeighbourJoining = checkEnabled(neighbourJoining, size,
            MIN_TREE_SELECTION);
    boolean checkAverageDistance = checkEnabled(averageDistance, size,
            MIN_TREE_SELECTION);

    if (checkPca || checkPasimap || checkNeighbourJoining || checkAverageDistance)
    {
      calculate.setToolTipText(null);
      calculate.setEnabled(true);
    }
    else
    {
      calculate.setEnabled(false);
    }
    updateScoreModels(modelNames, tips);
  }

  /**
   * Check the input and disable a calculation's radio button if necessary. A
   * tooltip is shown for disabled calculations.
   * 
   * @param calc
   *          - radio button for the calculation being validated
   * @param size
   *          - size of input to calculation
   * @param minsize
   *          - minimum size for calculation
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
      modelNames.setEnabled(calc.isEnabled());
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
   * A rather elaborate helper method (blame Swing, not me) that builds a
   * drop-down list of score models (by name) with descriptions as tooltips.
   * There is also a tooltip shown for the currently selected item when hovering
   * over it (without opening the list).
   */
  protected JComboBox<String> buildModelOptionsList()
  {
    final JComboBox<String> scoreModelsCombo = new JComboBox<>();
    scoreModelsCombo.setRenderer(renderer);

    /*
     * show tooltip on mouse over the combobox
     * note the listener has to be on the components that make up
     * the combobox, doesn't work if just on the combobox
     */
    final MouseAdapter mouseListener = new MouseAdapter()
    {
      @Override
      public void mouseEntered(MouseEvent e)
      {
        scoreModelsCombo.setToolTipText(
                tips.get(scoreModelsCombo.getSelectedIndex()));
      }

      @Override
      public void mouseExited(MouseEvent e)
      {
        scoreModelsCombo.setToolTipText(null);
      }
    };
    for (Component c : scoreModelsCombo.getComponents())
    {
      c.addMouseListener(mouseListener);
    }

    updateScoreModels(scoreModelsCombo, tips);

    /*
     * set the list of tooltips on the combobox's renderer
     */
    renderer.setTooltips(tips);

    return scoreModelsCombo;
  }

  private void updateScoreModels(JComboBox<String> comboBox,
          List<String> toolTips)
  {
    Object curSel = comboBox.getSelectedItem();
    toolTips.clear();
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();

    /*
     * select the score models applicable to the alignment type
     */
    boolean nucleotide = af.getViewport().getAlignment().isNucleotide();
    List<ScoreModelI> models = getApplicableScoreModels(nucleotide,
            pca.isSelected());

    /*
     * now we can actually add entries to the combobox,
     * remembering their descriptions for tooltips
     */
    boolean selectedIsPresent = false;
    for (ScoreModelI sm : models)
    {
      if (curSel != null && sm.getName().equals(curSel))
      {
        selectedIsPresent = true;
        curSel = sm.getName();
      }
      model.addElement(sm.getName());

      /*
       * tooltip is description if provided, else text lookup with
       * fallback on the model name
       */
      String tooltip = sm.getDescription();
      if (tooltip == null)
      {
        tooltip = MessageManager.getStringOrReturn("label.score_model_",
                sm.getName());
      }
      toolTips.add(tooltip);
    }

    if (selectedIsPresent)
    {
      model.setSelectedItem(curSel);
    }
    // finally, update the model
    comboBox.setModel(model);
  }

  /**
   * Builds a list of score models which are applicable for the alignment and
   * calculation type (peptide or generic models for protein, nucleotide or
   * generic models for nucleotide).
   * <p>
   * As a special case, includes BLOSUM62 as an extra option for nucleotide PCA.
   * This is for backwards compatibility with Jalview prior to 2.8 when BLOSUM62
   * was the only score matrix supported. This is included if property
   * BLOSUM62_PCA_FOR_NUCLEOTIDE is set to true in the Jalview properties file.
   * 
   * @param nucleotide
   * @param forPca
   * @return
   */
  protected static List<ScoreModelI> getApplicableScoreModels(
          boolean nucleotide, boolean forPca)
  {
    List<ScoreModelI> filtered = new ArrayList<>();

    ScoreModels scoreModels = ScoreModels.getInstance();
    for (ScoreModelI sm : scoreModels.getModels())
    {
      if (!nucleotide && sm.isProtein() || nucleotide && sm.isDNA())
      {
        filtered.add(sm);
      }
    }

    /*
     * special case: add BLOSUM62 as last option for nucleotide PCA, 
     * for backwards compatibility with Jalview < 2.8 (JAL-2962)
     */
    if (nucleotide && forPca
            && Cache.getDefault("BLOSUM62_PCA_FOR_NUCLEOTIDE", false))
    {
      filtered.add(scoreModels.getBlosum62());
    }

    return filtered;
  }

  /**
   * Open and calculate the selected tree or PCA on 'OK'
   */
  protected void calculate_actionPerformed()
  {
    boolean doPCA = pca.isSelected();
    boolean doPaSiMap = pasimap.isSelected();
    String modelName = modelNames.getSelectedItem().toString();
    SimilarityParamsI params = getSimilarityParameters(doPCA);

    if (doPCA && !doPaSiMap)
    {
      openPcaPanel(modelName, params);
    }
    else if (doPaSiMap && !doPCA)
    {
      openPasimapPanel(modelName, params);
    }
    else
    {
      openTreePanel(modelName, params);
    }

    closeFrame();
  }

  /**
   * Open a new Tree panel on the desktop
   * 
   * @param modelName
   * @param params
   */
  protected void openTreePanel(String modelName, SimilarityParamsI params)
  {
    /*
     * gui validation shouldn't allow insufficient sequences here, but leave
     * this check in in case this method gets exposed programmatically in future
     */
    AlignViewport viewport = af.getViewport();
    SequenceGroup sg = viewport.getSelectionGroup();
    if (sg != null && sg.getSize() < MIN_TREE_SELECTION)
    {
      JvOptionPane.showMessageDialog(Desktop.desktop,
              MessageManager.formatMessage(
                      "label.you_need_at_least_n_sequences",
                      MIN_TREE_SELECTION),
              MessageManager.getString("label.not_enough_sequences"),
              JvOptionPane.WARNING_MESSAGE);
      return;
    }

    String treeType = neighbourJoining.isSelected()
            ? TreeBuilder.NEIGHBOUR_JOINING
            : TreeBuilder.AVERAGE_DISTANCE;
    af.newTreePanel(treeType, modelName, params);
  }

  /**
   * Open a new PCA panel on the desktop
   * 
   * @param modelName
   * @param params
   */
  protected void openPcaPanel(String modelName, SimilarityParamsI params)
  {
    AlignViewport viewport = af.getViewport();

    /*
     * gui validation shouldn't allow insufficient sequences here, but leave
     * this check in in case this method gets exposed programmatically in future
     */
    if (((viewport.getSelectionGroup() != null)
            && (viewport.getSelectionGroup().getSize() < MIN_PCA_SELECTION)
            && (viewport.getSelectionGroup().getSize() > 0))
            || (viewport.getAlignment().getHeight() < MIN_PCA_SELECTION))
    {
      JvOptionPane.showInternalMessageDialog(this,
              MessageManager.formatMessage(
                      "label.you_need_at_least_n_sequences",
                      MIN_PCA_SELECTION),
              MessageManager
                      .getString("label.sequence_selection_insufficient"),
              JvOptionPane.WARNING_MESSAGE);
      return;
    }

    /*
     * construct the panel and kick off its calculation thread
     */
    pcaPanel = new PCAPanel(af.alignPanel, modelName, params);
    new Thread(pcaPanel).start();

  }

  /**
   * Open a new PaSiMap panel on the desktop
   * 
   * @param modelName
   * @param params
   */
  protected void openPasimapPanel(String modelName, SimilarityParamsI params)
  {
    AlignViewport viewport = af.getViewport();

    /*
     * gui validation shouldn't allow insufficient sequences here, but leave
     * this check in in case this method gets exposed programmatically in future
     */
    if (((viewport.getSelectionGroup() != null)
            && (viewport.getSelectionGroup().getSize() < MIN_PASIMAP_SELECTION)
            && (viewport.getSelectionGroup().getSize() > 0))
            || (viewport.getAlignment().getHeight() < MIN_PASIMAP_SELECTION))
    {
      JvOptionPane.showInternalMessageDialog(this,
              MessageManager.formatMessage(
                      "label.you_need_at_least_n_sequences",
                      MIN_PASIMAP_SELECTION),
              MessageManager
                      .getString("label.sequence_selection_insufficient"),
              JvOptionPane.WARNING_MESSAGE);
      return;
    }

    /*
     * construct the panel and kick off its calculation thread
     */
    pasimapPanel = new PaSiMapPanel(af.alignPanel, modelName, params);
    new Thread(pasimapPanel).start();

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
   * Returns a data bean holding parameters for similarity (or distance) model
   * calculation
   * 
   * @param doPCA
   * @return
   */
  protected SimilarityParamsI getSimilarityParameters(boolean doPCA)
  {
    // commented out: parameter choices read from gui widgets
    // SimilarityParamsI params = new SimilarityParams(
    // includeGappedColumns.isSelected(), matchGaps.isSelected(),
    // includeGaps.isSelected(), shorterSequence.isSelected());

    boolean includeGapGap = true;
    boolean includeGapResidue = true;
    boolean matchOnShortestLength = false;

    /*
     * 'matchGaps' flag is only used in the PID calculation
     * - set to false for PCA so that PCA using PID reproduces SeqSpace PCA
     * - set to true for Tree to reproduce Jalview 2.10.1 calculation
     * - set to false for Tree for a more correct calculation (JAL-374)
     */
    boolean matchGap = doPCA ? false : treeMatchGaps;

    return new SimilarityParams(includeGapGap, matchGap, includeGapResidue,
            matchOnShortestLength);
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

  public PCAPanel getPcaPanel()
  {
    return pcaPanel;
  }
}
