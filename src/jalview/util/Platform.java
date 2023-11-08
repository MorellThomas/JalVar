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

import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;

import javax.swing.SwingUtilities;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import jalview.javascript.json.JSON;

/**
 * System platform information used by Applet and Application
 * 
 * @author Jim Procter
 */
public class Platform
{

  private static boolean isJS = /** @j2sNative true || */
          false;

  private static Boolean isNoJSMac = null, isNoJSWin = null, isMac = null,
          isWin = null, isLinux = null;

  private static Boolean isHeadless = null;

  // If launched from CLI with launcher script then -DCOLUMNWIDTH is set
  private static final int CONSOLEWIDTH;

  private static final String CONSOLEWIDTHPROPERTY = "CONSOLEWIDTH";

  static
  {
    int cw = 80;
    if (System.getProperty(CONSOLEWIDTHPROPERTY) != null
            && System.getProperty(CONSOLEWIDTHPROPERTY).length() > 0)
    {
      try
      {
        cw = Integer.parseInt(System.getProperty(CONSOLEWIDTHPROPERTY));
      } catch (NumberFormatException e)
      {
      }
    }
    CONSOLEWIDTH = cw;
  }

  /**
   * added to group mouse events into Windows and nonWindows (mac, unix, linux)
   * 
   * @return
   */
  public static boolean isMac()
  {
    return (isMac == null
            ? (isMac = (System.getProperty("os.name").indexOf("Mac") >= 0))
            : isMac);
  }

  /**
   * added to group mouse events into Windows and nonWindows (mac, unix, linux)
   * 
   * @return
   */
  public static boolean isWin()
  {
    return (isWin == null
            ? (isWin = (System.getProperty("os.name").indexOf("Win") >= 0))
            : isWin);
  }

  /**
   * added to check LaF for Linux
   * 
   * @return
   */
  public static boolean isLinux()
  {
    return (isLinux == null
            ? (isLinux = (System.getProperty("os.name")
                    .indexOf("Linux") >= 0))
            : isLinux);
  }

  /**
   * 
   * @return true if HTML5 JavaScript
   */
  public static boolean isJS()
  {
    return isJS;
  }

  /**
   * sorry folks - Macs really are different
   * 
   * BH: disabled for SwingJS -- will need to check key-press issues
   * 
   * @return true if we do things in a special way.
   */
  public static boolean isAMacAndNotJS()
  {
    return (isNoJSMac == null ? (isNoJSMac = !isJS && isMac()) : isNoJSMac);
  }

  /**
   * Check if we are on a Microsoft plaform...
   * 
   * @return true if we have to cope with another platform variation
   */
  public static boolean isWindowsAndNotJS()
  {
    return (isNoJSWin == null ? (isNoJSWin = !isJS && isWin()) : isNoJSWin);
  }

  /**
   * 
   * @return true if we are running in non-interactive no UI mode
   */
  public static boolean isHeadless()
  {
    if (isHeadless == null)
    {
      isHeadless = "true".equals(System.getProperty("java.awt.headless"));
    }
    return isHeadless;
  }

  /**
   * 
   * @return nominal maximum command line length for this platform
   */
  public static int getMaxCommandLineLength()
  {
    // TODO: determine nominal limits for most platforms.
    return 2046; // this is the max length for a windows NT system.
  }

  /**
   * Answers the input with every backslash replaced with a double backslash (an
   * 'escaped' single backslash)
   * 
   * @param s
   * @return
   */
  public static String escapeBackslashes(String s)
  {
    return s == null ? null : s.replace("\\", "\\\\");
  }

  /**
   * Answers true if the mouse event has Meta-down (Command key on Mac) or
   * Ctrl-down (on other o/s). Note this answers _false_ if the Ctrl key is
   * pressed instead of the Meta/Cmd key on Mac. To test for Ctrl-pressed on
   * Mac, you can use e.isPopupTrigger().
   * 
   * @param e
   * @return
   */
  public static boolean isControlDown(MouseEvent e)
  {
    return isControlDown(e, isMac());
  }

