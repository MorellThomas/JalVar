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

import jalview.api.FeatureColourI;
import jalview.api.FeatureSettingsModelI;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.GeneLociI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.datamodel.features.SequenceFeatures;
import jalview.io.gff.SequenceOntologyFactory;
import jalview.io.gff.SequenceOntologyI;
import jalview.schemes.FeatureColour;
import jalview.schemes.FeatureSettingsAdapter;
import jalview.util.MapList;
import jalview.util.Platform;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.stevesoft.pat.Regex;

/**
 * A class that fetches genomic sequence and all transcripts for an Ensembl gene
 * 
 * @author gmcarstairs
 */
public class EnsemblGene extends EnsemblSeqProxy
{
  /*
   * accepts anything as we will attempt lookup of gene or 
   * transcript id or gene name
   */
  private static final Regex ACCESSION_REGEX = new Regex(".*");

  private static final EnsemblFeatureType[] FEATURES_TO_FETCH = {
      EnsemblFeatureType.gene, EnsemblFeatureType.transcript,
      EnsemblFeatureType.exon, EnsemblFeatureType.cds,
      EnsemblFeatureType.variation };

  private static final String CHROMOSOME = "chromosome";

  /**
   * Default constructor (to use rest.ensembl.org)
   */
  public EnsemblGene()
  {
    super();
  }

  /**
   * Constructor given the target domain to fetch data from
   * 
   * @param d
   */
  public EnsemblGene(String d)
  {
    super(d);
  }

  @Override
  public String getDbName()
  {
    return "ENSEMBL";
  }

  @Override
  protected EnsemblFeatureType[] getFeaturesToFetch()
  {
    return FEATURES_TO_FETCH;
  }

  @Override
  protected EnsemblSeqType getSourceEnsemblType()
  {
    return EnsemblSeqType.GENOMIC;
  }

  @Override
  protected String getObjectType()
  {
    return OBJECT_TYPE_GENE;
  }

  /**
   * Returns an alignment containing the gene(s) for the given gene or
   * transcript identifier, or external identifier (e.g. Uniprot id). If given a
   * gene name or external identifier, returns any related gene sequences found
   * for model organisms. If only a single gene is queried for, then its
   * transcripts are also retrieved and added to the alignment. <br>
   * Method:
   * <ul>
   * <li>resolves a transcript identifier by looking up its parent gene id</li>
   * <li>resolves an external identifier by looking up xref-ed gene ids</li>
   * <li>fetches the gene sequence</li>
   * <li>fetches features on the sequence</li>
   * <li>identifies "transcript" features whose Parent is the requested
   * gene</li>
   * <li>fetches the transcript sequence for each transcript</li>
   * <li>makes a mapping from the gene to each transcript</li>
   * <li>copies features from gene to transcript sequences</li>
   * <li>fetches the protein sequence for each transcript, maps and saves it as
   * a cross-reference</li>
   * <li>aligns each transcript against the gene sequence based on the position
   * mappings</li>
   * </ul>
   * 
   * @param query
   *          a single gene or transcript identifier or gene name
   * @return an alignment containing a gene, and possibly transcripts, or null
   */
  @Override
  public AlignmentI getSequenceRecords(String query) throws Exception
  {
    /*
     * convert to a non-duplicated list of gene identifiers
     */
    List<String> geneIds = getGeneIds(query);
    AlignmentI al = null;
    for (String geneId : geneIds)
    {
      /*
       * fetch the gene sequence(s) with features and xrefs
       */
      AlignmentI geneAlignment = super.getSequenceRecords(geneId);
      if (geneAlignment == null)
      {
        continue;
      }

      if (geneAlignment.getHeight() == 1)
      {
        // ensure id has 'correct' case for the Ensembl identifier
        geneId = geneAlignment.getSequenceAt(0).getName();
        findGeneLoci(geneAlignment.getSequenceAt(0), geneId);
        getTranscripts(geneAlignment, geneId);
      }
      if (al == null)
      {
        al = geneAlignment;
      }
      else
      {
        al.append(geneAlignment);
      }
    }
    return al;
  }

