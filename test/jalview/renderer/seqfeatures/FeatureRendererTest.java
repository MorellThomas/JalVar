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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jalview.analysis.GeneticCodes;
import jalview.api.AlignViewportI;
import jalview.api.FeatureColourI;
import jalview.bin.Jalview;
import jalview.datamodel.MappedFeatures;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.datamodel.features.FeatureMatcher;
import jalview.datamodel.features.FeatureMatcherSet;
import jalview.datamodel.features.FeatureMatcherSetI;
import jalview.gui.AlignFrame;
import jalview.gui.AlignViewport;
import jalview.gui.Desktop;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;
import jalview.schemes.FeatureColour;
import jalview.util.matcher.Condition;
import jalview.viewmodel.seqfeatures.FeatureRendererModel.FeatureSettingsBean;

public class FeatureRendererTest
{
  @BeforeMethod(alwaysRun = true)
  public void closeAll()
  {
    if (Desktop.instance != null)
      Desktop.instance.closeAll_actionPerformed(null);
  }

  @Test(groups = "Functional")
  public void testFindAllFeatures()
  {
    String seqData = ">s1\nabcdef\n>s2\nabcdef\n>s3\nabcdef\n>s4\nabcdef\n";
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(seqData,
            DataSourceType.PASTE);
    AlignViewportI av = af.getViewport();
    FeatureRenderer fr = new FeatureRenderer(av);

    /*
     * with no features
     */
    fr.findAllFeatures(true);
    assertTrue(fr.getRenderOrder().isEmpty());
    assertTrue(fr.getFeatureGroups().isEmpty());

    List<SequenceI> seqs = av.getAlignment().getSequences();

    // add a non-positional feature - should be ignored by FeatureRenderer
    SequenceFeature sf1 = new SequenceFeature("Type", "Desc", 0, 0, 1f,
            "Group");
    seqs.get(0).addSequenceFeature(sf1);
    fr.findAllFeatures(true);
    // ? bug - types and groups added for non-positional features
    List<String> types = fr.getRenderOrder();
    List<String> groups = fr.getFeatureGroups();
    assertEquals(types.size(), 0);
    assertFalse(types.contains("Type"));
    assertEquals(groups.size(), 0);
    assertFalse(groups.contains("Group"));

    // add some positional features
    seqs.get(1).addSequenceFeature(
            new SequenceFeature("Pfam", "Desc", 5, 9, 1f, "PfamGroup"));
    seqs.get(2).addSequenceFeature(
            new SequenceFeature("Pfam", "Desc", 14, 22, 2f, "RfamGroup"));
    // bug in findAllFeatures - group not checked for a known feature type
    seqs.get(2).addSequenceFeature(new SequenceFeature("Rfam", "Desc", 5, 9,
            Float.NaN, "RfamGroup"));
    // existing feature type with null group
    seqs.get(3).addSequenceFeature(
            new SequenceFeature("Rfam", "Desc", 5, 9, Float.NaN, null));
    // new feature type with null group
    seqs.get(3).addSequenceFeature(
            new SequenceFeature("Scop", "Desc", 5, 9, Float.NaN, null));
    // null value for type produces NullPointerException
    fr.findAllFeatures(true);
    types = fr.getRenderOrder();
    groups = fr.getFeatureGroups();
    assertEquals(types.size(), 3);
    assertFalse(types.contains("Type"));
    assertTrue(types.contains("Pfam"));
    assertTrue(types.contains("Rfam"));
    assertTrue(types.contains("Scop"));
    assertEquals(groups.size(), 2);
    assertFalse(groups.contains("Group"));
    assertTrue(groups.contains("PfamGroup"));
    assertTrue(groups.contains("RfamGroup"));
    assertFalse(groups.contains(null)); // null group is ignored

    /*
     * check min-max values
     */
    Map<String, float[][]> minMax = fr.getMinMax();
    assertEquals(minMax.size(), 1); // non-positional and NaN not stored
    assertEquals(minMax.get("Pfam")[0][0], 1f); // positional min
    assertEquals(minMax.get("Pfam")[0][1], 2f); // positional max

    // increase max for Pfam, add scores for Rfam
    seqs.get(0).addSequenceFeature(
            new SequenceFeature("Pfam", "Desc", 14, 22, 8f, "RfamGroup"));
    seqs.get(1).addSequenceFeature(
            new SequenceFeature("Rfam", "Desc", 5, 9, 6f, "RfamGroup"));
    fr.findAllFeatures(true);
    // note minMax is not a defensive copy, shouldn't expose this
    assertEquals(minMax.size(), 2);
    assertEquals(minMax.get("Pfam")[0][0], 1f);
    assertEquals(minMax.get("Pfam")[0][1], 8f);
    assertEquals(minMax.get("Rfam")[0][0], 6f);
    assertEquals(minMax.get("Rfam")[0][1], 6f);

    /*
     * check render order (last is on top)
     */
    List<String> renderOrder = fr.getRenderOrder();
    assertEquals(renderOrder, Arrays.asList("Scop", "Rfam", "Pfam"));

    /*
     * change render order (todo: an easier way)
     * nb here last comes first in the data array
     */
    FeatureSettingsBean[] data = new FeatureSettingsBean[3];
    FeatureColourI colour = new FeatureColour(Color.RED);
    data[0] = new FeatureSettingsBean("Rfam", colour, null, true);
    data[1] = new FeatureSettingsBean("Pfam", colour, null, false);
    data[2] = new FeatureSettingsBean("Scop", colour, null, false);
    fr.setFeaturePriority(data);
    assertEquals(fr.getRenderOrder(),
            Arrays.asList("Scop", "Pfam", "Rfam"));
    assertEquals(fr.getDisplayedFeatureTypes(), Arrays.asList("Rfam"));

    /*
     * add a new feature type: should go on top of render order as visible,
     * other feature ordering and visibility should be unchanged
     */
    seqs.get(2).addSequenceFeature(
            new SequenceFeature("Metal", "Desc", 14, 22, 8f, "MetalGroup"));
    fr.findAllFeatures(true);
    assertEquals(fr.getRenderOrder(),
            Arrays.asList("Scop", "Pfam", "Rfam", "Metal"));
    assertEquals(fr.getDisplayedFeatureTypes(),
            Arrays.asList("Rfam", "Metal"));
  }

