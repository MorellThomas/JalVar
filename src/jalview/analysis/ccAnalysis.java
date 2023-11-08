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

/*
* Copyright 2018-2022 Kathy Su, Kay Diederichs
* 
* This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>. 
*/

/**
* Ported from https://doi.org/10.1107/S2059798317000699 by
* @AUTHOR MorellThomas
*/

package jalview.analysis;

import jalview.bin.Console;
import jalview.math.MatrixI;
import jalview.math.Matrix;
import jalview.math.MiscMath;

import java.lang.Math;
import java.lang.System;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

/**
 * A class to model rectangular matrices of double values and operations on them
 */
public class ccAnalysis 
{
  private byte dim = 0;		//dimensions

  private MatrixI scoresOld;	//input scores

  public ccAnalysis(MatrixI scores, byte dim)
  {
    // round matrix to .4f to be same as in pasimap
    for (int i = 0; i < scores.height(); i++)
    {
      for (int j = 0; j < scores.width(); j++)
      {
	if (!Double.isNaN(scores.getValue(i,j)))
	{
	  scores.setValue(i, j, (double) Math.round(scores.getValue(i,j) * (int) 10000) / 10000);
	}
      }
    }
    this.scoresOld = scores;
    this.dim = dim;
  }

  /** 
  * Initialise a distrust-score for each hypothesis (h) of hSigns
  * distrust = conHypNum - proHypNum
  *
  * @param hSigns ~ hypothesis signs (+/-) for each sequence
  * @param scores ~ input score matrix
  *
  * @return distrustScores
  */
  private int[] initialiseDistrusts(byte[] hSigns, MatrixI scores)
  {
    int[] distrustScores = new int[scores.width()];
    
    // loop over symmetric matrix
    for (int i = 0; i < scores.width(); i++)
    {
      byte hASign = hSigns[i];
      int conHypNum = 0;
      int proHypNum = 0;

      for (int j = 0; j < scores.width(); j++)
      {
	double cell = scores.getRow(i)[j];	// value at [i][j] in scores
	byte hBSign = hSigns[j];
	if (!Double.isNaN(cell))
	{
	  byte cellSign = (byte) Math.signum(cell);	//check if sign of matrix value fits hyptohesis
	  if (cellSign == hASign * hBSign)
	  {
	    proHypNum++;
	  } else {
	    conHypNum++;
	  }
	}
      }
      distrustScores[i] = conHypNum - proHypNum;	//create distrust score for each sequence
    }
    return distrustScores;
  }

  /**
  * Optemise hypothesis concerning the sign of the hypothetical value for each hSigns by interpreting the pairwise correlation coefficients as scalar products
  *
  * @param hSigns ~ hypothesis signs (+/-)
  * @param distrustScores
  * @param scores ~ input score matrix
  *
  * @return hSigns
  */
  private byte[] optimiseHypothesis(byte[] hSigns, int[] distrustScores, MatrixI scores)
  {
    // get maximum distrust score
    int[] maxes = MiscMath.findMax(distrustScores);
    int maxDistrustIndex = maxes[0];
    int maxDistrust = maxes[1];

    // if hypothesis is not optimal yet
    if (maxDistrust > 0)
    {
      //toggle sign for hI with maximum distrust
      hSigns[maxDistrustIndex] *= -1;
      // update distrust at same position
      distrustScores[maxDistrustIndex] *= -1;

      // also update distrust scores for all hI that were not changed
      byte hASign = hSigns[maxDistrustIndex];
      for (int NOTmaxDistrustIndex = 0; NOTmaxDistrustIndex < distrustScores.length; NOTmaxDistrustIndex++)
      {
	if (NOTmaxDistrustIndex != maxDistrustIndex)
	{
	  byte hBSign = hSigns[NOTmaxDistrustIndex];
	  double cell = scores.getValue(maxDistrustIndex, NOTmaxDistrustIndex);

	  // distrust only changed if not NaN
	  if (!Double.isNaN(cell))
	  {
	    byte cellSign = (byte) Math.signum(cell);
	    // if sign of cell matches hypothesis decrease distrust by 2 because 1 more value supporting and 1 less contradicting
	    // else increase by 2
	    if (cellSign == hASign * hBSign)
	    {
	      distrustScores[NOTmaxDistrustIndex] -= 2;
	    } else {
	      distrustScores[NOTmaxDistrustIndex] += 2;
	    }
	  }
	}
      }
      //further optimisation necessary
      return optimiseHypothesis(hSigns, distrustScores, scores);

    } else {
      return hSigns;
    }
  }

