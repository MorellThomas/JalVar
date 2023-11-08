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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class GSliderPanel extends JPanel
{
  private static final Font VERDANA_11 = new java.awt.Font("Verdana", 0,
          11);

  protected static final int FRAME_WIDTH = 420;

  protected static final int FRAME_HEIGHT = 120;

  // this is used for conservation colours, PID colours and redundancy threshold
  protected JSlider slider = new JSlider();

  protected JTextField valueField = new JTextField();

  protected JLabel label = new JLabel();

  protected JPanel southPanel = new JPanel();

  protected JButton applyButton = new JButton();

  protected JButton undoButton = new JButton();

  protected JCheckBox allGroupsCheck = new JCheckBox();

  /**
   * Creates a new GSliderPanel object.
   */
  public GSliderPanel()
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
   * Constructs and lays out the controls
   */
  private void jbInit()
  {
    slider.setMajorTickSpacing(10);
    slider.setMinorTickSpacing(1);
    slider.setPaintTicks(true);
    slider.setBackground(Color.white);
    slider.setFont(VERDANA_11);
    slider.setDoubleBuffered(true);
    slider.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseReleased(MouseEvent e)
      {
        slider_mouseReleased(e);
      }
    });
    valueField.setFont(VERDANA_11);
    valueField.setMinimumSize(new Dimension(6, 14));
    valueField.setPreferredSize(new Dimension(50, 12));
    valueField.setText("");
    valueField.setHorizontalAlignment(SwingConstants.CENTER);
    valueField.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        valueField_actionPerformed();
      }
    });
    valueField.addFocusListener(new FocusAdapter()
    {
      @Override
      public void focusLost(FocusEvent e)
      {
        valueField_actionPerformed();
      }
    });
    label.setFont(VERDANA_11);
    label.setOpaque(false);
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setText(MessageManager.getString("label.set_this_label_text"));

    applyButton.setFont(VERDANA_11);
    applyButton.setOpaque(false);
    applyButton.setText(MessageManager.getString("action.apply"));
    applyButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        applyButton_actionPerformed(e);
      }
    });
    undoButton.setEnabled(false);
    undoButton.setFont(VERDANA_11);
    undoButton.setOpaque(false);
    undoButton.setText(MessageManager.getString("action.undo"));
    undoButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        undoButton_actionPerformed(e);
      }
    });
    allGroupsCheck.setEnabled(false);
    allGroupsCheck.setFont(VERDANA_11);
    allGroupsCheck.setOpaque(false);
    allGroupsCheck
            .setText(MessageManager.getString("action.apply_all_groups"));

    this.setLayout(new GridLayout(2, 0));
    this.setBackground(Color.white);

    JPanel firstRow = new JPanel(new FlowLayout());
    firstRow.setOpaque(false);
    firstRow.add(label);
    firstRow.add(applyButton);
    firstRow.add(undoButton);
    this.add(firstRow);

    JPanel jPanel1 = new JPanel(new BorderLayout());
    jPanel1.setOpaque(false);
    jPanel1.add(valueField, BorderLayout.CENTER);
    jPanel1.add(allGroupsCheck, BorderLayout.EAST);

    southPanel.setLayout(new BorderLayout());
    southPanel.setOpaque(false);
    southPanel.add(jPanel1, BorderLayout.EAST);
    southPanel.add(slider, BorderLayout.CENTER);
    this.add(southPanel);
  }

  /**
   * Action on changing the slider text field value
   */
  protected void valueField_actionPerformed()
  {
    try
    {
      int i = Integer.valueOf(valueField.getText());
      slider.setValue(i);
    } catch (NumberFormatException ex)
    {
      valueField.setText(String.valueOf(slider.getValue()));
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void applyButton_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void undoButton_actionPerformed(ActionEvent e)
  {
  }

  public void slider_mouseReleased(MouseEvent e)
  {

  }
}
