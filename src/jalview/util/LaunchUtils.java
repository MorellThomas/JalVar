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
package jalview.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class LaunchUtils
{

  public static void loadChannelProps(File dir)
  {
    ChannelProperties.loadProps(dir);
  }

  private static Properties userPreferences = null;

  public static String getUserPreference(String key)
  {
    if (userPreferences == null)
    {
      String channelPrefsFilename = ChannelProperties
              .getProperty("preferences.filename");
      if (channelPrefsFilename == null)
      {
        return null;
      }
      File propertiesFile = new File(System.getProperty("user.home"),
              channelPrefsFilename);
      if (!propertiesFile.exists())
      {
        return null;
      }
      try
      {
        userPreferences = new Properties();
        userPreferences.load(new FileInputStream(propertiesFile));
      } catch (FileNotFoundException e)
      {
        // didn't find user preferences file
        return null;
      } catch (IOException e)
      {
        System.err.println(e.getMessage());
        return null;
      }
    }
    return userPreferences.getProperty(key);
  }

  public static boolean getBooleanUserPreference(String key)
  {
    return Boolean.parseBoolean(getUserPreference(key));
  }

  public static int JAVA_COMPILE_VERSION = 0;

  public static int getJavaCompileVersion()
  {
    if (Platform.isJS())
    {
      return -1;
    }
    else if (JAVA_COMPILE_VERSION > 0)
    {
      return JAVA_COMPILE_VERSION;
    }
    String buildDetails = "jar:".concat(LaunchUtils.class
            .getProtectionDomain().getCodeSource().getLocation().toString()
            .concat("!" + "/.build_properties"));
    try
    {
      URL localFileURL = new URL(buildDetails);
      InputStream in = localFileURL.openStream();
      Properties buildProperties = new Properties();
      buildProperties.load(in);
      in.close();
      String JCV = buildProperties.getProperty("JAVA_COMPILE_VERSION",
              null);
      if (JCV == null)
      {
        System.out.println(
                "Could not obtain JAVA_COMPILE_VERSION for comparison");
        return -2;
      }
      JAVA_COMPILE_VERSION = Integer.parseInt(JCV);
    } catch (MalformedURLException e)
    {
      System.err.println("Could not find " + buildDetails);
      return -3;
    } catch (IOException e)
    {
      System.err.println("Could not load " + buildDetails);
      return -4;
    } catch (NumberFormatException e)
    {
      System.err.println("Could not parse JAVA_COMPILE_VERSION");
      return -5;
    }

    return JAVA_COMPILE_VERSION;
  }

  public static int JAVA_VERSION = 0;

  public static int getJavaVersion()
  {
    if (Platform.isJS())
    {
      return -1;
    }
    else if (JAVA_VERSION > 0)
    {
      return JAVA_VERSION;
    }
    try
    {
      String JV = System.getProperty("java.version");
      if (JV == null)
      {
        System.out.println("Could not obtain java.version for comparison");
        return -2;
      }
      if (JV.startsWith("1."))
      {
        JV = JV.substring(2);
      }
      JAVA_VERSION = JV.indexOf(".") == -1 ? Integer.parseInt(JV)
              : Integer.parseInt(JV.substring(0, JV.indexOf(".")));
    } catch (NumberFormatException e)
    {
      System.err.println("Could not parse java.version");
      return -3;
    }
    return JAVA_VERSION;
  }

  public static boolean checkJavaVersion()
  {
    if (Platform.isJS())
    {
      return true;
    }
    String buildDetails = "jar:".concat(LaunchUtils.class
            .getProtectionDomain().getCodeSource().getLocation().toString()
            .concat("!" + "/.build_properties"));

    int java_compile_version = getJavaCompileVersion();
    int java_version = getJavaVersion();

    if (java_compile_version <= 0 || java_version <= 0)
    {
      System.out.println("Could not make Java version check");
      return true;
    }
    // Warn if these java.version and JAVA_COMPILE_VERSION conditions exist
    // Usually this means a Java 11 compiled JAR being run by a Java 11 JVM
    if (java_version >= 11 && java_compile_version < 11)
    {
      return false;
    }

    return true;
  }
}