  @Test(groups = "Functional")
  public void testFindFeaturesAtColumn()
  {
    String seqData = ">s1/4-29\n-ab--cdefghijklmnopqrstuvwxyz\n";
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(seqData,
            DataSourceType.PASTE);
    AlignViewportI av = af.getViewport();
    FeatureRenderer fr = new FeatureRenderer(av);
    SequenceI seq = av.getAlignment().getSequenceAt(0);

    /*
     * with no features
     */
    List<SequenceFeature> features = fr.findFeaturesAtColumn(seq, 3);
    assertTrue(features.isEmpty());

    /*
     * add features
     */
    SequenceFeature sf1 = new SequenceFeature("Type1", "Desc", 0, 0, 1f,
            "Group"); // non-positional
    seq.addSequenceFeature(sf1);
    SequenceFeature sf2 = new SequenceFeature("Type2", "Desc", 8, 18, 1f,
            "Group1");
    seq.addSequenceFeature(sf2);
    SequenceFeature sf3 = new SequenceFeature("Type3", "Desc", 8, 18, 1f,
            "Group2");
    seq.addSequenceFeature(sf3);
    SequenceFeature sf4 = new SequenceFeature("Type3", "Desc", 8, 18, 1f,
            null); // null group is always treated as visible
    seq.addSequenceFeature(sf4);

    /*
     * add contact features
     */
    SequenceFeature sf5 = new SequenceFeature("Disulphide Bond", "Desc", 7,
            15, 1f, "Group1");
    seq.addSequenceFeature(sf5);
    SequenceFeature sf6 = new SequenceFeature("Disulphide Bond", "Desc", 7,
            15, 1f, "Group2");
    seq.addSequenceFeature(sf6);
    SequenceFeature sf7 = new SequenceFeature("Disulphide Bond", "Desc", 7,
            15, 1f, null);
    seq.addSequenceFeature(sf7);

    // feature spanning B--C
    SequenceFeature sf8 = new SequenceFeature("Type1", "Desc", 5, 6, 1f,
            "Group");
    seq.addSequenceFeature(sf8);
    // contact feature B/C
    SequenceFeature sf9 = new SequenceFeature("Disulphide Bond", "Desc", 5,
            6, 1f, "Group");
    seq.addSequenceFeature(sf9);

    /*
     * let feature renderer discover features (and make visible)
     */
    fr.findAllFeatures(true);
    features = fr.findFeaturesAtColumn(seq, 15); // all positional
    assertEquals(features.size(), 6);
    assertTrue(features.contains(sf2));
    assertTrue(features.contains(sf3));
    assertTrue(features.contains(sf4));
    assertTrue(features.contains(sf5));
    assertTrue(features.contains(sf6));
    assertTrue(features.contains(sf7));

    /*
     * at a non-contact position
     */
    features = fr.findFeaturesAtColumn(seq, 14);
    assertEquals(features.size(), 3);
    assertTrue(features.contains(sf2));
    assertTrue(features.contains(sf3));
    assertTrue(features.contains(sf4));

    /*
     * make "Type2" not displayed
     */
    FeatureColourI colour = new FeatureColour(Color.RED);
    FeatureSettingsBean[] data = new FeatureSettingsBean[4];
    data[0] = new FeatureSettingsBean("Type1", colour, null, true);
    data[1] = new FeatureSettingsBean("Type2", colour, null, false);
    data[2] = new FeatureSettingsBean("Type3", colour, null, true);
    data[3] = new FeatureSettingsBean("Disulphide Bond", colour, null,
            true);
    fr.setFeaturePriority(data);

    features = fr.findFeaturesAtColumn(seq, 15);
    assertEquals(features.size(), 5); // no sf2
    assertTrue(features.contains(sf3));
    assertTrue(features.contains(sf4));
    assertTrue(features.contains(sf5));
    assertTrue(features.contains(sf6));
    assertTrue(features.contains(sf7));

    /*
     * make "Group2" not displayed
     */
    fr.setGroupVisibility("Group2", false);

    features = fr.findFeaturesAtColumn(seq, 15);
    assertEquals(features.size(), 3); // no sf2, sf3, sf6
    assertTrue(features.contains(sf4));
    assertTrue(features.contains(sf5));
    assertTrue(features.contains(sf7));

    // features 'at' a gap between b and c
    // - returns enclosing feature BC but not contact feature B/C
    features = fr.findFeaturesAtColumn(seq, 4);
    assertEquals(features.size(), 1);
    assertTrue(features.contains(sf8));
    features = fr.findFeaturesAtColumn(seq, 5);
    assertEquals(features.size(), 1);
    assertTrue(features.contains(sf8));

    /*
     * give "Type3" features a graduated colour scheme
     * - first with no threshold
     */
    FeatureColourI gc = new FeatureColour(Color.green, Color.yellow,
            Color.red, null, 0f, 10f);
    fr.getFeatureColours().put("Type3", gc);
    features = fr.findFeaturesAtColumn(seq, 8);
    assertTrue(features.contains(sf4));
    // now with threshold > 2f - feature score of 1f is excluded
    gc.setAboveThreshold(true);
    gc.setThreshold(2f);
    features = fr.findFeaturesAtColumn(seq, 8);
    assertFalse(features.contains(sf4));

    /*
     * make "Type3" graduated colour by attribute "AF"
     * - first with no attribute held - feature should be excluded
     */
    gc.setAttributeName("AF");
    features = fr.findFeaturesAtColumn(seq, 8);
    assertFalse(features.contains(sf4));
    // now with the attribute above threshold - should be included
    sf4.setValue("AF", "2.4");
    features = fr.findFeaturesAtColumn(seq, 8);
    assertTrue(features.contains(sf4));
    // now with the attribute below threshold - should be excluded
    sf4.setValue("AF", "1.4");
    features = fr.findFeaturesAtColumn(seq, 8);
    assertFalse(features.contains(sf4));
  }

