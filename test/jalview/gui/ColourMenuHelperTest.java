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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import jalview.bin.Cache;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.schemes.ClustalxColourScheme;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ColourSchemes;
import jalview.schemes.NucleotideColourScheme;
import jalview.schemes.PIDColourScheme;
import jalview.schemes.ResidueColourScheme;
import jalview.util.MessageManager;

import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ColourMenuHelperTest
{
  /**
   * Use a properties file with a user-defined colour scheme
   */
  @BeforeClass(alwaysRun = true)
  public void setUp()
  {
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
  }

  @Test(groups = "Functional")
  public void testAddMenuItems_peptide()
  {
    SequenceI s1 = new Sequence("s1", "KFRQSILM");
    AlignmentI al = new Alignment(new SequenceI[] { s1 });
    JMenu menu = new JMenu();

    ButtonGroup bg = ColourMenuHelper.addMenuItems(menu, null, al, false);
    Enumeration<AbstractButton> bgElements = bg.getElements();

    /*
     * first entry is 'No Colour' option
     */
    JMenuItem item = menu.getItem(0);
    assertEquals(item.getName(), ResidueColourScheme.NONE);
    assertEquals(item.getText(), MessageManager.getString("label.none"));
    AbstractButton bgItem = bgElements.nextElement();
    assertSame(bgItem, item);

    /*
     * check that each registered colour scheme is in the menu,
     * and in the button group;
     * nucleotide-only schemes should be disabled menu items
     */
    Iterator<ColourSchemeI> colourSchemes = ColourSchemes.getInstance()
            .getColourSchemes().iterator();
    final int items = menu.getItemCount();
    for (int i = 1; i < items; i++)
    {
      item = menu.getItem(i);
      bgItem = bgElements.nextElement();
      assertSame(bgItem, item);
      ColourSchemeI cs = colourSchemes.next();
      String name = cs.getSchemeName();
      assertEquals(item.getName(), name);
      boolean enabled = item.isEnabled();
      assertEquals(enabled, cs.isApplicableTo(al));
      if (cs instanceof NucleotideColourScheme) // nucleotide only
      {
        assertFalse(enabled);
      }
      if (cs instanceof ClustalxColourScheme) // peptide only
      {
        assertTrue(enabled);
      }
      if (cs instanceof PIDColourScheme) // nucleotide or peptide
      {
        assertTrue(enabled);
      }

      /*
       * check i18n for display name
       */
      String label = MessageManager.getStringOrReturn("label.colourScheme_",
              name);
      assertEquals(item.getText(), label);
    }

    /*
     * check nothing left over
     */
    assertFalse(colourSchemes.hasNext());
    assertFalse(bgElements.hasMoreElements());
  }

  @Test(groups = "Functional")
  public void testAddMenuItems_nucleotide()
  {
    SequenceI s1 = new Sequence("s1", "GAATAATCCATAACAG");
    AlignmentI al = new Alignment(new SequenceI[] { s1 });
    JMenu menu = new JMenu();
    AlignFrame af = new AlignFrame(al, 500, 500);

    /*
     * menu for SequenceGroup excludes 'User Defined Colour'
     */
    PopupMenu popup = new PopupMenu(af.alignPanel, s1, null);
    ButtonGroup bg = ColourMenuHelper.addMenuItems(menu, popup, al, false);
    Enumeration<AbstractButton> bgElements = bg.getElements();

    /*
     * first entry is 'No Colour' option
     */
    JMenuItem item = menu.getItem(0);
    assertEquals(item.getName(), ResidueColourScheme.NONE);
    assertEquals(item.getText(), MessageManager.getString("label.none"));
    AbstractButton bgItem = bgElements.nextElement();
    assertSame(bgItem, item);

    /*
     * check that each registered colour scheme is in the menu,
     * and in the button group;
     * nucleotide-only schemes should be disabled menu items
     */
    Iterator<ColourSchemeI> colourSchemes = ColourSchemes.getInstance()
            .getColourSchemes().iterator();
    final int items = menu.getItemCount();
    for (int i = 1; i < items; i++)
    {
      item = menu.getItem(i);
      bgItem = bgElements.nextElement();
      assertSame(bgItem, item);
      ColourSchemeI cs = colourSchemes.next();
      String name = cs.getSchemeName();
      assertEquals(item.getName(), name);
      boolean enabled = item.isEnabled();
      assertEquals(enabled, cs.isApplicableTo(al));
      if (cs instanceof NucleotideColourScheme) // nucleotide only
      {
        assertTrue(enabled);
      }
      if (cs instanceof ClustalxColourScheme) // peptide only
      {
        assertFalse(enabled);
      }
      if (cs instanceof PIDColourScheme) // nucleotide or peptide
      {
        assertTrue(enabled);
      }

      /*
       * check i18n for display name
       */
      String label = MessageManager.getStringOrReturn("label.colourScheme_",
              name);
      assertEquals(item.getText(), label);
    }

    /*
     * check nothing left over
     */
    assertFalse(colourSchemes.hasNext());
    assertFalse(bgElements.hasMoreElements());
  }

  /**
   * 'Simple only' mode constructs colour menu for structures
   * <ul>
   * <li>no 'No Colour' option</li>
   * <li>only simple colour schemes (colour per residue)</li>
   * </ul>
   */
  @Test(groups = "Functional")
  public void testAddMenuItems_simpleOnly()
  {
    SequenceI s1 = new Sequence("s1", "KFRQSILM");
    AlignmentI al = new Alignment(new SequenceI[] { s1 });
    JMenu menu = new JMenu();

    ButtonGroup bg = ColourMenuHelper.addMenuItems(menu, null, al, true);
    Enumeration<AbstractButton> bgElements = bg.getElements();

    /*
     * check that only 'simple' colour schemes are included
     */
    Iterator<ColourSchemeI> colourSchemes = ColourSchemes.getInstance()
            .getColourSchemes().iterator();
    int i = 0;
    while (colourSchemes.hasNext())
    {
      ColourSchemeI cs = colourSchemes.next();
      if (!cs.isSimple())
      {
        continue;
      }
      JMenuItem item = menu.getItem(i++);
      AbstractButton bgItem = bgElements.nextElement();
      assertSame(bgItem, item);
    }

    /*
     * check nothing left over
     */
    assertEquals(i, menu.getItemCount());
    assertFalse(bgElements.hasMoreElements());
  }

  /*
   * menu for AlignFrame includes 'User Defined Colour'
   */
  @Test(groups = "Functional")
  public void testAddMenuItems_forAlignFrame()
  {
    SequenceI s1 = new Sequence("s1", "KFRQSILM");
    AlignmentI al = new Alignment(new SequenceI[] { s1 });
    AlignFrame af = new AlignFrame(al, 500, 500);
    JMenu menu = new JMenu();

    ButtonGroup bg = ColourMenuHelper.addMenuItems(menu, af, al, false);
    Enumeration<AbstractButton> bgElements = bg.getElements();

    /*
     * check that each registered colour scheme is in the menu,
     * (skipping over No Colour which is the first menu item),
     * and in the button group
     */
    bgElements.nextElement(); // skip No Colour
    Iterator<ColourSchemeI> colourSchemes = ColourSchemes.getInstance()
            .getColourSchemes().iterator();
    final int items = menu.getItemCount();
    for (int i = 1; i < items - 1; i++)
    {
      JMenuItem item = menu.getItem(i);
      AbstractButton bgItem = bgElements.nextElement();
      assertSame(bgItem, item);
      ColourSchemeI cs = colourSchemes.next();
      assertEquals(item.getName(), cs.getSchemeName());
    }

    /*
     * check menu also has User Defined Colour
     */
    assertFalse(colourSchemes.hasNext());
    JMenuItem item = menu.getItem(items - 1);
    AbstractButton bgItem = bgElements.nextElement();
    assertSame(bgItem, item);
    assertEquals(item.getName(), ResidueColourScheme.USER_DEFINED_MENU);
    assertEquals(item.getText(),
            MessageManager.getString("action.user_defined"));
  }
}
