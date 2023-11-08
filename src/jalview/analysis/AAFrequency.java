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

import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.Profile;
import jalview.datamodel.ProfileI;
import jalview.datamodel.Profiles;
import jalview.datamodel.ProfilesI;
import jalview.datamodel.ResidueCount;
import jalview.datamodel.ResidueCount.SymbolCounts;
import jalview.datamodel.SequenceI;
import jalview.ext.android.SparseIntArray;
import jalview.util.Comparison;
import jalview.util.Format;
import jalview.util.MappingUtils;
import jalview.util.QuickSort;

import java.awt.Color;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

/**
 * Takes in a vector or array of sequences and column start and column end and
 * returns a new Hashtable[] of size maxSeqLength, if Hashtable not supplied.
 * This class is used extensively in calculating alignment colourschemes that
 * depend on the amount of conservation in each alignment column.
 * 
 * @author $author$
 * @version $Revision$
 */
public class AAFrequency
{
  public static final String PROFILE = "P";

  /*
   * Quick look-up of String value of char 'A' to 'Z'
   */
  private static final String[] CHARS = new String['Z' - 'A' + 1];

  static
  {
    for (char c = 'A'; c <= 'Z'; c++)
    {
      CHARS[c - 'A'] = String.valueOf(c);
    }
  }

  public static final ProfilesI calculate(List<SequenceI> list, int start,
          int end)
  {
    return calculate(list, start, end, false);
  }

  public static final ProfilesI calculate(List<SequenceI> sequences,
          int start, int end, boolean profile)
  {
    SequenceI[] seqs = new SequenceI[sequences.size()];
    int width = 0;
    synchronized (sequences)
    {
      for (int i = 0; i < sequences.size(); i++)
      {
        seqs[i] = sequences.get(i);
        int length = seqs[i].getLength();
        if (length > width)
        {
          width = length;
        }
      }

      if (end >= width)
      {
        end = width;
      }

      ProfilesI reply = calculate(seqs, width, start, end, profile);
      return reply;
    }
  }

  /**
   * Calculate the consensus symbol(s) for each column in the given range.
   * 
   * @param sequences
   * @param width
   *          the full width of the alignment
   * @param start
   *          start column (inclusive, base zero)
   * @param end
   *          end column (exclusive)
   * @param saveFullProfile
   *          if true, store all symbol counts
   */
  public static final ProfilesI calculate(final SequenceI[] sequences,
          int width, int start, int end, boolean saveFullProfile)
  {
    // long now = System.currentTimeMillis();
    int seqCount = sequences.length;
    boolean nucleotide = false;
    int nucleotideCount = 0;
    int peptideCount = 0;

    ProfileI[] result = new ProfileI[width];

    for (int column = start; column < end; column++)
    {
      /*
       * Apply a heuristic to detect nucleotide data (which can
       * be counted in more compact arrays); here we test for
       * more than 90% nucleotide; recheck every 10 columns in case
       * of misleading data e.g. highly conserved Alanine in peptide!
       * Mistakenly guessing nucleotide has a small performance cost,
       * as it will result in counting in sparse arrays.
       * Mistakenly guessing peptide has a small space cost, 
       * as it will use a larger than necessary array to hold counts. 
       */
      if (nucleotideCount > 100 && column % 10 == 0)
      {
        nucleotide = (9 * peptideCount < nucleotideCount);
      }
      ResidueCount residueCounts = new ResidueCount(nucleotide);

      for (int row = 0; row < seqCount; row++)
      {
        if (sequences[row] == null)
        {
          System.err.println(
                  "WARNING: Consensus skipping null sequence - possible race condition.");
          continue;
        }
        if (sequences[row].getLength() > column)
        {
          char c = sequences[row].getCharAt(column);
          residueCounts.add(c);
          if (Comparison.isNucleotide(c))
          {
            nucleotideCount++;
          }
          else if (!Comparison.isGap(c))
          {
            peptideCount++;
          }
        }
        else
        {
          /*
           * count a gap if the sequence doesn't reach this column
           */
          residueCounts.addGap();
        }
      }

      int maxCount = residueCounts.getModalCount();
      String maxResidue = residueCounts.getResiduesForCount(maxCount);
      int gapCount = residueCounts.getGapCount();
      ProfileI profile = new Profile(seqCount, gapCount, maxCount,
              maxResidue);

      if (saveFullProfile)
      {
        profile.setCounts(residueCounts);
      }

      result[column] = profile;
    }
    return new Profiles(result);
    // long elapsed = System.currentTimeMillis() - now;
    // System.out.println(elapsed);
  }

