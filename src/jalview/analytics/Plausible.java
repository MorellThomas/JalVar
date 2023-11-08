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
package jalview.analytics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.util.ChannelProperties;
import jalview.util.HttpUtils;

public class Plausible
{
  private static final String USER_AGENT;

  private static final String JALVIEW_ID = "Jalview Desktop";

  private static final String DOMAIN = "jalview.org";

  private static final String CONFIG_API_BASE_URL = "https://www.jalview.org/services/config/analytics/url";

  private static final String DEFAULT_API_BASE_URL = "https://analytics.jalview.org/api/event";

  private static final String API_BASE_URL;

  private static final String clientId;

  public static final String APPLICATION_BASE_URL = "desktop://localhost";

  private List<Map.Entry<String, String>> queryStringValues;

  private List<Map.Entry<String, Object>> jsonObject;

  private List<Map.Entry<String, String>> cookieValues;

  private static boolean ENABLED = false;

  private static boolean DEBUG = true;

  private static Plausible instance = null;

  private static final Map<String, String> defaultProps;

  static
  {
    defaultProps = new HashMap<>();
    defaultProps.put("app_name",
            ChannelProperties.getProperty("app_name") + " Desktop");
    defaultProps.put("version", Cache.getProperty("VERSION"));
    defaultProps.put("build_date",
            Cache.getDefault("BUILD_DATE", "unknown"));
    defaultProps.put("java_version", System.getProperty("java.version"));
    String val = System.getProperty("sys.install4jVersion");
    if (val != null)
    {
      defaultProps.put("install4j_version", val);
    }
    val = System.getProperty("installer_template_version");
    if (val != null)
    {
      defaultProps.put("install4j_template_version", val);
    }
    val = System.getProperty("launcher_version");
    if (val != null)
    {
      defaultProps.put("launcher_version", val);
    }
    defaultProps.put("java_arch",
            System.getProperty("os.arch") + " "
                    + System.getProperty("os.name") + " "
                    + System.getProperty("os.version"));
    defaultProps.put("os", System.getProperty("os.name"));
    defaultProps.put("os_version", System.getProperty("os.version"));
    defaultProps.put("os_arch", System.getProperty("os.arch"));
    String installation = Cache.applicationProperties
            .getProperty("INSTALLATION");
    if (installation != null)
    {
      defaultProps.put("installation", installation);
    }

    // ascertain the API_BASE_URL
    API_BASE_URL = getAPIBaseURL();

    // random clientId to make User-Agent unique (to register analytic)
    clientId = String.format("%08x", new Random().nextInt());

    USER_AGENT = HttpUtils.getUserAgent(
            MethodHandles.lookup().lookupClass().getCanonicalName() + " "
                    + clientId);
  }

  private Plausible()
  {
    this.resetLists();
  }

  public static void setEnabled(boolean b)
  {
    ENABLED = b;
  }

  public void sendEvent(String eventName, String urlString,
          String... propsStrings)
  {
    sendEvent(eventName, urlString, false, propsStrings);
  }

