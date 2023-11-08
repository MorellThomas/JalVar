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
package jalview.bin;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import jalview.analytics.Plausible;
import jalview.datamodel.PDBEntry;
import jalview.gui.Preferences;
import jalview.gui.UserDefinedColours;
import jalview.log.JLoggerLog4j;
import jalview.schemes.ColourSchemeLoader;
import jalview.schemes.ColourSchemes;
import jalview.schemes.UserColourScheme;
import jalview.structure.StructureImportSettings;
import jalview.urls.IdOrgSettings;
import jalview.util.ChannelProperties;
import jalview.util.ColorUtils;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.ws.sifts.SiftsSettings;

/**
 * Stores and retrieves Jalview Application Properties Lists and fields within
 * list entries are separated by '|' symbols unless otherwise stated (|) clauses
 * are alternative values for a tag. <br>
 * <br>
 * Current properties include:
 * <ul>
 * <br>
 * logs.Axis.Level - one of the stringified Levels for log4j controlling the
 * logging level for axis (used for web services) <br>
 * </li>
 * <li>logs.Castor.Level - one of the stringified Levels for log4j controlling
 * the logging level for castor (used for serialization) <br>
 * </li>
 * <li>logs.Jalview.Level - Cache.log stringified level. <br>
 * </li>
 * <li>SCREEN_WIDTH</li>
 * <li>SCREEN_HEIGHT</li>
 * <li>SCREEN_Y=285</li>
 * <li>SCREEN_X=371</li>
 * <li>SHOW_FULLSCREEN boolean</li>
 * <li>FONT_NAME java font name for alignment text display</li>
 * <li>FONT_SIZE size of displayed alignment text</li>
 * <li>FONT_STYLE style of font displayed (sequence labels are always
 * italic)</li>
 * <li>GAP_SYMBOL character to treat as gap symbol (usually -,.,' ')</li>
 * <li>LAST_DIRECTORY last directory for browsing alignment</li>
 * <li>USER_DEFINED_COLOURS list of user defined colour scheme files</li>
 * <li>SHOW_FULL_ID show id with '/start-end' numbers appended</li>
 * <li>SHOW_IDENTITY show percentage identity annotation</li>
 * <li>SHOW_QUALITY show alignment quality annotation</li>
 * <li>SHOW_ANNOTATIONS show alignment annotation rows</li>
 * <li>SHOW_CONSERVATION show alignment conservation annotation</li>
 * <li>SORT_ANNOTATIONS currently either SEQUENCE_AND_LABEL or
 * LABEL_AND_SEQUENCE</li>
 * <li>SHOW_AUTOCALC_ABOVE true to show autocalculated annotations above
 * sequence annotations</li>
 * <li>CENTRE_COLUMN_LABELS centre the labels at each column in a displayed
 * annotation row</li>
 * <li>DEFAULT_COLOUR default colour scheme to apply for a new alignment</li>
 * <li>DEFAULT_FILE_FORMAT file format used to save</li>
 * <li>STARTUP_FILE file loaded on startup (may be a fully qualified url)</li>
 * <li>SHOW_STARTUP_FILE flag to control loading of startup file</li>
 * <li>VERSION the version of the jalview build</li>
 * <li>BUILD_DATE date of this build</li>
 * <li>LATEST_VERSION the latest jalview version advertised on the
 * www.jalview.org</li>
 * <li>PIR_MODELLER boolean indicating if PIR files are written with MODELLER
 * descriptions</li>
 * <li>(FASTA,MSF,PILEUP,CLUSTAL,BLC,PIR,PFAM)_JVSUFFIX boolean for adding jv
 * suffix to file</li>
 * <li>RECENT_URL list of recently retrieved URLs</li>
 * <li>RECENT_FILE list of recently opened files</li>
 * <li>USE_PROXY flag for whether a http proxy is to be used</li>
 * <li>PROXY_SERVER the proxy</li>
 * <li>PROXY_PORT</li>
 * <li>NOQUESTIONNAIRES true to prevent jalview from checking the questionnaire
 * service</li>
 * <li>QUESTIONNAIRE last questionnaire:responder id string from questionnaire
 * service</li>
 * <li>USAGESTATS (false - user prompted) Enable analytics tracker for
 * collecting usage statistics</li>
 * <li>SHOW_OVERVIEW boolean for overview window display</li>
 * <li>ANTI_ALIAS boolean for smooth fonts</li>
 * <li>RIGHT_ALIGN_IDS boolean</li>
 * <li>AUTO_CALC_CONSENSUS boolean for automatic recalculation of consensus</li>
 * <li>PAD_GAPS boolean</li>
 * <li>ID_ITALICS boolean</li>
 * <li>SHOW_JV_SUFFIX</li>
 * <li>WRAP_ALIGNMENT</li>
 * <li>EPS_RENDERING (Prompt each time|Lineart|Text) default for EPS rendering
 * style check</li>
 * <li>SORT_ALIGNMENT (No sort|Id|Pairwise Identity)</li>
 * <li>SEQUENCE_LINKS list of name|URL pairs for opening a url with
 * $SEQUENCE_ID$</li>
 * <li>STORED_LINKS list of name|url pairs which user has entered but are not
 * currently used
 * <li>DEFAULT_LINK name of single url to be used when user double clicks a
 * sequence id (must be in SEQUENCE_LINKS or STORED_LINKS)
 * <li>GROUP_LINKS list of name|URL[|&lt;separator&gt;] tuples - see
 * jalview.utils.GroupURLLink for more info</li>
 * <li>DEFAULT_BROWSER for unix</li>
 * <li>SHOW_MEMUSAGE boolean show memory usage and warning indicator on desktop
 * (false)</li>
 * <li>VERSION_CHECK (true) check for the latest release version from
 * www.jalview.org (or the alias given by the www.jalview.org property)</li>
 * <li>SHOW_NPFEATS_TOOLTIP (true) show non-positional features in the Sequence
 * ID tooltip</li>
 * <li>SHOW_DBREFS_TOOLTIP (true) show Database Cross References in the Sequence
 * ID tooltip</li>
 * <li>SHOW_UNCONSERVED (false) only render unconserved residues - conserved
 * displayed as '.'</li>
 * <li>SORT_BY_TREE (false) sort the current alignment view according to the
 * order of a newly displayed tree</li>
 * <li>DBFETCH_USEPICR (false) use PICR to recover valid DB references from
 * sequence ID strings before attempting retrieval from any datasource</li>
 * <li>SHOW_GROUP_CONSENSUS (false) Show consensus annotation for groups in the
 * alignment.</li>
 * <li>SHOW_GROUP_CONSERVATION (false) Show conservation annotation for groups
 * in the alignment.</li>
 * <li>SHOW_CONSENSUS_HISTOGRAM (false) Show consensus annotation row's
 * histogram.</li>
 * <li>SHOW_CONSENSUS_LOGO (false) Show consensus annotation row's sequence
 * logo.</li>
 * <li>NORMALISE_CONSENSUS_LOGO (false) Show consensus annotation row's sequence
 * logo normalised to row height rather than histogram height.</li>
 * <li>FOLLOW_SELECTIONS (true) Controls whether a new alignment view should
 * respond to selections made in other alignments containing the same sequences.
 * </li>
 * <li>SHOW_JWS2_SERVICES (true) when set to false, jalview will not
 * auto-discover JABAWS services</li>
 * <li>JWS2HOSTURLS comma-separated list of URLs to try for JABAWS services</li>
 * <li>SHOW_WSDISCOVERY_ERRORS (true) Controls if the web service URL discovery
 * warning dialog box is displayed.</li>
 * <li>ANNOTATIONCOLOUR_MIN (orange) Shade used for minimum value of annotation
 * when shading by annotation</li>
 * <li>ANNOTATIONCOLOUR_MAX (red) Shade used for maximum value of annotation
 * when shading by annotation</li>
 * <li>www.jalview.org (https://www.jalview.org) a property enabling all HTTP
 * requests to be redirected to a mirror of https://www.jalview.org</li>
 * <li>FIGURE_AUTOIDWIDTH (false) Expand the left hand column of an exported
 * alignment figure to accommodate even the longest sequence ID or annotation
 * label.</li>
 * <li>FIGURE_FIXEDIDWIDTH Specifies the width to use for the left-hand column
 * when exporting an alignment as a figure (setting FIGURE_AUTOIDWIDTH to true
 * will override this).</li>
 * <li>STRUCT_FROM_PDB (false) derive secondary structure annotation from PDB
 * record</li>
 * <li>USE_RNAVIEW (false) use RNAViewer to derive secondary structure</li>
 * <li>ADD_SS_ANN (false) add secondary structure annotation to alignment
 * display</li>
 * <li>ADD_TEMPFACT_ANN (false) add Temperature Factor annotation to alignment
 * display</li>
 * <li>STRUCTURE_DISPLAY choose from JMOL (default) or CHIMERA for 3D structure
 * display</li>
 * <li>CHIMERA_PATH specify full path to Chimera program (if non-standard)</li>
 * <li>ID_ORG_HOSTURL location of jalview service providing identifiers.org urls
 * </li>
 * <li>NONEWS - when set disables Jalview News from automatically appearing</li>
 * <li>NOHTMLTEMPLATES - when set, the
 * https://github.com/jalview/exporter-templates/tree/master/biojs repository is
 * not downloaded automatically</li>
 * <li>NOIDENTIFIERSSERVICE - when set, jalview won't automatically download
 * available URL linkouts via www.jalview.org/services/identifiers</li>
 * <li>
 * </ul>
 * Deprecated settings:
 * <ul>
 * *
 * <li>DISCOVERY_START - Boolean - controls if discovery services are queried on
 * startup (JWS1 services only)</li>
 * <li>DISCOVERY_URLS - comma separated list of Discovery Service endpoints.
 * (JWS1 services only)</li>
 * <li>SHOW_JWS1_SERVICES (true) enable or disable the original Jalview 2
 * services in the desktop GUI</li>
 * <li>ENABLE_RSBS_EDITOR (false for 2.7 release) enable or disable RSBS editing
 * panel in web service preferences</li>
 * </ul>
 * 
 * @author $author$
 * @version $Revision$
 */
