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

import java.awt.Color;
import java.util.List;
import java.util.Map;

import jalview.api.AlignViewportI;
import jalview.datamodel.AnnotatedCollectionI;
import jalview.datamodel.SequenceCollectionI;
import jalview.datamodel.SequenceI;
import jalview.util.Comparison;

public class ClustalxColourScheme extends ResidueColourScheme
{
  private static final int EIGHTY_FIVE = 85;

  private static final int FIFTY = 50;

  private static final int EIGHTY = 80;

  private static final int SIXTY = 60;

  enum ClustalColour
  {
    RED(0.9f, 0.2f, 0.1f), BLUE(0.5f, 0.7f, 0.9f), GREEN(0.1f, 0.8f, 0.1f),
    ORANGE(0.9f, 0.6f, 0.3f), CYAN(0.1f, 0.7f, 0.7f),
    PINK(0.9f, 0.5f, 0.5f), MAGENTA(0.8f, 0.3f, 0.8f),
    YELLOW(0.8f, 0.8f, 0.0f);

    final Color colour;

    ClustalColour(float r, float g, float b)
    {
      colour = new Color(r, g, b);
    }
  }

  private class ConsensusColour
  {
    Consensus[] cons;

    Color c;

    public ConsensusColour(ClustalColour col, Consensus[] conses)
    {
      this.cons = conses;
      this.c = col.colour;
    }
  }

  private int[][] cons2;

  private ConsensusColour[] colours;

  private ConsensusColour[] residueColour;

  private int size;

  private Consensus[] conses = new Consensus[32];

  private boolean includeGaps = true;

  /**
   * Default constructor (required for Class.newInstance())
   */
  public ClustalxColourScheme()
  {

  }

  public ClustalxColourScheme(AnnotatedCollectionI alignment,
          Map<SequenceI, SequenceCollectionI> hiddenReps)
  {
    alignmentChanged(alignment, hiddenReps);
  }

  @Override
  public synchronized void alignmentChanged(AnnotatedCollectionI alignment,
          Map<SequenceI, SequenceCollectionI> hiddenReps)
  {
    int maxWidth = alignment.getWidth();
    List<SequenceI> seqs = alignment.getSequences(hiddenReps);
    cons2 = new int[maxWidth][24];
    includeGaps = isIncludeGaps(); // does nothing - TODO replace with call to
    // get the current setting of the
    // includeGaps param.
    int res = 0;

    for (SequenceI sq : seqs)
    {
      int end_j = sq.getLength() - 1;
      int length = sq.getLength();

      for (int i = 0; i <= end_j; i++)
      {
        if (length - 1 < i)
        {
          res = 23;
        }
        else
        {
          res = ResidueProperties.aaIndex[sq.getCharAt(i)];
        }
        cons2[i][res]++;
      }
    }

    this.size = seqs.size();
    makeColours();
  }

