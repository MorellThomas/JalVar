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
package jalview.ext.rbvi.chimera;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.api.FeatureRenderer;
import jalview.api.structures.JalviewStructureDisplayI;
import jalview.bin.Cache;
import jalview.bin.Jalview;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;
import jalview.gui.Preferences;
import jalview.gui.StructureViewer;
import jalview.gui.StructureViewer.ViewerType;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;
import jalview.structure.StructureCommand;
import jalview.structure.StructureMapping;
import jalview.structure.StructureSelectionManager;
import jalview.ws.sifts.SiftsClient;
import jalview.ws.sifts.SiftsException;
import jalview.ws.sifts.SiftsSettings;

@Test(singleThreaded = true)
public class JalviewChimeraView
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  private JalviewStructureDisplayI chimeraViewer;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass(alwaysRun = true)
  public static void setUpBeforeClass() throws Exception
  {
    Jalview.main(
            new String[]
            { "-noquestionnaire", "-nonews", "-props",
                "test/jalview/ext/rbvi/chimera/testProps.jvprops" });
    Cache.setProperty(Preferences.STRUCTURE_DISPLAY,
            ViewerType.CHIMERA.name());
    Cache.setProperty("SHOW_ANNOTATIONS", "false");
    Cache.setProperty(Preferences.STRUCT_FROM_PDB, "false");
    Cache.setProperty(Preferences.STRUCTURE_DISPLAY,
            ViewerType.CHIMERA.name());
    Cache.setProperty("MAP_WITH_SIFTS", "true");
    // TODO this should not be necessary!
    SiftsSettings.setMapWithSifts(true);
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass(alwaysRun = true)
  public static void tearDownAfterClass() throws Exception
  {
    if (Desktop.instance != null)
      Desktop.instance.closeAll_actionPerformed(null);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDownAfterTest() throws Exception
  {
    SiftsClient.setMockSiftsFile(null);
    if (chimeraViewer != null)
    {
      chimeraViewer.closeViewer(true);
    }
  }

  /**
   * Load 1GAQ and view the first structure for which a PDB id is found. Note no
   * network connection is needed - PDB file is read locally, SIFTS fetch fails
   * so mapping falls back to Needleman-Wunsch - ok for this test.
   */
  // External as local install of Chimera required
  @Test(groups = { "External" })
  public void testSingleSeqViewChimera()
  {

    String inFile = "examples/1gaq.txt";
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(inFile,
            DataSourceType.FILE);
    assertNotNull(af, "Failed to create AlignFrame");
    SequenceI sq = af.getViewport().getAlignment().getSequenceAt(0);
    assertEquals(sq.getName(), "1GAQ|A");
    SequenceI dsq = sq.getDatasetSequence();
    Vector<PDBEntry> pdbIds = dsq.getAllPDBEntries();
    assertEquals(pdbIds.size(), 1);
    PDBEntry pdbEntry = pdbIds.get(0);
    assertEquals(pdbEntry.getId(), "1GAQ");
    StructureViewer structureViewer = new StructureViewer(
            af.getViewport().getStructureSelectionManager());
    chimeraViewer = structureViewer.viewStructures(pdbEntry,
            new SequenceI[]
            { sq }, af.getCurrentView().getAlignPanel());
    JalviewChimeraBinding binding = (JalviewChimeraBinding) chimeraViewer
            .getBinding();

    /*
     * Wait for viewer load thread to complete
     */
    do
    {
      try
      {
        Thread.sleep(500);
      } catch (InterruptedException e)
      {
      }
    } while (!binding.isFinishedInit() || !chimeraViewer.isVisible());

    assertTrue(binding.isViewerRunning(), "Failed to start Chimera");

    assertEquals(chimeraViewer.getBinding().getPdbCount(), 1);
    assertTrue(chimeraViewer.hasViewerActionsMenu());

    // now add another sequence and bind to view
    //
    AlignmentI al = af.getViewport().getAlignment();
    PDBEntry xpdb = al.getSequenceAt(0).getPDBEntry("1GAQ");
    sq = new Sequence("1GAQ",
            al.getSequenceAt(0).getSequence(25, 95).toString());
    al.addSequence(sq);
    structureViewer.viewStructures(new PDBEntry[] { xpdb },
            new SequenceI[]
            { sq }, af.getCurrentView().getAlignPanel());

    /*
     * Wait for viewer load thread to complete
     */
    do
    {
      try
      {
        Thread.sleep(1500);
      } catch (InterruptedException q)
      {
      }
      ;
    } while (!binding.isLoadingFinished());

    // still just one PDB structure shown
    assertEquals(chimeraViewer.getBinding().getPdbCount(), 1);
    // and the viewer action menu should still be visible
    assertTrue(chimeraViewer.hasViewerActionsMenu());

    chimeraViewer.closeViewer(true);
    chimeraViewer = null;
    return;
  }

  /**
   * Test for writing Jalview features as attributes on mapped residues in
   * Chimera. Note this uses local copies of PDB and SIFTS file, no network
   * connection required.
   * 
   * @throws IOException
   * @throws SiftsException
   */
  // External as this requires a local install of Chimera
  @Test(groups = { "External" })
  public void testTransferFeatures() throws IOException, SiftsException
  {
    String inFile = "examples/uniref50.fa";
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(inFile,
            DataSourceType.FILE);
    assertNotNull(af, "Failed to create AlignFrame");
    SequenceI sq = af.getViewport().getAlignment().findName("FER2_ARATH");
    assertNotNull(sq, "Didn't find FER2_ARATH");

    /*
     * need a Uniprot dbref for SIFTS mapping to work!!
     */
    sq.addDBRef(new DBRefEntry("UNIPROT", "0", "P16972", null));

    /*
     * use local test PDB and SIFTS files
     */
    String pdbFilePath = new File("test/jalview/ext/rbvi/chimera/4zho.pdb")
            .getPath();
    PDBEntry pdbEntry = new PDBEntry("4ZHO", null, null, pdbFilePath);
    String siftsFilePath = new File(
            "test/jalview/ext/rbvi/chimera/4zho.xml.gz").getPath();
    SiftsClient.setMockSiftsFile(new File(siftsFilePath));

    StructureViewer structureViewer = new StructureViewer(
            af.getViewport().getStructureSelectionManager());
    chimeraViewer = structureViewer.viewStructures(pdbEntry,
            new SequenceI[]
            { sq }, af.getCurrentView().getAlignPanel());

    JalviewChimeraBinding binding = (JalviewChimeraBinding) chimeraViewer
            .getBinding();
    do
    {
      try
      {
        Thread.sleep(500);
      } catch (InterruptedException e)
      {
      }
    } while (!binding.isFinishedInit());

    assertTrue(binding.isViewerRunning(), "Failed to launch Chimera");

    assertEquals(binding.getPdbCount(), 1);

    /*
     * check mapping is (sequence) 53-145 to (structure) 2-94 A/B
     * (or possibly 52-145 to 1-94 - see JAL-2319)
     */
    StructureSelectionManager ssm = binding.getSsm();
    String pdbFile = binding.getStructureFiles()[0];
    StructureMapping[] mappings = ssm.getMapping(pdbFile);
    assertTrue(mappings[0].getMappingDetailsOutput().contains("SIFTS"),
            "Failed to perform SIFTS mapping");
    assertEquals(mappings.length, 2);
    assertEquals(mappings[0].getChain(), "A");
    assertEquals(mappings[0].getPDBResNum(53), 2);
    assertEquals(mappings[0].getPDBResNum(145), 94);
    assertEquals(mappings[1].getChain(), "B");
    assertEquals(mappings[1].getPDBResNum(53), 2);
    assertEquals(mappings[1].getPDBResNum(145), 94);

    /*
     * now add some features to FER2_ARATH 
     */
    // feature on a sequence region not mapped to structure:
    sq.addSequenceFeature(new SequenceFeature("transit peptide",
            "chloroplast", 1, 51, Float.NaN, null));
    // feature on a region mapped to structure:
    sq.addSequenceFeature(new SequenceFeature("domain",
            "2Fe-2S ferredoxin-type", 55, 145, Float.NaN, null));
    // on sparse positions of the sequence
    sq.addSequenceFeature(new SequenceFeature("metal ion-binding site",
            "Iron-Sulfur (2Fe-2S)", 91, 91, Float.NaN, null));
    sq.addSequenceFeature(new SequenceFeature("metal ion-binding site",
            "Iron-Sulfur (2Fe-2S)", 96, 96, Float.NaN, null));
    // on a sequence region that is partially mapped to structure:
    sq.addSequenceFeature(
            new SequenceFeature("helix", null, 50, 60, Float.NaN, null));
    // and again:
    sq.addSequenceFeature(
            new SequenceFeature("chain", null, 50, 70, Float.NaN, null));
    // add numeric valued features - score is set as attribute value
    sq.addSequenceFeature(new SequenceFeature("kd", "hydrophobicity", 62,
            62, -2.1f, null));
    sq.addSequenceFeature(new SequenceFeature("kd", "hydrophobicity", 65,
            65, 3.6f, null));
    sq.addSequenceFeature(new SequenceFeature("RESNUM", "ALA:   2  4zhoA",
            53, 53, Float.NaN, null));

    /*
     * set all features visible except for chain
     */
    af.setShowSeqFeatures(true);
    FeatureRenderer fr = af.getFeatureRenderer();
    fr.setVisible("transit peptide");
    fr.setVisible("domain");
    fr.setVisible("metal ion-binding site");
    fr.setVisible("helix");
    fr.setVisible("kd");
    fr.setVisible("RESNUM");

    /*
     * 'perform' menu action to copy visible features to
     * attributes in Chimera
     */
    // TODO rename and pull up method to binding interface
    // once functionality is added for Jmol as well
    binding.sendFeaturesToViewer(af.getViewport().getAlignPanel());

    /*
     * give Chimera time to open the commands file and execute it
     */
    try
    {
      Thread.sleep(1000);
    } catch (InterruptedException e)
    {
    }

    /*
     * ask Chimera for its residue attribute names
     */
    List<String> reply = binding
            .executeCommand(new StructureCommand("list resattr"), true);
    // prefixed and sanitised attribute names for Jalview features:
    assertTrue(reply.contains("resattr jv_domain"));
    assertTrue(reply.contains("resattr jv_metal_ion_binding_site"));
    assertTrue(reply.contains("resattr jv_helix"));
    assertTrue(reply.contains("resattr jv_kd"));
    assertTrue(reply.contains("resattr jv_RESNUM"));
    // feature is not on a mapped region - no attribute created
    assertFalse(reply.contains("resattr jv_transit_peptide"));
    // feature is not visible - no attribute created
    assertFalse(reply.contains("resattr jv_chain"));

    /*
     * ask Chimera for residues with an attribute
     * 91 and 96 on sequence --> residues 40 and 45 on chains A and B
     */
    reply = binding.executeCommand(
            new StructureCommand("list resi att jv_metal_ion_binding_site"),
            true);
    assertEquals(reply.size(), 4);
    assertTrue(reply.contains(
            "residue id #0:40.A jv_metal_ion_binding_site \"Iron-Sulfur (2Fe-2S)\" index 40"));
    assertTrue(reply.contains(
            "residue id #0:45.A jv_metal_ion_binding_site \"Iron-Sulfur (2Fe-2S)\" index 45"));
    assertTrue(reply.contains(
            "residue id #0:40.B jv_metal_ion_binding_site \"Iron-Sulfur (2Fe-2S)\" index 40"));
    assertTrue(reply.contains(
            "residue id #0:45.B jv_metal_ion_binding_site \"Iron-Sulfur (2Fe-2S)\" index 45"));

    /*
     * check attributes with score values
     * sequence positions 62 and 65 --> residues 11 and 14 on chains A and B
     */
    reply = binding.executeCommand(
            new StructureCommand("list resi att jv_kd"), true);
    assertEquals(reply.size(), 4);
    assertTrue(reply.contains("residue id #0:11.A jv_kd -2.1 index 11"));
    assertTrue(reply.contains("residue id #0:14.A jv_kd 3.6 index 14"));
    assertTrue(reply.contains("residue id #0:11.B jv_kd -2.1 index 11"));
    assertTrue(reply.contains("residue id #0:14.B jv_kd 3.6 index 14"));

    /*
     * list residues with positive kd score 
     */
    reply = binding.executeCommand(
            new StructureCommand("list resi spec :*/jv_kd>0 attr jv_kd"),
            true);
    assertEquals(reply.size(), 2);
    assertTrue(reply.contains("residue id #0:14.A jv_kd 3.6 index 14"));
    assertTrue(reply.contains("residue id #0:14.B jv_kd 3.6 index 14"));

    SiftsClient.setMockSiftsFile(null);
    chimeraViewer.closeViewer(true);
    chimeraViewer = null;
  }

  /**
   * Test for creating Jalview features from attributes on mapped residues in
   * Chimera. Note this uses local copies of PDB and SIFTS file, no network
   * connection required.
   * 
   * @throws IOException
   * @throws SiftsException
   */
  // External as this requires a local install of Chimera
  @Test(groups = { "External" })
  public void testGetAttributes() throws IOException, SiftsException
  {
    String inFile = "examples/uniref50.fa";
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(inFile,
            DataSourceType.FILE);
    assertNotNull(af, "Failed to create AlignFrame");
    SequenceI fer2Arath = af.getViewport().getAlignment()
            .findName("FER2_ARATH");
    assertNotNull(fer2Arath, "Didn't find FER2_ARATH");

    /*
     * need a Uniprot dbref for SIFTS mapping to work!!
     */
    fer2Arath.addDBRef(new DBRefEntry("UNIPROT", "0", "P16972", null));

    /*
     * use local test PDB and SIFTS files
     */
    String pdbFilePath = new File("test/jalview/ext/rbvi/chimera/4zho.pdb")
            .getPath();
    PDBEntry pdbEntry = new PDBEntry("4ZHO", null, null, pdbFilePath);
    String siftsFilePath = new File(
            "test/jalview/ext/rbvi/chimera/4zho.xml.gz").getPath();
    SiftsClient.setMockSiftsFile(new File(siftsFilePath));

    StructureViewer structureViewer = new StructureViewer(
            af.getViewport().getStructureSelectionManager());
    chimeraViewer = structureViewer.viewStructures(pdbEntry,
            new SequenceI[]
            { fer2Arath }, af.getCurrentView().getAlignPanel());

    JalviewChimeraBinding binding = (JalviewChimeraBinding) chimeraViewer
            .getBinding();
    do
    {
      try
      {
        Thread.sleep(500);
      } catch (InterruptedException e)
      {
      }
    } while (!binding.isFinishedInit());

    assertTrue(binding.isViewerRunning(), "Failed to launch Chimera");

    assertEquals(binding.getPdbCount(), 1);

    /*
     * 'perform' menu action to copy Chimera attributes
     * to features in Jalview
     */
    // TODO rename and pull up method to binding interface
    // once functionality is added for Jmol as well
    binding.copyStructureAttributesToFeatures("isHelix",
            af.getViewport().getAlignPanel());

    /*
     * verify 22 residues have isHelix feature
     * (may merge into ranges in future)
     */
    af.setShowSeqFeatures(true);
    FeatureRenderer fr = af.getFeatureRenderer();
    fr.setVisible("isHelix");
    for (int res = 75; res <= 83; res++)
    {
      checkFeaturesAtRes(fer2Arath, fr, res, "isHelix");
    }
    for (int res = 117; res <= 123; res++)
    {
      checkFeaturesAtRes(fer2Arath, fr, res, "isHelix");
    }
    for (int res = 129; res <= 131; res++)
    {
      checkFeaturesAtRes(fer2Arath, fr, res, "isHelix");
    }
    for (int res = 143; res <= 145; res++)
    {
      checkFeaturesAtRes(fer2Arath, fr, res, "isHelix");
    }

    /*
     * fetch a numeric valued attribute
     */
    binding.copyStructureAttributesToFeatures("phi",
            af.getViewport().getAlignPanel());
    fr.setVisible("phi");
    List<SequenceFeature> fs = fer2Arath.getFeatures().findFeatures(54, 54,
            "phi");
    assertEquals(fs.size(), 2);
    assertTrue(fs.contains(new SequenceFeature("phi", "A", 54, 54,
            -131.0713f, "Chimera")));
    assertTrue(fs.contains(new SequenceFeature("phi", "B", 54, 54,
            -127.39512f, "Chimera")));

    /*
     * tear down - also in AfterMethod
     */
    SiftsClient.setMockSiftsFile(null);
    chimeraViewer.closeViewer(true);
    chimeraViewer = null;
  }

  /**
   * Helper method to verify new feature at a sequence position
   * 
   * @param seq
   * @param fr
   * @param res
   * @param featureType
   */
  protected void checkFeaturesAtRes(SequenceI seq, FeatureRenderer fr,
          int res, String featureType)
  {
    String where = "at position " + res;
    List<SequenceFeature> fs = seq.getFeatures().findFeatures(res, res,
            featureType);

    assertEquals(fs.size(), 1, where);
    SequenceFeature sf = fs.get(0);
    assertEquals(sf.getType(), featureType, where);
    assertEquals(sf.getFeatureGroup(), "Chimera", where);
    assertEquals(sf.getDescription(), "True", where);
    assertEquals(sf.getScore(), Float.NaN, where);
  }
}
