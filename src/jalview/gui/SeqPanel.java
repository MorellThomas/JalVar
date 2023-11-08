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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;

import jalview.api.AlignViewportI;
import jalview.bin.Console;
import jalview.commands.EditCommand;
import jalview.commands.EditCommand.Action;
import jalview.commands.EditCommand.Edit;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.MappedFeatures;
import jalview.datamodel.SearchResultMatchI;
import jalview.datamodel.SearchResults;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.io.SequenceAnnotationReport;
import jalview.renderer.ResidueShaderI;
import jalview.schemes.ResidueProperties;
import jalview.structure.SelectionListener;
import jalview.structure.SelectionSource;
import jalview.structure.SequenceListener;
import jalview.structure.StructureSelectionManager;
import jalview.structure.VamsasSource;
import jalview.util.Comparison;
import jalview.util.MappingUtils;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.viewmodel.AlignmentViewport;
import jalview.viewmodel.ViewportRanges;
import jalview.viewmodel.seqfeatures.FeatureRendererModel;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision: 1.130 $
 */
public class SeqPanel extends JPanel
        implements MouseListener, MouseMotionListener, MouseWheelListener,
        SequenceListener, SelectionListener
{
  /*
   * a class that holds computed mouse position
   * - column of the alignment (0...)
   * - sequence offset (0...)
   * - annotation row offset (0...)
   * where annotation offset is -1 unless the alignment is shown
   * in wrapped mode, annotations are shown, and the mouse is
   * over an annnotation row
   */
  static class MousePos
  {
    /*
     * alignment column position of cursor (0...)
     */
    final int column;

    /*
     * index in alignment of sequence under cursor,
     * or nearest above if cursor is not over a sequence
     */
    final int seqIndex;

    /*
     * index in annotations array of annotation under the cursor
     * (only possible in wrapped mode with annotations shown),
     * or -1 if cursor is not over an annotation row
     */
    final int annotationIndex;

    MousePos(int col, int seq, int ann)
    {
      column = col;
      seqIndex = seq;
      annotationIndex = ann;
    }

    boolean isOverAnnotation()
    {
      return annotationIndex != -1;
    }

    @Override
    public boolean equals(Object obj)
    {
      if (obj == null || !(obj instanceof MousePos))
      {
        return false;
      }
      MousePos o = (MousePos) obj;
      boolean b = (column == o.column && seqIndex == o.seqIndex
              && annotationIndex == o.annotationIndex);
      // System.out.println(obj + (b ? "= " : "!= ") + this);
      return b;
    }

    /**
     * A simple hashCode that ensures that instances that satisfy equals() have
     * the same hashCode
     */
    @Override
    public int hashCode()
    {
      return column + seqIndex + annotationIndex;
    }

    /**
     * toString method for debug output purposes only
     */
    @Override
    public String toString()
    {
      return String.format("c%d:s%d:a%d", column, seqIndex,
              annotationIndex);
    }
  }

  private static final int MAX_TOOLTIP_LENGTH = 300;

  public SeqCanvas seqCanvas;

  public AlignmentPanel ap;

  /*
   * last position for mouseMoved event
   */
  private MousePos lastMousePosition;

  protected int editLastRes;

  protected int editStartSeq;

  protected AlignViewport av;

  ScrollThread scrollThread = null;

  boolean mouseDragging = false;

  boolean editingSeqs = false;

  boolean groupEditing = false;

  // ////////////////////////////////////////
  // ///Everything below this is for defining the boundary of the rubberband
  // ////////////////////////////////////////
  int oldSeq = -1;

  boolean changeEndSeq = false;

  boolean changeStartSeq = false;

  boolean changeEndRes = false;

  boolean changeStartRes = false;

  SequenceGroup stretchGroup = null;

  boolean remove = false;

  Point lastMousePress;

  boolean mouseWheelPressed = false;

  StringBuffer keyboardNo1;

  StringBuffer keyboardNo2;

  private final SequenceAnnotationReport seqARep;

  /*
   * the last tooltip on mousing over the alignment (or annotation in wrapped mode)
   * - the tooltip is not set again if unchanged
   * - this is the tooltip text _before_ formatting as html
   */
  private String lastTooltip;

  /*
   * the last tooltip on mousing over the alignment (or annotation in wrapped mode)
   * - used to decide where to place the tooltip in getTooltipLocation() 
   * - this is the tooltip text _after_ formatting as html
   */
  private String lastFormattedTooltip;

  EditCommand editCommand;

  StructureSelectionManager ssm;

  SearchResultsI lastSearchResults;

  /**
   * Creates a new SeqPanel object
   * 
   * @param viewport
   * @param alignPanel
   */
  public SeqPanel(AlignViewport viewport, AlignmentPanel alignPanel)
  {
    seqARep = new SequenceAnnotationReport(true);
    ToolTipManager.sharedInstance().registerComponent(this);
    ToolTipManager.sharedInstance().setInitialDelay(0);
    ToolTipManager.sharedInstance().setDismissDelay(10000);

    this.av = viewport;
    setBackground(Color.white);

    seqCanvas = new SeqCanvas(alignPanel);
    setLayout(new BorderLayout());
    add(seqCanvas, BorderLayout.CENTER);

    this.ap = alignPanel;

    if (!viewport.isDataset())
    {
      addMouseMotionListener(this);
      addMouseListener(this);
      addMouseWheelListener(this);
      ssm = viewport.getStructureSelectionManager();
      ssm.addStructureViewerListener(this);
      ssm.addSelectionListener(this);
    }
  }

  int startWrapBlock = -1;

  int wrappedBlock = -1;

  /**
   * Computes the column and sequence row (and possibly annotation row when in
   * wrapped mode) for the given mouse position
   * <p>
   * Mouse position is not set if in wrapped mode with the cursor either between
   * sequences, or over the left or right vertical scale.
   * 
   * @param evt
   * @return
   */
  MousePos findMousePosition(MouseEvent evt)
  {
    int col = findColumn(evt);
    int seqIndex = -1;
    int annIndex = -1;
    int y = evt.getY();

    int charHeight = av.getCharHeight();
    int alignmentHeight = av.getAlignment().getHeight();
    if (av.getWrapAlignment())
    {
      seqCanvas.calculateWrappedGeometry(seqCanvas.getWidth(),
              seqCanvas.getHeight());

      /*
       * yPos modulo height of repeating width
       */
      int yOffsetPx = y % seqCanvas.wrappedRepeatHeightPx;

      /*
       * height of sequences plus space / scale above,
       * plus gap between sequences and annotations
       */
      int alignmentHeightPixels = seqCanvas.wrappedSpaceAboveAlignment
              + alignmentHeight * charHeight
              + SeqCanvas.SEQS_ANNOTATION_GAP;
      if (yOffsetPx >= alignmentHeightPixels)
      {
        /*
         * mouse is over annotations; find annotation index, also set
         * last sequence above (for backwards compatible behaviour)
         */
        AlignmentAnnotation[] anns = av.getAlignment()
                .getAlignmentAnnotation();
        int rowOffsetPx = yOffsetPx - alignmentHeightPixels;
        annIndex = AnnotationPanel.getRowIndex(rowOffsetPx, anns);
        seqIndex = alignmentHeight - 1;
      }
      else
      {
        /*
         * mouse is over sequence (or the space above sequences)
         */
        yOffsetPx -= seqCanvas.wrappedSpaceAboveAlignment;
        if (yOffsetPx >= 0)
        {
          seqIndex = Math.min(yOffsetPx / charHeight, alignmentHeight - 1);
        }
      }
    }
    else
    {
      ViewportRanges ranges = av.getRanges();
      seqIndex = Math.min((y / charHeight) + ranges.getStartSeq(),
              alignmentHeight - 1);
      seqIndex = Math.min(seqIndex, ranges.getEndSeq());
    }

    return new MousePos(col, seqIndex, annIndex);
  }

  /**
   * Returns the aligned sequence position (base 0) at the mouse position, or
   * the closest visible one
   * <p>
   * Returns -1 if in wrapped mode with the mouse over either left or right
   * vertical scale.
   * 
   * @param evt
   * @return
   */
  int findColumn(MouseEvent evt)
  {
    int res = 0;
    int x = evt.getX();

    final int startRes = av.getRanges().getStartRes();
    final int charWidth = av.getCharWidth();

    if (av.getWrapAlignment())
    {
      int hgap = av.getCharHeight();
      if (av.getScaleAboveWrapped())
      {
        hgap += av.getCharHeight();
      }

      int cHeight = av.getAlignment().getHeight() * av.getCharHeight()
              + hgap + seqCanvas.getAnnotationHeight();

      int y = evt.getY();
      y = Math.max(0, y - hgap);
      x -= seqCanvas.getLabelWidthWest();
      if (x < 0)
      {
        // mouse is over left scale
        return -1;
      }

      int cwidth = seqCanvas.getWrappedCanvasWidth(this.getWidth());
      if (cwidth < 1)
      {
        return 0;
      }
      if (x >= cwidth * charWidth)
      {
        // mouse is over right scale
        return -1;
      }

      wrappedBlock = y / cHeight;
      wrappedBlock += startRes / cwidth;
      // allow for wrapped view scrolled right (possible from Overview)
      int startOffset = startRes % cwidth;
      res = wrappedBlock * cwidth + startOffset
              + Math.min(cwidth - 1, x / charWidth);
    }
    else
    {
      /*
       * make sure we calculate relative to visible alignment, 
       * rather than right-hand gutter
       */
      x = Math.min(x, seqCanvas.getX() + seqCanvas.getWidth());
      res = (x / charWidth) + startRes;
      res = Math.min(res, av.getRanges().getEndRes());
    }

    if (av.hasHiddenColumns())
    {
      res = av.getAlignment().getHiddenColumns()
              .visibleToAbsoluteColumn(res);
    }

    return res;
  }

  /**
   * When all of a sequence of edits are complete, put the resulting edit list
   * on the history stack (undo list), and reset flags for editing in progress.
   */
  void endEditing()
  {
    try
    {
      if (editCommand != null && editCommand.getSize() > 0)
      {
        ap.alignFrame.addHistoryItem(editCommand);
        av.firePropertyChange("alignment", null,
                av.getAlignment().getSequences());
      }
    } finally
    {
      /*
       * Tidy up come what may...
       */
      editStartSeq = -1;
      editLastRes = -1;
      editingSeqs = false;
      groupEditing = false;
      keyboardNo1 = null;
      keyboardNo2 = null;
      editCommand = null;
    }
  }

  void setCursorRow()
  {
    seqCanvas.cursorY = getKeyboardNo1() - 1;
    scrollToVisible(true);
  }

  void setCursorColumn()
  {
    seqCanvas.cursorX = getKeyboardNo1() - 1;
    scrollToVisible(true);
  }

  void setCursorRowAndColumn()
  {
    if (keyboardNo2 == null)
    {
      keyboardNo2 = new StringBuffer();
    }
    else
    {
      seqCanvas.cursorX = getKeyboardNo1() - 1;
      seqCanvas.cursorY = getKeyboardNo2() - 1;
      scrollToVisible(true);
    }
  }

  void setCursorPosition()
  {
    SequenceI sequence = av.getAlignment().getSequenceAt(seqCanvas.cursorY);

    seqCanvas.cursorX = sequence.findIndex(getKeyboardNo1()) - 1;
    scrollToVisible(true);
  }

  void moveCursor(int dx, int dy)
  {
    moveCursor(dx, dy, false);
  }

  void moveCursor(int dx, int dy, boolean nextWord)
  {
    HiddenColumns hidden = av.getAlignment().getHiddenColumns();

    if (nextWord)
    {
      int maxWidth = av.getAlignment().getWidth();
      int maxHeight = av.getAlignment().getHeight();
      SequenceI seqAtRow = av.getAlignment()
              .getSequenceAt(seqCanvas.cursorY);
      // look for next gap or residue
      boolean isGap = Comparison
              .isGap(seqAtRow.getCharAt(seqCanvas.cursorX));
      int p = seqCanvas.cursorX, lastP, r = seqCanvas.cursorY, lastR;
      do
      {
        lastP = p;
        lastR = r;
        if (dy != 0)
        {
          r += dy;
          if (r < 0)
          {
            r = 0;
          }
          if (r >= maxHeight)
          {
            r = maxHeight - 1;
          }
          seqAtRow = av.getAlignment().getSequenceAt(r);
        }
        p = nextVisible(hidden, maxWidth, p, dx);
      } while ((dx != 0 ? p != lastP : r != lastR)
              && isGap == Comparison.isGap(seqAtRow.getCharAt(p)));
      seqCanvas.cursorX = p;
      seqCanvas.cursorY = r;
    }
    else
    {
      int maxWidth = av.getAlignment().getWidth();
      seqCanvas.cursorX = nextVisible(hidden, maxWidth, seqCanvas.cursorX,
              dx);
      seqCanvas.cursorY += dy;
    }
    scrollToVisible(false);
  }

  private int nextVisible(HiddenColumns hidden, int maxWidth, int original,
          int dx)
  {
    int newCursorX = original + dx;
    if (av.hasHiddenColumns() && !hidden.isVisible(newCursorX))
    {
      int visx = hidden.absoluteToVisibleColumn(newCursorX - dx);
      int[] region = hidden.getRegionWithEdgeAtRes(visx);

      if (region != null) // just in case
      {
        if (dx == 1)
        {
          // moving right
          newCursorX = region[1] + 1;
        }
        else if (dx == -1)
        {
          // moving left
          newCursorX = region[0] - 1;
        }
      }
    }
    newCursorX = (newCursorX < 0) ? 0 : newCursorX;
    if (newCursorX >= maxWidth || !hidden.isVisible(newCursorX))
    {
      newCursorX = original;
    }
    return newCursorX;
  }

  /**
   * Scroll to make the cursor visible in the viewport.
   * 
   * @param jump
   *          just jump to the location rather than scrolling
   */
  void scrollToVisible(boolean jump)
  {
    if (seqCanvas.cursorX < 0)
    {
      seqCanvas.cursorX = 0;
    }
    else if (seqCanvas.cursorX > av.getAlignment().getWidth() - 1)
    {
      seqCanvas.cursorX = av.getAlignment().getWidth() - 1;
    }

    if (seqCanvas.cursorY < 0)
    {
      seqCanvas.cursorY = 0;
    }
    else if (seqCanvas.cursorY > av.getAlignment().getHeight() - 1)
    {
      seqCanvas.cursorY = av.getAlignment().getHeight() - 1;
    }

    endEditing();

    boolean repaintNeeded = true;
    if (jump)
    {
      // only need to repaint if the viewport did not move, as otherwise it will
      // get a repaint
      repaintNeeded = !av.getRanges().setViewportLocation(seqCanvas.cursorX,
              seqCanvas.cursorY);
    }
    else
    {
      if (av.getWrapAlignment())
      {
        // scrollToWrappedVisible expects x-value to have hidden cols subtracted
        int x = av.getAlignment().getHiddenColumns()
                .absoluteToVisibleColumn(seqCanvas.cursorX);
        av.getRanges().scrollToWrappedVisible(x);
      }
      else
      {
        av.getRanges().scrollToVisible(seqCanvas.cursorX,
                seqCanvas.cursorY);
      }
    }

    if (av.getAlignment().getHiddenColumns().isVisible(seqCanvas.cursorX))
    {
      setStatusMessage(av.getAlignment().getSequenceAt(seqCanvas.cursorY),
              seqCanvas.cursorX, seqCanvas.cursorY);
    }

    if (repaintNeeded)
    {
      seqCanvas.repaint();
    }
  }

  void setSelectionAreaAtCursor(boolean topLeft)
  {
    SequenceI sequence = av.getAlignment().getSequenceAt(seqCanvas.cursorY);

    if (av.getSelectionGroup() != null)
    {
      SequenceGroup sg = av.getSelectionGroup();
      // Find the top and bottom of this group
      int min = av.getAlignment().getHeight(), max = 0;
      for (int i = 0; i < sg.getSize(); i++)
      {
        int index = av.getAlignment().findIndex(sg.getSequenceAt(i));
        if (index > max)
        {
          max = index;
        }
        if (index < min)
        {
          min = index;
        }
      }

      max++;

      if (topLeft)
      {
        sg.setStartRes(seqCanvas.cursorX);
        if (sg.getEndRes() < seqCanvas.cursorX)
        {
          sg.setEndRes(seqCanvas.cursorX);
        }

        min = seqCanvas.cursorY;
      }
      else
      {
        sg.setEndRes(seqCanvas.cursorX);
        if (sg.getStartRes() > seqCanvas.cursorX)
        {
          sg.setStartRes(seqCanvas.cursorX);
        }

        max = seqCanvas.cursorY + 1;
      }

      if (min > max)
      {
        // Only the user can do this
        av.setSelectionGroup(null);
      }
      else
      {
        // Now add any sequences between min and max
        sg.getSequences(null).clear();
        for (int i = min; i < max; i++)
        {
          sg.addSequence(av.getAlignment().getSequenceAt(i), false);
        }
      }
    }

    if (av.getSelectionGroup() == null)
    {
      SequenceGroup sg = new SequenceGroup();
      sg.setStartRes(seqCanvas.cursorX);
      sg.setEndRes(seqCanvas.cursorX);
      sg.addSequence(sequence, false);
      av.setSelectionGroup(sg);
    }

    ap.paintAlignment(false, false);
    av.sendSelection();
  }

  void insertGapAtCursor(boolean group)
  {
    groupEditing = group;
    editStartSeq = seqCanvas.cursorY;
    editLastRes = seqCanvas.cursorX;
    editSequence(true, false, seqCanvas.cursorX + getKeyboardNo1());
    endEditing();
  }

  void deleteGapAtCursor(boolean group)
  {
    groupEditing = group;
    editStartSeq = seqCanvas.cursorY;
    editLastRes = seqCanvas.cursorX + getKeyboardNo1();
    editSequence(false, false, seqCanvas.cursorX);
    endEditing();
  }

  void insertNucAtCursor(boolean group, String nuc)
  {
    // TODO not called - delete?
    groupEditing = group;
    editStartSeq = seqCanvas.cursorY;
    editLastRes = seqCanvas.cursorX;
    editSequence(false, true, seqCanvas.cursorX + getKeyboardNo1());
    endEditing();
  }

  void numberPressed(char value)
  {
    if (keyboardNo1 == null)
    {
      keyboardNo1 = new StringBuffer();
    }

    if (keyboardNo2 != null)
    {
      keyboardNo2.append(value);
    }
    else
    {
      keyboardNo1.append(value);
    }
  }

  int getKeyboardNo1()
  {
    try
    {
      if (keyboardNo1 != null)
      {
        int value = Integer.parseInt(keyboardNo1.toString());
        keyboardNo1 = null;
        return value;
      }
    } catch (Exception x)
    {
    }
    keyboardNo1 = null;
    return 1;
  }

  int getKeyboardNo2()
  {
    try
    {
      if (keyboardNo2 != null)
      {
        int value = Integer.parseInt(keyboardNo2.toString());
        keyboardNo2 = null;
        return value;
      }
    } catch (Exception x)
    {
    }
    keyboardNo2 = null;
    return 1;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  @Override
  public void mouseReleased(MouseEvent evt)
  {
    MousePos pos = findMousePosition(evt);
    if (pos.isOverAnnotation() || pos.seqIndex == -1 || pos.column == -1)
    {
      return;
    }

    boolean didDrag = mouseDragging; // did we come here after a drag
    mouseDragging = false;
    mouseWheelPressed = false;

    if (evt.isPopupTrigger()) // Windows: mouseReleased
    {
      showPopupMenu(evt, pos);
      evt.consume();
      return;
    }

    if (editingSeqs)
    {
      endEditing();
    }
    else
    {
      doMouseReleasedDefineMode(evt, didDrag);
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  @Override
  public void mousePressed(MouseEvent evt)
  {
    lastMousePress = evt.getPoint();
    MousePos pos = findMousePosition(evt);
    if (pos.isOverAnnotation() || pos.seqIndex == -1 || pos.column == -1)
    {
      return;
    }

    if (SwingUtilities.isMiddleMouseButton(evt))
    {
      mouseWheelPressed = true;
      return;
    }

    boolean isControlDown = Platform.isControlDown(evt);
    if (evt.isShiftDown() || isControlDown)
    {
      editingSeqs = true;
      if (isControlDown)
      {
        groupEditing = true;
      }
    }
    else
    {
      doMousePressedDefineMode(evt, pos);
      return;
    }

    int seq = pos.seqIndex;
    int res = pos.column;

    if ((seq < av.getAlignment().getHeight())
            && (res < av.getAlignment().getSequenceAt(seq).getLength()))
    {
      editStartSeq = seq;
      editLastRes = res;
    }
    else
    {
      editStartSeq = -1;
      editLastRes = -1;
    }

    return;
  }

  String lastMessage;

  @Override
  public void mouseOverSequence(SequenceI sequence, int index, int pos)
  {
    String tmp = sequence.hashCode() + " " + index + " " + pos;

    if (lastMessage == null || !lastMessage.equals(tmp))
    {
      // System.err.println("mouseOver Sequence: "+tmp);
      ssm.mouseOverSequence(sequence, index, pos, av);
    }
    lastMessage = tmp;
  }

  /**
   * Highlight the mapped region described by the search results object (unless
   * unchanged). This supports highlight of protein while mousing over linked
   * cDNA and vice versa. The status bar is also updated to show the location of
   * the start of the highlighted region.
   */
  @Override
  public String highlightSequence(SearchResultsI results)
  {
    if (results == null || results.equals(lastSearchResults))
    {
      return null;
    }
    lastSearchResults = results;

    boolean wasScrolled = false;

    if (av.isFollowHighlight())
    {
      // don't allow highlight of protein/cDNA to also scroll a complementary
      // panel,as this sets up a feedback loop (scrolling panel 1 causes moused
      // over residue to change abruptly, causing highlighted residue in panel 2
      // to change, causing a scroll in panel 1 etc)
      ap.setToScrollComplementPanel(false);
      wasScrolled = ap.scrollToPosition(results);
      if (wasScrolled)
      {
        seqCanvas.revalidate();
      }
      ap.setToScrollComplementPanel(true);
    }

    boolean fastPaint = !(wasScrolled && av.getWrapAlignment());
    if (seqCanvas.highlightSearchResults(results, fastPaint))
    {
      setStatusMessage(results);
    }
    return results.isEmpty() ? null : getHighlightInfo(results);
  }

  /**
   * temporary hack: answers a message suitable to show on structure hover
   * label. This is normally null. It is a peptide variation description if
   * <ul>
   * <li>results are a single residue in a protein alignment</li>
   * <li>there is a mapping to a coding sequence (codon)</li>
   * <li>there are one or more SNP variant features on the codon</li>
   * </ul>
   * in which case the answer is of the format (e.g.) "p.Glu388Asp"
   * 
   * @param results
   * @return
   */
  private String getHighlightInfo(SearchResultsI results)
  {
    /*
     * ideally, just find mapped CDS (as we don't care about render style here);
     * for now, go via split frame complement's FeatureRenderer
     */
    AlignViewportI complement = ap.getAlignViewport().getCodingComplement();
    if (complement == null)
    {
      return null;
    }
    AlignFrame af = Desktop.getAlignFrameFor(complement);
    FeatureRendererModel fr2 = af.getFeatureRenderer();

    List<SearchResultMatchI> matches = results.getResults();
    int j = matches.size();
    List<String> infos = new ArrayList<>();
    for (int i = 0; i < j; i++)
    {
      SearchResultMatchI match = matches.get(i);
      int pos = match.getStart();
      if (pos == match.getEnd())
      {
        SequenceI seq = match.getSequence();
        SequenceI ds = seq.getDatasetSequence() == null ? seq
                : seq.getDatasetSequence();
        MappedFeatures mf = fr2.findComplementFeaturesAtResidue(ds, pos);
        if (mf != null)
        {
          for (SequenceFeature sf : mf.features)
          {
            String pv = mf.findProteinVariants(sf);
            if (pv.length() > 0 && !infos.contains(pv))
            {
              infos.add(pv);
            }
          }
        }
      }
    }

    if (infos.isEmpty())
    {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    for (String info : infos)
    {
      if (sb.length() > 0)
      {
        sb.append("|");
      }
      sb.append(info);
    }
    return sb.toString();
  }

  @Override
  public VamsasSource getVamsasSource()
  {
    return this.ap == null ? null : this.ap.av;
  }

  @Override
  public void updateColours(SequenceI seq, int index)
  {
    System.out.println("update the seqPanel colours");
    // repaint();
  }

  /**
   * Action on mouse movement is to update the status bar to show the current
   * sequence position, and (if features are shown) to show any features at the
   * position in a tooltip. Does nothing if the mouse move does not change
   * residue position.
   * 
   * @param evt
   */
  @Override
  public void mouseMoved(MouseEvent evt)
  {
    if (editingSeqs)
    {
      // This is because MacOSX creates a mouseMoved
      // If control is down, other platforms will not.
      mouseDragged(evt);
    }

    final MousePos mousePos = findMousePosition(evt);
    if (mousePos.equals(lastMousePosition))
    {
      /*
       * just a pixel move without change of 'cell'
       */
      moveTooltip = false;
      return;
    }
    moveTooltip = true;
    lastMousePosition = mousePos;

    if (mousePos.isOverAnnotation())
    {
      mouseMovedOverAnnotation(mousePos);
      return;
    }
    final int seq = mousePos.seqIndex;

    final int column = mousePos.column;
    if (column < 0 || seq < 0 || seq >= av.getAlignment().getHeight())
    {
      lastMousePosition = null;
      setToolTipText(null);
      lastTooltip = null;
      lastFormattedTooltip = null;
      ap.alignFrame.setStatus("");
      return;
    }

    SequenceI sequence = av.getAlignment().getSequenceAt(seq);

    if (column >= sequence.getLength())
    {
      return;
    }

    /*
     * set status bar message, returning residue position in sequence
     */
    boolean isGapped = Comparison.isGap(sequence.getCharAt(column));
    final int pos = setStatusMessage(sequence, column, seq);
    if (ssm != null && !isGapped)
    {
      mouseOverSequence(sequence, column, pos);
    }

    StringBuilder tooltipText = new StringBuilder(64);

    SequenceGroup[] groups = av.getAlignment().findAllGroups(sequence);
    if (groups != null)
    {
      for (int g = 0; g < groups.length; g++)
      {
        if (groups[g].getStartRes() <= column
                && groups[g].getEndRes() >= column)
        {
          if (!groups[g].getName().startsWith("JTreeGroup")
                  && !groups[g].getName().startsWith("JGroup"))
          {
            tooltipText.append(groups[g].getName());
          }

          if (groups[g].getDescription() != null)
          {
            tooltipText.append(": " + groups[g].getDescription());
          }
        }
      }
    }

    /*
     * add any features at the position to the tooltip; if over a gap, only
     * add features that straddle the gap (pos may be the residue before or
     * after the gap)
     */
    int unshownFeatures = 0;
    if (av.isShowSequenceFeatures())
    {
      List<SequenceFeature> features = ap.getFeatureRenderer()
              .findFeaturesAtColumn(sequence, column + 1);
      unshownFeatures = seqARep.appendFeatures(tooltipText, pos, features,
              this.ap.getSeqPanel().seqCanvas.fr, MAX_TOOLTIP_LENGTH);

      /*
       * add features in CDS/protein complement at the corresponding
       * position if configured to do so
       */
      if (av.isShowComplementFeatures())
      {
        if (!Comparison.isGap(sequence.getCharAt(column)))
        {
          AlignViewportI complement = ap.getAlignViewport()
                  .getCodingComplement();
          AlignFrame af = Desktop.getAlignFrameFor(complement);
          FeatureRendererModel fr2 = af.getFeatureRenderer();
          MappedFeatures mf = fr2.findComplementFeaturesAtResidue(sequence,
                  pos);
          if (mf != null)
          {
            unshownFeatures += seqARep.appendFeatures(tooltipText, pos, mf,
                    fr2, MAX_TOOLTIP_LENGTH);
          }
        }
      }
    }
    if (tooltipText.length() == 0) // nothing added
    {
      setToolTipText(null);
      lastTooltip = null;
    }
    else
    {
      if (tooltipText.length() > MAX_TOOLTIP_LENGTH)
      {
        tooltipText.setLength(MAX_TOOLTIP_LENGTH);
        tooltipText.append("...");
      }
      if (unshownFeatures > 0)
      {
        tooltipText.append("<br/>").append("... ").append("<i>")
                .append(MessageManager.formatMessage(
                        "label.features_not_shown", unshownFeatures))
                .append("</i>");
      }
      String textString = tooltipText.toString();
      if (!textString.equals(lastTooltip))
      {
        lastTooltip = textString;
        lastFormattedTooltip = JvSwingUtils.wrapTooltip(true, textString);
        setToolTipText(lastFormattedTooltip);
      }
    }
  }

  /**
   * When the view is in wrapped mode, and the mouse is over an annotation row,
   * shows the corresponding tooltip and status message (if any)
   * 
   * @param pos
   * @param column
   */
  protected void mouseMovedOverAnnotation(MousePos pos)
  {
    final int column = pos.column;
    final int rowIndex = pos.annotationIndex;

    // TODO - get yOffset for annotation, too
    if (column < 0 || !av.getWrapAlignment() || !av.isShowAnnotation()
            || rowIndex < 0)
    {
      return;
    }
    AlignmentAnnotation[] anns = av.getAlignment().getAlignmentAnnotation();

    String tooltip = AnnotationPanel.buildToolTip(anns[rowIndex], column,
            anns, 0, av, ap);
    if (tooltip == null ? tooltip != lastTooltip
            : !tooltip.equals(lastTooltip))
    {
      lastTooltip = tooltip;
      lastFormattedTooltip = tooltip == null ? null
              : JvSwingUtils.wrapTooltip(true, tooltip);
      setToolTipText(lastFormattedTooltip);
    }

    String msg = AnnotationPanel.getStatusMessage(av.getAlignment(), column,
            anns[rowIndex], 0, av);
    ap.alignFrame.setStatus(msg);
  }

  /*
   * if Shift key is held down while moving the mouse, 
   * the tooltip location is not changed once shown
   */
  private Point lastTooltipLocation = null;

  /*
   * this flag is false for pixel moves within a residue,
   * to reduce tooltip flicker
   */
  private boolean moveTooltip = true;

  /*
   * a dummy tooltip used to estimate where to position tooltips
   */
  private JToolTip tempTip = new JLabel().createToolTip();

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComponent#getToolTipLocation(java.awt.event.MouseEvent)
   */
  @Override
  public Point getToolTipLocation(MouseEvent event)
  {
    // BH 2018

    if (lastTooltip == null || !moveTooltip)
    {
      return null;
    }

    if (lastTooltipLocation != null && event.isShiftDown())
    {
      return lastTooltipLocation;
    }

    int x = event.getX();
    int y = event.getY();
    int w = getWidth();

    tempTip.setTipText(lastFormattedTooltip);
    int tipWidth = (int) tempTip.getPreferredSize().getWidth();

    // was x += (w - x < 200) ? -(w / 2) : 5;
    x = (x + tipWidth < w ? x + 10 : w - tipWidth);
    Point p = new Point(x, y + av.getCharHeight()); // BH 2018 was - 20?

    return lastTooltipLocation = p;
  }

  /**
   * set when the current UI interaction has resulted in a change that requires
   * shading in overviews and structures to be recalculated. this could be
   * changed to a something more expressive that indicates what actually has
   * changed, so selective redraws can be applied (ie. only structures, only
   * overview, etc)
   */
  private boolean updateOverviewAndStructs = false; // TODO: refactor to
                                                    // avcontroller

  /**
   * set if av.getSelectionGroup() refers to a group that is defined on the
   * alignment view, rather than a transient selection
   */
  // private boolean editingDefinedGroup = false; // TODO: refactor to
  // avcontroller or viewModel

  /**
   * Sets the status message in alignment panel, showing the sequence number
   * (index) and id, and residue and residue position if not at a gap, for the
   * given sequence and column position. Returns the residue position returned
   * by Sequence.findPosition. Note this may be for the nearest adjacent residue
   * if at a gapped position.
   * 
   * @param sequence
   *          aligned sequence object
   * @param column
   *          alignment column
   * @param seqIndex
   *          index of sequence in alignment
   * @return sequence position of residue at column, or adjacent residue if at a
   *         gap
   */
  int setStatusMessage(SequenceI sequence, final int column, int seqIndex)
  {
    char sequenceChar = sequence.getCharAt(column);
    int pos = sequence.findPosition(column);
    setStatusMessage(sequence.getName(), seqIndex, sequenceChar, pos);

    return pos;
  }

  /**
   * Builds the status message for the current cursor location and writes it to
   * the status bar, for example
   * 
   * <pre>
   * Sequence 3 ID: FER1_SOLLC
   * Sequence 5 ID: FER1_PEA Residue: THR (4)
   * Sequence 5 ID: FER1_PEA Residue: B (3)
   * Sequence 6 ID: O.niloticus.3 Nucleotide: Uracil (2)
   * </pre>
   * 
   * @param seqName
   * @param seqIndex
   *          sequence position in the alignment (1..)
   * @param sequenceChar
   *          the character under the cursor
   * @param residuePos
   *          the sequence residue position (if not over a gap)
   */
  protected void setStatusMessage(String seqName, int seqIndex,
          char sequenceChar, int residuePos)
  {
    StringBuilder text = new StringBuilder(32);

    /*
     * Sequence number (if known), and sequence name.
     */
    String seqno = seqIndex == -1 ? "" : " " + (seqIndex + 1);
    text.append("Sequence").append(seqno).append(" ID: ").append(seqName);

    String residue = null;

    /*
     * Try to translate the display character to residue name (null for gap).
     */
    boolean isGapped = Comparison.isGap(sequenceChar);

    if (!isGapped)
    {
      boolean nucleotide = av.getAlignment().isNucleotide();
      String displayChar = String.valueOf(sequenceChar);
      if (nucleotide)
      {
        residue = ResidueProperties.nucleotideName.get(displayChar);
      }
      else
      {
        residue = "X".equalsIgnoreCase(displayChar) ? "X"
                : ("*".equals(displayChar) ? "STOP"
                        : ResidueProperties.aa2Triplet.get(displayChar));
      }
      text.append(" ").append(nucleotide ? "Nucleotide" : "Residue")
              .append(": ").append(residue == null ? displayChar : residue);

      text.append(" (").append(Integer.toString(residuePos)).append(")");
    }
    ap.alignFrame.setStatus(text.toString());
  }

  /**
   * Set the status bar message to highlight the first matched position in
   * search results.
   * 
   * @param results
   */
  private void setStatusMessage(SearchResultsI results)
  {
    AlignmentI al = this.av.getAlignment();
    int sequenceIndex = al.findIndex(results);
    if (sequenceIndex == -1)
    {
      return;
    }
    SequenceI alignedSeq = al.getSequenceAt(sequenceIndex);
    SequenceI ds = alignedSeq.getDatasetSequence();
    for (SearchResultMatchI m : results.getResults())
    {
      SequenceI seq = m.getSequence();
      if (seq.getDatasetSequence() != null)
      {
        seq = seq.getDatasetSequence();
      }

      if (seq == ds)
      {
        int start = m.getStart();
        setStatusMessage(alignedSeq.getName(), sequenceIndex,
                seq.getCharAt(start - 1), start);
        return;
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void mouseDragged(MouseEvent evt)
  {
    MousePos pos = findMousePosition(evt);
    if (pos.isOverAnnotation() || pos.column == -1)
    {
      return;
    }

    if (mouseWheelPressed)
    {
      boolean inSplitFrame = ap.av.getCodingComplement() != null;
      boolean copyChanges = inSplitFrame && av.isProteinFontAsCdna();

      int oldWidth = av.getCharWidth();

      // Which is bigger, left-right or up-down?
      if (Math.abs(evt.getY() - lastMousePress.getY()) > Math
              .abs(evt.getX() - lastMousePress.getX()))
      {
        /*
         * on drag up or down, decrement or increment font size
         */
        int fontSize = av.font.getSize();
        boolean fontChanged = false;

        if (evt.getY() < lastMousePress.getY())
        {
          fontChanged = true;
          fontSize--;
        }
        else if (evt.getY() > lastMousePress.getY())
        {
          fontChanged = true;
          fontSize++;
        }

        if (fontSize < 1)
        {
          fontSize = 1;
        }

        if (fontChanged)
        {
          Font newFont = new Font(av.font.getName(), av.font.getStyle(),
                  fontSize);
          av.setFont(newFont, true);
          av.setCharWidth(oldWidth);
          ap.fontChanged();
          if (copyChanges)
          {
            ap.av.getCodingComplement().setFont(newFont, true);
            SplitFrame splitFrame = (SplitFrame) ap.alignFrame
                    .getSplitViewContainer();
            splitFrame.adjustLayout();
            splitFrame.repaint();
          }
        }
      }
      else
      {
        /*
         * on drag left or right, decrement or increment character width
         */
        int newWidth = 0;
        if (evt.getX() < lastMousePress.getX() && av.getCharWidth() > 1)
        {
          newWidth = av.getCharWidth() - 1;
          av.setCharWidth(newWidth);
        }
        else if (evt.getX() > lastMousePress.getX())
        {
          newWidth = av.getCharWidth() + 1;
          av.setCharWidth(newWidth);
        }
        if (newWidth > 0)
        {
          ap.paintAlignment(false, false);
          if (copyChanges)
          {
            /*
             * need to ensure newWidth is set on cdna, regardless of which
             * panel the mouse drag happened in; protein will compute its 
             * character width as 1:1 or 3:1
             */
            av.getCodingComplement().setCharWidth(newWidth);
            SplitFrame splitFrame = (SplitFrame) ap.alignFrame
                    .getSplitViewContainer();
            splitFrame.adjustLayout();
            splitFrame.repaint();
          }
        }
      }

      FontMetrics fm = getFontMetrics(av.getFont());
      av.validCharWidth = fm.charWidth('M') <= av.getCharWidth();

      lastMousePress = evt.getPoint();

      return;
    }

    if (!editingSeqs)
    {
      dragStretchGroup(evt);
      return;
    }

    int res = pos.column;

    if (res < 0)
    {
      res = 0;
    }

    if ((editLastRes == -1) || (editLastRes == res))
    {
      return;
    }

    if ((res < av.getAlignment().getWidth()) && (res < editLastRes))
    {
      // dragLeft, delete gap
      editSequence(false, false, res);
    }
    else
    {
      editSequence(true, false, res);
    }

    mouseDragging = true;
    if (scrollThread != null)
    {
      scrollThread.setMousePosition(evt.getPoint());
    }
  }

  /**
   * Edits the sequence to insert or delete one or more gaps, in response to a
   * mouse drag or cursor mode command. The number of inserts/deletes may be
   * specified with the cursor command, or else depends on the mouse event
   * (normally one column, but potentially more for a fast mouse drag).
   * <p>
   * Delete gaps is limited to the number of gaps left of the cursor position
   * (mouse drag), or at or right of the cursor position (cursor mode).
   * <p>
   * In group editing mode (Ctrl or Cmd down), the edit acts on all sequences in
   * the current selection group.
   * <p>
   * In locked editing mode (with a selection group present), inserts/deletions
   * within the selection group are limited to its boundaries (and edits outside
   * the group stop at its border).
   * 
   * @param insertGap
   *          true to insert gaps, false to delete gaps
   * @param editSeq
   *          (unused parameter)
   * @param startres
   *          the column at which to perform the action; the number of columns
   *          affected depends on <code>this.editLastRes</code> (cursor column
   *          position)
   */
  synchronized void editSequence(boolean insertGap, boolean editSeq,
          final int startres)
  {
    int fixedLeft = -1;
    int fixedRight = -1;
    boolean fixedColumns = false;
    SequenceGroup sg = av.getSelectionGroup();

    final SequenceI seq = av.getAlignment().getSequenceAt(editStartSeq);

    // No group, but the sequence may represent a group
    if (!groupEditing && av.hasHiddenRows())
    {
      if (av.isHiddenRepSequence(seq))
      {
        sg = av.getRepresentedSequences(seq);
        groupEditing = true;
      }
    }

    StringBuilder message = new StringBuilder(64); // for status bar

    /*
     * make a name for the edit action, for
     * status bar message and Undo/Redo menu
     */
    String label = null;
    if (groupEditing)
    {
      message.append("Edit group:");
      label = MessageManager.getString("action.edit_group");
    }
    else
    {
      message.append("Edit sequence: " + seq.getName());
      label = seq.getName();
      if (label.length() > 10)
      {
        label = label.substring(0, 10);
      }
      label = MessageManager.formatMessage("label.edit_params",
              new String[]
              { label });
    }

    /*
     * initialise the edit command if there is not
     * already one being extended
     */
    if (editCommand == null)
    {
      editCommand = new EditCommand(label);
    }

    if (insertGap)
    {
      message.append(" insert ");
    }
    else
    {
      message.append(" delete ");
    }

    message.append(Math.abs(startres - editLastRes) + " gaps.");
    ap.alignFrame.setStatus(message.toString());

    /*
     * is there a selection group containing the sequence being edited?
     * if so the boundary of the group is the limit of the edit
     * (but the edit may be inside or outside the selection group)
     */
    boolean inSelectionGroup = sg != null
            && sg.getSequences(av.getHiddenRepSequences()).contains(seq);
    if (groupEditing || inSelectionGroup)
    {
      fixedColumns = true;

      // sg might be null as the user may only see 1 sequence,
      // but the sequence represents a group
      if (sg == null)
      {
        if (!av.isHiddenRepSequence(seq))
        {
          endEditing();
          return;
        }
        sg = av.getRepresentedSequences(seq);
      }

      fixedLeft = sg.getStartRes();
      fixedRight = sg.getEndRes();

      if ((startres < fixedLeft && editLastRes >= fixedLeft)
              || (startres >= fixedLeft && editLastRes < fixedLeft)
              || (startres > fixedRight && editLastRes <= fixedRight)
              || (startres <= fixedRight && editLastRes > fixedRight))
      {
        endEditing();
        return;
      }

      if (fixedLeft > startres)
      {
        fixedRight = fixedLeft - 1;
        fixedLeft = 0;
      }
      else if (fixedRight < startres)
      {
        fixedLeft = fixedRight;
        fixedRight = -1;
      }
    }

    if (av.hasHiddenColumns())
    {
      fixedColumns = true;
      int y1 = av.getAlignment().getHiddenColumns()
              .getNextHiddenBoundary(true, startres);
      int y2 = av.getAlignment().getHiddenColumns()
              .getNextHiddenBoundary(false, startres);

      if ((insertGap && startres > y1 && editLastRes < y1)
              || (!insertGap && startres < y2 && editLastRes > y2))
      {
        endEditing();
        return;
      }

      // System.out.print(y1+" "+y2+" "+fixedLeft+" "+fixedRight+"~~");
      // Selection spans a hidden region
      if (fixedLeft < y1 && (fixedRight > y2 || fixedRight == -1))
      {
        if (startres >= y2)
        {
          fixedLeft = y2;
        }
        else
        {
          fixedRight = y2 - 1;
        }
      }
    }

    boolean success = doEditSequence(insertGap, editSeq, startres,
            fixedRight, fixedColumns, sg);

    /*
     * report what actually happened (might be less than
     * what was requested), by inspecting the edit commands added
     */
    String msg = getEditStatusMessage(editCommand);
    ap.alignFrame.setStatus(msg == null ? " " : msg);
    if (!success)
    {
      endEditing();
    }

    editLastRes = startres;
    seqCanvas.repaint();
  }

  /**
   * A helper method that performs the requested editing to insert or delete
   * gaps (if possible). Answers true if the edit was successful, false if could
   * only be performed in part or not at all. Failure may occur in 'locked edit'
   * mode, when an insertion requires a matching gapped position (or column) to
   * delete, and deletion requires an adjacent gapped position (or column) to
   * remove.
   * 
   * @param insertGap
   *          true if inserting gap(s), false if deleting
   * @param editSeq
   *          (unused parameter, currently always false)
   * @param startres
   *          the column at which to perform the edit
   * @param fixedRight
   *          fixed right boundary column of a locked edit (within or to the
   *          left of a selection group)
   * @param fixedColumns
   *          true if this is a locked edit
   * @param sg
   *          the sequence group (if group edit is being performed)
   * @return
   */
  protected boolean doEditSequence(final boolean insertGap,
          final boolean editSeq, final int startres, int fixedRight,
          final boolean fixedColumns, final SequenceGroup sg)
  {
    final SequenceI seq = av.getAlignment().getSequenceAt(editStartSeq);
    SequenceI[] seqs = new SequenceI[] { seq };

    if (groupEditing)
    {
      List<SequenceI> vseqs = sg.getSequences(av.getHiddenRepSequences());
      int g, groupSize = vseqs.size();
      SequenceI[] groupSeqs = new SequenceI[groupSize];
      for (g = 0; g < groupSeqs.length; g++)
      {
        groupSeqs[g] = vseqs.get(g);
      }

      // drag to right
      if (insertGap)
      {
        // If the user has selected the whole sequence, and is dragging to
        // the right, we can still extend the alignment and selectionGroup
        if (sg.getStartRes() == 0 && sg.getEndRes() == fixedRight
                && sg.getEndRes() == av.getAlignment().getWidth() - 1)
        {
          sg.setEndRes(
                  av.getAlignment().getWidth() + startres - editLastRes);
          fixedRight = sg.getEndRes();
        }

        // Is it valid with fixed columns??
        // Find the next gap before the end
        // of the visible region boundary
        boolean blank = false;
        for (; fixedRight > editLastRes; fixedRight--)
        {
          blank = true;

          for (g = 0; g < groupSize; g++)
          {
            for (int j = 0; j < startres - editLastRes; j++)
            {
              if (!Comparison.isGap(groupSeqs[g].getCharAt(fixedRight - j)))
              {
                blank = false;
                break;
              }
            }
          }
          if (blank)
          {
            break;
          }
        }

        if (!blank)
        {
          if (sg.getSize() == av.getAlignment().getHeight())
          {
            if ((av.hasHiddenColumns()
                    && startres < av.getAlignment().getHiddenColumns()
                            .getNextHiddenBoundary(false, startres)))
            {
              return false;
            }

            int alWidth = av.getAlignment().getWidth();
            if (av.hasHiddenRows())
            {
              int hwidth = av.getAlignment().getHiddenSequences()
                      .getWidth();
              if (hwidth > alWidth)
              {
                alWidth = hwidth;
              }
            }
            // We can still insert gaps if the selectionGroup
            // contains all the sequences
            sg.setEndRes(sg.getEndRes() + startres - editLastRes);
            fixedRight = alWidth + startres - editLastRes;
          }
          else
          {
            return false;
          }
        }
      }

      // drag to left
      else if (!insertGap)
      {
        // / Are we able to delete?
        // ie are all columns blank?

        for (g = 0; g < groupSize; g++)
        {
          for (int j = startres; j < editLastRes; j++)
          {
            if (groupSeqs[g].getLength() <= j)
            {
              continue;
            }

            if (!Comparison.isGap(groupSeqs[g].getCharAt(j)))
            {
              // Not a gap, block edit not valid
              return false;
            }
          }
        }
      }

      if (insertGap)
      {
        // dragging to the right
        if (fixedColumns && fixedRight != -1)
        {
          for (int j = editLastRes; j < startres; j++)
          {
            insertGap(j, groupSeqs, fixedRight);
          }
        }
        else
        {
          appendEdit(Action.INSERT_GAP, groupSeqs, startres,
                  startres - editLastRes, false);
        }
      }
      else
      {
        // dragging to the left
        if (fixedColumns && fixedRight != -1)
        {
          for (int j = editLastRes; j > startres; j--)
          {
            deleteChar(startres, groupSeqs, fixedRight);
          }
        }
        else
        {
          appendEdit(Action.DELETE_GAP, groupSeqs, startres,
                  editLastRes - startres, false);
        }
      }
    }
    else
    {
      /*
       * editing a single sequence
       */
      if (insertGap)
      {
        // dragging to the right
        if (fixedColumns && fixedRight != -1)
        {
          for (int j = editLastRes; j < startres; j++)
          {
            if (!insertGap(j, seqs, fixedRight))
            {
              /*
               * e.g. cursor mode command specified 
               * more inserts than are possible
               */
              return false;
            }
          }
        }
        else
        {
          appendEdit(Action.INSERT_GAP, seqs, editLastRes,
                  startres - editLastRes, false);
        }
      }
      else
      {
        if (!editSeq)
        {
          // dragging to the left
          if (fixedColumns && fixedRight != -1)
          {
            for (int j = editLastRes; j > startres; j--)
            {
              if (!Comparison.isGap(seq.getCharAt(startres)))
              {
                return false;
              }
              deleteChar(startres, seqs, fixedRight);
            }
          }
          else
          {
            // could be a keyboard edit trying to delete none gaps
            int max = 0;
            for (int m = startres; m < editLastRes; m++)
            {
              if (!Comparison.isGap(seq.getCharAt(m)))
              {
                break;
              }
              max++;
            }
            if (max > 0)
            {
              appendEdit(Action.DELETE_GAP, seqs, startres, max, false);
            }
          }
        }
        else
        {// insertGap==false AND editSeq==TRUE;
          if (fixedColumns && fixedRight != -1)
          {
            for (int j = editLastRes; j < startres; j++)
            {
              insertGap(j, seqs, fixedRight);
            }
          }
          else
          {
            appendEdit(Action.INSERT_NUC, seqs, editLastRes,
                    startres - editLastRes, false);
          }
        }
      }
    }

    return true;
  }

  /**
   * Constructs an informative status bar message while dragging to insert or
   * delete gaps. Answers null if inserts and deletes cancel out.
   * 
   * @param editCommand
   *          a command containing the list of individual edits
   * @return
   */
  protected static String getEditStatusMessage(EditCommand editCommand)
  {
    if (editCommand == null)
    {
      return null;
    }

    /*
     * add any inserts, and subtract any deletes,  
     * not counting those auto-inserted when doing a 'locked edit'
     * (so only counting edits 'under the cursor')
     */
    int count = 0;
    for (Edit cmd : editCommand.getEdits())
    {
      if (!cmd.isSystemGenerated())
      {
        count += cmd.getAction() == Action.INSERT_GAP ? cmd.getNumber()
                : -cmd.getNumber();
      }
    }

    if (count == 0)
    {
      /*
       * inserts and deletes cancel out
       */
      return null;
    }

    String msgKey = count > 1 ? "label.insert_gaps"
            : (count == 1 ? "label.insert_gap"
                    : (count == -1 ? "label.delete_gap"
                            : "label.delete_gaps"));
    count = Math.abs(count);

    return MessageManager.formatMessage(msgKey, String.valueOf(count));
  }

  /**
   * Inserts one gap at column j, deleting the right-most gapped column up to
   * (and including) fixedColumn. Returns true if the edit is successful, false
   * if no blank column is available to allow the insertion to be balanced by a
   * deletion.
   * 
   * @param j
   * @param seq
   * @param fixedColumn
   * @return
   */
  boolean insertGap(int j, SequenceI[] seq, int fixedColumn)
  {
    int blankColumn = fixedColumn;
    for (int s = 0; s < seq.length; s++)
    {
      // Find the next gap before the end of the visible region boundary
      // If lastCol > j, theres a boundary after the gap insertion

      for (blankColumn = fixedColumn; blankColumn > j; blankColumn--)
      {
        if (Comparison.isGap(seq[s].getCharAt(blankColumn)))
        {
          // Theres a space, so break and insert the gap
          break;
        }
      }

      if (blankColumn <= j)
      {
        blankColumn = fixedColumn;
        endEditing();
        return false;
      }
    }

    appendEdit(Action.DELETE_GAP, seq, blankColumn, 1, true);

    appendEdit(Action.INSERT_GAP, seq, j, 1, false);

    return true;
  }

  /**
   * Helper method to add and perform one edit action
   * 
   * @param action
   * @param seq
   * @param pos
   * @param count
   * @param systemGenerated
   *          true if the edit is a 'balancing' delete (or insert) to match a
   *          user's insert (or delete) in a locked editing region
   */
  protected void appendEdit(Action action, SequenceI[] seq, int pos,
          int count, boolean systemGenerated)
  {

    final Edit edit = new EditCommand().new Edit(action, seq, pos, count,
            av.getAlignment().getGapCharacter());
    edit.setSystemGenerated(systemGenerated);

    editCommand.appendEdit(edit, av.getAlignment(), true, null);
  }

  /**
   * Deletes the character at column j, and inserts a gap at fixedColumn, in
   * each of the given sequences. The caller should ensure that all sequences
   * are gapped in column j.
   * 
   * @param j
   * @param seqs
   * @param fixedColumn
   */
  void deleteChar(int j, SequenceI[] seqs, int fixedColumn)
  {
    appendEdit(Action.DELETE_GAP, seqs, j, 1, false);

    appendEdit(Action.INSERT_GAP, seqs, fixedColumn, 1, true);
  }

  /**
   * On reentering the panel, stops any scrolling that was started on dragging
   * out of the panel
   * 
   * @param e
   */
  @Override
  public void mouseEntered(MouseEvent e)
  {
    if (oldSeq < 0)
    {
      oldSeq = 0;
    }
    stopScrolling();
  }

  /**
   * On leaving the panel, if the mouse is being dragged, starts a thread to
   * scroll it until the mouse is released (in unwrapped mode only)
   * 
   * @param e
   */
  @Override
  public void mouseExited(MouseEvent e)
  {
    lastMousePosition = null;
    ap.alignFrame.setStatus(" ");
    if (av.getWrapAlignment())
    {
      return;
    }

    if (mouseDragging && scrollThread == null)
    {
      startScrolling(e.getPoint());
    }
  }

  /**
   * Handler for double-click on a position with one or more sequence features.
   * Opens the Amend Features dialog to allow feature details to be amended, or
   * the feature deleted.
   */
  @Override
  public void mouseClicked(MouseEvent evt)
  {
    SequenceGroup sg = null;
    MousePos pos = findMousePosition(evt);
    if (pos.isOverAnnotation() || pos.seqIndex == -1 || pos.column == -1)
    {
      return;
    }

    if (evt.getClickCount() > 1 && av.isShowSequenceFeatures())
    {
      sg = av.getSelectionGroup();
      if (sg != null && sg.getSize() == 1
              && sg.getEndRes() - sg.getStartRes() < 2)
      {
        av.setSelectionGroup(null);
      }

      int column = pos.column;

      /*
       * find features at the position (if not gapped), or straddling
       * the position (if at a gap)
       */
      SequenceI sequence = av.getAlignment().getSequenceAt(pos.seqIndex);
      List<SequenceFeature> features = seqCanvas.getFeatureRenderer()
              .findFeaturesAtColumn(sequence, column + 1);

      if (!features.isEmpty())
      {
        /*
         * highlight the first feature at the position on the alignment
         */
        SearchResultsI highlight = new SearchResults();
        highlight.addResult(sequence, features.get(0).getBegin(),
                features.get(0).getEnd());
        seqCanvas.highlightSearchResults(highlight, true);

        /*
         * open the Amend Features dialog
         */
        new FeatureEditor(ap, Collections.singletonList(sequence), features,
                false).showDialog();
      }
    }
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e)
  {
    e.consume();
    double wheelRotation = e.getPreciseWheelRotation();
    if (wheelRotation > 0)
    {
      if (e.isShiftDown())
      {
        av.getRanges().scrollRight(true);

      }
      else
      {
        av.getRanges().scrollUp(false);
      }
    }
    else if (wheelRotation < 0)
    {
      if (e.isShiftDown())
      {
        av.getRanges().scrollRight(false);
      }
      else
      {
        av.getRanges().scrollUp(true);
      }
    }

    /*
     * update status bar and tooltip for new position
     * (need to synthesize a mouse movement to refresh tooltip)
     */
    mouseMoved(e);
    ToolTipManager.sharedInstance().mouseMoved(e);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param pos
   *          DOCUMENT ME!
   */
  protected void doMousePressedDefineMode(MouseEvent evt, MousePos pos)
  {
    if (pos.isOverAnnotation() || pos.seqIndex == -1 || pos.column == -1)
    {
      return;
    }

    final int res = pos.column;
    final int seq = pos.seqIndex;
    oldSeq = seq;
    updateOverviewAndStructs = false;

    startWrapBlock = wrappedBlock;

    SequenceI sequence = av.getAlignment().getSequenceAt(seq);

    if ((sequence == null) || (res > sequence.getLength()))
    {
      return;
    }

    stretchGroup = av.getSelectionGroup();

    if (stretchGroup == null || !stretchGroup.contains(sequence, res))
    {
      stretchGroup = av.getAlignment().findGroup(sequence, res);
      if (stretchGroup != null)
      {
        // only update the current selection if the popup menu has a group to
        // focus on
        av.setSelectionGroup(stretchGroup);
      }
    }

    /*
     * defer right-mouse click handling to mouseReleased on Windows
     * (where isPopupTrigger() will answer true)
     * NB isRightMouseButton is also true for Cmd-click on Mac
     */
    if (Platform.isWinRightButton(evt))
    {
      return;
    }

    if (evt.isPopupTrigger()) // Mac: mousePressed
    {
      showPopupMenu(evt, pos);
      return;
    }

    if (av.cursorMode)
    {
      seqCanvas.cursorX = res;
      seqCanvas.cursorY = seq;
      seqCanvas.repaint();
      return;
    }

    if (stretchGroup == null)
    {
      createStretchGroup(res, sequence);
    }

    if (stretchGroup != null)
    {
      stretchGroup.addPropertyChangeListener(seqCanvas);
    }

    seqCanvas.repaint();
  }

  private void createStretchGroup(int res, SequenceI sequence)
  {
    // Only if left mouse button do we want to change group sizes
    // define a new group here
    SequenceGroup sg = new SequenceGroup();
    sg.setStartRes(res);
    sg.setEndRes(res);
    sg.addSequence(sequence, false);
    av.setSelectionGroup(sg);
    stretchGroup = sg;

    if (av.getConservationSelected())
    {
      SliderPanel.setConservationSlider(ap, av.getResidueShading(),
              ap.getViewName());
    }

    if (av.getAbovePIDThreshold())
    {
      SliderPanel.setPIDSliderSource(ap, av.getResidueShading(),
              ap.getViewName());
    }
    // TODO: stretchGroup will always be not null. Is this a merge error ?
    // or is there a threading issue here?
    if ((stretchGroup != null) && (stretchGroup.getEndRes() == res))
    {
      // Edit end res position of selected group
      changeEndRes = true;
    }
    else if ((stretchGroup != null) && (stretchGroup.getStartRes() == res))
    {
      // Edit end res position of selected group
      changeStartRes = true;
    }
    stretchGroup.getWidth();

  }

  /**
   * Build and show a pop-up menu at the right-click mouse position
   *
   * @param evt
   * @param pos
   */
  void showPopupMenu(MouseEvent evt, MousePos pos)
  {
    final int column = pos.column;
    final int seq = pos.seqIndex;
    SequenceI sequence = av.getAlignment().getSequenceAt(seq);
    if (sequence != null)
    {
      PopupMenu pop = new PopupMenu(ap, sequence, column);
      pop.show(this, evt.getX(), evt.getY());
    }
  }

  /**
   * Update the display after mouse up on a selection or group
   * 
   * @param evt
   *          mouse released event details
   * @param afterDrag
   *          true if this event is happening after a mouse drag (rather than a
   *          mouse down)
   */
  protected void doMouseReleasedDefineMode(MouseEvent evt,
          boolean afterDrag)
  {
    if (stretchGroup == null)
    {
      return;
    }

    stretchGroup.removePropertyChangeListener(seqCanvas);

    // always do this - annotation has own state
    // but defer colourscheme update until hidden sequences are passed in
    boolean vischange = stretchGroup.recalcConservation(true);
    updateOverviewAndStructs |= vischange && av.isSelectionDefinedGroup()
            && afterDrag;
    if (stretchGroup.cs != null)
    {
      if (afterDrag)
      {
        stretchGroup.cs.alignmentChanged(stretchGroup,
                av.getHiddenRepSequences());
      }

      ResidueShaderI groupColourScheme = stretchGroup
              .getGroupColourScheme();
      String name = stretchGroup.getName();
      if (stretchGroup.cs.conservationApplied())
      {
        SliderPanel.setConservationSlider(ap, groupColourScheme, name);
      }
      if (stretchGroup.cs.getThreshold() > 0)
      {
        SliderPanel.setPIDSliderSource(ap, groupColourScheme, name);
      }
    }
    PaintRefresher.Refresh(this, av.getSequenceSetId());
    // TODO: structure colours only need updating if stretchGroup used to or now
    // does contain sequences with structure views
    ap.paintAlignment(updateOverviewAndStructs, updateOverviewAndStructs);
    updateOverviewAndStructs = false;
    changeEndRes = false;
    changeStartRes = false;
    stretchGroup = null;
    av.sendSelection();
  }

  /**
   * Resizes the borders of a selection group depending on the direction of
   * mouse drag
   * 
   * @param evt
   */
  protected void dragStretchGroup(MouseEvent evt)
  {
    if (stretchGroup == null)
    {
      return;
    }

    MousePos pos = findMousePosition(evt);
    if (pos.isOverAnnotation() || pos.column == -1 || pos.seqIndex == -1)
    {
      return;
    }

    int res = pos.column;
    int y = pos.seqIndex;

    if (wrappedBlock != startWrapBlock)
    {
      return;
    }

    res = Math.min(res, av.getAlignment().getWidth() - 1);

    if (stretchGroup.getEndRes() == res)
    {
      // Edit end res position of selected group
      changeEndRes = true;
    }
    else if (stretchGroup.getStartRes() == res)
    {
      // Edit start res position of selected group
      changeStartRes = true;
    }

    if (res < av.getRanges().getStartRes())
    {
      res = av.getRanges().getStartRes();
    }

    if (changeEndRes)
    {
      if (res > (stretchGroup.getStartRes() - 1))
      {
        stretchGroup.setEndRes(res);
        updateOverviewAndStructs |= av.isSelectionDefinedGroup();
      }
    }
    else if (changeStartRes)
    {
      if (res < (stretchGroup.getEndRes() + 1))
      {
        stretchGroup.setStartRes(res);
        updateOverviewAndStructs |= av.isSelectionDefinedGroup();
      }
    }

    int dragDirection = 0;

    if (y > oldSeq)
    {
      dragDirection = 1;
    }
    else if (y < oldSeq)
    {
      dragDirection = -1;
    }

    while ((y != oldSeq) && (oldSeq > -1)
            && (y < av.getAlignment().getHeight()))
    {
      // This routine ensures we don't skip any sequences, as the
      // selection is quite slow.
      Sequence seq = (Sequence) av.getAlignment().getSequenceAt(oldSeq);

      oldSeq += dragDirection;

      if (oldSeq < 0)
      {
        break;
      }

      Sequence nextSeq = (Sequence) av.getAlignment().getSequenceAt(oldSeq);

      if (stretchGroup.getSequences(null).contains(nextSeq))
      {
        stretchGroup.deleteSequence(seq, false);
        updateOverviewAndStructs |= av.isSelectionDefinedGroup();
      }
      else
      {
        if (seq != null)
        {
          stretchGroup.addSequence(seq, false);
        }

        stretchGroup.addSequence(nextSeq, false);
        updateOverviewAndStructs |= av.isSelectionDefinedGroup();
      }
    }

    if (oldSeq < 0)
    {
      oldSeq = -1;
    }

    mouseDragging = true;

    if (scrollThread != null)
    {
      scrollThread.setMousePosition(evt.getPoint());
    }

    /*
     * construct a status message showing the range of the selection
     */
    StringBuilder status = new StringBuilder(64);
    List<SequenceI> seqs = stretchGroup.getSequences();
    String name = seqs.get(0).getName();
    if (name.length() > 20)
    {
      name = name.substring(0, 20);
    }
    status.append(name).append(" - ");
    name = seqs.get(seqs.size() - 1).getName();
    if (name.length() > 20)
    {
      name = name.substring(0, 20);
    }
    status.append(name).append(" ");
    int startRes = stretchGroup.getStartRes();
    status.append(" cols ").append(String.valueOf(startRes + 1))
            .append("-");
    int endRes = stretchGroup.getEndRes();
    status.append(String.valueOf(endRes + 1));
    status.append(" (").append(String.valueOf(seqs.size())).append(" x ")
            .append(String.valueOf(endRes - startRes + 1)).append(")");
    ap.alignFrame.setStatus(status.toString());
  }

  /**
   * Stops the scroll thread if it is running
   */
  void stopScrolling()
  {
    if (scrollThread != null)
    {
      scrollThread.stopScrolling();
      scrollThread = null;
    }
    mouseDragging = false;
  }

  /**
   * Starts a thread to scroll the alignment, towards a given mouse position
   * outside the panel bounds, unless the alignment is in wrapped mode
   * 
   * @param mousePos
   */
  void startScrolling(Point mousePos)
  {
    /*
     * set this.mouseDragging in case this was called from 
     * a drag in ScalePanel or AnnotationPanel
     */
    mouseDragging = true;
    if (!av.getWrapAlignment() && scrollThread == null)
    {
      scrollThread = new ScrollThread();
      scrollThread.setMousePosition(mousePos);
      if (Platform.isJS())
      {
        /*
         * Javascript - run every 20ms until scrolling stopped
         * or reaches the limit of scrollable alignment
         */
        Timer t = new Timer(20, new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            if (scrollThread != null)
            {
              // if (!scrollOnce() {t.stop();}) gives compiler error :-(
              scrollThread.scrollOnce();
            }
          }
        });
        t.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            if (scrollThread == null)
            {
              // SeqPanel.stopScrolling called
              t.stop();
            }
          }
        });
        t.start();
      }
      else
      {
        /*
         * Java - run in a new thread
         */
        scrollThread.start();
      }
    }
  }

  /**
   * Performs scrolling of the visible alignment left, right, up or down, until
   * scrolling is stopped by calling stopScrolling, mouse drag is ended, or the
   * limit of the alignment is reached
   */
  class ScrollThread extends Thread
  {
    private Point mousePos;

    private volatile boolean keepRunning = true;

    /**
     * Constructor
     */
    public ScrollThread()
    {
      setName("SeqPanel$ScrollThread");
    }

    /**
     * Sets the position of the mouse that determines the direction of the
     * scroll to perform. If this is called as the mouse moves, scrolling should
     * respond accordingly. For example, if the mouse is dragged right, scroll
     * right should start; if the drag continues down, scroll down should also
     * happen.
     * 
     * @param p
     */
    public void setMousePosition(Point p)
    {
      mousePos = p;
    }

    /**
     * Sets a flag that will cause the thread to exit
     */
    public void stopScrolling()
    {
      keepRunning = false;
    }

    /**
     * Scrolls the alignment left or right, and/or up or down, depending on the
     * last notified mouse position, until the limit of the alignment is
     * reached, or a flag is set to stop the scroll
     */
    @Override
    public void run()
    {
      while (keepRunning)
      {
        if (mousePos != null)
        {
          keepRunning = scrollOnce();
        }
        try
        {
          Thread.sleep(20);
        } catch (Exception ex)
        {
        }
      }
      SeqPanel.this.scrollThread = null;
    }

    /**
     * Scrolls
     * <ul>
     * <li>one row up, if the mouse is above the panel</li>
     * <li>one row down, if the mouse is below the panel</li>
     * <li>one column left, if the mouse is left of the panel</li>
     * <li>one column right, if the mouse is right of the panel</li>
     * </ul>
     * Answers true if a scroll was performed, false if not - meaning either
     * that the mouse position is within the panel, or the edge of the alignment
     * has been reached.
     */
    boolean scrollOnce()
    {
      /*
       * quit after mouseUp ensures interrupt in JalviewJS
       */
      if (!mouseDragging)
      {
        return false;
      }

      boolean scrolled = false;
      ViewportRanges ranges = SeqPanel.this.av.getRanges();

      /*
       * scroll up or down
       */
      if (mousePos.y < 0)
      {
        // mouse is above this panel - try scroll up
        scrolled = ranges.scrollUp(true);
      }
      else if (mousePos.y >= getHeight())
      {
        // mouse is below this panel - try scroll down
        scrolled = ranges.scrollUp(false);
      }

      /*
       * scroll left or right
       */
      if (mousePos.x < 0)
      {
        scrolled |= ranges.scrollRight(false);
      }
      else if (mousePos.x >= getWidth())
      {
        scrolled |= ranges.scrollRight(true);
      }
      return scrolled;
    }
  }

  /**
   * modify current selection according to a received message.
   */
  @Override
  public void selection(SequenceGroup seqsel, ColumnSelection colsel,
          HiddenColumns hidden, SelectionSource source)
  {
    // TODO: fix this hack - source of messages is align viewport, but SeqPanel
    // handles selection messages...
    // TODO: extend config options to allow user to control if selections may be
    // shared between viewports.
    boolean iSentTheSelection = (av == source
            || (source instanceof AlignViewport
                    && ((AlignmentViewport) source).getSequenceSetId()
                            .equals(av.getSequenceSetId())));

    if (iSentTheSelection)
    {
      // respond to our own event by updating dependent dialogs
      if (ap.getCalculationDialog() != null)
      {
        ap.getCalculationDialog().validateCalcTypes();
      }

      return;
    }

    // process further ?
    if (!av.followSelection)
    {
      return;
    }

    /*
     * Ignore the selection if there is one of our own pending.
     */
    if (av.isSelectionGroupChanged(false) || av.isColSelChanged(false))
    {
      return;
    }

    /*
     * Check for selection in a view of which this one is a dna/protein
     * complement.
     */
    if (selectionFromTranslation(seqsel, colsel, hidden, source))
    {
      return;
    }

    // do we want to thread this ? (contention with seqsel and colsel locks, I
    // suspect)
    /*
     * only copy colsel if there is a real intersection between
     * sequence selection and this panel's alignment
     */
    boolean repaint = false;
    boolean copycolsel = false;

    SequenceGroup sgroup = null;
    if (seqsel != null && seqsel.getSize() > 0)
    {
      if (av.getAlignment() == null)
      {
        Console.warn("alignviewport av SeqSetId=" + av.getSequenceSetId()
                + " ViewId=" + av.getViewId()
                + " 's alignment is NULL! returning immediately.");
        return;
      }
      sgroup = seqsel.intersect(av.getAlignment(),
              (av.hasHiddenRows()) ? av.getHiddenRepSequences() : null);
      if ((sgroup != null && sgroup.getSize() > 0))
      {
        copycolsel = true;
      }
    }
    if (sgroup != null && sgroup.getSize() > 0)
    {
      av.setSelectionGroup(sgroup);
    }
    else
    {
      av.setSelectionGroup(null);
    }
    av.isSelectionGroupChanged(true);
    repaint = true;

    if (copycolsel)
    {
      // the current selection is unset or from a previous message
      // so import the new colsel.
      if (colsel == null || colsel.isEmpty())
      {
        if (av.getColumnSelection() != null)
        {
          av.getColumnSelection().clear();
          repaint = true;
        }
      }
      else
      {
        // TODO: shift colSel according to the intersecting sequences
        if (av.getColumnSelection() == null)
        {
          av.setColumnSelection(new ColumnSelection(colsel));
        }
        else
        {
          av.getColumnSelection().setElementsFrom(colsel,
                  av.getAlignment().getHiddenColumns());
        }
      }
      av.isColSelChanged(true);
      repaint = true;
    }

    if (copycolsel && av.hasHiddenColumns()
            && (av.getAlignment().getHiddenColumns() == null))
    {
      System.err.println("Bad things");
    }
    if (repaint) // always true!
    {
      // probably finessing with multiple redraws here
      PaintRefresher.Refresh(this, av.getSequenceSetId());
      // ap.paintAlignment(false);
    }

    // lastly, update dependent dialogs
    if (ap.getCalculationDialog() != null)
    {
      ap.getCalculationDialog().validateCalcTypes();
    }

  }

  /**
   * If this panel is a cdna/protein translation view of the selection source,
   * tries to map the source selection to a local one, and returns true. Else
   * returns false.
   * 
   * @param seqsel
   * @param colsel
   * @param source
   */
  protected boolean selectionFromTranslation(SequenceGroup seqsel,
          ColumnSelection colsel, HiddenColumns hidden,
          SelectionSource source)
  {
    if (!(source instanceof AlignViewportI))
    {
      return false;
    }
    final AlignViewportI sourceAv = (AlignViewportI) source;
    if (sourceAv.getCodingComplement() != av
            && av.getCodingComplement() != sourceAv)
    {
      return false;
    }

    /*
     * Map sequence selection
     */
    SequenceGroup sg = MappingUtils.mapSequenceGroup(seqsel, sourceAv, av);
    av.setSelectionGroup(sg != null && sg.getSize() > 0 ? sg : null);
    av.isSelectionGroupChanged(true);

    /*
     * Map column selection
     */
    // ColumnSelection cs = MappingUtils.mapColumnSelection(colsel, sourceAv,
    // av);
    ColumnSelection cs = new ColumnSelection();
    HiddenColumns hs = new HiddenColumns();
    MappingUtils.mapColumnSelection(colsel, hidden, sourceAv, av, cs, hs);
    av.setColumnSelection(cs);
    boolean hiddenChanged = av.getAlignment().setHiddenColumns(hs);

    // lastly, update any dependent dialogs
    if (ap.getCalculationDialog() != null)
    {
      ap.getCalculationDialog().validateCalcTypes();
    }

    /*
     * repaint alignment, and also Overview or Structure
     * if hidden column selection has changed
     */
    ap.paintAlignment(hiddenChanged, hiddenChanged);
    // propagate any selection changes
    PaintRefresher.Refresh(ap, av.getSequenceSetId());

    return true;
  }

  /**
   * 
   * @return null or last search results handled by this panel
   */
  public SearchResultsI getLastSearchResults()
  {
    return lastSearchResults;
  }
}
