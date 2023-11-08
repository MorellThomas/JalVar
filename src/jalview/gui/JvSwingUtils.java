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
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import jalview.util.MessageManager;

/**
 * useful functions for building Swing GUIs
 * 
 * @author JimP
 * 
 */
public final class JvSwingUtils
{
  /**
   * wrap a bare html safe string to around 60 characters per line using a CSS
   * style class specifying word-wrap and break-word
   * 
   * @param enclose
   *          if true, add &lt;html&gt; wrapper tags
   * @param ttext
   * 
   * @return
   */
  public static String wrapTooltip(boolean enclose, String ttext)
  {
    Objects.requireNonNull(ttext,
            "Tootip text to format must not be null!");
    ttext = ttext.trim();
    boolean maxLengthExceeded = false;

    if (ttext.contains("<br>"))
    {
      String[] htmllines = ttext.split("<br>");
      for (String line : htmllines)
      {
        maxLengthExceeded = line.length() > 60;
        if (maxLengthExceeded)
        {
          break;
        }
      }
    }
    else
    {
      maxLengthExceeded = ttext.length() > 60;
    }

    if (!maxLengthExceeded)
    {
      return enclose ? "<html>" + ttext + "</html>" : ttext;
    }

    return (enclose ? "<html>" : "")
            // BH 2018
            + "<style> div.ttip {width:350px;white-space:pre-wrap;padding:2px;overflow-wrap:break-word;}</style><div class=\"ttip\">"
            // + "<style> p.ttip {width:350px;margin:-14px 0px -14px
            // 0px;padding:2px;overflow-wrap:break-word;}"
            // + "</style><p class=\"ttip\">"
            + ttext + " </div>"
            // + "</p>"
            + ((enclose ? "</html>" : ""));
  }

  public static JButton makeButton(String label, String tooltip,
          ActionListener action)
  {
    JButton button = new JButton();
    button.setText(label);
    // TODO: get the base font metrics for the Jalview gui from somewhere
    button.setFont(new java.awt.Font("Verdana", Font.PLAIN, 10));
    button.setForeground(Color.black);
    button.setHorizontalAlignment(SwingConstants.CENTER);
    button.setToolTipText(tooltip);
    button.addActionListener(action);
    return button;
  }

  /**
   * find or add a submenu with the given title in the given menu
   * 
   * @param menu
   * @param submenu
   * @return the new or existing submenu
   */
  public static JMenu findOrCreateMenu(JMenu menu, String submenu)
  {
    JMenu submenuinstance = null;
    for (int i = 0, iSize = menu.getMenuComponentCount(); i < iSize; i++)
    {
      if (menu.getMenuComponent(i) instanceof JMenu
              && ((JMenu) menu.getMenuComponent(i)).getText()
                      .equals(submenu))
      {
        submenuinstance = (JMenu) menu.getMenuComponent(i);
      }
    }
    if (submenuinstance == null)
    {
      submenuinstance = new JMenu(submenu);
      menu.add(submenuinstance);
    }
    return submenuinstance;

  }

  /**
   * 
   * @param panel
   * @param tooltip
   * @param label
   * @param valBox
   * @return the GUI element created that was added to the layout so it's
   *         attributes can be changed.
   */
  public static JPanel addtoLayout(JPanel panel, String tooltip,
          JComponent label, JComponent valBox)
  {
    JPanel laypanel = new JPanel(new GridLayout(1, 2));
    JPanel labPanel = new JPanel(new BorderLayout());
    JPanel valPanel = new JPanel();
    labPanel.setBounds(new Rectangle(7, 7, 158, 23));
    valPanel.setBounds(new Rectangle(172, 7, 270, 23));
    labPanel.add(label, BorderLayout.WEST);
    valPanel.add(valBox);
    laypanel.add(labPanel);
    laypanel.add(valPanel);
    valPanel.setToolTipText(tooltip);
    labPanel.setToolTipText(tooltip);
    valBox.setToolTipText(tooltip);
    panel.add(laypanel);
    panel.validate();
    return laypanel;
  }

  public static void mgAddtoLayout(JPanel cpanel, String tooltip,
          JLabel jLabel, JComponent name)
  {
    mgAddtoLayout(cpanel, tooltip, jLabel, name, null);
  }

  public static void mgAddtoLayout(JPanel cpanel, String tooltip,
          JLabel jLabel, JComponent name, String params)
  {
    cpanel.add(jLabel);
    if (params == null)
    {
      cpanel.add(name);
    }
    else
    {
      cpanel.add(name, params);
    }
    name.setToolTipText(tooltip);
    jLabel.setToolTipText(tooltip);
  }

  /**
   * standard font for labels and check boxes in dialog boxes
   * 
   * @return
   */

