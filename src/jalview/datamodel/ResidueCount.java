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
import jalview.util.Format;
import jalview.util.QuickSort;
import jalview.util.SparseCount;

/**
 * A class to count occurrences of residues in a profile, optimised for speed
 * and memory footprint.
 * 
 * @author gmcarstairs
 *
 */
public class ResidueCount
{
  /**
   * A data bean to hold the results of counting symbols
   */
  public class SymbolCounts
  {
    /**
     * the symbols seen (as char values), in no particular order
     */
    public final char[] symbols;

    /**
     * the counts for each symbol, in the same order as the symbols
     */
    public final int[] values;

    SymbolCounts(char[] s, int[] v)
    {
      symbols = s;
      values = v;
    }
  }

  private static final int TOUPPERCASE = 'A' - 'a';

  /*
   * nucleotide symbols to count (including N unknown)
   */
  private static final String NUCS = "ACGNTU";

  /*
   * amino acid symbols to count (including X unknown)
   * NB we also include U so as to support counting of RNA bases
   * in the "don't know" case of nucleotide / peptide
   */
  private static final String AAS = "ACDEFGHIKLMNPQRSTUVWXY";

  static final int GAP_COUNT = 0;

  /*
   * fast lookup tables holding the index into our count
   * arrays of each symbol; index 0 is reserved for gap counting
   */
  private static int[] NUC_INDEX = new int[26];

  private static int[] AA_INDEX = new int[26];
  static
  {
    for (int i = 0; i < NUCS.length(); i++)
    {
      NUC_INDEX[NUCS.charAt(i) - 'A'] = i + 1;
    }
    for (int i = 0; i < AAS.length(); i++)
    {
      AA_INDEX[AAS.charAt(i) - 'A'] = i + 1;
    }
  }

  /*
   * counts array, just big enough for the nucleotide or peptide
   * character set (plus gap counts in position 0)
   */
  private short[] counts;

  /*
   * alternative array of int counts for use if any count 
   * exceeds the maximum value of short (32767)
   */
  private int[] intCounts;

  /*
   * flag set if we switch from short to int counts
   */
  private boolean useIntCounts;

  /*
   * general-purpose counter, only for use for characters
   * that are not in the expected alphabet
   */
  private SparseCount otherData;

  /*
   * keeps track of the maximum count value recorded
   * (if this class ever allows decrements, would need to
   * calculate this on request instead) 
   */
  int maxCount;

  /*
   * if we think we are counting nucleotide, can get by with smaller
   * array to hold counts
   */
  private boolean isNucleotide;

  /**
   * Default constructor allocates arrays able to count either nucleotide or
   * peptide bases. Use this constructor if not sure which the data is.
   */
  public ResidueCount()
  {
    this(false);
  }

  /**
   * Constructor that allocates an array just big enough for the anticipated
   * characters, plus one position to count gaps
   */
  public ResidueCount(boolean nucleotide)
  {
    isNucleotide = nucleotide;
    int charsToCount = nucleotide ? NUCS.length() : AAS.length();
    counts = new short[charsToCount + 1];
  }

  /**
   * Increments the count for the given character. The supplied character may be
   * upper or lower case but counts are for the upper case only. Gap characters
   * (space, ., -) are all counted together.
   * 
   * @param c
   * @return the new value of the count for the character
   */
  public int add(final char c)
  {
    char u = toUpperCase(c);
    int newValue = 0;
    int offset = getOffset(u);

    /*
     * offset 0 is reserved for gap counting, so 0 here means either
     * an unexpected character, or a gap character passed in error
     */
    if (offset == 0)
    {
      if (Comparison.isGap(u))
      {
        newValue = addGap();
      }
      else
      {
        newValue = addOtherCharacter(u);
      }
    }
    else
    {
      newValue = increment(offset);
    }
    return newValue;
  }

  /**
   * Increment the count at the specified offset. If this would result in short
   * overflow, promote to counting int values instead.
   * 
   * @param offset
   * @return the new value of the count at this offset
   */
  int increment(int offset)
  {
    int newValue = 0;
    if (useIntCounts)
    {
      newValue = intCounts[offset];
      intCounts[offset] = ++newValue;
    }
    else
    {
      if (counts[offset] == Short.MAX_VALUE)
      {
        handleOverflow();
        newValue = intCounts[offset];
        intCounts[offset] = ++newValue;
      }
      else
      {
        newValue = counts[offset];
        counts[offset] = (short) ++newValue;
      }
    }

    if (offset != GAP_COUNT)
    {
      // update modal residue count
      maxCount = Math.max(maxCount, newValue);
    }
    return newValue;
  }

