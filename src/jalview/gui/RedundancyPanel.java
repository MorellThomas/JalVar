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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import javax.swing.JInternalFrame;
import javax.swing.JProgressBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import jalview.analysis.AlignSeq;
import jalview.commands.CommandI;
import jalview.commands.EditCommand;
import jalview.commands.EditCommand.Action;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.jbgui.GSliderPanel;
import jalview.util.MessageManager;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class RedundancyPanel extends GSliderPanel implements Runnable
{
  AlignFrame af;

  AlignmentPanel ap;

  Stack<CommandI> historyList = new Stack<>();

  // simpler than synching with alignFrame.

  float[] redundancy;

  SequenceI[] originalSequences;

  JInternalFrame frame;

  Vector redundantSeqs;

  /**
   * Creates a new RedundancyPanel object.
   * 
   * @param ap
   *          DOCUMENT ME!
   * @param af
   *          DOCUMENT ME!
   */
  public RedundancyPanel(final AlignmentPanel ap, AlignFrame af)
  {
    this.ap = ap;
    this.af = af;
    redundantSeqs = new Vector();

    slider.addChangeListener(new ChangeListener()
    {
      @Override
      public void stateChanged(ChangeEvent evt)
      {
        valueField.setText(slider.getValue() + "");
        sliderValueChanged();
      }
    });

    applyButton.setText(MessageManager.getString("action.remove"));
    allGroupsCheck.setVisible(false);
    slider.setMinimum(0);
    slider.setMaximum(100);
    slider.setValue(100);

    Thread worker = new Thread(this);
    worker.start();

    frame = new JInternalFrame();
    frame.setFrameIcon(null);
    frame.setContentPane(this);
    Desktop.addInternalFrame(frame,
            MessageManager
                    .getString("label.redundancy_threshold_selection"),
            true, FRAME_WIDTH, FRAME_HEIGHT, false, true);
    frame.addInternalFrameListener(new InternalFrameAdapter()
    {
      @Override
      public void internalFrameClosing(InternalFrameEvent evt)
      {
        ap.getIdPanel().getIdCanvas().setHighlighted(null);
      }
    });

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
    JProgressBar progress = new JProgressBar();
    progress.setIndeterminate(true);
    southPanel.add(progress, java.awt.BorderLayout.SOUTH);

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
    if (ap.av.hasHiddenColumns())
    {
      omitHidden = ap.av.getViewAsString(sg != null);
    }
    redundancy = AlignSeq.computeRedundancyMatrix(originalSequences,
            omitHidden, start, end, false);

    progress.setIndeterminate(false);
    progress.setVisible(false);
    progress = null;

    label.setText(
            MessageManager.getString("label.enter_redundancy_threshold"));
    slider.setVisible(true);
    applyButton.setEnabled(true);
    valueField.setVisible(true);

    validate();
    sliderValueChanged();
    // System.out.println((System.currentTimeMillis()-start));
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
    ap.getIdPanel().getIdCanvas().setHighlighted(redundantSequences);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void applyButton_actionPerformed(ActionEvent e)
  {
    List<SequenceI> del = new ArrayList<>();

    undoButton.setEnabled(true);

    float value = slider.getValue();
    SequenceGroup sg = ap.av.getSelectionGroup();

    for (int i = 0; i < redundancy.length; i++)
    {
      if (value <= redundancy[i])
      {
        del.add(originalSequences[i]);
      }
    }

    // This has to be done before the restoreHistoryItem method of alignFrame
    // will actually restore these sequences.
    if (del.size() > 0)
    {
      SequenceI[] deleted = new SequenceI[del.size()];

      int width = 0;
      for (int i = 0; i < del.size(); i++)
      {
        deleted[i] = del.get(i);
        if (deleted[i].getLength() > width)
        {
          width = deleted[i].getLength();
        }
      }

      EditCommand cut = new EditCommand(
              MessageManager.getString("action.remove_redundancy"),
              Action.CUT, deleted, 0, width, ap.av.getAlignment());

      for (int i = 0; i < del.size(); i++)
      {
        ap.av.getAlignment().deleteSequence(deleted[i]);
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

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void undoButton_actionPerformed(ActionEvent e)
  {
    if (historyList == null || historyList.isEmpty())
    {
      undoButton.setEnabled(false);
      return;
    }

    CommandI command = historyList.pop();
    if (ap.av.getHistoryList().contains(command))
    {
      command.undoCommand(af.getViewAlignments());
      ap.av.getHistoryList().remove(command);
      ap.av.firePropertyChange("alignment", null,
              ap.av.getAlignment().getSequences());
      af.updateEditMenuBar();
    }

    ap.paintAlignment(true, true);

    if (historyList.size() == 0)
    {
      undoButton.setEnabled(false);
    }
  }

}
