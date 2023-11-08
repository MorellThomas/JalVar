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
package jalview.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import jalview.bin.Console;

/**
 * A simple way of bijectively mapping a non-contiguous linear range to another
 * non-contiguous linear range.
 * 
 * Use at your own risk!
 * 
 * TODO: test/ensure that sense of from and to ratio start position is conserved
 * (codon start position recovery)
 */
public class MapList
{

  /*
   * Subregions (base 1) described as { [start1, end1], [start2, end2], ...}
   */
  private List<int[]> fromShifts;

  /*
   * Same format as fromShifts, for the 'mapped to' sequence
   */
  private List<int[]> toShifts;

  /*
   * number of steps in fromShifts to one toRatio unit
   */
  private int fromRatio;

  /*
   * number of steps in toShifts to one fromRatio
   */
  private int toRatio;

  /*
   * lowest and highest value in the from Map
   */
  private int fromLowest;

  private int fromHighest;

  /*
   * lowest and highest value in the to Map
   */
  private int toLowest;

  private int toHighest;

  /**
   * Constructor
   */
  public MapList()
  {
    fromShifts = new ArrayList<>();
    toShifts = new ArrayList<>();
  }

  /**
   * Two MapList objects are equal if they are the same object, or they both
   * have populated shift ranges and all values are the same.
   */
  @Override
  public boolean equals(Object o)
  {
    if (o == null || !(o instanceof MapList))
    {
      return false;
    }

    MapList obj = (MapList) o;
    if (obj == this)
    {
      return true;
    }
    if (obj.fromRatio != fromRatio || obj.toRatio != toRatio
            || obj.fromShifts == null || obj.toShifts == null)
    {
      return false;
    }
    return Arrays.deepEquals(fromShifts.toArray(), obj.fromShifts.toArray())
            && Arrays.deepEquals(toShifts.toArray(),
                    obj.toShifts.toArray());
  }

  /**
   * Returns a hashcode made from the fromRatio, toRatio, and from/to ranges
   */
  @Override
  public int hashCode()
  {
    int hashCode = 31 * fromRatio;
    hashCode = 31 * hashCode + toRatio;
    for (int[] shift : fromShifts)
    {
      hashCode = 31 * hashCode + shift[0];
      hashCode = 31 * hashCode + shift[1];
    }
    for (int[] shift : toShifts)
    {
      hashCode = 31 * hashCode + shift[0];
      hashCode = 31 * hashCode + shift[1];
    }

    return hashCode;
  }

  /**
   * Returns the 'from' ranges as {[start1, end1], [start2, end2], ...}
   * 
   * @return
   */
  public List<int[]> getFromRanges()
  {
    return fromShifts;
  }

  /**
   * Returns the 'to' ranges as {[start1, end1], [start2, end2], ...}
   * 
   * @return
   */
  public List<int[]> getToRanges()
  {
    return toShifts;
  }

  /**
   * Flattens a list of [start, end] into a single [start1, end1, start2,
   * end2,...] array.
   * 
   * @param shifts
   * @return
   */
  protected static int[] getRanges(List<int[]> shifts)
  {
    int[] rnges = new int[2 * shifts.size()];
    int i = 0;
    for (int[] r : shifts)
    {
      rnges[i++] = r[0];
      rnges[i++] = r[1];
    }
    return rnges;
  }

  /**
   * 
   * @return length of mapped phrase in from
   */
  public int getFromRatio()
  {
    return fromRatio;
  }

  /**
   * 
   * @return length of mapped phrase in to
   */
  public int getToRatio()
  {
    return toRatio;
  }

  public int getFromLowest()
  {
    return fromLowest;
  }

  public int getFromHighest()
  {
    return fromHighest;
  }

  public int getToLowest()
  {
    return toLowest;
  }

  public int getToHighest()
  {
    return toHighest;
  }

  /**
   * Constructor given from and to ranges as [start1, end1, start2, end2,...].
   * There is no validation check that the ranges do not overlap each other.
   * 
   * @param from
   *          contiguous regions as [start1, end1, start2, end2, ...]
   * @param to
   *          same format as 'from'
   * @param fromRatio
   *          phrase length in 'from' (e.g. 3 for dna)
   * @param toRatio
   *          phrase length in 'to' (e.g. 1 for protein)
   */
  public MapList(int from[], int to[], int fromRatio, int toRatio)
  {
    this();
    this.fromRatio = fromRatio;
    this.toRatio = toRatio;
    fromLowest = Integer.MAX_VALUE;
    fromHighest = Integer.MIN_VALUE;

    for (int i = 0; i < from.length; i += 2)
    {
      /*
       * note lowest and highest values - bearing in mind the
       * direction may be reversed
       */
      fromLowest = Math.min(fromLowest, Math.min(from[i], from[i + 1]));
      fromHighest = Math.max(fromHighest, Math.max(from[i], from[i + 1]));
      fromShifts.add(new int[] { from[i], from[i + 1] });
    }

    toLowest = Integer.MAX_VALUE;
    toHighest = Integer.MIN_VALUE;
    for (int i = 0; i < to.length; i += 2)
    {
      toLowest = Math.min(toLowest, Math.min(to[i], to[i + 1]));
      toHighest = Math.max(toHighest, Math.max(to[i], to[i + 1]));
      toShifts.add(new int[] { to[i], to[i + 1] });
    }
  }

