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

/**
 * <p>
 * Title:
 * </p>
 * PileUpfile
 * <p>
 * Description:
 * </p>
 * 
 * Read and write PileUp style MSF Files. This used to be the MSFFile class, and
 * was written according to the EBI's idea of a subset of the MSF alignment
 * format. But, that was updated to reflect current GCG style IO fashion, as
 * found in Emboss (thanks David Martin!)
 * 
 */
import jalview.datamodel.SequenceI;
import jalview.util.Format;

import java.io.IOException;

public class PileUpfile extends MSFfile
{

  /**
   * Creates a new MSFfile object.
   */
  public PileUpfile()
  {
  }

  /**
   * Creates a new MSFfile object.
   * 
   * @param inFile
   *          DOCUMENT ME!
   * @param sourceType
   *          DOCUMENT ME!
   * 
   * @throws IOException
   *           DOCUMENT ME!
   */
  public PileUpfile(String inFile, DataSourceType sourceType)
          throws IOException
  {
    super(inFile, sourceType);
  }

  public PileUpfile(FileParse source) throws IOException
  {
    super(source);
  }

  @Override
  public String print(SequenceI[] s, boolean jvsuffix)
  {
    StringBuffer out = new StringBuffer("PileUp");
    out.append(newline);
    out.append(newline);

    int max = 0;
    int maxid = 0;

    int i = 0;
    int bigChecksum = 0;
    int[] checksums = new int[s.length];
    while (i < s.length)
    {
      checksums[i] = checkSum(s[i].getSequenceAsString());
      bigChecksum += checksums[i];
      i++;
    }

    out.append("   MSF: " + s[0].getLength() + "   Type: P    Check:  "
            + bigChecksum % 10000 + "   ..");
    out.append(newline);
    out.append(newline);
    out.append(newline);

    i = 0;
    while ((i < s.length) && (s[i] != null))
    {
      String seq = s[i].getSequenceAsString();
      out.append(" Name: " + printId(s[i], jvsuffix) + " oo  Len:  "
              + seq.length() + "  Check:  " + checksums[i]
              + "  Weight:  1.00");
      out.append(newline);

      if (seq.length() > max)
      {
        max = seq.length();
      }

      if (s[i].getName().length() > maxid)
      {
        maxid = s[i].getName().length();
      }

      i++;
    }

    if (maxid < 10)
    {
      maxid = 10;
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
        String name = printId(s[j], jvsuffix);

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
