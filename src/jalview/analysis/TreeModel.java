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

import jalview.datamodel.AlignmentView;
import jalview.datamodel.BinaryNode;
import jalview.datamodel.NodeTransformI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.datamodel.SequenceNode;
import jalview.io.NewickFile;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * A model of a tree, either computed by Jalview or loaded from a file or other
 * resource or service
 */
public class TreeModel
{

  SequenceI[] sequences;

  /* 
   * SequenceData is a string representation of what the user
   * sees. The display may contain hidden columns.
   */
  private AlignmentView seqData;

  int noseqs;

  BinaryNode top;

  double maxDistValue;

  double maxheight;

  int ycount;

  Vector<BinaryNode> node;

  boolean hasDistances = true; // normal case for jalview trees

  boolean hasBootstrap = false; // normal case for jalview trees

  private boolean hasRootDistance = true;

  /**
   * Create a new TreeModel object with leaves associated with sequences in
   * seqs, and (optionally) original alignment data represented by Cigar strings
   * 
   * @param seqs
   *          SequenceI[]
   * @param odata
   *          Cigar[]
   * @param treefile
   *          NewickFile
   */
  public TreeModel(SequenceI[] seqs, AlignmentView odata,
          NewickFile treefile)
  {
    this(seqs, treefile.getTree(), treefile.HasDistances(),
            treefile.HasBootstrap(), treefile.HasRootDistance());
    seqData = odata;

    associateLeavesToSequences(seqs);
  }

  /**
   * Constructor given a calculated tree
   * 
   * @param tree
   */
  public TreeModel(TreeBuilder tree)
  {
    this(tree.getSequences(), tree.getTopNode(), tree.hasDistances(),
            tree.hasBootstrap(), tree.hasRootDistance());
    seqData = tree.getOriginalData();
  }

  /**
   * Constructor given sequences, root node and tree property flags
   * 
   * @param seqs
   * @param root
   * @param hasDist
   * @param hasBoot
   * @param hasRootDist
   */
  public TreeModel(SequenceI[] seqs, BinaryNode root, boolean hasDist,
          boolean hasBoot, boolean hasRootDist)
  {
    this.sequences = seqs;
    top = root;

    hasDistances = hasDist;
    hasBootstrap = hasBoot;
    hasRootDistance = hasRootDist;

    maxheight = findHeight(top);
  }

  /**
   * @param seqs
   */
  public void associateLeavesToSequences(SequenceI[] seqs)
  {
    SequenceIdMatcher algnIds = new SequenceIdMatcher(seqs);

    Vector<BinaryNode> leaves = findLeaves(top);

    int i = 0;
    int namesleft = seqs.length;

    SequenceNode j;
    SequenceI nam;
    String realnam;
    Vector<SequenceI> one2many = new Vector<SequenceI>();
    // int countOne2Many = 0;
    while (i < leaves.size())
    {
      // TODO - decide if we get rid of the polymorphism here ?
      j = (SequenceNode) leaves.elementAt(i++);
      realnam = j.getName();
      nam = null;

      if (namesleft > -1)
      {
        nam = algnIds.findIdMatch(realnam);
      }

      if (nam != null)
      {
        j.setElement(nam);
        if (one2many.contains(nam))
        {
          // countOne2Many++;
          // if (Cache.isDebugEnabled())
          // Cache.debug("One 2 many relationship for
          // "+nam.getName());
        }
        else
        {
          one2many.addElement(nam);
          namesleft--;
        }
      }
      else
      {
        j.setElement(new Sequence(realnam, "THISISAPLACEHLDER"));
        j.setPlaceholder(true);
      }
    }
    // if (Cache.isDebugEnabled() && countOne2Many>0) {
    // Cache.debug("There were "+countOne2Many+" alignment
    // sequence ids (out of "+one2many.size()+" unique ids) linked to two or
    // more leaves.");
    // }
    // one2many.clear();
  }

  /**
   * Generate a string representation of the Tree
   * 
   * @return Newick File with all tree data available
   */
  public String print()
  {
    NewickFile fout = new NewickFile(getTopNode());

    return fout.print(hasBootstrap(), hasDistances(), hasRootDistance()); // output
                                                                          // all
                                                                          // data
                                                                          // available
                                                                          // for
                                                                          // tree
  }

  /**
   * 
   * used when the alignment associated to a tree has changed.
   * 
   * @param list
   *          Sequence set to be associated with tree nodes
   */
  public void updatePlaceHolders(List<SequenceI> list)
  {
    Vector<BinaryNode> leaves = findLeaves(top);

    int sz = leaves.size();
    SequenceIdMatcher seqmatcher = null;
    int i = 0;

    while (i < sz)
    {
      SequenceNode leaf = (SequenceNode) leaves.elementAt(i++);

      if (list.contains(leaf.element()))
      {
        leaf.setPlaceholder(false);
      }
      else
      {
        if (seqmatcher == null)
        {
          // Only create this the first time we need it
          SequenceI[] seqs = new SequenceI[list.size()];

          for (int j = 0; j < seqs.length; j++)
          {
            seqs[j] = list.get(j);
          }

          seqmatcher = new SequenceIdMatcher(seqs);
        }

        SequenceI nam = seqmatcher.findIdMatch(leaf.getName());

        if (nam != null)
        {
          if (!leaf.isPlaceholder())
          {
            // remapping the node to a new sequenceI - should remove any refs to
            // old one.
            // TODO - make many sequenceI to one leaf mappings possible!
            // (JBPNote)
          }
          leaf.setPlaceholder(false);
          leaf.setElement(nam);
        }
        else
        {
          if (!leaf.isPlaceholder())
          {
            // Construct a new placeholder sequence object for this leaf
            leaf.setElement(
                    new Sequence(leaf.getName(), "THISISAPLACEHLDER"));
          }
          leaf.setPlaceholder(true);

        }
      }
    }
  }

