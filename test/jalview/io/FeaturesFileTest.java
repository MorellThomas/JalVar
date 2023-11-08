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
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.api.FeatureColourI;
import jalview.api.FeatureRenderer;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceDummy;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.datamodel.features.FeatureMatcher;
import jalview.datamodel.features.FeatureMatcherI;
import jalview.datamodel.features.FeatureMatcherSet;
import jalview.datamodel.features.FeatureMatcherSetI;
import jalview.datamodel.features.SequenceFeatures;
import jalview.gui.AlignFrame;
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;
import jalview.schemes.FeatureColour;
import jalview.structure.StructureSelectionManager;
import jalview.util.matcher.Condition;
import jalview.viewmodel.seqfeatures.FeatureRendererModel;
import jalview.viewmodel.seqfeatures.FeatureRendererModel.FeatureSettingsBean;
import junit.extensions.PA;

public class FeaturesFileTest
{
  private static String simpleGffFile = "examples/testdata/simpleGff3.gff";

  @AfterClass(alwaysRun = true)
  public void tearDownAfterClass()
  {
    /*
     * remove any sequence mappings created so they don't pollute other tests
     */
    StructureSelectionManager ssm = StructureSelectionManager
            .getStructureSelectionManager(Desktop.instance);
    ssm.resetAll();
  }

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testParse() throws Exception
  {
    File f = new File("examples/uniref50.fa");
    AlignmentI al = readAlignmentFile(f);
    AlignFrame af = new AlignFrame(al, 500, 500);
    Map<String, FeatureColourI> colours = af.getFeatureRenderer()
            .getFeatureColours();
    FeaturesFile featuresFile = new FeaturesFile(
            "examples/exampleFeatures.txt", DataSourceType.FILE);
    assertTrue(
            "Test " + "Features file test"
                    + "\nFailed to parse features file.",
            featuresFile.parse(al.getDataset(), colours, true));

    /*
     * Refetch the colour map from the FeatureRenderer (to confirm it has been
     * updated - JAL-1904), and verify (some) feature group colours
     */
    colours = af.getFeatureRenderer().getFeatureColours();
    assertEquals("27 feature group colours not found", 27, colours.size());
    assertEquals(colours.get("Cath").getColour(), new Color(0x93b1d1));
    assertEquals(colours.get("ASX-MOTIF").getColour(), new Color(0x6addbb));
    FeatureColourI kdColour = colours.get("kdHydrophobicity");
    assertTrue(kdColour.isGraduatedColour());
    assertTrue(kdColour.isAboveThreshold());
    assertEquals(-2f, kdColour.getThreshold());

    /*
     * verify (some) features on sequences
     */
    List<SequenceFeature> sfs = al.getSequenceAt(0).getDatasetSequence()
            .getSequenceFeatures(); // FER_CAPAA
    SequenceFeatures.sortFeatures(sfs, true);
    assertEquals(8, sfs.size());

    /*
     * verify (in ascending start position order)
     */
    SequenceFeature sf = sfs.get(0);
    assertEquals("Pfam family%LINK%", sf.description);
    assertEquals(0, sf.begin);
    assertEquals(0, sf.end);
    assertEquals("uniprot", sf.featureGroup);
    assertEquals("Pfam", sf.type);
    assertEquals(1, sf.links.size());
    assertEquals("Pfam family|http://pfam.xfam.org/family/PF00111",
            sf.links.get(0));

    sf = sfs.get(1);
    assertEquals("Ferredoxin_fold Status: True Positive ", sf.description);
    assertEquals(3, sf.begin);
    assertEquals(93, sf.end);
    assertEquals("uniprot", sf.featureGroup);
    assertEquals("Cath", sf.type);

    sf = sfs.get(2);
    assertEquals("Fer2 Status: True Positive Pfam 8_8%LINK%",
            sf.description);
    assertEquals("Pfam 8_8|http://pfam.xfam.org/family/PF00111",
            sf.links.get(0));
    assertEquals(8, sf.begin);
    assertEquals(83, sf.end);
    assertEquals("uniprot", sf.featureGroup);
    assertEquals("Pfam", sf.type);

    sf = sfs.get(3);
    assertEquals("Iron-sulfur (2Fe-2S)", sf.description);
    assertEquals(39, sf.begin);
    assertEquals(39, sf.end);
    assertEquals("uniprot", sf.featureGroup);
    assertEquals("METAL", sf.type);

    sf = sfs.get(4);
    assertEquals("Iron-sulfur (2Fe-2S)", sf.description);
    assertEquals(44, sf.begin);
    assertEquals(44, sf.end);
    assertEquals("uniprot", sf.featureGroup);
    assertEquals("METAL", sf.type);

    sf = sfs.get(5);
    assertEquals("Iron-sulfur (2Fe-2S)", sf.description);
    assertEquals(47, sf.begin);
    assertEquals(47, sf.end);
    assertEquals("uniprot", sf.featureGroup);
    assertEquals("METAL", sf.type);

    sf = sfs.get(6);
    assertEquals("Iron-sulfur (2Fe-2S)", sf.description);
    assertEquals(77, sf.begin);
    assertEquals(77, sf.end);
    assertEquals("uniprot", sf.featureGroup);
    assertEquals("METAL", sf.type);

    sf = sfs.get(7);
    assertEquals(
            "High confidence server. Only hits with scores over 0.8 are reported. PHOSPHORYLATION (T) 89_8%LINK%",
            sf.description);
    assertEquals(
            "PHOSPHORYLATION (T) 89_8|http://www.cbs.dtu.dk/cgi-bin/proview/webface-link?seqid=P83527&amp;service=NetPhos-2.0",
            sf.links.get(0));
    assertEquals(89, sf.begin);
    assertEquals(89, sf.end);
    assertEquals("netphos", sf.featureGroup);
    assertEquals("PHOSPHORYLATION (T)", sf.type);
  }