  /**
   * Copy constructor. Creates an identical mapping.
   * 
   * @param map
   */
  public MapList(MapList map)
  {
    this();
    // TODO not used - remove?
    this.fromLowest = map.fromLowest;
    this.fromHighest = map.fromHighest;
    this.toLowest = map.toLowest;
    this.toHighest = map.toHighest;

    this.fromRatio = map.fromRatio;
    this.toRatio = map.toRatio;
    if (map.fromShifts != null)
    {
      for (int[] r : map.fromShifts)
      {
        fromShifts.add(new int[] { r[0], r[1] });
      }
    }
    if (map.toShifts != null)
    {
      for (int[] r : map.toShifts)
      {
        toShifts.add(new int[] { r[0], r[1] });
      }
    }
  }

  /**
   * Constructor given ranges as lists of [start, end] positions. There is no
   * validation check that the ranges do not overlap each other.
   * 
   * @param fromRange
   * @param toRange
   * @param fromRatio
   * @param toRatio
   */
  public MapList(List<int[]> fromRange, List<int[]> toRange, int fromRatio,
          int toRatio)
  {
    this();
    fromRange = coalesceRanges(fromRange);
    toRange = coalesceRanges(toRange);
    this.fromShifts = fromRange;
    this.toShifts = toRange;
    this.fromRatio = fromRatio;
    this.toRatio = toRatio;

    fromLowest = Integer.MAX_VALUE;
    fromHighest = Integer.MIN_VALUE;
    for (int[] range : fromRange)
    {
      if (range.length != 2)
      {
        // throw new IllegalArgumentException(range);
        Console.error("Invalid format for fromRange "
                + Arrays.toString(range) + " may cause errors");
      }
      fromLowest = Math.min(fromLowest, Math.min(range[0], range[1]));
      fromHighest = Math.max(fromHighest, Math.max(range[0], range[1]));
    }

    toLowest = Integer.MAX_VALUE;
    toHighest = Integer.MIN_VALUE;
    for (int[] range : toRange)
    {
      if (range.length != 2)
      {
        // throw new IllegalArgumentException(range);
        Console.error("Invalid format for toRange " + Arrays.toString(range)
                + " may cause errors");
      }
      toLowest = Math.min(toLowest, Math.min(range[0], range[1]));
      toHighest = Math.max(toHighest, Math.max(range[0], range[1]));
    }
  }

  /**
   * Consolidates a list of ranges so that any contiguous ranges are merged.
   * This assumes the ranges are already in start order (does not sort them).
   * <p>
   * The main use case for this method is when mapping cDNA sequence to its
   * protein product, based on CDS feature ranges which derive from spliced
   * exons, but are contiguous on the cDNA sequence. For example
   * 
   * <pre>
   *   CDS 1-20  // from exon1
   *   CDS 21-35 // from exon2
   *   CDS 36-71 // from exon3
   * 'coalesce' to range 1-71
   * </pre>
   * 
   * @param ranges
   * @return the same list (if unchanged), else a new merged list, leaving the
   *         input list unchanged
   */
  public static List<int[]> coalesceRanges(final List<int[]> ranges)
  {
    if (ranges == null || ranges.size() < 2)
    {
      return ranges;
    }

    boolean changed = false;
    List<int[]> merged = new ArrayList<>();
    int[] lastRange = ranges.get(0);
    int lastDirection = lastRange[1] >= lastRange[0] ? 1 : -1;
    lastRange = new int[] { lastRange[0], lastRange[1] };
    merged.add(lastRange);
    boolean first = true;

    for (final int[] range : ranges)
    {
      if (first)
      {
        first = false;
        continue;
      }

      int direction = range[1] >= range[0] ? 1 : -1;

      /*
       * if next range is in the same direction as last and contiguous,
       * just update the end position of the last range
       */
      boolean sameDirection = range[1] == range[0]
              || direction == lastDirection;
      boolean extending = range[0] == lastRange[1] + lastDirection;
      if (sameDirection && extending)
      {
        lastRange[1] = range[1];
        changed = true;
      }
      else
      {
        lastRange = new int[] { range[0], range[1] };
        merged.add(lastRange);
        // careful: merging [5, 5] after [7, 6] should keep negative direction
        lastDirection = (range[1] == range[0]) ? lastDirection : direction;
      }
    }

    return changed ? merged : ranges;
  }

