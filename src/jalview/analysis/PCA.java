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

import jalview.api.analysis.ScoreModelI;
import jalview.api.analysis.SimilarityParamsI;
import jalview.bin.Console;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.Point;
import jalview.math.MatrixI;

import java.io.PrintStream;

/**
 * Performs Principal Component Analysis on given sequences
 */
public class PCA implements Runnable
{
  /*
   * inputs
   */
  final private AlignmentView seqs;

  final private ScoreModelI scoreModel;

  final private SimilarityParamsI similarityParams;

  /*
   * outputs
   */
  private MatrixI pairwiseScores;

  private MatrixI tridiagonal;

  private MatrixI eigenMatrix;

  /**
   * Constructor given the sequences to compute for, the similarity model to
   * use, and a set of parameters for sequence comparison
   * 
   * @param sequences
   * @param sm
   * @param options
   */
  public PCA(AlignmentView sequences, ScoreModelI sm,
          SimilarityParamsI options)
  {
    this.seqs = sequences;
    this.scoreModel = sm;
    this.similarityParams = options;
  }

  /**
   * Returns Eigenvalue
   * 
   * @param i
   *          Index of diagonal within matrix
   * 
   * @return Returns value of diagonal from matrix
   */
  public double getEigenvalue(int i)
  {
    return eigenMatrix.getD()[i];
  }

  /**
   * DOCUMENT ME!
   * 
   * @param l
   *          DOCUMENT ME!
   * @param n
   *          DOCUMENT ME!
   * @param mm
   *          DOCUMENT ME!
   * @param factor
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public Point[] getComponents(int l, int n, int mm, float factor)
  {
    Point[] out = new Point[getHeight()];

    for (int i = 0; i < getHeight(); i++)
    {
      float x = (float) component(i, l) * factor;
      float y = (float) component(i, n) * factor;
      float z = (float) component(i, mm) * factor;
      out[i] = new Point(x, y, z);
    }

    return out;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param n
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public double[] component(int n)
  {
    // n = index of eigenvector
    double[] out = new double[getHeight()];

    for (int i = 0; i < out.length; i++)
    {
      out[i] = component(i, n);
    }

    return out;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param row
   *          DOCUMENT ME!
   * @param n
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  double component(int row, int n)
  {
    double out = 0.0;

    for (int i = 0; i < pairwiseScores.width(); i++)
    {
      out += (pairwiseScores.getValue(row, i) * eigenMatrix.getValue(i, n));
    }

    return out / eigenMatrix.getD()[n];
  }

  /**
   * Answers a formatted text report of the PCA calculation results (matrices
   * and eigenvalues) suitable for display
   * 
   * @return
   */
  public String getDetails()
  {
    StringBuilder sb = new StringBuilder(1024);
    sb.append("PCA calculation using ").append(scoreModel.getName())
            .append(" sequence similarity matrix\n========\n\n");
    PrintStream ps = wrapOutputBuffer(sb);

    /*
     * pairwise similarity scores
     */
    sb.append(" --- OrigT * Orig ---- \n");
    pairwiseScores.print(ps, "%8.2f");

    /*
     * tridiagonal matrix, with D and E vectors
     */
    sb.append(" ---Tridiag transform matrix ---\n");
    sb.append(" --- D vector ---\n");
    tridiagonal.printD(ps, "%15.4e");
    ps.println();
    sb.append("--- E vector ---\n");
    tridiagonal.printE(ps, "%15.4e");
    ps.println();

    /*
     * eigenvalues matrix, with D vector
     */
    sb.append(" --- New diagonalization matrix ---\n");
    eigenMatrix.print(ps, "%8.2f");
    sb.append(" --- Eigenvalues ---\n");
    eigenMatrix.printD(ps, "%15.4e");
    ps.println();

    return sb.toString();
  }

  /**
   * Performs the PCA calculation
   */
  @Override
  public void run()
  {
    try
    {
      /*
       * sequence pairwise similarity scores
       */
      pairwiseScores = scoreModel.findSimilarities(seqs, similarityParams);

      /*
       * tridiagonal matrix
       */
      tridiagonal = pairwiseScores.copy();
      tridiagonal.tred();

      /*
       * the diagonalization matrix
       */
      eigenMatrix = tridiagonal.copy();
      eigenMatrix.tqli();
    } catch (Exception q)
    {
      Console.error("Error computing PCA:  " + q.getMessage());
      q.printStackTrace();
    }
  }

  /**
   * Returns a PrintStream that wraps (appends its output to) the given
   * StringBuilder
   * 
   * @param sb
   * @return
   */
  protected PrintStream wrapOutputBuffer(StringBuilder sb)
  {
    PrintStream ps = new PrintStream(System.out)
    {
      @Override
      public void print(String x)
      {
        sb.append(x);
      }

      @Override
      public void println()
      {
        sb.append("\n");
      }
    };
    return ps;
  }

  /**
   * Answers the N dimensions of the NxN PCA matrix. This is the number of
   * sequences involved in the pairwise score calculation.
   * 
   * @return
   */
  public int getHeight()
  {
    // TODO can any of seqs[] be null?
    return pairwiseScores.height();// seqs.getSequences().length;
  }

  /**
   * Answers the sequence pairwise similarity scores which were the first step
   * of the PCA calculation
   * 
   * @return
   */
  public MatrixI getPairwiseScores()
  {
    return pairwiseScores;
  }

  public void setPairwiseScores(MatrixI m)
  {
    pairwiseScores = m;
  }

  public MatrixI getEigenmatrix()
  {
    return eigenMatrix;
  }

  public void setEigenmatrix(MatrixI m)
  {
    eigenMatrix = m;
  }

  public MatrixI getTridiagonal()
  {
    return tridiagonal;
  }

  public void setTridiagonal(MatrixI tridiagonal)
  {
    this.tridiagonal = tridiagonal;
  }
}
