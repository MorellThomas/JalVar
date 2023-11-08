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

import static org.testng.AssertJUnit.assertNotNull;

import jalview.api.AlignExportSettingsI;
import jalview.datamodel.AlignExportSettingsAdapter;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.datamodel.features.SequenceFeatures;
import jalview.gui.AlignFrame;
import jalview.gui.JvOptionPane;
import jalview.json.binding.biojson.v1.ColourSchemeMapper;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ResidueColourScheme;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class JSONFileTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  private int TEST_SEQ_HEIGHT = 0;

  private int TEST_GRP_HEIGHT = 0;

  private int TEST_ANOT_HEIGHT = 0;

  private int TEST_CS_HEIGHT = 0;

  private String TEST_JSON_FILE = "examples/example.json";

  private Alignment alignment;

  private HashMap<String, SequenceI> expectedSeqs = new HashMap<>();

  private HashMap<String, AlignmentAnnotation> expectedAnnots = new HashMap<>();

  private HashMap<String, SequenceGroup> expectedGrps = new HashMap<>();

  private HiddenColumns expectedColSel = new HiddenColumns();

  private SequenceI[] expectedHiddenSeqs = new SequenceI[1];

  private AlignmentI testAlignment;

  private int passedCount;

  private JSONFile testJsonFile;

  private JSONFile jf;

  private AlignExportSettingsI exportSettings;

  @BeforeTest(alwaysRun = true)
  public void setup() throws Exception
  {
    /*
     * construct expected values
     * nb this have to match the data in examples/example.json
     */
    // create and add sequences
    Sequence[] seqs = new Sequence[5];
    seqs[0] = new Sequence("FER_CAPAN",
            "SVSATMISTSFMPRKPAVTSL-KPIPNVGE--ALF", 3, 34);
    seqs[1] = new Sequence("FER1_SOLLC",
            "SISGTMISTSFLPRKPAVTSL-KAISNVGE--ALF", 3, 34);
    seqs[2] = new Sequence("Q93XJ9_SOLTU",
            "SISGTMISTSFLPRKPVVTSL-KAISNVGE--ALF", 3, 34);
    seqs[3] = new Sequence("FER1_PEA",
            "ALYGTAVSTSFLRTQPMPMSV-TTTKAFSN--GFL", 6, 37);
    seqs[4] = new Sequence("Q7XA98_TRIPR",
            "ALYGTAVSTSFMRRQPVPMSV-ATTTTTKAFPSGF", 6, 39);

    SequenceI hiddenSeq = new Sequence("FER_TOCH",
            "FILGTMISKSFLFRKPAVTSL-KAISNVGE--ALF", 3, 34);
    expectedHiddenSeqs[0] = hiddenSeq;

    // create and add sequence features
    SequenceFeature seqFeature2 = new SequenceFeature("feature_x",
            "theDesc", 6, 15, "Jalview");
    SequenceFeature seqFeature3 = new SequenceFeature("feature_x",
            "theDesc", 9, 18, "Jalview");
    SequenceFeature seqFeature4 = new SequenceFeature("feature_x",
            "theDesc", 9, 18, "Jalview");
    // non-positional feature:
    SequenceFeature seqFeature5 = new SequenceFeature("Domain",
            "My description", 0, 0, "Pfam");
    seqs[2].addSequenceFeature(seqFeature2);
    seqs[3].addSequenceFeature(seqFeature3);
    seqs[4].addSequenceFeature(seqFeature4);
    seqs[2].addSequenceFeature(seqFeature5);

    for (Sequence seq : seqs)
    {
      seq.createDatasetSequence();
      expectedSeqs.put(seq.getName(), seq);
    }

    // create and add a sequence group
    List<SequenceI> grpSeqs = new ArrayList<>();
    grpSeqs.add(seqs[1]);
    grpSeqs.add(seqs[2]);
    grpSeqs.add(seqs[3]);
    grpSeqs.add(seqs[4]);
    SequenceGroup seqGrp = new SequenceGroup(grpSeqs, "JGroup:1883305585",
            null, true, true, false, 21, 29);
    ColourSchemeI scheme = ColourSchemeMapper
            .getJalviewColourScheme("zappo", seqGrp);
    seqGrp.cs.setColourScheme(scheme);
    seqGrp.setShowNonconserved(false);
    seqGrp.setDescription(null);

    expectedGrps.put(seqGrp.getName(), seqGrp);

    // create and add annotation
    Annotation[] annot = new Annotation[35];
    annot[0] = new Annotation("", "", '\u0000', 0);
    annot[1] = new Annotation("", "", '\u0000', 0);
    annot[2] = new Annotation("α", "", 'H', 0);
    annot[3] = new Annotation("α", "", 'H', 0);
    annot[4] = new Annotation("α", "", 'H', 0);
    annot[5] = new Annotation("", "", '\u0000', 0);
    annot[6] = new Annotation("", "", '\u0000', 0);
    annot[7] = new Annotation("", "", '\u0000', 0);
    annot[8] = new Annotation("β", "", 'E', 0);
    annot[9] = new Annotation("β", "", 'E', 0);
    annot[10] = new Annotation("β", "", 'E', 0);
    annot[11] = new Annotation("β", "", 'E', 0);
    annot[12] = new Annotation("β", "", 'E', 0);
    annot[13] = new Annotation("β", "", 'E', 0);
    annot[14] = new Annotation("β", "", 'E', 0);
    annot[15] = new Annotation("β", "", 'E', 0);
    annot[16] = new Annotation("", "", '\u0000', 0);
    annot[17] = new Annotation("", "", '\u0000', 0);
    annot[18] = new Annotation("", "", '\u0000', 0);
    annot[19] = new Annotation("", "", '\u0000', 0);
    annot[20] = new Annotation("", "", '\u0000', 0);
    annot[21] = new Annotation("", "", '\u0000', 0);
    annot[22] = new Annotation("", "", '\u0000', 0);
    annot[23] = new Annotation("", "", '\u0000', 0);
    annot[24] = new Annotation("", "", '\u0000', 0);
    annot[25] = new Annotation("", "", '\u0000', 0);
    annot[26] = new Annotation("α", "", 'H', 0);
    annot[27] = new Annotation("α", "", 'H', 0);
    annot[28] = new Annotation("α", "", 'H', 0);
    annot[29] = new Annotation("α", "", 'H', 0);
    annot[30] = new Annotation("α", "", 'H', 0);
    annot[31] = new Annotation("", "", '\u0000', 0);
    annot[32] = new Annotation("", "", '\u0000', 0);
    annot[33] = new Annotation("", "", '\u0000', 0);
    annot[34] = new Annotation("", "", '\u0000', 0);

    AlignmentAnnotation alignAnnot = new AlignmentAnnotation(
            "Secondary Structure", "New description", annot);
    expectedAnnots.put(alignAnnot.label, alignAnnot);

    expectedColSel.hideColumns(32, 33);
    expectedColSel.hideColumns(34, 34);

    TEST_SEQ_HEIGHT = expectedSeqs.size();
    TEST_GRP_HEIGHT = expectedGrps.size();
    TEST_ANOT_HEIGHT = expectedAnnots.size();
    TEST_CS_HEIGHT = expectedColSel.getNumberOfRegions();

    exportSettings = new AlignExportSettingsAdapter(true);

    AppletFormatAdapter formatAdapter = new AppletFormatAdapter();
    try
    {
      alignment = (Alignment) formatAdapter.readFile(TEST_JSON_FILE,
              DataSourceType.FILE, FileFormat.Json);
      jf = (JSONFile) formatAdapter.getAlignFile();

      AlignFrame af = new AlignFrame(alignment, jf.getHiddenSequences(),
              jf.getHiddenColumns(), AlignFrame.DEFAULT_WIDTH,
              AlignFrame.DEFAULT_HEIGHT);
      af.getViewport().setShowSequenceFeatures(jf.isShowSeqFeatures());
      String colourSchemeName = jf.getGlobalColourScheme();
      ColourSchemeI cs = ColourSchemeMapper
              .getJalviewColourScheme(colourSchemeName, alignment);
      af.changeColour(cs);
      af.getViewport().setFeaturesDisplayed(jf.getDisplayedFeatures());

      formatAdapter = new AppletFormatAdapter(af.alignPanel,
              exportSettings);
      String jsonOutput = formatAdapter.formatSequences(FileFormat.Json,
              af.alignPanel.getAlignment(), false);

      formatAdapter = new AppletFormatAdapter();
      testAlignment = formatAdapter.readFile(jsonOutput,
              DataSourceType.PASTE, FileFormat.Json);
      testJsonFile = (JSONFile) formatAdapter.getAlignFile();
      System.out.println(jsonOutput);
    } catch (IOException e)
    {
      e.printStackTrace();
    }

  }

  @BeforeMethod(alwaysRun = true)
  public void methodSetup()
  {
    passedCount = 0;
  }

  @AfterTest(alwaysRun = true)
  public void tearDown() throws Exception
  {
    testJsonFile = null;
    alignment = null;
    expectedSeqs = null;
    expectedAnnots = null;
    expectedGrps = null;
    testAlignment = null;
    jf = null;
  }

  @Test(groups = { "Functional" })
  public void roundTripTest()
  {
    assertNotNull("JSON roundtrip test failed!", testJsonFile);
  }

  @Test(groups = { "Functional" })
  public void testSeqParsed()
  {
    assertNotNull("Couldn't read supplied alignment data.", testAlignment);
    Assert.assertNotNull(testAlignment.getSequences());
    for (SequenceI seq : testAlignment.getSequences())
    {
      SequenceI expectedSeq = expectedSeqs.get(seq.getName());
      AssertJUnit.assertTrue(
              "Failed Sequence Test  for >>> " + seq.getName(),
              isSeqMatched(expectedSeq, seq));
      passedCount++;
    }
    AssertJUnit.assertEquals("Some Sequences did not pass the test",
            TEST_SEQ_HEIGHT, passedCount);
  }

  @Test(groups = { "Functional" })
  public void hiddenColsTest()
  {
    HiddenColumns cs = testJsonFile.getHiddenColumns();
    Assert.assertNotNull(cs);

    Iterator<int[]> it = cs.iterator();
    Iterator<int[]> colselit = expectedColSel.iterator();
    Assert.assertTrue(it.hasNext());
    Assert.assertEquals(cs.getNumberOfRegions(), TEST_CS_HEIGHT);
    Assert.assertEquals(it.next(), colselit.next(),
            "Mismatched hidden columns!");
  }

  @Test(groups = { "Functional" })
  public void hiddenSeqsTest()
  {
    Assert.assertNotNull(testJsonFile.getHiddenSequences(),
            "Hidden sequence Expected but found Null");
    Assert.assertEquals(jf.getHiddenSequences().length, 1,
            "Hidden sequence");
  }

  @Test(groups = { "Functional" })
  public void colorSchemeTest()
  {
    Assert.assertNotNull(testJsonFile.getGlobalColourScheme(),
            "Colourscheme is null, parsing failed!");
    Assert.assertEquals(testJsonFile.getGlobalColourScheme(), "Zappo",
            "Zappo colour scheme expected!");
  }

  /**
   * Test for bug JAL-2489, NPE when exporting BioJSON with global colour
   * scheme, and a group colour scheme, set as 'None'
   */
  @Test(groups = { "Functional" })
  public void testBioJSONRoundTripWithColourSchemeNone()
  {
    AppletFormatAdapter formatAdapter = new AppletFormatAdapter();

    Alignment _alignment;
    try
    {
      // load example BioJSON file
      _alignment = (Alignment) formatAdapter.readFile(TEST_JSON_FILE,
              DataSourceType.FILE, FileFormat.Json);
      JSONFile bioJsonFile = (JSONFile) formatAdapter.getAlignFile();
      AlignFrame alignFrame = new AlignFrame(_alignment,
              bioJsonFile.getHiddenSequences(),
              bioJsonFile.getHiddenColumns(), AlignFrame.DEFAULT_WIDTH,
              AlignFrame.DEFAULT_HEIGHT);

      /*
       * Create a group on the alignment;
       * Change global and group colour scheme to 'None' and perform round trip
       */
      SequenceGroup sg = new SequenceGroup();
      sg.addSequence(_alignment.getSequenceAt(0), false);
      sg.setColourScheme(null);
      ColourSchemeI cs = ColourSchemeMapper
              .getJalviewColourScheme(ResidueColourScheme.NONE, _alignment);
      alignFrame.changeColour(cs);
      alignFrame.getViewport()
              .setFeaturesDisplayed(bioJsonFile.getDisplayedFeatures());
      formatAdapter = new AppletFormatAdapter(alignFrame.alignPanel,
              exportSettings);
      // export BioJSON string
      String jsonOutput = formatAdapter.formatSequences(FileFormat.Json,
              alignFrame.alignPanel.getAlignment(), false);
      // read back Alignment from BioJSON string
      formatAdapter = new AppletFormatAdapter();
      formatAdapter.readFile(jsonOutput, DataSourceType.PASTE,
              FileFormat.Json);
      // assert 'None' colour scheme is retained after round trip
      JSONFile _bioJsonFile = (JSONFile) formatAdapter.getAlignFile();
      Assert.assertEquals(_bioJsonFile.getGlobalColourScheme(),
              ResidueColourScheme.NONE);
    } catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  @Test(groups = { "Functional" })
  public void isShowSeqFeaturesSet()
  {
    Assert.assertTrue(testJsonFile.isShowSeqFeatures(),
            "Sequence feature isDisplayed setting expected to be true");
  }

  @Test(groups = { "Functional" })
  public void testGrpParsed()
  {
    Assert.assertNotNull(testAlignment.getGroups());
    for (SequenceGroup seqGrp : testAlignment.getGroups())
    {
      SequenceGroup expectedGrp = expectedGrps.get(seqGrp.getName());
      AssertJUnit.assertTrue(
              "Failed SequenceGroup Test for >>> " + seqGrp.getName(),
              isGroupMatched(expectedGrp, seqGrp));
      passedCount++;
    }
    AssertJUnit.assertEquals("Some SequenceGroups did not pass the test",
            TEST_GRP_HEIGHT, passedCount);
  }

  @Test(groups = { "Functional" })
  public void testAnnotationParsed()
  {
    Assert.assertNotNull(testAlignment.getAlignmentAnnotation());
    for (AlignmentAnnotation annot : testAlignment.getAlignmentAnnotation())
    {
      AlignmentAnnotation expectedAnnot = expectedAnnots.get(annot.label);
      AssertJUnit.assertTrue(
              "Failed AlignmentAnnotation Test for >>> " + annot.label,
              isAnnotationMatched(expectedAnnot, annot));
      passedCount++;
    }
    AssertJUnit.assertEquals("Some Sequences did not pass the test",
            TEST_ANOT_HEIGHT, passedCount);
  }

  public boolean isAnnotationMatched(AlignmentAnnotation eAnnot,
          AlignmentAnnotation annot)
  {
    if (!eAnnot.label.equals(annot.label)
            || !eAnnot.description.equals(annot.description)
            || eAnnot.annotations.length != annot.annotations.length)
    {
      return false;
    }

    for (int x = 0; x < annot.annotations.length; x++)
    {
      Annotation y = annot.annotations[x];
      Annotation z = annot.annotations[x];

      if (!y.displayCharacter.equals(z.displayCharacter)
              || y.value != z.value
              || y.secondaryStructure != z.secondaryStructure)
      {
        return false;
      }
    }
    return true;
  }

  boolean isSeqMatched(SequenceI expectedSeq, SequenceI actualSeq)
  {
    System.out.println("Testing >>> " + actualSeq.getName());

    if (expectedSeq.getName().equals(actualSeq.getName())
            && expectedSeq.getSequenceAsString()
                    .equals(actualSeq.getSequenceAsString())
            && expectedSeq.getStart() == actualSeq.getStart()
            && expectedSeq.getEnd() == actualSeq.getEnd()
            && featuresMatched(expectedSeq, actualSeq))
    {
      return true;
    }
    return false;
  }

  public boolean isGroupMatched(SequenceGroup expectedGrp,
          SequenceGroup actualGrp)
  {

    System.out.println("Testing >>> " + actualGrp.getName());
    System.out.println(expectedGrp.getName() + " | " + actualGrp.getName());
    System.out.println(expectedGrp.getColourText() + " | "
            + actualGrp.getColourText());
    System.out.println(expectedGrp.getDisplayBoxes() + " | "
            + actualGrp.getDisplayBoxes());
    System.out.println(expectedGrp.getIgnoreGapsConsensus() + " | "
            + actualGrp.getIgnoreGapsConsensus());
    System.out.println(expectedGrp.getSequences().size() + " | "
            + actualGrp.getSequences().size());
    System.out.println(
            expectedGrp.getStartRes() + " | " + actualGrp.getStartRes());
    System.out.println(
            expectedGrp.getEndRes() + " | " + actualGrp.getEndRes());
    System.out.println(expectedGrp.cs.getColourScheme() + " | "
            + actualGrp.cs.getColourScheme());

    boolean colourSchemeMatches = (expectedGrp.cs.getColourScheme() == null
            && actualGrp.cs.getColourScheme() == null)
            || expectedGrp.cs.getColourScheme().getClass()
                    .equals(actualGrp.cs.getColourScheme().getClass());
    if (expectedGrp.getName().equals(actualGrp.getName())
            && expectedGrp.getColourText() == actualGrp.getColourText()
            && expectedGrp.getDisplayBoxes() == actualGrp.getDisplayBoxes()
            && expectedGrp.getIgnoreGapsConsensus() == actualGrp
                    .getIgnoreGapsConsensus()
            && colourSchemeMatches
            && expectedGrp.getSequences().size() == actualGrp.getSequences()
                    .size()
            && expectedGrp.getStartRes() == actualGrp.getStartRes()
            && expectedGrp.getEndRes() == actualGrp.getEndRes())
    {
      return true;
    }
    return false;
  }

  private boolean featuresMatched(SequenceI seq1, SequenceI seq2)
  {
    try
    {
      if (seq1 == null && seq2 == null)
      {
        return true;
      }

      List<SequenceFeature> inFeature = seq1.getFeatures().getAllFeatures();
      List<SequenceFeature> outFeature = seq2.getFeatures()
              .getAllFeatures();

      if (inFeature.size() != outFeature.size())
      {
        System.err.println("Feature count in: " + inFeature.size()
                + ", out: " + outFeature.size());
        return false;
      }

      SequenceFeatures.sortFeatures(inFeature, true);
      SequenceFeatures.sortFeatures(outFeature, true);
      int i = 0;
      for (SequenceFeature in : inFeature)
      {
        SequenceFeature out = outFeature.get(i);
        /*
        System.out.println(out.getType() + " | " + in.getType());
        System.out.println(out.getBegin() + " | " + in.getBegin());
        System.out.println(out.getEnd() + " | " + in.getEnd());
        */
        if (!in.equals(out))
        {
          System.err.println(
                  "Mismatch of " + in.toString() + " " + out.toString());
          return false;
        }
        /*
                if (in.getBegin() == out.getBegin() && in.getEnd() == out.getEnd()
                        && in.getScore() == out.getScore()
                        && in.getFeatureGroup().equals(out.getFeatureGroup())
                        && in.getType().equals(out.getType())
                        && mapsMatch(in.otherDetails, out.otherDetails))
                {
                }
                else
                {
                  System.err.println("Feature[" + i + "] mismatch, in: "
                          + in.toString() + ", out: "
                          + outFeature.get(i).toString());
                  return false;
                }
                */
        i++;
      }
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    // System.out.println(">>>>>>>>>>>>>> features matched : " + matched);
    return true;
  }

  boolean mapsMatch(Map<String, Object> m1, Map<String, Object> m2)
  {
    if (m1 == null || m2 == null)
    {
      if (m1 != null || m2 != null)
      {
        System.err.println(
                "only one SequenceFeature.otherDetails is not null");
        return false;
      }
      else
      {
        return true;
      }
    }
    if (m1.size() != m2.size())
    {
      System.err.println("otherDetails map different sizes");
      return false;
    }
    for (String key : m1.keySet())
    {
      if (!m2.containsKey(key))
      {
        System.err.println(key + " in only one otherDetails");
        return false;
      }
      if (m1.get(key) == null && m2.get(key) != null
              || m1.get(key) != null && m2.get(key) == null
              || !m1.get(key).equals(m2.get(key)))
      {
        System.err.println(key + " values in otherDetails don't match");
        return false;
      }
    }
    return true;
  }

  /**
   * Test group roundtrip with null (None) group colour scheme
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testGrpParsed_colourNone() throws IOException
  {
    AlignmentI copy = new Alignment(testAlignment);
    SequenceGroup sg = testAlignment.getGroups().get(0);
    SequenceGroup copySg = new SequenceGroup(new ArrayList<SequenceI>(),
            sg.getName(), null, sg.getDisplayBoxes(), sg.getDisplayText(),
            sg.getColourText(), sg.getStartRes(), sg.getEndRes());
    for (SequenceI seq : sg.getSequences())
    {
      int seqIndex = testAlignment.findIndex(seq);
      copySg.addSequence(copy.getSequenceAt(seqIndex), false);
    }
    copy.addGroup(copySg);

    AlignFrame af = new AlignFrame(copy, copy.getWidth(), copy.getHeight());
    AppletFormatAdapter formatAdapter = new AppletFormatAdapter(
            af.alignPanel);
    String jsonOutput = formatAdapter.formatSequences(FileFormat.Json, copy,
            false);
    formatAdapter = new AppletFormatAdapter();
    AlignmentI newAlignment = formatAdapter.readFile(jsonOutput,
            DataSourceType.PASTE, FileFormat.Json);

    Assert.assertNotNull(newAlignment.getGroups());
    for (SequenceGroup seqGrp : newAlignment.getGroups())
    {
      SequenceGroup expectedGrp = copySg;
      AssertJUnit.assertTrue(
              "Failed SequenceGroup Test for >>> " + seqGrp.getName(),
              isGroupMatched(expectedGrp, seqGrp));
      passedCount++;
    }
    AssertJUnit.assertEquals("Some SequenceGroups did not pass the test",
            TEST_GRP_HEIGHT, passedCount);
  }
}
