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

import java.awt.Container;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import org.jmol.api.JmolAppConsoleInterface;
import org.openscience.jmol.app.jmolpanel.console.AppConsole;

import jalview.api.AlignmentViewPanel;
import jalview.api.structures.JalviewStructureDisplayI;
import jalview.bin.Console;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.ext.jmol.JalviewJmolBinding;
import jalview.io.DataSourceType;
import jalview.structure.StructureSelectionManager;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.ws.dbsources.EBIAlfaFold;
import jalview.ws.dbsources.Pdb;
import jalview.ws.utils.UrlDownloadClient;
import javajs.util.BS;

public class AppJmolBinding extends JalviewJmolBinding
{
  public AppJmolBinding(AppJmol appJmol, StructureSelectionManager sSm,
          PDBEntry[] pdbentry, SequenceI[][] sequenceIs,
          DataSourceType protocol)
  {
    super(sSm, pdbentry, sequenceIs, protocol);
    setViewer(appJmol);
  }

  @Override
  public SequenceRenderer getSequenceRenderer(AlignmentViewPanel alignment)
  {
    return new SequenceRenderer(((AlignmentPanel) alignment).av);
  }

  @Override
  public void sendConsoleEcho(String strEcho)
  {
    if (console != null)
    {
      console.sendConsoleEcho(strEcho);
    }
  }

  @Override
  public void sendConsoleMessage(String strStatus)
  {
    if (console != null && strStatus != null)
    // && !strStatus.equals("Script completed"))
    // should we squash the script completed string ?
    {
      console.sendConsoleMessage(strStatus);
    }
  }

  @Override
  public void showUrl(String url, String target)
  {
    try
    {
      jalview.util.BrowserLauncher.openURL(url);
    } catch (Exception e)
    {
      Console.error("Failed to launch Jmol-associated url " + url, e);
      // TODO: 2.6 : warn user if browser was not configured.
    }
  }

  @Override
  public void refreshGUI()
  {
    if (getMappedStructureCount() == 0)
    {
      // too soon!
      return;
    }
    // appJmolWindow.repaint();
    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        JalviewStructureDisplayI theViewer = getViewer();
        // invokes colourbySequence() via seqColour_ActionPerformed()
        theViewer.updateTitleAndMenus();
        ((JComponent) theViewer).revalidate();
      }
    });
  }

  @Override
  public void notifyScriptTermination(String strStatus, int msWalltime)
  {
    // todo - script termination doesn't happen ?
    // if (console != null)
    // console.notifyScriptTermination(strStatus,
    // msWalltime);
  }

  @Override
  public void showUrl(String url)
  {
    showUrl(url, "jmol");
  }

  public void newJmolPopup(String menuName)
  {
    // jmolpopup = new JmolAwtPopup();
    // jmolpopup.jpiInitialize((viewer), menuName);
  }

  @Override
  public void selectionChanged(BS arg0)
  {
  }

  @Override
  public void showConsole(boolean b)
  {
    getViewer().showConsole(b);
  }

  @Override
  protected JmolAppConsoleInterface createJmolConsole(
          Container consolePanel, String buttonsToShow)
  {
    jmolViewer.setJmolCallbackListener(this);
    // BH comment: can't do this yet [for JS only, or generally?]
    return Platform.isJS() ? null
            : new AppConsole(jmolViewer, consolePanel, buttonsToShow);
  }

  @Override
  protected void releaseUIResources()
  {
    setViewer(null);
    closeConsole();
  }

  @Override
  public void releaseReferences(Object svl)
  {
    if (svl instanceof SeqPanel)
    {
      getViewer().removeAlignmentPanel(((SeqPanel) svl).ap);
    }
  }

  @Override
  public Map<String, Object> getJSpecViewProperty(String arg0)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @SuppressWarnings("unused")
  public void cacheFiles(List<File> files)
  {
    if (files == null)
    {
      return;
    }
    for (File f : files)
    {
      Platform.cacheFileData(f);
    }
  }

  /**
   * Retrieves and saves as file any modelled PDB entries for which we do not
   * already have a file saved. Returns a list of absolute paths to structure
   * files which were either retrieved, or already stored but not modelled in
   * the structure viewer (i.e. files to add to the viewer display).
   * 
   * Currently only used by Jmol - similar but different code used for Chimera/X
   * and Pymol so still need to refactor
   * 
   * @param structureViewer
   *          UI proxy for the structure viewer
   * @return list of absolute paths to structures retrieved that need to be
   *         added to the display
   */
  public List<String> fetchPdbFiles(StructureViewerBase structureViewer)
  {
    // todo - record which pdbids were successfully imported.
    StringBuilder errormsgs = new StringBuilder();

    List<String> files = new ArrayList<>();
    String pdbid = "";
    try
    {
      String[] filesInViewer = getStructureFiles();
      // TODO: replace with reference fetching/transfer code (validate PDBentry
      // as a DBRef?)

      for (int pi = 0; pi < getPdbCount(); pi++)
      {
        PDBEntry strucEntry = getPdbEntry(pi);

        String file = strucEntry.getFile();
        if (file == null)
        {
          pdbid = strucEntry.getId();
          try
          {
            file = structureViewer.fetchPdbFile(strucEntry);
          } catch (OutOfMemoryError oomerror)
          {
            new OOMWarning("Retrieving PDB id " + pdbid, oomerror);
          } catch (Exception ex)
          {
            ex.printStackTrace();
            errormsgs.append("'").append(pdbid).append("'");
          }
          if (file != null)
          {
            // success
            files.add(file);
          }
          else
          {
            errormsgs.append("'").append(pdbid).append("' ");
          }
        }
        else
        {
          if (filesInViewer != null && filesInViewer.length > 0)
          {
            structureViewer.setAddingStructures(true); // already files loaded.
            for (int c = 0; c < filesInViewer.length; c++)
            {
              if (Platform.pathEquals(filesInViewer[c], file))
              {
                file = null;
                break;
              }
            }
          }
          if (file != null)
          {
            files.add(file);
          }
        }
      }
    } catch (OutOfMemoryError oomerror)
    {
      new OOMWarning("Retrieving PDB files: " + pdbid, oomerror);
    } catch (Exception ex)
    {
      ex.printStackTrace();
      errormsgs.append("When retrieving pdbfiles : current was: '")
              .append(pdbid).append("'");
    }
    if (errormsgs.length() > 0)
    {
      JvOptionPane.showInternalMessageDialog(Desktop.desktop,
              MessageManager.formatMessage(
                      "label.pdb_entries_couldnt_be_retrieved", new String[]
                      { errormsgs.toString() }),
              MessageManager.getString("label.couldnt_load_file"),
              JvOptionPane.ERROR_MESSAGE);
    }
    return files;
  }

}
