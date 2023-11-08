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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;

import jalview.api.AlignExportSettingsI;
import jalview.bin.Cache;
import jalview.bin.Jalview;
import jalview.datamodel.AlignExportSettingsAdapter;
import jalview.datamodel.AlignmentExportData;
import jalview.gui.AlignmentPanel;
import jalview.gui.IProgressIndicator;
import jalview.io.exceptions.ImageOutputException;
import jalview.util.MessageManager;

public abstract class HTMLOutput implements Runnable
{
  protected AlignmentPanel ap;

  /*
   * key for progress or status messages
   */
  protected long pSessionId;

  /*
   * (optional) place to write progress messages to
   */
  protected IProgressIndicator pIndicator;

  protected File generatedFile;

  String _bioJson = null;

  private String description;

  /**
   * Constructor given an alignment panel (which should not be null)
   * 
   * @param ap
   * @param desc
   */
  public HTMLOutput(AlignmentPanel ap, String desc)
  {
    this.ap = ap;
    this.pIndicator = ap.alignFrame;
    this.description = desc;
    this.pSessionId = System.currentTimeMillis();
  }

  /**
   * Gets the BioJSON data as a string, with lazy evaluation (first time called
   * only). If the output format is configured not to embed BioJSON, returns
   * null.
   * 
   * @return
   */
  public String getBioJSONData()
  {
    if (!isEmbedData())
    {
      return null;
    }
    if (_bioJson == null)
    {
      AlignExportSettingsI options = new AlignExportSettingsAdapter(true);
      AlignmentExportData exportData = ap.getAlignViewport()
              .getAlignExportData(options);
      _bioJson = new FormatAdapter(ap, options).formatSequences(
              FileFormat.Json, exportData.getAlignment(),
              exportData.getOmitHidden(), exportData.getStartEndPostions(),
              ap.getAlignViewport().getAlignment().getHiddenColumns());
    }

    return _bioJson;
  }

  /**
   * Read a template file content as string
   * 
   * @param file
   *          - the file to be read
   * @return File content as String
   * @throws IOException
   */
  public static String readFileAsString(File file) throws IOException
  {
    InputStreamReader isReader = null;
    BufferedReader buffReader = null;
    StringBuilder sb = new StringBuilder();
    Objects.requireNonNull(file, "File must not be null!");
    @SuppressWarnings("deprecation")
    URL url = file.toURL();
    if (url != null)
    {
      try
      {
        isReader = new InputStreamReader(url.openStream());
        buffReader = new BufferedReader(isReader);
        String line;
        String lineSeparator = System.getProperty("line.separator");
        while ((line = buffReader.readLine()) != null)
        {
          sb.append(line).append(lineSeparator);
        }

      } catch (Exception ex)
      {
        ex.printStackTrace();
      } finally
      {
        if (isReader != null)
        {
          isReader.close();
        }

        if (buffReader != null)
        {
          buffReader.close();
        }
      }
    }
    return sb.toString();
  }

  public static String getImageMapHTML()
  {
    return new String("<html>\n" + "<head>\n"
            + "<script language=\"JavaScript\">\n"
            + "var ns4 = document.layers;\n"
            + "var ns6 = document.getElementById && !document.all;\n"
            + "var ie4 = document.all;\n" + "offsetX = 0;\n"
            + "offsetY = 20;\n" + "var toolTipSTYLE=\"\";\n"
            + "function initToolTips()\n" + "{\n" + "  if(ns4||ns6||ie4)\n"
            + "  {\n"
            + "    if(ns4) toolTipSTYLE = document.toolTipLayer;\n"
            + "    else if(ns6) toolTipSTYLE = document.getElementById(\"toolTipLayer\").style;\n"
            + "    else if(ie4) toolTipSTYLE = document.all.toolTipLayer.style;\n"
            + "    if(ns4) document.captureEvents(Event.MOUSEMOVE);\n"
            + "    else\n" + "    {\n"
            + "      toolTipSTYLE.visibility = \"visible\";\n"
            + "      toolTipSTYLE.display = \"none\";\n" + "    }\n"
            + "    document.onmousemove = moveToMouseLoc;\n" + "  }\n"
            + "}\n" + "function toolTip(msg, fg, bg)\n" + "{\n"
            + "  if(toolTip.arguments.length < 1) // hide\n" + "  {\n"
            + "    if(ns4) toolTipSTYLE.visibility = \"hidden\";\n"
            + "    else toolTipSTYLE.display = \"none\";\n" + "  }\n"
            + "  else // show\n" + "  {\n"
            + "    if(!fg) fg = \"#555555\";\n"
            + "    if(!bg) bg = \"#FFFFFF\";\n" + "    var content =\n"
            + "    '<table border=\"0\" cellspacing=\"0\" cellpadding=\"1\" bgcolor=\"' + fg + '\"><td>' +\n"
            + "    '<table border=\"0\" cellspacing=\"0\" cellpadding=\"1\" bgcolor=\"' + bg + \n"
            + "    '\"><td align=\"center\"><font face=\"sans-serif\" color=\"' + fg +\n"
            + "    '\" size=\"-2\">&nbsp;' + msg +\n"
            + "    '&nbsp;</font></td></table></td></table>';\n"
            + "    if(ns4)\n" + "    {\n"
            + "      toolTipSTYLE.document.write(content);\n"
            + "      toolTipSTYLE.document.close();\n"
            + "      toolTipSTYLE.visibility = \"visible\";\n" + "    }\n"
            + "    if(ns6)\n" + "    {\n"
            + "      document.getElementById(\"toolTipLayer\").innerHTML = content;\n"
            + "      toolTipSTYLE.display='block'\n" + "    }\n"
            + "    if(ie4)\n" + "    {\n"
            + "      document.all(\"toolTipLayer\").innerHTML=content;\n"
            + "      toolTipSTYLE.display='block'\n" + "    }\n" + "  }\n"
            + "}\n" + "function moveToMouseLoc(e)\n" + "{\n"
            + "  if(ns4||ns6)\n" + "  {\n" + "    x = e.pageX;\n"
            + "    y = e.pageY;\n" + "  }\n" + "  else\n" + "  {\n"
            + "    x = event.x + document.body.scrollLeft;\n"
            + "    y = event.y + document.body.scrollTop;\n" + "  }\n"
            + "  toolTipSTYLE.left = x + offsetX;\n"
            + "  toolTipSTYLE.top = y + offsetY;\n" + "  return true;\n"
            + "}\n" + "</script>\n" + "</head>\n" + "<body>\n"
            + "<div id=\"toolTipLayer\" style=\"position:absolute; visibility: hidden\"></div>\n"
            + "<script language=\"JavaScript\"><!--\n"
            + "initToolTips(); //--></script>\n");

  }

