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

public class Profiles implements ProfilesI
{

  private ProfileI[] profiles;

  public Profiles(ProfileI[] p)
  {
    profiles = p;
  }

  /**
   * Returns the profile for the given column, or null if none found
   * 
   * @param col
   */
  @Override
  public ProfileI get(int col)
  {
    return profiles != null && col >= 0 && col < profiles.length
            ? profiles[col]
            : null;
  }

  /**
   * Returns the first column (base 0) covered by the profiles
   */
  @Override
  public int getStartColumn()
  {
    return 0;
  }

  /**
   * Returns the last column (base 0) covered by the profiles
   */
  @Override
  public int getEndColumn()
  {
    return profiles == null ? 0 : profiles.length - 1;
  }

}