  /** 
  * takes the a symmetric MatrixI as input scores which may contain Double.NaN 
  * approximate the missing values using hypothesis optimisation 
  *
  * runs analysis
  *
  * @param scores ~ score matrix
  *
  * @return
  */
  public MatrixI run () throws Exception
  {
    //initialse eigenMatrix and repMatrix
    MatrixI eigenMatrix = scoresOld.copy();
    MatrixI repMatrix = scoresOld.copy();
    try
    {
    /*
    * Calculate correction factor for 2nd and higher eigenvalue(s).
    * This correction is NOT needed for the 1st eigenvalue, because the
    * unknown (=NaN) values of the matrix are approximated by presuming
    * 1-dimensional vectors as the basis of the matrix interpretation as dot
    * products.
    */
        
    System.out.println("Input correlation matrix:");
    eigenMatrix.print(System.out, "%1.4f ");

    int matrixWidth = eigenMatrix.width(); // square matrix, so width == height
    int matrixElementsTotal = (int) Math.pow(matrixWidth, 2);	//total number of elemts

    float correctionFactor = (float) (matrixElementsTotal - eigenMatrix.countNaN()) / (float) matrixElementsTotal;
    
    /*
    * Calculate hypothetical value (1-dimensional vector) h_i for each
    * dataset by interpreting the given correlation coefficients as scalar
    * products.
    */

    /*
    * Memory for current hypothesis concerning sign of each h_i.
    * List of signs for all h_i in the encoding:
      * *  1: positive
      * *  0: zero
      * * -1: negative
    * Initial hypothesis: all signs are positive.
    */
    byte[] hSigns = new byte[matrixWidth];
    Arrays.fill(hSigns, (byte) 1);

    //Estimate signs for each h_i by refining hypothesis on signs.
    hSigns = optimiseHypothesis(hSigns, initialiseDistrusts(hSigns, eigenMatrix), eigenMatrix);


    //Estimate absolute values for each h_i by determining sqrt of mean of
    //non-NaN absolute values for every row.
    double[] hAbs = MiscMath.sqrt(eigenMatrix.absolute().meanRow());

    //Combine estimated signs with absolute values in obtain total value for
    //each h_i.
    double[] hValues = MiscMath.elementwiseMultiply(hSigns, hAbs);

    /*Complement symmetric matrix by using the scalar products of estimated
    *values of h_i to replace NaN-cells.
    *Matrix positions that have estimated values
    *(only for diagonal and upper off-diagonal values, due to the symmetry
    *the positions of the lower-diagonal values can be inferred).
    List of tuples (row_idx, column_idx).*/

    ArrayList<int[]> estimatedPositions = new ArrayList<int[]>();

    // for off-diagonal cells
    for (int rowIndex = 0; rowIndex < matrixWidth - 1; rowIndex++)
    {
      for (int columnIndex = rowIndex + 1; columnIndex < matrixWidth; columnIndex++)
      {
	double cell = eigenMatrix.getValue(rowIndex, columnIndex);
	if (Double.isNaN(cell))
	{
	  //calculate scalar product as new cell value
	  cell = hValues[rowIndex] * hValues[columnIndex];
          //fill in new value in cell and symmetric partner
	  eigenMatrix.setValue(rowIndex, columnIndex, cell);
	  eigenMatrix.setValue(columnIndex, rowIndex, cell);
	  //save positions of estimated values
	  estimatedPositions.add(new int[]{rowIndex, columnIndex});
	}
      }
    }

    // for diagonal cells
    for (int diagonalIndex = 0; diagonalIndex < matrixWidth; diagonalIndex++)
      {
        double cell = Math.pow(hValues[diagonalIndex], 2);
	eigenMatrix.setValue(diagonalIndex, diagonalIndex, cell);
	estimatedPositions.add(new int[]{diagonalIndex, diagonalIndex});
      }

    /*Refine total values of each h_i:
    *Initialise h_values of the hypothetical non-existant previous iteration
    *with the correct format but with impossible values.
     Needed for exit condition of otherwise endless loop.*/
    System.out.print("initial values: [ ");
    for (double h : hValues)
    {
      System.out.print(String.format("%1.4f, ", h));
    }
    System.out.println(" ]");


    double[] hValuesOld = new double[matrixWidth];

    int iterationCount = 0;

    // repeat unitl values of h do not significantly change anymore
    while (true)
    {
      for (int hIndex = 0; hIndex < matrixWidth; hIndex++)
      {
	double newH = Arrays.stream(MiscMath.elementwiseMultiply(hValues, eigenMatrix.getRow(hIndex))).sum() / Arrays.stream(MiscMath.elementwiseMultiply(hValues, hValues)).sum();
	hValues[hIndex] = newH;
      }

      System.out.print(String.format("iteration %d: [ ", iterationCount));
      for (double h : hValues)
      {
	System.out.print(String.format("%1.4f, ", h));
      }
      System.out.println(" ]");

      //update values of estimated positions
      for (int[] pair : estimatedPositions)	// pair ~ row, col
      {
        double newVal = hValues[pair[0]] * hValues[pair[1]];
	eigenMatrix.setValue(pair[0], pair[1], newVal);
	eigenMatrix.setValue(pair[1], pair[0], newVal);
      }

      iterationCount++;

      //exit loop as soon as new values are similar to the last iteration
      if (MiscMath.allClose(hValues, hValuesOld, 0d, 1e-05d, false))
      {
        break;
      }

      //save hValues for comparison in the next iteration
      System.arraycopy(hValues, 0, hValuesOld, 0, hValues.length);
    }

    //-----------------------------
    //Use complemented symmetric matrix to calculate final representative
    //vectors.

    //Eigendecomposition.
    eigenMatrix.tred();
    eigenMatrix.tqli();

    System.out.println("eigenmatrix");
    eigenMatrix.print(System.out, "%8.2f");
    System.out.println();
    System.out.println("uncorrected eigenvalues");
    eigenMatrix.printD(System.out, "%2.4f ");
    System.out.println();

    double[] eigenVals = eigenMatrix.getD();

    TreeMap<Double, Integer> eigenPairs = new TreeMap<>(Comparator.reverseOrder());
    for (int i = 0; i < eigenVals.length; i++)
    {
      eigenPairs.put(eigenVals[i], i);
    }

    // matrix of representative eigenvectors (each row is a vector)
    double[][] _repMatrix = new double[eigenVals.length][dim];
    double[][] _oldMatrix = new double[eigenVals.length][dim];
    double[] correctedEigenValues = new double[dim];	

    int l = 0;
    for (Entry<Double, Integer> pair : eigenPairs.entrySet())
    {
      double eigenValue = pair.getKey();
      int column = pair.getValue();
      double[] eigenVector = eigenMatrix.getColumn(column);
      //for 2nd and higher eigenvalues
      if (l >= 1)
      {
        eigenValue /= correctionFactor;
      }
      correctedEigenValues[l] = eigenValue;
      for (int j = 0; j < eigenVector.length; j++)
      {
	_repMatrix[j][l] = (eigenValue < 0) ? 0.0 : - Math.sqrt(eigenValue) * eigenVector[j];
	double tmpOldScore = scoresOld.getColumn(column)[j];
	_oldMatrix[j][dim - l - 1] = (Double.isNaN(tmpOldScore)) ? 0.0 : tmpOldScore;
      }
      l++;
      if (l >= dim)
      {
	break;
      }
    }

    System.out.println("correctedEigenValues");
    MiscMath.print(correctedEigenValues, "%2.4f ");

    repMatrix = new Matrix(_repMatrix);
    repMatrix.setD(correctedEigenValues);
    MatrixI oldMatrix = new Matrix(_oldMatrix);

    MatrixI dotMatrix = repMatrix.postMultiply(repMatrix.transpose());
    
    double rmsd = scoresOld.rmsd(dotMatrix);

    System.out.println("iteration, rmsd, maxDiff, rmsdDiff");
    System.out.println(String.format("0, %8.5f, -, -", rmsd));
    // Refine representative vectors by minimising sum-of-squared deviates between dotMatrix and original  score matrix
    for (int iteration = 1; iteration < 21; iteration++)	// arbitrarily set to 20
    {
      MatrixI repMatrixOLD = repMatrix.copy();
      MatrixI dotMatrixOLD = dotMatrix.copy();

      // for all rows/hA in the original matrix
      for (int hAIndex = 0; hAIndex < oldMatrix.height(); hAIndex++)
      {
	double[] row = oldMatrix.getRow(hAIndex);
	double[] hA = repMatrix.getRow(hAIndex);
	hAIndex = hAIndex;
	//find least-squares-solution fo rdifferences between original scores and representative vectors
	double[] hAlsm = leastSquaresOptimisation(repMatrix, scoresOld, hAIndex);
        // update repMatrix with new hAlsm
	for (int j = 0; j < repMatrix.width(); j++)
	{
	  repMatrix.setValue(hAIndex, j, hAlsm[j]);
	}
      }
      
      // dot product of representative vecotrs yields a matrix with values approximating the correlation matrix
      dotMatrix = repMatrix.postMultiply(repMatrix.transpose());
      // calculate rmsd between approximation and correlation matrix
      rmsd = scoresOld.rmsd(dotMatrix);

      // calculate maximum change of representative vectors of current iteration
      MatrixI diff = repMatrix.subtract(repMatrixOLD).absolute();
      double maxDiff = 0.0;
      for (int i = 0; i < diff.height(); i++)
      {
	for (int j = 0; j < diff.width(); j++)
	{
	  maxDiff = (diff.getValue(i, j) > maxDiff) ? diff.getValue(i, j) : maxDiff;
	}
      }

      // calculate rmsd between current and previous estimation
      double rmsdDiff = dotMatrix.rmsd(dotMatrixOLD);

      System.out.println(String.format("%d, %8.5f, %8.5f, %8.5f", iteration, rmsd, maxDiff, rmsdDiff));

      if (!(Math.abs(maxDiff) > 1e-06))
      {
	repMatrix = repMatrixOLD.copy();
	break;
      }
    }
    

    } catch (Exception q)
    {
      Console.error("Error computing cc_analysis:  " + q.getMessage());
      q.printStackTrace();
    }
    System.out.println("final coordinates:");
    repMatrix.print(System.out, "%1.8f ");
    return repMatrix;
  }

