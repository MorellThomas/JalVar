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

import java.util.Locale;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import jalview.commands.EditCommand.Action;
import jalview.commands.EditCommand.Edit;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.datamodel.features.SequenceFeatures;
import jalview.gui.JvOptionPane;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for EditCommand
 * 
 * @author gmcarstairs
 *
 */
public class EditCommandTest
{
  private static Comparator<SequenceFeature> BY_DESCRIPTION = new Comparator<SequenceFeature>()
  {

    @Override
    public int compare(SequenceFeature o1, SequenceFeature o2)
    {
      return o1.getDescription().compareTo(o2.getDescription());
    }
  };

  private EditCommand testee;

  private SequenceI[] seqs;

  private Alignment al;

  /*
   * compute n(n+1)/2 e.g. 
   * func(5) = 5 + 4 + 3 + 2 + 1 = 15
   */
  private static int func(int i)
  {
    return i * (i + 1) / 2;
  }

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @BeforeMethod(alwaysRun = true)
  public void setUp()
  {
    testee = new EditCommand();
    seqs = new SequenceI[4];
    seqs[0] = new Sequence("seq0", "abcdefghjk");
    seqs[0].setDatasetSequence(new Sequence("seq0ds", "ABCDEFGHJK"));
    seqs[1] = new Sequence("seq1", "fghjklmnopq");
    seqs[1].setDatasetSequence(new Sequence("seq1ds", "FGHJKLMNOPQ"));
    seqs[2] = new Sequence("seq2", "qrstuvwxyz");
    seqs[2].setDatasetSequence(new Sequence("seq2ds", "QRSTUVWXYZ"));
    seqs[3] = new Sequence("seq3", "1234567890");
    seqs[3].setDatasetSequence(new Sequence("seq3ds", "1234567890"));
    al = new Alignment(seqs);
    al.setGapCharacter('?');
  }

  /**
   * Test inserting gap characters
   */
  @Test(groups = { "Functional" })
  public void testAppendEdit_insertGap()
  {
    // set a non-standard gap character to prove it is actually used
    testee.appendEdit(Action.INSERT_GAP, seqs, 4, 3, al, true);
    assertEquals("abcd???efghjk", seqs[0].getSequenceAsString());
    assertEquals("fghj???klmnopq", seqs[1].getSequenceAsString());
    assertEquals("qrst???uvwxyz", seqs[2].getSequenceAsString());
    assertEquals("1234???567890", seqs[3].getSequenceAsString());

    // todo: test for handling out of range positions?
  }

  /**
   * Test deleting characters from sequences. Note the deleteGap() action does
   * not check that only gap characters are being removed.
   */
  @Test(groups = { "Functional" })
  public void testAppendEdit_deleteGap()
  {
    testee.appendEdit(Action.DELETE_GAP, seqs, 4, 3, al, true);
    assertEquals("abcdhjk", seqs[0].getSequenceAsString());
    assertEquals("fghjnopq", seqs[1].getSequenceAsString());
    assertEquals("qrstxyz", seqs[2].getSequenceAsString());
    assertEquals("1234890", seqs[3].getSequenceAsString());
  }

  /**
   * Test a cut action. The command should store the cut characters to support
   * undo.
   */
  @Test(groups = { "Functional" })
  public void testCut()
  {
    Edit ec = testee.new Edit(Action.CUT, seqs, 4, 3, al);
    EditCommand.cut(ec, new AlignmentI[] { al });
    assertEquals("abcdhjk", seqs[0].getSequenceAsString());
    assertEquals("fghjnopq", seqs[1].getSequenceAsString());
    assertEquals("qrstxyz", seqs[2].getSequenceAsString());
    assertEquals("1234890", seqs[3].getSequenceAsString());

    assertEquals("efg", new String(ec.string[0]));
    assertEquals("klm", new String(ec.string[1]));
    assertEquals("uvw", new String(ec.string[2]));
    assertEquals("567", new String(ec.string[3]));
    // TODO: case where whole sequence is deleted as nothing left; etc
  }

  /**
   * Test a Paste action, followed by Undo and Redo
   */
  @Test(groups = { "Functional" }, enabled = false)
  public void testPaste_undo_redo()
  {
    // TODO code this test properly, bearing in mind that:
    // Paste action requires something on the clipboard (Cut/Copy)
    // - EditCommand.paste doesn't add sequences to the alignment
    // ... that is done in AlignFrame.paste()
    // ... unless as a Redo
    // ...

    SequenceI[] newSeqs = new SequenceI[2];
    newSeqs[0] = new Sequence("newseq0", "ACEFKL");
    newSeqs[1] = new Sequence("newseq1", "JWMPDH");

    new EditCommand("Paste", Action.PASTE, newSeqs, 0, al.getWidth(), al);
    assertEquals(6, al.getSequences().size());
    assertEquals("1234567890", seqs[3].getSequenceAsString());
    assertEquals("ACEFKL", seqs[4].getSequenceAsString());
    assertEquals("JWMPDH", seqs[5].getSequenceAsString());
  }

  /**
   * Test insertGap followed by undo command
   */
  @Test(groups = { "Functional" })
  public void testUndo_insertGap()
  {
    // Edit ec = testee.new Edit(Action.INSERT_GAP, seqs, 4, 3, '?');
    testee.appendEdit(Action.INSERT_GAP, seqs, 4, 3, al, true);
    // check something changed
    assertEquals("abcd???efghjk", seqs[0].getSequenceAsString());
    testee.undoCommand(new AlignmentI[] { al });
    assertEquals("abcdefghjk", seqs[0].getSequenceAsString());
    assertEquals("fghjklmnopq", seqs[1].getSequenceAsString());
    assertEquals("qrstuvwxyz", seqs[2].getSequenceAsString());
    assertEquals("1234567890", seqs[3].getSequenceAsString());
  }

