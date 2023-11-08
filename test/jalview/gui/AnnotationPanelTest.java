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

import jalview.datamodel.AlignmentAnnotation;

import org.testng.annotations.Test;

public class AnnotationPanelTest
{

  @Test(groups = "Functional")
  public void testGetRowIndex()
  {
    assertEquals(AnnotationPanel.getRowIndex(0, null), -1);

    AlignmentAnnotation[] anns = new AlignmentAnnotation[] {};
    assertEquals(AnnotationPanel.getRowIndex(0, anns), -1);

    AlignmentAnnotation ann1 = new AlignmentAnnotation(null, null, null);
    AlignmentAnnotation ann2 = new AlignmentAnnotation(null, null, null);
    AlignmentAnnotation ann3 = new AlignmentAnnotation(null, null, null);
    ann1.visible = true;
    ann2.visible = true;
    ann3.visible = true;
    ann1.height = 10;
    ann2.height = 20;
    ann3.height = 30;
    anns = new AlignmentAnnotation[] { ann1, ann2, ann3 };

    assertEquals(AnnotationPanel.getRowIndex(0, anns), 0);
    assertEquals(AnnotationPanel.getRowIndex(9, anns), 0);
    assertEquals(AnnotationPanel.getRowIndex(10, anns), 1);
    assertEquals(AnnotationPanel.getRowIndex(29, anns), 1);
    assertEquals(AnnotationPanel.getRowIndex(30, anns), 2);
    assertEquals(AnnotationPanel.getRowIndex(59, anns), 2);
    assertEquals(AnnotationPanel.getRowIndex(60, anns), -1);

    ann2.visible = false;
    assertEquals(AnnotationPanel.getRowIndex(0, anns), 0);
    assertEquals(AnnotationPanel.getRowIndex(9, anns), 0);
    assertEquals(AnnotationPanel.getRowIndex(10, anns), 2);
    assertEquals(AnnotationPanel.getRowIndex(39, anns), 2);
    assertEquals(AnnotationPanel.getRowIndex(40, anns), -1);

    ann1.visible = false;
    assertEquals(AnnotationPanel.getRowIndex(0, anns), 2);
    assertEquals(AnnotationPanel.getRowIndex(29, anns), 2);
    assertEquals(AnnotationPanel.getRowIndex(30, anns), -1);
  }
}