  /**
   * Calls the /lookup/id REST service, parses the response for gene
   * coordinates, and if successful, adds these to the sequence. If this fails,
   * fall back on trying to parse the sequence description in case it is in
   * Ensembl-gene format e.g. chromosome:GRCh38:17:45051610:45109016:1.
   * 
   * @param seq
   * @param geneId
   */
  void findGeneLoci(SequenceI seq, String geneId)
  {
    GeneLociI geneLoci = new EnsemblLookup(getDomain()).getGeneLoci(geneId);
    if (geneLoci != null)
    {
      seq.setGeneLoci(geneLoci.getSpeciesId(), geneLoci.getAssemblyId(),
              geneLoci.getChromosomeId(), geneLoci.getMapping());
    }
    else
    {
      parseChromosomeLocations(seq);
    }
  }

  /**
   * Parses and saves fields of an Ensembl-style description e.g.
   * chromosome:GRCh38:17:45051610:45109016:1
   * 
   * @param seq
   */
  boolean parseChromosomeLocations(SequenceI seq)
  {
    String description = seq.getDescription();
    if (description == null)
    {
      return false;
    }
    String[] tokens = description.split(":");
    if (tokens.length == 6 && tokens[0].startsWith(CHROMOSOME))
    {
      String ref = tokens[1];
      String chrom = tokens[2];
      try
      {
        int chStart = Integer.parseInt(tokens[3]);
        int chEnd = Integer.parseInt(tokens[4]);
        boolean forwardStrand = "1".equals(tokens[5]);
        String species = ""; // not known here
        int[] from = new int[] { seq.getStart(), seq.getEnd() };
        int[] to = new int[] { forwardStrand ? chStart : chEnd,
            forwardStrand ? chEnd : chStart };
        MapList map = new MapList(from, to, 1, 1);
        seq.setGeneLoci(species, ref, chrom, map);
        return true;
      } catch (NumberFormatException e)
      {
        System.err.println("Bad integers in description " + description);
      }
    }
    return false;
  }

  /**
   * Converts a query, which may contain one or more gene, transcript, or
   * external (to Ensembl) identifiers, into a non-redundant list of gene
   * identifiers.
   * 
   * @param accessions
   * @return
   */
  List<String> getGeneIds(String accessions)
  {
    List<String> geneIds = new ArrayList<>();

    for (String acc : accessions.split(getAccessionSeparator()))
    {
      /*
       * First try lookup as an Ensembl (gene or transcript) identifier
       */
      String geneId = new EnsemblLookup(getDomain()).getGeneId(acc);
      if (geneId != null)
      {
        if (!geneIds.contains(geneId))
        {
          geneIds.add(geneId);
        }
      }
      else
      {
        /*
         * if given a gene or other external name, lookup and fetch 
         * the corresponding gene for all model organisms 
         */
        List<String> ids = new EnsemblSymbol(getDomain(), getDbSource(),
                getDbVersion()).getGeneIds(acc);
        for (String id : ids)
        {
          if (!geneIds.contains(id))
          {
            geneIds.add(id);
          }
        }
      }
    }
    return geneIds;
  }

  /**
   * Constructs all transcripts for the gene, as identified by "transcript"
   * features whose Parent is the requested gene. The coding transcript
   * sequences (i.e. with introns omitted) are added to the alignment.
   * 
   * @param al
   * @param accId
   * @throws Exception
   */
  protected void getTranscripts(AlignmentI al, String accId)
          throws Exception
  {
    SequenceI gene = al.getSequenceAt(0);
    List<SequenceFeature> transcriptFeatures = getTranscriptFeatures(accId,
            gene);

    for (SequenceFeature transcriptFeature : transcriptFeatures)
    {
      makeTranscript(transcriptFeature, al, gene);
    }

    clearGeneFeatures(gene);
  }

