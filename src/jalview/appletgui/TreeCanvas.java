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

import jalview.analysis.Conservation;
import jalview.analysis.TreeModel;
import jalview.api.AlignViewportI;
import jalview.datamodel.BinaryNode;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.datamodel.SequenceNode;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ColourSchemeProperty;
import jalview.schemes.UserColourScheme;
import jalview.util.Format;
import jalview.util.MappingUtils;
import jalview.viewmodel.AlignmentViewport;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

public class TreeCanvas extends Panel
        implements MouseListener, MouseMotionListener
{
  TreeModel tree;

  ScrollPane scrollPane;

  AlignViewport av;

  public static final String PLACEHOLDER = " * ";

  Font font;

  boolean fitToWindow = true;

  boolean showDistances = false;

  boolean showBootstrap = false;

  boolean markPlaceholders = false;

  int offx = 20;

  int offy;

  float threshold;

  String longestName;

  int labelLength = -1;

  Hashtable nameHash = new Hashtable();

  Hashtable nodeHash = new Hashtable();

  BinaryNode highlightNode;

  AlignmentPanel ap;

  public TreeCanvas(AlignmentPanel ap, ScrollPane scroller)
  {
    this.ap = ap;
    this.av = ap.av;
    font = av.getFont();
    scrollPane = scroller;
    addMouseListener(this);
    addMouseMotionListener(this);
    setLayout(null);

    PaintRefresher.Register(this, av.getSequenceSetId());
  }

  public void treeSelectionChanged(SequenceI sequence)
  {
    SequenceGroup selected = av.getSelectionGroup();
    if (selected == null)
    {
      selected = new SequenceGroup();
      av.setSelectionGroup(selected);
    }

    selected.setEndRes(av.getAlignment().getWidth() - 1);
    selected.addOrRemove(sequence, true);
  }

  public void setTree(TreeModel tree2)
  {
    this.tree = tree2;
    tree2.findHeight(tree2.getTopNode());

    // Now have to calculate longest name based on the leaves
    Vector<BinaryNode> leaves = tree2.findLeaves(tree2.getTopNode());
    boolean has_placeholders = false;
    longestName = "";

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
    }

    setMarkPlaceholders(has_placeholders);
  }

  public void drawNode(Graphics g, BinaryNode node, float chunk,
          double scale, int width, int offx, int offy)
  {
    if (node == null)
    {
      return;
    }

    if (node.left() == null && node.right() == null)
    {
      // Drawing leaf node

      double height = node.height;
      double dist = node.dist;

      int xstart = (int) ((height - dist) * scale) + offx;
      int xend = (int) (height * scale) + offx;

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
      if (showDistances && node.dist > 0)
      {
        nodeLabel = new Format("%-.2f").form(node.dist);
      }
      if (showBootstrap)
      {
        int btstrap = node.getBootstrap();
        if (btstrap > -1)
        {
          if (showDistances)
          {
            nodeLabel = nodeLabel + " : ";
          }
          nodeLabel = nodeLabel + String.valueOf(node.getBootstrap());
        }
      }
      if (!nodeLabel.equals(""))
      {
        g.drawString(nodeLabel, xstart + 2, ypos - 2);
      }

      String name = (markPlaceholders && node instanceof SequenceNode
              && ((SequenceNode) node).isPlaceholder())
                      ? (PLACEHOLDER + node.getName())
                      : node.getName();
      FontMetrics fm = g.getFontMetrics(font);
      int charWidth = fm.stringWidth(name) + 3;
      int charHeight = fm.getHeight();

      Rectangle rect = new Rectangle(xend + 10, ypos - charHeight,
              charWidth, charHeight);

      nameHash.put(node.element(), rect);

      // Colour selected leaves differently
      SequenceGroup selected = av.getSelectionGroup();
      if (selected != null
              && selected.getSequences(null).contains(node.element()))
      {
        g.setColor(Color.gray);

        g.fillRect(xend + 10, ypos - charHeight + 3, charWidth, charHeight);
        g.setColor(Color.white);
      }
      g.drawString(name, xend + 10, ypos);
      g.setColor(Color.black);
    }
    else
    {
      drawNode(g, (BinaryNode) node.left(), chunk, scale, width, offx,
              offy);
      drawNode(g, (BinaryNode) node.right(), chunk, scale, width, offx,
              offy);

      double height = node.height;
      double dist = node.dist;

      int xstart = (int) ((height - dist) * scale) + offx;
      int xend = (int) (height * scale) + offx;
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

      int ystart = (int) (node.left() == null ? 0
              : (((BinaryNode) node.left()).ycount * chunk)) + offy;
      int yend = (int) (node.right() == null ? 0
              : (((BinaryNode) node.right()).ycount * chunk)) + offy;

      Rectangle pos = new Rectangle(xend - 2, ypos - 2, 5, 5);
      nodeHash.put(node, pos);

      g.drawLine((int) (height * scale) + offx, ystart,
              (int) (height * scale) + offx, yend);

      String nodeLabel = "";

      if (showDistances && (node.dist > 0))
      {
        nodeLabel = new Format("%-.2f").form(node.dist);
      }

      if (showBootstrap)
      {
        int btstrap = node.getBootstrap();
        if (btstrap > -1)
        {
          if (showDistances)
          {
            nodeLabel = nodeLabel + " : ";
          }
          nodeLabel = nodeLabel + String.valueOf(node.getBootstrap());
        }
      }

      if (!nodeLabel.equals(""))
      {
        g.drawString(nodeLabel, xstart + 2, ypos - 2);
      }

    }
  }

  public Object findElement(int x, int y)
  {
    Enumeration keys = nameHash.keys();

    while (keys.hasMoreElements())
    {
      Object ob = keys.nextElement();
      Rectangle rect = (Rectangle) nameHash.get(ob);

      if (x >= rect.x && x <= (rect.x + rect.width) && y >= rect.y
              && y <= (rect.y + rect.height))
      {
        return ob;
      }
    }
    keys = nodeHash.keys();

    while (keys.hasMoreElements())
    {
      Object ob = keys.nextElement();
      Rectangle rect = (Rectangle) nodeHash.get(ob);

      if (x >= rect.x && x <= (rect.x + rect.width) && y >= rect.y
              && y <= (rect.y + rect.height))
      {
        return ob;
      }
    }
    return null;

  }

  public void pickNodes(Rectangle pickBox)
  {
    int width = getSize().width;
    int height = getSize().height;

    BinaryNode top = tree.getTopNode();

    double wscale = (float) (width * .8 - offx * 2) / tree.getMaxHeight();
    if (top.count == 0)
    {
      top.count = ((BinaryNode) top.left()).count
              + ((BinaryNode) top.right()).count;
    }
    float chunk = (float) (height - offy) / top.count;

    pickNode(pickBox, top, chunk, wscale, width, offx, offy);
  }

  public void pickNode(Rectangle pickBox, BinaryNode node, float chunk,
          double scale, int width, int offx, int offy)
  {
    if (node == null)
    {
      return;
    }

    if (node.left() == null && node.right() == null)
    {
      double height = node.height;
      // float dist = node.dist;

      // int xstart = (int) ( (height - dist) * scale) + offx;
      int xend = (int) (height * scale) + offx;

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
      pickNode(pickBox, (BinaryNode) node.left(), chunk, scale, width, offx,
              offy);
      pickNode(pickBox, (BinaryNode) node.right(), chunk, scale, width,
              offx, offy);
    }
  }

  public void setColor(BinaryNode node, Color c)
  {
    if (node == null)
    {
      return;
    }

    if (node.left() == null && node.right() == null)
    {
      node.color = c;

      if (node.element() instanceof SequenceI)
      {
        av.setSequenceColour((SequenceI) node.element(), c);
      }
    }
    else
    {
      node.color = c;
      setColor((BinaryNode) node.left(), c);
      setColor((BinaryNode) node.right(), c);
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
    if (tree == null)
    {
      return;
    }

    if (nameHash.size() == 0)
    {
      repaint();
    }

    int width = scrollPane.getSize().width;
    int height = scrollPane.getSize().height;
    if (!fitToWindow)
    {
      height = g.getFontMetrics(font).getHeight() * nameHash.size();
    }

    if (getSize().width > width)
    {
      setSize(new Dimension(width, height));
      scrollPane.validate();
      return;
    }

    setSize(new Dimension(width, height));

    g.setFont(font);
    draw(g, width, height);
    validate();
  }

  public void draw(Graphics g, int width, int height)
  {
    offy = font.getSize() + 10;

    g.setColor(Color.white);
    g.fillRect(0, 0, width, height);

    labelLength = g.getFontMetrics(font).stringWidth(longestName) + 20; // 20
    // allows
    // for
    // scrollbar

    double wscale = (width - labelLength - offx * 2) / tree.getMaxHeight();

    BinaryNode top = tree.getTopNode();

    if (top.count == 0)
    {
      top.count = ((BinaryNode) top.left()).count
              + ((BinaryNode) top.right()).count;
    }
    float chunk = (float) (height - offy) / top.count;

    drawNode(g, tree.getTopNode(), chunk, wscale, width, offx, offy);

    if (threshold != 0)
    {
      if (av.getCurrentTree() == tree)
      {
        g.setColor(Color.red);
      }
      else
      {
        g.setColor(Color.gray);
      }

      int x = (int) (threshold * (getSize().width - labelLength - 2 * offx)
              + offx);

      g.drawLine(x, 0, x, getSize().height);
    }

  }

  @Override
  public void mouseReleased(MouseEvent e)
  {
  }

  @Override
  public void mouseEntered(MouseEvent e)
  {
  }

  @Override
  public void mouseExited(MouseEvent e)
  {
  }

  @Override
  public void mouseClicked(MouseEvent evt)
  {
    if (highlightNode != null)
    {
      if (evt.getClickCount() > 1)
      {
        tree.swapNodes(highlightNode);
        tree.reCount(tree.getTopNode());
        tree.findHeight(tree.getTopNode());
      }
      else
      {
        Vector<BinaryNode> leaves = tree.findLeaves(highlightNode);

        for (int i = 0; i < leaves.size(); i++)
        {
          SequenceI seq = (SequenceI) leaves.elementAt(i).element();
          treeSelectionChanged(seq);
        }
      }

      PaintRefresher.Refresh(this, av.getSequenceSetId());
      repaint();
      av.sendSelection();
    }
  }

  @Override
  public void mouseDragged(MouseEvent ect)
  {
  }

  @Override
  public void mouseMoved(MouseEvent evt)
  {
    av.setCurrentTree(tree);

    Object ob = findElement(evt.getX(), evt.getY());

    if (ob instanceof BinaryNode)
    {
      highlightNode = (BinaryNode) ob;
      repaint();
    }
    else
    {
      if (highlightNode != null)
      {
        highlightNode = null;
        repaint();
      }
    }
  }

  @Override
  public void mousePressed(MouseEvent e)
  {
    av.setCurrentTree(tree);

    int x = e.getX();
    int y = e.getY();

    Object ob = findElement(x, y);

    if (ob instanceof SequenceI)
    {
      treeSelectionChanged((Sequence) ob);
      PaintRefresher.Refresh(this, av.getSequenceSetId());
      repaint();
      av.sendSelection();
      return;
    }
    else if (!(ob instanceof SequenceNode))
    {
      // Find threshold

      if (tree.getMaxHeight() != 0)
      {
        threshold = (float) (x - offx)
                / (float) (getSize().width - labelLength - 2 * offx);

        List<BinaryNode> groups = tree.groupNodes(threshold);
        setColor(tree.getTopNode(), Color.black);

        av.setSelectionGroup(null);
        av.getAlignment().deleteAllGroups();
        av.clearSequenceColours();
        final AlignViewportI codingComplement = av.getCodingComplement();
        if (codingComplement != null)
        {
          codingComplement.setSelectionGroup(null);
          codingComplement.getAlignment().deleteAllGroups();
          codingComplement.clearSequenceColours();
        }

        colourGroups(groups);

      }
    }

    PaintRefresher.Refresh(this, av.getSequenceSetId());
    repaint();

  }

  void colourGroups(List<BinaryNode> groups)
  {
    for (int i = 0; i < groups.size(); i++)
    {

      Color col = new Color((int) (Math.random() * 255),
              (int) (Math.random() * 255), (int) (Math.random() * 255));
      setColor(groups.get(i), col.brighter());

      Vector<BinaryNode> l = tree.findLeaves(groups.get(i));

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

      SequenceGroup sg = new SequenceGroup(sequences, "", cs, true, true,
              false, 0, av.getAlignment().getWidth() - 1);

      if (av.getGlobalColourScheme() != null)
      {
        if (av.getGlobalColourScheme() instanceof UserColourScheme)
        {
          cs = new UserColourScheme(
                  ((UserColourScheme) av.getGlobalColourScheme())
                          .getColours());

        }
        else
        {
          cs = ColourSchemeProperty.getColourScheme(av, sg,
                  ColourSchemeProperty
                          .getColourName(av.getGlobalColourScheme()));
        }
        // cs is null if shading is an annotationColourGradient
        // if (cs != null)
        // {
        // cs.setThreshold(av.getViewportColourScheme().getThreshold(),
        // av.isIgnoreGapsConsensus());
        // }
      }
      // TODO: cs used to be initialized with a sequence collection and
      // recalcConservation called automatically
      // instead we set it manually - recalc called after updateAnnotation
      sg.setColourScheme(cs);
      sg.getGroupColourScheme().setThreshold(
              av.getResidueShading().getThreshold(),
              av.isIgnoreGapsConsensus());

      sg.setName("JTreeGroup:" + sg.hashCode());
      sg.setIdColour(col);
      if (av.getGlobalColourScheme() != null
              && av.getResidueShading().conservationApplied())
      {
        Conservation c = new Conservation("Group", sg.getSequences(null),
                sg.getStartRes(), sg.getEndRes());

        c.calculate();
        c.verdict(false, av.getConsPercGaps());

        sg.setColourScheme(cs);
        sg.getGroupColourScheme().setConservation(c);
      }

      av.getAlignment().addGroup(sg);

      // TODO this is duplicated with gui TreeCanvas - refactor
      av.getAlignment().addGroup(sg);
      final AlignViewportI codingComplement = av.getCodingComplement();
      if (codingComplement != null)
      {
        SequenceGroup mappedGroup = MappingUtils.mapSequenceGroup(sg, av,
                codingComplement);
        if (mappedGroup.getSequences().size() > 0)
        {
          codingComplement.getAlignment().addGroup(mappedGroup);
          for (SequenceI seq : mappedGroup.getSequences())
          {
            // TODO why does gui require col.brighter() here??
            codingComplement.setSequenceColour(seq, col);
          }
        }
      }

    }
    ap.updateAnnotation();
    if (av.getCodingComplement() != null)
    {
      ((AlignmentViewport) av.getCodingComplement()).firePropertyChange(
              "alignment", null, ap.av.getAlignment().getSequences());
    }
  }

  public void setShowDistances(boolean state)
  {
    this.showDistances = state;
    repaint();
  }

  public void setShowBootstrap(boolean state)
  {
    this.showBootstrap = state;
    repaint();
  }

  public void setMarkPlaceholders(boolean state)
  {
    this.markPlaceholders = state;
    repaint();
  }

}
