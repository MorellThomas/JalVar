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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jalview.api.structures.JalviewStructureDisplayI;
import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.datamodel.StructureViewerModel;
import jalview.structure.StructureSelectionManager;

/**
 * A proxy for handling structure viewers, that orchestrates adding selected
 * structures, associated with sequences in Jalview, to an existing viewer, or
 * opening a new one. Currently supports either Jmol or Chimera as the structure
 * viewer.
 * 
 * @author jprocter
 */
public class StructureViewer
{
  private static final String UNKNOWN_VIEWER_TYPE = "Unknown structure viewer type ";

  StructureSelectionManager ssm;

  /**
   * decide if new structures are aligned to existing ones
   */
  private boolean superposeAdded = true;

  /**
   * whether to open structures in their own thread or not
   */
  private boolean async = true;

  public void setAsync(boolean b)
  {
    async = b;
  }

  public enum ViewerType
  {
    JMOL, CHIMERA, CHIMERAX, PYMOL
  };

  /**
   * Constructor
   * 
   * @param structureSelectionManager
   */
  public StructureViewer(
          StructureSelectionManager structureSelectionManager)
  {
    ssm = structureSelectionManager;
  }

  /**
   * Factory to create a proxy for modifying existing structure viewer
   * 
   */
  public static StructureViewer reconfigure(
          JalviewStructureDisplayI display)
  {
    StructureViewer sv = new StructureViewer(display.getBinding().getSsm());
    sv.sview = display;
    return sv;
  }

  @Override
  public String toString()
  {
    if (sview != null)
    {
      return sview.toString();
    }
    return "New View";
  }

  /**
   * 
   * @return ViewerType for currently configured structure viewer
   */
  public static ViewerType getViewerType()
  {
    String viewType = Cache.getDefault(Preferences.STRUCTURE_DISPLAY,
            ViewerType.JMOL.name());
    return ViewerType.valueOf(viewType);
  }

  public void setViewerType(ViewerType type)
  {
    Cache.setProperty(Preferences.STRUCTURE_DISPLAY, type.name());
  }

  /**
   * View multiple PDB entries, each with associated sequences
   * 
   * @param pdbs
   * @param seqs
   * @param ap
   * @return
   */
  public JalviewStructureDisplayI viewStructures(PDBEntry[] pdbs,
          SequenceI[] seqs, AlignmentPanel ap)
  {
    return viewStructures(pdbs, seqs, ap, null);
  }

  public JalviewStructureDisplayI viewStructures(PDBEntry[] pdbs,
          SequenceI[] seqs, AlignmentPanel ap, ViewerType viewerType)
  {
    JalviewStructureDisplayI viewer = onlyOnePdb(pdbs, seqs, ap);
    if (viewer != null)
    {
      /*
       * user added structure to an existing viewer - all done
       */
      return viewer;
    }

    if (viewerType == null)
      viewerType = getViewerType();

    Map<PDBEntry, SequenceI[]> seqsForPdbs = getSequencesForPdbs(pdbs,
            seqs);
    PDBEntry[] pdbsForFile = seqsForPdbs.keySet()
            .toArray(new PDBEntry[seqsForPdbs.size()]);
    SequenceI[][] theSeqs = seqsForPdbs.values()
            .toArray(new SequenceI[seqsForPdbs.size()][]);
    if (sview != null)
    {
      sview.setAlignAddedStructures(superposeAdded);

      Runnable viewRunnable = new Runnable()
      {
        @Override
        public void run()
        {

          for (int pdbep = 0; pdbep < pdbsForFile.length; pdbep++)
          {
            PDBEntry pdb = pdbsForFile[pdbep];
            if (!sview.addAlreadyLoadedFile(theSeqs[pdbep], null, ap,
                    pdb.getId()))
            {
              sview.addToExistingViewer(pdb, theSeqs[pdbep], null, ap,
                      pdb.getId());
            }
          }

          sview.updateTitleAndMenus();
        }
      };
      if (async)
      {
        new Thread(viewRunnable).start();
      }
      else
      {
        viewRunnable.run();
      }
      return sview;
    }

    if (viewerType.equals(ViewerType.JMOL))
    {
      sview = new AppJmol(ap, superposeAdded, pdbsForFile, theSeqs);
    }
    else if (viewerType.equals(ViewerType.CHIMERA))
    {
      sview = new ChimeraViewFrame(pdbsForFile, superposeAdded, theSeqs,
              ap);
    }
    else if (viewerType.equals(ViewerType.CHIMERAX))
    {
      sview = new ChimeraXViewFrame(pdbsForFile, superposeAdded, theSeqs,
              ap);
    }
    else if (viewerType.equals(ViewerType.PYMOL))
    {
      sview = new PymolViewer(pdbsForFile, superposeAdded, theSeqs, ap);
    }
    else
    {
      Console.error(UNKNOWN_VIEWER_TYPE + getViewerType().toString());
    }
    return sview;
  }

