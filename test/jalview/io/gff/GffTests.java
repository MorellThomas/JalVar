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
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Mapping;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceDummy;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.JvOptionPane;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests of use cases that include parsing GFF (version 2 or 3) features that
 * describe mappings between protein and cDNA. The format of the GFF varies
 * depending on which tool generated it.
 */
public class GffTests
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * Test the case where we load a protein ('query') sequence, then exonerateGff
   * describing its mapping to cDNA, and then a DNA sequence including the
   * mapped region
   */
  @Test(groups = "Functional")
  public void testResolveExonerateGff()
  {
    String proteinSeq = ">prot1/10-16\nYCWRSGA";
    AlignFrame af = new FileLoader(false).LoadFileWaitTillLoaded(proteinSeq,
            DataSourceType.PASTE);

    /*
     * exonerate GFF output mapping residues 11-15 (CWRSG) 
     * to bases 24-10 in sequence 'dna1' (reverse strand)
     */
    String exonerateGff = "##gff-version 2\n"
            + "prot1\tprotein2genome\tsimilarity\t11\t15\t99\t-\t.\talignment_id 0 ; Target dna1 ; Align 11 24 5";
    af.loadJalviewDataFile(exonerateGff, DataSourceType.PASTE, null, null);

    /*
     * check we have a mapping from prot1 to SequenceDummy 'dna1'
     */
    AlignmentI dataset = af.getViewport().getAlignment().getDataset();
    assertEquals(1, dataset.getSequences().size());
    assertEquals("prot1", dataset.getSequenceAt(0).getName());
    assertEquals("YCWRSGA", dataset.getSequenceAt(0).getSequenceAsString());
    List<AlignedCodonFrame> mappings = dataset.getCodonFrames();
    assertEquals(1, mappings.size());
    AlignedCodonFrame mapping = mappings.iterator().next();
    SequenceI mappedDna = mapping.getDnaForAaSeq(dataset.getSequenceAt(0));
    assertTrue(mappedDna instanceof SequenceDummy);
    assertEquals("dna1", mappedDna.getName());
    Mapping[] mapList = mapping.getProtMappings();
    assertEquals(1, mapList.length);
    // 11 in protein should map to codon [24, 23, 22] in dna
    int[] mappedRegion = mapList[0].getMap().locateInFrom(11, 11);
    assertArrayEquals(new int[] { 24, 22 }, mappedRegion);
    // 15 in protein should map to codon [12, 11, 10] in dna
    mappedRegion = mapList[0].getMap().locateInFrom(15, 15);
    assertArrayEquals(new int[] { 12, 10 }, mappedRegion);

    SequenceI dna1 = new Sequence("dna1", "AAACCCGGGTTTAAACCCGGGTTT");
    AlignmentI al = new Alignment(new SequenceI[] { dna1 });
    al.setDataset(null);

    /*
     * Now 'realise' the virtual mapping to the real DNA sequence;
     * interactively this could be by a drag or fetch of the sequence data
     * on to the alignment
     */
    mapping.realiseWith(dna1);
    // verify the mapping is now from the real, not the dummy sequence
    assertSame(dna1.getDatasetSequence(),
            mapping.getDnaForAaSeq(dataset.getSequenceAt(0)));
  }
}
