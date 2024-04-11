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

import jalview.analysis.scoremodels.ScoreModels;
import jalview.api.AlignViewportI;
import jalview.api.analysis.ScoreModelI;
import jalview.api.analysis.SimilarityParamsI;
import jalview.bin.Console;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.SequenceI;
import jalview.gui.ImageExporter.ImageWriterI;
import jalview.gui.JalviewColourChooser.ColourChooserListener;
import jalview.jbgui.GPCAPanel;
import jalview.math.RotatableMatrix.Axis;
import jalview.util.ImageMaker;
import jalview.util.MessageManager;
import jalview.viewmodel.AlignmentViewport;
import jalview.viewmodel.PaSiMapModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JProgressBar;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * The panel holding the Pairwise Similarity Map 3-D visualisation
 */
public class PaSiMapPanel extends GPCAPanel
        implements Runnable, IProgressIndicator
{
  private static final int MIN_WIDTH = 470;

  private static final int MIN_HEIGHT = 250;

  private RotatableCanvas rc;

  AlignmentPanel ap;

  AlignmentViewport av;

  private PaSiMapModel pasimapModel;

  private int top = 0;

  //private IProgressIndicator progressBar;
  private ProgressBar progressBar;

  private boolean working;
  
  private long progId;

  /**
   * Constructor given sequence data, a similarity (or distance) score model
   * name, and score calculation parameters
   * 
   * @param alignPanel
   * @param modelName
   * @param params
   */
  public PaSiMapPanel(AlignmentPanel alignPanel, String modelName,
          SimilarityParamsI params)
  {
    super(3);	// dim = 3
    this.av = alignPanel.av;
    this.ap = alignPanel;
    boolean nucleotide = av.getAlignment().isNucleotide();

    progressBar = new ProgressBar(statusPanel, statusBar);

    addInternalFrameListener(new InternalFrameAdapter()
    {
      @Override
      public void internalFrameClosed(InternalFrameEvent e)
      {
        close_actionPerformed();
      }
    });

    boolean selected = av.getSelectionGroup() != null
            && av.getSelectionGroup().getSize() > 0;
    SequenceI[] seqs;
    if (!selected)
    {
      seqs = av.getAlignment().getSequencesArray();
    }
    else
    {
      seqs = av.getSelectionGroup().getSequencesInOrder(av.getAlignment());
    }

    ScoreModelI scoreModel = ScoreModels.getInstance()
            .getScoreModel(modelName, ap);
    setPasimapModel(
            new PaSiMapModel(av, seqs, nucleotide, scoreModel));
    PaintRefresher.Register(this, av.getSequenceSetId());

    setRotatableCanvas(new RotatableCanvas(alignPanel));
    this.getContentPane().add(getRotatableCanvas(), BorderLayout.CENTER);

    addKeyListener(getRotatableCanvas());
    validate();
  }

  /**
   * Ensure references to potentially very large objects (the PaSiMap matrices) are
   * nulled when the frame is closed
   */
  protected void close_actionPerformed()
  {
    setPasimapModel(null);
    if (this.rc != null)
    {
      this.rc.sequencePoints = null;
      this.rc.setAxisEndPoints(null);
      this.rc = null;
    }
  }

  @Override
  protected void bgcolour_actionPerformed()
  {
    String ttl = MessageManager.getString("label.select_background_colour");
    ColourChooserListener listener = new ColourChooserListener()
    {
      @Override
      public void colourSelected(Color c)
      {
        rc.setBgColour(c);
        rc.repaint();
      }
    };
    JalviewColourChooser.showColourChooser(this, ttl, rc.getBgColour(),
            listener);
  }

  /**
   * Calculates the PaSiMap and displays the results
   */
  @Override
  public void run()
  {
    working = true;
    progId = System.currentTimeMillis();
    IProgressIndicator progress = this;
    String message = MessageManager.getString("label.pasimap_recalculating");
    if (getParent() == null)
    {
      progress = ap.alignFrame;
      message = MessageManager.getString("label.pasimap_calculating");
    }
    progress.setProgressBar(message, progId);
    try
    {
      //&! remove big seqs
      for (SequenceI seq : av.getAlignment().getSequencesArray())
      {
        if (seq.getLength() > 20000)
        {
          //TODO add warning dialog
          av.getAlignment().deleteSequence(seq);
        }
      }

      PairwiseAlignPanel pap = new PairwiseAlignPanel(av, true, false);
      setPairwiseAlignPanel(pap);
      getPasimapModel().calculate(pap);

      xCombobox.setSelectedIndex(0);
      yCombobox.setSelectedIndex(1);
      zCombobox.setSelectedIndex(2);

      getPasimapModel().updateRc(getRotatableCanvas());
      // rc.invalidate();
      setTop(getPasimapModel().getTop());

    } catch (OutOfMemoryError er)
    {
      new OOMWarning("calculating PaSiMap", er);
      working = false;
      return;
    } finally
    {
      progress.setProgressBar("", progId);
    }

    repaint();
    if (getParent() == null)
    {
      Desktop.addInternalFrame(this,
              MessageManager.formatMessage("label.calc_title", "PaSiMap",
                      getPasimapModel().getScoreModelName()),
              475, 450);
      this.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
    }
    working = false;
  }

  /**
   * Updates the PaSiMap display after a change of component to use for x, y or z
   * axis
   */
  @Override
  protected void doDimensionChange()
  {
    if (getTop() == 0)
    {
      return;
    }

    int dim1 = getTop() - xCombobox.getSelectedIndex();
    int dim2 = getTop() - yCombobox.getSelectedIndex();
    int dim3 = getTop() - zCombobox.getSelectedIndex();
    getPasimapModel().updateRcView(dim1, dim2, dim3);
    getRotatableCanvas().resetView();
  }

  /**
   * Sets the selected checkbox item index for PaSiMap dimension (1, 2, 3...) for
   * the given axis (X/Y/Z)
   * 
   * @param index
   * @param axis
   */
  public void setSelectedDimensionIndex(int index, Axis axis)
  {
    switch (axis)
    {
    case X:
      xCombobox.setSelectedIndex(index);
      break;
    case Y:
      yCombobox.setSelectedIndex(index);
      break;
    case Z:
      zCombobox.setSelectedIndex(index);
      break;
    default:
    }
  }

  @Override
  protected void outputValues_actionPerformed()
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer();
    try
    {
      cap.setText(getPasimapModel().getDetails());
      Desktop.addInternalFrame(cap,
              MessageManager.getString("label.pasimap_details"), 500, 500);
    } catch (OutOfMemoryError oom)
    {
      new OOMWarning("opening PaSiMap details", oom);
      cap.dispose();
    }
  }

  @Override
  protected void showLabels_actionPerformed()
  {
    getRotatableCanvas().showLabels(showLabels.getState());
  }

  @Override
  protected void print_actionPerformed()
  {
    PaSiMapPrinter printer = new PaSiMapPrinter();
    printer.start();
  }

  /**
   * If available, shows the data which formed the inputs for the PaSiMap as a new
   * alignment
   */
  @Override
  public void originalSeqData_actionPerformed()
  {
    // JAL-2647 disabled after load from project (until save to project done)
    if (getPasimapModel().getInputData() == null)
    {
      Console.info(
              "Unexpected call to originalSeqData_actionPerformed - should have hidden this menu action.");
      return;
    }
    // decide if av alignment is sufficiently different to original data to
    // warrant a new window to be created
    // create new alignment window with hidden regions (unhiding hidden regions
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

    Object[] alAndColsel = getPasimapModel().getInputData()
            .getAlignmentView(false).getAlignmentAndHiddenColumns(gc);

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
                "label.original_data_for_params", new String[]
                { this.title }), AlignFrame.DEFAULT_WIDTH,
                AlignFrame.DEFAULT_HEIGHT);
      }
    }
    /*
     * CutAndPasteTransfer cap = new CutAndPasteTransfer(); for (int i = 0; i <
     * seqs.length; i++) { cap.appendText(new jalview.util.Format("%-" + 15 +
     * "s").form( seqs[i].getName())); cap.appendText(" " + seqstrings[i] +
     * "\n"); }
     * 
     * Desktop.addInternalFrame(cap, "Original Data", 400, 400);
     */
  }

  class PaSiMapPrinter extends Thread implements Printable
  {
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

    @Override
    public int print(Graphics pg, PageFormat pf, int pi)
            throws PrinterException
    {
      pg.translate((int) pf.getImageableX(), (int) pf.getImageableY());

      getRotatableCanvas().drawBackground(pg);
      getRotatableCanvas().drawScene(pg);
      if (getRotatableCanvas().drawAxes)
      {
        getRotatableCanvas().drawAxes(pg);
      }

      if (pi == 0)
      {
        return Printable.PAGE_EXISTS;
      }
      else
      {
        return Printable.NO_SUCH_PAGE;
      }
    }
  }

  public void makePaSiMapImage(ImageMaker.TYPE type)  throws Exception
  {
    int width = getRotatableCanvas().getWidth();
    int height = getRotatableCanvas().getHeight();
    ImageWriterI writer = new ImageWriterI()
    {
      @Override
      public void exportImage(Graphics g) throws Exception
      {
        RotatableCanvas canvas = getRotatableCanvas();
        canvas.drawBackground(g);
        canvas.drawScene(g);
        if (canvas.drawAxes)
        {
          canvas.drawAxes(g);
        }
      }
    };
    String pasimap = MessageManager.getString("label.pasimap");
    ImageExporter exporter = new ImageExporter(writer, null, type, pasimap);
    exporter.doExport(null, this, width, height, pasimap);
  }

  @Override
  protected void viewMenu_menuSelected()
  {
    buildAssociatedViewMenu();
  }

  /**
   * Builds the menu showing the choice of possible views (for the associated
   * sequence data) to which the PaSiMap may be linked
   */
  void buildAssociatedViewMenu()
  {
    AlignmentPanel[] aps = PaintRefresher
            .getAssociatedPanels(av.getSequenceSetId());
    if (aps.length == 1 && getRotatableCanvas().av == aps[0].av)
    {
      associateViewsMenu.setVisible(false);
      return;
    }

    associateViewsMenu.setVisible(true);

    if ((viewMenu
            .getItem(viewMenu.getItemCount() - 2) instanceof JMenuItem))
    {
      viewMenu.insertSeparator(viewMenu.getItemCount() - 1);
    }

    associateViewsMenu.removeAll();

    JRadioButtonMenuItem item;
    ButtonGroup buttonGroup = new ButtonGroup();
    int iSize = aps.length;

    for (int i = 0; i < iSize; i++)
    {
      final AlignmentPanel panel = aps[i];
      item = new JRadioButtonMenuItem(panel.av.getViewName(),
              panel.av == getRotatableCanvas().av);
      buttonGroup.add(item);
      item.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent evt)
        {
          selectAssociatedView(panel);
        }
      });

      associateViewsMenu.add(item);
    }

    final JRadioButtonMenuItem itemf = new JRadioButtonMenuItem(
            "All Views");

    buttonGroup.add(itemf);

    itemf.setSelected(getRotatableCanvas().isApplyToAllViews());
    itemf.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent evt)
      {
        getRotatableCanvas().setApplyToAllViews(itemf.isSelected());
      }
    });
    associateViewsMenu.add(itemf);

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.jbgui.GPCAPanel#outputPoints_actionPerformed(java.awt.event.ActionEvent
   * )
   */
  @Override
  protected void outputPoints_actionPerformed()
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer();
    try
    {
      cap.setText(getPasimapModel().getPointsasCsv(false,
              xCombobox.getSelectedIndex(), yCombobox.getSelectedIndex(),
              zCombobox.getSelectedIndex()));
      Desktop.addInternalFrame(cap, MessageManager
              .formatMessage("label.points_for_params", new String[]
              { this.getTitle() }), 500, 500);
    } catch (OutOfMemoryError oom)
    {
      new OOMWarning("exporting PaSiMap points", oom);
      cap.dispose();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.jbgui.GPCAPanel#outputProjPoints_actionPerformed(java.awt.event
   * .ActionEvent)
   */
  @Override
  protected void outputProjPoints_actionPerformed()
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer();
    try
    {
      cap.setText(getPasimapModel().getPointsasCsv(true,
              xCombobox.getSelectedIndex(), yCombobox.getSelectedIndex(),
              zCombobox.getSelectedIndex()));
      Desktop.addInternalFrame(cap, MessageManager.formatMessage(
              "label.transformed_points_for_params", new String[]
              { this.getTitle() }), 500, 500);
    } catch (OutOfMemoryError oom)
    {
      new OOMWarning("exporting transformed PaSiMap points", oom);
      cap.dispose();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.gui.IProgressIndicator#setProgressBar(java.lang.String, long)
   */
  @Override
  public void setProgressBar(String message, long id)
  {
    progressBar.setProgressBar(message, id);
    // if (progressBars == null)
    // {
    // progressBars = new Hashtable();
    // progressBarHandlers = new Hashtable();
    // }
    //
    // JPanel progressPanel;
    // Long lId = Long.valueOf(id);
    // GridLayout layout = (GridLayout) statusPanel.getLayout();
    // if (progressBars.get(lId) != null)
    // {
    // progressPanel = (JPanel) progressBars.get(Long.valueOf(id));
    // statusPanel.remove(progressPanel);
    // progressBars.remove(lId);
    // progressPanel = null;
    // if (message != null)
    // {
    // statusBar.setText(message);
    // }
    // if (progressBarHandlers.contains(lId))
    // {
    // progressBarHandlers.remove(lId);
    // }
    // layout.setRows(layout.getRows() - 1);
    // }
    // else
    // {
    // progressPanel = new JPanel(new BorderLayout(10, 5));
    //
    // JProgressBar progressBar = new JProgressBar();
    // progressBar.setIndeterminate(true);
    //
    // progressPanel.add(new JLabel(message), BorderLayout.WEST);
    // progressPanel.add(progressBar, BorderLayout.CENTER);
    //
    // layout.setRows(layout.getRows() + 1);
    // statusPanel.add(progressPanel);
    //
    // progressBars.put(lId, progressPanel);
    // }
    // // update GUI
    // // setMenusForViewport();
    // validate();
  }
  
  /*
   * make the progressBar determinate and update its progress
   */
  public void updateProgressBar(int lengthOfTask, int progress)
  {
    JProgressBar pBar = progressBar.getProgressBar(progId);
    if (pBar.isIndeterminate())
    {
      pBar.setMaximum(lengthOfTask);
      pBar.setValue(0);
      pBar.setIndeterminate(false);
    }
    updateProgressBar(progress);
  }
  public void updateProgressBar(int progress)
  {
    JProgressBar pBar = progressBar.getProgressBar(progId);
    pBar.setValue(progress);
  }
  
  //&!
  public void setPairwiseAlignPanel(PairwiseAlignPanel pap)
  {
    pap.addPropertyChangeListener(new PropertyChangeListener() 
    {
      @Override
      public void propertyChange(PropertyChangeEvent pcEvent)
      {
        if (PairwiseAlignPanel.PROGRESS.equals(pcEvent.getPropertyName()))
        {
          updateProgressBar((int) pcEvent.getNewValue());
        } else if (PairwiseAlignPanel.TOTAL.equals(pcEvent.getPropertyName())) {
          updateProgressBar((int) pcEvent.getNewValue(), 0);
        }
      }
    });
  }

  @Override
  public void registerHandler(final long id,
          final IProgressIndicatorHandler handler)
  {
    progressBar.registerHandler(id, handler);
    // if (progressBarHandlers == null ||
    // !progressBars.contains(Long.valueOf(id)))
    // {
    // throw new
    // Error(MessageManager.getString("error.call_setprogressbar_before_registering_handler"));
    // }
    // progressBarHandlers.put(Long.valueOf(id), handler);
    // final JPanel progressPanel = (JPanel) progressBars.get(Long.valueOf(id));
    // if (handler.canCancel())
    // {
    // JButton cancel = new JButton(
    // MessageManager.getString("action.cancel"));
    // final IProgressIndicator us = this;
    // cancel.addActionListener(new ActionListener()
    // {
    //
    // @Override
    // public void actionPerformed(ActionEvent e)
    // {
    // handler.cancelActivity(id);
    // us.setProgressBar(MessageManager.formatMessage("label.cancelled_params",
    // new String[]{((JLabel) progressPanel.getComponent(0)).getText()}), id);
    // }
    // });
    // progressPanel.add(cancel, BorderLayout.EAST);
    // }
  }

  /**
   * 
   * @return true if any progress bars are still active
   */
  @Override
  public boolean operationInProgress()
  {
    return progressBar.operationInProgress();
  }

  @Override
  protected void resetButton_actionPerformed()
  {
    int t = getTop();
    setTop(0); // ugly - prevents dimensionChanged events from being processed
    xCombobox.setSelectedIndex(0);
    yCombobox.setSelectedIndex(1);
    setTop(t);
    zCombobox.setSelectedIndex(2);
  }

  /**
   * Answers true if PaSiMap calculation is in progress, else false
   * 
   * @return
   */
  public boolean isWorking()
  {
    return working;
  }

  /**
   * Answers the selected checkbox item index for PaSiMap dimension for the X, Y or
   * Z axis of the display
   * 
   * @param axis
   * @return
   */
  public int getSelectedDimensionIndex(Axis axis)
  {
    switch (axis)
    {
    case X:
      return xCombobox.getSelectedIndex();
    case Y:
      return yCombobox.getSelectedIndex();
    default:
      return zCombobox.getSelectedIndex();
    }
  }

  public void setShowLabels(boolean show)
  {
    showLabels.setSelected(show);
  }

  /**
   * Sets the input data used to calculate the PaSiMap. This is provided for
   * 'restore from project', which does not currently support this (AL-2647), so
   * sets the value to null, and hides the menu option for "Input Data...". J
   * 
   * @param data
   */
  public void setInputData(AlignmentViewport data)
  {
    getPasimapModel().setInputData(data);
    originalSeqData.setVisible(data != null);
  }

  public AlignViewportI getAlignViewport()
  {
    return av;
  }

  public PaSiMapModel getPasimapModel()
  {
    return pasimapModel;
  }

  public void setPasimapModel(PaSiMapModel pasimapModel)
  {
    this.pasimapModel = pasimapModel;
  }

  public RotatableCanvas getRotatableCanvas()
  {
    return rc;
  }

  public void setRotatableCanvas(RotatableCanvas rc)
  {
    this.rc = rc;
  }

  public int getTop()
  {
    return top;
  }

  public void setTop(int top)
  {
    this.top = top;
  }

  /**
   * set the associated view for this PaSiMap.
   * 
   * @param panel
   */
  public void selectAssociatedView(AlignmentPanel panel)
  {
    getRotatableCanvas().setApplyToAllViews(false);

    ap = panel;
    av = panel.av;

    getRotatableCanvas().av = panel.av;
    getRotatableCanvas().ap = panel;
    PaintRefresher.Register(PaSiMapPanel.this, panel.av.getSequenceSetId());
  }
  
  //@Override
  /*
  public JProgressBar getProgressBar(long id)
  {
    return progressBar.getProgressBar(id);
  }
  */
}
