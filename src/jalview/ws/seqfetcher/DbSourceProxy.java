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
package jalview.ws.seqfetcher;

import jalview.api.FeatureSettingsModelI;
import jalview.datamodel.AlignmentI;

import com.stevesoft.pat.Regex;

/**
 * generic Reference Retrieval interface for a particular database
 * source/version as cited in DBRefEntry.
 * 
 * TODO: add/define mechanism for retrieval of Trees and distance matrices from
 * a database (unify with io)
 * 
 * @author JimP
 */
public interface DbSourceProxy
{
  /**
   * 
   * @return source string constant used for this DB source
   */
  String getDbSource();

  /**
   * Short meaningful name for this data source for display in menus or
   * selection boxes.
   * 
   * @return String
   */
  String getDbName();

  /**
   * 
   * @return version string for this database.
   */
  String getDbVersion();

  /**
   * Separator between individual accession queries for a database that allows
   * multiple IDs to be fetched in a single query. Null implies that only a
   * single ID can be fetched at a time.
   * 
   * @return string for separating concatenated queries (as individually
   *         validated by the accession validator)
   */
  String getAccessionSeparator();

  /**
   * Regular expression for checking form of query string understood by this
   * source. If the Regex includes parenthesis, then the first parenthesis
   * should yield the same accession string as the one used to annotate the
   * sequence. This is used to match query strings to returned sequences.
   * 
   * @return null or a validation regex
   */
  Regex getAccessionValidator();

  /**
   * 
   * @return a test/example query that can be used to validate retrieval and
   *         parsing mechanisms
   */
  String getTestQuery();

  /**
   * Required for sources supporting multiple query retrieval for use with the
   * DBRefFetcher, which attempts to limit its queries with putative accession
   * strings for a source to only those that are likely to be valid.
   * 
   * @param accession
   * @return
   */
  boolean isValidReference(String accession);

  /**
   * make one or more queries to the database and attempt to parse the response
   * into an alignment
   * 
   * @param queries
   *          - one or more queries for database in expected form
   * @return null if queries were successful but result was not parsable.
   *         Otherwise, an AlignmentI object containing properly annotated data
   *         (e.g. sequences with accessions for this datasource)
   * @throws Exception
   *           - propagated from underlying transport to database (note -
   *           exceptions are not raised if query not found in database)
   * 
   */
  AlignmentI getSequenceRecords(String queries) throws Exception;

  /**
   * 
   * @return true if a query is currently being made
   */
  boolean queryInProgress();

  /**
   * get the raw reponse from the last set of queries
   * 
   * @return one or more string buffers for each individual query
   */
  StringBuffer getRawRecords();

  /**
   * Tier for this data source
   * 
   * @return 0 - primary datasource, 1 - das primary source, 2 - secondary
   */
  int getTier();

  /**
   * Extracts valid accession strings from a query string. If there is an
   * accession id validator, returns the the matched region or the first
   * subgroup match from the matched region; else just returns the whole query.
   * 
   * @param query
   * @return
   */
  String getAccessionIdFromQuery(String query);

  /**
   * Returns the maximum number of accession ids that can be queried in one
   * request.
   * 
   * @return
   */
  int getMaximumQueryCount();

  /**
   * Returns true if the source may provide coding DNA i.e. sequences with
   * implicit peptide products
   * 
   * @return
   */
  boolean isDnaCoding();

  /**
   * Answers true if the database is a source of alignments (for example, domain
   * families)
   * 
   * @return
   */
  boolean isAlignmentSource();

  /**
   * Returns an (optional) description of the source, suitable for display as a
   * tooltip, or null
   * 
   * @return
   */
  String getDescription();

  /**
   * Returns the preferred feature colour configuration if there is one, else
   * null
   * 
   * @return
   */
  FeatureSettingsModelI getFeatureColourScheme();
}