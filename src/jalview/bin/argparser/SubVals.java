package jalview.bin.argparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jalview.bin.Console;

/**
 * A helper class to parse a string of the possible forms "content"
 * "[index]content", "[keyName=keyValue]content" and return the integer index,
 * the strings keyName and keyValue, and the content after the square brackets
 * (if present). Values not set `will be -1 or null.
 */
public class SubVals
{
  public static int NOTSET = -1;

  private int index = NOTSET;

  private Map<String, String> subValMap;

  private static char SEPARATOR = ',';

  private static char EQUALS = '=';

  private String content = null;

  protected SubVals(SubVals sv, String c)
  {
    this(sv, c, true);
  }

  protected SubVals(SubVals sv, String c, boolean merge)
  {
    SubVals subvals;
    if (merge)
    {
      SubVals vsv = new SubVals(c);
      if (sv != null && sv.getSubValMap() != null)
      {
        for (String key : sv.getSubValMap().keySet())
        {
          vsv.put(key, sv.get(key));
        }
      }
      if (sv != null && sv.getIndex() > 0)
      {
        vsv.index = sv.getIndex();
      }
      subvals = vsv;
    }
    else
    {
      // replace
      subvals = sv;
    }
    if (subvals == null)
    {
      this.subValMap = new HashMap<>();
    }
    else
    {
      this.subValMap = subvals == null ? new HashMap<>()
              : subvals.getSubValMap();
      this.index = subvals.getIndex();
    }
    this.content = c;
  }

  protected SubVals(String item)
  {
    if (subValMap == null)
      subValMap = new HashMap<>();
    this.parseVals(item);
  }

  public void parseVals(String item)
  {
    if (item == null)
      return;
    if (item.indexOf('[') == 0 && item.indexOf(']') > 1)
    {
      int openBracket = 0;
      int closeBracket = item.indexOf(']');
      String subvalsString = item.substring(openBracket + 1, closeBracket);
      this.content = item.substring(closeBracket + 1);
      boolean setIndex = false;
      for (String subvalString : subvalsString
              .split(Character.toString(SEPARATOR)))
      {
        int equals = subvalString.indexOf(EQUALS);
        if (equals > -1)
        {
          this.put(subvalString.substring(0, equals),
                  subvalString.substring(equals + 1));
        }
        else
        {
          try
          {
            this.index = Integer.parseInt(subvalString);
            setIndex = true;
          } catch (NumberFormatException e)
          {
            // store this non-numeric key as a "true" value
            this.put(subvalString, "true");
          }
        }
      }
      if (!setIndex)
        this.index = NOTSET;
      else
        Console.debug("SubVals from '" + subvalsString + "' has index "
                + this.index + " set");
    }
    else
    {
      this.content = item;
    }
  }

  protected void put(String key, String val)
  {
    subValMap.put(key, val);
  }

  public boolean notSet()
  {
    // notSet is true if content present but nonsensical
    return index == NOTSET && (subValMap == null || subValMap.size() == 0);
  }

  public String getWithSubstitutions(ArgParser ap, String id, String key)
  {
    return ap.makeSubstitutions(subValMap.get(key), id);
  }

  public String get(String key)
  {
    return subValMap.get(key);
  }

  public boolean has(String key)
  {
    return subValMap.containsKey(key);
  }

  public int getIndex()
  {
    return index;
  }

  public String getContent()
  {
    return content;
  }

  protected Map<String, String> getSubValMap()
  {
    return subValMap;
  }

  public String toString()
  {
    if (subValMap == null && getIndex() == NOTSET)
      return "";

    StringBuilder sb = new StringBuilder();
    List<String> entries = new ArrayList<>();
    subValMap.entrySet().stream().forEachOrdered(
            m -> entries.add(m.getValue().equals("true") ? m.getKey()
                    : new StringBuilder().append(m.getKey()).append(EQUALS)
                            .append(m.getValue()).toString()));
    if (getIndex() != NOTSET)
      entries.add(Integer.toString(getIndex()));
    sb.append('[');
    sb.append(String.join(Character.toString(SEPARATOR), entries));
    sb.append(']');
    return sb.toString();
  }
}