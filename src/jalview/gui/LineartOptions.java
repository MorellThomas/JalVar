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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import jalview.bin.Cache;
import jalview.util.MessageManager;

/**
 * A dialog where the user may choose Text or Lineart rendering, and optionally
 * save this as a preference ("Don't ask me again")
 */
public class LineartOptions extends JPanel
{
  public static final String PROMPT_EACH_TIME = "Prompt each time";

  JvOptionPane dialog;

  public boolean cancelled = false;

  String value;

  JRadioButton lineartRB;

  JCheckBox askAgainCB = new JCheckBox();

  AtomicBoolean asText;

  private String dialogTitle;

  /**
   * Constructor that passes in an initial choice of Text or Lineart, as a
   * mutable boolean object. User action in the dialog should update this
   * object, and the <em>same</em> object should be used in any action handler
   * set by calling <code>setResponseAction</code>.
   * <p>
   * If the user chooses an option and also "Don't ask me again", the chosen
   * option is saved as a property with key type_RENDERING i.e. "EPS_RENDERING",
   * "SVG_RENDERING" or "HTML_RENDERING".
   * 
   * @param formatType
   *          image type e.g. EPS, SVG
   * @param textOption
   *          true to select Text, false for Lineart
   */
  public LineartOptions(String formatType, AtomicBoolean textOption)
  {
    this.asText = textOption;
    dialogTitle = MessageManager.formatMessage(
            "label.select_character_style_title", formatType);
    String preferencesKey = formatType + "_RENDERING";
    try
    {
      jbInit(preferencesKey, formatType);
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }

    dialog = JvOptionPane.newOptionDialog(Desktop.desktop);
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
   * Shows the dialog, and performs any registered actions depending on the user
   * choices
   */
  public void showDialog()
  {
    Object[] options = new Object[] { MessageManager.getString("action.ok"),
        MessageManager.getString("action.cancel") };
    dialog.showInternalDialog(this, dialogTitle,
            JvOptionPane.OK_CANCEL_OPTION, JvOptionPane.PLAIN_MESSAGE, null,
            options, MessageManager.getString("action.ok"));
  }

  /**
   * Initialises the panel layout
   * 
   * @param preferencesKey
   * @param formatType
   * @throws Exception
   */
  private void jbInit(String preferencesKey, String formatType)
          throws Exception
  {
    /*
     * radio buttons for Text or Lineart - selection updates the value
     * of field 'asText' so it is correct when used in the confirm action
     */
    lineartRB = new JRadioButton(MessageManager.getString("label.lineart"));
    lineartRB.setFont(JvSwingUtils.getLabelFont());
    lineartRB.setSelected(!asText.get());
    lineartRB.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        asText.set(!lineartRB.isSelected());
      }
    });

    JRadioButton textRB = new JRadioButton(
            MessageManager.getString("action.text"));
    textRB.setFont(JvSwingUtils.getLabelFont());
    textRB.setSelected(asText.get());
    textRB.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        asText.set(textRB.isSelected());
      }
    });

    ButtonGroup bg = new ButtonGroup();
    bg.add(lineartRB);
    bg.add(textRB);

    askAgainCB.setFont(JvSwingUtils.getLabelFont());
    askAgainCB.setText(MessageManager.getString("label.dont_ask_me_again"));

    JLabel prompt = new JLabel(MessageManager.formatMessage(
            "label.select_character_rendering_style", formatType));
    prompt.setFont(JvSwingUtils.getLabelFont());

    this.setLayout(new FlowLayout(FlowLayout.LEFT));
    setBorder(BorderFactory.createEtchedBorder());
    add(prompt);
    add(textRB);
    add(lineartRB);
    add(askAgainCB);
  }

  /**
   * If "Don't ask me again" is selected, saves the selected option as the user
   * preference, otherwise removes the existing user preference (if any) is
   * removed
   * 
   * @param preferencesKey
   */
  protected void updatePreference(String preferencesKey)
  {
    value = lineartRB.isSelected() ? "Lineart" : "Text";

    if (askAgainCB.isSelected())
    {
      Cache.setProperty(preferencesKey, value);
    }
    else
    {
      Cache.applicationProperties.remove(preferencesKey);
    }
  }

  /**
   * Answers "Lineart" or "Text" as selected by the user.
   * 
   * @return
   */
  public String getValue()
  {
    // todo remove
    return value;
  }
}