  /**
  * Create equations system using information on originally known
  * pairwise correlation coefficients (parsed from infile) and the
  * representative result vectors
  *
  * Each equation has the format:
  * hA * hA - pairwiseCC = 0
  * with:
  * hA: unknown variable
  * hB: known representative vector
  * pairwiseCC: known pairwise correlation coefficien
  * 
  * The resulting equations system is overdetermined, if there are more
  * equations than unknown elements
  *
  * @param x ~ unknown n-dimensional column-vector
  * (needed for generating equations system, NOT to be specified by user).
  * @param hAIndex ~ index of currently optimised representative result vector.
  * @param h ~ matrix with row-wise listing of representative result vectors.
  * @param originalRow ~ matrix-row of originally parsed pairwise correlation coefficients.
  *
  * @return
  */
  private double[] originalToEquasionSystem(double[] hA, MatrixI repMatrix, MatrixI scoresOld, int hAIndex)
  {
    double[] originalRow = scoresOld.getRow(hAIndex);
    int nans = MiscMath.countNaN(originalRow);
    double[] result = new double[originalRow.length - nans];

    //for all pairwiseCC in originalRow
    int resultIndex = 0;
    for (int hBIndex = 0; hBIndex < originalRow.length; hBIndex++)
    {
      double pairwiseCC = originalRow[hBIndex];
      // if not NaN -> create new equation and add it to the system
      if (!Double.isNaN(pairwiseCC))
      {
        double[] hB = repMatrix.getRow(hBIndex);
        result[resultIndex++] = MiscMath.sum(MiscMath.elementwiseMultiply(hA, hB)) - pairwiseCC;
      } else {
      }
    }
    return result;
  }

