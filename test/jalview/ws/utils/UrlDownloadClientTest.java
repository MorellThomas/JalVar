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
package jalview.ws.utils;

import java.io.File;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

public class UrlDownloadClientTest
{

  /**
   * Test that url is successfully loaded into download file
   */
  @Test(groups = { "Network" }, enabled = true)
  public void UrlDownloadTest()
  {
    UrlDownloadClient client = new UrlDownloadClient();
    String urlstring = "http://identifiers.org/rest/collections/";
    String outfile = "testfile.tmp";

    try
    {
      client.download(urlstring, outfile);
    } catch (IOException e)
    {
      Assert.fail("Exception was thrown from UrlDownloadClient download: "
              + e.getMessage());
      File f = new File(outfile);
      if (f.exists())
      {
        f.delete();
      }
    }

    // download file exists
    File f = new File(outfile);
    Assert.assertTrue(f.exists());

    // download file has a believable size
    // identifiers.org file typically at least 250K
    Assert.assertTrue(f.length() > 250000);

    if (f.exists())
    {
      f.delete();
    }

  }

  /**
   * Test that garbage in results in IOException
   */
  @Test(
    groups =
    { "Network" },
    enabled = true,
    expectedExceptions =
    { IOException.class })
  public void DownloadGarbageUrlTest() throws IOException
  {
    UrlDownloadClient client = new UrlDownloadClient();
    String urlstring = "identifiers.org/rest/collections/";
    String outfile = "testfile.tmp";

    client.download(urlstring, outfile);
  }
}
