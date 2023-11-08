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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import jalview.viewmodel.annotationfilter.AnnotationFilterParameter;
import jalview.viewmodel.annotationfilter.AnnotationFilterParameter.SearchableAnnotationField;

/**
 * Data class holding the selected columns and hidden column ranges for a view.
 * Ranges are base 1.
 */
public class ColumnSelection
{
  /**
   * A class to hold an efficient representation of selected columns
   */
  private class IntList
  {
    /*
     * list of selected columns (ordered by selection order, not column order)
     */
    private List<Integer> order;

    /*
     * an unmodifiable view of the selected columns list
     */
    private List<Integer> _uorder;

    /**
     * bitfield for column selection - allows quick lookup
     */
    private BitSet selected;

    /**
     * Constructor
     */
    IntList()
    {
      order = new ArrayList<>();
      _uorder = Collections.unmodifiableList(order);
      selected = new BitSet();
    }

    /**
     * Copy constructor
     * 
     * @param other
     */
    IntList(IntList other)
    {
      this();
      if (other != null)
      {
        int j = other.size();
        for (int i = 0; i < j; i++)
        {
          add(other.elementAt(i));
        }
      }
    }

    /**
     * adds a new column i to the selection - only if i is not already selected
     * 
     * @param i
     */
    void add(int i)
    {
      if (!selected.get(i))
      {
        order.add(Integer.valueOf(i));
        selected.set(i);
      }
    }

    void clear()
    {
      order.clear();
      selected.clear();
    }

    void remove(int col)
    {

      Integer colInt = Integer.valueOf(col);

      if (selected.get(col))
      {
        // if this ever changes to List.remove(), ensure Integer not int
        // argument
        // as List.remove(int i) removes the i'th item which is wrong
        order.remove(colInt);
        selected.clear(col);
      }
    }

    boolean contains(Integer colInt)
    {
      return selected.get(colInt);
    }

    boolean isEmpty()
    {
      return order.isEmpty();
    }

    /**
     * Returns a read-only view of the selected columns list
     * 
     * @return
     */
    List<Integer> getList()
    {
      return _uorder;
    }

    int size()
    {
      return order.size();
    }

    /**
     * gets the column that was selected first, second or i'th
     * 
     * @param i
     * @return
     */
    int elementAt(int i)
    {
      return order.get(i);
    }

    protected boolean pruneColumnList(final List<int[]> shifts)
    {
      int s = 0, t = shifts.size();
      int[] sr = shifts.get(s++);
      boolean pruned = false;
      int i = 0, j = order.size();
      while (i < j && s <= t)
      {
        int c = order.get(i++).intValue();
        if (sr[0] <= c)
        {
          if (sr[1] + sr[0] >= c)
          { // sr[1] -ve means inseriton.
            order.remove(--i);
            selected.clear(c);
            j--;
          }
          else
          {
            if (s < t)
            {
              sr = shifts.get(s);
            }
            s++;
          }
        }
      }
      return pruned;
    }

    /**
     * shift every selected column at or above start by change
     * 
     * @param start
     *          - leftmost column to be shifted
     * @param change
     *          - delta for shift
     */
    void compensateForEdits(int start, int change)
    {
      BitSet mask = new BitSet();
      for (int i = 0; i < order.size(); i++)
      {
        int temp = order.get(i);

        if (temp >= start)
        {
          // clear shifted bits and update List of selected columns
          selected.clear(temp);
          mask.set(temp - change);
          order.set(i, Integer.valueOf(temp - change));
        }
      }
      // lastly update the bitfield all at once
      selected.or(mask);
    }

    boolean isSelected(int column)
    {
      return selected.get(column);
    }

    int getMaxColumn()
    {
      return selected.length() - 1;
    }

    int getMinColumn()
    {
      return selected.get(0) ? 0 : selected.nextSetBit(0);
    }

    /**
     * @return a series of selection intervals along the range
     */
    List<int[]> getRanges()
    {
      List<int[]> rlist = new ArrayList<>();
      if (selected.isEmpty())
      {
        return rlist;
      }
      int next = selected.nextSetBit(0), clear = -1;
      while (next != -1)
      {
        clear = selected.nextClearBit(next);
        rlist.add(new int[] { next, clear - 1 });
        next = selected.nextSetBit(clear);
      }
      return rlist;
    }

    @Override
    public int hashCode()
    {
      // TODO Auto-generated method stub
      return selected.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
      if (obj instanceof IntList)
      {
        return ((IntList) obj).selected.equals(selected);
      }
      return false;
    }
  }

