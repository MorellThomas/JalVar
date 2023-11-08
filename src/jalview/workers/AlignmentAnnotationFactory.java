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
package jalview.workers;

import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.bin.Jalview;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Annotation;
import jalview.gui.AlignFrame;

import java.awt.Color;

/**
 * Factory class with methods which allow clients (including external scripts
 * such as Groovy) to 'register and forget' an alignment annotation calculator.
 * <br>
 * Currently supports two flavours of calculator:
 * <ul>
 * <li>a simple 'feature counter' which counts any desired score derivable from
 * residue value and any sequence features at each position of the
 * alignment</li>
 * <li>a 'general purpose' calculator which computes one or more complete
 * AlignmentAnnotation objects</li>
 * </ul>
 */
public class AlignmentAnnotationFactory
{
  /**
   * Constructs and registers a new alignment annotation worker
   * 
   * @param counter
   *          provider of feature counts per alignment position
   */
  public static void newCalculator(FeatureSetCounterI counter)
  {
    AlignmentViewPanel currentAlignFrame = Jalview
            .getCurrentAlignFrame().alignPanel;
    if (currentAlignFrame == null)
    {
      System.err.println(
              "Can't register calculator as no alignment window has focus");
      return;
    }
    new ColumnCounterSetWorker(currentAlignFrame.getAlignViewport(),
            currentAlignFrame, counter);
  }

  /**
   * Constructs and registers a new alignment annotation worker
   * 
   * @param calculator
   *          provider of AlignmentAnnotation for the alignment
   */
  public static void newCalculator(AnnotationProviderI calculator)
  {
    // TODO need an interface for AlignFrame by which to access
    // its AlignViewportI and AlignmentViewPanel
    AlignFrame currentAlignFrame = Jalview.getCurrentAlignFrame();
    if (currentAlignFrame != null)
    {
      new AnnotationWorker(currentAlignFrame.getViewport(),
              currentAlignFrame.getAlignPanels().get(0), calculator);
    }
    else
    {
      System.err.println(
              "Can't register calculator as no alignment window has focus");
    }
  }

  /**
   * Constructs and registers a new alignment annotation worker
   * 
   * @param viewport
   * @param panel
   * @param calculator
   *          provider of AlignmentAnnotation for the alignment
   */
  public static void newCalculator(AlignViewportI viewport,
          AlignmentViewPanel panel, AnnotationProviderI calculator)
  {
    new AnnotationWorker(viewport, panel, calculator);
  }

  /**
   * Factory method to construct an Annotation object
   * 
   * @param displayChar
   * @param desc
   * @param secondaryStructure
   * @param val
   * @param color
   * @return
   */
  public static Annotation newAnnotation(String displayChar, String desc,
          char secondaryStructure, float val, Color color)
  {
    return new Annotation(displayChar, desc, secondaryStructure, val,
            color);
  }

  /**
   * Factory method to construct an AlignmentAnnotation object
   * 
   * @param name
   * @param desc
   * @param anns
   * @return
   */
  public static AlignmentAnnotation newAlignmentAnnotation(String name,
          String desc, Annotation[] anns)
  {
    return new AlignmentAnnotation(name, desc, anns);
  }
}
