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

/**
 * A set of feature property constants used by jalview
 * 
 * @author JimP
 * 
 */
public class FeatureProperties
{
  public static final String EMBL_CODING_FEATURE = "CDS";

  public static final String EXONPOS = "exon number";

  public static final String EXONPRODUCT = "product";

  /**
   * lookup feature type for a particular database to see if its a coding region
   * feature
   * 
   * @param dbrefsource
   * @param string
   * @return
   */
  public static boolean isCodingFeature(String dbrefsource, String type)
  {
    if (type.equalsIgnoreCase(EMBL_CODING_FEATURE))
    {
      return (dbrefsource == null
              || dbrefsource.equalsIgnoreCase(DBRefSource.EMBL)
              || dbrefsource.equalsIgnoreCase(DBRefSource.EMBLCDS));
    }
    return false;
  }

  /**
   * Returns the coding feature name for a database source. Currently just
   * hard-coded to return CDS for EMBL/EMBLCDS, else null.
   * 
   * @param dbrefsource
   * @return
   */
  public static String getCodingFeature(String dbrefsource)
  {
    if (DBRefSource.EMBL.equalsIgnoreCase(dbrefsource)
            || DBRefSource.EMBLCDS.equalsIgnoreCase(dbrefsource))
    {
      return EMBL_CODING_FEATURE;
    }
    return null;
  }
}
