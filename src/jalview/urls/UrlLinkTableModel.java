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

package jalview.urls;

import jalview.bin.Console;
import jalview.urls.api.UrlProviderI;
import jalview.util.UrlLink;

import java.util.Iterator;
import java.util.List;

import javax.swing.RowFilter.Entry;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * TableModel for UrlLinks table
 * 
 * @author $author$
 * @version $Revision$
 */

public class UrlLinkTableModel extends AbstractTableModel
{
  // local storage of data
  private List<UrlLinkDisplay> data;

  // supplier of url data
  private UrlProviderI dataProvider;

  // list of columns to display in table in correct order
  private List<String> displayColumns;

  // row in table which is currently the primary
  private int primaryRow;

  /**
   * UrlLinkTableModel constructor
   * 
   * @param baseData
   *          base data set to be presented in table
   * @param entryNames
   *          keys of entries in baseData's nested hashmap. Should match order
   *          in displayColNames
   * @param displayColNames
   *          names of columns to display in order.
   * @param keyColName
   *          name of column corresponding to keys in baseData
   */
  public UrlLinkTableModel(UrlProviderI baseData)
  {
    dataProvider = baseData;
    data = baseData.getLinksForTable();
    displayColumns = UrlLinkDisplay.getDisplayColumnNames();

    // find the primary row
    primaryRow = 0;
    Iterator<UrlLinkDisplay> it = data.iterator();
    while (it.hasNext())
    {
      if (it.next().getIsPrimary())
      {
        break;
      }
      else
      {
        primaryRow++;
      }
    }

    // set up listener which updates data source when table changes
    this.addTableModelListener(new TableModelListener()
    {
      @Override
      public void tableChanged(TableModelEvent e)
      {
        try
        {
          // update the UrlProvider from data list
          dataProvider.setUrlData(data);
        } catch (IllegalArgumentException ex)
        {
          Console.error(ex.getMessage());
        }
      }
    });

  }

  @Override
  public int getRowCount()
  {
    if (data == null)
    {
      return 0;
    }
    else
    {
      return data.size();
    }
  }

  @Override
  public int getColumnCount()
  {
    return displayColumns.size();
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex)
  {
    return data.get(rowIndex).getValue(columnIndex);
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    return data.get(rowIndex).isEditable(columnIndex);
  }

  /**
   * Determine if a row is editable indirectly (rather than directly in table as
   * in isCellEditable)
   * 
   * @param rowIndex
   * @return true if row can be edited indirectly
   */
  public boolean isRowEditable(int rowIndex)
  {
    // to edit, row must be a user entered row
    return (dataProvider.isUserEntry(data.get(rowIndex).getId()));
  }

  /**
   * Determine if a row is deletable
   * 
   * @param rowIndex
   *          the row to be tested
   * @return true if row can be deleted
   */
  public boolean isRowDeletable(int rowIndex)
  {
    // to delete, row must be a user entered row, and not the default row
    return (dataProvider.isUserEntry(data.get(rowIndex).getId())
            && !data.get(rowIndex).getIsPrimary());
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex)
  {
    if (columnIndex == UrlLinkDisplay.PRIMARY)
    {
      // Default url column: exactly one row must always be true
      if (rowIndex != primaryRow)
      {
        // selected row is not currently the default
        // set the current default to false
        data.get(primaryRow).setValue(columnIndex, false);
        fireTableRowsUpdated(primaryRow, primaryRow);

        // set the default to be the selected row
        primaryRow = rowIndex;
        data.get(rowIndex).setValue(columnIndex, aValue);

        fireTableRowsUpdated(rowIndex, rowIndex);
      }
    }
    else
    {
      data.get(rowIndex).setValue(columnIndex, aValue);
      fireTableRowsUpdated(rowIndex, rowIndex);
    }
  }

  @Override
  public Class<?> getColumnClass(int columnIndex)
  {
    return getValueAt(0, columnIndex).getClass();
  }

  @Override
  public String getColumnName(int columnIndex)
  {
    return displayColumns.get(columnIndex);
  }

  public void removeRow(int rowIndex)
  {
    // remove the row from data
    data.remove(rowIndex);

    // update default row
    if (primaryRow > rowIndex)
    {
      primaryRow--;
    }

    // fire update which will update data source
    fireTableRowsDeleted(rowIndex, rowIndex);
  }

  public int insertRow(String name, String url)
  {
    // add a row to the data
    UrlLink link = new UrlLink(name, url, name);
    UrlLinkDisplay u = new UrlLinkDisplay(name, link, true, false);
    int index = data.size();
    data.add(u);

    // fire update which will update data source
    fireTableRowsInserted(index, index);
    return index;
  }

  public int getPrimaryColumn()
  {
    return UrlLinkDisplay.PRIMARY;
  }

  public int getNameColumn()
  {
    return UrlLinkDisplay.NAME;
  }

  public int getDatabaseColumn()
  {
    return UrlLinkDisplay.DATABASE;
  }

  public int getIdColumn()
  {
    return UrlLinkDisplay.ID;
  }

  public int getUrlColumn()
  {
    return UrlLinkDisplay.URL;
  }

  public int getSelectedColumn()
  {
    return UrlLinkDisplay.SELECTED;
  }

  public boolean isUserEntry(
          Entry<? extends TableModel, ? extends Object> entry)
  {
    return dataProvider
            .isUserEntry(entry.getStringValue(UrlLinkDisplay.ID));
  }

  public boolean isUniqueName(String name)
  {
    return !dataProvider.contains(name);
  }
}
