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
package jalview.ws.jws1;

import java.util.Locale;

import jalview.datamodel.AlignmentI;
import jalview.datamodel.AlignmentView;
import jalview.gui.AlignFrame;
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;
import jalview.gui.WebserviceInfo;
import jalview.util.MessageManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import ext.vamsas.MuscleWSServiceLocator;
import ext.vamsas.MuscleWSSoapBindingStub;
import ext.vamsas.ServiceHandle;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class MsaWSClient extends WS1Client
{
  /**
   * server is a WSDL2Java generated stub for an archetypal MsaWSI service.
   */
  ext.vamsas.MuscleWS server;

  AlignFrame alignFrame;

  /**
   * Creates a new MsaWSClient object that uses a service given by an externally
   * retrieved ServiceHandle
   * 
   * @param sh
   *          service handle of type AbstractName(MsaWS)
   * @param altitle
   *          DOCUMENT ME!
   * @param msa
   *          DOCUMENT ME!
   * @param submitGaps
   *          DOCUMENT ME!
   * @param preserveOrder
   *          DOCUMENT ME!
   */

  public MsaWSClient(ext.vamsas.ServiceHandle sh, String altitle,
          jalview.datamodel.AlignmentView msa, boolean submitGaps,
          boolean preserveOrder, AlignmentI seqdataset,
          AlignFrame _alignFrame)
  {
    super();
    alignFrame = _alignFrame;
    if (!sh.getAbstractName().equals("MsaWS"))
    {
      JvOptionPane.showMessageDialog(Desktop.desktop,
              MessageManager.formatMessage(
                      "label.service_called_is_not_msa_service",
                      new String[]
                      { sh.getName() }),
              MessageManager.getString("label.internal_jalview_error"),
              JvOptionPane.WARNING_MESSAGE);

      return;
    }

    if ((wsInfo = setWebService(sh)) == null)
    {
      JvOptionPane.showMessageDialog(Desktop.desktop, MessageManager
              .formatMessage("label.msa_service_is_unknown", new String[]
              { sh.getName() }),
              MessageManager.getString("label.internal_jalview_error"),
              JvOptionPane.WARNING_MESSAGE);

      return;
    }
    startMsaWSClient(altitle, msa, submitGaps, preserveOrder, seqdataset);

  }

  public MsaWSClient()
  {
    super();
    // add a class reference to the list
  }

  private void startMsaWSClient(String altitle, AlignmentView msa,
          boolean submitGaps, boolean preserveOrder, AlignmentI seqdataset)
  {
    if (!locateWebService())
    {
      return;
    }

    wsInfo.setProgressText(((submitGaps) ? "Re-alignment" : "Alignment")
            + " of " + altitle + "\nJob details\n");
    String jobtitle = WebServiceName.toLowerCase(Locale.ROOT);
    if (jobtitle.endsWith("alignment"))
    {
      if (submitGaps && (!jobtitle.endsWith("realignment")
              || jobtitle.indexOf("profile") == -1))
      {
        int pos = jobtitle.indexOf("alignment");
        jobtitle = WebServiceName.substring(0, pos) + "re-alignment of "
                + altitle;
      }
      else
      {
        jobtitle = WebServiceName + " of " + altitle;
      }
    }
    else
    {
      jobtitle = WebServiceName + (submitGaps ? " re" : " ")
              + "alignment of " + altitle;
    }

    MsaWSThread msathread = new MsaWSThread(server, WsURL, wsInfo,
            alignFrame, WebServiceName, jobtitle, msa, submitGaps,
            preserveOrder, seqdataset);
    wsInfo.setthisService(msathread);
    msathread.start();
  }

  /**
   * Initializes the server field with a valid service implementation.
   * 
   * @return true if service was located.
   */
  private boolean locateWebService()
  {
    // TODO: MuscleWS transmuted to generic MsaWS client
    MuscleWSServiceLocator loc = new MuscleWSServiceLocator(); // Default

    try
    {
      this.server = loc.getMuscleWS(new java.net.URL(WsURL));
      ((MuscleWSSoapBindingStub) this.server).setTimeout(60000); // One minute
      // timeout
    } catch (Exception ex)
    {
      wsInfo.setProgressText("Serious! " + WebServiceName
              + " Service location failed\nfor URL :" + WsURL + "\n"
              + ex.getMessage());
      wsInfo.setStatus(WebserviceInfo.ERROR);
      ex.printStackTrace();

      return false;
    }

    loc.getEngine().setOption("axis", "1");

    return true;
  }

  protected String getServiceActionKey()
  {
    return "MsaWS";
  }

  protected String getServiceActionDescription()
  {
    return "Multiple Sequence Alignment";
  }

  /**
   * look at ourselves and work out if we are a service that can take a profile
   * and align to it
   * 
   * @return true if we can send gapped sequences to the alignment service
   */
  private boolean canSubmitGaps()
  {
    // TODO: query service or extract service handle props to check if we can
    // realign
    return (WebServiceName.indexOf("lustal") > -1); // cheat!
  }

  @Override
  public void attachWSMenuEntry(JMenu msawsmenu,
          final ServiceHandle serviceHandle, final AlignFrame alignFrame)
  {
    setWebService(serviceHandle, true); // headless
    JMenuItem method = new JMenuItem(WebServiceName);
    method.setToolTipText(WsURL);
    method.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        AlignmentView msa = alignFrame.gatherSequencesForAlignment();
        new jalview.ws.jws1.MsaWSClient(serviceHandle,
                alignFrame.getTitle(), msa, false, true,
                alignFrame.getViewport().getAlignment().getDataset(),
                alignFrame);

      }

    });
    msawsmenu.add(method);
    if (canSubmitGaps())
    {
      // We know that ClustalWS can accept partial alignments for refinement.
      final JMenuItem methodR = new JMenuItem(
              serviceHandle.getName() + " Realign");
      methodR.setToolTipText(WsURL);
      methodR.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          AlignmentView msa = alignFrame.gatherSequencesForAlignment();
          new jalview.ws.jws1.MsaWSClient(serviceHandle,
                  alignFrame.getTitle(), msa, true, true,
                  alignFrame.getViewport().getAlignment().getDataset(),
                  alignFrame);

        }

      });
      msawsmenu.add(methodR);

    }

  }
}
