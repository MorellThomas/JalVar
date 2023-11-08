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
package jalview.bin.argparser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.bin.Jalview;
import jalview.bin.argparser.Arg.Opt;
import jalview.bin.argparser.Arg.Type;
import jalview.util.FileUtils;
import jalview.util.HttpUtils;

public class ArgParser
{
  protected static final String SINGLEDASH = "-";

  protected static final String DOUBLEDASH = "--";

  public static final char EQUALS = '=';

  protected static final String NEGATESTRING = "no";

  /**
   * the default linked id prefix used for no id (ie when not even square braces
   * are provided)
   */
  protected static final String DEFAULTLINKEDIDPREFIX = "JALVIEW:";

  /**
   * the linkedId string used to match all linkedIds seen so far
   */
  protected static final String MATCHALLLINKEDIDS = "*";

  /**
   * the linkedId string used to match all of the last --open'ed linkedIds
   */
  protected static final String MATCHOPENEDLINKEDIDS = "open*";

  /**
   * the counter added to the default linked id prefix
   */
  private int defaultLinkedIdCounter = 0;

  /**
   * the substitution string used to use the defaultLinkedIdCounter
   */
  private static final String DEFAULTLINKEDIDCOUNTER = "{}";

  /**
   * the linked id prefix used for --open files. NOW the same as DEFAULT
   */
  protected static final String OPENLINKEDIDPREFIX = DEFAULTLINKEDIDPREFIX;

  /**
   * the counter used for {n} substitutions
   */
  private int linkedIdAutoCounter = 0;

  /**
   * the linked id substitution string used to increment the idCounter (and use
   * the incremented value)
   */
  private static final String INCREMENTLINKEDIDAUTOCOUNTER = "{++n}";

  /**
   * the linked id substitution string used to use the idCounter
   */
  private static final String LINKEDIDAUTOCOUNTER = "{n}";

  /**
   * the linked id substitution string used to use the filename extension of
   * --append or --open
   */
  private static final String LINKEDIDEXTENSION = "{extension}";

  /**
   * the linked id substitution string used to use the base filename of --append
   */
  /** or --open */
  private static final String LINKEDIDBASENAME = "{basename}";

  /**
   * the linked id substitution string used to use the dir path of --append or
   * --open
   */
  private static final String LINKEDIDDIRNAME = "{dirname}";

  /**
   * the current argfile
   */
  private String argFile = null;

  /**
   * the linked id substitution string used to use the dir path of the latest
   */
  /** --argfile name */
  private static final String ARGFILEBASENAME = "{argfilebasename}";

  /**
   * the linked id substitution string used to use the dir path of the latest
   * --argfile name
   */
  private static final String ARGFILEDIRNAME = "{argfiledirname}";

  /**
   * flag to say whether {n} subtitutions in output filenames should be made.
   * Turn on and off with --substitutions and --nosubstitutions Start with it on
   */
  private boolean substitutions = true;

  /**
   * flag to say whether the default linkedId is the current default linked id
   *
   * or ALL linkedIds
   */
  private boolean allLinkedIds = false;

  /**
   * flag to say whether the default linkedId is the current default linked id
   * or OPENED linkedIds
   */
  private boolean openedLinkedIds = false;

  /**
   * flag to say whether the structure arguments should be applied to all
   * structures with this linked id
   */
  private boolean allStructures = false;

  protected static final Map<String, Arg> argMap;

  protected Map<String, ArgValuesMap> linkedArgs = new HashMap<>();

  protected List<String> linkedOrder = new ArrayList<>();

  protected List<String> storedLinkedIds = new ArrayList<>();

  protected List<Arg> argList = new ArrayList<>();

  private static final char ARGFILECOMMENT = '#';

  private int argIndex = 0;

  private BootstrapArgs bootstrapArgs = null;

  static
  {
    argMap = new HashMap<>();
    for (Arg a : EnumSet.allOf(Arg.class))
    {
      for (String argName : a.getNames())
      {
        if (argMap.containsKey(argName))
        {
          Console.warn("Trying to add argument name multiple times: '"
                  + argName + "'");
          if (argMap.get(argName) != a)
          {
            Console.error(
                    "Trying to add argument name multiple times for different Args: '"
                            + argMap.get(argName).getName() + ":" + argName
                            + "' and '" + a.getName() + ":" + argName
                            + "'");
          }
          continue;
        }
        argMap.put(argName, a);
      }
    }
  }

  public ArgParser(String[] args)
  {
    this(args, false, null);
  }

  public ArgParser(String[] args, boolean initsubstitutions,
          BootstrapArgs bsa)
  {
    // Make a mutable new ArrayList so that shell globbing parser works.
    // (When shell file globbing is used, there are a sequence of non-Arg
    // arguments (which are the expanded globbed filenames) that need to be
    // consumed by the --append/--argfile/etc Arg which is most easily done by
    // removing these filenames from the list one at a time. This can't be done
    // with an ArrayList made with only Arrays.asList(String[] args). )
    this(new ArrayList<>(Arrays.asList(args)), initsubstitutions, false,
            bsa);
  }