  /**
   * Test parsing a features file with a mix of Jalview and GFF formatted
   * content
   * 
   * @throws Exception
   */
  @Test(groups = { "Functional" })
  public void testParse_mixedJalviewGff() throws Exception
  {
    File f = new File("examples/uniref50.fa");
    AlignmentI al = readAlignmentFile(f);
    AlignFrame af = new AlignFrame(al, 500, 500);
    Map<String, FeatureColourI> colours = af.getFeatureRenderer()
            .getFeatureColours();
    // GFF2 uses space as name/value separator in column 9
    String gffData = "METAL\tcc9900\n" + "GFF\n"
            + "FER_CAPAA\tuniprot\tMETAL\t44\t45\t4.0\t.\t.\tNote Iron-sulfur; Note 2Fe-2S\n"
            + "FER1_SOLLC\tuniprot\tPfam\t55\t130\t2.0\t.\t.";
    FeaturesFile featuresFile = new FeaturesFile(gffData,
            DataSourceType.PASTE);
    assertTrue("Failed to parse features file",
            featuresFile.parse(al.getDataset(), colours, true));

    // verify colours read or synthesized
    colours = af.getFeatureRenderer().getFeatureColours();
    assertEquals("1 feature group colours not found", 1, colours.size());
    assertEquals(colours.get("METAL").getColour(), new Color(0xcc9900));

    // verify feature on FER_CAPAA
    List<SequenceFeature> sfs = al.getSequenceAt(0).getDatasetSequence()
            .getSequenceFeatures();
    assertEquals(1, sfs.size());
    SequenceFeature sf = sfs.get(0);
    assertEquals("Iron-sulfur,2Fe-2S", sf.description);
    assertEquals(44, sf.begin);
    assertEquals(45, sf.end);
    assertEquals("uniprot", sf.featureGroup);
    assertEquals("METAL", sf.type);
    assertEquals(4f, sf.getScore(), 0.001f);

    // verify feature on FER1_SOLLC
    sfs = al.getSequenceAt(2).getDatasetSequence().getSequenceFeatures();
    assertEquals(1, sfs.size());
    sf = sfs.get(0);
    assertEquals("uniprot", sf.description);
    assertEquals(55, sf.begin);
    assertEquals(130, sf.end);
    assertEquals("uniprot", sf.featureGroup);
    assertEquals("Pfam", sf.type);
    assertEquals(2f, sf.getScore(), 0.001f);
  }

  public static AlignmentI readAlignmentFile(File f) throws IOException
  {
    System.out.println("Reading file: " + f);
    String ff = f.getPath();
    FormatAdapter rf = new FormatAdapter();

    AlignmentI al = rf.readFile(ff, DataSourceType.FILE,
            new IdentifyFile().identify(ff, DataSourceType.FILE));

    al.setDataset(null); // creates dataset sequences
    assertNotNull("Couldn't read supplied alignment data.", al);
    return al;
  }

