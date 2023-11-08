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
package jalview.ws.jws2;

import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.gui.AlignFrame;
import jalview.gui.Desktop;
import jalview.gui.JvSwingUtils;
import jalview.util.MessageManager;
import jalview.ws.WSMenuEntryProviderI;
import jalview.ws.jws2.jabaws2.Jws2Instance;
import jalview.ws.params.ParamDatastoreI;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import compbio.ws.client.Services;

/**
 * discoverer for jws2 services. Follows the lightweight service discoverer
 * pattern (archetyped by EnfinEnvision2OneWay)
 * 
 * @author JimP
 * 
 */
public class Jws2Discoverer implements Runnable, WSMenuEntryProviderI
{
  public static final String COMPBIO_JABAWS = "http://www.compbio.dundee.ac.uk/jabaws";

  /*
   * the .jalview_properties entry for JWS2 URLS
   */
  private final static String JWS2HOSTURLS = "JWS2HOSTURLS";

  /*
   * Singleton instance
   */
  private static Jws2Discoverer discoverer;

  /*
   * Override for testing only
   */
  private static List<String> testUrls = null;

  // preferred url has precedence over others
  private String preferredUrl;

  private PropertyChangeSupport changeSupport = new PropertyChangeSupport(
          this);

  private Vector<String> invalidServiceUrls = null;

  private Vector<String> urlsWithoutServices = null;

  private Vector<String> validServiceUrls = null;

  private volatile boolean running = false;

  private volatile boolean aborted = false;

  private Thread oldthread = null;

  /**
   * holds list of services.
   */
  protected Vector<Jws2Instance> services;

  /**
   * Private constructor enforces use of singleton via getDiscoverer()
   */
  private Jws2Discoverer()
  {
  }

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
   * @return the aborted
   */
  public boolean isAborted()
  {
    return aborted;
  }

  /**
   * @param aborted
   *          the aborted to set
   */
  public void setAborted(boolean aborted)
  {
    this.aborted = aborted;
  }

  @Override
  public void run()
  {

    if (running && oldthread != null && oldthread.isAlive())
    {
      if (!aborted)
      {
        return;
      }
      while (running)
      {
        try
        {
          Console.debug(
                  "Waiting around for old discovery thread to finish.");
          // wait around until old discoverer dies
          Thread.sleep(100);
        } catch (Exception e)
        {
        }
      }
      aborted = false;
      Console.debug("Old discovery thread has finished.");
    }
    running = true;

    // first set up exclusion list if needed
    final Set<String> ignoredServices = new HashSet<>();
    for (String ignored : Cache
            .getDefault("IGNORED_JABAWS_SERVICETYPES", "").split("\\|"))
    {
      ignoredServices.add(ignored);
    }

    changeSupport.firePropertyChange("services", services,
            new Vector<Jws2Instance>());
    oldthread = Thread.currentThread();
    try
    {
      Class foo = getClass().getClassLoader()
              .loadClass("compbio.ws.client.Jws2Client");
    } catch (ClassNotFoundException e)
    {
      System.err.println(
              "Not enabling JABA Webservices : client jar is not available."
                      + "\nPlease check that your webstart JNLP file is up to date!");
      running = false;
      return;
    }
    // reinitialise records of good and bad service URLs
    if (services != null)
    {
      services.removeAllElements();
    }
    if (urlsWithoutServices != null)
    {
      urlsWithoutServices.removeAllElements();
    }
    if (invalidServiceUrls != null)
    {
      invalidServiceUrls.removeAllElements();
    }
    if (validServiceUrls != null)
    {
      validServiceUrls.removeAllElements();
    }
    ArrayList<String> svctypes = new ArrayList<>();

    List<JabaWsServerQuery> qrys = new ArrayList<>();
    for (final String jwsserver : getServiceUrls())
    {
      JabaWsServerQuery squery = new JabaWsServerQuery(this, jwsserver);
      if (svctypes.size() == 0)
      {
        // TODO: remove this ugly hack to get Canonical JABA service ordering
        // for all possible services
        for (Services sv : squery.JABAWS2SERVERS)
        {
          if (!ignoredServices.contains(sv.toString()))
          {
            svctypes.add(sv.toString());
          }
        }

      }
      qrys.add(squery);
      new Thread(squery).start();
    }
    boolean finished = true;
    do
    {
      finished = true;
      try
      {
        Thread.sleep(100);
      } catch (Exception e)
      {
      }
      for (JabaWsServerQuery squery : qrys)
      {
        if (squery.isRunning())
        {
          finished = false;
        }
      }
      if (aborted)
      {
        Console.debug(
                "Aborting " + qrys.size() + " JABAWS discovery threads.");
        for (JabaWsServerQuery squery : qrys)
        {
          squery.setQuit(true);
        }
      }
    } while (!aborted && !finished);
    if (!aborted)
    {
      // resort services according to order found in jabaws service list
      // also ensure services for each host are ordered in same way.

      if (services != null && services.size() > 0)
      {
        Jws2Instance[] svcs = new Jws2Instance[services.size()];
        int[] spos = new int[services.size()];
        int ipos = 0;
        List<String> svcUrls = getServiceUrls();
        for (Jws2Instance svc : services)
        {
          svcs[ipos] = svc;
          spos[ipos++] = 1000 * svcUrls.indexOf(svc.getHost()) + 1
                  + svctypes.indexOf(svc.serviceType);
        }
        jalview.util.QuickSort.sort(spos, svcs);
        services = new Vector<>();
        for (Jws2Instance svc : svcs)
        {
          if (!ignoredServices.contains(svc.serviceType))
          {
            services.add(svc);
          }
        }
      }
    }
    oldthread = null;
    running = false;
    changeSupport.firePropertyChange("services", new Vector<Jws2Instance>(),
            services);
  }

