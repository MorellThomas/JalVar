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
/**
 * 
 */
package jalview.ws.jws2;

import jalview.bin.Console;
import jalview.ws.jws2.jabaws2.Jws2Instance;
import jalview.ws.jws2.jabaws2.Jws2InstanceFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import compbio.data.msa.Category;
import compbio.data.msa.JABAService;
import compbio.ws.client.Jws2Client;
import compbio.ws.client.Services;

/**
 * @author JimP
 * 
 */
public class JabaWsServerQuery implements Runnable
{

  Jws2Discoverer jws2Discoverer = null;

  String jwsserver = null;

  boolean quit = false, running = false;

  /**
   * @return the running
   */
  public boolean isRunning()
  {
    return running;
  }

  /**
   * @param quit
   *          the quit to set
   */
  public void setQuit(boolean quit)
  {
    this.quit = quit;
  }

  public JabaWsServerQuery(Jws2Discoverer jws2Discoverer, String server)
  {
    this.jws2Discoverer = jws2Discoverer;
    this.jwsserver = server;
  }

  Services[] JABAWS1SERVERS = new Services[] { Services.ClustalWS,
      Services.MuscleWS, Services.MafftWS, Services.ProbconsWS,
      Services.TcoffeeWS };

  Services[] JABAWS2SERVERS = new Services[] { Services.ClustalWS,
      Services.MuscleWS, Services.MafftWS, Services.ProbconsWS,
      Services.TcoffeeWS, Services.AAConWS, Services.DisemblWS,
      Services.GlobPlotWS, Services.IUPredWS, Services.JronnWS,
      Services.RNAalifoldWS };

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run()
  {
    running = true;
    try
    {
      // TODO this test doesn't seem to attempt a connection - should it?
      // would save a lot of failed tries with UnknownHostException
      if (isValidUrl(jwsserver))
      {
        compbio.data.msa.RegistryWS registry = null;
        Set svccategories = null;
        boolean noservices = true;
        // look for services
        boolean jabasws2 = false;
        // If we are dealing with a JABAWS2 service, then just go and ask the
        // JABAWS 2 service registry
        Set<Services> srv_set = new HashSet<Services>();

        Set<Category> categories = Category.getCategories();
        String svc_cat;

        try
        {
          // JBPNote: why is RegistryWS in compbio.data.msa ?
          registry = Jws2Client.connectToRegistry(jwsserver);
          if (registry != null)
          {
            // System.err.println("Test Services Output\n"
            // + registry.testAllServices());
            // TODO: enumerate services and test those that haven't been tested
            // in the last n-days/hours/etc.

            jabasws2 = true;
            srv_set = registry.getSupportedServices();

            // dan test
            System.out.println(
                    "registry.getSupportedServices: " + srv_set.toString());

            svccategories = registry.getServiceCategories();

            // dan test
            // System.out.println("registry.getServiceCategories: " +
            // svccategories.toString());

          }
        } catch (Exception ex)
        {
          System.err.println("Exception whilst trying to get at registry:");
          ex.printStackTrace();
          // if that failed, then we are probably working with a JABAWS1 server.
          // in that case, look for each service endpoint
          System.err.println("JWS2 Discoverer: " + jwsserver
                  + " is a JABAWS1 server. Using hardwired list.");
          for (Services srv : JABAWS1SERVERS)
          {
            srv_set.add(srv);
          }
        }

        for (Category cat : categories)
        {
          for (Services srv : cat.getServices())
          {
            if (quit)
            {
              running = false;
              return;
            }
            if (!srv_set.contains(srv))
            {
              continue;
            }
            JABAService service = null;
            try
            {
              service = Jws2Client.connect(jwsserver, srv);
            } catch (Exception e)
            {
              System.err.println("Jws2 Discoverer: Problem on " + jwsserver
                      + " with service " + srv + ":\n" + e.getMessage());
              if (!(e instanceof javax.xml.ws.WebServiceException))
              {
                e.printStackTrace();
              }
              // For moment, report service as a problem.
              jws2Discoverer.addInvalidServiceUrl(jwsserver);
            }
            ;
            if (service != null
                    && !Jws2InstanceFactory.ignoreService(srv.toString()))
            {
              noservices = false;
              Jws2Instance svc = null;
              if (registry != null)
              {

                String description = registry.getServiceDescription(srv);

                svc = Jws2InstanceFactory.newJws2Instance(jwsserver,
                        srv.toString(), cat.name, description, service);
              }
              if (svc == null)
              {
                svc = Jws2InstanceFactory.newJws2Instance(jwsserver,
                        srv.toString(), cat.name,
                        "JABAWS 1 Alignment Service", service);
              }
              jws2Discoverer.addService(jwsserver, svc);
            }

          }
        }

        if (noservices)
        {
          jws2Discoverer.addUrlwithnoservices(jwsserver);
        }
      }
      else
      {
        jws2Discoverer.addInvalidServiceUrl(jwsserver);
        Console.warn("Ignoring invalid Jws2 service url " + jwsserver);
      }
    } catch (Exception e)
    {
      e.printStackTrace();
      Console.warn("Exception when discovering Jws2 services.", e);
      jws2Discoverer.addInvalidServiceUrl(jwsserver);
    } catch (Error e)
    {
      Console.error("Exception when discovering Jws2 services.", e);
      jws2Discoverer.addInvalidServiceUrl(jwsserver);
    }
    running = false;
  }

  /**
   * Check if the URL is valid and responding.
   * 
   * @return
   */
  private boolean isValidUrl(String server)
  {
    // return Jws2Client.validURL(jwsserver); // checks syntax only
    boolean result = false;
    if (server != null)
    {
      try
      {
        URL url = new URL(server);
        url.openStream().close();
        result = true;
      } catch (MalformedURLException e)
      {
        System.err.println("Invalid server URL: " + server);
        result = false;
      } catch (IOException e)
      {
        System.err.println("Error connecting to server: " + server + ": "
                + e.toString());
        result = false;
      }
    }
    return result;
  }

}
