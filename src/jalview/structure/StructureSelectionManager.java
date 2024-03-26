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
package jalview.structure;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import jalview.analysis.AlignSeq;
import jalview.api.StructureSelectionManagerProvider;
import jalview.bin.Console;
import jalview.commands.CommandI;
import jalview.commands.EditCommand;
import jalview.commands.OrderCommand;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.ContiguousI;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SearchResults;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceI;
import jalview.ext.jmol.JmolParser;
import jalview.gui.IProgressIndicator;
import jalview.io.AppletFormatAdapter;
import jalview.io.DataSourceType;
import jalview.io.StructureFile;
import jalview.structure.StructureImportSettings.TFType;
import jalview.util.MappingUtils;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.ws.sifts.SiftsClient;
import jalview.ws.sifts.SiftsException;
import jalview.ws.sifts.SiftsSettings;
import mc_view.Atom;
import mc_view.PDBChain;
import mc_view.PDBfile;

public class StructureSelectionManager
{
  public final static String NEWLINE = System.lineSeparator();

  static IdentityHashMap<StructureSelectionManagerProvider, StructureSelectionManager> instances;

  private List<StructureMapping> mappings = new ArrayList<>();

  private boolean processSecondaryStructure = false;

  private boolean secStructServices = false;

  private boolean addTempFacAnnot = false;

  /*
   * Set of any registered mappings between (dataset) sequences.
   */
  private List<AlignedCodonFrame> seqmappings = new ArrayList<>();

  private List<CommandListener> commandListeners = new ArrayList<>();

  private List<SelectionListener> sel_listeners = new ArrayList<>();

  /**
   * @return true if will try to use external services for processing secondary
   *         structure
   */
  public boolean isSecStructServices()
  {
    return secStructServices;
  }

  /**
   * control use of external services for processing secondary structure
   * 
   * @param secStructServices
   */
  public void setSecStructServices(boolean secStructServices)
  {
    this.secStructServices = secStructServices;
  }

  /**
   * flag controlling addition of any kind of structural annotation
   * 
   * @return true if temperature factor annotation will be added
   */
  public boolean isAddTempFacAnnot()
  {
    return addTempFacAnnot;
  }

  /**
   * set flag controlling addition of structural annotation
   * 
   * @param addTempFacAnnot
   */
  public void setAddTempFacAnnot(boolean addTempFacAnnot)
  {
    this.addTempFacAnnot = addTempFacAnnot;
  }

  /**
   * 
   * @return if true, the structure manager will attempt to add secondary
   *         structure lines for unannotated sequences
   */

  public boolean isProcessSecondaryStructure()
  {
    return processSecondaryStructure;
  }

  /**
   * Control whether structure manager will try to annotate mapped sequences
   * with secondary structure from PDB data.
   * 
   * @param enable
   */
  public void setProcessSecondaryStructure(boolean enable)
  {
    processSecondaryStructure = enable;
  }

  /**
   * debug function - write all mappings to stdout
   */
  public void reportMapping()
  {
    if (mappings.isEmpty())
    {
      System.err.println("reportMapping: No PDB/Sequence mappings.");
    }
    else
    {
      System.err.println(
              "reportMapping: There are " + mappings.size() + " mappings.");
      int i = 0;
      for (StructureMapping sm : mappings)
      {
        System.err.println("mapping " + i++ + " : " + sm.pdbfile);
      }
    }
  }

  /**
   * map between the PDB IDs (or structure identifiers) used by Jalview and the
   * absolute filenames for PDB data that corresponds to it
   */
  Map<String, String> pdbIdFileName = new HashMap<>();

  Map<String, String> pdbFileNameId = new HashMap<>();

  public void registerPDBFile(String idForFile, String absoluteFile)
  {
    pdbIdFileName.put(idForFile, absoluteFile);
    pdbFileNameId.put(absoluteFile, idForFile);
  }

  public String findIdForPDBFile(String idOrFile)
  {
    String id = pdbFileNameId.get(idOrFile);
    return id;
  }

  public String findFileForPDBId(String idOrFile)
  {
    String id = pdbIdFileName.get(idOrFile);
    return id;
  }

  public boolean isPDBFileRegistered(String idOrFile)
  {
    return pdbFileNameId.containsKey(idOrFile)
            || pdbIdFileName.containsKey(idOrFile);
  }

  private static StructureSelectionManager nullProvider = null;

  public static StructureSelectionManager getStructureSelectionManager(
          StructureSelectionManagerProvider context)
  {
    if (context == null)
    {
      if (nullProvider == null)
      {
        if (instances != null)
        {
          throw new Error(MessageManager.getString(
                  "error.implementation_error_structure_selection_manager_null"),
                  new NullPointerException(MessageManager
                          .getString("exception.ssm_context_is_null")));
        }
        else
        {
          nullProvider = new StructureSelectionManager();
        }
        return nullProvider;
      }
    }
    if (instances == null)
    {
      instances = new java.util.IdentityHashMap<>();
    }
    StructureSelectionManager instance = instances.get(context);
    if (instance == null)
    {
      if (nullProvider != null)
      {
        instance = nullProvider;
      }
      else
      {
        instance = new StructureSelectionManager();
      }
      instances.put(context, instance);
    }
    return instance;
  }

  /**
   * flag controlling whether SeqMappings are relayed from received sequence
   * mouse over events to other sequences
   */
  boolean relaySeqMappings = true;

  /**
   * Enable or disable relay of seqMapping events to other sequences. You might
   * want to do this if there are many sequence mappings and the host computer
   * is slow
   * 
   * @param relay
   */
  public void setRelaySeqMappings(boolean relay)
  {
    relaySeqMappings = relay;
  }

