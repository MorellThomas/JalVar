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
import static org.testng.AssertJUnit.assertNotNull;
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
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class InterProScanHelperTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * Test processing one InterProScan GFF line
   * 
   * @throws IOException
   */
  @Test(groups = "Functional")
  public void testProcessProteinMatch() throws IOException
  {
    InterProScanHelper testee = new InterProScanHelper();
    List<SequenceI> newseqs = new ArrayList<SequenceI>();
    String[] gff = "Submitted\tPfam\tprotein_match\t5\t30\t0\t+\t.\tName=PF12838;Target=Submitted 5 30;signature_desc=4Fe-4S dicluster domain;ID=match$17_5_30"
            .split("\\t");
    SequenceI seq = new Sequence("Prot1", "PQRASTGKEEDVMIWCHQN");
    seq.createDatasetSequence();
    AlignmentI align = new Alignment(new SequenceI[] {});
    Map<String, List<String>> set = Gff3Helper.parseNameValuePairs(gff[8]);

    /*
     * this should create a mapping from Prot1/5-30 to virtual sequence
     * match$17_5_30 (added to newseqs) positions 1-26
     */
    testee.processProteinMatch(set, seq, gff, align, newseqs, false);
    assertEquals(1, newseqs.size());
    assertTrue(newseqs.get(0) instanceof SequenceDummy);
    assertEquals("match$17_5_30", newseqs.get(0).getName());

    assertNotNull(newseqs.get(0).getSequenceFeatures());
    assertEquals(1, newseqs.get(0).getSequenceFeatures().size());
    SequenceFeature sf = newseqs.get(0).getSequenceFeatures().get(0);
    assertEquals(1, sf.getBegin());
    assertEquals(26, sf.getEnd());
    assertEquals("Pfam", sf.getType());
    assertEquals("4Fe-4S dicluster domain", sf.getDescription());
    assertEquals("InterProScan", sf.getFeatureGroup());

    assertEquals(1, align.getCodonFrames().size());
    AlignedCodonFrame mapping = align.getCodonFrames().iterator().next();

    /*
     * 'dnaseqs' (map from) is here [Prot1]
     * 'aaseqs' (map to) is here [match$17_5_30]
     */
    // TODO use more suitable naming in AlignedCodonFrame
    assertEquals(1, mapping.getAaSeqs().length);
    assertSame(seq.getDatasetSequence(), mapping.getdnaSeqs()[0]);
    assertEquals(1, mapping.getdnaSeqs().length);
    assertSame(newseqs.get(0), mapping.getAaSeqs()[0]);
    assertEquals(1, mapping.getdnaToProt().length);
    assertEquals(1, mapping.getdnaToProt()[0].getFromRanges().size());
    assertArrayEquals(new int[] { 5, 30 },
            mapping.getdnaToProt()[0].getFromRanges().get(0));
    assertEquals(1, mapping.getdnaToProt()[0].getToRanges().size());
    assertArrayEquals(new int[] { 1, 26 },
            mapping.getdnaToProt()[0].getToRanges().get(0));
  }

}
