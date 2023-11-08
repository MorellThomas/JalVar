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

import com.stevesoft.pat.Regex;

/**
 * Test class for accessing GeneDB - not yet finished.
 * 
 * @author JimP
 * 
 */
public class GeneDbSource extends EmblXmlSource
{

  public GeneDbSource()
  {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getAccessionSeparator()
   */
  @Override
  public String getAccessionSeparator()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getAccessionValidator()
   */
  @Override
  public Regex getAccessionValidator()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getDbSource()
   */
  @Override
  public String getDbSource()
  {
    return DBRefSource.GENEDB;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getDbVersion()
   */
  @Override
  public String getDbVersion()
  {
    // TODO Auto-generated method stub
    return "0";
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#getSequenceRecords(java.lang.String[])
   */
  @Override
  public AlignmentI getSequenceRecords(String queries) throws Exception
  {
    // query of form
    // http://www.genedb.org/genedb/ArtemisFormHandler?id=&dest=EMBL
    //
    return getEmblSequenceRecords(DBRefSource.GENEDB, queries);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.DbSourceProxy#isValidReference(java.lang.String)
   */
  @Override
  public boolean isValidReference(String accession)
  {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * return T.Brucei Mannosyl-Transferase TbPIG-M
   */
  @Override
  public String getTestQuery()
  {
    return "Tb927.6.3300";
  }

  @Override
  public String getDbName()
  {
    return "GeneDB"; // getDbSource();
  }

  @Override
  public int getTier()
  {
    return 0;
  }
}
