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

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.util.Format;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

public class ClustalFile extends AlignFile
{

  public ClustalFile()
  {
  }

  public ClustalFile(String inFile, DataSourceType sourceType)
          throws IOException
  {
    super(inFile, sourceType);
  }

  public ClustalFile(FileParse source) throws IOException
  {
    super(source);
  }

  @Override
  public void initData()
  {
    super.initData();
  }

  @Override
  public void parse() throws IOException
  {
    int i = 0;
    boolean flag = false;
    boolean top = false;
    StringBuffer pssecstr = new StringBuffer();
    StringBuffer consstr = new StringBuffer();
    Vector<String> headers = new Vector<>();
    Map<String, StringBuffer> seqhash = new HashMap<>();
    StringBuffer tempseq;
    String line, id;
    StringTokenizer str;

    try
    {
      while ((line = nextLine()) != null)
      {
        if (line.length() == 0)
        {
          top = true;
        }
        boolean isConservation = line.startsWith(SPACE)
                || line.startsWith(TAB);
        if (!isConservation)
        {
          str = new StringTokenizer(line);

          if (str.hasMoreTokens())
          {
            id = str.nextToken();

            if (id.equalsIgnoreCase("CLUSTAL"))
            {
              flag = true;
            }
            else
            {
              if (flag)
              {
                if (seqhash.containsKey(id))
                {
                  tempseq = seqhash.get(id);
                }
                else
                {
                  tempseq = new StringBuffer();
                  seqhash.put(id, tempseq);
                }

                if (!(headers.contains(id)))
                {
                  headers.addElement(id);
                }

                if (str.hasMoreTokens())
                {
                  tempseq.append(str.nextToken());
                }
                top = false;
              }
            }
          }
          else
          {
            flag = true;
          }
        }
        else
        {
          if (line.matches("\\s+(-|\\.|\\(|\\[|\\]|\\))+"))
          {
            if (top)
            {
              pssecstr.append(line.trim());
            }
            else
            {
              consstr.append(line.trim());
            }
          }
        }
      }
    } catch (IOException e)
    {
      System.err.println("Exception parsing clustal file " + e);
      e.printStackTrace();
    }

    if (flag)
    {
      this.noSeqs = headers.size();

      // Add sequences to the hash
      for (i = 0; i < headers.size(); i++)
      {
        if (seqhash.get(headers.elementAt(i)) != null)
        {
          if (maxLength < seqhash.get(headers.elementAt(i)).toString()
                  .length())
          {
            maxLength = seqhash.get(headers.elementAt(i)).toString()
                    .length();
          }

          Sequence newSeq = parseId(headers.elementAt(i).toString());
          newSeq.setSequence(
                  seqhash.get(headers.elementAt(i).toString()).toString());

          seqs.addElement(newSeq);
        }
        else
        {
          System.err.println("Clustal File Reader: Can't find sequence for "
                  + headers.elementAt(i));
        }
      }
      AlignmentAnnotation lastssa = null;
      if (pssecstr.length() == maxLength)
      {
        Vector<AlignmentAnnotation> ss = new Vector<>();
        AlignmentAnnotation ssa = lastssa = StockholmFile
                .parseAnnotationRow(ss, "secondary structure",
                        pssecstr.toString());
        ssa.label = "Secondary Structure";
        annotations.addElement(ssa);
      }
      if (consstr.length() == maxLength)
      {
        Vector<AlignmentAnnotation> ss = new Vector<>();
        AlignmentAnnotation ssa = StockholmFile.parseAnnotationRow(ss,
                "secondary structure", consstr.toString());
        ssa.label = "Consensus Secondary Structure";
        if (lastssa == null || !lastssa.getRNAStruc()
                .equals(ssa.getRNAStruc().replace('-', '.')))
        {
          annotations.addElement(ssa);
        }
      }
    }
  }

  @Override
  public String print(SequenceI[] s, boolean jvsuffix)
  {
    StringBuffer out = new StringBuffer("CLUSTAL" + newline + newline);

    int max = 0;
    int maxid = 0;

    int i = 0;

    while ((i < s.length) && (s[i] != null))
    {
      String tmp = printId(s[i], jvsuffix);

      max = Math.max(max, s[i].getLength());

      if (tmp.length() > maxid)
      {
        maxid = tmp.length();
      }

      i++;
    }

    if (maxid < 15)
    {
      maxid = 15;
    }

    maxid++;

    int len = 60;
    int nochunks = (max / len) + (max % len > 0 ? 1 : 0);

    for (i = 0; i < nochunks; i++)
    {
      int j = 0;

      while ((j < s.length) && (s[j] != null))
      {
        out.append(new Format("%-" + maxid + "s")
                .form(printId(s[j], jvsuffix) + " "));

        int chunkStart = i * len;
        int chunkEnd = chunkStart + len;

        int length = s[j].getLength();
        if ((chunkEnd < length) && (chunkStart < length))
        {
          out.append(s[j].getSequenceAsString(chunkStart, chunkEnd));
        }
        else
        {
          if (chunkStart < length)
          {
            out.append(s[j].getSequenceAsString().substring(chunkStart));
          }
        }

        out.append(newline);
        j++;
      }

      out.append(newline);
    }

    return out.toString();
  }
}
