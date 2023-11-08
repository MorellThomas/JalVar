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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;

import java.awt.Color;

import org.testng.annotations.Test;

public class ColourSchemePropertyTest
{
  @Test(groups = "Functional")
  public void testGetColourName()
  {
    SequenceI seq = new Sequence("Seq1", "abcd");
    AlignmentI al = new Alignment(new SequenceI[] { seq });
    ColourSchemeI cs = new ClustalxColourScheme(al, null);
    assertEquals(ColourSchemeProperty.getColourName(cs), "Clustal");
    cs = new Blosum62ColourScheme();
    assertEquals(ColourSchemeProperty.getColourName(cs), "Blosum62");
    cs = new PIDColourScheme();
    assertEquals(ColourSchemeProperty.getColourName(cs), "% Identity");
    cs = new HydrophobicColourScheme();
    assertEquals(ColourSchemeProperty.getColourName(cs), "Hydrophobic");
    cs = new ZappoColourScheme();
    assertEquals(ColourSchemeProperty.getColourName(cs), "Zappo");
    cs = new TaylorColourScheme();
    assertEquals(ColourSchemeProperty.getColourName(cs), "Taylor");
    cs = new HelixColourScheme();
    assertEquals(ColourSchemeProperty.getColourName(cs),
            "Helix Propensity");
    cs = new StrandColourScheme();
    assertEquals(ColourSchemeProperty.getColourName(cs),
            "Strand Propensity");
    cs = new TurnColourScheme();
    assertEquals(ColourSchemeProperty.getColourName(cs), "Turn Propensity");
    cs = new BuriedColourScheme();
    assertEquals(ColourSchemeProperty.getColourName(cs), "Buried Index");
    cs = new NucleotideColourScheme();
    assertEquals(ColourSchemeProperty.getColourName(cs), "Nucleotide");
    cs = new PurinePyrimidineColourScheme();
    assertEquals(ColourSchemeProperty.getColourName(cs),
            "Purine/Pyrimidine");
    cs = new TCoffeeColourScheme(al);
    assertEquals(ColourSchemeProperty.getColourName(cs), "T-Coffee Scores");
    cs = new RNAHelicesColour(al);
    assertEquals(ColourSchemeProperty.getColourName(cs), "RNA Helices");
    cs = new RNAInteractionColourScheme();
    assertEquals(ColourSchemeProperty.getColourName(cs),
            "RNA Interaction type");
    cs = new UserColourScheme();
    assertEquals(ColourSchemeProperty.getColourName(cs), "User Defined");

    /*
     * UserColourScheme may have a bespoke name
     */
    ((UserColourScheme) cs).setName("stripy");
    assertEquals(ColourSchemeProperty.getColourName(cs), "stripy");
    ((UserColourScheme) cs).setName("");
    assertEquals(ColourSchemeProperty.getColourName(cs), "User Defined");
    ((UserColourScheme) cs).setName(null);
    assertEquals(ColourSchemeProperty.getColourName(cs), "User Defined");

    assertEquals(ColourSchemeProperty.getColourName(null), "None");
  }

  @Test(groups = "Functional")
  public void testGetColourScheme()
  {
    SequenceI seq = new Sequence("Seq1", "abcd");
    AlignmentI al = new Alignment(new SequenceI[] { seq });
    // the strings here correspond to JalviewColourScheme.toString() values
    ColourSchemeI cs = ColourSchemeProperty.getColourScheme(null, al,
            "Clustal");
    assertTrue(cs instanceof ClustalxColourScheme);
    // not case-sensitive
    cs = ColourSchemeProperty.getColourScheme(null, al, "CLUSTAL");
    assertTrue(cs instanceof ClustalxColourScheme);
    cs = ColourSchemeProperty.getColourScheme(null, al, "clustal");
    assertTrue(cs instanceof ClustalxColourScheme);
    cs = ColourSchemeProperty.getColourScheme(null, al, "Blosum62");
    assertTrue(cs instanceof Blosum62ColourScheme);
    cs = ColourSchemeProperty.getColourScheme(null, al, "% Identity");
    assertTrue(cs instanceof PIDColourScheme);
    cs = ColourSchemeProperty.getColourScheme(null, al, "Zappo");
    assertTrue(cs instanceof ZappoColourScheme);
    cs = ColourSchemeProperty.getColourScheme(null, al, "Taylor");
    assertTrue(cs instanceof TaylorColourScheme);
    cs = ColourSchemeProperty.getColourScheme(null, al, "Hydrophobic");
    assertTrue(cs instanceof HydrophobicColourScheme);
    cs = ColourSchemeProperty.getColourScheme(null, al, "Helix Propensity");
    assertTrue(cs instanceof HelixColourScheme);
    cs = ColourSchemeProperty.getColourScheme(null, al,
            "Strand Propensity");
    assertTrue(cs instanceof StrandColourScheme);
    cs = ColourSchemeProperty.getColourScheme(null, al, "Turn Propensity");
    assertTrue(cs instanceof TurnColourScheme);
    cs = ColourSchemeProperty.getColourScheme(null, al, "Buried Index");
    assertTrue(cs instanceof BuriedColourScheme);
    cs = ColourSchemeProperty.getColourScheme(null, al, "Nucleotide");
    assertTrue(cs instanceof NucleotideColourScheme);
    cs = ColourSchemeProperty.getColourScheme(null, al,
            "Purine/Pyrimidine");
    assertTrue(cs instanceof PurinePyrimidineColourScheme);
    cs = ColourSchemeProperty.getColourScheme(null, al, "T-Coffee Scores");
    assertTrue(cs instanceof TCoffeeColourScheme);
    cs = ColourSchemeProperty.getColourScheme(null, al, "RNA Helices");
    assertTrue(cs instanceof RNAHelicesColour);
    // 'None' is a special value
    assertNull(ColourSchemeProperty.getColourScheme(null, al, "None"));
    assertNull(ColourSchemeProperty.getColourScheme(null, al, "none"));
    // default is to convert the name into a fixed colour
    cs = ColourSchemeProperty.getColourScheme(null, al, "elephants");
    assertTrue(cs instanceof UserColourScheme);

    /*
     * explicit aa colours
     */
    UserColourScheme ucs = (UserColourScheme) ColourSchemeProperty
            .getColourScheme(null, al,
                    "R,G=red;C=blue;c=green;Q=10,20,30;S,T=11ffdd");
    assertEquals(ucs.findColour('H'), Color.white);
    assertEquals(ucs.findColour('R'), Color.red);
    assertEquals(ucs.findColour('r'), Color.red);
    assertEquals(ucs.findColour('G'), Color.red);
    assertEquals(ucs.findColour('C'), Color.blue);
    assertEquals(ucs.findColour('c'), Color.green);
    assertEquals(ucs.findColour('Q'), new Color(10, 20, 30));
    assertEquals(ucs.findColour('S'), new Color(0x11ffdd));
    assertEquals(ucs.findColour('T'), new Color(0x11ffdd));
  }
}
