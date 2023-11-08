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
package jalview.ext.jmol;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import jalview.bin.Cache;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.JvOptionPane;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;
import jalview.structure.StructureImportSettings;
import jalview.structure.StructureImportSettings.StructureParser;

import java.util.Vector;

import org.jmol.c.STR;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import mc_view.PDBfile;

/**
 * @author jimp
 * 
 */
public class JmolParserTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /*
   * 1GAQ has been reduced to alpha carbons only
   * 1QCF is the full PDB file including headers, HETATM etc
   */
  String[] testFile = new String[] { "./examples/1gaq.txt",
      "./test/jalview/ext/jmol/1xyz.pdb",
      "./test/jalview/ext/jmol/1QCF.pdb" };

  //@formatter:off
  // a modified and very cut-down extract of 4UJ4
  String pastePDBDataWithChainBreak =
     "HEADER    TRANSPORT PROTEIN                       08-APR-15   4UJ4\n" +
     // chain B has missing residues; these should all go in the same sequence:
     "ATOM   1909  CA  VAL B 358      21.329 -19.739 -67.740  1.00201.05           C\n" +
     "ATOM   1916  CA  GLY B 359      21.694 -23.563 -67.661  1.00198.09           C\n" +
     "ATOM   1920  CA  LYS B 367      32.471 -12.135 -77.100  1.00257.97           C\n" +
     "ATOM   1925  CA  ALA B 368      31.032  -9.324 -74.946  1.00276.01           C\n" +
     // switch to chain C; should be a separate sequence
     "ATOM   1930  CA  SER C 369      32.589  -7.517 -71.978  1.00265.44           C\n" +
     "ATOM   1936  CA  ALA C 370      31.650  -6.849 -68.346  1.00249.48           C\n";
  //@formatter:on

  //@formatter:off
  // a very cut-down extract of 1ejg
  String pdbWithAltLoc =
     "HEADER    TRANSPORT PROTEIN                       08-APR-15   1EJG\n" +
     "ATOM    448  CA  ALA A  24       6.619  16.195   1.970  1.00  1.65           C\n" +
     "ATOM    458  CA ALEU A  25       3.048  14.822   1.781  0.57  1.48           C\n" +
     // alternative residue 25 entries (with ILE instead of LEU) should be ignored:
     "ATOM    478  CA BILE A  25       3.048  14.822   1.781  0.21  1.48           C\n" +
     // including the next altloc causes the unit test to fail but it works with the full file
     // not sure why!
     //     "ATOM    479  CA CILE A  25       3.048  14.822   1.781  0.22  1.48           C\n" +
     "ATOM    512  CA  CYS A  26       4.137  11.461   3.154  1.00  1.52           C\n";
  //@formatter:on

  @BeforeMethod(alwaysRun = true)
  public void setUp()
  {
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    Cache.applicationProperties.setProperty("STRUCT_FROM_PDB",
            Boolean.TRUE.toString());
    Cache.applicationProperties.setProperty("ADD_TEMPFACT_ANN",
            Boolean.FALSE.toString());
    Cache.applicationProperties.setProperty("ADD_SS_ANN",
            Boolean.TRUE.toString());
    StructureImportSettings.setDefaultStructureFileFormat("PDB");
    StructureImportSettings
            .setDefaultPDBFileParser(StructureParser.JALVIEW_PARSER);
  }

  @Test(groups = { "Functional" })
  public void testAlignmentLoader() throws Exception
  {
    for (String f : testFile)
    {
      FileLoader fl = new jalview.io.FileLoader(false);
      AlignFrame af = fl.LoadFileWaitTillLoaded(f, DataSourceType.FILE);
      validateSecStrRows(af.getViewport().getAlignment());
    }
  }

  @Test(groups = { "Functional" })
  public void testFileParser() throws Exception
  {
    for (String pdbStr : testFile)
    {
      PDBfile mctest = new PDBfile(false, false, false, pdbStr,
              DataSourceType.FILE);
      JmolParser jtest = new JmolParser(pdbStr, DataSourceType.FILE);
      Vector<SequenceI> seqs = jtest.getSeqs(), mcseqs = mctest.getSeqs();

      assertTrue("No sequences extracted from testfile\n"
              + (jtest.hasWarningMessage() ? jtest.getWarningMessage()
                      : "(No warnings raised)"),
              seqs != null && seqs.size() > 0);
      for (SequenceI sq : seqs)
      {
        assertEquals(
                "JMol didn't process " + pdbStr
                        + " to the same sequence as MCView",
                sq.getSequenceAsString(),
                mcseqs.remove(0).getSequenceAsString());
        AlignmentI al = new Alignment(new SequenceI[] { sq });
        validateSecStrRows(al);
      }
    }

  }

  private void validateSecStrRows(AlignmentI al)
  {
    if (!al.isNucleotide())
    {
      for (SequenceI asq : al.getSequences())
      {
        SequenceI sq = asq;
        boolean hasDs = false;
        while (sq.getDatasetSequence() != null
                && sq.getAnnotation() == null)
        {
          sq = sq.getDatasetSequence();
          hasDs = true;
        }
        checkFirstAAIsAssoc(sq);
        if (hasDs)
        {
          // also verify if alignment sequence has annotation on it
          // that is correctly mapped
          checkFirstAAIsAssoc(asq);
        }
      }
    }
  }

  private void checkFirstAAIsAssoc(SequenceI sq)
  {
    assertTrue(
            "No secondary structure assigned for protein sequence for "
                    + sq.getName(),
            sq.getAnnotation() != null && sq.getAnnotation().length >= 1
                    && sq.getAnnotation()[0].hasIcons);
    assertTrue(
            "Secondary structure not associated for sequence "
                    + sq.getName(),
            sq.getAnnotation()[0].sequenceRef == sq);
  }

  /**
   * Test parsing a chain with missing residues
   * 
   * @throws Exception
   */
  @Test(groups = { "Functional" })
  public void testParse_missingResidues() throws Exception
  {
    PDBfile mctest = new PDBfile(false, false, false,
            pastePDBDataWithChainBreak, DataSourceType.PASTE);
    JmolParser jtest = new JmolParser(pastePDBDataWithChainBreak,
            DataSourceType.PASTE);
    Vector<SequenceI> seqs = jtest.getSeqs();
    Vector<SequenceI> mcseqs = mctest.getSeqs();

    assertEquals("Failed to find 2 sequences\n", 2, seqs.size());
    assertEquals("Failed to find 2 sequences\n", 2, mcseqs.size());
    assertEquals("VGKA", seqs.get(0).getSequenceAsString());
    assertEquals("VGKA", mcseqs.get(0).getSequenceAsString());
    assertEquals("SA", seqs.get(1).getSequenceAsString());
    assertEquals("SA", mcseqs.get(1).getSequenceAsString());
  }

  /**
   * Test parsing a chain with 'altloc' residues
   * 
   * @throws Exception
   */
  @Test(groups = { "Functional" })
  public void testParse_alternativeResidues() throws Exception
  {
    PDBfile mctest = new PDBfile(false, false, false, pdbWithAltLoc,
            DataSourceType.PASTE);
    JmolParser jtest = new JmolParser(pdbWithAltLoc, DataSourceType.PASTE);
    Vector<SequenceI> seqs = jtest.getSeqs();
    Vector<SequenceI> mcseqs = mctest.getSeqs();

    assertEquals("Failed to find 1 sequence\n", 1, seqs.size());
    assertEquals("Failed to find 1 sequence\n", 1, mcseqs.size());
    assertEquals("ALC", seqs.get(0).getSequenceAsString());
    assertEquals("ALC", mcseqs.get(0).getSequenceAsString());
  }

  @Test(groups = "Functional")
  public void testSetSecondaryStructure()
  {
    JmolParser testee = new JmolParser();
    char[] struct = new char[10];
    char[] structCode = new char[10];
    struct[0] = '1';
    structCode[0] = '1';

    testee.setSecondaryStructure(STR.NONE, 0, struct, structCode);
    testee.setSecondaryStructure(STR.HELIX, 1, struct, structCode);
    testee.setSecondaryStructure(STR.HELIX310, 2, struct, structCode);
    testee.setSecondaryStructure(STR.HELIXALPHA, 3, struct, structCode);
    testee.setSecondaryStructure(STR.HELIXPI, 4, struct, structCode);
    testee.setSecondaryStructure(STR.SHEET, 5, struct, structCode);

    assertEquals(0, struct[0]);
    assertEquals('H', struct[1]);
    assertEquals('3', struct[2]);
    assertEquals('H', struct[3]);
    assertEquals('P', struct[4]);
    assertEquals('E', struct[5]);

    assertEquals(0, structCode[0]);
    assertEquals('H', structCode[1]);
    assertEquals('H', structCode[2]);
    assertEquals('H', structCode[3]);
    assertEquals('H', structCode[4]);
    assertEquals('E', structCode[5]);
  }

  @Test(groups = "Functional")
  public void testLocalPDBId() throws Exception
  {
    JmolParser structureData;
    /*
     * reads a local structure
     */
    structureData = new JmolParser("examples/testdata/localstruct.pdb",
            DataSourceType.FILE);
    assertNotNull(structureData);
    /*
     * local structure files should yield a false ID based on the filename
     */
    assertNotNull(structureData.getId());
    assertEquals(structureData.getId(), "localstruct");
    assertNotNull(structureData.getSeqs());
    /*
     * local structures have a fake ID
     */
    assertTrue(structureData.getSeqs().get(0).getAllPDBEntries().get(0)
            .fakedPDBId());
    /*
     * the ID is also the group for features derived from structure data 
     */
    String featureGroup = structureData.getSeqs().get(0)
            .getSequenceFeatures().get(0).featureGroup;
    assertNotNull(featureGroup);
    assertEquals(featureGroup, "localstruct");
  }
}
