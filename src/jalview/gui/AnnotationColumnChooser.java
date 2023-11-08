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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import jalview.bin.Console;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.HiddenColumns;
import jalview.io.cache.JvCacheableInputBox;
import jalview.schemes.AnnotationColourGradient;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.viewmodel.annotationfilter.AnnotationFilterParameter;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class AnnotationColumnChooser extends AnnotationRowFilter
        implements ItemListener
{
  private JPanel switchableViewsPanel = new JPanel(new CardLayout());

  private JPanel annotationComboBoxPanel = new JPanel();

  private StructureFilterPanel gStructureFilterPanel;

  private StructureFilterPanel ngStructureFilterPanel;

  private StructureFilterPanel currentStructureFilterPanel;

  private SearchPanel currentSearchPanel;

  private SearchPanel gSearchPanel;

  private SearchPanel ngSearchPanel;

  private FurtherActionPanel currentFurtherActionPanel;

  private FurtherActionPanel gFurtherActionPanel;

  private FurtherActionPanel ngFurtherActionPanel;

  public static final int ACTION_OPTION_SELECT = 1;

  public static int ACTION_OPTION_HIDE = 2;

  public static String NO_GRAPH_VIEW = "0";

  public static String GRAPH_VIEW = "1";

  private int actionOption = ACTION_OPTION_SELECT;

  private HiddenColumns oldHiddenColumns;

  protected static int MIN_WIDTH = (Platform.isJS() ? 370 : 420);

  protected static int MIN_HEIGHT = (Platform.isJS() ? 370 : 430);

  public AnnotationColumnChooser(AlignViewport av, final AlignmentPanel ap)
  {
    this(av,ap,null);
  }
  public AnnotationColumnChooser(AlignViewport av, final AlignmentPanel ap, AlignmentAnnotation selectedAnnotation)
  {
    super(av, ap);
    frame = new JInternalFrame();
    frame.setFrameIcon(null);
    frame.setContentPane(this);
    frame.setLayer(JLayeredPane.PALETTE_LAYER);
    Desktop.addInternalFrame(frame,
            MessageManager.getString("label.select_by_annotation"), 0, 0);
    // BH note: MIGLayout ignores this completely,
    // possibly creating a frame smaller than specified:
    frame.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));

    addSliderChangeListener();
    addSliderMouseListeners();

    if (av.getAlignment().getAlignmentAnnotation() == null)
    {
      return;
    }
    setOldHiddenColumns(av.getAlignment().getHiddenColumns());
    adjusting = true;
    
    setAnnotations(new JComboBox<>(getAnnotationItems(false)));
    populateThresholdComboBox(threshold);
    AnnotationColumnChooser lastChooser = av
            .getAnnotationColumnSelectionState();
    // restore Object state from the previous session if one exists
    if (lastChooser != null)
    {
      currentSearchPanel = lastChooser.getCurrentSearchPanel();
      currentStructureFilterPanel = lastChooser
              .getCurrentStructureFilterPanel();
      annotations.setSelectedIndex(
              lastChooser.getAnnotations().getSelectedIndex());
      threshold.setSelectedIndex(
              lastChooser.getThreshold().getSelectedIndex());
      actionOption = lastChooser.getActionOption();
      percentThreshold
              .setSelected(lastChooser.percentThreshold.isSelected());
    }
    if (selectedAnnotation!=null)
    {
      try {
        setCurrentAnnotation(selectedAnnotation);
        annotations.setSelectedItem(getAnnotationMenuLabel(selectedAnnotation));
      } catch (Exception x)
      {
        Console.error("Couldn't select annotation in column chooser",x);
      }
    }

    try
    {
      jbInit();
    } catch (Exception ex)
    {
    }
    adjusting = false;

    updateView();
    frame.invalidate();
    frame.pack();
  }

  @Override
  protected void jbInit()
  {
    super.jbInit();

    JPanel thresholdPanel = new JPanel();
    thresholdPanel.setBorder(new TitledBorder(
            MessageManager.getString("label.threshold_filter")));
    thresholdPanel.setBackground(Color.white);
    thresholdPanel.setFont(JvSwingUtils.getLabelFont());
    thresholdPanel.setLayout(new MigLayout("", "[left][right]", "[][]"));

    percentThreshold.setBackground(Color.white);
    percentThreshold.setFont(JvSwingUtils.getLabelFont());

    JPanel actionPanel = new JPanel();
    actionPanel.setBackground(Color.white);
    actionPanel.setFont(JvSwingUtils.getLabelFont());

    JPanel graphFilterView = new JPanel();
    graphFilterView.setLayout(new MigLayout("", "[left][right]", "[][]"));
    graphFilterView.setBackground(Color.white);

    JPanel noGraphFilterView = new JPanel();
    noGraphFilterView.setLayout(new MigLayout("", "[left][right]", "[][]"));
    noGraphFilterView.setBackground(Color.white);

    annotationComboBoxPanel.setBackground(Color.white);
    annotationComboBoxPanel.setFont(JvSwingUtils.getLabelFont());

    gSearchPanel = new SearchPanel(this);
    ngSearchPanel = new SearchPanel(this);
    gFurtherActionPanel = new FurtherActionPanel(this);
    ngFurtherActionPanel = new FurtherActionPanel(this);
    gStructureFilterPanel = new StructureFilterPanel(this);
    ngStructureFilterPanel = new StructureFilterPanel(this);

    thresholdPanel.add(getThreshold());
    thresholdPanel.add(percentThreshold, "wrap");
    thresholdPanel.add(slider, "grow");
    thresholdPanel.add(thresholdValue, "span, wrap");

    actionPanel.add(ok);
    actionPanel.add(cancel);

    graphFilterView.add(gSearchPanel, "grow, span, wrap");
    graphFilterView.add(gStructureFilterPanel, "grow, span, wrap");
    graphFilterView.add(thresholdPanel, "grow, span, wrap");
    graphFilterView.add(gFurtherActionPanel);

    noGraphFilterView.add(ngSearchPanel, "grow, span, wrap");
    noGraphFilterView.add(ngStructureFilterPanel, "grow, span, wrap");
    noGraphFilterView.add(ngFurtherActionPanel);

    annotationComboBoxPanel.add(getAnnotations());
    switchableViewsPanel.add(noGraphFilterView,
            AnnotationColumnChooser.NO_GRAPH_VIEW);
    switchableViewsPanel.add(graphFilterView,
            AnnotationColumnChooser.GRAPH_VIEW);
    this.setLayout(new BorderLayout());
    this.add(annotationComboBoxPanel, java.awt.BorderLayout.PAGE_START);
    this.add(switchableViewsPanel, java.awt.BorderLayout.CENTER);
    this.add(actionPanel, java.awt.BorderLayout.SOUTH);

    selectedAnnotationChanged();
    updateThresholdPanelToolTip();
    this.validate();
  }

  protected void updateThresholdPanelToolTip()
  {
    thresholdValue.setToolTipText("");
    slider.setToolTipText("");

    String defaultTtip = MessageManager
            .getString("info.change_threshold_mode_to_enable");

    String thresh = getThreshold().getSelectedItem().toString();
    if (thresh.equalsIgnoreCase("No Threshold"))
    {
      thresholdValue.setToolTipText(defaultTtip);
      slider.setToolTipText(defaultTtip);
    }
  }

  @Override
  protected void reset()
  {
    if (this.getOldHiddenColumns() != null)
    {
      av.getColumnSelection().clear();

      if (av.getAnnotationColumnSelectionState() != null)
      {
        HiddenColumns oldHidden = av.getAnnotationColumnSelectionState()
                .getOldHiddenColumns();
        av.getAlignment().setHiddenColumns(oldHidden);
      }
      av.sendSelection();
      ap.paintAlignment(true, true);
    }
  }

  @Override
  public void valueChanged(boolean updateAllAnnotation)
  {
    if (slider.isEnabled())
    {
      getCurrentAnnotation().threshold.value = getSliderValue();
      updateView();
      propagateSeqAssociatedThreshold(updateAllAnnotation,
              getCurrentAnnotation());
      ap.paintAlignment(false, false);
    }
  }

  @Override
  public void updateView()
  {
    // Check if combobox is still adjusting
    if (adjusting)
    {
      return;
    }

    AnnotationFilterParameter filterParams = new AnnotationFilterParameter();

    setCurrentAnnotation(av.getAlignment()
            .getAlignmentAnnotation()[annmap[getAnnotations()
                    .getSelectedIndex()]]);

    int selectedThresholdItem = getSelectedThresholdItem(
            getThreshold().getSelectedIndex());

    slider.setEnabled(true);
    thresholdValue.setEnabled(true);
    percentThreshold.setEnabled(true);

    final AlignmentAnnotation currentAnnotation = getCurrentAnnotation();
    if (selectedThresholdItem == AnnotationColourGradient.NO_THRESHOLD)
    {
      slider.setEnabled(false);
      thresholdValue.setEnabled(false);
      thresholdValue.setText("");
      percentThreshold.setEnabled(false);
      // build filter params
    }
    else if (selectedThresholdItem != AnnotationColourGradient.NO_THRESHOLD)
    {
      if (currentAnnotation.threshold == null)
      {
        currentAnnotation.setThreshold(new jalview.datamodel.GraphLine(
                (currentAnnotation.graphMax - currentAnnotation.graphMin)
                        / 2f,
                "Threshold", Color.black));
      }

      adjusting = true;

      setSliderModel(currentAnnotation.graphMin, currentAnnotation.graphMax,
              currentAnnotation.threshold.value);

      setThresholdValueText();

      slider.setEnabled(true);
      thresholdValue.setEnabled(true);
      adjusting = false;

      // build filter params
      filterParams.setThresholdType(
              AnnotationFilterParameter.ThresholdType.NO_THRESHOLD);
      if (currentAnnotation.isQuantitative())
      {
        filterParams.setThresholdValue(currentAnnotation.threshold.value);

        if (selectedThresholdItem == AnnotationColourGradient.ABOVE_THRESHOLD)
        {
          filterParams.setThresholdType(
                  AnnotationFilterParameter.ThresholdType.ABOVE_THRESHOLD);
        }
        else if (selectedThresholdItem == AnnotationColourGradient.BELOW_THRESHOLD)
        {
          filterParams.setThresholdType(
                  AnnotationFilterParameter.ThresholdType.BELOW_THRESHOLD);
        }
      }
    }

    updateThresholdPanelToolTip();
    if (currentStructureFilterPanel != null)
    {
      if (currentStructureFilterPanel.alphaHelix.isSelected())
      {
        filterParams.setFilterAlphaHelix(true);
      }
      if (currentStructureFilterPanel.betaStrand.isSelected())
      {
        filterParams.setFilterBetaSheet(true);
      }
      if (currentStructureFilterPanel.turn.isSelected())
      {
        filterParams.setFilterTurn(true);
      }
    }

    if (currentSearchPanel != null)
    {
      if (!currentSearchPanel.searchBox.getUserInput().isEmpty())
      {
        filterParams.setRegexString(
                currentSearchPanel.searchBox.getUserInput());
        if (currentSearchPanel.displayName.isSelected())
        {
          filterParams.addRegexSearchField(
                  AnnotationFilterParameter.SearchableAnnotationField.DISPLAY_STRING);
        }
        if (currentSearchPanel.description.isSelected())
        {
          filterParams.addRegexSearchField(
                  AnnotationFilterParameter.SearchableAnnotationField.DESCRIPTION);
        }
      }
    }

    // show hidden columns here, before changing the column selection in
    // filterAnnotations, because showing hidden columns has the side effect of
    // adding them to the selection
    av.showAllHiddenColumns();
    av.getColumnSelection().filterAnnotations(currentAnnotation,
            filterParams);

    boolean hideCols = getActionOption() == ACTION_OPTION_HIDE;
    if (hideCols)
    {
      av.hideSelectedColumns();
    }
    av.sendSelection();

    filterParams = null;
    av.setAnnotationColumnSelectionState(this);
    // only update overview and structures if columns were hidden
    ap.paintAlignment(hideCols, hideCols);
  }

  public HiddenColumns getOldHiddenColumns()
  {
    return oldHiddenColumns;
  }

  public void setOldHiddenColumns(HiddenColumns currentHiddenColumns)
  {
    if (currentHiddenColumns != null)
    {
      this.oldHiddenColumns = new HiddenColumns(currentHiddenColumns);
    }
  }

  public FurtherActionPanel getCurrentFutherActionPanel()
  {
    return currentFurtherActionPanel;
  }

  public void setCurrentFutherActionPanel(
          FurtherActionPanel currentFutherActionPanel)
  {
    this.currentFurtherActionPanel = currentFutherActionPanel;
  }

  public SearchPanel getCurrentSearchPanel()
  {
    return currentSearchPanel;
  }

  public void setCurrentSearchPanel(SearchPanel currentSearchPanel)
  {
    this.currentSearchPanel = currentSearchPanel;
  }

  public int getActionOption()
  {
    return actionOption;
  }

  public void setActionOption(int actionOption)
  {
    this.actionOption = actionOption;
  }

  public StructureFilterPanel getCurrentStructureFilterPanel()
  {
    return currentStructureFilterPanel;
  }

  public void setCurrentStructureFilterPanel(
          StructureFilterPanel currentStructureFilterPanel)
  {
    this.currentStructureFilterPanel = currentStructureFilterPanel;
  }

  public void select_action(ActionEvent actionEvent)
  {
    JRadioButton radioButton = (JRadioButton) actionEvent.getSource();
    if (radioButton.isSelected())
    {
      setActionOption(ACTION_OPTION_SELECT);
      updateView();
    }
  }

  public void hide_action(ActionEvent actionEvent)
  {
    JRadioButton radioButton = (JRadioButton) actionEvent.getSource();
    if (radioButton.isSelected())
    {
      setActionOption(ACTION_OPTION_HIDE);
      updateView();
    }
  }

  @Override
  public void itemStateChanged(ItemEvent e)
  {
    selectedAnnotationChanged();
  }

  @Override
  public void selectedAnnotationChanged()
  {
    String currentView = AnnotationColumnChooser.NO_GRAPH_VIEW;
    if (av.getAlignment().getAlignmentAnnotation()[annmap[getAnnotations()
            .getSelectedIndex()]].isQuantitative())
    {
      currentView = AnnotationColumnChooser.GRAPH_VIEW;
    }
    saveCache();
    gSearchPanel.syncState();
    gFurtherActionPanel.syncState();
    gStructureFilterPanel.syncState();

    ngSearchPanel.syncState();
    ngFurtherActionPanel.syncState();
    ngStructureFilterPanel.syncState();

    CardLayout switchableViewsLayout = (CardLayout) switchableViewsPanel
            .getLayout();
    switchableViewsLayout.show(switchableViewsPanel, currentView);
    updateView();
  }

  public class FurtherActionPanel extends JPanel
  {
    private AnnotationColumnChooser aColChooser;

    private JRadioButton hideOption = new JRadioButton();

    private JRadioButton selectOption = new JRadioButton();

    private ButtonGroup optionsGroup = new ButtonGroup();

    public FurtherActionPanel(AnnotationColumnChooser aColChooser)
    {
      this.aColChooser = aColChooser;
      JvSwingUtils.jvInitComponent(selectOption, "action.select");
      selectOption.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          selectRadioAction(actionEvent);
        }
      });

      JvSwingUtils.jvInitComponent(hideOption, "action.hide");
      hideOption.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          hideRadioAction(actionEvent);
        }
      });

      optionsGroup.add(selectOption);
      optionsGroup.add(hideOption);
      optionsGroup.setSelected(selectOption.getModel(), true);

      JvSwingUtils.jvInitComponent(this);
      syncState();

      this.add(selectOption);
      this.add(hideOption);
    }

    public void selectRadioAction(ActionEvent actionEvent)
    {
      aColChooser.setCurrentFutherActionPanel(this);
      aColChooser.select_action(actionEvent);
    }

    public void hideRadioAction(ActionEvent actionEvent)
    {
      aColChooser.setCurrentFutherActionPanel(this);
      aColChooser.hide_action(actionEvent);
    }

    public void syncState()
    {
      if (aColChooser
              .getActionOption() == AnnotationColumnChooser.ACTION_OPTION_HIDE)
      {
        this.optionsGroup.setSelected(this.hideOption.getModel(), true);
      }
      else
      {
        this.optionsGroup.setSelected(this.selectOption.getModel(), true);
      }
    }
  }

  public class StructureFilterPanel extends JPanel
  {
    private AnnotationColumnChooser aColChooser;

    private JCheckBox alphaHelix = new JCheckBox();

    private JCheckBox betaStrand = new JCheckBox();

    private JCheckBox turn = new JCheckBox();

    private JCheckBox all = new JCheckBox();

    public StructureFilterPanel(AnnotationColumnChooser aColChooser)
    {
      this.aColChooser = aColChooser;

      JvSwingUtils.jvInitComponent(alphaHelix, "label.alpha_helix");
      alphaHelix.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          alphaHelix_actionPerformed();
        }
      });

      JvSwingUtils.jvInitComponent(betaStrand, "label.beta_strand");
      betaStrand.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          betaStrand_actionPerformed();
        }
      });

      JvSwingUtils.jvInitComponent(turn, "label.turn");
      turn.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          turn_actionPerformed();
        }
      });

      JvSwingUtils.jvInitComponent(all, "label.select_all");
      all.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          all_actionPerformed();
        }
      });

      this.setBorder(new TitledBorder(
              MessageManager.getString("label.structures_filter")));
      JvSwingUtils.jvInitComponent(this);

      this.add(all);
      this.add(alphaHelix);
      this.add(betaStrand);
      this.add(turn);
    }

    public void alphaHelix_actionPerformed()
    {
      updateSelectAllState();
      aColChooser.setCurrentStructureFilterPanel(this);
      aColChooser.updateView();
    }

    public void betaStrand_actionPerformed()
    {
      updateSelectAllState();
      aColChooser.setCurrentStructureFilterPanel(this);
      aColChooser.updateView();
    }

    public void turn_actionPerformed()
    {
      updateSelectAllState();
      aColChooser.setCurrentStructureFilterPanel(this);
      aColChooser.updateView();
    }

    public void all_actionPerformed()
    {
      if (all.isSelected())
      {
        alphaHelix.setSelected(true);
        betaStrand.setSelected(true);
        turn.setSelected(true);
      }
      else
      {
        alphaHelix.setSelected(false);
        betaStrand.setSelected(false);
        turn.setSelected(false);
      }
      aColChooser.setCurrentStructureFilterPanel(this);
      aColChooser.updateView();
    }

    public void updateSelectAllState()
    {
      if (alphaHelix.isSelected() && betaStrand.isSelected()
              && turn.isSelected())
      {
        all.setSelected(true);
      }
      else
      {
        all.setSelected(false);
      }
    }

    public void syncState()
    {
      StructureFilterPanel sfp = aColChooser
              .getCurrentStructureFilterPanel();
      if (sfp != null)
      {
        alphaHelix.setSelected(sfp.alphaHelix.isSelected());
        betaStrand.setSelected(sfp.betaStrand.isSelected());
        turn.setSelected(sfp.turn.isSelected());
        if (sfp.all.isSelected())
        {
          all.setSelected(true);
          alphaHelix.setSelected(true);
          betaStrand.setSelected(true);
          turn.setSelected(true);
        }
      }

    }
  }

  public class SearchPanel extends JPanel
  {
    private AnnotationColumnChooser aColChooser;

    private JCheckBox displayName = new JCheckBox();

    private JCheckBox description = new JCheckBox();

    private static final String FILTER_BY_ANN_CACHE_KEY = "CACHE.SELECT_FILTER_BY_ANNOT";

    public JvCacheableInputBox<String> searchBox = new JvCacheableInputBox<>(
            FILTER_BY_ANN_CACHE_KEY, 23);

    public SearchPanel(AnnotationColumnChooser aColChooser)
    {

      this.aColChooser = aColChooser;
      JvSwingUtils.jvInitComponent(this);
      this.setBorder(new TitledBorder(
              MessageManager.getString("label.search_filter")));

      searchBox.getComponent().setToolTipText(
              MessageManager.getString("info.enter_search_text_here"));
      searchBox.addKeyListener(new java.awt.event.KeyAdapter()
      {
        @Override
        public void keyPressed(KeyEvent e)
        {
          if (e.getKeyCode() == KeyEvent.VK_ENTER)
          {
            e.consume();
            searchStringAction();
          }
        }
      });
      searchBox.addFocusListener(new FocusAdapter()
      {
        @Override
        public void focusLost(FocusEvent e)
        {
          searchStringAction();
        }
      });

      JvSwingUtils.jvInitComponent(displayName, "label.label");
      displayName.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          displayNameCheckboxAction();
        }
      });

      JvSwingUtils.jvInitComponent(description, "label.description");
      description.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          descriptionCheckboxAction();
        }
      });

      syncState();
      this.add(searchBox.getComponent());
      this.add(displayName);
      this.add(description);
    }

    public void displayNameCheckboxAction()
    {
      aColChooser.setCurrentSearchPanel(this);
      aColChooser.updateView();
    }

    public void descriptionCheckboxAction()
    {
      aColChooser.setCurrentSearchPanel(this);
      aColChooser.updateView();
    }

    public void searchStringAction()
    {
      aColChooser.setCurrentSearchPanel(this);
      aColChooser.updateView();
      updateSearchPanelToolTips();
      searchBox.updateCache();
    }

    public void syncState()
    {
      SearchPanel sp = aColChooser.getCurrentSearchPanel();
      if (sp != null)
      {
        description.setEnabled(sp.description.isEnabled());
        description.setSelected(sp.description.isSelected());

        displayName.setEnabled(sp.displayName.isEnabled());
        displayName.setSelected(sp.displayName.isSelected());

        searchBox.setSelectedItem(sp.searchBox.getUserInput());
      }
      updateSearchPanelToolTips();
    }

    public void updateSearchPanelToolTips()
    {
      String defaultTtip = MessageManager
              .getString("info.enter_search_text_to_enable");
      String labelTtip = MessageManager.formatMessage(
              "info.search_in_annotation_label",
              annotations.getSelectedItem().toString());
      String descTtip = MessageManager.formatMessage(
              "info.search_in_annotation_description",
              annotations.getSelectedItem().toString());
      displayName.setToolTipText(
              displayName.isEnabled() ? labelTtip : defaultTtip);
      description.setToolTipText(
              description.isEnabled() ? descTtip : defaultTtip);
    }
  }

  @Override
  public void ok_actionPerformed()
  {
    saveCache();
    super.ok_actionPerformed();
  }

  @Override
  public void cancel_actionPerformed()
  {
    saveCache();
    super.cancel_actionPerformed();
  }

  private void saveCache()
  {
    gSearchPanel.searchBox.persistCache();
    ngSearchPanel.searchBox.persistCache();
    gSearchPanel.searchBox.updateCache();
    ngSearchPanel.searchBox.updateCache();
  }

  public static void displayFor(AlignViewport av, AlignmentPanel ap,
          AlignmentAnnotation alignmentAnnotation)
  {
    AnnotationColumnChooser colchooser = new AnnotationColumnChooser(av, ap, alignmentAnnotation);    
  }
}
