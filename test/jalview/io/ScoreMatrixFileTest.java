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
package jalview.io;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import jalview.analysis.scoremodels.ScoreMatrix;
import jalview.analysis.scoremodels.ScoreModels;

import java.io.IOException;
import java.net.MalformedURLException;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class ScoreMatrixFileTest
{

  @AfterMethod(alwaysRun = true)
  public void tearDownAfterTest()
  {
    ScoreModels.getInstance().reset();
  }

  /**
   * Test a successful parse of a (small) score matrix file
   * 
   * @throws IOException
   * @throws MalformedURLException
   */
  @Test(groups = "Functional")
  public void testParseMatrix_ncbiMixedDelimiters()
          throws MalformedURLException, IOException
  {
    /*
     * some messy but valid input data, with comma, space
     * or tab (or combinations) as score value delimiters
     * this example includes 'guide' symbols on score rows
     */
    String data = "ScoreMatrix MyTest (example)\n" + "A\tT\tU\tt\tx\t-\n"
            + "A,1.1,1.2,1.3,1.4, 1.5, 1.6\n"
            + "T,2.1 2.2 2.3 2.4 2.5 2.6\n"
            + "U\t3.1\t3.2\t3.3\t3.4\t3.5\t3.6\t\n"
            + "t, 5.1,5.3,5.3,5.4,5.5, 5.6\n"
            + "x\t6.1, 6.2 6.3 6.4 6.5 6.6\n"
            + "-, \t7.1\t7.2 7.3, 7.4, 7.5\t,7.6\n";
    FileParse fp = new FileParse(data, DataSourceType.PASTE);
    ScoreMatrixFile parser = new ScoreMatrixFile(fp);
    ScoreMatrix sm = parser.parseMatrix();

    assertNotNull(sm);
    assertEquals(sm.getName(), "MyTest (example)");
    assertEquals(sm.getSize(), 6);
    assertNull(sm.getDescription());
    assertTrue(sm.isDNA());
    assertFalse(sm.isProtein());
    assertEquals(sm.getMinimumScore(), 1.1f);
    assertEquals(sm.getPairwiseScore('A', 'A'), 1.1f);
    assertEquals(sm.getPairwiseScore('A', 'T'), 1.2f);
    assertEquals(sm.getPairwiseScore('a', 'T'), 1.2f); // A/a equivalent
    assertEquals(sm.getPairwiseScore('A', 't'), 1.4f); // T/t not equivalent
    assertEquals(sm.getPairwiseScore('a', 't'), 1.4f);
    assertEquals(sm.getPairwiseScore('U', 'x'), 3.5f);
    assertEquals(sm.getPairwiseScore('u', 'x'), 3.5f);
    // X (upper) and '.' unmapped - get minimum score
    assertEquals(sm.getPairwiseScore('U', 'X'), 1.1f);
    assertEquals(sm.getPairwiseScore('A', '.'), 1.1f);
    assertEquals(sm.getPairwiseScore('-', '-'), 7.6f);
    assertEquals(sm.getPairwiseScore('A', (char) 128), 0f); // out of range
  }

  @Test(groups = "Functional")
  public void testParseMatrix_headerMissing()
  {
    String data;

    data = "X Y\n1 2\n3 4\n";
    try
    {
      new ScoreMatrixFile(new FileParse(data, DataSourceType.PASTE))
              .parseMatrix();
      fail("expected exception");
    } catch (IOException e)
    {
      assertEquals(e.getMessage(),
              "Format error: 'ScoreMatrix <name>' should be the first non-comment line");
    }
  }

  @Test(groups = "Functional")
  public void testParseMatrix_ncbiNotEnoughRows()
  {
    String data = "ScoreMatrix MyTest\nX Y Z\n1 2 3\n4 5 6\n";
    try
    {
      new ScoreMatrixFile(new FileParse(data, DataSourceType.PASTE))
              .parseMatrix();
      fail("expected exception");
    } catch (IOException e)
    {
      assertEquals(e.getMessage(),
              "Expected 3 rows of score data in score matrix but only found 2");
    }
  }

  @Test(groups = "Functional")
  public void testParseMatrix_ncbiNotEnoughColumns()
  {
    String data = "ScoreMatrix MyTest\nX Y Z\n1 2 3\n4 5\n7 8 9\n";
    try
    {
      new ScoreMatrixFile(new FileParse(data, DataSourceType.PASTE))
              .parseMatrix();
      fail("expected exception");
    } catch (IOException e)
    {
      assertEquals(e.getMessage(),
              "Expected 3 scores at line 4: '4 5' but found 2");
    }
  }

  @Test(groups = "Functional")
  public void testParseMatrix_ncbiTooManyColumns()
  {
    /*
     * with two too many columns:
     */
    String data = "ScoreMatrix MyTest\nX\tY\tZ\n1 2 3\n4 5 6 7\n8 9 10\n";
    try
    {
      new ScoreMatrixFile(new FileParse(data, DataSourceType.PASTE))
              .parseMatrix();
      fail("expected exception");
    } catch (IOException e)
    {
      assertEquals(e.getMessage(),
              "Expected 3 scores at line 4: '4 5 6 7' but found 4");
    }

    /*
     * with guide character and one too many columns:
     */
    data = "ScoreMatrix MyTest\nX Y\nX 1 2\nY 3 4 5\n";
    try
    {
      new ScoreMatrixFile(new FileParse(data, DataSourceType.PASTE))
              .parseMatrix();
      fail("expected exception");
    } catch (IOException e)
    {
      assertEquals(e.getMessage(),
              "Expected 2 scores at line 4: 'Y 3 4 5' but found 3");
    }

    /*
     * with no guide character and one too many columns
     */
    data = "ScoreMatrix MyTest\nX Y\n1 2\n3 4 5\n";
    try
    {
      new ScoreMatrixFile(new FileParse(data, DataSourceType.PASTE))
              .parseMatrix();
      fail("expected exception");
    } catch (IOException e)
    {
      assertEquals(e.getMessage(),
              "Expected 2 scores at line 4: '3 4 5' but found 3");
    }
  }

  @Test(groups = "Functional")
  public void testParseMatrix_ncbiTooManyRows()
  {
    String data = "ScoreMatrix MyTest\n\tX\tY\tZ\n1 2 3\n4 5 6\n7 8 9\n10 11 12\n";
    try
    {
      new ScoreMatrixFile(new FileParse(data, DataSourceType.PASTE))
              .parseMatrix();
      fail("expected exception");
    } catch (IOException e)
    {
      assertEquals(e.getMessage(),
              "Unexpected extra input line in score model file: '10 11 12'");
    }
  }

  @Test(groups = "Functional")
  public void testParseMatrix_ncbiBadDelimiter()
  {
    String data = "ScoreMatrix MyTest\n X Y Z\n1|2|3\n4|5|6\n";
    try
    {
      new ScoreMatrixFile(new FileParse(data, DataSourceType.PASTE))
              .parseMatrix();
      fail("expected exception");
    } catch (IOException e)
    {
      assertEquals(e.getMessage(),
              "Invalid score value '1|2|3' at line 3 column 0");
    }
  }

  @Test(groups = "Functional")
  public void testParseMatrix_ncbiBadFloat()
  {
    String data = "ScoreMatrix MyTest\n\tX\tY\tZ\n1 2 3\n4 five 6\n7 8 9\n";
    try
    {
      new ScoreMatrixFile(new FileParse(data, DataSourceType.PASTE))
              .parseMatrix();
      fail("expected exception");
    } catch (IOException e)
    {
      assertEquals(e.getMessage(),
              "Invalid score value 'five' at line 4 column 1");
    }
  }

  @Test(groups = "Functional")
  public void testParseMatrix_ncbiBadGuideCharacter()
  {
    String data = "ScoreMatrix MyTest\n\tX Y\nX 1 2\ny 3 4\n";
    try
    {
      new ScoreMatrixFile(new FileParse(data, DataSourceType.PASTE))
              .parseMatrix();
      fail("expected exception");
    } catch (IOException e)
    {
      assertEquals(e.getMessage(),
              "Error parsing score matrix at line 4, expected 'Y' but found 'y'");
    }

    data = "ScoreMatrix MyTest\n\tX Y\nXX 1 2\nY 3 4\n";
    try
    {
      new ScoreMatrixFile(new FileParse(data, DataSourceType.PASTE))
              .parseMatrix();
      fail("expected exception");
    } catch (IOException e)
    {
      assertEquals(e.getMessage(),
              "Error parsing score matrix at line 3, expected 'X' but found 'XX'");
    }
  }

  @Test(groups = "Functional")
  public void testParseMatrix_ncbiNameMissing()
  {
    /*
     * Name missing on ScoreMatrix header line
     */
    String data = "ScoreMatrix\nX Y\n1 2\n3 4\n";
    try
    {
      new ScoreMatrixFile(new FileParse(data, DataSourceType.PASTE))
              .parseMatrix();
      fail("expected exception");
    } catch (IOException e)
    {
      assertEquals(e.getMessage(),
              "Format error: expected 'ScoreMatrix <name>', found 'ScoreMatrix' at line 1");
    }
  }

  /**
   * Test a successful parse of a (small) score matrix file
   * 
   * @throws IOException
   * @throws MalformedURLException
   */
  @Test(groups = "Functional")
  public void testParseMatrix_ncbiFormat()
          throws MalformedURLException, IOException
  {
    // input including comment and blank lines
    String data = "ScoreMatrix MyTest\n#comment\n\n" + "\tA\tB\tC\n"
            + "A\t1.0\t2.0\t3.0\n" + "B\t4.0\t5.0\t6.0\n"
            + "C\t7.0\t8.0\t9.0\n";
    FileParse fp = new FileParse(data, DataSourceType.PASTE);
    ScoreMatrixFile parser = new ScoreMatrixFile(fp);
    ScoreMatrix sm = parser.parseMatrix();

    assertNotNull(sm);
    assertEquals(sm.getName(), "MyTest");
    assertEquals(parser.getMatrixName(), "MyTest");
    assertEquals(sm.getPairwiseScore('A', 'A'), 1.0f);
    assertEquals(sm.getPairwiseScore('B', 'c'), 6.0f);
    assertEquals(sm.getSize(), 3);
  }

  /**
   * Test a successful parse of a (small) score matrix file
   * 
   * @throws IOException
   * @throws MalformedURLException
   */
  @Test(groups = "Functional")
  public void testParseMatrix_aaIndexBlosum80()
          throws MalformedURLException, IOException
  {
    FileParse fp = new FileParse("resources/scoreModel/blosum80.scm",
            DataSourceType.FILE);
    ScoreMatrixFile parser = new ScoreMatrixFile(fp);
    ScoreMatrix sm = parser.parseMatrix();

    assertNotNull(sm);
    assertEquals(sm.getName(), "HENS920103");
    assertEquals(sm.getDescription(),
            "BLOSUM80 substitution matrix (Henikoff-Henikoff, 1992)");
    assertFalse(sm.isDNA());
    assertTrue(sm.isProtein());
    assertEquals(20, sm.getSize());

    assertEquals(sm.getPairwiseScore('A', 'A'), 7f);
    assertEquals(sm.getPairwiseScore('A', 'R'), -3f);
    assertEquals(sm.getPairwiseScore('r', 'a'), -3f); // A/a equivalent
  }

  /**
   * Test a successful parse of a (small) score matrix file
   * 
   * @throws IOException
   * @throws MalformedURLException
   */
  @Test(groups = "Functional")
  public void testParseMatrix_aaindexFormat()
          throws MalformedURLException, IOException
  {
    /*
     * aaindex format has scores for diagonal and below only
     */
    String data = "H MyTest\n" + "D My description\n" + "R PMID:1438297\n"
            + "A Authors, names\n" + "T Journal title\n"
            + "J Journal reference\n" + "* matrix in 1/3 Bit Units\n"
            + "M rows = ABC, cols = ABC\n" + "A\t1.0\n" + "B\t4.0\t5.0\n"
            + "C\t7.0\t8.0\t9.0\n";
    FileParse fp = new FileParse(data, DataSourceType.PASTE);
    ScoreMatrixFile parser = new ScoreMatrixFile(fp);
    ScoreMatrix sm = parser.parseMatrix();

    assertNotNull(sm);
    assertEquals(sm.getSize(), 3);
    assertEquals(sm.getName(), "MyTest");
    assertEquals(sm.getDescription(), "My description");
    assertEquals(sm.getPairwiseScore('A', 'A'), 1.0f);
    assertEquals(sm.getPairwiseScore('A', 'B'), 4.0f);
    assertEquals(sm.getPairwiseScore('A', 'C'), 7.0f);
    assertEquals(sm.getPairwiseScore('B', 'A'), 4.0f);
    assertEquals(sm.getPairwiseScore('B', 'B'), 5.0f);
    assertEquals(sm.getPairwiseScore('B', 'C'), 8.0f);
    assertEquals(sm.getPairwiseScore('C', 'C'), 9.0f);
    assertEquals(sm.getPairwiseScore('C', 'B'), 8.0f);
    assertEquals(sm.getPairwiseScore('C', 'A'), 7.0f);
  }

  @Test(groups = "Functional")
  public void testParseMatrix_aaindex_mMissing()
          throws MalformedURLException, IOException
  {
    /*
     * aaindex format but M cols=, rows= is missing
     */
    String data = "H MyTest\n" + "A\t1.0\n" + "B\t4.0\t5.0\n"
            + "C\t7.0\t8.0\t9.0\n";
    FileParse fp = new FileParse(data, DataSourceType.PASTE);
    ScoreMatrixFile parser = new ScoreMatrixFile(fp);
    try
    {
      parser.parseMatrix();
      fail("Expected exception");
    } catch (FileFormatException e)
    {
      assertEquals(e.getMessage(), "No alphabet specified in matrix file");
    }
  }

  @Test(groups = "Functional")
  public void testParseMatrix_aaindex_rowColMismatch()
          throws MalformedURLException, IOException
  {
    String data = "H MyTest\n" + "M rows=ABC, cols=ABD\n" + "A\t1.0\n"
            + "B\t4.0\t5.0\n" + "C\t7.0\t8.0\t9.0\n";
    FileParse fp = new FileParse(data, DataSourceType.PASTE);
    ScoreMatrixFile parser = new ScoreMatrixFile(fp);
    try
    {
      parser.parseMatrix();
      fail("Expected exception");
    } catch (FileFormatException e)
    {
      assertEquals(e.getMessage(),
              "Unexpected aaIndex score matrix data at line 2: M rows=ABC, cols=ABD rows != cols");
    }
  }

  @Test(groups = "Functional")
  public void testParseMatrix_ncbiHeaderRepeated()
  {
    String data = "ScoreMatrix BLOSUM\nScoreMatrix PAM250\nX Y\n1 2\n3 4\n";
    try
    {
      new ScoreMatrixFile(new FileParse(data, DataSourceType.PASTE))
              .parseMatrix();
      fail("expected exception");
    } catch (IOException e)
    {
      assertEquals(e.getMessage(),
              "Error: 'ScoreMatrix' repeated in file at line 2");
    }
  }

  @Test(groups = "Functional")
  public void testParseMatrix_aaindex_tooManyRows()
          throws MalformedURLException, IOException
  {
    String data = "H MyTest\n" + "M rows=ABC, cols=ABC\n" + "A\t1.0\n"
            + "B\t4.0\t5.0\n" + "C\t7.0\t8.0\t9.0\n" + "C\t7.0\t8.0\t9.0\n";
    FileParse fp = new FileParse(data, DataSourceType.PASTE);
    ScoreMatrixFile parser = new ScoreMatrixFile(fp);
    try
    {
      parser.parseMatrix();
      fail("Expected exception");
    } catch (FileFormatException e)
    {
      assertEquals(e.getMessage(), "Too many data rows in matrix file");
    }
  }

  @Test(groups = "Functional")
  public void testParseMatrix_aaindex_extraDataLines()
          throws MalformedURLException, IOException
  {
    String data = "H MyTest\n" + "M rows=ABC, cols=ABC\n" + "A\t1.0\n"
            + "B\t4.0\t5.0\n" + "C\t7.0\t8.0\t9.0\n" + "something extra\n";
    FileParse fp = new FileParse(data, DataSourceType.PASTE);
    ScoreMatrixFile parser = new ScoreMatrixFile(fp);
    try
    {
      parser.parseMatrix();
      fail("Expected exception");
    } catch (FileFormatException e)
    {
      assertEquals(e.getMessage(), "Too many data rows in matrix file");
    }
  }

  @Test(groups = "Functional")
  public void testParseMatrix_aaindex_tooFewColumns()
          throws MalformedURLException, IOException
  {
    String data = "H MyTest\n" + "M rows=ABC, cols=ABC\n" + "A\t1.0\n"
            + "B\t4.0\t5.0\n" + "C\t7.0\t8.0\n";
    FileParse fp = new FileParse(data, DataSourceType.PASTE);
    ScoreMatrixFile parser = new ScoreMatrixFile(fp);
    try
    {
      parser.parseMatrix();
      fail("Expected exception");
    } catch (FileFormatException e)
    {
      assertEquals(e.getMessage(),
              "Expected 3 scores at line 5: 'C\t7.0\t8.0' but found 2");
    }
  }

  /**
   * Test a successful parse and register of a score matrix file
   * 
   * @throws IOException
   * @throws MalformedURLException
   */
  @Test(groups = "Functional")
  public void testParse_ncbiFormat()
          throws MalformedURLException, IOException
  {
    assertNull(ScoreModels.getInstance().getScoreModel("MyNewTest", null));

    String data = "ScoreMatrix MyNewTest\n" + "\tA\tB\tC\n"
            + "A\t1.0\t2.0\t3.0\n" + "B\t4.0\t5.0\t6.0\n"
            + "C\t7.0\t8.0\t9.0\n";
    FileParse fp = new FileParse(data, DataSourceType.PASTE);
    ScoreMatrixFile parser = new ScoreMatrixFile(fp);

    parser.parse();

    ScoreMatrix sm = (ScoreMatrix) ScoreModels.getInstance()
            .getScoreModel("MyNewTest", null);
    assertNotNull(sm);
    assertEquals(sm.getName(), "MyNewTest");
    assertEquals(parser.getMatrixName(), "MyNewTest");
    assertEquals(sm.getPairwiseScore('A', 'A'), 1.0f);
    assertEquals(sm.getPairwiseScore('B', 'c'), 6.0f);
    assertEquals(sm.getSize(), 3);
  }
}
