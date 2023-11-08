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

import jalview.analysis.AlignSeq;
import jalview.api.AlignmentViewPanel;
import jalview.api.analysis.ScoreModelI;
import jalview.api.analysis.SimilarityParamsI;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.SequenceI;
import jalview.math.Matrix;
import jalview.math.MatrixI;
import jalview.util.Comparison;

/**
 * A class that computes pairwise similarity scores using the Smith-Waterman
 * alignment algorithm
 */
public class SmithWatermanModel extends SimilarityScoreModel
{
  private static final String NAME = "Smith Waterman Score";

  private String description;

  /**
   * Constructor
   */
  public SmithWatermanModel()
  {
  }

  @Override
  public MatrixI findSimilarities(AlignmentView seqData,
          SimilarityParamsI options)
  {
    SequenceI[] sequenceString = seqData
            .getVisibleAlignment(Comparison.GAP_SPACE).getSequencesArray();
    int noseqs = sequenceString.length;
    double[][] distances = new double[noseqs][noseqs];

    double max = -1;

    for (int i = 0; i < (noseqs - 1); i++)
    {
      for (int j = i; j < noseqs; j++)
      {
        AlignSeq as = new AlignSeq(sequenceString[i], sequenceString[j],
                seqData.isNa() ? "dna" : "pep");
        as.calcScoreMatrix();
        as.traceAlignment();
        as.printAlignment(System.out);
        distances[i][j] = as.maxscore;

        if (max < distances[i][j])
        {
          max = distances[i][j];
        }
      }
    }

    return new Matrix(distances);
  }

  @Override
  public String getName()
  {
    return NAME;
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
  public String getDescription()
  {
    return description;
  }

  @Override
  public ScoreModelI getInstance(AlignmentViewPanel avp)
  {
    return this;
  }
}
