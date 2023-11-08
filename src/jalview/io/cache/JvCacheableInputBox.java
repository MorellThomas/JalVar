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
package jalview.io.cache;

import jalview.bin.Cache;
import jalview.util.MessageManager;
import jalview.util.Platform;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * A class that provides an editable combobox with a memory of previous entries
 * that may be persisted
 * 
 * @author tcofoegbu
 *
 * @param <E>
 */
/*
 * (temporary?) patches to wrap a JTextField instead when running as Javascript
 */
public class JvCacheableInputBox<E>
{
  protected JComboBox<String> comboBox; // used for Jalview

  protected JTextField textField; // used for JalviewJS

  protected JTextComponent textComponent; // used for both

  protected String cacheKey;

  protected AppCache appCache;

  private JPopupMenu popup = new JPopupMenu();

  private JMenuItem menuItemClearCache = new JMenuItem();

  volatile boolean enterWasPressed = false;

  private String prototypeDisplayValue;

  /**
   * @return flag indicating if the most recent keypress was enter
   */
  public boolean wasEnterPressed()
  {
    return enterWasPressed;
  }

  /**
   * Constructor given the key to cached values, and the (approximate) length in
   * characters of the input field
   * 
   * @param newCacheKey
   * @param length
   */
  public JvCacheableInputBox(String newCacheKey, int length)
  {
    // super();
    cacheKey = newCacheKey;
    prototypeDisplayValue = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
    if (length > 0)
    {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < length; i++)
      {
        sb.append("X");
      }
      setPrototypeDisplayValue(sb.toString());
    }
    boolean useTextField = Platform.isJS();
    // BH 2019.03 only switch for JavaScript here
    // SwingJS TODO implement editable combo box
    if (useTextField)
    {
      appCache = null;
      textComponent = textField = new JTextField();
      // {
      // @Override
      // public Dimension getPreferredSize() {
      // return super.getPreferredSize();
      //// FontMetrics fm = getFontMetrics(getFont());
      //// return new Dimension(fm.stringWidth(prototypeDisplayValue),
      // fm.getHeight());
      // }
      // };
    }
    else
    {
      appCache = AppCache.getInstance();
      comboBox = new JComboBox<>();
      textComponent = (JTextComponent) comboBox.getEditor()
              .getEditorComponent();
      comboBox.setEditable(true);
      comboBox.addKeyListener(new KeyAdapter()
      {
        @Override
        public void keyTyped(KeyEvent e)
        {
          enterWasPressed = false;
          if (e.getKeyCode() == KeyEvent.VK_ENTER)
          {
            enterWasPressed = true;
          }
          // let event bubble up
        }
      });
      comboBox.setPrototypeDisplayValue(prototypeDisplayValue);
      initCachePopupMenu();
      initCache(newCacheKey);
      updateCache();
    }
  }

  /**
   * Method for initialising cache items for a given cache key and populating
   * the in-memory cache with persisted cache items
   * 
   * @param cacheKey
   */
  private void initCache(String cacheKey)
  {
    if (appCache == null)
    {
      return;
    }
    // obtain persisted cache items from properties file as a delimited string
    String delimitedCacheStr = Cache.getProperty(cacheKey);
    if (delimitedCacheStr == null || delimitedCacheStr.isEmpty())
    {
      return;
    }
    // convert delimited cache items to a list of strings
    List<String> persistedCacheItems = Arrays
            .asList(delimitedCacheStr.split(AppCache.CACHE_DELIMITER));

    LinkedHashSet<String> foundCacheItems = appCache
            .getAllCachedItemsFor(cacheKey);
    if (foundCacheItems == null)
    {
      foundCacheItems = new LinkedHashSet<>();
    }
    // populate memory cache
    for (String cacheItem : persistedCacheItems)
    {
      foundCacheItems.add(cacheItem);
    }
    appCache.putCache(cacheKey, foundCacheItems);
  }

  /**
   * Initialise this cache's pop-up menu
   */
  private void initCachePopupMenu()
  {
    if (appCache == null)
    {
      return;
    }
    menuItemClearCache.setFont(new java.awt.Font("Verdana", 0, 12));
    menuItemClearCache
            .setText(MessageManager.getString("action.clear_cached_items"));
    menuItemClearCache.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        // System.out.println(">>>>> Clear cache items");
        setSelectedItem("");
        appCache.deleteCacheItems(cacheKey);
        updateCache();
      }
    });

    popup.add(menuItemClearCache);
    comboBox.setComponentPopupMenu(popup);
    comboBox.add(popup);
  }

  /**
   * Answers true if input text is an integer
   * 
   * @param text
   * @return
   */
  static boolean isInteger(String text)
  {
    try
    {
      Integer.parseInt(text);
      return true;
    } catch (NumberFormatException e)
    {
      return false;
    }
  }

  /**
   * Method called to update the cache with the last user input
   */
  public void updateCache()
  {
    if (appCache == null)
    {
      return;
    }
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        int cacheLimit = Integer.parseInt(appCache.getCacheLimit(cacheKey));
        String userInput = getUserInput();
        if (userInput != null && !userInput.isEmpty())
        {
          LinkedHashSet<String> foundCache = appCache
                  .getAllCachedItemsFor(cacheKey);
          // remove old cache item so as to place current input at the top of
          // the result
          foundCache.remove(userInput);
          foundCache.add(userInput);
          appCache.putCache(cacheKey, foundCache);
        }

        String lastSearch = userInput;
        if (comboBox.getItemCount() > 0)
        {
          comboBox.removeAllItems();
        }
        Set<String> cacheItems = appCache.getAllCachedItemsFor(cacheKey);
        List<String> reversedCacheItems = new ArrayList<>();
        reversedCacheItems.addAll(cacheItems);
        cacheItems = null;
        Collections.reverse(reversedCacheItems);
        if (lastSearch.isEmpty())
        {
          comboBox.addItem("");
        }

        if (reversedCacheItems != null && !reversedCacheItems.isEmpty())
        {
          LinkedHashSet<String> foundCache = appCache
                  .getAllCachedItemsFor(cacheKey);
          boolean prune = reversedCacheItems.size() > cacheLimit;
          int count = 1;
          boolean limitExceeded = false;
          for (String cacheItem : reversedCacheItems)
          {
            limitExceeded = (count++ > cacheLimit);
            if (prune)
            {
              if (limitExceeded)
              {
                foundCache.remove(cacheItem);
              }
              else
              {
                comboBox.addItem(cacheItem);
              }
            }
            else
            {
              comboBox.addItem(cacheItem);
            }
          }
          appCache.putCache(cacheKey, foundCache);
        }
        setSelectedItem(lastSearch.isEmpty() ? "" : lastSearch);
      }
    });
  }

  /**
   * This method should be called to persist the in-memory cache when this
   * components parent frame is closed / exited
   */
  public void persistCache()
  {
    if (appCache == null)
    {
      return;
    }
    appCache.persistCache(cacheKey);
  }

  /**
   * Returns the trimmed text in the input field
   * 
   * @return
   */
  public String getUserInput()
  {
    if (comboBox == null)
    {
      return textField.getText().trim();
    }
    Object item = comboBox.getEditor().getItem();
    return item == null ? "" : item.toString().trim();
  }

  public JComponent getComponent()
  {
    return (comboBox == null ? textField : comboBox);
  }

  public void addActionListener(ActionListener actionListener)
  {
    if (comboBox == null)
    {
      textField.addActionListener(actionListener);
    }
    else
    {
      comboBox.addActionListener(actionListener);
    }
  }

  public void addDocumentListener(DocumentListener listener)
  {
    textComponent.getDocument().addDocumentListener(listener);
  }

  public void addFocusListener(FocusListener focusListener)
  {
    textComponent.addFocusListener(focusListener);
  }

  public void addKeyListener(KeyListener kl)
  {
    textComponent.addKeyListener(kl);
  }

  public void addCaretListener(CaretListener caretListener)
  {
    textComponent.addCaretListener(caretListener);
  }

  public void setEditable(boolean b)
  {
    if (comboBox != null)
    {
      comboBox.setEditable(b);
    }
  }

  public void setPrototypeDisplayValue(String string)
  {
    prototypeDisplayValue = string;
    if (comboBox != null)
    {
      comboBox.setPrototypeDisplayValue(string);
    }
  }

  public void setSelectedItem(String userInput)
  {
    if (comboBox != null)
    {
      comboBox.setSelectedItem(userInput);
    }
  }

  public boolean isPopupVisible()
  {
    return (comboBox != null && comboBox.isPopupVisible());
  }

  public void addItem(String item)
  {
    if (comboBox != null)
    {
      comboBox.addItem(item);
    }
  }

}
