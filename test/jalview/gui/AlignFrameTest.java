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

import static org.junit.Assert.assertNotEquals;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.awt.Color;
import java.util.Iterator;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jalview.api.FeatureColourI;
import jalview.bin.Cache;
import jalview.bin.Jalview;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;
import jalview.project.Jalview2xmlTests;
import jalview.renderer.ResidueShaderI;
import jalview.schemes.BuriedColourScheme;
import jalview.schemes.FeatureColour;
import jalview.schemes.HelixColourScheme;
import jalview.schemes.JalviewColourScheme;
import jalview.schemes.StrandColourScheme;
import jalview.schemes.TurnColourScheme;
import jalview.util.MessageManager;

public class AlignFrameTest
{
  AlignFrame af;

  @BeforeClass(alwaysRun = true)
  public static void setUpBeforeClass() throws Exception
  {
    setUpJvOptionPane();
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
   * configure (read-only) properties for test to ensure Consensus is computed
   * for colour Above PID testing
   */
  @BeforeMethod(alwaysRun = true)
  public void setUp()
  {
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    Cache.applicationProperties.setProperty("SHOW_IDENTITY",
            Boolean.TRUE.toString());
    af = new FileLoader().LoadFileWaitTillLoaded("examples/uniref50.fa",
            DataSourceType.FILE);

    /*
     * wait for Consensus thread to complete
     */
    do
    {
      try
      {
        Thread.sleep(50);
      } catch (InterruptedException x)
      {
      }
    } while (af.getViewport().getCalcManager().isWorking());
  }

  public static void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = "Functional")
  public void testHideFeatureColumns()
  {
    SequenceI seq1 = new Sequence("Seq1", "ABCDEFGHIJ");
    SequenceI seq2 = new Sequence("Seq2", "ABCDEFGHIJ");
    seq1.addSequenceFeature(
            new SequenceFeature("Metal", "", 1, 5, 0f, null));
    seq2.addSequenceFeature(
            new SequenceFeature("Metal", "", 6, 10, 10f, null));
    seq1.addSequenceFeature(
            new SequenceFeature("Turn", "", 2, 4, Float.NaN, null));
    seq2.addSequenceFeature(
            new SequenceFeature("Turn", "", 7, 9, Float.NaN, null));
    AlignmentI al = new Alignment(new SequenceI[] { seq1, seq2 });
    AlignFrame alignFrame = new AlignFrame(al, al.getWidth(),
            al.getHeight());

    /*
     * make all features visible (select feature columns checks visibility)
     */
    alignFrame.getFeatureRenderer().findAllFeatures(true);

    /*
     * hiding a feature not present does nothing
     */
    assertFalse(alignFrame.hideFeatureColumns("exon", true));
    assertTrue(alignFrame.getViewport().getColumnSelection().isEmpty());

    assertEquals(alignFrame.getViewport().getAlignment().getHiddenColumns()
            .getNumberOfRegions(), 0);

    assertFalse(alignFrame.hideFeatureColumns("exon", false));
    assertTrue(alignFrame.getViewport().getColumnSelection().isEmpty());

    assertEquals(alignFrame.getViewport().getAlignment().getHiddenColumns()
            .getNumberOfRegions(), 0);

    /*
     * hiding a feature in all columns does nothing
     */
    assertFalse(alignFrame.hideFeatureColumns("Metal", true));
    assertTrue(alignFrame.getViewport().getColumnSelection().isEmpty());

    assertEquals(alignFrame.getViewport().getAlignment().getHiddenColumns()
            .getNumberOfRegions(), 0);

    /*
     * threshold Metal to hide features where score < 5
     * seq1 feature in columns 1-5 is hidden
     * seq2 feature in columns 6-10 is shown
     */
    FeatureColourI fc = new FeatureColour(null, Color.red, Color.blue, null,
            0f, 10f);
    fc.setAboveThreshold(true);
    fc.setThreshold(5f);
    alignFrame.getFeatureRenderer().setColour("Metal", fc);
    assertTrue(alignFrame.hideFeatureColumns("Metal", true));
    HiddenColumns hidden = alignFrame.getViewport().getAlignment()
            .getHiddenColumns();
    assertEquals(hidden.getNumberOfRegions(), 1);
    Iterator<int[]> regions = hidden.iterator();
    int[] next = regions.next();
    assertEquals(next[0], 5);
    assertEquals(next[1], 9);

    /*
     * hide a feature present in some columns
     * sequence positions [2-4], [7-9] are column positions
     * [1-3], [6-8] base zero
     */
    alignFrame.getViewport().showAllHiddenColumns();
    assertTrue(alignFrame.hideFeatureColumns("Turn", true));
    regions = alignFrame.getViewport().getAlignment().getHiddenColumns()
            .iterator();
    assertEquals(alignFrame.getViewport().getAlignment().getHiddenColumns()
            .getNumberOfRegions(), 2);
    next = regions.next();
    assertEquals(next[0], 1);
    assertEquals(next[1], 3);
    next = regions.next();
    assertEquals(next[0], 6);
    assertEquals(next[1], 8);
  }

