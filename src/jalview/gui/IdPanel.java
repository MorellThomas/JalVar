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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.gui.SeqPanel.MousePos;
import jalview.io.SequenceAnnotationReport;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.viewmodel.AlignmentViewport;
import jalview.viewmodel.ViewportRanges;

/**
 * This panel hosts alignment sequence ids and responds to mouse clicks on them,
 * as well as highlighting ids matched by a search from the Find menu.
 * 
 * @author $author$
 * @version $Revision$
 */
public class IdPanel extends JPanel
        implements MouseListener, MouseMotionListener, MouseWheelListener
{
  private IdCanvas idCanvas;

  protected AlignmentViewport av;

  protected AlignmentPanel alignPanel;

  ScrollThread scrollThread = null;

  int offy;

  // int width;
  int lastid = -1;

  boolean mouseDragging = false;

  private final SequenceAnnotationReport seqAnnotReport;

  /**
   * Creates a new IdPanel object.
   * 
   * @param av
   * @param parent
   */
  public IdPanel(AlignViewport av, AlignmentPanel parent)
  {
    this.av = av;
    alignPanel = parent;
    setIdCanvas(new IdCanvas(av));
    seqAnnotReport = new SequenceAnnotationReport(true);
    setLayout(new BorderLayout());
    add(getIdCanvas(), BorderLayout.CENTER);
    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(this);
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  /**
   * Responds to mouse movement by setting tooltip text for the sequence id
   * under the mouse (or possibly annotation label, when in wrapped mode)
   * 
   * @param e
   */
  @Override
  public void mouseMoved(MouseEvent e)
  {
    SeqPanel sp = alignPanel.getSeqPanel();
    MousePos pos = sp.findMousePosition(e);
    if (pos.isOverAnnotation())
    {
      /*
       * mouse is over an annotation label in wrapped mode
       */
      AlignmentAnnotation[] anns = av.getAlignment()
              .getAlignmentAnnotation();
      AlignmentAnnotation annotation = anns[pos.annotationIndex];
      setToolTipText(AnnotationLabels.getTooltip(annotation));
      alignPanel.alignFrame.setStatus(
              AnnotationLabels.getStatusMessage(annotation, anns));
    }
    else
    {
      int seq = Math.max(0, pos.seqIndex);
      if (seq < av.getAlignment().getHeight())
      {
        SequenceI sequence = av.getAlignment().getSequenceAt(seq);
        StringBuilder tip = new StringBuilder(64);
        tip.append(sequence.getDisplayId(true)).append(" ");
        seqAnnotReport.createTooltipAnnotationReport(tip, sequence,
                av.isShowDBRefs(), av.isShowNPFeats(), sp.seqCanvas.fr);
        setToolTipText(JvSwingUtils.wrapTooltip(true, tip.toString()));

        StringBuilder text = new StringBuilder();
        text.append("Sequence ").append(String.valueOf(seq + 1))
                .append(" ID: ").append(sequence.getName());
        alignPanel.alignFrame.setStatus(text.toString());
      }
    }
  }

  /**
   * Responds to a mouse drag by selecting the sequences under the dragged
   * region.
   * 
   * @param e
   */
  @Override
  public void mouseDragged(MouseEvent e)
  {
    mouseDragging = true;

    MousePos pos = alignPanel.getSeqPanel().findMousePosition(e);
    if (pos.isOverAnnotation())
    {
      // mouse is over annotation label in wrapped mode
      return;
    }

    int seq = Math.max(0, pos.seqIndex);

    if (seq < lastid)
    {
      selectSeqs(lastid - 1, seq);
    }
    else if (seq > lastid)
    {
      selectSeqs(lastid + 1, seq);
    }

    lastid = seq;
    alignPanel.paintAlignment(false, false);
  }

  /**
   * Response to the mouse wheel by scrolling the alignment panel.
   */
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
  }

  /**
   * Handle a mouse click event. Currently only responds to a double-click. The
   * action is to try to open a browser window at a URL that searches for the
   * selected sequence id. The search URL is configured in Preferences |
   * Connections | URL link from Sequence ID. For example:
   * 
   * http://www.ebi.ac.uk/ebisearch/search.ebi?db=allebi&query=$SEQUENCE_ID$
   * 
   * @param e
   */
  @Override
  public void mouseClicked(MouseEvent e)
  {
    /*
     * Ignore single click. Ignore 'left' click followed by 'right' click (user
     * selects a row then its pop-up menu).
     */
    if (e.getClickCount() < 2 || SwingUtilities.isRightMouseButton(e))
    {
      // reinstate isRightMouseButton check to ignore mouse-related popup events
      // note - this does nothing on default MacBookPro force-trackpad config!
      return;
    }

    MousePos pos = alignPanel.getSeqPanel().findMousePosition(e);
    int seq = pos.seqIndex;
    if (pos.isOverAnnotation() || seq < 0)
    {
      return;
    }

    String id = av.getAlignment().getSequenceAt(seq).getName();
    String url = Preferences.sequenceUrlLinks.getPrimaryUrl(id);

    try
    {
      jalview.util.BrowserLauncher.openURL(url);
    } catch (Exception ex)
    {
      JvOptionPane.showInternalMessageDialog(Desktop.desktop,
              MessageManager.getString("label.web_browser_not_found_unix"),
              MessageManager.getString("label.web_browser_not_found"),
              JvOptionPane.WARNING_MESSAGE);
      ex.printStackTrace();
    }
  }

  /**
   * On (re-)entering the panel, stop any scrolling
   * 
   * @param e
   */
  @Override
  public void mouseEntered(MouseEvent e)
  {
    stopScrolling();
  }

  /**
   * Interrupts the scroll thread if one is running
   */
  void stopScrolling()
  {
    if (scrollThread != null)
    {
      scrollThread.stopScrolling();
      scrollThread = null;
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void mouseExited(MouseEvent e)
  {
    if (av.getWrapAlignment())
    {
      return;
    }

    if (mouseDragging)
    {
      /*
       * on mouse drag above or below the panel, start 
       * scrolling if there are more sequences to show
       */
      ViewportRanges ranges = av.getRanges();
      if (e.getY() < 0 && ranges.getStartSeq() > 0)
      {
        startScrolling(true);
      }
      else if (e.getY() >= getHeight()
              && ranges.getEndSeq() <= av.getAlignment().getHeight())
      {
        startScrolling(false);
      }
    }
  }

  /**
   * Starts scrolling either up or down
   * 
   * @param up
   */
  void startScrolling(boolean up)
  {
    scrollThread = new ScrollThread(up);
    if (Platform.isJS())
    {
      /*
       * for JalviewJS using Swing Timer
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
            // IdPanel.stopScrolling called
            t.stop();
          }
        }
      });
      t.start();
    }
    else
    /**
     * Java only
     * 
     * @j2sIgnore
     */
    {
      scrollThread.start();
    }
  }

  /**
   * Respond to a mouse press. Does nothing for (left) double-click as this is
   * handled by mouseClicked().
   * 
   * Right mouse down - construct and show context menu.
   * 
   * Ctrl-down or Shift-down - add to or expand current selection group if there
   * is one.
   * 
   * Mouse down - select this sequence.
   * 
   * @param e
   */
  @Override
  public void mousePressed(MouseEvent e)
  {
    if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e))
    {
      return;
    }

    MousePos pos = alignPanel.getSeqPanel().findMousePosition(e);

    if (e.isPopupTrigger()) // Mac reports this in mousePressed
    {
      showPopupMenu(e, pos);
      return;
    }

    /*
     * defer right-mouse click handling to mouseReleased on Windows
     * (where isPopupTrigger() will answer true)
     * NB isRightMouseButton is also true for Cmd-click on Mac
     */
    if (Platform.isWinRightButton(e))
    {
      return;
    }

    if ((av.getSelectionGroup() == null)
            || (!jalview.util.Platform.isControlDown(e) && !e.isShiftDown()
                    && av.getSelectionGroup() != null))
    {
      av.setSelectionGroup(new SequenceGroup());
      av.getSelectionGroup().setStartRes(0);
      av.getSelectionGroup().setEndRes(av.getAlignment().getWidth() - 1);
    }

    if (e.isShiftDown() && (lastid != -1))
    {
      selectSeqs(lastid, pos.seqIndex);
    }
    else
    {
      selectSeq(pos.seqIndex);
    }

    av.isSelectionGroupChanged(true);

    alignPanel.paintAlignment(false, false);
  }

  /**
   * Build and show the popup-menu at the right-click mouse position
   * 
   * @param e
   */
  void showPopupMenu(MouseEvent e, MousePos pos)
  {
    if (pos.isOverAnnotation())
    {
      showAnnotationMenu(e, pos);
      return;
    }

    Sequence sq = (Sequence) av.getAlignment().getSequenceAt(pos.seqIndex);
    if (sq != null)
    {
      PopupMenu pop = new PopupMenu(alignPanel, sq,
              Preferences.getGroupURLLinks());
      pop.show(this, e.getX(), e.getY());
    }
  }

  /**
   * On right mouse click on a Consensus annotation label, shows a limited popup
   * menu, with options to configure the consensus calculation and rendering.
   * 
   * @param e
   * @param pos
   * @see AnnotationLabels#showPopupMenu(MouseEvent)
   */
  void showAnnotationMenu(MouseEvent e, MousePos pos)
  {
    if (pos.annotationIndex == -1)
    {
      return;
    }
    AlignmentAnnotation[] anns = this.av.getAlignment()
            .getAlignmentAnnotation();
    if (anns == null || pos.annotationIndex >= anns.length)
    {
      return;
    }
    AlignmentAnnotation ann = anns[pos.annotationIndex];
    if (!ann.label.contains("Consensus"))
    {
      return;
    }

    JPopupMenu pop = new JPopupMenu(
            MessageManager.getString("label.annotations"));
    AnnotationLabels.addConsensusMenuOptions(this.alignPanel, ann, pop);
    pop.show(this, e.getX(), e.getY());
  }

  /**
   * Toggle whether the sequence is part of the current selection group.
   * 
   * @param seq
   */
  void selectSeq(int seq)
  {
    lastid = seq;

    SequenceI pickedSeq = av.getAlignment().getSequenceAt(seq);
    av.getSelectionGroup().addOrRemove(pickedSeq, false);
  }

  /**
   * Add contiguous rows of the alignment to the current selection group. Does
   * nothing if there is no selection group.
   * 
   * @param start
   * @param end
   */
  void selectSeqs(int start, int end)
  {
    if (av.getSelectionGroup() == null)
    {
      return;
    }

    if (end >= av.getAlignment().getHeight())
    {
      end = av.getAlignment().getHeight() - 1;
    }

    lastid = start;

    if (end < start)
    {
      int tmp = start;
      start = end;
      end = tmp;
      lastid = end;
    }

    for (int i = start; i <= end; i++)
    {
      av.getSelectionGroup().addSequence(av.getAlignment().getSequenceAt(i),
              false);
    }
  }

  /**
   * Respond to mouse released. Refreshes the display and triggers broadcast of
   * the new selection group to any listeners.
   * 
   * @param e
   */
  @Override
  public void mouseReleased(MouseEvent e)
  {
    if (scrollThread != null)
    {
      stopScrolling();
    }
    MousePos pos = alignPanel.getSeqPanel().findMousePosition(e);

    mouseDragging = false;
    PaintRefresher.Refresh(this, av.getSequenceSetId());
    // always send selection message when mouse is released
    av.sendSelection();

    if (e.isPopupTrigger()) // Windows reports this in mouseReleased
    {
      showPopupMenu(e, pos);
    }
  }

  /**
   * Highlight sequence ids that match the given list, and if necessary scroll
   * to the start sequence of the list.
   * 
   * @param list
   */
  public void highlightSearchResults(List<SequenceI> list)
  {
    getIdCanvas().setHighlighted(list);

    if (list == null || list.isEmpty())
    {
      return;
    }

    int index = av.getAlignment().findIndex(list.get(0));

    // do we need to scroll the panel?
    if ((av.getRanges().getStartSeq() > index)
            || (av.getRanges().getEndSeq() < index))
    {
      av.getRanges().setStartSeq(index);
    }
  }

  public IdCanvas getIdCanvas()
  {
    return idCanvas;
  }

  public void setIdCanvas(IdCanvas idCanvas)
  {
    this.idCanvas = idCanvas;
  }

  /**
   * Performs scrolling of the visible alignment up or down, adding newly
   * visible sequences to the current selection
   */
  class ScrollThread extends Thread
  {
    private boolean running = false;

    private boolean up;

    /**
     * Constructor for a thread that scrolls either up or down
     * 
     * @param up
     */
    public ScrollThread(boolean up)
    {
      this.up = up;
      setName("IdPanel$ScrollThread$" + String.valueOf(up));
    }

    /**
     * Sets a flag to stop the scrolling
     */
    public void stopScrolling()
    {
      running = false;
    }

    /**
     * Scrolls the alignment either up or down, one row at a time, adding newly
     * visible sequences to the current selection. Speed is limited to a maximum
     * of ten rows per second. The thread exits when the end of the alignment is
     * reached or a flag is set to stop it by a call to stopScrolling.
     */
    @Override
    public void run()
    {
      running = true;

      while (running)
      {
        running = scrollOnce();
        try
        {
          Thread.sleep(100);
        } catch (Exception ex)
        {
        }
      }
      IdPanel.this.scrollThread = null;
    }

    /**
     * Scrolls one row up or down. Answers true if a scroll could be done, false
     * if not (top or bottom of alignment reached).
     */
    boolean scrollOnce()
    {
      ViewportRanges ranges = IdPanel.this.av.getRanges();
      if (ranges.scrollUp(up))
      {
        int toSeq = up ? ranges.getStartSeq() : ranges.getEndSeq();
        int fromSeq = toSeq < lastid ? lastid - 1 : lastid + 1;
        IdPanel.this.selectSeqs(fromSeq, toSeq);
        lastid = toSeq;
        alignPanel.paintAlignment(false, false);
        return true;
      }

      return false;
    }
  }
}
