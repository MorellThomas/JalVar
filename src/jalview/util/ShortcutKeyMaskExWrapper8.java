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

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class ShortcutKeyMaskExWrapper8 implements ShortcutKeyMaskExWrapperI
{
  public final static int SHIFT_DOWN_MASK;

  public final static int ALT_DOWN_MASK;

  static
  {
    SHIFT_DOWN_MASK = KeyEvent.SHIFT_MASK;
    ALT_DOWN_MASK = KeyEvent.ALT_MASK;
  }

  @Override
  public int getMenuShortcutKeyMaskEx()
  {
    try
    {
      if (!GraphicsEnvironment.isHeadless())
      {
        return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
      }
    } catch (java.lang.Throwable t)
    {
    }
    return 0;
  }

  @Override
  public int getModifiersEx(MouseEvent e)
  {
    return e.getModifiers();
  }

}
