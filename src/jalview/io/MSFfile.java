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

import java.util.Locale;

import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.util.Comparison;
import jalview.util.Format;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class MSFfile extends AlignFile
{

  /**
   * Creates a new MSFfile object.
   */
  public MSFfile()
  {
  }

  /**
   * Creates a new MSFfile object.
   * 
   * @param inFile
   *          DOCUMENT ME!
   * @param type
   *          DOCUMENT ME!
   * 
   * @throws IOException
   *           DOCUMENT ME!
   */
  public MSFfile(String inFile, DataSourceType type) throws IOException
  {
    super(inFile, type);
  }

  public MSFfile(FileParse source) throws IOException
  {
    super(source);
  }

  /**
   * Read and parse MSF sequence data
   */
  @Override
  public void parse() throws IOException
  {
    boolean seqFlag = false;
    List<String> headers = new ArrayList<String>();
    Hashtable<String, StringBuilder> seqhash = new Hashtable<String, StringBuilder>();

    try
    {
      String line;
      while ((line = nextLine()) != null)
      {
        StringTokenizer str = new StringTokenizer(line);

        String key = null;
        while (str.hasMoreTokens())
        {
          String inStr = str.nextToken();

          // If line has header information add to the headers vector
          if (inStr.indexOf("Name:") != -1)
          {
            key = str.nextToken();
            headers.add(key);
          }

          // if line has // set SeqFlag so we know sequences are coming
          if (inStr.indexOf("//") != -1)
          {
            seqFlag = true;
          }

          // Process lines as sequence lines if seqFlag is set
          if ((inStr.indexOf("//") == -1) && seqFlag)
          {
            // sequence id is the first field
            key = inStr;

            StringBuilder tempseq;

            // Get sequence from hash if it exists
            if (seqhash.containsKey(key))
            {
              tempseq = seqhash.get(key);
            }
            else
            {
              tempseq = new StringBuilder(64);
              seqhash.put(key, tempseq);
            }

            // loop through the rest of the words
            while (str.hasMoreTokens())
            {
              // append the word to the sequence
              String sequenceBlock = str.nextToken();
              tempseq.append(sequenceBlock);
            }
          }
        }
      }
    } catch (IOException e)
    {
      System.err.println("Exception parsing MSFFile " + e);
      e.printStackTrace();
    }

    this.noSeqs = headers.size();

    // Add sequences to the hash
    for (int i = 0; i < headers.size(); i++)
    {
      if (seqhash.get(headers.get(i)) != null)
      {
        String head = headers.get(i);
        String seq = seqhash.get(head).toString();

        if (maxLength < head.length())
        {
          maxLength = head.length();
        }

        /*
         * replace ~ (leading/trailing positions) with the gap character;
         * use '.' as this is the internal gap character required by MSF
         */
        seq = seq.replace('~', '.');

        Sequence newSeq = parseId(head);

        newSeq.setSequence(seq);

        seqs.addElement(newSeq);
      }
      else
      {
        System.err.println("MSFFile Parser: Can't find sequence for "
                + headers.get(i));
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param seq
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int checkSum(String seq)
  {
    int check = 0;
    String sequence = seq.toUpperCase(Locale.ROOT);

    for (int i = 0; i < sequence.length(); i++)
    {
      try
      {

        int value = sequence.charAt(i);
        if (value != -1)
        {
          check += (i % 57 + 1) * value;
        }
      } catch (Exception e)
      {
        System.err.println("Exception during MSF Checksum calculation");
        e.printStackTrace();
      }
    }

    return check % 10000;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param s
   *          DOCUMENT ME!
   * @param is_NA
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  @Override
  public String print(SequenceI[] sqs, boolean jvSuffix)
  {

    boolean is_NA = Comparison.isNucleotide(sqs);

    SequenceI[] s = new SequenceI[sqs.length];

    StringBuilder out = new StringBuilder(256);
    out.append("!!").append(is_NA ? "NA" : "AA")
            .append("_MULTIPLE_ALIGNMENT 1.0");
    // TODO: JBPNote : Jalview doesn't remember NA or AA yet.
    out.append(newline);
    out.append(newline);
    int max = 0;
    int maxid = 0;
    int i = 0;

    while ((i < sqs.length) && (sqs[i] != null))
    {
      /*
       * modify to MSF format: uses '.' for internal gaps, 
       * and '~' for leading or trailing gaps
       */
      String seqString = sqs[i].getSequenceAsString().replace('-', '.');

      StringBuilder sb = new StringBuilder(seqString);

      for (int ii = 0; ii < sb.length(); ii++)
      {
        if (sb.charAt(ii) == '.')
        {
          sb.setCharAt(ii, '~');
        }
        else
        {
          break;
        }
      }

      for (int ii = sb.length() - 1; ii > 0; ii--)
      {
        if (sb.charAt(ii) == '.')
        {
          sb.setCharAt(ii, '~');
        }
        else
        {
          break;
        }
      }
      s[i] = new Sequence(sqs[i].getName(), sb.toString(),
              sqs[i].getStart(), sqs[i].getEnd());

      if (sb.length() > max)
      {
        max = sb.length();
      }

      i++;
    }

    Format maxLenpad = new Format(
            "%" + (new String("" + max)).length() + "d");
    Format maxChkpad = new Format(
            "%" + (new String("1" + max)).length() + "d");
    i = 0;

    int bigChecksum = 0;
    int[] checksums = new int[s.length];
    while (i < s.length)
    {
      checksums[i] = checkSum(s[i].getSequenceAsString());
      bigChecksum += checksums[i];
      i++;
    }

    long maxNB = 0;
    out.append("   MSF: " + s[0].getLength() + "   Type: "
            + (is_NA ? "N" : "P") + "    Check:  " + (bigChecksum % 10000)
            + "   ..");
    out.append(newline);
    out.append(newline);
    out.append(newline);

    String[] nameBlock = new String[s.length];
    String[] idBlock = new String[s.length];

    i = 0;
    while ((i < s.length) && (s[i] != null))
    {

      nameBlock[i] = new String("  Name: " + printId(s[i], jvSuffix) + " ");

      idBlock[i] = new String("Len: " + maxLenpad.form(s[i].getLength())
              + "  Check: " + maxChkpad.form(checksums[i])
              + "  Weight: 1.00" + newline);

      if (s[i].getName().length() > maxid)
      {
        maxid = s[i].getName().length();
      }

      if (nameBlock[i].length() > maxNB)
      {
        maxNB = nameBlock[i].length();
      }

      i++;
    }

    if (maxid < 10)
    {
      maxid = 10;
    }

    if (maxNB < 15)
    {
      maxNB = 15;
    }

    Format nbFormat = new Format("%-" + maxNB + "s");

    for (i = 0; (i < s.length) && (s[i] != null); i++)
    {
      out.append(nbFormat.form(nameBlock[i]) + idBlock[i]);
    }

    maxid++;
    out.append(newline);
    out.append(newline);
    out.append("//");
    out.append(newline);
    out.append(newline);
    int len = 50;

    int nochunks = (max / len) + (max % len > 0 ? 1 : 0);

    for (i = 0; i < nochunks; i++)
    {
      int j = 0;

      while ((j < s.length) && (s[j] != null))
      {
        String name = printId(s[j], jvSuffix);

        out.append(new Format("%-" + maxid + "s").form(name + " "));

        for (int k = 0; k < 5; k++)
        {
          int start = (i * 50) + (k * 10);
          int end = start + 10;

          int length = s[j].getLength();
          if ((end < length) && (start < length))
          {
            out.append(s[j].getSequence(start, end));

            if (k < 4)
            {
              out.append(" ");
            }
            else
            {
              out.append(newline);
            }
          }
          else
          {
            if (start < length)
            {
              out.append(s[j].getSequenceAsString().substring(start));
              out.append(newline);
            }
            else
            {
              if (k == 0)
              {
                out.append(newline);
              }
            }
          }
        }

        j++;
      }

      out.append(newline);
    }

    return out.toString();
  }
}