public class Cache
{
  /**
   * property giving log4j level for CASTOR loggers
   */
  public static final String CASTORLOGLEVEL = "logs.Castor.level";

  /**
   * property giving log4j level for AXIS loggers
   */
  public static final String AXISLOGLEVEL = "logs.Axis.level";

  /**
   * property giving log4j level for Jalview Log
   */
  public static final String JALVIEWLOGLEVEL = "logs.Jalview.level";

  // for tests
  public static final String BOOTSTRAP_TEST = "BOOTSTRAP_TEST";

  /**
   * Sifts settings
   */
  public static final String DEFAULT_SIFTS_DOWNLOAD_DIR = System
          .getProperty("user.home") + File.separatorChar
          + ".sifts_downloads" + File.separatorChar;

  private final static String DEFAULT_CACHE_THRESHOLD_IN_DAYS = "2";

  private final static String DEFAULT_FAIL_SAFE_PID_THRESHOLD = "30";

  /**
   * Identifiers.org download settings
   */
  private static final String ID_ORG_FILE = System.getProperty("user.home")
          + File.separatorChar + ".identifiers.org.ids.json";

  /**
   * Allowed values are PDB or mmCIF
   */
  private final static String PDB_DOWNLOAD_FORMAT = PDBEntry.Type.MMCIF
          .toString();

  private final static String DEFAULT_PDB_FILE_PARSER = StructureImportSettings.StructureParser.JMOL_PARSER
          .toString();

  /*
   * a date formatter using a fixed (rather than the user's) locale; 
   * this ensures that date properties can be written and re-read successfully
   * even if the user changes their locale setting
   */
  private static final DateFormat date_format = SimpleDateFormat
          .getDateTimeInstance(SimpleDateFormat.MEDIUM,
                  SimpleDateFormat.MEDIUM, Locale.UK);

  /**
   * Initialises the Jalview Application Log
   */

  public final static String JALVIEW_LOGGER_NAME = "JalviewLogger";

  // save the proxy properties set at startup
  public final static String[] startupProxyProperties = {
      System.getProperty("http.proxyHost"),
      System.getProperty("http.proxyPort"),
      System.getProperty("https.proxyHost"),
      System.getProperty("https.proxyPort"),
      System.getProperty("http.proxyUser"),
      System.getProperty("http.proxyPassword"),
      System.getProperty("https.proxyUser"),
      System.getProperty("https.proxyPassword"),
      System.getProperty("http.nonProxyHosts") };

  public final static String PROXYTYPE_NONE = "none";

  // "false" and "true" for backward compatibility
  public final static String PROXYTYPE_SYSTEM = "false";

  public final static String PROXYTYPE_CUSTOM = "true";

  // in-memory only storage of proxy password, safer to use char array
  public static char[] proxyAuthPassword = null;

  /**
   * Session properties, set by command line, try not to affect stored
   * properties!
   */
  private static Map<String, String> sessionProperties = new HashMap<>();

  private static boolean bypassSessionProperties = false;

  public static void enableSessionProperties()
  {
    bypassSessionProperties = false;
  }

  public static void disableSessionProperties()
  {
    bypassSessionProperties = true;
  }

