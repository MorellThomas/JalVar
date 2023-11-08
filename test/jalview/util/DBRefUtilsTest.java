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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.Mapping;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DBRefUtilsTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * Test the method that selects DBRefEntry items whose source is in a supplied
   * list
   */
  @Test(groups = { "Functional" })
  public void testSelectRefs()
  {
    assertNull(DBRefUtils.selectRefs(null, null));
    assertNull(DBRefUtils.selectRefs(null, DBRefSource.CODINGDBS));

    DBRefEntry ref1 = new DBRefEntry("EMBL", "1.2", "A12345");
    DBRefEntry ref2 = new DBRefEntry("UNIPROT", "1.2", "A12346");
    // Source is converted to upper-case by this constructor!
    DBRefEntry ref3 = new DBRefEntry("Uniprot", "1.2", "A12347");
    List<DBRefEntry> dbrefs = Arrays
            .asList(new DBRefEntry[]
            { ref1, ref2, ref3 });
    String[] sources = new String[] { "EMBL", "UNIPROT" };

    List<DBRefEntry> selected = DBRefUtils.selectRefs(dbrefs, sources);
    assertEquals(3, selected.size());
    assertSame(ref1, selected.get(0));
    assertSame(ref2, selected.get(1));
    assertSame(ref3, selected.get(2));

    sources = new String[] { "EMBL" };
    selected = DBRefUtils.selectRefs(dbrefs, sources);
    assertEquals(1, selected.size());
    assertSame(ref1, selected.get(0));

    sources = new String[] { "UNIPROT" };
    selected = DBRefUtils.selectRefs(dbrefs, sources);
    assertEquals(2, selected.size());
    assertSame(ref2, selected.get(0));
    assertSame(ref3, selected.get(1));

    sources = new String[] { "EMBLCDS" };
    selected = DBRefUtils.selectRefs(dbrefs, sources);
    assertNull(selected);

    sources = new String[] { "embl", "uniprot" };
    selected = DBRefUtils.selectRefs(dbrefs, sources);
    assertEquals(3, selected.size());
    assertSame(ref1, selected.get(0));
    assertSame(ref2, selected.get(1));
    assertSame(ref3, selected.get(2));
  }

  /**
   * Test the method that converts (currently three) database names to a
   * canonical name (not case-sensitive)
   */
  @Test(groups = { "Functional" })
  public void testGetCanonicalName()
  {
    assertNull(DBRefUtils.getCanonicalName(null));
    assertEquals("", DBRefUtils.getCanonicalName(""));
    assertEquals("PDB", DBRefUtils.getCanonicalName("pdb"));
    assertEquals("PDB", DBRefUtils.getCanonicalName("Pdb"));
    assertEquals("UNIPROT",
            DBRefUtils.getCanonicalName("uniprotkb/swiss-prot"));
    assertEquals("UNIPROT",
            DBRefUtils.getCanonicalName("uniprotkb/trembl"));
    assertEquals("UNIPROT",
            DBRefUtils.getCanonicalName("UNIPROTKB/SWISS-PROT"));
    assertEquals("UNIPROT",
            DBRefUtils.getCanonicalName("UNIPROTKB/TREMBL"));
    assertEquals("UNIPROTKB/SWISS-CHEESE",
            DBRefUtils.getCanonicalName("UNIPROTKB/SWISS-CHEESE"));
    assertEquals("ENSEMBL", DBRefUtils.getCanonicalName("Ensembl"));

    // these are not 'known' to Jalview
    assertEquals("PFAM", DBRefUtils.getCanonicalName("PFAM"));
    assertEquals("pfam", DBRefUtils.getCanonicalName("pfam"));

  }

  /**
   * Test 'parsing' a DBRef - non PDB case
   */
  @Test(groups = { "Functional" })
  public void testParseToDbRef()
  {
    SequenceI seq = new Sequence("Seq1", "ABCD");
    DBRefEntry ref = DBRefUtils.parseToDbRef(seq, "EMBL", "1.2", "a7890");
    List<DBRefEntry> refs = seq.getDBRefs();
    assertEquals(1, refs.size());
    assertSame(ref, refs.get(0));
    assertEquals("EMBL", ref.getSource());
    assertEquals("1.2", ref.getVersion());
    assertEquals("a7890", ref.getAccessionId());
    assertTrue(seq.getAllPDBEntries().isEmpty());
    SequenceI seq2 = new Sequence("Seq2", "ABCD");
    // Check that whitespace doesn't confuse parseToDbRef
    DBRefEntry ref2 = DBRefUtils.parseToDbRef(seq2, "EMBL", "1.2",
            " a7890");
    assertEquals(ref, ref2);
  }

  /**
   * Test 'parsing' a DBRef - Stockholm PDB format
   */
  @Test(groups = { "Functional" })
  public void testParseToDbRef_PDB()
  {
    SequenceI seq = new Sequence("Seq1", "ABCD");
    DBRefEntry ref = DBRefUtils.parseToDbRef(seq, "pdb", "1.2",
            "1WRI A; 7-80;");
    // TODO: correct PDBEntry and PDB DBRef accessions need to be generated for
    // PDB ref in Stockholm

    List<DBRefEntry> refs = seq.getDBRefs();
    assertEquals(1, refs.size());
    assertSame(ref, refs.get(0));
    assertEquals("PDB", ref.getSource());
    assertEquals("1.2", ref.getVersion());
    // DBRef id is pdbId + chain code
    assertEquals("1WRIA", ref.getAccessionId());
    assertEquals(1, seq.getAllPDBEntries().size());
    PDBEntry pdbRef = seq.getAllPDBEntries().get(0);
    assertEquals("1WRI", pdbRef.getId());
    assertNull(pdbRef.getFile());
    assertEquals("A", pdbRef.getChainCode());
    assertEquals("PDB", pdbRef.getType());
  }

  /**
   * Test the method that searches for matches references - case when we are
   * matching a reference with no mappings
   */
  @Test(groups = { "Functional" })
  public void testSearchRefs_noMapping()
  {
    DBRefEntry target = new DBRefEntry("EMBL", "2", "A1234");

    DBRefEntry ref1 = new DBRefEntry("EMBL", "1", "A1234"); // matches
    // constructor changes embl to EMBL
    DBRefEntry ref2 = new DBRefEntry("embl", "1", "A1234"); // matches
    // constructor does not upper-case accession id
    DBRefEntry ref3 = new DBRefEntry("EMBL", "1", "a1234"); // no match
    DBRefEntry ref4 = new DBRefEntry("EMBLCDS", "1", "A1234"); // no match
    // ref5 matches although it has a mapping - ignored
    DBRefEntry ref5 = new DBRefEntry("EMBL", "1", "A1234");
    ref5.setMap(
            new Mapping(new MapList(new int[]
            { 1, 1 }, new int[] { 1, 1 }, 1, 1)));

    List<DBRefEntry> matches = DBRefUtils
            .searchRefs(Arrays.asList(new DBRefEntry[]
            { ref1, ref2, ref3, ref4, ref5 }), target,
                    DBRefUtils.SEARCH_MODE_FULL);
    assertEquals(3, matches.size());
    assertSame(ref1, matches.get(0));
    assertSame(ref2, matches.get(1));
    assertSame(ref5, matches.get(2));
  }

  /**
   * Test the method that searches for matches references - case when we are
   * matching a reference with a mapping
   */
  @Test(groups = { "Functional" })
  public void testSearchRefs_withMapping()
  {
    DBRefEntry target = new DBRefEntry("EMBL", "2", "A1234");
    final Mapping map1 = new Mapping(
            new MapList(new int[]
            { 1, 1 }, new int[] { 1, 1 }, 1, 1));
    target.setMap(map1);

    // these all match target iff mappings match
    DBRefEntry ref1 = new DBRefEntry("EMBL", "1", "A1234"); // no map: matches
    DBRefEntry ref2 = new DBRefEntry("EMBL", "1", "A1234"); // =map: matches
    final Mapping map2 = new Mapping(
            new MapList(new int[]
            { 1, 1 }, new int[] { 1, 1 }, 1, 1));
    ref2.setMap(map2);

    // different map: no match
    DBRefEntry ref3 = new DBRefEntry("EMBL", "1", "A1234");
    final Mapping map3 = new Mapping(
            new MapList(new int[]
            { 1, 1 }, new int[] { 1, 1 }, 2, 2));
    ref3.setMap(map3);

    List<DBRefEntry> matches = DBRefUtils
            .searchRefs(Arrays.asList(new DBRefEntry[]
            { ref1, ref2, ref3 }), target, DBRefUtils.SEARCH_MODE_FULL);
    assertEquals(2, matches.size());
    assertSame(ref1, matches.get(0));
    assertSame(ref2, matches.get(1));
  }

  /**
   * Test the method that searches for matching references based on accession id
   * only
   */
  @Test(groups = { "Functional" })
  public void testSearchRefs_accessionid()
  {

    DBRefEntry ref1 = new DBRefEntry("Uniprot", "1", "A1234"); // matches
    DBRefEntry ref2 = new DBRefEntry("embl", "1", "A1234"); // matches
    // constructor does not upper-case accession id
    DBRefEntry ref3 = new DBRefEntry("EMBL", "1", "a1234"); // no match
    DBRefEntry ref4 = new DBRefEntry("EMBLCDS", "1", "A1235"); // no match
    // ref5 matches although it has a mapping - ignored
    DBRefEntry ref5 = new DBRefEntry("EMBL", "1", "A1234");
    ref5.setMap(
            new Mapping(new MapList(new int[]
            { 1, 1 }, new int[] { 1, 1 }, 1, 1)));

    List<DBRefEntry> dbrefs = Arrays
            .asList(new DBRefEntry[]
            { ref1, ref2, ref3, ref4, ref5 });
    List<DBRefEntry> matches = DBRefUtils.searchRefs(dbrefs, "A1234");
    assertEquals(3, matches.size());
    assertSame(ref1, matches.get(0));
    assertSame(ref2, matches.get(1));
    assertSame(ref5, matches.get(2));
  }

  /**
   * Test the method that searches for matches references - case when we are
   * matching a reference with null (any) accession id
   */
  @Test(groups = { "Functional" })
  public void testSearchRefs_wildcardAccessionid()
  {
    DBRefEntry target = new DBRefEntry("EMBL", "2", null);

    DBRefEntry ref1 = new DBRefEntry("EMBL", "1", "A1234"); // matches
    // constructor changes embl to EMBL
    DBRefEntry ref2 = new DBRefEntry("embl", "1", "A1235"); // matches
    // constructor does not upper-case accession id
    DBRefEntry ref3 = new DBRefEntry("EMBL", "1", "A1236"); // matches
    DBRefEntry ref4 = new DBRefEntry("EMBLCDS", "1", "A1234"); // no match
    // ref5 matches although it has a mapping - ignored
    DBRefEntry ref5 = new DBRefEntry("EMBL", "1", "A1237");
    ref5.setMap(
            new Mapping(new MapList(new int[]
            { 1, 1 }, new int[] { 1, 1 }, 1, 1)));

    List<DBRefEntry> matches = DBRefUtils
            .searchRefs(Arrays.asList(new DBRefEntry[]
            { ref1, ref2, ref3, ref4, ref5 }), target,
                    DBRefUtils.SEARCH_MODE_FULL);
    assertEquals(4, matches.size());
    assertSame(ref1, matches.get(0));
    assertSame(ref2, matches.get(1));
    assertSame(ref3, matches.get(2));
    assertSame(ref5, matches.get(3));
  }
}
