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
package jalview.javascript.web;

import jalview.util.Platform;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/*
 *  A JavaScript-only proxy for com.sun.jersey.api.client.WebResource
 * 
 */
public class WebResource
{

  private String endpoint, params = "";

  public WebResource(String endpoint)
  {
    this.endpoint = endpoint;
  }

  public WebResource queryParam(String key, String value)
  {
    params += (params == "" ? "?" : "&") + key + "="
            + Platform.encodeURI(value);
    return this;
  }

  public URI getURI()
  {
    try
    {
      return new URI(endpoint + params);
    } catch (URISyntaxException e)
    {
      e.printStackTrace();
      return null;
    }
  }

  public Builder accept(String... encoding)
  {
    return new Builder(getURI(), encoding);
  }

  public static class Builder
  {
    private URI uri;

    private String[] encoding;

    public Builder(URI uri, String... encoding)
    {
      this.uri = uri;
      this.encoding = encoding; // application/json
    }

    /**
     * Get the response
     * 
     * @param c
     *          must be ClientResponse
     * @return
     */
    public ClientResponse get(Class<?> c)
    {
      try
      {
        return new ClientResponse(new URL(uri.toString()), encoding);
      } catch (MalformedURLException e)
      {
        return null;
      }
    }
  }

}
