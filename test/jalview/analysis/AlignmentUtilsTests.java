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

import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.ContactListI;
import jalview.datamodel.ContactMatrixI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.GeneLociI;
import jalview.datamodel.Mapping;
import jalview.datamodel.SearchResultMatchI;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SeqDistanceContactMatrix;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;
import jalview.io.AppletFormatAdapter;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.FileFormatI;
import jalview.io.FormatAdapter;
import jalview.io.gff.SequenceOntologyI;
import jalview.util.Comparison;
import jalview.util.MapList;
import jalview.util.MappingUtils;

public class AlignmentUtilsTests
{
  private static Sequence ts = new Sequence("short",
          "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklm");

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testExpandContext()
  {
    AlignmentI al = new Alignment(new Sequence[] {});
    for (int i = 4; i < 14; i += 2)
    {
      SequenceI s1 = ts.deriveSequence().getSubSequence(i, i + 7);
      al.addSequence(s1);
    }
    System.out.println(new AppletFormatAdapter()
            .formatSequences(FileFormat.Clustal, al, true));
    for (int flnk = -1; flnk < 25; flnk++)
    {
      AlignmentI exp = AlignmentUtils.expandContext(al, flnk);
      System.out.println("\nFlank size: " + flnk);
      System.out.println(new AppletFormatAdapter()
              .formatSequences(FileFormat.Clustal, exp, true));
      if (flnk == -1)
      {
        /*
         * Full expansion to complete sequences
         */
        for (SequenceI sq : exp.getSequences())
        {
          String ung = sq.getSequenceAsString().replaceAll("-+", "");
          final String errorMsg = "Flanking sequence not the same as original dataset sequence.\n"
                  + ung + "\n"
                  + sq.getDatasetSequence().getSequenceAsString();
          assertTrue(errorMsg, ung.equalsIgnoreCase(
                  sq.getDatasetSequence().getSequenceAsString()));
        }
      }
      else if (flnk == 24)
      {
        /*
         * Last sequence is fully expanded, others have leading gaps to match
         */
        assertTrue(exp.getSequenceAt(4).getSequenceAsString()
                .startsWith("abc"));
        assertTrue(exp.getSequenceAt(3).getSequenceAsString()
                .startsWith("--abc"));
        assertTrue(exp.getSequenceAt(2).getSequenceAsString()
                .startsWith("----abc"));
        assertTrue(exp.getSequenceAt(1).getSequenceAsString()
                .startsWith("------abc"));
        assertTrue(exp.getSequenceAt(0).getSequenceAsString()
                .startsWith("--------abc"));
      }
    }
  }

  /**
   * Test that annotations are correctly adjusted by expandContext
   */
  @Test(groups = { "Functional" })
  public void testExpandContext_annotation()
  {
    AlignmentI al = new Alignment(new Sequence[] {});
    SequenceI ds = new Sequence("Seq1", "ABCDEFGHI");
    // subsequence DEF:
    SequenceI seq1 = ds.deriveSequence().getSubSequence(3, 6);
    al.addSequence(seq1);

    /*
     * Annotate DEF with 4/5/6 respectively
     */
    Annotation[] anns = new Annotation[] { new Annotation(4),
        new Annotation(5), new Annotation(6) };
    AlignmentAnnotation ann = new AlignmentAnnotation("SS",
            "secondary structure", anns);
    seq1.addAlignmentAnnotation(ann);

    /*
     * The annotations array should match aligned positions
     */
    assertEquals(3, ann.annotations.length);
    assertEquals(4, ann.annotations[0].value, 0.001);
    assertEquals(5, ann.annotations[1].value, 0.001);
    assertEquals(6, ann.annotations[2].value, 0.001);

    /*
     * Check annotation to sequence position mappings before expanding the
     * sequence; these are set up in Sequence.addAlignmentAnnotation ->
     * Annotation.setSequenceRef -> createSequenceMappings
     */
    assertNull(ann.getAnnotationForPosition(1));
    assertNull(ann.getAnnotationForPosition(2));
    assertNull(ann.getAnnotationForPosition(3));
    assertEquals(4, ann.getAnnotationForPosition(4).value, 0.001);
    assertEquals(5, ann.getAnnotationForPosition(5).value, 0.001);
    assertEquals(6, ann.getAnnotationForPosition(6).value, 0.001);
    assertNull(ann.getAnnotationForPosition(7));
    assertNull(ann.getAnnotationForPosition(8));
    assertNull(ann.getAnnotationForPosition(9));

    /*
     * Expand the subsequence to the full sequence abcDEFghi
     */
    AlignmentI expanded = AlignmentUtils.expandContext(al, -1);
    assertEquals("abcDEFghi",
            expanded.getSequenceAt(0).getSequenceAsString());

    /*
     * Confirm the alignment and sequence have the same SS annotation,
     * referencing the expanded sequence
     */
    ann = expanded.getSequenceAt(0).getAnnotation()[0];
    assertSame(ann, expanded.getAlignmentAnnotation()[0]);
    assertSame(expanded.getSequenceAt(0), ann.sequenceRef);

    /*
     * The annotations array should have null values except for annotated
     * positions
     */
    assertNull(ann.annotations[0]);
    assertNull(ann.annotations[1]);
    assertNull(ann.annotations[2]);
    assertEquals(4, ann.annotations[3].value, 0.001);
    assertEquals(5, ann.annotations[4].value, 0.001);
    assertEquals(6, ann.annotations[5].value, 0.001);
    assertNull(ann.annotations[6]);
    assertNull(ann.annotations[7]);
    assertNull(ann.annotations[8]);

    /*
     * sequence position mappings should be unchanged
     */
    assertNull(ann.getAnnotationForPosition(1));
    assertNull(ann.getAnnotationForPosition(2));
    assertNull(ann.getAnnotationForPosition(3));
    assertEquals(4, ann.getAnnotationForPosition(4).value, 0.001);
    assertEquals(5, ann.getAnnotationForPosition(5).value, 0.001);
    assertEquals(6, ann.getAnnotationForPosition(6).value, 0.001);
    assertNull(ann.getAnnotationForPosition(7));
    assertNull(ann.getAnnotationForPosition(8));
    assertNull(ann.getAnnotationForPosition(9));
  }

  /**
   * Test method that returns a map of lists of sequences by sequence name.
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testGetSequencesByName() throws IOException
  {
    final String data = ">Seq1Name\nKQYL\n" + ">Seq2Name\nRFPW\n"
            + ">Seq1Name\nABCD\n";
    AlignmentI al = loadAlignment(data, FileFormat.Fasta);
    Map<String, List<SequenceI>> map = AlignmentUtils
            .getSequencesByName(al);
    assertEquals(2, map.keySet().size());
    assertEquals(2, map.get("Seq1Name").size());
    assertEquals("KQYL", map.get("Seq1Name").get(0).getSequenceAsString());
    assertEquals("ABCD", map.get("Seq1Name").get(1).getSequenceAsString());
    assertEquals(1, map.get("Seq2Name").size());
    assertEquals("RFPW", map.get("Seq2Name").get(0).getSequenceAsString());
  }

  /**
   * Helper method to load an alignment and ensure dataset sequences are set up.
   * 
   * @param data
   * @param format
   *          TODO
   * @return
   * @throws IOException
   */
  protected AlignmentI loadAlignment(final String data, FileFormatI format)
          throws IOException
  {
    AlignmentI a = new FormatAdapter().readFile(data, DataSourceType.PASTE,
            format);
    a.setDataset(null);
    return a;
  }

  /**
   * Test mapping of protein to cDNA, for the case where we have no sequence
   * cross-references, so mappings are made first-served 1-1 where sequences
   * translate.
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testMapProteinAlignmentToCdna_noXrefs() throws IOException
  {
    List<SequenceI> protseqs = new ArrayList<>();
    protseqs.add(new Sequence("UNIPROT|V12345", "EIQ"));
    protseqs.add(new Sequence("UNIPROT|V12346", "EIQ"));
    protseqs.add(new Sequence("UNIPROT|V12347", "SAR"));
    AlignmentI protein = new Alignment(protseqs.toArray(new SequenceI[3]));
    protein.setDataset(null);

    List<SequenceI> dnaseqs = new ArrayList<>();
    dnaseqs.add(new Sequence("EMBL|A11111", "TCAGCACGC")); // = SAR
    dnaseqs.add(new Sequence("EMBL|A22222", "GAGATACAA")); // = EIQ
    dnaseqs.add(new Sequence("EMBL|A33333", "GAAATCCAG")); // = EIQ
    dnaseqs.add(new Sequence("EMBL|A44444", "GAAATTCAG")); // = EIQ
    AlignmentI cdna = new Alignment(dnaseqs.toArray(new SequenceI[4]));
    cdna.setDataset(null);

    assertTrue(AlignmentUtils.mapProteinAlignmentToCdna(protein, cdna));

    // 3 mappings made, each from 1 to 1 sequence
    assertEquals(3, protein.getCodonFrames().size());
    assertEquals(1, protein.getCodonFrame(protein.getSequenceAt(0)).size());
    assertEquals(1, protein.getCodonFrame(protein.getSequenceAt(1)).size());
    assertEquals(1, protein.getCodonFrame(protein.getSequenceAt(2)).size());

    // V12345 mapped to A22222
    AlignedCodonFrame acf = protein.getCodonFrame(protein.getSequenceAt(0))
            .get(0);
    assertEquals(1, acf.getdnaSeqs().length);
    assertEquals(cdna.getSequenceAt(1).getDatasetSequence(),
            acf.getdnaSeqs()[0]);
    Mapping[] protMappings = acf.getProtMappings();
    assertEquals(1, protMappings.length);
    MapList mapList = protMappings[0].getMap();
    assertEquals(3, mapList.getFromRatio());
    assertEquals(1, mapList.getToRatio());
    assertTrue(
            Arrays.equals(new int[]
            { 1, 9 }, mapList.getFromRanges().get(0)));
    assertEquals(1, mapList.getFromRanges().size());
    assertTrue(
            Arrays.equals(new int[]
            { 1, 3 }, mapList.getToRanges().get(0)));
    assertEquals(1, mapList.getToRanges().size());

    // V12346 mapped to A33333
    acf = protein.getCodonFrame(protein.getSequenceAt(1)).get(0);
    assertEquals(1, acf.getdnaSeqs().length);
    assertEquals(cdna.getSequenceAt(2).getDatasetSequence(),
            acf.getdnaSeqs()[0]);

    // V12347 mapped to A11111
    acf = protein.getCodonFrame(protein.getSequenceAt(2)).get(0);
    assertEquals(1, acf.getdnaSeqs().length);
    assertEquals(cdna.getSequenceAt(0).getDatasetSequence(),
            acf.getdnaSeqs()[0]);

    // no mapping involving the 'extra' A44444
    assertTrue(protein.getCodonFrame(cdna.getSequenceAt(3)).isEmpty());
  }

  /**
   * Test for the alignSequenceAs method that takes two sequences and a mapping.
   */
  @Test(groups = { "Functional" })
  public void testAlignSequenceAs_withMapping_noIntrons()
  {
    MapList map = new MapList(new int[] { 1, 6 }, new int[] { 1, 2 }, 3, 1);

    /*
     * No existing gaps in dna:
     */
    checkAlignSequenceAs("GGGAAA", "-A-L-", false, false, map,
            "---GGG---AAA");

    /*
     * Now introduce gaps in dna but ignore them when realigning.
     */
    checkAlignSequenceAs("-G-G-G-A-A-A-", "-A-L-", false, false, map,
            "---GGG---AAA");

    /*
     * Now include gaps in dna when realigning. First retaining 'mapped' gaps
     * only, i.e. those within the exon region.
     */
    checkAlignSequenceAs("-G-G--G-A--A-A-", "-A-L-", true, false, map,
            "---G-G--G---A--A-A");

    /*
     * Include all gaps in dna when realigning (within and without the exon
     * region). The leading gap, and the gaps between codons, are subsumed by
     * the protein alignment gap.
     */
    checkAlignSequenceAs("-G-GG--AA-A---", "-A-L-", true, true, map,
            "---G-GG---AA-A---");

    /*
     * Include only unmapped gaps in dna when realigning (outside the exon
     * region). The leading gap, and the gaps between codons, are subsumed by
     * the protein alignment gap.
     */
    checkAlignSequenceAs("-G-GG--AA-A-", "-A-L-", false, true, map,
            "---GGG---AAA---");
  }

  /**
   * Test for the alignSequenceAs method that takes two sequences and a mapping.
   */
  @Test(groups = { "Functional" })
  public void testAlignSequenceAs_withMapping_withIntrons()
  {
    /*
     * Exons at codon 2 (AAA) and 4 (TTT)
     */
    MapList map = new MapList(new int[] { 4, 6, 10, 12 },
            new int[]
            { 1, 2 }, 3, 1);

    /*
     * Simple case: no gaps in dna
     */
    checkAlignSequenceAs("GGGAAACCCTTTGGG", "--A-L-", false, false, map,
            "GGG---AAACCCTTTGGG");

    /*
     * Add gaps to dna - but ignore when realigning.
     */
    checkAlignSequenceAs("-G-G-G--A--A---AC-CC-T-TT-GG-G-", "--A-L-", false,
            false, map, "GGG---AAACCCTTTGGG");

    /*
     * Add gaps to dna - include within exons only when realigning.
     */
    checkAlignSequenceAs("-G-G-G--A--A---A-C-CC-T-TT-GG-G-", "--A-L-", true,
            false, map, "GGG---A--A---ACCCT-TTGGG");

    /*
     * Include gaps outside exons only when realigning.
     */
    checkAlignSequenceAs("-G-G-G--A--A---A-C-CC-T-TT-GG-G-", "--A-L-",
            false, true, map, "-G-G-GAAAC-CCTTT-GG-G-");

    /*
     * Include gaps following first intron if we are 'preserving mapped gaps'
     */
    checkAlignSequenceAs("-G-G-G--A--A---A-C-CC-T-TT-GG-G-", "--A-L-", true,
            true, map, "-G-G-G--A--A---A-C-CC-T-TT-GG-G-");

    /*
     * Include all gaps in dna when realigning.
     */
    checkAlignSequenceAs("-G-G-G--A--A---A-C-CC-T-TT-GG-G-", "--A-L-", true,
            true, map, "-G-G-G--A--A---A-C-CC-T-TT-GG-G-");
  }

  /**
   * Test for the case where not all of the protein sequence is mapped to cDNA.
   */
  @Test(groups = { "Functional" })
  public void testAlignSequenceAs_withMapping_withUnmappedProtein()
  {
    /*
     * Exons at codon 2 (AAA) and 4 (TTT) mapped to A and P
     */
    final MapList map = new MapList(new int[] { 4, 6, 10, 12 },
            new int[]
            { 1, 1, 3, 3 }, 3, 1);

    /*
     * -L- 'aligns' ccc------
     */
    checkAlignSequenceAs("gggAAAcccTTTggg", "-A-L-P-", false, false, map,
            "gggAAAccc------TTTggg");
  }

