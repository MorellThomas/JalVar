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

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.Profile;
import jalview.datamodel.ProfileI;
import jalview.datamodel.Profiles;
import jalview.datamodel.ProfilesI;
import jalview.datamodel.ResidueCount;
import jalview.datamodel.ResidueCount.SymbolCounts;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.ext.android.SparseIntArray;
import jalview.util.Format;
import jalview.util.QuickSort;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Takes in a vector or array of sequences and column start and column end and
 * returns a new Hashtable[] of size maxSeqLength, if Hashtable not supplied.
 * This class is used extensively in calculating alignment colourschemes that
 * depend on the amount of conservation in each alignment column.
 * 
 * @author $author$
 * @version $Revision$
 */
public class RepeatingVariance
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
  
  private static final HashMap<AlignmentI, Boolean> ALIGNMENTS_COVERED = new HashMap<AlignmentI, Boolean>();
  
  private static final HashMap<AlignmentI, ProfilesI> ALIGNMENTS_PROFILE = new HashMap<AlignmentI, ProfilesI>();
  
  private static final HashMap<AlignmentI, AlignmentAnnotation> ALIGNMENTS_ANNOTATION = new HashMap<AlignmentI, AlignmentAnnotation>();

  /**
   * Calculate the variance symbol(s) for each column in the given range.
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
  public static final ProfilesI calculate(final AlignmentI al)
  {
    if (ALIGNMENTS_COVERED.get(al))
    {
      return ALIGNMENTS_PROFILE.get(al);
    } else {
 System.out.println("Calculating variance for " + al.getProperty("name"));
      // long now = System.currentTimeMillis();
      int start = al.getStartRes();
      int end = al.getEndRes();
      int width = al.getWidth();
      SequenceI[] sequences = al.getSequencesArray();
      int seqCount = sequences.length;
      
      List<SequenceGroup> groups = al.getGroups();
      HashMap<List<Integer>, Integer> coordsToIndex = new HashMap<List<Integer>, Integer>();  // map alignment col, row to index in groups
      
      int index = 0;
      for (SequenceGroup sg : groups)
      {
        if (sg.getSequences().size() == 0)
        {
          index++;
          continue;
        }

        String name = sg.getSequences().get(0).getName();
        int row = 0;
        for (SequenceI seq : al.getSequencesArray())
        {
          if (seq.getName() == name)
            break;
          row++;
        }
        List<Integer> tmp = new ArrayList<Integer>();
        tmp.add(sg.getStartRes());
        tmp.add(row);
        coordsToIndex.put(tmp, index++);
      }

      ProfileI[] result = new ProfileI[width];

      for (int column = start; column < end; column++)
      {
        HashMap<String, Integer> savs = new HashMap<String, Integer>();
        
        int gapCount = 0;
        int seqsWithVar = 0;

        for (int row = 0; row < seqCount; row++)
        {
          if (sequences[row] == null)
          {
            System.err.println(
                    "WARNING: Variance skipping null sequence - possible race condition.");
            continue;
          }

          boolean hasVar = false;
          List<Integer> coords = new ArrayList<Integer>();
          coords.add(column);
          coords.add(row);
          if (coordsToIndex.containsKey(coords))
          {
            String sav = groups.get(coordsToIndex.get(coords)).getName();
            Pattern savPattern = Pattern.compile("[A-Z],[A-Z]");
            Matcher savMatcher = savPattern.matcher(sav);
            while (savMatcher.find())
            {
              String realSAV = savMatcher.group();
              if (savs.containsKey(realSAV))
              {
                savs.put(realSAV, savs.get(realSAV)+1);
              } else {
                savs.put(realSAV, 1);
              }
            }
            hasVar = true;
          }
          if (al.getSequenceAt(row).getCharAt(column) == al.getGapCharacter())
            gapCount++;

          if (hasVar)
            seqsWithVar++;
        }

        StringBuilder allSAVsb = new StringBuilder();
        int[] individCounts = new int[savs.size()];

        int i = 0;
        for (String sav : savs.keySet())
        {
          individCounts[i++] = savs.get(sav);
          allSAVsb.append(sav+";");
        }
        String[] allRes = allSAVsb.toString().split(";");

        //sorting arrays...
        String[] savsSorted = new String[allRes.length];
        Integer[] countsSorted = new Integer[individCounts.length];
        for (int j = 0; j < individCounts.length; j++)
        {
          countsSorted[j] = individCounts[j];
        }
        Arrays.sort(countsSorted, Collections.reverseOrder());
        
        HashSet<Integer> ignore = new HashSet<Integer>();
        if (individCounts.length > 0)
        {
          for (int l = 0; l < allRes.length; l++)
          {
            for (int k = 0; k < allRes.length; k++)
            {
              if (individCounts[l] == countsSorted[k] && !ignore.contains(k))
              {
                savsSorted[k] = allRes[l];
                ignore.add(k);
                break;
              }
            }
          }
        }
        
        StringBuilder sortedSB = new StringBuilder();
        for (String s : savsSorted)
        {
          sortedSB.append(s+";");
        }
        int[] sortedCnts = new int[countsSorted.length+1];
        for (int m = 0; m < countsSorted.length; m++)
        {
          sortedCnts[m] = countsSorted[m];
        }
        sortedSB.append("Total;");
        sortedCnts[sortedCnts.length-1] = seqsWithVar;
    
        ProfileI profile = new Profile(seqCount, gapCount, seqsWithVar, sortedSB.toString());
        profile.setIndividCounts(sortedCnts);

        result[column] = profile;
      }
      
      Profiles prof = new Profiles(result);
      ALIGNMENTS_COVERED.put(al, true);
      ALIGNMENTS_PROFILE.put(al, prof);
      return prof;
      // long elapsed = System.currentTimeMillis() - now;
      // System.out.println(elapsed);
    }
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
   * Derive the variance annotations to be added to the alignment for display.
   * This does not recompute the raw data, but may be called on a change in
   * display options, such as 'ignore gaps', which may in turn result in a
   * change in the derived values.
   * 
   * @param variance
   *          the annotation row to add annotations to
   * @param profiles
   *          the source variance data
   * @param startCol
   *          start column (inclusive)
   * @param endCol
   *          end column (exclusive)
   * @param ignoreGaps
   *          if true, normalise residue percentages ignoring gaps
   * @param showSequenceLogo
   *          if true include all variance symbols, else just show modal
   *          residue
   * @param nseq
   *          number of sequences
   */
  public static void completeVariance(AlignmentI al)
  {
    if (!isCovered(al))
      return;
    
    int startCol = al.getStartRes();
    int endCol = al.getEndRes();
    boolean notIgnoreGaps = true;
    boolean showSequenceLogo = false;
    long nseq = (long) al.getHeight();

    AlignmentAnnotation variance = ALIGNMENTS_ANNOTATION.get(al);
    ProfilesI profiles = ALIGNMENTS_PROFILE.get(al);
    // long now = System.currentTimeMillis();
    if (variance == null || variance.annotations == null
            || variance.annotations.length < endCol)
    {
      variance = new AlignmentAnnotation("", "", new Annotation[al.getWidth()], 0f,
              100f, AlignmentAnnotation.BAR_GRAPH);
      variance.hasText = true;
      variance.autoCalculated = false;
      //variance.groupRef = this;
      variance.label = "Variance";
      variance.description = "Most common SAV";
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
        variance.annotations[i] = null;
        return;
      }

      float value = profile.getPercentageIdentity(notIgnoreGaps);
      
      int[] individCounts = profile.getIndividCounts();
      String[] individValues = new String[individCounts.length];
      for (int k = 0; k < individCounts.length; k++)
      {
        if (k == individCounts.length-1)
        {
          individValues[k] = profile.getOccuranceFraction(individCounts[k], notIgnoreGaps);
        } else {
          individValues[k] = Integer.toString(individCounts[k]);
        }
      }

      String description = getTooltip(profile, individValues, showSequenceLogo,
              notIgnoreGaps);

      String modalResidue = profile.getModalResidue();
      if (individCounts.length <= 1)
      {
        modalResidue = "-";
      }
      else if (modalResidue.length() > 1)
      {
        // combine variants of same FromRes to get highest changing AA
        HashMap<String, Integer> mapAAtoNVar = new HashMap<String, Integer>();
        int j = 0;
        for (String sav : modalResidue.split(";"))
        {
          String aa = sav.split(",")[0];
          if (mapAAtoNVar.containsKey(aa))
          {
            mapAAtoNVar.replace(aa, mapAAtoNVar.get(aa) + individCounts[j]);
          } else {
            mapAAtoNVar.put(aa, individCounts[j]);
          }
          j++;
        }
        int highest = -1;
        for (String aa : mapAAtoNVar.keySet())
        {
          if (mapAAtoNVar.get(aa) > highest)
          {
            // set modalResidue to highest
            modalResidue = aa;
          }
        }
      }
      //bar height
      variance.annotations[i] = new Annotation(modalResidue, description,
              ' ', value);
    }
    // long elapsed = System.currentTimeMillis() - now;
    // System.out.println(-elapsed);
    ALIGNMENTS_ANNOTATION.put(al, variance);
    ALIGNMENTS_PROFILE.put(al, profiles);
  }

  /**
   * Derive the gap count annotation row.
   * 
   * @param gaprow
   *          the annotation row to add annotations to
   * @param profiles
   *          the source variance data
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
   * Returns the sorted profile for the given variance data. The returned array
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
   * @param individFracs
   * @param showSequenceLogo
   * @param ignoreGaps
   * @param dp
   *          the number of decimal places to format percentages to
   * @return
   */
  static String getTooltip(ProfileI profile, String[] individFracs,
          boolean showSequenceLogo, boolean ignoreGaps)
  {
    String description = null;
    StringBuilder sb = new StringBuilder(64);
    String[] allRes = profile.getModalResidue().split(";");
    
    if (individFracs.length > 1)
    {
      int i = 0;
      for (String sav : allRes)
      {
        sb.append(sav + " ");
        sb.append(individFracs[i++]+"<br>");
      }
    }
    description = sb.toString();
    return description;
  }

  /**
   * adds alignment to the global collections of alignments covered by this
   */
  public static void addAlignment(AlignmentI al)
  {
    ALIGNMENTS_COVERED.put(al, false);
  }
  
  /**
   * @return true if the alignment is already covered by this
   */
  public static boolean isCovered(AlignmentI al)
  {
    return ALIGNMENTS_COVERED.containsKey(al) ? ALIGNMENTS_COVERED.get(al) : false;
  }
  
  /**
   * @return ProfilesI for the alignment
   */
  public static ProfilesI getProfile(AlignmentI al)
  {
    return isCovered(al) ? ALIGNMENTS_PROFILE.get(al) : null;
  }

  /**
   * @return AlignmentAnnotation for the alignment
   */
  public static AlignmentAnnotation getAnnotation(AlignmentI al)
  {
    return isCovered(al) ? ALIGNMENTS_ANNOTATION.get(al) : null;
  }
}