  /**
   * get all mapped positions from 'from' to 'to'
   * 
   * @return int[][] { int[] { fromStart, fromFinish, toStart, toFinish }, int
   *         [fromFinish-fromStart+2] { toStart..toFinish mappings}}
   */
  protected int[][] makeFromMap()
  {
    // TODO only used for test - remove??
    return posMap(fromShifts, fromRatio, toShifts, toRatio);
  }

  /**
   * get all mapped positions from 'to' to 'from'
   * 
   * @return int[to position]=position mapped in from
   */
  protected int[][] makeToMap()
  {
    // TODO only used for test - remove??
    return posMap(toShifts, toRatio, fromShifts, fromRatio);
  }

  /**
   * construct an int map for intervals in intVals
   * 
   * @param shiftTo
   * @return int[] { from, to pos in range }, int[range.to-range.from+1]
   *         returning mapped position
   */
  private int[][] posMap(List<int[]> shiftTo, int sourceRatio,
          List<int[]> shiftFrom, int targetRatio)
  {
    // TODO only used for test - remove??
    int iv = 0, ivSize = shiftTo.size();
    if (iv >= ivSize)
    {
      return null;
    }
    int[] intv = shiftTo.get(iv++);
    int from = intv[0], to = intv[1];
    if (from > to)
    {
      from = intv[1];
      to = intv[0];
    }
    while (iv < ivSize)
    {
      intv = shiftTo.get(iv++);
      if (intv[0] < from)
      {
        from = intv[0];
      }
      if (intv[1] < from)
      {
        from = intv[1];
      }
      if (intv[0] > to)
      {
        to = intv[0];
      }
      if (intv[1] > to)
      {
        to = intv[1];
      }
    }
    int tF = 0, tT = 0;
    int mp[][] = new int[to - from + 2][];
    for (int i = 0; i < mp.length; i++)
    {
      int[] m = shift(i + from, shiftTo, sourceRatio, shiftFrom,
              targetRatio);
      if (m != null)
      {
        if (i == 0)
        {
          tF = tT = m[0];
        }
        else
        {
          if (m[0] < tF)
          {
            tF = m[0];
          }
          if (m[0] > tT)
          {
            tT = m[0];
          }
        }
      }
      mp[i] = m;
    }
    int[][] map = new int[][] { new int[] { from, to, tF, tT },
        new int[to - from + 2] };

    map[0][2] = tF;
    map[0][3] = tT;

    for (int i = 0; i < mp.length; i++)
    {
      if (mp[i] != null)
      {
        map[1][i] = mp[i][0] - tF;
      }
      else
      {
        map[1][i] = -1; // indicates an out of range mapping
      }
    }
    return map;
  }

  /**
   * addShift
   * 
   * @param pos
   *          start position for shift (in original reference frame)
   * @param shift
   *          length of shift
   * 
   *          public void addShift(int pos, int shift) { int sidx = 0; int[]
   *          rshift=null; while (sidx<shifts.size() && (rshift=(int[])
   *          shifts.elementAt(sidx))[0]<pos) sidx++; if (sidx==shifts.size())
   *          shifts.insertElementAt(new int[] { pos, shift}, sidx); else
   *          rshift[1]+=shift; }
   */

  /**
   * shift from pos to To(pos)
   * 
   * @param pos
   *          int
   * @return int shifted position in To, frameshift in From, direction of mapped
   *         symbol in To
   */
  public int[] shiftFrom(int pos)
  {
    return shift(pos, fromShifts, fromRatio, toShifts, toRatio);
  }

  /**
   * inverse of shiftFrom - maps pos in To to a position in From
   * 
   * @param pos
   *          (in To)
   * @return shifted position in From, frameshift in To, direction of mapped
   *         symbol in From
   */
  public int[] shiftTo(int pos)
  {
    return shift(pos, toShifts, toRatio, fromShifts, fromRatio);
  }

  /**
   * 
   * @param shiftTo
   * @param fromRatio
   * @param shiftFrom
   * @param toRatio
   * @return
   */
  protected static int[] shift(int pos, List<int[]> shiftTo, int fromRatio,
          List<int[]> shiftFrom, int toRatio)
  {
    // TODO: javadoc; tests
    int[] fromCount = countPositions(shiftTo, pos);
    if (fromCount == null)
    {
      return null;
    }
    int fromRemainder = (fromCount[0] - 1) % fromRatio;
    int toCount = 1 + (((fromCount[0] - 1) / fromRatio) * toRatio);
    int[] toPos = traverseToPosition(shiftFrom, toCount);
    if (toPos == null)
    {
      return null;
    }
    return new int[] { toPos[0], fromRemainder, toPos[1] };
  }

