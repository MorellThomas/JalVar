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
package jalview.httpserver;

import java.io.IOException;
import java.net.BindException;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * 
 * @author gmcarstairs
 *
 */
public abstract class AbstractRequestHandler extends AbstractHandler
{

  /*
   * The relative path (below context root) of this handler (without /
   * separators)
   */
  private String path;

  /*
   * The full URI on which this handler listens
   */
  private String uri;

  /**
   * Handle an incoming Http request.
   */
  @Override
  public void handle(String target, Request baseRequest,
          HttpServletRequest request, HttpServletResponse response)
          throws IOException, ServletException
  {
    try
    {
      // dumpRequest(request); // debug
      processRequest(request, response);
    } catch (Throwable t)
    {
      /*
       * Set server error status on response
       */
      System.err.println("Exception handling request "
              + request.getRequestURI() + " : " + t.getMessage());
      if (response.isCommitted())
      {
        /*
         * Can't write an HTTP header once any response content has been written
         */
        System.err.println(
                "Unable to return HTTP 500 as response already committed");
      }
      else
      {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
    } finally
    {
      response.getWriter().flush();
      baseRequest.setHandled(true);
    }
  }

  /**
   * Subclasses should override this method to perform request processing
   * 
   * @param request
   * @param response
   * @throws IOException
   */
  protected abstract void processRequest(HttpServletRequest request,
          HttpServletResponse response) throws IOException;

  /**
   * For debug - writes HTTP request details to stdout
   * 
   * @param request
   */
  protected void dumpRequest(HttpServletRequest request)
  {
    System.out.println(request.getMethod());
    System.out.println(request.getRequestURL());
    for (String hdr : Collections.list(request.getHeaderNames()))
    {
      for (String val : Collections.list(request.getHeaders(hdr)))
      {
        System.out.println(hdr + ": " + val);
      }
    }
    for (String param : Collections.list(request.getParameterNames()))
    {
      for (String val : request.getParameterValues(param))
      {
        System.out.println(param + "=" + val);
      }
    }
  }

  /**
   * Returns a display name for the handler
   * 
   * @return
   */
  public abstract String getName();

  /**
   * Deregister this listener and close it down
   * 
   * @throws Exception
   */
  public void shutdown()
  {
    try
    {
      HttpServer.getInstance().removeHandler(this);
      stop();
    } catch (Exception e)
    {
      System.err.println(
              "Error stopping " + getName() + ": " + e.getMessage());
    }
  }

  /**
   * Returns the URI on which we are listening
   * 
   * @return
   */
  public String getUri()
  {
    return this.uri;
  }

  /**
   * Set the URI to this handler
   * 
   * @param u
   */
  protected void setUri(String u)
  {
    this.uri = u;
  }

  /**
   * Sets the relative path to this handler - do this before registering the
   * handler.
   * 
   * @param p
   */
  protected void setPath(String p)
  {
    this.path = p;
  }

  /**
   * Returns the relative path to this handler below the context root (without /
   * separators)
   * 
   * @return
   */
  public String getPath()
  {
    return this.path;
  }

  /**
   * Registers the handler with the HttpServer and reports its URI on stdout
   * 
   * @throws BindException
   *           if no port could be allocated
   * @throws IllegalStateException
   *           if this method is called before {@link #setPath}
   */
  protected void registerHandler() throws BindException
  {
    HttpServer.getInstance().registerHandler(this);
  }
}
