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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.api.FeatureColourI;
import jalview.bin.Cache;
import jalview.bin.Jalview;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.datamodel.features.FeatureMatcher;
import jalview.datamodel.features.FeatureMatcherSet;
import jalview.datamodel.features.FeatureMatcherSetI;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;
import jalview.schemes.FeatureColour;
import jalview.schemes.FeatureColourTest;
import jalview.util.matcher.Condition;
import jalview.viewmodel.seqfeatures.FeatureRendererModel;

public class FeatureSettingsTest
{
  @BeforeClass(alwaysRun = true)
  public static void setUpBeforeClass() throws Exception
  {
    /*
     * use read-only test properties file
     */
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    Jalview.main(new String[] { "-nonews" });
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown()
  {
    if (Desktop.instance != null)
      Desktop.instance.closeAll_actionPerformed(null);
  }

  /**
   * Test a roundtrip of save and reload of feature colours and filters as XML
   * 
   * @throws IOException
   */
  @Test(groups = "Functional")
  public void testSaveLoad() throws IOException
  {
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            ">Seq1\nACDEFGHIKLM", DataSourceType.PASTE);
    SequenceI seq1 = af.getViewport().getAlignment().getSequenceAt(0);

    /*
     * add some features to the sequence
     */
    int score = 1;
    addFeatures(seq1, "type1", score++);
    addFeatures(seq1, "type2", score++);
    addFeatures(seq1, "type3", score++);
    addFeatures(seq1, "type4", score++);
    addFeatures(seq1, "type5", score++);

    /*
     * set colour schemes for features
     */
    FeatureRendererModel fr = af.getFeatureRenderer();

    // type1: red
    fr.setColour("type1", new FeatureColour(Color.red));

    // type2: by label
    FeatureColourI byLabel = new FeatureColour();
    byLabel.setColourByLabel(true);
    fr.setColour("type2", byLabel);

    // type3: by score above threshold
    FeatureColourI byScore = new FeatureColour(null, Color.BLACK,
            Color.BLUE, null, 1, 10);
    byScore.setAboveThreshold(true);
    byScore.setThreshold(2f);
    fr.setColour("type3", byScore);

    // type4: by attribute AF
    FeatureColourI byAF = new FeatureColour();
    byAF.setColourByLabel(true);
    byAF.setAttributeName("AF");
    fr.setColour("type4", byAF);

    // type5: by attribute CSQ:PolyPhen below threshold
    FeatureColourI byPolyPhen = new FeatureColour(null, Color.BLACK,
            Color.BLUE, null, 1, 10);
    byPolyPhen.setBelowThreshold(true);
    byPolyPhen.setThreshold(3f);
    byPolyPhen.setAttributeName("CSQ", "PolyPhen");
    fr.setColour("type5", byPolyPhen);

    /*
     * set filters for feature types
     */

    // filter type1 features by (label contains "x")
    FeatureMatcherSetI filterByX = new FeatureMatcherSet();
    filterByX.and(FeatureMatcher.byLabel(Condition.Contains, "x"));
    fr.setFeatureFilter("type1", filterByX);

    // filter type2 features by (score <= 2.4 and score > 1.1)
    FeatureMatcherSetI filterByScore = new FeatureMatcherSet();
    filterByScore.and(FeatureMatcher.byScore(Condition.LE, "2.4"));
    filterByScore.and(FeatureMatcher.byScore(Condition.GT, "1.1"));
    fr.setFeatureFilter("type2", filterByScore);

    // filter type3 features by (AF contains X OR CSQ:PolyPhen != 0)
    FeatureMatcherSetI filterByXY = new FeatureMatcherSet();
    filterByXY
            .and(FeatureMatcher.byAttribute(Condition.Contains, "X", "AF"));
    filterByXY.or(FeatureMatcher.byAttribute(Condition.NE, "0", "CSQ",
            "PolyPhen"));
    fr.setFeatureFilter("type3", filterByXY);

    /*
     * save colours and filters to an XML file
     */
    File coloursFile = File.createTempFile("testSaveLoad", ".fc");
    coloursFile.deleteOnExit();
    FeatureSettings fs = new FeatureSettings(af);
    fs.save(coloursFile);

    /*
     * change feature colours and filters
     */
    FeatureColourI pink = new FeatureColour(Color.pink);
    fr.setColour("type1", pink);
    fr.setColour("type2", pink);
    fr.setColour("type3", pink);
    fr.setColour("type4", pink);
    fr.setColour("type5", pink);

    FeatureMatcherSetI filter2 = new FeatureMatcherSet();
    filter2.and(FeatureMatcher.byLabel(Condition.NotContains, "y"));
    fr.setFeatureFilter("type1", filter2);
    fr.setFeatureFilter("type2", filter2);
    fr.setFeatureFilter("type3", filter2);
    fr.setFeatureFilter("type4", filter2);
    fr.setFeatureFilter("type5", filter2);

    /*
     * reload colours and filters from file and verify they are restored
     */
    fs.load(coloursFile);
    FeatureColourI fc = fr.getFeatureStyle("type1");
    assertTrue(fc.isSimpleColour());
    assertEquals(fc.getColour(), Color.red);
    fc = fr.getFeatureStyle("type2");
    assertTrue(fc.isColourByLabel());
    fc = fr.getFeatureStyle("type3");
    assertTrue(fc.isGraduatedColour());
    assertNull(fc.getAttributeName());
    assertTrue(fc.isAboveThreshold());
    assertEquals(fc.getThreshold(), 2f);
    fc = fr.getFeatureStyle("type4");
    assertTrue(fc.isColourByLabel());
    assertTrue(fc.isColourByAttribute());
    assertEquals(fc.getAttributeName(), new String[] { "AF" });
    fc = fr.getFeatureStyle("type5");
    assertTrue(fc.isGraduatedColour());
    assertTrue(fc.isColourByAttribute());
    assertEquals(fc.getAttributeName(), new String[] { "CSQ", "PolyPhen" });
    assertTrue(fc.isBelowThreshold());
    assertEquals(fc.getThreshold(), 3f);

    assertEquals(fr.getFeatureFilter("type1").toStableString(),
            "Label Contains x");
    assertEquals(fr.getFeatureFilter("type2").toStableString(),
            "(Score LE 2.4) AND (Score GT 1.1)");
    assertEquals(fr.getFeatureFilter("type3").toStableString(),
            "(AF Contains X) OR (CSQ:PolyPhen NE 0)");
  }

