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

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import jalview.analysis.SequenceIdMatcher.SeqIdName;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SequenceIdMatcherTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * Test the method that checks for one sequence id starting with the other,
   * followed by an 'allowed' separator character
   */
  @Test(groups = "Functional")
  public void test_seqIdNameEquals()
  {
    SequenceIdMatcher sequenceIdMatcher = new SequenceIdMatcher(
            new SequenceI[] {});

    /*
     * target name = matcher name + word separator...
     */
    SeqIdName testee = sequenceIdMatcher.new SeqIdName("A12345");
    assertTrue(testee.equals("A12345"));
    assertTrue(testee.equals("A12345~"));
    assertTrue(testee.equals("A12345."));
    assertTrue(testee.equals("A12345 "));
    assertTrue(testee.equals("A12345|"));
    assertTrue(testee.equals("A12345#"));
    assertTrue(testee.equals("A12345\\"));
    assertTrue(testee.equals("A12345/"));
    assertTrue(testee.equals("A12345<"));
    assertTrue(testee.equals("A12345>"));
    assertTrue(testee.equals("A12345!"));
    assertTrue(testee.equals("A12345\""));
    assertTrue(testee.equals("A12345" + String.valueOf((char) 0x00A4)));
    assertTrue(testee.equals("A12345$a"));
    assertTrue(testee.equals("A12345%b"));
    assertTrue(testee.equals("A12345^cd"));
    assertTrue(testee.equals("A12345*efg"));
    assertTrue(testee.equals("A12345)^&!"));
    assertTrue(testee.equals("A12345}01&*"));
    assertTrue(testee.equals("A12345[A23456"));
    assertTrue(testee.equals("A12345@|Uniprot"));
    assertTrue(testee.equals("A12345'whatever you want here"));
    assertTrue(testee.equals("A12345,"));
    assertTrue(testee.equals("A12345?"));
    assertTrue(testee.equals("A12345_"));
    /*
     * case insensitive matching
     */
    assertTrue(testee.equals("a12345"));

    /*
     * matcher name = target name + word separator...
     */
    testee = sequenceIdMatcher.new SeqIdName("A12345#");
    assertTrue(testee.equals("A12345"));

    /*
     * case insensitive matching
     */
    assertTrue(testee.equals("a12345"));

    /*
     * miscellaneous failing cases
     */
    testee = sequenceIdMatcher.new SeqIdName("A12345");
    assertFalse(testee.equals((Object) null));
    assertFalse(testee.equals(""));
    assertFalse(testee.equals("A12346|A12345"));
    /*
     * case insensitive matching
     */
    assertTrue(testee.equals("a12345"));

    testee = sequenceIdMatcher.new SeqIdName("A12345?B23456");
    assertFalse(testee.equals("B23456"));
    assertFalse(testee.equals("A12345|"));
    assertFalse(testee.equals("A12345?"));

    testee = sequenceIdMatcher.new SeqIdName("A12345<");
    assertFalse(testee.equals("A12345?"));
    assertTrue(testee.equals("A12345<")); // bug? inconsistent
    /*
     * case insensitive matching
     */
    assertTrue(testee.equals("a12345"));

    testee = sequenceIdMatcher.new SeqIdName("UNIPROT|A12345");
    assertFalse(testee.equals("A12345"));
    assertFalse(testee.equals("UNIPROT|B98765"));
    assertFalse(testee.equals("UNIPROT|"));
    assertTrue(testee.equals("UNIPROT"));
  }
}
