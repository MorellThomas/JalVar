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

import jalview.util.Comparison;
import jalview.util.MapList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

public class Mapping
{
  /**
   * An iterator that serves the aligned codon positions (with their protein
   * products).
   * 
   * @author gmcarstairs
   *
   */
  public class AlignedCodonIterator implements Iterator<AlignedCodon>
  {
    /*
     * The gap character used in the aligned sequence
     */
    private final char gap;

    /*
     * The characters of the aligned sequence e.g. "-cGT-ACgTG-"
     */
    private final SequenceI alignedSeq;

    /*
     * the sequence start residue
     */
    private int start;

    /*
     * Next position (base 0) in the aligned sequence
     */
    private int alignedColumn = 0;

    /*
     * Count of bases up to and including alignedColumn position
     */
    private int alignedBases = 0;

    /*
     * [start, end] from ranges (base 1)
     */
    private Iterator<int[]> fromRanges;

    /*
     * [start, end] to ranges (base 1)
     */
    private Iterator<int[]> toRanges;

    /*
     * The current [start, end] (base 1) from range
     */
    private int[] currentFromRange = null;

    /*
     * The current [start, end] (base 1) to range
     */
    private int[] currentToRange = null;

    /*
     * The next 'from' position (base 1) to process
     */
    private int fromPosition = 0;

    /*
     * The next 'to' position (base 1) to process
     */
    private int toPosition = 0;

    /**
     * Constructor
     * 
     * @param seq
     *          the aligned sequence
     * @param gapChar
     */
    public AlignedCodonIterator(SequenceI seq, char gapChar)
    {
      this.alignedSeq = seq;
      this.start = seq.getStart();
      this.gap = gapChar;
      fromRanges = map.getFromRanges().iterator();
      toRanges = map.getToRanges().iterator();
      if (fromRanges.hasNext())
      {
        currentFromRange = fromRanges.next();
        fromPosition = currentFromRange[0];
      }
      if (toRanges.hasNext())
      {
        currentToRange = toRanges.next();
        toPosition = currentToRange[0];
      }
    }

    /**
     * Returns true unless we have already traversed the whole mapping.
     */
    @Override
    public boolean hasNext()
    {
      if (fromRanges.hasNext())
      {
        return true;
      }
      if (currentFromRange == null || fromPosition >= currentFromRange[1])
      {
        return false;
      }
      return true;
    }

    /**
     * Returns the next codon's aligned positions, and translated value.
     * 
     * @throws NoSuchElementException
     *           if hasNext() would have returned false
     * @throws IncompleteCodonException
     *           if not enough mapped bases are left to make up a codon
     */
    @Override
    public AlignedCodon next() throws IncompleteCodonException
    {
      if (!hasNext())
      {
        throw new NoSuchElementException();
      }

      int[] codon = getNextCodon();
      int[] alignedCodon = getAlignedCodon(codon);

      String peptide = getPeptide();
      int peptideCol = toPosition - 1 - Mapping.this.to.getStart();
      return new AlignedCodon(alignedCodon[0], alignedCodon[1],
              alignedCodon[2], peptide, peptideCol);
    }

    /**
     * Retrieve the translation as the 'mapped to' position in the mapped to
     * sequence.
     * 
     * @return
     * @throws NoSuchElementException
     *           if the 'toRange' is exhausted (nothing to map to)
     */
    private String getPeptide()
    {
      // TODO should ideally handle toRatio other than 1 as well...
      // i.e. code like getNextCodon()
      if (toPosition <= currentToRange[1])
      {
        SequenceI seq = Mapping.this.to;
        char pep = seq.getCharAt(toPosition - seq.getStart());
        toPosition++;
        return String.valueOf(pep);
      }
      if (!toRanges.hasNext())
      {
        throw new NoSuchElementException(
                "Ran out of peptide at position " + toPosition);
      }
      currentToRange = toRanges.next();
      toPosition = currentToRange[0];
      return getPeptide();
    }

