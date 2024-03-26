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

import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 * A class to manage multiple progress bars embedded in a JPanel.
 */
/*
 * Refactored from code duplicated in AlignFrame, PCAPanel, WebserviceInfo.
 */
public class ProgressBar implements IProgressIndicator
{
  /*
   * Progress bars in progress, keyed by any arbitrary long value
   */
  Map<Long, JPanel> progressBars;

  /*
   * Optional handlers for the progress bars
   */
  Map<Long, IProgressIndicatorHandler> progressBarHandlers;

  /*
   * The panel containing the progress bars - must have GridLayout
   */
  private JPanel statusPanel;

  /*
   * Optional label where a status update message can be written on completion
   * of progress
   */
  private JLabel statusBar;

  /**
   * Constructor. Note that the container of the progress bars, and the status
   * bar to which an optional completion message is written, should be unchanged
   * for the lifetime of this object for consistent behaviour.
   * 
   * @param container
   *          the panel holding the progress bars; must have GridLayout manager
   * @param statusBar
   *          an optional place to write a message when a progress bar is
   *          removed
   */
  public ProgressBar(JPanel container, JLabel statusBar)
  {
    if (container == null)
    {
      throw new NullPointerException();
    }
    if (!GridLayout.class
            .isAssignableFrom(container.getLayout().getClass()))
    {
      throw new IllegalArgumentException("Container must have GridLayout");
    }
    this.statusPanel = container;
    this.statusBar = statusBar;
    this.progressBars = new Hashtable<>();
    this.progressBarHandlers = new Hashtable<>();

  }

  /**
   * Returns true if any progress bars are still active
   * 
   * @return
   */
  @Override
  public boolean operationInProgress()
  {
    if (progressBars != null && progressBars.size() > 0)
    {
      return true;
    }
    return false;
  }

  /**
   * First call for a given id will show the message as a new progress bar. A
   * second call with the same id will remove it. The 'removal' message is
   * written to the status bar field (if neither is null).
   * 
   * To avoid progress bars being left orphaned, ensure their removal is
   * performed in a finally block if there is any risk of an error during
   * execution.
   */
  @Override
  public void setProgressBar(final String message, final long id)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        JPanel progressPanel = progressBars.get(id);
        if (progressPanel != null)
        {
          /*
           * Progress bar is displayed for this id - remove it now, and any handler
           */
          progressBars.remove(id);
          if (message != null && statusBar != null)
          {
            statusBar.setText(message);
          }
          if (progressBarHandlers.containsKey(id))
          {
            progressBarHandlers.remove(id);
          }
          removeRow(progressPanel);
        }
        else
        {
          /*
           * No progress bar for this id - add one now
           */
          progressPanel = new JPanel(new BorderLayout(10, 5));

          JProgressBar progressBar = new JProgressBar();
          progressBar.setIndeterminate(true);

          progressPanel.add(new JLabel(message), BorderLayout.WEST);
          progressPanel.add(progressBar, BorderLayout.CENTER);

          addRow(progressPanel);

          progressBars.put(id, progressPanel);
        }

        refreshLayout();
      }
    });

  }

  /**
   * Lays out progress bar container hierarchy
   */
  protected void refreshLayout()
  {
    /*
     * lay out progress bar container hierarchy
     */
    Component root = SwingUtilities.getRoot(statusPanel);
    if (root != null)
    {
      root.validate();
    }
  }

  /**
   * Remove one row with a progress bar, in a thread-safe manner
   * 
   * @param progressPanel
   */
  protected void removeRow(JPanel progressPanel)
  {
    synchronized (statusPanel)
    {
      statusPanel.remove(progressPanel);
      GridLayout layout = (GridLayout) statusPanel.getLayout();
      layout.setRows(layout.getRows() - 1);
      statusPanel.remove(progressPanel);
    }
  }

  /**
   * Add one row with a progress bar, in a thread-safe manner
   * 
   * @param progressPanel
   */
  protected void addRow(JPanel progressPanel)
  {
    synchronized (statusPanel)
    {
      GridLayout layout = (GridLayout) statusPanel.getLayout();
      layout.setRows(layout.getRows() + 1);
      statusPanel.add(progressPanel);
    }
  }

  /**
   * Add a 'Cancel' handler for the given progress bar id. This should be called
   * _after_ setProgressBar to have any effect.
   */
  @Override
  public void registerHandler(final long id,
          final IProgressIndicatorHandler handler)
  {
    final IProgressIndicator us = this;

    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        final JPanel progressPanel = progressBars.get(id);
        if (progressPanel == null)
        {
          System.err.println(
                  "call setProgressBar before registering the progress bar's handler.");
          return;
        }

        /*
         * Nothing useful to do if not a Cancel handler
         */
        if (!handler.canCancel())
        {
          return;
        }

        progressBarHandlers.put(id, handler);
        JButton cancel = new JButton(
                MessageManager.getString("action.cancel"));
        cancel.addActionListener(new ActionListener()
        {

          @Override
          public void actionPerformed(ActionEvent e)
          {
            handler.cancelActivity(id);
            us.setProgressBar(MessageManager
                    .formatMessage("label.cancelled_params", new Object[]
                    { ((JLabel) progressPanel.getComponent(0)).getText() }),
                    id);
          }
        });
        progressPanel.add(cancel, BorderLayout.EAST);
        refreshLayout();

      }
    });
  }
  
  /*
   * return the JProgressBar mapped to id
   */
  public JProgressBar getProgressBar(long id)
  {
    for (Component component : progressBars.get(id).getComponents())
    {
      if (component.getClass().equals(JProgressBar.class))
        return (JProgressBar) component;
    }
    return null;
  }

}
