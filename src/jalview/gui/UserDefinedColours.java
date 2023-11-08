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

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import jalview.bin.Cache;
import jalview.io.JalviewFileChooser;
import jalview.io.JalviewFileView;
import jalview.jbgui.GUserDefinedColours;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ColourSchemeLoader;
import jalview.schemes.ColourSchemes;
import jalview.schemes.ResidueProperties;
import jalview.schemes.UserColourScheme;
import jalview.util.ColorUtils;
import jalview.util.Format;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.xml.binding.jalview.JalviewUserColours;
import jalview.xml.binding.jalview.JalviewUserColours.Colour;
import jalview.xml.binding.jalview.ObjectFactory;

/**
 * This panel allows the user to assign colours to Amino Acid residue codes, and
 * save the colour scheme.
 * 
 * @author Andrew Waterhouse
 * @author Mungo Carstairs
 */
public class UserDefinedColours extends GUserDefinedColours
        implements ChangeListener
{
  private static final Font VERDANA_BOLD_10 = new Font("Verdana", Font.BOLD,
          10);

  public static final String USER_DEFINED_COLOURS = "USER_DEFINED_COLOURS";

  private static final String LAST_DIRECTORY = "LAST_DIRECTORY";

  private static final int MY_FRAME_HEIGHT = 440;

  private static final int MY_FRAME_WIDTH = 810;

  private static final int MY_FRAME_WIDTH_CASE_SENSITIVE = 970;

  AlignmentPanel ap;

  /*
   * the colour scheme when the dialog was opened, or
   * the scheme last saved to file
   */
  ColourSchemeI oldColourScheme;

  /*
   * flag is true if the colour scheme has been changed since the
   * dialog was opened, or the changes last saved to file
   */
  boolean changedButNotSaved;

  JInternalFrame frame;

  List<JButton> upperCaseButtons;

  List<JButton> lowerCaseButtons;

  /**
   * Creates and displays a new UserDefinedColours panel
   * 
   * @param alignPanel
   */
  public UserDefinedColours(AlignmentPanel alignPanel)
  {
    this();

    lcaseColour.setEnabled(false);

    this.ap = alignPanel;

    oldColourScheme = alignPanel.av.getGlobalColourScheme();

    if (oldColourScheme instanceof UserColourScheme)
    {
      schemeName.setText(oldColourScheme.getSchemeName());
      if (((UserColourScheme) oldColourScheme)
              .getLowerCaseColours() != null)
      {
        caseSensitive.setSelected(true);
        lcaseColour.setEnabled(true);
        resetButtonPanel(true);
      }
      else
      {
        resetButtonPanel(false);
      }
    }
    else
    {
      resetButtonPanel(false);
    }

    showFrame();
  }

  UserDefinedColours()
  {
    super();
    selectedButtons = new ArrayList<>();
  }

  void showFrame()
  {
    colorChooser.getSelectionModel().addChangeListener(this);
    frame = new JInternalFrame();
    frame.setFrameIcon(null);
    frame.setContentPane(this);
    Desktop.addInternalFrame(frame,
            MessageManager.getString("label.user_defined_colours"),
            MY_FRAME_WIDTH, MY_FRAME_HEIGHT, true);
  }

  /**
   * Rebuilds the panel with coloured buttons for residues. If not case
   * sensitive colours, show 3-letter amino acid code as button text. If case
   * sensitive, just show the single letter code, in order to make space for the
   * additional buttons.
   * 
   * @param isCaseSensitive
   */
  void resetButtonPanel(boolean isCaseSensitive)
  {
    buttonPanel.removeAll();

    if (upperCaseButtons == null)
    {
      upperCaseButtons = new ArrayList<>();
    }

    for (int i = 0; i < 20; i++)
    {
      String label = isCaseSensitive ? ResidueProperties.aa[i]
              : ResidueProperties.aa2Triplet.get(ResidueProperties.aa[i])
                      .toString();
      JButton button = makeButton(label, ResidueProperties.aa[i],
              upperCaseButtons, i);
      buttonPanel.add(button);
    }

    buttonPanel.add(makeButton("B", "B", upperCaseButtons, 20));
    buttonPanel.add(makeButton("Z", "Z", upperCaseButtons, 21));
    buttonPanel.add(makeButton("X", "X", upperCaseButtons, 22));
    buttonPanel.add(makeButton("Gap", "-", upperCaseButtons, 23));

    if (!isCaseSensitive)
    {
      gridLayout.setRows(6);
      gridLayout.setColumns(4);
    }
    else
    {
      gridLayout.setRows(7);
      int cols = 7;
      gridLayout.setColumns(cols + 1);

      if (lowerCaseButtons == null)
      {
        lowerCaseButtons = new ArrayList<>();
      }

      for (int i = 0; i < 20; i++)
      {
        int row = i / cols + 1;
        int index = (row * cols) + i;
        JButton button = makeButton(
                ResidueProperties.aa[i].toLowerCase(Locale.ROOT),
                ResidueProperties.aa[i].toLowerCase(Locale.ROOT),
                lowerCaseButtons, i);

        buttonPanel.add(button, index);
      }
    }

    if (isCaseSensitive)
    {
      buttonPanel.add(makeButton("b", "b", lowerCaseButtons, 20));
      buttonPanel.add(makeButton("z", "z", lowerCaseButtons, 21));
      buttonPanel.add(makeButton("x", "x", lowerCaseButtons, 22));
    }

    // JAL-1360 widen the frame dynamically to accommodate case-sensitive AA
    // codes
    if (this.frame != null)
    {
      int newWidth = isCaseSensitive ? MY_FRAME_WIDTH_CASE_SENSITIVE
              : MY_FRAME_WIDTH;
      this.frame.setSize(newWidth, this.frame.getHeight());
    }

    buttonPanel.validate();
    validate();
  }

  /**
   * ChangeListener handler for when a colour is picked in the colour chooser.
   * The action is to apply the colour to all selected buttons as their
   * background colour. Foreground colour (text) is set to a lighter shade in
   * order to highlight which buttons are selected. If 'Lower Case Colour' is
   * active, then the colour is applied to all lower case buttons (as well as
   * the Lower Case Colour button itself).
   * 
   * @param evt
   */
  @Override
  public void stateChanged(ChangeEvent evt)
  {
    JButton button = null;
    final Color newColour = colorChooser.getColor();
    if (lcaseColour.isSelected())
    {
      selectedButtons.clear();
      for (int i = 0; i < lowerCaseButtons.size(); i++)
      {
        button = lowerCaseButtons.get(i);
        button.setBackground(newColour);
        button.setForeground(
                ColorUtils.brighterThan(button.getBackground()));
      }
    }
    for (int i = 0; i < selectedButtons.size(); i++)
    {
      button = selectedButtons.get(i);
      button.setBackground(newColour);
      button.setForeground(ColorUtils.brighterThan(newColour));
    }

    changedButNotSaved = true;
  }

  /**
   * Performs actions when a residue button is clicked. This manages the button
   * selection set (highlighted by brighter foreground text).
   * <p>
   * On select button(s) with Ctrl/click or Shift/click: set button foreground
   * text to brighter than background.
   * <p>
   * On unselect button(s) with Ctrl/click on selected, or click to release
   * current selection: reset foreground text to darker than background.
   * <p>
   * Simple click: clear selection (resetting foreground to darker); set clicked
   * button foreground to brighter
   * <p>
   * Finally, synchronize the colour chooser to the colour of the first button
   * in the selected set.
   * 
   * @param e
   */
  public void colourButtonPressed(MouseEvent e)
  {
    JButton pressed = (JButton) e.getSource();

    if (e.isShiftDown())
    {
      JButton start, end = (JButton) e.getSource();
      if (selectedButtons.size() > 0)
      {
        start = selectedButtons.get(selectedButtons.size() - 1);
      }
      else
      {
        start = (JButton) e.getSource();
      }

      int startIndex = 0, endIndex = 0;
      for (int b = 0; b < buttonPanel.getComponentCount(); b++)
      {
        if (buttonPanel.getComponent(b) == start)
        {
          startIndex = b;
        }
        if (buttonPanel.getComponent(b) == end)
        {
          endIndex = b;
        }
      }

      if (startIndex > endIndex)
      {
        int temp = startIndex;
        startIndex = endIndex;
        endIndex = temp;
      }

      for (int b = startIndex; b <= endIndex; b++)
      {
        JButton button = (JButton) buttonPanel.getComponent(b);
        if (!selectedButtons.contains(button))
        {
          button.setForeground(
                  ColorUtils.brighterThan(button.getBackground()));
          selectedButtons.add(button);
        }
      }
    }
    else if (!e.isControlDown())
    {
      for (int b = 0; b < selectedButtons.size(); b++)
      {
        JButton button = selectedButtons.get(b);
        button.setForeground(ColorUtils.darkerThan(button.getBackground()));
      }
      selectedButtons.clear();
      pressed.setForeground(
              ColorUtils.brighterThan(pressed.getBackground()));
      selectedButtons.add(pressed);

    }
    else if (e.isControlDown())
    {
      if (selectedButtons.contains(pressed))
      {
        pressed.setForeground(
                ColorUtils.darkerThan(pressed.getBackground()));
        selectedButtons.remove(pressed);
      }
      else
      {
        pressed.setForeground(
                ColorUtils.brighterThan(pressed.getBackground()));
        selectedButtons.add(pressed);
      }
    }

    if (selectedButtons.size() > 0)
    {
      colorChooser.setColor((selectedButtons.get(0)).getBackground());
    }
  }

  /**
   * A helper method to update or make a colour button, whose background colour
   * is the associated colour, and text colour a darker shade of the same. If
   * the button is already in the list, then its text and margins are updated,
   * if not then it is created and added. This method supports toggling between
   * case-sensitive and case-insensitive button panels. The case-sensitive
   * version has abbreviated button text in order to fit in more buttons.
   * 
   * @param label
   * @param residue
   * @param the
   *          list of buttons
   * @param buttonIndex
   *          the button's position in the list
   */
  JButton makeButton(String label, String residue, List<JButton> buttons,
          int buttonIndex)
  {
    final JButton button;
    Color col;

    if (buttonIndex < buttons.size())
    {
      button = buttons.get(buttonIndex);
      col = button.getBackground();
    }
    else
    {
      button = new JButton();
      button.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mouseClicked(MouseEvent e)
        {
          colourButtonPressed(e);
        }
      });

      buttons.add(button);

      /*
       * make initial button colour that of the current colour scheme,
       * if it is a simple per-residue colouring, else white
       */
      col = Color.white;
      if (oldColourScheme != null && oldColourScheme.isSimple())
      {
        col = oldColourScheme.findColour(residue.charAt(0), 0, null, null,
                0f);
      }
    }

    if (caseSensitive.isSelected())
    {
      button.setMargin(new Insets(2, 2, 2, 2));
    }
    else
    {
      button.setMargin(new Insets(2, 14, 2, 14));
    }

    button.setOpaque(true); // required for the next line to have effect
    button.setBackground(col);
    button.setText(label);
    button.setForeground(ColorUtils.darkerThan(col));
    button.setFont(VERDANA_BOLD_10);

    return button;
  }

  /**
   * On 'OK', check that at least one colour has been assigned to a residue (and
   * if not issue a warning), and apply the chosen colour scheme and close the
   * panel.
   */
  @Override
  protected void okButton_actionPerformed()
  {
    if (isNoSelectionMade())
    {
      JvOptionPane.showMessageDialog(Desktop.desktop,
              MessageManager
                      .getString("label.no_colour_selection_in_scheme"),
              MessageManager.getString("label.no_colour_selection_warn"),
              JvOptionPane.WARNING_MESSAGE);
    }
    else
    {
      /*
       * OK is treated as 'apply colours and close'
       */
      applyButton_actionPerformed();

      /*
       * If editing a named colour scheme, warn if changes
       * have not been saved
       */
      warnIfUnsavedChanges();

      try
      {
        frame.setClosed(true);
      } catch (Exception ex)
      {
      }
    }
  }

  /**
   * If we have made changes to an existing user defined colour scheme but not
   * saved them, show a dialog with the option to save. If the user chooses to
   * save, do so, else clear the colour scheme name to indicate a new colour
   * scheme.
   */
  protected void warnIfUnsavedChanges()
  {
    // BH 2018 no warning in JavaScript TODO

    if (!Platform.isJS() && changedButNotSaved)
    /**
     * Java only
     * 
     * @j2sIgnore
     */
    {
      String name = schemeName.getText().trim();
      if (oldColourScheme != null && !"".equals(name)
              && name.equals(oldColourScheme.getSchemeName()))
      {
        String message = MessageManager
                .formatMessage("label.scheme_changed", name);
        String title = MessageManager.getString("label.save_changes");
        String[] options = new String[] { title,
            MessageManager.getString("label.dont_save_changes"), };
        final String question = JvSwingUtils.wrapTooltip(true, message);
        int response = JvOptionPane.showOptionDialog(Desktop.desktop,
                question, title, JvOptionPane.DEFAULT_OPTION,
                JvOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (response == 0)
        {
          /*
           * prompt to save changes to file; if done,
           * resets 'changed' flag to false
           */
          savebutton_actionPerformed();
        }

        /*
         * if user chooses not to save (either in this dialog or in the
         * save as dialogs), treat this as a new user defined colour scheme
         */
        if (changedButNotSaved)
        {
          /*
           * clear scheme name and re-apply as an anonymous scheme
           */
          schemeName.setText("");
          applyButton_actionPerformed();
        }
      }
    }
  }

  /**
   * Returns true if the user has not made any colour selection (including if
   * 'case-sensitive' selected and no lower-case colour chosen).
   * 
   * @return
   */
  protected boolean isNoSelectionMade()
  {
    final boolean noUpperCaseSelected = upperCaseButtons == null
            || upperCaseButtons.isEmpty();
    final boolean noLowerCaseSelected = caseSensitive.isSelected()
            && (lowerCaseButtons == null || lowerCaseButtons.isEmpty());
    final boolean noSelectionMade = noUpperCaseSelected
            || noLowerCaseSelected;
    return noSelectionMade;
  }

  /**
   * Applies the current colour scheme to the alignment or sequence group
   */
  @Override
  protected void applyButton_actionPerformed()
  {
    if (isNoSelectionMade())
    {
      JvOptionPane.showMessageDialog(Desktop.desktop,
              MessageManager
                      .getString("label.no_colour_selection_in_scheme"),
              MessageManager.getString("label.no_colour_selection_warn"),
              JvOptionPane.WARNING_MESSAGE);

    }
    UserColourScheme ucs = getSchemeFromButtons();

    ap.alignFrame.changeColour(ucs);
  }

  /**
   * Constructs an instance of UserColourScheme with the residue colours
   * currently set on the buttons on the panel
   * 
   * @return
   */
  UserColourScheme getSchemeFromButtons()
  {

    Color[] newColours = new Color[24];

    int length = upperCaseButtons.size();
    if (length < 24)
    {
      int i = 0;
      for (JButton btn : upperCaseButtons)
      {
        newColours[i] = btn.getBackground();
        i++;
      }
    }
    else
    {
      for (int i = 0; i < 24; i++)
      {
        JButton button = upperCaseButtons.get(i);
        newColours[i] = button.getBackground();
      }
    }

    UserColourScheme ucs = new UserColourScheme(newColours);
    ucs.setName(schemeName.getText());

    if (caseSensitive.isSelected())
    {
      newColours = new Color[23];
      length = lowerCaseButtons.size();
      if (length < 23)
      {
        int i = 0;
        for (JButton btn : lowerCaseButtons)
        {
          newColours[i] = btn.getBackground();
          i++;
        }
      }
      else
      {
        for (int i = 0; i < 23; i++)
        {
          JButton button = lowerCaseButtons.get(i);
          newColours[i] = button.getBackground();
        }
      }
      ucs.setLowerCaseColours(newColours);
    }

    return ucs;
  }

  /**
   * Action on clicking Load scheme button.
   * <ul>
   * <li>Open a file chooser to browse for files with extension .jc</li>
   * <li>Load in the colour scheme and transfer it to this panel's buttons</li>
   * <li>Register the loaded colour scheme</li>
   * </ul>
   */
  @Override
  protected void loadbutton_actionPerformed()
  {
    upperCaseButtons = new ArrayList<>();
    lowerCaseButtons = new ArrayList<>();
    JalviewFileChooser chooser = new JalviewFileChooser("jc",
            "Jalview User Colours");
    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(
            MessageManager.getString("label.load_colour_scheme"));
    chooser.setToolTipText(MessageManager.getString("action.load"));
    chooser.setResponseHandler(0, () -> {
      File choice = chooser.getSelectedFile();
      Cache.setProperty(LAST_DIRECTORY, choice.getParent());

      UserColourScheme ucs = ColourSchemeLoader
              .loadColourScheme(choice.getAbsolutePath());
      Color[] colors = ucs.getColours();
      schemeName.setText(ucs.getSchemeName());

      if (ucs.getLowerCaseColours() != null)
      {
        caseSensitive.setSelected(true);
        lcaseColour.setEnabled(true);
        resetButtonPanel(true);
        for (int i = 0; i < lowerCaseButtons.size(); i++)
        {
          JButton button = lowerCaseButtons.get(i);
          button.setBackground(ucs.getLowerCaseColours()[i]);
        }
      }
      else
      {
        caseSensitive.setSelected(false);
        lcaseColour.setEnabled(false);
        resetButtonPanel(false);
      }

      for (int i = 0; i < upperCaseButtons.size(); i++)
      {
        JButton button = upperCaseButtons.get(i);
        button.setBackground(colors[i]);
      }

      addNewColourScheme(choice.getPath());
    });

    chooser.showOpenDialog(this);
  }

  /**
   * Loads the user-defined colour scheme from the first file listed in property
   * "USER_DEFINED_COLOURS". If this fails, returns an all-white colour scheme.
   * 
   * @return
   */
  public static UserColourScheme loadDefaultColours()
  {
    UserColourScheme ret = null;

    String colours = Cache.getProperty(USER_DEFINED_COLOURS);
    if (colours != null)
    {
      if (colours.indexOf("|") > -1)
      {
        colours = colours.substring(0, colours.indexOf("|"));
      }
      ret = ColourSchemeLoader.loadColourScheme(colours);
    }

    if (ret == null)
    {
      ret = new UserColourScheme("white");
    }

    return ret;
  }

  /**
   * Action on pressing the Save button.
   * <ul>
   * <li>Check a name has been entered</li>
   * <li>Warn if the name already exists, remove any existing scheme of the same
   * name if overwriting</li>
   * <li>Do the standard file chooser thing to write with extension .jc</li>
   * <li>If saving changes (possibly not yet applied) to the currently selected
   * colour scheme, then apply the changes, as it is too late to back out
   * now</li>
   * <li>Don't apply the changes if the currently selected scheme is different,
   * to allow a new scheme to be configured and saved but not applied</li>
   * </ul>
   * If the scheme is saved to file, the 'changed' flag field is reset to false.
   */
  @Override
  protected void savebutton_actionPerformed()
  {
    String name = schemeName.getText().trim();
    if (name.length() < 1)
    {
      JvOptionPane.showInternalMessageDialog(Desktop.desktop,
              MessageManager
                      .getString("label.user_colour_scheme_must_have_name"),
              MessageManager.getString("label.no_name_colour_scheme"),
              JvOptionPane.WARNING_MESSAGE);
    }

    if (!Platform.isJS() && ColourSchemes.getInstance().nameExists(name))
    {
      /**
       * java only
       * 
       * @j2sIgnore
       */
      {
        int reply = JvOptionPane.showInternalConfirmDialog(Desktop.desktop,
                MessageManager.formatMessage(
                        "label.colour_scheme_exists_overwrite", new Object[]
                        { name, name }),
                MessageManager.getString("label.duplicate_scheme_name"),
                JvOptionPane.YES_NO_OPTION);
        if (reply != JvOptionPane.YES_OPTION)
        {
          return;
        }
      }
    }

    JalviewFileChooser chooser = new JalviewFileChooser("jc",
            "Jalview User Colours");

    JalviewFileView fileView = new JalviewFileView();
    chooser.setFileView(fileView);
    chooser.setDialogTitle(
            MessageManager.getString("label.save_colour_scheme"));
    chooser.setToolTipText(MessageManager.getString("action.save"));
    int option = chooser.showSaveDialog(this);
    if (option == JalviewFileChooser.APPROVE_OPTION)
    {
      File file = chooser.getSelectedFile();
      UserColourScheme updatedScheme = addNewColourScheme(file.getPath());
      saveToFile(file);
      changedButNotSaved = false;

      /*
       * changes saved - apply to alignment if we are changing 
       * the currently selected colour scheme; also make the updated
       * colours the 'backout' scheme on Cancel
       */
      if (oldColourScheme != null
              && name.equals(oldColourScheme.getSchemeName()))
      {
        oldColourScheme = updatedScheme;
        applyButton_actionPerformed();
      }
    }
  }

  /**
   * Adds the current colour scheme to the Jalview properties file so it is
   * loaded on next startup, and updates the Colour menu in the parent
   * AlignFrame (if there is one). Note this action does not including applying
   * the colour scheme.
   * 
   * @param filePath
   * @return
   */
  protected UserColourScheme addNewColourScheme(String filePath)
  {
    /*
     * update the delimited list of user defined colour files in
     * Jalview property USER_DEFINED_COLOURS
     */
    String defaultColours = Cache.getDefault(USER_DEFINED_COLOURS,
            filePath);
    if (defaultColours.indexOf(filePath) == -1)
    {
      if (defaultColours.length() > 0)
      {
        defaultColours = defaultColours.concat("|");
      }
      defaultColours = defaultColours.concat(filePath);
    }
    Cache.setProperty(USER_DEFINED_COLOURS, defaultColours);

    /*
     * construct and register the colour scheme
     */
    UserColourScheme ucs = getSchemeFromButtons();
    ColourSchemes.getInstance().registerColourScheme(ucs);

    /*
     * update the Colour menu items
     */
    if (ap != null)
    {
      ap.alignFrame.buildColourMenu();
    }

    return ucs;
  }

  /**
   * Saves the colour scheme to file in XML format
   * 
   * @param path
   */
  protected void saveToFile(File toFile)
  {
    /*
     * build a Java model of colour scheme as XML, and 
     * marshal to file
     */
    JalviewUserColours ucs = new JalviewUserColours();
    String name = schemeName.getText();
    ucs.setSchemeName(name);
    try
    {
      PrintWriter out = new PrintWriter(new OutputStreamWriter(
              new FileOutputStream(toFile), "UTF-8"));

      for (int i = 0; i < buttonPanel.getComponentCount(); i++)
      {
        JButton button = (JButton) buttonPanel.getComponent(i);
        Colour col = new Colour();
        col.setName(button.getText());
        col.setRGB(Format.getHexString(button.getBackground()));
        ucs.getColour().add(col);
      }
      JAXBContext jaxbContext = JAXBContext
              .newInstance(JalviewUserColours.class);
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      jaxbMarshaller.marshal(
              new ObjectFactory().createJalviewUserColours(ucs), out);
      // ucs.marshal(out);
      out.close();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  /**
   * On cancel, restores the colour scheme that was selected before the dialogue
   * was opened
   */
  @Override
  protected void cancelButton_actionPerformed()
  {
    ap.alignFrame.changeColour(oldColourScheme);
    ap.paintAlignment(true, true);

    try
    {
      frame.setClosed(true);
    } catch (Exception ex)
    {
    }
  }

  /**
   * Action on selecting or deselecting the Case Sensitive option. When
   * selected, separate buttons are shown for lower case residues, and the panel
   * is resized to accommodate them. Also, the checkbox for 'apply colour to all
   * lower case' is enabled.
   */
  @Override
  public void caseSensitive_actionPerformed()
  {
    boolean selected = caseSensitive.isSelected();
    resetButtonPanel(selected);
    lcaseColour.setEnabled(selected);
  }
}
