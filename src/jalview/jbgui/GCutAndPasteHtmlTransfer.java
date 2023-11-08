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
package jalview.jbgui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;

import jalview.gui.JvSwingUtils;
import jalview.util.MessageManager;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class GCutAndPasteHtmlTransfer extends JInternalFrame
{
  protected JEditorPane textarea = new JEditorPane("text/html", "");

  protected JScrollPane scrollPane = new JScrollPane();

  BorderLayout borderLayout1 = new BorderLayout();

  JMenuBar editMenubar = new JMenuBar();

  JMenu editMenu = new JMenu();

  JMenuItem copyItem = new JMenuItem();

  protected JCheckBoxMenuItem displaySource = new JCheckBoxMenuItem();

  BorderLayout borderLayout2 = new BorderLayout();

  protected JPanel inputButtonPanel = new JPanel();

  protected JButton ok = new JButton();

  JButton cancel = new JButton();

  JMenuItem close = new JMenuItem();

  JMenuItem selectAll = new JMenuItem();

  JMenu jMenu1 = new JMenu();

  JMenuItem save = new JMenuItem();

  /**
   * Creates a new GCutAndPasteTransfer object.
   */
  public GCutAndPasteHtmlTransfer()
  {
    try
    {
      textarea.setEditorKit(new HTMLEditorKit());
      setJMenuBar(editMenubar);
      jbInit();
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @throws Exception
   *           DOCUMENT ME!
   */
  private void jbInit() throws Exception
  {
    setFrameIcon(null);
    scrollPane.setBorder(null);
    ok.setFont(JvSwingUtils.getLabelFont());
    ok.setText(MessageManager.getString("label.new_window"));
    ok.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        ok_actionPerformed(e);
      }
    });
    cancel.setText(MessageManager.getString("action.close"));
    cancel.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        cancel_actionPerformed(e);
      }
    });
    textarea.setBorder(null);
    close.setText(MessageManager.getString("action.close"));
    close.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        cancel_actionPerformed(e);
      }
    });
    close.setAccelerator(
            javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W,
                    jalview.util.ShortcutKeyMaskExWrapper
                            .getMenuShortcutKeyMaskEx(),
                    false));
    selectAll.setText(MessageManager.getString("action.select_all"));
    selectAll
            .setAccelerator(
                    javax.swing.KeyStroke
                            .getKeyStroke(java.awt.event.KeyEvent.VK_A,
                                    jalview.util.ShortcutKeyMaskExWrapper
                                            .getMenuShortcutKeyMaskEx(),
                                    false));
    selectAll.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        selectAll_actionPerformed(e);
      }
    });
    jMenu1.setText(MessageManager.getString("action.file"));
    save.setText(MessageManager.getString("action.save"));
    save.setAccelerator(
            javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S,
                    jalview.util.ShortcutKeyMaskExWrapper
                            .getMenuShortcutKeyMaskEx(),
                    false));
    save.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        save_actionPerformed(e);
      }
    });
    copyItem.setAccelerator(
            javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C,
                    jalview.util.ShortcutKeyMaskExWrapper
                            .getMenuShortcutKeyMaskEx(),
                    false));

    editMenubar.add(jMenu1);
    editMenubar.add(editMenu);
    textarea.setFont(new java.awt.Font("Monospaced", Font.PLAIN, 12));
    textarea.addMouseListener(new java.awt.event.MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        textarea_mousePressed(e);
      }

      @Override
      public void mouseReleased(MouseEvent e)
      {
        textarea_mousePressed(e);
      }
    });
    editMenu.setText(MessageManager.getString("action.edit"));
    copyItem.setText(MessageManager.getString("action.copy"));
    copyItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        copyItem_actionPerformed(e);
      }
    });
    displaySource
            .setText(MessageManager.getString("action.show_html_source"));
    displaySource.setToolTipText(
            MessageManager.getString("label.select_copy_raw_html"));
    displaySource.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        toggleHtml_actionPerformed(arg0);
      }
    });
    editMenu.add(displaySource);
    this.getContentPane().setLayout(borderLayout2);
    scrollPane.setBorder(null);
    scrollPane.getViewport().add(textarea, null);
    editMenu.add(selectAll);
    editMenu.add(copyItem);
    this.getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);
    inputButtonPanel.add(ok);
    inputButtonPanel.add(cancel);
    jMenu1.add(save);
    jMenu1.add(close);
  }

  protected void toggleHtml_actionPerformed(ActionEvent arg0)
  {
    // TODO Auto-generated method stub

  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void textarea_mousePressed(MouseEvent e)
  {

  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void copyItem_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void ok_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void cancel_actionPerformed(ActionEvent e)
  {
  }

  public void selectAll_actionPerformed(ActionEvent e)
  {
    textarea.selectAll();
  }

  public void save_actionPerformed(ActionEvent e)
  {

  }

  /**
   * Adds the given stylesheet rule to the Html editor. However note that CSS
   * support is limited.
   * 
   * @param rule
   * @see javax.swing.text.html.CSS
   */
  public void addStylesheetRule(String rule)
  {
    EditorKit editorKit = textarea.getEditorKit();
    if (editorKit != null)
    {
      ((HTMLEditorKit) editorKit).getStyleSheet().addRule(rule);
    }
  }
}
