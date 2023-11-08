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

import jalview.schemes.AnnotationColourGradient;
import jalview.util.MessageManager;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public abstract class AnnotationRowFilter extends Panel
{
  protected AlignViewport av;

  protected AlignmentPanel ap;

  protected int[] annmap;

  protected boolean enableSeqAss = false;

  private jalview.datamodel.AlignmentAnnotation currentAnnotation;

  protected boolean adjusting = false;

  protected Checkbox currentColours = new Checkbox();

  protected Panel minColour = new Panel();

  protected Panel maxColour = new Panel();

  protected Checkbox seqAssociated = new Checkbox();

  protected Checkbox thresholdIsMin = new Checkbox();

  protected Scrollbar slider = new Scrollbar(Scrollbar.HORIZONTAL);

  protected Checkbox percentThreshold = new Checkbox();

  protected TextField thresholdValue = new TextField(20);

  protected Frame frame;

  protected Button ok = new Button();

  protected Button cancel = new Button();

  /**
   * enabled if the user is dragging the slider - try to keep updates to a
   * minimun
   */
  protected boolean sliderDragging = false;

  public AnnotationRowFilter(AlignViewport av, final AlignmentPanel ap)
  {
    this.av = av;
    this.ap = ap;
  }

  public AnnotationRowFilter()
  {

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

  public void modelChanged()
  {
    seqAssociated.setEnabled(enableSeqAss);
  }

  public void ok_actionPerformed(ActionEvent e)
  {
    updateView();
    frame.setVisible(false);
  }

  public void cancel_actionPerformed(ActionEvent e)
  {
    reset();
    ap.paintAlignment(true, true);
    frame.setVisible(false);
  }

  public void thresholdCheck_actionPerformed(ActionEvent e)
  {
    updateView();
  }

  public void annotations_actionPerformed(ActionEvent e)
  {
    updateView();
  }

  public void threshold_actionPerformed(ActionEvent e)
  {
    updateView();
  }

  /**
   * update the text field from the threshold slider. preserves state of
   * 'adjusting' so safe to call in init.
   */
  protected void setThresholdValueText()
  {
    boolean oldadj = adjusting;
    adjusting = true;
    if (percentThreshold.getState())
    {
      double scl = slider.getMaximum() - slider.getMinimum();
      scl = (slider.getValue() - slider.getMinimum()) / scl;
      thresholdValue.setText(100f * scl + "");
    }
    else
    {
      thresholdValue.setText((slider.getValue() / 1000f) + "");
    }
    thresholdValue.setCaretPosition(0);
    adjusting = oldadj;
  }

  public void thresholdValue_actionPerformed(ActionEvent e)
  {
    try
    {
      float f = Float.parseFloat(thresholdValue.getText());
      if (percentThreshold.getState())
      {
        int pos = slider.getMinimum()
                + (int) ((slider.getMaximum() - slider.getMinimum()) * f
                        / 100f);
        slider.setValue(pos);
      }
      else
      {
        slider.setValue((int) (f * 1000));
      }
      valueChanged(false);
    } catch (NumberFormatException ex)
    {
    }
  }

  protected void percentageValue_actionPerformed()
  {
    setThresholdValueText();
  }

  protected void populateThresholdComboBox(Choice threshold)
  {
    threshold.addItem(MessageManager
            .getString("label.threshold_feature_no_threshold"));
    threshold.addItem(MessageManager
            .getString("label.threshold_feature_above_threshold"));
    threshold.addItem(MessageManager
            .getString("label.threshold_feature_below_threshold"));
  }

  public jalview.datamodel.AlignmentAnnotation getCurrentAnnotation()
  {
    return currentAnnotation;
  }

  public void setCurrentAnnotation(
          jalview.datamodel.AlignmentAnnotation currentAnnotation)
  {
    this.currentAnnotation = currentAnnotation;
  }

  public abstract void valueChanged(boolean updateAllAnnotation);

  public abstract void updateView();

  public abstract void reset();
}
