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

import jalview.bin.Jalview;
import jalview.gui.JvSwingUtils;
import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.colorchooser.AbstractColorChooserPanel;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class GUserDefinedColours extends JPanel
{
  protected JColorChooser colorChooser = new JColorChooser();

  protected JPanel buttonPanel = new JPanel();

  protected GridLayout gridLayout = new GridLayout();

  JPanel lowerPanel = new JPanel();

  protected JButton okButton = new JButton();

  protected JButton applyButton = new JButton();

  protected JButton loadbutton = new JButton();

  protected JButton savebutton = new JButton();

  protected JButton cancelButton = new JButton();

  JPanel namePanel = new JPanel();

  JLabel jLabel1 = new JLabel();

  public JTextField schemeName = new JTextField();

  BorderLayout borderLayout1 = new BorderLayout();

  JPanel panel1 = new JPanel();

  JPanel okCancelPanel = new JPanel();

  JPanel saveLoadPanel = new JPanel();

  BorderLayout borderLayout3 = new BorderLayout();

  GridBagLayout gridBagLayout1 = new GridBagLayout();

  BorderLayout borderLayout2 = new BorderLayout();

  FlowLayout flowLayout1 = new FlowLayout();

  BorderLayout borderLayout4 = new BorderLayout();

  JPanel jPanel4 = new JPanel();

  BorderLayout borderLayout5 = new BorderLayout();

  JLabel label = new JLabel();

  protected JPanel casePanel = new JPanel();

  public JCheckBox caseSensitive = new JCheckBox();

  public JCheckBox lcaseColour = new JCheckBox();

  protected List<JButton> selectedButtons;

  /**
   * Creates a new GUserDefinedColours object.
   */
  public GUserDefinedColours()
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
    this.setLayout(borderLayout4);
    buttonPanel.setLayout(gridLayout);
    gridLayout.setColumns(4);
    gridLayout.setRows(5);
    okButton.setFont(new java.awt.Font("Verdana", 0, 11));
    okButton.setText(MessageManager.getString("action.ok"));
    okButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        okButton_actionPerformed();
      }
    });
    applyButton.setFont(new java.awt.Font("Verdana", 0, 11));
    applyButton.setText(MessageManager.getString("action.apply"));
    applyButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        applyButton_actionPerformed();
      }
    });
    loadbutton.setFont(new java.awt.Font("Verdana", 0, 11));
    loadbutton.setText(MessageManager.getString("action.load_scheme"));
    loadbutton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        loadbutton_actionPerformed();
      }
    });
    savebutton.setFont(new java.awt.Font("Verdana", 0, 11));
    savebutton.setText(MessageManager.getString("action.save_scheme"));
    savebutton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        savebutton_actionPerformed();
      }
    });
    cancelButton.setFont(JvSwingUtils.getLabelFont());
    cancelButton.setText(MessageManager.getString("action.cancel"));
    cancelButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        cancelButton_actionPerformed();
      }
    });
    this.setBackground(new Color(212, 208, 223));
    lowerPanel.setOpaque(false);
    lowerPanel.setLayout(borderLayout3);
    colorChooser.setOpaque(false);
    jLabel1.setFont(JvSwingUtils.getLabelFont());
    jLabel1.setText(MessageManager.getString("label.name"));
    namePanel.setMinimumSize(new Dimension(300, 31));
    namePanel.setOpaque(false);
    namePanel.setPreferredSize(new Dimension(240, 25));
    namePanel.setLayout(borderLayout1);
    schemeName.setFont(JvSwingUtils.getLabelFont());
    schemeName.setPreferredSize(new Dimension(105, 21));
    schemeName.setText("");
    schemeName.setHorizontalAlignment(SwingConstants.CENTER);
    panel1.setLayout(flowLayout1);
    panel1.setOpaque(false);
    okCancelPanel.setOpaque(false);
    saveLoadPanel.setOpaque(false);
    jPanel4.setLayout(borderLayout5);
    label.setFont(new java.awt.Font("Verdana", Font.ITALIC, 10));
    label.setOpaque(false);
    label.setPreferredSize(new Dimension(260, 34));
    label.setText(
            MessageManager.formatMessage("label.html_content", new String[]
            { MessageManager.getString(
                    "label.save_colour_scheme_with_unique_name_added_to_colour_menu") }));
    caseSensitive.setText(MessageManager.getString("label.case_sensitive"));
    caseSensitive.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        caseSensitive_actionPerformed();
      }
    });
    lcaseColour
            .setText(MessageManager.getString("label.lower_case_colour"));
    lcaseColour.setToolTipText(
            MessageManager.getString("label.lower_case_tip"));

    saveLoadPanel.add(savebutton);
    saveLoadPanel.add(loadbutton);
    okCancelPanel.add(applyButton);
    okCancelPanel.add(okButton);
    okCancelPanel.add(cancelButton);
    lowerPanel.add(saveLoadPanel, java.awt.BorderLayout.NORTH);
    lowerPanel.add(okCancelPanel, java.awt.BorderLayout.SOUTH);

    namePanel.add(schemeName, java.awt.BorderLayout.CENTER);
    namePanel.add(jLabel1, java.awt.BorderLayout.WEST);
    panel1.add(namePanel, null);
    panel1.add(buttonPanel, null);
    panel1.add(casePanel);
    casePanel.add(caseSensitive);
    casePanel.add(lcaseColour);
    panel1.add(lowerPanel, null);
    panel1.add(label);

    jPanel4.add(panel1, java.awt.BorderLayout.CENTER);
    this.add(jPanel4, java.awt.BorderLayout.CENTER);
    this.add(colorChooser, java.awt.BorderLayout.EAST);

    AbstractColorChooserPanel[] choosers = colorChooser.getChooserPanels();
    // JAL-1360 larger JColorChooser in Java 7 overwrites AA panel; restrict to
    // swatch picker only
    if (choosers.length > 3)
    {
      // Java 7 default has 5 options rather than 3 for choosing colours; keep
      // the first only
      colorChooser
              .setChooserPanels(new AbstractColorChooserPanel[]
              { choosers[0] });
    }

    selectedButtons = new ArrayList<JButton>();
  }

  /**
   * DOCUMENT ME!
   */
  protected void okButton_actionPerformed()
  {
  }

  /**
   * DOCUMENT ME!
   */
  protected void applyButton_actionPerformed()
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void loadbutton_actionPerformed()
  {
  }

  protected void savebutton_actionPerformed()
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void cancelButton_actionPerformed()
  {
  }

  public void caseSensitive_actionPerformed()
  {

  }

  public void lcaseColour_actionPerformed()
  {

  }
}