  /**
   * record this service endpoint so we can use it
   * 
   * @param jwsservers
   * @param srv
   * @param service2
   */
  synchronized void addService(String jwsservers, Jws2Instance service)
  {
    if (services == null)
    {
      services = new Vector<>();
    }
    System.out.println(
            "Discovered service: " + jwsservers + " " + service.toString());
    // Jws2Instance service = new Jws2Instance(jwsservers, srv.toString(),
    // service2);

    services.add(service);
    // retrieve the presets and parameter set and cache now
    ParamDatastoreI pds = service.getParamStore();
    if (pds != null)
    {
      pds.getPresets();
    }
    service.hasParameters();
    if (validServiceUrls == null)
    {
      validServiceUrls = new Vector<>();
    }
    validServiceUrls.add(jwsservers);
  }

  /**
   * attach all available web services to the appropriate submenu in the given
   * JMenu
   */
  @Override
  public void attachWSMenuEntry(JMenu wsmenu, final AlignFrame alignFrame)
  {
    // dynamically regenerate service list.
    populateWSMenuEntry(wsmenu, alignFrame, null);
  }

  private boolean isRecalculable(String action)
  {
    return (action != null && action.equalsIgnoreCase("conservation"));
  }

  private void populateWSMenuEntry(JMenu jws2al,
          final AlignFrame alignFrame, String typeFilter)
  {
    if (running || services == null || services.size() == 0)
    {
      return;
    }

    /**
     * eventually, JWS2 services will appear under the same align/etc submenus.
     * for moment we keep them separate.
     */
    JMenu atpoint;
    List<Jws2Instance> enumerableServices = new ArrayList<>();
    // jws2al.removeAll();
    Map<String, Jws2Instance> preferredHosts = new HashMap<>();
    Map<String, List<Jws2Instance>> alternates = new HashMap<>();
    for (Jws2Instance service : services.toArray(new Jws2Instance[0]))
    {
      if (!isRecalculable(service.action))
      {
        // add 'one shot' services to be displayed using the classic menu
        // structure
        enumerableServices.add(service);
      }
      else
      {
        if (!preferredHosts.containsKey(service.serviceType))
        {
          Jws2Instance preferredInstance = getPreferredServiceFor(
                  alignFrame, service.serviceType);
          if (preferredInstance != null)
          {
            preferredHosts.put(service.serviceType, preferredInstance);
          }
          else
          {
            preferredHosts.put(service.serviceType, service);
          }
        }
        List<Jws2Instance> ph = alternates.get(service.serviceType);
        if (preferredHosts.get(service.serviceType) != service)
        {
          if (ph == null)
          {
            ph = new ArrayList<>();
          }
          ph.add(service);
          alternates.put(service.serviceType, ph);
        }
      }

    }

    // create GUI element for classic services
    addEnumeratedServices(jws2al, alignFrame, enumerableServices);
    // and the instantaneous services
    for (final Jws2Instance service : preferredHosts.values())
    {
      atpoint = JvSwingUtils.findOrCreateMenu(jws2al, service.action);
      JMenuItem hitm;
      if (atpoint.getItemCount() > 1)
      {
        // previous service of this type already present
        atpoint.addSeparator();
      }
      atpoint.add(hitm = new JMenuItem(service.getHost()));
      hitm.setForeground(Color.blue);
      hitm.addActionListener(new ActionListener()
      {

        @Override
        public void actionPerformed(ActionEvent e)
        {
          Desktop.showUrl(service.getHost());
        }
      });
      hitm.setToolTipText(JvSwingUtils.wrapTooltip(false,
              MessageManager.getString("label.open_jabaws_web_page")));

      service.attachWSMenuEntry(atpoint, alignFrame);
      if (alternates.containsKey(service.serviceType))
      {
        atpoint.add(hitm = new JMenu(
                MessageManager.getString("label.switch_server")));
        hitm.setToolTipText(JvSwingUtils.wrapTooltip(false,
                MessageManager.getString("label.choose_jabaws_server")));
        for (final Jws2Instance sv : alternates.get(service.serviceType))
        {
          JMenuItem itm;
          hitm.add(itm = new JMenuItem(sv.getHost()));
          itm.setForeground(Color.blue);
          itm.addActionListener(new ActionListener()
          {

            @Override
            public void actionPerformed(ActionEvent arg0)
            {
              new Thread(new Runnable()
              {
                @Override
                public void run()
                {
                  setPreferredServiceFor(alignFrame, sv.serviceType,
                          sv.action, sv);
                  changeSupport.firePropertyChange("services",
                          new Vector<Jws2Instance>(), services);
                }
              }).start();

            }
          });
        }
      }
    }
  }

