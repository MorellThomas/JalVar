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

import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;

import java.util.ArrayList;
import java.util.List;

/**
 * A convenience class to simplify writing unit tests (pending Mockito or
 * similar?)
 */
public class EnsemblSeqProxyAdapter extends EnsemblSeqProxy
{
  /**
   * Default constructor (to use rest.ensembl.org)
   */
  public EnsemblSeqProxyAdapter()
  {
    super();
  }

  /**
   * Constructor given the target domain to fetch data from
   * 
   * @param d
   */
  public EnsemblSeqProxyAdapter(String d)
  {
    super(d);
  }

  @Override
  public String getDbName()
  {
    return null;
  }

  @Override
  protected EnsemblFeatureType[] getFeaturesToFetch()
  {
    return null;
  }

  @Override
  protected EnsemblSeqType getSourceEnsemblType()
  {
    return null;
  }

  @Override
  protected List<SequenceFeature> getIdentifyingFeatures(SequenceI seq,
          String accId)
  {
    return new ArrayList<>();
  }

}
