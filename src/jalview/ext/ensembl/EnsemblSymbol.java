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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ParseException;

/**
 * A client for the Ensembl xrefs/symbol REST service;
 * 
 * @see http://rest.ensembl.org/documentation/info/xref_external
 * @author gmcarstairs
 *
 */
public class EnsemblSymbol extends EnsemblXref
{
  private static final String GENE = "gene";

  private static final String TYPE = "type";

  /**
   * Constructor given the target domain to fetch data from
   * 
   * @param domain
   * @param dbName
   * @param dbVersion
   */
  public EnsemblSymbol(String domain, String dbName, String dbVersion)
  {
    super(domain, dbName, dbVersion);
  }

  // /**
  // * Returns the first "id" value in gene identifier format from the JSON
  // * response, or null if none found
  // *
  // * @param br
  // * @return
  // * @throws IOException
  // */
  // @SuppressWarnings("unchecked")
  // protected String parseSymbolResponse(BufferedReader br) throws IOException
  // {
  // }

  /**
   * Constructs the URL for the REST symbol endpoint
   * 
   * @param id
   *          the accession id (Ensembl or external)
   * @param species
   *          a species name recognisable by Ensembl
   * @param type
   *          an optional type to filter the response (gene, transcript,
   *          translation)
   * @return
   */
  protected URL getUrl(String id, Species species, String... type)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(getDomain()).append("/xrefs/symbol/")
            .append(species.toString()).append("/").append(id)
            .append(CONTENT_TYPE_JSON);
    for (String t : type)
    {
      sb.append("&object_type=").append(t);
    }
    try
    {
      String url = sb.toString();
      return new URL(url);
    } catch (MalformedURLException e)
    {
      return null;
    }
  }

  /**
   * Calls the Ensembl xrefs REST 'symbol' endpoint and retrieves any gene ids
   * for the given identifier, for any known model organisms
   * 
   * @param identifier
   * @return
   */
  @SuppressWarnings("unchecked")
  public List<String> getGeneIds(String identifier)
  {
    List<String> result = new ArrayList<String>();
    List<String> ids = new ArrayList<String>();
    ids.add(identifier);

    String[] queries = identifier.split(getAccessionSeparator());
    try
    {
      for (String query : queries)
      {
        for (Species taxon : Species.getModelOrganisms())
        {
          String geneId = null;/// parseSymbolResponse(br);
          try
          {
            Iterator<Object> rvals = (Iterator<Object>) getJSON(
                    getUrl(query, taxon, GENE), ids, -1, MODE_ITERATOR,
                    null);
            if (rvals == null)
              continue;
            while (rvals.hasNext())
            {
              Map<String, Object> val = (Map<String, Object>) rvals.next();
              String id = val.get(JSON_ID).toString();
              String type = val.get(TYPE).toString();
              if (id != null && GENE.equals(type))
              {
                geneId = id;
                break;
              }
            }
          } catch (ParseException e)
          {
            // ignore
          }

          if (geneId != null && !result.contains(geneId))
          {
            result.add(geneId);
          }
        }
      }
    } catch (IOException e)
    {
      // ignore
    }
    return result;
  }

}