  private IntList selection = new IntList();

  /**
   * Add a column to the selection
   * 
   * @param col
   *          index of column
   */
  public void addElement(int col)
  {
    selection.add(col);
  }

  /**
   * add a series of start,end (inclusive) ranges to the column selection
   * 
   * @param rng
   *          [start_0, end_0, start_1, end_1, ... ]
   * @param baseOne
   *          - when true, ranges are base 1 and will be mapped to base 0
   */
  public void addRangeOfElements(int[] rng, boolean baseOne)
  {
    int base = baseOne ? -1 : 0;
    for (int c = 0; c < rng.length; c += 2)
    {
      for (int p = rng[c]; p <= rng[c + 1]; p++)
      {
        selection.add(base + p);
      }
    }

  }

  /**
   * clears column selection
   */
  public void clear()
  {
    selection.clear();
  }

  /**
   * Removes value 'col' from the selection (not the col'th item)
   * 
   * @param col
   *          index of column to be removed
   */
  public void removeElement(int col)
  {
    selection.remove(col);
  }

  /**
   * removes a range of columns from the selection
   * 
   * @param start
   *          int - first column in range to be removed
   * @param end
   *          int - last col
   */
  public void removeElements(int start, int end)
  {
    Integer colInt;
    for (int i = start; i < end; i++)
    {
      colInt = Integer.valueOf(i);
      if (selection.contains(colInt))
      {
        selection.remove(colInt);
      }
    }
  }

  /**
   * Returns a read-only view of the (possibly empty) list of selected columns
   * <p>
   * The list contains no duplicates but is not necessarily ordered. It also may
   * include columns hidden from the current view. To modify (for example sort)
   * the list, you should first make a copy.
   * <p>
   * The list is not thread-safe: iterating over it could result in
   * ConcurrentModificationException if it is modified by another thread.
   */
  public List<Integer> getSelected()
  {
    return selection.getList();
  }

  /**
   * @return list of int arrays containing start and end column position for
   *         runs of selected columns ordered from right to left.
   */
  public List<int[]> getSelectedRanges()
  {
    return selection.getRanges();
  }

  /**
   * 
   * @param col
   *          index to search for in column selection
   * 
   * @return true if col is selected
   */
  public boolean contains(int col)
  {
    return (col > -1) ? selection.isSelected(col) : false;
  }