  /**
   * get the state of the relay seqMappings flag.
   * 
   * @return true if sequence mouse overs are being relayed to other mapped
   *         sequences
   */
  public boolean isRelaySeqMappingsEnabled()
  {
    return relaySeqMappings;
  }

  Vector listeners = new Vector();

  /**
   * register a listener for alignment sequence mouseover events
   * 
   * @param svl
   */
  public void addStructureViewerListener(Object svl)
  {
    if (!listeners.contains(svl))
    {
      listeners.addElement(svl);
    }
  }

  /**
   * Returns the filename the PDB id is already mapped to if known, or null if
   * it is not mapped
   * 
   * @param pdbid
   * @return
   */
  public String alreadyMappedToFile(String pdbid)
  {
    for (StructureMapping sm : mappings)
    {
      if (sm.getPdbId().equalsIgnoreCase(pdbid))
      {
        return sm.pdbfile;
      }
    }
    return null;
  }

  /**
   * Import structure data and register a structure mapping for broadcasting
   * colouring, mouseovers and selection events (convenience wrapper).
   * 
   * @param sequence
   *          - one or more sequences to be mapped to pdbFile
   * @param targetChains
   *          - optional chain specification for mapping each sequence to pdb
   *          (may be nill, individual elements may be nill)
   * @param pdbFile
   *          - structure data resource
   * @param protocol
   *          - how to resolve data from resource
   * @return null or the structure data parsed as a pdb file
   */
  synchronized public StructureFile setMapping(SequenceI[] sequence,
          String[] targetChains, String pdbFile, DataSourceType protocol,
          IProgressIndicator progress)
  {
    return computeMapping(true, sequence, targetChains, pdbFile, protocol,
            progress, null, null, true);
  }

  /**
   * Import a single structure file and register sequence structure mappings for
   * broadcasting colouring, mouseovers and selection events (convenience
   * wrapper).
   * 
   * @param forStructureView
   *          when true, record the mapping for use in mouseOvers
   * @param sequence
   *          - one or more sequences to be mapped to pdbFile
   * @param targetChains
   *          - optional chain specification for mapping each sequence to pdb
   *          (may be nill, individual elements may be nill)
   * @param pdbFile
   *          - structure data resource
   * @param protocol
   *          - how to resolve data from resource
   * @return null or the structure data parsed as a pdb file
   */
  synchronized public StructureFile setMapping(boolean forStructureView,
          SequenceI[] sequenceArray, String[] targetChainIds,
          String pdbFile, DataSourceType sourceType, TFType tft,
          String paeFilename)
  {
    return setMapping(forStructureView, sequenceArray, targetChainIds,
            pdbFile, sourceType, tft, paeFilename, true);
  }

  synchronized public StructureFile setMapping(boolean forStructureView,
          SequenceI[] sequenceArray, String[] targetChainIds,
          String pdbFile, DataSourceType sourceType, TFType tft,
          String paeFilename, boolean doXferSettings)
  {
    return computeMapping(forStructureView, sequenceArray, targetChainIds,
            pdbFile, sourceType, null, tft, paeFilename, doXferSettings);
  }

