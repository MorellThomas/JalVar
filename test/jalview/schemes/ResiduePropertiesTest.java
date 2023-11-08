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
package jalview.schemes;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.gui.JvOptionPane;

public class ResiduePropertiesTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * Test 'standard' codon translations (no ambiguity codes)
   */
  @Test(groups = { "Functional" })
  public void testCodonTranslate()
  {
    // standard translation table order column 1/2/3/4
    assertEquals("F", ResidueProperties.codonTranslate("TTT"));
    assertEquals("F", ResidueProperties.codonTranslate("TTC"));
    assertEquals("L", ResidueProperties.codonTranslate("TTA"));
    assertEquals("L", ResidueProperties.codonTranslate("TTG"));
    assertEquals("L", ResidueProperties.codonTranslate("CTT"));
    assertEquals("L", ResidueProperties.codonTranslate("CTC"));
    assertEquals("L", ResidueProperties.codonTranslate("CTA"));
    assertEquals("L", ResidueProperties.codonTranslate("CTG"));
    assertEquals("I", ResidueProperties.codonTranslate("ATT"));
    assertEquals("I", ResidueProperties.codonTranslate("ATC"));
    assertEquals("I", ResidueProperties.codonTranslate("ATA"));
    assertEquals("M", ResidueProperties.codonTranslate("ATG"));
    assertEquals("V", ResidueProperties.codonTranslate("GTT"));
    assertEquals("V", ResidueProperties.codonTranslate("GTC"));
    assertEquals("V", ResidueProperties.codonTranslate("GTA"));
    assertEquals("V", ResidueProperties.codonTranslate("GTG"));
    assertEquals("S", ResidueProperties.codonTranslate("TCT"));
    assertEquals("S", ResidueProperties.codonTranslate("TCC"));
    assertEquals("S", ResidueProperties.codonTranslate("TCA"));
    assertEquals("S", ResidueProperties.codonTranslate("TCG"));
    assertEquals("P", ResidueProperties.codonTranslate("CCT"));
    assertEquals("P", ResidueProperties.codonTranslate("CCC"));
    assertEquals("P", ResidueProperties.codonTranslate("CCA"));
    assertEquals("P", ResidueProperties.codonTranslate("CCG"));
    assertEquals("T", ResidueProperties.codonTranslate("ACT"));
    assertEquals("T", ResidueProperties.codonTranslate("ACC"));
    assertEquals("T", ResidueProperties.codonTranslate("ACA"));
    assertEquals("T", ResidueProperties.codonTranslate("ACG"));
    assertEquals("A", ResidueProperties.codonTranslate("GCT"));
    assertEquals("A", ResidueProperties.codonTranslate("GCC"));
    assertEquals("A", ResidueProperties.codonTranslate("GCA"));
    assertEquals("A", ResidueProperties.codonTranslate("GCG"));
    assertEquals("Y", ResidueProperties.codonTranslate("TAT"));
    assertEquals("Y", ResidueProperties.codonTranslate("TAC"));
    assertEquals("STOP", ResidueProperties.codonTranslate("TAA"));
    assertEquals("STOP", ResidueProperties.codonTranslate("TAG"));
    assertEquals("H", ResidueProperties.codonTranslate("CAT"));
    assertEquals("H", ResidueProperties.codonTranslate("CAC"));
    assertEquals("Q", ResidueProperties.codonTranslate("CAA"));
    assertEquals("Q", ResidueProperties.codonTranslate("CAG"));
    assertEquals("N", ResidueProperties.codonTranslate("AAT"));
    assertEquals("N", ResidueProperties.codonTranslate("AAC"));
    assertEquals("K", ResidueProperties.codonTranslate("AAA"));
    assertEquals("K", ResidueProperties.codonTranslate("AAG"));
    assertEquals("D", ResidueProperties.codonTranslate("GAT"));
    assertEquals("D", ResidueProperties.codonTranslate("GAC"));
    assertEquals("E", ResidueProperties.codonTranslate("GAA"));
    assertEquals("E", ResidueProperties.codonTranslate("GAG"));
    assertEquals("C", ResidueProperties.codonTranslate("TGT"));
    assertEquals("C", ResidueProperties.codonTranslate("TGC"));
    assertEquals("STOP", ResidueProperties.codonTranslate("TGA"));
    assertEquals("W", ResidueProperties.codonTranslate("TGG"));
    assertEquals("R", ResidueProperties.codonTranslate("CGT"));
    assertEquals("R", ResidueProperties.codonTranslate("CGC"));
    assertEquals("R", ResidueProperties.codonTranslate("CGA"));
    assertEquals("R", ResidueProperties.codonTranslate("CGG"));
    assertEquals("S", ResidueProperties.codonTranslate("AGT"));
    assertEquals("S", ResidueProperties.codonTranslate("AGC"));
    assertEquals("R", ResidueProperties.codonTranslate("AGA"));
    assertEquals("R", ResidueProperties.codonTranslate("AGG"));
    assertEquals("G", ResidueProperties.codonTranslate("GGT"));
    assertEquals("G", ResidueProperties.codonTranslate("GGC"));
    assertEquals("G", ResidueProperties.codonTranslate("GGA"));
    assertEquals("G", ResidueProperties.codonTranslate("GGG"));
  }

  /**
   * Test a sample of codon translations involving ambiguity codes. Should
   * return a protein value where the ambiguity does not affect the translation.
   */
  @Test(groups = { "Functional" })
  public void testCodonTranslate_ambiguityCodes()
  {
    // Y is C or T
    assertEquals("C", ResidueProperties.codonTranslate("TGY"));
    // Phenylalanine first base variation
    assertEquals("L", ResidueProperties.codonTranslate("YTA"));

    // W is A or T
    assertEquals("L", ResidueProperties.codonTranslate("CTW"));
    assertNull(ResidueProperties.codonTranslate("TTW"));

    // S is G or C
    assertEquals("G", ResidueProperties.codonTranslate("GGS"));
    assertNull(ResidueProperties.codonTranslate("ATS"));

    // K is T or G
    assertEquals("S", ResidueProperties.codonTranslate("TCK"));
    assertNull(ResidueProperties.codonTranslate("ATK"));

    // M is C or A
    assertEquals("T", ResidueProperties.codonTranslate("ACM"));
    // Arginine first base variation
    assertEquals("R", ResidueProperties.codonTranslate("MGA"));
    assertEquals("R", ResidueProperties.codonTranslate("MGG"));
    assertNull(ResidueProperties.codonTranslate("TAM"));

    // D is A, G or T
    assertEquals("P", ResidueProperties.codonTranslate("CCD"));
    assertNull(ResidueProperties.codonTranslate("AAD"));

    // V is A, C or G
    assertEquals("V", ResidueProperties.codonTranslate("GTV"));
    assertNull(ResidueProperties.codonTranslate("TTV"));

    // H is A, C or T
    assertEquals("A", ResidueProperties.codonTranslate("GCH"));
    assertEquals("I", ResidueProperties.codonTranslate("ATH"));
    assertNull(ResidueProperties.codonTranslate("AGH"));

    // B is C, G or T
    assertEquals("P", ResidueProperties.codonTranslate("CCB"));
    assertNull(ResidueProperties.codonTranslate("TAB"));

    // R is A or G
    // additional tests for JAL-1685 (resolved)
    assertEquals("L", ResidueProperties.codonTranslate("CTR"));
    assertEquals("V", ResidueProperties.codonTranslate("GTR"));
    assertEquals("S", ResidueProperties.codonTranslate("TCR"));
    assertEquals("P", ResidueProperties.codonTranslate("CCR"));
    assertEquals("T", ResidueProperties.codonTranslate("ACR"));
    assertEquals("A", ResidueProperties.codonTranslate("GCR"));
    assertEquals("R", ResidueProperties.codonTranslate("CGR"));
    assertEquals("G", ResidueProperties.codonTranslate("GGR"));
    assertEquals("R", ResidueProperties.codonTranslate("AGR"));
    assertEquals("E", ResidueProperties.codonTranslate("GAR"));
    assertEquals("K", ResidueProperties.codonTranslate("AAR"));
    assertEquals("L", ResidueProperties.codonTranslate("TTR"));
    assertEquals("Q", ResidueProperties.codonTranslate("CAR"));
    assertEquals("STOP", ResidueProperties.codonTranslate("TAR"));
    assertEquals("STOP", ResidueProperties.codonTranslate("TRA"));
    // Arginine first and third base ambiguity
    assertEquals("R", ResidueProperties.codonTranslate("MGR"));
    assertNull(ResidueProperties.codonTranslate("ATR"));

    // N is any base; 8 proteins accept any base in 3rd position
    assertEquals("L", ResidueProperties.codonTranslate("CTN"));
    assertEquals("V", ResidueProperties.codonTranslate("GTN"));
    assertEquals("S", ResidueProperties.codonTranslate("TCN"));
    assertEquals("P", ResidueProperties.codonTranslate("CCN"));
    assertEquals("T", ResidueProperties.codonTranslate("ACN"));
    assertEquals("A", ResidueProperties.codonTranslate("GCN"));
    assertEquals("R", ResidueProperties.codonTranslate("CGN"));
    assertEquals("G", ResidueProperties.codonTranslate("GGN"));
    assertNull(ResidueProperties.codonTranslate("ATN"));
    assertNull(ResidueProperties.codonTranslate("ANT"));
    assertNull(ResidueProperties.codonTranslate("NAT"));
    assertNull(ResidueProperties.codonTranslate("ANN"));
    assertNull(ResidueProperties.codonTranslate("NNA"));
    assertNull(ResidueProperties.codonTranslate("NNN"));

    // some random stuff
    assertNull(ResidueProperties.codonTranslate("YWB"));
    assertNull(ResidueProperties.codonTranslate("VHD"));
    assertNull(ResidueProperties.codonTranslate("WSK"));
  }

  @Test(groups = { "Functional" })
  public void testGetResidues_nucleotide()
  {
    /*
     * Non-ambiguous only; we don't care about the order of the list, it is just
     * sorted here to make assertions reliable
     */
    List<String> residues = ResidueProperties.getResidues(true, false);
    Collections.sort(residues);
    assertEquals("[A, C, G, T, U]", residues.toString());

    /*
     * Including ambiguity codes I N R X Y
     */
    residues = ResidueProperties.getResidues(true, true);
    Collections.sort(residues);
    assertEquals("[A, B, C, D, G, H, I, K, M, N, R, S, T, U, V, W, X, Y]",
            residues.toString());
  }

  @Test(groups = { "Functional" })
  public void testGetResidues_peptide()
  {
    /*
     * Non-ambiguous only; we don't care about the order of the list, it is just
     * sorted here to make assertions reliable
     */
    List<String> residues = ResidueProperties.getResidues(false, false);
    Collections.sort(residues);
    assertEquals(
            "[ALA, ARG, ASN, ASP, CYS, GLN, GLU, GLY, HIS, ILE, LEU, LYS, MET, PHE, PRO, SER, THR, TRP, TYR, VAL]",
            residues.toString());

    /*
     * Including ambiguity codes ASX, GLX, XAA
     */
    residues = ResidueProperties.getResidues(false, true);
    Collections.sort(residues);
    assertEquals(
            "[ALA, ARG, ASN, ASP, ASX, CYS, GLN, GLU, GLX, GLY, HIS, ILE, LEU, LYS, MET, PHE, PRO, SER, THR, TRP, TYR, VAL, XAA]",
            residues.toString());
  }

  @Test(groups = { "Functional" })
  public void testGetCanonicalAminoAcid()
  {
    assertEquals("MET", ResidueProperties.getCanonicalAminoAcid("MET"));
    assertEquals("MET", ResidueProperties.getCanonicalAminoAcid("MSE"));

    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("00C"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("01W"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("02K"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("03Y"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("07O"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("08P"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("0A0"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("0A1"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("0A2"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("0A8"));
    assertEquals("VAL", ResidueProperties.getCanonicalAminoAcid("0AA"));
    assertEquals("VAL", ResidueProperties.getCanonicalAminoAcid("0AB"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("0AC"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("0AD"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("0AF"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("0AG"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("0AH"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("0AK"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("0AM"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("0AP"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("0AU"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("0AV"));
    assertEquals("PRO", ResidueProperties.getCanonicalAminoAcid("0AZ"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("0BN"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("0C "));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("0CS"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("0DC"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("0DG"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("0DT"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("0FL"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("0G "));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("0NC"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("0SP"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("0U "));
    assertEquals("YG", ResidueProperties.getCanonicalAminoAcid("0YG"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("10C"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("125"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("126"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("127"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("128"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("12A"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("143"));
    assertEquals("ASG", ResidueProperties.getCanonicalAminoAcid("175"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("193"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("1AP"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("1MA"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("1MG"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("1PA"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("1PI"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("1PR"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("1SC"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("1TQ"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("1TY"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("1X6"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("200"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("23F"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("23S"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("26B"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("2AD"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("2AG"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("2AO"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("2AR"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("2AS"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("2AT"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("2AU"));
    assertEquals("ILE", ResidueProperties.getCanonicalAminoAcid("2BD"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("2BT"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("2BU"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("2CO"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("2DA"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("2DF"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("2DM"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("2DO"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("2DT"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("2EG"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("2FE"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("2FI"));
    assertEquals("MET", ResidueProperties.getCanonicalAminoAcid("2FM"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("2GT"));
    assertEquals("HIS", ResidueProperties.getCanonicalAminoAcid("2HF"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("2LU"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("2MA"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("2MG"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("2ML"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("2MR"));
    assertEquals("PRO", ResidueProperties.getCanonicalAminoAcid("2MT"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("2MU"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("2NT"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("2OM"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("2OT"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("2PI"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("2PR"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("2SA"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("2SI"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("2ST"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("2TL"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("2TY"));
    assertEquals("VAL", ResidueProperties.getCanonicalAminoAcid("2VA"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("2XA"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("32S"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("32T"));
    assertEquals("HIS", ResidueProperties.getCanonicalAminoAcid("3AH"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("3AR"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("3CF"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("3DA"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("3DR"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("3GA"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("3MD"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("3ME"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("3NF"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("3QN"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("3TY"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("3XH"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("4AC"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("4BF"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("4CF"));
    assertEquals("MET", ResidueProperties.getCanonicalAminoAcid("4CY"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("4DP"));
    assertEquals("GYG", ResidueProperties.getCanonicalAminoAcid("4F3"));
    assertEquals("PRO", ResidueProperties.getCanonicalAminoAcid("4FB"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("4FW"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("4HT"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("4IN"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("4MF"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("4MM"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("4OC"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("4PC"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("4PD"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("4PE"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("4PH"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("4SC"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("4SU"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("4TA"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("4U7"));
    assertEquals("HIS", ResidueProperties.getCanonicalAminoAcid("56A"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("5AA"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("5AB"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("5AT"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("5BU"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("5CG"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("5CM"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("5CS"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("5FA"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("5FC"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("5FU"));
    assertEquals("GLU", ResidueProperties.getCanonicalAminoAcid("5HP"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("5HT"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("5HU"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("5IC"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("5IT"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("5IU"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("5MC"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("5MD"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("5MU"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("5NC"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("5PC"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("5PY"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("5SE"));
    assertEquals("TWG", ResidueProperties.getCanonicalAminoAcid("5ZA"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("64T"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("6CL"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("6CT"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("6CW"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("6HA"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("6HC"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("6HG"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("6HN"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("6HT"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("6IA"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("6MA"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("6MC"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("6MI"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("6MT"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("6MZ"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("6OG"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("70U"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("7DA"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("7GU"));
    assertEquals("ILE", ResidueProperties.getCanonicalAminoAcid("7JA"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("7MG"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("8AN"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("8FG"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("8MG"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("8OG"));
    assertEquals("GLU", ResidueProperties.getCanonicalAminoAcid("9NE"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("9NF"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("9NR"));
    assertEquals("VAL", ResidueProperties.getCanonicalAminoAcid("9NV"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("A  "));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("A1P"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("A23"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("A2L"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("A2M"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("A34"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("A35"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("A38"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("A39"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("A3A"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("A3P"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("A40"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("A43"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("A44"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("A47"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("A5L"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("A5M"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("A5N"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("A5O"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("A66"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("AA3"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("AA4"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("AAR"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("AB7"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("ABA"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("ABR"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("ABS"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("ABT"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("ACB"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("ACL"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("AD2"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("ADD"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("ADX"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("AEA"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("AEI"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("AET"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("AFA"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("AFF"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("AFG"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("AGM"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("AGT"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("AHB"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("AHH"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("AHO"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("AHP"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("AHS"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("AHT"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("AIB"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("AKL"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("AKZ"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("ALA"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("ALC"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("ALM"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("ALN"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("ALO"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("ALQ"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("ALS"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("ALT"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("ALV"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("ALY"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("AN8"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("AP7"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("APE"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("APH"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("API"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("APK"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("APM"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("APP"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("AR2"));
    assertEquals("GLU", ResidueProperties.getCanonicalAminoAcid("AR4"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("AR7"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("ARG"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("ARM"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("ARO"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("ARV"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("AS "));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("AS2"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("AS9"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("ASA"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("ASB"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("ASI"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("ASK"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("ASL"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("ASM"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("ASN"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("ASP"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("ASQ"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("ASU"));
    assertEquals("ASX", ResidueProperties.getCanonicalAminoAcid("ASX"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("ATD"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("ATL"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("ATM"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("AVC"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("AVN"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("AYA"));
    assertEquals("AYG", ResidueProperties.getCanonicalAminoAcid("AYG"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("AZK"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("AZS"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("AZY"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("B1F"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("B1P"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("B2A"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("B2F"));
    assertEquals("ILE", ResidueProperties.getCanonicalAminoAcid("B2I"));
    assertEquals("VAL", ResidueProperties.getCanonicalAminoAcid("B2V"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("B3A"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("B3D"));
    assertEquals("GLU", ResidueProperties.getCanonicalAminoAcid("B3E"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("B3K"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("B3L"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("B3M"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("B3Q"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("B3S"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("B3T"));
    assertEquals("HIS", ResidueProperties.getCanonicalAminoAcid("B3U"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("B3X"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("B3Y"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("BB6"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("BB7"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("BB8"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("BB9"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("BBC"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("BCS"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("BE2"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("BFD"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("BG1"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("BGM"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("BH2"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("BHD"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("BIF"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("BIL"));
    assertEquals("ILE", ResidueProperties.getCanonicalAminoAcid("BIU"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("BJH"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("BLE"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("BLY"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("BMP"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("BMT"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("BNN"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("BNO"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("BOE"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("BOR"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("BPE"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("BRU"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("BSE"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("BT5"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("BTA"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("BTC"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("BTR"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("BUC"));
    assertEquals("VAL", ResidueProperties.getCanonicalAminoAcid("BUG"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("BVP"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("BZG"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("C  "));
    assertEquals("TYG", ResidueProperties.getCanonicalAminoAcid("C12"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("C1X"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("C25"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("C2L"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("C2S"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("C31"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("C32"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("C34"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("C36"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("C37"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("C38"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("C3Y"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("C42"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("C43"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("C45"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("C46"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("C49"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("C4R"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("C4S"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("C5C"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("C66"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("C6C"));
    assertEquals("TFG", ResidueProperties.getCanonicalAminoAcid("C99"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CAF"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("CAL"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CAR"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CAS"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("CAV"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CAY"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CB2"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CBR"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CBV"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CCC"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("CCL"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CCS"));
    assertEquals("CYG", ResidueProperties.getCanonicalAminoAcid("CCY"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("CDE"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("CDV"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CDW"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CEA"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CFL"));
    assertEquals("FCYG", ResidueProperties.getCanonicalAminoAcid("CFY")); // check
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("CG1"));
    assertEquals("GLU", ResidueProperties.getCanonicalAminoAcid("CGA"));
    assertEquals("GLU", ResidueProperties.getCanonicalAminoAcid("CGU"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CH "));
    assertEquals("MYG", ResidueProperties.getCanonicalAminoAcid("CH6"));
    assertEquals("KYG", ResidueProperties.getCanonicalAminoAcid("CH7"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("CHF"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("CHG"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("CHP"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("CHS"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("CIR"));
    assertEquals("GYG", ResidueProperties.getCanonicalAminoAcid("CJO"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("CLE"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("CLG"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("CLH"));
    assertEquals("AFG", ResidueProperties.getCanonicalAminoAcid("CLV"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("CM0"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CME"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CMH"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CML"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CMR"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CMT"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("CNU"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CP1"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("CPC"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("CPI"));
    assertEquals("GYG", ResidueProperties.getCanonicalAminoAcid("CQR"));
    assertEquals("TLG", ResidueProperties.getCanonicalAminoAcid("CR0"));
    assertEquals("GYG", ResidueProperties.getCanonicalAminoAcid("CR2"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("CR5"));
    assertEquals("KYG", ResidueProperties.getCanonicalAminoAcid("CR7"));
    assertEquals("HYG", ResidueProperties.getCanonicalAminoAcid("CR8"));
    assertEquals("TWG", ResidueProperties.getCanonicalAminoAcid("CRF"));
    assertEquals("THG", ResidueProperties.getCanonicalAminoAcid("CRG"));
    assertEquals("MYG", ResidueProperties.getCanonicalAminoAcid("CRK"));
    assertEquals("GYG", ResidueProperties.getCanonicalAminoAcid("CRO"));
    assertEquals("QYG", ResidueProperties.getCanonicalAminoAcid("CRQ"));
    assertEquals("EYG", ResidueProperties.getCanonicalAminoAcid("CRU"));
    assertEquals("ASG", ResidueProperties.getCanonicalAminoAcid("CRW"));
    assertEquals("ASG", ResidueProperties.getCanonicalAminoAcid("CRX"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CS0"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CS1"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CS3"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CS4"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("CS8"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CSA"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CSB"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CSD"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CSE"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CSF"));
    assertEquals("SHG", ResidueProperties.getCanonicalAminoAcid("CSH"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("CSI"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CSJ"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CSL"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CSO"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CSP"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CSR"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CSS"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CSU"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CSW"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CSX"));
    assertEquals("SYG", ResidueProperties.getCanonicalAminoAcid("CSY"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CSZ"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("CTE"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("CTG"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("CTH"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("CUC"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("CWR"));
    assertEquals("MET", ResidueProperties.getCanonicalAminoAcid("CXM"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CY0"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CY1"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CY3"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CY4"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CYA"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CYD"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CYF"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CYG"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("CYJ"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CYM"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CYQ"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CYR"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CYS"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CZ2"));
    assertEquals("GYG", ResidueProperties.getCanonicalAminoAcid("CZO"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("CZZ"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("D11"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("D1P"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("D3 "));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("D33"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("D3P"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("D3T"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("D4M"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("D4P"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("DA "));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("DA2"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("DAB"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("DAH"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("DAL"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("DAR"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("DAS"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("DBB"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("DBM"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("DBS"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("DBU"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("DBY"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("DBZ"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("DC "));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("DC2"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("DCG"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("DCI"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("DCL"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("DCT"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("DCY"));
    assertEquals("HIS", ResidueProperties.getCanonicalAminoAcid("DDE"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("DDG"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("DDN"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("DDX"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("DFC"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("DFG"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("DFI"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("DFO"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("DFT"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("DG "));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("DGH"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("DGI"));
    assertEquals("GLU", ResidueProperties.getCanonicalAminoAcid("DGL"));
    assertEquals("GLN", ResidueProperties.getCanonicalAminoAcid("DGN"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("DHA"));
    assertEquals("HIS", ResidueProperties.getCanonicalAminoAcid("DHI"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("DHL"));
    assertEquals("VAL", ResidueProperties.getCanonicalAminoAcid("DHN"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("DHP"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("DHU"));
    assertEquals("VAL", ResidueProperties.getCanonicalAminoAcid("DHV"));
    assertEquals("ILE", ResidueProperties.getCanonicalAminoAcid("DI "));
    assertEquals("ILE", ResidueProperties.getCanonicalAminoAcid("DIL"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("DIR"));
    assertEquals("VAL", ResidueProperties.getCanonicalAminoAcid("DIV"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("DLE"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("DLS"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("DLY"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("DM0"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("DMH"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("DMK"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("DMT"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("DN "));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("DNE"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("DNG"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("DNL"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("DNM"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("DNP"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("DNR"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("DNS"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("DOA"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("DOC"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("DOH"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("DON"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("DPB"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("DPH"));
    assertEquals("PRO", ResidueProperties.getCanonicalAminoAcid("DPL"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("DPP"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("DPQ"));
    assertEquals("PRO", ResidueProperties.getCanonicalAminoAcid("DPR"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("DPY"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("DRM"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("DRP"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("DRT"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("DRZ"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("DSE"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("DSG"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("DSN"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("DSP"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("DT "));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("DTH"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("DTR"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("DTY"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("DU "));
    assertEquals("VAL", ResidueProperties.getCanonicalAminoAcid("DVA"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("DXD"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("DXN"));
    assertEquals("DYG", ResidueProperties.getCanonicalAminoAcid("DYG"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("DYS"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("DZM"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("E  "));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("E1X"));
    assertEquals("GLN", ResidueProperties.getCanonicalAminoAcid("ECC"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("EDA"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("EFC"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("EHP"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("EIT"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("ENP"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("ESB"));
    assertEquals("MET", ResidueProperties.getCanonicalAminoAcid("ESC"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("EXB"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("EXY"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("EY5"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("EYS"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("F2F"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("FA2"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("FA5"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("FAG"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("FAI"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("FB5"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("FB6"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("FCL"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("FFD"));
    assertEquals("GLU", ResidueProperties.getCanonicalAminoAcid("FGA"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("FGL"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("FGP"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("FHL"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("FHO"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("FHU"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("FLA"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("FLE"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("FLT"));
    assertEquals("MET", ResidueProperties.getCanonicalAminoAcid("FME"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("FMG"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("FMU"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("FOE"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("FOX"));
    assertEquals("PRO", ResidueProperties.getCanonicalAminoAcid("FP9"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("FPA"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("FRD"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("FT6"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("FTR"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("FTY"));
    assertEquals("VAL", ResidueProperties.getCanonicalAminoAcid("FVA"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("FZN"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("G  "));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("G25"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("G2L"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("G2S"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("G31"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("G32"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("G33"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("G36"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("G38"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("G42"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("G46"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("G47"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("G48"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("G49"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("G4P"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("G7M"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("GAO"));
    assertEquals("GLU", ResidueProperties.getCanonicalAminoAcid("GAU"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("GCK"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("GCM"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("GDP"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("GDR"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("GFL"));
    assertEquals("GLU", ResidueProperties.getCanonicalAminoAcid("GGL"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("GH3"));
    assertEquals("GLN", ResidueProperties.getCanonicalAminoAcid("GHG"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("GHP"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("GL3"));
    assertEquals("GLN", ResidueProperties.getCanonicalAminoAcid("GLH"));
    assertEquals("GLU", ResidueProperties.getCanonicalAminoAcid("GLJ"));
    assertEquals("GLU", ResidueProperties.getCanonicalAminoAcid("GLK"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("GLM"));
    assertEquals("GLN", ResidueProperties.getCanonicalAminoAcid("GLN"));
    assertEquals("GLU", ResidueProperties.getCanonicalAminoAcid("GLQ"));
    assertEquals("GLU", ResidueProperties.getCanonicalAminoAcid("GLU"));
    assertEquals("GLX", ResidueProperties.getCanonicalAminoAcid("GLX"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("GLY"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("GLZ"));
    assertEquals("GLU", ResidueProperties.getCanonicalAminoAcid("GMA"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("GMS"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("GMU"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("GN7"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("GND"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("GNE"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("GOM"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("GPL"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("GS "));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("GSC"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("GSR"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("GSS"));
    assertEquals("GLU", ResidueProperties.getCanonicalAminoAcid("GSU"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("GT9"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("GTP"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("GVL"));
    assertEquals("CYG", ResidueProperties.getCanonicalAminoAcid("GYC"));
    assertEquals("SYG", ResidueProperties.getCanonicalAminoAcid("GYS"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("H2U"));
    assertEquals("PRO", ResidueProperties.getCanonicalAminoAcid("H5M"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("HAC"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("HAR"));
    assertEquals("HIS", ResidueProperties.getCanonicalAminoAcid("HBN"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("HCS"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("HDP"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("HEU"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("HFA"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("HGL"));
    assertEquals("HIS", ResidueProperties.getCanonicalAminoAcid("HHI"));
    assertEquals("AK", ResidueProperties.getCanonicalAminoAcid("HHK")); // check
    assertEquals("HIS", ResidueProperties.getCanonicalAminoAcid("HIA"));
    assertEquals("HIS", ResidueProperties.getCanonicalAminoAcid("HIC"));
    assertEquals("HIS", ResidueProperties.getCanonicalAminoAcid("HIP"));
    assertEquals("HIS", ResidueProperties.getCanonicalAminoAcid("HIQ"));
    assertEquals("HIS", ResidueProperties.getCanonicalAminoAcid("HIS"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("HL2"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("HLU"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("HMR"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("HOL"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("HPC"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("HPE"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("HPH"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("HPQ"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("HQA"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("HRG"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("HRP"));
    assertEquals("HIS", ResidueProperties.getCanonicalAminoAcid("HS8"));
    assertEquals("HIS", ResidueProperties.getCanonicalAminoAcid("HS9"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("HSE"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("HSL"));
    assertEquals("HIS", ResidueProperties.getCanonicalAminoAcid("HSO"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("HTI"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("HTN"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("HTR"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("HV5"));
    assertEquals("VAL", ResidueProperties.getCanonicalAminoAcid("HVA"));
    assertEquals("PRO", ResidueProperties.getCanonicalAminoAcid("HY3"));
    assertEquals("PRO", ResidueProperties.getCanonicalAminoAcid("HYP"));
    assertEquals("PRO", ResidueProperties.getCanonicalAminoAcid("HZP"));
    assertEquals("ILE", ResidueProperties.getCanonicalAminoAcid("I  "));
    assertEquals("ILE", ResidueProperties.getCanonicalAminoAcid("I2M"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("I58"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("I5C"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("IAM"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("IAR"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("IAS"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("IC "));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("IEL"));
    assertEquals("HYG", ResidueProperties.getCanonicalAminoAcid("IEY"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("IG "));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("IGL"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("IGU"));
    assertEquals("SHG", ResidueProperties.getCanonicalAminoAcid("IIC"));
    assertEquals("ILE", ResidueProperties.getCanonicalAminoAcid("IIL"));
    assertEquals("ILE", ResidueProperties.getCanonicalAminoAcid("ILE"));
    assertEquals("GLU", ResidueProperties.getCanonicalAminoAcid("ILG"));
    assertEquals("ILE", ResidueProperties.getCanonicalAminoAcid("ILX"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("IMC"));
    assertEquals("ILE", ResidueProperties.getCanonicalAminoAcid("IML"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("IOY"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("IPG"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("IPN"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("IRN"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("IT1"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("IU "));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("IYR"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("IYT"));
    assertEquals("MET", ResidueProperties.getCanonicalAminoAcid("IZO"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("JJJ"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("JJK"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("JJL"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("JW5"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("K1R"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("KAG"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("KCX"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("KGC"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("KNB"));
    assertEquals("MET", ResidueProperties.getCanonicalAminoAcid("KOR"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("KPI"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("KST"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("KYQ"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("L2A"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("LA2"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("LAA"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("LAL"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("LBY"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("LC "));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("LCA"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("LCC"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("LCG"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("LCH"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("LCK"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("LCX"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("LDH"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("LED"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("LEF"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("LEH"));
    assertEquals("VAL", ResidueProperties.getCanonicalAminoAcid("LEI"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("LEM"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("LEN"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("LET"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("LEU"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("LEX"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("LG "));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("LGP"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("LHC"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("LHU"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("LKC"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("LLP"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("LLY"));
    assertEquals("GLU", ResidueProperties.getCanonicalAminoAcid("LME"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("LMF"));
    assertEquals("GLN", ResidueProperties.getCanonicalAminoAcid("LMQ"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("LMS"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("LP6"));
    assertEquals("PRO", ResidueProperties.getCanonicalAminoAcid("LPD"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("LPG"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("LPL"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("LPS"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("LSO"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("LTA"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("LTR"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("LVG"));
    assertEquals("VAL", ResidueProperties.getCanonicalAminoAcid("LVN"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("LYF"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("LYK"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("LYM"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("LYN"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("LYR"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("LYS"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("LYX"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("LYZ"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("M0H"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("M1G"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("M2G"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("M2L"));
    assertEquals("MET", ResidueProperties.getCanonicalAminoAcid("M2S"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("M30"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("M3L"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("M5M"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("MA "));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("MA6"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("MA7"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("MAA"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("MAD"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("MAI"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("MBQ"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("MBZ"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("MC1"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("MCG"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("MCL"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("MCS"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("MCY"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("MD3"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("MD6"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("MDH"));
    assertEquals("ASG", ResidueProperties.getCanonicalAminoAcid("MDO"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("MDR"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("MEA"));
    assertEquals("MET", ResidueProperties.getCanonicalAminoAcid("MED"));
    assertEquals("GLU", ResidueProperties.getCanonicalAminoAcid("MEG"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("MEN"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("MEP"));
    assertEquals("GLN", ResidueProperties.getCanonicalAminoAcid("MEQ"));
    assertEquals("MET", ResidueProperties.getCanonicalAminoAcid("MET"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("MEU"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("MF3"));
    assertEquals("GYG", ResidueProperties.getCanonicalAminoAcid("MFC"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("MG1"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("MGG"));
    assertEquals("GLN", ResidueProperties.getCanonicalAminoAcid("MGN"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("MGQ"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("MGV"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("MGY"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("MHL"));
    assertEquals("MET", ResidueProperties.getCanonicalAminoAcid("MHO"));
    assertEquals("HIS", ResidueProperties.getCanonicalAminoAcid("MHS"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("MIA"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("MIS"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("MK8"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("ML3"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("MLE"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("MLL"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("MLY"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("MLZ"));
    assertEquals("MET", ResidueProperties.getCanonicalAminoAcid("MME"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("MMO"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("MMT"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("MND"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("MNL"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("MNU"));
    assertEquals("VAL", ResidueProperties.getCanonicalAminoAcid("MNV"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("MOD"));
    assertEquals("PRO", ResidueProperties.getCanonicalAminoAcid("MP8"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("MPH"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("MPJ"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("MPQ"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("MRG"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("MSA"));
    assertEquals("MET", ResidueProperties.getCanonicalAminoAcid("MSE"));
    assertEquals("MET", ResidueProperties.getCanonicalAminoAcid("MSL"));
    assertEquals("MET", ResidueProperties.getCanonicalAminoAcid("MSO"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("MSP"));
    assertEquals("MET", ResidueProperties.getCanonicalAminoAcid("MT2"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("MTR"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("MTU"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("MTY"));
    assertEquals("VAL", ResidueProperties.getCanonicalAminoAcid("MVA"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("N  "));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("N10"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("N2C"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("N5I"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("N5M"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("N6G"));
    assertEquals("PRO", ResidueProperties.getCanonicalAminoAcid("N7P"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("NA8"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("NAL"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("NAM"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("NB8"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("NBQ"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("NC1"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("NCB"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("NCX"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("NCY"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("NDF"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("NDN"));
    assertEquals("HIS", ResidueProperties.getCanonicalAminoAcid("NEM"));
    assertEquals("HIS", ResidueProperties.getCanonicalAminoAcid("NEP"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("NF2"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("NFA"));
    assertEquals("GLU", ResidueProperties.getCanonicalAminoAcid("NHL"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("NIT"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("NIY"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("NLE"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("NLN"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("NLO"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("NLP"));
    assertEquals("GLN", ResidueProperties.getCanonicalAminoAcid("NLQ"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("NMC"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("NMM"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("NMS"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("NMT"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("NNH"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("NP3"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("NPH"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("NPI"));
    assertEquals("LYG", ResidueProperties.getCanonicalAminoAcid("NRP"));
    assertEquals("MYG", ResidueProperties.getCanonicalAminoAcid("NRQ"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("NSK"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("NTY"));
    assertEquals("VAL", ResidueProperties.getCanonicalAminoAcid("NVA"));
    assertEquals("TWG", ResidueProperties.getCanonicalAminoAcid("NYC"));
    assertEquals("NYG", ResidueProperties.getCanonicalAminoAcid("NYG"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("NYM"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("NYS"));
    assertEquals("HIS", ResidueProperties.getCanonicalAminoAcid("NZH"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("O12"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("O2C"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("O2G"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("OAD"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("OAS"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("OBF"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("OBS"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("OCS"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("OCY"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("ODP"));
    assertEquals("HIS", ResidueProperties.getCanonicalAminoAcid("OHI"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("OHS"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("OIC"));
    assertEquals("ILE", ResidueProperties.getCanonicalAminoAcid("OIP"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("OLE"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("OLT"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("OLZ"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("OMC"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("OMG"));
    assertEquals("MET", ResidueProperties.getCanonicalAminoAcid("OMT"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("OMU"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("ONE"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("ONH"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("ONL"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("OPR"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("ORN"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("ORQ"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("OSE"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("OTB"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("OTH"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("OTY"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("OXX"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("P  "));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("P1L"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("P1P"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("P2T"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("P2U"));
    assertEquals("PRO", ResidueProperties.getCanonicalAminoAcid("P2Y"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("P5P"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("PAQ"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("PAS"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("PAT"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("PAU"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("PBB"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("PBF"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("PBT"));
    assertEquals("GLU", ResidueProperties.getCanonicalAminoAcid("PCA"));
    assertEquals("PRO", ResidueProperties.getCanonicalAminoAcid("PCC"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("PCE"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("PCS"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("PDL"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("PDU"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("PEC"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("PF5"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("PFF"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("PFX"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("PG1"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("PG7"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("PG9"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("PGL"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("PGN"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("PGP"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("PGY"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("PHA"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("PHD"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("PHE"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("PHI"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("PHL"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("PHM"));
    assertEquals("AYG", ResidueProperties.getCanonicalAminoAcid("PIA"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("PIV"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("PLE"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("PM3"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("PMT"));
    assertEquals("PRO", ResidueProperties.getCanonicalAminoAcid("POM"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("PPN"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("PPU"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("PPW"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("PQ1"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("PR3"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("PR5"));
    assertEquals("PRO", ResidueProperties.getCanonicalAminoAcid("PR9"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("PRN"));
    assertEquals("PRO", ResidueProperties.getCanonicalAminoAcid("PRO"));
    assertEquals("PRO", ResidueProperties.getCanonicalAminoAcid("PRS"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("PSA"));
    assertEquals("HIS", ResidueProperties.getCanonicalAminoAcid("PSH"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("PST"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("PSU"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("PSW"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("PTA"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("PTH"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("PTM"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("PTR"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("PU "));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("PUY"));
    assertEquals("HIS", ResidueProperties.getCanonicalAminoAcid("PVH"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("PVL"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("PYA"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("PYO"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("PYX"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("PYY"));
    assertEquals("QLG", ResidueProperties.getCanonicalAminoAcid("QLG"));
    assertEquals("GLN", ResidueProperties.getCanonicalAminoAcid("QMM"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("QPA"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("QPH"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("QUO"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("R  "));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("R1A"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("R4K"));
    assertEquals("HYG", ResidueProperties.getCanonicalAminoAcid("RC7"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("RE0"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("RE3"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("RIA"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("RMP"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("RON"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("RT "));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("RTP"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("S1H"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("S2C"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("S2D"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("S2M"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("S2P"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("S4A"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("S4C"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("S4G"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("S4U"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("S6G"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("SAC"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("SAH"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("SAR"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("SBL"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("SC "));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("SCH"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("SCS"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("SCY"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("SD2"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("SDG"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("SDP"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("SEB"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("SEC"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("SEG"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("SEL"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("SEM"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("SEN"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("SEP"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("SER"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("SET"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("SGB"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("SHC"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("SHP"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("SHR"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("SIB"));
    assertEquals("DC", ResidueProperties.getCanonicalAminoAcid("SIC")); // check
    assertEquals("PRO", ResidueProperties.getCanonicalAminoAcid("SLA"));
    assertEquals("PRO", ResidueProperties.getCanonicalAminoAcid("SLR"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("SLZ"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("SMC"));
    assertEquals("MET", ResidueProperties.getCanonicalAminoAcid("SME"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("SMF"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("SMP"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("SMT"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("SNC"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("SNN"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("SOC"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("SOS"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("SOY"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("SPT"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("SRA"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("SSU"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("STY"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("SUB"));
    assertEquals("DG", ResidueProperties.getCanonicalAminoAcid("SUI"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("SUN"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("SUR"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("SVA"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("SVV"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("SVW"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("SVX"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("SVY"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("SVZ"));
    assertEquals("SWG", ResidueProperties.getCanonicalAminoAcid("SWG"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("SYS"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("T  "));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("T11"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("T23"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("T2S"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("T2T"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("T31"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("T32"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("T36"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("T37"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("T38"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("T39"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("T3P"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("T41"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("T48"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("T49"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("T4S"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("T5O"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("T5S"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("T66"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("T6A"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("TA3"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("TA4"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("TAF"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("TAL"));
    assertEquals("ASP", ResidueProperties.getCanonicalAminoAcid("TAV"));
    assertEquals("VAL", ResidueProperties.getCanonicalAminoAcid("TBG"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("TBM"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("TC1"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("TCP"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("TCQ"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("TCR"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("TCY"));
    assertEquals("LEU", ResidueProperties.getCanonicalAminoAcid("TDD"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("TDY"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("TFE"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("TFO"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("TFQ"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("TFT"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("TGP"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("TH6"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("THC"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("THO"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("THR"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("THX"));
    assertEquals("ARG", ResidueProperties.getCanonicalAminoAcid("THZ"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("TIH"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("TLB"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("TLC"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("TLN"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("TMB"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("TMD"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("TNB"));
    assertEquals("SER", ResidueProperties.getCanonicalAminoAcid("TNR"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("TOX"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("TP1"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("TPC"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("TPG"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("TPH"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("TPL"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("TPO"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("TPQ"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("TQI"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("TQQ"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("TRF"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("TRG"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("TRN"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("TRO"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("TRP"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("TRQ"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("TRW"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("TRX"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("TS "));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("TST"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("TT "));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("TTD"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("TTI"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("TTM"));
    assertEquals("TRP", ResidueProperties.getCanonicalAminoAcid("TTQ"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("TTS"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("TY1"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("TY2"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("TY3"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("TY5"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("TYB"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("TYI"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("TYJ"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("TYN"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("TYO"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("TYQ"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("TYR"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("TYS"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("TYT"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("TYU"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("TYW"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("TYX"));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("TYY"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("TZB"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("TZO"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("U  "));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("U25"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("U2L"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("U2N"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("U2P"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("U31"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("U33"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("U34"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("U36"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("U37"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("U8U"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("UAR"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("UCL"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("UD5"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("UDP"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("UFP"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("UFR"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("UFT"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("UMA"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("UMP"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("UMS"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("UN1"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("UN2"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("UNK"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("UR3"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("URD"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("US1"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("US2"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("US3"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("US5"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("USM"));
    assertEquals("VAL", ResidueProperties.getCanonicalAminoAcid("VAD"));
    assertEquals("VAL", ResidueProperties.getCanonicalAminoAcid("VAF"));
    assertEquals("VAL", ResidueProperties.getCanonicalAminoAcid("VAL"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("VB1"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("VDL"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("VLL"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("VLM"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("VMS"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("VOL"));
    assertEquals("GYG", ResidueProperties.getCanonicalAminoAcid("WCR"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("X  "));
    assertEquals("GLU", ResidueProperties.getCanonicalAminoAcid("X2W"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("X4A"));
    assertEquals("AFG", ResidueProperties.getCanonicalAminoAcid("X9Q"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("XAD"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("XAE"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("XAL"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("XAR"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("XCL"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("XCN"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("XCP"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("XCR"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("XCS"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("XCT"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("XCY"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("XGA"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("XGL"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("XGR"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("XGU"));
    assertEquals("PRO", ResidueProperties.getCanonicalAminoAcid("XPR"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("XSN"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("XTH"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("XTL"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("XTR"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("XTS"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("XTY"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("XUA"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("XUG"));
    assertEquals("LYS", ResidueProperties.getCanonicalAminoAcid("XX1"));
    assertEquals("THG", ResidueProperties.getCanonicalAminoAcid("XXY"));
    assertEquals("DYG", ResidueProperties.getCanonicalAminoAcid("XYG"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("Y  "));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("YCM"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("YG "));
    assertEquals("TYR", ResidueProperties.getCanonicalAminoAcid("YOF"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("YRR"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("YYG"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("Z  "));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("Z01"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("ZAD"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("ZAL"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("ZBC"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("ZBU"));
    assertEquals("PHE", ResidueProperties.getCanonicalAminoAcid("ZCL"));
    assertEquals("CYS", ResidueProperties.getCanonicalAminoAcid("ZCY"));
    assertEquals("UR3", ResidueProperties.getCanonicalAminoAcid("ZDU"));
    assertEquals("XAA", ResidueProperties.getCanonicalAminoAcid("ZFB"));
    assertEquals("GLY", ResidueProperties.getCanonicalAminoAcid("ZGU"));
    assertEquals("ASN", ResidueProperties.getCanonicalAminoAcid("ZHP"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("ZTH"));
    assertEquals("THR", ResidueProperties.getCanonicalAminoAcid("ZU0"));
    assertEquals("ALA", ResidueProperties.getCanonicalAminoAcid("ZZJ"));

    assertEquals(null, ResidueProperties.getCanonicalAminoAcid(null));
  }

  @Test(groups = { "Functional" })
  public void testGetSingleCharacterCode()
  {
    assertEquals('0', ResidueProperties.getSingleCharacterCode(null));
    assertEquals('0', ResidueProperties.getSingleCharacterCode(null));
    assertEquals('0', ResidueProperties.getSingleCharacterCode(""));
    assertEquals('Q', ResidueProperties.getSingleCharacterCode("GLN"));
    assertEquals('Q', ResidueProperties.getSingleCharacterCode("Gln"));
    assertEquals('Q', ResidueProperties.getSingleCharacterCode("gln"));
  }

  @Test(groups = { "Functional" })
  public void testGetDssp3State()
  {
    assertNull(ResidueProperties.getDssp3state(null));
    assertEquals("", ResidueProperties.getDssp3state(""));
    String foo = "0123 []<>abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String bar = "                                    E  E HHH                 ";
    assertEquals(bar, ResidueProperties.getDssp3state(foo));
  }

  @Test(groups = { "Functional" })
  public void testPhysicoChemicalProperties()
  {
    checkProperty("aromatic", "FYWH-*");
    checkProperty("aliphatic", "IVL-*");
    checkProperty("tiny", "GAS-*");
    checkProperty("small", "VCTGACSDNP-*");
    checkProperty("charged", "HKRDE-*");
    checkProperty("negative", "DE-*");
    checkProperty("polar", "YWHRKTSNDEQ-*X");
    checkProperty("positive", "HKR-*");
    checkProperty("proline", "P-*");
    checkProperty("hydrophobic", "MILVFYWHKTGAC-*X");
  }

  /**
   * Verify that the residues in the list have the named property, and other
   * residues do not
   * 
   * @param property
   * @param residues
   */
  void checkProperty(String property, String residues)
  {
    Map<String, Integer> props = ResidueProperties.propHash.get(property);

    /*
     * assert residues have the property (value 1 in lookup)
     */
    for (char res : residues.toCharArray())
    {
      assertEquals(res + " should be " + property, 1,
              props.get(String.valueOf(res)).intValue());
    }

    /*
     * assert other residues do not (value 0 in lookup)
     */
    for (String res : ResidueProperties.aa)
    {
      if (!residues.contains(res))
      {
        Integer propValue = props.get(String.valueOf(res));

        if (propValue != null)
        {
          /*
           * conservation calculation assigns unexpected symbols
           * the same value as '-'; here we just check those which
           * explicitly do not have the property
           */
          assertEquals(res + " should not be " + property, 0,
                  propValue.intValue());
        }
      }
    }
  }
}
