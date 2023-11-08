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

public interface GeneticCodeI
{
  /**
   * Answers the single letter amino acid code (e.g. "D") for the given codon
   * (e.g. "GAC"), or "*" for a stop codon, or null for an unknown input. The
   * codon is not case-sensitive, the return value is upper case.
   * <p>
   * If the codon includes any of the standard ambiguity codes
   * <ul>
   * <li>if all possible translations are the same, returns that value</li>
   * <li>else returns null</li>
   * </ul>
   * 
   * @param codon
   * @return
   */
  String translate(String codon);

  /**
   * Answers the single letter amino acid code (e.g. "D") for the given codon
   * (e.g. "GAC"), or "*" for a stop codon, or null for an unknown input. The
   * codon is not case-sensitive, the return value is upper case. If the codon
   * includes any of the standard ambiguity codes, this method returns null.
   * 
   * @param codon
   * @return
   */
  String translateCanonical(String codon);

  /**
   * Answers a unique identifier for the genetic code (using the numbering
   * system as on NCBI)
   * 
   * @return
   */
  String getId();

  /**
   * Answers a display name suitable for use in menus, reports etc
   * 
   * @return
   */
  String getName();
}
