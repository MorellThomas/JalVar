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
package jalview.project;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JInternalFrame;

import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.analysis.scoremodels.SimilarityParams;
import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.api.FeatureColourI;
import jalview.api.ViewStyleI;
import jalview.bin.Cache;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.ContactListI;
import jalview.datamodel.ContactMatrix;
import jalview.datamodel.ContactMatrixI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.GeneLocus;
import jalview.datamodel.GroupSet;
import jalview.datamodel.HiddenSequences;
import jalview.datamodel.Mapping;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.PDBEntry.Type;
import jalview.datamodel.Sequence.DBModList;
import jalview.datamodel.SequenceCollectionI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.datamodel.features.FeatureMatcher;
import jalview.datamodel.features.FeatureMatcherSet;
import jalview.datamodel.features.FeatureMatcherSetI;
import jalview.gui.AlignFrame;
import jalview.gui.AlignViewport;
import jalview.gui.AlignmentPanel;
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;
import jalview.gui.OverviewPanel;
import jalview.gui.PCAPanel;
import jalview.gui.PopupMenu;
import jalview.gui.Preferences;
import jalview.gui.SliderPanel;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.FileLoader;
import jalview.io.Jalview2xmlBase;
import jalview.renderer.ResidueShaderI;
import jalview.schemes.AnnotationColourGradient;
import jalview.schemes.BuriedColourScheme;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ColourSchemeProperty;
import jalview.schemes.FeatureColour;
import jalview.schemes.JalviewColourScheme;
import jalview.schemes.RNAHelicesColour;
import jalview.schemes.StrandColourScheme;
import jalview.schemes.TCoffeeColourScheme;
import jalview.structure.StructureImportSettings;
import jalview.util.MapList;
import jalview.util.matcher.Condition;
import jalview.viewmodel.AlignmentViewport;
import jalview.viewmodel.seqfeatures.FeatureRendererModel;
import jalview.ws.datamodel.MappableContactMatrixI;
import jalview.ws.datamodel.alphafold.PAEContactMatrix;

@Test(singleThreaded = true)
public class Jalview2xmlTests extends Jalview2xmlBase
{

  @Override
  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    if (Desktop.instance != null)
      Desktop.instance.closeAll_actionPerformed(null);
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testRNAStructureRecovery() throws Exception
  {
    String inFile = "examples/RF00031_folded.stk";
    String tfile = File.createTempFile("JalviewTest", ".jvp")
            .getAbsolutePath();
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(inFile,
            DataSourceType.FILE);
    assertNotNull(af, "Didn't read input file " + inFile);
    int olddsann = countDsAnn(af.getViewport());
    assertTrue(olddsann > 0, "Didn't find any dataset annotations");
    af.changeColour_actionPerformed(
            JalviewColourScheme.RNAHelices.toString());
    assertTrue(
            af.getViewport()
                    .getGlobalColourScheme() instanceof RNAHelicesColour,
            "Couldn't apply RNA helices colourscheme");
    af.saveAlignment(tfile, FileFormat.Jalview);
    assertTrue(af.isSaveAlignmentSuccessful(),
            "Failed to store as a project.");
    af.closeMenuItem_actionPerformed(true);
    af = null;
    af = new FileLoader().LoadFileWaitTillLoaded(tfile,
            DataSourceType.FILE);
    assertNotNull(af, "Failed to import new project");
    int newdsann = countDsAnn(af.getViewport());
    assertEquals(olddsann, newdsann,
            "Differing numbers of dataset sequence annotation\nOriginally "
                    + olddsann + " and now " + newdsann);
    System.out.println(
            "Read in same number of annotations as originally present ("
                    + olddsann + ")");
    assertTrue(

            af.getViewport()
                    .getGlobalColourScheme() instanceof RNAHelicesColour,
            "RNA helices colourscheme was not applied on import.");
  }

  @Test(groups = { "Functional" })
  public void testTCoffeeScores() throws Exception
  {
    String inFile = "examples/uniref50.fa",
            inAnnot = "examples/uniref50.score_ascii";
    String tfile = File.createTempFile("JalviewTest", ".jvp")
            .getAbsolutePath();
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(inFile,
            DataSourceType.FILE);
    assertNotNull(af, "Didn't read input file " + inFile);
    af.loadJalviewDataFile(inAnnot, DataSourceType.FILE, null, null);
    AlignViewport viewport = af.getViewport();
    assertSame(viewport.getGlobalColourScheme().getClass(),
            TCoffeeColourScheme.class, "Didn't set T-coffee colourscheme");
    assertNotNull(
            ColourSchemeProperty.getColourScheme(viewport,
                    viewport.getAlignment(),
                    viewport.getGlobalColourScheme().getSchemeName()),
            "Recognise T-Coffee score from string");

    af.saveAlignment(tfile, FileFormat.Jalview);
    assertTrue(af.isSaveAlignmentSuccessful(),
            "Failed to store as a project.");
    af.closeMenuItem_actionPerformed(true);
    af = null;
    af = new FileLoader().LoadFileWaitTillLoaded(tfile,
            DataSourceType.FILE);
    assertNotNull(af, "Failed to import new project");
    assertSame(af.getViewport().getGlobalColourScheme().getClass(),
            TCoffeeColourScheme.class,
            "Didn't set T-coffee colourscheme for imported project.");
    System.out.println(
            "T-Coffee score shading successfully recovered from project.");
  }

  @Test(groups = { "Functional" })
  public void testColourByAnnotScores() throws Exception
  {
    String inFile = "examples/uniref50.fa",
            inAnnot = "examples/testdata/uniref50_iupred.jva";
    String tfile = File.createTempFile("JalviewTest", ".jvp")
            .getAbsolutePath();
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(inFile,
            DataSourceType.FILE);
    assertNotNull(af, "Didn't read input file " + inFile);
    af.loadJalviewDataFile(inAnnot, DataSourceType.FILE, null, null);
    AlignmentAnnotation[] aa = af.getViewport().getAlignment()
            .getSequenceAt(0).getAnnotation("IUPredWS (Short)");
    assertTrue(

            aa != null && aa.length > 0,
            "Didn't find any IUPred annotation to use to shade alignment.");
    AnnotationColourGradient cs = new AnnotationColourGradient(aa[0], null,
            AnnotationColourGradient.ABOVE_THRESHOLD);
    AnnotationColourGradient gcs = new AnnotationColourGradient(aa[0], null,
            AnnotationColourGradient.BELOW_THRESHOLD);
    cs.setSeqAssociated(true);
    gcs.setSeqAssociated(true);
    af.changeColour(cs);
    SequenceGroup sg = new SequenceGroup();
    sg.setStartRes(57);
    sg.setEndRes(92);
    sg.cs.setColourScheme(gcs);
    af.getViewport().getAlignment().addGroup(sg);
    sg.addSequence(af.getViewport().getAlignment().getSequenceAt(1), false);
    sg.addSequence(af.getViewport().getAlignment().getSequenceAt(2), true);
    af.alignPanel.alignmentChanged();
    af.saveAlignment(tfile, FileFormat.Jalview);
    assertTrue(af.isSaveAlignmentSuccessful(),
            "Failed to store as a project.");
    af.closeMenuItem_actionPerformed(true);
    af = null;
    af = new FileLoader().LoadFileWaitTillLoaded(tfile,
            DataSourceType.FILE);
    assertNotNull(af, "Failed to import new project");

    // check for group and alignment colourschemes

    ColourSchemeI _rcs = af.getViewport().getGlobalColourScheme();
    ColourSchemeI _rgcs = af.getViewport().getAlignment().getGroups().get(0)
            .getColourScheme();
    assertNotNull(_rcs, "Didn't recover global colourscheme");
    assertTrue(_rcs instanceof AnnotationColourGradient,
            "Didn't recover annotation colour global scheme");
    AnnotationColourGradient __rcs = (AnnotationColourGradient) _rcs;
    assertTrue(__rcs.isSeqAssociated(),
            "Annotation colourscheme wasn't sequence associated");

    boolean diffseqcols = false, diffgseqcols = false;
    SequenceI[] sqs = af.getViewport().getAlignment().getSequencesArray();
    for (int p = 0, pSize = af.getViewport().getAlignment()
            .getWidth(); p < pSize && (!diffseqcols || !diffgseqcols); p++)
    {
      if (_rcs.findColour(sqs[0].getCharAt(p), p, sqs[0], null, 0f) != _rcs
              .findColour(sqs[5].getCharAt(p), p, sqs[5], null, 0f))
      {
        diffseqcols = true;
      }
    }
    assertTrue(diffseqcols, "Got Different sequence colours");
    System.out.println(
            "Per sequence colourscheme (Background) successfully applied and recovered.");

    assertNotNull(_rgcs, "Didn't recover group colourscheme");
    assertTrue(_rgcs instanceof AnnotationColourGradient,
            "Didn't recover annotation colour group colourscheme");
    __rcs = (AnnotationColourGradient) _rgcs;
    assertTrue(__rcs.isSeqAssociated(),
            "Group Annotation colourscheme wasn't sequence associated");

    for (int p = 0, pSize = af.getViewport().getAlignment()
            .getWidth(); p < pSize && (!diffseqcols || !diffgseqcols); p++)
    {
      if (_rgcs.findColour(sqs[1].getCharAt(p), p, sqs[1], null,
              0f) != _rgcs.findColour(sqs[2].getCharAt(p), p, sqs[2], null,
                      0f))
      {
        diffgseqcols = true;
      }
    }
    assertTrue(diffgseqcols, "Got Different group sequence colours");
    System.out.println(
            "Per sequence (Group) colourscheme successfully applied and recovered.");
  }

