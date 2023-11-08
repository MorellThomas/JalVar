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

import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

public class JalviewBooleanRadioButtons extends AbstractButton
{
  private static final Font LABEL_FONT = JvSwingUtils.getLabelFont();

  private ButtonGroup buttonGroup = new ButtonGroup();

  private JRadioButton buttonTrue = new JRadioButton();

  private JRadioButton buttonFalse = new JRadioButton();

  public JalviewBooleanRadioButtons(boolean value, String trueLabel,
          String falseLabel)
  {
    init();
    this.setLabels(trueLabel, falseLabel);
  }

  public JalviewBooleanRadioButtons(boolean value)
  {
    init();
    setSelected(value);
  }

  public JalviewBooleanRadioButtons()
  {
    init();
  }

  protected void init()
  {
    buttonTrue.setFont(LABEL_FONT);
    buttonFalse.setFont(LABEL_FONT);
    buttonGroup.add(buttonTrue);
    buttonGroup.add(buttonFalse);
  }

  public void setLabels(String trueLabel, String falseLabel)
  {
    buttonTrue.setText(trueLabel);
    buttonFalse.setText(falseLabel);
  }

  @Override
  public void setSelected(boolean b)
  {
    buttonFalse.setSelected(!b);
    // this should probably happen automatically, no harm in forcing the issue!
    // setting them this way round so the last setSelected is on buttonTrue
    buttonTrue.setSelected(b);
  }

  @Override
  public boolean isSelected()
  {
    // unambiguous selection
    return buttonTrue.isSelected() && !buttonFalse.isSelected();
  }

  @Override
  public void setEnabled(boolean b)
  {
    buttonTrue.setEnabled(b);
    buttonFalse.setEnabled(b);
  }

  @Override
  public boolean isEnabled()
  {
    return buttonTrue.isEnabled() && buttonFalse.isEnabled();
  }

  public JRadioButton getTrueButton()
  {
    return buttonTrue;
  }

  public JRadioButton getFalseButton()
  {
    return buttonFalse;
  }

  @Override
  public void addActionListener(ActionListener l)
  {
    buttonTrue.addActionListener(l);
    buttonFalse.addActionListener(l);
  }

  public void addTrueActionListener(ActionListener l)
  {
    buttonTrue.addActionListener(l);
  }

  public void addFalseActionListener(ActionListener l)
  {
    buttonFalse.addActionListener(l);
  }
}
