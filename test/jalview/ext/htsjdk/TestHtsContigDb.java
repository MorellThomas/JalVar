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
package jalview.ext.htsjdk;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import jalview.datamodel.SequenceI;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.testng.annotations.Test;

/**
 * @author jprocter
 *
 */
public class TestHtsContigDb
{
  @Test(groups = "Functional")
  public final void testGetSequenceProxy() throws Exception
  {
    String pathname = "test/jalview/ext/htsjdk/pgmB.fasta";
    HtsContigDb db = new HtsContigDb("ADB", new File(pathname));

    assertTrue(db.isValid());
    assertTrue(db.isIndexed()); // htsjdk opens the .fai file

    SequenceI sq = db.getSequenceProxy("Deminut");
    assertNotNull(sq);
    assertEquals(sq.getLength(), 606);

    /*
     * read a sequence earlier in the file
     */
    sq = db.getSequenceProxy("PPL_06716");
    assertNotNull(sq);
    assertEquals(sq.getLength(), 602);

    // dict = db.getDictionary(f, truncate))
  }

  /**
   * Trying to open a .fai file directly results in IllegalArgumentException -
   * have to provide the unindexed file name instead
   */
  @Test(
    groups = "Functional",
    expectedExceptions = java.lang.IllegalArgumentException.class)
  public final void testGetSequenceProxy_indexed()
  {
    String pathname = "test/jalview/ext/htsjdk/pgmB.fasta.fai";
    new HtsContigDb("ADB", new File(pathname));
    fail("Expected exception opening .fai file");
  }

  /**
   * Tests that exercise
   * <ul>
   * <li>opening an unindexed fasta file</li>
   * <li>creating a .fai index</li>
   * <li>opening the fasta file, now using the index</li>
   * <li>error on creating index if overwrite not allowed</li>
   * </ul>
   * 
   * @throws IOException
   */
  @Test(groups = "Functional")
  public void testCreateFastaSequenceIndex() throws IOException
  {
    File fasta = new File("test/jalview/ext/htsjdk/pgmB.fasta");

    /*
     * create .fai with no overwrite fails if it exists
     */
    try
    {
      HtsContigDb.createFastaSequenceIndex(fasta.toPath(), false);
      fail("Expected exception");
    } catch (IOException e)
    {
      // we expect an IO Exception because the pgmB.fasta.fai exists, since it
      // was checked it in.
    }

    /*
     * create a copy of the .fasta (as a temp file)
     */
    File copyFasta = File.createTempFile("copyFasta", ".fasta");
    copyFasta.deleteOnExit();
    assertTrue(copyFasta.exists());
    Files.copy(fasta.toPath(), copyFasta.toPath(),
            StandardCopyOption.REPLACE_EXISTING);

    /*
     * open the Fasta file - not indexed, as no .fai file yet exists
     */
    HtsContigDb db = new HtsContigDb("ADB", copyFasta);
    assertTrue(db.isValid());
    assertFalse(db.isIndexed());
    db.close();

    /*
     * create the .fai index, re-open the .fasta file - now indexed
     */
    HtsContigDb.createFastaSequenceIndex(copyFasta.toPath(), true);
    db = new HtsContigDb("ADB", copyFasta);
    assertTrue(db.isValid());
    assertTrue(db.isIndexed());
    db.close();
  }

  /**
   * A convenience 'test' that may be run to create a .fai file for any given
   * fasta file
   * 
   * @throws IOException
   */
  @Test(enabled = false)
  public void testCreateIndex() throws IOException
  {

    File fasta = new File("test/jalview/io/vcf/contigs.fasta");
    HtsContigDb.createFastaSequenceIndex(fasta.toPath(), true);
  }
}