  /**
   * Helper method that performs and verifies the method under test.
   * 
   * @param alignee
   *          the sequence to be realigned
   * @param alignModel
   *          the sequence whose alignment is to be copied
   * @param preserveMappedGaps
   * @param preserveUnmappedGaps
   * @param map
   * @param expected
   */
  protected void checkAlignSequenceAs(final String alignee,
          final String alignModel, final boolean preserveMappedGaps,
          final boolean preserveUnmappedGaps, MapList map,
          final String expected)
  {
    SequenceI alignMe = new Sequence("Seq1", alignee);
    alignMe.createDatasetSequence();
    SequenceI alignFrom = new Sequence("Seq2", alignModel);
    alignFrom.createDatasetSequence();
    AlignedCodonFrame acf = new AlignedCodonFrame();
    acf.addMap(alignMe.getDatasetSequence(), alignFrom.getDatasetSequence(),
            map);

    AlignmentUtils.alignSequenceAs(alignMe, alignFrom, acf, "---", '-',
            preserveMappedGaps, preserveUnmappedGaps);
    assertEquals(expected, alignMe.getSequenceAsString());
  }

  /**
   * Test for the alignSequenceAs method where we preserve gaps in introns only.
   */
  @Test(groups = { "Functional" })
  public void testAlignSequenceAs_keepIntronGapsOnly()
  {

    /*
     * Intron GGGAAA followed by exon CCCTTT
     */
    MapList map = new MapList(new int[] { 7, 12 }, new int[] { 1, 2 }, 3,
            1);

    checkAlignSequenceAs("GG-G-AA-A-C-CC-T-TT", "AL", false, true, map,
            "GG-G-AA-ACCCTTT");
  }

  /**
   * Test the method that realigns protein to match mapped codon alignment.
   */
  @Test(groups = { "Functional" })
  public void testAlignProteinAsDna()
  {
    // seq1 codons are [1,2,3] [4,5,6] [7,8,9] [10,11,12]
    SequenceI dna1 = new Sequence("Seq1", "TGCCATTACCAG-");
    // seq2 codons are [1,3,4] [5,6,7] [8,9,10] [11,12,13]
    SequenceI dna2 = new Sequence("Seq2", "T-GCCATTACCAG");
    // seq3 codons are [1,2,3] [4,5,7] [8,9,10] [11,12,13]
    SequenceI dna3 = new Sequence("Seq3", "TGCCA-TTACCAG");
    AlignmentI dna = new Alignment(new SequenceI[] { dna1, dna2, dna3 });
    dna.setDataset(null);

    // protein alignment will be realigned like dna
    SequenceI prot1 = new Sequence("Seq1", "CHYQ");
    SequenceI prot2 = new Sequence("Seq2", "CHYQ");
    SequenceI prot3 = new Sequence("Seq3", "CHYQ");
    SequenceI prot4 = new Sequence("Seq4", "R-QSV"); // unmapped, unchanged
    AlignmentI protein = new Alignment(
            new SequenceI[]
            { prot1, prot2, prot3, prot4 });
    protein.setDataset(null);

    MapList map = new MapList(new int[] { 1, 12 }, new int[] { 1, 4 }, 3,
            1);
    AlignedCodonFrame acf = new AlignedCodonFrame();
    acf.addMap(dna1.getDatasetSequence(), prot1.getDatasetSequence(), map);
    acf.addMap(dna2.getDatasetSequence(), prot2.getDatasetSequence(), map);
    acf.addMap(dna3.getDatasetSequence(), prot3.getDatasetSequence(), map);
    ArrayList<AlignedCodonFrame> acfs = new ArrayList<>();
    acfs.add(acf);
    protein.setCodonFrames(acfs);

    /*
     * Translated codon order is [1,2,3] [1,3,4] [4,5,6] [4,5,7] [5,6,7] [7,8,9]
     * [8,9,10] [10,11,12] [11,12,13]
     */
    AlignmentUtils.alignProteinAsDna(protein, dna);
    assertEquals("C-H--Y-Q-", prot1.getSequenceAsString());
    assertEquals("-C--H-Y-Q", prot2.getSequenceAsString());
    assertEquals("C--H--Y-Q", prot3.getSequenceAsString());
    assertEquals("R-QSV", prot4.getSequenceAsString());
  }

  /**
   * Test the method that tests whether a CDNA sequence translates to a protein
   * sequence
   */
  @Test(groups = { "Functional" })
  public void testTranslatesAs()
  {
    // null arguments check
    assertFalse(AlignmentUtils.translatesAs(null, 0, null));
    assertFalse(AlignmentUtils.translatesAs(new char[] { 't' }, 0, null));
    assertFalse(AlignmentUtils.translatesAs(null, 0, new char[] { 'a' }));

    // straight translation
    assertTrue(AlignmentUtils.translatesAs("tttcccaaaggg".toCharArray(), 0,
            "FPKG".toCharArray()));
    // with extra start codon (not in protein)
    assertTrue(AlignmentUtils.translatesAs("atgtttcccaaaggg".toCharArray(),
            3, "FPKG".toCharArray()));
    // with stop codon1 (not in protein)
    assertTrue(AlignmentUtils.translatesAs("tttcccaaagggtaa".toCharArray(),
            0, "FPKG".toCharArray()));
    // with stop codon1 (in protein as *)
    assertTrue(AlignmentUtils.translatesAs("tttcccaaagggtaa".toCharArray(),
            0, "FPKG*".toCharArray()));
    // with stop codon2 (not in protein)
    assertTrue(AlignmentUtils.translatesAs("tttcccaaagggtag".toCharArray(),
            0, "FPKG".toCharArray()));
    // with stop codon3 (not in protein)
    assertTrue(AlignmentUtils.translatesAs("tttcccaaagggtga".toCharArray(),
            0, "FPKG".toCharArray()));
    // with start and stop codon1
    assertTrue(AlignmentUtils.translatesAs(
            "atgtttcccaaagggtaa".toCharArray(), 3, "FPKG".toCharArray()));
    // with start and stop codon1 (in protein as *)
    assertTrue(AlignmentUtils.translatesAs(
            "atgtttcccaaagggtaa".toCharArray(), 3, "FPKG*".toCharArray()));
    // with start and stop codon2
    assertTrue(AlignmentUtils.translatesAs(
            "atgtttcccaaagggtag".toCharArray(), 3, "FPKG".toCharArray()));
    // with start and stop codon3
    assertTrue(AlignmentUtils.translatesAs(
            "atgtttcccaaagggtga".toCharArray(), 3, "FPKG".toCharArray()));

    // with embedded stop codons
    assertTrue(AlignmentUtils.translatesAs(
            "atgtttTAGcccaaaTAAgggtga".toCharArray(), 3,
            "F*PK*G".toCharArray()));

    // wrong protein
    assertFalse(AlignmentUtils.translatesAs("tttcccaaaggg".toCharArray(), 0,
            "FPMG".toCharArray()));

    // truncated dna
    assertFalse(AlignmentUtils.translatesAs("tttcccaaagg".toCharArray(), 0,
            "FPKG".toCharArray()));

    // truncated protein
    assertFalse(AlignmentUtils.translatesAs("tttcccaaaggg".toCharArray(), 0,
            "FPK".toCharArray()));

    // overlong dna (doesn't end in stop codon)
    assertFalse(AlignmentUtils.translatesAs("tttcccaaagggttt".toCharArray(),
            0, "FPKG".toCharArray()));

    // dna + stop codon + more
    assertFalse(AlignmentUtils.translatesAs(
            "tttcccaaagggttaga".toCharArray(), 0, "FPKG".toCharArray()));

    // overlong protein
    assertFalse(AlignmentUtils.translatesAs("tttcccaaaggg".toCharArray(), 0,
            "FPKGQ".toCharArray()));
  }

