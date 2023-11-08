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
import jalview.datamodel.DBRefSource;
import jalview.datamodel.GeneLociI;
import jalview.datamodel.GeneLocus;
import jalview.datamodel.Mapping;
import jalview.util.MapList;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ParseException;

/**
 * A client for the Ensembl REST service /map endpoint, to convert from
 * coordinates of one genome assembly to another.
 * <p>
 * Note that species and assembly identifiers passed to this class must be valid
 * in Ensembl. They are not case sensitive.
 * 
 * @author gmcarstairs
 * @see https://rest.ensembl.org/documentation/info/assembly_map
 * @see https://rest.ensembl.org/info/assembly/human?content-type=text/xml
 * @see https://rest.ensembl.org/info/species?content-type=text/xml
 */
public class EnsemblMap extends EnsemblRestClient
{
  private static final String MAPPED = "mapped";

  private static final String MAPPINGS = "mappings";

  private static final String CDS = "cds";

  private static final String CDNA = "cdna";

  /**
   * Default constructor (to use rest.ensembl.org)
   */
  public EnsemblMap()
  {
    super();
  }

  /**
   * Constructor given the target domain to fetch data from
   * 
   * @param
   */
  public EnsemblMap(String domain)
  {
    super(domain);
  }

  @Override
  public String getDbName()
  {
    return DBRefSource.ENSEMBL;
  }

  @Override
  public AlignmentI getSequenceRecords(String queries) throws Exception
  {
    return null; // not used
  }

  /**
   * Constructs a URL of the format <code>
   * http://rest.ensembl.org/map/human/GRCh38/17:45051610..45109016:1/GRCh37?content-type=application/json
   * </code>
   * 
   * @param species
   * @param chromosome
   * @param fromRef
   * @param toRef
   * @param startPos
   * @param endPos
   * @return
   * @throws MalformedURLException
   */
  protected URL getAssemblyMapUrl(String species, String chromosome,
          String fromRef, String toRef, int startPos, int endPos)
          throws MalformedURLException
  {
    /*
     * start-end might be reverse strand - present forwards to the service
     */
    boolean forward = startPos <= endPos;
    int start = forward ? startPos : endPos;
    int end = forward ? endPos : startPos;
    String strand = forward ? "1" : "-1";
    String url = String.format(
            "%s/map/%s/%s/%s:%d..%d:%s/%s?content-type=application/json",
            getDomain(), species, fromRef, chromosome, start, end, strand,
            toRef);
    return new URL(url);
  }

  @Override
  protected boolean useGetRequest()
  {
    return true;
  }

  @Override
  protected URL getUrl(List<String> ids) throws MalformedURLException
  {
    return null; // not used
  }

  /**
   * Calls the REST /map service to get the chromosomal coordinates (start/end)
   * in 'toRef' that corresponding to the (start/end) queryRange in 'fromRef'
   * 
   * @param species
   * @param chromosome
   * @param fromRef
   * @param toRef
   * @param queryRange
   * @return
   * @see http://rest.ensemblgenomes.org/documentation/info/assembly_map
   */
  public int[] getAssemblyMapping(String species, String chromosome,
          String fromRef, String toRef, int[] queryRange)
  {
    URL url = null;
    try
    {
      url = getAssemblyMapUrl(species, chromosome, fromRef, toRef,
              queryRange[0], queryRange[1]);
      return (parseAssemblyMappingResponse(url));
    } catch (Throwable t)
    {
      System.out.println("Error calling " + url + ": " + t.getMessage());
      return null;
    }
  }

  /**
   * Parses the JSON response from the /map/&lt;species&gt;/ REST service. The
   * format is (with some fields omitted)
   * 
   * <pre>
   *  {"mappings": 
   *    [{
   *       "original": {"end":45109016,"start":45051610},
   *       "mapped"  : {"end":43186384,"start":43128978} 
   *  }] }
   * </pre>
   * 
   * @param br
   * @return
   */
  @SuppressWarnings("unchecked")
  protected int[] parseAssemblyMappingResponse(URL url)
  {
    int[] result = null;

    try
    {
      Iterator<Object> rvals = (Iterator<Object>) getJSON(url, null, -1,
              MODE_ITERATOR, MAPPINGS);
      if (rvals == null)
      {
        return null;
      }
      while (rvals.hasNext())
      {
        // todo check for "mapped"
        Map<String, Object> val = (Map<String, Object>) rvals.next();
        Map<String, Object> mapped = (Map<String, Object>) val.get(MAPPED);
        int start = Integer.parseInt(mapped.get("start").toString());
        int end = Integer.parseInt(mapped.get("end").toString());
        String strand = mapped.get("strand").toString();
        if ("1".equals(strand))
        {
          result = new int[] { start, end };
        }
        else
        {
          result = new int[] { end, start };
        }
      }
    } catch (IOException | ParseException | NumberFormatException e)
    {
      // ignore
    }
    return result;
  }

