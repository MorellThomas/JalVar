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
package jalview.structure;

import static org.junit.Assert.assertArrayEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jalview.analysis.AlignmentUtils;
import jalview.api.structures.JalviewStructureDisplayI;
import jalview.bin.Cache;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.ext.jmol.JmolCommands;
import jalview.gui.AlignFrame;
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;
import jalview.gui.SequenceRenderer;
import jalview.gui.StructureChooser;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;
import jalview.io.Jalview2xmlBase;
import jalview.io.StructureFile;
import jalview.util.MapList;
import jalview.ws.DBRefFetcher;
import jalview.ws.sifts.SiftsSettings;

@Test(singleThreaded = true)
public class StructureSelectionManagerTest extends Jalview2xmlBase
{

  @Override
  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  private StructureSelectionManager ssm;

  @BeforeMethod(alwaysRun = true)
  public void setUp()
  {
    StructureImportSettings.setShowSeqFeatures(true);
    ssm = new StructureSelectionManager();
  }

  @Test(groups = { "Functional" })
  public void testRegisterMapping()
  {
    AlignedCodonFrame acf1 = new AlignedCodonFrame();
    acf1.addMap(new Sequence("s1", "ttt"), new Sequence("p1", "p"),
            new MapList(new int[]
            { 1, 3 }, new int[] { 1, 1 }, 1, 1));
    AlignedCodonFrame acf2 = new AlignedCodonFrame();
    acf2.addMap(new Sequence("s2", "ttt"), new Sequence("p2", "p"),
            new MapList(new int[]
            { 1, 3 }, new int[] { 1, 1 }, 1, 1));

    ssm.registerMapping(acf1);
    assertEquals(1, ssm.getSequenceMappings().size());
    assertTrue(ssm.getSequenceMappings().contains(acf1));

    ssm.registerMapping(acf2);
    assertEquals(2, ssm.getSequenceMappings().size());
    assertTrue(ssm.getSequenceMappings().contains(acf1));
    assertTrue(ssm.getSequenceMappings().contains(acf2));

    /*
     * Re-adding the first mapping does nothing
     */
    ssm.registerMapping(acf1);
    assertEquals(2, ssm.getSequenceMappings().size());
    assertTrue(ssm.getSequenceMappings().contains(acf1));
    assertTrue(ssm.getSequenceMappings().contains(acf2));
  }

  @Test(groups = { "Functional" })
  public void testRegisterMappings()
  {
    AlignedCodonFrame acf1 = new AlignedCodonFrame();
    acf1.addMap(new Sequence("s1", "ttt"), new Sequence("p1", "p"),
            new MapList(new int[]
            { 1, 3 }, new int[] { 1, 1 }, 1, 1));
    AlignedCodonFrame acf2 = new AlignedCodonFrame();
    acf2.addMap(new Sequence("s2", "ttt"), new Sequence("p2", "p"),
            new MapList(new int[]
            { 1, 3 }, new int[] { 1, 1 }, 1, 1));
    AlignedCodonFrame acf3 = new AlignedCodonFrame();
    acf3.addMap(new Sequence("s3", "ttt"), new Sequence("p3", "p"),
            new MapList(new int[]
            { 1, 3 }, new int[] { 1, 1 }, 1, 1));

    List<AlignedCodonFrame> set1 = new ArrayList<>();
    set1.add(acf1);
    set1.add(acf2);
    List<AlignedCodonFrame> set2 = new ArrayList<>();
    set2.add(acf2);
    set2.add(acf3);

    /*
     * Add both sets twice; each mapping should be added once only
     */
    ssm.registerMappings(set1);
    ssm.registerMappings(set1);
    ssm.registerMappings(set2);
    ssm.registerMappings(set2);

    assertEquals(3, ssm.getSequenceMappings().size());
    assertTrue(ssm.getSequenceMappings().contains(acf1));
    assertTrue(ssm.getSequenceMappings().contains(acf2));
    assertTrue(ssm.getSequenceMappings().contains(acf3));
  }

