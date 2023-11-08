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
package jalview.analysis;

import jalview.datamodel.AlignedCodon;

import java.util.Comparator;

/**
 * Implements rules for comparing two aligned codons, i.e. determining whether
 * they should occupy the same position in a translated protein alignment, or
 * one or the other should 'follow' (by preceded by a gap).
 * 
 * @author gmcarstairs
 *
 */
public final class CodonComparator implements Comparator<AlignedCodon>
{

  @Override
  public int compare(AlignedCodon ac1, AlignedCodon ac2)
  {
    if (ac1 == null || ac2 == null || ac1.equals(ac2))
    {
      return 0;
    }

    /**
     * <pre>
     * Case 1: if one starts before the other, and doesn't end after it, then it
     * precedes. We ignore the middle base position here.
     * A--GT
     * -CT-G
     * </pre>
     */
    if (ac1.pos1 < ac2.pos1 && ac1.pos3 <= ac2.pos3)
    {
      return -1;
    }
    if (ac2.pos1 < ac1.pos1 && ac2.pos3 <= ac1.pos3)
    {
      return 1;
    }

    /**
     * <pre>
     * Case 2: if one ends after the other, and doesn't start before it, then it
     * follows. We ignore the middle base position here.
     * -TG-A
     * G-TC
     * </pre>
     */
    if (ac1.pos3 > ac2.pos3 && ac1.pos1 >= ac2.pos1)
    {
      return 1;
    }
    if (ac2.pos3 > ac1.pos3 && ac2.pos1 >= ac1.pos1)
    {
      return -1;
    }

    /*
     * Case 3: if start and end match, compare middle base positions.
     */
    if (ac1.pos1 == ac2.pos1 && ac1.pos3 == ac2.pos3)
    {
      return Integer.compare(ac1.pos2, ac2.pos2);
    }

    /*
     * That just leaves the 'enclosing' case - one codon starts after but ends
     * before the other. If the middle bases don't match, use their comparison
     * (majority vote).
     */
    int compareMiddles = Integer.compare(ac1.pos2, ac2.pos2);
    if (compareMiddles != 0)
    {
      return compareMiddles;
    }

    /**
     * <pre>
     * Finally just leaves overlap with matching middle base, e.g. 
     * -A-A-A
     * G--GG 
     * In this case the choice is arbitrary whether to compare based on
     * first or last base position. We pick the first. Note this preserves
     * symmetricality of the comparison.
     * </pre>
     */
    return Integer.compare(ac1.pos1, ac2.pos1);
  }
}
