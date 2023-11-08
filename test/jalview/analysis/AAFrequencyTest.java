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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Annotation;
import jalview.datamodel.Profile;
import jalview.datamodel.ProfileI;
import jalview.datamodel.ProfilesI;
import jalview.datamodel.ResidueCount;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;

import java.util.Hashtable;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AAFrequencyTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testCalculate_noProfile()
  {
    SequenceI seq1 = new Sequence("Seq1", "CAG-T");
    SequenceI seq2 = new Sequence("Seq2", "CAC-T");
    SequenceI seq3 = new Sequence("Seq3", "C---G");
    SequenceI seq4 = new Sequence("Seq4", "CA--t");
    SequenceI[] seqs = new SequenceI[] { seq1, seq2, seq3, seq4 };
    int width = seq1.getLength();
    ProfilesI result = AAFrequency.calculate(seqs, width, 0, width, false);

    // col 0 is 100% C
    ProfileI col = result.get(0);
    assertEquals(100f, col.getPercentageIdentity(false));
    assertEquals(100f, col.getPercentageIdentity(true));
    assertEquals(4, col.getMaxCount());
    assertEquals("C", col.getModalResidue());
    assertNull(col.getCounts());

    // col 1 is 75% A
    col = result.get(1);
    assertEquals(75f, col.getPercentageIdentity(false));
    assertEquals(100f, col.getPercentageIdentity(true));
    assertEquals(3, col.getMaxCount());
    assertEquals("A", col.getModalResidue());

    // col 2 is 50% G 50% C or 25/25 counting gaps
    col = result.get(2);
    assertEquals(25f, col.getPercentageIdentity(false));
    assertEquals(50f, col.getPercentageIdentity(true));
    assertEquals(1, col.getMaxCount());
    assertEquals("CG", col.getModalResidue());

    // col 3 is all gaps
    col = result.get(3);
    assertEquals(0f, col.getPercentageIdentity(false));
    assertEquals(0f, col.getPercentageIdentity(true));
    assertEquals(0, col.getMaxCount());
    assertEquals("", col.getModalResidue());

    // col 4 is 75% T 25% G
    col = result.get(4);
    assertEquals(75f, col.getPercentageIdentity(false));
    assertEquals(75f, col.getPercentageIdentity(true));
    assertEquals(3, col.getMaxCount());
    assertEquals("T", col.getModalResidue());
  }

  @Test(groups = { "Functional" })
  public void testCalculate_withProfile()
  {
    SequenceI seq1 = new Sequence("Seq1", "CAGT");
    SequenceI seq2 = new Sequence("Seq2", "CACT");
    SequenceI seq3 = new Sequence("Seq3", "C--G");
    SequenceI seq4 = new Sequence("Seq4", "CA-t");
    SequenceI[] seqs = new SequenceI[] { seq1, seq2, seq3, seq4 };
    int width = seq1.getLength();
    ProfilesI result = AAFrequency.calculate(seqs, width, 0, width, true);

    ProfileI profile = result.get(0);
    assertEquals(4, profile.getCounts().getCount('C'));
    assertEquals(4, profile.getHeight());
    assertEquals(4, profile.getNonGapped());

    profile = result.get(1);
    assertEquals(3, profile.getCounts().getCount('A'));
    assertEquals(4, profile.getHeight());
    assertEquals(3, profile.getNonGapped());

    profile = result.get(2);
    assertEquals(1, profile.getCounts().getCount('C'));
    assertEquals(1, profile.getCounts().getCount('G'));
    assertEquals(4, profile.getHeight());
    assertEquals(2, profile.getNonGapped());

    profile = result.get(3);
    assertEquals(3, profile.getCounts().getCount('T'));
    assertEquals(1, profile.getCounts().getCount('G'));
    assertEquals(4, profile.getHeight());
    assertEquals(4, profile.getNonGapped());
  }

  @Test(groups = { "Functional" }, enabled = false)
  public void testCalculate_withProfileTiming()
  {
    SequenceI seq1 = new Sequence("Seq1", "CAGT");
    SequenceI seq2 = new Sequence("Seq2", "CACT");
    SequenceI seq3 = new Sequence("Seq3", "C--G");
    SequenceI seq4 = new Sequence("Seq4", "CA-t");
    SequenceI[] seqs = new SequenceI[] { seq1, seq2, seq3, seq4 };

    // ensure class loaded and initialised
    int width = seq1.getLength();
    AAFrequency.calculate(seqs, width, 0, width, true);

    int reps = 100000;
    long start = System.currentTimeMillis();
    for (int i = 0; i < reps; i++)
    {
      AAFrequency.calculate(seqs, width, 0, width, true);
    }
    System.out.println(System.currentTimeMillis() - start);
  }

  /**
   * Test generation of consensus annotation with options 'include gaps'
   * (profile percentages are of all sequences, whether gapped or not), and
   * 'show logo' (the full profile with all residue percentages is reported in
   * the description for the tooltip)
   */
  @Test(groups = { "Functional" })
  public void testCompleteConsensus_includeGaps_showLogo()
  {
    /*
     * first compute the profiles
     */
    SequenceI seq1 = new Sequence("Seq1", "CAG-T");
    SequenceI seq2 = new Sequence("Seq2", "CAC-T");
    SequenceI seq3 = new Sequence("Seq3", "C---G");
    SequenceI seq4 = new Sequence("Seq4", "CA--t");
    SequenceI[] seqs = new SequenceI[] { seq1, seq2, seq3, seq4 };
    int width = seq1.getLength();
    ProfilesI profiles = AAFrequency.calculate(seqs, width, 0, width, true);

    AlignmentAnnotation consensus = new AlignmentAnnotation("Consensus",
            "PID", new Annotation[width]);
    AAFrequency.completeConsensus(consensus, profiles, 0, 5, false, true,
            4);

    Annotation ann = consensus.annotations[0];
    assertEquals("C 100%", ann.description);
    assertEquals("C", ann.displayCharacter);
    ann = consensus.annotations[1];
    assertEquals("A 75%", ann.description);
    assertEquals("A", ann.displayCharacter);
    ann = consensus.annotations[2];
    assertEquals("C 25%; G 25%", ann.description);
    assertEquals("+", ann.displayCharacter);
    ann = consensus.annotations[3];
    assertEquals("", ann.description);
    assertEquals("-", ann.displayCharacter);
    ann = consensus.annotations[4];
    assertEquals("T 75%; G 25%", ann.description);
    assertEquals("T", ann.displayCharacter);
  }

  /**
   * Test generation of consensus annotation with options 'ignore gaps' (profile
   * percentages are of the non-gapped sequences) and 'no logo' (only the modal
   * residue[s] percentage is reported in the description for the tooltip)
   */
  @Test(groups = { "Functional" })
  public void testCompleteConsensus_ignoreGaps_noLogo()
  {
    /*
     * first compute the profiles
     */
    SequenceI seq1 = new Sequence("Seq1", "CAG-T");
    SequenceI seq2 = new Sequence("Seq2", "CAC-T");
    SequenceI seq3 = new Sequence("Seq3", "C---G");
    SequenceI seq4 = new Sequence("Seq4", "CA--t");
    SequenceI[] seqs = new SequenceI[] { seq1, seq2, seq3, seq4 };
    int width = seq1.getLength();
    ProfilesI profiles = AAFrequency.calculate(seqs, width, 0, width, true);

    AlignmentAnnotation consensus = new AlignmentAnnotation("Consensus",
            "PID", new Annotation[width]);
    AAFrequency.completeConsensus(consensus, profiles, 0, 5, true, false,
            4);

    Annotation ann = consensus.annotations[0];
    assertEquals("C 100%", ann.description);
    assertEquals("C", ann.displayCharacter);
    ann = consensus.annotations[1];
    assertEquals("A 100%", ann.description);
    assertEquals("A", ann.displayCharacter);
    ann = consensus.annotations[2];
    assertEquals("[CG] 50%", ann.description);
    assertEquals("+", ann.displayCharacter);
    ann = consensus.annotations[3];
    assertEquals("", ann.description);
    assertEquals("-", ann.displayCharacter);
    ann = consensus.annotations[4];
    assertEquals("T 75%", ann.description);
    assertEquals("T", ann.displayCharacter);
  }

  /**
   * Test to include rounding down of a non-zero count to 0% (JAL-3202)
   */
  @Test(groups = { "Functional" })
  public void testExtractProfile()
  {
    /*
     * 200 sequences of which 30 gapped (170 ungapped)
     * max count 70 for modal residue 'G'
     */
    ProfileI profile = new Profile(200, 30, 70, "G");
    ResidueCount counts = new ResidueCount();
    counts.put('G', 70);
    counts.put('R', 60);
    counts.put('L', 38);
    counts.put('H', 2);
    profile.setCounts(counts);

    /*
     * [0, noOfValues, totalPercent, char1, count1, ...]
     * G: 70/170 = 41.2 = 41
     * R: 60/170 = 35.3 = 35
     * L: 38/170 = 22.3 = 22
     * H: 2/170 = 1
     * total (rounded) percentages = 99 
     */
    int[] extracted = AAFrequency.extractProfile(profile, true);
    int[] expected = new int[] { 0, 4, 99, 'G', 41, 'R', 35, 'L', 22, 'H',
        1 };
    org.testng.Assert.assertEquals(extracted, expected);

    /*
     * add some counts of 1; these round down to 0% and should be discarded
     */
    counts.put('G', 68); // 68/170 = 40% exactly (percentages now total 98)
    counts.put('Q', 1);
    counts.put('K', 1);
    extracted = AAFrequency.extractProfile(profile, true);
    expected = new int[] { 0, 4, 98, 'G', 40, 'R', 35, 'L', 22, 'H', 1 };
    org.testng.Assert.assertEquals(extracted, expected);

  }

  /**
   * Tests for the profile calculation where gaps are included i.e. the
   * denominator is the total number of sequences in the column
   */
  @Test(groups = { "Functional" })
  public void testExtractProfile_countGaps()
  {
    /*
     * 200 sequences of which 30 gapped (170 ungapped)
     * max count 70 for modal residue 'G'
     */
    ProfileI profile = new Profile(200, 30, 70, "G");
    ResidueCount counts = new ResidueCount();
    counts.put('G', 70);
    counts.put('R', 60);
    counts.put('L', 38);
    counts.put('H', 2);
    profile.setCounts(counts);

    /*
     * [0, noOfValues, totalPercent, char1, count1, ...]
     * G: 70/200 = 35%
     * R: 60/200 = 30%
     * L: 38/200 = 19%
     * H: 2/200 = 1%
     * total (rounded) percentages = 85 
     */
    int[] extracted = AAFrequency.extractProfile(profile, false);
    int[] expected = new int[] { AlignmentAnnotation.SEQUENCE_PROFILE, 4,
        85, 'G', 35, 'R', 30, 'L', 19, 'H', 1 };
    org.testng.Assert.assertEquals(extracted, expected);

    /*
     * add some counts of 1; these round down to 0% and should be discarded
     */
    counts.put('G', 68); // 68/200 = 34%
    counts.put('Q', 1);
    counts.put('K', 1);
    extracted = AAFrequency.extractProfile(profile, false);
    expected = new int[] { AlignmentAnnotation.SEQUENCE_PROFILE, 4, 84, 'G',
        34, 'R', 30, 'L', 19, 'H', 1 };
    org.testng.Assert.assertEquals(extracted, expected);

  }

  @Test(groups = { "Functional" })
  public void testExtractCdnaProfile()
  {
    /*
     * 200 sequences of which 30 gapped (170 ungapped)
     * max count 70 for modal residue 'G'
     */
    Hashtable profile = new Hashtable();

    /*
     *  cdna profile is {seqCount, ungappedCount, codonCount1, ...codonCount64}
     * where 1..64 positions correspond to encoded codons
     * see CodingUtils.encodeCodon()
     */
    int[] codonCounts = new int[66];
    char[] codon1 = new char[] { 'G', 'C', 'A' };
    char[] codon2 = new char[] { 'c', 'C', 'A' };
    char[] codon3 = new char[] { 't', 'g', 'A' };
    char[] codon4 = new char[] { 'G', 'C', 't' };
    int encoded1 = CodingUtils.encodeCodon(codon1);
    int encoded2 = CodingUtils.encodeCodon(codon2);
    int encoded3 = CodingUtils.encodeCodon(codon3);
    int encoded4 = CodingUtils.encodeCodon(codon4);
    codonCounts[2 + encoded1] = 30;
    codonCounts[2 + encoded2] = 70;
    codonCounts[2 + encoded3] = 9;
    codonCounts[2 + encoded4] = 1;
    codonCounts[0] = 120;
    codonCounts[1] = 110;
    profile.put(AAFrequency.PROFILE, codonCounts);

    /*
     * [0, noOfValues, totalPercent, char1, count1, ...]
     * codon1: 30/110 = 27.2 = 27% 
     * codon2: 70/110 = 63.6% = 63%
     * codon3: 9/110 = 8.1% = 8%
     * codon4: 1/110 = 0.9% = 0% should be discarded
     * total (rounded) percentages = 98
     */
    int[] extracted = AAFrequency.extractCdnaProfile(profile, true);
    int[] expected = new int[] { AlignmentAnnotation.CDNA_PROFILE, 3, 98,
        encoded2, 63, encoded1, 27, encoded3, 8 };
    org.testng.Assert.assertEquals(extracted, expected);
  }

  @Test(groups = { "Functional" })
  public void testExtractCdnaProfile_countGaps()
  {
    /*
     * 200 sequences of which 30 gapped (170 ungapped)
     * max count 70 for modal residue 'G'
     */
    Hashtable profile = new Hashtable();

    /*
     *  cdna profile is {seqCount, ungappedCount, codonCount1, ...codonCount64}
     * where 1..64 positions correspond to encoded codons
     * see CodingUtils.encodeCodon()
     */
    int[] codonCounts = new int[66];
    char[] codon1 = new char[] { 'G', 'C', 'A' };
    char[] codon2 = new char[] { 'c', 'C', 'A' };
    char[] codon3 = new char[] { 't', 'g', 'A' };
    char[] codon4 = new char[] { 'G', 'C', 't' };
    int encoded1 = CodingUtils.encodeCodon(codon1);
    int encoded2 = CodingUtils.encodeCodon(codon2);
    int encoded3 = CodingUtils.encodeCodon(codon3);
    int encoded4 = CodingUtils.encodeCodon(codon4);
    codonCounts[2 + encoded1] = 30;
    codonCounts[2 + encoded2] = 70;
    codonCounts[2 + encoded3] = 9;
    codonCounts[2 + encoded4] = 1;
    codonCounts[0] = 120;
    codonCounts[1] = 110;
    profile.put(AAFrequency.PROFILE, codonCounts);

    /*
     * [0, noOfValues, totalPercent, char1, count1, ...]
     * codon1: 30/120 = 25% 
     * codon2: 70/120 = 58.3 = 58%
     * codon3: 9/120 = 7.5 = 7%
     * codon4: 1/120 = 0.8 = 0% should be discarded
     * total (rounded) percentages = 90
     */
    int[] extracted = AAFrequency.extractCdnaProfile(profile, false);
    int[] expected = new int[] { AlignmentAnnotation.CDNA_PROFILE, 3, 90,
        encoded2, 58, encoded1, 25, encoded3, 7 };
    org.testng.Assert.assertEquals(extracted, expected);
  }
}