  /**
   * Verify that RESNUM sequence features are present after creating a PDB
   * mapping
   */
  @Test(groups = { "Functional" })
  public void testSetMapping_seqFeatures()
  {
    SequenceI seq = new Sequence("1GAQ|B",
            "ATYNVKLITPEGEVELQVPDDVYILDQAEEDGIDLPYSCRAGSCSSCAGKVVSGSVDQSDQSYLDDGQIADGWVLTCHAYPTSDVVIETHKEEELTGA");
    StructureSelectionManager sm = new StructureSelectionManager();
    sm.setProcessSecondaryStructure(true);
    sm.setAddTempFacAnnot(true);
    StructureFile pmap = sm.setMapping(true, new SequenceI[] { seq },
            new String[]
            { null }, "examples/1gaq.txt", DataSourceType.FILE, null, null);
    assertTrue(pmap != null);

    assertEquals(3, pmap.getSeqs().size());
    assertEquals("1GAQ|A", pmap.getSeqs().get(0).getName());
    assertEquals("1GAQ|B", pmap.getSeqs().get(1).getName());
    assertEquals("1GAQ|C", pmap.getSeqs().get(2).getName());

    /*
     * Verify a RESNUM sequence feature in the PDBfile sequence
     */
    SequenceFeature sf = pmap.getSeqs().get(0).getSequenceFeatures().get(0);
    assertEquals("RESNUM", sf.getType());
    assertEquals("1gaq", sf.getFeatureGroup());
    assertEquals("GLU:  19  1gaqA", sf.getDescription());

    /*
     * Verify a RESNUM sequence feature in the StructureSelectionManager mapped
     * sequence
     */
    StructureMapping map = sm.getMapping("examples/1gaq.txt")[0];
    sf = map.sequence.getSequenceFeatures().get(0);
    assertEquals("RESNUM", sf.getType());
    assertEquals("1gaq", sf.getFeatureGroup());
    assertEquals("ALA:   1  1gaqB", sf.getDescription());
  }