  /** Jalview Properties */
  public static Properties applicationProperties = new Properties()
  {
    // override results in properties output in alphabetical order
    @Override
    public synchronized Enumeration<Object> keys()
    {
      return Collections.enumeration(new TreeSet<>(super.keySet()));
    }
  };

  /* build Properties (not all saved to .jalview_properties) */
  public static Properties buildProperties = new Properties();

  /** Default file is ~/.jalview_properties */
  static String propertiesFile;

  private static final String fallbackPropertiesFile = ".jalview_properties";

  private static boolean propsAreReadOnly = Platform.isJS();

  public static boolean isPropsAreReadOnly()
  {
    return propsAreReadOnly;
  }

  public static void setPropsAreReadOnly(boolean propsAreReadOnly)
  {
    Cache.propsAreReadOnly = propsAreReadOnly;
  }

  private final static String JS_PROPERTY_PREFIX = "jalview_";

  /**
   * Loads properties from the given properties file. Any existing properties
   * are first cleared.
   */
  public static void loadProperties(String propsFile)
  {
    propertiesFile = propsFile;
    String releasePropertiesFile = null;
    boolean defaultProperties = false;
    if (propsFile == null && !propsAreReadOnly)
    {
      String channelPrefsFilename = ChannelProperties
              .getProperty("preferences.filename");
      String releasePrefsFilename = fallbackPropertiesFile;
      propertiesFile = System.getProperty("user.home") + File.separatorChar
              + channelPrefsFilename;
      releasePropertiesFile = System.getProperty("user.home")
              + File.separatorChar + releasePrefsFilename;
      defaultProperties = true;
    }
    else
    {
      // don't corrupt the file we've been given.
      propsAreReadOnly = true;
    }

    if (propertiesFile == null)
    { // BH 2019
      Platform.readInfoProperties(JS_PROPERTY_PREFIX,
              applicationProperties);
    }
    else
    {
      try
      {
        InputStream fis;
        try
        {
          // props file provided as URL
          fis = new URL(propertiesFile).openStream();
          if (!Jalview.quiet())
          {
            System.out.println(
                    "Loading jalview properties from : " + propertiesFile);
            System.out.println(
                    "Disabling Jalview writing to user's local properties file.");
          }
          propsAreReadOnly = true;
        } catch (Exception ex)
        {
          fis = null;
        }
        if (fis == null)
        {
          String readPropertiesFile = propertiesFile;
          // if we're using the usual properties file and the channel properties
          // file doesn't exist, read .jalview_properties
          // (but we'll still save to the channel properties file).
          if (defaultProperties && (!new File(propertiesFile).exists())
                  && (new File(releasePropertiesFile).exists()))
          {
            readPropertiesFile = releasePropertiesFile;
          }
          fis = new FileInputStream(readPropertiesFile);
        }
        applicationProperties.clear();
        applicationProperties.load(fis);

        // remove any old build properties

        deleteBuildProperties();
        fis.close();
      } catch (Exception ex)
      {
        if (!Jalview.quiet())
          System.out.println("Error reading properties file: " + ex);
      }
    }

    /* TO BE REPLACED WITH PROXY_TYPE SETTINGS 
    if (getDefault("USE_PROXY", false))
    {
      String proxyServer = getDefault("PROXY_SERVER", ""),
              proxyPort = getDefault("PROXY_PORT", "8080");
    }
    */

    // PROXY TYPE settings (now three options "none", "false", "true", but using
    // backward compatible strings)
    String proxyType = getDefault("USE_PROXY", PROXYTYPE_SYSTEM);
    // default to upgrading old settings
    switch (proxyType)
    {
    case PROXYTYPE_NONE:
      clearProxyProperties();
      break;
    case PROXYTYPE_SYSTEM: // use system settings
      resetProxyProperties();
      break;
    case PROXYTYPE_CUSTOM: // use specified proxy settings
      String httpHost = getDefault("PROXY_SERVER", "");
      String httpPort = getDefault("PROXY_PORT", "8080");
      String httpsHost = getDefault("PROXY_SERVER_HTTPS", httpHost);
      String httpsPort = getDefault("PROXY_PORT_HTTPS", httpPort);
      String httpUser = getDefault("PROXY_AUTH_USER", null);
      // https.proxyUser and https.proxyPassword are not able to be
      // independently set in Preferences yet (or http.nonProxyHosts)
      String httpsUser = getDefault("PROXY_AUTH_USER_HTTPS", httpUser);
      setProxyProperties(httpHost, httpPort, httpsHost, httpsPort, httpUser,
              proxyAuthPassword, httpsUser, proxyAuthPassword, "localhost");
      break;
    default:
      String message = "Incorrect PROXY_TYPE - should be 'none' (clear proxy properties), 'false' (system settings), 'true' (custom settings): "
              + proxyType;
      Console.warn(message);
    }

    // LOAD THE AUTHORS FROM THE authors.props file
    String authorDetails = resolveResourceURLFor("/authors.props");

    try
    {
      if (authorDetails != null)
      {
        URL localJarFileURL = new URL(authorDetails);
        InputStream in = localJarFileURL.openStream();
        applicationProperties.load(in);
        in.close();
      }
    } catch (Exception ex)
    {
      if (!Jalview.quiet())
        System.out.println("Error reading author details: " + ex);
      authorDetails = null;
    }
    if (authorDetails == null)
    {
      applicationProperties.remove("AUTHORS");
      applicationProperties.remove("AUTHORFNAMES");
      applicationProperties.remove("YEAR");
    }

    loadBuildProperties(false);

    SiftsSettings
            .setMapWithSifts(Cache.getDefault("MAP_WITH_SIFTS", false));

    SiftsSettings.setSiftDownloadDirectory(Cache
            .getDefault("sifts_download_dir", DEFAULT_SIFTS_DOWNLOAD_DIR));

    SiftsSettings.setFailSafePIDThreshold(
            Cache.getDefault("sifts_fail_safe_pid_threshold",
                    DEFAULT_FAIL_SAFE_PID_THRESHOLD));

    SiftsSettings.setCacheThresholdInDays(
            Cache.getDefault("sifts_cache_threshold_in_days",
                    DEFAULT_CACHE_THRESHOLD_IN_DAYS));

    IdOrgSettings.setUrl(getDefault("ID_ORG_HOSTURL",
            "https://www.jalview.org/services/identifiers"));
    IdOrgSettings.setDownloadLocation(ID_ORG_FILE);

    StructureImportSettings.setDefaultStructureFileFormat(
            Cache.getDefault("PDB_DOWNLOAD_FORMAT", PDB_DOWNLOAD_FORMAT));
    StructureImportSettings
            .setDefaultPDBFileParser(DEFAULT_PDB_FILE_PARSER);
    // StructureImportSettings
    // .setDefaultPDBFileParser(Cache.getDefault(
    // "DEFAULT_PDB_FILE_PARSER", DEFAULT_PDB_FILE_PARSER));

    String jnlpVersion = System.getProperty("jalview.version");

    // jnlpVersion will be null if a latest version check for the channel needs
    // to be done
    // Dont do this check if running in headless mode

    if (jnlpVersion == null && getDefault("VERSION_CHECK", true)
            && (System.getProperty("java.awt.headless") == null || System
                    .getProperty("java.awt.headless").equals("false")))
    {

      class VersionChecker extends Thread
      {

        @Override
        public void run()
        {
          String remoteBuildPropertiesUrl = Cache
                  .getAppbaseBuildProperties();

          String orgtimeout = System
                  .getProperty("sun.net.client.defaultConnectTimeout");
          if (orgtimeout == null)
          {
            orgtimeout = "30";
            Console.debug("Setting default net timeout to " + orgtimeout
                    + " seconds.");
          }
          String remoteVersion = null;
          if (remoteBuildPropertiesUrl.startsWith("http"))
          {
            try
            {
              System.setProperty("sun.net.client.defaultConnectTimeout",
                      "5000");

              URL url = new URL(remoteBuildPropertiesUrl);

              BufferedReader in = new BufferedReader(
                      new InputStreamReader(url.openStream()));

              Properties remoteBuildProperties = new Properties();
              remoteBuildProperties.load(in);
              remoteVersion = remoteBuildProperties.getProperty("VERSION");
            } catch (Exception ex)
            {
              if (!Jalview.quiet())
              {
                System.out.println(
                        "Non-fatal exception when checking version at "
                                + remoteBuildPropertiesUrl + ":");
                System.out.println(ex);
              }
              remoteVersion = getProperty("VERSION");
            }
          }
          System.setProperty("sun.net.client.defaultConnectTimeout",
                  orgtimeout);

          setProperty("LATEST_VERSION", remoteVersion);
        }
      }

      VersionChecker vc = new VersionChecker();
      vc.start();
    }
    else
    {
      if (jnlpVersion != null)
      {
        setProperty("LATEST_VERSION", jnlpVersion);
      }
      else
      {
        applicationProperties.remove("LATEST_VERSION");
      }
    }

    // LOAD USERDEFINED COLOURS
    Cache.initUserColourSchemes(getProperty("USER_DEFINED_COLOURS"));
    jalview.io.PIRFile.useModellerOutput = Cache.getDefault("PIR_MODELLER",
            false);
  }

