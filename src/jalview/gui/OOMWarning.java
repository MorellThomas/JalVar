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

import jalview.bin.Console;
import jalview.util.MessageManager;

import java.awt.Component;

public class OOMWarning implements Runnable
{
  String action = null;

  String instructions = "";

  public static boolean oomInprogress = false;

  Component desktop = null;

  /**
   * Raise an out of memory error.
   * 
   * @param action
   *          - what was going on when OutOfMemory exception occured.
   * @param instance
   *          - Window where the dialog will appear
   * @param oomex
   *          - the actual exception - to be written to stderr or debugger.
   */
  OOMWarning(final String action, final OutOfMemoryError oomex,
          final Component instance)
  {
    if (!oomInprogress)
    {
      oomInprogress = true;
      this.action = action;
      desktop = instance;
      if (oomex != null)
      {
        Console.error("Out of Memory when " + action, oomex);
      }
      javax.swing.SwingUtilities.invokeLater(this);
      System.gc();
    }
  }

  public OOMWarning(String string, OutOfMemoryError oomerror)
  {
    this(string, oomerror, Desktop.desktop);
  }

  @Override
  public void run()
  {
    oomInprogress = false;
    JvOptionPane.showInternalMessageDialog(desktop, MessageManager
            .formatMessage("warn.out_of_memory_when_action", new String[]
            { action }), MessageManager.getString("label.out_of_memory"),
            JvOptionPane.WARNING_MESSAGE);
    // hope that there's enough memory left that no more appear.
  }

}
