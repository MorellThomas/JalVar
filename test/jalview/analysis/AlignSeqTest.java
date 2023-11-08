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
import static org.testng.Assert.assertNull;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import jalview.datamodel.Sequence;
import jalview.gui.JvOptionPane;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AlignSeqTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testExtractGaps()
  {
    assertNull(AlignSeq.extractGaps(null, null));
    assertNull(AlignSeq.extractGaps(null, "ACG"));
    assertNull(AlignSeq.extractGaps("-. ", null));

    assertEquals(AlignSeq.extractGaps("", " AC-G.T"), " AC-G.T");
    assertEquals(AlignSeq.extractGaps(" ", " AC-G.T"), "AC-G.T");
    assertEquals(AlignSeq.extractGaps(" -", " AC-G.T"), "ACG.T");
    assertEquals(AlignSeq.extractGaps(" -.", " AC-G.T ."), "ACGT");
    assertEquals(AlignSeq.extractGaps("-", " AC-G.T"), " ACG.T");
    assertEquals(AlignSeq.extractGaps("-. ", " -. .-"), "");
  }

  @Test(groups = { "Functional" })
  public void testIndexEncode_nucleotide()
  {
    AlignSeq as = new AlignSeq(new Sequence("s1", "TTAG"),
            new Sequence("s2", "ACGT"), AlignSeq.DNA);
    int[] expected = new int[] { 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6,
        7, 7, 8, 8, 9, 9, -1, -1, 10, -1 };
    String s = "aAcCgGtTuUiIxXrRyYnN .-?";
    assertArrayEquals(expected, as.indexEncode(s));
  }

  @Test(groups = { "Functional" })
  public void testIndexEncode_peptide()
  {
    AlignSeq as = new AlignSeq(new Sequence("s1", "PFY"),
            new Sequence("s2", "RQW"), AlignSeq.PEP);
    int[] expected = new int[] { 0, 0, 1, 1, 2, 2, 21, 21, 22, 22, -1, 23,
        -1, -1, -1 };
    String s = "aArRnNzZxX *.-?";
    assertArrayEquals(expected, as.indexEncode(s));
  }
}
