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
package jalview.schemes;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.awt.Color;
import java.util.Iterator;
import java.util.Map;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.api.AlignViewportI;
import jalview.bin.Cache;
import jalview.bin.Jalview;
import jalview.datamodel.AnnotatedCollectionI;
import jalview.datamodel.SequenceCollectionI;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.Desktop;
import jalview.gui.SequenceRenderer;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;
import jalview.schemes.ClustalxColourScheme.ClustalColour;

public class ColourSchemesTest
{
  /*
   * a colour scheme that alternates Taylor and Zappo
   * colouring by column
   */
  class Stripy extends ResidueColourScheme
  {
    private ResidueColourScheme odd;

    private ResidueColourScheme even;

    private Stripy()
    {
    }

    /**
     * constructor given colours for odd and even columns
     * 
     * @param odd
     * @param even
     */
    private Stripy(ColourSchemeI cs1, ColourSchemeI cs2)
    {
      odd = (ResidueColourScheme) cs1;
      even = (ResidueColourScheme) cs2;
    }

    @Override
    public ColourSchemeI getInstance(AlignViewportI view,
            AnnotatedCollectionI sg)
    {
      final ColourSchemeI cs1 = ColourSchemes.getInstance().getColourScheme(
              JalviewColourScheme.Taylor.toString(),
              (AnnotatedCollectionI) null);
      final ColourSchemeI cs2 = ColourSchemes.getInstance().getColourScheme(
              JalviewColourScheme.Zappo.toString(),
              (AnnotatedCollectionI) null);
      return new Stripy(cs1, cs2);
    }

    @Override
    public Color findColour(char c, int j, SequenceI seq)
    {
      if (j % 2 == 1)
      {
        return odd.findColour(c, j, seq);
      }
      else
      {
        return even.findColour(c, j, seq);
      }
    }

    @Override
    public String getSchemeName()
    {
      return "stripy";
    }
  };

  /*
   * a colour scheme that is Clustal but using AWT colour equivalents
   */
  class MyClustal extends ResidueColourScheme
  {
    ClustalxColourScheme delegate;

    private MyClustal()
    {
    }

    private MyClustal(AnnotatedCollectionI sg,
            Map<SequenceI, SequenceCollectionI> hiddenRepSequences)
    {
      delegate = new ClustalxColourScheme(sg, hiddenRepSequences);
    }

    @Override
    public Color findColour(char c, int j, SequenceI seq)
    {
      Color col = delegate.findColour(c, j, seq);
      Color result = col;
      if (col.equals(ClustalColour.BLUE.colour))
      {
        result = Color.blue;
      }
      else if (col.equals(ClustalColour.CYAN.colour))
      {
        result = Color.cyan;
      }
      else if (col.equals(ClustalColour.GREEN.colour))
      {
        result = Color.green;
      }
      else if (col.equals(ClustalColour.MAGENTA.colour))
      {
        result = Color.magenta;
      }
      else if (col.equals(ClustalColour.ORANGE.colour))
      {
        result = Color.orange;
      }
      else if (col.equals(ClustalColour.PINK.colour))
      {
        result = Color.pink;
      }
      else if (col.equals(ClustalColour.RED.colour))
      {
        result = Color.red;
      }
      else if (col.equals(ClustalColour.YELLOW.colour))
      {
        result = Color.yellow;
      }
      return result;
    }

    @Override
    public ColourSchemeI getInstance(AlignViewportI view,
            AnnotatedCollectionI sg)
    {
      return new MyClustal(sg, view.getHiddenRepSequences());
    }

    @Override
    public String getSchemeName()
    {
      return "MyClustal";
    }

  }

  @BeforeClass(alwaysRun = true)
  public static void setUpBeforeClass() throws Exception
  {
    /*
     * use read-only test properties file
     */
    Cache.loadProperties("test/jalview/io/testProps.jvprops");
    Jalview.main(new String[] { "-nonews" });
  }

  @AfterClass(alwaysRun = true)
  public static void tearDownAfterClass() throws Exception
  {
    if (Desktop.instance != null)
      Desktop.instance.closeAll_actionPerformed(null);
  }

  @Test(groups = "Functional")
  public void testGetColourSchemes()
  {
    /*
     * this just verifies that built-in colour schemes are loaded into ColourSchemes
     * in the order in which they are declared in the JalviewColourScheme enum
     * (this also determines their order in Colour menus)
     */
    Iterator<ColourSchemeI> schemes = ColourSchemes.getInstance()
            .getColourSchemes().iterator();
    JalviewColourScheme[] jalviewSchemes = JalviewColourScheme.values();
    int i = 0;
    while (schemes.hasNext() && i < jalviewSchemes.length)
    {
      assertTrue(schemes.next().getSchemeName()
              .equals(jalviewSchemes[i].toString()));
      i++;
    }
  }