  /**
   * Test parsing a features file with GFF formatted content only
   * 
   * @throws Exception
   */
  @Test(groups = { "Functional" })
  public void testParse_pureGff3() throws Exception
  {
    File f = new File("examples/uniref50.fa");
    AlignmentI al = readAlignmentFile(f);
    AlignFrame af = new AlignFrame(al, 500, 500);
    Map<String, FeatureColourI> colours = af.getFeatureRenderer()
            .getFeatureColours();
    // GFF3 uses '=' separator for name/value pairs in column 9
    // comma (%2C) equals (%3D) or semi-colon (%3B) should be url-escaped in
    // values
    String gffData = "##gff-version 3\n"
            + "FER_CAPAA\tuniprot\tMETAL\t39\t39\t0.0\t.\t.\t"
            + "Note=Iron-sulfur (2Fe-2S);Note=another note,and another;evidence=ECO%3B0000255%2CPROSITE%3DProRule:PRU00465;"
            + "CSQ=AF=21,POLYPHEN=benign,possibly_damaging,clin_sig=Benign%3Dgood\n"
            + "FER1_SOLLC\tuniprot\tPfam\t55\t130\t3.0\t.\t.\tID=$23";
    FeaturesFile featuresFile = new FeaturesFile(gffData,
            DataSourceType.PASTE);
    assertTrue("Failed to parse features file",
            featuresFile.parse(al.getDataset(), colours, true));

    // verify feature on FER_CAPAA
    List<SequenceFeature> sfs = al.getSequenceAt(0).getDatasetSequence()
            .getSequenceFeatures();
    assertEquals(1, sfs.size());
    SequenceFeature sf = sfs.get(0);
    // description parsed from Note attribute
    assertEquals("Iron-sulfur (2Fe-2S),another note,and another",
            sf.description);
    assertEquals(39, sf.begin);
    assertEquals(39, sf.end);
    assertEquals("uniprot", sf.featureGroup);
    assertEquals("METAL", sf.type);
    assertEquals(5, sf.otherDetails.size());
    assertEquals("ECO;0000255,PROSITE=ProRule:PRU00465", // url decoded
            sf.getValue("evidence"));
    assertEquals("Iron-sulfur (2Fe-2S),another note,and another",
            sf.getValue("Note"));
    assertEquals("21", sf.getValueAsString("CSQ", "AF"));
    assertEquals("benign,possibly_damaging",
            sf.getValueAsString("CSQ", "POLYPHEN"));
    assertEquals("Benign=good", sf.getValueAsString("CSQ", "clin_sig")); // url
                                                                         // decoded
    // todo change STRAND and !Phase into fields of SequenceFeature instead
    assertEquals(".", sf.otherDetails.get("STRAND"));
    assertEquals(0, sf.getStrand());
    assertEquals(".", sf.getPhase());

    // verify feature on FER1_SOLLC1
    sfs = al.getSequenceAt(2).getDatasetSequence().getSequenceFeatures();
    assertEquals(1, sfs.size());
    sf = sfs.get(0);
    // ID used for description if available
    assertEquals("$23", sf.description);
    assertEquals(55, sf.begin);
    assertEquals(130, sf.end);
    assertEquals("uniprot", sf.featureGroup);
    assertEquals("Pfam", sf.type);
    assertEquals(3f, sf.getScore(), 0.001f);
  }

  /**
   * Test parsing a features file with Jalview format features (but no colour
   * descriptors or startgroup to give the hint not to parse as GFF)
   * 
   * @throws Exception
   */
  @Test(groups = { "Functional" })
  public void testParse_jalviewFeaturesOnly() throws Exception
  {
    File f = new File("examples/uniref50.fa");
    AlignmentI al = readAlignmentFile(f);
    AlignFrame af = new AlignFrame(al, 500, 500);
    Map<String, FeatureColourI> colours = af.getFeatureRenderer()
            .getFeatureColours();

    /*
     * one feature on FER_CAPAA and one on sequence 3 (index 2) FER1_SOLLC
     */
    String featureData = "Iron-sulfur (2Fe-2S)\tFER_CAPAA\t-1\t39\t39\tMETAL\n"
            + "Iron-phosphorus (2Fe-P)\tID_NOT_SPECIFIED\t2\t86\t87\tMETALLIC\n";
    FeaturesFile featuresFile = new FeaturesFile(featureData,
            DataSourceType.PASTE);
    assertTrue("Failed to parse features file",
            featuresFile.parse(al.getDataset(), colours, true));

    // verify FER_CAPAA feature
    List<SequenceFeature> sfs = al.getSequenceAt(0).getDatasetSequence()
            .getSequenceFeatures();
    assertEquals(1, sfs.size());
    SequenceFeature sf = sfs.get(0);
    assertEquals("Iron-sulfur (2Fe-2S)", sf.description);
    assertEquals(39, sf.begin);
    assertEquals(39, sf.end);
    assertEquals("METAL", sf.type);

    // verify FER1_SOLLC feature
    sfs = al.getSequenceAt(2).getDatasetSequence().getSequenceFeatures();
    assertEquals(1, sfs.size());
    sf = sfs.get(0);
    assertEquals("Iron-phosphorus (2Fe-P)", sf.description);
    assertEquals(86, sf.begin);
    assertEquals(87, sf.end);
    assertEquals("METALLIC", sf.type);
  }

  private void checkDatasetfromSimpleGff3(AlignmentI dataset)
  {
    assertEquals("no sequences extracted from GFF3 file", 2,
            dataset.getHeight());

    SequenceI seq1 = dataset.findName("seq1");
    SequenceI seq2 = dataset.findName("seq2");
    assertNotNull(seq1);
    assertNotNull(seq2);
    assertFalse("Failed to replace dummy seq1 with real sequence",
            seq1 instanceof SequenceDummy
                    && ((SequenceDummy) seq1).isDummy());
    assertFalse("Failed to replace dummy seq2 with real sequence",
            seq2 instanceof SequenceDummy
                    && ((SequenceDummy) seq2).isDummy());
    String placeholderseq = new SequenceDummy("foo").getSequenceAsString();
    assertFalse("dummy replacement buggy for seq1",
            placeholderseq.equals(seq1.getSequenceAsString()));
    assertFalse("dummy replacement buggy for seq2",
            placeholderseq.equals(seq2.getSequenceAsString()));
    assertNotNull("No features added to seq1", seq1.getSequenceFeatures());
    assertEquals("Wrong number of features", 3,
            seq1.getSequenceFeatures().size());
    assertTrue(seq2.getSequenceFeatures().isEmpty());
    assertEquals("Wrong number of features", 0,
            seq2.getSequenceFeatures() == null ? 0
                    : seq2.getSequenceFeatures().size());
    assertTrue("Expected at least one CDNA/Protein mapping for seq1",
            dataset.getCodonFrame(seq1) != null
                    && dataset.getCodonFrame(seq1).size() > 0);

  }

