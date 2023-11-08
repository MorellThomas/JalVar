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
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class JalviewColourSchemeTest
{
  @Test(groups = "Functional")
  public void testGetSchemeClass()
  {
    assertTrue(JalviewColourScheme.Clustal
            .getSchemeClass() == ClustalxColourScheme.class);
    assertTrue(JalviewColourScheme.Blosum62
            .getSchemeClass() == Blosum62ColourScheme.class);
    assertTrue(JalviewColourScheme.PID
            .getSchemeClass() == PIDColourScheme.class);
    assertTrue(JalviewColourScheme.Hydrophobic
            .getSchemeClass() == HydrophobicColourScheme.class);
    assertTrue(JalviewColourScheme.Zappo
            .getSchemeClass() == ZappoColourScheme.class);
    assertTrue(JalviewColourScheme.Taylor
            .getSchemeClass() == TaylorColourScheme.class);
    assertTrue(JalviewColourScheme.Helix
            .getSchemeClass() == HelixColourScheme.class);
    assertTrue(JalviewColourScheme.Strand
            .getSchemeClass() == StrandColourScheme.class);
    assertTrue(JalviewColourScheme.Turn
            .getSchemeClass() == TurnColourScheme.class);
    assertTrue(JalviewColourScheme.Buried
            .getSchemeClass() == BuriedColourScheme.class);
    assertTrue(JalviewColourScheme.Nucleotide
            .getSchemeClass() == NucleotideColourScheme.class);
    assertTrue(JalviewColourScheme.PurinePyrimidine
            .getSchemeClass() == PurinePyrimidineColourScheme.class);
    assertTrue(JalviewColourScheme.TCoffee
            .getSchemeClass() == TCoffeeColourScheme.class);
    assertTrue(JalviewColourScheme.RNAHelices
            .getSchemeClass() == RNAHelicesColour.class);
    assertTrue(JalviewColourScheme.IdColour
            .getSchemeClass() == IdColourScheme.class);
  }

  @Test(groups = "Functional")
  public void testToString()
  {
    assertEquals(JalviewColourScheme.Clustal.toString(), "Clustal");
    assertEquals(JalviewColourScheme.Blosum62.toString(), "Blosum62");
    assertEquals(JalviewColourScheme.PID.toString(), "% Identity");
    assertEquals(JalviewColourScheme.Zappo.toString(), "Zappo");
    assertEquals(JalviewColourScheme.Taylor.toString(), "Taylor");
    assertEquals(JalviewColourScheme.Hydrophobic.toString(), "Hydrophobic");
    assertEquals(JalviewColourScheme.Helix.toString(), "Helix Propensity");
    assertEquals(JalviewColourScheme.Strand.toString(),
            "Strand Propensity");
    assertEquals(JalviewColourScheme.Turn.toString(), "Turn Propensity");
    assertEquals(JalviewColourScheme.Buried.toString(), "Buried Index");
    assertEquals(JalviewColourScheme.Nucleotide.toString(), "Nucleotide");
    assertEquals(JalviewColourScheme.PurinePyrimidine.toString(),
            "Purine/Pyrimidine");
    assertEquals(JalviewColourScheme.TCoffee.toString(), "T-Coffee Scores");
    assertEquals(JalviewColourScheme.RNAHelices.toString(), "RNA Helices");
    assertEquals(JalviewColourScheme.IdColour.toString(), "Sequence ID");
  }
}