  public ArgParser(List<String> args, boolean initsubstitutions)
  {
    this(args, initsubstitutions, false, null);
  }

  public ArgParser(List<String> args, boolean initsubstitutions,
          boolean allowPrivate, BootstrapArgs bsa)
  {
    // do nothing if there are no "--" args and (some "-" args || >0 arg is
    // "open")
    boolean d = false;
    boolean dd = false;
    for (String arg : args)
    {
      if (arg.startsWith(DOUBLEDASH))
      {
        dd = true;
        break;
      }
      else if (arg.startsWith("-") || arg.equals("open"))
      {
        d = true;
      }
    }
    if (d && !dd)
    {
      // leave it to the old style -- parse an empty list
      parse(new ArrayList<String>(), false, false);
      return;
    }
    if (bsa != null)
      this.bootstrapArgs = bsa;
    else
      this.bootstrapArgs = BootstrapArgs.getBootstrapArgs(args);
    parse(args, initsubstitutions, allowPrivate);
  }

  private void parse(List<String> args, boolean initsubstitutions,
          boolean allowPrivate)
  {
    this.substitutions = initsubstitutions;
    boolean openEachInitialFilenames = true;
    for (int i = 0; i < args.size(); i++)
    {
      String arg = args.get(i);

      // If the first arguments do not start with "--" or "-" or is not "open"
      // and` is a filename that exists it is probably a file/list of files to
      // open so we fake an Arg.OPEN argument and when adding files only add the
      // single arg[i] and increment the defaultLinkedIdCounter so that each of
      // these files is opened separately.
      if (openEachInitialFilenames && !arg.startsWith(DOUBLEDASH)
              && !arg.startsWith("-") && !arg.equals("open")
              && (new File(arg).exists()
                      || HttpUtils.startsWithHttpOrHttps(arg)))
      {
        arg = Arg.OPEN.argString();
      }
      else
      {
        openEachInitialFilenames = false;
      }

      // look for double-dash, e.g. --arg
      if (arg.startsWith(DOUBLEDASH))
      {
        String argName = null;
        String val = null;
        List<String> globVals = null; // for Opt.GLOB only
        SubVals globSubVals = null; // also for use by Opt.GLOB only
        String linkedId = null;
        Type type = null;

        // look for equals e.g. --arg=value
        int equalPos = arg.indexOf(EQUALS);
        if (equalPos > -1)
        {
          argName = arg.substring(DOUBLEDASH.length(), equalPos);
          val = arg.substring(equalPos + 1);
        }
        else
        {
          argName = arg.substring(DOUBLEDASH.length());
        }

        // look for linked ID e.g. --arg[linkedID]
        int idOpen = argName.indexOf('[');
        int idClose = argName.indexOf(']');
        if (idOpen > -1 && idClose == argName.length() - 1)
        {
          linkedId = argName.substring(idOpen + 1, idClose);
          argName = argName.substring(0, idOpen);
        }

        // look for type modification e.g. --help-opening
        int dashPos = argName.indexOf(SINGLEDASH);
        if (dashPos > -1)
        {
          String potentialArgName = argName.substring(0, dashPos);
          Arg potentialArg = argMap.get(potentialArgName);
          if (potentialArg != null && potentialArg.hasOption(Opt.HASTYPE))
          {
            String typeName = argName.substring(dashPos + 1);
            try
            {
              type = Type.valueOf(typeName);
            } catch (IllegalArgumentException e)
            {
              type = Type.INVALID;
            }
            argName = argName.substring(0, dashPos);
          }
        }

        Arg a = argMap.get(argName);
        // check for boolean prepended by "no" e.g. --nowrap
        boolean negated = false;
        if (a == null)
        {
          if (argName.startsWith(NEGATESTRING) && argMap
                  .containsKey(argName.substring(NEGATESTRING.length())))
          {
            argName = argName.substring(NEGATESTRING.length());
            a = argMap.get(argName);
            negated = true;
          }
          else
          {
            // after all other args, look for Opt.PREFIXKEV args if still not
            // found
            for (Arg potentialArg : EnumSet.allOf(Arg.class))
            {
              if (potentialArg.hasOption(Opt.PREFIXKEV) && argName != null
                      && argName.startsWith(potentialArg.getName())
                      && equalPos > -1)
              {
                val = argName.substring(potentialArg.getName().length())
                        + EQUALS + val;
                argName = argName.substring(0,
                        potentialArg.getName().length());
                a = potentialArg;
                break;
              }
            }
          }
        }

        // check for config errors
        if (a == null)
        {
          // arg not found
          Console.error("Argument '" + arg + "' not recognised.  Exiting.");
          Jalview.exit("Invalid argument used." + System.lineSeparator()
                  + "Use" + System.lineSeparator() + "jalview "
                  + Arg.HELP.argString() + System.lineSeparator()
                  + "for a usage statement.", 13);
          continue;
        }
        if (a.hasOption(Opt.PRIVATE) && !allowPrivate)
        {
          Console.error(
                  "Argument '" + a.argString() + "' is private. Ignoring.");
          continue;
        }
        if (!a.hasOption(Opt.BOOLEAN) && negated)
        {
          // used "no" with a non-boolean option
          Console.error("Argument '" + DOUBLEDASH + NEGATESTRING + argName
                  + "' not a boolean option. Ignoring.");
          continue;
        }
        if (!a.hasOption(Opt.STRING) && equalPos > -1)
        {
          // set --argname=value when arg does not accept values
          Console.error("Argument '" + a.argString()
                  + "' does not expect a value (given as '" + arg
                  + "').  Ignoring.");
          continue;
        }
        if (!a.hasOption(Opt.LINKED) && linkedId != null)
        {
          // set --argname[linkedId] when arg does not use linkedIds
          Console.error("Argument '" + a.argString()
                  + "' does not expect a linked id (given as '" + arg
                  + "'). Ignoring.");
          continue;
        }

        // String value(s)
        if (a.hasOption(Opt.STRING))
        {
          if (equalPos >= 0)
          {
            if (a.hasOption(Opt.GLOB))
            {
              // strip off and save the SubVals to be added individually later
              globSubVals = new SubVals(val);
              // make substitutions before looking for files
              String fileGlob = makeSubstitutions(globSubVals.getContent(),
                      linkedId);
              globVals = FileUtils.getFilenamesFromGlob(fileGlob);
            }
            else
            {
              // val is already set -- will be saved in the ArgValue later in
              // the normal way
            }
          }
          else
          {
            // There is no "=" so value is next arg or args (possibly shell
            // glob-expanded)
            if ((openEachInitialFilenames ? i : i + 1) >= args.size())
            {
              // no value to take for arg, which wants a value
              Console.error("Argument '" + a.getName()
                      + "' requires a value, none given. Ignoring.");
              continue;
            }
            // deal with bash globs here (--arg val* is expanded before reaching
            // the JVM). Note that SubVals cannot be used in this case.
            // If using the --arg=val then the glob is preserved and Java globs
            // will be used later. SubVals can be used.
            if (a.hasOption(Opt.GLOB))
            {
              // if this is the first argument with a file list at the start of
              // the args we add filenames from index i instead of i+1
              globVals = getShellGlobbedFilenameValues(a, args,
                      openEachInitialFilenames ? i : i + 1);
            }
            else
            {
              val = args.get(i + 1);
            }
          }
        }

        // make NOACTION adjustments
        // default and auto counter increments
        if (a == Arg.NPP)
        {
          linkedIdAutoCounter++;
        }
        else if (a == Arg.SUBSTITUTIONS)
        {
          substitutions = !negated;
        }
        else if (a == Arg.SETARGFILE)
        {
          argFile = val;
        }
        else if (a == Arg.UNSETARGFILE)
        {
          argFile = null;
        }
        else if (a == Arg.ALL)
        {
          allLinkedIds = !negated;
          openedLinkedIds = false;
        }
        else if (a == Arg.OPENED)
        {
          openedLinkedIds = !negated;
          allLinkedIds = false;
        }
        else if (a == Arg.ALLSTRUCTURES)
        {
          allStructures = !negated;
        }

        if (a.hasOption(Opt.STORED))
        {
          // reset the lastOpenedLinkedIds list
          this.storedLinkedIds = new ArrayList<>();
        }

        // this is probably only Arg.NEW and Arg.OPEN
        if (a.hasOption(Opt.INCREMENTDEFAULTCOUNTER))
        {
          // use the next default prefixed OPENLINKEDID
          defaultLinkedId(true);
        }

        String autoCounterString = null;
        String defaultLinkedId = defaultLinkedId(false);
        boolean usingDefaultLinkedId = false;
        if (a.hasOption(Opt.LINKED))
        {
          if (linkedId == null)
          {
            if (a.hasOption(Opt.OUTPUTFILE) && a.hasOption(Opt.ALLOWALL)
                    && val.startsWith(MATCHALLLINKEDIDS))
            {
              // --output=*.ext is shorthand for --all --output {basename}.ext
              // (or --image=*.ext)
              allLinkedIds = true;
              openedLinkedIds = false;
              linkedId = MATCHALLLINKEDIDS;
              val = LINKEDIDDIRNAME + File.separator + LINKEDIDBASENAME
                      + val.substring(MATCHALLLINKEDIDS.length());
            }
            else if (a.hasOption(Opt.OUTPUTFILE)
                    && a.hasOption(Opt.ALLOWALL)
                    && val.startsWith(MATCHOPENEDLINKEDIDS))
            {
              // --output=open*.ext is shorthand for --opened --output
              // {basename}.ext
              // (or --image=open*.ext)
              openedLinkedIds = true;
              allLinkedIds = false;
              linkedId = MATCHOPENEDLINKEDIDS;
              val = LINKEDIDDIRNAME + File.separator + LINKEDIDBASENAME
                      + val.substring(MATCHOPENEDLINKEDIDS.length());
            }
            else if (allLinkedIds && a.hasOption(Opt.ALLOWALL))
            {
              linkedId = MATCHALLLINKEDIDS;
            }
            else if (openedLinkedIds && a.hasOption(Opt.ALLOWALL))
            {
              linkedId = MATCHOPENEDLINKEDIDS;
            }
            else
            {
              // use default linkedId for linked arguments
              linkedId = defaultLinkedId;
              usingDefaultLinkedId = true;
              Console.debug("Changing linkedId to '" + linkedId + "' from "
                      + arg);
            }
          }
          else
          {
            if (linkedId.contains(LINKEDIDAUTOCOUNTER))
            {
              // turn {n} to the autoCounter
              autoCounterString = Integer.toString(linkedIdAutoCounter);
              linkedId = linkedId.replace(LINKEDIDAUTOCOUNTER,
                      autoCounterString);
              Console.debug("Changing linkedId to '" + linkedId + "' from "
                      + arg);
            }
            if (linkedId.contains(INCREMENTLINKEDIDAUTOCOUNTER))
            {
              // turn {++n} to the incremented autoCounter
              autoCounterString = Integer.toString(++linkedIdAutoCounter);
              linkedId = linkedId.replace(INCREMENTLINKEDIDAUTOCOUNTER,
                      autoCounterString);
              Console.debug("Changing linkedId to '" + linkedId + "' from "
                      + arg);
            }
          }
        }

        // do not continue in this block for NOACTION args
        if (a.hasOption(Opt.NOACTION))
          continue;

        ArgValuesMap avm = getOrCreateLinkedArgValuesMap(linkedId);

        // not dealing with both NODUPLICATEVALUES and GLOB
        if (a.hasOption(Opt.NODUPLICATEVALUES) && avm.hasValue(a, val))
        {
          Console.error("Argument '" + a.argString()
                  + "' cannot contain a duplicate value ('" + val
                  + "'). Ignoring this and subsequent occurrences.");
          continue;
        }

        // check for unique id
        SubVals subvals = new SubVals(val);
        boolean addNewSubVals = false;
        String id = subvals.get(ArgValues.ID);
        if (id != null && avm.hasId(a, id))
        {
          Console.error("Argument '" + a.argString()
                  + "' has a duplicate id ('" + id + "'). Ignoring.");
          continue;
        }

        // set allstructures to all non-primary structure options in this linked
        // id if --allstructures has been set
        if (allStructures
                && (a.getType() == Type.STRUCTURE
                        || a.getType() == Type.STRUCTUREIMAGE)
                && !a.hasOption(Opt.PRIMARY))
        {
          if (!subvals.has(Arg.ALLSTRUCTURES.getName()))
          // && !subvals.has("structureid"))
          {
            subvals.put(Arg.ALLSTRUCTURES.getName(), "true");
            addNewSubVals = true;
          }
        }

        ArgValues avs = avm.getOrCreateArgValues(a);

        // store appropriate String value(s)
        if (a.hasOption(Opt.STRING))
        {
          if (a.hasOption(Opt.GLOB) && globVals != null
                  && globVals.size() > 0)
          {
            Enumeration<String> gve = Collections.enumeration(globVals);
            while (gve.hasMoreElements())
            {
              String v = gve.nextElement();
              SubVals vsv = new SubVals(globSubVals, v);
              addValue(linkedId, type, avs, vsv, v, argIndex++, true);
              // if we're using defaultLinkedId and the arg increments the
              // counter:
              if (gve.hasMoreElements() && usingDefaultLinkedId
                      && a.hasOption(Opt.INCREMENTDEFAULTCOUNTER))
              {
                // increment the default linkedId
                linkedId = defaultLinkedId(true);
                // get new avm and avs
                avm = linkedArgs.get(linkedId);
                avs = avm.getOrCreateArgValues(a);
              }
            }
          }
          else
          {
            // addValue(linkedId, type, avs, val, argIndex, true);
            addValue(linkedId, type, avs, addNewSubVals ? subvals : null,
                    val, argIndex, true);
          }
        }
        else if (a.hasOption(Opt.BOOLEAN))
        {
          setBoolean(linkedId, type, avs, !negated, argIndex);
          setNegated(linkedId, avs, negated);
        }
        else if (a.hasOption(Opt.UNARY))
        {
          setBoolean(linkedId, type, avs, true, argIndex);
        }

        // remove the '*' or 'open*' linkedId that should be empty if it was
        // created
        if ((MATCHALLLINKEDIDS.equals(linkedId)
                && linkedArgs.containsKey(linkedId))
                || (MATCHOPENEDLINKEDIDS.equals(linkedId)
                        && linkedArgs.containsKey(linkedId)))
        {
          linkedArgs.remove(linkedId);
        }
      }
    }
  }

