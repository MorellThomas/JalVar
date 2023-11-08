/*******************************************************************************
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
 ******************************************************************************/
//
//========================================================================
//Copyright (c) 1995-2015 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//All rights reserved. This program and the accompanying materials
//are made available under the terms of the Eclipse Public License v1.0
//and Apache License v2.0 which accompanies this distribution.
//
//  The Eclipse Public License is available at
//  http://www.eclipse.org/legal/epl-v10.html
//
//  The Apache License v2.0 is available at
//  http://www.opensource.org/licenses/apache2.0.php
//
//You may elect to redistribute this code under either of these licenses.
//========================================================================
//

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

public class JettyExamplesDir
{

  /**
   * Simple Jetty FileServer. This is a simple example of Jetty configured as a
   * FileServer.
   */

  public static void main(String[] args) throws Exception
  {
    // Create a basic Jetty server object that will listen on port 8080. Note
    // that if you set this to port 0
    // then a randomly available port will be assigned that you can either look
    // in the logs for the port,
    // or programmatically obtain it for use in test cases.
    Server server = new Server(8080);

    // Create the ResourceHandler. It is the object that will actually handle
    // the request for a given file. It is
    // a Jetty Handler object so it is suitable for chaining with other handlers
    // as you will see in other examples.
    ResourceHandler resource_handler = new ResourceHandler();
    // Configure the ResourceHandler. Setting the resource base indicates where
    // the files should be served out of.
    // In this example it is the current directory but it can be configured to
    // anything that the jvm has access to.
    resource_handler.setDirectoriesListed(true);
    resource_handler.setWelcomeFiles(new String[] { "applets.html" });
    resource_handler.setResourceBase(".");

    // Add the ResourceHandler to the server.
    // GzipHandler gzip = new GzipHandler();
    // server.setHandler(gzip);
    HandlerList handlers = new HandlerList();
    handlers.setHandlers(new Handler[] { resource_handler,
        new DefaultHandler() });
    server.setHandler(handlers);

    // Start things up! By using the server.join() the server thread will join
    // with the current thread.
    // See
    // "http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/Thread.html#join()"
    // for more details.
    server.start();
    server.join();
  }
}