  /**
   * Make an estimate of the profile size we are going to compute i.e. how many
   * different characters may be present in it. Overestimating has a cost of
   * using more memory than necessary. Underestimating has a cost of needing to
   * extend the SparseIntArray holding the profile counts.
   * 
   * @param profileSizes
   *          counts of sizes of profiles so far encountered
   * @return
   */
  static int estimateProfileSize(SparseIntArray profileSizes)
  {
    if (profileSizes.size() == 0)
    {
      return 4;
    }

    /*
     * could do a statistical heuristic here e.g. 75%ile
     * for now just return the largest value
     */
    return profileSizes.keyAt(profileSizes.size() - 1);
  }

  /**
   * Derive the consensus annotations to be added to the alignment for display.
   * This does not recompute the raw data, but may be called on a change in
   * display options, such as 'ignore gaps', which may in turn result in a
   * change in the derived values.
   * 
   * @param consensus
   *          the annotation row to add annotations to
   * @param profiles
   *          the source consensus data
   * @param startCol
   *          start column (inclusive)
   * @param endCol
   *          end column (exclusive)
   * @param ignoreGaps
   *          if true, normalise residue percentages ignoring gaps
   * @param showSequenceLogo
   *          if true include all consensus symbols, else just show modal
   *          residue
   * @param nseq
   *          number of sequences
   */
  public static void completeConsensus(AlignmentAnnotation consensus,
          ProfilesI profiles, int startCol, int endCol, boolean ignoreGaps,
          boolean showSequenceLogo, long nseq)
  {
    // long now = System.currentTimeMillis();
    if (consensus == null || consensus.annotations == null
            || consensus.annotations.length < endCol)
    {
      /*
       * called with a bad alignment annotation row 
       * wait for it to be initialised properly
       */
      return;
    }

    for (int i = startCol; i < endCol; i++)
    {
      ProfileI profile = profiles.get(i);
      if (profile == null)
      {
        /*
         * happens if sequences calculated over were 
         * shorter than alignment width
         */
        consensus.annotations[i] = null;
        return;
      }

      final int dp = getPercentageDp(nseq);

      float value = profile.getPercentageIdentity(ignoreGaps);

      String description = getTooltip(profile, value, showSequenceLogo,
              ignoreGaps, dp);

      String modalResidue = profile.getModalResidue();
      if ("".equals(modalResidue))
      {
        modalResidue = "-";
      }
      else if (modalResidue.length() > 1)
      {
        modalResidue = "+";
      }
      consensus.annotations[i] = new Annotation(modalResidue, description,
              ' ', value);
    }
    // long elapsed = System.currentTimeMillis() - now;
    // System.out.println(-elapsed);
  }

  /**
   * Derive the gap count annotation row.
   * 
   * @param gaprow
   *          the annotation row to add annotations to
   * @param profiles
   *          the source consensus data
   * @param startCol
   *          start column (inclusive)
   * @param endCol
   *          end column (exclusive)
   */
  public static void completeGapAnnot(AlignmentAnnotation gaprow,
          ProfilesI profiles, int startCol, int endCol, long nseq)
  {
    if (gaprow == null || gaprow.annotations == null
            || gaprow.annotations.length < endCol)
    {
      /*
       * called with a bad alignment annotation row 
       * wait for it to be initialised properly
       */
      return;
    }
    // always set ranges again
    gaprow.graphMax = nseq;
    gaprow.graphMin = 0;
    double scale = 0.8 / nseq;
    for (int i = startCol; i < endCol; i++)
    {
      ProfileI profile = profiles.get(i);
      if (profile == null)
      {
        /*
         * happens if sequences calculated over were 
         * shorter than alignment width
         */
        gaprow.annotations[i] = null;
        return;
      }

      final int gapped = profile.getNonGapped();

      String description = "" + gapped;

      gaprow.annotations[i] = new Annotation("", description, '\0', gapped,
              jalview.util.ColorUtils.bleachColour(Color.DARK_GRAY,
                      (float) scale * gapped));
    }
  }