  @Test(groups = { "Functional" })
  public void readGff3File() throws IOException
  {
    FeaturesFile gffreader = new FeaturesFile(true, simpleGffFile,
            DataSourceType.FILE);
    Alignment dataset = new Alignment(gffreader.getSeqsAsArray());
    gffreader.addProperties(dataset);
    checkDatasetfromSimpleGff3(dataset);
  }

  @Test(groups = { "Functional" })
  public void simpleGff3FileClass() throws IOException
  {
    AlignmentI dataset = new Alignment(new SequenceI[] {});
    FeaturesFile ffile = new FeaturesFile(simpleGffFile,
            DataSourceType.FILE);

    boolean parseResult = ffile.parse(dataset, null, false, false);
    assertTrue("return result should be true", parseResult);
    checkDatasetfromSimpleGff3(dataset);
  }

  @Test(groups = { "Functional" })
  public void simpleGff3FileLoader() throws IOException
  {
    AlignFrame af = new FileLoader(false)
            .LoadFileWaitTillLoaded(simpleGffFile, DataSourceType.FILE);
    assertTrue(
            "Didn't read the alignment into an alignframe from Gff3 File",
            af != null);
    checkDatasetfromSimpleGff3(af.getViewport().getAlignment());
  }

  @Test(groups = { "Functional" })
  public void simpleGff3RelaxedIdMatching() throws IOException
  {
    AlignmentI dataset = new Alignment(new SequenceI[] {});
    FeaturesFile ffile = new FeaturesFile(simpleGffFile,
            DataSourceType.FILE);

    boolean parseResult = ffile.parse(dataset, null, false, true);
    assertTrue("return result (relaxedID matching) should be true",
            parseResult);
    checkDatasetfromSimpleGff3(dataset);
  }

