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
package jalview.gui;

import java.awt.Canvas;
import java.awt.Font;
import java.awt.FontMetrics;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FontChooserTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * Not a real test as it runs no methods on FontChooser and makes no
   * assertions, but this method writes to sysout the names of any (currently
   * available, plain) fonts and point sizes that would be rejected by Jalview's
   * FontChooser as having an I-width of less than 1.0.
   */
  @Test(groups = { "Functional" }, enabled = false)
  public void dumpInvalidFonts()
  {
    String[] fonts = java.awt.GraphicsEnvironment
            .getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    final Canvas canvas = new Canvas();
    for (int pointSize = 1;; pointSize++)
    {
      System.out.println(System.lineSeparator()
              + "Plain fonts with sub-pixel width at " + pointSize + "pt:");
      if (pointSize == 1)
      {
        System.out.println("All except:");
      }
      int badCount = 0;
      for (String fontname : fonts)
      {
        Font newFont = new Font(fontname, Font.PLAIN, pointSize);
        FontMetrics fontm = canvas.getFontMetrics(newFont);
        double iw = fontm.getStringBounds("I", null).getWidth();
        final boolean tooSmall = iw < 1d;
        if (tooSmall)
        {
          badCount++;
        }
        if ((pointSize > 1 && tooSmall) || (pointSize == 1 && !tooSmall))
        {
          System.out.println(fontname);
        }
      }
      if (badCount == 0)
      {
        break;
      }
    }
  }

}
