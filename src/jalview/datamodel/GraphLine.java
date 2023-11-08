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

public class GraphLine
{
  public float value;

  public String label = "";

  public java.awt.Color colour = java.awt.Color.black;

  public boolean displayed = true;

  public GraphLine(float value, String label, java.awt.Color col)
  {
    this.value = value;
    if (label != null)
    {
      this.label = label;
    }

    if (col != null)
    {
      this.colour = col;
    }
  }

  public GraphLine(GraphLine from)
  {
    if (from != null)
    {
      value = from.value;
      label = new String(from.label);
      colour = from.colour;
      displayed = from.displayed;
    }
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj != null && obj instanceof GraphLine)
    {
      GraphLine other = (GraphLine) obj;
      return displayed == other.displayed && value == other.value
              && (colour != null
                      ? (other.colour != null
                              && other.colour.equals(colour))
                      : other.colour == null)
              && (label != null
                      ? (other.label != null && other.label.equals(label))
                      : other.label == null);
    }
    return false;
  }
}
