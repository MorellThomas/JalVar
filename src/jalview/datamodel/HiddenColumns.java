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
package jalview.datamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class manages the collection of hidden columns associated with an
 * alignment. To iterate over the collection, or over visible columns/regions,
 * use an iterator obtained from one of:
 * 
 * - getBoundedIterator: iterates over the hidden regions, within some bounds,
 * returning *absolute* positions
 * 
 * - getBoundedStartIterator: iterates over the start positions of hidden
 * regions, within some bounds, returning *visible* positions
 * 
 * - getVisContigsIterator: iterates over visible regions in a range, returning
 * *absolute* positions
 * 
 * - getVisibleColsIterator: iterates over the visible *columns*
 * 
 * For performance reasons, provide bounds where possible. Note that column
 * numbering begins at 0 throughout this class.
 * 
 * @author kmourao
 */

/* Implementation notes:
 * 
 * Methods which change the hiddenColumns collection should use a writeLock to
 * prevent other threads accessing the hiddenColumns collection while changes
 * are being made. They should also reset the hidden columns cursor, and either
 * update the hidden columns count, or set it to 0 (so that it will later be
 * updated when needed).
 * 
 * 
 * Methods which only need read access to the hidden columns collection should
 * use a readLock to prevent other threads changing the hidden columns
 * collection while it is in use.
 */
public class HiddenColumns
{
  private static final int HASH_MULTIPLIER = 31;

  private static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();

  /*
   * Cursor which tracks the last used hidden columns region, and the number 
   * of hidden columns up to (but not including) that region.
   */
  private HiddenColumnsCursor cursor = new HiddenColumnsCursor();

  /*
   * cache of the number of hidden columns: must be kept up to date by methods 
   * which add or remove hidden columns
   */
  private int numColumns = 0;

  /*
   * list of hidden column [start, end] ranges; the list is maintained in
   * ascending start column order
   */
  private List<int[]> hiddenColumns = new ArrayList<>();

  /**
   * Constructor
   */
  public HiddenColumns()
  {
  }

