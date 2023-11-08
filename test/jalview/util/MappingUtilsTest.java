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
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.api.AlignViewportI;
import jalview.bin.Console;
import jalview.commands.EditCommand;
import jalview.commands.EditCommand.Action;
import jalview.commands.EditCommand.Edit;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.SearchResultMatchI;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignViewport;
import jalview.gui.JvOptionPane;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.FileFormatI;
import jalview.io.FormatAdapter;

public class MappingUtilsTest
{
  @BeforeClass(alwaysRun = true)
  public void setUp()
  {
    Console.initLogger();
  }

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  private AlignViewportI dnaView;

  private AlignViewportI proteinView;

  /**
   * Simple test of mapping with no intron involved.
   */
  @Test(groups = { "Functional" })
  public void testBuildSearchResults()
  {
    final Sequence seq1 = new Sequence("Seq1/5-10", "C-G-TA-GC");
    seq1.createDatasetSequence();

    final Sequence aseq1 = new Sequence("Seq1/12-13", "-P-R");
    aseq1.createDatasetSequence();

    /*
     * Map dna bases 5-10 to protein residues 12-13
     */
    AlignedCodonFrame acf = new AlignedCodonFrame();
    MapList map = new MapList(new int[] { 5, 10 }, new int[] { 12, 13 }, 3,
            1);
    acf.addMap(seq1.getDatasetSequence(), aseq1.getDatasetSequence(), map);
    List<AlignedCodonFrame> acfList = Arrays
            .asList(new AlignedCodonFrame[]
            { acf });

    /*
     * Check protein residue 12 maps to codon 5-7, 13 to codon 8-10
     */
    SearchResultsI sr = MappingUtils.buildSearchResults(aseq1, 12, acfList);
    assertEquals(1, sr.getResults().size());
    SearchResultMatchI m = sr.getResults().get(0);
    assertEquals(seq1.getDatasetSequence(), m.getSequence());
    assertEquals(5, m.getStart());
    assertEquals(7, m.getEnd());
    sr = MappingUtils.buildSearchResults(aseq1, 13, acfList);
    assertEquals(1, sr.getResults().size());
    m = sr.getResults().get(0);
    assertEquals(seq1.getDatasetSequence(), m.getSequence());
    assertEquals(8, m.getStart());
    assertEquals(10, m.getEnd());

    /*
     * Check inverse mappings, from codons 5-7, 8-10 to protein 12, 13
     */
    for (int i = 5; i < 11; i++)
    {
      sr = MappingUtils.buildSearchResults(seq1, i, acfList);
      assertEquals(1, sr.getResults().size());
      m = sr.getResults().get(0);
      assertEquals(aseq1.getDatasetSequence(), m.getSequence());
      int residue = i > 7 ? 13 : 12;
      assertEquals(residue, m.getStart());
      assertEquals(residue, m.getEnd());
    }
  }

  /**
   * Simple test of mapping with introns involved.
   */
  @Test(groups = { "Functional" })
  public void testBuildSearchResults_withIntron()
  {
    final Sequence seq1 = new Sequence("Seq1/5-17", "c-G-tAGa-GcAgCtt");
    seq1.createDatasetSequence();

    final Sequence aseq1 = new Sequence("Seq1/8-9", "-E-D");
    aseq1.createDatasetSequence();

    /*
     * Map dna bases [6, 8, 9], [11, 13, 115] to protein residues 8 and 9
     */
    AlignedCodonFrame acf = new AlignedCodonFrame();
    MapList map = new MapList(
            new int[]
            { 6, 6, 8, 9, 11, 11, 13, 13, 15, 15 }, new int[] { 8, 9 }, 3,
            1);
    acf.addMap(seq1.getDatasetSequence(), aseq1.getDatasetSequence(), map);
    List<AlignedCodonFrame> acfList = Arrays
            .asList(new AlignedCodonFrame[]
            { acf });

    /*
     * Check protein residue 8 maps to [6, 8, 9]
     */
    SearchResultsI sr = MappingUtils.buildSearchResults(aseq1, 8, acfList);
    assertEquals(2, sr.getResults().size());
    SearchResultMatchI m = sr.getResults().get(0);
    assertEquals(seq1.getDatasetSequence(), m.getSequence());
    assertEquals(6, m.getStart());
    assertEquals(6, m.getEnd());
    m = sr.getResults().get(1);
    assertEquals(seq1.getDatasetSequence(), m.getSequence());
    assertEquals(8, m.getStart());
    assertEquals(9, m.getEnd());

    /*
     * Check protein residue 9 maps to [11, 13, 15]
     */
    sr = MappingUtils.buildSearchResults(aseq1, 9, acfList);
    assertEquals(3, sr.getResults().size());
    m = sr.getResults().get(0);
    assertEquals(seq1.getDatasetSequence(), m.getSequence());
    assertEquals(11, m.getStart());
    assertEquals(11, m.getEnd());
    m = sr.getResults().get(1);
    assertEquals(seq1.getDatasetSequence(), m.getSequence());
    assertEquals(13, m.getStart());
    assertEquals(13, m.getEnd());
    m = sr.getResults().get(2);
    assertEquals(seq1.getDatasetSequence(), m.getSequence());
    assertEquals(15, m.getStart());
    assertEquals(15, m.getEnd());

    /*
     * Check inverse mappings, from codons to protein
     */
    for (int i = 5; i < 18; i++)
    {
      sr = MappingUtils.buildSearchResults(seq1, i, acfList);
      int residue = (i == 6 || i == 8 || i == 9) ? 8
              : (i == 11 || i == 13 || i == 15 ? 9 : 0);
      if (residue == 0)
      {
        assertEquals(0, sr.getResults().size());
        continue;
      }
      assertEquals(1, sr.getResults().size());
      m = sr.getResults().get(0);
      assertEquals(aseq1.getDatasetSequence(), m.getSequence());
      assertEquals(residue, m.getStart());
      assertEquals(residue, m.getEnd());
    }
  }

  /**
   * Test mapping a sequence group made of entire sequences.
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testMapSequenceGroup_sequences() throws IOException
  {
    /*
     * Set up dna and protein Seq1/2/3 with mappings (held on the protein
     * viewport).
     */
    AlignmentI cdna = loadAlignment(">Seq1\nACG\n>Seq2\nTGA\n>Seq3\nTAC\n",
            FileFormat.Fasta);
    cdna.setDataset(null);
    AlignmentI protein = loadAlignment(">Seq1\nK\n>Seq2\nL\n>Seq3\nQ\n",
            FileFormat.Fasta);
    protein.setDataset(null);
    AlignedCodonFrame acf = new AlignedCodonFrame();
    MapList map = new MapList(new int[] { 1, 3 }, new int[] { 1, 1 }, 3, 1);
    for (int seq = 0; seq < 3; seq++)
    {
      acf.addMap(cdna.getSequenceAt(seq).getDatasetSequence(),
              protein.getSequenceAt(seq).getDatasetSequence(), map);
    }
    List<AlignedCodonFrame> acfList = Arrays
            .asList(new AlignedCodonFrame[]
            { acf });

    AlignViewportI dnaView = new AlignViewport(cdna);
    AlignViewportI proteinView = new AlignViewport(protein);
    protein.setCodonFrames(acfList);

