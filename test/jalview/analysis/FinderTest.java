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
package jalview.analysis;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.api.AlignViewportI;
import jalview.api.FinderI;
import jalview.bin.Cache;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.SearchResultMatchI;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.AlignViewport;
import jalview.gui.JvOptionPane;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;
import junit.extensions.PA;

public class FinderTest
{
  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  private AlignFrame af;

  private AlignmentI al;

  private AlignViewportI av;

  @BeforeClass(groups = "Functional")
  public void setUp()
  {
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    Cache.applicationProperties.setProperty("PAD_GAPS",
            Boolean.FALSE.toString());

    //@formatter:off
    String seqData = 
        "seq1/8-18 ABCD--EF-GHIJI\n" + 
        "seq2      A--BCDefHI\n" + 
        "seq3      --bcdEFH\n" + 
        "seq4      aa---aMMMMMaaa\n";
    //@formatter:on
    af = new FileLoader().LoadFileWaitTillLoaded(seqData,
            DataSourceType.PASTE);
    av = af.getViewport();
    al = av.getAlignment();
    al.getSequenceAt(0).addSequenceFeature(
            new SequenceFeature("BBBB", "FeatureB", 9, 11, ""));
    al.getSequenceAt(3).addSequenceFeature(
            new SequenceFeature("BBAB", "FeatureA", 1, 3, ""));
    al.getSequenceAt(3).addSequenceFeature(
            new SequenceFeature("AAAA", "FeatureA", 9, 11, ""));
  }

  @AfterMethod(alwaysRun = true)
  public void tearDownAfterTest()
  {
    av.setSelectionGroup(null);
  }

  /**
   * Test for find matches of a regular expression
   */
  @Test(groups = "Functional")
  public void testFind_regex()
  {
    /*
     * find next match only
     */
    Finder f = new Finder(av);
    f.findNext("E.H", false, false, false, false); // 'E, any character, H'
    // should match seq2 efH only
    SearchResultsI sr = f.getSearchResults();
    assertEquals(sr.getCount(), 1);
    List<SearchResultMatchI> matches = sr.getResults();
    assertSame(matches.get(0).getSequence(), al.getSequenceAt(1));
    assertEquals(matches.get(0).getStart(), 5);
    assertEquals(matches.get(0).getEnd(), 7);

    f = new Finder(av);
    f.findAll("E.H", false, false, false, false); // 'E, any character, H'
    // should match seq2 efH and seq3 EFH
    sr = f.getSearchResults();
    assertEquals(sr.getCount(), 2);
    matches = sr.getResults();
    assertSame(matches.get(0).getSequence(), al.getSequenceAt(1));
    assertSame(matches.get(1).getSequence(), al.getSequenceAt(2));
    assertEquals(matches.get(0).getStart(), 5);
    assertEquals(matches.get(0).getEnd(), 7);
    assertEquals(matches.get(1).getStart(), 4);
    assertEquals(matches.get(1).getEnd(), 6);
  }

  @Test(groups = "Functional")
  public void testFind_findAll()
  {
    /*
     * simple JAL-3765 test
     * single symbol should find *all* matching symbols 
     */
    Finder f = new Finder(av);
    f.findAll("M", false, false, false, false);
    SearchResultsI sr = f.getSearchResults();
    assertEquals(sr.getCount(), 5);

  }