  private void finaliseStoringArgValue(String linkedId, ArgValues avs)
  {
    Arg a = avs.arg();
    incrementCount(linkedId, avs);
    argIndex++;

    // store in appropriate place
    if (a.hasOption(Opt.LINKED))
    {
      // store the order of linkedIds
      if (!linkedOrder.contains(linkedId))
        linkedOrder.add(linkedId);
    }

    // store arg in the list of args used
    if (!argList.contains(a))
      argList.add(a);
  }

  private String defaultLinkedId(boolean increment)
  {
    String defaultLinkedId = new StringBuilder(DEFAULTLINKEDIDPREFIX)
            .append(Integer.toString(defaultLinkedIdCounter)).toString();
    if (increment)
    {
      while (linkedArgs.containsKey(defaultLinkedId))
      {
        defaultLinkedIdCounter++;
        defaultLinkedId = new StringBuilder(DEFAULTLINKEDIDPREFIX)
                .append(Integer.toString(defaultLinkedIdCounter))
                .toString();
      }
    }
    getOrCreateLinkedArgValuesMap(defaultLinkedId);
    return defaultLinkedId;
  }

  public String makeSubstitutions(String val, String linkedId)
  {
    if (!this.substitutions || val == null)
      return val;

    String subvals;
    String rest;
    if (val.indexOf('[') == 0 && val.indexOf(']') > 1)
    {
      int closeBracket = val.indexOf(']');
      if (val.length() == closeBracket)
        return val;
      subvals = val.substring(0, closeBracket + 1);
      rest = val.substring(closeBracket + 1);
    }
    else
    {
      subvals = "";
      rest = val;
    }
    if (rest.contains(LINKEDIDAUTOCOUNTER))
      rest = rest.replace(LINKEDIDAUTOCOUNTER,
              String.valueOf(linkedIdAutoCounter));
    if (rest.contains(INCREMENTLINKEDIDAUTOCOUNTER))
      rest = rest.replace(INCREMENTLINKEDIDAUTOCOUNTER,
              String.valueOf(++linkedIdAutoCounter));
    if (rest.contains(DEFAULTLINKEDIDCOUNTER))
      rest = rest.replace(DEFAULTLINKEDIDCOUNTER,
              String.valueOf(defaultLinkedIdCounter));
    ArgValuesMap avm = linkedArgs.get(linkedId);
    if (avm != null)
    {
      if (rest.contains(LINKEDIDBASENAME))
      {
        rest = rest.replace(LINKEDIDBASENAME, avm.getBasename());
      }
      if (rest.contains(LINKEDIDEXTENSION))
      {
        rest = rest.replace(LINKEDIDEXTENSION, avm.getExtension());
      }
      if (rest.contains(LINKEDIDDIRNAME))
      {
        rest = rest.replace(LINKEDIDDIRNAME, avm.getDirname());
      }
    }
    if (argFile != null)
    {
      if (rest.contains(ARGFILEBASENAME))
      {
        rest = rest.replace(ARGFILEBASENAME,
                FileUtils.getBasename(new File(argFile)));
      }
      if (rest.contains(ARGFILEDIRNAME))
      {
        rest = rest.replace(ARGFILEDIRNAME,
                FileUtils.getDirname(new File(argFile)));
      }
    }

    return new StringBuilder(subvals).append(rest).toString();
  }

