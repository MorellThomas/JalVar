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

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.GraphLine;
import jalview.schemes.AnnotationColourGradient;
import jalview.util.MessageManager;

import java.awt.Color;
import java.awt.Dimension;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public abstract class AnnotationRowFilter extends JPanel
{
  private static final String TWO_DP = "%.2f";

  private final static MathContext FOUR_SIG_FIG = new MathContext(4);

  protected AlignViewport av;

  protected AlignmentPanel ap;

  protected int[] annmap;

  protected boolean adjusting = false;

  protected JCheckBox seqAssociated = new JCheckBox();

  protected JCheckBox percentThreshold = new JCheckBox();

  protected Slider slider;

  protected JTextField thresholdValue = new JTextField(20);

  protected JInternalFrame frame;

  protected JButton ok = new JButton();

  protected JButton cancel = new JButton();

  /**
   * enabled if the user is dragging the slider - try to keep updates to a
   * minimun
   */
  protected boolean sliderDragging = false;

  protected JComboBox<String> threshold = new JComboBox<>();

  protected JComboBox<String> annotations;

  /*
   * map from annotation to its menu item display label
   * - so we know which item to pre-select on restore
   */
  private Map<AlignmentAnnotation, String> annotationLabels;

  private AlignmentAnnotation currentAnnotation;

  /**
   * Constructor
   * 
   * @param viewport
   * @param alignPanel
   */
  public AnnotationRowFilter(AlignViewport viewport,
          final AlignmentPanel alignPanel)
  {
    this.av = viewport;
    this.ap = alignPanel;
    this.slider = new Slider(0f, 100f, 50f);

    thresholdValue.addFocusListener(new FocusAdapter()
    {
      @Override
      public void focusLost(FocusEvent e)
      {
        thresholdValue_actionPerformed();
      }
    });
  }

  protected void addSliderChangeListener()
  {

    slider.addChangeListener(new ChangeListener()
    {
      @Override
      public void stateChanged(ChangeEvent evt)
      {
        if (!adjusting)
        {
          setThresholdValueText();
          valueChanged(!sliderDragging);
        }
      }
    });
  }

  /**
   * update the text field from the threshold slider. preserves state of
   * 'adjusting' so safe to call in init.
   */
  protected void setThresholdValueText()
  {
    boolean oldadj = adjusting;
    adjusting = true;
    if (percentThreshold.isSelected())
    {
      thresholdValue
              .setText(String.format(TWO_DP, getSliderPercentageValue()));
    }
    else
    {
      /*
       * round to 4 significant digits without trailing zeroes
       */
      float f = getSliderValue();
      BigDecimal formatted = new BigDecimal(f).round(FOUR_SIG_FIG)
              .stripTrailingZeros();
      thresholdValue.setText(formatted.toPlainString());
    }
    adjusting = oldadj;
  }

  /**
   * Answers the value of the slider position (descaled to 'true' value)
   * 
   * @return
   */
  protected float getSliderValue()
  {
    return slider.getSliderValue();
  }

  /**
   * Sets the slider value (scaled from the true value to the slider range)
   * 
   * @param value
   */
  protected void setSliderValue(float value)
  {
    slider.setSliderValue(value);
  }

  /**
   * Answers the value of the slider position as a percentage between minimum
   * and maximum of its range
   * 
   * @return
   */
  protected float getSliderPercentageValue()
  {
    return slider.getSliderPercentageValue();
  }

  /**
   * Sets the slider position for a given percentage value of its min-max range
   * 
   * @param pct
   */
  protected void setSliderPercentageValue(float pct)
  {
    slider.setSliderPercentageValue(pct);
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
        sliderDragReleased();
      }
    });
  }

  /**
   * Builds and returns a list of menu items (display text) for choice of
   * annotation. Also builds maps between annotations, their positions in the
   * list, and their display labels in the list.
   * 
   * @param isSeqAssociated
   * @return
   */
  public Vector<String> getAnnotationItems(boolean isSeqAssociated)
  {
    annotationLabels = new HashMap<>();

    Vector<String> list = new Vector<>();
    int index = 1;
    int[] anmap = new int[av.getAlignment()
            .getAlignmentAnnotation().length];
    seqAssociated.setEnabled(false);
    for (int i = 0; i < av.getAlignment()
            .getAlignmentAnnotation().length; i++)
    {
      AlignmentAnnotation annotation = av.getAlignment()
              .getAlignmentAnnotation()[i];
      if (annotation.sequenceRef == null)
      {
        if (isSeqAssociated)
        {
          continue;
        }
      }
      else
      {
        seqAssociated.setEnabled(true);
      }
      String label = annotation.label;
      // add associated sequence ID if available
      if (!isSeqAssociated && annotation.sequenceRef != null)
      {
        label = label + "_" + annotation.sequenceRef.getName();
      }
      // make label unique
      if (!list.contains(label))
      {
        anmap[list.size()] = i;
        list.add(label);
        annotationLabels.put(annotation, label);
      }
      else
      {
        if (!isSeqAssociated)
        {
          anmap[list.size()] = i;
          label = label + "_" + (index++);
          list.add(label);
          annotationLabels.put(annotation, label);
        }
      }
    }
    this.annmap = new int[list.size()];
    System.arraycopy(anmap, 0, this.annmap, 0, this.annmap.length);
    return list;
  }

  protected int getSelectedThresholdItem(int indexValue)
  {
    int selectedThresholdItem = -1;
    if (indexValue == 1)
    {
      selectedThresholdItem = AnnotationColourGradient.ABOVE_THRESHOLD;
    }
    else if (indexValue == 2)
    {
      selectedThresholdItem = AnnotationColourGradient.BELOW_THRESHOLD;
    }
    return selectedThresholdItem;
  }

  public void ok_actionPerformed()
  {
    try
    {
      frame.setClosed(true);
    } catch (Exception ex)
    {
    }
  }

  public void cancel_actionPerformed()
  {
    reset();
    ap.paintAlignment(true, true);
    try
    {
      frame.setClosed(true);
    } catch (Exception ex)
    {
    }
  }

  protected void thresholdCheck_actionPerformed()
  {
    updateView();
  }

  protected void selectedAnnotationChanged()
  {
    updateView();
  }

  protected void threshold_actionPerformed()
  {
    updateView();
  }

  /**
   * Updates the slider position, and the display, for an update in the slider's
   * text input field
   */
  protected void thresholdValue_actionPerformed()
  {
    try
    {
      float f = Float.parseFloat(thresholdValue.getText());
      if (percentThreshold.isSelected())
      {
        setSliderPercentageValue(f);
      }
      else
      {
        setSliderValue(f);
      }
      updateView();
    } catch (NumberFormatException ex)
    {
    }
  }

  protected void percentageValue_actionPerformed()
  {
    setThresholdValueText();
  }

  protected void thresholdIsMin_actionPerformed()
  {
    updateView();
  }

  protected void populateThresholdComboBox(JComboBox<String> thresh)
  {
    thresh.addItem(MessageManager
            .getString("label.threshold_feature_no_threshold"));
    thresh.addItem(MessageManager
            .getString("label.threshold_feature_above_threshold"));
    thresh.addItem(MessageManager
            .getString("label.threshold_feature_below_threshold"));
  }

  /**
   * Rebuilds the drop-down list of annotations to choose from when the 'per
   * sequence only' checkbox is checked or unchecked.
   * 
   * @param anns
   */
  protected void seqAssociated_actionPerformed(JComboBox<String> anns)
  {
    adjusting = true;
    String cursel = (String) anns.getSelectedItem();
    boolean isvalid = false;
    boolean isseqs = seqAssociated.isSelected();
    anns.removeAllItems();
    for (String anitem : getAnnotationItems(seqAssociated.isSelected()))
    {
      if (anitem.equals(cursel) || (isseqs && cursel.startsWith(anitem)))
      {
        isvalid = true;
        cursel = anitem;
      }
      anns.addItem(anitem);
    }
    if (isvalid)
    {
      anns.setSelectedItem(cursel);
    }
    else
    {
      if (anns.getItemCount() > 0)
      {
        anns.setSelectedIndex(0);
      }
    }
    adjusting = false;

    updateView();
  }

  protected void propagateSeqAssociatedThreshold(boolean allAnnotation,
          AlignmentAnnotation annotation)
  {
    if (annotation.sequenceRef == null || annotation.threshold == null)
    {
      return;
    }

    float thr = annotation.threshold.value;
    for (int i = 0; i < av.getAlignment()
            .getAlignmentAnnotation().length; i++)
    {
      AlignmentAnnotation aa = av.getAlignment()
              .getAlignmentAnnotation()[i];
      if (aa.label.equals(annotation.label)
              && (annotation.getCalcId() == null ? aa.getCalcId() == null
                      : annotation.getCalcId().equals(aa.getCalcId())))
      {
        if (aa.threshold == null)
        {
          aa.threshold = new GraphLine(annotation.threshold);
        }
        else
        {
          aa.threshold.value = thr;
        }
      }
    }
  }

  public AlignmentAnnotation getCurrentAnnotation()
  {
    return currentAnnotation;
  }

  protected void setCurrentAnnotation(AlignmentAnnotation annotation)
  {
    this.currentAnnotation = annotation;
  }

  /**
   * update associated view model and trigger any necessary repaints.
   * 
   * @param updateAllAnnotation
   */
  protected abstract void valueChanged(boolean updateAllAnnotation);

  protected abstract void updateView();

  protected abstract void reset();

  protected String getAnnotationMenuLabel(AlignmentAnnotation ann)
  {
    return annotationLabels.get(ann);
  }

  protected void jbInit()
  {
    ok.setOpaque(false);
    ok.setText(MessageManager.getString("action.ok"));
    ok.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        ok_actionPerformed();
      }
    });

    cancel.setOpaque(false);
    cancel.setText(MessageManager.getString("action.cancel"));
    cancel.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        cancel_actionPerformed();
      }
    });

    annotations.addItemListener(new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent e)
      {
        selectedAnnotationChanged();
      }
    });
    annotations.setToolTipText(
            MessageManager.getString("info.select_annotation_row"));

    threshold.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        threshold_actionPerformed();
      }
    });

    thresholdValue.setEnabled(false);
    thresholdValue.setColumns(7);
    thresholdValue.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        thresholdValue_actionPerformed();
      }
    });

    percentThreshold
            .setText(MessageManager.getString("label.as_percentage"));
    percentThreshold.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        if (!adjusting)
        {
          percentageValue_actionPerformed();
        }
      }
    });
    slider.setPaintLabels(false);
    slider.setPaintTicks(true);
    slider.setBackground(Color.white);
    slider.setEnabled(false);
    slider.setOpaque(false);
    slider.setPreferredSize(new Dimension(100, 32));
  }

  public JComboBox<String> getThreshold()
  {
    return threshold;
  }

  public void setThreshold(JComboBox<String> thresh)
  {
    this.threshold = thresh;
  }

  public JComboBox<String> getAnnotations()
  {
    return annotations;
  }

  public void setAnnotations(JComboBox<String> anns)
  {
    this.annotations = anns;
  }

  protected void sliderDragReleased()
  {
    if (sliderDragging)
    {
      sliderDragging = false;
      valueChanged(true);
    }
  }

  /**
   * Sets the min-max range and current value of the slider, with rescaling from
   * true values to slider range as required
   * 
   * @param min
   * @param max
   * @param value
   */
  protected void setSliderModel(float min, float max, float value)
  {
    slider.setSliderModel(min, max, value);

    /*
     * tick mark every 10th position
     */
    slider.setMajorTickSpacing(
            (slider.getMaximum() - slider.getMinimum()) / 10);
  }
}
