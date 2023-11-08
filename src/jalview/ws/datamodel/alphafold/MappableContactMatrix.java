package jalview.ws.datamodel.alphafold;

import java.awt.Color;
import java.util.ArrayList;
import java.util.BitSet;

import jalview.datamodel.ContactListI;
import jalview.datamodel.ContactListImpl;
import jalview.datamodel.ContactListProviderI;
import jalview.datamodel.GroupSet;
import jalview.datamodel.GroupSetI;
import jalview.datamodel.Mapping;
import jalview.datamodel.SequenceI;
import jalview.util.MapList;
import jalview.ws.datamodel.MappableContactMatrixI;

public abstract class MappableContactMatrix<T extends MappableContactMatrix<T>>
        implements MappableContactMatrixI
{
  SequenceI refSeq = null;

  MapList toSeq = null;

  /**
   * the length that refSeq is expected to be (excluding gaps, of course)
   */
  int length;

  @Override
  public boolean hasReferenceSeq()
  {
    return (refSeq != null);
  }

  @Override
  public SequenceI getReferenceSeq()
  {
    return refSeq;
  }

  /**
   * container for groups - defined on matrix columns
   */
  GroupSet grps = new GroupSet();

  @Override
  public GroupSetI getGroupSet()
  {
    return grps;
  };

  @Override
  public void setGroupSet(GroupSet makeGroups)
  {
    grps = makeGroups;
  }

  @Override
  public MapList getMapFor(SequenceI mapSeq)
  {
    if (refSeq != null)
    {
      while (mapSeq != refSeq && mapSeq.getDatasetSequence() != null)
      {
        mapSeq = mapSeq.getDatasetSequence();
      }
      if (mapSeq != refSeq)
      {
        return null;
      }
    }
    else
    {
      if (mapSeq != null)
      {
        // our MapList does not concern this seq
        return null;
      }
    }

    return toSeq;
  }

  /**
   * set the reference sequence and construct the mapping between the start-end
   * positions of given sequence and row/columns of contact matrix
   * 
   * @param _refSeq
   */
  public void setRefSeq(SequenceI _refSeq)
  {
    refSeq = _refSeq;
    while (refSeq.getDatasetSequence() != null)
    {
      refSeq = refSeq.getDatasetSequence();
    }
    length = _refSeq.getEnd() - _refSeq.getStart() + 1;
    // if (length!=refSeq.getLength() || _refSeq.getStart()!=1)
    {
      toSeq = new MapList(
              new int[]
              { _refSeq.getStart(), _refSeq.getEnd() },
              new int[]
              { 0, length - 1 }, 1, 1);
    }
  }

  public T liftOver(SequenceI newRefSeq, Mapping sp2sq)
  {
    if (sp2sq.getMappedWidth() != sp2sq.getWidth())
    {
      // TODO: employ getWord/MappedWord to transfer annotation between cDNA and
      // Protein reference frames
      throw new Error(
              "liftOver currently not implemented for transfer of annotation between different types of seqeunce");
    }
    boolean mapIsTo = (sp2sq != null) ? (sp2sq.getTo() == refSeq) : false;

    /**
     * map from matrix to toSeq's coordinate frame
     */
    int[] refMap = toSeq.locateInFrom(0, length - 1);
    ArrayList<Integer> newFromMap = new ArrayList<Integer>();
    int last = -1;
    for (int i = 0; i < refMap.length; i += 2)
    {
      /*
       * for each contiguous range in toSeq, locate corresponding range in sequence mapped to toSeq by sp2sq
       */
      int[] sp2map = mapIsTo
              ? sp2sq.getMap().locateInFrom(refMap[i], refMap[i + 1])
              : sp2sq.getMap().locateInTo(refMap[i], refMap[i + 1]);
      if (sp2map == null)
      {
        continue;
      }

      for (int spm = 0; spm < sp2map.length; spm += 2)
      {

        if (last > -1)
        {
          if (sp2map[spm] != last + 1)
          {
            newFromMap.add(sp2map[spm]);
          }
          else
          {
            newFromMap.remove(newFromMap.size() - 1);
          }
        }
        else
        {
          newFromMap.add(sp2map[spm]);
        }
        last = sp2map[spm + 1];
        newFromMap.add(last);
      }
    }
    if ((newFromMap.size() % 2) != 0)
    {
      // should have had an even number of int ranges!
      throw new Error("PAEMatrix liftover failed.");
    }
    int fromIntMap[] = new int[newFromMap.size()];
    int ipos = 0;
    for (Integer i : newFromMap)
    {
      fromIntMap[ipos++] = i;
    }
    MapList newFromMapList = new MapList(fromIntMap,
            new int[]
            { 0, length - 1 }, 1, 1);

    T newCM = newMappableContactMatrix(newRefSeq, newFromMapList);
    return newCM;
  }

  protected abstract T newMappableContactMatrix(SequenceI newRefSeq,
          MapList newFromMapList);

  @Override
  public int[] getMappedPositionsFor(final SequenceI localFrame,
          final int column)
  {
    return getMappedPositionsFor(localFrame, column, column);
  }

  @Override
  public int[] getMappedPositionsFor(final SequenceI localFrame, int from,
          int to)
  {
    if (localFrame == null)
    {
      throw new Error("Unimplemented when no local sequence given.");
    }
    SequenceI lf = localFrame, uf = refSeq;

    // check that localFrame is derived from refSeq
    // just look for dataset sequences and check they are the same.
    // in future we could use DBRefMappings/whatever.
    while (lf.getDatasetSequence() != null
            || uf.getDatasetSequence() != null)
    {
      if (lf.getDatasetSequence() != null)
      {
        lf = lf.getDatasetSequence();
      }
      if (uf.getDatasetSequence() != null)
      {
        uf = uf.getDatasetSequence();
      }
    }
    if (lf != uf)
    {
      // could try harder to find a mapping
      throw new Error("This Matrix associated with '" + refSeq.getName()
              + "' is not mappable for the given localFrame sequence. ("
              + localFrame.getName() + ")");
    }
    
    // now look up from-to matrix columns in toSeq frame
    
    if (toSeq == null)
    {
      // no mapping - so we assume 1:1
      return new int[] { from, to };
    }
    // from-to are matrix columns
    // first locate on reference sequence

    int[] mappedPositions = toSeq.locateInFrom(from, to);
    if (mappedPositions==null)
    {
      return null;
    }
    
    // and now map to localFrame
    // from-to columns on the associated sequence should be
    // i. restricted to positions in localFrame
    // ii. 

//    int s = -1, e = -1;
//    for (int p = 0; p < mappedPositions.length; p++)
//    {
//      if (s == -1 && mappedPositions[p] >= localFrame.getStart())
//      {
//        s = p; // remember first position within local frame
//      }
//      if (e == -1 || mappedPositions[p] <= localFrame.getEnd())
//      {
//        // update end pointer
//        e = p;
//        // compute local map
//        mappedPositions[p] = localFrame.findIndex(mappedPositions[p]);
//      }
//    }
//    int[] _trimmed = new int[e - s + 1];
//    return _trimmed;
    return mappedPositions;
  }

  @Override
  public ContactListI getMappableContactList(final SequenceI localFrame,
          final int column)
  {
    final int _column;
    final int _lcolumn;
    if (localFrame == null)
    {
      throw new Error("Unimplemented when no local sequence given.");
    }
    // return a ContactListI for column
    // column is index into localFrame
    // 1. map column to corresponding column in matrix
    final MappableContactMatrix us = this;
    _lcolumn = localFrame.findPosition(column);

    if (toSeq != null)
    {
      SequenceI lf = localFrame, uf = refSeq;

      // just look for dataset sequences and check they are the same.
      // in future we could use DBRefMappings/whatever.
      while (lf.getDatasetSequence() != null
              || uf.getDatasetSequence() != null)
      {
        if (lf.getDatasetSequence() != null)
        {
          lf = lf.getDatasetSequence();
        }
        if (uf.getDatasetSequence() != null)
        {
          uf = uf.getDatasetSequence();
        }
      }
      if (lf != uf)
      {
        // could try harder to find a mapping
        throw new Error("This Matrix associated with '" + refSeq.getName()
                + "' is not mappable for the given localFrame sequence. ("
                + localFrame.getName() + ")");
      }
      // check the mapping to see if localFrame _lcolumn exists
      int[] word = toSeq.locateInTo(_lcolumn, _lcolumn);
      if (word == null)
      {
        return null;
      }
      _column = word[0];
    }
    else
    {
      // no mapping
      _column = _lcolumn;
    }

    // TODO - remove ? this may be a redundant check
    if (_column < 0 || ((toSeq != null && _column > toSeq.getToHighest())
            || (toSeq == null && getHeight() <= _column)))
    {
      return null;
    }

    // 2. resolve ranges in matrix corresponding to range in localFrame
    final int[] matrixRange = toSeq == null
            ? new int[]
            { localFrame.getStart(), localFrame.getEnd() }
            : toSeq.locateInTo(localFrame.getStart(), localFrame.getEnd());

    int h = 0;
    for (int p = 0; p < matrixRange.length; p += 2)
    {
      h += 1 + Math.abs(matrixRange[p + 1] - matrixRange[p]);
    }
    final int rangeHeight = h;
    // 3. Construct ContactListImpl instance for just those segments.

    return new ContactListImpl(new ContactListProviderI()
    {

      public int getColumn()
      {
        return column;
      }

      @Override
      public int getPosition()
      {
        return _column;
      }

      @Override
      public int getContactHeight()
      {
        return rangeHeight;
      }

      @Override
      public double getContactAt(int mcolumn)
      {
        if (mcolumn < 0 || mcolumn >= rangeHeight)
        {
          return -1;
        }
        return getElementAt(_column, locateInRange(mcolumn));

        // this code maps from mcolumn to localFrame - but that isn't what's
        // needed
        // int loccolumn = localFrame.findPosition(mcolumn);
        // int[] lcolumn=(toSeq==null) ? new int[] {mcolumn} :
        // toSeq.locateInTo(loccolumn,loccolumn);
        // if (lcolumn==null || lcolumn[0] < 0 || lcolumn[0] >= rangeHeight)
        // {
        // return -1;
        // }
        // return getElementAt(_column,lcolumn[0]);
      }

      @Override
      public int[] getMappedPositionsFor(int cStart, int cEnd)
      {
        if (!hasReferenceSeq())
        {
          return ContactListProviderI.super.getMappedPositionsFor(cStart,
                  cEnd);
        }
        // map into segment of matrix being shown
        int realCstart = locateInRange(cStart);
        int realCend = locateInRange(cEnd);

        // TODO account for discontinuities in the mapping

        int[] mappedPositions = toSeq.locateInFrom(realCstart, realCend);
        if (mappedPositions != null)
        {
          int s = -1, e = -1;
          for (int p = 0; p < mappedPositions.length; p++)
          {
            if (s == -1 && mappedPositions[p] >= localFrame.getStart())
            {
              s = p; // remember first position within local frame
            }
            if (e == -1 || mappedPositions[p] <= localFrame.getEnd())
            {
              // update end pointer
              e = p;
              // compute local map
              mappedPositions[p] = localFrame.findIndex(mappedPositions[p]);
            }
          }
        }
        return mappedPositions;
      }

      /**
       * @return the mcolumn'th position in the matrixRange window on the matrix
       */
      private int locateInRange(int mcolumn)
      {

        int h = 0, p = 0;
        while (h < mcolumn && p + 2 < matrixRange.length)
        {
          h += 1 + Math.abs(matrixRange[p + 1] - matrixRange[p]);
          p += 2;
        }
        return matrixRange[p] + mcolumn - h;
      }

      @Override
      public Color getColourForGroup()
      {
        BitSet gp = us.getGroupsFor(_column);
        Color col = us.getColourForGroup(gp);
        return col;
      }
    });
  }

  /**
   * get a specific element of the contact matrix in its data-local coordinates
   * rather than the mapped frame. Implementations are allowed to throw
   * RunTimeExceptions if _column/i are out of bounds
   * 
   * @param _column
   * @param i
   * @return
   */
  protected abstract double getElementAt(int _column, int i);

}
