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

import jalview.analysis.AlignSeq;
import jalview.analysis.SeqsetUtils;
import jalview.bin.Console;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.Desktop;
import jalview.gui.WebserviceInfo;
import jalview.io.DataSourceType;
import jalview.io.FileFormatI;
import jalview.io.FormatAdapter;
import jalview.io.IdentifyFile;
import jalview.io.JPredFile;
import jalview.io.JnetAnnotationMaker;
import jalview.io.PileUpfile;
import jalview.util.Comparison;
import jalview.util.MessageManager;
import jalview.ws.AWsJob;
import jalview.ws.JobStateSummary;
import jalview.ws.WSClientI;

import java.util.Hashtable;
import java.util.List;

import vamsas.objects.simple.JpredResult;

class JPredThread extends JWS1Thread implements WSClientI
{
  // TODO: put mapping between JPredJob input and input data here -
  // JNetAnnotation adding is done after result parsing.
  class JPredJob extends WSJob
  {
    // TODO: make JPredJob deal only with what was sent to and received from a
    // JNet service
    int[] predMap = null; // mapping from sequence(i) to the original

    // sequence(predMap[i]) being predicted on

    vamsas.objects.simple.Sequence sequence;

    vamsas.objects.simple.Msfalignment msa;

    java.util.Hashtable SequenceInfo = null;

    int msaIndex = 0; // the position of the original sequence in the array of

    // Sequences in the input object that this job holds a
    // prediction for

    /**
     * 
     * @return true if getResultSet will return a valid alignment and prediction
     *         result.
     */
    @Override
    public boolean hasResults()
    {
      if (subjobComplete && result != null && result.isFinished()
              && ((JpredResult) result).getPredfile() != null
              && ((JpredResult) result).getAligfile() != null)
      {
        return true;
      }
      return false;
    }

    @Override
    public boolean hasValidInput()
    {
      if (sequence != null)
      {
        return true;
      }
      return false;
    }

