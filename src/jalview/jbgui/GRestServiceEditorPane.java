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
import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

public class GRestServiceEditorPane extends JPanel
{

  protected JTabbedPane panels;

  protected JPanel details, inputs, paste;

  protected JTextArea urldesc, url, urlsuff, name, descr, parseRes;

  protected JComboBox action, gapChar;

  JLabel acttype;

  protected JButton okButton;

  protected JButton cancelButton;

  JPanel svcattribs;

  JPanel status;

  protected JList iprms;

  protected JList rdata;

  JScrollPane iprmVp, rdataVp, parseResVp, urlVp, descrVp, urldescVp;

  JButton rdataAdd, rdataRem, rdataNdown, rdataNup;

  JButton iprmsAdd, iprmsRem;

  protected JCheckBox hSeparable;

  protected JCheckBox vSeparable;

  protected JPanel parseWarnings;

  public GRestServiceEditorPane()
  {
    jbInit();
  }

  protected void jbInit()
  {
    details = new JPanel();
    details.setName(MessageManager.getString("label.details"));
    details.setLayout(new MigLayout());
    inputs = new JPanel();
    inputs.setName(MessageManager.getString("label.input_output"));
    inputs.setLayout(new MigLayout("", "[grow 85,fill][]", ""));
    paste = new JPanel();
    paste.setName(MessageManager.getString("label.cut_paste"));
    paste.setLayout(
            new MigLayout("", "[grow 100, fill]", "[][grow 100,fill]"));

    panels = new JTabbedPane();
    panels.addTab(details.getName(), details);
    panels.addTab(inputs.getName(), inputs);
    panels.addTab(paste.getName(), paste);

    JPanel cpanel;

    // Name and URL Panel
    cpanel = details;
    name = new JTextArea(1, 12);

    JvSwingUtils.mgAddtoLayout(cpanel,
            MessageManager
                    .getString("label.short_descriptive_name_for_service"),
            new JLabel(MessageManager.getString("label.name")), name,
            "wrap");
    action = new JComboBox();
    JvSwingUtils.mgAddtoLayout(cpanel,
            MessageManager.getString("label.function_service_performs"),
            new JLabel(MessageManager.getString("label.service_action")),
            action, "wrap");
    descr = new JTextArea(4, 60);
    descrVp = new JScrollPane();
    descrVp.setViewportView(descr);
    JvSwingUtils.mgAddtoLayout(cpanel,
            MessageManager.getString("label.brief_description_service"),
            new JLabel(MessageManager.getString("label.description")),
            descrVp, "wrap");

    url = new JTextArea(2, 60);
    urlVp = new JScrollPane();
    urlVp.setViewportView(url);
    JvSwingUtils.mgAddtoLayout(cpanel,
            MessageManager.getString("label.url_post_data_service"),
            new JLabel(MessageManager.getString("label.post_url")), urlVp,
            "wrap");

    urlsuff = new JTextArea();
    urlsuff.setColumns(60);

    JvSwingUtils.mgAddtoLayout(cpanel,
            MessageManager.getString("label.optional_suffix"),
            new JLabel(MessageManager.getString("label.url_suffix")),
            urlsuff, "wrap");

    // input options
    // details.add(cpanel = new JPanel(), BorderLayout.CENTER);
    // cpanel.setLayout(new FlowLayout());
    hSeparable = new JCheckBox(MessageManager.getString("label.per_seq"));
    hSeparable.setToolTipText(JvSwingUtils.wrapTooltip(true,
            MessageManager.getString("label.job_created_when_checked")));
    hSeparable.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        hSeparable_actionPerformed(arg0);

      }
    });
    vSeparable = new JCheckBox(
            MessageManager.getString("label.result_vertically_separable"));
    vSeparable.setToolTipText(
            JvSwingUtils.wrapTooltip(true, MessageManager.getString(
                    "label.when_checked_job_visible_region_and_results")));
    vSeparable.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        vSeparable_actionPerformed(arg0);

      }
    });
    gapChar = new JComboBox();
    JvSwingUtils.mgAddtoLayout(cpanel,
            MessageManager.getString("label.preferred_gap_character"),
            new JLabel(
                    MessageManager.getString("label.gap_character") + ":"),
            gapChar, "wrap");

    cpanel.add(hSeparable);
    cpanel.add(vSeparable);

    // Input and Output lists
    // Inputparams
    JPanel iprmsList = new JPanel();
    iprmsList.setBorder(new TitledBorder(
            MessageManager.getString("label.data_input_parameters")));
    iprmsList.setLayout(new MigLayout("", "[grow 90, fill][]"));
    iprmVp = new JScrollPane();
    iprmVp.getViewport().setView(iprms = new JList());
    iprmsList.add(iprmVp);
    iprms.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    iprms.addMouseListener(new MouseListener()
    {

      @Override
      public void mouseReleased(MouseEvent e)
      {
        // TODO Auto-generated method stub

      }

      @Override
      public void mousePressed(MouseEvent e)
      {
        // TODO Auto-generated method stub

      }

      @Override
      public void mouseExited(MouseEvent e)
      {
        // TODO Auto-generated method stub

      }

      @Override
      public void mouseEntered(MouseEvent e)
      {
        // TODO Auto-generated method stub

      }

      @Override
      public void mouseClicked(MouseEvent e)
      {
        if (e.getClickCount() > 1)
        {
          iprmListSelection_doubleClicked();
        }

      }
    });
    JPanel iprmButs = new JPanel();
    iprmButs.setLayout(new MigLayout());

    iprmsAdd = JvSwingUtils.makeButton("+",
            MessageManager.getString("action.add_input_parameter"),
            new ActionListener()
            {

              @Override
              public void actionPerformed(ActionEvent e)
              {
                iprmsAdd_actionPerformed(e);

              }
            });
    iprmsRem = JvSwingUtils.makeButton("-",
            MessageManager.getString("action.remove_input_parameter"),
            new ActionListener()
            {

              @Override
              public void actionPerformed(ActionEvent e)
              {
                iprmsRem_actionPerformed(e);

              }
            });

    iprmButs.add(iprmsAdd, "wrap");
    iprmButs.add(iprmsRem, "wrap");
    iprmsList.add(iprmButs, "wrap");
    inputs.add(iprmsList, "wrap");

    // Return Parameters

    rdataAdd = JvSwingUtils.makeButton("+",
            MessageManager.getString("action.add_return_datatype"),
            new ActionListener()
            {

              @Override
              public void actionPerformed(ActionEvent e)
              {
                rdataAdd_actionPerformed(e);

              }
            });
    rdataRem = JvSwingUtils.makeButton("-",
            MessageManager.getString("action.remove_return_datatype"),
            new ActionListener()
            {

              @Override
              public void actionPerformed(ActionEvent e)
              {
                rdataRem_actionPerformed(e);

              }
            });
    rdataNup = JvSwingUtils.makeButton(
            MessageManager.getString("action.move_up"),
            MessageManager.getString("label.move_return_type_up_order"),
            new ActionListener()
            {

              @Override
              public void actionPerformed(ActionEvent e)
              {
                rdataNup_actionPerformed(e);

              }
            });
    rdataNdown = JvSwingUtils.makeButton(
            MessageManager.getString("action.move_down"),
            MessageManager.getString("label.move_return_type_down_order"),
            new ActionListener()
            {

              @Override
              public void actionPerformed(ActionEvent e)
              {
                rdataNdown_actionPerformed(e);

              }
            });

    JPanel rparamList = new JPanel();
    rparamList.setBorder(new TitledBorder(
            MessageManager.getString("label.data_returned_by_service")));
    rparamList.setLayout(new MigLayout("", "[grow 90, fill][]"));
    rdata = new JList();
    rdata.setToolTipText(MessageManager.getString(
            "label.right_click_to_edit_currently_selected_parameter"));
    rdata.addMouseListener(new MouseListener()
    {

      @Override
      public void mouseReleased(MouseEvent arg0)
      {
        // TODO Auto-generated method stub

      }

      @Override
      public void mousePressed(MouseEvent arg0)
      {

      }

      @Override
      public void mouseExited(MouseEvent arg0)
      {
        // TODO Auto-generated method stub

      }

      @Override
      public void mouseEntered(MouseEvent arg0)
      {
        // TODO Auto-generated method stub

      }

      @Override
      public void mouseClicked(MouseEvent arg0)
      {
        if (arg0.getButton() == MouseEvent.BUTTON3)
        {
          rdata_rightClicked(arg0);
        }

      }
    });
    rdataVp = new JScrollPane();
    rdataVp.getViewport().setView(rdata);
    rparamList.add(rdataVp);
    JPanel rparamButs = new JPanel();
    rparamButs.setLayout(new MigLayout());
    rparamButs.add(rdataAdd, "wrap");
    rparamButs.add(rdataRem, "wrap");
    rparamButs.add(rdataNup, "wrap");
    rparamButs.add(rdataNdown, "wrap");
    rparamList.add(rparamButs, "wrap");
    inputs.add(rparamList, "wrap");

    // Parse flat-text to a service

    urldesc = new JTextArea(4, 60);
    urldesc.setEditable(true);
    urldesc.setWrapStyleWord(true);
    urldescVp = new JScrollPane();
    urldescVp.setViewportView(urldesc);
    JPanel urldescPane = new JPanel();
    urldescPane.setLayout(
            new MigLayout("", "[grow 100, fill]", "[grow 100, fill]"));
    urldescPane.setBorder(new TitledBorder(
            MessageManager.getString("label.rsbs_encoded_service")));
    urldescPane.add(urldescVp, "span");
    paste.add(urldescPane, "span");
    urldescPane.setToolTipText(JvSwingUtils.wrapTooltip(true,
            MessageManager.getString("label.flat_file_representation")));

    parseRes = new JTextArea();
    parseResVp = new JScrollPane();
    parseResVp.setViewportView(parseRes);
    parseRes.setWrapStyleWord(true);
    parseRes.setColumns(60);
    parseWarnings = new JPanel(
            new MigLayout("", "[grow 100, fill]", "[grow 100, fill]"));
    parseWarnings.setBorder(new TitledBorder(
            MessageManager.getString("label.parsing_errors")));
    parseWarnings.setToolTipText(JvSwingUtils.wrapTooltip(true,
            MessageManager.getString("label.result_of_parsing_rsbs")));
    parseWarnings.add(parseResVp, "center");
    parseRes.setEditable(false);
    paste.add(parseWarnings, "span");
    setLayout(new BorderLayout());
    add(panels, BorderLayout.CENTER);
    okButton = JvSwingUtils.makeButton(
            MessageManager.getString("action.ok"), "", new ActionListener()
            {

              @Override
              public void actionPerformed(ActionEvent e)
              {
                ok_actionPerformed();
              }
            });
    cancelButton = JvSwingUtils.makeButton(
            MessageManager.getString("action.cancel"), "",
            new ActionListener()
            {

              @Override
              public void actionPerformed(ActionEvent e)
              {
                cancel_actionPerformed();
              }
            });

  }

  protected void rdata_rightClicked(MouseEvent arg0)
  {
    // TODO Auto-generated method stub

  }

  protected void iprmListSelection_doubleClicked()
  {
    // TODO Auto-generated method stub

  }

  protected void hSeparable_actionPerformed(ActionEvent arg0)
  {
    // TODO Auto-generated method stub

  }

  protected void vSeparable_actionPerformed(ActionEvent arg0)
  {
    // TODO Auto-generated method stub

  }

  protected void cancel_actionPerformed()
  {
    // TODO Auto-generated method stub

  }

  protected void ok_actionPerformed()
  {
    // TODO Auto-generated method stub

  }

  protected void iprmsAdd_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void iprmsRem_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void rdataAdd_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void rdataRem_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void rdataNup_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void rdataNdown_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void ok_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void cancel_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

}
