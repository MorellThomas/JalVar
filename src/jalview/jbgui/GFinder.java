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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import jalview.datamodel.AlignmentI;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.FormatAdapter;
import jalview.io.cache.JvCacheableInputBox;
import jalview.util.MessageManager;

public class GFinder extends JPanel
{
  private static final java.awt.Font VERDANA_12 = new Font("Verdana",
          Font.PLAIN, 12);

  private static final String FINDER_CACHE_KEY = "CACHE.FINDER";

  /*
   * if more checkboxes are wanted, increase this value
   * and add to centrePanel in jbInit()  
   */
  private static final int PANEL_ROWS = 4;

  protected JButton createFeatures;

  protected JButton copyToClipboard;

  protected JvCacheableInputBox<String> searchBox;

  protected JCheckBox caseSensitive;

  protected JCheckBox searchDescription;

  protected JCheckBox searchFeatures;

  protected JCheckBox ignoreHidden;

  public GFinder()
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
   * Constructs the widgets and adds them to the layout
   */
  private void jbInit() throws Exception
  {
    /*
     * border layout
     * West: 4 rows
     *   first row 'Find'
     *   remaining rows empty
     * Center: 4 rows
     *   first row search box
     *   second row 'match case' checkbox
     *   third row 'include description' checkbox
     *   fourth row 'ignore hidden' checkbox
     * East: four rows
     *   first row 'find next' button
     *   second row 'find all' button
     *   third row 'new feature' button
     *   fourth row empty
     */
    this.setLayout(new BorderLayout());
    JPanel eastPanel = new JPanel();
    eastPanel.setLayout(new GridLayout(PANEL_ROWS, 1));
    this.add(eastPanel, BorderLayout.EAST);
    JPanel centrePanel = new JPanel();
    centrePanel.setLayout(new GridLayout(PANEL_ROWS, 1));
    this.add(centrePanel, BorderLayout.CENTER);
    JPanel westPanel = new JPanel();
    westPanel.setLayout(new GridLayout(PANEL_ROWS, 1));
    this.add(westPanel, BorderLayout.WEST);

    /*
     * 'Find' prompt goes top left
     */
    JLabel findLabel = new JLabel(
            " " + MessageManager.getString("label.find") + " ");
    findLabel.setFont(VERDANA_12);
    westPanel.add(findLabel);

    /*
     * search box
     */
    searchBox = new JvCacheableInputBox<>(FINDER_CACHE_KEY, 25);
    searchBox.getComponent().setFont(VERDANA_12);
    searchBox.addCaretListener(new CaretListener()
    {
      @Override
      public void caretUpdate(CaretEvent e)
      {
        textfield_caretUpdate();
      }
    });
    searchBox.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent e)
      {
        textfield_keyPressed(e);
      }
    });
    centrePanel.add(searchBox.getComponent());

    /*
     * search options checkboxes
     */
    caseSensitive = new JCheckBox();
    caseSensitive.setHorizontalAlignment(SwingConstants.LEFT);
    caseSensitive.setText(MessageManager.getString("label.match_case"));

    searchDescription = new JCheckBox();
    searchDescription
            .setText(MessageManager.getString("label.include_description"));

    searchFeatures = new JCheckBox();
    searchFeatures
            .setText(MessageManager.getString("label.include_features"));

    ignoreHidden = new JCheckBox();
    ignoreHidden.setText(MessageManager.getString("label.ignore_hidden"));
    ignoreHidden.setToolTipText(
            MessageManager.getString("label.ignore_hidden_tooltip"));

    centrePanel.add(caseSensitive);
    centrePanel.add(searchDescription);
    centrePanel.add(searchFeatures);
    centrePanel.add(ignoreHidden);

    /*
     * action buttons
     */
    JButton findAll = new JButton(
            MessageManager.getString("action.find_all"));
    findAll.setFont(VERDANA_12);
    findAll.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        findAll_actionPerformed();
      }
    });
    JButton findNext = new JButton(
            MessageManager.getString("action.find_next"));
    findNext.setFont(VERDANA_12);
    findNext.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        findNext_actionPerformed();
      }
    });
    createFeatures = new JButton();
    createFeatures.setEnabled(false);
    createFeatures.setFont(VERDANA_12);
    createFeatures.setText(MessageManager.getString("label.new_feature"));
    createFeatures.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        createFeatures_actionPerformed();
      }
    });
    copyToClipboard = new JButton();
    copyToClipboard.setEnabled(false);
    copyToClipboard.setFont(VERDANA_12);
    copyToClipboard.setText(MessageManager.getString("label.copy"));
    copyToClipboard.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        copyToClipboard_actionPerformed();
      }
    });
    eastPanel.add(findNext);
    eastPanel.add(findAll);
    eastPanel.add(createFeatures);
    eastPanel.add(copyToClipboard);
  }

  protected void copyToClipboard_actionPerformed()
  {
  }

  protected void textfield_keyPressed(KeyEvent e)
  {
    if (e.getKeyCode() == KeyEvent.VK_ENTER)
    {
      if (!searchBox.isPopupVisible())
      {
        e.consume();
        findNext_actionPerformed();
      }
    }
  }

  protected void findNext_actionPerformed()
  {
  }

  protected void findAll_actionPerformed()
  {
  }

  public void createFeatures_actionPerformed()
  {
  }

  public void textfield_caretUpdate()
  {
    // disabled as appears to be running a non-functional
    if (false && searchBox.getUserInput().indexOf(">") > -1)
    {
      SwingUtilities.invokeLater(new Runnable()
      {
        @Override
        public void run()
        {
          String str = searchBox.getUserInput();
          AlignmentI al = null;
          try
          {
            al = new FormatAdapter().readFile(str, DataSourceType.PASTE,
                    FileFormat.Fasta);
          } catch (Exception ex)
          {
          }
          if (al != null && al.getHeight() > 0)
          {
            str = jalview.analysis.AlignSeq.extractGaps(
                    jalview.util.Comparison.GapChars,
                    al.getSequenceAt(0).getSequenceAsString());
            // todo and what? set str as searchBox text?
          }
        }
      });
    }
  }

}
