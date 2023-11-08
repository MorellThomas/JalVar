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
package jalview.ws.dbsources;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.stevesoft.pat.Regex;

import jalview.analysis.SequenceIdMatcher;
import jalview.bin.Console;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
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
import jalview.ws.ebi.EBIFetchClient;
import jalview.xml.binding.embl.EntryType;
import jalview.xml.binding.embl.EntryType.Feature;
import jalview.xml.binding.embl.EntryType.Feature.Qualifier;
import jalview.xml.binding.embl.ROOT;
import jalview.xml.binding.embl.XrefType;

public abstract class EmblXmlSource extends EbiFileRetrievedProxy
{
  private static final Regex ACCESSION_REGEX = new Regex("^[A-Z]+[0-9]+");

  /*
   * JAL-1856 Embl returns this text for query not found
   */
  private static final String EMBL_NOT_FOUND_REPLY = "ERROR 12 No entries found.";

  public EmblXmlSource()
  {
    super();
  }

  /**
   * Retrieves and parses an emblxml file, and returns an alignment containing
   * the parsed sequences, or null if none were found
   * 
   * @param emprefx
   *          "EMBL" or "EMBLCDS" - anything else will not retrieve emblxml
   * @param query
   * @return
   * @throws Exception
   */
  protected AlignmentI getEmblSequenceRecords(String emprefx, String query)
          throws Exception
  {
    startQuery();
    EBIFetchClient dbFetch = new EBIFetchClient();
    File reply;
    try
    {
      reply = dbFetch.fetchDataAsFile(
              emprefx.toLowerCase(Locale.ROOT) + ":" + query.trim(),
              "display=xml", "xml");
    } catch (Exception e)
    {
      stopQuery();
      throw new Exception(
              String.format("EBI EMBL XML retrieval failed for %s:%s",
                      emprefx.toLowerCase(Locale.ROOT), query.trim()),
              e);
    }
    return getEmblSequenceRecords(emprefx, query, reply);
  }

  /**
   * parse an emblxml file stored locally
   * 
   * @param emprefx
   *          either EMBL or EMBLCDS strings are allowed - anything else will
   *          not retrieve emblxml
   * @param query
   * @param file
   *          the EMBL XML file containing the results of a query
   * @return
   * @throws Exception
   */
  protected AlignmentI getEmblSequenceRecords(String emprefx, String query,
          File reply) throws Exception
  {
    List<EntryType> entries = null;
    if (reply != null && reply.exists())
    {
      file = reply.getAbsolutePath();
      if (reply.length() > EMBL_NOT_FOUND_REPLY.length())
      {
        InputStream is = new FileInputStream(reply);
        entries = getEmblEntries(is);
      }
    }

    /*
     * invalid accession gets a reply with no <entry> elements, text content of
     * EmbFile reads something like (e.g.) this ungrammatical phrase
     * Entry: <acc> display type is either not supported or entry is not found.
     */
    AlignmentI al = null;
    List<SequenceI> seqs = new ArrayList<>();
    List<SequenceI> peptides = new ArrayList<>();
    if (entries != null)
    {
      for (EntryType entry : entries)
      {
        SequenceI seq = getSequence(emprefx, entry, peptides);
        if (seq != null)
        {
          seqs.add(seq.deriveSequence());
          // place DBReferences on dataset and refer
        }
      }
      if (!seqs.isEmpty())
      {
        al = new Alignment(seqs.toArray(new SequenceI[seqs.size()]));
      }
      else
      {
        System.out.println(
                "No record found for '" + emprefx + ":" + query + "'");
      }
    }

    stopQuery();
    return al;
  }

  /**
   * Reads the XML reply from file and unmarshals it to Java objects. Answers a
   * (possibly empty) list of <code>EntryType</code> objects.
   * 
   * is
   * 
   * @return
   */
  List<EntryType> getEmblEntries(InputStream is)
  {
    List<EntryType> entries = new ArrayList<>();
    try
    {
      JAXBContext jc = JAXBContext.newInstance("jalview.xml.binding.embl");
      XMLStreamReader streamReader = XMLInputFactory.newInstance()
              .createXMLStreamReader(is);
      javax.xml.bind.Unmarshaller um = jc.createUnmarshaller();
      JAXBElement<ROOT> rootElement = um.unmarshal(streamReader,
              ROOT.class);
      ROOT root = rootElement.getValue();

      /*
       * document root contains either "entry" or "entrySet"
       */
      if (root == null)
      {
        return entries;
      }
      if (root.getEntrySet() != null)
      {
        entries = root.getEntrySet().getEntry();
      }
      else if (root.getEntry() != null)
      {
        entries.add(root.getEntry());
      }
    } catch (JAXBException | XMLStreamException
            | FactoryConfigurationError e)
    {
      e.printStackTrace();
    }
    return entries;
  }

