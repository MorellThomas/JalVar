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
package jalview.io;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.SwingUtilities;

import jalview.api.ComplexAlignFile;
import jalview.api.FeatureSettingsModelI;
import jalview.api.FeaturesDisplayedI;
import jalview.api.FeaturesSourceI;
import jalview.bin.Cache;
import jalview.bin.Jalview;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.AlignViewport;
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;
import jalview.gui.QuitHandler;
import jalview.json.binding.biojson.v1.ColourSchemeMapper;
import jalview.project.Jalview2XML;
import jalview.schemes.ColourSchemeI;
import jalview.structure.StructureSelectionManager;
import jalview.util.MessageManager;
import jalview.ws.utils.UrlDownloadClient;

public class FileLoader implements Runnable
{
  String file;

  DataSourceType protocol;

  FileFormatI format;

  AlignmentFileReaderI source = null; // alternative specification of where data
                                      // comes

  // from

  AlignViewport viewport;

  AlignFrame alignFrame;

  long loadtime;

  long memused;

  boolean raiseGUI = true;

  private File selectedFile;

  /**
   * default constructor always raised errors in GUI dialog boxes
   */
  public FileLoader()
  {
    this(true);
  }

  /**
   * construct a Fileloader that may raise errors non-interactively
   * 
   * @param raiseGUI
   *          true if errors are to be raised as GUI dialog boxes
   */
  public FileLoader(boolean raiseGUI)
  {
    this.raiseGUI = raiseGUI;
  }

  public void LoadFile(AlignViewport viewport, Object file,
          DataSourceType protocol, FileFormatI format)
  {
    LoadFile(viewport, file, protocol, format, true);
  }

  public void LoadFile(AlignViewport viewport, Object file,
          DataSourceType protocol, FileFormatI format, boolean async)
  {
    this.viewport = viewport;
    if (file instanceof File)
    {
      this.selectedFile = (File) file;
      file = selectedFile.getPath();
    }
    LoadFile(file.toString(), protocol, format, async);
  }

  public void LoadFile(String file, DataSourceType protocol,
          FileFormatI format)
  {
    LoadFile(file, protocol, format, true);
  }

  public void LoadFile(String file, DataSourceType protocol,
          FileFormatI format, boolean async)
  {
    this.file = file;
    this.protocol = protocol;
    this.format = format;

    if (async)
    {
      final Thread loader = new Thread(this);

      SwingUtilities.invokeLater(new Runnable()
      {
        @Override
        public void run()
        {
          loader.start();
        }
      });
    }
    else
    {
      this.run();
    }
  }

  /**
   * Load a (file, protocol) source of unknown type
   * 
   * @param file
   * @param protocol
   */
  public void LoadFile(String file, DataSourceType protocol)
  {
    LoadFile(file, protocol, null);
  }

  /**
   * Load alignment from (file, protocol) and wait till loaded
   * 
   * @param file
   * @param sourceType
   * @return alignFrame constructed from file contents
   */
  public AlignFrame LoadFileWaitTillLoaded(String file,
          DataSourceType sourceType)
  {
    return LoadFileWaitTillLoaded(file, sourceType, null);
  }

  /**
   * Load alignment from (file, protocol) of type format and wait till loaded
   * 
   * @param file
   * @param sourceType
   * @param format
   * @return alignFrame constructed from file contents
   */
  public AlignFrame LoadFileWaitTillLoaded(String file,
          DataSourceType sourceType, FileFormatI format)
  {
    this.file = file;
    this.protocol = sourceType;
    this.format = format;
    return _LoadFileWaitTillLoaded();
  }

  /**
   * Load alignment from (file, protocol) of type format and wait till loaded
   * 
   * @param file
   * @param sourceType
   * @param format
   * @return alignFrame constructed from file contents
   */
  public AlignFrame LoadFileWaitTillLoaded(File file,
          DataSourceType sourceType, FileFormatI format)
  {
    this.selectedFile = file;
    this.file = file.getPath();
    this.protocol = sourceType;
    this.format = format;
    return _LoadFileWaitTillLoaded();
  }

  /**
   * Load alignment from FileParse source of type format and wait till loaded
   * 
   * @param source
   * @param format
   * @return alignFrame constructed from file contents
   */
  public AlignFrame LoadFileWaitTillLoaded(AlignmentFileReaderI source,
          FileFormatI format)
  {
    this.source = source;

    file = source.getInFile();
    protocol = source.getDataSourceType();
    this.format = format;
    return _LoadFileWaitTillLoaded();
  }

