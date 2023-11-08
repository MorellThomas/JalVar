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
import static org.testng.Assert.assertTrue;

import jalview.bin.Cache;
import jalview.bin.Jalview;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;
import jalview.util.MessageManager;
import jalview.viewmodel.ViewportRanges;

import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ScalePanelTest
{
  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = "Functional")
  public void testPreventNegativeStartColumn()
  {
    SequenceI seq1 = new Sequence("Seq1", "MATRESS");
    SequenceI seq2 = new Sequence("Seq2", "MADNESS");
    AlignmentI al = new Alignment(new SequenceI[] { seq1, seq2 });

    AlignFrame alignFrame = new AlignFrame(al, al.getWidth(),
            al.getHeight());
    ScalePanel scalePanel = alignFrame.alignPanel.getScalePanel();

    MouseEvent mouse = new MouseEvent(scalePanel, 0, 1, 0, 4, 0, 1, false);
    scalePanel.mousePressed(mouse);
    scalePanel.mouseDragged(mouse);

    // simulate dragging selection leftwards beyond the sequences giving
    // negative X
    mouse = new MouseEvent(scalePanel, 0, 1, 0, -30, 0, 1, false);

    scalePanel.mouseReleased(mouse);

    SequenceGroup sg = scalePanel.av.getSelectionGroup();
    int startCol = sg.getStartRes();

    assertTrue(startCol >= 0);
  }

  /**
   * Test for JAL-3212
   */
  @Test(groups = "Functional")
  public void testSelectColumns_withHidden()
  {
    String seq1 = ">Seq1\nANTOFAGASTAVALPARAISOMONTEVIDEOANTANANARIVO";
    AlignFrame alignFrame = new FileLoader().LoadFileWaitTillLoaded(seq1,
            DataSourceType.PASTE);
    ScalePanel scalePanel = alignFrame.alignPanel.getScalePanel();

    /*
     * hide columns 1-20 (of 43); then 'drag' to select columns 30-31;
     * 31 is 51 in absolute columns but bug JAL-3212 reduces it to
     * endRes which is 22
     */
    AlignViewport viewport = alignFrame.getViewport();
    ViewportRanges ranges = viewport.getRanges();
    assertEquals(ranges.getStartRes(), 0);
    assertEquals(ranges.getEndRes(), 42);
    viewport.hideColumns(0, 19);
    alignFrame.alignPanel.updateLayout();
    assertEquals(ranges.getStartRes(), 0);
    assertEquals(ranges.getEndRes(), 22);

    int cw = viewport.getCharWidth();
    int xPos = 9 * cw + 2;
    MouseEvent mouse = new MouseEvent(scalePanel, 0, 1, 0, xPos, 0, 1,
            false);
    scalePanel.mousePressed(mouse);
    scalePanel.mouseDragged(mouse);
    xPos += cw;
    mouse = new MouseEvent(scalePanel, 0, 1, 0, xPos, 0, 1, false);
    scalePanel.mouseReleased(mouse);

    SequenceGroup sg = scalePanel.av.getSelectionGroup();
    assertEquals(sg.getStartRes(), 29);
    assertEquals(sg.getEndRes(), 30);
  }

  @Test(groups = "Functional")
  public void testBuildPopupMenu()
  {
    final String hide = MessageManager.getString("label.hide_columns");
    final String reveal = MessageManager.getString("label.reveal");
    final String revealAll = MessageManager.getString("action.reveal_all");

    String seq1 = ">Seq1\nANTOFAGASTA";
    AlignFrame alignFrame = new FileLoader().LoadFileWaitTillLoaded(seq1,
            DataSourceType.PASTE);
    ScalePanel scalePanel = alignFrame.alignPanel.getScalePanel();
    AlignViewport viewport = alignFrame.getViewport();
    int cw = viewport.getCharWidth();

    /*
     * hide columns 3-4 (counting from 0)
     */
    viewport.hideColumns(3, 4);
    alignFrame.alignPanel.updateLayout();

    /*
     * verify popup menu left/right of hidden column marker
     * NB need to call mouseMoved first as this sets field 'reveal'
     */
    int xPos = 1 * cw + 2; // 2 columns left of hidden marker
    MouseEvent mouse = new MouseEvent(scalePanel, 0, 1, 0, xPos, 0, 1,
            false);
    scalePanel.mouseMoved(mouse);
    JPopupMenu popup = scalePanel.buildPopupMenu(1);
    assertEquals(popup.getSubElements().length, 0);

    /*
     * popup just left of hidden marker has 'Reveal'
     */
    xPos = 2 * cw + 2;
    mouse = new MouseEvent(scalePanel, 0, 1, 0, xPos, 0, 1, false);
    scalePanel.mouseMoved(mouse);
    popup = scalePanel.buildPopupMenu(2);
    assertEquals(popup.getSubElements().length, 1);
    assertEquals(((JMenuItem) popup.getSubElements()[0]).getText(), reveal);

    /*
     * popup just right of hidden marker has 'Reveal'
     */
    xPos = 3 * cw + 2;
    mouse = new MouseEvent(scalePanel, 0, 1, 0, xPos, 0, 1, false);
    scalePanel.mouseMoved(mouse);
    popup = scalePanel.buildPopupMenu(5); // allowing for 2 hidden columns
    assertEquals(popup.getSubElements().length, 1);
    assertEquals(((JMenuItem) popup.getSubElements()[0]).getText(), reveal);

    /*
     * popup further right is empty
     */
    xPos = 4 * cw + 2;
    mouse = new MouseEvent(scalePanel, 0, 1, 0, xPos, 0, 1, false);
    scalePanel.mouseMoved(mouse);
    popup = scalePanel.buildPopupMenu(6); // allowing for 2 hidden columns
    assertEquals(popup.getSubElements().length, 0);

    /*
     * 'drag' to select columns around the hidden column marker
     */
    xPos = 1 * cw + 2;
    mouse = new MouseEvent(scalePanel, 0, 1, 0, xPos, 0, 1, false);
    scalePanel.mousePressed(mouse);
    scalePanel.mouseDragged(mouse);
    xPos = 5 * cw + 2;
    mouse = new MouseEvent(scalePanel, 0, 1, 0, xPos, 0, 1, false);
    scalePanel.mouseReleased(mouse);

    /*
     * popup 2 columns left of marker: 'Hide' only
     */
    xPos = 1 * cw + 2;
    mouse = new MouseEvent(scalePanel, 0, 1, 0, xPos, 0, 1, false);
    scalePanel.mouseMoved(mouse);
    popup = scalePanel.buildPopupMenu(1);
    assertEquals(popup.getSubElements().length, 1);
    assertEquals(((JMenuItem) popup.getSubElements()[0]).getText(), hide);

    /*
     * popup just left of marker: 'Reveal' and 'Hide'
     */
    xPos = 2 * cw + 2;
    mouse = new MouseEvent(scalePanel, 0, 1, 0, xPos, 0, 1, false);
    scalePanel.mouseMoved(mouse);
    popup = scalePanel.buildPopupMenu(1);
    assertEquals(popup.getSubElements().length, 2);
    assertEquals(((JMenuItem) popup.getSubElements()[0]).getText(), reveal);
    assertEquals(((JMenuItem) popup.getSubElements()[1]).getText(), hide);

    /*
     * popup just right of marker: 'Reveal' and 'Hide'
     */
    xPos = 3 * cw + 2;
    mouse = new MouseEvent(scalePanel, 0, 1, 0, xPos, 0, 1, false);
    scalePanel.mouseMoved(mouse);
    popup = scalePanel.buildPopupMenu(5);
    assertEquals(popup.getSubElements().length, 2);
    assertEquals(((JMenuItem) popup.getSubElements()[0]).getText(), reveal);
    assertEquals(((JMenuItem) popup.getSubElements()[1]).getText(), hide);

    /*
     * hiding a second region adds option 'Reveal all' to 'Reveal'
     */
    viewport.hideColumns(6, 7);
    alignFrame.alignPanel.updateLayout();
    xPos = 3 * cw + 2;
    mouse = new MouseEvent(scalePanel, 0, 1, 0, xPos, 0, 1, false);
    scalePanel.mouseMoved(mouse);
    popup = scalePanel.buildPopupMenu(5);
    assertEquals(popup.getSubElements().length, 3);
    assertEquals(((JMenuItem) popup.getSubElements()[0]).getText(), reveal);
    assertEquals(((JMenuItem) popup.getSubElements()[1]).getText(),
            revealAll);
    assertEquals(((JMenuItem) popup.getSubElements()[2]).getText(), hide);

    alignFrame.deselectAllSequenceMenuItem_actionPerformed(null);
    popup = scalePanel.buildPopupMenu(5);
    assertEquals(popup.getSubElements().length, 2);
    assertEquals(((JMenuItem) popup.getSubElements()[0]).getText(), reveal);
    assertEquals(((JMenuItem) popup.getSubElements()[1]).getText(),
            revealAll);
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

}
