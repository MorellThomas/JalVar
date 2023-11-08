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
package jalview.gui;

import jalview.analysis.AlignmentUtils;
import jalview.analysis.CrossRef;
import jalview.api.AlignmentViewPanel;
import jalview.api.FeatureSettingsModelI;
import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.GeneLociI;
import jalview.datamodel.SequenceI;
import jalview.ext.ensembl.EnsemblInfo;
import jalview.ext.ensembl.EnsemblMap;
import jalview.io.gff.SequenceOntologyI;
import jalview.structure.StructureSelectionManager;
import jalview.util.DBRefUtils;
import jalview.util.MapList;
import jalview.util.MappingUtils;
import jalview.util.MessageManager;
import jalview.viewmodel.seqfeatures.FeatureRendererModel;
import jalview.ws.SequenceFetcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Factory constructor and runnable for discovering and displaying
 * cross-references for a set of aligned sequences
 * 
 * @author jprocter
 *
 */
public class CrossRefAction implements Runnable
{
  private AlignFrame alignFrame;

  private SequenceI[] sel;

  private final boolean _odna;

  private String source;

  List<AlignmentViewPanel> xrefViews = new ArrayList<>();

  List<AlignmentViewPanel> getXrefViews()
  {
    return xrefViews;
  }

  @Override
  public void run()
  {
    final long sttime = System.currentTimeMillis();
    alignFrame.setProgressBar(MessageManager.formatMessage(
            "status.searching_for_sequences_from", new Object[]
            { source }), sttime);
    try
    {
      AlignmentI alignment = alignFrame.getViewport().getAlignment();
      AlignmentI dataset = alignment.getDataset() == null ? alignment
              : alignment.getDataset();
      boolean dna = alignment.isNucleotide();
      if (_odna != dna)
      {
        System.err
                .println("Conflict: showProducts for alignment originally "
                        + "thought to be " + (_odna ? "DNA" : "Protein")
                        + " now searching for " + (dna ? "DNA" : "Protein")
                        + " Context.");
      }
      AlignmentI xrefs = new CrossRef(sel, dataset)
              .findXrefSequences(source, dna);
      if (xrefs == null)
      {
        return;
      }

      /*
       * try to look up chromosomal coordinates for nucleotide
       * sequences (if not already retrieved)
       */
      findGeneLoci(xrefs.getSequences());

      /*
       * get display scheme (if any) to apply to features
       */
      FeatureSettingsModelI featureColourScheme = new SequenceFetcher()
              .getFeatureColourScheme(source);

      if (dna && AlignmentUtils.looksLikeEnsembl(alignment))
      {
        // override default featureColourScheme so products have Ensembl variant
        // colours
        featureColourScheme = new SequenceFetcher()
                .getFeatureColourScheme(DBRefSource.ENSEMBL);
      }

      AlignmentI xrefsAlignment = makeCrossReferencesAlignment(dataset,
              xrefs);
      if (!dna)
      {
        xrefsAlignment = AlignmentUtils.makeCdsAlignment(
                xrefsAlignment.getSequencesArray(), dataset, sel);
        xrefsAlignment.alignAs(alignment);
      }

      /*
       * If we are opening a splitframe, make a copy of this alignment (sharing the same dataset
       * sequences). If we are DNA, drop introns and update mappings
       */
      AlignmentI copyAlignment = null;

      if (Cache.getDefault(Preferences.ENABLE_SPLIT_FRAME, true))
      {
        copyAlignment = copyAlignmentForSplitFrame(alignment, dataset, dna,
                xrefs, xrefsAlignment);
        if (copyAlignment == null)
        {
          return; // failed
        }
      }

      /*
       * build AlignFrame(s) according to available alignment data
       */
      AlignFrame newFrame = new AlignFrame(xrefsAlignment,
              AlignFrame.DEFAULT_WIDTH, AlignFrame.DEFAULT_HEIGHT);
      if (Cache.getDefault("HIDE_INTRONS", true))
      {
        newFrame.hideFeatureColumns(SequenceOntologyI.EXON, false);
      }
      String newtitle = String.format("%s %s %s",
              dna ? MessageManager.getString("label.proteins")
                      : MessageManager.getString("label.nucleotides"),
              MessageManager.getString("label.for"), alignFrame.getTitle());
      newFrame.setTitle(newtitle);

      if (copyAlignment == null)
      {
        /*
         * split frame display is turned off in preferences file
         */
        Desktop.addInternalFrame(newFrame, newtitle,
                AlignFrame.DEFAULT_WIDTH, AlignFrame.DEFAULT_HEIGHT);
        xrefViews.add(newFrame.alignPanel);
        return; // via finally clause
      }

      AlignFrame copyThis = new AlignFrame(copyAlignment,
              AlignFrame.DEFAULT_WIDTH, AlignFrame.DEFAULT_HEIGHT);
      copyThis.setTitle(alignFrame.getTitle());

      boolean showSequenceFeatures = alignFrame.getViewport()
              .isShowSequenceFeatures();
      newFrame.setShowSeqFeatures(showSequenceFeatures);
      copyThis.setShowSeqFeatures(showSequenceFeatures);
      FeatureRendererModel myFeatureStyling = alignFrame.alignPanel
              .getSeqPanel().seqCanvas.getFeatureRenderer();

      /*
       * copy feature rendering settings to split frame
       */
      FeatureRendererModel fr1 = newFrame.alignPanel.getSeqPanel().seqCanvas
              .getFeatureRenderer();
      fr1.transferSettings(myFeatureStyling);
      fr1.findAllFeatures(true);
      FeatureRendererModel fr2 = copyThis.alignPanel.getSeqPanel().seqCanvas
              .getFeatureRenderer();
      fr2.transferSettings(myFeatureStyling);
      fr2.findAllFeatures(true);

      /*
       * apply 'database source' feature configuration
       * if any - first to the new splitframe view about to be displayed
       */

      newFrame.getViewport().applyFeaturesStyle(featureColourScheme);
      copyThis.getViewport().applyFeaturesStyle(featureColourScheme);

      /*
       * and for JAL-3330 also to original alignFrame view(s)
       * this currently trashes any original settings.
       */
      for (AlignmentViewPanel origpanel : alignFrame.getAlignPanels())
      {
        origpanel.getAlignViewport()
                .mergeFeaturesStyle(featureColourScheme);
      }

      SplitFrame sf = new SplitFrame(dna ? copyThis : newFrame,
              dna ? newFrame : copyThis);

      newFrame.setVisible(true);
      copyThis.setVisible(true);
      String linkedTitle = MessageManager
              .getString("label.linked_view_title");
      Desktop.addInternalFrame(sf, linkedTitle, -1, -1);
      sf.adjustInitialLayout();

      // finally add the top, then bottom frame to the view list
      xrefViews.add(dna ? copyThis.alignPanel : newFrame.alignPanel);
      xrefViews.add(!dna ? copyThis.alignPanel : newFrame.alignPanel);

    } catch (OutOfMemoryError e)
    {
      new OOMWarning("whilst fetching crossreferences", e);
    } catch (Throwable e)
    {
      Console.error("Error when finding crossreferences", e);
    } finally
    {
      alignFrame.setProgressBar(MessageManager.formatMessage(
              "status.finished_searching_for_sequences_from", new Object[]
              { source }), sttime);
    }
  }

