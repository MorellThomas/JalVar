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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import jalview.api.AlignmentViewPanel;
import jalview.bin.Console;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.datamodel.StructureViewerModel;
import jalview.datamodel.StructureViewerModel.StructureData;
import jalview.gui.ImageExporter.ImageWriterI;
import jalview.gui.StructureViewer.ViewerType;
import jalview.io.exceptions.ImageOutputException;
import jalview.structure.StructureCommand;
import jalview.structures.models.AAStructureBindingModel;
import jalview.util.BrowserLauncher;
import jalview.util.ImageMaker;
import jalview.util.ImageMaker.TYPE;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.util.imagemaker.BitmapImageSizing;

public class AppJmol extends StructureViewerBase
{
  // ms to wait for Jmol to load files
  private static final int JMOL_LOAD_TIMEOUT = 20000;

  private static final String SPACE = " ";

  private static final String QUOTE = "\"";

  AppJmolBinding jmb;

  JPanel scriptWindow;

  JSplitPane splitPane;

  RenderPanel renderPanel;

  /**
   * 
   * @param files
   * @param ids
   * @param seqs
   * @param ap
   * @param usetoColour
   *          - add the alignment panel to the list used for colouring these
   *          structures
   * @param useToAlign
   *          - add the alignment panel to the list used for aligning these
   *          structures
   * @param leaveColouringToJmol
   *          - do not update the colours from any other source. Jmol is
   *          handling them
   * @param loadStatus
   * @param bounds
   * @param viewid
   */
  public AppJmol(StructureViewerModel viewerModel, AlignmentPanel ap,
          String sessionFile, String viewid)
  {
    Map<File, StructureData> pdbData = viewerModel.getFileData();
    PDBEntry[] pdbentrys = new PDBEntry[pdbData.size()];
    SequenceI[][] seqs = new SequenceI[pdbData.size()][];
    int i = 0;
    for (StructureData data : pdbData.values())
    {
      PDBEntry pdbentry = new PDBEntry(data.getPdbId(), null,
              PDBEntry.Type.PDB, data.getFilePath());
      pdbentrys[i] = pdbentry;
      List<SequenceI> sequencesForPdb = data.getSeqList();
      seqs[i] = sequencesForPdb
              .toArray(new SequenceI[sequencesForPdb.size()]);
      i++;
    }

    // TODO: check if protocol is needed to be set, and if chains are
    // autodiscovered.
    jmb = new AppJmolBinding(this, ap.getStructureSelectionManager(),
            pdbentrys, seqs, null);

    jmb.setLoadingFromArchive(true);
    addAlignmentPanel(ap);
    if (viewerModel.isAlignWithPanel())
    {
      useAlignmentPanelForSuperposition(ap);
    }
    initMenus();
    boolean useToColour = viewerModel.isColourWithAlignPanel();
    boolean leaveColouringToJmol = viewerModel.isColourByViewer();
    if (leaveColouringToJmol || !useToColour)
    {
      jmb.setColourBySequence(false);
      seqColour.setSelected(false);
      viewerColour.setSelected(true);
    }
    else if (useToColour)
    {
      useAlignmentPanelForColourbyseq(ap);
      jmb.setColourBySequence(true);
      seqColour.setSelected(true);
      viewerColour.setSelected(false);
    }

    this.setBounds(viewerModel.getX(), viewerModel.getY(),
            viewerModel.getWidth(), viewerModel.getHeight());
    setViewId(viewid);

    this.addInternalFrameListener(new InternalFrameAdapter()
    {
      @Override
      public void internalFrameClosing(
              InternalFrameEvent internalFrameEvent)
      {
        closeViewer(false);
      }
    });
    StringBuilder cmd = new StringBuilder();
    cmd.append("load FILES ").append(QUOTE)
            .append(Platform.escapeBackslashes(sessionFile)).append(QUOTE);
    initJmol(cmd.toString());
  }

  @Override
  protected void initMenus()
  {
    super.initMenus();

    viewerColour
            .setText(MessageManager.getString("label.colour_with_jmol"));
    viewerColour.setToolTipText(MessageManager
            .getString("label.let_jmol_manage_structure_colours"));
  }