  /**
   * Test mapping of protein to cDNA, for cases where the cDNA has start and/or
   * stop codons in addition to the protein coding sequence.
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testMapProteinAlignmentToCdna_withStartAndStopCodons()
          throws IOException
  {
    List<SequenceI> protseqs = new ArrayList<>();
    protseqs.add(new Sequence("UNIPROT|V12345", "EIQ"));
    protseqs.add(new Sequence("UNIPROT|V12346", "EIQ"));
    protseqs.add(new Sequence("UNIPROT|V12347", "SAR"));
    AlignmentI protein = new Alignment(protseqs.toArray(new SequenceI[3]));
    protein.setDataset(null);

    List<SequenceI> dnaseqs = new ArrayList<>();
    // start + SAR:
    dnaseqs.add(new Sequence("EMBL|A11111", "ATGTCAGCACGC"));
    // = EIQ + stop
    dnaseqs.add(new Sequence("EMBL|A22222", "GAGATACAATAA"));
    // = start +EIQ + stop
    dnaseqs.add(new Sequence("EMBL|A33333", "ATGGAAATCCAGTAG"));
    dnaseqs.add(new Sequence("EMBL|A44444", "GAAATTCAG"));
    AlignmentI cdna = new Alignment(dnaseqs.toArray(new SequenceI[4]));
    cdna.setDataset(null);

    assertTrue(AlignmentUtils.mapProteinAlignmentToCdna(protein, cdna));

    // 3 mappings made, each from 1 to 1 sequence
    assertEquals(3, protein.getCodonFrames().size());
    assertEquals(1, protein.getCodonFrame(protein.getSequenceAt(0)).size());
    assertEquals(1, protein.getCodonFrame(protein.getSequenceAt(1)).size());
    assertEquals(1, protein.getCodonFrame(protein.getSequenceAt(2)).size());

    // V12345 mapped from A22222
    AlignedCodonFrame acf = protein.getCodonFrame(protein.getSequenceAt(0))
            .get(0);
    assertEquals(1, acf.getdnaSeqs().length);
    assertEquals(cdna.getSequenceAt(1).getDatasetSequence(),
            acf.getdnaSeqs()[0]);
    Mapping[] protMappings = acf.getProtMappings();
    assertEquals(1, protMappings.length);
    MapList mapList = protMappings[0].getMap();
    assertEquals(3, mapList.getFromRatio());
    assertEquals(1, mapList.getToRatio());
    assertTrue(
            Arrays.equals(new int[]
            { 1, 9 }, mapList.getFromRanges().get(0)));
    assertEquals(1, mapList.getFromRanges().size());
    assertTrue(
            Arrays.equals(new int[]
            { 1, 3 }, mapList.getToRanges().get(0)));
    assertEquals(1, mapList.getToRanges().size());

    // V12346 mapped from A33333 starting position 4
    acf = protein.getCodonFrame(protein.getSequenceAt(1)).get(0);
    assertEquals(1, acf.getdnaSeqs().length);
    assertEquals(cdna.getSequenceAt(2).getDatasetSequence(),
            acf.getdnaSeqs()[0]);
    protMappings = acf.getProtMappings();
    assertEquals(1, protMappings.length);
    mapList = protMappings[0].getMap();
    assertEquals(3, mapList.getFromRatio());
    assertEquals(1, mapList.getToRatio());
    assertTrue(
            Arrays.equals(new int[]
            { 4, 12 }, mapList.getFromRanges().get(0)));
    assertEquals(1, mapList.getFromRanges().size());
    assertTrue(
            Arrays.equals(new int[]
            { 1, 3 }, mapList.getToRanges().get(0)));
    assertEquals(1, mapList.getToRanges().size());

    // V12347 mapped to A11111 starting position 4
    acf = protein.getCodonFrame(protein.getSequenceAt(2)).get(0);
    assertEquals(1, acf.getdnaSeqs().length);
    assertEquals(cdna.getSequenceAt(0).getDatasetSequence(),
            acf.getdnaSeqs()[0]);
    protMappings = acf.getProtMappings();
    assertEquals(1, protMappings.length);
    mapList = protMappings[0].getMap();
    assertEquals(3, mapList.getFromRatio());
    assertEquals(1, mapList.getToRatio());
    assertTrue(
            Arrays.equals(new int[]
            { 4, 12 }, mapList.getFromRanges().get(0)));
    assertEquals(1, mapList.getFromRanges().size());
    assertTrue(
            Arrays.equals(new int[]
            { 1, 3 }, mapList.getToRanges().get(0)));
    assertEquals(1, mapList.getToRanges().size());

    // no mapping involving the 'extra' A44444
    assertTrue(protein.getCodonFrame(cdna.getSequenceAt(3)).isEmpty());
  }

  /**
   * Test mapping of protein to cDNA, for the case where we have some sequence
   * cross-references. Verify that 1-to-many mappings are made where
   * cross-references exist and sequences are mappable.
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testMapProteinAlignmentToCdna_withXrefs() throws IOException
  {
    List<SequenceI> protseqs = new ArrayList<>();
    protseqs.add(new Sequence("UNIPROT|V12345", "EIQ"));
    protseqs.add(new Sequence("UNIPROT|V12346", "EIQ"));
    protseqs.add(new Sequence("UNIPROT|V12347", "SAR"));
    AlignmentI protein = new Alignment(protseqs.toArray(new SequenceI[3]));
    protein.setDataset(null);

    List<SequenceI> dnaseqs = new ArrayList<>();
    dnaseqs.add(new Sequence("EMBL|A11111", "TCAGCACGC")); // = SAR
    dnaseqs.add(new Sequence("EMBL|A22222", "ATGGAGATACAA")); // = start + EIQ
    dnaseqs.add(new Sequence("EMBL|A33333", "GAAATCCAG")); // = EIQ
    dnaseqs.add(new Sequence("EMBL|A44444", "GAAATTCAG")); // = EIQ
    dnaseqs.add(new Sequence("EMBL|A55555", "GAGATTCAG")); // = EIQ
    AlignmentI cdna = new Alignment(dnaseqs.toArray(new SequenceI[5]));
    cdna.setDataset(null);

    // Xref A22222 to V12345 (should get mapped)
    dnaseqs.get(1).addDBRef(new DBRefEntry("UNIPROT", "1", "V12345"));
    // Xref V12345 to A44444 (should get mapped)
    protseqs.get(0).addDBRef(new DBRefEntry("EMBL", "1", "A44444"));
    // Xref A33333 to V12347 (sequence mismatch - should not get mapped)
    dnaseqs.get(2).addDBRef(new DBRefEntry("UNIPROT", "1", "V12347"));
    // as V12345 is mapped to A22222 and A44444, this leaves V12346 unmapped.
    // it should get paired up with the unmapped A33333
    // A11111 should be mapped to V12347
    // A55555 is spare and has no xref so is not mapped

    assertTrue(AlignmentUtils.mapProteinAlignmentToCdna(protein, cdna));

    // 4 protein mappings made for 3 proteins, 2 to V12345, 1 each to V12346/7
    assertEquals(3, protein.getCodonFrames().size());
    assertEquals(1, protein.getCodonFrame(protein.getSequenceAt(0)).size());
    assertEquals(1, protein.getCodonFrame(protein.getSequenceAt(1)).size());
    assertEquals(1, protein.getCodonFrame(protein.getSequenceAt(2)).size());

    // one mapping for each of the first 4 cDNA sequences
    assertEquals(1, protein.getCodonFrame(cdna.getSequenceAt(0)).size());
    assertEquals(1, protein.getCodonFrame(cdna.getSequenceAt(1)).size());
    assertEquals(1, protein.getCodonFrame(cdna.getSequenceAt(2)).size());
    assertEquals(1, protein.getCodonFrame(cdna.getSequenceAt(3)).size());

    // V12345 mapped to A22222 and A44444
    AlignedCodonFrame acf = protein.getCodonFrame(protein.getSequenceAt(0))
            .get(0);
    assertEquals(2, acf.getdnaSeqs().length);
    assertEquals(cdna.getSequenceAt(1).getDatasetSequence(),
            acf.getdnaSeqs()[0]);
    assertEquals(cdna.getSequenceAt(3).getDatasetSequence(),
            acf.getdnaSeqs()[1]);

    // V12346 mapped to A33333
    acf = protein.getCodonFrame(protein.getSequenceAt(1)).get(0);
    assertEquals(1, acf.getdnaSeqs().length);
    assertEquals(cdna.getSequenceAt(2).getDatasetSequence(),
            acf.getdnaSeqs()[0]);

    // V12347 mapped to A11111
    acf = protein.getCodonFrame(protein.getSequenceAt(2)).get(0);
    assertEquals(1, acf.getdnaSeqs().length);
    assertEquals(cdna.getSequenceAt(0).getDatasetSequence(),
            acf.getdnaSeqs()[0]);

    // no mapping involving the 'extra' A55555
    assertTrue(protein.getCodonFrame(cdna.getSequenceAt(4)).isEmpty());
  }

  /**
   * Test mapping of protein to cDNA, for the case where we have some sequence
   * cross-references. Verify that once we have made an xref mapping we don't
   * also map un-xrefd sequeces.
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testMapProteinAlignmentToCdna_prioritiseXrefs()
          throws IOException
  {
    List<SequenceI> protseqs = new ArrayList<>();
    protseqs.add(new Sequence("UNIPROT|V12345", "EIQ"));
    protseqs.add(new Sequence("UNIPROT|V12346", "EIQ"));
    AlignmentI protein = new Alignment(
            protseqs.toArray(new SequenceI[protseqs.size()]));
    protein.setDataset(null);

    List<SequenceI> dnaseqs = new ArrayList<>();
    dnaseqs.add(new Sequence("EMBL|A11111", "GAAATCCAG")); // = EIQ
    dnaseqs.add(new Sequence("EMBL|A22222", "GAAATTCAG")); // = EIQ
    AlignmentI cdna = new Alignment(
            dnaseqs.toArray(new SequenceI[dnaseqs.size()]));
    cdna.setDataset(null);

    // Xref A22222 to V12345 (should get mapped)
    // A11111 should then be mapped to the unmapped V12346
    dnaseqs.get(1).addDBRef(new DBRefEntry("UNIPROT", "1", "V12345"));

    assertTrue(AlignmentUtils.mapProteinAlignmentToCdna(protein, cdna));

    // 2 protein mappings made
    assertEquals(2, protein.getCodonFrames().size());
    assertEquals(1, protein.getCodonFrame(protein.getSequenceAt(0)).size());
    assertEquals(1, protein.getCodonFrame(protein.getSequenceAt(1)).size());

    // one mapping for each of the cDNA sequences
    assertEquals(1, protein.getCodonFrame(cdna.getSequenceAt(0)).size());
    assertEquals(1, protein.getCodonFrame(cdna.getSequenceAt(1)).size());

    // V12345 mapped to A22222
    AlignedCodonFrame acf = protein.getCodonFrame(protein.getSequenceAt(0))
            .get(0);
    assertEquals(1, acf.getdnaSeqs().length);
    assertEquals(cdna.getSequenceAt(1).getDatasetSequence(),
            acf.getdnaSeqs()[0]);

    // V12346 mapped to A11111
    acf = protein.getCodonFrame(protein.getSequenceAt(1)).get(0);
    assertEquals(1, acf.getdnaSeqs().length);
    assertEquals(cdna.getSequenceAt(0).getDatasetSequence(),
            acf.getdnaSeqs()[0]);
  }

  /**
   * Test the method that shows or hides sequence annotations by type(s) and
   * selection group.
   */
  @Test(groups = { "Functional" })
  public void testShowOrHideSequenceAnnotations()
  {
    SequenceI seq1 = new Sequence("Seq1", "AAA");
    SequenceI seq2 = new Sequence("Seq2", "BBB");
    SequenceI seq3 = new Sequence("Seq3", "CCC");
    Annotation[] anns = new Annotation[] { new Annotation(2f) };
    AlignmentAnnotation ann1 = new AlignmentAnnotation("Structure", "ann1",
            anns);
    ann1.setSequenceRef(seq1);
    AlignmentAnnotation ann2 = new AlignmentAnnotation("Structure", "ann2",
            anns);
    ann2.setSequenceRef(seq2);
    AlignmentAnnotation ann3 = new AlignmentAnnotation("Structure", "ann3",
            anns);
    AlignmentAnnotation ann4 = new AlignmentAnnotation("Temp", "ann4",
            anns);
    ann4.setSequenceRef(seq1);
    AlignmentAnnotation ann5 = new AlignmentAnnotation("Temp", "ann5",
            anns);
    ann5.setSequenceRef(seq2);
    AlignmentAnnotation ann6 = new AlignmentAnnotation("Temp", "ann6",
            anns);
    AlignmentI al = new Alignment(new SequenceI[] { seq1, seq2, seq3 });
    al.addAnnotation(ann1); // Structure for Seq1
    al.addAnnotation(ann2); // Structure for Seq2
    al.addAnnotation(ann3); // Structure for no sequence
    al.addAnnotation(ann4); // Temp for seq1
    al.addAnnotation(ann5); // Temp for seq2
    al.addAnnotation(ann6); // Temp for no sequence
    List<String> types = new ArrayList<>();
    List<SequenceI> scope = new ArrayList<>();

    /*
     * Set all sequence related Structure to hidden (ann1, ann2)
     */
    types.add("Structure");
    AlignmentUtils.showOrHideSequenceAnnotations(al, types, null, false,
            false);
    assertFalse(ann1.visible);
    assertFalse(ann2.visible);
    assertTrue(ann3.visible); // not sequence-related, not affected
    assertTrue(ann4.visible); // not Structure, not affected
    assertTrue(ann5.visible); // "
    assertTrue(ann6.visible); // not sequence-related, not affected

    /*
     * Set Temp in {seq1, seq3} to hidden
     */
    types.clear();
    types.add("Temp");
    scope.add(seq1);
    scope.add(seq3);
    AlignmentUtils.showOrHideSequenceAnnotations(al, types, scope, false,
            false);
    assertFalse(ann1.visible); // unchanged
    assertFalse(ann2.visible); // unchanged
    assertTrue(ann3.visible); // not sequence-related, not affected
    assertFalse(ann4.visible); // Temp for seq1 hidden
    assertTrue(ann5.visible); // not in scope, not affected
    assertTrue(ann6.visible); // not sequence-related, not affected

    /*
     * Set Temp in all sequences to hidden
     */
    types.clear();
    types.add("Temp");
    scope.add(seq1);
    scope.add(seq3);
    AlignmentUtils.showOrHideSequenceAnnotations(al, types, null, false,
            false);
    assertFalse(ann1.visible); // unchanged
    assertFalse(ann2.visible); // unchanged
    assertTrue(ann3.visible); // not sequence-related, not affected
    assertFalse(ann4.visible); // Temp for seq1 hidden
    assertFalse(ann5.visible); // Temp for seq2 hidden
    assertTrue(ann6.visible); // not sequence-related, not affected

    /*
     * Set all types in {seq1, seq3} to visible
     */
    types.clear();
    scope.clear();
    scope.add(seq1);
    scope.add(seq3);
    AlignmentUtils.showOrHideSequenceAnnotations(al, types, scope, true,
            true);
    assertTrue(ann1.visible); // Structure for seq1 set visible
    assertFalse(ann2.visible); // not in scope, unchanged
    assertTrue(ann3.visible); // not sequence-related, not affected
    assertTrue(ann4.visible); // Temp for seq1 set visible
    assertFalse(ann5.visible); // not in scope, unchanged
    assertTrue(ann6.visible); // not sequence-related, not affected

    /*
     * Set all types in all scope to hidden
     */
    AlignmentUtils.showOrHideSequenceAnnotations(al, types, null, true,
            false);
    assertFalse(ann1.visible);
    assertFalse(ann2.visible);
    assertTrue(ann3.visible); // not sequence-related, not affected
    assertFalse(ann4.visible);
    assertFalse(ann5.visible);
    assertTrue(ann6.visible); // not sequence-related, not affected
  }

  /**
   * Tests for the method that checks if one sequence cross-references another
   */
  @Test(groups = { "Functional" })
  public void testHasCrossRef()
  {
    assertFalse(AlignmentUtils.hasCrossRef(null, null));
    SequenceI seq1 = new Sequence("EMBL|A12345", "ABCDEF");
    assertFalse(AlignmentUtils.hasCrossRef(seq1, null));
    assertFalse(AlignmentUtils.hasCrossRef(null, seq1));
    SequenceI seq2 = new Sequence("UNIPROT|V20192", "ABCDEF");
    assertFalse(AlignmentUtils.hasCrossRef(seq1, seq2));

    // different ref
    seq1.addDBRef(new DBRefEntry("UNIPROT", "1", "v20193"));
    assertFalse(AlignmentUtils.hasCrossRef(seq1, seq2));

    // case-insensitive; version number is ignored
    seq1.addDBRef(new DBRefEntry("UNIPROT", "1", "v20192"));
    assertTrue(AlignmentUtils.hasCrossRef(seq1, seq2));

    // right case!
    seq1.addDBRef(new DBRefEntry("UNIPROT", "1", "V20192"));
    assertTrue(AlignmentUtils.hasCrossRef(seq1, seq2));
    // test is one-way only
    assertFalse(AlignmentUtils.hasCrossRef(seq2, seq1));
  }

  /**
   * Tests for the method that checks if either sequence cross-references the
   * other
   */
  @Test(groups = { "Functional" })
  public void testHaveCrossRef()
  {
    assertFalse(AlignmentUtils.hasCrossRef(null, null));
    SequenceI seq1 = new Sequence("EMBL|A12345", "ABCDEF");
    assertFalse(AlignmentUtils.haveCrossRef(seq1, null));
    assertFalse(AlignmentUtils.haveCrossRef(null, seq1));
    SequenceI seq2 = new Sequence("UNIPROT|V20192", "ABCDEF");
    assertFalse(AlignmentUtils.haveCrossRef(seq1, seq2));

    seq1.addDBRef(new DBRefEntry("UNIPROT", "1", "V20192"));
    assertTrue(AlignmentUtils.haveCrossRef(seq1, seq2));
    // next is true for haveCrossRef, false for hasCrossRef
    assertTrue(AlignmentUtils.haveCrossRef(seq2, seq1));

    // now the other way round
    seq1.setDBRefs(null);
    seq2.addDBRef(new DBRefEntry("EMBL", "1", "A12345"));
    assertTrue(AlignmentUtils.haveCrossRef(seq1, seq2));
    assertTrue(AlignmentUtils.haveCrossRef(seq2, seq1));

    // now both ways
    seq1.addDBRef(new DBRefEntry("UNIPROT", "1", "V20192"));
    assertTrue(AlignmentUtils.haveCrossRef(seq1, seq2));
    assertTrue(AlignmentUtils.haveCrossRef(seq2, seq1));
  }

