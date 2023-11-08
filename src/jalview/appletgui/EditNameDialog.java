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
package jalview.appletgui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;

public class EditNameDialog extends JVDialog
{
  TextField id, description;

  public String getName()
  {
    return id.getText();
  }

  public String getDescription()
  {
    if (description.getText().length() < 1)
    {
      return null;
    }
    else
    {
      return description.getText();
    }
  }

  public EditNameDialog(String name, String desc, String label1,
          String label2, Frame owner, String title, int width, int height,
          boolean display)
  {
    super(owner, title, true, width, height);

    Font mono = new Font("Monospaced", Font.PLAIN, 12);
    Panel panel = new Panel(new BorderLayout());
    Panel panel2 = new Panel(new BorderLayout());

    id = new TextField(name, 40);
    id.setFont(mono);
    Label label = new Label(label1);
    label.setFont(mono);

    panel2.add(label, BorderLayout.WEST);
    panel2.add(id, BorderLayout.CENTER);
    panel.add(panel2, BorderLayout.NORTH);

    if (label2 != null)
    {
      panel2 = new Panel(new BorderLayout());
      description = new TextField(desc, 40);
      description.setFont(mono);
      label = new Label(label2);
      label.setFont(mono);
      panel2.add(label, BorderLayout.WEST);
      panel2.add(description, BorderLayout.CENTER);
      panel.add(panel2, BorderLayout.CENTER);
    }
    setMainPanel(panel);
    setVisible(display);
  }
}