  /*
   * A helper method to take a list of String args where we're expecting
   * {"--previousargs", "--arg", "file1", "file2", "file3", "--otheroptionsornot"}
   * and the index of the globbed arg, here 1. It returns a List<String> {"file1",
   * "file2", "file3"} *and remove these from the original list object* so that
   * processing can continue from where it has left off, e.g. args has become
   * {"--previousargs", "--arg", "--otheroptionsornot"} so the next increment
   * carries on from the next --arg if available.
   */
  protected static List<String> getShellGlobbedFilenameValues(Arg a,
          List<String> args, int i)
  {
    List<String> vals = new ArrayList<>();
    while (i < args.size() && !args.get(i).startsWith(DOUBLEDASH))
    {
      vals.add(FileUtils.substituteHomeDir(args.remove(i)));
      if (!a.hasOption(Opt.GLOB))
        break;
    }
    return vals;
  }

  public BootstrapArgs getBootstrapArgs()
  {
    return bootstrapArgs;
  }

  public boolean isSet(Arg a)
  {
    return a.hasOption(Opt.LINKED) ? isSetAtAll(a) : isSet(null, a);
  }

  public boolean isSetAtAll(Arg a)
  {
    for (String linkedId : linkedOrder)
    {
      if (isSet(linkedId, a))
        return true;
    }
    return false;
  }

