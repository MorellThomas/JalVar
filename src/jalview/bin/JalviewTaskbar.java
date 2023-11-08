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
package jalview.bin;

import java.awt.Image;
import java.awt.Taskbar;

import jalview.util.ChannelProperties;

public class JalviewTaskbar
{
  public JalviewTaskbar()
  {
  }

  protected static void setTaskbar(Jalview jalview)
  {

    if (Taskbar.isTaskbarSupported())
    {
      Taskbar tb = Taskbar.getTaskbar();
      if (tb.isSupported(Taskbar.Feature.ICON_IMAGE))
      {
        Image image = ChannelProperties.getImage("logo.512");
        if (image != null)
        {
          tb.setIconImage(image);
        }
        else
        {
          System.out.println("Unable to setIconImage()");
        }
      }
    }

  }
}
