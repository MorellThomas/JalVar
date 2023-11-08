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
package jalview.io;

import java.util.Locale;

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.SequenceI;
import jalview.util.MessageManager;

public class JnetAnnotationMaker
{
  public static void add_annotation(JPredFile prediction, AlignmentI al,
          int firstSeq, boolean noMsa) throws Exception
  {
    JnetAnnotationMaker.add_annotation(prediction, al, firstSeq, noMsa,
            (int[]) null);
  }

  /**
   * adds the annotation parsed by prediction to al.
   * 
   * @param prediction
   *          JPredFile
   * @param al
   *          AlignmentI
   * @param firstSeq
   *          int the index of the sequence to attach the annotation to (usually
   *          zero)
   * @param noMsa
   *          boolean
   * @param delMap
   *          mapping from columns in JPredFile prediction to residue number in
   *          al.getSequence(firstSeq)
   */
  public static void add_annotation(JPredFile prediction, AlignmentI al,
          int firstSeq, boolean noMsa, int[] delMap) throws Exception
  {
    int i = 0;
    SequenceI[] preds = prediction.getSeqsAsArray();
    // in the future we could search for the query
    // sequence in the alignment before calling this function.
    SequenceI seqRef = al.getSequenceAt(firstSeq);
    int width = preds[0].getLength();
    int[] gapmap = al.getSequenceAt(firstSeq).gapMap();
    if ((delMap != null && delMap.length > width)
            || (delMap == null && gapmap.length != width))
    {
      throw (new Exception(MessageManager.formatMessage(
              "exception.number_of_residues_in_query_sequence_differ_from_prediction",
              new String[]
              { (delMap == null ? ""
                      : MessageManager.getString("label.mapped")),
                  al.getSequenceAt(firstSeq).getName(),
                  al.getSequenceAt(firstSeq).getSequenceAsString(),
                  Integer.valueOf(width).toString() })));
    }

    AlignmentAnnotation annot;
    Annotation[] annotations = null;

    int existingAnnotations = 0;
    if (al.getAlignmentAnnotation() != null)
    {
      existingAnnotations = al.getAlignmentAnnotation().length;
    }

    Annotation[] sol = new Annotation[al.getWidth()];
    boolean firstsol = true;

    while (i < preds.length)
    {
      String id = preds[i].getName().toUpperCase(Locale.ROOT);

      if (id.startsWith("LUPAS") || id.startsWith("JNET")
              || id.startsWith("JPRED"))
      {
        if (id.startsWith("JNETSOL"))
        {
          float amnt = (id.endsWith("25") ? 3f
                  : id.endsWith("5") ? 6f : 9f);
          for (int spos = 0; spos < width; spos++)
          {
            int sposw = (delMap == null) ? gapmap[spos]
                    : gapmap[delMap[spos]];
            if (firstsol)
            {
              sol[sposw] = new Annotation(0f);
            }
            if (preds[i].getCharAt(spos) == 'B'
                    && (sol[sposw].value == 0f || sol[sposw].value < amnt))
            {
              sol[sposw].value = amnt;
            }
          }
          firstsol = false;
        }
        else
        {
          // some other kind of annotation
          annotations = new Annotation[al.getWidth()];
          /*
           * if (delMap!=null) { for (int j=0; j<annotations.length; j++)
           * annotations[j] = new Annotation("","",'',0); }
           */
          if (id.equals("JNETPRED") || id.equals("JNETPSSM")
                  || id.equals("JNETFREQ") || id.equals("JNETHMM")
                  || id.equals("JNETALIGN") || id.equals("JPRED"))
          {
            if (delMap == null)
            {
              for (int j = 0; j < width; j++)
              {
                annotations[gapmap[j]] = new Annotation("", "",
                        preds[i].getCharAt(j), 0);
              }
            }
            else
            {
              for (int j = 0; j < width; j++)
              {
                annotations[gapmap[delMap[j]]] = new Annotation("", "",
                        preds[i].getCharAt(j), 0);
              }
            }
          }
          else if (id.equals("JNETCONF"))
          {
            if (delMap == null)
            {
              for (int j = 0; j < width; j++)
              {
                float value = Float.valueOf(preds[i].getCharAt(j) + "")
                        .floatValue();
                annotations[gapmap[j]] = new Annotation(
                        preds[i].getCharAt(j) + "", "",
                        preds[i].getCharAt(j), value);
              }
            }
            else
            {
              for (int j = 0; j < width; j++)
              {
                float value = Float.valueOf(preds[i].getCharAt(j) + "")
                        .floatValue();
                annotations[gapmap[delMap[j]]] = new Annotation(
                        preds[i].getCharAt(j) + "", "",
                        preds[i].getCharAt(j), value);
              }
            }
          }
          else
          {
            if (delMap == null)
            {
              for (int j = 0; j < width; j++)
              {
                annotations[gapmap[j]] = new Annotation(
                        preds[i].getCharAt(j) + "", "", ' ', 0);
              }
            }
            else
            {
              for (int j = 0; j < width; j++)
              {
                annotations[gapmap[delMap[j]]] = new Annotation(
                        preds[i].getCharAt(j) + "", "", ' ', 0);
              }
            }
          }

          if (id.equals("JNETCONF"))
          {
            annot = new AlignmentAnnotation(preds[i].getName(),
                    "JPred Output", annotations, 0f, 10f,
                    AlignmentAnnotation.BAR_GRAPH);
          }
          else
          {
            annot = new AlignmentAnnotation(preds[i].getName(),
                    "JPred Output", annotations);
          }

          if (seqRef != null)
          {
            annot.createSequenceMapping(seqRef, 1, true);
            seqRef.addAlignmentAnnotation(annot);
          }

          al.addAnnotation(annot);
          al.setAnnotationIndex(annot, al.getAlignmentAnnotation().length
                  - existingAnnotations - 1);
        }
        if (noMsa)
        {
          al.deleteSequence(preds[i]);
        }
      }

      i++;
    }
    if (!firstsol)
    {
      // add the solvent accessibility
      annot = new AlignmentAnnotation("Jnet Burial",
              "<html>Prediction of Solvent Accessibility<br/>levels are<ul><li>0 - Exposed</li><li>3 - 25% or more S.A. accessible</li><li>6 - 5% or more S.A. accessible</li><li>9 - Buried (<5% exposed)</li></ul>",
              sol, 0f, 9f, AlignmentAnnotation.BAR_GRAPH);

      annot.validateRangeAndDisplay();
      if (seqRef != null)
      {
        annot.createSequenceMapping(seqRef, 1, true);
        seqRef.addAlignmentAnnotation(annot);
      }
      al.addAnnotation(annot);
      al.setAnnotationIndex(annot,
              al.getAlignmentAnnotation().length - existingAnnotations - 1);
    }
    // Hashtable scores = prediction.getScores();

    /*
     * addFloatAnnotations(al, gapmap, (Vector)scores.get("JNETPROPH"),
     * "JnetpropH", "Jnet Helix Propensity", 0f,1f,1);
     * 
     * addFloatAnnotations(al, gapmap, (Vector)scores.get("JNETPROPB"),
     * "JnetpropB", "Jnet Beta Sheet Propensity", 0f,1f,1);
     * 
     * addFloatAnnotations(al, gapmap, (Vector)scores.get("JNETPROPC"),
     * "JnetpropC", "Jnet Coil Propensity", 0f,1f,1);
     */

  }
}
