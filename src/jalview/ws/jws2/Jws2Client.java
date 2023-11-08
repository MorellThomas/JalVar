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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import compbio.metadata.Argument;
import jalview.api.AlignCalcWorkerI;
import jalview.bin.Console;
import jalview.gui.AlignFrame;
import jalview.gui.Desktop;
import jalview.gui.JvSwingUtils;
import jalview.gui.WebserviceInfo;
import jalview.gui.WsJobParameters;
import jalview.util.MessageManager;
import jalview.ws.jws2.dm.AAConSettings;
import jalview.ws.jws2.dm.JabaWsParamSet;
import jalview.ws.jws2.jabaws2.Jws2Instance;
import jalview.ws.params.WsParamSetI;
import jalview.ws.uimodel.AlignAnalysisUIText;

/**
 * provides metadata for a jabaws2 service instance - resolves names, etc.
 * 
 * @author JimP
 * 
 */
public abstract class Jws2Client extends jalview.ws.WSClient
{
  protected AlignFrame alignFrame;

  protected WsParamSetI preset;

  protected List<Argument> paramset;

  public Jws2Client(AlignFrame _alignFrame, WsParamSetI preset,
          List<Argument> arguments)
  {
    alignFrame = _alignFrame;
    this.preset = preset;
    if (preset != null)
    {
      if (!((preset instanceof JabaPreset)
              || preset instanceof JabaWsParamSet))
      {
        /*
         * { this.preset = ((JabaPreset) preset).p; } else if (preset instanceof
         * JabaWsParamSet) { List<Argument> newargs = new ArrayList<Argument>();
         * JabaWsParamSet pset = ((JabaWsParamSet) preset); for (Option opt :
         * pset.getjabaArguments()) { newargs.add(opt); } if (arguments != null
         * && arguments.size() > 0) { // merge arguments with preset's own
         * arguments. for (Argument opt : arguments) { newargs.add(opt); } }
         * paramset = newargs; } else {
         */
        throw new Error(MessageManager.getString(
                "error.implementation_error_can_only_instantiate_jaba_param_sets"));
      }
    }
    else
    {
      // just provided with a bunch of arguments
      this.paramset = arguments;
    }
  }

  boolean processParams(Jws2Instance sh, boolean editParams)
  {
    return processParams(sh, editParams, false);
  }

  protected boolean processParams(Jws2Instance sh, boolean editParams,
          boolean adjustingExisting)
  {

    if (editParams)
    {
      if (sh.paramStore == null)
      {
        sh.paramStore = new JabaParamStore(sh,
                Desktop.getUserParameterStore());
      }
      WsJobParameters jobParams = (preset == null && paramset != null
              && paramset.size() > 0)
                      ? new WsJobParameters(null, sh, null, paramset)
                      : new WsJobParameters(sh, preset);
      if (adjustingExisting)
      {
        jobParams.setName(MessageManager
                .getString("label.adjusting_parameters_for_calculation"));
      }
      if (!jobParams.showRunDialog())
      {
        return false;
      }
      WsParamSetI prset = jobParams.getPreset();
      if (prset == null)
      {
        paramset =
                /* JAL-3739 always take values from input form */
                /* jobParams.isServiceDefaults() ? null : */
                JabaParamStore.getJabafromJwsArgs(jobParams.getJobParams());
        this.preset = null;
      }
      else
      {
        this.preset = prset; // ((JabaPreset) prset).p;
        paramset = null; // no user supplied parameters.
      }
    }
    return true;

  }

  public Jws2Client()
  {
    // anonymous constructor - used for headless method calls only
  }

  protected WebserviceInfo setWebService(Jws2Instance serv, boolean b)
  {
    // serviceHandle = serv;
    String serviceInstance = serv.action; // serv.service.getClass().getName();
    WebServiceName = serv.serviceType;
    WebServiceJobTitle = serv.getActionText();
    WsURL = serv.hosturl;
    if (!b)
    {
      return new WebserviceInfo(WebServiceJobTitle,
              WebServiceJobTitle + " using service hosted at "
                      + serv.hosturl + "\n"
                      + (serv.description != null ? serv.description : ""),
              false);
    }
    return null;
  }

