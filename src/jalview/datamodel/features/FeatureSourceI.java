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
package jalview.datamodel.features;

public interface FeatureSourceI
{
  /**
   * Answers a name for the feature source (not necessarily unique)
   * 
   * @return
   */
  String getName();

  /**
   * Answers the 'long name' of an attribute given its id (short name or
   * abbreviation), or null if not known
   * 
   * @param attributeId
   * @return
   */
  String getAttributeName(String attributeId);

  /**
   * Sets the 'long name' of an attribute given its id (short name or
   * abbreviation).
   * 
   * @param id
   * @param name
   */
  void setAttributeName(String id, String name);

  /**
   * Answers the datatype of the attribute with given id, or null if not known
   * 
   * @param attributeId
   * @return
   */
  FeatureAttributeType getAttributeType(String attributeId);

  /**
   * Sets the datatype of the attribute with given id
   * 
   * @param id
   * @param type
   */
  void setAttributeType(String id, FeatureAttributeType type);
}
