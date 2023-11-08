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

package jalview.fts.api;

/**
 * This interface provides a model for the dynamic data column configuration
 * 
 * @author tcnofoegbu
 *
 */
public interface FTSDataColumnI
{
  /**
   * Returns the name of the data column
   * 
   * @return the data column's name
   */
  public String getName();

  /**
   * Returns the code of the data column
   * 
   * @return the data column's code
   */
  public String getCode();

  /**
   * Returns the alternative code value for the data column
   * 
   * @return the data column's code
   */
  public String getAltCode();

  /**
   * Returns the minimum width of the data column
   * 
   * @return the data column's minimum width
   */
  public int getMinWidth();

  /**
   * Returns the maximum width of the data column
   * 
   * @return the data column's maximum width
   */
  public int getMaxWidth();

  /**
   * Returns the preferred width of the data column
   * 
   * @return the data column's preferred width
   */
  public int getPreferredWidth();

  /**
   * Determines if the data column is the primary key column
   * 
   * @return true if data column is the primary key column, otherwise false
   */
  public boolean isPrimaryKeyColumn();

  /**
   * Checks if the data column field can be used to perform a search query
   * 
   * @return true means the data column is searchable
   */
  public boolean isSearchable();

  /**
   * Checks if the data column is displayed by default
   * 
   * @return true means the data column is shown by default
   */
  public boolean isVisibleByDefault();

  /**
   * Returns the data column's FTS data column group
   * 
   * @return the FTSDataColumnGroupI for the column
   */
  public FTSDataColumnGroupI getGroup();

  /**
   * Returns the data columns data type POJO
   * 
   * @return the DataTypeI for the column
   */
  public DataTypeI getDataType();

  /**
   * This interface provides a model for the dynamic data column group
   * 
   */
  public interface FTSDataColumnGroupI
  {
    /**
     * Returns the Id of the data column's group
     * 
     * @return the data column's group Id
     */
    public String getID();

    /**
     * Returns the name of the group
     * 
     * @return the group's name
     */
    public String getName();

    /**
     * Returns the sort order of the group
     * 
     * @return the group's sort order
     */
    public int getSortOrder();
  }

  public interface DataTypeI
  {
    /**
     * Returns the data column's data type class
     * 
     * @return the Class for the data column's data type
     */
    public Class getDataTypeClass();

    /**
     * Checks if the numeric data column's data will be formated
     * 
     * @return true means the numeric data column shall be formatted
     */
    public boolean isFormtted();

    /**
     * Returns the number of significant figure to be used for the numeric value
     * formatting
     * 
     * @return the number of significant figures
     */
    public int getSignificantFigures();
  }
}
