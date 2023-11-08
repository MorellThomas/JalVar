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
import java.util.List;

import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.datamodel.SequenceI;

/**
 * Assorted methods for analysing or comparing sequences.
 */
public class Comparison
{
  private static final int EIGHTY_FIVE = 85;

  private static final int NUCLEOTIDE_COUNT_PERCENT;

  private static final int NUCLEOTIDE_COUNT_LONG_SEQUENCE_AMBIGUITY_PERCENT;

  private static final int NUCLEOTIDE_COUNT_SHORT_SEQUENCE;

  private static final int NUCLEOTIDE_COUNT_VERY_SHORT_SEQUENCE;

  private static final boolean NUCLEOTIDE_AMBIGUITY_DETECTION;

  public static final char GAP_SPACE = ' ';

  public static final char GAP_DOT = '.';

  public static final char GAP_DASH = '-';

  public static final String GapChars = new String(
          new char[]
          { GAP_SPACE, GAP_DOT, GAP_DASH });

  static
  {
    // these options read only at start of session
    NUCLEOTIDE_COUNT_PERCENT = Cache.getDefault("NUCLEOTIDE_COUNT_PERCENT",
            55);
    NUCLEOTIDE_COUNT_LONG_SEQUENCE_AMBIGUITY_PERCENT = Cache.getDefault(
            "NUCLEOTIDE_COUNT_LONG_SEQUENCE_AMBIGUITY_PERCENT", 95);
    NUCLEOTIDE_COUNT_SHORT_SEQUENCE = Cache
            .getDefault("NUCLEOTIDE_COUNT_SHORT", 100);
    NUCLEOTIDE_COUNT_VERY_SHORT_SEQUENCE = Cache
            .getDefault("NUCLEOTIDE_COUNT_VERY_SHORT", 4);
    NUCLEOTIDE_AMBIGUITY_DETECTION = Cache
            .getDefault("NUCLEOTIDE_AMBIGUITY_DETECTION", true);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param ii
   *          DOCUMENT ME!
   * @param jj
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public static final float compare(SequenceI ii, SequenceI jj)
  {
    return Comparison.compare(ii, jj, 0, ii.getLength() - 1);
  }

  /**
   * this was supposed to be an ungapped pid calculation
   * 
   * @param ii
   *          SequenceI
   * @param jj
   *          SequenceI
   * @param start
   *          int
   * @param end
   *          int
   * @return float
   */
  public static float compare(SequenceI ii, SequenceI jj, int start,
          int end)
  {
    String si = ii.getSequenceAsString();
    String sj = jj.getSequenceAsString();

    int ilen = si.length() - 1;
    int jlen = sj.length() - 1;

    while (Comparison.isGap(si.charAt(start + ilen)))
    {
      ilen--;
    }

    while (Comparison.isGap(sj.charAt(start + jlen)))
    {
      jlen--;
    }

    int match = 0;
    float pid = -1;

    if (ilen > jlen)
    {
      for (int j = 0; j < jlen; j++)
      {
        if (si.substring(start + j, start + j + 1)
                .equals(sj.substring(start + j, start + j + 1)))
        {
          match++;
        }
      }

      pid = (float) match / (float) ilen * 100;
    }
    else
    {
      for (int j = 0; j < jlen; j++)
      {
        if (si.substring(start + j, start + j + 1)
                .equals(sj.substring(start + j, start + j + 1)))
        {
          match++;
        }
      }

      pid = (float) match / (float) jlen * 100;
    }

    return pid;
  }

  /**
   * this is a gapped PID calculation
   * 
   * @param s1
   *          SequenceI
   * @param s2
   *          SequenceI
   * @return float
   * @deprecated use PIDModel.computePID()
   */
  @Deprecated
  public final static float PID(String seq1, String seq2)
  {
    return PID(seq1, seq2, 0, seq1.length());
  }

  static final int caseShift = 'a' - 'A';

  // Another pid with region specification
  /**
   * @deprecated use PIDModel.computePID()
   */
  @Deprecated
  public final static float PID(String seq1, String seq2, int start,
          int end)
  {
    return PID(seq1, seq2, start, end, true, false);
  }

  /**
   * Calculate percent identity for a pair of sequences over a particular range,
   * with different options for ignoring gaps.
   * 
   * @param seq1
   * @param seq2
   * @param start
   *          - position in seqs
   * @param end
   *          - position in seqs
   * @param wcGaps
   *          - if true - gaps match any character, if false, do not match
   *          anything
   * @param ungappedOnly
   *          - if true - only count PID over ungapped columns
   * @return
   * @deprecated use PIDModel.computePID()
   */
  @Deprecated
  public final static float PID(String seq1, String seq2, int start,
          int end, boolean wcGaps, boolean ungappedOnly)
  {
    int s1len = seq1.length();
    int s2len = seq2.length();

    int len = Math.min(s1len, s2len);

    if (end < len)
    {
      len = end;
    }

    if (len < start)
    {
      start = len - 1; // we just use a single residue for the difference
    }

    int elen = len - start, bad = 0;
    char chr1;
    char chr2;
    boolean agap;
    for (int i = start; i < len; i++)
    {
      chr1 = seq1.charAt(i);

      chr2 = seq2.charAt(i);
      agap = isGap(chr1) || isGap(chr2);
      if ('a' <= chr1 && chr1 <= 'z')
      {
        // TO UPPERCASE !!!
        // Faster than toUpperCase
        chr1 -= caseShift;
      }
      if ('a' <= chr2 && chr2 <= 'z')
      {
        // TO UPPERCASE !!!
        // Faster than toUpperCase
        chr2 -= caseShift;
      }

      if (chr1 != chr2)
      {
        if (agap)
        {
          if (ungappedOnly)
          {
            elen--;
          }
          else if (!wcGaps)
          {
            bad++;
          }
        }
        else
        {
          bad++;
        }
      }

    }
    if (elen < 1)
    {
      return 0f;
    }
    return ((float) 100 * (elen - bad)) / elen;
  }

  /**
   * Answers true if the supplied character is a recognised gap character, else
   * false. Currently hard-coded to recognise '-', '-' or ' ' (hyphen / dot /
   * space).
   * 
   * @param c
   * 
   * @return
   */
  public static final boolean isGap(char c)
  {
    return c == GAP_DASH || c == GAP_DOT || c == GAP_SPACE;
  }

  /**
   * Overloaded method signature to test whether a single sequence is nucleotide
   * (that is, more than 85% CGTAUNX)
   * 
   * @param seq
   * @return
   */
  public static final boolean isNucleotide(SequenceI seq)
  {
    if (seq == null || seq.getLength() == 0)
    {
      return false;
    }
    long ntCount = 0; // nucleotide symbol count (does not include ntaCount)
    long aaCount = 0; // non-nucleotide, non-gap symbol count (includes nCount
                      // and ntaCount)
    long nCount = 0; // "Unknown" (N) symbol count
    long xCount = 0; // Also used as "Unknown" (X) symbol count
    long ntaCount = 0; // nucleotide ambiguity symbol count

    int len = seq.getLength();
    for (int i = 0; i < len; i++)
    {
      char c = seq.getCharAt(i);
      if (isNucleotide(c))
      {
        ntCount++;
      }
      else if (!isGap(c))
      {
        aaCount++;
        if (isN(c))
        {
          nCount++;
        }
        else
        {
          if (isX(c))
          {
            xCount++;
          }
          if (isNucleotideAmbiguity(c))
          {
            ntaCount++;
          }
        }
      }
    }
    long allCount = ntCount + aaCount;

    if (NUCLEOTIDE_AMBIGUITY_DETECTION)
    {
      Console.debug("Performing new nucleotide detection routine");
      if (allCount > NUCLEOTIDE_COUNT_SHORT_SEQUENCE)
      {
        // a long sequence.
        // check for at least 55% nucleotide, and nucleotide and ambiguity codes
        // (including N) must make up 95%
        return ntCount * 100 >= NUCLEOTIDE_COUNT_PERCENT * allCount
                && 100 * (ntCount + nCount
                        + ntaCount) >= NUCLEOTIDE_COUNT_LONG_SEQUENCE_AMBIGUITY_PERCENT
                                * allCount;
      }
      else if (allCount > NUCLEOTIDE_COUNT_VERY_SHORT_SEQUENCE)
      {
        // a short sequence.
        // check if a short sequence is at least 55% nucleotide and the rest of
        // the symbols are all X or all N
        if (ntCount * 100 >= NUCLEOTIDE_COUNT_PERCENT * allCount
                && (nCount == aaCount || xCount == aaCount))
        {
          return true;
        }

        // a short sequence.
        // check for at least x% nucleotide and all the rest nucleotide
        // ambiguity codes (including N), where x slides from 75% for sequences
        // of length 4 (i.e. only one non-nucleotide) to 55% for sequences of
        // length 100
        return myShortSequenceNucleotideProportionCount(ntCount, allCount)
                && nCount + ntaCount == aaCount;
      }
      else
      {
        // a very short sequence. (<4)
        // all bases must be nucleotide
        return ntCount > 0 && ntCount == allCount;
      }
    }
    else
    {
      Console.debug("Performing old nucleotide detection routine");
      /*
       * Check for nucleotide count > 85% of total count (in a form that evades
       * int / float conversion or divide by zero).
       */
      if ((ntCount + nCount) * 100 > EIGHTY_FIVE * allCount)
      {
        return ntCount > 0; // all N is considered protein. Could use a
                            // threshold here too
      }
    }
    return false;
  }

  protected static boolean myShortSequenceNucleotideProportionCount(
          long ntCount, long allCount)
  {
    /**
     * this method is valid only for NUCLEOTIDE_COUNT_VERY_SHORT_SEQUENCE <=
     * allCount <= NUCLEOTIDE_COUNT_SHORT_SEQUENCE
     */
    // the following is a simplified integer version of:
    //
    // a := allCount # the number of bases in the sequence
    // n : = ntCount # the number of definite nucleotide bases
    // vs := NUCLEOTIDE_COUNT_VERY_SHORT_SEQUENCE
    // s := NUCLEOTIDE_COUNT_SHORT_SEQUENCE
    // lp := NUCLEOTIDE_COUNT_LOWER_PERCENT
    // vsp := 1 - (1/a) # this is the proportion of required definite
    // nucleotides
    // # in a VERY_SHORT Sequence (4 bases).
    // # This should be equivalent to all but one base in the sequence.
    // p := (a - vs)/(s - vs) # proportion of the way between
    // # VERY_SHORT and SHORT thresholds.
    // tp := vsp + p * (lp/100 - vsp) # the proportion of definite nucleotides
    // # required for this length of sequence.
    // minNt := tp * a # the minimum number of definite nucleotide bases
    // # required for this length of sequence.
    //
    // We are then essentially returning:
    // # ntCount >= 55% of allCount and the rest are all nucleotide ambiguity:
    // ntCount >= tp * allCount && nCount + ntaCount == aaCount
    // but without going into float/double land
    long LHS = 100 * allCount
            * (NUCLEOTIDE_COUNT_SHORT_SEQUENCE
                    - NUCLEOTIDE_COUNT_VERY_SHORT_SEQUENCE)
            * (ntCount - allCount + 1);
    long RHS = allCount * (allCount - NUCLEOTIDE_COUNT_VERY_SHORT_SEQUENCE)
            * (allCount * NUCLEOTIDE_COUNT_PERCENT - 100 * allCount + 100);
    return LHS >= RHS;
  }

  /**
   * Answers true if more than 85% of the sequence residues (ignoring gaps) are
   * A, G, C, T or U, else false. This is just a heuristic guess and may give a
   * wrong answer (as AGCT are also amino acid codes).
   * 
   * @param seqs
   * @return
   */
  public static final boolean isNucleotide(SequenceI[] seqs)
  {
    if (seqs == null)
    {
      return false;
    }
    // true if we have seen a nucleotide sequence
    boolean na = false;
    for (SequenceI seq : seqs)
    {
      if (seq == null)
      {
        continue;
      }
      na = true;
      // TODO could possibly make an informed guess just from the first sequence
      // to save a lengthy calculation
      if (seq.isProtein())
      {
        // if even one looks like protein, the alignment is protein
        return false;
      }
    }
    return na;
  }

  /**
   * Answers true if the character is one of aAcCgGtTuU
   * 
   * @param c
   * @return
   */
  public static boolean isNucleotide(char c)
  {
    return isNucleotide(c, false);
  }

  /**
   * includeAmbiguity = true also includes Nucleotide Ambiguity codes
   */
  public static boolean isNucleotide(char c, boolean includeAmbiguity)
  {
    char C = Character.toUpperCase(c);
    switch (C)
    {
    case 'A':
    case 'C':
    case 'G':
    case 'T':
    case 'U':
      return true;
    }
    if (includeAmbiguity)
    {
      boolean ambiguity = isNucleotideAmbiguity(C);
      if (ambiguity)
        return true;
    }
    return false;
  }

  /**
   * Tests *only* nucleotide ambiguity codes (and not definite nucleotide codes)
   */
  public static boolean isNucleotideAmbiguity(char c)
  {
    switch (Character.toUpperCase(c))
    {
    case 'I':
    case 'X':
    case 'R':
    case 'Y':
    case 'W':
    case 'S':
    case 'M':
    case 'K':
    case 'B':
    case 'H':
    case 'D':
    case 'V':
      return true;
    case 'N': // not counting N as nucleotide
    }
    return false;
  }

  public static boolean isN(char c)
  {
    return 'n' == Character.toLowerCase(c);
  }

  public static boolean isX(char c)
  {
    return 'x' == Character.toLowerCase(c);
  }

  /**
   * Answers true if every character in the string is one of aAcCgGtTuU, or
   * (optionally) a gap character (dot, dash, space), else false
   * 
   * @param s
   * @param allowGaps
   * @return
   */
  public static boolean isNucleotideSequence(String s, boolean allowGaps)
  {
    return isNucleotideSequence(s, allowGaps, false);
  }

  public static boolean isNucleotideSequence(String s, boolean allowGaps,
          boolean includeAmbiguous)
  {
    if (s == null)
    {
      return false;
    }
    for (int i = 0; i < s.length(); i++)
    {
      char c = s.charAt(i);
      if (!isNucleotide(c, includeAmbiguous))
      {
        if (!allowGaps || !isGap(c))
        {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Convenience overload of isNucleotide
   * 
   * @param seqs
   * @return
   */
  public static boolean isNucleotide(SequenceI[][] seqs)
  {
    if (seqs == null)
    {
      return false;
    }
    List<SequenceI> flattened = new ArrayList<SequenceI>();
    for (SequenceI[] ss : seqs)
    {
      for (SequenceI s : ss)
      {
        flattened.add(s);
      }
    }
    final SequenceI[] oneDArray = flattened
            .toArray(new SequenceI[flattened.size()]);
    return isNucleotide(oneDArray);
  }

  /**
   * Compares two residues either case sensitively or case insensitively
   * depending on the caseSensitive flag
   * 
   * @param c1
   *          first char
   * @param c2
   *          second char to compare with
   * @param caseSensitive
   *          if true comparison will be case sensitive otherwise its not
   * @return
   */
  public static boolean isSameResidue(char c1, char c2,
          boolean caseSensitive)
  {
    return caseSensitive ? c1 == c2
            : Character.toUpperCase(c1) == Character.toUpperCase(c2);
  }
}
