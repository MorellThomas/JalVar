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
package jalview.io.gff;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Mapping;
import jalview.datamodel.MappingType;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceDummy;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.JvOptionPane;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ExonerateHelperTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = "Functional")
  public void testGetMappingType()
  {
    // protein-to-dna:
    assertSame(MappingType.PeptideToNucleotide, ExonerateHelper
            .getMappingType("exonerate:protein2genome:local"));
    assertSame(MappingType.PeptideToNucleotide,
            ExonerateHelper.getMappingType("exonerate:protein2dna:local"));

    // dna-to-dna:
    assertSame(MappingType.NucleotideToNucleotide,
            ExonerateHelper.getMappingType("coding2coding"));
    assertSame(MappingType.NucleotideToNucleotide,
            ExonerateHelper.getMappingType("coding2genome"));
    assertSame(MappingType.NucleotideToNucleotide,
            ExonerateHelper.getMappingType("cdna2genome"));
    assertSame(MappingType.NucleotideToNucleotide,
            ExonerateHelper.getMappingType("genome2genome"));
    assertNull(ExonerateHelper.getMappingType("affine:local"));
  }

  /**
   * Test processing one exonerate GFF line for the case where the mapping is
   * protein2dna, similarity feature is on the query (the protein), match to the
   * forward strand, target sequence is in neither the alignment nor the 'new
   * sequences'
   * 
   * @throws IOException
   */
  @Test(groups = "Functional")
  public void testProcessGffSimilarity_protein2dna_forward_querygff()
          throws IOException
  {
    ExonerateHelper testee = new ExonerateHelper();
    List<SequenceI> newseqs = new ArrayList<SequenceI>();
    String[] gff = "Seq\texonerate:protein2dna:local\tsimilarity\t3\t10\t.\t+\t.\talignment_id 0 ; Target dna1 ; Align 3 400 8"
            .split("\\t");
    SequenceI seq = new Sequence("Seq", "PQRASTGKEEDVMIWCHQN");
    seq.createDatasetSequence();
    AlignmentI align = new Alignment(new SequenceI[] {});
    Map<String, List<String>> set = Gff2Helper.parseNameValuePairs(gff[8]);

    /*
     * this should create a mapping from Seq2/3-10 to virtual sequence
     * dna1 (added to newseqs) positions 400-423
     */
    testee.processGffSimilarity(set, seq, gff, align, newseqs, false);
    assertEquals(1, newseqs.size());
    assertTrue(newseqs.get(0) instanceof SequenceDummy);
    assertEquals("dna1", newseqs.get(0).getName());
    assertEquals(1, align.getCodonFrames().size());
    AlignedCodonFrame mapping = align.getCodonFrames().iterator().next();
    assertEquals(1, mapping.getAaSeqs().length);
    assertSame(seq.getDatasetSequence(), mapping.getAaSeqs()[0]);
    assertEquals(1, mapping.getdnaSeqs().length);
    assertSame(newseqs.get(0), mapping.getdnaSeqs()[0]);
    assertEquals(1, mapping.getdnaToProt().length);
    assertEquals(1, mapping.getdnaToProt()[0].getFromRanges().size());
    assertArrayEquals(new int[] { 400, 423 },
            mapping.getdnaToProt()[0].getFromRanges().get(0));
    assertEquals(1, mapping.getdnaToProt()[0].getToRanges().size());
    assertArrayEquals(new int[] { 3, 10 },
            mapping.getdnaToProt()[0].getToRanges().get(0));
  }

  /**
   * Test processing one exonerate GFF line for the case where the mapping is
   * protein2dna, similarity feature is on the query (the protein), match to the
   * reverse strand
   * 
   * @throws IOException
   */
  @Test(groups = "Functional")
  public void testProcessGffSimilarity_protein2dna_reverse_querygff()
          throws IOException
  {
    ExonerateHelper testee = new ExonerateHelper();
    List<SequenceI> newseqs = new ArrayList<SequenceI>();
    String[] gff = "Seq\texonerate:protein2dna:local\tsimilarity\t3\t10\t0\t-\t.\talignment_id 0 ; Target dna1 ; Align 3 400 8"
            .split("\\t");
    SequenceI seq = new Sequence("Seq", "PQRASTGKEEDVMIWCHQN");
    seq.createDatasetSequence();
    AlignmentI align = new Alignment(new SequenceI[] {});
    Map<String, List<String>> set = Gff2Helper.parseNameValuePairs(gff[8]);

    /*
     * this should create a mapping from Seq2/3-10 to virtual sequence
     * dna1 (added to newseqs) positions 400-377 (reverse)
     */
    testee.processGffSimilarity(set, seq, gff, align, newseqs, false);
    assertEquals(1, newseqs.size());
    assertTrue(newseqs.get(0) instanceof SequenceDummy);
    assertEquals("dna1", newseqs.get(0).getName());
    assertEquals(1, align.getCodonFrames().size());
    AlignedCodonFrame mapping = align.getCodonFrames().iterator().next();
    assertEquals(1, mapping.getAaSeqs().length);
    assertSame(seq.getDatasetSequence(), mapping.getAaSeqs()[0]);
    assertEquals(1, mapping.getdnaSeqs().length);
    assertSame(newseqs.get(0), mapping.getdnaSeqs()[0]);
    assertEquals(1, mapping.getdnaToProt().length);
    assertEquals(1, mapping.getdnaToProt()[0].getFromRanges().size());
    assertArrayEquals(new int[] { 400, 377 },
            mapping.getdnaToProt()[0].getFromRanges().get(0));
    assertEquals(1, mapping.getdnaToProt()[0].getToRanges().size());
    assertArrayEquals(new int[] { 3, 10 },
            mapping.getdnaToProt()[0].getToRanges().get(0));
  }

  /**
   * Test processing one exonerate GFF line for the case where the mapping is
   * protein2dna, similarity feature is on the target (the dna), match to the
   * forward strand
   * 
   * @throws IOException
   */
  @Test(groups = "Functional")
  public void testProcessGffSimilarity_protein2dna_forward_targetgff()
          throws IOException
  {
    ExonerateHelper testee = new ExonerateHelper();
    List<SequenceI> newseqs = new ArrayList<SequenceI>();
    String[] gff = "dna1\texonerate:protein2dna:local\tsimilarity\t400\t423\t0\t+\t.\talignment_id 0 ; Query Prot1 ; Align 400 3 24"
            .split("\\t");
    SequenceI seq = new Sequence("dna1/391-430",
            "CGATCCGATCCGATCCGATCCGATCCGATCCGATCCGATC");
    seq.createDatasetSequence();
    AlignmentI align = new Alignment(new SequenceI[] { seq });
    // GFF feature on the target describes mapping from base 400 for
    // count 24 to position 3
    Map<String, List<String>> set = Gff2Helper.parseNameValuePairs(gff[8]);

    /*
     * this should create a mapping from virtual sequence dna1 (added to 
     * newseqs) positions 400-423 to Prot1/3-10
     */
    testee.processGffSimilarity(set, seq, gff, align, newseqs, false);
    assertEquals(1, newseqs.size());
    assertTrue(newseqs.get(0) instanceof SequenceDummy);
    assertEquals("Prot1", newseqs.get(0).getName());
    assertEquals(1, align.getCodonFrames().size());
    AlignedCodonFrame mapping = align.getCodonFrames().iterator().next();
    assertEquals(1, mapping.getAaSeqs().length);
    assertSame(newseqs.get(0), mapping.getAaSeqs()[0]);
    assertSame(seq.getDatasetSequence(), mapping.getdnaSeqs()[0]);
    assertEquals(1, mapping.getdnaSeqs().length);
    assertEquals(1, mapping.getdnaToProt().length);
    assertEquals(1, mapping.getdnaToProt()[0].getFromRanges().size());
    assertArrayEquals(new int[] { 400, 423 },
            mapping.getdnaToProt()[0].getFromRanges().get(0));
    assertEquals(1, mapping.getdnaToProt()[0].getToRanges().size());
    assertArrayEquals(new int[] { 3, 10 },
            mapping.getdnaToProt()[0].getToRanges().get(0));
  }

  /**
   * Test processing one exonerate GFF line for the case where the mapping is
   * protein2dna, similarity feature is on the target (the dna), match to the
   * reverse strand
   * 
   * @throws IOException
   */
  @Test(groups = "Functional")
  public void testProcessGffSimilarity_protein2dna_reverse_targetgff()
          throws IOException
  {
    ExonerateHelper testee = new ExonerateHelper();
    List<SequenceI> newseqs = new ArrayList<SequenceI>();
    String[] gff = "dna1\texonerate:protein2dna:local\tsimilarity\t377\t400\t0\t-\t.\talignment_id 0 ; Query Prot1 ; Align 400 3 24"
            .split("\\t");
    SequenceI seq = new Sequence("dna1/371-410",
            "CGATCCGATCCGATCCGATCCGATCCGATCCGATCCGATC");
    seq.createDatasetSequence();
    AlignmentI align = new Alignment(new SequenceI[] { seq });
    // GFF feature on the target describes mapping from base 400 for
    // count 24 to position 3
    Map<String, List<String>> set = Gff2Helper.parseNameValuePairs(gff[8]);

    /*
     * this should create a mapping from virtual sequence dna1 (added to 
     * newseqs) positions 400-377 (reverse) to Prot1/3-10
     */
    testee.processGffSimilarity(set, seq, gff, align, newseqs, false);
    assertEquals(1, newseqs.size());
    assertTrue(newseqs.get(0) instanceof SequenceDummy);
    assertEquals("Prot1", newseqs.get(0).getName());
    assertEquals(1, align.getCodonFrames().size());
    AlignedCodonFrame mapping = align.getCodonFrames().iterator().next();
    assertEquals(1, mapping.getAaSeqs().length);
    assertSame(newseqs.get(0), mapping.getAaSeqs()[0]);
    assertSame(seq.getDatasetSequence(), mapping.getdnaSeqs()[0]);
    assertEquals(1, mapping.getdnaSeqs().length);
    assertEquals(1, mapping.getdnaToProt().length);
    assertEquals(1, mapping.getdnaToProt()[0].getFromRanges().size());
    assertArrayEquals(new int[] { 400, 377 },
            mapping.getdnaToProt()[0].getFromRanges().get(0));
    assertEquals(1, mapping.getdnaToProt()[0].getToRanges().size());
    assertArrayEquals(new int[] { 3, 10 },
            mapping.getdnaToProt()[0].getToRanges().get(0));
  }

  /**
   * Tests loading exonerate GFF2 output, including 'similarity' alignment
   * feature, on to sequences
   */
  @Test(groups = { "Functional" })
  public void testAddExonerateGffToAlignment()
  {
    FileLoader loader = new FileLoader(false);
    AlignFrame af = loader.LoadFileWaitTillLoaded(
            "examples/testdata/exonerateseqs.fa", DataSourceType.FILE);

    af.loadJalviewDataFile("examples/testdata/exonerateoutput.gff",
            DataSourceType.FILE, null, null);

    /*
     * verify one mapping to a dummy sequence, one to a real one
     */
    List<AlignedCodonFrame> mappings = af.getViewport().getAlignment()
            .getDataset().getCodonFrames();
    assertEquals(2, mappings.size());
    Iterator<AlignedCodonFrame> iter = mappings.iterator();

    // first mapping is to dummy sequence
    AlignedCodonFrame mapping = iter.next();
    Mapping[] mapList = mapping.getProtMappings();
    assertEquals(1, mapList.length);
    assertTrue(mapList[0].getTo() instanceof SequenceDummy);
    assertEquals("DDB_G0269124", mapList[0].getTo().getName());

    // 143 in protein should map to codon [11270, 11269, 11268] in dna
    int[] mappedRegion = mapList[0].getMap().locateInFrom(143, 143);
    assertArrayEquals(new int[] { 11270, 11268 }, mappedRegion);

    // second mapping is to a sequence in the alignment
    mapping = iter.next();
    mapList = mapping.getProtMappings();
    assertEquals(1, mapList.length);
    SequenceI proteinSeq = af.getViewport().getAlignment()
            .findName("DDB_G0280897");
    assertSame(proteinSeq.getDatasetSequence(), mapList[0].getTo());
    assertEquals(1, mapping.getdnaToProt().length);

    // 143 in protein should map to codon [11270, 11269, 11268] in dna
    mappedRegion = mapList[0].getMap().locateInFrom(143, 143);
    assertArrayEquals(new int[] { 11270, 11268 }, mappedRegion);

    // 182 in protein should map to codon [11153, 11152, 11151] in dna
    mappedRegion = mapList[0].getMap().locateInFrom(182, 182);
    assertArrayEquals(new int[] { 11153, 11151 }, mappedRegion);

    // and the reverse mapping:
    mappedRegion = mapList[0].getMap().locateInTo(11151, 11153);
    assertArrayEquals(new int[] { 182, 182 }, mappedRegion);

    // 11150 in dna should _not_ map to protein
    mappedRegion = mapList[0].getMap().locateInTo(11150, 11150);
    assertNull(mappedRegion);

    // similarly 183 in protein should _not_ map to dna
    mappedRegion = mapList[0].getMap().locateInFrom(183, 183);
    assertNull(mappedRegion);
  }
}
