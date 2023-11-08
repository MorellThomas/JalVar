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
package jalview.appletgui;

import jalview.api.AlignmentViewPanel;
import jalview.api.ViewStyleI;
import jalview.bin.JalviewLite;
import jalview.datamodel.AlignmentI;
import jalview.structure.StructureSelectionManager;
import jalview.viewmodel.AlignmentViewport;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Panel;

public class SplitFrame extends EmbmenuFrame
{
  private static final long serialVersionUID = 1L;

  private AlignFrame topFrame;

  private AlignFrame bottomFrame;

  private Panel outermost;

  /**
   * Constructs the split frame placing cdna in the top half. No 'alignment' is
   * performed here, this should be done by the calling client if wanted.
   */
  public SplitFrame(AlignFrame af1, AlignFrame af2)
  {
    boolean af1IsNucleotide = af1.viewport.getAlignment().isNucleotide();
    topFrame = af1IsNucleotide ? af1 : af2;
    bottomFrame = topFrame == af1 ? af2 : af1;
    init();
  }

  /**
   * Creates a Panel containing two Panels, and adds the first and second
   * AlignFrame's components to each. At this stage we have not yet committed to
   * whether the enclosing panel will be added to this frame, for display as a
   * separate frame, or added to the applet (embedded mode).
   */
  public void init()
  {
    constructSplit();

    /*
     * Try to make and add dna/protein sequence mappings
     */
    final AlignViewport topViewport = topFrame.viewport;
    final AlignViewport bottomViewport = bottomFrame.viewport;
    final AlignmentI topAlignment = topViewport.getAlignment();
    final AlignmentI bottomAlignment = bottomViewport.getAlignment();
    AlignmentViewport cdna = topAlignment.isNucleotide() ? topViewport
            : (bottomAlignment.isNucleotide() ? bottomViewport : null);
    AlignmentViewport protein = !topAlignment.isNucleotide() ? topViewport
            : (!bottomAlignment.isNucleotide() ? bottomViewport : null);

    final StructureSelectionManager ssm = StructureSelectionManager
            .getStructureSelectionManager(topViewport.applet);
    ssm.registerMappings(protein.getAlignment().getCodonFrames());
    topViewport.setCodingComplement(bottomViewport);
    ssm.addCommandListener(cdna);
    ssm.addCommandListener(protein);

    /*
     * Compute cDNA consensus on protein alignment
     */
    protein.initComplementConsensus();
    AlignmentViewPanel ap = topAlignment.isNucleotide()
            ? bottomFrame.alignPanel
            : topFrame.alignPanel;
    protein.updateConsensus(ap);

    adjustLayout();
  }

  /**
   * 
   */
  protected void constructSplit()
  {
    setMenuBar(null);
    outermost = new Panel(new GridLayout(2, 1));

    Panel topPanel = new Panel();
    Panel bottomPanel = new Panel();
    outermost.add(topPanel);
    outermost.add(bottomPanel);

    addAlignFrameComponents(topFrame, topPanel);
    addAlignFrameComponents(bottomFrame, bottomPanel);
  }

  /**
   * Make any adjustments to the layout
   */
  protected void adjustLayout()
  {
    AlignmentViewport cdna = topFrame.getAlignViewport().getAlignment()
            .isNucleotide() ? topFrame.viewport : bottomFrame.viewport;
    AlignmentViewport protein = cdna == topFrame.viewport
            ? bottomFrame.viewport
            : topFrame.viewport;

    /*
     * Ensure sequence ids are the same width for good alignment.
     */
    // TODO should do this via av.getViewStyle/setViewStyle
    // however at present av.viewStyle is not set in IdPanel.fontChanged
    int w1 = topFrame.alignPanel.idPanel.idCanvas.getWidth();
    int w2 = bottomFrame.alignPanel.idPanel.idCanvas.getWidth();
    int w3 = Math.max(w1, w2);
    if (w1 != w3)
    {
      Dimension d = topFrame.alignPanel.idPanel.idCanvas.getSize();
      topFrame.alignPanel.idPanel.idCanvas
              .setSize(new Dimension(w3, d.height));
    }
    if (w2 != w3)
    {
      Dimension d = bottomFrame.alignPanel.idPanel.idCanvas.getSize();
      bottomFrame.alignPanel.idPanel.idCanvas
              .setSize(new Dimension(w3, d.height));
    }

    /*
     * Scale protein to either 1 or 3 times character width of dna
     */
    if (protein != null && cdna != null)
    {
      ViewStyleI vs = protein.getViewStyle();
      int scale = vs.isScaleProteinAsCdna() ? 3 : 1;
      vs.setCharWidth(scale * cdna.getViewStyle().getCharWidth());
      protein.setViewStyle(vs);
    }
  }

  /**
   * Add the menu bar, alignment panel and status bar from the AlignFrame to the
   * panel. The menu bar is a panel 'reconstructed' from the AlignFrame's frame
   * menu bar. This allows each half of the SplitFrame to have its own menu bar.
   * 
   * @param af
   * @param panel
   */
  private void addAlignFrameComponents(AlignFrame af, Panel panel)
  {
    panel.setLayout(new BorderLayout());
    Panel menuPanel = af.makeEmbeddedPopupMenu(af.getMenuBar(), true,
            false);
    panel.add(menuPanel, BorderLayout.NORTH);
    panel.add(af.statusBar, BorderLayout.SOUTH);
    panel.add(af.alignPanel, BorderLayout.CENTER);

    af.setSplitFrame(this);
  }

  /**
   * Display the content panel either as a new frame or embedded in the applet.
   * 
   * @param embedded
   * @param applet
   */
  public void addToDisplay(boolean embedded, JalviewLite applet)
  {
    createSplitFrameWindow(embedded, applet);
    validate();
    topFrame.alignPanel.adjustAnnotationHeight();
    topFrame.alignPanel.paintAlignment(true, true);
    bottomFrame.alignPanel.adjustAnnotationHeight();
    bottomFrame.alignPanel.paintAlignment(true, true);
  }

  /**
   * Either show the content panel in this frame as a new frame, or (if
   * embed=true) add it to the applet container instead.
   * 
   * @param embed
   * @param applet
   */
  protected void createSplitFrameWindow(boolean embed, JalviewLite applet)
  {
    if (embed)
    {
      applet.add(outermost);
      applet.validate();
    }
    else
    {
      this.add(outermost);
      int width = Math.max(topFrame.frameWidth, bottomFrame.frameWidth);
      int height = topFrame.frameHeight + bottomFrame.frameHeight;
      jalview.bin.JalviewLite.addFrame(this, this.getTitle(), width,
              height);
    }
  }

  /**
   * Returns the contained AlignFrame complementary to the one given (or null if
   * no match to top or bottom component).
   * 
   * @param af
   * @return
   */
  public AlignFrame getComplement(AlignFrame af)
  {
    if (topFrame == af)
    {
      return bottomFrame;
    }
    else if (bottomFrame == af)
    {
      return topFrame;
    }
    return null;
  }
}