    /*
     * Select Seq1 and Seq3 in the protein
     */
    SequenceGroup sg = new SequenceGroup();
    sg.setColourText(true);
    sg.setIdColour(Color.GREEN);
    sg.setOutlineColour(Color.LIGHT_GRAY);
    sg.addSequence(protein.getSequenceAt(0), false);
    sg.addSequence(protein.getSequenceAt(2), false);
    sg.setEndRes(protein.getWidth() - 1);

    /*
     * Verify the mapped sequence group in dna
     */
    SequenceGroup mappedGroup = MappingUtils.mapSequenceGroup(sg,
            proteinView, dnaView);
    assertTrue(mappedGroup.getColourText());
    assertSame(sg.getIdColour(), mappedGroup.getIdColour());
    assertSame(sg.getOutlineColour(), mappedGroup.getOutlineColour());
    assertEquals(2, mappedGroup.getSequences().size());
    assertSame(cdna.getSequenceAt(0), mappedGroup.getSequences().get(0));
    assertSame(cdna.getSequenceAt(2), mappedGroup.getSequences().get(1));
    assertEquals(0, mappedGroup.getStartRes());
    assertEquals(2, mappedGroup.getEndRes()); // 3 columns (1 codon)

    /*
     * Verify mapping sequence group from dna to protein
     */
    sg.clear();
    sg.addSequence(cdna.getSequenceAt(1), false);
    sg.addSequence(cdna.getSequenceAt(0), false);
    sg.setStartRes(0);
    sg.setEndRes(2);
    mappedGroup = MappingUtils.mapSequenceGroup(sg, dnaView, proteinView);
    assertTrue(mappedGroup.getColourText());
    assertSame(sg.getIdColour(), mappedGroup.getIdColour());
    assertSame(sg.getOutlineColour(), mappedGroup.getOutlineColour());
    assertEquals(2, mappedGroup.getSequences().size());
    assertSame(protein.getSequenceAt(1), mappedGroup.getSequences().get(0));
    assertSame(protein.getSequenceAt(0), mappedGroup.getSequences().get(1));
    assertEquals(0, mappedGroup.getStartRes());
    assertEquals(0, mappedGroup.getEndRes());
  }

  /**
   * Helper method to load an alignment and ensure dataset sequences are set up.
   * 
   * @param data
   * @param format
   *          TODO
   * @return
   * @throws IOException
   */
  protected AlignmentI loadAlignment(final String data, FileFormatI format)
          throws IOException
  {
    AlignmentI a = new FormatAdapter().readFile(data, DataSourceType.PASTE,
            format);
    a.setDataset(null);
    return a;
  }

  /**
   * Test mapping a column selection in protein to its dna equivalent
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testMapColumnSelection_proteinToDna() throws IOException
  {
    setupMappedAlignments();

    ColumnSelection colsel = new ColumnSelection();
    HiddenColumns hidden = new HiddenColumns();

    /*
     * Column 0 in protein picks up Seq2/L, Seq3/G which map to cols 0-4 and 0-3
     * in dna respectively, overall 0-4
     */
    colsel.addElement(0);
    ColumnSelection cs = new ColumnSelection();
    HiddenColumns hs = new HiddenColumns();
    MappingUtils.mapColumnSelection(colsel, hidden, proteinView, dnaView,
            cs, hs);
    assertEquals("[0, 1, 2, 3, 4]", cs.getSelected().toString());

    /*
     * Column 1 in protein picks up Seq1/K which maps to cols 0-3 in dna
     */
    cs.clear();
    colsel.clear();
    colsel.addElement(1);
    MappingUtils.mapColumnSelection(colsel, hidden, proteinView, dnaView,
            cs, hs);
    assertEquals("[0, 1, 2, 3]", cs.getSelected().toString());

    /*
     * Column 2 in protein picks up gaps only - no mapping
     */
    cs.clear();
    colsel.clear();
    colsel.addElement(2);
    MappingUtils.mapColumnSelection(colsel, hidden, proteinView, dnaView,
            cs, hs);
    assertEquals("[]", cs.getSelected().toString());

    /*
     * Column 3 in protein picks up Seq1/P, Seq2/Q, Seq3/S which map to columns
     * 6-9, 6-10, 5-8 respectively, overall to 5-10
     */
    cs.clear();
    colsel.clear();
    colsel.addElement(3);
    MappingUtils.mapColumnSelection(colsel, hidden, proteinView, dnaView,
            cs, hs);
    assertEquals("[5, 6, 7, 8, 9, 10]", cs.getSelected().toString());

    /*
     * Combine selection of columns 1 and 3 to get a discontiguous mapped
     * selection
     */
    cs.clear();
    colsel.clear();
    colsel.addElement(1);
    colsel.addElement(3);
    MappingUtils.mapColumnSelection(colsel, hidden, proteinView, dnaView,
            cs, hs);
    assertEquals("[0, 1, 2, 3, 5, 6, 7, 8, 9, 10]",
            cs.getSelected().toString());
  }

  /**
   * Set up mappings for tests from 3 dna to 3 protein sequences. Sequences have
   * offset start positions for a more general test case.
   * 
   * @throws IOException
   */
  protected void setupMappedAlignments() throws IOException
  {
    /*
     * Map (upper-case = coding):
     * Seq1/10-18 AC-GctGtC-T to Seq1/40 -K-P
     * Seq2/20-27 Tc-GA-G-T-T to Seq2/20-27 L--Q
     * Seq3/30-38 TtTT-AaCGg- to Seq3/60-61\nG--S
     */
    AlignmentI cdna = loadAlignment(">Seq1/10-18\nAC-GctGtC-T\n"
            + ">Seq2/20-27\nTc-GA-G-T-Tc\n" + ">Seq3/30-38\nTtTT-AaCGg-\n",
            FileFormat.Fasta);
    cdna.setDataset(null);
    AlignmentI protein = loadAlignment(
            ">Seq1/40-41\n-K-P\n>Seq2/50-51\nL--Q\n>Seq3/60-61\nG--S\n",
            FileFormat.Fasta);
    protein.setDataset(null);

    // map first dna to first protein seq
    AlignedCodonFrame acf = new AlignedCodonFrame();
    MapList map = new MapList(new int[] { 10, 12, 15, 15, 17, 18 },
            new int[]
            { 40, 41 }, 3, 1);
    acf.addMap(cdna.getSequenceAt(0).getDatasetSequence(),
            protein.getSequenceAt(0).getDatasetSequence(), map);

    // map second dna to second protein seq
    map = new MapList(new int[] { 20, 20, 22, 23, 24, 26 },
            new int[]
            { 50, 51 }, 3, 1);
    acf.addMap(cdna.getSequenceAt(1).getDatasetSequence(),
            protein.getSequenceAt(1).getDatasetSequence(), map);

    // map third dna to third protein seq
    map = new MapList(new int[] { 30, 30, 32, 34, 36, 37 },
            new int[]
            { 60, 61 }, 3, 1);
    acf.addMap(cdna.getSequenceAt(2).getDatasetSequence(),
            protein.getSequenceAt(2).getDatasetSequence(), map);
    List<AlignedCodonFrame> acfList = Arrays
            .asList(new AlignedCodonFrame[]
            { acf });

    dnaView = new AlignViewport(cdna);
    proteinView = new AlignViewport(protein);
    protein.setCodonFrames(acfList);
  }

  /**
   * Test mapping a column selection in dna to its protein equivalent
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testMapColumnSelection_dnaToProtein() throws IOException
  {
    setupMappedAlignments();

    ColumnSelection colsel = new ColumnSelection();
    HiddenColumns hidden = new HiddenColumns();

    /*
     * Column 0 in dna picks up first bases which map to residue 1, columns 0-1
     * in protein.
     */
    ColumnSelection cs = new ColumnSelection();
    HiddenColumns hs = new HiddenColumns();
    colsel.addElement(0);
    MappingUtils.mapColumnSelection(colsel, hidden, dnaView, proteinView,
            cs, hs);
    assertEquals("[0, 1]", cs.getSelected().toString());

    /*
     * Columns 3-5 in dna map to the first residues in protein Seq1, Seq2, and
     * the first two in Seq3. Overall to columns 0, 1, 3 (col2 is all gaps).
     */
    colsel.addElement(3);
    colsel.addElement(4);
    colsel.addElement(5);
    cs.clear();
    MappingUtils.mapColumnSelection(colsel, hidden, dnaView, proteinView,
            cs, hs);
    assertEquals("[0, 1, 3]", cs.getSelected().toString());
  }

  @Test(groups = { "Functional" })
  public void testMapColumnSelection_null() throws IOException
  {
    setupMappedAlignments();
    ColumnSelection cs = new ColumnSelection();
    HiddenColumns hs = new HiddenColumns();
    MappingUtils.mapColumnSelection(null, null, dnaView, proteinView, cs,
            hs);
    assertTrue("mapped selection not empty", cs.getSelected().isEmpty());
  }

  /**
   * Tests for the method that converts a series of [start, end] ranges to
   * single positions
   */
  @Test(groups = { "Functional" })
  public void testFlattenRanges()
  {
    assertEquals("[1, 2, 3, 4]",
            Arrays.toString(MappingUtils.flattenRanges(new int[]
            { 1, 4 })));
    assertEquals("[1, 2, 3, 4]",
            Arrays.toString(MappingUtils.flattenRanges(new int[]
            { 1, 2, 3, 4 })));
    assertEquals("[1, 2, 3, 4]",
            Arrays.toString(MappingUtils.flattenRanges(new int[]
            { 1, 1, 2, 2, 3, 3, 4, 4 })));
    assertEquals("[1, 2, 3, 4, 7, 8, 9, 12]",
            Arrays.toString(MappingUtils.flattenRanges(new int[]
            { 1, 4, 7, 9, 12, 12 })));
    // trailing unpaired start position is ignored:
    assertEquals("[1, 2, 3, 4, 7, 8, 9, 12]",
            Arrays.toString(MappingUtils.flattenRanges(new int[]
            { 1, 4, 7, 9, 12, 12, 15 })));
  }

  /**
   * Test mapping a sequence group made of entire columns.
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testMapSequenceGroup_columns() throws IOException
  {
    /*
     * Set up dna and protein Seq1/2/3 with mappings (held on the protein
     * viewport).
     */
    AlignmentI cdna = loadAlignment(
            ">Seq1\nACGGCA\n>Seq2\nTGACAG\n>Seq3\nTACGTA\n",
            FileFormat.Fasta);
    cdna.setDataset(null);
    AlignmentI protein = loadAlignment(">Seq1\nKA\n>Seq2\nLQ\n>Seq3\nQV\n",
            FileFormat.Fasta);
    protein.setDataset(null);
    AlignedCodonFrame acf = new AlignedCodonFrame();
    MapList map = new MapList(new int[] { 1, 6 }, new int[] { 1, 2 }, 3, 1);
    for (int seq = 0; seq < 3; seq++)
    {
      acf.addMap(cdna.getSequenceAt(seq).getDatasetSequence(),
              protein.getSequenceAt(seq).getDatasetSequence(), map);
    }
    List<AlignedCodonFrame> acfList = Arrays
            .asList(new AlignedCodonFrame[]
            { acf });

    AlignViewportI dnaView = new AlignViewport(cdna);
    AlignViewportI proteinView = new AlignViewport(protein);
    protein.setCodonFrames(acfList);

    /*
     * Select all sequences, column 2 in the protein
     */
    SequenceGroup sg = new SequenceGroup();
    sg.setColourText(true);
    sg.setIdColour(Color.GREEN);
    sg.setOutlineColour(Color.LIGHT_GRAY);
    sg.addSequence(protein.getSequenceAt(0), false);
    sg.addSequence(protein.getSequenceAt(1), false);
    sg.addSequence(protein.getSequenceAt(2), false);
    sg.setStartRes(1);
    sg.setEndRes(1);

    /*
     * Verify the mapped sequence group in dna
     */
    SequenceGroup mappedGroup = MappingUtils.mapSequenceGroup(sg,
            proteinView, dnaView);
    assertTrue(mappedGroup.getColourText());
    assertSame(sg.getIdColour(), mappedGroup.getIdColour());
    assertSame(sg.getOutlineColour(), mappedGroup.getOutlineColour());
    assertEquals(3, mappedGroup.getSequences().size());
    assertSame(cdna.getSequenceAt(0), mappedGroup.getSequences().get(0));
    assertSame(cdna.getSequenceAt(1), mappedGroup.getSequences().get(1));
    assertSame(cdna.getSequenceAt(2), mappedGroup.getSequences().get(2));
    assertEquals(3, mappedGroup.getStartRes());
    assertEquals(5, mappedGroup.getEndRes());

    /*
     * Verify mapping sequence group from dna to protein
     */
    sg.clear();
    sg.addSequence(cdna.getSequenceAt(0), false);
    sg.addSequence(cdna.getSequenceAt(1), false);
    sg.addSequence(cdna.getSequenceAt(2), false);
    // select columns 2 and 3 in DNA which span protein columns 0 and 1
    sg.setStartRes(2);
    sg.setEndRes(3);
    mappedGroup = MappingUtils.mapSequenceGroup(sg, dnaView, proteinView);
    assertTrue(mappedGroup.getColourText());
    assertSame(sg.getIdColour(), mappedGroup.getIdColour());
    assertSame(sg.getOutlineColour(), mappedGroup.getOutlineColour());
    assertEquals(3, mappedGroup.getSequences().size());
    assertSame(protein.getSequenceAt(0), mappedGroup.getSequences().get(0));
    assertSame(protein.getSequenceAt(1), mappedGroup.getSequences().get(1));
    assertSame(protein.getSequenceAt(2), mappedGroup.getSequences().get(2));
    assertEquals(0, mappedGroup.getStartRes());
    assertEquals(1, mappedGroup.getEndRes());
  }

  /**
   * Test mapping a sequence group made of a sequences/columns region.
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testMapSequenceGroup_region() throws IOException
  {
    /*
     * Set up gapped dna and protein Seq1/2/3 with mappings (held on the protein
     * viewport).
     */
    AlignmentI cdna = loadAlignment(
            ">Seq1\nA-CG-GC--AT-CA\n>Seq2\n-TG-AC-AG-T-AT\n>Seq3\n-T--ACG-TAAT-G\n",
            FileFormat.Fasta);
    cdna.setDataset(null);
    AlignmentI protein = loadAlignment(
            ">Seq1\n-KA-S\n>Seq2\n--L-QY\n>Seq3\nQ-V-M\n",
            FileFormat.Fasta);
    protein.setDataset(null);
    AlignedCodonFrame acf = new AlignedCodonFrame();
    MapList map = new MapList(new int[] { 1, 9 }, new int[] { 1, 3 }, 3, 1);
    for (int seq = 0; seq < 3; seq++)
    {
      acf.addMap(cdna.getSequenceAt(seq).getDatasetSequence(),
              protein.getSequenceAt(seq).getDatasetSequence(), map);
    }
    List<AlignedCodonFrame> acfList = Arrays
            .asList(new AlignedCodonFrame[]
            { acf });

    AlignViewportI dnaView = new AlignViewport(cdna);
    AlignViewportI proteinView = new AlignViewport(protein);
    protein.setCodonFrames(acfList);

    /*
     * Select Seq1 and Seq2 in the protein, column 1 (K/-). Expect mapped
     * sequence group to cover Seq1, columns 0-3 (ACG). Because the selection
     * only includes a gap in Seq2 there is no mappable selection region in the
     * corresponding DNA.
     */
    SequenceGroup sg = new SequenceGroup();
    sg.setColourText(true);
    sg.setIdColour(Color.GREEN);
    sg.setOutlineColour(Color.LIGHT_GRAY);
    sg.addSequence(protein.getSequenceAt(0), false);
    sg.addSequence(protein.getSequenceAt(1), false);
    sg.setStartRes(1);
    sg.setEndRes(1);

    /*
     * Verify the mapped sequence group in dna
     */
    SequenceGroup mappedGroup = MappingUtils.mapSequenceGroup(sg,
            proteinView, dnaView);
    assertTrue(mappedGroup.getColourText());
    assertSame(sg.getIdColour(), mappedGroup.getIdColour());
    assertSame(sg.getOutlineColour(), mappedGroup.getOutlineColour());
    assertEquals(1, mappedGroup.getSequences().size());
    assertSame(cdna.getSequenceAt(0), mappedGroup.getSequences().get(0));
    // Seq2 in protein has a gap in column 1 - ignored
    // Seq1 has K which should map to columns 0-3 in Seq1
    assertEquals(0, mappedGroup.getStartRes());
    assertEquals(3, mappedGroup.getEndRes());

    /*
     * Now select cols 2-4 in protein. These cover Seq1:AS Seq2:LQ Seq3:VM which
     * extend over DNA columns 3-12, 1-7, 6-13 respectively, or 1-13 overall.
     */
    sg.setStartRes(2);
    sg.setEndRes(4);
    mappedGroup = MappingUtils.mapSequenceGroup(sg, proteinView, dnaView);
    assertEquals(1, mappedGroup.getStartRes());
    assertEquals(13, mappedGroup.getEndRes());

    /*
     * Verify mapping sequence group from dna to protein
     */
    sg.clear();
    sg.addSequence(cdna.getSequenceAt(0), false);

    // select columns 4,5 - includes Seq1:codon2 (A) only
    sg.setStartRes(4);
    sg.setEndRes(5);
    mappedGroup = MappingUtils.mapSequenceGroup(sg, dnaView, proteinView);
    assertEquals(2, mappedGroup.getStartRes());
    assertEquals(2, mappedGroup.getEndRes());

    // add Seq2 to dna selection cols 4-5 include codons 1 and 2 (LQ)
    sg.addSequence(cdna.getSequenceAt(1), false);
    mappedGroup = MappingUtils.mapSequenceGroup(sg, dnaView, proteinView);
    assertEquals(2, mappedGroup.getStartRes());
    assertEquals(4, mappedGroup.getEndRes());

    // add Seq3 to dna selection cols 4-5 include codon 1 (Q)
    sg.addSequence(cdna.getSequenceAt(2), false);
    mappedGroup = MappingUtils.mapSequenceGroup(sg, dnaView, proteinView);
    assertEquals(0, mappedGroup.getStartRes());
    assertEquals(4, mappedGroup.getEndRes());
  }

  @Test(groups = { "Functional" })
  public void testFindMappingsForSequence()
  {
    SequenceI seq1 = new Sequence("Seq1", "ABC");
    SequenceI seq2 = new Sequence("Seq2", "ABC");
    SequenceI seq3 = new Sequence("Seq3", "ABC");
    SequenceI seq4 = new Sequence("Seq4", "ABC");
    seq1.createDatasetSequence();
    seq2.createDatasetSequence();
    seq3.createDatasetSequence();
    seq4.createDatasetSequence();

    /*
     * Create mappings from seq1 to seq2, seq2 to seq1, seq3 to seq1
     */
    AlignedCodonFrame acf1 = new AlignedCodonFrame();
    MapList map = new MapList(new int[] { 1, 3 }, new int[] { 1, 3 }, 1, 1);
    acf1.addMap(seq1.getDatasetSequence(), seq2.getDatasetSequence(), map);
    AlignedCodonFrame acf2 = new AlignedCodonFrame();
    acf2.addMap(seq2.getDatasetSequence(), seq1.getDatasetSequence(), map);
    AlignedCodonFrame acf3 = new AlignedCodonFrame();
    acf3.addMap(seq3.getDatasetSequence(), seq1.getDatasetSequence(), map);

    List<AlignedCodonFrame> mappings = new ArrayList<>();
    mappings.add(acf1);
    mappings.add(acf2);
    mappings.add(acf3);

    /*
     * Seq1 has three mappings
     */
    List<AlignedCodonFrame> result = MappingUtils
            .findMappingsForSequence(seq1, mappings);
    assertEquals(3, result.size());
    assertTrue(result.contains(acf1));
    assertTrue(result.contains(acf2));
    assertTrue(result.contains(acf3));

    /*
     * Seq2 has two mappings
     */
    result = MappingUtils.findMappingsForSequence(seq2, mappings);
    assertEquals(2, result.size());
    assertTrue(result.contains(acf1));
    assertTrue(result.contains(acf2));

    /*
     * Seq3 has one mapping
     */
    result = MappingUtils.findMappingsForSequence(seq3, mappings);
    assertEquals(1, result.size());
    assertTrue(result.contains(acf3));

    /*
     * Seq4 has no mappings
     */
    result = MappingUtils.findMappingsForSequence(seq4, mappings);
    assertEquals(0, result.size());

    result = MappingUtils.findMappingsForSequence(null, mappings);
    assertEquals(0, result.size());

    result = MappingUtils.findMappingsForSequence(seq1, null);
    assertEquals(0, result.size());

    result = MappingUtils.findMappingsForSequence(null, null);
    assertEquals(0, result.size());
  }

  /**
   * just like the one above, but this time, we provide a set of sequences to
   * subselect the mapping search
   */
  @Test(groups = { "Functional" })
  public void testFindMappingsForSequenceAndOthers()
  {
    SequenceI seq1 = new Sequence("Seq1", "ABC");
    SequenceI seq2 = new Sequence("Seq2", "ABC");
    SequenceI seq3 = new Sequence("Seq3", "ABC");
    SequenceI seq4 = new Sequence("Seq4", "ABC");
    seq1.createDatasetSequence();
    seq2.createDatasetSequence();
    seq3.createDatasetSequence();
    seq4.createDatasetSequence();

    /*
     * Create mappings from seq1 to seq2, seq2 to seq1, seq3 to seq1, seq3 to seq4
     */
    AlignedCodonFrame acf1 = new AlignedCodonFrame();
    MapList map = new MapList(new int[] { 1, 3 }, new int[] { 1, 3 }, 1, 1);
    acf1.addMap(seq1.getDatasetSequence(), seq2.getDatasetSequence(), map);
    AlignedCodonFrame acf2 = new AlignedCodonFrame();
    acf2.addMap(seq2.getDatasetSequence(), seq1.getDatasetSequence(), map);
    AlignedCodonFrame acf3 = new AlignedCodonFrame();
    acf3.addMap(seq3.getDatasetSequence(), seq1.getDatasetSequence(), map);
    AlignedCodonFrame acf4 = new AlignedCodonFrame();
    acf4.addMap(seq3.getDatasetSequence(), seq4.getDatasetSequence(), map);

    List<AlignedCodonFrame> mappings = new ArrayList<>();
    mappings.add(acf1);
    mappings.add(acf2);
    mappings.add(acf3);
    mappings.add(acf4);

    /*
     * test for null args
     */
    List<AlignedCodonFrame> result = MappingUtils
            .findMappingsForSequenceAndOthers(null, mappings,
                    Arrays.asList(new SequenceI[]
                    { seq1, seq2 }));
    assertTrue(result.isEmpty());

    result = MappingUtils.findMappingsForSequenceAndOthers(seq1, null,
            Arrays.asList(new SequenceI[]
            { seq1, seq2 }));
    assertTrue(result.isEmpty());

    /*
     * Seq1 has three mappings, but filter argument will only accept
     * those to seq2
     */
    result = MappingUtils.findMappingsForSequenceAndOthers(seq1, mappings,
            Arrays.asList(new SequenceI[]
            { seq1, seq2, seq1.getDatasetSequence() }));
    assertEquals(2, result.size());
    assertTrue(result.contains(acf1));
    assertTrue(result.contains(acf2));
    assertFalse("Did not expect to find mapping acf3 - subselect failed",
            result.contains(acf3));
    assertFalse(
            "Did not expect to find mapping acf4 - doesn't involve sequence",
            result.contains(acf4));

    /*
     * and verify the no filter case
     */
    result = MappingUtils.findMappingsForSequenceAndOthers(seq1, mappings,
            null);
    assertEquals(3, result.size());
    assertTrue(result.contains(acf1));
    assertTrue(result.contains(acf2));
    assertTrue(result.contains(acf3));
  }

  @Test(groups = { "Functional" })
  public void testMapEditCommand()
  {
    SequenceI dna = new Sequence("Seq1", "---ACG---GCATCA", 8, 16);
    SequenceI protein = new Sequence("Seq2", "-T-AS", 5, 7);
    dna.createDatasetSequence();
    protein.createDatasetSequence();
    AlignedCodonFrame acf = new AlignedCodonFrame();
    MapList map = new MapList(new int[] { 8, 16 }, new int[] { 5, 7 }, 3,
            1);
    acf.addMap(dna.getDatasetSequence(), protein.getDatasetSequence(), map);
    List<AlignedCodonFrame> mappings = new ArrayList<>();
    mappings.add(acf);

    AlignmentI prot = new Alignment(new SequenceI[] { protein });
    prot.setCodonFrames(mappings);
    AlignmentI nuc = new Alignment(new SequenceI[] { dna });

    /*
     * construct and perform the edit command to turn "-T-AS" in to "-T-A--S"
     * i.e. insert two gaps at column 4
     */
    EditCommand ec = new EditCommand();
    final Edit edit = ec.new Edit(Action.INSERT_GAP,
            new SequenceI[]
            { protein }, 4, 2, '-');
    ec.appendEdit(edit, prot, true, null);

    /*
     * the mapped edit command should be to insert 6 gaps before base 4 in the
     * nucleotide sequence, which corresponds to aligned column 12 in the dna
     */
    EditCommand mappedEdit = MappingUtils.mapEditCommand(ec, false, nuc,
            '-', mappings);
    assertEquals(1, mappedEdit.getEdits().size());
    Edit e = mappedEdit.getEdits().get(0);
    assertEquals(1, e.getSequences().length);
    assertEquals(dna, e.getSequences()[0]);
    assertEquals(12, e.getPosition());
    assertEquals(6, e.getNumber());
  }

  /**
   * Tests for the method that converts a series of [start, end] ranges to
   * single positions, where the mapping is to a reverse strand i.e. start is
   * greater than end point mapped to
   */
  @Test(groups = { "Functional" })
  public void testFlattenRanges_reverseStrand()
  {
    assertEquals("[4, 3, 2, 1]",
            Arrays.toString(MappingUtils.flattenRanges(new int[]
            { 4, 1 })));
    assertEquals("[4, 3, 2, 1]",
            Arrays.toString(MappingUtils.flattenRanges(new int[]
            { 4, 3, 2, 1 })));
    assertEquals("[4, 3, 2, 1]",
            Arrays.toString(MappingUtils.flattenRanges(new int[]
            { 4, 4, 3, 3, 2, 2, 1, 1 })));
    assertEquals("[12, 9, 8, 7, 4, 3, 2, 1]",
            Arrays.toString(MappingUtils.flattenRanges(new int[]
            { 12, 12, 9, 7, 4, 1 })));
    // forwards and backwards anyone?
    assertEquals("[4, 5, 6, 3, 2, 1]",
            Arrays.toString(MappingUtils.flattenRanges(new int[]
            { 4, 6, 3, 1 })));
    // backwards and forwards
    assertEquals("[3, 2, 1, 4, 5, 6]",
            Arrays.toString(MappingUtils.flattenRanges(new int[]
            { 3, 1, 4, 6 })));
    // trailing unpaired start position is ignored:
    assertEquals("[12, 9, 8, 7, 4, 3, 2]",
            Arrays.toString(MappingUtils.flattenRanges(new int[]
            { 12, 12, 9, 7, 4, 2, 1 })));
  }

  /**
   * Test mapping a column selection including hidden columns
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testMapColumnSelection_hiddenColumns() throws IOException
  {
    setupMappedAlignments();

    ColumnSelection proteinSelection = new ColumnSelection();
    HiddenColumns hiddenCols = new HiddenColumns();

    /*
     * Column 0 in protein picks up Seq2/L, Seq3/G which map to cols 0-4 and 0-3
     * in dna respectively, overall 0-4
     */
    proteinSelection.hideSelectedColumns(0, hiddenCols);
    ColumnSelection dnaSelection = new ColumnSelection();
    HiddenColumns dnaHidden = new HiddenColumns();
    MappingUtils.mapColumnSelection(proteinSelection, hiddenCols,
            proteinView, dnaView, dnaSelection, dnaHidden);
    assertEquals("[]", dnaSelection.getSelected().toString());
    Iterator<int[]> regions = dnaHidden.iterator();
    assertEquals(1, dnaHidden.getNumberOfRegions());
    assertEquals("[0, 4]", Arrays.toString(regions.next()));

    /*
     * Column 1 in protein picks up Seq1/K which maps to cols 0-3 in dna
     */
    dnaSelection = new ColumnSelection();
    dnaHidden = new HiddenColumns();
    hiddenCols.revealAllHiddenColumns(proteinSelection);
    // the unhidden columns are now marked selected!
    assertEquals("[0]", proteinSelection.getSelected().toString());
    // deselect these or hideColumns will be expanded to include 0
    proteinSelection.clear();
    proteinSelection.hideSelectedColumns(1, hiddenCols);
    MappingUtils.mapColumnSelection(proteinSelection, hiddenCols,
            proteinView, dnaView, dnaSelection, dnaHidden);
    regions = dnaHidden.iterator();
    assertEquals(1, dnaHidden.getNumberOfRegions());
    assertEquals("[0, 3]", Arrays.toString(regions.next()));

    /*
     * Column 2 in protein picks up gaps only - no mapping
     */
    dnaSelection = new ColumnSelection();
    dnaHidden = new HiddenColumns();
    hiddenCols.revealAllHiddenColumns(proteinSelection);
    proteinSelection.clear();
    proteinSelection.hideSelectedColumns(2, hiddenCols);
    MappingUtils.mapColumnSelection(proteinSelection, hiddenCols,
            proteinView, dnaView, dnaSelection, dnaHidden);
    assertEquals(0, dnaHidden.getNumberOfRegions());

    /*
     * Column 3 in protein picks up Seq1/P, Seq2/Q, Seq3/S which map to columns
     * 6-9, 6-10, 5-8 respectively, overall to 5-10
     */
    dnaSelection = new ColumnSelection();
    dnaHidden = new HiddenColumns();
    hiddenCols.revealAllHiddenColumns(proteinSelection);
    proteinSelection.clear();
    proteinSelection.hideSelectedColumns(3, hiddenCols); // 5-10 hidden in dna
    proteinSelection.addElement(1); // 0-3 selected in dna
    MappingUtils.mapColumnSelection(proteinSelection, hiddenCols,
            proteinView, dnaView, dnaSelection, dnaHidden);
    assertEquals("[0, 1, 2, 3]", dnaSelection.getSelected().toString());
    regions = dnaHidden.iterator();
    assertEquals(1, dnaHidden.getNumberOfRegions());
    assertEquals("[5, 10]", Arrays.toString(regions.next()));

    /*
     * Combine hiding columns 1 and 3 to get discontiguous hidden columns
     */
    dnaSelection = new ColumnSelection();
    dnaHidden = new HiddenColumns();
    hiddenCols.revealAllHiddenColumns(proteinSelection);
    proteinSelection.clear();
    proteinSelection.hideSelectedColumns(1, hiddenCols);
    proteinSelection.hideSelectedColumns(3, hiddenCols);
    MappingUtils.mapColumnSelection(proteinSelection, hiddenCols,
            proteinView, dnaView, dnaSelection, dnaHidden);
    regions = dnaHidden.iterator();
    assertEquals(2, dnaHidden.getNumberOfRegions());
    assertEquals("[0, 3]", Arrays.toString(regions.next()));
    assertEquals("[5, 10]", Arrays.toString(regions.next()));
  }

  @Test(groups = { "Functional" })
  public void testGetLength()
  {
    assertEquals(0, MappingUtils.getLength(null));

    /*
     * [start, end] ranges
     */
    List<int[]> ranges = new ArrayList<>();
    assertEquals(0, MappingUtils.getLength(ranges));
    ranges.add(new int[] { 1, 1 });
    assertEquals(1, MappingUtils.getLength(ranges));
    ranges.add(new int[] { 2, 10 });
    assertEquals(10, MappingUtils.getLength(ranges));
    ranges.add(new int[] { 20, 10 });
    assertEquals(21, MappingUtils.getLength(ranges));

    /*
     * [start, end, start, end...] ranges
     */
    ranges.clear();
    ranges.add(new int[] { 1, 5, 8, 4 });
    ranges.add(new int[] { 8, 2 });
    ranges.add(new int[] { 12, 12 });
    assertEquals(18, MappingUtils.getLength(ranges));
  }

  @Test(groups = { "Functional" })
  public void testContains()
  {
    assertFalse(MappingUtils.contains(null, 1));
    List<int[]> ranges = new ArrayList<>();
    assertFalse(MappingUtils.contains(ranges, 1));

    ranges.add(new int[] { 1, 4 });
    ranges.add(new int[] { 6, 6 });
    ranges.add(new int[] { 8, 10 });
    ranges.add(new int[] { 30, 20 });
    ranges.add(new int[] { -16, -44 });

    assertFalse(MappingUtils.contains(ranges, 0));
    assertTrue(MappingUtils.contains(ranges, 1));
    assertTrue(MappingUtils.contains(ranges, 2));
    assertTrue(MappingUtils.contains(ranges, 3));
    assertTrue(MappingUtils.contains(ranges, 4));
    assertFalse(MappingUtils.contains(ranges, 5));

    assertTrue(MappingUtils.contains(ranges, 6));
    assertFalse(MappingUtils.contains(ranges, 7));

    assertTrue(MappingUtils.contains(ranges, 8));
    assertTrue(MappingUtils.contains(ranges, 9));
    assertTrue(MappingUtils.contains(ranges, 10));

    assertFalse(MappingUtils.contains(ranges, 31));
    assertTrue(MappingUtils.contains(ranges, 30));
    assertTrue(MappingUtils.contains(ranges, 29));
    assertTrue(MappingUtils.contains(ranges, 20));
    assertFalse(MappingUtils.contains(ranges, 19));

    assertFalse(MappingUtils.contains(ranges, -15));
    assertTrue(MappingUtils.contains(ranges, -16));
    assertTrue(MappingUtils.contains(ranges, -44));
    assertFalse(MappingUtils.contains(ranges, -45));
  }

  /**
   * Test the method that drops positions from the start of a mapped range
   */
  @Test(groups = "Functional")
  public void testRemoveStartPositions()
  {
    int[] ranges = new int[] { 1, 10 };
    int[] adjusted = MappingUtils.removeStartPositions(0, ranges);
    assertEquals("[1, 10]", Arrays.toString(adjusted));

    adjusted = MappingUtils.removeStartPositions(1, ranges);
    assertEquals("[2, 10]", Arrays.toString(adjusted));
    assertEquals("[1, 10]", Arrays.toString(ranges));

    ranges = adjusted;
    adjusted = MappingUtils.removeStartPositions(1, ranges);
    assertEquals("[3, 10]", Arrays.toString(adjusted));
    assertEquals("[2, 10]", Arrays.toString(ranges));

    ranges = new int[] { 2, 3, 10, 12 };
    adjusted = MappingUtils.removeStartPositions(1, ranges);
    assertEquals("[3, 3, 10, 12]", Arrays.toString(adjusted));
    assertEquals("[2, 3, 10, 12]", Arrays.toString(ranges));

    ranges = new int[] { 2, 2, 8, 12 };
    adjusted = MappingUtils.removeStartPositions(1, ranges);
    assertEquals("[8, 12]", Arrays.toString(adjusted));
    assertEquals("[2, 2, 8, 12]", Arrays.toString(ranges));

    ranges = new int[] { 2, 2, 8, 12 };
    adjusted = MappingUtils.removeStartPositions(2, ranges);
    assertEquals("[9, 12]", Arrays.toString(adjusted));
    assertEquals("[2, 2, 8, 12]", Arrays.toString(ranges));

    ranges = new int[] { 2, 2, 4, 4, 9, 12 };
    adjusted = MappingUtils.removeStartPositions(1, ranges);
    assertEquals("[4, 4, 9, 12]", Arrays.toString(adjusted));
    assertEquals("[2, 2, 4, 4, 9, 12]", Arrays.toString(ranges));

    ranges = new int[] { 2, 2, 4, 4, 9, 12 };
    adjusted = MappingUtils.removeStartPositions(2, ranges);
    assertEquals("[9, 12]", Arrays.toString(adjusted));
    assertEquals("[2, 2, 4, 4, 9, 12]", Arrays.toString(ranges));

    ranges = new int[] { 2, 3, 9, 12 };
    adjusted = MappingUtils.removeStartPositions(3, ranges);
    assertEquals("[10, 12]", Arrays.toString(adjusted));
    assertEquals("[2, 3, 9, 12]", Arrays.toString(ranges));
  }

  /**
   * Test the method that drops positions from the start of a mapped range, on
   * the reverse strand
   */
  @Test(groups = "Functional")
  public void testRemoveStartPositions_reverseStrand()
  {
    int[] ranges = new int[] { 10, 1 };
    int[] adjusted = MappingUtils.removeStartPositions(0, ranges);
    assertEquals("[10, 1]", Arrays.toString(adjusted));
    assertEquals("[10, 1]", Arrays.toString(ranges));

    ranges = adjusted;
    adjusted = MappingUtils.removeStartPositions(1, ranges);
    assertEquals("[9, 1]", Arrays.toString(adjusted));
    assertEquals("[10, 1]", Arrays.toString(ranges));

    ranges = adjusted;
    adjusted = MappingUtils.removeStartPositions(1, ranges);
    assertEquals("[8, 1]", Arrays.toString(adjusted));
    assertEquals("[9, 1]", Arrays.toString(ranges));

    ranges = new int[] { 12, 11, 9, 6 };
    adjusted = MappingUtils.removeStartPositions(1, ranges);
    assertEquals("[11, 11, 9, 6]", Arrays.toString(adjusted));
    assertEquals("[12, 11, 9, 6]", Arrays.toString(ranges));

    ranges = new int[] { 12, 12, 8, 4 };
    adjusted = MappingUtils.removeStartPositions(1, ranges);
    assertEquals("[8, 4]", Arrays.toString(adjusted));
    assertEquals("[12, 12, 8, 4]", Arrays.toString(ranges));

    ranges = new int[] { 12, 12, 8, 4 };
    adjusted = MappingUtils.removeStartPositions(2, ranges);
    assertEquals("[7, 4]", Arrays.toString(adjusted));
    assertEquals("[12, 12, 8, 4]", Arrays.toString(ranges));

    ranges = new int[] { 12, 12, 10, 10, 8, 4 };
    adjusted = MappingUtils.removeStartPositions(1, ranges);
    assertEquals("[10, 10, 8, 4]", Arrays.toString(adjusted));
    assertEquals("[12, 12, 10, 10, 8, 4]", Arrays.toString(ranges));

    ranges = new int[] { 12, 12, 10, 10, 8, 4 };
    adjusted = MappingUtils.removeStartPositions(2, ranges);
    assertEquals("[8, 4]", Arrays.toString(adjusted));
    assertEquals("[12, 12, 10, 10, 8, 4]", Arrays.toString(ranges));

    ranges = new int[] { 12, 11, 8, 4 };
    adjusted = MappingUtils.removeStartPositions(3, ranges);
    assertEquals("[7, 4]", Arrays.toString(adjusted));
    assertEquals("[12, 11, 8, 4]", Arrays.toString(ranges));
  }

  @Test(groups = { "Functional" })
  public void testRangeContains()
  {
    /*
     * both forward ranges
     */
    assertTrue(
            MappingUtils.rangeContains(new int[]
            { 1, 10 }, new int[] { 1, 10 }));
    assertTrue(
            MappingUtils.rangeContains(new int[]
            { 1, 10 }, new int[] { 2, 10 }));
    assertTrue(
            MappingUtils.rangeContains(new int[]
            { 1, 10 }, new int[] { 1, 9 }));
    assertTrue(
            MappingUtils.rangeContains(new int[]
            { 1, 10 }, new int[] { 4, 5 }));
    assertFalse(
            MappingUtils.rangeContains(new int[]
            { 1, 10 }, new int[] { 0, 9 }));
    assertFalse(
            MappingUtils.rangeContains(new int[]
            { 1, 10 }, new int[] { -10, -9 }));
    assertFalse(
            MappingUtils.rangeContains(new int[]
            { 1, 10 }, new int[] { 1, 11 }));
    assertFalse(
            MappingUtils.rangeContains(new int[]
            { 1, 10 }, new int[] { 11, 12 }));

    /*
     * forward range, reverse query
     */
    assertTrue(
            MappingUtils.rangeContains(new int[]
            { 1, 10 }, new int[] { 10, 1 }));
    assertTrue(
            MappingUtils.rangeContains(new int[]
            { 1, 10 }, new int[] { 9, 1 }));
    assertTrue(
            MappingUtils.rangeContains(new int[]
            { 1, 10 }, new int[] { 10, 2 }));
    assertTrue(
            MappingUtils.rangeContains(new int[]
            { 1, 10 }, new int[] { 5, 5 }));
    assertFalse(
            MappingUtils.rangeContains(new int[]
            { 1, 10 }, new int[] { 11, 1 }));
    assertFalse(
            MappingUtils.rangeContains(new int[]
            { 1, 10 }, new int[] { 10, 0 }));

    /*
     * reverse range, forward query
     */
    assertTrue(
            MappingUtils.rangeContains(new int[]
            { 10, 1 }, new int[] { 1, 10 }));
    assertTrue(
            MappingUtils.rangeContains(new int[]
            { 10, 1 }, new int[] { 1, 9 }));
    assertTrue(
            MappingUtils.rangeContains(new int[]
            { 10, 1 }, new int[] { 2, 10 }));
    assertTrue(
            MappingUtils.rangeContains(new int[]
            { 10, 1 }, new int[] { 6, 6 }));
    assertFalse(
            MappingUtils.rangeContains(new int[]
            { 10, 1 }, new int[] { 6, 11 }));
    assertFalse(
            MappingUtils.rangeContains(new int[]
            { 10, 1 }, new int[] { 11, 20 }));
    assertFalse(
            MappingUtils.rangeContains(new int[]
            { 10, 1 }, new int[] { -3, -2 }));

    /*
     * both reverse
     */
    assertTrue(
            MappingUtils.rangeContains(new int[]
            { 10, 1 }, new int[] { 10, 1 }));
    assertTrue(
            MappingUtils.rangeContains(new int[]
            { 10, 1 }, new int[] { 9, 1 }));
    assertTrue(
            MappingUtils.rangeContains(new int[]
            { 10, 1 }, new int[] { 10, 2 }));
    assertTrue(
            MappingUtils.rangeContains(new int[]
            { 10, 1 }, new int[] { 3, 3 }));
    assertFalse(
            MappingUtils.rangeContains(new int[]
            { 10, 1 }, new int[] { 11, 1 }));
    assertFalse(
            MappingUtils.rangeContains(new int[]
            { 10, 1 }, new int[] { 10, 0 }));
    assertFalse(
            MappingUtils.rangeContains(new int[]
            { 10, 1 }, new int[] { 12, 11 }));
    assertFalse(
            MappingUtils.rangeContains(new int[]
            { 10, 1 }, new int[] { -5, -8 }));

    /*
     * bad arguments
     */
    assertFalse(
            MappingUtils.rangeContains(new int[]
            { 1, 10, 12 }, new int[] { 1, 10 }));
    assertFalse(
            MappingUtils.rangeContains(new int[]
            { 1, 10 }, new int[] { 1 }));
    assertFalse(MappingUtils.rangeContains(new int[] { 1, 10 }, null));
    assertFalse(MappingUtils.rangeContains(null, new int[] { 1, 10 }));
  }

  @Test(groups = "Functional")
  public void testRemoveEndPositions()
  {
    List<int[]> ranges = new ArrayList<>();

    /*
     * case 1: truncate last range
     */
    ranges.add(new int[] { 1, 10 });
    ranges.add(new int[] { 20, 30 });
    MappingUtils.removeEndPositions(5, ranges);
    assertEquals(2, ranges.size());
    assertEquals(25, ranges.get(1)[1]);

    /*
     * case 2: remove last range
     */
    ranges.clear();
    ranges.add(new int[] { 1, 10 });
    ranges.add(new int[] { 20, 22 });
    MappingUtils.removeEndPositions(3, ranges);
    assertEquals(1, ranges.size());
    assertEquals(10, ranges.get(0)[1]);

    /*
     * case 3: truncate penultimate range
     */
    ranges.clear();
    ranges.add(new int[] { 1, 10 });
    ranges.add(new int[] { 20, 21 });
    MappingUtils.removeEndPositions(3, ranges);
    assertEquals(1, ranges.size());
    assertEquals(9, ranges.get(0)[1]);

    /*
     * case 4: remove last two ranges
     */
    ranges.clear();
    ranges.add(new int[] { 1, 10 });
    ranges.add(new int[] { 20, 20 });
    ranges.add(new int[] { 30, 30 });
    MappingUtils.removeEndPositions(3, ranges);
    assertEquals(1, ranges.size());
    assertEquals(9, ranges.get(0)[1]);
  }

  @Test(groups = "Functional")
  public void testFindOverlap()
  {
    List<int[]> ranges = new ArrayList<>();
    ranges.add(new int[] { 4, 8 });
    ranges.add(new int[] { 10, 12 });
    ranges.add(new int[] { 16, 19 });

    int[] overlap = MappingUtils.findOverlap(ranges, 5, 13);
    assertArrayEquals(overlap, new int[] { 5, 12 });
    overlap = MappingUtils.findOverlap(ranges, -100, 100);
    assertArrayEquals(overlap, new int[] { 4, 19 });
    overlap = MappingUtils.findOverlap(ranges, 7, 17);
    assertArrayEquals(overlap, new int[] { 7, 17 });
    overlap = MappingUtils.findOverlap(ranges, 13, 15);
    assertNull(overlap);
  }

  /**
   * Test mapping a sequence group where sequences in and outside the group
   * share a dataset sequence (e.g. alternative CDS for the same gene)
   * <p>
   * This scenario doesn't arise after JAL-3763 changes, but test left as still
   * valid
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testMapSequenceGroup_sharedDataset() throws IOException
  {
    /*
     * Set up dna and protein Seq1/2/3 with mappings (held on the protein
     * viewport). CDS sequences share the same 'gene' dataset sequence.
     */
    SequenceI dna = new Sequence("dna", "aaatttgggcccaaatttgggccc");
    SequenceI cds1 = new Sequence("cds1/1-6", "aaattt");
    SequenceI cds2 = new Sequence("cds1/4-9", "tttggg");
    SequenceI cds3 = new Sequence("cds1/19-24", "gggccc");

    cds1.setDatasetSequence(dna);
    cds2.setDatasetSequence(dna);
    cds3.setDatasetSequence(dna);

    SequenceI pep1 = new Sequence("pep1", "KF");
    SequenceI pep2 = new Sequence("pep2", "FG");
    SequenceI pep3 = new Sequence("pep3", "GP");
    pep1.createDatasetSequence();
    pep2.createDatasetSequence();
    pep3.createDatasetSequence();

    /*
     * add mappings from coding positions of dna to respective peptides
     */
    AlignedCodonFrame acf = new AlignedCodonFrame();
    acf.addMap(dna, pep1,
            new MapList(new int[]
            { 1, 6 }, new int[] { 1, 2 }, 3, 1));
    acf.addMap(dna, pep2,
            new MapList(new int[]
            { 4, 9 }, new int[] { 1, 2 }, 3, 1));
    acf.addMap(dna, pep3,
            new MapList(new int[]
            { 19, 24 }, new int[] { 1, 2 }, 3, 1));

    List<AlignedCodonFrame> acfList = Arrays
            .asList(new AlignedCodonFrame[]
            { acf });

    AlignmentI cdna = new Alignment(new SequenceI[] { cds1, cds2, cds3 });
    AlignmentI protein = new Alignment(
            new SequenceI[]
            { pep1, pep2, pep3 });
    AlignViewportI cdnaView = new AlignViewport(cdna);
    AlignViewportI peptideView = new AlignViewport(protein);
    protein.setCodonFrames(acfList);

    /*
     * Select pep1 and pep3 in the protein alignment
     */
    SequenceGroup sg = new SequenceGroup();
    sg.setColourText(true);
    sg.setIdColour(Color.GREEN);
    sg.setOutlineColour(Color.LIGHT_GRAY);
    sg.addSequence(pep1, false);
    sg.addSequence(pep3, false);
    sg.setEndRes(protein.getWidth() - 1);

    /*
     * Verify the mapped sequence group in dna is cds1 and cds3
     */
    SequenceGroup mappedGroup = MappingUtils.mapSequenceGroup(sg,
            peptideView, cdnaView);
    assertTrue(mappedGroup.getColourText());
    assertSame(sg.getIdColour(), mappedGroup.getIdColour());
    assertSame(sg.getOutlineColour(), mappedGroup.getOutlineColour());
    assertEquals(2, mappedGroup.getSequences().size());
    assertSame(cds1, mappedGroup.getSequences().get(0));
    assertSame(cds3, mappedGroup.getSequences().get(1));
    // columns 1-6 selected (0-5 base zero)
    assertEquals(0, mappedGroup.getStartRes());
    assertEquals(5, mappedGroup.getEndRes());

    /*
     * Select mapping sequence group from dna to protein
     */
    sg.clear();
    sg.addSequence(cds2, false);
    sg.addSequence(cds1, false);
    sg.setStartRes(0);
    sg.setEndRes(cdna.getWidth() - 1);
    mappedGroup = MappingUtils.mapSequenceGroup(sg, cdnaView, peptideView);
    assertTrue(mappedGroup.getColourText());
    assertSame(sg.getIdColour(), mappedGroup.getIdColour());
    assertSame(sg.getOutlineColour(), mappedGroup.getOutlineColour());
    assertEquals(2, mappedGroup.getSequences().size());
    assertSame(protein.getSequenceAt(1), mappedGroup.getSequences().get(0));
    assertSame(protein.getSequenceAt(0), mappedGroup.getSequences().get(1));
    assertEquals(0, mappedGroup.getStartRes());
    assertEquals(1, mappedGroup.getEndRes()); // two columns
  }
}
