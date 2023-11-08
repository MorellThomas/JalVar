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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import jalview.gui.JvOptionPane;

import java.awt.Button;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PlatformTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  Button b = new Button();

  /**
   * isControlDown for Mac should answer true for Meta-down, but not for right
   * mouse (popup trigger)
   */
  @Test(groups = "Functional")
  public void testIsControlDown_mac()
  {
    String toolkit = Toolkit.getDefaultToolkit().getClass().getName();
    if ("sun.awt.X11.XToolkit".equals(toolkit))
    {
      /*
       * this toolkit on the build server fails these tests,
       * because it returns 2, not 4, for getMenuShortcutKeyMask
       */
      return;
    }

    int clickCount = 1;
    boolean isPopupTrigger = false;
    int buttonNo = MouseEvent.BUTTON1;
    boolean mac = true;

    int mods = 0;
    // not concerned with MouseEvent id, when, x, y, xAbs, yAbs values
    assertFalse(Platform.isControlDown(new MouseEvent(b, 0, 0L, mods, 0, 0,
            0, 0, clickCount, isPopupTrigger, buttonNo), mac));

    mods = InputEvent.CTRL_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK;
    assertFalse(Platform.isControlDown(new MouseEvent(b, 0, 0L, mods, 0, 0,
            0, 0, clickCount, isPopupTrigger, buttonNo), mac));

    mods = InputEvent.META_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK;
    assertTrue(Platform.isControlDown(new MouseEvent(b, 0, 0L, mods, 0, 0,
            0, 0, clickCount, isPopupTrigger, buttonNo), mac));

    isPopupTrigger = true;
    assertFalse(Platform.isControlDown(new MouseEvent(b, 0, 0L, mods, 0, 0,
            0, 0, clickCount, isPopupTrigger, buttonNo), mac));

    isPopupTrigger = false;
    buttonNo = MouseEvent.BUTTON2;
    mods = 0;
    assertFalse(Platform.isControlDown(new MouseEvent(b, 0, 0L, mods, 0, 0,
            0, 0, clickCount, isPopupTrigger, buttonNo), mac));
  }

  /**
   * If not a Mac, we only care whether CTRL_MASK modifier is set on the mouse
   * event
   */
  @Test(groups = "Functional")
  public void testIsControlDown_notMac()
  {
    int clickCount = 1;
    boolean isPopupTrigger = false;
    int buttonNo = MouseEvent.BUTTON1;
    boolean mac = false;

    int mods = 0;
    // not concerned with MouseEvent id, when, x, y, xAbs, yAbs values
    assertFalse(Platform.isControlDown(new MouseEvent(b, 0, 0L, mods, 0, 0,
            0, 0, clickCount, isPopupTrigger, buttonNo), mac));

    mods = InputEvent.CTRL_DOWN_MASK;
    assertTrue(Platform.isControlDown(new MouseEvent(b, 0, 0L, mods, 0, 0,
            0, 0, clickCount, isPopupTrigger, buttonNo), mac));

    mods = InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK
            | InputEvent.ALT_DOWN_MASK;
    clickCount = 2;
    buttonNo = 2;
    isPopupTrigger = true;
    assertTrue(Platform.isControlDown(new MouseEvent(b, 0, 0L, mods, 0, 0,
            0, 0, clickCount, isPopupTrigger, buttonNo), mac));
  }

  @Test(groups = "Functional")
  public void testPathEquals()
  {
    assertTrue(Platform.pathEquals(null, null));
    assertFalse(Platform.pathEquals(null, "apath"));
    assertFalse(Platform.pathEquals("apath", null));
    assertFalse(Platform.pathEquals("apath", "APATH"));
    assertTrue(Platform.pathEquals("apath", "apath"));
    assertTrue(Platform.pathEquals("apath/a/b", "apath\\a\\b"));
  }

  @Test(groups = "Functional")
  public void testEscapeBackslashes()
  {
    assertNull(Platform.escapeBackslashes(null));
    assertEquals(Platform.escapeBackslashes("hello world"), "hello world");
    assertEquals(Platform.escapeBackslashes("hello\\world"),
            "hello\\\\world");
  }
}
