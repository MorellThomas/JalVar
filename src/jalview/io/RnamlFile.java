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

import jalview.analysis.Rna;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Annotation;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.util.MessageManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.stevesoft.pat.Regex;

import fr.orsay.lri.varna.exceptions.ExceptionFileFormatOrSyntax;
import fr.orsay.lri.varna.exceptions.ExceptionLoadingFailed;
import fr.orsay.lri.varna.exceptions.ExceptionPermissionDenied;
import fr.orsay.lri.varna.factories.RNAFactory;
import fr.orsay.lri.varna.models.rna.RNA;

public class RnamlFile extends AlignFile
{
  public String id;

  protected ArrayList<RNA> result;

  public RnamlFile()
  {
    super();

  }

  public RnamlFile(String inFile, DataSourceType type) throws IOException
  {
    super(inFile, type);

  }

  public RnamlFile(FileParse source) throws IOException
  {
    super(source);

  }

  public BufferedReader CreateReader() throws FileNotFoundException
  {
    FileReader fr = null;
    fr = new FileReader(inFile);

    BufferedReader r = new BufferedReader(fr);
    return r;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.io.AlignFile#parse()
   */
  @Override
  public void parse() throws IOException
  {
    if (System.getProperty("java.version").indexOf("1.6") > -1
            || System.getProperty("java.version").indexOf("1.5") > -1)
    {
      // patch for 'This parser does not support specification "null" version
      // "null"' error
      // this hack ensures we get a properly updated SAXParserFactory on older
      // JVMs
      // thanks to Stefan Birkner over at https://coderwall.com/p/kqsrrw
      System.setProperty("javax.xml.parsers.SAXParserFactory",
              "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
    }
    // rather than lose exception semantics whilst parsing RNAML with VARNA we
    // wrap the routine and catch all exceptions before passing them up the
    // chain as an IOException
    try
    {
      _parse();
    } catch (ExceptionPermissionDenied pdx)
    {
      errormessage = MessageManager.formatMessage(
              "exception.rnaml_couldnt_access_datasource", new String[]
              { pdx.getMessage() });
      throw new IOException(pdx);
    } catch (ExceptionLoadingFailed lf)
    {
      errormessage = MessageManager.formatMessage(
              "exception.ranml_couldnt_process_data", new String[]
              { lf.getMessage() });
      throw new IOException(lf);
    } catch (ExceptionFileFormatOrSyntax iff)
    {
      errormessage = MessageManager
              .formatMessage("exception.ranml_invalid_file", new String[]
              { iff.getMessage() });
      throw new IOException(iff);
    } catch (Exception x)
    {
      error = true;
      errormessage = MessageManager.formatMessage(
              "exception.ranml_problem_parsing_data", new String[]
              { x.getMessage() });
      throw new IOException(errormessage, x);
    }
  }

  @SuppressWarnings("unchecked")
  public void _parse()
          throws FileNotFoundException, ExceptionPermissionDenied,
          ExceptionLoadingFailed, ExceptionFileFormatOrSyntax
  {

    result = RNAFactory.loadSecStrRNAML(getReader());

    // ArrayList<ArrayList> allarray = new ArrayList();
    // ArrayList<ArrayList<SimpleBP>> BP = new ArrayList();
    // ArrayList strucinarray = new ArrayList();
    SequenceI[] sqs = new SequenceI[result.size()];

    for (int i = 0; i < result.size(); i++)
    {

      RNA current = result.get(i);
      String rna = current.getStructDBN(true);
      String seq = current.getSeq();
      int begin = 1;
      int end = seq.length();

      id = current.getName();
      if (id == null || id.trim().length() == 0)
      {
        id = safeName(getDataName());
        if (result.size() > 1)
        {
          id += "." + i;
        }
      }
      sqs[i] = new Sequence(id, seq, begin, end);
      if (seq.length()!=(end-begin+1))
      {
        sqs[i].setEnd(sqs[i].findPosition(sqs[i].getLength()));
      }
      String[] annot = new String[rna.length()];
      Annotation[] ann = new Annotation[rna.length()];

      for (int j = 0; j < rna.length(); j++)
      {
        annot[j] = "" + rna.charAt(j);

      }
      for (int k = 0; k < rna.length(); k++)
      {
        ann[k] = new Annotation(annot[k], "",
                Rna.getRNASecStrucState(annot[k]).charAt(0), 0f);
      }

      AlignmentAnnotation align = new AlignmentAnnotation(
              "Secondary Structure",
              current.getID().trim().length() > 0
                      ? "Secondary Structure for " + current.getID()
                      : "",
              ann);

      sqs[i].addAlignmentAnnotation(align);
      sqs[i].setRNA(result.get(i));

      // allarray.add(strucinarray);

      annotations.addElement(align);
      // BP.add(align.bps);

    }

    setSeqs(sqs);
  }

  @Override
  public String print(SequenceI[] s, boolean jvSuffix)
  {
    return "not yet implemented";
  }

  public List<RNA> getRNA()
  {
    return result;
  }

  // public static void main(String[] args) {
  // Pattern p= Pattern.compile("(.+)[.][^.]+");
  // Matcher m = p.matcher("toto.xml.zip");
  // System.out.println(m.matches());
  // System.out.println(m.group(1));
  // }
  /**
   * make a friendly ID string.
   * 
   * @param dataName
   * @return truncated dataName to after last '/'
   */
  private String safeName(String dataName)
  {
    int b = 0;
    if ((b = dataName.lastIndexOf(".")) > 0)
    {
      dataName = dataName.substring(0, b - 1);
    }
    b = 0;
    Regex m = new Regex("[\\/]?([-A-Za-z0-9]+)\\.?");
    String mm = dataName;
    while (m.searchFrom(dataName, b))
    {
      mm = m.stringMatched();
      b = m.matchedTo();
    }
    return mm;
  }
}
