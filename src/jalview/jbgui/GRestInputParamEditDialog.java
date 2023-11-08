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

import jalview.gui.JvSwingUtils;
import jalview.gui.OptsAndParamsPage;
import jalview.util.MessageManager;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

public class GRestInputParamEditDialog
{

  protected JPanel dpane;

  protected JPanel okcancel;

  protected JList typeList;

  protected JTextField tok;

  protected JPanel options;

  protected JPanel optionsPanel;

  public GRestInputParamEditDialog()
  {
    jbInit();
  }

  protected void jbInit()
  {
    dpane = new JPanel(new MigLayout("", "[][][fill]", "[][fill][]"));
    dpane.setPreferredSize(
            new Dimension(110 + 100 + OptsAndParamsPage.PARAM_WIDTH, 400));
    typeList = new JList();
    typeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    typeList.getSelectionModel()
            .addListSelectionListener(new ListSelectionListener()
            {

              @Override
              public void valueChanged(ListSelectionEvent e)
              {
                type_SelectionChangedActionPerformed(e);
              };
            });

    tok = new JTextField();
    tok.addKeyListener(new KeyListener()
    {

      @Override
      public void keyTyped(KeyEvent e)
      {
      }

      @Override
      public void keyReleased(KeyEvent e)
      {
        tokChanged_actionPerformed();
      }

      @Override
      public void keyPressed(KeyEvent e)
      {

      }
    });
    options = new JPanel(new MigLayout("", "[grow 100,fill]", ""));
    optionsPanel = new JPanel(new MigLayout("", "[fill]", "[fill]"));
    JScrollPane optionView = new JScrollPane();
    optionView.setViewportView(options);
    JvSwingUtils.mgAddtoLayout(dpane,
            MessageManager.getString("label.input_parameter_name"),
            new JLabel(MessageManager.getString("label.name")), tok,
            "grow,spanx 3,wrap");
    JPanel paramsType = new JPanel(
            new MigLayout("", "[grow 100,fill]", "[grow 100,fill]"));
    paramsType.setBorder(new TitledBorder(
            MessageManager.getString("label.select_input_type")));
    JScrollPane jlistScroller = new JScrollPane();
    jlistScroller.setViewportView(typeList);
    paramsType.add(jlistScroller, "spanx 2,spany 2");
    dpane.add(paramsType);
    optionsPanel.setBorder(new TitledBorder(
            MessageManager.getString("label.set_options_for_type")));
    optionsPanel.add(optionView);
    dpane.add(optionsPanel, "wrap");
    okcancel = new JPanel(new MigLayout("", "[center][center]", "[]"));
    dpane.add(okcancel, "spanx 3,wrap");

  }

  protected void tokChanged_actionPerformed()
  {

  }

  protected void type_SelectionChangedActionPerformed(ListSelectionEvent e)
  {
  }

}
