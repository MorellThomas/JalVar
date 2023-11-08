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

/**
 * An interface to describe a set of one or more feature matchers, where all
 * matchers are combined with either AND or OR
 * 
 * @author gmcarstairs
 *
 */
public interface FeatureMatcherSetI
{
  /**
   * Answers true if the feature provided passes this matcher's match condition
   * 
   * @param feature
   * @return
   */
  boolean matches(SequenceFeature feature);

  /**
   * Adds (ANDs) match condition m to this object's matcher set
   * 
   * @param m
   * @throws IllegalStateException
   *           if an attempt is made to AND to existing OR-ed conditions
   */
  void and(FeatureMatcherI m);

  /**
   * Answers true if any second condition is AND-ed with this one, false if it
   * is OR-ed
   * 
   * @return
   */
  boolean isAnded();

  /**
   * Adds (ORs) the given condition to this object's match conditions
   * 
   * @param m
   * @throws IllegalStateException
   *           if an attempt is made to OR to existing AND-ed conditions
   */
  void or(FeatureMatcherI m);

  /**
   * Answers an iterator over the combined match conditions
   * 
   * @return
   */
  Iterable<FeatureMatcherI> getMatchers();

  /**
   * Answers true if this object contains no conditions
   * 
   * @return
   */
  boolean isEmpty();

  /**
   * Answers a string representation of this object suitable for use when
   * persisting data, in a format that can be reliably read back. Any changes to
   * the format should be backwards compatible.
   */
  String toStableString();
}
