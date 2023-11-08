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
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceGroup;
import jalview.util.MessageManager;

/**
 * A panel that allows the user to select which sequence-associated annotation
 * rows to show or hide.
 * 
 * @author gmcarstairs
 *
 */
@SuppressWarnings("serial")
public class AnnotationChooser extends JPanel
{

  private static final Font CHECKBOX_FONT = new Font("Serif", Font.BOLD,
          12);

  private static final int MY_FRAME_WIDTH = 600;

  private static final int MY_FRAME_HEIGHT = 250;

  private JInternalFrame frame;

  private AlignmentPanel ap;

  private SequenceGroup sg;

  // all annotation rows' original visible state
  private boolean[] resetState = null;

  // is 'Show' selected?
  private boolean showSelected;

  // apply settings to selected (or all) sequences?
  private boolean applyToSelectedSequences;

  // apply settings to unselected (or all) sequences?
  private boolean applyToUnselectedSequences;

  // currently selected 'annotation type' checkboxes
  private Map<String, String> selectedTypes = new HashMap<>();

  /**
   * Constructor.
   * 
   * @param alignPane
   */
  public AnnotationChooser(AlignmentPanel alignPane)
  {
    super();
    this.ap = alignPane;
    this.sg = alignPane.av.getSelectionGroup();
    saveResetState(alignPane.getAlignment());

    try
    {
      jbInit();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
    showFrame();
  }

  /**
   * Save the initial show/hide state of all annotations to allow a Cancel
   * operation.
   * 
   * @param alignment
   */
  protected void saveResetState(AlignmentI alignment)
  {
    AlignmentAnnotation[] annotations = alignment.getAlignmentAnnotation();
    final int count = annotations.length;
    this.resetState = new boolean[count];
    for (int i = 0; i < count; i++)
    {
      this.resetState[i] = annotations[i].visible;
    }
  }

  /**
   * Populate this frame with:
   * <p>
   * checkboxes for the types of annotation to show or hide (i.e. any annotation
   * type shown for any sequence in the whole alignment)
   * <p>
   * option to show or hide selected types
   * <p>
   * option to show/hide for the currently selected group, or its inverse
   * <p>
   * OK and Cancel (reset) buttons
   */
  protected void jbInit()
  {
    setLayout(new GridLayout(3, 1));
    add(buildAnnotationTypesPanel());
    add(buildShowHideOptionsPanel());
    add(buildActionButtonsPanel());
    validate();
  }

  /**
   * Construct the panel with checkboxes for annotation types.
   * 
   * @return
   */
  protected JPanel buildAnnotationTypesPanel()
  {
    JPanel jp = new JPanel(new FlowLayout(FlowLayout.LEFT));

    List<String> annotationTypes = getAnnotationTypes(
            this.ap.getAlignment(), true);

    for (final String type : annotationTypes)
    {
      final Checkbox check = new Checkbox(type);
      check.setFont(CHECKBOX_FONT);
      check.addItemListener(new ItemListener()
      {
        @Override
        public void itemStateChanged(ItemEvent evt)
        {
          if (evt.getStateChange() == ItemEvent.SELECTED)
          {
            AnnotationChooser.this.selectedTypes.put(type, type);
          }
          else
          {
            AnnotationChooser.this.selectedTypes.remove(type);
          }
          changeTypeSelected_actionPerformed(type);
        }
      });
      jp.add(check);
    }
    return jp;
  }

  /**
   * Update display when scope (All/Selected sequences/Unselected) is changed.
   * <p>
   * Set annotations (with one of the selected types) to the selected Show/Hide
   * visibility, if they are in the new application scope. Set to the opposite
   * if outside the scope.
   * <p>
   * Note this only affects sequence-specific annotations, others are left
   * unchanged.
   */
  protected void changeApplyTo_actionPerformed()
  {
    setAnnotationVisibility(true);

    ap.updateAnnotation();
  }

  /**
   * Update display when an annotation type is selected or deselected.
   * <p>
   * If the type is selected, set visibility of annotations of that type which
   * are in the application scope (all, selected or unselected sequences).
   * <p>
   * If the type is unselected, set visibility to the opposite value. That is,
   * treat select/deselect as a 'toggle' operation.
   * 
   * @param type
   */
  protected void changeTypeSelected_actionPerformed(String type)
  {
    boolean typeSelected = this.selectedTypes.containsKey(type);
    for (AlignmentAnnotation aa : this.ap.getAlignment()
            .getAlignmentAnnotation())
    {
      if (aa.sequenceRef != null && type.equals(aa.label)
              && isInActionScope(aa))
      {
        aa.visible = typeSelected ? this.showSelected : !this.showSelected;
      }
    }
    ap.updateAnnotation();
  }

  /**
   * Update display on change of choice of Show or Hide
   * <p>
   * For annotations of any selected type, set visibility of annotations of that
   * type which are in the application scope (all, selected or unselected
   * sequences).
   * 
   * @param dataSourceType
   */
  protected void changeShowHide_actionPerformed()
  {
    setAnnotationVisibility(false);

    ap.updateAnnotation();
  }

  /**
   * Update visibility flags on annotation rows as per the current user choices.
   * 
   * @param updateAllRows
   */
  protected void setAnnotationVisibility(boolean updateAllRows)
  {
    for (AlignmentAnnotation aa : this.ap.getAlignment()
            .getAlignmentAnnotation())
    {
      if (aa.sequenceRef != null)
      {
        setAnnotationVisibility(aa, updateAllRows);
      }
    }
  }

  /**
   * Determine and set the visibility of the given annotation from the currently
   * selected options.
   * <p>
   * Only update annotations whose type is one of the selected types.
   * <p>
   * If its sequence is in the selected application scope
   * (all/selected/unselected sequences), then we set its visibility according
   * to the current choice of Show or Hide.
   * <p>
   * If force update of all rows is wanted, then set rows not in the sequence
   * selection scope to the opposite visibility to those in scope.
   * 
   * @param aa
   * @param updateAllRows
   */
  protected void setAnnotationVisibility(AlignmentAnnotation aa,
          boolean updateAllRows)
  {
    if (this.selectedTypes.containsKey(aa.label))
    {
      if (isInActionScope(aa))
      {
        aa.visible = this.showSelected;
      }
      else if (updateAllRows)
      {
        aa.visible = !this.showSelected;
      }
    }
    // TODO force not visible if associated sequence is hidden?
    // currently hiding a sequence does not hide its annotation rows
  }

  /**
   * Answers true if the annotation falls in the current selection criteria for
   * show/hide.
   * <p>
   * It must be in the sequence selection group (for 'Apply to selection'), or
   * not in it (for 'Apply except to selection'). No check needed for 'Apply to
   * all'.
   * 
   * @param aa
   * @return
   */
  protected boolean isInActionScope(AlignmentAnnotation aa)
  {
    boolean result = false;
    if (this.applyToSelectedSequences && this.applyToUnselectedSequences)
    {
      // we don't care if the annotation's sequence is selected or not
      result = true;
    }
    else if (this.sg == null)
    {
      // shouldn't happen - defensive programming
      result = true;
    }
    else if (this.sg.getSequences().contains(aa.sequenceRef))
    {
      // annotation is for a member of the selection group
      result = this.applyToSelectedSequences ? true : false;
    }
    else
    {
      // annotation is not associated with the selection group
      result = this.applyToUnselectedSequences ? true : false;
    }
    return result;
  }

  /**
   * Get annotation 'types' for an alignment, optionally restricted to
   * sequence-specific annotations only. The label is currently used for 'type'.
   * 
   * TODO refactor to helper class. See
   * AnnotationColourChooser.getAnnotationItems() for another client
   * 
   * @param alignment
   * @param sequenceSpecific
   * @return
   */
  public static List<String> getAnnotationTypes(AlignmentI alignment,
          boolean sequenceSpecificOnly)
  {
    List<String> result = new ArrayList<>();
    for (AlignmentAnnotation aa : alignment.getAlignmentAnnotation())
    {
      if (!sequenceSpecificOnly || aa.sequenceRef != null)
      {
        String label = aa.label;
        if (!result.contains(label))
        {
          result.add(label);
        }
      }
    }
    return result;
  }

  /**
   * Construct the panel with options to:
   * <p>
   * show or hide the selected annotation types
   * <p>
   * do this for the current selection group or its inverse
   * 
   * @return
   */
  protected JPanel buildShowHideOptionsPanel()
  {
    JPanel jp = new JPanel();
    jp.setLayout(new BorderLayout());

    JPanel showHideOptions = buildShowHidePanel();
    jp.add(showHideOptions, BorderLayout.CENTER);

    JPanel applyToOptions = buildApplyToOptionsPanel();
    jp.add(applyToOptions, BorderLayout.SOUTH);

    return jp;
  }

  /**
   * Build a panel with radio buttons options for sequences to apply show/hide
   * to. Options are all, current selection, all except current selection.
   * Initial state has 'current selection' selected.
   * <p>
   * If the sequence group is null, then we are acting on the whole alignment,
   * and only 'all sequences' is enabled (and selected).
   * 
   * @return
   */
  protected JPanel buildApplyToOptionsPanel()
  {
    final boolean wholeAlignment = this.sg == null;
    JPanel applyToOptions = new JPanel(new FlowLayout(FlowLayout.LEFT));
    CheckboxGroup actingOn = new CheckboxGroup();

    String forAll = MessageManager.getString("label.all_sequences");
    final Checkbox allSequences = new Checkbox(forAll, actingOn,
            wholeAlignment);
    allSequences.addItemListener(new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent evt)
      {
        if (evt.getStateChange() == ItemEvent.SELECTED)
        {
          AnnotationChooser.this.setApplyToSelectedSequences(true);
          AnnotationChooser.this.setApplyToUnselectedSequences(true);
          AnnotationChooser.this.changeApplyTo_actionPerformed();
        }
      }
    });
    applyToOptions.add(allSequences);

