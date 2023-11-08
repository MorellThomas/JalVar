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
package jalview.datamodel;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.analysis.AlignmentGenerator;
import jalview.gui.JvOptionPane;
import jalview.viewmodel.annotationfilter.AnnotationFilterParameter;
import jalview.viewmodel.annotationfilter.AnnotationFilterParameter.SearchableAnnotationField;
import jalview.viewmodel.annotationfilter.AnnotationFilterParameter.ThresholdType;

public class ColumnSelectionTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testAddElement()
  {
    ColumnSelection cs = new ColumnSelection();
    cs.addElement(2);
    cs.addElement(5);
    cs.addElement(3);
    cs.addElement(5); // ignored
    List<Integer> sel = cs.getSelected();
    assertEquals("[2, 5, 3]", sel.toString());
  }

  @Test(groups = { "Functional" })
  public void testSetElementsFrom()
  {
    ColumnSelection fromcs = new ColumnSelection();
    ColumnSelection tocs = new ColumnSelection();
    HiddenColumns hidden = new HiddenColumns();

    fromcs.addElement(2);
    fromcs.addElement(3);
    fromcs.addElement(5);

    tocs.setElementsFrom(fromcs, hidden);
    assertTrue(tocs.equals(fromcs));

    hidden.hideColumns(4, 6);
    tocs.setElementsFrom(fromcs, hidden);

    // expect cols 2 and 3 to be selected but not 5
    ColumnSelection expectcs = new ColumnSelection();
    expectcs.addElement(2);
    expectcs.addElement(3);
    assertTrue(tocs.equals(expectcs));
  }

  /**
   * Test the remove method - in particular to verify that remove(int i) removes
   * the element whose value is i, _NOT_ the i'th element.
   */
  @Test(groups = { "Functional" })
  public void testRemoveElement()
  {
    ColumnSelection cs = new ColumnSelection();
    cs.addElement(2);
    cs.addElement(5);

    // removing elements not in the list has no effect
    cs.removeElement(0);
    cs.removeElement(1);
    List<Integer> sel = cs.getSelected();
    assertEquals(2, sel.size());
    assertEquals(Integer.valueOf(2), sel.get(0));
    assertEquals(Integer.valueOf(5), sel.get(1));

    // removing an element in the list removes it
    cs.removeElement(2);
    // ...and also from the read-only view
    assertEquals(1, sel.size());
    sel = cs.getSelected();
    assertEquals(1, sel.size());
    assertEquals(Integer.valueOf(5), sel.get(0));
  }

  /**
   * Test the method that hides a specified column including any adjacent
   * selected columns. This is a convenience method for the case where multiple
   * column regions are selected and then hidden using menu option View | Hide |
   * Selected Columns.
   */
  @Test(groups = { "Functional" })
  public void testHideColumns_withSelection()
  {
    // create random alignment
    AlignmentGenerator gen = new AlignmentGenerator(false);
    AlignmentI al = gen.generate(50, 20, 123, 5, 5);

    ColumnSelection cs = new ColumnSelection();
    // select columns 4-6
    cs.addElement(4);
    cs.addElement(5);
    cs.addElement(6);
    // hide column 5 (and adjacent):
    cs.hideSelectedColumns(5, al.getHiddenColumns());
    // 4,5,6 now hidden:
    Iterator<int[]> regions = al.getHiddenColumns().iterator();
    assertEquals(1, al.getHiddenColumns().getNumberOfRegions());
    assertEquals("[4, 6]", Arrays.toString(regions.next()));
    // none now selected:
    assertTrue(cs.getSelected().isEmpty());

    // repeat, hiding column 4 (5 and 6)
    al = gen.generate(50, 20, 123, 5, 5);
    cs = new ColumnSelection();
    cs.addElement(4);
    cs.addElement(5);
    cs.addElement(6);
    cs.hideSelectedColumns(4, al.getHiddenColumns());
    regions = al.getHiddenColumns().iterator();
    assertEquals(1, al.getHiddenColumns().getNumberOfRegions());
    assertEquals("[4, 6]", Arrays.toString(regions.next()));
    assertTrue(cs.getSelected().isEmpty());

    // repeat, hiding column (4, 5 and) 6
    al = gen.generate(50, 20, 123, 5, 5);
    cs = new ColumnSelection();
    cs.addElement(4);
    cs.addElement(5);
    cs.addElement(6);
    cs.hideSelectedColumns(6, al.getHiddenColumns());
    regions = al.getHiddenColumns().iterator();
    assertEquals(1, al.getHiddenColumns().getNumberOfRegions());
    assertEquals("[4, 6]", Arrays.toString(regions.next()));
    assertTrue(cs.getSelected().isEmpty());

    // repeat, with _only_ adjacent columns selected
    al = gen.generate(50, 20, 123, 5, 5);
    cs = new ColumnSelection();
    cs.addElement(4);
    cs.addElement(6);
    cs.hideSelectedColumns(5, al.getHiddenColumns());
    regions = al.getHiddenColumns().iterator();
    assertEquals(1, al.getHiddenColumns().getNumberOfRegions());
    assertEquals("[4, 6]", Arrays.toString(regions.next()));
    assertTrue(cs.getSelected().isEmpty());
  }

  /**
   * Test the method that hides all (possibly disjoint) selected column ranges
   */
  @Test(groups = { "Functional" })
  public void testHideSelectedColumns()
  {
    // create random alignment
    AlignmentGenerator gen = new AlignmentGenerator(false);
    AlignmentI al = gen.generate(50, 20, 123, 5, 5);

    ColumnSelection cs = new ColumnSelection();
    int[] sel = { 2, 3, 4, 7, 8, 9, 20, 21, 22 };
    for (int col : sel)
    {
      cs.addElement(col);
    }

    HiddenColumns cols = al.getHiddenColumns();
    cols.hideColumns(15, 18);

    cs.hideSelectedColumns(al);
    assertTrue(cs.getSelected().isEmpty());
    Iterator<int[]> regions = cols.iterator();
    assertEquals(4, cols.getNumberOfRegions());
    assertEquals("[2, 4]", Arrays.toString(regions.next()));
    assertEquals("[7, 9]", Arrays.toString(regions.next()));
    assertEquals("[15, 18]", Arrays.toString(regions.next()));
    assertEquals("[20, 22]", Arrays.toString(regions.next()));
  }

  /**
   * Test the method that gets runs of selected columns ordered by column. If
   * this fails, HideSelectedColumns may also fail
   */
  @Test(groups = { "Functional" })
  public void testGetSelectedRanges()
  {
    /*
     * getSelectedRanges returns ordered columns regardless
     * of the order in which they are added
     */
    ColumnSelection cs = new ColumnSelection();
    int[] sel = { 4, 3, 7, 21, 9, 20, 8, 22, 2 };
    for (int col : sel)
    {
      cs.addElement(col);
    }
    List<int[]> range;
    range = cs.getSelectedRanges();
    assertEquals(3, range.size());
    assertEquals("[2, 4]", Arrays.toString(range.get(0)));
    assertEquals("[7, 9]", Arrays.toString(range.get(1)));
    assertEquals("[20, 22]", Arrays.toString(range.get(2)));
    cs.addElement(0);
    cs.addElement(1);
    range = cs.getSelectedRanges();
    assertEquals(3, range.size());
    assertEquals("[0, 4]", Arrays.toString(range.get(0)));
  }

  @Test(groups = { "Functional" })
  public void testInvertColumnSelection()
  {
    // create random alignment
    AlignmentGenerator gen = new AlignmentGenerator(false);
    AlignmentI al = gen.generate(50, 20, 123, 5, 5);

    ColumnSelection cs = new ColumnSelection();
    cs.addElement(4);
    cs.addElement(6);
    cs.addElement(8);

    HiddenColumns cols = al.getHiddenColumns();
    cols.hideColumns(3, 3);
    cols.hideColumns(6, 6);

    // invert selection from start (inclusive) to end (exclusive)
    cs.invertColumnSelection(2, 9, al);
    assertEquals("[2, 5, 7]", cs.getSelected().toString());

    cs.invertColumnSelection(1, 9, al);
    assertEquals("[1, 4, 8]", cs.getSelected().toString());
  }

  @Test(groups = { "Functional" })
  public void testMaxColumnSelection()
  {
    ColumnSelection cs = new ColumnSelection();
    cs.addElement(0);
    cs.addElement(513);
    cs.addElement(1);
    assertEquals(513, cs.getMax());
    cs.removeElement(513);
    assertEquals(1, cs.getMax());
    cs.removeElement(1);
    assertEquals(0, cs.getMax());
    cs.addElement(512);
    cs.addElement(513);
    assertEquals(513, cs.getMax());

  }

  @Test(groups = { "Functional" })
  public void testMinColumnSelection()
  {
    ColumnSelection cs = new ColumnSelection();
    cs.addElement(0);
    cs.addElement(513);
    cs.addElement(1);
    assertEquals(0, cs.getMin());
    cs.removeElement(0);
    assertEquals(1, cs.getMin());
    cs.addElement(0);
    assertEquals(0, cs.getMin());
  }

  @Test(groups = { "Functional" })
  public void testEquals()
  {
    ColumnSelection cs = new ColumnSelection();
    cs.addElement(0);
    cs.addElement(513);
    cs.addElement(1);

    // same selections added in a different order
    ColumnSelection cs2 = new ColumnSelection();
    cs2.addElement(1);
    cs2.addElement(513);
    cs2.addElement(0);

    assertTrue(cs.equals(cs2));
    assertTrue(cs.equals(cs));
    assertTrue(cs2.equals(cs));
    assertTrue(cs2.equals(cs2));

    cs2.addElement(12);
    assertFalse(cs.equals(cs2));
    assertFalse(cs2.equals(cs));

    cs2.removeElement(12);
    assertTrue(cs.equals(cs2));
  }

  /*
      cs2.hideSelectedColumns(88);
      assertFalse(cs.equals(cs2));
      /*
       * unhiding a column adds it to selection!
       */
  /*    cs2.revealHiddenColumns(88);
      assertFalse(cs.equals(cs2));
      cs.addElement(88);
      assertTrue(cs.equals(cs2));
    */

  /**
   * Test the method that returns selected columns, in the order in which they
   * were added
   */
  @Test(groups = { "Functional" })
  public void testGetSelected()
  {
    ColumnSelection cs = new ColumnSelection();
    int[] sel = { 4, 3, 7, 21 };
    for (int col : sel)
    {
      cs.addElement(col);
    }

    List<Integer> selected = cs.getSelected();
    assertEquals(4, selected.size());
    assertEquals("[4, 3, 7, 21]", selected.toString());

    /*
     * getSelected returns a read-only view of the list
     * verify the view follows any changes in it
     */
    cs.removeElement(7);
    cs.addElement(1);
    cs.removeElement(4);
    assertEquals("[3, 21, 1]", selected.toString());
  }

  /**
   * Test to verify that the list returned by getSelection cannot be modified
   */
  @Test(groups = { "Functional" })
  public void testGetSelected_isReadOnly()
  {
    ColumnSelection cs = new ColumnSelection();
    cs.addElement(3);

    List<Integer> selected = cs.getSelected();
    try
    {
      selected.clear();
      fail("expected exception");
    } catch (UnsupportedOperationException e)
    {
      // expected
    }
    try
    {
      selected.add(1);
      fail("expected exception");
    } catch (UnsupportedOperationException e)
    {
      // expected
    }
    try
    {
      selected.remove(3);
      fail("expected exception");
    } catch (UnsupportedOperationException e)
    {
      // expected
    }
    try
    {
      Collections.sort(selected);
      fail("expected exception");
    } catch (UnsupportedOperationException e)
    {
      // expected
    }
  }

  /**
   * Test that demonstrates a ConcurrentModificationException is thrown if you
   * change the selection while iterating over it
   */
  @Test(
    groups = "Functional",
    expectedExceptions =
    { ConcurrentModificationException.class })
  public void testGetSelected_concurrentModification()
  {
    ColumnSelection cs = new ColumnSelection();
    cs.addElement(0);
    cs.addElement(1);
    cs.addElement(2);

    /*
     * simulate changing the list under us (e.g. in a separate
     * thread) while iterating over it -> ConcurrentModificationException
     */
    List<Integer> selected = cs.getSelected();
    for (Integer col : selected)
    {
      if (col.intValue() == 0)
      {
        cs.removeElement(1);
      }
    }
  }

  @Test(groups = "Functional")
  public void testMarkColumns()
  {
    ColumnSelection cs = new ColumnSelection();
    cs.addElement(5); // this will be cleared
    BitSet toMark = new BitSet();
    toMark.set(1);
    toMark.set(3);
    toMark.set(6);
    toMark.set(9);

    assertTrue(cs.markColumns(toMark, 3, 8, false, false, false));
    List<Integer> selected = cs.getSelected();
    assertEquals(2, selected.size());
    assertTrue(selected.contains(3));
    assertTrue(selected.contains(6));
  }

  @Test(groups = "Functional")
  public void testMarkColumns_extend()
  {
    ColumnSelection cs = new ColumnSelection();
    cs.addElement(1);
    cs.addElement(5);
    BitSet toMark = new BitSet();
    toMark.set(1);
    toMark.set(3);
    toMark.set(6);
    toMark.set(9);

    /*
     * extending selection of {3, 6} should leave {1, 3, 5, 6} selected
     */
    assertTrue(cs.markColumns(toMark, 3, 8, false, true, false));
    List<Integer> selected = cs.getSelected();
    assertEquals(4, selected.size());
    assertTrue(selected.contains(1));
    assertTrue(selected.contains(3));
    assertTrue(selected.contains(5));
    assertTrue(selected.contains(6));
  }

  @Test(groups = "Functional")
  public void testMarkColumns_invert()
  {
    ColumnSelection cs = new ColumnSelection();
    cs.addElement(5); // this will be cleared
    BitSet toMark = new BitSet();
    toMark.set(1);
    toMark.set(3);
    toMark.set(6);
    toMark.set(9);

    /*
     * inverted selection of {3, 6} should select {4, 5, 7, 8}
     */
    assertTrue(cs.markColumns(toMark, 3, 8, true, false, false));
    List<Integer> selected = cs.getSelected();
    assertEquals(4, selected.size());
    assertTrue(selected.contains(4));
    assertTrue(selected.contains(5));
    assertTrue(selected.contains(7));
    assertTrue(selected.contains(8));
  }

  @Test(groups = "Functional")
  public void testMarkColumns_toggle()
  {
    ColumnSelection cs = new ColumnSelection();
    cs.addElement(1); // outside change range
    cs.addElement(3);
    cs.addElement(4);
    cs.addElement(10); // outside change range
    BitSet toMark = new BitSet();
    toMark.set(1);
    toMark.set(3);
    toMark.set(6);
    toMark.set(9);

    /*
     * toggling state of {3, 6} should leave {1, 4, 6, 10} selected
     */
    assertTrue(cs.markColumns(toMark, 3, 8, false, false, true));
    List<Integer> selected = cs.getSelected();
    assertEquals(4, selected.size());
    assertTrue(selected.contains(1));
    assertTrue(selected.contains(4));
    assertTrue(selected.contains(6));
    assertTrue(selected.contains(10));
  }

  @Test(groups = "Functional")
  public void testCopyConstructor()
  {
    ColumnSelection cs = new ColumnSelection();
    cs.addElement(3);
    cs.addElement(1);

    ColumnSelection cs2 = new ColumnSelection(cs);
    assertTrue(cs2.hasSelectedColumns());

    // order of column selection is preserved
    assertEquals("[3, 1]", cs2.getSelected().toString());
  }

  @Test(groups = { "Functional" })
  public void testStretchGroup_expand()
  {
    /*
     * test that emulates clicking column 4 (selected)
     * and dragging right to column 5 (all base 0)
     */
    ColumnSelection cs = new ColumnSelection();
    cs.addElement(4);
    SequenceGroup sg = new SequenceGroup();
    sg.setStartRes(4);
    sg.setEndRes(4);
    cs.stretchGroup(5, sg, 4, 4);
    assertEquals(cs.getSelected().size(), 2);
    assertTrue(cs.contains(4));
    assertTrue(cs.contains(5));
    assertEquals(sg.getStartRes(), 4);
    assertEquals(sg.getEndRes(), 5);

    /*
     * emulate drag right with columns 10-20 already selected
     */
    cs.clear();
    for (int i = 10; i <= 20; i++)
    {
      cs.addElement(i);
    }
    assertEquals(cs.getSelected().size(), 11);
    sg = new SequenceGroup();
    sg.setStartRes(10);
    sg.setEndRes(20);
    cs.stretchGroup(21, sg, 10, 20);
    assertEquals(cs.getSelected().size(), 12);
    assertTrue(cs.contains(10));
    assertTrue(cs.contains(21));
    assertEquals(sg.getStartRes(), 10);
    assertEquals(sg.getEndRes(), 21);
  }

  @Test(groups = { "Functional" })
  public void testStretchGroup_shrink()
  {
    /*
     * emulate drag left to 19 with columns 10-20 already selected
     */
    ColumnSelection cs = new ColumnSelection();
    for (int i = 10; i <= 20; i++)
    {
      cs.addElement(i);
    }
    assertEquals(cs.getSelected().size(), 11);
    SequenceGroup sg = new SequenceGroup();
    sg.setStartRes(10);
    sg.setEndRes(20);
    cs.stretchGroup(19, sg, 10, 20);
    assertEquals(cs.getSelected().size(), 10);
    assertTrue(cs.contains(10));
    assertTrue(cs.contains(19));
    assertFalse(cs.contains(20));
    assertEquals(sg.getStartRes(), 10);
    assertEquals(sg.getEndRes(), 19);
  }

  @Test(groups = { "Functional" })
  public void testFilterAnnotations()
  {
    ColumnSelection cs = new ColumnSelection();
    AlignmentAnnotation alann = new AlignmentAnnotation("dummy",
            "dummyDesc", null);

    /*
     * filter with no conditions clears the selection
     */
    Annotation[] anns = new Annotation[] { null };
    AnnotationFilterParameter filter = new AnnotationFilterParameter();
    cs.addElement(3);
    alann.annotations = anns;
    int added = cs.filterAnnotations(alann, filter);
    assertEquals(0, added);
    assertTrue(cs.isEmpty());

    /*
     * select on description (regex)
     */
    filter.setRegexString("w.rld");
    filter.addRegexSearchField(SearchableAnnotationField.DESCRIPTION);
    Annotation helix = new Annotation("(", "hello", '<', 2f);
    Annotation sheet = new Annotation("(", "world", '<', 2f);
    alann.annotations = new Annotation[] { null, helix, sheet };
    added = cs.filterAnnotations(alann, filter);
    assertEquals(1, added);
    assertTrue(cs.contains(2));

    /*
     * select on label (invalid regex, exact match)
     */
    filter = new AnnotationFilterParameter();
    filter.setRegexString("(");
    filter.addRegexSearchField(SearchableAnnotationField.DISPLAY_STRING);
    alann.annotations = new Annotation[] { null, helix, sheet };
    added = cs.filterAnnotations(alann, filter);
    assertEquals(2, added);
    assertTrue(cs.contains(1));
    assertTrue(cs.contains(2));

    /*
     * select Helix (secondary structure symbol H)
     */
    filter = new AnnotationFilterParameter();
    filter.setFilterAlphaHelix(true);
    helix = new Annotation("x", "desc", 'H', 0f);
    sheet = new Annotation("x", "desc", 'E', 1f);
    Annotation turn = new Annotation("x", "desc", 'S', 2f);
    Annotation ann4 = new Annotation("x", "desc", 'Y', 3f);
    alann.annotations = new Annotation[] { null, helix, sheet, turn, ann4 };
    added = cs.filterAnnotations(alann, filter);
    assertEquals(1, added);
    assertTrue(cs.contains(1));

    /*
     * select Helix and Sheet (E)
     */
    filter.setFilterBetaSheet(true);
    alann.annotations = new Annotation[] { null, helix, sheet, turn, ann4 };
    added = cs.filterAnnotations(alann, filter);
    assertEquals(2, added);
    assertTrue(cs.contains(1));
    assertTrue(cs.contains(2));

    /*
     * select Sheet and Turn (S)
     */
    filter.setFilterAlphaHelix(false);
    filter.setFilterTurn(true);
    alann.annotations = new Annotation[] { null, helix, sheet, turn, ann4 };
    added = cs.filterAnnotations(alann, filter);
    assertEquals(2, added);
    assertTrue(cs.contains(2));
    assertTrue(cs.contains(3));

    /*
     * select value < 2f (ann1, ann2)
     */
    filter = new AnnotationFilterParameter();
    filter.setThresholdType(ThresholdType.BELOW_THRESHOLD);
    filter.setThresholdValue(2f);
    alann.annotations = new Annotation[] { null, helix, sheet, turn, ann4 };
    added = cs.filterAnnotations(alann, filter);
    assertEquals(2, added);
    assertTrue(cs.contains(1));
    assertTrue(cs.contains(2));

    /*
     * select value > 2f (ann4 only)
     */
    filter.setThresholdType(ThresholdType.ABOVE_THRESHOLD);
    alann.annotations = new Annotation[] { null, helix, sheet, turn, ann4 };
    added = cs.filterAnnotations(alann, filter);
    assertEquals(1, added);
    assertTrue(cs.contains(4));

    /*
     * select >2f or Helix
     */
    filter.setFilterAlphaHelix(true);
    alann.annotations = new Annotation[] { null, helix, sheet, turn, ann4 };
    added = cs.filterAnnotations(alann, filter);
    assertEquals(2, added);
    assertTrue(cs.contains(1));
    assertTrue(cs.contains(4));

    /*
     * select < 1f or Helix; one annotation matches both
     * return value should only count it once
     */
    filter.setThresholdType(ThresholdType.BELOW_THRESHOLD);
    filter.setThresholdValue(1f);
    alann.annotations = new Annotation[] { null, helix, sheet, turn, ann4 };
    added = cs.filterAnnotations(alann, filter);
    assertEquals(1, added);
    assertTrue(cs.contains(1));
  }
}
