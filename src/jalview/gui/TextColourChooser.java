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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jalview.datamodel.SequenceGroup;
import jalview.gui.JalviewColourChooser.ColourChooserListener;
import jalview.util.MessageManager;

public class TextColourChooser
{
  AlignmentPanel ap;

  SequenceGroup sg;

  Color original1, original2;

  int originalThreshold;

  Map<SequenceGroup, Color> groupColour1;

  Map<SequenceGroup, Color> groupColour2;

  Map<SequenceGroup, Integer> groupThreshold;

  /**
   * Show a dialogue which allows the user to select two text colours and adjust
   * a slider for the cross-over point
   * 
   * @param alignPanel
   *          the AlignmentPanel context
   * @param sequenceGroup
   *          the SequenceGroup context (only for group pop-menu option)
   */
  public void chooseColour(AlignmentPanel alignPanel,
          SequenceGroup sequenceGroup)
  {
    this.ap = alignPanel;
    this.sg = sequenceGroup;

    saveInitialSettings();

    final JSlider slider = new JSlider(0, 750, originalThreshold);
    final JPanel col1 = new JPanel();
    col1.setPreferredSize(new Dimension(40, 20));
    col1.setBorder(BorderFactory.createEtchedBorder());
    col1.setToolTipText(MessageManager.getString("label.dark_colour"));
    col1.setBackground(original1);
    final JPanel col2 = new JPanel();
    col2.setPreferredSize(new Dimension(40, 20));
    col2.setBorder(BorderFactory.createEtchedBorder());
    col2.setToolTipText(MessageManager.getString("label.light_colour"));
    col2.setBackground(original2);
    final JPanel bigpanel = new JPanel(new BorderLayout());
    JPanel panel = new JPanel(new BorderLayout());
    bigpanel.add(panel, BorderLayout.CENTER);
    bigpanel.add(
            new JLabel("<html>"
                    + MessageManager.getString(
                            "label.select_dark_light_set_threshold")
                    + "</html>"),
            BorderLayout.NORTH);
    panel.add(col1, BorderLayout.WEST);
    panel.add(slider, BorderLayout.CENTER);
    panel.add(col2, BorderLayout.EAST);

    col1.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        String ttl = MessageManager
                .getString("label.select_colour_for_text");
        ColourChooserListener listener = new ColourChooserListener()
        {
          @Override
          public void colourSelected(Color c)
          {
            colour1Changed(c);
            col1.setBackground(c);
          }
        };
        JalviewColourChooser.showColourChooser(bigpanel, ttl,
                col1.getBackground(), listener);
      }
    });

    col2.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        String ttl = MessageManager
                .getString("label.select_colour_for_text");
        ColourChooserListener listener = new ColourChooserListener()
        {
          @Override
          public void colourSelected(Color c)
          {
            colour2Changed(c);
            col2.setBackground(c);
          }
        };
        JalviewColourChooser.showColourChooser(bigpanel, ttl,
                col2.getBackground(), listener);
      }
    });

    slider.addChangeListener(new ChangeListener()
    {
      @Override
      public void stateChanged(ChangeEvent evt)
      {
        thresholdChanged(slider.getValue());
      }
    });

    Object[] options = new Object[] { MessageManager.getString("action.ok"),
        MessageManager.getString("action.cancel") };
    String title = MessageManager
            .getString("label.adjust_foreground_text_colour_threshold");
    Runnable action = () -> // response for 1 = Cancel
    {
      restoreInitialSettings();
    };
    JvOptionPane.newOptionDialog(alignPanel.alignFrame)
            .setResponseHandler(1, action).showInternalDialog(bigpanel,
                    title, JvOptionPane.YES_NO_CANCEL_OPTION,
                    JvOptionPane.PLAIN_MESSAGE, null, options,
                    MessageManager.getString("action.ok"));
  }

  /**
   * Restore initial settings on Cancel
   */
  protected void restoreInitialSettings()
  {
    if (sg == null)
    {
      ap.av.setTextColour(original1);
      ap.av.setTextColour2(original2);
      ap.av.setThresholdTextColour(originalThreshold);
    }
    else
    {
      sg.textColour = original1;
      sg.textColour2 = original2;
      sg.thresholdTextColour = originalThreshold;
    }

    /*
     * if 'Apply To All Groups' was in force, there will be 
     * group-specific settings to restore as well
     */
    for (SequenceGroup group : this.groupColour1.keySet())
    {
      group.textColour = groupColour1.get(group);
      group.textColour2 = groupColour2.get(group);
      group.thresholdTextColour = groupThreshold.get(group);
    }

    ap.paintAlignment(false, false);
  }

  /**
   * Save settings on entry, for restore on Cancel
   */
  protected void saveInitialSettings()
  {
    groupColour1 = new HashMap<>();
    groupColour2 = new HashMap<>();
    groupThreshold = new HashMap<>();

    if (sg == null)
    {
      /*
       * alignment scope
       */
      original1 = ap.av.getTextColour();
      original2 = ap.av.getTextColour2();
      originalThreshold = ap.av.getThresholdTextColour();
      if (ap.av.getColourAppliesToAllGroups()
              && ap.av.getAlignment().getGroups() != null)
      {
        /*
         * if applying changes to all groups, need to be able to 
         * restore group settings as well
         */
        for (SequenceGroup group : ap.av.getAlignment().getGroups())
        {
          groupColour1.put(group, group.textColour);
          groupColour2.put(group, group.textColour2);
          groupThreshold.put(group, group.thresholdTextColour);
        }
      }
    }
    else
    {
      /*
       * Sequence group scope
       */
      original1 = sg.textColour;
      original2 = sg.textColour2;
      originalThreshold = sg.thresholdTextColour;
    }
  }

  void colour1Changed(Color col)
  {
    if (sg == null)
    {
      ap.av.setTextColour(col);
      if (ap.av.getColourAppliesToAllGroups())
      {
        setGroupTextColour();
      }
    }
    else
    {
      sg.textColour = col;
    }

    ap.paintAlignment(false, false);
  }

  void colour2Changed(Color col)
  {
    if (sg == null)
    {
      ap.av.setTextColour2(col);
      if (ap.av.getColourAppliesToAllGroups())
      {
        setGroupTextColour();
      }
    }
    else
    {
      sg.textColour2 = col;
    }

    ap.paintAlignment(false, false);
  }

  void thresholdChanged(int value)
  {
    if (sg == null)
    {
      ap.av.setThresholdTextColour(value);
      if (ap.av.getColourAppliesToAllGroups())
      {
        setGroupTextColour();
      }
    }
    else
    {
      sg.thresholdTextColour = value;
    }

    ap.paintAlignment(false, false);
  }

  void setGroupTextColour()
  {
    if (ap.av.getAlignment().getGroups() == null)
    {
      return;
    }

    for (SequenceGroup group : ap.av.getAlignment().getGroups())
    {
      group.textColour = ap.av.getTextColour();
      group.textColour2 = ap.av.getTextColour2();
      group.thresholdTextColour = ap.av.getThresholdTextColour();
    }
  }

}