  /**
   * Test the method that extracts the cds-only part of a dna alignment.
   */
  @Test(groups = { "Functional" })
  public void testMakeCdsAlignment()
  {
    /*
     * scenario:
     *     dna1 --> [4, 6] [10,12]        --> pep1 
     *     dna2 --> [1, 3] [7, 9] [13,15] --> pep2
     */
    SequenceI dna1 = new Sequence("dna1", "aaaGGGcccTTTaaa");
    SequenceI dna2 = new Sequence("dna2", "GGGcccTTTaaaCCC");
    SequenceI pep1 = new Sequence("pep1", "GF");
    SequenceI pep2 = new Sequence("pep2", "GFP");
    pep1.addDBRef(new DBRefEntry("UNIPROT", "0", "pep1"));
    pep2.addDBRef(new DBRefEntry("UNIPROT", "0", "pep2"));
    dna1.createDatasetSequence();
    dna2.createDatasetSequence();
    pep1.createDatasetSequence();
    pep2.createDatasetSequence();
    AlignmentI dna = new Alignment(new SequenceI[] { dna1, dna2 });
    dna.setDataset(null);

    /*
     * put a variant feature on dna2 base 8
     * - should transfer to cds2 base 5
     */
    dna2.addSequenceFeature(
            new SequenceFeature("variant", "hgmd", 8, 8, 0f, null));

    /*
     * need a sourceDbRef if we are to construct dbrefs to the CDS
     * sequence from the dna contig sequences
     */
    DBRefEntry dbref = new DBRefEntry("ENSEMBL", "0", "dna1");
    dna1.getDatasetSequence().addDBRef(dbref);
    org.testng.Assert.assertEquals(dbref, dna1.getPrimaryDBRefs().get(0));
    dbref = new DBRefEntry("ENSEMBL", "0", "dna2");
    dna2.getDatasetSequence().addDBRef(dbref);
    org.testng.Assert.assertEquals(dbref, dna2.getPrimaryDBRefs().get(0));

    /*
     * CDS sequences are 'discovered' from dna-to-protein mappings on the alignment
     * dataset (e.g. added from dbrefs by CrossRef.findXrefSequences)
     */
    MapList mapfordna1 = new MapList(new int[] { 4, 6, 10, 12 },
            new int[]
            { 1, 2 }, 3, 1);
    AlignedCodonFrame acf = new AlignedCodonFrame();
    acf.addMap(dna1.getDatasetSequence(), pep1.getDatasetSequence(),
            mapfordna1);
    dna.addCodonFrame(acf);
    MapList mapfordna2 = new MapList(new int[] { 1, 3, 7, 9, 13, 15 },
            new int[]
            { 1, 3 }, 3, 1);
    acf = new AlignedCodonFrame();
    acf.addMap(dna2.getDatasetSequence(), pep2.getDatasetSequence(),
            mapfordna2);
    dna.addCodonFrame(acf);

    /*
     * In this case, mappings originally came from matching Uniprot accessions 
     * - so need an xref on dna involving those regions. 
     * These are normally constructed from CDS annotation
     */
    DBRefEntry dna1xref = new DBRefEntry("UNIPROT", "ENSEMBL", "pep1",
            new Mapping(mapfordna1));
    dna1.addDBRef(dna1xref);
    assertEquals(2, dna1.getDBRefs().size()); // to self and to pep1
    DBRefEntry dna2xref = new DBRefEntry("UNIPROT", "ENSEMBL", "pep2",
            new Mapping(mapfordna2));
    dna2.addDBRef(dna2xref);
    assertEquals(2, dna2.getDBRefs().size()); // to self and to pep2

    /*
     * execute method under test:
     */
    AlignmentI cds = AlignmentUtils
            .makeCdsAlignment(new SequenceI[]
            { dna1, dna2 }, dna.getDataset(), null);

    /*
     * verify cds sequences
     */
    assertEquals(2, cds.getSequences().size());
    assertEquals("GGGTTT", cds.getSequenceAt(0).getSequenceAsString());
    assertEquals("GGGTTTCCC", cds.getSequenceAt(1).getSequenceAsString());

    /*
     * verify shared, extended alignment dataset
     */
    assertSame(dna.getDataset(), cds.getDataset());
    SequenceI cds1Dss = cds.getSequenceAt(0).getDatasetSequence();
    SequenceI cds2Dss = cds.getSequenceAt(1).getDatasetSequence();
    assertTrue(dna.getDataset().getSequences().contains(cds1Dss));
    assertTrue(dna.getDataset().getSequences().contains(cds2Dss));

    /*
     * verify CDS has a dbref with mapping to peptide
     */
    assertNotNull(cds1Dss.getDBRefs());
    assertEquals(2, cds1Dss.getDBRefs().size());
    dbref = cds1Dss.getDBRefs().get(0);
    assertEquals(dna1xref.getSource(), dbref.getSource());
    // version is via ensembl's primary ref
    assertEquals(dna1xref.getVersion(), dbref.getVersion());
    assertEquals(dna1xref.getAccessionId(), dbref.getAccessionId());
    assertNotNull(dbref.getMap());
    assertSame(pep1.getDatasetSequence(), dbref.getMap().getTo());
    MapList cdsMapping = new MapList(new int[] { 1, 6 }, new int[] { 1, 2 },
            3, 1);
    assertEquals(cdsMapping, dbref.getMap().getMap());

    /*
     * verify peptide has added a dbref with reverse mapping to CDS
     */
    assertNotNull(pep1.getDBRefs());
    // FIXME pep1.getDBRefs() is 1 - is that the correct behaviour ?
    assertEquals(2, pep1.getDBRefs().size());
    dbref = pep1.getDBRefs().get(1);
    assertEquals("ENSEMBL", dbref.getSource());
    assertEquals("0", dbref.getVersion());
    assertEquals("CDS|dna1", dbref.getAccessionId());
    assertNotNull(dbref.getMap());
    assertSame(cds1Dss, dbref.getMap().getTo());
    assertEquals(cdsMapping.getInverse(), dbref.getMap().getMap());

    /*
     * verify cDNA has added a dbref with mapping to CDS
     */
    assertEquals(3, dna1.getDBRefs().size());
    DBRefEntry dbRefEntry = dna1.getDBRefs().get(2);
    assertSame(cds1Dss, dbRefEntry.getMap().getTo());
    MapList dnaToCdsMapping = new MapList(new int[] { 4, 6, 10, 12 },
            new int[]
            { 1, 6 }, 1, 1);
    assertEquals(dnaToCdsMapping, dbRefEntry.getMap().getMap());
    assertEquals(3, dna2.getDBRefs().size());
    dbRefEntry = dna2.getDBRefs().get(2);
    assertSame(cds2Dss, dbRefEntry.getMap().getTo());
    dnaToCdsMapping = new MapList(new int[] { 1, 3, 7, 9, 13, 15 },
            new int[]
            { 1, 9 }, 1, 1);
    assertEquals(dnaToCdsMapping, dbRefEntry.getMap().getMap());

    /*
     * verify CDS has added a dbref with mapping to cDNA
     */
    assertEquals(2, cds1Dss.getDBRefs().size());
    dbRefEntry = cds1Dss.getDBRefs().get(1);
    assertSame(dna1.getDatasetSequence(), dbRefEntry.getMap().getTo());
    MapList cdsToDnaMapping = new MapList(new int[] { 1, 6 },
            new int[]
            { 4, 6, 10, 12 }, 1, 1);
    assertEquals(cdsToDnaMapping, dbRefEntry.getMap().getMap());
    assertEquals(2, cds2Dss.getDBRefs().size());
    dbRefEntry = cds2Dss.getDBRefs().get(1);
    assertSame(dna2.getDatasetSequence(), dbRefEntry.getMap().getTo());
    cdsToDnaMapping = new MapList(new int[] { 1, 9 },
            new int[]
            { 1, 3, 7, 9, 13, 15 }, 1, 1);
    assertEquals(cdsToDnaMapping, dbRefEntry.getMap().getMap());

    /*
     * Verify mappings from CDS to peptide, cDNA to CDS, and cDNA to peptide
     * the mappings are on the shared alignment dataset
     * 6 mappings, 2*(DNA->CDS), 2*(DNA->Pep), 2*(CDS->Pep) 
     */
    List<AlignedCodonFrame> cdsMappings = cds.getDataset().getCodonFrames();
    assertEquals(6, cdsMappings.size());

    /*
     * verify that mapping sets for dna and cds alignments are different
     * [not current behaviour - all mappings are on the alignment dataset]  
     */
    // select -> subselect type to test.
    // Assert.assertNotSame(dna.getCodonFrames(), cds.getCodonFrames());
    // assertEquals(4, dna.getCodonFrames().size());
    // assertEquals(4, cds.getCodonFrames().size());

    /*
     * Two mappings involve pep1 (dna to pep1, cds to pep1)
     * Mapping from pep1 to GGGTTT in first new exon sequence
     */
    List<AlignedCodonFrame> pep1Mappings = MappingUtils
            .findMappingsForSequence(pep1, cdsMappings);
    assertEquals(2, pep1Mappings.size());
    List<AlignedCodonFrame> mappings = MappingUtils
            .findMappingsForSequence(cds.getSequenceAt(0), pep1Mappings);
    assertEquals(1, mappings.size());

    // map G to GGG
    SearchResultsI sr = MappingUtils.buildSearchResults(pep1, 1, mappings);
    assertEquals(1, sr.getResults().size());
    SearchResultMatchI m = sr.getResults().get(0);
    assertSame(cds1Dss, m.getSequence());
    assertEquals(1, m.getStart());
    assertEquals(3, m.getEnd());
    // map F to TTT
    sr = MappingUtils.buildSearchResults(pep1, 2, mappings);
    m = sr.getResults().get(0);
    assertSame(cds1Dss, m.getSequence());
    assertEquals(4, m.getStart());
    assertEquals(6, m.getEnd());

    /*
     * Two mappings involve pep2 (dna to pep2, cds to pep2)
     * Verify mapping from pep2 to GGGTTTCCC in second new exon sequence
     */
    List<AlignedCodonFrame> pep2Mappings = MappingUtils
            .findMappingsForSequence(pep2, cdsMappings);
    assertEquals(2, pep2Mappings.size());
    mappings = MappingUtils.findMappingsForSequence(cds.getSequenceAt(1),
            pep2Mappings);
    assertEquals(1, mappings.size());
    // map G to GGG
    sr = MappingUtils.buildSearchResults(pep2, 1, mappings);
    assertEquals(1, sr.getResults().size());
    m = sr.getResults().get(0);
    assertSame(cds2Dss, m.getSequence());
    assertEquals(1, m.getStart());
    assertEquals(3, m.getEnd());
    // map F to TTT
    sr = MappingUtils.buildSearchResults(pep2, 2, mappings);
    m = sr.getResults().get(0);
    assertSame(cds2Dss, m.getSequence());
    assertEquals(4, m.getStart());
    assertEquals(6, m.getEnd());
    // map P to CCC
    sr = MappingUtils.buildSearchResults(pep2, 3, mappings);
    m = sr.getResults().get(0);
    assertSame(cds2Dss, m.getSequence());
    assertEquals(7, m.getStart());
    assertEquals(9, m.getEnd());

    /*
     * check cds2 acquired a variant feature in position 5
     */
    List<SequenceFeature> sfs = cds2Dss.getSequenceFeatures();
    assertNotNull(sfs);
    assertEquals(1, sfs.size());
    assertEquals("variant", sfs.get(0).type);
    assertEquals(5, sfs.get(0).begin);
    assertEquals(5, sfs.get(0).end);
  }

  /**
   * Test the method that makes a cds-only alignment from a DNA sequence and its
   * product mappings, for the case where there are multiple exon mappings to
   * different protein products.
   */
  @Test(groups = { "Functional" })
  public void testMakeCdsAlignment_multipleProteins()
  {
    SequenceI dna1 = new Sequence("dna1", "aaaGGGcccTTTaaa");
    SequenceI pep1 = new Sequence("pep1", "GF"); // GGGTTT
    SequenceI pep2 = new Sequence("pep2", "KP"); // aaaccc
    SequenceI pep3 = new Sequence("pep3", "KF"); // aaaTTT
    dna1.createDatasetSequence();
    pep1.createDatasetSequence();
    pep2.createDatasetSequence();
    pep3.createDatasetSequence();
    pep1.getDatasetSequence()
            .addDBRef(new DBRefEntry("EMBLCDS", "2", "A12345"));
    pep2.getDatasetSequence()
            .addDBRef(new DBRefEntry("EMBLCDS", "3", "A12346"));
    pep3.getDatasetSequence()
            .addDBRef(new DBRefEntry("EMBLCDS", "4", "A12347"));

    /*
     * Create the CDS alignment
     */
    AlignmentI dna = new Alignment(new SequenceI[] { dna1 });
    dna.setDataset(null);

    /*
     * Make the mappings from dna to protein
     */
    // map ...GGG...TTT to GF
    MapList map = new MapList(new int[] { 4, 6, 10, 12 },
            new int[]
            { 1, 2 }, 3, 1);
    AlignedCodonFrame acf = new AlignedCodonFrame();
    acf.addMap(dna1.getDatasetSequence(), pep1.getDatasetSequence(), map);
    dna.addCodonFrame(acf);

    // map aaa...ccc to KP
    map = new MapList(new int[] { 1, 3, 7, 9 }, new int[] { 1, 2 }, 3, 1);
    acf = new AlignedCodonFrame();
    acf.addMap(dna1.getDatasetSequence(), pep2.getDatasetSequence(), map);
    dna.addCodonFrame(acf);

    // map aaa......TTT to KF
    map = new MapList(new int[] { 1, 3, 10, 12 }, new int[] { 1, 2 }, 3, 1);
    acf = new AlignedCodonFrame();
    acf.addMap(dna1.getDatasetSequence(), pep3.getDatasetSequence(), map);
    dna.addCodonFrame(acf);

    /*
     * execute method under test
     */
    AlignmentI cdsal = AlignmentUtils
            .makeCdsAlignment(new SequenceI[]
            { dna1 }, dna.getDataset(), null);

    /*
     * Verify we have 3 cds sequences, mapped to pep1/2/3 respectively
     */
    List<SequenceI> cds = cdsal.getSequences();
    assertEquals(3, cds.size());

    /*
     * verify shared, extended alignment dataset
     */
    assertSame(cdsal.getDataset(), dna.getDataset());
    assertTrue(dna.getDataset().getSequences()
            .contains(cds.get(0).getDatasetSequence()));
    assertTrue(dna.getDataset().getSequences()
            .contains(cds.get(1).getDatasetSequence()));
    assertTrue(dna.getDataset().getSequences()
            .contains(cds.get(2).getDatasetSequence()));

    /*
     * verify aligned cds sequences and their xrefs
     */
    SequenceI cdsSeq = cds.get(0);
    assertEquals("GGGTTT", cdsSeq.getSequenceAsString());
    // assertEquals("dna1|A12345", cdsSeq.getName());
    assertEquals("CDS|dna1", cdsSeq.getName());
    // assertEquals(1, cdsSeq.getDBRefs().length);
    // DBRefEntry cdsRef = cdsSeq.getDBRefs()[0];
    // assertEquals("EMBLCDS", cdsRef.getSource());
    // assertEquals("2", cdsRef.getVersion());
    // assertEquals("A12345", cdsRef.getAccessionId());

    cdsSeq = cds.get(1);
    assertEquals("aaaccc", cdsSeq.getSequenceAsString());
    // assertEquals("dna1|A12346", cdsSeq.getName());
    assertEquals("CDS|dna1", cdsSeq.getName());
    // assertEquals(1, cdsSeq.getDBRefs().length);
    // cdsRef = cdsSeq.getDBRefs()[0];
    // assertEquals("EMBLCDS", cdsRef.getSource());
    // assertEquals("3", cdsRef.getVersion());
    // assertEquals("A12346", cdsRef.getAccessionId());

    cdsSeq = cds.get(2);
    assertEquals("aaaTTT", cdsSeq.getSequenceAsString());
    // assertEquals("dna1|A12347", cdsSeq.getName());
    assertEquals("CDS|dna1", cdsSeq.getName());
    // assertEquals(1, cdsSeq.getDBRefs().length);
    // cdsRef = cdsSeq.getDBRefs()[0];
    // assertEquals("EMBLCDS", cdsRef.getSource());
    // assertEquals("4", cdsRef.getVersion());
    // assertEquals("A12347", cdsRef.getAccessionId());

    /*
     * Verify there are mappings from each cds sequence to its protein product
     * and also to its dna source
     */
    List<AlignedCodonFrame> newMappings = cdsal.getCodonFrames();

    /*
     * 6 mappings involve dna1 (to pep1/2/3, cds1/2/3) 
     */
    List<AlignedCodonFrame> dnaMappings = MappingUtils
            .findMappingsForSequence(dna1, newMappings);
    assertEquals(6, dnaMappings.size());

    /*
     * dna1 to pep1
     */
    List<AlignedCodonFrame> mappings = MappingUtils
            .findMappingsForSequence(pep1, dnaMappings);
    assertEquals(1, mappings.size());
    assertEquals(1, mappings.get(0).getMappings().size());
    assertSame(pep1.getDatasetSequence(),
            mappings.get(0).getMappings().get(0).getMapping().getTo());

    /*
     * dna1 to cds1
     */
    List<AlignedCodonFrame> dnaToCds1Mappings = MappingUtils
            .findMappingsForSequence(cds.get(0), dnaMappings);
    Mapping mapping = dnaToCds1Mappings.get(0).getMappings().get(0)
            .getMapping();
    assertSame(cds.get(0).getDatasetSequence(), mapping.getTo());
    assertEquals("G(1) in CDS should map to G(4) in DNA", 4,
            mapping.getMap().getToPosition(1));

    /*
     * dna1 to pep2
     */
    mappings = MappingUtils.findMappingsForSequence(pep2, dnaMappings);
    assertEquals(1, mappings.size());
    assertEquals(1, mappings.get(0).getMappings().size());
    assertSame(pep2.getDatasetSequence(),
            mappings.get(0).getMappings().get(0).getMapping().getTo());

    /*
     * dna1 to cds2
     */
    List<AlignedCodonFrame> dnaToCds2Mappings = MappingUtils
            .findMappingsForSequence(cds.get(1), dnaMappings);
    mapping = dnaToCds2Mappings.get(0).getMappings().get(0).getMapping();
    assertSame(cds.get(1).getDatasetSequence(), mapping.getTo());
    assertEquals("c(4) in CDS should map to c(7) in DNA", 7,
            mapping.getMap().getToPosition(4));

    /*
     * dna1 to pep3
     */
    mappings = MappingUtils.findMappingsForSequence(pep3, dnaMappings);
    assertEquals(1, mappings.size());
    assertEquals(1, mappings.get(0).getMappings().size());
    assertSame(pep3.getDatasetSequence(),
            mappings.get(0).getMappings().get(0).getMapping().getTo());

    /*
     * dna1 to cds3
     */
    List<AlignedCodonFrame> dnaToCds3Mappings = MappingUtils
            .findMappingsForSequence(cds.get(2), dnaMappings);
    mapping = dnaToCds3Mappings.get(0).getMappings().get(0).getMapping();
    assertSame(cds.get(2).getDatasetSequence(), mapping.getTo());
    assertEquals("T(4) in CDS should map to T(10) in DNA", 10,
            mapping.getMap().getToPosition(4));
  }