  /**
   * Test for (undocumented) find residue by position
   */
  @Test(groups = "Functional")
  public void testFind_residueNumber()
  {
    Finder f = new Finder(av);

    /*
     * find first match should return seq1 residue 9
     */
    f.findNext("9", false, false, false, false);
    SearchResultsI sr = f.getSearchResults();
    assertEquals(sr.getCount(), 1);
    List<SearchResultMatchI> matches = sr.getResults();
    assertSame(matches.get(0).getSequence(), al.getSequenceAt(0));
    assertEquals(matches.get(0).getStart(), 9);
    assertEquals(matches.get(0).getEnd(), 9);

    /*
     * find all matches should return seq1 and seq4 (others are too short)
     * (and not matches in sequence ids)
     */
    f = new Finder(av);
    String name = al.getSequenceAt(0).getName();
    al.getSequenceAt(0).setName("Q9XA0");
    f.findAll("9", false, false, false, false);
    sr = f.getSearchResults();
    assertEquals(sr.getCount(), 2);
    matches = sr.getResults();
    assertSame(matches.get(0).getSequence(), al.getSequenceAt(0));
    assertSame(matches.get(1).getSequence(), al.getSequenceAt(3));
    assertEquals(matches.get(0).getStart(), 9);
    assertEquals(matches.get(0).getEnd(), 9);
    assertEquals(matches.get(1).getStart(), 9);
    assertEquals(matches.get(1).getEnd(), 9);
    al.getSequenceAt(0).setName(name);

    /*
     * parsing of search string as integer is strict
     */
    f = new Finder(av);
    f.findNext(" 9", false, false, false, false);
    assertTrue(f.getSearchResults().isEmpty());
  }

  /**
   * Test for find next action
   */
  @Test(groups = "Functional")
  public void testFindNext()
  {
    /*
     * start at second sequence; residueIndex of -1
     * means sequence id / description is searched
     */
    Finder f = new Finder(av);
    PA.setValue(f, "sequenceIndex", 1);
    PA.setValue(f, "residueIndex", -1);
    f.findNext("e", false, false, false, false); // matches id

    assertTrue(f.getSearchResults().isEmpty());
    assertEquals(f.getIdMatches().size(), 1);
    assertSame(f.getIdMatches().get(0), al.getSequenceAt(1));

    // residueIndex is now 0 - for use in next find next
    // searching A--BCDefHI
    assertEquals(PA.getValue(f, "residueIndex"), 0);
    f = new Finder(av);
    PA.setValue(f, "sequenceIndex", 1);
    PA.setValue(f, "residueIndex", 0);
    f.findNext("e", false, false, false, false); // matches in sequence
    assertTrue(f.getIdMatches().isEmpty());
    assertEquals(f.getSearchResults().getCount(), 1);
    List<SearchResultMatchI> matches = f.getSearchResults().getResults();
    assertEquals(matches.get(0).getStart(), 5);
    assertEquals(matches.get(0).getEnd(), 5);
    assertSame(matches.get(0).getSequence(), al.getSequenceAt(1));
    // still in the second sequence
    assertEquals(PA.getValue(f, "sequenceIndex"), 1);
    // next residue offset to search from is 5
    assertEquals(PA.getValue(f, "residueIndex"), 5);

    // find next from end of sequence - finds next sequence id
    f = new Finder(av);
    PA.setValue(f, "sequenceIndex", 1);
    PA.setValue(f, "residueIndex", 7);
    f.findNext("e", false, false, false, false);
    assertEquals(f.getIdMatches().size(), 1);
    assertSame(f.getIdMatches().get(0), al.getSequenceAt(2));
    assertTrue(f.getSearchResults().isEmpty());
  }

