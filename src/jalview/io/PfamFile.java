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
import jalview.util.Format;
import jalview.util.MessageManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PfamFile extends AlignFile
{

  public PfamFile()
  {
  }

  public PfamFile(String inFile, DataSourceType sourceType)
          throws IOException
  {
    super(inFile, sourceType);
  }

  public PfamFile(FileParse source) throws IOException
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
    String line;

    HashMap<String, StringBuffer> seqhash = new HashMap<String, StringBuffer>();
    ArrayList<String> headers = new ArrayList<String>();
    boolean useTabs = false;
    int spces;
    while ((line = nextLine()) != null)
    {
      if (line.indexOf("#") == 0)
      {
        // skip comment lines
        continue;
      }
      // locate first space or (if already checked), tab
      if (useTabs)
      {
        spces = line.indexOf("\t");
      }
      else
      {
        spces = line.indexOf(" ");
        // check to see if we ought to split on tabs instead.
        if (!useTabs && spces == -1)
        {
          useTabs = true;
          spces = line.indexOf("\t");
        }
      }
      if (spces <= 0)
      {
        // no sequence data to split on
        continue;
      }
      String id = line.substring(0, spces);
      StringBuffer tempseq;

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
        headers.add(id);
      }
      if (spces + 1 < line.length())
      {
        tempseq.append(line.substring(spces + 1).trim());
      }
    }

    this.noSeqs = headers.size();

    if (noSeqs < 1)
    {
      throw new IOException(MessageManager
              .getString("exception.pfam_no_sequences_found"));
    }

    for (i = 0; i < headers.size(); i++)
    {
      if (seqhash.get(headers.get(i)) != null)
      {
        if (maxLength < seqhash.get(headers.get(i)).toString().length())
        {
          maxLength = seqhash.get(headers.get(i)).toString().length();
        }

        Sequence newSeq = parseId(headers.get(i).toString());
        newSeq.setSequence(
                seqhash.get(headers.get(i).toString()).toString());
        seqs.addElement(newSeq);
      }
      else
      {
        System.err.println("PFAM File reader: Can't find sequence for "
                + headers.get(i));
      }
    }
  }

  @Override
  public String print(SequenceI[] s, boolean jvsuffix)
  {
    StringBuffer out = new StringBuffer("");

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

    int j = 0;

    while ((j < s.length) && (s[j] != null))
    {
      out.append(new Format("%-" + maxid + "s")
              .form(printId(s[j], jvsuffix) + " "));

      out.append(s[j].getSequenceAsString());
      out.append(newline);
      j++;
    }

    out.append(newline);

    return out.toString();
  }
}
