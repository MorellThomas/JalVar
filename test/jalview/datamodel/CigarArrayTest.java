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

import jalview.gui.JvOptionPane;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CigarArrayTest
{
  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = "Functional")
  public void testConstructor()
  {
    SequenceI seq1 = new Sequence("sq1",
            "ASFDDABACBACBACBACBACBACBABCABCBACBABCAB");
    Sequence seq2 = new Sequence("sq2",
            "TTTTTTACBCBABCABCABCABCBACBACBABCABCABCBA");

    // construct alignment
    AlignmentI al = new Alignment(new SequenceI[] { seq1, seq2 });

    // hide columns
    HiddenColumns hc = new HiddenColumns();
    hc.hideColumns(3, 6);
    hc.hideColumns(16, 20);

    // select group
    SequenceGroup sg1 = new SequenceGroup();
    sg1.addSequence(seq1, false);
    sg1.setStartRes(2);
    sg1.setEndRes(23);

    // Cigar array meanings:
    // M = match
    // D = deletion
    // I = insertion
    // number preceding M/D/I is the number of residues which
    // match/are deleted/are inserted
    // In the CigarArray constructor only matches or deletions are created, as
    // we are comparing a sequence to its own subsequence (the group) + hidden
    // columns.

    // no hidden columns case
    CigarArray cig = new CigarArray(al, null, sg1);
    String result = cig.getCigarstring();
    assertEquals(result, "22M");

    cig = new CigarArray(al, hc, sg1);
    result = cig.getCigarstring();
    assertEquals(result, "1M4D9M5D3M");

    // group starts at hidden cols
    sg1.setStartRes(3);
    cig = new CigarArray(al, hc, sg1);
    result = cig.getCigarstring();
    assertEquals(result, "4D9M5D3M");

    // group starts at last but 1 hidden col
    sg1.setStartRes(5);
    cig = new CigarArray(al, hc, sg1);
    result = cig.getCigarstring();
    assertEquals(result, "2D9M5D3M");

    // group starts at last hidden col
    sg1.setStartRes(6);
    cig = new CigarArray(al, hc, sg1);
    result = cig.getCigarstring();
    assertEquals(result, "1D9M5D3M");

    // group starts just after hidden region
    sg1.setStartRes(7);
    cig = new CigarArray(al, hc, sg1);
    result = cig.getCigarstring();
    assertEquals(result, "9M5D3M");

    // group ends just before start of hidden region
    sg1.setStartRes(5);
    sg1.setEndRes(15);
    cig = new CigarArray(al, hc, sg1);
    result = cig.getCigarstring();
    assertEquals(result, "2D9M");

    // group ends at start of hidden region
    sg1.setEndRes(16);
    cig = new CigarArray(al, hc, sg1);
    result = cig.getCigarstring();
    assertEquals(result, "2D9M1D");

    // group ends 1 after start of hidden region
    sg1.setEndRes(17);
    cig = new CigarArray(al, hc, sg1);
    result = cig.getCigarstring();
    assertEquals(result, "2D9M2D");

    // group ends at end of hidden region
    sg1.setEndRes(20);
    cig = new CigarArray(al, hc, sg1);
    result = cig.getCigarstring();
    assertEquals(result, "2D9M5D");

    // group ends just after end of hidden region
    sg1.setEndRes(21);
    cig = new CigarArray(al, hc, sg1);
    result = cig.getCigarstring();
    assertEquals(result, "2D9M5D1M");

    // group ends 2 after end of hidden region
    sg1.setEndRes(22);
    cig = new CigarArray(al, hc, sg1);
    result = cig.getCigarstring();
    assertEquals(result, "2D9M5D2M");
  }
}