  /**
  * returns the jacobian matrix
  * @param repMatrix ~ matrix of representative vectors
  * @param hAIndex ~ current row index
  *
  * @return
  */
  private MatrixI approximateDerivative(MatrixI repMatrix, MatrixI scoresOld, int hAIndex)
  {
    //hA = x0
    double[] hA = repMatrix.getRow(hAIndex);
    double[] f0 = originalToEquasionSystem(hA, repMatrix, scoresOld, hAIndex);
    double[] signX0 = new double[hA.length];
    double[] xAbs = new double[hA.length];
    for (int i = 0; i < hA.length; i++)
    {
      signX0[i] = (hA[i] >= 0) ? 1 : -1;
      xAbs[i] = (Math.abs(hA[i]) >= 1.0) ? Math.abs(hA[i]) : 1.0;
      }
    double rstep = Math.pow(Math.ulp(1.0), 0.5);

    double[] h = new double [hA.length];
    for (int i = 0; i < hA.length; i++)
    {
      h[i] = rstep * signX0[i] * xAbs[i];
    }
      
    int m = f0.length;
    int n = hA.length;
    double[][] jTransposed = new double[n][m];
    for (int i = 0; i < h.length; i++)
    {
      double[] x = new double[h.length];
      System.arraycopy(hA, 0, x, 0, h.length);
      x[i] += h[i];
      double dx = x[i] - hA[i];
      double[] df = originalToEquasionSystem(x, repMatrix, scoresOld, hAIndex);
      for (int j = 0; j < df.length; j++)
      {
	df[j] -= f0[j];
	jTransposed[i][j] = df[j] / dx;
      }
    }
    MatrixI J = new Matrix(jTransposed).transpose();
    return J;
  }

