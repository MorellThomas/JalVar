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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.text.DefaultCaret;

import jalview.log.JLoggerI.LogLevel;
import jalview.log.JLoggerLog4j;
import jalview.log.JalviewAppender;
import jalview.util.ChannelProperties;
import jalview.util.MessageManager;
import jalview.util.Platform;

/**
 * Simple Jalview Java Console. Version 1 - allows viewing of console output
 * after desktop is created. Acquired with thanks from RJHM's site
 * http://www.comweb.nl/java/Console/Console.html A simple Java Console for your
 * application (Swing version) Requires Java 1.1.5 or higher Disclaimer the use
 * of this source is at your own risk. Permision to use and distribute into your
 * own applications RJHM van den Bergh , rvdb@comweb.nl
 */

public class Console extends WindowAdapter
        implements WindowListener, ActionListener, Runnable
{
  private JFrame frame;

  private JTextArea textArea;

  /*
   * unused - tally and limit for lines in console window int lines = 0;
   * 
   * int lim = 1000;
   */
  int byteslim = 102400, bytescut = 76800; // 100k and 75k cut point.

  private Thread reader, reader2, textAppender;

  private boolean quit;

  private final PrintStream stdout = System.out, stderr = System.err;

  private PipedInputStream pin = new PipedInputStream();

  private PipedInputStream pin2 = new PipedInputStream();

  private StringBuffer displayPipe = new StringBuffer();

  Thread errorThrower; // just for testing (Throws an Exception at this Console

  // are we attached to some parent Desktop
  Desktop parent = null;

  private int MIN_WIDTH = 300;

  private int MIN_HEIGHT = 250;

  private JComboBox<LogLevel> logLevelCombo = new JComboBox<LogLevel>();

  protected LogLevel startingLogLevel = LogLevel.INFO;

  public Console()
  {
    // create all components and add them
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame = initFrame("Java Console", screenSize.width / 2,
            screenSize.height / 2, -1, -1);
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    initConsole(true);
  }

  private void initConsole(boolean visible)
  {
    initConsole(visible, true);
  }

  /**
   * 
   * @param visible
   *          - open the window
   * @param redirect
   *          - redirect std*
   */
  private void initConsole(boolean visible, boolean redirect)
  {
    // CutAndPasteTransfer cpt = new CutAndPasteTransfer();
    // textArea = cpt.getTextArea();
    textArea = new JTextArea();
    textArea.setEditable(false);
    // autoscroll
    DefaultCaret caret = (DefaultCaret) textArea.getCaret();
    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    // toggle autoscroll by clicking on the text area
    Border pausedBorder = BorderFactory.createMatteBorder(2, 2, 2, 2,
            textArea.getForeground());
    Border noBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setBorder(noBorder);
    textArea.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent e)
      {
        if (e.getButton() == MouseEvent.BUTTON1)
        {
          if (caret.getUpdatePolicy() == DefaultCaret.ALWAYS_UPDATE)
          {
            caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
            scrollPane.setBorder(pausedBorder);
          }
          else
          {
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
            textArea.setCaretPosition(textArea.getDocument().getLength());
            scrollPane.setBorder(noBorder);
          }
        }
      }
    });

    JButton clearButton = new JButton(
            MessageManager.getString("action.clear"));
    JButton copyToClipboardButton = new JButton(
            MessageManager.getString("label.copy_to_clipboard"));
    copyToClipboardButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        copyConsoleTextToClipboard();
      }
    });
    copyToClipboardButton.addMouseListener(new MouseAdapter()
    {
      private Color bg = textArea.getBackground();

      private Color fg = textArea.getForeground();

      public void mousePressed(MouseEvent e)
      {
        textArea.setBackground(textArea.getSelectionColor());
        textArea.setForeground(textArea.getSelectedTextColor());
      }

      public void mouseReleased(MouseEvent e)
      {
        textArea.setBackground(bg);
        textArea.setForeground(fg);
      }

    });
    copyToClipboardButton.setToolTipText(
            MessageManager.getString("label.copy_to_clipboard_tooltip"));

    JLabel logLevelLabel = new JLabel(
            MessageManager.getString("label.log_level") + ":");

    // logLevelCombo.addItem(LogLevel.ALL);
    logLevelCombo.addItem(LogLevel.TRACE);
    logLevelCombo.addItem(LogLevel.DEBUG);
    logLevelCombo.addItem(LogLevel.INFO);
    logLevelCombo.addItem(LogLevel.WARN);
    // logLevelCombo.addItem(LogLevel.ERROR);
    // logLevelCombo.addItem(LogLevel.FATAL);
    // logLevelCombo.addItem(LogLevel.ERROR);
    // logLevelCombo.addItem(LogLevel.OFF);
    // set startingLogLevel
    startingLogLevel = jalview.bin.Console.log == null ? LogLevel.INFO
            : jalview.bin.Console.log.getLevel();
    setChosenLogLevelCombo();
    logLevelCombo.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (jalview.bin.Console.log != null)
        {
          jalview.bin.Console.log
                  .setLevel((LogLevel) logLevelCombo.getSelectedItem());
        }
      }

    });

    // frame = cpt;
    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
    JPanel southPanel = new JPanel();
    southPanel.setLayout(new GridBagLayout());

    JPanel logLevelPanel = new JPanel();
    logLevelPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    logLevelPanel.add(logLevelLabel);
    logLevelPanel.add(logLevelCombo);
    String logLevelTooltip = MessageManager.formatMessage(
            "label.log_level_tooltip", startingLogLevel.toString());
    logLevelLabel.setToolTipText(logLevelTooltip);
    logLevelCombo.setToolTipText(logLevelTooltip);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.weightx = 0.1;
    southPanel.add(logLevelPanel, gbc);

    gbc.gridx++;
    gbc.weightx = 0.8;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    southPanel.add(clearButton, gbc);

    gbc.gridx++;
    gbc.weightx = 0.1;
    gbc.fill = GridBagConstraints.NONE;
    southPanel.add(copyToClipboardButton, gbc);

    southPanel.setVisible(true);
    frame.getContentPane().add(southPanel, BorderLayout.SOUTH);
    frame.setVisible(visible);
    updateConsole = visible;
    frame.addWindowListener(this);
    clearButton.addActionListener(this);

    if (redirect)
    {
      redirectStreams();
    }
    else
    {
      unredirectStreams();
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
    // and a thread to append text to the textarea
    textAppender = new Thread(this);
    textAppender.setDaemon(true);
    textAppender.start();

    // set icons
    frame.setIconImages(ChannelProperties.getIconList());
  }

  private void setChosenLogLevelCombo()
  {
    setChosenLogLevelCombo(startingLogLevel);
  }

  private void setChosenLogLevelCombo(LogLevel setLogLevel)
  {
    logLevelCombo.setSelectedItem(setLogLevel);
    if (!logLevelCombo.getSelectedItem().equals(setLogLevel))
    {
      // setLogLevel not (yet) in list
      if (setLogLevel != null && setLogLevel instanceof LogLevel)
      {
        // add new item to list (might be set via .jalview_properties)
        boolean added = false;
        for (int i = 0; i < logLevelCombo.getItemCount(); i++)
        {
          LogLevel l = (LogLevel) logLevelCombo.getItemAt(i);
          if (l.compareTo(setLogLevel) >= 0)
          {
            logLevelCombo.insertItemAt(setLogLevel, i);
            added = true;
            break;
          }
        }
        if (!added) // lower priority than others or some confusion -- add to
                    // end of list
        {
          logLevelCombo.addItem(setLogLevel);
        }
        logLevelCombo.setSelectedItem(setLogLevel);
      }
      else
      {
        logLevelCombo.setSelectedItem(LogLevel.INFO);
      }
    }
  }

  private void copyConsoleTextToClipboard()
  {
    String consoleText = textArea.getText();
    StringSelection consoleTextSelection = new StringSelection(consoleText);
    Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
    cb.setContents(consoleTextSelection, null);
  }

  PipedOutputStream pout = null, perr = null;

  public void redirectStreams()
  {
    if (pout == null)
    {
      try
      {
        pout = new PipedOutputStream(this.pin);
        System.setOut(new PrintStream(pout, true));
      } catch (java.io.IOException io)
      {
        textArea.append("Couldn't redirect STDOUT to this console\n"
                + io.getMessage());
        io.printStackTrace(stderr);
      } catch (SecurityException se)
      {
        textArea.append("Couldn't redirect STDOUT to this console\n"
                + se.getMessage());
        se.printStackTrace(stderr);
      }

      try
      {
        perr = new PipedOutputStream(this.pin2);
        System.setErr(new PrintStream(perr, true));
      } catch (java.io.IOException io)
      {
        textArea.append("Couldn't redirect STDERR to this console\n"
                + io.getMessage());
        io.printStackTrace(stderr);
      } catch (SecurityException se)
      {
        textArea.append("Couldn't redirect STDERR to this console\n"
                + se.getMessage());
        se.printStackTrace(stderr);
      }
    }
  }

  public void unredirectStreams()
  {
    if (pout != null)
    {
      try
      {
        System.setOut(stdout);
        pout.flush();
        pout.close();
        pin = new PipedInputStream();
        pout = null;
      } catch (java.io.IOException io)
      {
        textArea.append("Couldn't unredirect STDOUT to this console\n"
                + io.getMessage());
        io.printStackTrace(stderr);
      } catch (SecurityException se)
      {
        textArea.append("Couldn't unredirect STDOUT to this console\n"
                + se.getMessage());
        se.printStackTrace(stderr);
      }

      try
      {
        System.setErr(stderr);
        perr.flush();
        perr.close();
        pin2 = new PipedInputStream();
        perr = null;
      } catch (java.io.IOException io)
      {
        textArea.append("Couldn't unredirect STDERR to this console\n"
                + io.getMessage());
        io.printStackTrace(stderr);
      } catch (SecurityException se)
      {
        textArea.append("Couldn't unredirect STDERR to this console\n"
                + se.getMessage());
        se.printStackTrace(stderr);
      }
    }
  }

  public void test()
  {
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

  private JFrame initFrame(String string, int i, int j, int x, int y)
  {
    JFrame frame = new JFrame(string);
    frame.setName(string);
    if (x == -1)
    {
      x = i / 2;
    }
    if (y == -1)
    {
      y = j / 2;
    }
    frame.setBounds(x, y, i, j);
    return frame;
  }

  /**
   * attach a console to the desktop - the desktop will open it if requested.
   * 
   * @param desktop
   */
  public Console(Desktop desktop)
  {
    this(desktop, true);
  }

  /**
   * attach a console to the desktop - the desktop will open it if requested.
   * 
   * @param desktop
   * @param showjconsole
   *          - if true, then redirect stdout immediately
   */
  public Console(Desktop desktop, boolean showjconsole)
  {
    parent = desktop;
    // window name - get x,y,width, height possibly scaled
    Rectangle bounds = desktop.getLastKnownDimensions("JAVA_CONSOLE_");
    if (bounds == null)
    {
      frame = initFrame(
              ChannelProperties.getProperty("app_name") + " Java Console",
              desktop.getWidth() / 2, desktop.getHeight() / 4,
              desktop.getX(), desktop.getY());
    }
    else
    {
      frame = initFrame(
              ChannelProperties.getProperty("app_name") + " Java Console",
              bounds.width, bounds.height, bounds.x, bounds.y);
    }
    frame.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
    // desktop.add(frame);
    initConsole(false);
    LogLevel level = (LogLevel) logLevelCombo.getSelectedItem();
    if (!Platform.isJS())
    {
      JalviewAppender jappender = new JalviewAppender(level);
      JalviewAppender.setTextArea(textArea);
      jappender.start();
      if (jalview.bin.Console.log != null
              && jalview.bin.Console.log instanceof JLoggerLog4j)
      {
        JLoggerLog4j.addAppender(jalview.bin.Console.log, jappender);
      }
    }
  }

  public synchronized void stopConsole()
  {
    quit = true;
    this.notifyAll();
    /*
     * reader.notify(); reader2.notify(); if (errorThrower!=null)
     * errorThrower.notify(); // stop all threads if (textAppender!=null)
     * textAppender.notify();
     */
    if (pout != null)
    {
      try
      {
        reader.join(10);
        pin.close();
      } catch (Exception e)
      {
      }
      try
      {
        reader2.join(10);
        pin2.close();
      } catch (Exception e)
      {
      }
      try
      {
        textAppender.join(10);
      } catch (Exception e)
      {
      }
    }
    /*
    if (!frame.isVisible())
    {
      frame.dispose();
    }
    */
    // System.exit(0);
  }

  @Override
  public synchronized void windowClosed(WindowEvent evt)
  {
    frame.setVisible(false);
    closeConsoleGui();
  }

  private void closeConsoleGui()
  {
    updateConsole = false;
    if (parent == null)
    {

      stopConsole();
    }
    else
    {
      parent.showConsole(false);
    }
  }

  @Override
  public synchronized void windowClosing(WindowEvent evt)
  {
    frame.setVisible(false); // default behaviour of JFrame
    closeConsoleGui();

    // frame.dispose();
  }

  @Override
  public synchronized void actionPerformed(ActionEvent evt)
  {
    trimBuffer(true);
    // textArea.setText("");
  }

  @Override
  public synchronized void run()
  {
    try
    {
      while (Thread.currentThread() == reader)
      {
        if (pin == null || pin.available() == 0)
        {
          try
          {
            this.wait(100);
            if (pin.available() == 0)
            {
              trimBuffer(false);
            }
          } catch (InterruptedException ie)
          {
          }
        }

        while (pin.available() != 0)
        {
          String input = this.readLine(pin);
          stdout.print(input);
          long time = System.nanoTime();
          appendToTextArea(input);
          // stderr.println("Time taken to stdout append:\t"
          // + (System.nanoTime() - time) + " ns");
          // lines++;
        }
        if (quit)
        {
          return;
        }
      }

      while (Thread.currentThread() == reader2)
      {
        if (pin2.available() == 0)
        {
          try
          {
            this.wait(100);
            if (pin2.available() == 0)
            {
              trimBuffer(false);
            }
          } catch (InterruptedException ie)
          {
          }
        }
        while (pin2.available() != 0)
        {
          String input = this.readLine(pin2);
          stderr.print(input);
          long time = System.nanoTime();
          appendToTextArea(input);
          // stderr.println("Time taken to stderr append:\t"
          // + (System.nanoTime() - time) + " ns");
          // lines++;
        }
        if (quit)
        {
          return;
        }
      }
      while (Thread.currentThread() == textAppender)
      {
        if (updateConsole)
        {
          // check string buffer - if greater than console, clear console and
          // replace with last segment of content, otherwise, append all to
          // content.
          long count;
          while (displayPipe.length() > 0)
          {
            count = 0;
            StringBuffer tmp = new StringBuffer(), replace;
            synchronized (displayPipe)
            {
              replace = displayPipe;
              displayPipe = tmp;
            }
            // simply append whole buffer
            textArea.append(replace.toString());
            count += replace.length();
            if (count > byteslim)
            {
              trimBuffer(false);
            }
          }
          if (displayPipe.length() == 0)
          {
            try
            {
              this.wait(100);
              if (displayPipe.length() == 0)
              {
                trimBuffer(false);
              }
            } catch (InterruptedException e)
            {
            }
          }
        }
        else
        {
          try
          {
            this.wait(100);
          } catch (InterruptedException e)
          {

          }
        }
        if (quit)
        {
          return;
        }

      }
    } catch (Exception e)
    {
      textArea.append("\nConsole reports an Internal error.");
      textArea.append("The error is: " + e.getMessage());
      // Need to uncomment this to ensure that line tally is synched.
      // lines += 2;
      stderr.println(
              "Console reports an Internal error.\nThe error is: " + e);
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

  private void appendToTextArea(final String input)
  {
    if (updateConsole == false)
    {
      // do nothing;
      return;
    }
    long time = System.nanoTime();
    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        displayPipe.append(input); // change to stringBuffer
        // displayPipe.flush();

      }
    });
    // stderr.println("Time taken to Spawnappend:\t" + (System.nanoTime() -
    // time)
    // + " ns");
  }

  private String header = null;

  private boolean updateConsole = false;

  private synchronized void trimBuffer(boolean clear)
  {
    if (header == null && textArea.getLineCount() > 5)
    {
      try
      {
        header = textArea.getText(0, textArea.getLineStartOffset(5))
                + "\nTruncated...\n";
      } catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    // trim the buffer
    int tlength = textArea.getDocument().getLength();
    if (header != null)
    {
      if (clear || (tlength > byteslim))
      {
        try
        {
          if (!clear)
          {
            long time = System.nanoTime();
            textArea.replaceRange(header, 0, tlength - bytescut);
            // stderr.println("Time taken to cut:\t"
            // + (System.nanoTime() - time) + " ns");
          }
          else
          {
            textArea.setText(header);
          }
        } catch (Exception e)
        {
          e.printStackTrace();
        }
        // lines = textArea.getLineCount();
      }
    }

  }

  public synchronized String readLine(PipedInputStream in)
          throws IOException
  {
    String input = "";
    int lp = -1;
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
      // counts lines - we don't do this for speed.
      // while ((lp = input.indexOf("\n", lp + 1)) > -1)
      // {
      // lines++;
      // }
    } while (!input.endsWith("\n") && !input.endsWith("\r\n") && !quit);
    return input;
  }

  /**
   * @j2sIgnore
   * @param arg
   */
  public static void main(String[] arg)
  {
    new Console().test(); // create console with not reference

  }

  public void setVisible(boolean selected)
  {
    frame.setVisible(selected);
    if (selected == true)
    {
      setChosenLogLevelCombo();
      redirectStreams();
      updateConsole = true;
      frame.toFront();
    }
    else
    {
      // reset log level to what it was before
      if (jalview.bin.Console.log != null)
      {
        jalview.bin.Console.log.setLevel(startingLogLevel);
      }

      unredirectStreams();
      updateConsole = false;
    }
  }

  public Rectangle getBounds()
  {
    if (frame != null)
    {
      return frame.getBounds();
    }
    return null;
  }

  /**
   * set the banner that appears at the top of the console output
   * 
   * @param string
   */
  public void setHeader(String string)
  {
    header = string;
    if (header.charAt(header.length() - 1) != '\n')
    {
      header += "\n";
    }
    textArea.insert(header, 0);
  }

  /**
   * get the banner
   * 
   * @return
   */
  public String getHeader()
  {
    return header;
  }
}