  public boolean isSet(String linkedId, Arg a)
  {
    ArgValuesMap avm = linkedArgs.get(linkedId);
    return avm == null ? false : avm.containsArg(a);
  }

  public boolean getBoolean(Arg a)
  {
    if (!a.hasOption(Opt.BOOLEAN) && !a.hasOption(Opt.UNARY))
    {
      Console.warn("Getting boolean from non boolean Arg '" + a.getName()
              + "'.");
    }
    return a.hasOption(Opt.LINKED) ? getBool("", a) : getBool(null, a);
  }

  public boolean getBool(String linkedId, Arg a)
  {
    ArgValuesMap avm = linkedArgs.get(linkedId);
    if (avm == null)
      return a.getDefaultBoolValue();
    ArgValues avs = avm.getArgValues(a);
    return avs == null ? a.getDefaultBoolValue() : avs.getBoolean();
  }

  public List<String> getLinkedIds()
  {
    return linkedOrder;
  }

  public ArgValuesMap getLinkedArgs(String id)
  {
    return linkedArgs.get(id);
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("UNLINKED\n");
    sb.append(argValuesMapToString(linkedArgs.get(null)));
    if (getLinkedIds() != null)
    {
      sb.append("LINKED\n");
      for (String id : getLinkedIds())
      {
        // already listed these as UNLINKED args
        if (id == null)
          continue;

        ArgValuesMap avm = getLinkedArgs(id);
        sb.append("ID: '").append(id).append("'\n");
        sb.append(argValuesMapToString(avm));
      }
    }
    return sb.toString();
  }

