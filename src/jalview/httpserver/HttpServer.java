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

import jalview.rest.RestHandler;

import java.net.BindException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 * An HttpServer built on Jetty. To use it
 * <ul>
 * <li>call getInstance() to create and start the server</li>
 * <li>call registerHandler to add a handler for a path (below /jalview)</li>
 * <li>when finished, call removedHandler</li>
 * </ul>
 * 
 * @author gmcarstairs
 * @see http://eclipse.org/jetty/documentation/current/embedding-jetty.html
 */
public class HttpServer
{
  /*
   * 'context root' - actually just prefixed to the path for each handler for
   * now - see registerHandler
   */
  private static final String JALVIEW_PATH = "jalview";

  /*
   * Singleton instance of this server
   */
  private static HttpServer instance;

  /*
   * The Http server
   */
  private Server server;

  /*
   * Registered handlers for context paths
   */
  private HandlerCollection contextHandlers;

  /*
   * Lookup of ContextHandler by its wrapped handler
   */
  Map<Handler, ContextHandler> myHandlers = new HashMap<Handler, ContextHandler>();

  /*
   * The context root for the server
   */
  private URI contextRoot;

  /**
   * Returns the singleton instance of this class.
   * 
   * @return
   * @throws BindException
   */
  public static HttpServer getInstance() throws BindException
  {
    synchronized (HttpServer.class)
    {
      if (instance == null)
      {
        instance = new HttpServer();
      }
      return instance;
    }
  }

  /**
   * Private constructor to enforce use of singleton
   * 
   * @throws BindException
   *           if no free port can be assigned
   */
  private HttpServer() throws BindException
  {
    startServer();

    /*
     * Provides a REST server by default; add more programmatically as required
     */
    registerHandler(RestHandler.getInstance());
  }

  /**
   * Start the http server
   * 
   * @throws BindException
   */
  private void startServer() throws BindException
  {
    try
    {
      /*
       * Create a server with a small number of threads; jetty will allocate a
       * free port
       */
      QueuedThreadPool tp = new QueuedThreadPool(4, 1); // max, min
      server = new Server(tp);
      // 2 selector threads to handle incoming connections
      ServerConnector connector = new ServerConnector(server, 0, 2);
      // restrict to localhost
      connector.setHost("localhost");
      server.addConnector(connector);

      /*
       * HttpServer shuts down with Jalview process
       */
      server.setStopAtShutdown(true);

      /*
       * Create a mutable set of handlers (can add handlers while the server is
       * running). Using vanilla handlers here rather than servlets
       */
      // TODO how to properly configure context root "/jalview"
      contextHandlers = new HandlerCollection(true);
      server.setHandler(contextHandlers);
      server.start();
      // System.out.println(String.format(
      // "HttpServer started with %d threads", server.getThreadPool()
      // .getThreads()));
      contextRoot = server.getURI();
    } catch (Exception e)
    {
      System.err.println(
              "Error trying to start HttpServer: " + e.getMessage());
      try
      {
        server.stop();
      } catch (Exception e1)
      {
        e1.printStackTrace();
      }
    }
    if (server == null)
    {
      throw new BindException("HttpServer failed to allocate a port");
    }
  }

  /**
   * Returns the URI on which we are listening
   * 
   * @return
   */
  public URI getUri()
  {
    return server == null ? null : server.getURI();
  }

  /**
   * For debug - write HTTP request details to stdout
   * 
   * @param request
   * @param response
   */
  protected void dumpRequest(HttpServletRequest request,
          HttpServletResponse response)
  {
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
   * Stop the Http server.
   */
  public void stopServer()
  {
    if (server != null)
    {
      if (server.isStarted())
      {
        try
        {
          server.stop();
        } catch (Exception e)
        {
          System.err.println("Error stopping Http Server on "
                  + server.getURI() + ": " + e.getMessage());
        }
      }
    }
  }

  /**
   * Register a handler for the given path and set its URI
   * 
   * @param handler
   * @return
   * @throws IllegalStateException
   *           if handler path has not been set
   */
  public void registerHandler(AbstractRequestHandler handler)
  {
    String path = handler.getPath();
    if (path == null)
    {
      throw new IllegalStateException(
              "Must set handler path before registering handler");
    }

    // http://stackoverflow.com/questions/20043097/jetty-9-embedded-adding-handlers-during-runtime
    ContextHandler ch = new ContextHandler();
    ch.setAllowNullPathInfo(true);
    ch.setContextPath("/" + JALVIEW_PATH + "/" + path);
    ch.setResourceBase(".");
    ch.setClassLoader(Thread.currentThread().getContextClassLoader());
    ch.setHandler(handler);

    /*
     * Remember the association so we can remove it later
     */
    this.myHandlers.put(handler, ch);

    /*
     * A handler added to a running server must be started explicitly
     */
    contextHandlers.addHandler(ch);
    try
    {
      ch.start();
    } catch (Exception e)
    {
      System.err.println(
              "Error starting handler for " + path + ": " + e.getMessage());
    }

    handler.setUri(this.contextRoot + ch.getContextPath().substring(1));
    System.out.println("Jalview " + handler.getName()
            + " handler started on " + handler.getUri());
  }

  /**
   * Removes the handler from the server; more precisely, remove the
   * ContextHandler wrapping the specified handler
   * 
   * @param handler
   */
  public void removeHandler(AbstractRequestHandler handler)
  {
    /*
     * Have to use this cached lookup table since there is no method
     * ContextHandler.getHandler()
     */
    ContextHandler ch = myHandlers.get(handler);
    if (ch != null)
    {
      contextHandlers.removeHandler(ch);
      myHandlers.remove(handler);
      System.out.println("Stopped Jalview " + handler.getName()
              + " handler on " + handler.getUri());
    }
  }
}