  /**
   * Converts the list of selected PDB entries (possibly including duplicates
   * for multiple chains), and corresponding sequences, into a map of sequences
   * for each distinct PDB file. Returns null if either argument is null, or
   * their lengths do not match.
   * 
   * @param pdbs
   * @param seqs
   * @return
   */
  Map<PDBEntry, SequenceI[]> getSequencesForPdbs(PDBEntry[] pdbs,
          SequenceI[] seqs)
  {
    if (pdbs == null || seqs == null || pdbs.length != seqs.length)
    {
      return null;
    }

    /*
     * we want only one 'representative' PDBEntry per distinct file name
     * (there may be entries for distinct chains)
     */
    Map<String, PDBEntry> pdbsSeen = new HashMap<>();

    /*
     * LinkedHashMap preserves order of PDB entries (significant if they
     * will get superimposed to the first structure)
     */
    Map<PDBEntry, List<SequenceI>> pdbSeqs = new LinkedHashMap<>();
    for (int i = 0; i < pdbs.length; i++)
    {
      PDBEntry pdb = pdbs[i];
      SequenceI seq = seqs[i];
      String pdbFile = pdb.getFile();
      if (pdbFile == null || pdbFile.length() == 0)
      {
        pdbFile = pdb.getId();
      }
      if (!pdbsSeen.containsKey(pdbFile))
      {
        pdbsSeen.put(pdbFile, pdb);
        pdbSeqs.put(pdb, new ArrayList<SequenceI>());
      }
      else
      {
        pdb = pdbsSeen.get(pdbFile);
      }
      List<SequenceI> seqsForPdb = pdbSeqs.get(pdb);
      if (!seqsForPdb.contains(seq))
      {
        seqsForPdb.add(seq);
      }
    }

    /*
     * convert to Map<PDBEntry, SequenceI[]>
     */
    Map<PDBEntry, SequenceI[]> result = new LinkedHashMap<>();
    for (Entry<PDBEntry, List<SequenceI>> entry : pdbSeqs.entrySet())
    {
      List<SequenceI> theSeqs = entry.getValue();
      result.put(entry.getKey(),
              theSeqs.toArray(new SequenceI[theSeqs.size()]));
    }

    return result;
  }

  /**
   * A strictly temporary method pending JAL-1761 refactoring. Determines if all
   * the passed PDB entries are the same (this is the case if selected sequences
   * to view structure for are chains of the same structure). If so, calls the
   * single-pdb version of viewStructures and returns the viewer, else returns
   * null.
   * 
   * @param pdbs
   * @param seqsForPdbs
   * @param ap
   * @return
   */
  private JalviewStructureDisplayI onlyOnePdb(PDBEntry[] pdbs,
          SequenceI[] seqsForPdbs, AlignmentPanel ap)
  {
    List<SequenceI> seqs = new ArrayList<>();
    if (pdbs == null || pdbs.length == 0)
    {
      return null;
    }
    int i = 0;
    String firstFile = pdbs[0].getFile();
    for (PDBEntry pdb : pdbs)
    {
      String pdbFile = pdb.getFile();
      if (pdbFile == null || !pdbFile.equals(firstFile))
      {
        return null;
      }
      SequenceI pdbseq = seqsForPdbs[i++];
      if (pdbseq != null)
      {
        seqs.add(pdbseq);
      }
    }
    return viewStructures(pdbs[0], seqs.toArray(new SequenceI[seqs.size()]),
            ap);
  }