  /**
   * Tries to add chromosomal coordinates to any nucleotide sequence which does
   * not already have them. Coordinates are retrieved from Ensembl given an
   * Ensembl identifier, either on the sequence itself or on a peptide sequence
   * it has a reference to.
   * 
   * <pre>
   * Example (human):
   * - fetch EMBLCDS cross-references for Uniprot entry P30419
   * - the EMBL sequences do not have xrefs to Ensembl
   * - the Uniprot entry has xrefs to 
   *    ENSP00000258960, ENSP00000468424, ENST00000258960, ENST00000592782
   * - either of the transcript ids can be used to retrieve gene loci e.g.
   *    http://rest.ensembl.org/map/cds/ENST00000592782/1..100000
   * Example (invertebrate):
   * - fetch EMBLCDS cross-references for Uniprot entry Q43517 (FER1_SOLLC)
   * - the Uniprot entry has an xref to ENSEMBLPLANTS Solyc10g044520.1.1
   * - can retrieve gene loci with
   *    http://rest.ensemblgenomes.org/map/cds/Solyc10g044520.1.1/1..100000
   * </pre>
   * 
   * @param sequences
   */
  public static void findGeneLoci(List<SequenceI> sequences)
  {
    Map<DBRefEntry, GeneLociI> retrievedLoci = new HashMap<>();
    for (SequenceI seq : sequences)
    {
      findGeneLoci(seq, retrievedLoci);
    }
  }

