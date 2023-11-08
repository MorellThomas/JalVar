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

import java.awt.Font;
import java.awt.FontMetrics;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import jalview.bin.Cache;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SearchResults;
import jalview.datamodel.SearchResultsI;
import jalview.io.DataSourceType;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;

import junit.extensions.PA;

public class SeqCanvasTest
{
  private AlignFrame af;

  /**
   * Test the method that computes wrapped width in residues, height of wrapped
   * widths in pixels, and the number of widths visible
   */
  @Test(groups = "Functional")
  public void testCalculateWrappedGeometry_noAnnotations()
  {
    AlignViewport av = af.getViewport();
    AlignmentI al = av.getAlignment();
    assertEquals(al.getWidth(), 157);
    assertEquals(al.getHeight(), 15);
    av.getRanges().setStartEndSeq(0, 14);

    SeqCanvas testee = af.alignPanel.getSeqPanel().seqCanvas;

    av.setWrapAlignment(true);
    av.setFont(new Font("SansSerif", Font.PLAIN, 14), true);
    int charHeight = av.getCharHeight();
    int charWidth = av.getCharWidth();
    assertEquals(charHeight, 17);
    assertEquals(charWidth, 12);

    /*
     * first with scales above, left, right
     */
    av.setShowAnnotation(false);
    av.setScaleAboveWrapped(true);
    av.setScaleLeftWrapped(true);
    av.setScaleRightWrapped(true);
    FontMetrics fm = testee.getFontMetrics(av.getFont());
    int labelWidth = fm.stringWidth("000") + charWidth;
    assertEquals(labelWidth, 39); // 3 x 9 + charWidth

    /*
     * width 400 pixels leaves (400 - 2*labelWidth) for residue columns
     * take the whole multiple of character widths
     */
    int canvasWidth = 400;
    int canvasHeight = 300;
    int residueColumns = (canvasWidth - 2 * labelWidth) / charWidth;
    int wrappedWidth = testee.calculateWrappedGeometry(canvasWidth,
            canvasHeight);
    assertEquals(wrappedWidth, residueColumns);
    assertEquals(PA.getValue(testee, "labelWidthWest"), labelWidth);
    assertEquals(PA.getValue(testee, "labelWidthEast"), labelWidth);
    assertEquals(PA.getValue(testee, "wrappedSpaceAboveAlignment"),
            2 * charHeight);
    int repeatingHeight = (int) PA.getValue(testee,
            "wrappedRepeatHeightPx");
    assertEquals(repeatingHeight, charHeight * (2 + al.getHeight()));
    assertEquals(PA.getValue(testee, "wrappedVisibleWidths"), 1);

    /*
     * repeat height is 17 * (2 + 15) = 289
     * make canvas height 2 * 289 + 3 * charHeight so just enough to
     * draw 2 widths and the first sequence of a third
     */
    canvasHeight = charHeight * (17 * 2 + 3);
    testee.calculateWrappedGeometry(canvasWidth, canvasHeight);
    assertEquals(PA.getValue(testee, "wrappedVisibleWidths"), 3);

    /*
     * reduce canvas height by 1 pixel 
     * - should not be enough height to draw 3 widths
     */
    canvasHeight -= 1;
    testee.calculateWrappedGeometry(canvasWidth, canvasHeight);
    assertEquals(PA.getValue(testee, "wrappedVisibleWidths"), 2);

    /*
     * turn off scale above - can now fit in 2 and a bit widths
     */
    av.setScaleAboveWrapped(false);
    testee.calculateWrappedGeometry(canvasWidth, canvasHeight);
    assertEquals(PA.getValue(testee, "wrappedVisibleWidths"), 3);

    /*
     * reduce height to enough for 2 widths and not quite a third
     * i.e. two repeating heights + spacer + sequence - 1 pixel
     */
    canvasHeight = charHeight * (16 * 2 + 2) - 1;
    testee.calculateWrappedGeometry(canvasWidth, canvasHeight);
    assertEquals(PA.getValue(testee, "wrappedVisibleWidths"), 2);

    /*
     * make canvas width enough for scales and 20 residues
     */
    canvasWidth = 2 * labelWidth + 20 * charWidth;
    wrappedWidth = testee.calculateWrappedGeometry(canvasWidth,
            canvasHeight);
    assertEquals(wrappedWidth, 20);

    /*
     * reduce width by 1 pixel - rounds down to 19 residues
     */
    canvasWidth -= 1;
    wrappedWidth = testee.calculateWrappedGeometry(canvasWidth,
            canvasHeight);
    assertEquals(wrappedWidth, 19);

    /*
     * turn off West scale - adds labelWidth (39) to available for residues
     * which with the 11 remainder makes 50 which is 4 more charWidths rem 2
     */
    av.setScaleLeftWrapped(false);
    wrappedWidth = testee.calculateWrappedGeometry(canvasWidth,
            canvasHeight);
    assertEquals(wrappedWidth, 23);

    /*
     * add 10 pixels to width to fit in another whole residue column
     */
    canvasWidth += 9;
    wrappedWidth = testee.calculateWrappedGeometry(canvasWidth,
            canvasHeight);
    assertEquals(wrappedWidth, 23);
    canvasWidth += 1;
    wrappedWidth = testee.calculateWrappedGeometry(canvasWidth,
            canvasHeight);
    assertEquals(wrappedWidth, 24);

    /*
     * turn off East scale to gain 39 more pixels (3 columns remainder 3)
     */
    av.setScaleRightWrapped(false);
    wrappedWidth = testee.calculateWrappedGeometry(canvasWidth,
            canvasHeight);
    assertEquals(wrappedWidth, 27);

    /*
     * add 9 pixels to width to gain a residue column
     */
    canvasWidth += 8;
    wrappedWidth = testee.calculateWrappedGeometry(canvasWidth,
            canvasHeight);
    assertEquals(wrappedWidth, 27); // 8px not enough
    canvasWidth += 1;
    wrappedWidth = testee.calculateWrappedGeometry(canvasWidth,
            canvasHeight);
    assertEquals(wrappedWidth, 28); // 9px is enough

    /*
     * now West but not East scale - lose 39 pixels or 4 columns
     */
    av.setScaleLeftWrapped(true);
    wrappedWidth = testee.calculateWrappedGeometry(canvasWidth,
            canvasHeight);
    assertEquals(wrappedWidth, 24);

    /*
     * adding 3 pixels to width regains one column
     */
    canvasWidth += 2;
    wrappedWidth = testee.calculateWrappedGeometry(canvasWidth,
            canvasHeight);
    assertEquals(wrappedWidth, 24); // 2px not enough
    canvasWidth += 1;
    wrappedWidth = testee.calculateWrappedGeometry(canvasWidth,
            canvasHeight);
    assertEquals(wrappedWidth, 25); // 3px is enough

    /*
     * turn off scales left and right, make width exactly 157 columns
     */
    av.setScaleLeftWrapped(false);
    canvasWidth = al.getWidth() * charWidth;
    testee.calculateWrappedGeometry(canvasWidth, canvasHeight);
    assertEquals(PA.getValue(testee, "wrappedVisibleWidths"), 1);
  }

