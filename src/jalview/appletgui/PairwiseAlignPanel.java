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
import jalview.datamodel.Alignment;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

public class PairwiseAlignPanel extends Panel implements ActionListener
{
  Vector sequences = new Vector();

  AlignmentPanel ap;

  public PairwiseAlignPanel(AlignmentPanel ap)
  {
    try
    {
      jbInit();
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    this.ap = ap;
    sequences = new Vector();

    SequenceI[] seqs;
    String[] seqStrings = ap.av.getViewAsString(true);

    if (ap.av.getSelectionGroup() == null)
    {
      seqs = ap.av.getAlignment().getSequencesArray();
    }
    else
    {
      seqs = ap.av.getSelectionGroup()
              .getSequencesInOrder(ap.av.getAlignment());
    }

    float scores[][] = new float[seqs.length][seqs.length];
    double totscore = 0;
    int count = ap.av.getSelectionGroup().getSize();
    String type = (ap.av.getAlignment().isNucleotide()) ? AlignSeq.DNA
            : AlignSeq.PEP;
    Sequence seq;

    for (int i = 1; i < count; i++)
    {
      for (int j = 0; j < i; j++)
      {

        AlignSeq as = new AlignSeq(seqs[i], seqStrings[i], seqs[j],
                seqStrings[j], type);

        if (as.s1str.length() == 0 || as.s2str.length() == 0)
        {
          continue;
        }

        as.calcScoreMatrix();
        as.traceAlignment();

        as.printAlignment(System.out);
        scores[i][j] = (float) as.getMaxScore()
                / (float) as.getASeq1().length;
        totscore = totscore + scores[i][j];

        textarea.append(as.getOutput());
        sequences.add(as.getAlignedSeq1());
        sequences.add(as.getAlignedSeq1());
      }
    }

    if (count > 2)
    {
      System.out.println(
              "Pairwise alignment scaled similarity score matrix\n");

      for (int i = 0; i < count; i++)
      {
        jalview.util.Format.print(System.out, "%s \n",
                ("" + i) + " " + seqs[i].getName());
      }

      System.out.println("\n");

      for (int i = 0; i < count; i++)
      {
        for (int j = 0; j < i; j++)
        {
          jalview.util.Format.print(System.out, "%7.3f",
                  scores[i][j] / totscore);
        }
      }

      System.out.println("\n");
    }
  }

  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == viewInEditorButton)
    {
      viewInEditorButton_actionPerformed();
    }
  }

  protected void viewInEditorButton_actionPerformed()
  {

    Sequence[] seq = new Sequence[sequences.size()];

    for (int i = 0; i < sequences.size(); i++)
    {
      seq[i] = (Sequence) sequences.elementAt(i);
    }

    new AlignFrame(new Alignment(seq), ap.av.applet,
            "Pairwise Aligned Sequences", false);

  }

  protected ScrollPane scrollPane = new ScrollPane();

  protected TextArea textarea = new TextArea();

  protected Button viewInEditorButton = new Button();

  Panel jPanel1 = new Panel();

  BorderLayout borderLayout1 = new BorderLayout();

  private void jbInit() throws Exception
  {
    this.setLayout(borderLayout1);
    textarea.setFont(new java.awt.Font("Monospaced", 0, 12));
    textarea.setText("");
    viewInEditorButton.setFont(new java.awt.Font("Verdana", 0, 12));
    viewInEditorButton.setLabel(
            MessageManager.getString("label.view_alignment_editor"));
    viewInEditorButton.addActionListener(this);
    this.add(scrollPane, BorderLayout.CENTER);
    scrollPane.add(textarea);
    this.add(jPanel1, BorderLayout.SOUTH);
    jPanel1.add(viewInEditorButton, null);
  }

}
