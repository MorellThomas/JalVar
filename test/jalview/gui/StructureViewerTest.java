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
package jalview.gui;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import jalview.datamodel.PDBEntry;
import jalview.datamodel.PDBEntry.Type;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;

import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class StructureViewerTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = "Functional")
  public void testGetSequencesForPdbs()
  {
    StructureViewer sv = new StructureViewer(null);

    assertNull(sv.getSequencesForPdbs(null, null));

    PDBEntry pdbe1 = new PDBEntry("1A70", "A", Type.PDB, "path1");
    PDBEntry pdbe2 = new PDBEntry("3A6S", "A", Type.PDB, "path2");
    PDBEntry pdbe3 = new PDBEntry("1A70", "B", Type.PDB, "path1");
    PDBEntry pdbe4 = new PDBEntry("1GAQ", "A", Type.PDB, null);
    PDBEntry pdbe5 = new PDBEntry("3A6S", "B", Type.PDB, "path2");
    PDBEntry pdbe6 = new PDBEntry("1GAQ", "B", Type.PDB, null);
    PDBEntry pdbe7 = new PDBEntry("1FOO", "Q", Type.PDB, null);

    PDBEntry[] pdbs = new PDBEntry[] { pdbe1, pdbe2, pdbe3, pdbe4, pdbe5,
        pdbe6, pdbe7 };

    /*
     * seq1 ... seq6 associated with pdbe1 ... pdbe6
     */
    SequenceI[] seqs = new SequenceI[pdbs.length];
    for (int i = 0; i < seqs.length; i++)
    {
      seqs[i] = new Sequence("Seq" + i, "abc");
    }

    /*
     * pdbe3/5/6 should get removed as having a duplicate file path
     */
    Map<PDBEntry, SequenceI[]> uniques = sv.getSequencesForPdbs(pdbs, seqs);
    assertTrue(uniques.containsKey(pdbe1));
    assertTrue(uniques.containsKey(pdbe2));
    assertFalse(uniques.containsKey(pdbe3));
    assertTrue(uniques.containsKey(pdbe4));
    assertFalse(uniques.containsKey(pdbe5));
    assertFalse(uniques.containsKey(pdbe6));
    assertTrue(uniques.containsKey(pdbe7));

    // 1A70 associates with seq1 and seq3
    SequenceI[] ss = uniques.get(pdbe1);
    assertEquals(ss.length, 2);
    assertSame(seqs[0], ss[0]);
    assertSame(seqs[2], ss[1]);

    // 3A6S has seq2 and seq5
    ss = uniques.get(pdbe2);
    assertEquals(ss.length, 2);
    assertSame(seqs[1], ss[0]);
    assertSame(seqs[4], ss[1]);

    // 1GAQ has seq4 and seq6
    ss = uniques.get(pdbe4);
    assertEquals(ss.length, 2);
    assertSame(seqs[3], ss[0]);
    assertSame(seqs[5], ss[1]);

    // 1FOO has seq7
    ss = uniques.get(pdbe7);
    assertEquals(ss.length, 1);
    assertSame(seqs[6], ss[0]);
  }
}
