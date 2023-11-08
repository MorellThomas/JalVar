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

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import jalview.bin.Console;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
 */
public class IdentifyFile
{

  public FileFormatI identify(Object file, DataSourceType protocol)
          throws FileFormatException
  {
    // BH 2018
    return (file instanceof File ? identify((File) file, protocol)
            : identify((String) file, protocol));

  }

  public FileFormatI identify(File file, DataSourceType sourceType)
          throws FileFormatException
  {
    // BH 2018
    String emessage = "UNIDENTIFIED FILE PARSING ERROR";
    FileParse parser = null;
    try
    {
      parser = new FileParse(file, sourceType);
      if (parser.isValid())
      {
        return identify(parser);
      }
    } catch (Exception e)
    {
      Console.error("Error whilst identifying " + file, e);
      emessage = e.getMessage();
    }
    if (parser != null)
    {
      throw new FileFormatException(parser.errormessage);
    }
    throw new FileFormatException(emessage);
  }

  /**
   * Identify a datasource's file content.
   *
   * @note Do not use this method for stream sources - create a FileParse object
   *       instead.
   *
   * @param file
   * @param sourceType
   * @return
   * @throws FileFormatException
   */
  public FileFormatI identify(String file, DataSourceType sourceType)
          throws FileFormatException
  {
    String emessage = "UNIDENTIFIED FILE PARSING ERROR";
    FileParse parser = null;
    try
    {
      parser = new FileParse(file, sourceType);
      if (parser.isValid())
      {
        return identify(parser);
      }
    } catch (Exception e)
    {
      Console.error("Error whilst identifying " + file, e);
      emessage = e.getMessage();
    }
    if (parser != null)
    {
      throw new FileFormatException(parser.errormessage);
    }
    throw new FileFormatException(emessage);
  }

  public FileFormatI identify(FileParse source) throws FileFormatException
  {
    return identify(source, true);
    // preserves original behaviour prior to version 2.3
  }

  public FileFormatI identify(AlignmentFileReaderI file,
          boolean closeSource) throws IOException
  {
    FileParse fp = new FileParse(file.getInFile(),
            file.getDataSourceType());
    return identify(fp, closeSource);
  }