  @Test(groups = { "Functional" })
  public void gatherViewsHere() throws Exception
  {
    int origCount = Desktop.getAlignFrames() == null ? 0
            : Desktop.getAlignFrames().length;
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            "examples/exampleFile_2_7.jar", DataSourceType.FILE);
    assertNotNull(af, "Didn't read in the example file correctly.");
    assertTrue(Desktop.getAlignFrames().length == 1 + origCount,
            "Didn't gather the views in the example file.");

  }

  /**
   * Test for JAL-2223 - multiple mappings in View Mapping report
   * 
   * @throws Exception
   */
  @Test(groups = { "Functional" })
  public void noDuplicatePdbMappingsMade() throws Exception
  {
    StructureImportSettings.setProcessSecondaryStructure(true);
    StructureImportSettings.setVisibleChainAnnotation(true);
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            "examples/exampleFile_2_7.jar", DataSourceType.FILE);
    assertNotNull(af, "Didn't read in the example file correctly.");

    // locate Jmol viewer
    // count number of PDB mappings the structure selection manager holds -
    String pdbFile = af.getCurrentView().getStructureSelectionManager()
            .findFileForPDBId("1A70");
    assertEquals(
            af.getCurrentView().getStructureSelectionManager()
                    .getMapping(pdbFile).length,
            2, "Expected only two mappings for 1A70");

  }

  @Test(groups = { "Functional" })
  public void viewRefPdbAnnotation() throws Exception
  {
    StructureImportSettings.setProcessSecondaryStructure(true);
    StructureImportSettings.setVisibleChainAnnotation(true);
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            "examples/exampleFile_2_7.jar", DataSourceType.FILE);
    assertNotNull(af, "Didn't read in the example file correctly.");
    AlignmentViewPanel sps = null;
    for (AlignmentViewPanel ap : af.alignPanel.alignFrame.getAlignPanels())
    {
      if ("Spinach Feredoxin Structure".equals(ap.getViewName()))
      {
        sps = ap;
        break;
      }
    }
    assertNotNull(sps, "Couldn't find the structure view");
    AlignmentAnnotation refan = null;
    for (AlignmentAnnotation ra : sps.getAlignment()
            .getAlignmentAnnotation())
    {
      if (ra.graph != 0)
      {
        refan = ra;
        break;
      }
    }
    assertNotNull(refan, "Annotation secondary structure not found.");
    SequenceI sq = sps.getAlignment().findName("1A70|");
    assertNotNull(sq, "Couldn't find 1a70 null chain");
    // compare the manually added temperature factor annotation
    // to the track automatically transferred from the pdb structure on load
    assertNotNull(sq.getDatasetSequence().getAnnotation(),
            "1a70 has no annotation");
    for (AlignmentAnnotation ala : sq.getDatasetSequence().getAnnotation())
    {
      AlignmentAnnotation alaa;
      sq.addAlignmentAnnotation(alaa = new AlignmentAnnotation(ala));
      alaa.adjustForAlignment();
      if (ala.graph == refan.graph)
      {
        for (int p = 0; p < ala.annotations.length; p++)
        {
          sq.findPosition(p);
          try
          {
            assertTrue((alaa.annotations[p] == null
                    && refan.annotations[p] == null)
                    || alaa.annotations[p].value == refan.annotations[p].value,
                    "Mismatch at alignment position " + p);
          } catch (NullPointerException q)
          {
            Assert.fail("Mismatch of alignment annotations at position " + p
                    + " Ref seq ann: " + refan.annotations[p]
                    + " alignment " + alaa.annotations[p]);
          }
        }
      }
    }

  }

  @Test(groups = { "Functional" })
  public void testCopyViewSettings() throws Exception
  {
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            "examples/exampleFile_2_7.jar", DataSourceType.FILE);
    assertNotNull(af, "Didn't read in the example file correctly.");
    AlignmentViewPanel sps = null, groups = null;
    for (AlignmentViewPanel ap : af.alignPanel.alignFrame.getAlignPanels())
    {
      if ("Spinach Feredoxin Structure".equals(ap.getViewName()))
      {
        sps = ap;
      }
      if (ap.getViewName().contains("MAFFT"))
      {
        groups = ap;
      }
    }
    assertNotNull(sps, "Couldn't find the structure view");
    assertNotNull(groups, "Couldn't find the MAFFT view");

    ViewStyleI structureStyle = sps.getAlignViewport().getViewStyle();
    ViewStyleI groupStyle = groups.getAlignViewport().getViewStyle();
    AssertJUnit.assertFalse(structureStyle.sameStyle(groupStyle));

    groups.getAlignViewport().setViewStyle(structureStyle);
    AssertJUnit.assertFalse(
            groupStyle.sameStyle(groups.getAlignViewport().getViewStyle()));
    Assert.assertTrue(structureStyle
            .sameStyle(groups.getAlignViewport().getViewStyle()));

  }

  /**
   * test store and recovery of expanded views
   * 
   * @throws Exception
   */
  @Test(groups = { "Functional" }, enabled = true)
  public void testStoreAndRecoverExpandedviews() throws Exception
  {
    Desktop.instance.closeAll_actionPerformed(null);

    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            "examples/exampleFile_2_7.jar", DataSourceType.FILE);
    Assert.assertEquals(Desktop.getAlignFrames().length, 1);
    String afid = af.getViewport().getSequenceSetId();

    // check FileLoader returned a reference to the one alignFrame that is
    // actually on the Desktop
    assertSame(af, Desktop.getAlignFrameFor(af.getViewport()),
            "Jalview2XML.loadAlignFrame() didn't return correct AlignFrame reference for multiple view window");

    Desktop.explodeViews(af);

    int oldviews = Desktop.getAlignFrames().length;
    Assert.assertEquals(Desktop.getAlignFrames().length,
            Desktop.getAlignmentPanels(afid).length);
    File tfile = File.createTempFile("testStoreAndRecoverExpanded", ".jvp");
    try
    {
      new Jalview2XML(false).saveState(tfile);
    } catch (Error e)
    {
      Assert.fail("Didn't save the expanded view state", e);
    } catch (Exception e)
    {
      Assert.fail("Didn't save the expanded view state", e);
    }
    Desktop.instance.closeAll_actionPerformed(null);
    if (Desktop.getAlignFrames() != null)
    {
      Assert.assertEquals(Desktop.getAlignFrames().length, 0);
    }
    af = new FileLoader().LoadFileWaitTillLoaded(tfile.getAbsolutePath(),
            DataSourceType.FILE);
    Assert.assertNotNull(af);
    Assert.assertEquals(Desktop.getAlignFrames().length,
            Desktop.getAlignmentPanels(
                    af.getViewport().getSequenceSetId()).length);
    Assert.assertEquals(Desktop
            .getAlignmentPanels(af.getViewport().getSequenceSetId()).length,
            oldviews);
  }

  /**
   * Test save and reload of a project with a different representative sequence
   * in each view.
   * 
   * @throws Exception
   */
  @Test(groups = { "Functional" })
  public void testStoreAndRecoverReferenceSeqSettings() throws Exception
  {
    Desktop.instance.closeAll_actionPerformed(null);
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            "examples/exampleFile_2_7.jar", DataSourceType.FILE);
    assertNotNull(af, "Didn't read in the example file correctly.");
    String afid = af.getViewport().getSequenceSetId();

    // remember reference sequence for each panel
    Map<String, SequenceI> refseqs = new HashMap<>();

    /*
     * mark sequence 2, 3, 4.. in panels 1, 2, 3...
     * as reference sequence for itself and the preceding sequence
     */
    int n = 1;
    for (AlignmentViewPanel ap : Desktop.getAlignmentPanels(afid))
    {
      AlignViewportI av = ap.getAlignViewport();
      AlignmentI alignment = ap.getAlignment();
      int repIndex = n % alignment.getHeight();
      SequenceI rep = alignment.getSequenceAt(repIndex);
      refseqs.put(ap.getViewName(), rep);

      // code from mark/unmark sequence as reference in jalview.gui.PopupMenu
      // todo refactor this to an alignment view controller
      av.setDisplayReferenceSeq(true);
      av.setColourByReferenceSeq(true);
      av.getAlignment().setSeqrep(rep);

      n++;
    }
    File tfile = File.createTempFile("testStoreAndRecoverReferenceSeq",
            ".jvp");
    try
    {
      new Jalview2XML(false).saveState(tfile);
    } catch (Throwable e)
    {
      Assert.fail("Didn't save the expanded view state", e);
    }
    Desktop.instance.closeAll_actionPerformed(null);
    if (Desktop.getAlignFrames() != null)
    {
      Assert.assertEquals(Desktop.getAlignFrames().length, 0);
    }

    af = new FileLoader().LoadFileWaitTillLoaded(tfile.getAbsolutePath(),
            DataSourceType.FILE);
    afid = af.getViewport().getSequenceSetId();

    for (AlignmentViewPanel ap : Desktop.getAlignmentPanels(afid))
    {
      // check representative
      AlignmentI alignment = ap.getAlignment();
      SequenceI rep = alignment.getSeqrep();
      Assert.assertNotNull(rep,
              "Couldn't restore sequence representative from project");
      // can't use a strong equals here, because by definition, the sequence IDs
      // will be different.
      // could set vamsas session save/restore flag to preserve IDs across
      // load/saves.
      Assert.assertEquals(refseqs.get(ap.getViewName()).toString(),
              rep.toString(),
              "Representative wasn't the same when recovered.");
      Assert.assertTrue(ap.getAlignViewport().isDisplayReferenceSeq(),
              "Display reference sequence view setting not set.");
      Assert.assertTrue(ap.getAlignViewport().isColourByReferenceSeq(),
              "Colour By Reference Seq view setting not set.");
    }
  }

  @Test(groups = { "Functional" })
  public void testIsVersionStringLaterThan()
  {
    /*
     * No version / development / test / autobuild is leniently assumed to be
     * compatible
     */
    assertTrue(Jalview2XML.isVersionStringLaterThan(null, null));
    assertTrue(Jalview2XML.isVersionStringLaterThan("2.8.3", null));
    assertTrue(Jalview2XML.isVersionStringLaterThan(null, "2.8.3"));
    assertTrue(Jalview2XML.isVersionStringLaterThan(null,
            "Development Build"));
    assertTrue(Jalview2XML.isVersionStringLaterThan(null,
            "DEVELOPMENT BUILD"));
    assertTrue(Jalview2XML.isVersionStringLaterThan("2.8.3",
            "Development Build"));
    assertTrue(Jalview2XML.isVersionStringLaterThan(null, "Test"));
    assertTrue(Jalview2XML.isVersionStringLaterThan(null, "TEST"));
    assertTrue(Jalview2XML.isVersionStringLaterThan("2.8.3", "Test"));
    assertTrue(
            Jalview2XML.isVersionStringLaterThan(null, "Automated Build"));
    assertTrue(Jalview2XML.isVersionStringLaterThan("2.8.3",
            "Automated Build"));
    assertTrue(Jalview2XML.isVersionStringLaterThan("2.8.3",
            "AUTOMATED BUILD"));

    /*
     * same version returns true i.e. compatible
     */
    assertTrue(Jalview2XML.isVersionStringLaterThan("2.8", "2.8"));
    assertTrue(Jalview2XML.isVersionStringLaterThan("2.8.3", "2.8.3"));
    assertTrue(Jalview2XML.isVersionStringLaterThan("2.8.3b1", "2.8.3b1"));
    assertTrue(Jalview2XML.isVersionStringLaterThan("2.8.3B1", "2.8.3b1"));
    assertTrue(Jalview2XML.isVersionStringLaterThan("2.8.3b1", "2.8.3B1"));

    /*
     * later version returns true
     */
    assertTrue(Jalview2XML.isVersionStringLaterThan("2.8.3", "2.8.4"));
    assertTrue(Jalview2XML.isVersionStringLaterThan("2.8.3", "2.9"));
    assertTrue(Jalview2XML.isVersionStringLaterThan("2.8.3", "2.9.2"));
    assertTrue(Jalview2XML.isVersionStringLaterThan("2.8", "2.8.3"));
    assertTrue(Jalview2XML.isVersionStringLaterThan("2.8.3", "2.8.3b1"));

    /*
     * earlier version returns false
     */
    assertFalse(Jalview2XML.isVersionStringLaterThan("2.8.3", "2.8"));
    assertFalse(Jalview2XML.isVersionStringLaterThan("2.8.4", "2.8.3"));
    assertFalse(Jalview2XML.isVersionStringLaterThan("2.8.3b1", "2.8.3"));
    assertFalse(Jalview2XML.isVersionStringLaterThan("2.8.3", "2.8.2b1"));
    assertFalse(Jalview2XML.isVersionStringLaterThan("2.8.0b2", "2.8.0b1"));
    /*
     * test for patch release versions
     */
    assertFalse(Jalview2XML.isVersionStringLaterThan("2.11.3.0", "2.11.2"));
    assertTrue(Jalview2XML.isVersionStringLaterThan("2.11.3.0", "2.11.4"));
    assertFalse(
            Jalview2XML.isVersionStringLaterThan("2.12.2.0b1", "2.12.2.0"));
    assertFalse(
            Jalview2XML.isVersionStringLaterThan("2.12.2.3", "2.12.2.2"));

  }

  /**
   * Test save and reload of a project with a different sequence group (and
   * representative sequence) in each view.
   * 
   * @throws Exception
   */
  @Test(groups = { "Functional" })
  public void testStoreAndRecoverGroupRepSeqs() throws Exception
  {
    Desktop.instance.closeAll_actionPerformed(null);
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            "examples/uniref50.fa", DataSourceType.FILE);
    assertNotNull(af, "Didn't read in the example file correctly.");
    String afid = af.getViewport().getSequenceSetId();
    // make a second view of the alignment
    af.newView_actionPerformed(null);

    /*
     * remember representative and hidden sequences marked 
     * on each panel
     */
    Map<String, SequenceI> repSeqs = new HashMap<>();
    Map<String, List<String>> hiddenSeqNames = new HashMap<>();

    /*
     * mark sequence 2, 3, 4.. in panels 1, 2, 3...
     * as reference sequence for itself and the preceding sequence
     */
    int n = 1;
    for (AlignmentViewPanel ap : Desktop.getAlignmentPanels(afid))
    {
      AlignViewportI av = ap.getAlignViewport();
      AlignmentI alignment = ap.getAlignment();
      int repIndex = n % alignment.getHeight();
      // ensure at least one preceding sequence i.e. index >= 1
      repIndex = Math.max(repIndex, 1);
      SequenceI repSeq = alignment.getSequenceAt(repIndex);
      repSeqs.put(ap.getViewName(), repSeq);
      List<String> hiddenNames = new ArrayList<>();
      hiddenSeqNames.put(ap.getViewName(), hiddenNames);

      /*
       * have rep sequence represent itself and the one before it
       * this hides the group (except for the rep seq)
       */
      SequenceGroup sg = new SequenceGroup();
      sg.addSequence(repSeq, false);
      SequenceI precedingSeq = alignment.getSequenceAt(repIndex - 1);
      sg.addSequence(precedingSeq, false);
      sg.setSeqrep(repSeq);
      assertTrue(sg.getSequences().contains(repSeq));
      assertTrue(sg.getSequences().contains(precedingSeq));
      av.setSelectionGroup(sg);
      assertSame(repSeq, sg.getSeqrep());

      /*
       * represent group with sequence adds to a map of hidden rep sequences
       * (it does not create a group on the alignment) 
       */
      ((AlignmentViewport) av).hideSequences(repSeq, true);
      assertSame(repSeq, sg.getSeqrep());
      assertTrue(sg.getSequences().contains(repSeq));
      assertTrue(sg.getSequences().contains(precedingSeq));
      assertTrue(alignment.getGroups().isEmpty(), "alignment has groups");
      Map<SequenceI, SequenceCollectionI> hiddenRepSeqsMap = av
              .getHiddenRepSequences();
      assertNotNull(hiddenRepSeqsMap);
      assertEquals(1, hiddenRepSeqsMap.size());
      assertSame(sg, hiddenRepSeqsMap.get(repSeq));
      assertTrue(alignment.getHiddenSequences().isHidden(precedingSeq));
      assertFalse(alignment.getHiddenSequences().isHidden(repSeq));
      hiddenNames.add(precedingSeq.getName());

      n++;
    }
    File tfile = File.createTempFile("testStoreAndRecoverGroupReps",
            ".jvp");
    try
    {
      new Jalview2XML(false).saveState(tfile);
    } catch (Throwable e)
    {
      Assert.fail("Didn't save the expanded view state", e);
    }
    Desktop.instance.closeAll_actionPerformed(null);
    if (Desktop.getAlignFrames() != null)
    {
      Assert.assertEquals(Desktop.getAlignFrames().length, 0);
    }

    af = new FileLoader().LoadFileWaitTillLoaded(tfile.getAbsolutePath(),
            DataSourceType.FILE);
    afid = af.getViewport().getSequenceSetId();

    for (AlignmentViewPanel ap : Desktop.getAlignmentPanels(afid))
    {
      String viewName = ap.getViewName();
      AlignViewportI av = ap.getAlignViewport();
      AlignmentI alignment = ap.getAlignment();
      List<SequenceGroup> groups = alignment.getGroups();
      assertNotNull(groups);
      assertTrue(groups.isEmpty(), "Alignment has groups");
      Map<SequenceI, SequenceCollectionI> hiddenRepSeqsMap = av
              .getHiddenRepSequences();
      assertNotNull(hiddenRepSeqsMap, "No hidden represented sequences");
      assertEquals(1, hiddenRepSeqsMap.size());
      assertEquals(repSeqs.get(viewName).getDisplayId(true),
              hiddenRepSeqsMap.keySet().iterator().next()
                      .getDisplayId(true));

      /*
       * verify hidden sequences in restored panel
       */
      List<String> hidden = hiddenSeqNames.get(ap.getViewName());
      HiddenSequences hs = alignment.getHiddenSequences();
      assertEquals(hidden.size(), hs.getSize(),
              "wrong number of restored hidden sequences in "
                      + ap.getViewName());
    }
  }

  /**
   * Test save and reload of PDBEntry in Jalview project
   * 
   * @throws Exception
   */
  @Test(groups = { "Functional" })
  public void testStoreAndRecoverPDBEntry() throws Exception
  {
    Desktop.instance.closeAll_actionPerformed(null);
    String exampleFile = "examples/3W5V.pdb";
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(exampleFile,
            DataSourceType.FILE);
    assertNotNull(af, "Didn't read in the example file correctly.");
    String afid = af.getViewport().getSequenceSetId();

    AlignmentPanel[] alignPanels = Desktop.getAlignmentPanels(afid);
    System.out.println();
    AlignmentViewPanel ap = alignPanels[0];
    String tfileBase = new File(".").getAbsolutePath().replace(".", "");
    String testFile = tfileBase + exampleFile;
    AlignmentI alignment = ap.getAlignment();
    System.out.println("blah");
    SequenceI[] seqs = alignment.getSequencesArray();
    Assert.assertNotNull(seqs[0]);
    Assert.assertNotNull(seqs[1]);
    Assert.assertNotNull(seqs[2]);
    Assert.assertNotNull(seqs[3]);
    Assert.assertNotNull(seqs[0].getDatasetSequence());
    Assert.assertNotNull(seqs[1].getDatasetSequence());
    Assert.assertNotNull(seqs[2].getDatasetSequence());
    Assert.assertNotNull(seqs[3].getDatasetSequence());
    PDBEntry[] pdbEntries = new PDBEntry[4];
    pdbEntries[0] = new PDBEntry("3W5V", "A", Type.PDB, testFile);
    pdbEntries[1] = new PDBEntry("3W5V", "B", Type.PDB, testFile);
    pdbEntries[2] = new PDBEntry("3W5V", "C", Type.PDB, testFile);
    pdbEntries[3] = new PDBEntry("3W5V", "D", Type.PDB, testFile);
    Assert.assertEquals(
            seqs[0].getDatasetSequence().getAllPDBEntries().get(0),
            pdbEntries[0]);
    Assert.assertEquals(
            seqs[1].getDatasetSequence().getAllPDBEntries().get(0),
            pdbEntries[1]);
    Assert.assertEquals(
            seqs[2].getDatasetSequence().getAllPDBEntries().get(0),
            pdbEntries[2]);
    Assert.assertEquals(
            seqs[3].getDatasetSequence().getAllPDBEntries().get(0),
            pdbEntries[3]);

    File tfile = File.createTempFile("testStoreAndRecoverPDBEntry", ".jvp");
    try
    {
      new Jalview2XML(false).saveState(tfile);
    } catch (Throwable e)
    {
      Assert.fail("Didn't save the state", e);
    }
    Desktop.instance.closeAll_actionPerformed(null);
    if (Desktop.getAlignFrames() != null)
    {
      Assert.assertEquals(Desktop.getAlignFrames().length, 0);
    }

    AlignFrame restoredFrame = new FileLoader().LoadFileWaitTillLoaded(
            tfile.getAbsolutePath(), DataSourceType.FILE);
    String rfid = restoredFrame.getViewport().getSequenceSetId();
    AlignmentPanel[] rAlignPanels = Desktop.getAlignmentPanels(rfid);
    AlignmentViewPanel rap = rAlignPanels[0];
    AlignmentI rAlignment = rap.getAlignment();
    System.out.println("blah");
    SequenceI[] rseqs = rAlignment.getSequencesArray();
    Assert.assertNotNull(rseqs[0]);
    Assert.assertNotNull(rseqs[1]);
    Assert.assertNotNull(rseqs[2]);
    Assert.assertNotNull(rseqs[3]);
    Assert.assertNotNull(rseqs[0].getDatasetSequence());
    Assert.assertNotNull(rseqs[1].getDatasetSequence());
    Assert.assertNotNull(rseqs[2].getDatasetSequence());
    Assert.assertNotNull(rseqs[3].getDatasetSequence());

    // The Asserts below are expected to fail until the PDB chainCode is
    // recoverable from a Jalview projects
    for (int chain = 0; chain < 4; chain++)
    {
      PDBEntry recov = rseqs[chain].getDatasetSequence().getAllPDBEntries()
              .get(0);
      PDBEntry expected = pdbEntries[chain];
      Assert.assertEquals(recov.getId(), expected.getId(),
              "Mismatch PDB ID");
      Assert.assertEquals(recov.getChainCode(), expected.getChainCode(),
              "Mismatch PDB ID");
      Assert.assertEquals(recov.getType(), expected.getType(),
              "Mismatch PDBEntry 'Type'");
      Assert.assertNotNull(recov.getFile(),
              "Recovered PDBEntry should have a non-null file entry");
      Assert.assertEquals(
              recov.getFile().toLowerCase(Locale.ENGLISH)
                      .lastIndexOf("pdb"),
              recov.getFile().length() - 3,
              "Recovered PDBEntry file should have PDB suffix");
    }
  }

  /**
   * Configure an alignment and a sub-group each with distinct colour schemes,
   * Conservation and PID thresholds, and confirm these are restored from the
   * saved project.
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testStoreAndRecoverAnnotationRowElementColours()
          throws IOException
  {
    Desktop.instance.closeAll_actionPerformed(null);
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded("SEQ\tMNQ",
            DataSourceType.PASTE);

    AlignViewport av = af.getViewport();
    AlignmentI al = av.getAlignment();
    SequenceI fsq;
    fsq = al.getSequenceAt(0);
    Annotation annots[] = new Annotation[fsq.getLength()];
    AlignmentAnnotation ala = new AlignmentAnnotation("Colour", "Annots",
            annots);
    annots[0] = new Annotation(1.0f);
    annots[1] = new Annotation(2.0f);
    annots[2] = new Annotation(3.0f);
    annots[0].colour = Color.RED;
    annots[1].colour = Color.GREEN;
    annots[2].colour = Color.BLUE;
    ala.validateRangeAndDisplay();
    al.getSequenceAt(0).addAlignmentAnnotation(ala);
    al.addAnnotation(ala);
    /*
     * and colour by annotation
     */
    AnnotationColourGradient acg = new AnnotationColourGradient(ala,
            af.alignPanel.av.getGlobalColourScheme(), 0);
    acg.setSeqAssociated(true);
    acg.setPredefinedColours(true);
    af.changeColour(acg);
    Color seqcol[] = new Color[3];
    for (int iStart = fsq.findIndex(fsq.getStart()), i = 0; i < 3; i++)
    {
      seqcol[i] = af.alignPanel.getSeqPanel().seqCanvas
              .getSequenceRenderer()
              .getResidueColour(fsq, iStart + i, null);
    }
    /*
     * save project, close windows, reload project, verify
     */
    File tfile = File.createTempFile(
            "testStoreAndRecoverAnnotRowElemColors", ".jvp");
    tfile.deleteOnExit();
    new Jalview2XML(false).saveState(tfile);
    // Desktop.instance.closeAll_actionPerformed(null);
    af = new FileLoader().LoadFileWaitTillLoaded(tfile.getAbsolutePath(),
            DataSourceType.FILE);
    Assert.assertNotNull(af, "Failed to reload project");
    /*
     * verify alignment annotation has colors
     */
    av = af.getViewport();

    ColourSchemeI loadedCscheme = av.getGlobalColourScheme();
    Assert.assertTrue(loadedCscheme instanceof AnnotationColourGradient,
            "Didn't apply Annotation colour gradient");
    acg = (AnnotationColourGradient) loadedCscheme;
    assertTrue(acg.isSeqAssociated());
    assertTrue(acg.isPredefinedColours());

    al = av.getAlignment();
    fsq = al.getSequenceAt(0);
    ala = fsq.getAnnotation()[0];
    Assert.assertNotNull(ala, "No annotation row recovered");
    Assert.assertNotNull(ala.annotations);
    for (int iStart = al.getSequenceAt(0)
            .findIndex(al.getSequenceAt(0).getStart()), i = 0; i < 3; i++)
    {
      Assert.assertTrue(ala.annotations[i].colour != null);
      Assert.assertTrue(ala.annotations[i].colour.equals(annots[i].colour));
      Color newseqcol = af.alignPanel.getSeqPanel().seqCanvas
              .getSequenceRenderer()
              .getResidueColour(fsq, iStart + i, null);
      Assert.assertTrue(seqcol[i].equals(newseqcol),
              "Sequence shading is different");

    }

  }

  /**
   * Configure an alignment and a sub-group each with distinct colour schemes,
   * Conservation and PID thresholds, and confirm these are restored from the
   * saved project.
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testStoreAndRecoverColourThresholds() throws IOException
  {
    Desktop.instance.closeAll_actionPerformed(null);
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            "examples/uniref50.fa", DataSourceType.FILE);

    AlignViewport av = af.getViewport();
    AlignmentI al = av.getAlignment();

    /*
     * Colour alignment by Buried Index, Above 10% PID, By Conservation 20%
     */
    av.setColourAppliesToAllGroups(false);
    af.changeColour_actionPerformed(JalviewColourScheme.Buried.toString());
    assertTrue(av.getGlobalColourScheme() instanceof BuriedColourScheme);
    af.abovePIDThreshold_actionPerformed(true);
    SliderPanel sp = SliderPanel.getSliderPanel();
    assertFalse(sp.isForConservation());
    sp.valueChanged(10);
    af.conservationMenuItem_actionPerformed(true);
    sp = SliderPanel.getSliderPanel();
    assertTrue(sp.isForConservation());
    sp.valueChanged(20);
    ResidueShaderI rs = av.getResidueShading();
    assertEquals(rs.getThreshold(), 10);
    assertTrue(rs.conservationApplied());
    assertEquals(rs.getConservationInc(), 20);

    /*
     * create a group with Strand colouring, 30% Conservation
     * and 40% PID threshold
     * (notice menu action applies to selection group even if mouse click
     * is at a sequence not in the group)
     */
    SequenceGroup sg = new SequenceGroup();
    sg.addSequence(al.getSequenceAt(0), false);
    sg.setStartRes(15);
    sg.setEndRes(25);
    av.setSelectionGroup(sg);
    PopupMenu popupMenu = new PopupMenu(af.alignPanel, al.getSequenceAt(2),
            null);
    popupMenu.changeColour_actionPerformed(
            JalviewColourScheme.Strand.toString());
    assertTrue(sg.getColourScheme() instanceof StrandColourScheme);
    assertEquals(al.getGroups().size(), 1);
    assertSame(al.getGroups().get(0), sg);
    popupMenu.conservationMenuItem_actionPerformed(true);
    sp = SliderPanel.getSliderPanel();
    assertTrue(sp.isForConservation());
    sp.valueChanged(30);
    popupMenu.abovePIDColour_actionPerformed(true);
    sp = SliderPanel.getSliderPanel();
    assertFalse(sp.isForConservation());
    sp.valueChanged(40);
    assertTrue(sg.getGroupColourScheme().conservationApplied());
    assertEquals(sg.getGroupColourScheme().getConservationInc(), 30);
    assertEquals(sg.getGroupColourScheme().getThreshold(), 40);

    /*
     * save project, close windows, reload project, verify
     */
    File tfile = File.createTempFile("testStoreAndRecoverColourThresholds",
            ".jvp");
    tfile.deleteOnExit();
    new Jalview2XML(false).saveState(tfile);
    Desktop.instance.closeAll_actionPerformed(null);
    af = new FileLoader().LoadFileWaitTillLoaded(tfile.getAbsolutePath(),
            DataSourceType.FILE);
    Assert.assertNotNull(af, "Failed to reload project");

    /*
     * verify alignment (background) colouring
     */
    rs = af.getViewport().getResidueShading();
    assertTrue(rs.getColourScheme() instanceof BuriedColourScheme);
    assertEquals(rs.getThreshold(), 10);
    assertTrue(rs.conservationApplied());
    assertEquals(rs.getConservationInc(), 20);

    /*
     * verify group colouring
     */
    assertEquals(1, af.getViewport().getAlignment().getGroups().size(), 1);
    rs = af.getViewport().getAlignment().getGroups().get(0)
            .getGroupColourScheme();
    assertTrue(rs.getColourScheme() instanceof StrandColourScheme);
    assertEquals(rs.getThreshold(), 40);
    assertTrue(rs.conservationApplied());
    assertEquals(rs.getConservationInc(), 30);
  }

  /**
   * Test save and reload of feature colour schemes and filter settings
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testSaveLoadFeatureColoursAndFilters() throws IOException
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
    fr.findAllFeatures(true);

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
     * save as Jalview project
     */
    File tfile = File.createTempFile("JalviewTest", ".jvp");
    tfile.deleteOnExit();
    String filePath = tfile.getAbsolutePath();
    af.saveAlignment(filePath, FileFormat.Jalview);
    assertTrue(af.isSaveAlignmentSuccessful(),
            "Failed to store as a project.");

    /*
     * close current alignment and load the saved project
     */
    af.closeMenuItem_actionPerformed(true);
    af = null;
    af = new FileLoader().LoadFileWaitTillLoaded(filePath,
            DataSourceType.FILE);
    assertNotNull(af, "Failed to import new project");

    /*
     * verify restored feature colour schemes and filters
     */
    fr = af.getFeatureRenderer();
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

  /**
   * pre 2.11 - jalview 2.10 erroneously created new dataset entries for each
   * view (JAL-3171) this test ensures we can import and merge those views
   */
  @Test(groups = { "Functional" })
  public void testMergeDatasetsforViews() throws IOException
  {
    // simple project - two views on one alignment
    AlignFrame af = new FileLoader(false).LoadFileWaitTillLoaded(
            "examples/testdata/projects/twoViews.jvp", DataSourceType.FILE);
    assertNotNull(af);
    assertTrue(af.getAlignPanels().size() > 1);
    verifyDs(af);
  }

  /**
   * pre 2.11 - jalview 2.10 erroneously created new dataset entries for each
   * view (JAL-3171) this test ensures we can import and merge those views This
   * is a more complex project
   */
  @Test(groups = { "Functional" })
  public void testMergeDatasetsforManyViews() throws IOException
  {
    Desktop.instance.closeAll_actionPerformed(null);

    // complex project - one dataset, several views on several alignments
    AlignFrame af = new FileLoader(false).LoadFileWaitTillLoaded(
            "examples/testdata/projects/manyViews.jvp",
            DataSourceType.FILE);
    assertNotNull(af);

    AlignmentI ds = null;
    for (AlignFrame alignFrame : Desktop.getAlignFrames())
    {
      if (ds == null)
      {
        ds = verifyDs(alignFrame);
      }
      else
      {
        // check that this frame's dataset matches the last
        assertTrue(ds == verifyDs(alignFrame));
      }
    }
  }

  private AlignmentI verifyDs(AlignFrame af)
  {
    AlignmentI ds = null;
    for (AlignmentViewPanel ap : af.getAlignPanels())
    {
      if (ds == null)
      {
        ds = ap.getAlignment().getDataset();
      }
      else
      {
        assertTrue(ap.getAlignment().getDataset() == ds,
                "Dataset was not the same for imported 2.10.5 project with several alignment views");
      }
    }
    return ds;
  }

  @Test(groups = "Functional")
  public void testPcaViewAssociation() throws IOException
  {
    Desktop.instance.closeAll_actionPerformed(null);
    final String PCAVIEWNAME = "With PCA";
    // create a new tempfile
    File tempfile = File.createTempFile("jvPCAviewAssoc", "jvp");

    {
      String exampleFile = "examples/uniref50.fa";
      AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(exampleFile,
              DataSourceType.FILE);
      assertNotNull(af, "Didn't read in the example file correctly.");
      AlignmentPanel origView = (AlignmentPanel) af.getAlignPanels().get(0);
      AlignmentPanel newview = af.newView(PCAVIEWNAME, true);
      // create another for good measure
      af.newView("Not the PCA View", true);
      PCAPanel pcaPanel = new PCAPanel(origView, "BLOSUM62",
              new SimilarityParams(true, true, true, false));
      // we're in the test exec thread, so we can just run synchronously here
      pcaPanel.run();

      // now switch the linked view
      pcaPanel.selectAssociatedView(newview);

      assertTrue(pcaPanel.getAlignViewport() == newview.getAlignViewport(),
              "PCA should be associated with 'With PCA' view: test is broken");

      // now save and reload project
      Jalview2XML jv2xml = new jalview.project.Jalview2XML(false);
      tempfile.delete();
      jv2xml.saveState(tempfile);
      assertTrue(jv2xml.errorMessage == null,
              "Failed to save dummy project with PCA: test broken");
    }

    // load again.
    Desktop.instance.closeAll_actionPerformed(null);
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            tempfile.getCanonicalPath(), DataSourceType.FILE);
    JInternalFrame[] frames = Desktop.instance.getAllFrames();
    // PCA and the tabbed alignment view should be the only two windows on the
    // desktop
    assertEquals(frames.length, 2,
            "PCA and the tabbed alignment view should be the only two windows on the desktop");
    PCAPanel pcaPanel = (PCAPanel) frames[frames[0] == af ? 1 : 0];

    AlignmentViewPanel restoredNewView = null;
    for (AlignmentViewPanel alignpanel : Desktop.getAlignmentPanels(null))
    {
      if (alignpanel.getAlignViewport() == pcaPanel.getAlignViewport())
      {
        restoredNewView = alignpanel;
      }
    }
    assertEquals(restoredNewView.getViewName(), PCAVIEWNAME);
    assertTrue(
            restoredNewView.getAlignViewport() == pcaPanel
                    .getAlignViewport(),
            "Didn't restore correct view association for the PCA view");
  }

  /**
   * Test save and reload of DBRefEntry including GeneLocus in project
   * 
   * @throws Exception
   */
  @Test(groups = { "Functional" })
  public void testStoreAndRecoverGeneLocus() throws Exception
  {
    Desktop.instance.closeAll_actionPerformed(null);
    String seqData = ">P30419\nACDE\n>X1235\nGCCTGTGACGAA";
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(seqData,
            DataSourceType.PASTE);
    assertNotNull(af, "Didn't read in the example file correctly.");

    AlignmentViewPanel ap = Desktop.getAlignmentPanels(null)[0];
    SequenceI pep = ap.getAlignment().getSequenceAt(0);
    SequenceI cds = ap.getAlignment().getSequenceAt(1);

    /*
     * give 'protein' a dbref to self, a dbref with map to CDS,
     * and a dbref with map to gene 'locus'
     */
    DBRefEntry dbref1 = new DBRefEntry("Uniprot", "1", "P30419", null);
    pep.addDBRef(dbref1);
    Mapping cdsmap = new Mapping(cds,
            new MapList(new int[]
            { 1, 4 }, new int[] { 1, 12 }, 1, 3));
    DBRefEntry dbref2 = new DBRefEntry("EMBLCDS", "2", "X1235", cdsmap);
    pep.addDBRef(dbref2);
    Mapping locusmap = new Mapping(null,
            new MapList(new int[]
            { 1, 4 }, new int[] { 2674123, 2674135 }, 1, 3));
    DBRefEntry dbref3 = new GeneLocus("human", "GRCh38", "5", locusmap);
    pep.addDBRef(dbref3);

    File tfile = File.createTempFile("testStoreAndRecoverGeneLocus",
            ".jvp");
    try
    {
      new Jalview2XML(false).saveState(tfile);
    } catch (Throwable e)
    {
      Assert.fail("Didn't save the state", e);
    }
    Desktop.instance.closeAll_actionPerformed(null);

    new FileLoader().LoadFileWaitTillLoaded(tfile.getAbsolutePath(),
            DataSourceType.FILE);
    AlignmentViewPanel rap = Desktop.getAlignmentPanels(null)[0];
    SequenceI rpep = rap.getAlignment().getSequenceAt(0);
    DBModList<DBRefEntry> dbrefs = rpep.getDBRefs();
    assertEquals(rpep.getName(), "P30419");
    assertEquals(dbrefs.size(), 3);
    DBRefEntry dbRef = dbrefs.get(0);
    assertFalse(dbRef instanceof GeneLocus);
    assertNull(dbRef.getMap());
    assertEquals(dbRef, dbref1);

    /*
     * restored dbrefs with mapping have a different 'map to'
     * sequence but otherwise match the original dbrefs
     */
    dbRef = dbrefs.get(1);
    assertFalse(dbRef instanceof GeneLocus);
    assertTrue(dbRef.equalRef(dbref2));
    assertNotNull(dbRef.getMap());
    SequenceI rcds = rap.getAlignment().getSequenceAt(1);
    assertSame(dbRef.getMap().getTo(), rcds);
    // compare MapList but not map.to
    assertEquals(dbRef.getMap().getMap(), dbref2.getMap().getMap());

    /*
     * GeneLocus map.to is null so can compare Mapping objects
     */
    dbRef = dbrefs.get(2);
    assertTrue(dbRef instanceof GeneLocus);
    assertEquals(dbRef, dbref3);
  }

  /**
   * test store and recovery of Overview windows
   * 
   * @throws Exception
   */
  @Test(groups = { "Functional" }, enabled = true)
  public void testStoreAndRecoverOverview() throws Exception
  {
    Desktop.instance.closeAll_actionPerformed(null);

    Cache.setProperty("SHOW_OVERVIEW", "false");
    Cache.setProperty(Preferences.USE_LEGACY_GAP, "false");
    Cache.setColourProperty(Preferences.GAP_COLOUR, Color.green);
    Cache.setColourProperty(Preferences.HIDDEN_COLOUR, Color.yellow);
    Cache.setProperty(Preferences.SHOW_OV_HIDDEN_AT_START, "true");

    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            "examples/uniref50.fa", DataSourceType.FILE);

    /*
     * open and resize / reposition overview 
     */
    af.overviewMenuItem_actionPerformed(null);
    OverviewPanel ov1 = af.alignPanel.getOverviewPanel();
    assertNotNull(ov1);
    ov1.setFrameBounds(20, 30, 200, 400);
    assertEquals(ov1.getTitle(), "Overview examples/uniref50.fa");
    assertTrue(ov1.isShowHiddenRegions());

    /*
     * open a New View and its Overview and reposition it
     */
    af.newView_actionPerformed(null);
    af.overviewMenuItem_actionPerformed(null);
    OverviewPanel ov2 = af.alignPanel.getOverviewPanel();
    assertNotNull(ov2);
    assertNotSame(ov1, ov2);
    ov2.setFrameBounds(25, 35, 205, 405);
    assertEquals(ov1.getTitle(), "Overview examples/uniref50.fa Original");
    assertEquals(ov2.getTitle(), "Overview examples/uniref50.fa View 1");

    File tfile = File.createTempFile("testStoreAndRecoverOverview", ".jvp");
    new Jalview2XML(false).saveState(tfile);
    Desktop.instance.closeAll_actionPerformed(null);

    /*
     * change preferences (should _not_ affect reloaded Overviews)
     */
    Cache.setProperty("SHOW_OVERVIEW", "true");
    Cache.setProperty(Preferences.USE_LEGACY_GAP, "true");
    Cache.setColourProperty(Preferences.GAP_COLOUR, Color.blue);
    Cache.setColourProperty(Preferences.HIDDEN_COLOUR, Color.orange);
    Cache.setProperty(Preferences.SHOW_OV_HIDDEN_AT_START, "false");

    af = new FileLoader().LoadFileWaitTillLoaded(tfile.getAbsolutePath(),
            DataSourceType.FILE);

    /*
     * workaround: explicitly select View 1 (not in focus after restore)
     */
    af.tabSelectionChanged(1);

    /*
     * verify restored overview for View 1
     */
    ov2 = af.alignPanel.getOverviewPanel();
    assertEquals(ov2.getCanvas().getGapColour(), Color.green);
    // 'non-legacy' colouring uses white for non-gapped residues
    assertEquals(ov2.getCanvas().getResidueColour(), Color.white);
    assertEquals(ov2.getCanvas().getHiddenColour(), Color.yellow);
    assertEquals(ov2.getTitle(), "Overview examples/uniref50.fa View 1");
    assertEquals(ov2.getFrameBounds(), new Rectangle(25, 35, 205, 405));
    assertTrue(ov2.isShowHiddenRegions());

    /*
     * verify restored overview for Original view
     */
    af.tabSelectionChanged(0);
    ov1 = af.alignPanel.getOverviewPanel();
    assertEquals(ov1.getCanvas().getGapColour(), Color.green);
    // 'non-legacy' colouring uses white for non-gapped residues
    assertEquals(ov1.getCanvas().getResidueColour(), Color.white);
    assertEquals(ov1.getCanvas().getHiddenColour(), Color.yellow);
    assertEquals(ov1.getTitle(), "Overview examples/uniref50.fa Original");
    assertEquals(ov1.getFrameBounds(), new Rectangle(20, 30, 200, 400));
    assertTrue(ov1.isShowHiddenRegions());
  }

  /**
   * Test that a view with no Overview is restored with no Overview, even if
   * 'Open Overview' is selected in Preferences
   * 
   * @throws Exception
   */
  @Test(groups = { "Functional" }, enabled = true)
  public void testStoreAndRecoverNoOverview() throws Exception
  {
    Cache.setProperty("SHOW_OVERVIEW", "false");
    Desktop.instance.closeAll_actionPerformed(null);
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            ">seq1\nMATRSQFLVNF\n", DataSourceType.PASTE);

    File tfile = File.createTempFile("testStoreAndRecoverOverview", ".jvp");
    new Jalview2XML(false).saveState(tfile);
    Desktop.instance.closeAll_actionPerformed(null);

    Cache.setProperty("SHOW_OVERVIEW", "true");
    af = new FileLoader().LoadFileWaitTillLoaded(tfile.getAbsolutePath(),
            DataSourceType.FILE);

    assertNull(af.alignPanel.getOverviewPanel());
  }

  /**
   * Test that a view from an older version of Jalview is restored with Overview
   * automatically shown when the preference is set
   * 
   * @throws Exception
   */
  @Test(groups = { "Functional" }, enabled = true)
  public void testAutoShowOverviewForLegacyProjects() throws Exception
  {
    Desktop.instance.closeAll_actionPerformed(null);
    Cache.setProperty("SHOW_OVERVIEW", "true");
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            "examples/exampleFile.jvp", DataSourceType.FILE);

    Cache.setProperty("SHOW_OVERVIEW", "false");
    assertNotNull(af.alignPanel.getOverviewPanel());
  }

  /**
   * Test that loading example.jvp, doing some stuff, then hitting reload
   * doesn't leave the modified window still open
   * 
   * See JAL-4127 - interactively performing the same actions and reloading
   * works fine, but programmatically they do not
   * 
   * @throws Exception
   */
  @Test(groups = { "Functional" }, enabled = false)
  public void testReloadActuallyReloads() throws Exception
  {
    Desktop.instance.closeAll_actionPerformed(null);
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            "examples/exampleFile.jvp", DataSourceType.FILE);
    af.getViewport().getColumnSelection().addElement(3);
    af.hideSelColumns_actionPerformed(null);
    af.newView("new", true);
    af.reload_actionPerformed(null);
    Thread.sleep(30);
    // af exists still but isn't shown
    assertTrue(af.isClosed());
  }

  @Test(groups = { "Functional" })
  public void testPAEsaveRestore() throws Exception
  {
    Desktop.instance.closeAll_actionPerformed(null);
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            ">seq1\nMATRSQFLVNF\n", DataSourceType.PASTE);
    AlignmentI al = af.getViewport().getAlignment();
    // PAE matrices are added as reference annotation to the dataset sequence
    // at least for now.
    SequenceI sq = al.getSequenceAt(0).getDatasetSequence();
    int i = sq.getLength();
    float[][] paevals = new float[i][i];
    for (i = i - 1; i >= 0; i--)
    {
      for (int j = 0; j <= i; j++)
      {
        paevals[i][j] = ((i - j < 2)
                || ((i > 1 && i < 5) && (j > 1 && i < 5))) ? 1 : 0f;
        paevals[j][i] = -paevals[i][j];
      }
    }
    PAEContactMatrix dummyMat = new PAEContactMatrix(sq, paevals);
    String content = ContactMatrix.contactToFloatString(dummyMat);
    Assert.assertTrue(content.contains("\t1.")); // at least one element must be
                                                 // 1
    float[][] vals = ContactMatrix.fromFloatStringToContacts(content,
            sq.getLength(), sq.getLength());
    assertEquals(vals[3][4], paevals[3][4]);
    assertEquals(vals[4][3], paevals[4][3]);
    dummyMat.setGroupSet(GroupSet.makeGroups(dummyMat, false,0.5f, false));
    Assert.assertNotSame(dummyMat.getNewick(), "");
    AlignmentAnnotation paeCm = sq.addContactList(dummyMat);
    al.addAnnotation(paeCm);
    // verify store/restore of group bitsets
    for (BitSet gp : dummyMat.getGroups())
    {
      StringBuilder sb = new StringBuilder();
      for (long val : gp.toLongArray())
      {
        if (sb.length() > 0)
        {
          sb.append(",");
        }
        sb.append(val);
      }
      String[] longvals = sb.toString().split(",");
      long[] newlongvals = new long[longvals.length];
      for (int lv = 0; lv < longvals.length; lv++)
      {
        try
        {
          newlongvals[lv] = Long.valueOf(longvals[lv]);
        } catch (Exception x)
        {
          Assert.fail("failed to deserialise bitset element ");
        }
      }
      BitSet newGp = BitSet.valueOf(newlongvals);
      assertTrue(gp.equals(newGp));
    }
    File tfile = File.createTempFile("testStoreAndRecoverPAEmatrix",
            ".jvp");
    new Jalview2XML(false).saveState(tfile);
    Desktop.instance.closeAll_actionPerformed(null);

    af = new FileLoader().LoadFileWaitTillLoaded(tfile.getAbsolutePath(),
            DataSourceType.FILE);
    AlignmentI newAl = af.getViewport().getAlignment();
    SequenceI newSeq = newAl.getSequenceAt(0).getDatasetSequence();
    // check annotation of the expected type exists
    Assert.assertEquals(newSeq.getAnnotation().length, 1);
    Assert.assertEquals(newSeq.getAnnotation()[0].graph, paeCm.graph);

    // check a contact matrix was recovered
    Assert.assertEquals(newSeq.getContactMaps().size(), 1);
    // and can be found for the annotation on the sequence
    ContactMatrixI restoredMat = newSeq
            .getContactMatrixFor(newSeq.getAnnotation()[0]);
    Assert.assertNotNull(restoredMat);
    MapList oldMap = ((MappableContactMatrixI) dummyMat).getMapFor(sq);
    MapList newMap = ((MappableContactMatrixI) restoredMat)
            .getMapFor(newSeq);
    Assert.assertEquals(oldMap.getFromRanges(), newMap.getFromRanges());
    Assert.assertEquals(oldMap.getToRanges(), newMap.getToRanges());
    Assert.assertEquals(oldMap.getFromRatio(), newMap.getFromRatio());
    Assert.assertEquals(oldMap.getToRatio(), newMap.getToRatio());
    for (i = sq.getLength() - 1; i >= 0; i--)
    {
      ContactListI oldCM = dummyMat.getContactList(i),
              newCM = restoredMat.getContactList(i);
      for (int j = oldCM.getContactHeight(); j >= 0; j--)
      {
        double old_j = oldCM.getContactAt(j);
        double new_j = newCM.getContactAt(j);
        Assert.assertEquals(old_j, new_j);
      }
    }
    Assert.assertEquals(restoredMat.hasGroups(), dummyMat.hasGroups());
    Assert.assertEquals(restoredMat.getGroups(), dummyMat.getGroups());
    Assert.assertEquals(restoredMat.hasTree(), dummyMat.hasTree());
    Assert.assertEquals(restoredMat.getNewick(), dummyMat.getNewick());
  }

}
