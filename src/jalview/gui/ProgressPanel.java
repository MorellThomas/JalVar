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

import jalview.api.RendererListenerI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

/**
 * A class to manage a panel containing a label and progress bar updated by an
 * event firing
 * 
 * @author kmourao
 *
 */
public class ProgressPanel extends JPanel implements RendererListenerI
{
  // max value of progress bar: values expected to be %s
  private final int MAXVALUE = 100;

  private final String VISIBLE = "VISIBLE";

  private final String INVISIBLE = "INVISIBLE";

  // name of event property which updates the progress bar
  private String eventName;

  private JProgressBar progressBar;

  private JLabel progressLabel;

  private JPanel labelPanel = new JPanel();

  private CardLayout labelLayout = new CardLayout();

  private JPanel barPanel = new JPanel();

  private CardLayout barLayout = new CardLayout();

  /**
   * Construct a JPanel containing a progress bar and a label.
   * 
   * @param eventPropertyName
   *          The name of the event property to update the progress bar
   * @param label
   *          The label to place next to the progress bar
   */
  public ProgressPanel(String eventPropertyName, String label, int maxwidth)
  {
    super(new BorderLayout(10, 0));
    setBorder(new EmptyBorder(0, 3, 0, 0));

    eventName = eventPropertyName;
    String labelText = label;

    final int w = maxwidth;

    progressBar = new JProgressBar()
    {
      @Override
      public Dimension getMaximumSize()
      {
        return new Dimension(w, 1);
      }
    };
    progressBar.setMinimum(0);
    progressBar.setPreferredSize(progressBar.getMaximumSize());
    progressLabel = new JLabel(labelText);
    progressLabel.setFont(new java.awt.Font("Verdana", 0, 11));

    // Use a CardLayout to stop the progress bar panel moving around when
    // changing visibility
    labelPanel.setLayout(labelLayout);
    barPanel.setLayout(barLayout);

    labelPanel.add(progressLabel, VISIBLE);
    labelPanel.add(new JPanel(), INVISIBLE);
    barPanel.add(progressBar, VISIBLE);
    barPanel.add(new JPanel(), INVISIBLE);

    labelLayout.show(labelPanel, VISIBLE);
    barLayout.show(barPanel, VISIBLE);

    add(labelPanel, BorderLayout.WEST);
    add(barPanel, BorderLayout.CENTER);
    add(new JLabel(" "), BorderLayout.EAST);

    setBorder(BorderFactory.createLineBorder(Color.black));
    // setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
  }

  @Override
  /**
   * Update the progress bar in response to the event. Expects the value
   * supplied by the event to be in the range 0-100 i.e. a percentage
   */
  public void propertyChange(PropertyChangeEvent evt)
  {
    if (evt.getPropertyName().equals(eventName))
    {
      int progress = (int) evt.getNewValue();
      progressBar.setValue(progress);

      // switch progress bar to visible if it is not visible and current
      // progress is less than MAXVALUE
      // switch progress bar to invisible if it is visible and we reached
      // MAXVALUE
      if (progress < MAXVALUE && !progressBar.isVisible())
      {
        labelLayout.show(labelPanel, VISIBLE);
        barLayout.show(barPanel, VISIBLE);
      }
      if (progress >= MAXVALUE)
      {
        labelLayout.show(labelPanel, INVISIBLE);
        barLayout.show(barPanel, INVISIBLE);
      }
    }
  }
}
