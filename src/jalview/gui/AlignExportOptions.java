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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import jalview.api.AlignExportSettingsI;
import jalview.api.AlignViewportI;
import jalview.io.FileFormatI;
import jalview.util.MessageManager;

/**
 * A dialog that allows the user to specify whether to include hidden columns or
 * sequences in an alignment export, and possibly features, annotations and
 * groups, if applicable to the output file format
 */
@SuppressWarnings("serial")
public class AlignExportOptions extends JPanel
{
  protected JCheckBox chkHiddenSeqs = new JCheckBox();

  protected JCheckBox chkHiddenCols = new JCheckBox();

  protected JCheckBox chkExportAnnots = new JCheckBox();

  protected JCheckBox chkExportFeats = new JCheckBox();

  protected JCheckBox chkExportGrps = new JCheckBox();

  protected AlignExportSettingsI settings;

  private boolean isComplexAlignFile;

  JvOptionPane dialog;

  /**
   * A convenience method that answers true if this dialog should be shown -
   * that is, if the alignment has any hidden rows or columns, or if the file
   * format is one that can (optionally) represent annotations, features or
   * groups data - else false
   * 
   * @param viewport
   * @param format
   * @return
   */
  public static boolean isNeeded(AlignViewportI viewport,
          FileFormatI format)
  {
    if (viewport.hasHiddenColumns() || viewport.hasHiddenRows()
            || format.isComplexAlignFile())
    {
      return true;
    }
    return false;
  }

  /**
   * Constructor that passes in an initial set of export options. User choices
   * in the dialog should update this object, and the <em>same</em> object
   * should be used in any action handler set by calling
   * <code>setResponseAction</code>.
   * 
   * @param viewport
   * @param format
   * @param defaults
   */
  public AlignExportOptions(AlignViewportI viewport, FileFormatI format,
          AlignExportSettingsI defaults)
  {
    this.settings = defaults;
    this.isComplexAlignFile = format.isComplexAlignFile();
    init(viewport.hasHiddenRows(), viewport.hasHiddenColumns());
    dialog = JvOptionPane.newOptionDialog(Desktop.desktop);
  }

  /**
   * Shows the dialog, and runs any registered response actions that correspond
   * to user choices
   */
  public void showDialog()
  {
    Object[] options = new Object[] { MessageManager.getString("action.ok"),
        MessageManager.getString("action.cancel") };
    dialog.showInternalDialog(this,
            MessageManager.getString("label.export_settings"),
            JvOptionPane.OK_CANCEL_OPTION, JvOptionPane.PLAIN_MESSAGE, null,
            options, MessageManager.getString("action.ok"));
  }

  /**
   * Registers a Runnable action to be performed for a particular user response
   * in the dialog
   * 
   * @param action
   */
  public void setResponseAction(Object response, Runnable action)
  {
    dialog.setResponseHandler(response, action);
  }

  /**
   * Selects/deselects all enabled and shown options on 'Check all' selected or
   * deselected
   * 
   * @param isSelected
   */
  void checkAllAction(boolean isSelected)
  {
    boolean set = chkHiddenSeqs.isEnabled() && isSelected;
    chkHiddenSeqs.setSelected(set);
    settings.setExportHiddenSequences(set);

    set = chkHiddenCols.isEnabled() && isSelected;
    chkHiddenCols.setSelected(set);
    settings.setExportHiddenColumns(set);

    set = isComplexAlignFile && chkExportAnnots.isEnabled() && isSelected;
    chkExportAnnots.setSelected(set);
    settings.setExportAnnotations(set);

    set = isComplexAlignFile && chkExportFeats.isEnabled() && isSelected;
    chkExportFeats.setSelected(set);
    settings.setExportFeatures(set);

    set = isComplexAlignFile && chkExportGrps.isEnabled() && isSelected;
    chkExportGrps.setSelected(set);
    settings.setExportGroups(set);
  }

  /**
   * Initialises the components of the display
   * 
   * @param hasHiddenSeq
   * @param hasHiddenCols
   */
  private void init(boolean hasHiddenSeq, boolean hasHiddenCols)
  {
    chkHiddenSeqs.setText(
            MessageManager.getString("action.export_hidden_sequences"));
    chkHiddenSeqs.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        settings.setExportHiddenSequences(chkHiddenSeqs.isSelected());
      }
    });

    chkHiddenCols.setText(
            MessageManager.getString("action.export_hidden_columns"));
    chkHiddenCols.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        settings.setExportHiddenColumns(chkHiddenCols.isSelected());
      }
    });

    chkExportAnnots
            .setText(MessageManager.getString("action.export_annotations"));
    chkExportAnnots.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        settings.setExportAnnotations(chkExportAnnots.isSelected());
      }
    });

    chkExportFeats
            .setText(MessageManager.getString("action.export_features"));
    chkExportFeats.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        settings.setExportFeatures(chkExportFeats.isSelected());
      }
    });

    chkExportGrps.setText(MessageManager.getString("action.export_groups"));
    chkExportGrps.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        settings.setExportGroups(chkExportGrps.isSelected());
      }
    });

    JCheckBox chkAll = new JCheckBox(
            MessageManager.getString("action.select_all"));

    JPanel hiddenRegionConfPanel = new JPanel(new BorderLayout());
    JPanel complexExportPanel = new JPanel(new BorderLayout());
    this.setLayout(new BorderLayout());

    chkAll.addItemListener(new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent e)
      {
        checkAllAction(chkAll.isSelected());
      }
    });

    hiddenRegionConfPanel.add(chkHiddenSeqs, BorderLayout.CENTER);
    hiddenRegionConfPanel.add(chkHiddenCols, BorderLayout.SOUTH);
    chkHiddenSeqs.setEnabled(hasHiddenSeq);
    chkHiddenCols.setEnabled(hasHiddenCols);

    complexExportPanel.add(chkExportAnnots, BorderLayout.NORTH);
    complexExportPanel.add(chkExportFeats, BorderLayout.CENTER);
    complexExportPanel.add(chkExportGrps, BorderLayout.SOUTH);

    JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    actionPanel.add(chkAll);

    JPanel optionsPanel = new JPanel();
    if (this.isComplexAlignFile)
    {
      optionsPanel.add(complexExportPanel);
    }

    if (hasHiddenSeq || hasHiddenCols)
    {
      optionsPanel.add(hiddenRegionConfPanel);
    }

    add(optionsPanel, BorderLayout.NORTH);
    add(actionPanel, BorderLayout.SOUTH);
  }
}
