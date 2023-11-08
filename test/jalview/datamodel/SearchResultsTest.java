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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.util.BitSet;
import java.util.List;

import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.gui.JvOptionPane;

public class SearchResultsTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testToString()
  {
    SequenceI seq = new Sequence("Seq1", "abcdefghijklm");
    SearchResultsI sr = new SearchResults();
    sr.addResult(seq, 1, 1);
    assertEquals("[Seq1/1-1]", sr.toString());
    sr.addResult(seq, 3, 5);
    assertEquals("[Seq1/1-1, Seq1/3-5]", sr.toString());

    seq = new Sequence("Seq2", "pqrstuvwxy");
    sr.addResult(seq, 6, 7);
    assertEquals("[Seq1/1-1, Seq1/3-5, Seq2/6-7]", sr.toString());
  }

  @Test(groups = { "Functional" })
  public void testEquals()
  {
    SequenceI seq1 = new Sequence("", "abcdefghijklm");
    SearchResultsI sr1 = new SearchResults();
    SearchResultsI sr2 = new SearchResults();

    assertFalse(sr1.equals(null)); // null object
    assertFalse(sr1.equals(seq1)); // wrong type
    assertTrue(sr1.equals(sr1)); // self
    assertTrue(sr1.equals(sr2)); // empty
    assertTrue(sr2.equals(sr1)); // reflexive

    /*
     * if only one result is not empty
     */
    sr1.addResult(seq1, 1, 1);
    assertTrue(sr1.equals(sr1));
    assertFalse(sr1.equals(sr2));
    assertFalse(sr2.equals(sr1));

    /*
     * both the same
     */
    sr2.addResult(seq1, 1, 1);
    assertTrue(sr1.equals(sr2));
    assertTrue(sr2.equals(sr1));

    /*
     * both have three matches
     */
    sr1.addResult(seq1, 3, 4);
    sr1.addResult(seq1, 6, 8);
    sr2.addResult(seq1, 3, 4);
    sr2.addResult(seq1, 6, 8);
    assertTrue(sr1.equals(sr1));
    assertTrue(sr2.equals(sr2));
    assertTrue(sr1.equals(sr2));
    assertTrue(sr2.equals(sr1));
  }

  /**
   * Matches that are similar but for distinct sequences are not equal
   */
  @Test(groups = { "Functional" })
  public void testEquals_distinctSequences()
  {
    SequenceI seq1 = new Sequence("", "abcdefghijklm");
    SequenceI seq2 = new Sequence("", "abcdefghijklm");
    SearchResultsI sr1 = new SearchResults();
    SearchResultsI sr2 = new SearchResults();

    sr1.addResult(seq1, 1, 1);
    sr2.addResult(seq2, 1, 1);
    assertFalse(sr1.equals(sr2));
    assertFalse(sr2.equals(sr1));
  }

  /**
   * Matches that are the same except for ordering are not equal
   */
  @Test(groups = { "Functional" })
  public void testEquals_orderDiffers()
  {
    SequenceI seq1 = new Sequence("", "abcdefghijklm");
    SearchResultsI sr1 = new SearchResults();
    SearchResultsI sr2 = new SearchResults();

    sr1.addResult(seq1, 1, 1);
    sr1.addResult(seq1, 2, 2);
    sr2.addResult(seq1, 2, 2);
    sr2.addResult(seq1, 1, 1);
    assertFalse(sr1.equals(sr2));
    assertFalse(sr2.equals(sr1));
  }

  /**
   * Verify that hashCode matches for equal objects
   */
  @Test(groups = { "Functional" })
  public void testHashcode()
  {
    SequenceI seq1 = new Sequence("", "abcdefghijklm");
    SearchResultsI sr1 = new SearchResults();
    SearchResultsI sr2 = new SearchResults();

    /*
     * both empty
     */
    assertEquals(sr1.hashCode(), sr2.hashCode());

    /*
     * both one match
     */
    sr1.addResult(seq1, 1, 1);
    sr2.addResult(seq1, 1, 1);
    assertEquals(sr1.hashCode(), sr2.hashCode());

    /*
     * both three matches
     */
    sr1.addResult(seq1, 3, 4);
    sr1.addResult(seq1, 6, 8);
    sr2.addResult(seq1, 3, 4);
    sr2.addResult(seq1, 6, 8);
    assertEquals(sr1.hashCode(), sr2.hashCode());
  }

  /**
   * Verify that SearchResults$Match constructor normalises start/end to the
   * 'forwards' direction
   */
  @Test(groups = { "Functional" })
  public void testMatchConstructor()
  {
    SequenceI seq1 = new Sequence("", "abcdefghijklm");
    SearchResultMatchI m = new SearchResults().new Match(seq1, 2, 5);
    assertSame(seq1, m.getSequence());
    assertEquals(2, m.getStart());
    assertEquals(5, m.getEnd());

    // now a reverse mapping:
    m = new SearchResults().new Match(seq1, 5, 2);
    assertSame(seq1, m.getSequence());
    assertEquals(2, m.getStart());
    assertEquals(5, m.getEnd());
  }

  @Test(groups = { "Functional" })
  public void testMatchContains()
  {
    SequenceI seq1 = new Sequence("", "abcdefghijklm");
    SequenceI seq2 = new Sequence("", "abcdefghijklm");
    SearchResultMatchI m = new SearchResults().new Match(seq1, 2, 5);

    assertTrue(m.contains(seq1, 2, 5));
    assertTrue(m.contains(seq1, 3, 5));
    assertTrue(m.contains(seq1, 2, 4));
    assertTrue(m.contains(seq1, 3, 3));

    assertFalse(m.contains(seq1, 2, 6));
    assertFalse(m.contains(seq1, 1, 5));
    assertFalse(m.contains(seq1, 1, 8));
    assertFalse(m.contains(seq2, 3, 3));
    assertFalse(m.contains(null, 3, 3));
  }

  /**
   * test markColumns for creating column selections
   */
  @Test(groups = { "Functional" })
  public void testMarkColumns()
  {
    int marked = 0;
    SequenceI seq1 = new Sequence("", "abcdefghijklm");
    SequenceI seq2 = new Sequence("", "abcdefghijklm");
    SequenceGroup s1g = new SequenceGroup(), s2g = new SequenceGroup(),
            sallg = new SequenceGroup();
    s1g.addSequence(seq1, false);
    s2g.addSequence(seq2, false);
    sallg.addSequence(seq1, false);
    sallg.addSequence(seq2, false);

    SearchResultsI sr = new SearchResults();
    BitSet bs = new BitSet();

    SearchResultMatchI srm = null;
    srm = sr.addResult(seq1, 1, 1);
    Assert.assertNotNull("addResult didn't return Match", srm);
    srm = sr.addResult(seq2, 1, 2);
    assertEquals("Sequence reference not set", seq2, srm.getSequence());
    assertEquals("match start incorrect", 1, srm.getStart());
    assertEquals("match end incorrect", 2, srm.getEnd());

    // set start/end range for groups to cover matches

    s1g.setStartRes(0);
    s1g.setEndRes(5);
    s2g.setStartRes(0);
    s2g.setEndRes(5);
    sallg.setStartRes(0);
    sallg.setEndRes(5);

    /*
     * just seq1
     */
    marked = sr.markColumns(s1g, bs);
    // check the bitset cardinality before checking the return value
    assertEquals("Didn't mark expected number", 1, bs.cardinality());
    assertEquals("Didn't return count of number of bits marked", 1, marked);
    assertTrue("Didn't mark expected position", bs.get(0));
    // now check return value for marking the same again
    assertEquals(
            "Didn't count number of bits marked for existing marked set", 0,
            sr.markColumns(s1g, bs));
    bs.clear();

    /*
     * just seq2
     */
    marked = sr.markColumns(s2g, bs);
    assertEquals("Didn't mark expected number", 2, bs.cardinality());
    assertEquals("Didn't return count of number of bits marked", 2, marked);
    assertTrue("Didn't mark expected position (1)", bs.get(0));
    assertTrue("Didn't mark expected position (2)", bs.get(1));

    /*
     * both seq1 and seq2 
     * should be same as seq2
     */
    BitSet allbs = new BitSet();
    assertEquals(2, sr.markColumns(sallg, allbs));
    assertEquals(bs, allbs);

    // now check range selection

    /*
     * limit s2g to just the second column, sallg to the first column
     */
    s2g.setStartRes(1);
    s2g.setEndRes(1);
    sallg.setEndRes(0);
    BitSet tbs = new BitSet();
    assertEquals("Group start/end didn't select columns to mark", 1,
            sr.markColumns(s2g, tbs));
    assertEquals("Group start/end didn't select columns to mark", 1,
            sr.markColumns(sallg, tbs));
    assertEquals(
            "Didn't set expected number of columns in total for two successive marks",
            2, tbs.cardinality());
  }

  /**
   * Test to verify adding doesn't create duplicate results
   */
  @Test(groups = { "Functional" })
  public void testAddResult()
  {
    SequenceI seq1 = new Sequence("", "abcdefghijklm");
    SearchResultsI sr = new SearchResults();
    sr.addResult(seq1, 3, 5);
    assertEquals(1, sr.getCount());
    sr.addResult(seq1, 3, 5);
    assertEquals(1, sr.getCount());
    sr.addResult(seq1, 3, 6);
    assertEquals(2, sr.getCount());
  }

  /**
   * Test for method that checks if search results matches a sequence region
   */
  @Test(groups = { "Functional" })
  public void testInvolvesSequence()
  {
    SequenceI dataset = new Sequence("genome", "ATGGCCCTTTAAGCAACATTT");
    // first 'exon':
    SequenceI cds1 = new Sequence("cds1/1-12", "ATGGCCCTTTAA");
    cds1.setDatasetSequence(dataset);
    // overlapping second 'exon':
    SequenceI cds2 = new Sequence("cds2/7-18", "CTTTAAGCAACA");
    cds2.setDatasetSequence(dataset);
    // unrelated sequence
    SequenceI cds3 = new Sequence("cds3", "ATGGCCCTTTAAGCAACA");

    SearchResults sr = new SearchResults();
    assertFalse(sr.involvesSequence(cds1));

    /*
     * cds1 and cds2 share the same dataset sequence, but
     * only cds1 overlaps match 4:6 (fixes bug JAL-3613)
     */
    sr.addResult(dataset, 4, 6);
    assertTrue(sr.involvesSequence(cds1));
    assertFalse(sr.involvesSequence(cds2));
    assertFalse(sr.involvesSequence(cds3));

    /*
     * search results overlap cds2 only
     */
    sr = new SearchResults();
    sr.addResult(dataset, 18, 18);
    assertFalse(sr.involvesSequence(cds1));
    assertTrue(sr.involvesSequence(cds2));

    /*
     * add a search result overlapping cds1
     */
    sr.addResult(dataset, 1, 1);
    assertTrue(sr.involvesSequence(cds1));
    assertTrue(sr.involvesSequence(cds2));

    /*
     * single search result overlapping both
     */
    sr = new SearchResults();
    sr.addResult(dataset, 10, 12);
    assertTrue(sr.involvesSequence(cds1));
    assertTrue(sr.involvesSequence(cds2));

    /*
     * search results matching aligned sequence
     */
    sr = new SearchResults();
    sr.addResult(cds1, 10, 12);
    assertTrue(sr.involvesSequence(cds1));
    assertFalse(sr.involvesSequence(cds2));
    sr.addResult(cds2, 1, 3); // no start-end overlap
    assertFalse(sr.involvesSequence(cds2));
    sr.addResult(cds2, 7, 9); // start-end overlap
    assertTrue(sr.involvesSequence(cds2));
  }

  /**
   * Test extraction of Sequence objects for matched ranges on a sequence
   */
  @Test(groups = { "Functional" })
  public void testGetSequences()
  {
    SequenceI seq1 = new Sequence("", "abcdefghijklm");
    SequenceI seq2 = new Sequence("", "nopqrstuvwxyz");
    seq2.setStart(23);
    seq2.setEnd(35);
    List<SequenceI> seqres = null;

    SearchResultsI sr = new SearchResults();
    seqres = sr.getMatchingSubSequences();
    assertEquals(0, seqres.size());

    sr.addResult(seq1, 3, 5);
    seqres = sr.getMatchingSubSequences();

    assertEquals(1, seqres.size());
    assertEquals("cde", seqres.get(0).getSequenceAsString());
    assertEquals(3, seqres.get(0).getStart());
    assertEquals(seq1, seqres.get(0).getDatasetSequence());

    sr.addResult(seq1, 3, 6);
    seqres = sr.getMatchingSubSequences();

    assertEquals(2, seqres.size());
    assertEquals("cdef", seqres.get(1).getSequenceAsString());
    assertEquals(3, seqres.get(1).getStart());

    // this is a quirk - match on 26-29 yields subsequence 27-30
    sr.addResult(seq2, 26, 29);
    seqres = sr.getMatchingSubSequences();
    assertEquals(3, seqres.size());
    assertEquals("qrst", seqres.get(2).getSequenceAsString());
    assertEquals(26, seqres.get(2).getStart());
  }

}