  @Test(groups = { "Functional" })
  public void testPrintJalviewFormat() throws Exception
  {
    File f = new File("examples/uniref50.fa");
    AlignmentI al = readAlignmentFile(f);
    AlignFrame af = new AlignFrame(al, 500, 500);
    Map<String, FeatureColourI> colours = af.getFeatureRenderer()
            .getFeatureColours();
    String features = "METAL\tcc9900\n"
            + "GAMMA-TURN\tred|0,255,255|20.0|95.0|below|66.0\n"
            + "Pfam\tred\n" + "STARTGROUP\tuniprot\n"
            + "Cath\tFER_CAPAA\t-1\t0\t0\tDomain\n" // non-positional feature
            + "Iron\tFER_CAPAA\t-1\t39\t39\tMETAL\n"
            + "Turn\tFER_CAPAA\t-1\t36\t38\tGAMMA-TURN\n"
            + "<html>Pfam domain<a href=\"http://pfam.xfam.org/family/PF00111\">Pfam_3_4</a></html>\tFER_CAPAA\t-1\t20\t20\tPfam\n"
            + "ENDGROUP\tuniprot\n";
    FeaturesFile featuresFile = new FeaturesFile(features,
            DataSourceType.PASTE);
    featuresFile.parse(al.getDataset(), colours, false);

    /*
     * add positional and non-positional features with null and
     * empty feature group to check handled correctly
     */
    SequenceI seq = al.getSequenceAt(1); // FER_CAPAN
    seq.addSequenceFeature(
            new SequenceFeature("Pfam", "desc1", 0, 0, 1.3f, null));
    seq.addSequenceFeature(
            new SequenceFeature("Pfam", "desc2", 4, 9, Float.NaN, null));
    seq = al.getSequenceAt(2); // FER1_SOLLC
    seq.addSequenceFeature(
            new SequenceFeature("Pfam", "desc3", 0, 0, Float.NaN, ""));
    seq.addSequenceFeature(
            new SequenceFeature("Pfam", "desc4", 5, 8, -2.6f, ""));

    /*
     * first with no features displayed, exclude non-positional features
     */
    FeatureRenderer fr = af.alignPanel.getFeatureRenderer();
    String exported = featuresFile
            .printJalviewFormat(al.getSequencesArray(), fr, false, false);
    String expected = "No Features Visible";
    assertEquals(expected, exported);

    /*
     * include non-positional features, but still no positional features
     */
    fr.setGroupVisibility("uniprot", true);
    exported = featuresFile.printJalviewFormat(al.getSequencesArray(), fr,
            true, false);
    expected = "\nSTARTGROUP\tuniprot\n"
            + "Cath\tFER_CAPAA\t-1\t0\t0\tDomain\t0.0\n"
            + "ENDGROUP\tuniprot\n\n"
            + "desc1\tFER_CAPAN\t-1\t0\t0\tPfam\t1.3\n\n"
            + "desc3\tFER1_SOLLC\t-1\t0\t0\tPfam\n"; // NaN is not output
    assertEquals(expected, exported);

    /*
     * set METAL (in uniprot group) and GAMMA-TURN visible, but not Pfam
     */
    fr.setVisible("METAL");
    fr.setVisible("GAMMA-TURN");
    exported = featuresFile.printJalviewFormat(al.getSequencesArray(), fr,
            false, false);
    expected = "METAL\tcc9900\n"
            + "GAMMA-TURN\tscore|ff0000|00ffff|noValueMin|20.0|95.0|below|66.0\n"
            + "\nSTARTGROUP\tuniprot\n"
            + "Iron\tFER_CAPAA\t-1\t39\t39\tMETAL\t0.0\n"
            + "Turn\tFER_CAPAA\t-1\t36\t38\tGAMMA-TURN\t0.0\n"
            + "ENDGROUP\tuniprot\n";
    assertEquals(expected, exported);

    /*
     * now set Pfam visible
     */
    fr.setVisible("Pfam");
    exported = featuresFile.printJalviewFormat(al.getSequencesArray(), fr,
            false, false);
    /*
     * features are output within group, ordered by sequence and type
     */
    expected = "METAL\tcc9900\n" + "Pfam\tff0000\n"
            + "GAMMA-TURN\tscore|ff0000|00ffff|noValueMin|20.0|95.0|below|66.0\n"
            + "\nSTARTGROUP\tuniprot\n"
            + "Iron\tFER_CAPAA\t-1\t39\t39\tMETAL\t0.0\n"
            + "<html>Pfam domain<a href=\"http://pfam.xfam.org/family/PF00111\">Pfam_3_4</a></html>\tFER_CAPAA\t-1\t20\t20\tPfam\t0.0\n"
            + "Turn\tFER_CAPAA\t-1\t36\t38\tGAMMA-TURN\t0.0\n"
            + "ENDGROUP\tuniprot\n"
            // null / empty group features are output after named groups
            + "\ndesc2\tFER_CAPAN\t-1\t4\t9\tPfam\n"
            + "\ndesc4\tFER1_SOLLC\t-1\t5\t8\tPfam\t-2.6\n";
    assertEquals(expected, exported);

    /*
     * hide uniprot group
     */
    fr.setGroupVisibility("uniprot", false);
    expected = "METAL\tcc9900\n" + "Pfam\tff0000\n"
            + "GAMMA-TURN\tscore|ff0000|00ffff|noValueMin|20.0|95.0|below|66.0\n"
            + "\ndesc2\tFER_CAPAN\t-1\t4\t9\tPfam\n"
            + "\ndesc4\tFER1_SOLLC\t-1\t5\t8\tPfam\t-2.6\n";
    exported = featuresFile.printJalviewFormat(al.getSequencesArray(), fr,
            false, false);
    assertEquals(expected, exported);

    /*
     * include non-positional (overrides group not shown)
     */
    exported = featuresFile.printJalviewFormat(al.getSequencesArray(), fr,
            true, false);
    expected = "METAL\tcc9900\n" + "Pfam\tff0000\n"
            + "GAMMA-TURN\tscore|ff0000|00ffff|noValueMin|20.0|95.0|below|66.0\n"
            + "\nSTARTGROUP\tuniprot\n"
            + "Cath\tFER_CAPAA\t-1\t0\t0\tDomain\t0.0\n"
            + "ENDGROUP\tuniprot\n"
            + "\ndesc1\tFER_CAPAN\t-1\t0\t0\tPfam\t1.3\n"
            + "desc2\tFER_CAPAN\t-1\t4\t9\tPfam\n"
            + "\ndesc3\tFER1_SOLLC\t-1\t0\t0\tPfam\n"
            + "desc4\tFER1_SOLLC\t-1\t5\t8\tPfam\t-2.6\n";
    assertEquals(expected, exported);
  }

