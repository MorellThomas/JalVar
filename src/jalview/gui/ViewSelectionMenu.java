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

import jalview.api.AlignmentViewPanel;
import jalview.util.MessageManager;
import jalview.util.Platform;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * this is an implementation of an abstract Jalview GUI class that provides a
 * dialog/menu which allows the user to select/deselect specific views from a
 * list of associated views.
 * 
 * Includes patches related to JAL-641
 * 
 * @author JimP
 * 
 */
public class ViewSelectionMenu extends JMenu
{
  public interface ViewSetProvider
  {
    public AlignmentPanel[] getAllAlignmentPanels();
  }

  private ViewSetProvider _allviews;

  private List<AlignmentViewPanel> _selectedviews;

  private ItemListener _handler;

  /**
   * create a new view selection menu. This menu has some standard entries
   * (select all, invert selection), and a checkbox for every view. Mousing over
   * a view entry will cause it to be raised/selected in the Desktop, allowing
   * the user to easily identify which view is being referred to.
   * 
   * @param title
   *          Name of menu
   * @param allviews
   *          all the views that might be selected
   * @param selectedviews
   *          the list of selected views which will be updated when
   *          selection/deselections occur
   * @param handler
   *          a handler called for each selection/deselection - use this to
   *          update any gui elements which need to reflect current
   *          selection/deselection state
   */
  public ViewSelectionMenu(String title, final ViewSetProvider allviews,
          final List<AlignmentViewPanel> selectedviews,
          final ItemListener handler)
  {
    super(title);
    this._allviews = allviews;
    this._selectedviews = selectedviews;
    this._handler = handler;
    addMenuListener(new MenuListener()
    {

      @Override
      public void menuSelected(MenuEvent e)
      {
        rebuild();

      }

      @Override
      public void menuDeselected(MenuEvent e)
      {
        // TODO Auto-generated method stub

      }

      @Override
      public void menuCanceled(MenuEvent e)
      {
        // TODO Auto-generated method stub

      }
    });
  }

  /**
   * view selection modifier flag - indicates if an action key is pressed when
   * menu selection event occurred.
   */
  private boolean append = false;

  /**
   * flag indicating if the itemStateChanged listener for view associated menu
   * items is currently enabled
   */
  private boolean enabled = true;

  private JMenuItem selectAll, invertSel;

  private JCheckBoxMenuItem toggleview = null;

  private void rebuild()
  {
    removeAll();
    AlignmentPanel[] allviews = _allviews.getAllAlignmentPanels();
    if (allviews == null)
    {
      setVisible(false);
      return;
    }
    if (allviews.length >= 2)
    {
      // ensure we update menu state to reflect external selection list state
      append = append || _selectedviews.size() > 1;
      toggleview = new JCheckBoxMenuItem(
              MessageManager.getString("label.select_many_views"), append);
      toggleview.setToolTipText(
              MessageManager.getString("label.toggle_enabled_views"));
      toggleview.addItemListener(new ItemListener()
      {

        @Override
        public void itemStateChanged(ItemEvent arg0)
        {
          if (enabled)
          {
            append = !append;
            selectAll.setEnabled(append);
            invertSel.setEnabled(append);
          }

        }

      });
      add(toggleview);
      add(selectAll = new JMenuItem(
              MessageManager.getString("label.select_all_views")));
      selectAll.addActionListener(new ActionListener()
      {

        @Override
        public void actionPerformed(ActionEvent e)
        {
          for (Component c : getMenuComponents())
          {
            boolean t = append;
            append = true;
            if (c instanceof JCheckBoxMenuItem)
            {
              if (toggleview != c && !((JCheckBoxMenuItem) c).isSelected())
              {
                ((JCheckBoxMenuItem) c).doClick();
              }
            }
            append = t;
          }
        }
      });
      add(invertSel = new JMenuItem(
              MessageManager.getString("label.invert_selection")));
      invertSel.addActionListener(new ActionListener()
      {

        @Override
        public void actionPerformed(ActionEvent e)
        {
          boolean t = append;
          append = true;
          for (Component c : getMenuComponents())
          {
            if (toggleview != c && c instanceof JCheckBoxMenuItem)
            {
              ((JCheckBoxMenuItem) c).doClick();
            }
          }
          append = t;
        }
      });
      invertSel.setEnabled(append);
      selectAll.setEnabled(append);
    }
    for (final AlignmentPanel ap : allviews)
    {
      String nm = ((ap.getViewName() == null
              || ap.getViewName().length() == 0) ? ""
                      : ap.getViewName() + " for ")
              + ap.alignFrame.getTitle();
      final JCheckBoxMenuItem checkBox = new JCheckBoxMenuItem(nm,
              _selectedviews.contains(ap));
      checkBox.addItemListener(new ItemListener()
      {
        @Override
        public void itemStateChanged(ItemEvent e)
        {
          if (enabled)
          {
            if (append)
            {
              enabled = false;
              // toggle the inclusion state
              if (_selectedviews.indexOf(ap) == -1)
              {
                _selectedviews.add(ap);
                checkBox.setSelected(true);
              }
              else
              {
                _selectedviews.remove(ap);
                checkBox.setSelected(false);
              }
              enabled = true;
              _handler.itemStateChanged(e);
            }
            else
            {
              // Deselect everything and select this item only
              _selectedviews.clear();
              _selectedviews.add(ap);
              enabled = false;
              for (Component c : getMenuComponents())
              {
                if (c instanceof JCheckBoxMenuItem)
                {
                  ((JCheckBoxMenuItem) c).setSelected(checkBox == c);
                }
              }
              enabled = true;
              // only fire event if we weren't selected before
              _handler.itemStateChanged(e);
            }
          }
        }
      });
      final ViewSelectionMenu us = this;
      checkBox.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mouseExited(MouseEvent e)
        {
          try
          {
          } catch (Exception ex)
          {
          }
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
          try
          {
            ap.setAlignFrameView();
          } catch (Exception ex)
          {
          }
        }
      });
      add(checkBox);
    }
  }

}
