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

import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Annotation;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class FastaFile extends AlignFile
{
  /**
   * Length of a sequence line
   */
  int len = 72;

  StringBuffer out;

  /**
   * Creates a new FastaFile object.
   */
  public FastaFile()
  {
  }

  /**
   * Creates a new FastaFile object.
   * 
   * @param inFile
   *          DOCUMENT ME!
   * @param sourceType
   *          DOCUMENT ME!
   * 
   * @throws IOException
   *           DOCUMENT ME!
   */
  public FastaFile(String inFile, DataSourceType sourceType)
          throws IOException
  {
    super(inFile, sourceType);
  }

  public FastaFile(FileParse source) throws IOException
  {
    this(source, true);
  }

  public FastaFile(FileParse source, boolean closeData) throws IOException
  {
    super(true, source, closeData);
  }

  public FastaFile(SequenceI[] seqs)
  {
    super(seqs);
  }

  /**
   * DOCUMENT ME!
   * 
   * @throws IOException
   *           DOCUMENT ME!
   */
  @Override
  public void parse() throws IOException
  {
    StringBuffer sb = new StringBuffer();
    boolean firstLine = true;

    String line, uline;
    Sequence seq = null;

    boolean annotation = false;

    while ((uline = nextLine()) != null)
    {
      line = uline.trim();
      if (line.length() > 0)
      {
        if (line.charAt(0) == '>')
        {
          if (line.startsWith(">#_"))
          {
            if (annotation)
            {
              annotations.addElement(makeAnnotation(seq, sb));
            }
          }
          else
          {
            annotation = false;
          }

          if (!firstLine)
          {
            seq.setSequence(sb.toString());

            if (!annotation)
            {
              seqs.addElement(seq);
            }
          }

          seq = parseId(line.substring(1));
          firstLine = false;

          sb = new StringBuffer();

          if (line.startsWith(">#_"))
          {
            annotation = true;
          }
        }
        else
        {
          sb.append(annotation ? uline : line);
        }
      }
    }

    if (annotation)
    {
      annotations.addElement(makeAnnotation(seq, sb));
    }

    else if (!firstLine)
    {
      seq.setSequence(sb.toString());
      seqs.addElement(seq);
    }
  }

  private AlignmentAnnotation makeAnnotation(SequenceI seq, StringBuffer sb)
  {
    Annotation[] anots = new Annotation[sb.length()];
    char cb;
    for (int i = 0; i < anots.length; i++)
    {
      char cn = sb.charAt(i);
      if (cn != ' ')
      {
        anots[i] = new Annotation("" + cn, null, ' ', Float.NaN);
      }
    }
    AlignmentAnnotation aa = new AlignmentAnnotation(
            seq.getName().substring(2), seq.getDescription(), anots);
    return aa;
  }

  /**
   * called by AppletFormatAdapter to generate an annotated alignment, rather
   * than bare sequences.
   * 
   * @param al
   */
  public void addAnnotations(Alignment al)
  {
    addProperties(al);
    for (int i = 0; i < annotations.size(); i++)
    {
      AlignmentAnnotation aa = annotations.elementAt(i);
      aa.setPadGaps(true, al.getGapCharacter());
      al.addAnnotation(aa);
    }
  }

  @Override
  public String print(SequenceI[] s, boolean jvsuffix)
  {
    out = new StringBuffer();
    int i = 0;

    while ((i < s.length) && (s[i] != null))
    {
      out.append(">" + printId(s[i], jvsuffix));
      if (s[i].getDescription() != null)
      {
        out.append(" " + s[i].getDescription());
      }

      out.append(newline);

      int nochunks = (s[i].getLength() / len)
              + (s[i].getLength() % len > 0 ? 1 : 0);

      for (int j = 0; j < nochunks; j++)
      {
        int start = j * len;
        int end = start + len;

        if (end < s[i].getLength())
        {
          out.append(s[i].getSequenceAsString(start, end) + newline);
        }
        else if (start < s[i].getLength())
        {
          out.append(s[i].getSequenceAsString(start, s[i].getLength())
                  + newline);
        }
      }

      i++;
    }

    return out.toString();
  }
}
