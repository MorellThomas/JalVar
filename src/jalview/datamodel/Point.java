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
 * A bean that models an (x, y, z) position in 3-D space
 */
public final class Point
{
  public final float x;

  public final float y;

  public final float z;

  public Point(float xVal, float yVal, float zVal)
  {
    x = xVal;
    y = yVal;
    z = zVal;
  }

  /**
   * toString for convenience of inspection in debugging or logging
   */
  @Override
  public String toString()
  {
    return String.format("[%f, %f, %f]", x, y, z);
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + Float.floatToIntBits(x);
    result = prime * result + Float.floatToIntBits(y);
    result = prime * result + Float.floatToIntBits(z);
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
    {
      return true;
    }
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    Point other = (Point) obj;
    if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
    {
      return false;
    }
    if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
    {
      return false;
    }
    if (Float.floatToIntBits(z) != Float.floatToIntBits(other.z))
    {
      return false;
    }
    return true;
  }
}