  /**
   * Calls the REST /map/cds/id service, and returns a DBRefEntry holding the
   * returned chromosomal coordinates, or returns null if the call fails
   * 
   * @param division
   *          e.g. Ensembl, EnsemblMetazoa
   * @param accession
   *          e.g. ENST00000592782, Y55B1AR.1.1
   * @param start
   * @param end
   * @return
   */
  public GeneLociI getCdsMapping(String division, String accession,
          int start, int end)
  {
    return getIdMapping(division, accession, start, end, CDS);
  }

  /**
   * Calls the REST /map/cdna/id service, and returns a DBRefEntry holding the
   * returned chromosomal coordinates, or returns null if the call fails
   * 
   * @param division
   *          e.g. Ensembl, EnsemblMetazoa
   * @param accession
   *          e.g. ENST00000592782, Y55B1AR.1.1
   * @param start
   * @param end
   * @return
   */
  public GeneLociI getCdnaMapping(String division, String accession,
          int start, int end)
  {
    return getIdMapping(division, accession, start, end, CDNA);
  }

  GeneLociI getIdMapping(String division, String accession, int start,
          int end, String cdsOrCdna)
  {
    URL url = null;
    try
    {
      String domain = new EnsemblInfo().getDomain(division);
      if (domain != null)
      {
        url = getIdMapUrl(domain, accession, start, end, cdsOrCdna);
        return (parseIdMappingResponse(url, accession, domain));
      }
      return null;
    } catch (Throwable t)
    {
      System.out.println("Error calling " + url + ": " + t.getMessage());
      return null;
    }
  }

  /**
   * Constructs a URL to the /map/cds/<id> or /map/cdna/<id> REST service. The
   * REST call is to either ensembl or ensemblgenomes, as determined from the
   * division, e.g. Ensembl or EnsemblProtists.
   * 
   * @param domain
   * @param accession
   * @param start
   * @param end
   * @param cdsOrCdna
   * @return
   * @throws MalformedURLException
   */
  URL getIdMapUrl(String domain, String accession, int start, int end,
          String cdsOrCdna) throws MalformedURLException
  {
    String url = String.format(
            "%s/map/%s/%s/%d..%d?include_original_region=1&content-type=application/json",
            domain, cdsOrCdna, accession, start, end);
    return new URL(url);
  }

  /**
   * Parses the JSON response from the /map/cds/ or /map/cdna REST service. The
   * format is
   * 
   * <pre>
   * {"mappings":
   *   [
   *    {"assembly_name":"TAIR10","end":2501311,"seq_region_name":"1","gap":0,
   *     "strand":-1,"coord_system":"chromosome","rank":0,"start":2501114},
   *    {"assembly_name":"TAIR10","end":2500815,"seq_region_name":"1","gap":0,
   *     "strand":-1,"coord_system":"chromosome","rank":0,"start":2500714}
   *   ]
   * }
   * </pre>
   * 
   * @param br
   * @param accession
   * @param domain
   * @return
   */
  @SuppressWarnings("unchecked")
  GeneLociI parseIdMappingResponse(URL url, String accession, String domain)
  {

    try
    {
      Iterator<Object> rvals = (Iterator<Object>) getJSON(url, null, -1,
              MODE_ITERATOR, MAPPINGS);
      if (rvals == null)
      {
        return null;
      }
      String assembly = null;
      String chromosome = null;
      int fromEnd = 0;
      List<int[]> regions = new ArrayList<>();

      while (rvals.hasNext())
      {
        Map<String, Object> val = (Map<String, Object>) rvals.next();
        Map<String, Object> original = (Map<String, Object>) val
                .get("original");
        fromEnd = Integer.parseInt(original.get("end").toString());

        Map<String, Object> mapped = (Map<String, Object>) val.get(MAPPED);
        int start = Integer.parseInt(mapped.get("start").toString());
        int end = Integer.parseInt(mapped.get("end").toString());
        String ass = mapped.get("assembly_name").toString();
        if (assembly != null && !assembly.equals(ass))
        {
          System.err.println(
                  "EnsemblMap found multiple assemblies - can't resolve");
          return null;
        }
        assembly = ass;
        String chr = mapped.get("seq_region_name").toString();
        if (chromosome != null && !chromosome.equals(chr))
        {
          System.err.println(
                  "EnsemblMap found multiple chromosomes - can't resolve");
          return null;
        }
        chromosome = chr;
        String strand = mapped.get("strand").toString();
        if ("-1".equals(strand))
        {
          regions.add(new int[] { end, start });
        }
        else
        {
          regions.add(new int[] { start, end });
        }
      }

      /*
       * processed all mapped regions on chromosome, assemble the result,
       * having first fetched the species id for the accession
       */
      final String species = new EnsemblLookup(domain)
              .getSpecies(accession);
      final String as = assembly;
      final String chr = chromosome;
      List<int[]> fromRange = Collections
              .singletonList(new int[]
              { 1, fromEnd });
      Mapping mapping = new Mapping(new MapList(fromRange, regions, 1, 1));
      return new GeneLocus(species == null ? "" : species, as, chr,
              mapping);
    } catch (IOException | ParseException | NumberFormatException e)
    {
      // ignore
    }

    return null;
  }

}
