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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils
{
  private static final Pattern DELIMITERS_PATTERN = Pattern
          .compile(".*='[^']*(?!')");

  private static final char PERCENT = '%';

  private static final boolean DEBUG = false;

  /*
   * URL encoded characters, indexed by char value
   * e.g. urlEncodings['='] = urlEncodings[61] = "%3D"
   */
  private static String[] urlEncodings = new String[255];

  /**
   * Returns a new character array, after inserting characters into the given
   * character array.
   * 
   * @param in
   *          the character array to insert into
   * @param position
   *          the 0-based position for insertion
   * @param count
   *          the number of characters to insert
   * @param ch
   *          the character to insert
   */
  public static final char[] insertCharAt(char[] in, int position,
          int count, char ch)
  {
    char[] tmp = new char[in.length + count];

    if (position >= in.length)
    {
      System.arraycopy(in, 0, tmp, 0, in.length);
      position = in.length;
    }
    else
    {
      System.arraycopy(in, 0, tmp, 0, position);
    }

    int index = position;
    while (count > 0)
    {
      tmp[index++] = ch;
      count--;
    }

    if (position < in.length)
    {
      System.arraycopy(in, position, tmp, index, in.length - position);
    }

    return tmp;
  }

  /**
   * Delete
   * 
   * @param in
   * @param from
   * @param to
   * @return
   */
  public static final char[] deleteChars(char[] in, int from, int to)
  {
    if (from >= in.length || from < 0)
    {
      return in;
    }

    char[] tmp;

    if (to >= in.length)
    {
      tmp = new char[from];
      System.arraycopy(in, 0, tmp, 0, from);
      to = in.length;
    }
    else
    {
      tmp = new char[in.length - to + from];
      System.arraycopy(in, 0, tmp, 0, from);
      System.arraycopy(in, to, tmp, from, in.length - to);
    }
    return tmp;
  }

  /**
   * Returns the last part of 'input' after the last occurrence of 'token'. For
   * example to extract only the filename from a full path or URL.
   * 
   * @param input
   * @param token
   *          a delimiter which must be in regular expression format
   * @return
   */
  public static String getLastToken(String input, String token)
  {
    if (input == null)
    {
      return null;
    }
    if (token == null)
    {
      return input;
    }
    String[] st = input.split(token);
    return st[st.length - 1];
  }

  /**
   * Parses the input string into components separated by the delimiter. Unlike
   * String.split(), this method will ignore occurrences of the delimiter which
   * are nested within single quotes in name-value pair values, e.g. a='b,c'.
   * 
   * @param input
   * @param delimiter
   * @return elements separated by separator
   */
  public static String[] separatorListToArray(String input,
          String delimiter)
  {
    int seplen = delimiter.length();
    if (input == null || input.equals("") || input.equals(delimiter))
    {
      return null;
    }
    List<String> jv = new ArrayList<>();
    int cp = 0, pos, escape;
    boolean wasescaped = false, wasquoted = false;
    String lstitem = null;
    while ((pos = input.indexOf(delimiter, cp)) >= cp)
    {
      escape = (pos > 0 && input.charAt(pos - 1) == '\\') ? -1 : 0;
      if (wasescaped || wasquoted)
      {
        // append to previous pos
        jv.set(jv.size() - 1, lstitem = lstitem + delimiter
                + input.substring(cp, pos + escape));
      }
      else
      {
        jv.add(lstitem = input.substring(cp, pos + escape));
      }
      cp = pos + seplen;
      wasescaped = escape == -1;
      // last separator may be in an unmatched quote
      wasquoted = DELIMITERS_PATTERN.matcher(lstitem).matches();
    }
    if (cp < input.length())
    {
      String c = input.substring(cp);
      if (wasescaped || wasquoted)
      {
        // append final separator
        jv.set(jv.size() - 1, lstitem + delimiter + c);
      }
      else
      {
        if (!c.equals(delimiter))
        {
          jv.add(c);
        }
      }
    }
    if (jv.size() > 0)
    {
      String[] v = jv.toArray(new String[jv.size()]);
      jv.clear();
      if (DEBUG)
      {
        System.err.println("Array from '" + delimiter
                + "' separated List:\n" + v.length);
        for (int i = 0; i < v.length; i++)
        {
          System.err.println("item " + i + " '" + v[i] + "'");
        }
      }
      return v;
    }
    if (DEBUG)
    {
      System.err.println(
              "Empty Array from '" + delimiter + "' separated List");
    }
    return null;
  }

  /**
   * Returns a string which contains the list elements delimited by the
   * separator. Null items are ignored. If the input is null or has length zero,
   * a single delimiter is returned.
   * 
   * @param list
   * @param separator
   * @return concatenated string
   */
  public static String arrayToSeparatorList(String[] list, String separator)
  {
    StringBuffer v = new StringBuffer();
    if (list != null && list.length > 0)
    {
      for (int i = 0, iSize = list.length; i < iSize; i++)
      {
        if (list[i] != null)
        {
          if (v.length() > 0)
          {
            v.append(separator);
          }
          // TODO - escape any separator values in list[i]
          v.append(list[i]);
        }
      }
      if (DEBUG)
      {
        System.err
                .println("Returning '" + separator + "' separated List:\n");
        System.err.println(v);
      }
      return v.toString();
    }
    if (DEBUG)
    {
      System.err.println(
              "Returning empty '" + separator + "' separated List\n");
    }
    return "" + separator;
  }

  /**
   * Converts a list to a string with a delimiter before each term except the
   * first. Returns an empty string given a null or zero-length argument. This
   * can be replaced with StringJoiner in Java 8.
   * 
   * @param terms
   * @param delim
   * @return
   */
  public static String listToDelimitedString(List<String> terms,
          String delim)
  {
    StringBuilder sb = new StringBuilder(32);
    if (terms != null && !terms.isEmpty())
    {
      boolean appended = false;
      for (String term : terms)
      {
        if (appended)
        {
          sb.append(delim);
        }
        appended = true;
        sb.append(term);
      }
    }
    return sb.toString();
  }

  /**
   * Convenience method to parse a string to an integer, returning 0 if the
   * input is null or not a valid integer
   * 
   * @param s
   * @return
   */
  public static int parseInt(String s)
  {
    int result = 0;
    if (s != null && s.length() > 0)
    {
      try
      {
        result = Integer.parseInt(s);
      } catch (NumberFormatException ex)
      {
      }
    }
    return result;
  }

  /**
   * Compares two versions formatted as e.g. "3.4.5" and returns -1, 0 or 1 as
   * the first version precedes, is equal to, or follows the second
   * 
   * @param v1
   * @param v2
   * @return
   */
  public static int compareVersions(String v1, String v2)
  {
    return compareVersions(v1, v2, null);
  }

  /**
   * Compares two versions formatted as e.g. "3.4.5b1" and returns -1, 0 or 1 as
   * the first version precedes, is equal to, or follows the second
   * 
   * @param v1
   * @param v2
   * @param pointSeparator
   *          a string used to delimit point increments in sub-tokens of the
   *          version
   * @return
   */
  public static int compareVersions(String v1, String v2,
          String pointSeparator)
  {
    if (v1 == null || v2 == null)
    {
      return 0;
    }
    String[] toks1 = v1.split("\\.");
    String[] toks2 = v2.split("\\.");
    int i = 0;
    for (; i < toks1.length; i++)
    {
      if (i >= toks2.length)
      {
        /*
         * extra tokens in v1
         */
        return 1;
      }
      String tok1 = toks1[i];
      String tok2 = toks2[i];
      if (pointSeparator != null)
      {
        /*
         * convert e.g. 5b2 into decimal 5.2 for comparison purposes
         */
        tok1 = tok1.replace(pointSeparator, ".");
        tok2 = tok2.replace(pointSeparator, ".");
      }
      try
      {
        float f1 = Float.valueOf(tok1);
        float f2 = Float.valueOf(tok2);
        int comp = Float.compare(f1, f2);
        if (comp != 0)
        {
          return comp;
        }
      } catch (NumberFormatException e)
      {
        System.err
                .println("Invalid version format found: " + e.getMessage());
        return 0;
      }
    }

    if (i < toks2.length)
    {
      /*
       * extra tokens in v2 
       */
      return -1;
    }

    /*
     * same length, all tokens match
     */
    return 0;
  }

  /**
   * Converts the string to all lower-case except the first character which is
   * upper-cased
   * 
   * @param s
   * @return
   */
  public static String toSentenceCase(String s)
  {
    if (s == null)
    {
      return s;
    }
    if (s.length() <= 1)
    {
      return s.toUpperCase(Locale.ROOT);
    }
    return s.substring(0, 1).toUpperCase(Locale.ROOT)
            + s.substring(1).toLowerCase(Locale.ROOT);
  }

  /**
   * A helper method that strips off any leading or trailing html and body tags.
   * If no html tag is found, then also html-encodes angle bracket characters.
   * 
   * @param text
   * @return
   */
  public static String stripHtmlTags(String text)
  {
    if (text == null)
    {
      return null;
    }
    String tmp2up = text.toUpperCase(Locale.ROOT);
    int startTag = tmp2up.indexOf("<HTML>");
    if (startTag > -1)
    {
      text = text.substring(startTag + 6);
      tmp2up = tmp2up.substring(startTag + 6);
    }
    // is omission of "<BODY>" intentional here??
    int endTag = tmp2up.indexOf("</BODY>");
    if (endTag > -1)
    {
      text = text.substring(0, endTag);
      tmp2up = tmp2up.substring(0, endTag);
    }
    endTag = tmp2up.indexOf("</HTML>");
    if (endTag > -1)
    {
      text = text.substring(0, endTag);
    }

    if (startTag == -1 && (text.contains("<") || text.contains(">")))
    {
      text = text.replaceAll("<", "&lt;");
      text = text.replaceAll(">", "&gt;");
    }
    return text;
  }

  /**
   * Answers the input string with any occurrences of the 'encodeable'
   * characters replaced by their URL encoding
   * 
   * @param s
   * @param encodable
   * @return
   */
  public static String urlEncode(String s, String encodable)
  {
    if (s == null || s.isEmpty())
    {
      return s;
    }

    /*
     * do % encoding first, as otherwise it may double-encode!
     */
    if (encodable.indexOf(PERCENT) != -1)
    {
      s = urlEncode(s, PERCENT);
    }

    for (char c : encodable.toCharArray())
    {
      if (c != PERCENT)
      {
        s = urlEncode(s, c);
      }
    }
    return s;
  }

  /**
   * Answers the input string with any occurrences of {@code c} replaced with
   * their url encoding. Answers the input string if it is unchanged.
   * 
   * @param s
   * @param c
   * @return
   */
  static String urlEncode(String s, char c)
  {
    String decoded = String.valueOf(c);
    if (s.indexOf(decoded) != -1)
    {
      String encoded = getUrlEncoding(c);
      if (!encoded.equals(decoded))
      {
        s = s.replace(decoded, encoded);
      }
    }
    return s;
  }

  /**
   * Answers the input string with any occurrences of the specified (unencoded)
   * characters replaced by their URL decoding.
   * <p>
   * Example: {@code urlDecode("a%3Db%3Bc", "-;=,")} should answer
   * {@code "a=b;c"}.
   * 
   * @param s
   * @param encodable
   * @return
   */
  public static String urlDecode(String s, String encodable)
  {
    if (s == null || s.isEmpty())
    {
      return s;
    }

    for (char c : encodable.toCharArray())
    {
      String encoded = getUrlEncoding(c);
      if (s.indexOf(encoded) != -1)
      {
        String decoded = String.valueOf(c);
        s = s.replace(encoded, decoded);
      }
    }
    return s;
  }

  /**
   * Does a lazy lookup of the url encoding of the given character, saving the
   * value for repeat lookups
   * 
   * @param c
   * @return
   */
  private static String getUrlEncoding(char c)
  {
    if (c < 0 || c >= urlEncodings.length)
    {
      return String.valueOf(c);
    }

    String enc = urlEncodings[c];
    if (enc == null)
    {
      try
      {
        enc = urlEncodings[c] = URLEncoder.encode(String.valueOf(c),
                "UTF-8");
      } catch (UnsupportedEncodingException e)
      {
        enc = urlEncodings[c] = String.valueOf(c);
      }
    }
    return enc;
  }

  public static int firstCharPosIgnoreCase(String text, String chars)
  {
    int min = text.length() + 1;
    for (char c : chars.toLowerCase(Locale.ROOT).toCharArray())
    {
      int i = text.toLowerCase(Locale.ROOT).indexOf(c);
      if (0 <= i && i < min)
      {
        min = i;
      }
    }
    return min < text.length() + 1 ? min : -1;
  }

  public static int indexOfFirstWhitespace(String text)
  {
    int index = -1;
    Pattern pat = Pattern.compile("\\s");
    Matcher m = pat.matcher(text);
    if (m.find())
    {
      index = m.start();
    }
    return index;
  }

  /*
   * implementation of String.replaceLast.
   * Replaces only the last occurrence of toReplace in string with replacement.
   */
  public static String replaceLast(String string, String toReplace,
          String replacement)
  {
    int pos = string.lastIndexOf(toReplace);
    if (pos > -1)
    {
      return new StringBuilder().append(string.substring(0, pos))
              .append(replacement)
              .append(string.substring(pos + toReplace.length()))
              .toString();
    }
    else
    {
      return string;
    }

  }

  /* 
   * return the maximum length of a List of Strings
   */
  public static int maxLength(List<String> l)
  {
    int max = 0;
    for (String s : l)
    {
      if (s == null)
        continue;
      if (s.length() > max)
        max = s.length();
    }
    return max;
  }
}