  /**
   * Test that changing background (alignment) colour scheme
   * <ul>
   * <li>with Apply Colour to All Groups not selected, does not change group
   * colours</li>
   * <li>with Apply Colour to All Groups selected, does change group
   * colours</li>
   * <li>in neither case, changes alignment or group colour thresholds (PID or
   * Conservation)</li>
   * </ul>
   */
  @Test(groups = "Functional")
  public void testChangeColour_background_groupsAndThresholds()
  {
    AlignViewport av = af.getViewport();
    AlignmentI al = av.getAlignment();

    /*
     * Colour alignment by Buried Index
     */
    af.applyToAllGroups_actionPerformed(false);
    af.changeColour_actionPerformed(JalviewColourScheme.Buried.toString());
    assertTrue(av.getGlobalColourScheme() instanceof BuriedColourScheme);
    assertFalse(av.getResidueShading().conservationApplied());
    assertEquals(av.getResidueShading().getThreshold(), 0);

    /*
     * Apply Conservation 20%
     */
    af.conservationMenuItem_actionPerformed(true);
    SliderPanel sp = SliderPanel.getSliderPanel();
    assertEquals(sp.getTitle(), MessageManager.formatMessage(
            "label.conservation_colour_increment", new String[]
            { "Background" }));
    assertTrue(sp.isForConservation());
    sp.valueChanged(20);
    assertTrue(av.getResidueShading().conservationApplied());
    assertEquals(av.getResidueShading().getConservationInc(), 20);

    /*
     * Apply PID threshold 10% (conservation still applies as well)
     */
    af.abovePIDThreshold_actionPerformed(true);
    sp = SliderPanel.getSliderPanel();
    assertFalse(sp.isForConservation());
    assertEquals(sp.getTitle(), MessageManager.formatMessage(
            "label.percentage_identity_threshold", new String[]
            { "Background" }));
    sp.valueChanged(10);
    assertEquals(av.getResidueShading().getThreshold(), 10);
    assertTrue(av.getResidueShading().conservationApplied());
    assertEquals(av.getResidueShading().getConservationInc(), 20);

    /*
     * create a group with Strand colouring, 30% Conservation
     * and 40% PID threshold
     */
    SequenceGroup sg = new SequenceGroup();
    sg.addSequence(al.getSequenceAt(0), false);
    sg.setStartRes(15);
    sg.setEndRes(25);
    av.setSelectionGroup(sg);

    /*
     * apply 30% Conservation to group
     * (notice menu action applies to selection group even if mouse click
     * is at a sequence not in the group)
     */
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
    assertEquals(sp.getTitle(), MessageManager.formatMessage(
            "label.conservation_colour_increment", new String[]
            { sg.getName() }));
    sp.valueChanged(30);
    assertTrue(sg.getGroupColourScheme().conservationApplied());
    assertEquals(sg.getGroupColourScheme().getConservationInc(), 30);

    /*
     * apply 40% PID threshold to group
     */
    popupMenu.abovePIDColour_actionPerformed(true);
    sp = SliderPanel.getSliderPanel();
    assertFalse(sp.isForConservation());
    assertEquals(sp.getTitle(), MessageManager.formatMessage(
            "label.percentage_identity_threshold", new String[]
            { sg.getName() }));
    sp.valueChanged(40);
    assertEquals(sg.getGroupColourScheme().getThreshold(), 40);
    // conservation threshold is unchanged:
    assertTrue(sg.getGroupColourScheme().conservationApplied());
    assertEquals(sg.getGroupColourScheme().getConservationInc(), 30);

    /*
     * change alignment colour - group colour, and all thresholds,
     * should be unaffected
     */
    af.changeColour_actionPerformed(JalviewColourScheme.Turn.toString());
    assertTrue(av.getGlobalColourScheme() instanceof TurnColourScheme);
    assertTrue(av.getResidueShading().conservationApplied());
    assertEquals(av.getResidueShading().getConservationInc(), 20);
    assertEquals(av.getResidueShading().getThreshold(), 10);
    assertTrue(sg.getColourScheme() instanceof StrandColourScheme);
    assertTrue(sg.getGroupColourScheme().conservationApplied());
    assertEquals(sg.getGroupColourScheme().getConservationInc(), 30);
    assertEquals(sg.getGroupColourScheme().getThreshold(), 40);

    /*
     * Now change alignment colour with Apply Colour To All Groups
     * - group colour should change, but not colour thresholds
     */
    af.applyToAllGroups_actionPerformed(true);
    af.changeColour_actionPerformed(JalviewColourScheme.Helix.toString());
    assertTrue(av.getGlobalColourScheme() instanceof HelixColourScheme);
    assertTrue(av.getResidueShading().conservationApplied());
    assertEquals(av.getResidueShading().getConservationInc(), 20);
    assertEquals(av.getResidueShading().getThreshold(), 10);
    assertTrue(sg.getColourScheme() instanceof HelixColourScheme);
    assertTrue(sg.getGroupColourScheme().conservationApplied());
    assertEquals(sg.getGroupColourScheme().getConservationInc(), 30);
    assertEquals(sg.getGroupColourScheme().getThreshold(), 40);
  }