  /**
   * Test the method that computes wrapped width in residues, height of wrapped
   * widths in pixels, and the number of widths visible
   */
  @Test(groups = "Functional")
  public void testCalculateWrappedGeometry_withAnnotations()
  {
    AlignViewport av = af.getViewport();
    AlignmentI al = av.getAlignment();
    assertEquals(al.getWidth(), 157);
    assertEquals(al.getHeight(), 15);

    av.setWrapAlignment(true);
    av.getRanges().setStartEndSeq(0, 14);
    av.setFont(new Font("SansSerif", Font.PLAIN, 14), true);
    int charHeight = av.getCharHeight();
    int charWidth = av.getCharWidth();
    assertEquals(charHeight, 17);
    assertEquals(charWidth, 12);

    SeqCanvas testee = af.alignPanel.getSeqPanel().seqCanvas;

    /*
     * first with scales above, left, right
     */
    av.setShowAnnotation(true);
    av.setScaleAboveWrapped(true);
    av.setScaleLeftWrapped(true);
    av.setScaleRightWrapped(true);
    FontMetrics fm = testee.getFontMetrics(av.getFont());
    int labelWidth = fm.stringWidth("000") + charWidth;
    assertEquals(labelWidth, 39); // 3 x 9 + charWidth
    int annotationHeight = testee.getAnnotationHeight();

    /*
     * width 400 pixels leaves (400 - 2*labelWidth) for residue columns
     * take the whole multiple of character widths
     */
    int canvasWidth = 400;
    int canvasHeight = 300;
    int residueColumns = (canvasWidth - 2 * labelWidth) / charWidth;
    int wrappedWidth = testee.calculateWrappedGeometry(canvasWidth,
            canvasHeight);
    assertEquals(wrappedWidth, residueColumns);
    assertEquals(PA.getValue(testee, "labelWidthWest"), labelWidth);
    assertEquals(PA.getValue(testee, "labelWidthEast"), labelWidth);
    assertEquals(PA.getValue(testee, "wrappedSpaceAboveAlignment"),
            2 * charHeight);
    int repeatingHeight = (int) PA.getValue(testee,
            "wrappedRepeatHeightPx");
    assertEquals(repeatingHeight, charHeight * (2 + al.getHeight())
            + SeqCanvas.SEQS_ANNOTATION_GAP + annotationHeight);
    assertEquals(PA.getValue(testee, "wrappedVisibleWidths"), 1);

    /*
     * repeat height is 17 * (2 + 15) = 289 + 3 + annotationHeight = 510
     * make canvas height 2 of these plus 3 charHeights 
     * so just enough to draw 2 widths, gap + scale + the first sequence of a third
     */
    canvasHeight = charHeight * (17 * 2 + 3)
            + 2 * (annotationHeight + SeqCanvas.SEQS_ANNOTATION_GAP);
    testee.calculateWrappedGeometry(canvasWidth, canvasHeight);
    assertEquals(PA.getValue(testee, "wrappedVisibleWidths"), 3);

    /*
     * reduce canvas height by 1 pixel - should not be enough height
     * to draw 3 widths
     */
    canvasHeight -= 1;
    testee.calculateWrappedGeometry(canvasWidth, canvasHeight);
    assertEquals(PA.getValue(testee, "wrappedVisibleWidths"), 2);

    /*
     * turn off scale above - can now fit in 2 and a bit widths
     */
    av.setScaleAboveWrapped(false);
    testee.calculateWrappedGeometry(canvasWidth, canvasHeight);
    assertEquals(PA.getValue(testee, "wrappedVisibleWidths"), 3);

    /*
     * reduce height to enough for 2 widths and not quite a third
     * i.e. two repeating heights + spacer + sequence - 1 pixel
     */
    canvasHeight = charHeight * (16 * 2 + 2)
            + 2 * (annotationHeight + SeqCanvas.SEQS_ANNOTATION_GAP) - 1;
    testee.calculateWrappedGeometry(canvasWidth, canvasHeight);
    assertEquals(PA.getValue(testee, "wrappedVisibleWidths"), 2);

    /*
     * add 1 pixel to height - should now get 3 widths drawn
     */
    canvasHeight += 1;
    testee.calculateWrappedGeometry(canvasWidth, canvasHeight);
    assertEquals(PA.getValue(testee, "wrappedVisibleWidths"), 3);
  }

