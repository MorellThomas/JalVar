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
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceCollectionI;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;

import java.util.Hashtable;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded = true)
public class OverviewDimensionsShowHiddenTest
{
  AlignmentI al;

  OverviewDimensionsShowHidden od;

  // cached widths and heights
  int boxWidth;

  int boxHeight;

  int viewHeight;

  int viewWidth;

  int alheight;

  int alwidth;

  ViewportRanges vpranges;

  Hashtable<SequenceI, SequenceCollectionI> hiddenRepSequences = new Hashtable<>();

  HiddenColumns hiddenCols = new HiddenColumns();

  @BeforeClass(alwaysRun = true)
  public void setUpAlignment()
  {
    // create random alignment
    AlignmentGenerator gen = new AlignmentGenerator(false);
    al = gen.generate(157, 525, 123, 5, 5);
  }

  @BeforeMethod(alwaysRun = true)
  public void setUp()
  {
    if (!hiddenRepSequences.isEmpty())
    {
      al.getHiddenSequences().showAll(hiddenRepSequences);
    }
    ColumnSelection colsel = new ColumnSelection();
    hiddenCols.revealAllHiddenColumns(colsel);

    vpranges = new ViewportRanges(al);
    vpranges.setViewportStartAndHeight(0, 18);
    vpranges.setViewportStartAndWidth(0, 63);

    viewHeight = vpranges.getEndSeq() - vpranges.getStartSeq() + 1;
    viewWidth = vpranges.getEndRes() - vpranges.getStartRes() + 1;

    HiddenColumns hiddenCols = new HiddenColumns();

    od = new OverviewDimensionsShowHidden(vpranges, true);
    // Initial box sizing - default path through code
    od.setBoxPosition(al.getHiddenSequences(), hiddenCols);

    mouseClick(od, 0, 0);
    moveViewport(0, 0);

    // calculate before hidden columns so we get absolute values
    alheight = vpranges.getAbsoluteAlignmentHeight();
    alwidth = vpranges.getAbsoluteAlignmentWidth();

    boxWidth = Math.round(
            (float) (vpranges.getEndRes() - vpranges.getStartRes() + 1)
                    * od.getWidth() / alwidth);
    boxHeight = Math.round(
            (float) (vpranges.getEndSeq() - vpranges.getStartSeq() + 1)
                    * od.getSequencesHeight() / alheight);
  }

  @AfterClass(alwaysRun = true)
  public void cleanUp()
  {
    al = null;
  }

  /**
   * Test that the OverviewDimensions constructor sets width and height
   * correctly
   */
  @Test(groups = { "Functional" })
  public void testConstructor()
  {
    SequenceI seqa = new Sequence("Seq1", "ABC");
    SequenceI seqb = new Sequence("Seq2", "ABC");
    SequenceI seqc = new Sequence("Seq3", "ABC");
    SequenceI seqd = new Sequence("Seq4", "ABC");
    SequenceI seqe = new Sequence("Seq5",
            "ABCABCABCABCABCABCABCABCBACBACBACBAC");

    int defaultGraphHeight = 20;
    int maxWidth = 400;
    int minWidth = 120;
    int maxSeqHeight = 300;
    int minSeqHeight = 40;

    // test for alignment with width > height
    SequenceI[] seqs1 = new SequenceI[] { seqa, seqb };
    Alignment al1 = new Alignment(seqs1);
    ViewportRanges props = new ViewportRanges(al1);

    OverviewDimensions od = new OverviewDimensionsShowHidden(props, true);
    int scaledHeight = 267;
    assertEquals(od.getGraphHeight(), defaultGraphHeight);
    assertEquals(od.getSequencesHeight(), scaledHeight);
    assertEquals(od.getWidth(), maxWidth);
    assertEquals(od.getHeight(), scaledHeight + defaultGraphHeight);

    // test for alignment with width < height
    SequenceI[] seqs2 = new SequenceI[] { seqa, seqb, seqc, seqd };
    Alignment al2 = new Alignment(seqs2);
    props = new ViewportRanges(al2);

    od = new OverviewDimensionsShowHidden(props, true);
    int scaledWidth = 300;
    assertEquals(od.getGraphHeight(), defaultGraphHeight);
    assertEquals(od.getSequencesHeight(), maxSeqHeight);
    assertEquals(od.getWidth(), scaledWidth);
    assertEquals(od.getHeight(), scaledWidth + defaultGraphHeight);

    // test for alignment with width > height and sequence height scaled below
    // min value
    SequenceI[] seqs3 = new SequenceI[] { seqe };
    Alignment al3 = new Alignment(seqs3);
    props = new ViewportRanges(al3);

    od = new OverviewDimensionsShowHidden(props, true);
    assertEquals(od.getGraphHeight(), defaultGraphHeight);
    assertEquals(od.getSequencesHeight(), minSeqHeight);
    assertEquals(od.getWidth(), maxWidth);
    assertEquals(od.getHeight(), minSeqHeight + defaultGraphHeight);

    // test for alignment with width < height and width scaled below min value
    SequenceI[] seqs4 = new SequenceI[] { seqa, seqb, seqc, seqd, seqa,
        seqb, seqc, seqd, seqa, seqb, seqc, seqd, seqa, seqb, seqc, seqd };
    Alignment al4 = new Alignment(seqs4);
    props = new ViewportRanges(al4);

    od = new OverviewDimensionsShowHidden(props, true);
    assertEquals(od.getGraphHeight(), defaultGraphHeight);
    assertEquals(od.getSequencesHeight(), maxSeqHeight);
    assertEquals(od.getWidth(), minWidth);
    assertEquals(od.getHeight(), maxSeqHeight + defaultGraphHeight);

    Alignment al5 = new Alignment(seqs4);
    props = new ViewportRanges(al5);

    od = new OverviewDimensionsShowHidden(props, false);
    assertEquals(od.getGraphHeight(), 0);
    assertEquals(od.getSequencesHeight(), maxSeqHeight);
    assertEquals(od.getWidth(), minWidth);
    assertEquals(od.getHeight(), maxSeqHeight);
  }

