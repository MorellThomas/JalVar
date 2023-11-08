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

import java.util.ArrayList;
import java.util.Hashtable;

public class SecStrConsensus
{

  /**
   * Internal class to represent a simple base-pair.
   * 
   * @author Yawn [JBPNote: ^^ is that Anne Menard or Ya(w)nn Ponty, I wonder !
   *         ]
   */
  public static class SimpleBP
  {
    int bp5;

    int bp3;

    public SimpleBP()
    {

    }

    public SimpleBP(int i5, int i3)
    {
      bp5 = i5;
      bp3 = i3;
    }

    public void setBP5(int i5)
    {
      bp5 = i5;
    }

    public void setBP3(int i3)
    {
      bp3 = i3;
    }

    public int getBP5()
    {
      return bp5;
    }

    public int getBP3()
    {
      return bp3;
    }

    public String toString()
    {
      return "(" + bp5 + "," + bp3 + ")";
    }

  }

  public static int[] extractConsensus(ArrayList<ArrayList<SimpleBP>> bps)
  {
    // We do not currently know the length of the alignment
    // => Estimate it as the biggest index of a base-pair plus one.
    int maxlength = 0;
    for (ArrayList<SimpleBP> strs : bps)
    {
      for (SimpleBP bp : strs)
      {

        maxlength = Math.max(1 + Math.max(bp.bp5, bp.bp3), maxlength);

      }
    }
    // Now we have a good estimate for length, allocate and initialize data
    // to be fed to the dynamic programming procedure.
    ArrayList<Hashtable<Integer, Double>> seq = new ArrayList<Hashtable<Integer, Double>>();
    for (int i = 0; i < maxlength; i++)
    {
      seq.add(new Hashtable<Integer, Double>());
    }
    for (ArrayList<SimpleBP> strs : bps)
    {
      for (SimpleBP bp : strs)
      {
        int i = bp.bp5;
        int j = bp.bp3;
        Hashtable<Integer, Double> h = seq.get(i);
        if (!h.containsKey(j))
        {
          h.put(j, 0.0);
        }
        h.put(j, h.get(j) + 1.);
      }
    }
    // At this point, seq contains, at each position i, a hashtable which
    // associates,
    // to each possible end j, the number of time a base-pair (i,j) occurs in
    // the alignment

    // We can now run the dynamic programming procedure on this data
    double[][] mat = fillMatrix(seq);
    ArrayList<SimpleBP> res = backtrack(mat, seq);

    // Convert it to an array, ie finalres[i] = j >= 0 iff a base-pair (i,j) is
    // present
    // in the consensus, or -1 otherwise
    int[] finalres = new int[seq.size()];
    for (int i = 0; i < seq.size(); i++)
    {
      finalres[i] = -1;
    }
    for (SimpleBP bp : res)
    {
      finalres[bp.bp5] = bp.bp3;
      finalres[bp.bp3] = bp.bp5;
    }

    return finalres;
  }

  private static boolean canBasePair(
          ArrayList<Hashtable<Integer, Double>> seq, int i, int k)
  {
    return seq.get(i).containsKey(k);
  }

  // Returns the score of a potential base-pair, ie the number of structures in
  // which it is found.
  private static double basePairScore(
          ArrayList<Hashtable<Integer, Double>> seq, int i, int k)
  {
    return seq.get(i).get(k);
  }

  private static double[][] fillMatrix(
          ArrayList<Hashtable<Integer, Double>> seq)
  {
    int n = seq.size();
    double[][] tab = new double[n][n];
    for (int m = 1; m <= n; m++)
    {
      for (int i = 0; i < n - m + 1; i++)
      {
        int j = i + m - 1;
        tab[i][j] = 0;
        if (i < j)
        {
          tab[i][j] = Math.max(tab[i][j], tab[i + 1][j]);
          for (int k = i + 1; k <= j; k++)
          {
            if (canBasePair(seq, i, k))
            {
              double fact1 = 0;
              if (k > i + 1)
              {
                fact1 = tab[i + 1][k - 1];
              }
              double fact2 = 0;
              if (k < j)
              {
                fact2 = tab[k + 1][j];
              }
              tab[i][j] = Math.max(tab[i][j],
                      basePairScore(seq, i, k) + fact1 + fact2);
            }
          }
        }
      }
    }
    return tab;
  }

  private static ArrayList<SimpleBP> backtrack(double[][] tab,
          ArrayList<Hashtable<Integer, Double>> seq)
  {
    return backtrack(tab, seq, 0, seq.size() - 1);
  }

  private static ArrayList<SimpleBP> backtrack(double[][] tab,
          ArrayList<Hashtable<Integer, Double>> seq, int i, int j)
  {
    ArrayList<SimpleBP> result = new ArrayList<SimpleBP>();
    if (i < j)
    {
      ArrayList<Integer> indices = new ArrayList<Integer>();
      indices.add(-1);
      for (int k = i + 1; k <= j; k++)
      {
        indices.add(k);
      }
      for (int k : indices)
      {
        if (k == -1)
        {
          if (tab[i][j] == tab[i + 1][j])
          {
            result = backtrack(tab, seq, i + 1, j);
          }
        }
        else
        {
          if (canBasePair(seq, i, k))
          {
            double fact1 = 0;
            if (k > i + 1)
            {
              fact1 = tab[i + 1][k - 1];
            }
            double fact2 = 0;
            if (k < j)
            {
              fact2 = tab[k + 1][j];
            }
            if (tab[i][j] == basePairScore(seq, i, k) + fact1 + fact2)
            {
              result = backtrack(tab, seq, i + 1, k - 1);
              result.addAll(backtrack(tab, seq, k + 1, j));
              result.add(new SimpleBP(i, k));
            }
          }
        }
      }
    }
    else if (i == j)
    {

    }
    else
    {

    }
    return result;
  }

}
