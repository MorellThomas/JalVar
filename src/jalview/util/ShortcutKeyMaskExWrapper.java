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

import java.awt.event.MouseEvent;

public class ShortcutKeyMaskExWrapper
{

  private static final Float specversion;

  private static final float modern;

  public static final int SHIFT_DOWN_MASK;

  public static final int ALT_DOWN_MASK;

  private static final ShortcutKeyMaskExWrapperI wrapper;

  static
  {
    specversion = Platform.isJS() ? Float.valueOf(8)
            : Float.parseFloat(
                    System.getProperty("java.specification.version"));
    modern = 11;

    if (specversion >= modern)
    {
      wrapper = new jalview.util.ShortcutKeyMaskExWrapper11();
      SHIFT_DOWN_MASK = jalview.util.ShortcutKeyMaskExWrapper11.SHIFT_DOWN_MASK;
      ALT_DOWN_MASK = jalview.util.ShortcutKeyMaskExWrapper11.ALT_DOWN_MASK;
    }
    else
    {
      wrapper = new jalview.util.ShortcutKeyMaskExWrapper8();
      SHIFT_DOWN_MASK = jalview.util.ShortcutKeyMaskExWrapper8.SHIFT_DOWN_MASK;
      ALT_DOWN_MASK = jalview.util.ShortcutKeyMaskExWrapper8.ALT_DOWN_MASK;
    }
  }

  public static int getMenuShortcutKeyMaskEx()
  {
    return wrapper.getMenuShortcutKeyMaskEx();
  }

  public static int getModifiersEx(MouseEvent e)
  {
    return wrapper.getModifiersEx(e);
  }

}
