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
package jalview.renderer.seqfeatures;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.awt.Color;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import jalview.api.FeatureColourI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.AlignViewport;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;
import jalview.schemes.FeatureColour;
import jalview.viewmodel.seqfeatures.FeatureRendererModel;
import jalview.viewmodel.seqfeatures.FeatureRendererModel.FeatureSettingsBean;

/**
 * Unit tests for feature colour determination, including but not limited to
 * <ul>
 * <li>gap position</li>
 * <li>no features present</li>
 * <li>features present but show features turned off</li>
 * <li>features displayed but selected feature turned off</li>
 * <li>features displayed but feature group turned off</li>
 * <li>feature displayed but none at the specified position</li>
 * <li>multiple features at position, with no transparency</li>
 * <li>multiple features at position, with transparency</li>
 * <li>score graduated feature colour</li>
 * <li>contact feature start at the selected position</li>
 * <li>contact feature end at the selected position</li>
 * <li>contact feature straddling the selected position (not shown)</li>
 * </ul>
 */
public class FeatureColourFinderTest
{
  private AlignViewport av;

  private SequenceI seq;

  private FeatureColourFinder finder;

  private AlignFrame af;

  private FeatureRendererModel fr;

  @BeforeTest(alwaysRun = true)
  public void setUp()
  {
    // aligned column 8 is sequence position 6
    String s = ">s1\nABCDE---FGHIJKLMNOPQRSTUVWXYZ\n";
    af = new FileLoader().LoadFileWaitTillLoaded(s, DataSourceType.PASTE);
    av = af.getViewport();
    seq = av.getAlignment().getSequenceAt(0);
    fr = af.getFeatureRenderer();
    finder = new FeatureColourFinder(fr);
  }

  /**
   * Clear down any sequence features before each test (not as easy as it
   * sounds...)
   */
  @BeforeMethod(alwaysRun = true)
  public void setUpBeforeTest()
  {
    List<SequenceFeature> sfs = seq.getSequenceFeatures();
    for (SequenceFeature sf : sfs)
    {
      seq.deleteFeature(sf);
    }
    fr.findAllFeatures(true);

    /*
     * reset all feature groups to visible
     */
    for (String group : fr.getGroups(false))
    {
      fr.setGroupVisibility(group, true);
    }

    fr.clearRenderOrder();
    av.setShowSequenceFeatures(true);
  }

  @Test(groups = "Functional")
  public void testFindFeatureColour_noFeatures()
  {
    av.setShowSequenceFeatures(false);
    Color c = finder.findFeatureColour(Color.blue, seq, 10);
    assertEquals(c, Color.blue);

    av.setShowSequenceFeatures(true);
    c = finder.findFeatureColour(Color.blue, seq, 10);
    assertEquals(c, Color.blue);
  }

  @Test(groups = "Functional")
  public void testFindFeatureColour_noFeaturesShown()
  {
    seq.addSequenceFeature(new SequenceFeature("Metal", "Metal", 2, 12,
            Float.NaN, "MetalGroup"));
    fr.featuresAdded();
    av.setShowSequenceFeatures(false);
    Color c = finder.findFeatureColour(Color.blue, seq, 10);
    assertEquals(c, Color.blue);
  }

  @Test(groups = "Functional")
  public void testFindFeatureColour_singleFeatureAtPosition()
  {
    seq.addSequenceFeature(new SequenceFeature("Metal", "Metal", 2, 12,
            Float.NaN, "MetalGroup"));
    fr.setColour("Metal", new FeatureColour(Color.red));
    fr.featuresAdded();
    av.setShowSequenceFeatures(true);
    Color c = finder.findFeatureColour(Color.blue, seq, 10);
    assertEquals(c, Color.red);
  }

  /**
   * feature colour at a gap is null (not white) - a user defined colour scheme
   * can then provide a bespoke gap colour if configured to do so
   */
  @Test(groups = "Functional")
  public void testFindFeatureColour_gapPosition()
  {
    seq.addSequenceFeature(
            new SequenceFeature("Metal", "Metal", 2, 12, 0f, null));
    fr.setColour("Metal", new FeatureColour(Color.red));
    fr.featuresAdded();
    av.setShowSequenceFeatures(true);
    Color c = finder.findFeatureColour(null, seq, 6);
    assertNull(c);
  }