  /**
   * Prompts the user to choose an output file and returns the file path, or
   * null on Cancel
   * 
   * @return
   */
  public String getOutputFile()
  {
    String selectedFile = null;

    // TODO: JAL-3048 generate html rendered view (requires SvgGraphics and/or
    // Jalview HTML rendering system- probably not required for Jalview-JS)
    JalviewFileChooser jvFileChooser = new JalviewFileChooser("html",
            "HTML files");
    jvFileChooser.setFileView(new JalviewFileView());

    jvFileChooser
            .setDialogTitle(MessageManager.getString("label.save_as_html"));
    jvFileChooser.setToolTipText(MessageManager.getString("action.save"));

    int fileChooserOpt = jvFileChooser.showSaveDialog(null);
    if (fileChooserOpt == JalviewFileChooser.APPROVE_OPTION)
    {
      Cache.setProperty("LAST_DIRECTORY",
              jvFileChooser.getSelectedFile().getParent());
      selectedFile = jvFileChooser.getSelectedFile().getPath();
    }

    return selectedFile;
  }

  protected void setProgressMessage(String message)
  {
    if (pIndicator != null && !isHeadless())
    {
      pIndicator.setProgressBar(message, pSessionId);
    }
    else
    {
      System.out.println(message);
    }
  }

  /**
   * Answers true if HTML export is invoke in headless mode or false otherwise
   * 
   * @return
   */
  protected boolean isHeadless()
  {
    return System.getProperty("java.awt.headless") != null
            && System.getProperty("java.awt.headless").equals("true");
  }

  /**
   * This method provides implementation of consistent behaviour which should
   * occur after a HTML file export. It MUST be called at the end of the
   * exportHTML() method implementation.
   */
  protected void exportCompleted()
  {
    if (isLaunchInBrowserAfterExport() && !isHeadless())
    {
      /*
      try
      {
      */
      jalview.util.BrowserLauncher.openURL("file:///" + getExportedFile());
      /*
      } catch (IOException e)
      {
        e.printStackTrace();
      }
      */
    }
  }

  /**
   * if this answers true then BioJSON data will be embedded to the exported
   * HTML file otherwise it won't be embedded.
   * 
   * @return
   */
  public abstract boolean isEmbedData();

  /**
   * if this answers true then the generated HTML file is opened for viewing in
   * a browser after its generation otherwise it won't be opened in a browser
   * 
   * @return
   */
  public abstract boolean isLaunchInBrowserAfterExport();

  /**
   * handle to the generated HTML file
   * 
   * @return
   */
  public File getExportedFile()
  {
    return generatedFile;
  }

  public void exportHTML(String outputFile) throws ImageOutputException
  {
    exportHTML(outputFile, null);
  }

  public void exportHTML(String outputFile, String renderer) throws ImageOutputException
  {
    setProgressMessage(MessageManager.formatMessage(
            "status.exporting_alignment_as_x_file", getDescription()));
    try
    {
      if (outputFile == null)
      {
        /*
         * prompt for output file
         */
        outputFile = getOutputFile();
        if (outputFile == null)
        {
          setProgressMessage(MessageManager.formatMessage(
                  "status.cancelled_image_export_operation",
                  getDescription()));
          return;
        }
      }
      generatedFile = new File(outputFile);
    } catch (Exception e)
    {
      setProgressMessage(MessageManager
              .formatMessage("info.error_creating_file", getDescription()));
      e.printStackTrace();
      return;
    }
    if (Jalview.isHeadlessMode())
    {
      this.run(renderer);
    }
    else
    {
      new Thread(this).start();
    }

  }

  /**
   * Answers a short description of the image format suitable for display in
   * messages
   * 
   * @return
   */
  protected final String getDescription()
  {
    return description;
  }

  // used to pass an option such as render to run
  public abstract void run(String string) throws ImageOutputException;
}