  /**
   * Test residue colouring with various options
   * <ol>
   * <li>no PID or Conservation threshold</li>
   * <li>colour by Conservation applied</li>
   * <li>colour by Conservation removed</li>
   * <li>colour above PID - various values</li>
   * <li>colour above PID removed</li>
   * <li>Above PID plus By Conservation combined</li>
   * <li>remove Above PID to leave just By Conservation</li>
   * <li>re-add Above PID</li>
   * <li>remove By Conservation to leave just Above PID</li>
   * <li>remove Above PID to leave original colours</li>
   * </ol>
   */
  @Test(groups = "Functional")
  public void testColourThresholdActions()
  {
    AlignViewport av = af.getViewport();
    AlignmentI al = av.getAlignment();

    /*
     * Colour alignment by Helix Propensity, no thresholds
     */
    af.applyToAllGroups_actionPerformed(false);
    af.changeColour_actionPerformed(JalviewColourScheme.Helix.toString());
    assertTrue(av.getGlobalColourScheme() instanceof HelixColourScheme);
    assertFalse(av.getResidueShading().conservationApplied());
    assertEquals(av.getResidueShading().getThreshold(), 0);

    /*
     * inspect the colour of 
     * FER_CAPAN.9(I), column 15 (14 base 0)
     * FER_CAPAN.10(SER), column 16 (15 base 0)
     */
    SequenceI ferCapan = al.findName("FER_CAPAN");
    ResidueShaderI rs = av.getResidueShading();
    Color c = rs.findColour('I', 14, ferCapan);
    Color i_original = new Color(138, 117, 138);
    assertEquals(c, i_original);
    c = rs.findColour('S', 15, ferCapan);
    Color s_original = new Color(54, 201, 54);
    assertEquals(c, s_original);

    /*
     * colour by conservation with increment 10
     */
    af.conservationMenuItem_actionPerformed(true);
    SliderPanel sp = SliderPanel.getSliderPanel();
    assertTrue(sp.isForConservation());
    assertEquals(sp.getValue(), 30); // initial slider setting
    c = rs.findColour('I', 14, ferCapan);
    Color i_faded = new Color(255, 255, 255);
    assertEquals(c, i_faded);
    sp.valueChanged(10);
    assertSame(rs, av.getResidueShading());
    assertEquals(rs.getConservationInc(), 10);
    c = rs.findColour('I', 14, ferCapan);
    i_faded = new Color(196, 186, 196);
    assertEquals(c, i_faded);
    c = rs.findColour('S', 15, ferCapan);
    Color s_faded = new Color(144, 225, 144);
    assertEquals(c, s_faded);

    /*
     * deselect By Conservation - colour should revert
     */
    af.conservationMenuItem_actionPerformed(false);
    c = rs.findColour('S', 15, ferCapan);
    assertEquals(c, s_original);

    /*
     * now Above PID, threshold = 0%
     * should be no change
     */
    af.abovePIDThreshold_actionPerformed(true);
    sp = SliderPanel.getSliderPanel();
    assertFalse(sp.isForConservation());
    assertEquals(sp.getValue(), 0); // initial slider setting
    c = rs.findColour('I', 14, ferCapan);
    assertEquals(c, i_original);
    c = rs.findColour('S', 15, ferCapan);
    assertEquals(c, s_original);

    /*
     * Above PID, threshold = 1%
     * 15.I becomes White because no match to consensus (V)
     * 16.S remains coloured as matches 66.66% consensus
     */
    sp.valueChanged(1);
    c = rs.findColour('I', 14, ferCapan);
    assertEquals(c, Color.white);
    c = rs.findColour('S', 15, ferCapan);
    assertEquals(c, s_original);

    /*
     * threshold 66% - no further change yet...
     */
    sp.valueChanged(66);
    c = rs.findColour('I', 14, ferCapan);
    assertEquals(c, Color.white);
    c = rs.findColour('S', 15, ferCapan);
    assertEquals(c, s_original);

    /*
     * threshold 67% - now both residues are white
     */
    sp.valueChanged(67);
    c = rs.findColour('I', 14, ferCapan);
    assertEquals(c, Color.white);
    c = rs.findColour('S', 15, ferCapan);
    assertEquals(c, Color.white);

    /*
     * deselect Above PID - colours should revert
     */
    af.abovePIDThreshold_actionPerformed(false);
    c = rs.findColour('I', 14, ferCapan);
    assertEquals(c, i_original);
    c = rs.findColour('S', 15, ferCapan);
    assertEquals(c, s_original);

    /*
     * Now combine Above 50% PID and By Conservation 10%
     * 15.I is White because no match to consensus (V)
     * 16.S is coloured but faded
     */
    af.abovePIDThreshold_actionPerformed(true);
    sp = SliderPanel.getSliderPanel();
    assertFalse(sp.isForConservation());
    sp.valueChanged(50);
    af.conservationMenuItem_actionPerformed(true);
    sp = SliderPanel.getSliderPanel();
    assertTrue(sp.isForConservation());
    sp.valueChanged(10);
    c = rs.findColour('I', 14, ferCapan);
    assertEquals(c, Color.white);
    c = rs.findColour('S', 15, ferCapan);
    assertEquals(c, s_faded);

    /*
     * turn off Above PID - should just leave Conservation fading as before 
     */
    af.abovePIDThreshold_actionPerformed(false);
    c = rs.findColour('I', 14, ferCapan);
    assertEquals(c, i_faded);
    c = rs.findColour('S', 15, ferCapan);
    assertEquals(c, s_faded);

    /*
     * Now add Above 50% PID to conservation colouring
     * - should give the same as PID followed by conservation (above)
     */
    af.abovePIDThreshold_actionPerformed(true);
    SliderPanel.getSliderPanel().valueChanged(50);
    c = rs.findColour('I', 14, ferCapan);
    assertEquals(c, Color.white);
    c = rs.findColour('S', 15, ferCapan);
    assertEquals(c, s_faded);

    /*
     * turn off By Conservation
     * should leave I white, S original (unfaded) colour
     */
    af.conservationMenuItem_actionPerformed(false);
    c = rs.findColour('I', 14, ferCapan);
    assertEquals(c, Color.white);
    c = rs.findColour('S', 15, ferCapan);
    assertEquals(c, s_original);

    /*
     * finally turn off Above PID to leave original colours
     */
    af.abovePIDThreshold_actionPerformed(false);
    c = rs.findColour('I', 14, ferCapan);
    assertEquals(c, i_original);
    c = rs.findColour('S', 15, ferCapan);
    assertEquals(c, s_original);
  }

