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
package jalview.commands;

import static org.testng.AssertJUnit.assertEquals;

import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for TrimRegionCommand
 * 
 * @author gmcarstairs
 *
 */
public class TrimRegionCommandTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  private AlignmentI al;

  @BeforeMethod(alwaysRun = true)
  public void setUp()
  {
    SequenceI[] seqs = new SequenceI[2];
    seqs[0] = new Sequence("seq0", "abcde-");
    seqs[1] = new Sequence("seq1", "-ghjkl");
    al = new Alignment(seqs);
    al.setDataset(null);
  }

  /**
   * Test performing, undoing and redoing a 'trim left'
   */
  @Test(groups = { "Functional" })
  public void testTrimLeft_withUndoAndRedo()
  {
    TrimRegionCommand cmd = new TrimRegionCommand("Remove Left", true,
            al.getSequencesArray(), 2, al);
    assertEquals(2, cmd.getSize());
    assertEquals("cde-", al.getSequenceAt(0).getSequenceAsString());
    assertEquals("hjkl", al.getSequenceAt(1).getSequenceAsString());

    /*
     * undo and verify
     */
    cmd.undoCommand(new AlignmentI[] { al });
    assertEquals("abcde-", al.getSequenceAt(0).getSequenceAsString());
    assertEquals("-ghjkl", al.getSequenceAt(1).getSequenceAsString());

    /*
     * redo and verify
     */
    cmd.doCommand(new AlignmentI[] { al });
    assertEquals("cde-", al.getSequenceAt(0).getSequenceAsString());
    assertEquals("hjkl", al.getSequenceAt(1).getSequenceAsString());
  }

  /**
   * Trim left of no columns - should do nothing. This is the case where the
   * first column is selected and 'Remove Left' is selected.
   */
  @Test(groups = { "Functional" })
  public void testTrimLeft_noColumns()
  {
    TrimRegionCommand cmd = new TrimRegionCommand("Remove Left", true,
            al.getSequencesArray(), 0, al);
    assertEquals(0, cmd.getSize());
    assertEquals("abcde-", al.getSequenceAt(0).getSequenceAsString());
    assertEquals("-ghjkl", al.getSequenceAt(1).getSequenceAsString());
  }

  /**
   * Trim left of a single column
   */
  @Test(groups = { "Functional" })
  public void testTrimLeft_oneColumn()
  {
    TrimRegionCommand cmd = new TrimRegionCommand("Remove Left", true,
            al.getSequencesArray(), 1, al);
    assertEquals(1, cmd.getSize());
    assertEquals("bcde-", al.getSequenceAt(0).getSequenceAsString());
    assertEquals("ghjkl", al.getSequenceAt(1).getSequenceAsString());
  }

  /**
   * Trim right of no columns - should do nothing. This is the case where the
   * last column is selected and 'Remove Right' is selected.
   */
  @Test(groups = { "Functional" })
  public void testTrimRight_noColumns()
  {
    TrimRegionCommand cmd = new TrimRegionCommand("Remove Right", false,
            al.getSequencesArray(), 5, al);
    assertEquals(0, cmd.getSize());
    assertEquals("abcde-", al.getSequenceAt(0).getSequenceAsString());
    assertEquals("-ghjkl", al.getSequenceAt(1).getSequenceAsString());
  }

  /**
   * Trim right of a single column
   */
  @Test(groups = { "Functional" })
  public void testTrimRight_oneColumn()
  {
    TrimRegionCommand cmd = new TrimRegionCommand("Remove Right", false,
            al.getSequencesArray(), 4, al);
    assertEquals(1, cmd.getSize());
    assertEquals("abcde", al.getSequenceAt(0).getSequenceAsString());
    assertEquals("-ghjk", al.getSequenceAt(1).getSequenceAsString());
  }

  /**
   * Test performing, undoing and redoing a 'trim right'
   */
  @Test(groups = { "Functional" })
  public void testTrimRight_withUndoAndRedo()
  {
    TrimRegionCommand cmd = new TrimRegionCommand("Remove Right", false,
            al.getSequencesArray(), 2, al);
    assertEquals(3, cmd.getSize());
    assertEquals("abc", al.getSequenceAt(0).getSequenceAsString());
    assertEquals("-gh", al.getSequenceAt(1).getSequenceAsString());

    /*
     * undo and verify
     */
    cmd.undoCommand(new AlignmentI[] { al });
    assertEquals("abcde-", al.getSequenceAt(0).getSequenceAsString());
    assertEquals("-ghjkl", al.getSequenceAt(1).getSequenceAsString());

    /*
     * redo and verify
     */
    cmd.doCommand(new AlignmentI[] { al });
    assertEquals("abc", al.getSequenceAt(0).getSequenceAsString());
    assertEquals("-gh", al.getSequenceAt(1).getSequenceAsString());
  }
}
