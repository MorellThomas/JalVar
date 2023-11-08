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

import jalview.analysis.Conservation;
import jalview.datamodel.SequenceGroup;
import jalview.renderer.ResidueShaderI;
import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class SliderPanel extends Panel
        implements ActionListener, AdjustmentListener, MouseListener
{
  private static final String BACKGROUND = "Background";

  AlignmentPanel ap;

  boolean forConservation = true;

  ResidueShaderI cs;

  static Frame conservationSlider;

  static Frame PIDSlider;

  public static int setConservationSlider(AlignmentPanel ap,
          ResidueShaderI ccs, String source)
  {
    SliderPanel sp = null;

    if (conservationSlider == null)
    {
      sp = new SliderPanel(ap, ccs.getConservationInc(), true, ccs);
      conservationSlider = new Frame();
      conservationSlider.add(sp);
    }
    else
    {
      sp = (SliderPanel) conservationSlider.getComponent(0);
      sp.cs = ccs;
      sp.valueField.setText(String.valueOf(ccs.getConservationInc()));
    }

    conservationSlider.setTitle(MessageManager.formatMessage(
            "label.conservation_colour_increment", new String[]
            { source == null ? BACKGROUND : source }));
    List<SequenceGroup> groups = ap.av.getAlignment().getGroups();
    if (groups != null && !groups.isEmpty())
    {
      sp.setAllGroupsCheckEnabled(true);
    }
    else
    {
      sp.setAllGroupsCheckEnabled(false);
    }

    return sp.getValue();
  }

  public static void showConservationSlider()
  {
    try
    {
      PIDSlider.setVisible(false);
      PIDSlider = null;
    } catch (Exception ex)
    {
    }

    if (!conservationSlider.isVisible())
    {
      jalview.bin.JalviewLite.addFrame(conservationSlider,
              conservationSlider.getTitle(), 420, 100);
      conservationSlider.addWindowListener(new WindowAdapter()
      {
        @Override
        public void windowClosing(WindowEvent e)
        {
          conservationSlider = null;
        }
      });

    }

  }

  public static int setPIDSliderSource(AlignmentPanel ap,
          ResidueShaderI ccs, String source)
  {
    SliderPanel pid = null;
    if (PIDSlider == null)
    {
      pid = new SliderPanel(ap, ccs.getThreshold(), false, ccs);
      PIDSlider = new Frame();
      PIDSlider.add(pid);
    }
    else
    {
      pid = (SliderPanel) PIDSlider.getComponent(0);
      pid.cs = ccs;
      pid.valueField.setText(String.valueOf(ccs.getThreshold()));
    }
    PIDSlider.setTitle(MessageManager.formatMessage(
            "label.percentage_identity_threshold", new String[]
            { source == null ? BACKGROUND : source }));

    if (ap.av.getAlignment().getGroups() != null)
    {
      pid.setAllGroupsCheckEnabled(true);
    }
    else
    {
      pid.setAllGroupsCheckEnabled(false);
    }

    return pid.getValue();

  }

  public static void showPIDSlider()
  {
    try
    {
      conservationSlider.setVisible(false);
      conservationSlider = null;
    } catch (Exception ex)
    {
    }

    if (!PIDSlider.isVisible())
    {
      jalview.bin.JalviewLite.addFrame(PIDSlider, PIDSlider.getTitle(), 420,
              100);
      PIDSlider.addWindowListener(new WindowAdapter()
      {
        @Override
        public void windowClosing(WindowEvent e)
        {
          PIDSlider = null;
        }
      });
    }

  }

  /**
   * Hides the PID slider panel if it is shown
   */
  public static void hidePIDSlider()
  {
    if (PIDSlider != null)
    {
      PIDSlider.setVisible(false);
      PIDSlider = null;
    }
  }

  /**
   * Hides the Conservation slider panel if it is shown
   */
  public static void hideConservationSlider()
  {
    if (conservationSlider != null)
    {
      conservationSlider.setVisible(false);
      conservationSlider = null;
    }
  }

  public SliderPanel(AlignmentPanel ap, int value, boolean forConserve,
          ResidueShaderI shader)
  {
    try
    {
      jbInit();
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    this.ap = ap;
    this.cs = shader;
    forConservation = forConserve;
    undoButton.setVisible(false);
    applyButton.setVisible(false);
    if (forConservation)
    {
      label.setText(MessageManager
              .getString("label.modify_conservation_visibility"));
      slider.setMinimum(0);
      slider.setMaximum(50 + slider.getVisibleAmount());
      slider.setUnitIncrement(1);
    }
    else
    {
      label.setText(MessageManager
              .getString("label.colour_residues_above_occurrence"));
      slider.setMinimum(0);
      slider.setMaximum(100 + slider.getVisibleAmount());
      slider.setBlockIncrement(1);
    }

    slider.addAdjustmentListener(this);
    slider.addMouseListener(this);

    slider.setValue(value);
    valueField.setText(value + "");
  }

  public void valueChanged(int i)
  {
    if (cs == null)
    {
      return;
    }
    if (forConservation)
    {
      cs.setConservationApplied(true);
      cs.setConservationInc(i);
    }
    else
    {
      cs.setThreshold(i, ap.av.isIgnoreGapsConsensus());
    }

    if (allGroupsCheck.getState())
    {
      for (SequenceGroup group : ap.av.getAlignment().getGroups())
      {
        ResidueShaderI groupColourScheme = group.getGroupColourScheme();
        if (forConservation)
        {
          if (!groupColourScheme.conservationApplied())
          {
            /*
             * first time the colour scheme has had Conservation shading applied
             * - compute conservation
             */
            Conservation c = new Conservation("Group",
                    group.getSequences(null), group.getStartRes(),
                    group.getEndRes());
            c.calculate();
            c.verdict(false, ap.av.getConsPercGaps());
            group.cs.setConservation(c);

          }
          groupColourScheme.setConservationApplied(true);
          groupColourScheme.setConservationInc(i);
        }
        else
        {
          groupColourScheme.setThreshold(i, ap.av.isIgnoreGapsConsensus());
        }
      }
    }

    ap.seqPanel.seqCanvas.repaint();
  }

  public void setAllGroupsCheckEnabled(boolean b)
  {
    allGroupsCheck.setState(ap.av.getColourAppliesToAllGroups());
    allGroupsCheck.setEnabled(b);
  }

  @Override
  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == applyButton)
    {
      applyButton_actionPerformed();
    }
    else if (evt.getSource() == undoButton)
    {
      undoButton_actionPerformed();
    }
    else if (evt.getSource() == valueField)
    {
      valueField_actionPerformed();
    }
  }

  @Override
  public void adjustmentValueChanged(AdjustmentEvent evt)
  {
    valueField.setText(slider.getValue() + "");
    valueChanged(slider.getValue());
  }

  public void valueField_actionPerformed()
  {
    try
    {
      int i = Integer.valueOf(valueField.getText());
      slider.setValue(i);
    } catch (NumberFormatException ex)
    {
      valueField.setText(String.valueOf(slider.getValue()));
    }
  }

  public void setValue(int value)
  {
    slider.setValue(value);
  }

  public int getValue()
  {
    return Integer.parseInt(valueField.getText());
  }

  // this is used for conservation colours, PID colours and redundancy threshold
  protected Scrollbar slider = new Scrollbar();

  protected TextField valueField = new TextField();

  protected Label label = new Label();

  Panel jPanel1 = new Panel();

  Panel jPanel2 = new Panel();

  protected Button applyButton = new Button();

  protected Button undoButton = new Button();

  FlowLayout flowLayout1 = new FlowLayout();

  protected Checkbox allGroupsCheck = new Checkbox();

  BorderLayout borderLayout1 = new BorderLayout();

  BorderLayout borderLayout2 = new BorderLayout();

  FlowLayout flowLayout2 = new FlowLayout();

  private void jbInit() throws Exception
  {
    this.setLayout(borderLayout2);

    // slider.setMajorTickSpacing(10);
    // slider.setMinorTickSpacing(1);
    // slider.setPaintTicks(true);
    slider.setBackground(Color.white);
    slider.setFont(new java.awt.Font("Verdana", 0, 11));
    slider.setOrientation(0);
    valueField.setFont(new java.awt.Font("Verdana", 0, 11));
    valueField.setText("   ");
    valueField.addActionListener(this);
    valueField.setColumns(3);
    valueField.addFocusListener(new FocusAdapter()
    {
      @Override
      public void focusLost(FocusEvent e)
      {
        valueField_actionPerformed();
        valueChanged(slider.getValue());
      }
    });

    label.setFont(new java.awt.Font("Verdana", 0, 11));
    label.setText(MessageManager.getString("label.set_this_label_text"));
    jPanel1.setLayout(borderLayout1);
    jPanel2.setLayout(flowLayout1);
    applyButton.setFont(new java.awt.Font("Verdana", 0, 11));
    applyButton.setLabel(MessageManager.getString("action.apply"));
    applyButton.addActionListener(this);
    undoButton.setEnabled(false);
    undoButton.setFont(new java.awt.Font("Verdana", 0, 11));
    undoButton.setLabel(MessageManager.getString("action.undo"));
    undoButton.addActionListener(this);
    allGroupsCheck.setEnabled(false);
    allGroupsCheck.setFont(new java.awt.Font("Verdana", 0, 11));
    allGroupsCheck.setLabel(
            MessageManager.getString("action.apply_threshold_all_groups"));
    allGroupsCheck
            .setName(MessageManager.getString("action.apply_all_groups"));
    this.setBackground(Color.white);
    this.setForeground(Color.black);
    jPanel2.add(label, null);
    jPanel2.add(applyButton, null);
    jPanel2.add(undoButton, null);
    jPanel2.add(allGroupsCheck);
    jPanel1.add(valueField, java.awt.BorderLayout.EAST);
    jPanel1.add(slider, java.awt.BorderLayout.CENTER);
    this.add(jPanel1, java.awt.BorderLayout.SOUTH);
    this.add(jPanel2, java.awt.BorderLayout.CENTER);
  }

  protected void applyButton_actionPerformed()
  {
  }

  protected void undoButton_actionPerformed()
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
  public void mouseClicked(MouseEvent evt)
  {
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