  private static String argValuesMapToString(ArgValuesMap avm)
  {
    if (avm == null)
      return null;
    StringBuilder sb = new StringBuilder();
    for (Arg a : avm.getArgKeys())
    {
      ArgValues v = avm.getArgValues(a);
      sb.append(v.toString());
      sb.append("\n");
    }
    return sb.toString();
  }

  public static ArgParser parseArgFiles(List<String> argFilenameGlobs,
          boolean initsubstitutions, BootstrapArgs bsa)
  {
    List<File> argFiles = new ArrayList<>();

    for (String pattern : argFilenameGlobs)
    {
      // I don't think we want to dedup files, making life easier
      argFiles.addAll(FileUtils.getFilesFromGlob(pattern));
    }

    return parseArgFileList(argFiles, initsubstitutions, bsa);
  }

  public static ArgParser parseArgFileList(List<File> argFiles,
          boolean initsubstitutions, BootstrapArgs bsa)
  {
    List<String> argsList = new ArrayList<>();
    for (File argFile : argFiles)
    {
      if (!argFile.exists())
      {
        String message = Arg.ARGFILE.argString() + EQUALS + "\""
                + argFile.getPath() + "\": File does not exist.";
        Jalview.exit(message, 2);
      }
      try
      {
        String setargfile = new StringBuilder(Arg.SETARGFILE.argString())
                .append(EQUALS).append(argFile.getCanonicalPath())
                .toString();
        argsList.add(setargfile);
        argsList.addAll(readArgFile(argFile));
        argsList.add(Arg.UNSETARGFILE.argString());
      } catch (IOException e)
      {
        String message = Arg.ARGFILE.argString() + "=\"" + argFile.getPath()
                + "\": File could not be read.";
        Jalview.exit(message, 3);
      }
    }
    // Third param "true" uses Opt.PRIVATE args --setargile=argfile and
    // --unsetargfile
    return new ArgParser(argsList, initsubstitutions, true, bsa);
  }

  protected static List<String> readArgFile(File argFile)
  {
    List<String> args = new ArrayList<>();
    if (argFile != null && argFile.exists())
    {
      try
      {
        for (String line : Files.readAllLines(Paths.get(argFile.getPath())))
        {
          if (line != null && line.length() > 0
                  && line.charAt(0) != ARGFILECOMMENT)
            args.add(line);
        }
      } catch (IOException e)
      {
        String message = Arg.ARGFILE.argString() + "=\"" + argFile.getPath()
                + "\": File could not be read.";
        Console.debug(message, e);
        Jalview.exit(message, 3);
      }
    }
    return args;
  }

  public static enum Position
  {
    FIRST, BEFORE, AFTER
  }

  /**
   * get from following Arg of type a or subval of same name (lowercase)
   */
  public static String getValueFromSubValOrArg(ArgValuesMap avm,
          ArgValue av, Arg a, SubVals sv)
  {
    return getFromSubValArgOrPref(avm, av, a, sv, null, null, null);
  }

  /**
   * get from following Arg of type a or subval key or preference pref or
   * default def
   */
  public static String getFromSubValArgOrPref(ArgValuesMap avm, ArgValue av,
          Arg a, SubVals sv, String key, String pref, String def)
  {
    return getFromSubValArgOrPref(avm, a, Position.AFTER, av, sv, key, pref,
            def);
  }

  /**
   * get from following(AFTER), first occurence of (FIRST) or previous (BEFORE)
   * Arg of type a or subval key or preference pref or default def
   */
  public static String getFromSubValArgOrPref(ArgValuesMap avm, Arg a,
          Position pos, ArgValue av, SubVals sv, String key, String pref,
          String def)
  {
    return getFromSubValArgOrPrefWithSubstitutions(null, avm, a, pos, av,
            sv, key, pref, def);
  }

