/*

  private static String ADJUSTMENT_MESSAGE = null;
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

import java.util.Locale;

/**
 * Methods to decide on appropriate memory setting for Jalview based on two
 * optionally provided values: jvmmempc - the maximum percentage of total
 * physical memory to allocate, and jvmmemmax - the maximum absolute amount of
 * physical memory to allocate. These can be provided as arguments or system
 * properties. Other considerations such as minimum application requirements and
 * leaving space for OS are used too.
 * 
 * @author bsoares
 *
 */
public class MemorySetting
{
  public static final String MAX_HEAPSIZE_PERCENT_PROPERTY_NAME = "jvmmempc";

  public static final String MAX_HEAPSIZE_PROPERTY_NAME = "jvmmemmax";

  private static final int MAX_HEAPSIZE_PERCENT_DEFAULT = 90; // 90%

  private static final long GIGABYTE = 1073741824; // 1GB

  public static final long LEAVE_FREE_MIN_MEMORY = GIGABYTE / 2;

  public static final long APPLICATION_MIN_MEMORY = GIGABYTE / 2;

  private static final long MAX_HEAPSIZE_GB_DEFAULT = 32;

  private static final long NOMEM_MAX_HEAPSIZE_GB_DEFAULT = 8;

  public static final String NS = "MEMORY";

  public static final String CUSTOMISED_SETTINGS = NS
          + "_CUSTOMISED_SETTINGS";

  public static final String MEMORY_JVMMEMPC = NS + "_"
          + MAX_HEAPSIZE_PERCENT_PROPERTY_NAME.toUpperCase(Locale.ROOT);

  public static final String MEMORY_JVMMEMMAX = NS + "_"
          + MAX_HEAPSIZE_PROPERTY_NAME.toUpperCase(Locale.ROOT);

  protected static boolean logToClassChecked = false;

  public static String memorySuffixes = "bkmgt"; // order of the suffixes is
                                                 // important!

  public static long getMemorySetting()
  {
    return getMemorySetting(null, null);
  }

  public static long getMemorySetting(String jvmmemmaxarg,
          String jvmmempcarg)
  {
    return getMemorySetting(jvmmemmaxarg, jvmmempcarg, true, false);
  }