  @Test(groups = { "Functional" })
  public void testPrintGffFormat() throws Exception
  {
    File f = new File("examples/uniref50.fa");
    AlignmentI al = readAlignmentFile(f);
    AlignFrame af = new AlignFrame(al, 500, 500);

    /*
     * no features
     */
    FeaturesFile featuresFile = new FeaturesFile();
    FeatureRendererModel fr = (FeatureRendererModel) af.alignPanel
            .getFeatureRenderer();
    String exported = featuresFile.printGffFormat(al.getSequencesArray(),
            fr, false, false);
    String gffHeader = "##gff-version 2\n";
    assertEquals(gffHeader, exported);
    exported = featuresFile.printGffFormat(al.getSequencesArray(), fr, true,
            false);
    assertEquals(gffHeader, exported);

    /*
     * add some features
     */
    al.getSequenceAt(0).addSequenceFeature(
            new SequenceFeature("Domain", "Cath", 0, 0, 0f, "Uniprot"));
    al.getSequenceAt(0).addSequenceFeature(
            new SequenceFeature("METAL", "Cath", 39, 39, 1.2f, null));
    al.getSequenceAt(1).addSequenceFeature(new SequenceFeature("GAMMA-TURN",
            "Turn", 36, 38, 2.1f, "s3dm"));
    SequenceFeature sf = new SequenceFeature("Pfam", "", 20, 20, 0f,
            "Uniprot");
    sf.setStrand("+");
    sf.setPhase("2");
    sf.setValue("x", "y");
    sf.setValue("black", "white");
    Map<String, String> csq = new HashMap<>();
    csq.put("SIFT", "benign,mostly benign,cloudy, with meatballs");
    csq.put("consequence", "missense_variant");
    sf.setValue("CSQ", csq);
    al.getSequenceAt(1).addSequenceFeature(sf);

    /*
     * 'discover' features then hide all feature types
     */
    fr.findAllFeatures(true);
    FeatureSettingsBean[] data = new FeatureSettingsBean[4];
    FeatureColourI fc = new FeatureColour(Color.PINK);
    data[0] = new FeatureSettingsBean("Domain", fc, null, false);
    data[1] = new FeatureSettingsBean("METAL", fc, null, false);
    data[2] = new FeatureSettingsBean("GAMMA-TURN", fc, null, false);
    data[3] = new FeatureSettingsBean("Pfam", fc, null, false);
    fr.setFeaturePriority(data);

    /*
     * with no features displayed, exclude non-positional features
     */
    exported = featuresFile.printGffFormat(al.getSequencesArray(), fr,
            false, false);
    assertEquals(gffHeader, exported);

    /*
     * include non-positional features
     */
    fr.setGroupVisibility("Uniprot", true);
    fr.setGroupVisibility("s3dm", false);
    exported = featuresFile.printGffFormat(al.getSequencesArray(), fr, true,
            false);
    String expected = gffHeader
            + "FER_CAPAA\tUniprot\tDomain\t0\t0\t0.0\t.\t.\n";
    assertEquals(expected, exported);

    /*
     * set METAL (in uniprot group) and GAMMA-TURN visible, but not Pfam
     * only Uniprot group visible here...
     */
    fr.setVisible("METAL");
    fr.setVisible("GAMMA-TURN");
    exported = featuresFile.printGffFormat(al.getSequencesArray(), fr,
            false, false);
    // METAL feature has null group: description used for column 2
    expected = gffHeader + "FER_CAPAA\tCath\tMETAL\t39\t39\t1.2\t.\t.\n";
    assertEquals(expected, exported);

    /*
     * set s3dm group visible
     */
    fr.setGroupVisibility("s3dm", true);
    exported = featuresFile.printGffFormat(al.getSequencesArray(), fr,
            false, false);
    // METAL feature has null group: description used for column 2
    expected = gffHeader + "FER_CAPAA\tCath\tMETAL\t39\t39\t1.2\t.\t.\n"
            + "FER_CAPAN\ts3dm\tGAMMA-TURN\t36\t38\t2.1\t.\t.\n";
    assertEquals(expected, exported);

    /*
     * now set Pfam visible
     */
    fr.setVisible("Pfam");
    exported = featuresFile.printGffFormat(al.getSequencesArray(), fr,
            false, false);
    // Pfam feature columns include strand(+), phase(2), attributes
    expected = gffHeader + "FER_CAPAA\tCath\tMETAL\t39\t39\t1.2\t.\t.\n"
    // CSQ output as CSQ=att1=value1,att2=value2
    // note all commas are encoded here which is wrong - it should be
    // SIFT=benign,mostly benign,cloudy%2C with meatballs
            + "FER_CAPAN\tUniprot\tPfam\t20\t20\t0.0\t+\t2\tx=y;black=white;"
            + "CSQ=SIFT=benign%2Cmostly benign%2Ccloudy%2C with meatballs,consequence=missense_variant\n"
            + "FER_CAPAN\ts3dm\tGAMMA-TURN\t36\t38\t2.1\t.\t.\n";
    assertEquals(expected, exported);
  }

  /**
   * Test for parsing of feature filters as represented in a Jalview features
   * file
   * 
   * @throws Exception
   */
  @Test(groups = { "Functional" })
  public void testParseFilters() throws Exception
  {
    Map<String, FeatureMatcherSetI> filters = new HashMap<>();
    String text = "sequence_variant\tCSQ:PolyPhen NotContains 'damaging'\n"
            + "missense_variant\t(label contains foobar) and (Score lt 1.3)";
    FeaturesFile featuresFile = new FeaturesFile(text,
            DataSourceType.PASTE);
    featuresFile.parseFilters(filters);
    assertEquals(filters.size(), 2);

    FeatureMatcherSetI fm = filters.get("sequence_variant");
    assertNotNull(fm);
    Iterator<FeatureMatcherI> matchers = fm.getMatchers().iterator();
    FeatureMatcherI matcher = matchers.next();
    assertFalse(matchers.hasNext());
    String[] attributes = matcher.getAttribute();
    assertArrayEquals(attributes, new String[] { "CSQ", "PolyPhen" });
    assertSame(matcher.getMatcher().getCondition(), Condition.NotContains);
    assertEquals(matcher.getMatcher().getPattern(), "damaging");

    fm = filters.get("missense_variant");
    assertNotNull(fm);
    matchers = fm.getMatchers().iterator();
    matcher = matchers.next();
    assertTrue(matcher.isByLabel());
    assertSame(matcher.getMatcher().getCondition(), Condition.Contains);
    assertEquals(matcher.getMatcher().getPattern(), "foobar");
    matcher = matchers.next();
    assertTrue(matcher.isByScore());
    assertSame(matcher.getMatcher().getCondition(), Condition.LT);
    assertEquals(matcher.getMatcher().getPattern(), "1.3");
    assertEquals(PA.getValue(matcher.getMatcher(), "floatValue"), 1.3f);

    assertFalse(matchers.hasNext());
  }