  public static String getFromSubValArgOrPrefWithSubstitutions(ArgParser ap,
          ArgValuesMap avm, Arg a, Position pos, ArgValue av, SubVals sv,
          String key, String pref, String def)
  {
    if (key == null)
      key = a.getName();
    String value = null;
    if (sv != null && sv.has(key) && sv.get(key) != null)
    {
      value = ap == null ? sv.get(key)
              : sv.getWithSubstitutions(ap, avm.getLinkedId(), key);
    }
    else if (avm != null && avm.containsArg(a))
    {
      if (pos == Position.FIRST && avm.getValue(a) != null)
        value = avm.getValue(a);
      else if (pos == Position.BEFORE
              && avm.getClosestPreviousArgValueOfArg(av, a) != null)
        value = avm.getClosestPreviousArgValueOfArg(av, a).getValue();
      else if (pos == Position.AFTER
              && avm.getClosestNextArgValueOfArg(av, a) != null)
        value = avm.getClosestNextArgValueOfArg(av, a).getValue();

      // look for allstructures subval for Type.STRUCTURE*
      Arg arg = av.getArg();
      if (value == null && arg.hasOption(Opt.PRIMARY)
              && arg.getType() == Type.STRUCTURE
              && !a.hasOption(Opt.PRIMARY) && (a.getType() == Type.STRUCTURE
                      || a.getType() == Type.STRUCTUREIMAGE))
      {
        ArgValue av2 = avm.getArgValueOfArgWithSubValKey(a,
                Arg.ALLSTRUCTURES.getName());
        if (av2 != null)
        {
          value = av2.getValue();
        }
      }
    }
    if (value == null)
    {
      value = pref != null ? Cache.getDefault(pref, def) : def;
    }
    return value;
  }

  public static boolean getBoolFromSubValOrArg(ArgValuesMap avm, Arg a,
          SubVals sv)
  {
    return getFromSubValArgOrPref(avm, a, sv, null, null, false);
  }

  public static boolean getFromSubValArgOrPref(ArgValuesMap avm, Arg a,
          SubVals sv, String key, String pref, boolean def)
  {
    return getFromSubValArgOrPref(avm, a, sv, key, pref, def, false);
  }

  public static boolean getFromSubValArgOrPref(ArgValuesMap avm, Arg a,
          SubVals sv, String key, String pref, boolean def,
          boolean invertPref)
  {
    if ((key == null && a == null) || (sv == null && a == null))
      return false;

    boolean usingArgKey = false;
    if (key == null)
    {
      key = a.getName();
      usingArgKey = true;
    }

    String nokey = ArgParser.NEGATESTRING + key;

    // look for key or nokey in subvals first (if using Arg check options)
    if (sv != null)
    {
      // check for true boolean
      if (sv.has(key) && sv.get(key) != null)
      {
        if (usingArgKey)
        {
          if (!(a.hasOption(Opt.BOOLEAN) || a.hasOption(Opt.UNARY)))
          {
            Console.debug(
                    "Looking for boolean in subval from non-boolean/non-unary Arg "
                            + a.getName());
            return false;
          }
        }
        return sv.get(key).toLowerCase(Locale.ROOT).equals("true");
      }

      // check for negative boolean (subval "no..." will be "true")
      if (sv.has(nokey) && sv.get(nokey) != null)
      {
        if (usingArgKey)
        {
          if (!(a.hasOption(Opt.BOOLEAN)))
          {
            Console.debug(
                    "Looking for negative boolean in subval from non-boolean Arg "
                            + a.getName());
            return false;
          }
        }
        return !sv.get(nokey).toLowerCase(Locale.ROOT).equals("true");
      }
    }

    // check argvalues
    if (avm != null && avm.containsArg(a))
      return avm.getBoolean(a);

    // return preference or default
    boolean prefVal = pref != null ? Cache.getDefault(pref, def) : false;
    return pref != null ? (invertPref ? !prefVal : prefVal) : def;
  }

  // the following methods look for the "*" linkedId and add the argvalue to all
  // linkedId ArgValues if it does.
  /**
   * This version inserts the subvals sv into all created values
   */
  private void addValue(String linkedId, Type type, ArgValues avs,
          SubVals sv, String v, int argIndex, boolean doSubs)
  {
    this.argValueOperation(Op.ADDVALUE, linkedId, type, avs, sv, v, false,
            argIndex, doSubs);
  }

  private void addValue(String linkedId, Type type, ArgValues avs, String v,
          int argIndex, boolean doSubs)
  {
    this.argValueOperation(Op.ADDVALUE, linkedId, type, avs, null, v, false,
            argIndex, doSubs);
  }

  private void setBoolean(String linkedId, Type type, ArgValues avs,
          boolean b, int argIndex)
  {
    this.argValueOperation(Op.SETBOOLEAN, linkedId, type, avs, null, null,
            b, argIndex, false);
  }

  private void setNegated(String linkedId, ArgValues avs, boolean b)
  {
    this.argValueOperation(Op.SETNEGATED, linkedId, null, avs, null, null,
            b, 0, false);
  }

  private void incrementCount(String linkedId, ArgValues avs)
  {
    this.argValueOperation(Op.INCREMENTCOUNT, linkedId, null, avs, null,
            null, false, 0, false);
  }

  private enum Op
  {
    ADDVALUE, SETBOOLEAN, SETNEGATED, INCREMENTCOUNT
  }