  /**
   * Test deleteGap followed by undo command
   */
  @Test(groups = { "Functional" })
  public void testUndo_deleteGap()
  {
    testee.appendEdit(Action.DELETE_GAP, seqs, 4, 3, al, true);
    // check something changed
    assertEquals("abcdhjk", seqs[0].getSequenceAsString());
    testee.undoCommand(new AlignmentI[] { al });
    // deleteGap doesn't 'remember' deleted characters, only gaps get put back
    assertEquals("abcd???hjk", seqs[0].getSequenceAsString());
    assertEquals("fghj???nopq", seqs[1].getSequenceAsString());
    assertEquals("qrst???xyz", seqs[2].getSequenceAsString());
    assertEquals("1234???890", seqs[3].getSequenceAsString());
  }

  /**
   * Test several commands followed by an undo command
   */
  @Test(groups = { "Functional" })
  public void testUndo_multipleCommands()
  {
    // delete positions 3/4/5 (counting from 1)
    testee.appendEdit(Action.DELETE_GAP, seqs, 2, 3, al, true);
    assertEquals("abfghjk", seqs[0].getSequenceAsString());
    assertEquals("1267890", seqs[3].getSequenceAsString());

    // insert 2 gaps after the second residue
    testee.appendEdit(Action.INSERT_GAP, seqs, 2, 2, al, true);
    assertEquals("ab??fghjk", seqs[0].getSequenceAsString());
    assertEquals("12??67890", seqs[3].getSequenceAsString());

    // delete positions 4/5/6
    testee.appendEdit(Action.DELETE_GAP, seqs, 3, 3, al, true);
    assertEquals("ab?hjk", seqs[0].getSequenceAsString());
    assertEquals("12?890", seqs[3].getSequenceAsString());

    // undo edit commands
    testee.undoCommand(new AlignmentI[] { al });
    assertEquals("ab?????hjk", seqs[0].getSequenceAsString());
    assertEquals("12?????890", seqs[3].getSequenceAsString());
  }

  /**
   * Unit test for JAL-1594 bug: click and drag sequence right to insert gaps -
   * undo did not remove them all.
   */
  @Test(groups = { "Functional" })
  public void testUndo_multipleInsertGaps()
  {
    testee.appendEdit(Action.INSERT_GAP, seqs, 4, 1, al, true);
    testee.appendEdit(Action.INSERT_GAP, seqs, 5, 1, al, true);
    testee.appendEdit(Action.INSERT_GAP, seqs, 6, 1, al, true);

    // undo edit commands
    testee.undoCommand(new AlignmentI[] { al });
    assertEquals("abcdefghjk", seqs[0].getSequenceAsString());
    assertEquals("1234567890", seqs[3].getSequenceAsString());

  }

  /**
   * Test cut followed by undo command
   */
  @Test(groups = { "Functional" })
  public void testUndo_cut()
  {
    testee.appendEdit(Action.CUT, seqs, 4, 3, al, true);
    // check something changed
    assertEquals("abcdhjk", seqs[0].getSequenceAsString());
    testee.undoCommand(new AlignmentI[] { al });
    assertEquals("abcdefghjk", seqs[0].getSequenceAsString());
    assertEquals("fghjklmnopq", seqs[1].getSequenceAsString());
    assertEquals("qrstuvwxyz", seqs[2].getSequenceAsString());
    assertEquals("1234567890", seqs[3].getSequenceAsString());
  }

  /**
   * Test the replace command (used to manually edit a sequence)
   */
  @Test(groups = { "Functional" })
  public void testReplace()
  {
    // seem to need a dataset sequence on the edited sequence here
    seqs[1].createDatasetSequence();
    assertEquals("fghjklmnopq", seqs[1].getSequenceAsString());
    // NB command.number holds end position for a Replace command
    new EditCommand("", Action.REPLACE, "Z-xY", new SequenceI[] { seqs[1] },
            4, 8, al);
    assertEquals("abcdefghjk", seqs[0].getSequenceAsString());
    assertEquals("fghjZ-xYopq", seqs[1].getSequenceAsString());
    // Dataset Sequence should always be uppercase
    assertEquals("fghjZxYopq".toUpperCase(Locale.ROOT),
            seqs[1].getDatasetSequence().getSequenceAsString());
    assertEquals("qrstuvwxyz", seqs[2].getSequenceAsString());
    assertEquals("1234567890", seqs[3].getSequenceAsString());
  }

