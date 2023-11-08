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

import jalview.util.Platform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class UrlDownloadClient
{
  /**
   * Download and save a file from a URL
   * 
   * @param urlstring
   *          url to download from, as string
   * @param outfile
   *          the name of file to save the URLs to
   * @throws IOException
   */
  public static void download(String urlstring, String outfile)
          throws IOException
  {

    FileOutputStream fos = null;
    ReadableByteChannel rbc = null;
    Path temp = null;
    try
    {
      temp = Files.createTempFile(".jalview_", ".tmp");

      URL url = new URL(urlstring);
      rbc = Channels.newChannel(url.openStream());
      fos = new FileOutputStream(temp.toString());
      fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

      // copy tempfile to outfile once our download completes
      // incase something goes wrong
      Files.copy(temp, Paths.get(outfile),
              StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e)
    {
      throw e;
    } finally
    {
      try
      {
        if (fos != null)
        {
          fos.close();
        }
      } catch (IOException e)
      {
        System.out.println(
                "Exception while closing download file output stream: "
                        + e.getMessage());
      }
      try
      {
        if (rbc != null)
        {
          rbc.close();
        }
      } catch (IOException e)
      {
        System.out.println("Exception while closing download channel: "
                + e.getMessage());
      }
      try
      {
        if (temp != null)
        {
          Files.deleteIfExists(temp);
        }
      } catch (IOException e)
      {
        System.out.println("Exception while deleting download temp file: "
                + e.getMessage());
      }
    }

  }

  public static void download(String urlstring, File tempFile)
          throws IOException
  {
    if (!Platform.setFileBytes(tempFile, urlstring))
    {
      download(urlstring, tempFile.toString());
    }
  }
}
