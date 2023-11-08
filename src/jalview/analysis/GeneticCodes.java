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
package jalview.analysis;

import java.util.Locale;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import jalview.bin.Console;

/**
 * A singleton that provides instances of genetic code translation tables
 * 
 * @author gmcarstairs
 * @see https://www.ncbi.nlm.nih.gov/Taxonomy/Utils/wprintgc.cgi
 */
public final class GeneticCodes
{
  private static final int CODON_LENGTH = 3;

  private static final String QUOTE = "\"";

  /*
   * nucleotides as ordered in data file
   */
  private static final String NUCS = "TCAG";

  private static final int NUCS_COUNT = NUCS.length();

  private static final int NUCS_COUNT_SQUARED = NUCS_COUNT * NUCS_COUNT;

  private static final int NUCS_COUNT_CUBED = NUCS_COUNT * NUCS_COUNT
          * NUCS_COUNT;

  private static final String AMBIGUITY_CODES_FILE = "/AmbiguityCodes.dat";

  private static final String RESOURCE_FILE = "/GeneticCodes.dat";

  private static GeneticCodes instance = new GeneticCodes();

  private Map<String, String> ambiguityCodes;

  /*
   * loaded code tables, with keys in order of loading 
   */
  private Map<String, GeneticCodeI> codeTables;

  /**
   * Private constructor enforces singleton
   */
  private GeneticCodes()
  {
    if (instance == null)
    {
      ambiguityCodes = new HashMap<>();

      /*
       * LinkedHashMap preserves order of addition of entries,
       * so we can assume the Standard Code Table is the first
       */
      codeTables = new LinkedHashMap<>();
      loadAmbiguityCodes(AMBIGUITY_CODES_FILE);
      loadCodes(RESOURCE_FILE);
    }
  }

  /**
   * Returns the singleton instance of this class
   * 
   * @return
   */
  public static GeneticCodes getInstance()
  {
    return instance;
  }

  /**
   * Returns the known code tables, in order of loading.
   * 
   * @return
   */
  public Iterable<GeneticCodeI> getCodeTables()
  {
    return codeTables.values();
  }

  /**
   * Answers the code table with the given id
   * 
   * @param id
   * @return
   */
  public GeneticCodeI getCodeTable(String id)
  {
    return codeTables.get(id);
  }

  /**
   * A convenience method that returns the standard code table (table 1). As
   * implemented, this has to be the first table defined in the data file.
   * 
   * @return
   */
  public GeneticCodeI getStandardCodeTable()
  {
    return codeTables.values().iterator().next();
  }

  /**
   * Loads the code tables from a data file
   */
  protected void loadCodes(String fileName)
  {
    try
    {
      InputStream is = getClass().getResourceAsStream(fileName);
      if (is == null)
      {
        System.err.println("Resource file not found: " + fileName);
        return;
      }
      BufferedReader dataIn = new BufferedReader(new InputStreamReader(is));

      /*
       * skip comments and start of table
       */
      String line = "";
      while (line != null && !line.startsWith("Genetic-code-table"))
      {
        line = readLine(dataIn);
      }
      line = readLine(dataIn);

      while (line.startsWith("{"))
      {
        line = loadOneTable(dataIn);
      }
    } catch (IOException | NullPointerException e)
    {
      Console.error("Error reading genetic codes data file " + fileName
              + ": " + e.getMessage());
    }
    if (codeTables.isEmpty())
    {
      System.err.println(
              "No genetic code tables loaded, check format of file "
                      + fileName);
    }
  }

  /**
   * Reads and saves Nucleotide ambiguity codes from a data file. The file may
   * include comment lines (starting with #), a header 'DNA', and one line per
   * ambiguity code, for example:
   * <p>
   * R&lt;tab&gt;AG
   * <p>
   * means that R is an ambiguity code meaning "A or G"
   * 
   * @param fileName
   */
  protected void loadAmbiguityCodes(String fileName)
  {
    try
    {
      InputStream is = getClass().getResourceAsStream(fileName);
      if (is == null)
      {
        System.err.println("Resource file not found: " + fileName);
        return;
      }
      BufferedReader dataIn = new BufferedReader(new InputStreamReader(is));
      String line = "";
      while (line != null)
      {
        line = readLine(dataIn);
        if (line != null && !"DNA".equals(line.toUpperCase(Locale.ROOT)))
        {
          String[] tokens = line.split("\\t");
          if (tokens.length == 2)
          {
            ambiguityCodes.put(tokens[0].toUpperCase(Locale.ROOT),
                    tokens[1].toUpperCase(Locale.ROOT));
          }
          else
          {
            System.err.println(
                    "Unexpected data in " + fileName + ": " + line);
          }
        }
      }
    } catch (IOException e)
    {
      Console.error("Error reading nucleotide ambiguity codes data file: "
              + e.getMessage());
    }
  }

  /**
   * Reads up to and returns the next non-comment line, trimmed. Comment lines
   * start with a #. Returns null at end of file.
   * 
   * @param dataIn
   * @return
   * @throws IOException
   */
  protected String readLine(BufferedReader dataIn) throws IOException
  {
    String line = dataIn.readLine();
    while (line != null && line.startsWith("#"))
    {
      line = readLine(dataIn);
    }
    return line == null ? null : line.trim();
  }