  @Test(groups = "Functional")
  public void testFilterFeaturesForDisplay()
  {
    String seqData = ">s1\nabcdef\n";
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(seqData,
            DataSourceType.PASTE);
    AlignViewportI av = af.getViewport();
    FeatureRenderer fr = new FeatureRenderer(av);

    List<SequenceFeature> features = new ArrayList<>();
    fr.filterFeaturesForDisplay(features); // empty list, does nothing

    SequenceI seq = av.getAlignment().getSequenceAt(0);
    SequenceFeature sf1 = new SequenceFeature("Cath", "", 6, 8, Float.NaN,
            "group1");
    SequenceFeature sf2 = new SequenceFeature("Cath", "", 5, 11, 2f,
            "group2");
    SequenceFeature sf3 = new SequenceFeature("Cath", "", 5, 11, 3f,
            "group3");
    SequenceFeature sf4 = new SequenceFeature("Cath", "", 6, 8, 4f,
            "group4");
    SequenceFeature sf5 = new SequenceFeature("Cath", "", 6, 9, 5f,
            "group4");
    seq.addSequenceFeature(sf1);
    seq.addSequenceFeature(sf2);
    seq.addSequenceFeature(sf3);
    seq.addSequenceFeature(sf4);
    seq.addSequenceFeature(sf5);

    fr.findAllFeatures(true);

    features = seq.getSequenceFeatures();
    assertEquals(features.size(), 5);
    assertTrue(features.contains(sf1));
    assertTrue(features.contains(sf2));
    assertTrue(features.contains(sf3));
    assertTrue(features.contains(sf4));
    assertTrue(features.contains(sf5));

    /*
     * filter out duplicate (co-located) features
     * note: which gets removed is not guaranteed
     */
    fr.filterFeaturesForDisplay(features);
    assertEquals(features.size(), 3);
    assertTrue(features.contains(sf1) || features.contains(sf4));
    assertFalse(features.contains(sf1) && features.contains(sf4));
    assertTrue(features.contains(sf2) || features.contains(sf3));
    assertFalse(features.contains(sf2) && features.contains(sf3));
    assertTrue(features.contains(sf5));

    /*
     * features in hidden groups are removed
     */
    fr.setGroupVisibility("group2", false);
    fr.setGroupVisibility("group3", false);
    features = seq.getSequenceFeatures();
    fr.filterFeaturesForDisplay(features);
    assertEquals(features.size(), 2);
    assertTrue(features.contains(sf1) || features.contains(sf4));
    assertFalse(features.contains(sf1) && features.contains(sf4));
    assertFalse(features.contains(sf2));
    assertFalse(features.contains(sf3));
    assertTrue(features.contains(sf5));

    /*
     * no filtering if transparency is applied
     */
    fr.setTransparency(0.5f);
    features = seq.getSequenceFeatures();
    fr.filterFeaturesForDisplay(features);
    assertEquals(features.size(), 5);
  }