  /**
   * Verify that making a New View transfers alignment and group colour schemes,
   * including any thresholds, to the new view. Because New View is performed by
   * saving and reloading a 'project' file, this is similar to verifying a
   * project save and reload.
   * 
   * @see Jalview2xmlTests#testStoreAndRecoverColourThresholds()
   */
  @Test(groups = "Functional")
  public void testNewView_colourThresholds()
  {
    AlignViewport av = af.getViewport();
    AlignmentI al = av.getAlignment();

    /*
     * Colour alignment by Buried Index, Above 10% PID, By Conservation 20%
     */
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
     */
    SequenceGroup sg = new SequenceGroup();
    sg.addSequence(al.getSequenceAt(0), false);
    sg.setStartRes(15);
    sg.setEndRes(25);
    av.setSelectionGroup(sg);
    PopupMenu popupMenu = new PopupMenu(af.alignPanel, al.getSequenceAt(0),
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
    rs = sg.getGroupColourScheme();
    assertTrue(rs.conservationApplied());
    assertEquals(rs.getConservationInc(), 30);
    assertEquals(rs.getThreshold(), 40);

    /*
     * set slider panel focus to the background alignment
     */
    af.conservationMenuItem_actionPerformed(true);
    sp = SliderPanel.getSliderPanel();
    assertTrue(sp.isForConservation());
    assertEquals(sp.getTitle(), MessageManager.formatMessage(
            "label.conservation_colour_increment", new String[]
            { "Background" }));

    /*
     * make a new View, verify alignment and group colour schemes
     */
    af.newView_actionPerformed(null);
    assertEquals(af.alignPanel.getViewName(), "View 1");
    AlignViewport av2 = af.getViewport();
    assertNotSame(av, av2);
    assertSame(av2, af.alignPanel.av);
    rs = av2.getResidueShading();
    assertNotSame(av.getResidueShading(), rs);
    assertEquals(rs.getThreshold(), 10);
    assertTrue(rs.conservationApplied(), rs.toString());
    assertEquals(rs.getConservationInc(), 20);
    assertEquals(av2.getAlignment().getGroups().size(), 1);
    sg = av2.getAlignment().getGroups().get(0);
    rs = sg.getGroupColourScheme();
    assertTrue(rs.conservationApplied());
    assertEquals(rs.getConservationInc(), 30);
    assertEquals(rs.getThreshold(), 40);

    /*
     * check the Conservation SliderPanel (still open) is linked to 
     * and updates the new view (JAL-2385)
     */
    sp = SliderPanel.getSliderPanel();
    assertTrue(sp.isForConservation());
    assertEquals(sp.getTitle(), MessageManager.formatMessage(
            "label.conservation_colour_increment", new String[]
            { "View 1" }));
    sp.valueChanged(22);
    assertEquals(av2.getResidueShading().getConservationInc(), 22);
  }

  /**
   * Verify that making a New View preserves the dataset reference for the
   * alignment. Otherwise, see a 'duplicate jar entry' reference when trying to
   * save alignments with multiple views, and codon mappings will not be shared
   * across all panels in a split frame.
   * 
   * @see Jalview2xmlTests#testStoreAndRecoverColourThresholds()
   */
  @Test(groups = "Functional")
  public void testNewView_dsRefPreserved()
  {
    AlignViewport av = af.getViewport();
    AlignmentI al = av.getAlignment();
    AlignmentI original_ds = al.getDataset();
    af.newView_actionPerformed(null);
    assertNotEquals("New view didn't select the a new panel", av,
            af.getViewport());
    org.testng.Assert.assertEquals(original_ds,
            af.getViewport().getAlignment().getDataset(),
            "Dataset was not preserved in new view");
  }
}
