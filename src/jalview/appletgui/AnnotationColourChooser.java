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

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.SequenceGroup;
import jalview.schemes.AnnotationColourGradient;
import jalview.schemes.ColourSchemeI;
import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class AnnotationColourChooser extends Panel implements
        ActionListener, AdjustmentListener, ItemListener, MouseListener
{
  Frame frame;

  AlignViewport av;

  AlignmentPanel ap;

  ColourSchemeI oldcs;

  Map<SequenceGroup, ColourSchemeI> oldgroupColours;

  /*
   * map from annotation to its menu item display label
   * - so we know which item to pre-select on restore
   */
  private Map<AlignmentAnnotation, String> annotationLabels;

  AlignmentAnnotation currentAnnotation;

  boolean adjusting = false;

  public AnnotationColourChooser(AlignViewport av, AlignmentPanel ap)
  {
    try
    {
      jbInit();
    } catch (Exception ex)
    {
    }

    oldcs = av.getGlobalColourScheme();
    if (av.getAlignment().getGroups() != null)
    {
      oldgroupColours = new HashMap<>();
      for (SequenceGroup sg : ap.av.getAlignment().getGroups())
      {
        oldgroupColours.put(sg, sg.getColourScheme());
      }
    }
    this.av = av;
    this.ap = ap;

    slider.addAdjustmentListener(this);
    slider.addMouseListener(this);

    AlignmentAnnotation[] anns = av.getAlignment().getAlignmentAnnotation();
    if (anns == null)
    {
      return;
    }

    setDefaultMinMax();

    adjusting = true;
    if (oldcs instanceof AnnotationColourGradient)
    {
      AnnotationColourGradient acg = (AnnotationColourGradient) oldcs;
      currentColours.setState(
              acg.isPredefinedColours() || acg.getBaseColour() != null);
      if (!acg.isPredefinedColours() && acg.getBaseColour() == null)
      {
        minColour.setBackground(acg.getMinColour());
        maxColour.setBackground(acg.getMaxColour());
      }
      // seqAssociated.setState(acg.isSeqAssociated());
    }

    Vector<String> list = getAnnotationItems();

    for (int i = 0; i < list.size(); i++)
    {
      annotations.addItem(list.elementAt(i).toString());
    }

    threshold.addItem(MessageManager
            .getString("label.threshold_feature_no_threshold"));
    threshold.addItem(MessageManager
            .getString("label.threshold_feature_above_threshold"));
    threshold.addItem(MessageManager
            .getString("label.threshold_feature_below_threshold"));

    if (oldcs instanceof AnnotationColourGradient)
    {
      AnnotationColourGradient acg = (AnnotationColourGradient) oldcs;
      String label = annotationLabels.get(acg.getAnnotation());
      annotations.select(label);
      switch (acg.getAboveThreshold())
      {
      case AnnotationColourGradient.NO_THRESHOLD:
        threshold.select(0);
        break;
      case AnnotationColourGradient.ABOVE_THRESHOLD:
        threshold.select(1);
        break;
      case AnnotationColourGradient.BELOW_THRESHOLD:
        threshold.select(1);
        break;
      default:
        throw new Error(MessageManager.getString(
                "error.implementation_error_dont_know_threshold_annotationcolourgradient"));
      }
      thresholdIsMin.setState(acg.isThresholdIsMinMax());
      thresholdValue.setText("" + acg.getAnnotationThreshold());
    }

    adjusting = false;

    changeColour();

    frame = new Frame();
    frame.add(this);
    jalview.bin.JalviewLite.addFrame(frame,
            MessageManager.getString("label.colour_by_annotation"), 560,
            175);
    validate();
  }

  /**
   * Builds and returns a list of menu items (display text) for choice of
   * annotation. Also builds a map between annotations and their display labels.
   * 
   * @return
   */
  protected Vector<String> getAnnotationItems()
  {
    // TODO remove duplication with gui.AnnotationRowFilter
    // TODO add 'per sequence only' option / parameter

    annotationLabels = new HashMap<>();
    Vector<String> list = new Vector<>();
    AlignmentAnnotation[] anns = av.getAlignment().getAlignmentAnnotation();
    if (anns == null)
    {
      return list;
    }
    int index = 1;
    for (int i = 0; i < anns.length; i++)
    {
      String label = anns[i].label;
      if (anns[i].sequenceRef != null)
      {
        /*
         * be helpful and include sequence id in label for
         * sequence-associated annotation (JAL-2236)
         */
        label = label + "_" + anns[i].sequenceRef.getName();
      }
      if (!list.contains(label))
      {
        list.addElement(label);
        annotationLabels.put(anns[i], label);
      }
      else
      {
        label = label + "_" + (index++);
        list.addElement(label);
        annotationLabels.put(anns[i], label);
      }
    }
    return list;
  }

  private void setDefaultMinMax()
  {
    minColour.setBackground(av.applet.getDefaultColourParameter(
            "ANNOTATIONCOLOUR_MIN", Color.orange));
    maxColour.setBackground(av.applet
            .getDefaultColourParameter("ANNOTATIONCOLOUR_MAX", Color.red));

  }

  public AnnotationColourChooser()
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
    minColour.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    minColour.setLabel(MessageManager.getString("label.min_colour"));
    minColour.addActionListener(this);

    maxColour.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    maxColour.setLabel(MessageManager.getString("label.max_colour"));
    maxColour.addActionListener(this);

    thresholdIsMin.addItemListener(this);
    ok.setLabel(MessageManager.getString("action.ok"));
    ok.addActionListener(this);

    cancel.setLabel(MessageManager.getString("action.cancel"));
    cancel.addActionListener(this);

    defColours.setLabel(MessageManager.getString("action.set_defaults"));
    defColours.addActionListener(this);

    annotations.addItemListener(this);

    thresholdValue.addActionListener(this);
    slider.setBackground(Color.white);
    slider.setPreferredSize(new Dimension(193, 21));
    slider.setEnabled(false);
    thresholdValue.setPreferredSize(new Dimension(79, 22));
    thresholdValue.setEnabled(false);
    thresholdValue.setColumns(5);
    currentColours.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    currentColours.setLabel(
            MessageManager.getString("label.use_original_colours"));
    currentColours.addItemListener(this);

    thresholdIsMin.setBackground(Color.white);
    thresholdIsMin
            .setLabel(MessageManager.getString("label.threshold_minmax"));

    this.setLayout(borderLayout1);

    jPanel1.setBackground(Color.white);

    jPanel2.setLayout(new FlowLayout());
    jPanel2.setBackground(Color.white);
    threshold.addItemListener(this);
    jPanel3.setLayout(new FlowLayout());
    jPanel3.setBackground(Color.white);
    Panel jPanel4 = new Panel();
    jPanel4.setLayout(new BorderLayout());
    jPanel4.setBackground(Color.white);

    jPanel1.add(ok);
    jPanel1.add(cancel);

    jPanel2.add(annotations);
    jPanel2.add(currentColours);
    jPanel2.add(minColour);
    jPanel2.add(maxColour);

    jPanel4.add(thresholdIsMin, BorderLayout.WEST);
    jPanel4.add(slider, BorderLayout.CENTER);
    jPanel4.add(thresholdValue, BorderLayout.EAST);

    Panel jPanel34 = new Panel();
    jPanel34.setLayout(new BorderLayout());
    jPanel34.setBackground(Color.white);
    jPanel34.add(jPanel2, BorderLayout.NORTH);
    jPanel34.add(threshold, BorderLayout.WEST);
    jPanel3.add(defColours);
    jPanel34.add(jPanel3, BorderLayout.EAST);
    jPanel34.add(jPanel4, BorderLayout.SOUTH);

    this.add(jPanel34, java.awt.BorderLayout.CENTER);
    this.add(jPanel1, java.awt.BorderLayout.SOUTH);

  }

  Choice annotations = new Choice();

  Button minColour = new Button();

  Button maxColour = new Button();

  Button ok = new Button();

  Button cancel = new Button();

  Button defColours = new Button();

  Panel jPanel1 = new Panel();

  Panel jPanel2 = new Panel();

  Choice threshold = new Choice();

  FlowLayout flowLayout1 = new FlowLayout();

  Panel jPanel3 = new Panel();

  Scrollbar slider = new Scrollbar(Scrollbar.HORIZONTAL);

  TextField thresholdValue = new TextField(20);

  Checkbox currentColours = new Checkbox();

  BorderLayout borderLayout1 = new BorderLayout();

  Checkbox thresholdIsMin = new Checkbox();

  @Override
  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == thresholdValue)
    {
      try
      {
        float f = Float.valueOf(thresholdValue.getText()).floatValue();
        slider.setValue((int) (f * 1000));
        adjustmentValueChanged(null);
      } catch (NumberFormatException ex)
      {
      }
    }
    else if (evt.getSource() == minColour)
    {
      minColour_actionPerformed(null);
    }
    else if (evt.getSource() == maxColour)
    {
      maxColour_actionPerformed(null);
    }
    else if (evt.getSource() == defColours)
    {
      defColour_actionPerformed();
    }
    else if (evt.getSource() == ok)
    {
      frame.setVisible(false);
    }
    else if (evt.getSource() == cancel)
    {
      reset();
      ap.paintAlignment(true, true);
      frame.setVisible(false);
    }

    else
    {
      changeColour();
    }
  }

  @Override
  public void itemStateChanged(ItemEvent evt)
  {
    if (evt.getSource() == currentColours)
    {
      if (currentColours.getState())
      {
        reset();
      }

      maxColour.setEnabled(!currentColours.getState());
      minColour.setEnabled(!currentColours.getState());

    }

    changeColour();
  }

  @Override
  public void adjustmentValueChanged(AdjustmentEvent evt)
  {
    if (!adjusting)
    {
      thresholdValue.setText((slider.getValue() / 1000f) + "");
      if (currentColours.getState() && !(av
              .getGlobalColourScheme() instanceof AnnotationColourGradient))
      {
        changeColour();
      }

      currentAnnotation.threshold.value = slider.getValue() / 1000f;
      ap.paintAlignment(false, false);
    }
  }

  public void minColour_actionPerformed(Color newCol)
  {
    if (newCol != null)
    {
      minColour.setBackground(newCol);
      minColour.repaint();
      changeColour();
    }
    else
    {
      new UserDefinedColours(this, "Min Colour", minColour.getBackground());
    }

  }

  public void maxColour_actionPerformed(Color newCol)
  {
    if (newCol != null)
    {
      maxColour.setBackground(newCol);
      maxColour.repaint();
      changeColour();
    }
    else
    {
      new UserDefinedColours(this, "Max Colour", maxColour.getBackground());
    }
  }

  public void defColour_actionPerformed()
  {
    setDefaultMinMax();
    minColour.repaint();
    maxColour.repaint();
    changeColour();
  }

  void changeColour()
  {
    // Check if combobox is still adjusting
    if (adjusting)
    {
      return;
    }

    currentAnnotation = av.getAlignment()
            .getAlignmentAnnotation()[annotations.getSelectedIndex()];

    int aboveThreshold = -1;
    if (threshold.getSelectedIndex() == 1)
    {
      aboveThreshold = AnnotationColourGradient.ABOVE_THRESHOLD;
    }
    else if (threshold.getSelectedIndex() == 2)
    {
      aboveThreshold = AnnotationColourGradient.BELOW_THRESHOLD;
    }

    slider.setEnabled(true);
    thresholdValue.setEnabled(true);
    thresholdIsMin.setEnabled(true);

    if (aboveThreshold == AnnotationColourGradient.NO_THRESHOLD)
    {
      slider.setEnabled(false);
      thresholdValue.setEnabled(false);
      thresholdIsMin.setEnabled(false);
      thresholdValue.setText("");
    }
    else if (aboveThreshold != AnnotationColourGradient.NO_THRESHOLD
            && currentAnnotation.threshold == null)
    {
      currentAnnotation.setThreshold(new jalview.datamodel.GraphLine(
              (currentAnnotation.graphMax - currentAnnotation.graphMin)
                      / 2f,
              "Threshold", Color.black));
    }

    if (aboveThreshold != AnnotationColourGradient.NO_THRESHOLD)
    {
      adjusting = true;

      slider.setMinimum((int) (currentAnnotation.graphMin * 1000));
      slider.setMaximum((int) (currentAnnotation.graphMax * 1000));
      slider.setValue((int) (currentAnnotation.threshold.value * 1000));
      thresholdValue.setText(currentAnnotation.threshold.value + "");
      slider.setEnabled(true);
      thresholdValue.setEnabled(true);
      adjusting = false;
    }

    AnnotationColourGradient acg = null;
    if (currentColours.getState())
    {
    }
    else
    {
      acg = new AnnotationColourGradient(currentAnnotation,
              minColour.getBackground(), maxColour.getBackground(),
              aboveThreshold);
    }

    if (currentAnnotation.graphMin == 0f
            && currentAnnotation.graphMax == 0f)
    {
      acg.setPredefinedColours(true);
    }

    acg.setThresholdIsMinMax(thresholdIsMin.getState());

    av.setGlobalColourScheme(acg);

    // TODO: per group colour propagation not always desired
    if (av.getAlignment().getGroups() != null)
    {
      for (SequenceGroup sg : ap.av.getAlignment().getGroups())
      {
        if (sg.getColourScheme() == null)
        {
          continue;
        }

        if (currentColours.getState())
        {
          sg.setColourScheme(new AnnotationColourGradient(currentAnnotation,
                  sg.getColourScheme(), aboveThreshold));
        }
        else
        {
          sg.setColourScheme(new AnnotationColourGradient(currentAnnotation,
                  minColour.getBackground(), maxColour.getBackground(),
                  aboveThreshold));
        }
      }
    }

    // update colours in linked windows
    ap.alignmentChanged();
    ap.paintAlignment(true, true);
  }

  void reset()
  {
    av.setGlobalColourScheme(oldcs);
    if (av.getAlignment().getGroups() != null)
    {
      for (SequenceGroup sg : ap.av.getAlignment().getGroups())
      {
        sg.setColourScheme(oldgroupColours.get(sg));
      }
    }
    ap.paintAlignment(true, true);
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
    ap.paintAlignment(true, true);
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