  /**
   * add services using the Java 2.5/2.6/2.7 system which optionally creates
   * submenus to index by host and service program type
   */
  private void addEnumeratedServices(final JMenu jws2al,
          final AlignFrame alignFrame,
          List<Jws2Instance> enumerableServices)
  {
    boolean byhost = Cache.getDefault("WSMENU_BYHOST", false),
            bytype = Cache.getDefault("WSMENU_BYTYPE", false);
    /**
     * eventually, JWS2 services will appear under the same align/etc submenus.
     * for moment we keep them separate.
     */
    JMenu atpoint;

    List<String> hostLabels = new ArrayList<>();
    Hashtable<String, String> lasthostFor = new Hashtable<>();
    Hashtable<String, ArrayList<Jws2Instance>> hosts = new Hashtable<>();
    ArrayList<String> hostlist = new ArrayList<>();
    for (Jws2Instance service : enumerableServices)
    {
      ArrayList<Jws2Instance> hostservices = hosts.get(service.getHost());
      if (hostservices == null)
      {
        hosts.put(service.getHost(), hostservices = new ArrayList<>());
        hostlist.add(service.getHost());
      }
      hostservices.add(service);
    }
    // now add hosts in order of the given array
    for (String host : hostlist)
    {
      Jws2Instance orderedsvcs[] = hosts.get(host)
              .toArray(new Jws2Instance[1]);
      String sortbytype[] = new String[orderedsvcs.length];
      for (int i = 0; i < sortbytype.length; i++)
      {
        sortbytype[i] = orderedsvcs[i].serviceType;
      }
      jalview.util.QuickSort.sort(sortbytype, orderedsvcs);
      for (final Jws2Instance service : orderedsvcs)
      {
        atpoint = JvSwingUtils.findOrCreateMenu(jws2al, service.action);
        String type = service.serviceType;
        if (byhost)
        {
          atpoint = JvSwingUtils.findOrCreateMenu(atpoint, host);
          if (atpoint.getToolTipText() == null)
          {
            atpoint.setToolTipText(MessageManager
                    .formatMessage("label.services_at", new String[]
                    { host }));
          }
        }
        if (bytype)
        {
          atpoint = JvSwingUtils.findOrCreateMenu(atpoint, type);
          if (atpoint.getToolTipText() == null)
          {
            atpoint.setToolTipText(service.getActionText());
          }
        }
        if (!byhost && !hostLabels.contains(
                host + service.serviceType + service.getActionText()))
        // !hostLabels.contains(host + (bytype ?
        // service.serviceType+service.getActionText() : "")))
        {
          // add a marker indicating where this service is hosted
          // relies on services from the same host being listed in a
          // contiguous
          // group
          JMenuItem hitm;
          if (hostLabels.contains(host))
          {
            atpoint.addSeparator();
          }
          else
          {
            hostLabels.add(host);
          }
          if (lasthostFor.get(service.action) == null
                  || !lasthostFor.get(service.action).equals(host))
          {
            atpoint.add(hitm = new JMenuItem(host));
            hitm.setForeground(Color.blue);
            hitm.addActionListener(new ActionListener()
            {

              @Override
              public void actionPerformed(ActionEvent e)
              {
                Desktop.showUrl(service.getHost());
              }
            });
            hitm.setToolTipText(
                    JvSwingUtils.wrapTooltip(true, MessageManager
                            .getString("label.open_jabaws_web_page")));
            lasthostFor.put(service.action, host);
          }
          hostLabels.add(
                  host + service.serviceType + service.getActionText());
        }

        service.attachWSMenuEntry(atpoint, alignFrame);
      }
    }
  }

