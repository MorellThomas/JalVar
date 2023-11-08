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
package jalview.schemes;

import jalview.api.AlignViewportI;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AnnotatedCollectionI;
import jalview.datamodel.Annotation;
import jalview.datamodel.SequenceCollectionI;
import jalview.datamodel.SequenceI;
import jalview.io.TCoffeeScoreFile;

import java.awt.Color;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines the color score for T-Coffee MSA
 * <p>
 * See http://tcoffee.org
 * 
 * 
 * @author Paolo Di Tommaso
 * 
 */
public class TCoffeeColourScheme extends ResidueColourScheme
{
  IdentityHashMap<SequenceI, Color[]> seqMap;

  /**
   * Default constructor (required for Class.newInstance())
   */
  public TCoffeeColourScheme()
  {

  }

  /**
   * the color scheme needs to look at the alignment to get and cache T-COFFEE
   * scores
   * 
   * @param alignment
   *          - annotated sequences to be searched
   */
  public TCoffeeColourScheme(AnnotatedCollectionI alignment)
  {
    alignmentChanged(alignment, null);
  }

  /**
   * Finds the TCoffeeScore annotation (if any) for each sequence and notes the
   * annotation colour for each column position. The colours are fixed for
   * scores 0-9 and are set when annotation is parsed.
   * 
   * @see TCoffeeScoreFile#annotateAlignment(AlignmentI, boolean)
   */
  @Override
  public void alignmentChanged(AnnotatedCollectionI alignment,
          Map<SequenceI, SequenceCollectionI> hiddenReps)
  {
    // TODO: if sequences have been represented and they have scores, could
    // compute an average sequence score for the representative

    // assume only one set of TCOFFEE scores - but could have more than one
    // potentially.
    List<AlignmentAnnotation> annots = new ArrayList<>();
    // Search alignment to get all tcoffee annotation and pick one set of
    // annotation to use to colour seqs.
    seqMap = new IdentityHashMap<>();
    AnnotatedCollectionI alcontext = alignment instanceof AlignmentI
            ? alignment
            : alignment.getContext();
    if (alcontext == null)
    {
      return;
    }
    int w = 0;
    for (AlignmentAnnotation al : alcontext
            .findAnnotation(TCoffeeScoreFile.TCOFFEE_SCORE))
    {
      if (al.sequenceRef != null && !al.belowAlignment)
      {
        annots.add(al);
        if (w < al.annotations.length)
        {
          w = al.annotations.length;
        }
        Color[] scores = new Color[al.annotations.length];
        int i = 0;
        for (Annotation an : al.annotations)
        {
          scores[i++] = (an != null) ? an.colour : Color.white;
        }
        seqMap.put(al.sequenceRef, scores);
      }
    }
    // TODO: compute average colour for each symbol type in each column - gives
    // a second order colourscheme for colouring a sequence logo derived from
    // the alignment (colour reflects quality of alignment for each residue
    // class)
  }

  @Override
  public Color findColour(char c, int j, SequenceI seq)
  {
    if (seqMap == null)
    {
      return Color.WHITE;
    }
    Color[] cols = seqMap.get(seq);
    if (cols == null)
    {
      // see above TODO about computing a colour for each residue in each
      // column: cc = _rcols[i][indexFor[c]];
      return Color.white;
    }

    if (j < 0 || j >= cols.length)
    {
      return Color.white;
    }
    return cols[j];
  }

  @Override
  public ColourSchemeI getInstance(AlignViewportI view,
          AnnotatedCollectionI sg)
  {
    return new TCoffeeColourScheme(sg);
  }

  /**
   * Answers true if the data has TCoffee score annotation
   */
  @Override
  public boolean isApplicableTo(AnnotatedCollectionI ac)
  {
    AnnotatedCollectionI alcontext = ac instanceof AlignmentI ? ac
            : ac.getContext();
    if (alcontext == null)
    {
      return false;
    }
    Iterable<AlignmentAnnotation> anns = alcontext
            .findAnnotation(TCoffeeScoreFile.TCOFFEE_SCORE);
    return anns.iterator().hasNext();
  }

  @Override
  public String getSchemeName()
  {
    return JalviewColourScheme.TCoffee.toString();
  }

  @Override
  public boolean isSimple()
  {
    return false;
  }
}
