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
package jalview.ws;

import jalview.ext.ensembl.EnsemblGene;
import jalview.ws.dbsources.EBIAlfaFold;
import jalview.ws.dbsources.EmblCdsSource;
import jalview.ws.dbsources.EmblSource;
import jalview.ws.dbsources.Pdb;
import jalview.ws.dbsources.PfamFull;
import jalview.ws.dbsources.PfamSeed;
import jalview.ws.dbsources.RfamSeed;
import jalview.ws.dbsources.TDBeacons;
import jalview.ws.dbsources.Uniprot;
import jalview.ws.seqfetcher.ASequenceFetcher;
import jalview.ws.seqfetcher.DbSourceProxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This implements the run-time discovery of sequence database clients.
 * 
 */
public class SequenceFetcher extends ASequenceFetcher
{
  /**
   * Thread safe construction of database proxies TODO: extend to a configurable
   * database plugin mechanism where classes are instantiated by reflection and
   * queried for their DbRefSource and version association.
   * 
   */
  public SequenceFetcher()
  {
    addDBRefSourceImpl(EnsemblGene.class);
    // addDBRefSourceImpl(EnsemblGenomes.class);
    addDBRefSourceImpl(EmblSource.class);
    addDBRefSourceImpl(EmblCdsSource.class);
    addDBRefSourceImpl(Uniprot.class);
    // not a sequence source yet
    // addDBRefSourceImpl(TDBeacons.class);
    addDBRefSourceImpl(Pdb.class);
    addDBRefSourceImpl(PfamFull.class);
    addDBRefSourceImpl(PfamSeed.class);
    addDBRefSourceImpl(RfamSeed.class);
    addDBRefSourceImpl(EBIAlfaFold.class);
  }

  /**
   * return an ordered list of database sources excluding alignment only
   * databases
   */
  public String[] getNonAlignmentSources()
  {
    String[] srcs = this.getSupportedDb();
    List<String> src = new ArrayList<>();

    for (int i = 0; i < srcs.length; i++)
    {
      boolean accept = true;
      for (DbSourceProxy dbs : getSourceProxy(srcs[i]))
      {
        // Skip the alignment databases for the moment - they're not useful for
        // verifying a single sequence against its reference source
        if (dbs.isAlignmentSource())
        {
          accept = false;
          break;
        }
      }
      if (accept)
      {
        src.add(srcs[i]);
      }
    }

    Collections.sort(src, String.CASE_INSENSITIVE_ORDER);
    return src.toArray(new String[src.size()]);
  }
}
