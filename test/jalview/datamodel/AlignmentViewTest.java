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
package jalview.datamodel;

import static org.testng.Assert.assertEquals;

import jalview.gui.AlignFrame;
import jalview.gui.AlignViewport;
import jalview.gui.JvOptionPane;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AlignmentViewTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testGetVisibleAlignmentGapChar()
  {
    SeqCigar ss = new SeqCigar(new Sequence("One", "A..CDE"));
    CigarArray ca = new CigarArray(new CigarSimple[] { ss });
    AlignmentView av = new AlignmentView(ca);
    String dots = av.getSequenceStrings('.')[0];
    assertEquals(dots, "A..CDE");
    String dollars = av.getSequenceStrings('$')[0];
    assertEquals(dollars, "A$$CDE");
    assertEquals(av.getVisibleAlignment('$').getSequenceAt(0)
            .getSequenceAsString(), "A$$CDE");
  }

  @Test(groups = { "Functional" })
  public void testGetVisibleContigs()
  {
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            ">s1\n0123456789\n", DataSourceType.PASTE);
    AlignViewport av = af.getViewport();
    AlignmentView view = av.getAlignmentView(true);

    /*
     * verify getVisibleContigs returns inclusive [start, end] ranges
     * 
     * no columns hidden
     */
    int[] contigs = view.getVisibleContigs();
    assertEquals(contigs, new int[] { 0, 9 });

    /*
     * hide 3 internal columns
     */
    av.hideColumns(5, 7);
    // the old AlignmentView is now stale!
    contigs = view.getVisibleContigs();
    assertEquals(contigs, new int[] { 0, 9 });
    // get a fresh AlignmentView
    view = av.getAlignmentView(true);
    contigs = view.getVisibleContigs();
    assertEquals(contigs, new int[] { 0, 4, 8, 9 });

    // hide first 2 columns
    av.hideColumns(0, 1);
    view = av.getAlignmentView(true);
    contigs = view.getVisibleContigs();
    assertEquals(contigs, new int[] { 2, 4, 8, 9 });

    // hide last column
    av.hideColumns(9, 9);
    view = av.getAlignmentView(true);
    contigs = view.getVisibleContigs();
    assertEquals(contigs, new int[] { 2, 4, 8, 8 });

    // unhide columns 5-7
    av.showColumn(5);
    view = av.getAlignmentView(true);
    contigs = view.getVisibleContigs();
    assertEquals(contigs, new int[] { 2, 8 });

    // hide columns 2-7
    av.hideColumns(2, 7);
    view = av.getAlignmentView(true);
    contigs = view.getVisibleContigs();
    assertEquals(contigs, new int[] { 8, 8 });

    // hide column 8
    av.hideColumns(8, 8);
    view = av.getAlignmentView(true);
    contigs = view.getVisibleContigs();
    assertEquals(contigs, new int[] {});

    // unhide all
    av.showAllHiddenColumns();
    view = av.getAlignmentView(true);
    contigs = view.getVisibleContigs();
    assertEquals(contigs, new int[] { 0, 9 });
  }
}