  /**
   * Decide on appropriate memory setting for Jalview based on the two arguments
   * values: jvmmempc - the maximum percentage of total physical memory to
   * allocate, and jvmmemmax - the maximum absolute amount of physical memory to
   * allocate. These can be provided as arguments. If not provided as arguments
   * (or set as null) system properties will be used instead (if set). The
   * memory setting returned will be the lower of the two values. If either of
   * the values are not provided then defaults will be used (jvmmempc=90,
   * jvmmemmax=32GB). If total physical memory can't be ascertained when
   * jvmmempc was set or neither jvmmempc nor jvmmemmax were set, then jvmmemmax
   * defaults to a much safer 8GB. In this case explicitly setting jvmmemmax and
   * not setting jvmmempc can set a higher memory for Jalview. The calculation
   * also tries to ensure 0.5GB memory for the OS, but also tries to ensure at
   * least 0.5GB memory for Jalview (which takes priority over the OS) If there
   * is less then 0.5GB of physical memory then the total physical memory is
   * used for Jalview.
   * 
   * @param jvmmemmaxarg
   *          Maximum value of memory to set. This can be a numeric string
   *          optionally followed by "b", "k", "m", "g", "t" (case insensitive)
   *          to indicate bytes, kilobytes, megabytes, gigabytes, terabytes
   *          respectively. If null a default value of 32G will be used. If null
   *          and either physical memory can't be determined then the default is
   *          8GB.
   * @param jvmmempcarg
   *          Max percentage of physical memory to use. Defaults to "90".
   * 
   * @param useProps
   *          boolean to decide whether to look at System properties.
   * 
   * @return The amount of memory (in bytes) to allocate to Jalview
   */
  public static long getMemorySetting(String jvmmemmaxarg,
          String jvmmempcarg, boolean useProps, boolean quiet)
  {
    // actual Xmx value-to-be
    long maxMemLong = -1;
    clearAdjustmentMessage();

    // (absolute) jvmmaxmem setting, start with default
    long memmax = MAX_HEAPSIZE_GB_DEFAULT * GIGABYTE;
    if (jvmmemmaxarg == null && useProps)
    {
      jvmmemmaxarg = System.getProperty(MAX_HEAPSIZE_PROPERTY_NAME);
    }
    String jvmmemmax = jvmmemmaxarg;
    if (jvmmemmax != null && jvmmemmax.length() > 0)
    {
      // parse the arg
      try
      {
        memmax = memoryStringToLong(jvmmemmax);
        if (memmax == 0)
        {
          throw (new NumberFormatException("Not allowing 0"));
        }
      } catch (NumberFormatException e)
      {
        memmax = MAX_HEAPSIZE_GB_DEFAULT * GIGABYTE;
        setAdjustmentMessage("MemorySetting Property '"
                + MAX_HEAPSIZE_PROPERTY_NAME + "' (" + jvmmemmaxarg
                + "') badly formatted or 0, using default ("
                + MAX_HEAPSIZE_GB_DEFAULT + "g).", quiet);
      }

      // check at least minimum value (this accounts for negatives too)
      if (memmax < APPLICATION_MIN_MEMORY)
      {
        memmax = APPLICATION_MIN_MEMORY;
        setAdjustmentMessage("MemorySetting Property '"
                + MAX_HEAPSIZE_PROPERTY_NAME + "' (" + jvmmemmaxarg
                + ") too small, using minimum (" + APPLICATION_MIN_MEMORY
                + ").", quiet);
      }

    }
    else
    {
      // no need to warn if no setting
      // adjustmentMessage("MemorySetting Property '" + maxHeapSizeProperty
      // + "' not
      // set.");
    }

    // get max percent of physical memory, starting with default
    float percent = MAX_HEAPSIZE_PERCENT_DEFAULT;
    if (jvmmempcarg == null && useProps)
    {
      jvmmempcarg = System.getProperty(MAX_HEAPSIZE_PERCENT_PROPERTY_NAME);
    }
    String jvmmempc = jvmmempcarg;
    long mempc = -1;
    try
    {
      if (jvmmempc != null)
      {
        int trypercent = Integer.parseInt(jvmmempc);
        if (0 <= trypercent && trypercent <= 100)
        {
          percent = trypercent;
        }
        else
        {
          setAdjustmentMessage("MemorySetting Property '"
                  + MAX_HEAPSIZE_PERCENT_PROPERTY_NAME
                  + "' should be in range 0..100. Using default " + percent
                  + "%", quiet);
        }
      }
    } catch (NumberFormatException e)
    {
      setAdjustmentMessage("MemorySetting property '"
              + MAX_HEAPSIZE_PERCENT_PROPERTY_NAME + "' (" + jvmmempcarg
              + ") badly formatted", quiet);
    }

    // catch everything in case of no com.sun.management.OperatingSystemMXBean
    boolean memoryPercentError = false;
    try
    {
      long physicalMem = GetMemory.getPhysicalMemory();
      if (physicalMem > APPLICATION_MIN_MEMORY)
      {
        // try and set at least applicationMinMemory and thereafter ensure
        // leaveFreeMinMemory is left for the OS

        mempc = (long) ((physicalMem / 100F) * percent);

        // check for memory left for OS
        boolean reducedmempc = false;
        if (physicalMem - mempc < LEAVE_FREE_MIN_MEMORY)
        {
          mempc = physicalMem - LEAVE_FREE_MIN_MEMORY;
          reducedmempc = true;
          setAdjustmentMessage("MemorySetting Property '"
                  + MAX_HEAPSIZE_PERCENT_PROPERTY_NAME + "' (" + jvmmempcarg
                  + ") too large. Leaving free space for OS and reducing to ("
                  + mempc + ").", quiet);
        }

        // check for minimum application memsize
        if (mempc < APPLICATION_MIN_MEMORY)
        {
          if (reducedmempc)
          {
            setAdjustmentMessage("Reduced MemorySetting (" + mempc
                    + ") too small. Increasing to application minimum ("
                    + APPLICATION_MIN_MEMORY + ").", quiet);
          }
          else
          {
            setAdjustmentMessage("MemorySetting Property '"
                    + MAX_HEAPSIZE_PERCENT_PROPERTY_NAME + "' ("
                    + jvmmempcarg + ") too small. Using minimum ("
                    + APPLICATION_MIN_MEMORY + ").", quiet);
          }
          mempc = APPLICATION_MIN_MEMORY;
        }
      }
      else
      {
        // not enough memory for application, just try and grab what we can!
        mempc = physicalMem;
        setAdjustmentMessage(
                "Not enough physical memory for application. Ignoring MemorySetting Property '"
                        + MAX_HEAPSIZE_PERCENT_PROPERTY_NAME + "' ("
                        + jvmmempcarg
                        + "). Using maximum memory available ("
                        + physicalMem + ").",
                quiet);
      }

    } catch (Throwable t)
    {
      memoryPercentError = true;
      setAdjustmentMessage(
              "Problem calling GetMemory.getPhysicalMemory(). Likely to be problem with com.sun.management.OperatingSystemMXBean",
              quiet);
      t.printStackTrace();
    }

    // In the case of an error reading the percentage of physical memory (when
    // jvmmempc was set OR neither jvmmempc nor jvmmemmax were set), let's cap
    // maxMemLong to 8GB
    if (memoryPercentError && mempc == -1
            && !(jvmmempcarg == null && jvmmemmaxarg != null) // the same as
                                                              // (jvmmempcarg !=
                                                              // null ||
                                                              // (jvmmempcarg ==
                                                              // null &&
                                                              // jvmmemmaxarg
                                                              // == null))
            && memmax > NOMEM_MAX_HEAPSIZE_GB_DEFAULT * GIGABYTE)
    {
      setAdjustmentMessage(
              "Capping maximum memory to " + NOMEM_MAX_HEAPSIZE_GB_DEFAULT
                      + "g due to failure to read physical memory size.",
              quiet);
      memmax = NOMEM_MAX_HEAPSIZE_GB_DEFAULT * GIGABYTE;
    }

    if (mempc == -1) // percentage memory not set
    {
      maxMemLong = memmax;
    }
    else
    {
      maxMemLong = Math.min(mempc, memmax);
    }

    return maxMemLong;
  }

