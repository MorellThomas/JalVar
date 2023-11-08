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
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jalview.api.AlignViewportI;
import jalview.bin.Cache;
import jalview.bin.Jalview;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.SequenceI;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;
import jalview.viewmodel.ViewportRanges;

public class AlignmentPanelTest
{
  AlignFrame af;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws InvocationTargetException, InterruptedException
  {
    Jalview.main(
            new String[]
            { "-nonews", "-props", "test/jalview/testProps.jvprops" });

    Cache.applicationProperties.setProperty("SHOW_IDENTITY",
            Boolean.TRUE.toString());
    af = new FileLoader().LoadFileWaitTillLoaded("examples/uniref50.fa",
            DataSourceType.FILE);

    /*
     * ensure the panel has been repainted and so ViewportRanges set
     */
    SwingUtilities.invokeAndWait(new Runnable()
    {
      @Override
      public void run()
      {
        af.repaint();
      }
    });

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

  /**
   * Test side effect that end residue is set correctly by setScrollValues, with
   * or without hidden columns
   */
  @Test(groups = "Functional")
  public void testSetScrollValues()
  {
    ViewportRanges ranges = af.getViewport().getRanges();
    af.alignPanel.setScrollValues(0, 0);

    int oldres = ranges.getEndRes();
    af.alignPanel.setScrollValues(-1, 5);

    // setting -ve x value does not change residue
    assertEquals(ranges.getEndRes(), oldres);

    af.alignPanel.setScrollValues(0, 5);

    // setting 0 as x value does not change residue
    assertEquals(ranges.getEndRes(), oldres);

    af.alignPanel.setScrollValues(5, 5);
    // setting x value to 5 extends endRes by 5 residues
    assertEquals(ranges.getEndRes(), oldres + 5);

    // scroll to position after hidden columns sets endres to oldres (width) +
    // position
    int scrollpos = 60;
    af.getViewport().hideColumns(30, 50);
    af.alignPanel.setScrollValues(scrollpos, 5);
    assertEquals(ranges.getEndRes(), oldres + scrollpos);

    // scroll to position within hidden columns, still sets endres to oldres +
    // position
    // not sure if this is actually correct behaviour but this is what Jalview
    // currently does
    scrollpos = 40;
    af.getViewport().showAllHiddenColumns();
    af.getViewport().hideColumns(30, 50);
    af.alignPanel.setScrollValues(scrollpos, 5);
    assertEquals(ranges.getEndRes(), oldres + scrollpos);

    // scroll to position within <width> distance of the end of the alignment
    // endRes should be set to width of alignment - 1
    scrollpos = 130;
    af.getViewport().showAllHiddenColumns();
    af.alignPanel.setScrollValues(scrollpos, 5);
    assertEquals(ranges.getEndRes(),
            af.getViewport().getAlignment().getWidth() - 1);

    // now hide some columns, and scroll to position within <width>
    // distance of the end of the alignment
    // endRes should be set to width of alignment - 1 - the number of hidden
    // columns
    af.getViewport().hideColumns(30, 50);
    af.alignPanel.setScrollValues(scrollpos, 5);
    assertEquals(ranges.getEndRes(),
            af.getViewport().getAlignment().getWidth() - 1 - 21); // 21 is the
                                                                  // number of
                                                                  // hidden
                                                                  // columns
  }

  /**
   * Test that update layout reverts to original (unwrapped) values for endRes
   * when switching from wrapped back to unwrapped mode (JAL-2739)
   */
  @Test(groups = "Functional")
  public void testUpdateLayout_endRes()
  {
    // get details of original alignment dimensions
    ViewportRanges ranges = af.getViewport().getRanges();
    int endres = ranges.getEndRes();

    // wrap
    af.alignPanel.getAlignViewport().setWrapAlignment(true);
    af.alignPanel.updateLayout();

    // endRes has changed
    assertNotEquals(ranges.getEndRes(), endres);

    // unwrap
    af.alignPanel.getAlignViewport().setWrapAlignment(false);
    af.alignPanel.updateLayout();

    // endRes back to original value
    assertEquals(ranges.getEndRes(), endres);
  }

  /**
   * Test the variant of calculateIdWidth that only recomputes the width if it
   * is not already saved in the viewport (initial value is -1)
   */
  @Test(groups = "Functional")
  public void testCalculateIdWidth_noArgs()
  {
    AlignViewportI av = af.alignPanel.getAlignViewport();
    av.setShowJVSuffix(true);
    av.setFont(new Font("Courier", Font.PLAIN, 15), true);

    av.setIdWidth(0);
    Dimension d = af.alignPanel.calculateIdWidth();
    assertEquals(d.width, 0);
    assertEquals(d.height, 0);

    av.setIdWidth(99);
    d = af.alignPanel.calculateIdWidth();
    assertEquals(d.width, 99);
    assertEquals(d.height, 0);

    /*
     * note 4 pixels padding are added to the longest sequence name width
     */
    av.setIdWidth(-1); // force recalculation
    d = af.alignPanel.calculateIdWidth();
    assertEquals(d.width, 166); // 4 + pixel width of "Q93Z60_ARATH/1-118"
    assertEquals(d.height, 12);
    assertEquals(d.width, av.getIdWidth());
  }

  /**
   * Test the variant of calculateIdWidth that computes the longest of any
   * sequence name or annotation label width
   */
  @Test(groups = "Functional")
  public void testCalculateIdWidth_withMaxWidth()
  {
    AlignViewportI av = af.alignPanel.getAlignViewport();
    av.setShowJVSuffix(true);
    av.setFont(new Font("Courier", Font.PLAIN, 15), true);
    av.setShowAnnotation(false);
    av.setIdWidth(18);

    /*
     * note 4 pixels 'padding' are added to the longest seq name/annotation label
     */
    Dimension d = af.alignPanel.calculateIdWidth(2000);
    assertEquals(d.width, 166); // 4 + pixel width of "Q93Z60_ARATH/1-118"
    assertEquals(d.height, 12); // fixed value (not used?)
    assertEquals(av.getIdWidth(), 18); // not changed by this method

    /*
     * make the longest sequence name longer
     */
    SequenceI seq = af.viewport.getAlignment()
            .findSequenceMatch("Q93Z60_ARATH")[0];
    seq.setName(seq.getName() + "MMMMM");
    d = af.alignPanel.calculateIdWidth(2000);
    assertEquals(d.width, 211); // 4 + pixel width of "Q93Z60_ARATHMMMMM/1-118"
    assertEquals(d.height, 12);
    assertEquals(av.getIdWidth(), 18); // unchanged

    /*
     * make the longest annotation name even longer
     * note this is checked even if annotations are not shown
     */
    AlignmentAnnotation aa = av.getAlignment().getAlignmentAnnotation()[0];
    aa.label = "THIS IS A VERY LONG LABEL INDEED";
    FontMetrics fmfor = af.alignPanel
            .getFontMetrics(af.alignPanel.getAlabels().getFont());
    // Assumption ID_WIDTH_PADDING == 4
    int expwidth = 4 + fmfor.stringWidth(aa.label);
    d = af.alignPanel.calculateIdWidth(2000);
    assertEquals(d.width, expwidth); // 228 == ID_WIDTH_PADDING + pixel width of
                                     // "THIS IS A VERY LONG LABEL INDEED"
    assertEquals(d.height, 12);

    /*
     * override with maxwidth
     * note the 4 pixels padding is added to this value
     */
    d = af.alignPanel.calculateIdWidth(213);
    assertEquals(d.width, 217);
    assertEquals(d.height, 12);
  }

  @Test(groups = { "Functional", "Not-bamboo" })
  public void testGetVisibleWidth()
  {
    /*
     * width for onscreen rendering is IDPanel width
     */
    int w = af.alignPanel.getVisibleIdWidth(true);
    assertEquals(w, af.alignPanel.getIdPanel().getWidth());
    assertEquals(w, 115);

    /*
     * width for offscreen rendering is the same
     * if no fixed id width is specified in preferences
     */
    Cache.setProperty("FIGURE_AUTOIDWIDTH", Boolean.FALSE.toString());
    Cache.removeProperty("FIGURE_FIXEDIDWIDTH");
    assertEquals(w, af.alignPanel.getVisibleIdWidth(false));

    /*
     * preference for fixed id width - note 4 pixels padding is added
     */
    Cache.setProperty("FIGURE_FIXEDIDWIDTH", "120");
    assertEquals(124, af.alignPanel.getVisibleIdWidth(false));

    /*
     * preference for auto id width overrides fixed width
     */
    Cache.setProperty("FIGURE_AUTOIDWIDTH", Boolean.TRUE.toString());
    assertEquals(115, af.alignPanel.getVisibleIdWidth(false));
  }

  @Test(groups = "Functional")
  public void testSetOverviewTitle()
  {
    OverviewPanel ov1 = this.af.openOverviewPanel(true);
    String alignFrameTitle = af.getTitle();
    assertEquals(ov1.getTitle(), "Overview " + alignFrameTitle);

    /*
     * on New View, existing overview should get " Original" added to title
     * and new view's overview should get " View 1" added
     */
    af.newView_actionPerformed(null);
    assertEquals(ov1.getTitle(),
            "Overview " + alignFrameTitle + " Original");
    OverviewPanel ov2 = this.af.openOverviewPanel(true);
    assertEquals(ov2.getTitle(), "Overview " + alignFrameTitle + " View 1");
  }

  @Test(groups = "Functional")
  public void testSetOverviewTitle_automaticOverview()
  {
    Cache.setProperty("SHOW_OVERVIEW", "true");
    AlignFrame alignFrame = new FileLoader().LoadFileWaitTillLoaded(
            "examples/uniref50.fa", DataSourceType.FILE);
    OverviewPanel ov1 = alignFrame.alignPanel.getOverviewPanel();
    assertNotNull(ov1);
    String alignFrameTitle = alignFrame.getTitle();
    assertEquals(ov1.getTitle(), "Overview " + alignFrameTitle);

    /*
     * on New View, existing overview should get " Original" added to title
     * and new view's automatic overview should have " View 1" added
     */
    alignFrame.newView_actionPerformed(null);
    assertEquals(ov1.getTitle(),
            "Overview " + alignFrameTitle + " Original");
    OverviewPanel ov2 = alignFrame.alignPanel.getOverviewPanel();
    assertNotNull(ov2);
    assertEquals(ov2.getTitle(), "Overview " + alignFrameTitle + " View 1");
  }
}