  /**
  * norm of regularized (by alpha) least-squares solution minus Delta
  * @param alpha
  * @param suf
  * @param s
  * @param Delta
  *
  * @return
  */
  private double[] phiAndDerivative(double alpha, double[] suf, double[] s, double Delta)
  {
    double[] denom = MiscMath.elementwiseAdd(MiscMath.elementwiseMultiply(s, s), alpha);
    double pNorm = MiscMath.norm(MiscMath.elementwiseDivide(suf, denom));
    double phi = pNorm - Delta;
    // - sum ( suf**2 / denom**3) / pNorm
    double phiPrime = - MiscMath.sum(MiscMath.elementwiseDivide(MiscMath.elementwiseMultiply(suf, suf), MiscMath.elementwiseMultiply(MiscMath.elementwiseMultiply(denom, denom), denom))) / pNorm;
    return new double[]{phi, phiPrime};
  }

  /**
  * class holding the result of solveLsqTrustRegion
  */
  private class TrustRegion
  {
    private double[] step;
    private double alpha;
    private int iteration;

    public TrustRegion(double[] step, double alpha, int iteration)
    {
      this.step = step;
      this.alpha = alpha;
      this.iteration = iteration;
    }

    public double[] getStep()
    {
      return this.step;
    }

    public double getAlpha()
    {
      return this.alpha;
    }
  
    public int getIteration()
    {
      return this.iteration;
    }
  }