  /**
   * Test the replace command (used to manually edit a sequence)
   */
  @Test(groups = { "Functional" })
  public void testReplace_withGaps()
  {
    SequenceI seq = new Sequence("seq", "ABC--DEF");
    seq.createDatasetSequence();
    assertEquals("ABCDEF", seq.getDatasetSequence().getSequenceAsString());
    assertEquals(1, seq.getStart());
    assertEquals(6, seq.getEnd());

    /*
     * replace C- with XYZ
     * NB arg4 = start column of selection for edit (base 0)
     * arg5 = column after end of selection for edit
     */
    EditCommand edit = new EditCommand("", Action.REPLACE, "xyZ",
            new SequenceI[]
            { seq }, 2, 4, al);
    assertEquals("ABxyZ-DEF", seq.getSequenceAsString());
    assertEquals(1, seq.getStart());
    assertEquals(8, seq.getEnd());
    // Dataset sequence always uppercase
    assertEquals("ABxyZDEF".toUpperCase(Locale.ROOT),
            seq.getDatasetSequence().getSequenceAsString());
    assertEquals(8, seq.getDatasetSequence().getEnd());

    /*
     * undo the edit
     */
    AlignmentI[] views = new AlignmentI[] {
        new Alignment(new SequenceI[]
        { seq }) };
    edit.undoCommand(views);

    assertEquals("ABC--DEF", seq.getSequenceAsString());
    assertEquals("ABCDEF", seq.getDatasetSequence().getSequenceAsString());
    assertEquals(1, seq.getStart());
    assertEquals(6, seq.getEnd());
    assertEquals(6, seq.getDatasetSequence().getEnd());

    /*
     * redo the edit
     */
    edit.doCommand(views);

    assertEquals("ABxyZ-DEF", seq.getSequenceAsString());
    assertEquals(1, seq.getStart());
    assertEquals(8, seq.getEnd());
    // dataset sequence should be Uppercase
    assertEquals("ABxyZDEF".toUpperCase(Locale.ROOT),
            seq.getDatasetSequence().getSequenceAsString());
    assertEquals(8, seq.getDatasetSequence().getEnd());

  }

  /**
   * Test replace command when it doesn't cause a sequence edit (see comment in
   */
  @Test(groups = { "Functional" })
  public void testReplaceFirstResiduesWithGaps()
  {
    // test replace when gaps are inserted at start. Start/end should change
    // w.r.t. original edited sequence.
    SequenceI dsseq = seqs[1].getDatasetSequence();
    EditCommand edit = new EditCommand("", Action.REPLACE, "----",
            new SequenceI[]
            { seqs[1] }, 0, 4, al);

    // trimmed start
    assertEquals("----klmnopq", seqs[1].getSequenceAsString());
    // and ds is preserved
    assertTrue(dsseq == seqs[1].getDatasetSequence());
    // and it is unchanged and UPPERCASE !
    assertEquals("fghjklmnopq".toUpperCase(Locale.ROOT),
            dsseq.getSequenceAsString());
    // and that alignment sequence start has been adjusted
    assertEquals(5, seqs[1].getStart());
    assertEquals(11, seqs[1].getEnd());

    AlignmentI[] views = new AlignmentI[] { new Alignment(seqs) };
    // and undo
    edit.undoCommand(views);

    // dataset sequence unchanged
    assertTrue(dsseq == seqs[1].getDatasetSequence());
    // restore sequence
    assertEquals("fghjklmnopq", seqs[1].getSequenceAsString());
    // and start/end numbering also restored
    assertEquals(1, seqs[1].getStart());
    assertEquals(11, seqs[1].getEnd());

    // now redo
    edit.undoCommand(views);

    // and repeat asserts for the original edit

    // trimmed start
    assertEquals("----klmnopq", seqs[1].getSequenceAsString());
    // and ds is preserved
    assertTrue(dsseq == seqs[1].getDatasetSequence());
    // and it is unchanged AND UPPERCASE !
    assertEquals("fghjklmnopq".toUpperCase(Locale.ROOT),
            dsseq.getSequenceAsString());
    // and that alignment sequence start has been adjusted
    assertEquals(5, seqs[1].getStart());
    assertEquals(11, seqs[1].getEnd());

  }

  /**
   * Test that the addEdit command correctly merges insert gap commands when
   * possible.
   */
  @Test(groups = { "Functional" })
  public void testAddEdit_multipleInsertGap()
  {
    /*
     * 3 insert gap in a row (aka mouse drag right):
     */
    Edit e = new EditCommand().new Edit(Action.INSERT_GAP,
            new SequenceI[]
            { seqs[0] }, 1, 1, al);
    testee.addEdit(e);
    SequenceI edited = new Sequence("seq0", "a?bcdefghjk");
    edited.setDatasetSequence(seqs[0].getDatasetSequence());
    e = new EditCommand().new Edit(Action.INSERT_GAP,
            new SequenceI[]
            { edited }, 2, 1, al);
    testee.addEdit(e);
    edited = new Sequence("seq0", "a??bcdefghjk");
    edited.setDatasetSequence(seqs[0].getDatasetSequence());
    e = new EditCommand().new Edit(Action.INSERT_GAP,
            new SequenceI[]
            { edited }, 3, 1, al);
    testee.addEdit(e);
    assertEquals(1, testee.getSize());
    assertEquals(Action.INSERT_GAP, testee.getEdit(0).getAction());
    assertEquals(1, testee.getEdit(0).getPosition());
    assertEquals(3, testee.getEdit(0).getNumber());

    /*
     * Add a non-contiguous edit - should not be merged.
     */
    e = new EditCommand().new Edit(Action.INSERT_GAP,
            new SequenceI[]
            { edited }, 5, 2, al);
    testee.addEdit(e);
    assertEquals(2, testee.getSize());
    assertEquals(5, testee.getEdit(1).getPosition());
    assertEquals(2, testee.getEdit(1).getNumber());

    /*
     * Add a Delete after the Insert - should not be merged.
     */
    e = new EditCommand().new Edit(Action.DELETE_GAP,
            new SequenceI[]
            { edited }, 6, 2, al);
    testee.addEdit(e);
    assertEquals(3, testee.getSize());
    assertEquals(Action.DELETE_GAP, testee.getEdit(2).getAction());
    assertEquals(6, testee.getEdit(2).getPosition());
    assertEquals(2, testee.getEdit(2).getNumber());
  }

