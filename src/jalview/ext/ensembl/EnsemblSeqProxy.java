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
package jalview.ext.ensembl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ParseException;

import jalview.analysis.AlignmentUtils;
import jalview.analysis.Dna;
import jalview.bin.Console;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.Mapping;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.datamodel.features.SequenceFeatures;
import jalview.exceptions.JalviewException;
import jalview.io.gff.Gff3Helper;
import jalview.io.gff.SequenceOntologyFactory;
import jalview.io.gff.SequenceOntologyI;
import jalview.util.Comparison;
import jalview.util.DBRefUtils;
import jalview.util.IntRangeComparator;
import jalview.util.MapList;

/**
 * Base class for Ensembl sequence fetchers
 * 
 * @see http://rest.ensembl.org/documentation/info/sequence_id
 * @author gmcarstairs
 */
public abstract class EnsemblSeqProxy extends EnsemblRestClient
{
  protected static final String DESCRIPTION = "description";

  /*
   * enum for 'type' parameter to the /sequence REST service
   */
  public enum EnsemblSeqType
  {
    /**
     * type=genomic to fetch full dna including introns
     */
    GENOMIC("genomic"),

    /**
     * type=cdna to fetch coding dna including UTRs
     */
    CDNA("cdna"),

    /**
     * type=cds to fetch coding dna excluding UTRs
     */
    CDS("cds"),

    /**
     * type=protein to fetch peptide product sequence
     */
    PROTEIN("protein");

    /*
     * the value of the 'type' parameter to fetch this version of 
     * an Ensembl sequence
     */
    private String type;

    EnsemblSeqType(String t)
    {
      type = t;
    }

    public String getType()
    {
      return type;
    }

  }

  /**
   * Default constructor (to use rest.ensembl.org)
   */
  public EnsemblSeqProxy()
  {
    super();
  }

  /**
   * Constructor given the target domain to fetch data from
   */
  public EnsemblSeqProxy(String d)
  {
    super(d);
  }

  /**
   * Makes the sequence queries to Ensembl's REST service and returns an
   * alignment consisting of the returned sequences.
   */
  @Override
  public AlignmentI getSequenceRecords(String query) throws Exception
  {
    // TODO use a String... query vararg instead?

    // danger: accession separator used as a regex here, a string elsewhere
    // in this case it is ok (it is just a space), but (e.g.) '\' would not be
    List<String> allIds = Arrays
            .asList(query.split(getAccessionSeparator()));
    AlignmentI alignment = null;
    inProgress = true;

    /*
     * execute queries, if necessary in batches of the
     * maximum allowed number of ids
     */
    int maxQueryCount = getMaximumQueryCount();
    for (int v = 0, vSize = allIds.size(); v < vSize; v += maxQueryCount)
    {
      int p = Math.min(vSize, v + maxQueryCount);
      List<String> ids = allIds.subList(v, p);
      try
      {
        alignment = fetchSequences(ids, alignment);
      } catch (Throwable r)
      {
        inProgress = false;
        String msg = "Aborting ID retrieval after " + v
                + " chunks. Unexpected problem (" + r.getLocalizedMessage()
                + ")";
        System.err.println(msg);
        r.printStackTrace();
        break;
      }
    }

    if (alignment == null)
    {
      return null;
    }

    /*
     * fetch and transfer genomic sequence features,
     * fetch protein product and add as cross-reference
     */
    for (int i = 0, n = allIds.size(); i < n; i++)
    {
      addFeaturesAndProduct(allIds.get(i), alignment);
    }

    List<SequenceI> seqs = alignment.getSequences();
    for (int i = 0, n = seqs.size(); i < n; i++)
    {
      getCrossReferences(seqs.get(i));
    }

    return alignment;
  }

