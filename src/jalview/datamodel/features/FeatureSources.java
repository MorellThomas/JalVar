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
 * A singleton to hold metadata about feature attributes, keyed by a unique
 * feature source identifier
 * 
 * @author gmcarstairs
 *
 */
public class FeatureSources
{
  private static FeatureSources instance = new FeatureSources();

  private Map<String, FeatureSourceI> sources;

  /**
   * Answers the singleton instance of this class
   * 
   * @return
   */
  public static FeatureSources getInstance()
  {
    return instance;
  }

  private FeatureSources()
  {
    sources = new HashMap<>();
  }

  /**
   * Answers the FeatureSource with the given unique identifier, or null if not
   * known
   * 
   * @param sourceId
   * @return
   */
  public FeatureSourceI getSource(String sourceId)
  {
    return sources.get(sourceId);
  }

  /**
   * Adds the given source under the given key. This will replace any existing
   * source with the same id, it is the caller's responsibility to ensure keys
   * are unique if necessary.
   * 
   * @param sourceId
   * @param source
   */
  public void addSource(String sourceId, FeatureSource source)
  {
    sources.put(sourceId, source);
  }
}