  /**
   * Counts how many positions pos is along the series of intervals. Returns an
   * array of two values:
   * <ul>
   * <li>the number of positions traversed (inclusive) to reach {@code pos}</li>
   * <li>+1 if the last interval traversed is forward, -1 if in a negative
   * direction</li>
   * </ul>
   * Returns null if {@code pos} does not lie in any of the given intervals.
   * 
   * @param intervals
   *          a list of start-end intervals
   * @param pos
   *          a position that may lie in one (or more) of the intervals
   * @return
   */
  protected static int[] countPositions(List<int[]> intervals, int pos)
  {
    int count = 0;
    int iv = 0;
    int ivSize = intervals.size();

    while (iv < ivSize)
    {
      int[] intv = intervals.get(iv++);
      if (intv[0] <= intv[1])
      {
        /*
         * forwards interval
         */
        if (pos >= intv[0] && pos <= intv[1])
        {
          return new int[] { count + pos - intv[0] + 1, +1 };
        }
        else
        {
          count += intv[1] - intv[0] + 1;
        }
      }
      else
      {
        /*
         * reverse interval
         */
        if (pos >= intv[1] && pos <= intv[0])
        {
          return new int[] { count + intv[0] - pos + 1, -1 };
        }
        else
        {
          count += intv[0] - intv[1] + 1;
        }
      }
    }
    return null;
  }

  /**
   * Reads through the given intervals until {@code count} positions have been
   * traversed, and returns an array consisting of two values:
   * <ul>
   * <li>the value at the {@code count'th} position</li>
   * <li>+1 if the last interval read is forwards, -1 if reverse direction</li>
   * </ul>
   * Returns null if the ranges include less than {@code count} positions, or if
   * {@code count < 1}.
   * 
   * @param intervals
   *          a list of [start, end] ranges
   * @param count
   *          the number of positions to traverse
   * @return
   */
  protected static int[] traverseToPosition(List<int[]> intervals,
          final int count)
  {
    int traversed = 0;
    int ivSize = intervals.size();
    int iv = 0;

    if (count < 1)
    {
      return null;
    }

    while (iv < ivSize)
    {
      int[] intv = intervals.get(iv++);
      int diff = intv[1] - intv[0];
      if (diff >= 0)
      {
        if (count <= traversed + 1 + diff)
        {
          return new int[] { intv[0] + (count - traversed - 1), +1 };
        }
        else
        {
          traversed += 1 + diff;
        }
      }
      else
      {
        if (count <= traversed + 1 - diff)
        {
          return new int[] { intv[0] - (count - traversed - 1), -1 };
        }
        else
        {
          traversed += 1 - diff;
        }
      }
    }
    return null;
  }

  /**
   * like shift - except returns the intervals in the given vector of shifts
   * which were spanned in traversing fromStart to fromEnd
   * 
   * @param shiftFrom
   * @param fromStart
   * @param fromEnd
   * @param fromRatio2
   * @return series of from,to intervals from from first position of starting
   *         region to final position of ending region inclusive
   */
  protected static int[] getIntervals(List<int[]> shiftFrom,
          int[] fromStart, int[] fromEnd, int fromRatio2)
  {
    if (fromStart == null || fromEnd == null)
    {
      return null;
    }
    int startpos, endpos;
    startpos = fromStart[0]; // first position in fromStart
    endpos = fromEnd[0]; // last position in fromEnd
    int endindx = (fromRatio2 - 1); // additional positions to get to last
    // position from endpos
    int intv = 0, intvSize = shiftFrom.size();
    int iv[], i = 0, fs = -1, fe_s = -1, fe = -1; // containing intervals
    // search intervals to locate ones containing startpos and count endindx
    // positions on from endpos
    while (intv < intvSize && (fs == -1 || fe == -1))
    {
      iv = shiftFrom.get(intv++);
      if (fe_s > -1)
      {
        endpos = iv[0]; // start counting from beginning of interval
        endindx--; // inclusive of endpos
      }
      if (iv[0] <= iv[1])
      {
        if (fs == -1 && startpos >= iv[0] && startpos <= iv[1])
        {
          fs = i;
        }
        if (endpos >= iv[0] && endpos <= iv[1])
        {
          if (fe_s == -1)
          {
            fe_s = i;
          }
          if (fe_s != -1)
          {
            if (endpos + endindx <= iv[1])
            {
              fe = i;
              endpos = endpos + endindx; // end of end token is within this
              // interval
            }
            else
            {
              endindx -= iv[1] - endpos; // skip all this interval too
            }
          }
        }
      }
      else
      {
        if (fs == -1 && startpos <= iv[0] && startpos >= iv[1])
        {
          fs = i;
        }
        if (endpos <= iv[0] && endpos >= iv[1])
        {
          if (fe_s == -1)
          {
            fe_s = i;
          }
          if (fe_s != -1)
          {
            if (endpos - endindx >= iv[1])
            {
              fe = i;
              endpos = endpos - endindx; // end of end token is within this
              // interval
            }
            else
            {
              endindx -= endpos - iv[1]; // skip all this interval too
            }
          }
        }
      }
      i++;
    }
    if (fs == fe && fe == -1)
    {
      return null;
    }
    List<int[]> ranges = new ArrayList<>();
    if (fs <= fe)
    {
      intv = fs;
      i = fs;
      // truncate initial interval
      iv = shiftFrom.get(intv++);
      iv = new int[] { iv[0], iv[1] };// clone
      if (i == fs)
      {
        iv[0] = startpos;
      }
      while (i != fe)
      {
        ranges.add(iv); // add initial range
        iv = shiftFrom.get(intv++); // get next interval
        iv = new int[] { iv[0], iv[1] };// clone
        i++;
      }
      if (i == fe)
      {
        iv[1] = endpos;
      }
      ranges.add(iv); // add only - or final range
    }
    else
    {
      // walk from end of interval.
      i = shiftFrom.size() - 1;
      while (i > fs)
      {
        i--;
      }
      iv = shiftFrom.get(i);
      iv = new int[] { iv[1], iv[0] };// reverse and clone
      // truncate initial interval
      if (i == fs)
      {
        iv[0] = startpos;
      }
      while (--i != fe)
      { // fix apparent logic bug when fe==-1
        ranges.add(iv); // add (truncated) reversed interval
        iv = shiftFrom.get(i);
        iv = new int[] { iv[1], iv[0] }; // reverse and clone
      }
      if (i == fe)
      {
        // interval is already reversed
        iv[1] = endpos;
      }
      ranges.add(iv); // add only - or final range
    }
    // create array of start end intervals.
    int[] range = null;
    if (ranges != null && ranges.size() > 0)
    {
      range = new int[ranges.size() * 2];
      intv = 0;
      intvSize = ranges.size();
      i = 0;
      while (intv < intvSize)
      {
        iv = ranges.get(intv);
        range[i++] = iv[0];
        range[i++] = iv[1];
        ranges.set(intv++, null); // remove
      }
    }
    return range;
  }

