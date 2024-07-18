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

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.api.ComplexAlignFile;
import jalview.api.FeatureSettingsModelI;
import jalview.api.FeaturesDisplayedI;
import jalview.api.FeaturesSourceI;
import jalview.bin.Cache;
import jalview.bin.Jalview;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.SequenceI;
import jalview.io.AlignmentFileReaderI;
import jalview.io.AppletFormatAdapter;
import jalview.io.DataSourceType;
import jalview.io.FileFormatException;
import jalview.io.FileFormatI;
import jalview.io.FormatAdapter;
import jalview.io.IdentifyFile;
import jalview.io.JalviewFileChooser;
import jalview.io.JalviewFileView;
import jalview.jbgui.GCutAndPasteTransfer;
import jalview.json.binding.biojson.v1.ColourSchemeMapper;
import jalview.schemes.ColourSchemeI;
import jalview.util.MessageManager;

/**
 * Cut'n'paste files into the desktop See JAL-1105
 * 
 * @author $author$
 * @version $Revision$
 */
public class CutAndPasteTransfer extends GCutAndPasteTransfer
{

  AlignmentViewPanel alignpanel;

  AlignViewportI viewport;

  AlignmentFileReaderI source = null;

  public CutAndPasteTransfer()
  {
    this.setFrameIcon(null);
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        textarea.requestFocus();
      }
    });

  }

  /**
   * set font size of the textarea
   * @param size
   */
  public void setFont(Font font)
  {
    textarea.setFont(font);
    
  }

  /**
   * DOCUMENT ME!
   */
  public void setForInput(AlignmentViewPanel viewpanel)
  {
    this.alignpanel = viewpanel;
    if (alignpanel != null)
    {
      this.viewport = alignpanel.getAlignViewport();
    }
    if (viewport != null)
    {
      ok.setText(MessageManager.getString("action.add"));
    }

    getContentPane().add(inputButtonPanel, java.awt.BorderLayout.SOUTH);
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String getText()
  {
    return textarea.getText();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param text
   *          DOCUMENT ME!
   */
  public void setText(String text)
  {
    textarea.setText(text);
  }

  public void appendText(String text)
  {
    textarea.append(text);
  }

  @Override
  public void save_actionPerformed(ActionEvent e)
  {
    // TODO: JAL-3048 JalviewFileChooser - Save option

    JalviewFileChooser chooser = new JalviewFileChooser(
            Cache.getProperty("LAST_DIRECTORY"));

    chooser.setAcceptAllFileFilterUsed(false);
    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(
            MessageManager.getString("label.save_text_to_file"));
    chooser.setToolTipText(MessageManager.getString("action.save"));

    int value = chooser.showSaveDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      try
      {
        PrintWriter out = new PrintWriter(
                new FileWriter(chooser.getSelectedFile()));

        out.print(getText());
        out.close();
      } catch (Exception ex)
      {
        ex.printStackTrace();
      }

    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void copyItem_actionPerformed(ActionEvent e)
  {
    textarea.getSelectedText();
    Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
    c.setContents(new StringSelection(textarea.getSelectedText()), null);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void pasteMenu_actionPerformed(ActionEvent e)
  {
    Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable contents = c.getContents(this);

    if (contents == null)
    {
      return;
    }

    try
    {
      textarea.append(
              (String) contents.getTransferData(DataFlavor.stringFlavor));
    } catch (Exception ex)
    {
    }
  }
  
  /**
   * show menu for chaning the font
   * @param e
   */
  @Override
  public void fontSizeMenu_actionPerformed(ActionEvent e)
  {
    new FontChooser(this);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void ok_actionPerformed(ActionEvent e)
  {
    String text = getText();
    if (text.trim().length() < 1)
    {
      return;
    }

    FileFormatI format = null;
    try
    {
      format = new IdentifyFile().identify(text, DataSourceType.PASTE);
    } catch (FileFormatException e1)
    {
      // leave as null
    }
    if (format == null)
    {
      System.err
              .println(MessageManager.getString("label.couldnt_read_data"));
      if (!Jalview.isHeadlessMode())
      {
        JvOptionPane.showInternalMessageDialog(Desktop.desktop,
                AppletFormatAdapter.getSupportedFormats(),
                MessageManager.getString("label.couldnt_read_data"),
                JvOptionPane.WARNING_MESSAGE);
      }
      return;
    }

    // TODO: identify feature, annotation or tree file and parse appropriately.
    AlignmentI al = null;

    try
    {
      FormatAdapter fa = new FormatAdapter(alignpanel);
      al = fa.readFile(getText(), DataSourceType.PASTE, format);
      source = fa.getAlignFile();

    } catch (IOException ex)
    {
      JvOptionPane.showInternalMessageDialog(Desktop.desktop, MessageManager
              .formatMessage("label.couldnt_read_pasted_text", new String[]
              { ex.toString() }),
              MessageManager.getString("label.error_parsing_text"),
              JvOptionPane.WARNING_MESSAGE);
    }

    if (al != null && al.hasValidSequence())
    {
      String title = MessageManager
              .formatMessage("label.input_cut_paste_params", new String[]
              { format.getName() });
      FeatureSettingsModelI proxyColourScheme = source
              .getFeatureColourScheme();

      /*
       * if the view panel was closed its alignment is nulled
       * and this is an orphaned cut and paste window
       */
      if (viewport != null && viewport.getAlignment() != null)
      {
        ((AlignViewport) viewport).addAlignment(al, title);
        viewport.applyFeaturesStyle(proxyColourScheme);
      }
      else
      {

        AlignFrame af;
        if (source instanceof ComplexAlignFile)
        {
          HiddenColumns hidden = ((ComplexAlignFile) source)
                  .getHiddenColumns();
          SequenceI[] hiddenSeqs = ((ComplexAlignFile) source)
                  .getHiddenSequences();
          boolean showSeqFeatures = ((ComplexAlignFile) source)
                  .isShowSeqFeatures();
          String colourSchemeName = ((ComplexAlignFile) source)
                  .getGlobalColourScheme();
          FeaturesDisplayedI fd = ((ComplexAlignFile) source)
                  .getDisplayedFeatures();
          af = new AlignFrame(al, hiddenSeqs, hidden,
                  AlignFrame.DEFAULT_WIDTH, AlignFrame.DEFAULT_HEIGHT);
          af.getViewport().setShowSequenceFeatures(showSeqFeatures);
          af.getViewport().setFeaturesDisplayed(fd);
          af.setMenusForViewport();
          ColourSchemeI cs = ColourSchemeMapper
                  .getJalviewColourScheme(colourSchemeName, al);
          if (cs != null)
          {
            af.changeColour(cs);
          }
        }
        else
        {
          af = new AlignFrame(al, AlignFrame.DEFAULT_WIDTH,
                  AlignFrame.DEFAULT_HEIGHT);
          if (source instanceof FeaturesSourceI)
          {
            af.getViewport().setShowSequenceFeatures(true);
          }
        }
        if (proxyColourScheme != null)
        {
          af.getViewport().applyFeaturesStyle(proxyColourScheme);
        }
        af.currentFileFormat = format;
        Desktop.addInternalFrame(af, title, AlignFrame.DEFAULT_WIDTH,
                AlignFrame.DEFAULT_HEIGHT);
        af.setStatus(MessageManager
                .getString("label.successfully_pasted_alignment_file"));

        try
        {
          af.setMaximum(Cache.getDefault("SHOW_FULLSCREEN", false));
        } catch (Exception ex)
        {
        }
      }
    }
    else
    {
      System.err
              .println(MessageManager.getString("label.couldnt_read_data"));
      if (!Jalview.isHeadlessMode())
      {
        JvOptionPane.showInternalMessageDialog(Desktop.desktop,
                AppletFormatAdapter.getSupportedFormats(),
                MessageManager.getString("label.couldnt_read_data"),
                JvOptionPane.WARNING_MESSAGE);
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void cancel_actionPerformed(ActionEvent e)
  {
    try
    {
      this.setClosed(true);
    } catch (Exception ex)
    {
    }
  }

  @Override
  public void textarea_mousePressed(MouseEvent e)
  {
    /*
     * isPopupTrigger is checked in mousePressed on Mac,
     * in mouseReleased on Windows
     */
    if (e.isPopupTrigger())
    {
      JPopupMenu popup = new JPopupMenu(
              MessageManager.getString("action.edit"));
      JMenuItem item = new JMenuItem(
              MessageManager.getString("action.copy"));
      item.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          copyItem_actionPerformed(e);
        }
      });
      popup.add(item);
      item = new JMenuItem(MessageManager.getString("action.paste"));
      item.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          pasteMenu_actionPerformed(e);
        }
      });
      popup.add(item);
      popup.show(this, e.getX() + 10, e.getY() + textarea.getY() + 40);

    }
  }

}