  @Test(groups = { "Functional" })
  public void testOutputFeatureFilters()
  {
    FeaturesFile ff = new FeaturesFile();
    StringBuilder sb = new StringBuilder();
    Map<String, FeatureColourI> visible = new HashMap<>();
    visible.put("pfam", new FeatureColour(Color.red));
    Map<String, FeatureMatcherSetI> featureFilters = new HashMap<>();

    // with no filters, nothing is output
    ff.outputFeatureFilters(sb, visible, featureFilters);
    assertEquals("", sb.toString());

    // with filter for not visible features only, nothing is output
    FeatureMatcherSet filter = new FeatureMatcherSet();
    filter.and(FeatureMatcher.byLabel(Condition.Present, null));
    featureFilters.put("foobar", filter);
    ff.outputFeatureFilters(sb, visible, featureFilters);
    assertEquals("", sb.toString());

    // with filters for visible feature types
    FeatureMatcherSet filter2 = new FeatureMatcherSet();
    filter2.and(FeatureMatcher.byAttribute(Condition.Present, null, "CSQ",
            "PolyPhen"));
    filter2.and(FeatureMatcher.byScore(Condition.LE, "-2.4"));
    featureFilters.put("pfam", filter2);
    visible.put("foobar", new FeatureColour(Color.blue));
    ff.outputFeatureFilters(sb, visible, featureFilters);
    String expected = "\nSTARTFILTERS\nfoobar\tLabel Present\npfam\t(CSQ:PolyPhen Present) AND (Score LE -2.4)\nENDFILTERS\n";
    assertEquals(expected, sb.toString());
  }

  /**
   * Output as GFF should not include features which are not visible due to
   * colour threshold or feature filter settings
   * 
   * @throws Exception
   */
  @Test(groups = { "Functional" })
  public void testPrintGffFormat_withFilters() throws Exception
  {
    File f = new File("examples/uniref50.fa");
    AlignmentI al = readAlignmentFile(f);
    AlignFrame af = new AlignFrame(al, 500, 500);
    SequenceFeature sf1 = new SequenceFeature("METAL", "Cath", 39, 39, 1.2f,
            null);
    sf1.setValue("clin_sig", "Likely Pathogenic");
    sf1.setValue("AF", "24");
    al.getSequenceAt(0).addSequenceFeature(sf1);
    SequenceFeature sf2 = new SequenceFeature("METAL", "Cath", 41, 41, 0.6f,
            null);
    sf2.setValue("clin_sig", "Benign");
    sf2.setValue("AF", "46");
    al.getSequenceAt(0).addSequenceFeature(sf2);

    FeaturesFile featuresFile = new FeaturesFile();
    FeatureRenderer fr = af.alignPanel.getFeatureRenderer();
    final String gffHeader = "##gff-version 2\n";

    fr.setVisible("METAL");
    fr.setColour("METAL", new FeatureColour(Color.PINK));
    String exported = featuresFile.printGffFormat(al.getSequencesArray(),
            fr, false, false);
    String expected = gffHeader
            + "FER_CAPAA\tCath\tMETAL\t39\t39\t1.2\t.\t.\tclin_sig=Likely Pathogenic;AF=24\n"
            + "FER_CAPAA\tCath\tMETAL\t41\t41\t0.6\t.\t.\tclin_sig=Benign;AF=46\n";
    assertEquals(expected, exported);

    /*
     * now threshold to Score > 1.1 - should exclude sf2
     */
    FeatureColourI fc = new FeatureColour(null, Color.white, Color.BLACK,
            Color.white, 0f, 2f);
    fc.setAboveThreshold(true);
    fc.setThreshold(1.1f);
    fr.setColour("METAL", fc);
    exported = featuresFile.printGffFormat(al.getSequencesArray(), fr,
            false, false);
    expected = gffHeader
            + "FER_CAPAA\tCath\tMETAL\t39\t39\t1.2\t.\t.\tclin_sig=Likely Pathogenic;AF=24\n";
    assertEquals(expected, exported);

    /*
     * remove threshold and check sf2 is exported
     */
    fc.setAboveThreshold(false);
    exported = featuresFile.printGffFormat(al.getSequencesArray(), fr,
            false, false);
    expected = gffHeader
            + "FER_CAPAA\tCath\tMETAL\t39\t39\t1.2\t.\t.\tclin_sig=Likely Pathogenic;AF=24\n"
            + "FER_CAPAA\tCath\tMETAL\t41\t41\t0.6\t.\t.\tclin_sig=Benign;AF=46\n";
    assertEquals(expected, exported);

    /*
     * filter on (clin_sig contains Benign) - should include sf2 and exclude sf1
     */
    FeatureMatcherSetI filter = new FeatureMatcherSet();
    filter.and(FeatureMatcher.byAttribute(Condition.Contains, "benign",
            "clin_sig"));
    fr.setFeatureFilter("METAL", filter);
    exported = featuresFile.printGffFormat(al.getSequencesArray(), fr,
            false, false);
    expected = gffHeader
            + "FER_CAPAA\tCath\tMETAL\t41\t41\t0.6\t.\t.\tclin_sig=Benign;AF=46\n";
    assertEquals(expected, exported);
  }

