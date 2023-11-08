package jalview.bin.argparser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jalview.bin.argparser.Arg.Opt;
import jalview.util.FileUtils;

/**
 * Helper class to allow easy extraction of information about specific argument
 * values (without having to check for null etc all the time)
 */
public class ArgValuesMap
{
  protected Map<Arg, ArgValues> m;

  private String linkedId;

  protected ArgValuesMap(String linkedId)
  {
    this.linkedId = linkedId;
    this.newMap();
  }

  protected ArgValuesMap(String linkedId, Map<Arg, ArgValues> map)
  {
    this.linkedId = linkedId;
    this.m = map;
  }

  public String getLinkedId()
  {
    return linkedId;
  }

  private Map<Arg, ArgValues> getMap()
  {
    return m;
  }

  private void newMap()
  {
    m = new HashMap<Arg, ArgValues>();
  }

  private void newArg(Arg a)
  {
    if (m == null)
      newMap();
    if (!containsArg(a))
      m.put(a, new ArgValues(a));
  }

  public ArgValues getArgValues(Arg a)
  {
    return m == null ? null : m.get(a);
  }

  public ArgValues getOrCreateArgValues(Arg a)
  {
    ArgValues avs = m.get(a);
    if (avs == null)
      newArg(a);
    return getArgValues(a);
  }

  public List<ArgValue> getArgValueList(Arg a)
  {
    ArgValues avs = getArgValues(a);
    return avs == null ? new ArrayList<>() : avs.getArgValueList();
  }

  public ArgValue getArgValue(Arg a)
  {
    List<ArgValue> vals = getArgValueList(a);
    return (vals == null || vals.size() == 0) ? null : vals.get(0);
  }

  public String getValue(Arg a)
  {
    ArgValue av = getArgValue(a);
    return av == null ? null : av.getValue();
  }

  public boolean containsArg(Arg a)
  {
    if (m == null || !m.containsKey(a))
      return false;
    return a.hasOption(Opt.STRING) ? getArgValue(a) != null : true;
  }

  public boolean hasValue(Arg a, String val)
  {
    if (m == null || !m.containsKey(a))
      return false;
    for (ArgValue av : getArgValueList(a))
    {
      String avVal = av.getValue();
      if ((val == null && avVal == null)
              || (val != null && val.equals(avVal)))
      {
        return true;
      }
    }
    return false;
  }

  public boolean getBoolean(Arg a)
  {
    ArgValues av = getArgValues(a);
    return av == null ? false : av.getBoolean();
  }

  public Set<Arg> getArgKeys()
  {
    return m.keySet();
  }

  public ArgValue getArgValueOfArgWithSubValKey(Arg a, String svKey)
  {
    return getArgValueOfArgWithSubValKey(a, svKey, false);
  }

  public ArgValue getArgValueOfArgWithSubValKey(Arg a, String svKey,
          boolean last)
  {
    ArgValues avs = this.getArgValues(a);
    if (avs == null)
    {
      return null;
    }
    List<ArgValue> compareAvs = avs.getArgValueList();
    for (int i = 0; i < compareAvs.size(); i++)
    {
      int index = last ? compareAvs.size() - 1 - i : i;
      ArgValue av = compareAvs.get(index);
      SubVals sv = av.getSubVals();
      if (sv.has(svKey) && !sv.get(svKey).equals("false"))
      {
        return av;
      }
    }
    return null;
  }

  public ArgValue getClosestPreviousArgValueOfArg(ArgValue thisAv, Arg a)
  {
    ArgValue closestAv = null;
    int thisArgIndex = thisAv.getArgIndex();
    ArgValues compareAvs = this.getArgValues(a);
    int closestPreviousIndex = -1;
    for (ArgValue av : compareAvs.getArgValueList())
    {
      int argIndex = av.getArgIndex();
      if (argIndex < thisArgIndex && argIndex > closestPreviousIndex)
      {
        closestPreviousIndex = argIndex;
        closestAv = av;
      }
    }
    return closestAv;
  }

  public ArgValue getClosestNextArgValueOfArg(ArgValue thisAv, Arg a)
  {
    // this looks for the *next* arg that *might* be referring back to
    // a thisAv. Such an arg would have no subValues (if it does it should
    // specify an id in the subValues so wouldn't need to be guessed).
    ArgValue closestAv = null;
    int thisArgIndex = thisAv.getArgIndex();
    if (!containsArg(a))
      return null;
    ArgValues compareAvs = this.getArgValues(a);
    int closestNextIndex = Integer.MAX_VALUE;
    for (ArgValue av : compareAvs.getArgValueList())
    {
      int argIndex = av.getArgIndex();
      if (argIndex > thisArgIndex && argIndex < closestNextIndex)
      {
        closestNextIndex = argIndex;
        closestAv = av;
      }
    }
    return closestAv;
  }

  // TODO this is incomplete and currently unused (fortunately)
  public ArgValue[] getArgValuesReferringTo(String key, String value, Arg a)
  {
    // this looks for the *next* arg that *might* be referring back to
    // a thisAv. Such an arg would have no subValues (if it does it should
    // specify an id in the subValues so wouldn't need to be guessed).
    List<ArgValue> avList = new ArrayList<>();
    Arg[] args = a == null ? (Arg[]) this.getMap().keySet().toArray()
            : new Arg[]
            { a };
    for (Arg keyArg : args)
    {
      for (ArgValue av : this.getArgValueList(keyArg))
      {

      }
    }
    return (ArgValue[]) avList.toArray();
  }

  public boolean hasId(Arg a, String id)
  {
    ArgValues avs = this.getArgValues(a);
    return avs == null ? false : avs.hasId(id);
  }

  public ArgValue getId(Arg a, String id)
  {
    ArgValues avs = this.getArgValues(a);
    return avs == null ? null : avs.getId(id);
  }

  /*
   * This method returns the basename of the first --append or --open value. 
   * Used primarily for substitutions in output filenames.
   */
  public String getBasename()
  {
    return getDirBasenameOrExtension(false, false);
  }

  /*
   * This method returns the basename of the first --append or --open value. 
   * Used primarily for substitutions in output filenames.
   */
  public String getExtension()
  {
    return getDirBasenameOrExtension(false, true);
  }

  /*
   * This method returns the dirname of the first --append or --open value. 
   * Used primarily for substitutions in output filenames.
   */
  public String getDirname()
  {
    return getDirBasenameOrExtension(true, false);
  }

  public String getDirBasenameOrExtension(boolean dirname,
          boolean extension)
  {
    String filename = null;
    String appendVal = getValue(Arg.APPEND);
    String openVal = getValue(Arg.OPEN);
    if (appendVal != null)
      filename = appendVal;
    if (filename == null && openVal != null)
      filename = openVal;
    if (filename == null)
      return null;

    File file = new File(filename);
    if (dirname)
    {
      return FileUtils.getDirname(file);
    }
    return extension ? FileUtils.getExtension(file)
            : FileUtils.getBasename(file);
  }

  /*
   * Checks if there is an Arg with Opt
   */
  public boolean hasArgWithOption(Opt o)
  {
    for (Arg a : getArgKeys())
    {
      if (a.hasOption(o))
        return true;
    }
    return false;
  }
}
