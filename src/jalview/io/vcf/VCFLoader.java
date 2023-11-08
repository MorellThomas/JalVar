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
package jalview.io.vcf;

import java.util.Locale;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import htsjdk.samtools.SAMException;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.tribble.TribbleException;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFConstants;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import jalview.analysis.Dna;
import jalview.api.AlignViewControllerGuiI;
import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.GeneLociI;
import jalview.datamodel.Mapping;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.datamodel.features.FeatureAttributeType;
import jalview.datamodel.features.FeatureSource;
import jalview.datamodel.features.FeatureSources;
import jalview.ext.ensembl.EnsemblMap;
import jalview.ext.htsjdk.HtsContigDb;
import jalview.ext.htsjdk.VCFReader;
import jalview.io.gff.Gff3Helper;
import jalview.io.gff.SequenceOntologyI;
import jalview.util.MapList;
import jalview.util.MappingUtils;
import jalview.util.MessageManager;
import jalview.util.StringUtils;

/**
 * A class to read VCF data (using the htsjdk) and add variants as sequence
 * features on dna and any related protein product sequences
 * 
 * @author gmcarstairs
 */
public class VCFLoader
{
  private static final String VCF_ENCODABLE = ":;=%,";

  /*
   * Jalview feature attributes for VCF fixed column data
   */
  private static final String VCF_POS = "POS";

  private static final String VCF_ID = "ID";

  private static final String VCF_QUAL = "QUAL";

  private static final String VCF_FILTER = "FILTER";

  private static final String NO_VALUE = VCFConstants.MISSING_VALUE_v4; // '.'

  private static final String DEFAULT_SPECIES = "homo_sapiens";

  /**
   * A class to model the mapping from sequence to VCF coordinates. Cases
   * include
   * <ul>
   * <li>a direct 1:1 mapping where the sequence is one of the VCF contigs</li>
   * <li>a mapping of sequence to chromosomal coordinates, where sequence and
   * VCF use the same reference assembly</li>
   * <li>a modified mapping of sequence to chromosomal coordinates, where
   * sequence and VCF use different reference assembles</li>
   * </ul>
   */
  class VCFMap
  {
    final String chromosome;

    final MapList map;

    VCFMap(String chr, MapList m)
    {
      chromosome = chr;
      map = m;
    }

    @Override
    public String toString()
    {
      return chromosome + ":" + map.toString();
    }
  }

  /*
   * Lookup keys, and default values, for Preference entries that describe
   * patterns for VCF and VEP fields to capture
   */
  private static final String VEP_FIELDS_PREF = "VEP_FIELDS";

  private static final String VCF_FIELDS_PREF = "VCF_FIELDS";

  private static final String DEFAULT_VCF_FIELDS = ".*";

  private static final String DEFAULT_VEP_FIELDS = ".*";// "Allele,Consequence,IMPACT,SWISSPROT,SIFT,PolyPhen,CLIN_SIG";

  /*
   * Lookup keys, and default values, for Preference entries that give
   * mappings from tokens in the 'reference' header to species or assembly
   */
  private static final String VCF_ASSEMBLY = "VCF_ASSEMBLY";

  private static final String DEFAULT_VCF_ASSEMBLY = "assembly19=GRCh37,hs37=GRCh37,grch37=GRCh37,grch38=GRCh38";

  private static final String VCF_SPECIES = "VCF_SPECIES"; // default is human

  private static final String DEFAULT_REFERENCE = "grch37"; // fallback default
                                                            // is human GRCh37

  /*
   * keys to fields of VEP CSQ consequence data
   * see https://www.ensembl.org/info/docs/tools/vep/vep_formats.html
   */
  private static final String CSQ_CONSEQUENCE_KEY = "Consequence";

  private static final String CSQ_ALLELE_KEY = "Allele";

  private static final String CSQ_ALLELE_NUM_KEY = "ALLELE_NUM"; // 0 (ref),
                                                                 // 1...

  private static final String CSQ_FEATURE_KEY = "Feature"; // Ensembl stable id

  /*
   * default VCF INFO key for VEP consequence data
   * NB this can be overridden running VEP with --vcf_info_field
   * - we don't handle this case (require identifier to be CSQ)
   */
  private static final String CSQ_FIELD = "CSQ";

  /*
   * separator for fields in consequence data is '|'
   */
  private static final String PIPE_REGEX = "\\|";

  /*
   * delimiter that separates multiple consequence data blocks
   */
  private static final String COMMA = ",";

  /*
   * the feature group assigned to a VCF variant in Jalview
   */
  private static final String FEATURE_GROUP_VCF = "VCF";

  /*
   * internal delimiter used to build keys for assemblyMappings
   * 
   */
  private static final String EXCL = "!";

  /*
   * the VCF file we are processing
   */
  protected String vcfFilePath;

  /*
   * mappings between VCF and sequence reference assembly regions, as 
   * key = "species!chromosome!fromAssembly!toAssembly
   * value = Map{fromRange, toRange}
   */
  private Map<String, Map<int[], int[]>> assemblyMappings;

  private VCFReader reader;

  /*
   * holds details of the VCF header lines (metadata)
   */
  private VCFHeader header;

  /*
   * species (as a valid Ensembl term) the VCF is for 
   */
  private String vcfSpecies;

  /*
   * genome assembly version (as a valid Ensembl identifier) the VCF is for 
   */
  private String vcfAssembly;

  /*
   * a Dictionary of contigs (if present) referenced in the VCF file
   */
  private SAMSequenceDictionary dictionary;

  /*
   * the position (0...) of field in each block of
   * CSQ (consequence) data (if declared in the VCF INFO header for CSQ)
   * see http://www.ensembl.org/info/docs/tools/vep/vep_formats.html
   */
  private int csqConsequenceFieldIndex = -1;

