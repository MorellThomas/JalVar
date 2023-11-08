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
 * A bean that models a set of (x, y, z) values and a reference to a sequence.
 * As used in Principal Component Analysis, the (x, y, z) values are the
 * sequence's score for the currently selected first, second and third
 * dimensions of the PCA.
 */
public class SequencePoint
{
  /*
   * Associated alignment sequence, or dummy sequence object
   */
  private final SequenceI sequence;

  /*
   * x, y, z values
   */
  public Point coord;

  /**
   * Constructor
   * 
   * @param sequence
   * @param coord
   */
  public SequencePoint(SequenceI sequence, Point pt)
  {
    this.sequence = sequence;
    this.coord = pt;
  }

  /**
   * Constructor given a sequence and an array of x, y, z coordinate positions
   * 
   * @param sequence
   * @param coords
   * @throws ArrayIndexOutOfBoundsException
   *           if array length is less than 3
   */
  public SequencePoint(SequenceI sequence, float[] coords)
  {
    this(sequence, new Point(coords[0], coords[1], coords[2]));
  }

  public SequenceI getSequence()
  {
    return sequence;
  }

  /**
   * Applies a translation to the (x, y, z) coordinates
   * 
   * @param centre
   */
  public void translate(float x, float y, float z)
  {
    coord = new Point(coord.x + x, coord.y + y, coord.z + z);
  }

  /**
   * string representation for ease of inspection in debugging or logging only
   */
  @Override
  public String toString()
  {
    return sequence.getName() + " " + coord.toString();
  }
}