  /**
   * Fetches Ensembl features using the /overlap REST endpoint, and adds them to
   * the sequence in the alignment. Also fetches the protein product, maps it
   * from the CDS features of the sequence, and saves it as a cross-reference of
   * the dna sequence.
   * 
   * @param accId
   * @param alignment
   */
  protected void addFeaturesAndProduct(String accId, AlignmentI alignment)
  {
    if (alignment == null)
    {
      return;
    }

    try
    {
      /*
       * get 'dummy' genomic sequence with gene, transcript, 
       * exon, cds and variation features
       */
      SequenceI genomicSequence = null;
      EnsemblFeatures gffFetcher = new EnsemblFeatures(getDomain());
      EnsemblFeatureType[] features = getFeaturesToFetch();

      // Platform.timeCheck("ESP.getsequencerec1", Platform.TIME_MARK);

      AlignmentI geneFeatures = gffFetcher.getSequenceRecords(accId,
              features);
      if (geneFeatures != null && geneFeatures.getHeight() > 0)
      {
        genomicSequence = geneFeatures.getSequenceAt(0);
      }

      // Platform.timeCheck("ESP.getsequencerec2", Platform.TIME_MARK);

      if (genomicSequence != null)
      {
        /*
         * transfer features to the query sequence
         */
        SequenceI querySeq = alignment.findName(accId, true);
        if (transferFeatures(accId, genomicSequence, querySeq))
        {

          /*
           * fetch and map protein product, and add it as a cross-reference
           * of the retrieved sequence
           */
          // Platform.timeCheck("ESP.transferFeatures", Platform.TIME_MARK);
          addProteinProduct(querySeq);
        }
      }
    } catch (IOException e)
    {
      System.err.println(
              "Error transferring Ensembl features: " + e.getMessage());
    }
    // Platform.timeCheck("ESP.addfeat done", Platform.TIME_MARK);
  }

  /**
   * Returns those sequence feature types to fetch from Ensembl. We may want
   * features either because they are of interest to the user, or as means to
   * identify the locations of the sequence on the genomic sequence (CDS
   * features identify CDS, exon features identify cDNA etc).
   * 
   * @return
   */
  protected abstract EnsemblFeatureType[] getFeaturesToFetch();

  /**
   * Fetches and maps the protein product, and adds it as a cross-reference of
   * the retrieved sequence
   */
  protected void addProteinProduct(SequenceI querySeq)
  {
    String accId = querySeq.getName();
    try
    {
      System.out.println("Adding protein product for " + accId);
      AlignmentI protein = new EnsemblProtein(getDomain())
              .getSequenceRecords(accId);
      if (protein == null || protein.getHeight() == 0)
      {
        System.out.println("No protein product found for " + accId);
        return;
      }
      SequenceI proteinSeq = protein.getSequenceAt(0);

      /*
       * need dataset sequences (to be the subject of mappings)
       */
      proteinSeq.createDatasetSequence();
      querySeq.createDatasetSequence();

      MapList mapList = AlignmentUtils.mapCdsToProtein(querySeq,
              proteinSeq);
      if (mapList != null)
      {
        // clunky: ensure Uniprot xref if we have one is on mapped sequence
        SequenceI ds = proteinSeq.getDatasetSequence();
        // TODO: Verify ensp primary ref is on proteinSeq.getDatasetSequence()
        Mapping map = new Mapping(ds, mapList);
        DBRefEntry dbr = new DBRefEntry(getDbSource(),
                getEnsemblDataVersion(), proteinSeq.getName(), map);
        querySeq.getDatasetSequence().addDBRef(dbr);
        List<DBRefEntry> uprots = DBRefUtils.selectRefs(ds.getDBRefs(),
                new String[]
                { DBRefSource.UNIPROT });
        List<DBRefEntry> upxrefs = DBRefUtils
                .selectRefs(querySeq.getDBRefs(), new String[]
                { DBRefSource.UNIPROT });
        if (uprots != null)
        {
          for (DBRefEntry up : uprots)
          {
            // locate local uniprot ref and map
            List<DBRefEntry> upx = DBRefUtils.searchRefs(upxrefs,
                    up.getAccessionId());
            DBRefEntry upxref;
            if (upx.size() != 0)
            {
              upxref = upx.get(0);

              if (upx.size() > 1)
              {
                Console.warn(
                        "Implementation issue - multiple uniprot acc on product sequence.");
              }
            }
            else
            {
              upxref = new DBRefEntry(DBRefSource.UNIPROT,
                      getEnsemblDataVersion(), up.getAccessionId());
            }

            Mapping newMap = new Mapping(ds, mapList);
            upxref.setVersion(getEnsemblDataVersion());
            upxref.setMap(newMap);
            if (upx.size() == 0)
            {
              // add the new uniprot ref
              querySeq.getDatasetSequence().addDBRef(upxref);
            }

          }
        }

        /*
         * copy exon features to protein, compute peptide variants from dna 
         * variants and add as features on the protein sequence ta-da
         */
        // JAL-3187 render on the fly instead
        // AlignmentUtils.computeProteinFeatures(querySeq, proteinSeq, mapList);
      }
    } catch (Exception e)
    {
      System.err
              .println(String.format("Error retrieving protein for %s: %s",
                      accId, e.getMessage()));
    }
  }

