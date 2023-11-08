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
import static org.testng.AssertJUnit.assertTrue;

import jalview.analysis.AnnotationSorter.SequenceAnnotationOrder;
import jalview.bin.Cache;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.FormatAdapter;
import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for AnnotationChooser
 * 
 * @author gmcarstairs
 *
 */
public class AnnotationChooserTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  // 4 sequences x 13 positions
  final static String TEST_DATA = ">FER_CAPAA Ferredoxin\n"
          + "TIETHKEAELVG-\n"
          + ">FER_CAPAN Ferredoxin, chloroplast precursor\n"
          + "TIETHKEAELVG-\n"
          + ">FER1_SOLLC Ferredoxin-1, chloroplast precursor\n"
          + "TIETHKEEELTA-\n" + ">Q93XJ9_SOLTU Ferredoxin I precursor\n"
          + "TIETHKEEELTA-\n";

  AnnotationChooser testee;

  AlignmentPanel parentPanel;

  AlignFrame af;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws IOException
  {
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    // pin down annotation sort order for test
    Cache.applicationProperties.setProperty(Preferences.SORT_ANNOTATIONS,
            SequenceAnnotationOrder.NONE.name());
    final String TRUE = Boolean.TRUE.toString();
    Cache.applicationProperties.setProperty(Preferences.SHOW_AUTOCALC_ABOVE,
            TRUE);
    Cache.applicationProperties.setProperty("SHOW_QUALITY", TRUE);
    Cache.applicationProperties.setProperty("SHOW_CONSERVATION", TRUE);
    Cache.applicationProperties.setProperty("SHOW_IDENTITY", TRUE);

    AlignmentI al = new FormatAdapter().readFile(TEST_DATA,
            DataSourceType.PASTE, FileFormat.Fasta);
    af = new AlignFrame(al, 700, 500);
    parentPanel = new AlignmentPanel(af, af.getViewport());
    addAnnotations();
  }

  /**
   * Add 4 annotations, 3 of them sequence-specific.
   * 
   * <PRE>
   * ann1 - for sequence 0 - label 'IUPRED' ann2 - not sequence related - label
   * 'Beauty' ann3 - for sequence 3 - label 'JMol' ann4 - for sequence 2 - label
   * 'IUPRED' ann5 - for sequence 1 - label 'JMol'
   */
  private void addAnnotations()
  {
    Annotation an = new Annotation(2f);
    Annotation[] anns = new Annotation[] { an, an, an };
    AlignmentAnnotation ann0 = new AlignmentAnnotation("IUPRED", "", anns);
    AlignmentAnnotation ann1 = new AlignmentAnnotation("Beauty", "", anns);
    AlignmentAnnotation ann2 = new AlignmentAnnotation("JMol", "", anns);
    AlignmentAnnotation ann3 = new AlignmentAnnotation("IUPRED", "", anns);
    AlignmentAnnotation ann4 = new AlignmentAnnotation("JMol", "", anns);
    SequenceI[] seqs = parentPanel.getAlignment().getSequencesArray();
    ann0.setSequenceRef(seqs[0]);
    ann2.setSequenceRef(seqs[3]);
    ann3.setSequenceRef(seqs[2]);
    ann4.setSequenceRef(seqs[1]);
    parentPanel.getAlignment().addAnnotation(ann0);
    parentPanel.getAlignment().addAnnotation(ann1);
    parentPanel.getAlignment().addAnnotation(ann2);
    parentPanel.getAlignment().addAnnotation(ann3);
    parentPanel.getAlignment().addAnnotation(ann4);
  }

  /**
   * Test creation of panel with OK and Cancel buttons
   */
  @Test(groups = { "Functional" })
  public void testBuildActionButtonsPanel()
  {
    testee = new AnnotationChooser(parentPanel);
    JPanel jp = testee.buildActionButtonsPanel();
    assertTrue("Wrong layout", jp.getLayout() instanceof FlowLayout);

    Component[] comps = jp.getComponents();
    assertEquals("Not 2 action buttons", 2, comps.length);

    final Component jb1 = comps[0];
    final Component jb2 = comps[1];

    assertEquals("Not 'OK' button", MessageManager.getString("action.ok"),
            ((JButton) jb1).getText());
    assertEquals("Wrong button font", JvSwingUtils.getLabelFont(),
            jb1.getFont());

    assertEquals("Not 'Cancel' button",
            MessageManager.getString("action.cancel"),
            ((JButton) jb2).getText());
    assertEquals("Wrong button font", JvSwingUtils.getLabelFont(),
            jb2.getFont());
  }

  /**
   * Test 'Apply to' has 3 radio buttons enabled, 'Selected Sequences' selected,
   * when there is a current selection group.
   */
  @Test(groups = { "Functional" })
  public void testBuildApplyToOptionsPanel_withSelectionGroup()
  {
    selectSequences(0, 2, 3);
    testee = new AnnotationChooser(parentPanel);

    JPanel jp = testee.buildApplyToOptionsPanel();
    Component[] comps = jp.getComponents();
    assertEquals("Not 3 radio buttons", 3, comps.length);

    final Checkbox cb1 = (Checkbox) comps[0];
    final Checkbox cb2 = (Checkbox) comps[1];
    final Checkbox cb3 = (Checkbox) comps[2];

    assertTrue("Not enabled", cb1.isEnabled());
    assertTrue("Not enabled", cb2.isEnabled());
    assertTrue("Not enabled", cb3.isEnabled());
    assertEquals("Option not selected", cb2,
            cb2.getCheckboxGroup().getSelectedCheckbox());

    // check state variables match checkbox selection
    assertTrue(testee.isApplyToSelectedSequences());
    assertFalse(testee.isApplyToUnselectedSequences());
  }

  /**
   * Add a sequence group to the alignment with the specified sequences (base 0)
   * in it
   * 
   * @param i
   * @param more
   */
  private void selectSequences(int... selected)
  {
    SequenceI[] seqs = parentPanel.getAlignment().getSequencesArray();
    SequenceGroup sg = new SequenceGroup();
    for (int i : selected)
    {
      sg.addSequence(seqs[i], false);
    }
    parentPanel.av.setSelectionGroup(sg);
  }

  /**
   * Test 'Apply to' has 1 radio button enabled, 'All Sequences' selected, when
   * there is no current selection group.
   */
  @Test(groups = { "Functional" })
  public void testBuildApplyToOptionsPanel_noSelectionGroup()
  {
    testee = new AnnotationChooser(parentPanel);
    JPanel jp = testee.buildApplyToOptionsPanel();
    verifyApplyToOptionsPanel_noSelectionGroup(jp);
  }

  protected void verifyApplyToOptionsPanel_noSelectionGroup(JPanel jp)
  {
    assertTrue("Wrong layout", jp.getLayout() instanceof FlowLayout);
    Component[] comps = jp.getComponents();
    assertEquals("Not 3 radio buttons", 3, comps.length);

    final Checkbox cb1 = (Checkbox) comps[0];
    final Checkbox cb2 = (Checkbox) comps[1];
    final Checkbox cb3 = (Checkbox) comps[2];

    assertTrue("Not enabled", cb1.isEnabled());
    assertFalse("Enabled", cb2.isEnabled());
    assertFalse("Enabled", cb3.isEnabled());
    assertEquals("Not selected", cb1,
            cb1.getCheckboxGroup().getSelectedCheckbox());

    // check state variables match checkbox selection
    assertTrue(testee.isApplyToSelectedSequences());
    assertTrue(testee.isApplyToUnselectedSequences());

    assertEquals("Wrong text",
            MessageManager.getString("label.all_sequences"),
            cb1.getLabel());
    assertEquals("Wrong text",
            MessageManager.getString("label.selected_sequences"),
            cb2.getLabel());
    assertEquals("Wrong text",
            MessageManager.getString("label.except_selected_sequences"),
            cb3.getLabel());
  }

  /**
   * Test Show and Hide radio buttons created, with Hide initially selected.
   */
  @Test(groups = { "Functional" })
  public void testBuildShowHidePanel()
  {
    testee = new AnnotationChooser(parentPanel);
    JPanel jp = testee.buildShowHidePanel();
    verifyShowHidePanel(jp);

  }

  protected void verifyShowHidePanel(JPanel jp)
  {
    assertTrue("Wrong layout", jp.getLayout() instanceof FlowLayout);
    Component[] comps = jp.getComponents();
    assertEquals("Not 2 radio buttons", 2, comps.length);

    final Checkbox cb1 = (Checkbox) comps[0];
    final Checkbox cb2 = (Checkbox) comps[1];

    assertTrue("Show not enabled", cb1.isEnabled());
    assertTrue("Hide not enabled", cb2.isEnabled());

    // Hide (button 2) selected; note this may change to none (null)
    assertEquals("Not selected", cb2,
            cb2.getCheckboxGroup().getSelectedCheckbox());

    assertTrue("Show is flagged", !testee.isShowSelected());

    assertEquals("Wrong text",
            MessageManager.getString("label.show_selected_annotations"),
            cb1.getLabel());
    assertEquals("Wrong text",
            MessageManager.getString("label.hide_selected_annotations"),
            cb2.getLabel());
  }

  /**
   * Test construction of panel containing two sub-panels
   */
  @Test(groups = { "Functional" })
  public void testBuildShowHideOptionsPanel()
  {
    testee = new AnnotationChooser(parentPanel);
    JPanel jp = testee.buildShowHideOptionsPanel();
    assertTrue("Wrong layout", jp.getLayout() instanceof BorderLayout);
    Component[] comps = jp.getComponents();
    assertEquals("Not 2 sub-panels", 2, comps.length);

    verifyShowHidePanel((JPanel) comps[0]);
    verifyApplyToOptionsPanel_noSelectionGroup((JPanel) comps[1]);
  }

  /**
   * Test that annotation types are (uniquely) identified.
   * 
   */
  @Test(groups = { "Functional" })
  public void testGetAnnotationTypes()
  {
    selectSequences(1);
    testee = new AnnotationChooser(parentPanel);
    // selection group should make no difference to the result
    // as all annotation types for the alignment are considered

    List<String> types = AnnotationChooser
            .getAnnotationTypes(parentPanel.getAlignment(), true);
    assertEquals("Not two annotation types", 2, types.size());
    assertTrue("IUPRED missing", types.contains("IUPRED"));
    assertTrue("JMol missing", types.contains("JMol"));

    types = AnnotationChooser.getAnnotationTypes(parentPanel.getAlignment(),
            false);
    assertEquals("Not six annotation types", 7, types.size());
    assertTrue("IUPRED missing", types.contains("IUPRED"));
    assertTrue("JMol missing", types.contains("JMol"));
    assertTrue("Beauty missing", types.contains("Beauty"));
    // These are added by viewmodel.AlignViewport.initAutoAnnotation():
    assertTrue("Consensus missing", types.contains("Consensus"));
    assertTrue("Quality missing", types.contains("Quality"));
    assertTrue("Conservation missing", types.contains("Conservation"));
    assertTrue("Occupancy missing", types.contains("Occupancy"));
  }

  /**
   * Test result of selecting an annotation type, with 'Hide for all sequences'.
   * 
   * We expect all annotations of that type to be set hidden. Other annotations
   * should be left visible.
   */
  @Test(groups = { "Functional" })
  public void testSelectType_hideForAll()
  {
    selectSequences(1, 2);
    testee = new AnnotationChooser(parentPanel);
    final Checkbox hideCheckbox = (Checkbox) getComponent(testee, 1, 0, 1);
    setSelected(hideCheckbox, true);

    final Checkbox allSequencesCheckbox = (Checkbox) getComponent(testee, 1,
            1, 0);
    setSelected(allSequencesCheckbox, true);

    AlignmentAnnotation[] anns = parentPanel.getAlignment()
            .getAlignmentAnnotation();

    int autocalc = countAutocalc(anns);
    assertTrue(anns[autocalc + 2].visible); // JMol for seq3
    assertTrue(anns[autocalc + 4].visible); // JMol for seq1

    setSelected(getTypeCheckbox("JMol"), true);
    assertTrue(anns[0].visible); // Conservation
    assertTrue(anns[1].visible); // Quality
    assertTrue(anns[2].visible); // Consensus
    assertTrue(anns[3].visible); // Occupancy
    assertTrue(anns[4].visible); // IUPred for seq0
    assertTrue(anns[5].visible); // Beauty
    assertFalse(anns[6].visible); // JMol for seq3 - not selected but hidden
    assertTrue(anns[7].visible); // IUPRED for seq2
    assertFalse(anns[8].visible); // JMol for seq1 - selected and hidden
  }

  /**
   * Test result of selecting an annotation type, with 'Hide for selected
   * sequences'.
   * 
   * We expect the annotations of that type, linked to the sequence group, to be
   * set hidden. Other annotations should be left visible.
   */
  @Test(groups = { "Functional" })
  public void testSelectType_hideForSelected()
  {
    selectSequences(1, 2);
    testee = new AnnotationChooser(parentPanel);
    final Checkbox hideCheckbox = (Checkbox) getComponent(testee, 1, 0, 1);
    setSelected(hideCheckbox, true);

    /*
     * Don't set the 'selected sequences' radio button since this would trigger
     * an update, including unselected sequences / annotation types
     */
    // setSelected(getSelectedSequencesCheckbox());

    AlignmentAnnotation[] anns = parentPanel.getAlignment()
            .getAlignmentAnnotation();

    int autocalc = countAutocalc(anns);
    assertTrue(anns[autocalc + 4].visible); // JMol for seq1

    setSelected(getTypeCheckbox("JMol"), true);
    assertTrue(anns[0].visible); // Conservation
    assertTrue(anns[1].visible); // Quality
    assertTrue(anns[2].visible); // Consensus
    assertTrue(anns[3].visible); // Occupancy
    assertTrue(anns[4].visible); // IUPred for seq0
    assertTrue(anns[5].visible); // Beauty
    assertTrue(anns[6].visible); // JMol for seq3 not in selection group
    assertTrue(anns[7].visible); // IUPRED for seq2
    assertFalse(anns[8].visible); // JMol for seq1 in selection group
  }

  /**
   * Test result of deselecting an annotation type, with 'Hide for all
   * sequences'.
   * 
   * We expect all annotations of that type to be set visible. Other annotations
   * should be left unchanged.
   */
  @Test(groups = { "Functional" })
  public void testDeselectType_hideForAll()
  {
    selectSequences(1, 2);
    testee = new AnnotationChooser(parentPanel);

    final Checkbox hideCheckbox = (Checkbox) getComponent(testee, 1, 0, 1);
    setSelected(hideCheckbox, true);

    final Checkbox allSequencesCheckbox = (Checkbox) getComponent(testee, 1,
            1, 0);
    setSelected(allSequencesCheckbox, true);

    AlignmentAnnotation[] anns = parentPanel.getAlignment()
            .getAlignmentAnnotation();

    final Checkbox typeCheckbox = getTypeCheckbox("JMol");

    // select JMol - all hidden
    setSelected(typeCheckbox, true);
    int autocalc = countAutocalc(anns);
    assertFalse(anns[autocalc + 2].visible); // JMol for seq3
    assertFalse(anns[autocalc + 4].visible); // JMol for seq1

    // deselect JMol - all unhidden
    setSelected(typeCheckbox, false);
    for (AlignmentAnnotation ann : anns)
    {
      assertTrue(ann.visible);
    }
  }

  /**
   * Returns a count of autocalculated annotations in the set provided
   * 
   * @param anns
   * @return
   */
  private int countAutocalc(AlignmentAnnotation[] anns)
  {
    int count = 0;
    for (AlignmentAnnotation ann : anns)
    {
      if (ann.autoCalculated)
      {
        count++;
      }
    }
    return count;
  }

  /**
   * Test result of deselecting an annotation type, with 'Hide for selected
   * sequences'.
   * 
   * We expect the annotations of that type, linked to the sequence group, to be
   * set visible. Other annotations should be left unchanged.
   */
  @Test(groups = { "Functional" })
  public void testDeselectType_hideForSelected()
  {
    selectSequences(1, 2);
    testee = new AnnotationChooser(parentPanel);
    final Checkbox hideCheckbox = (Checkbox) getComponent(testee, 1, 0, 1);
    setSelected(hideCheckbox, true);

    /*
     * Don't set the 'selected sequences' radio button since this would trigger
     * an update, including unselected sequences / annotation types
     */
    // setSelected(getSelectedSequencesCheckbox());

    setSelected(getTypeCheckbox("JMol"), true);
    setSelected(getTypeCheckbox("JMol"), false);

    AlignmentAnnotation[] anns = parentPanel.getAlignment()
            .getAlignmentAnnotation();
    assertTrue(anns[0].visible); // Conservation
    assertTrue(anns[1].visible); // Quality
    assertTrue(anns[2].visible); // Consensus
    assertTrue(anns[3].visible); // IUPred for seq0
    assertTrue(anns[4].visible); // Beauty
    assertTrue(anns[5].visible); // JMol for seq3 not in selection group
    assertTrue(anns[6].visible); // IUPRED for seq2
    assertTrue(anns[7].visible); // JMol for seq1 in selection group
  }

  /**
   * Test result of selecting an annotation type, with 'Show for all sequences'.
   * 
   * We expect all annotations of that type to be set visible. Other annotations
   * should be left unchanged
   */
  @Test(groups = { "Functional" })
  public void testSelectType_showForAll()
  {
    selectSequences(1, 2);
    testee = new AnnotationChooser(parentPanel);
    final Checkbox showCheckbox = (Checkbox) getComponent(testee, 1, 0, 0);
    final Checkbox hideCheckbox = (Checkbox) getComponent(testee, 1, 0, 1);

    final Checkbox allSequencesCheckbox = (Checkbox) getComponent(testee, 1,
            1, 0);

    AlignmentAnnotation[] anns = parentPanel.getAlignment()
            .getAlignmentAnnotation();

    // hide all JMol annotations
    setSelected(allSequencesCheckbox, true);
    setSelected(hideCheckbox, true);
    setSelected(getTypeCheckbox("JMol"), true);
    int autocalc = countAutocalc(anns);
    assertFalse(anns[autocalc + 2].visible); // JMol for seq3
    assertFalse(anns[autocalc + 4].visible); // JMol for seq1
    // ...now show them...
    setSelected(showCheckbox, true);
    for (AlignmentAnnotation ann : anns)
    {
      assertTrue(ann.visible);
    }
  }

  /**
   * Test result of selecting an annotation type, with 'Show for selected
   * sequences'.
   * 
   * We expect all annotations of that type, linked to the sequence group, to be
   * set visible. Other annotations should be left unchanged
   */
  @Test(groups = { "Functional" })
  public void testSelectType_showForSelected()
  {
    // sequences 1 and 2 have annotations IUPred and Jmol
    selectSequences(1, 2);
    testee = new AnnotationChooser(parentPanel);
    final Checkbox showCheckbox = (Checkbox) getComponent(testee, 1, 0, 0);
    final Checkbox hideCheckbox = (Checkbox) getComponent(testee, 1, 0, 1);

    final Checkbox selectedSequencesCheckbox = (Checkbox) getComponent(
            testee, 1, 1, 1);

    AlignmentAnnotation[] anns = parentPanel.getAlignment()
            .getAlignmentAnnotation();

    // hide all JMol annotations in the selection region (== annotation 7)
    setSelected(selectedSequencesCheckbox, true);
    setSelected(hideCheckbox, true);
    setSelected(getTypeCheckbox("JMol"), true);

    int autocalc = countAutocalc(anns);
    assertTrue(anns[autocalc + 2].visible); // JMol for seq3
    assertFalse(anns[autocalc + 4].visible); // JMol for seq1
    // ...now show them...
    setSelected(showCheckbox, true);

    for (AlignmentAnnotation ann : anns)
    {
      assertTrue(ann.visible);
    }
  }

  /**
   * Test result of deselecting an annotation type, with 'Show for all
   * sequences'.
   * 
   * We expect all annotations of that type to be set hidden. Other annotations
   * should be left unchanged.
   */
  @Test(groups = { "Functional" })
  public void testDeselectType_showForAll()
  {
    selectSequences(1, 2);
    testee = new AnnotationChooser(parentPanel);

    final Checkbox showCheckbox = (Checkbox) getComponent(testee, 1, 0, 0);
    setSelected(showCheckbox, true);

    final Checkbox allSequencesCheckbox = (Checkbox) getComponent(testee, 1,
            1, 0);
    setSelected(allSequencesCheckbox, true);

    AlignmentAnnotation[] anns = parentPanel.getAlignment()
            .getAlignmentAnnotation();

    final Checkbox typeCheckbox = getTypeCheckbox("JMol");
    // select JMol - all shown
    setSelected(typeCheckbox, true);
    int autocalc = countAutocalc(anns);
    assertTrue(anns[autocalc + 2].visible); // JMol for seq3
    assertTrue(anns[autocalc + 4].visible); // JMol for seq1

    // deselect JMol - all hidden
    setSelected(typeCheckbox, false);
    assertTrue(anns[0].visible); // Conservation
    assertTrue(anns[1].visible); // Quality
    assertTrue(anns[2].visible); // Consensus
    assertTrue(anns[3].visible); // Occupancy
    assertTrue(anns[4].visible); // IUPred for seq0
    assertTrue(anns[5].visible); // Beauty
    assertFalse(anns[6].visible); // JMol for seq3
    assertTrue(anns[7].visible); // IUPRED for seq2
    assertFalse(anns[8].visible); // JMol for seq1
  }

  /**
   * Test result of deselecting an annotation type, with 'Show for selected
   * sequences'.
   * 
   * We expect the annotations of that type, linked to the sequence group, to be
   * set hidden. Other annotations should be left unchanged.
   */
  @Test(groups = { "Functional" })
  public void testDeselectType_showForSelected()
  {
    selectSequences(1, 2);
    testee = new AnnotationChooser(parentPanel);
    final Checkbox showCheckbox = (Checkbox) getComponent(testee, 1, 0, 0);
    setSelected(showCheckbox, true);

    /*
     * Don't set the 'selected sequences' radio button since this would trigger
     * an update, including unselected sequences / annotation types
     */
    // setSelected(getSelectedSequencesCheckbox());

    AlignmentAnnotation[] anns = parentPanel.getAlignment()
            .getAlignmentAnnotation();

    // select JMol - should remain visible
    setSelected(getTypeCheckbox("JMol"), true);
    int autocalc = countAutocalc(anns);
    assertTrue(anns[autocalc + 2].visible); // JMol for seq3
    assertTrue(anns[autocalc + 4].visible); // JMol for seq1

    // deselect JMol - should be hidden for selected sequences only
    setSelected(getTypeCheckbox("JMol"), false);
    assertTrue(anns[0].visible); // Conservation
    assertTrue(anns[1].visible); // Quality
    assertTrue(anns[2].visible); // Consensus
    assertTrue(anns[3].visible); // Occupancy
    assertTrue(anns[4].visible); // IUPred for seq0
    assertTrue(anns[5].visible); // Beauty
    assertTrue(anns[6].visible); // JMol for seq3 not in selection group
    assertTrue(anns[7].visible); // IUPRED for seq2
    assertFalse(anns[8].visible); // JMol for seq1 in selection group
  }

  /**
   * Helper method to drill down to a sub-component in a Container hierarchy.
   * 
   * @param cont
   * @param i
   * @param j
   * @param k
   * @return
   */
  public static Component getComponent(Container cont, int... positions)
  {
    Component comp = cont;
    for (int i : positions)
    {
      comp = ((Container) comp).getComponent(i);
    }
    return comp;
  }

  /**
   * Helper method to set or unset a checkbox and fire its action listener.
   * 
   * @param cb
   * @param select
   */
  protected void setSelected(Checkbox cb, boolean select)
  {
    // TODO refactor to a test utility class
    cb.setState(select);
    // have to manually fire the action listener
    cb.getItemListeners()[0].itemStateChanged(
            new ItemEvent(cb, ItemEvent.ITEM_STATE_CHANGED, cb,
                    select ? ItemEvent.SELECTED : ItemEvent.DESELECTED));
  }

  /**
   * Helper method to drill down to the 'Annotation type' checkbox with given
   * label.
   * 
   * @return
   */
  private Checkbox getTypeCheckbox(String forLabel)
  {
    Component[] cbs = ((JPanel) testee.getComponent(0)).getComponents();
    for (Component comp : cbs)
    {
      final Checkbox cb = (Checkbox) comp;
      if (cb.getLabel().equals(forLabel))
      {
        return cb;
      }
    }
    return null;
  }

  /**
   * Test isInActionScope for the case where the scope is selected sequences.
   * Test cases include sequences in the selection group, and others not in the
   * group.
   */
  @Test(groups = { "Functional" })
  public void testIsInActionScope_selectedScope()
  {
    // sequences 1 and 2 have annotations 4 and 3 respectively
    selectSequences(1, 2);
    testee = new AnnotationChooser(parentPanel);

    final Checkbox selectedSequencesCheckbox = (Checkbox) getComponent(
            testee, 1, 1, 1);
    setSelected(selectedSequencesCheckbox, true);

    AlignmentAnnotation[] anns = parentPanel.getAlignment()
            .getAlignmentAnnotation();
    int autocalc = countAutocalc(anns);
    assertFalse(testee.isInActionScope(anns[autocalc]));
    assertFalse(testee.isInActionScope(anns[autocalc + 1]));
    assertFalse(testee.isInActionScope(anns[autocalc + 2]));
    assertTrue(testee.isInActionScope(anns[autocalc + 3]));
    assertTrue(testee.isInActionScope(anns[autocalc + 4]));
  }

  /**
   * Test isInActionScope for the case where the scope is unselected sequences.
   * Test cases include sequences in the selection group, and others not in the
   * group.
   */
  @Test(groups = { "Functional" })
  public void testIsInActionScope_unselectedScope()
  {
    // sequences 1 and 2 have annotations 4 and 3 respectively
    selectSequences(1, 2);
    testee = new AnnotationChooser(parentPanel);

    final Checkbox unselectedSequencesCheckbox = (Checkbox) getComponent(
            testee, 1, 1, 2);
    setSelected(unselectedSequencesCheckbox, true);

    AlignmentAnnotation[] anns = parentPanel.getAlignment()
            .getAlignmentAnnotation();
    int autocalc = countAutocalc(anns);
    assertTrue(testee.isInActionScope(anns[autocalc]));
    assertTrue(testee.isInActionScope(anns[autocalc + 1]));
    assertTrue(testee.isInActionScope(anns[autocalc + 2]));
    assertFalse(testee.isInActionScope(anns[autocalc + 3]));
    assertFalse(testee.isInActionScope(anns[autocalc + 4]));
  }

  /**
   * Test that the reset method restores previous visibility flags.
   */
  @Test(groups = { "Functional" })
  public void testResetOriginalState()
  {
    testee = new AnnotationChooser(parentPanel);

    AlignmentAnnotation[] anns = parentPanel.getAlignment()
            .getAlignmentAnnotation();
    // all start visible
    for (int i = 0; i < anns.length; i++)
    {
      assertTrue(i + "'th sequence not visible", anns[i].visible);
    }

    /*
     * check options to hide JMol and IUPRED annotations for all sequences
     */
    final Checkbox hideCheckbox = (Checkbox) getComponent(testee, 1, 0, 1);
    setSelected(hideCheckbox, true);

    final Checkbox allSequencesCheckbox = (Checkbox) getComponent(testee, 1,
            1, 0);
    setSelected(allSequencesCheckbox, true);

    setSelected(getTypeCheckbox("JMol"), true);
    setSelected(getTypeCheckbox("IUPRED"), true);

    assertTrue(anns[0].visible); // Conservation
    assertTrue(anns[1].visible); // Quality
    assertTrue(anns[2].visible); // Consensus
    assertTrue(anns[3].visible); // Occupancy
    assertFalse(anns[4].visible); // IUPRED
    assertTrue(anns[5].visible); // Beauty (not seq-related)
    assertFalse(anns[6].visible); // JMol
    assertFalse(anns[7].visible); // IUPRED
    assertFalse(anns[8].visible); // JMol

    // reset - should all be visible
    testee.resetOriginalState();
    for (int i = 0; i < anns.length; i++)
    {
      assertTrue(i + "'th sequence not visible", anns[i].visible);
    }
  }
}
