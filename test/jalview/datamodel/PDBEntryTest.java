/*
    assertEquals(case7, case9);
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
package jalview.datamodel;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import jalview.datamodel.PDBEntry.Type;
import jalview.gui.JvOptionPane;

//import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PDBEntryTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception
  {
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception
  {
  }

  @Test(groups = { "Functional" })
  public void testEquals()
  {
    PDBEntry pdbEntry = new PDBEntry("1xyz", "A", PDBEntry.Type.PDB,
            "x/y/z/File");

    // id comparison is not case sensitive
    PDBEntry case1 = new PDBEntry("1XYZ", "A", PDBEntry.Type.PDB,
            "x/y/z/File");
    // chain code comparison is not case sensitive
    PDBEntry case2 = new PDBEntry("1xyz", "a", PDBEntry.Type.PDB,
            "x/y/z/File");
    // different type
    PDBEntry case3 = new PDBEntry("1xyz", "A", PDBEntry.Type.FILE,
            "x/y/z/File");
    // different everything
    PDBEntry case4 = new PDBEntry(null, null, null, null);
    // null id
    PDBEntry case5 = new PDBEntry(null, "A", PDBEntry.Type.PDB,
            "x/y/z/File");
    // null chain
    PDBEntry case6 = new PDBEntry("1xyz", null, PDBEntry.Type.PDB,
            "x/y/z/File");
    // null type
    PDBEntry case7 = new PDBEntry("1xyz", "A", null, "x/y/z/File");
    // null file
    PDBEntry case8 = new PDBEntry("1xyz", "A", PDBEntry.Type.PDB, null);
    // identical to case7
    PDBEntry case9 = new PDBEntry("1xyz", "A", null, "x/y/z/File");
    // different file only
    PDBEntry case10 = new PDBEntry("1xyz", "A", null, "a/b/c/File");

    /*
     * assertEquals will invoke PDBEntry.equals()
     */
    assertFalse(pdbEntry.equals(null));
    assertFalse(pdbEntry.equals("a"));
    assertEquals(case1, pdbEntry);
    assertEquals(case2, pdbEntry);
    assertNotEquals(case3, pdbEntry);
    assertNotEquals(case4, pdbEntry);
    assertNotEquals(case5, pdbEntry);
    assertNotEquals(case6, pdbEntry);
    assertNotEquals(case7, pdbEntry);
    assertNotEquals(case8, pdbEntry);
    assertEquals(case7, case9);
    assertNotEquals(case9, case10);

    // add properties
    case7.setProperty("hello", "world");
    assertNotEquals(case7, case9);
    case9.setProperty("hello", "world");
    assertEquals(case7, case9);
    case9.setProperty("hello", "WORLD");
    assertNotEquals(case7, case9);

    /*
     * change string wrapper property to string...
     */
    case1.setProperty("chain_code", "a");
    assertFalse(pdbEntry.equals(case1));
    assertFalse(case1.equals(pdbEntry));
  }

  @Test(groups = { "Functional" })
  public void testSetChainCode()
  {
    PDBEntry pdbEntry = new PDBEntry("1xyz", null, PDBEntry.Type.PDB,
            "x/y/z/File");
    assertNull(pdbEntry.getChainCode());

    pdbEntry.setChainCode("a");
    assertEquals("a", pdbEntry.getChainCode());

    pdbEntry.setChainCode(null);
    assertNull(pdbEntry.getChainCode());
  }

  @Test(groups = { "Functional" })
  public void testGetType()
  {
    assertSame(PDBEntry.Type.FILE, PDBEntry.Type.getType("FILE"));
    assertSame(PDBEntry.Type.FILE, PDBEntry.Type.getType("File"));
    assertSame(PDBEntry.Type.FILE, PDBEntry.Type.getType("file"));
    assertNotSame(PDBEntry.Type.FILE, PDBEntry.Type.getType("file "));
  }

  @Test(groups = { "Functional" })
  public void testTypeMatches()
  {
    // TODO Type.matches() is not used - delete?
    assertTrue(PDBEntry.Type.FILE.matches("FILE"));
    assertTrue(PDBEntry.Type.FILE.matches("File"));
    assertTrue(PDBEntry.Type.FILE.matches("file"));
    assertFalse(PDBEntry.Type.FILE.matches("FILE "));
  }

  @Test(groups = { "Functional" })
  public void testUpdateFrom()
  {
    PDBEntry pdb1 = new PDBEntry("3A6S", null, null, null);
    PDBEntry pdb2 = new PDBEntry("3A6S", null, null, null);
    assertTrue(pdb1.updateFrom(pdb2));

    /*
     * mismatch of pdb id not allowed
     */
    pdb2 = new PDBEntry("1A70", "A", null, null);
    assertFalse(pdb1.updateFrom(pdb2));
    assertNull(pdb1.getChainCode());

    /*
     * match of pdb id is not case sensitive
     */
    pdb2 = new PDBEntry("3a6s", "A", null, null);
    assertTrue(pdb1.updateFrom(pdb2));
    assertEquals(pdb1.getChainCode(), "A");
    assertEquals(pdb1.getId(), "3A6S");

    /*
     * add chain - with differing case for id
     */
    pdb1 = new PDBEntry("3A6S", null, null, null);
    pdb2 = new PDBEntry("3a6s", "A", null, null);
    assertTrue(pdb1.updateFrom(pdb2));
    assertEquals(pdb1.getChainCode(), "A");

    /*
     * change of chain is not allowed
     */
    pdb2 = new PDBEntry("3A6S", "B", null, null);
    assertFalse(pdb1.updateFrom(pdb2));
    assertEquals(pdb1.getChainCode(), "A");

    /*
     * change chain from null
     */
    pdb1 = new PDBEntry("3A6S", null, null, null);
    pdb2 = new PDBEntry("3A6S", "B", null, null);
    assertTrue(pdb1.updateFrom(pdb2));
    assertEquals(pdb1.getChainCode(), "B");

    /*
     * set file and type
     */
    pdb2 = new PDBEntry("3A6S", "B", Type.FILE, "filePath");
    assertTrue(pdb1.updateFrom(pdb2));
    assertEquals(pdb1.getFile(), "filePath");
    assertEquals(pdb1.getType(), Type.FILE.toString());
    assertEquals(pdb1.getChainCode(), "B");
    /*
     * change of file is not allowed
     */
    pdb1 = new PDBEntry("3A6S", null, null, "file1");
    pdb2 = new PDBEntry("3A6S", "A", null, "file2");
    assertFalse(pdb1.updateFrom(pdb2));
    assertNull(pdb1.getChainCode());
    assertEquals(pdb1.getFile(), "file1");

    /*
     * set type without change of file
     */
    pdb1 = new PDBEntry("3A6S", null, null, "file1");
    pdb2 = new PDBEntry("3A6S", null, Type.PDB, "file1");
    assertTrue(pdb1.updateFrom(pdb2));
    assertEquals(pdb1.getType(), Type.PDB.toString());

    /*
     * set file with differing case of id and chain code
     */
    pdb1 = new PDBEntry("3A6S", "A", null, null);
    pdb2 = new PDBEntry("3a6s", "a", Type.PDB, "file1");
    assertTrue(pdb1.updateFrom(pdb2));
    assertEquals(pdb1.getType(), Type.PDB.toString());
    assertEquals(pdb1.getId(), "3A6S"); // unchanged
    assertEquals(pdb1.getFile(), "file1"); // updated
    assertEquals(pdb1.getChainCode(), "A"); // unchanged

    /*
     * changing nothing returns true
     */
    pdb1 = new PDBEntry("3A6S", "A", Type.PDB, "file1");
    pdb2 = new PDBEntry("3A6S", null, null, null);
    assertTrue(pdb1.updateFrom(pdb2));
    assertEquals(pdb1.getChainCode(), "A");
    assertEquals(pdb1.getType(), Type.PDB.toString());
    assertEquals(pdb1.getFile(), "file1");

    /*
     * add and update properties only
     */
    pdb1 = new PDBEntry("3A6S", null, null, null);
    pdb2 = new PDBEntry("3A6S", null, null, null);
    pdb1.setProperty("destination", "mars");
    pdb1.setProperty("hello", "world");
    pdb2.setProperty("hello", "moon");
    pdb2.setProperty("goodbye", "world");
    assertTrue(pdb1.updateFrom(pdb2));
    assertEquals(pdb1.getProperty("destination"), "mars");
    assertEquals(pdb1.getProperty("hello"), "moon");
    assertEquals(pdb1.getProperty("goodbye"), "world");

    /*
     * add properties only
     */
    pdb1 = new PDBEntry("3A6S", null, null, null);
    pdb2 = new PDBEntry("3A6S", null, null, null);
    pdb2.setProperty("hello", "moon");
    assertTrue(pdb1.updateFrom(pdb2));
    assertEquals(pdb1.getProperty("hello"), "moon");

    /*
    * different id but authoritative
    */
    pdb1 = new PDBEntry("af:1xyz", "A", null, "a/b/c/File");
    pdb2 = new PDBEntry("af-1xyz", "A", null, "a/b/c/File");
    pdb1.setAuthoritative(true);

    assertTrue(pdb1.isAuthoritative());
    assertFalse(pdb2.isAuthoritative());
    // can update pdb1 (authoritative) from pdb2 (non-authoritative)
    assertTrue(pdb1.updateFrom(pdb2));
    // but the ID must remain the same
    assertEquals(pdb1.getId(), "af:1xyz");

  }

  @Test(groups = { "Functional" })
  public void testConstructor_fromDbref()
  {
    PDBEntry pdb = new PDBEntry(new DBRefEntry("PDB", "0", "1A70"));
    assertEquals(pdb.getId(), "1A70");
    assertNull(pdb.getChainCode());
    assertNull(pdb.getType());
    assertNull(pdb.getFile());

    /*
     * from dbref with chain code appended
     */
    pdb = new PDBEntry(new DBRefEntry("PDB", "0", "1A70B"));
    assertEquals(pdb.getId(), "1A70");
    assertEquals(pdb.getChainCode(), "B");

    /*
     * from dbref with overlong accession
     */
    pdb = new PDBEntry(new DBRefEntry("PDB", "0", "1A70BC"));
    assertEquals(pdb.getId(), "1A70BC");
    assertNull(pdb.getChainCode());

    /*
     * from dbref which is not for PDB
     */
    try
    {
      pdb = new PDBEntry(new DBRefEntry("PDBe", "0", "1A70"));
      fail("Expected exception");
    } catch (IllegalArgumentException e)
    {
      // expected;
    }
  }

}