  /**
   * Returns a tooltip showing either
   * <ul>
   * <li>the full profile (percentages of all residues present), if
   * showSequenceLogo is true, or</li>
   * <li>just the modal (most common) residue(s), if showSequenceLogo is
   * false</li>
   * </ul>
   * Percentages are as a fraction of all sequence, or only ungapped sequences
   * if ignoreGaps is true.
   * 
   * @param profile
   * @param pid
   * @param showSequenceLogo
   * @param ignoreGaps
   * @param dp
   *          the number of decimal places to format percentages to
   * @return
   */
  static String getTooltip(ProfileI profile, float pid,
          boolean showSequenceLogo, boolean ignoreGaps, int dp)
  {
    ResidueCount counts = profile.getCounts();

    String description = null;
    if (counts != null && showSequenceLogo)
    {
      int normaliseBy = ignoreGaps ? profile.getNonGapped()
              : profile.getHeight();
      description = counts.getTooltip(normaliseBy, dp);
    }
    else
    {
      StringBuilder sb = new StringBuilder(64);
      String maxRes = profile.getModalResidue();
      if (maxRes.length() > 1)
      {
        sb.append("[").append(maxRes).append("]");
      }
      else
      {
        sb.append(maxRes);
      }
      if (maxRes.length() > 0)
      {
        sb.append(" ");
        Format.appendPercentage(sb, pid, dp);
        sb.append("%");
      }
      description = sb.toString();
    }
    return description;
  }

  /**
   * Returns the sorted profile for the given consensus data. The returned array
   * contains
   * 
   * <pre>
   *    [profileType, numberOfValues, totalPercent, charValue1, percentage1, charValue2, percentage2, ...]
   * in descending order of percentage value
   * </pre>
   * 
   * @param profile
   *          the data object from which to extract and sort values
   * @param ignoreGaps
   *          if true, only non-gapped values are included in percentage
   *          calculations
   * @return
   */
  public static int[] extractProfile(ProfileI profile, boolean ignoreGaps)
  {
    ResidueCount counts = profile.getCounts();
    if (counts == null)
    {
      return null;
    }

    SymbolCounts symbolCounts = counts.getSymbolCounts();
    char[] symbols = symbolCounts.symbols;
    int[] values = symbolCounts.values;
    QuickSort.sort(values, symbols);
    int totalPercentage = 0;
    final int divisor = ignoreGaps ? profile.getNonGapped()
            : profile.getHeight();

    /*
     * traverse the arrays in reverse order (highest counts first)
     */
    int[] result = new int[3 + 2 * symbols.length];
    int nextArrayPos = 3;
    int nonZeroCount = 0;

    for (int i = symbols.length - 1; i >= 0; i--)
    {
      int theChar = symbols[i];
      int charCount = values[i];
      final int percentage = (charCount * 100) / divisor;
      if (percentage == 0)
      {
        /*
         * this count (and any remaining) round down to 0% - discard
         */
        break;
      }
      nonZeroCount++;
      result[nextArrayPos++] = theChar;
      result[nextArrayPos++] = percentage;
      totalPercentage += percentage;
    }

    /*
     * truncate array if any zero values were discarded
     */
    if (nonZeroCount < symbols.length)
    {
      int[] tmp = new int[3 + 2 * nonZeroCount];
      System.arraycopy(result, 0, tmp, 0, tmp.length);
      result = tmp;
    }

    /*
     * fill in 'header' values
     */
    result[0] = AlignmentAnnotation.SEQUENCE_PROFILE;
    result[1] = nonZeroCount;
    result[2] = totalPercentage;

    return result;
  }

