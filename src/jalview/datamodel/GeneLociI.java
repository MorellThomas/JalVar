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

import jalview.util.MapList;

/**
 * An interface to model one or more contiguous regions on one chromosome
 */
public interface GeneLociI
{
  /**
   * Answers the species identifier
   * 
   * @return
   */
  String getSpeciesId();

  /**
   * Answers the reference assembly identifier
   * 
   * @return
   */
  String getAssemblyId();

  /**
   * Answers the chromosome identifier e.g. "2", "Y", "II"
   * 
   * @return
   */
  String getChromosomeId();

  /**
   * Answers the mapping from sequence to chromosome loci. For a reverse strand
   * mapping, the chromosomal ranges will have start > end.
   * 
   * @return
   */
  MapList getMapping();
}
