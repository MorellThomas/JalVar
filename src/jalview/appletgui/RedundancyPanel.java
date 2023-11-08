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

import jalview.analysis.AlignSeq;
import jalview.commands.CommandI;
import jalview.commands.EditCommand;
import jalview.commands.EditCommand.Action;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.util.MessageManager;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

public class RedundancyPanel extends SliderPanel
        implements Runnable, WindowListener
{
  Stack historyList = new Stack(); // simpler than synching with alignFrame.

  float[] redundancy;

  SequenceI[] originalSequences;

  Frame frame;

  Vector redundantSeqs;

  public RedundancyPanel(AlignmentPanel ap)
  {
    super(ap, 0, false, null);

    redundantSeqs = new Vector();
    this.ap = ap;
    undoButton.setVisible(true);
    applyButton.setVisible(true);
    allGroupsCheck.setVisible(false);

    label.setText(
            MessageManager.getString("label.enter_redundancy_threshold"));
    valueField.setText("100");

    slider.setVisibleAmount(1);
    slider.setMinimum(0);
    slider.setMaximum(100 + slider.getVisibleAmount());
    slider.setValue(100);

    slider.addAdjustmentListener(new AdjustmentListener()
    {
      @Override
      public void adjustmentValueChanged(AdjustmentEvent evt)
      {
        valueField.setText(String.valueOf(slider.getValue()));
        sliderValueChanged();
      }
    });

    frame = new Frame();
    frame.add(this);
    jalview.bin.JalviewLite.addFrame(frame, MessageManager
            .getString("label.redundancy_threshold_selection"), 400, 100);

    frame.addWindowListener(this);

    Thread worker = new Thread(this);
    worker.start();
  }

  /**
   * This is a copy of remove redundancy in jalivew.datamodel.Alignment except
   * we dont want to remove redundancy, just calculate once so we can use the
   * slider to dynamically hide redundant sequences
   * 
   * @param threshold
   *          DOCUMENT ME!
   * @param sel
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public void run()
  {
    label.setText(MessageManager.getString("label.calculating"));

    slider.setVisible(false);
    applyButton.setEnabled(false);
    valueField.setVisible(false);

    validate();

    String[] omitHidden = null;

    SequenceGroup sg = ap.av.getSelectionGroup();
    int height;

    int start, end;

    if ((sg != null) && (sg.getSize() >= 1))
    {
      originalSequences = sg.getSequencesInOrder(ap.av.getAlignment());
      start = sg.getStartRes();
      end = sg.getEndRes();
    }
    else
    {
      originalSequences = ap.av.getAlignment().getSequencesArray();
      start = 0;
      end = ap.av.getAlignment().getWidth();
    }

    height = originalSequences.length;

    redundancy = AlignSeq.computeRedundancyMatrix(originalSequences,
            omitHidden, start, end, false);
    label.setText(
            MessageManager.getString("label.enter_redundancy_threshold"));
    slider.setVisible(true);
    applyButton.setEnabled(true);
    valueField.setVisible(true);

    validate();
    sliderValueChanged();
    // System.out.println("blob done "+ (System.currentTimeMillis()-start));
  }

  void sliderValueChanged()
  {
    if (redundancy == null)
    {
      return;
    }

    float value = slider.getValue();

    List<SequenceI> redundantSequences = new ArrayList<>();
    for (int i = 0; i < redundancy.length; i++)
    {
      if (value <= redundancy[i])
      {
        redundantSequences.add(originalSequences[i]);
      }
    }

    ap.idPanel.idCanvas.setHighlighted(redundantSequences);
    PaintRefresher.Refresh(this, ap.av.getSequenceSetId(), true, true);

  }

  @Override
  public void applyButton_actionPerformed()
  {
    Vector del = new Vector();

    undoButton.setEnabled(true);

    float value = slider.getValue();
    SequenceGroup sg = ap.av.getSelectionGroup();

    for (int i = 0; i < redundancy.length; i++)
    {
      if (value <= redundancy[i])
      {
        del.addElement(originalSequences[i]);
      }
    }

    // This has to be done before the restoreHistoryItem method of alignFrame
    // will
    // actually restore these sequences.
    if (del.size() > 0)
    {
      SequenceI[] deleted = new SequenceI[del.size()];

      int width = 0;
      for (int i = 0; i < del.size(); i++)
      {
        deleted[i] = (SequenceI) del.elementAt(i);
        if (deleted[i].getLength() > width)
        {
          width = deleted[i].getLength();
        }
      }

      EditCommand cut = new EditCommand(
              MessageManager.getString("action.remove_redundancy"),
              Action.CUT, deleted, 0, width, ap.av.getAlignment());
      AlignmentI alignment = ap.av.getAlignment();
      for (int i = 0; i < del.size(); i++)
      {
        alignment.deleteSequence(deleted[i]);
        if (sg != null)
        {
          sg.deleteSequence(deleted[i], false);
        }
      }

      historyList.push(cut);

      ap.alignFrame.addHistoryItem(cut);

      PaintRefresher.Refresh(this, ap.av.getSequenceSetId(), true, true);
      ap.av.firePropertyChange("alignment", null,
              ap.av.getAlignment().getSequences());
    }

  }

  @Override
  public void undoButton_actionPerformed()
  {
    CommandI command = (CommandI) historyList.pop();
    command.undoCommand(null);

    if (ap.av.getHistoryList().contains(command))
    {
      ap.av.getHistoryList().remove(command);
      ap.alignFrame.updateEditMenuBar();
      ap.av.firePropertyChange("alignment", null,
              ap.av.getAlignment().getSequences());
    }

    ap.paintAlignment(true, true);

    if (historyList.size() == 0)
    {
      undoButton.setEnabled(false);
    }
  }

  public void valueField_actionPerformed(ActionEvent e)
  {
    try
    {
      int i = Integer.parseInt(valueField.getText());
      slider.setValue(i);
    } catch (Exception ex)
    {
      valueField.setText(slider.getValue() + "");
    }
  }

  @Override
  public void windowOpened(WindowEvent evt)
  {
  }

  @Override
  public void windowClosing(WindowEvent evt)
  {
    ap.idPanel.idCanvas.setHighlighted(null);
  }

  @Override
  public void windowClosed(WindowEvent evt)
  {
  }

  @Override
  public void windowActivated(WindowEvent evt)
  {
  }

  @Override
  public void windowDeactivated(WindowEvent evt)
  {
  }

  @Override
  public void windowIconified(WindowEvent evt)
  {
  }

  @Override
  public void windowDeiconified(WindowEvent evt)
  {
  }
}
