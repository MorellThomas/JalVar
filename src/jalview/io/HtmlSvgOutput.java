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

import java.awt.Graphics;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGHints;

import jalview.bin.Cache;
import jalview.gui.AlignmentPanel;
import jalview.gui.LineartOptions;
import jalview.gui.OOMWarning;
import jalview.math.AlignmentDimension;
import jalview.util.MessageManager;

public class HtmlSvgOutput extends HTMLOutput
{
  public HtmlSvgOutput(AlignmentPanel ap)
  {
    super(ap, "HTML");
  }

  public int printUnwrapped(int pwidth, int pheight, int pi,
          Graphics idGraphics, Graphics alignmentGraphics)
          throws PrinterException
  {
    return ap.printUnwrapped(pwidth, pheight, pi, idGraphics,
            alignmentGraphics);
  }

  public int printWrapped(int pwidth, int pheight, int pi, Graphics... pg)
          throws PrinterException
  {
    return ap.printWrappedAlignment(pwidth, pheight, pi, pg[0]);
  }

  String getHtml(String titleSvg, String alignmentSvg, String jsonData,
          boolean wrapped)
  {
    StringBuilder htmlSvg = new StringBuilder();
    htmlSvg.append("<html>\n");
    if (jsonData != null)
    {
      htmlSvg.append(
              "<button onclick=\"javascipt:openJalviewUsingCurrentUrl();\">Launch in Jalview</button> &nbsp;");
      htmlSvg.append(
              "<input type=\"submit\" value=\"View raw BioJSON Data\" onclick=\"jQuery.facebox({ div:'#seqData' }); return false;\" />");
      htmlSvg.append(
              "<div style=\"display: none;\" name=\"seqData\" id=\"seqData\" >"
                      + jsonData + "</div>");
      htmlSvg.append("<br/>&nbsp;");
    }
    htmlSvg.append("\n<style type=\"text/css\"> "
            + "div.parent{ width:100%;<!-- overflow: auto; -->}\n"
            + "div.titlex{ width:11%; float: left; }\n"
            + "div.align{ width:89%; float: right; }\n"
            + "div.main-container{ border: 2px solid blue; border: 2px solid blue; width: 99%;   min-height: 99%; }\n"
            + ".sub-category-container {overflow-y: scroll; overflow-x: hidden; width: 100%; height: 100%;}\n"
            + "object {pointer-events: none;}");
    if (jsonData != null)
    {
      // facebox style sheet for displaying raw BioJSON data
      htmlSvg.append(
              "#facebox { position: absolute;  top: 0;   left: 0; z-index: 100; text-align: left; }\n"
                      + "#facebox .popup{ position:relative; border:3px solid rgba(0,0,0,0); -webkit-border-radius:5px;"
                      + "-moz-border-radius:5px; border-radius:5px; -webkit-box-shadow:0 0 18px rgba(0,0,0,0.4); -moz-box-shadow:0 0 18px rgba(0,0,0,0.4);"
                      + "box-shadow:0 0 18px rgba(0,0,0,0.4); }\n"
                      + "#facebox .content { display:table; width: 98%; padding: 10px; background: #fff; -webkit-border-radius:4px; -moz-border-radius:4px;"
                      + " border-radius:4px; }\n"
                      + "#facebox .content > p:first-child{ margin-top:0; }\n"
                      + "#facebox .content > p:last-child{ margin-bottom:0; }\n"
                      + "#facebox .close{ position:absolute; top:5px; right:5px; padding:2px; background:#fff; }\n"
                      + "#facebox .close img{ opacity:0.3; }\n"
                      + "#facebox .close:hover img{ opacity:1.0; }\n"
                      + "#facebox .loading { text-align: center; }\n"
                      + "#facebox .image { text-align: center;}\n"
                      + "#facebox img { border: 0;  margin: 0; }\n"
                      + "#facebox_overlay { position: fixed; top: 0px; left: 0px; height:100%; width:100%; }\n"
                      + ".facebox_hide { z-index:-100; }\n"
                      + ".facebox_overlayBG { background-color: #000;  z-index: 99;  }");
    }
    htmlSvg.append("</style>");
    if (!wrapped)
    {
      htmlSvg.append("<div class=\"main-container\" \n>");
      htmlSvg.append("<div class=\"titlex\">\n");
      htmlSvg.append("<div class=\"sub-category-container\"> \n");
      htmlSvg.append(titleSvg);
      htmlSvg.append("</div>");
      htmlSvg.append(
              "</div>\n\n<!-- ========================================================================================== -->\n\n");
      htmlSvg.append("<div class=\"align\" >");
      htmlSvg.append(
              "<div class=\"sub-category-container\"> <div style=\"overflow-x: scroll;\">")
              .append(alignmentSvg).append("</div></div>").append("</div>");
      htmlSvg.append("</div>");

      htmlSvg.append(
              "<script language=\"JavaScript\" type=\"text/javascript\" src=\"http://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js\"></script>\n"
                      + "<script language=\"JavaScript\" type=\"text/javascript\"  src=\"http://ajax.googleapis.com/ajax/libs/jqueryui/1.11.2/jquery-ui.min.js\"></script>\n"
                      + "<script>\n"
                      + "var subCatContainer = $(\".sub-category-container\");\n"
                      + "subCatContainer.scroll(\nfunction() {\n"
                      + "subCatContainer.scrollTop($(this).scrollTop());\n});\n");

      htmlSvg.append("</script>\n");
    }
    else
    {
      htmlSvg.append("<div>\n").append(alignmentSvg).append("</div>");
      htmlSvg.append(
              "<script language=\"JavaScript\" type=\"text/javascript\" src=\"http://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js\"></script>\n"
                      + "<script language=\"JavaScript\" type=\"text/javascript\"  src=\"http://ajax.googleapis.com/ajax/libs/jqueryui/1.11.2/jquery-ui.min.js\"></script>\n");
    }

    // javascript for launching file in Jalview
    htmlSvg.append("<script language=\"JavaScript\">\n");
    htmlSvg.append("function openJalviewUsingCurrentUrl(){\n");
    htmlSvg.append(
            "    var json = JSON.parse(document.getElementById(\"seqData\").innerHTML);\n");
    htmlSvg.append(
            "    var jalviewVersion = json['appSettings'].version;\n");
    htmlSvg.append("    var url = json['appSettings'].webStartUrl;\n");
    htmlSvg.append(
            "    var myForm = document.createElement(\"form\");\n\n");
    htmlSvg.append("    var heap = document.createElement(\"input\");\n");
    htmlSvg.append("    heap.setAttribute(\"name\", \"jvm-max-heap\") ;\n");
    htmlSvg.append("    heap.setAttribute(\"value\", \"2G\");\n\n");
    htmlSvg.append("    var target = document.createElement(\"input\");\n");
    htmlSvg.append("    target.setAttribute(\"name\", \"open\");\n");
    htmlSvg.append("    target.setAttribute(\"value\", document.URL);\n\n");
    htmlSvg.append(
            "    var jvVersion = document.createElement(\"input\");\n");
    htmlSvg.append("    jvVersion.setAttribute(\"name\", \"version\") ;\n");
    htmlSvg.append(
            "    jvVersion.setAttribute(\"value\", jalviewVersion);\n\n");
    htmlSvg.append("    myForm.action = url;\n");
    htmlSvg.append("    myForm.appendChild(heap);\n");
    htmlSvg.append("    myForm.appendChild(target);\n");
    htmlSvg.append("    myForm.appendChild(jvVersion);\n");
    htmlSvg.append("    document.body.appendChild(myForm);\n");
    htmlSvg.append("    myForm.submit() ;\n");
    htmlSvg.append("    document.body.removeChild(myForm);\n");
    htmlSvg.append("}\n");

    if (jsonData != null)
    {
      // JQuery FaceBox for displaying raw BioJSON data");
      File faceBoxJsFile = new File("examples/javascript/facebox-1.3.js");
      try
      {
        htmlSvg.append(HTMLOutput.readFileAsString(faceBoxJsFile));
      } catch (IOException e)
      {
        e.printStackTrace();
      }
    }

    htmlSvg.append("</script>\n");
    htmlSvg.append("</html>");
    return htmlSvg.toString();
  }