  /**
   * runs the 'run' method (in this thread), then return the alignFrame that's
   * (hopefully) been read
   * 
   * @return
   */
  protected AlignFrame _LoadFileWaitTillLoaded()
  {
    this.run();

    return alignFrame;
  }

  public void updateRecentlyOpened()
  {
    Vector<String> recent = new Vector<>();
    if (protocol == DataSourceType.PASTE)
    {
      // do nothing if the file was pasted in as text... there is no filename to
      // refer to it as.
      return;
    }
    if (file != null
            && file.indexOf(System.getProperty("java.io.tmpdir")) > -1)
    {
      // ignore files loaded from the system's temporary directory
      return;
    }
    String type = protocol == DataSourceType.FILE ? "RECENT_FILE"
            : "RECENT_URL";

    String historyItems = Cache.getProperty(type);

    StringTokenizer st;

    if (historyItems != null)
    {
      st = new StringTokenizer(historyItems, "\t");

      while (st.hasMoreTokens())
      {
        recent.addElement(st.nextToken().trim());
      }
    }

    if (recent.contains(file))
    {
      recent.remove(file);
    }

    StringBuffer newHistory = new StringBuffer(file);
    for (int i = 0; i < recent.size() && i < 10; i++)
    {
      newHistory.append("\t");
      newHistory.append(recent.elementAt(i));
    }

    Cache.setProperty(type, newHistory.toString());

    if (protocol == DataSourceType.FILE)
    {
      Cache.setProperty("DEFAULT_FILE_FORMAT", format.getName());
    }
  }

