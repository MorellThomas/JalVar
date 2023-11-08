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

import java.util.BitSet;
import java.util.Vector;

import jalview.datamodel.BinaryNode;
import jalview.datamodel.SequenceNode;
import jalview.math.MatrixI;

public abstract class TreeEngine
{

  protected Vector<BitSet> clusters;

  protected BitSet done;

  protected int noseqs;

  protected int noClus;

  protected MatrixI distances;

  protected double ri;

  protected double rj;

  protected Vector<BinaryNode> node;

  BinaryNode maxdist;

  double maxDistValue;

  protected int mini;

  protected int minj;

  protected BinaryNode top;

  protected int ycount;

  double maxheight;

  /**
   * Calculates and returns r, whatever that is
   * 
   * @param i
   * @param j
   * 
   * @return
   */
  protected double findr(int i, int j)
  {
    double tmp = 1;

    for (int k = 0; k < noseqs; k++)
    {
      if ((k != i) && (k != j) && (!done.get(k)))
      {
        tmp = tmp + distances.getValue(i, k);
      }
    }

    if (noClus > 2)
    {
      tmp = tmp / (noClus - 2);
    }

    return tmp;
  }

  /**
   * Merges cluster(j) to cluster(i) and recalculates cluster and node distances
   * 
   * @param i
   * @param j
   */
  protected void joinClusters(final int i, final int j)
  {
    double dist = distances.getValue(i, j);

    ri = findr(i, j);
    rj = findr(j, i);

    findClusterDistance(i, j);

    BinaryNode sn = new BinaryNode();

    sn.setLeft((node.elementAt(i)));
    sn.setRight((node.elementAt(j)));

    BinaryNode tmpi = (node.elementAt(i));
    BinaryNode tmpj = (node.elementAt(j));

    findNewDistances(tmpi, tmpj, dist);

    tmpi.setParent(sn);
    tmpj.setParent(sn);

    node.setElementAt(sn, i);

    /*
     * move the members of cluster(j) to cluster(i)
     * and mark cluster j as out of the game
     */
    clusters.get(i).or(clusters.get(j));
    clusters.get(j).clear();
    done.set(j);
  }

  protected abstract void findNewDistances(BinaryNode nodei,
          BinaryNode nodej, double previousDistance);

  /**
   * Calculates and saves the distance between the combination of cluster(i) and
   * cluster(j) and all other clusters. The form of the calculation depends on
   * the tree clustering method being used.
   * 
   * @param i
   * @param j
   */
  protected abstract void findClusterDistance(int i, int j);

  /**
   * Finds the node, at or below the given node, with the maximum distance, and
   * saves the node and the distance value
   * 
   * @param nd
   */
  protected void findMaxDist(BinaryNode nd)
  {
    if (nd == null)
    {
      return;
    }

    if ((nd.left() == null) && (nd.right() == null))
    {
      double dist = nd.dist;

      if (dist > maxDistValue)
      {
        maxdist = nd;
        maxDistValue = dist;
      }
    }
    else
    {
      findMaxDist((BinaryNode) nd.left());
      findMaxDist((BinaryNode) nd.right());
    }
  }

  /**
   * Form clusters by grouping sub-clusters, starting from one sequence per
   * cluster, and finishing when only two clusters remain
   */
  protected void cluster()
  {
    while (noClus > 2)
    {
      findMinDistance();

      joinClusters(mini, minj);

      noClus--;
    }

    int rightChild = done.nextClearBit(0);
    int leftChild = done.nextClearBit(rightChild + 1);

    joinClusters(leftChild, rightChild);
    top = (node.elementAt(leftChild));

    reCount(top);
    findHeight(top);
    findMaxDist(top);
  }

  /**
   * Returns the minimum distance between two clusters, and also sets the
   * indices of the clusters in fields mini and minj
   * 
   * @return
   */
  protected abstract double findMinDistance();

  /**
   * DOCUMENT ME!
   * 
   * @param nd
   *          DOCUMENT ME!
   */
  protected void _reCount(BinaryNode nd)
  {
    // if (_lycount<_lylimit)
    // {
    // System.err.println("Warning: depth of _recount greater than number of
    // nodes.");
    // }
    if (nd == null)
    {
      return;
    }
    // _lycount++;

    if ((nd.left() != null) && (nd.right() != null))
    {

      _reCount(nd.left());
      _reCount((BinaryNode) nd.right());

      BinaryNode l = nd.left();
      BinaryNode r = nd.right();

      nd.count = l.count + r.count;
      nd.ycount = (l.ycount + r.ycount) / 2;
    }
    else
    {
      nd.count = 1;
      nd.ycount = ycount++;
    }
    // _lycount--;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param nd
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  double findHeight(BinaryNode nd)
  {
    if (nd == null)
    {
      return maxheight;
    }

    if ((nd.left() == null) && (nd.right() == null))
    {
      nd.height = ((BinaryNode) nd.parent()).height + nd.dist;

      if (nd.height > maxheight)
      {
        return nd.height;
      }
      else
      {
        return maxheight;
      }
    }
    else
    {
      if (nd.parent() != null)
      {
        nd.height = ((BinaryNode) nd.parent()).height + nd.dist;
      }
      else
      {
        maxheight = 0;
        nd.height = (float) 0.0;
      }

      maxheight = findHeight((BinaryNode) (nd.left()));
      maxheight = findHeight((BinaryNode) (nd.right()));
    }

    return maxheight;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param nd
   *          DOCUMENT ME!
   */
  void reCount(BinaryNode nd)
  {
    ycount = 0;
    // _lycount = 0;
    // _lylimit = this.node.size();
    _reCount(nd);
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public BinaryNode getTopNode()
  {
    return top;
  }

}
