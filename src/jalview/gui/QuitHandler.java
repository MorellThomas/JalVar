package jalview.gui;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;

import com.formdev.flatlaf.extras.FlatDesktop;
import com.formdev.flatlaf.extras.FlatDesktop.QuitResponse;

import jalview.api.AlignmentViewPanel;
import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceI;
import jalview.io.BackupFiles;
import jalview.project.Jalview2XML;
import jalview.util.MessageManager;
import jalview.util.Platform;

public class QuitHandler
{
  private static final int MIN_WAIT_FOR_SAVE = 1000;

  private static final int MAX_WAIT_FOR_SAVE = 20000;

  private static boolean interactive = true;

  private static QuitResponse flatlafResponse = null;

  public static enum QResponse
  {
    NULL, QUIT, CANCEL_QUIT, FORCE_QUIT
  };

  public static enum Message
  {
    UNSAVED_CHANGES, UNSAVED_ALIGNMENTS
  };

  protected static Message message = Message.UNSAVED_CHANGES;

  public static void setMessage(Message m)
  {
    message = m;
  }

  private static ExecutorService executor = Executors.newFixedThreadPool(3);

  public static void setQuitHandler()
  {
    FlatDesktop.setQuitHandler(response -> {
      flatlafResponse = response;
      Desktop.instance.desktopQuit();
    });
  }

  public static void startForceQuit()
  {
    setResponse(QResponse.FORCE_QUIT);
  }

  private static QResponse gotQuitResponse = QResponse.NULL;

  protected static QResponse setResponse(QResponse qresponse)
  {
    gotQuitResponse = qresponse;
    if ((qresponse == QResponse.CANCEL_QUIT || qresponse == QResponse.NULL)
            && flatlafResponse != null)
    {
      flatlafResponse.cancelQuit();
    }
    return qresponse;
  }

  public static QResponse gotQuitResponse()
  {
    return gotQuitResponse;
  }

  public static final Runnable defaultCancelQuit = () -> {
    Console.debug("QuitHandler: (default) Quit action CANCELLED by user");
    // reset
    setResponse(QResponse.CANCEL_QUIT);
  };

  public static final Runnable defaultOkQuit = () -> {
    Console.debug("QuitHandler: (default) Quit action CONFIRMED by user");
    setResponse(QResponse.QUIT);
  };

  public static final Runnable defaultForceQuit = () -> {
    Console.debug("QuitHandler: (default) Quit action FORCED by user");
    // note that shutdown hook will not be run
    Runtime.getRuntime().halt(0);
    setResponse(QResponse.FORCE_QUIT); // this line never reached!
  };

  public static QResponse getQuitResponse(boolean ui)
  {
    return getQuitResponse(ui, defaultOkQuit, defaultForceQuit,
            defaultCancelQuit);
  }