  /**
   * Test simulates loading an unwrapped alignment, shrinking it vertically so
   * not all sequences are visible, then changing to wrapped mode. The ranges
   * endSeq should be unchanged, but the vertical repeat height should include
   * all sequences.
   */
  @Test(groups = "Functional_Failing")
  public void testCalculateWrappedGeometry_fromScrolled()
  {
    AlignViewport av = af.getViewport();
    AlignmentI al = av.getAlignment();
    assertEquals(al.getWidth(), 157);
    assertEquals(al.getHeight(), 15);
    av.getRanges().setStartEndSeq(0, 3);
    av.setFont(new Font("SansSerif", Font.PLAIN, 14), true);
    av.setWrapAlignment(true);
    av.setShowAnnotation(false);
    av.setScaleAboveWrapped(true);

    SeqCanvas testee = af.alignPanel.getSeqPanel().seqCanvas;

    int charHeight = av.getCharHeight();
    int charWidth = av.getCharWidth();
    assertEquals(charHeight, 17);
    assertEquals(charWidth, 12);

    int canvasWidth = 400;
    int canvasHeight = 300;
    testee.calculateWrappedGeometry(canvasWidth, canvasHeight);

    assertEquals(av.getRanges().getEndSeq(), 3); // unchanged
    int repeatingHeight = (int) PA.getValue(testee,
            "wrappedRepeatHeightPx");
    assertEquals(repeatingHeight, charHeight * (2 + al.getHeight()));
  }

  @BeforeMethod(alwaysRun = true)
  public void setUp()
  {
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    Cache.applicationProperties.setProperty("SHOW_IDENTITY",
            Boolean.TRUE.toString());
    af = new FileLoader().LoadFileWaitTillLoaded("examples/uniref50.fa",
            DataSourceType.FILE);

    /*
     * wait for Consensus thread to complete
     */
    do
    {
      try
      {
        Thread.sleep(50);
      } catch (InterruptedException x)
      {
      }
    } while (af.getViewport().getCalcManager().isWorking());
  }

  @Test(groups = "Functional")
  public void testClear_HighlightAndSelection()
  {
    AlignViewport av = af.getViewport();
    SearchResultsI highlight = new SearchResults();
    highlight.addResult(
            av.getAlignment().getSequenceAt(1).getDatasetSequence(), 50,
            80);
    af.alignPanel.highlightSearchResults(highlight);
    af.avc.markHighlightedColumns(false, false, false);
    assertNotNull(av.getSearchResults(),
            "No highlight was created on alignment");
    assertFalse(av.getColumnSelection().isEmpty(),
            "No selection was created from highlight");
    af.deselectAllSequenceMenuItem_actionPerformed(null);
    assertTrue(av.getColumnSelection().isEmpty(),
            "No Selection should be present after deselecting all.");
    assertNull(av.getSearchResults(),
            "No higlighted search results should be present after deselecting all.");
  }
}