  /**
   * rename any nodes according to their associated sequence. This will modify
   * the tree's metadata! (ie the original NewickFile or newly generated
   * BinaryTree's label data)
   */
  public void renameAssociatedNodes()
  {
    applyToNodes(new NodeTransformI()
    {

      @Override
      public void transform(BinaryNode nd)
      {
        Object el = nd.element();
        if (el != null && el instanceof SequenceI)
        {
          nd.setName(((SequenceI) el).getName());
        }
      }
    });
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

  /**
   * printNode is mainly for debugging purposes.
   * 
   * @param nd
   *          SequenceNode
   */
  void printNode(BinaryNode nd)
  {
    if (nd == null)
    {
      return;
    }

    if ((nd.left() == null) && (nd.right() == null))
    {
      System.out.println("Leaf = " + ((SequenceI) nd.element()).getName());
      System.out.println("Dist " + nd.dist);
      System.out.println("Boot " + nd.getBootstrap());
    }
    else
    {
      System.out.println("Dist " + nd.dist);
      printNode((BinaryNode) nd.left());
      printNode((BinaryNode) nd.right());
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public double getMaxHeight()
  {
    return maxheight;
  }

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
   * DOCUMENT ME!
   * 
   * @param nd
   *          DOCUMENT ME!
   */
  void printN(BinaryNode nd)
  {
    if (nd == null)
    {
      return;
    }

    if ((nd.left() != null) && (nd.right() != null))
    {
      printN((BinaryNode) nd.left());
      printN((BinaryNode) nd.right());
    }
    else
    {
      System.out.println(" name = " + ((SequenceI) nd.element()).getName());
    }

    System.out.println(
            " dist = " + nd.dist + " " + nd.count + " " + nd.height);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param nd
   *          DOCUMENT ME!
   */
  public void reCount(BinaryNode nd)
  {
    ycount = 0;
    // _lycount = 0;
    // _lylimit = this.node.size();
    _reCount(nd);
  }

  // private long _lycount = 0, _lylimit = 0;

  /**
   * DOCUMENT ME!
   * 
   * @param nd
   *          DOCUMENT ME!
   */
  void _reCount(BinaryNode nd)
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

      _reCount((BinaryNode) nd.left());
      _reCount((BinaryNode) nd.right());

      BinaryNode l = (BinaryNode) nd.left();
      BinaryNode r = (BinaryNode) nd.right();

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
   */
  public void swapNodes(BinaryNode nd)
  {
    if (nd == null)
    {
      return;
    }

    BinaryNode tmp = (BinaryNode) nd.left();

    nd.setLeft(nd.right());
    nd.setRight(tmp);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param nd
   *          DOCUMENT ME!
   * @param dir
   *          DOCUMENT ME!
   */
  void changeDirection(BinaryNode nd, BinaryNode dir)
  {
    if (nd == null)
    {
      return;
    }

    if (nd.parent() != top)
    {
      changeDirection((BinaryNode) nd.parent(), nd);

      BinaryNode tmp = (BinaryNode) nd.parent();

      if (dir == nd.left())
      {
        nd.setParent(dir);
        nd.setLeft(tmp);
      }
      else if (dir == nd.right())
      {
        nd.setParent(dir);
        nd.setRight(tmp);
      }
    }
    else
    {
      if (dir == nd.left())
      {
        nd.setParent(nd.left());

        if (top.left() == nd)
        {
          nd.setRight(top.right());
        }
        else
        {
          nd.setRight(top.left());
        }
      }
      else
      {
        nd.setParent(nd.right());

        if (top.left() == nd)
        {
          nd.setLeft(top.right());
        }
        else
        {
          nd.setLeft(top.left());
        }
      }
    }
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

  /**
   * 
   * @return true if tree has real distances
   */
  public boolean hasDistances()
  {
    return hasDistances;
  }

  /**
   * 
   * @return true if tree has real bootstrap values
   */
  public boolean hasBootstrap()
  {
    return hasBootstrap;
  }

  public boolean hasRootDistance()
  {
    return hasRootDistance;
  }

  /**
   * apply the given transform to all the nodes in the tree.
   * 
   * @param nodeTransformI
   */
  public void applyToNodes(NodeTransformI nodeTransformI)
  {
    for (Enumeration<BinaryNode> nodes = node.elements(); nodes
            .hasMoreElements(); nodeTransformI
                    .transform(nodes.nextElement()))
    {
      ;
    }
  }

  public AlignmentView getOriginalData()
  {
    return seqData;
  }
}
