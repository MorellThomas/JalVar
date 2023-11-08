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

import java.util.Locale;

import jalview.log.JLogger;
import jalview.log.JLoggerI;
import jalview.log.JLoggerI.LogLevel;
import jalview.log.JLoggerLog4j;
import jalview.util.ChannelProperties;
import jalview.util.Log4j;
import jalview.util.Platform;

public class Console
{

  public static JLoggerLog4j log;

  public static void debug(String message, Throwable t)
  {
    if (Console.initLogger())
    {
      log.debug(message, t);
    }
    else
    {
      System.out.println(message);
      t.printStackTrace();
    }

  }

  public static void info(String message)
  {
    if (Console.initLogger())
    {
      log.info(message, null);
    }
    else
    {
      System.out.println(message);
    }

  }

  public static void trace(String message, Throwable t)
  {
    if (Console.initLogger())
    {
      log.trace(message, t);
    }
    else
    {
      System.out.println(message);
      t.printStackTrace();
    }
  }

  public static void debug(String message)
  {
    if (Console.initLogger())
    {
      log.debug(message, null);
    }
    else
    {
      System.out.println(message);
    }

  }

  public static void info(String message, Throwable t)
  {
    if (Console.initLogger())
    {
      log.info(message, t);
    }
    else
    {
      System.out.println(message);
      t.printStackTrace();
    }

  }

  public static void warn(String message)
  {
    if (Console.initLogger())
    {
      log.warn(message, null);
    }
    else
    {
      System.out.println(message);
    }

  }

  public static void trace(String message)
  {
    if (Console.initLogger())
    {
      log.trace(message, null);
    }
    else
    {
      System.out.println(message);
    }
  }

  public static void warn(String message, Throwable t)
  {
    if (Console.initLogger())
    {
      log.warn(message, t);
    }
    else
    {
      System.out.println(message);
      t.printStackTrace();
    }

  }

  public static void error(String message)
  {
    if (Console.initLogger())
    {
      log.error(message, null);
    }
    else
    {
      System.err.println(message);
    }

  }

  public static void error(String message, Throwable t)
  {
    if (Console.initLogger())
    {
      log.error(message, t);
    }
    else
    {
      System.err.println(message);
      t.printStackTrace(System.err);
    }

  }

  public static void fatal(String message)
  {
    if (Console.initLogger())
    {
      log.fatal(message, null);
    }
    else
    {
      System.err.println(message);
    }

  }

  public static void fatal(String message, Throwable t)
  {
    if (Console.initLogger())
    {
      log.fatal(message, t);
    }
    else
    {
      System.err.println(message);
      t.printStackTrace(System.err);
    }

  }

  public static boolean isDebugEnabled()
  {
    return log == null ? false : log.isDebugEnabled();
  }

  public static boolean isTraceEnabled()
  {
    return log == null ? false : log.isTraceEnabled();
  }

  public static JLogger.LogLevel getCachedLogLevel()
  {
    return Console.getCachedLogLevel(Cache.JALVIEWLOGLEVEL);
  }

  public static JLogger.LogLevel getCachedLogLevel(String key)
  {
    return getLogLevel(Cache.getDefault(key, "INFO"));
  }

  public static JLogger.LogLevel getLogLevel(String level)
  {
    return JLogger.toLevel(level);
  }

  public static JLogger getLogger()
  {
    return log;
  }

  public static boolean initLogger()
  {
    return initLogger(null);
  }

  public static boolean initLogger(String providedLogLevel)
  {
    if (log != null)
    {
      return true;
    }
    try
    {
      JLogger.LogLevel logLevel = JLogger.LogLevel.INFO;

      if (JLogger.isLevel(providedLogLevel))
      {
        logLevel = Console.getLogLevel(providedLogLevel);
      }
      else
      {
        logLevel = getCachedLogLevel();
      }

      if (!Platform.isJS())
      {
        if (!Jalview.quiet())
        {
          System.err.println(
                  "Setting initial log level to " + logLevel.name());
        }
        Log4j.init(logLevel);
      }
      // log output
      // is laxis used? Does getLogger do anything without a Logger object?
      // Logger laxis = Log4j.getLogger("org.apache.axis", myLevel);
      JLoggerLog4j.getLogger("org.apache.axis", logLevel);

      // The main application logger
      log = JLoggerLog4j.getLogger(Cache.JALVIEW_LOGGER_NAME, logLevel);
    } catch (NoClassDefFoundError e)
    {
      System.err.println("Could not initialise the logger framework");
      e.printStackTrace();
    }

    // Test message
    if (log != null)
    {
      // Logging test message should go through the logger object
      if (log.loggerExists())
        log.debug(Console.LOGGING_TEST_MESSAGE);
      // Tell the user that debug is enabled
      debug(ChannelProperties.getProperty("app_name")
              + " Debugging Output Follows.");
      return true;
    }
    else
    {
      return false;
    }
  }

  public static void setLogLevel(String logLevelString)
  {
    for (LogLevel logLevel : JLoggerI.LogLevel.values())
    {
      if (logLevel.toString().toLowerCase(Locale.ROOT)
              .equals(logLevelString.toLowerCase(Locale.ROOT)))
      {
        log.setLevel(logLevel);
        if (!Platform.isJS())
        {
          Log4j.init(logLevel);
        }
        JLoggerLog4j.getLogger("org.apache.axis", logLevel);
        break;
      }
    }
  }

  public final static String LOGGING_TEST_MESSAGE = "Logging to STDERR";

}
