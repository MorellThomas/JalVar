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
package jalview.api;

import java.util.List;

import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceI;

/**
 * An interface for searching for a pattern in an aligment
 */
public interface FinderI
{

  /**
   * Performs a find for the given search string (interpreted as a regular
   * expression). Search may optionally be case-sensitive, and may optionally
   * including match in sequence description (sequence id is always searched).
   * If the viewport has an active selection, then the find is restricted to the
   * selection region. Sequences matched by id or description can be retrieved
   * by getIdMatches(), and matched residue patterns by getSearchResults().
   * <p>
   * If {@code ignoreHidden} is true, then any residues in hidden columns are
   * ignored (skipped) when matching, so for example pattern {@code KRT} would
   * match sequence {@code KRqmT} (where {@code qm} are in hidden columns).
   * <p>
   * Matches of entirely hidden patterns are not returned. Matches that span
   * hidden regions on one or both sides may be returned.
   * 
   * @param theSearchString
   * @param caseSensitive
   * @param searchDescription
   * @param searchFeatureDesc
   * @param ignoreHidden
   * @return
   */
  void findAll(String theSearchString, boolean caseSensitive,
          boolean searchDescription, boolean searchFeatureDesc,
          boolean ignoreHidden);

  /**
   * Finds the next match for the given search string (interpreted as a regular
   * expression), starting from the position after the last match found. Search
   * may optionally be case-sensitive, and may optionally including match in
   * sequence description (sequence id is always searched). If the viewport has
   * an active selection, then the find is restricted to the selection region.
   * Sequences matched by id or description can be retrieved by getIdMatches(),
   * and matched residue patterns by getSearchResults().
   * <p>
   * If {@code ignoreHidden} is true, any hidden residues are skipped (matches
   * may span them). If false, they are included for matching purposes. In
   * either cases, entirely hidden matches are not returned.
   * 
   * @param theSearchString
   * @param caseSensitive
   * @param searchDescription
   * @param searchFeatureDesc
   * @param ignoreHidden
   * @return
   */
  void findNext(String theSearchString, boolean caseSensitive,
          boolean searchDescription, boolean searchFeatureDesc,
          boolean ignoreHidden);

  /**
   * Returns the (possibly empty) list of sequences matched on sequence name or
   * description
   * 
   * @return
   */
  List<SequenceI> getIdMatches();

  /**
   * Answers the search results (possibly empty) from the last search
   * 
   * @return
   */
  SearchResultsI getSearchResults();

  void setFeatureRenderer(FeatureRenderer featureRenderer);

}