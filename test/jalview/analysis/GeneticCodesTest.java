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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import java.util.Iterator;

import org.testng.annotations.Test;

public class GeneticCodesTest
{
  @Test(groups = "Functional")
  public void testGetCodeTable()
  {
    GeneticCodes codes = GeneticCodes.getInstance();
    assertEquals(codes.getStandardCodeTable().getName(), "Standard");
    assertEquals(codes.getStandardCodeTable().getId(), "1");
    assertSame(codes.getStandardCodeTable(), codes.getCodeTable("1"));
    assertEquals(codes.getCodeTable("2").getName(),
            "Vertebrate Mitochondrial");
    assertEquals(codes.getCodeTable("11").getName(),
            "Bacterial, Archaeal and Plant Plastid");
    assertEquals(codes.getCodeTable("31").getName(),
            "Blastocrithidia Nuclear");
  }

  @Test(groups = "Functional")
  public void testGetCodeTables()
  {
    GeneticCodes codes = GeneticCodes.getInstance();
    Iterator<GeneticCodeI> tableIterator = codes.getCodeTables().iterator();
    String[] ids = new String[] { "1", "2", "3", "4", "5", "6", "9", "10",
        "11", "12", "13", "14", "15", "16", "21", "22", "23", "24", "25",
        "26", "27", "28", "29", "30", "31" };
    for (int i = 0; i < ids.length; i++)
    {
      assertEquals(tableIterator.next().getId(), ids[i]);
    }
    assertFalse(tableIterator.hasNext());
  }

  @Test(groups = "Functional")
  public void testTranslate()
  {
    GeneticCodes codes = GeneticCodes.getInstance();

    GeneticCodeI gc = codes.getCodeTable("1");
    assertNull(gc.translate("XYZ"));
    assertEquals(gc.translate("AGA"), "R");

    gc = codes.getCodeTable("2");
    assertEquals(gc.translate("AGA"), "*"); // variant
    assertEquals(gc.translate("ttc"), "F"); // non-variant

    // table 11 has no variant translations - should serve the standard values
    gc = codes.getCodeTable("11");
    assertEquals(gc.translate("ttc"), "F");

    gc = codes.getCodeTable("31");
    assertEquals(gc.translate("TGA"), "W"); // variant
    assertEquals(gc.translate("tag"), "E"); // variant
    assertEquals(gc.translate("AGC"), "S"); // non-variant
  }