  void makeColours()
  {
    conses[0] = new Consensus("WLVIMAFCYHP", SIXTY);
    conses[1] = new Consensus("WLVIMAFCYHP", EIGHTY);
    conses[2] = new Consensus("ED", FIFTY);
    conses[3] = new Consensus("KR", SIXTY);
    conses[4] = new Consensus("G", FIFTY);
    conses[5] = new Consensus("N", FIFTY);
    conses[6] = new Consensus("QE", FIFTY);
    conses[7] = new Consensus("P", FIFTY);
    conses[8] = new Consensus("TS", FIFTY);

    conses[26] = new Consensus("A", EIGHTY_FIVE);
    conses[27] = new Consensus("C", EIGHTY_FIVE);
    conses[10] = new Consensus("E", EIGHTY_FIVE);
    conses[11] = new Consensus("F", EIGHTY_FIVE);
    conses[12] = new Consensus("G", EIGHTY_FIVE);
    conses[13] = new Consensus("H", EIGHTY_FIVE);
    conses[14] = new Consensus("I", EIGHTY_FIVE);
    conses[15] = new Consensus("L", EIGHTY_FIVE);
    conses[16] = new Consensus("M", EIGHTY_FIVE);
    conses[17] = new Consensus("N", EIGHTY_FIVE);
    conses[18] = new Consensus("P", EIGHTY_FIVE);
    conses[19] = new Consensus("Q", EIGHTY_FIVE);
    conses[20] = new Consensus("R", EIGHTY_FIVE);
    conses[21] = new Consensus("S", EIGHTY_FIVE);
    conses[22] = new Consensus("T", EIGHTY_FIVE);
    conses[23] = new Consensus("V", EIGHTY_FIVE);
    conses[24] = new Consensus("W", EIGHTY_FIVE);
    conses[25] = new Consensus("Y", EIGHTY_FIVE);
    conses[28] = new Consensus("K", EIGHTY_FIVE);
    conses[29] = new Consensus("D", EIGHTY_FIVE);

    conses[30] = new Consensus("G", 0);
    conses[31] = new Consensus("P", 0);

    // We now construct the colours
    colours = new ConsensusColour[11];

    Consensus[] tmp8 = new Consensus[1];
    tmp8[0] = conses[30]; // G
    colours[7] = new ConsensusColour(ClustalColour.ORANGE, tmp8);

    Consensus[] tmp9 = new Consensus[1];
    tmp9[0] = conses[31]; // P
    colours[8] = new ConsensusColour(ClustalColour.YELLOW, tmp9);

    Consensus[] tmp10 = new Consensus[1];
    tmp10[0] = conses[27]; // C
    colours[9] = new ConsensusColour(ClustalColour.PINK, tmp8);

    Consensus[] tmp1 = new Consensus[14];
    tmp1[0] = conses[0]; // %
    tmp1[1] = conses[1]; // #
    tmp1[2] = conses[26]; // A
    tmp1[3] = conses[27]; // C
    tmp1[4] = conses[11]; // F
    tmp1[5] = conses[13]; // H
    tmp1[6] = conses[14]; // I
    tmp1[7] = conses[15]; // L
    tmp1[8] = conses[16]; // M
    tmp1[9] = conses[23]; // V
    tmp1[10] = conses[24]; // W
    tmp1[11] = conses[25]; // Y
    tmp1[12] = conses[18]; // P
    tmp1[13] = conses[19]; // p
    colours[0] = new ConsensusColour(ClustalColour.BLUE, tmp1);

    colours[10] = new ConsensusColour(ClustalColour.CYAN, tmp1);

    Consensus[] tmp2 = new Consensus[5];
    tmp2[0] = conses[8]; // t
    tmp2[1] = conses[21]; // S
    tmp2[2] = conses[22]; // T
    tmp2[3] = conses[0]; // %
    tmp2[4] = conses[1]; // #
    colours[1] = new ConsensusColour(ClustalColour.GREEN, tmp2);

    Consensus[] tmp3 = new Consensus[3];

    tmp3[0] = conses[17]; // N
    tmp3[1] = conses[29]; // D
    tmp3[2] = conses[5]; // n
    colours[2] = new ConsensusColour(ClustalColour.GREEN, tmp3);

    Consensus[] tmp4 = new Consensus[6];
    tmp4[0] = conses[6]; // q = QE
    tmp4[1] = conses[19]; // Q
    tmp4[2] = conses[22]; // E
    tmp4[3] = conses[3]; // +
    tmp4[4] = conses[28]; // K
    tmp4[5] = conses[20]; // R
    colours[3] = new ConsensusColour(ClustalColour.GREEN, tmp4);

    Consensus[] tmp5 = new Consensus[4];
    tmp5[0] = conses[3]; // +
    tmp5[1] = conses[28]; // K
    tmp5[2] = conses[20]; // R
    tmp5[3] = conses[19]; // Q
    colours[4] = new ConsensusColour(ClustalColour.RED, tmp5);

    Consensus[] tmp6 = new Consensus[6];
    tmp6[0] = conses[3]; // -
    tmp6[1] = conses[29]; // D
    tmp6[2] = conses[10]; // E
    tmp6[3] = conses[6]; // QE
    tmp6[4] = conses[19]; // Q
    tmp6[5] = conses[2]; // DE
    colours[5] = new ConsensusColour(ClustalColour.MAGENTA, tmp6);

    Consensus[] tmp7 = new Consensus[5];
    tmp7[0] = conses[3]; // -
    tmp7[1] = conses[29]; // D
    tmp7[2] = conses[10]; // E
    tmp7[3] = conses[17]; // N
    tmp7[4] = conses[2]; // DE
    colours[6] = new ConsensusColour(ClustalColour.MAGENTA, tmp7);

    // Now attach the ConsensusColours to the residue letters
    residueColour = new ConsensusColour[20];
    residueColour[0] = colours[0]; // A
    residueColour[1] = colours[4]; // R
    residueColour[2] = colours[2]; // N
    residueColour[3] = colours[6]; // D
    residueColour[4] = colours[0]; // C
    residueColour[5] = colours[3]; // Q
    residueColour[6] = colours[5]; // E
    residueColour[7] = colours[7]; // G
    residueColour[8] = colours[10]; // H
    residueColour[9] = colours[0]; // I
    residueColour[10] = colours[0]; // L
    residueColour[11] = colours[4]; // K
    residueColour[12] = colours[0]; // M
    residueColour[13] = colours[0]; // F
    residueColour[14] = colours[8]; // P
    residueColour[15] = colours[1]; // S
    residueColour[16] = colours[1]; // T
    residueColour[17] = colours[0]; // W
    residueColour[18] = colours[10]; // Y
    residueColour[19] = colours[0]; // V
  }

  @Override
  public Color findColour(char c)
  {
    return Color.pink;
  }

  @Override
  protected synchronized Color findColour(char c, int j, SequenceI seq)
  {
    // TODO why the test for includeGaps here?
    if (cons2.length <= j || Comparison.isGap(c)
    /*|| (includeGaps && threshold != 0 && !aboveThreshold(c, j))*/)
    {
      return Color.white;
    }

    int i = ResidueProperties.aaIndex[c];

    Color colour = Color.white;

    if (i > 19)
    {
      return colour;
    }

    for (int k = 0; k < residueColour[i].cons.length; k++)
    {
      if (residueColour[i].cons[k].isConserved(cons2, j, size, includeGaps))
      {
        colour = residueColour[i].c;
      }
    }

    if (i == 4)
    {
      /*
       * override to colour C pink if >85% conserved
       */
      if (conses[27].isConserved(cons2, j, size, includeGaps))
      {
        colour = ClustalColour.PINK.colour;
      }
    }

    return colour;
  }

  /**
   * @return the includeGaps
   */
  protected boolean isIncludeGaps()
  {
    return includeGaps;
  }

  /**
   * @param includeGaps
   *          the includeGaps to set
   */
  protected void setIncludeGaps(boolean includeGaps)
  {
    this.includeGaps = includeGaps;
  }

  @Override
  public ColourSchemeI getInstance(AlignViewportI view,
          AnnotatedCollectionI sg)
  {
    ClustalxColourScheme css = new ClustalxColourScheme(sg,
            view == null ? null : view.getHiddenRepSequences());
    css.includeGaps = includeGaps;
    return css;
  }

  @Override
  public boolean isPeptideSpecific()
  {
    return true;
  }

  @Override
  public String getSchemeName()
  {
    return JalviewColourScheme.Clustal.toString();
  }

  @Override
  public boolean isSimple()
  {
    return false;
  }
}
