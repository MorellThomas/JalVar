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
package jalview.ws.jws1;

import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.gui.JvOptionPane;
import jalview.util.MessageManager;

import java.net.URL;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import ext.vamsas.IRegistry;
import ext.vamsas.IRegistryServiceLocator;
import ext.vamsas.RegistryServiceSoapBindingStub;
import ext.vamsas.ServiceHandle;
import ext.vamsas.ServiceHandles;

public class Discoverer implements Runnable
{
  ext.vamsas.IRegistry registry; // the root registry service.

  private java.beans.PropertyChangeSupport changeSupport = new java.beans.PropertyChangeSupport(
          this);

  /**
   * change listeners are notified of "services" property changes
   * 
   * @param listener
   *          to be added that consumes new services Hashtable object.
   */
  public void addPropertyChangeListener(
          java.beans.PropertyChangeListener listener)
  {
    changeSupport.addPropertyChangeListener(listener);
  }

  /**
   * 
   * 
   * @param listener
   *          to be removed
   */
  public void removePropertyChangeListener(
          java.beans.PropertyChangeListener listener)
  {
    changeSupport.removePropertyChangeListener(listener);
  }

  /**
   * Property change listener firing routine
   * 
   * @param prop
   *          services
   * @param oldvalue
   *          old services hash
   * @param newvalue
   *          new services hash
   */
  public void firePropertyChange(String prop, Object oldvalue,
          Object newvalue)
  {
    changeSupport.firePropertyChange(prop, oldvalue, newvalue);
  }

  /**
   * Initializes the server field with a valid service implementation.
   * 
   * @return true if service was located.
   */
  private IRegistry locateWebService(java.net.URL WsURL)
  {
    IRegistryServiceLocator loc = new IRegistryServiceLocator(); // Default
    IRegistry server = null;
    try
    {
      server = loc.getRegistryService(WsURL);
      ((RegistryServiceSoapBindingStub) server).setTimeout(60000); // One
      // minute
      // timeout
    } catch (Exception ex)
    {
      Console.error(
              "Serious!  Service location failed\nfor URL :" + WsURL + "\n",
              ex);

      return null;
    }

    loc.getEngine().setOption("axis", "1");

    return server;
  }

  static private java.net.URL RootServiceURL = null;

  static public Vector<URL> ServiceURLList = null;

  static private boolean reallyDiscoverServices = true;

  public static java.util.Hashtable<String, Vector<ServiceHandle>> services = null;
  // stored by
  // abstractServiceType
  // string

  public static java.util.Vector<ServiceHandle> serviceList = null;

  static private Vector<URL> getDiscoveryURLS()
  {
    Vector<URL> urls = new Vector<>();
    String RootServiceURLs = Cache.getDefault("DISCOVERY_URLS",
            "http://www.compbio.dundee.ac.uk/JalviewWS/services/ServiceRegistry");

    try
    {
      StringTokenizer st = new StringTokenizer(RootServiceURLs, ",");
      while (st.hasMoreElements())
      {
        String url = null;
        try
        {
          java.net.URL u = new java.net.URL(url = st.nextToken());
          if (!urls.contains(u))
          {
            urls.add(u);
          }
          else
          {
            Console.info("Ignoring duplicate url in DISCOVERY_URLS list");
          }
        } catch (Exception ex)
        {
          Console.warn("Problem whilst trying to make a URL from '"
                  + ((url != null) ? url : "<null>") + "'");
          Console.warn(
                  "This was probably due to a malformed comma separated list"
                          + " in the DISCOVERY_URLS entry of $(HOME)/.jalview_properties)");
          Console.debug("Exception was ", ex);
        }
      }
    } catch (Exception ex)
    {
      Console.warn(
              "Error parsing comma separated list of urls in DISCOVERY_URLS.",
              ex);
    }
    if (urls.size() > 0)
    {
      return urls;
    }
    return null;
  }