  private void argValueOperation(Op op, String linkedId, Type type,
          ArgValues avs, SubVals sv, String v, boolean b, int argIndex,
          boolean doSubs)
  {
    // default to merge subvals if subvals are provided
    argValueOperation(op, linkedId, type, avs, sv, true, v, b, argIndex,
            doSubs);
  }

  /**
   * The following operations look for the "*" and "open*" linkedIds and add the
   * argvalue to all appropriate linkedId ArgValues if it does. If subvals are
   * supplied, they are inserted into all new set values.
   * 
   * @param op
   *          The ArgParser.Op operation
   * @param linkedId
   *          The String linkedId from the ArgValuesMap
   * @param type
   *          The Arg.Type to attach to this ArgValue
   * @param avs
   *          The ArgValues for this linkedId
   * @param sv
   *          Use these SubVals on the ArgValue
   * @param merge
   *          Merge the SubVals with any existing on the value. False will
   *          replace unless sv is null
   * @param v
   *          The value of the ArgValue (may contain subvals).
   * @param b
   *          The boolean value of the ArgValue.
   * @param argIndex
   *          The argIndex for the ArgValue.
   * @param doSubs
   *          Whether to perform substitutions on the subvals and value.
   */
  private void argValueOperation(Op op, String linkedId, Type type,
          ArgValues avs, SubVals sv, boolean merge, String v, boolean b,
          int argIndex, boolean doSubs)
  {
    Arg a = avs.arg();

    List<String> wildcardLinkedIds = null;
    if (a.hasOption(Opt.ALLOWALL))
    {
      switch (linkedId)
      {
      case MATCHALLLINKEDIDS:
        wildcardLinkedIds = getLinkedIds();
        break;
      case MATCHOPENEDLINKEDIDS:
        wildcardLinkedIds = this.storedLinkedIds;
        break;
      }
    }

    // if we're not a wildcard linkedId and the arg is marked to be stored, add
    // to storedLinkedIds
    if (linkedId != null && wildcardLinkedIds == null
            && a.hasOption(Opt.STORED)
            && !storedLinkedIds.contains(linkedId))
    {
      storedLinkedIds.add(linkedId);
    }

    // if we are a wildcard linkedId, apply the arg and value to all appropriate
    // linkedIds
    if (wildcardLinkedIds != null)
    {
      for (String id : wildcardLinkedIds)
      {
        // skip incorrectly stored wildcard ids!
        if (id == null || MATCHALLLINKEDIDS.equals(id)
                || MATCHOPENEDLINKEDIDS.equals(id))
          continue;
        ArgValuesMap avm = linkedArgs.get(id);
        // don't set an output if there isn't an input
        if (a.hasOption(Opt.REQUIREINPUT)
                && !avm.hasArgWithOption(Opt.INPUT))
          continue;

        ArgValues tavs = avm.getOrCreateArgValues(a);
        switch (op)
        {

        case ADDVALUE:
          String val = v;
          if (sv != null)
          {
            if (doSubs)
            {
              sv = new SubVals(sv, val, merge);
              val = makeSubstitutions(sv.getContent(), id);
            }
            tavs.addValue(sv, type, val, argIndex, true);
          }
          else
          {
            if (doSubs)
            {
              val = makeSubstitutions(v, id);
            }
            tavs.addValue(type, val, argIndex, true);
          }
          finaliseStoringArgValue(id, tavs);
          break;

        case SETBOOLEAN:
          tavs.setBoolean(type, b, argIndex, true);
          finaliseStoringArgValue(id, tavs);
          break;

        case SETNEGATED:
          tavs.setNegated(b, true);
          break;

        case INCREMENTCOUNT:
          tavs.incrementCount();
          break;

        default:
          break;

        }

      }
    }
    else // no wildcard linkedId -- do it simpler
    {
      switch (op)
      {
      case ADDVALUE:
        String val = v;
        if (sv != null)
        {
          if (doSubs)
          {
            val = makeSubstitutions(v, linkedId);
            sv = new SubVals(sv, val);
          }
          avs.addValue(sv, type, val, argIndex, false);
        }
        else
        {
          if (doSubs)
          {
            val = makeSubstitutions(v, linkedId);
          }
          avs.addValue(type, val, argIndex, false);
        }
        finaliseStoringArgValue(linkedId, avs);
        break;

      case SETBOOLEAN:
        avs.setBoolean(type, b, argIndex, false);
        finaliseStoringArgValue(linkedId, avs);
        break;

      case SETNEGATED:
        avs.setNegated(b, false);
        break;

      case INCREMENTCOUNT:
        avs.incrementCount();
        break;

      default:
        break;
      }
    }
  }

  private ArgValuesMap getOrCreateLinkedArgValuesMap(String linkedId)
  {
    if (linkedArgs.containsKey(linkedId)
            && linkedArgs.get(linkedId) != null)
      return linkedArgs.get(linkedId);

    linkedArgs.put(linkedId, new ArgValuesMap(linkedId));
    return linkedArgs.get(linkedId);
  }

}