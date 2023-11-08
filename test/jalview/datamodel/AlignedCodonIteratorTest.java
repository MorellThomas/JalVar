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

import jalview.gui.JvOptionPane;
import jalview.util.MapList;

import java.util.Iterator;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Unit tests for Mapping$AlignedCodonIterator
 * 
 * @author gmcarstairs
 *
 */
public class AlignedCodonIteratorTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * Test normal case for iterating over aligned codons.
   */
  @Test(groups = { "Functional" })
  public void testNext()
  {
    SequenceI from = new Sequence("Seq1", "-CgC-C-cCtAG-AtG-Gc");
    from.createDatasetSequence();
    SequenceI to = new Sequence("Seq1", "-PQ-R-");
    to.createDatasetSequence();
    MapList map = new MapList(new int[] { 1, 1, 3, 4, 6, 6, 8, 10, 12, 13 },
            new int[]
            { 1, 3 }, 3, 1);
    Mapping m = new Mapping(to.getDatasetSequence(), map);

    Iterator<AlignedCodon> codons = m.getCodonIterator(from, '-');
    AlignedCodon codon = codons.next();
    assertEquals("[1, 3, 5]", codon.toString());
    assertEquals("P", codon.product);
    codon = codons.next();
    assertEquals("[8, 10, 11]", codon.toString());
    assertEquals("Q", codon.product);
    codon = codons.next();
    assertEquals("[13, 15, 17]", codon.toString());
    assertEquals("R", codon.product);
    assertFalse(codons.hasNext());
  }

  /**
   * Test weird case where the mapping skips over a peptide.
   */
  @Test(groups = { "Functional" })
  public void testNext_unmappedPeptide()
  {
    SequenceI from = new Sequence("Seq1", "-CgC-C-cCtAG-AtG-Gc");
    from.createDatasetSequence();
    SequenceI to = new Sequence("Seq1", "-PQ-TR-");
    to.createDatasetSequence();
    MapList map = new MapList(new int[] { 1, 1, 3, 4, 6, 6, 8, 10, 12, 13 },
            new int[]
            { 1, 2, 4, 4 }, 3, 1);
    Mapping m = new Mapping(to.getDatasetSequence(), map);

    Iterator<AlignedCodon> codons = m.getCodonIterator(from, '-');
    AlignedCodon codon = codons.next();
    assertEquals("[1, 3, 5]", codon.toString());
    assertEquals("P", codon.product);
    codon = codons.next();
    assertEquals("[8, 10, 11]", codon.toString());
    assertEquals("Q", codon.product);
    codon = codons.next();
    assertEquals("[13, 15, 17]", codon.toString());
    assertEquals("R", codon.product);
    assertFalse(codons.hasNext());
  }

  /**
   * Test for exception thrown for an incomplete codon.
   */
  @Test(groups = { "Functional" })
  public void testNext_incompleteCodon()
  {
    SequenceI from = new Sequence("Seq1", "-CgC-C-cCgTt");
    from.createDatasetSequence();
    SequenceI to = new Sequence("Seq1", "-PQ-R-");
    to.createDatasetSequence();
    MapList map = new MapList(new int[] { 1, 1, 3, 4, 6, 6, 8, 8 },
            new int[]
            { 1, 3 }, 3, 1);
    Mapping m = new Mapping(to.getDatasetSequence(), map);

    Iterator<AlignedCodon> codons = m.getCodonIterator(from, '-');
    AlignedCodon codon = codons.next();
    assertEquals("[1, 3, 5]", codon.toString());
    assertEquals("P", codon.product);
    try
    {
      codon = codons.next();
      Assert.fail("expected exception");
    } catch (IncompleteCodonException e)
    {
      // expected
    }
  }

  /**
   * Test normal case for iterating over aligned codons.
   */
  @Test(groups = { "Functional" })
  public void testAnother()
  {
    SequenceI from = new Sequence("Seq1", "TGCCATTACCAG-");
    from.createDatasetSequence();
    SequenceI to = new Sequence("Seq1", "CHYQ");
    to.createDatasetSequence();
    MapList map = new MapList(new int[] { 1, 12 }, new int[] { 1, 4 }, 3,
            1);
    Mapping m = new Mapping(to.getDatasetSequence(), map);

    Iterator<AlignedCodon> codons = m.getCodonIterator(from, '-');
    AlignedCodon codon = codons.next();
    assertEquals("[0, 1, 2]", codon.toString());
    assertEquals("C", codon.product);
    codon = codons.next();
    assertEquals("[3, 4, 5]", codon.toString());
    assertEquals("H", codon.product);
    codon = codons.next();
    assertEquals("[6, 7, 8]", codon.toString());
    assertEquals("Y", codon.product);
    codon = codons.next();
    assertEquals("[9, 10, 11]", codon.toString());
    assertEquals("Q", codon.product);
    assertFalse(codons.hasNext());
  }

  /**
   * Test for a case with sequence (and mappings) not starting at 1
   */
  @Test(groups = { "Functional" })
  public void testNext_withOffset()
  {
    SequenceI from = new Sequence("Seq1", "-CgC-C-cCtAG-AtG-Gc", 7, 20);
    from.createDatasetSequence();
    SequenceI to = new Sequence("Seq1/10-12", "-PQ-R-");
    to.createDatasetSequence();
    MapList map = new MapList(
            new int[]
            { 7, 7, 9, 10, 12, 12, 14, 16, 18, 19 }, new int[] { 10, 12 },
            3, 1);
    Mapping m = new Mapping(to.getDatasetSequence(), map);

    Iterator<AlignedCodon> codons = m.getCodonIterator(from, '-');
    AlignedCodon codon = codons.next();
    assertEquals("[1, 3, 5]", codon.toString());
    assertEquals("P", codon.product);
    codon = codons.next();
    assertEquals("[8, 10, 11]", codon.toString());
    assertEquals("Q", codon.product);
    codon = codons.next();
    assertEquals("[13, 15, 17]", codon.toString());
    assertEquals("R", codon.product);
    assertFalse(codons.hasNext());
  }
}
