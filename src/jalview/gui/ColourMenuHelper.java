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

import jalview.bin.Cache;
import jalview.datamodel.AnnotatedCollectionI;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ColourSchemeLoader;
import jalview.schemes.ColourSchemes;
import jalview.schemes.ResidueColourScheme;
import jalview.schemes.UserColourScheme;
import jalview.util.MessageManager;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

public class ColourMenuHelper
{
  public interface ColourChangeListener
  {
    /**
     * Change colour scheme to the selected scheme
     * 
     * @param name
     *          the registered (unique) name of a colour scheme
     */
    void changeColour_actionPerformed(String name);
  }

  /**
   * Adds items to the colour menu, as mutually exclusive members of a button
   * group. The callback handler is responsible for the action on selecting any
   * of these options. The callback method receives the name of the selected
   * colour, or "None" or "User Defined". This method returns the ButtonGroup to
   * which items were added.
   * <ul>
   * <li>None</li>
   * <li>Clustal</li>
   * <li>...other 'built-in' colours</li>
   * <li>...any user-defined colours</li>
   * <li>User Defined..(only for AlignFrame menu)</li>
   * </ul>
   * 
   * @param colourMenu
   *          the menu to attach items to
   * @param client
   *          a callback to handle menu selection
   * @param coll
   *          the data the menu is being built for
   * @param simpleOnly
   *          if true, only simple per-residue colour schemes are included
   */
  public static ButtonGroup addMenuItems(final JMenu colourMenu,
          final ColourChangeListener client, AnnotatedCollectionI coll,
          boolean simpleOnly)
  {
    /*
     * ButtonGroup groups those items whose 
     * selection is mutually exclusive
     */
    ButtonGroup colours = new ButtonGroup();

    if (!simpleOnly)
    {
      JRadioButtonMenuItem noColourmenuItem = new JRadioButtonMenuItem(
              MessageManager.getString("label.none"));
      noColourmenuItem.setName(ResidueColourScheme.NONE);
      noColourmenuItem.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          client.changeColour_actionPerformed(ResidueColourScheme.NONE);
        }
      });
      colourMenu.add(noColourmenuItem);
      colours.add(noColourmenuItem);
    }

    /*
     * scan registered colour schemes (built-in or user-defined)
     * and add them to the menu (in the order they were registered)
     */
    Iterable<ColourSchemeI> colourSchemes = ColourSchemes.getInstance()
            .getColourSchemes();
    for (ColourSchemeI scheme : colourSchemes)
    {
      if (simpleOnly && !scheme.isSimple())
      {
        continue;
      }

      /*
       * button text is i18n'd but the name is the canonical name of
       * the colour scheme (inspected in setColourSelected())
       */
      final String name = scheme.getSchemeName();
      String label = MessageManager.getStringOrReturn("label.colourScheme_",
              name);
      final JRadioButtonMenuItem radioItem = new JRadioButtonMenuItem(
              label);
      radioItem.setName(name);
      radioItem.setEnabled(scheme.isApplicableTo(coll));
      if (scheme instanceof UserColourScheme)
      {
        /*
         * user-defined colour scheme loaded on startup or during the
         * Jalview session; right-click on this offers the option to
         * remove it as a colour choice (unless currently selected)
         */
        radioItem.addMouseListener(new MouseAdapter()
        {
          @Override
          public void mousePressed(MouseEvent evt)
          {
            if (evt.isPopupTrigger() && !radioItem.isSelected()) // Mac
            {
              offerRemoval();
            }
          }

          @Override
          public void mouseReleased(MouseEvent evt)
          {
            if (evt.isPopupTrigger() && !radioItem.isSelected()) // Windows
            {
              offerRemoval();
            }
          }

          void offerRemoval()
          {
            ActionListener al = radioItem.getActionListeners()[0];
            radioItem.removeActionListener(al);
            int option = JvOptionPane.showInternalConfirmDialog(
                    Desktop.desktop,
                    MessageManager
                            .getString("label.remove_from_default_list"),
                    MessageManager
                            .getString("label.remove_user_defined_colour"),
                    JvOptionPane.YES_NO_OPTION);
            if (option == JvOptionPane.YES_OPTION)
            {
              ColourSchemes.getInstance()
                      .removeColourScheme(radioItem.getName());
              colourMenu.remove(radioItem);
              updatePreferences();
            }
            else
            {
              radioItem.addActionListener(al);
            }
          }
        });
      }
      radioItem.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent evt)
        {
          client.changeColour_actionPerformed(name);
        }
      });
      colourMenu.add(radioItem);
      colours.add(radioItem);
    }

    /*
     * only add the option to load/configure a user-defined colour
     * to the AlignFrame colour menu
     */
    if (client instanceof AlignFrame)
    {
      final String label = MessageManager.getString("action.user_defined");
      JRadioButtonMenuItem userDefinedColour = new JRadioButtonMenuItem(
              label);
      userDefinedColour.setName(ResidueColourScheme.USER_DEFINED_MENU);
      userDefinedColour.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          client.changeColour_actionPerformed(
                  ResidueColourScheme.USER_DEFINED_MENU);
        }
      });
      colourMenu.add(userDefinedColour);
      colours.add(userDefinedColour);
    }

    return colours;
  }

  /**
   * Marks as selected the colour menu item matching the given colour scheme, or
   * the first item ('None') if no match is found. If the colour scheme is a
   * user defined scheme, but not in the menu (this arises if a new scheme is
   * defined and applied but not saved to file), then menu option "User
   * Defined.." is selected.
   * 
   * @param colourMenu
   * @param cs
   */
  public static void setColourSelected(JMenu colourMenu, ColourSchemeI cs)
  {
    String colourName = cs == null ? ResidueColourScheme.NONE
            : cs.getSchemeName();

    JRadioButtonMenuItem none = null;
    JRadioButtonMenuItem userDefined = null;

    /*
     * select the radio button whose name matches the colour name
     * (not the button text, as it may be internationalised)
     */
    for (Component menuItem : colourMenu.getMenuComponents())
    {
      if (menuItem instanceof JRadioButtonMenuItem)
      {
        JRadioButtonMenuItem radioButton = (JRadioButtonMenuItem) menuItem;
        String buttonName = radioButton.getName();
        if (buttonName.equals(colourName))
        {
          radioButton.setSelected(true);
          return;
        }
        if (ResidueColourScheme.NONE.equals(buttonName))
        {
          none = radioButton;
        }
        if (ResidueColourScheme.USER_DEFINED_MENU.equals(buttonName))
        {
          userDefined = radioButton;
        }
      }
    }

    /*
     * no match by name; select User Defined.. if current scheme is a 
     * user defined one, else select None
     */
    if (cs instanceof UserColourScheme && userDefined != null)
    {
      userDefined.setSelected(true);
    }
    else if (none != null)
    {
      none.setSelected(true);
    }
  }

  /**
   * Updates the USER_DEFINE_COLOURS preference to remove any de-registered
   * colour scheme
   */
  static void updatePreferences()
  {
    StringBuilder coloursFound = new StringBuilder();
    String[] files = Cache.getProperty("USER_DEFINED_COLOURS").split("\\|");

    /*
     * the property does not include the scheme name, it is in the file;
     * so just load the colour schemes and discard any whose name is not
     * registered
     */
    for (String file : files)
    {
      try
      {
        UserColourScheme ucs = ColourSchemeLoader.loadColourScheme(file);
        if (ucs != null
                && ColourSchemes.getInstance().nameExists(ucs.getName()))
        {
          if (coloursFound.length() > 0)
          {
            coloursFound.append("|");
          }
          coloursFound.append(file);
        }
      } catch (Exception ex)
      {
        System.out.println("Error loading User ColourFile\n" + ex);
      }
    }

    if (coloursFound.toString().length() > 1)
    {
      Cache.setProperty("USER_DEFINED_COLOURS", coloursFound.toString());
    }
    else
    {
      Cache.applicationProperties.remove("USER_DEFINED_COLOURS");
    }
  }
}
