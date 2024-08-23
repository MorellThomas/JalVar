/*
 * Jalview - A Sequence Alignment Editor and Viewer (2.11.3.2)
 * Copyright (C) 2023 The Jalview Authors
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

import java.io.PrintStream;

import jalview.log.JLogger;
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
      outPrintln(message);
      Console.printStackTrace(t);
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
      outPrintln(message);
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
      outPrintln(message);
      Console.printStackTrace(t);
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
      outPrintln(message);
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
      outPrintln(message);
      Console.printStackTrace(t);
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
      outPrintln(message);
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
      outPrintln(message);
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
      outPrintln(message);
      Console.printStackTrace(t);
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
      jalview.bin.Console.errPrintln(message);
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
      jalview.bin.Console.errPrintln(message);
      Console.printStackTrace(t);
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
      jalview.bin.Console.errPrintln(message);
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
      jalview.bin.Console.errPrintln(message);
      Console.printStackTrace(t);
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

      if (providedLogLevel != null && JLogger.isLevel(providedLogLevel))
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
          jalview.bin.Console.errPrintln(
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
      jalview.bin.Console
              .errPrintln("Could not initialise the logger framework");
      Console.printStackTrace(e);
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
    LogLevel l = null;
    try
    {
      l = LogLevel.valueOf(logLevelString);
    } catch (IllegalArgumentException | NullPointerException e1)
    {
      Console.debug("Invalid log level '" + logLevelString + "'");
      return;
    }
    if (l != null)
    {
      log.setLevel(l);
      if (!Platform.isJS())
      {
        Log4j.init(l);
      }
      JLoggerLog4j.getLogger("org.apache.axis", l);
    }
  }

  public static void outPrint()
  {
    outPrint("");
  }

  public static void outPrintln()
  {
    outPrintln("");
  }

  public static void outPrint(Object message)
  {
    outPrintMessage(message, false, false);
  }

  public static void outPrint(Object message, boolean forceStdout)
  {
    outPrintMessage(message, false, forceStdout);
  }

  public static void outPrintln(Object message)
  {
    outPrintMessage(message, true, false);
  }

  public static PrintStream outputStream(boolean forceStdout)
  {
    // send message to stderr if an output file to stdout is expected
    if (!forceStdout && Jalview.getInstance() != null
            && Jalview.getInstance().getBootstrapArgs() != null
            && Jalview.getInstance().getBootstrapArgs().outputToStdout())
    {
      return System.err;
    }
    else
    {
      return System.out;
    }
  }

  public static void outPrintMessage(Object message, boolean newline,
          boolean forceStdout)
  {
    PrintStream ps = outputStream(forceStdout);
    if (newline)
    {
      ps.println(message);
    }
    else
    {
      ps.print(message);
    }
  }

  public static void errPrint()
  {
    errPrint("");
  }

  public static void errPrintln()
  {
    errPrintln("");
  }

  public static void errPrint(Object message)
  {
    System.err.print(message);
  }

  public static void errPrintln(Object message)
  {
    System.err.println(message);
  }

  public static void debugPrintStackTrace(Throwable t)
  {
    if (!isDebugEnabled())
    {
      return;
    }
    // send message to stderr if output to stdout is expected
    printStackTrace(t);
  }

  public static void printStackTrace(Throwable t)
  {
    // send message to stderr if output to stdout is expected
    t.printStackTrace(System.err);
  }

  public final static String LOGGING_TEST_MESSAGE = "Logging to STDERR";

}