  @Test(groups = "Functional")
  public void testGetColour()
  {
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(">s1\nABCD\n",
            DataSourceType.PASTE);
    AlignViewportI av = af.getViewport();
    FeatureRenderer fr = new FeatureRenderer(av);

    /*
     * simple colour, feature type and group displayed
     */
    FeatureColourI fc = new FeatureColour(Color.red);
    fr.getFeatureColours().put("Cath", fc);
    SequenceFeature sf1 = new SequenceFeature("Cath", "", 6, 8, Float.NaN,
            "group1");
    assertEquals(fr.getColour(sf1), Color.red);

    /*
     * hide feature type, then unhide
     * - feature type visibility should not affect the result
     */
    FeatureSettingsBean[] data = new FeatureSettingsBean[1];
    data[0] = new FeatureSettingsBean("Cath", fc, null, false);
    fr.setFeaturePriority(data);
    assertEquals(fr.getColour(sf1), Color.red);
    data[0] = new FeatureSettingsBean("Cath", fc, null, true);
    fr.setFeaturePriority(data);
    assertEquals(fr.getColour(sf1), Color.red);

    /*
     * hide feature group, then unhide
     */
    fr.setGroupVisibility("group1", false);
    assertNull(fr.getColour(sf1));
    fr.setGroupVisibility("group1", true);
    assertEquals(fr.getColour(sf1), Color.red);

    /*
     * graduated colour by score, no threshold, no score
     * 
     */
    FeatureColourI gc = new FeatureColour(Color.red, Color.yellow,
            Color.red, Color.green, 1f, 11f);
    fr.getFeatureColours().put("Cath", gc);
    assertEquals(fr.getColour(sf1), Color.green);

    /*
     * graduated colour by score, no threshold, with score value
     */
    SequenceFeature sf2 = new SequenceFeature("Cath", "", 6, 8, 6f,
            "group1");
    // score 6 is half way from yellow(255, 255, 0) to red(255, 0, 0)
    Color expected = new Color(255, 128, 0);
    assertEquals(fr.getColour(sf2), expected);

    /*
     * above threshold, score is above threshold - no change
     */
    gc.setAboveThreshold(true);
    gc.setThreshold(5f);
    assertEquals(fr.getColour(sf2), expected);

    /*
     * threshold is min-max; now score 6 is 1/6 of the way from 5 to 11
     * or from yellow(255, 255, 0) to red(255, 0, 0)
     */
    gc = new FeatureColour(Color.red, Color.yellow, Color.red, Color.green,
            5f, 11f);
    fr.getFeatureColours().put("Cath", gc);
    gc.setAutoScaled(false); // this does little other than save a checkbox
                             // setting!
    assertEquals(fr.getColour(sf2), new Color(255, 213, 0));

    /*
     * feature score is below threshold - no colour
     */
    gc.setAboveThreshold(true);
    gc.setThreshold(7f);
    assertNull(fr.getColour(sf2));

    /*
     * feature score is above threshold - no colour
     */
    gc.setBelowThreshold(true);
    gc.setThreshold(3f);
    assertNull(fr.getColour(sf2));

    /*
     * colour by feature attribute value
     * first with no value held
     */
    gc = new FeatureColour(Color.red, Color.yellow, Color.red, Color.green,
            1f, 11f);
    fr.getFeatureColours().put("Cath", gc);
    gc.setAttributeName("AF");
    assertEquals(fr.getColour(sf2), Color.green);

    // with non-numeric attribute value
    sf2.setValue("AF", "Five");
    assertEquals(fr.getColour(sf2), Color.green);

    // with numeric attribute value
    sf2.setValue("AF", "6");
    assertEquals(fr.getColour(sf2), expected);

    // with numeric value outwith threshold
    gc.setAboveThreshold(true);
    gc.setThreshold(10f);
    assertNull(fr.getColour(sf2));

    // with filter on AF < 4
    gc.setAboveThreshold(false);
    assertEquals(fr.getColour(sf2), expected);
    FeatureMatcherSetI filter = new FeatureMatcherSet();
    filter.and(FeatureMatcher.byAttribute(Condition.LT, "4.0", "AF"));
    fr.setFeatureFilter("Cath", filter);
    assertNull(fr.getColour(sf2));

    // with filter on 'Consequence contains missense'
    filter = new FeatureMatcherSet();
    filter.and(FeatureMatcher.byAttribute(Condition.Contains, "missense",
            "Consequence"));
    fr.setFeatureFilter("Cath", filter);
    // if feature has no Consequence attribute, no colour
    assertNull(fr.getColour(sf2));
    // if attribute does not match filter, no colour
    sf2.setValue("Consequence", "Synonymous");
    assertNull(fr.getColour(sf2));
    // attribute matches filter
    sf2.setValue("Consequence", "Missense variant");
    assertEquals(fr.getColour(sf2), expected);

    // with filter on CSQ:Feature contains "ENST01234"
    filter = new FeatureMatcherSet();
    filter.and(FeatureMatcher.byAttribute(Condition.Matches, "ENST01234",
            "CSQ", "Feature"));
    fr.setFeatureFilter("Cath", filter);
    // if feature has no CSQ data, no colour
    assertNull(fr.getColour(sf2));
    // if CSQ data does not include Feature, no colour
    Map<String, String> csqData = new HashMap<>();
    csqData.put("BIOTYPE", "Transcript");
    sf2.setValue("CSQ", csqData);
    assertNull(fr.getColour(sf2));
    // if attribute does not match filter, no colour
    csqData.put("Feature", "ENST9876");
    assertNull(fr.getColour(sf2));
    // attribute matches filter
    csqData.put("Feature", "ENST01234");
    assertEquals(fr.getColour(sf2), expected);
  }

