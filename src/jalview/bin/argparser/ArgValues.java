package jalview.bin.argparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jalview.bin.Console;
import jalview.bin.argparser.Arg.Opt;
import jalview.bin.argparser.Arg.Type;

public class ArgValues
{
  public static final String ID = "id";

  private Arg arg;

  private int argCount = 0;

  private boolean boolValue = false;

  private boolean negated = false;

  private boolean setByWildcard = false;

  private int boolIndex = -1;

  private List<Integer> argsIndexes;

  private List<ArgValue> argValueList;

  private Map<String, ArgValue> idMap = new HashMap<>();

  /*
   * Type type is only really used by --help-type
   */
  private Type type = null;

  protected ArgValues(Arg a)
  {
    this.arg = a;
    this.argValueList = new ArrayList<ArgValue>();
    this.boolValue = arg.getDefaultBoolValue();
  }

  protected boolean setByWildcard()
  {
    return setByWildcard;
  }

  protected void setSetByWildcard(boolean b)
  {
    setByWildcard = b;
  }

  public Arg arg()
  {
    return arg;
  }

  protected void setType(Type t)
  {
    if (this.arg().hasOption(Opt.HASTYPE))
      this.type = t;
  }

  public Type getType()
  {
    return type;
  }

  protected int getCount()
  {
    return argCount;
  }

  protected void incrementCount()
  {
    argCount++;
  }

  protected void setNegated(boolean b, boolean beingSetByWildcard)
  {
    // don't overwrite a wildcard set boolean with a non-wildcard set boolean
    if (boolIndex >= 0 && !this.setByWildcard && beingSetByWildcard)
      return;
    this.negated = b;
  }

  protected boolean isNegated()
  {
    return this.negated;
  }

  protected void setBoolean(Type t, boolean b, int i,
          boolean beingSetByWildcard)
  {
    this.setType(t);
    // don't overwrite a wildcard set boolean with a non-wildcard set boolean
    if (boolIndex >= 0 && !this.setByWildcard && beingSetByWildcard)
      return;
    this.boolValue = b;
    this.boolIndex = i;
    this.setSetByWildcard(beingSetByWildcard);
  }

  protected boolean getBoolean()
  {
    return this.boolValue;
  }

  @Override
  public String toString()
  {
    if (argValueList == null)
      return null;
    StringBuilder sb = new StringBuilder();
    sb.append(arg.toLongString());
    if (arg.hasOption(Opt.BOOLEAN) || arg.hasOption(Opt.UNARY))
      sb.append("Boolean: ").append(boolValue).append("; Default: ")
              .append(arg.getDefaultBoolValue()).append("; Negated: ")
              .append(negated).append("\n");
    if (arg.hasOption(Opt.STRING))
    {
      sb.append("Values:");
      sb.append("'")
              .append(String
                      .join("',\n  '",
                              argValueList.stream().map(av -> av.getValue())
                                      .collect(Collectors.toList())))
              .append("'");
      sb.append("\n");
    }
    sb.append("Count: ").append(argCount).append("\n");
    return sb.toString();
  }

  protected void addValue(Type type, String val, int argIndex,
          boolean wildcard)
  {
    addArgValue(new ArgValue(arg(), type, val, argIndex), wildcard);
  }

  protected void addValue(SubVals sv, Type type, String content,
          int argIndex, boolean wildcard)
  {
    addArgValue(new ArgValue(arg(), sv, type, content, argIndex), wildcard);
  }

  protected void addArgValue(ArgValue av, boolean beingSetByWildcard)
  {
    // allow a non-wildcard value to overwrite a wildcard set single value
    boolean overwrite = !arg.hasOption(Opt.MULTI) && setByWildcard
            && !beingSetByWildcard;
    if ((!arg.hasOption(Opt.MULTI) && argValueList.size() > 0)
            && !overwrite)
      return;
    if (arg.hasOption(Opt.NODUPLICATEVALUES)
            && this.containsValue(av.getValue()))
      return;
    // new or overwrite if single valued
    if (argValueList == null || overwrite)
    {
      argValueList = new ArrayList<ArgValue>();
    }
    SubVals sv = new SubVals(av.getValue());
    if (sv.has(ID))
    {
      String id = sv.get(ID);
      av.setId(id);
      idMap.put(id, av);
    }
    argValueList.add(av);
    this.setSetByWildcard(beingSetByWildcard);
  }

  protected boolean hasValue(String val)
  {
    return argValueList.contains(val);
  }

  protected ArgValue getArgValue()
  {
    if (arg.hasOption(Opt.MULTI))
      Console.warn("Requesting single value for multi value argument");
    return argValueList.size() > 0 ? argValueList.get(0) : null;
  }

  protected List<ArgValue> getArgValueList()
  {
    return argValueList;
  }

  protected boolean hasId(String id)
  {
    return idMap.containsKey(id);
  }

  protected ArgValue getId(String id)
  {
    return idMap.get(id);
  }

  private boolean containsValue(String v)
  {
    if (argValueList == null)
      return false;
    for (ArgValue av : argValueList)
    {
      String val = av.getValue();
      if (v == null && val == null)
        return true;
      if (v == null)
        continue;
      if (v.equals(val))
        return true;
    }
    return false;
  }
}