  /**
  * solve a trust-region problem arising in least-squares optimisation
  * @param n ~ number of variables
  * @param m ~ number of residuals
  * @param uf
  * @param s ~ singular values of J
  * @param V ~ transpose of VT
  * @param Delta ~ radius of a trust region
  * @param alpha ~ initial guess for alpha
  *
  * @return
  */
  private TrustRegion solveLsqTrustRegion(int n, int m, double[] uf, double[] s, MatrixI V, double Delta, double alpha)
  {
    double[] suf = MiscMath.elementwiseMultiply(s, uf);

    //check if J has full rank and tr Gauss-Newton step
    boolean fullRank = false;
    if (m >= n)
    {
      double threshold = s[0] * Math.ulp(1.0) * m;
      fullRank = s[s.length - 1] > threshold;
    }
    if (fullRank)
    {
      double[] p = MiscMath.elementwiseMultiply(V.sumProduct(MiscMath.elementwiseDivide(uf, s)), -1);
      if (MiscMath.norm(p) <= Delta)
      {
        TrustRegion result = new TrustRegion(p, 0.0, 0);
        return result;
      }
    }

    double alphaUpper = MiscMath.norm(suf) / Delta;
    double alphaLower = 0.0;
    if (fullRank)
    {
      double[] phiAndPrime = phiAndDerivative(0.0, suf, s, Delta);
      alphaLower = - phiAndPrime[0] / phiAndPrime[1];
    }

    alpha = (!fullRank && alpha == 0.0) ? alpha = Math.max(0.001 * alphaUpper, Math.pow(alphaLower * alphaUpper, 0.5)) : alpha;

    int iteration = 0;
    while (iteration < 10)	// 10 is default max_iter
    {
      alpha = (alpha < alphaLower || alpha > alphaUpper) ? alpha = Math.max(0.001 * alphaUpper, Math.pow(alphaLower * alphaUpper, 0.5)) : alpha;
      double[] phiAndPrime = phiAndDerivative(alpha, suf, s, Delta);
      double phi = phiAndPrime[0];
      double phiPrime = phiAndPrime[1];

      alphaUpper = (phi < 0) ? alpha : alphaUpper;
      double ratio = phi / phiPrime;
      alphaLower = Math.max(alphaLower, alpha - ratio);
      alpha -= (phi + Delta) * ratio / Delta;

      if (Math.abs(phi) < 0.01 * Delta)	// default rtol set to 0.01
      {
	break;
      }
      iteration++;
    }

    // p = - V.dot( suf / (s**2 + alpha))
    double[] tmp = MiscMath.elementwiseDivide(suf, MiscMath.elementwiseAdd(MiscMath.elementwiseMultiply(s, s), alpha));
    double[] p = MiscMath.elementwiseMultiply(V.sumProduct(tmp), -1);

    // Make the norm of p equal to Delta, p is changed only slightly during this.
    // It is done to prevent p lie outside of the trust region
    p = MiscMath.elementwiseMultiply(p, Delta / MiscMath.norm(p));

    TrustRegion result = new TrustRegion(p, alpha, iteration + 1);
    return result;
  }

  /**
  * compute values of a quadratic function arising in least squares
  * function: 0.5 * s.T * (J.T * J + diag) * s + g.T * s
  *
  * @param J ~ jacobian matrix
  * @param g ~ gradient
  * @param s ~ steps and rows
  *
  * @return
  */
  private double evaluateQuadratic(MatrixI J, double[] g, double[] s)
  {

    double[] Js = J.sumProduct(s);
    double q = MiscMath.dot(Js, Js);
    double l = MiscMath.dot(s, g);

    return 0.5 * q + l;
  }

  /**
  * update the radius of a trust region based on the cost reduction
  *
  * @param Delta
  * @param actualReduction
  * @param predictedReduction
  * @param stepNorm
  * @param boundHit
  *
  * @return
  */
  private double[] updateTrustRegionRadius(double Delta, double actualReduction, double predictedReduction, double stepNorm, boolean boundHit)
  {
    double ratio = 0;
    if (predictedReduction > 0)
    {
      ratio = actualReduction / predictedReduction;
    } else if (predictedReduction == 0 && actualReduction == 0) {
      ratio = 1;
    } else {
      ratio = 0;
    }

    if (ratio < 0.25)
    {
      Delta = 0.25 * stepNorm;
    } else if (ratio > 0.75 && boundHit) {
      Delta *= 2.0;
    }

    return new double[]{Delta, ratio};
  }

