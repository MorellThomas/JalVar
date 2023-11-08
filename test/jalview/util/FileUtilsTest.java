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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test
public class FileUtilsTest
{
  @Test(groups = "Functional", dataProvider = "patternsAndMinNumFiles")
  public void testJavaFileGlob(String pattern, int atLeast, int atMost)
  {
    List<File> files = FileUtils.getFilesFromGlob(pattern);
    if (atLeast != -1)
    {
      Assert.assertTrue(files.size() > atLeast,
              "Did not find more than " + atLeast + " files with " + pattern
                      + " (found " + files.size() + ")");
    }
    if (atLeast != -1)
    {
      Assert.assertTrue(files.size() > atLeast,
              "Did not find more than " + atLeast + " files with " + pattern
                      + " (found " + files.size() + ")");
    }
    if (atMost != -1)
    {
      Assert.assertTrue(files.size() < atMost,
              "Did not find fewer than " + atMost + " files with " + pattern
                      + " (found " + files.size() + ")");
    }
  }

  @Test(groups = "Functional", dataProvider = "dirnamesAndBasenames")
  public void testDirnamesAndBasenames(String filename, int where,
          String dirname, String basename, String notInDirname)
  {
    File file = new File(filename);
    String d = FileUtils.getDirname(file);
    String b = FileUtils.getBasename(file);
    Assert.assertEquals(b, basename);
    if (where == 0)
      Assert.assertEquals(d, dirname);
    else if (where < 0)
      Assert.assertTrue(d.startsWith(dirname),
              "getDirname(" + file.getPath() + ")=" + d
                      + " didn't start with '" + dirname + "'");
    else if (where > 0)
      Assert.assertTrue(d.endsWith(dirname), "getDirname(" + file.getPath()
              + ")=" + d + " didn't end with '" + d + "'");

    // ensure dirname doesn't end with basename (which means you can't use same
    // filename as dir in tests!)
    Assert.assertFalse(d.endsWith(b));

    if (notInDirname != null)
      Assert.assertFalse(d.contains(notInDirname));
  }

  @DataProvider(name = "patternsAndMinNumFiles")
  public Object[][] patternsAndMinNumFiles()
  {
    return new Object[][] { { "src/**/*.java", 900, 100000 },
        { "src/**.java", 900, 100000 },
        { "test/**/*.java", 250, 2500 },
        { "test/**.java", 250, 2500 },
        { "help/**/*.html", 100, 1000 },
        { "test/**/F*.java", 15, 150 },
        { "test/jalview/*/F*.java", 10, 15 }, // 12 at time of writing
        { "test/jalview/**/F*.java", 18, 30 }, // 20 at time of writing
        { "test/jalview/util/F**.java", 1, 5 }, // 2 at time of writing
        { "src/jalview/b*/*.java", 14, 19 }, // 15 at time of writing
        { "src/jalview/b**/*.java", 20, 25 }, // 22 at time of writing
    };
  }

  @DataProvider(name = "dirnamesAndBasenames")
  public Object[][] dirnamesAndBasenames()
  {
    String homeDir = null;
    try
    {
      homeDir = new File(System.getProperty("user.home"))
              .getCanonicalPath();
    } catch (IOException e)
    {
      System.err.println("Problem getting canonical home dir");
      e.printStackTrace();
    }
    return new Object[][] { // -1=startsWith, 0=equals, 1=endsWith
        { "~/hello/sailor", -1, homeDir, "sailor", "~" }, //
        { "~/hello/sailor", 1, "/hello", "sailor", "~" }, //
        { "./examples/uniref50.fa", -1, "/", "uniref50", "." }, //
        { "./examples/uniref50.fa", 1, "/examples", "uniref50", "." }, //
        { "examples/uniref50.fa", 1, "/examples", "uniref50", ".fa" }, //
    };
  }
}
