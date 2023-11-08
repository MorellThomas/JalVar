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

import java.util.HashMap;
import java.util.Map;

/**
 * A class to model one source of feature data, including metadata about
 * attributes of features
 * 
 * @author gmcarstairs
 *
 */
public class FeatureSource implements FeatureSourceI
{
  private String name;

  private Map<String, String> attributeNames;

  private Map<String, FeatureAttributeType> attributeTypes;

  /**
   * Constructor
   * 
   * @param theName
   */
  public FeatureSource(String theName)
  {
    this.name = theName;
    attributeNames = new HashMap<>();
    attributeTypes = new HashMap<>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName()
  {
    return name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getAttributeName(String attributeId)
  {
    return attributeNames.get(attributeId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FeatureAttributeType getAttributeType(String attributeId)
  {
    return attributeTypes.get(attributeId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setAttributeName(String id, String attName)
  {
    attributeNames.put(id, attName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setAttributeType(String id, FeatureAttributeType type)
  {
    attributeTypes.put(id, type);
  }

}