  /**
   * Get database xrefs from Ensembl, and attach them to the sequence
   * 
   * @param seq
   */
  protected void getCrossReferences(SequenceI seq)
  {

    // Platform.timeCheck("ESP. getdataseq ", Platform.TIME_MARK);

    while (seq.getDatasetSequence() != null)
    {
      seq = seq.getDatasetSequence();
    }

    // Platform.timeCheck("ESP. getxref ", Platform.TIME_MARK);

    EnsemblXref xrefFetcher = new EnsemblXref(getDomain(), getDbSource(),
            getEnsemblDataVersion());
    List<DBRefEntry> xrefs = xrefFetcher.getCrossReferences(seq.getName());

    for (int i = 0, n = xrefs.size(); i < n; i++)
    {
      // Platform.timeCheck("ESP. getxref + " + (i) + "/" + n,
      // Platform.TIME_MARK);
      // BH 2019.01.25 this next method was taking 174 ms PER addition for a
      // 266-reference example.
      // DBRefUtils.ensurePrimaries(seq)
      // was at the end of seq.addDBRef, so executed after ever addition!
      // This method was moved to seq.getPrimaryDBRefs()
      seq.addDBRef(xrefs.get(i));
    }

    // System.out.println("primaries are " + seq.getPrimaryDBRefs().toString());
    /*
     * and add a reference to itself
     */

    // Platform.timeCheck("ESP. getxref self ", Platform.TIME_MARK);

    DBRefEntry self = new DBRefEntry(getDbSource(), getEnsemblDataVersion(),
            seq.getName());

    // Platform.timeCheck("ESP. getxref self add ", Platform.TIME_MARK);

    seq.addDBRef(self);

    // Platform.timeCheck("ESP. seqprox done ", Platform.TIME_MARK);
  }

  /**
   * Fetches sequences for the list of accession ids and adds them to the
   * alignment. Returns the extended (or created) alignment.
   * 
   * @param ids
   * @param alignment
   * @return
   * @throws JalviewException
   * @throws IOException
   */
  protected AlignmentI fetchSequences(List<String> ids,
          AlignmentI alignment) throws JalviewException, IOException
  {
    if (!isEnsemblAvailable())
    {
      inProgress = false;
      throw new JalviewException("ENSEMBL Rest API not available.");
    }
    // Platform.timeCheck("EnsemblSeqProx.fetchSeq ", Platform.TIME_MARK);

    List<SequenceI> seqs = parseSequenceJson(ids);
    if (seqs == null)
      return alignment;

    if (seqs.isEmpty())
    {
      throw new IOException("No data returned for " + ids);
    }

    if (seqs.size() != ids.size())
    {
      System.out.println(String.format(
              "Only retrieved %d sequences for %d query strings",
              seqs.size(), ids.size()));
    }

    if (!seqs.isEmpty())
    {
      AlignmentI seqal = new Alignment(
              seqs.toArray(new SequenceI[seqs.size()]));
      for (SequenceI seq : seqs)
      {
        if (seq.getDescription() == null)
        {
          seq.setDescription(getDbName());
        }
        String name = seq.getName();
        if (ids.contains(name)
                || ids.contains(name.replace("ENSP", "ENST")))
        {
          // TODO JAL-3077 use true accession version in dbref
          DBRefEntry dbref = DBRefUtils.parseToDbRef(seq, getDbSource(),
                  getEnsemblDataVersion(), name);
          seq.addDBRef(dbref);
        }
      }
      if (alignment == null)
      {
        alignment = seqal;
      }
      else
      {
        alignment.append(seqal);
      }
    }
    return alignment;
  }

