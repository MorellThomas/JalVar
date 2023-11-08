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

import java.util.HashSet;
import java.util.Set;

/**
 * Selected species identifiers used by Ensembl
 * 
 * @author gmcarstairs
 * @see http://rest.ensembl.org/info/species?content-type=text/xml
 */
enum Species
{
  /*
   * using any suitably readable alias as the enum name; these are all
   * valid species parameters to Ensembl REST services where applicable
   */
  human(true), mouse(true), s_cerevisiae(true), cow(false), pig(false),
  rattus_norvegicus(true), celegans(true), sheep(false), horse(false),
  gorilla(false), rabbit(false), gibbon(false), dog(false),
  orangutan(false), xenopus_tropicalis(true), chimpanzee(false), cat(false),
  zebrafish(true), chicken(true), drosophila_melanogaster(true);

  static Set<Species> modelOrganisms = new HashSet<>();

  static
  {
    for (Species s : values())
    {
      if (s.isModelOrganism())
      {
        modelOrganisms.add(s);
      }
    }
  }

  boolean modelOrganism;

  private Species(boolean model)
  {
    this.modelOrganism = model;
  }

  boolean isModelOrganism()
  {
    return modelOrganism;
  }

  public static Set<Species> getModelOrganisms()
  {
    return modelOrganisms;
  }
}