  /**
   * Extract a sorted extract of cDNA codon profile data. The returned array
   * contains
   * 
   * <pre>
   *    [profileType, numberOfValues, totalPercentage, charValue1, percentage1, charValue2, percentage2, ...]
   * in descending order of percentage value, where the character values encode codon triplets
   * </pre>
   * 
   * @param hashtable
   * @return
   */
  public static int[] extractCdnaProfile(
          Hashtable<String, Object> hashtable, boolean ignoreGaps)
  {
    // this holds #seqs, #ungapped, and then codon count, indexed by encoded
    // codon triplet
    int[] codonCounts = (int[]) hashtable.get(PROFILE);
    int[] sortedCounts = new int[codonCounts.length - 2];
    System.arraycopy(codonCounts, 2, sortedCounts, 0,
            codonCounts.length - 2);

    int[] result = new int[3 + 2 * sortedCounts.length];
    // first value is just the type of profile data
    result[0] = AlignmentAnnotation.CDNA_PROFILE;

    char[] codons = new char[sortedCounts.length];
    for (int i = 0; i < codons.length; i++)
    {
      codons[i] = (char) i;
    }
    QuickSort.sort(sortedCounts, codons);
    int totalPercentage = 0;
    int distinctValuesCount = 0;
    int j = 3;
    int divisor = ignoreGaps ? codonCounts[1] : codonCounts[0];
    for (int i = codons.length - 1; i >= 0; i--)
    {
      final int codonCount = sortedCounts[i];
      if (codonCount == 0)
      {
        break; // nothing else of interest here
      }
      final int percentage = codonCount * 100 / divisor;
      if (percentage == 0)
      {
        /*
         * this (and any remaining) values rounded down to 0 - discard
         */
        break;
      }
      distinctValuesCount++;
      result[j++] = codons[i];
      result[j++] = percentage;
      totalPercentage += percentage;
    }
    result[2] = totalPercentage;

    /*
     * Just return the non-zero values
     */
    // todo next value is redundant if we limit the array to non-zero counts
    result[1] = distinctValuesCount;
    return Arrays.copyOfRange(result, 0, j);
  }

  /**
   * Compute a consensus for the cDNA coding for a protein alignment.
   * 
   * @param alignment
   *          the protein alignment (which should hold mappings to cDNA
   *          sequences)
   * @param hconsensus
   *          the consensus data stores to be populated (one per column)
   */
  public static void calculateCdna(AlignmentI alignment,
          Hashtable<String, Object>[] hconsensus)
  {
    final char gapCharacter = alignment.getGapCharacter();
    List<AlignedCodonFrame> mappings = alignment.getCodonFrames();
    if (mappings == null || mappings.isEmpty())
    {
      return;
    }

    int cols = alignment.getWidth();
    for (int col = 0; col < cols; col++)
    {
      // todo would prefer a Java bean for consensus data
      Hashtable<String, Object> columnHash = new Hashtable<>();
      // #seqs, #ungapped seqs, counts indexed by (codon encoded + 1)
      int[] codonCounts = new int[66];
      codonCounts[0] = alignment.getSequences().size();
      int ungappedCount = 0;
      for (SequenceI seq : alignment.getSequences())
      {
        if (seq.getCharAt(col) == gapCharacter)
        {
          continue;
        }
        List<char[]> codons = MappingUtils.findCodonsFor(seq, col,
                mappings);
        for (char[] codon : codons)
        {
          int codonEncoded = CodingUtils.encodeCodon(codon);
          if (codonEncoded >= 0)
          {
            codonCounts[codonEncoded + 2]++;
            ungappedCount++;
            break;
          }
        }
      }
      codonCounts[1] = ungappedCount;
      // todo: sort values here, save counts and codons?
      columnHash.put(PROFILE, codonCounts);
      hconsensus[col] = columnHash;
    }
  }

