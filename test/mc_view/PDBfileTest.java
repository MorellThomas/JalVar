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
package mc_view;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jalview.bin.Cache;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;
import jalview.io.DataSourceType;
import jalview.structure.StructureImportSettings;

public class PDBfileTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testIsRna()
  {
    SequenceI seq = new Sequence("Seq1", "CGAU");
    assertTrue(PDBfile.isRNA(seq));

    seq.setSequence("CGAu");
    assertFalse(PDBfile.isRNA(seq));

    seq.setSequence("CGAT");
    assertFalse(PDBfile.isRNA(seq));

    seq.setSequence("GRSWYFLAVM");
    assertFalse(PDBfile.isRNA(seq));
  }

  /**
   * Test the 'high level' outputs of parsing. More detailed tests in
   * PDBChainTest.
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testParse() throws IOException
  {
    /*
     * Constructor with file path performs parse()
     */
    PDBfile pf = new PDBfile(false, false, false, "examples/3W5V.pdb",
            DataSourceType.FILE);

    assertEquals("3W5V", pf.getId());
    // verify no alignment annotations created
    assertNull(getAlignmentAnnotations(pf));

    assertEquals(4, pf.getChains().size());
    assertEquals("A", pf.getChains().get(0).id);
    assertEquals("B", pf.getChains().get(1).id);
    assertEquals("C", pf.getChains().get(2).id);
    assertEquals("D", pf.getChains().get(3).id);

    PDBChain chainA = pf.getChains().get(0);
    SequenceI seqA = pf.getSeqs().get(0);

    assertEquals(0, chainA.seqstart); // not set
    assertEquals(0, chainA.seqend); // not set
    assertEquals(18, chainA.sequence.getStart());
    assertEquals(314, chainA.sequence.getEnd());
    assertTrue(
            chainA.sequence.getSequenceAsString().startsWith("KCSKKQEE"));
    assertTrue(chainA.sequence.getSequenceAsString().endsWith("WNVEVY"));
    assertEquals("3W5V|A", chainA.sequence.getName());
    assertNull(chainA.sequence.getAnnotation());
    assertEquals(1, seqA.getAllPDBEntries().size());
    PDBEntry pdb = seqA.getAllPDBEntries().get(0);
    assertEquals("A", pdb.getChainCode());
    assertEquals("PDB", pdb.getType());
    assertEquals("3W5V", pdb.getId());

    PDBChain chainB = pf.getChains().get(1);
    assertEquals(1, chainB.sequence.getStart());
    assertEquals(96, chainB.sequence.getEnd());
    assertTrue(chainB.sequence.getSequenceAsString().startsWith("ATYNVK"));
    assertTrue(chainB.sequence.getSequenceAsString().endsWith("KEEELT"));
    assertEquals("3W5V|B", chainB.sequence.getName());

    PDBChain chainC = pf.getChains().get(2);
    assertEquals(18, chainC.sequence.getStart());
    assertEquals(314, chainC.sequence.getEnd());
    assertTrue(
            chainC.sequence.getSequenceAsString().startsWith("KCSKKQEE"));
    assertTrue(chainC.sequence.getSequenceAsString().endsWith("WNVEVY"));
    assertEquals("3W5V|C", chainC.sequence.getName());

    PDBChain chainD = pf.getChains().get(3);
    assertEquals(1, chainD.sequence.getStart());
    assertEquals(96, chainD.sequence.getEnd());
    assertTrue(chainD.sequence.getSequenceAsString().startsWith("ATYNVK"));
    assertTrue(chainD.sequence.getSequenceAsString().endsWith("KEEELT"));
    assertEquals("3W5V|D", chainD.sequence.getName());

    /*
     * verify PDB-related data in parsed sequences
     */
    List<SequenceI> seqs = pf.getSeqs();
    assertEquals(4, seqs.size());
    assertEquals("3W5V|A", seqs.get(0).getName());
    assertEquals("3W5V|B", seqs.get(1).getName());
    assertEquals("3W5V|C", seqs.get(2).getName());
    assertEquals("3W5V|D", seqs.get(3).getName());
    assertEquals(1, seqs.get(0).getAllPDBEntries().size());
    PDBEntry pdbe = seqs.get(0).getAllPDBEntries().get(0);
    assertEquals("A", pdbe.getChainCode());
    assertEquals("3W5V", pdbe.getId());
    assertEquals(PDBEntry.Type.PDB.toString(), pdbe.getType());
  }

  /**
   * Test parsing, with annotations added to the alignment but no secondary
   * structure prediction
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testParse_withAnnotations_noSS() throws IOException
  {
    PDBfile pf = new PDBfile(true, false, false, "examples/3W5V.pdb",
            DataSourceType.FILE);

    AlignmentAnnotation[] anns = getAlignmentAnnotations(pf);
    assertEquals(4, anns.length);

    /*
     * Inspect temp factor annotation for chain A
     */
    AlignmentAnnotation chainAnnotation = anns[0];
    assertEquals("Temperature Factor", chainAnnotation.label);
    // PDBChain constructor changes PDB id to lower case (why?)
    assertEquals("Temperature Factor for 3w5vA",
            chainAnnotation.description);
    assertSame(pf.getSeqs().get(0), chainAnnotation.sequenceRef);
    assertEquals(AlignmentAnnotation.LINE_GRAPH, chainAnnotation.graph);
    assertEquals(0f, chainAnnotation.graphMin, 0.001f);
    assertEquals(40f, chainAnnotation.graphMax, 0.001f);
    assertEquals(297, chainAnnotation.annotations.length);
    assertEquals(40f, chainAnnotation.annotations[0].value, 0.001f);

    /*
     * Chain B temp factor
     */
    chainAnnotation = anns[1];
    assertEquals("Temperature Factor for 3w5vB",
            chainAnnotation.description);
    assertSame(pf.getSeqs().get(1), chainAnnotation.sequenceRef);
    assertEquals(96, chainAnnotation.annotations.length);

    /*
     * Chain C temp factor
     */
    chainAnnotation = anns[2];
    assertEquals("Temperature Factor for 3w5vC",
            chainAnnotation.description);
    assertSame(pf.getSeqs().get(2), chainAnnotation.sequenceRef);
    assertEquals(297, chainAnnotation.annotations.length);

    /*
     * Chain D temp factor
     */
    chainAnnotation = anns[3];
    assertEquals("Temperature Factor for 3w5vD",
            chainAnnotation.description);
    assertSame(pf.getSeqs().get(3), chainAnnotation.sequenceRef);
    assertEquals(96, chainAnnotation.annotations.length);
  }

  /**
   * Test parsing including secondary structure annotation using JMol; this test
   * for the case where flag to add annotations to alignment is set false
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testParse_withJmol_noAnnotations() throws IOException
  {
    PDBfile pf = new PDBfile(false, true, false, "examples/3W5V.pdb",
            DataSourceType.FILE);

    /*
     * alignment annotations _are_ created anyway (in
     * AlignSeq.replaceMatchingSeqsWith())
     */
    final AlignmentAnnotation[] anns = getAlignmentAnnotations(pf);
    assertEquals(4, anns.length);

    /*
     * no sequence annotations created - tempFactor annotation is not added
     * unless the flag to 'addAlignmentAnnotations' is set true
     */
    for (PDBChain c : pf.getChains())
    {
      assertNull(c.sequence.getAnnotation());
    }
  }

  /**
   * Test parsing including secondary structure prediction and annotation using
   * JMol
   * 
   * @throws IOException
   */
  @Test(groups = { "Functional" })
  public void testParse_withJmolAddAlignmentAnnotations() throws IOException
  {
    PDBfile pf = new PDBfile(true, true, false, "examples/3W5V.pdb",
            DataSourceType.FILE);

    /*
     * Alignment annotations for TempFactor, SecStruct, per sequence (chain)
     */
    AlignmentAnnotation[] anns = getAlignmentAnnotations(pf);
    assertEquals(8, anns.length);

    /*
     * other tests have detailed assertions for Temp Factor annotations
     */
    assertEquals("Temperature Factor for 3w5vA", anns[1].description);
    assertEquals("Temperature Factor for 3w5vB", anns[3].description);
    assertEquals("Temperature Factor for 3w5vC", anns[5].description);
    assertEquals("Temperature Factor for 3w5vD", anns[7].description);

    /*
     * PDBFileWithJmol (unlike PDBChain!) leaves PDB id upper case
     */
    assertEquals("Secondary Structure for 3w5vA", anns[0].description);
    assertEquals("Secondary Structure for 3w5vB", anns[2].description);
    assertEquals("Secondary Structure for 3w5vC", anns[4].description);
    assertEquals("Secondary Structure for 3w5vD", anns[6].description);

    /*
     * Verify SS annotations are linked to respective sequences (chains)
     */
    assertSame(pf.getSeqs().get(0), anns[0].sequenceRef);
    assertSame(pf.getSeqs().get(1), anns[2].sequenceRef);
    assertSame(pf.getSeqs().get(2), anns[4].sequenceRef);
    assertSame(pf.getSeqs().get(3), anns[6].sequenceRef);

    /*
     * Verify a sample of SS predictions
     */
    for (int i = 0; i < 20; i++)
    {
      assertNull(anns[0].annotations[i]);
      assertNull(anns[0].annotations[20].displayCharacter);
      assertEquals('E', anns[0].annotations[20].secondaryStructure);
      assertEquals('E', anns[2].annotations[18].secondaryStructure);
      assertEquals('H', anns[2].annotations[23].secondaryStructure);
    }
  }

  /**
   * Placeholder for a test of parsing RNA structure with secondary structure
   * prediction using the Annotate3D service
   * 
   * @throws IOException
   */

  @Test(groups = { "Functional" }, enabled = false)
  public void testParse_withAnnotate3D() throws IOException
  {
    // TODO requires a mock for Annotate3D processing
    // and/or run as an integration test
    PDBfile pf = new PDBfile(true, true, true, "examples/2GIS.pdb",
            DataSourceType.FILE);
  }

  /**
   * Helper method to extract parsed annotations from the PDBfile
   * 
   * @param pf
   * @return
   */
  private AlignmentAnnotation[] getAlignmentAnnotations(PDBfile pf)
  {
    AlignmentI al = new Alignment(pf.getSeqsAsArray());
    pf.addAnnotations(al);
    return al.getAlignmentAnnotation();
  }

  @BeforeMethod(alwaysRun = true)
  public void setUp()
  {
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    Cache.applicationProperties.setProperty("STRUCT_FROM_PDB",
            Boolean.TRUE.toString());
    Cache.applicationProperties.setProperty("ADD_TEMPFACT_ANN",
            Boolean.TRUE.toString());
    Cache.applicationProperties.setProperty("ADD_SS_ANN",
            Boolean.TRUE.toString());
    StructureImportSettings.setDefaultStructureFileFormat("PDB");
  }
}
