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
package jalview.io.gff;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

public class SequenceOntologyLiteTest
{
  @Test(groups = "Functional")
  public void testIsA_sequenceVariant()
  {
    SequenceOntologyI so = new SequenceOntologyLite();

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
    assertTrue(so.isA("splice_region_variant", "sequence_variant"));
  }
}