  /**
   * The simplest way to send an analytic event.
   * 
   * @param eventName
   *          The event name. To emulate a webpage view use "pageview" and set a
   *          "url" key/value. See https://plausible.io/docs/events-api
   * @param sendDefaultProps
   *          Flag whether to add the default props about the application.
   * @param propsStrings
   *          Optional multiple Strings in key, value pairs (there should be an
   *          even number of propsStrings) to be set as property of the event.
   *          To emulate a webpage view set "url" as the URL in a "pageview"
   *          event.
   */
  public void sendEvent(String eventName, String urlString,
          boolean sendDefaultProps, String... propsStrings)
  {
    // clear out old lists
    this.resetLists();

    if (!ENABLED)
    {
      Console.debug("Plausible not enabled.");
      return;
    }
    Map<String, String> props = new HashMap<>();

    // add these to all events from this application instance
    if (sendDefaultProps)
    {
      props.putAll(defaultProps);
    }

    // add (and overwrite with) the passed in props
    if (propsStrings != null && propsStrings.length > 0)
    {
      if (propsStrings.length % 2 != 0)
      {
        Console.warn(
                "Cannot addEvent with odd number of propsStrings.  Ignoring the last one.");
      }
      for (int i = 0; i < propsStrings.length - 1; i += 2)
      {
        String key = propsStrings[i];
        String value = propsStrings[i + 1];
        props.put(key, value);
      }
    }

    addJsonValue("domain", DOMAIN);
    addJsonValue("name", eventName);
    StringBuilder eventUrlSb = new StringBuilder(APPLICATION_BASE_URL);
    if (!APPLICATION_BASE_URL.endsWith("/") && !urlString.startsWith("/"))
    {
      eventUrlSb.append("/");
    }
    eventUrlSb.append(urlString);
    addJsonValue("url", eventUrlSb.toString());
    addJsonObject("props", props);
    StringBuilder urlSb = new StringBuilder();
    urlSb.append(API_BASE_URL);
    String qs = buildQueryString();
    if (qs != null && qs.length() > 0)
    {
      urlSb.append('?');
      urlSb.append(qs);
    }
    try
    {
      URL url = new URL(urlSb.toString());
      URLConnection urlConnection = url.openConnection();
      HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
      httpURLConnection.setRequestMethod("POST");
      httpURLConnection.setDoOutput(true);

      String jsonString = buildJson();

      Console.debug(
              "Plausible: HTTP Request is: '" + urlSb.toString() + "'");
      if (DEBUG)
      {
        Console.debug("Plausible: User-Agent is: '" + USER_AGENT + "'");
      }
      Console.debug("Plausible: POSTed JSON is:\n" + jsonString);

      byte[] jsonBytes = jsonString.getBytes(StandardCharsets.UTF_8);
      int jsonLength = jsonBytes.length;

      httpURLConnection.setFixedLengthStreamingMode(jsonLength);
      httpURLConnection.setRequestProperty("Content-Type",
              "application/json");
      httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
      httpURLConnection.connect();
      try (OutputStream os = httpURLConnection.getOutputStream())
      {
        os.write(jsonBytes);
      }
      int responseCode = httpURLConnection.getResponseCode();
      String responseMessage = httpURLConnection.getResponseMessage();

      if (responseCode < 200 || responseCode > 299)
      {
        Console.warn("Plausible connection failed: '" + responseCode + " "
                + responseMessage + "'");
      }
      else
      {
        Console.debug("Plausible connection succeeded: '" + responseCode
                + " " + responseMessage + "'");
      }

      if (DEBUG)
      {
        BufferedReader br = new BufferedReader(new InputStreamReader(
                (httpURLConnection.getInputStream())));
        StringBuilder sb = new StringBuilder();
        String response;
        while ((response = br.readLine()) != null)
        {
          sb.append(response);
        }
        String body = sb.toString();
        Console.debug("Plausible response content:\n" + body);
      }
    } catch (MalformedURLException e)
    {
      Console.debug(
              "Somehow the Plausible BASE_URL and queryString is malformed: '"
                      + urlSb.toString() + "'",
              e);
      return;
    } catch (IOException e)
    {
      Console.debug("Connection to Plausible BASE_URL '" + API_BASE_URL
              + "' failed.", e);
    } catch (ClassCastException e)
    {
      Console.debug(
              "Couldn't cast URLConnection to HttpURLConnection in Plausible.",
              e);
    }
  }

  private void addJsonObject(String key, Map<String, String> map)
  {
    List<Map.Entry<String, ? extends Object>> list = new ArrayList<>();
    for (String k : map.keySet())
    {
      list.add(stringEntry(k, map.get(k)));
    }
    addJsonObject(key, list);

  }

  private void addJsonObject(String key,
          List<Map.Entry<String, ? extends Object>> object)
  {
    jsonObject.add(objectEntry(key, object));
  }

  private void addJsonValues(String key, List<Object> values)
  {
    jsonObject.add(objectEntry(key, values));
  }

  private void addJsonValue(String key, String value)
  {
    jsonObject.add(objectEntry(key, value));
  }

  private void addJsonValue(String key, int value)
  {
    jsonObject.add(objectEntry(key, Integer.valueOf(value)));
  }

  private void addJsonValue(String key, boolean value)
  {
    jsonObject.add(objectEntry(key, Boolean.valueOf(value)));
  }

  private void addQueryStringValue(String key, String value)
  {
    queryStringValues.add(stringEntry(key, value));
  }

  private void addCookieValue(String key, String value)
  {
    cookieValues.add(stringEntry(key, value));
  }

  private void resetLists()
  {
    jsonObject = new ArrayList<>();
    queryStringValues = new ArrayList<>();
    cookieValues = new ArrayList<>();
  }

  public static Plausible getInstance()
  {
    if (instance == null)
    {
      instance = new Plausible();
    }
    return instance;
  }

  public static void reset()
  {
    getInstance().resetLists();
  }