  /**
   * Test 'standard' codon translations (no ambiguity codes)
   */
  @Test(groups = { "Functional" })
  public void testTranslate_standardTable()
  {
    GeneticCodeI st = GeneticCodes.getInstance().getStandardCodeTable();
    assertEquals("F", st.translate("TTT"));
    assertEquals("F", st.translate("TTC"));
    assertEquals("L", st.translate("TTA"));
    assertEquals("L", st.translate("TTG"));
    assertEquals("L", st.translate("CTT"));
    assertEquals("L", st.translate("CTC"));
    assertEquals("L", st.translate("CTA"));
    assertEquals("L", st.translate("CTG"));
    assertEquals("I", st.translate("ATT"));
    assertEquals("I", st.translate("ATC"));
    assertEquals("I", st.translate("ATA"));
    assertEquals("M", st.translate("ATG"));
    assertEquals("V", st.translate("GTT"));
    assertEquals("V", st.translate("GTC"));
    assertEquals("V", st.translate("GTA"));
    assertEquals("V", st.translate("GTG"));
    assertEquals("S", st.translate("TCT"));
    assertEquals("S", st.translate("TCC"));
    assertEquals("S", st.translate("TCA"));
    assertEquals("S", st.translate("TCG"));
    assertEquals("P", st.translate("CCT"));
    assertEquals("P", st.translate("CCC"));
    assertEquals("P", st.translate("CCA"));
    assertEquals("P", st.translate("CCG"));
    assertEquals("T", st.translate("ACT"));
    assertEquals("T", st.translate("ACC"));
    assertEquals("T", st.translate("ACA"));
    assertEquals("T", st.translate("ACG"));
    assertEquals("A", st.translate("GCT"));
    assertEquals("A", st.translate("GCC"));
    assertEquals("A", st.translate("GCA"));
    assertEquals("A", st.translate("GCG"));
    assertEquals("Y", st.translate("TAT"));
    assertEquals("Y", st.translate("TAC"));
    assertEquals("*", st.translate("TAA"));
    assertEquals("*", st.translate("TAG"));
    assertEquals("H", st.translate("CAT"));
    assertEquals("H", st.translate("CAC"));
    assertEquals("Q", st.translate("CAA"));
    assertEquals("Q", st.translate("CAG"));
    assertEquals("N", st.translate("AAT"));
    assertEquals("N", st.translate("AAC"));
    assertEquals("K", st.translate("AAA"));
    assertEquals("K", st.translate("AAG"));
    assertEquals("D", st.translate("GAT"));
    assertEquals("D", st.translate("GAC"));
    assertEquals("E", st.translate("GAA"));
    assertEquals("E", st.translate("GAG"));
    assertEquals("C", st.translate("TGT"));
    assertEquals("C", st.translate("TGC"));
    assertEquals("*", st.translate("TGA"));
    assertEquals("W", st.translate("TGG"));
    assertEquals("R", st.translate("CGT"));
    assertEquals("R", st.translate("CGC"));
    assertEquals("R", st.translate("CGA"));
    assertEquals("R", st.translate("CGG"));
    assertEquals("S", st.translate("AGT"));
    assertEquals("S", st.translate("AGC"));
    assertEquals("R", st.translate("AGA"));
    assertEquals("R", st.translate("AGG"));
    assertEquals("G", st.translate("GGT"));
    assertEquals("G", st.translate("GGC"));
    assertEquals("G", st.translate("GGA"));
    assertEquals("G", st.translate("GGG"));
  }

  /**
   * Test a sample of codon translations involving ambiguity codes. Should
   * return a protein value where the ambiguity does not affect the translation.
   */
  @Test(groups = { "Functional" })
  public void testTranslate_standardTableAmbiguityCodes()
  {
    GeneticCodeI st = GeneticCodes.getInstance().getStandardCodeTable();
    // Y is C or T
    assertEquals("C", st.translate("TGY"));
    // Phenylalanine first base variation
    assertEquals("L", st.translate("YTA"));

    // W is A or T
    assertEquals("L", st.translate("CTW"));
    assertNull(st.translate("TTW"));

    // S is G or C
    assertEquals("G", st.translate("GGS"));
    assertNull(st.translate("ATS"));

    // K is T or G
    assertEquals("S", st.translate("TCK"));
    assertNull(st.translate("ATK"));

    // M is C or A
    assertEquals("T", st.translate("ACM"));
    // Arginine first base variation
    assertEquals("R", st.translate("MGA"));
    assertEquals("R", st.translate("MGG"));
    assertNull(st.translate("TAM"));

    // D is A, G or T
    assertEquals("P", st.translate("CCD"));
    assertNull(st.translate("AAD"));

    // V is A, C or G
    assertEquals("V", st.translate("GTV"));
    assertNull(st.translate("TTV"));

    // H is A, C or T
    assertEquals("A", st.translate("GCH"));
    assertEquals("I", st.translate("ATH"));
    assertNull(st.translate("AGH"));

    // B is C, G or T
    assertEquals("P", st.translate("CCB"));
    assertNull(st.translate("TAB"));

    // R is A or G
    // additional tests for JAL-1685 (resolved)
    assertEquals("L", st.translate("CTR"));
    assertEquals("V", st.translate("GTR"));
    assertEquals("S", st.translate("TCR"));
    assertEquals("P", st.translate("CCR"));
    assertEquals("T", st.translate("ACR"));
    assertEquals("A", st.translate("GCR"));
    assertEquals("R", st.translate("CGR"));
    assertEquals("G", st.translate("GGR"));
    assertEquals("R", st.translate("AGR"));
    assertEquals("E", st.translate("GAR"));
    assertEquals("K", st.translate("AAR"));
    assertEquals("L", st.translate("TTR"));
    assertEquals("Q", st.translate("CAR"));
    assertEquals("*", st.translate("TAR"));
    assertEquals("*", st.translate("TRA"));
    // Arginine first and third base ambiguity
    assertEquals("R", st.translate("MGR"));
    assertNull(st.translate("ATR"));

    // N is any base; 8 proteins accept any base in 3rd position
    assertEquals("L", st.translate("CTN"));
    assertEquals("V", st.translate("GTN"));
    assertEquals("S", st.translate("TCN"));
    assertEquals("P", st.translate("CCN"));
    assertEquals("T", st.translate("ACN"));
    assertEquals("A", st.translate("GCN"));
    assertEquals("R", st.translate("CGN"));
    assertEquals("G", st.translate("GGN"));
    assertNull(st.translate("ATN"));
    assertNull(st.translate("ANT"));
    assertNull(st.translate("NAT"));
    assertNull(st.translate("ANN"));
    assertNull(st.translate("NNA"));
    assertNull(st.translate("NNN"));

    // some random stuff
    assertNull(st.translate("YWB"));
    assertNull(st.translate("VHD"));
    assertNull(st.translate("WSK"));
  }

