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

import jalview.appletgui.AlignmentPanel;
import jalview.appletgui.EmbmenuFrame;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.io.DataSourceType;
import jalview.schemes.BuriedColourScheme;
import jalview.schemes.HelixColourScheme;
import jalview.schemes.HydrophobicColourScheme;
import jalview.schemes.StrandColourScheme;
import jalview.schemes.TaylorColourScheme;
import jalview.schemes.TurnColourScheme;
import jalview.schemes.ZappoColourScheme;
import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.CheckboxGroup;
import java.awt.CheckboxMenuItem;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class AppletPDBViewer extends EmbmenuFrame
        implements ActionListener, ItemListener
{
  AppletPDBCanvas pdbcanvas;

  public AppletPDBViewer(PDBEntry pdbentry, SequenceI[] seq,
          String[] chains, AlignmentPanel ap, DataSourceType protocol)
  {
    try
    {
      jbInit();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }

    pdbcanvas = new AppletPDBCanvas(pdbentry, seq, chains, ap, protocol);

    embedMenuIfNeeded(pdbcanvas);
    add(pdbcanvas, BorderLayout.CENTER);

    StringBuffer title = new StringBuffer(
            seq[0].getName() + ":" + pdbcanvas.pdbentry.getFile());

    jalview.bin.JalviewLite.addFrame(this, title.toString(), 400, 400);

  }

  @Override
  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == mapping)
    {
      jalview.appletgui.CutAndPasteTransfer cap = new jalview.appletgui.CutAndPasteTransfer(
              false, null);
      Frame frame = new Frame();
      frame.add(cap);
      jalview.bin.JalviewLite.addFrame(frame,
              MessageManager.getString("label.pdb_sequence_mapping"), 500,
              600);
      cap.setText(pdbcanvas.mappingDetails.toString());

    }
    else if (evt.getSource() == charge)
    {
      pdbcanvas.bysequence = false;
      pdbcanvas.pdb.setChargeColours();
    }

    else if (evt.getSource() == chain)
    {
      pdbcanvas.bysequence = false;
      pdbcanvas.pdb.setChainColours();
    }
    else if (evt.getSource() == seqButton)
    {
      pdbcanvas.bysequence = true;
      pdbcanvas.colourBySequence();

    }
    else if (evt.getSource() == zappo)
    {
      pdbcanvas.setColours(new ZappoColourScheme());
    }
    else if (evt.getSource() == taylor)
    {
      pdbcanvas.setColours(new TaylorColourScheme());
    }
    else if (evt.getSource() == hydro)
    {
      pdbcanvas.setColours(new HydrophobicColourScheme());
    }
    else if (evt.getSource() == helix)
    {
      pdbcanvas.setColours(new HelixColourScheme());
    }
    else if (evt.getSource() == strand)
    {
      pdbcanvas.setColours(new StrandColourScheme());
    }
    else if (evt.getSource() == turn)
    {
      pdbcanvas.setColours(new TurnColourScheme());
    }
    else if (evt.getSource() == buried)
    {
      pdbcanvas.setColours(new BuriedColourScheme());
    }
    else if (evt.getSource() == user)
    {
      pdbcanvas.bysequence = false;
      new jalview.appletgui.UserDefinedColours(pdbcanvas);
    }

    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();

  }

  @Override
  public void itemStateChanged(ItemEvent evt)
  {
    if (evt.getSource() == allchains)
    {
      pdbcanvas.setAllchainsVisible(allchains.getState());
    }
    else if (evt.getSource() == wire)
    {
      pdbcanvas.wire = !pdbcanvas.wire;
    }
    else if (evt.getSource() == depth)
    {
      pdbcanvas.depthcue = !pdbcanvas.depthcue;
    }
    else if (evt.getSource() == zbuffer)
    {
      pdbcanvas.zbuffer = !pdbcanvas.zbuffer;
    }
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  private void jbInit() throws Exception
  {
    setMenuBar(jMenuBar1);
    fileMenu.setLabel(MessageManager.getString("action.file"));
    coloursMenu.setLabel(MessageManager.getString("label.colours"));
    mapping.setLabel(MessageManager.getString("label.view_mapping"));
    mapping.addActionListener(this);
    wire.setLabel(MessageManager.getString("label.wireframe"));
    wire.addItemListener(this);
    depth.setState(true);
    depth.setLabel(MessageManager.getString("label.depthcue"));
    depth.addItemListener(this);
    zbuffer.setState(true);
    zbuffer.setLabel(MessageManager.getString("label.z_buffering"));
    zbuffer.addItemListener(this);
    charge.setLabel(MessageManager.getString("label.charge_cysteine"));
    charge.addActionListener(this);
    hydro.setLabel(
            MessageManager.getString("label.colourScheme_hydrophobic"));
    hydro.addActionListener(this);
    chain.setLabel(MessageManager.getString("action.by_chain"));
    chain.addActionListener(this);
    seqButton.setLabel(MessageManager.getString("action.by_sequence"));
    seqButton.addActionListener(this);
    allchains
            .setLabel(MessageManager.getString("label.all_chains_visible"));
    allchains.addItemListener(this);
    viewMenu.setLabel(MessageManager.getString("action.view"));
    zappo.setLabel(MessageManager.getString("label.colourScheme_zappo"));
    zappo.addActionListener(this);
    taylor.setLabel(MessageManager.getString("label.colourScheme_taylor"));
    taylor.addActionListener(this);
    helix.setLabel(
            MessageManager.getString("label.colourScheme_helixpropensity"));
    helix.addActionListener(this);
    strand.setLabel(MessageManager
            .getString("label.colourScheme_strandpropensity"));
    strand.addActionListener(this);
    turn.setLabel(
            MessageManager.getString("label.colourScheme_turnpropensity"));
    turn.addActionListener(this);
    buried.setLabel(
            MessageManager.getString("label.colourScheme_buriedindex"));
    buried.addActionListener(this);
    user.setLabel(MessageManager.getString("action.user_defined"));
    user.addActionListener(this);
    jMenuBar1.add(fileMenu);
    jMenuBar1.add(coloursMenu);
    jMenuBar1.add(viewMenu);
    fileMenu.add(mapping);
    ;

    coloursMenu.add(seqButton);
    coloursMenu.add(chain);
    coloursMenu.add(charge);
    coloursMenu.add(zappo);
    coloursMenu.add(taylor);
    coloursMenu.add(hydro);
    coloursMenu.add(helix);
    coloursMenu.add(strand);
    coloursMenu.add(turn);
    coloursMenu.add(buried);
    coloursMenu.add(user);
    viewMenu.add(wire);
    viewMenu.add(depth);
    viewMenu.add(zbuffer);
    viewMenu.add(allchains);
    allchains.setState(true);
  }

  MenuBar jMenuBar1 = new MenuBar();

  Menu fileMenu = new Menu();

  Menu coloursMenu = new Menu();

  MenuItem mapping = new MenuItem();

  CheckboxGroup bg = new CheckboxGroup();

  CheckboxMenuItem wire = new CheckboxMenuItem();

  CheckboxMenuItem depth = new CheckboxMenuItem();

  CheckboxMenuItem zbuffer = new CheckboxMenuItem();

  MenuItem charge = new MenuItem();

  MenuItem hydro = new MenuItem();

  MenuItem chain = new MenuItem();

  MenuItem seqButton = new MenuItem();

  CheckboxMenuItem allchains = new CheckboxMenuItem();

  Menu viewMenu = new Menu();

  MenuItem turn = new MenuItem();

  MenuItem strand = new MenuItem();

  MenuItem helix = new MenuItem();

  MenuItem taylor = new MenuItem();

  MenuItem zappo = new MenuItem();

  MenuItem buried = new MenuItem();

  MenuItem user = new MenuItem();

  // End StructureListener
  // //////////////////////////

}