  /**
   * A helper method to parse XML data and construct a sequence, with any
   * available database references and features
   * 
   * @param emprefx
   * @param entry
   * @param peptides
   * @return
   */
  SequenceI getSequence(String sourceDb, EntryType entry,
          List<SequenceI> peptides)
  {
    String seqString = entry.getSequence();
    if (seqString == null)
    {
      return null;
    }
    seqString = seqString.replace(" ", "").replace("\n", "").replace("\t",
            "");
    String accession = entry.getAccession();
    SequenceI dna = new Sequence(sourceDb + "|" + accession, seqString);

    dna.setDescription(entry.getDescription());
    String sequenceVersion = String.valueOf(entry.getVersion().intValue());
    DBRefEntry selfRref = new DBRefEntry(sourceDb, sequenceVersion,
            accession);
    dna.addDBRef(selfRref);
    selfRref.setMap(
            new Mapping(null, new int[]
            { 1, dna.getLength() }, new int[] { 1, dna.getLength() }, 1,
                    1));

    /*
     * add db references
     */
    List<XrefType> xrefs = entry.getXref();
    if (xrefs != null)
    {
      for (XrefType xref : xrefs)
      {
        String acc = xref.getId();
        String source = DBRefUtils.getCanonicalName(xref.getDb());
        String version = xref.getSecondaryId();
        if (version == null || "".equals(version))
        {
          version = "0";
        }
        dna.addDBRef(new DBRefEntry(source, version, acc));
      }
    }

    SequenceIdMatcher matcher = new SequenceIdMatcher(peptides);
    try
    {
      List<Feature> features = entry.getFeature();
      if (features != null)
      {
        for (Feature feature : features)
        {
          if (FeatureProperties.isCodingFeature(sourceDb,
                  feature.getName()))
          {
            parseCodingFeature(entry, feature, sourceDb, dna, peptides,
                    matcher);
          }
        }
      }
    } catch (Exception e)
    {
      System.err.println("EMBL Record Features parsing error!");
      System.err
              .println("Please report the following to help@jalview.org :");
      System.err.println("EMBL Record " + accession);
      System.err.println("Resulted in exception: " + e.getMessage());
      e.printStackTrace(System.err);
    }

    return dna;
  }