  /**
   * Nested features coloured by label - expect the colour of the enclosed
   * feature
   */
  @Test(groups = "Functional")
  public void testFindFeatureColour_nestedFeatures()
  {
    SequenceFeature sf1 = new SequenceFeature("domain", "peptide", 1, 120,
            0f, null);
    seq.addSequenceFeature(sf1);
    SequenceFeature sf2 = new SequenceFeature("domain", "binding", 10, 20,
            0f, null);
    seq.addSequenceFeature(sf2);
    FeatureColourI fc = new FeatureColour(Color.red);
    fc.setColourByLabel(true);
    fr.setColour("domain", fc);
    fr.featuresAdded();
    av.setShowSequenceFeatures(true);
    Color c = finder.findFeatureColour(null, seq, 15);
    assertEquals(c, fr.getColor(sf2, fc));
  }

  @Test(groups = "Functional")
  public void testFindFeatureColour_multipleFeaturesAtPositionNoTransparency()
  {
    /*
     * featuresAdded -> FeatureRendererModel.updateRenderOrder which adds any
     * new features 'on top' (but reverses the order of any added features)
     */
    seq.addSequenceFeature(new SequenceFeature("Metal", "Metal", 2, 12,
            Float.NaN, "MetalGroup"));
    FeatureColour red = new FeatureColour(Color.red);
    fr.setColour("Metal", red);
    fr.featuresAdded();
    seq.addSequenceFeature(new SequenceFeature("Domain", "Domain", 4, 15,
            Float.NaN, "DomainGroup"));
    FeatureColour green = new FeatureColour(Color.green);
    fr.setColour("Domain", green);
    fr.featuresAdded();
    av.setShowSequenceFeatures(true);

    /*
     * expect Domain (green) to be rendered above Metal (red)
     */
    Color c = finder.findFeatureColour(Color.blue, seq, 10);
    assertEquals(c, Color.green);

    /*
     * now promote Metal above Domain
     * - currently no way other than mimicking reordering of
     * table in Feature Settings
     */
    FeatureSettingsBean[] data = new FeatureSettingsBean[2];
    data[0] = new FeatureSettingsBean("Metal", red, null, true);
    data[1] = new FeatureSettingsBean("Domain", green, null, true);
    fr.setFeaturePriority(data);
    c = finder.findFeatureColour(Color.blue, seq, 10);
    assertEquals(c, Color.red);

    /*
     * ..and turn off display of Metal
     */
    data[0] = new FeatureSettingsBean("Metal", red, null, false);
    fr.setFeaturePriority(data);
    c = finder.findFeatureColour(Color.blue, seq, 10);
    assertEquals(c, Color.green);
  }

  @Test(groups = "Functional")
  public void testFindFeatureColour_singleFeatureNotAtPosition()
  {
    seq.addSequenceFeature(new SequenceFeature("Metal", "Metal", 8, 12,
            Float.NaN, "MetalGroup"));
    fr.setColour("Metal", new FeatureColour(Color.red));
    fr.featuresAdded();
    av.setShowSequenceFeatures(true);
    // column 2 = sequence position 3
    Color c = finder.findFeatureColour(Color.blue, seq, 2);
    assertEquals(c, Color.blue);
  }

  @Test(groups = "Functional")
  public void testFindFeatureColour_featureTypeNotDisplayed()
  {
    seq.addSequenceFeature(new SequenceFeature("Metal", "Metal", 2, 12,
            Float.NaN, "MetalGroup"));
    FeatureColour red = new FeatureColour(Color.red);
    fr.setColour("Metal", red);
    fr.featuresAdded();
    av.setShowSequenceFeatures(true);
    Color c = finder.findFeatureColour(Color.blue, seq, 10);
    assertEquals(c, Color.red);

    /*
     * turn off display of Metal - is this the easiest way to do it??
     */
    FeatureSettingsBean[] data = new FeatureSettingsBean[1];
    data[0] = new FeatureSettingsBean("Metal", red, null, false);
    fr.setFeaturePriority(data);
    c = finder.findFeatureColour(Color.blue, seq, 10);
    assertEquals(c, Color.blue);

    /*
     * turn display of Metal back on
     */
    data[0] = new FeatureSettingsBean("Metal", red, null, true);
    fr.setFeaturePriority(data);
    c = finder.findFeatureColour(Color.blue, seq, 10);
    assertEquals(c, Color.red);
  }

