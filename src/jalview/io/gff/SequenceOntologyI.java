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

import java.util.List;

public interface SequenceOntologyI
{
  /*
   * selected commonly used values for quick reference
   */
  // SO:0000104
  public static final String POLYPEPTIDE = "polypeptide";

  // SO:0000349
  public static final String PROTEIN_MATCH = "protein_match";

  // SO:0000347
  public static final String NUCLEOTIDE_MATCH = "nucleotide_match";

  // SO:0000316
  public static final String CDS = "CDS";

  // SO:0001060
  public static final String SEQUENCE_VARIANT = "sequence_variant";

  // SO:0001819
  public static final String SYNONYMOUS_VARIANT = "synonymous_variant";

  // SO:0001992
  public static final String NONSYNONYMOUS_VARIANT = "nonsynonymous_variant";

  // SO:0001587
  public static final String STOP_GAINED = "stop_gained";

  // SO:0000147
  public static final String EXON = "exon";

  // SO:0000673
  public static final String TRANSCRIPT = "transcript";

  // SO:0001621 isA sequence_variant but used in Ensembl as a transcript
  public static final String NMD_TRANSCRIPT_VARIANT = "NMD_transcript_variant";

  // SO:0000704
  public static final String GENE = "gene";

  public boolean isA(String childTerm, String parentTerm);

  /**
   * Returns a sorted list of all valid terms queried for (i.e. terms processed
   * which were valid in the SO), using the friendly description.
   * 
   * This can be used to check that any hard-coded stand-in for the full SO
   * includes all the terms needed for correct processing.
   * 
   * @return
   */
  public List<String> termsFound();

  /**
   * Returns a sorted list of all invalid terms queried for (i.e. terms
   * processed which were not found in the SO), using the friendly description.
   * 
   * This can be used to report any 'non-compliance' in data, and/or to report
   * valid terms missing from any hard-coded stand-in for the full SO.
   * 
   * @return
   */
  public List<String> termsNotFound();
}