  /**
   * Test for matching within sequence descriptions
   */
  @Test(groups = "Functional")
  public void testFind_inDescription()
  {
    AlignmentI al2 = new Alignment(al);
    al2.getSequenceAt(0).setDescription("BRAF");
    al2.getSequenceAt(1).setDescription("braf");

    AlignViewportI av2 = new AlignViewport(al2);

    /*
     * find first match only
     */
    Finder f = new Finder(av2);
    f.findNext("rAF", false, true, false, false);
    assertEquals(f.getIdMatches().size(), 1);
    assertSame(f.getIdMatches().get(0), al2.getSequenceAt(0));
    assertTrue(f.getSearchResults().isEmpty());

    /*
     * find all matches
     */
    f = new Finder(av2);
    f.findAll("rAF", false, true, false, false);
    assertEquals(f.getIdMatches().size(), 2);
    assertSame(f.getIdMatches().get(0), al2.getSequenceAt(0));
    assertSame(f.getIdMatches().get(1), al2.getSequenceAt(1));
    assertTrue(f.getSearchResults().isEmpty());

    /*
     * case sensitive
     */
    f = new Finder(av2);
    f.findAll("RAF", true, true, false, false);
    assertEquals(f.getIdMatches().size(), 1);
    assertSame(f.getIdMatches().get(0), al2.getSequenceAt(0));
    assertTrue(f.getSearchResults().isEmpty());

    /*
     * match sequence id, description and sequence!
     */
    al2.getSequenceAt(0).setDescription("the efh sequence");
    al2.getSequenceAt(0).setName("mouseEFHkinase");
    al2.getSequenceAt(1).setName("humanEFHkinase");
    f = new Finder(av2);

    /*
     * sequence matches should have no duplicates
     */
    f.findAll("EFH", false, true, false, false);
    assertEquals(f.getIdMatches().size(), 2);
    assertSame(f.getIdMatches().get(0), al2.getSequenceAt(0));
    assertSame(f.getIdMatches().get(1), al2.getSequenceAt(1));

    assertEquals(f.getSearchResults().getCount(), 2);
    SearchResultMatchI match = f.getSearchResults().getResults().get(0);
    assertSame(match.getSequence(), al2.getSequenceAt(1));
    assertEquals(match.getStart(), 5);
    assertEquals(match.getEnd(), 7);
    match = f.getSearchResults().getResults().get(1);
    assertSame(match.getSequence(), al2.getSequenceAt(2));
    assertEquals(match.getStart(), 4);
    assertEquals(match.getEnd(), 6);
  }