    /**
     * Get the (base 1) dataset positions for the next codon in the mapping.
     * 
     * @throws IncompleteCodonException
     *           if less than 3 remaining bases are mapped
     */
    private int[] getNextCodon()
    {
      int[] codon = new int[3];
      int codonbase = 0;

      while (codonbase < 3)
      {
        if (fromPosition <= currentFromRange[1])
        {
          /*
           * Add next position from the current start-end range
           */
          codon[codonbase++] = fromPosition++;
        }
        else
        {
          /*
           * Move to the next range - if there is one
           */
          if (!fromRanges.hasNext())
          {
            throw new IncompleteCodonException();
          }
          currentFromRange = fromRanges.next();
          fromPosition = currentFromRange[0];
        }
      }
      return codon;
    }

    /**
     * Get the aligned column positions (base 0) for the given sequence
     * positions (base 1), by counting ungapped characters in the aligned
     * sequence.
     * 
     * @param codon
     * @return
     */
    private int[] getAlignedCodon(int[] codon)
    {
      int[] aligned = new int[codon.length];
      for (int i = 0; i < codon.length; i++)
      {
        aligned[i] = getAlignedColumn(codon[i]);
      }
      return aligned;
    }

    /**
     * Get the aligned column position (base 0) for the given sequence position
     * (base 1).
     * 
     * @param sequencePos
     * @return
     */
    private int getAlignedColumn(int sequencePos)
    {
      /*
       * allow for offset e.g. treat pos 8 as 2 if sequence starts at 7
       */
      int truePos = sequencePos - (start - 1);
      int length = alignedSeq.getLength();
      while (alignedBases < truePos && alignedColumn < length)
      {
        char c = alignedSeq.getCharAt(alignedColumn++);
        if (c != gap && !Comparison.isGap(c))
        {
          alignedBases++;
        }
      }
      return alignedColumn - 1;
    }

    @Override
    public void remove()
    {
      // ignore
    }

  }

  /*
   * Contains the start-end pairs mapping from the associated sequence to the
   * sequence in the database coordinate system. It also takes care of step
   * difference between coordinate systems.
   */
  MapList map = null;

  /*
   * The sequence that map maps the associated sequence to (if any).
   */
  SequenceI to = null;

  /*
   * optional sequence id for the 'from' ranges
   */
  private String mappedFromId;

  public Mapping(MapList map)
  {
    super();
    this.map = map;
  }

  public Mapping(SequenceI to, MapList map)
  {
    this(map);
    this.to = to;
  }

  /**
   * create a new mapping from
   * 
   * @param to
   *          the sequence being mapped
   * @param exon
   *          int[] {start,end,start,end} series on associated sequence
   * @param is
   *          int[] {start,end,...} ranges on the reference frame being mapped
   *          to
   * @param i
   *          step size on associated sequence
   * @param j
   *          step size on mapped frame
   */
  public Mapping(SequenceI to, int[] exon, int[] is, int i, int j)
  {
    this(to, new MapList(exon, is, i, j));
  }

  /**
   * create a duplicate (and independent) mapping object with the same reference
   * to any SequenceI being mapped to.
   * 
   * @param map2
   */
  public Mapping(Mapping map2)
  {
    if (map2 != this && map2 != null)
    {
      if (map2.map != null)
      {
        map = new MapList(map2.map);
      }
      to = map2.to;
      mappedFromId = map2.mappedFromId;
    }
  }

  /**
   * @return the map
   */
  public MapList getMap()
  {
    return map;
  }

  /**
   * @param map
   *          the map to set
   */
  public void setMap(MapList map)
  {
    this.map = map;
  }

  /**
   * Equals that compares both the to references and MapList mappings.
   * 
   * @param o
   * @return
   * @see MapList#equals
   */
  @Override
  public boolean equals(Object o)
  {
    if (o == null || !(o instanceof Mapping))
    {
      return false;
    }
    Mapping other = (Mapping) o;
    if (other == this)
    {
      return true;
    }
    if (other.to != to)
    {
      return false;
    }
    if ((map != null && other.map == null)
            || (map == null && other.map != null))
    {
      return false;
    }
    if ((map == null && other.map == null) || map.equals(other.map))
    {
      return true;
    }
    return false;
  }