    /**
     * 
     * @return null or Object[] { annotated alignment for this prediction,
     *         ColumnSelection for this prediction} or null if no results
     *         available.
     * @throws Exception
     */
    public Object[] getResultSet() throws Exception
    {
      if (result == null || !result.isFinished())
      {
        return null;
      }
      AlignmentI al = null;
      HiddenColumns alhidden = null;
      int FirstSeq = -1; // the position of the query sequence in Alignment al

      JpredResult result = (JpredResult) this.result;

      Console.debug("Parsing output from JNet job.");
      // JPredFile prediction = new JPredFile("C:/JalviewX/files/jpred.txt",
      // "File");
      JPredFile prediction = new JPredFile(result.getPredfile(),
              DataSourceType.PASTE);
      SequenceI[] preds = prediction.getSeqsAsArray();
      Console.debug("Got prediction profile.");

      if ((this.msa != null) && (result.getAligfile() != null))
      {
        Console.debug("Getting associated alignment.");
        // we ignore the returned alignment if we only predicted on a single
        // sequence
        FileFormatI format = new IdentifyFile()
                .identify(result.getAligfile(), DataSourceType.PASTE);

        if (format != null)
        {
          SequenceI sqs[];
          if (predMap != null)
          {
            Object[] alandcolsel = input
                    .getAlignmentAndHiddenColumns(getGapChar());
            sqs = (SequenceI[]) alandcolsel[0];
            al = new Alignment(sqs);
            alhidden = (HiddenColumns) alandcolsel[1];
          }
          else
          {
            al = new FormatAdapter().readFile(result.getAligfile(),
                    DataSourceType.PASTE, format);
            sqs = new SequenceI[al.getHeight()];

            for (int i = 0, j = al.getHeight(); i < j; i++)
            {
              sqs[i] = al.getSequenceAt(i);
            }
            if (!SeqsetUtils.deuniquify(SequenceInfo, sqs))
            {
              throw (new Exception(MessageManager.getString(
                      "exception.couldnt_recover_sequence_properties_for_alignment")));
            }
          }
          FirstSeq = 0;
          if (currentView.getDataset() != null)
          {
            al.setDataset(currentView.getDataset());

          }
          else
          {
            al.setDataset(null);
          }
          JnetAnnotationMaker.add_annotation(prediction, al, FirstSeq,
                  false, predMap);

        }
        else
        {
          throw (new Exception(MessageManager.formatMessage(
                  "exception.unknown_format_for_file", new String[]
                  { "", result.getAligfile() })));
        }
      }
      else
      {
        al = new Alignment(preds);
        FirstSeq = prediction.getQuerySeqPosition();
        if (predMap != null)
        {
          char gc = getGapChar();
          SequenceI[] sqs = (SequenceI[]) input
                  .getAlignmentAndHiddenColumns(gc)[0];
          if (this.msaIndex >= sqs.length)
          {
            throw new Error(MessageManager.getString(
                    "error.implementation_error_invalid_msa_index_for_job"));
          }

          // ///
          // Uses RemoveGapsCommand
          // ///
          new jalview.commands.RemoveGapsCommand(
                  MessageManager.getString("label.remove_gaps"),
                  new SequenceI[]
                  { sqs[msaIndex] }, currentView);

          SequenceI profileseq = al.getSequenceAt(FirstSeq);
          profileseq.setSequence(sqs[msaIndex].getSequenceAsString());
        }

        if (!jalview.analysis.SeqsetUtils.SeqCharacterUnhash(
                al.getSequenceAt(FirstSeq), SequenceInfo))
        {
          throw (new Exception(MessageManager.getString(
                  "exception.couldnt_recover_sequence_props_for_jnet_query")));
        }
        else
        {
          if (currentView.getDataset() != null)
          {
            al.setDataset(currentView.getDataset());

          }
          else
          {
            al.setDataset(null);
          }
          jalview.io.JnetAnnotationMaker.add_annotation(prediction, al,
                  FirstSeq, true, predMap);
          SequenceI profileseq = al.getSequenceAt(0); // this includes any gaps.
          alignToProfileSeq(al, profileseq);
          if (predMap != null)
          {
            // Adjust input view for gaps
            // propagate insertions into profile
            alhidden = al.propagateInsertions(profileseq, input);
          }
        }
      }
      // transfer to dataset
      for (AlignmentAnnotation alant : al.getAlignmentAnnotation())
      {
        if (alant.sequenceRef != null)
        {
          replaceAnnotationOnAlignmentWith(alant, alant.label,
                  "jalview.jws1.Jpred" + (this.msa == null ? "" : "MSA"),
                  alant.sequenceRef);
        }
      }
      return new Object[] { al, alhidden }; // , FirstSeq, noMsa};
    }

    /**
     * copied from JabawsCalcWorker
     * 
     * @param newAnnot
     * @param typeName
     * @param calcId
     * @param aSeq
     */
    protected void replaceAnnotationOnAlignmentWith(
            AlignmentAnnotation newAnnot, String typeName, String calcId,
            SequenceI aSeq)
    {
      SequenceI dsseq = aSeq.getDatasetSequence();
      while (dsseq.getDatasetSequence() != null)
      {
        dsseq = dsseq.getDatasetSequence();
      }
      // look for same annotation on dataset and lift this one over
      List<AlignmentAnnotation> dsan = dsseq.getAlignmentAnnotations(calcId,
              typeName);
      if (dsan != null && dsan.size() > 0)
      {
        for (AlignmentAnnotation dssan : dsan)
        {
          dsseq.removeAlignmentAnnotation(dssan);
        }
      }
      AlignmentAnnotation dssan = new AlignmentAnnotation(newAnnot);
      dsseq.addAlignmentAnnotation(dssan);
      dssan.adjustForAlignment();
    }

