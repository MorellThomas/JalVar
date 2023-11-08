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
package jalview.appletgui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.util.Vector;

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.HiddenColumns;
import jalview.schemes.AnnotationColourGradient;
import jalview.util.MessageManager;
import jalview.viewmodel.annotationfilter.AnnotationFilterParameter;

//import javax.swing.JPanel;

//import net.miginfocom.swing.MigLayout;

public class AnnotationColumnChooser extends AnnotationRowFilter implements
        ActionListener, AdjustmentListener, ItemListener, MouseListener
{

  private Choice annotations = new Choice();

  private Panel actionPanel = new Panel();

  private TitledPanel thresholdPanel = new TitledPanel();

  private Panel switchableViewsPanel = new Panel(new CardLayout());

  private CardLayout switchableViewsLayout = (CardLayout) (switchableViewsPanel
          .getLayout());

  private Panel noGraphFilterView = new Panel();

  private Panel graphFilterView = new Panel();

  private Panel annotationComboBoxPanel = new Panel();

  private BorderLayout borderLayout1 = new BorderLayout();

  private BorderLayout gBorderLayout = new BorderLayout();

  private BorderLayout ngBorderLayout = new BorderLayout();

  private Choice threshold = new Choice();

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

  public AnnotationColumnChooser()
  {
    try
    {
      jbInit();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public AnnotationColumnChooser(AlignViewport av, final AlignmentPanel ap)
  {
    super(av, ap);
    frame = new Frame();
    frame.add(this);
    jalview.bin.JalviewLite.addFrame(frame,
            MessageManager.getString("label.select_by_annotation"), 520,
            215);

    slider.addAdjustmentListener(this);
    slider.addMouseListener(this);

    AlignmentAnnotation[] anns = av.getAlignment().getAlignmentAnnotation();
    if (anns == null)
    {
      return;
    }
    setOldHiddenColumns(av.getAlignment().getHiddenColumns());
    adjusting = true;
    Vector<String> list = new Vector<>();
    int index = 1;
    for (int i = 0; i < anns.length; i++)
    {
      String label = anns[i].label;
      if (anns[i].sequenceRef != null)
      {
        label = label + "_" + anns[i].sequenceRef.getName();
      }
      if (!list.contains(label))
      {
        list.addElement(label);
      }
      else
      {
        list.addElement(label + "_" + (index++));
      }
    }

    for (int i = 0; i < list.size(); i++)
    {
      annotations.addItem(list.elementAt(i).toString());
    }

    populateThresholdComboBox(threshold);
    AnnotationColumnChooser lastChooser = av
            .getAnnotationColumnSelectionState();
    // restore Object state from the previous session if one exists
    if (lastChooser != null)
    {
      currentSearchPanel = lastChooser.getCurrentSearchPanel();
      currentStructureFilterPanel = lastChooser
              .getCurrentStructureFilterPanel();
      annotations.select(lastChooser.getAnnotations().getSelectedIndex());
      threshold.select(lastChooser.getThreshold().getSelectedIndex());
      actionOption = lastChooser.getActionOption();
      percentThreshold.setState(lastChooser.percentThreshold.getState());
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

  private void jbInit() throws Exception
  {
    ok.setLabel(MessageManager.getString("action.ok"));

    cancel.setLabel(MessageManager.getString("action.cancel"));

    thresholdValue.setEnabled(false);
    thresholdValue.setColumns(7);
    thresholdValue.setCaretPosition(0);

    ok.addActionListener(this);
    cancel.addActionListener(this);
    annotations.addItemListener(this);
    thresholdValue.addActionListener(this);
    threshold.addItemListener(this);

    slider.setBackground(Color.white);
    slider.setEnabled(false);
    slider.setPreferredSize(new Dimension(100, 32));

    thresholdPanel.setBackground(Color.white);
    // thresholdPanel.setFont(JvSwingUtils.getLabelFont());
    // thresholdPanel.setLayout(new MigLayout("", "[left][right]", "[][]"));

    percentThreshold.setLabel("As percentage");
    percentThreshold.addItemListener(this);

    actionPanel.setBackground(Color.white);
    // actionPanel.setFont(JvSwingUtils.getLabelFont());

    graphFilterView.setLayout(gBorderLayout);
    graphFilterView.setBackground(Color.white);

    noGraphFilterView.setLayout(ngBorderLayout);
    noGraphFilterView.setBackground(Color.white);

    annotationComboBoxPanel.setBackground(Color.white);
    // annotationComboBoxPanel.setFont(JvSwingUtils.getLabelFont());

    gSearchPanel = new SearchPanel(this);
    ngSearchPanel = new SearchPanel(this);
    gFurtherActionPanel = new FurtherActionPanel(this);
    ngFurtherActionPanel = new FurtherActionPanel(this);
    gStructureFilterPanel = new StructureFilterPanel(this);
    ngStructureFilterPanel = new StructureFilterPanel(this);

    thresholdPanel.setTitle("Threshold Filter");
    thresholdPanel.add(getThreshold());
    thresholdPanel.add(slider);
    thresholdPanel.add(thresholdValue);
    thresholdPanel.add(percentThreshold);

    actionPanel.add(ok);
    actionPanel.add(cancel);

    Panel staticPanel = new Panel();
    staticPanel.setLayout(new BorderLayout());
    staticPanel.setBackground(Color.white);

    staticPanel.add(gSearchPanel, java.awt.BorderLayout.NORTH);
    staticPanel.add(gStructureFilterPanel, java.awt.BorderLayout.SOUTH);

    graphFilterView.add(staticPanel, java.awt.BorderLayout.NORTH);
    graphFilterView.add(thresholdPanel, java.awt.BorderLayout.CENTER);
    graphFilterView.add(gFurtherActionPanel, java.awt.BorderLayout.SOUTH);

    noGraphFilterView.add(ngSearchPanel, java.awt.BorderLayout.PAGE_START);
    noGraphFilterView.add(ngStructureFilterPanel,
            java.awt.BorderLayout.CENTER);
    noGraphFilterView.add(ngFurtherActionPanel,
            java.awt.BorderLayout.CENTER);

    annotationComboBoxPanel.add(getAnnotations());
    switchableViewsPanel.add(noGraphFilterView,
            AnnotationColumnChooser.NO_GRAPH_VIEW);
    switchableViewsPanel.add(graphFilterView,
            AnnotationColumnChooser.GRAPH_VIEW);

    this.setLayout(borderLayout1);
    this.add(annotationComboBoxPanel, java.awt.BorderLayout.PAGE_START);
    this.add(switchableViewsPanel, java.awt.BorderLayout.CENTER);
    this.add(actionPanel, java.awt.BorderLayout.SOUTH);

    selectedAnnotationChanged();
    this.validate();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void reset()
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
  public void adjustmentValueChanged(AdjustmentEvent evt)
  {
    if (!adjusting)
    {
      setThresholdValueText();
      valueChanged(!sliderDragging);
    }
  }

  protected void addSliderMouseListeners()
  {

    slider.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        sliderDragging = true;
        super.mousePressed(e);
      }

      @Override
      public void mouseDragged(MouseEvent e)
      {
        sliderDragging = true;
        super.mouseDragged(e);
      }

      @Override
      public void mouseReleased(MouseEvent evt)
      {
        if (sliderDragging)
        {
          sliderDragging = false;
          valueChanged(true);
        }
        ap.paintAlignment(true, true);
      }
    });
  }

  @Override
  public void valueChanged(boolean updateAllAnnotation)
  {
    if (slider.isEnabled())
    {
      getCurrentAnnotation().threshold.value = slider.getValue() / 1000f;
      updateView(); // this also calls paintAlignment(true,true)
    }
  }

  public Choice getThreshold()
  {
    return threshold;
  }

  public void setThreshold(Choice threshold)
  {
    this.threshold = threshold;
  }

  public Choice getAnnotations()
  {
    return annotations;
  }

  public void setAnnotations(Choice annotations)
  {
    this.annotations = annotations;
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
            .getAlignmentAnnotation()[getAnnotations().getSelectedIndex()]);

    int selectedThresholdItem = getSelectedThresholdItem(
            getThreshold().getSelectedIndex());

    slider.setEnabled(true);
    thresholdValue.setEnabled(true);
    percentThreshold.setEnabled(true);

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
      if (getCurrentAnnotation().threshold == null)
      {
        getCurrentAnnotation().setThreshold(new jalview.datamodel.GraphLine(
                (getCurrentAnnotation().graphMax
                        - getCurrentAnnotation().graphMin) / 2f,
                "Threshold", Color.black));
      }

      adjusting = true;
      // float range = getCurrentAnnotation().graphMax * 1000
      // - getCurrentAnnotation().graphMin * 1000;

      slider.setMinimum((int) (getCurrentAnnotation().graphMin * 1000));
      slider.setMaximum((int) (getCurrentAnnotation().graphMax * 1000));
      slider.setValue(
              (int) (getCurrentAnnotation().threshold.value * 1000));
      setThresholdValueText();
      // slider.setMajorTickSpacing((int) (range / 10f));
      slider.setEnabled(true);
      thresholdValue.setEnabled(true);
      percentThreshold.setEnabled(true);
      adjusting = false;

      // build filter params
      filterParams.setThresholdType(
              AnnotationFilterParameter.ThresholdType.NO_THRESHOLD);
      if (getCurrentAnnotation().isQuantitative())
      {
        filterParams
                .setThresholdValue(getCurrentAnnotation().threshold.value);

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

    if (currentStructureFilterPanel != null)
    {
      if (currentStructureFilterPanel.alphaHelix.getState())
      {
        filterParams.setFilterAlphaHelix(true);
      }
      if (currentStructureFilterPanel.betaStrand.getState())
      {
        filterParams.setFilterBetaSheet(true);
      }
      if (currentStructureFilterPanel.turn.getState())
      {
        filterParams.setFilterTurn(true);
      }
    }

    if (currentSearchPanel != null)
    {

      if (!currentSearchPanel.searchBox.getText().isEmpty())
      {
        currentSearchPanel.description.setEnabled(true);
        currentSearchPanel.displayName.setEnabled(true);
        filterParams.setRegexString(currentSearchPanel.searchBox.getText());
        if (currentSearchPanel.displayName.getState())
        {
          filterParams.addRegexSearchField(
                  AnnotationFilterParameter.SearchableAnnotationField.DISPLAY_STRING);
        }
        if (currentSearchPanel.description.getState())
        {
          filterParams.addRegexSearchField(
                  AnnotationFilterParameter.SearchableAnnotationField.DESCRIPTION);
        }
      }
      else
      {
        currentSearchPanel.description.setEnabled(false);
        currentSearchPanel.displayName.setEnabled(false);
      }
    }

    // show hidden columns here, before changing the column selection in
    // filterAnnotations, because showing hidden columns has the side effect of
    // adding them to the selection
    av.showAllHiddenColumns();
    av.getColumnSelection().filterAnnotations(getCurrentAnnotation(),
            filterParams);

    if (getActionOption() == ACTION_OPTION_HIDE)
    {
      av.hideSelectedColumns();
    }

    filterParams = null;
    av.setAnnotationColumnSelectionState(this);
    av.sendSelection();
    ap.paintAlignment(true, true);
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

  @Override
  public void itemStateChanged(ItemEvent e)
  {
    if (e.getSource() == annotations)
    {
      selectedAnnotationChanged();
    }
    else if (e.getSource() == threshold)
    {
      threshold_actionPerformed(null);
    }
    else if (e.getSource() == percentThreshold)
    {
      if (!adjusting)
      {
        percentageValue_actionPerformed();
      }

    }
  }

  public void selectedAnnotationChanged()
  {
    String currentView = AnnotationColumnChooser.NO_GRAPH_VIEW;
    if (av.getAlignment().getAlignmentAnnotation()[getAnnotations()
            .getSelectedIndex()].isQuantitative())
    {
      currentView = AnnotationColumnChooser.GRAPH_VIEW;
    }

    gSearchPanel.syncState();
    gFurtherActionPanel.syncState();
    gStructureFilterPanel.syncState();

    ngSearchPanel.syncState();
    ngFurtherActionPanel.syncState();
    ngStructureFilterPanel.syncState();

    switchableViewsLayout.show(switchableViewsPanel, currentView);
    updateView();
  }

  public class FurtherActionPanel extends Panel implements ItemListener
  {
    private AnnotationColumnChooser aColChooser;

    private Choice furtherAction = new Choice();

    public FurtherActionPanel(AnnotationColumnChooser aColChooser)
    {
      this.aColChooser = aColChooser;
      furtherAction.addItem("Select");
      furtherAction.addItem("Hide");
      furtherAction.addItemListener(this);
      syncState();

      // this.setTitle("Filter Actions");
      // this.setFont(JvSwingUtils.getLabelFont());

      this.add(furtherAction);
    }

    public void syncState()
    {
      if (aColChooser
              .getActionOption() == AnnotationColumnChooser.ACTION_OPTION_HIDE)
      {
        furtherAction.select("Hide");
      }
      else
      {
        furtherAction.select("Select");
      }
    }

    @Override
    public void itemStateChanged(ItemEvent e)
    {
      aColChooser.setCurrentFutherActionPanel(this);
      if (furtherAction.getSelectedItem().equalsIgnoreCase("Select"))
      {
        setActionOption(ACTION_OPTION_SELECT);
        updateView();
      }
      else
      {
        setActionOption(ACTION_OPTION_HIDE);
        updateView();
      }

    }
  }

  public class StructureFilterPanel extends TitledPanel
          implements ItemListener
  {
    private AnnotationColumnChooser aColChooser;

    private Checkbox alphaHelix = new Checkbox();

    private Checkbox betaStrand = new Checkbox();

    private Checkbox turn = new Checkbox();

    private Checkbox all = new Checkbox();

    public StructureFilterPanel(AnnotationColumnChooser aColChooser)
    {
      this.aColChooser = aColChooser;

      alphaHelix.setLabel(MessageManager.getString("label.alpha_helix"));
      alphaHelix.setBackground(Color.white);

      alphaHelix.addItemListener(this);

      betaStrand.setLabel(MessageManager.getString("label.beta_strand"));
      betaStrand.setBackground(Color.white);
      betaStrand.addItemListener(this);

      turn.setLabel(MessageManager.getString("label.turn"));
      turn.setBackground(Color.white);
      turn.addItemListener(this);

      all.setLabel(MessageManager.getString("label.select_all"));
      all.setBackground(Color.white);
      all.addItemListener(this);

      this.setBackground(Color.white);
      this.setTitle("Structure Filter");
      // this.setFont(JvSwingUtils.getLabelFont());

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
      if (all.getState())
      {
        alphaHelix.setState(true);
        betaStrand.setState(true);
        turn.setState(true);
      }
      else
      {
        alphaHelix.setState(false);
        betaStrand.setState(false);
        turn.setState(false);
      }
      aColChooser.setCurrentStructureFilterPanel(this);
      aColChooser.updateView();
    }

    public void updateSelectAllState()
    {
      if (alphaHelix.getState() && betaStrand.getState() && turn.getState())
      {
        all.setState(true);
      }
      else
      {
        all.setState(false);
      }
    }

    public void syncState()
    {
      StructureFilterPanel sfp = aColChooser
              .getCurrentStructureFilterPanel();
      if (sfp != null)
      {
        alphaHelix.setState(sfp.alphaHelix.getState());
        betaStrand.setState(sfp.betaStrand.getState());
        turn.setState(sfp.turn.getState());
        if (sfp.all.getState())
        {
          all.setState(true);
          alphaHelix.setState(true);
          betaStrand.setState(true);
          turn.setState(true);
        }
      }

    }

    @Override
    public void itemStateChanged(ItemEvent e)
    {
      if (e.getSource() == alphaHelix)
      {
        alphaHelix_actionPerformed();
      }
      else if (e.getSource() == betaStrand)
      {
        betaStrand_actionPerformed();
      }
      else if (e.getSource() == turn)
      {
        turn_actionPerformed();
      }
      else if (e.getSource() == all)
      {
        all_actionPerformed();
      }
    }
  }

  public class SearchPanel extends TitledPanel implements ItemListener
  {
    private AnnotationColumnChooser aColChooser;

    private Checkbox displayName = new Checkbox();

    private Checkbox description = new Checkbox();

    private TextField searchBox = new TextField(10);

    public SearchPanel(AnnotationColumnChooser aColChooser)
    {

      this.aColChooser = aColChooser;
      searchBox.addTextListener(new TextListener()
      {

        @Override
        public void textValueChanged(TextEvent e)
        {
          searchStringAction();

        }

      });

      displayName.setLabel(MessageManager.getString("label.label"));
      displayName.setEnabled(false);
      displayName.addItemListener(this);

      description.setLabel(MessageManager.getString("label.description"));
      description.setEnabled(false);
      description.addItemListener(this);
      this.setTitle("Search Filter");
      // this.setFont(JvSwingUtils.getLabelFont());

      syncState();
      this.add(searchBox);
      this.add(displayName);
      this.add(description);
    }

    public void displayNameCheckboxAction()
    {
      aColChooser.setCurrentSearchPanel(this);
      aColChooser.updateView();
    }

    public void discriptionCheckboxAction()
    {
      aColChooser.setCurrentSearchPanel(this);
      aColChooser.updateView();
    }

    public void searchStringAction()
    {
      aColChooser.setCurrentSearchPanel(this);
      aColChooser.updateView();
    }

    public void syncState()
    {
      SearchPanel sp = aColChooser.getCurrentSearchPanel();
      if (sp != null)
      {
        description.setEnabled(sp.description.isEnabled());
        description.setState(sp.description.getState());

        displayName.setEnabled(sp.displayName.isEnabled());
        displayName.setState(sp.displayName.getState());

        searchBox.setText(sp.searchBox.getText());
      }
    }

    @Override
    public void itemStateChanged(ItemEvent e)
    {
      if (e.getSource() == displayName)
      {
        displayNameCheckboxAction();
      }
      else if (e.getSource() == description)
      {
        discriptionCheckboxAction();
      }

    }
  }

  @Override
  public void actionPerformed(ActionEvent evt)
  {

    if (evt.getSource() == ok)
    {
      ok_actionPerformed(null);
    }
    else if (evt.getSource() == cancel)
    {
      cancel_actionPerformed(null);
    }
    else if (evt.getSource() == thresholdValue)
    {
      thresholdValue_actionPerformed(null);
    }
    else
    {
      updateView();
    }
  }

  @Override
  public void mouseClicked(MouseEvent e)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void mousePressed(MouseEvent e)
  {
    if (e.getSource() == slider)
    {
      updateView();
    }

  }

  @Override
  public void mouseReleased(MouseEvent e)
  {
    if (e.getSource() == slider)
    {
      updateView();
    }
  }

  @Override
  public void mouseEntered(MouseEvent e)
  {
    if (e.getSource() == slider)
    {
      updateView();
    }
  }

  @Override
  public void mouseExited(MouseEvent e)
  {
    if (e.getSource() == slider)
    {
      updateView();
    }
  }

}
