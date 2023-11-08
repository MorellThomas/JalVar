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
package jalview.schemes;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import jalview.api.FeatureColourI;
import jalview.datamodel.SequenceFeature;
import jalview.gui.JvOptionPane;
import jalview.util.ColorUtils;
import jalview.util.Format;

import java.awt.Color;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FeatureColourTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testConstructors()
  {
    FeatureColourI fc = new FeatureColour();
    assertNull(fc.getColour());
    assertTrue(fc.isSimpleColour());
    assertFalse(fc.isColourByLabel());
    assertFalse(fc.isGraduatedColour());
    assertFalse(fc.isColourByAttribute());
    assertEquals(Color.white, fc.getMinColour());
    assertEquals(Color.black, fc.getMaxColour());

    fc = new FeatureColour(Color.RED);
    assertEquals(Color.red, fc.getColour());
    assertTrue(fc.isSimpleColour());
    assertFalse(fc.isColourByLabel());
    assertFalse(fc.isGraduatedColour());
    assertFalse(fc.isColourByAttribute());
    assertEquals(ColorUtils.bleachColour(Color.RED, 0.9f),
            fc.getMinColour());
    assertEquals(Color.RED, fc.getMaxColour());

  }

  @Test(groups = { "Functional" })
  public void testCopyConstructor()
  {
    /*
     * plain colour
     */
    FeatureColour fc = new FeatureColour(Color.RED);
    FeatureColour fc1 = new FeatureColour(fc);
    assertTrue(fc1.getColour().equals(Color.RED));
    assertFalse(fc1.isGraduatedColour());
    assertFalse(fc1.isColourByLabel());
    assertFalse(fc1.isColourByAttribute());
    assertNull(fc1.getAttributeName());

    /*
     * min-max colour
     */
    fc = new FeatureColour(null, Color.gray, Color.black, Color.gray, 10f,
            20f);
    fc.setAboveThreshold(true);
    fc.setThreshold(12f);
    fc1 = new FeatureColour(fc);
    assertTrue(fc1.isGraduatedColour());
    assertFalse(fc1.isColourByLabel());
    assertTrue(fc1.isAboveThreshold());
    assertFalse(fc1.isColourByAttribute());
    assertNull(fc1.getAttributeName());
    assertEquals(12f, fc1.getThreshold());
    assertEquals(Color.gray, fc1.getMinColour());
    assertEquals(Color.black, fc1.getMaxColour());
    assertEquals(Color.gray, fc1.getNoColour());
    assertEquals(10f, fc1.getMin());
    assertEquals(20f, fc1.getMax());

    /*
     * min-max-noValue colour
     */
    fc = new FeatureColour(Color.red, Color.gray, Color.black, Color.green,
            10f, 20f);
    fc.setAboveThreshold(true);
    fc.setThreshold(12f);
    fc1 = new FeatureColour(fc);
    assertTrue(fc1.isGraduatedColour());
    assertFalse(fc1.isColourByLabel());
    assertFalse(fc1.isSimpleColour());
    assertFalse(fc1.isColourByAttribute());
    assertNull(fc1.getAttributeName());
    assertTrue(fc1.isAboveThreshold());
    assertEquals(12f, fc1.getThreshold());
    assertEquals(Color.gray, fc1.getMinColour());
    assertEquals(Color.black, fc1.getMaxColour());
    assertEquals(Color.green, fc1.getNoColour());
    assertEquals(Color.red, fc1.getColour());
    assertEquals(10f, fc1.getMin());
    assertEquals(20f, fc1.getMax());

    /*
     * colour by label
     */
    fc = new FeatureColour();
    fc.setColourByLabel(true);
    fc1 = new FeatureColour(fc);
    assertTrue(fc1.isColourByLabel());
    assertFalse(fc1.isGraduatedColour());
    assertFalse(fc1.isColourByAttribute());
    assertNull(fc1.getAttributeName());

    /*
     * colour by attribute (label)
     */
    fc = new FeatureColour();
    fc.setColourByLabel(true);
    fc.setAttributeName("AF");
    fc1 = new FeatureColour(fc);
    assertTrue(fc1.isColourByLabel());
    assertFalse(fc1.isGraduatedColour());
    assertTrue(fc1.isColourByAttribute());
    assertArrayEquals(new String[] { "AF" }, fc1.getAttributeName());

    /*
     * colour by attribute (value)
     */
    fc = new FeatureColour(Color.yellow, Color.gray, Color.black,
            Color.green, 10f, 20f);
    fc.setAboveThreshold(true);
    fc.setThreshold(12f);
    fc.setAttributeName("AF");
    fc1 = new FeatureColour(fc);
    assertTrue(fc1.isGraduatedColour());
    assertFalse(fc1.isColourByLabel());
    assertTrue(fc1.isColourByAttribute());
    assertFalse(fc1.isSimpleColour());
    assertArrayEquals(new String[] { "AF" }, fc1.getAttributeName());
    assertTrue(fc1.isAboveThreshold());
    assertEquals(12f, fc1.getThreshold());
    assertEquals(Color.gray, fc1.getMinColour());
    assertEquals(Color.black, fc1.getMaxColour());
    assertEquals(Color.green, fc1.getNoColour());
    assertEquals(Color.yellow, fc1.getColour());
    assertEquals(10f, fc1.getMin());
    assertEquals(20f, fc1.getMax());

    /*
     * modify original attribute label and check that copy doesn't change
     */
    fc.setAttributeName("MAF", "AF");
    assertArrayEquals(new String[] { "AF" }, fc1.getAttributeName());

  }

  @Test(groups = { "Functional" })
  public void testGetColor_simpleColour()
  {
    FeatureColour fc = new FeatureColour(Color.RED);
    assertEquals(Color.RED,
            fc.getColor(new SequenceFeature("Cath", "", 1, 2, 0f, null)));
  }

  @Test(groups = { "Functional" })
  public void testGetColor_colourByLabel()
  {
    FeatureColour fc = new FeatureColour();
    fc.setColourByLabel(true);
    SequenceFeature sf = new SequenceFeature("type", "desc", 0, 20, 1f,
            null);
    Color expected = ColorUtils.createColourFromName("desc");
    assertEquals(expected, fc.getColor(sf));
  }

  @Test(groups = { "Functional" })
  public void testGetColor_Graduated()
  {
    /*
     * graduated colour from 
     * score 0 to 100
     * gray(128, 128, 128) to red(255, 0, 0)
     */
    FeatureColour fc = new FeatureColour(null, Color.GRAY, Color.RED, null,
            0f, 100f);
    // feature score is 75 which is 3/4 of the way from GRAY to RED
    SequenceFeature sf = new SequenceFeature("type", "desc", 0, 20, 75f,
            null);
    // the colour gradient is computed in float values from 0-1 (where 1 == 255)
    float red = 128 / 255f + 3 / 4f * (255 - 128) / 255f;
    float green = 128 / 255f + 3 / 4f * (0 - 128) / 255f;
    float blue = 128 / 255f + 3 / 4f * (0 - 128) / 255f;
    Color expected = new Color(red, green, blue);
    assertEquals(expected, fc.getColor(sf));
  }

  @Test(groups = { "Functional" })
  public void testGetColor_aboveBelowThreshold()
  {
    // gradient from [50, 150] from WHITE(255, 255, 255) to BLACK(0, 0, 0)
    FeatureColour fc = new FeatureColour(null, Color.WHITE, Color.BLACK,
            Color.white, 50f, 150f);
    SequenceFeature sf = new SequenceFeature("type", "desc", 0, 20, 70f,
            null);

    /*
     * feature with score of Float.NaN is always assigned minimum colour
     */
    SequenceFeature sf2 = new SequenceFeature("type", "desc", 0, 20,
            Float.NaN, null);

    fc.setThreshold(100f); // ignore for now
    assertEquals(new Color(204, 204, 204), fc.getColor(sf));
    assertEquals(Color.white, fc.getColor(sf2));

    fc.setAboveThreshold(true); // feature lies below threshold
    assertNull(fc.getColor(sf));
    assertEquals(Color.white, fc.getColor(sf2));

    fc.setBelowThreshold(true);
    fc.setThreshold(70f);
    assertNull(fc.getColor(sf)); // feature score == threshold - hidden
    assertEquals(Color.white, fc.getColor(sf2));
    fc.setThreshold(69f);
    assertNull(fc.getColor(sf)); // feature score > threshold - hidden
    assertEquals(Color.white, fc.getColor(sf2));
  }

  /**
   * Test output of feature colours to Jalview features file format
   */
  @Test(groups = { "Functional" })
  public void testToJalviewFormat()
  {
    /*
     * plain colour - to RGB hex code
     */
    FeatureColour fc = new FeatureColour(Color.RED);
    String redHex = Format.getHexString(Color.RED);
    String hexColour = redHex;
    assertEquals("domain\t" + hexColour, fc.toJalviewFormat("domain"));

    /*
     * colour by label (no threshold)
     */
    fc = new FeatureColour();
    fc.setColourByLabel(true);
    assertEquals("domain\tlabel", fc.toJalviewFormat("domain"));

    /*
     * colour by attribute text (no threshold)
     */
    fc = new FeatureColour();
    fc.setColourByLabel(true);
    fc.setAttributeName("CLIN_SIG");
    assertEquals("domain\tattribute|CLIN_SIG",
            fc.toJalviewFormat("domain"));

    /*
     * colour by label (autoscaled) (an odd state you can reach by selecting
     * 'above threshold', then deselecting 'threshold is min/max' then 'colour
     * by label')
     */
    fc.setAttributeName((String[]) null);
    fc.setAutoScaled(true);
    assertEquals("domain\tlabel", fc.toJalviewFormat("domain"));

    /*
     * colour by label (above threshold) 
     */
    fc.setAutoScaled(false);
    fc.setThreshold(12.5f);
    fc.setAboveThreshold(true);
    // min/max values are output though not used by this scheme
    assertEquals("domain\tlabel|||0.0|0.0|above|12.5",
            fc.toJalviewFormat("domain"));

    /*
     * colour by label (below threshold)
     */
    fc.setBelowThreshold(true);
    assertEquals("domain\tlabel|||0.0|0.0|below|12.5",
            fc.toJalviewFormat("domain"));

    /*
     * colour by attributes text (below threshold)
     */
    fc.setBelowThreshold(true);
    fc.setAttributeName("CSQ", "Consequence");
    assertEquals("domain\tattribute|CSQ:Consequence|||0.0|0.0|below|12.5",
            fc.toJalviewFormat("domain"));

    /*
     * graduated colour by score, no threshold
     * - default constructor sets noValueColor = minColor
     */
    fc = new FeatureColour(null, Color.GREEN, Color.RED, Color.GREEN, 12f,
            25f);
    String greenHex = Format.getHexString(Color.GREEN);
    String expected = String.format(
            "domain\tscore|%s|%s|noValueMin|abso|12.0|25.0|none", greenHex,
            redHex);
    assertEquals(expected, fc.toJalviewFormat("domain"));

    /*
     * graduated colour by score, no threshold, no value gets min colour
     */
    fc = new FeatureColour(Color.RED, Color.GREEN, Color.RED, Color.GREEN,
            12f, 25f);
    expected = String.format(
            "domain\tscore|%s|%s|noValueMin|abso|12.0|25.0|none", greenHex,
            redHex);
    assertEquals(expected, fc.toJalviewFormat("domain"));

    /*
     * graduated colour by score, no threshold, no value gets max colour
     */
    fc = new FeatureColour(Color.RED, Color.GREEN, Color.RED, Color.RED,
            12f, 25f);
    expected = String.format(
            "domain\tscore|%s|%s|noValueMax|abso|12.0|25.0|none", greenHex,
            redHex);
    assertEquals(expected, fc.toJalviewFormat("domain"));

    /*
     * colour ranges over the actual score ranges (not min/max)
     */
    fc.setAutoScaled(true);
    expected = String.format(
            "domain\tscore|%s|%s|noValueMax|12.0|25.0|none", greenHex,
            redHex);
    assertEquals(expected, fc.toJalviewFormat("domain"));

    /*
     * graduated colour by score, below threshold
     */
    fc.setThreshold(12.5f);
    fc.setBelowThreshold(true);
    expected = String.format(
            "domain\tscore|%s|%s|noValueMax|12.0|25.0|below|12.5", greenHex,
            redHex);
    assertEquals(expected, fc.toJalviewFormat("domain"));

    /*
     * graduated colour by score, above threshold
     */
    fc.setThreshold(12.5f);
    fc.setAboveThreshold(true);
    fc.setAutoScaled(false);
    expected = String.format(
            "domain\tscore|%s|%s|noValueMax|abso|12.0|25.0|above|12.5",
            greenHex, redHex);
    assertEquals(expected, fc.toJalviewFormat("domain"));

    /*
     * graduated colour by attribute, above threshold
     */
    fc.setAttributeName("CSQ", "AF");
    fc.setAboveThreshold(true);
    fc.setAutoScaled(false);
    expected = String.format(
            "domain\tattribute|CSQ:AF|%s|%s|noValueMax|abso|12.0|25.0|above|12.5",
            greenHex, redHex);
    assertEquals(expected, fc.toJalviewFormat("domain"));
  }

  /**
   * Test parsing of feature colours from Jalview features file format
   */
  @Test(groups = { "Functional" })
  public void testParseJalviewFeatureColour()
  {
    /*
     * simple colour by name
     */
    FeatureColourI fc = FeatureColour.parseJalviewFeatureColour("red");
    assertTrue(fc.isSimpleColour());
    assertEquals(Color.RED, fc.getColour());

    /*
     * simple colour by hex code
     */
    fc = FeatureColour
            .parseJalviewFeatureColour(Format.getHexString(Color.RED));
    assertTrue(fc.isSimpleColour());
    assertEquals(Color.RED, fc.getColour());

    /*
     * simple colour by rgb triplet
     */
    fc = FeatureColour.parseJalviewFeatureColour("255,0,0");
    assertTrue(fc.isSimpleColour());
    assertEquals(Color.RED, fc.getColour());

    /*
     * malformed colour
     */
    try
    {
      fc = FeatureColour.parseJalviewFeatureColour("oops");
      fail("expected exception");
    } catch (IllegalArgumentException e)
    {
      assertEquals("Invalid colour descriptor: oops", e.getMessage());
    }

    /*
     * colour by label (no threshold)
     */
    fc = FeatureColour.parseJalviewFeatureColour("label");
    assertTrue(fc.isColourByLabel());
    assertFalse(fc.hasThreshold());

    /*
     * colour by label (with threshold)
     */
    fc = FeatureColour
            .parseJalviewFeatureColour("label|||0.0|0.0|above|12.0");
    assertTrue(fc.isColourByLabel());
    assertTrue(fc.isAboveThreshold());
    assertEquals(12.0f, fc.getThreshold());

    /*
     * colour by attribute text (no threshold)
     */
    fc = FeatureColour.parseJalviewFeatureColour("attribute|CLIN_SIG");
    assertTrue(fc.isColourByAttribute());
    assertTrue(fc.isColourByLabel());
    assertFalse(fc.hasThreshold());
    assertArrayEquals(new String[] { "CLIN_SIG" }, fc.getAttributeName());

    /*
     * colour by attributes text (with score threshold)
     */
    fc = FeatureColour.parseJalviewFeatureColour(
            "attribute|CSQ:Consequence|||0.0|0.0|above|12.0");
    assertTrue(fc.isColourByLabel());
    assertTrue(fc.isColourByAttribute());
    assertArrayEquals(new String[] { "CSQ", "Consequence" },
            fc.getAttributeName());
    assertTrue(fc.isAboveThreshold());
    assertEquals(12.0f, fc.getThreshold());

    /*
     * graduated colour by score (with colour names) (no threshold)
     */
    fc = FeatureColour.parseJalviewFeatureColour("red|green|10.0|20.0");
    assertTrue(fc.isGraduatedColour());
    assertFalse(fc.hasThreshold());
    assertEquals(Color.RED, fc.getMinColour());
    assertEquals(Color.GREEN, fc.getMaxColour());
    assertEquals(Color.RED, fc.getNoColour());
    assertEquals(10f, fc.getMin());
    assertEquals(20f, fc.getMax());
    assertTrue(fc.isAutoScaled());

    /*
     * the same, with 'no value colour' specified as max
     */
    fc = FeatureColour
            .parseJalviewFeatureColour("red|green|novaluemax|10.0|20.0");
    assertEquals(Color.RED, fc.getMinColour());
    assertEquals(Color.GREEN, fc.getMaxColour());
    assertEquals(Color.GREEN, fc.getNoColour());
    assertEquals(10f, fc.getMin());
    assertEquals(20f, fc.getMax());

    /*
     * the same, with 'no value colour' specified as min
     */
    fc = FeatureColour
            .parseJalviewFeatureColour("red|green|novalueMin|10.0|20.0");
    assertEquals(Color.RED, fc.getMinColour());
    assertEquals(Color.GREEN, fc.getMaxColour());
    assertEquals(Color.RED, fc.getNoColour());
    assertEquals(10f, fc.getMin());
    assertEquals(20f, fc.getMax());

    /*
     * the same, with 'no value colour' specified as none
     */
    fc = FeatureColour
            .parseJalviewFeatureColour("red|green|novaluenone|10.0|20.0");
    assertEquals(Color.RED, fc.getMinColour());
    assertEquals(Color.GREEN, fc.getMaxColour());
    assertNull(fc.getNoColour());
    assertEquals(10f, fc.getMin());
    assertEquals(20f, fc.getMax());

    /*
     * the same, with invalid 'no value colour'
     */
    try
    {
      fc = FeatureColour
              .parseJalviewFeatureColour("red|green|blue|10.0|20.0");
      fail("expected exception");
    } catch (IllegalArgumentException e)
    {
      assertEquals(
              "Couldn't parse the minimum value for graduated colour ('blue')",
              e.getMessage());
    }

    /*
     * graduated colour (explicitly by 'score') (no threshold)
     */
    fc = FeatureColour
            .parseJalviewFeatureColour("Score|red|green|10.0|20.0");
    assertTrue(fc.isGraduatedColour());
    assertFalse(fc.hasThreshold());
    assertEquals(Color.RED, fc.getMinColour());
    assertEquals(Color.GREEN, fc.getMaxColour());
    assertEquals(10f, fc.getMin());
    assertEquals(20f, fc.getMax());
    assertTrue(fc.isAutoScaled());

    /*
     * graduated colour by attribute (no threshold)
     */
    fc = FeatureColour
            .parseJalviewFeatureColour("attribute|AF|red|green|10.0|20.0");
    assertTrue(fc.isGraduatedColour());
    assertTrue(fc.isColourByAttribute());
    assertArrayEquals(new String[] { "AF" }, fc.getAttributeName());
    assertFalse(fc.hasThreshold());
    assertEquals(Color.RED, fc.getMinColour());
    assertEquals(Color.GREEN, fc.getMaxColour());
    assertEquals(10f, fc.getMin());
    assertEquals(20f, fc.getMax());
    assertTrue(fc.isAutoScaled());

    /*
     * graduated colour by score (colours by hex code) (above threshold)
     */
    String descriptor = String.format("%s|%s|10.0|20.0|above|15",
            Format.getHexString(Color.RED),
            Format.getHexString(Color.GREEN));
    fc = FeatureColour.parseJalviewFeatureColour(descriptor);
    assertTrue(fc.isGraduatedColour());
    assertTrue(fc.hasThreshold());
    assertTrue(fc.isAboveThreshold());
    assertEquals(15f, fc.getThreshold());
    assertEquals(Color.RED, fc.getMinColour());
    assertEquals(Color.GREEN, fc.getMaxColour());
    assertEquals(10f, fc.getMin());
    assertEquals(20f, fc.getMax());
    assertTrue(fc.isAutoScaled());

    /*
     * graduated colour by attributes (below threshold)
     */
    fc = FeatureColour.parseJalviewFeatureColour(
            "attribute|CSQ:AF|red|green|10.0|20.0|below|13");
    assertTrue(fc.isGraduatedColour());
    assertTrue(fc.isColourByAttribute());
    assertArrayEquals(new String[] { "CSQ", "AF" }, fc.getAttributeName());
    assertTrue(fc.hasThreshold());
    assertTrue(fc.isBelowThreshold());
    assertEquals(13f, fc.getThreshold());
    assertEquals(Color.RED, fc.getMinColour());
    assertEquals(Color.GREEN, fc.getMaxColour());
    assertEquals(10f, fc.getMin());
    assertEquals(20f, fc.getMax());
    assertTrue(fc.isAutoScaled());

    /*
     * graduated colour (by RGB triplet) (below threshold), absolute scale
     */
    descriptor = "255,0,0|0,255,0|abso|10.0|20.0|below|15";
    fc = FeatureColour.parseJalviewFeatureColour(descriptor);
    assertTrue(fc.isGraduatedColour());
    assertFalse(fc.isAutoScaled());
    assertTrue(fc.hasThreshold());
    assertTrue(fc.isBelowThreshold());
    assertEquals(15f, fc.getThreshold());
    assertEquals(Color.RED, fc.getMinColour());
    assertEquals(Color.GREEN, fc.getMaxColour());
    assertEquals(10f, fc.getMin());
    assertEquals(20f, fc.getMax());

    descriptor = "blue|255,0,255|absolute|20.0|95.0|below|66.0";
    fc = FeatureColour.parseJalviewFeatureColour(descriptor);
    assertTrue(fc.isGraduatedColour());
  }

  @Test(groups = { "Functional" })
  public void testGetColor_colourByAttributeText()
  {
    FeatureColour fc = new FeatureColour();
    fc.setColourByLabel(true);
    fc.setAttributeName("consequence");
    SequenceFeature sf = new SequenceFeature("type", "desc", 0, 20, 1f,
            null);

    /*
     * if feature has no such attribute, use 'no value' colour
     */
    assertEquals(FeatureColour.DEFAULT_NO_COLOUR, fc.getColor(sf));

    /*
     * if feature has attribute, generate colour from value
     */
    sf.setValue("consequence", "benign");
    Color expected = ColorUtils.createColourFromName("benign");
    assertEquals(expected, fc.getColor(sf));
  }

  @Test(groups = { "Functional" })
  public void testGetColor_GraduatedByAttributeValue()
  {
    /*
     * graduated colour based on attribute value for AF
     * given a min-max range of 0-100
     */
    FeatureColour fc = new FeatureColour(Color.white,
            new Color(50, 100, 150), new Color(150, 200, 250), Color.yellow,
            0f, 100f);
    String attName = "AF";
    fc.setAttributeName(attName);

    /*
     * first case: feature lacks the attribute - use 'no value' colour
     */
    SequenceFeature sf = new SequenceFeature("type", "desc", 0, 20, 75f,
            null);
    assertEquals(Color.yellow, fc.getColor(sf));

    /*
     * second case: attribute present but not numeric - treat as if absent
     */
    sf.setValue(attName, "twelve");
    assertEquals(Color.yellow, fc.getColor(sf));

    /*
     * third case: valid attribute value
     */
    sf.setValue(attName, "20.0");
    Color expected = new Color(70, 120, 170);
    assertEquals(expected, fc.getColor(sf));
  }

  @Test(groups = { "Functional" })
  public void testIsOutwithThreshold()
  {
    FeatureColourI fc = new FeatureColour(Color.red);
    SequenceFeature sf = new SequenceFeature("METAL", "desc", 10, 12, 1.2f,
            "grp");
    assertFalse(fc.isOutwithThreshold(null));
    assertFalse(fc.isOutwithThreshold(sf));

    fc = new FeatureColour(null, Color.white, Color.black, Color.green, 0f,
            10f);
    assertFalse(fc.isOutwithThreshold(sf)); // no threshold

    fc.setAboveThreshold(true);
    fc.setThreshold(1f);
    assertFalse(fc.isOutwithThreshold(sf)); // feature score 1.2 is above 1

    fc.setThreshold(2f);
    assertTrue(fc.isOutwithThreshold(sf)); // feature score 1.2 is not above 2

    fc.setBelowThreshold(true);
    assertFalse(fc.isOutwithThreshold(sf)); // feature score 1.2 is below 2

    fc.setThreshold(1f);
    assertTrue(fc.isOutwithThreshold(sf)); // feature score 1.2 is not below 1

    /*
     * with attribute value threshold
     */
    fc.setAttributeName("AC");
    assertFalse(fc.isOutwithThreshold(sf)); // missing attribute AC is ignored

    sf.setValue("AC", "-1");
    assertFalse(fc.isOutwithThreshold(sf)); // value -1 is below 1

    sf.setValue("AC", "1");
    assertTrue(fc.isOutwithThreshold(sf)); // value 1 is not below 1

    sf.setValue("AC", "junk");
    assertFalse(fc.isOutwithThreshold(sf)); // bad value is ignored
  }

  /**
   * Test description of feature colour suitable for a tooltip
   */
  @Test(groups = { "Functional" })
  public void testGetDescription()
  {
    /*
     * plain colour
     */
    FeatureColour fc = new FeatureColour(Color.RED);
    assertEquals(
            String.format("r=%d,g=%d,b=%d", Color.RED.getRed(),
                    Color.red.getGreen(), Color.red.getBlue()),
            fc.getDescription());

    /*
     * colour by label (no threshold)
     */
    fc = new FeatureColour();
    fc.setColourByLabel(true);
    assertEquals("By Label", fc.getDescription());

    /*
     * colour by attribute text (no threshold)
     */
    fc = new FeatureColour();
    fc.setColourByLabel(true);
    fc.setAttributeName("CLIN_SIG");
    assertEquals("By CLIN_SIG", fc.getDescription());

    /*
     * colour by label (above score threshold) 
     */
    fc = new FeatureColour();
    fc.setColourByLabel(true);
    fc.setAutoScaled(false);
    fc.setThreshold(12.5f);
    fc.setAboveThreshold(true);
    assertEquals("By Label (Score > 12.5)", fc.getDescription());

    /*
     * colour by label (below score threshold)
     */
    fc.setBelowThreshold(true);
    assertEquals("By Label (Score < 12.5)", fc.getDescription());

    /*
     * colour by attributes text (below score threshold)
     */
    fc.setBelowThreshold(true);
    fc.setAttributeName("CSQ", "Consequence");
    assertEquals("By CSQ:Consequence (Score < 12.5)", fc.getDescription());

    /*
     * graduated colour by score, no threshold
     */
    fc = new FeatureColour(null, Color.GREEN, Color.RED, null, 12f, 25f);
    assertEquals("By Score", fc.getDescription());

    /*
     * graduated colour by score, below threshold
     */
    fc.setThreshold(12.5f);
    fc.setBelowThreshold(true);
    assertEquals("By Score (< 12.5)", fc.getDescription());

    /*
     * graduated colour by score, above threshold
     */
    fc.setThreshold(12.5f);
    fc.setAboveThreshold(true);
    fc.setAutoScaled(false);
    assertEquals("By Score (> 12.5)", fc.getDescription());

    /*
     * graduated colour by attribute, no threshold
     */
    fc.setAttributeName("CSQ", "AF");
    fc.setAboveThreshold(false);
    fc.setAutoScaled(false);
    assertEquals("By CSQ:AF", fc.getDescription());

    /*
     * graduated colour by attribute, above threshold
     */
    fc.setAboveThreshold(true);
    fc.setAutoScaled(false);
    assertEquals("By CSQ:AF (> 12.5)", fc.getDescription());
  }
}
