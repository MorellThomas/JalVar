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

import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.FilterComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;

import jalview.log.JLogger;
import jalview.log.JalviewAppender;

public class Log4j
{

  public final static String SIMPLE_PATTERN = "%level - %m%n";

  private static boolean init = false;

  public static boolean isInit()
  {
    return init;
  }

  public static Level log4jLevel(JLogger.LogLevel loglevel)
  {
    return Level.toLevel(loglevel.toString());
  }

  public static void init(JLogger.LogLevel myLevel)
  {
    init(log4jLevel(myLevel));
  }

  public static void init(Level myLevel)
  {
    if (init)
      return;
    try
    {
      // configure the root logger to stderr
      ConfigurationBuilder<BuiltConfiguration> configBuilder = Log4j
              .getConfigurationBuilder();

      configBuilder.setStatusLevel(Level.WARN);

      String consoleLoggerName = "STDERR";
      AppenderComponentBuilder appenderBuilder = configBuilder
              .newAppender(consoleLoggerName, "Console");
      appenderBuilder.addAttribute("target",
              ConsoleAppender.Target.SYSTEM_ERR);
      appenderBuilder.add(Log4j.getSimpleLayoutBuilder());
      appenderBuilder.add(Log4j.getThresholdFilterBuilder());
      configBuilder.add(appenderBuilder);

      configBuilder.add(configBuilder.newRootLogger(myLevel)
              .add(configBuilder.newAppenderRef(consoleLoggerName)));

      Configurator.initialize(configBuilder.build());

      init = true;
    } catch (Exception e)
    {
      System.err.println("Problems initializing the log4j system\n");
      e.printStackTrace(System.err);
    }
  }

  public static Logger getLogger(String name)
  {
    return getLogger(name, Level.INFO);
  }

  public static Logger getLogger(String name, JLogger.LogLevel loglevel)
  {
    return getLogger(name, log4jLevel(loglevel));
  }

  public static Logger getLogger(String name, Level level)
  {
    Logger logger = LogManager.getLogger(name);
    Log4j.setLevel(logger, level);
    return logger;
  }

  public static ConfigurationBuilder<BuiltConfiguration> getConfigurationBuilder()
  {
    return ConfigurationFactory.newConfigurationBuilder();
  }

  public static Layout getSimpleLayout()
  {
    return PatternLayout.newBuilder().withPattern(SIMPLE_PATTERN).build();
  }

  public static LayoutComponentBuilder getSimpleLayoutBuilder()
  {
    return getConfigurationBuilder().newLayout("PatternLayout")
            .addAttribute("pattern", Log4j.SIMPLE_PATTERN);
  }

  public static Filter getThresholdFilter(Level level)
  {
    return ThresholdFilter.createFilter(level, Filter.Result.ACCEPT,
            Filter.Result.NEUTRAL);
  }

  public static FilterComponentBuilder getThresholdFilterBuilder()
  {
    return getConfigurationBuilder().newFilter("ThresholdFilter",
            Filter.Result.ACCEPT, Filter.Result.NEUTRAL);
  }

  public static void setLevel(Logger logger, JLogger.LogLevel loglevel)
  {
    setLevel(logger, log4jLevel(loglevel));
  }

  public static void setLevel(Logger logger, Level level)
  {
    if (!Platform.isJS())
    {
      LoggerContext context = (LoggerContext) LogManager.getContext(false);
      Configuration config = context.getConfiguration();
      LoggerConfig loggerConfig = config.getLoggerConfig(logger.getName());
      loggerConfig.setLevel(level);

      Map<String, Appender> appenders = config.getAppenders();

      Appender jappender = config.getAppender(JalviewAppender.NAME);

      context.updateLoggers();
    }
  }

  public static void setRootLevel(JLogger.LogLevel loglevel)
  {
    setRootLevel(log4jLevel(loglevel));
  }

  public static void setRootLevel(Level level)
  {
    setLevel(LogManager.getRootLogger(), level);
  }

  public static Appender getAppender(String name)
  {
    LoggerContext context = (LoggerContext) LogManager.getContext(false);
    Configuration config = context.getConfiguration();
    Map<String, Appender> appenders = config.getAppenders();
    return appenders.get(name);
  }

  public static void addAppender(Logger logger, Logger logger2,
          String name2)
  {
    LoggerContext context = (LoggerContext) LogManager.getContext(false);
    Configuration config = context.getConfiguration();
    LoggerConfig logger2Config = config.getLoggerConfig(logger2.getName());
    Map<String, Appender> logger2AppendersMap = logger2Config
            .getAppenders();
    Appender appender = logger2AppendersMap.get(name2);
    addAppender(logger, appender);
    context.updateLoggers();
  }

  public static void addAppender(Logger logger, Appender appender)
  {
    if (appender == null)
      return;
    LoggerContext context = (LoggerContext) LogManager.getContext(false);
    Configuration config = context.getConfiguration();
    LoggerConfig loggerConfig = config.getLoggerConfig(logger.getName());
    if (loggerConfig == null)
      return;

    Level level = loggerConfig.getLevel();

    config.addAppender(appender);
    loggerConfig.addAppender(appender, null, null);

    context.updateLoggers();
  }

  public static void addAppenderToRootLogger(Appender appender)
  {
    Log4j.addAppender(LogManager.getRootLogger(), appender);
  }
}