  /**
   * Extracts coding region and product from a CDS feature and decorates it with
   * annotations
   * 
   * @param entry
   * @param feature
   * @param sourceDb
   * @param dna
   * @param peptides
   * @param matcher
   */
  void parseCodingFeature(EntryType entry, Feature feature, String sourceDb,
          SequenceI dna, List<SequenceI> peptides,
          SequenceIdMatcher matcher)
  {
    final boolean isEmblCdna = sourceDb.equals(DBRefSource.EMBLCDS);
    final String accession = entry.getAccession();
    final String sequenceVersion = entry.getVersion().toString();

    int[] exons = getCdsRanges(entry.getAccession(), feature);

    String translation = null;
    String proteinName = "";
    String proteinId = null;
    Map<String, String> vals = new Hashtable<>();

    /*
     * codon_start 1/2/3 in EMBL corresponds to phase 0/1/2 in CDS
     * (phase is required for CDS features in GFF3 format)
     */
    int codonStart = 1;

    /*
     * parse qualifiers, saving protein translation, protein id,
     * codon start position, product (name), and 'other values'
     */
    if (feature.getQualifier() != null)
    {
      for (Qualifier q : feature.getQualifier())
      {
        String qname = q.getName();
        String value = q.getValue();
        value = value == null ? ""
                : value.trim().replace(" ", "").replace("\n", "")
                        .replace("\t", "");
        if (qname.equals("translation"))
        {
          translation = value;
        }
        else if (qname.equals("protein_id"))
        {
          proteinId = value;
        }
        else if (qname.equals("codon_start"))
        {
          try
          {
            codonStart = Integer.parseInt(value.trim());
          } catch (NumberFormatException e)
          {
            System.err.println("Invalid codon_start in XML for "
                    + entry.getAccession() + ": " + e.getMessage());
          }
        }
        else if (qname.equals("product"))
        {
          // sometimes name is returned e.g. for V00488
          proteinName = value;
        }
        else
        {
          // throw anything else into the additional properties hash
          if (!"".equals(value))
          {
            vals.put(qname, value);
          }
        }
      }
    }

    DBRefEntry proteinToEmblProteinRef = null;
    exons = MappingUtils.removeStartPositions(codonStart - 1, exons);

    SequenceI product = null;
    Mapping dnaToProteinMapping = null;
    if (translation != null && proteinName != null && proteinId != null)
    {
      int translationLength = translation.length();

      /*
       * look for product in peptides list, if not found, add it
       */
      product = matcher.findIdMatch(proteinId);
      if (product == null)
      {
        product = new Sequence(proteinId, translation, 1,
                translationLength);
        product.setDescription(((proteinName.length() == 0)
                ? "Protein Product from " + sourceDb
                : proteinName));
        peptides.add(product);
        matcher.add(product);
      }

      // we have everything - create the mapping and perhaps the protein
      // sequence
      if (exons == null || exons.length == 0)
      {
        /*
         * workaround until we handle dna location for CDS sequence
         * e.g. location="X53828.1:60..1058" correctly
         */
        System.err.println(
                "Implementation Notice: EMBLCDS records not properly supported yet - Making up the CDNA region of this sequence... may be incorrect ("
                        + sourceDb + ":" + entry.getAccession() + ")");
        int dnaLength = dna.getLength();
        if (translationLength * 3 == (1 - codonStart + dnaLength))
        {
          System.err.println(
                  "Not allowing for additional stop codon at end of cDNA fragment... !");
          // this might occur for CDS sequences where no features are marked
          exons = new int[] { dna.getStart() + (codonStart - 1),
              dna.getEnd() };
          dnaToProteinMapping = new Mapping(product, exons,
                  new int[]
                  { 1, translationLength }, 3, 1);
        }
        if ((translationLength + 1) * 3 == (1 - codonStart + dnaLength))
        {
          System.err.println(
                  "Allowing for additional stop codon at end of cDNA fragment... will probably cause an error in VAMSAs!");
          exons = new int[] { dna.getStart() + (codonStart - 1),
              dna.getEnd() - 3 };
          dnaToProteinMapping = new Mapping(product, exons,
                  new int[]
                  { 1, translationLength }, 3, 1);
        }
      }
      else
      {
        // Trim the exon mapping if necessary - the given product may only be a
        // fragment of a larger protein. (EMBL:AY043181 is an example)

        if (isEmblCdna)
        {
          // TODO: Add a DbRef back to the parent EMBL sequence with the exon
          // map
          // if given a dataset reference, search dataset for parent EMBL
          // sequence if it exists and set its map
          // make a new feature annotating the coding contig
        }
        else
        {
          // final product length truncation check
          int[] exons2 = adjustForProteinLength(translationLength, exons);
          dnaToProteinMapping = new Mapping(product, exons2,
                  new int[]
                  { 1, translationLength }, 3, 1);
          if (product != null)
          {
            /*
             * make xref with mapping from protein to EMBL dna
             */
            DBRefEntry proteinToEmblRef = new DBRefEntry(DBRefSource.EMBL,
                    sequenceVersion, proteinId,
                    new Mapping(dnaToProteinMapping.getMap().getInverse()));
            product.addDBRef(proteinToEmblRef);

            /*
             * make xref from protein to EMBLCDS; we assume here that the 
             * CDS sequence version is same as dna sequence (?!)
             */
            MapList proteinToCdsMapList = new MapList(
                    new int[]
                    { 1, translationLength },
                    new int[]
                    { 1 + (codonStart - 1),
                        (codonStart - 1) + 3 * translationLength },
                    1, 3);
            DBRefEntry proteinToEmblCdsRef = new DBRefEntry(
                    DBRefSource.EMBLCDS, sequenceVersion, proteinId,
                    new Mapping(proteinToCdsMapList));
            product.addDBRef(proteinToEmblCdsRef);

            /*
             * make 'direct' xref from protein to EMBLCDSPROTEIN
             */
            proteinToEmblProteinRef = new DBRefEntry(proteinToEmblCdsRef);
            proteinToEmblProteinRef.setSource(DBRefSource.EMBLCDSProduct);
            proteinToEmblProteinRef.setMap(null);
            product.addDBRef(proteinToEmblProteinRef);
          }
        }
      }

      /*
       * add cds features to dna sequence
       */
      String cds = feature.getName(); // "CDS"
      for (int xint = 0; exons != null
              && xint < exons.length - 1; xint += 2)
      {
        int exonStart = exons[xint];
        int exonEnd = exons[xint + 1];
        int begin = Math.min(exonStart, exonEnd);
        int end = Math.max(exonStart, exonEnd);
        int exonNumber = xint / 2 + 1;
        String desc = String.format("Exon %d for protein '%s' EMBLCDS:%s",
                exonNumber, proteinName, proteinId);

        SequenceFeature sf = makeCdsFeature(cds, desc, begin, end, sourceDb,
                vals);

        sf.setEnaLocation(feature.getLocation());
        boolean forwardStrand = exonStart <= exonEnd;
        sf.setStrand(forwardStrand ? "+" : "-");
        sf.setPhase(String.valueOf(codonStart - 1));
        sf.setValue(FeatureProperties.EXONPOS, exonNumber);
        sf.setValue(FeatureProperties.EXONPRODUCT, proteinName);

        dna.addSequenceFeature(sf);
      }
    }

    /*
     * add feature dbRefs to sequence, and mappings for Uniprot xrefs
     */
    boolean hasUniprotDbref = false;
    List<XrefType> xrefs = feature.getXref();
    if (xrefs != null)
    {
      boolean mappingUsed = false;
      for (XrefType xref : xrefs)
      {
        /*
         * ensure UniProtKB/Swiss-Prot converted to UNIPROT
         */
        String source = DBRefUtils.getCanonicalName(xref.getDb());
        String version = xref.getSecondaryId();
        if (version == null || "".equals(version))
        {
          version = "0";
        }
        DBRefEntry dbref = new DBRefEntry(source, version, xref.getId());
        DBRefEntry proteinDbRef = new DBRefEntry(source, version,
                dbref.getAccessionId());
        if (source.equals(DBRefSource.UNIPROT))
        {
          String proteinSeqName = DBRefSource.UNIPROT + "|"
                  + dbref.getAccessionId();
          if (dnaToProteinMapping != null
                  && dnaToProteinMapping.getTo() != null)
          {
            if (mappingUsed)
            {
              /*
               * two or more Uniprot xrefs for the same CDS - 
               * each needs a distinct Mapping (as to a different sequence)
               */
              dnaToProteinMapping = new Mapping(dnaToProteinMapping);
            }
            mappingUsed = true;

            /*
             * try to locate the protein mapped to (possibly by a 
             * previous CDS feature); if not found, construct it from
             * the EMBL translation
             */
            SequenceI proteinSeq = matcher.findIdMatch(proteinSeqName);
            if (proteinSeq == null)
            {
              proteinSeq = new Sequence(proteinSeqName,
                      product.getSequenceAsString());
              matcher.add(proteinSeq);
              proteinSeq.setDescription(product.getDescription());
              peptides.add(proteinSeq);
            }
            dnaToProteinMapping.setTo(proteinSeq);
            dnaToProteinMapping.setMappedFromId(proteinId);
            proteinSeq.addDBRef(proteinDbRef);
            dbref.setMap(dnaToProteinMapping);
          }
          hasUniprotDbref = true;
        }
        if (product != null)
        {
          /*
           * copy feature dbref to our protein product
           */
          DBRefEntry pref = proteinDbRef;
          pref.setMap(null); // reference is direct
          product.addDBRef(pref);
          // Add converse mapping reference
          if (dnaToProteinMapping != null)
          {
            Mapping pmap = new Mapping(dna,
                    dnaToProteinMapping.getMap().getInverse());
            pref = new DBRefEntry(sourceDb, sequenceVersion, accession);
            pref.setMap(pmap);
            if (dnaToProteinMapping.getTo() != null)
            {
              dnaToProteinMapping.getTo().addDBRef(pref);
            }
          }
        }
        dna.addDBRef(dbref);
      }
    }

    /*
     * if we have a product (translation) but no explicit Uniprot dbref
     * (example: EMBL AAFI02000057 protein_id EAL65544.1)
     * then construct mappings to an assumed EMBLCDSPROTEIN accession
     */
    if (!hasUniprotDbref && product != null)
    {
      if (proteinToEmblProteinRef == null)
      {
        // assuming CDSPROTEIN sequence version = dna version (?!)
        proteinToEmblProteinRef = new DBRefEntry(DBRefSource.EMBLCDSProduct,
                sequenceVersion, proteinId);
      }
      product.addDBRef(proteinToEmblProteinRef);

      if (dnaToProteinMapping != null
              && dnaToProteinMapping.getTo() != null)
      {
        DBRefEntry dnaToEmblProteinRef = new DBRefEntry(
                DBRefSource.EMBLCDSProduct, sequenceVersion, proteinId);
        dnaToEmblProteinRef.setMap(dnaToProteinMapping);
        dnaToProteinMapping.setMappedFromId(proteinId);
        dna.addDBRef(dnaToEmblProteinRef);
      }
    }
  }

