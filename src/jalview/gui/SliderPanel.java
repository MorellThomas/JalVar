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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.util.List;

import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import jalview.analysis.Conservation;
import jalview.datamodel.SequenceGroup;
import jalview.jbgui.GSliderPanel;
import jalview.renderer.ResidueShaderI;
import jalview.util.MessageManager;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class SliderPanel extends GSliderPanel
{
  private static final String BACKGROUND = "Background";

  static JInternalFrame conservationSlider;

  static JInternalFrame PIDSlider;

  AlignmentPanel ap;

  boolean forConservation = true;

  ResidueShaderI cs;

  /**
   * Returns the currently displayed slider panel (or null if none).
   * 
   * @return
   */
  public static SliderPanel getSliderPanel()
  {
    if (conservationSlider != null && conservationSlider.isVisible())
    {
      return (SliderPanel) conservationSlider.getContentPane();
    }
    if (PIDSlider != null && PIDSlider.isVisible())
    {
      return (SliderPanel) PIDSlider.getContentPane();
    }
    return null;
  }

  /**
   * Creates a new SliderPanel object.
   * 
   * @param ap
   *          DOCUMENT ME!
   * @param value
   *          DOCUMENT ME!
   * @param forConserve
   *          DOCUMENT ME!
   * @param scheme
   *          DOCUMENT ME!
   */
  public SliderPanel(final AlignmentPanel ap, int value,
          boolean forConserve, ResidueShaderI scheme)
  {
    this.ap = ap;
    this.cs = scheme;
    forConservation = forConserve;
    undoButton.setVisible(false);
    applyButton.setVisible(false);

    if (forConservation)
    {
      label.setText(MessageManager.getString(
              "label.enter_value_increase_conservation_visibility"));
      slider.setMinimum(0);
      slider.setMaximum(100);
    }
    else
    {
      label.setText(MessageManager.getString(
              "label.enter_percentage_identity_above_which_colour_residues"));
      slider.setMinimum(0);
      slider.setMaximum(100);
    }

    slider.addChangeListener(new ChangeListener()
    {
      @Override
      public void stateChanged(ChangeEvent evt)
      {
        valueField.setText(String.valueOf(slider.getValue()));
        valueChanged(slider.getValue());
      }
    });

    slider.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseReleased(MouseEvent evt)
      {
        ap.paintAlignment(true, true);
      }
    });

    slider.setValue(value);
    valueField.setText(String.valueOf(value));
  }

  /**
   * Method to 'set focus' of the Conservation slider panel
   * 
   * @param ap
   *          the panel to repaint on change of slider
   * @param rs
   *          the colour scheme to update on change of slider
   * @param source
   *          a text description for the panel's title
   * 
   * @return
   */
  public static int setConservationSlider(AlignmentPanel ap,
          ResidueShaderI rs, String source)
  {
    SliderPanel sliderPanel = null;

    if (conservationSlider == null)
    {
      sliderPanel = new SliderPanel(ap, rs.getConservationInc(), true, rs);
      conservationSlider = new JInternalFrame();
      conservationSlider.setFrameIcon(null);
      conservationSlider.setContentPane(sliderPanel);
      conservationSlider.setLayer(JLayeredPane.PALETTE_LAYER);
    }
    else
    {
      sliderPanel = (SliderPanel) conservationSlider.getContentPane();
      sliderPanel.valueField
              .setText(String.valueOf(rs.getConservationInc()));
      sliderPanel.cs = rs;
      sliderPanel.ap = ap;
      sliderPanel.slider.setValue(rs.getConservationInc());
    }

    conservationSlider.setTitle(MessageManager.formatMessage(
            "label.conservation_colour_increment", new String[]
            { source == null ? BACKGROUND : source }));

    List<SequenceGroup> groups = ap.av.getAlignment().getGroups();
    if (groups != null && !groups.isEmpty())
    {
      sliderPanel.setAllGroupsCheckEnabled(true);
      sliderPanel.allGroupsCheck
              .setSelected(ap.av.getColourAppliesToAllGroups());
    }
    else
    {
      sliderPanel.setAllGroupsCheckEnabled(false);
    }

    return sliderPanel.getValue();
  }

  /**
   * Hides the PID slider panel if it is shown
   */
  public static void hidePIDSlider()
  {
    if (PIDSlider != null)
    {
      try
      {
        PIDSlider.setClosed(true);
        PIDSlider = null;
      } catch (PropertyVetoException ex)
      {
      }
    }
  }

  /**
   * Hides the conservation slider panel if it is shown
   */
  public static void hideConservationSlider()
  {
    if (conservationSlider != null)
    {
      try
      {
        conservationSlider.setClosed(true);
        conservationSlider = null;
      } catch (PropertyVetoException ex)
      {
      }
    }
  }

  /**
   * DOCUMENT ME!
   */
  public static void showConservationSlider()
  {
    hidePIDSlider();

    if (!conservationSlider.isVisible())
    {
      Desktop.addInternalFrame(conservationSlider,
              conservationSlider.getTitle(), true, FRAME_WIDTH,
              FRAME_HEIGHT, false, true);
      conservationSlider.addInternalFrameListener(new InternalFrameAdapter()
      {
        @Override
        public void internalFrameClosed(InternalFrameEvent e)
        {
          conservationSlider = null;
        }
      });
      conservationSlider.setLayer(JLayeredPane.PALETTE_LAYER);
    }
  }

  /**
   * Method to 'set focus' of the PID slider panel
   * 
   * @param ap
   *          the panel to repaint on change of slider
   * @param rs
   *          the colour scheme to update on change of slider
   * @param source
   *          a text description for the panel's title
   * 
   * @return
   */
  public static int setPIDSliderSource(AlignmentPanel ap, ResidueShaderI rs,
          String source)
  {
    int threshold = rs.getThreshold();

    SliderPanel sliderPanel = null;

    if (PIDSlider == null)
    {
      sliderPanel = new SliderPanel(ap, threshold, false, rs);
      PIDSlider = new JInternalFrame();
      PIDSlider.setFrameIcon(null);
      PIDSlider.setContentPane(sliderPanel);
      PIDSlider.setLayer(JLayeredPane.PALETTE_LAYER);
    }
    else
    {
      sliderPanel = (SliderPanel) PIDSlider.getContentPane();
      sliderPanel.cs = rs;
      sliderPanel.ap = ap;
      sliderPanel.valueField.setText(String.valueOf(rs.getThreshold()));
      sliderPanel.slider.setValue(rs.getThreshold());
    }

    PIDSlider.setTitle(MessageManager.formatMessage(
            "label.percentage_identity_threshold", new String[]
            { source == null ? BACKGROUND : source }));

    if (ap.av.getAlignment().getGroups() != null)
    {
      sliderPanel.setAllGroupsCheckEnabled(true);
    }
    else
    {
      sliderPanel.setAllGroupsCheckEnabled(false);
    }

    return sliderPanel.getValue();
  }

  /**
   * DOCUMENT ME!
   * 
   * @return
   */
  public static JInternalFrame showPIDSlider()
  {
    hideConservationSlider();

    if (!PIDSlider.isVisible())
    {
      Desktop.addInternalFrame(PIDSlider, PIDSlider.getTitle(), true,
              FRAME_WIDTH, FRAME_HEIGHT, false, true);
      PIDSlider.setLayer(JLayeredPane.PALETTE_LAYER);
      PIDSlider.addInternalFrameListener(new InternalFrameAdapter()
      {
        @Override
        public void internalFrameClosed(InternalFrameEvent e)
        {
          PIDSlider = null;
        }
      });
      PIDSlider.setLayer(JLayeredPane.PALETTE_LAYER);
    }
    return PIDSlider;
  }

  /**
   * Updates the colour scheme with the current (identity threshold or
   * conservation) percentage value. Also updates all groups if 'apply to all
   * groups' is selected.
   * 
   * @param percent
   */
  public void valueChanged(int percent)
  {
    if (!forConservation)
    {
      ap.av.setThreshold(percent);
    }
    updateColourScheme(percent, cs, null);

    if (allGroupsCheck.isSelected())
    {
      List<SequenceGroup> groups = ap.av.getAlignment().getGroups();
      for (SequenceGroup sg : groups)
      {
        updateColourScheme(percent, sg.getGroupColourScheme(), sg);
      }
    }

    ap.getSeqPanel().seqCanvas.repaint();
  }

  /**
   * Updates the colour scheme (if not null) with the current (identity
   * threshold or conservation) percentage value
   * 
   * @param percent
   * @param scheme
   * @param sg
   */
  protected void updateColourScheme(int percent, ResidueShaderI scheme,
          SequenceGroup sg)
  {
    if (scheme == null)
    {
      return;
    }
    if (forConservation)
    {
      if (!scheme.conservationApplied() && sg != null)
      {
        /*
         * first time the colour scheme has had Conservation shading applied
         * - compute conservation
         */
        Conservation c = new Conservation("Group", sg.getSequences(null),
                sg.getStartRes(), sg.getEndRes());
        c.calculate();
        c.verdict(false, ap.av.getConsPercGaps());
        sg.cs.setConservation(c);

      }
      scheme.setConservationApplied(true);
      scheme.setConservationInc(percent);
    }
    else
    {
      scheme.setThreshold(percent, ap.av.isIgnoreGapsConsensus());
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param b
   *          DOCUMENT ME!
   */
  public void setAllGroupsCheckEnabled(boolean b)
  {
    allGroupsCheck.setEnabled(b);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void valueField_actionPerformed()
  {
    try
    {
      int i = Integer.parseInt(valueField.getText());
      slider.setValue(i);
    } catch (NumberFormatException ex)
    {
      valueField.setText(slider.getValue() + "");
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param value
   *          DOCUMENT ME!
   */
  public void setValue(int value)
  {
    slider.setValue(value);
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getValue()
  {
    return Integer.parseInt(valueField.getText());
  }

  @Override
  public void slider_mouseReleased(MouseEvent e)
  {
    if (ap.overviewPanel != null)
    {
      ap.overviewPanel.updateOverviewImage();
    }
  }

  public static int getConservationValue()
  {
    return getValue(conservationSlider);
  }

  static int getValue(JInternalFrame slider)
  {
    return slider == null ? 0
            : ((SliderPanel) slider.getContentPane()).getValue();
  }

  public static int getPIDValue()
  {
    return getValue(PIDSlider);
  }

  /**
   * Answers true if the SliderPanel is for Conservation, false if it is for PID
   * threshold
   * 
   * @return
   */
  public boolean isForConservation()
  {
    return forConservation;
  }

  /**
   * Answers the title for the slider panel; this may include 'Background' if
   * for the alignment, or the group id if for a group
   * 
   * @return
   */
  public String getTitle()
  {
    String title = null;
    if (isForConservation())
    {
      if (conservationSlider != null)
      {
        title = conservationSlider.getTitle();
      }
    }
    else if (PIDSlider != null)
    {
      title = PIDSlider.getTitle();
    }
    return title;
  }
}
