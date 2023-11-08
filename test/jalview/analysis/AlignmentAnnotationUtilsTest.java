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
package jalview.analysis;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AlignmentAnnotationUtilsTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  // 4 sequences x 13 positions
  final static String EOL = "\n";

  // @formatter:off
  final static String TEST_DATA = 
          ">FER_CAPAA Ferredoxin" + EOL +
          "TIETHKEAELVG-" + EOL +
          ">FER_CAPAN Ferredoxin, chloroplast precursor" + EOL +
          "TIETHKEAELVG-" + EOL +
          ">FER1_SOLLC Ferredoxin-1, chloroplast precursor" + EOL +
          "TIETHKEEELTA-" + EOL + 
          ">Q93XJ9_SOLTU Ferredoxin I precursor" + EOL +
          "TIETHKEEELTA-" + EOL;
  // @formatter:on

  private static final int SEQ_ANN_COUNT = 12;

  private AlignmentI alignment;

  /**
   * Test method that converts a (possibly null) array to a list.
   */
  @Test(groups = { "Functional" })
  public void testAsList()
  {
    // null array
    Collection<AlignmentAnnotation> c1 = AlignmentAnnotationUtils
            .asList(null);
    assertTrue(c1.isEmpty());

    // empty array
    AlignmentAnnotation[] anns = new AlignmentAnnotation[0];
    c1 = AlignmentAnnotationUtils.asList(anns);
    assertTrue(c1.isEmpty());

    // non-empty array
    anns = new AlignmentAnnotation[2];
    anns[0] = new AlignmentAnnotation("label0", "desc0", 0.0f);
    anns[1] = new AlignmentAnnotation("label1", "desc1", 1.0f);
    c1 = AlignmentAnnotationUtils.asList(anns);
    assertEquals(2, c1.size());
    assertTrue(c1.contains(anns[0]));
    assertTrue(c1.contains(anns[1]));
  }

  /**
   * This output is not part of the test but may help make sense of it...
   * 
   * @param shownTypes
   * @param hiddenTypes
   */
  protected void consoleDebug(Map<String, List<List<String>>> shownTypes,
          Map<String, List<List<String>>> hiddenTypes)
  {
    for (String calcId : shownTypes.keySet())
    {
      System.out.println("Visible annotation types for calcId=" + calcId);
      for (List<String> type : shownTypes.get(calcId))
      {
        System.out.println("   " + type);
      }
    }
    for (String calcId : hiddenTypes.keySet())
    {
      System.out.println("Hidden annotation types for calcId=" + calcId);
      for (List<String> type : hiddenTypes.get(calcId))
      {
        System.out.println("   " + type);
      }
    }
  }

  /**
   * Add a sequence group to the alignment with the specified sequences (base 0)
   * in it
   * 
   * @param i
   * @param more
   */
  private List<SequenceI> selectSequences(int... selected)
  {
    List<SequenceI> result = new ArrayList<SequenceI>();
    SequenceI[] seqs = alignment.getSequencesArray();
    for (int i : selected)
    {
      result.add(seqs[i]);
    }
    return result;
  }

  /**
   * Load the test alignment and generate annotations on it
   * 
   * @throws IOException
   */
  @BeforeMethod(alwaysRun = true)
  public void setUp() throws IOException
  {
    alignment = new jalview.io.FormatAdapter().readFile(TEST_DATA,
            DataSourceType.PASTE, FileFormat.Fasta);

    AlignmentAnnotation[] anns = new AlignmentAnnotation[SEQ_ANN_COUNT];
    for (int i = 0; i < anns.length; i++)
    {
      /*
       * Use the constructor for a positional annotation (with an Annotation
       * array)
       */
      anns[i] = new AlignmentAnnotation("Label" + i, "Desc " + i,
              new Annotation[] {});
      anns[i].setCalcId("CalcId" + i);
      anns[i].visible = true;
      alignment.addAnnotation(anns[i]);
    }
  }

  /**
   * Test a mixture of show/hidden annotations in/outside selection group.
   */
  @Test(groups = { "Functional" })
  public void testGetShownHiddenTypes_forSelectionGroup()
  {
    Map<String, List<List<String>>> shownTypes = new HashMap<String, List<List<String>>>();
    Map<String, List<List<String>>> hiddenTypes = new HashMap<String, List<List<String>>>();
    AlignmentAnnotation[] anns = alignment.getAlignmentAnnotation();
    SequenceI[] seqs = alignment.getSequencesArray();

    /*
     * Configure annotation properties for test
     */
    // not in selection group (should be ignored):
    // hidden annotation Label4 not in selection group
    anns[4].sequenceRef = seqs[2];
    anns[4].visible = false;
    anns[7].sequenceRef = seqs[1];
    anns[7].visible = true;

    /*
     * in selection group, hidden:
     */
    anns[2].sequenceRef = seqs[3]; // CalcId2/Label2
    anns[2].visible = false;
    anns[3].sequenceRef = seqs[3]; // CalcId3/Label2
    anns[3].visible = false;
    anns[3].label = "Label2";
    anns[4].sequenceRef = seqs[3]; // CalcId2/Label3
    anns[4].visible = false;
    anns[4].label = "Label3";
    anns[4].setCalcId("CalcId2");
    anns[8].sequenceRef = seqs[0]; // CalcId9/Label9
    anns[8].visible = false;
    anns[8].label = "Label9";
    anns[8].setCalcId("CalcId9");
    /*
     * in selection group, visible
     */
    anns[6].sequenceRef = seqs[0]; // CalcId6/Label6
    anns[6].visible = true;
    anns[9].sequenceRef = seqs[3]; // CalcId9/Label9
    anns[9].visible = true;

    List<SequenceI> selected = selectSequences(0, 3);
    AlignmentAnnotationUtils.getShownHiddenTypes(shownTypes, hiddenTypes,
            AlignmentAnnotationUtils.asList(anns), selected);

    // check results; note CalcId9/Label9 is both hidden and shown (for
    // different sequences) so should be in both
    // shown: CalcId6/Label6 and CalcId9/Label9
    assertEquals(2, shownTypes.size());
    assertEquals(1, shownTypes.get("CalcId6").size());
    assertEquals(1, shownTypes.get("CalcId6").get(0).size());
    assertEquals("Label6", shownTypes.get("CalcId6").get(0).get(0));
    assertEquals(1, shownTypes.get("CalcId9").size());
    assertEquals(1, shownTypes.get("CalcId9").get(0).size());
    assertEquals("Label9", shownTypes.get("CalcId9").get(0).get(0));

    // hidden: CalcId2/Label2, CalcId2/Label3, CalcId3/Label2, CalcId9/Label9
    assertEquals(3, hiddenTypes.size());
    assertEquals(2, hiddenTypes.get("CalcId2").size());
    assertEquals(1, hiddenTypes.get("CalcId2").get(0).size());
    assertEquals("Label2", hiddenTypes.get("CalcId2").get(0).get(0));
    assertEquals(1, hiddenTypes.get("CalcId2").get(1).size());
    assertEquals("Label3", hiddenTypes.get("CalcId2").get(1).get(0));
    assertEquals(1, hiddenTypes.get("CalcId3").size());
    assertEquals(1, hiddenTypes.get("CalcId3").get(0).size());
    assertEquals("Label2", hiddenTypes.get("CalcId3").get(0).get(0));
    assertEquals(1, hiddenTypes.get("CalcId9").size());
    assertEquals(1, hiddenTypes.get("CalcId9").get(0).size());
    assertEquals("Label9", hiddenTypes.get("CalcId9").get(0).get(0));

    consoleDebug(shownTypes, hiddenTypes);
  }

  /**
   * Test case where there are 'grouped' annotations, visible and hidden, within
   * and without the selection group.
   */
  @Test(groups = { "Functional" })
  public void testGetShownHiddenTypes_withGraphGroups()
  {
    final int GROUP_3 = 3;
    final int GROUP_4 = 4;
    final int GROUP_5 = 5;
    final int GROUP_6 = 6;

    Map<String, List<List<String>>> shownTypes = new HashMap<String, List<List<String>>>();
    Map<String, List<List<String>>> hiddenTypes = new HashMap<String, List<List<String>>>();
    AlignmentAnnotation[] anns = alignment.getAlignmentAnnotation();
    SequenceI[] seqs = alignment.getSequencesArray();

    /*
     * Annotations for selection group and graph group
     * 
     * Hidden annotations Label2, Label3, in (hidden) group 5
     */
    anns[2].sequenceRef = seqs[3];
    anns[2].visible = false;
    anns[2].graph = AlignmentAnnotation.LINE_GRAPH;
    anns[2].graphGroup = GROUP_5; // not a visible group
    anns[3].sequenceRef = seqs[0];
    anns[3].visible = false;
    anns[3].graph = AlignmentAnnotation.LINE_GRAPH;
    anns[3].graphGroup = GROUP_5;
    // need to ensure annotations have the same calcId as well
    anns[3].setCalcId("CalcId2");
    // annotations for a different hidden group generating the same group label
    anns[10].sequenceRef = seqs[0];
    anns[10].visible = false;
    anns[10].graph = AlignmentAnnotation.LINE_GRAPH;
    anns[10].graphGroup = GROUP_3;
    anns[10].label = "Label3";
    anns[10].setCalcId("CalcId2");
    anns[11].sequenceRef = seqs[3];
    anns[11].visible = false;
    anns[11].graph = AlignmentAnnotation.LINE_GRAPH;
    anns[11].graphGroup = GROUP_3;
    anns[11].label = "Label2";
    anns[11].setCalcId("CalcId2");

    // annotations Label1 (hidden), Label5 (visible) in group 6 (visible)
    anns[1].sequenceRef = seqs[3];
    // being in a visible group should take precedence over this visibility
    anns[1].visible = false;
    anns[1].graph = AlignmentAnnotation.LINE_GRAPH;
    anns[1].graphGroup = GROUP_6;
    anns[5].sequenceRef = seqs[0];
    anns[5].visible = true;
    anns[5].graph = AlignmentAnnotation.LINE_GRAPH;
    anns[5].graphGroup = GROUP_6;
    anns[5].setCalcId("CalcId1");
    /*
     * Annotations 0 and 4 are visible, for a different CalcId and graph group.
     * They produce the same label as annotations 1 and 5, which should not be
     * duplicated in the results. This case corresponds to (e.g.) many
     * occurrences of an IUPred Short/Long annotation group, one per sequence.
     */
    anns[4].sequenceRef = seqs[0];
    anns[4].visible = false;
    anns[4].graph = AlignmentAnnotation.LINE_GRAPH;
    anns[4].graphGroup = GROUP_4;
    anns[4].label = "Label1";
    anns[4].setCalcId("CalcId1");
    anns[0].sequenceRef = seqs[0];
    anns[0].visible = true;
    anns[0].graph = AlignmentAnnotation.LINE_GRAPH;
    anns[0].graphGroup = GROUP_4;
    anns[0].label = "Label5";
    anns[0].setCalcId("CalcId1");

    /*
     * Annotations outwith selection group - should be ignored.
     */
    // Hidden grouped annotations
    anns[6].sequenceRef = seqs[2];
    anns[6].visible = false;
    anns[6].graph = AlignmentAnnotation.LINE_GRAPH;
    anns[6].graphGroup = GROUP_4;
    anns[8].sequenceRef = seqs[1];
    anns[8].visible = false;
    anns[8].graph = AlignmentAnnotation.LINE_GRAPH;
    anns[8].graphGroup = GROUP_4;

    // visible grouped annotations Label7, Label9
    anns[7].sequenceRef = seqs[2];
    anns[7].visible = true;
    anns[7].graph = AlignmentAnnotation.LINE_GRAPH;
    anns[7].graphGroup = GROUP_4;
    anns[9].sequenceRef = seqs[1];
    anns[9].visible = true;
    anns[9].graph = AlignmentAnnotation.LINE_GRAPH;
    anns[9].graphGroup = GROUP_4;

    /*
     * Generate annotations[] arrays to match aligned columns
     */
    // adjustForAlignment(anns);

    List<SequenceI> selected = selectSequences(0, 3);
    AlignmentAnnotationUtils.getShownHiddenTypes(shownTypes, hiddenTypes,
            AlignmentAnnotationUtils.asList(anns), selected);

    consoleDebug(shownTypes, hiddenTypes);

    // CalcId1 / Label1, Label5 (only) should be 'shown', once, as a compound
    // type
    assertEquals(1, shownTypes.size());
    assertEquals(1, shownTypes.get("CalcId1").size());
    assertEquals(2, shownTypes.get("CalcId1").get(0).size());
    assertEquals("Label1", shownTypes.get("CalcId1").get(0).get(0));
    assertEquals("Label5", shownTypes.get("CalcId1").get(0).get(1));

    // CalcId2 / Label2, Label3 (only) should be 'hidden'
    assertEquals(1, hiddenTypes.size());
    assertEquals(1, hiddenTypes.get("CalcId2").size());
    assertEquals(2, hiddenTypes.get("CalcId2").get(0).size());
    assertEquals("Label2", hiddenTypes.get("CalcId2").get(0).get(0));
    assertEquals("Label3", hiddenTypes.get("CalcId2").get(0).get(1));
  }

  /**
   * Test method that determines visible graph groups.
   */
  @Test(groups = { "Functional" })
  public void testGetVisibleGraphGroups()
  {
    AlignmentAnnotation[] anns = alignment.getAlignmentAnnotation();
    /*
     * a bar graph group is not included
     */
    anns[0].graph = AlignmentAnnotation.BAR_GRAPH;
    anns[0].graphGroup = 1;
    anns[0].visible = true;

    /*
     * a line graph group is included as long as one of its members is visible
     */
    anns[1].graph = AlignmentAnnotation.LINE_GRAPH;
    anns[1].graphGroup = 5;
    anns[1].visible = false;
    anns[2].graph = AlignmentAnnotation.LINE_GRAPH;
    anns[2].graphGroup = 5;
    anns[2].visible = true;

    /*
     * a line graph group with no visible rows is not included
     */
    anns[3].graph = AlignmentAnnotation.LINE_GRAPH;
    anns[3].graphGroup = 3;
    anns[3].visible = false;

    // a visible line graph with no graph group is not included
    anns[4].graph = AlignmentAnnotation.LINE_GRAPH;
    anns[4].graphGroup = -1;
    anns[4].visible = true;

    BitSet result = AlignmentAnnotationUtils.getVisibleLineGraphGroups(
            AlignmentAnnotationUtils.asList(anns));
    assertTrue(result.get(5));
    assertFalse(result.get(0));
    assertFalse(result.get(1));
    assertFalse(result.get(2));
    assertFalse(result.get(3));
  }

  /**
   * Test for case where no sequence is selected. Shouldn't normally arise but
   * check it handles it gracefully.
   */
  @Test(groups = { "Functional" })
  public void testGetShownHiddenTypes_noSequenceSelected()
  {
    Map<String, List<List<String>>> shownTypes = new HashMap<String, List<List<String>>>();
    Map<String, List<List<String>>> hiddenTypes = new HashMap<String, List<List<String>>>();
    AlignmentAnnotation[] anns = alignment.getAlignmentAnnotation();
    // selected sequences null
    AlignmentAnnotationUtils.getShownHiddenTypes(shownTypes, hiddenTypes,
            AlignmentAnnotationUtils.asList(anns), null);
    assertTrue(shownTypes.isEmpty());
    assertTrue(hiddenTypes.isEmpty());

    List<SequenceI> sequences = new ArrayList<SequenceI>();
    // selected sequences empty list
    AlignmentAnnotationUtils.getShownHiddenTypes(shownTypes, hiddenTypes,
            AlignmentAnnotationUtils.asList(anns), sequences);
    assertTrue(shownTypes.isEmpty());
    assertTrue(hiddenTypes.isEmpty());
  }
}