  @Override
  public boolean isEmbedData()
  {
    return Boolean
            .valueOf(Cache.getDefault("EXPORT_EMBBED_BIOJSON", "true"));
  }

  @Override
  public boolean isLaunchInBrowserAfterExport()
  {
    return true;
  }

  @Override
  public void run()
  {
    run(null);
  }

  @Override
  public void run(String renderer)
  {
    try
    {
      String renderStyle = renderer == null
              ? Cache.getDefault("HTML_RENDERING",
                      LineartOptions.PROMPT_EACH_TIME)
              : renderer;
      AtomicBoolean textOption = new AtomicBoolean(
              !"lineart".equals(renderStyle.toLowerCase(Locale.ROOT)));

      /*
       * configure the action to run on OK in the dialog
       */
      Runnable okAction = () -> {
        doOutput(textOption.get());
      };

      /*
       * Prompt for character rendering style if preference is not set
       */
      if (renderStyle.equalsIgnoreCase(LineartOptions.PROMPT_EACH_TIME)
              && !isHeadless())
      {
        LineartOptions svgOption = new LineartOptions("HTML", textOption);
        svgOption.setResponseAction(1, () -> {
          setProgressMessage(MessageManager.formatMessage(
                  "status.cancelled_image_export_operation",
                  getDescription()));
        });
        svgOption.setResponseAction(0, okAction);
        svgOption.showDialog();
        /* no code here - JalviewJS cannot execute it */
      }
      else
      {
        /*
         * else (if preference set) just do the export action
         */
        doOutput(textOption.get());
      }
    } catch (OutOfMemoryError err)
    {
      System.out.println("########################\n" + "OUT OF MEMORY "
              + generatedFile + "\n" + "########################");
      new OOMWarning("Creating Image for " + generatedFile, err);
    } catch (Exception e)
    {
      e.printStackTrace();
      setProgressMessage(MessageManager
              .formatMessage("info.error_creating_file", getDescription()));
    }
  }

