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
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import jalview.api.AlignViewportI;
import jalview.datamodel.AlignedCodon;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignViewport;
import jalview.gui.JvOptionPane;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.FormatAdapter;

import java.io.IOException;
import java.util.Iterator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DnaTest
{
  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  // @formatter:off
  // AA encoding codons as ordered on the Jalview help page Amino Acid Table
  private static String fasta = ">B\n" + "GCT" + "GCC" + "GCA" + "GCG"
          + "TGT" + "TGC" + "GAT" + "GAC" + "GAA" + "GAG" + "TTT" + "TTC"
          + "GGT" + "GGC" + "GGA" + "GGG" + "CAT" + "CAC" + "ATT" + "ATC"
          + "ATA" + "AAA" + "AAG" + "TTG" + "TTA" + "CTT" + "CTC" + "CTA"
          + "CTG" + "ATG" + "AAT" + "AAC" + "CCT" + "CCC" + "CCA" + "CCG"
          + "CAA" + "CAG" + "CGT" + "CGC" + "CGA" + "CGG" + "AGA" + "AGG"
          + "TCT" + "TCC" + "TCA" + "TCG" + "AGT" + "AGC" + "ACT" + "ACC"
          + "ACA" + "ACG" + "GTT" + "GTC" + "GTA" + "GTG" + "TGG" + "TAT"
          + "TAC" + "TAA" + "TAG" + "TGA";

  private static String JAL_1312_example_align_fasta = ">B.FR.83.HXB2_LAI_IIIB_BRU_K03455/45-306\n"
          + "ATGGGAAAAAATTCGGTTAAGGCCAGGGGGAAAGAAAAAATATAAATTAAAACATATAGTATGGGCAAGCAG\n"
          + "GGAGCTAGAACGATTCGCAGTTAATCCTGGCCTGTTAGAAACATCAGAAGGCTGTAGACAAATACTGGGACA\n"
          + "GCTACAACCATCCCTTCAGACAGGATCAGAAGAACTTAGATCATTATATAATACAGTAGCAACCCTCTATTG\n"
          + "TGTGCATCAAAGGATAGAGATAAAAGACACCAAGGAAGCTTTAGAC\n"
          + ">gi|27804621|gb|AY178912.1|/1-259\n"
          + "-TGGGAGAA-ATTCGGTT-CGGCCAGGGGGAAAGAAAAAATATCAGTTAAAACATATAGTATGGGCAAGCAG\n"
          + "AGAGCTAGAACGATTCGCAGTTAACCCTGGCCTTTTAGAGACATCACAAGGCTGTAGACAAATACTGGGACA\n"
          + "GCTACAACCATCCCTTCAGACAGGATCAGAAGAACTTAAATCATTATATAATACAGTAGCAACCCTCTATTG\n"
          + "TGTTCATCAAAGGATAGATATAAAAGACACCAAGGAAGCTTTAGAT\n"
          + ">gi|27804623|gb|AY178913.1|/1-259\n"
          + "-TGGGAGAA-ATTCGGTT-CGGCCAGGGGGAAAGAAAAAATATCAGTTAAAACATATAGTATGGGCAAGCAG\n"
          + "AGAGCTAGAACGATTCGCAGTTAACCCTGGCCTTTTAGAGACATCACAAGGCTGTAGACAAATACTGGAACA\n"
          + "GCTACAACCATCCCTTCAGACAGGATCAGAAGAACTTAAATCATTATATAATACAGTAGCAACCCTCTATTG\n"
          + "TGTTCATCAAAGGATAGATGTAAAAGACACCAAGGAAGCTTTAGAT\n"
          + ">gi|27804627|gb|AY178915.1|/1-260\n"
          + "-TGGGAAAA-ATTCGGTTAAGGCCAGGGGGAAAGAAAAAATATAAGTTAAAACATATAGTATGGGCAAGCAG\n"
          + "GGAGCTAGAACGATTCGCAGTTAACCCTGGCCTGTTAGAAACATCAGAAGGTTGTAGACAAATATTGGGACA\n"
          + "GCTACAACCATCCCTTGAGACAGGATCAGAAGAACTTAAATCATTATWTAATACCATAGCAGTCCTCTATTG\n"
          + "TGTACATCAAAGGATAGATATAAAAGACACCAAGGAAGCTTTAGAG\n"
          + ">gi|27804631|gb|AY178917.1|/1-261\n"
          + "-TGGGAAAAAATTCGGTTGAGGCCAGGGGGAAAGAAAAAATATAAGTTAAAACATATAGTATGGGCAAGCAG\n"
          + "GGAGCTAGAACGATTCGCAGTCAACCCTGGCCTGTTAGAAACACCAGAAGGCTGTAGACAAATACTGGGACA\n"
          + "GCTACAACCGTCCCTTCAGACAGGATCGGAAGAACTTAAATCATTATATAATACAGTAGCAACCCTCTATTG\n"
          + "TGTGCATCAAAGGATAGATGTAAAAGACACCAAGGAGGCTTTAGAC\n"
          + ">gi|27804635|gb|AY178919.1|/1-261\n"
          + "-TGGGAGAGAATTCGGTTACGGCCAGGAGGAAAGAAAAAATATAAATTGAAACATATAGTATGGGCAGGCAG\n"
          + "AGAGCTAGATCGATTCGCAGTCAATCCTGGCCTGTTAGAAACATCAGAAGGCTGCAGACAGATATTGGGACA\n"
          + "GCTACAACCGTCCCTTAAGACAGGATCAGAAGAACTTAAATCATTATATAATACAGTAGCAACCCTCTATTG\n"
          + "TGTACATCAAAGGATAGATGTAAAAGACACCAAGGAAGCTTTAGAT\n"
          + ">gi|27804641|gb|AY178922.1|/1-261\n"
          + "-TGGGAGAAAATTCGGTTACGGCCAGGGGGAAAGAAAAGATATAAGTTAAAACATATAGTATGGGCAAGCAG\n"
          + "GGAGCTAGAACGATTCGCAGTCAACCCTGGCCTGTTAGAAACATCAGAAGGCTGCAGACAAATACTGGGACA\n"
          + "GTTACACCCATCCCTTCATACAGGATCAGAAGAACTTAAATCATTATATAATACAGTAGCAACCCTCTATTG\n"
          + "TGTGCATCAAAGGATAGAAGTAAAAGACACCAAGGAAGCTTTAGAC\n"
          + ">gi|27804647|gb|AY178925.1|/1-261\n"
          + "-TGGGAAAAAATTCGGTTAAGGCCAGGGGGAAAGAAAAAATATCAATTAAAACATGTAGTATGGGCAAGCAG\n"
          + "GGAACTAGAACGATTCGCAGTTAATCCTGGCCTGTTAGAAACATCAGAAGGCTGTAGACAAATATTGGGACA\n"
          + "GCTACAACCATCCCTTCAGACAGGATCAGAGGAACTTAAATCATTATTTAATACAGTAGCAGTCCTCTATTG\n"
          + "TGTACATCAAAGAATAGATGTAAAAGACACCAAGGAAGCTCTAGAA\n"
          + ">gi|27804649|gb|AY178926.1|/1-261\n"
          + "-TGGGAAAAAATTCGGTTAAGGCCAGGGGGAAAGAAAAAATATAAGTTAAAACATATAGTATGGGCAAGCAG\n"
          + "GGAGCTAGAACGATTCGCGGTCAATCCTGGCCTGTTAGAAACATCAGAAGGCTGTAGACAACTACTGGGACA\n"
          + "GTTACAACCATCCCTTCAGACAGGATCAGAAGAACTCAAATCATTATATAATACAATAGCAACCCTCTATTG\n"
          + "TGTGCATCAAAGGATAGAGATAAAAGACACCAAGGAAGCCTTAGAT\n"
          + ">gi|27804653|gb|AY178928.1|/1-261\n"
          + "-TGGGAAAGAATTCGGTTAAGGCCAGGGGGAAAGAAACAATATAAATTAAAACATATAGTATGGGCAAGCAG\n"
          + "GGAGCTAGACCGATTCGCACTTAACCCCGGCCTGTTAGAAACATCAGAAGGCTGTAGACAAATATTGGGACA\n"
          + "GCTACAATCGTCCCTTCAGACAGGATCAGAAGAACTTAGATCACTATATAATACAGTAGCAGTCCTCTATTG\n"
          + "TGTGCATCAAAAGATAGATGTAAAAGACACCAAGGAAGCCTTAGAC\n"
          + ">gi|27804659|gb|AY178931.1|/1-261\n"
          + "-TGGGAAAAAATTCGGTTACGGCCAGGAGGAAAGAAAAGATATAAATTAAAACATATAGTATGGGCAAGCAG\n"
          + "GGAGCTAGAACGATTYGCAGTTAATCCTGGCCTTTTAGAAACAGCAGAAGGCTGTAGACAAATACTGGGACA\n"
          + "GCTACAACCATCCCTTCAGACAGGATCAGAAGAACTTAAATCATTATATAATACAGTAGCAACCCTCTATTG\n"
          + "TGTACATCAAAGGATAGAGATAAAAGACACCAAGGAAGCTTTAGAA\n";
  // @formatter:on

  /**
   * Corner case for this test is the presence of codons after codons that were
   * not translated.
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testTranslateCdna_withUntranslatableCodons()
          throws IOException
  {
    AlignmentI alf = new FormatAdapter().readFile(
            JAL_1312_example_align_fasta, DataSourceType.PASTE,
            FileFormat.Fasta);
    HiddenColumns cs = new HiddenColumns();
    AlignViewportI av = new AlignViewport(alf, cs);
    Iterator<int[]> contigs = cs.getVisContigsIterator(0, alf.getWidth(),
            false);
    Dna dna = new Dna(av, contigs);
    AlignmentI translated = dna.translateCdna(
            GeneticCodes.getInstance().getStandardCodeTable());
    assertNotNull("Couldn't do a full width translation of test data.",
            translated);
  }

  /**
   * Test variant in which 15 column blocks at a time are translated (the rest
   * hidden).
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testTranslateCdna_withUntranslatableCodonsAndHiddenColumns()
          throws IOException
  {
    AlignmentI alf = new FormatAdapter().readFile(
            JAL_1312_example_align_fasta, DataSourceType.PASTE,
            FileFormat.Fasta);
    int vwidth = 15;
    for (int ipos = 0; ipos + vwidth < alf.getWidth(); ipos += vwidth)
    {
      HiddenColumns cs = new HiddenColumns();
      if (ipos > 0)
      {
        cs.hideColumns(0, ipos - 1);
      }
      cs.hideColumns(ipos + vwidth, alf.getWidth());
      Iterator<int[]> vcontigs = cs.getVisContigsIterator(0, alf.getWidth(),
              false);
      AlignViewportI av = new AlignViewport(alf, cs);
      Dna dna = new Dna(av, vcontigs);
      AlignmentI transAlf = dna.translateCdna(
              GeneticCodes.getInstance().getStandardCodeTable());

      assertTrue(
              "Translation failed (ipos=" + ipos + ") No alignment data.",
              transAlf != null);
      assertTrue("Translation failed (ipos=" + ipos + ") Empty alignment.",
              transAlf.getHeight() > 0);
      assertTrue(
              "Translation failed (ipos=" + ipos + ") Translated "
                      + transAlf.getHeight() + " sequences from "
                      + alf.getHeight() + " sequences",
              alf.getHeight() == transAlf.getHeight());
    }
  }

  /**
   * Test simple translation to Amino Acids (with STOP codons translated to *).
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testTranslateCdna_simple() throws IOException
  {
    AlignmentI alf = new FormatAdapter().readFile(fasta,
            DataSourceType.PASTE, FileFormat.Fasta);
    HiddenColumns cs = new HiddenColumns();
    AlignViewportI av = new AlignViewport(alf, cs);
    Iterator<int[]> contigs = cs.getVisContigsIterator(0, alf.getWidth(),
            false);
    Dna dna = new Dna(av, contigs);
    AlignmentI translated = dna.translateCdna(
            GeneticCodes.getInstance().getStandardCodeTable());
    String aa = translated.getSequenceAt(0).getSequenceAsString();
    assertEquals(
            "AAAACCDDEEFFGGGGHHIIIKKLLLLLLMNNPPPPQQRRRRRRSSSSSSTTTTVVVVWYY***",
            aa);
  }

  /**
   * Test translation excluding hidden columns.
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testTranslateCdna_hiddenColumns() throws IOException
  {
    AlignmentI alf = new FormatAdapter().readFile(fasta,
            DataSourceType.PASTE, FileFormat.Fasta);
    HiddenColumns cs = new HiddenColumns();
    cs.hideColumns(6, 14); // hide codons 3/4/5
    cs.hideColumns(24, 35); // hide codons 9-12
    cs.hideColumns(177, 191); // hide codons 60-64
    AlignViewportI av = new AlignViewport(alf, cs);
    Iterator<int[]> contigs = cs.getVisContigsIterator(0, alf.getWidth(),
            false);
    Dna dna = new Dna(av, contigs);
    AlignmentI translated = dna.translateCdna(
            GeneticCodes.getInstance().getStandardCodeTable());
    String aa = translated.getSequenceAt(0).getSequenceAsString();
    assertEquals("AACDDGGGGHHIIIKKLLLLLLMNNPPPPQQRRRRRRSSSSSSTTTTVVVVW",
            aa);
  }

  /**
   * Use this test to help debug into any cases of interest.
   */
  @Test(groups = { "Functional" })
  public void testCompareCodonPos_oneOnly()
  {
    assertFollows("-AA--A", "G--GG"); // 2 shifted seq2, 3 shifted seq1
  }

  /**
   * Tests for method that compares 'alignment' of two codon position triplets.
   */
  @Test(groups = { "Functional" })
  public void testCompareCodonPos()
  {
    /*
     * Returns 0 for any null argument
     */
    assertEquals(0, Dna.compareCodonPos(new AlignedCodon(1, 2, 3), null));
    assertEquals(0, Dna.compareCodonPos(null, new AlignedCodon(1, 2, 3)));

    /*
     * Work through 27 combinations. First 9 cases where first position matches.
     */
    assertMatches("AAA", "GGG"); // 2 and 3 match
    assertFollows("AA-A", "GGG"); // 2 matches, 3 shifted seq1
    assertPrecedes("AAA", "GG-G"); // 2 matches, 3 shifted seq2
    assertFollows("A-AA", "GG-G"); // 2 shifted seq1, 3 matches
    assertFollows("A-A-A", "GG-G"); // 2 shifted seq1, 3 shifted seq1
    assertPrecedes("A-AA", "GG--G"); // 2 shifted seq1, 3 shifted seq2
    assertPrecedes("AA-A", "G-GG"); // 2 shifted seq2, 3 matches
    assertFollows("AA--A", "G-GG"); // 2 shifted seq2, 3 shifted seq1
    assertPrecedes("AAA", "G-GG"); // 2 shifted seq2, 3 shifted seq2

    /*
     * 9 cases where first position is shifted in first sequence.
     */
    assertFollows("-AAA", "G-GG"); // 2 and 3 match
    assertFollows("-AA-A", "G-GG"); // 2 matches, 3 shifted seq1
    // 'enclosing' case: pick first to start precedes
    assertFollows("-AAA", "G-G-G"); // 2 matches, 3 shifted seq2
    assertFollows("-A-AA", "G-G-G"); // 2 shifted seq1, 3 matches
    assertFollows("-A-A-A", "G-G-G"); // 2 shifted seq1, 3 shifted seq1
    // 'enclosing' case: pick first to start precedes
    assertFollows("-A-AA", "G-G--G"); // 2 shifted seq1, 3 shifted seq2
    assertFollows("-AA-A", "G--GG"); // 2 shifted seq2, 3 matches
    assertFollows("-AA--A", "G--GG"); // 2 shifted seq2, 3 shifted seq1
    assertPrecedes("-AAA", "G--GG"); // 2 shifted seq2, 3 shifted seq2

    /*
     * 9 cases where first position is shifted in second sequence.
     */
    assertPrecedes("A-AA", "-GGG"); // 2 and 3 match
    assertPrecedes("A-A-A", "-GGG"); // 2 matches, 3 shifted seq1
    assertPrecedes("A-AA", "-GG-G"); // 2 matches, 3 shifted seq2
    assertPrecedes("A--AA", "-GG-G"); // 2 shifted seq1, 3 matches
    // 'enclosing' case with middle base deciding:
    assertFollows("A--AA", "-GGG"); // 2 shifted seq1, 3 shifted seq1
    assertPrecedes("A--AA", "-GG--G"); // 2 shifted seq1, 3 shifted seq2
    assertPrecedes("AA-A", "-GGG"); // 2 shifted seq2, 3 matches
    assertPrecedes("AA--A", "-GGG"); // 2 shifted seq2, 3 shifted seq1
    assertPrecedes("AAA", "-GGG"); // 2 shifted seq2, 3 shifted seq2
  }

  /**
   * This test generates a random cDNA alignment and its translation, then
   * reorders the cDNA and retranslates, and verifies that the translations are
   * the same (apart from ordering).
   */
  @Test(groups = { "Functional" })
  public void testTranslateCdna_sequenceOrderIndependent()
  {
    /*
     * Generate cDNA - 8 sequences of 12 bases each.
     */
    AlignmentI cdna = new AlignmentGenerator(true).generate(12, 8, 97, 5,
            5);
    HiddenColumns cs = new HiddenColumns();
    AlignViewportI av = new AlignViewport(cdna, cs);
    Iterator<int[]> contigs = cs.getVisContigsIterator(0, cdna.getWidth(),
            false);
    Dna dna = new Dna(av, contigs);
    AlignmentI translated = dna.translateCdna(
            GeneticCodes.getInstance().getStandardCodeTable());

    /*
     * Jumble the cDNA sequences and translate.
     */
    SequenceI[] sorted = new SequenceI[cdna.getHeight()];
    final int[] jumbler = new int[] { 6, 7, 3, 4, 2, 0, 1, 5 };
    int seqNo = 0;
    for (int i : jumbler)
    {
      sorted[seqNo++] = cdna.getSequenceAt(i);
    }
    AlignmentI cdnaReordered = new Alignment(sorted);
    av = new AlignViewport(cdnaReordered, cs);
    contigs = cs.getVisContigsIterator(0, cdna.getWidth(), false);
    dna = new Dna(av, contigs);
    AlignmentI translated2 = dna.translateCdna(
            GeneticCodes.getInstance().getStandardCodeTable());

    /*
     * Check translated sequences are the same in both alignments.
     */
    System.out.println("Original");
    System.out.println(translated.toString());
    System.out.println("Sorted");
    System.out.println(translated2.toString());

    int sortedSequenceIndex = 0;
    for (int originalSequenceIndex : jumbler)
    {
      final String translation1 = translated
              .getSequenceAt(originalSequenceIndex).getSequenceAsString();
      final String translation2 = translated2
              .getSequenceAt(sortedSequenceIndex).getSequenceAsString();
      assertEquals(translation2, translation1);
      sortedSequenceIndex++;
    }
  }

  /**
   * Test that all the cases in testCompareCodonPos have a 'symmetric'
   * comparison (without checking the actual comparison result).
   */
  @Test(groups = { "Functional" })
  public void testCompareCodonPos_isSymmetric()
  {
    assertSymmetric("AAA", "GGG");
    assertSymmetric("AA-A", "GGG");
    assertSymmetric("AAA", "GG-G");
    assertSymmetric("A-AA", "GG-G");
    assertSymmetric("A-A-A", "GG-G");
    assertSymmetric("A-AA", "GG--G");
    assertSymmetric("AA-A", "G-GG");
    assertSymmetric("AA--A", "G-GG");
    assertSymmetric("AAA", "G-GG");
    assertSymmetric("-AAA", "G-GG");
    assertSymmetric("-AA-A", "G-GG");
    assertSymmetric("-AAA", "G-G-G");
    assertSymmetric("-A-AA", "G-G-G");
    assertSymmetric("-A-A-A", "G-G-G");
    assertSymmetric("-A-AA", "G-G--G");
    assertSymmetric("-AA-A", "G--GG");
    assertSymmetric("-AA--A", "G--GG");
    assertSymmetric("-AAA", "G--GG");
    assertSymmetric("A-AA", "-GGG");
    assertSymmetric("A-A-A", "-GGG");
    assertSymmetric("A-AA", "-GG-G");
    assertSymmetric("A--AA", "-GG-G");
    assertSymmetric("A--AA", "-GGG");
    assertSymmetric("A--AA", "-GG--G");
    assertSymmetric("AA-A", "-GGG");
    assertSymmetric("AA--A", "-GGG");
    assertSymmetric("AAA", "-GGG");
  }

  private void assertSymmetric(String codon1, String codon2)
  {
    assertEquals(
            "Comparison of '" + codon1 + "' and '" + codon2
                    + " not symmetric",
            Integer.signum(compare(codon1, codon2)),
            -Integer.signum(compare(codon2, codon1)));
  }

  /**
   * Assert that the first sequence should map to the same position as the
   * second in a translated alignment. Also checks that this is true if the
   * order of the codons is reversed.
   * 
   * @param codon1
   * @param codon2
   */
  private void assertMatches(String codon1, String codon2)
  {
    assertEquals("Expected '" + codon1 + "' matches '" + codon2 + "'", 0,
            compare(codon1, codon2));
    assertEquals("Expected '" + codon2 + "' matches '" + codon1 + "'", 0,
            compare(codon2, codon1));
  }

  /**
   * Assert that the first sequence should precede the second in a translated
   * alignment
   * 
   * @param codon1
   * @param codon2
   */
  private void assertPrecedes(String codon1, String codon2)
  {
    assertEquals("Expected '" + codon1 + "'  precedes '" + codon2 + "'", -1,
            compare(codon1, codon2));
  }

  /**
   * Assert that the first sequence should follow the second in a translated
   * alignment
   * 
   * @param codon1
   * @param codon2
   */
  private void assertFollows(String codon1, String codon2)
  {
    assertEquals("Expected '" + codon1 + "'  follows '" + codon2 + "'", 1,
            compare(codon1, codon2));
  }

  /**
   * Convert two nucleotide strings to base positions and pass to
   * Dna.compareCodonPos, return the result.
   * 
   * @param s1
   * @param s2
   * @return
   */
  private int compare(String s1, String s2)
  {
    final AlignedCodon cd1 = convertCodon(s1);
    final AlignedCodon cd2 = convertCodon(s2);
    System.out.println("K: " + s1 + "  " + cd1.toString());
    System.out.println("G: " + s2 + "  " + cd2.toString());
    System.out.println();
    return Dna.compareCodonPos(cd1, cd2);
  }

  /**
   * Convert a string e.g. "-GC-T" to base positions e.g. [1, 2, 4]. The string
   * should have exactly 3 non-gap characters, and use '-' for gaps.
   * 
   * @param s
   * @return
   */
  private AlignedCodon convertCodon(String s)
  {
    int[] codon = new int[3];
    int i = 0;
    for (int j = 0; j < s.length(); j++)
    {
      if (s.charAt(j) != '-')
      {
        codon[i++] = j;
      }
    }
    return new AlignedCodon(codon[0], codon[1], codon[2]);
  }

  /**
   * Weirdly, maybe worth a test to prove the helper method of this test class.
   */
  @Test(groups = { "Functional" })
  public void testConvertCodon()
  {
    assertEquals("[0, 1, 2]", convertCodon("AAA").toString());
    assertEquals("[0, 2, 5]", convertCodon("A-A--A").toString());
    assertEquals("[1, 3, 4]", convertCodon("-A-AA-").toString());
  }

  /**
   * Test dna complementing
   */
  @Test(groups = "Functional")
  public void testGetComplement()
  {
    assertEquals('t', Dna.getComplement('a'));
    assertEquals('T', Dna.getComplement('A'));
    assertEquals('a', Dna.getComplement('t'));
    assertEquals('A', Dna.getComplement('T'));
    assertEquals('c', Dna.getComplement('g'));
    assertEquals('C', Dna.getComplement('G'));
    assertEquals('g', Dna.getComplement('c'));
    assertEquals('G', Dna.getComplement('C'));
    // note uU --> aA but not vice versa
    assertEquals('a', Dna.getComplement('u'));
    assertEquals('A', Dna.getComplement('U'));
    // ambiguity codes, see http://www.bioinformatics.org/sms/iupac.html
    assertEquals('r', Dna.getComplement('y'));
    assertEquals('R', Dna.getComplement('Y'));
    assertEquals('y', Dna.getComplement('r'));
    assertEquals('Y', Dna.getComplement('R'));
    assertEquals('k', Dna.getComplement('m'));
    assertEquals('K', Dna.getComplement('M'));
    assertEquals('m', Dna.getComplement('k'));
    assertEquals('M', Dna.getComplement('K'));
    assertEquals('b', Dna.getComplement('v'));
    assertEquals('B', Dna.getComplement('V'));
    assertEquals('v', Dna.getComplement('b'));
    assertEquals('V', Dna.getComplement('B'));
    assertEquals('d', Dna.getComplement('h'));
    assertEquals('D', Dna.getComplement('H'));
    assertEquals('h', Dna.getComplement('d'));
    assertEquals('H', Dna.getComplement('D'));
    assertEquals('Q', Dna.getComplement('Q'));
  }

  @Test(groups = "Functional")
  public void testReverseSequence()
  {
    String seq = "-Ac-GtU--rYkMbVdHNX-";
    String seqRev = new StringBuilder(seq).reverse().toString();

    // reverse:
    SequenceI reversed = Dna.reverseSequence("Seq1", seq, false);
    assertEquals(1, reversed.getStart());
    assertEquals(15, reversed.getEnd());
    assertEquals(20, reversed.getLength());
    assertEquals(seqRev, reversed.getSequenceAsString());
    assertEquals("Seq1|rev", reversed.getName());

    // reverse complement:
    SequenceI revcomp = Dna.reverseSequence("Seq1", seq, true);
    assertEquals("-XNDhBvKmRy--AaC-gT-", revcomp.getSequenceAsString());
    assertEquals("Seq1|revcomp", revcomp.getName());
  }

  @Test(groups = "Functional")
  public void testReverseCdna()
  {
    String seq = "-Ac-GtU--rYkMbVdHNX-";
    String seqRev = new StringBuilder(seq).reverse().toString();
    String seqDs = seq.replaceAll("-", "");
    String seqDsRev = new StringBuilder(seqDs).reverse().toString();

    SequenceI dna = new Sequence("Seq1", seq);
    Alignment al = new Alignment(new SequenceI[] { dna });
    al.createDatasetAlignment();
    assertEquals(seqDs,
            al.getSequenceAt(0).getDatasetSequence().getSequenceAsString());

    HiddenColumns cs = new HiddenColumns();
    AlignViewportI av = new AlignViewport(al, cs);
    Iterator<int[]> contigs = cs.getVisContigsIterator(0, al.getWidth(),
            false);
    Dna testee = new Dna(av, contigs);
    AlignmentI reversed = testee.reverseCdna(false);
    assertEquals(1, reversed.getHeight());
    assertEquals(seqRev, reversed.getSequenceAt(0).getSequenceAsString());
    assertEquals(seqDsRev, reversed.getSequenceAt(0).getDatasetSequence()
            .getSequenceAsString());
  }
}
