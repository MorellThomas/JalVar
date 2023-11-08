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

import jalview.analysis.AlignSeq;
import jalview.bin.Console;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.SeqCigar;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.Desktop;
import jalview.gui.JvOptionPane;
import jalview.gui.WebserviceInfo;
import jalview.util.MessageManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import ext.vamsas.Jpred;
import ext.vamsas.JpredServiceLocator;
import ext.vamsas.JpredSoapBindingStub;
import ext.vamsas.ServiceHandle;

public class JPredClient extends WS1Client
{
  /**
   * crate a new GUI JPred Job
   * 
   * @param sh
   *          ServiceHandle
   * @param title
   *          String
   * @param msa
   *          boolean - true - submit alignment as a sequence profile
   * @param alview
   *          AlignmentView
   * @param viewonly
   *          TODO
   */
  public JPredClient(ext.vamsas.ServiceHandle sh, String title, boolean msa,
          AlignmentView alview, AlignFrame parentFrame, boolean viewonly)
  {
    super();
    wsInfo = setWebService(sh);
    startJPredClient(title, msa, alview, parentFrame, viewonly);

  }

  /**
   * startJPredClient TODO: refine submission to cope with local prediction of
   * visible regions or multiple single sequence jobs TODO: sequence
   * representative support - could submit alignment of representatives as msa.
   * TODO: msa hidden region prediction - submit each chunk for prediction.
   * concatenate results of each. TODO: single seq prediction - submit each
   * contig of each sequence for prediction (but must cope with flanking regions
   * and short seqs)
   * 
   * @param title
   *          String
   * @param msa
   *          boolean
   * @param alview
   *          AlignmentView
   * @param viewonly
   *          if true then the prediction will be made just on the concatenated
   *          visible regions
   */
  private void startJPredClient(String title, boolean msa,
          jalview.datamodel.AlignmentView alview, AlignFrame parentFrame,
          boolean viewonly)
  {
    AlignmentView input = alview;
    if (wsInfo == null)
    {
      wsInfo = setWebService();
    }
    Jpred server = locateWebService();
    if (server == null)
    {
      Console.warn("Couldn't find a Jpred webservice to invoke!");
      return;
    }
    SeqCigar[] msf = null;
    SequenceI seq = null;
    int[] delMap = null;
    // original JNetClient behaviour - submit full length of sequence or profile
    // and mask result.
    msf = input.getSequences();
    seq = msf[0].getSeq('-');

    if (viewonly)
    {
      delMap = alview.getVisibleContigMapFor(seq.gapMap());
    }
    if (msa && msf.length > 1)
    {

      String altitle = getPredictionName(WebServiceName) + " on "
              + (viewonly ? "visible " : "") + seq.getName()
              + " using alignment from " + title;

      SequenceI aln[] = new SequenceI[msf.length];
      for (int i = 0, j = msf.length; i < j; i++)
      {
        aln[i] = msf[i].getSeq('-');
      }

      Hashtable SequenceInfo = jalview.analysis.SeqsetUtils.uniquify(aln,
              true);
      if (viewonly)
      {
        // Remove hidden regions from sequence objects.
        String seqs[] = alview.getSequenceStrings('-');
        for (int i = 0, j = msf.length; i < j; i++)
        {
          aln[i].setSequence(seqs[i]);
        }
        seq.setSequence(seqs[0]);
      }
      wsInfo.setProgressText("Job details for "
              + (viewonly ? "visible " : "") + "MSA based prediction ("
              + title + ") on sequence :\n>" + seq.getName() + "\n"
              + AlignSeq.extractGaps("-. ", seq.getSequenceAsString())
              + "\n");
      JPredThread jthread = new JPredThread(wsInfo, altitle, server,
              SequenceInfo, aln, delMap, alview, parentFrame, WsURL);
      wsInfo.setthisService(jthread);
      jthread.start();
    }
    else
    {
      if (!msa && msf.length > 1)
      {
        throw new Error(MessageManager.getString(
                "error.implementation_error_multiple_single_sequence_prediction_jobs_not_supported"));
      }

      String altitle = getPredictionName(WebServiceName) + " for "
              + (viewonly ? "visible " : "") + "sequence " + seq.getName()
              + " from " + title;
      String seqname = seq.getName();
      Hashtable SequenceInfo = jalview.analysis.SeqsetUtils
              .SeqCharacterHash(seq);
      if (viewonly)
      {
        // Remove hidden regions from input sequence
        String seqs[] = alview.getSequenceStrings('-');
        seq.setSequence(seqs[0]);
      }
      wsInfo.setProgressText("Job details for prediction on "
              + (viewonly ? "visible " : "") + "sequence :\n>" + seqname
              + "\n"
              + AlignSeq.extractGaps("-. ", seq.getSequenceAsString())
              + "\n");
      JPredThread jthread = new JPredThread(wsInfo, altitle, server, WsURL,
              SequenceInfo, seq, delMap, alview, parentFrame);
      wsInfo.setthisService(jthread);
      jthread.start();
    }
  }

  private String getPredictionName(String webServiceName)
  {
    if (webServiceName.toLowerCase(Locale.ROOT)
            .indexOf("secondary structure prediction") > -1)
    {
      return webServiceName;
    }
    else
    {
      return webServiceName + "secondary structure prediction";
    }
  }

  public JPredClient(ext.vamsas.ServiceHandle sh, String title,
          SequenceI seq, AlignFrame parentFrame)
  {
    super();
    wsInfo = setWebService(sh);
    startJPredClient(title, seq, parentFrame);
  }

