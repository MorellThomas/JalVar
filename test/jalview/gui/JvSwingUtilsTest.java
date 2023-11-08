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
package jalview.gui;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import javax.swing.JScrollBar;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class JvSwingUtilsTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testGetScrollBarProportion()
  {
    /*
     * orientation, value, extent (width), min, max
     */
    JScrollBar sb = new JScrollBar(0, 125, 50, 0, 450);

    /*
     * operating range is 25 - 425 (400 wide) so value 125 is 100/400ths of this
     * range
     */
    assertEquals(0.25f, JvSwingUtils.getScrollBarProportion(sb), 0.001f);
  }

  @Test(groups = { "Functional" })
  public void testGetScrollValueForProportion()
  {
    /*
     * orientation, value, extent (width), min, max
     */
    JScrollBar sb = new JScrollBar(0, 125, 50, 0, 450);

    /*
     * operating range is 25 - 425 (400 wide) so value 125 is a quarter of this
     * range
     */
    assertEquals(125, JvSwingUtils.getScrollValueForProportion(sb, 0.25f));
  }

  /**
   * Test wrap tooltip where it is less than or equal to 60 characters long - no
   * wrap should be applied
   */
  @Test(groups = { "Functional" })
  public void testWrapTooltip_shortText()
  {
    String tip = "hello world";
    assertEquals(tip, JvSwingUtils.wrapTooltip(false, tip));
    assertEquals("<html>" + tip + "</html>",
            JvSwingUtils.wrapTooltip(true, tip));

    tip = "012345678901234567890123456789012345678901234567890123456789"; // 60
    assertEquals(tip, JvSwingUtils.wrapTooltip(false, tip));
    assertEquals("<html>" + tip + "</html>",
            JvSwingUtils.wrapTooltip(true, tip));

    tip = "0123456789012345678901234567890123456789012345678901234567890"; // 61
    assertFalse(tip.equals(JvSwingUtils.wrapTooltip(false, tip)));
    assertFalse(("<html>" + tip + "</html>")
            .equals(JvSwingUtils.wrapTooltip(true, tip)));
  }

  /**
   * Test wrap tooltip where it is more than one line (separated by &lt;br&gt;
   * tags) of less than or equal to 60 characters long - no wrap should be
   * applied
   */
  @Test(groups = { "Functional" })
  public void testWrapTooltip_multilineShortText()
  {
    String tip = "Now is the winter of our discontent<br>Made glorious summer by this sun of York";
    assertEquals(tip, JvSwingUtils.wrapTooltip(false, tip));
    assertEquals("<html>" + tip + "</html>",
            JvSwingUtils.wrapTooltip(true, tip));
  }

  /**
   * Test wrap tooltip where it is more than 60 characters long - word break and
   * word wrap styling should be applied
   */
  @Test(groups = { "Functional" })
  public void testWrapTooltip_longText()
  {
    String tip = "Now is the winter of our discontent made glorious summer by this sun of York";
    String expected = "<style> div.ttip {width:350px;white-space:pre-wrap;padding:2px;overflow-wrap:break-word;}</style>"
            + "<div class=\"ttip\">" + tip + " </div>";
    assertEquals("<html>" + expected + "</html>",
            JvSwingUtils.wrapTooltip(true, tip));
    assertEquals(expected, JvSwingUtils.wrapTooltip(false, tip));
  }
}