  public static boolean isValidMemoryString(String text)
  {
    if (text.length() > 0)
    {
      char lastChar = text.charAt(text.length() - 1);
      char[] otherChars = text.substring(0, text.length() - 1)
              .toCharArray();
      for (char c : otherChars)
      {
        if (c < '0' || c > '9')
        {
          return false;
        }
      }
      if ((lastChar < '0' || lastChar > '9') && memorySuffixes
              .indexOf(Character.toLowerCase(lastChar)) == -1)
      {
        return false;
      }
    }
    return true;
  }

  public static long memoryStringToLong(String memString)
          throws NumberFormatException
  {
    if (!isValidMemoryString(memString)) // not valid
    {
      throw (new NumberFormatException("Not a valid memory string"));
    }
    char suffix = Character
            .toLowerCase(memString.charAt(memString.length() - 1));
    if ('0' <= suffix && suffix <= '9') // no suffix
    {
      return Long.valueOf(memString);
    }
    if (memorySuffixes.indexOf(suffix) == -1) // suffix is unknown
    {
      return -1;
    }

    long multiplier = (long) Math.pow(2,
            memorySuffixes.indexOf(suffix) * 10); // note order of suffixes in
                                                  // memorySuffixes important
                                                  // here!
    // parse the arg. NumberFormatExceptions passed on to calling method
    long mem = Long
            .parseLong(memString.substring(0, memString.length() - 1));
    if (mem == 0)
    {
      return 0;
    }

    // apply multiplier only if result is not too big (i.e. bigger than a long)
    if (Long.MAX_VALUE / mem > multiplier)
    {
      return multiplier * mem;
    }
    else
    {
      // number too big for a Long. Limit to Long.MAX_VALUE
      System.out.println("Memory parsing of '" + memString
              + "' produces number too big.  Limiting to Long.MAX_VALUE="
              + Long.MAX_VALUE);
      return Long.MAX_VALUE;
    }
  }

  public static String memoryLongToString(long mem)
  {
    return memoryLongToString(mem, "%.3f");
  }

  public static String memoryLongToString(long mem, String format)
  {
    int exponent = 0;
    float num = mem;
    char suffix = 'b';

    for (int i = 0; i < memorySuffixes.length(); i++)
    {
      char s = Character.toUpperCase(memorySuffixes.charAt(i));
      if (mem < (long) Math.pow(2, exponent + 10)
              || i == memorySuffixes.length() - 1) // last suffix
      {
        suffix = s;
        num = (float) (mem / Math.pow(2, exponent));
        break;
      }
      exponent += 10;
    }

    return String.format(format, num) + suffix;
  }

  private static String ADJUSTMENT_MESSAGE = null;

  private static void setAdjustmentMessage(String reason, boolean quiet)
  {
    ADJUSTMENT_MESSAGE = reason;
    if (!quiet)
    {
      System.out.println(reason);
    }
  }

  public static void clearAdjustmentMessage()
  {
    ADJUSTMENT_MESSAGE = null;
  }

  public static String getAdjustmentMessage()
  {
    return ADJUSTMENT_MESSAGE;
  }

}