  @Test(groups = "Functional")
  public void testIsVisible()
  {
    String seqData = ">s1\nMLQGIFPRS\n";
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(seqData,
            DataSourceType.PASTE);
    AlignViewportI av = af.getViewport();
    FeatureRenderer fr = new FeatureRenderer(av);
    SequenceI seq = av.getAlignment().getSequenceAt(0);
    SequenceFeature sf = new SequenceFeature("METAL", "Desc", 10, 10, 1f,
            "Group");
    sf.setValue("AC", "11");
    sf.setValue("CLIN_SIG", "Likely Pathogenic");
    seq.addSequenceFeature(sf);

    assertFalse(fr.isVisible(null));

    /*
     * initial state FeatureRenderer hasn't 'found' feature
     * and so its feature type has not yet been set visible
     */
    assertFalse(fr.getDisplayedFeatureCols().containsKey("METAL"));
    assertFalse(fr.isVisible(sf));

    fr.findAllFeatures(true);
    assertTrue(fr.isVisible(sf));

    /*
     * feature group not visible
     */
    fr.setGroupVisibility("Group", false);
    assertFalse(fr.isVisible(sf));
    fr.setGroupVisibility("Group", true);
    assertTrue(fr.isVisible(sf));

    /*
     * feature score outwith colour threshold (score > 2)
     */
    FeatureColourI fc = new FeatureColour(null, Color.white, Color.black,
            Color.white, 0, 10);
    fc.setAboveThreshold(true);
    fc.setThreshold(2f);
    fr.setColour("METAL", fc);
    assertFalse(fr.isVisible(sf)); // score 1 is not above threshold 2
    fc.setBelowThreshold(true);
    assertTrue(fr.isVisible(sf)); // score 1 is below threshold 2

    /*
     * colour with threshold on attribute AC (value is 11)
     */
    fc.setAttributeName("AC");
    assertFalse(fr.isVisible(sf)); // value 11 is not below threshold 2
    fc.setAboveThreshold(true);
    assertTrue(fr.isVisible(sf)); // value 11 is above threshold 2

    fc.setAttributeName("AF"); // attribute AF is absent in sf
    assertTrue(fr.isVisible(sf)); // feature is not excluded by threshold

    FeatureMatcherSetI filter = new FeatureMatcherSet();
    filter.and(FeatureMatcher.byAttribute(Condition.Contains, "pathogenic",
            "CLIN_SIG"));
    fr.setFeatureFilter("METAL", filter);
    assertTrue(fr.isVisible(sf)); // feature matches filter
    filter.and(FeatureMatcher.byScore(Condition.LE, "0.4"));
    assertFalse(fr.isVisible(sf)); // feature doesn't match filter
  }