  /**
   * Test that the addEdit command correctly merges delete gap commands when
   * possible.
   */
  @Test(groups = { "Functional" })
  public void testAddEdit_multipleDeleteGap()
  {
    /*
     * 3 delete gap in a row (aka mouse drag left):
     */
    seqs[0].setSequence("a???bcdefghjk");
    Edit e = new EditCommand().new Edit(Action.DELETE_GAP,
            new SequenceI[]
            { seqs[0] }, 4, 1, al);
    testee.addEdit(e);
    assertEquals(1, testee.getSize());

    SequenceI edited = new Sequence("seq0", "a??bcdefghjk");
    edited.setDatasetSequence(seqs[0].getDatasetSequence());
    e = new EditCommand().new Edit(Action.DELETE_GAP,
            new SequenceI[]
            { edited }, 3, 1, al);
    testee.addEdit(e);
    assertEquals(1, testee.getSize());

    edited = new Sequence("seq0", "a?bcdefghjk");
    edited.setDatasetSequence(seqs[0].getDatasetSequence());
    e = new EditCommand().new Edit(Action.DELETE_GAP,
            new SequenceI[]
            { edited }, 2, 1, al);
    testee.addEdit(e);
    assertEquals(1, testee.getSize());
    assertEquals(Action.DELETE_GAP, testee.getEdit(0).getAction());
    assertEquals(2, testee.getEdit(0).getPosition());
    assertEquals(3, testee.getEdit(0).getNumber());

    /*
     * Add a non-contiguous edit - should not be merged.
     */
    e = new EditCommand().new Edit(Action.DELETE_GAP,
            new SequenceI[]
            { edited }, 2, 1, al);
    testee.addEdit(e);
    assertEquals(2, testee.getSize());
    assertEquals(Action.DELETE_GAP, testee.getEdit(0).getAction());
    assertEquals(2, testee.getEdit(1).getPosition());
    assertEquals(1, testee.getEdit(1).getNumber());

    /*
     * Add an Insert after the Delete - should not be merged.
     */
    e = new EditCommand().new Edit(Action.INSERT_GAP,
            new SequenceI[]
            { edited }, 1, 1, al);
    testee.addEdit(e);
    assertEquals(3, testee.getSize());
    assertEquals(Action.INSERT_GAP, testee.getEdit(2).getAction());
    assertEquals(1, testee.getEdit(2).getPosition());
    assertEquals(1, testee.getEdit(2).getNumber());
  }

  /**
   * Test that the addEdit command correctly handles 'remove gaps' edits for the
   * case when they appear contiguous but are acting on different sequences.
   * They should not be merged.
   */
  @Test(groups = { "Functional" })
  public void testAddEdit_removeAllGaps()
  {
    seqs[0].setSequence("a???bcdefghjk");
    Edit e = new EditCommand().new Edit(Action.DELETE_GAP,
            new SequenceI[]
            { seqs[0] }, 4, 1, al);
    testee.addEdit(e);

    seqs[1].setSequence("f??ghjklmnopq");
    Edit e2 = new EditCommand().new Edit(Action.DELETE_GAP,
            new SequenceI[]
            { seqs[1] }, 3, 1, al);
    testee.addEdit(e2);
    assertEquals(2, testee.getSize());
    assertSame(e, testee.getEdit(0));
    assertSame(e2, testee.getEdit(1));
  }

  /**
   * Test that the addEdit command correctly merges insert gap commands acting
   * on a multi-sequence selection.
   */
  @Test(groups = { "Functional" })
  public void testAddEdit_groupInsertGaps()
  {
    /*
     * 2 insert gap in a row (aka mouse drag right), on two sequences:
     */
    Edit e = new EditCommand().new Edit(Action.INSERT_GAP,
            new SequenceI[]
            { seqs[0], seqs[1] }, 1, 1, al);
    testee.addEdit(e);
    SequenceI seq1edited = new Sequence("seq0", "a?bcdefghjk");
    seq1edited.setDatasetSequence(seqs[0].getDatasetSequence());
    SequenceI seq2edited = new Sequence("seq1", "f?ghjklmnopq");
    seq2edited.setDatasetSequence(seqs[1].getDatasetSequence());
    e = new EditCommand().new Edit(Action.INSERT_GAP,
            new SequenceI[]
            { seq1edited, seq2edited }, 2, 1, al);
    testee.addEdit(e);

    assertEquals(1, testee.getSize());
    assertEquals(Action.INSERT_GAP, testee.getEdit(0).getAction());
    assertEquals(1, testee.getEdit(0).getPosition());
    assertEquals(2, testee.getEdit(0).getNumber());
    assertEquals(seqs[0].getDatasetSequence(),
            testee.getEdit(0).getSequences()[0].getDatasetSequence());
    assertEquals(seqs[1].getDatasetSequence(),
            testee.getEdit(0).getSequences()[1].getDatasetSequence());
  }

  /**
   * Test for 'undoing' a series of gap insertions.
   * <ul>
   * <li>Start: ABCDEF insert 2 at pos 1</li>
   * <li>next: A--BCDEF insert 1 at pos 4</li>
   * <li>next: A--B-CDEF insert 2 at pos 0</li>
   * <li>last: --A--B-CDEF</li>
   * </ul>
   */
  @Test(groups = { "Functional" })
  public void testPriorState_multipleInserts()
  {
    EditCommand command = new EditCommand();
    SequenceI seq = new Sequence("", "--A--B-CDEF");
    SequenceI ds = new Sequence("", "ABCDEF");
    seq.setDatasetSequence(ds);
    SequenceI[] sqs = new SequenceI[] { seq };
    Edit e = command.new Edit(Action.INSERT_GAP, sqs, 1, 2, '-');
    command.addEdit(e);
    e = command.new Edit(Action.INSERT_GAP, sqs, 4, 1, '-');
    command.addEdit(e);
    e = command.new Edit(Action.INSERT_GAP, sqs, 0, 2, '-');
    command.addEdit(e);

    Map<SequenceI, SequenceI> unwound = command.priorState(false);
    assertEquals("ABCDEF", unwound.get(ds).getSequenceAsString());
  }

