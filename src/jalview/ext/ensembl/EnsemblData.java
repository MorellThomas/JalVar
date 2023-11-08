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
package jalview.ext.ensembl;

/**
 * A data class to model the data and rest version of one Ensembl domain,
 * currently for rest.ensembl.org and rest.ensemblgenomes.org
 * 
 * @author gmcarstairs
 */
class EnsemblData
{
  /*
   * The http domain this object is holding data values for
   */
  String domain;

  /*
   * The latest version Jalview has tested for, e.g. "4.5"; a minor version change should be
   * ok, a major version change may break stuff 
   */
  String expectedRestVersion;

  /*
   * Major / minor / point version e.g. "4.5.1"
   * @see http://rest.ensembl.org/info/rest/?content-type=application/json
   */
  String restVersion;

  /*
   * data version
   * @see http://rest.ensembl.org/info/data/?content-type=application/json
   */
  String dataVersion;

  /*
   * true when http://rest.ensembl.org/info/ping/?content-type=application/json
   * returns response code 200 and not {"error":"Database is unavailable"}
   */
  boolean restAvailable;

  /*
   * absolute time when availability was last checked
   */
  long lastAvailableCheckTime;

  /*
   * absolute time when version numbers were last checked
   */
  long lastVersionCheckTime;

  // flag set to true if REST major version is not the one expected
  boolean restMajorVersionMismatch;

  /*
   * absolute time to wait till if we overloaded the REST service
   */
  long retryAfter;

  /**
   * Constructor given expected REST version number e.g 4.5 or 3.4.3
   * 
   * @param restExpected
   */
  EnsemblData(String theDomain, String restExpected)
  {
    domain = theDomain;
    expectedRestVersion = restExpected;
    lastAvailableCheckTime = -1;
    lastVersionCheckTime = -1;
  }

}
