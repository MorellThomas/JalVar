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
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JLabel;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.api.AlignViewportI;
import jalview.bin.Cache;
import jalview.bin.Jalview;
import jalview.commands.EditCommand;
import jalview.commands.EditCommand.Action;
import jalview.commands.EditCommand.Edit;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SearchResults;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.SeqPanel.MousePos;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;
import jalview.util.MessageManager;
import jalview.viewmodel.ViewportRanges;
import junit.extensions.PA;

public class SeqPanelTest
{
  AlignFrame af;

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = "Functional")
  public void testSetStatusReturnsNearestResiduePosition()
  {
    SequenceI seq1 = new Sequence("Seq1", "AACDE");
    SequenceI seq2 = new Sequence("Seq2", "AA--E");
    AlignmentI al = new Alignment(new SequenceI[] { seq1, seq2 });
    AlignFrame alignFrame = new AlignFrame(al, al.getWidth(),
            al.getHeight());
    AlignmentI visAl = alignFrame.getViewport().getAlignment();

    // Test either side of gap
    assertEquals(alignFrame.alignPanel.getSeqPanel()
            .setStatusMessage(visAl.getSequenceAt(1), 1, 1), 2);
    assertEquals(((JLabel) PA.getValue(alignFrame, "statusBar")).getText(),
            "Sequence 2 ID: Seq2 Residue: ALA (2)");
    assertEquals(alignFrame.alignPanel.getSeqPanel()
            .setStatusMessage(visAl.getSequenceAt(1), 4, 1), 3);
    assertEquals(((JLabel) PA.getValue(alignFrame, "statusBar")).getText(),
            "Sequence 2 ID: Seq2 Residue: GLU (3)");
    // no status message at a gap, returns next residue position to the right
    assertEquals(alignFrame.alignPanel.getSeqPanel()
            .setStatusMessage(visAl.getSequenceAt(1), 2, 1), 3);
    assertEquals(((JLabel) PA.getValue(alignFrame, "statusBar")).getText(),
            "Sequence 2 ID: Seq2");
    assertEquals(alignFrame.alignPanel.getSeqPanel()
            .setStatusMessage(visAl.getSequenceAt(1), 3, 1), 3);
    assertEquals(((JLabel) PA.getValue(alignFrame, "statusBar")).getText(),
            "Sequence 2 ID: Seq2");
  }

  @Test(groups = "Functional")
  public void testAmbiguousAminoAcidGetsStatusMessage()
  {
    SequenceI seq1 = new Sequence("Seq1", "ABCDE");
    SequenceI seq2 = new Sequence("Seq2", "AB--E");
    AlignmentI al = new Alignment(new SequenceI[] { seq1, seq2 });
    AlignFrame alignFrame = new AlignFrame(al, al.getWidth(),
            al.getHeight());
    AlignmentI visAl = alignFrame.getViewport().getAlignment();

    assertEquals(alignFrame.alignPanel.getSeqPanel()
            .setStatusMessage(visAl.getSequenceAt(1), 1, 1), 2);
    assertEquals(((JLabel) PA.getValue(alignFrame, "statusBar")).getText(),
            "Sequence 2 ID: Seq2 Residue: B (2)");
  }

  @Test(groups = "Functional")
  public void testGetEditStatusMessage()
  {
    assertNull(SeqPanel.getEditStatusMessage(null));

    EditCommand edit = new EditCommand(); // empty
    assertNull(SeqPanel.getEditStatusMessage(edit));

    SequenceI[] seqs = new SequenceI[] { new Sequence("a", "b") };

    // 1 gap
    edit.addEdit(edit.new Edit(Action.INSERT_GAP, seqs, 1, 1, '-'));
    String expected = MessageManager.formatMessage("label.insert_gap", "1");
    assertEquals(SeqPanel.getEditStatusMessage(edit), expected);

    // 3 more gaps makes +4
    edit.addEdit(edit.new Edit(Action.INSERT_GAP, seqs, 1, 3, '-'));
    expected = MessageManager.formatMessage("label.insert_gaps", "4");
    assertEquals(SeqPanel.getEditStatusMessage(edit), expected);

    // 2 deletes makes + 2
    edit.addEdit(edit.new Edit(Action.DELETE_GAP, seqs, 1, 2, '-'));
    expected = MessageManager.formatMessage("label.insert_gaps", "2");
    assertEquals(SeqPanel.getEditStatusMessage(edit), expected);

    // 2 more deletes makes 0 - no text
    edit.addEdit(edit.new Edit(Action.DELETE_GAP, seqs, 1, 2, '-'));
    assertNull(SeqPanel.getEditStatusMessage(edit));

    // 1 more delete makes 1 delete
    edit.addEdit(edit.new Edit(Action.DELETE_GAP, seqs, 1, 1, '-'));
    expected = MessageManager.formatMessage("label.delete_gap", "1");
    assertEquals(SeqPanel.getEditStatusMessage(edit), expected);

    // 1 more delete makes 2 deletes
    edit.addEdit(edit.new Edit(Action.DELETE_GAP, seqs, 1, 1, '-'));
    expected = MessageManager.formatMessage("label.delete_gaps", "2");
    assertEquals(SeqPanel.getEditStatusMessage(edit), expected);
  }

  /**
   * Tests that simulate 'locked editing', where an inserted gap is balanced by
   * a gap deletion in the selection group, and vice versa
   */
  @Test(groups = "Functional")
  public void testGetEditStatusMessage_lockedEditing()
  {
    EditCommand edit = new EditCommand(); // empty
    SequenceI[] seqs = new SequenceI[] { new Sequence("a", "b") };

    // 1 gap inserted, balanced by 1 delete
    Edit e1 = edit.new Edit(Action.INSERT_GAP, seqs, 1, 1, '-');
    edit.addEdit(e1);
    Edit e2 = edit.new Edit(Action.DELETE_GAP, seqs, 5, 1, '-');
    e2.setSystemGenerated(true);
    edit.addEdit(e2);
    String expected = MessageManager.formatMessage("label.insert_gap", "1");
    assertEquals(SeqPanel.getEditStatusMessage(edit), expected);

    // 2 more gaps makes +3
    Edit e3 = edit.new Edit(Action.INSERT_GAP, seqs, 1, 2, '-');
    edit.addEdit(e3);
    Edit e4 = edit.new Edit(Action.DELETE_GAP, seqs, 5, 2, '-');
    e4.setSystemGenerated(true);
    edit.addEdit(e4);
    expected = MessageManager.formatMessage("label.insert_gaps", "3");
    assertEquals(SeqPanel.getEditStatusMessage(edit), expected);

    // 2 deletes makes + 1
    Edit e5 = edit.new Edit(Action.DELETE_GAP, seqs, 1, 2, '-');
    edit.addEdit(e5);
    Edit e6 = edit.new Edit(Action.INSERT_GAP, seqs, 5, 2, '-');
    e6.setSystemGenerated(true);
    edit.addEdit(e6);
    expected = MessageManager.formatMessage("label.insert_gap", "1");
    assertEquals(SeqPanel.getEditStatusMessage(edit), expected);

    // 1 more delete makes 0 - no text
    Edit e7 = edit.new Edit(Action.DELETE_GAP, seqs, 1, 1, '-');
    edit.addEdit(e7);
    Edit e8 = edit.new Edit(Action.INSERT_GAP, seqs, 5, 1, '-');
    e8.setSystemGenerated(true);
    edit.addEdit(e8);
    expected = MessageManager.formatMessage("label.insert_gaps", "2");
    assertNull(SeqPanel.getEditStatusMessage(edit));

    // 1 more delete makes 1 delete
    Edit e9 = edit.new Edit(Action.DELETE_GAP, seqs, 1, 1, '-');
    edit.addEdit(e9);
    Edit e10 = edit.new Edit(Action.INSERT_GAP, seqs, 5, 1, '-');
    e10.setSystemGenerated(true);
    edit.addEdit(e10);
    expected = MessageManager.formatMessage("label.delete_gap", "1");
    assertEquals(SeqPanel.getEditStatusMessage(edit), expected);

    // 2 more deletes makes 3 deletes
    Edit e11 = edit.new Edit(Action.DELETE_GAP, seqs, 1, 2, '-');
    edit.addEdit(e11);
    Edit e12 = edit.new Edit(Action.INSERT_GAP, seqs, 5, 2, '-');
    e12.setSystemGenerated(true);
    edit.addEdit(e12);
    expected = MessageManager.formatMessage("label.delete_gaps", "3");
    assertEquals(SeqPanel.getEditStatusMessage(edit), expected);
  }

  public void testFindMousePosition_unwrapped()
  {
    String seqData = ">Seq1\nAACDE\n>Seq2\nAA--E\n";
    AlignFrame alignFrame = new FileLoader().LoadFileWaitTillLoaded(seqData,
            DataSourceType.PASTE);
    AlignViewportI av = alignFrame.getViewport();
    av.setShowAnnotation(true);
    av.setWrapAlignment(false);
    final int charHeight = av.getCharHeight();
    final int charWidth = av.getCharWidth();
    // sanity checks:
    assertTrue(charHeight > 0);
    assertTrue(charWidth > 0);
    assertTrue(alignFrame.alignPanel.getSeqPanel().getWidth() > 0);

    SeqPanel testee = alignFrame.alignPanel.getSeqPanel();
    int x = 0;
    int y = 0;

    /*
     * mouse at top left of unwrapped panel
     */
    MouseEvent evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0,
            x, y, 0, 0, 0, false, 0);
    MousePos pos = testee.findMousePosition(evt);
    assertEquals(pos.column, 0);
    assertEquals(pos.seqIndex, 0);
    assertEquals(pos.annotationIndex, -1);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown()
  {
    if (Desktop.instance != null)
      Desktop.instance.closeAll_actionPerformed(null);
  }

  @Test(groups = "Functional")
  public void testFindMousePosition_wrapped_annotations()
  {
    Cache.applicationProperties.setProperty("SHOW_ANNOTATIONS", "true");
    Cache.applicationProperties.setProperty("WRAP_ALIGNMENT", "true");
    AlignFrame alignFrame = new FileLoader().LoadFileWaitTillLoaded(
            "examples/uniref50.fa", DataSourceType.FILE);
    AlignViewportI av = alignFrame.getViewport();
    av.setScaleAboveWrapped(false);
    av.setScaleLeftWrapped(false);
    av.setScaleRightWrapped(false);

    alignFrame.alignPanel.updateLayout();

    final int charHeight = av.getCharHeight();
    final int charWidth = av.getCharWidth();
    final int alignmentHeight = av.getAlignment().getHeight();

    // sanity checks:
    assertTrue(charHeight > 0);
    assertTrue(charWidth > 0);
    assertTrue(alignFrame.alignPanel.getSeqPanel().getWidth() > 0);

    SeqPanel testee = alignFrame.alignPanel.getSeqPanel();
    int x = 0;
    int y = 0;

    /*
     * mouse at top left of wrapped panel; there is a gap of charHeight
     * above the alignment
     */
    MouseEvent evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0,
            x, y, 0, 0, 0, false, 0);
    MousePos pos = testee.findMousePosition(evt);
    assertEquals(pos.column, 0);
    assertEquals(pos.seqIndex, -1); // above sequences
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor at bottom of gap above
     */
    y = charHeight - 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, -1);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor over top of first sequence
     */
    y = charHeight;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, 0);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor at bottom of first sequence
     */
    y = 2 * charHeight - 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, 0);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor at top of second sequence
     */
    y = 2 * charHeight;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, 1);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor at bottom of second sequence
     */
    y = 3 * charHeight - 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, 1);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor at bottom of last sequence
     */
    y = charHeight * (1 + alignmentHeight) - 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, alignmentHeight - 1);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor below sequences, in 3-pixel gap above annotations
     * method reports index of nearest sequence above
     */
    y += 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, alignmentHeight - 1);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor still in the gap above annotations, now at the bottom of it
     */
    y += SeqCanvas.SEQS_ANNOTATION_GAP - 1; // 3-1 = 2
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, alignmentHeight - 1);
    assertEquals(pos.annotationIndex, -1);

    AlignmentAnnotation[] annotationRows = av.getAlignment()
            .getAlignmentAnnotation();
    for (int n = 0; n < annotationRows.length; n++)
    {
      /*
       * cursor at the top of the n'th annotation  
       */
      y += 1;
      evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0,
              0, 0, false, 0);
      pos = testee.findMousePosition(evt);
      assertEquals(pos.seqIndex, alignmentHeight - 1);
      assertEquals(pos.annotationIndex, n); // over n'th annotation

      /*
       * cursor at the bottom of the n'th annotation  
       */
      y += annotationRows[n].height - 1;
      evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0,
              0, 0, false, 0);
      pos = testee.findMousePosition(evt);
      assertEquals(pos.seqIndex, alignmentHeight - 1);
      assertEquals(pos.annotationIndex, n);
    }

    /*
     * cursor in gap between wrapped widths  
     */
    y += 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, -1);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor at bottom of gap between wrapped widths  
     */
    y += charHeight - 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, -1);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor at top of first sequence, second wrapped width  
     */
    y += 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, 0);
    assertEquals(pos.annotationIndex, -1);
  }

  @Test(groups = "Functional")
  public void testFindMousePosition_wrapped_scaleAbove()
  {
    Cache.applicationProperties.setProperty("SHOW_ANNOTATIONS", "true");
    Cache.applicationProperties.setProperty("WRAP_ALIGNMENT", "true");
    AlignFrame alignFrame = new FileLoader().LoadFileWaitTillLoaded(
            "examples/uniref50.fa", DataSourceType.FILE);
    AlignViewportI av = alignFrame.getViewport();
    av.setScaleAboveWrapped(true);
    av.setScaleLeftWrapped(false);
    av.setScaleRightWrapped(false);
    alignFrame.alignPanel.updateLayout();

    final int charHeight = av.getCharHeight();
    final int charWidth = av.getCharWidth();
    final int alignmentHeight = av.getAlignment().getHeight();

    // sanity checks:
    assertTrue(charHeight > 0);
    assertTrue(charWidth > 0);
    assertTrue(alignFrame.alignPanel.getSeqPanel().getWidth() > 0);

    SeqPanel testee = alignFrame.alignPanel.getSeqPanel();
    int x = 0;
    int y = 0;

    /*
     * mouse at top left of wrapped panel; there is a gap of charHeight
     * above the alignment
     */
    MouseEvent evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0,
            x, y, 0, 0, 0, false, 0);
    MousePos pos = testee.findMousePosition(evt);
    assertEquals(pos.column, 0);
    assertEquals(pos.seqIndex, -1); // above sequences
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor at bottom of gap above
     * two charHeights including scale panel
     */
    y = 2 * charHeight - 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, -1);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor over top of first sequence
     */
    y += 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, 0);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor at bottom of first sequence
     */
    y += charHeight - 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, 0);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor at top of second sequence
     */
    y += 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, 1);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor at bottom of second sequence
     */
    y += charHeight - 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, 1);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor at bottom of last sequence
     * (scale + gap + sequences)
     */
    y = charHeight * (2 + alignmentHeight) - 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, alignmentHeight - 1);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor below sequences, in 3-pixel gap above annotations
     */
    y += 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, alignmentHeight - 1);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor still in the gap above annotations, now at the bottom of it
     * method reports index of nearest sequence above  
     */
    y += SeqCanvas.SEQS_ANNOTATION_GAP - 1; // 3-1 = 2
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, alignmentHeight - 1);
    assertEquals(pos.annotationIndex, -1);

    AlignmentAnnotation[] annotationRows = av.getAlignment()
            .getAlignmentAnnotation();
    for (int n = 0; n < annotationRows.length; n++)
    {
      /*
       * cursor at the top of the n'th annotation  
       */
      y += 1;
      evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0,
              0, 0, false, 0);
      pos = testee.findMousePosition(evt);
      assertEquals(pos.seqIndex, alignmentHeight - 1);
      assertEquals(pos.annotationIndex, n); // over n'th annotation

      /*
       * cursor at the bottom of the n'th annotation  
       */
      y += annotationRows[n].height - 1;
      evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0,
              0, 0, false, 0);
      pos = testee.findMousePosition(evt);
      SeqCanvas sc = testee.seqCanvas;
      assertEquals(pos.seqIndex, alignmentHeight - 1,
              String.format("%s n=%d y=%d %d, %d, %d, %d",
                      annotationRows[n].label, n, y, sc.getWidth(),
                      sc.getHeight(), sc.wrappedRepeatHeightPx,
                      sc.wrappedSpaceAboveAlignment));
      assertEquals(pos.annotationIndex, n);
    }

    /*
     * cursor in gap between wrapped widths  
     */
    y += 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, -1);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor at bottom of gap between wrapped widths  
     */
    y += charHeight - 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, -1);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor at top of scale, second wrapped width  
     */
    y += 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, -1);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor at bottom of scale, second wrapped width  
     */
    y += charHeight - 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, -1);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor at top of first sequence, second wrapped width  
     */
    y += 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, 0);
    assertEquals(pos.annotationIndex, -1);
  }

  @Test(groups = "Functional")
  public void testFindMousePosition_wrapped_noAnnotations()
  {
    Cache.applicationProperties.setProperty("SHOW_ANNOTATIONS", "false");
    Cache.applicationProperties.setProperty("WRAP_ALIGNMENT", "true");
    Cache.applicationProperties.setProperty("FONT_SIZE", "10");
    AlignFrame alignFrame = new FileLoader().LoadFileWaitTillLoaded(
            "examples/uniref50.fa", DataSourceType.FILE);
    AlignViewportI av = alignFrame.getViewport();
    av.setScaleAboveWrapped(false);
    av.setScaleLeftWrapped(false);
    av.setScaleRightWrapped(false);
    alignFrame.alignPanel.updateLayout();

    final int charHeight = av.getCharHeight();
    final int charWidth = av.getCharWidth();
    final int alignmentHeight = av.getAlignment().getHeight();

    // sanity checks:
    assertTrue(charHeight > 0);
    assertTrue(charWidth > 0);
    assertTrue(alignFrame.alignPanel.getSeqPanel().getWidth() > 0);

    SeqPanel testee = alignFrame.alignPanel.getSeqPanel();
    int x = 0;
    int y = 0;

    /*
     * mouse at top left of wrapped panel; there is a gap of charHeight
     * above the alignment
     */
    MouseEvent evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0,
            x, y, 0, 0, 0, false, 0);
    MousePos pos = testee.findMousePosition(evt);
    assertEquals(pos.column, 0);
    assertEquals(pos.seqIndex, -1); // above sequences
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor over top of first sequence
     */
    y = charHeight;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, 0);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor at bottom of last sequence
     */
    y = charHeight * (1 + alignmentHeight) - 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, alignmentHeight - 1);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor below sequences, at top of charHeight gap between widths
     */
    y += 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, -1);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor below sequences, at top of charHeight gap between widths
     */
    y += charHeight - 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, -1);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor at the top of the first sequence, second width  
     */
    y += 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, 0);
    assertEquals(pos.annotationIndex, -1);
  }

  @Test(groups = "Functional")
  public void testFindColumn_unwrapped()
  {
    Cache.applicationProperties.setProperty("WRAP_ALIGNMENT", "false");
    AlignFrame alignFrame = new FileLoader().LoadFileWaitTillLoaded(
            "examples/uniref50.fa", DataSourceType.FILE);
    SeqPanel testee = alignFrame.alignPanel.getSeqPanel();
    int x = 0;
    final int charWidth = alignFrame.getViewport().getCharWidth();
    assertTrue(charWidth > 0); // sanity check
    ViewportRanges ranges = alignFrame.getViewport().getRanges();
    assertEquals(ranges.getStartRes(), 0);

    /*
     * mouse at top left of unwrapped panel
     */
    MouseEvent evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0,
            x, 0, 0, 0, 0, false, 0);
    assertEquals(testee.findColumn(evt), 0);

    /*
     * not quite one charWidth across
     */
    x = charWidth - 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, 0, 0, 0,
            0, false, 0);
    assertEquals(testee.findColumn(evt), 0);

    /*
     * one charWidth across
     */
    x = charWidth;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, 0, 0, 0,
            0, false, 0);
    assertEquals(testee.findColumn(evt), 1);

    /*
     * two charWidths across
     */
    x = 2 * charWidth;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, 0, 0, 0,
            0, false, 0);
    assertEquals(testee.findColumn(evt), 2);

    /*
     * limited to last column of seqcanvas
     */
    x = 20000;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, 0, 0, 0,
            0, false, 0);
    SeqCanvas seqCanvas = alignFrame.alignPanel.getSeqPanel().seqCanvas;
    int w = seqCanvas.getWidth();
    // limited to number of whole columns, base 0,
    // and to end of visible range
    int expected = w / charWidth;
    expected = Math.min(expected, ranges.getEndRes());
    assertEquals(testee.findColumn(evt), expected);

    /*
     * hide columns 5-10 (base 1)
     */
    alignFrame.getViewport().hideColumns(4, 9);
    x = 5 * charWidth + 2;
    // x is in 6th visible column, absolute column 12, or 11 base 0
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, 0, 0, 0,
            0, false, 0);
    assertEquals(testee.findColumn(evt), 11);
  }

  @Test(groups = "Functional")
  public void testFindColumn_wrapped()
  {
    Cache.applicationProperties.setProperty("WRAP_ALIGNMENT", "true");
    AlignFrame alignFrame = new FileLoader().LoadFileWaitTillLoaded(
            "examples/uniref50.fa", DataSourceType.FILE);
    AlignViewport av = alignFrame.getViewport();
    av.setScaleAboveWrapped(false);
    av.setScaleLeftWrapped(false);
    av.setScaleRightWrapped(false);
    alignFrame.alignPanel.updateLayout();
    SeqPanel testee = alignFrame.alignPanel.getSeqPanel();
    int x = 0;
    final int charWidth = av.getCharWidth();
    assertTrue(charWidth > 0); // sanity check
    assertEquals(av.getRanges().getStartRes(), 0);

    /*
     * mouse at top left of wrapped panel, no West (left) scale
     */
    MouseEvent evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0,
            x, 0, 0, 0, 0, false, 0);
    assertEquals(testee.findColumn(evt), 0);

    /*
     * not quite one charWidth across
     */
    x = charWidth - 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, 0, 0, 0,
            0, false, 0);
    assertEquals(testee.findColumn(evt), 0);

    /*
     * one charWidth across
     */
    x = charWidth;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, 0, 0, 0,
            0, false, 0);
    assertEquals(testee.findColumn(evt), 1);

    /*
     * x over scale left (before drawn columns) results in -1
     */
    av.setScaleLeftWrapped(true);
    alignFrame.alignPanel.updateLayout();
    SeqCanvas seqCanvas = testee.seqCanvas;
    int labelWidth = (int) PA.getValue(seqCanvas, "labelWidthWest");
    assertTrue(labelWidth > 0);
    x = labelWidth - 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, 0, 0, 0,
            0, false, 0);
    assertEquals(testee.findColumn(evt), -1);

    x = labelWidth;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, 0, 0, 0,
            0, false, 0);
    assertEquals(testee.findColumn(evt), 0);

    /*
     * x over right edge of last residue (including scale left)
     */
    int residuesWide = av.getRanges().getViewportWidth();
    assertTrue(residuesWide > 0);
    x = labelWidth + charWidth * residuesWide - 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, 0, 0, 0,
            0, false, 0);
    assertEquals(testee.findColumn(evt), residuesWide - 1);

    /*
     * x over scale right (beyond drawn columns) results in -1
     */
    av.setScaleRightWrapped(true);
    alignFrame.alignPanel.updateLayout();
    labelWidth = (int) PA.getValue(seqCanvas, "labelWidthEast");
    assertTrue(labelWidth > 0);
    int residuesWide2 = av.getRanges().getViewportWidth();
    assertTrue(residuesWide2 > 0);
    assertTrue(residuesWide2 < residuesWide); // available width reduced
    x += 1; // just over left edge of scale right
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, 0, 0, 0,
            0, false, 0);
    assertEquals(testee.findColumn(evt), -1);

    // todo add startRes offset, hidden columns

  }

  @BeforeClass(alwaysRun = true)
  public static void setUpBeforeClass() throws Exception
  {
    /*
     * use read-only test properties file
     */
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    Jalview.main(new String[] { "-nonews" });
  }

  /**
   * waits for Swing event dispatch queue to empty
   */
  synchronized void waitForSwing()
  {
    try
    {
      EventQueue.invokeAndWait(new Runnable()
      {
        @Override
        public void run()
        {
        }
      });
    } catch (InterruptedException | InvocationTargetException e)
    {
      e.printStackTrace();
    }
  }

  @Test(groups = "Functional")
  public void testFindMousePosition_wrapped_scales_longSequence()
  {
    Cache.applicationProperties.setProperty("SHOW_ANNOTATIONS", "false");
    Cache.applicationProperties.setProperty("WRAP_ALIGNMENT", "true");
    Cache.applicationProperties.setProperty("FONT_SIZE", "14");
    Cache.applicationProperties.setProperty("FONT_NAME", "SansSerif");
    Cache.applicationProperties.setProperty("FONT_STYLE", "0");
    // sequence of 50 bases, doubled 10 times, = 51200 bases
    String dna = "ATGGCCATTGGGCCCAAATTTCCCAAAGGGTTTCCCTGAGGTCAGTCAGA";
    for (int i = 0; i < 10; i++)
    {
      dna += dna;
    }
    assertEquals(dna.length(), 51200);
    AlignFrame alignFrame = new FileLoader()
            .LoadFileWaitTillLoaded("dna " + dna, DataSourceType.PASTE);
    SeqPanel testee = alignFrame.alignPanel.getSeqPanel();
    AlignViewport av = alignFrame.getViewport();
    av.setScaleAboveWrapped(true);
    av.setScaleLeftWrapped(true);
    av.setScaleRightWrapped(true);
    alignFrame.alignPanel.updateLayout();

    try
    {
      Thread.sleep(200);
    } catch (InterruptedException e)
    {
    }

    final int charHeight = av.getCharHeight();
    final int charWidth = av.getCharWidth();
    assertEquals(charHeight, 17);
    assertEquals(charWidth, 12);

    FontMetrics fm = testee.getFontMetrics(av.getFont());
    int labelWidth = fm.stringWidth("00000") + charWidth;
    assertEquals(labelWidth, 57); // 5 x 9 + charWidth
    assertEquals(testee.seqCanvas.getLabelWidthWest(), labelWidth);

    int x = 0;
    int y = 0;

    /*
     * mouse at top left of wrapped panel; there is a gap of 2 * charHeight
     * above the alignment
     */
    MouseEvent evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0,
            x, y, 0, 0, 0, false, 0);
    MousePos pos = testee.findMousePosition(evt);
    assertEquals(pos.column, -1); // over scale left, not an alignment column
    assertEquals(pos.seqIndex, -1); // above sequences
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor over scale above first sequence
     */
    y += charHeight;
    x = labelWidth;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, -1);
    assertEquals(pos.column, 0);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor over scale left of first sequence
     */
    y += charHeight;
    x = 0;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, 0);
    assertEquals(pos.column, -1);
    assertEquals(pos.annotationIndex, -1);

    /*
     * cursor over start of first sequence
     */
    x = labelWidth;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, 0);
    assertEquals(pos.column, 0);
    assertEquals(pos.annotationIndex, -1);

    /*
     * move one character right, to bottom pixel of same row
     */
    x += charWidth;
    y += charHeight - 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, 0);
    assertEquals(pos.column, 1);

    /*
     * move down one pixel - now in the no man's land between rows
     */
    y += 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, -1);
    assertEquals(pos.column, 1);

    /*
     * move down two char heights less one pixel - still in the no man's land
     * (scale above + spacer line)
     */
    y += (2 * charHeight - 1);
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, -1);
    assertEquals(pos.column, 1);

    /*
     * move down one more pixel - now on the next row of the sequence
     */
    y += 1;
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, 0);
    assertEquals(pos.column, 1 + av.getWrappedWidth());

    /*
     * scroll to near the end of the sequence
     */
    SearchResultsI sr = new SearchResults();
    int scrollTo = dna.length() - 1000;
    sr.addResult(av.getAlignment().getSequenceAt(0), scrollTo, scrollTo);
    alignFrame.alignPanel.scrollToPosition(sr);

    /*
     * place the mouse on the first column of the 6th sequence, and
     * verify that (computed) findMousePosition matches (actual) ViewportRanges
     */
    x = labelWidth;
    y = 17 * charHeight; // 17 = 6 times two header rows and 5 sequence rows
    evt = new MouseEvent(testee, MouseEvent.MOUSE_MOVED, 0L, 0, x, y, 0, 0,
            0, false, 0);
    pos = testee.findMousePosition(evt);
    assertEquals(pos.seqIndex, 0);
    int expected = av.getRanges().getStartRes() + 5 * av.getWrappedWidth();
    assertEquals(pos.column, expected);
  }
}
