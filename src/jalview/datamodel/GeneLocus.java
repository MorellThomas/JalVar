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
 * A specialisation of DBRefEntry used to hold the chromosomal coordinates for a
 * (typically gene) sequence
 * <ul>
 * <li>field <code>source</code> is used to hold a species id e.g. human</li>
 * <li>field <code>version</code> is used to hold assembly id e.g GRCh38</li>
 * <li>field <code>accession</code> is used to hold the chromosome id</li>
 * <li>field <code>map</code> is used to hold the mapping from sequence to
 * chromosome coordinates</li>
 * </ul>
 * 
 * @author gmcarstairs
 *
 */
public class GeneLocus extends DBRefEntry implements GeneLociI
{
  /**
   * Constructor adapts species, assembly, chromosome to DBRefEntry source,
   * version, accession, respectively, and saves the mapping of sequence to
   * chromosomal coordinates
   * 
   * @param speciesId
   * @param assemblyId
   * @param chromosomeId
   * @param mapping
   */
  public GeneLocus(String speciesId, String assemblyId, String chromosomeId,
          Mapping mapping)
  {
    super(speciesId, assemblyId, chromosomeId, mapping);
  }

  /**
   * Constructor
   * 
   * @param speciesId
   * @param assemblyId
   * @param chromosomeId
   */
  public GeneLocus(String speciesId, String assemblyId, String chromosomeId)
  {
    this(speciesId, assemblyId, chromosomeId, null);
  }

  @Override
  public boolean equals(Object o)
  {
    return o instanceof GeneLocus && super.equals(o);
  }

  @Override
  public MapList getMapping()
  {
    return map == null ? null : map.getMap();
  }

  /**
   * Answers the species identifier e.g. "human", stored as field
   * <code>source</code> of DBRefEntry
   */
  @Override
  public String getSpeciesId()
  {
    return getSource();
  }

  /**
   * Answers the genome assembly id e.g. "GRCh38", stored as field
   * <code>version</code> of DBRefEntry
   */
  @Override
  public String getAssemblyId()
  {
    return getVersion();
  }

  /**
   * Answers the chromosome identifier e.g. "X", stored as field
   * <code>accession</code> of DBRefEntry
   */
  @Override
  public String getChromosomeId()
  {
    return getAccessionId();
  }

}
