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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import jalview.api.AlignmentViewPanel;
import jalview.api.FeatureRenderer;
import jalview.bin.Console;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.datamodel.StructureViewerModel;
import jalview.datamodel.StructureViewerModel.StructureData;
import jalview.gui.StructureViewer.ViewerType;
import jalview.io.DataSourceType;
import jalview.io.StructureFile;
import jalview.structures.models.AAStructureBindingModel;
import jalview.util.MessageManager;

public class PymolViewer extends StructureViewerBase
{
  private static final int myWidth = 500;

  private static final int myHeight = 150;

  private PymolBindingModel binding;

  private String pymolSessionFile;

  public PymolViewer()
  {
    super();

    /*
     * closeViewer will decide whether or not to close this frame
     * depending on whether user chooses to Cancel or not
     */
    setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
  }

  public PymolViewer(PDBEntry pdb, SequenceI[] seqs, Object object,
          AlignmentPanel ap)
  {
    this();
    openNewPymol(ap, new PDBEntry[] { pdb }, new SequenceI[][] { seqs });
  }

  public PymolViewer(PDBEntry[] pe, boolean alignAdded, SequenceI[][] seqs,
          AlignmentPanel ap)
  {
    this();
    setAlignAddedStructures(alignAdded);
    openNewPymol(ap, pe, seqs);
  }

  /**
   * Constructor given a session file to be restored
   * 
   * @param sessionFile
   * @param alignPanel
   * @param pdbArray
   * @param seqsArray
   * @param colourByPymol
   * @param colourBySequence
   * @param newViewId
   */
  public PymolViewer(StructureViewerModel viewerModel,
          AlignmentPanel alignPanel, String sessionFile, String vid)
  {
    // TODO convert to base/factory class method
    this();
    setViewId(vid);
    this.pymolSessionFile = sessionFile;
    Map<File, StructureData> pdbData = viewerModel.getFileData();
    PDBEntry[] pdbArray = new PDBEntry[pdbData.size()];
    SequenceI[][] seqsArray = new SequenceI[pdbData.size()][];
    int i = 0;
    for (StructureData data : pdbData.values())
    {
      PDBEntry pdbentry = new PDBEntry(data.getPdbId(), null,
              PDBEntry.Type.PDB, data.getFilePath());
      pdbArray[i] = pdbentry;
      List<SequenceI> sequencesForPdb = data.getSeqList();
      seqsArray[i] = sequencesForPdb
              .toArray(new SequenceI[sequencesForPdb.size()]);
      i++;
    }

    openNewPymol(alignPanel, pdbArray, seqsArray);
    if (viewerModel.isColourByViewer())
    {
      binding.setColourBySequence(false);
      seqColour.setSelected(false);
      viewerColour.setSelected(true);
    }
    else if (viewerModel.isColourWithAlignPanel())
    {
      binding.setColourBySequence(true);
      seqColour.setSelected(true);
      viewerColour.setSelected(false);
    }
  }

  private void openNewPymol(AlignmentPanel ap, PDBEntry[] pe,
          SequenceI[][] seqs)
  {
    createProgressBar();
    binding = new PymolBindingModel(this, ap.getStructureSelectionManager(),
            pe, seqs);
    addAlignmentPanel(ap);
    useAlignmentPanelForColourbyseq(ap);

    if (pe.length > 1)
    {
      useAlignmentPanelForSuperposition(ap);
    }
    binding.setColourBySequence(true);
    setSize(myWidth, myHeight);
    initMenus();
    viewerActionMenu.setText("PyMOL");
    updateTitleAndMenus();

    addingStructures = false;
    worker = new Thread(this);
    worker.start();

    this.addInternalFrameListener(new InternalFrameAdapter()
    {
      @Override
      public void internalFrameClosing(
              InternalFrameEvent internalFrameEvent)
      {
        closeViewer(false);
      }
    });

  }

  /**
   * Create a helper to manage progress bar display
   */
  protected void createProgressBar()
  {
    if (getProgressIndicator() == null)
    {
      setProgressIndicator(new ProgressBar(statusPanel, statusBar));
    }
  }

