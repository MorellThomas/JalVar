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
package jalview.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DnaUtils
{

  /**
   * Parses an ENA/GenBank format location specifier and returns a list of
   * [start, end] ranges. Throws an exception if not able to parse.
   * <p>
   * Currently we do not parse "order()" specifiers, or indeterminate ranges of
   * the format "&lt;start..end" or "start..&gt;end" or "start.end" or
   * "start^end"
   * 
   * @param location
   * @return
   * @throws ParseException
   *           if unable to parse the location (the exception message is the
   *           location specifier being parsed); we use ParseException in
   *           preference to the unchecked IllegalArgumentException
   * @see http://www.insdc.org/files/feature_table.html#3.4
   */
  public static List<int[]> parseLocation(String location)
          throws ParseException
  {
    location = location.trim(); // failsafe for untidy input data
    if (location.startsWith("join("))
    {
      return parseJoin(location);
    }
    else if (location.startsWith("complement("))
    {
      return parseComplement(location);
    }
    if (location.startsWith("order("))
    {
      throw new ParseException(location, 0);
    }

    /*
     * try to parse m..n (or simply m)
     */
    String[] range = location.split("\\.\\.");
    if (range.length == 1 || range.length == 2)
    {
      try
      {
        int start = Integer.valueOf(range[0]);
        int end = range.length == 1 ? start : Integer.valueOf(range[1]);
        return Collections.singletonList(new int[] { start, end });
      } catch (NumberFormatException e)
      {
        /*
         * could be a location like <1..888 or 1..>888
         */
        throw new ParseException(location, 0);
      }
    }
    else
    {
      /*
       * could be a location like 102.110 or 123^124
       */
      throw new ParseException(location, 0);
    }
  }

  /**
   * Parses a complement(locationSpec) into a list of start-end ranges
   * 
   * @param location
   * @return
   * @throws ParseException
   */
  static List<int[]> parseComplement(String location) throws ParseException
  {
    /*
     * take what is inside complement()
     */
    if (!location.endsWith(")"))
    {
      throw new ParseException(location, 0);
    }
    String toComplement = location.substring("complement(".length(),
            location.length() - 1);
    List<int[]> ranges = parseLocation(toComplement);

    /*
     * reverse the order and direction of ranges
     */
    Collections.reverse(ranges);
    for (int[] range : ranges)
    {
      int temp = range[0];
      range[0] = range[1];
      range[1] = temp;
    }
    return ranges;
  }

  /**
   * Parses a join(loc1,loc2,...,locn) into a list of start-end ranges
   * 
   * @param location
   * @return
   * @throws ParseException
   */
  static List<int[]> parseJoin(String location) throws ParseException
  {
    List<int[]> ranges = new ArrayList<int[]>();

    /*
     * take what is inside join()
     */
    if (!location.endsWith(")"))
    {
      throw new ParseException(location, 0);
    }
    String joinedLocs = location.substring("join(".length(),
            location.length() - 1);
    String[] locations = joinedLocs.split(",");
    for (String loc : locations)
    {
      List<int[]> range = parseLocation(loc);
      ranges.addAll(range);
    }
    return ranges;
  }

}
