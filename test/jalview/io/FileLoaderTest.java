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

import org.junit.Assert;
import org.testng.annotations.Test;

public class FileLoaderTest
{

  @Test(groups = { "Network" })
  public void testDownloadStructuresIfInputFromURL()
  {
    String urlFile = "http://www.jalview.org/builds/develop/examples/3W5V.pdb";
    FileLoader fileLoader = new FileLoader();
    fileLoader.LoadFileWaitTillLoaded(urlFile, DataSourceType.URL,
            FileFormat.PDB);
    Assert.assertNotNull(fileLoader.file);
    // The FileLoader's file is expected to be same as the original URL.
    Assert.assertEquals(urlFile, fileLoader.file);
    // Data source type expected to be DataSourceType.URL
    Assert.assertEquals(DataSourceType.URL, fileLoader.protocol);
  }
}
