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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import jalview.analysis.Analysis;
import jalview.api.AlignViewportI;
import jalview.bin.Console;
import jalview.viewmodel.AlignmentViewport;

/**
 * The panel starts the residue analysis calculation
 * called by AnalysisInput
 */
public class AnalysisPanel extends JPanel 
    implements Runnable, IProgressIndicator
{
  AlignmentPanel ap;

  AlignmentViewport av;
  
  int residue;  //specified residue (base 1)

  private boolean working;
  
  private final boolean doVars;
  
  private IProgressIndicator progressBar;
  
  private long progId;
  
  private Analysis anal;

  /**
   * Constructor given sequence data and the specified residue (inputed at base 1)
   * 
   * @param alignPanel
   * @param residue
   */
  public AnalysisPanel(AlignmentPanel alignPanel, int residue, boolean doVars)
  {
    this.av = alignPanel.av;
    this.ap = alignPanel;
    this.doVars = doVars;
    
    this.residue = residue;
  }

  /**
   * Calculates the residue analysis and displays the results
   */
  @Override
  public void run()
  {
    working = true;
    progId = System.currentTimeMillis();
    progressBar = this;
    String message = "Analysis recalculating";
    if (getParent() == null)
    {
      progressBar = ap.alignFrame;
      message = "Analysis calculating";
    }
    progressBar.setProgressBar(message, progId);
    try
    {
      anal = new Analysis(ap, residue, doVars);
      setAnalysis(anal);
      anal.run(); // executes in same thread, wait for completion

    } catch (OutOfMemoryError | ClassNotFoundException | IOException er)
    {
      if (er instanceof OutOfMemoryError)
      {
        new OOMWarning("calculating Analysis", (OutOfMemoryError) er);
      } else {
        Console.error("Error computing Equivalent Positions:  " + er.getMessage());
        er.printStackTrace();
      }
      working = false;
      return;
    } finally
    {
      progressBar.setProgressBar("", progId);
    }

    working = false;
  }


  /**
   * Answers true if Analysis calculation is in progress, else false
   * 
   * @return
   */
  public boolean isWorking()
  {
    return working;
  }

  public AlignViewportI getAlignViewport()
  {
    return av;
  }
  
  @Override
  public void setProgressBar(String message, long id)
  {
    progressBar.setProgressBar(message, id);
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
    pBar.repaint();
  }
  
  public void setAnalysis(Analysis ana)
  {
    anal.addPropertyChangeListener(new PropertyChangeListener()
    {
      @Override
      public void propertyChange(PropertyChangeEvent pcEvent)
      {
 System.out.println(String.format("%s or %s == %s -> %d", Analysis.PROGRESS, Analysis.TOTAL, pcEvent.getPropertyName(), pcEvent.getNewValue()));
        if (Analysis.PROGRESS.equals(pcEvent.getPropertyName()))
        {
          updateProgressBar((int) pcEvent.getNewValue());
        } else if (Analysis.TOTAL.equals(pcEvent.getPropertyName()))
        {
          updateProgressBar((int) pcEvent.getNewValue(), 0);
        }
      }
    });
    //this.anal = ana;
  }
  
  @Override
  public void registerHandler(final long id, final IProgressIndicatorHandler handler)
  {
    progressBar.registerHandler(id, handler);
  }
  
  @Override
  public boolean operationInProgress()
  {
    return progressBar.operationInProgress();
  }
  
  @Override
  public JProgressBar getProgressBar(long id)
  {
    return progressBar.getProgressBar(id);
  }
  
}
