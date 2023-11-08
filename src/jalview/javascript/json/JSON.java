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
package jalview.javascript.json;

import java.io.BufferedReader;
import java.io.InputStream;
import java.net.URL;

/**
 * 
 * A rudimentary JSON converter/iterator that uses the browser's native AJAX
 * json data type delivery mechanism.
 * 
 * Arrays are delivered as ArrayList<Object> where Object may be Boolean,
 * String, Long, Double, ArrayList, and "Map-like object".
 * 
 * For speed, the maps returned are just JavaScript maps with a few added
 * methods for extracting data. [get(), contains(), probably should add keySet,
 * valueSet, and entrySet].
 * 
 * @author hansonr Bob Hanson St. Olaf College 1/24/2019
 *
 */
public class JSON
{

  /**
   * A simple encoding of sequential key/value pairs for a jQuery.ajax call. If
   * the first key is "url" and the second is an object, then the ajax object is
   * attached to that url as well, just for transport purposes within the
   * system.
   * 
   * @param keyValues
   *          assumed to be simple String,Object pairs. String objects will be
   *          surrounded by double quotes.
   */
  @SuppressWarnings("static-access")
  public static Object setAjax(Object... keyValues)
  {
    return /** @j2sNative swingjs.JSUtil.setAjax$OA(keyValues) || */
    null;
  }

  public static void setAjax(URL url)
  {
    /** @j2sNative swingjs.JSUtil.setAjax$java_net_URL(url); */
  }

  public static BufferedReader getJSONReader(InputStream is)
  {
    return /** @j2sNative swingjs.JSUtil.getJSONReader$O(is) || */
    null;
  }

  /**
   * 
   * @param obj
   *          as String, Reader, InputStream, or JavaScript Object or Array
   * @return Map or List
   */
  public static Object parse(Object obj)
  {
    return /** @j2sNative swingjs.JSUtil.parseJSON$O(obj) || */
    null;
  }

  public static String stringify(Object obj)
  {
    return /** @j2sNative swingjs.JSUtil.stringifyJSON$O(obj) || */
    null;
  }

}
