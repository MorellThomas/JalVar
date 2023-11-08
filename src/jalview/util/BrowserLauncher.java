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

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import jalview.bin.Cache;
import jalview.bin.Console;

public class BrowserLauncher
{
  private static BrowserLauncher INSTANCE = null;

  private static String preferredBrowser = null;

  public static BrowserLauncher getInstance()
  {
    if (INSTANCE != null)
    {
      return INSTANCE;
    }
    INSTANCE = new BrowserLauncher();
    return INSTANCE;
  }

  public static void openURL(String url)
  {
    if (Platform.isJS())
    {
      Platform.openURL(url);
      return;
    }
    else
    /**
     * Java only
     * 
     * @j2sIgnore
     */
    {
      Desktop d = Desktop.getDesktop();
      if (d != null && d.isSupported(Desktop.Action.BROWSE))
      {
        try
        {
          d.browse(new URI(url));
        } catch (IOException e)
        {
          Console.warn(MessageManager.formatMessage(
                  "exception.browser_unable_to_launch", url));
          Console.warn(e.getMessage());
          Console.debug(Cache.getStackTraceString(e));
        } catch (URISyntaxException e1)
        {
          Console.warn(MessageManager.formatMessage(
                  "exception.browser_unable_to_launch", url));
          Console.warn(e1.getMessage());
          Console.debug(Cache.getStackTraceString(e1));
        }
      }
      else
      {
        Console.warn(MessageManager
                .formatMessage("exception.browser_os_not_supported", url));
      }
    }
  }

  public static void resetBrowser()
  {
    resetBrowser(false);
  }

  public static void resetBrowser(boolean removeIfNull)
  {
    String defaultBrowser = Cache.getProperty("DEFAULT_BROWSER");
    preferredBrowser = defaultBrowser;
    // System.setProperty(getBrowserSystemProperty(),
    // Cache.getProperty("DEFAULT_BROWSER"));
    if (defaultBrowser == null && removeIfNull)
    {
      // System.clearProperty(getBrowserSystemProperty());
    }

  }

  public static List<String> getBrowserList()
  {
    return new ArrayList<String>();
  }

  public static String getBrowserSystemProperty()
  {
    // return IBrowserLaunching.BROWSER_SYSTEM_PROPERTY;
    return "jalview.default.browser";
  }

}