  /**
   * Test for 'undoing' a series of gap deletions.
   * <ul>
   * <li>Start: A-B-C delete 1 at pos 1</li>
   * <li>Next: AB-C delete 1 at pos 2</li>
   * <li>End: ABC</li>
   * </ul>
   */
  @Test(groups = { "Functional" })
  public void testPriorState_removeAllGaps()
  {
    EditCommand command = new EditCommand();
    SequenceI seq = new Sequence("", "ABC");
    SequenceI ds = new Sequence("", "ABC");
    seq.setDatasetSequence(ds);
    SequenceI[] sqs = new SequenceI[] { seq };
    Edit e = command.new Edit(Action.DELETE_GAP, sqs, 1, 1, '-');
    command.addEdit(e);
    e = command.new Edit(Action.DELETE_GAP, sqs, 2, 1, '-');
    command.addEdit(e);

    Map<SequenceI, SequenceI> unwound = command.priorState(false);
    assertEquals("A-B-C", unwound.get(ds).getSequenceAsString());
  }

  /**
   * Test for 'undoing' a single delete edit.
   */
  @Test(groups = { "Functional" })
  public void testPriorState_singleDelete()
  {
    EditCommand command = new EditCommand();
    SequenceI seq = new Sequence("", "ABCDEF");
    SequenceI ds = new Sequence("", "ABCDEF");
    seq.setDatasetSequence(ds);
    SequenceI[] sqs = new SequenceI[] { seq };
    Edit e = command.new Edit(Action.DELETE_GAP, sqs, 2, 2, '-');
    command.addEdit(e);

    Map<SequenceI, SequenceI> unwound = command.priorState(false);
    assertEquals("AB--CDEF", unwound.get(ds).getSequenceAsString());
  }

  /**
   * Test 'undoing' a single gap insertion edit command.
   */
  @Test(groups = { "Functional" })
  public void testPriorState_singleInsert()
  {
    EditCommand command = new EditCommand();
    SequenceI seq = new Sequence("", "AB---CDEF");
    SequenceI ds = new Sequence("", "ABCDEF");
    seq.setDatasetSequence(ds);
    SequenceI[] sqs = new SequenceI[] { seq };
    Edit e = command.new Edit(Action.INSERT_GAP, sqs, 2, 3, '-');
    command.addEdit(e);

    Map<SequenceI, SequenceI> unwound = command.priorState(false);
    SequenceI prior = unwound.get(ds);
    assertEquals("ABCDEF", prior.getSequenceAsString());
    assertEquals(1, prior.getStart());
    assertEquals(6, prior.getEnd());
  }

  /**
   * Test 'undoing' a single gap insertion edit command, on a sequence whose
   * start residue is other than 1
   */
  @Test(groups = { "Functional" })
  public void testPriorState_singleInsertWithOffset()
  {
    EditCommand command = new EditCommand();
    SequenceI seq = new Sequence("", "AB---CDEF", 8, 13);
    // SequenceI ds = new Sequence("", "ABCDEF", 8, 13);
    // seq.setDatasetSequence(ds);
    seq.createDatasetSequence();
    SequenceI[] sqs = new SequenceI[] { seq };
    Edit e = command.new Edit(Action.INSERT_GAP, sqs, 2, 3, '-');
    command.addEdit(e);

    Map<SequenceI, SequenceI> unwound = command.priorState(false);
    SequenceI prior = unwound.get(seq.getDatasetSequence());
    assertEquals("ABCDEF", prior.getSequenceAsString());
    assertEquals(8, prior.getStart());
    assertEquals(13, prior.getEnd());
  }

  /**
   * Test that mimics 'remove all gaps' action. This generates delete gap edits
   * for contiguous gaps in each sequence separately.
   */
  @Test(groups = { "Functional" })
  public void testPriorState_removeGapsMultipleSeqs()
  {
    EditCommand command = new EditCommand();
    String original1 = "--ABC-DEF";
    String original2 = "FG-HI--J";
    String original3 = "M-NOPQ";

    /*
     * Two edits for the first sequence
     */
    SequenceI seq = new Sequence("", "ABC-DEF");
    SequenceI ds1 = new Sequence("", "ABCDEF");
    seq.setDatasetSequence(ds1);
    SequenceI[] sqs = new SequenceI[] { seq };
    Edit e = command.new Edit(Action.DELETE_GAP, sqs, 0, 2, '-');
    command.addEdit(e);
    seq = new Sequence("", "ABCDEF");
    seq.setDatasetSequence(ds1);
    sqs = new SequenceI[] { seq };
    e = command.new Edit(Action.DELETE_GAP, sqs, 3, 1, '-');
    command.addEdit(e);

    /*
     * Two edits for the second sequence
     */
    seq = new Sequence("", "FGHI--J");
    SequenceI ds2 = new Sequence("", "FGHIJ");
    seq.setDatasetSequence(ds2);
    sqs = new SequenceI[] { seq };
    e = command.new Edit(Action.DELETE_GAP, sqs, 2, 1, '-');
    command.addEdit(e);
    seq = new Sequence("", "FGHIJ");
    seq.setDatasetSequence(ds2);
    sqs = new SequenceI[] { seq };
    e = command.new Edit(Action.DELETE_GAP, sqs, 4, 2, '-');
    command.addEdit(e);

    /*
     * One edit for the third sequence.
     */
    seq = new Sequence("", "MNOPQ");
    SequenceI ds3 = new Sequence("", "MNOPQ");
    seq.setDatasetSequence(ds3);
    sqs = new SequenceI[] { seq };
    e = command.new Edit(Action.DELETE_GAP, sqs, 1, 1, '-');
    command.addEdit(e);

    Map<SequenceI, SequenceI> unwound = command.priorState(false);
    assertEquals(original1, unwound.get(ds1).getSequenceAsString());
    assertEquals(original2, unwound.get(ds2).getSequenceAsString());
    assertEquals(original3, unwound.get(ds3).getSequenceAsString());
  }

