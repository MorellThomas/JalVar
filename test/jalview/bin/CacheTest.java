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

import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.gui.JvOptionPane;

public class CacheTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  private Locale locale;

  @BeforeClass(alwaysRun = true)
  public void setUpBeforeClass()
  {
    locale = Locale.getDefault();
  }

  @AfterClass(alwaysRun = true)
  public void tearDownAfterClass()
  {
    Locale.setDefault(locale);
  }

  /**
   * Test that saved date format does not vary with current locale
   */
  @Test(groups = "Functional")
  public void testSetDateProperty()
  {
    Date now = new Date();
    Locale.setDefault(Locale.FRENCH);
    String formattedDate = Cache.setDateProperty("test", now);
    Locale.setDefault(Locale.UK);
    String formattedDate2 = Cache.setDateProperty("test", now);
    assertEquals(formattedDate, formattedDate2);

    // currently using Locale.UK to format dates:
    assertEquals(formattedDate2,
            SimpleDateFormat
                    .getDateTimeInstance(SimpleDateFormat.MEDIUM,
                            SimpleDateFormat.MEDIUM, Locale.UK)
                    .format(now));
  }

  @Test(groups = "Functional")
  public void testVersionChecker()
  {
    Cache.loadProperties("test/jalview/bin/testProps.jvprops");
    try
    {
      // 10s sleep to allow VersionChecker thread to run
      Thread.sleep(10000);
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    String latestVersion = Cache.getProperty("LATEST_VERSION");
    assertNotNull(latestVersion);
    assertNotEquals(latestVersion, "test");
    assertTrue(latestVersion.startsWith("2."));
  }
}