  /**
   * Test that validation after mouse adjustments to boxX and boxY sets box
   * dimensions and scroll values correctly, when there are no hidden rows or
   * columns.
   */
  @Test(groups = { "Functional" })
  public void testSetBoxFromMouseClick()
  {
    od.updateViewportFromMouse(0, 0, al.getHiddenSequences(), hiddenCols);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(vpranges.getStartRes(), 0);
    assertEquals(vpranges.getStartSeq(), 0);

    // negative boxX value reset to 0
    mouseClick(od, -5, 10);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
    assertEquals(vpranges.getStartSeq() + vpranges.getViewportHeight() / 2,
            Math.round((float) 10 * alheight / od.getSequencesHeight()));
    assertEquals(vpranges.getStartRes(), 0);

    // negative boxY value reset to 0
    mouseClick(od, 6, -2);
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
    assertEquals(vpranges.getStartRes(), 0);
    assertEquals(vpranges.getStartSeq(), 0);

    // overly large boxX value reset to width-boxWidth
    mouseClick(od, 101, 6);
    assertEquals(od.getBoxX(), od.getWidth() - od.getBoxWidth());
    assertEquals(od.getBoxY(), 1);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
    assertEquals(vpranges.getStartRes(),
            Math.round((float) od.getBoxX() * alwidth / od.getWidth()));
    assertEquals(vpranges.getStartSeq(), Math.round(
            (float) od.getBoxY() * alheight / od.getSequencesHeight()));

    // overly large boxY value reset to sequenceHeight - boxHeight
    mouseClick(od, 10, 520);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), od.getSequencesHeight() - od.getBoxHeight());
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
    assertEquals(0,
            Math.round((float) od.getBoxX() * alwidth / od.getWidth()));

    // here (float) od.getBoxY() * alheight / od.getSequencesHeight() = 507.5
    // and round rounds to 508; however we get 507 working with row values
    // hence the subtraction of 1
    assertEquals(vpranges.getStartSeq(), Math.round(
            (float) od.getBoxY() * alheight / od.getSequencesHeight()) - 1);

    // click past end of alignment, as above
    mouseClick(od, 3000, 5);
    assertEquals(od.getBoxX(), od.getWidth() - od.getBoxWidth());
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
    assertEquals(vpranges.getStartRes(),
            Math.round((float) od.getBoxX() * alwidth / od.getWidth()));
    assertEquals(vpranges.getStartSeq(), Math.round(
            (float) od.getBoxY() * alheight / od.getSequencesHeight()));

    // move viewport so startRes non-zero and then mouseclick
    moveViewportH(20);

    // click at viewport position
    int oldboxx = od.getBoxX();
    int oldboxy = od.getBoxY();
    mouseClick(od, od.getBoxX() + od.getBoxWidth() / 2 + 6,
            od.getBoxY() + od.getBoxHeight() / 2 + 3);
    assertEquals(od.getBoxX(), oldboxx + 6);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
    assertEquals(vpranges.getStartRes(),
            Math.round((float) od.getBoxX() * alwidth / od.getWidth()));
    assertEquals(od.getBoxY(), oldboxy + 3);
    assertEquals(vpranges.getStartSeq(), Math.round(
            (float) od.getBoxY() * alheight / od.getSequencesHeight()));

    // click at top corner
    mouseClick(od, 0, 0);
    assertEquals(od.getBoxX(), 0);
    assertEquals(vpranges.getStartRes(), 0);
    assertEquals(od.getBoxY(), 0);
    assertEquals(vpranges.getStartSeq(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
  }

  /**
   * Test setting of the box position, when there are hidden cols at the start
   * of the alignment
   */
  @Test(groups = { "Functional" })
  public void testFromMouseWithHiddenColsAtStart()
  {
    od.updateViewportFromMouse(0, 0, al.getHiddenSequences(), hiddenCols);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(vpranges.getStartRes(), 0);
    assertEquals(vpranges.getStartSeq(), 0);

    // hide cols at start and check updated box position is correct
    // changes boxX but not boxwidth
    int lastHiddenCol = 30;
    hiddenCols.hideColumns(0, lastHiddenCol);

    od.setBoxPosition(al.getHiddenSequences(), hiddenCols);
    assertEquals(od.getBoxX(), Math
            .round((float) (lastHiddenCol + 1) * od.getWidth() / alwidth));
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);

    // try to click in hidden cols, check box does not move
    int xpos = 10;
    mouseClick(od, xpos, 0);
    assertEquals(od.getBoxX(), Math
            .round((float) (lastHiddenCol + 1) * od.getWidth() / alwidth));
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
    assertEquals(vpranges.getStartSeq(), 0);
    assertEquals(vpranges.getStartRes(), 0);

    // click to right of hidden columns, box moves to click point
    mouseClick(od, 60 + boxWidth / 2, boxHeight / 2);
    assertEquals(od.getBoxX(), 60);
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
    assertEquals(vpranges.getStartSeq(), 0);
    assertEquals(vpranges.getStartRes(),
            Math.round((float) 60 * alwidth / od.getWidth())
                    - (lastHiddenCol + 1));

    // click to right of hidden columns such that box runs over right hand side
    // of alignment
    // box position is adjusted away from the edge
    // overly large boxX value reset to width-boxWidth
    xpos = 100;
    mouseClick(od, xpos + boxWidth / 2, 5 + boxHeight / 2);
    assertEquals(od.getBoxX(), od.getWidth() - od.getBoxWidth());
    assertEquals(od.getBoxY(), 5);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
    assertEquals(vpranges.getStartRes(),
            Math.round((float) od.getBoxX() * alwidth / od.getWidth())
                    - (lastHiddenCol + 1));
    assertEquals(vpranges.getStartSeq(), Math.round(
            (float) od.getBoxY() * alheight / od.getSequencesHeight()));
  }

  /**
   * Test setting of the box position, when there are hidden cols in the middle
   * of the alignment
   */
  @Test(groups = { "Functional" })
  public void testFromMouseWithHiddenColsInMiddle()
  {
    od.updateViewportFromMouse(0, 0, al.getHiddenSequences(), hiddenCols);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(vpranges.getStartRes(), 0);
    assertEquals(vpranges.getStartSeq(), 0);

    // hide columns 63-73, no change to box position or dimensions
    int firstHidden = 63;
    int lastHidden = 73;
    hiddenCols.hideColumns(firstHidden, lastHidden);

    od.setBoxPosition(al.getHiddenSequences(), hiddenCols);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(vpranges.getStartRes(), 0);
    assertEquals(vpranges.getStartSeq(), 0);

    // move box so that it overlaps with hidden cols on one side
    // box width changes, boxX and scrollCol as for unhidden case
    int xpos = 54 - boxWidth / 2; // 54 is position in overview approx halfway
    // between cols 60 and 70
    mouseClick(od, xpos, boxHeight / 2);
    assertEquals(od.getBoxX(), xpos - boxWidth / 2);
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(),
            Math.round(boxWidth + (float) (lastHidden - firstHidden + 1)
                    * od.getWidth() / alwidth));
    assertEquals(od.getBoxHeight(), boxHeight);
    assertEquals(vpranges.getStartRes(),
            Math.round((xpos - boxWidth / 2) * alwidth / od.getWidth())
                    + 1); // +1 for rounding
    assertEquals(vpranges.getStartSeq(), 0);

    // move box so that it completely covers hidden cols
    // box width changes, boxX and scrollCol as for hidden case
    xpos = 24 + boxWidth / 2;
    mouseClick(od, xpos, 0);
    assertEquals(od.getBoxX(), 24);
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(),
            Math.round(boxWidth + (float) (lastHidden - firstHidden + 1)
                    * od.getWidth() / alwidth));
    assertEquals(od.getBoxHeight(), boxHeight);
    assertEquals(vpranges.getStartRes(),
            Math.round((float) 24 * alwidth / od.getWidth()));
    assertEquals(vpranges.getStartSeq(), 0);

    // move box so boxX is to right of hidden cols, but does not go beyond full
    // width of alignment
    // box width, boxX and scrollCol all as for non-hidden case
    xpos = Math.round((float) 75 * od.getWidth() / alwidth) + boxWidth / 2;
    mouseClick(od, xpos, boxHeight / 2);
    assertEquals(od.getBoxX(), xpos - boxWidth / 2);
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
    assertEquals(vpranges.getStartSeq(), 0);
    assertEquals(vpranges.getStartRes(),
            75 - (lastHidden - firstHidden + 1));

    // move box so it goes beyond full width of alignment
    // boxX, scrollCol adjusted back, box width normal
    xpos = 3000;
    mouseClick(od, xpos, 5);
    assertEquals(od.getBoxX(), od.getWidth() - od.getBoxWidth());
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
    assertEquals(vpranges.getStartRes(),
            Math.round(((float) od.getBoxX() * alwidth / od.getWidth())
                    - (lastHidden - firstHidden + 1)));
    assertEquals(vpranges.getStartSeq(), Math.round(
            (float) od.getBoxY() * alheight / od.getSequencesHeight()));

  }

  /**
   * Test setting of the box position, when there are hidden cols at the end of
   * the alignment
   */
  @Test(groups = { "Functional" })
  public void testFromMouseWithHiddenColsAtEnd()
  {
    od.updateViewportFromMouse(0, 0, al.getHiddenSequences(), hiddenCols);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(vpranges.getStartRes(), 0);
    assertEquals(vpranges.getStartSeq(), 0);

    // hide columns 140-164, no change to box position or dimensions
    int firstHidden = 140;
    int lastHidden = 164;
    hiddenCols.hideColumns(firstHidden, lastHidden);
    od.setBoxPosition(al.getHiddenSequences(), hiddenCols);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(vpranges.getStartRes(), 0);
    assertEquals(vpranges.getStartSeq(), 0);

    // click to left of hidden cols, without overlapping
    // boxX, scrollCol and width as normal
    int xpos = 30;
    int ypos = 6;
    testBoxIsAtClickPoint(xpos, ypos);
    assertEquals(vpranges.getStartSeq(), Math.round(
            (float) (ypos - boxHeight / 2) * alheight / od.getHeight()));
    assertEquals(vpranges.getStartRes(), Math.round(
            (float) (xpos - boxWidth / 2) * alwidth / od.getWidth()));

    // click to left of hidden cols, with overlap
    // boxX and scrollCol adjusted for hidden cols, width normal
    xpos = Math.round((float) 145 * od.getWidth() / alwidth) - boxWidth;
    mouseClick(od, xpos + boxWidth / 2, boxHeight / 2);
    assertEquals(od.getBoxX(),
            Math.round((float) (firstHidden - 1) * od.getWidth() / alwidth)
                    - boxWidth + 1);
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
    assertEquals(vpranges.getStartRes(),
            Math.round((float) (od.getBoxX()) * alwidth / od.getWidth()));
    assertEquals(vpranges.getStartSeq(), 0);

    // click in hidden cols
    // boxX and scrollCol adjusted for hidden cols, width normal
    xpos = 115;
    assertEquals(od.getBoxX(),
            Math.round((float) (firstHidden - 1) * od.getWidth() / alwidth)
                    - boxWidth + 1);
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
    assertEquals(vpranges.getStartRes(),
            Math.round((float) od.getBoxX() * alwidth / od.getWidth()));
    assertEquals(vpranges.getStartSeq(), 0);

    // click off end of alignment
    // boxX and scrollCol adjusted for hidden cols, width normal
    xpos = 3000;
    assertEquals(od.getBoxX(),
            Math.round((float) (firstHidden - 1) * od.getWidth() / alwidth)
                    - boxWidth + 1);
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
    assertEquals(vpranges.getStartRes(),
            Math.round((float) od.getBoxX() * alwidth / od.getWidth()));
    assertEquals(vpranges.getStartSeq(), 0);
  }

  /**
   * Test that the box position is set correctly when set from the viewport,
   * with no hidden rows or columns
   */
  @Test(groups = { "Functional" })
  public void testSetBoxFromViewport()
  {
    // move viewport to start of alignment
    moveViewport(0, 0);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);

    // move viewport to right
    moveViewportH(70);
    assertEquals(od.getBoxX(),
            Math.round((float) 70 * od.getWidth() / alwidth));
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);

    // move viewport down
    moveViewportV(100);
    assertEquals(od.getBoxX(),
            Math.round((float) 70 * od.getWidth() / alwidth));
    assertEquals(od.getBoxY(),
            Math.round(100 * od.getSequencesHeight() / alheight));
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);

    // move viewport to bottom right
    moveViewport(98, 508);
    assertEquals(od.getBoxX(),
            Math.round((float) 98 * od.getWidth() / alwidth));
    assertEquals(od.getBoxY(),
            Math.round((float) 508 * od.getSequencesHeight() / alheight));
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
  }

  /**
   * Test that the box position is set correctly when there are hidden columns
   * at the start
   */
  @Test(groups = { "Functional" })
  public void testSetBoxFromViewportHiddenColsAtStart()
  {
    int firstHidden = 0;
    int lastHidden = 20;
    hiddenCols.hideColumns(firstHidden, lastHidden);

    // move viewport to start of alignment
    moveViewport(0, 0);
    assertEquals(od.getBoxX(),
            Math.round((float) (lastHidden + 1) * od.getWidth() / alwidth));
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);

    // move viewport to end of alignment - need to make startRes by removing
    // hidden cols because of how viewport/overview are implemented
    moveViewport(98 - lastHidden - 1, 0);
    assertEquals(od.getBoxX(),
            Math.round((float) 98 * od.getWidth() / alwidth));
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
  }

  /**
   * Test that the box position is set correctly when there are hidden columns
   * in the middle
   */
  @Test(groups = { "Functional" })
  public void testSetBoxFromViewportHiddenColsInMiddle()
  {
    int firstHidden = 68;
    int lastHidden = 78;
    hiddenCols.hideColumns(firstHidden, lastHidden);

    // move viewport before hidden columns
    moveViewport(3, 0);

    assertEquals(od.getBoxX(),
            Math.round((float) 3 * od.getWidth() / alwidth));
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);

    // move viewport to left of hidden columns with overlap
    moveViewport(10, 0);
    assertEquals(od.getBoxX(),
            Math.round((float) 10 * od.getWidth() / alwidth));
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(),
            boxWidth + Math.round((float) (lastHidden - firstHidden + 1)
                    * od.getWidth() / alwidth));
    assertEquals(od.getBoxHeight(), boxHeight);

    // move viewport to straddle hidden columns
    moveViewport(63, 0);
    assertEquals(od.getBoxX(),
            Math.round((float) 63 * od.getWidth() / alwidth));
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth + Math.round(
            (lastHidden - firstHidden + 1) * od.getWidth() / alwidth));
    assertEquals(od.getBoxHeight(), boxHeight);

    // move viewport to right of hidden columns, no overlap
    moveViewport(80 - (lastHidden - firstHidden + 1), 0);
    assertEquals(od.getBoxX(),
            Math.round((float) 80 * od.getWidth() / alwidth));
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);

  }

  /**
   * Test that the box position is set correctly when there are hidden columns
   * at the end
   */
  @Test(groups = { "Functional" })
  public void testSetBoxFromViewportHiddenColsAtEnd()
  {
    int firstHidden = 152;
    int lastHidden = 164;
    hiddenCols.hideColumns(firstHidden, lastHidden);

    // move viewport before hidden columns
    moveViewport(3, 0);
    assertEquals(od.getBoxX(),
            Math.round((float) 3 * od.getWidth() / alwidth));
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);

    // move viewport to hidden columns
    // viewport can't actually extend into hidden cols,
    // so move to the far right edge of the viewport
    moveViewport(firstHidden - viewWidth, 0);
    assertEquals(od.getBoxX(), Math.round(
            (float) (firstHidden - viewWidth) * od.getWidth() / alwidth));
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
  }

  /**
   * Test that the box position is set correctly when there are hidden rows at
   * the start
   */
  @Test(groups = { "Functional" })
  public void testSetBoxFromViewportHiddenRowsAtStart()
  {
    int firstHidden = 0;
    int lastHidden = 20;
    hideSequences(firstHidden, lastHidden);

    // move viewport to start of alignment:
    // box moves to below hidden rows, height remains same
    moveViewport(0, 0);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), Math.round(
            (float) (lastHidden + 1) * od.getSequencesHeight() / alheight));
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);

    // move viewport to end of alignment
    moveViewport(0, 525 - viewHeight - lastHidden - 1);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), Math.round((float) (525 - viewHeight)
            * od.getSequencesHeight() / alheight));
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
  }

  /**
   * Test that the box position is set correctly when there are hidden rows in
   * the middle
   */
  @Test(groups = { "Functional" })
  public void testSetBoxFromViewportHiddenRowsInMiddle()
  {
    int firstHidden = 200;
    int lastHidden = 210;
    hideSequences(firstHidden, lastHidden);

    // move viewport to start of alignment:
    // box, height etc as in non-hidden case
    moveViewport(0, 0);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);

    // move viewport to straddle hidden rows
    moveViewport(0, 198);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(),
            Math.round((float) 198 * od.getSequencesHeight() / alheight));
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(),
            Math.round((float) (viewHeight + lastHidden - firstHidden + 1)
                    * od.getSequencesHeight() / alheight));
  }

  /**
   * Test that the box position is set correctly when there are hidden rows at
   * the bottom
   */
  @Test(groups = { "Functional" })
  public void testSetBoxFromViewportHiddenRowsAtEnd()
  {
    int firstHidden = 500;
    int lastHidden = 524;
    hideSequences(firstHidden, lastHidden);

    // move viewport to start of alignment:
    // box, height etc as in non-hidden case
    moveViewport(0, 0);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);

    // move viewport to end of alignment
    // viewport sits above hidden rows and does not include them
    moveViewport(0, firstHidden - viewHeight - 1);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(),
            Math.round((float) (firstHidden - viewHeight - 1)
                    * od.getSequencesHeight() / alheight));
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);

  }

  /**
   * Test setting of the box position, when there are hidden rows at the start
   * of the alignment
   */
  @Test(groups = { "Functional" })
  public void testFromMouseWithHiddenRowsAtStart()
  {
    od.updateViewportFromMouse(0, 0, al.getHiddenSequences(), hiddenCols);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxHeight(), boxHeight);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(vpranges.getStartRes(), 0);
    assertEquals(vpranges.getStartSeq(), 0);

    // hide rows at start and check updated box position is correct
    // changes boxY but not boxheight
    int lastHiddenRow = 30;
    hideSequences(0, lastHiddenRow);

    od.setBoxPosition(al.getHiddenSequences(), hiddenCols);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), Math.round((float) (lastHiddenRow + 1)
            * od.getSequencesHeight() / alheight));
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);

    // click in hidden rows - same result
    mouseClick(od, 0, 0);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), Math.round((float) (lastHiddenRow + 1)
            * od.getSequencesHeight() / alheight));
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);

    // click below hidden rows
    mouseClick(od, 0, 150 + boxHeight / 2);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), 150);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
  }

  /**
   * Test setting of the box position, when there are hidden rows at the middle
   * of the alignment
   */
  @Test(groups = { "Functional" })
  public void testFromMouseWithHiddenRowsInMiddle()
  {
    od.updateViewportFromMouse(0, 0, al.getHiddenSequences(), hiddenCols);

    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
    assertEquals(vpranges.getStartRes(), 0);
    assertEquals(vpranges.getStartSeq(), 0);

    // hide rows in middle and check updated box position is correct
    // no changes
    int firstHiddenRow = 50;
    int lastHiddenRow = 54;
    hideSequences(firstHiddenRow, lastHiddenRow);

    od.setBoxPosition(al.getHiddenSequences(), hiddenCols);

    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);

    // click above hidden rows, so that box overlaps
    int rowpos = 35; // row value in residues
    int centrepos = 43; // centre row
    mouseClick(od, 0, Math
            .round((float) centrepos * od.getSequencesHeight() / alheight));
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), Math
            .round((float) rowpos * od.getSequencesHeight() / alheight));
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(),
            boxHeight + Math
                    .round((float) (lastHiddenRow - firstHiddenRow + 1)
                            * od.getSequencesHeight() / alheight));
  }

  /**
   * Test setting of the box position, when there are hidden rows at the end of
   * the alignment
   */
  @Test(groups = { "Functional" })
  public void testFromMouseWithHiddenRowsAtEnd()
  {
    od.updateViewportFromMouse(0, 0, al.getHiddenSequences(), hiddenCols);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
    assertEquals(vpranges.getStartRes(), 0);
    assertEquals(vpranges.getStartSeq(), 0);

    // hide rows at end and check updated box position is correct
    // no changes
    int firstHidden = 500;
    int lastHidden = 524;
    hideSequences(firstHidden, lastHidden);

    od.setBoxPosition(al.getHiddenSequences(), hiddenCols);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);

    // click above hidden rows, no overlap
    int ypos = 40 + viewHeight / 2; // top is row 40
    mouseClick(od, 0,
            Math.round((float) ypos * od.getSequencesHeight() / alheight));
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(),
            Math.round((float) 40 * od.getSequencesHeight() / alheight));
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);

    // click above hidden rows so box overlaps
    // boxY moved upwards, boxHeight remains same
    ypos = 497 + viewHeight / 2; // row 497
    mouseClick(od, 0,
            Math.round((float) ypos * od.getSequencesHeight() / alheight));
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), Math.round((float) (firstHidden - viewHeight)
            * od.getSequencesHeight() / alheight));
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);

    // click within hidden rows
    ypos = 505 + boxHeight / 2;
    mouseClick(od, 0,
            Math.round((float) ypos * od.getSequencesHeight() / alheight));
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), Math.round((firstHidden - viewHeight)
            * od.getSequencesHeight() / alheight));
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
  }

  /**
   * Test the function to determine if a point is in the overview's box or not
   */
  @Test(groups = { "Functional" })
  public void testPositionInBox()
  {
    od.updateViewportFromMouse(0, 0, al.getHiddenSequences(), hiddenCols);

    assertFalse(od.isPositionInBox(0, 0));
    assertTrue(od.isPositionInBox(10, 9));
    assertFalse(od.isPositionInBox(0, 9));
    assertFalse(od.isPositionInBox(9, 0));
    assertFalse(od.isPositionInBox(75, 20));

    assertTrue(od.isPositionInBox(47, 6));
    assertFalse(od.isPositionInBox(48, 6));
    assertTrue(od.isPositionInBox(47, 9));
    assertFalse(od.isPositionInBox(47, 10));

    // hide columns in the box area
    // extends area where a point is considered to be in the box
    hiddenCols.hideColumns(1, 4);
    od.setBoxPosition(al.getHiddenSequences(), hiddenCols);
    assertFalse(od.isPositionInBox(0, 0));
    assertTrue(od.isPositionInBox(10, 9));
    assertFalse(od.isPositionInBox(0, 9));
    assertFalse(od.isPositionInBox(9, 0));
    assertFalse(od.isPositionInBox(75, 20));

    assertTrue(od.isPositionInBox(47, 6));
    assertTrue(od.isPositionInBox(48, 6));
    assertTrue(od.isPositionInBox(47, 9));
    assertFalse(od.isPositionInBox(47, 10));

    // hide sequences in box area
    // extends area where a point is considered to be in the box
    hideSequences(1, 3);
    ColumnSelection cs = new ColumnSelection();
    hiddenCols.revealAllHiddenColumns(cs);
    od.setBoxPosition(al.getHiddenSequences(), hiddenCols);
    assertFalse(od.isPositionInBox(0, 0));
    assertTrue(od.isPositionInBox(10, 9));
    assertFalse(od.isPositionInBox(0, 9));
    assertFalse(od.isPositionInBox(9, 0));
    assertFalse(od.isPositionInBox(75, 20));

    assertTrue(od.isPositionInBox(47, 6));
    assertFalse(od.isPositionInBox(48, 6));
    assertTrue(od.isPositionInBox(47, 9));
    assertTrue(od.isPositionInBox(47, 10));
  }

  /**
   * Test the dragging functionality
   */
  @Test(groups = { "Functional" })
  public void testDragging()
  {
    od.updateViewportFromMouse(0, 0, al.getHiddenSequences(), hiddenCols);
    od.setDragPoint(4, 16, al.getHiddenSequences(), hiddenCols);
    od.adjustViewportFromMouse(20, 22, al.getHiddenSequences(), hiddenCols);

    // updates require an OverviewPanel to exist which it doesn't here
    // so call setBoxPosition() as it would be called by the AlignmentPanel
    // normally
    od.setBoxPosition(al.getHiddenSequences(), hiddenCols);

    // corner moves 16 (20-4) right and 6 (22-16) up
    assertEquals(od.getBoxX(), 16);
    assertEquals(od.getBoxY(), 6);

    // hide columns - box moves drag distance + hidden cols, vertically makes no
    // difference
    hiddenCols.hideColumns(1, 4);
    od.updateViewportFromMouse(0, 0, al.getHiddenSequences(), hiddenCols);
    od.setDragPoint(4, 16, al.getHiddenSequences(), hiddenCols);
    od.adjustViewportFromMouse(20, 22, al.getHiddenSequences(), hiddenCols);
    od.setBoxPosition(al.getHiddenSequences(), hiddenCols);

    // corner moves 16 (20-4) + hiddenCols right and 6 (22-16) down
    assertEquals(od.getBoxX(),
            16 + Math.round((float) 4 * od.getWidth() / alwidth));
    assertEquals(od.getBoxY(), 6);

    // hide sequences in box area
    // makes absolutely no difference
    hideSequences(1, 3);
    od.updateViewportFromMouse(0, 0, al.getHiddenSequences(), hiddenCols);
    od.setDragPoint(4, 16, al.getHiddenSequences(), hiddenCols);
    od.adjustViewportFromMouse(20, 22, al.getHiddenSequences(), hiddenCols);
    od.setBoxPosition(al.getHiddenSequences(), hiddenCols);

    // corner moves 16 (20-4) + hiddenCols right and 6 (22-16) + hiddenRows down
    assertEquals(od.getBoxX(),
            16 + Math.round((float) 4 * od.getWidth() / alwidth));
    assertEquals(od.getBoxY(),
            6 + Math.round((float) 3 * od.getHeight() / alheight));
  }

  /*
   * Move viewport horizontally: startRes + previous width gives new horizontal extent. Vertical extent stays the same.
   */
  private void moveViewportH(int startRes)
  {
    vpranges.setViewportStartAndWidth(startRes, viewWidth);
    od.setBoxPosition(al.getHiddenSequences(), hiddenCols);
  }

  /*
   * Move viewport vertically: startSeq and endSeq give new vertical extent. Horizontal extent stays the same.
   */
  private void moveViewportV(int startSeq)
  {
    vpranges.setViewportStartAndHeight(startSeq, viewHeight);
    od.setBoxPosition(al.getHiddenSequences(), hiddenCols);
  }

  /*
   * Move viewport horizontally and vertically.
   */
  private void moveViewport(int startRes, int startSeq)
  {
    vpranges.setViewportStartAndWidth(startRes, viewWidth);
    vpranges.setViewportStartAndHeight(startSeq, viewHeight);
    od.setBoxPosition(al.getHiddenSequences(), hiddenCols);
  }

  /*
   * Mouse click as position x,y in overview window
   */
  private void mouseClick(OverviewDimensions od, int x, int y)
  {
    od.updateViewportFromMouse(x, y, al.getHiddenSequences(), hiddenCols);

    // updates require an OverviewPanel to exist which it doesn't here
    // so call setBoxPosition() as it would be called by the AlignmentPanel
    // normally
    od.setBoxPosition(al.getHiddenSequences(), hiddenCols);
  }

  /*
   * Test that the box is positioned with the centre at xpos, ypos
   * and with the original width and height
   */
  private void testBoxIsAtClickPoint(int xpos, int ypos)
  {
    mouseClick(od, xpos, ypos);
    assertEquals(od.getBoxX() + od.getBoxWidth() / 2, xpos);
    assertEquals(od.getBoxY() + od.getBoxHeight() / 2, ypos);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);

  }

  /*
   * Hide sequences between start and end
   */
  private void hideSequences(int start, int end)
  {
    SequenceI[] allseqs = al.getSequencesArray();
    SequenceGroup theseSeqs = new SequenceGroup();

    for (int i = start; i <= end; i++)
    {
      theseSeqs.addSequence(allseqs[i], false);
      al.getHiddenSequences().hideSequence(allseqs[i]);
    }

    hiddenRepSequences.put(allseqs[start], theseSeqs);
  }

  /**
   * Test setting of the box position, when there are hidden rows at the start
   * of the alignment
   */
  @Test(groups = { "Functional" })
  public void testFromMouseWithHiddenRowsAtStartWrapped()
  {
    vpranges.setWrappedMode(true);
    od.updateViewportFromMouse(0, 0, al.getHiddenSequences(), hiddenCols);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), 0);
    assertEquals(od.getBoxHeight(), boxHeight);
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(vpranges.getStartRes(), 0);
    assertEquals(vpranges.getStartSeq(), 0);

    // hide rows at start and check updated box position is correct
    // changes boxY but not boxheight
    int lastHiddenRow = 30;
    hideSequences(0, lastHiddenRow);

    od.setBoxPosition(al.getHiddenSequences(), hiddenCols);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), Math.round((float) (lastHiddenRow + 1)
            * od.getSequencesHeight() / alheight));
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);

    // click in hidden rows - same result
    mouseClick(od, 0, 0);
    assertEquals(od.getBoxX(), 0);
    int boxY = od.getBoxY();
    assertEquals(boxY, Math.round((float) (lastHiddenRow + 1)
            * od.getSequencesHeight() / alheight));
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);

    // click below hidden rows
    // vertical move of overview box is suppressed in wrapped mode
    mouseClick(od, 0, 150);
    assertEquals(od.getBoxX(), 0);
    assertEquals(od.getBoxY(), boxY); // unchanged
    assertEquals(od.getBoxWidth(), boxWidth);
    assertEquals(od.getBoxHeight(), boxHeight);
  }
}
