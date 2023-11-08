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

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

import jalview.log.JLoggerI.LogLevel;
import jalview.util.Log4j;

/**
 * From http://textareaappender.zcage.com/ the means to capture the logs, too.
 * Simple example of creating a Log4j appender that will write to a JTextArea.
 */
public class JalviewAppender extends AbstractAppender
{
  public final static String NAME = "JalviewAppender";

  public JalviewAppender()
  {
    this(LogLevel.INFO);
  }

  public JalviewAppender(LogLevel loglevel)
  {
    super(NAME,
            Log4j.getThresholdFilter(loglevel == null ? Level.INFO
                    : Log4j.log4jLevel(loglevel)),
            Log4j.getSimpleLayout(), false, new Property[0]);
  }

  protected JalviewAppender(String name, Filter filter,
          Layout<? extends Serializable> layout, boolean ignoreExceptions,
          Property[] properties)
  {
    super(name, filter, layout, ignoreExceptions, properties);
    // TODO Auto-generated constructor stub
  }

  static private JTextArea jTextArea = null;

  /** Set the target JTextArea for the logging information to appear. */
  static public void setTextArea(JTextArea jTextArea)
  {
    JalviewAppender.jTextArea = jTextArea;
  }

  /**
   * Format and then append the loggingEvent to the stored JTextArea.
   */
  public void append(LogEvent logEvent)
  {
    final String message = new String(
            this.getLayout().toByteArray(logEvent), StandardCharsets.UTF_8);

    // Append formatted message to textarea using the Swing Thread.
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        if (jTextArea != null)
        {
          jTextArea.append(message);
        }
      }
    });
  }
}