  /**
   * 
   * @param args
   * @j2sIgnore
   */
  public static void main(String[] args)
  {
    if (args.length > 0)
    {
      testUrls = new ArrayList<>();
      for (String url : args)
      {
        testUrls.add(url);
      }
    }
    Thread runner = getDiscoverer()
            .startDiscoverer(new PropertyChangeListener()
            {

              @Override
              public void propertyChange(PropertyChangeEvent evt)
              {
                if (getDiscoverer().services != null)
                {
                  System.out.println("Changesupport: There are now "
                          + getDiscoverer().services.size() + " services");
                  int i = 1;
                  for (Jws2Instance instance : getDiscoverer().services)
                  {
                    System.out.println("Service " + i++ + " "
                            + instance.getClass() + "@" + instance.getHost()
                            + ": " + instance.getActionText());
                  }

                }
              }
            });
    while (runner.isAlive())
    {
      try
      {
        Thread.sleep(50);
      } catch (InterruptedException e)
      {
      }
    }
    try
    {
      Thread.sleep(50);
    } catch (InterruptedException x)
    {
    }
  }

  /**
   * Returns the singleton instance of this class.
   * 
   * @return
   */
  public static Jws2Discoverer getDiscoverer()
  {
    if (discoverer == null)
    {
      discoverer = new Jws2Discoverer();
    }
    return discoverer;
  }

  public boolean hasServices()
  {
    return !running && services != null && services.size() > 0;
  }

  public boolean isRunning()
  {
    return running;
  }

  public void setServiceUrls(List<String> wsUrls)
  {
    if (wsUrls != null && !wsUrls.isEmpty())
    {
      StringBuilder urls = new StringBuilder(128);
      String sep = "";
      for (String url : wsUrls)
      {
        urls.append(sep);
        urls.append(url);
        sep = ",";
      }
      Cache.setProperty(JWS2HOSTURLS, urls.toString());
    }
    else
    {
      Cache.removeProperty(JWS2HOSTURLS);
    }
  }

  /**
   * Returns web service URLs, in the order in which they should be tried (or an
   * empty list).
   * 
   * @return
   */
  public List<String> getServiceUrls()
  {
    if (testUrls != null)
    {
      // return test urls, if there are any, instead of touching cache
      return testUrls;
    }
    List<String> urls = new ArrayList<>();

    if (this.preferredUrl != null)
    {
      urls.add(preferredUrl);
    }

    String surls = Cache.getDefault(JWS2HOSTURLS, COMPBIO_JABAWS);
    try
    {
      StringTokenizer st = new StringTokenizer(surls, ",");
      while (st.hasMoreElements())
      {
        String url = null;
        try
        {
          url = st.nextToken();
          new URL(url);
          if (!urls.contains(url))
          {
            urls.add(url);
          }
          else
          {
            Console.warn("Ignoring duplicate url " + url + " in "
                    + JWS2HOSTURLS + " list");
          }
        } catch (MalformedURLException ex)
        {
          Console.warn("Problem whilst trying to make a URL from '"
                  + ((url != null) ? url : "<null>") + "'");
          Console.warn(
                  "This was probably due to a malformed comma separated list"
                          + " in the " + JWS2HOSTURLS
                          + " entry of $(HOME)/.jalview_properties)");
          Console.debug("Exception was ", ex);
        }
      }
    } catch (Exception ex)
    {
      Console.warn("Error parsing comma separated list of urls in "
              + JWS2HOSTURLS + " preference.", ex);
    }
    return urls;
  }

