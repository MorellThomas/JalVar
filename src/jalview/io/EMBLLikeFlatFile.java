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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import jalview.bin.Console;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.FeatureProperties;
import jalview.datamodel.Mapping;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.util.DBRefUtils;
import jalview.util.DnaUtils;
import jalview.util.MapList;
import jalview.util.MappingUtils;

/**
 * A base class to support parsing of GenBank, EMBL or DDBJ flat file format
 * data. Example files (rather than formal specifications) are provided at
 * 
 * <pre>
 * https://ena-docs.readthedocs.io/en/latest/submit/fileprep/flat-file-example.html
 * https://www.ncbi.nlm.nih.gov/Sitemap/samplerecord.html
 * </pre>
 * 
 * or to compare the same entry, see
 * 
 * <pre>
 * https://www.ebi.ac.uk/ena/browser/api/embl/X81322.1
 * https://www.ncbi.nlm.nih.gov/nuccore/X81322.1
 * </pre>
 * 
 * The feature table part of the file has a common definition, only the start of
 * each line is formatted differently in GenBank and EMBL. See
 * http://www.insdc.org/files/feature_table.html#7.1.
 */
public abstract class EMBLLikeFlatFile extends AlignFile
{
  protected static final String LOCATION = "location";

  protected static final String QUOTE = "\"";

  protected static final String DOUBLED_QUOTE = QUOTE + QUOTE;

  protected static final String WHITESPACE = "\\s+";

  /**
   * Removes leading or trailing double quotes (") unless doubled, and changes
   * any 'escaped' (doubled) double quotes to single characters. As per the
   * Feature Table specification for Qualifiers, Free Text.
   * 
   * @param value
   * @return
   */
  protected static String removeQuotes(String value)
  {
    if (value == null)
    {
      return null;
    }
    if (value.startsWith(QUOTE) && !value.startsWith(DOUBLED_QUOTE))
    {
      value = value.substring(1);
    }
    if (value.endsWith(QUOTE) && !value.endsWith(DOUBLED_QUOTE))
    {
      value = value.substring(0, value.length() - 1);
    }
    value = value.replace(DOUBLED_QUOTE, QUOTE);
    return value;
  }

  /**
   * Truncates (if necessary) the exon intervals to match 3 times the length of
   * the protein(including truncation for stop codon included in exon)
   * 
   * @param proteinLength
   * @param exon
   *          an array of [start, end, start, end...] intervals
   * @return the same array (if unchanged) or a truncated copy
   */
  protected static int[] adjustForProteinLength(int proteinLength,
          int[] exon)
  {
    if (proteinLength <= 0 || exon == null)
    {
      return exon;
    }
    int expectedCdsLength = proteinLength * 3;
    int exonLength = MappingUtils.getLength(Arrays.asList(exon));

    /*
     * if exon length matches protein, or is shorter, then leave it unchanged
     */
    if (expectedCdsLength >= exonLength)
    {
      return exon;
    }

    int origxon[];
    int sxpos = -1;
    int endxon = 0;
    origxon = new int[exon.length];
    System.arraycopy(exon, 0, origxon, 0, exon.length);
    int cdspos = 0;
    for (int x = 0; x < exon.length; x += 2)
    {
      cdspos += Math.abs(exon[x + 1] - exon[x]) + 1;
      if (expectedCdsLength <= cdspos)
      {
        // advanced beyond last codon.
        sxpos = x;
        if (expectedCdsLength != cdspos)
        {
          // System.err
          // .println("Truncating final exon interval on region by "
          // + (cdspos - cdslength));
        }

        /*
         * shrink the final exon - reduce end position if forward
         * strand, increase it if reverse
         */
        if (exon[x + 1] >= exon[x])
        {
          endxon = exon[x + 1] - cdspos + expectedCdsLength;
        }
        else
        {
          endxon = exon[x + 1] + cdspos - expectedCdsLength;
        }
        break;
      }
    }

    if (sxpos != -1)
    {
      // and trim the exon interval set if necessary
      int[] nxon = new int[sxpos + 2];
      System.arraycopy(exon, 0, nxon, 0, sxpos + 2);
      nxon[sxpos + 1] = endxon; // update the end boundary for the new exon
                                // set
      exon = nxon;
    }
    return exon;
  }