  /**
   * 
   */
  public boolean intersects(int from, int to)
  {
    // TODO: do this in a more efficient bitwise way
    for (int f = from; f <= to; f++)
    {
      if (selection.isSelected(f))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Answers true if no columns are selected, else false
   */
  public boolean isEmpty()
  {
    return selection == null || selection.isEmpty();
  }

  /**
   * rightmost selected column
   * 
   * @return rightmost column in alignment that is selected
   */
  public int getMax()
  {
    if (selection.isEmpty())
    {
      return -1;
    }
    return selection.getMaxColumn();
  }

  /**
   * Leftmost column in selection
   * 
   * @return column index of leftmost column in selection
   */
  public int getMin()
  {
    if (selection.isEmpty())
    {
      return 1000000000;
    }
    return selection.getMinColumn();
  }

  public void hideSelectedColumns(AlignmentI al)
  {
    synchronized (selection)
    {
      for (int[] selregions : selection.getRanges())
      {
        al.getHiddenColumns().hideColumns(selregions[0], selregions[1]);
      }
      selection.clear();
    }

  }

  /**
   * Hides the specified column and any adjacent selected columns
   * 
   * @param res
   *          int
   */
  public void hideSelectedColumns(int col, HiddenColumns hidden)
  {
    /*
     * deselect column (whether selected or not!)
     */
    removeElement(col);

    /*
     * find adjacent selected columns
     */
    int min = col - 1, max = col + 1;
    while (contains(min))
    {
      removeElement(min);
      min--;
    }

    while (contains(max))
    {
      removeElement(max);
      max++;
    }

    /*
     * min, max are now the closest unselected columns
     */
    min++;
    max--;
    if (min > max)
    {
      min = max;
    }

    hidden.hideColumns(min, max);
  }

  /**
   * Copy constructor
   * 
   * @param copy
   */
  public ColumnSelection(ColumnSelection copy)
  {
    if (copy != null)
    {
      selection = new IntList(copy.selection);
    }
  }

  /**
   * ColumnSelection
   */
  public ColumnSelection()
  {
  }

  /**
   * Invert the column selection from first to end-1. leaves hiddenColumns
   * untouched (and unselected)
   * 
   * @param first
   * @param end
   */
  public void invertColumnSelection(int first, int width, AlignmentI al)
  {
    boolean hasHidden = al.getHiddenColumns().hasHiddenColumns();
    for (int i = first; i < width; i++)
    {
      if (contains(i))
      {
        removeElement(i);
      }
      else
      {
        if (!hasHidden || al.getHiddenColumns().isVisible(i))
        {
          addElement(i);
        }
      }
    }
  }

  /**
   * set the selected columns to the given column selection, excluding any
   * columns that are hidden.
   * 
   * @param colsel
   */
  public void setElementsFrom(ColumnSelection colsel,
          HiddenColumns hiddenColumns)
  {
    selection = new IntList();
    if (colsel.selection != null && colsel.selection.size() > 0)
    {
      if (hiddenColumns.hasHiddenColumns())
      {
        // only select visible columns in this columns selection
        for (Integer col : colsel.getSelected())
        {
          if (hiddenColumns != null
                  && hiddenColumns.isVisible(col.intValue()))
          {
            selection.add(col);
          }
        }
      }
      else
      {
        // add everything regardless
        for (Integer col : colsel.getSelected())
        {
          addElement(col);
        }
      }
    }
  }

  /**
   * 
   * @return true if there are columns marked
   */
  public boolean hasSelectedColumns()
  {
    return (selection != null && selection.size() > 0);
  }

  /**
   * Selects columns where the given annotation matches the provided filter
   * condition(s). Any existing column selections are first cleared. Answers the
   * number of columns added.
   * 
   * @param annotations
   * @param filterParams
   * @return
   */
  public int filterAnnotations(AlignmentAnnotation ann_row,
          AnnotationFilterParameter filterParams)
  {
    Annotation[] annotations = ann_row.annotations;
    // JBPNote - this method needs to be refactored to become independent of
    // viewmodel package
    this.clear();

    if (ann_row.graph == AlignmentAnnotation.CONTACT_MAP && (filterParams
            .getThresholdType() == AnnotationFilterParameter.ThresholdType.ABOVE_THRESHOLD
            || filterParams
                    .getThresholdType() == AnnotationFilterParameter.ThresholdType.BELOW_THRESHOLD))
    {
      float tVal = filterParams.getThresholdValue();
      if (ann_row.sequenceRef != null)
      {
        // TODO - get ContactList from AlignmentView for non-seq-ref associatd
        for (int column = 0; column < annotations.length; column++)
        {
          if (ann_row.annotations[column] == null)
          {
            continue;
          }

          int cpos = ann_row.sequenceRef.findPosition(column) - 1;
          ContactListI clist = ann_row.sequenceRef
                  .getContactListFor(ann_row, cpos);
          for (int row = column + 8, rowEnd = clist
                  .getContactHeight(); row < rowEnd; row++)
          {
            if (filterParams
                    .getThresholdType() == AnnotationFilterParameter.ThresholdType.ABOVE_THRESHOLD
                            ? (clist.getContactAt(row) > tVal)
                            : (clist.getContactAt(row) < tVal))
            {
              addElement(column);
              break;
              // int column_forrowpos = ann_row.sequenceRef.findIndex(row + 1);
              // addElement(column_forrowpos);
            }
          }
        }
      }
      return selection.size();
    }

    int addedCount = 0;
    int column = 0;
    do
    {
      Annotation ann = annotations[column];
      if (ann != null)
      {
        float value = ann.value;
        boolean matched = false;

        /*
         * filter may have multiple conditions - 
         * these are or'd until a match is found
         */
        if (filterParams
                .getThresholdType() == AnnotationFilterParameter.ThresholdType.ABOVE_THRESHOLD
                && value > filterParams.getThresholdValue())
        {
          matched = true;
        }

        if (!matched && filterParams
                .getThresholdType() == AnnotationFilterParameter.ThresholdType.BELOW_THRESHOLD
                && value < filterParams.getThresholdValue())
        {
          matched = true;
        }

        if (!matched && filterParams.isFilterAlphaHelix()
                && ann.secondaryStructure == 'H')
        {
          matched = true;
        }

        if (!matched && filterParams.isFilterBetaSheet()
                && ann.secondaryStructure == 'E')
        {
          matched = true;
        }

        if (!matched && filterParams.isFilterTurn()
                && ann.secondaryStructure == 'S')
        {
          matched = true;
        }

        String regexSearchString = filterParams.getRegexString();
        if (!matched && regexSearchString != null)
        {
          List<SearchableAnnotationField> fields = filterParams
                  .getRegexSearchFields();
          for (SearchableAnnotationField field : fields)
          {
            String compareTo = field == SearchableAnnotationField.DISPLAY_STRING
                    ? ann.displayCharacter // match 'Label'
                    : ann.description; // and/or 'Description'
            if (compareTo != null)
            {
              try
              {
                if (compareTo.matches(regexSearchString))
                {
                  matched = true;
                }
              } catch (PatternSyntaxException pse)
              {
                if (compareTo.equals(regexSearchString))
                {
                  matched = true;
                }
              }
              if (matched)
              {
                break;
              }
            }
          }
        }

        if (matched)
        {
          this.addElement(column);
          addedCount++;
        }
      }
      column++;
    } while (column < annotations.length);

    return addedCount;
  }

  /**
   * Returns a hashCode built from selected columns ranges
   */
  @Override
  public int hashCode()
  {
    return selection.hashCode();
  }

  /**
   * Answers true if comparing to a ColumnSelection with the same selected
   * columns and hidden columns, else false
   */
  @Override
  public boolean equals(Object obj)
  {
    if (!(obj instanceof ColumnSelection))
    {
      return false;
    }
    ColumnSelection that = (ColumnSelection) obj;

    /*
     * check columns selected are either both null, or match
     */
    if (this.selection == null)
    {
      if (that.selection != null)
      {
        return false;
      }
    }
    if (!this.selection.equals(that.selection))
    {
      return false;
    }

    return true;
  }

  /**
   * Updates the column selection depending on the parameters, and returns true
   * if any change was made to the selection
   * 
   * @param markedColumns
   *          a set identifying marked columns (base 0)
   * @param startCol
   *          the first column of the range to operate over (base 0)
   * @param endCol
   *          the last column of the range to operate over (base 0)
   * @param invert
   *          if true, deselect marked columns and select unmarked
   * @param extendCurrent
   *          if true, extend rather than replacing the current column selection
   * @param toggle
   *          if true, toggle the selection state of marked columns
   * 
   * @return
   */
  public boolean markColumns(BitSet markedColumns, int startCol, int endCol,
          boolean invert, boolean extendCurrent, boolean toggle)
  {
    boolean changed = false;
    if (!extendCurrent && !toggle)
    {
      changed = !this.isEmpty();
      clear();
    }
    if (invert)
    {
      // invert only in the currently selected sequence region
      int i = markedColumns.nextClearBit(startCol);
      int ibs = markedColumns.nextSetBit(startCol);
      while (i >= startCol && i <= endCol)
      {
        if (ibs < 0 || i < ibs)
        {
          changed = true;
          if (toggle && contains(i))
          {
            removeElement(i++);
          }
          else
          {
            addElement(i++);
          }
        }
        else
        {
          i = markedColumns.nextClearBit(ibs);
          ibs = markedColumns.nextSetBit(i);
        }
      }
    }
    else
    {
      int i = markedColumns.nextSetBit(startCol);
      while (i >= startCol && i <= endCol)
      {
        changed = true;
        if (toggle && contains(i))
        {
          removeElement(i);
        }
        else
        {
          addElement(i);
        }
        i = markedColumns.nextSetBit(i + 1);
      }
    }
    return changed;
  }

  /**
   * Adjusts column selections, and the given selection group, to match the
   * range of a stretch (e.g. mouse drag) operation
   * <p>
   * Method refactored from ScalePanel.mouseDragged
   * 
   * @param res
   *          current column position, adjusted for hidden columns
   * @param sg
   *          current selection group
   * @param min
   *          start position of the stretch group
   * @param max
   *          end position of the stretch group
   */
  public void stretchGroup(int res, SequenceGroup sg, int min, int max)
  {
    if (!contains(res))
    {
      addElement(res);
    }

    if (res > sg.getStartRes())
    {
      // expand selection group to the right
      sg.setEndRes(res);
    }
    if (res < sg.getStartRes())
    {
      // expand selection group to the left
      sg.setStartRes(res);
    }

    /*
     * expand or shrink column selection to match the
     * range of the drag operation
     */
    for (int col = min; col <= max; col++)
    {
      if (col < sg.getStartRes() || col > sg.getEndRes())
      {
        // shrinking drag - remove from selection
        removeElement(col);
      }
      else
      {
        // expanding drag - add to selection
        addElement(col);
      }
    }
  }
}
