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
import static org.testng.AssertJUnit.assertSame;

import jalview.gui.JvOptionPane;

import java.awt.Color;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ColorUtilsTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  Color paleColour = new Color(97, 203, 111); // pale green

  Color midColour = new Color(135, 57, 41); // mid red

  Color darkColour = new Color(11, 30, 50); // dark blue

  @Test(groups = { "Functional" })
  public void testDarkerThan()
  {
    assertEquals("Wrong darker shade", new Color(32, 69, 37),
            ColorUtils.darkerThan(paleColour));
    assertEquals("Wrong darker shade", new Color(45, 18, 13),
            ColorUtils.darkerThan(midColour));
    assertEquals("Wrong darker shade", new Color(2, 9, 16),
            ColorUtils.darkerThan(darkColour));
    assertNull(ColorUtils.darkerThan(null));
  }

  @Test(groups = { "Functional" })
  public void testBrighterThan()
  {
    assertEquals("Wrong brighter shade", new Color(255, 255, 255), // white
            ColorUtils.brighterThan(paleColour));
    assertEquals("Wrong brighter shade", new Color(255, 164, 117),
            ColorUtils.brighterThan(midColour));
    assertEquals("Wrong brighter shade", new Color(30, 85, 144),
            ColorUtils.brighterThan(darkColour));
    assertNull(ColorUtils.brighterThan(null));
  }

  /**
   * @see http://www.rtapo.com/notes/named_colors.html
   */
  @Test(groups = { "Functional" })
  public void testToTkCode()
  {
    assertEquals("#fffafa", ColorUtils.toTkCode(new Color(255, 250, 250))); // snow
    assertEquals("#e6e6fa", ColorUtils.toTkCode(new Color(230, 230, 250))); // lavender
    assertEquals("#dda0dd", ColorUtils.toTkCode(new Color(221, 160, 221))); // plum
    assertEquals("#800080", ColorUtils.toTkCode(new Color(128, 0, 128))); // purple
    assertEquals("#00ff00", ColorUtils.toTkCode(new Color(0, 255, 0))); // lime
  }

  @Test(groups = { "Functional" })
  public void testGetGraduatedColour()
  {
    Color minColour = new Color(100, 100, 100);
    Color maxColour = new Color(180, 200, 220);

    /*
     * value half-way between min and max
     */
    Color col = ColorUtils.getGraduatedColour(20f, 10f, minColour, 30f,
            maxColour);
    assertEquals(140, col.getRed());
    assertEquals(150, col.getGreen());
    assertEquals(160, col.getBlue());

    /*
     * value two-thirds of the way between min and max
     */
    col = ColorUtils.getGraduatedColour(30f, 10f, minColour, 40f,
            maxColour);
    assertEquals(153, col.getRed());
    // Color constructor rounds float value to nearest int
    assertEquals(167, col.getGreen());
    assertEquals(180, col.getBlue());

    /*
     * value = min
     */
    col = ColorUtils.getGraduatedColour(10f, 10f, minColour, 30f,
            maxColour);
    assertEquals(minColour, col);

    /*
     * value = max
     */
    col = ColorUtils.getGraduatedColour(30f, 10f, minColour, 30f,
            maxColour);
    assertEquals(maxColour, col);

    /*
     * value < min
     */
    col = ColorUtils.getGraduatedColour(0f, 10f, minColour, 30f, maxColour);
    assertEquals(minColour, col);

    /*
     * value > max
     */
    col = ColorUtils.getGraduatedColour(40f, 10f, minColour, 30f,
            maxColour);
    assertEquals(maxColour, col);

    /*
     * min = max
     */
    col = ColorUtils.getGraduatedColour(40f, 10f, minColour, 10f,
            maxColour);
    assertEquals(minColour, col);
  }

  @Test(groups = { "Functional" })
  public void testBleachColour()
  {
    Color colour = new Color(155, 105, 55);
    assertSame(colour, ColorUtils.bleachColour(colour, 0));
    assertEquals(Color.WHITE, ColorUtils.bleachColour(colour, 1));
    assertEquals(Color.WHITE, ColorUtils.bleachColour(colour, 2));
    assertEquals(new Color(175, 135, 95),
            ColorUtils.bleachColour(colour, 0.2f));
    assertEquals(new Color(225, 210, 195),
            ColorUtils.bleachColour(colour, 0.7f));

    /*
     * and some 'negative fade'
     */
    assertEquals(Color.BLACK, ColorUtils.bleachColour(colour, -1));
    assertEquals(Color.BLACK, ColorUtils.bleachColour(colour, -2));
    assertEquals(new Color(124, 84, 44),
            ColorUtils.bleachColour(colour, -0.2f));
    assertEquals(new Color(46, 31, 16), // with rounding down
            ColorUtils.bleachColour(colour, -0.7f));
  }

  @Test(groups = "Functional")
  public void testParseColourString()
  {
    /*
     * by colour name - if known to AWT, and included in
     * 
     * @see ColourSchemeProperty.getAWTColorFromName()
     */
    assertSame(Color.RED, ColorUtils.parseColourString("red"));
    assertSame(Color.RED, ColorUtils.parseColourString("Red"));
    assertSame(Color.RED, ColorUtils.parseColourString(" RED "));

    /*
     * by RGB hex code
     */
    String hexColour = Integer.toHexString(Color.RED.getRGB() & 0xffffff);
    assertEquals("ff0000", hexColour);
    assertEquals(Color.RED, ColorUtils.parseColourString(hexColour));
    // 'hex' prefixes _not_ wanted here
    assertNull(ColorUtils.parseColourString("0x" + hexColour));
    assertNull(ColorUtils.parseColourString("#" + hexColour));
    // out of range, but Color constructor just or's the rgb value with 0
    assertEquals(Color.black, ColorUtils.parseColourString("1000000"));

    /*
     * by RGB triplet
     */
    Color c = Color.pink;
    String rgb = String.format("%d,%d,%d", c.getRed(), c.getGreen(),
            c.getBlue());
    assertEquals("255,175,175", rgb);
    assertEquals(c, ColorUtils.parseColourString(rgb));
    assertEquals(c, ColorUtils.parseColourString("255, 175 , 175"));

    /*
     * odds and ends
     */
    assertNull(ColorUtils.parseColourString(null));
    assertNull(ColorUtils.parseColourString("rubbish"));
    assertEquals(Color.WHITE, ColorUtils.parseColourString("-1"));
    assertNull(ColorUtils
            .parseColourString(String.valueOf(Integer.MAX_VALUE)));
    assertNull(ColorUtils.parseColourString("100,200,300")); // out of range
    assertNull(ColorUtils.parseColourString("100,200")); // too few
    assertNull(ColorUtils.parseColourString("100,200,100,200")); // too many
  }

  @Test(groups = "Functional")
  public void testGetAWTColorFromName()
  {
    assertEquals(Color.white, ColorUtils.getAWTColorFromName("white"));
    assertEquals(Color.white, ColorUtils.getAWTColorFromName("White"));
    assertEquals(Color.white, ColorUtils.getAWTColorFromName("WHITE"));
    assertEquals(Color.pink, ColorUtils.getAWTColorFromName("pink"));
    assertNull(ColorUtils.getAWTColorFromName("mauve")); // no such name
    assertNull(ColorUtils.getAWTColorFromName(""));
    assertNull(ColorUtils.getAWTColorFromName(null));
  }

  @Test(groups = "Functional")
  public void testCreateColourFromName()
  {
    assertEquals(Color.white, ColorUtils.createColourFromName(null));
    assertEquals(new Color(20, 20, 20),
            ColorUtils.createColourFromName(""));
    assertEquals(new Color(98, 131, 171),
            ColorUtils.createColourFromName("None")); // no special treatment!
    assertEquals(new Color(123, 211, 122),
            ColorUtils.createColourFromName("hello world"));
    assertEquals(new Color(27, 147, 112),
            ColorUtils.createColourFromName("HELLO WORLD"));
    /*
     * the algorithm makes the same values for r,g,b if 
     * the string consists of 3 repeating substrings
     */
    assertEquals(new Color(184, 184, 184),
            ColorUtils.createColourFromName("HELLO HELLO HELLO "));
  }
}
