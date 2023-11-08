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

import java.awt.Component;
import java.awt.Graphics;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import jalview.bin.Cache;
import jalview.bin.Jalview;
import jalview.io.JalviewFileChooser;
import jalview.io.JalviewFileView;
import jalview.io.exceptions.ImageOutputException;
import jalview.util.ImageMaker;
import jalview.util.ImageMaker.TYPE;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.util.imagemaker.BitmapImageSizing;

/**
 * A class that marshals steps in exporting a view in image graphics format
 * <ul>
 * <li>prompts the user for the output file, if not already specified</li>
 * <li>prompts the user for Text or Lineart character rendering, if
 * necessary</li>
 * <li>instantiates an ImageMaker to create the appropriate Graphics output
 * context for the image format</li>
 * <li>invokes a callback to do the work of writing to the graphics</li>
 * </ul>
 * 
 * @author gmcarstairs
 *
 */
public class ImageExporter
{
  // todo move interface to jalview.api? or replace with lambda?
  /**
   * An interface for the callback that can be run to write the image on to the
   * graphics object. The callback should throw any exceptions arising so they
   * can be reported by this class.
   */
  public interface ImageWriterI
  {
    void exportImage(Graphics g) throws Exception;
  }

  private IProgressIndicator messageBoard;

  private ImageWriterI imageWriter;

  TYPE imageType;

  private String title;

  /**
   * Constructor given a callback handler to write graphics data, an (optional)
   * target for status messages, image type and (optional) title for output file
   * 
   * @param writer
   * @param statusBar
   * @param type
   * @param fileTitle
   */
  public ImageExporter(ImageWriterI writer, IProgressIndicator statusBar,
          TYPE type, String fileTitle)
  {
    this.imageWriter = writer;
    this.messageBoard = statusBar;
    this.imageType = type;
    this.title = fileTitle;
  }

  /**
   * Prompts the user for output file and Text/Lineart options as required,
   * configures a Graphics context for output, and makes a callback to the
   * client code to perform the image output
   * 
   * @param file
   *          output file (if null, user is prompted to choose)
   * @param parent
   *          parent component for any dialogs shown
   * @param width
   * @param height
   * @param imageSource
   *          what the image is of e.g. Tree, Alignment
   */
  public void doExport(File file, Component parent, int width, int height,
          String imageSource) throws ImageOutputException
  {
    doExport(file, parent, width, height, imageSource, null,
            BitmapImageSizing.nullBitmapImageSizing());
  }

  public void doExport(File file, Component parent, int width, int height,
          String imageSource, String renderer, BitmapImageSizing userBis) throws ImageOutputException
  {
    final long messageId = System.currentTimeMillis();
    setStatus(
            MessageManager.formatMessage(
                    "status.exporting_alignment_as_x_file", imageType),
            messageId);

    /*
     * prompt user for output file if not provided
     */
    if (file == null && !Jalview.isHeadlessMode())
    {
      if (Desktop.instance.isInBatchMode())
      {
        // defensive error report - we could wait for user input.. I  guess ?
        throw(new ImageOutputException("Need an output file to render to when exporting images in batch mode!"));
      }
      JalviewFileChooser chooser = imageType.getFileChooser();
      chooser.setFileView(new JalviewFileView());
      MessageManager.formatMessage("label.create_image_of",
              imageType.getName(), imageSource);
      String title = "Create " + imageType.getName()
              + " image from alignment";
      chooser.setDialogTitle(title);
      chooser.setToolTipText(MessageManager.getString("action.save"));
      int value = chooser.showSaveDialog(parent);
      if (value != JalviewFileChooser.APPROVE_OPTION)
      {
        String msg = MessageManager.formatMessage(
                "status.cancelled_image_export_operation", imageType.name);
        setStatus(msg, messageId);
        return;
      }
      Cache.setProperty("LAST_DIRECTORY",
              chooser.getSelectedFile().getParent());
      file = chooser.getSelectedFile();
    }

    /*
     * Prompt for Text or Lineart (EPS/SVG) unless a preference is already set
     * for this as EPS_RENDERING / SVG_RENDERING
     * Always set to Text for JalviewJS as Lineart (glyph fonts) not available
     */
    String renderStyle = renderer == null
            ? Cache.getDefault(imageType.getName() + "_RENDERING",
                    LineartOptions.PROMPT_EACH_TIME)
            : renderer;
    if (Platform.isJS())
    {
      renderStyle = "Text";
    }
    AtomicBoolean textSelected = new AtomicBoolean(
            !"Lineart".equals(renderStyle));
    if ((imageType == TYPE.EPS || imageType == TYPE.SVG)
            && LineartOptions.PROMPT_EACH_TIME.equals(renderStyle)
            && !Jalview.isHeadlessMode())
    {
      final File chosenFile = file;
      Runnable okAction = () -> {
        exportImage(chosenFile, !textSelected.get(), width, height,
                messageId, userBis);
      };
      LineartOptions epsOption = new LineartOptions(TYPE.EPS.getName(),
              textSelected);
      epsOption.setResponseAction(1, () -> {
        setStatus(MessageManager.formatMessage(
                "status.cancelled_image_export_operation",
                imageType.getName()), messageId);
      });
      epsOption.setResponseAction(0, okAction);
      epsOption.showDialog();
      /* no code here - JalviewJS cannot execute it */
    }
    else
    {
      /*
       * character rendering not required, or preference already set 
       * - just do the export
       */
      exportImage(file, !textSelected.get(), width, height, messageId,
              userBis);
    }
  }

  /**
   * Constructs a suitable graphics context and passes it to the callback
   * handler for the image to be written. Shows status messages for export in
   * progress, complete, or failed as appropriate.
   * 
   * @param chosenFile
   * @param asLineart
   * @param width
   * @param height
   * @param messageId
   */
  protected void exportImage(File chosenFile, boolean asLineart, int width,
          int height, long messageId, BitmapImageSizing userBis)
  {
    String type = imageType.getName();
    try
    {
      // setStatus(
      // MessageManager.formatMessage(
      // "status.exporting_alignment_as_x_file", type),
      // messageId);
      ImageMaker im = new ImageMaker(imageType, width, height, chosenFile,
              title, asLineart, userBis);
      imageWriter.exportImage(im.getGraphics());
      im.writeImage();
      setStatus(
              MessageManager.formatMessage("status.export_complete", type),
              messageId);
    } catch (Exception e)
    {
      jalview.bin.Console.error(String.format("Error creating %s file: %s", type,
              e.toString()),e);
      setStatus(MessageManager.formatMessage("info.error_creating_file",
              type), messageId);
    }
  }

  /**
   * Asks the callback to show a status message with given id
   * 
   * @param msg
   * @param id
   */
  void setStatus(String msg, long id)
  {
    if (messageBoard != null && !Jalview.isHeadlessMode())
    {
      messageBoard.setProgressBar(msg, id);
    }
  }

}