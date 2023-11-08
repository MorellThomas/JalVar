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
import static org.testng.AssertJUnit.assertTrue;

import jalview.gui.JvOptionPane;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class StringUtilsTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testInsertCharAt()
  {
    char[] c1 = "ABC".toCharArray();
    char[] expected = new char[] { 'A', 'B', 'C', 'w', 'w' };
    assertTrue(Arrays.equals(expected,
            StringUtils.insertCharAt(c1, 3, 2, 'w')));
    expected = new char[] { 'A', 'B', 'C', 'w', 'w' };
    assertTrue(Arrays.equals(expected,
            StringUtils.insertCharAt(c1, 4, 2, 'w')));
    assertTrue(Arrays.equals(expected,
            StringUtils.insertCharAt(c1, 5, 2, 'w')));
    assertTrue(Arrays.equals(expected,
            StringUtils.insertCharAt(c1, 6, 2, 'w')));
    assertTrue(Arrays.equals(expected,
            StringUtils.insertCharAt(c1, 7, 2, 'w')));
  }

  @Test(groups = { "Functional" })
  public void testDeleteChars()
  {
    char[] c1 = "ABC".toCharArray();

    // delete second position
    assertTrue(
            Arrays.equals(new char[]
            { 'A', 'C' }, StringUtils.deleteChars(c1, 1, 2)));

    // delete positions 1 and 2
    assertTrue(
            Arrays.equals(new char[]
            { 'C' }, StringUtils.deleteChars(c1, 0, 2)));

    // delete positions 1-3
    assertTrue(Arrays.equals(new char[] {},
            StringUtils.deleteChars(c1, 0, 3)));

    // delete position 3
    assertTrue(
            Arrays.equals(new char[]
            { 'A', 'B' }, StringUtils.deleteChars(c1, 2, 3)));

    // out of range deletion is ignore
    assertTrue(Arrays.equals(c1, StringUtils.deleteChars(c1, 3, 4)));
  }

  @Test(groups = { "Functional" })
  public void testGetLastToken()
  {
    assertNull(StringUtils.getLastToken(null, null));
    assertNull(StringUtils.getLastToken(null, "/"));
    assertEquals("a", StringUtils.getLastToken("a", null));

    assertEquals("abc", StringUtils.getLastToken("abc", "/"));
    assertEquals("c", StringUtils.getLastToken("abc", "b"));
    assertEquals("file1.dat", StringUtils.getLastToken(
            "file://localhost:8080/data/examples/file1.dat", "/"));
  }

  @Test(groups = { "Functional" })
  public void testSeparatorListToArray()
  {
    String[] result = StringUtils.separatorListToArray(
            "foo=',',min='foo',max='1,2,3',fa=','", ",");
    assertEquals("[foo=',', min='foo', max='1,2,3', fa=',']",
            Arrays.toString(result));
    /*
     * Comma nested in '' is not treated as delimiter; tokens are not trimmed
     */
    result = StringUtils.separatorListToArray("minsize='2', sep=','", ",");
    assertEquals("[minsize='2',  sep=',']", Arrays.toString(result));

    /*
     * String delimited by | containing a quoted | (should not be treated as
     * delimiter)
     */
    assertEquals("[abc='|'d, ef, g]", Arrays.toString(
            StringUtils.separatorListToArray("abc='|'d|ef|g", "|")));
  }

  @Test(groups = { "Functional" })
  public void testArrayToSeparatorList()
  {
    assertEquals("*", StringUtils.arrayToSeparatorList(null, "*"));
    assertEquals("*",
            StringUtils.arrayToSeparatorList(new String[] {}, "*"));
    assertEquals("a*bc*cde",
            StringUtils.arrayToSeparatorList(new String[]
            { "a", "bc", "cde" }, "*"));
    assertEquals("a*cde",
            StringUtils.arrayToSeparatorList(new String[]
            { "a", null, "cde" }, "*"));
    assertEquals("a**cde",
            StringUtils.arrayToSeparatorList(new String[]
            { "a", "", "cde" }, "*"));
    // delimiter within token is not (yet) escaped
    assertEquals("a*b*c*cde",
            StringUtils.arrayToSeparatorList(new String[]
            { "a", "b*c", "cde" }, "*"));
  }

  @Test(groups = { "Functional" })
  public void testListToDelimitedString()
  {
    assertEquals("", StringUtils.listToDelimitedString(null, ";"));
    List<String> list = new ArrayList<>();
    assertEquals("", StringUtils.listToDelimitedString(list, ";"));
    list.add("now");
    assertEquals("now", StringUtils.listToDelimitedString(list, ";"));
    list.add("is");
    assertEquals("now;is", StringUtils.listToDelimitedString(list, ";"));
    assertEquals("now ; is",
            StringUtils.listToDelimitedString(list, " ; "));
    list.add("the");
    list.add("winter");
    list.add("of");
    list.add("our");
    list.add("discontent");
    assertEquals("now is the winter of our discontent",
            StringUtils.listToDelimitedString(list, " "));
  }

  @Test(groups = { "Functional" })
  public void testParseInt()
  {
    assertEquals(0, StringUtils.parseInt(null));
    assertEquals(0, StringUtils.parseInt(""));
    assertEquals(0, StringUtils.parseInt("x"));
    assertEquals(0, StringUtils.parseInt("1.2"));
    assertEquals(33, StringUtils.parseInt("33"));
    assertEquals(33, StringUtils.parseInt("+33"));
    assertEquals(-123, StringUtils.parseInt("-123"));
    // too big for an int:
    assertEquals(0,
            StringUtils.parseInt(String.valueOf(Integer.MAX_VALUE) + "1"));
  }

  @Test(groups = { "Functional" })
  public void testCompareVersions()
  {
    assertEquals(0, StringUtils.compareVersions(null, null));
    assertEquals(0, StringUtils.compareVersions("2.8.3", null));

    /*
     * same version returns 0
     */
    assertEquals(0, StringUtils.compareVersions("2.8", "2.8"));
    assertEquals(0, StringUtils.compareVersions("2.8.3", "2.8.3"));
    assertEquals(0, StringUtils.compareVersions("2.8.3b1", "2.8.3b1", "b"));
    assertEquals(0, StringUtils.compareVersions("2.8.3B1", "2.8.3b1", "b"));
    assertEquals(0, StringUtils.compareVersions("2.8.3b1", "2.8.3B1", "b"));

    /*
     * v1 < v2 returns -1
     */
    assertEquals(-1, StringUtils.compareVersions("2.8.3", "2.8.4"));
    assertEquals(-1, StringUtils.compareVersions("2.8.3", "2.9"));
    assertEquals(-1, StringUtils.compareVersions("2.8.3", "2.9.2"));
    assertEquals(-1, StringUtils.compareVersions("2.8", "2.8.3"));
    assertEquals(-1, StringUtils.compareVersions("2.8.3", "2.8.3b1", "b"));
    assertEquals(-1,
            StringUtils.compareVersions("2.8.3b1", "2.8.3b2", "b"));
    assertEquals(-1, StringUtils.compareVersions("2.8", "2.8.0", "b"));
    assertEquals(-1, StringUtils.compareVersions("2", "12"));
    assertEquals(-1, StringUtils.compareVersions("3.2.4", "3.12.11"));

    /*
     * v1 > v2 returns +1
     */
    assertEquals(1, StringUtils.compareVersions("2.8.3", "2.8"));
    assertEquals(1, StringUtils.compareVersions("2.8.0", "2.8"));
    assertEquals(1, StringUtils.compareVersions("2.8.4", "2.8.3"));
    assertEquals(1, StringUtils.compareVersions("2.8.3b1", "2.8.3", "b"));
    assertEquals(1, StringUtils.compareVersions("2.8.3", "2.8.2b1", "b"));
    assertEquals(1, StringUtils.compareVersions("2.8.0b2", "2.8.0b1", "b"));
    assertEquals(1, StringUtils.compareVersions("12", "2"));
    assertEquals(1, StringUtils.compareVersions("3.12.11", "3.2.4"));
  }

  @Test(groups = { "Functional" })
  public void testToSentenceCase()
  {
    assertEquals("John", StringUtils.toSentenceCase("john"));
    assertEquals("John", StringUtils.toSentenceCase("JOHN"));
    assertEquals("John and james",
            StringUtils.toSentenceCase("JOHN and JAMES"));
    assertEquals("J", StringUtils.toSentenceCase("j"));
    assertEquals("", StringUtils.toSentenceCase(""));
    assertNull(StringUtils.toSentenceCase(null));
  }

  @Test(groups = { "Functional" })
  public void testStripHtmlTags()
  {
    assertNull(StringUtils.stripHtmlTags(null));
    assertEquals("", StringUtils.stripHtmlTags(""));
    assertEquals("<a href=\"something\">label</href>",
            StringUtils.stripHtmlTags(
                    "<html><a href=\"something\">label</href></html>"));

    // if no "<html>" tag, < and > get html-encoded (not sure why)
    assertEquals("&lt;a href=\"something\"&gt;label&lt;/href&gt;",
            StringUtils
                    .stripHtmlTags("<a href=\"something\">label</href>"));

    // </body> gets removed but not <body> (is this intentional?)
    assertEquals("<body><p>hello", StringUtils
            .stripHtmlTags("<html><body><p>hello</body></html>"));

    assertEquals("kdHydro &lt; 12.53",
            StringUtils.stripHtmlTags("kdHydro < 12.53"));
  }

  @Test(groups = { "Functional" })
  public void testUrlEncode()
  {
    // degenerate cases
    assertNull(StringUtils.urlEncode(null, ";,"));
    assertEquals("", StringUtils.urlEncode("", ""));
    assertEquals("", StringUtils.urlEncode("", ";,"));

    // sanity checks, see
    // https://en.wikipedia.org/wiki/Percent-encoding#Percent-encoding_reserved_characters
    assertEquals("+", StringUtils.urlEncode(" ", " "));
    assertEquals("%25", StringUtils.urlEncode("%", "%"));
    assertEquals(".", StringUtils.urlEncode(".", ".")); // note . is not encoded
    assertEquals("%3A", StringUtils.urlEncode(":", ":"));
    assertEquals("%3B", StringUtils.urlEncode(";", ";"));
    assertEquals("%3D", StringUtils.urlEncode("=", "="));
    assertEquals("%2C", StringUtils.urlEncode(",", ","));

    // check % does not get recursively encoded!
    assertEquals("a%25b%3Dc%3Bd%3Ae%2C%2C",
            StringUtils.urlEncode("a%b=c;d:e,,", "=,;:%"));

    // = not in the list for encoding
    assertEquals("a=b", StringUtils.urlEncode("a=b", ";,"));

    // encode = (as %3B) and ; (as %3D)
    assertEquals("a%3Db.c%3B", StringUtils.urlEncode("a=b.c;", ";=,"));

    // . and space not in the list for encoding
    assertEquals("a%3Db.c d", StringUtils.urlEncode("a=b.c d", ";=,"));

    // encode space also (as +)
    assertEquals("a%3Db.c+d", StringUtils.urlEncode("a=b.c d", ";=, "));

    // . does not get encoded even if requested - behaviour of URLEncoder
    assertEquals("a%3Db.c+d.e%3Df",
            StringUtils.urlEncode("a=b.c d.e=f", ";=,. "));
  }

  @Test(groups = { "Functional" })
  public void testUrlDecode()
  {
    // degenerate cases
    assertNull(StringUtils.urlDecode(null, ";,"));
    assertEquals("", StringUtils.urlDecode("", ""));
    assertEquals("", StringUtils.urlDecode("", ";,"));

    // = not in the list for encoding
    assertEquals("a%3Db", StringUtils.urlDecode("a%3Db", ";,"));

    // decode = and ; but not .
    assertEquals("a=b%3Ec; d",
            StringUtils.urlDecode("a%3Db%3Ec; d", ";=,"));

    // space not in the list for decoding
    assertEquals("a=b;c+d", StringUtils.urlDecode("a%3Db%3Bc+d", ";=,"));

    // decode space also; %3E is not decoded to .
    assertEquals("a=b%3Ec d=,",
            StringUtils.urlDecode("a%3Db%3Ec+d%3D%2C", ";=, "));

    // decode encoded % (%25)
    assertEquals("a,=;\t%z",
            StringUtils.urlDecode("a%2C%3D%3B%09%25z", ";=,\t%"));
  }
}