  /**
   * Derive displayable cDNA consensus annotation from computed consensus data.
   * 
   * @param consensusAnnotation
   *          the annotation row to be populated for display
   * @param consensusData
   *          the computed consensus data
   * @param showProfileLogo
   *          if true show all symbols present at each position, else only the
   *          modal value
   * @param nseqs
   *          the number of sequences in the alignment
   */
  public static void completeCdnaConsensus(
          AlignmentAnnotation consensusAnnotation,
          Hashtable<String, Object>[] consensusData,
          boolean showProfileLogo, int nseqs)
  {
    if (consensusAnnotation == null
            || consensusAnnotation.annotations == null
            || consensusAnnotation.annotations.length < consensusData.length)
    {
      // called with a bad alignment annotation row - wait for it to be
      // initialised properly
      return;
    }

    // ensure codon triplet scales with font size
    consensusAnnotation.scaleColLabel = true;
    for (int col = 0; col < consensusData.length; col++)
    {
      Hashtable<String, Object> hci = consensusData[col];
      if (hci == null)
      {
        // gapped protein column?
        continue;
      }
      // array holds #seqs, #ungapped, then codon counts indexed by codon
      final int[] codonCounts = (int[]) hci.get(PROFILE);
      int totalCount = 0;

      /*
       * First pass - get total count and find the highest
       */
      final char[] codons = new char[codonCounts.length - 2];
      for (int j = 2; j < codonCounts.length; j++)
      {
        final int codonCount = codonCounts[j];
        codons[j - 2] = (char) (j - 2);
        totalCount += codonCount;
      }

      /*
       * Sort array of encoded codons by count ascending - so the modal value
       * goes to the end; start by copying the count (dropping the first value)
       */
      int[] sortedCodonCounts = new int[codonCounts.length - 2];
      System.arraycopy(codonCounts, 2, sortedCodonCounts, 0,
              codonCounts.length - 2);
      QuickSort.sort(sortedCodonCounts, codons);

      int modalCodonEncoded = codons[codons.length - 1];
      int modalCodonCount = sortedCodonCounts[codons.length - 1];
      String modalCodon = String
              .valueOf(CodingUtils.decodeCodon(modalCodonEncoded));
      if (sortedCodonCounts.length > 1 && sortedCodonCounts[codons.length
              - 2] == sortedCodonCounts[codons.length - 1])
      {
        /*
         * two or more codons share the modal count
         */
        modalCodon = "+";
      }
      float pid = sortedCodonCounts[sortedCodonCounts.length - 1] * 100
              / (float) totalCount;

      /*
       * todo ? Replace consensus hashtable with sorted arrays of codons and
       * counts (non-zero only). Include total count in count array [0].
       */

      /*
       * Scan sorted array backwards for most frequent values first. Show
       * repeated values compactly.
       */
      StringBuilder mouseOver = new StringBuilder(32);
      StringBuilder samePercent = new StringBuilder();
      String percent = null;
      String lastPercent = null;
      int percentDecPl = getPercentageDp(nseqs);

      for (int j = codons.length - 1; j >= 0; j--)
      {
        int codonCount = sortedCodonCounts[j];
        if (codonCount == 0)
        {
          /*
           * remaining codons are 0% - ignore, but finish off the last one if
           * necessary
           */
          if (samePercent.length() > 0)
          {
            mouseOver.append(samePercent).append(": ").append(percent)
                    .append("% ");
          }
          break;
        }
        int codonEncoded = codons[j];
        final int pct = codonCount * 100 / totalCount;
        String codon = String
                .valueOf(CodingUtils.decodeCodon(codonEncoded));
        StringBuilder sb = new StringBuilder();
        Format.appendPercentage(sb, pct, percentDecPl);
        percent = sb.toString();
        if (showProfileLogo || codonCount == modalCodonCount)
        {
          if (percent.equals(lastPercent) && j > 0)
          {
            samePercent.append(samePercent.length() == 0 ? "" : ", ");
            samePercent.append(codon);
          }
          else
          {
            if (samePercent.length() > 0)
            {
              mouseOver.append(samePercent).append(": ").append(lastPercent)
                      .append("% ");
            }
            samePercent.setLength(0);
            samePercent.append(codon);
          }
          lastPercent = percent;
        }
      }

      consensusAnnotation.annotations[col] = new Annotation(modalCodon,
              mouseOver.toString(), ' ', pid);
    }
  }

  /**
   * Returns the number of decimal places to show for profile percentages. For
   * less than 100 sequences, returns zero (the integer percentage value will be
   * displayed). For 100-999 sequences, returns 1, for 1000-9999 returns 2, etc.
   * 
   * @param nseq
   * @return
   */
  protected static int getPercentageDp(long nseq)
  {
    int scale = 0;
    while (nseq >= 100)
    {
      scale++;
      nseq /= 10;
    }
    return scale;
  }
}