  /**
   * Test for matching within sequence ids
   */
  @Test(groups = "Functional")
  public void testFindAll_sequenceIds()
  {
    Finder f = new Finder(av);

    /*
     * case insensitive; seq1 occurs twice in sequence id but
     * only one match should be returned
     */
    f.findAll("SEQ1", false, false, false, false);
    assertEquals(f.getIdMatches().size(), 1);
    assertSame(f.getIdMatches().get(0), al.getSequenceAt(0));
    SearchResultsI searchResults = f.getSearchResults();
    assertTrue(searchResults.isEmpty());

    /*
     * case sensitive
     */
    f = new Finder(av);
    f.findAll("SEQ1", true, false, false, false);
    searchResults = f.getSearchResults();
    assertTrue(searchResults.isEmpty());

    /*
     * match both sequence id and sequence
     */
    AlignmentI al2 = new Alignment(al);
    AlignViewportI av2 = new AlignViewport(al2);
    al2.addSequence(new Sequence("aBz", "xyzabZpqrAbZ"));
    f = new Finder(av2);
    f.findAll("ABZ", false, false, false, false);
    assertEquals(f.getIdMatches().size(), 1);
    assertSame(f.getIdMatches().get(0), al2.getSequenceAt(4));
    searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 2);
    SearchResultMatchI match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al2.getSequenceAt(4));
    assertEquals(match.getStart(), 4);
    assertEquals(match.getEnd(), 6);
    match = searchResults.getResults().get(1);
    assertSame(match.getSequence(), al2.getSequenceAt(4));
    assertEquals(match.getStart(), 10);
    assertEquals(match.getEnd(), 12);
  }

  /**
   * Test finding next match of a sequence pattern in an alignment
   */
  @Test(groups = "Functional")
  public void testFind_findNext()
  {
    // "seq1/8-18 ABCD--EF-GHIJI\n" +
    // "seq2 A--BCDefHI\n" +
    // "seq3 --bcdEFH\n" +
    // "seq4 aa---aMMMMMaaa\n";
    /*
     * efh should be matched in seq2 only
     */
    FinderI f = new Finder(av);
    f.findNext("EfH", false, false, false, false);
    SearchResultsI searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 1);
    SearchResultMatchI match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(1));
    assertEquals(match.getStart(), 5);
    assertEquals(match.getEnd(), 7);

    /*
     * I should be found in seq1 (twice) and seq2 (once)
     */
    f = new Finder(av);
    f.findNext("I", false, false, false, false); // find next: seq1/16
    searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 1);
    match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(0));
    assertEquals(match.getStart(), 16);
    assertEquals(match.getEnd(), 16);

    f.findNext("I", false, false, false, false); // find next: seq1/18
    searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 1);
    match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(0));
    assertEquals(match.getStart(), 18);
    assertEquals(match.getEnd(), 18);

    f.findNext("I", false, false, false, false); // find next: seq2/8
    searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 1);
    match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(1));
    assertEquals(match.getStart(), 8);
    assertEquals(match.getEnd(), 8);

    f.findNext("I", false, false, false, false);
    assertTrue(f.getSearchResults().isEmpty());

    /*
     * find should reset to start of alignment after a failed search
     */
    f.findNext("I", false, false, false, false); // find next: seq1/16
    searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 1);
    match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(0));
    assertEquals(match.getStart(), 16);
    assertEquals(match.getEnd(), 16);
  }

  /**
   * Test for JAL-2302 to verify that sub-matches are not included in a find all
   * result
   */
  @Test(groups = "Functional")
  public void testFindAll_maximalResultOnly()
  {
    Finder f = new Finder(av);
    f.findAll("M+", false, false, false, false);
    SearchResultsI searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 1);
    SearchResultMatchI match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(3));
    assertEquals(match.getStart(), 4); // dataset sequence positions
    assertEquals(match.getEnd(), 8); // base 1
  }

  /**
   * Test finding all matches of a sequence pattern in an alignment
   */
  @Test(groups = "Functional")
  public void testFindAll()
  {
    Finder f = new Finder(av);
    f.findAll("EfH", false, false, false, false);
    SearchResultsI searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 2);
    SearchResultMatchI match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(1));
    assertEquals(match.getStart(), 5);
    assertEquals(match.getEnd(), 7);
    match = searchResults.getResults().get(1);
    assertSame(match.getSequence(), al.getSequenceAt(2));
    assertEquals(match.getStart(), 4);
    assertEquals(match.getEnd(), 6);

    /*
     * find all I should find 2 positions in seq1, 1 in seq2
     */
    f.findAll("I", false, false, false, false);
    searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 3);
    match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(0));
    assertEquals(match.getStart(), 16);
    assertEquals(match.getEnd(), 16);
    match = searchResults.getResults().get(1);
    assertSame(match.getSequence(), al.getSequenceAt(0));
    assertEquals(match.getStart(), 18);
    assertEquals(match.getEnd(), 18);
    match = searchResults.getResults().get(2);
    assertSame(match.getSequence(), al.getSequenceAt(1));
    assertEquals(match.getStart(), 8);
    assertEquals(match.getEnd(), 8);
  }

  /**
   * Test finding all matches, case-sensitive
   */
  @Test(groups = "Functional")
  public void testFindAll_caseSensitive()
  {
    Finder f = new Finder(av);

    /*
     * BC should match seq1/9-10 and seq2/2-3
     */
    f.findAll("BC", true, false, false, false);
    SearchResultsI searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 2);
    SearchResultMatchI match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(0));
    assertEquals(match.getStart(), 9);
    assertEquals(match.getEnd(), 10);
    match = searchResults.getResults().get(1);
    assertSame(match.getSequence(), al.getSequenceAt(1));
    assertEquals(match.getStart(), 2);
    assertEquals(match.getEnd(), 3);

    /*
     * bc should match seq3/1-2
     */
    f = new Finder(av);
    f.findAll("bc", true, false, false, false);
    searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 1);
    match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(2));
    assertEquals(match.getStart(), 1);
    assertEquals(match.getEnd(), 2);

    f.findAll("bC", true, false, false, false);
    assertTrue(f.getSearchResults().isEmpty());
  }

  /**
   * Test finding next match of a sequence pattern in a selection group
   */
  @Test(groups = "Functional")
  public void testFindNext_inSelection()
  {
    /*
     * select sequences 2 and 3, columns 4-6 which contains
     * BCD
     * cdE
     */
    SequenceGroup sg = new SequenceGroup();
    sg.setStartRes(3);
    sg.setEndRes(5);
    sg.addSequence(al.getSequenceAt(1), false);
    sg.addSequence(al.getSequenceAt(2), false);
    av.setSelectionGroup(sg);

    FinderI f = new Finder(av);
    f.findNext("b", false, false, false, false);
    assertTrue(f.getIdMatches().isEmpty());
    SearchResultsI searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 1);
    SearchResultMatchI match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(1));
    assertEquals(match.getStart(), 2);
    assertEquals(match.getEnd(), 2);

    /*
     * a second Find should not return the 'b' in seq3 as outside the selection
     */
    f.findNext("b", false, false, false, false);
    assertTrue(f.getSearchResults().isEmpty());
    assertTrue(f.getIdMatches().isEmpty());

    f = new Finder(av);
    f.findNext("d", false, false, false, false);
    assertTrue(f.getIdMatches().isEmpty());
    searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 1);
    match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(1));
    assertEquals(match.getStart(), 4);
    assertEquals(match.getEnd(), 4);
    f.findNext("d", false, false, false, false);
    assertTrue(f.getIdMatches().isEmpty());
    searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 1);
    match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(2));
    assertEquals(match.getStart(), 3);
    assertEquals(match.getEnd(), 3);
  }

  /**
   * Test finding all matches of a search pattern in a selection group
   */
  @Test(groups = "Functional")
  public void testFindAll_inSelection()
  {
    /*
     * select sequences 2 and 3, columns 4-6 which contains
     * BCD
     * cdE
     */
    SequenceGroup sg = new SequenceGroup();
    sg.setStartRes(3);
    sg.setEndRes(5);
    sg.addSequence(al.getSequenceAt(1), false);
    sg.addSequence(al.getSequenceAt(2), false);
    av.setSelectionGroup(sg);

    /*
     * search for 'e' should match two sequence ids and one residue
     */
    Finder f = new Finder(av);
    f.findAll("e", false, false, false, false);
    assertEquals(f.getIdMatches().size(), 2);
    assertSame(f.getIdMatches().get(0), al.getSequenceAt(1));
    assertSame(f.getIdMatches().get(1), al.getSequenceAt(2));
    SearchResultsI searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 1);
    SearchResultMatchI match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(2));
    assertEquals(match.getStart(), 4);
    assertEquals(match.getEnd(), 4);

    /*
     * search for 'Q' should match two sequence ids only
     */
    f = new Finder(av);
    f.findAll("Q", false, false, false, false);
    assertEquals(f.getIdMatches().size(), 2);
    assertSame(f.getIdMatches().get(0), al.getSequenceAt(1));
    assertSame(f.getIdMatches().get(1), al.getSequenceAt(2));
    assertTrue(f.getSearchResults().isEmpty());
  }

  /**
   * Test finding in selection with a sequence too short to reach it
   */
  @Test(groups = "Functional")
  public void testFind_findAllInSelectionWithShortSequence()
  {
    /*
     * select all sequences, columns 10-12
     * BCD
     * cdE
     */
    SequenceGroup sg = new SequenceGroup();
    sg.setStartRes(9);
    sg.setEndRes(11);
    sg.addSequence(al.getSequenceAt(0), false);
    sg.addSequence(al.getSequenceAt(1), false);
    sg.addSequence(al.getSequenceAt(2), false);
    sg.addSequence(al.getSequenceAt(3), false);
    av.setSelectionGroup(sg);

    /*
     * search for 'I' should match two sequence positions
     */
    Finder f = new Finder(av);
    f.findAll("I", false, false, false, false);
    assertTrue(f.getIdMatches().isEmpty());
    SearchResultsI searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 2);
    SearchResultMatchI match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(0));
    assertEquals(match.getStart(), 16);
    assertEquals(match.getEnd(), 16);
    match = searchResults.getResults().get(1);
    assertSame(match.getSequence(), al.getSequenceAt(1));
    assertEquals(match.getStart(), 8);
    assertEquals(match.getEnd(), 8);
  }

  /**
   * Test that find does not report hidden positions, but does report matches
   * that span hidden gaps
   */
  @Test(groups = "Functional")
  public void testFind_withHiddenColumns()
  {
    /*
     * 0    5   9
     * ABCD--EF-GHI
     * A--BCDefHI
     * --bcdEFH
     * aa---aMMMMMaaa
     */

    /*
     * hide column 3 only, search for aaa
     * should find two matches: aa-[-]-aa and trailing aaa
     */
    HiddenColumns hc = new HiddenColumns();
    hc.hideColumns(3, 3);
    al.setHiddenColumns(hc);
    Finder f = new Finder(av);
    f.findAll("aaa", false, false, false, false);
    SearchResultsI searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 2);
    SearchResultMatchI match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(3));
    assertEquals(match.getStart(), 1);
    assertEquals(match.getEnd(), 3);
    match = searchResults.getResults().get(1);
    assertSame(match.getSequence(), al.getSequenceAt(3));
    assertEquals(match.getStart(), 9);
    assertEquals(match.getEnd(), 11);

    /*
     * hide 2-4 (CD- -BC bcd ---)
     */
    hc.hideColumns(2, 4);

    /*
     * find all search for D should ignore hidden positions in seq1 and seq3,
     * find the visible D in seq2
     */
    f = new Finder(av);
    f.findAll("D", false, false, false, false);
    searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 1);
    match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(1));
    assertEquals(match.getStart(), 4);
    assertEquals(match.getEnd(), 4);

    /*
     * search for AD should fail although these are now
     * consecutive in the visible columns
     */
    f = new Finder(av);
    f.findAll("AD", false, false, false, false);
    searchResults = f.getSearchResults();
    assertTrue(searchResults.isEmpty());

    /*
     * find all 'aaa' should find both start and end of seq4
     * (first run includes hidden gaps)
     */
    f = new Finder(av);
    f.findAll("aaa", false, false, false, false);
    searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 2);
    match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(3));
    assertEquals(match.getStart(), 1);
    assertEquals(match.getEnd(), 3);
    match = searchResults.getResults().get(1);
    assertSame(match.getSequence(), al.getSequenceAt(3));
    assertEquals(match.getStart(), 9);
    assertEquals(match.getEnd(), 11);

    /*
     * hide columns 2-5:
     * find all 'aaa' should match twice in seq4
     * (first match partly hidden, second all visible)
     */
    hc.hideColumns(2, 5);
    f = new Finder(av);
    f.findAll("aaa", false, false, false, false);
    searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 2);
    match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(3));
    assertEquals(match.getStart(), 1);
    assertEquals(match.getEnd(), 3);
    match = searchResults.getResults().get(1);
    assertSame(match.getSequence(), al.getSequenceAt(3));
    assertEquals(match.getStart(), 9);
    assertEquals(match.getEnd(), 11);

    /*
     * find all 'BE' should not match across hidden columns in seq1
     */
    f.findAll("BE", false, false, false, false);
    assertTrue(f.getSearchResults().isEmpty());

    /*
     * boundary case: hide columns at end of alignment
     * search for H should match seq3/6 only
     */
    hc.revealAllHiddenColumns(new ColumnSelection());
    hc.hideColumns(8, 13);
    f = new Finder(av);
    f.findNext("H", false, false, false, false);
    searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 1);
    match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(2));
    assertEquals(match.getStart(), 6);
    assertEquals(match.getEnd(), 6);
  }

  @Test(groups = "Functional")
  public void testFind_withHiddenColumnsAndSelection()
  {
    /*
     * 0    5   9
     * ABCD--EF-GHI
     * A--BCDefHI
     * --bcdEFH
     * aa---aMMMMMaaa
     */

    /*
     * hide columns 2-4 and 6-7
     */
    HiddenColumns hc = new HiddenColumns();
    hc.hideColumns(2, 4);
    hc.hideColumns(6, 7);
    al.setHiddenColumns(hc);

    /*
     * select rows 2-3
     */
    SequenceGroup sg = new SequenceGroup();
    sg.addSequence(al.getSequenceAt(1), false);
    sg.addSequence(al.getSequenceAt(2), false);
    sg.setStartRes(0);
    sg.setEndRes(13);
    av.setSelectionGroup(sg);

    /*
     * find all search for A or H
     * should match seq2/1, seq2/7, not seq3/6
     */
    Finder f = new Finder(av);
    f.findAll("[AH]", false, false, false, false);
    SearchResultsI searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 2);
    SearchResultMatchI match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(1));
    assertEquals(match.getStart(), 1);
    assertEquals(match.getEnd(), 1);
    match = searchResults.getResults().get(1);
    assertSame(match.getSequence(), al.getSequenceAt(1));
    assertEquals(match.getStart(), 7);
    assertEquals(match.getEnd(), 7);
  }

  @Test(groups = "Functional")
  public void testFind_ignoreHiddenColumns()
  {
    /*
     * 0    5   9
     * ABCD--EF-GHI
     * A--BCDefHI
     * --bcdEFH
     * aa---aMMMMMaaa
     */
    HiddenColumns hc = new HiddenColumns();
    hc.hideColumns(2, 4);
    hc.hideColumns(7, 7);
    al.setHiddenColumns(hc);

    /*
     * now have
     * 015689
     * AB-E-GHI
     * A-DeHI
     * --EF
     * aaaMMMMaaa
     */
    Finder f = new Finder(av);
    f.findAll("abe", false, false, false, true); // true = ignore hidden
    SearchResultsI searchResults = f.getSearchResults();

    /*
     * match of seq1 ABE made up of AB and E
     * note only one match is counted
     */
    assertEquals(searchResults.getCount(), 1);
    assertEquals(searchResults.getResults().size(), 2);
    SearchResultMatchI match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(0));
    assertEquals(match.getStart(), 8); // A
    assertEquals(match.getEnd(), 9); // B
    match = searchResults.getResults().get(1);
    assertSame(match.getSequence(), al.getSequenceAt(0));
    assertEquals(match.getStart(), 12); // E
    assertEquals(match.getEnd(), 12);

    f = new Finder(av);
    f.findNext("a.E", false, false, false, true);
    searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 1);
    assertEquals(searchResults.getResults().size(), 2);
    match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(0));
    assertEquals(match.getStart(), 8); // A
    assertEquals(match.getEnd(), 9); // B
    match = searchResults.getResults().get(1);
    assertSame(match.getSequence(), al.getSequenceAt(0));
    assertEquals(match.getStart(), 12); // E
    assertEquals(match.getEnd(), 12);

    f.findNext("a.E", false, false, false, true);
    searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 1);
    assertEquals(searchResults.getResults().size(), 2);
    match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(1));
    assertEquals(match.getStart(), 1); // a
    assertEquals(match.getEnd(), 1);
    match = searchResults.getResults().get(1);
    assertSame(match.getSequence(), al.getSequenceAt(1));
    assertEquals(match.getStart(), 4); // D
    assertEquals(match.getEnd(), 5); // e

    /*
     * find all matching across two hidden column regions
     * note one 'match' is returned as three contiguous matches
     */
    f.findAll("BEG", false, false, false, true);
    searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 1);
    assertEquals(searchResults.getResults().size(), 3);
    match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(0));
    assertEquals(match.getStart(), 9); // B
    assertEquals(match.getEnd(), 9);
    match = searchResults.getResults().get(1);
    assertSame(match.getSequence(), al.getSequenceAt(0));
    assertEquals(match.getStart(), 12); // E
    assertEquals(match.getEnd(), 12);
    match = searchResults.getResults().get(2);
    assertSame(match.getSequence(), al.getSequenceAt(0));
    assertEquals(match.getStart(), 14); // G
    assertEquals(match.getEnd(), 14);

    /*
     * now select columns 0-9 and search for A.*H
     * this should match in the second sequence (split as 3 matches)
     * but not the first (as H is outside the selection)
     */
    SequenceGroup selection = new SequenceGroup();
    selection.setStartRes(0);
    selection.setEndRes(9);
    al.getSequences().forEach(seq -> selection.addSequence(seq, false));
    av.setSelectionGroup(selection);
    f.findAll("A.*H", false, false, false, true);
    searchResults = f.getSearchResults();
    assertEquals(searchResults.getCount(), 1);
    assertEquals(searchResults.getResults().size(), 3);
    // match made of contiguous matches A, DE, H
    match = searchResults.getResults().get(0);
    assertSame(match.getSequence(), al.getSequenceAt(1));
    assertEquals(match.getStart(), 1); // A
    assertEquals(match.getEnd(), 1);
    match = searchResults.getResults().get(1);
    assertSame(match.getSequence(), al.getSequenceAt(1));
    assertEquals(match.getStart(), 4); // D
    assertEquals(match.getEnd(), 5); // E
    match = searchResults.getResults().get(2);
    assertSame(match.getSequence(), al.getSequenceAt(1));
    assertEquals(match.getStart(), 7); // H (there is no G)
    assertEquals(match.getEnd(), 7);
  }

  @Test(groups = "Functional")
  public void testFind_featuresOnly()
  {
    Finder f = new Finder(av);
    // no match when not searching feature descriptions
    f.findAll("Feature", false, false, false, true);
    assertEquals(f.getSearchResults().getCount(), 0);

    // no match when case sensitive on feature descriptions
    f.findAll("feature", true, false, true, true);
    assertEquals(f.getSearchResults().getCount(), 0);

    // search feature descriptions - all match
    f.findAll("Feature", false, false, true, true);
    assertEquals(f.getSearchResults().getCount(), 3);

    List<SequenceI> seqs = f.getSearchResults().getMatchingSubSequences();
    // assume order is preserved in results
    assertEquals(al.getSequenceAt(0).getDatasetSequence(),
            seqs.get(0).getDatasetSequence());
    assertEquals(seqs.get(0).getStart(), 9);
    assertEquals(seqs.get(0).getEnd(), 11);
    assertEquals(al.getSequenceAt(3).getDatasetSequence(),
            seqs.get(1).getDatasetSequence());
    assertEquals(seqs.get(1).getStart(), 9);
    assertEquals(seqs.get(1).getEnd(), 11);
    assertEquals(al.getSequenceAt(3).getDatasetSequence(),
            seqs.get(2).getDatasetSequence());
    assertEquals(seqs.get(2).getStart(), 1);
    assertEquals(seqs.get(2).getEnd(), 3);

    SequenceI sq = null;
    // search feature descriptions incrementally
    // assume same order as before
    f.findNext("Feature", false, false, true, true);
    assertEquals(f.getSearchResults().getCount(), 1);
    sq = f.getSearchResults().getMatchingSubSequences().get(0);
    assertEquals(sq.getSequenceAsString(),
            seqs.get(0).getSequenceAsString());

    // ..
    f.findNext("Feature", false, false, true, true);
    assertEquals(f.getSearchResults().getCount(), 1);
    sq = f.getSearchResults().getMatchingSubSequences().get(0);
    assertEquals(sq.getSequenceAsString(),
            seqs.get(1).getSequenceAsString());

    // ..
    f.findNext("Feature", false, false, true, true);
    assertEquals(f.getSearchResults().getCount(), 1);
    sq = f.getSearchResults().getMatchingSubSequences().get(0);
    assertEquals(sq.getSequenceAsString(),
            seqs.get(2).getSequenceAsString());

  }
}