  /*
   * Jws2Instance serviceHandle; (non-Javadoc)
   * 
   * @see jalview.ws.WSMenuEntryProviderI#attachWSMenuEntry(javax.swing.JMenu,
   * jalview.gui.AlignFrame)
   * 
   * @Override public void attachWSMenuEntry(JMenu wsmenu, AlignFrame
   * alignFrame) { if (serviceHandle==null) { throw new
   * Error("Implementation error: No service handle for this Jws2 service."); }
   * attachWSMenuEntry(wsmenu, serviceHandle, alignFrame); }
   */
  /**
   * add the menu item for a particular jws2 service instance
   * 
   * @param wsmenu
   * @param service
   * @param alignFrame
   */
  abstract void attachWSMenuEntry(JMenu wsmenu, final Jws2Instance service,
          final AlignFrame alignFrame);

  protected boolean registerAAConWSInstance(final JMenu wsmenu,
          final Jws2Instance service, final AlignFrame alignFrame)
  {
    final AlignAnalysisUIText aaui = service.getAlignAnalysisUI(); // null ; //
                                                                   // AlignAnalysisUIText.aaConGUI.get(service.serviceType.toString());
    if (aaui == null)
    {
      // not an instantaneous calculation GUI type service
      return false;
    }
    // create the instaneous calculation GUI bits and update state if existing
    // GUI elements already present

    JCheckBoxMenuItem _aaConEnabled = null;
    for (int i = 0; i < wsmenu.getItemCount(); i++)
    {
      JMenuItem item = wsmenu.getItem(i);
      if (item instanceof JCheckBoxMenuItem
              && item.getText().equals(aaui.getAAconToggle()))
      {
        _aaConEnabled = (JCheckBoxMenuItem) item;
      }
    }
    // is there an aaCon worker already present - if so, set it to use the
    // given service handle
    {
      List<AlignCalcWorkerI> aaconClient = alignFrame.getViewport()
              .getCalcManager()
              .getRegisteredWorkersOfClass(aaui.getClient());
      if (aaconClient != null && aaconClient.size() > 0)
      {
        AbstractJabaCalcWorker worker = (AbstractJabaCalcWorker) aaconClient
                .get(0);
        if (!worker.service.hosturl.equals(service.hosturl))
        {
          // javax.swing.SwingUtilities.invokeLater(new Runnable()
          {
            // @Override
            // public void run()
            {
              removeCurrentAAConWorkerFor(aaui, alignFrame);
              buildCurrentAAConWorkerFor(aaui, alignFrame, service);
            }
          } // );
        }
      }
    }

    // is there a service already registered ? there shouldn't be if we are
    // being called correctly
    if (_aaConEnabled == null)
    {
      final JCheckBoxMenuItem aaConEnabled = new JCheckBoxMenuItem(
              aaui.getAAconToggle());

      aaConEnabled.setToolTipText(
              JvSwingUtils.wrapTooltip(true, aaui.getAAconToggleTooltip()));
      aaConEnabled.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
          List<AlignCalcWorkerI> aaconClient = alignFrame.getViewport()
                  .getCalcManager()
                  .getRegisteredWorkersOfClass(aaui.getClient());
          if (aaconClient != null && aaconClient.size() > 0)
          {
            removeCurrentAAConWorkerFor(aaui, alignFrame);
          }
          else
          {
            buildCurrentAAConWorkerFor(aaui, alignFrame);

          }
        }

      });
      wsmenu.add(aaConEnabled);
      final JMenuItem modifyParams = new JMenuItem(
              aaui.getAAeditSettings());
      modifyParams.setToolTipText(JvSwingUtils.wrapTooltip(true,
              aaui.getAAeditSettingsTooltip()));
      modifyParams.addActionListener(new ActionListener()
      {

        @Override
        public void actionPerformed(ActionEvent arg0)
        {
          showAAConAnnotationSettingsFor(aaui, alignFrame);
        }
      });
      wsmenu.add(modifyParams);
      wsmenu.addMenuListener(new MenuListener()
      {

        @Override
        public void menuSelected(MenuEvent arg0)
        {
          // TODO: refactor to the implementing class.
          if (alignFrame.getViewport().getAlignment().isNucleotide()
                  ? aaui.isNa()
                  : aaui.isPr())
          {
            aaConEnabled.setEnabled(true);
            modifyParams.setEnabled(true);
          }
          else
          {
            aaConEnabled.setEnabled(false);
            modifyParams.setEnabled(false);
          }
          List<AlignCalcWorkerI> aaconClient = alignFrame.getViewport()
                  .getCalcManager()
                  .getRegisteredWorkersOfClass(aaui.getClient());
          if (aaconClient != null && aaconClient.size() > 0)
          {
            aaConEnabled.setSelected(true);
          }
          else
          {
            aaConEnabled.setSelected(false);
          }
        }

        @Override
        public void menuDeselected(MenuEvent arg0)
        {
          // TODO Auto-generated method stub

        }

        @Override
        public void menuCanceled(MenuEvent arg0)
        {
          // TODO Auto-generated method stub

        }
      });

    }
    return true;
  }

  private static void showAAConAnnotationSettingsFor(
          final AlignAnalysisUIText aaui, AlignFrame alignFrame)
  {
    /*
     * preferred settings Whether AACon is automatically recalculated Which
     * AACon server to use What parameters to use
     */
    // could actually do a class search for this too
    AAConSettings fave = (AAConSettings) alignFrame.getViewport()
            .getCalcIdSettingsFor(aaui.getCalcId());
    if (fave == null)
    {
      fave = createDefaultAAConSettings(aaui);
    }
    new SequenceAnnotationWSClient(fave, alignFrame, true);

  }

  private static void buildCurrentAAConWorkerFor(
          final AlignAnalysisUIText aaui, AlignFrame alignFrame)
  {
    buildCurrentAAConWorkerFor(aaui, alignFrame, null);
  }

  private static void buildCurrentAAConWorkerFor(
          final AlignAnalysisUIText aaui, AlignFrame alignFrame,
          Jws2Instance service)
  {
    /*
     * preferred settings Whether AACon is automatically recalculated Which
     * AACon server to use What parameters to use
     */
    AAConSettings fave = (AAConSettings) alignFrame.getViewport()
            .getCalcIdSettingsFor(aaui.getCalcId());
    if (fave == null)
    {
      fave = createDefaultAAConSettings(aaui, service);
    }
    else
    {
      if (service != null
              && !fave.getService().hosturl.equals(service.hosturl))
      {
        Console.debug("Changing AACon service to " + service.hosturl
                + " from " + fave.getService().hosturl);
        fave.setService(service);
      }
    }
    new SequenceAnnotationWSClient(fave, alignFrame, false);
  }

  private static AAConSettings createDefaultAAConSettings(
          AlignAnalysisUIText aaui)
  {
    return createDefaultAAConSettings(aaui, null);
  }

  private static AAConSettings createDefaultAAConSettings(
          AlignAnalysisUIText aaui, Jws2Instance service)
  {
    if (service != null)
    {
      if (!service.serviceType.toString()
              .equals(compbio.ws.client.Services.AAConWS.toString()))
      {
        Console.warn(
                "Ignoring invalid preferred service for AACon calculations (service type was "
                        + service.serviceType + ")");
        service = null;
      }
      else
      {
        // check service is actually in the list of currently avaialable
        // services
        if (!Jws2Discoverer.getDiscoverer().getServices().contains(service))
        {
          // it isn't ..
          service = null;
        }
      }
    }
    if (service == null)
    {
      // get the default service for AACon
      service = Jws2Discoverer.getDiscoverer().getPreferredServiceFor(null,
              aaui.getServiceType());
    }
    if (service == null)
    {
      // TODO raise dialog box explaining error, and/or open the JABA
      // preferences menu.
      throw new Error(
              MessageManager.getString("error.no_aacon_service_found"));
    }
    return new AAConSettings(true, service, null, null);
  }

  private static void removeCurrentAAConWorkerFor(AlignAnalysisUIText aaui,
          AlignFrame alignFrame)
  {
    alignFrame.getViewport().getCalcManager()
            .removeRegisteredWorkersOfClass(aaui.getClient());
  }

}