  /**
   * construct a resource URL for the given absolute resource pathname
   * 
   * @param resourcePath
   * @return
   */
  private static String resolveResourceURLFor(String resourcePath)
  {
    String url = null;
    if (Platform.isJS() || !Cache.class.getProtectionDomain()
            .getCodeSource().getLocation().toString().endsWith(".jar"))
    {
      try
      {
        url = Cache.class.getResource(resourcePath).toString();
      } catch (Exception ex)
      {
        System.err.println("Failed to resolve resource " + resourcePath
                + ": " + ex.getMessage());
      }
    }
    else
    {
      url = "jar:".concat(Cache.class.getProtectionDomain().getCodeSource()
              .getLocation().toString().concat("!" + resourcePath));
    }
    return url;
  }

  public static void loadBuildProperties(boolean reportVersion)
  {
    String codeInstallation = getProperty("INSTALLATION");
    boolean printVersion = codeInstallation == null;

    /*
     * read build properties - from the Jalview jar for a Java distribution,
     * or from codebase file in test or JalviewJS context
     */
    try
    {
      String buildDetails = resolveResourceURLFor("/.build_properties");
      URL localJarFileURL = new URL(buildDetails);
      InputStream in = localJarFileURL.openStream();
      buildProperties.load(in);
      in.close();
      if (buildProperties.getProperty("BUILD_DATE", null) != null)
      {
        applicationProperties.put("BUILD_DATE",
                buildProperties.getProperty("BUILD_DATE"));
      }
      if (buildProperties.getProperty("INSTALLATION", null) != null)
      {
        applicationProperties.put("INSTALLATION",
                buildProperties.getProperty("INSTALLATION"));
      }
      if (buildProperties.getProperty("VERSION", null) != null)
      {
        applicationProperties.put("VERSION",
                buildProperties.getProperty("VERSION"));
      }
      if (buildProperties.getProperty("JAVA_COMPILE_VERSION", null) != null)
      {
        applicationProperties.put("JAVA_COMPILE_VERSION",
                buildProperties.getProperty("JAVA_COMPILE_VERSION"));
      }
    } catch (Exception ex)
    {
      if (!Jalview.quiet())
        System.out.println("Error reading build details: " + ex);
      applicationProperties.remove("VERSION");
    }
    String codeVersion = getProperty("VERSION");
    codeInstallation = getProperty("INSTALLATION");

    if (codeVersion == null)
    {
      // THIS SHOULD ONLY BE THE CASE WHEN TESTING!!
      codeVersion = "Test";
      codeInstallation = "";
    }
    else
    {
      codeInstallation = " (" + codeInstallation + ")";
    }
    setProperty("VERSION", codeVersion);
    new BuildDetails(codeVersion, null, codeInstallation);
    if (printVersion && reportVersion)
    {
      System.out.println(ChannelProperties.getProperty("app_name")
              + " version: " + codeVersion + codeInstallation);
    }
  }

  private static void deleteBuildProperties()
  {
    applicationProperties.remove("LATEST_VERSION");
    applicationProperties.remove("VERSION");
    applicationProperties.remove("AUTHORS");
    applicationProperties.remove("AUTHORFNAMES");
    applicationProperties.remove("YEAR");
    applicationProperties.remove("BUILD_DATE");
    applicationProperties.remove("INSTALLATION");
  }

  /**
   * Gets Jalview application property of given key. Returns null if key not
   * found
   * 
   * @param key
   *          Name of property
   * 
   * @return Property value
   */
  public static String getProperty(String key)
  {
    return getProperty(key, false);
  }

  public static String getProperty(String key,
          boolean skipSessionProperties)
  {
    String prop = null;
    if (!(skipSessionProperties || bypassSessionProperties))
    {
      prop = getSessionProperty(key);
    }
    if (prop == null)
    {
      prop = applicationProperties.getProperty(key);
    }
    if (prop == null && Platform.isJS())
    {
      prop = applicationProperties.getProperty(Platform.getUniqueAppletID()
              + "_" + JS_PROPERTY_PREFIX + key);
    }
    return prop;
  }

  /**
   * These methods are used when checking if the saved preference is different
   * to the default setting
   */

  public static boolean getDefault(String property, boolean def)
  {
    String string = getProperty(property);
    if (string != null)
    {
      def = Boolean.valueOf(string).booleanValue();
    }

    return def;
  }

