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
package jalview.ws.rest;

import jalview.bin.Console;
import jalview.io.FileParse;
import jalview.io.packed.DataProvider;
import jalview.io.packed.DataProvider.JvDataType;
import jalview.io.packed.ParsePackedSet;
import jalview.io.packed.SimpleDataProvider;
import jalview.util.MessageManager;
import jalview.ws.io.mime.JalviewMimeContentHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.MimeStreamParser;

/**
 * data source instantiated from the response of an httpclient request.
 * 
 * @author JimP
 * 
 */

public class HttpResultSet extends FileParse implements AutoCloseable
{

  private HttpRequestBase cachedRequest;

  /**
   * when set, indicates that en can be recreated by repeating the HttpRequest
   * in cachedRequest
   */
  boolean repeatable = false;

  /**
   * response that is to be parsed as jalview input data
   */
  private HttpEntity en = null;

  /**
   * (sub)job that produced this result set.
   */
  private RestJob restJob;

  public HttpResultSet(RestJob rj, HttpResponse con, HttpRequestBase req)
          throws IOException
  {
    super();
    setDataName(rj.getJobId() + " Part " + rj.getJobnum());
    restJob = rj;
    cachedRequest = req;
    initDataSource(con);
  }

  /**
   * construct a set of dataproviders to parse a result set from this service
   * 
   * @param resSet
   * @return
   */
  public List<DataProvider> createResultDataProviders()
  {
    List<DataProvider> dp = new ArrayList<>();
    for (JvDataType type : restJob.rsd.getResultDataTypes())
    {
      dp.add(new SimpleDataProvider(type, this, null));
    }
    return dp;
  }

  /**
   * parses the results of the service output.
   * 
   * @return the result of ParsePackedSet.getAlignment()
   * @throws Exception
   * @throws Error
   */
  public Object[] parseResultSet() throws Exception, Error
  {
    List<DataProvider> dp = new ArrayList<>();
    Object[] results = null;

    if (en == null)
    {
      throw new Error(MessageManager.getString(
              "error.implementation_error_need_to_have_httpresponse"));
    }
    jalview.io.packed.JalviewDataset ds = restJob.newJalviewDataset();
    // Decide how we deal with content.
    if (en instanceof MultipartEntity)
    {
      // Multipart messages should be properly typed, so we parse them as we go.
      MultipartEntity mpe = (MultipartEntity) en;
      // multipart
      JalviewMimeContentHandler handler = new JalviewMimeContentHandler(ds);
      MimeStreamParser parser = new MimeStreamParser();
      parser.setContentHandler(handler);
      try
      {
        parser.parse(mpe.getContent());
      } catch (MimeException me)
      {
        error = true;
        errormessage = "Couldn't parse message from web service.";
        Console.warn("Failed to parse MIME multipart content", me);
        en.consumeContent();
      }
      return new ParsePackedSet().getAlignment(ds,
              handler.getJalviewDataProviders());
    }
    else
    {
      // Need to use hints from rest service description.
      dp = createResultDataProviders();
      ParsePackedSet pps = new ParsePackedSet();
      return pps.getAlignment(ds, dp);
    }
  }

  private void initDataSource(HttpResponse con) throws IOException
  {
    en = con.getEntity();
    repeatable = en.isRepeatable();

    if (!(en instanceof MultipartEntity))
    {
      // assume content is simple text stream that can be read from
      String enc = (en.getContentEncoding() == null) ? null
              : en.getContentEncoding().getValue();
      if (en.getContentType() != null)
      {
        Console.debug("Result Type: " + en.getContentType().toString());
      }
      else
      {
        Console.debug("No Result Type Specified.");
      }
      if (enc == null || enc.length() < 1)
      {
        Console.debug("Assuming 'Default' Result Encoding.");
      }
      else
      {
        Console.debug("Result Encoded as : " + enc);
      }
      // attempt to identify file and construct an appropriate DataSource
      // identifier for it.
      // try to parse
      // Mime-Multipart or single content type will be expected.
      // if (enc.equals(org.apache.http.client.utils.)))
      InputStreamReader br = null;
      try
      {
        br = (enc != null) ? new InputStreamReader(en.getContent(), enc)
                : new InputStreamReader(en.getContent());
      } catch (UnsupportedEncodingException e)
      {
        Console.error("Can't handle encoding '" + enc
                + "' for response from webservice.", e);
        en.consumeContent();
        error = true;
        errormessage = "Can't handle encoding for response from webservice";
        return;
      }
      if (br != null)
      {
        dataIn = new BufferedReader(br);
        error = false;
      }
    }
  }

  @Override
  public void close()
  {
    dataIn = null;
    cachedRequest = null;
    try
    {
      if (en != null)
      {
        en.consumeContent();
      }
    } catch (Exception e)
    {
    } catch (Error ex)
    {
    }
    // no finalize for FileParse
    // super.close();
  }

  /**
   * 
   * @return the URL that this result set read data from.
   */
  public String getUrl()
  {
    try
    {
      return cachedRequest.getURI().toURL().toString();
    } catch (Exception x)
    {
      x.printStackTrace();
      return null;
    }
  }

}
