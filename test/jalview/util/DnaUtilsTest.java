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
package jalview.util;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.fail;

import jalview.gui.JvOptionPane;

import java.text.ParseException;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DnaUtilsTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * Tests for parsing an ENA/GenBank location specifier
   * 
   * @throws ParseException
   * 
   * @see http://www.insdc.org/files/feature_table.html#3.4
   */
  @Test(groups = { "Functional" })
  public void testParseLocation() throws ParseException
  {
    /*
     * single locus
     */
    List<int[]> ranges = DnaUtils.parseLocation("467");
    assertEquals(1, ranges.size());
    assertEquals(467, ranges.get(0)[0]);
    assertEquals(467, ranges.get(0)[1]);

    /*
     * simple range
     */
    ranges = DnaUtils.parseLocation("12..78");
    assertEquals(1, ranges.size());
    assertEquals(12, ranges.get(0)[0]);
    assertEquals(78, ranges.get(0)[1]);

    /*
     * join of simple ranges
     */
    ranges = DnaUtils.parseLocation("join(12..78,134..202,322..345)");
    assertEquals(3, ranges.size());
    assertEquals(12, ranges.get(0)[0]);
    assertEquals(78, ranges.get(0)[1]);
    assertEquals(134, ranges.get(1)[0]);
    assertEquals(202, ranges.get(1)[1]);
    assertEquals(322, ranges.get(2)[0]);
    assertEquals(345, ranges.get(2)[1]);

    /*
     * complement of a simple range
     */
    ranges = DnaUtils.parseLocation("complement(34..126)");
    assertEquals(1, ranges.size());
    assertEquals(126, ranges.get(0)[0]);
    assertEquals(34, ranges.get(0)[1]);

    /*
     * complement of a join
     */
    ranges = DnaUtils
            .parseLocation("complement(join(2691..4571,4918..5163))");
    assertEquals(2, ranges.size());
    assertEquals(5163, ranges.get(0)[0]);
    assertEquals(4918, ranges.get(0)[1]);
    assertEquals(4571, ranges.get(1)[0]);
    assertEquals(2691, ranges.get(1)[1]);

    /*
     * join of two complements
     */
    ranges = DnaUtils.parseLocation(
            "join(complement(4918..5163),complement(2691..4571))");
    assertEquals(2, ranges.size());
    assertEquals(5163, ranges.get(0)[0]);
    assertEquals(4918, ranges.get(0)[1]);
    assertEquals(4571, ranges.get(1)[0]);
    assertEquals(2691, ranges.get(1)[1]);

    /*
     * join complement to non-complement
     * @see http://www.ncbi.nlm.nih.gov/genbank/genomesubmit_annotation/ Transpliced Genes
     */
    ranges = DnaUtils
            .parseLocation("join(complement(36618..36700),86988..87064)");
    assertEquals(2, ranges.size());
    assertEquals(36700, ranges.get(0)[0]);
    assertEquals(36618, ranges.get(0)[1]);
    assertEquals(86988, ranges.get(1)[0]);
    assertEquals(87064, ranges.get(1)[1]);

    /*
     * valid things we don't yet handle
     */
    checkForParseException("<34..126");
    checkForParseException("35..>126");
    checkForParseException("34.126");
    checkForParseException("34^126");
    checkForParseException("order(34..126,130..180)");

    /*
     * invalid things
     */
    checkForParseException("");
    checkForParseException("JOIN(1..2)");
    checkForParseException("join(1..2");
    checkForParseException("join(1..2(");
    checkForParseException("complement(1..2");
    checkForParseException("complement(1..2(");
    try
    {
      assertNull(DnaUtils.parseLocation(null));
      fail("Expected exception");
    } catch (NullPointerException e)
    {
      // expected
    }

    /*
     * nested joins are not allowed; just as well since this fails to parse
     * (splitting tokens by comma fragments the inner join expression)
     */
    checkForParseException("join(1..2,join(4..5,10..12),18..22)");
    /*
     * complement may not enclose multiple ranges 
     * parsing fails for the same reason
     */
    checkForParseException(
            "join(complement(36618..36700,4000..4200),86988..87064)");
  }

  /**
   * Verifies that a ParseException is thrown when the given location is parsed
   * 
   * @param location
   */
  void checkForParseException(String location)
  {
    try
    {
      DnaUtils.parseLocation(location);
      fail("Expected exception");
    } catch (ParseException e)
    {
      // expected;
    }
  }

}
