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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import jalview.datamodel.Mapping;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Locale;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;

/**
 * Test the alignment -> Mapping routines
 * 
 * @author jimp
 * 
 */
public class TestAlignSeq
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  SequenceI s1, s2, s3;

  /**
   * @throws java.lang.Exception
   */
  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception
  {
    s1 = new Sequence("Seq1", "ASDFAQQQRRRSSS");
    s1.setStart(3);
    s1.setEnd(18);
    s2 = new Sequence("Seq2", "ASDFA");
    s2.setStart(5);
    s2.setEnd(9);
    s3 = new Sequence("Seq3", "SDFAQQQSSS");

  }

  @Test(groups = { "Functional" })
  /**
   * simple test that mapping from alignment corresponds identical positions.
   */
  public void testGetMappingForS1()
  {
    AlignSeq as = AlignSeq.doGlobalNWAlignment(s1, s2, AlignSeq.PEP);
    System.out.println("s1: " + as.getAStr1());
    System.out.println("s2: " + as.getAStr2());

    // aligned results match
    assertEquals("ASDFA", as.getAStr1());
    assertEquals(as.getAStr1(), as.getAStr2());

    Mapping s1tos2 = as.getMappingFromS1(false);
    checkMapping(s1tos2,s1,s2);
  }

  public void checkMapping(Mapping s1tos2,SequenceI _s1,SequenceI _s2)
  {
    System.out.println(s1tos2.getMap().toString());
    for (int i = _s2.getStart(); i < _s2.getEnd(); i++)
    {
      int p=s1tos2.getPosition(i);
      char s2c=_s2.getCharAt(i-_s2.getStart());
      char s1c=_s1.getCharAt(p-_s1.getStart());
      System.out.println("Position in s2: " + i +s2c 
      + " maps to position in s1: " +p+s1c);
      assertEquals(s1c,s2c);
    }
  }
  @Test(groups = { "Functional" })
  /**
   * simple test that mapping from alignment corresponds identical positions.
   */
  public void testGetMappingForS1_withLowerCase()
  {
    // make one of the sequences lower case
    SequenceI ns2 = new Sequence(s2);
    ns2.replace('D', 'd');
    AlignSeq as = AlignSeq.doGlobalNWAlignment(s1, ns2, AlignSeq.PEP);
    System.out.println("s1: " + as.getAStr1());
    System.out.println("s2: " + as.getAStr2());

    // aligned results match
    assertEquals("ASDFA", as.getAStr1());
    assertEquals(as.getAStr1(), as.getAStr2().toUpperCase(Locale.ROOT));

    Mapping s1tos2 = as.getMappingFromS1(false);
    assertEquals("ASdFA",as.getAStr2());
    // verify mapping is consistent between original all-caps sequences
    checkMapping(s1tos2,s1,s2);
  }

  @Test(groups = { "Functional" })
  public void testExtractGaps()
  {
    assertNull(AlignSeq.extractGaps(null, null));
    assertNull(AlignSeq.extractGaps(". -", null));
    assertNull(AlignSeq.extractGaps(null, "AB-C"));

    assertEquals("ABCD", AlignSeq.extractGaps(" .-", ". -A-B.C D."));
  }

  @Test(groups = { "Functional" })
  public void testPrintAlignment()
  {
    AlignSeq as = AlignSeq.doGlobalNWAlignment(s1, s3, AlignSeq.PEP);
    final StringBuilder baos = new StringBuilder();
    PrintStream ps = new PrintStream(System.out)
    {
      @Override
      public void print(String x)
      {
        baos.append(x);
      }

      @Override
      public void println()
      {
        baos.append("\n");
      }
    };

    as.printAlignment(ps);
    String expected = "Score = 320.0\nLength of alignment = 10\nSequence Seq1/4-13 (Sequence length = 14)\nSequence Seq3/1-10 (Sequence length = 10)\n\n"
            + "Seq1/4-13 SDFAQQQRRR\n" + "          |||||||   \n"
            + "Seq3/1-10 SDFAQQQSSS\n\n" + "Percentage ID = 70.00\n\n";
    assertEquals(expected, baos.toString());
  }
}