  /**
   * get the 'initial' position of mpos in To
   * 
   * @param mpos
   *          position in from
   * @return position of first word in to reference frame
   */
  public int getToPosition(int mpos)
  {
    int[] mp = shiftTo(mpos);
    if (mp != null)
    {
      return mp[0];
    }
    return mpos;
  }

  /**
   * 
   * @return a MapList whose From range is this maplist's To Range, and vice
   *         versa
   */
  public MapList getInverse()
  {
    return new MapList(getToRanges(), getFromRanges(), getToRatio(),
            getFromRatio());
  }

  /**
   * String representation - for debugging, not guaranteed not to change
   */
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder(64);
    sb.append("[");
    for (int[] shift : fromShifts)
    {
      sb.append(" ").append(Arrays.toString(shift));
    }
    sb.append(" ] ");
    sb.append(fromRatio).append(":").append(toRatio);
    sb.append(" to [");
    for (int[] shift : toShifts)
    {
      sb.append(" ").append(Arrays.toString(shift));
    }
    sb.append(" ]");
    return sb.toString();
  }

  /**
   * Extend this map list by adding the given map's ranges. There is no
   * validation check that the ranges do not overlap existing ranges (or each
   * other), but contiguous ranges are merged.
   * 
   * @param map
   */
  public void addMapList(MapList map)
  {
    if (this.equals(map))
    {
      return;
    }
    this.fromLowest = Math.min(fromLowest, map.fromLowest);
    this.toLowest = Math.min(toLowest, map.toLowest);
    this.fromHighest = Math.max(fromHighest, map.fromHighest);
    this.toHighest = Math.max(toHighest, map.toHighest);

    for (int[] range : map.getFromRanges())
    {
      addRange(range, fromShifts);
    }
    for (int[] range : map.getToRanges())
    {
      addRange(range, toShifts);
    }
  }

  /**
   * Adds the given range to a list of ranges. If the new range just extends
   * existing ranges, the current endpoint is updated instead.
   * 
   * @param range
   * @param addTo
   */
  static void addRange(int[] range, List<int[]> addTo)
  {
    /*
     * list is empty - add to it!
     */
    if (addTo.size() == 0)
    {
      addTo.add(range);
      return;
    }

    int[] last = addTo.get(addTo.size() - 1);
    boolean lastForward = last[1] >= last[0];
    boolean newForward = range[1] >= range[0];

    /*
     * contiguous range in the same direction - just update endpoint
     */
    if (lastForward == newForward && last[1] == range[0])
    {
      last[1] = range[1];
      return;
    }

    /*
     * next range starts at +1 in forward sense - update endpoint
     */
    if (lastForward && newForward && range[0] == last[1] + 1)
    {
      last[1] = range[1];
      return;
    }

    /*
     * next range starts at -1 in reverse sense - update endpoint
     */
    if (!lastForward && !newForward && range[0] == last[1] - 1)
    {
      last[1] = range[1];
      return;
    }

    /*
     * just add the new range
     */
    addTo.add(range);
  }

  /**
   * Returns true if mapping is from forward strand, false if from reverse
   * strand. Result is just based on the first 'from' range that is not a single
   * position. Default is true unless proven to be false. Behaviour is not well
   * defined if the mapping has a mixture of forward and reverse ranges.
   * 
   * @return
   */
  public boolean isFromForwardStrand()
  {
    return isForwardStrand(getFromRanges());
  }

  /**
   * Returns true if mapping is to forward strand, false if to reverse strand.
   * Result is just based on the first 'to' range that is not a single position.
   * Default is true unless proven to be false. Behaviour is not well defined if
   * the mapping has a mixture of forward and reverse ranges.
   * 
   * @return
   */
  public boolean isToForwardStrand()
  {
    return isForwardStrand(getToRanges());
  }

  /**
   * A helper method that returns true unless at least one range has start >
   * end. Behaviour is undefined for a mixture of forward and reverse ranges.
   * 
   * @param ranges
   * @return
   */
  private boolean isForwardStrand(List<int[]> ranges)
  {
    boolean forwardStrand = true;
    for (int[] range : ranges)
    {
      if (range[1] > range[0])
      {
        break; // forward strand confirmed
      }
      else if (range[1] < range[0])
      {
        forwardStrand = false;
        break; // reverse strand confirmed
      }
    }
    return forwardStrand;
  }

  /**
   * 
   * @return true if from, or to is a three to 1 mapping
   */
  public boolean isTripletMap()
  {
    return (toRatio == 3 && fromRatio == 1)
            || (fromRatio == 3 && toRatio == 1);
  }

  /**
   * Returns a map which is the composite of this one and the input map. That
   * is, the output map has the fromRanges of this map, and its toRanges are the
   * toRanges of this map as transformed by the input map.
   * <p>
   * Returns null if the mappings cannot be traversed (not all toRanges of this
   * map correspond to fromRanges of the input), or if this.toRatio does not
   * match map.fromRatio.
   * 
   * <pre>
   * Example 1:
   *    this:   from [1-100] to [501-600]
   *    input:  from [10-40] to [60-90]
   *    output: from [10-40] to [560-590]
   * Example 2 ('reverse strand exons'):
   *    this:   from [1-100] to [2000-1951], [1000-951] // transcript to loci
   *    input:  from [1-50]  to [41-90] // CDS to transcript
   *    output: from [10-40] to [1960-1951], [1000-971] // CDS to gene loci
   * </pre>
   * 
   * @param map
   * @return
   */
  public MapList traverse(MapList map)
  {
    if (map == null)
    {
      return null;
    }

    /*
     * compound the ratios by this rule:
     * A:B with M:N gives A*M:B*N
     * reduced by greatest common divisor
     * so 1:3 with 3:3 is 3:9 or 1:3
     * 1:3 with 3:1 is 3:3 or 1:1
     * 1:3 with 1:3 is 1:9
     * 2:5 with 3:7 is 6:35
     */
    int outFromRatio = getFromRatio() * map.getFromRatio();
    int outToRatio = getToRatio() * map.getToRatio();
    int gcd = MathUtils.gcd(outFromRatio, outToRatio);
    outFromRatio /= gcd;
    outToRatio /= gcd;

    List<int[]> toRanges = new ArrayList<>();
    for (int[] range : getToRanges())
    {
      int fromLength = Math.abs(range[1] - range[0]) + 1;
      int[] transferred = map.locateInTo(range[0], range[1]);
      if (transferred == null || transferred.length % 2 != 0)
      {
        return null;
      }

      /*
       *  convert [start1, end1, start2, end2, ...] 
       *  to [[start1, end1], [start2, end2], ...]
       */
      int toLength = 0;
      for (int i = 0; i < transferred.length;)
      {
        toRanges.add(new int[] { transferred[i], transferred[i + 1] });
        toLength += Math.abs(transferred[i + 1] - transferred[i]) + 1;
        i += 2;
      }

      /*
       * check we mapped the full range - if not, abort
       */
      if (fromLength * map.getToRatio() != toLength * map.getFromRatio())
      {
        return null;
      }
    }

    return new MapList(getFromRanges(), toRanges, outFromRatio, outToRatio);
  }

  /**
   * Answers true if the mapping is from one contiguous range to another, else
   * false
   * 
   * @return
   */
  public boolean isContiguous()
  {
    return fromShifts.size() == 1 && toShifts.size() == 1;
  }

  /**
   * <<<<<<< HEAD Returns the [start1, end1, start2, end2, ...] positions in the
   * 'from' range that map to positions between {@code start} and {@code end} in
   * the 'to' range. Note that for a reverse strand mapping this will return
   * ranges with end < start. Returns null if no mapped positions are found in
   * start-end.
   * 
   * @param start
   * @param end
   * @return
   */
  public int[] locateInFrom(int start, int end)
  {
    return mapPositions(start, end, toShifts, fromShifts, toRatio,
            fromRatio);
  }

  /**
   * Returns the [start1, end1, start2, end2, ...] positions in the 'to' range
   * that map to positions between {@code start} and {@code end} in the 'from'
   * range. Note that for a reverse strand mapping this will return ranges with
   * end < start. Returns null if no mapped positions are found in start-end.
   * 
   * @param start
   * @param end
   * @return
   */
  public int[] locateInTo(int start, int end)
  {
    return mapPositions(start, end, fromShifts, toShifts, fromRatio,
            toRatio);
  }

  /**
   * Helper method that returns the [start1, end1, start2, end2, ...] positions
   * in {@code targetRange} that map to positions between {@code start} and
   * {@code end} in {@code sourceRange}. Note that for a reverse strand mapping
   * this will return ranges with end < start. Returns null if no mapped
   * positions are found in start-end.
   * 
   * @param start
   * @param end
   * @param sourceRange
   * @param targetRange
   * @param sourceWordLength
   * @param targetWordLength
   * @return
   */
  final static int[] mapPositions(int start, int end,
          List<int[]> sourceRange, List<int[]> targetRange,
          int sourceWordLength, int targetWordLength)
  {
    if (end < start)
    {
      int tmp = end;
      end = start;
      start = tmp;
    }

    /*
     * traverse sourceRange and mark offsets in targetRange 
     * of any positions that lie in [start, end]
     */
    BitSet offsets = getMappedOffsetsForPositions(start, end, sourceRange,
            sourceWordLength, targetWordLength);

    /*
     * traverse targetRange and collect positions at the marked offsets
     */
    List<int[]> mapped = getPositionsForOffsets(targetRange, offsets);

    // TODO: or just return the List and adjust calling code to match
    return mapped.isEmpty() ? null : MappingUtils.rangeListToArray(mapped);
  }

  /**
   * Scans the list of {@code ranges} for any values (positions) that lie
   * between start and end (inclusive), and records the <em>offsets</em> from
   * the start of the list as a BitSet. The offset positions are converted to
   * corresponding words in blocks of {@code wordLength2}.
   * 
   * <pre>
   * For example:
   * 1:1 (e.g. gene to CDS):
   * ranges { [10-20], [31-40] }, wordLengthFrom = wordLength 2 = 1
   *   for start = 1, end = 9, returns a BitSet with no bits set
   *   for start = 1, end = 11, returns a BitSet with bits 0-1 set
   *   for start = 15, end = 35, returns a BitSet with bits 5-15 set
   * 1:3 (peptide to codon):
   * ranges { [1-200] }, wordLengthFrom = 1, wordLength 2 = 3
   *   for start = 9, end = 9, returns a BitSet with bits 24-26 set
   * 3:1 (codon to peptide):
   * ranges { [101-150], [171-180] }, wordLengthFrom = 3, wordLength 2 = 1
   *   for start = 101, end = 102 (partial first codon), returns a BitSet with bit 0 set
   *   for start = 150, end = 171 (partial 17th codon), returns a BitSet with bit 16 set
   * 3:1 (circular DNA to peptide):
   * ranges { [101-150], [21-30] }, wordLengthFrom = 3, wordLength 2 = 1
   *   for start = 24, end = 40 (spans codons 18-20), returns a BitSet with bits 17-19 set
   * </pre>
   * 
   * @param start
   * @param end
   * @param sourceRange
   * @param sourceWordLength
   * @param targetWordLength
   * @return
   */
  protected final static BitSet getMappedOffsetsForPositions(int start,
          int end, List<int[]> sourceRange, int sourceWordLength,
          int targetWordLength)
  {
    BitSet overlaps = new BitSet();
    int offset = 0;
    final int s1 = sourceRange.size();
    for (int i = 0; i < s1; i++)
    {
      int[] range = sourceRange.get(i);
      final int offset1 = offset;
      int overlapStartOffset = -1;
      int overlapEndOffset = -1;

      if (range[1] >= range[0])
      {
        /*
         * forward direction range
         */
        if (start <= range[1] && end >= range[0])
        {
          /*
           * overlap
           */
          int overlapStart = Math.max(start, range[0]);
          overlapStartOffset = offset1 + overlapStart - range[0];
          int overlapEnd = Math.min(end, range[1]);
          overlapEndOffset = offset1 + overlapEnd - range[0];
        }
      }
      else
      {
        /*
         * reverse direction range
         */
        if (start <= range[0] && end >= range[1])
        {
          /*
           * overlap
           */
          int overlapStart = Math.max(start, range[1]);
          int overlapEnd = Math.min(end, range[0]);
          overlapStartOffset = offset1 + range[0] - overlapEnd;
          overlapEndOffset = offset1 + range[0] - overlapStart;
        }
      }

      if (overlapStartOffset > -1)
      {
        /*
         * found an overlap
         */
        if (sourceWordLength != targetWordLength)
        {
          /*
           * convert any overlap found to whole words in the target range
           * (e.g. treat any partial codon overlap as if the whole codon)
           */
          overlapStartOffset -= overlapStartOffset % sourceWordLength;
          overlapStartOffset = overlapStartOffset / sourceWordLength
                  * targetWordLength;

          /*
           * similar calculation for range end, adding 
           * (wordLength2 - 1) for end of mapped word
           */
          overlapEndOffset -= overlapEndOffset % sourceWordLength;
          overlapEndOffset = overlapEndOffset / sourceWordLength
                  * targetWordLength;
          overlapEndOffset += targetWordLength - 1;
        }
        overlaps.set(overlapStartOffset, overlapEndOffset + 1);
      }
      offset += 1 + Math.abs(range[1] - range[0]);
    }
    return overlaps;
  }

  /**
   * Returns a (possibly empty) list of the [start-end] values (positions) at
   * offsets in the {@code targetRange} list that are marked by 'on' bits in the
   * {@code offsets} bitset.
   * 
   * @param targetRange
   * @param offsets
   * @return
   */
  protected final static List<int[]> getPositionsForOffsets(
          List<int[]> targetRange, BitSet offsets)
  {
    List<int[]> mapped = new ArrayList<>();
    if (offsets.isEmpty())
    {
      return mapped;
    }

    /*
     * count of positions preceding ranges[i]
     */
    int traversed = 0;

    /*
     * for each [from-to] range in ranges:
     * - find subranges (if any) at marked offsets
     * - add the start-end values at the marked positions
     */
    final int toAdd = offsets.cardinality();
    int added = 0;
    final int s2 = targetRange.size();
    for (int i = 0; added < toAdd && i < s2; i++)
    {
      int[] range = targetRange.get(i);
      added += addOffsetPositions(mapped, traversed, range, offsets);
      traversed += Math.abs(range[1] - range[0]) + 1;
    }
    return mapped;
  }

  /**
   * Helper method that adds any start-end subranges of {@code range} that are
   * at offsets in {@code range} marked by set bits in overlaps.
   * {@code mapOffset} is added to {@code range} offset positions. Returns the
   * count of positions added.
   * 
   * @param mapped
   * @param mapOffset
   * @param range
   * @param overlaps
   * @return
   */
  final static int addOffsetPositions(List<int[]> mapped,
          final int mapOffset, final int[] range, final BitSet overlaps)
  {
    final int rangeLength = 1 + Math.abs(range[1] - range[0]);
    final int step = range[1] < range[0] ? -1 : 1;
    int offsetStart = 0; // offset into range
    int added = 0;

    while (offsetStart < rangeLength)
    {
      /*
       * find the start of the next marked overlap offset;
       * if there is none, or it is beyond range, then finished
       */
      int overlapStart = overlaps.nextSetBit(mapOffset + offsetStart);
      if (overlapStart == -1 || overlapStart - mapOffset >= rangeLength)
      {
        /*
         * no more overlaps, or no more within range[]
         */
        return added;
      }
      overlapStart -= mapOffset;

      /*
       * end of the overlap range is just before the next clear bit;
       * restrict it to end of range if necessary;
       * note we may add a reverse strand range here (end < start)
       */
      int overlapEnd = overlaps.nextClearBit(mapOffset + overlapStart + 1);
      overlapEnd = (overlapEnd == -1) ? rangeLength - 1
              : Math.min(rangeLength - 1, overlapEnd - mapOffset - 1);
      int startPosition = range[0] + step * overlapStart;
      int endPosition = range[0] + step * overlapEnd;
      mapped.add(new int[] { startPosition, endPosition });
      offsetStart = overlapEnd + 1;
      added += Math.abs(endPosition - startPosition) + 1;
    }

    return added;
  }

  /*
   * Returns the [start, end...] positions in the range mapped from, that are
   * mapped to by part or all of the given begin-end of the range mapped to.
   * Returns null if begin-end does not overlap any position mapped to.
   * 
   * @param begin
   * @param end
   * @return
   */
  public int[] getOverlapsInFrom(final int begin, final int end)
  {
    int[] overlaps = MappingUtils.findOverlap(toShifts, begin, end);

    return overlaps == null ? null : locateInFrom(overlaps[0], overlaps[1]);
  }

  /**
   * Returns the [start, end...] positions in the range mapped to, that are
   * mapped to by part or all of the given begin-end of the range mapped from.
   * Returns null if begin-end does not overlap any position mapped from.
   * 
   * @param begin
   * @param end
   * @return
   */
  public int[] getOverlapsInTo(final int begin, final int end)
  {
    int[] overlaps = MappingUtils.findOverlap(fromShifts, begin, end);

    return overlaps == null ? null : locateInTo(overlaps[0], overlaps[1]);
  }
}
