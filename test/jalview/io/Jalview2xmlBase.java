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
package jalview.io;

import java.util.Date;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;

import jalview.bin.Cache;
import jalview.bin.Jalview;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.SequenceI;
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;

public class Jalview2xmlBase
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass(alwaysRun = true)
  public static void setUpBeforeClass() throws Exception
  {
    /*
     * use read-only test properties file
     */
    Cache.loadProperties("test/jalview/io/testProps.jvprops");

    /*
     * set news feed last read to a future time to ensure no
     * 'unread' news item is displayed
     */
    Date oneHourFromNow = new Date(
            System.currentTimeMillis() + 3600 * 1000);
    Cache.setDateProperty("JALVIEW_NEWS_RSS_LASTMODIFIED", oneHourFromNow);

    Jalview.main(new String[] {});
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass(alwaysRun = true)
  public static void tearDownAfterClass() throws Exception
  {
    if (Desktop.instance != null)
      Desktop.instance.closeAll_actionPerformed(null);
  }

  @BeforeTest(alwaysRun = true)
  public static void clearDesktop()
  {
    if (Desktop.instance != null && Desktop.getFrames() != null
            && Desktop.getFrames().length > 0)
    {
      Desktop.instance.closeAll_actionPerformed(null);
    }
  }

  public int countDsAnn(jalview.viewmodel.AlignmentViewport avp)
  {
    int numdsann = 0;
    for (SequenceI sq : avp.getAlignment().getDataset().getSequences())
    {
      if (sq.getAnnotation() != null)
      {
        for (AlignmentAnnotation dssa : sq.getAnnotation())
        {
          if (dssa.isValidStruc())
          {
            numdsann++;
          }
        }
      }
    }
    return numdsann;
  }

}
