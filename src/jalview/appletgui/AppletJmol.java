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

import java.awt.BorderLayout;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import jalview.bin.JalviewLite;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.io.DataSourceType;
import jalview.io.FileParse;
import jalview.io.StructureFile;
import jalview.schemes.BuriedColourScheme;
import jalview.schemes.HelixColourScheme;
import jalview.schemes.HydrophobicColourScheme;
import jalview.schemes.PurinePyrimidineColourScheme;
import jalview.schemes.StrandColourScheme;
import jalview.schemes.TaylorColourScheme;
import jalview.schemes.TurnColourScheme;
import jalview.schemes.UserColourScheme;
import jalview.schemes.ZappoColourScheme;
import jalview.structure.StructureSelectionManager;
import jalview.util.MessageManager;

public class AppletJmol extends EmbmenuFrame implements
        // StructureListener,
        KeyListener, ActionListener, ItemListener

{
  Menu fileMenu = new Menu(MessageManager.getString("action.file"));

  Menu viewMenu = new Menu(MessageManager.getString("action.view"));

  Menu coloursMenu = new Menu(MessageManager.getString("action.colour"));

  Menu chainMenu = new Menu(MessageManager.getString("action.show_chain"));

  Menu helpMenu = new Menu(MessageManager.getString("action.help"));

  MenuItem mappingMenuItem = new MenuItem(
          MessageManager.getString("label.view_mapping"));

  CheckboxMenuItem seqColour = new CheckboxMenuItem(
          MessageManager.getString("action.by_sequence"), true);

  CheckboxMenuItem jmolColour = new CheckboxMenuItem(
          MessageManager.getString("action.using_jmol"), false);

  MenuItem chain = new MenuItem(
          MessageManager.getString("action.by_chain"));

  MenuItem charge = new MenuItem(
          MessageManager.getString("label.charge_cysteine"));

  MenuItem zappo = new MenuItem(
          MessageManager.getString("label.colourScheme_zappo"));

  MenuItem taylor = new MenuItem(
          MessageManager.getString("label.colourScheme_taylor"));

  MenuItem hydro = new MenuItem(
          MessageManager.getString("label.colourScheme_hydrophobic"));

  MenuItem helix = new MenuItem(
          MessageManager.getString("label.colourScheme_helixpropensity"));

  MenuItem strand = new MenuItem(
          MessageManager.getString("label.colourScheme_strandpropensity"));

  MenuItem turn = new MenuItem(
          MessageManager.getString("label.colourScheme_turnpropensity"));

  MenuItem buried = new MenuItem(
          MessageManager.getString("label.colourScheme_buriedindex"));

  MenuItem purinepyrimidine = new MenuItem(
          MessageManager.getString("label.colourScheme_purine/pyrimidine"));

  MenuItem user = new MenuItem(
          MessageManager.getString("label.user_defined_colours"));

  MenuItem jmolHelp = new MenuItem(
          MessageManager.getString("label.jmol_help"));

  Panel scriptWindow;

  TextField inputLine;

  TextArea history;

  RenderPanel renderPanel;

  AlignmentPanel ap;

  List<AlignmentPanel> _aps = new ArrayList<>(); // remove? never
                                                 // added to

  String fileLoadingError;

  boolean loadedInline;

  // boolean colourBySequence = true;

  FeatureRenderer fr = null;

  AppletJmolBinding jmb;

  /**
   * datasource protocol for access to PDBEntry
   */
  String protocol = null;

  /**
   * Load a bunch of pdb entries associated with sequences in the alignment and
   * display them - aligning them if necessary.
   * 
   * @param pdbentries
   *          each pdb file (at least one needed)
   * @param boundseqs
   *          each set of sequences for each pdb file (must match number of pdb
   *          files)
   * @param boundchains
   *          the target pdb chain corresponding with each sequence associated
   *          with each pdb file (may be null at any level)
   * @param align
   *          true/false
   * @param ap
   *          associated alignment
   * @param protocol
   *          how to get pdb data
   */
  public AppletJmol(PDBEntry[] pdbentries, SequenceI[][] boundseqs,
          String[][] boundchains, boolean align, AlignmentPanel ap,
          String protocol)
  {
    throw new Error(MessageManager.getString("error.not_yet_implemented"));
  }

  public AppletJmol(PDBEntry pdbentry, SequenceI[] seq, String[] chains,
          AlignmentPanel ap, DataSourceType protocol)
  {
    this.ap = ap;
    jmb = new AppletJmolBinding(this, ap.getStructureSelectionManager(),
            new PDBEntry[]
            { pdbentry }, new SequenceI[][] { seq }, protocol);
    jmb.setColourBySequence(true);
    if (pdbentry.getId() == null || pdbentry.getId().length() < 1)
    {
      if (protocol == DataSourceType.PASTE)
      {
        pdbentry.setId(
                "PASTED PDB" + (chains == null ? "_" : chains.toString()));
      }
      else
      {
        pdbentry.setId(pdbentry.getFile());
      }
    }

    if (JalviewLite.debug)
    {
      System.err
              .println("AppletJmol: PDB ID is '" + pdbentry.getId() + "'");
    }

    String alreadyMapped = StructureSelectionManager
            .getStructureSelectionManager(ap.av.applet)
            .alreadyMappedToFile(pdbentry.getId());
    StructureFile reader = null;
    if (alreadyMapped != null)
    {
      reader = StructureSelectionManager
              .getStructureSelectionManager(ap.av.applet)
              .setMapping(seq, chains, pdbentry.getFile(), protocol, null);
      // PROMPT USER HERE TO ADD TO NEW OR EXISTING VIEW?
      // FOR NOW, LETS JUST OPEN A NEW WINDOW
    }
    MenuBar menuBar = new MenuBar();
    menuBar.add(fileMenu);
    fileMenu.add(mappingMenuItem);
    menuBar.add(viewMenu);
    mappingMenuItem.addActionListener(this);
    viewMenu.add(chainMenu);
    menuBar.add(coloursMenu);
    menuBar.add(helpMenu);

    charge.addActionListener(this);
    hydro.addActionListener(this);
    chain.addActionListener(this);
    seqColour.addItemListener(this);
    jmolColour.addItemListener(this);
    zappo.addActionListener(this);
    taylor.addActionListener(this);
    helix.addActionListener(this);
    strand.addActionListener(this);
    turn.addActionListener(this);
    buried.addActionListener(this);
    purinepyrimidine.addActionListener(this);
    user.addActionListener(this);

    jmolHelp.addActionListener(this);

    coloursMenu.add(seqColour);
    coloursMenu.add(chain);
    coloursMenu.add(charge);
    coloursMenu.add(zappo);
    coloursMenu.add(taylor);
    coloursMenu.add(hydro);
    coloursMenu.add(helix);
    coloursMenu.add(strand);
    coloursMenu.add(turn);
    coloursMenu.add(buried);
    coloursMenu.add(purinepyrimidine);
    coloursMenu.add(user);
    coloursMenu.add(jmolColour);
    helpMenu.add(jmolHelp);
    this.setLayout(new BorderLayout());

    setMenuBar(menuBar);

    renderPanel = new RenderPanel();
    embedMenuIfNeeded(renderPanel);
    this.add(renderPanel, BorderLayout.CENTER);
    scriptWindow = new Panel();
    scriptWindow.setVisible(false);
    // this.add(scriptWindow, BorderLayout.SOUTH);

    try
    {
      jmb.allocateViewer(renderPanel, true,
              ap.av.applet.getName() + "_jmol_",
              ap.av.applet.getDocumentBase(), ap.av.applet.getCodeBase(),
              "-applet", scriptWindow, null);
    } catch (Exception e)
    {
      System.err.println(
              "Couldn't create a jmol viewer. Args to allocate viewer were:\nDocumentBase="
                      + ap.av.applet.getDocumentBase() + "\nCodebase="
                      + ap.av.applet.getCodeBase());
      e.printStackTrace();
      dispose();
      return;
    }
    // jmb.newJmolPopup(true, "Jmol", true);

    this.addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent evt)
      {
        closeViewer();
      }
    });
    pdbentry.setProperty("protocol", protocol);
    if (pdbentry.getFile() != null)

    {
      // import structure data from pdbentry.getFile based on given protocol
      if (protocol == DataSourceType.PASTE)
      {
        // TODO: JAL-623 : correctly record file contents for matching up later
        // pdbentry.getProperty().put("pdbfilehash",""+pdbentry.getFile().hashCode());
        loadInline(pdbentry.getFile());
      }
      else if (protocol == DataSourceType.FILE
              || protocol == DataSourceType.URL)
      {
        jmb.jmolViewer.openFile(pdbentry.getFile());
      }
      else
      {
        // probably CLASSLOADER based datasource..
        // Try and get a reader on the datasource, and pass that to Jmol
        try
        {
          java.io.Reader freader = null;
          if (reader != null)
          {
            if (jalview.bin.JalviewLite.debug)
            {
              System.err.println(
                      "AppletJmol:Trying to reuse existing PDBfile IO parser.");
            }
            // re-use the one we opened earlier
            freader = reader.getReader();
          }
          if (freader == null)
          {
            if (jalview.bin.JalviewLite.debug)
            {
              System.err.println(
                      "AppletJmol:Creating new PDBfile IO parser.");
            }
            FileParse fp = new FileParse(pdbentry.getFile(), protocol);
            fp.mark();
            // reader = new mc_view.PDBfile(fp);
            // could set ID, etc.
            // if (!reader.isValid())
            // {
            // throw new Exception("Invalid datasource.
            // "+reader.getWarningMessage());
            // }
            // fp.reset();
            freader = fp.getReader();
          }
          if (freader == null)
          {
            throw new Exception(MessageManager.getString(
                    "exception.invalid_datasource_couldnt_obtain_reader"));
          }
          jmb.jmolViewer.openReader(pdbentry.getFile(), pdbentry.getId(),
                  freader);
        } catch (Exception e)
        {
          // give up!
          System.err.println("Couldn't access pdbentry id="
                  + pdbentry.getId() + " and file=" + pdbentry.getFile()
                  + " using protocol=" + protocol);
          e.printStackTrace();
        }
      }
    }

    jalview.bin.JalviewLite.addFrame(this, jmb.getViewerTitle(), 400, 400);
  }

  public void loadInline(String string)
  {
    loadedInline = true;
    jmb.loadInline(string);
  }

  void setChainMenuItems(List<String> chains)
  {
    chainMenu.removeAll();

    MenuItem menuItem = new MenuItem(MessageManager.getString("label.all"));
    menuItem.addActionListener(this);

    chainMenu.add(menuItem);

    CheckboxMenuItem menuItemCB;
    for (String ch : chains)
    {
      menuItemCB = new CheckboxMenuItem(ch, true);
      menuItemCB.addItemListener(this);
      chainMenu.add(menuItemCB);
    }
  }

  boolean allChainsSelected = false;

  void centerViewer()
  {
    Vector<String> toshow = new Vector<>();
    for (int i = 0; i < chainMenu.getItemCount(); i++)
    {
      if (chainMenu.getItem(i) instanceof CheckboxMenuItem)
      {
        CheckboxMenuItem item = (CheckboxMenuItem) chainMenu.getItem(i);
        if (item.getState())
        {
          toshow.addElement(item.getLabel());
        }
      }
    }
    jmb.showChains(toshow);
  }

  void closeViewer()
  {
    jmb.closeViewer(true);
    jmb = null;
    this.setVisible(false);
  }

  @Override
  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == mappingMenuItem)
    {
      jalview.appletgui.CutAndPasteTransfer cap = new jalview.appletgui.CutAndPasteTransfer(
              false, null);
      Frame frame = new Frame();
      frame.add(cap);

      StringBuffer sb = new StringBuffer();
      try
      {
        cap.setText(jmb.printMappings());
      } catch (OutOfMemoryError ex)
      {
        frame.dispose();
        System.err.println(
                "Out of memory when trying to create dialog box with sequence-structure mapping.");
        return;
      }
      jalview.bin.JalviewLite.addFrame(frame,
              MessageManager.getString("label.pdb_sequence_mapping"), 550,
              600);
    }
    else if (evt.getSource() == charge)
    {
      setEnabled(charge);
      jmb.colourByCharge();
    }

    else if (evt.getSource() == chain)
    {
      setEnabled(chain);
      jmb.colourByChain();
    }
    else if (evt.getSource() == zappo)
    {
      setEnabled(zappo);
      jmb.colourByJalviewColourScheme(new ZappoColourScheme());
    }
    else if (evt.getSource() == taylor)
    {
      setEnabled(taylor);
      jmb.colourByJalviewColourScheme(new TaylorColourScheme());
    }
    else if (evt.getSource() == hydro)
    {
      setEnabled(hydro);
      jmb.colourByJalviewColourScheme(new HydrophobicColourScheme());
    }
    else if (evt.getSource() == helix)
    {
      setEnabled(helix);
      jmb.colourByJalviewColourScheme(new HelixColourScheme());
    }
    else if (evt.getSource() == strand)
    {
      setEnabled(strand);
      jmb.colourByJalviewColourScheme(new StrandColourScheme());
    }
    else if (evt.getSource() == turn)
    {
      setEnabled(turn);
      jmb.colourByJalviewColourScheme(new TurnColourScheme());
    }
    else if (evt.getSource() == buried)
    {
      setEnabled(buried);
      jmb.colourByJalviewColourScheme(new BuriedColourScheme());
    }
    else if (evt.getSource() == purinepyrimidine)
    {
      jmb.colourByJalviewColourScheme(new PurinePyrimidineColourScheme());
    }
    else if (evt.getSource() == user)
    {
      setEnabled(user);
      new UserDefinedColours(this);
    }
    else if (evt.getSource() == jmolHelp)
    {
      try
      {
        ap.av.applet.getAppletContext()
                .showDocument(new java.net.URL(
                        "http://jmol.sourceforge.net/docs/JmolUserGuide/"),
                        "jmolHelp");
      } catch (java.net.MalformedURLException ex)
      {
      }
    }
    else
    {
      allChainsSelected = true;
      for (int i = 0; i < chainMenu.getItemCount(); i++)
      {
        if (chainMenu.getItem(i) instanceof CheckboxMenuItem)
        {
          ((CheckboxMenuItem) chainMenu.getItem(i)).setState(true);
        }
      }

      centerViewer();
      allChainsSelected = false;
    }
  }

  /**
   * tick or untick the seqColour menu entry or jmoColour entry depending upon
   * if it was selected or not.
   * 
   * @param itm
   */
  private void setEnabled(MenuItem itm)
  {
    jmolColour.setState(itm == jmolColour);
    seqColour.setState(itm == seqColour);
    jmb.setColourBySequence(itm == seqColour);
  }

  @Override
  public void itemStateChanged(ItemEvent evt)
  {
    if (evt.getSource() == jmolColour)
    {
      setEnabled(jmolColour);
      jmb.setColourBySequence(false);
    }
    else if (evt.getSource() == seqColour)
    {
      setEnabled(seqColour);
      jmb.colourBySequence(ap);
    }
    else if (!allChainsSelected)
    {
      centerViewer();
    }
  }

  @Override
  public void keyPressed(KeyEvent evt)
  {
    if (evt.getKeyCode() == KeyEvent.VK_ENTER && scriptWindow.isVisible())
    {
      jmb.eval(inputLine.getText());
      addToHistory("$ " + inputLine.getText());
      inputLine.setText("");
    }

  }

  @Override
  public void keyTyped(KeyEvent evt)
  {
  }

  @Override
  public void keyReleased(KeyEvent evt)
  {
  }

  public void updateColours(Object source)
  {
    AlignmentPanel panel = (AlignmentPanel) source;
    jmb.colourBySequence(panel);
  }

  public void updateTitleAndMenus()
  {
    if (jmb.hasFileLoadingError())
    {
      repaint();
      return;
    }
    setChainMenuItems(jmb.getChainNames());
    jmb.colourBySequence(ap);

    setTitle(jmb.getViewerTitle());
  }

  public void showUrl(String url)
  {
    try
    {
      ap.av.applet.getAppletContext().showDocument(new java.net.URL(url),
              "jmolOutput");
    } catch (java.net.MalformedURLException ex)
    {
    }
  }

  Panel splitPane = null;

  public void showConsole(boolean showConsole)
  {
    if (showConsole)
    {
      remove(renderPanel);
      splitPane = new Panel();

      splitPane.setLayout(new java.awt.GridLayout(2, 1));
      splitPane.add(renderPanel);
      splitPane.add(scriptWindow);
      scriptWindow.setVisible(true);
      this.add(splitPane, BorderLayout.CENTER);
      splitPane.setVisible(true);
      splitPane.validate();
    }
    else
    {
      scriptWindow.setVisible(false);
      remove(splitPane);
      add(renderPanel, BorderLayout.CENTER);
      splitPane = null;
    }
    validate();
  }

  public float[][] functionXY(String functionName, int x, int y)
  {
    return null;
  }

  // /End JmolStatusListener
  // /////////////////////////////

  class RenderPanel extends Panel
  {
    Dimension currentSize = new Dimension();

    @Override
    public void update(Graphics g)
    {
      paint(g);
    }

    @Override
    public void paint(Graphics g)
    {
      currentSize = this.getSize();

      if (jmb.jmolViewer == null)
      {
        g.setColor(Color.black);
        g.fillRect(0, 0, currentSize.width, currentSize.height);
        g.setColor(Color.white);
        g.setFont(new Font("Verdana", Font.BOLD, 14));
        g.drawString(MessageManager.getString("label.retrieving_pdb_data"),
                20, currentSize.height / 2);
      }
      else
      {
        jmb.jmolViewer.renderScreenImage(g, currentSize.width,
                currentSize.height);
      }
    }
  }

  /*
   * @Override public Color getColour(int atomIndex, int pdbResNum, String
   * chain, String pdbId) { return jmb.getColour(atomIndex, pdbResNum, chain,
   * pdbId); }
   * 
   * @Override public String[] getPdbFile() { return jmb.getPdbFile(); }
   * 
   * @Override public void highlightAtom(int atomIndex, int pdbResNum, String
   * chain, String pdbId) { jmb.highlightAtom(atomIndex, pdbResNum, chain,
   * pdbId);
   * 
   * }
   * 
   * @Override public void mouseOverStructure(int atomIndex, String strInfo) {
   * jmb.mouseOverStructure(atomIndex, strInfo);
   * 
   * }
   */
  public void colourByJalviewColourScheme(UserColourScheme ucs)
  {
    jmb.colourByJalviewColourScheme(ucs);
  }

  public AlignmentPanel getAlignmentPanelFor(AlignmentI alignment)
  {
    for (int i = 0; i < _aps.size(); i++)
    {
      if (_aps.get(i).av.getAlignment() == alignment)
      {
        return (_aps.get(i));
      }
    }
    return ap;
  }

  /**
   * Append the given text to the history object
   * 
   * @param text
   */
  public void addToHistory(String text)
  {
    // actually currently never initialised
    if (history != null)
    {
      history.append("\n" + text);
    }
  }
}