  /**
   * Copy constructor
   * 
   * @param copy
   *          the HiddenColumns object to copy from
   */
  public HiddenColumns(HiddenColumns copy)
  {
    this(copy, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
  }

  /**
   * Copy constructor within bounds and with offset. Copies hidden column
   * regions fully contained between start and end, and offsets positions by
   * subtracting offset.
   * 
   * @param copy
   *          HiddenColumns instance to copy from
   * @param start
   *          lower bound to copy from
   * @param end
   *          upper bound to copy to
   * @param offset
   *          offset to subtract from each region boundary position
   * 
   */
  public HiddenColumns(HiddenColumns copy, int start, int end, int offset)
  {
    try
    {
      LOCK.writeLock().lock();
      if (copy != null)
      {
        numColumns = 0;
        Iterator<int[]> it = copy.getBoundedIterator(start, end);
        while (it.hasNext())
        {
          int[] region = it.next();
          // still need to check boundaries because iterator returns
          // all overlapping regions and we need contained regions
          if (region[0] >= start && region[1] <= end)
          {
            hiddenColumns
                    .add(new int[]
                    { region[0] - offset, region[1] - offset });
            numColumns += region[1] - region[0] + 1;
          }
        }
        cursor = new HiddenColumnsCursor(hiddenColumns);
      }
    } finally
    {
      LOCK.writeLock().unlock();
    }
  }

  /**
   * Adds the specified column range to the hidden columns collection
   * 
   * @param start
   *          start of range to add (absolute position in alignment)
   * @param end
   *          end of range to add (absolute position in alignment)
   */
  public void hideColumns(int start, int end)
  {
    try
    {
      LOCK.writeLock().lock();

      int previndex = 0;
      int prevHiddenCount = 0;
      int regionindex = 0;
      if (!hiddenColumns.isEmpty())
      {
        // set up cursor reset values
        HiddenCursorPosition cursorPos = cursor.findRegionForColumn(start,
                false);
        regionindex = cursorPos.getRegionIndex();

        if (regionindex > 0)
        {
          // get previous index and hidden count for updating the cursor later
          previndex = regionindex - 1;
          int[] prevRegion = hiddenColumns.get(previndex);
          prevHiddenCount = cursorPos.getHiddenSoFar()
                  - (prevRegion[1] - prevRegion[0] + 1);
        }
      }

      // new range follows everything else; check first to avoid looping over
      // whole hiddenColumns collection
      if (hiddenColumns.isEmpty()
              || start > hiddenColumns.get(hiddenColumns.size() - 1)[1])
      {
        hiddenColumns.add(new int[] { start, end });
        numColumns += end - start + 1;
      }
      else
      {
        /*
         * traverse existing hidden ranges and insert / amend / append as
         * appropriate
         */
        boolean added = false;
        if (regionindex > 0)
        {
          added = insertRangeAtRegion(regionindex - 1, start, end);
        }
        if (!added && regionindex < hiddenColumns.size())
        {
          insertRangeAtRegion(regionindex, start, end);
        }
      }

      // reset the cursor to just before our insertion point: this saves
      // a lot of reprocessing in large alignments
      cursor = new HiddenColumnsCursor(hiddenColumns, previndex,
              prevHiddenCount);
    } finally
    {
      LOCK.writeLock().unlock();
    }
  }

  /**
   * Insert [start, range] at the region at index i in hiddenColumns, if
   * feasible
   * 
   * @param i
   *          index to insert at
   * @param start
   *          start of range to insert
   * @param end
   *          end of range to insert
   * @return true if range was successfully inserted
   */
  private boolean insertRangeAtRegion(int i, int start, int end)
  {
    boolean added = false;

    int[] region = hiddenColumns.get(i);
    if (end < region[0] - 1)
    {
      /*
       * insert discontiguous preceding range
       */
      hiddenColumns.add(i, new int[] { start, end });
      numColumns += end - start + 1;
      added = true;
    }
    else if (end <= region[1])
    {
      /*
       * new range overlaps existing, or is contiguous preceding it - adjust
       * start column
       */
      int oldstart = region[0];
      region[0] = Math.min(region[0], start);
      numColumns += oldstart - region[0]; // new columns are between old and
                                          // adjusted starts
      added = true;
    }
    else if (start <= region[1] + 1)
    {
      /*
       * new range overlaps existing, or is contiguous following it - adjust
       * start and end columns
       */
      insertRangeAtOverlap(i, start, end, region);
      added = true;
    }
    return added;
  }

  /**
   * Insert a range whose start position overlaps an existing region and/or is
   * contiguous to the right of the region
   * 
   * @param i
   *          index to insert at
   * @param start
   *          start of range to insert
   * @param end
   *          end of range to insert
   * @param region
   *          the overlapped/continued region
   */
  private void insertRangeAtOverlap(int i, int start, int end, int[] region)
  {
    int oldstart = region[0];
    int oldend = region[1];
    region[0] = Math.min(region[0], start);
    region[1] = Math.max(region[1], end);

    numColumns += oldstart - region[0];

    /*
     * also update or remove any subsequent ranges 
     * that are overlapped
     */
    int endi = i;
    while (endi < hiddenColumns.size() - 1)
    {
      int[] nextRegion = hiddenColumns.get(endi + 1);
      if (nextRegion[0] > end + 1)
      {
        /*
         * gap to next hidden range - no more to update
         */
        break;
      }
      numColumns -= nextRegion[1] - nextRegion[0] + 1;
      region[1] = Math.max(nextRegion[1], end);
      endi++;
    }
    numColumns += region[1] - oldend;
    hiddenColumns.subList(i + 1, endi + 1).clear();
  }

  /**
   * hide a list of ranges
   * 
   * @param ranges
   */
  public void hideList(List<int[]> ranges)
  {
    try
    {
      LOCK.writeLock().lock();
      for (int[] r : ranges)
      {
        hideColumns(r[0], r[1]);
      }
      cursor = new HiddenColumnsCursor(hiddenColumns);

    } finally
    {
      LOCK.writeLock().unlock();
    }
  }

  /**
   * Unhides, and adds to the selection list, all hidden columns
   */
  public void revealAllHiddenColumns(ColumnSelection sel)
  {
    try
    {
      LOCK.writeLock().lock();

      for (int[] region : hiddenColumns)
      {
        for (int j = region[0]; j < region[1] + 1; j++)
        {
          sel.addElement(j);
        }
      }
      hiddenColumns.clear();
      cursor = new HiddenColumnsCursor(hiddenColumns);
      numColumns = 0;

    } finally
    {
      LOCK.writeLock().unlock();
    }
  }

  /**
   * Reveals, and marks as selected, the hidden column range with the given
   * start column
   * 
   * @param start
   *          the start column to look for
   * @param sel
   *          the column selection to add the hidden column range to
   */
  public void revealHiddenColumns(int start, ColumnSelection sel)
  {
    try
    {
      LOCK.writeLock().lock();

      if (!hiddenColumns.isEmpty())
      {
        int regionIndex = cursor.findRegionForColumn(start, false)
                .getRegionIndex();

        if (regionIndex != -1 && regionIndex != hiddenColumns.size())
        {
          // regionIndex is the region which either contains start
          // or lies to the right of start
          int[] region = hiddenColumns.get(regionIndex);
          if (start == region[0])
          {
            for (int j = region[0]; j < region[1] + 1; j++)
            {
              sel.addElement(j);
            }
            int colsToRemove = region[1] - region[0] + 1;
            hiddenColumns.remove(regionIndex);
            numColumns -= colsToRemove;
          }
        }
      }
    } finally
    {
      LOCK.writeLock().unlock();
    }
  }

  /**
   * Output regions data as a string. String is in the format:
   * reg0[0]<between>reg0[1]<delimiter>reg1[0]<between>reg1[1] ... regn[1]
   * 
   * @param delimiter
   *          string to delimit regions
   * @param betweenstring
   *          to put between start and end region values
   * @return regions formatted according to delimiter and between strings
   */
  public String regionsToString(String delimiter, String between)
  {
    try
    {
      LOCK.readLock().lock();
      StringBuilder regionBuilder = new StringBuilder();

      boolean first = true;
      for (int[] range : hiddenColumns)
      {
        if (!first)
        {
          regionBuilder.append(delimiter);
        }
        else
        {
          first = false;
        }
        regionBuilder.append(range[0]).append(between).append(range[1]);

      }

      return regionBuilder.toString();
    } finally
    {
      LOCK.readLock().unlock();
    }
  }

  /**
   * Find the number of hidden columns
   * 
   * @return number of hidden columns
   */
  public int getSize()
  {
    return numColumns;
  }

  /**
   * Get the number of distinct hidden regions
   * 
   * @return number of regions
   */
  public int getNumberOfRegions()
  {
    try
    {
      LOCK.readLock().lock();
      return hiddenColumns.size();
    } finally
    {
      LOCK.readLock().unlock();
    }
  }

  /**
   * Answers true if obj is an instance of HiddenColumns, and holds the same
   * array of start-end column ranges as this, else answers false
   */
  @Override
  public boolean equals(Object obj)
  {
    try
    {
      LOCK.readLock().lock();

      if (!(obj instanceof HiddenColumns))
      {
        return false;
      }
      HiddenColumns that = (HiddenColumns) obj;

      /*
       * check hidden columns are either both null, or match
       */

      if (that.hiddenColumns.size() != this.hiddenColumns.size())
      {
        return false;
      }

      Iterator<int[]> it = this.iterator();
      Iterator<int[]> thatit = that.iterator();
      while (it.hasNext())
      {
        if (!(Arrays.equals(it.next(), thatit.next())))
        {
          return false;
        }
      }
      return true;

    } finally
    {
      LOCK.readLock().unlock();
    }
  }

  /**
   * Return absolute column index for a visible column index
   * 
   * @param column
   *          int column index in alignment view (count from zero)
   * @return alignment column index for column
   */
  public int visibleToAbsoluteColumn(int column)
  {
    try
    {
      LOCK.readLock().lock();
      int result = column;

      if (!hiddenColumns.isEmpty())
      {
        result += cursor.findRegionForColumn(column, true).getHiddenSoFar();
      }

      return result;
    } finally
    {
      LOCK.readLock().unlock();
    }
  }

  /**
   * Use this method to find out where a column will appear in the visible
   * alignment when hidden columns exist. If the column is not visible, then the
   * index of the next visible column on the left will be returned (or 0 if
   * there is no visible column on the left)
   * 
   * @param hiddenColumn
   *          the column index in the full alignment including hidden columns
   * @return the position of the column in the visible alignment
   */
  public int absoluteToVisibleColumn(int hiddenColumn)
  {
    try
    {
      LOCK.readLock().lock();
      int result = hiddenColumn;

      if (!hiddenColumns.isEmpty())
      {
        HiddenCursorPosition cursorPos = cursor
                .findRegionForColumn(hiddenColumn, false);
        int index = cursorPos.getRegionIndex();
        int hiddenBeforeCol = cursorPos.getHiddenSoFar();

        // just subtract hidden cols count - this works fine if column is
        // visible
        result = hiddenColumn - hiddenBeforeCol;

        // now check in case column is hidden - it will be in the returned
        // hidden region
        if (index < hiddenColumns.size())
        {
          int[] region = hiddenColumns.get(index);
          if (hiddenColumn >= region[0] && hiddenColumn <= region[1])
          {
            // actually col is hidden, return region[0]-1
            // unless region[0]==0 in which case return 0
            if (region[0] == 0)
            {
              result = 0;
            }
            else
            {
              result = region[0] - 1 - hiddenBeforeCol;
            }
          }
        }
      }

      return result; // return the shifted position after removing hidden
                     // columns.
    } finally
    {
      LOCK.readLock().unlock();
    }
  }

  /**
   * Find the visible column which is a given visible number of columns to the
   * left (negative visibleDistance) or right (positive visibleDistance) of
   * startColumn. If startColumn is not visible, we use the visible column at
   * the left boundary of the hidden region containing startColumn.
   * 
   * @param visibleDistance
   *          the number of visible columns to offset by (left offset = negative
   *          value; right offset = positive value)
   * @param startColumn
   *          the position of the column to start from (absolute position)
   * @return the position of the column which is <visibleDistance> away
   *         (absolute position)
   */
  public int offsetByVisibleColumns(int visibleDistance, int startColumn)
  {
    try
    {
      LOCK.readLock().lock();
      int start = absoluteToVisibleColumn(startColumn);
      return visibleToAbsoluteColumn(start + visibleDistance);

    } finally
    {
      LOCK.readLock().unlock();
    }
  }

  /**
   * This method returns the rightmost limit of a region of an alignment with
   * hidden columns. In otherwords, the next hidden column.
   * 
   * @param alPos
   *          the absolute (visible) alignmentPosition to find the next hidden
   *          column for
   * @return the index of the next hidden column, or alPos if there is no next
   *         hidden column
   */
  public int getNextHiddenBoundary(boolean left, int alPos)
  {
    try
    {
      LOCK.readLock().lock();
      if (!hiddenColumns.isEmpty())
      {
        int index = cursor.findRegionForColumn(alPos, false)
                .getRegionIndex();

        if (left && index > 0)
        {
          int[] region = hiddenColumns.get(index - 1);
          return region[1];
        }
        else if (!left && index < hiddenColumns.size())
        {
          int[] region = hiddenColumns.get(index);
          if (alPos < region[0])
          {
            return region[0];
          }
          else if ((alPos <= region[1])
                  && (index + 1 < hiddenColumns.size()))
          {
            // alPos is within a hidden region, return the next one
            // if there is one
            region = hiddenColumns.get(index + 1);
            return region[0];
          }
        }
      }
      return alPos;
    } finally
    {
      LOCK.readLock().unlock();
    }
  }

  /**
   * Answers if a column in the alignment is visible
   * 
   * @param column
   *          absolute position of column in the alignment
   * @return true if column is visible
   */
  public boolean isVisible(int column)
  {
    try
    {
      LOCK.readLock().lock();

      if (!hiddenColumns.isEmpty())
      {
        int regionindex = cursor.findRegionForColumn(column, false)
                .getRegionIndex();
        if (regionindex > -1 && regionindex < hiddenColumns.size())
        {
          int[] region = hiddenColumns.get(regionindex);
          // already know that column <= region[1] as cursor returns containing
          // region or region to right
          if (column >= region[0])
          {
            return false;
          }
        }
      }
      return true;

    } finally
    {
      LOCK.readLock().unlock();
    }
  }

  /**
   * 
   * @return true if there are columns hidden
   */
  public boolean hasHiddenColumns()
  {
    try
    {
      LOCK.readLock().lock();

      // we don't use getSize()>0 here because it has to iterate over
      // the full hiddenColumns collection and so will be much slower
      return (!hiddenColumns.isEmpty());
    } finally
    {
      LOCK.readLock().unlock();
    }
  }

  /**
   * 
   * @return true if there is more than one hidden column region
   */
  public boolean hasMultiHiddenColumnRegions()
  {
    try
    {
      LOCK.readLock().lock();
      return !hiddenColumns.isEmpty() && hiddenColumns.size() > 1;
    } finally
    {
      LOCK.readLock().unlock();
    }
  }

  /**
   * Returns a hashCode built from hidden column ranges
   */
  @Override
  public int hashCode()
  {
    try
    {
      LOCK.readLock().lock();
      int hashCode = 1;

      for (int[] hidden : hiddenColumns)
      {
        hashCode = HASH_MULTIPLIER * hashCode + hidden[0];
        hashCode = HASH_MULTIPLIER * hashCode + hidden[1];
      }
      return hashCode;
    } finally
    {
      LOCK.readLock().unlock();
    }
  }

  /**
   * Hide columns corresponding to the marked bits
   * 
   * @param inserts
   *          - columns mapped to bits starting from zero
   */
  public void hideColumns(BitSet inserts)
  {
    hideColumns(inserts, 0, inserts.length() - 1);
  }

  /**
   * Hide columns corresponding to the marked bits, within the range
   * [start,end]. Entries in tohide which are outside [start,end] are ignored.
   * 
   * @param tohide
   *          columns mapped to bits starting from zero
   * @param start
   *          start of range to hide columns within
   * @param end
   *          end of range to hide columns within
   */
  private void hideColumns(BitSet tohide, int start, int end)
  {
    try
    {
      LOCK.writeLock().lock();
      for (int firstSet = tohide
              .nextSetBit(start), lastSet = start; firstSet >= start
                      && lastSet <= end; firstSet = tohide
                              .nextSetBit(lastSet))
      {
        lastSet = tohide.nextClearBit(firstSet);
        if (lastSet <= end)
        {
          hideColumns(firstSet, lastSet - 1);
        }
        else if (firstSet <= end)
        {
          hideColumns(firstSet, end);
        }
      }
      cursor = new HiddenColumnsCursor(hiddenColumns);
    } finally
    {
      LOCK.writeLock().unlock();
    }
  }

  /**
   * Hide columns corresponding to the marked bits, within the range
   * [start,end]. Entries in tohide which are outside [start,end] are ignored.
   * NB Existing entries in [start,end] are cleared.
   * 
   * @param tohide
   *          columns mapped to bits starting from zero
   * @param start
   *          start of range to hide columns within
   * @param end
   *          end of range to hide columns within
   */
  public void clearAndHideColumns(BitSet tohide, int start, int end)
  {
    clearHiddenColumnsInRange(start, end);
    hideColumns(tohide, start, end);
  }

  /**
   * Make all columns in the range [start,end] visible
   * 
   * @param start
   *          start of range to show columns
   * @param end
   *          end of range to show columns
   */
  private void clearHiddenColumnsInRange(int start, int end)
  {
    try
    {
      LOCK.writeLock().lock();

      if (!hiddenColumns.isEmpty())
      {
        HiddenCursorPosition pos = cursor.findRegionForColumn(start, false);
        int index = pos.getRegionIndex();

        if (index != -1 && index != hiddenColumns.size())
        {
          // regionIndex is the region which either contains start
          // or lies to the right of start
          int[] region = hiddenColumns.get(index);
          if (region[0] < start && region[1] >= start)
          {
            // region contains start, truncate so that it ends just before start
            numColumns -= region[1] - start + 1;
            region[1] = start - 1;
            index++;
          }

          int endi = index;
          while (endi < hiddenColumns.size())
          {
            region = hiddenColumns.get(endi);

            if (region[1] > end)
            {
              if (region[0] <= end)
              {
                // region contains end, truncate so it starts just after end
                numColumns -= end - region[0] + 1;
                region[0] = end + 1;
              }
              break;
            }

            numColumns -= region[1] - region[0] + 1;
            endi++;
          }
          hiddenColumns.subList(index, endi).clear();

        }

        cursor = new HiddenColumnsCursor(hiddenColumns);
      }
    } finally
    {
      LOCK.writeLock().unlock();
    }
  }

  /**
   * 
   * @param updates
   *          BitSet where hidden columns will be marked
   */
  protected void andNot(BitSet updates)
  {
    try
    {
      LOCK.writeLock().lock();

      BitSet hiddenBitSet = new BitSet();
      for (int[] range : hiddenColumns)
      {
        hiddenBitSet.set(range[0], range[1] + 1);
      }
      hiddenBitSet.andNot(updates);
      hiddenColumns.clear();
      hideColumns(hiddenBitSet);
    } finally
    {
      LOCK.writeLock().unlock();
    }
  }

  /**
   * Calculate the visible start and end index of an alignment.
   * 
   * @param width
   *          full alignment width
   * @return integer array where: int[0] = startIndex, and int[1] = endIndex
   */
  public int[] getVisibleStartAndEndIndex(int width)
  {
    try
    {
      LOCK.readLock().lock();

      int firstVisible = 0;
      int lastVisible = width - 1;

      if (!hiddenColumns.isEmpty())
      {
        // first visible col with index 0, convert to absolute index
        firstVisible = visibleToAbsoluteColumn(0);

        // last visible column is either immediately to left of
        // last hidden region, or is just the last column in the alignment
        int[] lastregion = hiddenColumns.get(hiddenColumns.size() - 1);
        if (lastregion[1] == width - 1)
        {
          // last region is at very end of alignment
          // last visible column immediately precedes it
          lastVisible = lastregion[0] - 1;
        }
      }
      return new int[] { firstVisible, lastVisible };

    } finally
    {
      LOCK.readLock().unlock();
    }
  }

  /**
   * Finds the hidden region (if any) which starts or ends at res
   * 
   * @param res
   *          visible residue position, unadjusted for hidden columns
   * @return region as [start,end] or null if no matching region is found. If
   *         res is adjacent to two regions, returns the left region.
   */
  public int[] getRegionWithEdgeAtRes(int res)
  {
    try
    {
      LOCK.readLock().lock();
      int adjres = visibleToAbsoluteColumn(res);

      int[] reveal = null;

      if (!hiddenColumns.isEmpty())
      {
        // look for a region ending just before adjres
        int regionindex = cursor.findRegionForColumn(adjres - 1, false)
                .getRegionIndex();
        if (regionindex < hiddenColumns.size()
                && hiddenColumns.get(regionindex)[1] == adjres - 1)
        {
          reveal = hiddenColumns.get(regionindex);
        }
        // check if the region ends just after adjres
        else if (regionindex < hiddenColumns.size()
                && hiddenColumns.get(regionindex)[0] == adjres + 1)
        {
          reveal = hiddenColumns.get(regionindex);
        }
      }
      return reveal;

    } finally
    {
      LOCK.readLock().unlock();
    }
  }

  /**
   * Return an iterator over the hidden regions
   */
  public Iterator<int[]> iterator()
  {
    try
    {
      LOCK.readLock().lock();
      return new RangeIterator(hiddenColumns);
    } finally
    {
      LOCK.readLock().unlock();
    }
  }

  /**
   * Return a bounded iterator over the hidden regions
   * 
   * @param start
   *          position to start from (inclusive, absolute column position)
   * @param end
   *          position to end at (inclusive, absolute column position)
   * @return
   */
  public Iterator<int[]> getBoundedIterator(int start, int end)
  {
    try
    {
      LOCK.readLock().lock();
      return new RangeIterator(start, end, hiddenColumns);
    } finally
    {
      LOCK.readLock().unlock();
    }
  }

  /**
   * Return a bounded iterator over the *visible* start positions of hidden
   * regions
   * 
   * @param start
   *          position to start from (inclusive, visible column position)
   * @param end
   *          position to end at (inclusive, visible column position)
   */
  public Iterator<Integer> getStartRegionIterator(int start, int end)
  {
    try
    {
      LOCK.readLock().lock();

      // get absolute position of column in alignment
      int absoluteStart = visibleToAbsoluteColumn(start);

      // Get cursor position and supply it to the iterator:
      // Since we want visible region start, we look for a cursor for the
      // (absoluteStart-1), then if absoluteStart is the start of a visible
      // region we'll get the cursor pointing to the region before, which is
      // what we want
      HiddenCursorPosition pos = cursor
              .findRegionForColumn(absoluteStart - 1, false);

      return new StartRegionIterator(pos, start, end, hiddenColumns);
    } finally
    {
      LOCK.readLock().unlock();
    }
  }

  /**
   * Return an iterator over visible *columns* (not regions) between the given
   * start and end boundaries
   * 
   * @param start
   *          first column (inclusive)
   * @param end
   *          last column (inclusive)
   */
  public Iterator<Integer> getVisibleColsIterator(int start, int end)
  {
    try
    {
      LOCK.readLock().lock();
      return new RangeElementsIterator(
              new VisibleContigsIterator(start, end + 1, hiddenColumns));
    } finally
    {
      LOCK.readLock().unlock();
    }
  }

  /**
   * return an iterator over visible segments between the given start and end
   * boundaries
   * 
   * @param start
   *          first column, inclusive from 0
   * @param end
   *          last column - not inclusive
   * @param useVisibleCoords
   *          if true, start and end are visible column positions, not absolute
   *          positions*
   */
  public VisibleContigsIterator getVisContigsIterator(int start, int end,
          boolean useVisibleCoords)
  {
    int adjstart = start;
    int adjend = end;
    if (useVisibleCoords)
    {
      adjstart = visibleToAbsoluteColumn(start);
      adjend = visibleToAbsoluteColumn(end);
    }

    try
    {
      LOCK.readLock().lock();
      return new VisibleContigsIterator(adjstart, adjend, hiddenColumns);
    } finally
    {
      LOCK.readLock().unlock();
    }
  }
}
