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
import jalview.datamodel.AnnotatedCollectionI;
import jalview.datamodel.SequenceCollectionI;
import jalview.datamodel.SequenceI;

import java.awt.Color;
import java.util.Map;

public interface ColourSchemeI
{
  /**
   * Returns the possibly context dependent colour for the given symbol at the
   * aligned position in the given sequence. For example, the colour may depend
   * on the symbol's relationship to the consensus residue for the column.
   * 
   * @param symbol
   * @param position
   * @param seq
   * @param consensusResidue
   *          the modal symbol (e.g. K) or symbols (e.g. KF) for the column
   * @param pid
   *          the percentage identity of the column's consensus (if known)
   * @return
   */
  Color findColour(char symbol, int position, SequenceI seq,
          String consensusResidue, float pid);

  /**
   * Recalculate dependent data using the given sequence collection, taking
   * account of hidden rows
   * 
   * @param alignment
   * @param hiddenReps
   */
  void alignmentChanged(AnnotatedCollectionI alignment,
          Map<SequenceI, SequenceCollectionI> hiddenReps);

  /**
   * Creates and returns a new instance of the colourscheme configured to colour
   * the given collection. Note that even simple colour schemes should return a
   * new instance for each call to this method, as different instances may have
   * differing shading by consensus or percentage identity applied.
   * 
   * @param viewport
   *          - the parent viewport
   * @param sg
   *          - the collection of sequences to be coloured
   * @return copy of current scheme with any inherited settings transferred
   */
  ColourSchemeI getInstance(AlignViewportI viewport,
          AnnotatedCollectionI sg);

  /**
   * Answers true if the colour scheme is suitable for the given data, else
   * false. For example, some colour schemes are specific to either peptide or
   * nucleotide, or only apply if certain kinds of annotation are present.
   * 
   * @param ac
   * @return
   */
  // TODO can make this method static in Java 8
  boolean isApplicableTo(AnnotatedCollectionI ac);

  /**
   * Answers the 'official' name of the colour scheme (as used, for example, as
   * a Jalview startup parameter)
   * 
   * @return
   */
  String getSchemeName();

  /**
   * Answers true if the colour scheme depends only on the sequence symbol, and
   * not on other information such as alignment consensus or annotation. (Note
   * that simple colour schemes may have a fading by percentage identity or
   * conservation overlaid.) Simple colour schemes can be propagated to
   * structure viewers.
   * 
   * @return
   */
  boolean isSimple();

  /**
   * Answers true if the colour scheme has a colour specified for gaps.
   * 
   * @return
   */
  boolean hasGapColour();
}