  @Test(groups = "Functional")
  public void testFindFeatureColour_featureGroupNotDisplayed()
  {
    seq.addSequenceFeature(new SequenceFeature("Metal", "Metal", 2, 12,
            Float.NaN, "MetalGroup"));
    FeatureColour red = new FeatureColour(Color.red);
    fr.setColour("Metal", red);
    fr.featuresAdded();
    av.setShowSequenceFeatures(true);
    Color c = finder.findFeatureColour(Color.blue, seq, 10);
    assertEquals(c, Color.red);

    /*
     * turn off display of MetalGroup
     */
    fr.setGroupVisibility("MetalGroup", false);
    c = finder.findFeatureColour(Color.blue, seq, 10);
    assertEquals(c, Color.blue);

    /*
     * turn display of MetalGroup back on
     */
    fr.setGroupVisibility("MetalGroup", true);
    c = finder.findFeatureColour(Color.blue, seq, 10);
    assertEquals(c, Color.red);
  }

  @Test(groups = "Functional")
  public void testFindFeatureColour_contactFeature()
  {
    /*
     * currently contact feature == type "Disulphide Bond" or "Disulfide Bond" !!
     */
    seq.addSequenceFeature(new SequenceFeature("Disulphide Bond", "Contact",
            2, 12, Float.NaN, "Disulphide"));
    fr.setColour("Disulphide Bond", new FeatureColour(Color.red));
    fr.featuresAdded();
    av.setShowSequenceFeatures(true);

    /*
     * Contact positions are residues 2 and 12
     * which are columns 1 and 14
     * positions in between don't count for a contact feature!
     */
    Color c = finder.findFeatureColour(Color.blue, seq, 10);
    assertEquals(c, Color.blue);
    c = finder.findFeatureColour(Color.blue, seq, 8);
    assertEquals(c, Color.blue);
    c = finder.findFeatureColour(Color.blue, seq, 1);
    assertEquals(c, Color.red);
    c = finder.findFeatureColour(Color.blue, seq, 14);
    assertEquals(c, Color.red);
  }

  @Test(groups = "Functional")
  public void testFindFeatureAtEnd()
  {
    /*
     * terminal residue feature
     */
    seq.addSequenceFeature(new SequenceFeature("PDBRESNUM", "pdb res 1",
            seq.getEnd(), seq.getEnd(), Float.NaN, "1seq.pdb"));
    fr.setColour("PDBRESNUM", new FeatureColour(Color.red));
    fr.featuresAdded();
    av.setShowSequenceFeatures(true);

    /*
     * final column should have PDBRESNUM feature, the others not
     */
    Color c = finder.findFeatureColour(Color.blue, seq,
            seq.getLength() - 2);
    assertNotEquals(c, Color.red);
    c = finder.findFeatureColour(Color.blue, seq, seq.getLength() - 1);
    assertEquals(c, Color.red);
  }

  @Test(groups = "Functional")
  public void testFindFeatureColour_graduatedFeatureColour()
  {
    seq.addSequenceFeature(new SequenceFeature("kd", "hydrophobicity", 2, 2,
            0f, "KdGroup"));
    seq.addSequenceFeature(new SequenceFeature("kd", "hydrophobicity", 4, 4,
            5f, "KdGroup"));
    seq.addSequenceFeature(new SequenceFeature("kd", "hydrophobicity", 7, 7,
            10f, "KdGroup"));

    /*
     * graduated colour from 0 to 10
     */
    Color min = new Color(100, 50, 150);
    Color max = new Color(200, 0, 100);
    FeatureColourI fc = new FeatureColour(null, min, max, null, 0, 10);
    fr.setColour("kd", fc);
    fr.featuresAdded();
    av.setShowSequenceFeatures(true);

    /*
     * position 2, column 1, score 0 - minimum colour in range
     */
    Color c = finder.findFeatureColour(Color.blue, seq, 1);
    assertEquals(c, min);

    /*
     * position 7, column 9, score 10 - maximum colour in range
     */
    c = finder.findFeatureColour(Color.blue, seq, 9);
    assertEquals(c, max);

    /*
     * position 4, column 3, score 5 - half way from min to max
     */
    c = finder.findFeatureColour(Color.blue, seq, 3);
    assertEquals(c, new Color(150, 25, 125));
  }

