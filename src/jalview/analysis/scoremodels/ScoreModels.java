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
package jalview.analysis.scoremodels;

import jalview.api.AlignmentViewPanel;
import jalview.api.analysis.ScoreModelI;
import jalview.io.DataSourceType;
import jalview.io.FileParse;
import jalview.io.ScoreMatrixFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A class that can register and serve instances of ScoreModelI
 */
public class ScoreModels
{
  private final ScoreMatrix BLOSUM62;

  private final ScoreMatrix PAM250;

  private final ScoreMatrix DNA;

  private static ScoreModels instance;

  private Map<String, ScoreModelI> models;

  /**
   * Answers the singleton instance of this class, with lazy initialisation
   * (built-in score models are loaded on the first call to this method)
   * 
   * @return
   */
  public static ScoreModels getInstance()
  {
    if (instance == null)
    {
      instance = new ScoreModels();
    }
    return instance;
  }

  /**
   * Private constructor to enforce use of singleton. Registers Jalview's
   * "built-in" score models:
   * <ul>
   * <li>BLOSUM62</li>
   * <li>PAM250</li>
   * <li>PID</li>
   * <li>DNA</li>
   * <li>Sequence Feature Similarity</li>
   * </ul>
   */
  private ScoreModels()
  {
    /*
     * using LinkedHashMap keeps models ordered as added
     */
    models = new LinkedHashMap<>();
    BLOSUM62 = loadScoreMatrix("scoreModel/blosum62.scm");
    PAM250 = loadScoreMatrix("scoreModel/pam250.scm");
    DNA = loadScoreMatrix("scoreModel/dna.scm");
    registerScoreModel(new PIDModel());
    registerScoreModel(new FeatureDistanceModel());
  }

  /**
   * Tries to load a score matrix from the given resource file, and if
   * successful, registers it.
   * 
   * @param string
   * @return
   */
  ScoreMatrix loadScoreMatrix(String resourcePath)
  {
    try
    {
      /*
       * delegate parsing to ScoreMatrixFile
       */
      FileParse fp = new FileParse(resourcePath,
              DataSourceType.CLASSLOADER);
      ScoreMatrix sm = new ScoreMatrixFile(fp).parseMatrix();
      registerScoreModel(sm);
      return sm;
    } catch (IOException e)
    {
      System.err.println(
              "Error reading " + resourcePath + ": " + e.getMessage());
    }
    return null;
  }

  /**
   * Answers an iterable set of the registered score models. Currently these are
   * returned in the order in which they were registered.
   * 
   * @return
   */
  public Iterable<ScoreModelI> getModels()
  {
    return models.values();
  }

  /**
   * Returns an instance of a score model for the given name. If the model is of
   * 'view dependent' type (e.g. feature similarity), instantiates a new
   * instance configured for the given view. Otherwise returns a cached instance
   * of the score model.
   * 
   * @param name
   * @param avp
   * @return
   */
  public ScoreModelI getScoreModel(String name, AlignmentViewPanel avp)
  {
    ScoreModelI model = models.get(name);
    return model == null ? null : model.getInstance(avp);
  }

  public void registerScoreModel(ScoreModelI sm)
  {
    ScoreModelI sm2 = models.get(sm.getName());
    if (sm2 != null)
    {
      System.err.println("Warning: replacing score model " + sm2.getName());
    }
    models.put(sm.getName(), sm);
  }

  /**
   * Resets to just the built-in score models
   */
  public void reset()
  {
    instance = new ScoreModels();
  }

  /**
   * Returns the default peptide or nucleotide score model, currently BLOSUM62
   * or DNA
   * 
   * @param forPeptide
   * @return
   */
  public ScoreMatrix getDefaultModel(boolean forPeptide)
  {
    return forPeptide ? BLOSUM62 : DNA;
  }

  public ScoreMatrix getBlosum62()
  {
    return BLOSUM62;
  }

  public ScoreMatrix getPam250()
  {
    return PAM250;
  }
}
