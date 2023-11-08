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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import javax.swing.SwingUtilities;

import jalview.analysis.AnnotationSorter;
import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.bin.Jalview;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.gui.ImageExporter.ImageWriterI;
import jalview.io.HTMLOutput;
import jalview.io.exceptions.ImageOutputException;
import jalview.jbgui.GAlignmentPanel;
import jalview.math.AlignmentDimension;
import jalview.schemes.ResidueProperties;
import jalview.structure.StructureSelectionManager;
import jalview.util.Comparison;
import jalview.util.ImageMaker;
import jalview.util.MessageManager;
import jalview.util.imagemaker.BitmapImageSizing;
import jalview.viewmodel.ViewportListenerI;
import jalview.viewmodel.ViewportRanges;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision: 1.161 $
 */
@SuppressWarnings("serial")
public class AlignmentPanel extends GAlignmentPanel implements
        AdjustmentListener, Printable, AlignmentViewPanel, ViewportListenerI
{
  /*
   * spare space in pixels between sequence id and alignment panel
   */
  private static final int ID_WIDTH_PADDING = 4;

  public AlignViewport av;

  OverviewPanel overviewPanel;

  private SeqPanel seqPanel;

  private IdPanel idPanel;

  IdwidthAdjuster idwidthAdjuster;

  public AlignFrame alignFrame;

  private ScalePanel scalePanel;

  private AnnotationPanel annotationPanel;

  private AnnotationLabels alabels;

  private int hextent = 0;

  private int vextent = 0;

  /*
   * Flag set while scrolling to follow complementary cDNA/protein scroll. When
   * false, suppresses invoking the same method recursively.
   */
  private boolean scrollComplementaryPanel = true;

  private PropertyChangeListener propertyChangeListener;

  private CalculationChooser calculationDialog;

  /**
   * Creates a new AlignmentPanel object.
   * 
   * @param af
   * @param av
   */
  public AlignmentPanel(AlignFrame af, final AlignViewport av)
  {
    // setBackground(Color.white); // BH 2019
    alignFrame = af;
    this.av = av;
    setSeqPanel(new SeqPanel(av, this));
    setIdPanel(new IdPanel(av, this));

    setScalePanel(new ScalePanel(av, this));

    idPanelHolder.add(getIdPanel(), BorderLayout.CENTER);
    idwidthAdjuster = new IdwidthAdjuster(this);
    idSpaceFillerPanel1.add(idwidthAdjuster, BorderLayout.CENTER);

    setAnnotationPanel(new AnnotationPanel(this));
    setAlabels(new AnnotationLabels(this));

    annotationScroller.setViewportView(getAnnotationPanel());
    annotationSpaceFillerHolder.add(getAlabels(), BorderLayout.CENTER);

    scalePanelHolder.add(getScalePanel(), BorderLayout.CENTER);
    seqPanelHolder.add(getSeqPanel(), BorderLayout.CENTER);

    setScrollValues(0, 0);

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
        ViewportRanges ranges = av.getRanges();
        if (av.getWrapAlignment())
        {
          int widthInRes = getSeqPanel().seqCanvas.getWrappedCanvasWidth(
                  getSeqPanel().seqCanvas.getWidth());
          ranges.setViewportWidth(widthInRes);
        }
        else
        {
          int widthInRes = getSeqPanel().seqCanvas.getWidth()
                  / av.getCharWidth();
          int heightInSeq = getSeqPanel().seqCanvas.getHeight()
                  / av.getCharHeight();

          ranges.setViewportWidth(widthInRes);
          ranges.setViewportHeight(heightInSeq);
        }
      }

    });

    final AlignmentPanel ap = this;
    propertyChangeListener = new PropertyChangeListener()
    {
      @Override
      public void propertyChange(PropertyChangeEvent evt)
      {
        if (evt.getPropertyName().equals("alignment"))
        {
          PaintRefresher.Refresh(ap, av.getSequenceSetId(), true, true);
          alignmentChanged();
        }
      }
    };
    av.addPropertyChangeListener(propertyChangeListener);

    av.getRanges().addPropertyChangeListener(this);
    fontChanged();
    adjustAnnotationHeight();
    updateLayout();
  }

  @Override
  public AlignViewportI getAlignViewport()
  {
    return av;
  }

  public void alignmentChanged()
  {
    av.alignmentChanged(this);

    if (getCalculationDialog() != null)
    {
      getCalculationDialog().validateCalcTypes();
    }

    alignFrame.updateEditMenuBar();

    // no idea if we need to update structure
    paintAlignment(true, true);

  }

  /**
   * DOCUMENT ME!
   */
  public void fontChanged()
  {
    // set idCanvas bufferedImage to null
    // to prevent drawing old image
    FontMetrics fm = getFontMetrics(av.getFont());

    // update the flag controlling whether the grid is too small to render the
    // font
    av.validCharWidth = fm.charWidth('M') <= av.getCharWidth();

    scalePanelHolder.setPreferredSize(
            new Dimension(10, av.getCharHeight() + fm.getDescent()));
    idSpaceFillerPanel1.setPreferredSize(
            new Dimension(10, av.getCharHeight() + fm.getDescent()));
    idwidthAdjuster.invalidate();
    scalePanelHolder.invalidate();
    // BH 2018 getIdPanel().getIdCanvas().gg = null;
    getSeqPanel().seqCanvas.img = null;
    getAnnotationPanel().adjustPanelHeight();

    Dimension d = calculateIdWidth();
    getIdPanel().getIdCanvas().setPreferredSize(d);
    hscrollFillerPanel.setPreferredSize(d);

    repaint();
  }

  /**
   * Calculates the width of the alignment labels based on the displayed names
   * and any bounds on label width set in preferences. The calculated width is
   * also set as a property of the viewport.
   * 
   * @return Dimension giving the maximum width of the alignment label panel
   *         that should be used.
   */
  public Dimension calculateIdWidth()
  {
    int oldWidth = av.getIdWidth();

    // calculate sensible default width when no preference is available
    Dimension r = null;
    if (av.getIdWidth() < 0)
    {
      int afwidth = (alignFrame != null ? alignFrame.getWidth() : 300);
      int idWidth = Math.min(afwidth - 200, 2 * afwidth / 3);
      int maxwidth = Math.max(IdwidthAdjuster.MIN_ID_WIDTH, idWidth);
      r = calculateIdWidth(maxwidth);
      av.setIdWidth(r.width);
    }
    else
    {
      r = new Dimension();
      r.width = av.getIdWidth();
      r.height = 0;
    }

    /*
     * fudge: if desired width has changed, update layout
     * (see also paintComponent - updates layout on a repaint)
     */
    if (r.width != oldWidth)
    {
      idPanelHolder.setPreferredSize(r);
      validate();
    }
    return r;
  }

  /**
   * Calculate the width of the alignment labels based on the displayed names
   * and any bounds on label width set in preferences.
   * 
   * @param maxwidth
   *          -1 or maximum width allowed for IdWidth
   * @return Dimension giving the maximum width of the alignment label panel
   *         that should be used.
   */
  protected Dimension calculateIdWidth(int maxwidth)
  {
    Container c = new Container();

    FontMetrics fm = c.getFontMetrics(
            new Font(av.font.getName(), Font.ITALIC, av.font.getSize()));

    AlignmentI al = av.getAlignment();
    int i = 0;
    int idWidth = 0;

    while ((i < al.getHeight()) && (al.getSequenceAt(i) != null))
    {
      SequenceI s = al.getSequenceAt(i);
      String id = s.getDisplayId(av.getShowJVSuffix());
      int stringWidth = fm.stringWidth(id);
      idWidth = Math.max(idWidth, stringWidth);
      i++;
    }

    // Also check annotation label widths
    i = 0;

    if (al.getAlignmentAnnotation() != null)
    {
      fm = c.getFontMetrics(getAlabels().getFont());

      while (i < al.getAlignmentAnnotation().length)
      {
        String label = al.getAlignmentAnnotation()[i].label;
        int stringWidth = fm.stringWidth(label);
        idWidth = Math.max(idWidth, stringWidth);
        i++;
      }
    }

    int w = maxwidth < 0 ? idWidth : Math.min(maxwidth, idWidth);
    w += ID_WIDTH_PADDING;

    return new Dimension(w, 12);
  }

  /**
   * Highlight the given results on the alignment
   * 
   */
  public void highlightSearchResults(SearchResultsI results)
  {
    boolean scrolled = scrollToPosition(results, 0, false);

    boolean fastPaint = !(scrolled && av.getWrapAlignment());

    getSeqPanel().seqCanvas.highlightSearchResults(results, fastPaint);
  }

  /**
   * Scroll the view to show the position of the highlighted region in results
   * (if any)
   * 
   * @param searchResults
   * @return
   */
  public boolean scrollToPosition(SearchResultsI searchResults)
  {
    return scrollToPosition(searchResults, 0, false);
  }

  /**
   * Scrolls the view (if necessary) to show the position of the first
   * highlighted region in results (if any). Answers true if the view was
   * scrolled, or false if no matched region was found, or it is already
   * visible.
   * 
   * @param results
   * @param verticalOffset
   *          if greater than zero, allows scrolling to a position below the
   *          first displayed sequence
   * @param centre
   *          if true, try to centre the search results horizontally in the view
   * @return
   */
  protected boolean scrollToPosition(SearchResultsI results,
          int verticalOffset, boolean centre)
  {
    int startv, endv, starts, ends;
    ViewportRanges ranges = av.getRanges();

    if (results == null || results.isEmpty() || av == null
            || av.getAlignment() == null)
    {
      return false;
    }
    int seqIndex = av.getAlignment().findIndex(results);
    if (seqIndex == -1)
    {
      return false;
    }
    SequenceI seq = av.getAlignment().getSequenceAt(seqIndex);

    int[] r = results.getResults(seq, 0, av.getAlignment().getWidth());
    if (r == null)
    {
      return false;
    }
    int start = r[0];
    int end = r[1];

    /*
     * To centre results, scroll to positions half the visible width
     * left/right of the start/end positions
     */
    if (centre)
    {
      int offset = (ranges.getEndRes() - ranges.getStartRes() + 1) / 2 - 1;
      start = Math.max(start - offset, 0);
      end = end + offset - 1;
    }
    if (start < 0)
    {
      return false;
    }
    if (end == seq.getEnd())
    {
      return false;
    }

    if (av.hasHiddenColumns())
    {
      HiddenColumns hidden = av.getAlignment().getHiddenColumns();
      start = hidden.absoluteToVisibleColumn(start);
      end = hidden.absoluteToVisibleColumn(end);
      if (start == end)
      {
        if (!hidden.isVisible(r[0]))
        {
          // don't scroll - position isn't visible
          return false;
        }
      }
    }

    /*
     * allow for offset of target sequence (actually scroll to one above it)
     */
    seqIndex = Math.max(0, seqIndex - verticalOffset);
    boolean scrollNeeded = true;

    if (!av.getWrapAlignment())
    {
      if ((startv = ranges.getStartRes()) >= start)
      {
        /*
         * Scroll left to make start of search results visible
         */
        setScrollValues(start, seqIndex);
      }
      else if ((endv = ranges.getEndRes()) <= end)
      {
        /*
         * Scroll right to make end of search results visible
         */
        setScrollValues(startv + end - endv, seqIndex);
      }
      else if ((starts = ranges.getStartSeq()) > seqIndex)
      {
        /*
         * Scroll up to make start of search results visible
         */
        setScrollValues(ranges.getStartRes(), seqIndex);
      }
      else if ((ends = ranges.getEndSeq()) <= seqIndex)
      {
        /*
         * Scroll down to make end of search results visible
         */
        setScrollValues(ranges.getStartRes(), starts + seqIndex - ends + 1);
      }
      /*
       * Else results are already visible - no need to scroll
       */
      scrollNeeded = false;
    }
    else
    {
      scrollNeeded = ranges.scrollToWrappedVisible(start);
    }

    paintAlignment(false, false);

    return scrollNeeded;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public OverviewPanel getOverviewPanel()
  {
    return overviewPanel;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param op
   *          DOCUMENT ME!
   */
  public void setOverviewPanel(OverviewPanel op)
  {
    overviewPanel = op;
  }

  /**
   * 
   * @param b
   *          Hide or show annotation panel
   * 
   */
  public void setAnnotationVisible(boolean b)
  {
    if (!av.getWrapAlignment())
    {
      annotationSpaceFillerHolder.setVisible(b);
      annotationScroller.setVisible(b);
    }
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
    if (alignFrame.getHeight() == 0)
    {
      System.out.println("NEEDS FIXING");
    }
    validateAnnotationDimensions(true);
    addNotify();
    // TODO: many places call this method and also paintAlignment with various
    // different settings. this means multiple redraws are triggered...
    paintAlignment(true, av.needToUpdateStructureViews());
  }

  /**
   * calculate the annotation dimensions and refresh slider values accordingly.
   * need to do repaints/notifys afterwards.
   */
  protected void validateAnnotationDimensions(boolean adjustPanelHeight)
  {
    // BH 2018.04.18 comment: addNotify() is not appropriate here. We
    // are not changing ancestors, and keyboard action listeners do
    // not need to be reset. addNotify() is a very expensive operation,
    // requiring a full re-layout of all parents and children.
    // Note in JComponent:
    // This method is called by the toolkit internally and should
    // not be called directly by programs.
    // I note that addNotify() is called in several areas of Jalview.

    int annotationHeight = getAnnotationPanel().adjustPanelHeight();
    annotationHeight = getAnnotationPanel()
            .adjustForAlignFrame(adjustPanelHeight, annotationHeight);

    hscroll.addNotify();
    annotationScroller.setPreferredSize(
            new Dimension(annotationScroller.getWidth(), annotationHeight));

    Dimension e = idPanel.getSize();
    alabels.setSize(new Dimension(e.width, annotationHeight));

    annotationSpaceFillerHolder.setPreferredSize(new Dimension(
            annotationSpaceFillerHolder.getWidth(), annotationHeight));
    annotationScroller.validate();
    annotationScroller.addNotify();
  }

  /**
   * update alignment layout for viewport settings
   * 
   * @param wrap
   *          DOCUMENT ME!
   */
  public void updateLayout()
  {
    fontChanged();
    setAnnotationVisible(av.isShowAnnotation());
    boolean wrap = av.getWrapAlignment();
    ViewportRanges ranges = av.getRanges();
    ranges.setStartSeq(0);
    scalePanelHolder.setVisible(!wrap);
    hscroll.setVisible(!wrap);
    idwidthAdjuster.setVisible(!wrap);

    if (wrap)
    {
      annotationScroller.setVisible(false);
      annotationSpaceFillerHolder.setVisible(false);
    }
    else if (av.isShowAnnotation())
    {
      annotationScroller.setVisible(true);
      annotationSpaceFillerHolder.setVisible(true);
      validateAnnotationDimensions(false);
    }

    int canvasWidth = getSeqPanel().seqCanvas.getWidth();
    if (canvasWidth > 0)
    { // may not yet be laid out
      if (wrap)
      {
        int widthInRes = getSeqPanel().seqCanvas
                .getWrappedCanvasWidth(canvasWidth);
        ranges.setViewportWidth(widthInRes);
      }
      else
      {
        int widthInRes = (canvasWidth / av.getCharWidth());
        int heightInSeq = (getSeqPanel().seqCanvas.getHeight()
                / av.getCharHeight());

        ranges.setViewportWidth(widthInRes);
        ranges.setViewportHeight(heightInSeq);
      }
    }

    idSpaceFillerPanel1.setVisible(!wrap);

    repaint();
  }

  /**
   * Adjust row/column scrollers to show a visible position in the alignment.
   * 
   * @param x
   *          visible column to scroll to
   * @param y
   *          visible row to scroll to
   * 
   */
  public void setScrollValues(int xpos, int ypos)
  {
    int x = xpos;
    int y = ypos;

    if (av == null || av.getAlignment() == null)
    {
      return;
    }

    if (av.getWrapAlignment())
    {
      setScrollingForWrappedPanel(x);
    }
    else
    {
      int width = av.getAlignment().getVisibleWidth();
      int height = av.getAlignment().getHeight();

      hextent = getSeqPanel().seqCanvas.getWidth() / av.getCharWidth();
      vextent = getSeqPanel().seqCanvas.getHeight() / av.getCharHeight();

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
        x = 0;
      }

      // update the scroll values
      hscroll.setValues(x, hextent, 0, width);
      vscroll.setValues(y, vextent, 0, height);
    }
  }

  /**
   * Respond to adjustment event when horizontal or vertical scrollbar is
   * changed
   * 
   * @param evt
   *          adjustment event encoding whether hscroll or vscroll changed
   */
  @Override
  public void adjustmentValueChanged(AdjustmentEvent evt)
  {
    if (av.getWrapAlignment())
    {
      adjustScrollingWrapped(evt);
      return;
    }

    ViewportRanges ranges = av.getRanges();

    if (evt.getSource() == hscroll)
    {
      int oldX = ranges.getStartRes();
      int oldwidth = ranges.getViewportWidth();
      int x = hscroll.getValue();
      int width = getSeqPanel().seqCanvas.getWidth() / av.getCharWidth();

      // if we're scrolling to the position we're already at, stop
      // this prevents infinite recursion of events when the scroll/viewport
      // ranges values are the same
      if ((x == oldX) && (width == oldwidth))
      {
        return;
      }
      ranges.setViewportStartAndWidth(x, width);
    }
    else if (evt.getSource() == vscroll)
    {
      int oldY = ranges.getStartSeq();
      int oldheight = ranges.getViewportHeight();
      int y = vscroll.getValue();
      int height = getSeqPanel().seqCanvas.getHeight() / av.getCharHeight();

      // if we're scrolling to the position we're already at, stop
      // this prevents infinite recursion of events when the scroll/viewport
      // ranges values are the same
      if ((y == oldY) && (height == oldheight))
      {
        return;
      }
      ranges.setViewportStartAndHeight(y, height);
    }
    repaint();
  }

  /**
   * Responds to a scroll change by setting the start position of the viewport.
   * Does
   * 
   * @param evt
   */
  protected void adjustScrollingWrapped(AdjustmentEvent evt)
  {
    if (evt.getSource() == hscroll)
    {
      return; // no horizontal scroll when wrapped
    }
    final ViewportRanges ranges = av.getRanges();

    if (evt.getSource() == vscroll)
    {
      int newY = vscroll.getValue();

      /*
       * if we're scrolling to the position we're already at, stop
       * this prevents infinite recursion of events when the scroll/viewport
       * ranges values are the same
       */
      int oldX = ranges.getStartRes();
      int oldY = ranges.getWrappedScrollPosition(oldX);
      if (oldY == newY)
      {
        return;
      }
      if (newY > -1)
      {
        /*
         * limit page up/down to one width's worth of positions
         */
        int rowSize = ranges.getViewportWidth();
        int newX = newY > oldY ? oldX + rowSize : oldX - rowSize;
        ranges.setViewportStartAndWidth(Math.max(0, newX), rowSize);
      }
    }
    else
    {
      // This is only called if file loaded is a jar file that
      // was wrapped when saved and user has wrap alignment true
      // as preference setting
      SwingUtilities.invokeLater(new Runnable()
      {
        @Override
        public void run()
        {
          // When updating scrolling to use ViewportChange events, this code
          // could not be validated and it is not clear if it is now being
          // called. Log warning here in case it is called and unforeseen
          // problems occur
          Console.warn(
                  "Unexpected path through code: Wrapped jar file opened with wrap alignment set in preferences");

          // scroll to start of panel
          ranges.setStartRes(0);
          ranges.setStartSeq(0);
        }
      });
    }
    repaint();
  }

  /* (non-Javadoc)
   * @see jalview.api.AlignmentViewPanel#paintAlignment(boolean)
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
      av.getStructureSelectionManager().sequenceColoursChanged(this);
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
  public void paintComponent(Graphics g)
  {
    invalidate(); // needed so that the id width adjuster works correctly

    Dimension d = getIdPanel().getIdCanvas().getPreferredSize();
    idPanelHolder.setPreferredSize(d);
    hscrollFillerPanel.setPreferredSize(new Dimension(d.width, 12));

    validate(); // needed so that the id width adjuster works correctly

    /*
     * set scroll bar positions - tried to remove but necessary for split panel to resize correctly
     * though I still think this call should be elsewhere.
     */
    ViewportRanges ranges = av.getRanges();
    setScrollValues(ranges.getStartRes(), ranges.getStartSeq());
    super.paintComponent(g);
  }

  /**
   * Set vertical scroll bar position, and number of increments, for wrapped
   * panel
   * 
   * @param topLeftColumn
   *          the column position at top left (0..)
   */
  private void setScrollingForWrappedPanel(int topLeftColumn)
  {
    ViewportRanges ranges = av.getRanges();
    int scrollPosition = ranges.getWrappedScrollPosition(topLeftColumn);
    int maxScroll = ranges.getWrappedMaxScroll(topLeftColumn);

    /*
     * a scrollbar's value can be set to at most (maximum-extent)
     * so we add extent (1) to the maxScroll value
     */
    vscroll.setUnitIncrement(1);
    vscroll.setValues(scrollPosition, 1, 0, maxScroll + 1);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param pg
   *          DOCUMENT ME!
   * @param pf
   *          DOCUMENT ME!
   * @param pi
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   * 
   * @throws PrinterException
   *           DOCUMENT ME!
   */
  @Override
  public int print(Graphics pg, PageFormat pf, int pi)
          throws PrinterException
  {
    pg.translate((int) pf.getImageableX(), (int) pf.getImageableY());

    int pwidth = (int) pf.getImageableWidth();
    int pheight = (int) pf.getImageableHeight();

    if (av.getWrapAlignment())
    {
      return printWrappedAlignment(pwidth, pheight, pi, pg);
    }
    else
    {
      return printUnwrapped(pwidth, pheight, pi, pg, pg);
    }
  }

  /**
   * Draws the alignment image, including sequence ids, sequences, and
   * annotation labels and annotations if shown, on either one or two Graphics
   * contexts.
   * 
   * @param pageWidth
   *          in pixels
   * @param pageHeight
   *          in pixels
   * @param pageIndex
   *          (0, 1, ...)
   * @param idGraphics
   *          the graphics context for sequence ids and annotation labels
   * @param alignmentGraphics
   *          the graphics context for sequences and annotations (may or may not
   *          be the same context as idGraphics)
   * @return
   * @throws PrinterException
   */
  public int printUnwrapped(int pageWidth, int pageHeight, int pageIndex,
          Graphics idGraphics, Graphics alignmentGraphics)
          throws PrinterException
  {
    final int idWidth = getVisibleIdWidth(false);

    /*
     * Get the horizontal offset to where we draw the sequences.
     * This is idWidth if using a single Graphics context, else zero.
     */
    final int alignmentGraphicsOffset = idGraphics != alignmentGraphics ? 0
            : idWidth;

    FontMetrics fm = getFontMetrics(av.getFont());
    final int charHeight = av.getCharHeight();
    final int scaleHeight = charHeight + fm.getDescent();

    idGraphics.setColor(Color.white);
    idGraphics.fillRect(0, 0, pageWidth, pageHeight);
    idGraphics.setFont(av.getFont());

    /*
     * How many sequences and residues can we fit on a printable page?
     */
    final int totalRes = (pageWidth - idWidth) / av.getCharWidth();

    final int totalSeq = (pageHeight - scaleHeight) / charHeight - 1;

    final int alignmentWidth = av.getAlignment().getVisibleWidth();
    int pagesWide = (alignmentWidth / totalRes) + 1;

    final int startRes = (pageIndex % pagesWide) * totalRes;
    final int endRes = Math.min(startRes + totalRes - 1,
            alignmentWidth - 1);

    final int startSeq = (pageIndex / pagesWide) * totalSeq;
    final int alignmentHeight = av.getAlignment().getHeight();
    final int endSeq = Math.min(startSeq + totalSeq, alignmentHeight);

    int pagesHigh = ((alignmentHeight / totalSeq) + 1) * pageHeight;

    if (av.isShowAnnotation())
    {
      pagesHigh += getAnnotationPanel().adjustPanelHeight() + 3;
    }

    pagesHigh /= pageHeight;

    if (pageIndex >= (pagesWide * pagesHigh))
    {
      return Printable.NO_SUCH_PAGE;
    }
    final int alignmentDrawnHeight = (endSeq - startSeq) * charHeight + 3;

    /*
     * draw the Scale at horizontal offset, then reset to top left (0, 0)
     */
    alignmentGraphics.translate(alignmentGraphicsOffset, 0);
    getScalePanel().drawScale(alignmentGraphics, startRes, endRes,
            pageWidth - idWidth, scaleHeight);
    alignmentGraphics.translate(-alignmentGraphicsOffset, 0);

    /*
     * Draw the sequence ids, offset for scale height,
     * then reset to top left (0, 0)
     */
    idGraphics.translate(0, scaleHeight);
    IdCanvas idCanvas = getIdPanel().getIdCanvas();
    List<SequenceI> selection = av.getSelectionGroup() == null ? null
            : av.getSelectionGroup().getSequences(null);
    idCanvas.drawIds((Graphics2D) idGraphics, av, startSeq, endSeq - 1,
            selection);

    idGraphics.setFont(av.getFont());
    idGraphics.translate(0, -scaleHeight);

    /*
     * draw the sequences, offset for scale height, and id width (if using a
     * single graphics context), then reset to (0, scale height)
     */
    alignmentGraphics.translate(alignmentGraphicsOffset, scaleHeight);
    getSeqPanel().seqCanvas.drawPanelForPrinting(alignmentGraphics,
            startRes, endRes, startSeq, endSeq - 1);
    alignmentGraphics.translate(-alignmentGraphicsOffset, 0);

    if (av.isShowAnnotation() && (endSeq == alignmentHeight))
    {
      /*
       * draw annotation labels; drawComponent() translates by
       * getScrollOffset(), so compensate for that first;
       * then reset to (0, scale height)
       */
      int offset = getAlabels().getScrollOffset();
      idGraphics.translate(0, -offset);
      idGraphics.translate(0, alignmentDrawnHeight);
      getAlabels().drawComponent(idGraphics, idWidth);
      idGraphics.translate(0, -alignmentDrawnHeight);

      /*
       * draw the annotations starting at 
       * (idOffset, alignmentHeight) from (0, scaleHeight)
       */
      alignmentGraphics.translate(alignmentGraphicsOffset,
              alignmentDrawnHeight);
      updateLayout();
      getAnnotationPanel().renderer.drawComponent(getAnnotationPanel(), av,
              alignmentGraphics, -1, startRes, endRes + 1);
    }

    return Printable.PAGE_EXISTS;
  }

  /**
   * Prints one page of an alignment in wrapped mode. Returns
   * Printable.PAGE_EXISTS (0) if a page was drawn, or Printable.NO_SUCH_PAGE if
   * no page could be drawn (page number out of range).
   * 
   * @param pageWidth
   * @param pageHeight
   * @param pageNumber
   *          (0, 1, ...)
   * @param g
   * 
   * @return
   * 
   * @throws PrinterException
   */
  public int printWrappedAlignment(int pageWidth, int pageHeight,
          int pageNumber, Graphics g) throws PrinterException
  {
    getSeqPanel().seqCanvas.calculateWrappedGeometry(getWidth(),
            getHeight());
    int annotationHeight = 0;
    if (av.isShowAnnotation())
    {
      annotationHeight = getAnnotationPanel().adjustPanelHeight();
    }

    int hgap = av.getCharHeight();
    if (av.getScaleAboveWrapped())
    {
      hgap += av.getCharHeight();
    }

    int cHeight = av.getAlignment().getHeight() * av.getCharHeight() + hgap
            + annotationHeight;

    int idWidth = getVisibleIdWidth(false);

    int maxwidth = av.getAlignment().getVisibleWidth();

    int resWidth = getSeqPanel().seqCanvas
            .getWrappedCanvasWidth(pageWidth - idWidth);
    av.getRanges().setViewportStartAndWidth(0, resWidth);

    int totalHeight = cHeight * (maxwidth / resWidth + 1);

    g.setColor(Color.white);
    g.fillRect(0, 0, pageWidth, pageHeight);
    g.setFont(av.getFont());
    g.setColor(Color.black);

    /*
     * method: print the whole wrapped alignment, but with a clip region that
     * is restricted to the requested page; this supports selective print of 
     * single pages or ranges, (at the cost of repeated processing in the 
     * 'normal' case, when all pages are printed)
     */
    g.translate(0, -pageNumber * pageHeight);

    g.setClip(0, pageNumber * pageHeight, pageWidth, pageHeight);

    /*
     * draw sequence ids and annotation labels (if shown)
     */
    IdCanvas idCanvas = getIdPanel().getIdCanvas();
    idCanvas.drawIdsWrapped((Graphics2D) g, av, 0, totalHeight);

    g.translate(idWidth, 0);

    getSeqPanel().seqCanvas.drawWrappedPanelForPrinting(g,
            pageWidth - idWidth, totalHeight, 0);

    if ((pageNumber * pageHeight) < totalHeight)
    {
      return Printable.PAGE_EXISTS;
    }
    else
    {
      return Printable.NO_SUCH_PAGE;
    }
  }

  /**
   * get current sequence ID panel width, or nominal value if panel were to be
   * displayed using default settings
   * 
   * @return
   */
  public int getVisibleIdWidth()
  {
    return getVisibleIdWidth(true);
  }

  /**
   * get current sequence ID panel width, or nominal value if panel were to be
   * displayed using default settings
   * 
   * @param onscreen
   *          indicate if the Id width for onscreen or offscreen display should
   *          be returned
   * @return
   */
  protected int getVisibleIdWidth(boolean onscreen)
  {
    // see if rendering offscreen - check preferences and calc width accordingly
    if (!onscreen && Cache.getDefault("FIGURE_AUTOIDWIDTH", false))
    {
      return calculateIdWidth(-1).width;
    }
    Integer idwidth = onscreen ? null
            : Cache.getIntegerProperty("FIGURE_FIXEDIDWIDTH");
    if (idwidth != null)
    {
      return idwidth.intValue() + ID_WIDTH_PADDING;
    }

    int w = getIdPanel().getWidth();
    return (w > 0 ? w : calculateIdWidth().width);
  }

  void makeAlignmentImage(ImageMaker.TYPE type, File file, String renderer) throws ImageOutputException
  {
    makeAlignmentImage(type, file, renderer,
            BitmapImageSizing.nullBitmapImageSizing());
  }

  /**
   * Builds an image of the alignment of the specified type (EPS/PNG/SVG) and
   * writes it to the specified file
   * 
   * @param type
   * @param file
   * @param textrenderer
   * @param bitmapscale
   */
  void makeAlignmentImage(ImageMaker.TYPE type, File file, String renderer,
          BitmapImageSizing userBis) throws ImageOutputException
  {
    final int borderBottomOffset = 5;

    AlignmentDimension aDimension = getAlignmentDimension();
    // todo use a lambda function in place of callback here?
    ImageWriterI writer = new ImageWriterI()
    {
      @Override
      public void exportImage(Graphics graphics) throws Exception
      {
        if (av.getWrapAlignment())
        {
          printWrappedAlignment(aDimension.getWidth(),
                  aDimension.getHeight() + borderBottomOffset, 0, graphics);
        }
        else
        {
          printUnwrapped(aDimension.getWidth(), aDimension.getHeight(), 0,
                  graphics, graphics);
        }
      }
    };

    String fileTitle = alignFrame.getTitle();
    ImageExporter exporter = new ImageExporter(writer, alignFrame, type,
            fileTitle);
    int imageWidth = aDimension.getWidth();
    int imageHeight = aDimension.getHeight() + borderBottomOffset;
    String of = MessageManager.getString("label.alignment");
    exporter.doExport(file, this, imageWidth, imageHeight, of, renderer,
            userBis);
  }

  /**
   * Calculates and returns a suitable width and height (in pixels) for an
   * exported image
   * 
   * @return
   */
  public AlignmentDimension getAlignmentDimension()
  {
    int maxwidth = av.getAlignment().getVisibleWidth();

    int height = ((av.getAlignment().getHeight() + 1) * av.getCharHeight())
            + getScalePanel().getHeight();
    int width = getVisibleIdWidth(false) + (maxwidth * av.getCharWidth());

    if (av.getWrapAlignment())
    {
      height = getWrappedHeight();
      if (Jalview.isHeadlessMode())
      {
        // need to obtain default alignment width and then add in any
        // additional allowance for id margin
        // this duplicates the calculation in getWrappedHeight but adjusts for
        // offscreen idWith
        width = alignFrame.getWidth() - vscroll.getPreferredSize().width
                - alignFrame.getInsets().left - alignFrame.getInsets().right
                - getVisibleIdWidth() + getVisibleIdWidth(false);
      }
      else
      {
        width = getSeqPanel().getWidth() + getVisibleIdWidth(false);
      }

    }
    else if (av.isShowAnnotation())
    {
      height += getAnnotationPanel().adjustPanelHeight() + 3;
    }
    return new AlignmentDimension(width, height);

  }

  public void makePNGImageMap(File imgMapFile, String imageName) throws ImageOutputException
  {
    // /////ONLY WORKS WITH NON WRAPPED ALIGNMENTS
    // ////////////////////////////////////////////
    int idWidth = getVisibleIdWidth(false);
    FontMetrics fm = getFontMetrics(av.getFont());
    int scaleHeight = av.getCharHeight() + fm.getDescent();

    // Gen image map
    // ////////////////////////////////
    if (imgMapFile != null)
    {
      try
      {
        int sSize = av.getAlignment().getHeight();
        int alwidth = av.getAlignment().getWidth();
        PrintWriter out = new PrintWriter(new FileWriter(imgMapFile));
        out.println(HTMLOutput.getImageMapHTML());
        out.println("<img src=\"" + imageName
                + "\" border=\"0\" usemap=\"#Map\" >"
                + "<map name=\"Map\">");

        for (int s = 0; s < sSize; s++)
        {
          int sy = s * av.getCharHeight() + scaleHeight;

          SequenceI seq = av.getAlignment().getSequenceAt(s);
          SequenceGroup[] groups = av.getAlignment().findAllGroups(seq);
          for (int column = 0; column < alwidth; column++)
          {
            StringBuilder text = new StringBuilder(512);
            String triplet = null;
            if (av.getAlignment().isNucleotide())
            {
              triplet = ResidueProperties.nucleotideName
                      .get(seq.getCharAt(column) + "");
            }
            else
            {
              triplet = ResidueProperties.aa2Triplet
                      .get(seq.getCharAt(column) + "");
            }

            if (triplet == null)
            {
              continue;
            }

            int seqPos = seq.findPosition(column);
            int gSize = groups.length;
            for (int g = 0; g < gSize; g++)
            {
              if (text.length() < 1)
              {
                text.append("<area shape=\"rect\" coords=\"")
                        .append((idWidth + column * av.getCharWidth()))
                        .append(",").append(sy).append(",")
                        .append((idWidth
                                + (column + 1) * av.getCharWidth()))
                        .append(",").append((av.getCharHeight() + sy))
                        .append("\"").append(" onMouseOver=\"toolTip('")
                        .append(seqPos).append(" ").append(triplet);
              }

              if (groups[g].getStartRes() < column
                      && groups[g].getEndRes() > column)
              {
                text.append("<br><em>").append(groups[g].getName())
                        .append("</em>");
              }
            }

            if (text.length() < 1)
            {
              text.append("<area shape=\"rect\" coords=\"")
                      .append((idWidth + column * av.getCharWidth()))
                      .append(",").append(sy).append(",")
                      .append((idWidth + (column + 1) * av.getCharWidth()))
                      .append(",").append((av.getCharHeight() + sy))
                      .append("\"").append(" onMouseOver=\"toolTip('")
                      .append(seqPos).append(" ").append(triplet);
            }
            if (!Comparison.isGap(seq.getCharAt(column)))
            {
              List<SequenceFeature> features = seq.findFeatures(column,
                      column);
              for (SequenceFeature sf : features)
              {
                if (sf.isContactFeature())
                {
                  text.append("<br>").append(sf.getType()).append(" ")
                          .append(sf.getBegin()).append(":")
                          .append(sf.getEnd());
                }
                else
                {
                  text.append("<br>");
                  text.append(sf.getType());
                  String description = sf.getDescription();
                  if (description != null
                          && !sf.getType().equals(description))
                  {
                    description = description.replace("\"", "&quot;");
                    text.append(" ").append(description);
                  }
                }
                String status = sf.getStatus();
                if (status != null && !"".equals(status))
                {
                  text.append(" (").append(status).append(")");
                }
              }
              if (text.length() > 1)
              {
                text.append("')\"; onMouseOut=\"toolTip()\";  href=\"#\">");
                out.println(text.toString());
              }
            }
          }
        }
        out.println("</map></body></html>");
        out.close();

      } catch (Exception ex)
      {
        throw new ImageOutputException("couldn't write ImageMap due to unexpected error",ex);
      }
    } // /////////END OF IMAGE MAP

  }

  /**
   * Answers the height of the entire alignment in pixels, assuming it is in
   * wrapped mode
   * 
   * @return
   */
  int getWrappedHeight()
  {
    int seqPanelWidth = getSeqPanel().seqCanvas.getWidth();

    if (System.getProperty("java.awt.headless") != null
            && System.getProperty("java.awt.headless").equals("true"))
    {
      seqPanelWidth = alignFrame.getWidth() - getVisibleIdWidth()
              - vscroll.getPreferredSize().width
              - alignFrame.getInsets().left - alignFrame.getInsets().right;
    }

    int chunkWidth = getSeqPanel().seqCanvas
            .getWrappedCanvasWidth(seqPanelWidth);

    int hgap = av.getCharHeight();
    if (av.getScaleAboveWrapped())
    {
      hgap += av.getCharHeight();
    }

    int annotationHeight = 0;
    if (av.isShowAnnotation())
    {
      hgap += SeqCanvas.SEQS_ANNOTATION_GAP;
      annotationHeight = getAnnotationPanel().adjustPanelHeight();
    }

    int cHeight = av.getAlignment().getHeight() * av.getCharHeight() + hgap
            + annotationHeight;

    int maxwidth = av.getAlignment().getWidth();
    if (av.hasHiddenColumns())
    {
      maxwidth = av.getAlignment().getHiddenColumns()
              .absoluteToVisibleColumn(maxwidth) - 1;
    }

    int height = ((maxwidth / chunkWidth) + 1) * cHeight;

    return height;
  }

  /**
   * close the panel - deregisters all listeners and nulls any references to
   * alignment data.
   */
  public void closePanel()
  {
    PaintRefresher.RemoveComponent(getSeqPanel().seqCanvas);
    PaintRefresher.RemoveComponent(getIdPanel().getIdCanvas());
    PaintRefresher.RemoveComponent(this);

    closeChildFrames();

    /*
     * try to ensure references are nulled
     */
    if (annotationPanel != null)
    {
      annotationPanel.dispose();
      annotationPanel = null;
    }

    if (av != null)
    {
      av.removePropertyChangeListener(propertyChangeListener);
      propertyChangeListener = null;
      StructureSelectionManager ssm = av.getStructureSelectionManager();
      ssm.removeStructureViewerListener(getSeqPanel(), null);
      ssm.removeSelectionListener(getSeqPanel());
      ssm.removeCommandListener(av);
      ssm.removeStructureViewerListener(getSeqPanel(), null);
      ssm.removeSelectionListener(getSeqPanel());
      av.dispose();
      av = null;
    }
    else
    {
      if (Console.isDebugEnabled())
      {
        Console.warn("Closing alignment panel which is already closed.");
      }
    }
  }

  /**
   * Close any open dialogs that would be orphaned when this one is closed
   */
  protected void closeChildFrames()
  {
    if (overviewPanel != null)
    {
      overviewPanel.dispose();
      overviewPanel = null;
    }
    if (calculationDialog != null)
    {
      calculationDialog.closeFrame();
      calculationDialog = null;
    }
  }

  /**
   * hides or shows dynamic annotation rows based on groups and av state flags
   */
  public void updateAnnotation()
  {
    updateAnnotation(false, false);
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
    return av == null ? null : av.getAlignment();
  }

  @Override
  public String getViewName()
  {
    return av.getViewName();
  }

  /**
   * Make/Unmake this alignment panel the current input focus
   * 
   * @param b
   */
  public void setSelected(boolean b)
  {
    try
    {
      if (alignFrame.getSplitViewContainer() != null)
      {
        /*
         * bring enclosing SplitFrame to front first if there is one
         */
        ((SplitFrame) alignFrame.getSplitViewContainer()).setSelected(b);
      }
      alignFrame.setSelected(b);
    } catch (Exception ex)
    {
    }
    if (b)
    {
      setAlignFrameView();
    }
  }

  public void setAlignFrameView()
  {
    alignFrame.setDisplayedView(this);
  }

  @Override
  public StructureSelectionManager getStructureSelectionManager()
  {
    return av.getStructureSelectionManager();
  }

  @Override
  public void raiseOOMWarning(String string, OutOfMemoryError error)
  {
    new OOMWarning(string, error, this);
  }

  @Override
  public jalview.api.FeatureRenderer cloneFeatureRenderer()
  {

    return new FeatureRenderer(this);
  }

  @Override
  public jalview.api.FeatureRenderer getFeatureRenderer()
  {
    return seqPanel.seqCanvas.getFeatureRenderer();
  }

  public void updateFeatureRenderer(
          jalview.renderer.seqfeatures.FeatureRenderer fr)
  {
    fr.transferSettings(getSeqPanel().seqCanvas.getFeatureRenderer());
  }

  public void updateFeatureRendererFrom(jalview.api.FeatureRenderer fr)
  {
    if (getSeqPanel().seqCanvas.getFeatureRenderer() != null)
    {
      getSeqPanel().seqCanvas.getFeatureRenderer().transferSettings(fr);
    }
  }

  public ScalePanel getScalePanel()
  {
    return scalePanel;
  }

  public void setScalePanel(ScalePanel scalePanel)
  {
    this.scalePanel = scalePanel;
  }

  public SeqPanel getSeqPanel()
  {
    return seqPanel;
  }

  public void setSeqPanel(SeqPanel seqPanel)
  {
    this.seqPanel = seqPanel;
  }

  public AnnotationPanel getAnnotationPanel()
  {
    return annotationPanel;
  }

  public void setAnnotationPanel(AnnotationPanel annotationPanel)
  {
    this.annotationPanel = annotationPanel;
  }

  public AnnotationLabels getAlabels()
  {
    return alabels;
  }

  public void setAlabels(AnnotationLabels alabels)
  {
    this.alabels = alabels;
  }

  public IdPanel getIdPanel()
  {
    return idPanel;
  }

  public void setIdPanel(IdPanel idPanel)
  {
    this.idPanel = idPanel;
  }

  /**
   * Follow a scrolling change in the (cDNA/Protein) complementary alignment.
   * The aim is to keep the two alignments 'lined up' on their centre columns.
   * 
   * @param sr
   *          holds mapped region(s) of this alignment that we are scrolling
   *          'to'; may be modified for sequence offset by this method
   * @param verticalOffset
   *          the number of visible sequences to show above the mapped region
   */
  protected void scrollToCentre(SearchResultsI sr, int verticalOffset)
  {
    scrollToPosition(sr, verticalOffset, true);
  }

  /**
   * Set a flag to say do not scroll any (cDNA/protein) complement.
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

  /**
   * Redraw sensibly.
   * 
   * @adjustHeight if true, try to recalculate panel height for visible
   *               annotations
   */
  protected void refresh(boolean adjustHeight)
  {
    validateAnnotationDimensions(adjustHeight);
    addNotify();
    if (adjustHeight)
    {
      // sort, repaint, update overview
      paintAlignment(true, false);
    }
    else
    {
      // lightweight repaint
      repaint();
    }
  }

  @Override
  /**
   * Property change event fired when a change is made to the viewport ranges
   * object associated with this alignment panel's viewport
   */
  public void propertyChange(PropertyChangeEvent evt)
  {
    // update this panel's scroll values based on the new viewport ranges values
    ViewportRanges ranges = av.getRanges();
    int x = ranges.getStartRes();
    int y = ranges.getStartSeq();
    setScrollValues(x, y);

    // now update any complementary alignment (its viewport ranges object
    // is different so does not get automatically updated)
    if (isSetToScrollComplementPanel())
    {
      setToScrollComplementPanel(false);
      av.scrollComplementaryAlignment();
      setToScrollComplementPanel(true);
    }
  }

  /**
   * Set the reference to the PCA/Tree chooser dialog for this panel. This
   * reference should be nulled when the dialog is closed.
   * 
   * @param calculationChooser
   */
  public void setCalculationDialog(CalculationChooser calculationChooser)
  {
    calculationDialog = calculationChooser;
  }

  /**
   * Returns the reference to the PCA/Tree chooser dialog for this panel (null
   * if none is open)
   */
  public CalculationChooser getCalculationDialog()
  {
    return calculationDialog;
  }

  /**
   * Constructs and sets the title for the Overview window (if there is one),
   * including the align frame's title, and view name (if applicable). Returns
   * the title, or null if this panel has no Overview window open.
   * 
   * @param alignFrame
   * @return
   */
  public String setOverviewTitle(AlignFrame alignFrame)
  {
    if (this.overviewPanel == null)
    {
      return null;
    }
    String overviewTitle = MessageManager
            .formatMessage("label.overview_params", new Object[]
            { alignFrame.getTitle() });
    String viewName = getViewName();
    if (viewName != null)
    {
      overviewTitle += (" " + viewName);
    }
    overviewPanel.setTitle(overviewTitle);
    return overviewTitle;
  }

  /**
   * If this alignment panel has an Overview panel open, closes it
   */
  public void closeOverviewPanel()
  {
    if (overviewPanel != null)
    {
      overviewPanel.close();
      overviewPanel = null;
    }
  }

}
