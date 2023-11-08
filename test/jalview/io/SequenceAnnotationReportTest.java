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
package jalview.io;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.api.FeatureColourI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.MappedFeatures;
import jalview.datamodel.Mapping;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;
import jalview.io.gff.GffConstants;
import jalview.renderer.seqfeatures.FeatureRenderer;
import jalview.schemes.FeatureColour;
import jalview.util.MapList;
import jalview.viewmodel.seqfeatures.FeatureRendererModel;
import junit.extensions.PA;

public class SequenceAnnotationReportTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = "Functional")
  public void testAppendFeature_disulfideBond()
  {
    SequenceAnnotationReport sar = new SequenceAnnotationReport(false);
    StringBuilder sb = new StringBuilder();
    sb.append("123456");
    SequenceFeature sf = new SequenceFeature("disulfide bond", "desc", 1, 3,
            1.2f, "group");

    // residuePos == 2 does not match start or end of feature, nothing done:
    sar.appendFeature(sb, 2, null, sf, null, 0);
    assertEquals("123456", sb.toString());

    // residuePos == 1 matches start of feature, text appended (but no <br/>)
    // feature score is not included
    sar.appendFeature(sb, 1, null, sf, null, 0);
    assertEquals("123456disulfide bond 1:3", sb.toString());

    // residuePos == 3 matches end of feature, text appended
    // <br/> is prefixed once sb.length() > 6
    sar.appendFeature(sb, 3, null, sf, null, 0);
    assertEquals("123456disulfide bond 1:3<br/>disulfide bond 1:3",
            sb.toString());
  }

  @Test(groups = "Functional")
  public void testAppendFeatures_longText()
  {
    SequenceAnnotationReport sar = new SequenceAnnotationReport(false);
    StringBuilder sb = new StringBuilder();
    String longString = "Abcd".repeat(50);
    SequenceFeature sf = new SequenceFeature("sequence", longString, 1, 3,
            "group");

    sar.appendFeature(sb, 1, null, sf, null, 0);
    assertTrue(sb.length() < 100);

    List<SequenceFeature> sfl = new ArrayList<>();
    sb.setLength(0);
    sfl.add(sf);
    sfl.add(sf);
    sfl.add(sf);
    sfl.add(sf);
    sfl.add(sf);
    sfl.add(sf);
    sfl.add(sf);
    sfl.add(sf);
    sfl.add(sf);
    sfl.add(sf);
    int n = sar.appendFeatures(sb, 1, sfl, new FeatureRenderer(null), 200); // text
                                                                            // should
                                                                            // terminate
                                                                            // before
                                                                            // 200
                                                                            // characters
    String s = sb.toString();
    assertTrue(s.length() < 200);
    assertEquals(n, 7); // should be 7 features left over

  }

  @Test(groups = "Functional")
  public void testAppendFeature_status()
  {
    SequenceAnnotationReport sar = new SequenceAnnotationReport(false);
    StringBuilder sb = new StringBuilder();
    SequenceFeature sf = new SequenceFeature("METAL", "Fe2-S", 1, 3,
            Float.NaN, "group");
    sf.setStatus("Confirmed");

    sar.appendFeature(sb, 1, null, sf, null, 0);
    assertEquals("METAL 1 3; Fe2-S; (Confirmed)", sb.toString());
  }

  @Test(groups = "Functional")
  public void testAppendFeature_withScore()
  {
    SequenceAnnotationReport sar = new SequenceAnnotationReport(false);
    StringBuilder sb = new StringBuilder();
    SequenceFeature sf = new SequenceFeature("METAL", "Fe2-S", 1, 3, 1.3f,
            "group");

    FeatureRendererModel fr = new FeatureRenderer(null);
    Map<String, float[][]> minmax = fr.getMinMax();
    sar.appendFeature(sb, 1, fr, sf, null, 0);
    /*
     * map has no entry for this feature type - score is not shown:
     */
    assertEquals("METAL 1 3; Fe2-S", sb.toString());

    /*
     * map has entry for this feature type - score is shown:
     */
    minmax.put("METAL", new float[][] { { 0f, 1f }, null });
    sar.appendFeature(sb, 1, fr, sf, null, 0);
    // <br/> is appended to a buffer > 6 in length
    assertEquals("METAL 1 3; Fe2-S<br/>METAL 1 3; Fe2-S Score=1.3",
            sb.toString());

    /*
     * map has min == max for this feature type - score is not shown:
     */
    minmax.put("METAL", new float[][] { { 2f, 2f }, null });
    sb.setLength(0);
    sar.appendFeature(sb, 1, fr, sf, null, 0);
    assertEquals("METAL 1 3; Fe2-S", sb.toString());
  }

  @Test(groups = "Functional")
  public void testAppendFeature_noScore()
  {
    SequenceAnnotationReport sar = new SequenceAnnotationReport(false);
    StringBuilder sb = new StringBuilder();
    SequenceFeature sf = new SequenceFeature("METAL", "Fe2-S", 1, 3,
            Float.NaN, "group");

    sar.appendFeature(sb, 1, null, sf, null, 0);
    assertEquals("METAL 1 3; Fe2-S", sb.toString());
  }

  /**
   * A specific attribute value is included if it is used to colour the feature
   */
  @Test(groups = "Functional")
  public void testAppendFeature_colouredByAttribute()
  {
    SequenceAnnotationReport sar = new SequenceAnnotationReport(false);
    StringBuilder sb = new StringBuilder();
    SequenceFeature sf = new SequenceFeature("METAL", "Fe2-S", 1, 3,
            Float.NaN, "group");
    sf.setValue("clinical_significance", "Benign");

    /*
     * first with no colour by attribute
     */
    FeatureRendererModel fr = new FeatureRenderer(null);
    sar.appendFeature(sb, 1, fr, sf, null, 0);
    assertEquals("METAL 1 3; Fe2-S", sb.toString());

    /*
     * then with colour by an attribute the feature lacks
     */
    FeatureColourI fc = new FeatureColour(null, Color.white, Color.black,
            null, 5, 10);
    fc.setAttributeName("Pfam");
    fr.setColour("METAL", fc);
    sb.setLength(0);
    sar.appendFeature(sb, 1, fr, sf, null, 0);
    assertEquals("METAL 1 3; Fe2-S", sb.toString()); // no change

    /*
     * then with colour by an attribute the feature has
     */
    fc.setAttributeName("clinical_significance");
    sb.setLength(0);
    sar.appendFeature(sb, 1, fr, sf, null, 0);
    assertEquals("METAL 1 3; Fe2-S; clinical_significance=Benign",
            sb.toString());
  }

  @Test(groups = "Functional")
  public void testAppendFeature_withScoreStatusAttribute()
  {
    SequenceAnnotationReport sar = new SequenceAnnotationReport(false);
    StringBuilder sb = new StringBuilder();
    SequenceFeature sf = new SequenceFeature("METAL", "Fe2-S", 1, 3, 1.3f,
            "group");
    sf.setStatus("Confirmed");
    sf.setValue("clinical_significance", "Benign");

    FeatureRendererModel fr = new FeatureRenderer(null);
    Map<String, float[][]> minmax = fr.getMinMax();
    FeatureColourI fc = new FeatureColour(null, Color.white, Color.blue,
            null, 12, 22);
    fc.setAttributeName("clinical_significance");
    fr.setColour("METAL", fc);
    minmax.put("METAL", new float[][] { { 0f, 1f }, null });
    sar.appendFeature(sb, 1, fr, sf, null, 0);

    assertEquals(
            "METAL 1 3; Fe2-S Score=1.3; (Confirmed); clinical_significance=Benign",
            sb.toString());
  }

  @Test(groups = "Functional")
  public void testAppendFeature_DescEqualsType()
  {
    SequenceAnnotationReport sar = new SequenceAnnotationReport(false);
    StringBuilder sb = new StringBuilder();
    SequenceFeature sf = new SequenceFeature("METAL", "METAL", 1, 3,
            Float.NaN, "group");

    // description is not included if it duplicates type:
    sar.appendFeature(sb, 1, null, sf, null, 0);
    assertEquals("METAL 1 3", sb.toString());

    sb.setLength(0);
    sf.setDescription("Metal");
    // test is case-sensitive:
    sar.appendFeature(sb, 1, null, sf, null, 0);
    assertEquals("METAL 1 3; Metal", sb.toString());
  }

  @Test(groups = "Functional")
  public void testAppendFeature_stripHtml()
  {
    SequenceAnnotationReport sar = new SequenceAnnotationReport(false);
    StringBuilder sb = new StringBuilder();
    SequenceFeature sf = new SequenceFeature("METAL",
            "<html><body>hello<em>world</em></body></html>", 1, 3,
            Float.NaN, "group");

    sar.appendFeature(sb, 1, null, sf, null, 0);
    // !! strips off </body> but not <body> ??
    assertEquals("METAL 1 3; <body>hello<em>world</em>", sb.toString());

    sb.setLength(0);
    sf.setDescription("<br>&kHD>6");
    sar.appendFeature(sb, 1, null, sf, null, 0);
    // if no <html> tag, html-encodes > and < (only):
    assertEquals("METAL 1 3; &lt;br&gt;&kHD&gt;6", sb.toString());
  }

  @Test(groups = "Functional")
  public void testCreateSequenceAnnotationReport()
  {
    SequenceAnnotationReport sar = new SequenceAnnotationReport(false);
    StringBuilder sb = new StringBuilder();

    SequenceI seq = new Sequence("s1", "MAKLKRFQSSTLL");
    seq.setDescription("SeqDesc");

    /*
     * positional features are ignored
     */
    seq.addSequenceFeature(
            new SequenceFeature("Domain", "Ferredoxin", 5, 10, 1f, null));
    sar.createSequenceAnnotationReport(sb, seq, true, true, null);
    assertEquals("<i>SeqDesc\n" + "\n" + "</i>", sb.toString());

    /*
     * non-positional feature
     */
    seq.addSequenceFeature(
            new SequenceFeature("Type1", "Nonpos", 0, 0, 1f, null));
    sb.setLength(0);
    sar.createSequenceAnnotationReport(sb, seq, true, true, null);
    String expected = "<i>SeqDesc\n" + "\n"
            + "<br/>Type1 ; Nonpos Score=1.0</i>";
    assertEquals(expected, sb.toString());

    /*
     * non-positional features not wanted
     */
    sb.setLength(0);
    sar.createSequenceAnnotationReport(sb, seq, true, false, null);
    assertEquals("<i>SeqDesc\n\n</i>", sb.toString());

    /*
     * add non-pos feature with score inside min-max range for feature type
     * minmax holds { [positionalMin, positionalMax], [nonPosMin, nonPosMax] }
     * score is only appended for positional features so ignored here!
     * minMax are not recorded for non-positional features
     */
    seq.addSequenceFeature(
            new SequenceFeature("Metal", "Desc", 0, 0, 5f, null));

    FeatureRendererModel fr = new FeatureRenderer(null);
    Map<String, float[][]> minmax = fr.getMinMax();
    minmax.put("Metal", new float[][] { null, new float[] { 2f, 5f } });

    sb.setLength(0);
    sar.createSequenceAnnotationReport(sb, seq, true, true, fr);
    expected = "<i>SeqDesc\n" + "\n"
            + "<br/>Metal ; Desc<br/>Type1 ; Nonpos</i>";
    assertEquals(expected, sb.toString());

    /*
     * 'linkonly' features are ignored; this is obsolete, as linkonly
     * is only set by DasSequenceFetcher, and DAS is history
     */
    SequenceFeature sf = new SequenceFeature("Metal", "Desc", 0, 0, 5f,
            null);
    sf.setValue("linkonly", Boolean.TRUE);
    seq.addSequenceFeature(sf);
    sb.setLength(0);
    sar.createSequenceAnnotationReport(sb, seq, true, true, fr);
    assertEquals(expected, sb.toString()); // unchanged!

    /*
     * 'clinical_significance' attribute is only included in description 
     * when used for feature colouring
     */
    SequenceFeature sf2 = new SequenceFeature("Variant", "Havana", 0, 0, 5f,
            null);
    sf2.setValue(GffConstants.CLINICAL_SIGNIFICANCE, "benign");
    seq.addSequenceFeature(sf2);
    sb.setLength(0);
    sar.createSequenceAnnotationReport(sb, seq, true, true, fr);
    expected = "<i>SeqDesc\n" + "\n"
            + "<br/>Metal ; Desc<br/>Type1 ; Nonpos<br/>Variant ; Havana</i>";
    assertEquals(expected, sb.toString());

    /*
     * add dbrefs
     */
    seq.addDBRef(new DBRefEntry("PDB", "0", "3iu1"));
    seq.addDBRef(new DBRefEntry("Uniprot", "1", "P30419"));

    // with showDbRefs = false
    sb.setLength(0);
    sar.createSequenceAnnotationReport(sb, seq, false, true, fr);
    assertEquals(expected, sb.toString()); // unchanged

    // with showDbRefs = true, colour Variant features by clinical_significance
    sb.setLength(0);
    FeatureColourI fc = new FeatureColour(null, Color.green, Color.pink,
            null, 2, 3);
    fc.setAttributeName("clinical_significance");
    fr.setColour("Variant", fc);
    sar.createSequenceAnnotationReport(sb, seq, true, true, fr);
    expected = "<i>SeqDesc\n" + "<br/>\n" + "UNIPROT P30419<br/>\n"
            + "PDB 3iu1\n"
            + "<br/>Metal ; Desc<br/>Type1 ; Nonpos<br/>Variant ; Havana; clinical_significance=benign</i>";
    assertEquals(expected, sb.toString());
    // with showNonPositionalFeatures = false
    sb.setLength(0);
    sar.createSequenceAnnotationReport(sb, seq, true, false, fr);
    expected = "<i>SeqDesc\n" + "<br/>\n" + "UNIPROT P30419<br/>\n"
            + "PDB 3iu1\n" + "</i>";
    assertEquals(expected, sb.toString());

    /*
     * long feature description is truncated with ellipsis
     */
    sb.setLength(0);
    sf2.setDescription(
            "This is a very long description which should be truncated");
    sar.createSequenceAnnotationReport(sb, seq, false, true, fr);
    expected = "<i>SeqDesc\n" + "\n"
            + "<br/>Metal ; Desc<br/>Type1 ; Nonpos<br/>Variant ; This is a very long description which sh...; clinical_significance=benign</i>";
    assertEquals(expected, sb.toString());

    // see other tests for treatment of status and html
  }

  /**
   * Test that exercises an abbreviated sequence details report, with ellipsis
   * where there are more than 40 different sources, or more than 4 dbrefs for a
   * single source
   */
  @Test(groups = "Functional")
  public void testCreateSequenceAnnotationReport_withEllipsis()
  {
    SequenceAnnotationReport sar = new SequenceAnnotationReport(false);
    StringBuilder sb = new StringBuilder();

    SequenceI seq = new Sequence("s1", "ABC");

    int maxSources = (int) PA.getValue(sar, "MAX_SOURCES");
    for (int i = 0; i <= maxSources; i++)
    {
      seq.addDBRef(new DBRefEntry("PDB" + i, "0", "3iu1"));
    }

    int maxRefs = (int) PA.getValue(sar, "MAX_REFS_PER_SOURCE");
    for (int i = 0; i <= maxRefs; i++)
    {
      seq.addDBRef(new DBRefEntry("Uniprot", "0", "P3041" + i));
    }

    sar.createSequenceAnnotationReport(sb, seq, true, true, null, true);
    String report = sb.toString();
    assertTrue(report.startsWith("<i>\n" + "<br/>\n" + "UNIPROT P30410,\n"
            + " P30411,\n" + " P30412,\n" + " P30413,...<br/>\n"
            + "PDB0 3iu1<br/>\n" + "PDB1 3iu1<br/>"));
    assertTrue(report.endsWith("PDB5 3iu1<br/>\n" + "PDB6 3iu1<br/>\n"
            + "PDB7 3iu1<br/>\n" + "PDB8,...<br/>\n"
            + "(Output Sequence Details to list all database references)\n"
            + "</i>"));
  }

  /**
   * Test adding a linked feature to the tooltip
   */
  @Test(groups = "Functional")
  public void testAppendFeature_virtualFeature()
  {
    /*
     * map CDS to peptide sequence
     */
    SequenceI cds = new Sequence("Cds/101-121", "CCTttgAGAtttCAAatgGAT");
    SequenceI peptide = new Sequence("Peptide/8-14", "PLRFQMD");
    MapList map = new MapList(new int[] { 101, 118 }, new int[] { 8, 13 },
            3, 1);
    Mapping mapping = new Mapping(peptide, map);

    /*
     * assume variant feature found at CDS position 106 G>C
     */
    List<SequenceFeature> features = new ArrayList<>();
    // vary ttg (Leu) to ttc (Phe)
    SequenceFeature sf = new SequenceFeature("variant", "G,C", 106, 106,
            Float.NaN, null);
    features.add(sf);
    MappedFeatures mf = new MappedFeatures(mapping, cds, 9, 'L', features);

    StringBuilder sb = new StringBuilder();
    SequenceAnnotationReport sar = new SequenceAnnotationReport(false);
    sar.appendFeature(sb, 1, null, sf, mf, 0);

    /*
     * linked feature shown in tooltip in protein coordinates
     */
    assertEquals("variant 9; G,C", sb.toString());

    /*
     * adding "alleles" attribute to variant allows peptide consequence
     * to be calculated and added to the tooltip
     */
    sf.setValue("alleles", "G,C");
    sb = new StringBuilder();
    sar.appendFeature(sb, 1, null, sf, mf, 0);
    assertEquals("variant 9; G,C p.Leu9Phe", sb.toString());

    /*
     * now a virtual peptide feature on CDS
     * feature at 11-12 on peptide maps to 110-115 on CDS
     * here we test for tooltip at 113 (t)
     */
    SequenceFeature sf2 = new SequenceFeature("metal", "Fe", 11, 12, 2.3f,
            "Uniprot");
    features.clear();
    features.add(sf2);
    mapping = new Mapping(peptide, map);
    mf = new MappedFeatures(mapping, peptide, 113, 't', features);
    sb = new StringBuilder();
    sar.appendFeature(sb, 1, null, sf2, mf, 0);
    assertEquals("metal 110 115; Fe Score=2.3", sb.toString());
  }
}
