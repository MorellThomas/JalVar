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

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.json.simple.parser.ParseException;

public class JSONUtils
{

  /**
   * Converts a JSONArray of values to a string as a comma-separated list.
   * Answers null if the array is null or empty.
   * 
   * @param jsonArray
   * @return
   */
  public static String arrayToStringList(List<Object> jsonArray)
  {
    int n;

    if (jsonArray == null || (n = jsonArray.size()) == 0)
    {
      return null;
    }

    /**
     * BH TODO to Platform?
     * 
     * @j2sNative
     * 
     *            return jsonArray.elementData.slice(0, n).join(",");
     */
    {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < n; i++)
      {
        if (i > 0)
        {
          sb.append(",");
        }
        sb.append(jsonArray.get(i).toString());
      }
      return sb.toString();
    }
  }

  /**
   * The method all JSON parsing must go through for JavaScript.
   * 
   * @param r
   *          a BufferedReader or a javascript.json.JSON.JSONReader
   * @return
   * @throws IOException
   * @throws ParseException
   */
  public static Object parse(Reader r) throws IOException, ParseException
  {
    return Platform.parseJSON(r);
  }

  public static Object parse(String json) throws ParseException
  {
    return Platform.parseJSON(json);
  }

  public static String stringify(Object obj)
  {
    return new org.json.JSONObject(obj).toString();
  }

}
