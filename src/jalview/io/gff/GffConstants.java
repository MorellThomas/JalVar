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

/**
 * A class to hold constants shared by creators and consumers of GFF or feature
 * data
 */
public class GffConstants
{
  // SequenceOntology terms are to be found in SequenceOntologyI

  /*
   * clinical_significance may be an attribute of 
   * sequence_variant data from Ensembl
   */
  public static final String CLINICAL_SIGNIFICANCE = "clinical_significance";

  /*
   * not instantiable
   */
  private GffConstants()
  {
  }
}
