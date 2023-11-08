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

//
// A simple Java Console for your application (Swing version)
// Requires Java 1.1.5 or higher
//
// Disclaimer the use of this source is at your own risk. 
//
// Permision to use and distribute into your own applications
//
// RJHM van den Bergh , rvdb@comweb.nl

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import jalview.bin.Jalview;

public class AWTConsole extends WindowAdapter
        implements WindowListener, ActionListener, Runnable
{
  private Frame frame;

  private TextArea textArea;

  private Thread reader;

  private Thread reader2;

  private boolean quit;

  private final PipedInputStream pin = new PipedInputStream();

  private final PipedInputStream pin2 = new PipedInputStream();

  Thread errorThrower; // just for testing (Throws an Exception at this Console

  public AWTConsole()
  {
    // create all components and add them
    frame = new Frame("Java Console");
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = new Dimension(screenSize.width / 2,
            screenSize.height / 2);
    int x = frameSize.width / 2;
    int y = frameSize.height / 2;
    frame.setBounds(x, y, frameSize.width, frameSize.height);

    textArea = new TextArea();
    textArea.setEditable(false);
    Button button = new Button("clear");

    Panel panel = new Panel();
    panel.setLayout(new BorderLayout());
    panel.add(textArea, BorderLayout.CENTER);
    panel.add(button, BorderLayout.SOUTH);
    frame.add(panel);

    frame.setVisible(true);

    frame.addWindowListener(this);
    button.addActionListener(this);

    try
    {
      PipedOutputStream pout = new PipedOutputStream(this.pin);
      System.setOut(new PrintStream(pout, true));
    } catch (java.io.IOException io)
    {
      textArea.append("Couldn't redirect STDOUT to this console\n"
              + io.getMessage());
    } catch (SecurityException se)
    {
      textArea.append("Couldn't redirect STDOUT to this console\n"
              + se.getMessage());
    }

    try
    {
      PipedOutputStream pout2 = new PipedOutputStream(this.pin2);
      System.setErr(new PrintStream(pout2, true));
    } catch (java.io.IOException io)
    {
      textArea.append("Couldn't redirect STDERR to this console\n"
              + io.getMessage());
    } catch (SecurityException se)
    {
      textArea.append("Couldn't redirect STDERR to this console\n"
              + se.getMessage());
    }

    quit = false; // signals the Threads that they should exit

    // Starting two seperate threads to read from the PipedInputStreams
    //
    reader = new Thread(this);
    reader.setDaemon(true);
    reader.start();
    //
    reader2 = new Thread(this);
    reader2.setDaemon(true);
    reader2.start();

    // testing part
    // you may omit this part for your application
    //
    System.out.println("Hello World 2");
    System.out.println("All fonts available to Graphic2D:\n");
    GraphicsEnvironment ge = GraphicsEnvironment
            .getLocalGraphicsEnvironment();
    String[] fontNames = ge.getAvailableFontFamilyNames();
    for (int n = 0; n < fontNames.length; n++)
    {
      System.out.println(fontNames[n]);
    }
    // Testing part: simple an error thrown anywhere in this JVM will be printed
    // on the Console
    // We do it with a seperate Thread becasue we don't wan't to break a Thread
    // used by the Console.
    System.out.println("\nLets throw an error on this console");
    errorThrower = new Thread(this);
    errorThrower.setDaemon(true);
    errorThrower.start();
  }

  @Override
  public synchronized void windowClosed(WindowEvent evt)
  {
    quit = true;
    this.notifyAll(); // stop all threads
    try
    {
      reader.join(1000);
      pin.close();
    } catch (Exception e)
    {
    }
    try
    {
      reader2.join(1000);
      pin2.close();
    } catch (Exception e)
    {
    }
    Jalview.exit("Window closing. Bye!", 0);
  }

  @Override
  public synchronized void windowClosing(WindowEvent evt)
  {
    frame.setVisible(false); // default behaviour of JFrame
    frame.dispose();
  }

  @Override
  public synchronized void actionPerformed(ActionEvent evt)
  {
    textArea.setText("");
  }

  @Override
  public synchronized void run()
  {
    try
    {
      while (Thread.currentThread() == reader)
      {
        try
        {
          this.wait(100);
        } catch (InterruptedException ie)
        {
        }
        if (pin.available() != 0)
        {
          String input = this.readLine(pin);
          textArea.append(input);
        }
        if (quit)
        {
          return;
        }
      }

      while (Thread.currentThread() == reader2)
      {
        try
        {
          this.wait(100);
        } catch (InterruptedException ie)
        {
        }
        if (pin2.available() != 0)
        {
          String input = this.readLine(pin2);
          textArea.append(input);
        }
        if (quit)
        {
          return;
        }
      }
    } catch (Exception e)
    {
      textArea.append("\nConsole reports an Internal error.");
      textArea.append("The error is: " + e);
    }

    // just for testing (Throw a Nullpointer after 1 second)
    if (Thread.currentThread() == errorThrower)
    {
      try
      {
        this.wait(1000);
      } catch (InterruptedException ie)
      {
      }
      throw new NullPointerException(
              MessageManager.getString("exception.application_test_npe"));
    }

  }

  public synchronized String readLine(PipedInputStream in)
          throws IOException
  {
    String input = "";
    do
    {
      int available = in.available();
      if (available == 0)
      {
        break;
      }
      byte b[] = new byte[available];
      in.read(b);
      input = input + new String(b, 0, b.length);
    } while (!input.endsWith("\n") && !input.endsWith("\r\n") && !quit);
    return input;
  }

  /**
   * 
   * @param arg
   * @j2sIgnore
   */
  public static void main(String[] arg)
  {
    new AWTConsole(); // create console with not reference
  }
}