  /**
   * Overloaded version of method (to allow unit testing)
   * 
   * @param e
   * @param aMac
   * @return
   */
  protected static boolean isControlDown(MouseEvent e, boolean aMac)
  {
    if (!aMac)
    {
      return e.isControlDown();

      // Jalview 2.11 code below: above is as amended for JalviewJS
      // /*
      // * answer false for right mouse button
      // */
      // if (e.isPopupTrigger())
      // {
      // return false;
      // }
      // return
      // (jalview.util.ShortcutKeyMaskExWrapper.getMenuShortcutKeyMaskEx() //
      // .getMenuShortcutKeyMaskEx()
      // & jalview.util.ShortcutKeyMaskExWrapper
      // .getModifiersEx(e)) != 0; // getModifiers()) != 0;
    }
    // answer false for right mouse button
    // shortcut key will be META for a Mac
    return !e.isPopupTrigger()
            && (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
                    & e.getModifiers()) != 0;
    // could we use e.isMetaDown() here?
  }

  // BH: I don't know about that previous method. Here is what SwingJS uses.
  // Notice the distinction in mouse events. (BUTTON3_MASK == META)
  //
  // private static boolean isPopupTrigger(int id, int mods, boolean isWin) {
  // boolean rt = ((mods & InputEvent.BUTTON3_MASK) != 0);
  // if (isWin) {
  // if (id != MouseEvent.MOUSE_RELEASED)
  // return false;
  ////
  //// // Oddly, Windows returns InputEvent.META_DOWN_MASK on release, though
  //// // BUTTON3_DOWN_MASK for pressed. So here we just accept both.
  ////
  //// actually, we can use XXX_MASK, not XXX_DOWN_MASK and avoid this issue,
  // because
  //// J2S adds the appropriate extended (0x3FC0) and simple (0x3F) modifiers.
  ////
  // return rt;
  // } else {
  // // mac, linux, unix
  // if (id != MouseEvent.MOUSE_PRESSED)
  // return false;
  // boolean lt = ((mods & InputEvent.BUTTON1_MASK) != 0);
  // boolean ctrl = ((mods & InputEvent.CTRL_MASK) != 0);
  // return rt || (ctrl && lt);
  // }
  // }
  //

  /**
   * Windows (not Mac, Linux, or Unix) and right button to test for the
   * right-mouse pressed event in Windows that would have opened a menu or a
   * Mac.
   * 
   * @param e
   * @return
   */
  public static boolean isWinRightButton(MouseEvent e)
  {
    // was !isAMac(), but that is true also for Linux and Unix and JS,

    return isWin() && SwingUtilities.isRightMouseButton(e);
  }

  /**
   * Windows (not Mac, Linux, or Unix) and middle button -- for mouse wheeling
   * without pressing the button.
   * 
   * @param e
   * @return
   */
  public static boolean isWinMiddleButton(MouseEvent e)
  {
    // was !isAMac(), but that is true also for Linux and Unix and JS
    return isWin() && SwingUtilities.isMiddleMouseButton(e);
  }

  public static boolean allowMnemonics()
  {
    return !isMac();
  }

  public final static int TIME_RESET = 0;

  public final static int TIME_MARK = 1;

  public static final int TIME_SET = 2;

  public static final int TIME_GET = 3;

  public static long time, mark, set, duration;

  public static void timeCheck(String msg, int mode)
  {
    long t = System.currentTimeMillis();
    switch (mode)
    {
    case TIME_RESET:
      time = mark = t;
      if (msg != null)
      {
        System.err.println("Platform: timer reset\t\t\t" + msg);
      }
      break;
    case TIME_MARK:
      if (set > 0)
      {
        duration += (t - set);
      }
      else
      {
        if (time == 0)
        {
          time = mark = t;
        }
        if (msg != null)
        {
          System.err.println("Platform: timer mark\t" + ((t - time) / 1000f)
                  + "\t" + ((t - mark) / 1000f) + "\t" + msg);
        }
        mark = t;
      }
      break;
    case TIME_SET:
      set = t;
      break;
    case TIME_GET:
      if (msg != null)
      {
        System.err.println("Platform: timer dur\t" + ((t - time) / 1000f)
                + "\t" + ((duration) / 1000f) + "\t" + msg);
      }
      set = 0;
      break;
    }
  }

