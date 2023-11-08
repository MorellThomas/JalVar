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

import jalview.bin.Console;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.GeneLociI;
import jalview.datamodel.GeneLocus;
import jalview.datamodel.Mapping;
import jalview.util.MapList;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ParseException;

/**
 * A client for the Ensembl /lookup REST endpoint, used to find the gene
 * identifier given a gene, transcript or protein identifier, or to extract the
 * species or chromosomal coordinates from the same service response
 * 
 * @author gmcarstairs
 */
public class EnsemblLookup extends EnsemblRestClient
{
  private static final String SPECIES = "species";

  /**
   * Default constructor (to use rest.ensembl.org)
   */
  public EnsemblLookup()
  {
    super();
  }

  /**
   * Constructor given the target domain to fetch data from
   * 
   * @param
   */
  public EnsemblLookup(String d)
  {
    super(d);
  }

  @Override
  public String getDbName()
  {
    return "ENSEMBL";
  }

  @Override
  public AlignmentI getSequenceRecords(String queries) throws Exception
  {
    return null;
  }

  @Override
  protected URL getUrl(List<String> ids) throws MalformedURLException
  {
    String identifier = ids.get(0);
    return getUrl(identifier, null);
  }

  /**
   * Gets the url for lookup of the given identifier, optionally with objectType
   * also specified in the request
   * 
   * @param identifier
   * @param objectType
   * @return
   */
  protected URL getUrl(String identifier, String objectType)
  {
    String url = getDomain() + "/lookup/id/" + identifier
            + CONTENT_TYPE_JSON;
    if (objectType != null)
    {
      url += "&" + OBJECT_TYPE + "=" + objectType;
    }

    try
    {
      return new URL(url);
    } catch (MalformedURLException e)
    {
      return null;
    }
  }

  @Override
  protected boolean useGetRequest()
  {
    return true;
  }

  /**
   * Returns the gene id related to the given identifier (which may be for a
   * gene, transcript or protein), or null if none is found
   * 
   * @param identifier
   * @return
   */
  public String getGeneId(String identifier)
  {
    return getGeneId(identifier, null);
  }

  /**
   * Returns the gene id related to the given identifier (which may be for a
   * gene, transcript or protein), or null if none is found
   * 
   * @param identifier
   * @param objectType
   * @return
   */
  public String getGeneId(String identifier, String objectType)
  {
    return parseGeneId(getResult(identifier, objectType));
  }

  /**
   * Parses the JSON response and returns the gene identifier, or null if not
   * found. If the returned object_type is Gene, returns the id, if Transcript
   * returns the Parent. If it is Translation (peptide identifier), then the
   * Parent is the transcript identifier, so we redo the search with this value.
   * 
   * @param br
   * @return
   */
  protected String parseGeneId(Map<String, Object> val)
  {
    if (val == null)
    {
      return null;
    }
    String geneId = null;
    String type = val.get(OBJECT_TYPE).toString();
    if (OBJECT_TYPE_GENE.equalsIgnoreCase(type))
    {
      // got the gene - just returns its id
      geneId = val.get(JSON_ID).toString();
    }
    else if (OBJECT_TYPE_TRANSCRIPT.equalsIgnoreCase(type))
    {
      // got the transcript - return its (Gene) Parent
      geneId = val.get(PARENT).toString();
    }
    else if (OBJECT_TYPE_TRANSLATION.equalsIgnoreCase(type))
    {
      // got the protein - get its Parent, restricted to type Transcript
      String transcriptId = val.get(PARENT).toString();
      geneId = getGeneId(transcriptId, OBJECT_TYPE_TRANSCRIPT);
    }

    return geneId;
  }

  /**
   * Calls the Ensembl lookup REST endpoint and retrieves the 'species' for the
   * given identifier, or null if not found
   * 
   * @param identifier
   * @return
   */
  public String getSpecies(String identifier)
  {
    String species = null;
    Map<String, Object> json = getResult(identifier, null);
    if (json != null)
    {
      Object o = json.get(SPECIES);
      if (o != null)
      {
        species = o.toString();
      }
    }
    return species;
  }

  /**
   * Calls the /lookup/id rest service and returns the response as a Map<String,
   * Object>, or null if any error
   * 
   * @param identifier
   * @param objectType
   *          (optional)
   * @return
   */
  @SuppressWarnings("unchecked")
  protected Map<String, Object> getResult(String identifier,
          String objectType)
  {
    List<String> ids = Arrays.asList(new String[] { identifier });

    try
    {
      return (Map<String, Object>) getJSON(getUrl(identifier, objectType),
              ids, -1, MODE_MAP, null);
    } catch (IOException | ParseException e)
    {
      System.err.println("Error parsing " + identifier + " lookup response "
              + e.getMessage());
      return null;
    }
  }

  /**
   * Calls the /lookup/id rest service for the given id, and if successful,
   * parses and returns the gene's chromosomal coordinates
   * 
   * @param geneId
   * @return
   */
  public GeneLociI getGeneLoci(String geneId)
  {
    return parseGeneLoci(getResult(geneId, OBJECT_TYPE_GENE));
  }

  /**
   * Parses the /lookup/id response for species, asssembly_name,
   * seq_region_name, start, end and returns an object that wraps them, or null
   * if unsuccessful
   * 
   * @param json
   * @return
   */
  GeneLociI parseGeneLoci(Map<String, Object> json)
  {
    if (json == null)
    {
      return null;
    }

    try
    {
      final String species = json.get("species").toString();
      final String assembly = json.get("assembly_name").toString();
      final String chromosome = json.get("seq_region_name").toString();
      String strand = json.get("strand").toString();
      int start = Integer.parseInt(json.get("start").toString());
      int end = Integer.parseInt(json.get("end").toString());
      int fromEnd = end - start + 1;
      boolean reverseStrand = "-1".equals(strand);
      int toStart = reverseStrand ? end : start;
      int toEnd = reverseStrand ? start : end;
      List<int[]> fromRange = Collections
              .singletonList(new int[]
              { 1, fromEnd });
      List<int[]> toRange = Collections
              .singletonList(new int[]
              { toStart, toEnd });
      final Mapping map = new Mapping(
              new MapList(fromRange, toRange, 1, 1));
      return new GeneLocus(species == null ? "" : species, assembly,
              chromosome, map);
    } catch (NullPointerException | NumberFormatException e)
    {
      Console.error("Error looking up gene loci: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

}
