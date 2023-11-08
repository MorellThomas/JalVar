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
package jalview.fts.core;

import jalview.fts.api.FTSDataColumnI;
import jalview.fts.api.FTSDataColumnI.FTSDataColumnGroupI;
import jalview.fts.api.FTSRestClientI;
import jalview.fts.api.StructureFTSRestClientI;
import jalview.fts.service.pdb.PDBFTSRestClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * Helps render GUI allowing control of which columns to show for entries
 * returned from an FTS query. TODO: push down FTSClient specific code
 * 
 * @author tcofoegbu
 *
 */
@SuppressWarnings("serial")
public class FTSDataColumnPreferences extends JScrollPane
{
  protected JTable tbl_FTSDataColumnPrefs = new JTable();

  protected JScrollPane scrl_pdbDocFieldConfig = new JScrollPane(
          tbl_FTSDataColumnPrefs);

  private HashMap<String, FTSDataColumnI> map = new HashMap<String, FTSDataColumnI>();

  private Collection<FTSDataColumnI> structSummaryColumns = new LinkedHashSet<FTSDataColumnI>();

  private Collection<FTSDataColumnI> allFTSDataColumns = new LinkedHashSet<FTSDataColumnI>();

  public enum PreferenceSource
  {
    SEARCH_SUMMARY, STRUCTURE_CHOOSER, PREFERENCES;
  }

  private PreferenceSource currentSource;

  private FTSRestClientI ftsRestClient;

  public FTSDataColumnPreferences(PreferenceSource source,
          FTSRestClientI ftsRestClient)
  {
    this.ftsRestClient = ftsRestClient;
    if (source.equals(PreferenceSource.STRUCTURE_CHOOSER)
            || source.equals(PreferenceSource.PREFERENCES))
    {
      structSummaryColumns = ((StructureFTSRestClientI) ftsRestClient)
              .getAllDefaultDisplayedStructureDataColumns();
    }
    allFTSDataColumns.addAll(ftsRestClient.getAllFTSDataColumns());

    tbl_FTSDataColumnPrefs.setAutoCreateRowSorter(true);
    this.getViewport().add(tbl_FTSDataColumnPrefs);
    this.currentSource = source;

    String[] columnNames = ftsRestClient.getPreferencesColumnsFor(source);

    Object[][] data = new Object[allFTSDataColumns.size()][3];

    int x = 0;
    for (FTSDataColumnI field : allFTSDataColumns)
    {
      // System.out.println("allFTSDataColumns==" + allFTSDataColumns);
      if (field.getName().equalsIgnoreCase("all"))
      {
        continue;
      }

      switch (source)
      {
      case SEARCH_SUMMARY:
        data[x++] = new Object[] { ftsRestClient
                .getAllDefaultDisplayedFTSDataColumns().contains(field),
            field.getName(), field.getGroup() };
        // System.out.println(" PUIS " + field.getName() + " ET AUSSI " +
        // field.getGroup() + "X = " + x);
        break;
      case STRUCTURE_CHOOSER:
        data[x++] = new Object[] { structSummaryColumns.contains(field),
            field.getName(), field.getGroup() };
        break;
      case PREFERENCES:
        data[x++] = new Object[] {
            field.getName(), ftsRestClient
                    .getAllDefaultDisplayedFTSDataColumns().contains(field),
            structSummaryColumns.contains(field) };
        break;
      default:
        break;
      }
      map.put(field.getName(), field);
    }

    FTSDataColumnPrefsTableModel model = new FTSDataColumnPrefsTableModel(
            columnNames, data);
    tbl_FTSDataColumnPrefs.setModel(model);

    switch (source)
    {
    case SEARCH_SUMMARY:
    case STRUCTURE_CHOOSER:
      tbl_FTSDataColumnPrefs.getColumnModel().getColumn(0)
              .setPreferredWidth(30);
      tbl_FTSDataColumnPrefs.getColumnModel().getColumn(0).setMinWidth(20);
      tbl_FTSDataColumnPrefs.getColumnModel().getColumn(0).setMaxWidth(40);
      tbl_FTSDataColumnPrefs.getColumnModel().getColumn(1)
              .setPreferredWidth(150);
      tbl_FTSDataColumnPrefs.getColumnModel().getColumn(1).setMinWidth(150);
      tbl_FTSDataColumnPrefs.getColumnModel().getColumn(2)
              .setPreferredWidth(150);
      tbl_FTSDataColumnPrefs.getColumnModel().getColumn(2).setMinWidth(150);

      TableRowSorter<TableModel> sorter = new TableRowSorter<>(
              tbl_FTSDataColumnPrefs.getModel());
      tbl_FTSDataColumnPrefs.setRowSorter(sorter);
      List<RowSorter.SortKey> sortKeys = new ArrayList<>();
      int columnIndexToSort = 2;
      sortKeys.add(new RowSorter.SortKey(columnIndexToSort,
              SortOrder.ASCENDING));
      sorter.setComparator(columnIndexToSort,
              new Comparator<FTSDataColumnGroupI>()
              {
                @Override
                public int compare(FTSDataColumnGroupI o1,
                        FTSDataColumnGroupI o2)
                {
                  return o1.getSortOrder() - o2.getSortOrder();
                }
              });
      sorter.setSortKeys(sortKeys);
      // BH 2018 setSortKeys does a sort sorter.sort();

      tbl_FTSDataColumnPrefs
              .setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
      break;
    case PREFERENCES:
    default:
      break;
    }

  }