  public static void cacheFileData(String path, Object data)
  {
    if (!isJS() || data == null)
    {
      return;
    }
    /**
     * @j2sNative
     * 
     *            swingjs.JSUtil.cacheFileData$S$O(path, data);
     * 
     */
  }

  public static void cacheFileData(File file)
  {
    byte[] data;
    if (!isJS() || (data = Platform.getFileBytes(file)) == null)
    {
      return;
    }
    cacheFileData(file.toString(), data);
  }

  public static byte[] getFileBytes(File f)
  {
    return /** @j2sNative f && swingjs.JSUtil.getFileAsBytes$O(f) || */
    null;
  }

  public static byte[] getFileAsBytes(String fileStr)
  {
    byte[] bytes = null;
    // BH 2018 hack for no support for access-origin
    /**
     * @j2sNative bytes = swingjs.JSUtil.getFileAsBytes$O(fileStr)
     */
    cacheFileData(fileStr, bytes);
    return bytes;
  }

  @SuppressWarnings("unused")
  public static String getFileAsString(String url)
  {
    String ret = null;
    /**
     * @j2sNative
     * 
     *            ret = swingjs.JSUtil.getFileAsString$S(url);
     * 
     * 
     */
    cacheFileData(url, ret);
    return ret;
  }

  public static boolean setFileBytes(File f, String urlstring)
  {
    if (!isJS())
    {
      return false;
    }
    @SuppressWarnings("unused")
    byte[] bytes = getFileAsBytes(urlstring);
    // TODO temporary doubling of 秘bytes and _bytes;
    // just remove _bytes when new transpiler has been installed
    /**
     * @j2sNative f.\u79d8bytes = f._bytes = bytes;
     */
    return true;
  }

  public static void addJ2SBinaryType(String ext)
  {
    /**
     * @j2sNative
     * 
     *            J2S._binaryTypes.push("." + ext + "?");
     * 
     */
  }

  /**
   * Encode the URI using JavaScript encodeURIComponent
   * 
   * @param value
   * @return encoded value
   */
  public static String encodeURI(String value)
  {
    /**
     * @j2sNative value = encodeURIComponent(value);
     */
    return value;
  }

  /**
   * Open the URL using a simple window call if this is JavaScript
   * 
   * @param url
   * @return true if window has been opened
   */
  public static boolean openURL(String url)
  {
    if (!isJS())
    {
      return false;
    }
    /**
     * @j2sNative
     * 
     * 
     *            window.open(url);
     */
    return true;
  }

  public static String getUniqueAppletID()
  {
    /**
     * @j2sNative return swingjs.JSUtil.getApplet$()._uniqueId;
     *
     */
    return null;

  }

  /**
   * Read the Info block for this applet.
   * 
   * @param prefix
   *          "jalview_"
   * @param p
   * @return unique id for this applet
   */
  public static void readInfoProperties(String prefix, Properties p)
  {
    if (!isJS())
    {
      return;
    }
    String id = getUniqueAppletID();
    String key = "", value = "";
    /**
     * @j2sNative var info = swingjs.JSUtil.getApplet$().__Info || {}; for (var
     *            key in info) { if (key.indexOf(prefix) == 0) { value = "" +
     *            info[key];
     */

    System.out.println(
            "Platform id=" + id + " reading Info." + key + " = " + value);
    p.put(id + "_" + key, value);

    /**
     * @j2sNative
     * 
     * 
     *            } }
     */
  }

  public static void setAjaxJSON(URL url)
  {
    if (isJS())
    {
      JSON.setAjax(url);
    }
  }