  @Test(groups = "Functional")
  public void testFindFeatureColour_transparencySingleFeature()
  {
    seq.addSequenceFeature(new SequenceFeature("Metal", "Metal", 2, 12,
            Float.NaN, "MetalGroup"));
    FeatureColour red = new FeatureColour(Color.red);
    fr.setColour("Metal", red);
    fr.featuresAdded();
    av.setShowSequenceFeatures(true);

    /*
     * the FeatureSettings transparency slider has range 0-70 which
     * corresponds to a transparency value of 1 - 0.3
     * A value of 0.4 gives a combination of
     * 0.4 * red(255, 0, 0) + 0.6 * cyan(0, 255, 255) = (102, 153, 153)
     */
    fr.setTransparency(0.4f);
    Color c = finder.findFeatureColour(Color.cyan, seq, 10);
    assertEquals(c, new Color(102, 153, 153));
  }

  @Test(groups = "Functional")
  public void testFindFeatureColour_transparencyTwoFeatures()
  {
    seq.addSequenceFeature(new SequenceFeature("Metal", "Metal", 2, 12,
            Float.NaN, "MetalGroup"));
    FeatureColour red = new FeatureColour(Color.red);
    fr.setColour("Metal", red);
    fr.featuresAdded();
    seq.addSequenceFeature(new SequenceFeature("Domain", "Domain", 4, 15,
            Float.NaN, "DomainGroup"));
    FeatureColour green = new FeatureColour(Color.green);
    fr.setColour("Domain", green);
    fr.featuresAdded();
    av.setShowSequenceFeatures(true);

    /*
     * Domain (green) rendered above Metal (red) above background (cyan)
     * 1) 0.6 * red(255, 0, 0) + 0.4 * cyan(0, 255, 255) = (153, 102, 102)
     * 2) 0.6* green(0, 255, 0) + 0.4 * (153, 102, 102) = (61, 194, 41) rounded
     */
    fr.setTransparency(0.6f);
    Color c = finder.findFeatureColour(Color.cyan, seq, 10);
    assertEquals(c, new Color(61, 194, 41));

    /*
     * now promote Metal above Domain
     * - currently no way other than mimicking reordering of
     * table in Feature Settings
     * Metal (red) rendered above Domain (green) above background (cyan)
     * 1) 0.6 * green(0, 255, 0) + 0.4 * cyan(0, 255, 255) = (0, 255, 102)
     * 2) 0.6* red(255, 0, 0) + 0.4 * (0, 255, 102) = (153, 102, 41) rounded
     */
    FeatureSettingsBean[] data = new FeatureSettingsBean[2];
    data[0] = new FeatureSettingsBean("Metal", red, null, true);
    data[1] = new FeatureSettingsBean("Domain", green, null, true);
    fr.setFeaturePriority(data);
    c = finder.findFeatureColour(Color.cyan, seq, 10);
    assertEquals(c, new Color(153, 102, 41));

    /*
     * ..and turn off display of Metal
     * Domain (green) above background (pink)
     * 0.6 * green(0, 255, 0) + 0.4 * pink(255, 175, 175) = (102, 223, 70)
     */
    data[0] = new FeatureSettingsBean("Metal", red, null, false);
    fr.setFeaturePriority(data);
    c = finder.findFeatureColour(Color.pink, seq, 10);
    assertEquals(c, new Color(102, 223, 70));
  }