  /**
   * Parses a JSON response for a single sequence ID query
   * 
   * @param br
   * @return a single jalview.datamodel.Sequence
   * @see http://rest.ensembl.org/documentation/info/sequence_id
   */
  @SuppressWarnings("unchecked")
  protected List<SequenceI> parseSequenceJson(List<String> ids)
  {
    List<SequenceI> result = new ArrayList<>();
    try
    {
      /*
       * for now, assumes only one sequence returned; refactor if needed
       * in future to handle a JSONArray with more than one
       */
      // Platform.timeCheck("ENS seqproxy", Platform.TIME_MARK);
      Map<String, Object> val = (Map<String, Object>) getJSON(null, ids, -1,
              MODE_MAP, null);
      if (val == null)
        return null;
      Object s = val.get("desc");
      String desc = s == null ? null : s.toString();
      s = val.get("id");
      String id = s == null ? null : s.toString();
      s = val.get("seq");
      String seq = s == null ? null : s.toString();
      Sequence sequence = new Sequence(id, seq);
      if (desc != null)
      {
        sequence.setDescription(desc);
      }
      // todo JAL-3077 make a DBRefEntry with true accession version
      // s = val.get("version");
      // String version = s == null ? "0" : s.toString();
      // DBRefEntry dbref = new DBRefEntry(getDbSource(), version, id);
      // sequence.addDBRef(dbref);
      result.add(sequence);
    } catch (ParseException | IOException e)
    {
      System.err.println("Error processing JSON response: " + e.toString());
      // ignore
    }
    // Platform.timeCheck("ENS seqproxy2", Platform.TIME_MARK);
    return result;
  }

  /**
   * Returns the URL for the REST call
   * 
   * @return
   * @throws MalformedURLException
   */
  @Override
  protected URL getUrl(List<String> ids) throws MalformedURLException
  {
    /*
     * a single id is included in the URL path
     * multiple ids go in the POST body instead
     */
    StringBuffer urlstring = new StringBuffer(128);
    urlstring.append(getDomain() + "/sequence/id");
    if (ids.size() == 1)
    {
      urlstring.append("/").append(ids.get(0));
    }
    // @see https://github.com/Ensembl/ensembl-rest/wiki/Output-formats
    urlstring.append("?type=").append(getSourceEnsemblType().getType());
    urlstring.append(("&Accept=application/json"));
    urlstring.append(("&content-type=application/json"));

    String objectType = getObjectType();
    if (objectType != null)
    {
      urlstring.append("&").append(OBJECT_TYPE).append("=")
              .append(objectType);
    }

    URL url = new URL(urlstring.toString());
    return url;
  }

  /**
   * Override this method to specify object_type request parameter
   * 
   * @return
   */
  protected String getObjectType()
  {
    return null;
  }

  /**
   * A sequence/id POST request currently allows up to 50 queries
   * 
   * @see http://rest.ensembl.org/documentation/info/sequence_id_post
   */
  @Override
  public int getMaximumQueryCount()
  {
    return 50;
  }

