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
package jalview.datamodel;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import jalview.gui.JvOptionPane;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MappingTypeTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = "Functional")
  public void testGetInverse()
  {
    assertSame(MappingType.PeptideToNucleotide,
            MappingType.NucleotideToPeptide.getInverse());
    assertSame(MappingType.NucleotideToPeptide,
            MappingType.PeptideToNucleotide.getInverse());
    assertSame(MappingType.NucleotideToNucleotide,
            MappingType.NucleotideToNucleotide.getInverse());
    assertSame(MappingType.PeptideToPeptide,
            MappingType.PeptideToPeptide.getInverse());
  }

  @Test(groups = "Functional")
  public void testGetFromRatio()
  {
    assertEquals(1, MappingType.NucleotideToNucleotide.getFromRatio());
    assertEquals(1, MappingType.PeptideToNucleotide.getFromRatio());
    assertEquals(1, MappingType.PeptideToPeptide.getFromRatio());
    assertEquals(3, MappingType.NucleotideToPeptide.getFromRatio());
  }

  @Test(groups = "Functional")
  public void testGetToRatio()
  {
    assertEquals(1, MappingType.NucleotideToNucleotide.getToRatio());
    assertEquals(3, MappingType.PeptideToNucleotide.getToRatio());
    assertEquals(1, MappingType.PeptideToPeptide.getToRatio());
    assertEquals(1, MappingType.NucleotideToPeptide.getToRatio());
  }
}
