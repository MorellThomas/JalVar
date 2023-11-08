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
package jalview.ext.ensembl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceDummy;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;
import jalview.io.gff.SequenceOntologyFactory;
import jalview.io.gff.SequenceOntologyLite;
import jalview.util.MapList;

import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class EnsemblGenomeTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @BeforeClass(alwaysRun = true)
  public void setUp()
  {
    SequenceOntologyFactory.setInstance(new SequenceOntologyLite());
  }

  @AfterClass(alwaysRun = true)
  public void tearDown()
  {
    SequenceOntologyFactory.setInstance(null);
  }

  /**
   * Test that the genomic sequence part of genomic sequence is correctly
   * identified by 'transcript' features (or subtypes) with the correct gene ID
   */
  @Test(groups = "Functional")
  public void testGetGenomicRangesFromFeatures()
  {
    EnsemblGenome testee = new EnsemblGenome();
    SequenceI genomic = new SequenceDummy("chr7");
    genomic.setStart(10000);
    genomic.setEnd(50000);
    String transcriptId = "ABC123";

    // transcript at (start+10000) length 501
    SequenceFeature sf = new SequenceFeature("transcript", "", 20000, 20500,
            0f, null);
    sf.setValue("id", transcriptId);
    sf.setStrand("+");
    genomic.addSequenceFeature(sf);

    // transcript (sub-type) at (start + 10500) length 101
    sf = new SequenceFeature("ncRNA", "", 10500, 10600, 0f, null);
    sf.setValue("id", transcriptId);
    sf.setStrand("+");
    genomic.addSequenceFeature(sf);

    // Ensembl treats NMD_transcript_variant as if transcript
    // although strictly it is a sequence_variant in SO
    sf = new SequenceFeature("NMD_transcript_variant", "", 11000, 12000, 0f,
            null);
    sf.setValue("id", transcriptId);
    sf.setStrand("+");
    genomic.addSequenceFeature(sf);

    // transcript with a different ID doesn't count
    sf = new SequenceFeature("transcript", "", 11500, 12600, 0f, null);
    sf.setValue("id", "anotherOne");
    genomic.addSequenceFeature(sf);

    // parent of transcript feature doesn't count
    sf = new SequenceFeature("gene_member_region", "", 10000, 50000, 0f,
            null);
    genomic.addSequenceFeature(sf);

    MapList ranges = testee.getGenomicRangesFromFeatures(genomic,
            transcriptId, 23);
    List<int[]> fromRanges = ranges.getFromRanges();
    assertEquals(3, fromRanges.size());
    // from ranges should be sorted by start order
    assertEquals(10500, fromRanges.get(0)[0]);
    assertEquals(10600, fromRanges.get(0)[1]);
    assertEquals(11000, fromRanges.get(1)[0]);
    assertEquals(12000, fromRanges.get(1)[1]);
    assertEquals(20000, fromRanges.get(2)[0]);
    assertEquals(20500, fromRanges.get(2)[1]);
    // to range should start from given start numbering
    List<int[]> toRanges = ranges.getToRanges();
    assertEquals(1, toRanges.size());
    assertEquals(23, toRanges.get(0)[0]);
    assertEquals(1625, toRanges.get(0)[1]);
  }

  /**
   * Test the method that retains features except for 'transcript' (or
   * sub-type), or those with parent other than the given id
   */
  @Test(groups = "Functional")
  public void testRetainFeature()
  {
    String accId = "ABC123";
    EnsemblGenome testee = new EnsemblGenome();

    SequenceFeature sf = new SequenceFeature("transcript", "", 20000, 20500,
            0f, null);
    assertFalse(testee.retainFeature(sf, accId));

    sf = new SequenceFeature("mature_transcript", "", 20000, 20500, 0f,
            null);
    assertFalse(testee.retainFeature(sf, accId));

    sf = new SequenceFeature("NMD_transcript_variant", "", 20000, 20500, 0f,
            null);
    assertFalse(testee.retainFeature(sf, accId));

    // other feature with no parent is kept
    sf = new SequenceFeature("anything", "", 20000, 20500, 0f, null);
    assertTrue(testee.retainFeature(sf, accId));

    // other feature with correct parent is kept
    sf.setValue("Parent", accId);
    assertTrue(testee.retainFeature(sf, accId));

    // other feature with wrong parent is not kept
    sf.setValue("Parent", "XYZ");
    assertFalse(testee.retainFeature(sf, accId));
  }

  /**
   * Test the method that picks out 'transcript' (or subtype) features with the
   * accession id as ID
   */
  @Test(groups = "Functional")
  public void testGetIdentifyingFeatures()
  {
    String accId = "ABC123";
    SequenceI seq = new Sequence(accId, "HEARTS");

    // transcript with no ID not valid
    SequenceFeature sf1 = new SequenceFeature("transcript", "", 1, 2, 0f,
            null);
    seq.addSequenceFeature(sf1);

    // transcript with wrong ID not valid
    // NB change desc to avoid rejection of duplicate feature!
    SequenceFeature sf2 = new SequenceFeature("transcript", "a", 1, 2, 0f,
            null);
    sf2.setValue("id", "transcript");
    seq.addSequenceFeature(sf2);

    // transcript with right ID is valid
    SequenceFeature sf3 = new SequenceFeature("transcript", "b", 1, 2, 0f,
            null);
    sf3.setValue("id", accId);
    seq.addSequenceFeature(sf3);

    // transcript sub-type with right ID is valid
    SequenceFeature sf4 = new SequenceFeature("ncRNA", "", 1, 2, 0f, null);
    sf4.setValue("id", accId);
    seq.addSequenceFeature(sf4);

    // Ensembl treats NMD_transcript_variant as if a transcript
    SequenceFeature sf5 = new SequenceFeature("NMD_transcript_variant", "",
            1, 2, 0f, null);
    sf5.setValue("id", accId);
    seq.addSequenceFeature(sf5);

    // gene not valid:
    SequenceFeature sf6 = new SequenceFeature("gene", "", 1, 2, 0f, null);
    sf6.setValue("id", accId);
    seq.addSequenceFeature(sf6);

    // exon not valid:
    SequenceFeature sf7 = new SequenceFeature("exon", "", 1, 2, 0f, null);
    sf7.setValue("id", accId);
    seq.addSequenceFeature(sf7);

    List<SequenceFeature> sfs = new EnsemblGenome()
            .getIdentifyingFeatures(seq, accId);
    assertFalse(sfs.contains(sf1));
    assertFalse(sfs.contains(sf2));
    assertTrue(sfs.contains(sf3));
    assertTrue(sfs.contains(sf4));
    assertTrue(sfs.contains(sf5));
    assertFalse(sfs.contains(sf6));
    assertFalse(sfs.contains(sf7));
  }

}
