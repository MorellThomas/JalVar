/*
 * Jalview - A Sequence Alignment Editor and Viewer (2.11.3.2)
 * Copyright (C) 2023 The Jalview Authors
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
package jalview.fts.service.threedbeacons;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import jalview.fts.core.FTSDataColumnPreferences.PreferenceSource;
import jalview.fts.core.FTSRestClient;
import jalview.fts.core.FTSRestRequest;
import jalview.fts.core.FTSRestResponse;
import jalview.util.JSONUtils;
import jalview.util.MessageManager;
import jalview.util.Platform;

public class TDBeaconsFTSRestClient extends FTSRestClient
        implements StructureFTSRestClientI
{
  /**
   * production server URI
   */
  private static String TDB_PROD_API = "https://www.ebi.ac.uk/pdbe/pdbe-kb/3dbeacons/api/uniprot/summary/";

  /**
   * dev server URI
   */
  private static String TDB_DEV_API = "https://wwwdev.ebi.ac.uk/pdbe/pdbe-kb/3dbeacons/api/uniprot/summary/";

  private static String DEFAULT_THREEDBEACONS_DOMAIN = TDB_PROD_API;

  public static FTSRestClientI instance = null;

  protected TDBeaconsFTSRestClient()
  {
  }

  @SuppressWarnings("unchecked")
  @Override
  public FTSRestResponse executeRequest(FTSRestRequest tdbRestRequest)
          throws Exception
  {
    try
    {
      String query = tdbRestRequest.getSearchTerm();
      Client client;
      Class<ClientResponse> clientResponseClass;
      if (Platform.isJS())
      {
        // JavaScript only
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
      webResource = client.resource(DEFAULT_THREEDBEACONS_DOMAIN + query);

      URI uri = webResource.getURI();
      jalview.bin.Console.outPrintln(uri.toString());

      // Execute the REST request
      ClientResponse clientResponse;
      if (isMocked())
      {
        clientResponse = null;
      }
      else
      {
        clientResponse = webResource.accept(MediaType.APPLICATION_JSON)
                .get(clientResponseClass);
      }

      // Get the JSON string from the response object or directly from the
      // client (JavaScript)
      Map<String, Object> jsonObj = null;
      String responseString = null;

      // Check the response status and report exception if one occurs
      int responseStatus = isMocked()
              ? (mockQueries.containsKey(query) ? 200 : 404)
              : clientResponse.getStatus();
      switch (responseStatus)
      {
      // if success
      case 200:
        if (Platform.isJS())
        {
          jsonObj = clientResponse.getEntity(Map.class);
        }
        else
        {
          responseString = isMocked() ? mockQueries.get(query)
                  : clientResponse.getEntity(String.class);
        }
        break;
      case 400:
        throw new Exception(parseJsonExceptionString(responseString));
      case 404:
        return emptyTDBeaconsJsonResponse();
      default:
        throw new Exception(
                getMessageByHTTPStatusCode(responseStatus, "3DBeacons"));
      }
      // Process the response and return the result to the caller.
      return parseTDBeaconsJsonResponse(responseString, jsonObj,
              tdbRestRequest);
    } catch (Exception e)
    {
      String exceptionMsg = e.getMessage();
      if (exceptionMsg != null)
      {
        if (exceptionMsg.contains("SocketException"))
        {
          // No internet connection
          throw new Exception(MessageManager.getString(
                  "exception.unable_to_detect_internet_connection"));
        }
        else if (exceptionMsg.contains("UnknownHostException"))
        {
          // The server is unreachable
          throw new Exception(MessageManager.formatMessage(
                  "exception.fts_server_unreachable", "3DB Hub"));
        }
      }
      throw e;

    }

  }

  /**
   * returns response for when the 3D-Beacons service doesn't have a record for
   * the given query - in 2.11.2 this triggers a failover to the PDBe FTS
   * 
   * @return null
   */
  private FTSRestResponse emptyTDBeaconsJsonResponse()
  {
    return null;
  }

  public String setSearchTerm(String term)
  {
    return term;
  }

  public static FTSRestResponse parseTDBeaconsJsonResponse(
          String tdbJsonResponseString, FTSRestRequest tdbRestRequest)
  {
    return parseTDBeaconsJsonResponse(tdbJsonResponseString,
            (Map<String, Object>) null, tdbRestRequest);
  }

  @SuppressWarnings("unchecked")
  public static FTSRestResponse parseTDBeaconsJsonResponse(
          String tdbJsonResponseString, Map<String, Object> jsonObj,
          FTSRestRequest tdbRestRequest)
  {
    FTSRestResponse searchResult = new FTSRestResponse();
    List<FTSData> result = null;

    try
    {
      if (jsonObj == null)
      {
        jsonObj = (Map<String, Object>) JSONUtils
                .parse(tdbJsonResponseString);
      }

      Object uniprot_entry = jsonObj.get("uniprot_entry");
      // TODO: decide if anything from uniprot_entry needs to be reported via
      // the FTSRestResponse object
      // Arnaud added seqLength = (Long) ((Map<String, Object>)
      // jsonObj.get("uniprot_entry")).get("sequence_length");

      List<Object> structures = (List<Object>) jsonObj.get("structures");
      result = new ArrayList<>();

      int numFound = 0;
      for (Iterator<Object> strucIter = structures.iterator(); strucIter
              .hasNext();)
      {
        Map<String, Object> structure = (Map<String, Object>) strucIter
                .next();
        result.add(getFTSData(structure, tdbRestRequest));
        numFound++;
      }

      searchResult.setNumberOfItemsFound(numFound);
      searchResult.setSearchSummary(result);

    } catch (ParseException e)
    {
      e.printStackTrace();
    }
    return searchResult;
  }

  private static FTSData getFTSData(
          Map<String, Object> tdbJsonStructureSummary,
          FTSRestRequest tdbRequest)
  {
    String primaryKey = null;
    Object[] summaryRowData;

    SequenceI associatedSequence;

    Collection<FTSDataColumnI> displayFields = tdbRequest.getWantedFields();
    SequenceI associatedSeq = tdbRequest.getAssociatedSequence();
    int colCounter = 0;
    summaryRowData = new Object[(associatedSeq != null)
            ? displayFields.size() + 1
            : displayFields.size()];
    if (associatedSeq != null)
    {
      associatedSequence = associatedSeq;
      summaryRowData[0] = associatedSequence;
      colCounter = 1;
    }
    Map<String, Object> tdbJsonStructure = (Map<String, Object>) tdbJsonStructureSummary
            .get("summary");
    for (FTSDataColumnI field : displayFields)
    {
      String fieldData = (tdbJsonStructure.get(field.getCode()) == null)
              ? " "
              : tdbJsonStructure.get(field.getCode()).toString();
      // jalview.bin.Console.outPrintln("Field : " + field + " Data : " +
      // fieldData);
      if (field.isPrimaryKeyColumn())
      {
        primaryKey = fieldData;
        summaryRowData[colCounter++] = primaryKey;
      }
      else if (fieldData == null || fieldData.trim().isEmpty())
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
                                          : fieldData;
        } catch (Exception e)
        {
          // e.printStackTrace();
          jalview.bin.Console
                  .outPrintln("offending value:" + fieldData + fieldData);
        }
      }
    }
    final String primaryKey1 = primaryKey;
    final Object[] summaryRowData1 = summaryRowData;

    return new TDB_FTSData(primaryKey, tdbJsonStructure, summaryRowData1);
  }

  // private static FTSData getFTSData(Map<String, Object> doc,
  // FTSRestRequest tdbRestRequest)
  // {
  // String primaryKey = null;
  //
  // Object[] summaryRowData;
  //
  // Collection<FTSDataColumnI> displayFields =
  // tdbRestRequest.getWantedFields();
  // int colCounter = 0;
  // summaryRowData = new Object[displayFields.size() + 1];
  //
  // return null;
  // }

  private String parseJsonExceptionString(String jsonErrorString)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getColumnDataConfigFileName()
  {
    return "/fts/tdbeacons_data_columns.txt";
  }

  public static FTSRestClientI getInstance()
  {
    if (instance == null)
    {
      instance = new TDBeaconsFTSRestClient();
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
      columnNames = new String[] { "3DB Beacons Field",
          "Show in search summary", "Show in structure summary" };
      break;
    default:
      break;
    }
    return columnNames;
  }
}