  public Vector<Jws2Instance> getServices()
  {
    return (services == null) ? new Vector<>() : new Vector<>(services);
  }

  /**
   * test the given URL with the JabaWS test code
   * 
   * @param foo
   * @return
   */
  public static boolean testServiceUrl(URL foo)
  {
    try
    {
      compbio.ws.client.WSTester
              .main(new String[]
              { "-h=" + foo.toString() });
    } catch (Exception e)
    {
      e.printStackTrace();
      return false;
    } catch (OutOfMemoryError e)
    {
      e.printStackTrace();
      return false;
    } catch (Error e)
    {
      e.printStackTrace();
      return false;
    }

    return true;
  }

  public boolean restart()
  {
    synchronized (this)
    {
      if (running)
      {
        aborted = true;
      }
      else
      {
        running = true;
      }
      return aborted;
    }
  }

  /**
   * Start a fresh discovery thread and notify the given object when we're
   * finished. Any known existing threads will be killed before this one is
   * started.
   * 
   * @param changeSupport2
   * @return new thread
   */
  public Thread startDiscoverer(PropertyChangeListener changeSupport2)
  {
    /*    if (restart())
        {
          return;
        }
        else
        {
          Thread thr = new Thread(this);
          thr.start();
        }
       */
    if (isRunning())
    {
      setAborted(true);
    }
    addPropertyChangeListener(changeSupport2);
    Thread thr = new Thread(this);
    thr.start();
    return thr;
  }

  /**
   * @return the invalidServiceUrls
   */
  public Vector<String> getInvalidServiceUrls()
  {
    return invalidServiceUrls;
  }

  /**
   * @return the urlsWithoutServices
   */
  public Vector<String> getUrlsWithoutServices()
  {
    return urlsWithoutServices;
  }

  /**
   * add an 'empty' JABA server to the list. Only servers not already in the
   * 'bad URL' list will be added to this list.
   * 
   * @param jwsservers
   */
  public synchronized void addUrlwithnoservices(String jwsservers)
  {
    if (urlsWithoutServices == null)
    {
      urlsWithoutServices = new Vector<>();
    }

    if ((invalidServiceUrls == null
            || !invalidServiceUrls.contains(jwsservers))
            && !urlsWithoutServices.contains(jwsservers))
    {
      urlsWithoutServices.add(jwsservers);
    }
  }

  /**
   * add a bad URL to the list
   * 
   * @param jwsservers
   */
  public synchronized void addInvalidServiceUrl(String jwsservers)
  {
    if (invalidServiceUrls == null)
    {
      invalidServiceUrls = new Vector<>();
    }
    if (!invalidServiceUrls.contains(jwsservers))
    {
      invalidServiceUrls.add(jwsservers);
    }
  }

  /**
   * 
   * @return a human readable report of any problems with the service URLs used
   *         for discovery
   */
  public String getErrorMessages()
  {
    if (!isRunning() && !isAborted())
    {
      StringBuffer ermsg = new StringBuffer();
      boolean list = false;
      if (getInvalidServiceUrls() != null
              && getInvalidServiceUrls().size() > 0)
      {
        ermsg.append(MessageManager.getString("warn.urls_not_contacted")
                + ": \n");
        for (String svcurl : getInvalidServiceUrls())
        {
          if (list)
          {
            ermsg.append(", ");
          }
          list = true;
          ermsg.append(svcurl);
        }
        ermsg.append("\n\n");
      }
      list = false;
      if (getUrlsWithoutServices() != null
              && getUrlsWithoutServices().size() > 0)
      {
        ermsg.append(
                MessageManager.getString("warn.urls_no_jaba") + ": \n");
        for (String svcurl : getUrlsWithoutServices())
        {
          if (list)
          {
            ermsg.append(", ");
          }
          list = true;
          ermsg.append(svcurl);
        }
        ermsg.append("\n");
      }
      if (ermsg.length() > 1)
      {
        return ermsg.toString();
      }

    }
    return null;
  }

