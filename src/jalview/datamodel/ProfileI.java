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

public interface ProfileI
{

  /**
   * Set the full profile of counts
   * 
   * @param residueCounts
   */
  public abstract void setCounts(ResidueCount residueCounts);

  /**
   * Returns the percentage identity of the profile, i.e. the highest proportion
   * of conserved (equal) symbols. The percentage is as a fraction of all
   * sequences, or only ungapped sequences if flag ignoreGaps is set true.
   * 
   * @param ignoreGaps
   * @return
   */
  public abstract float getPercentageIdentity(boolean ignoreGaps);

  /**
   * Returns the full symbol counts for this profile
   * 
   * @return
   */
  public abstract ResidueCount getCounts();

  /**
   * Returns the number of sequences in the profile
   * 
   * @return
   */
  public abstract int getHeight();

  /**
   * Returns the number of sequences in the profile which had a gap character
   * (or were too short to be included in this column's profile)
   * 
   * @return
   */
  public abstract int getGapped();

  /**
   * Returns the highest count for any symbol(s) in the profile
   * 
   * @return
   */
  public abstract int getMaxCount();

  /**
   * Returns the symbol (or concatenated symbols) which have the highest count
   * in the profile, or an empty string if there were no symbols counted
   * 
   * @return
   */
  public abstract String getModalResidue();

  /**
   * Answers the number of non-gapped sequences in the profile
   * 
   * @return
   */
  public abstract int getNonGapped();

}