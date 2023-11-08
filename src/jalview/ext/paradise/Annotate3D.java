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
package jalview.ext.paradise;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.ParseException;

import jalview.util.JSONUtils;
import jalview.util.MessageManager;
import jalview.ws.HttpClientUtils;

/**
 * simple methods for calling the various paradise RNA tools
 * 
 * @author jimp
 * 
 * @version v1.0 revised from original due to refactoring of
 *          paradise-ubmc.u-strasbg.fr/webservices/annotate3d to
 *          http://arn-ibmc.in2p3.fr/api/compute/2d?tool=rnaview <br/>
 *          See also testing URL from fjossinet:<br/>
 *          http://charn2-ibmc.u-strasbg.fr:8080/api/compute/2d <br/>
 *          If in doubt, check against the REST client at:
 *          https://github.com/fjossinet/RNA-Science
 *          -Toolbox/blob/master/pyrna/restclient.py
 */
public class Annotate3D
{
  // also test with
  // "http://charn2-ibmc.u-strasbg.fr:8080/api/compute/2d";
  private static String twoDtoolsURL = "http://arn-ibmc.in2p3.fr/api/compute/2d?tool=rnaview";

  private static ContentHandler createContentHandler()
  {
    ContentHandler ch = new ContentHandler()
    {

      @Override
      public void startJSON() throws ParseException, IOException
      {
        // TODO Auto-generated method stub

      }

      @Override
      public void endJSON() throws ParseException, IOException
      {
        // TODO Auto-generated method stub

      }

      @Override
      public boolean startObject() throws ParseException, IOException
      {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public boolean endObject() throws ParseException, IOException
      {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public boolean startObjectEntry(String key)
              throws ParseException, IOException
      {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public boolean endObjectEntry() throws ParseException, IOException
      {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public boolean startArray() throws ParseException, IOException
      {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public boolean endArray() throws ParseException, IOException
      {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public boolean primitive(Object value)
              throws ParseException, IOException
      {
        // TODO Auto-generated method stub
        return false;
      }

    };
    return ch;
  }

  public static Iterator<Reader> getRNAMLForPDBFileAsString(String pdbfile)
          throws Exception
  {
    List<NameValuePair> vals = new ArrayList<>();
    vals.add(new BasicNameValuePair("tool", "rnaview"));
    vals.add(new BasicNameValuePair("data", pdbfile));
    vals.add(new BasicNameValuePair("output", "rnaml"));
    // return processJsonResponseFor(HttpClientUtils.doHttpUrlPost(twoDtoolsURL,
    // vals));
    ArrayList<Reader> readers = new ArrayList<>();
    final BufferedReader postResponse = HttpClientUtils
            .doHttpUrlPost(twoDtoolsURL, vals, 0, 0);
    readers.add(postResponse);
    return readers.iterator();

  }

  public static Iterator<Reader> processJsonResponseFor(Reader respons)
          throws Exception
  {
    org.json.simple.parser.JSONParser jp = new org.json.simple.parser.JSONParser();
    try
    {
      final RvalsIterator rvals = new RvalsIterator(respons);
      return rvals;
    } catch (Exception foo)
    {
      throw new Exception(MessageManager.getString(
              "exception.couldnt_parse_responde_from_annotated3d_server"),
              foo);
    }

  }

  public static Iterator<Reader> getRNAMLForPDBId(String pdbid)
          throws Exception
  {
    List<NameValuePair> vals = new ArrayList<>();
    vals.add(new BasicNameValuePair("tool", "rnaview"));
    vals.add(new BasicNameValuePair("pdbid", pdbid));
    vals.add(new BasicNameValuePair("output", "rnaml"));
    java.net.URL geturl = new URL(twoDtoolsURL + "?tool=rnaview&pdbid="
            + pdbid + "&output=rnaml");
    // return processJsonResponseFor(new
    // InputStreamReader(geturl.openStream()));
    ArrayList<Reader> readers = new ArrayList<>();
    readers.add(new InputStreamReader(geturl.openStream()));
    return readers.iterator();
  }

}

class RvalsIterator implements Iterator, AutoCloseable
{
  private Iterator<Object> rvals;

  @SuppressWarnings("unchecked")
  protected RvalsIterator(Reader respons) throws IOException, ParseException
  {
    /*
     * as in 2.11.1 (pre-JalviewJS)
     */
    // final JSONArray responses = (JSONArray) jp.parse(respons);
    // this.rvals = responses.iterator();

    /*
     * as in JalviewJS (with comment that this code is never called)
     */
    this.rvals = ((List<Object>) JSONUtils.parse(respons)).iterator();
  }

  @Override
  public boolean hasNext()
  {
    return rvals.hasNext();
  }

  @Override
  public Reader next()
  {
    // JSONObject val = (JSONObject) rvals.next(); // 2.11.1
    @SuppressWarnings("unchecked")
    Map<String, Object> val = (Map<String, Object>) rvals.next();

    Object sval = null;
    try
    {
      sval = val.get("2D");
    } catch (Exception x)
    {
      x.printStackTrace();
    }

    if (sval == null)
    {
      System.err.println(
              "DEVELOPER WARNING: Annotate3d didn't return a '2D' tag in its response. Consider checking output of server. Response was :"
                      + val.toString());

      sval = "";
    }
    // 2.11.1:
    // return new StringReader(
    // (sval instanceof JSONObject) ? ((JSONObject) sval).toString()
    // : sval.toString());
    return new StringReader(sval.toString());

  }

  @Override
  public void remove()
  {
    throw new Error(
            MessageManager.getString("error.not_implemented_remove"));

  }

  @Override
  protected Object clone() throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException(
            MessageManager.getString("error.not_implemented_clone"));
  }

  @Override
  public boolean equals(Object obj)
  {
    return super.equals(obj);
  }

  @Override
  public void close()
  {
    while (rvals.hasNext())
    {
      rvals.next();
    }
  }
}
