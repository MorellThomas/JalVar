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
package jalview.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import javax.ws.rs.HttpMethod;

import jalview.bin.Cache;

public class HttpUtils
{

  /**
   * Returns true if it is possible to open an input stream at the given URL,
   * else false. The input stream is closed.
   * 
   * @param url
   * @return
   */
  public static boolean isValidUrl(String url)
  {
    InputStream is = null;
    try
    {
      is = new URL(url).openStream();
      if (is != null)
      {
        return true;
      }
    } catch (IOException x)
    {
      // MalformedURLException, FileNotFoundException
      return false;
    } finally
    {
      if (is != null)
      {
        try
        {
          is.close();
        } catch (IOException e)
        {
          // ignore
        }
      }
    }
    return false;
  }

  public static boolean startsWithHttpOrHttps(String file)
  {
    return file.startsWith("http://") || file.startsWith("https://");
  }

  /**
   * wrapper to get/post to a URL or check headers
   * 
   * @param url
   * @param ids
   * @param readTimeout
   * @return
   * @throws IOException
   * @throws ProtocolException
   */
  public static boolean checkUrlAvailable(URL url, int readTimeout)
          throws IOException, ProtocolException
  {
    // System.out.println(System.currentTimeMillis() + " " + url);

    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

    connection.setRequestMethod(HttpMethod.HEAD);

    connection.setDoInput(true);

    connection.setUseCaches(false);
    connection.setConnectTimeout(300);
    connection.setReadTimeout(readTimeout);
    return connection.getResponseCode() == 200;
  }

  public static String getUserAgent()
  {
    return getUserAgent(null);
  }

  public static String getUserAgent(String className)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("Jalview");
    sb.append('/');
    sb.append(Cache.getDefault("VERSION", "Unknown"));
    sb.append(" (");
    sb.append(System.getProperty("os.name"));
    sb.append("; ");
    sb.append(System.getProperty("os.arch"));
    sb.append(' ');
    sb.append(System.getProperty("os.name"));
    sb.append(' ');
    sb.append(System.getProperty("os.version"));
    sb.append("; ");
    sb.append("java/");
    sb.append(System.getProperty("java.version"));
    sb.append("; ");
    sb.append("jalview/");
    sb.append(ChannelProperties.getProperty("channel"));
    if (className != null)
    {
      sb.append("; ");
      sb.append(className);
    }
    String installation = Cache.applicationProperties
            .getProperty("INSTALLATION");
    if (installation != null)
    {
      sb.append("; ");
      sb.append(installation);
    }
    sb.append(')');
    sb.append(" help@jalview.org");
    return sb.toString();
  }

}
