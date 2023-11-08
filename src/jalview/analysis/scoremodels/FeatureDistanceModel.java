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
package jalview.analysis.scoremodels;

import jalview.api.AlignmentViewPanel;
import jalview.api.FeatureRenderer;
import jalview.api.analysis.ScoreModelI;
import jalview.api.analysis.SimilarityParamsI;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.SeqCigar;
import jalview.datamodel.SequenceFeature;
import jalview.math.Matrix;
import jalview.math.MatrixI;
import jalview.util.SetUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FeatureDistanceModel extends DistanceScoreModel
{
  private static final String NAME = "Sequence Feature Similarity";

  private String description;

  FeatureRenderer fr;

  /**
   * Constructor
   */
  public FeatureDistanceModel()
  {
  }

  @Override
  public ScoreModelI getInstance(AlignmentViewPanel view)
  {
    FeatureDistanceModel instance;
    try
    {
      instance = this.getClass().getDeclaredConstructor().newInstance();
      instance.configureFromAlignmentView(view);
      return instance;
    } catch (InstantiationException | IllegalAccessException e)
    {
      System.err.println("Error in " + getClass().getName()
              + ".getInstance(): " + e.getMessage());
      return null;
    } catch (ReflectiveOperationException roe)
    {
      return null;
    }
  }

  boolean configureFromAlignmentView(AlignmentViewPanel view)

  {
    fr = view.cloneFeatureRenderer();
    return true;
  }

  /**
   * Calculates a distance measure [i][j] between each pair of sequences as the
   * average number of features they have but do not share. That is, find the
   * features each sequence pair has at each column, ignore feature types they
   * have in common, and count the rest. The totals are normalised by the number
   * of columns processed.
   * <p>
   * The parameters argument provides settings for treatment of gap-residue
   * aligned positions, and whether the score is over the longer or shorter of
   * each pair of sequences
   * 
   * @param seqData
   * @param params
   */
  @Override
  public MatrixI findDistances(AlignmentView seqData,
          SimilarityParamsI params)
  {
    SeqCigar[] seqs = seqData.getSequences();
    int noseqs = seqs.length;
    int cpwidth = 0;// = seqData.getWidth();
    double[][] distances = new double[noseqs][noseqs];
    List<String> dft = null;
    if (fr != null)
    {
      dft = fr.getDisplayedFeatureTypes();
    }
    if (dft == null || dft.isEmpty())
    {
      return new Matrix(distances);
    }

    // need to get real position for view position
    int[] viscont = seqData.getVisibleContigs();

    /*
     * scan each column, compute and add to each distance[i, j]
     * the number of feature types that seqi and seqj do not share
     */
    for (int vc = 0; vc < viscont.length; vc += 2)
    {
      for (int cpos = viscont[vc]; cpos <= viscont[vc + 1]; cpos++)
      {
        cpwidth++;

        /*
         * first record feature types in this column for each sequence
         */
        Map<SeqCigar, Set<String>> sfap = findFeatureTypesAtColumn(seqs,
                cpos);

        /*
         * count feature types on either i'th or j'th sequence but not both
         * and add this 'distance' measure to the total for [i, j] for j > i
         */
        for (int i = 0; i < (noseqs - 1); i++)
        {
          for (int j = i + 1; j < noseqs; j++)
          {
            SeqCigar sc1 = seqs[i];
            SeqCigar sc2 = seqs[j];
            Set<String> set1 = sfap.get(sc1);
            Set<String> set2 = sfap.get(sc2);
            boolean gap1 = set1 == null;
            boolean gap2 = set2 == null;

            /*
             * gap-gap always scores zero
             * residue-residue is always scored
             * include gap-residue score if params say to do so
             */
            if ((!gap1 && !gap2) || params.includeGaps())
            {
              int seqDistance = SetUtils.countDisjunction(set1, set2);
              distances[i][j] += seqDistance;
            }
          }
        }
      }
    }

    /*
     * normalise the distance scores (summed over columns) by the
     * number of visible columns used in the calculation
     * and fill in the bottom half of the matrix
     */
    // TODO JAL-2424 cpwidth may be out by 1 - affects scores but not tree shape
    for (int i = 0; i < noseqs; i++)
    {
      for (int j = i + 1; j < noseqs; j++)
      {
        distances[i][j] /= cpwidth;
        distances[j][i] = distances[i][j];
      }
    }
    return new Matrix(distances);
  }

  /**
   * Builds and returns a map containing a (possibly empty) list (one per
   * SeqCigar) of visible feature types at the given column position. The map
   * does not include entries for features which straddle a gapped column
   * positions.
   * 
   * @param seqs
   * @param columnPosition
   *          (0..)
   * @return
   */
  protected Map<SeqCigar, Set<String>> findFeatureTypesAtColumn(
          SeqCigar[] seqs, int columnPosition)
  {
    Map<SeqCigar, Set<String>> sfap = new HashMap<>();
    for (SeqCigar seq : seqs)
    {
      int spos = seq.findPosition(columnPosition);
      if (spos != -1)
      {
        /*
         * position is not a gap
         */
        Set<String> types = new HashSet<>();
        List<SequenceFeature> sfs = fr
                .findFeaturesAtResidue(seq.getRefSeq(), spos, spos);
        for (SequenceFeature sf : sfs)
        {
          types.add(sf.getType());
        }
        sfap.put(seq, types);
      }
    }
    return sfap;
  }

  @Override
  public String getName()
  {
    return NAME;
  }

  @Override
  public String getDescription()
  {
    return description;
  }

  @Override
  public boolean isDNA()
  {
    return true;
  }

  @Override
  public boolean isProtein()
  {
    return true;
  }

  @Override
  public String toString()
  {
    return "Score between sequences based on hamming distance between binary vectors marking features displayed at each column";
  }
}
