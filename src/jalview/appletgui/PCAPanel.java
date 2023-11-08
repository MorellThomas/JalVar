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

import jalview.analysis.scoremodels.ScoreModels;
import jalview.analysis.scoremodels.SimilarityParams;
import jalview.api.analysis.ScoreModelI;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.SeqCigar;
import jalview.datamodel.SequenceI;
import jalview.util.MessageManager;
import jalview.viewmodel.PCAModel;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.CheckboxMenuItem;
import java.awt.Choice;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class PCAPanel extends EmbmenuFrame
        implements Runnable, ActionListener, ItemListener
{
  RotatableCanvas rc;

  AlignViewport av;

  PCAModel pcaModel;

  int top = 0;

  public PCAPanel(AlignViewport viewport)
  {
    try
    {
      jbInit();
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    for (int i = 1; i < 8; i++)
    {
      xCombobox.addItem("dim " + i);
      yCombobox.addItem("dim " + i);
      zCombobox.addItem("dim " + i);
    }

    this.av = viewport;
    boolean selected = viewport.getSelectionGroup() != null
            && viewport.getSelectionGroup().getSize() > 0;
    AlignmentView seqstrings = viewport.getAlignmentView(selected);
    boolean nucleotide = viewport.getAlignment().isNucleotide();
    SequenceI[] seqs;
    if (!selected)
    {
      seqs = viewport.getAlignment().getSequencesArray();
    }
    else
    {
      seqs = viewport.getSelectionGroup()
              .getSequencesInOrder(viewport.getAlignment());
    }
    SeqCigar sq[] = seqstrings.getSequences();
    int length = sq[0].getWidth();

    for (int i = 0; i < seqs.length; i++)
    {
      if (sq[i].getWidth() != length)
      {
        System.out
                .println("Sequences must be equal length for PCA analysis");
        return;
      }
    }

    ScoreModelI scoreModel = ScoreModels.getInstance()
            .getDefaultModel(!nucleotide);
    pcaModel = new PCAModel(seqstrings, seqs, nucleotide, scoreModel,
            SimilarityParams.SeqSpace);

    rc = new RotatableCanvas(viewport);
    embedMenuIfNeeded(rc);
    add(rc, BorderLayout.CENTER);

    jalview.bin.JalviewLite.addFrame(this,
            MessageManager.getString("label.principal_component_analysis"),
            475, 400);

    Thread worker = new Thread(this);
    worker.start();
  }

  /**
   * DOCUMENT ME!
   */
  @Override
  public void run()
  {
    // TODO progress indicator
    calcSettings.setEnabled(false);
    rc.setEnabled(false);
    try
    {
      nuclSetting.setState(pcaModel.isNucleotide());
      protSetting.setState(!pcaModel.isNucleotide());
      pcaModel.calculate();
      // ////////////////
      xCombobox.select(0);
      yCombobox.select(1);
      zCombobox.select(2);

      pcaModel.updateRc(rc);
      // rc.invalidate();
      top = pcaModel.getTop();
    } catch (OutOfMemoryError x)
    {
      System.err.println("Out of memory when calculating PCA.");
      return;
    }
    calcSettings.setEnabled(true);

    // TODO revert progress indicator
    rc.setEnabled(true);
    rc.repaint();
    this.repaint();
  }

  void doDimensionChange()
  {
    if (top == 0)
    {
      return;
    }

    int dim1 = top - xCombobox.getSelectedIndex();
    int dim2 = top - yCombobox.getSelectedIndex();
    int dim3 = top - zCombobox.getSelectedIndex();
    pcaModel.updateRcView(dim1, dim2, dim3);
    rc.resetView();
    rc.paint(rc.getGraphics());
  }

  @Override
  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == inputData)
    {
      showOriginalData();
    }
    if (evt.getSource() == resetButton)
    {
      xCombobox.select(0);
      yCombobox.select(1);
      zCombobox.select(2);
      doDimensionChange();
    }
    if (evt.getSource() == values)
    {
      values_actionPerformed();
    }
  }

  @Override
  public void itemStateChanged(ItemEvent evt)
  {
    if (evt.getSource() == xCombobox)
    {
      xCombobox_actionPerformed();
    }
    else if (evt.getSource() == yCombobox)
    {
      yCombobox_actionPerformed();
    }
    else if (evt.getSource() == zCombobox)
    {
      zCombobox_actionPerformed();
    }
    else if (evt.getSource() == labels)
    {
      labels_itemStateChanged(evt);
    }
    else if (evt.getSource() == nuclSetting)
    {
      if (!pcaModel.isNucleotide())
      {
        pcaModel.setNucleotide(true);
        ScoreModelI scoreModel = ScoreModels.getInstance()
                .getDefaultModel(false);
        pcaModel.setScoreModel(scoreModel);
        new Thread(this).start();
      }
    }
    else if (evt.getSource() == protSetting)
    {
      if (pcaModel.isNucleotide())
      {
        pcaModel.setNucleotide(false);
        ScoreModelI scoreModel = ScoreModels.getInstance()
                .getDefaultModel(true);
        pcaModel.setScoreModel(scoreModel);
        new Thread(this).start();
      }
    }
  }

  protected void xCombobox_actionPerformed()
  {
    doDimensionChange();
  }

  protected void yCombobox_actionPerformed()
  {
    doDimensionChange();
  }

  protected void zCombobox_actionPerformed()
  {
    doDimensionChange();
  }

  public void values_actionPerformed()
  {

    CutAndPasteTransfer cap = new CutAndPasteTransfer(false, null);
    Frame frame = new Frame();
    frame.add(cap);
    jalview.bin.JalviewLite.addFrame(frame,
            MessageManager.getString("label.pca_details"), 500, 500);

    cap.setText(pcaModel.getDetails());
  }

  void showOriginalData()
  {
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
    ;
    Object[] alAndColsel = pcaModel.getInputData()
            .getAlignmentAndHiddenColumns(gc);

    if (alAndColsel != null && alAndColsel[0] != null)
    {
      Alignment al = new Alignment((SequenceI[]) alAndColsel[0]);
      AlignFrame af = new AlignFrame(al, av.applet, "Original Data for PCA",
              false);

      af.viewport.getAlignment()
              .setHiddenColumns((HiddenColumns) alAndColsel[1]);
    }
  }

  public void labels_itemStateChanged(ItemEvent itemEvent)
  {
    rc.showLabels(labels.getState());
  }

  Panel jPanel2 = new Panel();

  Label jLabel1 = new Label();

  Label jLabel2 = new Label();

  Label jLabel3 = new Label();

  protected Choice xCombobox = new Choice();

  protected Choice yCombobox = new Choice();

  protected Choice zCombobox = new Choice();

  protected Button resetButton = new Button();

  FlowLayout flowLayout1 = new FlowLayout();

  BorderLayout borderLayout1 = new BorderLayout();

  MenuBar menuBar1 = new MenuBar();

  Menu menu1 = new Menu();

  Menu menu2 = new Menu();

  Menu calcSettings = new Menu();

  protected CheckboxMenuItem labels = new CheckboxMenuItem();

  protected CheckboxMenuItem protSetting = new CheckboxMenuItem();

  protected CheckboxMenuItem nuclSetting = new CheckboxMenuItem();

  MenuItem values = new MenuItem();

  MenuItem inputData = new MenuItem();

  private void jbInit() throws Exception
  {
    this.setLayout(borderLayout1);
    jPanel2.setLayout(flowLayout1);
    jLabel1.setFont(new java.awt.Font("Verdana", 0, 12));
    jLabel1.setText("x=");
    jLabel2.setFont(new java.awt.Font("Verdana", 0, 12));
    jLabel2.setText("y=");
    jLabel3.setFont(new java.awt.Font("Verdana", 0, 12));
    jLabel3.setText("z=");
    jPanel2.setBackground(Color.white);
    zCombobox.setFont(new java.awt.Font("Verdana", 0, 12));
    zCombobox.addItemListener(this);
    yCombobox.setFont(new java.awt.Font("Verdana", 0, 12));
    yCombobox.addItemListener(this);
    xCombobox.setFont(new java.awt.Font("Verdana", 0, 12));
    xCombobox.addItemListener(this);
    resetButton.setFont(new java.awt.Font("Verdana", 0, 12));
    resetButton.setLabel(MessageManager.getString("action.reset"));
    resetButton.addActionListener(this);
    this.setMenuBar(menuBar1);
    menu1.setLabel(MessageManager.getString("action.file"));
    menu2.setLabel(MessageManager.getString("action.view"));
    calcSettings.setLabel(MessageManager.getString("action.change_params"));
    labels.setLabel(MessageManager.getString("label.labels"));
    labels.addItemListener(this);
    values.setLabel(MessageManager.getString("label.output_values"));
    values.addActionListener(this);
    inputData.setLabel(MessageManager.getString("label.input_data"));
    nuclSetting
            .setLabel(MessageManager.getString("label.nucleotide_matrix"));
    nuclSetting.addItemListener(this);
    protSetting.setLabel(MessageManager.getString("label.protein_matrix"));
    protSetting.addItemListener(this);
    this.add(jPanel2, BorderLayout.SOUTH);
    jPanel2.add(jLabel1, null);
    jPanel2.add(xCombobox, null);
    jPanel2.add(jLabel2, null);
    jPanel2.add(yCombobox, null);
    jPanel2.add(jLabel3, null);
    jPanel2.add(zCombobox, null);
    jPanel2.add(resetButton, null);
    menuBar1.add(menu1);
    menuBar1.add(menu2);
    menuBar1.add(calcSettings);
    menu2.add(labels);
    menu1.add(values);
    menu1.add(inputData);
    calcSettings.add(nuclSetting);
    calcSettings.add(protSetting);
    inputData.addActionListener(this);
  }

}
