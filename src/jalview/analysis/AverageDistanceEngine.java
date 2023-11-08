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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Vector;

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.BinaryNode;
import jalview.datamodel.ContactListI;
import jalview.datamodel.ContactMatrixI;
import jalview.math.Matrix;
import jalview.viewmodel.AlignmentViewport;

/**
 * This class implements distance calculations used in constructing a Average
 * Distance tree (also known as UPGMA)
 */
public class AverageDistanceEngine extends TreeEngine
{
  ContactMatrixI cm;

  AlignmentViewport av;

  AlignmentAnnotation aa;

  // 0 - normalised dot product
  // 1 - L1 - ie (abs(v_1-v_2)/dim(v))
  // L1 is more rational - since can reason about value of difference,
  // normalised dot product might give cleaner clusters, but more difficult to
  // understand.

  int mode = 1;

  /**
   * compute cosine distance matrix for a given contact matrix and create a
   * UPGMA tree
   * @param cm
   * @param cosineOrDifference false - dot product : true - L1
   */
  public AverageDistanceEngine(AlignmentViewport av, AlignmentAnnotation aa,
          ContactMatrixI cm, boolean cosineOrDifference)
  {
    this.av = av;
    this.aa = aa;
    this.cm = cm;
    mode = (cosineOrDifference) ? 1 :0; 
    calculate(cm);

  }


  public void calculate(ContactMatrixI cm)
  {
    this.cm = cm;
    node = new Vector<BinaryNode>();
    clusters = new Vector<BitSet>();
    distances = new Matrix(new double[cm.getWidth()][cm.getWidth()]);
    noseqs = cm.getWidth();
    done = new BitSet();
    double moduli[] = new double[cm.getWidth()];
    double max;
    if (mode == 0)
    {
      max = 1;
    }
    else
    {
      max = cm.getMax() * cm.getMax();
    }

    for (int i = 0; i < cm.getWidth(); i++)
    {
      // init the tree engine node for this column
      BinaryNode cnode = new BinaryNode();
      cnode.setElement(Integer.valueOf(i));
      cnode.setName("c" + i);
      node.addElement(cnode);
      BitSet bs = new BitSet();
      bs.set(i);
      clusters.addElement(bs);

      // compute distance matrix element
      ContactListI ith = cm.getContactList(i);
      distances.setValue(i, i, 0);
      if (ith==null)
      {
        continue;
      }
      for (int j = 0; j < i; j++)
      {
        ContactListI jth = cm.getContactList(j);
        if (jth == null)
        {
          break;
        }
        double prd = 0;
        for (int indx = 0; indx < cm.getHeight(); indx++)
        {
          if (mode == 0)
          {
            if (j == 0)
            {
              moduli[i] += ith.getContactAt(indx) * ith.getContactAt(indx);
            }
            prd += ith.getContactAt(indx) * jth.getContactAt(indx);
          }
          else
          {
            prd += Math
                    .abs(ith.getContactAt(indx) - jth.getContactAt(indx));
          }
        }
        if (mode == 0)
        {
          if (j == 0)
          {
            moduli[i] = Math.sqrt(moduli[i]);
          }
          prd = (moduli[i] != 0 && moduli[j] != 0)
                  ? prd / (moduli[i] * moduli[j])
                  : 0;
          prd = 1 - prd;
        }
        else
        {
          prd /= cm.getHeight();
        }
        distances.setValue(i, j, prd);
        distances.setValue(j, i, prd);
      }
    }

    noClus = clusters.size();
    cluster();
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

  /***
   * not the right place - OH WELL!
   */

  /**
   * Makes a list of groups, where each group is represented by a node whose
   * height (distance from the root node), as a fraction of the height of the
   * whole tree, is greater than the given threshold. This corresponds to
   * selecting the nodes immediately to the right of a vertical line
   * partitioning the tree (if the tree is drawn with root to the left). Each
   * such node represents a group that contains all of the sequences linked to
   * the child leaf nodes.
   * 
   * @param threshold
   * @see #getGroups()
   */
  public List<BinaryNode> groupNodes(float threshold)
  {
    List<BinaryNode> groups = new ArrayList<BinaryNode>();
    _groupNodes(groups, getTopNode(), threshold);
    return groups;
  }

  protected void _groupNodes(List<BinaryNode> groups, BinaryNode nd,
          float threshold)
  {
    if (nd == null)
    {
      return;
    }

    if ((nd.height / maxheight) > threshold)
    {
      groups.add(nd);
    }
    else
    {
      _groupNodes(groups, nd.left(), threshold);
      _groupNodes(groups, nd.right(), threshold);
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param nd
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public double findHeight(BinaryNode nd)
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
   * Search for leaf nodes below (or at) the given node
   * 
   * @param top2
   *          root node to search from
   * 
   * @return
   */
  public Vector<BinaryNode> findLeaves(BinaryNode top2)
  {
    Vector<BinaryNode> leaves = new Vector<BinaryNode>();
    findLeaves(top2, leaves);
    return leaves;
  }

  /**
   * Search for leaf nodes.
   * 
   * @param nd
   *          root node to search from
   * @param leaves
   *          Vector of leaves to add leaf node objects too.
   * 
   * @return Vector of leaf nodes on binary tree
   */
  Vector<BinaryNode> findLeaves(BinaryNode nd, Vector<BinaryNode> leaves)
  {
    if (nd == null)
    {
      return leaves;
    }

    if ((nd.left() == null) && (nd.right() == null)) // Interior node
    // detection
    {
      leaves.addElement(nd);

      return leaves;
    }
    else
    {
      /*
       * TODO: Identify internal nodes... if (node.isSequenceLabel()) {
       * leaves.addElement(node); }
       */
      findLeaves(nd.left(), leaves);
      findLeaves(nd.right(), leaves);
    }

    return leaves;
  }

}