  /**
   * Builds and writes the image to the file specified by field
   * <code>generatedFile</code>
   * 
   * @param textCharacters
   *          true for Text character rendering, false for Lineart
   */
  protected void doOutput(boolean textCharacters)
  {
    try
    {
      AlignmentDimension aDimension = ap.getAlignmentDimension();
      SVGGraphics2D idPanelGraphics = new SVGGraphics2D(
              aDimension.getWidth(), aDimension.getHeight());
      SVGGraphics2D alignPanelGraphics = new SVGGraphics2D(
              aDimension.getWidth(), aDimension.getHeight());
      if (!textCharacters) // Lineart selected
      {
        idPanelGraphics.setRenderingHint(SVGHints.KEY_DRAW_STRING_TYPE,
                SVGHints.VALUE_DRAW_STRING_TYPE_VECTOR);
        alignPanelGraphics.setRenderingHint(SVGHints.KEY_DRAW_STRING_TYPE,
                SVGHints.VALUE_DRAW_STRING_TYPE_VECTOR);
      }
      if (ap.av.getWrapAlignment())
      {
        printWrapped(aDimension.getWidth(), aDimension.getHeight(), 0,
                alignPanelGraphics);
      }
      else
      {
        printUnwrapped(aDimension.getWidth(), aDimension.getHeight(), 0,
                idPanelGraphics, alignPanelGraphics);
      }

      String idPanelSvgData = idPanelGraphics.getSVGDocument();
      String alignPanelSvgData = alignPanelGraphics.getSVGDocument();
      String jsonData = getBioJSONData();
      String htmlData = getHtml(idPanelSvgData, alignPanelSvgData, jsonData,
              ap.av.getWrapAlignment());
      FileOutputStream out = new FileOutputStream(generatedFile);
      out.write(htmlData.getBytes());
      out.flush();
      out.close();
      setProgressMessage(MessageManager
              .formatMessage("status.export_complete", getDescription()));
      exportCompleted();
    } catch (Exception e)
    {
      e.printStackTrace();
      setProgressMessage(MessageManager
              .formatMessage("info.error_creating_file", getDescription()));
    }
  }
}
