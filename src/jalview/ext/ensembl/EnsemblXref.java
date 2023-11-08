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

import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.util.DBRefUtils;
import jalview.util.JSONUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ParseException;

/**
 * A class to fetch cross-references from Ensembl by calling the /xrefs REST
 * service
 * 
 * @author gmcarstairs
 * @see http://rest.ensembl.org/documentation/info/xref_id
 */
class EnsemblXref extends EnsemblRestClient
{

  private static final String GO_GENE_ONTOLOGY = "GO";

  private String dbName = "ENSEMBL (xref)";

  /**
   * Constructor given the target domain to fetch data from
   * 
   * @param d
   */
  public EnsemblXref(String d, String dbSource, String version)
  {
    super(d);
    dbName = dbSource;
    xrefVersion = dbSource + ":" + version;

  }

  @Override
  public String getDbName()
  {
    return dbName;
  }

  @Override
  public AlignmentI getSequenceRecords(String queries) throws Exception
  {
    return null;
  }

  @Override
  protected URL getUrl(List<String> ids) throws MalformedURLException
  {
    return getUrl(ids.get(0));
  }

  @Override
  protected boolean useGetRequest()
  {
    return true;
  }

  /**
   * Calls the Ensembl xrefs REST endpoint and retrieves any cross-references
   * ("primary_id") for the given identifier (Ensembl accession id) and database
   * names. The "dbname" returned by Ensembl is canonicalised to Jalview's
   * standard version, and a DBRefEntry constructed. Currently takes all
   * identifiers apart from GO terms and synonyms.
   * 
   * @param identifier
   *          an Ensembl stable identifier
   * @return
   */
  @SuppressWarnings("unchecked")
  public List<DBRefEntry> getCrossReferences(String identifier)
  {
    List<DBRefEntry> result = new ArrayList<>();
    List<String> ids = new ArrayList<>();
    ids.add(identifier);

    try
    {
      Iterator<Object> rvals = (Iterator<Object>) getJSON(
              getUrl(identifier), ids, -1, MODE_ITERATOR, null);
      while (rvals.hasNext())
      {
        Map<String, Object> val = (Map<String, Object>) rvals.next();
        String db = val.get("dbname").toString();
        String id = val.get("primary_id").toString();
        if (db != null && id != null && !GO_GENE_ONTOLOGY.equals(db))
        {
          db = DBRefUtils.getCanonicalName(db);
          DBRefEntry dbref = new DBRefEntry(db, getXRefVersion(), id);
          result.add(dbref);
        }
      }
    } catch (ParseException | IOException e)
    {
      // ignore
    }
    return result;
  }

  // /**
  // * Parses "primary_id" and "dbname" values from the JSON response and
  // * constructs a DBRefEntry. Returns a list of the DBRefEntry created. Note
  // we
  // * don't parse "synonyms" as they appear to be either redirected or obsolete
  // * in Uniprot.
  // *
  // * @param br
  // * @return
  // * @throws IOException
  // */
  // @SuppressWarnings("unchecked")
  // protected List<DBRefEntry> parseResponse(BufferedReader br)
  // throws IOException
  // {
  // return result;
  // }
  //
  private String xrefVersion = "ENSEMBL:0";

  /**
   * version string for Xrefs - for 2.10, hardwired for ENSEMBL:0
   * 
   * @return
   */
  public String getXRefVersion()
  {
    return xrefVersion;
  }

  /**
   * Returns the URL for the REST endpoint to fetch all cross-references for an
   * identifier. Note this may return protein cross-references for nucleotide.
   * Filter the returned list as required.
   * 
   * @param identifier
   * @return
   */
  protected URL getUrl(String identifier)
  {
    String url = getDomain() + "/xrefs/id/" + identifier + CONTENT_TYPE_JSON
            + "&all_levels=1";
    try
    {
      return new URL(url);
    } catch (MalformedURLException e)
    {
      return null;
    }
  }

}
