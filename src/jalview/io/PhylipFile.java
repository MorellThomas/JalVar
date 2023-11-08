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

import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;

import java.io.IOException;

/**
 * <p>
 * Parser and exporter for PHYLIP file format, as defined
 * <a href="http://evolution.genetics.washington.edu/phylip/doc/main.html">in
 * the documentation</a>. The parser imports PHYLIP files in both sequential and
 * interleaved format, and (currently) exports in interleaved format (using 60
 * characters per matrix for the sequence).
 * <p>
 *
 * <p>
 * The following assumptions have been made for input
 * <ul>
 * <li>Sequences are expressed as letters, not real numbers with decimal points
 * separated by blanks (which is a valid option according to the
 * specification)</li>
 * </ul>
 *
 * The following assumptions have been made for output
 * <ul>
 * <li>Interleaved format is used, with each matrix consisting of 60 characters;
 * </li>
 * <li>a blank line is added between each matrix;</li>
 * <li>no spacing is added between the sequence characters.</li>
 * </ul>
 *
 *
 * </p>
 *
 * @author David Corsar
 *
 *
 */
public class PhylipFile extends AlignFile
{

  public static final String FILE_DESC = "PHYLIP";

  /**
   * 
   * @see {@link AlignFile#AlignFile()}
   */
  public PhylipFile()
  {
    super();
  }

  /**
   * 
   * @param source
   * @throws IOException
   */
  public PhylipFile(FileParse source) throws IOException
  {
    super(source);
  }

  /**
   * @param inFile
   * @param sourceType
   * @throws IOException
   * @see {@link AlignFile#AlignFile(FileParse)}
   */
  public PhylipFile(String inFile, DataSourceType sourceType)
          throws IOException
  {
    super(inFile, sourceType);
  }

  /**
   * Parses the input source
   * 
   * @see {@link AlignFile#parse()}
   */
  @Override
  public void parse() throws IOException
  {
    try
    {
      // First line should contain number of species and number of
      // characters, separated by blanks
      String line = nextLine();
      String[] lineElements = line.trim().split("\\s+");
      if (lineElements.length < 2)
      {
        throw new IOException(
                "First line must contain the number of specifies and number of characters");
      }

      int numberSpecies = Integer.parseInt(lineElements[0]),
              numberCharacters = Integer.parseInt(lineElements[1]);

      if (numberSpecies <= 0)
      {
        // there are no sequences in this file so exit a nothing to
        // parse
        return;
      }

      SequenceI[] sequenceElements = new Sequence[numberSpecies];
      StringBuffer[] sequences = new StringBuffer[numberSpecies];

      // if file is in sequential format there is only one data matrix,
      // else there are multiple

      // read the first data matrix
      for (int i = 0; i < numberSpecies; i++)
      {
        line = nextLine();
        // lines start with the name - a maximum of 10 characters
        // if less, then padded out or terminated with a tab
        String potentialName = line.substring(0, 10);
        int tabIndex = potentialName.indexOf('\t');
        if (tabIndex == -1)
        {
          sequenceElements[i] = parseId(validateName(potentialName));
          sequences[i] = new StringBuffer(
                  removeWhitespace(line.substring(10)));
        }
        else
        {
          sequenceElements[i] = parseId(
                  validateName(potentialName.substring(0, tabIndex)));
          sequences[i] = new StringBuffer(
                  removeWhitespace(line.substring(tabIndex)));
        }
      }

      // determine if interleaved
      if ((sequences[0]).length() != numberCharacters)
      {
        // interleaved file, so have to read the remainder
        int i = 0;
        for (line = nextLine(); line != null; line = nextLine())
        {
          // ignore blank lines, as defined by the specification
          if (line.length() > 0)
          {
            sequences[i++].append(removeWhitespace(line));
          }
          // reached end of matrix, so get ready for the next one
          if (i == sequences.length)
          {
            i = 0;
          }
        }
      }

      // file parsed completely, now store sequences
      for (int i = 0; i < numberSpecies; i++)
      {
        // first check sequence is the expected length
        if (sequences[i].length() != numberCharacters)
        {
          throw new IOException(sequenceElements[i].getName()
                  + " sequence is incorrect length - should be "
                  + numberCharacters + " but is " + sequences[i].length());
        }
        sequenceElements[i].setSequence(sequences[i].toString());
        seqs.add(sequenceElements[i]);
      }

    } catch (IOException e)
    {
      System.err.println("Exception parsing PHYLIP file " + e);
      e.printStackTrace(System.err);
      throw e;
    }

  }