  /**
   * Remove unwanted features (transcript, exon, CDS) from the gene sequence
   * after we have used them to derive transcripts and transfer features
   * 
   * @param gene
   */
  protected void clearGeneFeatures(SequenceI gene)
  {
    /*
     * Note we include NMD_transcript_variant here because it behaves like 
     * 'transcript' in Ensembl, although strictly speaking it is not 
     * (it is a sub-type of sequence_variant)    
     */
    String[] soTerms = new String[] {
        SequenceOntologyI.NMD_TRANSCRIPT_VARIANT,
        SequenceOntologyI.TRANSCRIPT, SequenceOntologyI.EXON,
        SequenceOntologyI.CDS };
    List<SequenceFeature> sfs = gene.getFeatures()
            .getFeaturesByOntology(soTerms);
    for (SequenceFeature sf : sfs)
    {
      gene.deleteFeature(sf);
    }
  }

  /**
   * Constructs a spliced transcript sequence by finding 'exon' features for the
   * given id (or failing that 'CDS'). Copies features on to the new sequence.
   * 'Aligns' the new sequence against the gene sequence by padding with gaps,
   * and adds it to the alignment.
   * 
   * @param transcriptFeature
   * @param al
   *          the alignment to which to add the new sequence
   * @param gene
   *          the parent gene sequence, with features
   * @return
   */
  SequenceI makeTranscript(SequenceFeature transcriptFeature, AlignmentI al,
          SequenceI gene)
  {
    String accId = getTranscriptId(transcriptFeature);
    if (accId == null)
    {
      return null;
    }

    /*
     * NB we are mapping from gene sequence (not genome), so do not
     * need to check for reverse strand (gene and transcript sequences 
     * are in forward sense)
     */

    /*
     * make a gene-length sequence filled with gaps
     * we will fill in the bases for transcript regions
     */
    char[] seqChars = new char[gene.getLength()];
    Arrays.fill(seqChars, al.getGapCharacter());

    /*
     * look for exon features of the transcript, failing that for CDS
     * (for example ENSG00000124610 has 1 CDS but no exon features)
     */
    String parentId = accId;
    List<SequenceFeature> splices = findFeatures(gene,
            SequenceOntologyI.EXON, parentId);
    if (splices.isEmpty())
    {
      splices = findFeatures(gene, SequenceOntologyI.CDS, parentId);
    }
    SequenceFeatures.sortFeatures(splices, true);

    int transcriptLength = 0;
    final char[] geneChars = gene.getSequence();
    int offset = gene.getStart(); // to convert to 0-based positions
    List<int[]> mappedFrom = new ArrayList<>();

    for (SequenceFeature sf : splices)
    {
      int start = sf.getBegin() - offset;
      int end = sf.getEnd() - offset;
      int spliceLength = end - start + 1;
      System.arraycopy(geneChars, start, seqChars, start, spliceLength);
      transcriptLength += spliceLength;
      mappedFrom.add(new int[] { sf.getBegin(), sf.getEnd() });
    }

    Sequence transcript = new Sequence(accId, seqChars, 1,
            transcriptLength);

    /*
     * Ensembl has gene name as transcript Name
     * EnsemblGenomes doesn't, but has a url-encoded description field
     */
    String description = transcriptFeature.getDescription();
    if (description == null)
    {
      description = (String) transcriptFeature.getValue(DESCRIPTION);
    }
    if (description != null)
    {
      try
      {
        transcript.setDescription(URLDecoder.decode(description, "UTF-8"));
      } catch (UnsupportedEncodingException e)
      {
        e.printStackTrace(); // as if
      }
    }
    transcript.createDatasetSequence();

    al.addSequence(transcript);

    /*
     * transfer features to the new sequence; we use EnsemblCdna to do this,
     * to filter out unwanted features types (see method retainFeature)
     */
    List<int[]> mapTo = new ArrayList<>();
    mapTo.add(new int[] { 1, transcriptLength });
    MapList mapping = new MapList(mappedFrom, mapTo, 1, 1);
    EnsemblCdna cdna = new EnsemblCdna(getDomain());
    cdna.transferFeatures(gene.getFeatures().getPositionalFeatures(),
            transcript.getDatasetSequence(), mapping, parentId);

    mapTranscriptToChromosome(transcript, gene, mapping);

    /*
     * fetch and save cross-references
     */
    cdna.getCrossReferences(transcript);

    /*
     * and finally fetch the protein product and save as a cross-reference
     */
    cdna.addProteinProduct(transcript);

    return transcript;
  }