  /*
   * when true, interpret the mol_type 'source' feature attribute
   * and generate an RNA sequence from the DNA record
   */
  protected boolean produceRna = true;

  /*
   * values parsed from the data file
   */
  protected String sourceDb;

  protected String accession;

  protected String version;

  protected String description;

  protected int length = 128;

  protected List<DBRefEntry> dbrefs;

  protected boolean sequenceStringIsRNA = false;

  protected String sequenceString;

  protected Map<String, CdsData> cds;

  /**
   * Constructor
   * 
   * @param fp
   * @param sourceId
   * @throws IOException
   */
  public EMBLLikeFlatFile(FileParse fp, String sourceId) throws IOException
  {
    super(false, fp); // don't parse immediately
    this.sourceDb = sourceId;
    dbrefs = new ArrayList<>();

    /*
     * using TreeMap gives CDS sequences in alphabetical, so readable, order
     */
    cds = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    parse();
  }

  /**
   * process attributes for 'source' until the next FT feature entry only
   * interested in 'mol_type'
   * 
   * @param tokens
   * @return
   * @throws IOException
   */
  private String parseSourceQualifiers(String[] tokens) throws IOException
  {
    if (!"source".equals(tokens[0]))
    {
      throw (new RuntimeException("Not given a 'source' qualifier line"));
    }
    // search for mol_type attribute

    StringBuilder sb = new StringBuilder().append(tokens[1]); // extent of
                                                              // sequence

    String line = parseFeatureQualifier(sb, false);
    while (line != null)
    {
      if (!line.startsWith("FT    ")) // four spaces, end of this feature table
                                      // entry
      {
        return line;
      }

      // case sensitive ?
      int p = line.indexOf("\\mol_type");
      int qs = line.indexOf("\"", p);
      int qe = line.indexOf("\"", qs + 1);
      String qualifier = line.substring(qs, qe).toLowerCase(Locale.ROOT);
      if (qualifier.indexOf("rna") > -1)
      {
        sequenceStringIsRNA = true;
      }
      if (qualifier.indexOf("dna") > -1)
      {
        sequenceStringIsRNA = false;
      }
      line = parseFeatureQualifier(sb, false);
    }
    return line;
  }

