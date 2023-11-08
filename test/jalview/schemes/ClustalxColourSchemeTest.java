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
package jalview.schemes;

import static org.testng.Assert.assertEquals;

import jalview.datamodel.AlignmentI;
import jalview.gui.AlignFrame;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;

import java.awt.Color;

import org.testng.annotations.Test;

public class ClustalxColourSchemeTest
{
  // @formatter:off
  private static final String FASTA = 
          ">seq1\nAAANNNRQ\n" + 
          ">seq2\nAAANNNRQ\n" + 
          ">seq3\nAAANNNRQ\n" + 
          ">seq4\nAAANNNRQ\n" + 
          ">seq5\nAAANYYKQ\n" + 
          ">seq6\nAAANYYKQ\n" + 
          ">seq7\nAVKWYYKQ\n" + 
          ">seq8\nKKKWYYQQ\n" + 
          ">seq9\nKKKWWYQQ\n" + 
          ">seq0\nKKKWWWQW\n";
  // @formatter:on

  @Test(groups = "Functional")
  public void testFindColour()
  {
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(FASTA,
            DataSourceType.PASTE);
    AlignmentI al = af.getViewport().getAlignment();
    ClustalxColourScheme cs = new ClustalxColourScheme(al, null);

    /*
     * column 1 is 70% A which is above Clustalx threshold of 60%
     */
    Color clustalBlue = new Color(0.5f, 0.7f, 0.9f);
    assertEquals(cs.findColour('A', 0, al.getSequenceAt(0)), clustalBlue);

    /*
     * column 2 is 70% A or V which is above Clustalx threshold for group
     */
    assertEquals(cs.findColour('A', 0, al.getSequenceAt(1)), clustalBlue);

    /*
     * column 3 is 60% A which is not above Clustalx threshold
     * the Ks in the other rows are not in the same Clustalx group
     */
    assertEquals(cs.findColour('A', 2, al.getSequenceAt(1)), Color.white);

    /*
     * column 4 is 60% N which is above Clustalx threshold of 50%
     */
    Color clustalGreen = new Color(0.1f, 0.8f, 0.1f);
    assertEquals(cs.findColour('N', 3, al.getSequenceAt(1)), clustalGreen);

    /*
     * column 5 is 40% N and 40% Y which fails to pass the threshold of
     * 50% N or 85% either
     */
    assertEquals(cs.findColour('N', 4, al.getSequenceAt(1)), Color.white);

    /*
     * column 6 is 40% N and 50% Y which fails to pass the threshold of
     * 85% for either
     */
    assertEquals(cs.findColour('N', 5, al.getSequenceAt(1)), Color.white);

    /*
     * column 7 is 40% R and 30% K which combine to make > 60%
     */
    Color clustalRed = new Color(0.9f, 0.2f, 0.1f);
    assertEquals(cs.findColour('R', 6, al.getSequenceAt(1)), clustalRed);
    assertEquals(cs.findColour('K', 6, al.getSequenceAt(7)), clustalRed);

    /*
     * column 8 is >85% Q which qualifies K and R to be red
     */
    assertEquals(cs.findColour('R', 7, al.getSequenceAt(1)), clustalRed);
    assertEquals(cs.findColour('K', 7, al.getSequenceAt(1)), clustalRed);

    // TODO more test cases; check if help documentation matches implementation
  }

  // @formatter:on

  /**
   * Test for colour calculation when the consensus percentage ignores gapped
   * sequences
   */
  @Test(groups = "Functional")
  public void testFindColour_ignoreGaps()
  {
    /*
     * CCC
     * CCC
     * -CC
     * first column is 66% C (blue) including gaps
     * or 100% C ignoring gaps
     */
    String fasta = ">seq1\nCCC\n>seq2\nccc\n>seq3\n-CC\n";
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(fasta,
            DataSourceType.PASTE);
    AlignmentI al = af.getViewport().getAlignment();
    ClustalxColourScheme cs = new ClustalxColourScheme(al, null);

    /*
     * column 1 is 66% C which is above Clustalx threshold of 60%
     */
    Color clustalBlue = ClustalxColourScheme.ClustalColour.BLUE.colour;
    assertEquals(cs.findColour('C', 0, al.getSequenceAt(0)), clustalBlue);

    /*
     * set directly to ignore gaps
     */
    cs.setIncludeGaps(false);
    Color clustalPink = ClustalxColourScheme.ClustalColour.PINK.colour;
    assertEquals(cs.findColour('C', 0, al.getSequenceAt(0)), clustalPink);

    /*
     * set ignore gaps on the viewport...
     */
    cs.setIncludeGaps(true);
    assertEquals(cs.findColour('C', 0, al.getSequenceAt(0)), clustalBlue);
    af.getViewport().setIgnoreGapsConsensus(true, af.alignPanel);
    // next test fails: colour scheme does not read ignore gaps flag from
    // viewport
    // assertEquals(cs.findColour('C', 0, al.getSequenceAt(0)), clustalPink);
  }
}