  @Override
  protected boolean useGetRequest()
  {
    return false;
  }

  /**
   * 
   * @return the configured sequence return type for this source
   */
  protected abstract EnsemblSeqType getSourceEnsemblType();

  /**
   * Returns a list of [start, end] genomic ranges corresponding to the sequence
   * being retrieved.
   * 
   * The correspondence between the frames of reference is made by locating
   * those features on the genomic sequence which identify the retrieved
   * sequence. Specifically
   * <ul>
   * <li>genomic sequence is identified by "transcript" features with
   * ID=transcript:transcriptId</li>
   * <li>cdna sequence is identified by "exon" features with
   * Parent=transcript:transcriptId</li>
   * <li>cds sequence is identified by "CDS" features with
   * Parent=transcript:transcriptId</li>
   * </ul>
   * 
   * The returned ranges are sorted to run forwards (for positive strand) or
   * backwards (for negative strand). Aborts and returns null if both positive
   * and negative strand are found (this should not normally happen).
   * 
   * @param sourceSequence
   * @param accId
   * @param start
   *          the start position of the sequence we are mapping to
   * @return
   */
  protected MapList getGenomicRangesFromFeatures(SequenceI sourceSequence,
          String accId, int start)
  {
    List<SequenceFeature> sfs = getIdentifyingFeatures(sourceSequence,
            accId);
    if (sfs.isEmpty())
    {
      return null;
    }

    /*
     * generously initial size for number of cds regions
     * (worst case titin Q8WZ42 has c. 313 exons)
     */
    List<int[]> regions = new ArrayList<>(100);
    int mappedLength = 0;
    int direction = 1; // forward
    boolean directionSet = false;

    for (SequenceFeature sf : sfs)
    {
      int strand = sf.getStrand();
      strand = strand == 0 ? 1 : strand; // treat unknown as forward

      if (directionSet && strand != direction)
      {
        // abort - mix of forward and backward
        System.err
                .println("Error: forward and backward strand for " + accId);
        return null;
      }
      direction = strand;
      directionSet = true;

      /*
       * add to CDS ranges, semi-sorted forwards/backwards
       */
      if (strand < 0)
      {
        regions.add(0, new int[] { sf.getEnd(), sf.getBegin() });
      }
      else
      {
        regions.add(new int[] { sf.getBegin(), sf.getEnd() });
      }
      mappedLength += Math.abs(sf.getEnd() - sf.getBegin() + 1);
    }

    if (regions.isEmpty())
    {
      System.out.println("Failed to identify target sequence for " + accId
              + " from genomic features");
      return null;
    }

    /*
     * a final sort is needed since Ensembl returns CDS sorted within source
     * (havana / ensembl_havana)
     */
    Collections.sort(regions, direction == 1 ? IntRangeComparator.ASCENDING
            : IntRangeComparator.DESCENDING);

    List<int[]> to = Arrays
            .asList(new int[]
            { start, start + mappedLength - 1 });

    return new MapList(regions, to, 1, 1);
  }

  /**
   * Answers a list of sequence features that mark positions of the genomic
   * sequence feature which are within the sequence being retrieved. For
   * example, an 'exon' feature whose parent is the target transcript marks the
   * cdna positions of the transcript. For a gene sequence, this is trivially
   * just the 'gene' feature with matching gene id.
   * 
   * @param seq
   * @param accId
   * @return
   */
  protected abstract List<SequenceFeature> getIdentifyingFeatures(
          SequenceI seq, String accId);

  int bhtest = 0;

