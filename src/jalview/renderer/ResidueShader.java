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
package jalview.renderer;

import jalview.analysis.Conservation;
import jalview.api.ViewStyleI;
import jalview.datamodel.AnnotatedCollectionI;
import jalview.datamodel.ProfileI;
import jalview.datamodel.ProfilesI;
import jalview.datamodel.SequenceCollectionI;
import jalview.datamodel.SequenceI;
import jalview.schemes.ColourSchemeI;
import jalview.util.ColorUtils;
import jalview.util.Comparison;

import java.awt.Color;
import java.util.Map;

/**
 * A class that computes the colouring of an alignment (or subgroup). Currently
 * the factors that may influence residue colouring are
 * <ul>
 * <li>the colour scheme that provides a colour for each aligned residue</li>
 * <li>any threshold for colour, based on percentage identity with
 * consensus</li>
 * <li>any graduation based on conservation of physico-chemical properties</li>
 * </ul>
 * 
 * @author gmcarstairs
 *
 */
public class ResidueShader implements ResidueShaderI
{
  private static final int INITIAL_CONSERVATION = 30;

  /*
   * the colour scheme that gives the colour of each residue
   * before applying any conservation or PID shading
   */
  private ColourSchemeI colourScheme;

  /*
   * the consensus data for each column
   */
  private ProfilesI consensus;

  /*
   * if true, apply shading of colour by conservation
   */
  private boolean conservationColouring;

  /*
   * the physico-chemical property conservation scores for columns, with values
   * 0-9, '+' (all properties conserved), '*' (residue fully conserved) or '-' (gap)
   * (may be null if colour by conservation is not selected)
   */
  private char[] conservation;

  /*
   * minimum percentage identity for colour to be applied;
   * if above zero, residue must match consensus (or joint consensus)
   * and column have >= pidThreshold identity with the residue
   */
  private int pidThreshold;

  /*
   * if true, ignore gaps in percentage identity calculation
   */
  private boolean ignoreGaps;

  /*
   * setting of the By Conservation slider
   */
  private int conservationIncrement = INITIAL_CONSERVATION;

  public ResidueShader(ColourSchemeI cs)
  {
    colourScheme = cs;
  }

  /**
   * Default constructor
   */
  public ResidueShader()
  {
  }

  /**
   * Constructor given view style settings
   * 
   * @param viewStyle
   */
  public ResidueShader(ViewStyleI viewStyle)
  {
    // TODO remove duplicated storing of conservation / pid thresholds?
    this();
    setConservationApplied(viewStyle.isConservationColourSelected());
    // setThreshold(viewStyle.getThreshold());
  }

  /**
   * Copy constructor
   */
  public ResidueShader(ResidueShader rs)
  {
    this.colourScheme = rs.colourScheme;
    this.consensus = rs.consensus;
    this.conservation = rs.conservation;
    this.conservationColouring = rs.conservationColouring;
    this.conservationIncrement = rs.conservationIncrement;
    this.ignoreGaps = rs.ignoreGaps;
    this.pidThreshold = rs.pidThreshold;
  }

  /**
   * @see jalview.renderer.ResidueShaderI#setConsensus(jalview.datamodel.ProfilesI)
   */
  @Override
  public void setConsensus(ProfilesI cons)
  {
    consensus = cons;
  }

  /**
   * @see jalview.renderer.ResidueShaderI#conservationApplied()
   */
  @Override
  public boolean conservationApplied()
  {
    return conservationColouring;
  }

  /**
   * @see jalview.renderer.ResidueShaderI#setConservationApplied(boolean)
   */
  @Override
  public void setConservationApplied(boolean conservationApplied)
  {
    conservationColouring = conservationApplied;
  }

  /**
   * @see jalview.renderer.ResidueShaderI#setConservation(jalview.analysis.Conservation)
   */
  @Override
  public void setConservation(Conservation cons)
  {
    if (cons == null)
    {
      conservationColouring = false;
      conservation = null;
    }
    else
    {
      conservationColouring = true;
      conservation = cons.getConsSequence().getSequenceAsString()
              .toCharArray();
    }

  }

  /**
   * @see jalview.renderer.ResidueShaderI#alignmentChanged(jalview.datamodel.AnnotatedCollectionI,
   *      java.util.Map)
   */
  @Override
  public void alignmentChanged(AnnotatedCollectionI alignment,
          Map<SequenceI, SequenceCollectionI> hiddenReps)
  {
    if (colourScheme != null)
    {
      colourScheme.alignmentChanged(alignment, hiddenReps);
    }
  }

  /**
   * @see jalview.renderer.ResidueShaderI#setThreshold(int, boolean)
   */
  @Override
  public void setThreshold(int consensusThreshold, boolean ignoreGaps)
  {
    pidThreshold = consensusThreshold;
    this.ignoreGaps = ignoreGaps;
  }

  /**
   * @see jalview.renderer.ResidueShaderI#setConservationInc(int)
   */
  @Override
  public void setConservationInc(int i)
  {
    conservationIncrement = i;
  }

  /**
   * @see jalview.renderer.ResidueShaderI#getConservationInc()
   */
  @Override
  public int getConservationInc()
  {
    return conservationIncrement;
  }