  /**
   * fetch new services or reset to hardwired defaults depending on preferences.
   */
  static public void doDiscovery()
  {
    Console.debug("(Re)-Initialising the discovery URL list.");
    try
    {
      reallyDiscoverServices = Cache.getDefault("DISCOVERY_START", false);
      if (reallyDiscoverServices)
      {
        ServiceURLList = getDiscoveryURLS();
      }
      else
      {
        Console.debug("Setting default services");
        services = new Hashtable<>();
        // Muscle, Clustal and JPred.
        ServiceHandle[] defServices = { new ServiceHandle("MsaWS",
                "Edgar, Robert C. (2004), MUSCLE: multiple sequence alignment "
                        + "with high accuracy and high throughput, Nucleic Acids Research 32(5), 1792-97.",
                "http://www.compbio.dundee.ac.uk/JalviewWS/services/MuscleWS",
                MessageManager.getString(
                        "label.muscle_multiple_protein_sequence_alignment")),
            new ServiceHandle("MsaWS",
                    "Katoh, K., K. Kuma, K., Toh, H.,  and Miyata, T. (2005) "
                            + "\"MAFFT version 5: improvement in accuracy of multiple sequence alignment.\""
                            + " Nucleic Acids Research, 33 511-518",
                    "http://www.compbio.dundee.ac.uk/JalviewWS/services/MafftWS",
                    MessageManager.getString(
                            "label.mafft_multiple_sequence_alignment")),
            new ServiceHandle("MsaWS",
                    "Thompson, J.D., Higgins, D.G. and Gibson, T.J. (1994) CLUSTAL W: improving the sensitivity of progressive multiple"
                            + " sequence alignment through sequence weighting, position specific gap penalties and weight matrix choice."
                            + " Nucleic Acids Research, 22 4673-4680",
                    "http://www.compbio.dundee.ac.uk/JalviewWS/services/ClustalWS",
                    MessageManager.getString(
                            "label.clustalw_multiple_sequence_alignment")),
            new ServiceHandle("SecStrPred",
                    "Drozdetskiy A, Cole C, Procter J & Barton GJ. (2015)\nJPred4: a protein secondary structure prediction server"
                            + "\nNucleic Acids Research, Web Server issue (first published 15th April 2015)"
                            + "\ndoi://10.1093/nar/gkv332",
                    "http://www.compbio.dundee.ac.uk/JalviewWS/services/jpred",
                    "JPred Secondary Structure Prediction") };
        services = new Hashtable<>();
        serviceList = new Vector<>();
        buildServiceLists(defServices, serviceList, services);
      }

    } catch (Exception e)
    {
      System.err.println(
              "jalview.rootRegistry is not a proper url!\nWas set to "
                      + RootServiceURL + "\n" + e);
    }

  }

  // TODO: JBPNote : make this discover more services based on list of
  // discovery service urls, break cyclic references to the same url and
  // duplicate service entries (same endpoint *and* same interface)
  private ServiceHandle[] getServices(java.net.URL location)
  {
    ServiceHandles shs = null;
    try
    {
      Console.debug("Discovering services using " + location);
      shs = locateWebService(location).getServices();
    } catch (org.apache.axis.AxisFault f)
    {
      // JBPNote - should do this a better way!
      if (f.getFaultReason().indexOf("(407)") > -1)
      {
        if (jalview.gui.Desktop.desktop != null)
        {
          JvOptionPane.showMessageDialog(jalview.gui.Desktop.desktop,
                  MessageManager.getString("label.set_proxy_settings"),
                  MessageManager
                          .getString("label.proxy_authorization_failed"),
                  JvOptionPane.WARNING_MESSAGE);
        }
      }
      else
      {
        Console.warn("No Discovery service at " + location);
        Console.debug("Axis Fault", f);
      }
    } catch (Exception e)
    {
      Console.warn("No Discovery service at " + location);
      Console.debug("Discovery Service General Exception", e);
    }
    if ((shs != null) && shs.getServices().length > 0)
    {
      return shs.getServices();
    }
    return null;
  }

