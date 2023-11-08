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

import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.io.gff.SequenceOntologyI;
import jalview.util.JSONUtils;
import jalview.util.Platform;

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
 * A client for fetching and processing Ensembl feature data in GFF format by
 * calling the overlap REST service
 * 
 * @author gmcarstairs
 * @see http://rest.ensembl.org/documentation/info/overlap_id
 */
class EnsemblFeatures extends EnsemblRestClient
{
  /*
   * The default features to retrieve from Ensembl
   * can override in getSequenceRecords parameter
   */
  private EnsemblFeatureType[] featuresWanted = { EnsemblFeatureType.cds,
      EnsemblFeatureType.exon, EnsemblFeatureType.variation };

  /**
   * Default constructor (to use rest.ensembl.org)
   */
  public EnsemblFeatures()
  {
    super();
  }

  /**
   * Constructor given the target domain to fetch data from
   * 
   * @param d
   */
  public EnsemblFeatures(String d)
  {
    super(d);
  }

  @Override
  public String getDbName()
  {
    return "ENSEMBL (features)";
  }

  /**
   * Makes a query to the REST overlap endpoint for the given sequence
   * identifier. This returns an 'alignment' consisting of one 'dummy sequence'
   * (the genomic sequence for which overlap features are returned by the
   * service). This sequence will have on it sequence features which are the
   * real information of interest, such as CDS regions or sequence variations.
   */
  @Override
  public AlignmentI getSequenceRecords(String query) throws IOException
  {
    // TODO: use a vararg String... for getSequenceRecords instead?

    List<String> queries = new ArrayList<>();
    queries.add(query);
    SequenceI seq = parseFeaturesJson(queries);
    if (seq == null)
      return null;
    return new Alignment(new SequenceI[] { seq });

  }

  /**
   * Parses the JSON response into Jalview sequence features and attaches them
   * to a dummy sequence
   * 
   * @param br
   * @return
   */
  @SuppressWarnings("unchecked")
  private SequenceI parseFeaturesJson(List<String> queries)
  {
    SequenceI seq = new Sequence("Dummy", "");
    try
    {
      Iterator<Object> rvals = (Iterator<Object>) getJSON(null, queries, -1,
              MODE_ITERATOR, null);
      if (rvals == null)
      {
        return null;
      }
      while (rvals.hasNext())
      {
        try
        {
          Map<String, Object> obj = (Map<String, Object>) rvals.next();
          String type = obj.get("feature_type").toString();
          int start = Integer.parseInt(obj.get("start").toString());
          int end = Integer.parseInt(obj.get("end").toString());
          String source = obj.get("source").toString();
          String strand = obj.get("strand").toString();
          Object phase = obj.get("phase");
          String alleles = JSONUtils
                  .arrayToStringList((List<Object>) obj.get("alleles"));
          String clinSig = JSONUtils.arrayToStringList(
                  (List<Object>) obj.get("clinical_significance"));

          /*
           * convert 'variation' to 'sequence_variant', and 'cds' to 'CDS'
           * so as to have a valid SO term for the feature type
           * ('gene', 'exon', 'transcript' don't need any conversion)
           */
          if ("variation".equals(type))
          {
            type = SequenceOntologyI.SEQUENCE_VARIANT;
          }
          else if (SequenceOntologyI.CDS.equalsIgnoreCase((type)))
          {
            type = SequenceOntologyI.CDS;
          }

          String desc = getFirstNotNull(obj, "alleles", "external_name",
                  JSON_ID);
          SequenceFeature sf = new SequenceFeature(type, desc, start, end,
                  source);
          sf.setStrand("1".equals(strand) ? "+" : "-");
          if (phase != null)
          {
            sf.setPhase(phase.toString());
          }
          setFeatureAttribute(sf, obj, "id");
          setFeatureAttribute(sf, obj, "Parent");
          setFeatureAttribute(sf, obj, "consequence_type");
          sf.setValue("alleles", alleles);
          sf.setValue("clinical_significance", clinSig);

          seq.addSequenceFeature(sf);

        } catch (Throwable t)
        {
          // ignore - keep trying other features
        }
      }
    } catch (ParseException | IOException e)
    {
      e.printStackTrace();
      // ignore
    }

    return seq;
  }

  /**
   * Returns the first non-null attribute found (if any) as a string, formatted
   * suitably for display as feature description or tooltip. Answers null if
   * none of the attribute keys is present.
   * 
   * @param obj
   * @param keys
   * @return
   */
  @SuppressWarnings("unchecked")
  protected String getFirstNotNull(Map<String, Object> obj, String... keys)
  {
    for (String key : keys)
    {
      Object val = obj.get(key);
      if (val != null)
      {
        String s = val instanceof List<?>
                ? JSONUtils.arrayToStringList((List<Object>) val)
                : val.toString();
        if (!s.isEmpty())
        {
          return s;
        }
      }
    }
    return null;
  }

  /**
   * A helper method that reads the 'key' entry in the JSON object, and if not
   * null, sets its string value as an attribute on the sequence feature
   * 
   * @param sf
   * @param obj
   * @param key
   */
  protected void setFeatureAttribute(SequenceFeature sf,
          Map<String, Object> obj, String key)
  {
    Object object = obj.get(key);
    if (object != null)
    {
      sf.setValue(key, object.toString());
    }
  }

  /**
   * Returns a URL for the REST overlap endpoint
   * 
   * @param ids
   * @return
   */
  @Override
  protected URL getUrl(List<String> ids) throws MalformedURLException
  {
    StringBuffer urlstring = new StringBuffer(128);
    urlstring.append(getDomain()).append("/overlap/id/").append(ids.get(0));

    // @see https://github.com/Ensembl/ensembl-rest/wiki/Output-formats
    urlstring.append("?content-type=" + getResponseMimeType());

    /*
     * specify object_type=gene in case is shared by transcript and/or protein;
     * currently only fetching features for gene sequences;
     * refactor in future if needed to fetch for transcripts
     */
    urlstring.append("&").append(OBJECT_TYPE).append("=")
            .append(OBJECT_TYPE_GENE);

    /*
     * specify  features to retrieve
     * @see http://rest.ensembl.org/documentation/info/overlap_id
     * could make the list a configurable entry in .jalview_properties
     */
    for (EnsemblFeatureType feature : featuresWanted)
    {
      urlstring.append("&feature=").append(feature.name());
    }

    return new URL(urlstring.toString());
  }

  @Override
  protected boolean useGetRequest()
  {
    return true;
  }

  /**
   * Returns the MIME type for GFF3. For GET requests the Content-type header
   * describes the required encoding of the response.
   */
  @Override
  protected String getRequestMimeType()
  {
    return "application/json";
  }

  /**
   * Returns the MIME type wanted for the response
   */
  @Override
  protected String getResponseMimeType()
  {
    return "application/json";
  }

  /**
   * Overloaded method that allows a list of features to retrieve to be
   * specified
   * 
   * @param accId
   * @param features
   * @return
   * @throws IOException
   */
  protected AlignmentI getSequenceRecords(String accId,
          EnsemblFeatureType[] features) throws IOException
  {
    featuresWanted = features;
    return getSequenceRecords(accId);
  }
}