  /**
   * Identify contents of source, closing it or resetting source to start
   * afterwards.
   *
   * @param source
   * @param closeSource
   * @return (best guess at) file format
   * @throws FileFormatException
   */
  public FileFormatI identify(FileParse source, boolean closeSource)
          throws FileFormatException
  {
    FileFormatI reply = FileFormat.Pfam;
    String data;
    int bytesRead = 0;
    int trimmedLength = 0;
    boolean lineswereskipped = false;
    boolean isBinary = false; // true if length is non-zero and non-printable
    // characters are encountered

    try
    {
      if (!closeSource)
      {
        source.mark();
      }
      boolean aaIndexHeaderRead = false;

      while ((data = source.nextLine()) != null)
      {
        bytesRead += data.length();
        trimmedLength += data.trim().length();
        if (!lineswereskipped)
        {
          for (int i = 0; !isBinary && i < data.length(); i++)
          {
            char c = data.charAt(i);
            isBinary = (c < 32 && c != '\t' && c != '\n' && c != '\r'
                    && c != 5 && c != 27); // nominal binary character filter
            // excluding CR, LF, tab,DEL and ^E
            // for certain blast ids
          }
        }
        if (isBinary)
        {
          // jar files are special - since they contain all sorts of random
          // characters.
          if (source.inFile != null)
          {
            String fileStr = source.inFile.getName();
            if (fileStr.contains(".jar") || fileStr.contains(".zip")
                    || fileStr.contains(".jvp"))
            {
              // possibly a Jalview archive (but check further)
              reply = FileFormat.Jalview;
            }
          }
          if (!lineswereskipped && data.startsWith("PK"))
          {
            reply = FileFormat.Jalview; // archive
            break;
          }
        }
        data = data.toUpperCase(Locale.ROOT);

        if (data.startsWith(ScoreMatrixFile.SCOREMATRIX))
        {
          reply = FileFormat.ScoreMatrix;
          break;
        }
        if (data.startsWith("LOCUS"))
        {
          reply = FileFormat.GenBank;
          break;
        }
        if (data.startsWith("ID "))
        {
          if (data.substring(2).trim().split(";").length == 7)
          {
            reply = FileFormat.Embl;
            break;
          }
        }
        if (data.startsWith("H ") && !aaIndexHeaderRead)
        {
          aaIndexHeaderRead = true;
        }
        if (data.startsWith("D ") && aaIndexHeaderRead)
        {
          reply = FileFormat.ScoreMatrix;
          break;
        }
        if (data.startsWith("##GFF-VERSION"))
        {
          // GFF - possibly embedded in a Jalview features file!
          reply = FileFormat.Features;
          break;
        }
        if (looksLikeFeatureData(data))
        {
          reply = FileFormat.Features;
          break;
        }
        if (data.indexOf("# STOCKHOLM") > -1)
        {
          reply = FileFormat.Stockholm;
          break;
        }
        if (data.indexOf("_ENTRY.ID") > -1
                || data.indexOf("_AUDIT_AUTHOR.NAME") > -1
                || data.indexOf("_ATOM_SITE.") > -1)
        {
          reply = FileFormat.MMCif;
          break;
        }
        // if (data.indexOf(">") > -1)
        if (data.startsWith(">"))
        {
          // FASTA, PIR file or BLC file
          boolean checkPIR = false, starterm = false;
          if ((data.indexOf(">P1;") > -1) || (data.indexOf(">DL;") > -1))
          {
            // watch for PIR file attributes
            checkPIR = true;
            reply = FileFormat.PIR;
          }
          // could also be BLC file, read next line to confirm
          data = source.nextLine();

          if (data.indexOf(">") > -1)
          {
            reply = FileFormat.BLC;
          }
          else
          {
            // Is this a single line BLC file?
            String data1 = source.nextLine();
            String data2 = source.nextLine();
            int c1;
            if (checkPIR)
            {
              starterm = (data1 != null && data1.indexOf("*") > -1)
                      || (data2 != null && data2.indexOf("*") > -1);
            }
            if (data2 != null && (c1 = data.indexOf("*")) > -1)
            {
              if (c1 == 0 && c1 == data2.indexOf("*"))
              {
                reply = FileFormat.BLC;
              }
              else
              {
                reply = FileFormat.Fasta; // possibly a bad choice - may be
                                          // recognised as
                // PIR
              }
              // otherwise can still possibly be a PIR file
            }
            else
            {
              reply = FileFormat.Fasta;
              // TODO : AMSA File is indicated if there is annotation in the
              // FASTA file - but FASTA will automatically generate this at the
              // mo.
              if (!checkPIR)
              {
                break;
              }
            }
          }
          // final check for PIR content. require
          // >P1;title\n<blah>\nterminated sequence to occur at least once.

          // TODO the PIR/fasta ambiguity may be the use case that is needed to
          // have
          // a 'Parse as type XXX' parameter for the applet/application.
          if (checkPIR)
          {
            String dta = null;
            if (!starterm)
            {
              do
              {
                try
                {
                  dta = source.nextLine();
                } catch (IOException ex)
                {
                }
                if (dta != null && dta.indexOf("*") > -1)
                {
                  starterm = true;
                }
              } while (dta != null && !starterm);
            }
            if (starterm)
            {
              reply = FileFormat.PIR;
              break;
            }
            else
            {
              reply = FileFormat.Fasta; // probably a bad choice!
            }
          }
          // read as a FASTA (probably)
          break;
        }
        if (data.indexOf("{\"") > -1)
        {
          reply = FileFormat.Json;
          break;
        }
        int lessThan = data.indexOf("<");
        if ((lessThan > -1)) // possible Markup Language data i.e HTML,
                             // RNAML, XML
        {
          String upper = data.toUpperCase(Locale.ROOT);
          if (upper.substring(lessThan).startsWith("<HTML"))
          {
            reply = FileFormat.Html;
            break;
          }
          if (upper.substring(lessThan).startsWith("<RNAML"))
          {
            reply = FileFormat.Rnaml;
            break;
          }
        }

        if ((data.length() < 1) || (data.indexOf("#") == 0))
        {
          lineswereskipped = true;
          continue;
        }

        if (data.indexOf("PILEUP") > -1)
        {
          reply = FileFormat.Pileup;

          break;
        }

        if ((data.indexOf("//") == 0) || ((data.indexOf("!!") > -1) && (data
                .indexOf("!!") < data.indexOf("_MULTIPLE_ALIGNMENT "))))
        {
          reply = FileFormat.MSF;

          break;
        }
        else if (data.indexOf("CLUSTAL") > -1)
        {
          reply = FileFormat.Clustal;

          break;
        }

        else if (data.indexOf("HEADER") == 0 || data.indexOf("ATOM") == 0)
        {
          reply = FileFormat.PDB;
          break;
        }
        else if (data.matches("\\s*\\d+\\s+\\d+\\s*"))
        {
          reply = FileFormat.Phylip;
          break;
        }
        else
        {
          if (!lineswereskipped && looksLikeJnetData(data))
          {
            reply = FileFormat.Jnet;
            break;
          }
        }

        lineswereskipped = true; // this means there was some junk before any
        // key file signature
      }
      if (closeSource)
      {
        source.close();
      }
      else
      {
        source.reset(bytesRead); // so the file can be parsed from the mark
      }
    } catch (Exception ex)
    {
      Console.error("File Identification failed!\n" + ex);
      throw new FileFormatException(source.errormessage);
    }
    if (trimmedLength == 0)
    {
      Console.error("File Identification failed! - Empty file was read.");
      throw new FileFormatException("EMPTY DATA FILE");
    }
    Console.debug("File format identified as " + reply.toString());
    return reply;
  }

