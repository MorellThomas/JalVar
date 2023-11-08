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
import static org.testng.AssertJUnit.assertSame;

import jalview.gui.JvOptionPane;
import jalview.util.MapList;

import java.util.Arrays;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class refactored from main method
 */
public class MappingTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * trite test of the intersectVisContigs method for a simple DNA -> Protein
   * exon map and a range of visContigs
   */
  @Test(groups = { "Functional" })
  public void testIntersectVisContigs()
  {
    MapList fk = new MapList(new int[] { 1, 6, 8, 13, 15, 23 },
            new int[]
            { 1, 7 }, 3, 1);
    Mapping m = new Mapping(fk);
    Mapping m_1 = m
            .intersectVisContigs(new int[]
            { fk.getFromLowest(), fk.getFromHighest() });
    Mapping m_2 = m.intersectVisContigs(new int[] { 1, 7, 11, 20 });

    // assertions from output values 'as is', not checked for correctness
    String result = Arrays.deepToString(m_1.map.getFromRanges().toArray());
    System.out.println(result);
    assertEquals("[[1, 6], [8, 13], [15, 23]]", result);

    result = Arrays.deepToString(m_2.map.getFromRanges().toArray());
    System.out.println(result);
    assertEquals("[[1, 6], [11, 13], [15, 20]]", result);
  }

  @Test(groups = { "Functional" })
  public void testToString()
  {
    /*
     * with no sequence
     */
    MapList fk = new MapList(new int[] { 1, 6, 8, 13 }, new int[] { 4, 7 },
            3, 1);
    Mapping m = new Mapping(fk);
    assertEquals("[ [1, 6] [8, 13] ] 3:1 to [ [4, 7] ] ", m.toString());

    /*
     * with a sequence
     */
    SequenceI seq = new Sequence("Seq1", "");
    m = new Mapping(seq, fk);
    assertEquals("[ [1, 6] [8, 13] ] 3:1 to [ [4, 7] ] Seq1", m.toString());
  }

  @Test(groups = { "Functional" })
  public void testCopyConstructor()
  {
    MapList ml = new MapList(new int[] { 1, 6, 8, 13 }, new int[] { 4, 7 },
            3, 1);
    SequenceI seq = new Sequence("seq1", "agtacg");
    Mapping m = new Mapping(seq, ml);
    m.setMappedFromId("abc");
    Mapping copy = new Mapping(m);
    assertEquals("abc", copy.getMappedFromId());
    assertEquals(ml, copy.getMap());
    assertSame(seq, copy.getTo());
  }
}
