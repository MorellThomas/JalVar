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

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class JvOptionPaneTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  Component parentComponent = null;

  String message = "Hello World!";

  String title = "Title";

  int optionType = JvOptionPane.OK_CANCEL_OPTION;

  int messageType = JvOptionPane.INFORMATION_MESSAGE;

  Icon icon = null;

  Object initialSelectionValue = null;

  Object[] selectionValues = null;

  @Test(groups = { "Functional" })
  public void showConfirmDialogFamilyTest()
  {
    JvOptionPane.showConfirmDialog(parentComponent, message);
    JvOptionPane.showConfirmDialog(parentComponent, message, title,
            optionType);
    JvOptionPane.showConfirmDialog(parentComponent, message, title,
            optionType, messageType);
    JvOptionPane.showConfirmDialog(parentComponent, message, title,
            optionType, messageType, icon);
    Assert.assertTrue(true);
  }

  @Test(groups = { "Functional" })
  public void showInputDialogFamilyTest()
  {
    JvOptionPane.showInputDialog(message);
    JvOptionPane.showInputDialog(parentComponent, message);
    JvOptionPane.showInputDialog(message, initialSelectionValue);
    JvOptionPane.showInputDialog(parentComponent, message,
            initialSelectionValue);
    JvOptionPane.showInputDialog(parentComponent, message, title,
            messageType);
    JvOptionPane.showInputDialog(parentComponent, message, title,
            messageType, icon, selectionValues, initialSelectionValue);
    Assert.assertTrue(true);
  }

  @Test(groups = { "Functional" })
  public void showMessageDialogFamilyTest()
  {
    JvOptionPane.showMessageDialog(parentComponent, message);
    JvOptionPane.showMessageDialog(parentComponent, message, title,
            messageType);
    JvOptionPane.showMessageDialog(parentComponent, message, title,
            messageType, icon);
    Assert.assertTrue(true);
  }

  @Test(groups = { "Functional" })
  public void showInternalMessageDialogFamilyTest()
  {
    JvOptionPane.showInternalMessageDialog(parentComponent, message);
    JvOptionPane.showInternalMessageDialog(parentComponent, message, title,
            messageType);
    JvOptionPane.showInternalMessageDialog(parentComponent, message, title,
            messageType, icon);
    Assert.assertTrue(true);
  }

  @Test(groups = { "Functional" })
  public void showInternalConfirmDialogFamilyTest()
  {
    JvOptionPane.showInternalConfirmDialog(parentComponent, message, title,
            optionType);
    JvOptionPane.showInternalConfirmDialog(parentComponent, message, title,
            optionType, messageType);

    JvOptionPane.showInternalConfirmDialog(getDummyDesktopPane(), message);

    JvOptionPane.showInternalConfirmDialog(getDummyDesktopPane(), message,
            title, optionType, messageType, icon);
    JvOptionPane.showInternalInputDialog(getDummyDesktopPane(), message);
    JvOptionPane.showInternalInputDialog(getDummyDesktopPane(), message,
            title, messageType);
    JvOptionPane.showInternalInputDialog(getDummyDesktopPane(), message,
            title, messageType, icon, selectionValues,
            initialSelectionValue);
    Assert.assertTrue(true);

  }

  private JDesktopPane getDummyDesktopPane()
  {
    JFrame frame = new JFrame("Dummy JDesktopPane");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    @SuppressWarnings("serial")
    JDesktopPane jdpDesktop = new JDesktopPane()
    {
      @Override
      public Dimension getPreferredSize()
      {
        return new Dimension(400, 300);
      }
    };
    frame.setContentPane(jdpDesktop);
    JPanel panel = new JPanel();
    panel.setBounds(0, 0, 400, 300);
    jdpDesktop.add(panel);
    frame.pack();
    frame.setVisible(true);
    panel.setVisible(true);
    return jdpDesktop;
  }
}