  public int getServerStatusFor(String url)
  {
    if (validServiceUrls != null && validServiceUrls.contains(url))
    {
      return 1;
    }
    if (urlsWithoutServices != null && urlsWithoutServices.contains(url))
    {
      return 0;
    }
    if (invalidServiceUrls != null && invalidServiceUrls.contains(url))
    {
      return -1;
    }
    return -2;
  }

  /**
   * pick the user's preferred service based on a set of URLs (jaba server
   * locations) and service URIs (specifying version and service interface
   * class)
   * 
   * @param serviceURL
   * @return null or best match for given uri/ls.
   */
  public Jws2Instance getPreferredServiceFor(String[] serviceURLs)
  {
    HashSet<String> urls = new HashSet<>();
    urls.addAll(Arrays.asList(serviceURLs));
    Jws2Instance match = null;
    if (services != null)
    {
      for (Jws2Instance svc : services)
      {
        if (urls.contains(svc.getServiceTypeURI()))
        {
          if (match == null)
          {
            // for moment we always pick service from server ordered first in
            // user's preferences
            match = svc;
          }
          if (urls.contains(svc.getUri()))
          {
            // stop and return - we've matched type URI and URI for service
            // endpoint
            return svc;
          }
        }
      }
    }
    return match;
  }

  Map<String, Map<String, String>> preferredServiceMap = new HashMap<>();

  /**
   * get current preferred service of the given type, or global default
   * 
   * @param af
   *          null or a specific alignFrame
   * @param serviceType
   *          Jws2Instance.serviceType for service
   * @return null if no service of this type is available, the preferred service
   *         for the serviceType and af if specified and if defined.
   */
  public Jws2Instance getPreferredServiceFor(AlignFrame af,
          String serviceType)
  {
    String serviceurl = null;
    synchronized (preferredServiceMap)
    {
      String afid = (af == null) ? "" : af.getViewport().getSequenceSetId();
      Map<String, String> prefmap = preferredServiceMap.get(afid);
      if (afid.length() > 0 && prefmap == null)
      {
        // recover global setting, if any
        prefmap = preferredServiceMap.get("");
      }
      if (prefmap != null)
      {
        serviceurl = prefmap.get(serviceType);
      }

    }
    Jws2Instance response = null;
    for (Jws2Instance svc : services)
    {
      if (svc.serviceType.equals(serviceType))
      {
        if (serviceurl == null || serviceurl.equals(svc.getHost()))
        {
          response = svc;
          break;
        }
      }
    }
    return response;
  }

  public void setPreferredServiceFor(AlignFrame af, String serviceType,
          String serviceAction, Jws2Instance selectedServer)
  {
    String afid = (af == null) ? "" : af.getViewport().getSequenceSetId();
    if (preferredServiceMap == null)
    {
      preferredServiceMap = new HashMap<>();
    }
    Map<String, String> prefmap = preferredServiceMap.get(afid);
    if (prefmap == null)
    {
      prefmap = new HashMap<>();
      preferredServiceMap.put(afid, prefmap);
    }
    prefmap.put(serviceType, selectedServer.getHost());
    prefmap.put(serviceAction, selectedServer.getHost());
  }

  public void setPreferredServiceFor(String serviceType,
          String serviceAction, Jws2Instance selectedServer)
  {
    setPreferredServiceFor(null, serviceType, serviceAction,
            selectedServer);
  }

  /**
   * Set a URL to try before any others. For use with command-line parameter to
   * configure a local Jabaws installation without the need to add to property
   * files.
   * 
   * @param value
   * @throws MalformedURLException
   */
  public void setPreferredUrl(String value) throws MalformedURLException
  {
    if (value != null && value.trim().length() > 0)
    {
      new URL(value);
      preferredUrl = value;
    }
  }
}