  private int csqAlleleFieldIndex = -1;

  private int csqAlleleNumberFieldIndex = -1;

  private int csqFeatureFieldIndex = -1;

  // todo the same fields for SnpEff ANN data if wanted
  // see http://snpeff.sourceforge.net/SnpEff_manual.html#input

  /*
   * a unique identifier under which to save metadata about feature
   * attributes (selected INFO field data)
   */
  private String sourceId;

  /*
   * The INFO IDs of data that is both present in the VCF file, and
   * also matched by any filters for data of interest
   */
  List<String> vcfFieldsOfInterest;

  /*
   * The field offsets and identifiers for VEP (CSQ) data that is both present
   * in the VCF file, and also matched by any filters for data of interest
   * for example 0 -> Allele, 1 -> Consequence, ..., 36 -> SIFT, ...
   */
  Map<Integer, String> vepFieldsOfInterest;

  /*
   * key:value for which rejected data has been seen
   * (the error is logged only once for each combination)
   */
  private Set<String> badData;

  /**
   * Constructor given a VCF file
   * 
   * @param alignment
   */
  public VCFLoader(String vcfFile)
  {
    try
    {
      initialise(vcfFile);
    } catch (IOException e)
    {
      System.err.println("Error opening VCF file: " + e.getMessage());
    }

    // map of species!chromosome!fromAssembly!toAssembly to {fromRange, toRange}
    assemblyMappings = new HashMap<>();
  }

  /**
   * Starts a new thread to query and load VCF variant data on to the given
   * sequences
   * <p>
   * This method is not thread safe - concurrent threads should use separate
   * instances of this class.
   * 
   * @param seqs
   * @param gui
   */
  public void loadVCF(SequenceI[] seqs, final AlignViewControllerGuiI gui)
  {
    if (gui != null)
    {
      gui.setStatus(MessageManager.getString("label.searching_vcf"));
    }

    new Thread()
    {
      @Override
      public void run()
      {
        VCFLoader.this.doLoad(seqs, gui);
      }
    }.start();
  }

  /**
   * Reads the specified contig sequence and adds its VCF variants to it
   * 
   * @param contig
   *          the id of a single sequence (contig) to load
   * @return
   */
  public SequenceI loadVCFContig(String contig)
  {
    VCFHeaderLine headerLine = header
            .getOtherHeaderLine(VCFHeader.REFERENCE_KEY);
    if (headerLine == null)
    {
      Console.error("VCF reference header not found");
      return null;
    }
    String ref = headerLine.getValue();
    if (ref.startsWith("file://"))
    {
      ref = ref.substring(7);
    }
    setSpeciesAndAssembly(ref);

    SequenceI seq = null;
    File dbFile = new File(ref);

    if (dbFile.exists())
    {
      HtsContigDb db = new HtsContigDb("", dbFile);
      seq = db.getSequenceProxy(contig);
      loadSequenceVCF(seq);
      db.close();
    }
    else
    {
      Console.error("VCF reference not found: " + ref);
    }

    return seq;
  }

  /**
   * Loads VCF on to one or more sequences
   * 
   * @param seqs
   * @param gui
   *          optional callback handler for messages
   */
  protected void doLoad(SequenceI[] seqs, AlignViewControllerGuiI gui)
  {
    try
    {
      VCFHeaderLine ref = header
              .getOtherHeaderLine(VCFHeader.REFERENCE_KEY);
      String reference = ref == null ? null : ref.getValue();

      setSpeciesAndAssembly(reference);

      int varCount = 0;
      int seqCount = 0;

      /*
       * query for VCF overlapping each sequence in turn
       */
      for (SequenceI seq : seqs)
      {
        int added = loadSequenceVCF(seq);
        if (added > 0)
        {
          seqCount++;
          varCount += added;
          transferAddedFeatures(seq);
        }
      }
      if (gui != null)
      {
        String msg = MessageManager.formatMessage("label.added_vcf",
                varCount, seqCount);
        gui.setStatus(msg);
        if (gui.getFeatureSettingsUI() != null)
        {
          gui.getFeatureSettingsUI().discoverAllFeatureData();
        }
      }
    } catch (Throwable e)
    {
      System.err.println("Error processing VCF: " + e.getMessage());
      e.printStackTrace();
      if (gui != null)
      {
        gui.setStatus("Error occurred - see console for details");
      }
    } finally
    {
      if (reader != null)
      {
        try
        {
          reader.close();
        } catch (IOException e)
        {
          // ignore
        }
      }
      header = null;
      dictionary = null;
    }
  }

  /**
   * Attempts to determine and save the species and genome assembly version to
   * which the VCF data applies. This may be done by parsing the
   * {@code reference} header line, configured in a property file, or
   * (potentially) confirmed interactively by the user.
   * <p>
   * The saved values should be identifiers valid for Ensembl's REST service
   * {@code map} endpoint, so they can be used (if necessary) to retrieve the
   * mapping between VCF coordinates and sequence coordinates.
   * 
   * @param reference
   * @see https://rest.ensembl.org/documentation/info/assembly_map
   * @see https://rest.ensembl.org/info/assembly/human?content-type=text/xml
   * @see https://rest.ensembl.org/info/species?content-type=text/xml
   */
  protected void setSpeciesAndAssembly(String reference)
  {
    if (reference == null)
    {
      Console.error("No VCF ##reference found, defaulting to "
              + DEFAULT_REFERENCE + ":" + DEFAULT_SPECIES);
      reference = DEFAULT_REFERENCE; // default to GRCh37 if not specified
    }
    reference = reference.toLowerCase(Locale.ROOT);

    /*
     * for a non-human species, or other assembly identifier,
     * specify as a Jalview property file entry e.g.
     * VCF_ASSEMBLY = hs37=GRCh37,assembly19=GRCh37
     * VCF_SPECIES = c_elegans=celegans
     * to map a token in the reference header to a value
     */
    String prop = Cache.getDefault(VCF_ASSEMBLY, DEFAULT_VCF_ASSEMBLY);
    for (String token : prop.split(","))
    {
      String[] tokens = token.split("=");
      if (tokens.length == 2)
      {
        if (reference.contains(tokens[0].trim().toLowerCase(Locale.ROOT)))
        {
          vcfAssembly = tokens[1].trim();
          break;
        }
      }
    }

    vcfSpecies = DEFAULT_SPECIES;
    prop = Cache.getProperty(VCF_SPECIES);
    if (prop != null)
    {
      for (String token : prop.split(","))
      {
        String[] tokens = token.split("=");
        if (tokens.length == 2)
        {
          if (reference.contains(tokens[0].trim().toLowerCase(Locale.ROOT)))
          {
            vcfSpecies = tokens[1].trim();
            break;
          }
        }
      }
    }
  }

