/*******************************************************************************
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
 ******************************************************************************/
package jalview.util;

import static org.testng.AssertJUnit.assertEquals;

import jalview.gui.JvOptionPane;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ParseHtmlBodyAndLinksTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testParseHtml_noLinks()
  {
    ParseHtmlBodyAndLinks testee = new ParseHtmlBodyAndLinks(
            "<html>something here</html>", false, "\n");
    assertEquals("something here", testee.getContent());
    assertEquals("something here", testee.getNonHtmlContent());

    // second argument makes no difference??
    testee = new ParseHtmlBodyAndLinks("<html>something here</html>", true,
            "\n");
    assertEquals("something here", testee.getContent());
    assertEquals("something here", testee.getNonHtmlContent());
  }

  @Test(groups = { "Functional" })
  public void testParseHtml_withLinks()
  {
    ParseHtmlBodyAndLinks testee = new ParseHtmlBodyAndLinks(
            "<html>Please click <a href=\"http://www.nowhere.com\">on this</a> to learn more about <a href=\"http://www.somewhere.com/here\">this</a></html>",
            false, "\n");
    assertEquals(
            "Please click on this%LINK% to learn more about this%LINK%",
            testee.getContent());
    assertEquals(
            "Please click on this%LINK% to learn more about this%LINK%",
            testee.getNonHtmlContent());
    assertEquals(2, testee.getLinks().size());
    assertEquals("on this|http://www.nowhere.com",
            testee.getLinks().get(0));
    assertEquals("this|http://www.somewhere.com/here",
            testee.getLinks().get(1));
  }

  @Test(groups = { "Functional" })
  public void testParseHtml_withLinksWithParameters()
  {
    ParseHtmlBodyAndLinks testee = new ParseHtmlBodyAndLinks(
            "<html>Please click <a href=\"http://www.nowhere.com?id=234&taxon=human\">on this</a> to learn more</html>",
            false, "\n");
    assertEquals("Please click on this%LINK% to learn more",
            testee.getContent());
    assertEquals("Please click on this%LINK% to learn more",
            testee.getNonHtmlContent());
    assertEquals(1, testee.getLinks().size());
    assertEquals("on this|http://www.nowhere.com?id=234&taxon=human",
            testee.getLinks().get(0));
  }

  @Test(groups = { "Functional" })
  public void testParseHtml_withLinksWithEncoding()
  {
    ParseHtmlBodyAndLinks testee = new ParseHtmlBodyAndLinks(
            "<html>Please click <a href=\"http://www.nowhere.com?id=234&amp;taxon=human&amp;id&gt;3&amp;id&lt;10\">on this</a> to learn &amp;&lt;&gt;more</html>",
            false, "\n");
    // html encoding in the text body is translated
    assertEquals("Please click on this%LINK% to learn &<>more",
            testee.getContent());
    assertEquals("Please click on this%LINK% to learn &<>more",
            testee.getNonHtmlContent());
    assertEquals(1, testee.getLinks().size());
    // html encoding in the url links is not translated
    assertEquals(
            "on this|http://www.nowhere.com?id=234&amp;taxon=human&amp;id&gt;3&amp;id&lt;10",
            testee.getLinks().get(0));
  }
}
