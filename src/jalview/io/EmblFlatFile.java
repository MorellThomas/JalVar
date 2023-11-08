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

import jalview.bin.Console;
import jalview.datamodel.DBRefEntry;
import jalview.util.DBRefUtils;

/**
 * A class that provides selective parsing of the EMBL flatfile format.
 * <p>
 * The initial implementation is limited to extracting fields used by Jalview
 * after fetching an EMBL or EMBLCDS entry:
 * 
 * <pre>
 * accession, version, sequence, xref
 * and (for CDS feature) location, protein_id, product, codon_start, translation
 * </pre>
 * 
 * For a complete parser, it may be best to adopt that provided in
 * https://github.com/enasequence/sequencetools/tree/master/src/main/java/uk/ac/ebi/embl/flatfile
 * (but note this has a dependency on the Apache Commons library)
 * 
 * @author gmcarstairs
 * @see ftp://ftp.ebi.ac.uk/pub/databases/ena/sequence/release/doc/usrman.txt
 * @see ftp://ftp.ebi.ac.uk/pub/databases/embl/doc/FT_current.html
 */
public class EmblFlatFile extends EMBLLikeFlatFile
{
  /**
   * Constructor given a data source and the id of the source database
   * 
   * @param fp
   * @param sourceId
   * @throws IOException
   */
  public EmblFlatFile(FileParse fp, String sourceId) throws IOException
  {
    super(fp, sourceId);
  }

  /**
   * Parses the flatfile, and if successful, saves as an annotated sequence
   * which may be retrieved by calling {@code getSequence()}
   * 
   * @throws IOException
   */
  @Override
  public void parse() throws IOException
  {
    String line = nextLine();
    while (line != null)
    {
      if (line.startsWith("ID"))
      {
        line = parseID(line);
      }
      else if (line.startsWith("DE"))
      {
        line = parseDE(line);
      }
      else if (line.startsWith("DR"))
      {
        line = parseDR(line);
      }
      else if (line.startsWith("SQ"))
      {
        line = parseSequence();
      }
      else if (line.startsWith("FT"))
      {
        line = parseFeature(line.substring(2));
      }
      else
      {
        line = nextLine();
      }
    }
    buildSequence();
  }

  /**
   * Extracts and saves the primary accession and version (SV value) from an ID
   * line, or null if not found. Returns the next line after the one processed.
   * 
   * @param line
   * @throws IOException
   */
  String parseID(String line) throws IOException
  {
    String[] tokens = line.substring(2).split(";");

    /*
     * first is primary accession
     */
    String token = tokens[0].trim();
    if (!token.isEmpty())
    {
      this.accession = token;
    }

    /*
     * second token is 'SV versionNo'
     */
    if (tokens.length > 1)
    {
      token = tokens[1].trim();
      if (token.startsWith("SV"))
      {
        String[] bits = token.trim().split(WHITESPACE);
        this.version = bits[bits.length - 1];
      }
    }

    /*
     * seventh token is 'length BP'
     */
    if (tokens.length > 6)
    {
      token = tokens[6].trim();
      String[] bits = token.trim().split(WHITESPACE);
      try
      {
        this.length = Integer.valueOf(bits[0]);
      } catch (NumberFormatException e)
      {
        Console.error("bad length read in flatfile, line: " + line);
      }
    }

    return nextLine();
  }

  /**
   * Reads sequence description from the first DE line found. Any trailing
   * period is discarded. If there are multiple DE lines, only the first (short
   * description) is read, the rest are ignored.
   * 
   * @param line
   * @return
   * @throws IOException
   */
  String parseDE(String line) throws IOException
  {
    String desc = line.substring(2).trim();
    if (desc.endsWith("."))
    {
      desc = desc.substring(0, desc.length() - 1);
    }
    this.description = desc;

    /*
     * pass over any additional DE lines
     */
    while ((line = nextLine()) != null)
    {
      if (!line.startsWith("DE"))
      {
        break;
      }
    }

    return line;
  }

  /**
   * Processes one DR line and saves as a DBRefEntry cross-reference. Returns
   * the line following the line processed.
   * 
   * @param line
   * @throws IOException
   */
  String parseDR(String line) throws IOException
  {
    String[] tokens = line.substring(2).split(";");
    if (tokens.length > 1)
    {
      /*
       * ensure UniProtKB/Swiss-Prot converted to UNIPROT
       */
      String db = tokens[0].trim();
      db = DBRefUtils.getCanonicalName(db);
      String acc = tokens[1].trim();
      if (acc.endsWith("."))
      {
        acc = acc.substring(0, acc.length() - 1);
      }
      String version = "0";
      if (tokens.length > 2)
      {
        String secondaryId = tokens[2].trim();
        if (!secondaryId.isEmpty())
        {
          // todo: is this right? secondary id is not a version number
          // version = secondaryId;
        }
      }
      this.dbrefs.add(new DBRefEntry(db, version, acc));
    }

    return nextLine();
  }

  @Override
  protected boolean isFeatureContinuationLine(String line)
  {
    return line.startsWith("FT    "); // 4 spaces
  }
}
