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

import java.util.Locale;

import java.awt.HeadlessException;

public class HiDPISetting
{
  private static final int hidpiThreshold = 160;

  private static final int hidpiMultiThreshold = 240;

  private static final int bigScreenThreshold = 1400;

  public static final String scalePropertyName = "sun.java2d.uiScale";

  private static final boolean isLinux;

  // private static final boolean isAMac;

  // private static final boolean isWindows;

  public static final String setHiDPIPropertyName = "setHiDPI";

  public static final String setHiDPIScalePropertyName = "setHiDPIScale";

  private static boolean setHiDPI = false;

  private static int setHiDPIScale = 0;

  public static int dpi = 0;

  public static int mindimension = 0;

  public static int width = 0;

  public static int scale = 0;

  public final static int MAX_SCALE = 8;

  private static boolean doneInit = false;

  private static boolean allowScalePropertyArg = false;

  private static ScreenInfo screenInfo = new ScreenInfo();

  static
  {
    String system = System.getProperty("os.name") == null ? null
            : System.getProperty("os.name").toLowerCase(Locale.ROOT);
    if (system != null)
    {
      isLinux = system.indexOf("linux") > -1;
      // isAMac = system.indexOf("mac") > -1;
      // isWindows = system.indexOf("windows") > -1;
    }
    else
    {
      isLinux = false;
      // isAMac = isWindows = false;
    }
  }

  private static void init()
  {
    if (doneInit)
    {
      return;
    }

    // get and use command line property values first
    String setHiDPIProperty = System.getProperty(setHiDPIPropertyName);
    boolean setHiDPIPropertyBool = Boolean.parseBoolean(setHiDPIProperty);

    // allow -DsetHiDPI=false to turn off HiDPI scaling
    if (setHiDPIProperty != null && !setHiDPIPropertyBool)
    {
      clear();
      doneInit = true;
      return;
    }

    setHiDPI = setHiDPIProperty != null && setHiDPIPropertyBool;

    String setHiDPIScaleProperty = System
            .getProperty(setHiDPIScalePropertyName);
    if (setHiDPIScaleProperty != null)
    {
      try
      {
        setHiDPIScale = Integer.parseInt(setHiDPIScaleProperty);
        // if setHiDPIScale property is validly set and setHiDPI property wasn't
        // attempted to be set we assume setHiDPIScale to be true
        if (setHiDPIProperty == null)
        {
          setHiDPI = true;
        }
      } catch (NumberFormatException e)
      {
        System.err.println(setHiDPIScalePropertyName + " property give ("
                + setHiDPIScaleProperty + ") but not parseable as integer");
      }
    }
    if (setHiDPI && setHiDPIScale > 0)
    {
      setHiDPIScale(setHiDPIScale);
      return;
    }

    // check to see if the scale property has already been set by something else
    // (e.g. the OS)
    String existingProperty = System.getProperty(scalePropertyName);
    if (existingProperty != null)
    {
      try
      {
        int existingPropertyVal = Integer.parseInt(existingProperty);
        System.out.println("Existing " + scalePropertyName + " is "
                + existingPropertyVal);
        if (existingPropertyVal > 1)
        {
          setHiDPIScale(existingPropertyVal);
          return;
        }
      } catch (NumberFormatException e)
      {
        System.out.println("Could not convert property " + scalePropertyName
                + " vale '" + existingProperty + "' to number");
      }
    }

    // Try and auto guess a good scale based on reported DPI (not trustworthy)
    // and screen resolution (more trustworthy)

    // get screen dpi
    screenInfo = getScreenInfo();
    try
    {
      dpi = screenInfo.getScreenResolution();
    } catch (HeadlessException e)
    {
      System.err.println("Cannot get screen resolution: " + e.getMessage());
    }

    // try and get screen size height and width
    try
    {
      int height = screenInfo.getScreenHeight();
      int width = screenInfo.getScreenWidth();
      // using mindimension in case of portrait screens
      mindimension = Math.min(height, width);
    } catch (HeadlessException e)
    {
      System.err.println(
              "Cannot get screen size height and width:" + e.getMessage());
    }

    // attempt at a formula for scaling based on screen dpi and mindimension.
    // scale will be an integer >=1. This formula is based on some testing and
    // guesswork!

    // scale based on reported dpi. if dpi>hidpiThreshold then scale=2+multiples
    // of hidpiMultiThreshold (else scale=1)
    // (e.g. dpi of 110 scales 1. dpi of 120 scales 2. dpi of 360 scales 3)
    int dpiScale = (dpi - hidpiThreshold > 0)
            ? 2 + ((dpi - hidpiThreshold) / hidpiMultiThreshold)
            : 1;

    int dimensionScale = 1 + (mindimension / bigScreenThreshold);

    // reject outrageous values -- dpiScale in particular could be mistaken
    if (dpiScale > MAX_SCALE)
    {
      dpiScale = 1;
    }
    if (dimensionScale > MAX_SCALE)
    {
      dimensionScale = 1;
    }

    // choose larger of dimensionScale or dpiScale (most likely dimensionScale
    // as dpiScale often misreported)
    int autoScale = Math.max(dpiScale, dimensionScale);

    // only make an automatic change if scale is changed and other conditions
    // (OS is linux) apply, or if setHiDPI has been specified
    if ((autoScale > 1 && isLinux) || setHiDPI)
    {
      setHiDPIScale(autoScale);
      return;
    }

    // looks like we're not doing any scaling
    doneInit = true;
  }

  public static void setHiDPIScale(int s)
  {
    scale = s;
    allowScalePropertyArg = true;
    doneInit = true;
  }

  public static String getScalePropertyArg(int s)
  {
    return "-D" + scalePropertyName + "=" + String.valueOf(s);
  }

  public static String getScalePropertyArg()
  {
    init();
    // HiDPI setting. Just looking at Linux to start with. Test with Windows.
    return allowScalePropertyArg ? getScalePropertyArg(scale) : null;
  }

  public static void clear()
  {
    setHiDPI = false;
    setHiDPIScale = 0;
    dpi = 0;
    mindimension = 0;
    width = 0;
    scale = 0;
    doneInit = false;
    allowScalePropertyArg = false;
  }

  public static void setScreenInfo(ScreenInfo si)
  {
    screenInfo = si;
  }

  public static ScreenInfo getScreenInfo()
  {
    if (screenInfo == null)
    {
      screenInfo = new ScreenInfo();
    }
    return screenInfo;
  }
}