  public static Object parseJSON(InputStream response)
          throws IOException, ParseException
  {
    if (isJS())
    {
      return JSON.parse(response);
    }

    BufferedReader br = null;
    try
    {
      br = new BufferedReader(new InputStreamReader(response, "UTF-8"));
      return new JSONParser().parse(br);
    } finally
    {
      if (br != null)
      {
        try
        {
          br.close();
        } catch (IOException e)
        {
          // ignore
        }
      }
    }
  }

  public static Object parseJSON(String json) throws ParseException
  {
    return (isJS() ? JSON.parse(json) : new JSONParser().parse(json));
  }

  public static Object parseJSON(Reader r)
          throws IOException, ParseException
  {
    if (r == null)
    {
      return null;
    }

    if (!isJS())
    {
      return new JSONParser().parse(r);
    }
    // Using a file reader is not currently supported in SwingJS JavaScript

    if (r instanceof FileReader)
    {
      throw new IOException(
              "StringJS does not support FileReader parsing for JSON -- but it could...");
    }
    return JSON.parse(r);

  }

  /**
   * Dump the input stream to an output file.
   * 
   * @param is
   * @param outFile
   * @throws IOException
   *           if the file cannot be created or there is a problem reading the
   *           input stream.
   */
  public static void streamToFile(InputStream is, File outFile)
          throws IOException
  {
    if (isJS() && /**
                   * @j2sNative outFile.setBytes$O && outFile.setBytes$O(is) &&
                   */
            true)
    {
      return;
    }
    FileOutputStream fio = new FileOutputStream(outFile);
    try
    {
      byte[] bb = new byte[32 * 1024];
      int l;
      while ((l = is.read(bb)) > 0)
      {
        fio.write(bb, 0, l);
      }
    } finally
    {
      fio.close();
    }
  }

  /**
   * Add a known domain that implements access-control-allow-origin:*
   * 
   * These should be reviewed periodically.
   * 
   * @param domain
   *          for a service that is not allowing ajax
   * 
   * @author hansonr@stolaf.edu
   * 
   */
  public static void addJ2SDirectDatabaseCall(String domain)
  {

    if (isJS())
    {
      System.out.println(
              "Platform adding known access-control-allow-origin * for domain "
                      + domain);
      /**
       * @j2sNative
       * 
       *            J2S.addDirectDatabaseCall(domain);
       */
    }

  }

  public static void getURLCommandArguments()
  {
    try
    {
      /**
       * Retrieve the first query field as command arguments to Jalview. Include
       * only if prior to "?j2s" or "&j2s" or "#". Assign the applet's
       * __Info.args element to this value.
       * 
       * @j2sNative var a =
       *            decodeURI((document.location.href.replace("&","?").split("?j2s")[0]
       *            + "?").split("?")[1].split("#")[0]); a &&
       *            (System.out.println("URL arguments detected were "+a)) &&
       *            (J2S.thisApplet.__Info.urlargs = a.split(" "));
       *            (!J2S.thisApplet.__Info.args || J2S.thisApplet.__Info.args
       *            == "" || J2S.thisApplet.__Info.args == "??") &&
       *            (J2S.thisApplet.__Info.args = a) && (System.out.println("URL
       *            arguments were passed to J2S main."));
       */
    } catch (Throwable t)
    {
    }
  }

  /**
   * A (case sensitive) file path comparator that ignores the difference between
   * / and \
   * 
   * @param path1
   * @param path2
   * @return
   */
  public static boolean pathEquals(String path1, String path2)
  {
    if (path1 == null)
    {
      return path2 == null;
    }
    if (path2 == null)
    {
      return false;
    }
    String p1 = path1.replace('\\', '/');
    String p2 = path2.replace('\\', '/');
    return p1.equals(p2);
  }

  /**
   * If started on command line using launch script, return the console width
   */
  public static int consoleWidth()
  {
    return CONSOLEWIDTH;
  }
}
