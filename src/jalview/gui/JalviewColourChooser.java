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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;

/**
 * A helper class that shows a JColorChooser and passes the selected colour back
 * to a listener
 */
public class JalviewColourChooser
{
  public interface ColourChooserListener
  {
    void colourSelected(Color c);

    default void cancel()
    {
    };
  }

  /**
   * Shows a colour chooser dialog with the given parent component, title, and
   * (optionally) initially selected colour. The chosen colour is passed back to
   * the listener. There is no action if the dialog is cancelled.
   * 
   * @param parent
   * @param title
   * @param initialColour
   * @param listener
   */
  public static void showColourChooser(Component parent, String title,
          Color initialColour, ColourChooserListener listener)
  {
    JColorChooser colorChooser = new JColorChooser();
    if (initialColour != null)
    {
      colorChooser.setColor(initialColour);
    }
    ActionListener onChoose = evt -> listener
            .colourSelected(colorChooser.getColor());
    ActionListener onCancel = evt -> listener.cancel();
    JDialog dialog = JColorChooser.createDialog(parent, title, true,
            colorChooser, onChoose, onCancel);
    dialog.setVisible(true);
  }

  /**
   * A convenience method that shows a colour chooser, with initial colour the
   * background of the given 'paintable', and updates its background colour and
   * repaints it after a colour selection is made
   * 
   * @param parent
   * @param title
   * @param paintable
   */
  public static void showColourChooser(Component parent, String title,
          JComponent paintable)
  {
    ColourChooserListener listener = new ColourChooserListener()
    {
      @Override
      public void colourSelected(Color c)
      {
        paintable.setBackground(c);
        paintable.repaint();
      }
    };
    JalviewColourChooser.showColourChooser(parent, title,
            paintable.getBackground(), listener);
  }
}
