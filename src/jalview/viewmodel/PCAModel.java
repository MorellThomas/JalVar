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
package jalview.viewmodel;

import jalview.analysis.PCA;
import jalview.api.RotatableCanvasI;
import jalview.api.analysis.ScoreModelI;
import jalview.api.analysis.SimilarityParamsI;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.Point;
import jalview.datamodel.SequenceI;
import jalview.datamodel.SequencePoint;

import java.util.List;
import java.util.Vector;

public class PCAModel
{
  /*
   * inputs
   */
  private AlignmentView inputData;

  private final SequenceI[] seqs;

  private final SimilarityParamsI similarityParams;

  /*
   * options - score model, nucleotide / protein
   */
  private ScoreModelI scoreModel;

  private boolean nucleotide = false;

  /*
   * outputs
   */
  private PCA pca;

  int top;

  private List<SequencePoint> points;

  /**
   * Constructor given sequence data, score model and score calculation
   * parameter options.
   * 
   * @param seqData
   * @param sqs
   * @param nuc
   * @param modelName
   * @param params
   */
  public PCAModel(AlignmentView seqData, SequenceI[] sqs, boolean nuc,
          ScoreModelI modelName, SimilarityParamsI params)
  {
    inputData = seqData;
    seqs = sqs;
    nucleotide = nuc;
    scoreModel = modelName;
    similarityParams = params;
  }

  /**
   * Performs the PCA calculation (in the same thread) and extracts result data
   * needed for visualisation by PCAPanel
   */
  public void calculate()
  {
    pca = new PCA(inputData, scoreModel, similarityParams);
    pca.run(); // executes in same thread, wait for completion

    // Now find the component coordinates
    int ii = 0;

    while ((ii < seqs.length) && (seqs[ii] != null))
    {
      ii++;
    }

    int height = pca.getHeight();
    // top = pca.getM().height() - 1;
    top = height - 1;

    points = new Vector<>();
    Point[] scores = pca.getComponents(top - 1, top - 2, top - 3, 100);

    for (int i = 0; i < height; i++)
    {
      SequencePoint sp = new SequencePoint(seqs[i], scores[i]);
      points.add(sp);
    }
  }

  public void updateRc(RotatableCanvasI rc)
  {
    rc.setPoints(points, pca.getHeight());
  }

  public boolean isNucleotide()
  {
    return nucleotide;
  }

  public void setNucleotide(boolean nucleotide)
  {
    this.nucleotide = nucleotide;
  }

  /**
   * Answers the index of the principal dimension of the PCA
   * 
   * @return
   */
  public int getTop()
  {
    return top;
  }

  public void setTop(int t)
  {
    top = t;
  }

  /**
   * Updates the 3D coordinates for the list of points to the given dimensions.
   * Principal dimension is getTop(). Next greatest eigenvector is getTop()-1.
   * Note - pca.getComponents starts counting the spectrum from rank-2 to zero,
   * rather than rank-1, so getComponents(dimN ...) == updateRcView(dimN+1 ..)
   * 
   * @param dim1
   * @param dim2
   * @param dim3
   */
  public void updateRcView(int dim1, int dim2, int dim3)
  {
    // note: actual indices for components are dim1-1, etc (patch for JAL-1123)
    Point[] scores = pca.getComponents(dim1 - 1, dim2 - 1, dim3 - 1, 100);

    for (int i = 0; i < pca.getHeight(); i++)
    {
      points.get(i).coord = scores[i];
    }
  }

  public String getDetails()
  {
    return pca.getDetails();
  }

  public AlignmentView getInputData()
  {
    return inputData;
  }

  public void setInputData(AlignmentView data)
  {
    inputData = data;
  }

  public String getPointsasCsv(boolean transformed, int xdim, int ydim,
          int zdim)
  {
    StringBuffer csv = new StringBuffer();
    csv.append("\"Sequence\"");
    if (transformed)
    {
      csv.append(",");
      csv.append(xdim);
      csv.append(",");
      csv.append(ydim);
      csv.append(",");
      csv.append(zdim);
    }
    else
    {
      for (int d = 1, dmax = pca.component(1).length; d <= dmax; d++)
      {
        csv.append("," + d);
      }
    }
    csv.append("\n");
    for (int s = 0; s < seqs.length; s++)
    {
      csv.append("\"" + seqs[s].getName() + "\"");
      double fl[];
      if (!transformed)
      {
        // output pca in correct order
        fl = pca.component(s);
        for (int d = fl.length - 1; d >= 0; d--)
        {
          csv.append(",");
          csv.append(fl[d]);
        }
      }
      else
      {
        Point p = points.get(s).coord;
        csv.append(",").append(p.x);
        csv.append(",").append(p.y);
        csv.append(",").append(p.z);
      }
      csv.append("\n");
    }
    return csv.toString();
  }

  public String getScoreModelName()
  {
    return scoreModel == null ? "" : scoreModel.getName();
  }

  public void setScoreModel(ScoreModelI sm)
  {
    this.scoreModel = sm;
  }

  /**
   * Answers the parameters configured for pairwise similarity calculations
   * 
   * @return
   */
  public SimilarityParamsI getSimilarityParameters()
  {
    return similarityParams;
  }

  public List<SequencePoint> getSequencePoints()
  {
    return points;
  }

  public void setSequencePoints(List<SequencePoint> sp)
  {
    points = sp;
  }

  /**
   * Answers the object holding the values of the computed PCA
   * 
   * @return
   */
  public PCA getPcaData()
  {
    return pca;
  }

  public void setPCA(PCA data)
  {
    pca = data;
  }
}