  JalviewStructureDisplayI sview = null;

  public JalviewStructureDisplayI getJalviewStructureDisplay()
  {
    return sview;
  }

  public JalviewStructureDisplayI viewStructures(PDBEntry pdb,
          SequenceI[] seqsForPdb, AlignmentPanel ap)
  {
    return viewStructures(pdb, seqsForPdb, ap, null);
  }

  public JalviewStructureDisplayI viewStructures(PDBEntry pdb,
          SequenceI[] seqsForPdb, AlignmentPanel ap, ViewerType viewerType)
  {
    if (sview != null)
    {
      sview.setAlignAddedStructures(superposeAdded);
      String pdbId = pdb.getId();
      if (!sview.addAlreadyLoadedFile(seqsForPdb, null, ap, pdbId))
      {
        sview.addToExistingViewer(pdb, seqsForPdb, null, ap, pdbId);
      }
      sview.updateTitleAndMenus();
      sview.raiseViewer();
      return sview;
    }
    if (viewerType == null)
      viewerType = getViewerType();
    if (viewerType.equals(ViewerType.JMOL))
    {
      sview = new AppJmol(pdb, seqsForPdb, null, ap);
    }
    else if (viewerType.equals(ViewerType.CHIMERA))
    {
      sview = new ChimeraViewFrame(pdb, seqsForPdb, null, ap);
    }
    else if (viewerType.equals(ViewerType.CHIMERAX))
    {
      sview = new ChimeraXViewFrame(pdb, seqsForPdb, null, ap);
    }
    else if (viewerType.equals(ViewerType.PYMOL))
    {
      sview = new PymolViewer(pdb, seqsForPdb, null, ap);
    }
    else
    {
      Console.error(UNKNOWN_VIEWER_TYPE + getViewerType().toString());
    }
    return sview;
  }

  /**
   * Creates a new panel controlling a structure viewer
   * 
   * @param type
   * @param alignPanel
   * @param viewerData
   * @param sessionFile
   * @param vid
   * @return
   */
  public static JalviewStructureDisplayI createView(ViewerType type,
          AlignmentPanel alignPanel, StructureViewerModel viewerData,
          String sessionFile, String vid)
  {
    JalviewStructureDisplayI viewer = null;
    switch (type)
    {
    case JMOL:
      viewer = new AppJmol(viewerData, alignPanel, sessionFile, vid);
      // todo or construct and then openSession(sessionFile)?
      break;
    case CHIMERA:
      viewer = new ChimeraViewFrame(viewerData, alignPanel, sessionFile,
              vid);
      break;
    case CHIMERAX:
      viewer = new ChimeraXViewFrame(viewerData, alignPanel, sessionFile,
              vid);
      break;
    case PYMOL:
      viewer = new PymolViewer(viewerData, alignPanel, sessionFile, vid);
      break;
    default:
      Console.error(UNKNOWN_VIEWER_TYPE + type.toString());
    }
    return viewer;
  }

  public boolean isBusy()
  {
    if (sview != null)
    {
      if (!sview.hasMapping())
      {
        return true;
      }
    }
    return false;
  }

  /**
   * 
   * @param pDBid
   * @return true if view is already showing PDBid
   */
  public boolean hasPdbId(String pDBid)
  {
    if (sview == null)
    {
      return false;
    }

    return sview.getBinding().hasPdbId(pDBid);
  }

  public boolean isVisible()
  {
    return sview != null && sview.isVisible();
  }

  public void setSuperpose(boolean alignAddedStructures)
  {
    superposeAdded = alignAddedStructures;
  }

}
