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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JInternalFrame;
import javax.swing.JMenu;
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
import jalview.ext.rbvi.chimera.JalviewChimeraBinding;
import jalview.gui.StructureViewer.ViewerType;
import jalview.io.DataSourceType;
import jalview.io.StructureFile;
import jalview.structures.models.AAStructureBindingModel;
import jalview.util.ImageMaker.TYPE;
import jalview.util.MessageManager;
import jalview.util.Platform;

/**
 * GUI elements for handling an external chimera display
 * 
 * @author jprocter
 *
 */
public class ChimeraViewFrame extends StructureViewerBase
{
  private JalviewChimeraBinding jmb;

  /*
   * Path to Chimera session file. This is set when an open Jalview/Chimera
   * session is saved, or on restore from a Jalview project (if it holds the
   * filename of any saved Chimera sessions).
   */
  private String chimeraSessionFile = null;

  private int myWidth = 500;

  private int myHeight = 150;

  private JMenuItem writeFeatures = null;

  private JMenu fetchAttributes = null;

  /**
   * Initialise menu options.
   */
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
        sendFeaturesToChimera();
      }
    });
    viewerActionMenu.add(writeFeatures);

    fetchAttributes = new JMenu(MessageManager.formatMessage(
            "label.fetch_viewer_attributes", getViewerName()));
    fetchAttributes.setToolTipText(MessageManager.formatMessage(
            "label.fetch_viewer_attributes_tip", getViewerName()));
    fetchAttributes.addMouseListener(new MouseAdapter()
    {

      @Override
      public void mouseEntered(MouseEvent e)
      {
        buildAttributesMenu(fetchAttributes);
      }
    });
    viewerActionMenu.add(fetchAttributes);
  }

  @Override
  protected void buildActionMenu()
  {
    super.buildActionMenu();
    // add these back in after menu is refreshed
    viewerActionMenu.add(writeFeatures);
    viewerActionMenu.add(fetchAttributes);

  };

  /**
   * Query the structure viewer for its residue attribute names and add them as
   * items off the attributes menu
   * 
   * @param attributesMenu
   */
  protected void buildAttributesMenu(JMenu attributesMenu)
  {
    List<String> atts = jmb.getChimeraAttributes();
    attributesMenu.removeAll();
    Collections.sort(atts);
    for (String attName : atts)
    {
      JMenuItem menuItem = new JMenuItem(attName);
      menuItem.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          if (getBinding().copyStructureAttributesToFeatures(attName,
                  getAlignmentPanel()) > 0)
          {
            getAlignmentPanel().getFeatureRenderer().featuresAdded();
          }
        }
      });
      attributesMenu.add(menuItem);
    }
  }

  /**
   * Sends command(s) to the structure viewer to create residue attributes for
   * visible Jalview features
   */
  protected void sendFeaturesToChimera()
  {
    // todo pull up?
    int count = jmb.sendFeaturesToViewer(getAlignmentPanel());
    statusBar.setText(MessageManager.formatMessage("label.attributes_set",
            count, getViewerName()));
  }

  /**
   * open a single PDB structure in a new Chimera view
   * 
   * @param pdbentry
   * @param seq
   * @param chains
   * @param ap
   */
  public ChimeraViewFrame(PDBEntry pdbentry, SequenceI[] seq,
          String[] chains, final AlignmentPanel ap)
  {
    this();

    openNewChimera(ap, new PDBEntry[] { pdbentry },
            new SequenceI[][]
            { seq });
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

  private void openNewChimera(AlignmentPanel ap, PDBEntry[] pdbentrys,
          SequenceI[][] seqs)
  {
    createProgressBar();
    jmb = newBindingModel(ap, pdbentrys, seqs);
    addAlignmentPanel(ap);
    useAlignmentPanelForColourbyseq(ap);

    if (pdbentrys.length > 1)
    {
      useAlignmentPanelForSuperposition(ap);
    }
    jmb.setColourBySequence(true);
    setSize(myWidth, myHeight);
    initMenus();

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

  protected JalviewChimeraBindingModel newBindingModel(AlignmentPanel ap,
          PDBEntry[] pdbentrys, SequenceI[][] seqs)
  {
    return new JalviewChimeraBindingModel(this,
            ap.getStructureSelectionManager(), pdbentrys, seqs, null);
  }

  /**
   * Create a new viewer from saved session state data including Chimera session
   * file
   * 
   * @param chimeraSessionFile
   * @param alignPanel
   * @param pdbArray
   * @param seqsArray
   * @param colourByChimera
   * @param colourBySequence
   * @param newViewId
   */
  public ChimeraViewFrame(StructureViewerModel viewerData,
          AlignmentPanel alignPanel, String sessionFile, String vid)
  {
    this();
    setViewId(vid);
    this.chimeraSessionFile = sessionFile;
    Map<File, StructureData> pdbData = viewerData.getFileData();
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
    openNewChimera(alignPanel, pdbArray, seqsArray);
    if (viewerData.isColourByViewer())
    {
      jmb.setColourBySequence(false);
      seqColour.setSelected(false);
      viewerColour.setSelected(true);
    }
    else if (viewerData.isColourWithAlignPanel())
    {
      jmb.setColourBySequence(true);
      seqColour.setSelected(true);
      viewerColour.setSelected(false);
    }
  }

  /**
   * create a new viewer containing several structures, optionally superimposed
   * using the given alignPanel.
   * 
   * @param pe
   * @param seqs
   * @param ap
   */
  public ChimeraViewFrame(PDBEntry[] pe, boolean alignAdded,
          SequenceI[][] seqs, AlignmentPanel ap)
  {
    this();
    setAlignAddedStructures(alignAdded);
    openNewChimera(ap, pe, seqs);
  }

  /**
   * Default constructor
   */
  public ChimeraViewFrame()
  {
    super();

    /*
     * closeViewer will decide whether or not to close this frame
     * depending on whether user chooses to Cancel or not
     */
    setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
  }

  /**
   * Launch Chimera. If we have a chimera session file name, send Chimera the
   * command to open its saved session file.
   */
  void initChimera()
  {
    jmb.setFinishedInit(false);
    Desktop.addInternalFrame(this,
            jmb.getViewerTitle(getViewerName(), true), getBounds().width,
            getBounds().height);

    if (!jmb.launchChimera())
    {
      JvOptionPane.showMessageDialog(Desktop.desktop,
              MessageManager.formatMessage("label.open_viewer_failed",
                      getViewerName()),
              MessageManager.getString("label.error_loading_file"),
              JvOptionPane.ERROR_MESSAGE);
      jmb.closeViewer(true);
      this.dispose();
      return;
    }

    if (this.chimeraSessionFile != null)
    {
      boolean opened = jmb.openSession(chimeraSessionFile);
      if (!opened)
      {
        System.err.println("An error occurred opening Chimera session file "
                + chimeraSessionFile);
      }
    }

    jmb.startChimeraListener();
  }

  /**
   * Open any newly added PDB structures in Chimera, having first fetched data
   * from PDB (if not already saved).
   */
  @Override
  public void run()
  {
    _started = true;
    // todo - record which pdbids were successfully imported.
    StringBuilder errormsgs = new StringBuilder(128);
    StringBuilder files = new StringBuilder(128);
    List<PDBEntry> filePDB = new ArrayList<>();
    List<Integer> filePDBpos = new ArrayList<>();
    PDBEntry thePdbEntry = null;
    StructureFile pdb = null;
    try
    {
      String[] curfiles = jmb.getStructureFiles(); // files currently in viewer
      // TODO: replace with reference fetching/transfer code (validate PDBentry
      // as a DBRef?)
      for (int pi = 0; pi < jmb.getPdbCount(); pi++)
      {
        String file = null;
        thePdbEntry = jmb.getPdbEntry(pi);
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
           * Got file already - ignore if already loaded in Chimera.
           */
          file = new File(thePdbEntry.getFile()).getAbsoluteFile()
                  .getPath();
          if (curfiles != null && curfiles.length > 0)
          {
            addingStructures = true; // already files loaded.
            for (int c = 0; c < curfiles.length; c++)
            {
              if (curfiles[c].equals(file))
              {
                file = null;
                break;
              }
            }
          }
        }
        if (file != null)
        {
          filePDB.add(thePdbEntry);
          filePDBpos.add(Integer.valueOf(pi));
          files.append(" \"" + Platform.escapeBackslashes(file) + "\"");
        }
      }
    } catch (OutOfMemoryError oomerror)
    {
      new OOMWarning("Retrieving PDB files: " + thePdbEntry.getId(),
              oomerror);
    } catch (Exception ex)
    {
      ex.printStackTrace();
      errormsgs.append(
              "When retrieving pdbfiles for '" + thePdbEntry.getId() + "'");
    }
    if (errormsgs.length() > 0)
    {

      JvOptionPane.showInternalMessageDialog(Desktop.desktop,
              MessageManager.formatMessage(
                      "label.pdb_entries_couldnt_be_retrieved", new Object[]
                      { errormsgs.toString() }),
              MessageManager.getString("label.couldnt_load_file"),
              JvOptionPane.ERROR_MESSAGE);
    }

    if (files.length() > 0)
    {
      jmb.setFinishedInit(false);
      if (!addingStructures)
      {
        try
        {
          initChimera();
        } catch (Exception ex)
        {
          Console.error("Couldn't open Chimera viewer!", ex);
        }
      }
      if (!jmb.isViewerRunning())
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
            jmb.openFile(pe);
            jmb.addSequence(pos, jmb.getSequence()[pos]);
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
            // Explicitly map to the filename used by Chimera ;

            pdb = jmb.getSsm().setMapping(jmb.getSequence()[pos],
                    jmb.getChains()[pos], pe.getFile(), protocol,
                    getProgressIndicator());
            jmb.stashFoundChains(pdb, pe.getFile());

          } catch (OutOfMemoryError oomerror)
          {
            new OOMWarning(
                    "When trying to open and map structures from Chimera!",
                    oomerror);
          } catch (Exception ex)
          {
            Console.error(
                    "Couldn't open " + pe.getFile() + " in Chimera viewer!",
                    ex);
          } finally
          {
            Console.debug("File locations are " + files);
          }
        }
      }

      jmb.refreshGUI();
      jmb.setFinishedInit(true);
      jmb.setLoadingFromArchive(false);

      /*
       * ensure that any newly discovered features (e.g. RESNUM)
       * are notified to the FeatureRenderer (and added to any 
       * open feature settings dialog)
       */
      FeatureRenderer fr = getBinding().getFeatureRenderer(null);
      if (fr != null)
      {
        fr.featuresAdded();
      }

      // refresh the sequence colours for the new structure(s)
      for (AlignmentViewPanel ap : _colourwith)
      {
        jmb.updateColours(ap);
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

  @Override
  public void makePDBImage(TYPE imageType)
  {
    throw new UnsupportedOperationException(
            "Image export for Chimera is not implemented");
  }

  @Override
  public AAStructureBindingModel getBinding()
  {
    return jmb;
  }

  @Override
  public ViewerType getViewerType()
  {
    return ViewerType.CHIMERA;
  }

  @Override
  protected String getViewerName()
  {
    return "Chimera";
  }
}
