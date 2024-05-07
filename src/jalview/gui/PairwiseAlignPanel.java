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

import jalview.analysis.AlignSeq;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.jbgui.GPairwiseAlignPanel;
import jalview.math.MiscMath;
import jalview.util.MessageManager;
import jalview.viewmodel.AlignmentViewport;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import javax.swing.event.SwingPropertyChangeSupport;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class PairwiseAlignPanel extends GPairwiseAlignPanel
{

  private static final String DASHES = "---------------------\n";
  
  private float[][] scores;

  private float[][] alignmentScores;	// scores used by PaSiMap

  AlignmentViewport av;

  Vector<SequenceI> sequences;

  private boolean suppressTextbox;

  private boolean discardAlignments;
 
  private boolean endGaps;
 
  // for listening
  public static final String TOTAL = "total";
  
  public static final String PROGRESS = "progress";

  //private SwingPropertyChangeSupport pcSupport = new SwingPropertyChangeSupport(this);

  private final int total;
  
  private int progress;

  /**
   * Creates a new PairwiseAlignPanel object.
   * 
   * @param viewport
   *          DOCUMENT ME!
   * @param endGaps ~ toggle gaps and the beginning and end of sequences
   */
  public PairwiseAlignPanel(AlignmentViewport viewport)
  {
    this(viewport, false);
  }
  public PairwiseAlignPanel(AlignmentViewport viewport, boolean endGaps)
  {
    this(viewport, endGaps, true);
  }
  public PairwiseAlignPanel(AlignmentViewport viewport, boolean endGaps, boolean run)
  {
    super();
    this.av = viewport;

    sequences = new Vector<SequenceI>();
    
    this.endGaps = endGaps;

    //&!
    total = MiscMath.combinations(av.getAlignment().getHeight(), 2);
    
    if (run)
      calculate();
  }
  
  public void calculate()
  {
    SequenceGroup selectionGroup = av.getSelectionGroup();
    boolean isSelection = selectionGroup != null
            && selectionGroup.getSize() > 0;
    AlignmentView view = av.getAlignmentView(isSelection);
    // String[] seqStrings = viewport.getViewAsString(true);
    String[] seqStrings = view
            .getSequenceStrings(av.getGapCharacter());

    SequenceI[] seqs;
    if (isSelection)
    {
      seqs = (SequenceI[]) view
              .getAlignmentAndHiddenColumns(av.getGapCharacter())[0];
    }
    else
    {
      seqs = av.getAlignment().getSequencesArray();
    }
    
    progress = 0;
    firePropertyChange(TOTAL, 0, total);

    String type = (av.getAlignment().isNucleotide()) ? AlignSeq.DNA
            : AlignSeq.PEP;

    float[][] scores = new float[seqs.length][seqs.length];
    float[][] alignmentScores = new float[seqs.length][seqs.length];
    double totscore = 0D;
    int count = seqs.length;
    suppressTextbox = count<10;
    discardAlignments = count<15;
    boolean first = true;
    //AlignSeq as = new AlignSeq(seqs[1], seqStrings[1], seqs[0], seqStrings[0], type);

    for (int i = 1; i < count; i++)
    {
      // fill diagonal alignmentScores with Float.NaN
      alignmentScores[i - 1][i - 1] = Float.NaN;
      for (int j = 0; j < i; j++)
      {
        System.out.println(String.format("i: %d ; j: %d", i, j));
        AlignSeq as = new AlignSeq(seqs[i], seqStrings[i], seqs[j],
                seqStrings[j], type);
        //as.seqInit(seqs[i], seqStrings[i], seqs[j], seqStrings[j], type);

        if (as.s1str.length() == 0 || as.s2str.length() == 0)
        {
          continue;
        }

        as.calcScoreMatrix();
        if (endGaps)
        {
          as.traceAlignmentWithEndGaps();
        } else {
          as.traceAlignment();
        }
        as.scoreAlignment();

        if (!first)
        {
          System.out.println(DASHES);
          textarea.append(DASHES);
        }
        first = false;
        if (discardAlignments) {
          as.printAlignment(System.out);
        }
        scores[i][j] = as.getMaxScore() / as.getASeq1().length;
        alignmentScores[i][j] = as.getAlignmentScore();
        totscore = totscore + scores[i][j];

        if (suppressTextbox)
        {
          textarea.append(as.getOutput());
        }
        if (discardAlignments)
        {
          sequences.add(as.getAlignedSeq1());
          sequences.add(as.getAlignedSeq2());
        }
        firePropertyChange(PROGRESS, progress, ++progress);

      }
    }
    alignmentScores[count - 1][count - 1] = Float.NaN;

    this.scores = scores;
    this.alignmentScores = alignmentScores;

    if (count > 2)
    {
      printScoreMatrix(seqs, scores, totscore);
    }
  }

  public float[][] getScores()
  {
    return this.scores;
  }

  public float[][] getAlignmentScores()
  {
    return this.alignmentScores;
  }

  /**
   * Prints a matrix of seqi-seqj pairwise alignment scores to sysout
   * 
   * @param seqs
   * @param scores
   * @param totscore
   */
  protected void printScoreMatrix(SequenceI[] seqs, float[][] scores,
          double totscore)
  {
    System.out
            .println("Pairwise alignment scaled similarity score matrix\n");

    for (int i = 0; i < seqs.length; i++)
    {
      System.out.println(
              String.format("%3d %s", i + 1, seqs[i].getDisplayId(true)));
    }

    /*
     * table heading columns for sequences 1, 2, 3...
     */
    System.out.print("\n ");
    for (int i = 0; i < seqs.length; i++)
    {
      System.out.print(String.format("%7d", i + 1));
    }
    System.out.println();

    for (int i = 0; i < seqs.length; i++)
    {
      System.out.print(String.format("%3d", i + 1));
      for (int j = 0; j < i; j++)
      {
        /*
         * as a fraction of tot score, outputs are 0 <= score <= 1
         */
        System.out.print(String.format("%7.3f", scores[i][j] / totscore));
      }
      System.out.println();
    }

    System.out.println("\n");
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  protected void viewInEditorButton_actionPerformed(ActionEvent e)
  {
    SequenceI[] seq = new SequenceI[sequences.size()];

    for (int i = 0; i < sequences.size(); i++)
    {
      seq[i] = sequences.elementAt(i);
    }

    AlignFrame af = new AlignFrame(new Alignment(seq),
            AlignFrame.DEFAULT_WIDTH, AlignFrame.DEFAULT_HEIGHT);

    Desktop.addInternalFrame(af,
            MessageManager.getString("label.pairwise_aligned_sequences"),
            AlignFrame.DEFAULT_WIDTH, AlignFrame.DEFAULT_HEIGHT);
  }
  
  //&!
  /*
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    System.out.println(pcSupport != null);
    pcSupport.addPropertyChangeListener(listener);
  }
  
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    pcSupport.removePropertyChangeListener(listener);
  }
  */
  
  public long getTotal()
  {
    return total;
  }
  
  public long getProgress()
  {
    return progress;
  }
}