  /**
   * create sequence structure mappings between each sequence and the given
   * pdbFile (retrieved via the given protocol). Either constructs a mapping
   * using NW alignment or derives one from any available SIFTS mapping data.
   * 
   * @param forStructureView
   *          when true, record the mapping for use in mouseOvers
   * 
   * @param sequenceArray
   *          - one or more sequences to be mapped to pdbFile
   * @param targetChainIds
   *          - optional chain specification for mapping each sequence to pdb
   *          (may be nill, individual elements may be nill) - JBPNote: JAL-2693
   *          - this should be List<List<String>>, empty lists indicate no
   *          predefined mappings
   * @param pdbFile
   *          - structure data resource
   * @param sourceType
   *          - how to resolve data from resource
   * @param IProgressIndicator
   *          reference to UI component that maintains a progress bar for the
   *          mapping operation
   * @param tft - specify how to interpret the temperature factor column in the atom data
   * @param paeFilename - when not null, specifies a filename containing a matrix formatted in JSON using one of the known PAE formats
   * @param doXferSettings - when true, transfer annotation to mapped sequences in sequenceArray 
   * @return null or the structure data parsed as a pdb file
   */
  synchronized public StructureFile computeMapping(boolean forStructureView,
          SequenceI[] sequenceArray, String[] targetChainIds,
          String pdbFile, DataSourceType sourceType,
          IProgressIndicator progress, TFType tft, String paeFilename,
          boolean doXferSettings)
  {
    long progressSessionId = System.currentTimeMillis() * 3;

    /**
     * do we extract and transfer annotation from 3D data ?
     */
    // FIXME: possibly should just delete

    boolean parseSecStr = processSecondaryStructure
            && !isStructureFileProcessed(pdbFile, sequenceArray);

    StructureFile pdb = null;
    boolean isMapUsingSIFTs = SiftsSettings.isMapWithSifts();
    try
    {
      // FIXME if sourceType is not null, we've lost data here
      sourceType = AppletFormatAdapter.checkProtocol(pdbFile);
      pdb = new JmolParser(false, pdbFile, sourceType);
      if (paeFilename != null)
      {
        pdb.setPAEMatrix(paeFilename);
      }
      pdb.setTemperatureFactorType(tft);
      pdb.addSettings(parseSecStr && processSecondaryStructure,
              parseSecStr && addTempFacAnnot,
              parseSecStr && secStructServices);
      // save doXferSettings and reset after doParse()
      boolean temp = pdb.getDoXferSettings();
      pdb.setDoXferSettings(doXferSettings);
      pdb.doParse();
      pdb.setDoXferSettings(temp);
      if (pdb.getId() != null && pdb.getId().trim().length() > 0
              && DataSourceType.FILE == sourceType)
      {
        registerPDBFile(pdb.getId().trim(), pdbFile);
      }
      // if PDBId is unavailable then skip SIFTS mapping execution path
      // TODO: JAL-3868 need to know if structure is actually from
      // PDB (has valid PDB ID and has provenance suggesting it
      // actually came from PDB)
      boolean isProtein = false;
      for (SequenceI s : sequenceArray)
      {
        if (s.isProtein())
        {
          isProtein = true;
          break;
        }
      }
      isMapUsingSIFTs = isMapUsingSIFTs && pdb.isPPDBIdAvailable()
              && !pdb.getId().startsWith("AF-") && isProtein;

    } catch (Exception ex)
    {
      ex.printStackTrace();
      return null;
    }
    /*
     * sifts client - non null if SIFTS mappings are to be used 
     */
    SiftsClient siftsClient = null;
    try
    {
      if (isMapUsingSIFTs)
      {
        siftsClient = new SiftsClient(pdb);
      }
    } catch (SiftsException e)
    {
      isMapUsingSIFTs = false;
      Console.error("SIFTS mapping failed", e);
      Console.error("Falling back on Needleman & Wunsch alignment");
      siftsClient = null;
    }

    String targetChainId;
    for (int s = 0; s < sequenceArray.length; s++)
    {
      boolean infChain = true;
      final SequenceI seq = sequenceArray[s];
      SequenceI ds = seq;
      while (ds.getDatasetSequence() != null)
      {
        ds = ds.getDatasetSequence();
      }

      if (targetChainIds != null && targetChainIds[s] != null)
      {
        infChain = false;
        targetChainId = targetChainIds[s];
      }
      else if (seq.getName().indexOf("|") > -1)
      {
        targetChainId = seq.getName()
                .substring(seq.getName().lastIndexOf("|") + 1);
        if (targetChainId.length() > 1)
        {
          if (targetChainId.trim().length() == 0)
          {
            targetChainId = " ";
          }
          else
          {
            // not a valid chain identifier
            targetChainId = "";
          }
        }
      }
      else
      {
        targetChainId = "";
      }

      /*
       * Attempt pairwise alignment of the sequence with each chain in the PDB,
       * and remember the highest scoring chain
       */
      float max = -10;
      AlignSeq maxAlignseq = null;
      String maxChainId = " ";
      PDBChain maxChain = null;
      boolean first = true;
      for (PDBChain chain : pdb.getChains())
      {
        if (targetChainId.length() > 0 && !targetChainId.equals(chain.id)
                && !infChain)
        {
          continue; // don't try to map chains don't match.
        }
        // TODO: correctly determine sequence type for mixed na/peptide
        // structures
        final String type = chain.isNa ? AlignSeq.DNA : AlignSeq.PEP;
        AlignSeq as = AlignSeq.doGlobalNWAlignment(seq, chain.sequence,
                type);
        // equivalent to:
        // AlignSeq as = new AlignSeq(sequence[s], chain.sequence, type);
        // as.calcScoreMatrix();
        // as.traceAlignment();

        if (first || as.maxscore > max
                || (as.maxscore == max && chain.id.equals(targetChainId)))
        {
          first = false;
          maxChain = chain;
          max = as.maxscore;
          maxAlignseq = as;
          maxChainId = chain.id;
        }
      }
      if (maxChain == null)
      {
        continue;
      }

      if (sourceType == DataSourceType.PASTE)
      {
        pdbFile = "INLINE" + pdb.getId();
      }

      List<StructureMapping> seqToStrucMapping = new ArrayList<>();
      if (isMapUsingSIFTs && seq.isProtein())
      {
        if (progress != null)
        {
          progress.setProgressBar(
                  MessageManager
                          .getString("status.obtaining_mapping_with_sifts"),
                  progressSessionId);
        }
        jalview.datamodel.Mapping sqmpping = maxAlignseq
                .getMappingFromS1(false);
        if (targetChainId != null && !targetChainId.trim().isEmpty())
        {
          StructureMapping siftsMapping;
          try
          {
            siftsMapping = getStructureMapping(seq, pdbFile, targetChainId,
                    pdb, maxChain, sqmpping, maxAlignseq, siftsClient);
            seqToStrucMapping.add(siftsMapping);
            maxChain.makeExactMapping(siftsMapping, seq);
            maxChain.transferRESNUMFeatures(seq, "IEA: SIFTS",
                    pdb.getId().toLowerCase(Locale.ROOT));
            maxChain.transferResidueAnnotation(siftsMapping, null);
            ds.addPDBId(maxChain.sequence.getAllPDBEntries().get(0));

          } catch (SiftsException e)
          {
            // fall back to NW alignment
            Console.error(e.getMessage());
            StructureMapping nwMapping = getNWMappings(seq, pdbFile,
                    targetChainId, maxChain, pdb, maxAlignseq);
            seqToStrucMapping.add(nwMapping);
            maxChain.makeExactMapping(maxAlignseq, seq);
            maxChain.transferRESNUMFeatures(seq, "IEA:Jalview",
                    pdb.getId().toLowerCase(Locale.ROOT)); // FIXME: is
            // this
            // "IEA:Jalview" ?
            maxChain.transferResidueAnnotation(nwMapping, sqmpping);
            ds.addPDBId(maxChain.sequence.getAllPDBEntries().get(0));
          }
        }
        else
        {
          List<StructureMapping> foundSiftsMappings = new ArrayList<>();
          for (PDBChain chain : pdb.getChains())
          {
            StructureMapping siftsMapping = null;
            try
            {
              siftsMapping = getStructureMapping(seq, pdbFile, chain.id,
                      pdb, chain, sqmpping, maxAlignseq, siftsClient);
              foundSiftsMappings.add(siftsMapping);
              chain.makeExactMapping(siftsMapping, seq);
              chain.transferRESNUMFeatures(seq, "IEA: SIFTS",
                      pdb.getId().toLowerCase(Locale.ROOT));// FIXME: is this
              // "IEA:SIFTS" ?
              chain.transferResidueAnnotation(siftsMapping, null);
            } catch (SiftsException e)
            {
              System.err.println(e.getMessage());
            } catch (Exception e)
            {
              System.err.println(
                      "Unexpected exception during SIFTS mapping - falling back to NW for this sequence/structure pair");
              System.err.println(e.getMessage());
            }
          }
          if (!foundSiftsMappings.isEmpty())
          {
            seqToStrucMapping.addAll(foundSiftsMappings);
            ds.addPDBId(sqmpping.getTo().getAllPDBEntries().get(0));
          }
          else
          {
            StructureMapping nwMapping = getNWMappings(seq, pdbFile,
                    maxChainId, maxChain, pdb, maxAlignseq);
            seqToStrucMapping.add(nwMapping);
            maxChain.transferRESNUMFeatures(seq, null,
                    pdb.getId().toLowerCase(Locale.ROOT)); // FIXME: is this
            // "IEA:Jalview" ?
            maxChain.transferResidueAnnotation(nwMapping, sqmpping);
            ds.addPDBId(maxChain.sequence.getAllPDBEntries().get(0));
          }
        }
      }
      else
      {
        if (progress != null)
        {
          progress.setProgressBar(
                  MessageManager.getString(
                          "status.obtaining_mapping_with_nw_alignment"),
                  progressSessionId);
        }
        StructureMapping nwMapping = getNWMappings(seq, pdbFile, maxChainId,
                maxChain, pdb, maxAlignseq);
        seqToStrucMapping.add(nwMapping);
        ds.addPDBId(maxChain.sequence.getAllPDBEntries().get(0));
      }
      if (forStructureView)
      {
        for (StructureMapping sm : seqToStrucMapping)
        {
          addStructureMapping(sm); // not addAll!
        }
      }
      if (progress != null)
      {
        progress.setProgressBar(null, progressSessionId);
      }
    }
    return pdb;
  }

