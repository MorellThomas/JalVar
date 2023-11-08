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

import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.api.FeatureColourI;
import jalview.bin.Console;
import jalview.datamodel.GraphLine;
import jalview.datamodel.features.FeatureAttributes;
import jalview.datamodel.features.FeatureAttributes.Datatype;
import jalview.datamodel.features.FeatureMatcher;
import jalview.datamodel.features.FeatureMatcherI;
import jalview.datamodel.features.FeatureMatcherSet;
import jalview.datamodel.features.FeatureMatcherSetI;
import jalview.gui.JalviewColourChooser.ColourChooserListener;
import jalview.schemes.FeatureColour;
import jalview.util.ColorUtils;
import jalview.util.MessageManager;
import jalview.util.matcher.Condition;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A dialog where the user can configure colour scheme, and any filters, for one
 * feature type
 * <p>
 * (Was FeatureColourChooser prior to Jalview 1.11, renamed with the addition of
 * filter options)
 */
public class FeatureTypeSettings extends JalviewDialog
{
  private final static MathContext FOUR_SIG_FIG = new MathContext(4);

  private final static String LABEL_18N = MessageManager
          .getString("label.label");

  private final static String SCORE_18N = MessageManager
          .getString("label.score");

  private static final int RADIO_WIDTH = 130;

  private static final String COLON = ":";

  private static final int MAX_TOOLTIP_LENGTH = 50;

  private static final int NO_COLOUR_OPTION = 0;

  private static final int MIN_COLOUR_OPTION = 1;

  private static final int MAX_COLOUR_OPTION = 2;

  private static final int ABOVE_THRESHOLD_OPTION = 1;

  private static final int BELOW_THRESHOLD_OPTION = 2;

  private static final DecimalFormat DECFMT_2_2 = new DecimalFormat(
          "##.##");

  /*
   * FeatureRenderer holds colour scheme and filters for feature types
   */
  private final FeatureRenderer fr; // todo refactor to allow interface type
                                    // here

  /*
   * the view panel to update when settings change
   */
  final AlignmentViewPanel ap;

  final String featureType;

  /*
   * the colour and filters to reset to on Cancel
   */
  private final FeatureColourI originalColour;

  private final FeatureMatcherSetI originalFilter;

  /*
   * set flag to true when setting values programmatically,
   * to avoid invocation of action handlers
   */
  boolean adjusting = false;

  /*
   * minimum of the value range for graduated colour
   * (may be for feature score or for a numeric attribute)
   */
  private float min;

  /*
   * maximum of the value range for graduated colour
   */
  private float max;

  /*
   * radio button group, to select what to colour by:
   * simple colour, by category (text), or graduated
   */
  JRadioButton simpleColour = new JRadioButton();

  JRadioButton byCategory = new JRadioButton();

  JRadioButton graduatedColour = new JRadioButton();

  JPanel coloursPanel;

  JPanel filtersPanel;

  JPanel singleColour = new JPanel();

  JPanel minColour = new JPanel();

  JPanel maxColour = new JPanel();

  private JComboBox<Object> threshold = new JComboBox<>();

  private Slider slider;

  JTextField thresholdValue = new JTextField(20);

  private JCheckBox thresholdIsMin = new JCheckBox();

  private GraphLine threshline;

  private ActionListener featureSettings = null;

  private ActionListener changeColourAction;

  /*
   * choice of option for 'colour for no value'
   */
  private JComboBox<Object> noValueCombo;

  /*
   * choice of what to colour by text (Label or attribute)
   */
  private JComboBox<Object> colourByTextCombo;

  /*
   * choice of what to colour by range (Score or attribute)
   */
  private JComboBox<Object> colourByRangeCombo;

  private JRadioButton andFilters;

  private JRadioButton orFilters;

  /*
   * filters for the currently selected feature type
   */
  List<FeatureMatcherI> filters;

  private JPanel chooseFiltersPanel;

  /**
   * Constructor
   * 
   * @param frender
   * @param theType
   */
  public FeatureTypeSettings(FeatureRenderer frender, String theType)
  {
    this.fr = frender;
    this.featureType = theType;
    ap = fr.ap;
    originalFilter = fr.getFeatureFilter(theType);
    originalColour = fr.getFeatureColours().get(theType);

    adjusting = true;

    try
    {
      initialise();
    } catch (Exception ex)
    {
      ex.printStackTrace();
      return;
    }

    updateColoursPanel();

    updateFiltersPanel();

    adjusting = false;

    colourChanged(false);

    String title = MessageManager
            .formatMessage("label.display_settings_for", new String[]
            { theType });
    initDialogFrame(this, true, false, title, 580, 500);
    waitForInput();
  }

