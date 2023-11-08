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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import jalview.datamodel.Alignment;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.viewmodel.AlignmentViewport;

import java.awt.Component;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PaintRefresherTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  // TODO would prefer PaintRefresher to be a single rather than static
  @BeforeMethod(alwaysRun = true)
  public void setUp()
  {
    PaintRefresher.components.clear();
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown()
  {
    PaintRefresher.components.clear();
  }

  @Test(groups = { "Functional" })
  public void testRegister()
  {
    JPanel jp = new JPanel();
    JPanel jp2 = new JPanel();
    JPanel jp3 = new JPanel();
    JPanel jp4 = new JPanel();
    PaintRefresher.Register(jp, "22");
    PaintRefresher.Register(jp, "22");
    PaintRefresher.Register(jp2, "22");
    PaintRefresher.Register(jp3, "33");
    PaintRefresher.Register(jp3, "44");
    PaintRefresher.Register(jp4, "44");

    Map<String, List<Component>> registered = PaintRefresher.components;
    assertEquals(3, registered.size());
    assertEquals(2, registered.get("22").size());
    assertEquals(1, registered.get("33").size());
    assertEquals(2, registered.get("44").size());
    assertTrue(registered.get("22").contains(jp));
    assertTrue(registered.get("22").contains(jp2));
    assertTrue(registered.get("33").contains(jp3));
    assertTrue(registered.get("44").contains(jp3));
    assertTrue(registered.get("44").contains(jp4));
  }

  @Test(groups = { "Functional" })
  public void testRemoveComponent()
  {
    Map<String, List<Component>> registered = PaintRefresher.components;

    // no error with an empty PaintRefresher
    JPanel jp = new JPanel();
    JPanel jp2 = new JPanel();
    PaintRefresher.RemoveComponent(jp);
    assertTrue(registered.isEmpty());

    /*
     * Add then remove one item
     */
    PaintRefresher.Register(jp, "11");
    PaintRefresher.RemoveComponent(jp);
    assertTrue(registered.isEmpty());

    /*
     * Add one item under two ids, then remove it. It is removed from both ids,
     * and the now empty id is removed.
     */
    PaintRefresher.Register(jp, "11");
    PaintRefresher.Register(jp, "22");
    PaintRefresher.Register(jp2, "22");
    PaintRefresher.RemoveComponent(jp);
    // "11" is removed as now empty, only 22/jp2 left
    assertEquals(1, registered.size());
    assertEquals(1, registered.get("22").size());
    assertTrue(registered.get("22").contains(jp2));
  }

  @Test(groups = { "Functional" })
  public void testGetAssociatedPanels()
  {
    SequenceI[] seqs = new SequenceI[] { new Sequence("", "ABC") };
    Alignment al = new Alignment(seqs);

    /*
     * AlignFrame constructor has side-effects: AlignmentPanel is constructed,
     * and SeqCanvas, IdPanel, AlignmentPanel are all registered under the
     * sequence set id of the viewport.
     */
    AlignmentViewport av = new AlignViewport(al);
    AlignFrame af = new AlignFrame(al, 4, 1);
    AlignmentPanel ap1 = af.alignPanel;
    AlignmentPanel[] panels = PaintRefresher
            .getAssociatedPanels(av.getSequenceSetId());
    assertEquals(1, panels.length);
    assertSame(ap1, panels[0]);

    panels = PaintRefresher.getAssociatedPanels(av.getSequenceSetId() + 1);
    assertEquals(0, panels.length);
  }
}