  /**
   * Tres to find chromosomal coordinates for the sequence, by searching its
   * direct and indirect cross-references for Ensembl. If the loci have already
   * been retrieved, just reads them out of the map of retrievedLoci; this is
   * the case of an alternative transcript for the same protein. Otherwise calls
   * a REST service to retrieve the loci, and if successful, adds them to the
   * sequence and to the retrievedLoci.
   * 
   * @param seq
   * @param retrievedLoci
   */
  static void findGeneLoci(SequenceI seq,
          Map<DBRefEntry, GeneLociI> retrievedLoci)
  {
    /*
     * don't replace any existing chromosomal coordinates
     */
    if (seq == null || seq.isProtein() || seq.getGeneLoci() != null
            || seq.getDBRefs() == null)
    {
      return;
    }

    Set<String> ensemblDivisions = new EnsemblInfo().getDivisions();

    /*
     * first look for direct dbrefs from sequence to Ensembl
     */
    String[] divisionsArray = ensemblDivisions
            .toArray(new String[ensemblDivisions.size()]);
    List<DBRefEntry> seqRefs = seq.getDBRefs();
    List<DBRefEntry> directEnsemblRefs = DBRefUtils.selectRefs(seqRefs,
            divisionsArray);
    if (directEnsemblRefs != null)
    {
      for (DBRefEntry ensemblRef : directEnsemblRefs)
      {
        if (fetchGeneLoci(seq, ensemblRef, retrievedLoci))
        {
          return;
        }
      }
    }

    /*
     * else look for indirect dbrefs from sequence to Ensembl
     */
    for (DBRefEntry dbref : seq.getDBRefs())
    {
      if (dbref.getMap() != null && dbref.getMap().getTo() != null)
      {
        List<DBRefEntry> dbrefs = dbref.getMap().getTo().getDBRefs();
        List<DBRefEntry> indirectEnsemblRefs = DBRefUtils.selectRefs(dbrefs,
                divisionsArray);
        if (indirectEnsemblRefs != null)
        {
          for (DBRefEntry ensemblRef : indirectEnsemblRefs)
          {
            if (fetchGeneLoci(seq, ensemblRef, retrievedLoci))
            {
              return;
            }
          }
        }
      }
    }
  }

  /**
   * Retrieves chromosomal coordinates for the Ensembl (or EnsemblGenomes)
   * identifier in dbref. If successful, and the sequence length matches gene
   * loci length, then add it to the sequence, and to the retrievedLoci map.
   * Answers true if successful, else false.
   * 
   * @param seq
   * @param dbref
   * @param retrievedLoci
   * @return
   */
  static boolean fetchGeneLoci(SequenceI seq, DBRefEntry dbref,
          Map<DBRefEntry, GeneLociI> retrievedLoci)
  {
    String accession = dbref.getAccessionId();
    String division = dbref.getSource();

    /*
     * hack: ignore cross-references to Ensembl protein ids
     * (or use map/translation perhaps?)
     * todo: is there an equivalent in EnsemblGenomes?
     */
    if (accession.startsWith("ENSP"))
    {
      return false;
    }
    EnsemblMap mapper = new EnsemblMap();

    /*
     * try CDS mapping first
     */
    GeneLociI geneLoci = mapper.getCdsMapping(division, accession, 1,
            seq.getLength());
    if (geneLoci != null)
    {
      MapList map = geneLoci.getMapping();
      int mappedFromLength = MappingUtils.getLength(map.getFromRanges());
      if (mappedFromLength == seq.getLength())
      {
        seq.setGeneLoci(geneLoci.getSpeciesId(), geneLoci.getAssemblyId(),
                geneLoci.getChromosomeId(), map);
        retrievedLoci.put(dbref, geneLoci);
        return true;
      }
    }

    /*
     * else try CDNA mapping
     */
    geneLoci = mapper.getCdnaMapping(division, accession, 1,
            seq.getLength());
    if (geneLoci != null)
    {
      MapList map = geneLoci.getMapping();
      int mappedFromLength = MappingUtils.getLength(map.getFromRanges());
      if (mappedFromLength == seq.getLength())
      {
        seq.setGeneLoci(geneLoci.getSpeciesId(), geneLoci.getAssemblyId(),
                geneLoci.getChromosomeId(), map);
        retrievedLoci.put(dbref, geneLoci);
        return true;
      }
    }

    return false;
  }