  /**
   * Parses one (GenBank or EMBL format) CDS feature, saves the parsed data, and
   * returns the next line
   * 
   * @param location
   * @return
   * @throws IOException
   */
  protected String parseCDSFeature(String location) throws IOException
  {
    String line;

    /*
     * parse location, which can be over >1 line e.g. EAW51554
     */
    CdsData data = new CdsData();
    StringBuilder sb = new StringBuilder().append(location);
    line = parseFeatureQualifier(sb, false);
    data.cdsLocation = sb.toString();

    while (line != null)
    {
      if (!isFeatureContinuationLine(line))
      {
        // e.g. start of next feature "FT source..."
        break;
      }

      /*
       * extract qualifier, e.g. FT    /protein_id="CAA37824.1"
       * - the value may extend over more than one line
       * - if the value has enclosing quotes, these are removed
       * - escaped double quotes ("") are reduced to a single character
       */
      int slashPos = line.indexOf('/');
      if (slashPos == -1)
      {
        Console.error("Unexpected EMBL line ignored: " + line);
        line = nextLine();
        continue;
      }
      int eqPos = line.indexOf('=', slashPos + 1);
      if (eqPos == -1)
      {
        // can happen, e.g. /ribosomal_slippage
        line = nextLine();
        continue;
      }
      String qualifier = line.substring(slashPos + 1, eqPos);
      String value = line.substring(eqPos + 1);
      value = removeQuotes(value);
      sb = new StringBuilder().append(value);
      boolean asText = !"translation".equals(qualifier);
      line = parseFeatureQualifier(sb, asText);
      String featureValue = sb.toString();

      if ("protein_id".equals(qualifier))
      {
        data.proteinId = featureValue;
      }
      else if ("codon_start".equals(qualifier))
      {
        try
        {
          data.codonStart = Integer.parseInt(featureValue.trim());
        } catch (NumberFormatException e)
        {
          Console.error("Invalid codon_start in XML for " + this.accession
                  + ": " + e.getMessage());
        }
      }
      else if ("db_xref".equals(qualifier))
      {
        String[] parts = featureValue.split(":");
        if (parts.length == 2)
        {
          String db = parts[0].trim();
          db = DBRefUtils.getCanonicalName(db);
          DBRefEntry dbref = new DBRefEntry(db, "0", parts[1].trim());
          data.xrefs.add(dbref);
        }
      }
      else if ("product".equals(qualifier))
      {
        data.proteinName = featureValue;
      }
      else if ("translation".equals(qualifier))
      {
        data.translation = featureValue;
      }
      else if (!"".equals(featureValue))
      {
        // throw anything else into the additional properties hash
        data.cdsProps.put(qualifier, featureValue);
      }
    }

    if (data.proteinId != null)
    {
      this.cds.put(data.proteinId, data);
    }
    else
    {
      Console.error("Ignoring CDS feature with no protein_id for "
              + sourceDb + ":" + accession);
    }

    return line;
  }

  protected abstract boolean isFeatureContinuationLine(String line);

  /**
   * Output (print) is not (yet) implemented for flat file format
   */
  @Override
  public String print(SequenceI[] seqs, boolean jvsuffix)
  {
    return null;
  }

  /**
   * Constructs and saves the sequence from parsed components
   */
  protected void buildSequence()
  {
    if (this.accession == null || this.sequenceString == null)
    {
      Console.error("Failed to parse data from EMBL");
      return;
    }

    String name = this.accession;
    if (this.sourceDb != null)
    {
      name = this.sourceDb + "|" + name;
    }

    if (produceRna && sequenceStringIsRNA)
    {
      sequenceString = sequenceString.replace('T', 'U').replace('t', 'u');
    }

    SequenceI seq = new Sequence(name, this.sequenceString);
    seq.setDescription(this.description);

    /*
     * add a DBRef to itself
     */
    DBRefEntry selfRef = new DBRefEntry(sourceDb, version, accession);
    int[] startEnd = new int[] { 1, seq.getLength() };
    selfRef.setMap(new Mapping(null, startEnd, startEnd, 1, 1));
    seq.addDBRef(selfRef);

    for (DBRefEntry dbref : this.dbrefs)
    {
      seq.addDBRef(dbref);
    }

    processCDSFeatures(seq);

    seq.deriveSequence();

    addSequence(seq);
  }

  /**
   * Process the CDS features, including generation of cross-references and
   * mappings to the protein products (translation)
   * 
   * @param seq
   */
  protected void processCDSFeatures(SequenceI seq)
  {
    /*
     * record protein products found to avoid duplication i.e. >1 CDS with 
     * the same /protein_id [though not sure I can find an example of this]
     */
    Map<String, SequenceI> proteins = new HashMap<>();
    for (CdsData data : cds.values())
    {
      processCDSFeature(seq, data, proteins);
    }
  }

