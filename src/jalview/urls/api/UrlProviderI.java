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
package jalview.urls.api;

import jalview.urls.UrlLinkDisplay;

import java.util.List;

/**
 * Methods for providing consistent access to up-to-date URLs
 * 
 * @author $author$
 * @version $Revision$
 */
public interface UrlProviderI
{

  /**
   * Get names and urls in the UrlProvider as strings for display
   * 
   */
  List<String> getLinksForMenu();

  /**
   * Get names and urls as strings for display
   * 
   */
  List<UrlLinkDisplay> getLinksForTable();

  /**
   * Set names and urls from display settings
   */
  void setUrlData(List<UrlLinkDisplay> links);

  /**
   * Get the link for the primary URL
   * 
   * @seqid sequence id for which to build link
   * @return link for the primary URL
   */
  String getPrimaryUrl(String seqid);

  /**
   * Get the primary URL id
   * 
   * @return id for primary URL
   */
  String getPrimaryUrlId();

  /**
   * Get the target of the link for the primary URL
   * 
   * @seqid sequence id for which to build link
   * @return target of link for the primary URL
   */
  String getPrimaryTarget(String seqid);

  /**
   * Set the primary URL: if only one URL can be used, this URL is the one which
   * should be chosen, e.g. provides the URL to be used on double-click of a
   * sequence id
   * 
   * @param id
   *          the id of the URL to set as primary
   * @return true if setting is successful
   * @throws IllegalArgumentException
   *           if id does not exist as a url in the UrlProvider
   */
  boolean setPrimaryUrl(String id) throws IllegalArgumentException;

  /**
   * Test if UrlProvider contains a url
   * 
   * @param id
   *          to test for
   * @return true of UrlProvider contains this id, false otherwise
   */
  boolean contains(String id);

  /**
   * Write out all URLs as a string suitable for serialising
   * 
   * @return string representation of available URLs
   */
  String writeUrlsAsString(boolean selected);

  /**
   * Choose the primary URL in the event of the selected primary being
   * unavailable
   * 
   * @return id of chosen primary url
   */
  String choosePrimaryUrl();

  /**
   * Determine if id is for a user-defined URL
   */
  boolean isUserEntry(String id);
}
