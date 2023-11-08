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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import jalview.analysis.Conservation;
import jalview.analysis.TreeModel;
import jalview.api.AlignViewportI;
import jalview.bin.Console;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Annotation;
import jalview.datamodel.BinaryNode;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.ContactMatrixI;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.datamodel.SequenceNode;
import jalview.gui.JalviewColourChooser.ColourChooserListener;
import jalview.schemes.ColourSchemeI;
import jalview.structure.SelectionSource;
import jalview.util.ColorUtils;
import jalview.util.Format;
import jalview.util.MessageManager;
import jalview.ws.datamodel.MappableContactMatrixI;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class TreeCanvas extends JPanel implements MouseListener, Runnable,
        Printable, MouseMotionListener, SelectionSource
{
  /** DOCUMENT ME!! */
  public static final String PLACEHOLDER = " * ";

  TreeModel tree;

  JScrollPane scrollPane;

  TreePanel tp;

  private AlignViewport av;

  private AlignmentPanel ap;

  Font font;

  FontMetrics fm;

  boolean fitToWindow = true;

  boolean showDistances = false;

  boolean showBootstrap = false;

  boolean markPlaceholders = false;

  int offx = 20;

  int offy;

  private float threshold;

  String longestName;

  int labelLength = -1;

  Map<Object, Rectangle> nameHash = new Hashtable<>();

  Map<BinaryNode, Rectangle> nodeHash = new Hashtable<>();

  BinaryNode highlightNode;

  boolean applyToAllViews = false;

  /**
   * Creates a new TreeCanvas object.
   * 
   * @param av
   *          DOCUMENT ME!
   * @param tree
   *          DOCUMENT ME!
   * @param scroller
   *          DOCUMENT ME!
   * @param label
   *          DOCUMENT ME!
   */
  public TreeCanvas(TreePanel tp, AlignmentPanel ap, JScrollPane scroller)
  {
    this.tp = tp;
    this.av = ap.av;
    this.setAssociatedPanel(ap);
    font = av.getFont();
    scrollPane = scroller;
    addMouseListener(this);
    addMouseMotionListener(this);
    
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  public void clearSelectedLeaves()
  {
    Vector<BinaryNode> leaves = tp.getTree()
            .findLeaves(tp.getTree().getTopNode());
    if (tp.isColumnWise())
    {
      markColumnsFor(getAssociatedPanels(), leaves, Color.white, true);
    }
    else
    {
      for (AlignmentPanel ap : getAssociatedPanels())
      {
        SequenceGroup selected = ap.av.getSelectionGroup();
        if (selected != null)
        {
          {
            for (int i = 0; i < leaves.size(); i++)
            {
              SequenceI seq = (SequenceI) leaves.elementAt(i).element();
              if (selected.contains(seq))
              {
                selected.addOrRemove(seq, false);
              }
            }
            selected.recalcConservation();
          }
        }
        ap.av.sendSelection();
      }
    }
    PaintRefresher.Refresh(tp, av.getSequenceSetId());
    repaint();
  }
  /**
   * DOCUMENT ME!
   * 
   * @param sequence
   *          DOCUMENT ME!
   */
  public void treeSelectionChanged(SequenceI sequence)
  {
    AlignmentPanel[] aps = getAssociatedPanels();

    for (int a = 0; a < aps.length; a++)
    {
      SequenceGroup selected = aps[a].av.getSelectionGroup();

      if (selected == null)
      {
        selected = new SequenceGroup();
        aps[a].av.setSelectionGroup(selected);
      }

      selected.setEndRes(aps[a].av.getAlignment().getWidth() - 1);
      selected.addOrRemove(sequence, true);
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param tree
   *          DOCUMENT ME!
   */
  public void setTree(TreeModel tree)
  {
    this.tree = tree;
    tree.findHeight(tree.getTopNode());

    // Now have to calculate longest name based on the leaves
    Vector<BinaryNode> leaves = tree.findLeaves(tree.getTopNode());
    boolean has_placeholders = false;
    longestName = "";

    AlignmentAnnotation aa = tp.getAssocAnnotation();
    ContactMatrixI cm = (aa!=null) ? av.getContactMatrix(aa) : null;
    if (cm!=null && cm.hasCutHeight())
    {
      threshold=(float) cm.getCutHeight();
    }
    
    for (int i = 0; i < leaves.size(); i++)
    {
      BinaryNode lf = leaves.elementAt(i);

      if (lf instanceof SequenceNode && ((SequenceNode) lf).isPlaceholder())
      {
        has_placeholders = true;
      }

      if (longestName.length() < ((Sequence) lf.element()).getName()
              .length())
      {
        longestName = TreeCanvas.PLACEHOLDER
                + ((Sequence) lf.element()).getName();
      }
      if (tp.isColumnWise() && cm!=null)
      {
        // get color from group colours, if they are set for the matrix
        try {
          Color col = cm.getGroupColorForPosition(parseColumnNode(lf));
          setColor(lf,col.brighter());
        } catch (NumberFormatException ex) {};
      }
    }

    setMarkPlaceholders(has_placeholders);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   * @param node
   *          DOCUMENT ME!
   * @param chunk
   *          DOCUMENT ME!
   * @param wscale
   *          DOCUMENT ME!
   * @param width
   *          DOCUMENT ME!
   * @param offx
   *          DOCUMENT ME!
   * @param offy
   *          DOCUMENT ME!
   */
  public void drawNode(Graphics g, BinaryNode node, double chunk,
          double wscale, int width, int offx, int offy)
  {
    if (node == null)
    {
      return;
    }

    if ((node.left() == null) && (node.right() == null))
    {
      // Drawing leaf node
      double height = node.height;
      double dist = node.dist;

      int xstart = (int) ((height - dist) * wscale) + offx;
      int xend = (int) (height * wscale) + offx;

      int ypos = (int) (node.ycount * chunk) + offy;

      if (node.element() instanceof SequenceI)
      {
        SequenceI seq = (SequenceI) node.element();

        if (av.getSequenceColour(seq) == Color.white)
        {
          g.setColor(Color.black);
        }
        else
        {
          g.setColor(av.getSequenceColour(seq).darker());
        }
      }
      else
      {
        g.setColor(Color.black);
      }

      // Draw horizontal line
      g.drawLine(xstart, ypos, xend, ypos);

      String nodeLabel = "";

      if (showDistances && (node.dist > 0))
      {
        nodeLabel = new Format("%g").form(node.dist);
      }

      if (showBootstrap && node.bootstrap > -1)
      {
        if (showDistances)
        {
          nodeLabel = nodeLabel + " : ";
        }

        nodeLabel = nodeLabel + String.valueOf(node.bootstrap);
      }

      if (!nodeLabel.equals(""))
      {
        g.drawString(nodeLabel, xstart + 2, ypos - 2);
      }

      String name = (markPlaceholders && ((node instanceof SequenceNode
              && ((SequenceNode) node).isPlaceholder())))
                      ? (PLACEHOLDER + node.getName())
                      : node.getName();

      int charWidth = fm.stringWidth(name) + 3;
      int charHeight = font.getSize();

      Rectangle rect = new Rectangle(xend + 10, ypos - charHeight / 2,
              charWidth, charHeight);

      nameHash.put(node.element(), rect);

      // Colour selected leaves differently
      boolean isSelected = false;
      if (tp.isColumnWise())
      {
        isSelected = isColumnForNodeSelected(node);
      }
      else
      {
        SequenceGroup selected = av.getSelectionGroup();

        if ((selected != null)
                && selected.getSequences(null).contains(node.element()))
        {
          isSelected = true;
        }
      }
      if (isSelected)
      {
        g.setColor(Color.gray);

        g.fillRect(xend + 10, ypos - charHeight / 2, charWidth, charHeight);
        g.setColor(Color.white);
      }

      g.drawString(name, xend + 10, ypos + fm.getDescent());
      g.setColor(Color.black);
    }
    else
    {
      drawNode(g, (BinaryNode) node.left(), chunk, wscale, width, offx,
              offy);
      drawNode(g, (BinaryNode) node.right(), chunk, wscale, width, offx,
              offy);

      double height = node.height;
      double dist = node.dist;

      int xstart = (int) ((height - dist) * wscale) + offx;
      int xend = (int) (height * wscale) + offx;
      int ypos = (int) (node.ycount * chunk) + offy;

      g.setColor(node.color.darker());

      // Draw horizontal line
      g.drawLine(xstart, ypos, xend, ypos);
      if (node == highlightNode)
      {
        g.fillRect(xend - 3, ypos - 3, 6, 6);
      }
      else
      {
        g.fillRect(xend - 2, ypos - 2, 4, 4);
      }

      int ystart = (node.left() == null ? 0
              : (int) (((BinaryNode) node.left()).ycount * chunk)) + offy;
      int yend = (node.right() == null ? 0
              : (int) (((BinaryNode) node.right()).ycount * chunk)) + offy;

      Rectangle pos = new Rectangle(xend - 2, ypos - 2, 5, 5);
      nodeHash.put(node, pos);

      g.drawLine((int) (height * wscale) + offx, ystart,
              (int) (height * wscale) + offx, yend);

      String nodeLabel = "";

      if (showDistances && (node.dist > 0))
      {
        nodeLabel = new Format("%g").form(node.dist);
      }

      if (showBootstrap && node.bootstrap > -1)
      {
        if (showDistances)
        {
          nodeLabel = nodeLabel + " : ";
        }

        nodeLabel = nodeLabel + String.valueOf(node.bootstrap);
      }

      if (!nodeLabel.equals(""))
      {
        g.drawString(nodeLabel, xstart + 2, ypos - 2);
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param x
   *          DOCUMENT ME!
   * @param y
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public Object findElement(int x, int y)
  {
    for (Entry<Object, Rectangle> entry : nameHash.entrySet())
    {
      Rectangle rect = entry.getValue();

      if ((x >= rect.x) && (x <= (rect.x + rect.width)) && (y >= rect.y)
              && (y <= (rect.y + rect.height)))
      {
        return entry.getKey();
      }
    }

    for (Entry<BinaryNode, Rectangle> entry : nodeHash.entrySet())
    {
      Rectangle rect = entry.getValue();

      if ((x >= rect.x) && (x <= (rect.x + rect.width)) && (y >= rect.y)
              && (y <= (rect.y + rect.height)))
      {
        return entry.getKey();
      }
    }

    return null;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param pickBox
   *          DOCUMENT ME!
   */
  public void pickNodes(Rectangle pickBox)
  {
    int width = getWidth();
    int height = getHeight();

    BinaryNode top = tree.getTopNode();

    double wscale = ((width * .8) - (offx * 2)) / tree.getMaxHeight();

    if (top.count == 0)
    {
      top.count = ((BinaryNode) top.left()).count
              + ((BinaryNode) top.right()).count;
    }

    float chunk = (float) (height - (offy)) / top.count;

    pickNode(pickBox, top, chunk, wscale, width, offx, offy);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param pickBox
   *          DOCUMENT ME!
   * @param node
   *          DOCUMENT ME!
   * @param chunk
   *          DOCUMENT ME!
   * @param wscale
   *          DOCUMENT ME!
   * @param width
   *          DOCUMENT ME!
   * @param offx
   *          DOCUMENT ME!
   * @param offy
   *          DOCUMENT ME!
   */
  public void pickNode(Rectangle pickBox, BinaryNode node, float chunk,
          double wscale, int width, int offx, int offy)
  {
    if (node == null)
    {
      return;
    }

    if ((node.left() == null) && (node.right() == null))
    {
      double height = node.height;
      // double dist = node.dist;
      // int xstart = (int) ((height - dist) * wscale) + offx;
      int xend = (int) (height * wscale) + offx;

      int ypos = (int) (node.ycount * chunk) + offy;

      if (pickBox.contains(new Point(xend, ypos)))
      {
        if (node.element() instanceof SequenceI)
        {
          SequenceI seq = (SequenceI) node.element();
          SequenceGroup sg = av.getSelectionGroup();

          if (sg != null)
          {
            sg.addOrRemove(seq, true);
          }
        }
      }
    }
    else
    {
      pickNode(pickBox, (BinaryNode) node.left(), chunk, wscale, width,
              offx, offy);
      pickNode(pickBox, (BinaryNode) node.right(), chunk, wscale, width,
              offx, offy);
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param node
   *          DOCUMENT ME!
   * @param c
   *          DOCUMENT ME!
   */
  public void setColor(BinaryNode node, Color c)
  {
    if (node == null)
    {
      return;
    }

    node.color = c;
    if (node.element() instanceof SequenceI)
    {
      final SequenceI seq = (SequenceI) node.element();
      AlignmentPanel[] aps = getAssociatedPanels();
      if (aps != null)
      {
        for (int a = 0; a < aps.length; a++)
        {
          aps[a].av.setSequenceColour(seq, c);
        }
      }
    }
    setColor((BinaryNode) node.left(), c);
    setColor((BinaryNode) node.right(), c);
  }

  /**
   * DOCUMENT ME!
   */
  void startPrinting()
  {
    Thread thread = new Thread(this);
    thread.start();
  }

  // put printing in a thread to avoid painting problems
  @Override
  public void run()
  {
    PrinterJob printJob = PrinterJob.getPrinterJob();
    PageFormat defaultPage = printJob.defaultPage();
    PageFormat pf = printJob.pageDialog(defaultPage);

    if (defaultPage == pf)
    {
      /*
       * user cancelled
       */
      return;
    }

    printJob.setPrintable(this, pf);

    if (printJob.printDialog())
    {
      try
      {
        printJob.print();
      } catch (Exception PrintException)
      {
        PrintException.printStackTrace();
      }
    }
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
    pg.setFont(font);
    pg.translate((int) pf.getImageableX(), (int) pf.getImageableY());

    int pwidth = (int) pf.getImageableWidth();
    int pheight = (int) pf.getImageableHeight();

    int noPages = getHeight() / pheight;

    if (pi > noPages)
    {
      return Printable.NO_SUCH_PAGE;
    }

    if (pwidth > getWidth())
    {
      pwidth = getWidth();
    }

    if (fitToWindow)
    {
      if (pheight > getHeight())
      {
        pheight = getHeight();
      }

      noPages = 0;
    }
    else
    {
      FontMetrics fm = pg.getFontMetrics(font);
      int height = fm.getHeight() * nameHash.size();
      pg.translate(0, -pi * pheight);
      pg.setClip(0, pi * pheight, pwidth, (pi * pheight) + pheight);

      // translate number of pages,
      // height is screen size as this is the
      // non overlapping text size
      pheight = height;
    }

    draw(pg, pwidth, pheight);

    return Printable.PAGE_EXISTS;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   */
  @Override
  public void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    g.setFont(font);

    if (tree == null)
    {
      g.drawString(
              MessageManager.getString("label.calculating_tree") + "....",
              20, getHeight() / 2);
    }
    else
    {
      fm = g.getFontMetrics(font);

      int nameCount = nameHash.size();
      if (nameCount == 0)
      {
        repaint();
      }

      if (fitToWindow || (!fitToWindow && (scrollPane
              .getHeight() > ((fm.getHeight() * nameCount) + offy))))
      {
        draw(g, scrollPane.getWidth(), scrollPane.getHeight());
        setPreferredSize(null);
      }
      else
      {
        setPreferredSize(new Dimension(scrollPane.getWidth(),
                fm.getHeight() * nameCount));
        draw(g, scrollPane.getWidth(), fm.getHeight() * nameCount);
      }

      scrollPane.revalidate();
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param fontSize
   *          DOCUMENT ME!
   */
  @Override
  public void setFont(Font font)
  {
    this.font = font;
    repaint();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param g1
   *          DOCUMENT ME!
   * @param width
   *          DOCUMENT ME!
   * @param height
   *          DOCUMENT ME!
   */
  public void draw(Graphics g1, int width, int height)
  {
    Graphics2D g2 = (Graphics2D) g1;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(Color.white);
    g2.fillRect(0, 0, width, height);
    g2.setFont(font);

    if (longestName == null || tree == null)
    {
      g2.drawString("Calculating tree.", 20, 20);
      return;
    }
    offy = font.getSize() + 10;

    fm = g2.getFontMetrics(font);

    labelLength = fm.stringWidth(longestName) + 20; // 20 allows for scrollbar

    double wscale = (width - labelLength - (offx * 2))
            / tree.getMaxHeight();

    BinaryNode top = tree.getTopNode();

    if (top.count == 0)
    {
      top.count = ((BinaryNode) top.left()).count
              + ((BinaryNode) top.right()).count;
    }

    double chunk = (double) (height - (offy)) / (double)top.count;

    drawNode(g2, tree.getTopNode(), chunk, wscale, width, offx, offy);

    if (threshold != 0)
    {
      if (av.getCurrentTree() == tree)
      {
        g2.setColor(Color.red);
      }
      else
      {
        g2.setColor(Color.gray);
      }

      int x = (int) ((threshold * (getWidth() - labelLength - (2 * offx)))
              + offx);

      g2.drawLine(x, 0, x, getHeight());
    }
  }

  /**
   * Empty method to satisfy the MouseListener interface
   * 
   * @param e
   */
  @Override
  public void mouseReleased(MouseEvent e)
  {
    /*
     * isPopupTrigger is set on mouseReleased on Windows
     */
    if (e.isPopupTrigger())
    {
      chooseSubtreeColour();
      e.consume(); // prevent mouseClicked happening
    }
  }

  /**
   * Empty method to satisfy the MouseListener interface
   * 
   * @param e
   */
  @Override
  public void mouseEntered(MouseEvent e)
  {
  }

  /**
   * Empty method to satisfy the MouseListener interface
   * 
   * @param e
   */
  @Override
  public void mouseExited(MouseEvent e)
  {
  }

  /**
   * Handles a mouse click on a tree node (clicks elsewhere are handled in
   * mousePressed). Click selects the sub-tree, double-click swaps leaf nodes
   * order, right-click opens a dialogue to choose colour for the sub-tree.
   * 
   * @param e
   */
  @Override
  public void mouseClicked(MouseEvent evt)
  {
    if (highlightNode == null)
    {
      return;
    }

    if (evt.getClickCount() > 1)
    {
      tree.swapNodes(highlightNode);
      tree.reCount(tree.getTopNode());
      tree.findHeight(tree.getTopNode());
    }
    else
    {
      Vector<BinaryNode> leaves = tree.findLeaves(highlightNode);
      if (tp.isColumnWise())
      {
        markColumnsFor(getAssociatedPanels(), leaves, Color.red,false);
      }
      else
      {
        for (int i = 0; i < leaves.size(); i++)
        {
          SequenceI seq = (SequenceI) leaves.elementAt(i).element();
          treeSelectionChanged(seq);
        }
      }
      av.sendSelection();
    }

    PaintRefresher.Refresh(tp, av.getSequenceSetId());
    repaint();
  }

  /**
   * Offer the user the option to choose a colour for the highlighted node and
   * its children; this colour is also applied to the corresponding sequence ids
   * in the alignment
   */
  void chooseSubtreeColour()
  {
    String ttl = MessageManager.getString("label.select_subtree_colour");
    ColourChooserListener listener = new ColourChooserListener()
    {
      @Override
      public void colourSelected(Color c)
      {
        setColor(highlightNode, c);
        PaintRefresher.Refresh(tp, ap.av.getSequenceSetId());
        repaint();
      }
    };
    JalviewColourChooser.showColourChooser(this, ttl, highlightNode.color,
            listener);
  }

  @Override
  public void mouseMoved(MouseEvent evt)
  {
    av.setCurrentTree(tree);

    Object ob = findElement(evt.getX(), evt.getY());

    if (ob instanceof BinaryNode)
    {
      highlightNode = (BinaryNode) ob;
      this.setToolTipText(
              "<html>" + MessageManager.getString("label.highlightnode"));
      repaint();

    }
    else
    {
      if (highlightNode != null)
      {
        highlightNode = null;
        setToolTipText(null);
        repaint();
      }
    }
  }

  @Override
  public void mouseDragged(MouseEvent ect)
  {
  }

  /**
   * Handles a mouse press on a sequence name or the tree background canvas
   * (click on a node is handled in mouseClicked). The action is to create
   * groups by partitioning the tree at the mouse position. Colours for the
   * groups (and sequence names) are generated randomly.
   * 
   * @param e
   */
  @Override
  public void mousePressed(MouseEvent e)
  {
    av.setCurrentTree(tree);

    /*
     * isPopupTrigger is set for mousePressed (Mac)
     * or mouseReleased (Windows)
     */
    if (e.isPopupTrigger())
    {
      if (highlightNode != null)
      {
        chooseSubtreeColour();
      }
      return;
    }

    /*
     * defer right-click handling on Windows to
     * mouseClicked; note isRightMouseButton
     * also matches Cmd-click on Mac which should do
     * nothing here
     */
    if (SwingUtilities.isRightMouseButton(e))
    {
      return;
    }

    int x = e.getX();
    int y = e.getY();

    Object ob = findElement(x, y);

    if (ob instanceof SequenceI)
    {
      treeSelectionChanged((Sequence) ob);
      PaintRefresher.Refresh(tp,
              getAssociatedPanel().av.getSequenceSetId());
      repaint();
      av.sendSelection();
      return;
    }
    else if (!(ob instanceof BinaryNode))
    {
      // Find threshold
      if (tree.getMaxHeight() != 0)
      {
        threshold = (float) (x - offx)
                / (float) (getWidth() - labelLength - (2 * offx));

        List<BinaryNode> groups = tree.groupNodes(threshold);
        setColor(tree.getTopNode(), Color.black);

        AlignmentPanel[] aps = getAssociatedPanels();

        // TODO push calls below into a single AlignViewportI method?
        // see also AlignViewController.deleteGroups
        for (int a = 0; a < aps.length; a++)
        {
          aps[a].av.setSelectionGroup(null);
          aps[a].av.getAlignment().deleteAllGroups();
          aps[a].av.clearSequenceColours();
          if (aps[a].av.getCodingComplement() != null)
          {
            aps[a].av.getCodingComplement().setSelectionGroup(null);
            aps[a].av.getCodingComplement().getAlignment()
                    .deleteAllGroups();
            aps[a].av.getCodingComplement().clearSequenceColours();
          }
          aps[a].av.setUpdateStructures(true);
        }
        colourGroups(groups);

        /*
         * clear partition (don't show vertical line) if
         * it is to the right of all nodes
         */
        if (groups.isEmpty())
        {
          threshold = 0f;
        }
      }
      Console.log.debug("Tree cut threshold set at:" + threshold);
      PaintRefresher.Refresh(tp,
              getAssociatedPanel().av.getSequenceSetId());
      repaint();
    }

  }

  void colourGroups(List<BinaryNode> groups)
  {
    AlignmentPanel[] aps = getAssociatedPanels();
    List<BitSet> colGroups = new ArrayList<>();
    Map<BitSet, Color> colors = new HashMap();
    for (int i = 0; i < groups.size(); i++)
    {
      Color col = ColorUtils.getARandomColor();
      
      setColor(groups.get(i), col.brighter());

      Vector<BinaryNode> l = tree.findLeaves(groups.get(i));
      if (!tp.isColumnWise())
      {
        createSeqGroupFor(aps, l, col);
      }
      else
      {
        BitSet gp = createColumnGroupFor(l, col);

        colGroups.add(gp);
        colors.put(gp, col);
      }
    }
    if (tp.isColumnWise())
    {
      AlignmentAnnotation aa = tp.getAssocAnnotation();
      if (aa != null)
      {
        ContactMatrixI cm = av.getContactMatrix(aa);
        if (cm != null)
        {
          cm.updateGroups(colGroups);
          for (BitSet gp : colors.keySet())
          {
            cm.setColorForGroup(gp, colors.get(gp));
          }
        }
        cm.transferGroupColorsTo(aa);
      }
    }

    // notify the panel(s) to redo any group specific stuff
    // also updates structure views if necessary
    for (int a = 0; a < aps.length; a++)
    {
      aps[a].updateAnnotation();
      final AlignViewportI codingComplement = aps[a].av
              .getCodingComplement();
      if (codingComplement != null)
      {
        ((AlignViewport) codingComplement).getAlignPanel()
                .updateAnnotation();
      }
    }
  }
  private int parseColumnNode(BinaryNode bn) throws NumberFormatException
  {
    return Integer.parseInt(
            bn.getName().substring(bn.getName().indexOf("c") + 1));
  }
  private boolean isColumnForNodeSelected(BinaryNode bn)
  {
    SequenceI rseq = tp.assocAnnotation.sequenceRef;
    int colm = -1;
    try
    {
      colm = parseColumnNode(bn);
    } catch (Exception e)
    {
      return false;
    }
    if (av == null || av.getAlignment() == null)
    {
      // alignment is closed
      return false;
    }
    ColumnSelection cs = av.getColumnSelection();
    HiddenColumns hc = av.getAlignment().getHiddenColumns();
    AlignmentAnnotation aa = tp.getAssocAnnotation();
    int offp=-1;
    if (aa != null)
    {
      ContactMatrixI cm = av.getContactMatrix(aa);
      // generally, we assume cm has 1:1 mapping to annotation row - probably wrong
      // but.. if
      if (cm instanceof MappableContactMatrixI)
      {
        int[] pos;
          // use the mappable's mapping - always the case for PAE Matrices so good
        // for 2.11.3
        MappableContactMatrixI mcm = (MappableContactMatrixI) cm;
        pos = mcm.getMappedPositionsFor(rseq, colm + 1);
        // finally, look up the position of the column
        if (pos != null)
        {
          offp = rseq.findIndex(pos[0]);
        }
      } else {
        offp = colm;
      }
    }
    if (offp<=0)
    {
      return false;
    }

    offp-=2;
    if (!av.hasHiddenColumns())
    {
      return cs.contains(offp);
    }
    if (hc.isVisible(offp))
    {
      return cs.contains(offp);
      // return cs.contains(hc.absoluteToVisibleColumn(offp));
    }
    return false;
  }
  private BitSet createColumnGroupFor(Vector<BinaryNode> l, Color col)
  {
    BitSet gp = new BitSet();
    for (BinaryNode bn : l)
    {
      int colm = -1;
      if (bn.element() != null && bn.element() instanceof Integer)
      {
        colm = (Integer) bn.element();
      }
      else
      {
        // parse out from nodename
        try
        {
          colm = parseColumnNode(bn);
        } catch (Exception e)
        {
          continue;
        }
      }
      gp.set(colm);
    }
    return gp;
  }

  private void markColumnsFor(AlignmentPanel[] aps, Vector<BinaryNode> l,
          Color col, boolean clearSelected)
  {
    SequenceI rseq = tp.assocAnnotation.sequenceRef;
    if (av == null || av.getAlignment() == null)
    {
      // alignment is closed
      return;
    }

    // TODO - sort indices for faster lookup
    ColumnSelection cs = av.getColumnSelection();
    HiddenColumns hc = av.getAlignment().getHiddenColumns();
    ContactMatrixI cm = av.getContactMatrix(tp.assocAnnotation);
    MappableContactMatrixI mcm = null;
    int offp;
    if (cm instanceof MappableContactMatrixI)
    {
      mcm = (MappableContactMatrixI) cm;
    }
    for (BinaryNode bn : l)
    {
      int colm = -1;
      try
      {
        colm = Integer.parseInt(
                bn.getName().substring(bn.getName().indexOf("c") + 1));
      } catch (Exception e)
      {
        continue;
      }
      if (mcm!=null)
      {
        int[] seqpos = mcm.getMappedPositionsFor(
                rseq, colm);
        if (seqpos == null)
        {
          // no mapping for this column.
          continue;
        }
        // TODO: handle ranges...
        offp = rseq.findIndex(seqpos[0])-1;
      }
      else
      {
        offp = (rseq != null) ? rseq.findIndex(rseq.getStart() + colm)
                : colm;
      }
      if (!av.hasHiddenColumns() || hc.isVisible(offp))
      {
        if (clearSelected || cs.contains(offp))
        {
          cs.removeElement(offp);
        }
        else
        {
          cs.addElement(offp);
        }
      }
    }
    PaintRefresher.Refresh(tp, av.getSequenceSetId());
  }

  public void createSeqGroupFor(AlignmentPanel[] aps, Vector<BinaryNode> l,
          Color col)
  {

    Vector<SequenceI> sequences = new Vector<>();

    for (int j = 0; j < l.size(); j++)
    {
      SequenceI s1 = (SequenceI) l.elementAt(j).element();

      if (!sequences.contains(s1))
      {
        sequences.addElement(s1);
      }
    }

    ColourSchemeI cs = null;
    SequenceGroup _sg = new SequenceGroup(sequences, null, cs, true, true,
            false, 0, av.getAlignment().getWidth() - 1);

    _sg.setName("JTreeGroup:" + _sg.hashCode());
    _sg.setIdColour(col);

    for (int a = 0; a < aps.length; a++)
    {
      SequenceGroup sg = new SequenceGroup(_sg);
      AlignViewport viewport = aps[a].av;

      // Propagate group colours in each view
      if (viewport.getGlobalColourScheme() != null)
      {
        cs = viewport.getGlobalColourScheme().getInstance(viewport, sg);
        sg.setColourScheme(cs);
        sg.getGroupColourScheme().setThreshold(
                viewport.getResidueShading().getThreshold(),
                viewport.isIgnoreGapsConsensus());

        if (viewport.getResidueShading().conservationApplied())
        {
          Conservation c = new Conservation("Group", sg.getSequences(null),
                  sg.getStartRes(), sg.getEndRes());
          c.calculate();
          c.verdict(false, viewport.getConsPercGaps());
          sg.cs.setConservation(c);
        }
      }
      // indicate that associated structure views will need an update
      viewport.setUpdateStructures(true);
      // propagate structure view update and sequence group to complement view
      viewport.addSequenceGroup(sg);
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param state
   *          DOCUMENT ME!
   */
  public void setShowDistances(boolean state)
  {
    this.showDistances = state;
    repaint();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param state
   *          DOCUMENT ME!
   */
  public void setShowBootstrap(boolean state)
  {
    this.showBootstrap = state;
    repaint();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param state
   *          DOCUMENT ME!
   */
  public void setMarkPlaceholders(boolean state)
  {
    this.markPlaceholders = state;
    repaint();
  }

  AlignmentPanel[] getAssociatedPanels()
  {
    if (applyToAllViews)
    {
      return PaintRefresher.getAssociatedPanels(av.getSequenceSetId());
    }
    else
    {
      return new AlignmentPanel[] { getAssociatedPanel() };
    }
  }

  public AlignmentPanel getAssociatedPanel()
  {
    return ap;
  }

  public void setAssociatedPanel(AlignmentPanel ap)
  {
    this.ap = ap;
  }

  public AlignViewport getViewport()
  {
    return av;
  }

  public void setViewport(AlignViewport av)
  {
    this.av = av;
  }

  public float getThreshold()
  {
    return threshold;
  }

  public void setThreshold(float threshold)
  {
    this.threshold = threshold;
  }

  public boolean isApplyToAllViews()
  {
    return this.applyToAllViews;
  }

  public void setApplyToAllViews(boolean applyToAllViews)
  {
    this.applyToAllViews = applyToAllViews;
  }
}
