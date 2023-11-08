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
package jalview.analysis;

import java.util.Locale;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import jalview.analysis.SecStrConsensus.SimpleBP;
import jalview.datamodel.SequenceFeature;
import jalview.gui.JvOptionPane;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class RnaTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = { "Functional" })
  public void testGetSimpleBPs() throws WUSSParseException
  {
    String rna = "([{})]"; // JAL-1081 example
    List<SimpleBP> bps = Rna.getSimpleBPs(rna);
    assertEquals(3, bps.size());

    /*
     * the base pairs are added in the order in which the matching base is found
     * (popping the stack of unmatched opening brackets)
     */
    assertEquals(2, bps.get(0).bp5); // {
    assertEquals(3, bps.get(0).bp3); // }
    assertEquals(0, bps.get(1).bp5); // (
    assertEquals(4, bps.get(1).bp3); // )
    assertEquals(1, bps.get(2).bp5); // [
    assertEquals(5, bps.get(2).bp3); // ]
  }

  @Test(groups = { "Functional" })
  public void testGetSimpleBPs_unmatchedOpener()
  {
    String rna = "(([{})]";
    try
    {
      Rna.getSimpleBPs(rna);
      fail("expected exception");
    } catch (WUSSParseException e)
    {
      // error reported as after end of input string
      assertEquals(rna.length(), e.getProblemPos());
    }
  }

  @Test(groups = { "Functional" })
  public void testGetSimpleBPs_unmatchedCloser()
  {
    String rna = "([{})]]]";
    try
    {
      Rna.getSimpleBPs(rna);
      fail("expected exception");
    } catch (WUSSParseException e)
    {
      // error reported as at first unmatched close
      assertEquals(6, e.getProblemPos());
    }

    /*
     * a variant where we have no opening bracket of the same type
     * as the unmatched closing bracket (no stack rather than empty stack)
     */
    rna = "((()])";
    try
    {
      Rna.getSimpleBPs(rna);
      fail("expected exception");
    } catch (WUSSParseException e)
    {
      assertEquals(4, e.getProblemPos());
    }
  }

  @Test(groups = { "Functional" })
  public void testGetRNASecStrucState()
  {
    assertNull(Rna.getRNASecStrucState(null));
    for (int i = 0; i <= 255; i++)
    {
      String s = String.valueOf((char) i);
      String ss = Rna.getRNASecStrucState(s);

      /*
       * valid SS chars are a-z, A-Z, and various brackets;
       * anything else is returned as a space
       */
      if ((i >= 'a' && i <= 'z') || (i >= 'A' && i <= 'Z')
              || "()[]{}<>".indexOf(s) > -1)
      {
        assertEquals("" + i, s, ss);
      }
      else
      {
        assertEquals(" ", ss);
      }
    }

    /*
     * a string is processed character by character
     */
    assertEquals("a [K ]z} {Q b(w)p><i",
            Rna.getRNASecStrucState("a.[K-]z}?{Q b(w)p><i"));
  }

  /**
   * Tests for isClosingParenthesis with char or String argument
   */
  @Test(groups = { "Functional" })
  public void testIsClosingParenthesis()
  {
    assertFalse(Rna.isClosingParenthesis(null));

    /*
     * only a-z, )]}> are closing bracket symbols
     */
    for (int i = 0; i <= 255; i++)
    {
      boolean isClosingChar = Rna.isClosingParenthesis((char) i);
      boolean isClosingString = Rna
              .isClosingParenthesis(String.valueOf((char) i));
      if ((i >= 'a' && i <= 'z') || i == ')' || i == '}' || i == ']'
              || i == '>')
      {
        assertTrue(String.format("close base pair %c", i), isClosingChar);
        assertTrue(String.format("close base pair %c", i), isClosingString);
      }
      else
      {
        assertFalse(String.format("close base pair %c", i), isClosingChar);
        assertFalse(String.format("close base pair %c", i),
                isClosingString);
      }
      assertFalse(Rna.isClosingParenthesis(String.valueOf((char) i) + " "));
    }
  }

  @Test(groups = { "Functional" })
  public void testIsCanonicalOrWobblePair()
  {
    String bases = "acgtuACGTU";
    for (int i = 0; i < bases.length(); i++)
    {
      for (int j = 0; j < bases.length(); j++)
      {
        char first = bases.charAt(i);
        char second = bases.charAt(j);
        boolean result = Rna.isCanonicalOrWobblePair(first, second);
        String pair = new String(new char[] { first, second })
                .toUpperCase(Locale.ROOT);
        if (pair.equals("AT") || pair.equals("TA") || pair.equals("AU")
                || pair.equals("UA") || pair.equals("GC")
                || pair.equals("CG") || pair.equals("GT")
                || pair.equals("TG") || pair.equals("GU")
                || pair.equals("UG"))
        {
          assertTrue(pair + " should be valid", result);
        }
        else
        {
          assertFalse(pair + " should be invalid", result);
        }
      }
    }
  }

  @Test(groups = { "Functional" })
  public void testIsCanonicalPair()
  {
    String bases = "acgtuACGTU";
    for (int i = 0; i < bases.length(); i++)
    {
      for (int j = 0; j < bases.length(); j++)
      {
        char first = bases.charAt(i);
        char second = bases.charAt(j);
        boolean result = Rna.isCanonicalPair(first, second);
        String pair = new String(new char[] { first, second })
                .toUpperCase(Locale.ROOT);
        if (pair.equals("AT") || pair.equals("TA") || pair.equals("AU")
                || pair.equals("UA") || pair.equals("GC")
                || pair.equals("CG"))
        {
          assertTrue(pair + " should be valid", result);
        }
        else
        {
          assertFalse(pair + " should be invalid", result);
        }
      }
    }
  }

  /**
   * Tests for isOpeningParenthesis with char or String argument
   */
  @Test(groups = { "Functional" })
  public void testIsOpeningParenthesis()
  {
    /*
     * only A-Z, ([{< are opening bracket symbols
     */
    for (int i = 0; i <= 255; i++)
    {
      boolean isOpeningChar = Rna.isOpeningParenthesis((char) i);
      boolean isOpeningString = Rna
              .isOpeningParenthesis(String.valueOf((char) i));
      if ((i >= 'A' && i <= 'Z') || i == '(' || i == '{' || i == '['
              || i == '<')
      {
        assertTrue(String.format("Open base pair %c", i), isOpeningChar);
        assertTrue(String.format("Open base pair %c", i), isOpeningString);
      }
      else
      {
        assertFalse(String.format("Open base pair %c", i), isOpeningChar);
        assertFalse(String.format("Open base pair %c", i), isOpeningString);
      }
      assertFalse(Rna.isOpeningParenthesis(String.valueOf((char) i) + " "));
    }
  }

  @Test(groups = { "Functional" })
  public void testGetMatchingOpeningParenthesis() throws WUSSParseException
  {
    for (int i = 0; i <= 255; i++)
    {
      boolean isClosing = Rna.isClosingParenthesis((char) i);
      if (isClosing)
      {
        char opening = Rna.getMatchingOpeningParenthesis((char) i);
        if (i >= 'a' && i <= 'z')
        {
          assertEquals(i + 'A' - 'a', opening);
        }
        else if (i == ')' && opening == '(' || i == ']' && opening == '['
                || i == '}' && opening == '{' || i == '>' && opening == '<')
        {
          // ok
        }
        else
        {
          fail("Got " + opening + " as opening bracket pair for "
                  + ((char) i));
        }
      }
    }
  }

  /**
   * Tests for isRnaSecondaryStructureSymbol with char or String argument
   */
  @Test(groups = { "Functional" })
  public void testIsRnaSecondaryStructureSymbol()
  {
    assertFalse(Rna.isRnaSecondaryStructureSymbol(null));

    /*
     * only A-Z,  a-z, ()[]{}<> are valid symbols
     */
    for (int i = 0; i <= 255; i++)
    {
      boolean isValidChar = Rna.isRnaSecondaryStructureSymbol((char) i);
      boolean isValidString = Rna
              .isRnaSecondaryStructureSymbol(String.valueOf((char) i));
      if ((i >= 'A' && i <= 'Z') || (i >= 'a' && i <= 'z') || i == '('
              || i == ')' || i == '{' || i == '}' || i == '[' || i == ']'
              || i == '<' || i == '>')
      {
        assertTrue(String.format("close base pair %c", i), isValidChar);
        assertTrue(String.format("close base pair %c", i), isValidString);
      }
      else
      {
        assertFalse(String.format("close base pair %c", i), isValidChar);
        assertFalse(String.format("close base pair %c", i), isValidString);
      }
      assertFalse(Rna.isRnaSecondaryStructureSymbol(
              String.valueOf((char) i) + " "));
    }
  }

  @Test(groups = "Functional")
  public void testGetHelixMap_oneHelix() throws WUSSParseException
  {
    String rna = ".(..[{.<..>}..].)";
    SequenceFeature[] sfs = Rna.getHelixMap(rna);
    assertEquals(4, sfs.length);

    /*
     * pairs are added in the order in which the closing bracket is found
     * (see testGetSimpleBPs)
     */
    assertEquals(7, sfs[0].getBegin());
    assertEquals(10, sfs[0].getEnd());
    assertEquals("0", sfs[0].getFeatureGroup());
    assertEquals(5, sfs[1].getBegin());
    assertEquals(11, sfs[1].getEnd());
    assertEquals("0", sfs[1].getFeatureGroup());
    assertEquals(4, sfs[2].getBegin());
    assertEquals(14, sfs[2].getEnd());
    assertEquals("0", sfs[2].getFeatureGroup());
    assertEquals(1, sfs[3].getBegin());
    assertEquals(16, sfs[3].getEnd());
    assertEquals("0", sfs[3].getFeatureGroup());
  }

  @Test(groups = "Functional")
  public void testGetHelixMap_twoHelices() throws WUSSParseException
  {
    String rna = ".([.)]..{.<}.>";
    SequenceFeature[] sfs = Rna.getHelixMap(rna);
    assertEquals(4, sfs.length);

    /*
     * pairs are added in the order in which the closing bracket is found
     * (see testGetSimpleBPs)
     */
    assertEquals(1, sfs[0].getBegin());
    assertEquals(4, sfs[0].getEnd());
    assertEquals("0", sfs[0].getFeatureGroup());
    assertEquals(2, sfs[1].getBegin());
    assertEquals(5, sfs[1].getEnd());
    assertEquals("0", sfs[1].getFeatureGroup());
    assertEquals(8, sfs[2].getBegin());
    assertEquals(11, sfs[2].getEnd());
    assertEquals("1", sfs[2].getFeatureGroup());
    assertEquals(10, sfs[3].getBegin());
    assertEquals(13, sfs[3].getEnd());
    assertEquals("1", sfs[3].getFeatureGroup());
  }
}
