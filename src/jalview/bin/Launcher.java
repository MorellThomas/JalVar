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

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import jalview.util.ChannelProperties;
import jalview.util.LaunchUtils;

/**
 * A Launcher class for Jalview. This class is used to launch Jalview from the
 * shadowJar when Getdown is not used or available. It attempts to take all the
 * command line arguments to pass on to the jalview.bin.Jalview class, but to
 * insert a -Xmx memory setting to a sensible default, using the -jvmmempc and
 * -jvmmemmax application arguments if specified. If not specified then system
 * properties will be looked for by jalview.bin.MemorySetting. If the user has
 * provided the JVM with a -Xmx setting directly and not set -jvmmempc or
 * -jvmmemmax then this setting will be used and system properties ignored. If
 * -Xmx is set as well as -jvmmempc or -jvmmemmax as argument(s) then the -Xmx
 * argument will NOT be passed on to the main application launch.
 * 
 * @author bsoares
 *
 */
public class Launcher
{
  private final static String startClass = "jalview.bin.Jalview";

  private static boolean checkJVMSymlink(String testBin)
  {
    File testBinFile = new File(testBin);
    if (!testBinFile.exists())
    {
      return false;
    }
    File targetFile = null;
    try
    {
      targetFile = testBinFile.getCanonicalFile();
    } catch (IOException e)
    {
      return false;
    }
    if (targetFile != null && ("java".equals(targetFile.getName())
            || "java.exe".equals(targetFile.getName())))
    {
      return true;
    }
    return false;
  }

