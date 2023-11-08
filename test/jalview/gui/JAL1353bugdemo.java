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

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JTextArea;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.bin.Console;

public class JAL1353bugdemo
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @BeforeClass(alwaysRun = true)
  public static void setUpBeforeClass() throws Exception
  {
  }

  @AfterClass(alwaysRun = true)
  public static void tearDownAfterClass() throws Exception
  {
  }

  volatile boolean finish = false;

  @Test(groups = { "Functional" }, enabled = false)
  public void test()
  {
    Console.initLogger();
    // final Desktop foo = new Desktop();
    final JFrame cfoo = new JFrame("Crash Java");
    final JDesktopPane foo = new JDesktopPane();
    foo.setPreferredSize(new Dimension(600, 800));
    cfoo.setSize(600, 800);
    final JInternalFrame cont = new JInternalFrame("My Frame");
    cont.setPreferredSize(new Dimension(400, 400));
    String msg = "This is a dummy string. See the dummy string go.\n";
    msg += msg; // 2
    msg += msg; // 4
    msg += msg; // 8
    msg += msg; // 16
    JTextArea evt = new JTextArea(
            "Click here and drag text over this window to freeze java.\n\n"
                    + msg);
    cont.add(evt);
    cont.pack();
    foo.add("A frame", cont);
    foo.setVisible(true);
    foo.setEnabled(true);
    foo.doLayout();
    cfoo.add(foo);
    // final JMenu jm = new JMenu("Do");
    // JMenuItem jmi = new JMenuItem("this");
    // jm.add(jmi);
    evt.addMouseListener(new MouseAdapter()
    {

      @Override
      public void mouseClicked(MouseEvent e)
      {
        // JFrame parent = new JFrame();
        // parent.setBounds(foo.getBounds());
        // JPanel oo = new JPanel();
        // parent.add(oo);
        // oo.setVisible(true);
        // parent.setVisible(true);
        EditNameDialog end = new EditNameDialog("Sequence Name",
                "Try and drag between the two text fields", "label 1",
                "Label 2");// );cont.getRootPane());
        assert (end != null);
        finish = true;
      }
    });
    cont.setVisible(true);

    // jmi.addActionListener(new ActionListener()
    // {
    //
    // @Override
    // public void actionPerformed(ActionEvent arg0)
    // {
    // EditNameDialog end = new EditNameDialog("Sequence Name",
    // "Sequence Description", "label 1", "Label 2",
    // "Try and drag between the two text fields", cont);
    // assert (end != null);
    // finish = true;
    // }
    // });
    foo.setVisible(true);
    cfoo.setVisible(true);
    while (!finish)
    {
      try
      {
        Thread.sleep(100);
      } catch (InterruptedException x)
      {
      }
    }
  }

}
