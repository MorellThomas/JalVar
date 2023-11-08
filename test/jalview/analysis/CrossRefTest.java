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
package jalview.analysis;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jalview.bin.Cache;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.AlignedCodonFrame.SequenceToSequenceMapping;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.Mapping;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;
import jalview.util.DBRefUtils;
import jalview.util.MapList;
import jalview.ws.SequenceFetcher;
import jalview.ws.SequenceFetcherFactory;

public class CrossRefTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @BeforeMethod(alwaysRun = true)
  public void loadProperties()
  {
    Cache.loadProperties("test/jalview/util/comparisonTestProps.jvprops");
  }

  @Test(groups = { "Functional" })
  public void testFindXDbRefs()
  {
    DBRefEntry ref1 = new DBRefEntry("UNIPROT", "1", "A123");
    DBRefEntry ref2 = new DBRefEntry("UNIPROTKB/TREMBL", "1", "A123");
    DBRefEntry ref3 = new DBRefEntry("pdb", "1", "A123");
    DBRefEntry ref4 = new DBRefEntry("EMBLCDSPROTEIN", "1", "A123");
    DBRefEntry ref5 = new DBRefEntry("embl", "1", "A123");
    DBRefEntry ref6 = new DBRefEntry("emblCDS", "1", "A123");
    DBRefEntry ref7 = new DBRefEntry("GeneDB", "1", "A123");
    DBRefEntry ref8 = new DBRefEntry("PFAM", "1", "A123");
    // ENSEMBL is a source of either dna or protein sequence data
    DBRefEntry ref9 = new DBRefEntry("ENSEMBL", "1", "A123");
    List<DBRefEntry> refs = Arrays
            .asList(new DBRefEntry[]
            { ref1, ref2, ref3, ref4, ref5, ref6, ref7, ref8, ref9 });

    /*
     * Just the DNA refs:
     */
    List<DBRefEntry> found = DBRefUtils.selectDbRefs(true, refs);
    assertEquals(4, found.size());
    assertSame(ref5, found.get(0));
    assertSame(ref6, found.get(1));
    assertSame(ref7, found.get(2));
    assertSame(ref9, found.get(3));

    /*
     * Just the protein refs:
     */
    found = DBRefUtils.selectDbRefs(false, refs);
    assertEquals(4, found.size());
    assertSame(ref1, found.get(0));
    assertSame(ref2, found.get(1));
    assertSame(ref4, found.get(2));
    assertSame(ref9, found.get(3));
  }

  /**
   * Test the method that finds a sequence's "product" xref source databases,
   * which may be direct (dbrefs on the sequence), or indirect (dbrefs on
   * sequences which share a dbref with the sequence
   */
  @Test(groups = { "Functional" }, enabled = true)
  public void testFindXrefSourcesForSequence_proteinToDna()
  {
    SequenceI seq = new Sequence("Seq1", "MGKYQARLSS");
    List<String> sources = new ArrayList<>();
    AlignmentI al = new Alignment(new SequenceI[] {});

    /*
     * first with no dbrefs to search
     */
    sources = new CrossRef(new SequenceI[] { seq }, al)
            .findXrefSourcesForSequences(false);
    assertTrue(sources.isEmpty());

    /*
     * add some dbrefs to sequence
     */
    // protein db is not a candidate for findXrefSources
    seq.addDBRef(new DBRefEntry("UNIPROT", "0", "A1234"));
    // dna coding databatases are
    seq.addDBRef(new DBRefEntry("EMBL", "0", "E2345"));
    // a second EMBL xref should not result in a duplicate
    seq.addDBRef(new DBRefEntry("EMBL", "0", "E2346"));
    seq.addDBRef(new DBRefEntry("EMBLCDS", "0", "E2347"));
    seq.addDBRef(new DBRefEntry("GENEDB", "0", "E2348"));
    seq.addDBRef(new DBRefEntry("ENSEMBL", "0", "E2349"));
    seq.addDBRef(new DBRefEntry("ENSEMBLGENOMES", "0", "E2350"));
    sources = new CrossRef(new SequenceI[] { seq }, al)
            .findXrefSourcesForSequences(false);
    // method is patched to remove EMBL from the sources to match
    assertEquals(4, sources.size());
    assertEquals("[EMBLCDS, GENEDB, ENSEMBL, ENSEMBLGENOMES]",
            sources.toString());

    /*
     * add a sequence to the alignment which has a dbref to UNIPROT|A1234
     * and others to dna coding databases
     */
    sources.clear();
    seq.setDBRefs(null);
    seq.addDBRef(new DBRefEntry("UNIPROT", "0", "A1234"));
    seq.addDBRef(new DBRefEntry("EMBLCDS", "0", "E2347"));
    SequenceI seq2 = new Sequence("Seq2", "MGKYQARLSS");
    seq2.addDBRef(new DBRefEntry("UNIPROT", "0", "A1234"));
    seq2.addDBRef(new DBRefEntry("EMBL", "0", "E2345"));
    seq2.addDBRef(new DBRefEntry("GENEDB", "0", "E2348"));
    // TODO include ENSEMBLGENOMES in DBRefSource.DNACODINGDBS ?
    al.addSequence(seq2);
    sources = new CrossRef(new SequenceI[] { seq, seq2 }, al)
            .findXrefSourcesForSequences(false);
    // method removed EMBL from sources to match
    assertEquals(2, sources.size());
    assertEquals("[EMBLCDS, GENEDB]", sources.toString());
  }

  /**
   * Test for finding 'product' sequences for the case where only an indirect
   * xref is found - not on the nucleotide sequence but on a peptide sequence in
   * the alignment which which it shares a nucleotide dbref
   */
  @Test(groups = { "Functional" }, enabled = true)
  public void testFindXrefSequences_indirectDbrefToProtein()
  {
    /*
     * Alignment setup:
     *   - nucleotide dbref  EMBL|AF039662
     *   - peptide    dbrefs EMBL|AF039662, UNIPROT|Q9ZTS2
     */
    SequenceI emblSeq = new Sequence("AF039662", "GGGGCAGCACAAGAAC");
    emblSeq.addDBRef(new DBRefEntry("EMBL", "0", "AF039662"));
    SequenceI uniprotSeq = new Sequence("Q9ZTS2", "MASVSATMISTS");
    uniprotSeq.addDBRef(new DBRefEntry("EMBL", "0", "AF039662"));
    uniprotSeq.addDBRef(new DBRefEntry("UNIPROT", "0", "Q9ZTS2"));

    /*
     * Find UNIPROT xrefs for nucleotide 
     * - it has no UNIPROT dbref of its own
     * - but peptide with matching nucleotide dbref does, so is returned
     */
    AlignmentI al = new Alignment(new SequenceI[] { emblSeq, uniprotSeq });
    Alignment xrefs = new CrossRef(new SequenceI[] { emblSeq }, al)
            .findXrefSequences("UNIPROT", true);
    System.err.println("xrefs=" + xrefs);
    assertEquals(1, xrefs.getHeight());
    assertSame(uniprotSeq, xrefs.getSequenceAt(0));
  }

  /**
   * Test for finding 'product' sequences for the case where only an indirect
   * xref is found - not on the peptide sequence but on a nucleotide sequence in
   * the alignment which which it shares a protein dbref
   */
  @Test(groups = { "Functional" }, enabled = true)
  public void testFindXrefSequences_indirectDbrefToNucleotide()
  {
    /*
     * Alignment setup:
     *   - peptide    dbref  UNIPROT|Q9ZTS2
     *   - nucleotide dbref  EMBL|AF039662, UNIPROT|Q9ZTS2
     */
    SequenceI uniprotSeq = new Sequence("Q9ZTS2", "MASVSATMISTS");
    uniprotSeq.addDBRef(new DBRefEntry("UNIPROT", "0", "Q9ZTS2"));
    SequenceI emblSeq = new Sequence("AF039662", "GGGGCAGCACAAGAAC");
    emblSeq.addDBRef(new DBRefEntry("EMBL", "0", "AF039662"));
    emblSeq.addDBRef(new DBRefEntry("UNIPROT", "0", "Q9ZTS2"));

    /*
     * find EMBL xrefs for peptide sequence - it has no direct
     * dbrefs, but the 'corresponding' nucleotide sequence does, so is returned
     */
    /*
     * Find EMBL xrefs for peptide 
     * - it has no EMBL dbref of its own
     * - but nucleotide with matching peptide dbref does, so is returned
     */
    AlignmentI al = new Alignment(new SequenceI[] { emblSeq, uniprotSeq });
    Alignment xrefs = new CrossRef(new SequenceI[] { uniprotSeq }, al)
            .findXrefSequences("EMBL", false);
    assertEquals(1, xrefs.getHeight());
    assertSame(emblSeq, xrefs.getSequenceAt(0));
  }

  /**
   * Test for finding 'product' sequences for the case where the selected
   * sequence has no dbref to the desired source, and there are no indirect
   * references via another sequence in the alignment
   */
  @Test(groups = { "Functional" })
  public void testFindXrefSequences_noDbrefs()
  {
    /*
     * two nucleotide sequences, one with UNIPROT dbref
     */
    SequenceI dna1 = new Sequence("AF039662", "GGGGCAGCACAAGAAC");
    dna1.addDBRef(new DBRefEntry("UNIPROT", "0", "Q9ZTS2"));
    SequenceI dna2 = new Sequence("AJ307031", "AAACCCTTT");

    /*
     * find UNIPROT xrefs for peptide sequence - it has no direct
     * dbrefs, and the other sequence (which has a UNIPROT dbref) is not 
     * equatable to it, so no results found
     */
    AlignmentI al = new Alignment(new SequenceI[] { dna1, dna2 });
    Alignment xrefs = new CrossRef(new SequenceI[] { dna2 }, al)
            .findXrefSequences("UNIPROT", true);
    assertNull(xrefs);
  }

  /**
   * Tests for the method that searches an alignment (with one sequence
   * excluded) for protein/nucleotide sequences with a given cross-reference
   */
  @Test(groups = { "Functional" }, enabled = true)
  public void testSearchDataset()
  {
    /*
     * nucleotide sequence with UNIPROT AND EMBL dbref
     * peptide sequence with UNIPROT dbref
     */
    SequenceI dna1 = new Sequence("AF039662", "GGGGCAGCACAAGAAC");
    Mapping map = new Mapping(new Sequence("pep2", "MLAVSRG"),
            new MapList(new int[]
            { 1, 21 }, new int[] { 1, 7 }, 3, 1));
    DBRefEntry dbref = new DBRefEntry("UNIPROT", "0", "Q9ZTS2", map);
    dna1.addDBRef(dbref);
    dna1.addDBRef(new DBRefEntry("EMBL", "0", "AF039662"));
    SequenceI pep1 = new Sequence("Q9ZTS2", "MLAVSRGQ");
    dbref = new DBRefEntry("UNIPROT", "0", "Q9ZTS2");
    pep1.addDBRef(new DBRefEntry("UNIPROT", "0", "Q9ZTS2"));
    AlignmentI al = new Alignment(new SequenceI[] { dna1, pep1 });

    List<SequenceI> result = new ArrayList<>();

    /*
     * first search for a dbref nowhere on the alignment:
     */
    dbref = new DBRefEntry("UNIPROT", "0", "P30419");
    CrossRef testee = new CrossRef(al.getSequencesArray(), al);
    AlignedCodonFrame acf = new AlignedCodonFrame();
    boolean found = testee.searchDataset(true, dna1, dbref, result, acf,
            true, DBRefUtils.SEARCH_MODE_FULL);
    assertFalse(found);
    assertTrue(result.isEmpty());
    assertTrue(acf.isEmpty());

    /*
     * search for a protein sequence with dbref UNIPROT:Q9ZTS2
     */
    acf = new AlignedCodonFrame();
    dbref = new DBRefEntry("UNIPROT", "0", "Q9ZTS2");
    found = testee.searchDataset(!dna1.isProtein(), dna1, dbref, result,
            acf, false, DBRefUtils.SEARCH_MODE_FULL); // search dataset with a
                                                      // protein xref from a dna
    // sequence to locate the protein product
    assertTrue(found);
    assertEquals(1, result.size());
    assertSame(pep1, result.get(0));
    assertTrue(acf.isEmpty());

    /*
     * search for a nucleotide sequence with dbref UNIPROT:Q9ZTS2
     */
    result.clear();
    acf = new AlignedCodonFrame();
    dbref = new DBRefEntry("UNIPROT", "0", "Q9ZTS2");
    found = testee.searchDataset(!pep1.isProtein(), pep1, dbref, result,
            acf, false, DBRefUtils.SEARCH_MODE_FULL); // search dataset with a
                                                      // protein's direct dbref
                                                      // to
    // locate dna sequences with matching xref
    assertTrue(found);
    assertEquals(1, result.size());
    assertSame(dna1, result.get(0));
    // should now have a mapping from dna to pep1
    List<SequenceToSequenceMapping> mappings = acf.getMappings();
    assertEquals(1, mappings.size());
    SequenceToSequenceMapping mapping = mappings.get(0);
    assertSame(dna1, mapping.getFromSeq());
    assertSame(pep1, mapping.getMapping().getTo());
    MapList mapList = mapping.getMapping().getMap();
    assertEquals(1, mapList.getToRatio());
    assertEquals(3, mapList.getFromRatio());
    assertEquals(1, mapList.getFromRanges().size());
    assertEquals(1, mapList.getFromRanges().get(0)[0]);
    assertEquals(21, mapList.getFromRanges().get(0)[1]);
    assertEquals(1, mapList.getToRanges().size());
    assertEquals(1, mapList.getToRanges().get(0)[0]);
    assertEquals(7, mapList.getToRanges().get(0)[1]);
  }

  /**
   * Test for finding 'product' sequences for the case where the selected
   * sequence has a dbref with a mapping to a sequence. This represents the case
   * where either
   * <ul>
   * <li>a fetched sequence is already decorated with its cross-reference (e.g.
   * EMBL + translation), or</li>
   * <li>Get Cross-References has been done once resulting in instantiated
   * cross-reference mappings</li>
   * </ul>
   */
  @Test(groups = { "Functional" })
  public void testFindXrefSequences_fromDbRefMap()
  {
    /*
     * scenario: nucleotide sequence AF039662
     *   with dbref + mapping to Q9ZTS2 and P30419
     *     which themselves each have a dbref and feature
     */
    SequenceI dna1 = new Sequence("AF039662", "GGGGCAGCACAAGAAC");
    SequenceI pep1 = new Sequence("Q9ZTS2", "MALFQRSV");
    SequenceI pep2 = new Sequence("P30419", "MTRRSQIF");
    dna1.createDatasetSequence();
    pep1.createDatasetSequence();
    pep2.createDatasetSequence();

    pep1.getDatasetSequence()
            .addDBRef(new DBRefEntry("Pfam", "0", "PF00111"));
    pep1.addSequenceFeature(
            new SequenceFeature("type", "desc", 12, 14, 1f, "group"));
    pep2.getDatasetSequence().addDBRef(new DBRefEntry("PDB", "0", "3JTK"));
    pep2.addSequenceFeature(
            new SequenceFeature("type2", "desc2", 13, 15, 12f, "group2"));

    MapList mapList = new MapList(new int[] { 1, 24 }, new int[] { 1, 3 },
            3, 1);
    Mapping map = new Mapping(pep1, mapList);
    DBRefEntry dbRef1 = new DBRefEntry("UNIPROT", "0", "Q9ZTS2", map);
    dna1.getDatasetSequence().addDBRef(dbRef1);
    mapList = new MapList(new int[] { 1, 24 }, new int[] { 1, 3 }, 3, 1);
    map = new Mapping(pep2, mapList);
    DBRefEntry dbRef2 = new DBRefEntry("UNIPROT", "0", "P30419", map);
    dna1.getDatasetSequence().addDBRef(dbRef2);

    /*
     * find UNIPROT xrefs for nucleotide sequence - it should pick up 
     * mapped sequences
     */
    AlignmentI al = new Alignment(new SequenceI[] { dna1 });
    Alignment xrefs = new CrossRef(new SequenceI[] { dna1 }, al)
            .findXrefSequences("UNIPROT", true);
    assertEquals(2, xrefs.getHeight());

    /*
     * cross-refs alignment holds copies of the mapped sequences
     * including copies of their dbrefs and features
     */
    checkCopySequence(pep1, xrefs.getSequenceAt(0));
    checkCopySequence(pep2, xrefs.getSequenceAt(1));
  }

  /**
   * Helper method that verifies that 'copy' has the same name, start, end,
   * sequence and dataset sequence object as 'original' (but is not the same
   * object)
   * 
   * @param copy
   * @param original
   */
  private void checkCopySequence(SequenceI copy, SequenceI original)
  {
    assertNotSame(copy, original);
    assertSame(copy.getDatasetSequence(), original.getDatasetSequence());
    assertEquals(copy.getName(), original.getName());
    assertEquals(copy.getStart(), original.getStart());
    assertEquals(copy.getEnd(), original.getEnd());
    assertEquals(copy.getSequenceAsString(),
            original.getSequenceAsString());
  }

  /**
   * Test for finding 'product' sequences for the case where the selected
   * sequence has a dbref with no mapping, triggering a fetch from database
   */
  @Test(groups = { "Functional_Failing" })
  public void testFindXrefSequences_withFetch()
  {
    // JBPNote: this fails because pep1 and pep2 do not have DbRefEntrys with
    // mappings
    // Fix#1 would be to revise the test data so it fits with 2.11.2+ Jalview
    // assumptions
    // that ENA retrievals yield dbrefs with Mappings

    SequenceI dna1 = new Sequence("AF039662", "GGGGCAGCACAAGAAC");
    dna1.addDBRef(new DBRefEntry("UNIPROT", "ENA:0", "Q9ZTS2"));
    dna1.addDBRef(new DBRefEntry("UNIPROT", "ENA:0", "P30419"));
    dna1.addDBRef(new DBRefEntry("UNIPROT", "ENA:0", "P00314"));
    final SequenceI pep1 = new Sequence("Q9ZTS2", "MYQLIRSSW");
    pep1.addDBRef(new DBRefEntry("UNIPROT", "0", "Q9ZTS2", null, true));

    final SequenceI pep2 = new Sequence("P00314", "MRKLLAASG");
    pep2.addDBRef(new DBRefEntry("UNIPROT", "0", "P00314", null, true));

    /*
     * argument false suppresses adding DAS sources
     * todo: define an interface type SequenceFetcherI and mock that
     */
    SequenceFetcher mockFetcher = new SequenceFetcher()
    {
      @Override
      public boolean isFetchable(String source)
      {
        return true;
      }

      @Override
      public SequenceI[] getSequences(List<DBRefEntry> refs, boolean dna)
      {
        return new SequenceI[] { pep1, pep2 };
      }
    };
    SequenceFetcherFactory.setSequenceFetcher(mockFetcher);

    /*
     * find UNIPROT xrefs for nucleotide sequence
     */
    AlignmentI al = new Alignment(new SequenceI[] { dna1 });
    Alignment xrefs = new CrossRef(new SequenceI[] { dna1 }, al)
            .findXrefSequences("UNIPROT", true);
    assertEquals(2, xrefs.getHeight());
    assertSame(pep1, xrefs.getSequenceAt(0));
    assertSame(pep2, xrefs.getSequenceAt(1));
  }

  @AfterClass(alwaysRun = true)
  public void tearDown()
  {
    SequenceFetcherFactory.setSequenceFetcher(null);
  }

  /**
   * Test for finding 'product' sequences for the case where both gene and
   * transcript sequences have dbrefs to Uniprot.
   */
  @Test(groups = { "Functional_Failing" })
  public void testFindXrefSequences_forGeneAndTranscripts()
  {
    /*
     * 'gene' sequence
     */
    SequenceI gene = new Sequence("ENSG00000157764", "CGCCTCCCTTCCCC");
    gene.addDBRef(new DBRefEntry("UNIPROT", "0", "P15056"));
    gene.addDBRef(new DBRefEntry("UNIPROT", "0", "H7C5K3"));

    /*
     * 'transcript' with CDS feature (supports mapping to protein)
     */
    SequenceI braf001 = new Sequence("ENST00000288602",
            "taagATGGCGGCGCTGa");
    braf001.addDBRef(new DBRefEntry("UNIPROT", "0", "P15056"));
    braf001.addSequenceFeature(
            new SequenceFeature("CDS", "", 5, 16, 0f, null));

    /*
     * 'spliced transcript' with CDS ranges
     */
    SequenceI braf002 = new Sequence("ENST00000497784",
            "gCAGGCtaTCTGTTCaa");
    braf002.addDBRef(new DBRefEntry("UNIPROT", "ENSEMBL|0", "H7C5K3"));
    braf002.addSequenceFeature(
            new SequenceFeature("CDS", "", 2, 6, 0f, null));
    braf002.addSequenceFeature(
            new SequenceFeature("CDS", "", 9, 15, 0f, null));

    /*
     * TODO code is fragile - use of SequenceIdMatcher depends on fetched
     * sequences having a name starting Source|Accession
     * which happens to be true for Uniprot,PDB,EMBL but not Pfam,Rfam,Ensembl 
     */
    final SequenceI pep1 = new Sequence("UNIPROT|P15056", "MAAL");
    pep1.addDBRef(new DBRefEntry("UNIPROT", "0", "P15056"));
    final SequenceI pep2 = new Sequence("UNIPROT|H7C5K3", "QALF");
    pep2.addDBRef(new DBRefEntry("UNIPROT", "0", "H7C5K3"));
    /*
     * argument false suppresses adding DAS sources
     * todo: define an interface type SequenceFetcherI and mock that
     */
    SequenceFetcher mockFetcher = new SequenceFetcher()
    {
      @Override
      public boolean isFetchable(String source)
      {
        return true;
      }

      @Override
      public SequenceI[] getSequences(List<DBRefEntry> refs, boolean dna)
      {
        return new SequenceI[] { pep1, pep2 };
      }
    };
    SequenceFetcherFactory.setSequenceFetcher(mockFetcher);

    /*
     * find UNIPROT xrefs for gene and transcripts
     * verify that
     * - the two proteins are retrieved but not duplicated
     * - mappings are built from transcript (CDS) to proteins
     * - no mappings from gene to proteins
     */
    SequenceI[] seqs = new SequenceI[] { gene, braf001, braf002 };
    AlignmentI al = new Alignment(seqs);
    Alignment xrefs = new CrossRef(seqs, al).findXrefSequences("UNIPROT",
            true);
    assertEquals(2, xrefs.getHeight());
    assertSame(pep1, xrefs.getSequenceAt(0));
    assertSame(pep2, xrefs.getSequenceAt(1));
  }

  /**
   * <pre>
   * Test that emulates this (real but simplified) case:
   * Alignment:          DBrefs
   *     UNIPROT|P0CE19  EMBL|J03321, EMBL|X06707, EMBL|M19487
   *     UNIPROT|P0CE20  EMBL|J03321, EMBL|X06707, EMBL|X07547
   * Find cross-references for EMBL. These are mocked here as
   *     EMBL|J03321     with mappings to P0CE18, P0CE19, P0CE20
   *     EMBL|X06707     with mappings to P0CE17, P0CE19, P0CE20
   *     EMBL|M19487     with mappings to P0CE19, Q46432
   *     EMBL|X07547     with mappings to P0CE20, B0BCM4
   * EMBL sequences are first 'fetched' (mocked here) for P0CE19.
   * The 3 EMBL sequences are added to the alignment dataset.
   * Their dbrefs to Uniprot products P0CE19 and P0CE20 should be matched in the
   * alignment dataset and updated to reference the original Uniprot sequences.
   * For the second Uniprot sequence, the J03321 and X06707 xrefs should be 
   * resolved from the dataset, and only the X07547 dbref fetched.
   * So the end state to verify is:
   * - 4 cross-ref sequences returned: J03321, X06707,  M19487, X07547
   * - P0CE19/20 dbrefs to EMBL sequences now have mappings
   * - J03321 dbrefs to P0CE19/20 mapped to original Uniprot sequences
   * - X06707 dbrefs to P0CE19/20 mapped to original Uniprot sequences
   * </pre>
   */
  @Test(groups = { "Functional_Failing" })
  public void testFindXrefSequences_uniprotEmblManyToMany()
  {
    /*
     * Uniprot sequences, both with xrefs to EMBL|J03321 
     * and EMBL|X07547
     */
    SequenceI p0ce19 = new Sequence("UNIPROT|P0CE19", "KPFG");
    p0ce19.addDBRef(new DBRefEntry("EMBL", "0", "J03321"));
    p0ce19.addDBRef(new DBRefEntry("EMBL", "0", "X06707"));
    p0ce19.addDBRef(new DBRefEntry("EMBL", "0", "M19487"));
    SequenceI p0ce20 = new Sequence("UNIPROT|P0CE20", "PFGK");
    p0ce20.addDBRef(new DBRefEntry("EMBL", "0", "J03321"));
    p0ce20.addDBRef(new DBRefEntry("EMBL", "0", "X06707"));
    p0ce20.addDBRef(new DBRefEntry("EMBL", "0", "X07547"));

    /*
     * EMBL sequences to be 'fetched', complete with dbrefs and mappings
     * to their protein products (CDS location  and translations  are provided
     * in  EMBL XML); these should be matched to, and replaced with,
     * the corresponding uniprot sequences after fetching
     */

    /*
     * J03321 with mappings to P0CE19 and P0CE20
     */
    final SequenceI j03321 = new Sequence("EMBL|J03321",
            "AAACCCTTTGGGAAAA");
    DBRefEntry dbref1 = new DBRefEntry("UNIPROT", "0", "P0CE19");
    MapList mapList = new MapList(new int[] { 1, 12 }, new int[] { 1, 4 },
            3, 1);
    Mapping map = new Mapping(new Sequence("UNIPROT|P0CE19", "KPFG"),
            mapList);
    // add a dbref to the mapped to sequence - should get copied to p0ce19
    map.getTo().addDBRef(new DBRefEntry("PIR", "0", "S01875"));
    dbref1.setMap(map);
    j03321.addDBRef(dbref1);
    DBRefEntry dbref2 = new DBRefEntry("UNIPROT", "0", "P0CE20");
    mapList = new MapList(new int[] { 4, 15 }, new int[] { 2, 5 }, 3, 1);
    dbref2.setMap(new Mapping(new Sequence("UNIPROT|P0CE20", "PFGK"),
            new MapList(mapList)));
    j03321.addDBRef(dbref2);

    /*
     * X06707 with mappings to P0CE19 and P0CE20
     */
    final SequenceI x06707 = new Sequence("EMBL|X06707", "atgAAACCCTTTGGG");
    DBRefEntry dbref3 = new DBRefEntry("UNIPROT", "0", "P0CE19");
    MapList map2 = new MapList(new int[] { 4, 15 }, new int[] { 1, 4 }, 3,
            1);
    dbref3.setMap(
            new Mapping(new Sequence("UNIPROT|P0CE19", "KPFG"), map2));
    x06707.addDBRef(dbref3);
    DBRefEntry dbref4 = new DBRefEntry("UNIPROT", "0", "P0CE20");
    MapList map3 = new MapList(new int[] { 4, 15 }, new int[] { 1, 4 }, 3,
            1);
    dbref4.setMap(
            new Mapping(new Sequence("UNIPROT|P0CE20", "PFGK"), map3));
    x06707.addDBRef(dbref4);

    /*
     * M19487 with mapping to P0CE19 and Q46432
     */
    final SequenceI m19487 = new Sequence("EMBL|M19487", "AAACCCTTTGGG");
    DBRefEntry dbref5 = new DBRefEntry("UNIPROT", "0", "P0CE19");
    dbref5.setMap(new Mapping(new Sequence("UNIPROT|P0CE19", "KPFG"),
            new MapList(mapList)));
    m19487.addDBRef(dbref5);
    DBRefEntry dbref6 = new DBRefEntry("UNIPROT", "0", "Q46432");
    dbref6.setMap(new Mapping(new Sequence("UNIPROT|Q46432", "KPFG"),
            new MapList(mapList)));
    m19487.addDBRef(dbref6);

    /*
     * X07547 with mapping to P0CE20 and B0BCM4
     */
    final SequenceI x07547 = new Sequence("EMBL|X07547", "cccAAACCCTTTGGG");
    DBRefEntry dbref7 = new DBRefEntry("UNIPROT", "0", "P0CE20");
    dbref7.setMap(new Mapping(new Sequence("UNIPROT|P0CE20", "PFGK"),
            new MapList(map2)));
    x07547.addDBRef(dbref7);
    DBRefEntry dbref8 = new DBRefEntry("UNIPROT", "0", "B0BCM4");
    dbref8.setMap(new Mapping(new Sequence("UNIPROT|B0BCM4", "KPFG"),
            new MapList(map2)));
    x07547.addDBRef(dbref8);

    /*
     * mock sequence fetcher to 'return' the EMBL sequences
     * TODO: Mockito would allow .thenReturn().thenReturn() here, 
     * and also capture and verification of the parameters
     * passed in calls to getSequences() - important to verify that
     * duplicate sequence fetches are not requested
     */
    SequenceFetcher mockFetcher = new SequenceFetcher()
    {
      int call = 0;

      @Override
      public boolean isFetchable(String source)
      {
        return true;
      }

      @Override
      public SequenceI[] getSequences(List<DBRefEntry> refs, boolean dna)
      {
        call++;
        if (call == 1)
        {
          assertEquals("Expected 3 embl seqs in first fetch", 3,
                  refs.size());
          return new SequenceI[] { j03321, x06707, m19487 };
        }
        else
        {
          assertEquals("Expected 1 embl seq in second fetch", 1,
                  refs.size());
          return new SequenceI[] { x07547 };
        }
      }
    };
    SequenceFetcherFactory.setSequenceFetcher(mockFetcher);

    /*
     * find EMBL xrefs for Uniprot seqs and verify that
     * - the EMBL xref'd sequences are retrieved without duplicates
     * - mappings are added to the Uniprot dbrefs
     * - mappings in the EMBL-to-Uniprot dbrefs are updated to the 
     *   alignment sequences
     * - dbrefs on the EMBL sequences are added to the original dbrefs
     */
    SequenceI[] seqs = new SequenceI[] { p0ce19, p0ce20 };
    AlignmentI al = new Alignment(seqs);
    Alignment xrefs = new CrossRef(seqs, al).findXrefSequences("EMBL",
            false);

    /*
     * verify retrieved sequences
     */
    assertNotNull(xrefs);
    assertEquals(4, xrefs.getHeight());
    assertSame(j03321, xrefs.getSequenceAt(0));
    assertSame(x06707, xrefs.getSequenceAt(1));
    assertSame(m19487, xrefs.getSequenceAt(2));
    assertSame(x07547, xrefs.getSequenceAt(3));

    /*
     * verify mappings added to Uniprot-to-EMBL dbrefs
     */
    Mapping mapping = p0ce19.getDBRefs().get(0).getMap();
    assertSame(j03321, mapping.getTo());
    mapping = p0ce19.getDBRefs().get(1).getMap();
    assertSame(x06707, mapping.getTo());
    mapping = p0ce20.getDBRefs().get(0).getMap();
    assertSame(j03321, mapping.getTo());
    mapping = p0ce20.getDBRefs().get(1).getMap();
    assertSame(x06707, mapping.getTo());

    /*
     * verify dbrefs on EMBL are mapped to alignment seqs
     */

    assertSame(p0ce19, j03321.getDBRefs().get(0).getMap().getTo());
    assertSame(p0ce20, j03321.getDBRefs().get(1).getMap().getTo());
    assertSame(p0ce19, x06707.getDBRefs().get(0).getMap().getTo());
    assertSame(p0ce20, x06707.getDBRefs().get(1).getMap().getTo());

    /*
     * verify new dbref on EMBL dbref mapping is copied to the
     * original Uniprot sequence
     */
    assertEquals(4, p0ce19.getDBRefs().size());
    assertEquals("PIR", p0ce19.getDBRefs().get(3).getSource());
    assertEquals("S01875", p0ce19.getDBRefs().get(3).getAccessionId());
  }

  @Test(groups = "Functional")
  public void testSameSequence()
  {
    assertTrue(CrossRef.sameSequence(null, null));
    SequenceI seq1 = new Sequence("seq1", "ABCDEF");
    assertFalse(CrossRef.sameSequence(seq1, null));
    assertFalse(CrossRef.sameSequence(null, seq1));
    assertTrue(CrossRef.sameSequence(seq1, new Sequence("seq2", "ABCDEF")));
    assertTrue(CrossRef.sameSequence(seq1, new Sequence("seq2", "abcdef")));
    assertFalse(
            CrossRef.sameSequence(seq1, new Sequence("seq2", "ABCDE-F")));
    assertFalse(CrossRef.sameSequence(seq1, new Sequence("seq2", "BCDEF")));
  }
}
