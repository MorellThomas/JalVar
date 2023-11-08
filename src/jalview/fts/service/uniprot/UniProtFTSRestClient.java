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

package jalview.fts.service.uniprot;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.fts.api.FTSData;
import jalview.fts.api.FTSDataColumnI;
import jalview.fts.core.FTSRestClient;
import jalview.fts.core.FTSRestRequest;
import jalview.fts.core.FTSRestResponse;
import jalview.util.ChannelProperties;
import jalview.util.MessageManager;
import jalview.util.Platform;

/*
 * 2022-07-20 bsoares
 * See https://issues.jalview.org/browse/JAL-4036
 * The new Uniprot API is not dissimilar to the old one, but has some important changes.
 * Some group names have changed slightly, some old groups have gone and there are quite a few new groups.
 * 
 * Most changes are mappings of old column ids to new field ids. There are a handful of old
 * columns not mapped to new fields, and new fields without an old column.
 * [aside: not all possible columns were listed in the resources/fts/uniprot_data_columns.txt file.
 * These were presumably additions after the file was created]
 * For existing/mapped fields, the same preferences found in the resource file have been migrated to
 * the new file with the new field name, id and group.
 * 
 * The new mapped groups and files are stored and read from resources/fts/uniprot_data_columns-2022.txt.
 * 
 * There is now no "sort" query string parameter.
 * 
 * See https://www.uniprot.org/help/api_queries
 * 
 * SIGNIFICANT CHANGE: Pagination is no longer performed using a record offset, but with a "cursor"
 * query string parameter that is not really a cursor.  The value is an opaque string that is passed (or
 * rather a whole URL is passed) in the "Link" header of the HTTP response of the previous page.
 * Where such a link is passed it is put into the cursors ArrayList.
 * There are @Overridden methods in UniprotFTSPanel.
 */

public class UniProtFTSRestClient extends FTSRestClient
{
  private static final String DEFAULT_UNIPROT_DOMAIN = "https://rest.uniprot.org";

  private static final String USER_AGENT = ChannelProperties
          .getProperty("app_name", "Jalview") + " "
          + Cache.getDefault("VERSION", "Unknown") + " "
          + MethodHandles.lookup().lookupClass() + " help@jalview.org";

  static
  {
    Platform.addJ2SDirectDatabaseCall(DEFAULT_UNIPROT_DOMAIN);
  }

  private static UniProtFTSRestClient instance = null;

  public final String uniprotSearchEndpoint;

  public UniProtFTSRestClient()
  {
    super();
    this.clearCursors();
    uniprotSearchEndpoint = Cache.getDefault("UNIPROT_2022_DOMAIN",
            DEFAULT_UNIPROT_DOMAIN) + "/uniprotkb/search";
  }

  @SuppressWarnings("unchecked")
  @Override
  public FTSRestResponse executeRequest(FTSRestRequest uniprotRestRequest)
          throws Exception
  {
    return executeRequest(uniprotRestRequest, null);
  }

