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

import jalview.datamodel.SequenceFeature;
import jalview.util.matcher.MatcherI;

/**
 * An interface for an object that can apply a match condition to a
 * SequenceFeature object
 * 
 * @author gmcarstairs
 */
public interface FeatureMatcherI
{
  /**
   * Answers true if the value provided for this matcher's key passes this
   * matcher's match condition
   * 
   * @param feature
   * @return
   */
  boolean matches(SequenceFeature feature);

  /**
   * Answers the attribute key this matcher operates on (or null if match is by
   * Label or Score)
   * 
   * @return
   */
  String[] getAttribute();

  /**
   * Answers true if match is against feature label (description), else false
   * 
   * @return
   */
  boolean isByLabel();

  /**
   * Answers true if match is against feature score, else false
   * 
   * @return
   */
  boolean isByScore();

  /**
   * Answers true if match is against a feature attribute (text or range)
   * 
   * @return
   */
  boolean isByAttribute();

  /**
   * Answers the match condition that is applied
   * 
   * @return
   */
  MatcherI getMatcher();

  /**
   * Answers a string representation of this object suitable for use when
   * persisting data, in a format that can be reliably read back. Any changes to
   * the format should be backwards compatible.
   */
  String toStableString();
}
