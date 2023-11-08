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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import jalview.util.ChannelProperties;
import jalview.util.Platform;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class SplashScreen extends JPanel
        implements Runnable, HyperlinkListener
{
  private static final int SHOW_FOR_SECS = 5;

  private static final int FONT_SIZE = 11;

  private boolean visible = true;

  private JPanel iconimg = new JPanel(new BorderLayout());

  // could change fg, bg, font later to use ChannelProperties (these are not
  // actually being used!)
  private static Color bg = Color.WHITE;

  private static Color fg = Color.BLACK;

  private static Font font = new Font("SansSerif", Font.PLAIN, FONT_SIZE);

  /*
   * as JTextPane in Java, JLabel in javascript
   */
  private Component splashText;

  private JInternalFrame iframe;

  private Image image;

  private boolean transientDialog = false;

  private long oldTextLength = -1;

  public static int logoSize = 32;

  /*
   * allow click in the initial splash screen to dismiss it
   * immediately (not if opened from About menu)
   */
  private MouseAdapter closer = new MouseAdapter()
  {
    @Override
    public void mousePressed(MouseEvent evt)
    {
      if (transientDialog)
      {
        try
        {
          visible = false;
          closeSplash();
        } catch (Exception ex)
        {
        }
      }
    }
  };

  /**
   * Constructor that displays the splash screen
   * 
   * @param isTransient
   *          if true the panel removes itself on click or after a few seconds;
   *          if false it stays up until closed by the user
   */
  public SplashScreen(boolean isTransient)
  {
    Desktop.instance.acquireDialogQueue();
    this.transientDialog = isTransient;

    if (Platform.isJS()) // BH 2019
    {
      splashText = new JLabel("");
      run();
    }
    else
    {
      /**
       * Java only
       *
       * @j2sIgnore
       */
      {
        splashText = new JTextPane();
        splashText.setBackground(bg);
        splashText.setForeground(fg);
        splashText.setFont(font);
        Thread t = new Thread(this);
        t.start();
      }
    }
  }

  /**
   * ping the jalview version page then create and display the jalview
   * splashscreen window.
   */
  void initSplashScreenWindow()
  {
    addMouseListener(closer);

    try
    {
      if (!Platform.isJS())
      {
        image = ChannelProperties.getImage("banner");
        Image logo = ChannelProperties.getImage("logo.48");
        MediaTracker mt = new MediaTracker(this);
        if (image != null)
        {
          mt.addImage(image, 0);
        }
        if (logo != null)
        {
          mt.addImage(logo, 1);
        }
        do
        {
          try
          {
            mt.waitForAll();
          } catch (InterruptedException x)
          {
          }
          if (mt.isErrorAny())
          {
            System.err.println("Error when loading images!");
          }
        } while (!mt.checkAll());
        Desktop.instance.setIconImages(ChannelProperties.getIconList());
      }
    } catch (Exception ex)
    {
    }

    this.setBackground(bg);
    this.setForeground(fg);
    this.setFont(font);

    iframe = new JInternalFrame();
    iframe.setFrameIcon(null);
    iframe.setClosable(true);
    this.setLayout(new BorderLayout());
    iframe.setContentPane(this);
    iframe.setLayer(JLayeredPane.PALETTE_LAYER);
    iframe.setBackground(bg);
    iframe.setForeground(fg);
    iframe.setFont(font);

    if (Platform.isJS())
    {
      // ignore in JavaScript
    }
    else
    /**
     * Java only
     * 
     * @j2sIgnore
     */
    {
      ((JTextPane) splashText).setEditable(false);
      splashText.setBackground(bg);
      splashText.setForeground(fg);
      splashText.setFont(font);

      SplashImage splashimg = new SplashImage(image);
      iconimg.add(splashimg, BorderLayout.LINE_START);
      iconimg.setBackground(bg);
      add(iconimg, BorderLayout.NORTH);
    }
    add(splashText, BorderLayout.CENTER);
    splashText.addMouseListener(closer);
    Desktop.desktop.add(iframe);
    refreshText();
  }

  /**
   * update text in author text panel reflecting current version information
   */
  protected boolean refreshText()
  {
    String newtext = Desktop.instance.getAboutMessage();
    // System.err.println("Text found: \n"+newtext+"\nEnd of newtext.");
    if (oldTextLength != newtext.length())
    {
      iframe.setVisible(false);
      oldTextLength = newtext.length();
      if (Platform.isJS()) // BH 2019
      {
        /*
         * SwingJS doesn't have HTMLEditorKit, required for a JTextPane
         * to display formatted html, so we use a simple alternative
         */
        String text = "<html><br><img src=\""
                + ChannelProperties.getImageURL("banner") + "\"/>" + newtext
                + "<br></html>";
        JLabel ta = new JLabel(text);
        ta.setOpaque(true);
        ta.setBackground(Color.white);
        splashText = ta;
      }
      else
      /**
       * Java only
       *
       * @j2sIgnore
       */
      {
        JTextPane jtp = new JTextPane();
        jtp.setEditable(false);
        jtp.setBackground(bg);
        jtp.setForeground(fg);
        jtp.setFont(font);
        jtp.setContentType("text/html");
        jtp.setText("<html>" + newtext + "</html>");
        jtp.addHyperlinkListener(this);
        splashText = jtp;
      }
      splashText.addMouseListener(closer);

      splashText.setVisible(true);
      splashText.setSize(new Dimension(750,
              425 + logoSize + (Platform.isJS() ? 40 : 0)));
      splashText.setBackground(bg);
      splashText.setForeground(fg);
      splashText.setFont(font);
      add(splashText, BorderLayout.CENTER);
      revalidate();
      int width = Math.max(splashText.getWidth(), iconimg.getWidth());
      int height = splashText.getHeight() + iconimg.getHeight();
      iframe.setBounds(
              Math.max(0, (Desktop.instance.getWidth() - width) / 2),
              Math.max(0, (Desktop.instance.getHeight() - height) / 2),
              width, height);
      iframe.validate();
      iframe.setVisible(true);
      return true;
    }
    return false;
  }

  /**
   * Create splash screen, display it and clear it off again.
   */
  @Override
  public void run()
  {
    initSplashScreenWindow();

    long startTime = System.currentTimeMillis() / 1000;

    while (visible)
    {
      iframe.repaint();
      try
      {
        Thread.sleep(500);
      } catch (Exception ex)
      {
      }

      if (transientDialog && ((System.currentTimeMillis() / 1000)
              - startTime) > SHOW_FOR_SECS)
      {
        visible = false;
      }

      if (visible && refreshText())
      {
        iframe.repaint();
      }
      if (!transientDialog)
      {
        return;
      }
    }

    closeSplash();
    Desktop.instance.releaseDialogQueue();
  }

  /**
   * DOCUMENT ME!
   */
  public void closeSplash()
  {
    try
    {

      iframe.setClosed(true);
    } catch (Exception ex)
    {
    }
  }

  public class SplashImage extends JPanel
  {
    Image image;

    public SplashImage(Image todisplay)
    {
      image = todisplay;
      if (image != null)
      {
        setPreferredSize(new Dimension(image.getWidth(this) + 8,
                image.getHeight(this)));
      }
    }

    @Override
    public Dimension getPreferredSize()
    {
      return new Dimension(image.getWidth(this) + 8, image.getHeight(this));
    }

    @Override
    public void paintComponent(Graphics g)
    {
      g.setColor(bg);
      g.fillRect(0, 0, getWidth(), getHeight());
      g.setColor(fg);
      g.setFont(new Font(font.getFontName(), Font.BOLD, FONT_SIZE + 6));

      if (image != null)
      {
        g.drawImage(image, (getWidth() - image.getWidth(this)) / 2,
                (getHeight() - image.getHeight(this)) / 2, this);
      }
    }
  }

  @Override
  public void hyperlinkUpdate(HyperlinkEvent e)
  {
    Desktop.hyperlinkUpdate(e);

  }
}
