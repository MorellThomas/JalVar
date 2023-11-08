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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class EnsemblCdsTest
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
   * Test that the cdna part of genomic sequence is correctly identified by
   * 'CDS' features (or subtypes) with the desired transcript as parent
   */
  @Test(groups = "Functional")
  public void testGetGenomicRangesFromFeatures()
  {
    EnsemblCds testee = new EnsemblCds();
    SequenceI genomic = new SequenceDummy("chr7");
    genomic.setStart(10000);
    genomic.setEnd(50000);
    String transcriptId = "ABC123";

    // CDS at (start+10000) length 501
    SequenceFeature sf = new SequenceFeature("CDS", "", 20000, 20500, 0f,
            null);
    sf.setValue("Parent", transcriptId);
    sf.setStrand("+");
    genomic.addSequenceFeature(sf);

    // CDS (sub-type) at (start + 10500) length 101
    sf = new SequenceFeature("CDS_predicted", "", 10500, 10600, 0f, null);
    sf.setValue("Parent", transcriptId);
    sf.setStrand("+");
    genomic.addSequenceFeature(sf);

    // CDS belonging to a different transcript doesn't count
    sf = new SequenceFeature("CDS", "", 11500, 12600, 0f, null);
    sf.setValue("Parent", "anotherOne");
    genomic.addSequenceFeature(sf);

    // exon feature doesn't count
    sf = new SequenceFeature("exon", "", 10000, 50000, 0f, null);
    genomic.addSequenceFeature(sf);

    // mRNA_region feature doesn't count (parent of CDS)
    sf = new SequenceFeature("mRNA_region", "", 10000, 50000, 0f, null);
    genomic.addSequenceFeature(sf);

    MapList ranges = testee.getGenomicRangesFromFeatures(genomic,
            transcriptId, 23);
    List<int[]> fromRanges = ranges.getFromRanges();
    assertEquals(2, fromRanges.size());
    // from ranges should be sorted by start order
    assertEquals(10500, fromRanges.get(0)[0]);
    assertEquals(10600, fromRanges.get(0)[1]);
    assertEquals(20000, fromRanges.get(1)[0]);
    assertEquals(20500, fromRanges.get(1)[1]);
    // to range should start from given start numbering
    List<int[]> toRanges = ranges.getToRanges();
    assertEquals(1, toRanges.size());
    assertEquals(23, toRanges.get(0)[0]);
    assertEquals(624, toRanges.get(0)[1]);
  }

  /**
   * Test the method that retains features except for 'CDS' (or subtypes), or
   * features with parent other than the given id
   */
  @Test(groups = "Functional")
  public void testRetainFeature()
  {
    String accId = "ABC123";
    EnsemblCds testee = new EnsemblCds();

    SequenceFeature sf = new SequenceFeature("CDS", "", 20000, 20500, 0f,
            null);
    assertFalse(testee.retainFeature(sf, accId));

    sf = new SequenceFeature("CDS_predicted", "", 20000, 20500, 0f, null);
    assertFalse(testee.retainFeature(sf, accId));

    // other feature with no parent is retained
    sf = new SequenceFeature("anotherType", "", 20000, 20500, 0f, null);
    assertTrue(testee.retainFeature(sf, accId));

    // other feature with desired parent is retained
    sf.setValue("Parent", accId);
    assertTrue(testee.retainFeature(sf, accId));

    // feature with wrong parent is not retained
    sf.setValue("Parent", "XYZ");
    assertFalse(testee.retainFeature(sf, accId));
  }

  /**
   * Test the method that picks out 'CDS' (or subtype) features with the
   * accession id as parent
   */
  @Test(groups = "Functional")
  public void testGetIdentifyingFeatures()
  {
    String accId = "ABC123";
    SequenceI seq = new Sequence(accId, "MKDONS");

    // cds with no parent not valid
    SequenceFeature sf1 = new SequenceFeature("CDS", "", 1, 2, 0f, null);
    seq.addSequenceFeature(sf1);

    // cds with wrong parent not valid
    SequenceFeature sf2 = new SequenceFeature("CDS", "", 1, 2, 0f, null);
    sf2.setValue("Parent", "XYZ");
    seq.addSequenceFeature(sf2);

    // cds with right parent is valid
    SequenceFeature sf3 = new SequenceFeature("CDS", "", 1, 2, 0f, null);
    sf3.setValue("Parent", accId);
    seq.addSequenceFeature(sf3);

    // cds sub-type with right parent is valid
    SequenceFeature sf4 = new SequenceFeature("CDS_predicted", "", 1, 2, 0f,
            null);
    sf4.setValue("Parent", accId);
    seq.addSequenceFeature(sf4);

    // transcript not valid:
    SequenceFeature sf5 = new SequenceFeature("transcript", "", 1, 2, 0f,
            null);
    sf5.setValue("Parent", accId);
    seq.addSequenceFeature(sf5);

    // exon not valid:
    SequenceFeature sf6 = new SequenceFeature("exon", "", 1, 2, 0f, null);
    sf6.setValue("Parent", accId);
    seq.addSequenceFeature(sf6);

    List<SequenceFeature> sfs = new EnsemblCds().getIdentifyingFeatures(seq,
            accId);
    assertFalse(sfs.contains(sf1));
    assertFalse(sfs.contains(sf2));
    assertTrue(sfs.contains(sf3));
    assertTrue(sfs.contains(sf4));
    assertFalse(sfs.contains(sf5));
    assertFalse(sfs.contains(sf6));
  }

  @Test(groups = "Functional")
  public void testIsValidReference() throws Exception
  {
    EnsemblSequenceFetcher esq = new EnsemblCds();
    Assert.assertTrue(esq.isValidReference("CCDS5863.1"));
    Assert.assertTrue(esq.isValidReference("ENST00000288602"));
    Assert.assertTrue(esq.isValidReference("ENSG00000288602"));
    Assert.assertTrue(esq.isValidReference("ENSP00000288602"));
    Assert.assertFalse(esq.isValidReference("ENST0000288602"));
    // non-human species have a 3 character identifier included:
    Assert.assertTrue(esq.isValidReference("ENSMUSG00000099398"));
  }

}