  /**
   * main method for jalview.bin.Launcher. This restarts the same JRE's JVM with
   * the same arguments but with memory adjusted based on extracted -jvmmempc
   * and -jvmmemmax application arguments. If on a Mac then extra dock:icon and
   * dock:name arguments are also set.
   * 
   * @param args
   */
  public static void main(String[] args)
  {
    if (!LaunchUtils.checkJavaVersion())
    {
      System.err.println("WARNING - The Java version being used (Java "
              + LaunchUtils.getJavaVersion()
              + ") may lead to problems. This installation of Jalview should be used with Java "
              + LaunchUtils.getJavaCompileVersion() + ".");
    }
    final String appName = ChannelProperties.getProperty("app_name");
    final String javaBinDir = System.getProperty("java.home")
            + File.separator + "bin" + File.separator;
    String javaBin = null;
    if (javaBin == null && checkJVMSymlink(javaBinDir + appName))
    {
      javaBin = javaBinDir + appName;
    }
    if (javaBin == null && checkJVMSymlink(javaBinDir + "Jalview"))
    {
      javaBin = javaBinDir + "Jalview";
    }
    if (javaBin == null)
    {
      javaBin = "java";
    }

    List<String> command = new ArrayList<>();
    command.add(javaBin);

    String memSetting = null;

    boolean isAMac = System.getProperty("os.name").indexOf("Mac") > -1;

    for (String jvmArg : ManagementFactory.getRuntimeMXBean()
            .getInputArguments())
    {
      command.add(jvmArg);
    }
    command.add("-cp");
    command.add(ManagementFactory.getRuntimeMXBean().getClassPath());

    String jvmmempc = null;
    String jvmmemmax = null;
    boolean debug = false;
    boolean wait = true;
    boolean quiet = false;
    // must set --debug before --launcher...
    boolean launcherstop = false;
    boolean launcherprint = false;
    boolean launcherwait = false;
    ArrayList<String> arguments = new ArrayList<>();
    for (String arg : args)
    {
      if (arg.equals("--debug"))
      {
        debug = true;
      }
      if (arg.equals("--quiet"))
      {
        quiet = true;
      }
      if (debug && arg.equals("--launcherprint"))
      {
        launcherprint = true;
      }
      if (debug && arg.equals("--launcherstop"))
      {
        launcherstop = true;
      }
      if (debug && arg.equals("--launcherwait"))
      {
        launcherwait = true;
      }
      // this ends the launcher immediately
      if (debug && arg.equals("--launchernowait"))
      {
        wait = false;
      }
      // Don't add the --launcher... args to Jalview launch
      if (arg.startsWith("--launcher"))
      {
        continue;
      }
      // jvmmempc and jvmmemmax args used to set memory and are not passed on to
      // startClass
      if (arg.startsWith(
              "-" + MemorySetting.MAX_HEAPSIZE_PERCENT_PROPERTY_NAME + "="))
      {
        jvmmempc = arg.substring(
                MemorySetting.MAX_HEAPSIZE_PERCENT_PROPERTY_NAME.length()
                        + 2);
      }
      else if (arg.startsWith(
              "-" + MemorySetting.MAX_HEAPSIZE_PROPERTY_NAME + "="))
      {
        jvmmemmax = arg.substring(
                MemorySetting.MAX_HEAPSIZE_PROPERTY_NAME.length() + 2);
      }
      // --doubledash versions
      else if (arg.startsWith("--"
              + MemorySetting.MAX_HEAPSIZE_PERCENT_PROPERTY_NAME + "="))
      {
        jvmmempc = arg.substring(
                MemorySetting.MAX_HEAPSIZE_PERCENT_PROPERTY_NAME.length()
                        + 3);
      }
      else if (arg.startsWith(
              "--" + MemorySetting.MAX_HEAPSIZE_PROPERTY_NAME + "="))
      {
        jvmmemmax = arg.substring(
                MemorySetting.MAX_HEAPSIZE_PROPERTY_NAME.length() + 3);
      }
      // retain arg
      else
      {
        arguments.add(arg);
      }
    }

    // use saved preferences if no cmdline args
    boolean useCustomisedSettings = LaunchUtils
            .getBooleanUserPreference(MemorySetting.CUSTOMISED_SETTINGS);
    if (useCustomisedSettings)
    {
      if (jvmmempc == null)
      {
        jvmmempc = LaunchUtils
                .getUserPreference(MemorySetting.MEMORY_JVMMEMPC);
      }
      if (jvmmemmax == null)
      {
        jvmmemmax = LaunchUtils
                .getUserPreference(MemorySetting.MEMORY_JVMMEMMAX);
      }
    }

    // add these settings if not already specified
    boolean memSet = false;
    boolean dockIcon = false;
    boolean dockName = false;
    for (int i = 0; i < command.size(); i++)
    {
      String arg = command.get(i);
      if (arg.startsWith("-Xmx"))
      {
        // only use -Xmx if jvmmemmax and jvmmempc have not been set
        if (jvmmempc == null && jvmmemmax == null)
        {
          memSetting = arg;
          memSet = true;
        }
      }
      else if (arg.startsWith("-Xdock:icon"))
      {
        dockIcon = true;
      }
      else if (arg.startsWith("-Xdock:name"))
      {
        dockName = true;
      }
    }

    if (!memSet)
    {
      long maxMemLong = MemorySetting.getMemorySetting(jvmmemmax, jvmmempc);

      if (maxMemLong > 0)
      {
        memSetting = "-Xmx" + Long.toString(maxMemLong);
        memSet = true;
        command.add(memSetting);
      }
    }

    if (isAMac)
    {
      if (!dockIcon)
      {
        String dockIconPath = System.getProperty("getdownappdir", ".")
                + File.separator + "resource/jalview_logo.png";
        command.add("-Xdock:icon=" + dockIconPath);
      }
      if (!dockName)
      {
        // -Xdock:name=... doesn't actually work :(
        // Leaving it in in case it gets fixed
        command.add("-Xdock:name=" + appName);
        // this launches WITHOUT an icon in the macOS dock. Could be useful for
        // getdown?
        // command.add("-Dapple.awt.UIElement=false");
        // This also does not work for the dock
        command.add("-Dcom.apple.mrj.application.apple.menu.about.name="
                + appName);
      }
    }

    String scalePropertyArg = HiDPISetting.getScalePropertyArg();
    if (scalePropertyArg != null)
    {
      sysout(debug, quiet, "Running " + startClass + " with scale setting "
              + scalePropertyArg);
      command.add(scalePropertyArg);
    }

    command.add(startClass);
    command.addAll(arguments);

    final ProcessBuilder builder = new ProcessBuilder(command);

    if ((Boolean.parseBoolean(System.getProperty("launcherprint", "false"))
            || launcherprint))
    {
      sysout(debug, quiet,
              "LAUNCHER COMMAND: " + String.join(" ", builder.command()));
    }
    sysout(debug, quiet,
            "Running " + startClass + " with "
                    + (memSetting == null ? "no memory setting"
                            : ("memory setting " + memSetting)));

    if (Boolean.parseBoolean(System.getProperty("launcherstop", "false"))
            || (debug && launcherstop))
    {
      sysout(debug, quiet,
              "System property 'launcherstop' is set and not 'false'. Exiting.");
      System.exit(0);
    }
    try
    {
      builder.inheritIO();
      Process process = builder.start();
      if (wait || launcherwait)
      {
        sysout(debug, quiet, "Launching application process");
        process.waitFor();
      }
      else
      {
        int waitInt = 0;
        sysout(debug, quiet,
                "Wait time for application process is " + waitInt + "ms");
        process.waitFor(waitInt, TimeUnit.MILLISECONDS);
      }
      sysout(debug, quiet, "Launcher process ending");
    } catch (IOException e)
    {
      if (e.getMessage().toLowerCase(Locale.ROOT).contains("memory"))
      {
        System.err.println("Caught a memory exception: " + e.getMessage());
        // Probably the "Cannot allocate memory" error, try without the memory
        // setting
        ArrayList<String> commandNoMem = new ArrayList<>();
        for (int i = 0; i < command.size(); i++)
        {
          if (!command.get(i).startsWith("-Xmx"))
          {
            commandNoMem.add(command.get(i));
          }
        }
        final ProcessBuilder builderNoMem = new ProcessBuilder(
                commandNoMem);
        System.err.println("Command without memory setting: "
                + String.join(" ", builderNoMem.command()));
        try
        {
          builderNoMem.inheritIO();
          Process processNoMem = builderNoMem.start();
          processNoMem.waitFor();
        } catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
      else
      {
        e.printStackTrace();
      }
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private static void sysout(boolean debug, boolean quiet, String message)
  {
    if (debug && !quiet)
    {
      System.out.println("LAUNCHERDEBUG - " + message);
    }
  }

}
