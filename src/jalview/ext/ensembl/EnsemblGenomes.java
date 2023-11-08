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
package jalview.ext.ensembl;

import jalview.bin.Cache;
import jalview.datamodel.DBRefSource;

/**
 * A class to behave much like EnsemblGene but referencing the ensemblgenomes
 * domain and data
 * 
 * @author gmcarstairs
 *
 */
public class EnsemblGenomes extends EnsemblGene
{
  /**
   * Constructor sets domain to rest.ensemblgenomes.org instead of the 'usual'
   * rest.ensembl.org
   */
  public EnsemblGenomes()
  {
    super();
    setDomain(Cache.getDefault(ENSEMBL_GENOMES_BASEURL,
            DEFAULT_ENSEMBL_GENOMES_BASEURL));
  }

  @Override
  public String getDbName()
  {
    return DBRefSource.ENSEMBLGENOMES;
  }

  @Override
  public String getTestQuery()
  {
    /*
     * Salmonella gene, Uniprot Q8Z9G6, EMBLCDS CAD01290
     */
    return "CAD01290";
  }

  @Override
  public String getDbSource()
  {
    return DBRefSource.ENSEMBLGENOMES;
  }

}