  @Test(groups = "Functional")
  public void testGetColourScheme()
  {
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            ">seq1\nAGLRTWQU", DataSourceType.PASTE);
    ColourSchemes schemes = ColourSchemes.getInstance();

    AnnotatedCollectionI al = af.getViewport().getAlignment();

    for (JalviewColourScheme cs : JalviewColourScheme.values())
    {
      ColourSchemeI registered = schemes.getColourScheme(cs.toString(), al);
      assertSame(registered.getClass(), cs.getSchemeClass());
    }
    af.closeMenuItem_actionPerformed(true);
  }

  @Test(groups = "Functional")
  public void testRegisterColourScheme()
  {
    ColourSchemes.getInstance().registerColourScheme(new Stripy());
    ColourSchemes.getInstance().registerColourScheme(new MyClustal());
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(
            "examples/uniref50.fa", DataSourceType.FILE);
    /*
     * set a breakpoint here to see and play with the newly registered
     *  colour schemes in the AlignFrame colour menu
     */
    SequenceRenderer sr = new SequenceRenderer(af.getViewport());
    SequenceI seq = af.getViewport().getAlignment().findName("FER_CAPAA");

    /*
     * set and check Taylor colours
     */
    af.changeColour_actionPerformed(JalviewColourScheme.Taylor.toString());
    Color taylor1 = sr.getResidueColour(seq, 88, null); // E 255,0,102
    Color taylor2 = sr.getResidueColour(seq, 89, null); // A 204,255,0
    Color taylor3 = sr.getResidueColour(seq, 90, null); // G 255,153,0
    assertEquals(taylor1, new Color(255, 0, 102));
    assertEquals(taylor2, new Color(204, 255, 0));
    assertEquals(taylor3, new Color(255, 153, 0));

    /*
     * set and check Zappo colours
     */
    af.changeColour_actionPerformed(JalviewColourScheme.Zappo.toString());
    Color zappo1 = sr.getResidueColour(seq, 88, null); // E red
    Color zappo2 = sr.getResidueColour(seq, 89, null); // A pink
    Color zappo3 = sr.getResidueColour(seq, 90, null); // G magenta
    assertEquals(zappo1, Color.red);
    assertEquals(zappo2, Color.pink);
    assertEquals(zappo3, Color.magenta);

    /*
     * set 'stripy' colours - odd columns are Taylor and even are Zappo 
     */
    af.changeColour_actionPerformed("stripy");
    Color stripy1 = sr.getResidueColour(seq, 88, null);
    Color stripy2 = sr.getResidueColour(seq, 89, null);
    Color stripy3 = sr.getResidueColour(seq, 90, null);
    assertEquals(stripy1, zappo1);
    assertEquals(stripy2, taylor2);
    assertEquals(stripy3, zappo3);

    /*
     * set and check Clustal colours
     */
    af.changeColour_actionPerformed(JalviewColourScheme.Clustal.toString());
    Color clustal1 = sr.getResidueColour(seq, 88, null);
    Color clustal2 = sr.getResidueColour(seq, 89, null);
    Color clustal3 = sr.getResidueColour(seq, 90, null);
    assertEquals(clustal1, ClustalColour.MAGENTA.colour);
    assertEquals(clustal2, ClustalColour.BLUE.colour);
    assertEquals(clustal3, ClustalColour.ORANGE.colour);

    /*
     * set 'MyClustal' colours - uses AWT colour equivalents
     */
    af.changeColour_actionPerformed("MyClustal");
    Color myclustal1 = sr.getResidueColour(seq, 88, null);
    Color myclustal2 = sr.getResidueColour(seq, 89, null);
    Color myclustal3 = sr.getResidueColour(seq, 90, null);
    assertEquals(myclustal1, Color.MAGENTA);
    assertEquals(myclustal2, Color.BLUE);
    assertEquals(myclustal3, Color.ORANGE);
  }

  /**
   * Tests for check if scheme name exists. Built-in scheme names are the
   * toString() values of enum JalviewColourScheme.
   */
  @Test(groups = "Functional")
  public void testNameExists()
  {
    ColourSchemes cs = ColourSchemes.getInstance();
    assertFalse(cs.nameExists(null));
    assertFalse(cs.nameExists(""));
    assertTrue(cs.nameExists("Clustal"));
    assertTrue(cs.nameExists("CLUSTAL"));
    assertFalse(cs.nameExists("CLUSTAL "));
    assertTrue(cs.nameExists("% Identity"));
    assertFalse(cs.nameExists("PID"));
  }
}
