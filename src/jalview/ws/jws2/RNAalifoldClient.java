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
import jalview.gui.AlignFrame;
import jalview.util.MessageManager;
import jalview.ws.jws2.jabaws2.Jws2Instance;
import jalview.ws.params.WsParamSetI;
import jalview.ws.uimodel.AlignAnalysisUIText;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;

import compbio.data.sequence.FastaSequence;
import compbio.data.sequence.RNAStructReader.AlifoldResult;
import compbio.data.sequence.RNAStructScoreManager;
import compbio.data.sequence.Range;
import compbio.data.sequence.Score;
import compbio.metadata.Argument;

/**
 * Client for the JABA RNA Alifold Service
 * 
 * @author daluke - Daniel Barton
 * 
 */

public class RNAalifoldClient extends JabawsCalcWorker
{

  String methodName;

  AlignFrame af;

  // keeps track of whether the RNAalifold result includes base contact
  // probabilities
  boolean bpScores;

  public RNAalifoldClient(Jws2Instance sh, AlignFrame alignFrame,
          WsParamSetI preset, List<Argument> paramset)
  {
    super(sh, alignFrame, preset, paramset);
    af = alignFrame;
    methodName = sh.serviceType;
    alignedSeqs = true;
    submitGaps = true;
    nucleotidesAllowed = true;
    proteinAllowed = false;
    initViewportParams();
  }

  @Override
  public String getCalcId()
  {
    return CALC_ID;
  }

  private static String CALC_ID = "jalview.ws.jws2.RNAalifoldClient";

  public static AlignAnalysisUIText getAlignAnalysisUITest()
  {
    return new AlignAnalysisUIText(
            compbio.ws.client.Services.RNAalifoldWS.toString(),
            jalview.ws.jws2.RNAalifoldClient.class, CALC_ID, true, false,
            true, MessageManager.getString("label.rnalifold_calculations"),
            MessageManager.getString("tooltip.rnalifold_calculations"),
            MessageManager.getString("label.rnalifold_settings"),
            MessageManager.getString("tooltip.rnalifold_settings"));
  }

  @Override
  public String getServiceActionText()
  {
    return "Submitting RNA alignment for Secondary Structure prediction using "
            + "RNAalifold Service";
  }

  @Override
  boolean checkValidInputSeqs(boolean dynamic, List<FastaSequence> seqs)
  {
    return (seqs.size() > 1);
  }

  @Override
  public void updateResultAnnotation(boolean immediate)
  {

    if (immediate || !calcMan.isWorking(this) && scoremanager != null)
    {

      List<AlignmentAnnotation> ourAnnot = new ArrayList<AlignmentAnnotation>();

      // Unpack the ScoreManager
      List<String> structs = ((RNAStructScoreManager) scoremanager)
              .getStructs();
      List<TreeSet<Score>> data = ((RNAStructScoreManager) scoremanager)
              .getData();

      // test to see if this data object contains base pair contacts
      Score fscore = data.get(0).first();
      this.bpScores = (fscore.getMethod()
              .equals(AlifoldResult.contactProbabilities.toString()));

      // add annotation for the consensus sequence alignment
      createAnnotationRowforScoreHolder(ourAnnot, getCalcId(),
              structs.get(0), null, null);

      // Add annotations for the mfe Structure
      createAnnotationRowforScoreHolder(ourAnnot, getCalcId(),
              structs.get(1), data.get(1), null);

      // decide whether to add base pair contact probability histogram
      int count = 2;
      if (bpScores)
      {
        createAnnotationRowforScoreHolder(ourAnnot, getCalcId(),
                structs.get(2), data.get(0), data.get(2));
        count++;
      }

      // Now loop for the rest of the Annotations (if there it isn't stochastic
      // output
      // only the centroid and MEA structures remain anyway)
      for (int i = count; i < structs.size(); i++)
      {
        // The ensemble values should be displayed in the description of the
        // first (or all?) Stochastic Backtrack Structures.
        if (!data.get(i).first().getMethod()
                .equals(AlifoldResult.ensembleValues.toString()))
        {

          createAnnotationRowforScoreHolder(ourAnnot, getCalcId(),
                  structs.get(i), data.get(i), null);
        }
      }

      if (ourAnnot.size() > 0)
      {

        updateOurAnnots(ourAnnot);
        ap.adjustAnnotationHeight();
      }
    }
  }

  protected void createAnnotationRowforScoreHolder(
          List<AlignmentAnnotation> ourAnnot, String calcId, String struct,
          TreeSet<Score> data, TreeSet<Score> descriptionData)
  {
    /*
     * If contactProbability information is returned from RNAalifold it is
     * stored in the first TreeSet<Score> object corresponding to the String Id
     * which holds the consensus alignment. The method enumeration is then
     * updated to AlifoldResult.contactProbabilties. This line recreates the
     * same data object as was overwritten with the contact probabilites data.
     */
    if (data == null)
    {
      data = compbio.data.sequence.RNAStructReader
              .newEmptyScore(AlifoldResult.consensusAlignment);
    }

    if (descriptionData == null)
    {
      descriptionData = data;
    }

    String[] typenameAndDescription = constructTypenameAndDescription(
            descriptionData.first());
    String typename = typenameAndDescription[0];
    String description = typenameAndDescription[1];

    AlignmentAnnotation annotation = alignViewport.getAlignment()
            .findOrCreateAnnotation(typename, calcId, false, null, null);

    constructAnnotationFromScoreHolder(annotation, struct, data);

    /*
     * update annotation description with the free Energy, frequency in ensemble
     * or other data where appropriate.
     * 
     * Doesnt deal with AlifoldResult.ensembleValues, the free energy of
     * ensemble and frequency of mfe structure in ensemble. How to deal with
     * these?
     */
    annotation.description = description;

    annotation.belowAlignment = false;
    // annotation.showAllColLabels = true;

    alignViewport.getAlignment().validateAnnotation(annotation);
    af.setMenusForViewport();

    ourAnnot.add(annotation);
  }