  public static QResponse getQuitResponse(boolean ui, Runnable okQuit,
          Runnable forceQuit, Runnable cancelQuit)
  {
    QResponse got = gotQuitResponse();
    if (got != QResponse.NULL && got != QResponse.CANCEL_QUIT)
    {
      // quit has already been selected, continue with calling quit method
      return got;
    }

    interactive = ui && !Platform.isHeadless();
    // confirm quit if needed and wanted
    boolean confirmQuit = true;

    if (!interactive)
    {
      Console.debug("Non interactive quit -- not confirming");
      confirmQuit = false;
    }
    else if (Jalview2XML.allSavedUpToDate())
    {
      Console.debug("Nothing changed -- not confirming quit");
      confirmQuit = false;
    }
    else
    {
      confirmQuit = jalview.bin.Cache
              .getDefault(jalview.gui.Desktop.CONFIRM_KEYBOARD_QUIT, true);
      Console.debug("Jalview property '"
              + jalview.gui.Desktop.CONFIRM_KEYBOARD_QUIT
              + "' is/defaults to " + confirmQuit + " -- "
              + (confirmQuit ? "" : "not ") + "confirming quit");
    }
    got = confirmQuit ? QResponse.NULL : QResponse.QUIT;
    setResponse(got);

    if (confirmQuit)
    {
      String messageString = MessageManager
              .getString(message == Message.UNSAVED_ALIGNMENTS
                      ? "label.unsaved_alignments"
                      : "label.unsaved_changes");
      setQuitDialog(JvOptionPane.newOptionDialog()
              .setResponseHandler(JOptionPane.YES_OPTION, defaultOkQuit)
              .setResponseHandler(JOptionPane.NO_OPTION, cancelQuit));
      JvOptionPane qd = getQuitDialog();
      qd.showDialogOnTopAsync(
              new StringBuilder(
                      MessageManager.getString("label.quit_jalview"))
                      .append("\n").append(messageString).toString(),
              MessageManager.getString("action.quit"),
              JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
              new Object[]
              { MessageManager.getString("action.quit"),
                  MessageManager.getString("action.cancel") },
              MessageManager.getString("action.quit"), true);
    }

    got = gotQuitResponse();

    // check for external viewer frames
    if (got != QResponse.CANCEL_QUIT)
    {
      int count = Desktop.instance.structureViewersStillRunningCount();
      if (count > 0)
      {
        String alwaysCloseExternalViewers = Cache
                .getDefault("ALWAYS_CLOSE_EXTERNAL_VIEWERS", "ask");
        String prompt = MessageManager
                .formatMessage(count == 1 ? "label.confirm_quit_viewer"
                        : "label.confirm_quit_viewers");
        String title = MessageManager.getString(
                count == 1 ? "label.close_viewer" : "label.close_viewers");
        String cancelQuitText = MessageManager
                .getString("action.cancel_quit");
        String[] buttonsText = { MessageManager.getString("action.yes"),
            MessageManager.getString("action.no"), cancelQuitText };

        int confirmResponse = -1;
        if (alwaysCloseExternalViewers == null || "ask".equals(
                alwaysCloseExternalViewers.toLowerCase(Locale.ROOT)))
        {
          confirmResponse = JvOptionPane.showOptionDialog(Desktop.instance,
                  prompt, title, JvOptionPane.YES_NO_CANCEL_OPTION,
                  JvOptionPane.WARNING_MESSAGE, null, buttonsText,
                  cancelQuit);
        }
        else
        {
          confirmResponse = Cache
                  .getDefault("ALWAYS_CLOSE_EXTERNAL_VIEWERS", false)
                          ? JvOptionPane.YES_OPTION
                          : JvOptionPane.NO_OPTION;
        }

        if (confirmResponse == JvOptionPane.CANCEL_OPTION)
        {
          // Cancel Quit
          QuitHandler.setResponse(QResponse.CANCEL_QUIT);
        }
        else
        {
          // Close viewers/Leave viewers open
          StructureViewerBase
                  .setQuitClose(confirmResponse == JvOptionPane.YES_OPTION);
        }
      }

    }

    got = gotQuitResponse();

    boolean wait = false;
    if (got == QResponse.CANCEL_QUIT)
    {
      // reset
      Console.debug("Cancelling quit.  Resetting response to NULL");
      setResponse(QResponse.NULL);
      // but return cancel
      return QResponse.CANCEL_QUIT;
    }
    else if (got == QResponse.QUIT)
    {
      if (Cache.getDefault("WAIT_FOR_SAVE", true)
              && BackupFiles.hasSavesInProgress())
      {
        waitQuit(interactive, okQuit, forceQuit, cancelQuit);
        QResponse waitResponse = gotQuitResponse();
        wait = waitResponse == QResponse.QUIT;
      }
    }

    Runnable next = null;
    switch (gotQuitResponse())
    {
    case QUIT:
      next = okQuit;
      break;
    case FORCE_QUIT: // not actually an option at this stage
      next = forceQuit;
      break;
    default:
      next = cancelQuit;
      break;
    }
    try
    {
      executor.submit(next).get();
      got = gotQuitResponse();
    } catch (RejectedExecutionException e)
    {
      // QuitHander.abortQuit() probably called
      // CANCEL_QUIT test will reset QuitHandler
      Console.info("Quit aborted!");
      got = QResponse.NULL;
      setResponse(QResponse.NULL);
    } catch (InterruptedException | ExecutionException e)
    {
      jalview.bin.Console
              .debug("Exception during quit handling (final choice)", e);
    }
    setResponse(got);

    if (quitCancelled())
    {
      // reset if cancelled
      Console.debug("Quit cancelled");
      setResponse(QResponse.NULL);
      return QResponse.CANCEL_QUIT;
    }
    return gotQuitResponse();
  }