  /**
   * Returns a hashCode made from the sequence and maplist
   */
  @Override
  public int hashCode()
  {
    int hashCode = (this.to == null ? 1 : this.to.hashCode());
    if (this.map != null)
    {
      hashCode = hashCode * 31 + this.map.hashCode();
    }

    return hashCode;
  }

//  /**
//   * gets boundary in direction of mapping
//   * 
//   * @param position
//   *          in mapped reference frame
//   * @return int{start, end} positions in associated sequence (in direction of
//   *         mapped word)
//   */
//  public int[] getWord(int mpos)
//  {
//	  // BH never called
//    if (map != null)
//    {
//      return map.getToWord(mpos);
//    }
//    return null;
//  }

  /**
   * width of mapped unit in associated sequence
   * 
   */
  public int getWidth()
  {
    if (map != null)
    {
      return map.getFromRatio();
    }
    return 1;
  }

  /**
   * width of unit in mapped reference frame
   * 
   * @return
   */
  public int getMappedWidth()
  {
    if (map != null)
    {
      return map.getToRatio();
    }
    return 1;
  }

	/**
	 * get the 'initial' position in the associated sequence for a position in the
	 * mapped reference frame
	 * 
	 * or the mapped position in the associated reference frame for position pos in
	 * the associated sequence.
	 * 
	 * 
	 * @param reg      reg[POS]
	 * @param isMapped
	 * 
	 * @return position or mapped position
	 */
  public int getPosition(int[] reg, boolean isMapped)
  {
	int pos = reg[MapList.POS];
    if (map != null)
    {
      reg = (isMapped ? map.shiftFrom(reg) : map.shiftTo(reg));
      if (reg != null)
      {
          return reg[MapList.POS_TO]; // was newArray[0], but shift puts the result in COUNT_TO
      }
    }
    return pos;
  }

//  /**
//* get mapped position in the associated reference frame for position pos in
//* the associated sequence.
//   * 
//   * @param pos
//   * @return
//   */
//  public int getMappedPosition(int[] reg)
//  {
//	int mpos = reg[MapList.POS]; 
//    if (map != null)
//    {
//      reg = map.shiftFrom(reg);
//      if (reg != null)
//      {
//        return reg[MapList.POS_TO]; // was newArray[0], but shift puts the result in COUNT_TO
//      }
//    }
//    return mpos;
//  }

//  public int[] getMappedWord(int pos)
//  {
//	  // BH Not used? 
//    if (map != null)
//    {
//      reg = map.shiftFrom(reg);
//      if (reg != null)
//      {
//    	reg[MP_0] = 
//        return new int[] { mp[0], mp[0] + mp[2] * (map.getToRatio() - 1) };
//      }
//    }
//    return null;
//  }

  /**
   * locates the region of feature f in the associated sequence's reference
   * frame
   * 
   * @param f
   * @return one or more features corresponding to f
   */
  public SequenceFeature[] locateFeature(SequenceFeature f)
  {
    if (true)
    { // f.getBegin()!=f.getEnd()) {
      if (map != null)
      {
        int[] frange = map.locateInFrom(f.getBegin(), f.getEnd());
        if (frange == null)
        {
          // JBPNote - this isprobably not the right thing to doJBPHack
          return null;
        }
        SequenceFeature[] vf = new SequenceFeature[frange.length / 2];
        for (int i = 0, v = 0; i < frange.length; i += 2, v++)
        {
          vf[v] = new SequenceFeature(f, frange[i], frange[i + 1],
                  f.getFeatureGroup(), f.getScore());
          if (frange.length > 2)
          {
            vf[v].setDescription(f.getDescription() + "\nPart " + (v + 1));
          }
        }
        return vf;
      }
    }

    // give up and just return the feature.
    return new SequenceFeature[] { f };
  }

  /**
   * return a series of contigs on the associated sequence corresponding to the
   * from,to interval on the mapped reference frame
   * 
   * @param from
   * @param to
   * @return int[] { from_i, to_i for i=1 to n contiguous regions in the
   *         associated sequence}
   */
  public int[] locateRange(int from, int to)
  {
    if (map != null)
    {
      if (from <= to)
      {
        from = (map.getToLowest() < from) ? from : map.getToLowest();
        to = (map.getToHighest() > to) ? to : map.getToHighest();
        if (from > to)
        {
          return null;
        }
      }
      else
      {
        from = (map.getToHighest() > from) ? from : map.getToHighest();
        to = (map.getToLowest() < to) ? to : map.getToLowest();
        if (from < to)
        {
          return null;
        }
      }
      return map.locateInFrom(from, to);
    }
    return new int[] { from, to };
  }