  /**
   * Transfers the sequence feature to the target sequence, locating its start
   * and end range based on the mapping. Features which do not overlap the
   * target sequence are ignored.
   * 
   * @param sf
   * @param targetSequence
   * @param mapping
   *          mapping from the sequence feature's coordinates to the target
   *          sequence
   * @param forwardStrand
   */
  protected void transferFeature(SequenceFeature sf,
          SequenceI targetSequence, MapList mapping, boolean forwardStrand)
  {
    int start = sf.getBegin();
    int end = sf.getEnd();
    int[] mappedRange = mapping.locateInTo(start, end);

    if (mappedRange != null)
    {
      // Platform.timeCheck(null, Platform.TIME_SET);
      String group = sf.getFeatureGroup();
      if (".".equals(group))
      {
        group = getDbSource();
      }
      int newBegin = Math.min(mappedRange[0], mappedRange[1]);
      int newEnd = Math.max(mappedRange[0], mappedRange[1]);
      // Platform.timeCheck(null, Platform.TIME_MARK);
      bhtest++;
      // 280 ms/1000 here:
      SequenceFeature copy = new SequenceFeature(sf, newBegin, newEnd,
              group, sf.getScore());
      // 0.175 ms here:
      targetSequence.addSequenceFeature(copy);

      /*
       * for sequence_variant on reverse strand, have to convert the allele
       * values to their complements
       */
      if (!forwardStrand && SequenceOntologyFactory.getInstance()
              .isA(sf.getType(), SequenceOntologyI.SEQUENCE_VARIANT))
      {
        reverseComplementAlleles(copy);
      }
    }
  }

  /**
   * Change the 'alleles' value of a feature by converting to complementary
   * bases, and also update the feature description to match
   * 
   * @param sf
   */
  static void reverseComplementAlleles(SequenceFeature sf)
  {
    final String alleles = (String) sf.getValue(Gff3Helper.ALLELES);
    if (alleles == null)
    {
      return;
    }
    StringBuilder complement = new StringBuilder(alleles.length());
    for (String allele : alleles.split(","))
    {
      reverseComplementAllele(complement, allele);
    }
    String comp = complement.toString();
    sf.setValue(Gff3Helper.ALLELES, comp);
    sf.setDescription(comp);
  }

  /**
   * Makes the 'reverse complement' of the given allele and appends it to the
   * buffer, after a comma separator if not the first
   * 
   * @param complement
   * @param allele
   */
  static void reverseComplementAllele(StringBuilder complement,
          String allele)
  {
    if (complement.length() > 0)
    {
      complement.append(",");
    }

    /*
     * some 'alleles' are actually descriptive terms 
     * e.g. HGMD_MUTATION, PhenCode_variation
     * - we don't want to 'reverse complement' these
     */
    if (!Comparison.isNucleotideSequence(allele, true))
    {
      complement.append(allele);
    }
    else
    {
      for (int i = allele.length() - 1; i >= 0; i--)
      {
        complement.append(Dna.getComplement(allele.charAt(i)));
      }
    }
  }

  /**
   * Transfers features from sourceSequence to targetSequence
   * 
   * @param accessionId
   * @param sourceSequence
   * @param targetSequence
   * @return true if any features were transferred, else false
   */
  protected boolean transferFeatures(String accessionId,
          SequenceI sourceSequence, SequenceI targetSequence)
  {
    if (sourceSequence == null || targetSequence == null)
    {
      return false;
    }

    // long start = System.currentTimeMillis();
    List<SequenceFeature> sfs = sourceSequence.getFeatures()
            .getPositionalFeatures();
    MapList mapping = getGenomicRangesFromFeatures(sourceSequence,
            accessionId, targetSequence.getStart());
    if (mapping == null)
    {
      return false;
    }

    // Platform.timeCheck("ESP. xfer " + sfs.size(), Platform.TIME_MARK);

    boolean result = transferFeatures(sfs, targetSequence, mapping,
            accessionId);
    // System.out.println("transferFeatures (" + (sfs.size()) + " --> "
    // + targetSequence.getFeatures().getFeatureCount(true) + ") to "
    // + targetSequence.getName() + " took "
    // + (System.currentTimeMillis() - start) + "ms");
    return result;
  }