    /**
     * Given an alignment where all other sequences except profileseq are
     * aligned to the ungapped profileseq, insert gaps in the other sequences to
     * realign them with the residues in profileseq
     * 
     * @param al
     * @param profileseq
     */
    private void alignToProfileSeq(AlignmentI al, SequenceI profileseq)
    {
      char gc = al.getGapCharacter();
      int[] gapMap = profileseq.gapMap();
      // insert gaps into profile
      for (int lp = 0, r = 0; r < gapMap.length; r++)
      {
        if (gapMap[r] - lp > 1)
        {
          StringBuffer sb = new StringBuffer();
          for (int s = 0, ns = gapMap[r] - lp; s < ns; s++)
          {
            sb.append(gc);
          }
          for (int s = 1, ns = al.getHeight(); s < ns; s++)
          {
            String sq = al.getSequenceAt(s).getSequenceAsString();
            int diff = gapMap[r] - sq.length();
            if (diff > 0)
            {
              // pad gaps
              sq = sq + sb;
              while ((diff = gapMap[r] - sq.length()) > 0)
              {
                sq = sq + ((diff >= sb.length()) ? sb.toString()
                        : sb.substring(0, diff));
              }
              al.getSequenceAt(s).setSequence(sq);
            }
            else
            {
              al.getSequenceAt(s).setSequence(sq.substring(0, gapMap[r])
                      + sb.toString() + sq.substring(gapMap[r]));
            }
          }
        }
        lp = gapMap[r];
      }
    }

    public JPredJob(Hashtable SequenceInfo, SequenceI seq, int[] delMap)
    {
      super();
      this.predMap = delMap;
      String sq = AlignSeq.extractGaps(Comparison.GapChars,
              seq.getSequenceAsString());
      if (sq.length() >= 20)
      {
        this.SequenceInfo = SequenceInfo;
        sequence = new vamsas.objects.simple.Sequence();
        sequence.setId(seq.getName());
        sequence.setSeq(sq);
      }
      else
      {
        errorMessage = "Sequence is too short to predict with JPred - need at least 20 amino acids.";
      }
    }

    public JPredJob(Hashtable SequenceInfo, SequenceI[] msf, int[] delMap)
    {
      this(SequenceInfo, msf[0], delMap);
      if (sequence != null)
      {
        if (msf.length > 1)
        {
          msa = new vamsas.objects.simple.Msfalignment();
          PileUpfile pileup = new PileUpfile();
          msa.setMsf(pileup.print(msf, true));
        }
      }
    }

    String errorMessage = "";

    public String getValidationMessages()
    {
      return errorMessage + "\n";
    }
  }

  ext.vamsas.Jpred server;

  String altitle = "";

  JPredThread(WebserviceInfo wsinfo, String altitle,
          ext.vamsas.Jpred server, String wsurl, AlignmentView alview,
          AlignFrame alframe)
  {
    super(alframe, wsinfo, alview, wsurl);
    this.altitle = altitle;
    this.server = server;
  }

  JPredThread(WebserviceInfo wsinfo, String altitle,
          ext.vamsas.Jpred server, String wsurl, Hashtable SequenceInfo,
          SequenceI seq, int[] delMap, AlignmentView alview,
          AlignFrame alframe)
  {
    this(wsinfo, altitle, server, wsurl, alview, alframe);
    JPredJob job = new JPredJob(SequenceInfo, seq, delMap);
    if (job.hasValidInput())
    {
      OutputHeader = wsInfo.getProgressText();
      jobs = new WSJob[] { job };
      job.setJobnum(0);
    }
    else
    {
      wsInfo.appendProgressText(job.getValidationMessages());
    }
  }

  JPredThread(WebserviceInfo wsinfo, String altitle,
          ext.vamsas.Jpred server, Hashtable SequenceInfo, SequenceI[] msf,
          int[] delMap, AlignmentView alview, AlignFrame alframe,
          String wsurl)
  {
    this(wsinfo, altitle, server, wsurl, alview, alframe);
    JPredJob job = new JPredJob(SequenceInfo, msf, delMap);
    if (job.hasValidInput())
    {
      jobs = new WSJob[] { job };
      OutputHeader = wsInfo.getProgressText();
      job.setJobnum(0);
    }
    else
    {
      wsInfo.appendProgressText(job.getValidationMessages());
    }
  }

