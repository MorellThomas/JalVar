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
package jalview.viewmodel;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import jalview.analysis.AlignmentGenerator;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.HiddenSequences;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ViewportRangesTest
{

  AlignmentGenerator gen = new AlignmentGenerator(false);

  AlignmentI al = gen.generate(20, 30, 1, 5, 5);

  AlignmentI smallAl = gen.generate(7, 2, 2, 5, 5);

  @BeforeClass(alwaysRun = true)
  public void setUp()
  {
    gen = new AlignmentGenerator(false);
    al = gen.generate(20, 30, 1, 5, 5);
    smallAl = gen.generate(7, 2, 2, 5, 5);
  }

  @BeforeMethod(alwaysRun = true)
  public void cleanUp()
  {
    ColumnSelection sel = new ColumnSelection();
    al.getHiddenColumns().revealAllHiddenColumns(sel);
    al.getHiddenSequences().showAll(null);
    smallAl.getHiddenColumns().revealAllHiddenColumns(sel);
    smallAl.getHiddenSequences().showAll(null);
  }

  @Test(groups = { "Functional" })
  public void testViewportRanges()
  {
    ViewportRanges vr = new ViewportRanges(al);

    assertEquals(vr.getStartRes(), 0);
    assertEquals(vr.getEndRes(), al.getWidth() - 1);
    assertEquals(vr.getStartSeq(), 0);
    assertEquals(vr.getEndSeq(), al.getHeight() - 1);
  }

  @Test(groups = { "Functional" })
  public void testGetAbsoluteAlignmentHeight()
  {
    ViewportRanges vr = new ViewportRanges(al);

    assertEquals(vr.getAbsoluteAlignmentHeight(), al.getHeight());

    al.getHiddenSequences().hideSequence(al.getSequenceAt(3));
    assertEquals(vr.getAbsoluteAlignmentHeight(), al.getHeight() + 1);
  }

  @Test(groups = { "Functional" })
  public void testGetAbsoluteAlignmentWidth()
  {
    ViewportRanges vr = new ViewportRanges(al);
    assertEquals(vr.getAbsoluteAlignmentWidth(), al.getWidth());
  }

  @Test(groups = { "Functional" })
  public void testSetEndSeq()
  {
    ViewportRanges vr = new ViewportRanges(al);
    vr.setEndSeq(-1);
    assertEquals(vr.getEndSeq(), 0);

    vr.setEndSeq(al.getHeight());
    assertEquals(vr.getEndSeq(), al.getHeight() - 1);

    vr.setEndSeq(al.getHeight() - 1);
    assertEquals(vr.getEndSeq(), al.getHeight() - 1);
  }

  @Test(groups = { "Functional" })
  public void testSetStartRes()
  {
    ViewportRanges vr = new ViewportRanges(al);
    vr.setStartRes(-1);
    assertEquals(vr.getStartRes(), 0);

    vr.setStartRes(al.getWidth());
    assertEquals(vr.getStartRes(), al.getWidth() - 1);

    vr.setStartRes(al.getWidth() - 1);
    assertEquals(vr.getStartRes(), al.getWidth() - 1);
  }

  @Test(groups = { "Functional" })
  public void testSetStartSeq()
  {
    ViewportRanges vr = new ViewportRanges(al);
    vr.setStartSeq(-1);
    assertEquals(vr.getStartSeq(), 0);

    vr.setStartSeq(al.getHeight() - vr.getViewportHeight() + 1);
    assertEquals(vr.getStartSeq(), al.getHeight() - vr.getViewportHeight());

    vr.setStartSeq(al.getHeight() - vr.getViewportHeight());
    assertEquals(vr.getStartSeq(), al.getHeight() - vr.getViewportHeight());
  }

  @Test(groups = { "Functional" })
  public void testSetStartEndRes()
  {
    ViewportRanges vr = new ViewportRanges(al);
    vr.setStartEndRes(-1, -1);
    assertEquals(vr.getStartRes(), 0);
    assertEquals(vr.getEndRes(), 0);

    vr.setStartEndRes(5, 19);
    assertEquals(vr.getStartRes(), 5);
    assertEquals(vr.getEndRes(), 19);

    vr.setStartEndRes(al.getWidth(), al.getWidth());
    assertEquals(vr.getEndRes(), al.getWidth() - 1);

    ViewportRanges vrsmall = new ViewportRanges(smallAl);
    vrsmall.setStartEndRes(al.getWidth(), al.getWidth());
    assertEquals(vrsmall.getEndRes(), 6);

    // make visible alignment width = 0
    smallAl.getHiddenColumns().hideColumns(0, 6);
    vrsmall.setStartEndRes(0, 4);
    assertEquals(vrsmall.getStartRes(), 0);
    assertEquals(vrsmall.getEndRes(), 0);
  }

  @Test(groups = { "Functional" })
  public void testSetStartEndSeq()
  {
    ViewportRanges vr = new ViewportRanges(al);
    vr.setStartEndSeq(-1, -1);
    assertEquals(vr.getStartSeq(), 0);
    assertEquals(vr.getEndSeq(), 0);

    vr.setStartEndSeq(5, 19);
    assertEquals(vr.getStartSeq(), 5);
    assertEquals(vr.getEndSeq(), 19);

    vr.setStartEndSeq(al.getHeight(), al.getHeight());
    assertEquals(vr.getEndSeq(), al.getHeight() - 1);

    // make visible alignment height = 0
    smallAl.getHiddenSequences().hideSequence(smallAl.getSequenceAt(0));
    smallAl.getHiddenSequences().hideSequence(smallAl.getSequenceAt(0));
    ViewportRanges vrsmall = new ViewportRanges(smallAl);
    vrsmall.setStartEndSeq(0, 3);
    assertEquals(vrsmall.getStartSeq(), 0);
    assertEquals(vrsmall.getEndSeq(), 0);
  }

  @Test(groups = { "Functional" })
  public void testSetStartResAndSeq()
  {
    ViewportRanges vr = new ViewportRanges(al);
    vr.setViewportHeight(10);
    vr.setStartResAndSeq(3, 6);
    assertEquals(vr.getStartRes(), 3);
    assertEquals(vr.getStartSeq(), 6);
    assertEquals(vr.getEndRes(), 3 + vr.getViewportWidth() - 1);
    assertEquals(vr.getEndSeq(), 6 + vr.getViewportHeight() - 1);

    vr.setStartResAndSeq(10, 25);
    assertEquals(vr.getStartRes(), 10);
    assertEquals(vr.getStartSeq(), 19);
    assertEquals(vr.getEndRes(), 10 + vr.getViewportWidth() - 1);
    assertEquals(vr.getEndSeq(), 19 + vr.getViewportHeight() - 1);
  }

  @Test(groups = { "Functional" })
  public void testSetViewportHeight()
  {
    ViewportRanges vr = new ViewportRanges(al);
    vr.setViewportHeight(13);
    assertEquals(vr.getViewportHeight(), 13);
  }

  @Test(groups = { "Functional" })
  public void testSetViewportWidth()
  {
    ViewportRanges vr = new ViewportRanges(al);
    vr.setViewportWidth(13);
    assertEquals(vr.getViewportWidth(), 13);
  }

  @Test(groups = { "Functional" })
  public void testSetViewportStartAndHeight()
  {
    ViewportRanges vr = new ViewportRanges(al);
    vr.setViewportStartAndHeight(2, 6);
    assertEquals(vr.getViewportHeight(), 6);
    assertEquals(vr.getStartSeq(), 2);

    // reset -ve values of start to 0
    vr.setViewportStartAndHeight(-1, 7);
    assertEquals(vr.getViewportHeight(), 7);
    assertEquals(vr.getStartSeq(), 0);

    // reset out of bounds start values to within bounds
    vr.setViewportStartAndHeight(35, 5);
    assertEquals(vr.getViewportHeight(), 5);
    assertEquals(vr.getStartSeq(), 24);
  }

  @Test(groups = { "Functional" })
  public void testSetViewportStartAndWidth()
  {
    ViewportRanges vr = new ViewportRanges(al);
    vr.setViewportStartAndWidth(2, 6);
    assertEquals(vr.getViewportWidth(), 6);
    assertEquals(vr.getStartRes(), 2);

    // reset -ve values of start to 0
    vr.setViewportStartAndWidth(-1, 7);
    assertEquals(vr.getViewportWidth(), 7);
    assertEquals(vr.getStartRes(), 0);

    // reset out of bounds start values to within bounds
    vr.setViewportStartAndWidth(35, 5);
    assertEquals(vr.getViewportWidth(), 5);
    assertEquals(vr.getStartRes(), 16);

    // small alignment doesn't get bounds reset
    ViewportRanges vrsmall = new ViewportRanges(smallAl);
    vrsmall.setViewportStartAndWidth(0, 63);
    assertEquals(vrsmall.getViewportWidth(), 7);
    assertEquals(vrsmall.getStartRes(), 0);
  }

  @Test(groups = { "Functional" })
  public void testPageUpDown()
  {
    ViewportRanges vr = new ViewportRanges(al);
    vr.setViewportStartAndHeight(8, 6);
    vr.pageDown();
    assertEquals(vr.getStartSeq(), 13);

    vr.pageUp();
    assertEquals(vr.getStartSeq(), 8);

    vr.pageUp();
    assertEquals(vr.getStartSeq(), 3);

    vr.pageUp();
    // pageup does not go beyond 0, viewport height stays the same
    assertEquals(vr.getStartSeq(), 0);
    assertEquals(vr.getViewportHeight(), 6);

    vr.pageDown();
    vr.pageDown();
    vr.pageDown();
    vr.pageDown();
    vr.pageDown();

    // pagedown to bottom does not go beyond end, and height stays same
    assertEquals(vr.getStartSeq(), 24);
    assertEquals(vr.getViewportHeight(), 6);
  }

  @Test(groups = { "Functional" })
  public void testScrollUp()
  {
    ViewportRanges vr = new ViewportRanges(al);
    vr.setViewportStartAndHeight(1, 5);
    vr.scrollUp(true);
    assertEquals(vr.getStartSeq(), 0);
    // can't scroll above top
    vr.scrollUp(true);
    assertEquals(vr.getStartSeq(), 0);

    vr.setViewportStartAndHeight(24, 5);
    vr.scrollUp(false);
    assertEquals(vr.getStartSeq(), 25);
    // can't scroll beyond bottom
    vr.scrollUp(false);
    assertEquals(vr.getStartSeq(), 25);
  }

  @Test(groups = { "Functional" })
  public void testScrollUpWithHidden()
  {
    ViewportRanges vr = new ViewportRanges(al);

    // hide last sequence
    HiddenSequences hidden = new HiddenSequences(al);
    hidden.hideSequence(al.getSequenceAt(29));

    vr.setViewportStartAndHeight(1, 5);
    vr.scrollUp(true);
    assertEquals(vr.getStartSeq(), 0);
    // can't scroll above top
    vr.scrollUp(true);
    assertEquals(vr.getStartSeq(), 0);

    vr.setViewportStartAndHeight(23, 5);
    vr.scrollUp(false);
    assertEquals(vr.getStartSeq(), 24);
    // can't scroll beyond bottom
    vr.scrollUp(false);
    assertEquals(vr.getStartSeq(), 24);
  }

  @Test(groups = { "Functional" })
  public void testScrollRight()
  {
    ViewportRanges vr = new ViewportRanges(al);
    vr.setViewportStartAndWidth(1, 5);
    vr.scrollRight(false);
    assertEquals(vr.getStartRes(), 0);
    // can't scroll left past start
    vr.scrollRight(false);
    assertEquals(vr.getStartRes(), 0);

    vr.setViewportStartAndWidth(15, 5);
    vr.scrollRight(true);
    assertEquals(vr.getStartRes(), 16);
    // can't scroll right past end
    vr.scrollRight(true);
    assertEquals(vr.getStartRes(), 16);
  }

  @Test(groups = { "Functional" })
  public void testScrollRightWithHidden()
  {
    ViewportRanges vr = new ViewportRanges(al);

    // hide last 2 columns
    HiddenColumns cols = new HiddenColumns();
    cols.hideColumns(19, 20);
    al.setHiddenColumns(cols);

    vr.setViewportStartAndWidth(1, 5);
    vr.scrollRight(false);
    assertEquals(vr.getStartRes(), 0);
    // can't scroll left past start
    vr.scrollRight(false);
    assertEquals(vr.getStartRes(), 0);

    vr.setViewportStartAndWidth(13, 5);
    vr.scrollRight(true);
    assertEquals(vr.getStartRes(), 14);
    // can't scroll right past last visible col
    vr.scrollRight(true);
    assertEquals(vr.getStartRes(), 14);
  }

  @Test(groups = { "Functional" })
  public void testScrollToWrappedVisible()
  {
    AlignmentI al2 = gen.generate(60, 30, 1, 5, 5);

    ViewportRanges vr = new ViewportRanges(al2);

    // start with viewport on 5-14
    vr.setViewportStartAndWidth(5, 10);
    assertEquals(vr.getStartRes(), 5);
    assertEquals(vr.getEndRes(), 14);

    // scroll to 12 - no change
    assertFalse(vr.scrollToWrappedVisible(12));
    assertEquals(vr.getStartRes(), 5);

    // scroll to 2 - back to 0-9
    assertTrue(vr.scrollToWrappedVisible(2));
    assertEquals(vr.getStartRes(), 0);
    assertEquals(vr.getEndRes(), 9);

    // scroll to 9 - no change
    assertFalse(vr.scrollToWrappedVisible(9));
    assertEquals(vr.getStartRes(), 0);

    // scroll to 12 - moves to 10-19
    assertTrue(vr.scrollToWrappedVisible(12));
    assertEquals(vr.getStartRes(), 10);
    assertEquals(vr.getEndRes(), 19);

    vr.setStartRes(13);
    assertEquals(vr.getStartRes(), 13);
    assertEquals(vr.getEndRes(), 22);

    // scroll to 45 - jumps to 43-52
    assertTrue(vr.scrollToWrappedVisible(45));
    assertEquals(vr.getStartRes(), 43);
    assertEquals(vr.getEndRes(), 52);
  }

  @Test(groups = { "Functional" })
  public void testScrollToVisible()
  {
    ViewportRanges vr = new ViewportRanges(al);
    vr.setViewportStartAndWidth(12, 5);
    vr.setViewportStartAndHeight(10, 6);
    vr.scrollToVisible(13, 14);

    // no change
    assertEquals(vr.getStartRes(), 12);
    assertEquals(vr.getStartSeq(), 10);

    vr.scrollToVisible(5, 6);
    assertEquals(vr.getStartRes(), 5);
    assertEquals(vr.getStartSeq(), 6);

    // test for hidden columns too
    al.getHiddenColumns().hideColumns(1, 3);
    vr.scrollToVisible(13, 3);
    assertEquals(vr.getStartRes(), 6);
    assertEquals(vr.getStartSeq(), 3);

    vr.scrollToVisible(2, 9);
    assertEquals(vr.getStartRes(), 0);
    assertEquals(vr.getStartSeq(), 4);
  }

  @Test(groups = { "Functional" })
  public void testEventFiring()
  {
    ViewportRanges vr = new ViewportRanges(al);
    MockPropChangeListener l = new MockPropChangeListener(vr);
    List<String> emptylist = new ArrayList<>();

    vr.setViewportWidth(5);
    vr.setViewportHeight(5);
    l.reset();

    // one event fired when startRes is called with new value
    vr.setStartRes(4);
    assertTrue(l.verify(1, Arrays.asList(ViewportRanges.STARTRES)));
    l.reset();

    // no event fired for same value
    vr.setStartRes(4);
    assertTrue(l.verify(0, emptylist));
    l.reset();

    vr.setStartSeq(4);
    assertTrue(l.verify(1, Arrays.asList(ViewportRanges.STARTSEQ)));
    l.reset();

    vr.setStartSeq(4);
    assertTrue(l.verify(0, emptylist));
    l.reset();

    vr.setEndSeq(10);
    assertTrue(l.verify(1, Arrays.asList(ViewportRanges.STARTSEQ)));
    l.reset();

    vr.setEndSeq(10);
    assertTrue(l.verify(0, emptylist));
    l.reset();

    vr.setStartEndRes(2, 15);
    assertTrue(l.verify(1, Arrays.asList(ViewportRanges.STARTRES)));
    l.reset();

    vr.setStartEndRes(2, 15);
    assertTrue(l.verify(0, emptylist));
    l.reset();

    // check new value fired by event is corrected startres
    vr.setStartEndRes(-1, 5);
    assertTrue(l.verify(1, Arrays.asList(ViewportRanges.STARTRES),
            Arrays.asList(0)));
    l.reset();

    // check new value fired by event is corrected endres
    vr.setStartEndRes(0, -1);
    assertTrue(l.verify(1, Arrays.asList(ViewportRanges.ENDRES),
            Arrays.asList(0)));
    l.reset();

    vr.setStartEndSeq(2, 15);
    assertTrue(l.verify(1, Arrays.asList(ViewportRanges.STARTSEQ)));
    l.reset();

    vr.setStartEndSeq(2, 15);
    assertTrue(l.verify(0, emptylist));
    l.reset();

    vr.setStartEndRes(2, 2); // so seq and res values should be different, in
                             // case of transposing in code
    l.reset();

    // check new value fired by event is corrected startseq
    vr.setStartEndSeq(-1, 5);
    assertTrue(l.verify(1, Arrays.asList(ViewportRanges.STARTSEQ),
            Arrays.asList(0)));
    l.reset();

    // check new value fired by event is corrected endseq
    vr.setStartEndSeq(0, -1);
    assertTrue(l.verify(1, Arrays.asList(ViewportRanges.ENDSEQ),
            Arrays.asList(0)));
    l.reset();

    // reset for later tests
    vr.setStartEndSeq(2, 15);
    l.reset();

    // test viewport height and width setting triggers event
    vr.setViewportHeight(10);
    assertTrue(l.verify(1, Arrays.asList(ViewportRanges.ENDSEQ)));
    l.reset();

    vr.setViewportWidth(18);
    assertTrue(l.verify(1, Arrays.asList(ViewportRanges.ENDRES)));
    l.reset();

    // already has seq start set to 2, so triggers endseq
    vr.setViewportStartAndHeight(2, 16);
    assertTrue(l.verify(1, Arrays.asList(ViewportRanges.ENDSEQ)));
    l.reset();

    vr.setViewportStartAndWidth(1, 14);
    assertTrue(l.verify(1, Arrays.asList(ViewportRanges.STARTRES)));
    l.reset();

    // test page up/down triggers event
    vr.pageUp();
    assertTrue(l.verify(1, Arrays.asList(ViewportRanges.STARTSEQ)));
    l.reset();

    vr.pageDown();
    assertTrue(l.verify(1, Arrays.asList(ViewportRanges.STARTSEQ)));
    l.reset();

    // test scrolling triggers event
    vr.scrollUp(true);
    assertTrue(l.verify(1, Arrays.asList(ViewportRanges.STARTSEQ)));
    l.reset();

    vr.scrollUp(false);
    assertTrue(l.verify(1, Arrays.asList(ViewportRanges.STARTSEQ)));
    l.reset();

    vr.scrollRight(true);
    assertTrue(l.verify(1, Arrays.asList(ViewportRanges.STARTRES)));
    l.reset();

    vr.scrollRight(false);
    assertTrue(l.verify(1, Arrays.asList(ViewportRanges.STARTRES)));
    l.reset();

    vr.scrollToVisible(10, 10);
    assertTrue(l.verify(4,
            Arrays.asList(ViewportRanges.STARTSEQ, ViewportRanges.STARTSEQ,
                    ViewportRanges.STARTSEQ, ViewportRanges.STARTSEQ)));
    l.reset();

    /*
     * scrollToWrappedVisible does nothing if the target position is
     * within the current startRes-endRes range
     */
    assertFalse(vr.scrollToWrappedVisible(5));
    assertTrue(l.verify(0, Collections.<String> emptyList()));
    l.reset();

    vr.scrollToWrappedVisible(25);
    assertTrue(l.verify(1, Arrays.asList(ViewportRanges.STARTRES)));
    l.reset();

    // test setStartResAndSeq triggers one event
    vr.setStartResAndSeq(5, 7);
    assertTrue(l.verify(1, Arrays.asList(ViewportRanges.STARTRESANDSEQ),
            Arrays.asList(5, 7)));

    l.reset();
  }

  @Test(groups = { "Functional" })
  public void testGetWrappedScrollPosition()
  {
    AlignmentI al2 = gen.generate(157, 15, 1, 5, 5);
    ViewportRanges vr = new ViewportRanges(al2);
    vr.setStartEndRes(0, 39);
    int width = vr.getViewportWidth(); // 40

    /*
     * scroll is 0 at column 0 (only)
     */
    assertEquals(vr.getWrappedScrollPosition(0), 0);

    /*
     * scroll is 1 at columns 1-40
     */
    int i = 1;
    int j = width;
    for (; i <= j; i++)
    {
      assertEquals(1, vr.getWrappedScrollPosition(i));
    }

    /*
     * scroll is 2 at columns 41-80, etc
     */
    j += width;
    for (; i <= j; i++)
    {
      assertEquals(2, vr.getWrappedScrollPosition(i), "For " + i);
    }
  }

  @Test(groups = { "Functional" })
  public void testPageUpDownWrapped()
  {
    /*
     * 15 sequences, 110 residues wide (+gaps)
     */
    AlignmentI al2 = gen.generate(110, 15, 1, 5, 5);

    ViewportRanges vr = new ViewportRanges(al2);
    vr.setWrappedMode(true);

    // first row
    vr.setViewportStartAndWidth(0, 40);
    int width = vr.getViewportWidth();
    assertEquals(width, 40);
    assertEquals(vr.getStartRes(), 0);
    assertEquals(vr.getEndRes(), 39);
    assertEquals(vr.getStartSeq(), 0);
    assertEquals(vr.getEndSeq(), 14);

    // second row
    vr.pageDown();
    assertEquals(vr.getStartRes(), 40);
    assertEquals(vr.getEndRes(), 79);
    assertEquals(vr.getStartSeq(), 0);
    assertEquals(vr.getEndSeq(), 14);

    // third and last row
    // note endRes is nominal (>width) to preserve viewport width
    vr.pageDown();
    assertEquals(vr.getStartRes(), 80);
    assertEquals(vr.getEndRes(), 119);
    assertEquals(vr.getStartSeq(), 0);
    assertEquals(vr.getEndSeq(), 14);

    // another pageDown should do nothing
    vr.pageDown();
    assertEquals(vr.getStartRes(), 80);
    assertEquals(vr.getEndRes(), 119);
    assertEquals(vr.getStartSeq(), 0);
    assertEquals(vr.getEndSeq(), 14);

    // back to second row
    vr.pageUp();
    assertEquals(vr.getStartRes(), 40);
    assertEquals(vr.getEndRes(), 79);
    assertEquals(vr.getStartSeq(), 0);
    assertEquals(vr.getEndSeq(), 14);

    // back to first row
    vr.pageUp();
    assertEquals(vr.getStartRes(), 0);
    assertEquals(vr.getEndRes(), 39);
    assertEquals(vr.getStartSeq(), 0);
    assertEquals(vr.getEndSeq(), 14);

    // another pageUp should do nothing
    vr.pageUp();
    assertEquals(vr.getStartRes(), 0);
    assertEquals(vr.getEndRes(), 39);
    assertEquals(vr.getStartSeq(), 0);
    assertEquals(vr.getEndSeq(), 14);

    /*
     * simulate scroll right a few positions
     */
    vr.setStartRes(5);
    assertEquals(vr.getStartRes(), 5);
    assertEquals(vr.getEndRes(), 5 + width - 1); // 44

    vr.pageDown(); // 5-44 shifts to 45-84
    assertEquals(vr.getStartRes(), 45);
    assertEquals(vr.getEndRes(), 84);

    vr.pageDown(); // 45-84 shifts to 85-124
    assertEquals(vr.getStartRes(), 85);
    assertEquals(vr.getEndRes(), 124);

    vr.pageDown(); // no change - at end already
    assertEquals(vr.getStartRes(), 85);
    assertEquals(vr.getEndRes(), 124);

    vr.pageUp(); // back we go
    assertEquals(vr.getStartRes(), 45);
    assertEquals(vr.getEndRes(), 84);

    vr.pageUp();
    assertEquals(vr.getStartRes(), 5);
    assertEquals(vr.getEndRes(), 44);

    vr.pageUp(); // back to the start
    assertEquals(vr.getStartRes(), 0);
    assertEquals(vr.getEndRes(), 39);
  }

  @Test(groups = { "Functional" })
  public void testSetStartEndResWrapped()
  {
    ViewportRanges vr = new ViewportRanges(al);
    vr.setWrappedMode(true);
    vr.setStartEndRes(-1, -1);
    assertEquals(vr.getStartRes(), 0);
    assertEquals(vr.getEndRes(), 0);

    vr.setStartEndRes(5, 19);
    assertEquals(vr.getStartRes(), 5);
    assertEquals(vr.getEndRes(), 19);

    // bounds are not constrained to alignment width
    // when in wrapped mode
    vr.setStartEndRes(88, 888);
    assertEquals(vr.getStartRes(), 88);
    assertEquals(vr.getEndRes(), 888);

    ViewportRanges vrsmall = new ViewportRanges(smallAl);
    vrsmall.setWrappedMode(true);
    vrsmall.setStartEndRes(88, 888);
    assertEquals(vrsmall.getStartRes(), 88);
    assertEquals(vrsmall.getEndRes(), 888);

    // make visible alignment width = 0
    smallAl.getHiddenColumns().hideColumns(0, 6);
    vrsmall.setStartEndRes(0, 4);
    assertEquals(vrsmall.getStartRes(), 0);
    assertEquals(vrsmall.getEndRes(), 4);
  }

  @Test(groups = { "Functional" })
  public void testSetViewportStartAndWidthWrapped()
  {
    ViewportRanges vr = new ViewportRanges(al);
    vr.setWrappedMode(true);
    vr.setViewportStartAndWidth(2, 6);
    assertEquals(vr.getViewportWidth(), 6);
    assertEquals(vr.getStartRes(), 2);

    // reset -ve values of start to 0
    vr.setViewportStartAndWidth(-1, 7);
    assertEquals(vr.getViewportWidth(), 7);
    assertEquals(vr.getStartRes(), 0);

    // out of bounds values are not forced to within bounds
    vr.setViewportStartAndWidth(35, 5);
    assertEquals(vr.getViewportWidth(), 5);
    assertEquals(vr.getStartRes(), 35);

    // small alignment doesn't get bounds reset
    ViewportRanges vrsmall = new ViewportRanges(smallAl);
    vrsmall.setViewportStartAndWidth(0, 63);
    assertEquals(vrsmall.getViewportWidth(), 7);
    assertEquals(vrsmall.getStartRes(), 0);
  }

  @Test(groups = { "Functional" })
  public void testGetWrappedMaxScroll()
  {
    // generate an ungapped alignment of width 140
    int alignmentWidth = 140;
    AlignmentI al2 = gen.generate(alignmentWidth, 15, 1, 0, 5);
    ViewportRanges vr = new ViewportRanges(al2);
    vr.setStartEndRes(0, 39);
    int width = vr.getViewportWidth(); // 40
    int partWidth = alignmentWidth % width; // 20

    /*
     * there are 3 * 40 remainder 20 residues
     * number of widths depends on offset (scroll right)
     * 4 widths (maxScroll = 3) if offset by 0 or more than 19 columns
     * 5 widths (maxScroll = 4) if 1 <= offset <= 19
     */
    for (int col = 0; col < alignmentWidth; col++)
    {
      int offset = col % width;
      if (offset > 0 && offset < partWidth)
      {
        assertEquals(vr.getWrappedMaxScroll(col), 4, "col " + col);
      }
      else
      {
        assertEquals(vr.getWrappedMaxScroll(col), 3, "col " + col);
      }
    }
  }

  @Test(groups = { "Functional" })
  public void testScrollUp_wrapped()
  {
    /*
     * alignment 30 tall and 45 wide
     */
    AlignmentI al2 = gen.generate(45, 30, 1, 0, 5);

    /*
     * wrapped view, 5 sequences high, start at sequence offset 1
     */
    ViewportRanges vr = new ViewportRanges(al2);
    vr.setWrappedMode(true);
    vr.setViewportStartAndHeight(1, 5);

    /*
     * offset wrapped view to column 3
     */
    vr.setStartEndRes(3, 22);

    int startRes = vr.getStartRes();
    int width = vr.getViewportWidth();
    assertEquals(startRes, 3);
    assertEquals(width, 20);

    // in wrapped mode, we change startRes but not startSeq
    // scroll down:
    vr.scrollUp(false);
    assertEquals(vr.getStartSeq(), 1);
    assertEquals(vr.getStartRes(), 23);

    // scroll up returns to original position
    vr.scrollUp(true);
    assertEquals(vr.getStartSeq(), 1);
    assertEquals(vr.getStartRes(), 3);

    // scroll up again returns to 'origin'
    vr.scrollUp(true);
    assertEquals(vr.getStartSeq(), 1);
    assertEquals(vr.getStartRes(), 0);

    /*
     * offset 3 columns once more and do some scroll downs
     */
    vr.setStartEndRes(3, 22);
    vr.scrollUp(false);
    assertEquals(vr.getStartSeq(), 1);
    assertEquals(vr.getStartRes(), 23);
    vr.scrollUp(false);
    assertEquals(vr.getStartSeq(), 1);
    assertEquals(vr.getStartRes(), 43);

    /*
     * scroll down beyond end of alignment does nothing
     */
    vr.scrollUp(false);
    assertEquals(vr.getStartSeq(), 1);
    assertEquals(vr.getStartRes(), 43);
  }

  @Test(groups = { "Functional" })
  public void testSetViewportLocation()
  {
    AlignmentI al2 = gen.generate(60, 80, 1, 0, 0);

    ViewportRanges vr = new ViewportRanges(al2);

    // start with viewport on 5-14
    vr.setViewportStartAndWidth(5, 10);
    assertEquals(vr.getStartRes(), 5);
    assertEquals(vr.getEndRes(), 14);

    vr.setViewportStartAndHeight(3, 13);
    assertEquals(vr.getStartSeq(), 3);
    assertEquals(vr.getEndSeq(), 15);

    // set location to (8,5) - no change
    vr.setViewportLocation(8, 5);
    assertEquals(vr.getStartRes(), 5);
    assertEquals(vr.getEndRes(), 14);
    assertEquals(vr.getStartSeq(), 3);
    assertEquals(vr.getEndSeq(), 15);

    // set location to (40,50) - change to top left (40,50)
    vr.setViewportLocation(40, 50);
    assertEquals(vr.getStartRes(), 40);
    assertEquals(vr.getEndRes(), 49);
    assertEquals(vr.getStartSeq(), 50);
    assertEquals(vr.getEndSeq(), 62);

    // set location past end of alignment - resets to leftmost pos
    vr.setViewportLocation(63, 85);
    assertEquals(vr.getStartRes(), 50);
    assertEquals(vr.getEndRes(), 59);
    assertEquals(vr.getStartSeq(), 67);
    assertEquals(vr.getEndSeq(), 79);

    // hide some columns
    al2.getHiddenColumns().hideColumns(20, 50);
    vr.setViewportLocation(55, 4);
    assertEquals(vr.getStartRes(), 19);
    assertEquals(vr.getEndRes(), 28);
    assertEquals(vr.getStartSeq(), 4);
    assertEquals(vr.getEndSeq(), 16);

    // hide some sequences
    al2.getHiddenSequences().hideSequence(al2.getSequenceAt(3));
    al2.getHiddenSequences().hideSequence(al2.getSequenceAt(4));
    vr.setViewportLocation(17, 5);
    assertEquals(vr.getStartRes(), 17);
    assertEquals(vr.getEndRes(), 26);
    assertEquals(vr.getStartSeq(), 3);
    assertEquals(vr.getEndSeq(), 15);

    // set wrapped mode
    vr.setWrappedMode(true);
    vr.setViewportLocation(1, 8);
    assertEquals(vr.getStartRes(), 0);
    assertEquals(vr.getEndRes(), 9);
    assertEquals(vr.getStartSeq(), 3);
    assertEquals(vr.getEndSeq(), 15);

    // try further down the alignment
    vr.setViewportLocation(57, 5);
    assertEquals(vr.getStartRes(), 20);
    assertEquals(vr.getEndRes(), 29);
    assertEquals(vr.getStartSeq(), 3);
    assertEquals(vr.getEndSeq(), 15);
  }
}

// mock listener for property change events
class MockPropChangeListener implements ViewportListenerI
{
  private int firecount = 0;

  private List<String> events = new ArrayList<>();

  private List<Integer> newvalues = new ArrayList<>();

  public MockPropChangeListener(ViewportRanges vr)
  {
    vr.addPropertyChangeListener(this);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    firecount++;
    events.add(evt.getPropertyName());
    if (evt.getPropertyName().equals(ViewportRanges.STARTRESANDSEQ))
    {
      newvalues.add(((int[]) evt.getNewValue())[0]);
      newvalues.add(((int[]) evt.getNewValue())[1]);
    }
    else
    {
      newvalues.add((Integer) evt.getNewValue());
    }
  }

  public boolean verify(int count, List<String> eventslist,
          List<Integer> valueslist)
  {
    return (count == firecount) && events.equals(eventslist)
            && newvalues.equals(valueslist);
  }

  public boolean verify(int count, List<String> eventslist)
  {
    return (count == firecount) && events.equals(eventslist);
  }

  public void reset()
  {
    firecount = 0;
    events.clear();
    newvalues.clear();
  }
}
