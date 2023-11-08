package jalview.datamodel;

import java.awt.Color;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import jalview.util.ColorUtils;
import jalview.ws.datamodel.MappableContactMatrixI;

public interface ContactMatrixI
{

  ContactListI getContactList(int column);

  float getMin();

  float getMax();

  String getAnnotDescr();

  String getAnnotLabel();

  /**
   * string indicating how the contactMatrix should be rendered - stored in
   * calcId
   * 
   * @return
   */
  String getType();

  int getWidth();
  int getHeight();
  public GroupSetI getGroupSet();

  /// proxy methods to simplify use of the interface
  /// Mappable contact matrices can override these to perform mapping

  default public boolean hasGroupSet()
  {
    return getGroupSet() != null;
  }

  default boolean hasGroups()
  {
    return hasGroupSet() && getGroupSet().hasGroups();
  }

  default BitSet getGroupsFor(int column)
  {
    if (!hasGroupSet())
    {
      BitSet colbitset = new BitSet();
      colbitset.set(column);
      return colbitset;
    }
    return getGroupSet().getGroupsFor(column);
  }

  default List<BitSet> getGroups()
  {
    if (!hasGroupSet())
    {
      return Arrays.asList();
    }
    return getGroupSet().getGroups();
  }

  default boolean hasTree()
  {
    return hasGroupSet() ? getGroupSet().hasTree() : false;
  }

  /**
   * Newick representation of clustered matrix
   * 
   * @return null unless hasTree is true
   */
  default String getNewick()
  {
    return hasGroupSet() ? getGroupSet().getNewick() : null;
  }

  default String getTreeMethod()
  {
    return hasGroupSet() ? getGroupSet().getTreeMethod() : null;
  }

  default boolean hasCutHeight()
  {
    return hasGroupSet() ? getGroupSet().hasCutHeight() : false;
  }

  default double getCutHeight()
  {
    return hasGroupSet() ? getGroupSet().getCutHeight() : 0;
  }

  default void updateGroups(List<BitSet> colGroups)
  {
    if (hasGroupSet())
    {
      getGroupSet().updateGroups(colGroups);
    }
  }

  default void setColorForGroup(BitSet bs, Color color)
  {
    if (hasGroupSet())
    {
      getGroupSet().setColorForGroup(bs, color);
    }
  }

  default Color getColourForGroup(BitSet bs)
  {
    if (hasGroupSet())
    {
      return getGroupSet().getColourForGroup(bs);
    }
    else
    {
      return Color.white;
    }
  }

  void setGroupSet(GroupSet makeGroups);

  default void randomlyReColourGroups() {
    if (hasGroupSet())
    {
      GroupSetI groups = getGroupSet();
      for (BitSet group:groups.getGroups())
      {
        groups.setColorForGroup(group, ColorUtils.getARandomColor());
      }
    }
  }

  default void transferGroupColorsTo(AlignmentAnnotation aa)
  {
    if (hasGroupSet())
    {
      GroupSetI groups = getGroupSet();
      // stash colors in linked annotation row.
      // doesn't work yet. TESTS!
      int sstart = aa.sequenceRef != null ? aa.sequenceRef.getStart() - 1
              : 0;
      Annotation ae;
      Color gpcol = null;
      int[] seqpos = null;
      for (BitSet gp : groups.getGroups())
      {
        gpcol = groups.getColourForGroup(gp);
        for (int p = gp.nextSetBit(0); p >= 0
                && p < Integer.MAX_VALUE; p = gp.nextSetBit(p + 1))
        {
          if (this instanceof MappableContactMatrixI)
          {
            MappableContactMatrixI mcm = (MappableContactMatrixI) this;
            seqpos = mcm.getMappedPositionsFor(aa.sequenceRef, p);
            if (seqpos == null)
            {
              // no mapping for this column.
              continue;
            }
            // TODO: handle ranges...
            ae = aa.getAnnotationForPosition(seqpos[0]);
          }
          else
          {
            ae = aa.getAnnotationForPosition(p + sstart);
          }
          if (ae != null)
          {
            ae.colour = gpcol.brighter().darker();
          }
        }
      }
    }
  }
  
  /**
   * look up the colour for a column in the associated contact matrix 
   * @return Color.white or assigned colour
   */
  default Color getGroupColorForPosition(int column)
  {
    if (hasGroupSet())
    {
      GroupSetI groups = getGroupSet();
      for (BitSet gp:groups.getGroups())
      {
        if (gp.get(column))
        {
          return groups.getColourForGroup(gp);
        }
      }
    }
    return Color.white;
  }
  
}