  @Override
  public void StartJob(AWsJob j)
  {
    if (!(j instanceof JPredJob))
    {
      throw new Error(MessageManager.formatMessage(
              "error.implementation_error_startjob_called", new String[]
              { j.getClass().toString() }));
    }
    try
    {
      JPredJob job = (JPredJob) j;
      if (job.msa != null)
      {
        job.setJobId(server.predictOnMsa(job.msa));
      }
      else if (job.sequence != null)
      {
        job.setJobId(server.predict(job.sequence)); // debug like : job.jobId =
        // "/jobs/www-jpred/jp_Yatat29";//
      }

      if (job.getJobId() != null)
      {
        if (job.getJobId().startsWith("Broken"))
        {
          job.result = new JpredResult();
          job.result.setInvalid(true);
          job.result.setStatus(MessageManager
                  .formatMessage("label.submission_params", new String[]
                  { job.getJobId().toString() }));
          throw new Exception(job.getJobId());
        }
        else
        {
          job.setSubmitted(true);
          job.setSubjobComplete(false);
          Console.info(WsUrl + " Job Id '" + job.getJobId() + "'");
        }
      }
      else
      {
        throw new Exception(MessageManager
                .getString("exception.server_timeout_try_later"));
      }
    } catch (Exception e)
    {
      // kill the whole job.
      wsInfo.setStatus(WebserviceInfo.STATE_STOPPED_SERVERERROR);
      if (e.getMessage().indexOf("Exception") > -1)
      {
        wsInfo.setStatus(j.getJobnum(),
                WebserviceInfo.STATE_STOPPED_SERVERERROR);
        wsInfo.setProgressText(j.getJobnum(),
                "Failed to submit the prediction. (Just close the window)\n"
                        + "It is most likely that there is a problem with the server.\n");
        System.err.println(
                "JPredWS Client: Failed to submit the prediction. Quite possibly because of a server error - see below)\n"
                        + e.getMessage() + "\n");

        Console.warn("Server Exception", e);
      }
      else
      {
        wsInfo.setStatus(j.getJobnum(), WebserviceInfo.STATE_STOPPED_ERROR);
        // JBPNote - this could be a popup informing the user of the problem.
        wsInfo.appendProgressText(j.getJobnum(),
                MessageManager.formatMessage(
                        "info.failed_to_submit_prediction", new String[]
                        { e.getMessage(), wsInfo.getProgressText() }));

        Console.debug("Failed Submission of job " + j.getJobnum(), e);

      }
      j.setAllowedServerExceptions(-1);
      j.setSubjobComplete(true);
    }
  }

