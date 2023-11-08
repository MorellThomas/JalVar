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
package jalview.controller;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.awt.Color;
import java.util.Arrays;
import java.util.BitSet;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.analysis.Finder;
import jalview.api.AlignViewControllerI;
import jalview.api.FeatureColourI;
import jalview.api.FinderI;
import jalview.datamodel.Alignment;
import jalview.datamodel.SearchResults;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.JvOptionPane;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;
import jalview.schemes.FeatureColour;

public class AlignViewControllerTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = "Functional")
  public void testFindColumnsWithFeature()
  {
    SequenceI seq1 = new Sequence("seq1", "-a-MMMaaaaaaaaaaaaaaaa");
    SequenceI seq2 = new Sequence("seq2", "aa--aMM-MMMMMaaaaaaaaaa");
    SequenceI seq3 = new Sequence("seq3", "abcab-caD-aaMMMMMaaaaa");
    SequenceI seq4 = new Sequence("seq4", "abc--abcaaaaaaaaaaaaaa");

    /*
     * features start/end are base 1
     */
    seq1.addSequenceFeature(
            new SequenceFeature("Metal", "desc", 2, 4, 0f, null));
    seq1.addSequenceFeature(
            new SequenceFeature("Helix", "desc", 1, 15, 0f, null));
    seq2.addSequenceFeature(
            new SequenceFeature("Metal", "desc", 4, 10, 10f, null));
    seq3.addSequenceFeature(
            new SequenceFeature("Metal", "desc", 11, 15, 10f, null));
    // disulfide bond is a 'contact feature' - only select its 'start' and 'end'
    seq3.addSequenceFeature(
            new SequenceFeature("disulfide bond", "desc", 8, 12, 0f, null));

    /*
     * select the first five columns --> Metal in seq1 cols 4-5
     */
    SequenceGroup sg = new SequenceGroup();
    sg.setStartRes(0); // base 0
    sg.setEndRes(4);
    sg.addSequence(seq1, false);
    sg.addSequence(seq2, false);
    sg.addSequence(seq3, false);
    sg.addSequence(seq4, false);

    /*
     * set features visible on a viewport as only visible features are selected
     */
    AlignFrame af = new AlignFrame(
            new Alignment(new SequenceI[]
            { seq1, seq2, seq3, seq4 }), 100, 100);
    af.getFeatureRenderer().findAllFeatures(true);

    AlignViewController avc = new AlignViewController(af, af.getViewport(),
            af.alignPanel);

    BitSet bs = new BitSet();
    int seqCount = avc.findColumnsWithFeature("Metal", sg, bs);
    assertEquals(1, seqCount);
    assertEquals(2, bs.cardinality());
    assertTrue(bs.get(3)); // base 0
    assertTrue(bs.get(4));

    /*
     * select the first seven columns: Metal in seq1 cols 4-6, seq2 cols 6-7 
     */
    sg.setEndRes(6);
    bs.clear();
    seqCount = avc.findColumnsWithFeature("Metal", sg, bs);
    assertEquals(2, seqCount);
    assertEquals(4, bs.cardinality());
    assertTrue(bs.get(3));
    assertTrue(bs.get(4));
    assertTrue(bs.get(5));
    assertTrue(bs.get(6));

    /*
     * select column 14: Metal in seq3 only
     */
    sg.setStartRes(13);
    sg.setEndRes(13);
    bs.clear();
    seqCount = avc.findColumnsWithFeature("Metal", sg, bs);
    assertEquals(1, seqCount);
    assertEquals(1, bs.cardinality());
    assertTrue(bs.get(13));

    /*
     * select columns 18-20: no Metal feature
     */
    sg.setStartRes(17);
    sg.setEndRes(19);
    bs.clear();
    seqCount = avc.findColumnsWithFeature("Metal", sg, bs);
    assertEquals(0, seqCount);
    assertEquals(0, bs.cardinality());

    /*
     * threshold Metal to hide where score < 5
     * seq1 feature in columns 4-6 is hidden
     * seq2 feature in columns 6-7 is shown
     */
    FeatureColourI fc = new FeatureColour(null, Color.red, Color.blue, null,
            0f, 10f);
    fc.setAboveThreshold(true);
    fc.setThreshold(5f);
    af.getFeatureRenderer().setColour("Metal", fc);
    sg.setStartRes(0);
    sg.setEndRes(6);
    bs.clear();
    seqCount = avc.findColumnsWithFeature("Metal", sg, bs);
    assertEquals(1, seqCount);
    assertEquals(2, bs.cardinality());
    assertTrue(bs.get(5));
    assertTrue(bs.get(6));

    /*
     * columns 11-13 should not match disulfide bond at 8/12
     */
    sg.setStartRes(10);
    sg.setEndRes(12);
    bs.clear();
    seqCount = avc.findColumnsWithFeature("disulfide bond", sg, bs);
    assertEquals(0, seqCount);
    assertEquals(0, bs.cardinality());

    /*
     * columns 6-18 should match disulfide bond at columns 9, 14
     */
    sg.setStartRes(5);
    sg.setEndRes(17);
    bs.clear();
    seqCount = avc.findColumnsWithFeature("disulfide bond", sg, bs);
    assertEquals(1, seqCount);
    assertEquals(2, bs.cardinality());
    assertTrue(bs.get(8));
    assertTrue(bs.get(13));

    /*
     * look for a feature that isn't there
     */
    sg.setStartRes(0);
    sg.setEndRes(19);
    bs.clear();
    seqCount = avc.findColumnsWithFeature("Pfam", sg, bs);
    assertEquals(0, seqCount);
    assertEquals(0, bs.cardinality());
  }

  /**
   * shameless copy of test data from findFeature for testing mark columns from
   * highlight
   */
  @Test(groups = "Functional")
  public void testSelectColumnsWithHighlight()
  {
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            "seq1 aMMMaaaaaaaaaaaaaaaa\n" + "seq2 aaaMMMMMMMaaaaaaaaaa\n"
                    + "seq3 aaaaaaaaaaMMMMMaaaaa\n"
                    + "seq4 aaaaaaaaaaaaaaaaaaaa\n",
            DataSourceType.PASTE);

    SearchResultsI sr = new SearchResults();
    SequenceI[] sqs = af.getViewport().getAlignment().getSequencesArray();
    SequenceI seq1 = sqs[0];
    SequenceI seq2 = sqs[1];
    SequenceI seq3 = sqs[2];
    SequenceI seq4 = sqs[3];

    /*
     * features start/end are base 1
     */
    sr.addResult(seq1, 2, 4);
    sr.addResult(seq2, 4, 10);
    sr.addResult(seq3, 11, 15);

    /*
     *  test Match/Find works first
     */
    FinderI f = new Finder(af.getViewport());
    f.findAll("M+", true, false, false, false);
    assertEquals(
            "Finder found different set of results to manually created SearchResults",
            sr, f.getSearchResults());

    /*
     * now check simple mark columns from find operation
     */
    af.getViewport().setSearchResults(sr);
    AlignViewControllerI avc = af.avc;

    avc.markHighlightedColumns(false, false, false);
    assertTrue("Didn't select highlighted columns",
            Arrays.deepEquals(af.getViewport().getColumnSelection()
                    .getSelectedRanges().toArray(), new int[][]
                    { { 1, 14 } }));
  }
}
