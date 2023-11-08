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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import jalview.api.AlignmentViewPanel;
import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.gui.JalviewColourChooser.ColourChooserListener;
import jalview.gui.StructureViewer.ViewerType;
import jalview.gui.ViewSelectionMenu.ViewSetProvider;
import jalview.io.DataSourceType;
import jalview.io.JalviewFileChooser;
import jalview.io.JalviewFileView;
import jalview.jbgui.GStructureViewer;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ColourSchemes;
import jalview.structure.StructureMapping;
import jalview.structures.models.AAStructureBindingModel;
import jalview.util.BrowserLauncher;
import jalview.util.MessageManager;
import jalview.ws.dbsources.EBIAlfaFold;
import jalview.ws.dbsources.Pdb;
import jalview.ws.utils.UrlDownloadClient;

/**
 * Base class with common functionality for JMol, Chimera or other structure
 * viewers.
 * 
 * @author gmcarstairs
 *
 */
public abstract class StructureViewerBase extends GStructureViewer
        implements Runnable, ViewSetProvider
{
  /*
   * names for colour options (additional to Jalview colour schemes)
   */
  enum ViewerColour
  {
    BySequence, ByChain, ChargeCysteine, ByViewer
  }

  /**
   * list of sequenceSet ids associated with the view
   */
  protected List<String> _aps = new ArrayList<>();

  /**
   * list of alignment panels to use for superposition
   */
  protected Vector<AlignmentViewPanel> _alignwith = new Vector<>();

  /**
   * list of alignment panels that are used for colouring structures by aligned
   * sequences
   */
  protected Vector<AlignmentViewPanel> _colourwith = new Vector<>();

  private String viewId = null;

  private AlignmentPanel ap;

  protected boolean alignAddedStructures = false;

  protected volatile boolean _started = false;

  protected volatile boolean addingStructures = false;

  protected Thread worker = null;

  protected boolean allChainsSelected = false;

  protected JMenu viewSelectionMenu;

  /**
   * set after sequence colouring has been applied for this structure viewer.
   * used to determine if the final sequence/structure mapping has been
   * determined
   */
  protected volatile boolean seqColoursApplied = false;

  private IProgressIndicator progressBar = null;

  private Random random = new Random();

  /**
   * Default constructor
   */
  public StructureViewerBase()
  {
    super();
    setFrameIcon(null);
  }

  /**
   * @return true if added structures should be aligned to existing one(s)
   */
  @Override
  public boolean isAlignAddedStructures()
  {
    return alignAddedStructures;
  }

  /**
   * 
   * @param true
   *          if added structures should be aligned to existing one(s)
   */
  @Override
  public void setAlignAddedStructures(boolean alignAdded)
  {
    alignAddedStructures = alignAdded;
  }

  /**
   * called by the binding model to indicate when adding structures is happening
   * or has been completed
   * 
   * @param addingStructures
   */
  public synchronized void setAddingStructures(boolean addingStructures)
  {
    this.addingStructures = addingStructures;
  }

  /**
   * 
   * @param ap2
   * @return true if this Jmol instance is linked with the given alignPanel
   */
  public boolean isLinkedWith(AlignmentPanel ap2)
  {
    return _aps.contains(ap2.av.getSequenceSetId());
  }

  public boolean isUsedforaligment(AlignmentViewPanel ap2)
  {

    return (_alignwith != null) && _alignwith.contains(ap2);
  }

  @Override
  public boolean isUsedForColourBy(AlignmentViewPanel ap2)
  {
    return (_colourwith != null) && _colourwith.contains(ap2);
  }

  /**
   * 
   * @return TRUE if the view is NOT being coloured by the alignment colours.
   */
  public boolean isColouredByViewer()
  {
    return !getBinding().isColourBySequence();
  }

  public String getViewId()
  {
    if (viewId == null)
    {
      viewId = System.currentTimeMillis() + "." + this.hashCode();
    }
    return viewId;
  }

  protected void setViewId(String viewId)
  {
    this.viewId = viewId;
  }

  protected void buildActionMenu()
  {
    if (_alignwith == null)
    {
      _alignwith = new Vector<>();
    }
    if (_alignwith.size() == 0 && ap != null)
    {
      _alignwith.add(ap);
    }
    ;
    // TODO: refactor to allow concrete classes to register buttons for adding
    // here
    // currently have to override to add buttons back in after they are cleared
    // in this loop
    for (Component c : viewerActionMenu.getMenuComponents())
    {
      if (c != alignStructs)
      {
        viewerActionMenu.remove((JMenuItem) c);
      }
    }
  }

  @Override
  public AlignmentPanel getAlignmentPanel()
  {
    return ap;
  }

  protected void setAlignmentPanel(AlignmentPanel alp)
  {
    this.ap = alp;
  }

  @Override
  public AlignmentPanel[] getAllAlignmentPanels()
  {
    AlignmentPanel[] t, list = new AlignmentPanel[0];
    for (String setid : _aps)
    {
      AlignmentPanel[] panels = PaintRefresher.getAssociatedPanels(setid);
      if (panels != null)
      {
        t = new AlignmentPanel[list.length + panels.length];
        System.arraycopy(list, 0, t, 0, list.length);
        System.arraycopy(panels, 0, t, list.length, panels.length);
        list = t;
      }
    }

    return list;
  }

  /**
   * set the primary alignmentPanel reference and add another alignPanel to the
   * list of ones to use for colouring and aligning
   * 
   * @param nap
   */
  public void addAlignmentPanel(AlignmentPanel nap)
  {
    if (getAlignmentPanel() == null)
    {
      setAlignmentPanel(nap);
    }
    if (!_aps.contains(nap.av.getSequenceSetId()))
    {
      _aps.add(nap.av.getSequenceSetId());
    }
  }

  /**
   * remove any references held to the given alignment panel
   * 
   * @param nap
   */
  @Override
  public void removeAlignmentPanel(AlignmentViewPanel nap)
  {
    try
    {
      _alignwith.remove(nap);
      _colourwith.remove(nap);
      if (getAlignmentPanel() == nap)
      {
        setAlignmentPanel(null);
        for (AlignmentPanel aps : getAllAlignmentPanels())
        {
          if (aps != nap)
          {
            setAlignmentPanel(aps);
            break;
          }
        }
      }
    } catch (Exception ex)
    {
    }
    if (getAlignmentPanel() != null)
    {
      buildActionMenu();
    }
  }

  public void useAlignmentPanelForSuperposition(AlignmentPanel nap)
  {
    addAlignmentPanel(nap);
    if (!_alignwith.contains(nap))
    {
      _alignwith.add(nap);
    }
  }

  public void excludeAlignmentPanelForSuperposition(AlignmentPanel nap)
  {
    if (_alignwith.contains(nap))
    {
      _alignwith.remove(nap);
    }
  }

  public void useAlignmentPanelForColourbyseq(AlignmentPanel nap,
          boolean enableColourBySeq)
  {
    useAlignmentPanelForColourbyseq(nap);
    getBinding().setColourBySequence(enableColourBySeq);
    seqColour.setSelected(enableColourBySeq);
    viewerColour.setSelected(!enableColourBySeq);
  }

  public void useAlignmentPanelForColourbyseq(AlignmentPanel nap)
  {
    addAlignmentPanel(nap);
    if (!_colourwith.contains(nap))
    {
      _colourwith.add(nap);
    }
  }

  public void excludeAlignmentPanelForColourbyseq(AlignmentPanel nap)
  {
    if (_colourwith.contains(nap))
    {
      _colourwith.remove(nap);
    }
  }

  public abstract ViewerType getViewerType();

  /**
   * add a new structure (with associated sequences and chains) to this viewer,
   * retrieving it if necessary first.
   * 
   * @param pdbentry
   * @param seqs
   * @param chains
   * @param align
   *          if true, new structure(s) will be aligned using associated
   *          alignment
   * @param alignFrame
   */
  protected void addStructure(final PDBEntry pdbentry,
          final SequenceI[] seqs, final String[] chains,
          final IProgressIndicator alignFrame)
  {
    if (pdbentry.getFile() == null)
    {
      if (worker != null && worker.isAlive())
      {
        // a retrieval is in progress, wait around and add ourselves to the
        // queue.
        new Thread(new Runnable()
        {
          @Override
          public void run()
          {
            while (worker != null && worker.isAlive() && _started)
            {
              try
              {
                Thread.sleep(100 + ((int) Math.random() * 100));

              } catch (Exception e)
              {
              }
            }
            // and call ourselves again.
            addStructure(pdbentry, seqs, chains, alignFrame);
          }
        }).start();
        return;
      }
    }
    // otherwise, start adding the structure.
    getBinding().addSequenceAndChain(new PDBEntry[] { pdbentry },
            new SequenceI[][]
            { seqs }, new String[][] { chains });
    addingStructures = true;
    _started = false;
    worker = new Thread(this);
    worker.start();
    return;
  }

  protected boolean hasPdbId(String pdbId)
  {
    return getBinding().hasPdbId(pdbId);
  }

  /**
   * Returns a list of any viewer of the instantiated type. The list is
   * restricted to those linked to the given alignment panel if it is not null.
   */
  protected List<StructureViewerBase> getViewersFor(AlignmentPanel alp)
  {
    return Desktop.instance.getStructureViewers(alp, this.getClass());
  }

  @Override
  public void addToExistingViewer(PDBEntry pdbentry, SequenceI[] seq,
          String[] chains, final AlignmentViewPanel apanel, String pdbId)
  {
    /*
     * JAL-1742 exclude view with this structure already mapped (don't offer
     * to align chain B to chain A of the same structure); code may defend
     * against this possibility before we reach here
     */
    if (hasPdbId(pdbId))
    {
      return;
    }
    AlignmentPanel alignPanel = (AlignmentPanel) apanel; // Implementation error
                                                         // if this
    // cast fails
    useAlignmentPanelForSuperposition(alignPanel);
    addStructure(pdbentry, seq, chains, alignPanel.alignFrame);
  }

  /**
   * Adds mappings for the given sequences to an already opened PDB structure,
   * and updates any viewers that have the PDB file
   * 
   * @param seq
   * @param chains
   * @param apanel
   * @param pdbFilename
   */
  public void addSequenceMappingsToStructure(SequenceI[] seq,
          String[] chains, final AlignmentViewPanel alpanel,
          String pdbFilename)
  {
    AlignmentPanel apanel = (AlignmentPanel) alpanel;

    // TODO : Fix multiple seq to one chain issue here.
    /*
     * create the mappings
     */
    apanel.getStructureSelectionManager().setMapping(seq, chains,
            pdbFilename, DataSourceType.FILE, getProgressIndicator());

    /*
     * alert the FeatureRenderer to show new (PDB RESNUM) features
     */
    if (apanel.getSeqPanel().seqCanvas.fr != null)
    {
      apanel.getSeqPanel().seqCanvas.fr.featuresAdded();
      // note - we don't do a refresh for structure here because we do it
      // explicitly for all panels later on
      apanel.paintAlignment(true, false);
    }

    /*
     * add the sequences to any other viewers (of the same type) for this pdb
     * file
     */
    // JBPNOTE: this looks like a binding routine, rather than a gui routine
    for (StructureViewerBase viewer : getViewersFor(null))
    {
      AAStructureBindingModel bindingModel = viewer.getBinding();
      for (int pe = 0; pe < bindingModel.getPdbCount(); pe++)
      {
        if (bindingModel.getPdbEntry(pe).getFile().equals(pdbFilename))
        {
          bindingModel.addSequence(pe, seq);
          viewer.addAlignmentPanel(apanel);
          /*
           * add it to the set of alignments used for colouring structure by
           * sequence
           */
          viewer.useAlignmentPanelForColourbyseq(apanel);
          viewer.buildActionMenu();
          apanel.getStructureSelectionManager()
                  .sequenceColoursChanged(apanel);
          break;
        }
      }
    }
  }

  @Override
  public boolean addAlreadyLoadedFile(SequenceI[] seq, String[] chains,
          final AlignmentViewPanel apanel, String pdbId)
  {
    String alreadyMapped = apanel.getStructureSelectionManager()
            .alreadyMappedToFile(pdbId);

    if (alreadyMapped == null)
    {
      return false;
    }

    addSequenceMappingsToStructure(seq, chains, apanel, alreadyMapped);
    return true;
  }

  void setChainMenuItems(List<String> chainNames)
  {
    chainMenu.removeAll();
    if (chainNames == null || chainNames.isEmpty())
    {
      return;
    }
    JMenuItem menuItem = new JMenuItem(
            MessageManager.getString("label.all"));
    menuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent evt)
      {
        allChainsSelected = true;
        for (int i = 0; i < chainMenu.getItemCount(); i++)
        {
          if (chainMenu.getItem(i) instanceof JCheckBoxMenuItem)
          {
            ((JCheckBoxMenuItem) chainMenu.getItem(i)).setSelected(true);
          }
        }
        showSelectedChains();
        allChainsSelected = false;
      }
    });

    chainMenu.add(menuItem);

    for (String chain : chainNames)
    {
      menuItem = new JCheckBoxMenuItem(chain, true);
      menuItem.addItemListener(new ItemListener()
      {
        @Override
        public void itemStateChanged(ItemEvent evt)
        {
          if (!allChainsSelected)
          {
            showSelectedChains();
          }
        }
      });

      chainMenu.add(menuItem);
    }
  }

  /**
   * Action on selecting one of Jalview's registered colour schemes
   */
  @Override
  public void changeColour_actionPerformed(String colourSchemeName)
  {
    AlignmentI al = getAlignmentPanel().av.getAlignment();
    ColourSchemeI cs = ColourSchemes.getInstance().getColourScheme(
            colourSchemeName, getAlignmentPanel().av, al, null);
    getBinding().colourByJalviewColourScheme(cs);
  }

  /**
   * Builds the colour menu
   */
  protected void buildColourMenu()
  {
    colourMenu.removeAll();
    AlignmentI al = getAlignmentPanel().av.getAlignment();

    /*
     * add colour by sequence, by chain, by charge and cysteine
     */
    colourMenu.add(seqColour);
    colourMenu.add(chainColour);
    colourMenu.add(chargeColour);
    chargeColour.setEnabled(!al.isNucleotide());

    /*
     * add all 'simple' (per-residue) colour schemes registered to Jalview
     */
    ButtonGroup itemGroup = ColourMenuHelper.addMenuItems(colourMenu, this,
            al, true);

    /*
     * add 'colour by viewer' (menu item text is set in subclasses)
     */
    viewerColour.setSelected(false);
    viewerColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        viewerColour_actionPerformed();
      }
    });
    colourMenu.add(viewerColour);

    /*
     * add 'set background colour'
     */
    JMenuItem backGround = new JMenuItem();
    backGround
            .setText(MessageManager.getString("action.background_colour"));
    backGround.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        background_actionPerformed();
      }
    });
    colourMenu.add(backGround);

    /*
     * add colour buttons to a group so their selection is
     * mutually exclusive (background colour is a separate option)
     */
    itemGroup.add(seqColour);
    itemGroup.add(chainColour);
    itemGroup.add(chargeColour);
    itemGroup.add(viewerColour);
  }

  /**
   * Construct menu items
   */
  protected void initMenus()
  {
    AAStructureBindingModel binding = getBinding();

    seqColour = new JRadioButtonMenuItem();
    seqColour.setText(MessageManager.getString("action.by_sequence"));
    seqColour.setName(ViewerColour.BySequence.name());
    seqColour.setSelected(binding.isColourBySequence());
    seqColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        seqColour_actionPerformed();
      }
    });

    chainColour = new JRadioButtonMenuItem();
    chainColour.setText(MessageManager.getString("action.by_chain"));
    chainColour.setName(ViewerColour.ByChain.name());
    chainColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        chainColour_actionPerformed();
      }
    });

    chargeColour = new JRadioButtonMenuItem();
    chargeColour.setText(MessageManager.getString("label.charge_cysteine"));
    chargeColour.setName(ViewerColour.ChargeCysteine.name());
    chargeColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        chargeColour_actionPerformed();
      }
    });

    viewerColour = new JRadioButtonMenuItem();
    viewerColour
            .setText(MessageManager.getString("label.colour_with_viewer"));
    viewerColour.setToolTipText(MessageManager
            .getString("label.let_viewer_manage_structure_colours"));
    viewerColour.setName(ViewerColour.ByViewer.name());
    viewerColour.setSelected(!binding.isColourBySequence());

    if (_colourwith == null)
    {
      _colourwith = new Vector<>();
    }
    if (_alignwith == null)
    {
      _alignwith = new Vector<>();
    }

    ViewSelectionMenu seqColourBy = new ViewSelectionMenu(
            MessageManager.getString("label.colour_by"), this, _colourwith,
            new ItemListener()
            {
              @Override
              public void itemStateChanged(ItemEvent e)
              {
                if (!seqColour.isSelected())
                {
                  seqColour.doClick();
                }
                else
                {
                  // update the viewer display now.
                  seqColour_actionPerformed();
                }
              }
            });
    viewMenu.add(seqColourBy);

    final ItemListener handler = new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent e)
      {
        if (_alignwith.isEmpty())
        {
          alignStructs.setEnabled(false);
          alignStructs.setToolTipText(null);
        }
        else
        {
          alignStructs.setEnabled(true);
          alignStructs.setToolTipText(MessageManager.formatMessage(
                  "label.align_structures_using_linked_alignment_views",
                  _alignwith.size()));
        }
      }
    };
    viewSelectionMenu = new ViewSelectionMenu(
            MessageManager.getString("label.superpose_with"), this,
            _alignwith, handler);
    handler.itemStateChanged(null);
    viewerActionMenu.add(viewSelectionMenu, 0);
    viewerActionMenu.addMenuListener(new MenuListener()
    {
      @Override
      public void menuSelected(MenuEvent e)
      {
        handler.itemStateChanged(null);
      }

      @Override
      public void menuDeselected(MenuEvent e)
      {
      }

      @Override
      public void menuCanceled(MenuEvent e)
      {
      }
    });

    viewerActionMenu.setText(getViewerName());
    helpItem.setText(MessageManager.formatMessage("label.viewer_help",
            getViewerName()));

    buildColourMenu();
  }

  /**
   * Sends commands to the structure viewer to superimpose structures based on
   * currently associated alignments. May optionally return an error message for
   * the operation.
   */
  @Override
  protected String alignStructsWithAllAlignPanels()
  {
    if (getAlignmentPanel() == null)
    {
      return null;
    }

    if (_alignwith.size() == 0)
    {
      _alignwith.add(getAlignmentPanel());
    }

    String reply = null;
    try
    {
      reply = getBinding().superposeStructures(_alignwith);
      if (reply != null && !reply.isEmpty())
      {
        String text = MessageManager
                .formatMessage("error.superposition_failed", reply);
        statusBar.setText(text);
      }
    } catch (Exception e)
    {
      StringBuffer sp = new StringBuffer();
      for (AlignmentViewPanel alignPanel : _alignwith)
      {
        sp.append("'" + alignPanel.getViewName() + "' ");
      }
      Console.info("Couldn't align structures with the " + sp.toString()
              + "associated alignment panels.", e);
    }
    return reply;
  }

  /**
   * Opens a colour chooser dialog, and applies the chosen colour to the
   * background of the structure viewer
   */
  @Override
  public void background_actionPerformed()
  {
    String ttl = MessageManager.getString("label.select_background_colour");
    ColourChooserListener listener = new ColourChooserListener()
    {
      @Override
      public void colourSelected(Color c)
      {
        getBinding().setBackgroundColour(c);
      }
    };
    JalviewColourChooser.showColourChooser(this, ttl, null, listener);
  }

  @Override
  public void viewerColour_actionPerformed()
  {
    if (viewerColour.isSelected())
    {
      // disable automatic sequence colouring.
      getBinding().setColourBySequence(false);
    }
  }

  @Override
  public void chainColour_actionPerformed()
  {
    chainColour.setSelected(true);
    getBinding().colourByChain();
  }

  @Override
  public void chargeColour_actionPerformed()
  {
    chargeColour.setSelected(true);
    getBinding().colourByCharge();
  }

  @Override
  public void seqColour_actionPerformed()
  {
    AAStructureBindingModel binding = getBinding();
    binding.setColourBySequence(seqColour.isSelected());
    if (_colourwith == null)
    {
      _colourwith = new Vector<>();
    }
    if (binding.isColourBySequence())
    {
      if (!binding.isLoadingFromArchive())
      {
        if (_colourwith.size() == 0 && getAlignmentPanel() != null)
        {
          // Make the currently displayed alignment panel the associated view
          _colourwith.add(getAlignmentPanel().alignFrame.alignPanel);
        }
      }
      // Set the colour using the current view for the associated alignframe
      for (AlignmentViewPanel alignPanel : _colourwith)
      {
        binding.colourBySequence(alignPanel);
      }
      seqColoursApplied = true;
    }
  }

  @Override
  public void pdbFile_actionPerformed()
  {
    // TODO: JAL-3048 not needed for Jalview-JS - save PDB file
    JalviewFileChooser chooser = new JalviewFileChooser(
            Cache.getProperty("LAST_DIRECTORY"));

    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(MessageManager.getString("label.save_pdb_file"));
    chooser.setToolTipText(MessageManager.getString("action.save"));

    int value = chooser.showSaveDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      BufferedReader in = null;
      try
      {
        // TODO: cope with multiple PDB files in view
        in = new BufferedReader(
                new FileReader(getBinding().getStructureFiles()[0]));
        File outFile = chooser.getSelectedFile();

        PrintWriter out = new PrintWriter(new FileOutputStream(outFile));
        String data;
        while ((data = in.readLine()) != null)
        {
          if (!(data.indexOf("<PRE>") > -1 || data.indexOf("</PRE>") > -1))
          {
            out.println(data);
          }
        }
        out.close();
      } catch (Exception ex)
      {
        ex.printStackTrace();
      } finally
      {
        if (in != null)
        {
          try
          {
            in.close();
          } catch (IOException e)
          {
            // ignore
          }
        }
      }
    }
  }

  @Override
  public void viewMapping_actionPerformed()
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer();
    try
    {
      cap.appendText(getBinding().printMappings());
    } catch (OutOfMemoryError e)
    {
      new OOMWarning(
              "composing sequence-structure alignments for display in text box.",
              e);
      cap.dispose();
      return;
    }
    Desktop.addInternalFrame(cap,
            MessageManager.getString("label.pdb_sequence_mapping"), 550,
            600);
  }

  protected abstract String getViewerName();

  /**
   * Configures the title and menu items of the viewer panel.
   */
  @Override
  public void updateTitleAndMenus()
  {
    AAStructureBindingModel binding = getBinding();
    if (binding.hasFileLoadingError())
    {
      repaint();
      return;
    }
    setChainMenuItems(binding.getChainNames());

    this.setTitle(binding.getViewerTitle(getViewerName(), true));

    /*
     * enable 'Superpose with' if more than one mapped structure
     */
    viewSelectionMenu.setEnabled(false);
    if (getBinding().getMappedStructureCount() > 1
            && getBinding().getSequence().length > 1)
    {
      viewSelectionMenu.setEnabled(true);
    }

    /*
     * Show action menu if it has any enabled items
     */
    viewerActionMenu.setVisible(false);
    for (int i = 0; i < viewerActionMenu.getItemCount(); i++)
    {
      if (viewerActionMenu.getItem(i).isEnabled())
      {
        viewerActionMenu.setVisible(true);
        break;
      }
    }

    if (!binding.isLoadingFromArchive())
    {
      seqColour_actionPerformed();
    }
  }

  @Override
  public String toString()
  {
    return getTitle();
  }

  @Override
  public boolean hasMapping()
  {
    if (worker != null && (addingStructures || _started))
    {
      return false;
    }
    if (getBinding() == null)
    {
      if (_aps == null || _aps.size() == 0)
      {
        // viewer has been closed, but we did at some point run.
        return true;
      }
      return false;
    }
    String[] pdbids = getBinding().getStructureFiles();
    if (pdbids == null)
    {
      return false;
    }
    int p = 0;
    for (String pdbid : pdbids)
    {
      StructureMapping sm[] = getBinding().getSsm().getMapping(pdbid);
      if (sm != null && sm.length > 0 && sm[0] != null)
      {
        p++;
      }
    }
    // only return true if there is a mapping for every structure file we have
    // loaded
    if (p == 0 || p != pdbids.length)
    {
      return false;
    }
    // and that coloring has been applied
    return seqColoursApplied;
  }

  @Override
  public void raiseViewer()
  {
    toFront();
  }

  @Override
  public long startProgressBar(String msg)
  {
    // TODO would rather have startProgress/stopProgress as the
    // IProgressIndicator interface
    long tm = random.nextLong();
    if (progressBar != null)
    {
      progressBar.setProgressBar(msg, tm);
    }
    return tm;
  }

  @Override
  public void stopProgressBar(String msg, long handle)
  {
    if (progressBar != null)
    {
      progressBar.setProgressBar(msg, handle);
    }
  }

  protected IProgressIndicator getProgressIndicator()
  {
    return progressBar;
  }

  protected void setProgressIndicator(IProgressIndicator pi)
  {
    progressBar = pi;
  }

  public void setProgressMessage(String message, long id)
  {
    if (progressBar != null)
    {
      progressBar.setProgressBar(message, id);
    }
  }

  @Override
  public void showConsole(boolean show)
  {
    // default does nothing
  }

  /**
   * Show only the selected chain(s) in the viewer
   */
  protected void showSelectedChains()
  {
    List<String> toshow = new ArrayList<>();
    for (int i = 0; i < chainMenu.getItemCount(); i++)
    {
      if (chainMenu.getItem(i) instanceof JCheckBoxMenuItem)
      {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem) chainMenu.getItem(i);
        if (item.isSelected())
        {
          toshow.add(item.getText());
        }
      }
    }
    getBinding().showChains(toshow);
  }

  /**
   * Tries to fetch a PDB file and save to a temporary local file. Returns the
   * saved file path if successful, or null if not.
   * 
   * @param processingEntry
   * @return
   */
  protected String fetchPdbFile(PDBEntry processingEntry)
  {
    String filePath = null;
    Pdb pdbclient = new Pdb();
    EBIAlfaFold afclient = new EBIAlfaFold();
    AlignmentI pdbseq = null;
    String pdbid = processingEntry.getId();
    long handle = System.currentTimeMillis()
            + Thread.currentThread().hashCode();

    /*
     * Write 'fetching PDB' progress on AlignFrame as we are not yet visible
     */
    String msg = MessageManager.formatMessage("status.fetching_pdb",
            new Object[]
            { pdbid });
    getAlignmentPanel().alignFrame.setProgressBar(msg, handle);
    // long hdl = startProgressBar(MessageManager.formatMessage(
    // "status.fetching_pdb", new Object[]
    // { pdbid }));
    try
    {
      if (afclient.isValidReference(pdbid))
      {
        pdbseq = afclient.getSequenceRecords(pdbid,
                processingEntry.getRetrievalUrl());
      }
      else
      {
        if (processingEntry.hasRetrievalUrl())
        {
          String safePDBId = java.net.URLEncoder.encode(pdbid, "UTF-8")
                  .replace("%", "__");

          // retrieve from URL to new local tmpfile
          File tmpFile = File.createTempFile(safePDBId,
                  "." + (PDBEntry.Type.MMCIF.toString().equals(
                          processingEntry.getType().toString()) ? "cif"
                                  : "pdb"));
          String fromUrl = processingEntry.getRetrievalUrl();
          UrlDownloadClient.download(fromUrl, tmpFile);

          // may not need this check ?
          String file = tmpFile.getAbsolutePath();
          if (file != null)
          {
            pdbseq = EBIAlfaFold.importDownloadedStructureFromUrl(fromUrl,
                    tmpFile, pdbid, null, null, null);
          }
        }
        else
        {
          pdbseq = pdbclient.getSequenceRecords(pdbid);
        }
      }
    } catch (Exception e)
    {
      System.err.println(
              "Error retrieving PDB id " + pdbid + ": " + e.getMessage());
    } finally
    {
      msg = pdbid + " " + MessageManager.getString("label.state_completed");
      getAlignmentPanel().alignFrame.setProgressBar(msg, handle);
      // stopProgressBar(msg, hdl);
    }
    /*
     * If PDB data were saved and are not invalid (empty alignment), return the
     * file path.
     */
    if (pdbseq != null && pdbseq.getHeight() > 0)
    {
      // just use the file name from the first sequence's first PDBEntry
      filePath = new File(pdbseq.getSequenceAt(0).getAllPDBEntries()
              .elementAt(0).getFile()).getAbsolutePath();
      processingEntry.setFile(filePath);
    }
    return filePath;
  }

  /**
   * If supported, saves the state of the structure viewer to a temporary file
   * and returns the file, else returns null
   * 
   * @return
   */
  public File saveSession()
  {
    if (getBinding() == null)
    {
      return null;
    }
    File session = getBinding().saveSession();
    long l = session.length();
    int wait = 50;
    do
    {
      try
      {
        Thread.sleep(5);
      } catch (InterruptedException e)
      {
      }
      long nextl = session.length();
      if (nextl != l)
      {
        wait = 50;
        l = nextl;
      }
    } while (--wait > 0);
    return session;
  }

  private static boolean quitClose = false;

  public static void setQuitClose(boolean b)
  {
    quitClose = b;
  }

  @Override
  public boolean stillRunning()
  {
    AAStructureBindingModel binding = getBinding();
    return binding != null && binding.isViewerRunning();
  }

  /**
   * Close down this instance of Jalview's Chimera viewer, giving the user the
   * option to close the associated Chimera window (process). They may wish to
   * keep it open until they have had an opportunity to save any work.
   * 
   * @param forceClose
   *          if true, close any linked Chimera process; if false, prompt first
   */
  @Override
  public void closeViewer(boolean forceClose)
  {
    AAStructureBindingModel binding = getBinding();
    if (stillRunning())
    {
      if (!forceClose)
      {
        String viewerName = getViewerName();

        int confirm = JvOptionPane.CANCEL_OPTION;
        if (QuitHandler.quitting())
        {
          // already asked about closing external windows
          confirm = quitClose ? JvOptionPane.YES_OPTION
                  : JvOptionPane.NO_OPTION;
        }
        else
        {
          String prompt = MessageManager
                  .formatMessage("label.confirm_close_viewer", new Object[]
                  { binding.getViewerTitle(viewerName, false),
                      viewerName });
          prompt = JvSwingUtils.wrapTooltip(true, prompt);
          String title = MessageManager.getString("label.close_viewer");
          confirm = showCloseDialog(title, prompt);
        }

        /*
         * abort closure if user hits escape or Cancel
         */
        if (confirm == JvOptionPane.CANCEL_OPTION
                || confirm == JvOptionPane.CLOSED_OPTION)
        {
          // abort possible quit handling if CANCEL chosen
          if (confirm == JvOptionPane.CANCEL_OPTION)
          {
            try
            {
              // this is a bit futile
              this.setClosed(false);
            } catch (PropertyVetoException e)
            {
            }
            QuitHandler.abortQuit();
          }
          return;
        }
        forceClose = confirm == JvOptionPane.YES_OPTION;
      }
    }
    if (binding != null)
    {
      binding.closeViewer(forceClose);
    }
    setAlignmentPanel(null);
    _aps.clear();
    _alignwith.clear();
    _colourwith.clear();
    // TODO: check for memory leaks where instance isn't finalised because jmb
    // holds a reference to the window
    // jmb = null;
    dispose();
  }

  private int showCloseDialog(final String title, final String prompt)
  {
    int confirmResponse = JvOptionPane.CANCEL_OPTION;
    confirmResponse = JvOptionPane.showConfirmDialog(this, prompt,
            MessageManager.getString("label.close_viewer"),
            JvOptionPane.YES_NO_CANCEL_OPTION,
            JvOptionPane.WARNING_MESSAGE);
    return confirmResponse;
  }

  @Override
  public void showHelp_actionPerformed()
  {
    /*
    try
    {
    */
    String url = getBinding().getHelpURL();
    if (url != null)
    {
      BrowserLauncher.openURL(url);
    }
    /* 
    }
    catch (IOException ex)
    {
      System.err
              .println("Show " + getViewerName() + " failed with: "
                      + ex.getMessage());
    }
    */
  }

  @Override
  public boolean hasViewerActionsMenu()
  {
    return viewerActionMenu != null && viewerActionMenu.isEnabled()
            && viewerActionMenu.getItemCount() > 0
            && viewerActionMenu.isVisible();
  }

}
