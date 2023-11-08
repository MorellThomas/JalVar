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

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Annotation;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.util.MessageManager;
import jalview.ws.jws2.jabaws2.Jws2Instance;
import jalview.ws.params.WsParamSetI;

import java.util.Iterator;
import java.util.List;

import compbio.data.msa.SequenceAnnotation;
import compbio.data.sequence.Score;
import compbio.data.sequence.ScoreManager;
import compbio.metadata.Argument;
import compbio.metadata.ChunkHolder;
import compbio.metadata.JobStatus;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.ResultNotAvailableException;
import compbio.metadata.WrongParameterException;

public abstract class JabawsCalcWorker extends AbstractJabaCalcWorker
{

  @SuppressWarnings("unchecked")
  protected SequenceAnnotation aaservice;

  protected ScoreManager scoremanager;

  public JabawsCalcWorker(Jws2Instance service, AlignFrame alignFrame,
          WsParamSetI preset, List<Argument> paramset)
  {
    super(service, alignFrame, preset, paramset);
    aaservice = (SequenceAnnotation) service.service;
  }

  @Override
  ChunkHolder pullExecStatistics(String rslt, long rpos)
  {
    return aaservice.pullExecStatistics(rslt, rpos);
  }

  @Override
  boolean collectAnnotationResultsFor(String rslt)
          throws ResultNotAvailableException
  {
    scoremanager = aaservice.getAnnotation(rslt);
    if (scoremanager != null)
    {
      return true;
    }
    return false;
  }

  @Override
  boolean cancelJob(String rslt) throws Exception
  {
    return aaservice.cancelJob(rslt);
  }

  @Override
  protected JobStatus getJobStatus(String rslt) throws Exception
  {
    return aaservice.getJobStatus(rslt);
  }

  @Override
  boolean hasService()
  {
    return aaservice != null;
  }

  @Override
  protected boolean isInteractiveUpdate()
  {
    return this instanceof AAConClient;
  }

  @Override
  protected String submitToService(
          List<compbio.data.sequence.FastaSequence> seqs)
          throws JobSubmissionException
  {
    String rslt;
    if (preset == null && arguments == null)
    {
      rslt = aaservice.analize(seqs);
    }
    else
    {
      try
      {
        rslt = aaservice.customAnalize(seqs, getJabaArguments());
      } catch (WrongParameterException x)
      {
        throw new JobSubmissionException(MessageManager.getString(
                "exception.jobsubmission_invalid_params_set"), x);

      }
    }
    return rslt;
  }

  protected void createAnnotationRowsForScores(
          List<AlignmentAnnotation> ourAnnot, String calcId, int alWidth,
          Score scr)
  {
    // simple annotation row
    AlignmentAnnotation annotation = alignViewport.getAlignment()
            .findOrCreateAnnotation(scr.getMethod(), calcId, true, null,
                    null);
    if (alWidth == gapMap.length) // scr.getScores().size())
    {
      constructAnnotationFromScore(annotation, 0, alWidth, scr);
      ourAnnot.add(annotation);
    }
  }

  protected AlignmentAnnotation createAnnotationRowsForScores(
          List<AlignmentAnnotation> ourAnnot, String typeName,
          String calcId, SequenceI dseq, int base, Score scr)
  {
    System.out.println("Creating annotation on dseq:" + dseq.getStart()
            + " base is " + base + " and length=" + dseq.getLength()
            + " == " + scr.getScores().size());
    // AlignmentAnnotation annotation = new AlignmentAnnotation(
    // scr.getMethod(), typeName, new Annotation[]
    // {}, 0, -1, AlignmentAnnotation.LINE_GRAPH);
    // annotation.setCalcId(calcId);
    AlignmentAnnotation annotation = alignViewport.getAlignment()
            .findOrCreateAnnotation(typeName, calcId, false, dseq, null);
    constructAnnotationFromScore(annotation, 0, dseq.getLength(), scr);
    annotation.createSequenceMapping(dseq, base, false);
    annotation.adjustForAlignment();
    dseq.addAlignmentAnnotation(annotation);
    ourAnnot.add(annotation);
    return annotation;
  }

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

  private void constructAnnotationFromScore(AlignmentAnnotation annotation,
          int base, int alWidth, Score scr)
  {
    Annotation[] elm = new Annotation[alWidth];
    Iterator<Float> vals = scr.getScores().iterator();
    float m = 0f, x = 0f;
    for (int i = 0; vals.hasNext(); i++)
    {
      float val = vals.next().floatValue();
      if (i == 0)
      {
        m = val;
        x = val;
      }
      else
      {
        if (m > val)
        {
          m = val;
        }
        ;
        if (x < val)
        {
          x = val;
        }
      }
      // if we're at a gapped column then skip to next ungapped position
      if (gapMap != null && gapMap.length > 0)
      {
        while (!gapMap[i])
        {
          elm[i++] = new Annotation("", "", ' ', Float.NaN);
        }
      }
      elm[i] = new Annotation("", "" + val, ' ', val);
    }

    annotation.annotations = elm;
    annotation.belowAlignment = true;
    if (x < 0)
    {
      x = 0;
    }
    x += (x - m) * 0.1;
    annotation.graphMax = x;
    annotation.graphMin = m;
    annotation.validateRangeAndDisplay();
  }

}
