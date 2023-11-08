package jalview.bin.argparser;

import jalview.bin.argparser.Arg.Opt;
import jalview.bin.argparser.Arg.Type;

/**
 * A helper class to keep an index of argument position with argument values
 */
public class ArgValue implements Comparable<ArgValue>
{
  private Arg arg;

  private int argIndex;

  private String value;

  /*
   * Type type is only really used by --help-type
   */
  private Type type = null;

  /*
   * This id is set by a subVal id= to identify the product of this ArgValue
   * later. Set but not currently used.
   */
  private String id;

  private SubVals subVals;

  protected ArgValue(Arg a, SubVals sv, Type type, String content,
          int argIndex)
  {
    this.arg = a;
    this.value = content;
    this.argIndex = argIndex;
    this.subVals = sv == null ? new SubVals("") : sv;
    this.setType(type);
  }

  protected ArgValue(Arg a, Type type, String value, int argIndex)
  {
    this.arg = a;
    this.argIndex = argIndex;
    this.subVals = new SubVals(value);
    this.value = getSubVals().getContent();
    this.setType(type);
  }

  protected void setType(Type t)
  {
    if (this.getArg().hasOption(Opt.HASTYPE))
      this.type = t;
  }

  public Type getType()
  {
    return type;
  }

  public Arg getArg()
  {
    return arg;
  }

  public String getValue()
  {
    return value;
  }

  public int getArgIndex()
  {
    return argIndex;
  }

  protected void setId(String i)
  {
    id = i;
  }

  public String getId()
  {
    return id;
  }

  public SubVals getSubVals()
  {
    return subVals;
  }

  public String getSubVal(String key)
  {
    if (subVals == null || !subVals.has(key))
      return null;
    return subVals.get(key);
  }

  protected void putSubVal(String key, String val)
  {
    this.subVals.put(key, val);
  }

  @Override
  public final int compareTo(ArgValue o)
  {
    return this.getArgIndex() - o.getArgIndex();
  }
}