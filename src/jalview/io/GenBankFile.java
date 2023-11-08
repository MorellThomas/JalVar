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

/**
 * A class that provides selective parsing of the GenBank flatfile format.
 * <p>
 * The initial implementation is limited to extracting fields used by Jalview
 * after fetching an EMBL or EMBLCDS entry:
 * 
 * <pre>
 * accession, version, sequence, xref
 * and (for CDS feature) location, protein_id, product, codon_start, translation
 * </pre>
 * 
 * @author gmcarstairs
 * @see https://www.ncbi.nlm.nih.gov/Sitemap/samplerecord.html
 */
public class GenBankFile extends EMBLLikeFlatFile
{
  private static final String DEFINITION = "DEFINITION";

  /**
   * Constructor given a data source and the id of the source database
   * 
   * @param fp
   * @param sourceId
   * @throws IOException
   */
  public GenBankFile(FileParse fp, String sourceId) throws IOException
  {
    super(fp, sourceId);
  }

  /**
   * Parses the flatfile, and if successful, saves as an annotated sequence
   * which may be retrieved by calling {@code getSequence()}
   * 
   * @throws IOException
   * @see https://www.ncbi.nlm.nih.gov/Sitemap/samplerecord.html
   */
  @Override
  public void parse() throws IOException
  {
    String line = nextLine();
    while (line != null)
    {
      if (line.startsWith("LOCUS"))
      {
        line = parseLocus(line);
      }
      else if (line.startsWith(DEFINITION))
      {
        line = parseDefinition(line);
      }
      else if (line.startsWith("ACCESSION"))
      {
        this.accession = line.split(WHITESPACE)[1];
        line = nextLine();
      }
      else if (line.startsWith("VERSION"))
      {
        line = parseVersion(line);
      }
      else if (line.startsWith("ORIGIN"))
      {
        line = parseSequence();
      }
      else if (line.startsWith("FEATURES"))
      {
        line = nextLine();
        while (line.startsWith(" "))
        {
          line = parseFeature(line);
        }
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
  String parseLocus(String line) throws IOException
  {
    String[] tokens = line.split(WHITESPACE);

    /*
     * first should be "LOCUS"
     */
    if (tokens.length < 2 || !"LOCUS".equals(tokens[0]))
    {
      return nextLine();
    }
    /*
     * second is primary accession
     */
    String token = tokens[1].trim();
    if (!token.isEmpty())
    {
      this.accession = token;
    }

    // not going to guess the rest just yet, but third is length with unit (bp)

    return nextLine();
  }

  /**
   * Reads sequence description from DEFINITION lines. Any trailing period is
   * discarded. Returns the next line after the definition line(s).
   * 
   * @param line
   * @return
   * @throws IOException
   */
  String parseDefinition(String line) throws IOException
  {
    String desc = line.substring(DEFINITION.length()).trim();
    if (desc.endsWith("."))
    {
      desc = desc.substring(0, desc.length() - 1);
    }

    /*
     * pass over any additional DE lines
     */
    while ((line = nextLine()) != null)
    {
      if (line.startsWith(" "))
      {
        // definition continuation line
        desc += line.trim();
      }
      else
      {
        break;
      }
    }
    this.description = desc;

    return line;
  }

  /**
   * Parses the VERSION line e.g.
   * 
   * <pre>
   * VERSION     X81322.1
   * </pre>
   * 
   * and returns the next line
   * 
   * @param line
   * @throws IOException
   */
  String parseVersion(String line) throws IOException
  {
    /*
     * extract version part of <accession>.<version>
     * https://www.ncbi.nlm.nih.gov/Sitemap/samplerecord.html#VersionB
     */
    String[] tokens = line.split(WHITESPACE);
    if (tokens.length > 1)
    {
      tokens = tokens[1].split("\\.");
      if (tokens.length > 1)
      {
        this.version = tokens[1];
      }
    }

    return nextLine();
  }

  @Override
  protected boolean isFeatureContinuationLine(String line)
  {
    return line.startsWith("      "); // 6 spaces
  }
}
