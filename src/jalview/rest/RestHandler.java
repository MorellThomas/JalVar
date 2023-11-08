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
package jalview.rest;

import jalview.httpserver.AbstractRequestHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.BindException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A simple handler to process (or delegate) HTTP requests on /jalview/rest
 */
public class RestHandler extends AbstractRequestHandler
{
  private static final String MY_PATH = "rest";

  private static final String MY_NAME = "Rest";

  /**
   * Singleton instance of this class
   */
  private static RestHandler instance = null;

  /**
   * Returns the singleton instance of this class
   * 
   * @return
   * @throws BindException
   */
  public static RestHandler getInstance() throws BindException
  {
    synchronized (RestHandler.class)
    {
      if (instance == null)
      {
        instance = new RestHandler();
      }
    }
    return instance;
  }

  /**
   * Private constructor enforces use of singleton
   * 
   * @throws BindException
   */
  private RestHandler() throws BindException
  {
    setPath(MY_PATH);

    /*
     * We don't register the handler here - this is done as a special case in
     * HttpServer initialisation; to do it here would invite an infinite loop of
     * RestHandler/HttpServer constructor
     */
  }

  /**
   * Handle a jalview/rest request
   * 
   * @throws IOException
   */
  @Override
  protected void processRequest(HttpServletRequest request,
          HttpServletResponse response) throws IOException
  {
    /*
     * Currently just echoes the request; add helper classes as required to
     * process requests
     */
    final String queryString = request.getQueryString();
    final String reply = "REST not yet implemented; received "
            + request.getMethod() + ": " + request.getRequestURL()
            + (queryString == null ? "" : "?" + queryString);
    System.out.println(reply);

    response.setHeader("Cache-Control", "no-cache/no-store");
    response.setHeader("Content-type", "text/plain");
    final PrintWriter writer = response.getWriter();
    writer.write(reply);
    writer.close();
  }

  /**
   * Returns a display name for this service
   */
  @Override
  public String getName()
  {
    return MY_NAME;
  }

}
