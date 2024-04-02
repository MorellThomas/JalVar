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
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;

import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;

import jalview.bin.Cache;
import jalview.jbgui.GFontChooser;
import jalview.util.MessageManager;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class FontChooser extends GFontChooser
{
  AlignmentPanel ap;

  TreePanel tp;
  
  CutAndPasteTransfer cap;

  /*
   * The font on opening the dialog (to be restored on Cancel)
   */
  Font oldFont;

  /*
   * The font on opening the dialog (to be restored on Cancel)
   * on the other half of a split frame (if applicable)
   */
  Font oldComplementFont;

  /*
   * the state of 'scale protein as cDNA' on opening the dialog
   */
  boolean oldProteinScale;

  /*
   * the state of 'same font for protein and cDNA' on opening the dialog
   */
  boolean oldMirrorFont;

  boolean init = true;

  JInternalFrame frame;

  /*
   * The last font settings selected in the dialog
   */
  private Font lastSelected = null;

  private boolean lastSelMono = false;

  private boolean oldSmoothFont;

  private boolean oldComplementSmooth;

  /**
   * Creates a new FontChooser for a tree panel
   * 
   * @param treePanel
   */
  public FontChooser(TreePanel treePanel)
  {
    this.tp = treePanel;
    ap = treePanel.getTreeCanvas().getAssociatedPanel();
    oldFont = treePanel.getTreeFont();
    defaultButton.setVisible(false);
    smoothFont.setEnabled(false);
    init();
  }

  /**
   * Creates a new FontChooser for an alignment panel
   * 
   * @param alignPanel
   */
  public FontChooser(AlignmentPanel alignPanel)
  {
    oldFont = alignPanel.av.getFont();
    oldProteinScale = alignPanel.av.isScaleProteinAsCdna();
    oldMirrorFont = alignPanel.av.isProteinFontAsCdna();
    oldSmoothFont = alignPanel.av.antiAlias;
    this.ap = alignPanel;
    init();
  }
  
  /**
   * Creates a new FontChooser for a CutAndPasteTransfer
   * @param cap
   */
  public FontChooser(CutAndPasteTransfer cap)
  {
    oldFont = new Font("Monospaced", Font.PLAIN, 12);
    this.cap = cap;
    init();
  }

  void init()
  {
    frame = new JInternalFrame();
    frame.setFrameIcon(null);
    frame.setContentPane(this);

    if (!isCapFont())
    {
      smoothFont.setSelected(ap.av.antiAlias);

      /*
       * Enable 'scale protein as cDNA' in a SplitFrame view. The selection is
       * stored in the ViewStyle of both dna and protein Viewport. Also enable
       * checkbox for copy font changes to other half of split frame.
       */
      boolean inSplitFrame = ap.av.getCodingComplement() != null;
      if (inSplitFrame)
      {
        oldComplementFont = ((AlignViewport) ap.av.getCodingComplement())
                .getFont();
        oldComplementSmooth = ((AlignViewport) ap.av
                .getCodingComplement()).antiAlias;
        scaleAsCdna.setVisible(true);
        scaleAsCdna.setSelected(ap.av.isScaleProteinAsCdna());
        fontAsCdna.setVisible(true);
        fontAsCdna.setSelected(ap.av.isProteinFontAsCdna());
      }
    }

    if (isTreeFont())
    {
      Desktop.addInternalFrame(frame,
              MessageManager.getString("action.change_font_tree_panel"),
              400, 200, false);
    }
    else
    {
      Desktop.addInternalFrame(frame,
              MessageManager.getString("action.change_font"), 380, 220,
              false);
    }

    frame.setLayer(JLayeredPane.PALETTE_LAYER);

    String[] fonts = java.awt.GraphicsEnvironment
            .getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

    for (int i = 0; i < fonts.length; i++)
    {
      fontName.addItem(fonts[i]);
    }

    for (int i = 1; i < 51; i++)
    {
      fontSize.addItem(i);
    }

    fontStyle.addItem("plain");
    fontStyle.addItem("bold");
    fontStyle.addItem("italic");

    fontName.setSelectedItem(oldFont.getName());
    fontSize.setSelectedItem(oldFont.getSize());
    fontStyle.setSelectedIndex(oldFont.getStyle());

    FontMetrics fm = getGraphics().getFontMetrics(oldFont);
    monospaced.setSelected(
            fm.getStringBounds("M", getGraphics()).getWidth() == fm
                    .getStringBounds("|", getGraphics()).getWidth());

    init = false;
  }

  @Override
  protected void smoothFont_actionPerformed()
  {
    ap.av.antiAlias = smoothFont.isSelected();
    ap.getAnnotationPanel().image = null;
    ap.paintAlignment(true, false);
    if (ap.av.getCodingComplement() != null && ap.av.isProteinFontAsCdna())
    {
      ((AlignViewport) ap.av
              .getCodingComplement()).antiAlias = ap.av.antiAlias;
      SplitFrame sv = (SplitFrame) ap.alignFrame.getSplitViewContainer();
      sv.adjustLayout();
      sv.repaint();
    }

  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  protected void ok_actionPerformed()
  {
    try
    {
      frame.setClosed(true);
    } catch (Exception ex)
    {
    }

    if (ap != null)
    {
      if (ap.getOverviewPanel() != null)
      {
        ap.getOverviewPanel().updateOverviewImage();
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
  protected void cancel_actionPerformed()
  {
    if (isTreeFont())
    {
      tp.setTreeFont(oldFont);
    }
    else if (ap != null)
    {
      ap.av.setFont(oldFont, true);
      ap.av.setScaleProteinAsCdna(oldProteinScale);
      ap.av.setProteinFontAsCdna(oldMirrorFont);
      ap.av.antiAlias = oldSmoothFont;
      ap.fontChanged();

      if (scaleAsCdna.isVisible() && scaleAsCdna.isEnabled())
      {
        ap.av.getCodingComplement().setScaleProteinAsCdna(oldProteinScale);
        ap.av.getCodingComplement().setProteinFontAsCdna(oldMirrorFont);
        ((AlignViewport) ap.av
                .getCodingComplement()).antiAlias = oldComplementSmooth;
        ap.av.getCodingComplement().setFont(oldComplementFont, true);
        SplitFrame splitFrame = (SplitFrame) ap.alignFrame
                .getSplitViewContainer();
        splitFrame.adjustLayout();
        splitFrame.repaint();
      }
    }

    try
    {
      frame.setClosed(true);
    } catch (Exception ex)
    {
    }
  }

  private boolean isTreeFont()
  {
    return tp != null;
  }
  
  private boolean isCapFont()
  {
    return cap != null;
  }

  /**
   * DOCUMENT ME!
   */
  void changeFont()
  {
    if (lastSelected == null)
    {
      // initialise with original font
      lastSelected = oldFont;
      FontMetrics fm = getGraphics().getFontMetrics(oldFont);
      double mw = fm.getStringBounds("M", getGraphics()).getWidth();
      double iw = fm.getStringBounds("I", getGraphics()).getWidth();
      lastSelMono = (mw == iw); // == on double - flaky?
    }

    Font newFont = new Font(fontName.getSelectedItem().toString(),
            fontStyle.getSelectedIndex(),
            (Integer) fontSize.getSelectedItem());
    FontMetrics fm = getGraphics().getFontMetrics(newFont);
    double mw = fm.getStringBounds("M", getGraphics()).getWidth();
    final Rectangle2D iBounds = fm.getStringBounds("I", getGraphics());
    double iw = iBounds.getWidth();
    if (mw < 1 || iw < 1)
    {
      String message = iBounds.getHeight() < 1
              ? MessageManager
                      .getString("label.font_doesnt_have_letters_defined")
              : MessageManager.getString("label.font_too_small");
      JvOptionPane.showInternalMessageDialog(this, message,
              MessageManager.getString("label.invalid_font"),
              JvOptionPane.WARNING_MESSAGE);
      /*
       * Restore the changed value - note this will reinvoke this method via the
       * ActionListener, but now validation should pass
       */
      if (lastSelected.getSize() != (Integer) fontSize.getSelectedItem()) // autoboxing
      {
        fontSize.setSelectedItem(lastSelected.getSize());
      }
      if (!lastSelected.getName()
              .equals(fontName.getSelectedItem().toString()))
      {
        fontName.setSelectedItem(lastSelected.getName());
      }
      if (lastSelected.getStyle() != fontStyle.getSelectedIndex())
      {
        fontStyle.setSelectedIndex(lastSelected.getStyle());
      }
      if (lastSelMono != monospaced.isSelected())
      {
        monospaced.setSelected(lastSelMono);
      }
      return;
    }
    if (isTreeFont())
    {
      tp.setTreeFont(newFont);
    }
    else if (ap != null)
    {
      ap.av.setFont(newFont, true);
      ap.fontChanged();

      /*
       * adjust other half of split frame if present, whether or not same font or
       * scale to cDNA is selected, because a font change may affect character
       * width, and this is kept the same in both panels
       */
      if (fontAsCdna.isVisible())
      {
        if (fontAsCdna.isSelected())
        {
          ap.av.getCodingComplement().setFont(newFont, true);
        }

        SplitFrame splitFrame = (SplitFrame) ap.alignFrame
                .getSplitViewContainer();
        splitFrame.adjustLayout();
        splitFrame.repaint();
      }
    }
    else if (isCapFont())
    {
      cap.setFont(newFont);
    }

    monospaced.setSelected(mw == iw);

    /*
     * Remember latest valid selection, so it can be restored if followed by an
     * invalid one
     */
    lastSelected = newFont;
  }

  /**
   * Updates on change of selected font name
   */
  @Override
  protected void fontName_actionPerformed()
  {
    if (init)
    {
      return;
    }

    changeFont();
  }

  /**
   * Updates on change of selected font size
   */
  @Override
  protected void fontSize_actionPerformed()
  {
    if (init)
    {
      return;
    }

    changeFont();
  }

  /**
   * Updates on change of selected font style
   */
  @Override
  protected void fontStyle_actionPerformed()
  {
    if (init)
    {
      return;
    }

    changeFont();
  }

  /**
   * Make selected settings the defaults by storing them (via Cache class) in
   * the .jalview_properties file (the file is only written when Jalview exits)
   */
  @Override
  public void defaultButton_actionPerformed()
  {
    Cache.setProperty("FONT_NAME", fontName.getSelectedItem().toString());
    Cache.setProperty("FONT_STYLE", fontStyle.getSelectedIndex() + "");
    Cache.setProperty("FONT_SIZE", fontSize.getSelectedItem().toString());
    Cache.setProperty("ANTI_ALIAS",
            Boolean.toString(smoothFont.isSelected()));
    Cache.setProperty(Preferences.SCALE_PROTEIN_TO_CDNA,
            Boolean.toString(scaleAsCdna.isSelected()));
  }

  /**
   * Turn on/off scaling of protein characters to 3 times the width of cDNA
   * characters
   */
  @Override
  protected void scaleAsCdna_actionPerformed()
  {
    ap.av.setScaleProteinAsCdna(scaleAsCdna.isSelected());
    ap.av.getCodingComplement()
            .setScaleProteinAsCdna(scaleAsCdna.isSelected());
    final SplitFrame splitFrame = (SplitFrame) ap.alignFrame
            .getSplitViewContainer();
    splitFrame.adjustLayout();
    splitFrame.repaint();
  }

  /**
   * Turn on/off mirroring of font across split frame. If turning on, also
   * copies the current font across the split frame. If turning off, restores
   * the other half of the split frame to its initial font.
   */
  @Override
  protected void mirrorFonts_actionPerformed()
  {
    boolean selected = fontAsCdna.isSelected();
    ap.av.setProteinFontAsCdna(selected);
    ap.av.getCodingComplement().setProteinFontAsCdna(selected);

    /*
     * reset other half of split frame if turning option off
     */
    if (!selected)
    {
      ap.av.getCodingComplement().setFont(oldComplementFont, true);
    }

    changeFont();
  }
}
