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
package mc_view;

import jalview.analysis.AlignSeq;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignmentPanel;
import jalview.gui.FeatureRenderer;
import jalview.gui.SequenceRenderer;
import jalview.io.DataSourceType;
import jalview.io.StructureFile;
import jalview.renderer.seqfeatures.FeatureColourFinder;
import jalview.structure.AtomSpec;
import jalview.structure.StructureListener;
import jalview.structure.StructureMapping;
import jalview.structure.StructureSelectionManager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
// JBPNote TODO: This class is quite noisy - needs proper log.info/log.debug
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.PrintStream;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;

public class PDBCanvas extends JPanel
        implements MouseListener, MouseMotionListener, StructureListener
{
  boolean redrawneeded = true;

  int omx = 0;

  int mx = 0;

  int omy = 0;

  int my = 0;

  public StructureFile pdb;

  PDBEntry pdbentry;

  int bsize;

  Image img;

  Graphics ig;

  Dimension prefsize;

  float[] centre = new float[3];

  float[] width = new float[3];

  float maxwidth;

  float scale;

  String inStr;

  String inType;

  boolean bysequence = true;

  boolean depthcue = true;

  boolean wire = false;

  boolean bymolecule = false;

  boolean zbuffer = true;

  boolean dragging;

  int xstart;

  int xend;

  int ystart;

  int yend;

  int xmid;

  int ymid;

  Font font = new Font("Helvetica", Font.PLAIN, 10);

  jalview.gui.SeqCanvas seqcanvas;

  public SequenceI[] sequence;

  final StringBuffer mappingDetails = new StringBuffer();

  PDBChain mainchain;

  Vector<String> highlightRes;

  boolean pdbAction = false;

  boolean seqColoursReady = false;

  jalview.renderer.seqfeatures.FeatureRenderer fr;

  Color backgroundColour = Color.black;

  AlignmentPanel ap;

  StructureSelectionManager ssm;

  String errorMessage;

  void init(PDBEntry pdbentry, SequenceI[] seq, String[] chains,
          AlignmentPanel ap, DataSourceType protocol)
  {
    this.ap = ap;
    this.pdbentry = pdbentry;
    this.sequence = seq;

    ssm = ap.av.getStructureSelectionManager();

    try
    {
      pdb = ssm.setMapping(seq, chains, pdbentry.getFile(), protocol,
              ap.alignFrame);

      if (protocol.equals(jalview.io.DataSourceType.PASTE))
      {
        pdbentry.setFile("INLINE" + pdb.getId());
      }

    } catch (Exception ex)
    {
      ex.printStackTrace();
      return;
    }

    if (pdb == null)
    {
      errorMessage = "Error loading file: " + pdbentry.getId();
      return;
    }
    pdbentry.setId(pdb.getId());

    ssm.addStructureViewerListener(this);

    colourBySequence();

    float max = -10;
    int maxchain = -1;
    int pdbstart = 0;
    int pdbend = 0;
    int seqstart = 0;
    int seqend = 0;

    // JUST DEAL WITH ONE SEQUENCE FOR NOW
    SequenceI sequence = seq[0];

    for (int i = 0; i < pdb.getChains().size(); i++)
    {

      mappingDetails.append("\n\nPDB Sequence is :\nSequence = "
              + pdb.getChains().elementAt(i).sequence
                      .getSequenceAsString());
      mappingDetails.append("\nNo of residues = "
              + pdb.getChains().elementAt(i).residues.size() + "\n\n");

      // Now lets compare the sequences to get
      // the start and end points.
      // Align the sequence to the pdb
      AlignSeq as = new AlignSeq(sequence,
              pdb.getChains().elementAt(i).sequence, "pep");
      as.calcScoreMatrix();
      as.traceAlignment();
      PrintStream ps = new PrintStream(System.out)
      {
        @Override
        public void print(String x)
        {
          mappingDetails.append(x);
        }

        @Override
        public void println()
        {
          mappingDetails.append("\n");
        }
      };

      as.printAlignment(ps);

      if (as.maxscore > max)
      {
        max = as.maxscore;
        maxchain = i;

        pdbstart = as.seq2start;
        pdbend = as.seq2end;
        seqstart = as.seq1start + sequence.getStart() - 1;
        seqend = as.seq1end + sequence.getEnd() - 1;
      }

      mappingDetails.append("\nPDB start/end " + pdbstart + " " + pdbend);
      mappingDetails.append("\nSEQ start/end " + seqstart + " " + seqend);
    }

    mainchain = pdb.getChains().elementAt(maxchain);

    mainchain.pdbstart = pdbstart;
    mainchain.pdbend = pdbend;
    mainchain.seqstart = seqstart;
    mainchain.seqend = seqend;
    mainchain.isVisible = true;

    this.pdb = pdb;
    this.prefsize = new Dimension(getSize().width, getSize().height);

    addMouseMotionListener(this);
    addMouseListener(this);

    addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent evt)
      {
        keyPressed(evt);
      }
    });

    findCentre();
    findWidth();

    setupBonds();

    scale = findScale();

    ToolTipManager.sharedInstance().registerComponent(this);
    ToolTipManager.sharedInstance().setInitialDelay(0);
    ToolTipManager.sharedInstance().setDismissDelay(10000);
  }

  Vector<Bond> visiblebonds;

  void setupBonds()
  {
    seqColoursReady = false;
    // Sort the bonds by z coord
    visiblebonds = new Vector<Bond>();

    for (PDBChain chain : pdb.getChains())
    {
      if (chain.isVisible)
      {
        for (Bond bond : chain.bonds)
        {
          visiblebonds.addElement(bond);
        }
      }
    }

    updateSeqColours();
    seqColoursReady = true;
    redrawneeded = true;
    repaint();
  }

  public void findWidth()
  {
    float[] max = new float[3];
    float[] min = new float[3];

    max[0] = (float) -1e30;
    max[1] = (float) -1e30;
    max[2] = (float) -1e30;

    min[0] = (float) 1e30;
    min[1] = (float) 1e30;
    min[2] = (float) 1e30;

    for (PDBChain chain : pdb.getChains())
    {
      if (chain.isVisible)
      {
        for (Bond tmp : chain.bonds)
        {
          if (tmp.start[0] >= max[0])
          {
            max[0] = tmp.start[0];
          }

          if (tmp.start[1] >= max[1])
          {
            max[1] = tmp.start[1];
          }

          if (tmp.start[2] >= max[2])
          {
            max[2] = tmp.start[2];
          }

          if (tmp.start[0] <= min[0])
          {
            min[0] = tmp.start[0];
          }

          if (tmp.start[1] <= min[1])
          {
            min[1] = tmp.start[1];
          }

          if (tmp.start[2] <= min[2])
          {
            min[2] = tmp.start[2];
          }

          if (tmp.end[0] >= max[0])
          {
            max[0] = tmp.end[0];
          }

          if (tmp.end[1] >= max[1])
          {
            max[1] = tmp.end[1];
          }

          if (tmp.end[2] >= max[2])
          {
            max[2] = tmp.end[2];
          }

          if (tmp.end[0] <= min[0])
          {
            min[0] = tmp.end[0];
          }

          if (tmp.end[1] <= min[1])
          {
            min[1] = tmp.end[1];
          }

          if (tmp.end[2] <= min[2])
          {
            min[2] = tmp.end[2];
          }
        }
      }
    }
    /*
     * System.out.println("xmax " + max[0] + " min " + min[0]);
     * System.out.println("ymax " + max[1] + " min " + min[1]);
     * System.out.println("zmax " + max[2] + " min " + min[2]);
     */

    width[0] = Math.abs(max[0] - min[0]);
    width[1] = Math.abs(max[1] - min[1]);
    width[2] = Math.abs(max[2] - min[2]);

    maxwidth = width[0];

    if (width[1] > width[0])
    {
      maxwidth = width[1];
    }

    if (width[2] > width[1])
    {
      maxwidth = width[2];
    }

    // System.out.println("Maxwidth = " + maxwidth);
  }

  public float findScale()
  {
    int dim;
    int width;
    int height;

    if (getWidth() != 0)
    {
      width = getWidth();
      height = getHeight();
    }
    else
    {
      width = prefsize.width;
      height = prefsize.height;
    }

    if (width < height)
    {
      dim = width;
    }
    else
    {
      dim = height;
    }

    return (float) (dim / (1.5d * maxwidth));
  }

  public void findCentre()
  {
    float xtot = 0;
    float ytot = 0;
    float ztot = 0;

    int bsize = 0;

    // Find centre coordinate
    for (PDBChain chain : pdb.getChains())
    {
      if (chain.isVisible)
      {
        bsize += chain.bonds.size();

        for (Bond bond : chain.bonds)
        {
          xtot = xtot + bond.start[0] + bond.end[0];
          ytot = ytot + bond.start[1] + bond.end[1];
          ztot = ztot + bond.start[2] + bond.end[2];
        }
      }
    }

    centre[0] = xtot / (2 * (float) bsize);
    centre[1] = ytot / (2 * (float) bsize);
    centre[2] = ztot / (2 * (float) bsize);
  }

  @Override
  public void paintComponent(Graphics g)
  {
    super.paintComponent(g);

    if (!seqColoursReady || errorMessage != null)
    {
      g.setColor(Color.black);
      g.setFont(new Font("Verdana", Font.BOLD, 14));
      g.drawString(errorMessage == null ? "Retrieving PDB data...."
              : errorMessage, 20, getHeight() / 2);
      return;
    }

    // Only create the image at the beginning -
    // this saves much memory usage
    if ((img == null) || (prefsize.width != getWidth())
            || (prefsize.height != getHeight()))

    {
      prefsize.width = getWidth();
      prefsize.height = getHeight();

      scale = findScale();
      img = createImage(prefsize.width, prefsize.height);
      ig = img.getGraphics();
      Graphics2D ig2 = (Graphics2D) ig;

      ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);

      redrawneeded = true;
    }

    if (redrawneeded)
    {
      drawAll(ig, prefsize.width, prefsize.height);
      redrawneeded = false;
    }

    g.drawImage(img, 0, 0, this);

    pdbAction = false;
  }

  public void drawAll(Graphics g, int width, int height)
  {
    g.setColor(backgroundColour);
    g.fillRect(0, 0, width, height);
    drawScene(g);
    drawLabels(g);
  }

  public void updateSeqColours()
  {
    if (pdbAction)
    {
      return;
    }

    colourBySequence();

    redrawneeded = true;
    repaint();
  }

  // This method has been taken out of PDBChain to allow
  // Applet and Application specific sequence renderers to be used
  void colourBySequence()
  {
    SequenceRenderer sr = new SequenceRenderer(ap.av);

    StructureMapping[] mapping = ssm.getMapping(pdbentry.getFile());

    boolean showFeatures = false;
    if (ap.av.isShowSequenceFeatures())
    {
      if (fr == null)
      {
        fr = new FeatureRenderer(ap);
      }

      fr.transferSettings(ap.alignFrame.getFeatureRenderer());

      showFeatures = true;
    }

    FeatureColourFinder finder = new FeatureColourFinder(fr);
    PDBChain chain;
    if (bysequence && pdb != null)
    {
      for (int ii = 0; ii < pdb.getChains().size(); ii++)
      {
        chain = pdb.getChains().elementAt(ii);

        for (int i = 0; i < chain.bonds.size(); i++)
        {
          Bond tmp = chain.bonds.elementAt(i);
          tmp.startCol = Color.lightGray;
          tmp.endCol = Color.lightGray;
          if (chain != mainchain)
          {
            continue;
          }

          for (int s = 0; s < sequence.length; s++)
          {
            for (int m = 0; m < mapping.length; m++)
            {
              if (mapping[m].getSequence() == sequence[s])
              {
                int pos = mapping[m].getSeqPos(tmp.at1.resNumber) - 1;
                if (pos > 0)
                {
                  pos = sequence[s].findIndex(pos);
                  tmp.startCol = sr.getResidueColour(sequence[s], pos,
                          finder);
                }
                pos = mapping[m].getSeqPos(tmp.at2.resNumber) - 1;
                if (pos > 0)
                {
                  pos = sequence[s].findIndex(pos);
                  tmp.endCol = sr.getResidueColour(sequence[s], pos,
                          finder);
                }

              }
            }
          }
        }
      }
    }
  }

  Zsort zsort;

  public void drawScene(Graphics g)
  {
    if (zbuffer)
    {
      if (zsort == null)
      {
        zsort = new Zsort();
      }

      zsort.sort(visiblebonds);
    }

    Bond tmpBond = null;
    for (int i = 0; i < visiblebonds.size(); i++)
    {
      tmpBond = visiblebonds.elementAt(i);

      xstart = (int) (((tmpBond.start[0] - centre[0]) * scale)
              + (getWidth() / 2));
      ystart = (int) (((centre[1] - tmpBond.start[1]) * scale)
              + (getHeight() / 2));

      xend = (int) (((tmpBond.end[0] - centre[0]) * scale)
              + (getWidth() / 2));
      yend = (int) (((centre[1] - tmpBond.end[1]) * scale)
              + (getHeight() / 2));

      xmid = (xend + xstart) / 2;
      ymid = (yend + ystart) / 2;
      if (depthcue && !bymolecule)
      {
        if (tmpBond.start[2] < (centre[2] - (maxwidth / 6)))
        {

          g.setColor(tmpBond.startCol.darker().darker());
          drawLine(g, xstart, ystart, xmid, ymid);
          g.setColor(tmpBond.endCol.darker().darker());
          drawLine(g, xmid, ymid, xend, yend);

        }
        else if (tmpBond.start[2] < (centre[2] + (maxwidth / 6)))
        {
          g.setColor(tmpBond.startCol.darker());
          drawLine(g, xstart, ystart, xmid, ymid);

          g.setColor(tmpBond.endCol.darker());
          drawLine(g, xmid, ymid, xend, yend);
        }
        else
        {
          g.setColor(tmpBond.startCol);
          drawLine(g, xstart, ystart, xmid, ymid);

          g.setColor(tmpBond.endCol);
          drawLine(g, xmid, ymid, xend, yend);
        }
      }
      else if (depthcue && bymolecule)
      {
        if (tmpBond.start[2] < (centre[2] - (maxwidth / 6)))
        {
          g.setColor(Color.green.darker().darker());
          drawLine(g, xstart, ystart, xend, yend);
        }
        else if (tmpBond.start[2] < (centre[2] + (maxwidth / 6)))
        {
          g.setColor(Color.green.darker());
          drawLine(g, xstart, ystart, xend, yend);
        }
        else
        {
          g.setColor(Color.green);
          drawLine(g, xstart, ystart, xend, yend);
        }
      }
      else if (!depthcue && !bymolecule)
      {
        g.setColor(tmpBond.startCol);
        drawLine(g, xstart, ystart, xmid, ymid);
        g.setColor(tmpBond.endCol);
        drawLine(g, xmid, ymid, xend, yend);
      }
      else
      {
        drawLine(g, xstart, ystart, xend, yend);
      }

      if (highlightBond1 != null && highlightBond1 == tmpBond)
      {
        g.setColor(
                tmpBond.endCol.brighter().brighter().brighter().brighter());
        drawLine(g, xmid, ymid, xend, yend);
      }

      if (highlightBond2 != null && highlightBond2 == tmpBond)
      {
        g.setColor(tmpBond.startCol.brighter().brighter().brighter()
                .brighter());
        drawLine(g, xstart, ystart, xmid, ymid);
      }

    }

  }

  public void drawLine(Graphics g, int x1, int y1, int x2, int y2)
  {
    if (!wire)
    {
      if (((float) Math.abs(y2 - y1) / (float) Math.abs(x2 - x1)) < 0.5)
      {
        g.drawLine(x1, y1, x2, y2);
        g.drawLine(x1 + 1, y1 + 1, x2 + 1, y2 + 1);
        g.drawLine(x1, y1 - 1, x2, y2 - 1);
      }
      else
      {
        g.setColor(g.getColor().brighter());
        g.drawLine(x1, y1, x2, y2);
        g.drawLine(x1 + 1, y1, x2 + 1, y2);
        g.drawLine(x1 - 1, y1, x2 - 1, y2);
      }
    }
    else
    {
      g.drawLine(x1, y1, x2, y2);
    }
  }

  public Dimension minimumsize()
  {
    return prefsize;
  }

  public Dimension preferredsize()
  {
    return prefsize;
  }

  public void keyPressed(KeyEvent evt)
  {
    if (evt.getKeyCode() == KeyEvent.VK_UP)
    {
      scale = (float) (scale * 1.1);
      redrawneeded = true;
      repaint();
    }
    else if (evt.getKeyCode() == KeyEvent.VK_DOWN)
    {
      scale = (float) (scale * 0.9);
      redrawneeded = true;
      repaint();
    }
  }

  @Override
  public void mousePressed(MouseEvent e)
  {
    pdbAction = true;
    Atom fatom = findAtom(e.getX(), e.getY());
    if (fatom != null)
    {
      fatom.isSelected = !fatom.isSelected;

      redrawneeded = true;
      repaint();
      if (foundchain != -1)
      {
        PDBChain chain = pdb.getChains().elementAt(foundchain);
        if (chain == mainchain)
        {
          if (fatom.alignmentMapping != -1)
          {
            if (highlightRes == null)
            {
              highlightRes = new Vector<String>();
            }

            final String atomString = Integer
                    .toString(fatom.alignmentMapping);
            if (highlightRes.contains(atomString))
            {
              highlightRes.remove(atomString);
            }
            else
            {
              highlightRes.add(atomString);
            }
          }
        }
      }

    }
    mx = e.getX();
    my = e.getY();
    omx = mx;
    omy = my;
    dragging = false;
  }

  @Override
  public void mouseMoved(MouseEvent e)
  {
    pdbAction = true;
    if (highlightBond1 != null)
    {
      highlightBond1.at2.isSelected = false;
      highlightBond2.at1.isSelected = false;
      highlightBond1 = null;
      highlightBond2 = null;
    }

    Atom fatom = findAtom(e.getX(), e.getY());

    PDBChain chain = null;
    if (foundchain != -1)
    {
      chain = pdb.getChains().elementAt(foundchain);
      if (chain == mainchain)
      {
        mouseOverStructure(fatom.resNumber, chain.id);
      }
    }

    if (fatom != null)
    {
      this.setToolTipText(
              chain.id + ":" + fatom.resNumber + " " + fatom.resName);
    }
    else
    {
      mouseOverStructure(-1, chain != null ? chain.id : null);
      this.setToolTipText(null);
    }
  }

  @Override
  public void mouseClicked(MouseEvent e)
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
  public void mouseDragged(MouseEvent evt)
  {
    int x = evt.getX();
    int y = evt.getY();
    mx = x;
    my = y;

    MCMatrix objmat = new MCMatrix(3, 3);
    objmat.setIdentity();

    if ((evt.getModifiers() & Event.META_MASK) != 0)
    {
      objmat.rotatez(((mx - omx)));
    }
    else
    {
      objmat.rotatex(((my - omy)));
      objmat.rotatey(((omx - mx)));
    }

    // Alter the bonds
    for (PDBChain chain : pdb.getChains())
    {
      for (Bond tmpBond : chain.bonds)
      {
        // Translate the bond so the centre is 0,0,0
        tmpBond.translate(-centre[0], -centre[1], -centre[2]);

        // Now apply the rotation matrix
        tmpBond.start = objmat.vectorMultiply(tmpBond.start);
        tmpBond.end = objmat.vectorMultiply(tmpBond.end);

        // Now translate back again
        tmpBond.translate(centre[0], centre[1], centre[2]);
      }
    }

    objmat = null;

    omx = mx;
    omy = my;

    dragging = true;

    redrawneeded = true;

    repaint();
  }

  @Override
  public void mouseReleased(MouseEvent evt)
  {
    dragging = false;
    return;
  }

  void drawLabels(Graphics g)
  {

    for (PDBChain chain : pdb.getChains())
    {
      if (chain.isVisible)
      {
        for (Bond tmpBond : chain.bonds)
        {
          if (tmpBond.at1.isSelected)
          {
            labelAtom(g, tmpBond, 1);
          }

          if (tmpBond.at2.isSelected)
          {
            labelAtom(g, tmpBond, 2);
          }
        }
      }
    }
  }

  public void labelAtom(Graphics g, Bond b, int n)
  {
    g.setFont(font);
    g.setColor(Color.red);
    if (n == 1)
    {
      int xstart = (int) (((b.start[0] - centre[0]) * scale)
              + (getWidth() / 2));
      int ystart = (int) (((centre[1] - b.start[1]) * scale)
              + (getHeight() / 2));

      g.drawString(b.at1.resName + "-" + b.at1.resNumber, xstart, ystart);
    }

    if (n == 2)
    {
      int xstart = (int) (((b.end[0] - centre[0]) * scale)
              + (getWidth() / 2));
      int ystart = (int) (((centre[1] - b.end[1]) * scale)
              + (getHeight() / 2));

      g.drawString(b.at2.resName + "-" + b.at2.resNumber, xstart, ystart);
    }
  }

  int foundchain = -1;

  public Atom findAtom(int x, int y)
  {
    Atom fatom = null;

    foundchain = -1;

    for (int ii = 0; ii < pdb.getChains().size(); ii++)
    {
      PDBChain chain = pdb.getChains().elementAt(ii);
      int truex;
      Bond tmpBond = null;

      if (chain.isVisible)
      {
        for (Bond bond : chain.bonds)
        {
          tmpBond = bond;

          truex = (int) (((tmpBond.start[0] - centre[0]) * scale)
                  + (getWidth() / 2));

          if (Math.abs(truex - x) <= 2)
          {
            int truey = (int) (((centre[1] - tmpBond.start[1]) * scale)
                    + (getHeight() / 2));

            if (Math.abs(truey - y) <= 2)
            {
              fatom = tmpBond.at1;
              foundchain = ii;
              break;
            }
          }
        }

        // Still here? Maybe its the last bond

        truex = (int) (((tmpBond.end[0] - centre[0]) * scale)
                + (getWidth() / 2));

        if (Math.abs(truex - x) <= 2)
        {
          int truey = (int) (((tmpBond.end[1] - centre[1]) * scale)
                  + (getHeight() / 2));

          if (Math.abs(truey - y) <= 2)
          {
            fatom = tmpBond.at2;
            foundchain = ii;
            break;
          }
        }

      }

      if (fatom != null) // )&& chain.ds != null)
      { // dead code? value of chain is either overwritten or discarded
        chain = pdb.getChains().elementAt(foundchain);
      }
    }

    return fatom;
  }

  Bond highlightBond1, highlightBond2;

  public void highlightRes(int ii)
  {
    if (!seqColoursReady)
    {
      return;
    }

    if (highlightRes != null && highlightRes.contains((ii - 1) + ""))
    {
      return;
    }

    int index = -1;
    Bond tmpBond;
    for (index = 0; index < mainchain.bonds.size(); index++)
    {
      tmpBond = mainchain.bonds.elementAt(index);
      if (tmpBond.at1.alignmentMapping == ii - 1)
      {
        if (highlightBond1 != null)
        {
          highlightBond1.at2.isSelected = false;
        }

        if (highlightBond2 != null)
        {
          highlightBond2.at1.isSelected = false;
        }

        highlightBond1 = null;
        highlightBond2 = null;

        if (index > 0)
        {
          highlightBond1 = mainchain.bonds.elementAt(index - 1);
          highlightBond1.at2.isSelected = true;
        }

        if (index != mainchain.bonds.size())
        {
          highlightBond2 = mainchain.bonds.elementAt(index);
          highlightBond2.at1.isSelected = true;
        }

        break;
      }
    }

    redrawneeded = true;
    repaint();
  }

  public void setAllchainsVisible(boolean b)
  {
    for (int ii = 0; ii < pdb.getChains().size(); ii++)
    {
      PDBChain chain = pdb.getChains().elementAt(ii);
      chain.isVisible = b;
    }
    mainchain.isVisible = true;
    findCentre();
    setupBonds();
  }

  // ////////////////////////////////
  // /StructureListener
  @Override
  public String[] getStructureFiles()
  {
    return new String[] { pdbentry.getFile() };
  }

  String lastMessage;

  public void mouseOverStructure(int pdbResNum, String chain)
  {
    if (lastMessage == null || !lastMessage.equals(pdbResNum + chain))
    {
      ssm.mouseOverStructure(pdbResNum, chain, pdbentry.getFile());
    }

    lastMessage = pdbResNum + chain;
  }

  StringBuffer resetLastRes = new StringBuffer();

  StringBuffer eval = new StringBuffer();

  /**
   * Highlight the specified atoms in the structure.
   * 
   * @param atoms
   */
  @Override
  public void highlightAtoms(List<AtomSpec> atoms)
  {
    if (!seqColoursReady)
    {
      return;
    }

    for (AtomSpec atom : atoms)
    {
      int atomIndex = atom.getAtomIndex();
      if (highlightRes != null
              && highlightRes.contains((atomIndex - 1) + ""))
      {
        continue;
      }

      highlightAtom(atomIndex);
    }

    redrawneeded = true;
    repaint();
  }

  /**
   * Highlight the atom at the specified index.
   * 
   * @param atomIndex
   */
  protected void highlightAtom(int atomIndex)
  {
    int index = -1;
    Bond tmpBond;
    for (index = 0; index < mainchain.bonds.size(); index++)
    {
      tmpBond = mainchain.bonds.elementAt(index);
      if (tmpBond.at1.atomIndex == atomIndex)
      {
        if (highlightBond1 != null)
        {
          highlightBond1.at2.isSelected = false;
        }

        if (highlightBond2 != null)
        {
          highlightBond2.at1.isSelected = false;
        }

        highlightBond1 = null;
        highlightBond2 = null;

        if (index > 0)
        {
          highlightBond1 = mainchain.bonds.elementAt(index - 1);
          highlightBond1.at2.isSelected = true;
        }

        if (index != mainchain.bonds.size())
        {
          highlightBond2 = mainchain.bonds.elementAt(index);
          highlightBond2.at1.isSelected = true;
        }

        break;
      }
    }
  }

  public Color getColour(int atomIndex, int pdbResNum, String chain,
          String pdbfile)
  {
    return Color.white;
    // if (!pdbfile.equals(pdbentry.getFile()))
    // return null;

    // return new Color(viewer.getAtomArgb(atomIndex));
  }

  @Override
  public void updateColours(Object source)
  {
    colourBySequence();
    redrawneeded = true;
    repaint();
  }

  @Override
  public void releaseReferences(Object svl)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isListeningFor(SequenceI seq)
  {
    if (sequence != null)
    {
      for (SequenceI s : sequence)
      {
        if (s == seq)
        {
          return true;
        }
      }
    }
    return false;
  }
}
