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

import java.awt.AWTEvent;
import java.awt.ActiveEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.MenuComponent;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import jalview.bin.Console;
import jalview.util.ChannelProperties;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.util.dialogrunner.DialogRunnerI;

public class JvOptionPane extends JOptionPane
        implements DialogRunnerI, PropertyChangeListener
{
  private static final long serialVersionUID = -3019167117756785229L;

  private static Object mockResponse = JvOptionPane.CANCEL_OPTION;

  private static boolean interactiveMode = true;

  public static final Runnable NULLCALLABLE = () -> {
  };

  private Component parentComponent;

  private ExecutorService executor = Executors.newCachedThreadPool();

  private JDialog dialog = null;

  private Map<Object, Runnable> callbacks = new HashMap<>();

  /*
   * JalviewJS reports user choice in the dialog as the selected option (text);
   * this list allows conversion to index (int)
   */
  List<Object> ourOptions;

  public JvOptionPane(final Component parent)
  {
    this.parentComponent = Platform.isJS() ? this : parent;
    this.setIcon(null);
  }

  public static int showConfirmDialog(Component parentComponent,
          Object message) throws HeadlessException
  {
    // only called by test
    return isInteractiveMode()
            ? JOptionPane.showConfirmDialog(parentComponent, message)
            : (int) getMockResponse();
  }

  /**
   * Message, title, optionType
   * 
   * @param parentComponent
   * @param message
   * @param title
   * @param optionType
   * @return
   * @throws HeadlessException
   */
  public static int showConfirmDialog(Component parentComponent,
          Object message, String title, int optionType)
          throws HeadlessException
  {
    if (!isInteractiveMode())
    {
      return (int) getMockResponse();
    }
    switch (optionType)
    {
    case JvOptionPane.YES_NO_CANCEL_OPTION:
      // FeatureRenderer amendFeatures ?? TODO ??
      // Chimera close
      // PromptUserConfig
      // $FALL-THROUGH$
    default:
    case JvOptionPane.YES_NO_OPTION:
      // PromptUserConfig usage stats
      // for now treated as "OK CANCEL"
      // $FALL-THROUGH$
    case JvOptionPane.OK_CANCEL_OPTION:
      // will fall back to simple HTML
      return JOptionPane.showConfirmDialog(parentComponent, message, title,
              optionType);
    }
  }

  /**
   * Adds a message type. Fallback is to just add it in the beginning.
   * 
   * @param parentComponent
   * @param message
   * @param title
   * @param optionType
   * @param messageType
   * @return
   * @throws HeadlessException
   */
  public static int showConfirmDialog(Component parentComponent,
          Object message, String title, int optionType, int messageType)
          throws HeadlessException
  {
    // JalviewServicesChanged
    // PromptUserConfig raiseDialog
    return isInteractiveMode()
            ? JOptionPane.showConfirmDialog(parentComponent, message, title,
                    optionType, messageType)
            : (int) getMockResponse();
  }

  /**
   * Adds an icon
   * 
   * @param parentComponent
   * @param message
   * @param title
   * @param optionType
   * @param messageType
   * @param icon
   * @return
   * @throws HeadlessException
   */
  public static int showConfirmDialog(Component parentComponent,
          Object message, String title, int optionType, int messageType,
          Icon icon) throws HeadlessException
  {
    // JvOptionPaneTest only
    return isInteractiveMode()
            ? JOptionPane.showConfirmDialog(parentComponent, message, title,
                    optionType, messageType, icon)
            : (int) getMockResponse();
  }

  /**
   * Internal version "OK"
   * 
   * @param parentComponent
   * @param message
   * @return
   */
  public static int showInternalConfirmDialog(Component parentComponent,
          Object message)
  {
    // JvOptionPaneTest only;
    return isInteractiveMode()
            ? JOptionPane.showInternalConfirmDialog(parentComponent,
                    message)
            : (int) getMockResponse();
  }

  /**
   * Internal version -- changed to standard version for now
   * 
   * @param parentComponent
   * @param message
   * @param title
   * @param optionType
   * @return
   */
  public static int showInternalConfirmDialog(Component parentComponent,
          String message, String title, int optionType)
  {
    if (!isInteractiveMode())
    {
      return (int) getMockResponse();
    }
    switch (optionType)
    {
    case JvOptionPane.YES_NO_CANCEL_OPTION:
      // ColourMenuHelper.addMenuItmers.offerRemoval TODO
    case JvOptionPane.YES_NO_OPTION:
      // UserDefinedColoursSave -- relevant? TODO
      // $FALL-THROUGH$
    default:
    case JvOptionPane.OK_CANCEL_OPTION:

      // EditNameDialog --- uses panel for messsage TODO

      // Desktop.inputURLMenuItem
      // WsPreferenses
      return JOptionPane.showConfirmDialog(parentComponent, message, title,
              optionType);
    }
  }

  /**
   * 
   * @param parentComponent
   * @param message
   * @param title
   * @param optionType
   * @param messageType
   * @return
   */
  public static int showInternalConfirmDialog(Component parentComponent,
          Object message, String title, int optionType, int messageType)
  {
    if (!isInteractiveMode())
    {
      return (int) getMockResponse();
    }
    switch (optionType)
    {
    case JvOptionPane.YES_NO_CANCEL_OPTION:
    case JvOptionPane.YES_NO_OPTION:
      // UserQuestionanaireCheck
      // VamsasApplication
      // $FALL-THROUGH$
    default:
    case JvOptionPane.OK_CANCEL_OPTION:
      // will fall back to simple HTML
      return JOptionPane.showConfirmDialog(parentComponent, message, title,
              optionType, messageType);
    }
  }

  /**
   * adds icon; no longer internal
   * 
   * @param parentComponent
   * @param message
   * @param title
   * @param optionType
   * @param messageType
   * @param icon
   * @return
   */
  public static int showInternalConfirmDialog(Component parentComponent,
          Object message, String title, int optionType, int messageType,
          Icon icon)
  {
    if (!isInteractiveMode())
    {
      return (int) getMockResponse();
    }
    switch (optionType)
    {
    case JvOptionPane.YES_NO_CANCEL_OPTION:
    case JvOptionPane.YES_NO_OPTION:
      //$FALL-THROUGH$
    default:
    case JvOptionPane.OK_CANCEL_OPTION:
      // Preferences editLink/newLink
      return JOptionPane.showConfirmDialog(parentComponent, message, title,
              optionType, messageType, icon);
    }

  }

  /**
   * custom options full-featured
   * 
   * @param parentComponent
   * @param message
   * @param title
   * @param optionType
   * @param messageType
   * @param icon
   * @param options
   * @param initialValue
   * @return
   * @throws HeadlessException
   */
  public static int showOptionDialog(Component parentComponent,
          String message, String title, int optionType, int messageType,
          Icon icon, Object[] options, Object initialValue)
          throws HeadlessException
  {
    if (!isInteractiveMode())
    {
      return (int) getMockResponse();
    }
    // two uses:
    //
    // TODO
    //
    // 1) AlignViewport for openLinkedAlignment
    //
    // Show a dialog with the option to open and link (cDNA <-> protein) as a
    // new
    // alignment, either as a standalone alignment or in a split frame. Returns
    // true if the new alignment was opened, false if not, because the user
    // declined the offer.
    //
    // 2) UserDefinedColors warning about saving over a name already defined
    //
    return JOptionPane.showOptionDialog(parentComponent, message, title,
            optionType, messageType, icon, options, initialValue);
  }

  /**
   * Just an OK message
   * 
   * @param message
   * @throws HeadlessException
   */
  public static void showMessageDialog(Component parentComponent,
          String message) throws HeadlessException
  {
    if (!isInteractiveMode())
    {
      outputMessage(message);
      return;
    }

    // test class only

    JOptionPane.showMessageDialog(parentComponent, message);
  }

  /**
   * OK with message, title, and type
   * 
   * @param parentComponent
   * @param message
   * @param title
   * @param messageType
   * @throws HeadlessException
   */
  public static void showMessageDialog(Component parentComponent,
          String message, String title, int messageType)
          throws HeadlessException
  {
    // 30 implementations -- all just fine.

    if (!isInteractiveMode())
    {
      outputMessage(message);
      return;
    }

    JOptionPane.showMessageDialog(parentComponent,
            getPrefix(messageType) + message, title, messageType);
  }

  /**
   * adds title and icon
   * 
   * @param parentComponent
   * @param message
   * @param title
   * @param messageType
   * @param icon
   * @throws HeadlessException
   */
  public static void showMessageDialog(Component parentComponent,
          String message, String title, int messageType, Icon icon)
          throws HeadlessException
  {

    // test only

    if (!isInteractiveMode())
    {
      outputMessage(message);
      return;
    }

    JOptionPane.showMessageDialog(parentComponent, message, title,
            messageType, icon);
  }

  /**
   * was internal
   * 
   */
  public static void showInternalMessageDialog(Component parentComponent,
          Object message)
  {

    // WsPreferences only

    if (!isInteractiveMode())
    {
      outputMessage(message);
      return;
    }

    JOptionPane.showMessageDialog(parentComponent, message);
  }

  /**
   * Adds title and messageType
   * 
   * @param parentComponent
   * @param message
   * @param title
   * @param messageType
   */
  public static void showInternalMessageDialog(Component parentComponent,
          String message, String title, int messageType)
  {

    // 41 references

    if (!isInteractiveMode())
    {
      outputMessage(message);
      return;
    }

    JOptionPane.showMessageDialog(parentComponent,
            getPrefix(messageType) + message, title, messageType);
  }

  /**
   * 
   * @param parentComponent
   * @param message
   * @param title
   * @param messageType
   * @param icon
   */
  public static void showInternalMessageDialog(Component parentComponent,
          Object message, String title, int messageType, Icon icon)
  {

    // test only

    if (!isInteractiveMode())
    {
      outputMessage(message);
      return;
    }

    JOptionPane.showMessageDialog(parentComponent, message, title,
            messageType, icon);
  }

  /**
   * 
   * @param message
   * @return
   * @throws HeadlessException
   */
  public static String showInputDialog(Object message)
          throws HeadlessException
  {
    // test only

    if (!isInteractiveMode())
    {
      return getMockResponse().toString();
    }

    return JOptionPane.showInputDialog(message);
  }

  /**
   * adds inital selection value
   * 
   * @param message
   * @param initialSelectionValue
   * @return
   */
  public static String showInputDialog(String message,
          String initialSelectionValue)
  {
    if (!isInteractiveMode())
    {
      return getMockResponse().toString();
    }

    // AnnotationPanel character option

    return JOptionPane.showInputDialog(message, initialSelectionValue);
  }

  /**
   * adds inital selection value
   * 
   * @param message
   * @param initialSelectionValue
   * @return
   */
  public static String showInputDialog(Object message,
          Object initialSelectionValue)
  {
    if (!isInteractiveMode())
    {
      return getMockResponse().toString();
    }

    // AnnotationPanel character option

    return JOptionPane.showInputDialog(message, initialSelectionValue);
  }

  /**
   * centered on parent
   * 
   * @param parentComponent
   * @param message
   * @return
   * @throws HeadlessException
   */
  public static String showInputDialog(Component parentComponent,
          String message) throws HeadlessException
  {
    // test only

    return isInteractiveMode()
            ? JOptionPane.showInputDialog(parentComponent, message)
            : getMockResponse().toString();
  }

  /**
   * input with initial selection
   * 
   * @param parentComponent
   * @param message
   * @param initialSelectionValue
   * @return
   */
  public static String showInputDialog(Component parentComponent,
          String message, String initialSelectionValue)
  {

    // AnnotationPanel

    return isInteractiveMode()
            ? JOptionPane.showInputDialog(parentComponent, message,
                    initialSelectionValue)
            : getMockResponse().toString();
  }

  /**
   * input with initial selection
   * 
   * @param parentComponent
   * @param message
   * @param initialSelectionValue
   * @return
   */
  public static String showInputDialog(Component parentComponent,
          Object message, Object initialSelectionValue)
  {

    // AnnotationPanel

    return isInteractiveMode()
            ? JOptionPane.showInputDialog(parentComponent, message,
                    initialSelectionValue)
            : getMockResponse().toString();
  }

  /**
   * 
   * @param parentComponent
   * @param message
   * @param title
   * @param messageType
   * @return
   * @throws HeadlessException
   */
  public static String showInputDialog(Component parentComponent,
          String message, String title, int messageType)
          throws HeadlessException
  {

    // test only

    return isInteractiveMode()
            ? JOptionPane.showInputDialog(parentComponent, message, title,
                    messageType)
            : getMockResponse().toString();
  }

  /**
   * Customized input option
   * 
   * @param parentComponent
   * @param message
   * @param title
   * @param messageType
   * @param icon
   * @param selectionValues
   * @param initialSelectionValue
   * @return
   * @throws HeadlessException
   */
  public static Object showInputDialog(Component parentComponent,
          Object message, String title, int messageType, Icon icon,
          Object[] selectionValues, Object initialSelectionValue)
          throws HeadlessException
  {

    // test only

    return isInteractiveMode()
            ? JOptionPane.showInputDialog(parentComponent, message, title,
                    messageType, icon, selectionValues,
                    initialSelectionValue)
            : getMockResponse().toString();
  }

  /**
   * internal version
   * 
   * @param parentComponent
   * @param message
   * @return
   */
  public static String showInternalInputDialog(Component parentComponent,
          String message)
  {
    // test only

    return isInteractiveMode()
            ? JOptionPane.showInternalInputDialog(parentComponent, message)
            : getMockResponse().toString();
  }

  /**
   * internal with title and messageType
   * 
   * @param parentComponent
   * @param message
   * @param title
   * @param messageType
   * @return
   */
  public static String showInternalInputDialog(Component parentComponent,
          String message, String title, int messageType)
  {

    // AlignFrame tabbedPane_mousePressed

    return isInteractiveMode()
            ? JOptionPane.showInternalInputDialog(parentComponent,
                    getPrefix(messageType) + message, title, messageType)
            : getMockResponse().toString();
  }

  /**
   * customized internal
   * 
   * @param parentComponent
   * @param message
   * @param title
   * @param messageType
   * @param icon
   * @param selectionValues
   * @param initialSelectionValue
   * @return
   */
  public static Object showInternalInputDialog(Component parentComponent,
          String message, String title, int messageType, Icon icon,
          Object[] selectionValues, Object initialSelectionValue)
  {
    // test only

    return isInteractiveMode()
            ? JOptionPane.showInternalInputDialog(parentComponent, message,
                    title, messageType, icon, selectionValues,
                    initialSelectionValue)
            : getMockResponse().toString();
  }

  ///////////// end of options ///////////////

  private static void outputMessage(Object message)
  {
    System.out.println(">>> JOption Message : " + message.toString());
  }

  public static Object getMockResponse()
  {
    return mockResponse;
  }

  public static void setMockResponse(Object mockOption)
  {
    JvOptionPane.mockResponse = mockOption;
  }

  public static void resetMock()
  {
    setMockResponse(JvOptionPane.CANCEL_OPTION);
    setInteractiveMode(true);
  }

  public static boolean isInteractiveMode()
  {
    return interactiveMode;
  }

  public static void setInteractiveMode(boolean interactive)
  {
    JvOptionPane.interactiveMode = interactive;
  }

  private static String getPrefix(int messageType)
  {
    String prefix = "";

    // JavaScript only
    if (Platform.isJS())
    {
      switch (messageType)
      {
      case JvOptionPane.WARNING_MESSAGE:
        prefix = "WARNING! ";
        break;
      case JvOptionPane.ERROR_MESSAGE:
        prefix = "ERROR! ";
        break;
      default:
        prefix = "Note: ";
      }
    }
    return prefix;
  }

  /**
   * create a new option dialog that can be used to register responses - along
   * lines of showOptionDialog
   * 
   * @param desktop
   * @param question
   * @param string
   * @param defaultOption
   * @param plainMessage
   * @param object
   * @param options
   * @param string2
   * @return
   */
  public static JvOptionPane newOptionDialog()
  {
    return new JvOptionPane(null);
  }

  public static JvOptionPane newOptionDialog(Component parentComponent)
  {
    return new JvOptionPane(parentComponent);
  }

  public void showDialog(String message, String title, int optionType,
          int messageType, Icon icon, Object[] options, Object initialValue)
  {
    showDialog(message, title, optionType, messageType, icon, options,
            initialValue, true);
  }

  public void showDialog(Object message, String title, int optionType,
          int messageType, Icon icon, Object[] options, Object initialValue,
          boolean modal)
  {
    showDialog(message, title, optionType, messageType, icon, options,
            initialValue, modal, null);
  }

  public void showDialog(Object message, String title, int optionType,
          int messageType, Icon icon, Object[] options, Object initialValue,
          boolean modal, JButton[] buttons)
  {
    if (!isInteractiveMode())
    {
      handleResponse(getMockResponse());
      return;
    }
    // two uses:
    //
    // TODO
    //
    // 1) AlignViewport for openLinkedAlignment
    //
    // Show a dialog with the option to open and link (cDNA <-> protein) as a
    // new
    // alignment, either as a standalone alignment or in a split frame. Returns
    // true if the new alignment was opened, false if not, because the user
    // declined the offer.
    //
    // 2) UserDefinedColors warning about saving over a name already defined
    //

    ourOptions = Arrays.asList(options);

    if (modal)
    {
      boolean useButtons = false;
      Object initialValueButton = null;
      NOTNULL: if (buttons != null)
      {
        if (buttons.length != options.length)
        {
          jalview.bin.Console.error(
                  "Supplied buttons array not the same length as supplied options array.");
          break NOTNULL;
        }
        int[] buttonActions = { JOptionPane.YES_OPTION,
            JOptionPane.NO_OPTION, JOptionPane.CANCEL_OPTION };
        for (int i = 0; i < options.length; i++)
        {
          Object o = options[i];
          jalview.bin.Console.debug(
                  "Setting button " + i + " to '" + o.toString() + "'");
          JButton jb = buttons[i];

          if (o.equals(initialValue))
            initialValueButton = jb;

          int buttonAction = buttonActions[i];
          Runnable action = callbacks.get(buttonAction);
          jb.setText((String) o);
          jb.addActionListener(new ActionListener()
          {
            @Override
            public void actionPerformed(ActionEvent e)
            {

              Object obj = e.getSource();
              if (obj == null || !(obj instanceof Component))
              {
                jalview.bin.Console.debug(
                        "Could not find Component source of event object "
                                + obj);
                return;
              }
              Object joptionpaneObject = SwingUtilities.getAncestorOfClass(
                      JOptionPane.class, (Component) obj);
              if (joptionpaneObject == null
                      || !(joptionpaneObject instanceof JOptionPane))
              {
                jalview.bin.Console.debug(
                        "Could not find JOptionPane ancestor of event object "
                                + obj);
                return;
              }
              JOptionPane joptionpane = (JOptionPane) joptionpaneObject;
              joptionpane.setValue(buttonAction);
              if (action != null)
                new Thread(action).start();
              joptionpane.transferFocusBackward();
              joptionpane.setVisible(false);
              // put focus and raise parent window if possible, unless cancel or
              // no button pressed
              boolean raiseParent = (parentComponent != null);
              if (buttonAction == JOptionPane.CANCEL_OPTION)
                raiseParent = false;
              if (optionType == JOptionPane.YES_NO_OPTION
                      && buttonAction == JOptionPane.NO_OPTION)
                raiseParent = false;
              if (raiseParent)
              {
                parentComponent.requestFocus();
                if (parentComponent instanceof JInternalFrame)
                {
                  JInternalFrame jif = (JInternalFrame) parentComponent;
                  jif.show();
                  jif.moveToFront();
                  jif.grabFocus();
                }
                else if (parentComponent instanceof Window)
                {
                  Window w = (Window) parentComponent;
                  w.toFront();
                  w.requestFocus();
                }
              }
              joptionpane.setVisible(false);
            }
          });

        }
        useButtons = true;
      }
      // use a JOptionPane as usual
      int response = JOptionPane.showOptionDialog(parentComponent, message,
              title, optionType, messageType, icon,
              useButtons ? buttons : options,
              useButtons ? initialValueButton : initialValue);

      /*
       * In Java, the response is returned to this thread and handled here; (for
       * Javascript, see propertyChange)
       */
      if (!Platform.isJS())
      /**
       * Java only
       * 
       * @j2sIgnore
       */
      {
        handleResponse(response);
      }
    }
    else
    {
      /*
       * This is java similar to the swingjs handling, with the callbacks attached to
       * the button press of the dialog. This means we can use a non-modal JDialog for
       * the confirmation without blocking the GUI.
       */
      JOptionPane joptionpane = new JOptionPane();
      // Make button options
      int[] buttonActions = { JvOptionPane.YES_OPTION,
          JvOptionPane.NO_OPTION, JvOptionPane.CANCEL_OPTION };

      // we need the strings to make the buttons with actionEventListener
      if (options == null)
      {
        ArrayList<String> options_default = new ArrayList<>();
        options_default
                .add(UIManager.getString("OptionPane.yesButtonText"));
        if (optionType == JvOptionPane.YES_NO_OPTION
                || optionType == JvOptionPane.YES_NO_CANCEL_OPTION)
        {
          options_default
                  .add(UIManager.getString("OptionPane.noButtonText"));
        }
        if (optionType == JvOptionPane.YES_NO_CANCEL_OPTION)
        {
          options_default
                  .add(UIManager.getString("OptionPane.cancelButtonText"));
        }
        options = options_default.toArray();
      }

      ArrayList<JButton> options_btns = new ArrayList<>();
      Object initialValue_btn = null;
      if (!Platform.isJS()) // JalviewJS already uses callback, don't need to
                            // add them here
      {
        for (int i = 0; i < options.length && i < 3; i++)
        {
          Object o = options[i];
          int buttonAction = buttonActions[i];
          Runnable action = callbacks.get(buttonAction);
          JButton jb = new JButton();
          jb.setText((String) o);
          jb.addActionListener(new ActionListener()
          {

            @Override
            public void actionPerformed(ActionEvent e)
            {
              joptionpane.setValue(buttonAction);
              if (action != null)
                new Thread(action).start();
              // joptionpane.transferFocusBackward();
              joptionpane.transferFocusBackward();
              joptionpane.setVisible(false);
              // put focus and raise parent window if possible, unless cancel
              // button pressed
              boolean raiseParent = (parentComponent != null);
              if (buttonAction == JvOptionPane.CANCEL_OPTION)
                raiseParent = false;
              if (optionType == JvOptionPane.YES_NO_OPTION
                      && buttonAction == JvOptionPane.NO_OPTION)
                raiseParent = false;
              if (raiseParent)
              {
                parentComponent.requestFocus();
                if (parentComponent instanceof JInternalFrame)
                {
                  JInternalFrame jif = (JInternalFrame) parentComponent;
                  jif.show();
                  jif.moveToFront();
                  jif.grabFocus();
                }
                else if (parentComponent instanceof Window)
                {
                  Window w = (Window) parentComponent;
                  w.toFront();
                  w.requestFocus();
                }
              }
              joptionpane.setVisible(false);
            }
          });
          options_btns.add(jb);
          if (o.equals(initialValue))
            initialValue_btn = jb;
        }
      }
      joptionpane.setMessage(message);
      joptionpane.setMessageType(messageType);
      joptionpane.setOptionType(optionType);
      joptionpane.setIcon(icon);
      joptionpane.setOptions(
              Platform.isJS() ? options : options_btns.toArray());
      joptionpane.setInitialValue(
              Platform.isJS() ? initialValue : initialValue_btn);

      JDialog dialog = joptionpane.createDialog(parentComponent, title);
      dialog.setIconImages(ChannelProperties.getIconList());
      dialog.setModalityType(modal ? ModalityType.APPLICATION_MODAL
              : ModalityType.MODELESS);
      dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      dialog.setVisible(true);
      setDialog(dialog);
    }
  }

  public void showInternalDialog(Object mainPanel, String title,
          int yesNoCancelOption, int questionMessage, Icon icon,
          Object[] options, String initresponse)
  {
    if (!isInteractiveMode())
    {
      handleResponse(getMockResponse());
    }

    // need to set these separately so we can set the title bar icon later
    this.setOptionType(yesNoCancelOption);
    this.setMessageType(questionMessage);
    this.setIcon(icon);
    this.setInitialValue(initresponse);
    this.setOptions(options);
    this.setMessage(mainPanel);

    ourOptions = Arrays.asList(options);
    if (parentComponent != this
            && !(parentComponent == null && Desktop.instance == null))
    {
      JInternalFrame jif = this.createInternalFrame(
              parentComponent != null ? parentComponent : Desktop.instance,
              title);
      jif.setFrameIcon(null);
      jif.addInternalFrameListener(new InternalFrameListener()
      {
        @Override
        public void internalFrameActivated(InternalFrameEvent arg0)
        {
        }

        @Override
        public void internalFrameClosed(InternalFrameEvent arg0)
        {
          JvOptionPane.this.internalDialogHandleResponse();
        }

        @Override
        public void internalFrameClosing(InternalFrameEvent arg0)
        {
        }

        @Override
        public void internalFrameDeactivated(InternalFrameEvent arg0)
        {
        }

        @Override
        public void internalFrameDeiconified(InternalFrameEvent arg0)
        {
        }

        @Override
        public void internalFrameIconified(InternalFrameEvent arg0)
        {
        }

        @Override
        public void internalFrameOpened(InternalFrameEvent arg0)
        {
        }
      });
      jif.setVisible(true);
      startModal(jif);
      return;
    }
    else
    {
      JDialog dialog = this.createDialog(parentComponent, title);
      dialog.setIconImages(ChannelProperties.getIconList());
      dialog.setVisible(true); // blocking
      this.internalDialogHandleResponse();
      return;
    }
  }

  private void internalDialogHandleResponse()
  {
    String responseString = (String) this.getValue();
    int response = ourOptions.indexOf(responseString);

    if (!Platform.isJS())
    /**
     * Java only
     * 
     * @j2sIgnore
     */
    {
      handleResponse(response);
    }
  }

  /*
   * @Override public JvOptionPane setResponseHandler(Object response, Runnable
   * action) { callbacks.put(response, new Callable<Void>() {
   * 
   * @Override public Void call() { action.run(); return null; } }); return this;
   * }
   */
  @Override
  public JvOptionPane setResponseHandler(Object response, Runnable action)
  {
    if (action == null)
    {
      action = NULLCALLABLE;
    }
    callbacks.put(response, action);
    return this;
  }

  public void setDialog(JDialog d)
  {
    dialog = d;
  }

  public JDialog getDialog()
  {
    return dialog;
  }

  /**
   * showDialogOnTop will create a dialog that (attempts to) come to top of OS
   * desktop windows
   */
  public static int showDialogOnTop(String label, String actionString,
          int JOPTIONPANE_OPTION, int JOPTIONPANE_MESSAGETYPE)
  {
    return showDialogOnTop(null, label, actionString, JOPTIONPANE_OPTION,
            JOPTIONPANE_MESSAGETYPE);
  }

  public static int showDialogOnTop(Component dialogParentComponent,
          String label, String actionString, int JOPTIONPANE_OPTION,
          int JOPTIONPANE_MESSAGETYPE)
  {
    if (!isInteractiveMode())
    {
      return (int) getMockResponse();
    }
    // Ensure Jalview window is brought to front (primarily for Quit
    // confirmation window to be visible)

    // This method of raising the Jalview window is broken in java
    // jalviewDesktop.setVisible(true);
    // jalviewDesktop.toFront();

    // A better hack which works is to create a new JFrame parent with
    // setAlwaysOnTop(true)
    JFrame dialogParent = new JFrame();
    if (dialogParentComponent == null)
    {
      dialogParent.setIconImages(ChannelProperties.getIconList());
      dialogParent.setAlwaysOnTop(true);
    }

    int answer = JOptionPane.showConfirmDialog(
            dialogParentComponent == null ? dialogParent
                    : dialogParentComponent,
            label, actionString, JOPTIONPANE_OPTION,
            JOPTIONPANE_MESSAGETYPE);

    if (dialogParentComponent == null)
    {
      dialogParent.setAlwaysOnTop(false);
      dialogParent.dispose();
    }

    return answer;
  }

  public void showDialogOnTopAsync(String label, String actionString,
          int JOPTIONPANE_OPTION, int JOPTIONPANE_MESSAGETYPE, Icon icon,
          Object[] options, Object initialValue, boolean modal)
  {
    JFrame frame = new JFrame();
    frame.setIconImages(ChannelProperties.getIconList());
    showDialogOnTopAsync(frame, label, actionString, JOPTIONPANE_OPTION,
            JOPTIONPANE_MESSAGETYPE, icon, options, initialValue, modal);
  }

  public void showDialogOnTopAsync(JFrame dialogParent, Object label,
          String actionString, int JOPTIONPANE_OPTION,
          int JOPTIONPANE_MESSAGETYPE, Icon icon, Object[] options,
          Object initialValue, boolean modal)
  {
    showDialogOnTopAsync(dialogParent, label, actionString,
            JOPTIONPANE_OPTION, JOPTIONPANE_MESSAGETYPE, icon, options,
            initialValue, modal, null);
  }

  public void showDialogOnTopAsync(JFrame dialogParent, Object label,
          String actionString, int JOPTIONPANE_OPTION,
          int JOPTIONPANE_MESSAGETYPE, Icon icon, Object[] options,
          Object initialValue, boolean modal, JButton[] buttons)
  {
    if (!isInteractiveMode())
    {
      handleResponse(getMockResponse());
      return;
    }
    // Ensure Jalview window is brought to front (primarily for Quit
    // confirmation window to be visible)

    // This method of raising the Jalview window is broken in java
    // jalviewDesktop.setVisible(true);
    // jalviewDesktop.toFront();

    // A better hack which works is to create a new JFrame parent with
    // setAlwaysOnTop(true)
    dialogParent.setAlwaysOnTop(true);
    parentComponent = dialogParent;

    showDialog(label, actionString, JOPTIONPANE_OPTION,
            JOPTIONPANE_MESSAGETYPE, icon, options, initialValue, modal,
            buttons);

    dialogParent.setAlwaysOnTop(false);
    dialogParent.dispose();
  }

  /**
   * JalviewJS signals option selection by a property change event for the
   * option e.g. "OK". This methods responds to that by running the response
   * action that corresponds to that option.
   * 
   * @param evt
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    Object newValue = evt.getNewValue();
    int ourOption = ourOptions.indexOf(newValue);
    if (ourOption >= 0)
    {
      handleResponse(ourOption);
    }
    else
    {
      // try our luck..
      handleResponse(newValue);
    }
  }

  @Override
  public void handleResponse(Object response)
  {
    /*
     * this test is for NaN in Chrome
     */
    if (response != null && !response.equals(response))
    {
      return;
    }
    Runnable action = callbacks.get(response);
    if (action != null)
    {
      try
      {
        new Thread(action).start();
        // action.call();
      } catch (Exception e)
      {
        e.printStackTrace();
      }
      if (parentComponent != null)
        parentComponent.requestFocus();
    }
  }

  /**
   * Create a non-modal confirm dialog
   */
  public JDialog createDialog(Component parentComponent, Object message,
          String title, int optionType, int messageType, Icon icon,
          Object[] options, Object initialValue, boolean modal)
  {
    return createDialog(parentComponent, message, title, optionType,
            messageType, icon, options, initialValue, modal, null);
  }

  public JDialog createDialog(Component parentComponent, Object message,
          String title, int optionType, int messageType, Icon icon,
          Object[] options, Object initialValue, boolean modal,
          JButton[] buttons)
  {
    if (!isInteractiveMode())
    {
      handleResponse(getMockResponse());
      return null;
    }
    JButton[] optionsButtons = null;
    Object initialValueButton = null;
    JOptionPane joptionpane = new JOptionPane();
    // Make button options
    int[] buttonActions = { JOptionPane.YES_OPTION, JOptionPane.NO_OPTION,
        JOptionPane.CANCEL_OPTION };

    // we need the strings to make the buttons with actionEventListener
    if (options == null)
    {
      ArrayList<String> options_default = new ArrayList<>();
      options_default.add(UIManager.getString("OptionPane.yesButtonText"));
      if (optionType == JOptionPane.YES_NO_OPTION
              || optionType == JOptionPane.YES_NO_CANCEL_OPTION)
      {
        options_default.add(UIManager.getString("OptionPane.noButtonText"));
      }
      if (optionType == JOptionPane.YES_NO_CANCEL_OPTION)
      {
        options_default
                .add(UIManager.getString("OptionPane.cancelButtonText"));
      }
      options = options_default.toArray();
    }
    if (!Platform.isJS()) // JalviewJS already uses callback, don't need to
                          // add them here
    {
      if (((optionType == JOptionPane.YES_OPTION
              || optionType == JOptionPane.NO_OPTION
              || optionType == JOptionPane.CANCEL_OPTION
              || optionType == JOptionPane.OK_OPTION
              || optionType == JOptionPane.DEFAULT_OPTION)
              && options.length < 1)
              || ((optionType == JOptionPane.YES_NO_OPTION
                      || optionType == JOptionPane.OK_CANCEL_OPTION)
                      && options.length < 2)
              || (optionType == JOptionPane.YES_NO_CANCEL_OPTION
                      && options.length < 3))
      {
        jalview.bin.Console
                .debug("JvOptionPane: not enough options for dialog type");
      }
      optionsButtons = new JButton[options.length];
      for (int i = 0; i < options.length && i < 3; i++)
      {
        Object o = options[i];
        int buttonAction = buttonActions[i];
        Runnable action = callbacks.get(buttonAction);
        JButton jb;
        if (buttons != null && buttons.length > i && buttons[i] != null)
        {
          jb = buttons[i];
        }
        else
        {
          jb = new JButton();
        }
        jb.setText((String) o);
        jb.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            joptionpane.setValue(buttonAction);
            if (action != null)
              new Thread(action).start();
            // joptionpane.transferFocusBackward();
            joptionpane.transferFocusBackward();
            joptionpane.setVisible(false);
            // put focus and raise parent window if possible, unless cancel
            // button pressed
            boolean raiseParent = (parentComponent != null);
            if (buttonAction == JOptionPane.CANCEL_OPTION)
              raiseParent = false;
            if (optionType == JOptionPane.YES_NO_OPTION
                    && buttonAction == JOptionPane.NO_OPTION)
              raiseParent = false;
            if (raiseParent)
            {
              parentComponent.requestFocus();
              if (parentComponent instanceof JInternalFrame)
              {
                JInternalFrame jif = (JInternalFrame) parentComponent;
                jif.show();
                jif.moveToFront();
                jif.grabFocus();
              }
              else if (parentComponent instanceof Window)
              {
                Window w = (Window) parentComponent;
                w.toFront();
                w.requestFocus();
              }
            }
            joptionpane.setVisible(false);
          }
        });
        optionsButtons[i] = jb;
        if (o.equals(initialValue))
          initialValueButton = jb;
      }
    }
    joptionpane.setMessage(message);
    joptionpane.setMessageType(messageType);
    joptionpane.setOptionType(optionType);
    joptionpane.setIcon(icon);
    joptionpane.setOptions(Platform.isJS() ? options : optionsButtons);
    joptionpane.setInitialValue(
            Platform.isJS() ? initialValue : initialValueButton);

    JDialog dialog = joptionpane.createDialog(parentComponent, title);
    dialog.setIconImages(ChannelProperties.getIconList());
    dialog.setModalityType(
            modal ? ModalityType.APPLICATION_MODAL : ModalityType.MODELESS);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    setDialog(dialog);
    return dialog;
  }

  /**
   * Utility to programmatically click a button on a JOptionPane (as a JFrame)
   * 
   * returns true if button was found
   */
  public static boolean clickButton(JFrame frame, int buttonType)
  {

    return false;
  }

  /**
   * This helper method makes the JInternalFrame wait until it is notified by an
   * InternalFrameClosing event. This method also adds the given JOptionPane to
   * the JInternalFrame and sizes it according to the JInternalFrame's preferred
   * size.
   *
   * @param f
   *          The JInternalFrame to make modal.
   */
  private static void startModal(JInternalFrame f)
  {
    // We need to add an additional glasspane-like component directly
    // below the frame, which intercepts all mouse events that are not
    // directed at the frame itself.
    JPanel modalInterceptor = new JPanel();
    modalInterceptor.setOpaque(false);
    JLayeredPane lp = JLayeredPane.getLayeredPaneAbove(f);
    lp.setLayer(modalInterceptor, JLayeredPane.MODAL_LAYER.intValue());
    modalInterceptor.setBounds(0, 0, lp.getWidth(), lp.getHeight());
    modalInterceptor.addMouseListener(new MouseAdapter()
    {
    });
    modalInterceptor.addMouseMotionListener(new MouseMotionAdapter()
    {
    });
    lp.add(modalInterceptor);
    f.toFront();

    // We need to explicitly dispatch events when we are blocking the event
    // dispatch thread.
    EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
    try
    {
      while (!f.isClosed())
      {
        if (EventQueue.isDispatchThread())
        {
          // The getNextEventMethod() issues wait() when no
          // event is available, so we don't need do explicitly wait().
          AWTEvent ev = queue.getNextEvent();
          // This mimics EventQueue.dispatchEvent(). We can't use
          // EventQueue.dispatchEvent() directly, because it is
          // protected, unfortunately.
          if (ev instanceof ActiveEvent)
            ((ActiveEvent) ev).dispatch();
          else if (ev.getSource() instanceof Component)
            ((Component) ev.getSource()).dispatchEvent(ev);
          else if (ev.getSource() instanceof MenuComponent)
            ((MenuComponent) ev.getSource()).dispatchEvent(ev);
          // Other events are ignored as per spec in
          // EventQueue.dispatchEvent
        }
        else
        {
          // Give other threads a chance to become active.
          Thread.yield();
        }
      }
    } catch (InterruptedException ex)
    {
      // If we get interrupted, then leave the modal state.
    } finally
    {
      // Clean up the modal interceptor.
      lp.remove(modalInterceptor);

      // Remove the internal frame from its parent, so it is no longer
      // lurking around and clogging memory.
      Container parent = f.getParent();
      if (parent != null)
        parent.remove(f);
    }
  }

  public static JvOptionPane frameDialog(Object message, String title,
          int messageType, String[] buttonsTextS, String defaultButtonS,
          List<Runnable> handlers, boolean modal)
  {
    JFrame parent = new JFrame();
    JvOptionPane jvop = JvOptionPane.newOptionDialog();
    final String[] buttonsText;
    final String defaultButton;
    if (buttonsTextS == null)
    {
      String ok = MessageManager.getString("action.ok");
      buttonsText = new String[] { ok };
      defaultButton = ok;
    }
    else
    {
      buttonsText = buttonsTextS;
      defaultButton = defaultButtonS;
    }
    JButton[] buttons = new JButton[buttonsText.length];
    for (int i = 0; i < buttonsText.length; i++)
    {
      buttons[i] = new JButton();
      buttons[i].setText(buttonsText[i]);
      Console.debug("DISABLING BUTTON " + buttons[i].getText());
      buttons[i].setEnabled(false);
      buttons[i].setVisible(false);
    }

    int dialogType = -1;
    if (buttonsText.length == 1)
    {
      dialogType = JOptionPane.OK_OPTION;
    }
    else if (buttonsText.length == 2)
    {
      dialogType = JOptionPane.YES_NO_OPTION;
    }
    else
    {
      dialogType = JOptionPane.YES_NO_CANCEL_OPTION;
    }
    jvop.setResponseHandler(JOptionPane.YES_OPTION,
            (handlers != null && handlers.size() > 0) ? handlers.get(0)
                    : NULLCALLABLE);
    if (dialogType == JOptionPane.YES_NO_OPTION
            || dialogType == JOptionPane.YES_NO_CANCEL_OPTION)
    {
      jvop.setResponseHandler(JOptionPane.NO_OPTION,
              (handlers != null && handlers.size() > 1) ? handlers.get(1)
                      : NULLCALLABLE);
    }
    if (dialogType == JOptionPane.YES_NO_CANCEL_OPTION)
    {
      jvop.setResponseHandler(JOptionPane.CANCEL_OPTION,
              (handlers != null && handlers.size() > 2) ? handlers.get(2)
                      : NULLCALLABLE);
    }

    final int dt = dialogType;
    new Thread(() -> {
      jvop.showDialog(message, title, dt, messageType, null, buttonsText,
              defaultButton, modal, buttons);
    }).start();

    return jvop;
  }
}
