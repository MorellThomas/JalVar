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
package jalview.jbgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

@SuppressWarnings("serial")
public class GAlignmentPanel extends JPanel
{
  protected JScrollBar vscroll = new JScrollBar();

  protected JScrollBar hscroll = new JScrollBar();

  BorderLayout borderLayout1 = new BorderLayout();

  BorderLayout borderLayout3 = new BorderLayout();

  BorderLayout borderLayout5 = new BorderLayout();

  BorderLayout borderLayout6 = new BorderLayout();

  ButtonGroup buttonGroup1 = new ButtonGroup();

  BorderLayout borderLayout7 = new BorderLayout();

  BorderLayout borderLayout10 = new BorderLayout();

  BorderLayout borderLayout11 = new BorderLayout();

  public JScrollPane annotationScroller = new JScrollPane();

  Border border1;

  BorderLayout borderLayout4 = new BorderLayout();

  static JPanel newJPanel()
  { // BH 2019
    JPanel p = new JPanel();
    // leaving this in, as it prevents
    // the checkerboard business, despite how
    // funky that looks. Remove if you want to.
    p.setBackground(Color.white);
    return p;
  }

  protected JPanel sequenceHolderPanel = newJPanel();

  protected JPanel seqPanelHolder = newJPanel();

  protected JPanel scalePanelHolder = newJPanel();

  protected JPanel idPanelHolder = newJPanel();

  protected JPanel idSpaceFillerPanel1 = newJPanel();

  public JPanel annotationSpaceFillerHolder = newJPanel();

  protected JPanel hscrollFillerPanel = newJPanel();

  JPanel hscrollHolder = newJPanel();

  public GAlignmentPanel()
  {
    try
    {
      jbInit();
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception
  {
    // annotationScroller.setBackground(Color.white); // BH 2019

    border1 = BorderFactory.createLineBorder(Color.gray, 1);
    idPanelHolder.setBorder(null);
    idPanelHolder.setPreferredSize(new Dimension(70, 10));
    this.setLayout(borderLayout7);
    sequenceHolderPanel
            .setMaximumSize(new Dimension(2147483647, 2147483647));
    sequenceHolderPanel.setMinimumSize(new Dimension(150, 150));
    sequenceHolderPanel.setPreferredSize(new Dimension(150, 150));
    sequenceHolderPanel.setLayout(borderLayout3);
    seqPanelHolder.setLayout(borderLayout1);
    scalePanelHolder.setBackground(Color.white);
    scalePanelHolder.setMinimumSize(new Dimension(10, 80));
    scalePanelHolder.setPreferredSize(new Dimension(10, 30));
    scalePanelHolder.setLayout(borderLayout6);
    idPanelHolder.setLayout(borderLayout5);
    idSpaceFillerPanel1.setBackground(Color.white);
    idSpaceFillerPanel1.setPreferredSize(new Dimension(10, 30));
    idSpaceFillerPanel1.setLayout(borderLayout11);
    annotationSpaceFillerHolder.setBackground(Color.white);
    annotationSpaceFillerHolder.setPreferredSize(new Dimension(10, 80));
    annotationSpaceFillerHolder.setLayout(borderLayout4);
    hscroll.setOrientation(JScrollBar.HORIZONTAL);
    hscrollHolder.setLayout(borderLayout10);
    hscrollFillerPanel.setBackground(Color.white);
    hscrollFillerPanel.setPreferredSize(new Dimension(70, 10));
    hscrollHolder.setBackground(Color.white);
    annotationScroller.setBorder(null);
    annotationScroller.setPreferredSize(new Dimension(10, 80));
    this.setPreferredSize(new Dimension(220, 166));

    sequenceHolderPanel.add(scalePanelHolder, BorderLayout.NORTH);
    sequenceHolderPanel.add(seqPanelHolder, BorderLayout.CENTER);
    seqPanelHolder.add(vscroll, BorderLayout.EAST);
    sequenceHolderPanel.add(annotationScroller, BorderLayout.SOUTH);

    // jPanel3.add(secondaryPanelHolder, BorderLayout.SOUTH);
    this.add(idPanelHolder, BorderLayout.WEST);
    idPanelHolder.add(idSpaceFillerPanel1, BorderLayout.NORTH);
    idPanelHolder.add(annotationSpaceFillerHolder, BorderLayout.SOUTH);
    this.add(hscrollHolder, BorderLayout.SOUTH);
    hscrollHolder.add(hscroll, BorderLayout.CENTER);
    hscrollHolder.add(hscrollFillerPanel, BorderLayout.WEST);
    this.add(sequenceHolderPanel, BorderLayout.CENTER);
  }
}