  public Collection<FTSDataColumnI> getStructureSummaryFields()
  {
    return structSummaryColumns;
  }

  class FTSDataColumnPrefsTableModel extends AbstractTableModel
  {

    public FTSDataColumnPrefsTableModel(String[] columnNames,
            Object[][] data)
    {
      this.data = data;
      this.columnNames = columnNames;
    }

    private Object[][] data;

    private String[] columnNames;

    @Override
    public int getColumnCount()
    {
      return columnNames.length;
    }

    @Override
    public int getRowCount()
    {
      return data.length;
    }

    @Override
    public String getColumnName(int col)
    {
      return columnNames[col];
    }

    @Override
    public Object getValueAt(int row, int col)
    {
      return data[row][col];
    }

    /*
     * JTable uses this method to determine the default renderer/ editor for
     * each cell. If we didn't implement this method, then the last column would
     * contain text ("true"/"false"), rather than a check box.
     */
    @Override
    public Class getColumnClass(int c)
    {
      return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's editable.
     */
    @Override
    public boolean isCellEditable(int row, int col)
    {
      // Note that the data/cell address is constant,
      // no matter where the cell appears onscreen.
      // !isPDBID(row, col) ensures the PDB_Id cell is never editable as it
      // serves as a unique id for each row.
      // return (col == 1 || col == 2) && !isPDBID(row, col);
      switch (currentSource)
      {
      case SEARCH_SUMMARY:
      case STRUCTURE_CHOOSER:
        return (col == 0) && !isPrimaryKeyCell(row, 1);
      case PREFERENCES:
        return (col == 1 || col == 2) && !isPrimaryKeyCell(row, 0);
      default:
        return false;
      }

    }

    /**
     * Determines whether the data in a given cell is a PDB ID.
     * 
     * @param row
     * @param col
     * @return
     */

    public boolean isPrimaryKeyCell(int row, int col)
    {
      String name = getValueAt(row, col).toString();
      FTSDataColumnI pdbField = map.get(name);
      return pdbField.isPrimaryKeyColumn();
    }

    /*
     * Don't need to implement this method unless your table's data can change.
     */
    @Override
    public void setValueAt(Object value, int row, int col)
    {
      data[row][col] = value;
      fireTableCellUpdated(row, col);

      String name = null;
      switch (currentSource)
      {
      case SEARCH_SUMMARY:
      case STRUCTURE_CHOOSER:
        name = getValueAt(row, 1).toString();
        break;
      case PREFERENCES:
        name = getValueAt(row, 0).toString();
        break;
      default:
        break;
      }
      boolean selected = ((Boolean) value).booleanValue();

      FTSDataColumnI ftsDataColumn = map.get(name);

      if (currentSource == PreferenceSource.SEARCH_SUMMARY)
      {
        updatePrefs(ftsRestClient.getAllDefaultDisplayedFTSDataColumns(),
                ftsDataColumn, selected);
      }
      else if (currentSource == PreferenceSource.STRUCTURE_CHOOSER)
      {
        updatePrefs(structSummaryColumns, ftsDataColumn, selected);
      }
      else if (currentSource == PreferenceSource.PREFERENCES)
      {
        if (col == 1)
        {
          updatePrefs(ftsRestClient.getAllDefaultDisplayedFTSDataColumns(),
                  ftsDataColumn, selected);
        }
        else if (col == 2)
        {
          updatePrefs(structSummaryColumns, ftsDataColumn, selected);
        }
      }
    }

    private void updatePrefs(Collection<FTSDataColumnI> prefConfig,
            FTSDataColumnI dataColumn, boolean selected)
    {
      if (prefConfig.contains(dataColumn) && !selected)
      {
        prefConfig.remove(dataColumn);
      }

      if (!prefConfig.contains(dataColumn) && selected)
      {
        prefConfig.add(dataColumn);
      }
    }

  }
}
