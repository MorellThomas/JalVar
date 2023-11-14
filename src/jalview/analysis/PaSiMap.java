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

import jalview.analysis.AAFrequency; // not for pasimap
import jalview.api.analysis.ScoreModelI;
import jalview.api.analysis.SimilarityParamsI;
import jalview.bin.Console;
import jalview.datamodel.AlignmentAnnotation; // not for pasimap
import jalview.datamodel.AlignmentI; // not for pasimap
//import jalview.datamodel.AlignmentView;
import jalview.datamodel.Annotation;  // not for pasimap
import jalview.datamodel.Point;
import jalview.datamodel.ProfilesI;
import jalview.datamodel.SequenceI;
import jalview.datamodel.ResidueCount; // not for pasimap
import jalview.gui.CutAndPasteTransfer;
import jalview.gui.Desktop;
import jalview.gui.OOMWarning;
import jalview.gui.PairwiseAlignPanel;
//import jalview.gui.PaSiMapPanel;
import jalview.math.Matrix;
import jalview.math.MatrixI;
import jalview.math.MiscMath;  // not for pasimap
import jalview.util.MessageManager;
import jalview.viewmodel.AlignmentViewport;


import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Performs Principal Component Analysis on given sequences
 * @AUTHOR MorellThomas 
 */
public class PaSiMap implements Runnable
{
  /*
   * inputs
   */
  final private AlignmentViewport seqs;

  final private ScoreModelI scoreModel;

  final private SimilarityParamsI similarityParams;

  final private byte dim = 3;

  /*
   * outputs
   */
  private MatrixI pairwiseScores;

  private MatrixI eigenMatrix;

