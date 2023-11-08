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

public interface JLoggerI
{
  public enum LogLevel
  {
    ALL, TRACE, DEBUG, INFO, WARN, ERROR, FATAL
  }

  public LogLevel getLevel();

  public void setLevel(LogLevel level);

  public void trace(String message);

  public void trace(String message, Throwable t);

  public void debug(String message);

  public void debug(String message, Throwable t);

  public void info(String message);

  public void info(String message, Throwable t);

  public void warn(String message);

  public void warn(String message, Throwable t);

  public void error(String message);

  public void error(String message, Throwable t);

  public void fatal(String message);

  public void fatal(String message, Throwable t);

  public boolean isDebugEnabled();

  public boolean isTraceEnabled();
}
