package jalview.bin.argparser;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jalview.bin.argparser.Arg.Opt;
import jalview.bin.argparser.Arg.Type;
import jalview.util.FileUtils;

public class BootstrapArgs
{
  // only need one
  private Map<Arg, List<Map.Entry<Type, String>>> bootstrapArgMap = new HashMap<>();

  private Set<File> argFiles = new HashSet<>();

  private Set<Opt> argsOptions = new HashSet<>();

  private Set<Type> argsTypes = new HashSet<>();

  public static BootstrapArgs getBootstrapArgs(String[] args)
  {
    List<String> argList = new ArrayList<>(Arrays.asList(args));
    return new BootstrapArgs(argList);
  }

  public static BootstrapArgs getBootstrapArgs(List<String> args)
  {
    return new BootstrapArgs(args);
  }

  private BootstrapArgs(List<String> args)
  {
    parse(args, null);
  }

  private void parse(List<String> args, File inArgFile)
  {
    if (args == null)
      return;
    // avoid looping argFiles
    if (inArgFile != null)
    {
      if (argFiles.contains(inArgFile))
      {
        System.err.println(
                "Looped argfiles detected: '" + inArgFile.getPath() + "'");
        return;
      }
      argFiles.add(inArgFile);
    }

    for (int i = 0; i < args.size(); i++)
    {
      String arg = args.get(i);
      // look for double-dash, e.g. --arg
      if (arg.startsWith(ArgParser.DOUBLEDASH))
      {
        String argName = null;
        String val = null;
        Type type = null;
        // remove "--"
        argName = arg.substring(ArgParser.DOUBLEDASH.length());

        // look for equals e.g. --arg=value
        int equalPos = argName.indexOf(ArgParser.EQUALS);
        if (equalPos > -1)
        {
          val = argName.substring(equalPos + 1);
          argName = argName.substring(0, equalPos);
        }

        // check for boolean prepended by "no"
        if (argName.startsWith(ArgParser.NEGATESTRING)
                && ArgParser.argMap.containsKey(
                        argName.substring(ArgParser.NEGATESTRING.length())))
        {
          val = "false";
          argName = argName.substring(ArgParser.NEGATESTRING.length());
        }

        // look for type modification e.g. --help-opening
        int dashPos = argName.indexOf(ArgParser.SINGLEDASH);
        if (dashPos > -1)
        {
          String potentialArgName = argName.substring(0, dashPos);
          Arg potentialArg = ArgParser.argMap.get(potentialArgName);
          if (potentialArg != null && potentialArg.hasOption(Opt.HASTYPE))
          {
            String typeName = argName.substring(dashPos + 1);
            try
            {
              type = Type.valueOf(typeName.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e)
            {
              type = Type.INVALID;
            }
            argName = argName.substring(0, dashPos);
          }
        }

        // after all other args, look for Opt.PREFIX args if still not found
        if (!ArgParser.argMap.containsKey(argName))
        {
          for (Arg potentialArg : EnumSet.allOf(Arg.class))
          {
            if (potentialArg.hasOption(Opt.PREFIXKEV) && argName != null
                    && argName.startsWith(potentialArg.getName())
                    && val != null)
            {
              val = argName.substring(potentialArg.getName().length())
                      + ArgParser.EQUALS + val;
              argName = argName.substring(0,
                      potentialArg.getName().length());
              break;
            }
          }
        }

        if (ArgParser.argMap.containsKey(argName) && val == null)
        {
          val = "true";
        }

        Arg a = ArgParser.argMap.get(argName);

        if (a != null)
        {
          for (Opt opt : a.getOptions())
          {
            if (!argsOptions.contains(opt))
            {
              argsOptions.add(opt);
            }
          }
          Type t = a.getType();
          if (!argsTypes.contains(t))
          {
            argsTypes.add(t);
          }
        }

        if (a == null || !a.hasOption(Opt.BOOTSTRAP))
        {
          // not a valid bootstrap arg
          continue;
        }

        if (a.hasOption(Opt.STRING))
        {
          List<String> vals = null;
          if (equalPos == -1)
          {
            vals = ArgParser.getShellGlobbedFilenameValues(a, args, i + 1);
          }
          else
          {
            if (a.hasOption(Opt.GLOB))
            {
              vals = FileUtils.getFilenamesFromGlob(val);
            }
            else
            {
              vals = new ArrayList<>();
              vals.add(val);
            }
          }
          addAll(a, type, vals);

          if (a == Arg.ARGFILE)
          {
            for (String filename : vals)
            {
              File argFile = new File(filename);
              parse(ArgParser.readArgFile(argFile), argFile);
            }
          }
        }
        else
        {
          add(a, type, val);
        }
      }
    }
  }

  public boolean contains(Arg a)
  {
    return bootstrapArgMap.containsKey(a);
  }

  public boolean containsType(Type t)
  {
    for (List<Map.Entry<Type, String>> l : bootstrapArgMap.values())
    {
      for (Map.Entry<Type, String> e : l)
      {
        if (e.getKey() == t)
          return true;
      }
    }
    return false;
  }

  public List<Arg> getArgsOfType(Type t)
  {
    return getArgsOfType(t, new Opt[] {});
  }

  public List<Arg> getArgsOfType(Type t, Opt... opts)
  {
    List<Arg> args = new ArrayList<>();
    for (Arg a : bootstrapArgMap.keySet())
    {
      if (!a.hasAllOptions(opts))
        continue;

      List<Map.Entry<Type, String>> l = bootstrapArgMap.get(a);
      if (l.stream().anyMatch(e -> e.getKey() == t))
      {
        args.add(a);
      }
    }
    return args;
  }

  public List<Map.Entry<Type, String>> getList(Arg a)
  {
    return bootstrapArgMap.get(a);
  }

  public List<String> getValueList(Arg a)
  {
    return bootstrapArgMap.get(a).stream().map(e -> e.getValue())
            .collect(Collectors.toList());
  }

  private List<Map.Entry<Type, String>> getOrCreateList(Arg a)
  {
    List<Map.Entry<Type, String>> l = getList(a);
    if (l == null)
    {
      l = new ArrayList<>();
      putList(a, l);
    }
    return l;
  }

  private void putList(Arg a, List<Map.Entry<Type, String>> l)
  {
    bootstrapArgMap.put(a, l);
  }

  /*
   * Creates a new list if not used before,
   * adds the value unless the existing list is non-empty
   * and the arg is not MULTI (so first expressed value is
   * retained).
   */
  private void add(Arg a, Type t, String s)
  {
    List<Map.Entry<Type, String>> l = getOrCreateList(a);
    if (a.hasOption(Opt.MULTI) || l.size() == 0)
    {
      l.add(entry(t, s));
    }
  }

  private void addAll(Arg a, Type t, List<String> al)
  {
    List<Map.Entry<Type, String>> l = getOrCreateList(a);
    if (a.hasOption(Opt.MULTI))
    {
      for (String s : al)
      {
        l.add(entry(t, s));
      }
    }
    else if (l.size() == 0 && al.size() > 0)
    {
      l.add(entry(t, al.get(0)));
    }
  }

  private static Map.Entry<Type, String> entry(Type t, String s)
  {
    return new AbstractMap.SimpleEntry<Type, String>(t, s);
  }

  /*
   * Retrieves the first value even if MULTI.
   * A convenience for non-MULTI args.
   */
  public String getValue(Arg a)
  {
    if (!bootstrapArgMap.containsKey(a))
      return null;
    List<Map.Entry<Type, String>> aL = bootstrapArgMap.get(a);
    return (aL == null || aL.size() == 0) ? null : aL.get(0).getValue();
  }

  public boolean getBoolean(Arg a, boolean d)
  {
    if (!bootstrapArgMap.containsKey(a))
      return d;
    return Boolean.parseBoolean(getValue(a));
  }

  public boolean getBoolean(Arg a)
  {
    if (!(a.hasOption(Opt.BOOLEAN) || a.hasOption(Opt.UNARY)))
    {
      return false;
    }
    if (bootstrapArgMap.containsKey(a))
    {
      return Boolean.parseBoolean(getValue(a));
    }
    else
    {
      return a.getDefaultBoolValue();
    }
  }

  public boolean argsHaveOption(Opt opt)
  {
    return argsOptions.contains(opt);
  }

  public boolean argsHaveType(Type type)
  {
    return argsTypes.contains(type);
  }

  public boolean isHeadless()
  {
    boolean isHeadless = false;
    if (this.argsHaveType(Type.HELP))
    {
      // --help, --help-all, ... specified => headless
      isHeadless = true;
    }
    else if (this.contains(Arg.VERSION))
    {
      // --version specified => headless
      isHeadless = true;
    }
    else if (this.contains(Arg.GUI))
    {
      // --gui specified => forced NOT headless
      isHeadless = !this.getBoolean(Arg.GUI);
    }
    else if (this.contains(Arg.HEADLESS))
    {
      // --headless, --noheadless specified => use value
      isHeadless = this.getBoolean(Arg.HEADLESS);
    }
    else if (this.argsHaveOption(Opt.OUTPUTFILE))
    {
      // --output file.fa, --image pic.png, --structureimage struct.png =>
      // assume headless unless above has been already specified
      isHeadless = true;
    }
    return isHeadless;
  }
}
