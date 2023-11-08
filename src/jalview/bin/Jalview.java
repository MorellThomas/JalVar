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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.util.SystemInfo;
import com.threerings.getdown.util.LaunchUtil;

//import edu.stanford.ejalbert.launching.IBrowserLaunching;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import jalview.bin.argparser.Arg;
import jalview.bin.argparser.Arg.Opt;
import jalview.bin.argparser.Arg.Type;
import jalview.bin.argparser.ArgParser;
import jalview.bin.argparser.BootstrapArgs;
import jalview.ext.so.SequenceOntology;
import jalview.gui.AlignFrame;
import jalview.gui.Desktop;
import jalview.gui.PromptUserConfig;
import jalview.gui.QuitHandler;
import jalview.gui.QuitHandler.QResponse;
import jalview.gui.StructureViewerBase;
import jalview.io.AppletFormatAdapter;
import jalview.io.BioJsHTMLOutput;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.FileFormatException;
import jalview.io.FileFormatI;
import jalview.io.FileFormats;
import jalview.io.FileLoader;
import jalview.io.HtmlSvgOutput;
import jalview.io.IdentifyFile;
import jalview.io.NewickFile;
import jalview.io.exceptions.ImageOutputException;
import jalview.io.gff.SequenceOntologyFactory;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ColourSchemeProperty;
import jalview.util.ChannelProperties;
import jalview.util.HttpUtils;
import jalview.util.LaunchUtils;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.ws.jws2.Jws2Discoverer;

/**
 * Main class for Jalview Application <br>
 * <br>
 * start with: java -classpath "$PATH_TO_LIB$/*:$PATH_TO_CLASSES$" \
 * jalview.bin.Jalview
 * 
 * or on Windows: java -classpath "$PATH_TO_LIB$/*;$PATH_TO_CLASSES$" \
 * jalview.bin.Jalview jalview.bin.Jalview
 * 
 * (ensure -classpath arg is quoted to avoid shell expansion of '*' and do not
 * embellish '*' to e.g. '*.jar')
 * 
 * @author $author$
 * @version $Revision$
 */
public class Jalview
{
  static
  {
    Platform.getURLCommandArguments();
    Platform.addJ2SDirectDatabaseCall("https://www.jalview.org");
    Platform.addJ2SDirectDatabaseCall("http://www.jalview.org");
    Platform.addJ2SDirectDatabaseCall("http://www.compbio.dundee.ac.uk");
    Platform.addJ2SDirectDatabaseCall("https://www.compbio.dundee.ac.uk");
  }

  /*
   * singleton instance of this class
   */
  private static Jalview instance;

  private Desktop desktop;

  protected Commands cmds;

  public static AlignFrame currentAlignFrame;

  public ArgParser argparser = null;

  public BootstrapArgs bootstrapArgs = null;

  private boolean QUIET = false;

  public static boolean quiet()
  {
    return Jalview.getInstance() != null && Jalview.getInstance().QUIET;
  }

  static
  {
    if (!Platform.isJS())
    /**
     * Java only
     * 
     * @j2sIgnore
     */
    {
      // grab all the rights we can for the JVM
      Policy.setPolicy(new Policy()
      {
        @Override
        public PermissionCollection getPermissions(CodeSource codesource)
        {
          Permissions perms = new Permissions();
          perms.add(new AllPermission());
          return (perms);
        }

        @Override
        public void refresh()
        {
        }
      });
    }
  }

  /**
   * keep track of feature fetching tasks.
   * 
   * @author JimP
   * 
   */
  class FeatureFetcher
  {
    /*
     * TODO: generalise to track all jalview events to orchestrate batch processing
     * events.
     */

    private int queued = 0;

    private int running = 0;

    public FeatureFetcher()
    {

    }

    public void addFetcher(final AlignFrame af,
            final Vector<String> dasSources)
    {
      final long id = System.currentTimeMillis();
      queued++;
      final FeatureFetcher us = this;
      new Thread(new Runnable()
      {

        @Override
        public void run()
        {
          synchronized (us)
          {
            queued--;
            running++;
          }

          af.setProgressBar(MessageManager
                  .getString("status.das_features_being_retrived"), id);
          af.featureSettings_actionPerformed(null);
          af.setProgressBar(null, id);
          synchronized (us)
          {
            running--;
          }
        }
      }).start();
    }

    public synchronized boolean allFinished()
    {
      return queued == 0 && running == 0;
    }

  }

  public static Jalview getInstance()
  {
    return instance;
  }

  /**
   * main class for Jalview application
   * 
   * @param args
   *          open <em>filename</em>
   */
  public static void main(String[] args)
  {
    // setLogging(); // BH - for event debugging in JavaScript
    instance = new Jalview();
    instance.doMain(args);
  }

  private static void logClass(String name)
  {
    // BH - for event debugging in JavaScript
    ConsoleHandler consoleHandler = new ConsoleHandler();
    consoleHandler.setLevel(Level.ALL);
    Logger logger = Logger.getLogger(name);
    logger.setLevel(Level.ALL);
    logger.addHandler(consoleHandler);
  }

  @SuppressWarnings("unused")
  private static void setLogging()
  {

    /**
     * @j2sIgnore
     * 
     */
    {
      System.out.println("not in js");
    }

    // BH - for event debugging in JavaScript (Java mode only)
    if (!Platform.isJS())
    /**
     * Java only
     * 
     * @j2sIgnore
     */
    {
      Logger.getLogger("").setLevel(Level.ALL);
      logClass("java.awt.EventDispatchThread");
      logClass("java.awt.EventQueue");
      logClass("java.awt.Component");
      logClass("java.awt.focus.Component");
      logClass("java.awt.focus.DefaultKeyboardFocusManager");
    }

  }

