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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.datamodel.AlignedCodonFrame.SequenceToSequenceMapping;
import jalview.gui.JvOptionPane;
import jalview.util.MapList;

public class AlignedCodonFrameTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * Test the method that locates the first aligned sequence that has a mapping.
   */
  @Test(groups = { "Functional" })
  public void testFindAlignedSequence()
  {
    AlignmentI cdna = new Alignment(new SequenceI[] {});
    final Sequence seq1 = new Sequence("Seq1", "C-G-TA-GC");
    seq1.createDatasetSequence();
    cdna.addSequence(seq1);
    final Sequence seq2 = new Sequence("Seq2", "-TA-GG-GG");
    seq2.createDatasetSequence();
    cdna.addSequence(seq2);

    AlignmentI aa = new Alignment(new SequenceI[] {});
    final Sequence aseq1 = new Sequence("Seq1", "-P-R");
    aseq1.createDatasetSequence();
    aa.addSequence(aseq1);
    final Sequence aseq2 = new Sequence("Seq2", "-LY-");
    aseq2.createDatasetSequence();
    aa.addSequence(aseq2);

    /*
     * Mapping from first DNA sequence to second AA sequence.
     */
    AlignedCodonFrame acf = new AlignedCodonFrame();

    assertNull(acf.findAlignedSequence(seq1, aa));

    MapList map = new MapList(new int[] { 1, 6 }, new int[] { 1, 2 }, 3, 1);
    acf.addMap(seq1.getDatasetSequence(), aseq2.getDatasetSequence(), map);

    /*
     * DNA seq1 maps to AA seq2
     */
    assertEquals(aa.getSequenceAt(1), acf.findAlignedSequence(
            cdna.getSequenceAt(0).getDatasetSequence(), aa));
    // can also find this from the dna aligned sequence
    assertEquals(aa.getSequenceAt(1),
            acf.findAlignedSequence(cdna.getSequenceAt(0), aa));

    assertEquals(cdna.getSequenceAt(0), acf.findAlignedSequence(
            aa.getSequenceAt(1).getDatasetSequence(), cdna));
  }

  /**
   * Test the method that locates the mapped codon for a protein position.
   */
  @Test(groups = { "Functional" })
  public void testGetMappedRegion()
  {
    // introns lower case, exons upper case
    final Sequence dna1 = new Sequence("Seq1/10-18", "c-G-TA-gC-gT-T");
    dna1.createDatasetSequence();
    final Sequence dna2 = new Sequence("Seq2/20-28", "-TA-gG-Gg-CG-a");
    dna2.createDatasetSequence();

    final Sequence pep1 = new Sequence("Seq1/3-4", "-P-R");
    pep1.createDatasetSequence();
    final Sequence pep2 = new Sequence("Seq2/7-9", "-LY-Q");
    pep2.createDatasetSequence();

    /*
     * First with no mappings
     */
    AlignedCodonFrame acf = new AlignedCodonFrame();

    assertNull(acf.getMappedRegion(dna1, pep1, 3));

    /*
     * Set up the mappings for the exons (upper-case bases)
     * Note residue Q is unmapped
     */
    MapList map1 = new MapList(new int[] { 11, 13, 15, 15, 17, 18 },
            new int[]
            { 3, 4 }, 3, 1);
    acf.addMap(dna1.getDatasetSequence(), pep1.getDatasetSequence(), map1);
    MapList map2 = new MapList(new int[] { 20, 21, 23, 24, 26, 27 },
            new int[]
            { 7, 9 }, 3, 1);
    acf.addMap(dna2.getDatasetSequence(), pep2.getDatasetSequence(), map2);

    /*
     * get codon positions for peptide position
     */
    assertArrayEquals(new int[] { 11, 13 },
            acf.getMappedRegion(dna1, pep1, 3));
    assertArrayEquals(new int[] { 15, 15, 17, 18 },
            acf.getMappedRegion(dna1, pep1, 4));
    assertArrayEquals(new int[] { 20, 21, 23, 23 },
            acf.getMappedRegion(dna2, pep2, 7));
    assertArrayEquals(new int[] { 24, 24, 26, 27 },
            acf.getMappedRegion(dna2, pep2, 8));

    /*
     * No mapping from dna2 to Q
     */
    assertNull(acf.getMappedRegion(dna2, pep2, 9));

    /*
     * No mapping from dna1 to pep2
     */
    assertNull(acf.getMappedRegion(dna1, pep2, 7));

    /*
     * get peptide position for codon position
     */
    assertArrayEquals(new int[] { 3, 3 },
            acf.getMappedRegion(pep1, dna1, 11));
    assertArrayEquals(new int[] { 3, 3 },
            acf.getMappedRegion(pep1, dna1, 12));
    assertArrayEquals(new int[] { 3, 3 },
            acf.getMappedRegion(pep1, dna1, 13));
    assertNull(acf.getMappedRegion(pep1, dna1, 14)); // intron base, not mapped

  }

  @Test(groups = { "Functional" })
  public void testGetMappedCodons()
  {
    final Sequence seq1 = new Sequence("Seq1", "c-G-TA-gC-gT-T");
    seq1.createDatasetSequence();
    final Sequence aseq1 = new Sequence("Seq1", "-V-L");
    aseq1.createDatasetSequence();

    /*
     * First with no mappings
     */
    AlignedCodonFrame acf = new AlignedCodonFrame();

    assertNull(acf.getMappedCodons(seq1.getDatasetSequence(), 0));

    /*
     * Set up the mappings for the exons (upper-case bases)
     */
    MapList map = new MapList(new int[] { 2, 4, 6, 6, 8, 9 },
            new int[]
            { 1, 2 }, 3, 1);
    acf.addMap(seq1.getDatasetSequence(), aseq1.getDatasetSequence(), map);

    assertEquals(1,
            acf.getMappedCodons(aseq1.getDatasetSequence(), 1).size());
    assertEquals("[G, T, A]", Arrays.toString(
            acf.getMappedCodons(aseq1.getDatasetSequence(), 1).get(0)));
    assertEquals("[C, T, T]", Arrays.toString(
            acf.getMappedCodons(aseq1.getDatasetSequence(), 2).get(0)));
  }

  /**
   * Test for the case where there is more than one variant of the DNA mapping
   * to a protein sequence
   */
  @Test(groups = { "Functional" })
  public void testGetMappedCodons_dnaVariants()
  {
    final Sequence seq1 = new Sequence("Seq1", "c-G-TA-gC-gT-T");
    seq1.createDatasetSequence();
    final Sequence seq2 = new Sequence("Seq2", "c-G-TT-gT-gT-A");
    seq2.createDatasetSequence();
    final Sequence aseq1 = new Sequence("Seq1", "-V-L");
    aseq1.createDatasetSequence();

    AlignedCodonFrame acf = new AlignedCodonFrame();

    /*
     * Set up the mappings for the exons (upper-case bases)
     */
    MapList map = new MapList(new int[] { 2, 4, 6, 6, 8, 9 },
            new int[]
            { 1, 2 }, 3, 1);
    acf.addMap(seq1.getDatasetSequence(), aseq1.getDatasetSequence(), map);
    acf.addMap(seq2.getDatasetSequence(), aseq1.getDatasetSequence(), map);

    assertEquals(2,
            acf.getMappedCodons(aseq1.getDatasetSequence(), 1).size());
    List<char[]> codonsForV = acf
            .getMappedCodons(aseq1.getDatasetSequence(), 1);
    assertEquals("[G, T, A]", Arrays.toString(codonsForV.get(0)));
    assertEquals("[G, T, T]", Arrays.toString(codonsForV.get(1)));
    List<char[]> codonsForL = acf
            .getMappedCodons(aseq1.getDatasetSequence(), 2);
    assertEquals("[C, T, T]", Arrays.toString(codonsForL.get(0)));
    assertEquals("[T, T, A]", Arrays.toString(codonsForL.get(1)));
  }

  /**
   * Test for the case where sequences have start > 1
   */
  @Test(groups = { "Functional" })
  public void testGetMappedCodons_forSubSequences()
  {
    final Sequence seq1 = new Sequence("Seq1", "c-G-TA-gC-gT-T", 27, 35);
    seq1.createDatasetSequence();

    final Sequence aseq1 = new Sequence("Seq1", "-V-L", 12, 13);
    aseq1.createDatasetSequence();

    /*
     * Set up the mappings for the exons (upper-case bases)
     */
    AlignedCodonFrame acf = new AlignedCodonFrame();
    MapList map = new MapList(new int[] { 28, 30, 32, 32, 34, 35 },
            new int[]
            { 12, 13 }, 3, 1);
    acf.addMap(seq1.getDatasetSequence(), aseq1.getDatasetSequence(), map);

    assertEquals("[G, T, A]", Arrays.toString(
            acf.getMappedCodons(aseq1.getDatasetSequence(), 12).get(0)));
    assertEquals("[C, T, T]", Arrays.toString(
            acf.getMappedCodons(aseq1.getDatasetSequence(), 13).get(0)));
  }

  @Test(groups = { "Functional" })
  public void testCouldReplaceSequence()
  {
    SequenceI seq1 = new Sequence("Seq1/10-21", "aaacccgggttt");
    SequenceI seq1proxy = new SequenceDummy("Seq1");

    // map to region within sequence is ok
    assertTrue(AlignedCodonFrame.couldRealiseSequence(seq1proxy, seq1, 12,
            17));
    // map to region overlapping sequence is ok
    assertTrue(
            AlignedCodonFrame.couldRealiseSequence(seq1proxy, seq1, 5, 10));
    assertTrue(AlignedCodonFrame.couldRealiseSequence(seq1proxy, seq1, 21,
            26));
    // map to region before sequence is not ok
    assertFalse(
            AlignedCodonFrame.couldRealiseSequence(seq1proxy, seq1, 4, 9));
    // map to region after sequence is not ok
    assertFalse(AlignedCodonFrame.couldRealiseSequence(seq1proxy, seq1, 22,
            27));

    /*
     * test should fail if name doesn't match
     */
    seq1proxy.setName("Seq1a");
    assertFalse(AlignedCodonFrame.couldRealiseSequence(seq1proxy, seq1, 12,
            17));
    seq1proxy.setName("Seq1");
    seq1.setName("Seq1a");
    assertFalse(AlignedCodonFrame.couldRealiseSequence(seq1proxy, seq1, 12,
            17));

    /*
     * a dummy sequence can't replace a real one
     */
    assertFalse(AlignedCodonFrame.couldRealiseSequence(seq1, seq1proxy, 12,
            17));

    /*
     * a dummy sequence can't replace a dummy sequence
     */
    SequenceI seq1proxy2 = new SequenceDummy("Seq1");
    assertFalse(AlignedCodonFrame.couldRealiseSequence(seq1proxy,
            seq1proxy2, 12, 17));

    /*
     * a real sequence can't replace a real one
     */
    SequenceI seq1a = new Sequence("Seq1/10-21", "aaacccgggttt");
    assertFalse(
            AlignedCodonFrame.couldRealiseSequence(seq1, seq1a, 12, 17));
  }

  /**
   * Tests for the method that tests whether any mapping to a dummy sequence can
   * be 'realised' to a given real sequence
   */
  @Test(groups = { "Functional" })
  public void testIsRealisableWith()
  {
    SequenceI seq1 = new Sequence("Seq1", "tttaaaCCCGGGtttaaa");
    SequenceI seq2 = new Sequence("Seq2", "PG");
    SequenceI seq1proxy = new SequenceDummy("Seq1");
    seq1.createDatasetSequence();
    seq2.createDatasetSequence();
    MapList mapList = new MapList(new int[] { 7, 12 }, new int[] { 2, 3 },
            3, 1);
    AlignedCodonFrame acf = new AlignedCodonFrame();
    acf.addMap(seq1proxy, seq2, mapList);

    /*
     * Seq2 is mapped to SequenceDummy seq1proxy bases 4-9
     * This is 'realisable' from real sequence Seq1
     */
    assertTrue(acf.isRealisableWith(seq1));

    /*
     * test should fail if name doesn't match
     */
    seq1proxy.setName("Seq1a");
    assertFalse(acf.isRealisableWith(seq1));
    seq1proxy.setName("Seq1");

    SequenceI seq1ds = seq1.getDatasetSequence();
    seq1ds.setName("Seq1a");
    assertFalse(acf.isRealisableWith(seq1));
    seq1ds.setName("Seq1");

    /*
     * test should fail if no sequence overlap with mapping of bases 7-12
     * use artificial start/end values to test this
     */
    seq1ds.setStart(1);
    seq1ds.setEnd(6);
    // seq1 precedes mapped region:
    assertFalse(acf.isRealisableWith(seq1));
    seq1ds.setEnd(7);
    // seq1 includes first mapped base:
    assertTrue(acf.isRealisableWith(seq1));
    seq1ds.setStart(13);
    seq1ds.setEnd(18);
    // seq1 follows mapped region:
    assertFalse(acf.isRealisableWith(seq1));
    seq1ds.setStart(12);
    // seq1 includes last mapped base:
    assertTrue(acf.isRealisableWith(seq1));
  }

  /**
   * Tests for the method that converts mappings to a dummy sequence to mappings
   * to a compatible real sequence
   */
  @Test(groups = { "Functional" })
  public void testRealiseWith()
  {
    SequenceI seq1 = new Sequence("Seq1", "tttCAACCCGGGtttaaa");
    SequenceI seq2 = new Sequence("Seq2", "QPG");
    SequenceI seq2a = new Sequence("Seq2a", "QPG");
    SequenceI seq1proxy = new SequenceDummy("Seq1");
    seq1.createDatasetSequence();
    seq2.createDatasetSequence();
    seq2a.createDatasetSequence();

    /*
     * Make mappings from Seq2 and Seq2a peptides to dummy sequence Seq1
     */
    AlignedCodonFrame acf = new AlignedCodonFrame();

    // map PG to codons 7-12 (CCCGGG)
    MapList mapping1 = new MapList(new int[] { 7, 12 }, new int[] { 2, 3 },
            3, 1);
    acf.addMap(seq1proxy, seq2, mapping1);
    acf.addMap(seq1proxy, seq2a, mapping1);

    // map QP to codons 4-9 (CAACCC)
    MapList mapping2 = new MapList(new int[] { 4, 9 }, new int[] { 1, 2 },
            3, 1);
    acf.addMap(seq1proxy, seq2, mapping2);
    acf.addMap(seq1proxy, seq2a, mapping2);

    /*
     * acf now has two mappings one from Seq1 to Seq2, one from Seq1 to Seq2a
     */
    assertEquals(2, acf.getdnaSeqs().length);
    assertSame(seq1proxy, acf.getdnaSeqs()[0]);
    assertSame(seq1proxy, acf.getdnaSeqs()[1]);
    assertEquals(2, acf.getProtMappings().length);

    // 'realise' these mappings with the compatible sequence seq1
    // two mappings should be updated:
    assertEquals(2, acf.realiseWith(seq1));
    assertSame(seq1.getDatasetSequence(), acf.getdnaSeqs()[0]);
    assertSame(seq1.getDatasetSequence(), acf.getdnaSeqs()[1]);
  }

  /**
   * Test the method that locates the mapped codon for a protein position.
   */
  @Test(groups = { "Functional" })
  public void testGetMappedRegion_eitherWay()
  {
    final Sequence seq1 = new Sequence("Seq1", "AAACCCGGGTTT");
    seq1.createDatasetSequence();
    final Sequence seq2 = new Sequence("Seq2", "KPGF");
    seq2.createDatasetSequence();
    final Sequence seq3 = new Sequence("Seq3", "QYKPGFSW");
    seq3.createDatasetSequence();

    /*
     * map Seq1 to all of Seq2 and part of Seq3
     */
    AlignedCodonFrame acf = new AlignedCodonFrame();
    MapList map = new MapList(new int[] { 1, 12 }, new int[] { 1, 4 }, 3,
            1);
    acf.addMap(seq1.getDatasetSequence(), seq2.getDatasetSequence(), map);
    map = new MapList(new int[] { 1, 12 }, new int[] { 3, 6 }, 3, 1);
    acf.addMap(seq1.getDatasetSequence(), seq3.getDatasetSequence(), map);

    /*
     * map part of Seq3 to Seq2
     */
    map = new MapList(new int[] { 3, 6 }, new int[] { 1, 4 }, 1, 1);
    acf.addMap(seq3.getDatasetSequence(), seq2.getDatasetSequence(), map);

    /*
     * original case - locate mapped codon for protein position
     */
    assertArrayEquals(new int[] { 4, 6 },
            acf.getMappedRegion(seq1, seq2, 2));
    assertArrayEquals(new int[] { 7, 9 },
            acf.getMappedRegion(seq1, seq3, 5));
    assertNull(acf.getMappedRegion(seq1, seq3, 1));

    /*
     * locate mapped protein for protein position
     */
    assertArrayEquals(new int[] { 4, 4 },
            acf.getMappedRegion(seq3, seq2, 2));

    /*
     * reverse location protein-to-protein
     */
    assertArrayEquals(new int[] { 2, 2 },
            acf.getMappedRegion(seq2, seq3, 4));

    /*
     * reverse location protein-from-nucleotide
     * any of codon [4, 5, 6] positions map to seq2/2
     */
    assertArrayEquals(new int[] { 2, 2 },
            acf.getMappedRegion(seq2, seq1, 4));
    assertArrayEquals(new int[] { 2, 2 },
            acf.getMappedRegion(seq2, seq1, 5));
    assertArrayEquals(new int[] { 2, 2 },
            acf.getMappedRegion(seq2, seq1, 6));
  }

  /**
   * Tests for addMap. See also tests for MapList.addMapList
   */
  @Test(groups = { "Functional" })
  public void testAddMap()
  {
    final Sequence seq1 = new Sequence("Seq1", "c-G-TA-gC-gT-T");
    seq1.createDatasetSequence();
    final Sequence aseq1 = new Sequence("Seq1", "-V-L");
    aseq1.createDatasetSequence();

    AlignedCodonFrame acf = new AlignedCodonFrame();
    MapList map = new MapList(new int[] { 2, 4, 6, 6, 8, 9 },
            new int[]
            { 1, 2 }, 3, 1);
    acf.addMap(seq1.getDatasetSequence(), aseq1.getDatasetSequence(), map);
    assertEquals(1, acf.getMappingsFromSequence(seq1).size());
    Mapping before = acf.getMappingsFromSequence(seq1).get(0);

    /*
     * add the same map again, verify it doesn't get duplicated
     */
    acf.addMap(seq1.getDatasetSequence(), aseq1.getDatasetSequence(), map);
    assertEquals(1, acf.getMappingsFromSequence(seq1).size());
    assertSame(before, acf.getMappingsFromSequence(seq1).get(0));
  }

  @Test(groups = { "Functional" })
  public void testGetCoveringMapping()
  {
    SequenceI dna = new Sequence("dna", "acttcaATGGCGGACtaattt");
    SequenceI cds = new Sequence("cds/7-15", "ATGGCGGAC");
    cds.setDatasetSequence(dna);
    SequenceI pep = new Sequence("pep", "MAD");

    /*
     * with null argument or no mappings
     */
    AlignedCodonFrame acf = new AlignedCodonFrame();
    assertNull(acf.getCoveringMapping(null, null));
    assertNull(acf.getCoveringMapping(dna, null));
    assertNull(acf.getCoveringMapping(null, pep));
    assertNull(acf.getCoveringMapping(dna, pep));

    /*
     * with a non-covering mapping e.g. overlapping exon
     */
    MapList map = new MapList(new int[] { 7, 9 }, new int[] { 1, 1 }, 3, 1);
    acf.addMap(dna, pep, map);
    assertNull(acf.getCoveringMapping(dna, pep));

    acf = new AlignedCodonFrame();
    MapList map2 = new MapList(new int[] { 13, 18 }, new int[] { 2, 2 }, 3,
            1);
    acf.addMap(dna, pep, map2);
    assertNull(acf.getCoveringMapping(dna, pep));

    /*
     * with a covering mapping from CDS (dataset) to protein
     */
    acf = new AlignedCodonFrame();
    MapList map3 = new MapList(new int[] { 7, 15 }, new int[] { 1, 3 }, 3,
            1);
    acf.addMap(dna, pep, map3);
    assertNull(acf.getCoveringMapping(dna, pep));
    SequenceToSequenceMapping mapping = acf.getCoveringMapping(cds, pep);
    assertNotNull(mapping);

    /*
     * with a mapping that extends to stop codon
     */
    acf = new AlignedCodonFrame();
    MapList map4 = new MapList(new int[] { 7, 18 }, new int[] { 1, 3 }, 3,
            1);
    acf.addMap(dna, pep, map4);
    assertNull(acf.getCoveringMapping(dna, pep));
    assertNull(acf.getCoveringMapping(cds, pep));
    SequenceI cds2 = new Sequence("cds/7-18", "ATGGCGGACtaa");
    cds2.setDatasetSequence(dna);
    mapping = acf.getCoveringMapping(cds2, pep);
    assertNotNull(mapping);
  }

  /**
   * Test the method that adds mapped positions to SearchResults
   */
  @Test(groups = { "Functional" })
  public void testMarkMappedRegion()
  {
    // introns lower case, exons upper case
    final Sequence dna1 = new Sequence("Seq1/10-18", "c-G-TA-gC-gT-T");
    dna1.createDatasetSequence();
    final Sequence dna2 = new Sequence("Seq2/20-28", "-TA-gG-Gg-CG-a");
    dna2.createDatasetSequence();

    final Sequence pep1 = new Sequence("Seq1/3-4", "-P-R");
    pep1.createDatasetSequence();
    final Sequence pep2 = new Sequence("Seq2/7-9", "-LY-Q");
    pep2.createDatasetSequence();

    /*
     * First with no mappings
     */
    AlignedCodonFrame acf = new AlignedCodonFrame();
    SearchResults sr = new SearchResults();
    acf.markMappedRegion(dna1, 12, sr);
    assertTrue(sr.isEmpty());

    /*
     * Set up the mappings for the exons (upper-case bases)
     * Note residue Q is unmapped
     */
    MapList map1 = new MapList(new int[] { 11, 13, 15, 15, 17, 18 },
            new int[]
            { 3, 4 }, 3, 1);
    acf.addMap(dna1.getDatasetSequence(), pep1.getDatasetSequence(), map1);
    MapList map2 = new MapList(new int[] { 20, 21, 23, 24, 26, 27 },
            new int[]
            { 7, 8 }, 3, 1);
    acf.addMap(dna2.getDatasetSequence(), pep2.getDatasetSequence(), map2);

    /*
     * intron bases are not mapped
     */
    acf.markMappedRegion(dna1, 10, sr);
    assertTrue(sr.isEmpty());

    /*
     * Q is not mapped
     */
    acf.markMappedRegion(pep2, 9, sr);
    assertTrue(sr.isEmpty());

    /*
     * mark peptide position for exon position (of aligned sequence)
     */
    acf.markMappedRegion(dna1, 11, sr);
    SearchResults expected = new SearchResults();
    expected.addResult(pep1.getDatasetSequence(), 3, 3);
    assertEquals(sr, expected);

    /*
     * mark peptide position for exon position of dataset sequence - same result
     */
    sr = new SearchResults();
    acf.markMappedRegion(dna1.getDatasetSequence(), 11, sr);
    assertEquals(sr, expected);

    /*
     * marking the same position a second time should not create a duplicate match
     */
    acf.markMappedRegion(dna1.getDatasetSequence(), 12, sr);
    assertEquals(sr, expected);

    /*
     * mark exon positions for peptide position (of aligned sequence)
     */
    sr = new SearchResults();
    acf.markMappedRegion(pep2, 7, sr); // codon positions 20, 21, 23
    expected = new SearchResults();
    expected.addResult(dna2.getDatasetSequence(), 20, 21);
    expected.addResult(dna2.getDatasetSequence(), 23, 23);
    assertEquals(sr, expected);

    /*
     * add another codon to the same SearchResults
     */
    acf.markMappedRegion(pep1.getDatasetSequence(), 4, sr); // codon positions
                                                            // 15, 17, 18
    expected.addResult(dna1.getDatasetSequence(), 15, 15);
    expected.addResult(dna1.getDatasetSequence(), 17, 18);
    assertEquals(sr, expected);
  }

  @Test(groups = { "Functional" })
  public void testGetCoveringCodonMapping()
  {
    SequenceI dna = new Sequence("dna/10-30", "acttcaATGGCGGACtaattt");
    // CDS sequence with its own dataset sequence (JAL-3763)
    SequenceI cds = new Sequence("cds/1-9", "-A--TGGC-GGAC");
    cds.createDatasetSequence();
    SequenceI pep = new Sequence("pep/1-3", "MAD");

    /*
     * with null argument or no mappings
     */
    AlignedCodonFrame acf = new AlignedCodonFrame();
    assertNull(acf.getCoveringCodonMapping(null));
    assertNull(acf.getCoveringCodonMapping(dna));
    assertNull(acf.getCoveringCodonMapping(pep));

    /*
     * with a non-covering mapping e.g. overlapping exon
     */
    MapList map = new MapList(new int[] { 16, 18 }, new int[] { 1, 1 }, 3,
            1);
    acf.addMap(dna, pep, map);
    assertNull(acf.getCoveringCodonMapping(dna));
    assertNull(acf.getCoveringCodonMapping(pep));

    acf = new AlignedCodonFrame();
    MapList map2 = new MapList(new int[] { 13, 18 }, new int[] { 2, 2 }, 3,
            1);
    acf.addMap(dna, pep, map2);
    assertNull(acf.getCoveringCodonMapping(dna));
    assertNull(acf.getCoveringCodonMapping(pep));

    /*
     * with a covering mapping from CDS (dataset) to protein
     */
    acf = new AlignedCodonFrame();
    MapList map3 = new MapList(new int[] { 1, 9 }, new int[] { 1, 3 }, 3,
            1);
    acf.addMap(cds.getDatasetSequence(), pep, map3);
    assertNull(acf.getCoveringCodonMapping(dna));
    SequenceToSequenceMapping mapping = acf.getCoveringCodonMapping(pep);
    assertNotNull(mapping);
    SequenceToSequenceMapping mapping2 = acf
            .getCoveringCodonMapping(cds.getDatasetSequence());
    assertSame(mapping, mapping2);

    /*
     * with a mapping that extends to stop codon
     * (EMBL CDS location often includes the stop codon)
     * - getCoveringCodonMapping is lenient (doesn't require exact length match)
     */
    SequenceI cds2 = new Sequence("cds/1-12", "-A--TGGC-GGACTAA");
    cds2.createDatasetSequence();
    acf = new AlignedCodonFrame();
    MapList map4 = new MapList(new int[] { 1, 12 }, new int[] { 1, 3 }, 3,
            1);
    acf.addMap(cds2, pep, map4);
    mapping = acf.getCoveringCodonMapping(cds2.getDatasetSequence());
    assertNotNull(mapping);
    mapping2 = acf.getCoveringCodonMapping(pep);
    assertSame(mapping, mapping2);
  }
}
