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
package jalview.io;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import java.io.File;

import javax.swing.ImageIcon;

import org.testng.annotations.Test;

public class JalviewFileViewTest
{
  @Test(groups = "Functional")
  public void testGetImageIcon()
  {
    JalviewFileView jfv = new JalviewFileView();
    ImageIcon icon1 = jfv.getImageIcon("/images/file.png");
    ImageIcon icon2 = jfv.getImageIcon("/images/file.png");
    ImageIcon icon3 = jfv.getImageIcon("/images/dna.png");
    ImageIcon icon4 = jfv.getImageIcon("/images/dna.png");

    /*
     * verify a single image object is served per file path
     */
    assertNotNull(icon1);
    assertSame(icon1, icon2);
    assertNotNull(icon3);
    assertSame(icon3, icon4);
    assertNotSame(icon1, icon3);

    assertNull(jfv.getImageIcon("/images/nosuchfile.png"));
    assertNull(jfv.getImageIcon("images/file.png"));
  }

  @Test(groups = "Functional")
  public void testGetExtension()
  {
    assertEquals(JalviewFileView.getExtension(new File("text.txt")), "txt");
    assertEquals(JalviewFileView.getExtension(
            new File("/a/longer/file/path/text.png.TXT")), "txt");
    assertNull(JalviewFileView
            .getExtension(new File("/a/longer/file/path/text.")));
    assertNull(JalviewFileView
            .getExtension(new File("/a/longer/file/path/text")));
  }

  @Test(groups = "Functional")
  public void testGetTypeDescription()
  {
    JalviewFileView jfw = new JalviewFileView();
    assertEquals(jfw.getTypeDescription(new File("uniref50.fa")),
            "Fasta file");
    assertEquals(jfw.getTypeDescription(new File("uniref50.fasta")),
            "Fasta file");
    assertEquals(jfw.getTypeDescription(new File("uniref50.MFA")),
            "Fasta file");
    assertEquals(jfw.getTypeDescription(new File("uniref50.fastQ")),
            "Fasta file");
    assertEquals(jfw.getTypeDescription(new File("uniref50.pfam")),
            "PFAM file");
    assertEquals(jfw.getTypeDescription(new File("uniref50.stk")),
            "Stockholm file");
    assertEquals(jfw.getTypeDescription(new File("uniref50.sto")),
            "Stockholm file");
    assertEquals(jfw.getTypeDescription(new File("uniref50.pir")),
            "PIR file");
    assertEquals(jfw.getTypeDescription(new File("uniref50.blc")),
            "BLC file");
    assertEquals(jfw.getTypeDescription(new File("uniref50.amsa")),
            "AMSA file");
    assertEquals(jfw.getTypeDescription(new File("uniref50.html")),
            "HTML file");
    assertNull(jfw.getTypeDescription(new File("uniref50.htm")));
    assertEquals(jfw.getTypeDescription(new File("uniref50.xml")),
            "RNAML file");
    assertEquals(jfw.getTypeDescription(new File("uniref50.rnaml")),
            "RNAML file");
    assertEquals(jfw.getTypeDescription(new File("uniref50.json")),
            "JSON file");
    assertEquals(jfw.getTypeDescription(new File("uniref50.pileup")),
            "PileUp file");
    assertEquals(jfw.getTypeDescription(new File("uniref50.msf")),
            "MSF file");
    assertEquals(jfw.getTypeDescription(new File("uniref50.aln")),
            "Clustal file");
    assertEquals(jfw.getTypeDescription(new File("uniref50.phy")),
            "PHYLIP file");
    assertEquals(jfw.getTypeDescription(new File("uniref50.gff2")),
            "GFF or Jalview features file");
    assertEquals(jfw.getTypeDescription(new File("uniref50.gff3")),
            "GFF or Jalview features file");
    assertEquals(jfw.getTypeDescription(new File("uniref50.pdb")),
            "PDB file");
    assertEquals(jfw.getTypeDescription(new File("uniref50.ent")),
            "PDB file");
    assertEquals(jfw.getTypeDescription(new File("uniref50.cif")),
            "mmCIF file");
    assertEquals(jfw.getTypeDescription(new File("uniref50.jvp")),
            "Jalview file");
    assertEquals(jfw.getTypeDescription(new File("uniref50.jar")),
            "Jalview file (old)");
  }
}
