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
import jalview.gui.AlignFrame;
import jalview.util.MessageManager;
import jalview.ws.jws2.jabaws2.Jws2Instance;
import jalview.ws.params.WsParamSetI;
import jalview.ws.uimodel.AlignAnalysisUIText;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import compbio.data.sequence.FastaSequence;
import compbio.data.sequence.Score;
import compbio.metadata.Argument;

public class AAConClient extends JabawsCalcWorker
{

  public AAConClient(Jws2Instance service, AlignFrame alignFrame,
          WsParamSetI preset, List<Argument> paramset)
  {
    super(service, alignFrame, preset, paramset);
    submitGaps = true;
    alignedSeqs = true;
    nucleotidesAllowed = false;
    proteinAllowed = true;
    filterNonStandardResidues = true;
    gapMap = new boolean[0];
    initViewportParams();
  }

  @Override
  public String getServiceActionText()
  {
    return "calculating Amino acid consensus using AACon service";
  }

  /**
   * update the consensus annotation from the sequence profile data using
   * current visualization settings.
   */

  @Override
  public void updateResultAnnotation(boolean immediate)
  {
    if (immediate || !calcMan.isWorking(this) && scoremanager != null)
    {
      Map<String, TreeSet<Score>> scoremap = scoremanager.asMap();
      int alWidth = alignViewport.getAlignment().getWidth();
      ArrayList<AlignmentAnnotation> ourAnnot = new ArrayList<>();
      for (String score : scoremap.keySet())
      {
        Set<Score> scores = scoremap.get(score);
        for (Score scr : scores)
        {
          if (scr.getRanges() != null && scr.getRanges().size() > 0)
          {
            /**
             * annotation in range annotation = findOrCreate(scr.getMethod(),
             * true, null, null); Annotation[] elm = new Annotation[alWidth];
             * Iterator<Float> vals = scr.getScores().iterator(); for (Range rng
             * : scr.getRanges()) { float val = vals.next().floatValue(); for
             * (int i = rng.from; i <= rng.to; i++) { elm[i] = new
             * Annotation("", "", ' ', val); } } annotation.annotations = elm;
             * annotation.validateRangeAndDisplay();
             */
          }
          else
          {
            createAnnotationRowsForScores(ourAnnot, getCalcId(), alWidth,
                    scr);
          }
        }
      }

      if (ourAnnot.size() > 0)
      {
        updateOurAnnots(ourAnnot);
      }
    }
  }

  @Override
  boolean checkValidInputSeqs(boolean dynamic, List<FastaSequence> seqs)
  {
    return (seqs.size() > 1);
  }

  @Override
  public String getCalcId()
  {
    return CALC_ID;
  }

  private static String CALC_ID = "jabaws2.AACon";

  public static AlignAnalysisUIText getAlignAnalysisUITest()
  {
    return new AlignAnalysisUIText(
            compbio.ws.client.Services.AAConWS.toString(),
            jalview.ws.jws2.AAConClient.class, CALC_ID, false, true, true,
            MessageManager.getString("label.aacon_calculations"),
            MessageManager.getString("tooltip.aacon_calculations"),
            MessageManager.getString("label.aacon_settings"),
            MessageManager.getString("tooltip.aacon_settings"));
  }
}