  @Test(groups = "Functional")
  public void testFindComplementFeaturesAtResidue()
  {
    Jalview.main(
            new String[]
            { "-nonews", "-props", "test/jalview/testProps.jvprops" });

    // codons for MCWHSE
    String cdsSeq = ">cds\nATGtgtTGGcacTCAgaa";
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(cdsSeq,
            DataSourceType.PASTE);
    af.showTranslation_actionPerformed(
            GeneticCodes.getInstance().getStandardCodeTable());
    af.closeMenuItem_actionPerformed(true);

    /*
     * find the complement frames (ugly)
     */
    AlignFrame[] frames = Desktop.getAlignFrames();
    assertEquals(frames.length, 2);
    AlignViewport av1 = frames[0].getViewport();
    AlignViewport av2 = frames[1].getViewport();
    AlignViewport cds = av1.getAlignment().isNucleotide() ? av1 : av2;
    AlignViewport peptide = cds == av1 ? av2 : av1;
    assertNotNull(cds);
    assertNotNull(peptide);

    /*
     * add features to CDS at first codon, positions 2-3
     */
    SequenceI seq1 = cds.getAlignment().getSequenceAt(0);
    SequenceFeature sf1 = new SequenceFeature("sequence_variant", "G,GT", 2,
            2, "ensembl");
    seq1.addSequenceFeature(sf1);
    SequenceFeature sf2 = new SequenceFeature("sequence_variant", "C, CA",
            3, 3, "ensembl");
    seq1.addSequenceFeature(sf2);

    /*
     * 'find' mapped features from the peptide position
     * - first with CDS features _not_ shown on peptide alignment
     */
    SequenceI seq2 = peptide.getAlignment().getSequenceAt(0);
    FeatureRenderer frC = new FeatureRenderer(cds);
    frC.featuresAdded();
    MappedFeatures mf = frC.findComplementFeaturesAtResidue(seq2, 1);
    assertNotNull(mf);
    assertEquals(mf.features.size(), 2);
    assertSame(mf.features.get(0), sf1);
    assertSame(mf.features.get(1), sf2);

    /*
     * add exon feature and verify it is only returned once for a
     * peptide position, even though it is on all 3 codon positions
     */
    SequenceFeature sf3 = new SequenceFeature("exon", "exon1", 4, 12,
            "ensembl");
    seq1.addSequenceFeature(sf3);
    frC.featuresAdded();
    mf = frC.findComplementFeaturesAtResidue(seq2, 3);
    assertNotNull(mf);
    assertEquals(mf.features.size(), 1);
    assertSame(mf.features.get(0), sf3);
  }
}
