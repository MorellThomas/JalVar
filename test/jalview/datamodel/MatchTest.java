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
import static org.testng.AssertJUnit.assertTrue;

import jalview.gui.JvOptionPane;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MatchTest
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
    SearchResultMatchI m = new SearchResults().new Match(seq, 3, 5);
    assertEquals("Seq1/3-5", m.toString());
  }

  @Test(groups = { "Functional" })
  public void testEquals()
  {
    SequenceI seq1 = new Sequence("", "abcdefghijklm");
    SequenceI seq2 = new Sequence("", "abcdefghijklm");
    SearchResultsI sr1 = new SearchResults();
    SearchResultsI sr2 = new SearchResults();

    assertFalse(sr1.equals(null));
    assertFalse(sr1.equals(seq1));
    assertTrue(sr1.equals(sr1));
    assertTrue(sr1.equals(sr2));
    assertTrue(sr2.equals(sr1));

    sr1.addResult(seq1, 1, 1);
    assertFalse(sr1.equals(sr2));
    assertFalse(sr2.equals(sr1));

    sr2.addResult(seq1, 1, 1);
    assertTrue(sr1.equals(sr2));
    assertTrue(sr2.equals(sr1));

    /*
     * same match but on different sequences - not equal
     */
    SearchResultsI sr3 = new SearchResults();
    sr3.addResult(seq2, 1, 1);
    assertFalse(sr1.equals(sr3));
    assertFalse(sr3.equals(sr1));

    /*
     * same sequence but different end position - not equal
     */
    sr1.addResult(seq1, 3, 4);
    sr2.addResult(seq1, 3, 5);
    assertFalse(sr1.equals(sr2));

    /*
     * same sequence but different start position - not equal
     */
    sr1 = new SearchResults();
    sr2 = new SearchResults();
    sr1.addResult(seq1, 3, 4);
    sr2.addResult(seq1, 2, 4);
    assertFalse(sr1.equals(sr2));
  }
}
