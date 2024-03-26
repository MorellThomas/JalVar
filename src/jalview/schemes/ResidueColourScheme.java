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

import jalview.datamodel.AnnotatedCollectionI;
import jalview.datamodel.SequenceCollectionI;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;

import java.awt.Color;
import java.util.Map;

/**
 * Base class for residue-based colour schemes
 */
public abstract class ResidueColourScheme implements ColourSchemeI
{
  public static final String NONE = "None";

  /*
   * default display name for a user defined colour scheme
   */
  public static final String USER_DEFINED = "User Defined";

  /*
   * name for (new) "User Defined.." colour scheme menu item
   */
  public static final String USER_DEFINED_MENU = "*User Defined*";

  /*
   * the canonical name of the annotation colour scheme 
   * (may be used to identify it in menu items)
   */
  public static final String ANNOTATION_COLOUR = "Annotation";

  /*
   * lookup up by character value e.g. 'G' to the colors array index
   * e.g. if symbolIndex['K'] = 11 then colors[11] is the colour for K
   */
  final int[] symbolIndex;

  /*
   * colour for residue characters as indexed by symbolIndex
   */
  Color[] colors = null;

  /* Set when threshold colouring to either pid_gaps or pid_nogaps */
  protected boolean ignoreGaps = false;

  /**
   * Creates a new ResidueColourScheme object.
   * 
   * @param final
   *          int[] index table into colors (ResidueProperties.naIndex or
   *          ResidueProperties.aaIndex)
   * @param colors
   *          colours for symbols in sequences
   */
  public ResidueColourScheme(int[] aaOrnaIndex, Color[] colours)
  {
    symbolIndex = aaOrnaIndex;
    this.colors = colours;
  }

  /**
   * Creates a new ResidueColourScheme object with a lookup table for indexing
   * the colour map
   */
  public ResidueColourScheme(int[] aaOrNaIndex)
  {
    symbolIndex = aaOrNaIndex;
  }

  /**
   * Creates a new ResidueColourScheme object - default constructor for
   * non-sequence dependent colourschemes
   */
  public ResidueColourScheme()
  {
    symbolIndex = null;
  }
  
  public void setColours(Color[] colours)
  {
    this.colors = colours;
  }

  /**
   * Find a colour without an index in a sequence
   */
  public Color findColour(char c)
  {
    Color colour = Color.white;

    if (colors != null && symbolIndex != null && c < symbolIndex.length
            && symbolIndex[c] < colors.length)
    {
      colour = colors[symbolIndex[c]];
    }

    return colour;
  }

  /**
   * Default is to call the overloaded method that ignores consensus. A colour
   * scheme that depends on consensus (for example, Blosum62), should override
   * this method instead.
   */
  @Override
  public Color findColour(char c, int j, SequenceI seq,
          String consensusResidue, float pid)
  {
    return findColour(c, j, seq);
  }

  /**
   * Default implementation looks up the residue colour in a fixed scheme, or
   * returns White if not found. Override this method for a colour scheme that
   * depends on the column position or sequence.
   * 
   * @param c
   * @param j
   * @param seq
   * @return
   */
  protected Color findColour(char c, int j, SequenceI seq)
  {
    return findColour(c);
  }

  @Override
  public void alignmentChanged(AnnotatedCollectionI alignment,
          Map<SequenceI, SequenceCollectionI> hiddenReps)
  {
  }

  /**
   * Answers false if the colour scheme is nucleotide or peptide specific, and
   * the data does not match, else true. Override to modify or extend this test
   * as required.
   */
  @Override
  public boolean isApplicableTo(AnnotatedCollectionI ac)
  {
    if (!isPeptideSpecific() && !isNucleotideSpecific())
    {
      return true;
    }
    if (ac == null)
    {
      return true;
    }
    /*
     * pop-up menu on selection group before group created
     * (no alignment context)
     */
    // TODO: add nucleotide flag to SequenceGroup?
    if (ac instanceof SequenceGroup && ac.getContext() == null)
    {
      return true;
    }

    /*
     * inspect the data context (alignment) for residue type
     */
    boolean nucleotide = ac.isNucleotide();

    /*
     * does data type match colour scheme type?
     */
    return (nucleotide && isNucleotideSpecific())
            || (!nucleotide && isPeptideSpecific());
  }

  /**
   * Answers true if the colour scheme is normally only for peptide data
   * 
   * @return
   */
  public boolean isPeptideSpecific()
  {
    return false;
  }

  /**
   * Answers true if the colour scheme is normally only for nucleotide data
   * 
   * @return
   */
  public boolean isNucleotideSpecific()
  {
    return false;
  }

  /**
   * Default method returns true. Override this to return false in colour
   * schemes that are not determined solely by the sequence symbol.
   */
  @Override
  public boolean isSimple()
  {
    return true;
  }

  /**
   * Default method returns false. Override this to return true in colour
   * schemes that have a colour associated with gap residues.
   */
  @Override
  public boolean hasGapColour()
  {
    return false;
  }
}