  public static int getDefault(String property, int def)
  {
    String string = getProperty(property);
    if (string != null)
    {
      try
      {
        def = Integer.parseInt(string);
      } catch (NumberFormatException e)
      {
        if (!Jalview.quiet())
          System.out.println("Error parsing int property '" + property
                  + "' with value '" + string + "'");
      }
    }

    return def;
  }

  /**
   * Answers the value of the given property, or the supplied default value if
   * the property is not set
   */
  public static String getDefault(String property, String def)
  {
    String value = getProperty(property);
    return value == null ? def : value;
  }

  /**
   * Stores property in the file "HOME_DIR/.jalview_properties"
   * 
   * @param key
   *          Name of object
   * @param obj
   *          String value of property
   * 
   * @return previous value of property (or null)
   */
  public static Object setProperty(String key, String obj)
  {
    Object oldValue = null;
    try
    {
      oldValue = applicationProperties.setProperty(key, obj);
      if (propertiesFile != null && !propsAreReadOnly
      // don't rewrite if new value is same as old value
              && !((obj == null && oldValue == null)
                      || (obj != null && obj.equals(oldValue))))
      {
        // reset the session property too
        if (sessionProperties.containsKey(key))
        {
          sessionProperties.remove(key);
        }
        FileOutputStream out = new FileOutputStream(propertiesFile);
        applicationProperties.store(out, "---JalviewX Properties File---");
        out.close();
      }
    } catch (Exception ex)
    {
      if (!Jalview.quiet())
        System.out.println(
                "Error setting property: " + key + " " + obj + "\n" + ex);
    }
    return oldValue;
  }

  /**
   * remove the specified property from the jalview properties file
   * 
   * @param string
   */
  public static void removeProperty(String string)
  {
    applicationProperties.remove(string);
    saveProperties();
  }

  /**
   * save the properties to the jalview properties path
   */
  public static void saveProperties()
  {
    if (!propsAreReadOnly)
    {
      try
      {
        FileOutputStream out = new FileOutputStream(propertiesFile);
        applicationProperties.store(out, "---JalviewX Properties File---");
        out.close();
      } catch (Exception ex)
      {
        if (!Jalview.quiet())
          System.out.println("Error saving properties: " + ex);
      }
    }
  }

  /**
   * internal vamsas class discovery state
   */
  private static int vamsasJarsArePresent = -1;

  /**
   * Searches for vamsas client classes on class path.
   * 
   * @return true if vamsas client is present on classpath
   */
  public static boolean vamsasJarsPresent()
  {
    if (vamsasJarsArePresent == -1)
    {
      try
      {
        if (jalview.jbgui.GDesktop.class.getClassLoader()
                .loadClass("uk.ac.vamsas.client.VorbaId") != null)
        {
          Console.debug(
                  "Found Vamsas Classes (uk.ac..vamsas.client.VorbaId can be loaded)");
          vamsasJarsArePresent = 1;
          JLoggerLog4j lvclient = JLoggerLog4j.getLogger("uk.ac.vamsas",
                  Console.getCachedLogLevel("logs.Vamsas.Level"));
          JLoggerLog4j.addAppender(lvclient, Console.log,
                  JALVIEW_LOGGER_NAME);
          // Tell the user that debug is enabled
          lvclient.debug(ChannelProperties.getProperty("app_name")
                  + " Vamsas Client Debugging Output Follows.");
        }
      } catch (Exception e)
      {
        vamsasJarsArePresent = 0;
        Console.debug("Vamsas Classes are not present");
      }
    }
    return (vamsasJarsArePresent > 0);
  }

  /**
   * internal vamsas class discovery state
   */
  private static int groovyJarsArePresent = -1;

  /**
   * Searches for vamsas client classes on class path.
   * 
   * @return true if vamsas client is present on classpath
   */
  public static boolean groovyJarsPresent()
  {
    if (groovyJarsArePresent == -1)
    {
      try
      {
        if (Cache.class.getClassLoader()
                .loadClass("groovy.lang.GroovyObject") != null)
        {
          Console.debug(
                  "Found Groovy (groovy.lang.GroovyObject can be loaded)");
          groovyJarsArePresent = 1;
          JLoggerLog4j lgclient = JLoggerLog4j.getLogger("groovy",
                  Console.getCachedLogLevel("logs.Groovy.Level"));
          JLoggerLog4j.addAppender(lgclient, Console.log,
                  JALVIEW_LOGGER_NAME);
          // Tell the user that debug is enabled
          lgclient.debug(ChannelProperties.getProperty("app_name")
                  + " Groovy Client Debugging Output Follows.");
        }
      } catch (Error e)
      {
        groovyJarsArePresent = 0;
        Console.debug("Groovy Classes are not present", e);
      } catch (Exception e)
      {
        groovyJarsArePresent = 0;
        Console.debug("Groovy Classes are not present");
      }
    }
    return (groovyJarsArePresent > 0);
  }

  /**
   * Initialise the tracker if it is not done already.
   */
  public static void initAnalytics()
  {
    Plausible.setEnabled(true);

    String appName = ChannelProperties.getProperty("app_name") + " Desktop";
    String version = Cache.getProperty("VERSION") + "_"
            + Cache.getDefault("BUILD_DATE", "unknown");
    String path;
    /* we don't want to encode ':' as "%3A" for backward compatibility with the UA setup
    try
    {
      path = "/" + String.join("/", URLEncoder.encode(appName, "UTF-8"),
              URLEncoder.encode(version, "UTF-8"),
              URLEncoder.encode(APPLICATION_STARTED, "UTF-8"));
    } catch (UnsupportedEncodingException e)
    {
    */
    List<String> pathParts = new ArrayList<>();
    pathParts.add(appName);
    pathParts.add(version);
    pathParts.add(APPLICATION_STARTED);
    path = ("/" + String.join("/", pathParts)).replace(' ', '+');
    /*
    }
    */
    Plausible plausible = Plausible.getInstance();

    // This will send a new "application_launch" event with parameters
    // including the old-style "path", the channel name and version
    plausible.sendEvent("application_launch", path, true);
  }

  private static final String APPLICATION_STARTED = "Application Started";

  /**
   * get the user's default colour if available
   * 
   * @param property
   * @param defcolour
   * @return
   */
  public static Color getDefaultColour(String property, Color defcolour)
  {
    String colprop = getProperty(property);
    if (colprop == null)
    {
      return defcolour;
    }
    Color col = ColorUtils.parseColourString(colprop);
    if (col == null)
    {
      Console.warn("Couldn't parse '" + colprop + "' as a colour for "
              + property);
    }
    return (col == null) ? defcolour : col;
  }

  /**
   * store a colour as a Jalview user default property
   * 
   * @param property
   * @param colour
   */
  public static void setColourProperty(String property, Color colour)
  {
    setProperty(property, jalview.util.Format.getHexString(colour));
  }