  @Test(groups = "Functional")
  public void testNoFeaturesDisplayed()
  {
    /*
     * no features on alignment to render
     */
    assertTrue(finder.noFeaturesDisplayed());

    /*
     * add a feature
     * it will be automatically set visible but we leave
     * the viewport configured not to show features
     */
    av.setShowSequenceFeatures(false);
    seq.addSequenceFeature(new SequenceFeature("Metal", "Metal", 2, 12,
            Float.NaN, "MetalGroup"));
    FeatureColour red = new FeatureColour(Color.red);
    fr.setColour("Metal", red);
    fr.featuresAdded();
    assertTrue(finder.noFeaturesDisplayed());

    /*
     * turn on feature display
     */
    av.setShowSequenceFeatures(true);
    assertFalse(finder.noFeaturesDisplayed());

    /*
     * turn off display of Metal
     */
    FeatureSettingsBean[] data = new FeatureSettingsBean[1];
    data[0] = new FeatureSettingsBean("Metal", red, null, false);
    fr.setFeaturePriority(data);
    assertTrue(finder.noFeaturesDisplayed());

    /*
     * turn display of Metal back on
     */
    fr.setVisible("Metal");
    assertFalse(finder.noFeaturesDisplayed());

    /*
     * turn off MetalGroup - has no effect here since the group of a
     * sequence feature instance is independent of its type
     */
    fr.setGroupVisibility("MetalGroup", false);
    assertFalse(finder.noFeaturesDisplayed());

    /*
     * a finder with no feature renderer
     */
    FeatureColourFinder finder2 = new FeatureColourFinder(null);
    assertTrue(finder2.noFeaturesDisplayed());
  }

  @Test(groups = "Functional")
  public void testFindFeatureColour_graduatedWithThreshold()
  {
    String kdFeature = "kd";
    String metalFeature = "Metal";
    seq.addSequenceFeature(new SequenceFeature(kdFeature, "hydrophobicity",
            2, 2, 0f, "KdGroup"));
    seq.addSequenceFeature(new SequenceFeature(kdFeature, "hydrophobicity",
            4, 4, 5f, "KdGroup"));
    seq.addSequenceFeature(new SequenceFeature(metalFeature, "Fe", 4, 4, 5f,
            "MetalGroup"));
    seq.addSequenceFeature(new SequenceFeature(kdFeature, "hydrophobicity",
            7, 7, 10f, "KdGroup"));

    /*
     * kd feature has graduated colour from 0 to 10
     * above threshold value of 5
     */
    Color min = new Color(100, 50, 150);
    Color max = new Color(200, 0, 100);
    FeatureColourI fc = new FeatureColour(null, min, max, null, 0, 10);
    fc.setAboveThreshold(true);
    fc.setThreshold(5f);
    fr.setColour(kdFeature, fc);
    FeatureColour green = new FeatureColour(Color.green);
    fr.setColour(metalFeature, green);
    fr.featuresAdded();

    /*
     * render order is kd above Metal
     */
    FeatureSettingsBean[] data = new FeatureSettingsBean[2];
    data[0] = new FeatureSettingsBean(kdFeature, fc, null, true);
    data[1] = new FeatureSettingsBean(metalFeature, green, null, true);
    fr.setFeaturePriority(data);

    av.setShowSequenceFeatures(true);

    /*
     * position 2, column 1, score 0 - below threshold - default colour
     */
    Color c = finder.findFeatureColour(Color.blue, seq, 1);
    assertEquals(c, Color.blue);

    /*
     * position 4, column 3, score 5 - at threshold
     * should return Green (colour of Metal feature)
     */
    c = finder.findFeatureColour(Color.blue, seq, 3);
    assertEquals(c, Color.green);

    /*
     * position 7, column 9, score 10 - maximum colour in range
     */
    c = finder.findFeatureColour(Color.blue, seq, 9);
    assertEquals(c, max);

    /*
     * now colour below threshold of 5
     */
    fc.setBelowThreshold(true);

    /*
     * position 2, column 1, score 0 - min colour
     */
    c = finder.findFeatureColour(Color.blue, seq, 1);
    assertEquals(c, min);

    /*
     * position 4, column 3, score 5 - at threshold
     * should return Green (colour of Metal feature)
     */
    c = finder.findFeatureColour(Color.blue, seq, 3);
    assertEquals(c, Color.green);

    /*
     * position 7, column 9, score 10 - above threshold - default colour
     */
    c = finder.findFeatureColour(Color.blue, seq, 9);
    assertEquals(c, Color.blue);
  }
}
