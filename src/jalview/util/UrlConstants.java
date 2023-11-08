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
package jalview.util;

/**
 * A class to hold constants relating to Url links used in Jalview
 */
public class UrlConstants
{

  /*
   * Sequence ID string
   */
  public static final String DB_ACCESSION = "DB_ACCESSION";

  /*
   * Sequence Name string
   */
  public static final String SEQUENCE_ID = "SEQUENCE_ID";

  /*
   * Separator character used in Url links
   */
  public static final String SEP = "|";

  /*
   * Delimiter character used in Url links
   */
  public static final String DELIM = "$";

  /*
   * Default sequence URL link label for EMBL-EBI search
   */
  public static final String DEFAULT_LABEL = "EMBL-EBI Search";

  /*
   * Default sequence URL link string for EMBL-EBI search
   */
  public static final String DEFAULT_STRING = DEFAULT_LABEL
          + "|https://www.ebi.ac.uk/ebisearch/search.ebi?db=allebi&query=$SEQUENCE_ID$";

  private static final String COLON = ":";

  /*
   * not instantiable
   */
  private UrlConstants()
  {
  }

  public static boolean isDefaultString(String link)
  {
    String sublink = link.substring(link.indexOf(COLON) + 1);
    String subdefault = DEFAULT_STRING
            .substring(DEFAULT_STRING.indexOf(COLON) + 1);
    return sublink.equalsIgnoreCase(subdefault);
  }
}
