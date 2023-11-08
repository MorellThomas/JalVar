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

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import jalview.api.AlignmentViewPanel;
import jalview.api.structures.JalviewStructureDisplayI;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.ext.rbvi.chimera.JalviewChimeraBinding;
import jalview.io.DataSourceType;
import jalview.structure.StructureSelectionManager;

public class JalviewChimeraBindingModel extends JalviewChimeraBinding
{
  public JalviewChimeraBindingModel(ChimeraViewFrame chimeraViewFrame,
          StructureSelectionManager ssm, PDBEntry[] pdbentry,
          SequenceI[][] sequenceIs, DataSourceType protocol)
  {
    super(ssm, pdbentry, sequenceIs, protocol);
    setViewer(chimeraViewFrame);
  }

  @Override
  public jalview.api.SequenceRenderer getSequenceRenderer(
          AlignmentViewPanel alignment)
  {
    return new SequenceRenderer(((AlignmentPanel) alignment).av);
  }

  @Override
  public void refreshGUI()
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        JalviewStructureDisplayI theViewer = getViewer();
        theViewer.updateTitleAndMenus();
        ((JComponent) theViewer).revalidate();
      }
    });
  }
}
