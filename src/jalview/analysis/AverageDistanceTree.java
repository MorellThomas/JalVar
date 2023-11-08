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
import jalview.datamodel.BinaryNode;
import jalview.viewmodel.AlignmentViewport;

/**
 * This class implements distance calculations used in constructing a Average
 * Distance tree (also known as UPGMA)
 */
public class AverageDistanceTree extends TreeBuilder
{
  /**
   * Constructor
   * 
   * @param av
   * @param sm
   * @param scoreParameters
   */
  public AverageDistanceTree(AlignmentViewport av, ScoreModelI sm,
          SimilarityParamsI scoreParameters)
  {
    super(av, sm, scoreParameters);
  }

  /**
   * Calculates and saves the distance between the combination of cluster(i) and
   * cluster(j) and all other clusters. An average of the distances from
   * cluster(i) and cluster(j) is calculated, weighted by the sizes of each
   * cluster.
   * 
   * @param i
   * @param j
   */
  @Override
  protected void findClusterDistance(int i, int j)
  {
    int noi = clusters.elementAt(i).cardinality();
    int noj = clusters.elementAt(j).cardinality();

    // New distances from cluster i to others
    double[] newdist = new double[noseqs];

    for (int l = 0; l < noseqs; l++)
    {
      if ((l != i) && (l != j))
      {
        newdist[l] = ((distances.getValue(i, l) * noi)
                + (distances.getValue(j, l) * noj)) / (noi + noj);
      }
      else
      {
        newdist[l] = 0;
      }
    }

    for (int ii = 0; ii < noseqs; ii++)
    {
      distances.setValue(i, ii, newdist[ii]);
      distances.setValue(ii, i, newdist[ii]);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected double findMinDistance()
  {
    double min = Double.MAX_VALUE;

    for (int i = 0; i < (noseqs - 1); i++)
    {
      for (int j = i + 1; j < noseqs; j++)
      {
        if (!done.get(i) && !done.get(j))
        {
          if (distances.getValue(i, j) < min)
          {
            mini = i;
            minj = j;

            min = distances.getValue(i, j);
          }
        }
      }
    }
    return min;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void findNewDistances(BinaryNode nodei, BinaryNode nodej,
          double dist)
  {
    double ih = 0;
    double jh = 0;

    BinaryNode sni = nodei;
    BinaryNode snj = nodej;

    while (sni != null)
    {
      ih = ih + sni.dist;
      sni = (BinaryNode) sni.left();
    }

    while (snj != null)
    {
      jh = jh + snj.dist;
      snj = (BinaryNode) snj.left();
    }

    nodei.dist = ((dist / 2) - ih);
    nodej.dist = ((dist / 2) - jh);
  }

}
