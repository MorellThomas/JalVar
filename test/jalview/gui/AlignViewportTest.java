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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jalview.bin.Cache;
import jalview.bin.Jalview;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.ContactMatrixI;
import jalview.datamodel.SearchResults;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SeqDistanceContactMatrix;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;
import jalview.schemes.ClustalxColourScheme;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.PIDColourScheme;
import jalview.structure.StructureSelectionManager;
import jalview.util.MapList;
import jalview.viewmodel.ViewportRanges;

public class AlignViewportTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  AlignmentI al;

  AlignViewport testee;

  @BeforeClass(alwaysRun = true)
  public static void setUpBeforeClass() throws Exception
  {
    Jalview.main(
            new String[]
            { "-nonews", "-props", "test/jalview/testProps.jvprops" });

    /*
     * remove any sequence mappings left lying around by other tests
     */
    StructureSelectionManager ssm = StructureSelectionManager
            .getStructureSelectionManager(Desktop.instance);
    ssm.resetAll();
  }

  @BeforeMethod(alwaysRun = true)
  public void setUp()
  {
    SequenceI seq1 = new Sequence("Seq1", "ABC");
    SequenceI seq2 = new Sequence("Seq2", "ABC");
    SequenceI seq3 = new Sequence("Seq3", "ABC");
    SequenceI[] seqs = new SequenceI[] { seq1, seq2, seq3 };
    al = new Alignment(seqs);
    al.setDataset(null);
    testee = new AlignViewport(al);
  }

  /**
   * Test that a mapping is not deregistered when a second view is closed but
   * the first still holds a reference to the mapping
   */
  @Test(groups = { "Functional" })
  public void testDeregisterMapping_onCloseView()
  {
    /*
     * alignment with reference to mappings
     */
    AlignFrame af1 = new FileLoader()
            .LoadFileWaitTillLoaded(">Seq1\nCAGT\n", DataSourceType.PASTE);

    SequenceI s1 = af1.getViewport().getAlignment().getSequenceAt(0);
    AlignedCodonFrame acf1 = new AlignedCodonFrame();
    acf1.addMap(s1, s1,
            new MapList(new int[]
            { 1, 4 }, new int[] { 1, 4 }, 1, 1));
    AlignedCodonFrame acf2 = new AlignedCodonFrame();
    acf2.addMap(s1, s1,
            new MapList(new int[]
            { 1, 4 }, new int[] { 4, 1 }, 1, 1));

    List<AlignedCodonFrame> mappings = new ArrayList<>();
    mappings.add(acf1);
    mappings.add(acf2);
    af1.getViewport().getAlignment().setCodonFrames(mappings);
    af1.newView_actionPerformed(null);

    /*
     * Verify that creating the alignment for the new View has registered the
     * mappings
     */
    StructureSelectionManager ssm = StructureSelectionManager
            .getStructureSelectionManager(Desktop.instance);
    List<AlignedCodonFrame> sequenceMappings = ssm.getSequenceMappings();
    assertEquals(2, sequenceMappings.size());
    assertTrue(sequenceMappings.contains(acf1));
    assertTrue(sequenceMappings.contains(acf2));

    /*
     * Close the second view. Verify that mappings are not removed as the first
     * view still holds a reference to them.
     */
    af1.closeMenuItem_actionPerformed(false);
    assertEquals(2, sequenceMappings.size());
    assertTrue(sequenceMappings.contains(acf1));
    assertTrue(sequenceMappings.contains(acf2));
  }

  /**
   * Test that a mapping is deregistered if no alignment holds a reference to it
   */
  @Test(groups = { "Functional" })
  public void testDeregisterMapping_withNoReference()
  {
    Desktop d = Desktop.instance;
    assertNotNull(d);
    StructureSelectionManager ssm = StructureSelectionManager
            .getStructureSelectionManager(Desktop.instance);
    ssm.resetAll();

    AlignFrame af1 = new FileLoader()
            .LoadFileWaitTillLoaded(">Seq1\nRSVQ\n", DataSourceType.PASTE);
    AlignFrame af2 = new FileLoader()
            .LoadFileWaitTillLoaded(">Seq2\nDGEL\n", DataSourceType.PASTE);
    SequenceI cs1 = new Sequence("cseq1", "CCCGGGTTTAAA");
    SequenceI cs2 = new Sequence("cseq2", "CTTGAGTCTAGA");
    SequenceI s1 = af1.getViewport().getAlignment().getSequenceAt(0);
    SequenceI s2 = af2.getViewport().getAlignment().getSequenceAt(0);
    // need to be distinct
    AlignedCodonFrame acf1 = new AlignedCodonFrame();
    acf1.addMap(cs1, s1,
            new MapList(new int[]
            { 1, 4 }, new int[] { 1, 12 }, 1, 3));
    AlignedCodonFrame acf2 = new AlignedCodonFrame();
    acf2.addMap(cs2, s2,
            new MapList(new int[]
            { 1, 4 }, new int[] { 1, 12 }, 1, 3));
    AlignedCodonFrame acf3 = new AlignedCodonFrame();
    acf3.addMap(cs2, cs2,
            new MapList(new int[]
            { 1, 12 }, new int[] { 1, 12 }, 1, 1));

    List<AlignedCodonFrame> mappings1 = new ArrayList<>();
    mappings1.add(acf1);
    af1.getViewport().getAlignment().setCodonFrames(mappings1);

    List<AlignedCodonFrame> mappings2 = new ArrayList<>();
    mappings2.add(acf2);
    mappings2.add(acf3);
    af2.getViewport().getAlignment().setCodonFrames(mappings2);

    /*
     * AlignFrame1 has mapping acf1, AlignFrame2 has acf2 and acf3
     */

    List<AlignedCodonFrame> ssmMappings = ssm.getSequenceMappings();
    assertEquals(0, ssmMappings.size());
    ssm.registerMapping(acf1);
    assertEquals(1, ssmMappings.size());
    ssm.registerMapping(acf2);
    assertEquals(2, ssmMappings.size());
    ssm.registerMapping(acf3);
    assertEquals(3, ssmMappings.size());

    /*
     * Closing AlignFrame2 should remove its mappings from
     * StructureSelectionManager, since AlignFrame1 has no reference to them
     */
    af2.closeMenuItem_actionPerformed(true);
    assertEquals(1, ssmMappings.size());
    assertTrue(ssmMappings.contains(acf1));
  }

  /**
   * Test that a mapping is not deregistered if another alignment holds a
   * reference to it
   */
  @Test(groups = { "Functional" })
  public void testDeregisterMapping_withReference()
  {
    Desktop d = Desktop.instance;
    assertNotNull(d);
    StructureSelectionManager ssm = StructureSelectionManager
            .getStructureSelectionManager(Desktop.instance);
    ssm.resetAll();

    AlignFrame af1 = new FileLoader()
            .LoadFileWaitTillLoaded(">Seq1\nRSVQ\n", DataSourceType.PASTE);
    AlignFrame af2 = new FileLoader()
            .LoadFileWaitTillLoaded(">Seq2\nDGEL\n", DataSourceType.PASTE);
    SequenceI cs1 = new Sequence("cseq1", "CCCGGGTTTAAA");
    SequenceI cs2 = new Sequence("cseq2", "CTTGAGTCTAGA");
    SequenceI s1 = af1.getViewport().getAlignment().getSequenceAt(0);
    SequenceI s2 = af2.getViewport().getAlignment().getSequenceAt(0);
    // need to be distinct
    AlignedCodonFrame acf1 = new AlignedCodonFrame();
    acf1.addMap(cs1, s1,
            new MapList(new int[]
            { 1, 4 }, new int[] { 1, 12 }, 1, 3));
    AlignedCodonFrame acf2 = new AlignedCodonFrame();
    acf2.addMap(cs2, s2,
            new MapList(new int[]
            { 1, 4 }, new int[] { 1, 12 }, 1, 3));
    AlignedCodonFrame acf3 = new AlignedCodonFrame();
    acf3.addMap(cs2, cs2,
            new MapList(new int[]
            { 1, 12 }, new int[] { 1, 12 }, 1, 1));

    List<AlignedCodonFrame> mappings1 = new ArrayList<>();
    mappings1.add(acf1);
    mappings1.add(acf2);
    af1.getViewport().getAlignment().setCodonFrames(mappings1);

    List<AlignedCodonFrame> mappings2 = new ArrayList<>();
    mappings2.add(acf2);
    mappings2.add(acf3);
    af2.getViewport().getAlignment().setCodonFrames(mappings2);

    /*
     * AlignFrame1 has mappings acf1 and acf2, AlignFrame2 has acf2 and acf3
     */

    List<AlignedCodonFrame> ssmMappings = ssm.getSequenceMappings();
    assertEquals(0, ssmMappings.size());
    ssm.registerMapping(acf1);
    assertEquals(1, ssmMappings.size());
    ssm.registerMapping(acf2);
    assertEquals(2, ssmMappings.size());
    ssm.registerMapping(acf3);
    assertEquals(3, ssmMappings.size());

    /*
     * Closing AlignFrame2 should remove mapping acf3 from
     * StructureSelectionManager, but not acf2, since AlignFrame1 still has a
     * reference to it
     */
    af2.closeMenuItem_actionPerformed(true);
    assertEquals(2, ssmMappings.size());
    assertTrue(ssmMappings.contains(acf1));
    assertTrue(ssmMappings.contains(acf2));
    assertFalse(ssmMappings.contains(acf3));
  }

  /**
   * Test for JAL-1306 - conservation thread should run even when only Quality
   * (and not Conservation) is enabled in Preferences
   */
  @Test(groups = { "Functional" }, timeOut = 2000)
  public void testUpdateConservation_qualityOnly()
  {
    Cache.applicationProperties.setProperty("SHOW_ANNOTATIONS",
            Boolean.TRUE.toString());
    Cache.applicationProperties.setProperty("SHOW_QUALITY",
            Boolean.TRUE.toString());
    Cache.applicationProperties.setProperty("SHOW_CONSERVATION",
            Boolean.FALSE.toString());
    Cache.applicationProperties.setProperty("SHOW_OCCUPANCY",
            Boolean.FALSE.toString());
    Cache.applicationProperties.setProperty("SHOW_IDENTITY",
            Boolean.FALSE.toString());
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            "examples/uniref50.fa", DataSourceType.FILE);

    /*
     * wait for Conservation thread to complete
     */
    AlignViewport viewport = af.getViewport();
    waitForCalculations(viewport);
    AlignmentAnnotation[] anns = viewport.getAlignment()
            .getAlignmentAnnotation();
    assertNotNull("No annotations found", anns);
    assertEquals("More than one annotation found", 1, anns.length);
    assertTrue("Annotation is not Quality",
            anns[0].description.startsWith("Alignment Quality"));
    Annotation[] annotations = anns[0].annotations;
    assertNotNull("Quality annotations are null", annotations);
    assertNotNull("Quality in column 1 is null", annotations[0]);
    assertTrue("No quality value in column 1", annotations[0].value > 10f);
  }

  /**
   * Wait for consensus etc calculation threads to complete
   * 
   * @param viewport
   */
  protected void waitForCalculations(AlignViewport viewport)
  {
    synchronized (this)
    {
      do
      {
        try
        {
          wait(50);
        } catch (InterruptedException e)
        {
        }
      } while (viewport.getCalcManager().isWorking());
    }
  }

  @Test(groups = { "Functional" })
  public void testSetGlobalColourScheme()
  {
    /*
     * test for JAL-2283: don't inadvertently turn on colour by conservation
     */
    Cache.applicationProperties.setProperty("DEFAULT_COLOUR_PROT", "None");
    Cache.applicationProperties.setProperty("SHOW_CONSERVATION",
            Boolean.TRUE.toString());
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            "examples/uniref50.fa", DataSourceType.FILE);
    ColourSchemeI cs = new PIDColourScheme();
    AlignViewport viewport = af.getViewport();
    viewport.setGlobalColourScheme(cs);
    assertFalse(viewport.getResidueShading().conservationApplied());

    /*
     * JAL-3201 groups have their own ColourSchemeI instances
     */
    AlignmentI aln = viewport.getAlignment();
    SequenceGroup sg1 = new SequenceGroup();
    sg1.addSequence(aln.getSequenceAt(0), false);
    sg1.addSequence(aln.getSequenceAt(2), false);
    SequenceGroup sg2 = new SequenceGroup();
    sg2.addSequence(aln.getSequenceAt(1), false);
    sg2.addSequence(aln.getSequenceAt(3), false);
    aln.addGroup(sg1);
    aln.addGroup(sg2);
    viewport.setColourAppliesToAllGroups(true);
    viewport.setGlobalColourScheme(new ClustalxColourScheme());
    ColourSchemeI cs0 = viewport.getGlobalColourScheme();
    ColourSchemeI cs1 = sg1.getColourScheme();
    ColourSchemeI cs2 = sg2.getColourScheme();
    assertTrue(cs0 instanceof ClustalxColourScheme);
    assertTrue(cs1 instanceof ClustalxColourScheme);
    assertTrue(cs2 instanceof ClustalxColourScheme);
    assertNotSame(cs0, cs1);
    assertNotSame(cs0, cs2);
    assertNotSame(cs1, cs2);
  }

  @Test(groups = { "Functional" })
  public void testSetGetHasSearchResults()
  {
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            "examples/uniref50.fa", DataSourceType.FILE);
    SearchResultsI sr = new SearchResults();
    SequenceI s1 = af.getViewport().getAlignment().getSequenceAt(0);

    // create arbitrary range on first sequence
    sr.addResult(s1, s1.getStart() + 10, s1.getStart() + 15);

    // test set
    af.getViewport().setSearchResults(sr);
    // has -> true
    assertTrue(af.getViewport().hasSearchResults());
    // get == original
    assertEquals(sr, af.getViewport().getSearchResults());

    // set(null) results in has -> false

    af.getViewport().setSearchResults(null);
    assertFalse(af.getViewport().hasSearchResults());
  }

  /**
   * Verify that setting the selection group has the side-effect of setting the
   * context on the group, unless it already has one, but does not change
   * whether the group is defined or not.
   */
  @Test(groups = { "Functional" })
  public void testSetSelectionGroup()
  {
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            "examples/uniref50.fa", DataSourceType.FILE);
    AlignViewport av = af.getViewport();
    SequenceGroup sg1 = new SequenceGroup();
    SequenceGroup sg2 = new SequenceGroup();
    SequenceGroup sg3 = new SequenceGroup();

    av.setSelectionGroup(sg1);
    assertSame(sg1.getContext(), av.getAlignment()); // context set
    assertFalse(sg1.isDefined()); // group not defined

    sg2.setContext(sg1, false);
    av.setSelectionGroup(sg2);
    assertFalse(sg2.isDefined()); // unchanged
    assertSame(sg2.getContext(), sg1); // unchanged

    // create a defined group
    sg3.setContext(av.getAlignment(), true);
    av.setSelectionGroup(sg3);
    assertTrue(sg3.isDefined()); // unchanged
  }

  /**
   * Verify that setting/clearing SHOW_OCCUPANCY preference adds or omits
   * occupancy row from viewport
   */
  @Test(groups = { "Functional" })
  public void testShowOrDontShowOccupancy()
  {
    // disable occupancy
    jalview.bin.Cache.setProperty("SHOW_OCCUPANCY",
            Boolean.FALSE.toString());
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            "examples/uniref50.fa", DataSourceType.FILE);
    AlignViewport av = af.getViewport();
    Assert.assertNull(av.getAlignmentGapAnnotation(),
            "Preference did not disable occupancy row.");
    int c = 0;
    for (AlignmentAnnotation aa : av.getAlignment().findAnnotations(null,
            null, "Occupancy"))
    {
      c++;
    }
    Assert.assertEquals(c, 0, "Expected zero occupancy rows.");

    // enable occupancy
    jalview.bin.Cache.setProperty("SHOW_OCCUPANCY",
            Boolean.TRUE.toString());
    af = new FileLoader().LoadFileWaitTillLoaded("examples/uniref50.fa",
            DataSourceType.FILE);
    av = af.getViewport();
    Assert.assertNotNull(av.getAlignmentGapAnnotation(),
            "Preference did not enable occupancy row.");
    c = 0;
    for (AlignmentAnnotation aa : av.getAlignment().findAnnotations(null,
            null, av.getAlignmentGapAnnotation().label))
    {
      c++;
    }
    ;
    Assert.assertEquals(c, 1, "Expected to find one occupancy row.");
  }

  @Test(groups = { "Functional" })
  public void testGetConsensusSeq()
  {
    /*
     * A-C
     * A-C
     * A-D
     * --D
     * consensus expected to be A-C
     */
    String fasta = ">s1\nA-C\n>s2\nA-C\n>s3\nA-D\n>s4\n--D\n";
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(fasta,
            DataSourceType.PASTE);
    AlignViewport testme = af.getViewport();
    waitForCalculations(testme);
    SequenceI cons = testme.getConsensusSeq();
    assertEquals("A-C", cons.getSequenceAsString());
  }

  @Test(groups = { "Functional" })
  public void testHideRevealSequences()
  {
    ViewportRanges ranges = testee.getRanges();
    assertEquals(3, al.getHeight());
    assertEquals(0, ranges.getStartSeq());
    assertEquals(2, ranges.getEndSeq());

    /*
     * hide first sequence
     */
    testee.hideSequence(new SequenceI[] { al.getSequenceAt(0) });
    assertEquals(2, al.getHeight());
    assertEquals(0, ranges.getStartSeq());
    assertEquals(1, ranges.getEndSeq());

    /*
     * reveal hidden sequences above the first
     */
    testee.showSequence(0);
    assertEquals(3, al.getHeight());
    assertEquals(0, ranges.getStartSeq());
    assertEquals(2, ranges.getEndSeq());

    /*
     * hide first and third sequences
     */
    testee.hideSequence(
            new SequenceI[]
            { al.getSequenceAt(0), al.getSequenceAt(2) });
    assertEquals(1, al.getHeight());
    assertEquals(0, ranges.getStartSeq());
    assertEquals(0, ranges.getEndSeq());

    /*
     * reveal all hidden sequences
     */
    testee.showAllHiddenSeqs();
    assertEquals(3, al.getHeight());
    assertEquals(0, ranges.getStartSeq());
    assertEquals(2, ranges.getEndSeq());
  }
  @Test(groups = {"Functional"})
  public void testGetSelectionAsNewSequences_withContactMatrices()
  {
    SequenceI seq = new Sequence("seq","ACADA");
    ContactMatrixI cmat = new SeqDistanceContactMatrix(5);
    seq.addContactList(cmat);
    Alignment al = new Alignment(new SequenceI[] {seq.deriveSequence() });
    al.addAnnotation(al.getSequenceAt(0).getAnnotation()[0]);
    AlignFrame af = new AlignFrame(al,500,500);
    
    SequenceI selseqs[] = af.getViewport().getSelectionAsNewSequence();
    assertNotNull(selseqs[0].getAnnotation());
    assertNotNull(selseqs[0].getAnnotation()[0]);
    assertEquals(cmat,selseqs[0].getContactMatrixFor(selseqs[0].getAnnotation()[0]));
    
    assertNotNull(selseqs[0].getContactListFor(selseqs[0].getAnnotation()[0], 2));
    
    // now select everything and test again
    af.selectAllSequenceMenuItem_actionPerformed(null);
    selseqs = af.getViewport().getSelectionAsNewSequence();
    assertNotNull(selseqs[0].getAnnotation());
    assertNotNull(selseqs[0].getAnnotation()[0]);
    assertEquals(cmat,selseqs[0].getContactMatrixFor(selseqs[0].getAnnotation()[0]));
    assertNotNull(selseqs[0].getContactListFor(selseqs[0].getAnnotation()[0], 2));
  }
}
