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
package jalview.ws.dbsources;

import jalview.bin.Cache;
import jalview.datamodel.DBRefSource;
import jalview.util.Platform;

import com.stevesoft.pat.Regex;

/**
 * Contains methods for fetching sequences from Rfam database
 * 
 * @author Lauren Michelle Lui
 */
abstract public class Rfam extends Xfam
{
  static final String RFAM_BASEURL_KEY = "RFAM_BASEURL";

  private static final String DEFAULT_RFAM_BASEURL = "https://rfam.org";

  static
  {
    Platform.addJ2SDirectDatabaseCall(DEFAULT_RFAM_BASEURL);
  }

  /*
   * append to URLs to retrieve as a gzipped file
   */
  protected static final String GZIPPED = "?gzip=1&download=1";

  @Override
  protected String getURLPrefix()
  {
    return Cache.getDefault(RFAM_BASEURL_KEY, DEFAULT_RFAM_BASEURL);
  }

  public Rfam()
  {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getAccessionSeparator() Left here for
   * consistency with Pfam class
   */
  @Override
  public String getAccessionSeparator()
  {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getAccessionValidator() * Left here for
   */
  @Override
  public Regex getAccessionValidator()
  {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * Left here for consistency with Pfam class
   * 
   * @see jalview.ws.DbSourceProxy#getDbSource() public String getDbSource() { *
   * this doesn't work - DbSource is key for the hash of DbSourceProxy instances
   * - 1:many mapping for DbSource to proxy will be lost. * suggest : RFAM is an
   * 'alignment' source - means proxy is higher level than a sequence source.
   * return jalview.datamodel.DBRefSource.RFAM; }
   */

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getDbVersion()
   */
  @Override
  public String getDbVersion()
  {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#isValidReference(java.lang.String)
   */
  @Override
  public boolean isValidReference(String accession)
  {
    return accession.indexOf("RF") == 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.dbsources.Xfam#getXfamSource()
   */
  @Override
  public String getXfamSource()
  {
    return DBRefSource.RFAM;
  }

}
