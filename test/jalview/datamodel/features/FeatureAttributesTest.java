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
package jalview.datamodel.features;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import jalview.datamodel.SequenceFeature;
import jalview.datamodel.features.FeatureAttributes.Datatype;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import junit.extensions.PA;

public class FeatureAttributesTest
{

  /**
   * clear down attributes map before tests
   */
  @BeforeClass(alwaysRun = true)
  public void setUp()
  {
    FeatureAttributes fa = FeatureAttributes.getInstance();
    ((Map<?, ?>) PA.getValue(fa, "attributes")).clear();
  }

  /**
   * clear down attributes map after tests
   */
  @AfterMethod(alwaysRun = true)
  public void tearDown()
  {
    FeatureAttributes fa = FeatureAttributes.getInstance();
    ((Map<?, ?>) PA.getValue(fa, "attributes")).clear();
  }

  /**
   * Test the method that keeps attribute names in non-case-sensitive order,
   * including handling of 'compound' names
   */
  @Test(groups = "Functional")
  public void testAttributeNameComparator()
  {
    FeatureAttributes fa = FeatureAttributes.getInstance();
    Comparator<String[]> comp = (Comparator<String[]>) PA.getValue(fa,
            "comparator");

    assertEquals(
            comp.compare(new String[]
            { "CSQ" }, new String[] { "csq" }), 0);

    assertTrue(
            comp.compare(new String[]
            { "CSQ", "a" }, new String[] { "csq" }) > 0);

    assertTrue(
            comp.compare(new String[]
            { "CSQ" }, new String[] { "csq", "b" }) < 0);

    assertTrue(
            comp.compare(new String[]
            { "CSQ", "AF" }, new String[] { "csq", "ac" }) > 0);

    assertTrue(
            comp.compare(new String[]
            { "CSQ", "ac" }, new String[] { "csq", "AF" }) < 0);
  }

  @Test(groups = "Functional")
  public void testGetMinMax()
  {
    SequenceFeature sf = new SequenceFeature("Pfam", "desc", 10, 20,
            "group");
    FeatureAttributes fa = FeatureAttributes.getInstance();
    assertNull(fa.getMinMax("Pfam", "kd"));
    sf.setValue("domain", "xyz");
    assertNull(fa.getMinMax("Pfam", "kd"));
    sf.setValue("kd", "1.3");
    assertEquals(fa.getMinMax("Pfam", "kd"), new float[] { 1.3f, 1.3f });
    sf.setValue("kd", "-2.6");
    assertEquals(fa.getMinMax("Pfam", "kd"), new float[] { -2.6f, 1.3f });
    // setting 'mixed' character and numeric values wipes the min/max value
    sf.setValue("kd", "some text");
    assertNull(fa.getMinMax("Pfam", "kd"));

    Map<String, String> csq = new HashMap<>();
    csq.put("AF", "-3");
    sf.setValue("CSQ", csq);
    assertEquals(fa.getMinMax("Pfam", "CSQ", "AF"),
            new float[]
            { -3f, -3f });
    csq.put("AF", "4");
    sf.setValue("CSQ", csq);
    assertEquals(fa.getMinMax("Pfam", "CSQ", "AF"),
            new float[]
            { -3f, 4f });
  }

  /**
   * Test the method that returns an attribute description, provided it is
   * recorded and unique
   */
  @Test(groups = "Functional")
  public void testGetDescription()
  {
    FeatureAttributes fa = FeatureAttributes.getInstance();
    // with no description returns null
    assertNull(fa.getDescription("Pfam", "kd"));
    // with a unique description, returns that value
    fa.addDescription("Pfam", "desc1", "kd");
    assertEquals(fa.getDescription("Pfam", "kd"), "desc1");
    // with ambiguous description, returns null
    fa.addDescription("Pfam", "desc2", "kd");
    assertNull(fa.getDescription("Pfam", "kd"));
  }

  @Test(groups = "Functional")
  public void testDatatype()
  {
    FeatureAttributes fa = FeatureAttributes.getInstance();
    assertNull(fa.getDatatype("Pfam", "kd"));
    SequenceFeature sf = new SequenceFeature("Pfam", "desc", 10, 20,
            "group");
    sf.setValue("kd", "-1");
    sf.setValue("domain", "Metal");
    sf.setValue("phase", "1");
    sf.setValue("phase", "reverse");
    assertEquals(fa.getDatatype("Pfam", "kd"), Datatype.Number);
    assertEquals(fa.getDatatype("Pfam", "domain"), Datatype.Character);
    assertEquals(fa.getDatatype("Pfam", "phase"), Datatype.Mixed);
  }
}