  @Override
  public void run()
  {
    // todo pull up much of this

    StringBuilder errormsgs = new StringBuilder(128);
    List<PDBEntry> filePDB = new ArrayList<>();
    List<Integer> filePDBpos = new ArrayList<>();
    String[] curfiles = binding.getStructureFiles(); // files currently in
                                                     // viewer
    for (int pi = 0; pi < binding.getPdbCount(); pi++)
    {
      String file = null;
      PDBEntry thePdbEntry = binding.getPdbEntry(pi);
      if (thePdbEntry.getFile() == null)
      {
        /*
         * Retrieve PDB data, save to file, attach to PDBEntry
         */
        file = fetchPdbFile(thePdbEntry);
        if (file == null)
        {
          errormsgs.append("'" + thePdbEntry.getId() + "' ");
        }
      }
      else
      {
        /*
         * got file already
         */
        file = new File(thePdbEntry.getFile()).getAbsoluteFile().getPath();
        // todo - skip if already loaded in PyMOL
      }
      if (file != null)
      {
        filePDB.add(thePdbEntry);
        filePDBpos.add(Integer.valueOf(pi));
      }
    }

    if (!filePDB.isEmpty())
    {
      /*
       * at least one structure to add to viewer
       */
      binding.setFinishedInit(false);
      if (!addingStructures)
      {
        try
        {
          initPymol();
        } catch (Exception ex)
        {
          Console.error("Couldn't open PyMOL viewer!", ex);
          // if we couldn't open Pymol, no point continuing
          return;
        }
      }
      if (!binding.isViewerRunning())
      {
        // nothing to do
        // TODO: ensure we tidy up JAL-3619

        return;
      }

      int num = -1;
      for (PDBEntry pe : filePDB)
      {
        num++;
        if (pe.getFile() != null)
        {
          try
          {
            int pos = filePDBpos.get(num).intValue();
            long startTime = startProgressBar(getViewerName() + " "
                    + MessageManager.getString("status.opening_file_for")
                    + " " + pe.getId());
            binding.openFile(pe);
            binding.addSequence(pos, binding.getSequence()[pos]);
            File fl = new File(pe.getFile());
            DataSourceType protocol = DataSourceType.URL;
            try
            {
              if (fl.exists())
              {
                protocol = DataSourceType.FILE;
              }
            } catch (Throwable e)
            {
            } finally
            {
              stopProgressBar("", startTime);
            }

            StructureFile pdb = null;
            if (pe.hasStructureFile())
            {
              pdb = pe.getStructureFile();
              Console.debug("(Re)Using StructureFile " + pdb.getId());
            }
            else
            {
              pdb = binding.getSsm().setMapping(binding.getSequence()[pos],
                      binding.getChains()[pos], pe.getFile(), protocol,
                      getProgressIndicator());
            }
            binding.stashFoundChains(pdb, pe.getFile());
          } catch (Exception ex)
          {
            Console.error("Couldn't open " + pe.getFile() + " in "
                    + getViewerName() + "!", ex);
          } finally
          {
            // Cache.debug("File locations are " + files);
          }
        }
      }

      binding.refreshGUI();
      binding.setFinishedInit(true);
      binding.setLoadingFromArchive(false);

      /*
       * ensure that any newly discovered features (e.g. RESNUM)
       * are added to any open feature settings dialog
       */
      FeatureRenderer fr = getBinding().getFeatureRenderer(null);
      if (fr != null)
      {
        fr.featuresAdded();
      }

      // refresh the sequence colours for the new structure(s)
      for (AlignmentViewPanel ap : _colourwith)
      {
        binding.updateColours(ap);
      }
      // do superposition if asked to
      if (alignAddedStructures)
      {
        new Thread(new Runnable()
        {
          @Override
          public void run()
          {
            alignStructsWithAllAlignPanels();
          }
        }).start();
      }
      addingStructures = false;
    }
    _started = false;
    worker = null;

  }

  /**
   * Launch PyMOL. If we have a session file name, send PyMOL the command to
   * open its saved session file.
   */
  void initPymol()
  {
    Desktop.addInternalFrame(this,
            binding.getViewerTitle(getViewerName(), true),
            getBounds().width, getBounds().height);

    if (!binding.launchPymol())
    {
      JvOptionPane.showMessageDialog(Desktop.desktop,
              MessageManager.formatMessage("label.open_viewer_failed",
                      getViewerName()),
              MessageManager.getString("label.error_loading_file"),
              JvOptionPane.ERROR_MESSAGE);
      binding.closeViewer(true);
      this.dispose();
      return;
    }

    if (this.pymolSessionFile != null)
    {
      boolean opened = binding.openSession(pymolSessionFile);
      if (!opened)
      {
        Console.error("An error occurred opening PyMOL session file "
                + pymolSessionFile);
      }
    }
    // binding.startPymolListener();
  }

  @Override
  public AAStructureBindingModel getBinding()
  {
    return binding;
  }

  @Override
  public ViewerType getViewerType()
  {
    return ViewerType.PYMOL;
  }

  @Override
  protected String getViewerName()
  {
    return "PyMOL";
  }

  JMenuItem writeFeatures = null;

  @Override
  protected void initMenus()
  {
    super.initMenus();

    savemenu.setVisible(false); // not yet implemented
    viewMenu.add(fitToWindow);

    writeFeatures = new JMenuItem(
            MessageManager.getString("label.create_viewer_attributes"));
    writeFeatures.setToolTipText(
            MessageManager.getString("label.create_viewer_attributes_tip"));
    writeFeatures.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        sendFeaturesToPymol();
      }
    });
    viewerActionMenu.add(writeFeatures);
  }

  @Override
  protected void buildActionMenu()
  {
    super.buildActionMenu();
    viewerActionMenu.add(writeFeatures);
  }

  protected void sendFeaturesToPymol()
  {
    int count = binding.sendFeaturesToViewer(getAlignmentPanel());
    statusBar.setText(MessageManager.formatMessage("label.attributes_set",
            count, getViewerName()));
  }

}
