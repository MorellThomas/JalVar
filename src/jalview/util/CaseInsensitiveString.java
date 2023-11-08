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
package jalview.util;

/**
 * A class to wrap a case insensitive string. For use in collections where we
 * want to preserve case, but do not want to duplicate upper and lower case
 * variants
 */
import java.util.Locale;

public final class CaseInsensitiveString
{
  String value;

  public CaseInsensitiveString(String s)
  {
    this.value = s;
  }

  @Override
  public String toString()
  {
    return value;
  }

  /**
   * Answers true if the object compared to is a CaseInsensitiveString wrapping
   * the same string value (ignoring case), or if both wrap a null value, else
   * false
   */
  @Override
  public boolean equals(Object o)
  {
    if (o == null)
    {
      return false;
    }
    if (!(o instanceof CaseInsensitiveString))
    {
      return false;
    }
    CaseInsensitiveString obj = (CaseInsensitiveString) o;
    if (value == null)
    {
      return obj.value == null;
    }
    return value.equalsIgnoreCase(obj.value);
  }

  /**
   * hashCode overriden to guarantee that 'equal' objects have the same hash
   * code
   */
  @Override
  public int hashCode()
  {
    return value == null ? super.hashCode()
            : value.toUpperCase(Locale.ROOT).hashCode();
  }
}