  /**
   * return a series of mapped contigs mapped from a range on the associated
   * sequence
   * 
   * @param from
   * @param to
   * @return
   */
  public int[] locateMappedRange(int from, int to)
  {
    if (map != null)
    {

      if (from <= to)
      {
        from = (map.getFromLowest() < from) ? from : map.getFromLowest();
        to = (map.getFromHighest() > to) ? to : map.getFromHighest();
        if (from > to)
        {
          return null;
        }
      }
      else
      {
        from = (map.getFromHighest() > from) ? from : map.getFromHighest();
        to = (map.getFromLowest() < to) ? to : map.getFromLowest();
        if (from < to)
        {
          return null;
        }
      }
      return map.locateInTo(from, to);
    }
    return new int[] { from, to };
  }

  /**
   * return a new mapping object with a maplist modifed to only map the visible
   * regions defined by viscontigs.
   * 
   * @param viscontigs
   * @return
   */
  public Mapping intersectVisContigs(int[] viscontigs)
  {
    Mapping copy = new Mapping(this);
    if (map != null)
    {
//      int vpos = 0;
//      int apos = 0;
      List<int[]> toRange = new ArrayList<int[]>();
      List<int[]> fromRange = new ArrayList<int[]>();
      for (int vc = 0; vc < viscontigs.length; vc += 2)
      {
        // find a mapped range in this visible region
        int[] mpr = locateMappedRange(1 + viscontigs[vc],
                viscontigs[vc + 1] - 1);
        if (mpr != null)
        {
          for (int m = 0; m < mpr.length; m += 2)
          {
            toRange.add(new int[] { mpr[m], mpr[m + 1] });
            int[] xpos = locateRange(mpr[m], mpr[m + 1]);
            for (int x = 0; x < xpos.length; x += 2)
            {
              fromRange.add(new int[] { xpos[x], xpos[x + 1] });
            }
          }
        }
      }
      int[] from = new int[fromRange.size() * 2];
      int[] to = new int[toRange.size() * 2];
      int[] r;
      for (int f = 0, fSize = fromRange.size(); f < fSize; f++)
      {
        r = fromRange.get(f);
        from[f * 2] = r[0];
        from[f * 2 + 1] = r[1];
      }
      for (int f = 0, fSize = toRange.size(); f < fSize; f++)
      {
        r = toRange.get(f);
        to[f * 2] = r[0];
        to[f * 2 + 1] = r[1];
      }
      copy.setMap(
              new MapList(from, to, map.getFromRatio(), map.getToRatio()));
    }
    return copy;
  }

  /**
   * get the sequence being mapped to - if any
   * 
   * @return null or a dataset sequence
   */
  public SequenceI getTo()
  {
    return to;
  }

  /**
   * set the dataset sequence being mapped to if any
   * 
   * @param tto
   */
  public void setTo(SequenceI tto)
  {
    to = tto;
  }

  /**
   * Returns an iterator which can serve up the aligned codon column positions
   * and their corresponding peptide products
   * 
   * @param seq
   *          an aligned (i.e. possibly gapped) sequence
   * @param gapChar
   * @return
   */
  public Iterator<AlignedCodon> getCodonIterator(SequenceI seq,
          char gapChar)
  {
    return new AlignedCodonIterator(seq, gapChar);
  }

  /**
   * Readable representation for debugging only, not guaranteed not to change
   */
  @Override
  public String toString()
  {
    return String.format("%s %s", this.map.toString(),
            this.to == null ? "" : this.to.getName());
  }

  /**
   * Returns the identifier for the 'from' range sequence, or null if not set
   * 
   * @return
   */
  public String getMappedFromId()
  {
    return mappedFromId;
  }

  /**
   * Sets the identifier for the 'from' range sequence
   */
  public void setMappedFromId(String mappedFromId)
  {
    this.mappedFromId = mappedFromId;
  }

}
