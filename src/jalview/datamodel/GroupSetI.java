package jalview.datamodel;

import java.awt.Color;
import java.util.BitSet;
import java.util.List;

public interface GroupSetI
{
  boolean hasGroups();

  String getNewick();

  boolean hasTree();

  void updateGroups(List<BitSet> colGroups);

  BitSet getGroupsFor(int column);

  Color getColourForGroup(BitSet bs);

  void setColorForGroup(BitSet bs, Color color);

  void restoreGroups(List<BitSet> newgroups, String treeMethod, String tree,
          double thresh2);

  boolean hasCutHeight();

  double getCutHeight();

  String getTreeMethod();

  List<BitSet> getGroups();

}