    String forSelected = MessageManager
            .getString("label.selected_sequences");
    final Checkbox selectedSequences = new Checkbox(forSelected, actingOn,
            !wholeAlignment);
    selectedSequences.setEnabled(!wholeAlignment);
    selectedSequences.addItemListener(new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent evt)
      {
        if (evt.getStateChange() == ItemEvent.SELECTED)
        {
          AnnotationChooser.this.setApplyToSelectedSequences(true);
          AnnotationChooser.this.setApplyToUnselectedSequences(false);
          AnnotationChooser.this.changeApplyTo_actionPerformed();
        }
      }
    });
    applyToOptions.add(selectedSequences);

    String exceptSelected = MessageManager
            .getString("label.except_selected_sequences");
    final Checkbox unselectedSequences = new Checkbox(exceptSelected,
            actingOn, false);
    unselectedSequences.setEnabled(!wholeAlignment);
    unselectedSequences.addItemListener(new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent evt)
      {
        if (evt.getStateChange() == ItemEvent.SELECTED)
        {
          AnnotationChooser.this.setApplyToSelectedSequences(false);
          AnnotationChooser.this.setApplyToUnselectedSequences(true);
          AnnotationChooser.this.changeApplyTo_actionPerformed();
        }
      }
    });
    applyToOptions.add(unselectedSequences);

    // set member variables to match the initial selection state
    this.applyToSelectedSequences = selectedSequences.getState()
            || allSequences.getState();
    this.applyToUnselectedSequences = unselectedSequences.getState()
            || allSequences.getState();

    return applyToOptions;
  }

  /**
   * Build a panel with radio button options to show or hide selected
   * annotations.
   * 
   * @return
   */
  protected JPanel buildShowHidePanel()
  {
    JPanel showHideOptions = new JPanel(new FlowLayout(FlowLayout.LEFT));
    CheckboxGroup showOrHide = new CheckboxGroup();

    /*
     * Radio button 'Show selected annotations' - initially unselected
     */
    String showLabel = MessageManager
            .getString("label.show_selected_annotations");
    final Checkbox showOption = new Checkbox(showLabel, showOrHide, false);
    showOption.addItemListener(new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent evt)
      {
        if (evt.getStateChange() == ItemEvent.SELECTED)
        {
          AnnotationChooser.this.setShowSelected(true);
          AnnotationChooser.this.changeShowHide_actionPerformed();
        }
      }
    });
    showHideOptions.add(showOption);

    /*
     * Radio button 'hide selected annotations'- initially selected
     */
    String hideLabel = MessageManager
            .getString("label.hide_selected_annotations");
    final Checkbox hideOption = new Checkbox(hideLabel, showOrHide, true);
    hideOption.addItemListener(new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent evt)
      {
        if (evt.getStateChange() == ItemEvent.SELECTED)
        {
          AnnotationChooser.this.setShowSelected(false);
          AnnotationChooser.this.changeShowHide_actionPerformed();
        }
      }
    });
    showHideOptions.add(hideOption);

    /*
     * Set member variable to match initial selection state
     */
    this.showSelected = showOption.getState();

    return showHideOptions;
  }

  /**
   * Construct the panel with OK and Cancel buttons.
   * 
   * @return
   */
  protected JPanel buildActionButtonsPanel()
  {
    JPanel jp = new JPanel();
    final Font labelFont = JvSwingUtils.getLabelFont();

    JButton ok = new JButton(MessageManager.getString("action.ok"));
    ok.setFont(labelFont);
    ok.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        close_actionPerformed();
      }
    });
    jp.add(ok);

    JButton cancel = new JButton(MessageManager.getString("action.cancel"));
    cancel.setFont(labelFont);
    cancel.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        cancel_actionPerformed();
      }
    });
    jp.add(cancel);

    return jp;
  }

  /**
   * On 'Cancel' button, undo any changes.
   */
  protected void cancel_actionPerformed()
  {
    resetOriginalState();
    this.ap.repaint();
    close_actionPerformed();
  }

  /**
   * Restore annotation visibility to their state on entry here, and repaint
   * alignment.
   */
  protected void resetOriginalState()
  {
    int i = 0;
    for (AlignmentAnnotation aa : this.ap.getAlignment()
            .getAlignmentAnnotation())
    {
      aa.visible = this.resetState[i++];
    }
  }

  /**
   * On 'Close' button, close the dialog.
   */
  protected void close_actionPerformed()
  {
    try
    {
      this.frame.setClosed(true);
    } catch (Exception exe)
    {
    }
  }

  /**
   * Render a frame containing this panel.
   */
  private void showFrame()
  {
    frame = new JInternalFrame();
    frame.setFrameIcon(null);
    frame.setContentPane(this);
    frame.setLayer(JLayeredPane.PALETTE_LAYER);
    Desktop.addInternalFrame(frame,
            MessageManager.getString("label.choose_annotations"),
            MY_FRAME_WIDTH, MY_FRAME_HEIGHT, true);
  }

  protected void setShowSelected(boolean showSelected)
  {
    this.showSelected = showSelected;
  }

  protected void setApplyToSelectedSequences(
          boolean applyToSelectedSequences)
  {
    this.applyToSelectedSequences = applyToSelectedSequences;
  }

  protected void setApplyToUnselectedSequences(
          boolean applyToUnselectedSequences)
  {
    this.applyToUnselectedSequences = applyToUnselectedSequences;
  }

  protected boolean isShowSelected()
  {
    return showSelected;
  }

  protected boolean isApplyToSelectedSequences()
  {
    return applyToSelectedSequences;
  }

  protected boolean isApplyToUnselectedSequences()
  {
    return applyToUnselectedSequences;
  }

}