  /**
   * Output as Jalview should not include features which are not visible due to
   * colour threshold or feature filter settings
   * 
   * @throws Exception
   */
  @Test(groups = { "Functional" })
  public void testPrintJalviewFormat_withFilters() throws Exception
  {
    File f = new File("examples/uniref50.fa");
    AlignmentI al = readAlignmentFile(f);
    AlignFrame af = new AlignFrame(al, 500, 500);
    SequenceFeature sf1 = new SequenceFeature("METAL", "Cath", 39, 39, 1.2f,
            "grp1");
    sf1.setValue("clin_sig", "Likely Pathogenic");
    sf1.setValue("AF", "24");
    al.getSequenceAt(0).addSequenceFeature(sf1);
    SequenceFeature sf2 = new SequenceFeature("METAL", "Cath", 41, 41, 0.6f,
            "grp2");
    sf2.setValue("clin_sig", "Benign");
    sf2.setValue("AF", "46");
    al.getSequenceAt(0).addSequenceFeature(sf2);

    FeaturesFile featuresFile = new FeaturesFile();
    FeatureRenderer fr = af.alignPanel.getFeatureRenderer();
    fr.findAllFeatures(true);

    fr.setVisible("METAL");
    fr.setColour("METAL", new FeatureColour(Color.PINK));
    String exported = featuresFile
            .printJalviewFormat(al.getSequencesArray(), fr, false, false);
    String expected = "METAL\tffafaf\n\nSTARTGROUP\tgrp1\n"
            + "Cath\tFER_CAPAA\t-1\t39\t39\tMETAL\t1.2\n"
            + "ENDGROUP\tgrp1\n\nSTARTGROUP\tgrp2\n"
            + "Cath\tFER_CAPAA\t-1\t41\t41\tMETAL\t0.6\n"
            + "ENDGROUP\tgrp2\n";
    assertEquals(expected, exported);

    /*
     * now threshold to Score > 1.1 - should exclude sf2
     * (and there should be no empty STARTGROUP/ENDGROUP output)
     */
    FeatureColourI fc = new FeatureColour(null, Color.white, Color.BLACK,
            Color.white, 0f, 2f);
    fc.setAboveThreshold(true);
    fc.setThreshold(1.1f);
    fr.setColour("METAL", fc);
    exported = featuresFile.printJalviewFormat(al.getSequencesArray(), fr,
            false, false);
    expected = "METAL\tscore|ffffff|000000|noValueMin|abso|0.0|2.0|above|1.1\n\n"
            + "STARTGROUP\tgrp1\n"
            + "Cath\tFER_CAPAA\t-1\t39\t39\tMETAL\t1.2\n"
            + "ENDGROUP\tgrp1\n";
    assertEquals(expected, exported);

    /*
     * remove threshold and check sf2 is exported
     */
    fc.setAboveThreshold(false);
    exported = featuresFile.printJalviewFormat(al.getSequencesArray(), fr,
            false, false);
    expected = "METAL\tscore|ffffff|000000|noValueMin|abso|0.0|2.0|none\n\n"
            + "STARTGROUP\tgrp1\n"
            + "Cath\tFER_CAPAA\t-1\t39\t39\tMETAL\t1.2\n"
            + "ENDGROUP\tgrp1\n\nSTARTGROUP\tgrp2\n"
            + "Cath\tFER_CAPAA\t-1\t41\t41\tMETAL\t0.6\n"
            + "ENDGROUP\tgrp2\n";
    assertEquals(expected, exported);

    /*
     * filter on (clin_sig contains Benign) - should include sf2 and exclude sf1
     */
    FeatureMatcherSetI filter = new FeatureMatcherSet();
    filter.and(FeatureMatcher.byAttribute(Condition.Contains, "benign",
            "clin_sig"));
    fr.setFeatureFilter("METAL", filter);
    exported = featuresFile.printJalviewFormat(al.getSequencesArray(), fr,
            false, false);
    expected = "FER_CAPAA\tCath\tMETAL\t41\t41\t0.6\t.\t.\n";
    expected = "METAL\tscore|ffffff|000000|noValueMin|abso|0.0|2.0|none\n\n"
            + "STARTFILTERS\nMETAL\tclin_sig Contains benign\nENDFILTERS\n\n"
            + "STARTGROUP\tgrp2\n"
            + "Cath\tFER_CAPAA\t-1\t41\t41\tMETAL\t0.6\n"
            + "ENDGROUP\tgrp2\n";
    assertEquals(expected, exported);
  }
}
