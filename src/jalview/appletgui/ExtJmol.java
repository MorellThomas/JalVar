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
package jalview.appletgui;

import jalview.api.AlignmentViewPanel;
import jalview.api.SequenceRenderer;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.ext.jmol.JalviewJmolBinding;
import jalview.io.DataSourceType;

import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.jmol.api.JmolAppConsoleInterface;
import javajs.util.BS;
import org.jmol.viewer.Viewer;

/**
 * bind an alignment view to an external Jmol instance.
 * 
 * @author JimP
 * 
 */
public class ExtJmol extends JalviewJmolBinding
{

  private AlignmentPanel ap;

  protected ExtJmol(AlignFrame alframe, PDBEntry[] pdbentry,
          SequenceI[][] seq, DataSourceType protocol)
  {
    super(alframe.alignPanel.getStructureSelectionManager(), pdbentry, seq,
            protocol);
  }

  public ExtJmol(Viewer viewer, AlignmentPanel alignPanel,
          SequenceI[][] seqs)
  {
    super(alignPanel.getStructureSelectionManager(), seqs, viewer);
    ap = alignPanel;
    notifyFileLoaded(null, null, null, null, 0);
  }

  @Override
  public void updateColours(Object source)
  {
  }

  @Override
  public void showUrl(String arg0)
  {
    showUrl(arg0, "jmol");
  }

  @Override
  public SequenceRenderer getSequenceRenderer(AlignmentViewPanel alignment)
  {
    return ((AlignmentPanel) alignment).getSequenceRenderer();
  }

  @Override
  public void notifyScriptTermination(String strStatus, int msWalltime)
  {
    // ignore
  }

  @Override
  public void sendConsoleEcho(String strEcho)
  {
    // ignore
  }

  @Override
  public void sendConsoleMessage(String strStatus)
  {
    // ignore
  }

  @Override
  public void showUrl(String url, String target)
  {
    ap.alignFrame.showURL(url, target);
  }

  @Override
  public void refreshGUI()
  {
    // ignore
  }

  @Override
  public void selectionChanged(BS arg0)
  {
    System.out.println(arg0);
  }

  @Override
  public void refreshPdbEntries()
  {
    List<PDBEntry> pdbe = new ArrayList<>();
    List<String> fileids = new ArrayList<>();
    SequenceI[] sq = ap.av.getAlignment().getSequencesArray();
    for (int s = 0; s < sq.length; s++)
    {
      Vector<PDBEntry> pdbids = sq[s].getAllPDBEntries();
      if (pdbids != null)
      {
        for (int pe = 0, peSize = pdbids.size(); pe < peSize; pe++)
        {
          PDBEntry pentry = pdbids.elementAt(pe);
          if (!fileids.contains(pentry.getId()))
          {
            pdbe.add(pentry);
          }
          else
          {
            fileids.add(pentry.getId());
          }
        }
      }
    }
    PDBEntry[] newEntries = new PDBEntry[pdbe.size()];
    for (int pe = 0; pe < pdbe.size(); pe++)
    {
      newEntries[pe] = pdbe.get(pe);
    }
    setPdbentry(newEntries);
  }

  @Override
  public void showConsole(boolean show)
  {
    // This never gets called because we haven't overriden the associated Jmol's
    // console
    System.err.println(
            "WARNING: unexpected call to ExtJmol's showConsole method. (showConsole="
                    + show);
  }

  @Override
  protected JmolAppConsoleInterface createJmolConsole(
          Container consolePanel, String buttonsToShow)
  {
    return null;
  }

  @Override
  protected void releaseUIResources()
  {
    ap = null;
    closeConsole();

  }

  @Override
  public Map<String, Object> getJSpecViewProperty(String arg0)
  {
    return null;
  }
}
