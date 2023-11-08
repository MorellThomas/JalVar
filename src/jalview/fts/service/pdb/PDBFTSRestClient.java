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
package jalview.fts.service.pdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.net.URI;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.core.MediaType;

import org.json.simple.parser.ParseException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import jalview.datamodel.SequenceI;
import jalview.fts.api.FTSData;
import jalview.fts.api.FTSDataColumnI;
import jalview.fts.api.FTSRestClientI;
import jalview.fts.api.StructureFTSRestClientI;
import jalview.fts.core.FTSDataColumnPreferences;
import jalview.fts.core.FTSDataColumnPreferences.PreferenceSource;
import jalview.fts.core.FTSRestClient;
import jalview.fts.core.FTSRestRequest;
import jalview.fts.core.FTSRestResponse;
import jalview.fts.service.alphafold.AlphafoldRestClient;
import jalview.util.JSONUtils;
import jalview.util.MessageManager;
import jalview.util.Platform;

/**
 * A rest client for querying the Search endpoint of the PDB API
 * 
 * @author tcnofoegbu
 */
public class PDBFTSRestClient extends FTSRestClient
        implements StructureFTSRestClientI
{

  private static FTSRestClientI instance = null;

  public static final String PDB_SEARCH_ENDPOINT = "https://www.ebi.ac.uk/pdbe/search/pdb/select?";

  protected PDBFTSRestClient()
  {
  }

  /**
   * Takes a PDBRestRequest object and returns a response upon execution
   * 
   * @param pdbRestRequest
   *          the PDBRestRequest instance to be processed
   * @return the pdbResponse object for the given request
   * @throws Exception
   */
  @SuppressWarnings({ "unused", "unchecked" })
  @Override
  public FTSRestResponse executeRequest(FTSRestRequest pdbRestRequest)
          throws Exception
  {
    try
    {
      String wantedFields = getDataColumnsFieldsAsCommaDelimitedString(
              pdbRestRequest.getWantedFields());
      int responseSize = (pdbRestRequest.getResponseSize() == 0)
              ? getDefaultResponsePageSize()
              : pdbRestRequest.getResponseSize();
      int offSet = pdbRestRequest.getOffSet();
      String sortParam = null;
      if (pdbRestRequest.getFieldToSortBy() == null
              || pdbRestRequest.getFieldToSortBy().trim().isEmpty())
      {
        sortParam = "";
      }
      else
      {
        if (pdbRestRequest.getFieldToSortBy()
                .equalsIgnoreCase("Resolution"))
        {
          sortParam = pdbRestRequest.getFieldToSortBy()
                  + (pdbRestRequest.isAscending() ? " asc" : " desc");
        }
        else
        {
          sortParam = pdbRestRequest.getFieldToSortBy()
                  + (pdbRestRequest.isAscending() ? " desc" : " asc");
        }
      }

      String facetPivot = (pdbRestRequest.getFacetPivot() == null
              || pdbRestRequest.getFacetPivot().isEmpty()) ? ""
                      : pdbRestRequest.getFacetPivot();
      String facetPivotMinCount = String
              .valueOf(pdbRestRequest.getFacetPivotMinCount());

      String query = pdbRestRequest.getFieldToSearchBy()
              + pdbRestRequest.getSearchTerm()
              + (pdbRestRequest.isAllowEmptySeq() ? ""
                      : " AND molecule_sequence:['' TO *]")
              + (pdbRestRequest.isAllowUnpublishedEntries() ? ""
                      : " AND status:REL");

      // Build request parameters for the REST Request

      // BH 2018 the trick here is to coerce the classes in Javascript to be
      // different from the ones in Java yet still allow this to be correct for
      // Java
      Client client;
      Class<ClientResponse> clientResponseClass;
      if (Platform.isJS())
      {
        // JavaScript only -- coerce types to Java types for Java
        client = (Client) (Object) new jalview.javascript.web.Client();
        clientResponseClass = (Class<ClientResponse>) (Object) jalview.javascript.web.ClientResponse.class;
      }
      else
      /**
       * Java only
       * 
       * @j2sIgnore
       */
      {
        client = Client.create(new DefaultClientConfig());
        clientResponseClass = ClientResponse.class;
      }

      WebResource webResource;
      if (pdbRestRequest.isFacet())
      {
        webResource = client.resource(PDB_SEARCH_ENDPOINT)
                .queryParam("wt", "json").queryParam("fl", wantedFields)
                .queryParam("rows", String.valueOf(responseSize))
                .queryParam("q", query)
                .queryParam("start", String.valueOf(offSet))
                .queryParam("sort", sortParam).queryParam("facet", "true")
                .queryParam("facet.pivot", facetPivot)
                .queryParam("facet.pivot.mincount", facetPivotMinCount);
      }
      else
      {
        webResource = client.resource(PDB_SEARCH_ENDPOINT)
                .queryParam("wt", "json").queryParam("fl", wantedFields)
                .queryParam("rows", String.valueOf(responseSize))
                .queryParam("start", String.valueOf(offSet))
                .queryParam("q", query).queryParam("sort", sortParam);
      }

      URI uri = webResource.getURI();

      System.out.println(uri);
      ClientResponse clientResponse = null;
      int responseStatus = -1;
      // Get the JSON string from the response object or directly from the
      // client (JavaScript)
      Map<String, Object> jsonObj = null;
      String responseString = null;

      System.out.println("query >>>>>>> " + pdbRestRequest.toString());

      if (!isMocked())
      {
        // Execute the REST request
        clientResponse = webResource.accept(MediaType.APPLICATION_JSON)
                .get(clientResponseClass);
        responseStatus = clientResponse.getStatus();
      }
      else
      {
        // mock response
        if (mockQueries.containsKey(uri.toString()))
        {
          responseStatus = 200;
        }
        else
        {
          // FIXME - may cause unexpected exceptions for callers when mocked
          responseStatus = 400;
        }
      }

      // Check the response status and report exception if one occurs
      switch (responseStatus)
      {
      case 200:

        if (isMocked())
        {
          responseString = mockQueries.get(uri.toString());
        }
        else
        {
          if (Platform.isJS())
          {
            jsonObj = clientResponse.getEntity(Map.class);
          }
          else
          {
            responseString = clientResponse.getEntity(String.class);
          }
        }
        break;
      case 400:
        throw new Exception(isMocked() ? "400 response (Mocked)"
                : parseJsonExceptionString(responseString));
      default:
        throw new Exception(
                getMessageByHTTPStatusCode(responseStatus, "PDB"));
      }

      // Process the response and return the result to the caller.
      return parsePDBJsonResponse(responseString, jsonObj, pdbRestRequest);
    } catch (Exception e)
    {
      if (e.getMessage() == null)
      {
        throw (e);
      }
      String exceptionMsg = e.getMessage();
      if (exceptionMsg.contains("SocketException"))
      {
        // No internet connection
        throw new Exception(MessageManager.getString(
                "exception.unable_to_detect_internet_connection"));
      }
      else if (exceptionMsg.contains("UnknownHostException"))
      {
        // The server 'www.ebi.ac.uk' is unreachable
        throw new Exception(MessageManager.formatMessage(
                "exception.fts_server_unreachable", "PDB Solr"));
      }
      else
      {
        throw e;
      }
    }
  }

  /**
   * Process error response from PDB server if/when one occurs.
   * 
   * @param jsonResponse
   *          the JSON string containing error message from the server
   * @return the processed error message from the JSON string
   */
  @SuppressWarnings("unchecked")
  public static String parseJsonExceptionString(String jsonErrorResponse)
  {
    StringBuilder errorMessage = new StringBuilder(
            "\n============= PDB Rest Client RunTime error =============\n");

    // {
    // "responseHeader":{
    // "status":0,
    // "QTime":0,
    // "params":{
    // "q":"(text:q93xj9_soltu) AND molecule_sequence:['' TO *] AND status:REL",
    // "fl":"pdb_id,title,experimental_method,resolution",
    // "start":"0",
    // "sort":"overall_quality desc",
    // "rows":"500",
    // "wt":"json"}},
    // "response":{"numFound":1,"start":0,"docs":[
    // {
    // "experimental_method":["X-ray diffraction"],
    // "pdb_id":"4zhp",
    // "resolution":2.46,
    // "title":"The crystal structure of Potato ferredoxin I with 2Fe-2S
    // cluster"}]
    // }}
    //
    try
    {
      Map<String, Object> jsonObj = (Map<String, Object>) JSONUtils
              .parse(jsonErrorResponse);
      Map<String, Object> errorResponse = (Map<String, Object>) jsonObj
              .get("error");

      Map<String, Object> responseHeader = (Map<String, Object>) jsonObj
              .get("responseHeader");
      Map<String, Object> paramsObj = (Map<String, Object>) responseHeader
              .get("params");
      String status = responseHeader.get("status").toString();
      String message = errorResponse.get("msg").toString();
      String query = paramsObj.get("q").toString();
      String fl = paramsObj.get("fl").toString();

      errorMessage.append("Status: ").append(status).append("\n");
      errorMessage.append("Message: ").append(message).append("\n");
      errorMessage.append("query: ").append(query).append("\n");
      errorMessage.append("fl: ").append(fl).append("\n");

    } catch (ParseException e)
    {
      e.printStackTrace();
    }
    return errorMessage.toString();
  }

  /**
   * Parses the JSON response string from PDB REST API. The response is dynamic
   * hence, only fields specifically requested for in the 'wantedFields'
   * parameter is fetched/processed
   * 
   * @param pdbJsonResponseString
   *          the JSON string to be parsed
   * @param pdbRestRequest
   *          the request object which contains parameters used to process the
   *          JSON string
   * @return
   */
  public static FTSRestResponse parsePDBJsonResponse(
          String pdbJsonResponseString, FTSRestRequest pdbRestRequest)
  {
    return parsePDBJsonResponse(pdbJsonResponseString,
            (Map<String, Object>) null, pdbRestRequest);
  }

  @SuppressWarnings("unchecked")
  public static FTSRestResponse parsePDBJsonResponse(
          String pdbJsonResponseString, Map<String, Object> jsonObj,
          FTSRestRequest pdbRestRequest)
  {
    FTSRestResponse searchResult = new FTSRestResponse();
    List<FTSData> result = null;
    try
    {
      if (jsonObj == null)
      {
        jsonObj = (Map<String, Object>) JSONUtils
                .parse(pdbJsonResponseString);
      }
      Map<String, Object> pdbResponse = (Map<String, Object>) jsonObj
              .get("response");
      String queryTime = ((Map<String, Object>) jsonObj
              .get("responseHeader")).get("QTime").toString();
      int numFound = Integer
              .valueOf(pdbResponse.get("numFound").toString());
      List<Object> docs = (List<Object>) pdbResponse.get("docs");

      result = new ArrayList<FTSData>();
      if (numFound > 0)
      {

        for (Iterator<Object> docIter = docs.iterator(); docIter.hasNext();)
        {
          Map<String, Object> doc = (Map<String, Object>) docIter.next();
          result.add(getFTSData(doc, pdbRestRequest));
        }
      }
      // this is the total number found by the query,
      // rather than the set returned in SearchSummary
      searchResult.setNumberOfItemsFound(numFound);
      searchResult.setResponseTime(queryTime);
      searchResult.setSearchSummary(result);

    } catch (ParseException e)
    {
      e.printStackTrace();
    }
    return searchResult;
  }

  public static FTSData getFTSData(Map<String, Object> pdbJsonDoc,
          FTSRestRequest request)
  {

    String primaryKey = null;

    Object[] summaryRowData;

    SequenceI associatedSequence;

    Collection<FTSDataColumnI> diplayFields = request.getWantedFields();
    SequenceI associatedSeq = request.getAssociatedSequence();
    int colCounter = 0;
    summaryRowData = new Object[(associatedSeq != null)
            ? diplayFields.size() + 1
            : diplayFields.size()];
    if (associatedSeq != null)
    {
      associatedSequence = associatedSeq;
      summaryRowData[0] = associatedSequence;
      colCounter = 1;
    }

    for (FTSDataColumnI field : diplayFields)
    {
      // System.out.println("Field " + field);
      String fieldData = (pdbJsonDoc.get(field.getCode()) == null) ? ""
              : pdbJsonDoc.get(field.getCode()).toString();
      // System.out.println("Field Data : " + fieldData);
      if (field.isPrimaryKeyColumn())
      {
        primaryKey = fieldData;
        summaryRowData[colCounter++] = primaryKey;
      }
      else if (fieldData == null || fieldData.isEmpty())
      {
        summaryRowData[colCounter++] = null;
      }
      else
      {
        try
        {
          summaryRowData[colCounter++] = (field.getDataType()
                  .getDataTypeClass() == Integer.class)
                          ? Integer.valueOf(fieldData)
                          : (field.getDataType()
                                  .getDataTypeClass() == Double.class)
                                          ? Double.valueOf(fieldData)
                                          : sanitiseData(fieldData);
        } catch (Exception e)
        {
          e.printStackTrace();
          System.out.println("offending value:" + fieldData);
        }
      }
    }

    final String primaryKey1 = primaryKey;

    final Object[] summaryRowData1 = summaryRowData;
    return new FTSData()
    {
      @Override
      public Object[] getSummaryData()
      {
        return summaryRowData1;
      }

      @Override
      public Object getPrimaryKey()
      {
        return primaryKey1;
      }

      /**
       * Returns a string representation of this object;
       */
      @Override
      public String toString()
      {
        StringBuilder summaryFieldValues = new StringBuilder();
        for (Object summaryField : summaryRowData1)
        {
          summaryFieldValues.append(
                  summaryField == null ? " " : summaryField.toString())
                  .append("\t");
        }
        return summaryFieldValues.toString();
      }

      /**
       * Returns hash code value for this object
       */
      @Override
      public int hashCode()
      {
        return Objects.hash(primaryKey1, this.toString());
      }

      @Override
      public boolean equals(Object that)
      {
        return this.toString().equals(that.toString());
      }
    };
  }

  private static String sanitiseData(String data)
  {
    String cleanData = data.replaceAll("\\[\"", "").replaceAll("\\]\"", "")
            .replaceAll("\\[", "").replaceAll("\\]", "")
            .replaceAll("\",\"", ", ").replaceAll("\"", "");
    return cleanData;
  }

  @Override
  public String getColumnDataConfigFileName()
  {
    return "/fts/pdb_data_columns.txt";
  }

  public static FTSRestClientI getInstance()
  {
    if (instance == null)
    {
      instance = new PDBFTSRestClient();
    }
    return instance;
  }

  private Collection<FTSDataColumnI> allDefaultDisplayedStructureDataColumns;

  @Override
  public Collection<FTSDataColumnI> getAllDefaultDisplayedStructureDataColumns()
  {
    if (allDefaultDisplayedStructureDataColumns == null
            || allDefaultDisplayedStructureDataColumns.isEmpty())
    {
      allDefaultDisplayedStructureDataColumns = new ArrayList<>();
      allDefaultDisplayedStructureDataColumns
              .addAll(super.getAllDefaultDisplayedFTSDataColumns());
    }
    return allDefaultDisplayedStructureDataColumns;
  }

  @Override
  public String[] getPreferencesColumnsFor(PreferenceSource source)
  {
    String[] columnNames = null;
    switch (source)
    {
    case SEARCH_SUMMARY:
      columnNames = new String[] { "", "Display", "Group" };
      break;
    case STRUCTURE_CHOOSER:
      columnNames = new String[] { "", "Display", "Group" };
      break;
    case PREFERENCES:
      columnNames = new String[] { "PDB Field", "Show in search summary",
          "Show in structure summary" };
      break;
    default:
      break;
    }
    return columnNames;
  }
}
