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
package jalview.bin;

import java.net.URLDecoder;
import java.util.Vector;

/**
 * Notes: this argParser does not distinguish between parameter switches,
 * parameter values and argument text. If an argument happens to be identical to
 * a parameter, it will be taken as such (even though it didn't have a '-'
 * prefixing it).
 * 
 * @author Andrew Waterhouse and JBP documented.
 * 
 */
public class ArgsParser
{
  Vector<String> vargs = null;

  public ArgsParser(String[] args)
  {
    vargs = new Vector<String>();
    for (int i = 0; i < args.length; i++)
    {
      String arg = args[i].trim();
      if (arg.length() > 0 && arg.charAt(0) == '-')
      {
        arg = arg.substring(1);
      }
      vargs.addElement(arg);
    }
  }

  /**
   * check for and remove first occurence of arg+parameter in arglist.
   * 
   * @param arg
   * @return return the argument following the given arg if arg was in list.
   */
  public String getValue(String arg)
  {
    return getValue(arg, false);
  }

  public String getValue(String arg, boolean utf8decode)
  {
    int index = vargs.indexOf(arg);
    String dc = null, ret = null;
    if (index != -1)
    {
      ret = vargs.elementAt(index + 1).toString();
      vargs.removeElementAt(index);
      vargs.removeElementAt(index);
      if (utf8decode && ret != null)
      {
        try
        {
          dc = URLDecoder.decode(ret, "UTF-8");
          ret = dc;
        } catch (Exception e)
        {
          // TODO: log failure to decode
        }
      }
    }
    return ret;
  }

  /**
   * check for and remove first occurence of arg in arglist.
   * 
   * @param arg
   * @return true if arg was present in argslist.
   */
  public boolean contains(String arg)
  {
    if (vargs.contains(arg))
    {
      vargs.removeElement(arg);
      return true;
    }
    else
    {
      return false;
    }
  }

  public String nextValue()
  {
    return vargs.remove(0);
  }

  public int getSize()
  {
    return vargs.size();
  }

}