  @Override
  public void run()
  {
    String title = protocol == DataSourceType.PASTE
            ? "Copied From Clipboard"
            : file;
    Runtime rt = Runtime.getRuntime();

    try
    {
      if (Desktop.instance != null)
      {
        Desktop.instance.startLoading(file);
      }
      if (format == null)
      {
        // just in case the caller didn't identify the file for us
        if (source != null)
        {
          format = new IdentifyFile().identify(source, false);
          // identify stream and rewind rather than close
        }
        else if (selectedFile != null)
        {
          format = new IdentifyFile().identify(selectedFile, protocol);
        }
        else
        {
          format = new IdentifyFile().identify(file, protocol);
        }

      }

      if (format == null)
      {
        Desktop.instance.stopLoading();
        System.err.println("The input file \"" + file
                + "\" has null or unidentifiable data content!");
        if (!Jalview.isHeadlessMode())
        {
          JvOptionPane.showInternalMessageDialog(Desktop.desktop,
                  MessageManager.getString("label.couldnt_read_data")
                          + " in " + file + "\n"
                          + AppletFormatAdapter.getSupportedFormats(),
                  MessageManager.getString("label.couldnt_read_data"),
                  JvOptionPane.WARNING_MESSAGE);
        }
        // don't set shouldBeSaved if didn't load anything
        // this.setShouldBeSaved();
        return;
      }
      // TODO: cache any stream datasources as a temporary file (eg. PDBs
      // retrieved via URL)
      if (Desktop.desktop != null && Desktop.desktop.isShowMemoryUsage())
      {
        System.gc();
        memused = (rt.maxMemory() - rt.totalMemory() + rt.freeMemory()); // free
        // memory
        // before
        // load
      }
      loadtime = -System.currentTimeMillis();
      AlignmentI al = null;

      if (FileFormat.Jalview.equals(format))
      {
        if (source != null)
        {
          // Tell the user (developer?) that this is going to cause a problem
          System.err.println(
                  "IMPLEMENTATION ERROR: Cannot read consecutive Jalview XML projects from a stream.");
          // We read the data anyway - it might make sense.
        }
        // BH 2018 switch to File object here instead of filename
        alignFrame = new Jalview2XML(raiseGUI).loadJalviewAlign(
                selectedFile == null ? file : selectedFile);
      }
      else
      {
        String error = AppletFormatAdapter.getSupportedFormats();
        try
        {
          if (source != null)
          {
            // read from the provided source
            al = new FormatAdapter().readFromFile(source, format);
          }
          else
          {

            // open a new source and read from it
            FormatAdapter fa = new FormatAdapter();
            boolean downloadStructureFile = format.isStructureFile()
                    && protocol.equals(DataSourceType.URL);
            if (downloadStructureFile)
            {
              String structExt = format.getExtensions().split(",")[0];
              String urlLeafName = file.substring(
                      file.lastIndexOf(
                              System.getProperty("file.separator")),
                      file.lastIndexOf("."));
              String tempStructureFileStr = createNamedJvTempFile(
                      urlLeafName, structExt);

              // BH - switching to File object here so as to hold
              // ._bytes array directly
              File tempFile = new File(tempStructureFileStr);
              UrlDownloadClient.download(file, tempFile);

              al = fa.readFile(tempFile, DataSourceType.FILE, format);
              source = fa.getAlignFile();
            }
            else
            {
              if (selectedFile == null)
              {
                al = fa.readFile(null, file, protocol, format);

              }
              else
              {
                al = fa.readFile(selectedFile, null, protocol, format);
              }
              source = fa.getAlignFile(); // keep reference for later if

              // necessary.
            }
          }
        } catch (java.io.IOException ex)
        {
          error = ex.getMessage();
        }

        if ((al != null) && (al.getHeight() > 0) && al.hasValidSequence())
        {
          // construct and register dataset sequences
          for (SequenceI sq : al.getSequences())
          {
            while (sq.getDatasetSequence() != null)
            {
              sq = sq.getDatasetSequence();
            }
            if (sq.getAllPDBEntries() != null)
            {
              for (PDBEntry pdbe : sq.getAllPDBEntries())
              {
                // register PDB entries with desktop's structure selection
                // manager
                StructureSelectionManager
                        .getStructureSelectionManager(Desktop.instance)
                        .registerPDBEntry(pdbe);
              }
            }
          }

          FeatureSettingsModelI proxyColourScheme = source
                  .getFeatureColourScheme();
          if (viewport != null)
          {
            // append to existing alignment
            viewport.addAlignment(al, title);
            viewport.applyFeaturesStyle(proxyColourScheme);
          }
          else
          {
            // otherwise construct the alignFrame

            if (source instanceof ComplexAlignFile)
            {
              HiddenColumns colSel = ((ComplexAlignFile) source)
                      .getHiddenColumns();
              SequenceI[] hiddenSeqs = ((ComplexAlignFile) source)
                      .getHiddenSequences();
              String colourSchemeName = ((ComplexAlignFile) source)
                      .getGlobalColourScheme();
              FeaturesDisplayedI fd = ((ComplexAlignFile) source)
                      .getDisplayedFeatures();
              alignFrame = new AlignFrame(al, hiddenSeqs, colSel,
                      AlignFrame.DEFAULT_WIDTH, AlignFrame.DEFAULT_HEIGHT);
              alignFrame.getViewport().setFeaturesDisplayed(fd);
              alignFrame.getViewport().setShowSequenceFeatures(
                      ((ComplexAlignFile) source).isShowSeqFeatures());
              ColourSchemeI cs = ColourSchemeMapper
                      .getJalviewColourScheme(colourSchemeName, al);
              if (cs != null)
              {
                alignFrame.changeColour(cs);
              }
            }
            else
            {
              alignFrame = new AlignFrame(al, AlignFrame.DEFAULT_WIDTH,
                      AlignFrame.DEFAULT_HEIGHT);
              if (source instanceof FeaturesSourceI)
              {
                alignFrame.getViewport().setShowSequenceFeatures(true);
              }
            }
            // add metadata and update ui
            if (!(protocol == DataSourceType.PASTE))
            {
              alignFrame.setFileName(file, format);
              alignFrame.setFileObject(selectedFile); // BH 2018 SwingJS
            }
            if (proxyColourScheme != null)
            {
              alignFrame.getViewport()
                      .applyFeaturesStyle(proxyColourScheme);
            }
            alignFrame.setStatus(MessageManager.formatMessage(
                    "label.successfully_loaded_file", new String[]
                    { title }));

            if (raiseGUI)
            {
              // add the window to the GUI
              // note - this actually should happen regardless of raiseGUI
              // status in Jalview 3
              // TODO: define 'virtual desktop' for benefit of headless scripts
              // that perform queries to find the 'current working alignment'
              Desktop.addInternalFrame(alignFrame, title,
                      AlignFrame.DEFAULT_WIDTH, AlignFrame.DEFAULT_HEIGHT);

              /*
               * for an Overview automatically opened with alignment,
               * set its title now alignFrame title has been set
               */
              alignFrame.alignPanel.setOverviewTitle(alignFrame);
            }

            try
            {
              alignFrame.setMaximum(
                      Cache.getDefault("SHOW_FULLSCREEN", false));
            } catch (java.beans.PropertyVetoException ex)
            {
            }
          }
        }
        else
        {
          if (Desktop.instance != null)
          {
            Desktop.instance.stopLoading();
          }

          final String errorMessage = MessageManager.getString(
                  "label.couldnt_load_file") + " " + title + "\n" + error;
          // TODO: refactor FileLoader to be independent of Desktop / Applet GUI
          // bits ?
          if (raiseGUI && Desktop.desktop != null)
          {
            javax.swing.SwingUtilities.invokeLater(new Runnable()
            {
              @Override
              public void run()
              {
                JvOptionPane.showInternalMessageDialog(Desktop.desktop,
                        errorMessage,
                        MessageManager
                                .getString("label.error_loading_file"),
                        JvOptionPane.WARNING_MESSAGE);
              }
            });
          }
          else
          {
            System.err.println(errorMessage);
          }
        }
      }

      updateRecentlyOpened();

    } catch (Exception er)
    {
      System.err.println("Exception whilst opening file '" + file);
      er.printStackTrace();
      if (raiseGUI)
      {
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            JvOptionPane.showInternalMessageDialog(Desktop.desktop,
                    MessageManager.formatMessage(
                            "label.problems_opening_file", new String[]
                            { file }),
                    MessageManager.getString("label.file_open_error"),
                    JvOptionPane.WARNING_MESSAGE);
          }
        });
      }
      alignFrame = null;
    } catch (OutOfMemoryError er)
    {

      er.printStackTrace();
      alignFrame = null;
      if (raiseGUI)
      {
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            JvOptionPane.showInternalMessageDialog(Desktop.desktop,
                    MessageManager.formatMessage(
                            "warn.out_of_memory_loading_file", new String[]
                            { file }),
                    MessageManager.getString("label.out_of_memory"),
                    JvOptionPane.WARNING_MESSAGE);
          }
        });
      }
      System.err.println("Out of memory loading file " + file + "!!");

    }
    loadtime += System.currentTimeMillis();
    // TODO: Estimate percentage of memory used by a newly loaded alignment -
    // warn if more memory will be needed to work with it
    // System.gc();
    memused = memused
            - (rt.maxMemory() - rt.totalMemory() + rt.freeMemory()); // difference
    // in free
    // memory
    // after
    // load
    if (Desktop.desktop != null && Desktop.desktop.isShowMemoryUsage())
    {
      if (alignFrame != null)
      {
        AlignmentI al = alignFrame.getViewport().getAlignment();

        System.out.println("Loaded '" + title + "' in "
                + (loadtime / 1000.0) + "s, took an additional "
                + (1.0 * memused / (1024.0 * 1024.0)) + " MB ("
                + al.getHeight() + " seqs by " + al.getWidth() + " cols)");
      }
      else
      {
        // report that we didn't load anything probably due to an out of memory
        // error
        System.out.println("Failed to load '" + title + "' in "
                + (loadtime / 1000.0) + "s, took an additional "
                + (1.0 * memused / (1024.0 * 1024.0))
                + " MB (alignment is null)");
      }
    }
    // remove the visual delay indicator
    if (Desktop.instance != null)
    {
      Desktop.instance.stopLoading();
    }

    this.setShouldBeSaved();
  }

  /**
   * This method creates the file -
   * {tmpdir}/jalview/{current_timestamp}/fileName.exetnsion using the supplied
   * file name and extension
   * 
   * @param fileName
   *          the name of the temp file to be created
   * @param extension
   *          the extension of the temp file to be created
   * @return
   */
  private static String createNamedJvTempFile(String fileName,
          String extension) throws IOException
  {
    String seprator = System.getProperty("file.separator");
    String jvTempDir = System.getProperty("java.io.tmpdir") + "jalview"
            + seprator + System.currentTimeMillis();
    File tempStructFile = new File(
            jvTempDir + seprator + fileName + "." + extension);
    tempStructFile.mkdirs();
    return tempStructFile.toString();
  }

  /*
   * set whether quit should ask to save when just loaded this source
   */
  private void setShouldBeSaved()
  {
    if (protocol == null)
      return;
    AlignFrame af = this.alignFrame;
    if (af == null)
      return;
    AlignViewport avp = af.getViewport();
    if (avp == null)
      return;
    avp.setSavedUpToDate(!protocol.isDynamic(),
            QuitHandler.Message.UNSAVED_ALIGNMENTS);
  }

}
