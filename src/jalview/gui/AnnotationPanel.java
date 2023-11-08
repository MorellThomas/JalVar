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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Scrollable;
import javax.swing.ToolTipManager;

import jalview.api.AlignViewportI;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.ContactListI;
import jalview.datamodel.ContactMatrixI;
import jalview.datamodel.ContactRange;
import jalview.datamodel.GraphLine;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.SequenceI;
import jalview.gui.JalviewColourChooser.ColourChooserListener;
import jalview.renderer.AnnotationRenderer;
import jalview.renderer.AwtRenderPanelI;
import jalview.renderer.ContactGeometry;
import jalview.schemes.ResidueProperties;
import jalview.util.Comparison;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.viewmodel.ViewportListenerI;
import jalview.viewmodel.ViewportRanges;
import jalview.ws.datamodel.MappableContactMatrixI;
import jalview.ws.datamodel.alphafold.PAEContactMatrix;

/**
 * AnnotationPanel displays visible portion of annotation rows below unwrapped
 * alignment
 * 
 * @author $author$
 * @version $Revision$
 */
public class AnnotationPanel extends JPanel implements AwtRenderPanelI,
        MouseListener, MouseWheelListener, MouseMotionListener,
        ActionListener, AdjustmentListener, Scrollable, ViewportListenerI
{
  enum DragMode
  {
    Select, Resize, Undefined, MatrixSelect
  };

  String HELIX = MessageManager.getString("label.helix");

  String SHEET = MessageManager.getString("label.sheet");

  /**
   * For RNA secondary structure "stems" aka helices
   */
  String STEM = MessageManager.getString("label.rna_helix");

  String LABEL = MessageManager.getString("label.label");

  String REMOVE = MessageManager.getString("label.remove_annotation");

  String COLOUR = MessageManager.getString("action.colour");

  public final Color HELIX_COLOUR = Color.red.darker();

  public final Color SHEET_COLOUR = Color.green.darker().darker();

  public final Color STEM_COLOUR = Color.blue.darker();

  /** DOCUMENT ME!! */
  public AlignViewport av;

  AlignmentPanel ap;

  public int activeRow = -1;

  public BufferedImage image;

  public volatile BufferedImage fadedImage;

  // private Graphics2D gg;

  public FontMetrics fm;

  public int imgWidth = 0;

  boolean fastPaint = false;

  // Used For mouse Dragging and resizing graphs
  int graphStretch = -1;

  int mouseDragLastX = -1;

  int mouseDragLastY = -1;

  int firstDragX = -1;

  int firstDragY = -1;

  DragMode dragMode = DragMode.Undefined;

  boolean mouseDragging = false;

  // for editing cursor
  int cursorX = 0;

  int cursorY = 0;

  public final AnnotationRenderer renderer;

  private MouseWheelListener[] _mwl;

  private boolean notJustOne;

  /**
   * Creates a new AnnotationPanel object.
   * 
   * @param ap
   *          DOCUMENT ME!
   */
  public AnnotationPanel(AlignmentPanel ap)
  {
    ToolTipManager.sharedInstance().registerComponent(this);
    ToolTipManager.sharedInstance().setInitialDelay(0);
    ToolTipManager.sharedInstance().setDismissDelay(10000);
    this.ap = ap;
    av = ap.av;
    this.setLayout(null);
    addMouseListener(this);
    addMouseMotionListener(this);
    ap.annotationScroller.getVerticalScrollBar()
            .addAdjustmentListener(this);
    // save any wheel listeners on the scroller, so we can propagate scroll
    // events to them.
    _mwl = ap.annotationScroller.getMouseWheelListeners();
    // and then set our own listener to consume all mousewheel events
    ap.annotationScroller.addMouseWheelListener(this);
    renderer = new AnnotationRenderer();

    av.getRanges().addPropertyChangeListener(this);
  }

  public AnnotationPanel(AlignViewport av)
  {
    this.av = av;
    renderer = new AnnotationRenderer();
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e)
  {
    if (e.isShiftDown())
    {
      e.consume();
      double wheelRotation = e.getPreciseWheelRotation();
      if (wheelRotation > 0)
      {
        av.getRanges().scrollRight(true);
      }
      else if (wheelRotation < 0)
      {
        av.getRanges().scrollRight(false);
      }
    }
    else
    {
      // TODO: find the correct way to let the event bubble up to
      // ap.annotationScroller
      for (MouseWheelListener mwl : _mwl)
      {
        if (mwl != null)
        {
          mwl.mouseWheelMoved(e);
        }
        if (e.isConsumed())
        {
          break;
        }
      }
    }
  }

  @Override
  public Dimension getPreferredScrollableViewportSize()
  {
    Dimension ps = getPreferredSize();
    return new Dimension(ps.width, adjustForAlignFrame(false, ps.height));
  }

  @Override
  public int getScrollableBlockIncrement(Rectangle visibleRect,
          int orientation, int direction)
  {
    return 30;
  }

  @Override
  public boolean getScrollableTracksViewportHeight()
  {
    return false;
  }

  @Override
  public boolean getScrollableTracksViewportWidth()
  {
    return true;
  }

  @Override
  public int getScrollableUnitIncrement(Rectangle visibleRect,
          int orientation, int direction)
  {
    return 30;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * java.awt.event.AdjustmentListener#adjustmentValueChanged(java.awt.event
   * .AdjustmentEvent)
   */
  @Override
  public void adjustmentValueChanged(AdjustmentEvent evt)
  {
    // update annotation label display
    ap.getAlabels().setScrollOffset(-evt.getValue());
  }

  /**
   * Calculates the height of the annotation displayed in the annotation panel.
   * Callers should normally call the ap.adjustAnnotationHeight method to ensure
   * all annotation associated components are updated correctly.
   * 
   */
  public int adjustPanelHeight()
  {
    int height = av.calcPanelHeight();
    this.setPreferredSize(new Dimension(1, height));
    if (ap != null)
    {
      // revalidate only when the alignment panel is fully constructed
      ap.validate();
    }

    return height;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  @Override
  public void actionPerformed(ActionEvent evt)
  {
    AlignmentAnnotation[] aa = av.getAlignment().getAlignmentAnnotation();
    if (aa == null)
    {
      return;
    }
    Annotation[] anot = aa[activeRow].annotations;

    if (anot.length < av.getColumnSelection().getMax())
    {
      Annotation[] temp = new Annotation[av.getColumnSelection().getMax()
              + 2];
      System.arraycopy(anot, 0, temp, 0, anot.length);
      anot = temp;
      aa[activeRow].annotations = anot;
    }

    String action = evt.getActionCommand();
    if (action.equals(REMOVE))
    {
      for (int index : av.getColumnSelection().getSelected())
      {
        if (av.getAlignment().getHiddenColumns().isVisible(index))
        {
          anot[index] = null;
        }
      }
    }
    else if (action.equals(LABEL))
    {
      String exMesg = collectAnnotVals(anot, LABEL);
      String label = JvOptionPane.showInputDialog(
              MessageManager.getString("label.enter_label"), exMesg);

      if (label == null)
      {
        return;
      }

      if ((label.length() > 0) && !aa[activeRow].hasText)
      {
        aa[activeRow].hasText = true;
      }

      for (int index : av.getColumnSelection().getSelected())
      {
        if (!av.getAlignment().getHiddenColumns().isVisible(index))
        {
          continue;
        }

        if (anot[index] == null)
        {
          anot[index] = new Annotation(label, "", ' ', 0);
        }
        else
        {
          anot[index].displayCharacter = label;
        }
      }
    }
    else if (action.equals(COLOUR))
    {
      final Annotation[] fAnot = anot;
      String title = MessageManager
              .getString("label.select_foreground_colour");
      ColourChooserListener listener = new ColourChooserListener()
      {
        @Override
        public void colourSelected(Color c)
        {
          HiddenColumns hiddenColumns = av.getAlignment()
                  .getHiddenColumns();
          for (int index : av.getColumnSelection().getSelected())
          {
            if (hiddenColumns.isVisible(index))
            {
              if (fAnot[index] == null)
              {
                fAnot[index] = new Annotation("", "", ' ', 0);
              }
              fAnot[index].colour = c;
            }
          }
        };
      };
      JalviewColourChooser.showColourChooser(this, title, Color.black,
              listener);
    }
    else
    // HELIX, SHEET or STEM
    {
      char type = 0;
      String symbol = "\u03B1"; // alpha

      if (action.equals(HELIX))
      {
        type = 'H';
      }
      else if (action.equals(SHEET))
      {
        type = 'E';
        symbol = "\u03B2"; // beta
      }

      // Added by LML to color stems
      else if (action.equals(STEM))
      {
        type = 'S';
        int column = av.getColumnSelection().getSelectedRanges().get(0)[0];
        symbol = aa[activeRow].getDefaultRnaHelixSymbol(column);
      }

      if (!aa[activeRow].hasIcons)
      {
        aa[activeRow].hasIcons = true;
      }

      String label = JvOptionPane.showInputDialog(MessageManager
              .getString("label.enter_label_for_the_structure"), symbol);

      if (label == null)
      {
        return;
      }

      if ((label.length() > 0) && !aa[activeRow].hasText)
      {
        aa[activeRow].hasText = true;
        if (action.equals(STEM))
        {
          aa[activeRow].showAllColLabels = true;
        }
      }
      for (int index : av.getColumnSelection().getSelected())
      {
        if (!av.getAlignment().getHiddenColumns().isVisible(index))
        {
          continue;
        }

        if (anot[index] == null)
        {
          anot[index] = new Annotation(label, "", type, 0);
        }

        anot[index].secondaryStructure = type != 'S' ? type
                : label.length() == 0 ? ' ' : label.charAt(0);
        anot[index].displayCharacter = label;

      }
    }

    av.getAlignment().validateAnnotation(aa[activeRow]);
    ap.alignmentChanged();
    ap.alignFrame.setMenusForViewport();
    adjustPanelHeight();
    repaint();

    return;
  }

  /**
   * Returns any existing annotation concatenated as a string. For each
   * annotation, takes the description, if any, else the secondary structure
   * character (if type is HELIX, SHEET or STEM), else the display character (if
   * type is LABEL).
   * 
   * @param anots
   * @param type
   * @return
   */
  private String collectAnnotVals(Annotation[] anots, String type)
  {
    // TODO is this method wanted? why? 'last' is not used

    StringBuilder collatedInput = new StringBuilder(64);
    String last = "";
    ColumnSelection viscols = av.getColumnSelection();
    HiddenColumns hidden = av.getAlignment().getHiddenColumns();

    /*
     * the selection list (read-only view) is in selection order, not
     * column order; make a copy so we can sort it
     */
    List<Integer> selected = new ArrayList<>(viscols.getSelected());
    Collections.sort(selected);
    for (int index : selected)
    {
      // always check for current display state - just in case
      if (!hidden.isVisible(index))
      {
        continue;
      }
      String tlabel = null;
      if (anots[index] != null)
      { // LML added stem code
        if (type.equals(HELIX) || type.equals(SHEET) || type.equals(STEM)
                || type.equals(LABEL))
        {
          tlabel = anots[index].description;
          if (tlabel == null || tlabel.length() < 1)
          {
            if (type.equals(HELIX) || type.equals(SHEET)
                    || type.equals(STEM))
            {
              tlabel = "" + anots[index].secondaryStructure;
            }
            else
            {
              tlabel = "" + anots[index].displayCharacter;
            }
          }
        }
        if (tlabel != null && !tlabel.equals(last))
        {
          if (last.length() > 0)
          {
            collatedInput.append(" ");
          }
          collatedInput.append(tlabel);
        }
      }
    }
    return collatedInput.toString();
  }

  /**
   * Action on right mouse pressed on Mac is to show a pop-up menu for the
   * annotation. Action on left mouse pressed is to find which annotation is
   * pressed and mark the start of a column selection or graph resize operation.
   * 
   * @param evt
   */
  @Override
  public void mousePressed(MouseEvent evt)
  {

    AlignmentAnnotation[] aa = av.getAlignment().getAlignmentAnnotation();
    if (aa == null)
    {
      return;
    }
    mouseDragLastX = evt.getX();
    mouseDragLastY = evt.getY();

    /*
     * add visible annotation heights until we reach the y
     * position, to find which annotation it is in
     */
    int height = 0;
    activeRow = -1;
    int yOffset = 0;
    // todo could reuse getRowIndexAndOffset ?
    final int y = evt.getY();

    for (int i = 0; i < aa.length; i++)
    {
      if (aa[i].visible)
      {
        height += aa[i].height;
      }

      if (y < height)
      {
        if (aa[i].editable)
        {
          activeRow = i;
        }
        else if (aa[i].graph != 0)
        {
          /*
           * we have clicked on a resizable graph annotation
           */
          graphStretch = i;
          yOffset = height - y;
        }
        break;
      }
    }

    /*
     * isPopupTrigger fires in mousePressed on Mac,
     * not until mouseRelease on Windows
     */
    if (evt.isPopupTrigger() && activeRow != -1)
    {
      showPopupMenu(y, evt.getX());
      return;
    }

    if (graphStretch != -1)
    {

      if (aa[graphStretch].graph == AlignmentAnnotation.CONTACT_MAP)
      {
        // data in row has position on y as well as x axis
        if (evt.isAltDown() || evt.isAltGraphDown())
        {
          dragMode = DragMode.MatrixSelect;
          firstDragX = mouseDragLastX;
          firstDragY = mouseDragLastY;
        }
      }
    }
    else
    {
      // no row (or row that can be adjusted) was pressed. Simulate a ruler
      // click
      ap.getScalePanel().mousePressed(evt);
    }
  }

  /**
   * checks whether the annotation row under the mouse click evt's handles the
   * event
   * 
   * @param evt
   * @return false if evt was not handled
   */
  boolean matrix_clicked(MouseEvent evt)
  {
    int[] rowIndex = getRowIndexAndOffset(evt.getY(),
            av.getAlignment().getAlignmentAnnotation());
    if (rowIndex == null)
    {
      jalview.bin.Console
              .error("IMPLEMENTATION ERROR: matrix click out of range.");
      return false;
    }
    int yOffset = rowIndex[1];

    AlignmentAnnotation clicked = av.getAlignment()
            .getAlignmentAnnotation()[rowIndex[0]];
    if (clicked.graph != AlignmentAnnotation.CONTACT_MAP)
    {
      return false;
    }

    // TODO - use existing threshold to select related sections of matrix
    GraphLine thr = clicked.getThreshold();

    int currentX = getColumnForXPos(evt.getX());
    ContactListI forCurrentX = av.getContactList(clicked, currentX);
    if (forCurrentX != null)
    {
      ContactGeometry cXcgeom = new ContactGeometry(forCurrentX,
              clicked.graphHeight);
      ContactGeometry.contactInterval cXci = cXcgeom.mapFor(yOffset,
              yOffset);
      /**
       * start and end range corresponding to the row range under the mouse at
       * column currentX
       */
      int fr, to;
      fr = Math.min(cXci.cStart, cXci.cEnd);
      to = Math.max(cXci.cStart, cXci.cEnd);

      // double click selects the whole group
      if (evt.getClickCount() == 2)
      {
        ContactMatrixI matrix = av.getContactMatrix(clicked);

        if (matrix != null)
        {
          // simplest approach is to select all group containing column
          if (matrix.hasGroups())
          {
            SequenceI rseq = clicked.sequenceRef;
            BitSet grp = new BitSet();
            grp.or(matrix.getGroupsFor(currentX));
            // TODO: cXci needs to be mapped to real groups
            for (int c = fr; c <= to; c++)
            {
              BitSet additionalGrp = matrix.getGroupsFor(c);
              grp.or(additionalGrp);
            }

            HiddenColumns hc = av.getAlignment().getHiddenColumns();
            ColumnSelection cs = av.getColumnSelection();
            
            for (int p=grp.nextSetBit(0); p >= 0; p = grp
                    .nextSetBit(p + 1))
            {
              if (matrix instanceof MappableContactMatrixI)
              {
                // find the end of this run of set bits
                int nextp = grp.nextClearBit(p)-1;
                int[] pos = ((MappableContactMatrixI)matrix).getMappedPositionsFor(rseq, p,nextp);
                p=nextp;
                
                if (pos!=null)
                {
                  for (int pos_p = pos[0];pos_p<=pos[1];pos_p++)
                  {
                    int col = rseq.findIndex(pos_p)-1;
                    if (col>=0 && (!av.hasHiddenColumns() || hc.isVisible(col)))
                    {
                      cs.addElement(col);
                    }
                  }
                }
              } else {
                int offp = (rseq != null)
                        ? rseq.findIndex(rseq.getStart() - 1 + p)
                        : p;
  
                if (!av.hasHiddenColumns() || hc.isVisible(offp))
                {
                  cs.addElement(offp);
                }
              }
            }
          }
          // possible alternative for interactive selection - threshold
          // gives 'ceiling' for forming a cluster
          // when a row+column is selected, farthest common ancestor less
          // than thr is used to compute cluster

        }
      }
      else
      {
        // select corresponding range in segment under mouse
        {
          int[] rng = forCurrentX.getMappedPositionsFor(fr, to);
          if (rng != null)
          {
            av.getColumnSelection().addRangeOfElements(rng, true);
          }
          av.getColumnSelection().addElement(currentX);
        }
        // PAE SPECIFIC
        // and also select everything lower than the max range adjacent
        // (kind of works)
        if (evt.isControlDown()
                && PAEContactMatrix.PAEMATRIX.equals(clicked.getCalcId()))
        {
          int c = fr;
          ContactRange cr = forCurrentX.getRangeFor(fr, to);
          double cval;
          // TODO: could use GraphLine instead of arbitrary picking
          // TODO: could report mean/median/variance for partitions
          // (contiguous selected vs unselected regions and inter-contig
          // regions)
          // controls feathering - what other elements in row/column
          // should we select
          double thresh = cr.getMean() + (cr.getMax() - cr.getMean()) * .15;
          while (c >= 0)
          {
            cval = forCurrentX.getContactAt(c);
            if (// cr.getMin() <= cval &&
            cval <= thresh)
            {
              int[] cols = forCurrentX.getMappedPositionsFor(c, c);
              if (cols != null)
              {
                av.getColumnSelection().addRangeOfElements(cols, true);
              }
              else
              {
                break;
              }
            }
            c--;
          }
          c = to;
          while (c < forCurrentX.getContactHeight())
          {
            cval = forCurrentX.getContactAt(c);
            if (// cr.getMin() <= cval &&
            cval <= thresh)
            {
              int[] cols = forCurrentX.getMappedPositionsFor(c, c);
              if (cols != null)
              {
                av.getColumnSelection().addRangeOfElements(cols, true);
              }
            }
            else
            {
              break;
            }
            c++;

          }
        }
      }
    }
    ap.paintAlignment(false, false);
    PaintRefresher.Refresh(ap, av.getSequenceSetId());
    av.sendSelection();
    return true;
  }

  /**
   * Construct and display a context menu at the right-click position
   * 
   * @param y
   * @param x
   */
  void showPopupMenu(final int y, int x)
  {
    if (av.getColumnSelection() == null
            || av.getColumnSelection().isEmpty())
    {
      return;
    }

    JPopupMenu pop = new JPopupMenu(
            MessageManager.getString("label.structure_type"));
    JMenuItem item;
    /*
     * Just display the needed structure options
     */
    if (av.getAlignment().isNucleotide())
    {
      item = new JMenuItem(STEM);
      item.addActionListener(this);
      pop.add(item);
    }
    else
    {
      item = new JMenuItem(HELIX);
      item.addActionListener(this);
      pop.add(item);
      item = new JMenuItem(SHEET);
      item.addActionListener(this);
      pop.add(item);
    }
    item = new JMenuItem(LABEL);
    item.addActionListener(this);
    pop.add(item);
    item = new JMenuItem(COLOUR);
    item.addActionListener(this);
    pop.add(item);
    item = new JMenuItem(REMOVE);
    item.addActionListener(this);
    pop.add(item);
    pop.show(this, x, y);
  }

  /**
   * Action on mouse up is to clear mouse drag data and call mouseReleased on
   * ScalePanel, to deal with defining the selection group (if any) defined by
   * the mouse drag
   * 
   * @param evt
   */
  @Override
  public void mouseReleased(MouseEvent evt)
  {
    if (dragMode == DragMode.MatrixSelect)
    {
      matrixSelectRange(evt);
    }
    graphStretch = -1;
    mouseDragLastX = -1;
    mouseDragLastY = -1;
    firstDragX = -1;
    firstDragY = -1;
    mouseDragging = false;
    if (dragMode == DragMode.Resize)
    {
      ap.adjustAnnotationHeight();
    }
    dragMode = DragMode.Undefined;
    if (!matrix_clicked(evt))
    {
      ap.getScalePanel().mouseReleased(evt);
    }

    /*
     * isPopupTrigger is set in mouseReleased on Windows
     * (in mousePressed on Mac)
     */
    if (evt.isPopupTrigger() && activeRow != -1)
    {
      showPopupMenu(evt.getY(), evt.getX());
    }

  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  @Override
  public void mouseEntered(MouseEvent evt)
  {
    this.mouseDragging = false;
    ap.getScalePanel().mouseEntered(evt);
  }

  /**
   * On leaving the panel, calls ScalePanel.mouseExited to deal with scrolling
   * with column selection on a mouse drag
   * 
   * @param evt
   */
  @Override
  public void mouseExited(MouseEvent evt)
  {
    ap.getScalePanel().mouseExited(evt);
  }

  /**
   * Action on starting or continuing a mouse drag. There are two possible
   * actions:
   * <ul>
   * <li>drag up or down on a graphed annotation increases or decreases the
   * height of the graph</li>
   * <li>dragging left or right selects the columns dragged across</li>
   * </ul>
   * A drag on a graph annotation is treated as column selection if it starts
   * with more horizontal than vertical movement, and as resize if it starts
   * with more vertical than horizontal movement. Once started, the drag does
   * not change mode.
   * 
   * @param evt
   */
  @Override
  public void mouseDragged(MouseEvent evt)
  {
    /*
     * if dragMode is Undefined:
     * - set to Select if dx > dy
     * - set to Resize if dy > dx
     * - do nothing if dx == dy
     */
    final int x = evt.getX();
    final int y = evt.getY();
    if (dragMode == DragMode.Undefined)
    {
      int dx = Math.abs(x - mouseDragLastX);
      int dy = Math.abs(y - mouseDragLastY);
      if (graphStretch == -1 || dx > dy)
      {
        /*
         * mostly horizontal drag, or not a graph annotation
         */
        dragMode = DragMode.Select;
      }
      else if (dy > dx)
      {
        /*
         * mostly vertical drag
         */
        dragMode = DragMode.Resize;
        notJustOne = evt.isShiftDown();

        /*
         * but could also be a matrix drag
         */
        if ((evt.isAltDown() || evt.isAltGraphDown()) && (av.getAlignment()
                .getAlignmentAnnotation()[graphStretch].graph == AlignmentAnnotation.CONTACT_MAP))
        {
          /*
           * dragging in a matrix
           */
          dragMode = DragMode.MatrixSelect;
          firstDragX = mouseDragLastX;
          firstDragY = mouseDragLastY;
        }
      }
    }

    if (dragMode == DragMode.Undefined)

    {
      /*
       * drag is diagonal - defer deciding whether to
       * treat as up/down or left/right
       */
      return;
    }

    try
    {
      if (dragMode == DragMode.Resize)
      {
        /*
         * resize graph annotation if mouse was dragged up or down
         */
        int deltaY = mouseDragLastY - evt.getY();
        if (deltaY != 0)
        {
          AlignmentAnnotation graphAnnotation = av.getAlignment()
                  .getAlignmentAnnotation()[graphStretch];
          int newHeight = Math.max(0, graphAnnotation.graphHeight + deltaY);
          if (notJustOne)
          {
            for (AlignmentAnnotation similar : av.getAlignment()
                    .findAnnotations(null, graphAnnotation.getCalcId(),
                            graphAnnotation.label))
            {
              similar.graphHeight = newHeight;
            }

          }
          else
          {
            graphAnnotation.graphHeight = newHeight;
          }
          adjustPanelHeight();
          ap.paintAlignment(false, false);
        }
      }
      else if (dragMode == DragMode.MatrixSelect)
      {
        /*
         * TODO draw a rubber band for range
         */
        mouseDragLastX = x;
        mouseDragLastY = y;
        ap.paintAlignment(false, false);
      }
      else
      {
        /*
         * for mouse drag left or right, delegate to 
         * ScalePanel to adjust the column selection
         */
        ap.getScalePanel().mouseDragged(evt);
      }
    } finally
    {
      mouseDragLastX = x;
      mouseDragLastY = y;
    }
  }

  public void matrixSelectRange(MouseEvent evt)
  {
    /*
     * get geometry of drag
     */
    int fromY = Math.min(firstDragY, evt.getY());
    int toY = Math.max(firstDragY, evt.getY());
    int fromX = Math.min(firstDragX, evt.getX());
    int toX = Math.max(firstDragX, evt.getX());

    int deltaY = toY - fromY;
    int deltaX = toX - fromX;

    int[] rowIndex = getRowIndexAndOffset(fromY,
            av.getAlignment().getAlignmentAnnotation());
    int[] toRowIndex = getRowIndexAndOffset(toY,
            av.getAlignment().getAlignmentAnnotation());

    if (rowIndex == null || toRowIndex == null)
    {
      jalview.bin.Console.trace("Drag out of range. needs to be clipped");

    }
    if (rowIndex[0] != toRowIndex[0])
    {
      jalview.bin.Console
              .trace("Drag went to another row. needs to be clipped");
    }

    // rectangular selection on matrix style annotation
    AlignmentAnnotation cma = av.getAlignment()
            .getAlignmentAnnotation()[rowIndex[0]];

    int lastX = getColumnForXPos(fromX);
    int currentX = getColumnForXPos(toX);
    int fromXc = Math.min(lastX, currentX);
    int toXc = Math.max(lastX, currentX);
    ContactListI forFromX = av.getContactList(cma, fromXc);
    ContactListI forToX = av.getContactList(cma, toXc);

    if (forFromX != null && forToX != null)
    {
      ContactGeometry lastXcgeom = new ContactGeometry(forFromX,
              cma.graphHeight);
      ContactGeometry.contactInterval lastXci = lastXcgeom
              .mapFor(rowIndex[1], rowIndex[1] + deltaY);

      ContactGeometry cXcgeom = new ContactGeometry(forToX,
              cma.graphHeight);
      ContactGeometry.contactInterval cXci = cXcgeom.mapFor(rowIndex[1],
              rowIndex[1] + deltaY);

      // mark rectangular region formed by drag
      jalview.bin.Console.trace("Matrix Selection from last(" + fromXc
              + ",[" + lastXci.cStart + "," + lastXci.cEnd + "]) to cur("
              + toXc + ",[" + cXci.cStart + "," + cXci.cEnd + "])");
      int fr, to;
      fr = Math.min(lastXci.cStart, lastXci.cEnd);
      to = Math.max(lastXci.cStart, lastXci.cEnd);
      int[] mappedPos = forFromX.getMappedPositionsFor(fr, to);
      if (mappedPos != null)
      {
        jalview.bin.Console.trace("Marking " + fr + " to " + to
                + " mapping to sequence positions " + mappedPos[0] + " to "
                + mappedPos[1]);
        for (int pair = 0; pair < mappedPos.length; pair += 2)
        {
          for (int c = mappedPos[pair]; c <= mappedPos[pair + 1]; c++)
          // {
          // if (cma.sequenceRef != null)
          // {
          // int col = cma.sequenceRef.findIndex(cma.sequenceRef.getStart()+c);
          // av.getColumnSelection().addElement(col);
          // }
          // else
          {
            av.getColumnSelection().addElement(c);
          }
        }
      }
      // and again for most recent corner of drag
      fr = Math.min(cXci.cStart, cXci.cEnd);
      to = Math.max(cXci.cStart, cXci.cEnd);
      mappedPos = forFromX.getMappedPositionsFor(fr, to);
      if (mappedPos != null)
      {
        for (int pair = 0; pair < mappedPos.length; pair += 2)
        {
          jalview.bin.Console.trace("Marking " + fr + " to " + to
                  + " mapping to sequence positions " + mappedPos[pair]
                  + " to " + mappedPos[pair + 1]);
          for (int c = mappedPos[pair]; c <= mappedPos[pair + 1]; c++)
          {
            // if (cma.sequenceRef != null)
            // {
            // int col =
            // cma.sequenceRef.findIndex(cma.sequenceRef.getStart()+c);
            // av.getColumnSelection().addElement(col);
            // }
            // else
            {
              av.getColumnSelection().addElement(c);
            }
          }
        }
      }
      fr = Math.min(lastX, currentX);
      to = Math.max(lastX, currentX);

      jalview.bin.Console.trace("Marking " + fr + " to " + to);
      for (int c = fr; c <= to; c++)
      {
        av.getColumnSelection().addElement(c);
      }
    }

  }

  /**
   * Constructs the tooltip, and constructs and displays a status message, for
   * the current mouse position
   * 
   * @param evt
   */
  @Override
  public void mouseMoved(MouseEvent evt)
  {
    int yPos = evt.getY();
    AlignmentAnnotation[] aa = av.getAlignment().getAlignmentAnnotation();
    int rowAndOffset[] = getRowIndexAndOffset(yPos, aa);
    int row = rowAndOffset[0];

    if (row == -1)
    {
      this.setToolTipText(null);
      return;
    }

    int column = getColumnForXPos(evt.getX());

    AlignmentAnnotation ann = aa[row];
    if (row > -1 && ann.annotations != null
            && column < ann.annotations.length)
    {
      String toolTip = buildToolTip(ann, column, aa, rowAndOffset[1], av,
              ap);
      setToolTipText(toolTip == null ? null
              : JvSwingUtils.wrapTooltip(true, toolTip));
      String msg = getStatusMessage(av.getAlignment(), column, ann,
              rowAndOffset[1], av);
      ap.alignFrame.setStatus(msg);
    }
    else
    {
      this.setToolTipText(null);
      ap.alignFrame.setStatus(" ");
    }
  }

  private int getColumnForXPos(int x)
  {
    int column = (x / av.getCharWidth()) + av.getRanges().getStartRes();
    column = Math.min(column, av.getRanges().getEndRes());

    if (av.hasHiddenColumns())
    {
      column = av.getAlignment().getHiddenColumns()
              .visibleToAbsoluteColumn(column);
    }
    return column;
  }

  /**
   * Answers the index in the annotations array of the visible annotation at the
   * given y position. This is done by adding the heights of visible annotations
   * until the y position has been exceeded. Answers -1 if no annotations are
   * visible, or the y position is below all annotations.
   * 
   * @param yPos
   * @param aa
   * @return
   */
  static int getRowIndex(int yPos, AlignmentAnnotation[] aa)
  {
    if (aa == null)
    {
      return -1;
    }
    return getRowIndexAndOffset(yPos, aa)[0];
  }

  static int[] getRowIndexAndOffset(int yPos, AlignmentAnnotation[] aa)
  {
    int[] res = new int[2];
    res[0] = -1;
    res[1] = 0;
    if (aa == null)
    {
      return res;
    }
    int row = -1;
    int height = 0, lheight = 0;
    for (int i = 0; i < aa.length; i++)
    {
      if (aa[i].visible)
      {
        lheight = height;
        height += aa[i].height;
      }

      if (height > yPos)
      {
        row = i;
        res[0] = row;
        res[1] = yPos-lheight;
        break;
      }
    }
    return res;
  }

  /**
   * Answers a tooltip for the annotation at the current mouse position, not
   * wrapped in &lt;html&gt; tags (apply if wanted). Answers null if there is no
   * tooltip to show.
   * 
   * @param ann
   * @param column
   * @param anns
   * @param rowAndOffset
   */
  static String buildToolTip(AlignmentAnnotation ann, int column,
          AlignmentAnnotation[] anns, int rowAndOffset, AlignViewportI av,
          AlignmentPanel ap)
  {
    String tooltip = null;
    if (ann.graphGroup > -1)
    {
      StringBuilder tip = new StringBuilder(32);
      boolean first = true;
      for (int i = 0; i < anns.length; i++)
      {
        if (anns[i].graphGroup == ann.graphGroup
                && anns[i].annotations[column] != null)
        {
          if (!first)
          {
            tip.append("<br>");
          }
          first = false;
          tip.append(anns[i].label);
          String description = anns[i].annotations[column].description;
          if (description != null && description.length() > 0)
          {
            tip.append(" ").append(description);
          }
        }
      }
      tooltip = first ? null : tip.toString();
    }
    else if (column < ann.annotations.length
            && ann.annotations[column] != null)
    {
      tooltip = ann.annotations[column].description;
    }
    // TODO abstract tooltip generator so different implementations can be built
    if (ann.graph == AlignmentAnnotation.CONTACT_MAP)
    {
      if (rowAndOffset>=ann.graphHeight)
      {
        return null;
      }
      ContactListI clist = av.getContactList(ann, column);
      if (clist != null)
      {
        ContactGeometry cgeom = new ContactGeometry(clist, ann.graphHeight);
        ContactGeometry.contactInterval ci = cgeom.mapFor(rowAndOffset);
        ContactRange cr = clist.getRangeFor(ci.cStart, ci.cEnd);
        tooltip = "Contact from " + clist.getPosition() + ", [" + ci.cStart
                + " - " + ci.cEnd + "]" + "<br/>Mean:" + cr.getMean();

        int col = ann.sequenceRef.findPosition(column);
        int[][] highlightPos;
        int[] mappedPos = clist.getMappedPositionsFor(ci.cStart, ci.cEnd);
        if (mappedPos != null)
        {
          highlightPos = new int[1 + mappedPos.length][2];
          highlightPos[0] = new int[] { col, col };
          for (int p = 0, h = 0; p < mappedPos.length; h++, p += 2)
          {
            highlightPos[h][0] = ann.sequenceRef
                    .findPosition(mappedPos[p] - 1);
            highlightPos[h][1] = ann.sequenceRef
                    .findPosition(mappedPos[p + 1] - 1);
          }
        }
        else
        {
          highlightPos = new int[][] { new int[] { col, col } };
        }
        ap.getStructureSelectionManager()
                .highlightPositionsOn(ann.sequenceRef, highlightPos, null);
      }
    }
    return tooltip;
  }

  /**
   * Constructs and returns the status bar message
   * 
   * @param al
   * @param column
   * @param ann
   * @param rowAndOffset
   */
  static String getStatusMessage(AlignmentI al, int column,
          AlignmentAnnotation ann, int rowAndOffset, AlignViewportI av)
  {
    /*
     * show alignment column and annotation description if any
     */
    StringBuilder text = new StringBuilder(32);
    text.append(MessageManager.getString("label.column")).append(" ")
            .append(column + 1);

    if (column < ann.annotations.length && ann.annotations[column] != null)
    {
      String description = ann.annotations[column].description;
      if (description != null && description.trim().length() > 0)
      {
        text.append("  ").append(description);
      }
    }

    /*
     * if the annotation is sequence-specific, show the sequence number
     * in the alignment, and (if not a gap) the residue and position
     */
    SequenceI seqref = ann.sequenceRef;
    if (seqref != null)
    {
      int seqIndex = al.findIndex(seqref);
      if (seqIndex != -1)
      {
        text.append(", ").append(MessageManager.getString("label.sequence"))
                .append(" ").append(seqIndex + 1);
        char residue = seqref.getCharAt(column);
        if (!Comparison.isGap(residue))
        {
          text.append(" ");
          String name;
          if (al.isNucleotide())
          {
            name = ResidueProperties.nucleotideName
                    .get(String.valueOf(residue));
            text.append(" Nucleotide: ")
                    .append(name != null ? name : residue);
          }
          else
          {
            name = 'X' == residue ? "X"
                    : ('*' == residue ? "STOP"
                            : ResidueProperties.aa2Triplet
                                    .get(String.valueOf(residue)));
            text.append(" Residue: ").append(name != null ? name : residue);
          }
          int residuePos = seqref.findPosition(column);
          text.append(" (").append(residuePos).append(")");
        }
      }
    }

    return text.toString();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  @Override
  public void mouseClicked(MouseEvent evt)
  {
    // if (activeRow != -1)
    // {
    // AlignmentAnnotation[] aa = av.getAlignment().getAlignmentAnnotation();
    // AlignmentAnnotation anot = aa[activeRow];
    // }
  }

  // TODO mouseClicked-content and drawCursor are quite experimental!
  public void drawCursor(Graphics graphics, SequenceI seq, int res, int x1,
          int y1)
  {
    int pady = av.getCharHeight() / 5;
    int charOffset = 0;
    graphics.setColor(Color.black);
    graphics.fillRect(x1, y1, av.getCharWidth(), av.getCharHeight());

    if (av.validCharWidth)
    {
      graphics.setColor(Color.white);

      char s = seq.getCharAt(res);

      charOffset = (av.getCharWidth() - fm.charWidth(s)) / 2;
      graphics.drawString(String.valueOf(s), charOffset + x1,
              (y1 + av.getCharHeight()) - pady);
    }

  }

  private volatile boolean imageFresh = false;

  private Rectangle visibleRect = new Rectangle(),
          clipBounds = new Rectangle();

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   */
  @Override
  public void paintComponent(Graphics g)
  {

    // BH: note that this method is generally recommended to
    // call super.paintComponent(g). Otherwise, the children of this
    // component will not be rendered. That is not needed here
    // because AnnotationPanel does not have any children. It is
    // just a JPanel contained in a JViewPort.

    computeVisibleRect(visibleRect);

    g.setColor(Color.white);
    g.fillRect(0, 0, visibleRect.width, visibleRect.height);

    if (image != null)
    {
      // BH 2018 optimizing generation of new Rectangle().
      if (fastPaint
              || (visibleRect.width != (clipBounds = g
                      .getClipBounds(clipBounds)).width)
              || (visibleRect.height != clipBounds.height))
      {

        g.drawImage(image, 0, 0, this);
        fastPaint = false;
        return;
      }
    }
    updateFadedImageWidth();
    if (imgWidth < 1)
    {
      return;
    }
    Graphics2D gg;
    if (image == null || imgWidth != image.getWidth(this)
            || image.getHeight(this) != getHeight())
    {
      boolean tried = false;
      image = null;
      while (image == null && !tried)
      {
        try
        {
          image = new BufferedImage(imgWidth,
                  ap.getAnnotationPanel().getHeight(),
                  BufferedImage.TYPE_INT_RGB);
          tried = true;
        } catch (IllegalArgumentException exc)
        {
          System.err.println(
                  "Serious issue with viewport geometry imgWidth requested was "
                          + imgWidth);
          return;
        } catch (OutOfMemoryError oom)
        {
          try
          {
            System.gc();
          } catch (Exception x)
          {
          }
          ;
          new OOMWarning(
                  "Couldn't allocate memory to redraw screen. Please restart Jalview",
                  oom);
          return;
        }

      }
      gg = (Graphics2D) image.getGraphics();

      if (av.antiAlias)
      {
        gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
      }

      gg.setFont(av.getFont());
      fm = gg.getFontMetrics();
      gg.setColor(Color.white);
      gg.fillRect(0, 0, imgWidth, image.getHeight());
      imageFresh = true;
    }
    else
    {
      gg = (Graphics2D) image.getGraphics();

    }

    drawComponent(gg, av.getRanges().getStartRes(),
            av.getRanges().getEndRes() + 1);
    gg.dispose();
    imageFresh = false;
    g.drawImage(image, 0, 0, this);
  }

  public void updateFadedImageWidth()
  {
    imgWidth = (av.getRanges().getEndRes() - av.getRanges().getStartRes()
            + 1) * av.getCharWidth();

  }

  /**
   * set true to enable redraw timing debug output on stderr
   */
  private final boolean debugRedraw = false;

  /**
   * non-Thread safe repaint
   * 
   * @param horizontal
   *          repaint with horizontal shift in alignment
   */
  public void fastPaint(int horizontal)
  {
    if ((horizontal == 0) || image == null
            || av.getAlignment().getAlignmentAnnotation() == null
            || av.getAlignment().getAlignmentAnnotation().length < 1
            || av.isCalcInProgress())
    {
      repaint();
      return;
    }

    int sr = av.getRanges().getStartRes();
    int er = av.getRanges().getEndRes() + 1;
    int transX = 0;

    Graphics2D gg = (Graphics2D) image.getGraphics();

    if (imgWidth > Math.abs(horizontal * av.getCharWidth()))
    {
      // scroll is less than imgWidth away so can re-use buffered graphics
      gg.copyArea(0, 0, imgWidth, getHeight(),
              -horizontal * av.getCharWidth(), 0);

      if (horizontal > 0) // scrollbar pulled right, image to the left
      {
        transX = (er - sr - horizontal) * av.getCharWidth();
        sr = er - horizontal;
      }
      else if (horizontal < 0)
      {
        er = sr - horizontal;
      }
    }
    gg.translate(transX, 0);

    drawComponent(gg, sr, er);

    gg.translate(-transX, 0);

    gg.dispose();

    fastPaint = true;

    // Call repaint on alignment panel so that repaints from other alignment
    // panel components can be aggregated. Otherwise performance of the overview
    // window and others may be adversely affected.
    av.getAlignPanel().repaint();
  }

  private volatile boolean lastImageGood = false;

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   * @param startRes
   *          DOCUMENT ME!
   * @param endRes
   *          DOCUMENT ME!
   */
  public void drawComponent(Graphics g, int startRes, int endRes)
  {
    BufferedImage oldFaded = fadedImage;
    if (av.isCalcInProgress())
    {
      if (image == null)
      {
        lastImageGood = false;
        return;
      }
      // We'll keep a record of the old image,
      // and draw a faded image until the calculation
      // has completed
      if (lastImageGood
              && (fadedImage == null || fadedImage.getWidth() != imgWidth
                      || fadedImage.getHeight() != image.getHeight()))
      {
        // System.err.println("redraw faded image ("+(fadedImage==null ?
        // "null image" : "") + " lastGood="+lastImageGood+")");
        fadedImage = new BufferedImage(imgWidth, image.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D fadedG = (Graphics2D) fadedImage.getGraphics();

        fadedG.setColor(Color.white);
        fadedG.fillRect(0, 0, imgWidth, image.getHeight());

        fadedG.setComposite(
                AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .3f));
        fadedG.drawImage(image, 0, 0, this);

      }
      // make sure we don't overwrite the last good faded image until all
      // calculations have finished
      lastImageGood = false;

    }
    else
    {
      if (fadedImage != null)
      {
        oldFaded = fadedImage;
      }
      fadedImage = null;
    }

    g.setColor(Color.white);
    g.fillRect(0, 0, (endRes - startRes) * av.getCharWidth(), getHeight());

    g.setFont(av.getFont());
    if (fm == null)
    {
      fm = g.getFontMetrics();
    }

    if ((av.getAlignment().getAlignmentAnnotation() == null)
            || (av.getAlignment().getAlignmentAnnotation().length < 1))
    {
      g.setColor(Color.white);
      g.fillRect(0, 0, getWidth(), getHeight());
      g.setColor(Color.black);
      if (av.validCharWidth)
      {
        g.drawString(MessageManager
                .getString("label.alignment_has_no_annotations"), 20, 15);
      }

      return;
    }
    lastImageGood = renderer.drawComponent(this, av, g, activeRow, startRes,
            endRes);
    if (!lastImageGood && fadedImage == null)
    {
      fadedImage = oldFaded;
    }
    if (dragMode == DragMode.MatrixSelect)
    {
      g.setColor(Color.yellow);
      g.drawRect(Math.min(firstDragX, mouseDragLastX),
              Math.min(firstDragY, mouseDragLastY),
              Math.max(firstDragX, mouseDragLastX)
                      - Math.min(firstDragX, mouseDragLastX),
              Math.max(firstDragY, mouseDragLastY)
                      - Math.min(firstDragY, mouseDragLastY));

    }
  }

  @Override
  public FontMetrics getFontMetrics()
  {
    return fm;
  }

  @Override
  public Image getFadedImage()
  {
    return fadedImage;
  }

  @Override
  public int getFadedImageWidth()
  {
    updateFadedImageWidth();
    return imgWidth;
  }

  private int[] bounds = new int[2];

  @Override
  public int[] getVisibleVRange()
  {
    if (ap != null && ap.getAlabels() != null)
    {
      int sOffset = -ap.getAlabels().getScrollOffset();
      int visHeight = sOffset + ap.annotationSpaceFillerHolder.getHeight();
      bounds[0] = sOffset;
      bounds[1] = visHeight;
      return bounds;
    }
    else
    {
      return null;
    }
  }

  /**
   * Try to ensure any references held are nulled
   */
  public void dispose()
  {
    av = null;
    ap = null;
    image = null;
    fadedImage = null;
    // gg = null;
    _mwl = null;

    /*
     * I created the renderer so I will dispose of it
     */
    if (renderer != null)
    {
      renderer.dispose();
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    // Respond to viewport range changes (e.g. alignment panel was scrolled)
    // Both scrolling and resizing change viewport ranges: scrolling changes
    // both start and end points, but resize only changes end values.
    // Here we only want to fastpaint on a scroll, with resize using a normal
    // paint, so scroll events are identified as changes to the horizontal or
    // vertical start value.
    if (evt.getPropertyName().equals(ViewportRanges.STARTRES))
    {
      fastPaint((int) evt.getNewValue() - (int) evt.getOldValue());
    }
    else if (evt.getPropertyName().equals(ViewportRanges.STARTRESANDSEQ))
    {
      fastPaint(((int[]) evt.getNewValue())[0]
              - ((int[]) evt.getOldValue())[0]);
    }
    else if (evt.getPropertyName().equals(ViewportRanges.MOVE_VIEWPORT))
    {
      repaint();
    }
  }

  /**
   * computes the visible height of the annotation panel
   * 
   * @param adjustPanelHeight
   *          - when false, just adjust existing height according to other
   *          windows
   * @param annotationHeight
   * @return height to use for the ScrollerPreferredVisibleSize
   */
  public int adjustForAlignFrame(boolean adjustPanelHeight,
          int annotationHeight)
  {
    /*
     * Estimate available height in the AlignFrame for alignment +
     * annotations. Deduct an estimate for title bar, menu bar, scale panel,
     * hscroll, status bar, insets. 
     */
    int stuff = (ap.getViewName() != null ? 30 : 0)
            + (Platform.isAMacAndNotJS() ? 120 : 140);
    int availableHeight = ap.alignFrame.getHeight() - stuff;
    int rowHeight = av.getCharHeight();

    if (adjustPanelHeight)
    {
      int alignmentHeight = rowHeight * av.getAlignment().getHeight();

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
      annotationHeight = Math.min(ap.annotationScroller.getSize().height,
              availableHeight - 2 * rowHeight);
    }
    return annotationHeight;
  }
}
