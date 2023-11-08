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
/* Author: Lauren Michelle Lui 
 * Methods are based on RALEE methods http://personalpages.manchester.ac.uk/staff/sam.griffiths-jones/software/ralee/
 * Additional Author: Jan Engelhart (2011) - Structure consensus and bug fixing
 * Additional Author: Anne Menard (2012) - Pseudoknot support and secondary structure consensus
 * */

package jalview.analysis;

import jalview.analysis.SecStrConsensus.SimpleBP;
import jalview.datamodel.SequenceFeature;
import jalview.util.MessageManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Rna
{

  /**
   * Answers true if the character is a valid open pair rna secondary structure
   * symbol. Currently accepts A-Z, ([{<
   * 
   * @param c
   * @return
   */
  public static boolean isOpeningParenthesis(char c)
  {
    return ('A' <= c && c <= 'Z' || c == '(' || c == '[' || c == '{'
            || c == '<');
  }

  /**
   * Answers true if the string is a valid open pair rna secondary structure
   * symbol. Currently accepts A-Z, ([{<
   * 
   * @param s
   * @return
   */
  public static boolean isOpeningParenthesis(String s)
  {
    return s != null && s.length() == 1
            && isOpeningParenthesis(s.charAt(0));
  }

  /**
   * Answers true if the character is a valid close pair rna secondary structure
   * symbol. Currently accepts a-z, )]}>
   * 
   * @param c
   * @return
   */
  public static boolean isClosingParenthesis(char c)
  {
    return ('a' <= c && c <= 'z' || c == ')' || c == ']' || c == '}'
            || c == '>');
  }

  /**
   * Answers true if the string is a valid close pair rna secondary structure
   * symbol. Currently accepts a-z, )]}>
   * 
   * @param s
   * @return
   */
  public static boolean isClosingParenthesis(String s)
  {
    return s != null && s.length() == 1
            && isClosingParenthesis(s.charAt(0));
  }

  /**
   * Returns the matching open pair symbol for the given closing symbol.
   * Currently returns A-Z for a-z, or ([{< for )]}>, or the input symbol if it
   * is not a valid closing symbol.
   * 
   * @param c
   * @return
   */
  public static char getMatchingOpeningParenthesis(char c)
  {
    if ('a' <= c && c <= 'z')
    {
      return (char) (c + 'A' - 'a');
    }
    switch (c)
    {
    case ')':
      return '(';
    case ']':
      return '[';
    case '}':
      return '{';
    case '>':
      return '<';
    default:
      return c;
    }
  }

  /**
   * Based off of RALEE code ralee-get-base-pairs. Keeps track of open bracket
   * positions in "stack" vector. When a close bracket is reached, pair this
   * with the last matching element in the "stack" vector and store in "pairs"
   * vector. Remove last element in the "stack" vector. Continue in this manner
   * until the whole string is processed. Parse errors are thrown as exceptions
   * wrapping the error location - position of the first unmatched closing
   * bracket, or string length if there is an unmatched opening bracket.
   * 
   * @param line
   *          Secondary structure line of an RNA Stockholm file
   * @return
   * @throw {@link WUSSParseException}
   */
  protected static List<SimpleBP> getSimpleBPs(CharSequence line)
          throws WUSSParseException
  {
    Hashtable<Character, Stack<Integer>> stacks = new Hashtable<Character, Stack<Integer>>();
    List<SimpleBP> pairs = new ArrayList<SimpleBP>();
    int i = 0;
    while (i < line.length())
    {
      char base = line.charAt(i);

      if (isOpeningParenthesis(base))
      {
        if (!stacks.containsKey(base))
        {
          stacks.put(base, new Stack<Integer>());
        }
        stacks.get(base).push(i);

      }
      else if (isClosingParenthesis(base))
      {

        char opening = getMatchingOpeningParenthesis(base);

        if (!stacks.containsKey(opening))
        {
          throw new WUSSParseException(MessageManager.formatMessage(
                  "exception.mismatched_unseen_closing_char", new String[]
                  { String.valueOf(base) }), i);
        }

        Stack<Integer> stack = stacks.get(opening);
        if (stack.isEmpty())
        {
          // error whilst parsing i'th position. pass back
          throw new WUSSParseException(MessageManager.formatMessage(
                  "exception.mismatched_closing_char", new String[]
                  { String.valueOf(base) }), i);
        }
        int temp = stack.pop();

        pairs.add(new SimpleBP(temp, i));
      }
      i++;
    }
    for (char opening : stacks.keySet())
    {
      Stack<Integer> stack = stacks.get(opening);
      if (!stack.empty())
      {
        /*
         * we have an unmatched opening bracket; report error as at
         * i (length of input string)
         */
        throw new WUSSParseException(MessageManager.formatMessage(
                "exception.mismatched_opening_char", new String[]
                { String.valueOf(opening), String.valueOf(stack.pop()) }),
                i);
      }
    }
    return pairs;
  }

  /**
   * Function to get the end position corresponding to a given start position
   * 
   * @param indice
   *          - start position of a base pair
   * @return - end position of a base pair
   */
  /*
   * makes no sense at the moment :( public int findEnd(int indice){ //TODO:
   * Probably extend this to find the start to a given end? //could be done by
   * putting everything twice to the hash ArrayList<Integer> pair = new
   * ArrayList<Integer>(); return pairHash.get(indice); }
   */

  /**
   * Answers true if the character is a recognised symbol for RNA secondary
   * structure. Currently accepts a-z, A-Z, ()[]{}<>.
   * 
   * @param c
   * @return
   */
  public static boolean isRnaSecondaryStructureSymbol(char c)
  {
    return isOpeningParenthesis(c) || isClosingParenthesis(c);
  }

  /**
   * Answers true if the string is a recognised symbol for RNA secondary
   * structure. Currently accepts a-z, A-Z, ()[]{}<>.
   * 
   * @param s
   * @return
   */
  public static boolean isRnaSecondaryStructureSymbol(String s)
  {
    return isOpeningParenthesis(s) || isClosingParenthesis(s);
  }

  /**
   * Translates a string to RNA secondary structure representation. Returns the
   * string with any non-SS characters changed to spaces. Accepted characters
   * are a-z, A-Z, and (){}[]<> brackets.
   * 
   * @param ssString
   * @return
   */
  public static String getRNASecStrucState(String ssString)
  {
    if (ssString == null)
    {
      return null;
    }
    StringBuilder result = new StringBuilder(ssString.length());
    for (int i = 0; i < ssString.length(); i++)
    {
      char c = ssString.charAt(i);
      result.append(isRnaSecondaryStructureSymbol(c) ? c : " ");
    }
    return result.toString();
  }

  /**
   * Answers true if the base-pair is either a Watson-Crick (A:T/U, C:G) or a
   * wobble (G:T/U) pair (either way round), else false
   * 
   * @param first
   * @param second
   * @return
   */
  public static boolean isCanonicalOrWobblePair(char first, char second)
  {
    if (first > 'Z')
    {
      first -= 32;
    }
    if (second > 'Z')
    {
      second -= 32;
    }

    switch (first)
    {
    case 'A':
      switch (second)
      {
      case 'T':
      case 'U':
        return true;
      }
      break;
    case 'C':
      switch (second)
      {
      case 'G':
        return true;
      }
      break;
    case 'T':
    case 'U':
      switch (second)
      {
      case 'A':
      case 'G':
        return true;
      }
      break;
    case 'G':
      switch (second)
      {
      case 'C':
      case 'T':
      case 'U':
        return true;
      }
      break;
    }
    return false;
  }

  /**
   * Answers true if the base-pair is Watson-Crick - (A:T/U or C:G, either way
   * round), else false
   * 
   * @param first
   * @param second
   * @return
   */
  public static boolean isCanonicalPair(char first, char second)
  {

    if (first > 'Z')
    {
      first -= 32;
    }
    if (second > 'Z')
    {
      second -= 32;
    }

    switch (first)
    {
    case 'A':
      switch (second)
      {
      case 'T':
      case 'U':
        return true;
      }
      break;
    case 'G':
      switch (second)
      {
      case 'C':
        return true;
      }
      break;
    case 'C':
      switch (second)
      {
      case 'G':
        return true;
      }
      break;
    case 'T':
    case 'U':
      switch (second)
      {
      case 'A':
        return true;
      }
      break;
    }
    return false;
  }

  /**
   * Returns the matching close pair symbol for the given opening symbol.
   * Currently returns a-z for A-Z, or )]}> for ([{<, or the input symbol if it
   * is not a valid opening symbol.
   * 
   * @param c
   * @return
   */
  public static char getMatchingClosingParenthesis(char c)
  {
    if ('A' <= c && c <= 'Z')
    {
      return (char) (c + 'a' - 'A');
    }
    switch (c)
    {
    case '(':
      return ')';
    case '[':
      return ']';
    case '{':
      return '}';
    case '<':
      return '>';
    default:
      return c;
    }
  }

  public static SequenceFeature[] getHelixMap(CharSequence rnaAnnotation)
          throws WUSSParseException
  {
    List<SequenceFeature> result = new ArrayList<SequenceFeature>();

    int helix = 0; // Number of helices/current helix
    int lastopen = 0; // Position of last open bracket reviewed
    int lastclose = 9999999; // Position of last close bracket reviewed

    Map<Integer, Integer> helices = new HashMap<Integer, Integer>();
    // Keep track of helix number for each position

    // Go through each base pair and assign positions a helix
    List<SimpleBP> bps = getSimpleBPs(rnaAnnotation);
    for (SimpleBP basePair : bps)
    {
      final int open = basePair.getBP5();
      final int close = basePair.getBP3();

      // System.out.println("open " + open + " close " + close);
      // System.out.println("lastclose " + lastclose + " lastopen " + lastopen);

      // we're moving from right to left based on closing pair
      /*
       * catch things like <<..>>..<<..>> |
       */
      if (open > lastclose)
      {
        helix++;
      }

      /*
       * catch things like <<..<<..>>..<<..>>>> |
       */
      int j = bps.size();
      while (--j >= 0)
      {
        int popen = bps.get(j).getBP5();

        // System.out.println("j " + j + " popen " + popen + " lastopen "
        // +lastopen + " open " + open);
        if ((popen < lastopen) && (popen > open))
        {
          if (helices.containsValue(popen)
                  && ((helices.get(popen)) == helix))
          {
            continue;
          }
          else
          {
            helix++;
            break;
          }
        }
      }

      // Put positions and helix information into the hashtable
      helices.put(open, helix);
      helices.put(close, helix);

      // Record helix as featuregroup
      result.add(new SequenceFeature("RNA helix", "", open, close,
              String.valueOf(helix)));

      lastopen = open;
      lastclose = close;
    }

    return result.toArray(new SequenceFeature[result.size()]);
  }
}