  /**
   * If the gene has a mapping to chromosome coordinates, derive the transcript
   * chromosome regions and save on the transcript sequence
   * 
   * @param transcript
   * @param gene
   * @param mapping
   *          the mapping from gene to transcript positions
   */
  protected void mapTranscriptToChromosome(SequenceI transcript,
          SequenceI gene, MapList mapping)
  {
    GeneLociI loci = gene.getGeneLoci();
    if (loci == null)
    {
      return;
    }

    MapList geneMapping = loci.getMapping();

    List<int[]> exons = mapping.getFromRanges();
    List<int[]> transcriptLoci = new ArrayList<>();

    for (int[] exon : exons)
    {
      transcriptLoci.add(geneMapping.locateInTo(exon[0], exon[1]));
    }

    List<int[]> transcriptRange = Arrays
            .asList(new int[]
            { transcript.getStart(), transcript.getEnd() });
    MapList mapList = new MapList(transcriptRange, transcriptLoci, 1, 1);

    transcript.setGeneLoci(loci.getSpeciesId(), loci.getAssemblyId(),
            loci.getChromosomeId(), mapList);
  }

  /**
   * Returns the 'transcript_id' property of the sequence feature (or null)
   * 
   * @param feature
   * @return
   */
  protected String getTranscriptId(SequenceFeature feature)
  {
    return (String) feature.getValue(JSON_ID);
  }

  /**
   * Returns a list of the transcript features on the sequence whose Parent is
   * the gene for the accession id.
   * <p>
   * Transcript features are those of type "transcript", or any of its sub-types
   * in the Sequence Ontology e.g. "mRNA", "processed_transcript". We also
   * include "NMD_transcript_variant", because this type behaves like a
   * transcript identifier in Ensembl, although strictly speaking it is not in
   * the SO.
   * 
   * @param accId
   * @param geneSequence
   * @return
   */
  protected List<SequenceFeature> getTranscriptFeatures(String accId,
          SequenceI geneSequence)
  {
    List<SequenceFeature> transcriptFeatures = new ArrayList<>();

    String parentIdentifier = accId;

    List<SequenceFeature> sfs = geneSequence.getFeatures()
            .getFeaturesByOntology(SequenceOntologyI.TRANSCRIPT);
    sfs.addAll(geneSequence.getFeatures().getPositionalFeatures(
            SequenceOntologyI.NMD_TRANSCRIPT_VARIANT));

    for (SequenceFeature sf : sfs)
    {
      String parent = (String) sf.getValue(PARENT);
      if (parentIdentifier.equalsIgnoreCase(parent))
      {
        transcriptFeatures.add(sf);
      }
    }

    return transcriptFeatures;
  }

  @Override
  public String getDescription()
  {
    return "Fetches all transcripts and variant features for a gene or transcript";
  }

  /**
   * Default test query is a gene id (can also enter a transcript id)
   */
  @Override
  public String getTestQuery()
  {
    return Platform.isJS() ? "ENSG00000123569" : "ENSG00000157764";
    // ENSG00000123569 // H2BFWT histone, 2 transcripts, reverse strand
    // ENSG00000157764 // BRAF, 5 transcripts, reverse strand
    // ENSG00000090266 // NDUFB2, 15 transcripts, forward strand
    // ENSG00000101812 // H2BFM histone, 3 transcripts, forward strand
  }