  /**
  * trust region reflective algorithm
  * @param repMatrix ~ Matrix containing representative vectors
  * @param scoresOld ~ Matrix containing initial observations
  * @param index ~ current row index
  * @param J ~ jacobian matrix
  *
  * @return
  */
  private double[] trf(MatrixI repMatrix, MatrixI scoresOld, int index, MatrixI J)
  {
    //hA = x0
    double[] hA = repMatrix.getRow(index);
    double[] f0 = originalToEquasionSystem(hA, repMatrix, scoresOld, index);
    int nfev = 1;
    int m = J.height();
    int n = J.width();
    double cost = 0.5 * MiscMath.dot(f0, f0);
    double[] g = J.transpose().sumProduct(f0);
    double Delta = MiscMath.norm(hA);
    int maxNfev = hA.length * 100;
    double alpha = 0.0;		// "Levenberg-Marquardt" parameter

    double gNorm = 0;
    boolean terminationStatus = false;
    int iteration = 0;

    while (true)
    {
      gNorm = MiscMath.norm(g);
      if (terminationStatus || nfev == maxNfev)
      {
	break;
      }
      SingularValueDecomposition svd = new SingularValueDecomposition(new Array2DRowRealMatrix(J.asArray()));
      MatrixI U = new Matrix(svd.getU().getData());
      double[] s = svd.getSingularValues();	
      MatrixI V = new Matrix(svd.getV().getData()).transpose();
      double[] uf = U.transpose().sumProduct(f0);

      double actualReduction = -1;
      double[] xNew = new double[hA.length];
      double[] fNew = new double[f0.length];
      double costNew = 0;
      double stepHnorm = 0;
      
      while (actualReduction <= 0 && nfev < maxNfev)
      {
        TrustRegion trustRegion = solveLsqTrustRegion(n, m, uf, s, V, Delta, alpha);
	double[] stepH = trustRegion.getStep();	
	alpha = trustRegion.getAlpha();
	int nIterations = trustRegion.getIteration();
        double predictedReduction = - (evaluateQuadratic(J, g, stepH));	

        xNew = MiscMath.elementwiseAdd(hA, stepH);
	fNew = originalToEquasionSystem(xNew, repMatrix, scoresOld, index);
	nfev++;
	
	stepHnorm = MiscMath.norm(stepH);

	if (MiscMath.countNaN(fNew) > 0)
	{
	  Delta = 0.25 * stepHnorm;
	  continue;
	}

	// usual trust-region step quality estimation
	costNew = 0.5 * MiscMath.dot(fNew, fNew); 
	actualReduction = cost - costNew;

	double[] updatedTrustRegion = updateTrustRegionRadius(Delta, actualReduction, predictedReduction, stepHnorm, stepHnorm > (0.95 * Delta));
	double DeltaNew = updatedTrustRegion[0];
	double ratio = updatedTrustRegion[1];

        // default ftol and xtol = 1e-8
        boolean ftolSatisfied = actualReduction < (1e-8 * cost) && ratio > 0.25;
        boolean xtolSatisfied = stepHnorm < (1e-8 * (1e-8 + MiscMath.norm(hA)));
	terminationStatus = ftolSatisfied || xtolSatisfied;
	if (terminationStatus)
	{
	  break;
	}

	alpha *= Delta / DeltaNew;
	Delta = DeltaNew;

      }
      if (actualReduction > 0)
      {
	hA = xNew;
	f0 = fNew;
	cost = costNew;

	J = approximateDerivative(repMatrix, scoresOld, index);

        g = J.transpose().sumProduct(f0);
      } else {
        stepHnorm = 0;
	actualReduction = 0;
      }
      iteration++;
    }

    return hA;
  }

  /**
  * performs the least squares optimisation
  * adapted from https://docs.scipy.org/doc/scipy/reference/generated/scipy.optimize.least_squares.html#scipy.optimize.least_squares
  *
  * @param repMatrix ~ Matrix containing representative vectors
  * @param scoresOld ~ Matrix containing initial observations
  * @param index ~ current row index
  *
  * @return
  */
  private double[] leastSquaresOptimisation(MatrixI repMatrix, MatrixI scoresOld, int index)
  {
    MatrixI J = approximateDerivative(repMatrix, scoresOld, index);
    double[] result = trf(repMatrix, scoresOld, index, J);
    return result;
  }

}