  /**
   * @see jalview.renderer.ResidueShaderI#getThreshold()
   */
  @Override
  public int getThreshold()
  {
    return pidThreshold;
  }

  /**
   * @see jalview.renderer.ResidueShaderI#findColour(char, int,
   *      jalview.datamodel.SequenceI)
   */
  @Override
  public Color findColour(char symbol, int position, SequenceI seq)
  {
    if (colourScheme == null)
    {
      return Color.white; // Colour is 'None'
    }

    /*
     * get 'base' colour
     */
    ProfileI profile = consensus == null ? null : consensus.get(position);
    String modalResidue = profile == null ? null
            : profile.getModalResidue();
    float pid = profile == null ? 0f
            : profile.getPercentageIdentity(ignoreGaps);
    Color colour = colourScheme.findColour(symbol, position, seq,
            modalResidue, pid);

    /*
     * apply PID threshold and consensus fading if in force
     */
    if (!Comparison.isGap(symbol))
    {
      colour = adjustColour(symbol, position, colour);
    }

    return colour;
  }

  /**
   * Adjusts colour by applying thresholding or conservation shading, if in
   * force. That is
   * <ul>
   * <li>if there is a threshold set for colouring, and the residue doesn't
   * match the consensus (or a joint consensus) residue, or the consensus score
   * is not above the threshold, then the colour is set to white</li>
   * <li>if conservation colouring is selected, the colour is faded by an amount
   * depending on the conservation score for the column, and the conservation
   * colour threshold</li>
   * </ul>
   * 
   * @param symbol
   * @param column
   * @param colour
   * @return
   */
  protected Color adjustColour(char symbol, int column, Color colour)
  {
    if (!aboveThreshold(symbol, column))
    {
      colour = Color.white;
    }

    if (conservationColouring)
    {
      colour = applyConservation(colour, column);
    }
    return colour;
  }

  /**
   * Answers true if there is a consensus profile for the specified column, and
   * the given residue matches the consensus (or joint consensus) residue for
   * the column, and the percentage identity for the profile is equal to or
   * greater than the current threshold; else answers false. The percentage
   * calculation depends on whether or not we are ignoring gapped sequences.
   * 
   * @param residue
   * @param column
   *          (index into consensus profiles)
   * 
   * @return
   * @see #setThreshold(int, boolean)
   */
  protected boolean aboveThreshold(char residue, int column)
  {
    if (pidThreshold == 0)
    {
      return true;
    }
    if ('a' <= residue && residue <= 'z')
    {
      // TO UPPERCASE !!!
      // Faster than toUpperCase
      residue -= ('a' - 'A');
    }

    if (consensus == null)
    {
      return false;
    }

    ProfileI profile = consensus.get(column);

    /*
     * test whether this is the consensus (or joint consensus) residue
     */
    if (profile != null
            && profile.getModalResidue().contains(String.valueOf(residue)))
    {
      if (profile.getPercentageIdentity(ignoreGaps) >= pidThreshold)
      {
        return true;
      }
    }

    return false;
  }

  /**
   * Applies a combination of column conservation score, and conservation
   * percentage slider, to 'bleach' out the residue colours towards white.
   * <p>
   * If a column is fully conserved (identical residues, conservation score 11,
   * shown as *), or all 10 physico-chemical properties are conserved
   * (conservation score 10, shown as +), then the colour is left unchanged.
   * <p>
   * Otherwise a 'bleaching' factor is computed and applied to the colour. This
   * is designed to fade colours for scores of 0-9 completely to white at slider
   * positions ranging from 18% - 100% respectively.
   * 
   * @param currentColour
   * @param column
   * 
   * @return bleached (or unmodified) colour
   */
  protected Color applyConservation(Color currentColour, int column)
  {
    if (conservation == null || conservation.length <= column)
    {
      return currentColour;
    }
    char conservationScore = conservation[column];

    /*
     * if residues are fully conserved (* or 11), or all properties
     * are conserved (+ or 10), leave colour unchanged
     */
    if (conservationScore == '*' || conservationScore == '+'
            || conservationScore == (char) 10
            || conservationScore == (char) 11)
    {
      return currentColour;
    }

    if (Comparison.isGap(conservationScore))
    {
      return Color.white;
    }

    /*
     * convert score 0-9 to a bleaching factor 1.1 - 0.2
     */
    float bleachFactor = (11 - (conservationScore - '0')) / 10f;

    /*
     * scale this up by 0-5 (percentage slider / 20)
     * as a result, scores of:         0  1  2  3  4  5  6  7  8  9
     * fade to white at slider value: 18 20 22 25 29 33 40 50 67 100%
     */
    bleachFactor *= (conservationIncrement / 20f);

    return ColorUtils.bleachColour(currentColour, bleachFactor);
  }

  /**
   * @see jalview.renderer.ResidueShaderI#getColourScheme()
   */
  @Override
  public ColourSchemeI getColourScheme()
  {
    return this.colourScheme;
  }

  /**
   * @see jalview.renderer.ResidueShaderI#setColourScheme(jalview.schemes.ColourSchemeI)
   */
  @Override
  public void setColourScheme(ColourSchemeI cs)
  {
    colourScheme = cs;
  }
}
