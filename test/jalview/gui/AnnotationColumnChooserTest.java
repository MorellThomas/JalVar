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

import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertEquals;

import jalview.analysis.AnnotationSorter.SequenceAnnotationOrder;
import jalview.bin.Cache;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.SequenceI;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.FormatAdapter;

import java.io.IOException;
import java.util.Iterator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for AnnotationChooser
 * 
 * @author kmourao
 *
 */
public class AnnotationColumnChooserTest
{
  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  // 4 sequences x 13 positions
  final static String TEST_DATA = ">FER_CAPAA Ferredoxin\n"
          + "TIETHKEAELVG-\n"
          + ">FER_CAPAN Ferredoxin, chloroplast precursor\n"
          + "TIETHKEAELVG-\n"
          + ">FER1_SOLLC Ferredoxin-1, chloroplast precursor\n"
          + "TIETHKEEELTA-\n" + ">Q93XJ9_SOLTU Ferredoxin I precursor\n"
          + "TIETHKEEELTA-\n";

  AnnotationChooser testee;

  AlignmentPanel parentPanel;

  AlignFrame af;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws IOException
  {
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    // pin down annotation sort order for test
    Cache.applicationProperties.setProperty(Preferences.SORT_ANNOTATIONS,
            SequenceAnnotationOrder.NONE.name());
    final String TRUE = Boolean.TRUE.toString();
    Cache.applicationProperties.setProperty(Preferences.SHOW_AUTOCALC_ABOVE,
            TRUE);
    Cache.applicationProperties.setProperty("SHOW_QUALITY", TRUE);
    Cache.applicationProperties.setProperty("SHOW_CONSERVATION", TRUE);
    Cache.applicationProperties.setProperty("SHOW_IDENTITY", TRUE);

    AlignmentI al = new FormatAdapter().readFile(TEST_DATA,
            DataSourceType.PASTE, FileFormat.Fasta);
    af = new AlignFrame(al, 700, 500);
    parentPanel = new AlignmentPanel(af, af.getViewport());
    addAnnotations();
  }

  /**
   * Add 4 annotations, 3 of them sequence-specific.
   * 
   * <PRE>
   * ann1 - for sequence 0 - label 'IUPRED' ann2 - not sequence related - label
   * 'Beauty' ann3 - for sequence 3 - label 'JMol' ann4 - for sequence 2 - label
   * 'IUPRED' ann5 - for sequence 1 - label 'JMol'
   */
  private void addAnnotations()
  {
    Annotation an = new Annotation(2f);
    Annotation[] anns = new Annotation[] { an, an, an };
    AlignmentAnnotation ann0 = new AlignmentAnnotation("IUPRED", "", anns);
    AlignmentAnnotation ann1 = new AlignmentAnnotation("Beauty", "", anns);
    AlignmentAnnotation ann2 = new AlignmentAnnotation("JMol", "", anns);
    AlignmentAnnotation ann3 = new AlignmentAnnotation("IUPRED", "", anns);
    AlignmentAnnotation ann4 = new AlignmentAnnotation("JMol", "", anns);
    SequenceI[] seqs = parentPanel.getAlignment().getSequencesArray();
    ann0.setSequenceRef(seqs[0]);
    ann2.setSequenceRef(seqs[3]);
    ann3.setSequenceRef(seqs[2]);
    ann4.setSequenceRef(seqs[1]);
    parentPanel.getAlignment().addAnnotation(ann0);
    parentPanel.getAlignment().addAnnotation(ann1);
    parentPanel.getAlignment().addAnnotation(ann2);
    parentPanel.getAlignment().addAnnotation(ann3);
    parentPanel.getAlignment().addAnnotation(ann4);
  }

  /**
   * Test reset
   */
  @Test(groups = { "Functional" })
  public void testReset()
  {
    AnnotationColumnChooser acc = new AnnotationColumnChooser(
            af.getViewport(), af.alignPanel);

    HiddenColumns oldhidden = new HiddenColumns();
    oldhidden.hideColumns(10, 20);
    acc.setOldHiddenColumns(oldhidden);

    HiddenColumns newHidden = new HiddenColumns();
    newHidden.hideColumns(0, 3);
    newHidden.hideColumns(22, 25);
    af.getViewport().setHiddenColumns(newHidden);

    HiddenColumns currentHidden = af.getViewport().getAlignment()
            .getHiddenColumns();
    Iterator<int[]> regions = currentHidden.iterator();
    int[] next = regions.next();
    assertEquals(0, next[0]);
    assertEquals(3, next[1]);
    next = regions.next();
    assertEquals(22, next[0]);
    assertEquals(25, next[1]);

    // now reset hidden columns
    acc.reset();
    currentHidden = af.getViewport().getAlignment().getHiddenColumns();
    regions = currentHidden.iterator();
    next = regions.next();
    assertEquals(10, next[0]);
    assertEquals(20, next[1]);

    // check works with empty hidden columns as old columns
    oldhidden = new HiddenColumns();
    acc.setOldHiddenColumns(oldhidden);
    acc.reset();
    currentHidden = af.getViewport().getAlignment().getHiddenColumns();
    assertFalse(currentHidden.hasHiddenColumns());

    // check works with empty hidden columns as new columns
    oldhidden.hideColumns(10, 20);
    acc.setOldHiddenColumns(oldhidden);
    currentHidden = af.getViewport().getAlignment().getHiddenColumns();
    assertFalse(currentHidden.hasHiddenColumns());

    acc.reset();
    currentHidden = af.getViewport().getAlignment().getHiddenColumns();
    regions = currentHidden.iterator();
    next = regions.next();
    assertEquals(10, next[0]);
    assertEquals(20, next[1]);
  }
}
