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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.bin.Console;
import jalview.gui.JvOptionPane;

/**
 * @author jimp
 * 
 */
public class FileIOTester
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
    Console.initLogger();
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass(alwaysRun = true)
  public static void tearDownAfterClass() throws Exception
  {
  }

  // TODO: make a better/more comprehensive test harness for identify/io

  final static File ALIGN_FILE = new File(
          "test/jalview/io/test_gz_fasta.gz");

  final static File NOTGZALIGN_FILE = new File(
          "test/jalview/io/test_gz_fasta_notgz.gz");

  final static File STARS_FA_FILE1 = new File(
          "test/jalview/io/test_fasta_stars.fa");

  final static File STARS_FA_FILE2 = new File(
          "test/jalview/io/test_fasta_stars2.fa");

  private void assertValidFormat(FileFormatI fmt, String src, FileParse fp)
          throws FileFormatException
  {
    AssertJUnit.assertTrue("Couldn't resolve " + src + " as a valid file",
            fp.isValid());
    FileFormatI type = new IdentifyFile().identify(fp);
    AssertJUnit.assertSame("Data from '" + src + "' Expected to be '" + fmt
            + "' identified as '" + type + "'", type, fmt);
  }

  @Test(groups = { "Functional" })
  public void testStarsInFasta1() throws IOException
  {
    String uri;
    FileParse fp = new FileParse(
            uri = STARS_FA_FILE1.getAbsoluteFile().toString(),
            DataSourceType.FILE);
    assertValidFormat(FileFormat.Fasta, uri, fp);
  }

  @Test(groups = { "Functional" })
  public void testStarsInFasta2() throws IOException
  {
    String uri;
    FileParse fp = new FileParse(
            uri = STARS_FA_FILE2.getAbsoluteFile().toString(),
            DataSourceType.FILE);
    assertValidFormat(FileFormat.Fasta, uri, fp);
  }

  @Test(groups = { "Functional" })
  public void testGzipIo() throws IOException
  {
    String uri;
    FileParse fp = new FileParse(
            uri = ALIGN_FILE.getAbsoluteFile().toURI().toString(),
            DataSourceType.URL);
    assertValidFormat(FileFormat.Fasta, uri, fp);
  }

  @Test(groups = { "Functional" })
  public void testGziplocalFileIO() throws IOException
  {
    String filepath;
    FileParse fp = new FileParse(
            filepath = ALIGN_FILE.getAbsoluteFile().toString(),
            DataSourceType.FILE);
    assertValidFormat(FileFormat.Fasta, filepath, fp);
  }

  @Test(groups = { "Functional" })
  public void testIsGzipInputStream() throws IOException
  {
    InputStream is = new FileInputStream(ALIGN_FILE);

    /*
     * first try fails - FileInputStream does not support mark/reset
     */
    assertFalse(FileParse.isGzipStream(is));

    /*
     * wrap in a BufferedInputStream and try again
     */
    is = new BufferedInputStream(is, 16);
    assertTrue(FileParse.isGzipStream(is));

    /*
     * check recognition of non-gzipped input
     */
    assertFalse(FileParse.isGzipStream(new BufferedInputStream(
            new ByteArrayInputStream("NOT A GZIP".getBytes()))));
  }

  @Test(groups = { "Functional" })
  public void testNonGzipURLIO() throws IOException
  {
    String uri;
    FileParse fp = new FileParse(
            uri = NOTGZALIGN_FILE.getAbsoluteFile().toURI().toString(),
            DataSourceType.URL);
    assertValidFormat(FileFormat.Fasta, uri, fp);
  }

  @Test(groups = { "Functional" })
  public void testNonGziplocalFileIO() throws IOException
  {
    String filepath;
    FileParse fp = new FileParse(
            filepath = NOTGZALIGN_FILE.getAbsoluteFile().toString(),
            DataSourceType.FILE);
    assertValidFormat(FileFormat.Fasta, filepath, fp);
  }
}
