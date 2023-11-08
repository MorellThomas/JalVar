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
 * A data bean class to hold properties of an RNA viewer
 */
public class RnaViewerModel
{
  public final String viewId;

  public final String title;

  public final int x;

  public final int y;

  public final int width;

  public final int height;

  public final int dividerLocation;

  /**
   * Constructor
   * 
   * @param viewId
   * @param title
   * @param xpos
   * @param ypos
   * @param width
   * @param height
   * @param dividerLocation
   */
  public RnaViewerModel(String viewId, String title, int xpos, int ypos,
          int width, int height, int dividerLocation)
  {
    this.viewId = viewId;
    this.title = title;
    this.x = xpos;
    this.y = ypos;
    this.width = width;
    this.height = height;
    this.dividerLocation = dividerLocation;
  }
}
