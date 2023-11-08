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
package jalview.structures.models;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.SwingUtilities;

import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.api.FeatureRenderer;
import jalview.api.SequenceRenderer;
import jalview.api.StructureSelectionManagerProvider;
import jalview.api.structures.JalviewStructureDisplayI;
import jalview.bin.Console;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.MappedFeatures;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.ext.rbvi.chimera.JalviewChimeraBinding;
import jalview.gui.AlignmentPanel;
import jalview.gui.Desktop;
import jalview.gui.StructureViewer.ViewerType;
import jalview.io.DataSourceType;
import jalview.io.StructureFile;
import jalview.renderer.seqfeatures.FeatureColourFinder;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ResidueProperties;
import jalview.structure.AtomSpec;
import jalview.structure.AtomSpecModel;
import jalview.structure.StructureCommandI;
import jalview.structure.StructureCommandsI;
import jalview.structure.StructureCommandsI.AtomSpecType;
import jalview.structure.StructureListener;
import jalview.structure.StructureMapping;
import jalview.structure.StructureSelectionManager;
import jalview.util.Comparison;
import jalview.util.MessageManager;

/**
 * 
 * A base class to hold common function for 3D structure model binding. Initial
 * version created by refactoring JMol and Chimera binding models, but other
 * structure viewers could in principle be accommodated in future.
 * 
 * @author gmcarstairs
 *
 */
