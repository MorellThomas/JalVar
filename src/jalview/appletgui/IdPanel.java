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

import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.urls.api.UrlProviderFactoryI;
import jalview.urls.api.UrlProviderI;
import jalview.urls.applet.AppletUrlProviderFactory;
import jalview.viewmodel.AlignmentViewport;

import java.awt.BorderLayout;
import java.awt.Panel;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IdPanel extends Panel
        implements MouseListener, MouseMotionListener
{

  protected IdCanvas idCanvas;

  protected AlignmentViewport av;

  protected AlignmentPanel alignPanel;

  ScrollThread scrollThread = null;

  int lastid = -1;

  boolean mouseDragging = false;

  UrlProviderI urlProvider = null;

  public IdPanel(AlignViewport viewport, AlignmentPanel parent)
  {
    this.av = viewport;
    alignPanel = parent;
    idCanvas = new IdCanvas(viewport);
    setLayout(new BorderLayout());
    add(idCanvas, BorderLayout.CENTER);
    idCanvas.addMouseListener(this);
    idCanvas.addMouseMotionListener(this);

    String label, url;
    // TODO: add in group link parameter

    // make a list of label,url pairs
    HashMap<String, String> urlList = new HashMap<>();
    if (viewport.applet != null)
    {
      for (int i = 1; i < 10; i++)
      {
        label = viewport.applet.getParameter("linkLabel_" + i);
        url = viewport.applet.getParameter("linkURL_" + i);

        // only add non-null parameters
        if (label != null)
        {
          urlList.put(label, url);
        }
      }

      if (!urlList.isEmpty())
      {
        // set default as first entry in list
        String defaultUrl = viewport.applet.getParameter("linkLabel_1");
        UrlProviderFactoryI factory = new AppletUrlProviderFactory(
                defaultUrl, urlList);
        urlProvider = factory.createUrlProvider();
      }
    }
  }

  Tooltip tooltip;

  @Override
  public void mouseMoved(MouseEvent e)
  {
    int seq = alignPanel.seqPanel.findSeq(e);

    SequenceI sequence = av.getAlignment().getSequenceAt(seq);

    StringBuffer tooltiptext = new StringBuffer();
    if (sequence == null)
    {
      return;
    }
    if (sequence.getDescription() != null)
    {
      tooltiptext.append(sequence.getDescription());
      tooltiptext.append("\n");
    }

    for (SequenceFeature sf : sequence.getFeatures()
            .getNonPositionalFeatures())
    {
      boolean nl = false;
      if (sf.getFeatureGroup() != null)
      {
        tooltiptext.append(sf.getFeatureGroup());
        nl = true;
      }
      if (sf.getType() != null)
      {
        tooltiptext.append(" ");
        tooltiptext.append(sf.getType());
        nl = true;
      }
      if (sf.getDescription() != null)
      {
        tooltiptext.append(" ");
        tooltiptext.append(sf.getDescription());
        nl = true;
      }
      if (!Float.isNaN(sf.getScore()) && sf.getScore() != 0f)
      {
        tooltiptext.append(" Score = ");
        tooltiptext.append(sf.getScore());
        nl = true;
      }
      if (sf.getStatus() != null && sf.getStatus().length() > 0)
      {
        tooltiptext.append(" (");
        tooltiptext.append(sf.getStatus());
        tooltiptext.append(")");
        nl = true;
      }
      if (nl)
      {
        tooltiptext.append("\n");
      }
    }

    if (tooltiptext.length() == 0)
    {
      // nothing to display - so clear tooltip if one is visible
      if (tooltip != null)
      {
        tooltip.setVisible(false);
      }
      tooltip = null;
      tooltiptext = null;
      return;
    }
    if (tooltip == null)
    {
      tooltip = new Tooltip(
              sequence.getDisplayId(true) + "\n" + tooltiptext.toString(),
              idCanvas);
    }
    else
    {
      tooltip.setTip(
              sequence.getDisplayId(true) + "\n" + tooltiptext.toString());
    }
    tooltiptext = null;
  }

  @Override
  public void mouseDragged(MouseEvent e)
  {
    mouseDragging = true;

    int seq = Math.max(0, alignPanel.seqPanel.findSeq(e));

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

  @Override
  public void mouseClicked(MouseEvent e)
  {
    if (e.getClickCount() < 2)
    {
      return;
    }

    // get the sequence details
    int seq = alignPanel.seqPanel.findSeq(e);
    SequenceI sq = av.getAlignment().getSequenceAt(seq);
    if (sq == null)
    {
      return;
    }
    String id = sq.getName();

    // get the default url with the sequence details filled in
    if (urlProvider == null)
    {
      return;
    }
    String url = urlProvider.getPrimaryUrl(id);
    String target = urlProvider.getPrimaryTarget(id);
    try
    {
      alignPanel.alignFrame.showURL(url, target);
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  @Override
  public void mouseEntered(MouseEvent e)
  {
    if (scrollThread != null)
    {
      scrollThread.running = false;
    }
  }

  @Override
  public void mouseExited(MouseEvent e)
  {
    if (av.getWrapAlignment())
    {
      return;
    }

    if (mouseDragging && e.getY() < 0 && av.getRanges().getStartSeq() > 0)
    {
      scrollThread = new ScrollThread(true);
    }

    if (mouseDragging && e.getY() >= getSize().height
            && av.getAlignment().getHeight() > av.getRanges().getEndSeq())
    {
      scrollThread = new ScrollThread(false);
    }
  }

  @Override
  public void mousePressed(MouseEvent e)
  {
    if (e.getClickCount() > 1)
    {
      return;
    }

    int y = e.getY();
    if (av.getWrapAlignment())
    {
      y -= 2 * av.getCharHeight();
    }

    int seq = alignPanel.seqPanel.findSeq(e);

    if ((e.getModifiersEx()
            & InputEvent.BUTTON3_DOWN_MASK) == InputEvent.BUTTON3_DOWN_MASK)
    {
      SequenceI sq = av.getAlignment().getSequenceAt(seq);

      /*
       *  build a new links menu based on the current links
       *  and any non-positional features
       */
      List<String> nlinks;
      if (urlProvider != null)
      {
        nlinks = urlProvider.getLinksForMenu();
      }
      else
      {
        nlinks = new ArrayList<>();
      }

      for (SequenceFeature sf : sq.getFeatures().getNonPositionalFeatures())
      {
        if (sf.links != null)
        {
          for (String link : sf.links)
          {
            nlinks.add(link);
          }
        }
      }

      APopupMenu popup = new APopupMenu(alignPanel, sq, nlinks);
      this.add(popup);
      popup.show(this, e.getX(), e.getY());
      return;
    }

    if ((av.getSelectionGroup() == null)
            || ((!jalview.util.Platform.isControlDown(e)
                    && !e.isShiftDown()) && av.getSelectionGroup() != null))
    {
      av.setSelectionGroup(new SequenceGroup());
      av.getSelectionGroup().setStartRes(0);
      av.getSelectionGroup().setEndRes(av.getAlignment().getWidth() - 1);
    }

    if (e.isShiftDown() && lastid != -1)
    {
      selectSeqs(lastid, seq);
    }
    else
    {
      selectSeq(seq);
    }

    alignPanel.paintAlignment(false, false);
  }

  void selectSeq(int seq)
  {
    lastid = seq;
    SequenceI pickedSeq = av.getAlignment().getSequenceAt(seq);
    av.getSelectionGroup().addOrRemove(pickedSeq, true);
  }

  void selectSeqs(int start, int end)
  {

    lastid = start;

    if (end >= av.getAlignment().getHeight())
    {
      end = av.getAlignment().getHeight() - 1;
    }

    if (end < start)
    {
      int tmp = start;
      start = end;
      end = tmp;
      lastid = end;
    }
    if (av.getSelectionGroup() == null)
    {
      av.setSelectionGroup(new SequenceGroup());
    }
    for (int i = start; i <= end; i++)
    {
      av.getSelectionGroup().addSequence(av.getAlignment().getSequenceAt(i),
              i == end);
    }

  }

  @Override
  public void mouseReleased(MouseEvent e)
  {
    if (scrollThread != null)
    {
      scrollThread.running = false;
    }

    if (av.getSelectionGroup() != null)
    {
      av.getSelectionGroup().recalcConservation();
    }

    mouseDragging = false;
    PaintRefresher.Refresh(this, av.getSequenceSetId());
    // always send selection message when mouse is released
    av.sendSelection();
  }

  public void highlightSearchResults(List<SequenceI> list)
  {
    idCanvas.setHighlighted(list);

    if (list == null || list.isEmpty())
    {
      return;
    }

    int index = av.getAlignment().findIndex(list.get(0));

    // do we need to scroll the panel?
    if (av.getRanges().getStartSeq() > index
            || av.getRanges().getEndSeq() < index)
    {
      av.getRanges().setStartSeq(index);
    }
  }

  // this class allows scrolling off the bottom of the visible alignment
  class ScrollThread extends Thread
  {
    boolean running = false;

    boolean up = true;

    public ScrollThread(boolean isUp)
    {
      this.up = isUp;
      start();
    }

    public void stopScrolling()
    {
      running = false;
    }

    @Override
    public void run()
    {
      running = true;
      while (running)
      {
        if (av.getRanges().scrollUp(up))
        {
          // scroll was ok, so add new sequence to selection
          int seq = av.getRanges().getStartSeq();
          if (!up)
          {
            seq = av.getRanges().getEndSeq();
          }

          if (seq < lastid)
          {
            selectSeqs(lastid - 1, seq);
          }
          else if (seq > lastid && seq < av.getAlignment().getHeight())
          {
            selectSeqs(lastid + 1, seq);
          }

          lastid = seq;
        }
        else
        {
          running = false;
        }

        alignPanel.paintAlignment(true, false);
        try
        {
          Thread.sleep(100);
        } catch (Exception ex)
        {
        }
      }
    }
  }
}