  /**
   * Configures the widgets on the Colours panel according to the current
   * feature colour scheme
   */
  private void updateColoursPanel()
  {
    FeatureColourI fc = fr.getFeatureColours().get(featureType);

    /*
     * suppress action handling while updating values programmatically
     */
    adjusting = true;
    try
    {
      /*
       * single colour
       */
      if (fc.isSimpleColour())
      {
        singleColour.setBackground(fc.getColour());
        singleColour.setForeground(fc.getColour());
        simpleColour.setSelected(true);
      }

      /*
       * colour by text (Label or attribute text)
       */
      if (fc.isColourByLabel())
      {
        byCategory.setSelected(true);
        colourByTextCombo.setEnabled(colourByTextCombo.getItemCount() > 1);
        if (fc.isColourByAttribute())
        {
          String[] attributeName = fc.getAttributeName();
          colourByTextCombo.setSelectedItem(
                  FeatureMatcher.toAttributeDisplayName(attributeName));
        }
        else
        {
          colourByTextCombo.setSelectedItem(LABEL_18N);
        }
      }
      else
      {
        colourByTextCombo.setEnabled(false);
      }

      if (!fc.isGraduatedColour())
      {
        colourByRangeCombo.setEnabled(false);
        minColour.setEnabled(false);
        maxColour.setEnabled(false);
        noValueCombo.setEnabled(false);
        threshold.setEnabled(false);
        slider.setEnabled(false);
        thresholdValue.setEnabled(false);
        thresholdIsMin.setEnabled(false);
        return;
      }

      /*
       * Graduated colour, by score or attribute value range
       */
      graduatedColour.setSelected(true);
      updateColourMinMax(); // ensure min, max are set
      colourByRangeCombo.setEnabled(colourByRangeCombo.getItemCount() > 1);
      minColour.setEnabled(true);
      maxColour.setEnabled(true);
      noValueCombo.setEnabled(true);
      threshold.setEnabled(true);
      minColour.setBackground(fc.getMinColour());
      maxColour.setBackground(fc.getMaxColour());

      if (fc.isColourByAttribute())
      {
        String[] attributeName = fc.getAttributeName();
        colourByRangeCombo.setSelectedItem(
                FeatureMatcher.toAttributeDisplayName(attributeName));
      }
      else
      {
        colourByRangeCombo.setSelectedItem(SCORE_18N);
      }
      Color noColour = fc.getNoColour();
      if (noColour == null)
      {
        noValueCombo.setSelectedIndex(NO_COLOUR_OPTION);
      }
      else if (noColour.equals(fc.getMinColour()))
      {
        noValueCombo.setSelectedIndex(MIN_COLOUR_OPTION);
      }
      else if (noColour.equals(fc.getMaxColour()))
      {
        noValueCombo.setSelectedIndex(MAX_COLOUR_OPTION);
      }

      /*
       * update min-max scaling if there is a range to work with,
       * else disable the widgets (this shouldn't happen if only 
       * valid options are offered in the combo box)
       * offset slider to have only non-negative values if necessary (JAL-2983)
       */
      slider.setSliderModel(min, max, min);
      slider.setMajorTickSpacing(
              (int) ((slider.getMaximum() - slider.getMinimum()) / 10f));

      threshline = new GraphLine((max - min) / 2f, "Threshold",
              Color.black);
      threshline.value = fc.getThreshold();

      if (fc.hasThreshold())
      {
        threshold.setSelectedIndex(
                fc.isAboveThreshold() ? ABOVE_THRESHOLD_OPTION
                        : BELOW_THRESHOLD_OPTION);
        slider.setEnabled(true);
        slider.setSliderValue(fc.getThreshold());
        setThresholdValueText(fc.getThreshold());
        thresholdValue.setEnabled(true);
        thresholdIsMin.setEnabled(true);
      }
      else
      {
        slider.setEnabled(false);
        thresholdValue.setEnabled(false);
        thresholdIsMin.setEnabled(false);
      }
      thresholdIsMin.setSelected(!fc.isAutoScaled());
    } finally
    {
      adjusting = false;
    }
  }