  /**
   * Stores a formatted date in a jalview property, using a fixed locale.
   * 
   * @param propertyName
   * @param date
   * @return the formatted date string
   */
  public static String setDateProperty(String propertyName, Date date)
  {
    String formatted = date_format.format(date);
    setProperty(propertyName, formatted);
    return formatted;
  }

  /**
   * Reads a date stored in a Jalview property, parses it (using a fixed locale
   * format) and returns as a Date, or null if parsing fails
   * 
   * @param propertyName
   * @return
   * 
   */
  public static Date getDateProperty(String propertyName)
  {
    String val = getProperty(propertyName);
    if (val != null)
    {
      try
      {
        return date_format.parse(val);
      } catch (Exception ex)
      {
        System.err.println("Invalid or corrupt date in property '"
                + propertyName + "' : value was '" + val + "'");
      }
    }
    return null;
  }

  /**
   * get and parse a property as an integer. send any parsing problems to
   * System.err
   * 
   * @param property
   * @return null or Integer
   */
  public static Integer getIntegerProperty(String property)
  {
    String val = getProperty(property);
    if (val != null && (val = val.trim()).length() > 0)
    {
      try
      {
        return Integer.valueOf(val);
      } catch (NumberFormatException x)
      {
        System.err.println("Invalid integer in property '" + property
                + "' (value was '" + val + "')");
      }
    }
    return null;
  }

  /**
   * Set the specified value, or remove it if null or empty. Does not save the
   * properties file.
   * 
   * @param propName
   * @param value
   */
  public static void setOrRemove(String propName, String value)
  {
    if (propName == null)
    {
      return;
    }
    if (value == null || value.trim().length() < 1)
    {
      Cache.applicationProperties.remove(propName);
    }
    else
    {
      Cache.applicationProperties.setProperty(propName, value);
    }
  }

  /**
   * Loads in user colour schemes from files.
   * 
   * @param files
   *          a '|'-delimited list of file paths
   */
  public static void initUserColourSchemes(String files)
  {
    if (files == null || files.length() == 0)
    {
      return;
    }

    // In case colours can't be loaded, we'll remove them
    // from the default list here.
    StringBuffer coloursFound = new StringBuffer();
    StringTokenizer st = new StringTokenizer(files, "|");
    while (st.hasMoreElements())
    {
      String file = st.nextToken();
      try
      {
        UserColourScheme ucs = ColourSchemeLoader.loadColourScheme(file);
        if (ucs != null)
        {
          if (coloursFound.length() > 0)
          {
            coloursFound.append("|");
          }
          coloursFound.append(file);
          ColourSchemes.getInstance().registerColourScheme(ucs);
        }
      } catch (Exception ex)
      {
        if (!Jalview.quiet())
          System.out.println("Error loading User ColourFile\n" + ex);
      }
    }
    if (!files.equals(coloursFound.toString()))
    {
      if (coloursFound.toString().length() > 1)
      {
        setProperty(UserDefinedColours.USER_DEFINED_COLOURS,
                coloursFound.toString());
      }
      else
      {
        applicationProperties
                .remove(UserDefinedColours.USER_DEFINED_COLOURS);
      }
    }
  }

  /**
   * Initial logging information helper for various versions output
   * 
   * @param prefix
   * @param value
   * @param defaultValue
   */
  private static void appendIfNotNull(StringBuilder sb, String prefix,
          String value, String suffix, String defaultValue)
  {
    if (value == null && defaultValue == null)
    {
      return;
    }
    if (prefix != null)
      sb.append(prefix);
    sb.append(value == null ? defaultValue : value);
    if (suffix != null)
      sb.append(suffix);
  }