  /**
   * Test that mimics 'remove all gapped columns' action. This generates a
   * series Delete Gap edits that each act on all sequences that share a gapped
   * column region.
   */
  @Test(groups = { "Functional" })
  public void testPriorState_removeGappedCols()
  {
    EditCommand command = new EditCommand();
    String original1 = "--ABC--DEF";
    String original2 = "-G-HI--J";
    String original3 = "-M-NO--PQ";

    /*
     * First edit deletes the first column.
     */
    SequenceI seq1 = new Sequence("", "-ABC--DEF");
    SequenceI ds1 = new Sequence("", "ABCDEF");
    seq1.setDatasetSequence(ds1);
    SequenceI seq2 = new Sequence("", "G-HI--J");
    SequenceI ds2 = new Sequence("", "GHIJ");
    seq2.setDatasetSequence(ds2);
    SequenceI seq3 = new Sequence("", "M-NO--PQ");
    SequenceI ds3 = new Sequence("", "MNOPQ");
    seq3.setDatasetSequence(ds3);
    SequenceI[] sqs = new SequenceI[] { seq1, seq2, seq3 };
    Edit e = command.new Edit(Action.DELETE_GAP, sqs, 0, 1, '-');
    command.addEdit(e);

    /*
     * Second edit deletes what is now columns 4 and 5.
     */
    seq1 = new Sequence("", "-ABCDEF");
    seq1.setDatasetSequence(ds1);
    seq2 = new Sequence("", "G-HIJ");
    seq2.setDatasetSequence(ds2);
    seq3 = new Sequence("", "M-NOPQ");
    seq3.setDatasetSequence(ds3);
    sqs = new SequenceI[] { seq1, seq2, seq3 };
    e = command.new Edit(Action.DELETE_GAP, sqs, 4, 2, '-');
    command.addEdit(e);

    Map<SequenceI, SequenceI> unwound = command.priorState(false);
    assertEquals(original1, unwound.get(ds1).getSequenceAsString());
    assertEquals(original2, unwound.get(ds2).getSequenceAsString());
    assertEquals(original3, unwound.get(ds3).getSequenceAsString());
    assertEquals(ds1, unwound.get(ds1).getDatasetSequence());
    assertEquals(ds2, unwound.get(ds2).getDatasetSequence());
    assertEquals(ds3, unwound.get(ds3).getDatasetSequence());
  }

  /**
   * Test a cut action's relocation of sequence features
   */
  @Test(groups = { "Functional" })
  public void testCut_withFeatures()
  {
    /*
     * create sequence features before, after and overlapping
     * a cut of columns/residues 4-7
     */
    SequenceI seq0 = seqs[0]; // abcdefghjk/1-10
    seq0.addSequenceFeature(
            new SequenceFeature("before", "", 1, 3, 0f, null));
    seq0.addSequenceFeature(
            new SequenceFeature("overlap left", "", 2, 6, 0f, null));
    seq0.addSequenceFeature(
            new SequenceFeature("internal", "", 5, 6, 0f, null));
    seq0.addSequenceFeature(
            new SequenceFeature("overlap right", "", 7, 8, 0f, null));
    seq0.addSequenceFeature(
            new SequenceFeature("after", "", 8, 10, 0f, null));

    /*
     * add some contact features
     */
    SequenceFeature internalContact = new SequenceFeature("disulphide bond",
            "", 5, 6, 0f, null);
    seq0.addSequenceFeature(internalContact); // should get deleted
    SequenceFeature overlapLeftContact = new SequenceFeature(
            "disulphide bond", "", 2, 6, 0f, null);
    seq0.addSequenceFeature(overlapLeftContact); // should get deleted
    SequenceFeature overlapRightContact = new SequenceFeature(
            "disulphide bond", "", 5, 8, 0f, null);
    seq0.addSequenceFeature(overlapRightContact); // should get deleted
    SequenceFeature spanningContact = new SequenceFeature("disulphide bond",
            "", 2, 9, 0f, null);
    seq0.addSequenceFeature(spanningContact); // should get shortened 3'

    /*
     * cut columns 3-6 (base 0), residues d-g 4-7
     */
    Edit ec = testee.new Edit(Action.CUT, seqs, 3, 4, al); // cols 3-6 base 0
    EditCommand.cut(ec, new AlignmentI[] { al });

    List<SequenceFeature> sfs = seq0.getSequenceFeatures();
    SequenceFeatures.sortFeatures(sfs, true);

    assertEquals(5, sfs.size()); // features internal to cut were deleted
    SequenceFeature sf = sfs.get(0);
    assertEquals("before", sf.getType());
    assertEquals(1, sf.getBegin());
    assertEquals(3, sf.getEnd());
    sf = sfs.get(1);
    assertEquals("disulphide bond", sf.getType());
    assertEquals(2, sf.getBegin());
    assertEquals(5, sf.getEnd()); // truncated by cut
    sf = sfs.get(2);
    assertEquals("overlap left", sf.getType());
    assertEquals(2, sf.getBegin());
    assertEquals(3, sf.getEnd()); // truncated by cut
    sf = sfs.get(3);
    assertEquals("after", sf.getType());
    assertEquals(4, sf.getBegin()); // shifted left by cut
    assertEquals(6, sf.getEnd()); // shifted left by cut
    sf = sfs.get(4);
    assertEquals("overlap right", sf.getType());
    assertEquals(4, sf.getBegin()); // shifted left by cut
    assertEquals(4, sf.getEnd()); // truncated by cut
  }