  public FTSRestResponse executeRequest(FTSRestRequest uniprotRestRequest,
          String cursor) throws Exception
  {
    try
    {
      String wantedFields = getDataColumnsFieldsAsCommaDelimitedString(
              uniprotRestRequest.getWantedFields());
      int responseSize = (uniprotRestRequest.getResponseSize() == 0)
              ? getDefaultResponsePageSize()
              : uniprotRestRequest.getResponseSize();

      int offSet = uniprotRestRequest.getOffSet();
      String query;
      if (isAdvancedQuery(uniprotRestRequest.getSearchTerm()))
      {
        query = uniprotRestRequest.getSearchTerm();
      }
      else
      {
        query = uniprotRestRequest.getFieldToSearchBy().equalsIgnoreCase(
                "Search All") ? uniprotRestRequest.getSearchTerm()
                        // + " or mnemonic:"
                        // + uniprotRestRequest.getSearchTerm()
                        : uniprotRestRequest.getFieldToSearchBy() + ":"
                                + uniprotRestRequest.getSearchTerm();
      }

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
        // Java only
        client = Client.create(new DefaultClientConfig());
        clientResponseClass = ClientResponse.class;
      }

      WebResource webResource = null;
      webResource = client.resource(uniprotSearchEndpoint)
              .queryParam("format", "tsv")
              .queryParam("fields", wantedFields)
              .queryParam("size", String.valueOf(responseSize))
              /* 2022 new api has no "sort"
               * .queryParam("sort", "score")
               */
              .queryParam("query", query);
      if (offSet != 0 && cursor != null && cursor.length() > 0)
      // 2022 new api does not do pagination with an offset, it requires a
      // "cursor" parameter with a key (given for the next page).
      // (see https://www.uniprot.org/help/pagination)
      {
        webResource = webResource.queryParam("cursor", cursor);
      }
      Console.debug(
              "Uniprot FTS Request: " + webResource.getURI().toString());
      // Execute the REST request
      WebResource.Builder wrBuilder = webResource
              .accept(MediaType.TEXT_PLAIN);
      if (!Platform.isJS())
      /**
       * Java only
       * 
       * @j2sIgnore
       */
      {
        wrBuilder.header("User-Agent", USER_AGENT);
      }
      ClientResponse clientResponse = wrBuilder.get(clientResponseClass);

      if (!Platform.isJS())
      /**
       * Java only
       * 
       * @j2sIgnore
       */
      {
        if (clientResponse.getHeaders().containsKey("Link"))
        {
          // extract the URL from the 'Link: <URL>; ref="stuff"' header
          String linkHeader = clientResponse.getHeaders().get("Link")
                  .get(0);
          if (linkHeader.indexOf("<") > -1)
          {
            String temp = linkHeader.substring(linkHeader.indexOf("<") + 1);
            if (temp.indexOf(">") > -1)
            {
              String nextUrl = temp.substring(0, temp.indexOf(">"));
              // then get the cursor value from the query string parameters
              String nextCursor = getQueryParam("cursor", nextUrl);
              setCursor(cursorPage + 1, nextCursor);
            }
          }
        }
      }

      String uniProtTabDelimittedResponseString = clientResponse
              .getEntity(String.class);
      // Make redundant objects eligible for garbage collection to conserve
      // memory
      // System.out.println(">>>>> response : "
      // + uniProtTabDelimittedResponseString);
      if (clientResponse.getStatus() != 200)
      {
        String errorMessage = getMessageByHTTPStatusCode(
                clientResponse.getStatus(), "Uniprot");
        throw new Exception(errorMessage);

      }
      // new Uniprot API is not including a "X-Total-Results" header when there
      // are 0 results
      List<String> resultsHeaders = clientResponse.getHeaders()
              .get("X-Total-Results");
      int xTotalResults = 0;
      if (Platform.isJS())
      {
        xTotalResults = 1;
      }
      else if (resultsHeaders != null && resultsHeaders.size() >= 1)
      {
        xTotalResults = Integer.valueOf(resultsHeaders.get(0));
      }
      clientResponse = null;
      client = null;
      return parseUniprotResponse(uniProtTabDelimittedResponseString,
              uniprotRestRequest, xTotalResults);
    } catch (Exception e)
    {
      Console.warn("Problem with the query: " + e.getMessage());
      Console.debug("Exception stacktrace:", e);
      String exceptionMsg = e.getMessage();
      if (exceptionMsg.contains("SocketException"))
      {
        // No internet connection
        throw new Exception(MessageManager.getString(
                "exception.unable_to_detect_internet_connection"));
      }
      else if (exceptionMsg.contains("UnknownHostException"))
      {
        // The server 'http://www.uniprot.org' is unreachable
        throw new Exception(MessageManager.formatMessage(
                "exception.fts_server_unreachable", "Uniprot"));
      }
      else
      {
        throw e;
      }
    }
  }

  public boolean isAdvancedQuery(String query)
  {
    if (query.contains(" AND ") || query.contains(" OR ")
            || query.contains(" NOT ") || query.contains(" ! ")
            || query.contains(" || ") || query.contains(" && ")
            || query.contains(":") || query.contains("-"))
    {
      return true;
    }
    return false;
  }

  public FTSRestResponse parseUniprotResponse(
          String uniProtTabDelimittedResponseString,
          FTSRestRequest uniprotRestRequest, int xTotalResults)
  {
    FTSRestResponse searchResult = new FTSRestResponse();
    List<FTSData> result = null;
    if (uniProtTabDelimittedResponseString == null
            || uniProtTabDelimittedResponseString.trim().isEmpty())
    {
      searchResult.setNumberOfItemsFound(0);
      return searchResult;
    }
    String[] foundDataRow = uniProtTabDelimittedResponseString.split("\n");
    if (foundDataRow != null && foundDataRow.length > 0)
    {
      result = new ArrayList<>();
      boolean firstRow = true;
      for (String dataRow : foundDataRow)
      {
        // The first data row is usually the header data. This should be
        // filtered out from the rest of the data See: JAL-2485
        if (firstRow)
        {
          firstRow = false;
          continue;
        }
        // System.out.println(dataRow);
        result.add(getFTSData(dataRow, uniprotRestRequest));
      }
      searchResult.setNumberOfItemsFound(xTotalResults);
      searchResult.setSearchSummary(result);
    }
    return searchResult;
  }

  // /**
  // * Takes a collection of FTSDataColumnI and converts its 'code' values into
  // a
  // * tab delimited string.
  // *
  // * @param dataColumnFields
  // * the collection of FTSDataColumnI to process
  // * @return the generated comma delimited string from the supplied
  // * FTSDataColumnI collection
  // */
  // private String getDataColumnsFieldsAsTabDelimitedString(
  // Collection<FTSDataColumnI> dataColumnFields)
  // {
  // String result = "";
  // if (dataColumnFields != null && !dataColumnFields.isEmpty())
  // {
  // StringBuilder returnedFields = new StringBuilder();
  // for (FTSDataColumnI field : dataColumnFields)
  // {
  // if (field.getName().equalsIgnoreCase("Uniprot Id"))
  // {
  // returnedFields.append("\t").append("Entry");
  // }
  // else
  // {
  // returnedFields.append("\t").append(field.getName());
  // }
  // }
  // returnedFields.deleteCharAt(0);
  // result = returnedFields.toString();
  // }
  // return result;
  // }

  public static FTSData getFTSData(String tabDelimittedDataStr,
          FTSRestRequest request)
  {
    String primaryKey = null;

    Object[] summaryRowData;

    Collection<FTSDataColumnI> diplayFields = request.getWantedFields();
    int colCounter = 0;
    summaryRowData = new Object[diplayFields.size()];
    String[] columns = tabDelimittedDataStr.split("\t");
    for (FTSDataColumnI field : diplayFields)
    {
      try
      {
        String fieldData = columns[colCounter];
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
                            ? Integer.valueOf(fieldData.replace(",", ""))
                            : (field.getDataType()
                                    .getDataTypeClass() == Double.class)
                                            ? Double.valueOf(fieldData)
                                            : fieldData;
          } catch (Exception e)
          {
            e.printStackTrace();
            System.out.println("offending value:" + fieldData);
          }
        }
      } catch (Exception e)
      {
        // e.printStackTrace();
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

  public static UniProtFTSRestClient getInstance()
  {
    if (instance == null)
    {
      instance = new UniProtFTSRestClient();
    }
    return instance;
  }

  @Override
  public String getColumnDataConfigFileName()
  {
    return "/fts/uniprot_data_columns-2022.txt";
  }

  /* 2022-07-20 bsoares
   * used for the new API "cursor" pagination. See https://www.uniprot.org/help/pagination
   */
  private ArrayList<String> cursors;

  private int cursorPage = 0;

  protected int getCursorPage()
  {
    return cursorPage;
  }

  protected void setCursorPage(int i)
  {
    cursorPage = i;
  }

  protected void setPrevCursorPage()
  {
    if (cursorPage > 0)
      cursorPage--;
  }

  protected void setNextCursorPage()
  {
    cursorPage++;
  }

  protected void clearCursors()
  {
    cursors = new ArrayList(10);
  }

  protected String getCursor(int i)
  {
    return cursors.get(i);
  }

  protected String getNextCursor()
  {
    if (cursors.size() < cursorPage + 2)
      return null;
    return cursors.get(cursorPage + 1);
  }

  protected String getPrevCursor()
  {
    if (cursorPage == 0)
      return null;
    return cursors.get(cursorPage - 1);
  }

  protected void setCursor(int i, String c)
  {
    cursors.ensureCapacity(i + 1);
    while (cursors.size() <= i)
    {
      cursors.add(null);
    }
    cursors.set(i, c);
    Console.debug(
            "Set UniprotFRSRestClient cursors[" + i + "] to '" + c + "'");
    // cursors.add(c);
  }

  public static String getQueryParam(String param, String u)
  {
    if (param == null || u == null)
      return null;
    try
    {
      URL url = new URL(u);
      String[] kevs = url.getQuery().split("&");
      for (int j = 0; j < kevs.length; j++)
      {
        String[] kev = kevs[j].split("=", 2);
        if (param.equals(kev[0]))
        {
          return kev[1];
        }
      }
    } catch (MalformedURLException e)
    {
      Console.warn("Could not obtain next page 'cursor' value from 'u");
    }
    return null;
  }
}