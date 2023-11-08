package jalview.datamodel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;

import jalview.analysis.AverageDistanceEngine;
import jalview.bin.Console;

public class GroupSet implements GroupSetI
{
  List<BitSet> groups = Arrays.asList();

  public GroupSet(GroupSet grps)
  {
    abs = grps.abs;
    colorMap = new HashMap<BitSet, Color>(grps.colorMap);
    groups = new ArrayList<BitSet>(grps.groups);
    newick = grps.newick;
    thresh = grps.thresh;
    treeType = grps.treeType;
  }

  public GroupSet()
  {
    // TODO Auto-generated constructor stub
  }

  public GroupSet(boolean abs2, float thresh2, List<BitSet> groups2,
          String treeType2, String newick2)
  {
    abs = abs2;
    thresh = thresh2;
    groups = groups2;
    treeType = treeType2;
    newick = newick2;
  }

  @Override
  public boolean hasGroups()
  {
    return groups != null;
  }

  String newick = null;

  @Override
  public String getNewick()
  {
    return newick;
  }

  @Override
  public boolean hasTree()
  {
    return newick != null && newick.length() > 0;
  }

  boolean abs = false;

  double thresh = 0;

  String treeType = null;

  @Override
  public void updateGroups(List<BitSet> colGroups)
  {
    if (colGroups != null)
    {
      groups = colGroups;
    }
  }

  @Override
  public BitSet getGroupsFor(int column)
  {
    if (groups != null)
    {
      for (BitSet gp : groups)
      {
        if (gp.get(column))
        {
          return gp;
        }
      }
    }
    // return singleton set;
    BitSet bs = new BitSet();
    bs.set(column);
    return bs;
  }

  HashMap<BitSet, Color> colorMap = new HashMap<>();

  @Override
  public Color getColourForGroup(BitSet bs)
  {
    if (bs == null)
    {
      return Color.white;
    }
    Color groupCol = colorMap.get(bs);
    if (groupCol == null)
    {
      return Color.white;
    }
    return groupCol;
  }

  @Override
  public void setColorForGroup(BitSet bs, Color color)
  {
    colorMap.put(bs, color);
  }

  @Override
  public void restoreGroups(List<BitSet> newgroups, String treeMethod,
          String tree, double thresh2)
  {
    treeType = treeMethod;
    groups = newgroups;
    thresh = thresh2;
    newick = tree;

  }

  @Override
  public boolean hasCutHeight()
  {
    return groups != null && thresh != 0;
  }

  @Override
  public double getCutHeight()
  {
    return thresh;
  }

  @Override
  public String getTreeMethod()
  {
    return treeType;
  }

  public static GroupSet makeGroups(ContactMatrixI matrix, boolean autoCut)
  {
    return makeGroups(matrix, autoCut, 0, autoCut);
  }
  public static GroupSet makeGroups(ContactMatrixI matrix, boolean auto, float thresh,
          boolean abs)
  {
    AverageDistanceEngine clusterer = new AverageDistanceEngine(null, null,
            matrix, true);
    double height = clusterer.findHeight(clusterer.getTopNode());
    Console.debug("Column tree height: " + height);
    String newick = new jalview.io.NewickFile(clusterer.getTopNode(), false,
            true).print();
    String treeType = "UPGMA";
    Console.trace("Newick string\n" + newick);

    List<BinaryNode> nodegroups;
    float cut = -1f;
    if (auto)
    {
      double rootw = 0;
      int p = 2;
      BinaryNode bn = clusterer.getTopNode();
      while (p-- > 0 & bn.left() != null)
      {
        if (bn.left() != null)
        {
          bn = bn.left();
        }
        if (bn.left() != null)
        {
          rootw = bn.height;
        }
      }
      thresh = Math.max((float) (rootw / height) - 0.01f, 0);
      cut = thresh;
      nodegroups = clusterer.groupNodes(thresh);
    }
    else
    {
      if (abs ? (height > thresh) : (0 < thresh && thresh < 1))
      {
        cut = abs ? thresh : (float) (thresh * height);
        Console.debug("Threshold " + cut + " for height=" + height);
        nodegroups = clusterer.groupNodes(cut);
      }
      else
      {
        nodegroups = new ArrayList<BinaryNode>();
        nodegroups.add(clusterer.getTopNode());
      }
    }
    
    List<BitSet> groups = new ArrayList<>();
    for (BinaryNode root : nodegroups)
    {
      BitSet gpset = new BitSet();
      for (BinaryNode leaf : clusterer.findLeaves(root))
      {
        gpset.set((Integer) leaf.element());
      }
      groups.add(gpset);
    }
    GroupSet grps = new GroupSet(abs, (cut == -1f) ? thresh : cut, groups,
            treeType, newick);
    return grps;
  }

  @Override
  public List<BitSet> getGroups()
  {
    return groups;
  }
}
