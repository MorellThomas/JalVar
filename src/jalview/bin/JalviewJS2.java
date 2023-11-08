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

import jalview.util.Platform;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

/**
 * Entry point for JalviewJS development.
 * 
 * 
 * 
 * @author RM
 *
 */
public class JalviewJS2
{

  static
  {
    /**
     * @j2sNative
     * 
     *            J2S.thisApplet.__Info.args =
     *            ["open","examples/uniref50.fa","features",
     *            "examples/exampleFeatures.txt"];
     */
  }

  public static void main(String[] args) throws Exception
  {
    Jalview.main(args);
    // showFocusTimer();
  }

  protected static int focusTime = 0;

  private static void showFocusTimer()
  {

    if (Platform.isJS())
    {
      Timer t = new Timer(100, new ActionListener()
      {

        @Override
        public void actionPerformed(ActionEvent e)
        {
          String s = /** @j2sNative document.activeElement.id || */
                  null;

          s += " " + (++focusTime);

          /** @j2sNative document.title = s; */
        }

      });

      t.setRepeats(true);
      t.start();
    }
  }

}