  /**
   * Adds a list of services to the service catalog and categorised catalog
   * returns true if ServiceURLList was modified with a new DiscoveryService URL
   * 
   * @param sh
   *          ServiceHandle[]
   * @param cat
   *          Vector
   * @param sscat
   *          Hashtable
   * @return boolean
   */
  static private boolean buildServiceLists(ServiceHandle[] sh,
          Vector<ServiceHandle> cat,
          Hashtable<String, Vector<ServiceHandle>> sscat)
  {
    boolean seenNewDiscovery = false;
    for (int i = 0, j = sh.length; i < j; i++)
    {
      if (!cat.contains(sh[i]))
      {
        Console.debug("A " + sh[i].getAbstractName() + " service called "
                + sh[i].getName() + " exists at " + sh[i].getEndpointURL()
                + "\n");
        if (!sscat.containsKey(sh[i].getAbstractName()))
        {
          sscat.put(sh[i].getAbstractName(), cat = new Vector<>());
        }
        else
        {
          cat = sscat.get(sh[i].getAbstractName());
        }
        cat.add(sh[i]);
        if (sh[i].getAbstractName().equals("Registry"))
        {
          for (int s = 0, sUrls = ServiceURLList.size(); s < sUrls; s++)
          {
            java.net.URL disc_serv = null;
            try
            {
              disc_serv = new java.net.URL(sh[i].getEndpointURL());
              if (!ServiceURLList.contains(disc_serv))
              {
                Console.debug(
                        "Adding new discovery service at " + disc_serv);
                ServiceURLList.add(disc_serv);
                seenNewDiscovery = true;
              }
            } catch (Exception e)
            {
              Console.debug("Ignoring bad discovery service URL "
                      + sh[i].getEndpointURL(), e);
            }
          }
        }
      }
    }
    return seenNewDiscovery;
  }

  public void discoverServices()
  {
    Hashtable<String, Vector<ServiceHandle>> sscat = new Hashtable<>();
    Vector<ServiceHandle> cat = new Vector<>();
    ServiceHandle sh[] = null;
    int s_url = 0;
    if (ServiceURLList == null)
    {
      Console.debug("No service endpoints to use for service discovery.");
      return;
    }
    while (s_url < ServiceURLList.size())
    {
      if ((sh = getServices(ServiceURLList.get(s_url))) != null)
      {

        buildServiceLists(sh, cat, sscat);
      }
      else
      {
        Console.warn("No services at " + (ServiceURLList.get(s_url))
                + " - check DISCOVERY_URLS property in .jalview_properties");
      }
      s_url++;
    }
    // TODO: decide on correct semantics for services list - PropertyChange
    // provides a way of passing the new object around
    // so no need to access original discovery thread.
    // Curent decision is to change properties then notify listeners with old
    // and new values.
    Hashtable<String, Vector<ServiceHandle>> oldServices = services;
    // Vector oldServicelist = serviceList;
    services = sscat;
    serviceList = cat;
    changeSupport.firePropertyChange("services", oldServices, services);
  }

  /**
   * creates a new thread to call discoverServices()
   */
  @Override
  public void run()
  {
    final Discoverer discoverer = this;
    Thread discoverThread = new Thread()
    {
      @Override
      public void run()
      {
        Discoverer.doDiscovery();
        discoverer.discoverServices();
      }
    };
    discoverThread.start();
  }

  /**
   * binding service abstract name to handler class
   */
  private static Hashtable<String, WS1Client> serviceClientBindings;

  public static WS1Client getServiceClient(ServiceHandle sh)
  {
    if (serviceClientBindings == null)
    {
      // get a list from Config or create below
      serviceClientBindings = new Hashtable<>();
      serviceClientBindings.put("MsaWS", new MsaWSClient());
      serviceClientBindings.put("SecStrPred", new JPredClient());
      serviceClientBindings.put("SeqSearch", new SeqSearchWSClient());
    }
    WS1Client instance = serviceClientBindings.get(sh.getAbstractName());
    if (instance == null)
    {
      System.err.println(
              "WARNING - POSSIBLE IMPLEMENTATION ERROR - cannot find WSClient implementation for "
                      + sh.getAbstractName());
    }
    else
    {
      instance.serviceHandle = sh;
    }
    return instance;
  }
  /**
   * notes on discovery service 1. need to allow multiple discovery source urls.
   * 2. user interface to add/control list of urls in preferences notes on
   * wsclient discovery 1. need a classpath property with list of additional
   * plugin directories 2. optional config to cite specific bindings between
   * class name and Abstract service name. 3. precedence for automatic discovery
   * by using getAbstractName for WSClient - user added plugins override default
   * plugins ? notes on wsclient gui code for gui attachment now moved to
   * wsclient implementation. Needs more abstraction but approach seems to work.
   * is it possible to 'generalise' the data retrieval calls further ? current
   * methods are very specific (gatherForMSA or gatherForSeqOrMsaSecStrPred),
   * new methods for conservation (group or alignment), treecalc (aligned
   * profile), seqannot (sequences selected from dataset, annotation back to
   * dataset).
   * 
   */
}