  private static QResponse waitQuit(boolean interactive, Runnable okQuit,
          Runnable forceQuit, Runnable cancelQuit)
  {
    // check for saves in progress
    if (!BackupFiles.hasSavesInProgress())
      return QResponse.QUIT;

    int size = 0;
    AlignFrame[] afArray = Desktop.getAlignFrames();
    if (!(afArray == null || afArray.length == 0))
    {
      for (int i = 0; i < afArray.length; i++)
      {
        AlignFrame af = afArray[i];
        List<? extends AlignmentViewPanel> avpList = af.getAlignPanels();
        for (AlignmentViewPanel avp : avpList)
        {
          AlignmentI a = avp.getAlignment();
          List<SequenceI> sList = a.getSequences();
          for (SequenceI s : sList)
          {
            size += s.getLength();
          }
        }
      }
    }
    int waitTime = Math.min(MAX_WAIT_FOR_SAVE,
            Math.max(MIN_WAIT_FOR_SAVE, size / 2));
    Console.debug("Set waitForSave to " + waitTime);

    int iteration = 0;
    boolean doIterations = true; // note iterations not used in the gui now,
                                 // only one pass without the "Wait" button
    while (doIterations && BackupFiles.hasSavesInProgress()
            && iteration++ < (interactive ? 100 : 5))
    {
      // future that returns a Boolean when all files are saved
      CompletableFuture<Boolean> filesAllSaved = new CompletableFuture<>();

      // callback as each file finishes saving
      for (CompletableFuture<Boolean> cf : BackupFiles
              .savesInProgressCompletableFutures(false))
      {
        // if this is the last one then complete filesAllSaved
        cf.whenComplete((ret, e) -> {
          if (!BackupFiles.hasSavesInProgress())
          {
            filesAllSaved.complete(true);
          }
        });
      }
      try
      {
        filesAllSaved.get(waitTime, TimeUnit.MILLISECONDS);
      } catch (InterruptedException | ExecutionException e1)
      {
        Console.debug(
                "Exception whilst waiting for files to save before quit",
                e1);
      } catch (TimeoutException e2)
      {
        // this Exception to be expected
      }

      if (interactive && BackupFiles.hasSavesInProgress())
      {
        boolean showForceQuit = iteration > 0; // iteration > 1 to not show
                                               // force quit the first time
        JFrame parent = new JFrame();
        JButton[] buttons = { new JButton(), new JButton() };
        JvOptionPane waitDialog = JvOptionPane.newOptionDialog();
        JTextPane messagePane = new JTextPane();
        messagePane.setBackground(waitDialog.getBackground());
        messagePane.setBorder(null);
        messagePane.setText(waitingForSaveMessage());
        // callback as each file finishes saving
        for (CompletableFuture<Boolean> cf : BackupFiles
                .savesInProgressCompletableFutures(false))
        {
          cf.whenComplete((ret, e) -> {
            if (BackupFiles.hasSavesInProgress())
            {
              // update the list of saving files as they save too
              messagePane.setText(waitingForSaveMessage());
            }
            else
            {
              if (!(quitCancelled()))
              {
                for (int i = 0; i < buttons.length; i++)
                {
                  Console.debug("DISABLING BUTTON " + buttons[i].getText());
                  buttons[i].setEnabled(false);
                  buttons[i].setVisible(false);
                }
                // if this is the last one then close the dialog
                messagePane.setText(new StringBuilder()
                        .append(MessageManager.getString("label.all_saved"))
                        .append("\n")
                        .append(MessageManager
                                .getString("label.quitting_bye"))
                        .toString());
                messagePane.setEditable(false);
                try
                {
                  Thread.sleep(1500);
                } catch (InterruptedException e1)
                {
                }
                parent.dispose();
              }
            }
          });
        }

        String[] options;
        int dialogType = -1;
        if (showForceQuit)
        {
          options = new String[2];
          options[0] = MessageManager.getString("action.force_quit");
          options[1] = MessageManager.getString("action.cancel_quit");
          dialogType = JOptionPane.YES_NO_OPTION;
          waitDialog.setResponseHandler(JOptionPane.YES_OPTION, forceQuit)
                  .setResponseHandler(JOptionPane.NO_OPTION, cancelQuit);
        }
        else
        {
          options = new String[1];
          options[0] = MessageManager.getString("action.cancel_quit");
          dialogType = JOptionPane.YES_OPTION;
          waitDialog.setResponseHandler(JOptionPane.YES_OPTION, cancelQuit);
        }
        waitDialog.showDialogOnTopAsync(parent, messagePane,
                MessageManager.getString("label.wait_for_save"), dialogType,
                JOptionPane.WARNING_MESSAGE, null, options,
                MessageManager.getString("action.cancel_quit"), true,
                buttons);

        parent.dispose();
        final QResponse thisWaitResponse = gotQuitResponse();
        switch (thisWaitResponse)
        {
        case QUIT: // wait -- do another iteration
          break;
        case FORCE_QUIT:
          doIterations = false;
          break;
        case CANCEL_QUIT:
          doIterations = false;
          break;
        case NULL: // already cancelled
          doIterations = false;
          break;
        default:
        }
      } // end if interactive

    } // end while wait iteration loop
    return gotQuitResponse();
  };

  private static String waitingForSaveMessage()
  {
    StringBuilder messageSB = new StringBuilder();

    messageSB.append(MessageManager.getString("label.save_in_progress"));
    List<File> files = BackupFiles.savesInProgressFiles(false);
    boolean any = files.size() > 0;
    if (any)
    {
      for (File file : files)
      {
        messageSB.append("\n\u2022 ").append(file.getName());
      }
    }
    else
    {
      messageSB.append(MessageManager.getString("label.unknown"));
    }
    messageSB.append("\n\n")
            .append(MessageManager.getString("label.quit_after_saving"));
    return messageSB.toString();
  }

  public static void abortQuit()
  {
    setResponse(QResponse.NULL);
    // executor.shutdownNow();
  }

  private static JvOptionPane quitDialog = null;

  private static void setQuitDialog(JvOptionPane qd)
  {
    quitDialog = qd;
  }

  private static JvOptionPane getQuitDialog()
  {
    return quitDialog;
  }

  public static boolean quitCancelled()
  {
    return QuitHandler.gotQuitResponse() == QResponse.CANCEL_QUIT
            || QuitHandler.gotQuitResponse() == QResponse.NULL;
  }

  public static boolean quitting()
  {
    return QuitHandler.gotQuitResponse() == QResponse.QUIT
            || QuitHandler.gotQuitResponse() == QResponse.FORCE_QUIT;
  }
}
