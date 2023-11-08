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
package jalview.ws;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * Helpful procedures for working with services via HTTPClient
 * 
 * @author jimp
 * 
 */
public class HttpClientUtils
{
  /**
   * do a minimal HTTP post with URL-Encoded parameters passed in the Query
   * string
   * 
   * @param postUrl
   * @param vals
   * @return Reader containing content, if any, or null if no entity returned.
   * @throws IOException
   * @throws ClientProtocolException
   * @throws Exception
   */
  public static BufferedReader doHttpUrlPost(String postUrl,
          List<NameValuePair> vals, int connectionTimeoutMs,
          int readTimeoutMs) throws ClientProtocolException, IOException
  {
    // todo use HttpClient 4.3 or later and class RequestConfig
    HttpParams params = new BasicHttpParams();
    params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
            HttpVersion.HTTP_1_1);
    if (connectionTimeoutMs > 0)
    {
      HttpConnectionParams.setConnectionTimeout(params,
              connectionTimeoutMs);
    }
    if (readTimeoutMs > 0)
    {
      HttpConnectionParams.setSoTimeout(params, readTimeoutMs);
    }
    HttpClient httpclient = new DefaultHttpClient(params);
    HttpPost httppost = new HttpPost(postUrl);
    UrlEncodedFormEntity ue = new UrlEncodedFormEntity(vals, "UTF-8");
    httppost.setEntity(ue);
    HttpResponse response = httpclient.execute(httppost);
    HttpEntity resEntity = response.getEntity();

    if (resEntity != null)
    {
      BufferedReader r = new BufferedReader(
              new InputStreamReader(resEntity.getContent()));
      return r;
    }
    else
    {
      return null;
    }
  }

  public static BufferedReader doHttpMpartFilePost(String postUrl,
          List<NameValuePair> vals, String fparm, File file, String mtype)
          throws ClientProtocolException, IOException
  {
    HttpClient httpclient = new DefaultHttpClient();
    HttpPost httppost = new HttpPost(postUrl);
    MultipartEntity mpe = new MultipartEntity(
            HttpMultipartMode.BROWSER_COMPATIBLE);
    for (NameValuePair nvp : vals)
    {
      mpe.addPart(nvp.getName(), new StringBody(nvp.getValue()));
    }

    FileBody fb = new FileBody(file,
            mtype != null ? mtype : "application/octet-stream");
    mpe.addPart(fparm, fb);
    UrlEncodedFormEntity ue = new UrlEncodedFormEntity(vals, "UTF-8");
    httppost.setEntity(ue);
    HttpResponse response = httpclient.execute(httppost);
    HttpEntity resEntity = response.getEntity();

    if (resEntity != null)
    {
      BufferedReader r = new BufferedReader(
              new InputStreamReader(resEntity.getContent()));
      return r;
    }
    else
    {
      return null;
    }
  }

  public static BufferedReader doHttpMpartInputstreamPost(String postUrl,
          List<NameValuePair> vals, String fparm, String fname,
          InputStream is, String mtype)
          throws ClientProtocolException, IOException
  {
    HttpClient httpclient = new DefaultHttpClient();
    HttpPost httppost = new HttpPost(postUrl);
    MultipartEntity mpe = new MultipartEntity(HttpMultipartMode.STRICT);
    for (NameValuePair nvp : vals)
    {
      mpe.addPart(nvp.getName(), new StringBody(nvp.getValue()));
    }

    InputStreamBody fb = (mtype != null)
            ? new InputStreamBody(is, fname, mtype)
            : new InputStreamBody(is, fname);
    mpe.addPart(fparm, fb);
    UrlEncodedFormEntity ue = new UrlEncodedFormEntity(vals, "UTF-8");
    httppost.setEntity(ue);
    HttpResponse response = httpclient.execute(httppost);
    HttpEntity resEntity = response.getEntity();

    if (resEntity != null)
    {
      BufferedReader r = new BufferedReader(
              new InputStreamReader(resEntity.getContent()));
      return r;
    }
    else
    {
      return null;
    }
  }

  /**
   * do an HTTP GET with URL-Encoded parameters passed in the Query string
   * 
   * @param url
   * @param vals
   * @return Reader containing content, if any, or null if no entity returned.
   * @throws IOException
   * @throws ClientProtocolException
   * @throws Exception
   */
  public static BufferedReader doHttpGet(String url,
          List<NameValuePair> vals, int connectionTimeoutMs,
          int readTimeoutMs) throws ClientProtocolException, IOException
  {
    // todo use HttpClient 4.3 or later and class RequestConfig
    HttpParams params = new BasicHttpParams();
    params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
            HttpVersion.HTTP_1_1);
    if (connectionTimeoutMs > 0)
    {
      HttpConnectionParams.setConnectionTimeout(params,
              connectionTimeoutMs);
    }
    if (readTimeoutMs > 0)
    {
      HttpConnectionParams.setSoTimeout(params, readTimeoutMs);
    }
    boolean first = true;
    for (NameValuePair param : vals)
    {
      if (first)
      {
        url += "?";
      }
      else
      {
        url += "&";
      }
      url += param.getName();
      url += "=";
      url += param.getValue();
    }
    HttpClient httpclient = new DefaultHttpClient(params);
    HttpGet httpGet = new HttpGet(url);
    // UrlEncodedFormEntity ue = new UrlEncodedFormEntity(vals, "UTF-8");
    // httpGet.setEntity(ue);
    HttpResponse response = httpclient.execute(httpGet);
    HttpEntity resEntity = response.getEntity();

    if (resEntity != null)
    {
      BufferedReader r = new BufferedReader(
              new InputStreamReader(resEntity.getContent()));
      return r;
    }
    else
    {
      return null;
    }
  }
}