  @Test(groups = { "Functional" })
  public void testIsMappable()
  {
    SequenceI dna1 = new Sequence("dna1", "cgCAGtgGT");
    SequenceI aa1 = new Sequence("aa1", "RSG");
    AlignmentI al1 = new Alignment(new SequenceI[] { dna1 });
    AlignmentI al2 = new Alignment(new SequenceI[] { aa1 });

    assertFalse(AlignmentUtils.isMappable(null, null));
    assertFalse(AlignmentUtils.isMappable(al1, null));
    assertFalse(AlignmentUtils.isMappable(null, al1));
    assertFalse(AlignmentUtils.isMappable(al1, al1));
    assertFalse(AlignmentUtils.isMappable(al2, al2));

    assertTrue(AlignmentUtils.isMappable(al1, al2));
    assertTrue(AlignmentUtils.isMappable(al2, al1));
  }

  /**
   * Test creating a mapping when the sequences involved do not start at residue
   * 1
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testMapCdnaToProtein_forSubsequence() throws IOException
  {
    SequenceI prot = new Sequence("UNIPROT|V12345", "E-I--Q", 10, 12);
    prot.createDatasetSequence();

    SequenceI dna = new Sequence("EMBL|A33333", "GAA--AT-C-CAG", 40, 48);
    dna.createDatasetSequence();

    MapList map = AlignmentUtils.mapCdnaToProtein(prot, dna);
    assertEquals(10, map.getToLowest());
    assertEquals(12, map.getToHighest());
    assertEquals(40, map.getFromLowest());
    assertEquals(48, map.getFromHighest());
  }

  /**
   * Test for the alignSequenceAs method where we have protein mapped to protein
   */
  @Test(groups = { "Functional" })
  public void testAlignSequenceAs_mappedProteinProtein()
  {

    SequenceI alignMe = new Sequence("Match", "MGAASEV");
    alignMe.createDatasetSequence();
    SequenceI alignFrom = new Sequence("Query", "LQTGYMGAASEVMFSPTRR");
    alignFrom.createDatasetSequence();

    AlignedCodonFrame acf = new AlignedCodonFrame();
    // this is like a domain or motif match of part of a peptide sequence
    MapList map = new MapList(new int[] { 6, 12 }, new int[] { 1, 7 }, 1,
            1);
    acf.addMap(alignFrom.getDatasetSequence(), alignMe.getDatasetSequence(),
            map);

    AlignmentUtils.alignSequenceAs(alignMe, alignFrom, acf, "-", '-', true,
            true);
    assertEquals("-----MGAASEV-------", alignMe.getSequenceAsString());
  }

  /**
   * Test for the alignSequenceAs method where there are trailing unmapped
   * residues in the model sequence
   */
  @Test(groups = { "Functional" })
  public void testAlignSequenceAs_withTrailingPeptide()
  {
    // map first 3 codons to KPF; G is a trailing unmapped residue
    MapList map = new MapList(new int[] { 1, 9 }, new int[] { 1, 3 }, 3, 1);

    checkAlignSequenceAs("AAACCCTTT", "K-PFG", true, true, map,
            "AAA---CCCTTT---");
  }

  /**
   * Tests for transferring features between mapped sequences
   */
  @Test(groups = { "Functional" })
  public void testTransferFeatures()
  {
    SequenceI dna = new Sequence("dna/20-34", "acgTAGcaaGCCcgt");
    SequenceI cds = new Sequence("cds/10-15", "TAGGCC");

    // no overlap
    dna.addSequenceFeature(
            new SequenceFeature("type1", "desc1", 1, 2, 1f, null));
    // partial overlap - to [1, 1]
    dna.addSequenceFeature(
            new SequenceFeature("type2", "desc2", 3, 4, 2f, null));
    // exact overlap - to [1, 3]
    dna.addSequenceFeature(
            new SequenceFeature("type3", "desc3", 4, 6, 3f, null));
    // spanning overlap - to [2, 5]
    dna.addSequenceFeature(
            new SequenceFeature("type4", "desc4", 5, 11, 4f, null));
    // exactly overlaps whole mapped range [1, 6]
    dna.addSequenceFeature(
            new SequenceFeature("type5", "desc5", 4, 12, 5f, null));
    // no overlap (internal)
    dna.addSequenceFeature(
            new SequenceFeature("type6", "desc6", 7, 9, 6f, null));
    // no overlap (3' end)
    dna.addSequenceFeature(
            new SequenceFeature("type7", "desc7", 13, 15, 7f, null));
    // overlap (3' end) - to [6, 6]
    dna.addSequenceFeature(
            new SequenceFeature("type8", "desc8", 12, 12, 8f, null));
    // extended overlap - to [6, +]
    dna.addSequenceFeature(
            new SequenceFeature("type9", "desc9", 12, 13, 9f, null));

    MapList map = new MapList(new int[] { 4, 6, 10, 12 },
            new int[]
            { 1, 6 }, 1, 1);

    /*
     * transferFeatures() will build 'partial overlap' for regions
     * that partially overlap 5' or 3' (start or end) of target sequence
     */
    AlignmentUtils.transferFeatures(dna, cds, map, null);
    List<SequenceFeature> sfs = cds.getSequenceFeatures();
    assertEquals(6, sfs.size());

    SequenceFeature sf = sfs.get(0);
    assertEquals("type2", sf.getType());
    assertEquals("desc2", sf.getDescription());
    assertEquals(2f, sf.getScore());
    assertEquals(1, sf.getBegin());
    assertEquals(1, sf.getEnd());

    sf = sfs.get(1);
    assertEquals("type3", sf.getType());
    assertEquals("desc3", sf.getDescription());
    assertEquals(3f, sf.getScore());
    assertEquals(1, sf.getBegin());
    assertEquals(3, sf.getEnd());

    sf = sfs.get(2);
    assertEquals("type4", sf.getType());
    assertEquals(2, sf.getBegin());
    assertEquals(5, sf.getEnd());

    sf = sfs.get(3);
    assertEquals("type5", sf.getType());
    assertEquals(1, sf.getBegin());
    assertEquals(6, sf.getEnd());

    sf = sfs.get(4);
    assertEquals("type8", sf.getType());
    assertEquals(6, sf.getBegin());
    assertEquals(6, sf.getEnd());

    sf = sfs.get(5);
    assertEquals("type9", sf.getType());
    assertEquals(6, sf.getBegin());
    assertEquals(6, sf.getEnd());
  }

  /**
   * Tests for transferring features between mapped sequences
   */
  @Test(groups = { "Functional" })
  public void testTransferFeatures_withOmit()
  {
    SequenceI dna = new Sequence("dna/20-34", "acgTAGcaaGCCcgt");
    SequenceI cds = new Sequence("cds/10-15", "TAGGCC");

    MapList map = new MapList(new int[] { 4, 6, 10, 12 },
            new int[]
            { 1, 6 }, 1, 1);

    // [5, 11] maps to [2, 5]
    dna.addSequenceFeature(
            new SequenceFeature("type4", "desc4", 5, 11, 4f, null));
    // [4, 12] maps to [1, 6]
    dna.addSequenceFeature(
            new SequenceFeature("type5", "desc5", 4, 12, 5f, null));
    // [12, 12] maps to [6, 6]
    dna.addSequenceFeature(
            new SequenceFeature("type8", "desc8", 12, 12, 8f, null));

    // desc4 and desc8 are the 'omit these' varargs
    AlignmentUtils.transferFeatures(dna, cds, map, null, "type4", "type8");
    List<SequenceFeature> sfs = cds.getSequenceFeatures();
    assertEquals(1, sfs.size());

    SequenceFeature sf = sfs.get(0);
    assertEquals("type5", sf.getType());
    assertEquals(1, sf.getBegin());
    assertEquals(6, sf.getEnd());
  }

  /**
   * Tests for transferring features between mapped sequences
   */
  @Test(groups = { "Functional" })
  public void testTransferFeatures_withSelect()
  {
    SequenceI dna = new Sequence("dna/20-34", "acgTAGcaaGCCcgt");
    SequenceI cds = new Sequence("cds/10-15", "TAGGCC");

    MapList map = new MapList(new int[] { 4, 6, 10, 12 },
            new int[]
            { 1, 6 }, 1, 1);

    // [5, 11] maps to [2, 5]
    dna.addSequenceFeature(
            new SequenceFeature("type4", "desc4", 5, 11, 4f, null));
    // [4, 12] maps to [1, 6]
    dna.addSequenceFeature(
            new SequenceFeature("type5", "desc5", 4, 12, 5f, null));
    // [12, 12] maps to [6, 6]
    dna.addSequenceFeature(
            new SequenceFeature("type8", "desc8", 12, 12, 8f, null));

    // "type5" is the 'select this type' argument
    AlignmentUtils.transferFeatures(dna, cds, map, "type5");
    List<SequenceFeature> sfs = cds.getSequenceFeatures();
    assertEquals(1, sfs.size());

    SequenceFeature sf = sfs.get(0);
    assertEquals("type5", sf.getType());
    assertEquals(1, sf.getBegin());
    assertEquals(6, sf.getEnd());
  }

  /**
   * Test the method that extracts the cds-only part of a dna alignment, for the
   * case where the cds should be aligned to match its nucleotide sequence.
   */
  @Test(groups = { "Functional" })
  public void testMakeCdsAlignment_alternativeTranscripts()
  {
    SequenceI dna1 = new Sequence("dna1", "aaaGGGCC-----CTTTaaaGGG");
    // alternative transcript of same dna skips CCC codon
    SequenceI dna2 = new Sequence("dna2", "aaaGGGCC-----cttTaaaGGG");
    // dna3 has no mapping (protein product) so should be ignored here
    SequenceI dna3 = new Sequence("dna3", "aaaGGGCCCCCGGGcttTaaaGGG");
    SequenceI pep1 = new Sequence("pep1", "GPFG");
    SequenceI pep2 = new Sequence("pep2", "GPG");
    dna1.createDatasetSequence();
    dna2.createDatasetSequence();
    dna3.createDatasetSequence();
    pep1.createDatasetSequence();
    pep2.createDatasetSequence();

    AlignmentI dna = new Alignment(new SequenceI[] { dna1, dna2, dna3 });
    dna.setDataset(null);

    MapList map = new MapList(new int[] { 4, 12, 16, 18 },
            new int[]
            { 1, 4 }, 3, 1);
    AlignedCodonFrame acf = new AlignedCodonFrame();
    acf.addMap(dna1.getDatasetSequence(), pep1.getDatasetSequence(), map);
    dna.addCodonFrame(acf);
    map = new MapList(new int[] { 4, 8, 12, 12, 16, 18 },
            new int[]
            { 1, 3 }, 3, 1);
    acf = new AlignedCodonFrame();
    acf.addMap(dna2.getDatasetSequence(), pep2.getDatasetSequence(), map);
    dna.addCodonFrame(acf);

    AlignmentI cds = AlignmentUtils
            .makeCdsAlignment(new SequenceI[]
            { dna1, dna2, dna3 }, dna.getDataset(), null);
    List<SequenceI> cdsSeqs = cds.getSequences();
    assertEquals(2, cdsSeqs.size());
    assertEquals("GGGCCCTTTGGG", cdsSeqs.get(0).getSequenceAsString());
    assertEquals("GGGCCTGGG", cdsSeqs.get(1).getSequenceAsString());

    /*
     * verify shared, extended alignment dataset
     */
    assertSame(dna.getDataset(), cds.getDataset());
    assertTrue(dna.getDataset().getSequences()
            .contains(cdsSeqs.get(0).getDatasetSequence()));
    assertTrue(dna.getDataset().getSequences()
            .contains(cdsSeqs.get(1).getDatasetSequence()));

    /*
     * Verify 6 mappings: dna1 to cds1, cds1 to pep1, dna1 to pep1
     * and the same for dna2/cds2/pep2
     */
    List<AlignedCodonFrame> mappings = cds.getCodonFrames();
    assertEquals(6, mappings.size());

    /*
     * 2 mappings involve pep1
     */
    List<AlignedCodonFrame> pep1Mappings = MappingUtils
            .findMappingsForSequence(pep1, mappings);
    assertEquals(2, pep1Mappings.size());

    /*
     * Get mapping of pep1 to cds1 and verify it
     * maps GPFG to 1-3,4-6,7-9,10-12
     */
    List<AlignedCodonFrame> pep1CdsMappings = MappingUtils
            .findMappingsForSequence(cds.getSequenceAt(0), pep1Mappings);
    assertEquals(1, pep1CdsMappings.size());
    SearchResultsI sr = MappingUtils.buildSearchResults(pep1, 1,
            pep1CdsMappings);
    assertEquals(1, sr.getResults().size());
    SearchResultMatchI m = sr.getResults().get(0);
    assertEquals(cds.getSequenceAt(0).getDatasetSequence(),
            m.getSequence());
    assertEquals(1, m.getStart());
    assertEquals(3, m.getEnd());
    sr = MappingUtils.buildSearchResults(pep1, 2, pep1CdsMappings);
    m = sr.getResults().get(0);
    assertEquals(4, m.getStart());
    assertEquals(6, m.getEnd());
    sr = MappingUtils.buildSearchResults(pep1, 3, pep1CdsMappings);
    m = sr.getResults().get(0);
    assertEquals(7, m.getStart());
    assertEquals(9, m.getEnd());
    sr = MappingUtils.buildSearchResults(pep1, 4, pep1CdsMappings);
    m = sr.getResults().get(0);
    assertEquals(10, m.getStart());
    assertEquals(12, m.getEnd());

    /*
     * Get mapping of pep2 to cds2 and verify it
     * maps GPG in pep2 to 1-3,4-6,7-9 in second CDS sequence
     */
    List<AlignedCodonFrame> pep2Mappings = MappingUtils
            .findMappingsForSequence(pep2, mappings);
    assertEquals(2, pep2Mappings.size());
    List<AlignedCodonFrame> pep2CdsMappings = MappingUtils
            .findMappingsForSequence(cds.getSequenceAt(1), pep2Mappings);
    assertEquals(1, pep2CdsMappings.size());
    sr = MappingUtils.buildSearchResults(pep2, 1, pep2CdsMappings);
    assertEquals(1, sr.getResults().size());
    m = sr.getResults().get(0);
    assertEquals(cds.getSequenceAt(1).getDatasetSequence(),
            m.getSequence());
    assertEquals(1, m.getStart());
    assertEquals(3, m.getEnd());
    sr = MappingUtils.buildSearchResults(pep2, 2, pep2CdsMappings);
    m = sr.getResults().get(0);
    assertEquals(4, m.getStart());
    assertEquals(6, m.getEnd());
    sr = MappingUtils.buildSearchResults(pep2, 3, pep2CdsMappings);
    m = sr.getResults().get(0);
    assertEquals(7, m.getStart());
    assertEquals(9, m.getEnd());
  }

