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

import java.io.IOException;
import java.util.StringTokenizer;

import jalview.analysis.scoremodels.ScoreMatrix;
import jalview.analysis.scoremodels.ScoreModels;
import jalview.datamodel.SequenceI;

/**
 * A class that can parse a file containing a substitution matrix and register
 * it for use in Jalview
 * <p>
 * Accepts 'NCBI' format (e.g.
 * https://www.ncbi.nlm.nih.gov/Class/FieldGuide/BLOSUM62.txt), with the
 * addition of a header line to provide a matrix name, e.g.
 * 
 * <pre>
 * ScoreMatrix BLOSUM62
 * </pre>
 * 
 * Also accepts 'AAindex' format (as described at
 * http://www.genome.jp/aaindex/aaindex_help.html) with the minimum data
 * required being
 * 
 * <pre>
 * H accession number (used as score matrix identifier in Jalview)
 * D description (used for tooltip in Jalview)
 * M rows = symbolList
 * and the substitution scores
 * </pre>
 */
public class ScoreMatrixFile extends AlignFile
        implements AlignmentFileReaderI
{
  // first non-comment line identifier - also checked in IdentifyFile
  public static final String SCOREMATRIX = "SCOREMATRIX";

  private static final String DELIMITERS = " ,\t";

  private static final String COMMENT_CHAR = "#";

  private String matrixName;

  /*
   * aaindex format has scores for diagonal and below only
   */
  boolean isLowerDiagonalOnly;

  /*
   * ncbi format has symbols as first column on score rows
   */
  boolean hasGuideColumn;

  /**
   * Constructor
   * 
   * @param source
   * @throws IOException
   */
  public ScoreMatrixFile(FileParse source) throws IOException
  {
    super(false, source);
  }

  @Override
  public String print(SequenceI[] sqs, boolean jvsuffix)
  {
    return null;
  }

  /**
   * Parses the score matrix file, and if successful registers the matrix so it
   * will be shown in Jalview menus. This method is not thread-safe (a separate
   * instance of this class should be used by each thread).
   */
  @Override
  public void parse() throws IOException
  {
    ScoreMatrix sm = parseMatrix();

    ScoreModels.getInstance().registerScoreModel(sm);
  }

  /**
   * Parses the score matrix file and constructs a ScoreMatrix object. If an
   * error is found in parsing, it is thrown as FileFormatException. Any
   * warnings are written to syserr.
   * 
   * @return
   * @throws IOException
   */
  public ScoreMatrix parseMatrix() throws IOException
  {
    ScoreMatrix sm = null;
    int lineNo = 0;
    String name = null;
    char[] alphabet = null;
    float[][] scores = null;
    int size = 0;
    int row = 0;
    String err = null;
    String data;
    isLowerDiagonalOnly = false;

    while ((data = nextLine()) != null)
    {
      lineNo++;
      data = data.trim();
      if (data.startsWith(COMMENT_CHAR) || data.length() == 0)
      {
        continue;
      }
      // equivalent to data.startsWithIgnoreCase(SCOREMATRIX)
      if (data.regionMatches(true, 0, SCOREMATRIX, 0, SCOREMATRIX.length()))
      {
        /*
         * Parse name from ScoreMatrix <name>
         * we allow any delimiter after ScoreMatrix then take the rest of the line
         */
        if (name != null)
        {
          throw new FileFormatException(
                  "Error: 'ScoreMatrix' repeated in file at line "
                          + lineNo);
        }
        StringTokenizer nameLine = new StringTokenizer(data, DELIMITERS);
        if (nameLine.countTokens() < 2)
        {
          err = "Format error: expected 'ScoreMatrix <name>', found '"
                  + data + "' at line " + lineNo;
          throw new FileFormatException(err);
        }
        nameLine.nextToken(); // 'ScoreMatrix'
        name = nameLine.nextToken(); // next field
        name = data.substring(1).substring(data.substring(1).indexOf(name));
        continue;
      }
      else if (data.startsWith("H ") && name == null)
      {
        /*
         * AAindex identifier 
         */
        return parseAAIndexFormat(lineNo, data);
      }
      else if (name == null)
      {
        err = "Format error: 'ScoreMatrix <name>' should be the first non-comment line";
        throw new FileFormatException(err);
      }

      /*
       * next non-comment line after ScoreMatrix should be the 
       * column header line with the alphabet of scored symbols
       */
      if (alphabet == null)
      {
        StringTokenizer columnHeadings = new StringTokenizer(data,
                DELIMITERS);
        size = columnHeadings.countTokens();
        alphabet = new char[size];
        int col = 0;
        while (columnHeadings.hasMoreTokens())
        {
          alphabet[col++] = columnHeadings.nextToken().charAt(0);
        }
        scores = new float[size][];
        continue;
      }

      /*
       * too much information
       */
      if (row >= size)
      {
        err = "Unexpected extra input line in score model file: '" + data
                + "'";
        throw new FileFormatException(err);
      }

      parseValues(data, lineNo, scores, row, alphabet);
      row++;
    }

    /*
     * out of data - check we found enough
     */
    if (row < size)
    {
      err = String.format(
              "Expected %d rows of score data in score matrix but only found %d",
              size, row);
      throw new FileFormatException(err);
    }

    /*
     * If we get here, then name, alphabet and scores have been parsed successfully
     */
    sm = new ScoreMatrix(name, alphabet, scores);
    matrixName = name;

    return sm;
  }

  /**
   * Parse input as AAIndex format, starting from the header line with the
   * accession id
   * 
   * @param lineNo
   * @param data
   * @return
   * @throws IOException
   */
  protected ScoreMatrix parseAAIndexFormat(int lineNo, String data)
          throws IOException
  {
    String name = data.substring(2).trim();
    String description = null;

    float[][] scores = null;
    char[] alphabet = null;
    int row = 0;
    int size = 0;

    while ((data = nextLine()) != null)
    {
      lineNo++;
      data = data.trim();
      if (skipAAindexLine(data))
      {
        continue;
      }
      if (data.startsWith("D "))
      {
        description = data.substring(2).trim();
      }
      else if (data.startsWith("M "))
      {
        alphabet = parseAAindexRowsColumns(lineNo, data);
        size = alphabet.length;
        scores = new float[size][size];
      }
      else if (scores == null)
      {
        throw new FileFormatException(
                "No alphabet specified in matrix file");
      }
      else if (row >= size)
      {
        throw new FileFormatException("Too many data rows in matrix file");
      }
      else
      {
        parseValues(data, lineNo, scores, row, alphabet);
        row++;
      }
    }

    ScoreMatrix sm = new ScoreMatrix(name, description, alphabet, scores);
    matrixName = name;

    return sm;
  }

  /**
   * Parse one row of score values, delimited by whitespace or commas. The line
   * may optionally include the symbol from which the scores are defined. Values
   * may be present for all columns, or only up to the diagonal (in which case
   * upper diagonal values are set symmetrically).
   * 
   * @param data
   *          the line to be parsed
   * @param lineNo
   * @param scores
   *          the score matrix to add data to
   * @param row
   *          the row number / alphabet index position
   * @param alphabet
   * @return
   * @throws exception
   *           if invalid, or too few, or too many values
   */
  protected void parseValues(String data, int lineNo, float[][] scores,
          int row, char[] alphabet) throws FileFormatException
  {
    String err;
    int size = alphabet.length;
    StringTokenizer scoreLine = new StringTokenizer(data, DELIMITERS);

    int tokenCount = scoreLine.countTokens();

    /*
     * inspect first row to see if it includes the symbol in the first column,
     * and to see if it is lower diagonal values only (i.e. just one score)
     */
    if (row == 0)
    {
      if (data.startsWith(String.valueOf(alphabet[0])))
      {
        hasGuideColumn = true;
      }
      if (tokenCount == (hasGuideColumn ? 2 : 1))
      {
        isLowerDiagonalOnly = true;
      }
    }

    if (hasGuideColumn)
    {
      /*
       * check 'guide' symbol is the row'th letter of the alphabet
       */
      String symbol = scoreLine.nextToken();
      if (symbol.length() > 1 || symbol.charAt(0) != alphabet[row])
      {
        err = String.format(
                "Error parsing score matrix at line %d, expected '%s' but found '%s'",
                lineNo, alphabet[row], symbol);
        throw new FileFormatException(err);
      }
      tokenCount = scoreLine.countTokens(); // excluding guide symbol
    }

    /*
     * check the right number of values (lower diagonal or full format)
     */
    if (isLowerDiagonalOnly && tokenCount != row + 1)
    {
      err = String.format(
              "Expected %d scores at line %d: '%s' but found %d", row + 1,
              lineNo, data, tokenCount);
      throw new FileFormatException(err);
    }

    if (!isLowerDiagonalOnly && tokenCount != size)
    {
      err = String.format(
              "Expected %d scores at line %d: '%s' but found %d", size,
              lineNo, data, scoreLine.countTokens());
      throw new FileFormatException(err);
    }

    /*
     * parse and set the values, setting the symmetrical value
     * as well if lower diagonal format data
     */
    scores[row] = new float[size];
    int col = 0;
    String value = null;
    while (scoreLine.hasMoreTokens())
    {
      try
      {
        value = scoreLine.nextToken();
        scores[row][col] = Float.valueOf(value);
        if (isLowerDiagonalOnly)
        {
          scores[col][row] = scores[row][col];
        }
        col++;
      } catch (NumberFormatException e)
      {
        err = String.format("Invalid score value '%s' at line %d column %d",
                value, lineNo, col);
        throw new FileFormatException(err);
      }
    }
  }

  /**
   * Parse the line in an aaindex file that looks like
   * 
   * <pre>
   * M rows = ARNDCQEGHILKMFPSTWYV, cols = ARNDCQEGHILKMFPSTWYV
   * </pre>
   * 
   * rejecting it if rows and cols do not match. Returns the string of
   * characters in the row/cols alphabet.
   * 
   * @param lineNo
   * @param data
   * @return
   * @throws FileFormatException
   */
  protected char[] parseAAindexRowsColumns(int lineNo, String data)
          throws FileFormatException
  {
    String err = "Unexpected aaIndex score matrix data at line " + lineNo
            + ": " + data;

    try
    {
      String[] toks = data.split(",");
      String rowsAlphabet = toks[0].split("=")[1].trim();
      String colsAlphabet = toks[1].split("=")[1].trim();
      if (!rowsAlphabet.equals(colsAlphabet))
      {
        throw new FileFormatException("rows != cols");
      }
      return rowsAlphabet.toCharArray();
    } catch (Throwable t)
    {
      throw new FileFormatException(err + " " + t.getMessage());
    }
  }

  /**
   * Answers true if line is one we are not interested in from AAindex format
   * file
   * 
   * @param data
   * @return
   */
  protected boolean skipAAindexLine(String data)
  {
    if (data.startsWith(COMMENT_CHAR) || data.length() == 0)
    {
      return true;
    }
    if (data.startsWith("*") || data.startsWith("R ")
            || data.startsWith("A ") || data.startsWith("T ")
            || data.startsWith("J ") || data.startsWith("//"))
    {
      return true;
    }
    return false;
  }

  public String getMatrixName()
  {
    return matrixName;
  }
}