  /**
   * Opens the VCF file and parses header data
   * 
   * @param filePath
   * @throws IOException
   */
  private void initialise(String filePath) throws IOException
  {
    vcfFilePath = filePath;

    reader = new VCFReader(filePath);

    header = reader.getFileHeader();

    try
    {
      dictionary = header.getSequenceDictionary();
    } catch (SAMException e)
    {
      // ignore - thrown if any contig line lacks length info
    }

    sourceId = filePath;

    saveMetadata(sourceId);

    /*
     * get offset of CSQ ALLELE_NUM and Feature if declared
     */
    parseCsqHeader();
  }

  /**
   * Reads metadata (such as INFO field descriptions and datatypes) and saves
   * them for future reference
   * 
   * @param theSourceId
   */
  void saveMetadata(String theSourceId)
  {
    List<Pattern> vcfFieldPatterns = getFieldMatchers(VCF_FIELDS_PREF,
            DEFAULT_VCF_FIELDS);
    vcfFieldsOfInterest = new ArrayList<>();

    FeatureSource metadata = new FeatureSource(theSourceId);

    for (VCFInfoHeaderLine info : header.getInfoHeaderLines())
    {
      String attributeId = info.getID();
      String desc = info.getDescription();
      VCFHeaderLineType type = info.getType();
      FeatureAttributeType attType = null;
      switch (type)
      {
      case Character:
        attType = FeatureAttributeType.Character;
        break;
      case Flag:
        attType = FeatureAttributeType.Flag;
        break;
      case Float:
        attType = FeatureAttributeType.Float;
        break;
      case Integer:
        attType = FeatureAttributeType.Integer;
        break;
      case String:
        attType = FeatureAttributeType.String;
        break;
      }
      metadata.setAttributeName(attributeId, desc);
      metadata.setAttributeType(attributeId, attType);

      if (isFieldWanted(attributeId, vcfFieldPatterns))
      {
        vcfFieldsOfInterest.add(attributeId);
      }
    }

    FeatureSources.getInstance().addSource(theSourceId, metadata);
  }