  @Override
  public void parseResult()
  {
    int results = 0; // number of result sets received
    JobStateSummary finalState = new JobStateSummary();
    try
    {
      for (int j = 0; j < jobs.length; j++)
      {
        finalState.updateJobPanelState(wsInfo, OutputHeader, jobs[j]);
        if (jobs[j].isSubmitted() && jobs[j].isSubjobComplete()
                && jobs[j].hasResults())
        {
          results++;
        }
      }
    } catch (Exception ex)
    {

      Console.error(
              "Unexpected exception when processing results for " + altitle,
              ex);
      wsInfo.setStatus(WebserviceInfo.STATE_STOPPED_ERROR);
    }
    if (results > 0)
    {
      wsInfo.showResultsNewFrame
              .addActionListener(new java.awt.event.ActionListener()
              {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt)
                {
                  displayResults(true);
                }
              });
      wsInfo.mergeResults
              .addActionListener(new java.awt.event.ActionListener()
              {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt)
                {
                  displayResults(false);
                }
              });
      wsInfo.setResultsReady();
    }
    else
    {
      wsInfo.setStatus(wsInfo.STATE_STOPPED_ERROR);
      wsInfo.appendInfoText("No jobs ran.");
      wsInfo.setFinishedNoResults();
    }
  }

  void displayResults(boolean newWindow)
  {
    // TODO: cope with multiple subjobs.
    if (jobs != null)
    {
      Object[] res = null;
      boolean msa = false;
      for (int jn = 0; jn < jobs.length; jn++)
      {
        Object[] jobres = null;
        JPredJob j = (JPredJob) jobs[jn];

        if (j.hasResults())
        {
          // hack - we only deal with all single seuqence predictions or all
          // profile predictions
          msa = (j.msa != null) ? true : msa;
          try
          {
            Console.debug("Parsing output of job " + jn);
            jobres = j.getResultSet();
            Console.debug("Finished parsing output.");
            if (jobs.length == 1)
            {
              res = jobres;
            }
            else
            {
              // do merge with other job results
              throw new Error(MessageManager.getString(
                      "error.multiple_jnet_subjob_merge_not_implemented"));
            }
          } catch (Exception e)
          {
            Console.error("JNet Client: JPred Annotation Parse Error", e);
            wsInfo.setStatus(j.getJobnum(),
                    WebserviceInfo.STATE_STOPPED_ERROR);
            wsInfo.appendProgressText(j.getJobnum(),
                    MessageManager.formatMessage(
                            "info.invalid_jnet_job_result_data",
                            new String[]
                            { OutputHeader.toString(), j.result.getStatus(),
                                e.getMessage() }));
            j.result.setBroken(true);
          }
        }
      }

      if (res != null)
      {
        if (newWindow)
        {
          AlignFrame af;
          ((AlignmentI) res[0])
                  .setSeqrep(((AlignmentI) res[0]).getSequenceAt(0));
          if (input == null)
          {
            if (res[1] != null)
            {
              af = new AlignFrame((Alignment) res[0],
                      (HiddenColumns) res[1], AlignFrame.DEFAULT_WIDTH,
                      AlignFrame.DEFAULT_HEIGHT);
            }
            else
            {
              af = new AlignFrame((Alignment) res[0],
                      AlignFrame.DEFAULT_WIDTH, AlignFrame.DEFAULT_HEIGHT);
            }
          }
          else
          {
            /*
             * java.lang.Object[] alandcolsel =
             * input.getAlignmentAndColumnSelection
             * (alignFrame.getViewport().getGapCharacter()); if
             * (((SequenceI[])alandcolsel[0])[0].getLength()!=res.getWidth()) {
             * if (msa) { throw new Error("Implementation Error! ColumnSelection
             * from input alignment will not map to result alignment!"); } } if
             * (!msa) { // update hidden regions to account for loss of gaps in
             * profile. - if any // gapMap returns insert list, interpreted as
             * delete list by pruneDeletions //((ColumnSelection)
             * alandcolsel[1]).pruneDeletions(ShiftList.parseMap(((SequenceI[])
             * alandcolsel[0])[0].gapMap())); }
             */

            af = new AlignFrame((Alignment) res[0], (HiddenColumns) res[1],
                    AlignFrame.DEFAULT_WIDTH, AlignFrame.DEFAULT_HEIGHT);
          }
          Desktop.addInternalFrame(af, altitle, AlignFrame.DEFAULT_WIDTH,
                  AlignFrame.DEFAULT_HEIGHT);
        }
        else
        {
          Console.info("Append results onto existing alignment.");
        }
      }
    }
  }

  @Override
  public void pollJob(AWsJob job) throws Exception
  {
    ((JPredJob) job).result = server.getresult(job.getJobId());
  }

  @Override
  public boolean isCancellable()
  {
    return false;
  }

  @Override
  public void cancelJob()
  {
    throw new Error(MessageManager.getString("error.implementation_error"));
  }

  @Override
  public boolean canMergeResults()
  {
    return false;
  }

}
