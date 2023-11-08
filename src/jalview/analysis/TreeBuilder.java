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
package jalview.analysis;

import jalview.api.analysis.ScoreModelI;
import jalview.api.analysis.SimilarityParamsI;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.BinaryNode;
import jalview.datamodel.CigarArray;
import jalview.datamodel.SeqCigar;
import jalview.datamodel.SequenceI;
import jalview.datamodel.SequenceNode;
import jalview.viewmodel.AlignmentViewport;

import java.util.BitSet;
import java.util.Vector;

public abstract class TreeBuilder extends TreeEngine
{
  public static final String AVERAGE_DISTANCE = "AV";

  public static final String NEIGHBOUR_JOINING = "NJ";

  protected SequenceI[] sequences;

  public AlignmentView seqData;

  private AlignmentView seqStrings;

  /**
   * Constructor
   * 
   * @param av
   * @param sm
   * @param scoreParameters
   */
  public TreeBuilder(AlignmentViewport av, ScoreModelI sm,
          SimilarityParamsI scoreParameters)
  {
    int start, end;
    boolean selview = av.getSelectionGroup() != null
            && av.getSelectionGroup().getSize() > 1;
    seqStrings = av.getAlignmentView(selview);
    if (!selview)
    {
      start = 0;
      end = av.getAlignment().getWidth();
      this.sequences = av.getAlignment().getSequencesArray();
    }
    else
    {
      start = av.getSelectionGroup().getStartRes();
      end = av.getSelectionGroup().getEndRes() + 1;
      this.sequences = av.getSelectionGroup()
              .getSequencesInOrder(av.getAlignment());
    }

    init(seqStrings, start, end);

    computeTree(sm, scoreParameters);
  }

  public SequenceI[] getSequences()
  {
    return sequences;
  }

  /**
   * 
   * @return true if tree has real distances
   */
  public boolean hasDistances()
  {
    return true;
  }

  /**
   * 
   * @return true if tree has real bootstrap values
   */
  public boolean hasBootstrap()
  {
    return false;
  }

  public boolean hasRootDistance()
  {
    return true;
  }

  /**
   * Calculates the tree using the given score model and parameters, and the
   * configured tree type
   * <p>
   * If the score model computes pairwise distance scores, then these are used
   * directly to derive the tree
   * <p>
   * If the score model computes similarity scores, then the range of the scores
   * is reversed to give a distance measure, and this is used to derive the tree
   * 
   * @param sm
   * @param scoreOptions
   */
  protected void computeTree(ScoreModelI sm, SimilarityParamsI scoreOptions)
  {
    distances = sm.findDistances(seqData, scoreOptions);

    makeLeaves();

    noClus = clusters.size();

    cluster();
  }

  protected void init(AlignmentView seqView, int start, int end)
  {
    this.node = new Vector<BinaryNode>();
    if (seqView != null)
    {
      this.seqData = seqView;
    }
    else
    {
      SeqCigar[] seqs = new SeqCigar[sequences.length];
      for (int i = 0; i < sequences.length; i++)
      {
        seqs[i] = new SeqCigar(sequences[i], start, end);
      }
      CigarArray sdata = new CigarArray(seqs);
      sdata.addOperation(CigarArray.M, end - start + 1);
      this.seqData = new AlignmentView(sdata, start);
    }

    /*
     * count the non-null sequences
     */
    noseqs = 0;

    done = new BitSet();

    for (SequenceI seq : sequences)
    {
      if (seq != null)
      {
        noseqs++;
      }
    }
  }

  /**
   * Start by making a cluster for each individual sequence
   */
  void makeLeaves()
  {
    clusters = new Vector<BitSet>();

    for (int i = 0; i < noseqs; i++)
    {
      SequenceNode sn = new SequenceNode();

      sn.setElement(sequences[i]);
      sn.setName(sequences[i].getName());
      node.addElement(sn);
      BitSet bs = new BitSet();
      bs.set(i);
      clusters.addElement(bs);
    }
  }

  public AlignmentView getOriginalData()
  {
    return seqStrings;
  }

}