  /**
   * Removes any whitespace from txt, used to strip and spaces added to
   * sequences to improve human readability
   * 
   * @param txt
   * @return
   */
  private String removeWhitespace(String txt)
  {
    return txt.replaceAll("\\s*", "");
  }

  /**
   * According to the specification, the name cannot have parentheses, square
   * brackets, colon, semicolon, comma
   * 
   * @param name
   * @return
   * @throws IOException
   */
  private String validateName(String name) throws IOException
  {
    char[] invalidCharacters = new char[] { '(', ')', '[', ']', ':', ';',
        ',' };
    for (char c : invalidCharacters)
    {
      if (name.indexOf(c) > -1)
      {
        throw new IOException(
                "Species name contains illegal character " + c);
      }
    }
    return name;
  }

  /**
   * <p>
   * Prints the seqs in interleaved format, with each matrix consisting of 60
   * characters; a blank line is added between each matrix; no spacing is added
   * between the sequence characters.
   * </p>
   * 
   * 
   * @see {@link AlignFile#print()}
   */
  @Override
  public String print(SequenceI[] sqs, boolean jvsuffix)
  {

    StringBuffer sb = new StringBuffer(Integer.toString(sqs.length));
    sb.append(" ");
    // if there are no sequences, then define the number of characters as 0
    sb.append((sqs.length > 0) ? Integer.toString(sqs[0].getLength()) : "0")
            .append(newline);

    // Due to how IO is handled, there doesn't appear to be a way to store
    // if the original file was sequential or interleaved; if there is, then
    // use that to set the value of the following variable
    boolean sequential = false;

    // maximum number of columns for each row of interleaved format
    int numInterleavedColumns = 60;

    int sequenceLength = 0;
    for (SequenceI s : sqs)
    {

      // ensure name is only 10 characters
      String name = s.getName();
      if (name.length() > 10)
      {
        name = name.substring(0, 10);
      }
      else
      {
        // add padding 10 characters
        name = String.format("%1$-" + 10 + "s", s.getName());
      }
      sb.append(name);

      // sequential has the entire sequence following the name
      if (sequential)
      {
        sb.append(s.getSequenceAsString());
      }
      else
      {
        // Jalview ensures all sequences are of same length so no need
        // to keep track of min/max length
        sequenceLength = s.getLength();
        // interleaved breaks the sequence into chunks for
        // interleavedColumns characters
        sb.append(s.getSequence(0,
                Math.min(numInterleavedColumns, sequenceLength)));
      }
      sb.append(newline);
    }

    // add the remaining matrixes if interleaved and there is something to
    // add
    if (!sequential && sequenceLength > numInterleavedColumns)
    {
      // determine number of remaining matrixes
      int numMatrics = sequenceLength / numInterleavedColumns;
      if ((sequenceLength % numInterleavedColumns) > 0)
      {
        numMatrics++;
      }

      // start i = 1 as first matrix has already been printed
      for (int i = 1; i < numMatrics; i++)
      {
        // add blank line to separate this matrix from previous
        sb.append(newline);
        int start = i * numInterleavedColumns;
        for (SequenceI s : sqs)
        {
          sb.append(s.getSequence(start,
                  Math.min(start + numInterleavedColumns, sequenceLength)))
                  .append(newline);
        }
      }

    }

    return sb.toString();
  }
}
