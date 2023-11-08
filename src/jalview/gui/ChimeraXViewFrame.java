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

import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.datamodel.StructureViewerModel;
import jalview.gui.StructureViewer.ViewerType;

/**
 * A class for the gui frame through which Jalview interacts with the ChimeraX
 * structure viewer. Mostly the same as ChimeraViewFrame with a few overrides
 * for the differences.
 * 
 * @author gmcarstairs
 *
 */
public class ChimeraXViewFrame extends ChimeraViewFrame
{

  public ChimeraXViewFrame(PDBEntry pdb, SequenceI[] seqsForPdb,
          String[] chains, AlignmentPanel ap)
  {
    super(pdb, seqsForPdb, chains, ap);
  }

  public ChimeraXViewFrame(PDBEntry[] pdbsForFile, boolean superposeAdded,
          SequenceI[][] theSeqs, AlignmentPanel ap)
  {
    super(pdbsForFile, superposeAdded, theSeqs, ap);
  }

  /**
   * Constructor given a session file to be loaded
   * 
   * @param viewerData
   * @param alignPanel
   * @param sessionFile
   * @param vid
   */
  public ChimeraXViewFrame(StructureViewerModel viewerData,
          AlignmentPanel alignPanel, String sessionFile, String vid)
  {
    super(viewerData, alignPanel, sessionFile, vid);
  }

  @Override
  public ViewerType getViewerType()
  {
    return ViewerType.CHIMERAX;
  }

  @Override
  protected String getViewerName()
  {
    return "ChimeraX";
  }

  @Override
  protected JalviewChimeraBindingModel newBindingModel(AlignmentPanel ap,
          PDBEntry[] pdbentrys, SequenceI[][] seqs)
  {
    return new JalviewChimeraXBindingModel(this,
            ap.getStructureSelectionManager(), pdbentrys, seqs, null);
  }
}
