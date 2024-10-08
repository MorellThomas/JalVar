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
package jalview.log;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;

import jalview.util.Log4j;
import jalview.util.Platform;

public class JLoggerLog4j extends JLogger implements JLoggerI
{
  private Logger logger = null;

  public static JLoggerLog4j getLogger(Class c)
  {
    return getLogger(c);
  }

  public static JLoggerLog4j getLogger(Class c, LogLevel loglevel)
  {
    return getLogger(c.getCanonicalName(), loglevel);
  }

  public static JLoggerLog4j getLogger(String name)
  {
    return getLogger(name, LogLevel.INFO);
  }

  public static JLoggerLog4j getLogger(String name, LogLevel loglevel)
  {
    return registryContainsKey(name) ? (JLoggerLog4j) registryGet(name)
            : new JLoggerLog4j(name, loglevel);
  }

  private JLoggerLog4j(String name, LogLevel level)
  {
    this.name = name;
    this.level = level;
    this.loggerSetup();
    this.registryStore();
  }

  @Override
  protected void loggerSetup()
  {
    if (!Platform.isJS())
      this.logger = Log4j.isInit() ? Log4j.getLogger(this.name, this.level)
              : null;
  }

  @Override
  public boolean loggerExists()
  {
    return logger != null;
  }

  @Override
  protected void loggerSetLevel(JLoggerI.LogLevel level)
  {
    if (loggerExists())
    {
      Log4j.setLevel(logger, level);
    }
  }

  @Override
  protected void loggerLogMessage(LogLevel level, String message,
          Throwable t)
  {
    if (!loggerExists())
      return;
    if (t != null)
    {
      switch (level)
      {
      case FATAL:
        logger.fatal(message, t);
        break;
      case ERROR:
        logger.error(message, t);
        break;
      case WARN:
        logger.warn(message, t);
        break;
      case INFO:
        logger.info(message, t);
        break;
      case DEBUG:
        logger.debug(message, t);
        break;
      case TRACE:
        logger.trace(message, t);
        break;
      case ALL:
        logger.trace(message, t);
        break;
      }
    }
    else
    {
      switch (level)
      {
      case FATAL:
        logger.fatal(message);
        break;
      case ERROR:
        logger.error(message);
        break;
      case WARN:
        logger.warn(message);
        break;
      case INFO:
        logger.info(message);
        break;
      case DEBUG:
        logger.debug(message);
        break;
      case TRACE:
        logger.trace(message);
        break;
      case ALL:
        logger.trace(message);
        break;
      }
    }
  }

  private Logger getLoggerObject()
  {
    return this.logger;
  }

  public synchronized static void addAppender(JLoggerLog4j level,
          Appender appender)
  {
    if (!Platform.isJS())
      Log4j.addAppender(level.getLoggerObject(), appender);
  }

  public synchronized static void addAppender(JLoggerLog4j l1,
          JLoggerLog4j l2, String name)
  {
    if (!Platform.isJS())
      Log4j.addAppender(l1.getLoggerObject(), l2.getLoggerObject(), name);
  }

}
