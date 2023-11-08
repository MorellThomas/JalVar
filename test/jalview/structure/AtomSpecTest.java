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
package jalview.structure;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

public class AtomSpecTest
{
  @Test
  public void testFromChimeraAtomSpec()
  {
    AtomSpec as = AtomSpec.fromChimeraAtomspec("#1:12.B");
    assertEquals(as.getModelNumber(), 1);
    assertEquals(as.getPdbResNum(), 12);
    assertEquals(as.getChain(), "B");
    assertNull(as.getPdbFile());

    // no model - default to zero
    as = AtomSpec.fromChimeraAtomspec(":13.C");
    assertEquals(as.getModelNumber(), 0);
    assertEquals(as.getPdbResNum(), 13);
    assertEquals(as.getChain(), "C");
    assertNull(as.getPdbFile());

    // model.submodel
    as = AtomSpec.fromChimeraAtomspec("#3.2:15");
    assertEquals(as.getModelNumber(), 3);
    assertEquals(as.getPdbResNum(), 15);
    assertEquals(as.getChain(), "");
    assertNull(as.getPdbFile());

    String spec = "3:12.B";
    try
    {
      as = AtomSpec.fromChimeraAtomspec(spec);
      fail("Expected exception for " + spec);
    } catch (IllegalArgumentException e)
    {
      // ok
    }

    spec = "#3:12-14.B";
    try
    {
      as = AtomSpec.fromChimeraAtomspec(spec);
      fail("Expected exception for " + spec);
    } catch (IllegalArgumentException e)
    {
      // ok
    }

    spec = "";
    try
    {
      as = AtomSpec.fromChimeraAtomspec(spec);
      fail("Expected exception for " + spec);
    } catch (IllegalArgumentException e)
    {
      // ok
    }

    spec = null;
    try
    {
      as = AtomSpec.fromChimeraAtomspec(spec);
      fail("Expected exception for " + spec);
    } catch (NullPointerException e)
    {
      // ok
    }
  }

  @Test
  public void testFromChimeraXAtomSpec()
  {
    AtomSpec as = AtomSpec.fromChimeraXAtomspec("#1/B:12");
    assertEquals(as.getModelNumber(), 1);
    assertEquals(as.getPdbResNum(), 12);
    assertEquals(as.getChain(), "B");
    assertNull(as.getPdbFile());

    // no model - default to zero
    as = AtomSpec.fromChimeraXAtomspec("/C:13");
    assertEquals(as.getModelNumber(), 0);
    assertEquals(as.getPdbResNum(), 13);
    assertEquals(as.getChain(), "C");
    assertNull(as.getPdbFile());

    // model.submodel
    as = AtomSpec.fromChimeraXAtomspec("#3.2/:15");
    assertEquals(as.getModelNumber(), 3);
    assertEquals(as.getPdbResNum(), 15);
    assertEquals(as.getChain(), "");
    assertNull(as.getPdbFile());

    String spec = "3:12.B";
    try
    {
      as = AtomSpec.fromChimeraXAtomspec(spec);
      fail("Expected exception for " + spec);
    } catch (IllegalArgumentException e)
    {
      // ok
    }

    spec = "#3:12-14.B";
    try
    {
      as = AtomSpec.fromChimeraXAtomspec(spec);
      fail("Expected exception for " + spec);
    } catch (IllegalArgumentException e)
    {
      // ok
    }

    spec = "";
    try
    {
      as = AtomSpec.fromChimeraXAtomspec(spec);
      fail("Expected exception for " + spec);
    } catch (IllegalArgumentException e)
    {
      // ok
    }

    spec = null;
    try
    {
      as = AtomSpec.fromChimeraXAtomspec(spec);
      fail("Expected exception for " + spec);
    } catch (NullPointerException e)
    {
      // ok
    }
  }
}