  /**
   * display a single PDB structure in a new Jmol view
   * 
   * @param pdbentry
   * @param seq
   * @param chains
   * @param ap
   */
  public AppJmol(PDBEntry pdbentry, SequenceI[] seq, String[] chains,
          final AlignmentPanel ap)
  {
    setProgressIndicator(ap.alignFrame);

    openNewJmol(ap, alignAddedStructures, new PDBEntry[] { pdbentry },
            new SequenceI[][]
            { seq });
  }

  private void openNewJmol(AlignmentPanel ap, boolean alignAdded,
          PDBEntry[] pdbentrys, SequenceI[][] seqs)
  {
    setProgressIndicator(ap.alignFrame);
    jmb = new AppJmolBinding(this, ap.getStructureSelectionManager(),
            pdbentrys, seqs, null);
    addAlignmentPanel(ap);
    useAlignmentPanelForColourbyseq(ap);

    alignAddedStructures = alignAdded;
    if (pdbentrys.length > 1)
    {
      useAlignmentPanelForSuperposition(ap);
    }

    jmb.setColourBySequence(true);
    setSize(400, 400); // probably should be a configurable/dynamic default here
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

  /**
   * create a new Jmol containing several structures optionally superimposed
   * using the given alignPanel.
   * 
   * @param ap
   * @param alignAdded
   *          - true to superimpose
   * @param pe
   * @param seqs
   */
  public AppJmol(AlignmentPanel ap, boolean alignAdded, PDBEntry[] pe,
          SequenceI[][] seqs)
  {
    openNewJmol(ap, alignAdded, pe, seqs);
  }

  void initJmol(String command)
  {
    jmb.setFinishedInit(false);
    renderPanel = new RenderPanel();
    // TODO: consider waiting until the structure/view is fully loaded before
    // displaying
    this.getContentPane().add(renderPanel, java.awt.BorderLayout.CENTER);
    jalview.gui.Desktop.addInternalFrame(this, jmb.getViewerTitle(),
            getBounds().width, getBounds().height);
    if (scriptWindow == null)
    {
      BorderLayout bl = new BorderLayout();
      bl.setHgap(0);
      bl.setVgap(0);
      scriptWindow = new JPanel(bl);
      scriptWindow.setVisible(false);
    }

    jmb.allocateViewer(renderPanel, true, "", null, null, "", scriptWindow,
            null);
    // jmb.newJmolPopup("Jmol");
    if (command == null)
    {
      command = "";
    }
    jmb.executeCommand(new StructureCommand(command), false);
    jmb.executeCommand(new StructureCommand("set hoverDelay=0.1"), false);
    jmb.setFinishedInit(true);
  }

  @Override
  public void run()
  {
    _started = true;
    try
    {
      List<String> files = jmb.fetchPdbFiles(this);
      if (files.size() > 0)
      {
        showFilesInViewer(files);
      }
    } finally
    {
      _started = false;
      worker = null;
    }
  }

  /**
   * Either adds the given files to a structure viewer or opens a new viewer to
   * show them
   * 
   * @param files
   *          list of absolute paths to structure files
   */
  void showFilesInViewer(List<String> files)
  {
    long lastnotify = jmb.getLoadNotifiesHandled();
    StringBuilder fileList = new StringBuilder();
    for (String s : files)
    {
      fileList.append(SPACE).append(QUOTE)
              .append(Platform.escapeBackslashes(s)).append(QUOTE);
    }
    String filesString = fileList.toString();

    if (!addingStructures)
    {
      try
      {
        initJmol("load FILES " + filesString);
      } catch (OutOfMemoryError oomerror)
      {
        new OOMWarning("When trying to open the Jmol viewer!", oomerror);
        Console.debug("File locations are " + filesString);
      } catch (Exception ex)
      {
        Console.error("Couldn't open Jmol viewer!", ex);
        ex.printStackTrace();
        return;
      }
    }
    else
    {
      StringBuilder cmd = new StringBuilder();
      cmd.append("loadingJalviewdata=true\nload APPEND ");
      cmd.append(filesString);
      cmd.append("\nloadingJalviewdata=null");
      final StructureCommand command = new StructureCommand(cmd.toString());
      lastnotify = jmb.getLoadNotifiesHandled();

      try
      {
        jmb.executeCommand(command, false);
      } catch (OutOfMemoryError oomerror)
      {
        new OOMWarning("When trying to add structures to the Jmol viewer!",
                oomerror);
        Console.debug("File locations are " + filesString);
        return;
      } catch (Exception ex)
      {
        Console.error("Couldn't add files to Jmol viewer!", ex);
        ex.printStackTrace();
        return;
      }
    }

    // need to wait around until script has finished
    int waitMax = JMOL_LOAD_TIMEOUT;
    int waitFor = 35;
    int waitTotal = 0;
    while (addingStructures ? lastnotify >= jmb.getLoadNotifiesHandled()
            : !(jmb.isFinishedInit() && jmb.getStructureFiles() != null
                    && jmb.getStructureFiles().length == files.size()))
    {
      try
      {
        Console.debug("Waiting around for jmb notify.");
        waitTotal += waitFor;

        // Thread.sleep() throws an exception in JS
        Thread.sleep(waitFor);
      } catch (Exception e)
      {
      }
      if (waitTotal > waitMax)
      {
        System.err.println("Timed out waiting for Jmol to load files after "
                + waitTotal + "ms");
        // System.err.println("finished: " + jmb.isFinishedInit()
        // + "; loaded: " + Arrays.toString(jmb.getPdbFile())
        // + "; files: " + files.toString());
        jmb.getStructureFiles();
        break;
      }
    }

    // refresh the sequence colours for the new structure(s)
    for (AlignmentViewPanel ap : _colourwith)
    {
      jmb.updateColours(ap);
    }
    // do superposition if asked to
    if (alignAddedStructures)
    {
      alignAddedStructures();
    }
    addingStructures = false;
  }

  /**
   * Queues a thread to align structures with Jalview alignments
   */
  void alignAddedStructures()
  {
    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        if (jmb.jmolViewer.isScriptExecuting())
        {
          SwingUtilities.invokeLater(this);
          try
          {
            Thread.sleep(5);
          } catch (InterruptedException q)
          {
          }
          return;
        }
        else
        {
          alignStructsWithAllAlignPanels();
        }
      }
    });

  }

  /**
   * Outputs the Jmol viewer image as an image file, after prompting the user to
   * choose a file and (for EPS) choice of Text or Lineart character rendering
   * (unless a preference for this is set)
   * 
   * @param type
   */
  @Override
  public void makePDBImage(ImageMaker.TYPE type)
  {
    try {
    makePDBImage(null, type, null,
            BitmapImageSizing.nullBitmapImageSizing());
    } catch (ImageOutputException ioex) {
      Console.error("Unexpected error whilst writing "+type.toString(),ioex);
    }
  }

  public void makePDBImage(File file, ImageMaker.TYPE type, String renderer,
          BitmapImageSizing userBis) throws ImageOutputException
  {
    int width = getWidth();
    int height = getHeight();

    BitmapImageSizing bis = ImageMaker.getScaleWidthHeight(width, height,
            userBis);
    float usescale = bis.scale;
    int usewidth = bis.width;
    int useheight = bis.height;

    ImageWriterI writer = new ImageWriterI()
    {
      @Override
      public void exportImage(Graphics g) throws Exception
      {
        Graphics2D ig2 = (Graphics2D) g;
        ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        if (type == TYPE.PNG && usescale > 0.0f)
        {
          // for a scaled image, this scales down a bigger image to give the
          // right resolution
          if (usescale > 0.0f)
          {
            ig2.scale(1 / usescale, 1 / usescale);
          }
        }

        jmb.jmolViewer.antialiased = true;
        jmb.jmolViewer.requestRepaintAndWait("image export");
        jmb.jmolViewer.renderScreenImage(ig2, usewidth, useheight);
      }
    };
    String view = MessageManager.getString("action.view")
            .toLowerCase(Locale.ROOT);
    final ImageExporter exporter = new ImageExporter(writer,
            getProgressIndicator(), type, getTitle());
    
    final Throwable[] exceptions = new Throwable[1];
    exceptions[0] = null;
    final AppJmol us = this;
    try
    {
      Thread runner = Executors.defaultThreadFactory().newThread(new Runnable()
      {
        @Override
        public void run()
        {
          try
          {
            exporter.doExport(file, us, width, height, view, renderer,
                    userBis);
          } catch (Throwable t)
          {
            exceptions[0] = t;
          }
        }
      });
      runner.start();
      do { Thread.sleep(25); } while (runner.isAlive());
    } catch (Throwable e)
    {
      throw new ImageOutputException(
              "Unexpected error when generating image", e);
    }
    if (exceptions[0] != null)
    {
      if (exceptions[0] instanceof ImageOutputException)
      {
        throw ((ImageOutputException) exceptions[0]);
      }
      else
      {
        throw new ImageOutputException(
                "Unexpected error when generating image", exceptions[0]);
      }
    }
  }

  @Override
  public void showHelp_actionPerformed()
  {
    try
    {
      BrowserLauncher // BH 2018
              .openURL("http://wiki.jmol.org");// http://jmol.sourceforge.net/docs/JmolUserGuide/");
    } catch (Exception ex)
    {
      System.err.println("Show Jmol help failed with: " + ex.getMessage());
    }
  }

  @Override
  public void showConsole(boolean showConsole)
  {
    if (showConsole)
    {
      if (splitPane == null)
      {
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(renderPanel);
        splitPane.setBottomComponent(scriptWindow);
        this.getContentPane().add(splitPane, BorderLayout.CENTER);
        splitPane.setDividerLocation(getHeight() - 200);
        scriptWindow.setVisible(true);
        scriptWindow.validate();
        splitPane.validate();
      }

    }
    else
    {
      if (splitPane != null)
      {
        splitPane.setVisible(false);
      }

      splitPane = null;

      this.getContentPane().add(renderPanel, BorderLayout.CENTER);
    }

    validate();
  }

  class RenderPanel extends JPanel
  {
    final Dimension currentSize = new Dimension();

    @Override
    public void paintComponent(Graphics g)
    {
      getSize(currentSize);

      if (jmb != null && jmb.hasFileLoadingError())
      {
        g.setColor(Color.black);
        g.fillRect(0, 0, currentSize.width, currentSize.height);
        g.setColor(Color.white);
        g.setFont(new Font("Verdana", Font.BOLD, 14));
        g.drawString(MessageManager.getString("label.error_loading_file")
                + "...", 20, currentSize.height / 2);
        StringBuffer sb = new StringBuffer();
        int lines = 0;
        for (int e = 0; e < jmb.getPdbCount(); e++)
        {
          sb.append(jmb.getPdbEntry(e).getId());
          if (e < jmb.getPdbCount() - 1)
          {
            sb.append(",");
          }

          if (e == jmb.getPdbCount() - 1 || sb.length() > 20)
          {
            lines++;
            g.drawString(sb.toString(), 20, currentSize.height / 2
                    - lines * g.getFontMetrics().getHeight());
          }
        }
      }
      else if (jmb == null || jmb.jmolViewer == null
              || !jmb.isFinishedInit())
      {
        g.setColor(Color.black);
        g.fillRect(0, 0, currentSize.width, currentSize.height);
        g.setColor(Color.white);
        g.setFont(new Font("Verdana", Font.BOLD, 14));
        g.drawString(MessageManager.getString("label.retrieving_pdb_data"),
                20, currentSize.height / 2);
      }
      else
      {
        jmb.jmolViewer.renderScreenImage(g, currentSize.width,
                currentSize.height);
      }
    }
  }

  @Override
  public AAStructureBindingModel getBinding()
  {
    return this.jmb;
  }

  @Override
  public ViewerType getViewerType()
  {
    return ViewerType.JMOL;
  }

  @Override
  protected String getViewerName()
  {
    return "Jmol";
  }
}
