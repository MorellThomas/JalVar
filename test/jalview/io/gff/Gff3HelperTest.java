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
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceDummy;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Gff3HelperTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * Test processing one PASA GFF line giving a match from forward strand to
   * forward strand
   * 
   * @throws IOException
   */
  @Test(groups = "Functional")
  public void testProcessCdnaMatch_forwardToForward() throws IOException
  {
    GffHelperBase testee = new Gff3Helper();
    List<SequenceI> newseqs = new ArrayList<SequenceI>();
    String[] gff = "gi|68711\tblat-pasa\tcDNA_match\t12923\t13060\t98.55\t+\t.\tID=align_68;Target=gi|N37351 1 138 +"
            .split("\\t");
    SequenceI seq = new Sequence("gi|68711",
            "GAATTCGTTCATGTAGGTTGATTTTTATT");
    seq.createDatasetSequence();
    AlignmentI align = new Alignment(new SequenceI[] {});

    /*
     * this should create a mapping from gi|68711/12923-13060
     * to virtual sequence gi|N37351 (added to newseqs) positions 1-138
     */
    testee.processGff(seq, gff, align, newseqs, false);
    assertEquals(1, newseqs.size());
    assertTrue(newseqs.get(0) instanceof SequenceDummy);
    assertEquals("gi|N37351", newseqs.get(0).getName());
    assertEquals(1, align.getCodonFrames().size());
    AlignedCodonFrame mapping = align.getCodonFrames().iterator().next();

    /*
     * 'dnaseqs' (map from) is here [gi|68711]
     * 'aaseqs' (map to) is here [gi|N37351]
     */
    // TODO use more suitable naming in AlignedCodonFrame
    assertEquals(1, mapping.getAaSeqs().length);
    assertSame(seq.getDatasetSequence(), mapping.getdnaSeqs()[0]);
    assertEquals(1, mapping.getdnaSeqs().length);
    assertSame(newseqs.get(0), mapping.getAaSeqs()[0]);
    assertEquals(1, mapping.getdnaToProt().length);
    assertEquals(1, mapping.getdnaToProt()[0].getFromRanges().size());
    assertArrayEquals(new int[] { 12923, 13060 },
            mapping.getdnaToProt()[0].getFromRanges().get(0));
    assertEquals(1, mapping.getdnaToProt()[0].getToRanges().size());
    assertArrayEquals(new int[] { 1, 138 },
            mapping.getdnaToProt()[0].getToRanges().get(0));
  }

  /**
   * Test processing one PASA GFF line giving a match from forward strand to
   * reverse strand
   * 
   * @throws IOException
   */
  @Test(groups = "Functional")
  public void testProcessCdnaMatch_forwardToReverse() throws IOException
  {
    GffHelperBase testee = new Gff3Helper();
    List<SequenceI> newseqs = new ArrayList<SequenceI>();
    String[] gff = "gi|68711\tblat-pasa\tcDNA_match\t12923\t13060\t98.55\t+\t.\tID=align_68;Target=gi|N37351 1 138 -"
            .split("\\t");
    SequenceI seq = new Sequence("gi|68711",
            "GAATTCGTTCATGTAGGTTGATTTTTATT");
    seq.createDatasetSequence();
    AlignmentI align = new Alignment(new SequenceI[] {});

    /*
     * this should create a mapping from gi|68711/12923-13060
     * to virtual sequence gi|N37351 (added to newseqs) positions 138-1
     */
    testee.processGff(seq, gff, align, newseqs, false);
    assertEquals(1, newseqs.size());
    assertTrue(newseqs.get(0) instanceof SequenceDummy);
    assertEquals("gi|N37351", newseqs.get(0).getName());
    assertEquals(1, align.getCodonFrames().size());
    AlignedCodonFrame mapping = align.getCodonFrames().iterator().next();

    /*
     * 'dnaseqs' (map from) is here [gi|68711]
     * 'aaseqs' (map to) is here [gi|N37351]
     */
    // TODO use more suitable naming in AlignedCodonFrame
    assertEquals(1, mapping.getAaSeqs().length);
    assertSame(seq.getDatasetSequence(), mapping.getdnaSeqs()[0]);
    assertEquals(1, mapping.getdnaSeqs().length);
    assertSame(newseqs.get(0), mapping.getAaSeqs()[0]);
    assertEquals(1, mapping.getdnaToProt().length);
    assertEquals(1, mapping.getdnaToProt()[0].getFromRanges().size());
    assertArrayEquals(new int[] { 12923, 13060 },
            mapping.getdnaToProt()[0].getFromRanges().get(0));
    assertEquals(1, mapping.getdnaToProt()[0].getToRanges().size());
    assertArrayEquals(new int[] { 138, 1 },
            mapping.getdnaToProt()[0].getToRanges().get(0));
  }

  /**
   * Test processing one PASA GFF line giving a match from reverse complement
   * strand to forward strand
   * 
   * @throws IOException
   */
  @Test(groups = "Functional")
  public void testProcessCdnaMatch_reverseToForward() throws IOException
  {
    GffHelperBase testee = new Gff3Helper();
    List<SequenceI> newseqs = new ArrayList<SequenceI>();
    String[] gff = "gi|68711\tblat-pasa\tcDNA_match\t12923\t13060\t98.55\t-\t.\tID=align_68;Target=gi|N37351 1 138 +"
            .split("\\t");
    SequenceI seq = new Sequence("gi|68711",
            "GAATTCGTTCATGTAGGTTGATTTTTATT");
    seq.createDatasetSequence();
    AlignmentI align = new Alignment(new SequenceI[] {});

    /*
     * (For now) we don't process reverse complement mappings; to do this
     * would require (a) creating a virtual sequence placeholder for the
     * reverse complement (b) resolving the sequence by its id from some
     * source (GFF ##FASTA or other) (c) creating the reverse complement
     * sequence (d) updating the mapping to be to the reverse complement
     */
    SequenceFeature sf = testee.processGff(seq, gff, align, newseqs, false);
    assertNull(sf);
    assertTrue(newseqs.isEmpty());
  }

  /**
   * Test processing two PASA GFF lines representing a spliced mapping
   * 
   * @throws IOException
   */
  @Test(groups = "Functional")
  public void testProcessCdnaMatch_spliced() throws IOException
  {
    GffHelperBase testee = new Gff3Helper();
    List<SequenceI> newseqs = new ArrayList<SequenceI>();
    SequenceI seq = new Sequence("gi|68711",
            "GAATTCGTTCATGTAGGTTGATTTTTATT");
    seq.createDatasetSequence();
    AlignmentI align = new Alignment(new SequenceI[] {});

    // mapping from gi|68711 12923-13060 to gi|N37351 1-138
    String[] gff = "gi|68711\tblat-pasa\tcDNA_match\t12923\t13060\t98.55\t+\t.\tID=align_68;Target=gi|N37351 1 138 +"
            .split("\\t");
    testee.processGff(seq, gff, align, newseqs, false);
    // mapping from gi|68711 13411-13550 to gi|N37351 139-278
    gff = "gi|68711\tblat-pasa\tcDNA_match\t13411\t13550\t98.55\t+\t.\tID=align_68;Target=gi|N37351 139 278 +"
            .split("\\t");
    testee.processGff(seq, gff, align, newseqs, false);

    assertEquals(1, newseqs.size());
    assertTrue(newseqs.get(0) instanceof SequenceDummy);
    assertEquals("gi|N37351", newseqs.get(0).getName());

    // only 1 AlignedCodonFrame added to the alignment with both mappings!
    // (this is important for 'align cdna to genome' to work correctly)
    assertEquals(1, align.getCodonFrames().size());
    AlignedCodonFrame mapping = align.getCodonFrames().get(0);

    /*
     * 'dnaseqs' (map from) is here [gi|68711]
     * 'aaseqs' (map to) is here [gi|N37351]
     */
    // TODO use more suitable naming in AlignedCodonFrame
    assertEquals(1, mapping.getAaSeqs().length);
    assertSame(seq.getDatasetSequence(), mapping.getdnaSeqs()[0]);
    assertEquals(1, mapping.getdnaSeqs().length);
    assertSame(newseqs.get(0), mapping.getAaSeqs()[0]);
    assertEquals(1, mapping.getdnaToProt().length);
    assertEquals(2, mapping.getdnaToProt()[0].getFromRanges().size());
    // the two spliced dna ranges are combined in one MapList
    assertArrayEquals(new int[] { 12923, 13060 },
            mapping.getdnaToProt()[0].getFromRanges().get(0));
    assertArrayEquals(new int[] { 13411, 13550 },
            mapping.getdnaToProt()[0].getFromRanges().get(1));
    assertEquals(1, mapping.getdnaToProt()[0].getToRanges().size());
    // the two cdna ranges are merged into one contiguous region
    assertArrayEquals(new int[] { 1, 278 },
            mapping.getdnaToProt()[0].getToRanges().get(0));
  }

  @Test(groups = "Functional")
  public void testGetDescription()
  {
    Gff3Helper testee = new Gff3Helper();
    SequenceFeature sf = new SequenceFeature("type", "desc", 10, 20, 3f,
            "group");
    Map<String, List<String>> attributes = new HashMap<String, List<String>>();
    assertNull(testee.getDescription(sf, attributes));

    // ID if any is a fall-back for description
    sf.setValue("ID", "Patrick");
    assertEquals("Patrick", testee.getDescription(sf, attributes));

    // Target is set by Exonerate
    sf.setValue("Target", "Destination Moon");
    assertEquals("Destination", testee.getDescription(sf, attributes));

    // Ensembl variant feature - extract "alleles" value
    // may be sequence_variant or a sub-type in the sequence ontology
    sf = new SequenceFeature("feature_variant", "desc", 10, 20, 3f,
            "group");
    List<String> atts = new ArrayList<String>();
    atts.add("A");
    atts.add("C");
    atts.add("T");
    attributes.put("alleles", atts);
    assertEquals("A,C,T", testee.getDescription(sf, attributes));

    // Ensembl transcript or exon feature - extract Name
    List<String> atts2 = new ArrayList<String>();
    atts2.add("ENSE00001871077");
    attributes.put("Name", atts2);
    sf = new SequenceFeature("transcript", "desc", 10, 20, 3f, "group");
    assertEquals("ENSE00001871077", testee.getDescription(sf, attributes));
    // transcript sub-type in SO
    sf = new SequenceFeature("mRNA", "desc", 10, 20, 3f, "group");
    assertEquals("ENSE00001871077", testee.getDescription(sf, attributes));
    // special usage of feature by Ensembl
    sf = new SequenceFeature("NMD_transcript_variant", "desc", 10, 20, 3f,
            "group");
    assertEquals("ENSE00001871077", testee.getDescription(sf, attributes));
    // exon feature
    sf = new SequenceFeature("exon", "desc", 10, 20, 3f, "group");
    assertEquals("ENSE00001871077", testee.getDescription(sf, attributes));
  }
}