  /**
   * Constructor given the sequences to compute for, the similarity model to
   * use, and a set of parameters for sequence comparison
   * 
   * @param sequences
   * @param sm
   * @param options
   */
  public PaSiMap(AlignmentViewport sequences, ScoreModelI sm,
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
   * Returns coordinates for each datapoint
   * 
   * @param l
   *          DOCUMENT ME!
   * @param n
   *          DOCUMENT ME!
   * @param mm
   *          DOCUMENT ME!
   * @param factor ~ is 1
   * 
   * @return DOCUMENT ME!
   */
  public Point[] getComponents(int l, int n, int mm, float factor)
  {
    Point[] out = new Point[getHeight()];

    for (int i = 0; i < out.length; i++)
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
    return eigenMatrix.getValue(row, n);
  }

  /**
   * Answers a formatted text report of the PaSiMap calculation results (matrices
   * and eigenvalues) suitable for display
   * 
   * @return
   */
  public String getDetails()
  {
    StringBuilder sb = new StringBuilder(1024);
    sb.append("PaSiMap calculation using ").append(scoreModel.getName())
            .append(" sequence similarity matrix\n========\n\n");
    PrintStream ps = wrapOutputBuffer(sb);

    /*
     * coordinates matrix, with D vector
     */
    sb.append(" --- Pairwise correlation coefficients ---\n");
    pairwiseScores.print(ps, "%8.6f ");
    ps.println();

    sb.append(" --- Eigenvalues ---\n");
    eigenMatrix.printD(ps, "%15.4e");
    ps.println();

    sb.append(" --- Coordinates ---\n");
    eigenMatrix.print(ps, "%8.6f ");
    ps.println();

    return sb.toString();
  }

  /**
   * Performs the PaSiMap calculation
   *
   * creates a new gui/PairwiseAlignPanel with the input sequences (AlignmentViewport)
   * uses analysis/AlignSeq to creatue the pairwise alignments and calculate the AlignmentScores (float for each pair)
   * gets all float[][] scores from the gui/PairwiseAlignPanel
   * checks the connections for each sequence with AlignmentViewport seqs.calculateConnectivity(float[][] scores, int dim) (from analysis/Connectivity) -- throws an Exception if insufficient
   * creates a math/MatrixI pairwiseScores of the float[][] scores
   * copys the scores and fills the diagonal to create a symmetric matrix using math/Matrix.fillDiagonal()
   * performs the analysis/ccAnalysis with the symmetric matrix
   * gets the eigenmatrix and the eigenvalues using math/Matrix.tqli()
   */
  @Override
  public void run()
  {
    try
    {
      PairwiseAlignPanel alignment = new PairwiseAlignPanel(seqs, true);
      float[][] scores = alignment.getAlignmentScores();	//bigger index first -- eg scores[14][13]

      Hashtable<SequenceI, Integer> connectivity = seqs.calculateConnectivity(scores, dim);

      pairwiseScores = new Matrix(scores);
      pairwiseScores.fillDiagonal();

      eigenMatrix = pairwiseScores.copy();

      ccAnalysis cc = new ccAnalysis(pairwiseScores, dim);
      eigenMatrix = cc.run();
      
      
      /**********
       * everything below here until the catch block does not belong to PaSiMap 
       * I just use it to run code on the alignment or whatever
       */
      
      AlignmentAnnotation consensus = seqs.getAlignmentConsensusAnnotation();
      System.out.println(consensus.toString());
      //for (Annotation an : consensus.annotations)
      //{
        //System.out.println(an.toString());
      //}
      
      AlignmentI al = seqs.getAlignment();
      int width = al.getWidth();
      SequenceI[] aseqs = al.getSequencesArray();
      ProfilesI hconsensus = AAFrequency.calculate(aseqs, width, 0, width, true);
      
      ResidueCount[] residueCountses = new ResidueCount[hconsensus.getEndColumn() + 1];
      for (int i = 0; i < hconsensus.getEndColumn() + 1; i++)
      {
        residueCountses[i] = hconsensus.get(i).getCounts();
      }
      
      char[] AaList = new char[]{'A', 'R', 'N', 'D', 'C', 'Q', 'E', 'G', 'H', 'I', 'L', 'K', 'M', 'F', 'P', 'S', 'T', 'W', 'Y', 'V', '-'};
      StringBuffer csv = new StringBuffer();
      csv.append("\"Position\"");
      for (char Aa : AaList)
      {
        csv.append("," + Aa);
      }
      csv.append("\n");
      int nSeqs = aseqs.length;
      int position = 1;
      for (ResidueCount rc : residueCountses)
      {
        csv.append(Integer.toString(position));
        
        float[] percentages = new float[AaList.length];

        ResidueCount.SymbolCounts symbolCounts = rc.getSymbolCounts();
        float gapPC = 1f;
        for (int i = 0; i < symbolCounts.symbols.length; i++)
        {
          char AA = symbolCounts.symbols[i];  
          int n = symbolCounts.values[i];     // number of times the AA appears at this position
          float pc = (float) n / (float) nSeqs;               // % of AA appearance at this position
          pc = MiscMath.round(pc, 2);
          gapPC -= pc;
          
          int indexOfAa = new String(AaList).indexOf(AA);
          percentages[indexOfAa] = pc;
        }
        if (gapPC > 0)
        {
          gapPC = MiscMath.round(gapPC, 2);
          percentages[percentages.length - 1] = gapPC;
        }
        for (float pc : percentages)
        {
          csv.append(",").append(pc);
        }
        csv.append("\n");
        position++;
      }
      
      String CsvString = csv.toString();
      
      CutAndPasteTransfer cap = new CutAndPasteTransfer();
      try
      {
        cap.setText(CsvString);
        Desktop.addInternalFrame(cap, MessageManager
                .formatMessage("label.points_for_params", "Outputting Natural Frequencies"), 500, 500);
      } catch (OutOfMemoryError oom)
      {
        new OOMWarning("exporting Natural Frequencies", oom);
        cap.dispose();
      }

    } catch (Exception q)
    {
      Console.error("Error computing PaSiMap:  " + q.getMessage());
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
   * Answers the N dimensions of the NxM PaSiMap matrix. This is the number of
   * sequences involved in the pairwise score calculation.
   * 
   * @return
   */
  public int getHeight()
  {
    // TODO can any of seqs[] be null?
    return eigenMatrix.height();// seqs.getSequences().length;
  }

  /**
   * Answers the M dimensions of the NxM PaSiMap matrix. This is the number of
   * sequences involved in the pairwise score calculation.
   * 
   * @return
   */
  public int getWidth()
  {
    // TODO can any of seqs[] be null?
    return eigenMatrix.width();// seqs.getSequences().length;
  }

  /**
   * Answers the sequence pairwise similarity scores which were the first step
   * of the PaSiMap calculation
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
}