  /**
   * @param args
   */
  void doMain(String[] args)
  {
    if (!Platform.isJS())
    {
      System.setSecurityManager(null);
    }

    if (args == null || args.length == 0 || (args.length == 1
            && (args[0] == null || args[0].length() == 0)))
    {
      args = new String[] {};
    }

    // get args needed before proper ArgParser
    bootstrapArgs = BootstrapArgs.getBootstrapArgs(args);

    if (!Platform.isJS())
    {
      // are we being --quiet ?
      if (bootstrapArgs.contains(Arg.QUIET))
      {
        QUIET = true;
        OutputStream devNull = new OutputStream()
        {

          @Override
          public void write(int b)
          {
            // DO NOTHING
          }
        };
        System.setOut(new PrintStream(devNull));
        // redirecting stderr not working
        if (bootstrapArgs.getList(Arg.QUIET).size() > 1)
        {
          System.setErr(new PrintStream(devNull));
        }
      }

      if (bootstrapArgs.contains(Arg.HELP)
              || bootstrapArgs.contains(Arg.VERSION))
      {
        QUIET = true;
      }
    }

    // set individual session preferences
    if (bootstrapArgs.contains(Arg.P))
    {
      for (String kev : bootstrapArgs.getValueList(Arg.P))
      {
        if (kev == null)
        {
          continue;
        }
        int equalsIndex = kev.indexOf(ArgParser.EQUALS);
        if (equalsIndex > -1)
        {
          String key = kev.substring(0, equalsIndex);
          String val = kev.substring(equalsIndex + 1);
          Cache.setSessionProperty(key, val);
        }
      }
    }

    // Move any new getdown-launcher-new.jar into place over old
    // getdown-launcher.jar
    String appdirString = System.getProperty("getdownappdir");
    if (appdirString != null && appdirString.length() > 0)
    {
      final File appdir = new File(appdirString);
      new Thread()
      {

        @Override
        public void run()
        {
          LaunchUtil.upgradeGetdown(
                  new File(appdir, "getdown-launcher-old.jar"),
                  new File(appdir, "getdown-launcher.jar"),
                  new File(appdir, "getdown-launcher-new.jar"));
        }
      }.start();
    }

    if (!quiet() || bootstrapArgs.contains(Arg.VERSION))
    {
      System.out.println(
              "Java version: " + System.getProperty("java.version"));
      System.out.println("Java home: " + System.getProperty("java.home"));
      System.out.println("Java arch: " + System.getProperty("os.arch") + " "
              + System.getProperty("os.name") + " "
              + System.getProperty("os.version"));

      String val = System.getProperty("sys.install4jVersion");
      if (val != null)
      {
        System.out.println("Install4j version: " + val);
      }
      val = System.getProperty("installer_template_version");
      if (val != null)
      {
        System.out.println("Install4j template version: " + val);
      }
      val = System.getProperty("launcher_version");
      if (val != null)
      {
        System.out.println("Launcher version: " + val);
      }
    }

    if (Platform.isLinux() && LaunchUtils.getJavaVersion() < 11)
    {
      System.setProperty("flatlaf.uiScale", "1");
    }

    // get bootstrap properties (mainly for the logger level)
    Properties bootstrapProperties = Cache
            .bootstrapProperties(bootstrapArgs.getValue(Arg.PROPS));

    // report Jalview version
    Cache.loadBuildProperties(
            !quiet() || bootstrapArgs.contains(Arg.VERSION));

    // stop now if only after --version
    if (bootstrapArgs.contains(Arg.VERSION))
    {
      Jalview.exit(null, 0);
    }

    // old ArgsParser
    ArgsParser aparser = new ArgsParser(args);

    // old
    boolean headless = false;
    // new
    boolean headlessArg = false;

    try
    {
      String logLevel = null;
      if (bootstrapArgs.contains(Arg.TRACE))
      {
        logLevel = "TRACE";
      }
      else if (bootstrapArgs.contains(Arg.DEBUG))
      {
        logLevel = "DEBUG";
      }
      if (logLevel == null && !(bootstrapProperties == null))
      {
        logLevel = bootstrapProperties.getProperty(Cache.JALVIEWLOGLEVEL);
      }
      Console.initLogger(logLevel);
    } catch (NoClassDefFoundError error)
    {
      error.printStackTrace();
      String message = "\nEssential logging libraries not found."
              + "\nUse: java -classpath \"$PATH_TO_LIB$/*:$PATH_TO_CLASSES$\" jalview.bin.Jalview";
      Jalview.exit(message, 0);
    }

    // register SIGTERM listener
    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      public void run()
      {
        Console.debug("Running shutdown hook");
        QuitHandler.startForceQuit();
        boolean closeExternal = Cache
                .getDefault("DEFAULT_CLOSE_EXTERNAL_VIEWERS", false)
                || Cache.getDefault("ALWAYS_CLOSE_EXTERNAL_VIEWERS", false);
        StructureViewerBase.setQuitClose(closeExternal);
        if (desktop != null)
        {
          for (JInternalFrame frame : Desktop.desktop.getAllFrames())
          {
            if (frame instanceof StructureViewerBase)
            {
              ((StructureViewerBase) frame).closeViewer(closeExternal);
            }
          }
        }

        if (QuitHandler.gotQuitResponse() == QResponse.CANCEL_QUIT)
        {
          // Got to here by a SIGTERM signal.
          // Note we will not actually cancel the quit from here -- it's too
          // late -- but we can wait for saving files and close external viewers
          // if configured.
          // Close viewers/Leave viewers open
          Console.debug("Checking for saving files");
          QuitHandler.getQuitResponse(false);
        }
        else
        {
          Console.debug("Nothing more to do");
        }
        Console.debug("Exiting, bye!");
        // shutdownHook cannot be cancelled, JVM will now halt
      }
    });

    String usrPropsFile = bootstrapArgs.contains(Arg.PROPS)
            ? bootstrapArgs.getValue(Arg.PROPS)
            : aparser.getValue("props");
    // if usrPropsFile == null, loadProperties will use the Channel
    // preferences.file
    Cache.loadProperties(usrPropsFile);
    if (usrPropsFile != null)
    {
      System.out.println(
              "CMD [-props " + usrPropsFile + "] executed successfully!");
      testoutput(bootstrapArgs, Arg.PROPS,
              "test/jalview/bin/testProps.jvprops", usrPropsFile);
    }

    // --argfile=... -- OVERRIDES ALL NON-BOOTSTRAP ARGS
    if (bootstrapArgs.contains(Arg.ARGFILE))
    {
      argparser = ArgParser.parseArgFiles(
              bootstrapArgs.getValueList(Arg.ARGFILE),
              bootstrapArgs.getBoolean(Arg.INITSUBSTITUTIONS),
              bootstrapArgs);
    }
    else
    {
      argparser = new ArgParser(args,
              bootstrapArgs.getBoolean(Arg.INITSUBSTITUTIONS),
              bootstrapArgs);
    }

    if (!Platform.isJS())
    /**
     * Java only
     * 
     * @j2sIgnore
     */
    {
      if (bootstrapArgs.contains(Arg.HELP))
      {
        List<Map.Entry<Type, String>> helpArgs = bootstrapArgs
                .getList(Arg.HELP);
        System.out.println(Arg.usage(helpArgs.stream().map(e -> e.getKey())
                .collect(Collectors.toList())));
        Jalview.exit(null, 0);
      }
      if (aparser.contains("help") || aparser.contains("h"))
      {
        /*
         * Now using new usage statement.
        showUsage();
        */
        System.out.println(Arg.usage());
        Jalview.exit(null, 0);
      }

      // new CLI
      headlessArg = bootstrapArgs.isHeadless();
      if (headlessArg)
      {
        System.setProperty("java.awt.headless", "true");
      }
      // old CLI
      if (aparser.contains("nodisplay") || aparser.contains("nogui")
              || aparser.contains("headless"))
      {
        System.setProperty("java.awt.headless", "true");
        headless = true;
      }
      // anything else!

      // allow https handshakes to download intermediate certs if necessary
      System.setProperty("com.sun.security.enableAIAcaIssuers", "true");

      String jabawsUrl = bootstrapArgs.getValue(Arg.JABAWS);
      if (jabawsUrl == null)
        jabawsUrl = aparser.getValue("jabaws");
      if (jabawsUrl != null)
      {
        try
        {
          Jws2Discoverer.getDiscoverer().setPreferredUrl(jabawsUrl);
          System.out.println(
                  "CMD [-jabaws " + jabawsUrl + "] executed successfully!");
          testoutput(bootstrapArgs, Arg.JABAWS,
                  "http://www.compbio.dundee.ac.uk/jabaws", jabawsUrl);
        } catch (MalformedURLException e)
        {
          System.err.println(
                  "Invalid jabaws parameter: " + jabawsUrl + " ignored");
        }
      }
    }

    List<String> setprops = new ArrayList<>();
    if (bootstrapArgs.contains(Arg.SETPROP))
    {
      setprops = bootstrapArgs.getValueList(Arg.SETPROP);
    }
    else
    {
      String sp = aparser.getValue("setprop");
      while (sp != null)
      {
        setprops.add(sp);
        sp = aparser.getValue("setprop");
      }
    }
    for (String setprop : setprops)
    {
      int p = setprop.indexOf('=');
      if (p == -1)
      {
        System.err
                .println("Ignoring invalid setprop argument : " + setprop);
      }
      else
      {
        System.out.println("Executing setprop argument: " + setprop);
        if (Platform.isJS())
        {
          Cache.setProperty(setprop.substring(0, p),
                  setprop.substring(p + 1));
        }
        // DISABLED FOR SECURITY REASONS
        // TODO: add a property to allow properties to be overriden by cli args
        // Cache.setProperty(setprop.substring(0,p), setprop.substring(p+1));
      }
    }
    if (System.getProperty("java.awt.headless") != null
            && System.getProperty("java.awt.headless").equals("true"))
    {
      headless = true;
    }
    System.setProperty("http.agent", HttpUtils.getUserAgent());

    try
    {
      Console.initLogger();
    } catch (

    NoClassDefFoundError error)
    {
      error.printStackTrace();
      String message = "\nEssential logging libraries not found."
              + "\nUse: java -classpath \"$PATH_TO_LIB$/*:$PATH_TO_CLASSES$\" jalview.bin.Jalview";
      Jalview.exit(message, 0);
    }
    desktop = null;

    if (!(headless || headlessArg))
      setLookAndFeel();

    /*
     * configure 'full' SO model if preferences say to, else use the default (full SO)
     * - as JS currently doesn't have OBO parsing, it must use 'Lite' version
     */
    boolean soDefault = !Platform.isJS();
    if (Cache.getDefault("USE_FULL_SO", soDefault))
    {
      SequenceOntologyFactory.setInstance(new SequenceOntology());
    }

    if (!(headless || headlessArg))
    {
      Desktop.nosplash = "false".equals(bootstrapArgs.getValue(Arg.SPLASH))
              || aparser.contains("nosplash")
              || Cache.getDefault("SPLASH", "true").equals("false");
      desktop = new Desktop();
      desktop.setInBatchMode(true); // indicate we are starting up

      try
      {
        JalviewTaskbar.setTaskbar(this);
      } catch (Exception e)
      {
        Console.info("Cannot set Taskbar");
        Console.error(e.getMessage());
        // e.printStackTrace();
      } catch (Throwable t)
      {
        Console.info("Cannot set Taskbar");
        Console.error(t.getMessage());
        // t.printStackTrace();
      }

      // set Proxy settings before all the internet calls
      Cache.setProxyPropertiesFromPreferences();

      desktop.setVisible(true);

      if (!Platform.isJS())
      /**
       * Java only
       * 
       * @j2sIgnore
       */
      {

        /**
         * Check to see that the JVM version being run is suitable for the Java
         * version this Jalview was compiled for. Popup a warning if not.
         */
        if (!LaunchUtils.checkJavaVersion())
        {
          Console.warn("The Java version being used (Java "
                  + LaunchUtils.getJavaVersion()
                  + ") may lead to problems. This installation of Jalview should be used with Java "
                  + LaunchUtils.getJavaCompileVersion() + ".");

          if (!LaunchUtils
                  .getBooleanUserPreference("IGNORE_JVM_WARNING_POPUP"))
          {
            Object[] options = {
                MessageManager.getString("label.continue") };
            JOptionPane.showOptionDialog(null,
                    MessageManager.formatMessage(
                            "warning.wrong_jvm_version_message",
                            LaunchUtils.getJavaVersion(),
                            LaunchUtils.getJavaCompileVersion()),
                    MessageManager
                            .getString("warning.wrong_jvm_version_title"),
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
          }
        }

        boolean webservicediscovery = bootstrapArgs
                .getBoolean(Arg.WEBSERVICEDISCOVERY);
        if (aparser.contains("nowebservicediscovery"))
          webservicediscovery = false;
        if (webservicediscovery)
        {
          desktop.startServiceDiscovery();
        }
        else
        {
          testoutput(argparser, Arg.WEBSERVICEDISCOVERY);
        }

        boolean usagestats = !bootstrapArgs.getBoolean(Arg.NOUSAGESTATS);
        if (aparser.contains("nousagestats"))
          usagestats = false;
        if (usagestats)
        {
          startUsageStats(desktop);
          testoutput(argparser, Arg.NOUSAGESTATS);
        }
        else
        {
          System.out.println("CMD [-nousagestats] executed successfully!");
          testoutput(argparser, Arg.NOUSAGESTATS);
        }

        boolean questionnaire = bootstrapArgs.getBoolean(Arg.QUESTIONNAIRE);
        if (aparser.contains("noquestionnaire"))
          questionnaire = false;
        if (questionnaire)
        {
          String url = aparser.getValue("questionnaire");
          if (url != null)
          {
            // Start the desktop questionnaire prompter with the specified
            // questionnaire
            Console.debug("Starting questionnaire url at " + url);
            desktop.checkForQuestionnaire(url);
            System.out.println("CMD questionnaire[-" + url
                    + "] executed successfully!");
          }
          else
          {
            if (Cache.getProperty("NOQUESTIONNAIRES") == null)
            {
              // Start the desktop questionnaire prompter with the specified
              // questionnaire
              // String defurl =
              // "http://anaplog.compbio.dundee.ac.uk/cgi-bin/questionnaire.pl";
              // //
              String defurl = "https://www.jalview.org/cgi-bin/questionnaire.pl";
              Console.debug(
                      "Starting questionnaire with default url: " + defurl);
              desktop.checkForQuestionnaire(defurl);
            }
          }
        }
        else
        {
          System.out
                  .println("CMD [-noquestionnaire] executed successfully!");
          testoutput(argparser, Arg.QUESTIONNAIRE);
        }

        if ((!aparser.contains("nonews")
                && Cache.getProperty("NONEWS") == null
                && !"false".equals(bootstrapArgs.getValue(Arg.NEWS)))
                || "true".equals(bootstrapArgs.getValue(Arg.NEWS)))
        {
          desktop.checkForNews();
        }

        if (!aparser.contains("nohtmltemplates")
                && Cache.getProperty("NOHTMLTEMPLATES") == null)
        {
          BioJsHTMLOutput.updateBioJS();
        }
      }
    }
    // Run Commands from cli
    cmds = new Commands(argparser, headlessArg);
    boolean commandsSuccess = cmds.argsWereParsed();

    if (commandsSuccess)
    {
      if (headlessArg)
      {
        if (argparser.getBoolean(Arg.NOQUIT))
        {
          Console.warn(
                  "Completed " + Arg.HEADLESS.getName() + " commands, but "
                          + Arg.NOQUIT + " is set so not quitting!");
        }
        else
        {
          Jalview.exit("Successfully completed commands in headless mode",
                  0);
        }
      }
      Console.info("Successfully completed commands");
    }
    else
    {
      if (headlessArg)
      {
        Jalview.exit("Error when running Commands in headless mode", 1);
      }
      Console.warn("Error when running commands");
    }

    // Check if JVM and compile version might cause problems and log if it
    // might.
    if (headless && !Platform.isJS() && !LaunchUtils.checkJavaVersion())
    {
      Console.warn("The Java version being used (Java "
              + LaunchUtils.getJavaVersion()
              + ") may lead to problems. This installation of Jalview should be used with Java "
              + LaunchUtils.getJavaCompileVersion() + ".");
    }

    String file = null, data = null;

    FileFormatI format = null;

    DataSourceType protocol = null;

    FileLoader fileLoader = new FileLoader(!headless);

    String groovyscript = null; // script to execute after all loading is
    // completed one way or another
    // extract groovy argument and execute if necessary
    groovyscript = aparser.getValue("groovy", true);
    file = aparser.getValue("open", true);

    if (file == null && desktop == null && !commandsSuccess)
    {
      Jalview.exit("No files to open!", 1);
    }

    long progress = -1;
    // Finally, deal with the remaining input data.
    if (file != null)
    {
      if (!headless)
      {
        desktop.setProgressBar(
                MessageManager
                        .getString("status.processing_commandline_args"),
                progress = System.currentTimeMillis());
      }
      System.out.println("CMD [-open " + file + "] executed successfully!");

      if (!Platform.isJS())
      /**
       * ignore in JavaScript -- can't just file existence - could load it?
       * 
       * @j2sIgnore
       */
      {
        if (!HttpUtils.startsWithHttpOrHttps(file))
        {
          if (!(new File(file)).exists())
          {
            if (headless)
            {
              Jalview.exit(
                      "Can't find file '" + file + "' in headless mode", 1);
            }
            Console.warn("Can't find file'" + file + "'");
          }
        }
      }

      protocol = AppletFormatAdapter.checkProtocol(file);

      try
      {
        format = new IdentifyFile().identify(file, protocol);
      } catch (FileFormatException e1)
      {
        // TODO ?
      }

      AlignFrame af = fileLoader.LoadFileWaitTillLoaded(file, protocol,
              format);
      if (af == null)
      {
        System.out.println("error");
      }
      else
      {
        setCurrentAlignFrame(af);
        data = aparser.getValue("colour", true);
        if (data != null)
        {
          data.replaceAll("%20", " ");

          ColourSchemeI cs = ColourSchemeProperty.getColourScheme(
                  af.getViewport(), af.getViewport().getAlignment(), data);

          if (cs != null)
          {
            System.out.println(
                    "CMD [-colour " + data + "] executed successfully!");
          }
          af.changeColour(cs);
        }

        // Must maintain ability to use the groups flag
        data = aparser.getValue("groups", true);
        if (data != null)
        {
          af.parseFeaturesFile(data,
                  AppletFormatAdapter.checkProtocol(data));
          // System.out.println("Added " + data);
          System.out.println(
                  "CMD groups[-" + data + "]  executed successfully!");
        }
        data = aparser.getValue("features", true);
        if (data != null)
        {
          af.parseFeaturesFile(data,
                  AppletFormatAdapter.checkProtocol(data));
          // System.out.println("Added " + data);
          System.out.println(
                  "CMD [-features " + data + "]  executed successfully!");
        }

        data = aparser.getValue("annotations", true);
        if (data != null)
        {
          af.loadJalviewDataFile(data, null, null, null);
          // System.out.println("Added " + data);
          System.out.println(
                  "CMD [-annotations " + data + "] executed successfully!");
        }
        // set or clear the sortbytree flag.
        if (aparser.contains("sortbytree"))
        {
          af.getViewport().setSortByTree(true);
          if (af.getViewport().getSortByTree())
          {
            System.out.println("CMD [-sortbytree] executed successfully!");
          }
        }
        if (aparser.contains("no-annotation"))
        {
          af.getViewport().setShowAnnotation(false);
          if (!af.getViewport().isShowAnnotation())
          {
            System.out.println("CMD no-annotation executed successfully!");
          }
        }
        if (aparser.contains("nosortbytree"))
        {
          af.getViewport().setSortByTree(false);
          if (!af.getViewport().getSortByTree())
          {
            System.out
                    .println("CMD [-nosortbytree] executed successfully!");
          }
        }
        data = aparser.getValue("tree", true);
        if (data != null)
        {
          try
          {
            System.out.println(
                    "CMD [-tree " + data + "] executed successfully!");
            NewickFile nf = new NewickFile(data,
                    AppletFormatAdapter.checkProtocol(data));
            af.getViewport()
                    .setCurrentTree(af.showNewickTree(nf, data).getTree());
          } catch (IOException ex)
          {
            System.err.println("Couldn't add tree " + data);
            ex.printStackTrace(System.err);
          }
        }

        if (groovyscript != null)
        {
          // Execute the groovy script after we've done all the rendering stuff
          // and before any images or figures are generated.
          System.out.println("Executing script " + groovyscript);
          executeGroovyScript(groovyscript, af);
          System.out.println("CMD groovy[" + groovyscript
                  + "] executed successfully!");
          groovyscript = null;
        }
        String imageName = "unnamed.png";
        while (aparser.getSize() > 1)
        {
          try
          {
            String outputFormat = aparser.nextValue();
            file = aparser.nextValue();

            if (outputFormat.equalsIgnoreCase("png"))
            {
              System.out.println("Creating PNG image: " + file);
              af.createPNG(new File(file));
              imageName = (new File(file)).getName();
              continue;
            }
            else if (outputFormat.equalsIgnoreCase("svg"))
            {
              System.out.println("Creating SVG image: " + file);
              File imageFile = new File(file);
              imageName = imageFile.getName();
              af.createSVG(imageFile);
              continue;
            }
            else if (outputFormat.equalsIgnoreCase("html"))
            {
              File imageFile = new File(file);
              imageName = imageFile.getName();
              HtmlSvgOutput htmlSVG = new HtmlSvgOutput(af.alignPanel);

              System.out.println("Creating HTML image: " + file);
              htmlSVG.exportHTML(file);
              continue;
            }
            else if (outputFormat.equalsIgnoreCase("biojsmsa"))
            {
              if (file == null)
              {
                System.err.println("The output html file must not be null");
                return;
              }
              try
              {
                BioJsHTMLOutput.refreshVersionInfo(
                        BioJsHTMLOutput.BJS_TEMPLATES_LOCAL_DIRECTORY);
              } catch (URISyntaxException e)
              {
                e.printStackTrace();
              }
              BioJsHTMLOutput bjs = new BioJsHTMLOutput(af.alignPanel);
              System.out.println(
                      "Creating BioJS MSA Viwer HTML file: " + file);
              bjs.exportHTML(file);
              continue;
            }
            else if (outputFormat.equalsIgnoreCase("imgMap"))
            {
              System.out.println("Creating image map: " + file);
              af.createImageMap(new File(file), imageName);
              continue;
            }
            else if (outputFormat.equalsIgnoreCase("eps"))
            {
              File outputFile = new File(file);
              System.out.println(
                      "Creating EPS file: " + outputFile.getAbsolutePath());
              af.createEPS(outputFile);
              continue;
            }

            FileFormatI outFormat = null;
            try
            {
              outFormat = FileFormats.getInstance().forName(outputFormat);
            } catch (Exception formatP)
            {
              System.out.println("Couldn't parse " + outFormat
                      + " as a valid Jalview format string.");
            }
            if (outFormat != null)
            {
              if (!outFormat.isWritable())
              {
                System.out.println(
                        "This version of Jalview does not support alignment export as "
                                + outputFormat);
              }
              else
              {
                af.saveAlignment(file, outFormat);
                if (af.isSaveAlignmentSuccessful())
                {
                  System.out.println("Written alignment in "
                          + outFormat.getName() + " format to " + file);
                }
                else
                {
                  System.out.println("Error writing file " + file + " in "
                          + outFormat.getName() + " format!!");
                }
              }
            }
          } catch (ImageOutputException ioexc)
          {
            System.out.println(
                    "Unexpected error whilst exporting image to " + file);
            ioexc.printStackTrace();
          }

        }

        while (aparser.getSize() > 0)
        {
          System.out.println("Unknown arg: " + aparser.nextValue());
        }
      }
    }

    AlignFrame startUpAlframe = null;
    // We'll only open the default file if the desktop is visible.
    // And the user
    // ////////////////////

    if (!Platform.isJS() && !headless && file == null
            && Cache.getDefault("SHOW_STARTUP_FILE", true)
            && !cmds.commandArgsProvided()
            && !bootstrapArgs.getBoolean(Arg.NOSTARTUPFILE))
    // don't open the startup file if command line args have been processed
    // (&& !Commands.commandArgsProvided())
    /**
     * Java only
     * 
     * @j2sIgnore
     */
    {
      file = Cache.getDefault("STARTUP_FILE",
              Cache.getDefault("www.jalview.org", "https://www.jalview.org")
                      + "/examples/exampleFile_2_7.jvp");
      if (file.equals("http://www.jalview.org/examples/exampleFile_2_3.jar")
              || file.equals(
                      "http://www.jalview.org/examples/exampleFile_2_7.jar"))
      {
        file.replace("http:", "https:");
        // hardwire upgrade of the startup file
        file.replace("_2_3", "_2_7");
        file.replace("2_7.jar", "2_7.jvp");
        // and remove the stale setting
        Cache.removeProperty("STARTUP_FILE");
      }

      protocol = AppletFormatAdapter.checkProtocol(file);

      if (file.endsWith(".jar"))
      {
        format = FileFormat.Jalview;
      }
      else
      {
        try
        {
          format = new IdentifyFile().identify(file, protocol);
        } catch (FileFormatException e)
        {
          // TODO what?
        }
      }

      startUpAlframe = fileLoader.LoadFileWaitTillLoaded(file, protocol,
              format);
      // don't ask to save when quitting if only the startup file has been
      // opened
      Console.debug("Resetting up-to-date flag for startup file");
      startUpAlframe.getViewport().setSavedUpToDate(true);
      // extract groovy arguments before anything else.
    }

    // Once all other stuff is done, execute any groovy scripts (in order)
    if (groovyscript != null)
    {
      if (Cache.groovyJarsPresent())
      {
        System.out.println("Executing script " + groovyscript);
        executeGroovyScript(groovyscript, startUpAlframe);
      }
      else
      {
        System.err.println(
                "Sorry. Groovy Support is not available, so ignoring the provided groovy script "
                        + groovyscript);
      }
    }
    // and finally, turn off batch mode indicator - if the desktop still exists
    if (desktop != null)
    {
      if (progress != -1)
      {
        desktop.setProgressBar(null, progress);
      }
      desktop.setInBatchMode(false);
    }
  }

  private static void setLookAndFeel()
  {
    if (!Platform.isJS())
    /**
     * Java only
     * 
     * @j2sIgnore
     */
    {
      // property laf = "crossplatform", "system", "gtk", "metal", "nimbus",
      // "mac" or "flat"
      // If not set (or chosen laf fails), use the normal SystemLaF and if on
      // Mac,
      // try Quaqua/Vaqua.
      String lafProp = System.getProperty("laf");
      String lafSetting = Cache.getDefault("PREFERRED_LAF", null);
      String laf = "none";
      if (lafProp != null)
      {
        laf = lafProp;
      }
      else if (lafSetting != null)
      {
        laf = lafSetting;
      }
      boolean lafSet = false;
      switch (laf)
      {
      case "crossplatform":
        lafSet = setCrossPlatformLookAndFeel();
        if (!lafSet)
        {
          Console.error("Could not set requested laf=" + laf);
        }
        break;
      case "system":
        lafSet = setSystemLookAndFeel();
        if (!lafSet)
        {
          Console.error("Could not set requested laf=" + laf);
        }
        break;
      case "gtk":
        lafSet = setGtkLookAndFeel();
        if (!lafSet)
        {
          Console.error("Could not set requested laf=" + laf);
        }
        break;
      case "metal":
        lafSet = setMetalLookAndFeel();
        if (!lafSet)
        {
          Console.error("Could not set requested laf=" + laf);
        }
        break;
      case "nimbus":
        lafSet = setNimbusLookAndFeel();
        if (!lafSet)
        {
          Console.error("Could not set requested laf=" + laf);
        }
        break;
      case "flat":
        lafSet = setFlatLookAndFeel();
        if (!lafSet)
        {
          Console.error("Could not set requested laf=" + laf);
        }
        break;
      case "mac":
        lafSet = setMacLookAndFeel();
        if (!lafSet)
        {
          Console.error("Could not set requested laf=" + laf);
        }
        break;
      case "none":
        break;
      default:
        Console.error("Requested laf=" + laf + " not implemented");
      }
      if (!lafSet)
      {
        // Flatlaf default for everyone!
        lafSet = setFlatLookAndFeel();
        if (!lafSet)
        {
          setSystemLookAndFeel();
        }
        if (Platform.isLinux())
        {
          setLinuxLookAndFeel();
        }
        if (Platform.isMac())
        {
          setMacLookAndFeel();
        }
      }
    }
  }

  private static boolean setCrossPlatformLookAndFeel()
  {
    boolean set = false;
    try
    {
      UIManager.setLookAndFeel(
              UIManager.getCrossPlatformLookAndFeelClassName());
      set = true;
    } catch (Exception ex)
    {
      Console.error("Unexpected Look and Feel Exception");
      Console.error(ex.getMessage());
      Console.debug(Cache.getStackTraceString(ex));
    }
    return set;
  }

  private static boolean setSystemLookAndFeel()
  {
    boolean set = false;
    try
    {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      set = true;
    } catch (Exception ex)
    {
      Console.error("Unexpected Look and Feel Exception");
      Console.error(ex.getMessage());
      Console.debug(Cache.getStackTraceString(ex));
    }
    return set;
  }

  private static boolean setSpecificLookAndFeel(String name,
          String className, boolean nameStartsWith)
  {
    boolean set = false;
    try
    {
      for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
      {
        if (info.getName() != null && nameStartsWith
                ? info.getName().toLowerCase(Locale.ROOT)
                        .startsWith(name.toLowerCase(Locale.ROOT))
                : info.getName().toLowerCase(Locale.ROOT)
                        .equals(name.toLowerCase(Locale.ROOT)))
        {
          className = info.getClassName();
          break;
        }
      }
      UIManager.setLookAndFeel(className);
      set = true;
    } catch (Exception ex)
    {
      Console.error("Unexpected Look and Feel Exception");
      Console.error(ex.getMessage());
      Console.debug(Cache.getStackTraceString(ex));
    }
    return set;
  }

  private static boolean setGtkLookAndFeel()
  {
    return setSpecificLookAndFeel("gtk",
            "com.sun.java.swing.plaf.gtk.GTKLookAndFeel", true);
  }

  private static boolean setMetalLookAndFeel()
  {
    return setSpecificLookAndFeel("metal",
            "javax.swing.plaf.metal.MetalLookAndFeel", false);
  }

  private static boolean setNimbusLookAndFeel()
  {
    return setSpecificLookAndFeel("nimbus",
            "javax.swing.plaf.nimbus.NimbusLookAndFeel", false);
  }

  private static boolean setFlatLookAndFeel()
  {
    boolean set = false;
    if (SystemInfo.isMacOS)
    {
      try
      {
        UIManager.setLookAndFeel(
                "com.formdev.flatlaf.themes.FlatMacLightLaf");
        set = true;
        Console.debug("Using FlatMacLightLaf");
      } catch (ClassNotFoundException | InstantiationException
              | IllegalAccessException | UnsupportedLookAndFeelException e)
      {
        Console.debug("Exception loading FlatLightLaf", e);
      }
      System.setProperty("apple.laf.useScreenMenuBar", "true");
      System.setProperty("apple.awt.application.name",
              ChannelProperties.getProperty("app_name"));
      System.setProperty("apple.awt.application.appearance", "system");
      if (SystemInfo.isMacFullWindowContentSupported
              && Desktop.desktop != null)
      {
        Console.debug("Setting transparent title bar");
        Desktop.desktop.getRootPane()
                .putClientProperty("apple.awt.fullWindowContent", true);
        Desktop.desktop.getRootPane()
                .putClientProperty("apple.awt.transparentTitleBar", true);
        Desktop.desktop.getRootPane()
                .putClientProperty("apple.awt.fullscreenable", true);
      }
      SwingUtilities.invokeLater(() -> {
        FlatMacLightLaf.setup();
      });
      Console.debug("Using FlatMacLightLaf");
      set = true;
    }
    if (!set)
    {
      try
      {
        UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        set = true;
        Console.debug("Using FlatLightLaf");
      } catch (ClassNotFoundException | InstantiationException
              | IllegalAccessException | UnsupportedLookAndFeelException e)
      {
        Console.debug("Exception loading FlatLightLaf", e);
      }
      // Windows specific properties here
      SwingUtilities.invokeLater(() -> {
        FlatLightLaf.setup();
      });
      Console.debug("Using FlatLightLaf");
      set = true;
    }
    else if (SystemInfo.isLinux)
    {
      try
      {
        UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        set = true;
        Console.debug("Using FlatLightLaf");
      } catch (ClassNotFoundException | InstantiationException
              | IllegalAccessException | UnsupportedLookAndFeelException e)
      {
        Console.debug("Exception loading FlatLightLaf", e);
      }
      // enable custom window decorations
      JFrame.setDefaultLookAndFeelDecorated(true);
      JDialog.setDefaultLookAndFeelDecorated(true);
      SwingUtilities.invokeLater(() -> {
        FlatLightLaf.setup();
      });
      Console.debug("Using FlatLightLaf");
      set = true;
    }

    if (!set)
    {
      try
      {
        UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        set = true;
        Console.debug("Using FlatLightLaf");
      } catch (ClassNotFoundException | InstantiationException
              | IllegalAccessException | UnsupportedLookAndFeelException e)
      {
        Console.debug("Exception loading FlatLightLaf", e);
      }
    }

    if (set)
    {
      UIManager.put("TabbedPane.tabType", "card");
      UIManager.put("TabbedPane.showTabSeparators", true);
      UIManager.put("TabbedPane.showContentSeparator", true);
      UIManager.put("TabbedPane.tabSeparatorsFullHeight", true);
      UIManager.put("TabbedPane.tabsOverlapBorder", true);
      UIManager.put("TabbedPane.hasFullBorder", true);
      UIManager.put("TabbedPane.tabLayoutPolicy", "scroll");
      UIManager.put("TabbedPane.scrollButtonsPolicy", "asNeeded");
      UIManager.put("TabbedPane.smoothScrolling", true);
      UIManager.put("TabbedPane.tabWidthMode", "compact");
      UIManager.put("TabbedPane.selectedBackground", Color.white);
      UIManager.put("TabbedPane.background", new Color(236, 236, 236));
      UIManager.put("TabbedPane.hoverColor", Color.lightGray);
    }

    Desktop.setLiveDragMode(Cache.getDefault("FLAT_LIVE_DRAG_MODE", true));
    return set;
  }

  private static boolean setMacLookAndFeel()
  {
    boolean set = false;
    System.setProperty("com.apple.mrj.application.apple.menu.about.name",
            ChannelProperties.getProperty("app_name"));
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    /*
     * broken native LAFs on (ARM?) macbooks
    set = setQuaquaLookAndFeel();
    if ((!set) || !UIManager.getLookAndFeel().getClass().toString()
            .toLowerCase(Locale.ROOT).contains("quaqua"))
    {
      set = setVaquaLookAndFeel();
    }
     */
    set = setFlatLookAndFeel();
    return set;
  }

  private static boolean setLinuxLookAndFeel()
  {
    boolean set = false;
    set = setFlatLookAndFeel();
    if (!set)
      set = setMetalLookAndFeel();
    // avoid GtkLookAndFeel -- not good results especially on HiDPI
    if (!set)
      set = setNimbusLookAndFeel();
    return set;
  }

  /*
  private static void showUsage()
  {
    System.out.println(
            "Usage: jalview -open [FILE] [OUTPUT_FORMAT] [OUTPUT_FILE]\n\n"
                    + "-nodisplay\tRun Jalview without User Interface.\n"
                    + "-props FILE\tUse the given Jalview properties file instead of users default.\n"
                    + "-colour COLOURSCHEME\tThe colourscheme to be applied to the alignment\n"
                    + "-annotations FILE\tAdd precalculated annotations to the alignment.\n"
                    + "-tree FILE\tLoad the given newick format tree file onto the alignment\n"
                    + "-features FILE\tUse the given file to mark features on the alignment.\n"
                    + "-fasta FILE\tCreate alignment file FILE in Fasta format.\n"
                    + "-clustal FILE\tCreate alignment file FILE in Clustal format.\n"
                    + "-pfam FILE\tCreate alignment file FILE in PFAM format.\n"
                    + "-msf FILE\tCreate alignment file FILE in MSF format.\n"
                    + "-pileup FILE\tCreate alignment file FILE in Pileup format\n"
                    + "-pir FILE\tCreate alignment file FILE in PIR format.\n"
                    + "-blc FILE\tCreate alignment file FILE in BLC format.\n"
                    + "-json FILE\tCreate alignment file FILE in JSON format.\n"
                    + "-jalview FILE\tCreate alignment file FILE in Jalview format.\n"
                    + "-png FILE\tCreate PNG image FILE from alignment.\n"
                    + "-svg FILE\tCreate SVG image FILE from alignment.\n"
                    + "-html FILE\tCreate HTML file from alignment.\n"
                    + "-biojsMSA FILE\tCreate BioJS MSA Viewer HTML file from alignment.\n"
                    + "-imgMap FILE\tCreate HTML file FILE with image map of PNG image.\n"
                    + "-eps FILE\tCreate EPS file FILE from alignment.\n"
                    + "-questionnaire URL\tQueries the given URL for information about any Jalview user questionnaires.\n"
                    + "-noquestionnaire\tTurn off questionnaire check.\n"
                    + "-nonews\tTurn off check for Jalview news.\n"
                    + "-nousagestats\tTurn off analytics tracking for this session.\n"
                    + "-sortbytree OR -nosortbytree\tEnable or disable sorting of the given alignment by the given tree\n"
                    // +
                    // "-setprop PROPERTY=VALUE\tSet the given Jalview property,
                    // after all other properties files have been read\n\t
                    // (quote the 'PROPERTY=VALUE' pair to ensure spaces are
                    // passed in correctly)"
                    + "-jabaws URL\tSpecify URL for Jabaws services (e.g. for a local installation).\n"
                    + "-fetchfrom nickname\tQuery nickname for features for the alignments and display them.\n"
                    + "-groovy FILE\tExecute groovy script in FILE, after all other arguments have been processed (if FILE is the text 'STDIN' then the file will be read from STDIN)\n"
                    + "-jvmmempc=PERCENT\tOnly available with standalone executable jar or jalview.bin.Launcher. Limit maximum heap size (memory) to PERCENT% of total physical memory detected. This defaults to 90 if total physical memory can be detected. See https://www.jalview.org/help/html/memory.html for more details.\n"
                    + "-jvmmemmax=MAXMEMORY\tOnly available with standalone executable jar or jalview.bin.Launcher. Limit maximum heap size (memory) to MAXMEMORY. MAXMEMORY can be specified in bytes, kilobytes(k), megabytes(m), gigabytes(g) or if you're lucky enough, terabytes(t). This defaults to 32g if total physical memory can be detected, or to 8g if total physical memory cannot be detected. See https://www.jalview.org/help/html/memory.html for more details.\n"
                    + "\n~Read documentation in Application or visit https://www.jalview.org for description of Features and Annotations file~\n\n");
  }
  */

  private static void startUsageStats(final Desktop desktop)
  {
    /**
     * start a User Config prompt asking if we can log usage statistics.
     */
    PromptUserConfig prompter = new PromptUserConfig(Desktop.desktop,
            "USAGESTATS",
            MessageManager.getString("prompt.plausible_analytics_title"),
            MessageManager.getString("prompt.plausible_analytics"),
            new Runnable()
            {
              @Override
              public void run()
              {
                Console.debug("Initialising analytics for usage stats.");
                Cache.initAnalytics();
                Console.debug("Tracking enabled.");
              }
            }, new Runnable()
            {
              @Override
              public void run()
              {
                Console.debug("Not enabling analytics.");
              }
            }, null, true);
    desktop.addDialogThread(prompter);
  }

  /**
   * Locate the given string as a file and pass it to the groovy interpreter.
   * 
   * @param groovyscript
   *          the script to execute
   * @param jalviewContext
   *          the Jalview Desktop object passed in to the groovy binding as the
   *          'Jalview' object.
   */
  protected void executeGroovyScript(String groovyscript, AlignFrame af)
  {
    /**
     * for scripts contained in files
     */
    File tfile = null;
    /**
     * script's URI
     */
    URL sfile = null;
    if (groovyscript.trim().equals("STDIN"))
    {
      // read from stdin into a tempfile and execute it
      try
      {
        tfile = File.createTempFile("jalview", "groovy");
        PrintWriter outfile = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(tfile)));
        BufferedReader br = new BufferedReader(
                new InputStreamReader(System.in));
        String line = null;
        while ((line = br.readLine()) != null)
        {
          outfile.write(line + "\n");
        }
        br.close();
        outfile.flush();
        outfile.close();

      } catch (Exception ex)
      {
        System.err.println("Failed to read from STDIN into tempfile "
                + ((tfile == null) ? "(tempfile wasn't created)"
                        : tfile.toString()));
        ex.printStackTrace();
        return;
      }
      try
      {
        sfile = tfile.toURI().toURL();
      } catch (Exception x)
      {
        System.err.println(
                "Unexpected Malformed URL Exception for temporary file created from STDIN: "
                        + tfile.toURI());
        x.printStackTrace();
        return;
      }
    }
    else
    {
      try
      {
        sfile = new URI(groovyscript).toURL();
      } catch (Exception x)
      {
        tfile = new File(groovyscript);
        if (!tfile.exists())
        {
          System.err.println("File '" + groovyscript + "' does not exist.");
          return;
        }
        if (!tfile.canRead())
        {
          System.err.println("File '" + groovyscript + "' cannot be read.");
          return;
        }
        if (tfile.length() < 1)
        {
          System.err.println("File '" + groovyscript + "' is empty.");
          return;
        }
        try
        {
          sfile = tfile.getAbsoluteFile().toURI().toURL();
        } catch (Exception ex)
        {
          System.err.println("Failed to create a file URL for "
                  + tfile.getAbsoluteFile());
          return;
        }
      }
    }
    try
    {
      Map<String, java.lang.Object> vbinding = new HashMap<>();
      vbinding.put("Jalview", this);
      if (af != null)
      {
        vbinding.put("currentAlFrame", af);
      }
      Binding gbinding = new Binding(vbinding);
      GroovyScriptEngine gse = new GroovyScriptEngine(new URL[] { sfile });
      gse.run(sfile.toString(), gbinding);
      if ("STDIN".equals(groovyscript))
      {
        // delete temp file that we made -
        // only if it was successfully executed
        tfile.delete();
      }
    } catch (Exception e)
    {
      System.err.println("Exception Whilst trying to execute file " + sfile
              + " as a groovy script.");
      e.printStackTrace(System.err);

    }
  }

  public static boolean isHeadlessMode()
  {
    String isheadless = System.getProperty("java.awt.headless");
    if (isheadless != null && isheadless.equalsIgnoreCase("true"))
    {
      return true;
    }
    return false;
  }

  public AlignFrame[] getAlignFrames()
  {
    return desktop == null ? new AlignFrame[] { getCurrentAlignFrame() }
            : Desktop.getAlignFrames();

  }

  /**
   * jalview.bin.Jalview.quit() will just run the non-GUI shutdownHook and exit
   */
  public void quit()
  {
    // System.exit will run the shutdownHook first
    Jalview.exit("Quitting now. Bye!", 0);
  }

  public static AlignFrame getCurrentAlignFrame()
  {
    return Jalview.currentAlignFrame;
  }

  public static void setCurrentAlignFrame(AlignFrame currentAlignFrame)
  {
    Jalview.currentAlignFrame = currentAlignFrame;
  }

  protected Commands getCommands()
  {
    return cmds;
  }

  public static void exit(String message, int exitcode)
  {
    if (Console.log == null)
    {
      // Don't start the logger just to exit!
      if (message != null)
      {
        if (exitcode == 0)
        {
          System.out.println(message);
        }
        else
        {
          System.err.println(message);
        }
      }
    }
    else
    {
      Console.debug("Using Jalview.exit");
      if (message != null)
      {
        if (exitcode == 0)
        {
          Console.info(message);
        }
        else
        {
          Console.error(message);
        }
      }
    }
    if (exitcode > -1)
    {
      System.exit(exitcode);
    }
  }

  /******************************
   * 
   * TEST OUTPUT METHODS
   * 
   ******************************/
  /**
   * method for reporting string values parsed/processed during tests
   * 
   */
  protected static void testoutput(ArgParser ap, Arg a, String s1,
          String s2)
  {
    BootstrapArgs bsa = ap.getBootstrapArgs();
    if (!bsa.getBoolean(Arg.TESTOUTPUT))
      return;
    if (!((s1 == null && s2 == null) || (s1 != null && s1.equals(s2))))
    {
      Console.debug("testoutput with unmatching values '" + s1 + "' and '"
              + s2 + "' for arg " + a.argString());
      return;
    }
    boolean isset = a.hasOption(Opt.BOOTSTRAP) ? bsa.contains(a)
            : ap.isSet(a);
    if (!isset)
    {
      Console.warn("Arg '" + a.getName() + "' not set at all");
      return;
    }
    testoutput(true, a, s1, s2);
  }

  /**
   * method for reporting string values parsed/processed during tests
   */

  protected static void testoutput(BootstrapArgs bsa, Arg a, String s1,
          String s2)
  {
    if (!bsa.getBoolean(Arg.TESTOUTPUT))
      return;
    if (!((s1 == null && s2 == null) || (s1 != null && s1.equals(s2))))
    {
      Console.debug("testoutput with unmatching values '" + s1 + "' and '"
              + s2 + "' for arg " + a.argString());
      return;
    }
    if (!a.hasOption(Opt.BOOTSTRAP))
    {
      Console.error("Non-bootstrap Arg '" + a.getName()
              + "' given to testoutput(BootstrapArgs bsa, Arg a, String s1, String s2) with only BootstrapArgs");
    }
    if (!bsa.contains(a))
    {
      Console.warn("Arg '" + a.getName() + "' not set at all");
      return;
    }
    testoutput(true, a, s1, s2);
  }

  /**
   * report value set for string values parsed/processed during tests
   */
  private static void testoutput(boolean yes, Arg a, String s1, String s2)
  {
    if (yes && ((s1 == null && s2 == null)
            || (s1 != null && s1.equals(s2))))
    {
      System.out.println("[TESTOUTPUT] arg " + a.argString() + "='" + s1
              + "' was set");
    }
  }

  /*
   * testoutput for boolean and unary values
   */
  protected static void testoutput(ArgParser ap, Arg a)
  {
    if (ap == null)
      return;
    BootstrapArgs bsa = ap.getBootstrapArgs();
    if (bsa == null)
      return;
    if (!bsa.getBoolean(Arg.TESTOUTPUT))
      return;
    boolean val = a.hasOption(Opt.BOOTSTRAP) ? bsa.getBoolean(a)
            : ap.getBoolean(a);
    boolean isset = a.hasOption(Opt.BOOTSTRAP) ? bsa.contains(a)
            : ap.isSet(a);
    if (!isset)
    {
      Console.warn("Arg '" + a.getName() + "' not set at all");
      return;
    }
    testoutput(val, a);
  }

  protected static void testoutput(BootstrapArgs bsa, Arg a)
  {
    if (!bsa.getBoolean(Arg.TESTOUTPUT))
      return;
    if (!a.hasOption(Opt.BOOTSTRAP))
    {
      Console.warn("Non-bootstrap Arg '" + a.getName()
              + "' given to testoutput(BootstrapArgs bsa, Arg a) with only BootstrapArgs");

    }
    if (!bsa.contains(a))
    {
      Console.warn("Arg '" + a.getName() + "' not set at all");
      return;
    }
    testoutput(bsa.getBoolean(a), a);
  }

  private static void testoutput(boolean yes, Arg a)
  {
    String message = null;
    if (a.hasOption(Opt.BOOLEAN))
    {
      message = (yes ? a.argString() : a.negateArgString()) + " was set";
    }
    else if (a.hasOption(Opt.UNARY))
    {
      message = a.argString() + (yes ? " was set" : " was not set");
    }
    System.out.println("[TESTOUTPUT] arg " + message);
  }
}
