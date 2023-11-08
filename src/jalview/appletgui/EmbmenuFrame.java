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
package jalview.appletgui;

import jalview.util.Platform;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implements a pattern for embedding toolbars as a panel with popups
 * for situations where the system menu bar is either invisible or
 * inappropriate. It was derived from the code for embedding the jalview applet
 * alignFrame as a component on the web-page, which requires the local
 * alignFrame menu to be attached to that panel rather than placed on the parent
 * (which isn't allowed anyhow). TODO: try to modify the embeddedMenu display so
 * it looks like a real toolbar menu TODO: modify click/mouse handler for
 * embeddedMenu so it behaves more like a real pulldown menu toolbar
 * 
 * @author Jim Procter and Andrew Waterhouse
 * 
 */
public class EmbmenuFrame extends Frame
        implements MouseListener, AutoCloseable
{
  protected static final Font FONT_ARIAL_PLAIN_11 = new Font("Arial",
          Font.PLAIN, 11);

  public static final Font DEFAULT_MENU_FONT = FONT_ARIAL_PLAIN_11;

  /**
   * map from labels to popup menus for the embedded menubar
   */
  protected Map<Label, PopupMenu> embeddedPopup = new HashMap<>();

  /**
   * the embedded menu is built on this and should be added to the frame at the
   * appropriate position.
   * 
   */
  protected Panel embeddedMenu;

  public EmbmenuFrame() throws HeadlessException
  {
    super();
  }

  public EmbmenuFrame(String title) throws HeadlessException
  {
    super(title);
  }

  /**
   * Check if the applet is running on a platform that requires the Frame
   * menuBar to be embedded, and if so, embeds it.
   * 
   * @param tobeAdjusted
   *          the panel that is to be reduced to make space for the embedded
   *          menu bar
   * @return true if menuBar was embedded and tobeAdjusted's height modified
   */
  protected boolean embedMenuIfNeeded(Panel tobeAdjusted)
  {
    MenuBar topMenuBar = getMenuBar();
    if (topMenuBar == null)
    {
      return false;
    }
    // DEBUG Hint: can test embedded menus by inserting true here.
    if (Platform.isAMacAndNotJS())
    {
      // Build the embedded menu panel, allowing override with system font
      embeddedMenu = makeEmbeddedPopupMenu(topMenuBar, true, false);
      setMenuBar(null);
      // add the components to the Panel area.
      add(embeddedMenu, BorderLayout.NORTH);
      tobeAdjusted.setSize(getSize().width,
              getSize().height - embeddedMenu.getHeight());
      return true;
    }
    return false;
  }

  /**
   * Create or add elements to the embedded menu from menuBar. This removes all
   * menu from menuBar and it is up to the caller to remove the now useless
   * menuBar from the Frame if it is already attached.
   * 
   * @param menuBar
   * @param overrideFonts
   * @param append
   *          true means existing menu will be emptied before adding new
   *          elements
   * @return
   */
  protected Panel makeEmbeddedPopupMenu(MenuBar menuBar,
          boolean overrideFonts, boolean append)
  {
    if (!append)
    {
      embeddedPopup.clear(); // TODO: check if j1.1
      if (embeddedMenu != null)
      {
        embeddedMenu.removeAll();
      }
    }
    embeddedMenu = makeEmbeddedPopupMenu(menuBar, DEFAULT_MENU_FONT,
            overrideFonts, new Panel(), this);
    return embeddedMenu;
  }

  /**
   * Generic method to move elements from menubar onto embeddedMenu using the
   * existing or the supplied font, and adds binding from panel to attached
   * menus in embeddedPopup This removes all menu from menuBar and it is up to
   * the caller to remove the now useless menuBar from the Frame if it is
   * already attached.
   * 
   * @param menuBar
   *          must be non-null
   * @param font
   * @param overrideFonts
   * @param embeddedMenu
   *          if null, a new panel will be created and returned
   * @param clickHandler
   *          - usually the instance of EmbmenuFrame that holds references to
   *          embeddedPopup and embeddedMenu
   * @return the panel instance for convenience.
   */
  protected Panel makeEmbeddedPopupMenu(MenuBar menuBar, Font font,
          boolean overrideFonts, Panel embeddedMenu,
          MouseListener clickHandler)
  {
    if (overrideFonts)
    {
      Font mbf = menuBar.getFont();
      if (mbf != null)
      {
        font = mbf;
      }
    }
    if (embeddedMenu == null)
    {
      embeddedMenu = new Panel();
    }
    FlowLayout flowLayout1 = new FlowLayout();
    embeddedMenu.setBackground(Color.lightGray);
    embeddedMenu.setLayout(flowLayout1);
    // loop thru
    for (int mbi = 0, nMbi = menuBar.getMenuCount(); mbi < nMbi; mbi++)
    {
      Menu mi = menuBar.getMenu(mbi);
      Label elab = new Label(mi.getLabel());
      elab.setFont(font);
      // add the menu entries
      PopupMenu popup = new PopupMenu();
      int m, mSize = mi.getItemCount();
      for (m = 0; m < mSize; m++)
      {
        popup.add(mi.getItem(m));
        mSize--;
        m--;
      }
      embeddedPopup.put(elab, popup);
      embeddedMenu.add(elab);
      elab.addMouseListener(clickHandler);
    }
    flowLayout1.setAlignment(FlowLayout.LEFT);
    flowLayout1.setHgap(2);
    flowLayout1.setVgap(0);
    return embeddedMenu;
  }

  @Override
  public void mousePressed(MouseEvent evt)
  {
    PopupMenu popup = null;
    Label source = (Label) evt.getSource();
    popup = getPopupMenu(source);
    if (popup != null)
    {
      embeddedMenu.add(popup);
      popup.show(embeddedMenu, source.getBounds().x,
              source.getBounds().y + source.getBounds().getSize().height);
    }
  }

  /**
   * get the menu for source from the hash.
   * 
   * @param source
   *          what was clicked on.
   */
  PopupMenu getPopupMenu(Label source)
  {
    return embeddedPopup.get(source);
  }

  @Override
  public void mouseClicked(MouseEvent evt)
  {
  }

  @Override
  public void mouseReleased(MouseEvent evt)
  {
  }

  @Override
  public void mouseEntered(MouseEvent evt)
  {
  }

  @Override
  public void mouseExited(MouseEvent evt)
  {
  }

  /**
   * called to clear the GUI resources taken up for embedding and remove any
   * self references so we can be garbage collected.
   */
  public void destroyMenus()
  {
    if (embeddedPopup != null)
    {
      for (Label lb : embeddedPopup.keySet())
      {
        lb.removeMouseListener(this);
      }
      embeddedPopup.clear();
    }
    if (embeddedMenu != null)
    {
      embeddedMenu.removeAll();
    }
  }

  /**
   * calls destroyMenus()
   */
  @Override
  public void close()
  {
    destroyMenus();
    embeddedPopup = null;
    embeddedMenu = null;
  }
}
