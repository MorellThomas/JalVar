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
package jalview.ws.ebi;

import java.util.Locale;

import jalview.datamodel.DBRefSource;
import jalview.util.MessageManager;
import jalview.util.Platform;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class EBIFetchClient
{

  /**
   * Creates a new EBIFetchClient object.
   */
  public EBIFetchClient()
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String[] getSupportedDBs()
  {
    // TODO - implement rest call for dbfetch getSupportedDBs
    throw new Error(MessageManager.getString("error.not_yet_implemented"));
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String[] getSupportedFormats()
  {
    // TODO - implement rest call for dbfetch getSupportedFormats
    throw new Error(MessageManager.getString("error.not_yet_implemented"));
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String[] getSupportedStyles()
  {
    // TODO - implement rest call for dbfetch getSupportedStyles
    throw new Error(MessageManager.getString("error.not_yet_implemented"));
  }

  /**
   * Send an HTTP fetch request to EBI and save the reply in a temporary file.
   * 
   * @param ids
   *          the query formatted as db:query1;query2;query3
   * @param format
   *          the format wanted
   * @param ext
   *          for the temporary file to hold response (without separator)
   * @return the file holding the response
   * @throws OutOfMemoryError
   */

  public File fetchDataAsFile(String ids, String format, String ext)
          throws OutOfMemoryError
  {
    File outFile = null;
    try
    {
      outFile = File.createTempFile("jalview", "." + ext);
      outFile.deleteOnExit();
      fetchData(ids, format, outFile);
      if (outFile.length() == 0)
      {
        outFile.delete();
        return null;
      }
    } catch (Exception ex)
    {
    }
    return outFile;
  }

  /**
   * Fetches queries and either saves the response to a file or returns as
   * string data
   * 
   * @param ids
   * @param format
   * @param outFile
   * @return
   * @throws OutOfMemoryError
   */
  String[] fetchData(String ids, String format, File outFile)
          throws OutOfMemoryError
  {
    StringBuilder querystring = new StringBuilder(ids.length());
    String database = parseIds(ids, querystring);
    if (database == null)
    {
      System.err.println("Invalid Query string : '" + ids + "'");
      System.err.println("Should be of form 'dbname:q1;q2;q3;q4'");
      return null;
    }

    // note: outFile is currently always specified, so return value is null
    String[] rslt = fetchBatch(querystring.toString(), database, format,
            outFile);

    return (rslt != null && rslt.length > 0 ? rslt : null);
  }

  /**
   * Parses ids formatted as dbname:q1;q2;q3, returns the dbname and adds
   * queries as comma-separated items to the querystring. dbname must be
   * specified for at least one queryId. Returns null if a mixture of different
   * dbnames is found (ignoring case).
   * 
   * @param ids
   * @param queryString
   * @return
   */
  static String parseIds(String ids, StringBuilder queryString)
  {
    String database = null;
    StringTokenizer queries = new StringTokenizer(ids, ";");
    boolean appending = queryString.length() > 0;
    while (queries.hasMoreTokens())
    {
      String query = queries.nextToken();
      int p = query.indexOf(':');
      if (p > -1)
      {
        String db = query.substring(0, p);
        if (database != null && !db.equalsIgnoreCase(database))
        {
          /*
           * different databases mixed in together - invalid
           */
          return null;
        }
        database = db;
        query = query.substring(p + 1);
      }
      queryString.append(appending ? "," : "");
      queryString.append(query);
      appending = true;
    }
    return database;
  }

  /**
   * Fetches queries and either saves the response to a file or (if no file
   * specified) returns as string data
   * 
   * @param ids
   * @param database
   * @param format
   * @param outFile
   * @return array of lines from EBI only if outFile is null (which it will not
   *         be)
   * @throws OutOfMemoryError
   */
  String[] fetchBatch(String ids, String database, String format,
          File outFile) throws OutOfMemoryError
  {
    String url = buildUrl(ids, database, format);
    InputStream is = null;
    BufferedReader br = null;
    try
    {
      URL rcall = new URL(url);
      HttpURLConnection conn = (HttpURLConnection) rcall.openConnection();
      int responseCode = conn.getResponseCode();
      if (responseCode == 200)
      {
        is = conn.getInputStream();
        if (outFile != null)
        {
          Platform.streamToFile(is, outFile);
          return null;
        }
        br = new BufferedReader(new InputStreamReader(is));
        String rtn;
        List<String> arl = new ArrayList<>();
        while ((rtn = br.readLine()) != null)
        {
          arl.add(rtn);
        }
        return (String[]) arl.toArray();
      }
      System.err.println(
              "Warning: response code " + responseCode + " for " + url);
    } catch (OutOfMemoryError er)
    {
      System.out.println("OUT OF MEMORY DOWNLOADING QUERY FROM " + database
              + ":\n" + ids);
      throw er;
    } catch (Exception ex)
    {
      if (!ex.getMessage().startsWith(
              "uk.ac.ebi.jdbfetch.exceptions.DbfNoEntryFoundException"))
      {
        System.err.println("Unexpected exception when retrieving from "
                + database + "\nQuery was : '" + ids + "'");
        ex.printStackTrace(System.err);
      }
    } finally
    {
      if (is != null)
      {
        try
        {
          is.close();
        } catch (IOException e)
        {
        }
      }
      if (br != null)
      {
        try
        {
          br.close();
        } catch (IOException e)
        {
        }
      }
    }
    return null;
  }

  static
  {
    Platform.addJ2SDirectDatabaseCall("https://www.ebi.ac.uk/");
  }

  /**
   * Constructs the URL to fetch from
   * 
   * @param ids
   * @param database
   * @param format
   * @return
   */
  static String buildUrl(String ids, String database, String format)
  {
    String url;
    if (database.equalsIgnoreCase(DBRefSource.EMBL)
            || database.equalsIgnoreCase(DBRefSource.EMBLCDS))
    {
      url = "https://www.ebi.ac.uk/ena/browser/api/embl/"
              + ids.toLowerCase(Locale.ROOT) + "?download=true&gzip=true";
    }
    else
    {
      url = "https://www.ebi.ac.uk/Tools/dbfetch/dbfetch/"
              + database.toLowerCase(Locale.ROOT) + "/"
              + ids.toLowerCase(Locale.ROOT)
              + (format != null ? "/" + format : "");
    }
    return url;
  }
}
