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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.testng.annotations.Test;

public class JSONUtilsTest
{
  @Test(groups = "Functional")
  public void testArrayToList() throws JSONException
  {
    assertNull(JSONUtils.arrayToStringList(null));

    JSONArray ja = new JSONArray();
    assertNull(JSONUtils.arrayToStringList(null));

    ja.add("hello");
    assertEquals(JSONUtils.arrayToStringList(ja), "hello");

    ja.add("world");
    assertEquals(JSONUtils.arrayToStringList(ja), "hello,world");
  }
}