  /**
   * Answers true if the field id is matched by any of the filter patterns, else
   * false. Matching is against regular expression patterns, and is not
   * case-sensitive.
   * 
   * @param id
   * @param filters
   * @return
   */
  private boolean isFieldWanted(String id, List<Pattern> filters)
  {
    for (Pattern p : filters)
    {
      if (p.matcher(id.toUpperCase(Locale.ROOT)).matches())
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Records 'wanted' fields defined in the CSQ INFO header (if there is one).
   * Also records the position of selected fields (Allele, ALLELE_NUM, Feature)
   * required for processing.
   * <p>
   * CSQ fields are declared in the CSQ INFO Description e.g.
   * <p>
   * Description="Consequence ...from ... VEP. Format: Allele|Consequence|...
   */
  protected void parseCsqHeader()
  {
    List<Pattern> vepFieldFilters = getFieldMatchers(VEP_FIELDS_PREF,
            DEFAULT_VEP_FIELDS);
    vepFieldsOfInterest = new HashMap<>();

    VCFInfoHeaderLine csqInfo = header.getInfoHeaderLine(CSQ_FIELD);
    if (csqInfo == null)
    {
      return;
    }

    /*
     * parse out the pipe-separated list of CSQ fields; we assume here that
     * these form the last part of the description, and contain no spaces
     */
    String desc = csqInfo.getDescription();
    int spacePos = desc.lastIndexOf(" ");
    desc = desc.substring(spacePos + 1);

    if (desc != null)
    {
      String[] format = desc.split(PIPE_REGEX);
      int index = 0;
      for (String field : format)
      {
        if (CSQ_CONSEQUENCE_KEY.equals(field))
        {
          csqConsequenceFieldIndex = index;
        }
        if (CSQ_ALLELE_NUM_KEY.equals(field))
        {
          csqAlleleNumberFieldIndex = index;
        }
        if (CSQ_ALLELE_KEY.equals(field))
        {
          csqAlleleFieldIndex = index;
        }
        if (CSQ_FEATURE_KEY.equals(field))
        {
          csqFeatureFieldIndex = index;
        }

        if (isFieldWanted(field, vepFieldFilters))
        {
          vepFieldsOfInterest.put(index, field);
        }

        index++;
      }
    }
  }

  /**
   * Reads the Preference value for the given key, with default specified if no
   * preference set. The value is interpreted as a comma-separated list of
   * regular expressions, and converted into a list of compiled patterns ready
   * for matching. Patterns are forced to upper-case for non-case-sensitive
   * matching.
   * <p>
   * This supports user-defined filters for fields of interest to capture while
   * processing data. For example, VCF_FIELDS = AF,AC* would mean that VCF INFO
   * fields with an ID of AF, or starting with AC, would be matched.
   * 
   * @param key
   * @param def
   * @return
   */
  private List<Pattern> getFieldMatchers(String key, String def)
  {
    String pref = Cache.getDefault(key, def);
    List<Pattern> patterns = new ArrayList<>();
    String[] tokens = pref.split(",");
    for (String token : tokens)
    {
      try
      {
        patterns.add(Pattern.compile(token.toUpperCase(Locale.ROOT)));
      } catch (PatternSyntaxException e)
      {
        System.err.println("Invalid pattern ignored: " + token);
      }
    }
    return patterns;
  }

  /**
   * Transfers VCF features to sequences to which this sequence has a mapping.
   * 
   * @param seq
   */
  protected void transferAddedFeatures(SequenceI seq)
  {
    List<DBRefEntry> dbrefs = seq.getDBRefs();
    if (dbrefs == null)
    {
      return;
    }
    for (DBRefEntry dbref : dbrefs)
    {
      Mapping mapping = dbref.getMap();
      if (mapping == null || mapping.getTo() == null)
      {
        continue;
      }

      SequenceI mapTo = mapping.getTo();
      MapList map = mapping.getMap();
      if (map.getFromRatio() == 3)
      {
        /*
         * dna-to-peptide product mapping
         */
        // JAL-3187 render on the fly instead
        // AlignmentUtils.computeProteinFeatures(seq, mapTo, map);
      }
      else
      {
        /*
         * nucleotide-to-nucleotide mapping e.g. transcript to CDS
         */
        List<SequenceFeature> features = seq.getFeatures()
                .getPositionalFeatures(SequenceOntologyI.SEQUENCE_VARIANT);
        for (SequenceFeature sf : features)
        {
          if (FEATURE_GROUP_VCF.equals(sf.getFeatureGroup()))
          {
            transferFeature(sf, mapTo, map);
          }
        }
      }
    }
  }

  /**
   * Tries to add overlapping variants read from a VCF file to the given
   * sequence, and returns the number of variant features added
   * 
   * @param seq
   * @return
   */
  protected int loadSequenceVCF(SequenceI seq)
  {
    VCFMap vcfMap = getVcfMap(seq);
    if (vcfMap == null)
    {
      return 0;
    }

    /*
     * work with the dataset sequence here
     */
    SequenceI dss = seq.getDatasetSequence();
    if (dss == null)
    {
      dss = seq;
    }
    return addVcfVariants(dss, vcfMap);
  }

  /**
   * Answers a map from sequence coordinates to VCF chromosome ranges
   * 
   * @param seq
   * @return
   */
  private VCFMap getVcfMap(SequenceI seq)
  {
    /*
     * simplest case: sequence has id and length matching a VCF contig
     */
    VCFMap vcfMap = null;
    if (dictionary != null)
    {
      vcfMap = getContigMap(seq);
    }
    if (vcfMap != null)
    {
      return vcfMap;
    }

    /*
     * otherwise, map to VCF from chromosomal coordinates 
     * of the sequence (if known)
     */
    GeneLociI seqCoords = seq.getGeneLoci();
    if (seqCoords == null)
    {
      Console.warn(String.format(
              "Can't query VCF for %s as chromosome coordinates not known",
              seq.getName()));
      return null;
    }

    String species = seqCoords.getSpeciesId();
    String chromosome = seqCoords.getChromosomeId();
    String seqRef = seqCoords.getAssemblyId();
    MapList map = seqCoords.getMapping();

    // note this requires the configured species to match that
    // returned with the Ensembl sequence; todo: support aliases?
    if (!vcfSpecies.equalsIgnoreCase(species))
    {
      Console.warn("No VCF loaded to " + seq.getName()
              + " as species not matched");
      return null;
    }

    if (seqRef.equalsIgnoreCase(vcfAssembly))
    {
      return new VCFMap(chromosome, map);
    }

    /*
     * VCF data has a different reference assembly to the sequence:
     * query Ensembl to map chromosomal coordinates from sequence to VCF
     */
    List<int[]> toVcfRanges = new ArrayList<>();
    List<int[]> fromSequenceRanges = new ArrayList<>();

    for (int[] range : map.getToRanges())
    {
      int[] fromRange = map.locateInFrom(range[0], range[1]);
      if (fromRange == null)
      {
        // corrupted map?!?
        continue;
      }

      int[] newRange = mapReferenceRange(range, chromosome, "human", seqRef,
              vcfAssembly);
      if (newRange == null)
      {
        Console.error(String.format("Failed to map %s:%s:%s:%d:%d to %s",
                species, chromosome, seqRef, range[0], range[1],
                vcfAssembly));
        continue;
      }
      else
      {
        toVcfRanges.add(newRange);
        fromSequenceRanges.add(fromRange);
      }
    }

    return new VCFMap(chromosome,
            new MapList(fromSequenceRanges, toVcfRanges, 1, 1));
  }

  /**
   * If the sequence id matches a contig declared in the VCF file, and the
   * sequence length matches the contig length, then returns a 1:1 map of the
   * sequence to the contig, else returns null
   * 
   * @param seq
   * @return
   */
  private VCFMap getContigMap(SequenceI seq)
  {
    String id = seq.getName();
    SAMSequenceRecord contig = dictionary.getSequence(id);
    if (contig != null)
    {
      int len = seq.getLength();
      if (len == contig.getSequenceLength())
      {
        MapList map = new MapList(new int[] { 1, len },
                new int[]
                { 1, len }, 1, 1);
        return new VCFMap(id, map);
      }
    }
    return null;
  }

  /**
   * Queries the VCF reader for any variants that overlap the mapped chromosome
   * ranges of the sequence, and adds as variant features. Returns the number of
   * overlapping variants found.
   * 
   * @param seq
   * @param map
   *          mapping from sequence to VCF coordinates
   * @return
   */
  protected int addVcfVariants(SequenceI seq, VCFMap map)
  {
    boolean forwardStrand = map.map.isToForwardStrand();

    /*
     * query the VCF for overlaps of each contiguous chromosomal region
     */
    int count = 0;

    for (int[] range : map.map.getToRanges())
    {
      int vcfStart = Math.min(range[0], range[1]);
      int vcfEnd = Math.max(range[0], range[1]);
      try
      {
        CloseableIterator<VariantContext> variants = reader
                .query(map.chromosome, vcfStart, vcfEnd);
        while (variants.hasNext())
        {
          VariantContext variant = variants.next();

          int[] featureRange = map.map.locateInFrom(variant.getStart(),
                  variant.getEnd());

          /*
           * only take features whose range is fully mappable to sequence positions
           */
          if (featureRange != null)
          {
            int featureStart = Math.min(featureRange[0], featureRange[1]);
            int featureEnd = Math.max(featureRange[0], featureRange[1]);
            if (featureEnd - featureStart == variant.getEnd()
                    - variant.getStart())
            {
              count += addAlleleFeatures(seq, variant, featureStart,
                      featureEnd, forwardStrand);
            }
          }
        }
        variants.close();
      } catch (TribbleException e)
      {
        /*
         * RuntimeException throwable by htsjdk
         */
        String msg = String.format("Error reading VCF for %s:%d-%d: %s ",
                map.chromosome, vcfStart, vcfEnd, e.getLocalizedMessage());
        Console.error(msg);
      }
    }

    return count;
  }

  /**
   * A convenience method to get an attribute value for an alternate allele
   * 
   * @param variant
   * @param attributeName
   * @param alleleIndex
   * @return
   */
  protected String getAttributeValue(VariantContext variant,
          String attributeName, int alleleIndex)
  {
    Object att = variant.getAttribute(attributeName);

    if (att instanceof String)
    {
      return (String) att;
    }
    else if (att instanceof ArrayList)
    {
      return ((List<String>) att).get(alleleIndex);
    }

    return null;
  }

  /**
   * Adds one variant feature for each allele in the VCF variant record, and
   * returns the number of features added.
   * 
   * @param seq
   * @param variant
   * @param featureStart
   * @param featureEnd
   * @param forwardStrand
   * @return
   */
  protected int addAlleleFeatures(SequenceI seq, VariantContext variant,
          int featureStart, int featureEnd, boolean forwardStrand)
  {
    int added = 0;

    /*
     * Javadoc says getAlternateAlleles() imposes no order on the list returned
     * so we proceed defensively to get them in strict order
     */
    int altAlleleCount = variant.getAlternateAlleles().size();
    for (int i = 0; i < altAlleleCount; i++)
    {
      added += addAlleleFeature(seq, variant, i, featureStart, featureEnd,
              forwardStrand);
    }
    return added;
  }

  /**
   * Inspects one allele and attempts to add a variant feature for it to the
   * sequence. The additional data associated with this allele is extracted to
   * store in the feature's key-value map. Answers the number of features added
   * (0 or 1).
   * 
   * @param seq
   * @param variant
   * @param altAlleleIndex
   *          (0, 1..)
   * @param featureStart
   * @param featureEnd
   * @param forwardStrand
   * @return
   */
  protected int addAlleleFeature(SequenceI seq, VariantContext variant,
          int altAlleleIndex, int featureStart, int featureEnd,
          boolean forwardStrand)
  {
    String reference = variant.getReference().getBaseString();
    Allele alt = variant.getAlternateAllele(altAlleleIndex);
    String allele = alt.getBaseString();

    /*
     * insertion after a genomic base, if on reverse strand, has to be 
     * converted to insertion of complement after the preceding position 
     */
    int referenceLength = reference.length();
    if (!forwardStrand && allele.length() > referenceLength
            && allele.startsWith(reference))
    {
      featureStart -= referenceLength;
      featureEnd = featureStart;
      char insertAfter = seq.getCharAt(featureStart - seq.getStart());
      reference = Dna.reverseComplement(String.valueOf(insertAfter));
      allele = allele.substring(referenceLength) + reference;
    }

    /*
     * build the ref,alt allele description e.g. "G,A", using the base
     * complement if the sequence is on the reverse strand
     */
    StringBuilder sb = new StringBuilder();
    sb.append(forwardStrand ? reference : Dna.reverseComplement(reference));
    sb.append(COMMA);
    sb.append(forwardStrand ? allele : Dna.reverseComplement(allele));
    String alleles = sb.toString(); // e.g. G,A

    /*
     * pick out the consequence data (if any) that is for the current allele
     * and feature (transcript) that matches the current sequence
     */
    String consequence = getConsequenceForAlleleAndFeature(variant,
            CSQ_FIELD, altAlleleIndex, csqAlleleFieldIndex,
            csqAlleleNumberFieldIndex,
            seq.getName().toLowerCase(Locale.ROOT), csqFeatureFieldIndex);

    /*
     * pick out the ontology term for the consequence type
     */
    String type = SequenceOntologyI.SEQUENCE_VARIANT;
    if (consequence != null)
    {
      type = getOntologyTerm(consequence);
    }

    SequenceFeature sf = new SequenceFeature(type, alleles, featureStart,
            featureEnd, FEATURE_GROUP_VCF);
    sf.setSource(sourceId);

    /*
     * save the derived alleles as a named attribute; this will be
     * needed when Jalview computes derived peptide variants
     */
    addFeatureAttribute(sf, Gff3Helper.ALLELES, alleles);

    /*
     * add selected VCF fixed column data as feature attributes
     */
    addFeatureAttribute(sf, VCF_POS, String.valueOf(variant.getStart()));
    addFeatureAttribute(sf, VCF_ID, variant.getID());
    addFeatureAttribute(sf, VCF_QUAL,
            String.valueOf(variant.getPhredScaledQual()));
    addFeatureAttribute(sf, VCF_FILTER, getFilter(variant));

    addAlleleProperties(variant, sf, altAlleleIndex, consequence);

    seq.addSequenceFeature(sf);

    return 1;
  }

  /**
   * Answers the VCF FILTER value for the variant - or an approximation to it.
   * This field is either PASS, or a semi-colon separated list of filters not
   * passed. htsjdk saves filters as a HashSet, so the order when reassembled
   * into a list may be different.
   * 
   * @param variant
   * @return
   */
  String getFilter(VariantContext variant)
  {
    Set<String> filters = variant.getFilters();
    if (filters.isEmpty())
    {
      return NO_VALUE;
    }
    Iterator<String> iterator = filters.iterator();
    String first = iterator.next();
    if (filters.size() == 1)
    {
      return first;
    }

    StringBuilder sb = new StringBuilder(first);
    while (iterator.hasNext())
    {
      sb.append(";").append(iterator.next());
    }

    return sb.toString();
  }

  /**
   * Adds one feature attribute unless the value is null, empty or '.'
   * 
   * @param sf
   * @param key
   * @param value
   */
  void addFeatureAttribute(SequenceFeature sf, String key, String value)
  {
    if (value != null && !value.isEmpty() && !NO_VALUE.equals(value))
    {
      sf.setValue(key, value);
    }
  }

  /**
   * Determines the Sequence Ontology term to use for the variant feature type
   * in Jalview. The default is 'sequence_variant', but a more specific term is
   * used if:
   * <ul>
   * <li>VEP (or SnpEff) Consequence annotation is included in the VCF</li>
   * <li>sequence id can be matched to VEP Feature (or SnpEff Feature_ID)</li>
   * </ul>
   * 
   * @param consequence
   * @return
   * @see http://www.sequenceontology.org/browser/current_svn/term/SO:0001060
   */
  String getOntologyTerm(String consequence)
  {
    String type = SequenceOntologyI.SEQUENCE_VARIANT;

    /*
     * could we associate Consequence data with this allele and feature (transcript)?
     * if so, prefer the consequence term from that data
     */
    if (csqAlleleFieldIndex == -1) // && snpEffAlleleFieldIndex == -1
    {
      /*
       * no Consequence data so we can't refine the ontology term
       */
      return type;
    }

    if (consequence != null)
    {
      String[] csqFields = consequence.split(PIPE_REGEX);
      if (csqFields.length > csqConsequenceFieldIndex)
      {
        type = csqFields[csqConsequenceFieldIndex];
      }
    }
    else
    {
      // todo the same for SnpEff consequence data matching if wanted
    }

    /*
     * if of the form (e.g.) missense_variant&splice_region_variant,
     * just take the first ('most severe') consequence
     */
    if (type != null)
    {
      int pos = type.indexOf('&');
      if (pos > 0)
      {
        type = type.substring(0, pos);
      }
    }
    return type;
  }

  /**
   * Returns matched consequence data if it can be found, else null.
   * <ul>
   * <li>inspects the VCF data for key 'vcfInfoId'</li>
   * <li>splits this on comma (to distinct consequences)</li>
   * <li>returns the first consequence (if any) where</li>
   * <ul>
   * <li>the allele matches the altAlleleIndex'th allele of variant</li>
   * <li>the feature matches the sequence name (e.g. transcript id)</li>
   * </ul>
   * </ul>
   * If matched, the consequence is returned (as pipe-delimited fields).
   * 
   * @param variant
   * @param vcfInfoId
   * @param altAlleleIndex
   * @param alleleFieldIndex
   * @param alleleNumberFieldIndex
   * @param seqName
   * @param featureFieldIndex
   * @return
   */
  private String getConsequenceForAlleleAndFeature(VariantContext variant,
          String vcfInfoId, int altAlleleIndex, int alleleFieldIndex,
          int alleleNumberFieldIndex, String seqName, int featureFieldIndex)
  {
    if (alleleFieldIndex == -1 || featureFieldIndex == -1)
    {
      return null;
    }
    Object value = variant.getAttribute(vcfInfoId);

    if (value == null || !(value instanceof List<?>))
    {
      return null;
    }

    /*
     * inspect each consequence in turn (comma-separated blocks
     * extracted by htsjdk)
     */
    List<String> consequences = (List<String>) value;

    for (String consequence : consequences)
    {
      String[] csqFields = consequence.split(PIPE_REGEX);
      if (csqFields.length > featureFieldIndex)
      {
        String featureIdentifier = csqFields[featureFieldIndex];
        if (featureIdentifier.length() > 4 && seqName
                .indexOf(featureIdentifier.toLowerCase(Locale.ROOT)) > -1)
        {
          /*
           * feature (transcript) matched - now check for allele match
           */
          if (matchAllele(variant, altAlleleIndex, csqFields,
                  alleleFieldIndex, alleleNumberFieldIndex))
          {
            return consequence;
          }
        }
      }
    }
    return null;
  }

  private boolean matchAllele(VariantContext variant, int altAlleleIndex,
          String[] csqFields, int alleleFieldIndex,
          int alleleNumberFieldIndex)
  {
    /*
     * if ALLELE_NUM is present, it must match altAlleleIndex
     * NB first alternate allele is 1 for ALLELE_NUM, 0 for altAlleleIndex
     */
    if (alleleNumberFieldIndex > -1)
    {
      if (csqFields.length <= alleleNumberFieldIndex)
      {
        return false;
      }
      String alleleNum = csqFields[alleleNumberFieldIndex];
      return String.valueOf(altAlleleIndex + 1).equals(alleleNum);
    }

    /*
     * else consequence allele must match variant allele
     */
    if (alleleFieldIndex > -1 && csqFields.length > alleleFieldIndex)
    {
      String csqAllele = csqFields[alleleFieldIndex];
      String vcfAllele = variant.getAlternateAllele(altAlleleIndex)
              .getBaseString();
      return csqAllele.equals(vcfAllele);
    }
    return false;
  }

  /**
   * Add any allele-specific VCF key-value data to the sequence feature
   * 
   * @param variant
   * @param sf
   * @param altAlelleIndex
   *          (0, 1..)
   * @param consequence
   *          if not null, the consequence specific to this sequence (transcript
   *          feature) and allele
   */
  protected void addAlleleProperties(VariantContext variant,
          SequenceFeature sf, final int altAlelleIndex, String consequence)
  {
    Map<String, Object> atts = variant.getAttributes();

    for (Entry<String, Object> att : atts.entrySet())
    {
      String key = att.getKey();

      /*
       * extract Consequence data (if present) that we are able to
       * associated with the allele for this variant feature
       */
      if (CSQ_FIELD.equals(key))
      {
        addConsequences(variant, sf, consequence);
        continue;
      }

      /*
       * filter out fields we don't want to capture
       */
      if (!vcfFieldsOfInterest.contains(key))
      {
        continue;
      }

      /*
       * we extract values for other data which are allele-specific; 
       * these may be per alternate allele (INFO[key].Number = 'A') 
       * or per allele including reference (INFO[key].Number = 'R') 
       */
      VCFInfoHeaderLine infoHeader = header.getInfoHeaderLine(key);
      if (infoHeader == null)
      {
        /*
         * can't be sure what data belongs to this allele, so
         * play safe and don't take any
         */
        continue;
      }

      VCFHeaderLineCount number = infoHeader.getCountType();
      int index = altAlelleIndex;
      if (number == VCFHeaderLineCount.R)
      {
        /*
         * one value per allele including reference, so bump index
         * e.g. the 3rd value is for the  2nd alternate allele
         */
        index++;
      }
      else if (number != VCFHeaderLineCount.A)
      {
        /*
         * don't save other values as not allele-related
         */
        continue;
      }

      /*
       * take the index'th value
       */
      String value = getAttributeValue(variant, key, index);
      if (value != null && isValid(variant, key, value))
      {
        /*
         * decode colon, semicolon, equals sign, percent sign, comma (only)
         * as required by the VCF specification (para 1.2)
         */
        value = StringUtils.urlDecode(value, VCF_ENCODABLE);
        addFeatureAttribute(sf, key, value);
      }
    }
  }

  /**
   * Answers true for '.', null, or an empty value, or if the INFO type is
   * String. If the INFO type is Integer or Float, answers false if the value is
   * not in valid format.
   * 
   * @param variant
   * @param infoId
   * @param value
   * @return
   */
  protected boolean isValid(VariantContext variant, String infoId,
          String value)
  {
    if (value == null || value.isEmpty() || NO_VALUE.equals(value))
    {
      return true;
    }
    VCFInfoHeaderLine infoHeader = header.getInfoHeaderLine(infoId);
    if (infoHeader == null)
    {
      Console.error("Field " + infoId + " has no INFO header");
      return false;
    }
    VCFHeaderLineType infoType = infoHeader.getType();
    try
    {
      if (infoType == VCFHeaderLineType.Integer)
      {
        Integer.parseInt(value);
      }
      else if (infoType == VCFHeaderLineType.Float)
      {
        Float.parseFloat(value);
      }
    } catch (NumberFormatException e)
    {
      logInvalidValue(variant, infoId, value);
      return false;
    }
    return true;
  }

  /**
   * Logs an error message for malformed data; duplicate messages (same id and
   * value) are not logged
   * 
   * @param variant
   * @param infoId
   * @param value
   */
  private void logInvalidValue(VariantContext variant, String infoId,
          String value)
  {
    if (badData == null)
    {
      badData = new HashSet<>();
    }
    String token = infoId + ":" + value;
    if (!badData.contains(token))
    {
      badData.add(token);
      Console.error(String.format("Invalid VCF data at %s:%d %s=%s",
              variant.getContig(), variant.getStart(), infoId, value));
    }
  }

  /**
   * Inspects CSQ data blocks (consequences) and adds attributes on the sequence
   * feature.
   * <p>
   * If <code>myConsequence</code> is not null, then this is the specific
   * consequence data (pipe-delimited fields) that is for the current allele and
   * transcript (sequence) being processed)
   * 
   * @param variant
   * @param sf
   * @param myConsequence
   */
  protected void addConsequences(VariantContext variant, SequenceFeature sf,
          String myConsequence)
  {
    Object value = variant.getAttribute(CSQ_FIELD);

    if (value == null || !(value instanceof List<?>))
    {
      return;
    }

    List<String> consequences = (List<String>) value;

    /*
     * inspect CSQ consequences; restrict to the consequence
     * associated with the current transcript (Feature)
     */
    Map<String, String> csqValues = new HashMap<>();

    for (String consequence : consequences)
    {
      if (myConsequence == null || myConsequence.equals(consequence))
      {
        String[] csqFields = consequence.split(PIPE_REGEX);

        /*
         * inspect individual fields of this consequence, copying non-null
         * values which are 'fields of interest'
         */
        int i = 0;
        for (String field : csqFields)
        {
          if (field != null && field.length() > 0)
          {
            String id = vepFieldsOfInterest.get(i);
            if (id != null)
            {
              /*
               * VCF spec requires encoding of special characters e.g. '='
               * so decode them here before storing
               */
              field = StringUtils.urlDecode(field, VCF_ENCODABLE);
              csqValues.put(id, field);
            }
          }
          i++;
        }
      }
    }

    if (!csqValues.isEmpty())
    {
      sf.setValue(CSQ_FIELD, csqValues);
    }
  }

  /**
   * A convenience method to complement a dna base and return the string value
   * of its complement
   * 
   * @param reference
   * @return
   */
  protected String complement(byte[] reference)
  {
    return String.valueOf(Dna.getComplement((char) reference[0]));
  }

  /**
   * Determines the location of the query range (chromosome positions) in a
   * different reference assembly.
   * <p>
   * If the range is just a subregion of one for which we already have a mapping
   * (for example, an exon sub-region of a gene), then the mapping is just
   * computed arithmetically.
   * <p>
   * Otherwise, calls the Ensembl REST service that maps from one assembly
   * reference's coordinates to another's
   * 
   * @param queryRange
   *          start-end chromosomal range in 'fromRef' coordinates
   * @param chromosome
   * @param species
   * @param fromRef
   *          assembly reference for the query coordinates
   * @param toRef
   *          assembly reference we wish to translate to
   * @return the start-end range in 'toRef' coordinates
   */
  protected int[] mapReferenceRange(int[] queryRange, String chromosome,
          String species, String fromRef, String toRef)
  {
    /*
     * first try shorcut of computing the mapping as a subregion of one
     * we already have (e.g. for an exon, if we have the gene mapping)
     */
    int[] mappedRange = findSubsumedRangeMapping(queryRange, chromosome,
            species, fromRef, toRef);
    if (mappedRange != null)
    {
      return mappedRange;
    }

    /*
     * call (e.g.) http://rest.ensembl.org/map/human/GRCh38/17:45051610..45109016:1/GRCh37
     */
    EnsemblMap mapper = new EnsemblMap();
    int[] mapping = mapper.getAssemblyMapping(species, chromosome, fromRef,
            toRef, queryRange);

    if (mapping == null)
    {
      // mapping service failure
      return null;
    }

    /*
     * save mapping for possible future re-use
     */
    String key = makeRangesKey(chromosome, species, fromRef, toRef);
    if (!assemblyMappings.containsKey(key))
    {
      assemblyMappings.put(key, new HashMap<int[], int[]>());
    }

    assemblyMappings.get(key).put(queryRange, mapping);

    return mapping;
  }

  /**
   * If we already have a 1:1 contiguous mapping which subsumes the given query
   * range, this method just calculates and returns the subset of that mapping,
   * else it returns null. In practical terms, if a gene has a contiguous
   * mapping between (for example) GRCh37 and GRCh38, then we assume that its
   * subsidiary exons occupy unchanged relative positions, and just compute
   * these as offsets, rather than do another lookup of the mapping.
   * <p>
   * If in future these assumptions prove invalid (e.g. for bacterial dna?!),
   * simply remove this method or let it always return null.
   * <p>
   * Warning: many rapid calls to the /map service map result in a 429 overload
   * error response
   * 
   * @param queryRange
   * @param chromosome
   * @param species
   * @param fromRef
   * @param toRef
   * @return
   */
  protected int[] findSubsumedRangeMapping(int[] queryRange,
          String chromosome, String species, String fromRef, String toRef)
  {
    String key = makeRangesKey(chromosome, species, fromRef, toRef);
    if (assemblyMappings.containsKey(key))
    {
      Map<int[], int[]> mappedRanges = assemblyMappings.get(key);
      for (Entry<int[], int[]> mappedRange : mappedRanges.entrySet())
      {
        int[] fromRange = mappedRange.getKey();
        int[] toRange = mappedRange.getValue();
        if (fromRange[1] - fromRange[0] == toRange[1] - toRange[0])
        {
          /*
           * mapping is 1:1 in length, so we trust it to have no discontinuities
           */
          if (MappingUtils.rangeContains(fromRange, queryRange))
          {
            /*
             * fromRange subsumes our query range
             */
            int offset = queryRange[0] - fromRange[0];
            int mappedRangeFrom = toRange[0] + offset;
            int mappedRangeTo = mappedRangeFrom
                    + (queryRange[1] - queryRange[0]);
            return new int[] { mappedRangeFrom, mappedRangeTo };
          }
        }
      }
    }
    return null;
  }

  /**
   * Transfers the sequence feature to the target sequence, locating its start
   * and end range based on the mapping. Features which do not overlap the
   * target sequence are ignored.
   * 
   * @param sf
   * @param targetSequence
   * @param mapping
   *          mapping from the feature's coordinates to the target sequence
   */
  protected void transferFeature(SequenceFeature sf,
          SequenceI targetSequence, MapList mapping)
  {
    int[] mappedRange = mapping.locateInTo(sf.getBegin(), sf.getEnd());

    if (mappedRange != null)
    {
      String group = sf.getFeatureGroup();
      int newBegin = Math.min(mappedRange[0], mappedRange[1]);
      int newEnd = Math.max(mappedRange[0], mappedRange[1]);
      SequenceFeature copy = new SequenceFeature(sf, newBegin, newEnd,
              group, sf.getScore());
      targetSequence.addSequenceFeature(copy);
    }
  }

  /**
   * Formats a ranges map lookup key
   * 
   * @param chromosome
   * @param species
   * @param fromRef
   * @param toRef
   * @return
   */
  protected static String makeRangesKey(String chromosome, String species,
          String fromRef, String toRef)
  {
    return species + EXCL + chromosome + EXCL + fromRef + EXCL + toRef;
  }
}
