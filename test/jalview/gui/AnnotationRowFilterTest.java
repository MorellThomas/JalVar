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
package jalview.gui;

import static org.testng.Assert.assertEquals;

import jalview.bin.Cache;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceI;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;

import java.util.Vector;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for methods of base class of annotation column or colour chooser
 */
public class AnnotationRowFilterTest
{
  AlignFrame af;

  private AnnotationRowFilter testee;

  @BeforeClass(alwaysRun = true)
  public void setUp()
  {
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    Cache.applicationProperties.setProperty("SHOW_ANNOTATIONS",
            Boolean.TRUE.toString());
    Cache.applicationProperties.setProperty(Preferences.SHOW_AUTOCALC_ABOVE,
            Boolean.TRUE.toString());
    af = new FileLoader().LoadFileWaitTillLoaded("examples/uniref50.fa",
            DataSourceType.FILE);
    testee = new AnnotationRowFilter(af.viewport, af.alignPanel)
    {
      @Override
      public void valueChanged(boolean updateAllAnnotation)
      {
      }

      @Override
      public void updateView()
      {
      }

      @Override
      public void reset()
      {
      }
    };
  }

  /**
   * Test the method that builds the drop-down list of annotations to choose
   * from for colour by annotation or select columns by annotation
   */
  @Test(groups = "Functional")
  public void testGetAnnotationItems()
  {
    AlignmentI al = af.getViewport().getAlignment();
    SequenceI seq1 = al.findSequenceMatch("FER_CAPAA")[0];
    SequenceI seq2 = al.findSequenceMatch("FER_BRANA")[0];

    AlignmentAnnotation ann1 = new AlignmentAnnotation("ann1Label", "ann1",
            null);
    al.addAnnotation(ann1);
    AlignmentAnnotation ann2 = new AlignmentAnnotation("Significance",
            "ann2", null);
    al.addAnnotation(ann2);
    /*
     * a second Significance alignment annotation
     */
    AlignmentAnnotation ann2a = new AlignmentAnnotation("Significance",
            "ann2", null);
    al.addAnnotation(ann2a);

    AlignmentAnnotation ann3 = new AlignmentAnnotation("Jronn", "Jronn",
            null);
    ann3.setSequenceRef(seq1);
    al.addAnnotation(ann3);
    AlignmentAnnotation ann4 = new AlignmentAnnotation("Jronn", "Jronn",
            null);
    ann4.setSequenceRef(seq2);
    al.addAnnotation(ann4);
    AlignmentAnnotation ann5 = new AlignmentAnnotation("Jnet", "Jnet",
            null);
    ann5.setSequenceRef(seq2);
    al.addAnnotation(ann5);
    /*
     * a second Jnet annotation for FER_BRANA
     */
    AlignmentAnnotation ann6 = new AlignmentAnnotation("Jnet", "Jnet",
            null);
    ann6.setSequenceRef(seq2);
    al.addAnnotation(ann6);

    /*
     * drop-down items with 'Per-sequence only' not checked
     */
    Vector<String> items = testee.getAnnotationItems(false);
    assertEquals(items.toString(),
            "[Conservation, Quality, Consensus, Occupancy, ann1Label, Significance, Significance_1, Jronn_FER_CAPAA, Jronn_FER_BRANA, Jnet_FER_BRANA, Jnet_FER_BRANA_2]");
    assertEquals(testee.getAnnotationMenuLabel(ann1), "ann1Label");
    assertEquals(testee.getAnnotationMenuLabel(ann2), "Significance");
    assertEquals(testee.getAnnotationMenuLabel(ann2a), "Significance_1");
    assertEquals(testee.getAnnotationMenuLabel(ann3), "Jronn_FER_CAPAA");
    assertEquals(testee.getAnnotationMenuLabel(ann4), "Jronn_FER_BRANA");
    assertEquals(testee.getAnnotationMenuLabel(ann5), "Jnet_FER_BRANA");
    assertEquals(testee.getAnnotationMenuLabel(ann6), "Jnet_FER_BRANA_2");

    /*
     * drop-down items with 'Per-sequence only' checked
     */
    items = testee.getAnnotationItems(true);
    assertEquals(items.toString(), "[Jronn, Jnet]");
    // the first annotation of the type is associated with the menu item
    assertEquals(testee.getAnnotationMenuLabel(ann3), "Jronn");
    assertEquals(testee.getAnnotationMenuLabel(ann5), "Jnet");
  }
}
