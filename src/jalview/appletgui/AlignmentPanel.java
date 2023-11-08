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

import jalview.analysis.AnnotationSorter;
import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.bin.JalviewLite;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceI;
import jalview.structure.StructureSelectionManager;
import jalview.viewmodel.ViewportListenerI;
import jalview.viewmodel.ViewportRanges;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;

@SuppressWarnings("serial")
public class AlignmentPanel extends Panel
        implements AdjustmentListener, AlignmentViewPanel, ViewportListenerI
{

  public AlignViewport av;

  OverviewPanel overviewPanel;

  SeqPanel seqPanel;

  IdPanel idPanel;

  IdwidthAdjuster idwidthAdjuster;

  public AlignFrame alignFrame;

  ScalePanel scalePanel;

  AnnotationPanel annotationPanel;

  AnnotationLabels alabels;

  ViewportRanges vpRanges;

  // this value is set false when selection area being dragged
  boolean fastPaint = true;

  public AlignmentPanel(AlignFrame af, final AlignViewport av)
  {
    try
    {
      jbInit();
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    alignFrame = af;
    this.av = av;
    vpRanges = av.getRanges();
    seqPanel = new SeqPanel(av, this);
    idPanel = new IdPanel(av, this);
    scalePanel = new ScalePanel(av, this);
    idwidthAdjuster = new IdwidthAdjuster(this);
    annotationPanel = new AnnotationPanel(this);
    annotationPanelHolder.add(annotationPanel, BorderLayout.CENTER);

    sequenceHolderPanel.add(annotationPanelHolder, BorderLayout.SOUTH);
    alabels = new AnnotationLabels(this);

    setAnnotationVisible(av.isShowAnnotation());

    idPanelHolder.add(idPanel, BorderLayout.CENTER);
    idSpaceFillerPanel1.add(idwidthAdjuster, BorderLayout.CENTER);
    annotationSpaceFillerHolder.add(alabels, BorderLayout.CENTER);
    scalePanelHolder.add(scalePanel, BorderLayout.CENTER);
    seqPanelHolder.add(seqPanel, BorderLayout.CENTER);

    fontChanged();
    setScrollValues(0, 0);

    apvscroll.addAdjustmentListener(this);
    hscroll.addAdjustmentListener(this);
    vscroll.addAdjustmentListener(this);

    addComponentListener(new ComponentAdapter()
    {
      @Override
      public void componentResized(ComponentEvent evt)
      {
        // reset the viewport ranges when the alignment panel is resized
        // in particular, this initialises the end residue value when Jalview
        // is initialised
        if (av.getWrapAlignment())
        {
          int widthInRes = seqPanel.seqCanvas
                  .getWrappedCanvasWidth(seqPanel.seqCanvas.getWidth());
          vpRanges.setViewportWidth(widthInRes);
        }
        else
        {
          int widthInRes = seqPanel.seqCanvas.getWidth()
                  / av.getCharWidth();
          int heightInSeq = seqPanel.seqCanvas.getHeight()
                  / av.getCharHeight();

          vpRanges.setViewportWidth(widthInRes);
          vpRanges.setViewportHeight(heightInSeq);
        }
        // setScrollValues(vpRanges.getStartRes(), vpRanges.getStartSeq());
        if (getSize().height > 0
                && annotationPanelHolder.getSize().height > 0)
        {
          validateAnnotationDimensions(false);
        }
        repaint();
      }

    });

    Dimension d = calculateIdWidth();
    idPanel.idCanvas.setSize(d);

    hscrollFillerPanel.setSize(d.width, annotationPanel.getSize().height);

    idPanel.idCanvas.setSize(d.width, seqPanel.seqCanvas.getSize().height);
    annotationSpaceFillerHolder.setSize(d.width,
            annotationPanel.getSize().height);
    alabels.setSize(d.width, annotationPanel.getSize().height);
    final AlignmentPanel ap = this;
    av.addPropertyChangeListener(new java.beans.PropertyChangeListener()
    {
      @Override
      public void propertyChange(java.beans.PropertyChangeEvent evt)
      {
        if (evt.getPropertyName().equals("alignment"))
        {
          PaintRefresher.Refresh(ap, av.getSequenceSetId(), true, true);
          alignmentChanged();
        }
      }
    });
    av.getRanges().addPropertyChangeListener(this);
  }

  @Override
  public AlignViewportI getAlignViewport()
  {
    return av;
  }

  public SequenceRenderer getSequenceRenderer()
  {
    return seqPanel.seqCanvas.sr;
  }

  @Override
  public jalview.api.FeatureRenderer getFeatureRenderer()
  {
    return seqPanel.seqCanvas.fr;
  }

  @Override
  public jalview.api.FeatureRenderer cloneFeatureRenderer()
  {
    FeatureRenderer nfr = new FeatureRenderer(av);
    nfr.transferSettings(seqPanel.seqCanvas.fr);
    return nfr;
  }

  public void alignmentChanged()
  {
    av.alignmentChanged(this);

    if (overviewPanel != null)
    {
      overviewPanel.updateOverviewImage();
    }

    alignFrame.updateEditMenuBar();

    repaint();
  }

  public void fontChanged()
  {
    // set idCanvas bufferedImage to null
    // to prevent drawing old image
    idPanel.idCanvas.image = null;
    FontMetrics fm = getFontMetrics(av.getFont());

    scalePanel.setSize(
            new Dimension(10, av.getCharHeight() + fm.getDescent()));
    idwidthAdjuster.setSize(
            new Dimension(10, av.getCharHeight() + fm.getDescent()));
    av.updateSequenceIdColours();
    annotationPanel.image = null;
    int ap = annotationPanel.adjustPanelHeight(false);
    Dimension d = calculateIdWidth();
    d.setSize(d.width + 4, seqPanel.seqCanvas.getSize().height);
    alabels.setSize(d.width + 4, ap);

    idPanel.idCanvas.setSize(d);
    hscrollFillerPanel.setSize(d);

    validateAnnotationDimensions(false);
    annotationPanel.repaint();
    validate();
    repaint();
  }

  public void setIdWidth(int w, int h)
  {
    idPanel.idCanvas.setSize(w, h);
    idPanelHolder.setSize(w, idPanelHolder.getSize().height);
    annotationSpaceFillerHolder.setSize(w,
            annotationSpaceFillerHolder.getSize().height);
    alabels.setSize(w, alabels.getSize().height);
    validate();
  }

  Dimension calculateIdWidth()
  {
    if (av.nullFrame == null)
    {
      av.nullFrame = new Frame();
      av.nullFrame.addNotify();
    }

    Graphics g = av.nullFrame.getGraphics();

    FontMetrics fm = g.getFontMetrics(av.font);
    AlignmentI al = av.getAlignment();

    int i = 0;
    int idWidth = 0;
    String id;
    while (i < al.getHeight() && al.getSequenceAt(i) != null)
    {
      SequenceI s = al.getSequenceAt(i);
      id = s.getDisplayId(av.getShowJVSuffix());

      if (fm.stringWidth(id) > idWidth)
      {
        idWidth = fm.stringWidth(id);
      }
      i++;
    }

    // Also check annotation label widths
    i = 0;
    if (al.getAlignmentAnnotation() != null)
    {
      fm = g.getFontMetrics(av.nullFrame.getFont());
      while (i < al.getAlignmentAnnotation().length)
      {
        String label = al.getAlignmentAnnotation()[i].label;
        if (fm.stringWidth(label) > idWidth)
        {
          idWidth = fm.stringWidth(label);
        }
        i++;
      }
    }

    return new Dimension(idWidth, idPanel.idCanvas.getSize().height);
  }

  /**
   * Highlight the given results on the alignment.
   * 
   */
  public void highlightSearchResults(SearchResultsI results)
  {
    scrollToPosition(results);
    seqPanel.seqCanvas.highlightSearchResults(results);
  }

  /**
   * scroll the view to show the position of the highlighted region in results
   * (if any) and redraw the overview
   * 
   * @param results
   * @return false if results were not found
   */
  public boolean scrollToPosition(SearchResultsI results)
  {
    return scrollToPosition(results, true);
  }

  /**
   * scroll the view to show the position of the highlighted region in results
   * (if any)
   * 
   * @param results
   * @param redrawOverview
   *          - when set, the overview will be recalculated (takes longer)
   * @return false if results were not found
   */
  public boolean scrollToPosition(SearchResultsI results,
          boolean redrawOverview)
  {
    return scrollToPosition(results, 0, redrawOverview, false);
  }

  /**
   * scroll the view to show the position of the highlighted region in results
   * (if any)
   * 
   * @param results
   * @param redrawOverview
   *          - when set, the overview will be recalculated (takes longer)
   * @return false if results were not found
   */
  public boolean scrollToPosition(SearchResultsI results,
          int verticalOffset, boolean redrawOverview, boolean centre)
  {
    // do we need to scroll the panel?
    if (results != null && results.getCount() > 0)
    {
      AlignmentI alignment = av.getAlignment();
      int seqIndex = alignment.findIndex(results);
      if (seqIndex == -1)
      {
        return false;
      }
      /*
       * allow for offset of target sequence (actually scroll to one above it)
       */

      SequenceI seq = alignment.getSequenceAt(seqIndex);
      int[] r = results.getResults(seq, 0, alignment.getWidth());
      if (r == null)
      {
        if (JalviewLite.debug)
        {// DEBUG
          System.out.println(
                  "DEBUG: scroll didn't happen - results not within alignment : "
                          + seq.getStart() + "," + seq.getEnd());
        }
        return false;
      }
      if (JalviewLite.debug)
      {
        // DEBUG
        /*
         * System.out.println("DEBUG: scroll: start=" + r[0] +
         * " av.getStartRes()=" + av.getStartRes() + " end=" + r[1] +
         * " seq.end=" + seq.getEnd() + " av.getEndRes()=" + av.getEndRes() +
         * " hextent=" + hextent);
         */
      }
      int start = r[0];
      int end = r[1];

      /*
       * To centre results, scroll to positions half the visible width
       * left/right of the start/end positions
       */
      if (centre)
      {
        int offset = (vpRanges.getEndRes() - vpRanges.getStartRes() + 1) / 2
                - 1;
        start = Math.max(start - offset, 0);
        end = end + offset - 1;
        // end = Math.min(end + offset, seq.getEnd() - 1);
      }

      if (start < 0)
      {
        return false;
      }
      if (end == seq.getEnd())
      {
        return false;
      }

      /*
       * allow for offset of target sequence (actually scroll to one above it)
       */
      seqIndex = Math.max(0, seqIndex - verticalOffset);
      return scrollTo(start, end, seqIndex, false, redrawOverview);
    }
    return true;
  }

  public boolean scrollTo(int ostart, int end, int seqIndex,
          boolean scrollToNearest, boolean redrawOverview)
  {
    int startv, endv, starts, ends;// , width;

    int start = -1;
    if (av.hasHiddenColumns())
    {
      AlignmentI al = av.getAlignment();
      start = al.getHiddenColumns().absoluteToVisibleColumn(ostart);
      end = al.getHiddenColumns().absoluteToVisibleColumn(end);
      if (start == end)
      {
        if (!scrollToNearest && !al.getHiddenColumns().isVisible(ostart))
        {
          // don't scroll - position isn't visible
          return false;
        }
      }
    }
    else
    {
      start = ostart;
    }

    if (!av.getWrapAlignment())
    {
      /*
       * int spos=av.getStartRes(),sqpos=av.getStartSeq(); if ((startv =
       * av.getStartRes()) >= start) { spos=start-1; // seqIn //
       * setScrollValues(start - 1, seqIndex); } else if ((endv =
       * av.getEndRes()) <= end) { // setScrollValues(spos=startv + 1 + end -
       * endv, seqIndex); spos=startv + 1 + end - endv; } else if ((starts =
       * av.getStartSeq()) > seqIndex) { setScrollValues(av.getStartRes(),
       * seqIndex); } else if ((ends = av.getEndSeq()) <= seqIndex) {
       * setScrollValues(av.getStartRes(), starts + seqIndex - ends + 1); }
       */

      // below is scrolling logic up to Jalview 2.8.2
      // if ((av.getStartRes() > end)
      // || (av.getEndRes() < start)
      // || ((av.getStartSeq() > seqIndex) || (av.getEndSeq() < seqIndex)))
      // {
      // if (start > av.getAlignment().getWidth() - hextent)
      // {
      // start = av.getAlignment().getWidth() - hextent;
      // if (start < 0)
      // {
      // start = 0;
      // }
      //
      // }
      // if (seqIndex > av.getAlignment().getHeight() - vextent)
      // {
      // seqIndex = av.getAlignment().getHeight() - vextent;
      // if (seqIndex < 0)
      // {
      // seqIndex = 0;
      // }
      // }
      // setScrollValues(start, seqIndex);
      // }
      // logic copied from jalview.gui.AlignmentPanel:
      if ((startv = vpRanges.getStartRes()) >= start)
      {
        /*
         * Scroll left to make start of search results visible
         */
        setScrollValues(start - 1, seqIndex);
      }
      else if ((endv = vpRanges.getEndRes()) <= end)
      {
        /*
         * Scroll right to make end of search results visible
         */
        setScrollValues(startv + 1 + end - endv, seqIndex);
      }
      else if ((starts = vpRanges.getStartSeq()) > seqIndex)
      {
        /*
         * Scroll up to make start of search results visible
         */
        setScrollValues(vpRanges.getStartRes(), seqIndex);
      }
      else if ((ends = vpRanges.getEndSeq()) <= seqIndex)
      {
        /*
         * Scroll down to make end of search results visible
         */
        setScrollValues(vpRanges.getStartRes(),
                starts + seqIndex - ends + 1);
      }
      /*
       * Else results are already visible - no need to scroll
       */
    }
    else
    {
      vpRanges.scrollToWrappedVisible(start);
    }

    paintAlignment(redrawOverview, false);
    return true;
  }

  public OverviewPanel getOverviewPanel()
  {
    return overviewPanel;
  }

  public void setOverviewPanel(OverviewPanel op)
  {
    overviewPanel = op;
  }

  public void setAnnotationVisible(boolean b)
  {
    if (!av.getWrapAlignment())
    {
      annotationSpaceFillerHolder.setVisible(b);
      annotationPanelHolder.setVisible(b);
    }
    else
    {
      annotationSpaceFillerHolder.setVisible(false);
      annotationPanelHolder.setVisible(false);
    }
    validate();
    repaint();
  }

  /**
   * automatically adjust annotation panel height for new annotation whilst
   * ensuring the alignment is still visible.
   */
  @Override
  public void adjustAnnotationHeight()
  {
    // TODO: display vertical annotation scrollbar if necessary
    // this is called after loading new annotation onto alignment
    if (alignFrame.getSize().height == 0)
    {
      System.out.println(
              "adjustAnnotationHeight frame size zero NEEDS FIXING");
    }
    fontChanged();
    validateAnnotationDimensions(true);
    apvscroll.addNotify();
    hscroll.addNotify();
    validate();
    paintAlignment(true, false);
  }

  /**
   * Calculate the annotation dimensions and refresh slider values accordingly.
   * Need to do repaints/notifys afterwards.
   */
  protected void validateAnnotationDimensions(boolean adjustPanelHeight)
  {
    int rowHeight = av.getCharHeight();
    int alignmentHeight = rowHeight * av.getAlignment().getHeight();
    int annotationHeight = av.calcPanelHeight();

    int mheight = annotationHeight;
    Dimension d = sequenceHolderPanel.getSize();

    int availableHeight = d.height - scalePanelHolder.getHeight();

    if (adjustPanelHeight)
    {
      /*
       * If not enough vertical space, maximize annotation height while keeping
       * at least two rows of alignment visible
       */
      if (annotationHeight + alignmentHeight > availableHeight)
      {
        annotationHeight = Math.min(annotationHeight,
                availableHeight - 2 * rowHeight);
      }
    }
    else
    {
      // maintain same window layout whilst updating sliders
      annotationHeight = annotationPanelHolder.getSize().height;
    }

    if (availableHeight - annotationHeight < 5)
    {
      annotationHeight = availableHeight;
    }

    annotationPanel.setSize(new Dimension(d.width, annotationHeight));
    annotationPanelHolder.setSize(new Dimension(d.width, annotationHeight));
    // seqPanelHolder.setSize(d.width, seqandannot - height);
    seqPanel.seqCanvas.setSize(d.width,
            seqPanel.seqCanvas.getSize().height);

    Dimension e = idPanel.getSize();
    alabels.setSize(new Dimension(e.width, annotationHeight));
    annotationSpaceFillerHolder
            .setSize(new Dimension(e.width, annotationHeight));

    int s = apvscroll.getValue();
    if (s > mheight - annotationHeight)
    {
      s = 0;
    }
    apvscroll.setValues(s, annotationHeight, 0, mheight);
    annotationPanel.setScrollOffset(apvscroll.getValue(), false);
    alabels.setScrollOffset(apvscroll.getValue(), false);
  }

  public void setWrapAlignment(boolean wrap)
  {
    vpRanges.setStartEndSeq(0, vpRanges.getVisibleAlignmentHeight());
    vpRanges.setStartRes(0);
    scalePanelHolder.setVisible(!wrap);

    hscroll.setVisible(!wrap);
    idwidthAdjuster.setVisible(!wrap);

    if (wrap)
    {
      annotationPanelHolder.setVisible(false);
      annotationSpaceFillerHolder.setVisible(false);
    }
    else if (av.isShowAnnotation())
    {
      annotationPanelHolder.setVisible(true);
      annotationSpaceFillerHolder.setVisible(true);
    }

    idSpaceFillerPanel1.setVisible(!wrap);

    fontChanged(); // This is so that the scalePanel is resized correctly

    validate();
    sequenceHolderPanel.validate();
    repaint();

  }

  int hextent = 0;

  int vextent = 0;

  public void setScrollValues(int xpos, int ypos)
  {
    int x = xpos;
    int y = ypos;

    if (av.getWrapAlignment())
    {
      setScrollingForWrappedPanel(x);
    }
    else
    {
      int width = av.getAlignment().getVisibleWidth();
      int height = av.getAlignment().getHeight();

      if (x < 0)
      {
        x = 0;
      }

      hextent = seqPanel.seqCanvas.getSize().width / av.getCharWidth();
      vextent = seqPanel.seqCanvas.getSize().height / av.getCharHeight();

      if (hextent > width)
      {
        hextent = width;
      }

      if (vextent > height)
      {
        vextent = height;
      }

      if ((hextent + x) > width)
      {
        System.err.println("hextent was " + hextent + " and x was " + x);

        x = width - hextent;
      }

      if ((vextent + y) > height)
      {
        y = height - vextent;
      }

      if (y < 0)
      {
        y = 0;
      }

      if (x < 0)
      {
        System.err.println("x was " + x);
        x = 0;
      }

      hscroll.setValues(x, hextent, 0, width);
      vscroll.setValues(y, vextent, 0, height);

      // AWT scrollbar does not fire adjustmentValueChanged for setValues
      // so also call adjustment code!
      adjustHorizontal(x);
      adjustVertical(y);

      sendViewPosition();
    }
  }

  /**
   * Respond to adjustment event when horizontal or vertical scrollbar is
   * changed
   * 
   * @param evt
   *          adjustment event encoding whether apvscroll, hscroll or vscroll
   *          changed
   */
  @Override
  public void adjustmentValueChanged(AdjustmentEvent evt)
  {
    // Note that this event is NOT fired by the AWT scrollbar when setValues is
    // called. Instead manually call adjustHorizontal and adjustVertical
    // directly.
    if (evt == null || evt.getSource() == apvscroll)
    {
      annotationPanel.setScrollOffset(apvscroll.getValue(), false);
      alabels.setScrollOffset(apvscroll.getValue(), false);
    }
    if (evt == null || evt.getSource() == hscroll)
    {
      int x = hscroll.getValue();
      adjustHorizontal(x);
    }

    if (evt == null || evt.getSource() == vscroll)
    {
      int offy = vscroll.getValue();
      adjustVertical(offy);
    }

  }

  private void adjustHorizontal(int x)
  {
    int oldX = vpRanges.getStartRes();
    int oldwidth = vpRanges.getViewportWidth();
    int width = seqPanel.seqCanvas.getWidth() / av.getCharWidth();

    // if we're scrolling to the position we're already at, stop
    // this prevents infinite recursion of events when the scroll/viewport
    // ranges values are the same
    if ((x == oldX) && (width == oldwidth))
    {
      return;
    }
    vpRanges.setViewportStartAndWidth(x, width);

    if (av.getWrapAlignment() || !fastPaint)
    {
      repaint();
    }
    sendViewPosition();
  }

  private void adjustVertical(int newY)
  {
    if (av.getWrapAlignment())
    {
      /*
       * if we're scrolling to the position we're already at, stop
       * this prevents infinite recursion of events when the scroll/viewport
       * ranges values are the same
       */
      int oldX = vpRanges.getStartRes();
      int oldY = vpRanges.getWrappedScrollPosition(oldX);
      if (oldY == newY)
      {
        return;
      }
      if (newY > -1)
      {
        /*
         * limit page up/down to one width's worth of positions
         */
        int rowSize = vpRanges.getViewportWidth();
        int newX = newY > oldY ? oldX + rowSize : oldX - rowSize;
        vpRanges.setViewportStartAndWidth(Math.max(0, newX), rowSize);
      }
    }
    else
    {
      int height = seqPanel.seqCanvas.getHeight() / av.getCharHeight();
      int oldY = vpRanges.getStartSeq();
      int oldheight = vpRanges.getViewportHeight();

      // if we're scrolling to the position we're already at, stop
      // this prevents infinite recursion of events when the scroll/viewport
      // ranges values are the same
      if ((newY == oldY) && (height == oldheight))
      {
        return;
      }
      vpRanges.setViewportStartAndHeight(newY, height);
    }
    if (av.getWrapAlignment() || !fastPaint)
    {
      repaint();
    }
    sendViewPosition();
  }

  /**
   * A helper method to return the AlignmentPanel in the other (complementary)
   * half of a SplitFrame view. Returns null if not in a SplitFrame.
   * 
   * @return
   */
  private AlignmentPanel getComplementPanel()
  {
    AlignmentPanel ap = null;
    if (alignFrame != null)
    {
      SplitFrame sf = alignFrame.getSplitFrame();
      if (sf != null)
      {
        AlignFrame other = sf.getComplement(alignFrame);
        if (other != null)
        {
          ap = other.alignPanel;
        }
      }
    }
    return ap;
  }

  /**
   * Follow a scrolling change in the (cDNA/Protein) complementary alignment.
   * The aim is to keep the two alignments 'lined up' on their centre columns.
   * 
   * @param sr
   *          holds mapped region(s) of this alignment that we are scrolling
   *          'to'; may be modified for sequence offset by this method
   * @param seqOffset
   *          the number of visible sequences to show above the mapped region
   */
  protected void scrollToCentre(SearchResultsI sr, int seqOffset)
  {
    /*
     * To avoid jumpy vertical scrolling (if some sequences are gapped or not
     * mapped), we can make the scroll-to location a sequence above the one
     * actually mapped.
     */
    SequenceI mappedTo = sr.getResults().get(0).getSequence();
    List<SequenceI> seqs = av.getAlignment().getSequences();

    /*
     * This is like AlignmentI.findIndex(seq) but here we are matching the
     * dataset sequence not the aligned sequence
     */
    boolean matched = false;
    for (SequenceI seq : seqs)
    {
      if (mappedTo == seq.getDatasetSequence())
      {
        matched = true;
        break;
      }
    }
    if (!matched)
    {
      return; // failsafe, shouldn't happen
    }

    /*
     * Scroll to position but centring the target residue. Also set a state flag
     * to prevent adjustmentValueChanged performing this recursively.
     */
    scrollToPosition(sr, seqOffset, true, true);
  }

  private void sendViewPosition()
  {
    StructureSelectionManager.getStructureSelectionManager(av.applet)
            .sendViewPosition(this, vpRanges.getStartRes(),
                    vpRanges.getEndRes(), vpRanges.getStartSeq(),
                    vpRanges.getEndSeq());
  }

  /**
   * Repaint the alignment and annotations, and, optionally, any overview window
   */
  @Override
  public void paintAlignment(boolean updateOverview,
          boolean updateStructures)
  {
    final AnnotationSorter sorter = new AnnotationSorter(getAlignment(),
            av.isShowAutocalculatedAbove());
    sorter.sort(getAlignment().getAlignmentAnnotation(),
            av.getSortAnnotationsBy());
    repaint();

    if (updateStructures)
    {
      jalview.structure.StructureSelectionManager
              .getStructureSelectionManager(av.applet)
              .sequenceColoursChanged(this);
    }
    if (updateOverview)
    {
      if (overviewPanel != null)
      {
        overviewPanel.updateOverviewImage();
      }
    }
  }

  @Override
  public void update(Graphics g)
  {
    paint(g);
  }

  @Override
  public void paint(Graphics g)
  {
    invalidate();
    Dimension d = idPanel.idCanvas.getSize();
    final int canvasHeight = seqPanel.seqCanvas.getSize().height;
    if (canvasHeight != d.height)
    {
      idPanel.idCanvas.setSize(d.width, canvasHeight);
    }

    setScrollValues(vpRanges.getStartRes(), vpRanges.getStartSeq());

    seqPanel.seqCanvas.repaint();
    idPanel.idCanvas.repaint();
    if (!av.getWrapAlignment())
    {
      if (av.isShowAnnotation())
      {
        alabels.repaint();
        annotationPanel.repaint();
      }
      scalePanel.repaint();
    }

  }

  /**
   * Set vertical scroll bar parameters for wrapped panel
   * 
   * @param topLeftColumn
   *          the column position at top left (0..)
   */
  private void setScrollingForWrappedPanel(int topLeftColumn)
  {
    int scrollPosition = vpRanges.getWrappedScrollPosition(topLeftColumn);
    int maxScroll = vpRanges.getWrappedMaxScroll(topLeftColumn);

    /*
     * a scrollbar's value can be set to at most (maximum-extent)
     * so we add extent (1) to the maxScroll value
     */
    vscroll.setUnitIncrement(1);
    vscroll.setValues(scrollPosition, 1, 0, maxScroll + 1);
  }

  protected Panel sequenceHolderPanel = new Panel();

  protected Scrollbar vscroll = new Scrollbar();

  protected Scrollbar hscroll = new Scrollbar();

  protected Panel seqPanelHolder = new Panel();

  protected Panel scalePanelHolder = new Panel();

  protected Panel idPanelHolder = new Panel();

  protected Panel idSpaceFillerPanel1 = new Panel();

  public Panel annotationSpaceFillerHolder = new Panel();

  protected Panel hscrollFillerPanel = new Panel();

  Panel annotationPanelHolder = new Panel();

  protected Scrollbar apvscroll = new Scrollbar();

  /*
   * Flag set while scrolling to follow complementary cDNA/protein scroll. When
   * false, suppresses invoking the same method recursively.
   */
  private boolean scrollComplementaryPanel = true;

  private void jbInit() throws Exception
  {
    // idPanelHolder.setPreferredSize(new Dimension(70, 10));
    this.setLayout(new BorderLayout());

    // sequenceHolderPanel.setPreferredSize(new Dimension(150, 150));
    sequenceHolderPanel.setLayout(new BorderLayout());
    seqPanelHolder.setLayout(new BorderLayout());
    scalePanelHolder.setBackground(Color.white);

    // scalePanelHolder.setPreferredSize(new Dimension(10, 30));
    scalePanelHolder.setLayout(new BorderLayout());
    idPanelHolder.setLayout(new BorderLayout());
    idSpaceFillerPanel1.setBackground(Color.white);

    // idSpaceFillerPanel1.setPreferredSize(new Dimension(10, 30));
    idSpaceFillerPanel1.setLayout(new BorderLayout());
    annotationSpaceFillerHolder.setBackground(Color.white);

    // annotationSpaceFillerHolder.setPreferredSize(new Dimension(10, 80));
    annotationSpaceFillerHolder.setLayout(new BorderLayout());
    hscroll.setOrientation(Scrollbar.HORIZONTAL);

    Panel hscrollHolder = new Panel();
    hscrollHolder.setLayout(new BorderLayout());
    hscrollFillerPanel.setBackground(Color.white);
    apvscroll.setOrientation(Scrollbar.VERTICAL);
    apvscroll.setVisible(true);
    apvscroll.addAdjustmentListener(this);

    annotationPanelHolder.setBackground(Color.white);
    annotationPanelHolder.setLayout(new BorderLayout());
    annotationPanelHolder.add(apvscroll, BorderLayout.EAST);
    // hscrollFillerPanel.setPreferredSize(new Dimension(70, 10));
    hscrollHolder.setBackground(Color.white);

    // annotationScroller.setPreferredSize(new Dimension(10, 80));
    // this.setPreferredSize(new Dimension(220, 166));
    seqPanelHolder.setBackground(Color.white);
    idPanelHolder.setBackground(Color.white);
    sequenceHolderPanel.add(scalePanelHolder, BorderLayout.NORTH);
    sequenceHolderPanel.add(seqPanelHolder, BorderLayout.CENTER);
    seqPanelHolder.add(vscroll, BorderLayout.EAST);

    // Panel3.add(secondaryPanelHolder, BorderLayout.SOUTH);
    this.add(idPanelHolder, BorderLayout.WEST);
    idPanelHolder.add(idSpaceFillerPanel1, BorderLayout.NORTH);
    idPanelHolder.add(annotationSpaceFillerHolder, BorderLayout.SOUTH);
    this.add(hscrollHolder, BorderLayout.SOUTH);
    hscrollHolder.add(hscroll, BorderLayout.CENTER);
    hscrollHolder.add(hscrollFillerPanel, BorderLayout.WEST);
    this.add(sequenceHolderPanel, BorderLayout.CENTER);
  }

  /**
   * hides or shows dynamic annotation rows based on groups and av state flags
   */
  public void updateAnnotation()
  {
    updateAnnotation(false);
  }

  public void updateAnnotation(boolean applyGlobalSettings)
  {
    updateAnnotation(applyGlobalSettings, false);
  }

  public void updateAnnotation(boolean applyGlobalSettings,
          boolean preserveNewGroupSettings)
  {
    av.updateGroupAnnotationSettings(applyGlobalSettings,
            preserveNewGroupSettings);
    adjustAnnotationHeight();
  }

  @Override
  public AlignmentI getAlignment()
  {
    return av.getAlignment();
  }

  @Override
  public String getViewName()
  {
    return getName();
  }

  @Override
  public StructureSelectionManager getStructureSelectionManager()
  {
    return StructureSelectionManager
            .getStructureSelectionManager(av.applet);
  }

  @Override
  public void raiseOOMWarning(String string, OutOfMemoryError error)
  {
    // TODO: JAL-960
    System.err.println("Out of memory whilst '" + string + "'");
    error.printStackTrace();
  }

  /**
   * Set a flag to say we are scrolling to follow a (cDNA/protein) complement.
   * 
   * @param b
   */
  protected void setToScrollComplementPanel(boolean b)
  {
    this.scrollComplementaryPanel = b;
  }

  /**
   * Get whether to scroll complement panel
   * 
   * @return true if cDNA/protein complement panels should be scrolled
   */
  protected boolean isSetToScrollComplementPanel()
  {
    return this.scrollComplementaryPanel;
  }

  @Override
  /**
   * Property change event fired when a change is made to the viewport ranges
   * object associated with this alignment panel's viewport
   */
  public void propertyChange(PropertyChangeEvent evt)
  {
    // update this panel's scroll values based on the new viewport ranges values
    int x = vpRanges.getStartRes();
    int y = vpRanges.getStartSeq();
    setScrollValues(x, y);

    // now update any complementary alignment (its viewport ranges object
    // is different so does not get automatically updated)
    if (isSetToScrollComplementPanel())
    {
      setToScrollComplementPanel(false);
      av.scrollComplementaryAlignment(getComplementPanel());
      setToScrollComplementPanel(true);
    }

  }

}