  /**
   * Switch from counting in short to counting in int
   */
  synchronized void handleOverflow()
  {
    intCounts = new int[counts.length];
    for (int i = 0; i < counts.length; i++)
    {
      intCounts[i] = counts[i];
    }
    counts = null;
    useIntCounts = true;
  }

  /**
   * Returns this character's offset in the count array
   * 
   * @param c
   * @return
   */
  int getOffset(char c)
  {
    int offset = 0;
    if ('A' <= c && c <= 'Z')
    {
      offset = isNucleotide ? NUC_INDEX[c - 'A'] : AA_INDEX[c - 'A'];
    }
    return offset;
  }

  /**
   * @param c
   * @return
   */
  protected char toUpperCase(final char c)
  {
    char u = c;
    if ('a' <= c && c <= 'z')
    {
      u = (char) (c + TOUPPERCASE);
    }
    return u;
  }

  /**
   * Increment count for some unanticipated character. The first time this
   * called, a SparseCount is instantiated to hold these 'extra' counts.
   * 
   * @param c
   * @return the new value of the count for the character
   */
  int addOtherCharacter(char c)
  {
    if (otherData == null)
    {
      otherData = new SparseCount();
    }
    int newValue = otherData.add(c, 1);
    maxCount = Math.max(maxCount, newValue);
    return newValue;
  }

  /**
   * Set count for some unanticipated character. The first time this called, a
   * SparseCount is instantiated to hold these 'extra' counts.
   * 
   * @param c
   * @param value
   */
  void setOtherCharacter(char c, int value)
  {
    if (otherData == null)
    {
      otherData = new SparseCount();
    }
    otherData.put(c, value);
  }

  /**
   * Increment count of gap characters
   * 
   * @return the new count of gaps
   */
  public int addGap()
  {
    int newValue = increment(GAP_COUNT);
    return newValue;
  }

  /**
   * Answers true if we are counting ints (only after overflow of short counts)
   * 
   * @return
   */
  boolean isCountingInts()
  {
    return useIntCounts;
  }

  /**
   * Sets the count for the given character. The supplied character may be upper
   * or lower case but counts are for the upper case only.
   * 
   * @param c
   * @param count
   */
  public void put(char c, int count)
  {
    char u = toUpperCase(c);
    int offset = getOffset(u);

    /*
     * offset 0 is reserved for gap counting, so 0 here means either
     * an unexpected character, or a gap character passed in error
     */
    if (offset == 0)
    {
      if (Comparison.isGap(u))
      {
        set(0, count);
      }
      else
      {
        setOtherCharacter(u, count);
        maxCount = Math.max(maxCount, count);
      }
    }
    else
    {
      set(offset, count);
      maxCount = Math.max(maxCount, count);
    }
  }

  /**
   * Sets the count at the specified offset. If this would result in short
   * overflow, promote to counting int values instead.
   * 
   * @param offset
   * @param value
   */
  void set(int offset, int value)
  {
    if (useIntCounts)
    {
      intCounts[offset] = value;
    }
    else
    {
      if (value > Short.MAX_VALUE || value < Short.MIN_VALUE)
      {
        handleOverflow();
        intCounts[offset] = value;
      }
      else
      {
        counts[offset] = (short) value;
      }
    }
  }

  /**
   * Returns the count for the given character, or zero if no count held
   * 
   * @param c
   * @return
   */
  public int getCount(char c)
  {
    char u = toUpperCase(c);
    int offset = getOffset(u);
    if (offset == 0)
    {
      if (!Comparison.isGap(u))
      {
        // should have called getGapCount()
        return otherData == null ? 0 : otherData.get(u);
      }
    }
    return useIntCounts ? intCounts[offset] : counts[offset];
  }

  public int getGapCount()
  {
    return useIntCounts ? intCounts[0] : counts[0];
  }

  /**
   * Answers true if this object wraps a counter for unexpected characters
   * 
   * @return
   */
  boolean isUsingOtherData()
  {
    return otherData != null;
  }