public abstract class AAStructureBindingModel
        extends SequenceStructureBindingModel
        implements StructureListener, StructureSelectionManagerProvider
{
  /**
   * Data bean class to simplify parameterisation in superposeStructures
   */
  public static class SuperposeData
  {
    public String filename;

    public String pdbId;

    public String chain = "";

    /**
     * is the mapped sequence not protein ?
     */
    public boolean isRna;

    /*
     * The pdb residue number (if any) mapped to columns of the alignment
     */
    public int[] pdbResNo; // or use SparseIntArray?

    public String modelId;

    /**
     * Constructor
     * 
     * @param width
     *          width of alignment (number of columns that may potentially
     *          participate in superposition)
     * @param model
     *          structure viewer model number
     */
    public SuperposeData(int width, String model)
    {
      pdbResNo = new int[width];
      modelId = model;
    }
  }

  private static final int MIN_POS_TO_SUPERPOSE = 4;

  private static final String COLOURING_STRUCTURES = MessageManager
          .getString("status.colouring_structures");

  /*
   * the Jalview panel through which the user interacts
   * with the structure viewer
   */
  private JalviewStructureDisplayI viewer;

  /*
   * helper that generates command syntax
   */
  private StructureCommandsI commandGenerator;

  private StructureSelectionManager ssm;

  /*
   * modelled chains, formatted as "pdbid:chainCode"
   */
  private List<String> chainNames;

  /*
   * lookup of pdb file name by key "pdbid:chainCode"
   */
  private Map<String, String> chainFile;

  /*
   * distinct PDB entries (pdb files) associated
   * with sequences
   */
  private PDBEntry[] pdbEntry;

  /*
   * sequences mapped to each pdbentry
   */
  private SequenceI[][] sequence;

  /*
   * array of target chains for sequences - tied to pdbentry and sequence[]
   */
  private String[][] chains;

  /*
   * datasource protocol for access to PDBEntrylatest
   */
  DataSourceType protocol = null;

  protected boolean colourBySequence = true;

  /**
   * true if all sequences appear to be nucleotide
   */
  private boolean nucleotide;

  private boolean finishedInit = false;

  /**
   * current set of model filenames loaded in the viewer
   */
  protected String[] modelFileNames = null;

  public String fileLoadingError;

  protected Thread externalViewerMonitor;

  /**
   * Constructor
   * 
   * @param ssm
   * @param seqs
   */
  public AAStructureBindingModel(StructureSelectionManager ssm,
          SequenceI[][] seqs)
  {
    this.ssm = ssm;
    this.sequence = seqs;
    chainNames = new ArrayList<>();
    chainFile = new HashMap<>();
  }

  /**
   * Constructor
   * 
   * @param ssm
   * @param pdbentry
   * @param sequenceIs
   * @param protocol
   */
  public AAStructureBindingModel(StructureSelectionManager ssm,
          PDBEntry[] pdbentry, SequenceI[][] sequenceIs,
          DataSourceType protocol)
  {
    this(ssm, sequenceIs);
    this.nucleotide = Comparison.isNucleotide(sequenceIs);
    this.pdbEntry = pdbentry;
    this.protocol = protocol;
    resolveChains();
  }

  private boolean resolveChains()
  {
    /**
     * final count of chain mappings discovered
     */
    int chainmaps = 0;
    // JBPNote: JAL-2693 - this should be a list of chain mappings per
    // [pdbentry][sequence]
    String[][] newchains = new String[pdbEntry.length][];
    int pe = 0;
    for (PDBEntry pdb : pdbEntry)
    {
      SequenceI[] seqsForPdb = sequence[pe];
      if (seqsForPdb != null)
      {
        newchains[pe] = new String[seqsForPdb.length];
        int se = 0;
        for (SequenceI asq : seqsForPdb)
        {
          String chain = (chains != null && chains[pe] != null)
                  ? chains[pe][se]
                  : null;
          SequenceI sq = (asq.getDatasetSequence() == null) ? asq
                  : asq.getDatasetSequence();
          if (sq.getAllPDBEntries() != null)
          {
            for (PDBEntry pdbentry : sq.getAllPDBEntries())
            {
              if (pdb.getFile() != null && pdbentry.getFile() != null
                      && pdb.getFile().equals(pdbentry.getFile()))
              {
                String chaincode = pdbentry.getChainCode();
                if (chaincode != null && chaincode.length() > 0)
                {
                  chain = chaincode;
                  chainmaps++;
                  break;
                }
              }
            }
          }
          newchains[pe][se] = chain;
          se++;
        }
        pe++;
      }
    }

    chains = newchains;
    return chainmaps > 0;
  }

  public StructureSelectionManager getSsm()
  {
    return ssm;
  }

  /**
   * Returns the i'th PDBEntry (or null)
   * 
   * @param i
   * @return
   */
  public PDBEntry getPdbEntry(int i)
  {
    return (pdbEntry != null && pdbEntry.length > i) ? pdbEntry[i] : null;
  }

  /**
   * Answers true if this binding includes the given PDB id, else false
   * 
   * @param pdbId
   * @return
   */
  public boolean hasPdbId(String pdbId)
  {
    if (pdbEntry != null)
    {
      for (PDBEntry pdb : pdbEntry)
      {
        if (pdb.getId().equals(pdbId))
        {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns the number of modelled PDB file entries.
   * 
   * @return
   */
  public int getPdbCount()
  {
    return pdbEntry == null ? 0 : pdbEntry.length;
  }

  public SequenceI[][] getSequence()
  {
    return sequence;
  }

  public String[][] getChains()
  {
    return chains;
  }

  public DataSourceType getProtocol()
  {
    return protocol;
  }

  // TODO may remove this if calling methods can be pulled up here
  protected void setPdbentry(PDBEntry[] pdbentry)
  {
    this.pdbEntry = pdbentry;
  }

  protected void setSequence(SequenceI[][] sequence)
  {
    this.sequence = sequence;
  }

  protected void setChains(String[][] chains)
  {
    this.chains = chains;
  }

  /**
   * Construct a title string for the viewer window based on the data Jalview
   * knows about
   * 
   * @param viewerName
   *          TODO
   * @param verbose
   * 
   * @return
   */
  public String getViewerTitle(String viewerName, boolean verbose)
  {
    if (getSequence() == null || getSequence().length < 1
            || getPdbCount() < 1 || getSequence()[0].length < 1)
    {
      return ("Jalview " + viewerName + " Window");
    }
    // TODO: give a more informative title when multiple structures are
    // displayed.
    StringBuilder title = new StringBuilder(64);
    final PDBEntry pdbe = getPdbEntry(0);
    title.append(viewerName + " view for " + getSequence()[0][0].getName()
            + ":" + pdbe.getId());

    if (verbose)
    {
      String method = (String) pdbe.getProperty("method");
      if (method != null)
      {
        title.append(" Method: ").append(method);
      }
      String chain = (String) pdbe.getProperty("chains");
      if (chain != null)
      {
        title.append(" Chain:").append(chain);
      }
    }
    return title.toString();
  }

  /**
   * Called by after closeViewer is called, to release any resources and
   * references so they can be garbage collected. Override if needed.
   */
  protected void releaseUIResources()
  {
  }

  @Override
  public void releaseReferences(Object svl)
  {
  }

  public boolean isColourBySequence()
  {
    return colourBySequence;
  }

  /**
   * Called when the binding thinks the UI needs to be refreshed after a
   * structure viewer state change. This could be because structures were
   * loaded, or because an error has occurred. Default does nothing, override as
   * required.
   */
  public void refreshGUI()
  {
  }

  /**
   * Instruct the Jalview binding to update the pdbentries vector if necessary
   * prior to matching the jmol view's contents to the list of structure files
   * Jalview knows about. By default does nothing, override as required.
   */
  public void refreshPdbEntries()
  {
  }

  public void setColourBySequence(boolean colourBySequence)
  {
    this.colourBySequence = colourBySequence;
  }

  protected void addSequenceAndChain(int pe, SequenceI[] seq,
          String[] tchain)
  {
    if (pe < 0 || pe >= getPdbCount())
    {
      throw new Error(MessageManager.formatMessage(
              "error.implementation_error_no_pdbentry_from_index",
              new Object[]
              { Integer.valueOf(pe).toString() }));
    }
    final String nullChain = "TheNullChain";
    List<SequenceI> s = new ArrayList<>();
    List<String> c = new ArrayList<>();
    if (getChains() == null)
    {
      setChains(new String[getPdbCount()][]);
    }
    if (getSequence()[pe] != null)
    {
      for (int i = 0; i < getSequence()[pe].length; i++)
      {
        s.add(getSequence()[pe][i]);
        if (getChains()[pe] != null)
        {
          if (i < getChains()[pe].length)
          {
            c.add(getChains()[pe][i]);
          }
          else
          {
            c.add(nullChain);
          }
        }
        else
        {
          if (tchain != null && tchain.length > 0)
          {
            c.add(nullChain);
          }
        }
      }
    }
    for (int i = 0; i < seq.length; i++)
    {
      if (!s.contains(seq[i]))
      {
        s.add(seq[i]);
        if (tchain != null && i < tchain.length)
        {
          c.add(tchain[i] == null ? nullChain : tchain[i]);
        }
      }
    }
    SequenceI[] tmp = s.toArray(new SequenceI[s.size()]);
    getSequence()[pe] = tmp;
    if (c.size() > 0)
    {
      String[] tch = c.toArray(new String[c.size()]);
      for (int i = 0; i < tch.length; i++)
      {
        if (tch[i] == nullChain)
        {
          tch[i] = null;
        }
      }
      getChains()[pe] = tch;
    }
    else
    {
      getChains()[pe] = null;
    }
  }

  /**
   * add structures and any known sequence associations
   * 
   * @returns the pdb entries added to the current set.
   */
  public synchronized PDBEntry[] addSequenceAndChain(PDBEntry[] pdbe,
          SequenceI[][] seq, String[][] chns)
  {
    List<PDBEntry> v = new ArrayList<>();
    List<int[]> rtn = new ArrayList<>();
    for (int i = 0; i < getPdbCount(); i++)
    {
      v.add(getPdbEntry(i));
    }
    for (int i = 0; i < pdbe.length; i++)
    {
      int r = v.indexOf(pdbe[i]);
      if (r == -1 || r >= getPdbCount())
      {
        rtn.add(new int[] { v.size(), i });
        v.add(pdbe[i]);
      }
      else
      {
        // just make sure the sequence/chain entries are all up to date
        addSequenceAndChain(r, seq[i], chns[i]);
      }
    }
    pdbe = v.toArray(new PDBEntry[v.size()]);
    setPdbentry(pdbe);
    if (rtn.size() > 0)
    {
      // expand the tied sequence[] and string[] arrays
      SequenceI[][] sqs = new SequenceI[getPdbCount()][];
      String[][] sch = new String[getPdbCount()][];
      System.arraycopy(getSequence(), 0, sqs, 0, getSequence().length);
      System.arraycopy(getChains(), 0, sch, 0, this.getChains().length);
      setSequence(sqs);
      setChains(sch);
      pdbe = new PDBEntry[rtn.size()];
      for (int r = 0; r < pdbe.length; r++)
      {
        int[] stri = (rtn.get(r));
        // record the pdb file as a new addition
        pdbe[r] = getPdbEntry(stri[0]);
        // and add the new sequence/chain entries
        addSequenceAndChain(stri[0], seq[stri[1]], chns[stri[1]]);
      }
    }
    else
    {
      pdbe = null;
    }
    return pdbe;
  }

  /**
   * Add sequences to the pe'th pdbentry's sequence set.
   * 
   * @param pe
   * @param seq
   */
  public void addSequence(int pe, SequenceI[] seq)
  {
    addSequenceAndChain(pe, seq, null);
  }

  /**
   * add the given sequences to the mapping scope for the given pdb file handle
   * 
   * @param pdbFile
   *          - pdbFile identifier
   * @param seq
   *          - set of sequences it can be mapped to
   */
  public void addSequenceForStructFile(String pdbFile, SequenceI[] seq)
  {
    for (int pe = 0; pe < getPdbCount(); pe++)
    {
      if (getPdbEntry(pe).getFile().equals(pdbFile))
      {
        addSequence(pe, seq);
      }
    }
  }

  @Override
  public abstract void highlightAtoms(List<AtomSpec> atoms);

  protected boolean isNucleotide()
  {
    return this.nucleotide;
  }

  /**
   * Returns a readable description of all mappings for the wrapped pdbfile to
   * any mapped sequences
   * 
   * @param pdbfile
   * @param seqs
   * @return
   */
  public String printMappings()
  {
    if (pdbEntry == null)
    {
      return "";
    }
    StringBuilder sb = new StringBuilder(128);
    for (int pdbe = 0; pdbe < getPdbCount(); pdbe++)
    {
      String pdbfile = getPdbEntry(pdbe).getFile();
      List<SequenceI> seqs = Arrays.asList(getSequence()[pdbe]);
      sb.append(getSsm().printMappings(pdbfile, seqs));
    }
    return sb.toString();
  }

  /**
   * Returns the mapped structure position for a given aligned column of a given
   * sequence, or -1 if the column is gapped, beyond the end of the sequence, or
   * not mapped to structure.
   * 
   * @param seq
   * @param alignedPos
   * @param mapping
   * @return
   */
  protected int getMappedPosition(SequenceI seq, int alignedPos,
          StructureMapping mapping)
  {
    if (alignedPos >= seq.getLength())
    {
      return -1;
    }

    if (Comparison.isGap(seq.getCharAt(alignedPos)))
    {
      return -1;
    }
    int seqPos = seq.findPosition(alignedPos);
    int pos = mapping.getPDBResNum(seqPos);
    return pos;
  }

  /**
   * Helper method to identify residues that can participate in a structure
   * superposition command. For each structure, identify a sequence in the
   * alignment which is mapped to the structure. Identify non-gapped columns in
   * the sequence which have a mapping to a residue in the structure. Returns
   * the index of the first structure that has a mapping to the alignment.
   * 
   * @param alignment
   *          the sequence alignment which is the basis of structure
   *          superposition
   * @param matched
   *          a BitSet, where bit j is set to indicate that every structure has
   *          a mapped residue present in column j (so the column can
   *          participate in structure alignment)
   * @param structures
   *          an array of data beans corresponding to pdb file index
   * @return
   */
  protected int findSuperposableResidues(AlignmentI alignment,
          BitSet matched,
          AAStructureBindingModel.SuperposeData[] structures)
  {
    int refStructure = -1;
    String[] files = getStructureFiles();
    if (files == null)
    {
      return -1;
    }
    for (int pdbfnum = 0; pdbfnum < files.length; pdbfnum++)
    {
      StructureMapping[] mappings = getSsm().getMapping(files[pdbfnum]);
      int lastPos = -1;

      /*
       * Find the first mapped sequence (if any) for this PDB entry which is in
       * the alignment
       */
      final int seqCountForPdbFile = getSequence()[pdbfnum].length;
      for (int s = 0; s < seqCountForPdbFile; s++)
      {
        for (StructureMapping mapping : mappings)
        {
          final SequenceI theSequence = getSequence()[pdbfnum][s];
          if (mapping.getSequence() == theSequence
                  && alignment.findIndex(theSequence) > -1)
          {
            if (refStructure < 0)
            {
              refStructure = pdbfnum;
            }
            for (int r = 0; r < alignment.getWidth(); r++)
            {
              if (!matched.get(r))
              {
                continue;
              }
              int pos = getMappedPosition(theSequence, r, mapping);
              if (pos < 1 || pos == lastPos)
              {
                matched.clear(r);
                continue;
              }
              lastPos = pos;
              structures[pdbfnum].pdbResNo[r] = pos;
            }
            String chain = mapping.getChain();
            if (chain != null && chain.trim().length() > 0)
            {
              structures[pdbfnum].chain = chain;
            }
            structures[pdbfnum].pdbId = mapping.getPdbId();
            structures[pdbfnum].isRna = !theSequence.isProtein();

            /*
             * move on to next pdb file (ignore sequences for other chains
             * for the same structure)
             */
            s = seqCountForPdbFile;
            break; // fixme break out of two loops here!
          }
        }
      }
    }
    return refStructure;
  }

  /**
   * Returns true if the structure viewer has loaded all of the files of
   * interest (identified by the file mapping having been set up), or false if
   * any are still not loaded after a timeout interval.
   * 
   * @param files
   */
  protected boolean waitForFileLoad(String[] files)
  {
    /*
     * give up after 10 secs plus 1 sec per file
     */
    long starttime = System.currentTimeMillis();
    long endTime = 10000 + 1000 * files.length + starttime;
    String notLoaded = null;

    boolean waiting = true;
    while (waiting && System.currentTimeMillis() < endTime)
    {
      waiting = false;
      for (String file : files)
      {
        notLoaded = file;
        if (file == null)
        {
          continue;
        }
        try
        {
          StructureMapping[] sm = getSsm().getMapping(file);
          if (sm == null || sm.length == 0)
          {
            waiting = true;
          }
        } catch (Throwable x)
        {
          waiting = true;
        }
      }
    }

    if (waiting)
    {
      System.err.println(
              "Timed out waiting for structure viewer to load file "
                      + notLoaded);
      return false;
    }
    return true;
  }

  @Override
  public boolean isListeningFor(SequenceI seq)
  {
    if (sequence != null)
    {
      for (SequenceI[] seqs : sequence)
      {
        if (seqs != null)
        {
          for (SequenceI s : seqs)
          {
            if (s == seq || (s.getDatasetSequence() != null
                    && s.getDatasetSequence() == seq.getDatasetSequence()))
            {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  public boolean isFinishedInit()
  {
    return finishedInit;
  }

  public void setFinishedInit(boolean fi)
  {
    this.finishedInit = fi;
  }

  /**
   * Returns a list of chains mapped in this viewer, formatted as
   * "pdbid:chainCode"
   * 
   * @return
   */
  public List<String> getChainNames()
  {
    return chainNames;
  }

  /**
   * Returns the Jalview panel hosting the structure viewer (if any)
   * 
   * @return
   */
  public JalviewStructureDisplayI getViewer()
  {
    return viewer;
  }

  public void setViewer(JalviewStructureDisplayI v)
  {
    viewer = v;
  }

  /**
   * Constructs and sends a command to align structures against a reference
   * structure, based on one or more sequence alignments. May optionally return
   * an error or warning message for the alignment command(s).
   * 
   * @param alignWith
   *          an array of one or more alignment views to process
   * @return
   */
  public String superposeStructures(List<AlignmentViewPanel> alignWith)
  {
    String error = "";
    String[] files = getStructureFiles();

    if (!waitForFileLoad(files))
    {
      return null;
    }
    refreshPdbEntries();

    for (AlignmentViewPanel view : alignWith)
    {
      AlignmentI alignment = view.getAlignment();
      HiddenColumns hiddenCols = alignment.getHiddenColumns();
      /*
       * 'matched' bit i will be set for visible alignment columns i where
       * all sequences have a residue with a mapping to their PDB structure
       */
      final int width = alignment.getWidth();
      BitSet matched = new BitSet();
      ColumnSelection cs = view.getAlignViewport().getColumnSelection();
      // restrict to active column selection, if there is one
      if (cs != null && cs.hasSelectedColumns()
              && cs.getSelected().size() >= 4)
      {
        for (int s : cs.getSelected())
        {
          if (hiddenCols == null)
          {
            matched.set(s);
          }
          else
          {
            matched.set(hiddenCols.visibleToAbsoluteColumn(s));
          }
        }
      }
      else
      {
        for (int m = 0; m < width; m++)
        {
          if (hiddenCols == null || hiddenCols.isVisible(m))
          {
            matched.set(m);
          }
        }
      }
      AAStructureBindingModel.SuperposeData[] structures = new AAStructureBindingModel.SuperposeData[files.length];
      for (int f = 0; f < files.length; f++)
      {
        structures[f] = new AAStructureBindingModel.SuperposeData(width,
                getModelIdForFile(files[f]));
      }

      /*
       * Calculate the superposable alignment columns ('matched'), and the
       * corresponding structure residue positions (structures.pdbResNo)
       */
      int refStructure = findSuperposableResidues(alignment, matched,
              structures);

      /*
       * require at least 4 positions to be able to execute superposition
       */
      int nmatched = matched.cardinality();
      if (nmatched < MIN_POS_TO_SUPERPOSE)
      {
        String msg = MessageManager
                .formatMessage("label.insufficient_residues", nmatched);
        error += view.getViewName() + ": " + msg + "; ";
        continue;
      }

      /*
       * get a model of the superposable residues in the reference structure 
       */
      AtomSpecModel refAtoms = getAtomSpec(structures[refStructure],
              matched);

      /*
       * Show all as backbone before doing superposition(s)
       * (residues used for matching will be shown as ribbon)
       */
      // todo better way to ensure synchronous than setting getReply true!!
      executeCommands(commandGenerator.showBackbone(), true, null);

      AtomSpecType backbone = structures[refStructure].isRna
              ? AtomSpecType.PHOSPHATE
              : AtomSpecType.ALPHA;
      List<AtomSpecModel> models = new ArrayList<AtomSpecModel>();
      models.add(refAtoms);
      /*
       * superpose each (other) structure to the reference in turn
       */
      for (int i = 0; i < structures.length; i++)
      {
        if (i != refStructure)
        {
          AtomSpecModel atomSpec = getAtomSpec(structures[i], matched);
          List<StructureCommandI> commands = commandGenerator
                  .superposeStructures(refAtoms, atomSpec, backbone);
          List<String> replies = executeCommands(commands, true, null);
          for (String reply : replies)
          {
            // return this error (Chimera only) to the user
            if (reply.toLowerCase(Locale.ROOT)
                    .contains("unequal numbers of atoms"))
            {
              error += "; " + reply;
            }
          }
          models.add(atomSpec);
        }
      }
      List<StructureCommandI> finalView = commandGenerator
              .centerViewOn(models);
      if (finalView != null && finalView.size() > 0)
      {
        executeCommands(finalView, false, "Centered on Superposition");
      }
    }
    return error;
  }

  private AtomSpecModel getAtomSpec(
          AAStructureBindingModel.SuperposeData superposeData,
          BitSet matched)
  {
    AtomSpecModel model = new AtomSpecModel();
    int nextColumnMatch = matched.nextSetBit(0);
    while (nextColumnMatch != -1)
    {
      int pdbResNum = superposeData.pdbResNo[nextColumnMatch];
      model.addRange(superposeData.modelId, pdbResNum, pdbResNum,
              superposeData.chain);
      nextColumnMatch = matched.nextSetBit(nextColumnMatch + 1);
    }

    return model;
  }

  /**
   * returns the current sequenceRenderer that should be used to colour the
   * structures
   * 
   * @param alignment
   * 
   * @return
   */
  public abstract SequenceRenderer getSequenceRenderer(
          AlignmentViewPanel alignment);

  /**
   * Sends a command to the structure viewer to colour each chain with a
   * distinct colour (to the extent supported by the viewer)
   */
  public void colourByChain()
  {
    colourBySequence = false;

    // TODO: JAL-628 colour chains distinctly across all visible models

    executeCommand(false, COLOURING_STRUCTURES,
            commandGenerator.colourByChain());
  }

  /**
   * Sends a command to the structure viewer to colour each chain with a
   * distinct colour (to the extent supported by the viewer)
   */
  public void colourByCharge()
  {
    colourBySequence = false;

    executeCommands(commandGenerator.colourByCharge(), false,
            COLOURING_STRUCTURES);
  }

  /**
   * Sends a command to the structure to apply a colour scheme (defined in
   * Jalview but not necessarily applied to the alignment), which defines a
   * colour per residue letter. More complex schemes (e.g. that depend on
   * consensus) cannot be used here and are ignored.
   * 
   * @param cs
   */
  public void colourByJalviewColourScheme(ColourSchemeI cs)
  {
    colourBySequence = false;

    if (cs == null || !cs.isSimple())
    {
      return;
    }

    /*
     * build a map of {Residue3LetterCode, Color}
     */
    Map<String, Color> colours = new HashMap<>();
    List<String> residues = ResidueProperties.getResidues(isNucleotide(),
            false);
    for (String resName : residues)
    {
      char res = resName.length() == 3
              ? ResidueProperties.getSingleCharacterCode(resName)
              : resName.charAt(0);
      Color colour = cs.findColour(res, 0, null, null, 0f);
      colours.put(resName, colour);
    }

    /*
     * pass to the command constructor, and send the command
     */
    List<StructureCommandI> cmd = commandGenerator
            .colourByResidues(colours);
    executeCommands(cmd, false, COLOURING_STRUCTURES);
  }

  public void setBackgroundColour(Color col)
  {
    StructureCommandI cmd = commandGenerator.setBackgroundColour(col);
    executeCommand(false, null, cmd);
  }

  /**
   * Execute one structure viewer command. If {@code getReply} is true, may
   * optionally return one or more reply messages, else returns null.
   * 
   * @param cmd
   * @param getReply
   */
  protected abstract List<String> executeCommand(StructureCommandI cmd,
          boolean getReply);

  /**
   * Executes one or more structure viewer commands
   * 
   * @param commands
   * @param getReply
   * @param msg
   */
  protected List<String> executeCommands(List<StructureCommandI> commands,
          boolean getReply, String msg)
  {
    return executeCommand(getReply, msg,
            commands.toArray(new StructureCommandI[commands.size()]));
  }

  /**
   * Executes one or more structure viewer commands, optionally returning the
   * reply, and optionally showing a status message while the command is being
   * executed.
   * <p>
   * If a reply is wanted, the execution is done synchronously (waits),
   * otherwise it is done in a separate thread (doesn't wait). WARNING: if you
   * are sending commands that need to execute before later calls to
   * executeCommand (e.g. mouseovers, which clean up after previous ones) then
   * set getReply true to ensure that commands are not executed out of order.
   * 
   * @param getReply
   * @param msg
   * @param cmds
   * @return
   */
  protected List<String> executeCommand(boolean getReply, String msg,
          StructureCommandI... cmds)
  {
    JalviewStructureDisplayI theViewer = getViewer();
    final long handle = msg == null ? 0 : theViewer.startProgressBar(msg);

    if (getReply)
    {
      /*
       * execute and wait for reply
       */
      List<String> response = new ArrayList<>();
      try
      {
        for (StructureCommandI cmd : cmds)
        {
          List<String> replies = executeCommand(cmd, true);
          if (replies != null)
          {
            response.addAll(replies);
          }
        }
        return response;
      } finally
      {
        if (msg != null)
        {
          theViewer.stopProgressBar(null, handle);
        }
      }
    }

    /*
     * fire and forget
     */
    String threadName = msg == null ? "StructureCommand" : msg;
    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          for (StructureCommandI cmd : cmds)
          {
            executeCommand(cmd, false);
          }
        } finally
        {
          if (msg != null)
          {
            SwingUtilities.invokeLater(new Runnable()
            {
              @Override
              public void run()
              {
                theViewer.stopProgressBar(null, handle);
              }
            });
          }
        }
      }
    }, threadName).start();
    return null;
  }

  /**
   * Colours any structures associated with sequences in the given alignment as
   * coloured in the alignment view, provided colourBySequence is enabled
   */
  public void colourBySequence(AlignmentViewPanel alignmentv)
  {
    if (!colourBySequence || !isLoadingFinished() || getSsm() == null)
    {
      return;
    }
    Map<Object, AtomSpecModel> colourMap = buildColoursMap(ssm, sequence,
            alignmentv);

    List<StructureCommandI> colourBySequenceCommands = commandGenerator
            .colourBySequence(colourMap);
    executeCommands(colourBySequenceCommands, false, COLOURING_STRUCTURES);
  }

  /**
   * Centre the display in the structure viewer
   */
  public void focusView()
  {
    executeCommand(false, null, commandGenerator.focusView());
  }

  /**
   * Generates and executes a command to show only specified chains in the
   * structure viewer. The list of chains to show should contain entries
   * formatted as "pdbid:chaincode".
   * 
   * @param toShow
   */
  public void showChains(List<String> toShow)
  {
    // todo or reformat toShow list entries as modelNo:pdbId:chainCode ?

    /*
     * Reformat the pdbid:chainCode values as modelNo:chainCode
     * since this is what is needed to construct the viewer command
     * todo: find a less messy way to do this
     */
    List<String> showThese = new ArrayList<>();
    for (String chainId : toShow)
    {
      String[] tokens = chainId.split("\\:");
      if (tokens.length == 2)
      {
        String pdbFile = getFileForChain(chainId);
        String model = getModelIdForFile(pdbFile);
        showThese.add(model + ":" + tokens[1]);
      }
    }
    executeCommands(commandGenerator.showChains(showThese), false, null);
  }

  /**
   * Answers the structure viewer's model id given a PDB file name. Returns an
   * empty string if model id is not found.
   * 
   * @param chainId
   * @return
   */
  protected abstract String getModelIdForFile(String chainId);

  public boolean hasFileLoadingError()
  {
    return fileLoadingError != null && fileLoadingError.length() > 0;
  }

  /**
   * Returns the FeatureRenderer for the given alignment view
   * 
   * @param avp
   * @return
   */
  public FeatureRenderer getFeatureRenderer(AlignmentViewPanel avp)
  {
    AlignmentViewPanel ap = (avp == null) ? getViewer().getAlignmentPanel()
            : avp;
    if (ap == null)
    {
      return null;
    }
    return ap.getFeatureRenderer();
  }

  protected void setStructureCommands(StructureCommandsI cmd)
  {
    commandGenerator = cmd;
  }

  /**
   * Records association of one chain id (formatted as "pdbid:chainCode") with
   * the corresponding PDB file name
   * 
   * @param chainId
   * @param fileName
   */
  public void addChainFile(String chainId, String fileName)
  {
    chainFile.put(chainId, fileName);
  }

  /**
   * Returns the PDB filename for the given chain id (formatted as
   * "pdbid:chainCode"), or null if not found
   * 
   * @param chainId
   * @return
   */
  protected String getFileForChain(String chainId)
  {
    return chainFile.get(chainId);
  }

  @Override
  public void updateColours(Object source)
  {
    if (getViewer() == null)
    {
      // can happen if a viewer was not instantiated or cleaned up and is still
      // registered - mostly during tests
      return;
    }
    AlignmentViewPanel ap = (AlignmentViewPanel) source;
    // ignore events from panels not used to colour this view
    if (!getViewer().isUsedForColourBy(ap))
    {
      return;
    }
    if (!isLoadingFromArchive())
    {
      colourBySequence(ap);
    }
  }

  public StructureCommandsI getCommandGenerator()
  {
    return commandGenerator;
  }

  protected abstract ViewerType getViewerType();

  /**
   * Builds a data structure which records mapped structure residues for each
   * colour. From this we can easily generate the viewer commands for colour by
   * sequence. Constructs and returns a map of {@code Color} to
   * {@code AtomSpecModel}, where the atomspec model holds
   * 
   * <pre>
   *   Model ids
   *     Chains
   *       Residue positions
   * </pre>
   * 
   * Ordering is by order of addition (for colours), natural ordering (for
   * models and chains)
   * 
   * @param ssm
   * @param sequence
   * @param viewPanel
   * @return
   */
  protected Map<Object, AtomSpecModel> buildColoursMap(
          StructureSelectionManager ssm, SequenceI[][] sequence,
          AlignmentViewPanel viewPanel)
  {
    String[] files = getStructureFiles();
    SequenceRenderer sr = getSequenceRenderer(viewPanel);
    FeatureRenderer fr = viewPanel.getFeatureRenderer();
    FeatureColourFinder finder = new FeatureColourFinder(fr);
    AlignViewportI viewport = viewPanel.getAlignViewport();
    HiddenColumns cs = viewport.getAlignment().getHiddenColumns();
    AlignmentI al = viewport.getAlignment();
    Map<Object, AtomSpecModel> colourMap = new LinkedHashMap<>();
    Color lastColour = null;

    for (int pdbfnum = 0; pdbfnum < files.length; pdbfnum++)
    {
      final String modelId = getModelIdForFile(files[pdbfnum]);
      StructureMapping[] mapping = ssm.getMapping(files[pdbfnum]);

      if (mapping == null || mapping.length < 1)
      {
        continue;
      }

      int startPos = -1, lastPos = -1;
      String lastChain = "";
      for (int s = 0; s < sequence[pdbfnum].length; s++)
      {
        for (int sp, m = 0; m < mapping.length; m++)
        {
          final SequenceI seq = sequence[pdbfnum][s];
          if (mapping[m].getSequence() == seq
                  && (sp = al.findIndex(seq)) > -1)
          {
            SequenceI asp = al.getSequenceAt(sp);
            for (int r = 0; r < asp.getLength(); r++)
            {
              // no mapping to gaps in sequence
              if (Comparison.isGap(asp.getCharAt(r)))
              {
                continue;
              }
              int pos = mapping[m].getPDBResNum(asp.findPosition(r));

              if (pos < 1 || pos == lastPos)
              {
                continue;
              }

              Color colour = sr.getResidueColour(seq, r, finder);

              /*
               * darker colour for hidden regions
               */
              if (!cs.isVisible(r))
              {
                colour = Color.GRAY;
              }

              final String chain = mapping[m].getChain();

              /*
               * Just keep incrementing the end position for this colour range
               * _unless_ colour, PDB model or chain has changed, or there is a
               * gap in the mapped residue sequence
               */
              final boolean newColour = !colour.equals(lastColour);
              final boolean nonContig = lastPos + 1 != pos;
              final boolean newChain = !chain.equals(lastChain);
              if (newColour || nonContig || newChain)
              {
                if (startPos != -1)
                {
                  addAtomSpecRange(colourMap, lastColour, modelId, startPos,
                          lastPos, lastChain);
                }
                startPos = pos;
              }
              lastColour = colour;
              lastPos = pos;
              lastChain = chain;
            }
            // final colour range
            if (lastColour != null)
            {
              addAtomSpecRange(colourMap, lastColour, modelId, startPos,
                      lastPos, lastChain);
            }
            // break;
          }
        }
      }
    }
    return colourMap;
  }

  /**
   * todo better refactoring (map lookup or similar to get viewer structure id)
   * 
   * @param pdbfnum
   * @param file
   * @return
   */
  protected String getModelId(int pdbfnum, String file)
  {
    return String.valueOf(pdbfnum);
  }

  /**
   * Saves chains, formatted as "pdbId:chainCode", and lookups from this to the
   * full PDB file path
   * 
   * @param pdb
   * @param file
   */
  public void stashFoundChains(StructureFile pdb, String file)
  {
    for (int i = 0; i < pdb.getChains().size(); i++)
    {
      String chid = pdb.getId() + ":" + pdb.getChains().elementAt(i).id;
      addChainFile(chid, file);
      getChainNames().add(chid);
    }
  }

  /**
   * Helper method to add one contiguous range to the AtomSpec model for the
   * given value (creating the model if necessary). As used by Jalview,
   * {@code value} is
   * <ul>
   * <li>a colour, when building a 'colour structure by sequence' command</li>
   * <li>a feature value, when building a 'set Chimera attributes from features'
   * command</li>
   * </ul>
   * 
   * @param map
   * @param value
   * @param model
   * @param startPos
   * @param endPos
   * @param chain
   */
  public static final void addAtomSpecRange(Map<Object, AtomSpecModel> map,
          Object value, String model, int startPos, int endPos,
          String chain)
  {
    /*
     * Get/initialize map of data for the colour
     */
    AtomSpecModel atomSpec = map.get(value);
    if (atomSpec == null)
    {
      atomSpec = new AtomSpecModel();
      map.put(value, atomSpec);
    }

    atomSpec.addRange(model, startPos, endPos, chain);
  }

  /**
   * Returns the file extension (including '.' separator) to use for a saved
   * viewer session file. Default is to return null (not supported), override as
   * required.
   * 
   * @return
   */
  public String getSessionFileExtension()
  {
    return null;
  }

  /**
   * If supported, saves the state of the structure viewer to a temporary file
   * and returns the file. Returns null and logs an error on any failure.
   * 
   * @return
   */
  public File saveSession()
  {
    String prefix = getViewerType().toString();
    String suffix = getSessionFileExtension();
    File f = null;
    try
    {
      f = File.createTempFile(prefix, suffix);
      saveSession(f);
    } catch (IOException e)
    {
      Console.error(String.format("Error saving %s session: %s", prefix,
              e.toString()));
    }

    return f;
  }

  /**
   * Saves the structure viewer session to the given file
   * 
   * @param f
   */
  protected void saveSession(File f)
  {
    StructureCommandI cmd = commandGenerator.saveSession(f.getPath());
    if (cmd != null)
    {
      executeCommand(cmd, false);
    }
  }

  /**
   * Returns true if the viewer is an external structure viewer for which the
   * process is still alive, else false (for Jmol, or an external viewer which
   * the user has independently closed)
   * 
   * @return
   */
  public boolean isViewerRunning()
  {
    return false;
  }

  /**
   * Closes Jalview's structure viewer panel and releases associated resources.
   * If it is managing an external viewer program, and {@code forceClose} is
   * true, also asks that program to close.
   * 
   * @param forceClose
   */
  public void closeViewer(boolean forceClose)
  {
    getSsm().removeStructureViewerListener(this, this.getStructureFiles());
    releaseUIResources();

    /*
     * end the thread that closes this panel if the external viewer closes
     */
    if (externalViewerMonitor != null)
    {
      externalViewerMonitor.interrupt();
      externalViewerMonitor = null;
    }

    stopListening();

    if (forceClose)
    {
      StructureCommandI cmd = getCommandGenerator().closeViewer();
      if (cmd != null)
      {
        executeCommand(cmd, false);
      }
    }
  }

  /**
   * Returns the URL of a help page for the structure viewer, or null if none is
   * known
   * 
   * @return
   */
  public String getHelpURL()
  {
    return null;
  }

  /**
   * <pre>
   * Helper method to build a map of 
   *   { featureType, { feature value, AtomSpecModel } }
   * </pre>
   * 
   * @param viewPanel
   * @return
   */
  protected Map<String, Map<Object, AtomSpecModel>> buildFeaturesMap(
          AlignmentViewPanel viewPanel)
  {
    Map<String, Map<Object, AtomSpecModel>> theMap = new LinkedHashMap<>();
    String[] files = getStructureFiles();
    if (files == null)
    {
      return theMap;
    }

    FeatureRenderer fr = viewPanel.getFeatureRenderer();
    if (fr == null)
    {
      return theMap;
    }

    AlignViewportI viewport = viewPanel.getAlignViewport();
    List<String> visibleFeatures = fr.getDisplayedFeatureTypes();

    /*
     * if alignment is showing features from complement, we also transfer
     * these features to the corresponding mapped structure residues
     */
    boolean showLinkedFeatures = viewport.isShowComplementFeatures();
    List<String> complementFeatures = new ArrayList<>();
    FeatureRenderer complementRenderer = null;
    if (showLinkedFeatures)
    {
      AlignViewportI comp = fr.getViewport().getCodingComplement();
      if (comp != null)
      {
        complementRenderer = Desktop.getAlignFrameFor(comp)
                .getFeatureRenderer();
        complementFeatures = complementRenderer.getDisplayedFeatureTypes();
      }
    }
    if (visibleFeatures.isEmpty() && complementFeatures.isEmpty())
    {
      return theMap;
    }

    AlignmentI alignment = viewPanel.getAlignment();
    SequenceI[][] seqs = getSequence();

    for (int pdbfnum = 0; pdbfnum < files.length; pdbfnum++)
    {
      String modelId = getModelIdForFile(files[pdbfnum]);
      StructureMapping[] mapping = ssm.getMapping(files[pdbfnum]);

      if (mapping == null || mapping.length < 1)
      {
        continue;
      }

      for (int seqNo = 0; seqNo < seqs[pdbfnum].length; seqNo++)
      {
        for (int m = 0; m < mapping.length; m++)
        {
          final SequenceI seq = seqs[pdbfnum][seqNo];
          int sp = alignment.findIndex(seq);
          StructureMapping structureMapping = mapping[m];
          if (structureMapping.getSequence() == seq && sp > -1)
          {
            /*
             * found a sequence with a mapping to a structure;
             * now scan its features
             */
            if (!visibleFeatures.isEmpty())
            {
              scanSequenceFeatures(visibleFeatures, structureMapping, seq,
                      theMap, modelId);
            }
            if (showLinkedFeatures)
            {
              scanComplementFeatures(complementRenderer, structureMapping,
                      seq, theMap, modelId);
            }
          }
        }
      }
    }
    return theMap;
  }

  /**
   * Ask the structure viewer to open a session file. Returns true if
   * successful, else false (or not supported).
   * 
   * @param filepath
   * @return
   */
  public boolean openSession(String filepath)
  {
    StructureCommandI cmd = getCommandGenerator().openSession(filepath);
    if (cmd == null)
    {
      return false;
    }
    executeCommand(cmd, true);
    // todo: test for failure - how?
    return true;
  }

  /**
   * Scans visible features in mapped positions of the CDS/peptide complement,
   * and adds any found to the map of attribute values/structure positions
   * 
   * @param complementRenderer
   * @param structureMapping
   * @param seq
   * @param theMap
   * @param modelNumber
   */
  protected static void scanComplementFeatures(
          FeatureRenderer complementRenderer,
          StructureMapping structureMapping, SequenceI seq,
          Map<String, Map<Object, AtomSpecModel>> theMap,
          String modelNumber)
  {
    /*
     * for each sequence residue mapped to a structure position...
     */
    for (int seqPos : structureMapping.getMapping().keySet())
    {
      /*
       * find visible complementary features at mapped position(s)
       */
      MappedFeatures mf = complementRenderer
              .findComplementFeaturesAtResidue(seq, seqPos);
      if (mf != null)
      {
        for (SequenceFeature sf : mf.features)
        {
          String type = sf.getType();

          /*
           * Don't copy features which originated from Chimera
           */
          if (JalviewChimeraBinding.CHIMERA_FEATURE_GROUP
                  .equals(sf.getFeatureGroup()))
          {
            continue;
          }

          /*
           * record feature 'value' (score/description/type) as at the
           * corresponding structure position
           */
          List<int[]> mappedRanges = structureMapping
                  .getPDBResNumRanges(seqPos, seqPos);

          if (!mappedRanges.isEmpty())
          {
            String value = sf.getDescription();
            if (value == null || value.length() == 0)
            {
              value = type;
            }
            float score = sf.getScore();
            if (score != 0f && !Float.isNaN(score))
            {
              value = Float.toString(score);
            }
            Map<Object, AtomSpecModel> featureValues = theMap.get(type);
            if (featureValues == null)
            {
              featureValues = new HashMap<>();
              theMap.put(type, featureValues);
            }
            for (int[] range : mappedRanges)
            {
              addAtomSpecRange(featureValues, value, modelNumber, range[0],
                      range[1], structureMapping.getChain());
            }
          }
        }
      }
    }
  }

  /**
   * Inspect features on the sequence; for each feature that is visible,
   * determine its mapped ranges in the structure (if any) according to the
   * given mapping, and add them to the map.
   * 
   * @param visibleFeatures
   * @param mapping
   * @param seq
   * @param theMap
   * @param modelId
   */
  protected static void scanSequenceFeatures(List<String> visibleFeatures,
          StructureMapping mapping, SequenceI seq,
          Map<String, Map<Object, AtomSpecModel>> theMap, String modelId)
  {
    List<SequenceFeature> sfs = seq.getFeatures().getPositionalFeatures(
            visibleFeatures.toArray(new String[visibleFeatures.size()]));
    for (SequenceFeature sf : sfs)
    {
      String type = sf.getType();

      /*
       * Don't copy features which originated from Chimera
       */
      if (JalviewChimeraBinding.CHIMERA_FEATURE_GROUP
              .equals(sf.getFeatureGroup()))
      {
        continue;
      }

      List<int[]> mappedRanges = mapping.getPDBResNumRanges(sf.getBegin(),
              sf.getEnd());

      if (!mappedRanges.isEmpty())
      {
        String value = sf.getDescription();
        if (value == null || value.length() == 0)
        {
          value = type;
        }
        float score = sf.getScore();
        if (score != 0f && !Float.isNaN(score))
        {
          value = Float.toString(score);
        }
        Map<Object, AtomSpecModel> featureValues = theMap.get(type);
        if (featureValues == null)
        {
          featureValues = new HashMap<>();
          theMap.put(type, featureValues);
        }
        for (int[] range : mappedRanges)
        {
          addAtomSpecRange(featureValues, value, modelId, range[0],
                  range[1], mapping.getChain());
        }
      }
    }
  }

  /**
   * Returns the number of structure files in the structure viewer and mapped to
   * Jalview. This may be zero if the files are still in the process of loading
   * in the viewer.
   * 
   * @return
   */
  public int getMappedStructureCount()
  {
    String[] files = getStructureFiles();
    return files == null ? 0 : files.length;
  }

  /**
   * Starts a thread that waits for the external viewer program process to
   * finish, so that we can then close the associated resources. This avoids
   * leaving orphaned viewer panels in Jalview if the user closes the external
   * viewer.
   * 
   * @param p
   */
  protected void startExternalViewerMonitor(Process p)
  {
    externalViewerMonitor = new Thread(new Runnable()
    {

      @Override
      public void run()
      {
        try
        {
          p.waitFor();
          JalviewStructureDisplayI display = getViewer();
          if (display != null)
          {
            display.closeViewer(false);
          }
        } catch (InterruptedException e)
        {
          // exit thread if Chimera Viewer is closed in Jalview
        }
      }
    });
    externalViewerMonitor.start();
  }

  /**
   * If supported by the external structure viewer, sends it commands to notify
   * model or selection changes to the specified URL (where Jalview has started
   * a listener)
   * 
   * @param uri
   */
  protected void startListening(String uri)
  {
    List<StructureCommandI> commands = getCommandGenerator()
            .startNotifications(uri);
    if (commands != null)
    {
      executeCommands(commands, false, null);
    }
  }

  /**
   * If supported by the external structure viewer, sends it commands to stop
   * notifying model or selection changes
   */
  protected void stopListening()
  {
    List<StructureCommandI> commands = getCommandGenerator()
            .stopNotifications();
    if (commands != null)
    {
      executeCommands(commands, false, null);
    }
  }

  /**
   * If supported by the structure viewer, queries it for all residue attributes
   * with the given attribute name, and creates features on corresponding
   * residues of the alignment. Returns the number of features added.
   * 
   * @param attName
   * @param alignmentPanel
   * @return
   */
  public int copyStructureAttributesToFeatures(String attName,
          AlignmentPanel alignmentPanel)
  {
    StructureCommandI cmd = getCommandGenerator()
            .getResidueAttributes(attName);
    if (cmd == null)
    {
      return 0;
    }
    List<String> residueAttributes = executeCommand(cmd, true);

    int featuresAdded = createFeaturesForAttributes(attName,
            residueAttributes);
    if (featuresAdded > 0)
    {
      alignmentPanel.getFeatureRenderer().featuresAdded();
    }
    return featuresAdded;
  }

  /**
   * Parses {@code residueAttributes} and creates sequence features on any
   * mapped alignment residues. Returns the number of features created.
   * <p>
   * {@code residueAttributes} is the reply from the structure viewer to a
   * command to list any residue attributes for the given attribute name. Syntax
   * and parsing of this is viewer-specific.
   * 
   * @param attName
   * @param residueAttributes
   * @return
   */
  protected int createFeaturesForAttributes(String attName,
          List<String> residueAttributes)
  {
    return 0;
  }
}
