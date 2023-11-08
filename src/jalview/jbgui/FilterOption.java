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
package jalview.jbgui;

import jalview.gui.structurechooser.StructureChooserQuerySource;
import jalview.gui.structurechooser.ThreeDBStructureChooserQuerySource;

/**
 * This inner class provides the data model for the structure filter combo-box
 * 
 * @author tcnofoegbu
 *
 */
public class FilterOption
{
  private String name;

  private String value;

  private String view;

  private boolean addSeparatorAfter;

  private StructureChooserQuerySource querySource;

  /**
   * Model for structure filter option
   * 
   * @param name
   *          - the name of the Option
   * @param value
   *          - the value of the option
   * @param view
   *          - the category of the filter option
   * @param addSeparatorAfter
   *          - if true, a horizontal separator is rendered immediately after
   *          this filter option, otherwise
   * @param structureChooserQuerySource
   *          - the query source that actions this filter
   */
  public FilterOption(String name, String value, String view,
          boolean addSeparatorAfter,
          StructureChooserQuerySource structureChooserQuerySource)
  {
    this.name = name;
    this.value = value;
    this.view = view;
    this.querySource = structureChooserQuerySource;
    this.addSeparatorAfter = addSeparatorAfter;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public String getView()
  {
    return view;
  }

  public void setView(String view)
  {
    this.view = view;
  }

  @Override
  public String toString()
  {
    return this.name;
  }

  public boolean isAddSeparatorAfter()
  {
    return addSeparatorAfter;
  }

  public void setAddSeparatorAfter(boolean addSeparatorAfter)
  {
    this.addSeparatorAfter = addSeparatorAfter;
  }

  public StructureChooserQuerySource getQuerySource()
  {
    return querySource;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof FilterOption)
    {
      FilterOption o = (FilterOption) obj;
      return o.name.equals(name) && o.querySource == querySource
              && o.value.equals(value) && o.view == view;
    }
    else
    {
      return super.equals(obj);
    }
  }

  @Override
  public int hashCode()
  {
    return ("" + name + ":" + value).hashCode()
            + (view != null ? view.hashCode() : 0)
            + (querySource != null ? querySource.hashCode() : 0);
  }
}