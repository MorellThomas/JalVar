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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.StringWriter;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import jalview.bin.Cache;
import jalview.io.JalviewFileChooser;
import jalview.io.JalviewFileView;
import jalview.jbgui.GCutAndPasteHtmlTransfer;
import jalview.util.MessageManager;
import jalview.viewmodel.AlignmentViewport;

/**
 * Cut'n'paste files into the desktop See JAL-1105
 * 
 * @author $author$
 * @version $Revision$
 */
public class CutAndPasteHtmlTransfer extends GCutAndPasteHtmlTransfer
{

  AlignmentViewport viewport;

  public CutAndPasteHtmlTransfer()
  {
    super();
    this.setFrameIcon(null);
    displaySource.setSelected(false);
    textarea.addKeyListener(new KeyListener()
    {

      @Override
      public void keyTyped(KeyEvent arg0)
      {
        // if (arg0.isControlDown() && arg0.getKeyCode()==KeyEvent.VK_C)
        // {
        // copyItem_actionPerformed(null);
        // }
        arg0.consume();
      }

      @Override
      public void keyReleased(KeyEvent arg0)
      {
        // TODO Auto-generated method stub

      }

      @Override
      public void keyPressed(KeyEvent arg0)
      {
        // TODO Auto-generated method stub

      }
    });
    textarea.setEditable(false);
    textarea.addHyperlinkListener(new HyperlinkListener()
    {

      @Override
      public void hyperlinkUpdate(HyperlinkEvent e)
      {
        if (e.getEventType().equals(EventType.ACTIVATED))
        {
          Desktop.showUrl(e.getURL().toExternalForm());
        }
      }
    });
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        textarea.requestFocus();
      }
    });

  }

  /**
   * DOCUMENT ME!
   */
  public void setForInput(AlignmentViewport viewport)
  {
    this.viewport = viewport;
    if (viewport != null)
    {
      ok.setText(MessageManager.getString("action.add"));
    }

    getContentPane().add(inputButtonPanel, java.awt.BorderLayout.SOUTH);
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String getText()
  {
    return textarea.getText();
  }

  /**
   * Set contents of HTML Display pane
   * 
   * @param text
   *          HTML text
   */
  public void setText(String text)
  {
    textarea.setDocument(textarea.getEditorKit().createDefaultDocument());
    textarea.setText(text);
    textarea.setCaretPosition(0);
  }

  @Override
  public void save_actionPerformed(ActionEvent e)
  {
    // TODO: JAL-3048 JalviewFileChooser - Save option

    JalviewFileChooser chooser = new JalviewFileChooser(
            Cache.getProperty("LAST_DIRECTORY"));

    chooser.setAcceptAllFileFilterUsed(false);
    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(
            MessageManager.getString("label.save_text_to_file"));
    chooser.setToolTipText(MessageManager.getString("action.save"));

    int value = chooser.showSaveDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      try
      {
        java.io.PrintWriter out = new java.io.PrintWriter(
                new java.io.FileWriter(chooser.getSelectedFile()));

        out.print(getText());
        out.close();
      } catch (Exception ex)
      {
        ex.printStackTrace();
      }

    }
  }

  @Override
  public void toggleHtml_actionPerformed(ActionEvent e)
  {
    String txt = textarea.getText();
    textarea.setContentType(
            displaySource.isSelected() ? "text/text" : "text/html");
    textarea.setText(txt);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void copyItem_actionPerformed(ActionEvent e)
  {
    Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
    StringWriter sw = new StringWriter();
    try
    {
      textarea.getEditorKit().write(sw, textarea.getDocument(),
              textarea.getSelectionStart(),
              textarea.getSelectionEnd() - textarea.getSelectionStart());
    } catch (Exception x)
    {
    }
    ;
    StringSelection ssel = new StringSelection(sw.getBuffer().toString());
    c.setContents(ssel, ssel);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void cancel_actionPerformed(ActionEvent e)
  {
    try
    {
      this.setClosed(true);
    } catch (Exception ex)
    {
    }
  }

  @Override
  public void textarea_mousePressed(MouseEvent e)
  {
    // isPopupTrigger is on mousePressed (Mac) or mouseReleased (Windows)
    if (e.isPopupTrigger())
    {
      JPopupMenu popup = new JPopupMenu(
              MessageManager.getString("action.edit"));
      JMenuItem item = new JMenuItem(
              MessageManager.getString("action.copy"));
      item.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          copyItem_actionPerformed(e);
        }
      });
      popup.add(item);
      popup.show(this, e.getX() + 10, e.getY() + textarea.getY() + 40);

    }
  }

}
