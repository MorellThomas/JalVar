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
import java.awt.Button;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class JVDialog extends Dialog implements ActionListener
{
  AlignmentPanel ap;

  Panel buttonPanel;

  Button ok = new Button("Accept");

  Button cancel = new Button("Cancel");

  boolean accept = false;

  Frame owner;

  public JVDialog(Frame owner, String title, boolean modal, int width,
          int height)
  {
    super(owner, title, modal);
    this.owner = owner;

    height += owner.getInsets().top + getInsets().bottom;

    setBounds(owner.getBounds().x + (owner.getSize().width - width) / 2,
            owner.getBounds().y + (owner.getSize().height - height) / 2,
            width, height);
  }

  public JVDialog(Frame owner, Panel mainPanel, String title, boolean modal,
          int width, int height)
  {
    super(owner, title, modal);
    this.owner = owner;

    height += owner.getInsets().top + getInsets().bottom;

    setBounds(owner.getBounds().x + (owner.getSize().width - width) / 2,
            owner.getBounds().y + (owner.getSize().height - height) / 2,
            width, height);
    setMainPanel(mainPanel);
  }

  void setMainPanel(Panel panel)
  {
    add(panel, BorderLayout.NORTH);

    buttonPanel = new Panel(new FlowLayout());

    buttonPanel.add(ok);
    buttonPanel.add(cancel);
    ok.addActionListener(this);
    cancel.addActionListener(this);
    add(buttonPanel, BorderLayout.SOUTH);

    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent ev)
      {
        setVisible(false);
        dispose();
      }
    });

    pack();

  }

  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == ok)
    {
      accept = true;
    }

    setVisible(false);
    dispose();
  }

}
