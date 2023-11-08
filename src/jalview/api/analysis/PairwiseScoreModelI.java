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
package jalview.api.analysis;

/**
 * An interface that describes classes that can compute similarity (aka
 * substitution) scores for pairs of residues
 */
public interface PairwiseScoreModelI
{
  /**
   * Answers a similarity score between two sequence characters (for
   * substitution of the first by the second). Typically the highest scores are
   * for identity, and the lowest for substitution of a residue by one with very
   * different properties.
   * 
   * @param c
   * @param d
   * @return
   */
  abstract public float getPairwiseScore(char c, char d);
  // TODO make this static when Java 8

}
