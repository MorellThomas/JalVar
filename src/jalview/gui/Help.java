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

import java.awt.Point;
import java.net.URL;

import javax.help.BadIDException;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;

import jalview.util.BrowserLauncher;
import jalview.util.Platform;

/**
 * Utility class to show the help documentation window
 * 
 * @author gmcarstairs
 */
public class Help
{
  private static final String HELP_PAGE_ROOT = "http://www.jalview.org/help/";

  /**
   * Defines selected help targets with links to inbuilt (Java) help page
   * target, and externally hosted help page. Will need to be maintained
   * manually if help pages are reorganised in future.
   */
  public enum HelpId
  {
    Home("home", "help.html"),
    SequenceFeatureSettings("seqfeatures.settings",
            "html/features/featuresettings.html"),
    StructureViewer("viewingpdbs", "html/features/viewingpdbs.html"),
    PdbFts("pdbfts", "html/features/pdbsequencefetcher.html#pdbfts"),
    UniprotFts("uniprotfts",
            "html/features/uniprotsequencefetcher.html#uniprotfts");

    private String id;

    private String path;

    private HelpId(String hepLoc, String htmlPath)
    {
      this.id = hepLoc;
      this.path = htmlPath;
    }

    String getId()
    {
      return this.id;
    }

    String getPath()
    {
      return this.path;
    }
  }

  private static HelpBroker hb;

  /**
   * Not instantiable
   */
  private Help()
  {

  }

  /**
   * Shows the help window, at the entry specified by the given helpId
   * 
   * @param id
   * 
   * @throws HelpSetException
   */
  public static void showHelpWindow(HelpId id) throws HelpSetException
  {
    if (Platform.isJS())
    {
      /*
      try
      {
      */
      BrowserLauncher.openURL(HELP_PAGE_ROOT + id.getPath());
      /*
      } catch (IOException e)
      {
      }
      */
    }
    else
    /**
     * Java only
     * 
     * @j2sIgnore
     */
    {

      ClassLoader cl = Desktop.class.getClassLoader();
      URL url = HelpSet.findHelpSet(cl, "help/help"); // $NON-NLS-$
      HelpSet hs = new HelpSet(cl, url);

      if (hb == null)
      {
        /*
         * create help broker first time (only)
         */
        hb = hs.createHelpBroker();
      }

      try
      {
        hb.setCurrentID(id.getId());
      } catch (BadIDException bad)
      {
        System.out.println("Bad help link: " + id.getId()
                + ": must match a target in help.jhm");
        throw bad;
      }

      /*
       * set Help visible - at its current location if it is already shown,
       * else at a location as determined by the window manager
       */
      Point p = hb.getLocation();
      hb.setLocation(p);
      hb.setDisplayed(true);
    }
  }

  /**
   * Show the Help window at the root entry
   * 
   * @throws HelpSetException
   */
  public static void showHelpWindow() throws HelpSetException
  {
    showHelpWindow(HelpId.Home);
  }
}
