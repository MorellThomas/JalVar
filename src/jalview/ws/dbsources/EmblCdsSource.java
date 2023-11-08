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
package jalview.ws.dbsources;

import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefSource;

public class EmblCdsSource extends EmblFlatfileSource // was EmblXmlSource
{

  public EmblCdsSource()
  {
    super();
  }

  @Override
  public String getDbSource()
  {
    return DBRefSource.EMBLCDS;
  }

  @Override
  public AlignmentI getSequenceRecords(String queries) throws Exception
  {
    if (queries.indexOf(".") > -1)
    {
      queries = queries.substring(0, queries.indexOf("."));
    }
    return getEmblSequenceRecords(DBRefSource.EMBLCDS, queries);
  }

  /**
   * cDNA for LDHA_CHICK swissprot sequence
   */
  @Override
  public String getTestQuery()
  {
    return "CAA37824";
  }

  @Override
  public String getDbName()
  {
    return "EMBL (CDS)";
  }

}
