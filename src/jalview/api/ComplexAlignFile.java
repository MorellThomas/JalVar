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
package jalview.api;

import jalview.datamodel.HiddenColumns;
import jalview.datamodel.SequenceI;

/**
 * This interface should be implemented by complex file parser with the ability
 * to store linked data and complex view states in addition to Alignment data
 * 
 *
 */
public interface ComplexAlignFile
{
  /**
   * Determines if Sequence features should be shown
   * 
   * @return
   */
  public boolean isShowSeqFeatures();

  /**
   * Obtains the colour scheme from a complex file parser
   * 
   * @return
   */
  public String getGlobalColourScheme();

  /**
   * Retrieves the Column selection/hidden column from a complex file parser
   * 
   * @return
   */
  public HiddenColumns getHiddenColumns();

  /**
   * Retrieves hidden sequences from a complex file parser
   * 
   * @return
   */
  public SequenceI[] getHiddenSequences();

  /**
   * Retrieves displayed features from a complex file parser
   * 
   * @return
   */
  public FeaturesDisplayedI getDisplayedFeatures();
}