  /**
   * Processes data for one parsed CDS feature to
   * <ul>
   * <li>create a protein product sequence for the translation</li>
   * <li>create a cross-reference to protein with mapping from dna</li>
   * <li>add a CDS feature to the sequence for each CDS start-end range</li>
   * <li>add any CDS dbrefs to the sequence and to the protein product</li>
   * </ul>
   * 
   * @param SequenceI
   *          dna
   * @param proteins
   *          map of protein products so far derived from CDS data
   */
  void processCDSFeature(SequenceI dna, CdsData data,
          Map<String, SequenceI> proteins)
  {
    /*
     * parse location into a list of [start, end, start, end] positions
     */
    int[] exons = getCdsRanges(this.accession, data.cdsLocation);

    MapList maplist = buildMappingToProtein(dna, exons, data);

    int exonNumber = 0;

    for (int xint = 0; exons != null && xint < exons.length - 1; xint += 2)
    {
      int exonStart = exons[xint];
      int exonEnd = exons[xint + 1];
      int begin = Math.min(exonStart, exonEnd);
      int end = Math.max(exonStart, exonEnd);
      exonNumber++;
      String desc = String.format("Exon %d for protein EMBLCDS:%s",
              exonNumber, data.proteinId);

      SequenceFeature sf = new SequenceFeature("CDS", desc, begin, end,
              this.sourceDb);
      for (Entry<String, String> val : data.cdsProps.entrySet())
      {
        sf.setValue(val.getKey(), val.getValue());
      }

      sf.setEnaLocation(data.cdsLocation);
      boolean forwardStrand = exonStart <= exonEnd;
      sf.setStrand(forwardStrand ? "+" : "-");
      sf.setPhase(String.valueOf(data.codonStart - 1));
      sf.setValue(FeatureProperties.EXONPOS, exonNumber);
      sf.setValue(FeatureProperties.EXONPRODUCT, data.proteinName);

      dna.addSequenceFeature(sf);
    }

    boolean hasUniprotDbref = false;
    for (DBRefEntry xref : data.xrefs)
    {
      dna.addDBRef(xref);
      if (xref.getSource().equals(DBRefSource.UNIPROT))
      {
        /*
         * construct (or find) the sequence for (data.protein_id, data.translation)
         */
        SequenceI protein = buildProteinProduct(dna, xref, data, proteins);
        Mapping map = new Mapping(protein, maplist);
        map.setMappedFromId(data.proteinId);
        xref.setMap(map);

        /*
         * add DBRefs with mappings from dna to protein and the inverse
         */
        DBRefEntry db1 = new DBRefEntry(sourceDb, version, accession);
        db1.setMap(new Mapping(dna, maplist.getInverse()));
        protein.addDBRef(db1);

        hasUniprotDbref = true;
      }
    }

    /*
     * if we have a product (translation) but no explicit Uniprot dbref
     * (example: EMBL M19487 protein_id AAB02592.1)
     * then construct mappings to an assumed EMBLCDSPROTEIN accession
     */
    if (!hasUniprotDbref)
    {
      SequenceI protein = proteins.get(data.proteinId);
      if (protein == null)
      {
        protein = new Sequence(data.proteinId, data.translation);
        protein.setDescription(data.proteinName);
        proteins.put(data.proteinId, protein);
      }
      // assuming CDSPROTEIN sequence version = dna version (?!)
      DBRefEntry db1 = new DBRefEntry(DBRefSource.EMBLCDSProduct,
              this.version, data.proteinId);
      protein.addDBRef(db1);

      DBRefEntry dnaToEmblProteinRef = new DBRefEntry(
              DBRefSource.EMBLCDSProduct, this.version, data.proteinId);
      Mapping map = new Mapping(protein, maplist);
      map.setMappedFromId(data.proteinId);
      dnaToEmblProteinRef.setMap(map);
      dna.addDBRef(dnaToEmblProteinRef);
    }

    /*
     * comment brought forward from EmblXmlSource, lines 447-451:
     * TODO: if retrieved from EMBLCDS, add a DBRef back to the parent EMBL
     * sequence with the exon  map; if given a dataset reference, search
     * dataset for parent EMBL sequence if it exists and set its map;
     * make a new feature annotating the coding contig
     */
  }

