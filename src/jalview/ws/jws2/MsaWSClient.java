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
package jalview.ws.jws2;

import java.util.Locale;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.ToolTipManager;

import compbio.data.msa.MsaWS;
import compbio.metadata.Argument;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AlignmentView;
import jalview.gui.AlignFrame;
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;
import jalview.gui.JvSwingUtils;
import jalview.util.MessageManager;
import jalview.ws.jws2.jabaws2.Jws2Instance;
import jalview.ws.params.WsParamSetI;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class MsaWSClient extends Jws2Client
{
  /**
   * server is a WSDL2Java generated stub for an archetypal MsaWSI service.
   */
  MsaWS server;

  public MsaWSClient(Jws2Instance sh, String altitle,
          jalview.datamodel.AlignmentView msa, boolean submitGaps,
          boolean preserveOrder, AlignmentI seqdataset,
          AlignFrame _alignFrame)
  {
    this(sh, null, null, false, altitle, msa, submitGaps, preserveOrder,
            seqdataset, _alignFrame);
    // TODO Auto-generated constructor stub
  }

  public MsaWSClient(Jws2Instance sh, WsParamSetI preset, String altitle,
          jalview.datamodel.AlignmentView msa, boolean submitGaps,
          boolean preserveOrder, AlignmentI seqdataset,
          AlignFrame _alignFrame)
  {
    this(sh, preset, null, false, altitle, msa, submitGaps, preserveOrder,
            seqdataset, _alignFrame);
    // TODO Auto-generated constructor stub
  }

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

  public MsaWSClient(Jws2Instance sh, WsParamSetI preset,
          List<Argument> arguments, boolean editParams, String altitle,
          jalview.datamodel.AlignmentView msa, boolean submitGaps,
          boolean preserveOrder, AlignmentI seqdataset,
          AlignFrame _alignFrame)
  {
    super(_alignFrame, preset, arguments);
    if (!processParams(sh, editParams))
    {
      return;
    }

    if (!(sh.service instanceof MsaWS))
    {
      // redundant at mo - but may change
      JvOptionPane.showMessageDialog(Desktop.desktop,
              MessageManager.formatMessage(
                      "label.service_called_is_not_msa_service",
                      new String[]
                      { sh.serviceType }),
              MessageManager.getString("label.internal_jalview_error"),
              JvOptionPane.WARNING_MESSAGE);

      return;
    }
    server = (MsaWS) sh.service;
    if ((wsInfo = setWebService(sh, false)) == null)
    {
      JvOptionPane.showMessageDialog(Desktop.desktop, MessageManager
              .formatMessage("label.msa_service_is_unknown", new String[]
              { sh.serviceType }),
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
    // if (!locateWebService())
    // {
    // return;
    // }

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

    MsaWSThread msathread = new MsaWSThread(server, preset, paramset, WsURL,
            wsInfo, alignFrame, WebServiceName, jobtitle, msa, submitGaps,
            preserveOrder, seqdataset);
    if (msathread.hasValidInput())
    {
      wsInfo.setthisService(msathread);
      wsInfo.setVisible(true);
      msathread.start();
    }
    else
    {
      wsInfo.setVisible(false);
      JvOptionPane.showMessageDialog(alignFrame,
              MessageManager.getString("info.invalid_msa_input_mininfo"),
              MessageManager.getString("info.invalid_msa_notenough"),
              JvOptionPane.INFORMATION_MESSAGE);
    }
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
  public void attachWSMenuEntry(JMenu rmsawsmenu,
          final Jws2Instance service, final AlignFrame af)
  {
    if (registerAAConWSInstance(rmsawsmenu, service, af))
    {
      // Alignment dependent analysis calculation WS gui
      return;
    }
    setWebService(service, true); // headless
    boolean finished = true, submitGaps = false;
    JMenu msawsmenu = rmsawsmenu;
    String svcname = WebServiceName;
    if (svcname.endsWith("WS"))
    {
      svcname = svcname.substring(0, svcname.length() - 2);
    }
    String calcName = svcname + " ";
    if (canSubmitGaps())
    {
      msawsmenu = new JMenu(svcname);
      rmsawsmenu.add(msawsmenu);
      calcName = "";
    }
    boolean hasparams = service.hasParameters();
    do
    {
      String action = "Align ";
      if (submitGaps == true)
      {
        action = "Realign ";
        msawsmenu = new JMenu(MessageManager
                .formatMessage("label.realign_with_params", new String[]
                { svcname }));
        msawsmenu.setToolTipText(MessageManager
                .getString("label.align_sequences_to_existing_alignment"));
        rmsawsmenu.add(msawsmenu);
      }
      final boolean withGaps = submitGaps;

      JMenuItem method = new JMenuItem(MessageManager.formatMessage(
              "label.calcname_with_default_settings", new String[]
              { calcName }));
      method.setToolTipText(MessageManager.formatMessage(
              "label.action_with_default_settings", new String[]
              { action }));

      method.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          AlignmentView msa = af.gatherSequencesForAlignment();

          if (msa != null)
          {
            new MsaWSClient(service, af.getTitle(), msa, withGaps, true,
                    af.getViewport().getAlignment().getDataset(), af);
          }

        }
      });
      msawsmenu.add(method);
      if (hasparams)
      {
        // only add these menu options if the service has user-modifiable
        // arguments
        method = new JMenuItem(
                MessageManager.getString("label.edit_settings_and_run"));
        method.setToolTipText(MessageManager.getString(
                "label.view_and_change_parameters_before_alignment"));

        method.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            AlignmentView msa = af.gatherSequencesForAlignment();
            if (msa != null)
            {
              startJob(service, af, withGaps, msa);
            }

          }
        });
        msawsmenu.add(method);
        List<WsParamSetI> presets = service.getParamStore().getPresets();
        if (presets != null && presets.size() > 0)
        {
          JMenu presetlist = new JMenu(MessageManager.formatMessage(
                  "label.run_with_preset_params", new String[]
                  { calcName }));

          final int showToolTipFor = ToolTipManager.sharedInstance()
                  .getDismissDelay();
          for (final WsParamSetI preSet : presets)
          {
            final JMenuItem methodR = new JMenuItem(preSet.getName());
            final int QUICK_TOOLTIP = 1500;
            // JAL-1582 shorten tooltip display time in these menu items as
            // they can obscure other options
            methodR.addMouseListener(new MouseAdapter()
            {
              @Override
              public void mouseEntered(MouseEvent e)
              {
                ToolTipManager.sharedInstance()
                        .setDismissDelay(QUICK_TOOLTIP);
              }

              @Override
              public void mouseExited(MouseEvent e)
              {
                ToolTipManager.sharedInstance()
                        .setDismissDelay(showToolTipFor);
              }

            });
            String tooltip = JvSwingUtils.wrapTooltip(true, "<strong>"
                    + (preSet.isModifiable()
                            ? MessageManager.getString("label.user_preset")
                            : MessageManager
                                    .getString("label.service_preset"))
                    + "</strong><br/>" + preSet.getDescription());
            methodR.setToolTipText(tooltip);
            methodR.addActionListener(new ActionListener()
            {
              @Override
              public void actionPerformed(ActionEvent e)
              {
                AlignmentView msa = af.gatherSequencesForAlignment();

                if (msa != null)
                {
                  MsaWSClient msac = new MsaWSClient(service, preSet,
                          af.getTitle(), msa, false, true,
                          af.getViewport().getAlignment().getDataset(), af);
                }

              }

            });
            presetlist.add(methodR);
          }
          msawsmenu.add(presetlist);
        }
      }
      if (!submitGaps && canSubmitGaps())
      {
        submitGaps = true;
        finished = false;
      }
      else
      {
        finished = true;
      }
    } while (!finished);
  }

  protected void startJob(final Jws2Instance service, final AlignFrame af,
          final boolean withGaps, AlignmentView msa)
  {
    try
    {
      new MsaWSClient(service, null, null, true, af.getTitle(), msa,
              withGaps, true, af.getViewport().getAlignment().getDataset(),
              af);
    } catch (Exception e)
    {
      JvOptionPane.showMessageDialog(alignFrame, e.getMessage(),
              MessageManager.getString("label.state_job_error"),
              JvOptionPane.WARNING_MESSAGE);

    }
  }
}
