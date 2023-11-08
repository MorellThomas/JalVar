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

import jalview.api.FeatureColourI;
import jalview.datamodel.GraphLine;
import jalview.schemes.AnnotationColourGradient;
import jalview.schemes.FeatureColour;
import jalview.util.MessageManager;

import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class FeatureColourChooser extends Panel implements ActionListener,
        AdjustmentListener, ItemListener, MouseListener
{
  /*
   * the absolute min-max range of a feature score is scaled to 
   * 1000 positions on the colour threshold slider
   */
  private static final int SCALE_FACTOR_1K = 1000;

  private static final String COLON = ":";

  private JVDialog frame;

  private Frame owner;

  private FeatureRenderer fr;

  private FeatureSettings fs = null;

  private FeatureColourI cs;

  private FeatureColourI oldcs;

  private boolean adjusting = false;

  private float min, max;

  private String type = null;

  private AlignFrame af = null;

  private Panel minColour = new Panel();

  private Panel maxColour = new Panel();

  private Choice threshold = new Choice();

  private Scrollbar slider = new Scrollbar(Scrollbar.HORIZONTAL);

  private TextField thresholdValue = new TextField(20);

  private Checkbox thresholdIsMin = new Checkbox();

  private Checkbox colourFromLabel = new Checkbox();

  private GraphLine threshline;

  /**
   * Constructor given a context AlignFrame and a feature type. This is used
   * when opening the graduated colour dialog from the Amend Feature dialog.
   * 
   * @param alignFrame
   * @param featureType
   */
  public FeatureColourChooser(AlignFrame alignFrame, String featureType)
  {
    this.af = alignFrame;
    init(alignFrame.getSeqcanvas().getFeatureRenderer(), featureType);
  }

  /**
   * Constructor given a context FeatureSettings and a feature type. This is
   * used when opening the graduated colour dialog from Feature Settings.
   * 
   * @param fsettings
   * @param featureType
   */
  public FeatureColourChooser(FeatureSettings fsettings, String featureType)
  {
    this.fs = fsettings;
    init(fsettings.fr, featureType);
  }

  private void init(FeatureRenderer frenderer, String featureType)
  {
    this.type = featureType;
    fr = frenderer;
    float mm[] = fr.getMinMax().get(type)[0];
    min = mm[0];
    max = mm[1];
    threshline = new GraphLine((max - min) / 2f, "Threshold", Color.black);
    oldcs = fr.getFeatureColours().get(type);
    if (oldcs.isGraduatedColour())
    {
      threshline.value = oldcs.getThreshold();
      cs = new FeatureColour(oldcs.getColour(), oldcs.getMinColour(),
              oldcs.getMaxColour(), oldcs.getNoColour(), min, max);
    }
    else
    {
      // promote original color to a graduated color
      Color bl = Color.black;
      if (oldcs.isSimpleColour())
      {
        bl = oldcs.getColour();
      }
      // original colour becomes the maximum colour
      cs = new FeatureColour(bl, Color.white, bl, Color.white, mm[0],
              mm[1]);
    }
    minColour.setBackground(cs.getMinColour());
    maxColour.setBackground(cs.getMaxColour());
    minColour.setForeground(cs.getMinColour());
    maxColour.setForeground(cs.getMaxColour());
    colourFromLabel.setState(cs.isColourByLabel());
    adjusting = true;

    try
    {
      jbInit();
    } catch (Exception ex)
    {
    }
    threshold.select(
            cs.isAboveThreshold() ? 1 : (cs.isBelowThreshold() ? 2 : 0));

    adjusting = false;
    changeColour(true);
    colourFromLabel.addItemListener(this);
    slider.addAdjustmentListener(this);
    slider.addMouseListener(this);
    owner = (af != null) ? af : fs.frame;
    frame = new JVDialog(owner, MessageManager
            .formatMessage("label.variable_color_for", new String[]
            { type }), true, 480, 248);
    frame.setMainPanel(this);
    validate();
    frame.setVisible(true);
    if (frame.accept)
    {
      changeColour(true);
    }
    else
    {
      // cancel
      reset();
      frame.setVisible(false);
    }
  }

  public FeatureColourChooser()
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
    Label minLabel = new Label(
            MessageManager.getString("label.min_value") + COLON);
    Label maxLabel = new Label(
            MessageManager.getString("label.max_value") + COLON);
    minLabel.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    maxLabel.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    // minColour.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    // minColour.setLabel("Min Colour");

    minColour.setBounds(0, 0, 40, 27);
    maxColour.setBounds(0, 0, 40, 27);
    minColour.addMouseListener(this);

    maxColour.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    maxColour.addMouseListener(this);

    thresholdIsMin.addItemListener(this);

    this.setLayout(new GridLayout(4, 1));
    Panel jPanel1 = new Panel();
    jPanel1.setLayout(new FlowLayout());
    Panel jPanel2 = new Panel();
    jPanel2.setLayout(new FlowLayout());
    Panel jPanel3 = new Panel();
    jPanel3.setLayout(new GridLayout(1, 1));
    Panel jPanel4 = new Panel();
    jPanel4.setLayout(new FlowLayout());
    jPanel1.setBackground(Color.white);
    jPanel2.setBackground(Color.white);
    jPanel4.setBackground(Color.white);
    threshold.addItemListener(this);
    threshold.addItem(MessageManager
            .getString("label.threshold_feature_no_threshold"));
    threshold.addItem(MessageManager
            .getString("label.threshold_feature_above_threshold"));
    threshold.addItem(MessageManager
            .getString("label.threshold_feature_below_threshold"));
    thresholdValue.addActionListener(this);
    thresholdValue.addFocusListener(new FocusAdapter()
    {
      @Override
      public void focusLost(FocusEvent e)
      {
        thresholdValue_actionPerformed();
      }
    });
    slider.setBackground(Color.white);
    slider.setEnabled(false);
    slider.setSize(new Dimension(93, 21));
    thresholdValue.setEnabled(false);
    thresholdValue.setSize(new Dimension(79, 22)); // setBounds(new
                                                   // Rectangle(248, 2, 79,
                                                   // 22));
    thresholdValue.setColumns(5);
    jPanel3.setBackground(Color.white);

    colourFromLabel.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    colourFromLabel
            .setLabel(MessageManager.getString("label.colour_by_label"));
    colourFromLabel.setSize(new Dimension(139, 22));
    // threshold.setBounds(new Rectangle(11, 3, 139, 22));
    thresholdIsMin.setBackground(Color.white);
    thresholdIsMin
            .setLabel(MessageManager.getString("label.threshold_minmax"));
    thresholdIsMin.setSize(new Dimension(135, 23));
    // thresholdIsMin.setBounds(new Rectangle(328, 3, 135, 23));
    jPanel1.add(minLabel);
    jPanel1.add(minColour);
    jPanel1.add(maxLabel);
    jPanel1.add(maxColour);
    jPanel1.add(colourFromLabel);
    jPanel2.add(threshold);
    jPanel3.add(slider);
    jPanel4.add(thresholdValue);
    jPanel4.add(thresholdIsMin);
    this.add(jPanel1);// , java.awt.BorderLayout.NORTH);
    this.add(jPanel2);// , java.awt.BorderLayout.NORTH);
    this.add(jPanel3);// , java.awt.BorderLayout.CENTER);
    this.add(jPanel4);// , java.awt.BorderLayout.CENTER);
  }

  @Override
  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == thresholdValue)
    {
      thresholdValue_actionPerformed();
    }
    else if (evt.getSource() == minColour)
    {
      minColour_actionPerformed(null);
    }
    else if (evt.getSource() == maxColour)
    {
      maxColour_actionPerformed(null);
    }
    else
    {
      changeColour(true);
    }
  }

  /**
   * Action on input of a value for colour score threshold
   */
  protected void thresholdValue_actionPerformed()
  {
    try
    {
      float f = Float.valueOf(thresholdValue.getText()).floatValue();
      slider.setValue((int) (f * SCALE_FACTOR_1K));
      adjustmentValueChanged(null);

      /*
       * force repaint of any Overview window or structure
       */
      changeColour(true);
    } catch (NumberFormatException ex)
    {
    }
  }

  @Override
  public void itemStateChanged(ItemEvent evt)
  {
    maxColour.setEnabled(!colourFromLabel.getState());
    minColour.setEnabled(!colourFromLabel.getState());
    changeColour(true);
  }

  /**
   * Handler called when the value of the threshold slider changes, either by
   * user action or programmatically
   */
  @Override
  public void adjustmentValueChanged(AdjustmentEvent evt)
  {
    if (!adjusting)
    {
      thresholdValue.setText((slider.getValue() / 1000f) + "");
      valueChanged();
    }
  }

  /**
   * Responds to a change of colour threshold by computing the absolute value
   * and refreshing the alignment.
   */
  protected void valueChanged()
  {
    threshline.value = slider.getValue() / 1000f;
    cs.setThreshold(threshline.value);
    changeColour(false);
    PaintRefresher.Refresh(this, fr.getViewport().getSequenceSetId());
  }

  public void minColour_actionPerformed(Color newCol)
  {
    if (newCol == null)
    {
      new UserDefinedColours(this, minColour.getBackground(), owner,
              MessageManager
                      .getString("label.select_colour_minimum_value"));
    }
    else
    {
      minColour.setBackground(newCol);
      minColour.setForeground(newCol);
      minColour.repaint();
      changeColour(true);
    }

  }

  public void maxColour_actionPerformed(Color newCol)
  {
    if (newCol == null)
    {
      new UserDefinedColours(this, maxColour.getBackground(), owner,
              MessageManager
                      .getString("label.select_colour_maximum_value"));
    }
    else
    {
      maxColour.setBackground(newCol);
      maxColour.setForeground(newCol);
      maxColour.repaint();
      changeColour(true);
    }
  }

  void changeColour(boolean updateOverview)
  {
    // Check if combobox is still adjusting
    if (adjusting)
    {
      return;
    }

    int thresholdOption = AnnotationColourGradient.NO_THRESHOLD;
    if (threshold.getSelectedIndex() == 1)
    {
      thresholdOption = AnnotationColourGradient.ABOVE_THRESHOLD;
    }
    else if (threshold.getSelectedIndex() == 2)
    {
      thresholdOption = AnnotationColourGradient.BELOW_THRESHOLD;
    }

    slider.setEnabled(true);
    thresholdValue.setEnabled(true);
    Color minc = minColour.getBackground();
    Color maxc = maxColour.getBackground();
    FeatureColour acg = new FeatureColour(maxc, minc, maxc, minc, min, max);

    acg.setColourByLabel(colourFromLabel.getState());
    maxColour.setEnabled(!colourFromLabel.getState());
    minColour.setEnabled(!colourFromLabel.getState());
    if (thresholdOption == AnnotationColourGradient.NO_THRESHOLD)
    {
      slider.setEnabled(false);
      thresholdValue.setEnabled(false);
      thresholdValue.setText("");
    }

    if (thresholdOption != AnnotationColourGradient.NO_THRESHOLD)
    {
      adjusting = true;
      acg.setThreshold(threshline.value);

      slider.setMinimum((int) (min * SCALE_FACTOR_1K));
      slider.setMaximum((int) (max * SCALE_FACTOR_1K));
      slider.setValue((int) (threshline.value * SCALE_FACTOR_1K));
      thresholdValue.setText(threshline.value + "");
      slider.setEnabled(true);
      thresholdValue.setEnabled(true);
      adjusting = false;
    }

    acg.setAboveThreshold(
            thresholdOption == AnnotationColourGradient.ABOVE_THRESHOLD);
    acg.setBelowThreshold(
            thresholdOption == AnnotationColourGradient.BELOW_THRESHOLD);

    if (thresholdIsMin.getState()
            && thresholdOption != AnnotationColourGradient.NO_THRESHOLD)
    {
      if (thresholdOption == AnnotationColourGradient.ABOVE_THRESHOLD)
      {
        acg = new FeatureColour(acg.getColour(), acg.getMinColour(),
                acg.getMaxColour(), acg.getNoColour(), threshline.value,
                max);
      }
      else
      {
        acg = new FeatureColour(acg.getColour(), acg.getMinColour(),
                acg.getMaxColour(), acg.getNoColour(), min,
                threshline.value);
      }
    }

    fr.setColour(type, acg);
    cs = acg;
    fs.selectionChanged(updateOverview);
  }

  void reset()
  {
    fr.setColour(type, oldcs);
    fs.selectionChanged(true);
  }

  @Override
  public void mouseClicked(MouseEvent evt)
  {
  }

  @Override
  public void mousePressed(MouseEvent evt)
  {
  }

  @Override
  public void mouseReleased(MouseEvent evt)
  {
    if (evt.getSource() == minColour)
    {
      minColour_actionPerformed(null);
    }
    else if (evt.getSource() == maxColour)
    {
      maxColour_actionPerformed(null);
    }
    else
    {
      changeColour(true);
      // PaintRefresher.Refresh(this, fr.getViewport().getSequenceSetId());
    }
  }

  @Override
  public void mouseEntered(MouseEvent evt)
  {
  }

  @Override
  public void mouseExited(MouseEvent evt)
  {
  }

}