  /**
   * Test the method that realigns protein to match mapped codon alignment.
   */
  @Test(groups = { "Functional" })
  public void testAlignProteinAsDna_incompleteStartCodon()
  {
    // seq1: incomplete start codon (not mapped), then [3, 11]
    SequenceI dna1 = new Sequence("Seq1", "ccAAA-TTT-GGG-");
    // seq2 codons are [4, 5], [8, 11]
    SequenceI dna2 = new Sequence("Seq2", "ccaAA-ttT-GGG-");
    // seq3 incomplete start codon at 'tt'
    SequenceI dna3 = new Sequence("Seq3", "ccaaa-ttt-GGG-");
    AlignmentI dna = new Alignment(new SequenceI[] { dna1, dna2, dna3 });
    dna.setDataset(null);

    // prot1 has 'X' for incomplete start codon (not mapped)
    SequenceI prot1 = new Sequence("Seq1", "XKFG"); // X for incomplete start
    SequenceI prot2 = new Sequence("Seq2", "NG");
    SequenceI prot3 = new Sequence("Seq3", "XG"); // X for incomplete start
    AlignmentI protein = new Alignment(
            new SequenceI[]
            { prot1, prot2, prot3 });
    protein.setDataset(null);

    // map dna1 [3, 11] to prot1 [2, 4] KFG
    MapList map = new MapList(new int[] { 3, 11 }, new int[] { 2, 4 }, 3,
            1);
    AlignedCodonFrame acf = new AlignedCodonFrame();
    acf.addMap(dna1.getDatasetSequence(), prot1.getDatasetSequence(), map);

    // map dna2 [4, 5] [8, 11] to prot2 [1, 2] NG
    map = new MapList(new int[] { 4, 5, 8, 11 }, new int[] { 1, 2 }, 3, 1);
    acf.addMap(dna2.getDatasetSequence(), prot2.getDatasetSequence(), map);

    // map dna3 [9, 11] to prot3 [2, 2] G
    map = new MapList(new int[] { 9, 11 }, new int[] { 2, 2 }, 3, 1);
    acf.addMap(dna3.getDatasetSequence(), prot3.getDatasetSequence(), map);

    ArrayList<AlignedCodonFrame> acfs = new ArrayList<>();
    acfs.add(acf);
    protein.setCodonFrames(acfs);

    /*
     * verify X is included in the aligned proteins, and placed just
     * before the first mapped residue 
     * CCT is between CCC and TTT
     */
    AlignmentUtils.alignProteinAsDna(protein, dna);
    assertEquals("XK-FG", prot1.getSequenceAsString());
    assertEquals("--N-G", prot2.getSequenceAsString());
    assertEquals("---XG", prot3.getSequenceAsString());
  }

  /**
   * Tests for the method that maps the subset of a dna sequence that has CDS
   * (or subtype) feature - case where the start codon is incomplete.
   */
  @Test(groups = "Functional")
  public void testFindCdsPositions_fivePrimeIncomplete()
  {
    SequenceI dnaSeq = new Sequence("dna", "aaagGGCCCaaaTTTttt");
    dnaSeq.createDatasetSequence();
    SequenceI ds = dnaSeq.getDatasetSequence();

    // CDS for dna 5-6 (incomplete codon), 7-9
    SequenceFeature sf = new SequenceFeature("CDS", "", 5, 9, 0f, null);
    sf.setPhase("2"); // skip 2 bases to start of next codon
    ds.addSequenceFeature(sf);
    // CDS for dna 13-15
    sf = new SequenceFeature("CDS_predicted", "", 13, 15, 0f, null);
    ds.addSequenceFeature(sf);

    List<int[]> ranges = AlignmentUtils.findCdsPositions(dnaSeq);

    /*
     * check the mapping starts with the first complete codon
     */
    assertEquals(6, MappingUtils.getLength(ranges));
    assertEquals(2, ranges.size());
    assertEquals(7, ranges.get(0)[0]);
    assertEquals(9, ranges.get(0)[1]);
    assertEquals(13, ranges.get(1)[0]);
    assertEquals(15, ranges.get(1)[1]);
  }

  /**
   * Tests for the method that maps the subset of a dna sequence that has CDS
   * (or subtype) feature.
   */
  @Test(groups = "Functional")
  public void testFindCdsPositions()
  {
    SequenceI dnaSeq = new Sequence("dna", "aaaGGGcccAAATTTttt");
    dnaSeq.createDatasetSequence();
    SequenceI ds = dnaSeq.getDatasetSequence();

    // CDS for dna 10-12
    SequenceFeature sf = new SequenceFeature("CDS_predicted", "", 10, 12,
            0f, null);
    sf.setStrand("+");
    ds.addSequenceFeature(sf);
    // CDS for dna 4-6
    sf = new SequenceFeature("CDS", "", 4, 6, 0f, null);
    sf.setStrand("+");
    ds.addSequenceFeature(sf);
    // exon feature should be ignored here
    sf = new SequenceFeature("exon", "", 7, 9, 0f, null);
    ds.addSequenceFeature(sf);

    List<int[]> ranges = AlignmentUtils.findCdsPositions(dnaSeq);
    /*
     * verify ranges { [4-6], [12-10] }
     * note CDS ranges are ordered ascending even if the CDS
     * features are not
     */
    assertEquals(6, MappingUtils.getLength(ranges));
    assertEquals(2, ranges.size());
    assertEquals(4, ranges.get(0)[0]);
    assertEquals(6, ranges.get(0)[1]);
    assertEquals(10, ranges.get(1)[0]);
    assertEquals(12, ranges.get(1)[1]);
  }

  /**
   * Tests for the method that maps the subset of a dna sequence that has CDS
   * (or subtype) feature, with CDS strand = '-' (reverse)
   */
  // test turned off as currently findCdsPositions is not strand-dependent
  // left in case it comes around again...
  @Test(groups = "Functional", enabled = false)
  public void testFindCdsPositions_reverseStrand()
  {
    SequenceI dnaSeq = new Sequence("dna", "aaaGGGcccAAATTTttt");
    dnaSeq.createDatasetSequence();
    SequenceI ds = dnaSeq.getDatasetSequence();

    // CDS for dna 4-6
    SequenceFeature sf = new SequenceFeature("CDS", "", 4, 6, 0f, null);
    sf.setStrand("-");
    ds.addSequenceFeature(sf);
    // exon feature should be ignored here
    sf = new SequenceFeature("exon", "", 7, 9, 0f, null);
    ds.addSequenceFeature(sf);
    // CDS for dna 10-12
    sf = new SequenceFeature("CDS_predicted", "", 10, 12, 0f, null);
    sf.setStrand("-");
    ds.addSequenceFeature(sf);

    List<int[]> ranges = AlignmentUtils.findCdsPositions(dnaSeq);
    /*
     * verify ranges { [12-10], [6-4] }
     */
    assertEquals(6, MappingUtils.getLength(ranges));
    assertEquals(2, ranges.size());
    assertEquals(12, ranges.get(0)[0]);
    assertEquals(10, ranges.get(0)[1]);
    assertEquals(6, ranges.get(1)[0]);
    assertEquals(4, ranges.get(1)[1]);
  }

  /**
   * Tests for the method that maps the subset of a dna sequence that has CDS
   * (or subtype) feature - reverse strand case where the start codon is
   * incomplete.
   */
  @Test(groups = "Functional", enabled = false)
  // test turned off as currently findCdsPositions is not strand-dependent
  // left in case it comes around again...
  public void testFindCdsPositions_reverseStrandThreePrimeIncomplete()
  {
    SequenceI dnaSeq = new Sequence("dna", "aaagGGCCCaaaTTTttt");
    dnaSeq.createDatasetSequence();
    SequenceI ds = dnaSeq.getDatasetSequence();

    // CDS for dna 5-9
    SequenceFeature sf = new SequenceFeature("CDS", "", 5, 9, 0f, null);
    sf.setStrand("-");
    ds.addSequenceFeature(sf);
    // CDS for dna 13-15
    sf = new SequenceFeature("CDS_predicted", "", 13, 15, 0f, null);
    sf.setStrand("-");
    sf.setPhase("2"); // skip 2 bases to start of next codon
    ds.addSequenceFeature(sf);

    List<int[]> ranges = AlignmentUtils.findCdsPositions(dnaSeq);

    /*
     * check the mapping starts with the first complete codon
     * expect ranges [13, 13], [9, 5]
     */
    assertEquals(6, MappingUtils.getLength(ranges));
    assertEquals(2, ranges.size());
    assertEquals(13, ranges.get(0)[0]);
    assertEquals(13, ranges.get(0)[1]);
    assertEquals(9, ranges.get(1)[0]);
    assertEquals(5, ranges.get(1)[1]);
  }

  @Test(groups = "Functional")
  public void testAlignAs_alternateTranscriptsUngapped()
  {
    SequenceI dna1 = new Sequence("dna1", "cccGGGTTTaaa");
    SequenceI dna2 = new Sequence("dna2", "CCCgggtttAAA");
    AlignmentI dna = new Alignment(new SequenceI[] { dna1, dna2 });
    ((Alignment) dna).createDatasetAlignment();
    SequenceI cds1 = new Sequence("cds1", "GGGTTT");
    SequenceI cds2 = new Sequence("cds2", "CCCAAA");
    AlignmentI cds = new Alignment(new SequenceI[] { cds1, cds2 });
    ((Alignment) cds).createDatasetAlignment();

    AlignedCodonFrame acf = new AlignedCodonFrame();
    MapList map = new MapList(new int[] { 4, 9 }, new int[] { 1, 6 }, 1, 1);
    acf.addMap(dna1.getDatasetSequence(), cds1.getDatasetSequence(), map);
    map = new MapList(new int[] { 1, 3, 10, 12 }, new int[] { 1, 6 }, 1, 1);
    acf.addMap(dna2.getDatasetSequence(), cds2.getDatasetSequence(), map);

    /*
     * verify CDS alignment is as:
     *   cccGGGTTTaaa (cdna)
     *   CCCgggtttAAA (cdna)
     *   
     *   ---GGGTTT--- (cds)
     *   CCC------AAA (cds)
     */
    dna.addCodonFrame(acf);
    AlignmentUtils.alignAs(cds, dna);
    assertEquals("---GGGTTT", cds.getSequenceAt(0).getSequenceAsString());
    assertEquals("CCC------AAA",
            cds.getSequenceAt(1).getSequenceAsString());
  }

  @Test(groups = { "Functional" })
  public void testAddMappedPositions()
  {
    SequenceI from = new Sequence("dna", "ggAA-ATcc-TT-g");
    SequenceI seq1 = new Sequence("cds", "AAATTT");
    from.createDatasetSequence();
    seq1.createDatasetSequence();
    Mapping mapping = new Mapping(seq1,
            new MapList(new int[]
            { 3, 6, 9, 10 }, new int[] { 1, 6 }, 1, 1));
    Map<Integer, Map<SequenceI, Character>> map = new TreeMap<>();
    AlignmentUtils.addMappedPositions(seq1, from, mapping, map);

    /*
     * verify map has seq1 residues in columns 3,4,6,7,11,12
     */
    assertEquals(6, map.size());
    assertEquals('A', map.get(3).get(seq1).charValue());
    assertEquals('A', map.get(4).get(seq1).charValue());
    assertEquals('A', map.get(6).get(seq1).charValue());
    assertEquals('T', map.get(7).get(seq1).charValue());
    assertEquals('T', map.get(11).get(seq1).charValue());
    assertEquals('T', map.get(12).get(seq1).charValue());

    /*
     * 
     */
  }

  /**
   * Test case where the mapping 'from' range includes a stop codon which is
   * absent in the 'to' range
   */
  @Test(groups = { "Functional" })
  public void testAddMappedPositions_withStopCodon()
  {
    SequenceI from = new Sequence("dna", "ggAA-ATcc-TT-g");
    SequenceI seq1 = new Sequence("cds", "AAATTT");
    from.createDatasetSequence();
    seq1.createDatasetSequence();
    Mapping mapping = new Mapping(seq1,
            new MapList(new int[]
            { 3, 6, 9, 10 }, new int[] { 1, 6 }, 1, 1));
    Map<Integer, Map<SequenceI, Character>> map = new TreeMap<>();
    AlignmentUtils.addMappedPositions(seq1, from, mapping, map);

    /*
     * verify map has seq1 residues in columns 3,4,6,7,11,12
     */
    assertEquals(6, map.size());
    assertEquals('A', map.get(3).get(seq1).charValue());
    assertEquals('A', map.get(4).get(seq1).charValue());
    assertEquals('A', map.get(6).get(seq1).charValue());
    assertEquals('T', map.get(7).get(seq1).charValue());
    assertEquals('T', map.get(11).get(seq1).charValue());
    assertEquals('T', map.get(12).get(seq1).charValue());
  }