  /**
   * Answers a list of sequence features (if any) whose type is 'gene' (or a
   * subtype of gene in the Sequence Ontology), and whose ID is the accession we
   * are retrieving
   */
  @Override
  protected List<SequenceFeature> getIdentifyingFeatures(SequenceI seq,
          String accId)
  {
    List<SequenceFeature> result = new ArrayList<>();
    List<SequenceFeature> sfs = seq.getFeatures()
            .getFeaturesByOntology(SequenceOntologyI.GENE);
    for (SequenceFeature sf : sfs)
    {
      String id = (String) sf.getValue(JSON_ID);
      if (accId.equalsIgnoreCase(id))
      {
        result.add(sf);
      }
    }
    return result;
  }

  /**
   * Answers true unless feature type is 'gene', or 'transcript' with a parent
   * which is a different gene. We need the gene features to identify the range,
   * but it is redundant information on the gene sequence. Checking the parent
   * allows us to drop transcript features which belong to different
   * (overlapping) genes.
   */
  @Override
  protected boolean retainFeature(SequenceFeature sf, String accessionId)
  {
    SequenceOntologyI so = SequenceOntologyFactory.getInstance();
    String type = sf.getType();
    if (so.isA(type, SequenceOntologyI.GENE))
    {
      return false;
    }
    if (isTranscript(type))
    {
      String parent = (String) sf.getValue(PARENT);
      if (!accessionId.equalsIgnoreCase(parent))
      {
        return false;
      }
    }
    return true;
  }

  /**
   * Override to do nothing as Ensembl doesn't return a protein sequence for a
   * gene identifier
   */
  @Override
  protected void addProteinProduct(SequenceI querySeq)
  {
  }

  @Override
  public Regex getAccessionValidator()
  {
    return ACCESSION_REGEX;
  }

  /**
   * Returns a descriptor for suitable feature display settings with
   * <ul>
   * <li>only exon or sequence_variant features (or their subtypes in the
   * Sequence Ontology) visible</li>
   * <li>variant features coloured red</li>
   * <li>exon features coloured by label (exon name)</li>
   * <li>variants displayed above (on top of) exons</li>
   * </ul>
   */
  @Override
  public FeatureSettingsModelI getFeatureColourScheme()
  {
    return new FeatureSettingsAdapter()
    {
      SequenceOntologyI so = SequenceOntologyFactory.getInstance();

      @Override
      public boolean isFeatureHidden(String type)
      {
        return (!so.isA(type, SequenceOntologyI.EXON)
                && !so.isA(type, SequenceOntologyI.SEQUENCE_VARIANT));
      }

      @Override
      public FeatureColourI getFeatureColour(String type)
      {
        if (so.isA(type, SequenceOntologyI.EXON))
        {
          return new FeatureColour()
          {
            @Override
            public boolean isColourByLabel()
            {
              return true;
            }
          };
        }
        if (so.isA(type, SequenceOntologyI.SEQUENCE_VARIANT))
        {
          return new FeatureColour()
          {

            @Override
            public Color getColour()
            {
              return Color.RED;
            }
          };
        }
        return null;
      }

      /**
       * order to render sequence_variant after exon after the rest
       */
      @Override
      public int compare(String feature1, String feature2)
      {
        if (so.isA(feature1, SequenceOntologyI.SEQUENCE_VARIANT))
        {
          return +1;
        }
        if (so.isA(feature2, SequenceOntologyI.SEQUENCE_VARIANT))
        {
          return -1;
        }
        if (so.isA(feature1, SequenceOntologyI.EXON))
        {
          return +1;
        }
        if (so.isA(feature2, SequenceOntologyI.EXON))
        {
          return -1;
        }
        return 0;
      }
    };
  }

}