  /**
   * @param alignment
   * @param dataset
   * @param dna
   * @param xrefs
   * @param xrefsAlignment
   * @return
   */
  protected AlignmentI copyAlignmentForSplitFrame(AlignmentI alignment,
          AlignmentI dataset, boolean dna, AlignmentI xrefs,
          AlignmentI xrefsAlignment)
  {
    AlignmentI copyAlignment;
    boolean copyAlignmentIsAligned = false;
    if (dna)
    {
      copyAlignment = AlignmentUtils.makeCdsAlignment(sel, dataset,
              xrefsAlignment.getSequencesArray());
      if (copyAlignment.getHeight() == 0)
      {
        JvOptionPane.showMessageDialog(alignFrame,
                MessageManager.getString("label.cant_map_cds"),
                MessageManager.getString("label.operation_failed"),
                JvOptionPane.OK_OPTION);
        System.err.println("Failed to make CDS alignment");
        return null;
      }

      /*
       * pending getting Embl transcripts to 'align', 
       * we are only doing this for Ensembl
       */
      // TODO proper criteria for 'can align as cdna'
      if (DBRefSource.ENSEMBL.equalsIgnoreCase(source)
              || AlignmentUtils.looksLikeEnsembl(alignment))
      {
        copyAlignment.alignAs(alignment);
        copyAlignmentIsAligned = true;
      }
    }
    else
    {
      copyAlignment = AlignmentUtils.makeCopyAlignment(sel,
              xrefs.getSequencesArray(), dataset);
    }
    copyAlignment.setGapCharacter(alignFrame.viewport.getGapCharacter());

    StructureSelectionManager ssm = StructureSelectionManager
            .getStructureSelectionManager(Desktop.instance);

    /*
     * register any new mappings for sequence mouseover etc
     * (will not duplicate any previously registered mappings)
     */
    ssm.registerMappings(dataset.getCodonFrames());

    if (copyAlignment.getHeight() <= 0)
    {
      System.err.println("No Sequences generated for xRef type " + source);
      return null;
    }

    /*
     * align protein to dna
     */
    if (dna && copyAlignmentIsAligned)
    {
      xrefsAlignment.alignAs(copyAlignment);
    }
    else
    {
      /*
       * align cdna to protein - currently only if 
       * fetching and aligning Ensembl transcripts!
       */
      // TODO: generalise for other sources of locus/transcript/cds data
      if (dna && DBRefSource.ENSEMBL.equalsIgnoreCase(source))
      {
        copyAlignment.alignAs(xrefsAlignment);
      }
    }

    return copyAlignment;
  }

  /**
   * Makes an alignment containing the given sequences, and adds them to the
   * given dataset, which is also set as the dataset for the new alignment
   * 
   * TODO: refactor to DatasetI method
   * 
   * @param dataset
   * @param seqs
   * @return
   */
  protected AlignmentI makeCrossReferencesAlignment(AlignmentI dataset,
          AlignmentI seqs)
  {
    SequenceI[] sprods = new SequenceI[seqs.getHeight()];
    for (int s = 0; s < sprods.length; s++)
    {
      sprods[s] = (seqs.getSequenceAt(s)).deriveSequence();
      if (dataset.getSequences() == null || !dataset.getSequences()
              .contains(sprods[s].getDatasetSequence()))
      {
        dataset.addSequence(sprods[s].getDatasetSequence());
      }
      sprods[s].updatePDBIds();
    }
    Alignment al = new Alignment(sprods);
    al.setDataset(dataset);
    return al;
  }

  /**
   * Constructor
   * 
   * @param af
   * @param seqs
   * @param fromDna
   * @param dbSource
   */
  CrossRefAction(AlignFrame af, SequenceI[] seqs, boolean fromDna,
          String dbSource)
  {
    this.alignFrame = af;
    this.sel = seqs;
    this._odna = fromDna;
    this.source = dbSource;
  }

  public static CrossRefAction getHandlerFor(final SequenceI[] sel,
          final boolean fromDna, final String source,
          final AlignFrame alignFrame)
  {
    return new CrossRefAction(alignFrame, sel, fromDna, source);
  }

}