  /**
   * Reads the lines of the data file describing one translation table, and
   * creates and stores an instance of GeneticCodeI. Returns the '{' line
   * starting the next table, or the '}' line at end of all tables. Data format
   * is
   * 
   * <pre>
   * {
   *   name "Vertebrate Mitochondrial" ,
   *   name "SGC1" ,
   *   id 2 ,
   *   ncbieaa  "FFLLSSSSYY**CCWWLLLLPPPPHHQQRRRRIIMMTTTTNNKKSS**VVVVAAAADDEEGGGG",
   *   sncbieaa "----------**--------------------MMMM----------**---M------------"
   *   -- Base1  TTTTTTTTTTTTTTTTCCCCCCCCCCCCCCCCAAAAAAAAAAAAAAAAGGGGGGGGGGGGGGGG
   *   -- Base2  TTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGG
   *   -- Base3  TCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAG
   * },
   * </pre>
   * 
   * of which we parse the first name, the id, and the ncbieaa translations for
   * codons as ordered by the Base1/2/3 lines. Note Base1/2/3 are included for
   * readability and are in a fixed order, these are not parsed. The sncbieaa
   * line marks alternative start codons, these are not parsed.
   * 
   * @param dataIn
   * @return
   * @throws IOException
   */
  protected String loadOneTable(BufferedReader dataIn) throws IOException
  {
    String name = null;
    String id = null;
    Map<String, String> codons = new HashMap<>();

    String line = readLine(dataIn);

    while (line != null && !line.startsWith("}"))
    {
      if (line.startsWith("name") && name == null)
      {
        name = line.substring(line.indexOf(QUOTE) + 1,
                line.lastIndexOf(QUOTE));
      }
      else if (line.startsWith("id"))
      {
        id = new StringTokenizer(line.substring(2)).nextToken();
      }
      else if (line.startsWith("ncbieaa"))
      {
        String aminos = line.substring(line.indexOf(QUOTE) + 1,
                line.lastIndexOf(QUOTE));
        if (aminos.length() != NUCS_COUNT_CUBED) // 4 * 4 * 4 combinations
        {
          Console.error("wrong data length in code table: " + line);
        }
        else
        {
          for (int i = 0; i < aminos.length(); i++)
          {
            String peptide = String.valueOf(aminos.charAt(i));
            char codon1 = NUCS.charAt(i / NUCS_COUNT_SQUARED);
            char codon2 = NUCS
                    .charAt((i % NUCS_COUNT_SQUARED) / NUCS_COUNT);
            char codon3 = NUCS.charAt(i % NUCS_COUNT);
            String codon = new String(
                    new char[]
                    { codon1, codon2, codon3 });
            codons.put(codon, peptide);
          }
        }
      }
      line = readLine(dataIn);
    }

    registerCodeTable(id, name, codons);
    return readLine(dataIn);
  }

  /**
   * Constructs and registers a GeneticCodeI instance with the codon
   * translations as defined in the data file. For all instances except the
   * first, any undeclared translations default to those in the standard code
   * table.
   * 
   * @param id
   * @param name
   * @param codons
   */
  protected void registerCodeTable(final String id, final String name,
          final Map<String, String> codons)
  {
    codeTables.put(id, new GeneticCodeI()
    {
      /*
       * map of ambiguous codons to their 'product'
       * (null if not all possible translations match)
       */
      Map<String, String> ambiguous = new HashMap<>();

      @Override
      public String translateCanonical(String codon)
      {
        return codons.get(codon.toUpperCase(Locale.ROOT));
      }

      @Override
      public String translate(String codon)
      {
        String upper = codon.toUpperCase(Locale.ROOT);
        String peptide = translateCanonical(upper);

        /*
         * if still not translated, check for ambiguity codes
         */
        if (peptide == null)
        {
          peptide = getAmbiguousTranslation(upper, ambiguous, this);
        }
        return peptide;
      }

      @Override
      public String getId()
      {
        return id;
      }

      @Override
      public String getName()
      {
        return name;
      }
    });
  }

  /**
   * Computes all possible translations of a codon including one or more
   * ambiguity codes, and stores and returns the result (null if not all
   * translations match). If the codon includes no ambiguity codes, simply
   * returns null.
   * 
   * @param codon
   * @param ambiguous
   * @param codeTable
   * @return
   */
  protected String getAmbiguousTranslation(String codon,
          Map<String, String> ambiguous, GeneticCodeI codeTable)
  {
    if (codon.length() != CODON_LENGTH)
    {
      return null;
    }

    boolean isAmbiguous = false;

    char[][] expanded = new char[CODON_LENGTH][];
    for (int i = 0; i < CODON_LENGTH; i++)
    {
      String base = String.valueOf(codon.charAt(i));
      if (ambiguityCodes.containsKey(base))
      {
        isAmbiguous = true;
        base = ambiguityCodes.get(base);
      }
      expanded[i] = base.toCharArray();
    }

    if (!isAmbiguous)
    {
      // no ambiguity code involved here
      return null;
    }

    /*
     * generate and translate all permutations of the ambiguous codon
     * only return the translation if they all agree, else null
     */
    String peptide = null;
    for (char c1 : expanded[0])
    {
      for (char c2 : expanded[1])
      {
        for (char c3 : expanded[2])
        {
          char[] cdn = new char[] { c1, c2, c3 };
          String possibleCodon = String.valueOf(cdn);
          String pep = codeTable.translate(possibleCodon);
          if (pep == null || (peptide != null && !pep.equals(peptide)))
          {
            ambiguous.put(codon, null);
            return null;
          }
          peptide = pep;
        }
      }
    }

    /*
     * all translations of ambiguous codons matched!
     */
    ambiguous.put(codon, peptide);
    return peptide;
  }
}