  private String buildQueryString()
  {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> entry : queryStringValues)
    {
      if (sb.length() > 0)
      {
        sb.append('&');
      }
      try
      {
        sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
      } catch (UnsupportedEncodingException e)
      {
        sb.append(entry.getKey());
      }
      sb.append('=');
      try
      {
        sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
      } catch (UnsupportedEncodingException e)
      {
        sb.append(entry.getValue());
      }
    }
    return sb.toString();
  }

  private void buildCookieHeaders()
  {
    // TODO not needed yet
  }

  private String buildJson()
  {
    StringBuilder sb = new StringBuilder();
    addJsonObject(sb, 0, jsonObject);
    return sb.toString();
  }

  private void addJsonObject(StringBuilder sb, int indent,
          List<Map.Entry<String, Object>> entries)
  {
    indent(sb, indent);
    sb.append('{');
    newline(sb);
    Iterator<Map.Entry<String, Object>> entriesI = entries.iterator();
    while (entriesI.hasNext())
    {
      Map.Entry<String, Object> entry = entriesI.next();
      String key = entry.getKey();
      // TODO sensibly escape " characters in key
      Object value = entry.getValue();
      indent(sb, indent + 1);
      sb.append('"').append(quoteEscape(key)).append('"').append(':');
      space(sb);
      if (value != null && value instanceof List)
      {
        newline(sb);
      }
      addJsonValue(sb, indent + 2, value);
      if (entriesI.hasNext())
      {
        sb.append(',');
      }
      newline(sb);
    }
    indent(sb, indent);
    sb.append('}');
  }

  private void addJsonValue(StringBuilder sb, int indent, Object value)
  {
    if (value == null)
    {
      return;
    }
    try
    {
      if (value instanceof Map.Entry)
      {
        Map.Entry<String, Object> entry = (Map.Entry<String, Object>) value;
        List<Map.Entry<String, Object>> object = new ArrayList<>();
        object.add(entry);
        addJsonObject(sb, indent, object);
      }
      else if (value instanceof List)
      {
        // list of Map.Entries or list of values?
        List<Object> valueList = (List<Object>) value;
        if (valueList.size() > 0 && valueList.get(0) instanceof Map.Entry)
        {
          // entries
          // indent(sb, indent);
          List<Map.Entry<String, Object>> entryList = (List<Map.Entry<String, Object>>) value;
          addJsonObject(sb, indent, entryList);
        }
        else
        {
          // values
          indent(sb, indent);
          sb.append('[');
          newline(sb);
          Iterator<Object> valueListI = valueList.iterator();
          while (valueListI.hasNext())
          {
            Object v = valueListI.next();
            addJsonValue(sb, indent + 1, v);
            if (valueListI.hasNext())
            {
              sb.append(',');
            }
            newline(sb);
          }
          indent(sb, indent);
          sb.append("]");
        }
      }
      else if (value instanceof String)
      {
        sb.append('"').append(quoteEscape((String) value)).append('"');
      }
      else if (value instanceof Integer)
      {
        sb.append(((Integer) value).toString());
      }
      else if (value instanceof Boolean)
      {
        sb.append('"').append(((Boolean) value).toString()).append('"');
      }
    } catch (ClassCastException e)
    {
      Console.debug(
              "Could not deal with type of json Object " + value.toString(),
              e);
    }
  }

  private static String quoteEscape(String s)
  {
    if (s == null)
    {
      return null;
    }
    // this escapes quotation marks (") that aren't already escaped (in the
    // string) ready to go into a quoted JSON string value
    return s.replaceAll("((?<!\\\\)(?:\\\\{2})*)\"", "$1\\\\\"");
  }

  private static void prettyWhitespace(StringBuilder sb, String whitespace,
          int repeat)
  {
    // only add whitespace if we're in DEBUG mode
    if (!Console.getLogger().isDebugEnabled())
    {
      return;
    }
    if (repeat >= 0 && whitespace != null)
    {
      // sb.append(whitespace.repeat(repeat));
      sb.append(String.join("", Collections.nCopies(repeat, whitespace)));

    }
    else
    {
      sb.append(whitespace);
    }
  }

  private static void indent(StringBuilder sb, int indent)
  {
    prettyWhitespace(sb, "  ", indent);
  }

  private static void newline(StringBuilder sb)
  {
    prettyWhitespace(sb, "\n", -1);
  }

  private static void space(StringBuilder sb)
  {
    prettyWhitespace(sb, " ", -1);
  }

  protected static Map.Entry<String, Object> objectEntry(String s, Object o)
  {
    return new AbstractMap.SimpleEntry<String, Object>(s, o);
  }

  protected static Map.Entry<String, String> stringEntry(String s, String v)
  {
    return new AbstractMap.SimpleEntry<String, String>(s, v);
  }

  private static String getAPIBaseURL()
  {
    try
    {
      URL url = new URL(CONFIG_API_BASE_URL);
      URLConnection urlConnection = url.openConnection();
      HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
      httpURLConnection.setRequestMethod("GET");
      httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
      httpURLConnection.setConnectTimeout(5000);
      httpURLConnection.setReadTimeout(3000);
      httpURLConnection.connect();
      int responseCode = httpURLConnection.getResponseCode();
      String responseMessage = httpURLConnection.getResponseMessage();

      if (responseCode < 200 || responseCode > 299)
      {
        Console.warn("Config URL connection to '" + CONFIG_API_BASE_URL
                + "' failed: '" + responseCode + " " + responseMessage
                + "'");
      }

      BufferedReader br = new BufferedReader(
              new InputStreamReader((httpURLConnection.getInputStream())));
      StringBuilder sb = new StringBuilder();
      String response;
      while ((response = br.readLine()) != null)
      {
        sb.append(response);
      }
      if (sb.length() > 7 && sb.substring(0, 5).equals("https"))
      {
        return sb.toString();
      }

    } catch (MalformedURLException e)
    {
      Console.debug("Somehow the config URL is malformed: '"
              + CONFIG_API_BASE_URL + "'", e);
    } catch (IOException e)
    {
      Console.debug("Connection to Plausible BASE_URL '" + API_BASE_URL
              + "' failed.", e);
    } catch (ClassCastException e)
    {
      Console.debug(
              "Couldn't cast URLConnection to HttpURLConnection in Plausible.",
              e);
    }
    return DEFAULT_API_BASE_URL;
  }
}
