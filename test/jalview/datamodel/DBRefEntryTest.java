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
package jalview.datamodel;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import jalview.gui.JvOptionPane;
import jalview.util.MapList;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DBRefEntryTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * Tests for the method that compares equality of reference (but not mapping)
   */
  @Test(groups = { "Functional" })
  public void testEqualRef()
  {
    DBRefEntry ref1 = new DBRefEntry("UNIPROT", "1", "V71633");
    assertTrue(ref1.equalRef(ref1));
    assertFalse(ref1.equalRef(null));

    // comparison is not case sensitive
    DBRefEntry ref2 = new DBRefEntry("uniprot", "1", "v71633");
    assertTrue(ref1.equalRef(ref2));
    assertTrue(ref2.equalRef(ref1));

    // source, version and accessionid must match
    assertFalse(ref1.equalRef(new DBRefEntry("UNIPRO", "1", "V71633")));
    assertFalse(ref1.equalRef(new DBRefEntry("UNIPROT", "2", "V71633")));
    assertFalse(ref1.equalRef(new DBRefEntry("UNIPROT", "1", "V71632")));

    // presence of or differences in mappings are ignored
    ref1.setMap(
            new Mapping(new MapList(new int[]
            { 1, 3 }, new int[] { 1, 1 }, 3, 1)));
    assertTrue(ref1.equalRef(ref2));
    assertTrue(ref2.equalRef(ref1));
    ref1.setMap(
            new Mapping(new MapList(new int[]
            { 1, 6 }, new int[] { 1, 2 }, 3, 1)));
    assertTrue(ref1.equalRef(ref2));
    assertTrue(ref2.equalRef(ref1));
  }

  /**
   * Tests for the method that may update a DBRefEntry from another with a
   * mapping or 'real' version
   */
  @Test(groups = { "Functional" })
  public void testUpdateFrom()
  {
    DBRefEntry ref1 = new DBRefEntry("UNIPROT", "1", "V71633");

    assertFalse(ref1.updateFrom(null));

    /*
     * equivalent other dbref
     */
    DBRefEntry ref2 = new DBRefEntry("uniprot", "1", "v71633");
    assertTrue(ref1.updateFrom(ref2));
    assertEquals("UNIPROT", ref1.getSource()); // unchanged
    assertEquals("V71633", ref1.getAccessionId()); // unchanged

    /*
     * ref1 has no mapping, acquires mapping from ref2
     */
    Mapping map = new Mapping(
            new MapList(new int[]
            { 1, 3 }, new int[] { 1, 1 }, 3, 1));
    ref2.setMap(map);
    assertTrue(ref1.updateFrom(ref2));
    assertSame(map, ref1.getMap()); // null mapping updated

    /*
     * ref1 has a mapping, does not acquire mapping from ref2
     */
    ref2.setMap(new Mapping(map));
    assertTrue(ref1.updateFrom(ref2));
    assertSame(map, ref1.getMap()); // non-null mapping not updated

    /*
     * ref2 has a different source, accession or version
     */
    ref2.setSource("pdb");
    assertFalse(ref1.updateFrom(ref2));
    ref2.setSource(ref1.getSource());
    ref2.setAccessionId("P12345");
    assertFalse(ref1.updateFrom(ref2));
    ref2.setAccessionId(ref1.getAccessionId());
    ref1.setVersion("2");
    assertFalse(ref1.updateFrom(ref2));

    /*
     * a non-null version supersedes "0" or "source:0"
     */
    ref2.setVersion(null);
    assertFalse(ref1.updateFrom(ref2));
    assertEquals("2", ref1.getVersion());
    ref2.setVersion("3");
    ref1.setVersion("0");
    assertTrue(ref1.updateFrom(ref2));
    assertEquals("3", ref1.getVersion());
    ref1.setVersion("UNIPROT:0");
    assertTrue(ref1.updateFrom(ref2));
    assertEquals("3", ref1.getVersion());

    /*
     * canonical == false superseded by canonical == true
     */
    ref1.setCanonical(false);
    ref2.setCanonical(true);
    assertTrue(ref1.updateFrom(ref2));
    assertTrue(ref1.isCanonical());

    /*
     * canonical == true NOT superseded by canonical == false
     */
    ref1.setCanonical(true);
    ref2.setCanonical(false);
    assertFalse(ref1.updateFrom(ref2));

    /*
     * version "source:n" with n>0 is not superseded
     */
    ref1.setVersion("UNIPROT:1");
    assertFalse(ref1.updateFrom(ref2));
    assertEquals("UNIPROT:1", ref1.getVersion());

    /*
     * version "10" is not superseded
     */
    ref1.setVersion("10");
    assertFalse(ref1.updateFrom(ref2));
    assertEquals("10", ref1.getVersion());
  }

  @Test(groups = { "Functional" })
  public void testIsPrimaryCandidate()
  {
    DBRefEntry dbr = new DBRefEntry(DBRefSource.UNIPROT, "", "Q12345");
    assertTrue(dbr.isPrimaryCandidate());

    /*
     *  1:1 mapping - ok
     */
    dbr.setMap(
            new Mapping(null, new int[]
            { 1, 3 }, new int[] { 1, 3 }, 1, 1));
    assertTrue(dbr.isPrimaryCandidate());

    /*
     *  1:1 mapping of identical split ranges - not ok
     */
    dbr.setMap(
            new Mapping(null, new int[]
            { 1, 3, 6, 9 }, new int[] { 1, 3, 6, 9 }, 1, 1));
    assertFalse(dbr.isPrimaryCandidate());

    /*
     *  1:1 mapping of different ranges - not ok
     */
    dbr.setMap(
            new Mapping(null, new int[]
            { 1, 4 }, new int[] { 2, 5 }, 1, 1));
    assertFalse(dbr.isPrimaryCandidate());

    /*
     *  1:1 mapping of 'isoform' ranges - not ok
     */
    dbr.setMap(
            new Mapping(null, new int[]
            { 1, 2, 6, 9 }, new int[] { 1, 3, 7, 9 }, 1, 1));
    assertFalse(dbr.isPrimaryCandidate());
    dbr.setMap(null);
    assertTrue(dbr.isPrimaryCandidate());

    /*
     * Version string is prefixed with another dbref source string (fail)
     */
    dbr.setVersion(DBRefSource.EMBL + ":0");
    assertFalse(dbr.isPrimaryCandidate());

    /*
     * Version string is alphanumeric
     */
    dbr.setVersion("0.1.b");
    assertTrue(dbr.isPrimaryCandidate());

    /*
     * null version string can't be primary ref
     */
    dbr.setVersion(null);
    assertFalse(dbr.isPrimaryCandidate());
    dbr.setVersion("");
    assertTrue(dbr.isPrimaryCandidate());

    /*
     *  1:1 mapping and sequenceRef (fail)
     */
    dbr.setMap(
            new Mapping(new Sequence("foo", "ASDF"), new int[]
            { 1, 3 }, new int[] { 1, 3 }, 1, 1));
    assertFalse(dbr.isPrimaryCandidate());

    /*
     * 1:3 mapping (fail)
     */
    dbr.setMap(
            new Mapping(null, new int[]
            { 1, 3 }, new int[] { 1, 3 }, 1, 3));
    assertFalse(dbr.isPrimaryCandidate());

    /*
     * 2:2 mapping with shift (expected fail, but maybe use case for a pass)
     */
    dbr.setMap(
            new Mapping(null, new int[]
            { 1, 4 }, new int[] { 1, 4 }, 2, 2));
    assertFalse(dbr.isPrimaryCandidate());
  }
}
