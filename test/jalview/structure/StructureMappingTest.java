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
package jalview.structure;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import jalview.datamodel.Mapping;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.util.MapList;

import java.util.HashMap;
import java.util.List;

import org.testng.annotations.Test;

public class StructureMappingTest
{
  @Test(groups = "Functional")
  public void testGetPDBResNumRanges()
  {
    HashMap<Integer, int[]> map = new HashMap<>();

    StructureMapping mapping = new StructureMapping(null, null, null, null,
            map, null);

    List<int[]> ranges = mapping.getPDBResNumRanges(1, 2);
    assertTrue(ranges.isEmpty());

    map.put(1, new int[] { 12, 20 }); // 1 maps to 12
    ranges = mapping.getPDBResNumRanges(2, 3);
    assertTrue(ranges.isEmpty());
    ranges = mapping.getPDBResNumRanges(1, 2);
    assertEquals(ranges.size(), 1);
    assertEquals(ranges.get(0)[0], 12);
    assertEquals(ranges.get(0)[1], 12);

    map.put(2, new int[] { 13, 20 }); // 2 maps to 13
    ranges = mapping.getPDBResNumRanges(1, 2);
    assertEquals(ranges.size(), 1);
    assertEquals(ranges.get(0)[0], 12);
    assertEquals(ranges.get(0)[1], 13);

    map.put(3, new int[] { 15, 20 }); // 3 maps to 15 - break
    ranges = mapping.getPDBResNumRanges(1, 5);
    assertEquals(ranges.size(), 2);
    assertEquals(ranges.get(0)[0], 12);
    assertEquals(ranges.get(0)[1], 13);
    assertEquals(ranges.get(1)[0], 15);
    assertEquals(ranges.get(1)[1], 15);
  }

  @Test(groups = "Functional")
  public void testEquals()
  {
    SequenceI seq1 = new Sequence("seq1", "ABCDE");
    SequenceI seq2 = new Sequence("seq1", "ABCDE");
    String pdbFile = "a/b/file1.pdb";
    String pdbId = "1a70";
    String chain = "A";
    String mappingDetails = "these are the mapping details, honest";
    HashMap<Integer, int[]> map = new HashMap<>();

    Mapping seqToPdbMapping = new Mapping(seq1,
            new MapList(new int[]
            { 1, 5 }, new int[] { 2, 6 }, 1, 1));
    StructureMapping sm1 = new StructureMapping(seq1, pdbFile, pdbId, chain,
            map, mappingDetails, seqToPdbMapping);
    assertFalse(sm1.equals(null));
    assertFalse(sm1.equals("x"));

    StructureMapping sm2 = new StructureMapping(seq1, pdbFile, pdbId, chain,
            map, mappingDetails, seqToPdbMapping);
    assertTrue(sm1.equals(sm2));
    assertTrue(sm2.equals(sm1));
    assertEquals(sm1.hashCode(), sm2.hashCode());

    // with different sequence
    sm2 = new StructureMapping(seq2, pdbFile, pdbId, chain, map,
            mappingDetails, seqToPdbMapping);
    assertFalse(sm1.equals(sm2));
    assertFalse(sm2.equals(sm1));

    // with different file
    sm2 = new StructureMapping(seq1, "a/b/file2.pdb", pdbId, chain, map,
            mappingDetails, seqToPdbMapping);
    assertFalse(sm1.equals(sm2));
    assertFalse(sm2.equals(sm1));

    // with different pdbid (case sensitive)
    sm2 = new StructureMapping(seq1, pdbFile, "1A70", chain, map,
            mappingDetails, seqToPdbMapping);
    assertFalse(sm1.equals(sm2));
    assertFalse(sm2.equals(sm1));

    // with different chain
    sm2 = new StructureMapping(seq1, pdbFile, pdbId, "B", map,
            mappingDetails, seqToPdbMapping);
    assertFalse(sm1.equals(sm2));
    assertFalse(sm2.equals(sm1));

    // map is ignore for this test
    sm2 = new StructureMapping(seq1, pdbFile, pdbId, chain, null,
            mappingDetails, seqToPdbMapping);
    assertTrue(sm1.equals(sm2));
    assertTrue(sm2.equals(sm1));

    // with different mapping details
    sm2 = new StructureMapping(seq1, pdbFile, pdbId, chain, map,
            "different details!", seqToPdbMapping);
    assertFalse(sm1.equals(sm2));
    assertFalse(sm2.equals(sm1));

    // with different seq to pdb mapping
    Mapping map2 = new Mapping(seq1,
            new MapList(new int[]
            { 1, 5 }, new int[] { 3, 7 }, 1, 1));
    sm2 = new StructureMapping(seq1, pdbFile, pdbId, chain, map,
            mappingDetails, map2);
    assertFalse(sm1.equals(sm2));
    assertFalse(sm2.equals(sm1));
  }
}
