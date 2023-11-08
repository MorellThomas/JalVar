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
package jalview.io.gff;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import jalview.gui.JvOptionPane;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GffHelperBaseTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * Test the method that parses lines like <br>
   * ID=2345;Name=Something,Another thing;Notes=Hello;Notes=World
   */
  @Test(groups = { "Functional" })
  public void testParseNameValuePairs()
  {
    assertTrue(GffHelperBase.parseNameValuePairs(null, ";", ' ', ",")
            .isEmpty());
    assertTrue(
            GffHelperBase.parseNameValuePairs("", ";", ' ', ",").isEmpty());
    assertTrue(GffHelperBase
            .parseNameValuePairs("hello=world", ";", ' ', ",").isEmpty());

    Map<String, List<String>> map = GffHelperBase
            .parseNameValuePairs("hello world", ";", ' ', ", ");
    assertEquals(map.size(), 1);
    assertEquals(map.get("hello").size(), 1);
    assertEquals(map.get("hello").get(0), "world");

    map = GffHelperBase.parseNameValuePairs(
            "Method= manual curation ;nothing; Notes=F2 S ; Notes=Metal,Shiny%2Csmooth; Type=",
            ";", '=', ",");

    // Type is ignored as no value was supplied
    assertEquals(map.size(), 2);

    assertEquals(map.get("Method").size(), 1);
    assertEquals(map.get("Method").get(0), "manual curation"); // trimmed

    assertEquals(map.get("Notes").size(), 3);
    assertEquals(map.get("Notes").get(0), "F2 S");
    assertEquals(map.get("Notes").get(1), "Metal");
    assertEquals(map.get("Notes").get(2), "Shiny%2Csmooth"); // not decoded here

    /*
     * gff3 style with nested attribute values
     */
    String csqValue = "POLYPHEN=possibly_damaging,probably_damaging,SIFT=tolerated%2Cdeleterious";
    map = GffHelperBase.parseNameValuePairs("hello=world;CSQ=" + csqValue,
            ";", '=', ",");
    assertEquals(map.size(), 2); // keys hello, CSQ
    assertEquals(map.get("hello").size(), 1);
    assertEquals(map.get("hello").get(0), "world");
    // CSQ values is read 'raw' here, and parsed further elsewhere
    assertEquals(map.get("CSQ").size(), 1);
    assertEquals(map.get("CSQ").get(0), csqValue);
  }

  /**
   * Test for the method that tries to trim mappings to equivalent lengths
   */
  @Test(groups = "Functional")
  public void testTrimMapping()
  {
    int[] from = { 1, 12 };
    int[] to = { 20, 31 };
    assertTrue(GffHelperBase.trimMapping(from, to, 1, 1));
    assertEquals(Arrays.toString(from), "[1, 12]"); // unchanged
    assertEquals(Arrays.toString(to), "[20, 31]"); // unchanged

    // from too long:
    from = new int[] { 1, 13 };
    assertTrue(GffHelperBase.trimMapping(from, to, 1, 1));
    assertEquals(Arrays.toString(from), "[1, 12]"); // trimmed
    assertEquals(Arrays.toString(to), "[20, 31]"); // unchanged

    // to too long:
    to = new int[] { 20, 33 };
    assertTrue(GffHelperBase.trimMapping(from, to, 1, 1));
    assertEquals(Arrays.toString(from), "[1, 12]"); // unchanged
    assertEquals(Arrays.toString(to), "[20, 31]"); // trimmed

    // from reversed:
    from = new int[] { 12, 1 };
    assertTrue(GffHelperBase.trimMapping(from, to, 1, 1));
    assertEquals(Arrays.toString(from), "[12, 1]"); // unchanged
    assertEquals(Arrays.toString(to), "[20, 31]"); // unchanged

    // to reversed:
    to = new int[] { 31, 20 };
    assertTrue(GffHelperBase.trimMapping(from, to, 1, 1));
    assertEquals(Arrays.toString(from), "[12, 1]"); // unchanged
    assertEquals(Arrays.toString(to), "[31, 20]"); // unchanged

    // from reversed and too long:
    from = new int[] { 14, 1 };
    assertTrue(GffHelperBase.trimMapping(from, to, 1, 1));
    assertEquals(Arrays.toString(from), "[14, 3]"); // end trimmed
    assertEquals(Arrays.toString(to), "[31, 20]"); // unchanged

    // to reversed and too long:
    to = new int[] { 31, 10 };
    assertTrue(GffHelperBase.trimMapping(from, to, 1, 1));
    assertEquals(Arrays.toString(from), "[14, 3]"); // unchanged
    assertEquals(Arrays.toString(to), "[31, 20]"); // end trimmed

    // cdna to peptide (matching)
    from = new int[] { 1, 18 };
    to = new int[] { 4, 9 };
    assertTrue(GffHelperBase.trimMapping(from, to, 3, 1));
    assertEquals(Arrays.toString(from), "[1, 18]"); // unchanged
    assertEquals(Arrays.toString(to), "[4, 9]"); // unchanged

    // overlong cdna to peptide
    from = new int[] { 1, 20 };
    assertTrue(GffHelperBase.trimMapping(from, to, 3, 1));
    assertEquals(Arrays.toString(from), "[1, 18]"); // end trimmed
    assertEquals(Arrays.toString(to), "[4, 9]"); // unchanged

    // overlong cdna (reversed) to peptide
    from = new int[] { 20, 1 };
    assertTrue(GffHelperBase.trimMapping(from, to, 3, 1));
    assertEquals(Arrays.toString(from), "[20, 3]"); // end trimmed
    assertEquals(Arrays.toString(to), "[4, 9]"); // unchanged

    // overlong cdna (reversed) to peptide (reversed)
    from = new int[] { 20, 1 };
    to = new int[] { 9, 4 };
    assertTrue(GffHelperBase.trimMapping(from, to, 3, 1));
    assertEquals(Arrays.toString(from), "[20, 3]"); // end trimmed
    assertEquals(Arrays.toString(to), "[9, 4]"); // unchanged

    // peptide to cdna (matching)
    from = new int[] { 4, 9 };
    to = new int[] { 1, 18 };
    assertTrue(GffHelperBase.trimMapping(from, to, 1, 3));
    assertEquals(Arrays.toString(from), "[4, 9]"); // unchanged
    assertEquals(Arrays.toString(to), "[1, 18]"); // unchanged

    // peptide to overlong cdna
    to = new int[] { 1, 20 };
    assertTrue(GffHelperBase.trimMapping(from, to, 1, 3));
    assertEquals(Arrays.toString(from), "[4, 9]"); // unchanged
    assertEquals(Arrays.toString(to), "[1, 18]"); // end trimmed

    // peptide to overlong cdna (reversed)
    to = new int[] { 20, 1 };
    assertTrue(GffHelperBase.trimMapping(from, to, 1, 3));
    assertEquals(Arrays.toString(from), "[4, 9]"); // unchanged
    assertEquals(Arrays.toString(to), "[20, 3]"); // end trimmed

    // peptide (reversed) to overlong cdna (reversed)
    from = new int[] { 9, 4 };
    to = new int[] { 20, 1 };
    assertTrue(GffHelperBase.trimMapping(from, to, 1, 3));
    assertEquals(Arrays.toString(from), "[9, 4]"); // unchanged
    assertEquals(Arrays.toString(to), "[20, 3]"); // end trimmed

    // overlong peptide to word-length cdna
    from = new int[] { 4, 10 };
    to = new int[] { 1, 18 };
    assertTrue(GffHelperBase.trimMapping(from, to, 1, 3));
    assertEquals(Arrays.toString(from), "[4, 9]"); // end trimmed
    assertEquals(Arrays.toString(to), "[1, 18]"); // unchanged

    // overlong peptide to non-word-length cdna
    from = new int[] { 4, 10 };
    to = new int[] { 1, 19 };
    assertFalse(GffHelperBase.trimMapping(from, to, 1, 3));
    assertEquals(Arrays.toString(from), "[4, 10]"); // unchanged
    assertEquals(Arrays.toString(to), "[1, 19]"); // unchanged
  }

  @Test(groups = { "Functional" })
  public void testParseAttributeMap()
  {
    Map<String, String> map = GffHelperBase
            .parseAttributeMap("A=B,C%2C%3D%3B%09%25D,X=Y");
    assertEquals(map.size(), 2);
    // value of A is everything up to and excluding ,X=
    assertEquals(map.get("A"), "B,C,=;\t%D");
    assertEquals(map.get("X"), "Y");

    /*
     * malformed cases should result in an empty map
     */
    map = GffHelperBase.parseAttributeMap("=B=Y");
    assertTrue(map.isEmpty());
    // first token should be an attribute name only, no commas
    map = GffHelperBase.parseAttributeMap("A,B=C");
    assertTrue(map.isEmpty());
    // intermediate tokens need at least one comma (value,name=)
    map = GffHelperBase.parseAttributeMap("A=B=C");
    assertTrue(map.isEmpty());
    // last token may have a comma or not
    map = GffHelperBase.parseAttributeMap("A=B");
    assertEquals(map.get("A"), "B");
    map = GffHelperBase.parseAttributeMap("A=B,C");
    assertEquals(map.get("A"), "B,C");
    map = GffHelperBase.parseAttributeMap("A");
    assertTrue(map.isEmpty());
    map = GffHelperBase.parseAttributeMap("A=");
    assertTrue(map.isEmpty());
    map = GffHelperBase.parseAttributeMap("A==C");
    assertTrue(map.isEmpty());
    map = GffHelperBase.parseAttributeMap("=A");
    assertTrue(map.isEmpty());
    map = GffHelperBase.parseAttributeMap("=");
    assertTrue(map.isEmpty());
    map = GffHelperBase.parseAttributeMap(",");
    assertTrue(map.isEmpty());
    map = GffHelperBase.parseAttributeMap(" ");
    assertTrue(map.isEmpty());
    map = GffHelperBase.parseAttributeMap("");
    assertTrue(map.isEmpty());
    map = GffHelperBase.parseAttributeMap("A=B, =C");
    assertTrue(map.isEmpty());

    try
    {
      GffHelperBase.parseAttributeMap(null);
      fail("expected exception");
    } catch (NullPointerException e)
    {
      // expected
    }
  }
}