  /**
   * Test a cut action's relocation of sequence features, with full coverage of
   * all possible feature and cut locations for a 5-position ungapped sequence
   */
  @Test(groups = { "Functional" })
  public void testCut_withFeatures_exhaustive()
  {
    /*
     * create a sequence features on each subrange of 1-5
     */
    SequenceI seq0 = new Sequence("seq", "ABCDE");
    int start = 8;
    int end = 12;
    seq0.setStart(start);
    seq0.setEnd(end);
    AlignmentI alignment = new Alignment(new SequenceI[] { seq0 });
    alignment.setDataset(null);

    /*
     * create a new alignment with shared dataset sequence
     */
    AlignmentI copy = new Alignment(
            new SequenceI[]
            { alignment.getDataset().getSequenceAt(0).deriveSequence() });
    SequenceI copySeq0 = copy.getSequenceAt(0);

    for (int from = start; from <= end; from++)
    {
      for (int to = from; to <= end; to++)
      {
        String desc = String.format("%d-%d", from, to);
        SequenceFeature sf = new SequenceFeature("test", desc, from, to, 0f,
                null);
        sf.setValue("from", Integer.valueOf(from));
        sf.setValue("to", Integer.valueOf(to));
        seq0.addSequenceFeature(sf);
      }
    }
    // sanity check
    List<SequenceFeature> sfs = seq0.getSequenceFeatures();
    assertEquals(func(5), sfs.size());
    assertEquals(sfs, copySeq0.getSequenceFeatures());
    String copySequenceFeatures = copySeq0.getSequenceFeatures().toString();

    /*
     * now perform all possible cuts of subranges of columns 1-5
     * and validate the resulting remaining sequence features!
     */
    SequenceI[] sqs = new SequenceI[] { seq0 };

    for (int from = 0; from < seq0.getLength(); from++)
    {
      for (int to = from; to < seq0.getLength(); to++)
      {
        EditCommand ec = new EditCommand("Cut", Action.CUT, sqs, from,
                (to - from + 1), alignment);
        final String msg = String.format("Cut %d-%d ", from + 1, to + 1);
        boolean newDatasetSequence = copySeq0.getDatasetSequence() != seq0
                .getDatasetSequence();

        verifyCut(seq0, from, to, msg, start);

        /*
         * verify copy alignment dataset sequence unaffected
         */
        assertEquals("Original dataset sequence was modified",
                copySequenceFeatures,
                copySeq0.getSequenceFeatures().toString());

        /*
         * verify any new dataset sequence was added to the
         * alignment dataset
         */
        assertEquals("Wrong Dataset size after " + msg,
                newDatasetSequence ? 2 : 1,
                alignment.getDataset().getHeight());

        /*
         * undo and verify all restored
         */
        AlignmentI[] views = new AlignmentI[] { alignment };
        ec.undoCommand(views);
        sfs = seq0.getSequenceFeatures();
        assertEquals("After undo of " + msg, func(5), sfs.size());
        verifyUndo(from, to, sfs);

        /*
         * verify copy alignment dataset sequence still unaffected
         * and alignment dataset has shrunk (if it was added to)
         */
        assertEquals("Original dataset sequence was modified",
                copySequenceFeatures,
                copySeq0.getSequenceFeatures().toString());
        assertEquals("Wrong Dataset size after Undo of " + msg, 1,
                alignment.getDataset().getHeight());

        /*
         * redo and verify
         */
        ec.doCommand(views);
        verifyCut(seq0, from, to, msg, start);

        /*
         * verify copy alignment dataset sequence unaffected
         * and any new dataset sequence readded to alignment dataset
         */
        assertEquals("Original dataset sequence was modified",
                copySequenceFeatures,
                copySeq0.getSequenceFeatures().toString());
        assertEquals("Wrong Dataset size after Redo of " + msg,
                newDatasetSequence ? 2 : 1,
                alignment.getDataset().getHeight());

        /*
         * undo ready for next cut
         */
        ec.undoCommand(views);

        /*
         * final verify that copy alignment dataset sequence is still unaffected
         * and that alignment dataset has shrunk
         */
        assertEquals("Original dataset sequence was modified",
                copySequenceFeatures,
                copySeq0.getSequenceFeatures().toString());
        assertEquals("Wrong Dataset size after final Undo of " + msg, 1,
                alignment.getDataset().getHeight());
      }
    }
  }