  /**
   * Transfer features to the target sequence. The start/end positions are
   * converted using the mapping. Features which do not overlap are ignored.
   * Features whose parent is not the specified identifier are also ignored.
   * 
   * @param sfs
   * @param targetSequence
   * @param mapping
   * @param parentId
   * @return
   */
  protected boolean transferFeatures(List<SequenceFeature> sfs,
          SequenceI targetSequence, MapList mapping, String parentId)
  {
    final boolean forwardStrand = mapping.isFromForwardStrand();

    /*
     * sort features by start position (which corresponds to end
     * position descending if reverse strand) so as to add them in
     * 'forwards' order to the target sequence
     */
    SequenceFeatures.sortFeatures(sfs, forwardStrand);

    boolean transferred = false;

    for (int i = 0, n = sfs.size(); i < n; i++)
    {

      // if ((i%1000) == 0) {
      //// Platform.timeCheck("Feature " + bhtest, Platform.TIME_GET);
      // Platform.timeCheck("ESP. xferFeature + " + (i) + "/" + n,
      // Platform.TIME_MARK);
      // }

      SequenceFeature sf = sfs.get(i);
      if (retainFeature(sf, parentId))
      {
        transferFeature(sf, targetSequence, mapping, forwardStrand);
        transferred = true;
      }
    }

    return transferred;
  }

  /**
   * Answers true if the feature type is one we want to keep for the sequence.
   * Some features are only retrieved in order to identify the sequence range,
   * and may then be discarded as redundant information (e.g. "CDS" feature for
   * a CDS sequence).
   */
  @SuppressWarnings("unused")
  protected boolean retainFeature(SequenceFeature sf, String accessionId)
  {
    return true; // override as required
  }

  /**
   * Answers true if the feature has a Parent which refers to the given
   * accession id, or if the feature has no parent. Answers false if the
   * feature's Parent is for a different accession id.
   * 
   * @param sf
   * @param identifier
   * @return
   */
  protected boolean featureMayBelong(SequenceFeature sf, String identifier)
  {
    String parent = (String) sf.getValue(PARENT);
    if (parent != null && !parent.equalsIgnoreCase(identifier))
    {
      // this genomic feature belongs to a different transcript
      return false;
    }
    return true;
  }

  /**
   * Answers a short description of the sequence fetcher
   */
  @Override
  public String getDescription()
  {
    return "Ensembl " + getSourceEnsemblType().getType()
            + " sequence with variant features";
  }

  /**
   * Returns a (possibly empty) list of features on the sequence which have the
   * specified sequence ontology term (or a sub-type of it), and the given
   * identifier as parent
   * 
   * @param sequence
   * @param term
   * @param parentId
   * @return
   */
  protected List<SequenceFeature> findFeatures(SequenceI sequence,
          String term, String parentId)
  {
    List<SequenceFeature> result = new ArrayList<>();

    List<SequenceFeature> sfs = sequence.getFeatures()
            .getFeaturesByOntology(term);
    for (SequenceFeature sf : sfs)
    {
      String parent = (String) sf.getValue(PARENT);
      if (parent != null && parent.equalsIgnoreCase(parentId))
      {
        result.add(sf);
      }
    }

    return result;
  }

  /**
   * Answers true if the feature type is either 'NMD_transcript_variant' or
   * 'transcript' (or one of its sub-types in the Sequence Ontology). This is
   * because NMD_transcript_variant behaves like 'transcript' in Ensembl
   * although strictly speaking it is not (it is a sub-type of
   * sequence_variant).
   * <p>
   * (This test was needed when fetching transcript features as GFF. As we are
   * now fetching as JSON, all features have type 'transcript' so the check for
   * NMD_transcript_variant is redundant. Left in for any future case arising.)
   * 
   * @param featureType
   * @return
   */
  public static boolean isTranscript(String featureType)
  {
    return SequenceOntologyI.NMD_TRANSCRIPT_VARIANT.equals(featureType)
            || SequenceOntologyFactory.getInstance().isA(featureType,
                    SequenceOntologyI.TRANSCRIPT);
  }
}