  /**
   * 
   * @return Jalview version, build details and JVM platform version for console
   */
  public static String getVersionDetailsForConsole()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(ChannelProperties.getProperty("app_name"))
            .append(" Version: ");
    sb.append(Cache.getDefault("VERSION", "TEST"));
    sb.append("\n");
    sb.append(ChannelProperties.getProperty("app_name"))
            .append(" Installation: ");
    sb.append(Cache.getDefault("INSTALLATION", "unknown"));
    sb.append("\n");
    sb.append("Build Date: ");
    sb.append(Cache.getDefault("BUILD_DATE", "unknown"));
    sb.append("\n");
    sb.append("Java version: ");
    sb.append(System.getProperty("java.version"));
    sb.append("\n");
    sb.append("Java platform: ");
    sb.append(System.getProperty("os.arch"));
    sb.append(" ");
    sb.append(System.getProperty("os.name"));
    sb.append(" ");
    sb.append(System.getProperty("os.version"));
    sb.append("\n");
    appendIfNotNull(sb, "Install4j version: ",
            System.getProperty("sys.install4jVersion"), "\n", null);
    appendIfNotNull(sb, "Install4j template version: ",
            System.getProperty("installer_template_version"), "\n", null);
    appendIfNotNull(sb, "Launcher version: ",
            System.getProperty("launcher_version"), "\n", null);
    LookAndFeel laf = UIManager.getLookAndFeel();
    String lafName = laf == null ? "Not obtained" : laf.getName();
    String lafClass = laf == null ? "unknown" : laf.getClass().getName();
    sb.append("LookAndFeel: ");
    sb.append(lafName);
    sb.append(" (");
    sb.append(lafClass);
    sb.append(")\n");
    appendIfNotNull(sb, "Channel: ",
            ChannelProperties.getProperty("channel"), "\n", null);
    if (Console.isDebugEnabled()
            || !"release".equals(ChannelProperties.getProperty("channel")))
    {
      appendIfNotNull(sb, "Getdown appdir: ",
              System.getProperty("getdowninstanceappdir"), "\n", null);
      appendIfNotNull(sb, "Getdown appbase: ",
              System.getProperty("getdowninstanceappbase"), "\n", null);
      appendIfNotNull(sb, "Java home: ", System.getProperty("java.home"),
              "\n", "unknown");
      appendIfNotNull(sb, "Preferences file: ", propertiesFile, "\n",
              "unknown");
    }
    return sb.toString();
  }

  /**
   * 
   * @return build details as reported in splashscreen
   */
  public static String getBuildDetailsForSplash()
  {
    // consider returning more human friendly info
    // eg 'built from Source' or update channel
    return Cache.getDefault("INSTALLATION", "unknown");
  }

  public static String getStackTraceString(Throwable t)
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    return sw.toString();
  }

  // proxy properties methods
  public static void clearProxyProperties()
  {
    setProxyProperties(null, null, null, null, null, null, null, null,
            null);
  }

  public static void resetProxyProperties()
  {
    setProxyProperties(startupProxyProperties[0], startupProxyProperties[1],
            startupProxyProperties[2], startupProxyProperties[3],
            startupProxyProperties[4],
            startupProxyProperties[5] == null ? null
                    : startupProxyProperties[5].toCharArray(),
            startupProxyProperties[6],
            startupProxyProperties[7] == null ? null
                    : startupProxyProperties[7].toCharArray(),
            startupProxyProperties[8]);
    StringBuilder sb = new StringBuilder();
    sb.append("Setting proxy properties to: http.proxyHost=")
            .append(startupProxyProperties[0]).append(", http.proxyPort=")
            .append(startupProxyProperties[1])
            .append(startupProxyProperties[4] != null
                    && !startupProxyProperties[4].isEmpty()
                            ? " [" + startupProxyProperties[4] + "]"
                            : "")
            .append(", https.proxyHost=").append(startupProxyProperties[2])
            .append(", https.proxyPort=").append(startupProxyProperties[3])
            .append(startupProxyProperties[6] != null
                    && !startupProxyProperties[6].isEmpty()
                            ? " [" + startupProxyProperties[6] + "]"
                            : "");

    Console.debug(sb.toString());
  }

  public static void setProxyPropertiesFromPreferences()
  {
    setProxyPropertiesFromPreferences(Cache.PROXYTYPE_SYSTEM);
  }

  public static void setProxyPropertiesFromPreferences(
          String previousProxyType)
  {
    String proxyType = Cache.getDefault("USE_PROXY",
            Cache.PROXYTYPE_SYSTEM);
    if (previousProxyType != null
            && !proxyType.equals(Cache.PROXYTYPE_CUSTOM) // always apply
                                                         // customProxy
            && proxyType.equals(previousProxyType))
    {
      // no change
      return;
    }
    switch (proxyType)
    {
    case Cache.PROXYTYPE_NONE:
      if (!previousProxyType.equals(proxyType))
      {
        Console.info("Setting no proxy settings");
        Cache.setProxyProperties(null, null, null, null, null, null, null,
                null, null);
      }
      break;
    case Cache.PROXYTYPE_CUSTOM:
      // always re-set a custom proxy -- it might have changed, particularly
      // password
      Console.info("Setting custom proxy settings");
      boolean proxyAuthSet = Cache.getDefault("PROXY_AUTH", false);
      Cache.setProxyProperties(Cache.getDefault("PROXY_SERVER", null),
              Cache.getDefault("PROXY_PORT", null),
              Cache.getDefault("PROXY_SERVER_HTTPS", null),
              Cache.getDefault("PROXY_PORT_HTTPS", null),
              proxyAuthSet ? Cache.getDefault("PROXY_AUTH_USERNAME", "")
                      : null,
              proxyAuthSet ? Cache.proxyAuthPassword : null,
              proxyAuthSet ? Cache.getDefault("PROXY_AUTH_USERNAME", "")
                      : null,
              proxyAuthSet ? Cache.proxyAuthPassword : null, "localhost");
      break;
    default: // system proxy settings by default
      Console.info("Setting system proxy settings");
      Cache.resetProxyProperties();
    }
  }

  public static void setProxyProperties(String httpHost, String httpPort,
          String httpsHost, String httpsPort, String httpUser,
          char[] httpPassword, String httpsUser, char[] httpsPassword,
          String nonProxyHosts)
  {
    setOrClearSystemProperty("http.proxyHost", httpHost);
    setOrClearSystemProperty("http.proxyPort", httpPort);
    setOrClearSystemProperty("https.proxyHost", httpsHost);
    setOrClearSystemProperty("https.proxyPort", httpsPort);
    setOrClearSystemProperty("http.proxyUser", httpUser);
    setOrClearSystemProperty("https.proxyUser", httpsUser);
    // note: passwords for http.proxyPassword and https.proxyPassword are sent
    // via the Authenticator, properties do not need to be set

    // are we using a custom proxy (password prompt might be required)?
    boolean customProxySet = getDefault("USE_PROXY", PROXYTYPE_SYSTEM)
            .equals(PROXYTYPE_CUSTOM);

    /*
     * A bug in Java means the AuthCache does not get reset, so once it has working credentials,
     * it never asks for more, so changing the Authenticator has no effect (as getPasswordAuthentication()
     * is not re-called).
     * This could lead to password leak to a hostile proxy server, so I'm putting in a hack to clear
     * the AuthCache.
     * see https://www.generacodice.com/en/articolo/154918/Reset-the-Authenticator-credentials
     * ...
     * Turns out this is only accessible in Java 8, and not in Java 9 onwards, so commenting out
     */
    /*
    try
    {
      sun.net.www.protocol.http.AuthCacheValue
              .setAuthCache(new sun.net.www.protocol.http.AuthCacheImpl());
    } catch (Throwable t)
    {
      Cache.error(t.getMessage());
      Cache.debug(getStackTraceString(t));
    }
    */

    if (httpUser != null || httpsUser != null)
    {
      try
      {
        char[] displayHttpPw = new char[httpPassword == null ? 0
                : httpPassword.length];
        Arrays.fill(displayHttpPw, '*');
        Console.debug(
                "CACHE Proxy: setting new Authenticator with httpUser='"
                        + httpUser + "' httpPassword='" + displayHttpPw
                        + "'");
        if (!Platform.isJS())
        /* *
         * java.net.Authenticator not implemented in SwingJS yet
         * 
         * @j2sIgnore
         * 
         */
        {
          Authenticator.setDefault(new Authenticator()
          {
            @Override
            protected PasswordAuthentication getPasswordAuthentication()
            {
              if (getRequestorType() == RequestorType.PROXY)
              {
                String protocol = getRequestingProtocol();
                boolean needProxyPasswordSet = false;
                if (customProxySet &&
                // we have a username but no password for the scheme being
                // requested
                        (protocol.equalsIgnoreCase("http")
                                && (httpUser != null
                                        && httpUser.length() > 0
                                        && (httpPassword == null
                                                || httpPassword.length == 0)))
                        || (protocol.equalsIgnoreCase("https")
                                && (httpsUser != null
                                        && httpsUser.length() > 0
                                        && (httpsPassword == null
                                                || httpsPassword.length == 0))))
                {
                  // open Preferences -> Connections
                  String message = MessageManager
                          .getString("label.proxy_password_required");
                  Preferences.openPreferences(
                          Preferences.TabRef.CONNECTIONS_TAB, message);
                  Preferences.getInstance()
                          .proxyAuthPasswordCheckHighlight(true, true);
                }
                else
                {
                  try
                  {
                    if (protocol.equalsIgnoreCase("http")
                            && getRequestingHost()
                                    .equalsIgnoreCase(httpHost)
                            && getRequestingPort() == Integer
                                    .valueOf(httpPort))
                    {
                      Console.debug(
                              "AUTHENTICATOR returning PasswordAuthentication(\""
                                      + httpUser + "\", '"
                                      + new String(displayHttpPw) + "')");
                      return new PasswordAuthentication(httpUser,
                              httpPassword);
                    }
                    if (protocol.equalsIgnoreCase("https")
                            && getRequestingHost()
                                    .equalsIgnoreCase(httpsHost)
                            && getRequestingPort() == Integer
                                    .valueOf(httpsPort))
                    {
                      char[] displayHttpsPw = new char[httpPassword.length];
                      Arrays.fill(displayHttpsPw, '*');
                      Console.debug(
                              "AUTHENTICATOR returning PasswordAuthentication(\""
                                      + httpsUser + "\", '" + displayHttpsPw
                                      + "'");
                      return new PasswordAuthentication(httpsUser,
                              httpsPassword);
                    }
                  } catch (NumberFormatException e)
                  {
                    Console.error("Problem with proxy port values [http:"
                            + httpPort + ", https:" + httpsPort + "]");
                  }
                  Console.debug(
                          "AUTHENTICATOR after trying to get PasswordAuthentication");
                }
              }
              // non proxy request
              Console.debug("AUTHENTICATOR returning null");
              return null;
            }
          });
        } // end of j2sIgnore for java.net.Authenticator

        // required to re-enable basic authentication (should be okay for a
        // local proxy)
        Console.debug(
                "AUTHENTICATOR setting property 'jdk.http.auth.tunneling.disabledSchemes' to \"\"");
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
      } catch (SecurityException e)
      {
        Console.error("Could not set default Authenticator");
        Console.debug(getStackTraceString(e));
      }
    }
    else
    {
      // reset the Authenticator to protect http.proxyUser and
      // http.proxyPassword Just In Case
      /* as noted above, due to bug in java this doesn't work if the sun.net.www.protocol.http.AuthCache
       * has working credentials. No workaround for Java 11.
       */
      if (!Platform.isJS())
      /* *
       * java.net.Authenticator not implemented in SwingJS yet
       * 
       * @j2sIgnore
       * 
       */
      {
        Console.debug(
                "AUTHENTICATOR setting default Authenticator to null");
        Authenticator.setDefault(null);
      }
    }

    // nonProxyHosts not currently configurable in Preferences
    Console.debug(
            "AUTHENTICATOR setting property 'http.nonProxyHosts' to \""
                    + nonProxyHosts + "\"");
    setOrClearSystemProperty("http.nonProxyHosts", nonProxyHosts);
  }

  public static void setOrClearSystemProperty(String key, char[] value)
  {
    setOrClearSystemProperty(key,
            (value == null) ? null : new String(value));
  }

  public static void setOrClearSystemProperty(String key, String value)
  {
    if (key == null)
    {
      return;
    }
    if (value == null)
    {
      System.clearProperty(key);
    }
    else
    {
      System.setProperty(key, value);
    }
  }

  /**
   * Getdown appbase methods
   */

  private static final String releaseAppbase;

  private static String getdownAppbase;

  private static String getdownDistDir;

  static
  {
    if (!Platform.isJS())
    {
      Float specversion = Float
              .parseFloat(System.getProperty("java.specification.version"));
      releaseAppbase = (specversion < 9)
              ? "https://www.jalview.org/getdown/release/1.8"
              : "https://www.jalview.org/getdown/release/11";
    }
    else
    {
      // this value currenly made up, can be changed to URL that will be
      // "https://www.jalview.org/jalview-js/swingjs/j2s/build_properties"
      releaseAppbase = "https://www.jalview.org/jalview-js";
      getdownAppbase = releaseAppbase;
      getdownDistDir = "/swingjs/j2s";
    }
  }

  // look for properties (passed in by getdown) otherwise default to release
  private static void setGetdownAppbase()
  {
    if (getdownAppbase != null)
    {
      return;
    }
    String appbase = System.getProperty("getdownappbase");
    String distDir = System.getProperty("getdowndistdir");
    if (appbase == null)
    {
      appbase = buildProperties.getProperty("GETDOWNAPPBASE");
      distDir = buildProperties.getProperty("GETDOWNAPPDISTDIR");
    }
    if (appbase == null)
    {
      appbase = releaseAppbase;
      distDir = "release";
    }
    if (appbase.endsWith("/"))
    {
      appbase = appbase.substring(0, appbase.length() - 1);
    }
    if (distDir == null)
    {
      distDir = appbase.equals(releaseAppbase) ? "release" : "alt";
    }
    getdownAppbase = appbase;
    getdownDistDir = distDir;
  }

  public static String getGetdownAppbase()
  {
    setGetdownAppbase();
    return getdownAppbase;
  }

  public static String getAppbaseBuildProperties()
  {
    String appbase = getGetdownAppbase();
    return appbase + "/" + getdownDistDir + "/build_properties";
  }

  private static final Collection<String> bootstrapProperties = new ArrayList<>(
          Arrays.asList(JALVIEWLOGLEVEL, BOOTSTRAP_TEST));

  public static Properties bootstrapProperties(String filename)
  {
    Properties bootstrapProps = new Properties();
    File file = null;
    if (filename != null)
    {
      file = new File(filename);
    }
    if (file == null || !file.exists())
    {
      String channelPrefsFilename = ChannelProperties
              .getProperty("preferences.filename");
      String propertiesFilename = System.getProperty("user.home")
              + File.separatorChar + channelPrefsFilename;
      file = new File(propertiesFilename);
    }
    if (file == null || !file.exists())
    {
      String releasePrefsFilename = fallbackPropertiesFile;
      String releasePropertiesFilename = System.getProperty("user.home")
              + File.separatorChar + releasePrefsFilename;
      file = new File(releasePropertiesFilename);
    }

    if (filename == null)
      return null;
    if (!file.exists())
    {
      System.err.println("Could not load bootstrap preferences file '"
              + filename + "'");
      return null;
    }

    try
    {
      FileInputStream in = new FileInputStream(file.getAbsoluteFile());
      Properties props = new Properties();
      props.load(in);
      for (String prop : bootstrapProperties)
      {
        if (props.containsKey(prop))
          bootstrapProps.put(prop, props.getProperty(prop));
      }
    } catch (FileNotFoundException e)
    {
      System.err.println("Could not find bootstrap preferences file '"
              + file.getAbsolutePath() + "'");
    } catch (IOException e)
    {
      System.err.println(
              "IOException when loading bootstrap preferences file '"
                      + file.getAbsolutePath() + "'");
    }
    return bootstrapProps;
  }

  public static void setSessionProperty(String key, String val)
  {
    if (key != null)
    {
      sessionProperties.put(key, val);
    }
  }

  public static String getSessionProperty(String key)
  {
    return key == null ? null : sessionProperties.get(key);
  }
}
