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
package jalview.util.matcher;

import jalview.util.MessageManager;

/**
 * An enumeration for binary conditions that a user might choose from when
 * setting filter or match conditions for values
 */
public enum Condition
{
  Contains(false, true, "Contains"),
  NotContains(false, true, "NotContains"), Matches(false, true, "Matches"),
  NotMatches(false, true, "NotMatches"), Present(false, false, "Present"),
  NotPresent(false, false, "NotPresent"), EQ(true, true, "EQ"),
  NE(true, true, "NE"), LT(true, true, "LT"), LE(true, true, "LE"),
  GT(true, true, "GT"), GE(true, true, "GE");

  private boolean numeric;

  private boolean needsAPattern;

  /*
   * value used to save a Condition to the 
   * Jalview project file or restore it from project; 
   * it should not be changed even if enum names change in future
   */
  private String stableName;

  /**
   * Answers the enum value whose 'stable name' matches the argument (not case
   * sensitive), or null if no match
   * 
   * @param stableName
   * @return
   */
  public static Condition fromString(String stableName)
  {
    for (Condition c : values())
    {
      if (c.stableName.equalsIgnoreCase(stableName))
      {
        return c;
      }
    }
    return null;
  }

  /**
   * Constructor
   * 
   * @param isNumeric
   * @param needsPattern
   * @param stablename
   */
  Condition(boolean isNumeric, boolean needsPattern, String stablename)
  {
    numeric = isNumeric;
    needsAPattern = needsPattern;
    stableName = stablename;
  }

  /**
   * Answers true if the condition does a numerical comparison, else false
   * (string comparison)
   * 
   * @return
   */
  public boolean isNumeric()
  {
    return numeric;
  }

  /**
   * Answers true if the condition requires a pattern to compare against, else
   * false
   * 
   * @return
   */
  public boolean needsAPattern()
  {
    return needsAPattern;
  }

  public String getStableName()
  {
    return stableName;
  }

  /**
   * Answers a display name for the match condition, suitable for showing in
   * drop-down menus. The value may be internationalized using the resource key
   * "label.matchCondition_" with the enum name appended.
   * 
   * @return
   */
  @Override
  public String toString()
  {
    return MessageManager.getStringOrReturn("label.matchCondition_",
            name());
  }
}
