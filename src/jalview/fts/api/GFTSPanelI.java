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

import java.util.Map;

import javax.swing.JTable;

/**
 * 
 * @author tcnofoegbu
 *
 */
public interface GFTSPanelI
{

  /**
   * Action performed when a text is entered in the search field.
   * 
   * @param isFreshSearch
   *          if true a fresh search is executed else a pagination search is
   *          executed
   */
  public void searchAction(boolean isFreshSearch);

  /**
   * Action performed when search results are selected and the 'ok' button is
   * pressed.
   */
  public void okAction();

  /**
   * Return the entered text
   * 
   * @return the entered text
   */
  public String getTypedText();

  /**
   * The JTable for presenting the query result
   * 
   * @return JTable
   */
  public JTable getResultTable();

  /**
   * Return the title to display on the search interface main panel
   * 
   * @return String - the title
   */
  public String getFTSFrameTitle();

  /**
   * Return a singleton instance of FTSRestClientI
   * 
   * @return FTSRestClientI
   */
  public FTSRestClientI getFTSRestClient();

  /**
   * Set error message when one occurs
   * 
   * @param message
   *          the error message to set
   */
  public void setErrorMessage(String message);

  /**
   * Updates the title displayed on the search interface's main panel
   * 
   * @param newTitle
   */
  public void updateSearchFrameTitle(String newTitle);

  /**
   * Controls the progress spinner, set to 'true' while search operation is in
   * progress and 'false' after it completes
   * 
   * @param isSearchInProgress
   */
  public void setSearchInProgress(Boolean isSearchInProgress);

  /**
   * Action performed when previous page (<<) button is pressed pressed.
   */
  public void prevPageAction();

  /**
   * Action performed when next page (>>) button is pressed pressed.
   */
  public void nextPageAction();

  /**
   * Checks if the current service's search result is paginate-able
   * 
   * @return true means the service provides paginated results
   */
  public boolean isPaginationEnabled();

  /**
   * Updates the 'enabled' state for the previous page button
   * 
   * @param isEnabled
   */
  public void setPrevPageButtonEnabled(boolean isEnabled);

  /**
   * Updates the 'enabled' state for the next page button
   * 
   * @param isEnabled
   */
  public void setNextPageButtonEnabled(boolean isEnabled);

  /**
   * The HashMap used to store user preferences for summary table columns,
   * window size and position
   * 
   * @return
   */
  public Map<String, Integer> getTempUserPrefs();

  /**
   * Returns unique key used for storing an FTSs instance cache items in the
   * cache data structure
   * 
   * @return
   */
  public String getCacheKey();

  /**
   * 
   * @return user preference name for configuring this FTS search's autosearch
   *         checkbox
   */
  public String getAutosearchPreference();

  /**
   * Return the name of the database being searched
   * 
   * @return The database name
   */
  public String getDbName();
}