  /**
   * Test for the case where the products for which we want CDS are specified.
   * This is to represent the case where EMBL has CDS mappings to both Uniprot
   * and EMBLCDSPROTEIN. makeCdsAlignment() should only return the mappings for
   * the protein sequences specified.
   */
  @Test(groups = { "Functional" })
  public void testMakeCdsAlignment_filterProducts()
  {
    SequenceI dna1 = new Sequence("dna1", "aaaGGGcccTTTaaa");
    SequenceI dna2 = new Sequence("dna2", "GGGcccTTTaaaCCC");
    SequenceI pep1 = new Sequence("Uniprot|pep1", "GF");
    SequenceI pep2 = new Sequence("Uniprot|pep2", "GFP");
    SequenceI pep3 = new Sequence("EMBL|pep3", "GF");
    SequenceI pep4 = new Sequence("EMBL|pep4", "GFP");
    dna1.createDatasetSequence();
    dna2.createDatasetSequence();
    pep1.createDatasetSequence();
    pep2.createDatasetSequence();
    pep3.createDatasetSequence();
    pep4.createDatasetSequence();
    AlignmentI dna = new Alignment(new SequenceI[] { dna1, dna2 });
    dna.setDataset(null);
    AlignmentI emblPeptides = new Alignment(new SequenceI[] { pep3, pep4 });
    emblPeptides.setDataset(null);

    AlignedCodonFrame acf = new AlignedCodonFrame();
    MapList map = new MapList(new int[] { 4, 6, 10, 12 },
            new int[]
            { 1, 2 }, 3, 1);
    acf.addMap(dna1.getDatasetSequence(), pep1.getDatasetSequence(), map);
    acf.addMap(dna1.getDatasetSequence(), pep3.getDatasetSequence(), map);
    dna.addCodonFrame(acf);

    acf = new AlignedCodonFrame();
    map = new MapList(new int[] { 1, 3, 7, 9, 13, 15 }, new int[] { 1, 3 },
            3, 1);
    acf.addMap(dna2.getDatasetSequence(), pep2.getDatasetSequence(), map);
    acf.addMap(dna2.getDatasetSequence(), pep4.getDatasetSequence(), map);
    dna.addCodonFrame(acf);

    /*
     * execute method under test to find CDS for EMBL peptides only
     */
    AlignmentI cds = AlignmentUtils
            .makeCdsAlignment(new SequenceI[]
            { dna1, dna2 }, dna.getDataset(),
                    emblPeptides.getSequencesArray());

    assertEquals(2, cds.getSequences().size());
    assertEquals("GGGTTT", cds.getSequenceAt(0).getSequenceAsString());
    assertEquals("GGGTTTCCC", cds.getSequenceAt(1).getSequenceAsString());

    /*
     * verify shared, extended alignment dataset
     */
    assertSame(dna.getDataset(), cds.getDataset());
    assertTrue(dna.getDataset().getSequences()
            .contains(cds.getSequenceAt(0).getDatasetSequence()));
    assertTrue(dna.getDataset().getSequences()
            .contains(cds.getSequenceAt(1).getDatasetSequence()));

    /*
     * Verify mappings from CDS to peptide, cDNA to CDS, and cDNA to peptide
     * the mappings are on the shared alignment dataset
     */
    List<AlignedCodonFrame> cdsMappings = cds.getDataset().getCodonFrames();
    /*
     * 6 mappings, 2*(DNA->CDS), 2*(DNA->Pep), 2*(CDS->Pep) 
     */
    assertEquals(6, cdsMappings.size());

    /*
     * verify that mapping sets for dna and cds alignments are different
     * [not current behaviour - all mappings are on the alignment dataset]  
     */
    // select -> subselect type to test.
    // Assert.assertNotSame(dna.getCodonFrames(), cds.getCodonFrames());
    // assertEquals(4, dna.getCodonFrames().size());
    // assertEquals(4, cds.getCodonFrames().size());

    /*
     * Two mappings involve pep3 (dna to pep3, cds to pep3)
     * Mapping from pep3 to GGGTTT in first new exon sequence
     */
    List<AlignedCodonFrame> pep3Mappings = MappingUtils
            .findMappingsForSequence(pep3, cdsMappings);
    assertEquals(2, pep3Mappings.size());
    List<AlignedCodonFrame> mappings = MappingUtils
            .findMappingsForSequence(cds.getSequenceAt(0), pep3Mappings);
    assertEquals(1, mappings.size());

    // map G to GGG
    SearchResultsI sr = MappingUtils.buildSearchResults(pep3, 1, mappings);
    assertEquals(1, sr.getResults().size());
    SearchResultMatchI m = sr.getResults().get(0);
    assertSame(cds.getSequenceAt(0).getDatasetSequence(), m.getSequence());
    assertEquals(1, m.getStart());
    assertEquals(3, m.getEnd());
    // map F to TTT
    sr = MappingUtils.buildSearchResults(pep3, 2, mappings);
    m = sr.getResults().get(0);
    assertSame(cds.getSequenceAt(0).getDatasetSequence(), m.getSequence());
    assertEquals(4, m.getStart());
    assertEquals(6, m.getEnd());

    /*
     * Two mappings involve pep4 (dna to pep4, cds to pep4)
     * Verify mapping from pep4 to GGGTTTCCC in second new exon sequence
     */
    List<AlignedCodonFrame> pep4Mappings = MappingUtils
            .findMappingsForSequence(pep4, cdsMappings);
    assertEquals(2, pep4Mappings.size());
    mappings = MappingUtils.findMappingsForSequence(cds.getSequenceAt(1),
            pep4Mappings);
    assertEquals(1, mappings.size());
    // map G to GGG
    sr = MappingUtils.buildSearchResults(pep4, 1, mappings);
    assertEquals(1, sr.getResults().size());
    m = sr.getResults().get(0);
    assertSame(cds.getSequenceAt(1).getDatasetSequence(), m.getSequence());
    assertEquals(1, m.getStart());
    assertEquals(3, m.getEnd());
    // map F to TTT
    sr = MappingUtils.buildSearchResults(pep4, 2, mappings);
    m = sr.getResults().get(0);
    assertSame(cds.getSequenceAt(1).getDatasetSequence(), m.getSequence());
    assertEquals(4, m.getStart());
    assertEquals(6, m.getEnd());
    // map P to CCC
    sr = MappingUtils.buildSearchResults(pep4, 3, mappings);
    m = sr.getResults().get(0);
    assertSame(cds.getSequenceAt(1).getDatasetSequence(), m.getSequence());
    assertEquals(7, m.getStart());
    assertEquals(9, m.getEnd());
  }

  /**
   * Test the method that just copies aligned sequences, provided all sequences
   * to be aligned share the aligned sequence's dataset
   */
  @Test(groups = "Functional")
  public void testAlignAsSameSequences()
  {
    SequenceI dna1 = new Sequence("dna1", "cccGGGTTTaaa");
    SequenceI dna2 = new Sequence("dna2", "CCCgggtttAAA");
    AlignmentI al1 = new Alignment(new SequenceI[] { dna1, dna2 });
    ((Alignment) al1).createDatasetAlignment();

    SequenceI dna3 = new Sequence(dna1);
    SequenceI dna4 = new Sequence(dna2);
    assertSame(dna3.getDatasetSequence(), dna1.getDatasetSequence());
    assertSame(dna4.getDatasetSequence(), dna2.getDatasetSequence());
    String seq1 = "-cc-GG-GT-TT--aaa";
    dna3.setSequence(seq1);
    String seq2 = "C--C-Cgg--gtt-tAA-A-";
    dna4.setSequence(seq2);
    AlignmentI al2 = new Alignment(new SequenceI[] { dna3, dna4 });
    ((Alignment) al2).createDatasetAlignment();

    /*
     * alignment removes gapped columns (two internal, two trailing)
     */
    assertTrue(AlignmentUtils.alignAsSameSequences(al1, al2));
    String aligned1 = "-cc-GG-GTTT-aaa";
    assertEquals(aligned1, al1.getSequenceAt(0).getSequenceAsString());
    String aligned2 = "C--C-Cgg-gtttAAA";
    assertEquals(aligned2, al1.getSequenceAt(1).getSequenceAsString());

    /*
     * add another sequence to 'aligned' - should still succeed, since
     * unaligned sequences still share a dataset with aligned sequences
     */
    SequenceI dna5 = new Sequence("dna5", "CCCgggtttAAA");
    dna5.createDatasetSequence();
    al2.addSequence(dna5);
    assertTrue(AlignmentUtils.alignAsSameSequences(al1, al2));
    assertEquals(aligned1, al1.getSequenceAt(0).getSequenceAsString());
    assertEquals(aligned2, al1.getSequenceAt(1).getSequenceAsString());

    /*
     * add another sequence to 'unaligned' - should fail, since now not
     * all unaligned sequences share a dataset with aligned sequences
     */
    SequenceI dna6 = new Sequence("dna6", "CCCgggtttAAA");
    dna6.createDatasetSequence();
    al1.addSequence(dna6);
    // JAL-2110 JBP Comment: what's the use case for this behaviour ?
    assertFalse(AlignmentUtils.alignAsSameSequences(al1, al2));
  }

  @Test(groups = "Functional")
  public void testAlignAsSameSequencesMultipleSubSeq()
  {
    SequenceI dna1 = new Sequence("dna1", "cccGGGTTTaaa");
    SequenceI dna2 = new Sequence("dna2", "CCCgggtttAAA");
    SequenceI as1 = dna1.deriveSequence(); // cccGGGTTTaaa/1-12
    SequenceI as2 = dna1.deriveSequence().getSubSequence(3, 7); // GGGT/4-7
    SequenceI as3 = dna2.deriveSequence(); // CCCgggtttAAA/1-12
    as1.insertCharAt(6, 5, '-');
    assertEquals("cccGGG-----TTTaaa", as1.getSequenceAsString());
    as2.insertCharAt(6, 5, '-');
    assertEquals("GGGT-----", as2.getSequenceAsString());
    as3.insertCharAt(3, 5, '-');
    assertEquals("CCC-----gggtttAAA", as3.getSequenceAsString());
    AlignmentI aligned = new Alignment(new SequenceI[] { as1, as2, as3 });

    // why do we need to cast this still ?
    ((Alignment) aligned).createDatasetAlignment();
    SequenceI uas1 = dna1.deriveSequence();
    SequenceI uas2 = dna1.deriveSequence().getSubSequence(3, 7);
    SequenceI uas3 = dna2.deriveSequence();
    AlignmentI tobealigned = new Alignment(
            new SequenceI[]
            { uas1, uas2, uas3 });
    ((Alignment) tobealigned).createDatasetAlignment();

    /*
     * alignAs lines up dataset sequences and removes empty columns (two)
     */
    assertTrue(AlignmentUtils.alignAsSameSequences(tobealigned, aligned));
    assertEquals("cccGGG---TTTaaa", uas1.getSequenceAsString());
    assertEquals("GGGT", uas2.getSequenceAsString());
    assertEquals("CCC---gggtttAAA", uas3.getSequenceAsString());
  }

  @Test(groups = { "Functional" })
  public void testTransferGeneLoci()
  {
    SequenceI from = new Sequence("transcript",
            "aaacccgggTTTAAACCCGGGtttaaacccgggttt");
    SequenceI to = new Sequence("CDS", "TTTAAACCCGGG");
    MapList map = new MapList(new int[] { 1, 12 }, new int[] { 10, 21 }, 1,
            1);

    /*
     * first with nothing to transfer
     */
    AlignmentUtils.transferGeneLoci(from, map, to);
    assertNull(to.getGeneLoci());

    /*
     * next with gene loci set on 'from' sequence
     */
    int[] exons = new int[] { 100, 105, 155, 164, 210, 229 };
    MapList geneMap = new MapList(new int[] { 1, 36 }, exons, 1, 1);
    from.setGeneLoci("human", "GRCh38", "7", geneMap);
    AlignmentUtils.transferGeneLoci(from, map, to);

    GeneLociI toLoci = to.getGeneLoci();
    assertNotNull(toLoci);
    // DBRefEntry constructor upper-cases 'source'
    assertEquals("HUMAN", toLoci.getSpeciesId());
    assertEquals("GRCh38", toLoci.getAssemblyId());
    assertEquals("7", toLoci.getChromosomeId());

    /*
     * transcript 'exons' are 1-6, 7-16, 17-36
     * CDS 1:12 is transcript 10-21
     * transcript 'CDS' is 10-16, 17-21
     * which is 'gene' 158-164, 210-214
     */
    MapList toMap = toLoci.getMapping();
    assertEquals(1, toMap.getFromRanges().size());
    assertEquals(2, toMap.getFromRanges().get(0).length);
    assertEquals(1, toMap.getFromRanges().get(0)[0]);
    assertEquals(12, toMap.getFromRanges().get(0)[1]);
    assertEquals(2, toMap.getToRanges().size());
    assertEquals(2, toMap.getToRanges().get(0).length);
    assertEquals(158, toMap.getToRanges().get(0)[0]);
    assertEquals(164, toMap.getToRanges().get(0)[1]);
    assertEquals(210, toMap.getToRanges().get(1)[0]);
    assertEquals(214, toMap.getToRanges().get(1)[1]);
    // or summarised as (but toString might change in future):
    assertEquals("[ [1, 12] ] 1:1 to [ [158, 164] [210, 214] ]",
            toMap.toString());

    /*
     * an existing value is not overridden 
     */
    geneMap = new MapList(new int[] { 1, 36 }, new int[] { 36, 1 }, 1, 1);
    from.setGeneLoci("inhuman", "GRCh37", "6", geneMap);
    AlignmentUtils.transferGeneLoci(from, map, to);
    assertEquals("GRCh38", toLoci.getAssemblyId());
    assertEquals("7", toLoci.getChromosomeId());
    toMap = toLoci.getMapping();
    assertEquals("[ [1, 12] ] 1:1 to [ [158, 164] [210, 214] ]",
            toMap.toString());
  }