  /**
   * Returns the character (or concatenated characters) for the symbol(s) with
   * the given count in the profile. Can be used to get the modal residue by
   * supplying the modal count value. Returns an empty string if no symbol has
   * the given count. The symbols are in alphabetic order of standard peptide or
   * nucleotide characters, followed by 'other' symbols if any.
   * 
   * @return
   */
  public String getResiduesForCount(int count)
  {
    if (count == 0)
    {
      return "";
    }

    /*
     * find counts for the given value and append the
     * corresponding symbol
     */
    StringBuilder modal = new StringBuilder();
    if (useIntCounts)
    {
      for (int i = 1; i < intCounts.length; i++)
      {
        if (intCounts[i] == count)
        {
          modal.append(
                  isNucleotide ? NUCS.charAt(i - 1) : AAS.charAt(i - 1));
        }
      }
    }
    else
    {
      for (int i = 1; i < counts.length; i++)
      {
        if (counts[i] == count)
        {
          modal.append(
                  isNucleotide ? NUCS.charAt(i - 1) : AAS.charAt(i - 1));
        }
      }
    }
    if (otherData != null)
    {
      for (int i = 0; i < otherData.size(); i++)
      {
        if (otherData.valueAt(i) == count)
        {
          modal.append((char) otherData.keyAt(i));
        }
      }
    }
    return modal.toString();
  }

  /**
   * Returns the highest count for any symbol(s) in the profile (excluding gap)
   * 
   * @return
   */
  public int getModalCount()
  {
    return maxCount;
  }

  /**
   * Returns the number of distinct symbols with a non-zero count (excluding the
   * gap symbol)
   * 
   * @return
   */
  public int size()
  {
    int size = 0;
    if (useIntCounts)
    {
      for (int i = 1; i < intCounts.length; i++)
      {
        if (intCounts[i] > 0)
        {
          size++;
        }
      }
    }
    else
    {
      for (int i = 1; i < counts.length; i++)
      {
        if (counts[i] > 0)
        {
          size++;
        }
      }
    }

    /*
     * include 'other' characters recorded (even if count is zero
     * though that would be a strange use case)
     */
    if (otherData != null)
    {
      size += otherData.size();
    }

    return size;
  }

  /**
   * Returns a data bean holding those symbols that have a non-zero count
   * (excluding the gap symbol), with their counts.
   * 
   * @return
   */
  public SymbolCounts getSymbolCounts()
  {
    int size = size();
    char[] symbols = new char[size];
    int[] values = new int[size];
    int j = 0;

    if (useIntCounts)
    {
      for (int i = 1; i < intCounts.length; i++)
      {
        if (intCounts[i] > 0)
        {
          char symbol = isNucleotide ? NUCS.charAt(i - 1)
                  : AAS.charAt(i - 1);
          symbols[j] = symbol;
          values[j] = intCounts[i];
          j++;
        }
      }
    }
    else
    {
      for (int i = 1; i < counts.length; i++)
      {
        if (counts[i] > 0)
        {
          char symbol = isNucleotide ? NUCS.charAt(i - 1)
                  : AAS.charAt(i - 1);
          symbols[j] = symbol;
          values[j] = counts[i];
          j++;
        }
      }
    }
    if (otherData != null)
    {
      for (int i = 0; i < otherData.size(); i++)
      {
        symbols[j] = (char) otherData.keyAt(i);
        values[j] = otherData.valueAt(i);
        j++;
      }
    }

    return new SymbolCounts(symbols, values);
  }

  /**
   * Returns a tooltip string showing residues in descending order of their
   * percentage frequency in the profile
   * 
   * @param normaliseBy
   *          the divisor for residue counts (may or may not include gapped
   *          sequence count)
   * @param percentageDecPl
   *          the number of decimal places to show in percentages
   * @return
   */
  public String getTooltip(int normaliseBy, int percentageDecPl)
  {
    SymbolCounts symbolCounts = getSymbolCounts();
    char[] ca = symbolCounts.symbols;
    int[] vl = symbolCounts.values;

    /*
     * sort characters into ascending order of their counts
     */
    QuickSort.sort(vl, ca);

    /*
     * traverse in reverse order (highest count first) to build tooltip
     */
    boolean first = true;
    StringBuilder sb = new StringBuilder(64);
    for (int c = ca.length - 1; c >= 0; c--)
    {
      final char residue = ca[c];
      // TODO combine residues which share a percentage
      // (see AAFrequency.completeCdnaConsensus)
      float tval = (vl[c] * 100f) / normaliseBy;
      sb.append(first ? "" : "; ").append(residue).append(" ");
      Format.appendPercentage(sb, tval, percentageDecPl);
      sb.append("%");
      first = false;
    }
    return sb.toString();
  }

  /**
   * Returns a string representation of the symbol counts, for debug purposes.
   */
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("[ ");
    SymbolCounts sc = getSymbolCounts();
    for (int i = 0; i < sc.symbols.length; i++)
    {
      sb.append(sc.symbols[i]).append(":").append(sc.values[i]).append(" ");
    }
    sb.append("]");
    return sb.toString();
  }
}