  @Override
  public boolean isDnaCoding()
  {
    return true;
  }

  /**
   * Returns the CDS positions as a single array of [start, end, start, end...]
   * positions. If on the reverse strand, these will be in descending order.
   * 
   * @param accession
   * @param feature
   * @return
   */
  protected int[] getCdsRanges(String accession, Feature feature)
  {
    String location = feature.getLocation();
    if (location == null)
    {
      return new int[] {};
    }

    try
    {
      List<int[]> ranges = DnaUtils.parseLocation(location);
      return listToArray(ranges);
    } catch (ParseException e)
    {
      Console.warn(
              String.format("Not parsing inexact CDS location %s in ENA %s",
                      location, accession));
      return new int[] {};
    }
  }

  /**
   * Converts a list of [start, end] ranges to a single array of [start, end,
   * start, end ...]
   * 
   * @param ranges
   * @return
   */
  int[] listToArray(List<int[]> ranges)
  {
    int[] result = new int[ranges.size() * 2];
    int i = 0;
    for (int[] range : ranges)
    {
      result[i++] = range[0];
      result[i++] = range[1];
    }
    return result;
  }

  /**
   * Helper method to construct a SequenceFeature for one cds range
   * 
   * @param type
   *          feature type ("CDS")
   * @param desc
   *          description
   * @param begin
   *          start position
   * @param end
   *          end position
   * @param group
   *          feature group
   * @param vals
   *          map of 'miscellaneous values' for feature
   * @return
   */
  protected SequenceFeature makeCdsFeature(String type, String desc,
          int begin, int end, String group, Map<String, String> vals)
  {
    SequenceFeature sf = new SequenceFeature(type, desc, begin, end, group);
    if (!vals.isEmpty())
    {
      for (Entry<String, String> val : vals.entrySet())
      {
        sf.setValue(val.getKey(), val.getValue());
      }
    }
    return sf;
  }

  @Override
  public String getAccessionSeparator()
  {
    return null;
  }

  @Override
  public Regex getAccessionValidator()
  {
    return ACCESSION_REGEX;
  }

  @Override
  public String getDbVersion()
  {
    return "0";
  }

  @Override
  public int getTier()
  {
    return 0;
  }

  @Override
  public boolean isValidReference(String accession)
  {
    if (accession == null || accession.length() < 2)
    {
      return false;
    }
    return getAccessionValidator().search(accession);
  }

  /**
   * Truncates (if necessary) the exon intervals to match 3 times the length of
   * the protein (including truncation for stop codon included in exon)
   * 
   * @param proteinLength
   * @param exon
   *          an array of [start, end, start, end...] intervals
   * @return the same array (if unchanged) or a truncated copy
   */
  static int[] adjustForProteinLength(int proteinLength, int[] exon)
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

}