  /**
   * Tests for the method that maps nucleotide to protein based on CDS features
   */
  @Test(groups = "Functional")
  public void testMapCdsToProtein()
  {
    SequenceI peptide = new Sequence("pep", "KLQ");

    /*
     * Case 1: CDS 3 times length of peptide
     * NB method only checks lengths match, not translation
     */
    SequenceI dna = new Sequence("dna", "AACGacgtCTCCT");
    dna.createDatasetSequence();
    dna.addSequenceFeature(new SequenceFeature("CDS", "", 1, 4, null));
    dna.addSequenceFeature(new SequenceFeature("CDS", "", 9, 13, null));
    MapList ml = AlignmentUtils.mapCdsToProtein(dna, peptide);
    assertEquals(3, ml.getFromRatio());
    assertEquals(1, ml.getToRatio());
    assertEquals("[[1, 3]]",
            Arrays.deepToString(ml.getToRanges().toArray()));
    assertEquals("[[1, 4], [9, 13]]",
            Arrays.deepToString(ml.getFromRanges().toArray()));

    /*
     * Case 2: CDS 3 times length of peptide + stop codon
     * (note code does not currently check trailing codon is a stop codon)
     */
    dna = new Sequence("dna", "AACGacgtCTCCTCCC");
    dna.createDatasetSequence();
    dna.addSequenceFeature(new SequenceFeature("CDS", "", 1, 4, null));
    dna.addSequenceFeature(new SequenceFeature("CDS", "", 9, 16, null));
    ml = AlignmentUtils.mapCdsToProtein(dna, peptide);
    assertEquals(3, ml.getFromRatio());
    assertEquals(1, ml.getToRatio());
    assertEquals("[[1, 3]]",
            Arrays.deepToString(ml.getToRanges().toArray()));
    assertEquals("[[1, 4], [9, 13]]",
            Arrays.deepToString(ml.getFromRanges().toArray()));

    /*
     * Case 3: CDS longer than 3 * peptide + stop codon - no mapping is made
     */
    dna = new Sequence("dna", "AACGacgtCTCCTTGATCA");
    dna.createDatasetSequence();
    dna.addSequenceFeature(new SequenceFeature("CDS", "", 1, 4, null));
    dna.addSequenceFeature(new SequenceFeature("CDS", "", 9, 19, null));
    ml = AlignmentUtils.mapCdsToProtein(dna, peptide);
    assertNull(ml);

    /*
     * Case 4: CDS shorter than 3 * peptide - no mapping is made
     */
    dna = new Sequence("dna", "AACGacgtCTCC");
    dna.createDatasetSequence();
    dna.addSequenceFeature(new SequenceFeature("CDS", "", 1, 4, null));
    dna.addSequenceFeature(new SequenceFeature("CDS", "", 9, 12, null));
    ml = AlignmentUtils.mapCdsToProtein(dna, peptide);
    assertNull(ml);

    /*
     * Case 5: CDS 3 times length of peptide + part codon - mapping is truncated
     */
    dna = new Sequence("dna", "AACGacgtCTCCTTG");
    dna.createDatasetSequence();
    dna.addSequenceFeature(new SequenceFeature("CDS", "", 1, 4, null));
    dna.addSequenceFeature(new SequenceFeature("CDS", "", 9, 15, null));
    ml = AlignmentUtils.mapCdsToProtein(dna, peptide);
    assertEquals(3, ml.getFromRatio());
    assertEquals(1, ml.getToRatio());
    assertEquals("[[1, 3]]",
            Arrays.deepToString(ml.getToRanges().toArray()));
    assertEquals("[[1, 4], [9, 13]]",
            Arrays.deepToString(ml.getFromRanges().toArray()));

    /*
     * Case 6: incomplete start codon corresponding to X in peptide
     */
    dna = new Sequence("dna", "ACGacgtCTCCTTGG");
    dna.createDatasetSequence();
    SequenceFeature sf = new SequenceFeature("CDS", "", 1, 3, null);
    sf.setPhase("2"); // skip 2 positions (AC) to start of next codon (GCT)
    dna.addSequenceFeature(sf);
    dna.addSequenceFeature(new SequenceFeature("CDS", "", 8, 15, null));
    peptide = new Sequence("pep", "XLQ");
    ml = AlignmentUtils.mapCdsToProtein(dna, peptide);
    assertEquals("[[2, 3]]",
            Arrays.deepToString(ml.getToRanges().toArray()));
    assertEquals("[[3, 3], [8, 12]]",
            Arrays.deepToString(ml.getFromRanges().toArray()));
  }

  /**
   * Tests for the method that locates the CDS sequence that has a mapping to
   * the given protein. That is, given a transcript-to-peptide mapping, find the
   * cds-to-peptide mapping that relates to both, and return the CDS sequence.
   */
  @Test(groups = "Functional")
  public void testFindCdsForProtein()
  {
    List<AlignedCodonFrame> mappings = new ArrayList<>();
    AlignedCodonFrame acf1 = new AlignedCodonFrame();
    mappings.add(acf1);

    SequenceI dna1 = new Sequence("dna1", "cgatATcgGCTATCTATGacg");
    dna1.createDatasetSequence();

    // NB we currently exclude STOP codon from CDS sequences
    // the test would need to change if this changes in future
    SequenceI cds1 = new Sequence("cds1", "ATGCTATCT");
    cds1.createDatasetSequence();

    SequenceI pep1 = new Sequence("pep1", "MLS");
    pep1.createDatasetSequence();
    List<AlignedCodonFrame> seqMappings = new ArrayList<>();
    MapList mapList = new MapList(new int[] { 5, 6, 9, 15 },
            new int[]
            { 1, 3 }, 3, 1);
    Mapping dnaToPeptide = new Mapping(pep1.getDatasetSequence(), mapList);

    // add dna to peptide mapping
    seqMappings.add(acf1);
    acf1.addMap(dna1.getDatasetSequence(), pep1.getDatasetSequence(),
            mapList);

    /*
     * first case - no dna-to-CDS mapping exists - search fails
     */
    SequenceI seq = AlignmentUtils.findCdsForProtein(mappings, dna1,
            seqMappings, dnaToPeptide);
    assertNull(seq);

    /*
     * second case - CDS-to-peptide mapping exists but no dna-to-CDS
     * - search fails
     */
    // todo this test fails if the mapping is added to acf1, not acf2
    // need to tidy up use of lists of mappings in AlignedCodonFrame
    AlignedCodonFrame acf2 = new AlignedCodonFrame();
    mappings.add(acf2);
    MapList cdsToPeptideMapping = new MapList(new int[] { 1, 9 },
            new int[]
            { 1, 3 }, 3, 1);
    acf2.addMap(cds1.getDatasetSequence(), pep1.getDatasetSequence(),
            cdsToPeptideMapping);
    assertNull(AlignmentUtils.findCdsForProtein(mappings, dna1, seqMappings,
            dnaToPeptide));

    /*
     * third case - add dna-to-CDS mapping - CDS is now found!
     */
    MapList dnaToCdsMapping = new MapList(new int[] { 5, 6, 9, 15 },
            new int[]
            { 1, 9 }, 1, 1);
    acf1.addMap(dna1.getDatasetSequence(), cds1.getDatasetSequence(),
            dnaToCdsMapping);
    seq = AlignmentUtils.findCdsForProtein(mappings, dna1, seqMappings,
            dnaToPeptide);
    assertSame(seq, cds1.getDatasetSequence());
  }

  /**
   * Tests for the method that locates the CDS sequence that has a mapping to
   * the given protein. That is, given a transcript-to-peptide mapping, find the
   * cds-to-peptide mapping that relates to both, and return the CDS sequence.
   * This test is for the case where transcript and CDS are the same length.
   */
  @Test(groups = "Functional")
  public void testFindCdsForProtein_noUTR()
  {
    List<AlignedCodonFrame> mappings = new ArrayList<>();
    AlignedCodonFrame acf1 = new AlignedCodonFrame();
    mappings.add(acf1);

    SequenceI dna1 = new Sequence("dna1", "ATGCTATCTTAA");
    dna1.createDatasetSequence();

    // NB we currently exclude STOP codon from CDS sequences
    // the test would need to change if this changes in future
    SequenceI cds1 = new Sequence("cds1", "ATGCTATCT");
    cds1.createDatasetSequence();

    SequenceI pep1 = new Sequence("pep1", "MLS");
    pep1.createDatasetSequence();
    List<AlignedCodonFrame> seqMappings = new ArrayList<>();
    MapList mapList = new MapList(new int[] { 1, 9 }, new int[] { 1, 3 }, 3,
            1);
    Mapping dnaToPeptide = new Mapping(pep1.getDatasetSequence(), mapList);

    // add dna to peptide mapping
    seqMappings.add(acf1);
    acf1.addMap(dna1.getDatasetSequence(), pep1.getDatasetSequence(),
            mapList);

    /*
     * first case - transcript lacks CDS features - it appears to be
     * the CDS sequence and is returned
     */
    SequenceI seq = AlignmentUtils.findCdsForProtein(mappings, dna1,
            seqMappings, dnaToPeptide);
    assertSame(seq, dna1.getDatasetSequence());

    /*
     * second case - transcript has CDS feature - this means it is
     * not returned as a match for CDS (CDS sequences don't have CDS features)
     */
    dna1.addSequenceFeature(
            new SequenceFeature(SequenceOntologyI.CDS, "cds", 1, 12, null));
    seq = AlignmentUtils.findCdsForProtein(mappings, dna1, seqMappings,
            dnaToPeptide);
    assertNull(seq);

    /*
     * third case - CDS-to-peptide mapping exists but no dna-to-CDS
     * - search fails
     */
    // todo this test fails if the mapping is added to acf1, not acf2
    // need to tidy up use of lists of mappings in AlignedCodonFrame
    AlignedCodonFrame acf2 = new AlignedCodonFrame();
    mappings.add(acf2);
    MapList cdsToPeptideMapping = new MapList(new int[] { 1, 9 },
            new int[]
            { 1, 3 }, 3, 1);
    acf2.addMap(cds1.getDatasetSequence(), pep1.getDatasetSequence(),
            cdsToPeptideMapping);
    assertNull(AlignmentUtils.findCdsForProtein(mappings, dna1, seqMappings,
            dnaToPeptide));

    /*
     * fourth case - add dna-to-CDS mapping - CDS is now found!
     */
    MapList dnaToCdsMapping = new MapList(new int[] { 1, 9 },
            new int[]
            { 1, 9 }, 1, 1);
    acf1.addMap(dna1.getDatasetSequence(), cds1.getDatasetSequence(),
            dnaToCdsMapping);
    seq = AlignmentUtils.findCdsForProtein(mappings, dna1, seqMappings,
            dnaToPeptide);
    assertSame(seq, cds1.getDatasetSequence());
  }

  @Test(groups = "Functional")
  public void testAddReferenceAnnotations()
  {
    SequenceI longseq = new Sequence("longA", "ASDASDASDASDAASDASDASDASDA");
    Annotation[] aa = new Annotation[longseq.getLength()];

    for (int p = 0; p < aa.length; p++)
    {
      aa[p] = new Annotation("P", "pos " + (p + 1), (char) 0,
              (float) p + 1);
    }
    AlignmentAnnotation refAnnot = new AlignmentAnnotation("LongSeqAnnot",
            "Annotations", aa);
    refAnnot.setCalcId("Test");
    longseq.addAlignmentAnnotation(refAnnot);
    verifyExpectedSequenceAnnotation(refAnnot);

    Alignment ourAl = new Alignment(
            new SequenceI[]
            { longseq.getSubSequence(5, 10),
                longseq.getSubSequence(7, 12) });
    ourAl.createDatasetAlignment();

    // transfer annotation
    SortedMap<String, String> tipEntries = new TreeMap<>();
    Map<SequenceI, List<AlignmentAnnotation>> candidates = new LinkedHashMap<>();

    AlignmentUtils.findAddableReferenceAnnotations(ourAl.getSequences(),
            tipEntries, candidates, ourAl);
    AlignmentUtils.addReferenceAnnotations(candidates, ourAl, null);

    assertNotNull(ourAl.getAlignmentAnnotation());
    assertEquals(ourAl.getAlignmentAnnotation().length, 2);

    for (AlignmentAnnotation alan : ourAl.getAlignmentAnnotation())
    {
      verifyExpectedSequenceAnnotation(alan);
    }
    // Everything above works for 2.11.3 and 2.11.2.x.
    // now simulate copy/paste to new alignment
    SequenceI[] newSeqAl = new SequenceI[2];
    // copy sequences but no annotation
    newSeqAl[0] = new Sequence(ourAl.getSequenceAt(0),
            ourAl.getSequenceAt(0).getAnnotation());
    newSeqAl[1] = new Sequence(ourAl.getSequenceAt(1),
            ourAl.getSequenceAt(1).getAnnotation());

    Alignment newAl = new Alignment(newSeqAl);
    // delete annotation
    for (SequenceI sq : newAl.getSequences())
    {
      sq.setAlignmentAnnotation(new AlignmentAnnotation[0]);
    }
    // JAL-4182 scenario test
    SequenceGroup sg = new SequenceGroup(Arrays.asList(newSeqAl));
    sg.setStartRes(0);
    sg.setEndRes(newAl.getWidth());
    AlignmentUtils.addReferenceAnnotationTo(newAl, newSeqAl[0],
            newSeqAl[0].getDatasetSequence().getAnnotation()[0], sg);
    AlignmentUtils.addReferenceAnnotationTo(newAl, newSeqAl[1],
            newSeqAl[1].getDatasetSequence().getAnnotation()[0], sg);
    for (AlignmentAnnotation alan : newAl.getAlignmentAnnotation())
    {
      verifyExpectedSequenceAnnotation(alan);
    }
  }

  /**
   * helper - tests annotation is mapped to position it was originally created
   * for
   * 
   * @param alan
   */
  private void verifyExpectedSequenceAnnotation(AlignmentAnnotation alan)
  {
    for (int c = 0; c < alan.annotations.length; c++)
    {
      Annotation a = alan.annotations[c];
      if (a != null)
      {
        assertEquals("Misaligned annotation at " + c,
                (float) alan.sequenceRef.findPosition(c), a.value);
      }
      else
      {
        assertTrue("Unexpected Null at position " + c,
                c >= alan.sequenceRef.getLength()
                        || Comparison.isGap(alan.sequenceRef.getCharAt(c)));
      }
    }
  }

  @Test(groups = "Functional")
  public void testAddReferenceContactMap()
  {
    SequenceI sq = new Sequence("a", "SSSQ");
    ContactMatrixI cm = new SeqDistanceContactMatrix(4);
    AlignmentAnnotation cm_aan = sq.addContactList(cm);
    cm_aan.description = cm_aan.description + " cm1";
    SequenceI dssq = sq.createDatasetSequence();

    // remove annotation on our non-dataset sequence
    sq.removeAlignmentAnnotation(sq.getAnnotation()[0]);
    // test transfer
    Alignment al = new Alignment(new SequenceI[] { sq });
    SortedMap<String, String> tipEntries = new TreeMap<>();
    Map<SequenceI, List<AlignmentAnnotation>> candidates = new LinkedHashMap<>();

    AlignmentUtils.findAddableReferenceAnnotations(al.getSequences(),
            tipEntries, candidates, al);
    AlignmentUtils.addReferenceAnnotations(candidates, al, null);
    assertTrue("No contact map annotation transferred",
            al.getAlignmentAnnotation() != null
                    && al.getAlignmentAnnotation().length == 1);
    AlignmentAnnotation alan = al.findAnnotations(sq, null, cm_aan.label)
            .iterator().next();
    ContactMatrixI t_cm = al.getContactMatrixFor(alan);
    assertNotNull("No contact map for the transferred annotation row.",
            t_cm);
    assertTrue(t_cm instanceof SeqDistanceContactMatrix);
    assertTrue(((SeqDistanceContactMatrix) t_cm).hasReferenceSeq());

    ContactListI cl = al.getContactListFor(alan, 1);
    assertNotNull(
            "No contact matrix recovered after reference annotation transfer",
            cl);
    // semantics of sequence associated contact list is slightly tricky - column
    // 3 in alignment should have data
    cl = al.getContactListFor(alan, 3);
    assertNotNull(
            "Contact matrix should have data for last position in sequence",
            cl);

    ContactMatrixI cm2 = new SeqDistanceContactMatrix(4);
    dssq.addContactList(cm2);
    tipEntries = new TreeMap<>();
    candidates = new LinkedHashMap<>();

    AlignmentUtils.findAddableReferenceAnnotations(al.getSequences(),
            tipEntries, candidates, al);
    AlignmentUtils.addReferenceAnnotations(candidates, al, null);
    assertTrue("Expected two contact map annotation transferred",
            al.getAlignmentAnnotation() != null
                    && al.getAlignmentAnnotation().length == 2);

  }
}