  /**
   * Test a sample of codon translations involving ambiguity codes. Should
   * return a protein value where the ambiguity does not affect the translation.
   */
  @Test(groups = { "Functional" })
  public void testTranslate_nonStandardTableAmbiguityCodes()
  {
    GeneticCodeI standard = GeneticCodes.getInstance()
            .getStandardCodeTable();

    /*
     * Vertebrate Mitochondrial (Table 2)
     */
    GeneticCodeI gc = GeneticCodes.getInstance().getCodeTable("2");
    // AGR is AGA or AGG - R in standard code, * in table 2
    assertEquals(gc.translate("AGR"), "*");
    assertEquals(standard.translate("AGR"), "R");
    // TGR is TGA or TGG - ambiguous in standard code, W in table 2
    assertEquals(gc.translate("TGR"), "W");
    assertNull(standard.translate("TGR"));

    /*
     * Yeast Mitochondrial (Table 3)
     */
    gc = GeneticCodes.getInstance().getCodeTable("3");
    // CTN is L in standard code, T in table 3
    assertEquals(gc.translate("ctn"), "T");
    assertEquals(standard.translate("CTN"), "L");

    /*
     * Alternative Yeast Nuclear (Table 12)
     */
    gc = GeneticCodes.getInstance().getCodeTable("12");
    // CTG is S; in the standard code CTN is L
    assertEquals(gc.translate("CTG"), "S");
    assertNull(gc.translate("CTK")); // K is G or T -> S or L
    assertEquals(standard.translate("CTK"), "L");
    assertEquals(gc.translate("CTH"), "L"); // H is anything other than G
    assertEquals(standard.translate("CTH"), "L");
    assertEquals(standard.translate("CTN"), "L");

    /*
     * Trematode Mitochondrial (Table 21)
     */
    gc = GeneticCodes.getInstance().getCodeTable("21");
    // AAR is K in standard code, ambiguous in table 21 as AAA=N not K
    assertNull(gc.translate("AAR"));
    assertEquals(standard.translate("AAR"), "K");
  }

  @Test(groups = "Functional")
  public void testTranslateCanonical()
  {
    GeneticCodes codes = GeneticCodes.getInstance();

    GeneticCodeI gc = codes.getCodeTable("1");
    assertNull(gc.translateCanonical("XYZ"));
    assertEquals(gc.translateCanonical("AGA"), "R");
    // translateCanonical should not resolve ambiguity codes
    assertNull(gc.translateCanonical("TGY"));

    gc = codes.getCodeTable("2");
    assertNull(gc.translateCanonical("AGR"));
    assertEquals(gc.translateCanonical("AGA"), "*"); // variant
    assertEquals(gc.translateCanonical("ttc"), "F"); // non-variant
  }
}
