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

package jalview.urls;

import jalview.util.MessageManager;
import jalview.util.UrlLink;

import java.util.ArrayList;
import java.util.List;

/**
 * UrlLink table row definition
 * 
 * @author $author$
 * @version $Revision$
 */

public class UrlLinkDisplay
{
  // column positions
  public static final int DATABASE = 0;

  public static final int NAME = 1;

  public static final int URL = 2;

  public static final int SELECTED = 3;

  public static final int PRIMARY = 4;

  public static final int ID = 5;

  // Headers for columns in table
  @SuppressWarnings("serial")
  private static final List<String> COLNAMES = new ArrayList<String>()
  {
    {
      add(MessageManager.formatMessage("label.database"));
      add(MessageManager.formatMessage("label.name"));
      add(MessageManager.formatMessage("label.url"));
      add(MessageManager.formatMessage("label.inmenu"));
      add(MessageManager.formatMessage("label.primary"));
      add(MessageManager.formatMessage("label.id"));
    }
  };

  private String id; // id is not supplied to display, but used to identify
  // entries when saved

  private boolean isPrimary;

  private boolean isSelected;

  private UrlLink link;

  public UrlLinkDisplay(String rowId, UrlLink rowLink, boolean rowSelected,
          boolean rowDefault)
  {
    id = rowId;
    isPrimary = rowDefault;
    isSelected = rowSelected;

    link = rowLink;
  }

  // getters/setters
  public String getId()
  {
    return id;
  }

  public String getDescription()
  {
    return link.getLabel();
  }

  public String getDBName()
  {
    return link.getTarget();
  }

  public String getUrl()
  {
    return link.getUrlWithToken();
  }

  public boolean getIsPrimary()
  {
    return isPrimary;
  }

  public boolean getIsSelected()
  {
    return isSelected;
  }

  public void setDBName(String name)
  {
    link.setTarget(name);
  }

  public void setUrl(String rowUrl)
  {
    link = new UrlLink(getDescription(), rowUrl, getDBName());
  }

  public void setDescription(String desc)
  {
    link.setLabel(desc);
  }

  public void setIsDefault(boolean rowDefault)
  {
    isPrimary = rowDefault;
  }

  public void setIsSelected(boolean rowSelected)
  {
    isSelected = rowSelected;
  }

  public Object getValue(int index)
  {
    switch (index)
    {
    case ID:
      return id;
    case URL:
      return getUrl();
    case PRIMARY:
      return isPrimary;
    case SELECTED:
      return isSelected;
    case NAME:
      return getDescription();
    case DATABASE:
      return getDBName();
    default:
      return null;
    }
  }

  public void setValue(int index, Object value)
  {
    switch (index)
    {
    case ID:
      id = (String) value;
      break;
    case URL:
      setUrl((String) value);
      break;
    case PRIMARY:
      isPrimary = (boolean) value;
      break;
    case SELECTED:
      isSelected = (boolean) value;
      break;
    case NAME:
      setDescription((String) value);
      // deliberate fall through
    case DATABASE:
      setDBName((String) value);
      break;
    default:
      // do nothing
    }
  }

  /**
   * Identify editable columns
   * 
   * @param index
   *          index of column
   * @return whether column can be edited in table
   */
  public boolean isEditable(int index)
  {
    if (index == PRIMARY)
    {
      // primary link must not be a $DB_ACCESSION$ link
      // so only allow editing if it is not
      return (!link.usesDBAccession());
    }
    else
    {
      return index == SELECTED;
    }
  }

  /**
   * Get list of column names to display in UI
   * 
   * @return column names
   */
  public static List<String> getDisplayColumnNames()
  {
    // Display names between DESCRIPTION and ID (excludes ID)
    return COLNAMES.subList(DATABASE, ID);
  }
}