  /**
   * Configures the initial layout
   */
  private void initialise()
  {
    this.setLayout(new BorderLayout());

    /*
     * an ActionListener that applies colour changes
     */
    changeColourAction = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        colourChanged(true);
      }
    };

    /*
     * first panel: colour options
     */
    JPanel coloursPanel = initialiseColoursPanel();
    this.add(coloursPanel, BorderLayout.NORTH);

    /*
     * second panel: filter options
     */
    JPanel filtersPanel = initialiseFiltersPanel();
    this.add(filtersPanel, BorderLayout.CENTER);

    JPanel okCancelPanel = initialiseOkCancelPanel();

    this.add(okCancelPanel, BorderLayout.SOUTH);
  }

  /**
   * Updates the min-max range if Colour By selected item is Score, or an
   * attribute, with a min-max range
   */
  protected void updateColourMinMax()
  {
    if (!graduatedColour.isSelected())
    {
      return;
    }

    String colourBy = (String) colourByRangeCombo.getSelectedItem();
    float[] minMax = getMinMax(colourBy);

    if (minMax != null)
    {
      min = minMax[0];
      max = minMax[1];
    }
  }

  /**
   * Retrieves the min-max range:
   * <ul>
   * <li>of feature score, if colour or filter is by Score</li>
   * <li>else of the selected attribute</li>
   * </ul>
   * 
   * @param attName
   * @return
   */
  private float[] getMinMax(String attName)
  {
    float[] minMax = null;
    if (SCORE_18N.equals(attName))
    {
      minMax = fr.getMinMax().get(featureType)[0];
    }
    else
    {
      // colour by attribute range
      minMax = FeatureAttributes.getInstance().getMinMax(featureType,
              FeatureMatcher.fromAttributeDisplayName(attName));
    }
    return minMax;
  }

  /**
   * Lay out fields for graduated colour (by score or attribute value)
   * 
   * @return
   */
  private JPanel initialiseGraduatedColourPanel()
  {
    JPanel graduatedColourPanel = new JPanel();
    graduatedColourPanel.setLayout(
            new BoxLayout(graduatedColourPanel, BoxLayout.Y_AXIS));
    JvSwingUtils.createTitledBorder(graduatedColourPanel,
            MessageManager.getString("label.graduated_colour"), true);
    graduatedColourPanel.setBackground(Color.white);

    /*
     * first row: graduated colour radio button, score/attribute drop-down
     */
    JPanel graduatedChoicePanel = new JPanel(
            new FlowLayout(FlowLayout.LEFT));
    graduatedChoicePanel.setBackground(Color.white);
    graduatedColour = new JRadioButton(
            MessageManager.getString("label.by_range_of") + COLON);
    graduatedColour.setPreferredSize(new Dimension(RADIO_WIDTH, 20));
    graduatedColour.setOpaque(false);
    graduatedColour.addItemListener(new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent e)
      {
        if (graduatedColour.isSelected())
        {
          colourChanged(true);
        }
      }
    });
    graduatedChoicePanel.add(graduatedColour);

    List<String[]> attNames = FeatureAttributes.getInstance()
            .getAttributes(featureType);
    colourByRangeCombo = populateAttributesDropdown(attNames, true, false);
    colourByRangeCombo.addItemListener(new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent e)
      {
        colourChanged(true);
      }
    });

    /*
     * disable graduated colour option if no range found
     */
    graduatedColour.setEnabled(colourByRangeCombo.getItemCount() > 0);

    graduatedChoicePanel.add(colourByRangeCombo);
    graduatedColourPanel.add(graduatedChoicePanel);

    /*
     * second row - min/max/no colours
     */
    JPanel colourRangePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    colourRangePanel.setBackground(Color.white);
    graduatedColourPanel.add(colourRangePanel);

    minColour.setFont(JvSwingUtils.getLabelFont());
    minColour.setBorder(BorderFactory.createLineBorder(Color.black));
    minColour.setPreferredSize(new Dimension(40, 20));
    minColour.setToolTipText(MessageManager.getString("label.min_colour"));
    minColour.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        if (minColour.isEnabled())
        {
          String ttl = MessageManager
                  .getString("label.select_colour_minimum_value");
          showColourChooser(minColour, ttl);
        }
      }
    });

    maxColour.setFont(JvSwingUtils.getLabelFont());
    maxColour.setBorder(BorderFactory.createLineBorder(Color.black));
    maxColour.setPreferredSize(new Dimension(40, 20));
    maxColour.setToolTipText(MessageManager.getString("label.max_colour"));
    maxColour.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        if (maxColour.isEnabled())
        {
          String ttl = MessageManager
                  .getString("label.select_colour_maximum_value");
          showColourChooser(maxColour, ttl);
        }
      }
    });
    maxColour.setBorder(new LineBorder(Color.black));

    /*
     * if not set, default max colour to last plain colour,
     * and make min colour a pale version of max colour
     */
    Color max = originalColour.getMaxColour();
    if (max == null)
    {
      max = originalColour.getColour();
      minColour.setBackground(ColorUtils.bleachColour(max, 0.9f));
    }
    else
    {
      maxColour.setBackground(max);
      minColour.setBackground(originalColour.getMinColour());
    }

    noValueCombo = new JComboBox<>();
    noValueCombo.addItem(MessageManager.getString("label.no_colour"));
    noValueCombo.addItem(MessageManager.getString("label.min_colour"));
    noValueCombo.addItem(MessageManager.getString("label.max_colour"));
    noValueCombo.addItemListener(new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent e)
      {
        colourChanged(true);
      }
    });

    JLabel minText = new JLabel(
            MessageManager.getString("label.min_value") + COLON);
    minText.setFont(JvSwingUtils.getLabelFont());
    JLabel maxText = new JLabel(
            MessageManager.getString("label.max_value") + COLON);
    maxText.setFont(JvSwingUtils.getLabelFont());
    JLabel noText = new JLabel(
            MessageManager.getString("label.no_value") + COLON);
    noText.setFont(JvSwingUtils.getLabelFont());

    colourRangePanel.add(minText);
    colourRangePanel.add(minColour);
    colourRangePanel.add(maxText);
    colourRangePanel.add(maxColour);
    colourRangePanel.add(noText);
    colourRangePanel.add(noValueCombo);

    /*
     * third row - threshold options and value
     */
    JPanel thresholdPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    thresholdPanel.setBackground(Color.white);
    graduatedColourPanel.add(thresholdPanel);

    threshold.addActionListener(changeColourAction);
    threshold.setToolTipText(MessageManager
            .getString("label.threshold_feature_display_by_score"));
    threshold.addItem(MessageManager
            .getString("label.threshold_feature_no_threshold")); // index 0
    threshold.addItem(MessageManager
            .getString("label.threshold_feature_above_threshold")); // index 1
    threshold.addItem(MessageManager
            .getString("label.threshold_feature_below_threshold")); // index 2

    thresholdValue.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        thresholdValue_actionPerformed();
      }
    });
    thresholdValue.addFocusListener(new FocusAdapter()
    {
      @Override
      public void focusLost(FocusEvent e)
      {
        thresholdValue_actionPerformed();
      }
    });
    slider = new Slider(0f, 100f, 50f);
    slider.setPaintLabels(false);
    slider.setPaintTicks(true);
    slider.setBackground(Color.white);
    slider.setEnabled(false);
    slider.setOpaque(false);
    slider.setPreferredSize(new Dimension(100, 32));
    slider.setToolTipText(
            MessageManager.getString("label.adjust_threshold"));

    slider.addChangeListener(new ChangeListener()
    {
      @Override
      public void stateChanged(ChangeEvent evt)
      {
        if (!adjusting)
        {
          setThresholdValueText(slider.getSliderValue());
          thresholdValue.setBackground(Color.white); // to reset red for invalid
          sliderValueChanged();
        }
      }
    });
    slider.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseReleased(MouseEvent evt)
      {
        /*
         * only update Overview and/or structure colouring
         * when threshold slider drag ends (mouse up)
         */
        if (ap != null)
        {
          refreshDisplay(true);
        }
      }
    });

    thresholdValue.setEnabled(false);
    thresholdValue.setColumns(7);

    thresholdPanel.add(threshold);
    thresholdPanel.add(slider);
    thresholdPanel.add(thresholdValue);

    thresholdIsMin.setBackground(Color.white);
    thresholdIsMin
            .setText(MessageManager.getString("label.threshold_minmax"));
    thresholdIsMin.setToolTipText(MessageManager
            .getString("label.toggle_absolute_relative_display_threshold"));
    thresholdIsMin.addActionListener(changeColourAction);
    thresholdPanel.add(thresholdIsMin);

    return graduatedColourPanel;
  }

  /**
   * Lay out OK and Cancel buttons
   * 
   * @return
   */
  private JPanel initialiseOkCancelPanel()
  {
    JPanel okCancelPanel = new JPanel();
    // okCancelPanel.setBackground(Color.white);
    okCancelPanel.add(ok);
    okCancelPanel.add(cancel);
    return okCancelPanel;
  }

  /**
   * Lay out Colour options panel, containing
   * <ul>
   * <li>plain colour, with colour picker</li>
   * <li>colour by text, with choice of Label or other attribute</li>
   * <li>colour by range, of score or other attribute, when available</li>
   * </ul>
   * 
   * @return
   */
  private JPanel initialiseColoursPanel()
  {
    JPanel colourByPanel = new JPanel();
    colourByPanel.setBackground(Color.white);
    colourByPanel.setLayout(new BoxLayout(colourByPanel, BoxLayout.Y_AXIS));
    JvSwingUtils.createTitledBorder(colourByPanel,
            MessageManager.getString("action.colour"), true);

    /*
     * simple colour radio button and colour picker
     */
    JPanel simpleColourPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    simpleColourPanel.setBackground(Color.white);
    colourByPanel.add(simpleColourPanel);

    simpleColour = new JRadioButton(
            MessageManager.getString("label.simple_colour"));
    simpleColour.setPreferredSize(new Dimension(RADIO_WIDTH, 20));
    simpleColour.setOpaque(false);
    simpleColour.addItemListener(new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent e)
      {
        if (simpleColour.isSelected() && !adjusting)
        {
          colourChanged(true);
        }
      }
    });

    singleColour.setFont(JvSwingUtils.getLabelFont());
    singleColour.setBorder(BorderFactory.createLineBorder(Color.black));
    singleColour.setPreferredSize(new Dimension(40, 20));
    // if (originalColour.isGraduatedColour())
    // {
    // singleColour.setBackground(originalColour.getMaxColour());
    // singleColour.setForeground(originalColour.getMaxColour());
    // }
    // else
    // {
    singleColour.setBackground(originalColour.getColour());
    singleColour.setForeground(originalColour.getColour());
    // }
    singleColour.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        if (simpleColour.isSelected())
        {
          String ttl = MessageManager
                  .formatMessage("label.select_colour_for", featureType);
          showColourChooser(singleColour, ttl);
        }
      }
    });
    simpleColourPanel.add(simpleColour); // radio button
    simpleColourPanel.add(singleColour); // colour picker button

    /*
     * colour by text (category) radio button and drop-down choice list
     */
    JPanel byTextPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    byTextPanel.setBackground(Color.white);
    JvSwingUtils.createTitledBorder(byTextPanel,
            MessageManager.getString("label.colour_by_text"), true);
    colourByPanel.add(byTextPanel);
    byCategory = new JRadioButton(
            MessageManager.getString("label.by_text_of") + COLON);
    byCategory.setPreferredSize(new Dimension(RADIO_WIDTH, 20));
    byCategory.setOpaque(false);
    byCategory.addItemListener(new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent e)
      {
        if (byCategory.isSelected())
        {
          colourChanged(true);
        }
      }
    });
    byTextPanel.add(byCategory);

    List<String[]> attNames = FeatureAttributes.getInstance()
            .getAttributes(featureType);
    colourByTextCombo = populateAttributesDropdown(attNames, false, true);
    colourByTextCombo.addItemListener(new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent e)
      {
        colourChanged(true);
      }
    });
    byTextPanel.add(colourByTextCombo);

    /*
     * graduated colour panel
     */
    JPanel graduatedColourPanel = initialiseGraduatedColourPanel();
    colourByPanel.add(graduatedColourPanel);

    /*
     * 3 radio buttons select between simple colour, 
     * by category (text), or graduated
     */
    ButtonGroup bg = new ButtonGroup();
    bg.add(simpleColour);
    bg.add(byCategory);
    bg.add(graduatedColour);

    return colourByPanel;
  }

  /**
   * Shows a colour chooser dialog, and if a selection is made, updates the
   * colour of the given panel
   * 
   * @param colourPanel
   *          the panel whose background colour is being picked
   * @param title
   */
  void showColourChooser(JPanel colourPanel, String title)
  {
    ColourChooserListener listener = new ColourChooserListener()
    {
      @Override
      public void colourSelected(Color col)
      {
        colourPanel.setBackground(col);
        colourPanel.setForeground(col);
        colourPanel.repaint();
        colourChanged(true);
      }
    };
    JalviewColourChooser.showColourChooser(this, title,
            colourPanel.getBackground(), listener);
  }

  /**
   * Constructs and sets the selected colour options as the colour for the
   * feature type, and repaints the alignment, and optionally the Overview
   * and/or structure viewer if open
   * 
   * @param updateStructsAndOverview
   */
  void colourChanged(boolean updateStructsAndOverview)
  {
    if (adjusting)
    {
      /*
       * ignore action handlers while setting values programmatically
       */
      return;
    }

    /*
     * ensure min-max range is for the latest choice of 
     * 'graduated colour by'
     */
    updateColourMinMax();

    FeatureColourI acg = makeColourFromInputs();

    /*
     * save the colour, and repaint stuff
     */
    fr.setColour(featureType, acg);
    refreshDisplay(updateStructsAndOverview);

    updateColoursPanel();
  }

  /**
   * Converts the input values into an instance of FeatureColour
   * 
   * @return
   */
  private FeatureColourI makeColourFromInputs()
  {
    /*
     * min-max range is to (or from) threshold value if 
     * 'threshold is min/max' is selected 
     */

    float thresh = 0f;
    try
    {
      thresh = Float.valueOf(thresholdValue.getText());
    } catch (NumberFormatException e)
    {
      // invalid inputs are already handled on entry
    }
    float minValue = min;
    float maxValue = max;
    int thresholdOption = threshold.getSelectedIndex();
    if (thresholdIsMin.isSelected()
            && thresholdOption == ABOVE_THRESHOLD_OPTION)
    {
      minValue = thresh;
    }
    if (thresholdIsMin.isSelected()
            && thresholdOption == BELOW_THRESHOLD_OPTION)
    {
      maxValue = thresh;
    }
    Color noColour = null;
    if (noValueCombo.getSelectedIndex() == MIN_COLOUR_OPTION)
    {
      noColour = minColour.getBackground();
    }
    else if (noValueCombo.getSelectedIndex() == MAX_COLOUR_OPTION)
    {
      noColour = maxColour.getBackground();
    }

    /*
     * construct a colour that 'remembers' all the options, including
     * those not currently selected
     */
    FeatureColourI fc = new FeatureColour(singleColour.getBackground(),
            minColour.getBackground(), maxColour.getBackground(), noColour,
            minValue, maxValue);

    /*
     * easiest case - a single colour
     */
    if (simpleColour.isSelected())
    {
      ((FeatureColour) fc).setGraduatedColour(false);
      return fc;
    }

    /*
     * next easiest case - colour by Label, or attribute text
     */
    if (byCategory.isSelected())
    {
      fc.setColourByLabel(true);
      String byWhat = (String) colourByTextCombo.getSelectedItem();
      if (!LABEL_18N.equals(byWhat))
      {
        fc.setAttributeName(
                FeatureMatcher.fromAttributeDisplayName(byWhat));
      }
      return fc;
    }

    /*
     * remaining case - graduated colour by score, or attribute value;
     * set attribute to colour by if selected
     */
    String byWhat = (String) colourByRangeCombo.getSelectedItem();
    if (!SCORE_18N.equals(byWhat))
    {
      fc.setAttributeName(FeatureMatcher.fromAttributeDisplayName(byWhat));
    }

    /*
     * set threshold options and 'autoscaled' which is
     * false if 'threshold is min/max' is selected
     * else true (colour range is on actual range of values)
     */
    fc.setThreshold(thresh);
    fc.setAutoScaled(!thresholdIsMin.isSelected());
    fc.setAboveThreshold(thresholdOption == ABOVE_THRESHOLD_OPTION);
    fc.setBelowThreshold(thresholdOption == BELOW_THRESHOLD_OPTION);

    if (threshline == null)
    {
      /*
       * todo not yet implemented: visual indication of feature threshold
       */
      threshline = new GraphLine((max - min) / 2f, "Threshold",
              Color.black);
    }

    return fc;
  }

  @Override
  protected void raiseClosed()
  {
    if (this.featureSettings != null)
    {
      featureSettings.actionPerformed(new ActionEvent(this, 0, "CLOSED"));
    }
  }

  /**
   * Action on OK is just to dismiss the dialog - any changes have already been
   * applied
   */
  @Override
  public void okPressed()
  {
  }

  /**
   * Action on Cancel is to restore colour scheme and filters as they were when
   * the dialog was opened
   */
  @Override
  public void cancelPressed()
  {
    fr.setColour(featureType, originalColour);
    fr.setFeatureFilter(featureType, originalFilter);
    refreshDisplay(true);
  }

  /**
   * Action on text entry of a threshold value
   */
  protected void thresholdValue_actionPerformed()
  {
    try
    {
      /*
       * set 'adjusting' flag while moving the slider, so it 
       * doesn't then in turn change the value (with rounding)
       */
      adjusting = true;
      float f = Float.parseFloat(thresholdValue.getText());
      f = Float.max(f, this.min);
      f = Float.min(f, this.max);
      setThresholdValueText(f);
      slider.setSliderValue(f);
      threshline.value = f;
      thresholdValue.setBackground(Color.white); // ok
      adjusting = false;
      colourChanged(true);
    } catch (NumberFormatException ex)
    {
      thresholdValue.setBackground(Color.red); // not ok
      adjusting = false;
    }
  }

  /**
   * Sets the text field for threshold value, rounded to four significant
   * figures
   * 
   * @param f
   */
  void setThresholdValueText(float f)
  {
    BigDecimal formatted = new BigDecimal(f).round(FOUR_SIG_FIG)
            .stripTrailingZeros();
    thresholdValue.setText(formatted.toPlainString());
  }

  /**
   * Action on change of threshold slider value. This may be done interactively
   * (by moving the slider), or programmatically (to update the slider after
   * manual input of a threshold value).
   */
  protected void sliderValueChanged()
  {
    threshline.value = slider.getSliderValue();

    /*
     * repaint alignment, but not Overview or structure,
     * to avoid overload while dragging the slider
     */
    colourChanged(false);
  }

  void addActionListener(ActionListener listener)
  {
    if (featureSettings != null)
    {
      System.err.println(
              "IMPLEMENTATION ISSUE: overwriting action listener for FeatureColourChooser");
    }
    featureSettings = listener;
  }

  /**
   * A helper method to build the drop-down choice of attributes for a feature.
   * If 'withRange' is true, then Score, and any attributes with a min-max
   * range, are added. If 'withText' is true, Label and any known attributes are
   * added. This allows 'categorical numerical' attributes e.g. codon position
   * to be coloured by text.
   * <p>
   * Where metadata is available with a description for an attribute, that is
   * added as a tooltip.
   * <p>
   * Attribute names may be 'simple' e.g. "AC" or 'compound' e.g. {"CSQ",
   * "Allele"}. Compound names are rendered for display as (e.g.) CSQ:Allele.
   * <p>
   * This method does not add any ActionListener to the JComboBox.
   * 
   * @param attNames
   * @param withRange
   * @param withText
   */
  protected JComboBox<Object> populateAttributesDropdown(
          List<String[]> attNames, boolean withRange, boolean withText)
  {
    List<String> displayAtts = new ArrayList<>();
    List<String> tooltips = new ArrayList<>();

    if (withText)
    {
      displayAtts.add(LABEL_18N);
      tooltips.add(MessageManager.getString("label.description"));
    }
    if (withRange)
    {
      float[][] minMax = fr.getMinMax().get(featureType);
      if (minMax != null && minMax[0][0] != minMax[0][1])
      {
        displayAtts.add(SCORE_18N);
        tooltips.add(SCORE_18N);
      }
    }

    FeatureAttributes fa = FeatureAttributes.getInstance();
    for (String[] attName : attNames)
    {
      float[] minMax = fa.getMinMax(featureType, attName);
      boolean hasRange = minMax != null && minMax[0] != minMax[1];
      if (!withText && !hasRange)
      {
        continue;
      }
      displayAtts.add(FeatureMatcher.toAttributeDisplayName(attName));
      String desc = fa.getDescription(featureType, attName);
      if (desc != null && desc.length() > MAX_TOOLTIP_LENGTH)
      {
        desc = desc.substring(0, MAX_TOOLTIP_LENGTH) + "...";
      }
      tooltips.add(desc == null ? "" : desc);
    }

    // now convert String List to Object List for buildComboWithTooltips
    List<Object> displayAttsObjects = new ArrayList<>(displayAtts);
    JComboBox<Object> attCombo = JvSwingUtils
            .buildComboWithTooltips(displayAttsObjects, tooltips);

    return attCombo;
  }

  /**
   * Populates initial layout of the feature attribute filters panel
   */
  private JPanel initialiseFiltersPanel()
  {
    filters = new ArrayList<>();

    JPanel filtersPanel = new JPanel();
    filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.Y_AXIS));
    filtersPanel.setBackground(Color.white);
    JvSwingUtils.createTitledBorder(filtersPanel,
            MessageManager.getString("label.filters"), true);

    JPanel andOrPanel = initialiseAndOrPanel();
    filtersPanel.add(andOrPanel);

    /*
     * panel with filters - populated by refreshFiltersDisplay, 
     * which also sets the layout manager
     */
    chooseFiltersPanel = new JPanel();
    chooseFiltersPanel.setBackground(Color.white);
    filtersPanel.add(chooseFiltersPanel);

    return filtersPanel;
  }

  /**
   * Lays out the panel with radio buttons to AND or OR filter conditions
   * 
   * @return
   */
  private JPanel initialiseAndOrPanel()
  {
    JPanel andOrPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    andOrPanel.setBackground(Color.white);
    andFilters = new JRadioButton(MessageManager.getString("label.and"));
    orFilters = new JRadioButton(MessageManager.getString("label.or"));
    andFilters.setOpaque(false);
    orFilters.setOpaque(false);
    ActionListener actionListener = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        filtersChanged();
      }
    };
    andFilters.addActionListener(actionListener);
    orFilters.addActionListener(actionListener);
    ButtonGroup andOr = new ButtonGroup();
    andOr.add(andFilters);
    andOr.add(orFilters);
    andFilters.setSelected(true);
    andOrPanel.add(
            new JLabel(MessageManager.getString("label.join_conditions")));
    andOrPanel.add(andFilters);
    andOrPanel.add(orFilters);
    return andOrPanel;
  }

  /**
   * Refreshes the display to show any filters currently configured for the
   * selected feature type (editable, with 'remove' option), plus one extra row
   * for adding a condition. This should be called after a filter has been
   * removed, added or amended.
   */
  private void updateFiltersPanel()
  {
    /*
     * clear the panel and list of filter conditions
     */
    chooseFiltersPanel.removeAll();
    filters.clear();

    /*
     * look up attributes known for feature type
     */
    List<String[]> attNames = FeatureAttributes.getInstance()
            .getAttributes(featureType);

    /*
     * if this feature type has filters set, load them first
     */
    FeatureMatcherSetI featureFilters = fr.getFeatureFilter(featureType);
    if (featureFilters != null)
    {
      if (!featureFilters.isAnded())
      {
        orFilters.setSelected(true);
      }
      // avoid use of lambda expression to keep SwingJS happy
      // featureFilters.getMatchers().forEach(item -> filters.add(item));
      for (FeatureMatcherI matcher : featureFilters.getMatchers())
      {
        filters.add(matcher);
      }
    }

    /*
     * and an empty filter for the user to populate (add)
     */
    filters.add(FeatureMatcher.NULL_MATCHER);

    /*
     * use GridLayout to 'justify' rows to the top of the panel, until
     * there are too many to fit in, then fall back on BoxLayout
     */
    if (filters.size() <= 5)
    {
      chooseFiltersPanel.setLayout(new GridLayout(5, 1));
    }
    else
    {
      chooseFiltersPanel.setLayout(
              new BoxLayout(chooseFiltersPanel, BoxLayout.Y_AXIS));
    }

    /*
     * render the conditions in rows, each in its own JPanel
     */
    int filterIndex = 0;
    for (FeatureMatcherI filter : filters)
    {
      JPanel row = addFilter(filter, attNames, filterIndex);
      chooseFiltersPanel.add(row);
      filterIndex++;
    }

    this.validate();
    this.repaint();
  }

  /**
   * A helper method that constructs a row (panel) with one filter condition:
   * <ul>
   * <li>a drop-down list of Label, Score and attribute names to choose
   * from</li>
   * <li>a drop-down list of conditions to choose from</li>
   * <li>a text field for input of a match pattern</li>
   * <li>optionally, a 'remove' button</li>
   * </ul>
   * The filter values are set as defaults for the input fields. The 'remove'
   * button is added unless the pattern is empty (incomplete filter condition).
   * <p>
   * Action handlers on these fields provide for
   * <ul>
   * <li>validate pattern field - should be numeric if condition is numeric</li>
   * <li>save filters and refresh display on any (valid) change</li>
   * <li>remove filter and refresh on 'Remove'</li>
   * <li>update conditions list on change of Label/Score/Attribute</li>
   * <li>refresh value field tooltip with min-max range on change of
   * attribute</li>
   * </ul>
   * 
   * @param filter
   * @param attNames
   * @param filterIndex
   * @return
   */
  protected JPanel addFilter(FeatureMatcherI filter,
          List<String[]> attNames, int filterIndex)
  {
    String[] attName = filter.getAttribute();
    Condition cond = filter.getMatcher().getCondition();
    String pattern = filter.getMatcher().getPattern();

    JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
    filterRow.setBackground(Color.white);

    /*
     * drop-down choice of attribute, with description as a tooltip 
     * if we can obtain it
     */
    final JComboBox<Object> attCombo = populateAttributesDropdown(attNames,
            true, true);
    String filterBy = setSelectedAttribute(attCombo, filter);

    JComboBox<Condition> condCombo = new JComboBox<>();

    JTextField patternField = new JTextField(8);
    patternField.setText(pattern);

    /*
     * action handlers that validate and (if valid) apply changes
     */
    ActionListener actionListener = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        if (validateFilter(patternField, condCombo))
        {
          if (updateFilter(attCombo, condCombo, patternField, filterIndex))
          {
            filtersChanged();
          }
        }
      }
    };
    ItemListener itemListener = new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent e)
      {
        actionListener.actionPerformed(null);
      }
    };

    if (filter == FeatureMatcher.NULL_MATCHER) // the 'add a condition' row
    {
      attCombo.setSelectedIndex(0);
    }
    else
    {
      attCombo.setSelectedItem(
              FeatureMatcher.toAttributeDisplayName(attName));
    }
    attCombo.addItemListener(new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent e)
      {
        /*
         * on change of attribute, refresh the conditions list to
         * ensure it is appropriate for the attribute datatype
         */
        populateConditions((String) attCombo.getSelectedItem(),
                (Condition) condCombo.getSelectedItem(), condCombo,
                patternField);
        actionListener.actionPerformed(null);
      }
    });

    filterRow.add(attCombo);

    /*
     * drop-down choice of test condition
     */
    populateConditions(filterBy, cond, condCombo, patternField);
    condCombo.setPreferredSize(new Dimension(150, 20));
    condCombo.addItemListener(itemListener);
    filterRow.add(condCombo);

    /*
     * pattern to match against
     */
    patternField.addActionListener(actionListener);
    patternField.addFocusListener(new FocusAdapter()
    {
      @Override
      public void focusLost(FocusEvent e)
      {
        actionListener.actionPerformed(null);
      }
    });
    filterRow.add(patternField);

    /*
     * disable pattern field for condition 'Present / NotPresent'
     */
    Condition selectedCondition = (Condition) condCombo.getSelectedItem();
    patternField.setEnabled(selectedCondition.needsAPattern());

    /*
     * if a numeric condition is selected, show the value range
     * as a tooltip on the value input field
     */
    setNumericHints(filterBy, selectedCondition, patternField);

    /*
     * add remove button if filter is populated (non-empty pattern)
     */
    if (!patternField.isEnabled()
            || (pattern != null && pattern.trim().length() > 0))
    {
      JButton removeCondition = new JButton("\u2717");
      // Dingbats cursive x
      removeCondition.setBorder(new EmptyBorder(0, 0, 0, 0));
      removeCondition.setBackground(Color.WHITE);
      removeCondition.setPreferredSize(new Dimension(23, 17));
      removeCondition.setToolTipText(
              MessageManager.getString("label.delete_condition"));
      removeCondition.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          filters.remove(filterIndex);
          filtersChanged();
        }
      });
      filterRow.add(removeCondition);
    }

    return filterRow;
  }

  /**
   * Sets the selected item in the Label/Score/Attribute drop-down to match the
   * filter
   * 
   * @param attCombo
   * @param filter
   */
  private String setSelectedAttribute(JComboBox<Object> attCombo,
          FeatureMatcherI filter)
  {
    String item = null;
    if (filter.isByScore())
    {
      item = SCORE_18N;
    }
    else if (filter.isByLabel())
    {
      item = LABEL_18N;
    }
    else
    {
      item = FeatureMatcher.toAttributeDisplayName(filter.getAttribute());
    }
    attCombo.setSelectedItem(item);
    return item;
  }

  /**
   * If a numeric comparison condition is selected, retrieves the min-max range
   * for the value (score or attribute), and sets it as a tooltip on the value
   * field. If the field is currently empty, then pre-populates it with
   * <ul>
   * <li>the minimum value, if condition is > or >=</li>
   * <li>the maximum value, if condition is < or <=</li>
   * </ul>
   * 
   * @param attName
   * @param selectedCondition
   * @param patternField
   */
  private void setNumericHints(String attName, Condition selectedCondition,
          JTextField patternField)
  {
    patternField.setToolTipText("");

    if (selectedCondition.isNumeric())
    {
      float[] minMax = getMinMax(attName);
      if (minMax != null)
      {
        String minFormatted = DECFMT_2_2.format(minMax[0]);
        String maxFormatted = DECFMT_2_2.format(minMax[1]);
        String tip = String.format("(%s - %s)", minFormatted, maxFormatted);
        patternField.setToolTipText(tip);
        if (patternField.getText().isEmpty())
        {
          if (selectedCondition == Condition.GE
                  || selectedCondition == Condition.GT)
          {
            patternField.setText(minFormatted);
          }
          else
          {
            if (selectedCondition == Condition.LE
                    || selectedCondition == Condition.LT)
            {
              patternField.setText(maxFormatted);
            }
          }
        }
      }
    }
  }

  /**
   * Populates the drop-down list of comparison conditions for the given
   * attribute name. The conditions added depend on the datatype of the
   * attribute values. The supplied condition is set as the selected item in the
   * list, provided it is in the list. If the pattern is now invalid
   * (non-numeric pattern for a numeric condition), it is cleared.
   * 
   * @param attName
   * @param cond
   * @param condCombo
   * @param patternField
   */
  void populateConditions(String attName, Condition cond,
          JComboBox<Condition> condCombo, JTextField patternField)
  {
    Datatype type = FeatureAttributes.getInstance().getDatatype(featureType,
            FeatureMatcher.fromAttributeDisplayName(attName));
    if (LABEL_18N.equals(attName))
    {
      type = Datatype.Character;
    }
    else if (SCORE_18N.equals(attName))
    {
      type = Datatype.Number;
    }

    /*
     * remove itemListener before starting
     */
    ItemListener listener = condCombo.getItemListeners()[0];
    condCombo.removeItemListener(listener);
    boolean condIsValid = false;

    condCombo.removeAllItems();
    for (Condition c : Condition.values())
    {
      if ((c.isNumeric() && type == Datatype.Number)
              || (!c.isNumeric() && type != Datatype.Number))
      {
        condCombo.addItem(c);
        if (c == cond)
        {
          condIsValid = true;
        }
      }
    }

    /*
     * set the selected condition (does nothing if not in the list)
     */
    if (condIsValid)
    {
      condCombo.setSelectedItem(cond);
    }
    else
    {
      condCombo.setSelectedIndex(0);
    }

    /*
     * clear pattern if it is now invalid for condition
     */
    if (((Condition) condCombo.getSelectedItem()).isNumeric())
    {
      try
      {
        String pattern = patternField.getText().trim();
        if (pattern.length() > 0)
        {
          Float.valueOf(pattern);
        }
      } catch (NumberFormatException e)
      {
        patternField.setText("");
      }
    }

    /*
     * restore the listener
     */
    condCombo.addItemListener(listener);
  }

  /**
   * Answers true unless a numeric condition has been selected with a
   * non-numeric value. Sets the value field to RED with a tooltip if in error.
   * <p>
   * If the pattern is expected but is empty, this method returns false, but
   * does not mark the field as invalid. This supports selecting an attribute
   * for a new condition before a match pattern has been entered.
   * 
   * @param value
   * @param condCombo
   */
  protected boolean validateFilter(JTextField value,
          JComboBox<Condition> condCombo)
  {
    if (value == null || condCombo == null)
    {
      return true; // fields not populated
    }

    Condition cond = (Condition) condCombo.getSelectedItem();
    if (!cond.needsAPattern())
    {
      return true;
    }

    value.setBackground(Color.white);
    value.setToolTipText("");
    String v1 = value.getText().trim();
    if (v1.length() == 0)
    {
      // return false;
    }

    if (cond.isNumeric() && v1.length() > 0)
    {
      try
      {
        Float.valueOf(v1);
      } catch (NumberFormatException e)
      {
        value.setBackground(Color.red);
        value.setToolTipText(
                MessageManager.getString("label.numeric_required"));
        return false;
      }
    }

    return true;
  }

  /**
   * Constructs a filter condition from the given input fields, and replaces the
   * condition at filterIndex with the new one. Does nothing if the pattern
   * field is blank (unless the match condition is one that doesn't require a
   * pattern, e.g. 'Is present'). Answers true if the filter was updated, else
   * false.
   * <p>
   * This method may update the tooltip on the filter value field to show the
   * value range, if a numeric condition is selected. This ensures the tooltip
   * is updated when a numeric valued attribute is chosen on the last 'add a
   * filter' row.
   * 
   * @param attCombo
   * @param condCombo
   * @param valueField
   * @param filterIndex
   */
  protected boolean updateFilter(JComboBox<Object> attCombo,
          JComboBox<Condition> condCombo, JTextField valueField,
          int filterIndex)
  {
    String attName;
    try
    {
      attName = (String) attCombo.getSelectedItem();
    } catch (Exception e)
    {
      Console.error("Problem casting Combo box entry to String");
      attName = attCombo.getSelectedItem().toString();
    }
    Condition cond = (Condition) condCombo.getSelectedItem();
    String pattern = valueField.getText().trim();

    setNumericHints(attName, cond, valueField);

    if (pattern.length() == 0 && cond.needsAPattern())
    {
      valueField.setEnabled(true); // ensure pattern field is enabled!
      return false;
    }

    /*
     * Construct a matcher that operates on Label, Score, 
     * or named attribute
     */
    FeatureMatcherI km = null;
    if (LABEL_18N.equals(attName))
    {
      km = FeatureMatcher.byLabel(cond, pattern);
    }
    else if (SCORE_18N.equals(attName))
    {
      km = FeatureMatcher.byScore(cond, pattern);
    }
    else
    {
      km = FeatureMatcher.byAttribute(cond, pattern,
              FeatureMatcher.fromAttributeDisplayName(attName));
    }

    filters.set(filterIndex, km);

    return true;
  }

  /**
   * Action on any change to feature filtering, namely
   * <ul>
   * <li>change of selected attribute</li>
   * <li>change of selected condition</li>
   * <li>change of match pattern</li>
   * <li>removal of a condition</li>
   * </ul>
   * The inputs are parsed into a combined filter and this is set for the
   * feature type, and the alignment redrawn.
   */
  protected void filtersChanged()
  {
    /*
     * update the filter conditions for the feature type
     */
    boolean anded = andFilters.isSelected();
    FeatureMatcherSetI combined = new FeatureMatcherSet();

    for (FeatureMatcherI filter : filters)
    {
      String pattern = filter.getMatcher().getPattern();
      Condition condition = filter.getMatcher().getCondition();
      if (pattern.trim().length() > 0 || !condition.needsAPattern())
      {
        if (anded)
        {
          combined.and(filter);
        }
        else
        {
          combined.or(filter);
        }
      }
    }

    /*
     * save the filter conditions in the FeatureRenderer
     * (note this might now be an empty filter with no conditions)
     */
    fr.setFeatureFilter(featureType, combined.isEmpty() ? null : combined);
    refreshDisplay(true);

    updateFiltersPanel();
  }

  /**
   * Repaints alignment, structure and overview (if shown). If there is a
   * complementary view which is showing this view's features, then also
   * repaints that.
   * 
   * @param updateStructsAndOverview
   */
  void refreshDisplay(boolean updateStructsAndOverview)
  {
    ap.paintAlignment(true, updateStructsAndOverview);
    AlignViewportI complement = ap.getAlignViewport().getCodingComplement();
    if (complement != null && complement.isShowComplementFeatures())
    {
      AlignFrame af2 = Desktop.getAlignFrameFor(complement);
      af2.alignPanel.paintAlignment(true, updateStructsAndOverview);
    }
  }
}