  public JPredClient(ext.vamsas.ServiceHandle sh, String title,
          SequenceI[] msa, AlignFrame parentFrame)
  {
    wsInfo = setWebService(sh);
    startJPredClient(title, msa, parentFrame);
  }

  public JPredClient(String title, SequenceI[] msf)
  {
    startJPredClient(title, msf, null);
  }

  public JPredClient(String title, SequenceI seq)
  {
    startJPredClient(title, seq, null);
  }

  public JPredClient()
  {

    super();
    // add a class reference to the list
  }

  private void startJPredClient(String title, SequenceI[] msf,
          AlignFrame parentFrame)
  {
    if (wsInfo == null)
    {
      wsInfo = setWebService();
    }

    SequenceI seq = msf[0];

    String altitle = "JPred prediction on " + seq.getName()
            + " using alignment from " + title;

    wsInfo.setProgressText("Job details for MSA based prediction (" + title
            + ") on sequence :\n>" + seq.getName() + "\n"
            + AlignSeq.extractGaps("-. ", seq.getSequenceAsString())
            + "\n");
    SequenceI aln[] = new SequenceI[msf.length];
    for (int i = 0, j = msf.length; i < j; i++)
    {
      aln[i] = new jalview.datamodel.Sequence(msf[i]);
    }

    Hashtable SequenceInfo = jalview.analysis.SeqsetUtils.uniquify(aln,
            true);

    Jpred server = locateWebService();
    if (server == null)
    {
      return;
    }

    JPredThread jthread = new JPredThread(wsInfo, altitle, server,
            SequenceInfo, aln, null, null, parentFrame, WsURL);
    wsInfo.setthisService(jthread);
    jthread.start();
  }

  public void startJPredClient(String title, SequenceI seq,
          AlignFrame parentFrame)
  {
    if (wsInfo == null)
    {
      wsInfo = setWebService();
    }
    wsInfo.setProgressText("Job details for prediction on sequence :\n>"
            + seq.getName() + "\n"
            + AlignSeq.extractGaps("-. ", seq.getSequenceAsString())
            + "\n");
    String altitle = "JPred prediction for sequence " + seq.getName()
            + " from " + title;

    Hashtable SequenceInfo = jalview.analysis.SeqsetUtils
            .SeqCharacterHash(seq);

    Jpred server = locateWebService();
    if (server == null)
    {
      return;
    }

    JPredThread jthread = new JPredThread(wsInfo, altitle, server, WsURL,
            SequenceInfo, seq, null, null, parentFrame);
    wsInfo.setthisService(jthread);
    jthread.start();
  }

  private WebserviceInfo setWebService()
  {
    WebServiceName = "JNetWS";
    WebServiceJobTitle = MessageManager
            .getString("label.jnet_secondary_structure_prediction");
    WebServiceReference = "\"Cuff J. A and Barton G.J (2000) Application of "
            + "multiple sequence alignment profiles to improve protein secondary structure prediction, "
            + "Proteins 40:502-511\".";
    WsURL = "http://www.compbio.dundee.ac.uk/JalviewWS/services/jpred";

    WebserviceInfo wsInfo = new WebserviceInfo(WebServiceJobTitle,
            WebServiceReference, true);

    return wsInfo;
  }

  private ext.vamsas.Jpred locateWebService()
  {
    ext.vamsas.JpredServiceLocator loc = new JpredServiceLocator(); // Default
    ext.vamsas.Jpred server = null;
    try
    {
      server = loc.getjpred(new java.net.URL(WsURL)); // JBPNote will be set
      // from properties
      ((JpredSoapBindingStub) server).setTimeout(60000); // one minute stub
      // ((JpredSoapBindingStub)this.server)._setProperty(org.apache.axis.encoding.C,
      // Boolean.TRUE);

    } catch (Exception ex)
    {
      JvOptionPane.showMessageDialog(Desktop.desktop,
              MessageManager.formatMessage(
                      "label.secondary_structure_prediction_service_couldnt_be_located",
                      new String[]
                      { WebServiceName, WsURL }),
              MessageManager.getString("label.internal_jalview_error"),
              JvOptionPane.WARNING_MESSAGE);
      wsInfo.setProgressText(MessageManager.formatMessage(
              "label.secondary_structure_prediction_service_couldnt_be_located",
              new String[]
              { WebServiceName, WsURL }) + "\n" + ex.getMessage());
      wsInfo.setStatus(WebserviceInfo.STATE_STOPPED_SERVERERROR);

    }

    return server;
  }

  @Override
  public void attachWSMenuEntry(JMenu wsmenu, final ServiceHandle sh,
          final AlignFrame af)
  {
    final JMenuItem method = new JMenuItem(sh.getName());
    method.setToolTipText(sh.getEndpointURL());
    method.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        AlignmentView msa = af.gatherSeqOrMsaForSecStrPrediction();
        if (msa.getSequences().length == 1)
        {
          // Single Sequence prediction
          new jalview.ws.jws1.JPredClient(sh, af.getTitle(), false, msa, af,
                  true);
        }
        else
        {
          if (msa.getSequences().length > 1)
          {
            // Sequence profile based prediction
            new jalview.ws.jws1.JPredClient(sh, af.getTitle(), true, msa,
                    af, true);
          }
        }
      }
    });
    wsmenu.add(method);
  }
}
