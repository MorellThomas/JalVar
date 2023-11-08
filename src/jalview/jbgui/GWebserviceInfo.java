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

import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class GWebserviceInfo extends JPanel
{
  protected JTextArea infoText = new JTextArea();

  JScrollPane jScrollPane1 = new JScrollPane();

  JPanel jPanel1 = new JPanel();

  BorderLayout borderLayout1 = new BorderLayout();

  BorderLayout borderLayout2 = new BorderLayout();

  protected JPanel titlePanel = new JPanel();

  BorderLayout borderLayout3 = new BorderLayout();

  protected JPanel buttonPanel = new JPanel();

  public JLabel titleText = new JLabel();

  public JButton cancel = new JButton();

  public JButton showResultsNewFrame = new JButton();

  public JButton mergeResults = new JButton();

  GridBagLayout gridBagLayout1 = new GridBagLayout();

  public JPanel statusPanel = new JPanel(new GridLayout());

  public JLabel statusBar = new JLabel();

  /**
   * Creates a new GWebserviceInfo object.
   */
  public GWebserviceInfo()
  {
    try
    {
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
    infoText.setFont(new Font("Verdana", 0, 10));
    infoText.setBorder(null);
    infoText.setEditable(false);
    infoText.setText("");
    infoText.setLineWrap(true);
    infoText.setWrapStyleWord(true);
    this.setLayout(borderLayout1);
    jPanel1.setLayout(borderLayout2);
    titlePanel.setBackground(Color.white);
    titlePanel.setPreferredSize(new Dimension(0, 60));
    titlePanel.setLayout(borderLayout3);
    titleText.setFont(new Font("Arial", Font.BOLD, 12));
    titleText.setBorder(null);
    titleText.setText("");
    jScrollPane1.setBorder(null);
    jScrollPane1.setPreferredSize(new Dimension(400, 70));
    cancel.setFont(new Font("Verdana", 0, 11));
    cancel.setText(MessageManager.getString("action.cancel"));
    cancel.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        cancel_actionPerformed(e);
      }
    });
    buttonPanel.setLayout(gridBagLayout1);
    buttonPanel.setOpaque(false);
    showResultsNewFrame
            .setText(MessageManager.getString("label.new_window"));
    mergeResults.setText(MessageManager.getString("action.merge_results"));
    this.setBackground(Color.white);
    this.add(jPanel1, BorderLayout.NORTH);
    jPanel1.add(jScrollPane1, BorderLayout.CENTER);
    jScrollPane1.getViewport().add(infoText, null);
    jPanel1.add(titlePanel, BorderLayout.NORTH);
    titlePanel.add(buttonPanel, BorderLayout.EAST);
    buttonPanel.add(cancel,
            new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE,
                    new Insets(19, 6, 16, 4), 0, 0));
    this.add(statusPanel, java.awt.BorderLayout.SOUTH);
    statusPanel.add(statusBar, null);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void cancel_actionPerformed(ActionEvent e)
  {
  }
}