  /**
   * Adds two features of the given type to the given sequence, also setting the
   * score as the value of attribute "AF" and sub-attribute "CSQ:PolyPhen"
   * 
   * @param seq
   * @param featureType
   * @param score
   */
  private void addFeatures(SequenceI seq, String featureType, int score)
  {
    addFeature(seq, featureType, score++);
    addFeature(seq, featureType, score);
  }

  private void addFeature(SequenceI seq, String featureType, int score)
  {
    SequenceFeature sf = new SequenceFeature(featureType, "desc", 1, 2,
            score, "grp");
    sf.setValue("AF", score);
    sf.setValue("CSQ", new HashMap<String, String>()
    {
      {
        put("PolyPhen", Integer.toString(score));
      }
    });
    seq.addSequenceFeature(sf);
  }

  /**
   * @see FeatureColourTest#testGetDescription()
   * @throws IOException
   */
  @Test(groups = "Functional")
  public void testGetColorTooltip() throws IOException
  {
    assertNull(FeatureSettings.getColorTooltip(null, false));

    /*
     * simple colour
     */
    FeatureColourI fc = new FeatureColour(Color.black);
    String simpleTooltip = "Click to edit, right-click for menu";
    assertEquals(FeatureSettings.getColorTooltip(fc, true), simpleTooltip);
    assertNull(FeatureSettings.getColorTooltip(fc, false));

    /*
     * graduated colour tooltip includes description of colour
     */
    fc.setColourByLabel(true);
    assertEquals(FeatureSettings.getColorTooltip(fc, false),
            "<html>By Label</html>");
    assertEquals(FeatureSettings.getColorTooltip(fc, true),
            "<html>By Label<br>" + simpleTooltip + "</br></html>");

    /*
     * graduated colour with threshold is html-encoded
     */
    fc = new FeatureColour(null, Color.red, Color.blue, null, 2f, 10f);
    fc.setBelowThreshold(true);
    fc.setThreshold(4f);
    assertEquals(FeatureSettings.getColorTooltip(fc, false),
            "<html>By Score (&lt; 4.0)</html>");
    assertEquals(FeatureSettings.getColorTooltip(fc, true),
            "<html>By Score (&lt; 4.0)<br>" + simpleTooltip
                    + "</br></html>");

    fc.setAboveThreshold(true);
    assertEquals(FeatureSettings.getColorTooltip(fc, false),
            "<html>By Score (&gt; 4.0)</html>");
    assertEquals(FeatureSettings.getColorTooltip(fc, true),
            "<html>By Score (&gt; 4.0)<br>" + simpleTooltip
                    + "</br></html>");
  }
}
