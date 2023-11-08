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

import jalview.analysis.AverageDistanceTree;
import jalview.analysis.NJTree;
import jalview.analysis.TreeBuilder;
import jalview.analysis.TreeModel;
import jalview.analysis.scoremodels.ScoreModels;
import jalview.analysis.scoremodels.SimilarityParams;
import jalview.api.analysis.ScoreModelI;
import jalview.datamodel.Alignment;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.SequenceI;
import jalview.io.NewickFile;
import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class TreePanel extends EmbmenuFrame
        implements ActionListener, ItemListener, AutoCloseable
{
  SequenceI[] seq;

  String type;

  String pwtype;

  int start;

  int end;

  TreeCanvas treeCanvas;

  TreeModel tree;

  AlignmentPanel ap;

  AlignViewport av;

  public TreeModel getTree()
  {
    return tree;
  }

  @Override
  public void close()
  {
    ap = null;
    av = null;
    super.close();
  }

  /**
   * Creates a new TreePanel object.
   */
  public TreePanel(AlignmentPanel alignPanel, String type, String pwtype)
  {
    try
    {
      jbInit();
      this.setMenuBar(jMenuBar1);
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }

    initTreePanel(alignPanel, type, pwtype, null);
  }

  /**
   * Creates a new TreePanel object.
   * 
   */
  public TreePanel(AlignmentPanel ap, String type, String pwtype,
          NewickFile newtree)
  {
    try
    {
      jbInit();
      this.setMenuBar(jMenuBar1);
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    initTreePanel(ap, type, pwtype, newtree);
  }

  void initTreePanel(AlignmentPanel ap, String type, String pwtype,
          NewickFile newTree)
  {

    this.ap = ap;
    this.av = ap.av;
    this.type = type;
    this.pwtype = pwtype;

    treeCanvas = new TreeCanvas(ap, scrollPane);
    TreeLoader tl = new TreeLoader(newTree);
    tl.start();
    embedMenuIfNeeded(treeCanvas);
    scrollPane.add(treeCanvas, BorderLayout.CENTER);
  }

  void showOriginalData()
  {
    // decide if av alignment is sufficiently different to original data to
    // warrant a new window to be created
    // create new alignmnt window with hidden regions (unhiding hidden regions
    // yields unaligned seqs)
    // or create a selection box around columns in alignment view
    // test Alignment(SeqCigar[])
    if (tree.getOriginalData() != null)
    {
      char gc = '-';
      try
      {
        // we try to get the associated view's gap character
        // but this may fail if the view was closed...
        gc = av.getGapCharacter();
      } catch (Exception ex)
      {
      }

      Object[] alAndColsel = tree.getOriginalData()
              .getAlignmentAndHiddenColumns(gc);

      if (alAndColsel != null && alAndColsel[0] != null)
      {
        Alignment al = new Alignment((SequenceI[]) alAndColsel[0]);
        AlignFrame af = new AlignFrame(al, av.applet,
                "Original Data for Tree", false);

        af.viewport.getAlignment()
                .setHiddenColumns((HiddenColumns) alAndColsel[1]);
      }
    }
    else
    {
      System.out.println("Original Tree Data not available");
    }
  }

  class TreeLoader extends Thread
  {
    NewickFile newtree;

    jalview.datamodel.AlignmentView odata = null;

    public TreeLoader(NewickFile newtree)
    {
      this.newtree = newtree;
    }

    @Override
    public void run()
    {
      if (newtree != null)
      {
        tree = new TreeModel(av.getAlignment().getSequencesArray(), odata,
                newtree);
      }
      else
      {
        ScoreModelI sm1 = ScoreModels.getInstance().getScoreModel(pwtype,
                treeCanvas.ap);
        ScoreModelI sm = sm1;
        TreeBuilder njtree = type.equals(TreeBuilder.NEIGHBOUR_JOINING)
                ? new NJTree(av, sm, SimilarityParams.Jalview)
                : new AverageDistanceTree(av, sm, SimilarityParams.Jalview);
        tree = new TreeModel(njtree);
      }

      tree.reCount(tree.getTopNode());
      tree.findHeight(tree.getTopNode());
      treeCanvas.setTree(tree);
      if (newtree != null)
      {
        // Set default view, paying lip service to any overriding tree view
        // parameter settings
        boolean showDist = newtree.HasDistances()
                && av.applet.getDefaultParameter("showTreeDistances",
                        newtree.HasDistances());
        boolean showBoots = newtree.HasBootstrap()
                && av.applet.getDefaultParameter("showTreeBootstraps",
                        newtree.HasBootstrap());
        distanceMenu.setState(showDist);
        bootstrapMenu.setState(showBoots);
        treeCanvas.setShowBootstrap(showBoots);
        treeCanvas.setShowDistances(showDist);
        treeCanvas.setMarkPlaceholders(av.applet
                .getDefaultParameter("showUnlinkedTreeNodes", false));
      }

      treeCanvas.repaint();

      av.setCurrentTree(tree);

    }
  }

  @Override
  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == newickOutput)
    {
      newickOutput_actionPerformed();
    }
    else if (evt.getSource() == fontSize)
    {
      fontSize_actionPerformed();
    }
    else if (evt.getSource() == inputData)
    {
      showOriginalData();
    }
  }

  @Override
  public void itemStateChanged(ItemEvent evt)
  {
    if (evt.getSource() == fitToWindow)
    {
      treeCanvas.fitToWindow = fitToWindow.getState();
    }

    else if (evt.getSource() == distanceMenu)
    {
      treeCanvas.setShowDistances(distanceMenu.getState());
    }

    else if (evt.getSource() == bootstrapMenu)
    {
      treeCanvas.setShowBootstrap(bootstrapMenu.getState());
    }

    else if (evt.getSource() == placeholdersMenu)
    {
      treeCanvas.setMarkPlaceholders(placeholdersMenu.getState());
    }

    treeCanvas.repaint();
  }

  public void newickOutput_actionPerformed()
  {
    jalview.io.NewickFile fout = new jalview.io.NewickFile(
            tree.getTopNode());
    String output = fout.print(false, true);
    CutAndPasteTransfer cap = new CutAndPasteTransfer(false, null);
    cap.setText(output);
    java.awt.Frame frame = new java.awt.Frame();
    frame.add(cap);
    jalview.bin.JalviewLite.addFrame(frame, type + " " + pwtype, 500, 100);
  }

  public java.awt.Font getTreeFont()
  {
    return treeCanvas.font;
  }

  public void setTreeFont(java.awt.Font font)
  {
    treeCanvas.font = font;
    treeCanvas.repaint();
  }

  protected void fontSize_actionPerformed()
  {
    if (treeCanvas == null)
    {
      return;
    }

    new FontChooser(this);
  }

  BorderLayout borderLayout1 = new BorderLayout();

  protected ScrollPane scrollPane = new ScrollPane();

  MenuBar jMenuBar1 = new MenuBar();

  Menu jMenu2 = new Menu();

  protected MenuItem fontSize = new MenuItem();

  protected CheckboxMenuItem bootstrapMenu = new CheckboxMenuItem();

  protected CheckboxMenuItem distanceMenu = new CheckboxMenuItem();

  protected CheckboxMenuItem placeholdersMenu = new CheckboxMenuItem();

  protected CheckboxMenuItem fitToWindow = new CheckboxMenuItem();

  Menu fileMenu = new Menu();

  MenuItem newickOutput = new MenuItem();

  MenuItem inputData = new MenuItem();

  private void jbInit() throws Exception
  {
    setLayout(borderLayout1);
    this.setBackground(Color.white);
    this.setFont(new java.awt.Font("Verdana", 0, 12));
    jMenu2.setLabel(MessageManager.getString("action.view"));
    fontSize.setLabel(MessageManager.getString("action.font"));
    fontSize.addActionListener(this);
    bootstrapMenu.setLabel(
            MessageManager.getString("label.show_bootstrap_values"));
    bootstrapMenu.addItemListener(this);
    distanceMenu.setLabel(MessageManager.getString("label.show_distances"));
    distanceMenu.addItemListener(this);
    placeholdersMenu.setLabel(
            MessageManager.getString("label.mark_unassociated_leaves"));
    placeholdersMenu.addItemListener(this);
    fitToWindow.setState(true);
    fitToWindow.setLabel(MessageManager.getString("label.fit_to_window"));
    fitToWindow.addItemListener(this);
    fileMenu.setLabel(MessageManager.getString("action.file"));
    newickOutput.setLabel(MessageManager.getString("label.newick_format"));
    newickOutput.addActionListener(this);
    inputData.setLabel(MessageManager.getString("label.input_data"));

    add(scrollPane, BorderLayout.CENTER);
    jMenuBar1.add(fileMenu);
    jMenuBar1.add(jMenu2);
    jMenu2.add(fitToWindow);
    jMenu2.add(fontSize);
    jMenu2.add(distanceMenu);
    jMenu2.add(bootstrapMenu);
    jMenu2.add(placeholdersMenu);
    fileMenu.add(newickOutput);
    fileMenu.add(inputData);
    inputData.addActionListener(this);
  }

}