  /**
   * Verify by inspection that the sequence features left on the sequence after
   * a cut match the expected results. The trick to this is that we can parse
   * each feature's original start-end positions from its description.
   * 
   * @param seq0
   * @param from
   * @param to
   * @param msg
   * @param seqStart
   */
  protected void verifyCut(SequenceI seq0, int from, int to,
          final String msg, int seqStart)
  {
    List<SequenceFeature> sfs;
    sfs = seq0.getSequenceFeatures();

    Collections.sort(sfs, BY_DESCRIPTION);

    /*
     * confirm the number of features has reduced by the
     * number of features within the cut region i.e. by
     * func(length of cut); exception is a cut at start or end of sequence, 
     * which retains the original coordinates, dataset sequence 
     * and all its features
     */
    boolean datasetRetained = from == 0 || to == 4;
    if (datasetRetained)
    {
      // dataset and all features retained
      assertEquals(msg, func(5), sfs.size());
    }
    else if (to - from == 4)
    {
      // all columns were cut
      assertTrue(sfs.isEmpty());
    }
    else
    {
      // failure in checkFeatureRelocation is more informative!
      assertEquals(msg + "wrong number of features left",
              func(5) - func(to - from + 1), sfs.size());
    }

    /*
     * inspect individual features
     */
    for (SequenceFeature sf : sfs)
    {
      verifyFeatureRelocation(sf, from + 1, to + 1, !datasetRetained,
              seqStart);
    }
  }

  /**
   * Check that after Undo, every feature has start/end that match its original
   * "start" and "end" properties
   * 
   * @param from
   * @param to
   * @param sfs
   */
  protected void verifyUndo(int from, int to, List<SequenceFeature> sfs)
  {
    for (SequenceFeature sf : sfs)
    {
      final int oldFrom = ((Integer) sf.getValue("from")).intValue();
      final int oldTo = ((Integer) sf.getValue("to")).intValue();
      String msg = String.format("Undo cut of [%d-%d], feature at [%d-%d] ",
              from + 1, to + 1, oldFrom, oldTo);
      assertEquals(msg + "start", oldFrom, sf.getBegin());
      assertEquals(msg + "end", oldTo, sf.getEnd());
    }
  }

  /**
   * Helper method to check a feature has been correctly relocated after a cut
   * 
   * @param sf
   * @param from
   *          start of cut (first residue cut 1..)
   * @param to
   *          end of cut (last residue cut 1..)
   * @param newDataset
   * @param seqStart
   */
  private void verifyFeatureRelocation(SequenceFeature sf, int from, int to,
          boolean newDataset, int seqStart)
  {
    // TODO handle the gapped sequence case as well
    int cutSize = to - from + 1;
    final int oldFrom = ((Integer) sf.getValue("from")).intValue();
    final int oldTo = ((Integer) sf.getValue("to")).intValue();
    final int oldFromPosition = oldFrom - seqStart + 1; // 1..
    final int oldToPosition = oldTo - seqStart + 1; // 1..

    String msg = String.format(
            "Feature %s relocated to %d-%d after cut of %d-%d",
            sf.getDescription(), sf.getBegin(), sf.getEnd(), from, to);
    if (!newDataset)
    {
      // dataset retained with all features unchanged
      assertEquals("0: " + msg, oldFrom, sf.getBegin());
      assertEquals("0: " + msg, oldTo, sf.getEnd());
    }
    else if (oldToPosition < from)
    {
      // before cut region so unchanged
      assertEquals("1: " + msg, oldFrom, sf.getBegin());
      assertEquals("2: " + msg, oldTo, sf.getEnd());
    }
    else if (oldFromPosition > to)
    {
      // follows cut region - shift by size of cut
      assertEquals("3: " + msg, newDataset ? oldFrom - cutSize : oldFrom,
              sf.getBegin());
      assertEquals("4: " + msg, newDataset ? oldTo - cutSize : oldTo,
              sf.getEnd());
    }
    else if (oldFromPosition < from && oldToPosition > to)
    {
      // feature encloses cut region - shrink it right
      assertEquals("5: " + msg, oldFrom, sf.getBegin());
      assertEquals("6: " + msg, oldTo - cutSize, sf.getEnd());
    }
    else if (oldFromPosition < from)
    {
      // feature overlaps left side of cut region - truncated right
      assertEquals("7: " + msg, from - 1 + seqStart - 1, sf.getEnd());
    }
    else if (oldToPosition > to)
    {
      // feature overlaps right side of cut region - truncated left
      assertEquals("8: " + msg, newDataset ? from + seqStart - 1 : to + 1,
              sf.getBegin());
      assertEquals("9: " + msg, newDataset ? from + oldTo - to - 1 : oldTo,
              sf.getEnd());
    }
    else
    {
      // feature internal to cut - should have been deleted!
      Assert.fail(msg + " - should have been deleted");
    }
  }

  /**
   * Test a cut action's relocation of sequence features
   */
  @Test(groups = { "Functional" })
  public void testCut_withFeatures5prime()
  {
    SequenceI seq0 = new Sequence("seq/8-11", "A-BCC");
    seq0.createDatasetSequence();
    assertEquals(8, seq0.getStart());
    seq0.addSequenceFeature(new SequenceFeature("", "", 10, 11, 0f, null));
    SequenceI[] seqsArray = new SequenceI[] { seq0 };
    AlignmentI alignment = new Alignment(seqsArray);

    /*
     * cut columns of A-B; same dataset sequence is retained, aligned sequence
     * start becomes 10
     */
    Edit ec = testee.new Edit(Action.CUT, seqsArray, 0, 3, alignment);
    EditCommand.cut(ec, new AlignmentI[] { alignment });

    /*
     * feature on CC(10-11) should still be on CC(10-11)
     */
    assertSame(seq0, alignment.getSequenceAt(0));
    assertEquals(10, seq0.getStart());
    List<SequenceFeature> sfs = seq0.getSequenceFeatures();
    assertEquals(1, sfs.size());
    SequenceFeature sf = sfs.get(0);
    assertEquals(10, sf.getBegin());
    assertEquals(11, sf.getEnd());
  }
}