  /**
   * Verify that RESNUM sequence features are present after creating a PDB
   * mapping from a local file, then that everything stays in the same place
   * when the file is viewed. The corner case is that 4IM2 is a fragment of a
   * PDB file, which still includes the 'ID' field - a bug in Jalview 2.10.3
   * causes features, annotation and positions to be remapped to the wrong place
   * on viewing the structure
   */
  @Test(groups = { "Network" })
  public void testMapping_EqualsFeatures()
  {
    // for some reason 'BeforeMethod' (which should be inherited from
    // Jalview2XmlBase isn't always called)...
    if (Desktop.instance != null)
      Desktop.instance.closeAll_actionPerformed(null);
    try
    {
      Thread.sleep(200);
    } catch (Exception foo)
    {
    }
    ;
    SequenceI seq = new Sequence("4IM2|A",
            "LDFCIRNIEKTVMGEISDIHTKLLRLSSSQGTIE");
    String P4IM2_MISSING = "examples/testdata/4IM2_missing.pdb";
    StructureSelectionManager sm = new StructureSelectionManager();
    sm.setProcessSecondaryStructure(true);
    sm.setAddTempFacAnnot(true);
    StructureFile pmap = sm.setMapping(true, new SequenceI[] { seq },
            new String[]
            { null }, P4IM2_MISSING, DataSourceType.FILE, null, null);
    assertTrue(pmap != null);

    assertEquals(1, pmap.getSeqs().size());
    assertEquals("4IM2|A", pmap.getSeqs().get(0).getName());

    List<int[]> structuremap1 = new ArrayList<>(
            sm.getMapping(P4IM2_MISSING)[0]
                    .getPDBResNumRanges(seq.getStart(), seq.getEnd()));

    /*
     * Verify a RESNUM sequence feature in the PDBfile sequence
     * LEU468 - start+0 
     * VAL479 - start+11
     * MET486 - start+12
     * GLY496 - start+13
     * GLU516 - start+33 (last)
     * 
     * Expect features and mapping to resolve to same residues.
     * Also try creating a view and test again
     *   
     */
    String[] feats = new String[] { "LEU", "468", "VAL", "479", "MET",
        "486", "GLY", "496", "GLU", "516" };
    int[] offset = new int[] { 0, 11, 12, 13, 33 };

    List<String> fdesc = new ArrayList<>();
    for (int f = 0; f < feats.length; f += 2)
    {
      fdesc.add(feats[f] + ": " + feats[f + 1] + "  4im2A");
    }
    SequenceI pdbseq = pmap.getSeqs().get(0);
    verifySeqFeats(pdbseq, offset, fdesc);

    /// Now load as a view

    AlignFrame alf = new FileLoader(false).LoadFileWaitTillLoaded(
            "examples/testdata/4IM2_missing.pdb", DataSourceType.FILE);
    Desktop.addInternalFrame(alf, "examples/testdata/4IM2_missing.pdb", 800,
            400);
    AlignmentI pdbal = alf.getViewport().getAlignment();
    SequenceI pdb_viewseq = pdbal.getSequenceAt(0);
    assertEquals(pdb_viewseq.getSequenceAsString(),
            seq.getSequenceAsString());
    // verify the feature location on the sequence when pdb imported as an
    // alignment
    verifySeqFeats(pdb_viewseq, offset, fdesc);

    JalviewStructureDisplayI viewr = openStructureViaChooser(alf,
            pdb_viewseq, "4IM2");

    // and check all is good with feature location still
    verifySeqFeats(pdb_viewseq, offset, fdesc);

    // finally check positional mapping for sequence and structure
    PDBEntry pdbe = seq.getPDBEntry("4IM2");
    StructureSelectionManager apssm = alf.alignPanel
            .getStructureSelectionManager();
    StructureMapping[] smap = apssm.getMapping(pdbe.getFile());
    assertNotNull(smap);
    assertNotNull(smap[0]);
    // find the last position in the alignment sequence - this is not
    // 'SequenceI.getEnd()' - which gets the last PDBRESNUM rather than
    // SequenceI.getStart() + number of residues in file...
    int realSeqEnd = pdb_viewseq.findPosition(pdb_viewseq.getLength());
    List<int[]> ranges = smap[0].getPDBResNumRanges(pdb_viewseq.getStart(),
            realSeqEnd);
    assertEquals(structuremap1.size(), ranges.size());
    int tot_mapped = 0;
    for (int p = 0; p < ranges.size(); p++)
    {
      assertArrayEquals(structuremap1.get(p), ranges.get(p));
      tot_mapped += 1 + (structuremap1.get(p)[1] - structuremap1.get(p)[0]);
    }

    assertEquals(pdb_viewseq.getLength(), tot_mapped);

    int lastmappedp = StructureMapping.UNASSIGNED_VALUE;
    for (int rp = pdb_viewseq.getStart(), rpEnd = pdb_viewseq
            .findPosition(pdb_viewseq.getLength() - 1); rp <= rpEnd; rp++)
    {
      int mappedp = smap[0].getPDBResNum(rp);
      if (mappedp != StructureMapping.UNASSIGNED_VALUE)
      {
        tot_mapped--;
        if (lastmappedp == mappedp)
        {
          Assert.fail("Duplicate mapped position at " + rp + " (dupe = "
                  + mappedp + ")");
        }
      }
    }

    Assert.assertEquals(tot_mapped, 0,
            "Different number of mapped residues compared to ranges of mapped residues");

    // positional mapping to atoms for color by structure is still wrong, even
    // though panel looks correct.

    String[] smcr = new JmolCommands().colourBySequence(apssm,
            new String[]
            { pdbe.getFile() },
            new SequenceI[][]
            { new SequenceI[] { pdb_viewseq } },
            new SequenceRenderer(alf.alignPanel.getAlignViewport()),
            alf.alignPanel);
    // Expected - all residues are white
    for (String c : smcr)
    {
      assertTrue(c.contains("color[255,255,255]"));
      System.out.println(c);
    }
  }

  private void verifySeqFeats(SequenceI pdbseq, int[] offset,
          List<String> fdesc)
  {
    for (int o = 0; o < offset.length; o++)
    {
      int res = pdbseq.findPosition(offset[o]);
      List<SequenceFeature> sf = pdbseq.getFeatures().findFeatures(res, res,
              "RESNUM");
      assertEquals("Expected sequence feature at position " + res + "("
              + offset[o] + ")", 1, sf.size());
      assertEquals("Wrong description at " + res + "(" + offset[o] + ")",
              fdesc.get(o), sf.get(0).getDescription());
    }

  }

