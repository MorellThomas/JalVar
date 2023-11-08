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
 * This class implements distance calculations used in constructing a Neighbour
 * Joining tree
 */
public class NJTree extends TreeBuilder
{
  /**
   * Constructor given a viewport, tree type and score model
   * 
   * @param av
   *          the current alignment viewport
   * @param sm
   *          a distance or similarity score model to use to compute the tree
   * @param scoreParameters
   */
  public NJTree(AlignmentViewport av, ScoreModelI sm,
          SimilarityParamsI scoreParameters)
  {
    super(av, sm, scoreParameters);
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
          double tmp = distances.getValue(i, j)
                  - (findr(i, j) + findr(j, i));

          if (tmp < min)
          {
            mini = i;
            minj = j;

            min = tmp;
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
    nodei.dist = ((dist + ri) - rj) / 2;
    nodej.dist = (dist - nodei.dist);

    if (nodei.dist < 0)
    {
      nodei.dist = 0;
    }

    if (nodej.dist < 0)
    {
      nodej.dist = 0;
    }
  }

  /**
   * Calculates and saves the distance between the combination of cluster(i) and
   * cluster(j) and all other clusters. The new distance to cluster k is
   * calculated as the average of the distances from i to k and from j to k,
   * less half the distance from i to j.
   * 
   * @param i
   * @param j
   */
  @Override
  protected void findClusterDistance(int i, int j)
  {
    // New distances from cluster i to others
    double[] newdist = new double[noseqs];

    double ijDistance = distances.getValue(i, j);
    for (int l = 0; l < noseqs; l++)
    {
      if ((l != i) && (l != j))
      {
        newdist[l] = (distances.getValue(i, l) + distances.getValue(j, l)
                - ijDistance) / 2;
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
}