  private AlignmentAnnotation constructAnnotationFromScoreHolder(
          AlignmentAnnotation annotation, String struct,
          TreeSet<Score> data)
  {
    Annotation[] anns = new Annotation[gapMap != null ? gapMap.length + 1
            : struct.length()];

    if (data != null && data.size() > 1 && data.first().getMethod()
            .equals(AlifoldResult.contactProbabilities.toString()))
    {

      // The base pair probabilities are stored in a set in scoreholder. we want
      // a map
      LinkedHashMap<Range, Float> basePairs = new LinkedHashMap<Range, Float>();
      for (Score score : data)
      {
        // The Score objects contain a set of size one containing the range and
        // an ArrayList<float> of size one containing the probabilty
        basePairs.put(score.getRanges().first(),
                Float.valueOf(score.getScores().get(0)));
      }

      for (int i = 0, ri = 0, iEnd = struct.length(); i < iEnd; i++, ri++)
      {
        if (gapMap != null)
        {
          // skip any gapped columns in the input data
          while (!gapMap[ri])
          {
            ri++;
          }
        }
        // Return all the contacts associated with position i
        LinkedHashMap<Range, Float> contacts = isContact(basePairs, i + 1);

        String description = "";
        float prob = 0f;

        if (contacts.size() == 0)
        {
          description = "No Data";
        }
        else
        {
          for (Range contact : contacts.keySet())
          {
            float t = contacts.get(contact);
            if (t > prob)
            {
              prob = t;
            }
            description += Integer.toString(contact.from) + "->"
                    + Integer.toString(contact.to) + ": "
                    + Float.toString(t) + "%  |  ";
          }
        }

        anns[ri] = new Annotation(struct.substring(i, i + 1), description,
                isSS(struct.charAt(i)), prob);
      }
    }
    else if (data == null || data.size() == 1)
    {
      for (int i = 0, ri = 0, iEnd = struct.length(); i < iEnd; i++, ri++)
      {
        if (gapMap != null)
        {
          // skip any gapped columns in the input data
          while (!gapMap[ri] && ri < gapMap.length)
          {
            ri++;
          }
          if (ri == gapMap.length)
          {
            break;
          }
        }
        anns[ri] = new Annotation(struct.substring(i, i + 1), "",
                isSS(struct.charAt(i)), Float.NaN);
      }

      annotation.graph = 0; // No graph
    }

    annotation.annotations = anns;

    return annotation;
  }

  private String[] constructTypenameAndDescription(Score score)
  {
    String description = "";
    String typename = "";
    String datatype = score.getMethod();

    // Look up java switch syntax and use one here
    if (datatype.equals(AlifoldResult.mfeStructure.toString()))
    {

      description = MessageFormat.format(
              "Minimum Free Energy Structure. Energy: {0} = {1} + {2}",
              score.getScores().get(0), score.getScores().get(1),
              score.getScores().get(2));
      typename = "MFE Structure";
    }
    else if (datatype
            .equals(AlifoldResult.contactProbabilityStructure.toString()))
    {
      description = MessageFormat.format("Base Pair Contact Probabilities. "
              + "Energy of Ensemble: {0}  Frequency of Ensemble: {1}",
              score.getScores().get(0), score.getScores().get(1));
      typename = "Contact Probabilities";
    }
    else if (datatype.equals(AlifoldResult.centroidStructure.toString()))
    {
      description = MessageFormat.format(
              "Centroid Structure. Energy: {0} = {1} + {2}",
              score.getScores().get(0), score.getScores().get(1),
              score.getScores().get(2));
      typename = "Centroid Structure";
    }
    else if (datatype.equals(AlifoldResult.stochBTStructure.toString()))
    {
      if (score.getScores().size() > 0)
      {
        description = MessageFormat.format("Probability: {0}  Energy: {1}",
                score.getScores().get(0), score.getScores().get(1));
      }
      else
      {
        description = "Stochastic Backtrack Structure";
      }
    }
    else if (datatype.equals(AlifoldResult.MEAStucture.toString()))
    {
      description = MessageFormat.format(
              "Maximum Expected Accuracy Values: '{' {0} MEA={1} '}",
              score.getScores().get(0), score.getScores().get(1));
      typename = "MEA Structure";
    }
    else if (datatype.equals(AlifoldResult.consensusAlignment.toString()))
    {
      typename = "RNAalifold Consensus";
      description = "Consensus Alignment Produced by RNAalifold";
    }
    else
    {
      typename = datatype;
      description = typename;
    }

    return new String[] { typename, description };
  }

  // Check whether, at position i there is a base contact and return all the
  // contacts at this position. Should be in order of descending probability.
  private LinkedHashMap<Range, Float> isContact(
          LinkedHashMap<Range, Float> basePairs, int i)
  {
    LinkedHashMap<Range, Float> contacts = new LinkedHashMap<Range, Float>();

    for (Range contact : basePairs.keySet())
    {
      // finds the contacts associtated with position i ordered by the natural
      // ordering of the Scores TreeSet in ScoreManager which is, descending
      // probability
      if (contact.from == i || contact.to == i)
      {
        contacts.put(contact, basePairs.get(contact));
      }
    }

    return contacts;
  }

  private char isSS(char chr)
  {
    String regex = "\\(|\\)|\\{|\\}|\\[|\\]";
    char ss = (Pattern.matches(regex, Character.toString(chr))) ? 'S' : ' ';
    return ss;
  }
}