  /**
   * Computes a mapping from CDS positions in DNA sequence to protein product
   * positions, with allowance for stop codon or incomplete start codon
   * 
   * @param dna
   * @param exons
   * @param data
   * @return
   */
  MapList buildMappingToProtein(final SequenceI dna, final int[] exons,
          final CdsData data)
  {
    MapList dnaToProteinMapping = null;
    int peptideLength = data.translation.length();

    int[] proteinRange = new int[] { 1, peptideLength };
    if (exons != null && exons.length > 0)
    {
      /*
       * We were able to parse 'location'; do a final 
       * product length truncation check
       */
      int[] cdsRanges = adjustForProteinLength(peptideLength, exons);
      dnaToProteinMapping = new MapList(cdsRanges, proteinRange, 3, 1);
    }
    else
    {
      /*
       * workaround until we handle all 'location' formats fully
       * e.g. X53828.1:60..1058 or <123..>289
       */
      Console.error(String.format(
              "Implementation Notice: EMBLCDS location '%s'not properly supported yet"
                      + " - Making up the CDNA region of (%s:%s)... may be incorrect",
              data.cdsLocation, sourceDb, this.accession));

      int completeCodonsLength = 1 - data.codonStart + dna.getLength();
      int mappedDnaEnd = dna.getEnd();
      if (peptideLength * 3 == completeCodonsLength)
      {
        // this might occur for CDS sequences where no features are marked
        Console.warn("Assuming no stop codon at end of cDNA fragment");
        mappedDnaEnd = dna.getEnd();
      }
      else if ((peptideLength + 1) * 3 == completeCodonsLength)
      {
        Console.warn("Assuming stop codon at end of cDNA fragment");
        mappedDnaEnd = dna.getEnd() - 3;
      }

      if (mappedDnaEnd != -1)
      {
        int[] cdsRanges = new int[] {
            dna.getStart() + (data.codonStart - 1), mappedDnaEnd };
        dnaToProteinMapping = new MapList(cdsRanges, proteinRange, 3, 1);
      }
    }

    return dnaToProteinMapping;
  }

  /**
   * Constructs a sequence for the protein product for the CDS data (if there is
   * one), and dbrefs with mappings from CDS to protein and the reverse
   * 
   * @param dna
   * @param xref
   * @param data
   * @param proteins
   * @return
   */
  SequenceI buildProteinProduct(SequenceI dna, DBRefEntry xref,
          CdsData data, Map<String, SequenceI> proteins)
  {
    /*
     * check we have some data to work with
     */
    if (data.proteinId == null || data.translation == null)
    {
      return null;
    }

    /*
     * Construct the protein sequence (if not already seen)
     */
    String proteinSeqName = xref.getSource() + "|" + xref.getAccessionId();
    SequenceI protein = proteins.get(proteinSeqName);
    if (protein == null)
    {
      protein = new Sequence(proteinSeqName, data.translation, 1,
              data.translation.length());
      protein.setDescription(data.proteinName != null ? data.proteinName
              : "Protein Product from " + sourceDb);
      proteins.put(proteinSeqName, protein);
    }

    return protein;
  }

  /**
   * Returns the CDS location as a single array of [start, end, start, end...]
   * positions. If on the reverse strand, these will be in descending order.
   * 
   * @param accession
   * @param location
   * @return
   */
  protected int[] getCdsRanges(String accession, String location)
  {
    if (location == null)
    {
      return new int[] {};
    }

    try
    {
      List<int[]> ranges = DnaUtils.parseLocation(location);
      return MappingUtils.rangeListToArray(ranges);
    } catch (ParseException e)
    {
      Console.warn(
              String.format("Not parsing inexact CDS location %s in ENA %s",
                      location, accession));
      return new int[] {};
    }
  }