  @Test(groups = { "Network" })
  public void testAssociatedMappingToSubSeq() throws Exception
  {

    // currently this test fails if trimming is enabled
    Cache.setProperty(DBRefFetcher.TRIM_RETRIEVED_SEQUENCES,
            Boolean.FALSE.toString());
    String TEMP_FACTOR_AA = "Temperature Factor";
    String PDBID = "4IM2";
    String FullLengthSeq = ">TBK1_HUMAN Serine/threonine-protein kinase TBK1\n"
            + "MQSTSNHLWLLSDILGQGATANVFRGRHKKTGDLFAIKVFNNISFLRPVDVQMREFEVLKKLNHKNIVKLFA\n"
            + "IEEETTTRHKVLIMEFCPCGSLYTVLEEPSNAYGLPESEFLIVLRDVVGGMNHLRENGIVHRDIKPGNIMRV\n"
            + "IGEDGQSVYKLTDFGAARELEDDEQFVSLYGTEEYLHPDMYERAVLRKDHQKKYGATVDLWSIGVTFYHAAT\n"
            + "GSLPFRPFEGPRRNKEVMYKIITGKPSGAISGVQKAENGPIDWSGDMPVSCSLSRGLQVLLTPVLANILEAD\n"
            + "QEKCWGFDQFFAETSDILHRMVIHVFSLQQMTAHKIYIHSYNTATIFHELVYKQTKIISSNQELIYEGRRLV\n"
            + "LEPGRLAQHFPKTTEENPIFVVSREPLNTIGLIYEKISLPKVHPRYDLDGDASMAKAITGVVCYACRIASTL\n"
            + "LLYQELMRKGIRWLIELIKDDYNETVHKKTEVVITLDFCIRNIEKTVKVYEKLMKINLEAAELGEISDIHTK\n"
            + "LLRLSSSQGTIETSLQDIDSRLSPGGSLADAWAHQEGTHPKDRNVEKLQVLLNCMTEIYYQFKKDKAERRLA\n"
            + "YNEEQIHKFDKQKLYYHATKAMTHFTDECVKKYEAFLNKSEEWIRKMLHLRKQLLSLTNQCFDIEEEVSKYQ\n"
            + "EYTNELQETLPQKMFTASSGIKHTMTPIYPSSNTLVEMTLGMKKLKEEMEGVVKELAENNHILERFGSLTMD\n"
            + "GGLRNVDCL";
    /*
     * annotation exported after importing full length sequence to desktop, opening 4IM2 and selecting 'Add Reference Annotation'.
     * 
     * Note - tabs must be replaced with \t - Eclipse expands them to spaces otherwise.
     */
    String FullLengthAnnot = "JALVIEW_ANNOTATION\n"
            + "# Created: Mon Feb 05 15:30:20 GMT 2018\n"
            + "# Updated: Fri Feb 09 17:05:17 GMT 2018\n" + "\n" + "\n"
            + "SEQUENCE_REF\tTBK1_HUMAN\n"
            + "LINE_GRAPH\tTemperature Factor\tTemperature Factor for 4im2A\t125.22|128.51|120.35|113.12|122.6|114.44|91.49|102.53|98.22|111.41|111.32|116.64|103.55|100.53|95.07|105.55|114.76|128.29|133.55|142.14|121.12|110.36|95.79|95.39|87.14|99.56|93.55|94.21|100.33|110.68|97.85|82.37|75.87|76.53|77.85|82.49|80.92|96.88|122.58|133.31|160.15|180.51|||||242.88|258.97|247.01|227.12|223.24|211.62|184.65|183.51|168.96|160.04|150.88|131.68|130.43|139.87|148.59|136.57|125.7|96.51|74.49|74.08|85.87|70.93|86.47|101.59|97.51|97.39|117.19|114.27|129.5|112.98|147.52|170.26|154.98|168.18|157.51|131.95|105.85|97.78|97.35|76.51|76.31|72.55|71.43|78.82|79.94|75.04|79.54|77.95|83.56|88.5|71.51|71.73|75.96|82.36|81.75|66.51|67.23|69.35|67.92|54.75|71.19|61.85|65.34|67.97|64.51|67.41|62.28|72.85|72.76|70.64|65.23|71.07|67.73|87.72|64.93|75.92|94.02|99.35|93.71|103.59|106.29|115.46|118.69|147.18|130.62|171.64|158.95|164.11||107.42|88.53|83.52|88.06|94.06|80.82|59.01|59.73|78.89|69.21|70.34|81.95|74.53|60.92|64.65|55.79|75.71|68.86|70.95|75.08|87.76|85.43|105.84|||||||||||||||||137.46|151.33|145.17|122.79|111.56|126.72|124.06|161.75|176.84|180.51|198.49|196.75|187.41||195.23|202.27|203.16|226.55|221.75|193.83||||||172.33|177.97|151.47|132.65|99.22|93.7|91.15|88.24|72.35|70.05|70.0|74.92|66.51|68.37|65.76|70.12|74.97|76.89|80.83|70.21|69.48|79.54|82.65|96.54|114.31|140.46|168.51|176.99|205.08|209.27|155.83|139.41|151.3|129.33|111.31|119.62|121.37|102.26|115.39|129.97|128.65|110.38|110.66|116.1|82.53|84.02|82.17|87.63|86.42|77.23|91.23|95.53|102.21|120.73|133.26|109.67|108.49|93.25|92.85|86.39|95.66|94.92|85.82|80.13|76.17|86.61|78.9|77.97|105.6|70.66|69.35|78.94|66.68|63.03|69.91|79.05|75.43|70.73|70.02|80.57|81.74|77.99|84.1|91.66|92.42|94.03|116.47|132.01|154.55|163.99|161.37|155.23|132.78|109.3|90.38|101.83|99.61|91.68|82.77|86.12|82.73|90.13|85.14|79.54|74.27|74.06|72.88|86.34|72.0|69.32|60.9|68.15|52.99|63.53|61.3|66.01|68.28|77.41|71.52|67.18|66.17|71.51|65.47|52.63|65.08|66.37|73.76|77.79|67.58|79.53|84.75|87.42|78.9|79.19|85.57|73.67|80.56|86.19|72.17|66.27|72.8|86.28|78.89|74.5|90.6|80.42|92.5|92.84|96.18|92.08|88.5|87.25|64.6|68.95|65.56|67.55|71.62|78.24|84.95|71.35|86.41|84.73|94.41|95.09|84.74|87.64|88.85|75.1|86.42|79.28|73.14|78.54|80.81|60.66|67.93|71.64|59.85|64.7|61.22|63.84|65.9|62.18|74.95|72.92|93.37|90.47|96.0|93.8|88.46|79.78|83.4|66.55|68.7|73.2|78.76|85.67|84.8|89.59|96.52|79.53|103.51|134.72|126.7|145.31|156.17|149.35|128.48|117.29|118.98|131.59|109.36|90.39|87.68|91.81|78.77|80.11|91.39|75.57|78.98|71.53|76.85|70.9|64.71|73.55|73.45|60.0|69.92|57.89|69.07|66.45|62.85|57.83|57.89|66.4|61.61|60.85|66.47|63.53|63.84|65.96|73.06|70.82|64.51|63.66|73.37|73.59|68.09|78.93|76.99|75.05|71.32|88.4|78.88|93.08|110.61|94.32|99.24|128.99|129.49|132.74|124.21|120.32|142.06|166.41|149.87|153.29|172.19|165.89|181.6|223.11|237.73|176.41|171.09|189.65|188.61|154.84|142.72|154.25|170.99|175.65|||||||110.61||||||||||158.07|170.73|167.93|198.47|212.36|181.71|157.69|163.31|138.96|120.29|131.63|152.26|125.06|136.66|148.97|129.68|120.52|135.31|136.05|119.39|124.18|128.94|123.02|103.37|128.44|134.12|118.88|120.94|130.38|124.67|112.21|113.69|123.65|132.06|114.97|110.75|92.38|101.2|103.25|94.84|85.3|82.19|89.81|98.81|83.03|68.91|65.24|70.31|63.49|86.38|71.07|62.65|63.95|66.98|58.06|68.28|62.11|63.86|67.4|68.69|69.57|68.03|74.23|75.66|70.67|81.08|81.31|82.49|88.15|95.99|92.97|100.01|113.18|122.37|110.99|122.19|159.27|147.74|133.96|111.2|115.64|126.55|107.15|102.85|117.06|116.56|109.55|96.82|98.92|96.53|86.0|88.11|92.76|85.77|79.41|93.06|86.96|76.35|72.37|74.19|68.6|67.46|74.47|76.25|66.73|73.18|75.2|88.21|84.93|75.04|71.09|82.6|80.03|76.22|75.76|83.72|75.85|79.36|90.35|86.9|78.24|95.64|97.38|86.41|85.02|91.87|87.36|77.56|81.25|91.66|83.65|77.67|85.07|89.21|92.66|92.46|89.0|100.83|96.71|94.81|101.37|111.28|124.48|119.73|127.81|134.41|132.4|140.32|140.86|166.52|160.16|168.39|176.74|174.63|172.86|168.55|155.9|132.71|113.44|113.49|123.9|151.11|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||\n"
            + "\n" + "";
    AlignFrame alf_full = new FileLoader(false)
            .LoadFileWaitTillLoaded(FullLengthSeq, DataSourceType.PASTE);
    alf_full.loadJalviewDataFile(FullLengthAnnot, DataSourceType.PASTE,
            null, null);
    AlignmentI al_full = alf_full.getViewport().getAlignment();
    AlignmentAnnotation fullseq_tf = al_full
            .findAnnotations(al_full.getSequences().get(0), null,
                    TEMP_FACTOR_AA)
            .iterator().next();
    assertNotNull(fullseq_tf);

    // getMappingFor
    // AlignmentI al_full=alf_full.getViewport().getAlignment();
    //
    // // load 4IM2 (full length, SIFTS onto full alingnment)
    // SiftsSettings.setMapWithSifts(true);
    // StructureChooser schoose = new StructureChooser(selectedSeqs_full,
    // seq_full,
    // alf_full.getViewport().getAlignPanel());
    // schoose.selectStructure(PDBID);
    // schoose.ok_ActionPerformed();

    AlignFrame alf = new FileLoader(false).LoadFileWaitTillLoaded(
            ">TBK1_HUMAN/470-502 Serine/threonine-protein kinase TBK1\nFCIRNIEKTVKVYEKLMKINLEAAELGEISDIH",
            DataSourceType.PASTE);
    Desktop.addInternalFrame(alf, "Foo", 800, 600);
    ;
    AlignmentI al = alf.getViewport().getAlignment();
    SequenceI seq = al.getSequenceAt(0);
    assertEquals(470, seq.getStart());
    // load 4IM2 (full length, SIFTS)
    SiftsSettings.setMapWithSifts(true);
    StructureImportSettings.setProcessSecondaryStructure(true);
    StructureImportSettings.setVisibleChainAnnotation(true);
    JalviewStructureDisplayI sview = openStructureViaChooser(alf, seq,
            PDBID);

    AlignmentAnnotation subseq_tf = null;
    assertTrue(seq.getDBRefs() != null && seq.getDBRefs().size() > 0);

    if (!al.findAnnotations(seq, null, TEMP_FACTOR_AA).iterator().hasNext())
    {
      // FIXME JAL-2321 - don't see reference annotation on alignment the first
      // time
      // around
      SortedMap<String, String> tipEntries = new TreeMap<>();
      final Map<SequenceI, List<AlignmentAnnotation>> candidates = new LinkedHashMap<>();

      AlignmentUtils.findAddableReferenceAnnotations(al.getSequences(),
              tipEntries, candidates, al);
      AlignmentUtils.addReferenceAnnotations(candidates, al, null);
      // FAILS BECAUSE LABEL HAS PDB ID STRING
      if (!al.findAnnotations(seq, null, TEMP_FACTOR_AA).iterator()
              .hasNext())
      {
        Assert.fail(
                "JAL-2321 or worse has occured. No secondary structure added to alignment.");
      }
    }
    subseq_tf = al.findAnnotations(seq, null, TEMP_FACTOR_AA).iterator()
            .next();
    // verify against annotation after loading 4IM2 to full length TBK1_HUMAN
    // verify location of mapped residues
    // verify location of secondary structure annotation
    // Specific positions: LYS477 (h),THR478 (no helix), ... GLY496(no helix),
    // GLU497 (helix),

    // check there is or is not a tempfactor for each mapped position, and that
    // values are equal for those positions.
    for (int p = seq.getStart(); p <= seq.getEnd(); p++)
    {
      Annotation orig, subseq;
      orig = fullseq_tf.getAnnotationForPosition(p);
      subseq = subseq_tf.getAnnotationForPosition(p);
      if (orig == null)
      {
        Assert.assertNull(subseq,
                "Expected no annotation transferred at position " + p);
      }
      ;
      if (orig != null)
      {
        Assert.assertNotNull(subseq,
                "Expected annotation transfer at position " + p);
        assertEquals(orig.value, subseq.value);
      }
      ;

    }
  }

  private JalviewStructureDisplayI openStructureViaChooser(AlignFrame alf,
          SequenceI seq, String pDBID)
  {

    SequenceI[] selectedSeqs = new SequenceI[] { seq };

    StructureChooser schoose = new StructureChooser(selectedSeqs, seq,
            alf.getViewport().getAlignPanel());

    try
    {
      Thread.sleep(5000);
    } catch (InterruptedException q)
    {
    }
    ;
    Assert.assertTrue(schoose.selectStructure(pDBID),
            "Couldn't select structure via structure chooser: " + pDBID);
    schoose.showStructures(true);
    return schoose.getOpenedStructureViewer();
  }

}
