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

import jalview.fts.api.FTSDataColumnI.FTSDataColumnGroupI;
import jalview.fts.core.FTSDataColumnPreferences.PreferenceSource;
import jalview.fts.core.FTSRestRequest;
import jalview.fts.core.FTSRestResponse;

import java.util.Collection;

/**
 * Methods for FTS Rest client.
 * 
 * @author tcnofoegbu
 */
public interface FTSRestClientI
{

  /**
   * Execute a given FTS request, process the response and return it as an
   * FTSRestResponse object
   * 
   * @param ftsRestRequest
   *          the FTS request to execute
   * @return FTSRestResponse - the response after executing an FTS request
   * @throws Exception
   */
  public FTSRestResponse executeRequest(FTSRestRequest ftsRequest)
          throws Exception;

  /**
   * Return the resource file path for the data columns configuration file
   * 
   * @return
   */
  public String getColumnDataConfigFileName();

  /**
   * Fetch FTSDataColumnGroupI by the group's Id
   * 
   * @param groupId
   * @return FTSDataColumnGroupI
   * @throws Exception
   */
  public FTSDataColumnGroupI getDataColumnGroupById(String groupId)
          throws Exception;

  /**
   * Fetch FTSDataColumnI by name or code
   * 
   * @param nameOrCode
   * @return FTSDataColumnI
   * @throws Exception
   */
  public FTSDataColumnI getDataColumnByNameOrCode(String nameOrCode)
          throws Exception;

  /**
   * Convert collection of FTSDataColumnI objects to a comma delimited string of
   * the 'code' values
   * 
   * @param wantedFields
   *          the collection of FTSDataColumnI to process
   * @return the generated comma delimited string from the supplied
   *         FTSDataColumnI collection
   */
  public String getDataColumnsFieldsAsCommaDelimitedString(
          Collection<FTSDataColumnI> wantedFields);

  /**
   * Fetch index of the primary key column for the dynamic table TODO: consider
   * removing 'hasRefSeq' - never used in code
   * 
   * @param wantedFields
   *          the available table columns
   * @param hasRefSeq
   *          true if the data columns has an additional column for reference
   *          sequence
   * @return index of the primary key column
   * @throws Exception
   */
  public int getPrimaryKeyColumIndex(
          Collection<FTSDataColumnI> wantedFields, boolean hasRefSeq)
          throws Exception;

  /**
   * Fetch the primary key data column object
   * 
   * @return the FTSDataColumnI object for the primary key column
   */
  public FTSDataColumnI getPrimaryKeyColumn();

  /**
   * Returns list of FTSDataColumnI objects to be displayed by default
   * 
   * @return list of columns to display by default
   */
  public Collection<FTSDataColumnI> getAllDefaultDisplayedFTSDataColumns();

  /**
   * Return list of FTSDataColumnI objects that can be used to perform a search
   * query
   * 
   * @return list of searchable FTSDataColumnI object
   */
  public Collection<FTSDataColumnI> getSearchableDataColumns();

  /**
   * Return list of all available FTSDataColumnI object
   * 
   * @return list of all FTSColumnI objcet
   */
  public Collection<FTSDataColumnI> getAllFTSDataColumns();

  /**
   * Return the default response page limit
   * 
   * @return the default response page size
   */
  public int getDefaultResponsePageSize();

  public String[] getPreferencesColumnsFor(PreferenceSource source);
}
