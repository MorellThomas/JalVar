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
package jalview.gui;

import com.formdev.flatlaf.extras.FlatDesktop;
import com.formdev.flatlaf.extras.FlatDesktop.Action;

import jalview.util.Platform;

public class APQHandlers
{
  public static boolean setAbout = false;

  public static boolean setPreferences = false;

  public static boolean setQuit = false;

  public static boolean setAPQHandlers(Desktop desktop)
  {
    if (Platform.isJS())
    {
      return false;
    }
    if (FlatDesktop.isSupported(Action.APP_ABOUT))
    {
      FlatDesktop.setAboutHandler(() -> {
        desktop.aboutMenuItem_actionPerformed(null);
      });
      setAbout = true;
    }
    if (FlatDesktop.isSupported(Action.APP_PREFERENCES))
    {
      FlatDesktop.setPreferencesHandler(() -> {
        desktop.preferences_actionPerformed(null);
      });
      setPreferences = true;
    }
    if (FlatDesktop.isSupported(Action.APP_QUIT_HANDLER))
    {
      QuitHandler.setQuitHandler();
      setQuit = true;
    }
    // if we got to here, no exceptions occurred when we set the handlers.
    return setAbout || setPreferences || setQuit;
  }

}
