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

import jalview.datamodel.SequencePoint;

import java.util.List;

/**
 * interface implemented by RotatatableCanvas GUI elements (such as point clouds
 * and simple structure views)
 * 
 * @author jimp
 * 
 */
public interface RotatableCanvasI
{
  void setPoints(List<SequencePoint> points, int rows);

  /**
   * Zoom the view in (or out) by the given factor, which should be >= 0. A
   * factor greater than 1 zooms in (expands the display), a factor less than 1
   * zooms out (shrinks the display).
   * 
   * @param factor
   */
  void zoom(float factor);

  /**
   * Rotates the view by the specified number of degrees about the x and/or y
   * axis
   * 
   * @param x
   * @param y
   */
  void rotate(float x, float y);
}
