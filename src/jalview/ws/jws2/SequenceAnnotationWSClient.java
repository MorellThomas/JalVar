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

import jalview.api.AlignCalcWorkerI;
import jalview.gui.AlignFrame;
import jalview.gui.Desktop;
import jalview.gui.JvSwingUtils;
import jalview.util.MessageManager;
import jalview.ws.jws2.dm.AAConSettings;
import jalview.ws.jws2.jabaws2.Jws2Instance;
import jalview.ws.params.WsParamSetI;
import jalview.ws.uimodel.AlignAnalysisUIText;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * @author jprocter
 * 
 */
public class SequenceAnnotationWSClient extends Jws2Client
{
  /**
   * initialise a client so its attachWSMenuEntry method can be called.
   */
  public SequenceAnnotationWSClient()
  {
    // TODO Auto-generated constructor stub
  }

  public SequenceAnnotationWSClient(final Jws2Instance sh,
          AlignFrame alignFrame, WsParamSetI preset, boolean editParams)
  {
    super(alignFrame, preset, null);
    initSequenceAnnotationWSClient(sh, alignFrame, preset, editParams);
  }

  // dan think. Do I need to change this method to run RNAalifold through the
  // GUI

  public void initSequenceAnnotationWSClient(final Jws2Instance sh,
          AlignFrame alignFrame, WsParamSetI preset, boolean editParams)
  {
    // dan changed! dan test. comment out if conditional
    // if (alignFrame.getViewport().getAlignment().isNucleotide())
    // {
    // JvOptionPane.showMessageDialog(Desktop.desktop, sh.serviceType
    // + " can only be used\nfor amino acid alignments.",
    // "Wrong type of sequences!", JvOptionPane.WARNING_MESSAGE);
    // return;
    //
    // }
    AlignAnalysisUIText aaui = sh.getAlignAnalysisUI();
    if (aaui != null)
    {
      Class clientClass = aaui.getClient();

      // Build an AACon style client - take alignment, return annotation for
      // columns

      List<AlignCalcWorkerI> clnts = alignFrame.getViewport()
              .getCalcManager().getRegisteredWorkersOfClass(clientClass);
      AbstractJabaCalcWorker worker;
      if (clnts == null || clnts.size() == 0)
      {
        if (!processParams(sh, editParams))
        {
          return;
        }
        try
        {
          worker = (AbstractJabaCalcWorker) (clientClass
                  .getConstructor(new Class[]
                  { Jws2Instance.class, AlignFrame.class, WsParamSetI.class,
                      List.class })
                  .newInstance(new Object[]
                  { sh, alignFrame, this.preset, paramset }));
        } catch (Exception x)
        {
          x.printStackTrace();
          throw new Error(
                  MessageManager.getString("error.implementation_error"),
                  x);
        }
        alignFrame.getViewport().getCalcManager().registerWorker(worker);
        alignFrame.getViewport().getCalcManager().startWorker(worker);

      }
      else
      {
        worker = (AbstractJabaCalcWorker) clnts.get(0);
        if (editParams)
        {
          paramset = worker.getArguments();
          preset = worker.getPreset();
        }

        if (!processParams(sh, editParams, true))
        {
          return;
        }
        // reinstate worker if it was blacklisted (might have happened due to
        // invalid parameters)
        alignFrame.getViewport().getCalcManager().enableWorker(worker);
        worker.updateParameters(this.preset, paramset);
      }
    }
    if (sh.action.toLowerCase(Locale.ROOT).contains("disorder"))
    {
      // build IUPred style client. take sequences, returns annotation per
      // sequence.
      if (!processParams(sh, editParams))
      {
        return;
      }

      alignFrame.getViewport().getCalcManager().startWorker(
              new AADisorderClient(sh, alignFrame, preset, paramset));
    }
  }

  public SequenceAnnotationWSClient(AAConSettings fave,
          AlignFrame alignFrame, boolean b)
  {
    super(alignFrame, fave.getPreset(), fave.getJobArgset());
    initSequenceAnnotationWSClient(fave.getService(), alignFrame,
            fave.getPreset(), b);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.jws2.Jws2Client#attachWSMenuEntry(javax.swing.JMenu,
   * jalview.ws.jws2.jabaws2.Jws2Instance, jalview.gui.AlignFrame)
   */
  public void attachWSMenuEntry(JMenu wsmenu, final Jws2Instance service,
          final AlignFrame alignFrame)
  {
    if (registerAAConWSInstance(wsmenu, service, alignFrame))
    {
      // Alignment dependent analysis calculation WS gui
      return;
    }
    boolean hasparams = service.hasParameters();
    // Assume name ends in WS
    String calcName = service.serviceType.substring(0,
            service.serviceType.length() - 2);

    JMenuItem annotservice = new JMenuItem(MessageManager.formatMessage(
            "label.calcname_with_default_settings", new String[]
            { calcName }));
    annotservice.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        new SequenceAnnotationWSClient(service, alignFrame, null, false);
      }
    });
    wsmenu.add(annotservice);
    if (hasparams)
    {
      // only add these menu options if the service has user-modifiable
      // arguments
      annotservice = new JMenuItem(
              MessageManager.getString("label.edit_settings_and_run"));
      annotservice.setToolTipText(MessageManager.getString(
              "label.view_and_change_parameters_before_running_calculation"));

      annotservice.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          new SequenceAnnotationWSClient(service, alignFrame, null, true);
        }
      });
      wsmenu.add(annotservice);
      List<WsParamSetI> presets = service.getParamStore().getPresets();
      if (presets != null && presets.size() > 0)
      {
        JMenu presetlist = new JMenu(MessageManager
                .formatMessage("label.run_with_preset", new String[]
                { calcName }));

        for (final WsParamSetI preset : presets)
        {
          final JMenuItem methodR = new JMenuItem(preset.getName());
          methodR.setToolTipText(JvSwingUtils.wrapTooltip(true, "<strong>"
                  + (preset.isModifiable()
                          ? MessageManager.getString("label.user_preset")
                          : MessageManager
                                  .getString("label.service_preset"))
                  + "</strong><br/>" + preset.getDescription()));
          methodR.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent e)
            {
              new SequenceAnnotationWSClient(service, alignFrame, preset,
                      false);
            }

          });
          presetlist.add(methodR);
        }
        wsmenu.add(presetlist);
      }

    }
    else
    {
      annotservice = new JMenuItem(
              MessageManager.getString("label.view_documentation"));
      if (service.docUrl != null)
      {
        annotservice.addActionListener(new ActionListener()
        {

          @Override
          public void actionPerformed(ActionEvent arg0)
          {
            Desktop.instance.showUrl(service.docUrl);
          }
        });
        annotservice.setToolTipText(
                JvSwingUtils.wrapTooltip(true, MessageManager.formatMessage(
                        "label.view_service_doc_url", new String[]
                        { service.docUrl, service.docUrl })));
        wsmenu.add(annotservice);
      }
    }
  }
}
