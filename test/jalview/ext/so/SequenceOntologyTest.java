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
package jalview.ext.so;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import jalview.gui.JvOptionPane;
import jalview.io.gff.SequenceOntologyI;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SequenceOntologyTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  private SequenceOntologyI so;

  @BeforeClass(alwaysRun = true)
  public void setUp()
  {
    long now = System.currentTimeMillis();
    try
    {
      so = new SequenceOntology();
    } catch (Throwable t)
    {
      System.out.println("SOTest error ");
      t.printStackTrace(System.err);
    }
    long elapsed = System.currentTimeMillis() - now;
    System.out.println(
            "Load and cache of Sequence Ontology took " + elapsed + "ms");
  }

  @Test(groups = "Functional")
  public void testIsA()
  {
    assertFalse(so.isA(null, null));
    assertFalse(so.isA(null, "SO:0000087"));
    assertFalse(so.isA("SO:0000087", null));
    assertFalse(so.isA("complete", "garbage"));

    assertTrue(so.isA("SO:0000087", "SO:0000704"));
    assertFalse(so.isA("SO:0000704", "SO:0000087"));
    assertTrue(so.isA("SO:0000736", "SO:0000735"));

    // same thing:
    assertTrue(so.isA("micronuclear_sequence", "micronuclear_sequence"));
    // direct parent:
    assertTrue(so.isA("micronuclear_sequence", "organelle_sequence"));
    // grandparent:
    assertTrue(so.isA("micronuclear_sequence", "sequence_location"));
    // great-grandparent:
    assertTrue(so.isA("micronuclear_sequence", "sequence_attribute"));

    // same thing by name / description:
    assertTrue(so.isA("micronuclear_sequence", "SO:0000084"));
    assertTrue(so.isA("SO:0000084", "micronuclear_sequence"));
    assertTrue(so.isA("SO:0000084", "SO:0000084"));

    // SO name to description:
    assertTrue(so.isA("SO:0000084", "organelle_sequence"));
    assertTrue(so.isA("SO:0000084", "sequence_location"));
    assertTrue(so.isA("SO:0000084", "sequence_attribute"));

    // description to SO name:
    assertTrue(so.isA("micronuclear_sequence", "SO:0000736"));
    assertTrue(so.isA("micronuclear_sequence", "SO:0000735"));
    assertTrue(so.isA("micronuclear_sequence", "SO:0000400"));
  }

  @Test(groups = "Functional")
  public void testIsCDS()
  {
    assertTrue(so.isA("CDS", "CDS"));
    assertTrue(so.isA("CDS_predicted", "CDS"));
    assertTrue(so.isA("transposable_element_CDS", "CDS"));
    assertTrue(so.isA("edited_CDS", "CDS"));
    assertTrue(so.isA("CDS_independently_known", "CDS"));
    assertTrue(so.isA("CDS_fragment", "CDS"));
    assertFalse(so.isA("CDS_region", "CDS"));// part_of
    assertFalse(so.isA("polypeptide", "CDS")); // derives_from
  }

  @Test(groups = "Functional")
  public void testIsSequenceVariant()
  {
    assertFalse(so.isA("CDS", "sequence_variant"));
    assertTrue(so.isA("sequence_variant", "sequence_variant"));

    /*
     * these should all be sub-types of sequence_variant
     */
    assertTrue(so.isA("structural_variant", "sequence_variant"));
    assertTrue(so.isA("feature_variant", "sequence_variant"));
    assertTrue(so.isA("gene_variant", "sequence_variant"));
    assertTrue(so.isA("transcript_variant", "sequence_variant"));
    assertTrue(so.isA("NMD_transcript_variant", "sequence_variant"));
    assertTrue(so.isA("missense_variant", "sequence_variant"));
    assertTrue(so.isA("synonymous_variant", "sequence_variant"));
    assertTrue(so.isA("frameshift_variant", "sequence_variant"));
    assertTrue(so.isA("5_prime_UTR_variant", "sequence_variant"));
    assertTrue(so.isA("3_prime_UTR_variant", "sequence_variant"));
    assertTrue(so.isA("stop_gained", "sequence_variant"));
    assertTrue(so.isA("stop_lost", "sequence_variant"));
    assertTrue(so.isA("inframe_deletion", "sequence_variant"));
    assertTrue(so.isA("inframe_insertion", "sequence_variant"));
  }
}