  public static Font getLabelFont()
  {
    return getLabelFont(false, false);
  }

  public static Font getLabelFont(boolean bold, boolean italic)
  {
    return new java.awt.Font("Verdana",
            (!bold && !italic) ? Font.PLAIN
                    : (bold ? Font.BOLD : 0) + (italic ? Font.ITALIC : 0),
            11);
  }

  /**
   * standard font for editable text areas
   * 
   * @return
   */
  public static Font getTextAreaFont()
  {
    return getLabelFont(false, false);
  }

  /**
   * clean up a swing menu. Removes any empty submenus without selection
   * listeners.
   * 
   * @param webService
   */
  public static void cleanMenu(JMenu webService)
  {
    for (int i = 0; i < webService.getItemCount();)
    {
      JMenuItem item = webService.getItem(i);
      if (item instanceof JMenu && ((JMenu) item).getItemCount() == 0)
      {
        webService.remove(i);
      }
      else
      {
        i++;
      }
    }
  }

  /**
   * Returns the proportion of its range that a scrollbar's position represents,
   * as a value between 0 and 1. For example if the whole range is from 0 to
   * 200, then a position of 40 gives proportion = 0.2.
   * 
   * @see http://www.javalobby.org/java/forums/t33050.html#91885334
   * 
   * @param scroll
   * @return
   */
  public static float getScrollBarProportion(JScrollBar scroll)
  {
    /*
     * The extent (scroll handle width) deduction gives the true operating range
     * of possible positions.
     */
    int possibleRange = scroll.getMaximum() - scroll.getMinimum()
            - scroll.getModel().getExtent();
    float valueInRange = scroll.getValue()
            - (scroll.getModel().getExtent() / 2f);
    float proportion = valueInRange / possibleRange;
    return proportion;
  }

  /**
   * Returns the scroll bar position in its range that would match the given
   * proportion (between 0 and 1) of the whole. For example if the whole range
   * is from 0 to 200, then a proportion of 0.25 gives position 50.
   * 
   * @param scrollbar
   * @param proportion
   * @return
   */
  public static int getScrollValueForProportion(JScrollBar scrollbar,
          float proportion)
  {
    /*
     * The extent (scroll handle width) deduction gives the true operating range
     * of possible positions.
     */
    float fraction = proportion
            * (scrollbar.getMaximum() - scrollbar.getMinimum()
                    - scrollbar.getModel().getExtent())
            + (scrollbar.getModel().getExtent() / 2f);
    return Math.min(Math.round(fraction), scrollbar.getMaximum());
  }

  public static void jvInitComponent(AbstractButton comp, String i18nString)
  {
    setColorAndFont(comp);
    if (i18nString != null && !i18nString.isEmpty())
    {
      comp.setText(MessageManager.getString(i18nString));
    }
  }

  public static void jvInitComponent(JComponent comp)
  {
    setColorAndFont(comp);
  }

  private static void setColorAndFont(JComponent comp)
  {
    comp.setBackground(Color.white);
    comp.setFont(JvSwingUtils.getLabelFont());
  }

  /**
   * A helper method to build a drop-down choice of values, with tooltips for
   * the entries
   * 
   * @param entries
   * @param tooltips
   */
  public static JComboBox<Object> buildComboWithTooltips(
          List<Object> entries, List<String> tooltips)
  {
    JComboBox<Object> combo = new JComboBox<>();
    final ComboBoxTooltipRenderer renderer = new ComboBoxTooltipRenderer();
    combo.setRenderer(renderer);
    for (Object attName : entries)
    {
      combo.addItem(attName);
    }
    renderer.setTooltips(tooltips);
    final MouseAdapter mouseListener = new MouseAdapter()
    {
      @Override
      public void mouseEntered(MouseEvent e)
      {
        int j = combo.getSelectedIndex();
        if (j > -1)
        {
          combo.setToolTipText(tooltips.get(j));
        }
      }

      @Override
      public void mouseExited(MouseEvent e)
      {
        combo.setToolTipText(null);
      }
    };
    for (Component c : combo.getComponents())
    {
      c.addMouseListener(mouseListener);
    }
    return combo;
  }

  /**
   * Adds a titled border to the component in the default font and position (top
   * left), optionally witht italic text
   * 
   * @param comp
   * @param title
   * @param italic
   */
  public static TitledBorder createTitledBorder(JComponent comp,
          String title, boolean italic)
  {
    Font font = comp.getFont();
    if (italic)
    {
      font = new Font(font.getName(), Font.ITALIC, font.getSize());
    }
    Border border = BorderFactory.createTitledBorder("");
    TitledBorder titledBorder = BorderFactory.createTitledBorder(border,
            title, TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
            font);
    comp.setBorder(titledBorder);

    return titledBorder;
  }

}