  /**
   * Reads the value of a feature (FT) qualifier from one or more lines of the
   * file, and returns the next line after that. Values are appended to the
   * string buffer, which should be already primed with the value read from the
   * first line for the qualifier (with any leading double quote removed).
   * Enclosing double quotes are removed, and escaped (repeated) double quotes
   * reduced to one only. For example for
   * 
   * <pre>
   * FT      /note="gene_id=hCG28070.3 
   * FT      ""foobar"" isoform=CRA_b"
   * the returned value is
   * gene_id=hCG28070.3 "foobar" isoform=CRA_b
   * </pre>
   * 
   * Note the side-effect of this method, to advance data reading to the next
   * line after the feature qualifier (which could be another qualifier, a
   * different feature, a non-feature line, or null at end of file).
   * 
   * @param sb
   *          a string buffer primed with the first line of the value
   * @param asText
   * @return
   * @throws IOException
   */
  String parseFeatureQualifier(StringBuilder sb, boolean asText)
          throws IOException
  {
    String line;
    while ((line = nextLine()) != null)
    {
      if (!isFeatureContinuationLine(line))
      {
        break; // reached next feature or other input line
      }
      String[] tokens = line.split(WHITESPACE);
      if (tokens.length < 2)
      {
        Console.error("Ignoring bad EMBL line for " + this.accession + ": "
                + line);
        break;
      }
      if (tokens[1].startsWith("/"))
      {
        break; // next feature qualifier
      }

      /*
       * if text (e.g. /product), add a word separator for a new line,
       * else (e.g. /translation) don't
       */
      if (asText)
      {
        sb.append(" ");
      }

      /*
       * remove trailing " and unescape doubled ""
       */
      String data = removeQuotes(tokens[1]);
      sb.append(data);
    }

    return line;
  }

  /**
   * Reads and saves the sequence, read from the lines following the ORIGIN
   * (GenBank) or SQ (EMBL) line. Whitespace and position counters are
   * discarded. Returns the next line following the sequence data (the next line
   * that doesn't start with whitespace).
   * 
   * @throws IOException
   */
  protected String parseSequence() throws IOException
  {
    StringBuilder sb = new StringBuilder(this.length);
    String line = nextLine();
    while (line != null && line.startsWith(" "))
    {
      line = line.trim();
      String[] blocks = line.split(WHITESPACE);

      /*
       * the first or last block on each line might be a position count - omit
       */
      for (int i = 0; i < blocks.length; i++)
      {
        try
        {
          Long.parseLong(blocks[i]);
          // position counter - ignore it
        } catch (NumberFormatException e)
        {
          // sequence data - append it
          sb.append(blocks[i]);
        }
      }
      line = nextLine();
    }
    this.sequenceString = sb.toString();

    return line;
  }

  /**
   * Processes a feature line. If it declares a feature type of interest
   * (currently, only CDS is processed), processes all of the associated lines
   * (feature qualifiers), and returns the next line after that, otherwise
   * simply returns the next line.
   * 
   * @param line
   *          the first line for the feature (with initial FT omitted for EMBL
   *          format)
   * @return
   * @throws IOException
   */
  protected String parseFeature(String line) throws IOException
  {
    String[] tokens = line.trim().split(WHITESPACE);
    if (tokens.length < 2
            || (!"CDS".equals(tokens[0]) && (!"source".equals(tokens[0]))))
    {
      return nextLine();
    }
    if (tokens[0].equals("source"))
    {
      return parseSourceQualifiers(tokens);
    }
    return parseCDSFeature(tokens[1]);
  }
}

/**
 * A data bean class to hold values parsed from one CDS Feature
 */
class CdsData
{
  String translation; // from /translation qualifier

  String cdsLocation; // the raw value e.g. join(1..1234,2012..2837)

  int codonStart = 1; // from /codon_start qualifier

  String proteinName; // from /product qualifier; used for protein description

  String proteinId; // from /protein_id qualifier

  List<DBRefEntry> xrefs = new ArrayList<>(); // from /db_xref qualifiers

  Map<String, String> cdsProps = new Hashtable<>(); // other qualifiers
}