  /**
   * Returns true if the data appears to be Jnet concise annotation format
   * 
   * @param data
   * @return
   */
  protected boolean looksLikeJnetData(String data)
  {
    char firstChar = data.charAt(0);
    int colonPos = data.indexOf(":");
    int commaPos = data.indexOf(",");
    boolean isJnet = firstChar != '*' && firstChar != ' ' && colonPos > -1
            && commaPos > -1 && colonPos < commaPos;
    // && data.indexOf(",")<data.indexOf(",", data.indexOf(","))) / ??
    return isJnet;
  }

  /**
   * Returns true if the data has at least 6 tab-delimited fields _and_ fields 4
   * and 5 are integer (start/end)
   * 
   * @param data
   * @return
   */
  protected boolean looksLikeFeatureData(String data)
  {
    if (data == null)
    {
      return false;
    }
    String[] columns = data.split("\t");
    if (columns.length < 6)
    {
      return false;
    }
    for (int col = 3; col < 5; col++)
    {
      try
      {
        Integer.parseInt(columns[col]);
      } catch (NumberFormatException e)
      {
        return false;
      }
    }
    return true;
  }

  /**
   * 
   * @param args
   * @j2sIgnore
   */
  public static void main(String[] args)
  {
    for (int i = 0; args != null && i < args.length; i++)
    {
      IdentifyFile ider = new IdentifyFile();
      FileFormatI type = null;
      try
      {
        type = ider.identify(args[i], DataSourceType.FILE);
      } catch (FileFormatException e)
      {
        Console.error(
                String.format("Error '%s' identifying file type for %s",
                        args[i], e.getMessage()));
      }
      Console.debug("Type of " + args[i] + " is " + type);
    }
    if (args == null || args.length == 0)
    {
      Console.error("Usage: <Filename> [<Filename> ...]");
    }
  }

}