  /**
   * check if we need to extract secondary structure from given pdbFile and
   * transfer to sequences
   * 
   * @param pdbFile
   * @param sequenceArray
   * @return
   */
  private boolean isStructureFileProcessed(String pdbFile,
          SequenceI[] sequenceArray)
  {
    boolean processed = false;
    if (isPDBFileRegistered(pdbFile))
    {
      for (SequenceI sq : sequenceArray)
      {
        SequenceI ds = sq;
        while (ds.getDatasetSequence() != null)
        {
          ds = ds.getDatasetSequence();
        }
        ;
        if (ds.getAnnotation() != null)
        {
          for (AlignmentAnnotation ala : ds.getAnnotation())
          {
            // false if any annotation present from this structure
            // JBPNote this fails for jmol/chimera view because the *file* is
            // passed, not the structure data ID -
            if (PDBfile.isCalcIdForFile(ala, findIdForPDBFile(pdbFile)))
            {
              processed = true;
            }
          }
        }
      }
    }
    return processed;
  }

  public void addStructureMapping(StructureMapping sm)
  {
    if (!mappings.contains(sm))
    {
      mappings.add(sm);
    }
  }

  /**
   * retrieve a mapping for seq from SIFTs using associated DBRefEntry for
   * uniprot or PDB
   * 
   * @param seq
   * @param pdbFile
   * @param targetChainId
   * @param pdb
   * @param maxChain
   * @param sqmpping
   * @param maxAlignseq
   * @param siftsClient
   *          client for retrieval of SIFTS mappings for this structure
   * @return
   * @throws SiftsException
   */
  private StructureMapping getStructureMapping(SequenceI seq,
          String pdbFile, String targetChainId, StructureFile pdb,
          PDBChain maxChain, jalview.datamodel.Mapping sqmpping,
          AlignSeq maxAlignseq, SiftsClient siftsClient)
          throws SiftsException
  {
    StructureMapping curChainMapping = siftsClient
            .getSiftsStructureMapping(seq, pdbFile, targetChainId);
    try
    {
      PDBChain chain = pdb.findChain(targetChainId);
      if (chain != null)
      {
        chain.transferResidueAnnotation(curChainMapping, null);
      }
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    return curChainMapping;
  }

  private StructureMapping getNWMappings(SequenceI seq, String pdbFile,
          String maxChainId, PDBChain maxChain, StructureFile pdb,
          AlignSeq maxAlignseq)
  {
    final StringBuilder mappingDetails = new StringBuilder(128);
    mappingDetails.append(NEWLINE)
            .append("Sequence \u27f7 Structure mapping details");
    mappingDetails.append(NEWLINE);
    mappingDetails
            .append("Method: inferred with Needleman & Wunsch alignment");
    mappingDetails.append(NEWLINE).append("PDB Sequence is :")
            .append(NEWLINE).append("Sequence = ")
            .append(maxChain.sequence.getSequenceAsString());
    mappingDetails.append(NEWLINE).append("No of residues = ")
            .append(maxChain.residues.size()).append(NEWLINE)
            .append(NEWLINE);
    PrintStream ps = new PrintStream(System.out)
    {
      @Override
      public void print(String x)
      {
        mappingDetails.append(x);
      }

      @Override
      public void println()
      {
        mappingDetails.append(NEWLINE);
      }
    };

    maxAlignseq.printAlignment(ps);

    mappingDetails.append(NEWLINE).append("PDB start/end ");
    mappingDetails.append(String.valueOf(maxAlignseq.seq2start))
            .append(" ");
    mappingDetails.append(String.valueOf(maxAlignseq.seq2end));
    mappingDetails.append(NEWLINE).append("SEQ start/end ");
    mappingDetails
            .append(String
                    .valueOf(maxAlignseq.seq1start + (seq.getStart() - 1)))
            .append(" ");
    mappingDetails.append(
            String.valueOf(maxAlignseq.seq1end + (seq.getStart() - 1)));
    mappingDetails.append(NEWLINE);
    maxChain.makeExactMapping(maxAlignseq, seq);
    jalview.datamodel.Mapping sqmpping = maxAlignseq
            .getMappingFromS1(false);
    maxChain.transferRESNUMFeatures(seq, null,
            pdb.getId().toLowerCase(Locale.ROOT));

    HashMap<Integer, int[]> mapping = new HashMap<>();
    int resNum = -10000;
    int index = 0;
    char insCode = ' ';

    do
    {
      Atom tmp = maxChain.atoms.elementAt(index);
      if ((resNum != tmp.resNumber || insCode != tmp.insCode)
              && tmp.alignmentMapping != -1)
      {
        resNum = tmp.resNumber;
        insCode = tmp.insCode;
        if (tmp.alignmentMapping >= -1)
        {
          mapping.put(tmp.alignmentMapping + 1,
                  new int[]
                  { tmp.resNumber, tmp.atomIndex });
        }
      }

      index++;
    } while (index < maxChain.atoms.size());

    StructureMapping nwMapping = new StructureMapping(seq, pdbFile,
            pdb.getId(), maxChainId, mapping, mappingDetails.toString());
    maxChain.transferResidueAnnotation(nwMapping, sqmpping);
    return nwMapping;
  }

  public void removeStructureViewerListener(Object svl, String[] pdbfiles)
  {
    listeners.removeElement(svl);
    if (svl instanceof SequenceListener)
    {
      for (int i = 0; i < listeners.size(); i++)
      {
        if (listeners.elementAt(i) instanceof StructureListener)
        {
          ((StructureListener) listeners.elementAt(i))
                  .releaseReferences(svl);
        }
      }
    }

    if (pdbfiles == null)
    {
      return;
    }

    /*
     * Remove mappings to the closed listener's PDB files, but first check if
     * another listener is still interested
     */
    List<String> pdbs = new ArrayList<>(Arrays.asList(pdbfiles));

    StructureListener sl;
    for (int i = 0; i < listeners.size(); i++)
    {
      if (listeners.elementAt(i) instanceof StructureListener)
      {
        sl = (StructureListener) listeners.elementAt(i);
        for (String pdbfile : sl.getStructureFiles())
        {
          pdbs.remove(pdbfile);
        }
      }
    }

    /*
     * Rebuild the mappings set, retaining only those which are for 'other' PDB
     * files
     */
    if (pdbs.size() > 0)
    {
      List<StructureMapping> tmp = new ArrayList<>();
      for (StructureMapping sm : mappings)
      {
        if (!pdbs.contains(sm.pdbfile))
        {
          tmp.add(sm);
        }
      }

      mappings = tmp;
    }
  }

  /**
   * hack to highlight a range of positions at once on any structure views
   * 
   * @param sequenceRef
   * @param is
   *          - series of int start-end ranges as positions on sequenceRef
   * @param i
   * @param object
   */
  public void highlightPositionsOn(SequenceI sequenceRef, int[][] is,
          Object source)
  {
    boolean hasSequenceListeners = handlingVamsasMo
            || !seqmappings.isEmpty();
    SearchResultsI results = null;
    ArrayList<Integer> listOfPositions = new ArrayList<Integer>();
    for (int[] s_e : is)
    {
      for (int p = s_e[0]; p <= s_e[1]; listOfPositions.add(p++))
        ;
    }
    int seqpos[] = new int[listOfPositions.size()];
    int i = 0;
    for (Integer p : listOfPositions)
    {
      seqpos[i++] = p;
    }

    for (i = 0; i < listeners.size(); i++)
    {
      Object listener = listeners.elementAt(i);
      if (listener == source)
      {
        // TODO listener (e.g. SeqPanel) is never == source (AlignViewport)
        // Temporary fudge with SequenceListener.getVamsasSource()
        continue;
      }
      if (listener instanceof StructureListener)
      {
        highlightStructure((StructureListener) listener, sequenceRef,
                seqpos);
      }

    }
  }

  /**
   * Propagate mouseover of a single position in a structure
   * 
   * @param pdbResNum
   * @param chain
   * @param pdbfile
   * @return
   */
  public String mouseOverStructure(int pdbResNum, String chain,
          String pdbfile)
  {
    AtomSpec atomSpec = new AtomSpec(pdbfile, chain, pdbResNum, 0);
    List<AtomSpec> atoms = Collections.singletonList(atomSpec);
    return mouseOverStructure(atoms);
  }

  /**
   * Propagate mouseover or selection of multiple positions in a structure
   * 
   * @param atoms
   */
  public String mouseOverStructure(List<AtomSpec> atoms)
  {
    if (listeners == null)
    {
      // old or prematurely sent event
      return null;
    }
    boolean hasSequenceListener = false;
    for (int i = 0; i < listeners.size(); i++)
    {
      if (listeners.elementAt(i) instanceof SequenceListener)
      {
        hasSequenceListener = true;
      }
    }
    if (!hasSequenceListener)
    {
      return null;
    }

    SearchResultsI results = findAlignmentPositionsForStructurePositions(
            atoms);
    String result = null;
    for (Object li : listeners)
    {
      if (li instanceof SequenceListener)
      {
    System.out.println("des war hier oder? " + results.getCount() + " results");
        String s = ((SequenceListener) li).highlightSequence(results);
    System.out.println(s);
        if (s != null)
        {
          result = s;
        }
      }
    }
    return result;
  }

  /**
   * Constructs a SearchResults object holding regions (if any) in the Jalview
   * alignment which have a mapping to the structure viewer positions in the
   * supplied list
   * 
   * @param atoms
   * @return
   */
  public SearchResultsI findAlignmentPositionsForStructurePositions(
          List<AtomSpec> atoms)
  {
    SearchResultsI results = new SearchResults();
    for (AtomSpec atom : atoms)
    {
      SequenceI lastseq = null;
      int lastipos = -1;
      for (StructureMapping sm : mappings)
      {
        if (sm.pdbfile.equals(atom.getPdbFile())
                && sm.pdbchain.equals(atom.getChain()))
        {
          int indexpos = sm.getSeqPos(atom.getPdbResNum());
          if (lastipos != indexpos || lastseq != sm.sequence)
          {
            results.addResult(sm.sequence, indexpos, indexpos);
            lastipos = indexpos;
            lastseq = sm.sequence;
            // construct highlighted sequence list
            for (AlignedCodonFrame acf : seqmappings)
            {
              acf.markMappedRegion(sm.sequence, indexpos, results);
            }
          }
        }
      }
    }
    return results;
  }

  /**
   * highlight regions associated with a position (indexpos) in seq
   * 
   * @param seq
   *          the sequence that the mouse over occurred on
   * @param indexpos
   *          the absolute position being mouseovered in seq (0 to seq.length())
   * @param seqPos
   *          the sequence position (if -1, seq.findPosition is called to
   *          resolve the residue number)
   */
  public void mouseOverSequence(SequenceI seq, int indexpos, int seqPos,
          VamsasSource source)
  {
    boolean hasSequenceListeners = handlingVamsasMo
            || !seqmappings.isEmpty();
    SearchResultsI results = null;
    if (seqPos == -1)
    {
      seqPos = seq.findPosition(indexpos);
    }
    for (int i = 0; i < listeners.size(); i++)
    {
      Object listener = listeners.elementAt(i);
      if (listener == source)
      {
        // TODO listener (e.g. SeqPanel) is never == source (AlignViewport)
        // Temporary fudge with SequenceListener.getVamsasSource()
        continue;
      }
      if (listener instanceof StructureListener)
      {
        highlightStructure((StructureListener) listener, seq, seqPos);
      }
      else
      {
        if (listener instanceof SequenceListener)
        {
          final SequenceListener seqListener = (SequenceListener) listener;
          if (hasSequenceListeners
                  && seqListener.getVamsasSource() != source)
          {
            if (relaySeqMappings)
            {
              if (results == null)
              {
                results = MappingUtils.buildSearchResults(seq, seqPos,
                        seqmappings);
              }
              if (handlingVamsasMo)
              {
                results.addResult(seq, seqPos, seqPos);

              }
              if (!results.isEmpty())
              {
                seqListener.highlightSequence(results);
              }
            }
          }
        }
        else if (listener instanceof VamsasListener && !handlingVamsasMo)
        {
          ((VamsasListener) listener).mouseOverSequence(seq, indexpos,
                  source);
        }
        else if (listener instanceof SecondaryStructureListener)
        {
          ((SecondaryStructureListener) listener).mouseOverSequence(seq,
                  indexpos, seqPos);
        }
      }
    }
  }

  /**
   * Send suitable messages to a StructureListener to highlight atoms
   * corresponding to the given sequence position(s)
   * 
   * @param sl
   * @param seq
   * @param positions
   */
  public void highlightStructure(StructureListener sl, SequenceI seq,
          int... positions)
  {
    if (!sl.isListeningFor(seq))
    {
      return;
    }
    int atomNo;
    List<AtomSpec> atoms = new ArrayList<>();
    for (StructureMapping sm : mappings)
    {
      if (sm.sequence == seq || sm.sequence == seq.getDatasetSequence()
              || (sm.sequence.getDatasetSequence() != null && sm.sequence
                      .getDatasetSequence() == seq.getDatasetSequence()))
      {
        for (int index : positions)
        {
          atomNo = sm.getAtomNum(index);

          if (atomNo > 0)
          {
            atoms.add(new AtomSpec(sm.pdbfile, sm.pdbchain,
                    sm.getPDBResNum(index), atomNo));
          }
        }
      }
    }
    sl.highlightAtoms(atoms);
  }

  public void highlightStructureRegionsFor(StructureListener sl,
          SequenceI[] seqs, int... columns)
  {
    List<SequenceI> to_highlight = new ArrayList<SequenceI>();
    for (SequenceI seq : seqs)
    {
      if (sl.isListeningFor(seq))
      {
        to_highlight.add(seq);
      }
    }
    if (to_highlight.size() == 0)
    {
      return;
    }
    List<AtomSpec> atoms = new ArrayList<>();
    for (SequenceI seq : to_highlight)
    {
      int atomNo;
      for (StructureMapping sm : mappings)
      {
        if (sm.sequence == seq || sm.sequence == seq.getDatasetSequence()
                || (sm.sequence.getDatasetSequence() != null && sm.sequence
                        .getDatasetSequence() == seq.getDatasetSequence()))
        {

          for (int i = 0; i < columns.length; i += 2)
          {
            ContiguousI positions = seq.findPositions(columns[i] + 1,
                    columns[i + 1] + 1);
            if (positions == null)
            {
              continue;
            }
            for (int index = positions.getBegin(); index <= positions
                    .getEnd(); index++)
            {

              atomNo = sm.getAtomNum(index);

              if (atomNo > 0)
              {
                atoms.add(new AtomSpec(sm.pdbfile, sm.pdbchain,
                        sm.getPDBResNum(index), atomNo));
              }
            }
          }
        }
      }
      if (atoms.size() > 0)
      {
        sl.highlightAtoms(atoms);
      }
    }
  }

  /**
   * true if a mouse over event from an external (ie Vamsas) source is being
   * handled
   */
  boolean handlingVamsasMo = false;

  long lastmsg = 0;

  /**
   * as mouseOverSequence but only route event to SequenceListeners
   * 
   * @param sequenceI
   * @param position
   *          in an alignment sequence
   */
  public void mouseOverVamsasSequence(SequenceI sequenceI, int position,
          VamsasSource source)
  {
    handlingVamsasMo = true;
    long msg = sequenceI.hashCode() * (1 + position);
    if (lastmsg != msg)
    {
      lastmsg = msg;
      mouseOverSequence(sequenceI, position, -1, source);
    }
    handlingVamsasMo = false;
  }

  public Annotation[] colourSequenceFromStructure(SequenceI seq,
          String pdbid)
  {
    return null;
    // THIS WILL NOT BE AVAILABLE IN JALVIEW 2.3,
    // UNTIL THE COLOUR BY ANNOTATION IS REWORKED
    /*
     * Annotation [] annotations = new Annotation[seq.getLength()];
     * 
     * StructureListener sl; int atomNo = 0; for (int i = 0; i <
     * listeners.size(); i++) { if (listeners.elementAt(i) instanceof
     * StructureListener) { sl = (StructureListener) listeners.elementAt(i);
     * 
     * for (int j = 0; j < mappings.length; j++) {
     * 
     * if (mappings[j].sequence == seq && mappings[j].getPdbId().equals(pdbid)
     * && mappings[j].pdbfile.equals(sl.getPdbFile())) {
     * System.out.println(pdbid+" "+mappings[j].getPdbId() +"
     * "+mappings[j].pdbfile);
     * 
     * java.awt.Color col; for(int index=0; index<seq.getLength(); index++) {
     * if(jalview.util.Comparison.isGap(seq.getCharAt(index))) continue;
     * 
     * atomNo = mappings[j].getAtomNum(seq.findPosition(index)); col =
     * java.awt.Color.white; if (atomNo > 0) { col = sl.getColour(atomNo,
     * mappings[j].getPDBResNum(index), mappings[j].pdbchain,
     * mappings[j].pdbfile); }
     * 
     * annotations[index] = new Annotation("X",null,' ',0,col); } return
     * annotations; } } } }
     * 
     * return annotations;
     */
  }

  public void structureSelectionChanged()
  {
  }

  public void sequenceSelectionChanged()
  {
  }

  public void sequenceColoursChanged(Object source)
  {
    StructureListener sl;
    for (int i = 0; i < listeners.size(); i++)
    {
      if (listeners.elementAt(i) instanceof StructureListener)
      {
        sl = (StructureListener) listeners.elementAt(i);
        sl.updateColours(source);
      }
    }
  }

  public StructureMapping[] getMapping(String pdbfile)
  {
    List<StructureMapping> tmp = new ArrayList<>();
    for (StructureMapping sm : mappings)
    {
      if (sm.pdbfile.equals(pdbfile))
      {
        tmp.add(sm);
      }
    }
    return tmp.toArray(new StructureMapping[tmp.size()]);
  }

  /**
   * Returns a readable description of all mappings for the given pdbfile to any
   * of the given sequences
   * 
   * @param pdbfile
   * @param seqs
   * @return
   */
  public String printMappings(String pdbfile, List<SequenceI> seqs)
  {
    if (pdbfile == null || seqs == null || seqs.isEmpty())
    {
      return "";
    }

    StringBuilder sb = new StringBuilder(64);
    for (StructureMapping sm : mappings)
    {
      if (Platform.pathEquals(sm.pdbfile, pdbfile)
              && seqs.contains(sm.sequence))
      {
        sb.append(sm.mappingDetails);
        sb.append(NEWLINE);
        // separator makes it easier to read multiple mappings
        sb.append("=====================");
        sb.append(NEWLINE);
      }
    }
    sb.append(NEWLINE);

    return sb.toString();
  }

  /**
   * Remove the given mapping
   * 
   * @param acf
   */
  public void deregisterMapping(AlignedCodonFrame acf)
  {
    if (acf != null)
    {
      boolean removed = seqmappings.remove(acf);
      if (removed && seqmappings.isEmpty())
      { // debug
        System.out.println("All mappings removed");
      }
    }
  }

  /**
   * Add each of the given codonFrames to the stored set, if not aready present.
   * 
   * @param mappings
   */
  public void registerMappings(List<AlignedCodonFrame> mappings)
  {
    if (mappings != null)
    {
      for (AlignedCodonFrame acf : mappings)
      {
        registerMapping(acf);
      }
    }
  }

  /**
   * Add the given mapping to the stored set, unless already stored.
   */
  public void registerMapping(AlignedCodonFrame acf)
  {
    if (acf != null)
    {
      if (!seqmappings.contains(acf))
      {
        seqmappings.add(acf);
      }
    }
  }

  /**
   * Resets this object to its initial state by removing all registered
   * listeners, codon mappings, PDB file mappings
   */
  public void resetAll()
  {
    if (mappings != null)
    {
      mappings.clear();
    }
    if (seqmappings != null)
    {
      seqmappings.clear();
    }
    if (sel_listeners != null)
    {
      sel_listeners.clear();
    }
    if (listeners != null)
    {
      listeners.clear();
    }
    if (commandListeners != null)
    {
      commandListeners.clear();
    }
    if (view_listeners != null)
    {
      view_listeners.clear();
    }
    if (pdbFileNameId != null)
    {
      pdbFileNameId.clear();
    }
    if (pdbIdFileName != null)
    {
      pdbIdFileName.clear();
    }
  }

  public void addSelectionListener(SelectionListener selecter)
  {
    if (!sel_listeners.contains(selecter))
    {
      sel_listeners.add(selecter);
    }
  }

  public void removeSelectionListener(SelectionListener toremove)
  {
    if (sel_listeners.contains(toremove))
    {
      sel_listeners.remove(toremove);
    }
  }

  public synchronized void sendSelection(
          jalview.datamodel.SequenceGroup selection,
          jalview.datamodel.ColumnSelection colsel, HiddenColumns hidden,
          SelectionSource source)
  {
    for (SelectionListener slis : sel_listeners)
    {
      if (slis != source)
      {
        slis.selection(selection, colsel, hidden, source);
      }
    }
  }

  Vector<AlignmentViewPanelListener> view_listeners = new Vector<>();

  public synchronized void sendViewPosition(
          jalview.api.AlignmentViewPanel source, int startRes, int endRes,
          int startSeq, int endSeq)
  {

    if (view_listeners != null && view_listeners.size() > 0)
    {
      Enumeration<AlignmentViewPanelListener> listeners = view_listeners
              .elements();
      while (listeners.hasMoreElements())
      {
        AlignmentViewPanelListener slis = listeners.nextElement();
        if (slis != source)
        {
          slis.viewPosition(startRes, endRes, startSeq, endSeq, source);
        }
        ;
      }
    }
  }

  /**
   * release all references associated with this manager provider
   * 
   * @param jalviewLite
   */
  public static void release(StructureSelectionManagerProvider jalviewLite)
  {
    // synchronized (instances)
    {
      if (instances == null)
      {
        return;
      }
      StructureSelectionManager mnger = (instances.get(jalviewLite));
      if (mnger != null)
      {
        instances.remove(jalviewLite);
        try
        {
          /* bsoares 2019-03-20 finalize deprecated, no apparent external
           * resources to close
           */
          // mnger.finalize();
        } catch (Throwable x)
        {
        }
      }
    }
  }

  public void registerPDBEntry(PDBEntry pdbentry)
  {
    if (pdbentry.getFile() != null
            && pdbentry.getFile().trim().length() > 0)
    {
      registerPDBFile(pdbentry.getId(), pdbentry.getFile());
    }
  }

  public void addCommandListener(CommandListener cl)
  {
    if (!commandListeners.contains(cl))
    {
      commandListeners.add(cl);
    }
  }

  public boolean hasCommandListener(CommandListener cl)
  {
    return this.commandListeners.contains(cl);
  }

  public boolean removeCommandListener(CommandListener l)
  {
    return commandListeners.remove(l);
  }

  /**
   * Forward a command to any command listeners (except for the command's
   * source).
   * 
   * @param command
   *          the command to be broadcast (in its form after being performed)
   * @param undo
   *          if true, the command was being 'undone'
   * @param source
   */
  public void commandPerformed(CommandI command, boolean undo,
          VamsasSource source)
  {
    for (CommandListener listener : commandListeners)
    {
      listener.mirrorCommand(command, undo, this, source);
    }
  }

  /**
   * Returns a new CommandI representing the given command as mapped to the
   * given sequences. If no mapping could be made, or the command is not of a
   * mappable kind, returns null.
   * 
   * @param command
   * @param undo
   * @param mapTo
   * @param gapChar
   * @return
   */
  public CommandI mapCommand(CommandI command, boolean undo,
          final AlignmentI mapTo, char gapChar)
  {
    if (command instanceof EditCommand)
    {
      return MappingUtils.mapEditCommand((EditCommand) command, undo, mapTo,
              gapChar, seqmappings);
    }
    else if (command instanceof OrderCommand)
    {
      return MappingUtils.mapOrderCommand((OrderCommand) command, undo,
              mapTo, seqmappings);
    }
    return null;
  }

  public List<AlignedCodonFrame> getSequenceMappings()
  {
    return seqmappings;
  }

  /**
   * quick and dirty route to just highlight all structure positions for a range
   * of columns
   * 
   * @param sequencesArray
   * @param is
   *          start-end columns on sequencesArray
   * @param source
   *          origin parent AlignmentPanel
   */
  public void highlightPositionsOnMany(SequenceI[] sequencesArray, int[] is,
          Object source)
  {
    for (int i = 0; i < listeners.size(); i++)
    {
      Object listener = listeners.elementAt(i);
      if (listener == source)
      {
        // TODO listener (e.g. SeqPanel) is never == source (AlignViewport)
        // Temporary fudge with SequenceListener.getVamsasSource()
        continue;
      }
      if (listener instanceof StructureListener)
      {
        highlightStructureRegionsFor((StructureListener) listener,
                sequencesArray, is);
      }
    }
  }

  public Map<String, String> getPdbFileNameIdMap()
  {
    return pdbFileNameId;
  }

  public Map<String, String> getPdbIdFileNameMap()
  {
    return pdbIdFileName;
  }

}
