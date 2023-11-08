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

import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.jibble.epsgraphics.EpsGraphics2D;

import jalview.analysis.AlignmentSorter;
import jalview.analysis.AverageDistanceTree;
import jalview.analysis.NJTree;
import jalview.analysis.TreeBuilder;
import jalview.analysis.TreeModel;
import jalview.analysis.scoremodels.ScoreModels;
import jalview.api.analysis.ScoreModelI;
import jalview.api.analysis.SimilarityParamsI;
import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.commands.CommandI;
import jalview.commands.OrderCommand;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.BinaryNode;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.NodeTransformI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.datamodel.SequenceNode;
import jalview.gui.ImageExporter.ImageWriterI;
import jalview.io.JalviewFileChooser;
import jalview.io.JalviewFileView;
import jalview.io.NewickFile;
import jalview.io.exceptions.ImageOutputException;
import jalview.jbgui.GTreePanel;
import jalview.util.ImageMaker.TYPE;
import jalview.util.MessageManager;
import jalview.viewmodel.AlignmentViewport;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class TreePanel extends GTreePanel
{
  String treeType;

  String scoreModelName; // if tree computed

  String treeTitle; // if tree loaded

  SimilarityParamsI similarityParams;

  private TreeCanvas treeCanvas;

  TreeModel tree;

  private AlignViewport av;

  /**
   * Creates a new TreePanel object.
   * 
   * @param ap
   * @param type
   * @param modelName
   * @param options
   */
  public TreePanel(AlignmentPanel ap, String type, String modelName,
          SimilarityParamsI options)
  {
    super();
    this.setFrameIcon(null);
    this.similarityParams = options;
    initTreePanel(ap, type, modelName, null, null);

    // We know this tree has distances. JBPNote TODO: prolly should add this as
    // a userdefined default
    // showDistances(true);
  }

  public TreePanel(AlignmentPanel alignPanel, NewickFile newtree,
          String theTitle, AlignmentView inputData)
  {
    super();
    this.setFrameIcon(null);
    this.treeTitle = theTitle;
    initTreePanel(alignPanel, null, null, newtree, inputData);
  }

  /**
   * columnwise tree associated with positions in aa
   * 
   * @param alignPanel
   * @param fin
   * @param title
   * @param aa
   */
  public TreePanel(AlignmentPanel alignPanel, NewickFile fin,
          AlignmentAnnotation aa, String title)
  {
    super();
    columnWise = true;
    assocAnnotation = aa;
    this.setFrameIcon(null);
    this.treeTitle = title;
    initTreePanel(alignPanel, null, null, fin, null);
  }

  boolean columnWise = false;

  AlignmentAnnotation assocAnnotation = null;

  public boolean isColumnWise()
  {
    return columnWise;
  }

  public AlignmentAnnotation getAssocAnnotation()
  {
    return assocAnnotation;
  }

  public AlignmentI getAlignment()
  {
    return getTreeCanvas().getViewport().getAlignment();
  }

  public AlignmentViewport getViewPort()
  {
    // @Mungo - Why don't we return our own viewport ???
    return getTreeCanvas().getViewport();
  }

  void initTreePanel(AlignmentPanel ap, String type, String modelName,
          NewickFile newTree, AlignmentView inputData)
  {

    av = ap.av;
    this.treeType = type;
    this.scoreModelName = modelName;

    treeCanvas = new TreeCanvas(this, ap, scrollPane);
    scrollPane.setViewportView(treeCanvas);
    
    if (columnWise)
    {
      bootstrapMenu.setVisible(false);
      placeholdersMenu.setState(false);
      placeholdersMenu.setVisible(false);
      fitToWindow.setState(false);
      sortAssocViews.setVisible(false);
    }


    addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent e)
      {
        switch (e.getKeyCode())
        { 
        case 27: // escape
          treeCanvas.clearSelectedLeaves();
          e.consume();
          break;
          
        }
        
      }
    });
    PaintRefresher.Register(this, ap.av.getSequenceSetId());

    buildAssociatedViewMenu();

    final PropertyChangeListener listener = addAlignmentListener();

    /*
     * remove listener when window is closed, so that this
     * panel can be garbage collected
     */
    addInternalFrameListener(new InternalFrameAdapter()
    {
      @Override
      public void internalFrameClosed(InternalFrameEvent evt)
      {
        if (av != null)
        {
          av.removePropertyChangeListener(listener);
        }
        releaseReferences();
      }
    });

    TreeLoader tl = new TreeLoader(newTree, inputData);
    tl.start();

  }

  /**
   * Ensure any potentially large object references are nulled
   */
  public void releaseReferences()
  {
    this.tree = null;
    this.treeCanvas.tree = null;
    this.treeCanvas.nodeHash = null;
    this.treeCanvas.nameHash = null;
  }

  /**
   * @return
   */
  protected PropertyChangeListener addAlignmentListener()
  {
    final PropertyChangeListener listener = new PropertyChangeListener()
    {
      @Override
      public void propertyChange(PropertyChangeEvent evt)
      {
        if (evt.getPropertyName().equals("alignment"))
        {
          if (tree == null)
          {
            System.out.println("tree is null");
            // TODO: deal with case when a change event is received whilst a
            // tree is still being calculated - should save reference for
            // processing message later.
            return;
          }
          if (evt.getNewValue() == null)
          {
            System.out.println(
                    "new alignment sequences vector value is null");
          }

          tree.updatePlaceHolders((List<SequenceI>) evt.getNewValue());
          treeCanvas.nameHash.clear(); // reset the mapping between canvas
          // rectangles and leafnodes
          repaint();
        }
      }
    };
    av.addPropertyChangeListener(listener);
    return listener;
  }

  @Override
  public void viewMenu_menuSelected()
  {
    buildAssociatedViewMenu();
  }

  void buildAssociatedViewMenu()
  {
    AlignmentPanel[] aps = PaintRefresher
            .getAssociatedPanels(av.getSequenceSetId());
    if (aps.length == 1 && getTreeCanvas().getAssociatedPanel() == aps[0])
    {
      associateLeavesMenu.setVisible(false);
      return;
    }

    associateLeavesMenu.setVisible(true);

    if ((viewMenu
            .getItem(viewMenu.getItemCount() - 2) instanceof JMenuItem))
    {
      viewMenu.insertSeparator(viewMenu.getItemCount() - 1);
    }

    associateLeavesMenu.removeAll();

    JRadioButtonMenuItem item;
    ButtonGroup buttonGroup = new ButtonGroup();
    int i, iSize = aps.length;
    final TreePanel thisTreePanel = this;
    for (i = 0; i < iSize; i++)
    {
      final AlignmentPanel ap = aps[i];
      item = new JRadioButtonMenuItem(ap.av.getViewName(),
              ap == treeCanvas.getAssociatedPanel());
      buttonGroup.add(item);
      item.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent evt)
        {
          treeCanvas.applyToAllViews = false;
          treeCanvas.setAssociatedPanel(ap);
          treeCanvas.setViewport(ap.av);
          PaintRefresher.Register(thisTreePanel, ap.av.getSequenceSetId());
        }
      });

      associateLeavesMenu.add(item);
    }

    final JRadioButtonMenuItem itemf = new JRadioButtonMenuItem(
            MessageManager.getString("label.all_views"));
    buttonGroup.add(itemf);
    itemf.setSelected(treeCanvas.applyToAllViews);
    itemf.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent evt)
      {
        treeCanvas.applyToAllViews = itemf.isSelected();
      }
    });
    associateLeavesMenu.add(itemf);

  }

  class TreeLoader extends Thread
  {
    private NewickFile newtree;

    private AlignmentView odata = null;

    public TreeLoader(NewickFile newickFile, AlignmentView inputData)
    {
      this.newtree = newickFile;
      this.odata = inputData;

      if (newickFile != null)
      {
        // Must be outside run(), as Jalview2XML tries to
        // update distance/bootstrap visibility at the same time
        showBootstrap(newickFile.HasBootstrap());
        showDistances(newickFile.HasDistances());
      }
    }

    @Override
    public void run()
    {

      if (newtree != null)
      {
        tree = new TreeModel(av.getAlignment().getSequencesArray(), odata,
                newtree);
        if (tree.getOriginalData() == null)
        {
          originalSeqData.setVisible(false);
        }
      }
      else
      {
        ScoreModelI sm = ScoreModels.getInstance().getScoreModel(
                scoreModelName, treeCanvas.getAssociatedPanel());
        TreeBuilder njtree = treeType.equals(TreeBuilder.NEIGHBOUR_JOINING)
                ? new NJTree(av, sm, similarityParams)
                : new AverageDistanceTree(av, sm, similarityParams);
        tree = new TreeModel(njtree);
        // don't display distances for columnwise trees        
      }
      showDistances(!columnWise);
      tree.reCount(tree.getTopNode());
      tree.findHeight(tree.getTopNode());
      treeCanvas.setTree(tree);
      treeCanvas.repaint();
      av.setCurrentTree(tree);
      if (av.getSortByTree())
      {
        sortByTree_actionPerformed();
      }
    }
  }

  public void showDistances(boolean b)
  {
    treeCanvas.setShowDistances(b);
    distanceMenu.setSelected(b);
  }

  public void showBootstrap(boolean b)
  {
    treeCanvas.setShowBootstrap(b);
    bootstrapMenu.setSelected(b);
  }

  public void showPlaceholders(boolean b)
  {
    placeholdersMenu.setState(b);
    treeCanvas.setMarkPlaceholders(b);
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public TreeModel getTree()
  {
    return tree;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void textbox_actionPerformed(ActionEvent e)
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer();

    String newTitle = getPanelTitle();

    NewickFile fout = new NewickFile(tree.getTopNode());
    try
    {
      cap.setText(fout.print(tree.hasBootstrap(), tree.hasDistances(),
              tree.hasRootDistance()));
      Desktop.addInternalFrame(cap, newTitle, 500, 100);
    } catch (OutOfMemoryError oom)
    {
      new OOMWarning("generating newick tree file", oom);
      cap.dispose();
    }

  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void saveAsNewick_actionPerformed(ActionEvent e)
  {
    // TODO: JAL-3048 save newick file for Jalview-JS
    JalviewFileChooser chooser = new JalviewFileChooser(
            Cache.getProperty("LAST_DIRECTORY"));
    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(
            MessageManager.getString("label.save_tree_as_newick"));
    chooser.setToolTipText(MessageManager.getString("action.save"));

    int value = chooser.showSaveDialog(null);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      String choice = chooser.getSelectedFile().getPath();
      Cache.setProperty("LAST_DIRECTORY",
              chooser.getSelectedFile().getParent());

      try
      {
        jalview.io.NewickFile fout = new jalview.io.NewickFile(
                tree.getTopNode());
        String output = fout.print(tree.hasBootstrap(), tree.hasDistances(),
                tree.hasRootDistance());
        java.io.PrintWriter out = new java.io.PrintWriter(
                new java.io.FileWriter(choice));
        out.println(output);
        out.close();
      } catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void printMenu_actionPerformed(ActionEvent e)
  {
    // Putting in a thread avoids Swing painting problems
    treeCanvas.startPrinting();
  }

  @Override
  public void originalSeqData_actionPerformed(ActionEvent e)
  {
    AlignmentView originalData = tree.getOriginalData();
    if (originalData == null)
    {
      Console.info(
              "Unexpected call to originalSeqData_actionPerformed - should have hidden this menu action.");
      return;
    }
    // decide if av alignment is sufficiently different to original data to
    // warrant a new window to be created
    // create new alignmnt window with hidden regions (unhiding hidden regions
    // yields unaligned seqs)
    // or create a selection box around columns in alignment view
    // test Alignment(SeqCigar[])
    char gc = '-';
    try
    {
      // we try to get the associated view's gap character
      // but this may fail if the view was closed...
      gc = av.getGapCharacter();

    } catch (Exception ex)
    {
    }

    Object[] alAndColsel = originalData.getAlignmentAndHiddenColumns(gc);

    if (alAndColsel != null && alAndColsel[0] != null)
    {
      // AlignmentOrder origorder = new AlignmentOrder(alAndColsel[0]);

      AlignmentI al = new Alignment((SequenceI[]) alAndColsel[0]);
      AlignmentI dataset = (av != null && av.getAlignment() != null)
              ? av.getAlignment().getDataset()
              : null;
      if (dataset != null)
      {
        al.setDataset(dataset);
      }

      if (true)
      {
        // make a new frame!
        AlignFrame af = new AlignFrame(al, (HiddenColumns) alAndColsel[1],
                AlignFrame.DEFAULT_WIDTH, AlignFrame.DEFAULT_HEIGHT);

        // >>>This is a fix for the moment, until a better solution is
        // found!!<<<
        // af.getFeatureRenderer().transferSettings(alignFrame.getFeatureRenderer());

        // af.addSortByOrderMenuItem(ServiceName + " Ordering",
        // msaorder);

        Desktop.addInternalFrame(af, MessageManager.formatMessage(
                "label.original_data_for_params", new Object[]
                { this.title }), AlignFrame.DEFAULT_WIDTH,
                AlignFrame.DEFAULT_HEIGHT);
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void fitToWindow_actionPerformed(ActionEvent e)
  {
    treeCanvas.fitToWindow = fitToWindow.isSelected();
    repaint();
  }

  /**
   * sort the associated alignment view by the current tree.
   * 
   * @param e
   */
  @Override
  public void sortByTree_actionPerformed()
  {

    if (treeCanvas.applyToAllViews)
    {
      final ArrayList<CommandI> commands = new ArrayList<>();
      for (AlignmentPanel ap : PaintRefresher
              .getAssociatedPanels(av.getSequenceSetId()))
      {
        commands.add(sortAlignmentIn(ap.av.getAlignPanel()));
      }
      av.getAlignPanel().alignFrame.addHistoryItem(new CommandI()
      {

        @Override
        public void undoCommand(AlignmentI[] views)
        {
          for (CommandI tsort : commands)
          {
            tsort.undoCommand(views);
          }
        }

        @Override
        public int getSize()
        {
          return commands.size();
        }

        @Override
        public String getDescription()
        {
          return "Tree Sort (many views)";
        }

        @Override
        public void doCommand(AlignmentI[] views)
        {

          for (CommandI tsort : commands)
          {
            tsort.doCommand(views);
          }
        }
      });
      for (AlignmentPanel ap : PaintRefresher
              .getAssociatedPanels(av.getSequenceSetId()))
      {
        // ensure all the alignFrames refresh their GI after adding an undo item
        ap.alignFrame.updateEditMenuBar();
      }
    }
    else
    {
      treeCanvas.getAssociatedPanel().alignFrame.addHistoryItem(
              sortAlignmentIn(treeCanvas.getAssociatedPanel()));
    }

  }

  public CommandI sortAlignmentIn(AlignmentPanel ap)
  {
    // TODO: move to alignment view controller
    AlignmentViewport viewport = ap.av;
    SequenceI[] oldOrder = viewport.getAlignment().getSequencesArray();
    AlignmentSorter.sortByTree(viewport.getAlignment(), tree);
    CommandI undo;
    undo = new OrderCommand("Tree Sort", oldOrder, viewport.getAlignment());

    ap.paintAlignment(true, false);
    return undo;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void font_actionPerformed(ActionEvent e)
  {
    if (treeCanvas == null)
    {
      return;
    }

    new FontChooser(this);
  }

  public Font getTreeFont()
  {
    return treeCanvas.font;
  }

  public void setTreeFont(Font f)
  {
    if (treeCanvas != null)
    {
      treeCanvas.setFont(f);
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void distanceMenu_actionPerformed(ActionEvent e)
  {
    treeCanvas.setShowDistances(distanceMenu.isSelected());
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void bootstrapMenu_actionPerformed(ActionEvent e)
  {
    treeCanvas.setShowBootstrap(bootstrapMenu.isSelected());
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void placeholdersMenu_actionPerformed(ActionEvent e)
  {
    treeCanvas.setMarkPlaceholders(placeholdersMenu.isSelected());
  }

  /**
   * Outputs the Tree in image format (currently EPS or PNG). The user is
   * prompted for the file to save to, and for EPS (unless a preference is
   * already set) for the choice of Text or Lineart for character rendering.
   */
  @Override
  public void writeTreeImage(TYPE imageFormat)
  {
    int width = treeCanvas.getWidth();
    int height = treeCanvas.getHeight();
    ImageWriterI writer = new ImageWriterI()
    {
      @Override
      public void exportImage(Graphics g) throws Exception
      {
        treeCanvas.draw(g, width, height);
      }
    };
    String tree = MessageManager.getString("label.tree");
    ImageExporter exporter = new ImageExporter(writer, null, imageFormat,
            tree);
    try {
    exporter.doExport(null, this, width, height,
            tree.toLowerCase(Locale.ROOT));
    } catch (ImageOutputException ioex) {
      Console.error("Unexpected error whilst writing "+imageFormat.toString(),ioex);
    }
  }

  /**
   * change node labels to the annotation referred to by labelClass TODO:
   * promote to a datamodel modification that can be undone TODO: make argument
   * one case of a generic transformation function ie { undoStep = apply(Tree,
   * TransformFunction)};
   * 
   * @param labelClass
   */
  public void changeNames(final String labelClass)
  {
    tree.applyToNodes(new NodeTransformI()
    {

      @Override
      public void transform(BinaryNode node)
      {
        if (node instanceof SequenceNode
                && !((SequenceNode) node).isPlaceholder()
                && !((SequenceNode) node).isDummy())
        {
          String newname = null;
          SequenceI sq = (SequenceI) ((BinaryNode) node).element();
          if (sq != null)
          {
            // search dbrefs, features and annotation
            List<DBRefEntry> refs = jalview.util.DBRefUtils
                    .selectRefs(sq.getDBRefs(), new String[]
                    { labelClass.toUpperCase(Locale.ROOT) });
            if (refs != null)
            {
              for (int i = 0, ni = refs.size(); i < ni; i++)
              {
                if (newname == null)
                {
                  newname = new String(refs.get(i).getAccessionId());
                }
                else
                {
                  newname += "; " + refs.get(i).getAccessionId();
                }
              }
            }
            if (newname == null)
            {
              List<SequenceFeature> features = sq.getFeatures()
                      .getPositionalFeatures(labelClass);
              for (SequenceFeature feature : features)
              {
                if (newname == null)
                {
                  newname = feature.getDescription();
                }
                else
                {
                  newname = newname + "; " + feature.getDescription();
                }
              }
            }
          }
          if (newname != null)
          {
            // String oldname = ((SequenceNode) node).getName();
            // TODO : save oldname in the undo object for this modification.
            ((BinaryNode) node).setName(newname);
          }
        }
      }
    });
  }

  /**
   * Formats a localised title for the tree panel, like
   * <p>
   * Neighbour Joining Using BLOSUM62
   * <p>
   * For a tree loaded from file, just uses the file name
   * 
   * @return
   */
  public String getPanelTitle()
  {
    if (treeTitle != null)
    {
      return treeTitle;
    }

    /*
     * i18n description of Neighbour Joining or Average Distance method
     */
    String treecalcnm = MessageManager.getString(
            "label.tree_calc_" + treeType.toLowerCase(Locale.ROOT));

    /*
     * short score model name (long description can be too long)
     */
    String smn = scoreModelName;

    /*
     * put them together as <method> Using <model>
     */
    final String ttl = MessageManager.formatMessage("label.calc_title",
            treecalcnm, smn);
    return ttl;
  }

  /**
   * Builds an EPS image and writes it to the specified file.
   * 
   * @param outFile
   * @param textOption
   *          true for Text character rendering, false for Lineart
   */
  protected void writeEpsFile(File outFile, boolean textOption)
  {
    try
    {
      int width = treeCanvas.getWidth();
      int height = treeCanvas.getHeight();

      FileOutputStream out = new FileOutputStream(outFile);
      EpsGraphics2D pg = new EpsGraphics2D("Tree", out, 0, 0, width,
              height);
      pg.setAccurateTextMode(!textOption);
      treeCanvas.draw(pg, width, height);

      pg.flush();
      pg.close();
    } catch (Exception ex)
    {
      System.err.println("Error writing tree as EPS");
      ex.printStackTrace();
    }
  }

  public AlignViewport getViewport()
  {
    return av;
  }

  public void setViewport(AlignViewport av)
  {
    this.av = av;
  }

  public TreeCanvas getTreeCanvas()
  {
    return treeCanvas;
  }
}
