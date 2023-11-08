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

import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jalview.util.MessageManager;

/**
 * A dialog where a name and description may be edited
 */
public class EditNameDialog
{
  private static final Font COURIER_12 = new Font("Courier", Font.PLAIN,
          12);

  JTextField id;

  JTextField description;

  JButton ok = new JButton(MessageManager.getString("action.accept"));

  JButton cancel = new JButton(MessageManager.getString("action.cancel"));

  private JPanel panel;

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

  /**
   * Constructor
   * 
   * @param name
   * @param desc
   * @param label1
   * @param label2
   */
  public EditNameDialog(String name, String desc, String label1,
          String label2)
  {
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JPanel descriptionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    panel.add(namePanel);
    panel.add(descriptionPanel);

    JLabel nameLabel = new JLabel(label1);
    // nameLabel.setFont(COURIER_12);
    namePanel.add(nameLabel);

    id = new JTextField(name, 40);
    namePanel.add(id);

    description = new JTextField(desc, 40);

    /*
     * optionally add field for description
     */
    if (desc != null || label2 != null)
    {
      JLabel descLabel = new JLabel(label2);
      // descLabel.setFont(COURIER_12);
      descriptionPanel.add(descLabel);
      descriptionPanel.add(description);
    }
  }

  /**
   * Shows the dialog, and runs the response action if OK is selected
   * 
   * @param action
   */
  public void showDialog(JComponent parent, String title, Runnable action)
  {
    String ok = MessageManager.getString("action.ok");
    String cancel = MessageManager.getString("action.cancel");
    String[] options = new String[] { ok, cancel };

    JvOptionPane.newOptionDialog(parent)
            .setResponseHandler(JvOptionPane.OK_OPTION, action)
            .showInternalDialog(panel, title, JvOptionPane.OK_CANCEL_OPTION,
                    JvOptionPane.PLAIN_MESSAGE, null, options, ok);

    /*
    List<Runnable> actions = new ArrayList<>();
    actions.add(action);
    actions.add(JvOptionPane.NULLCALLABLE);
    
    JvOptionPane.frameDialog(panel, title, JvOptionPane.PLAIN_MESSAGE,
            options, ok, actions, false);
            */
  }
}
