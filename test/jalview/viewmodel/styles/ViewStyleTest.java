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
package jalview.viewmodel.styles;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import jalview.gui.JvOptionPane;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.Random;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ViewStyleTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  Random r = new Random();

  /**
   * This test uses reflection to set all fields on a ViewStyle, make a copy of
   * it, and verify all fields match. This test should fail if a getter/setter
   * pair are added to the class but missing in the copy constructor. Using
   * reflection in the copy constructor itself is broken by obfuscation when the
   * applet is built.
   * 
   * To prove this test works, simply comment out a line in the ViewStyle copy
   * constructor, or add a new member field to ViewStyle.
   * 
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  @Test(groups = { "Functional" })
  public void testCopyConstructor()
          throws IllegalArgumentException, IllegalAccessException
  {
    ViewStyle vs1 = new ViewStyle();
    Field[] fields = ViewStyle.class.getDeclaredFields();
    for (Field field : fields)
    {
      field.setAccessible(true);
      if (!copyConstructorIgnores(field))
      {
        changeValue(vs1, field);
      }
    }

    ViewStyle vs2 = new ViewStyle(vs1);

    for (Field field1 : fields)
    {
      if (!copyConstructorIgnores(field1))
      {
        final Object value1 = field1.get(vs1);
        final Object value2 = field1.get(vs2);
        String msg = "Mismatch in " + field1.getName() + "(" + value1 + "/"
                + value2 + ") - not set in copy constructor?";
        assertEquals(msg, value1, value2);
      }
    }
    assertEquals("Hashcode not equals", vs1.hashCode(), vs2.hashCode());
  }

  /**
   * Add tests here for any fields that we expect to be ignored by the copy
   * constructor
   * 
   * @param field
   * @return
   */
  private boolean copyConstructorIgnores(Field field)
  {
    /*
     * ignore instrumentation added by jacoco for test coverage
     */
    if (field.isSynthetic())
    {
      return true;
    }
    if (field.getType().toString().contains("com_atlassian_clover"))
    {
      return true;
    }

    return false;
  }

  /**
   * Change the value of one field in a ViewStyle object
   * 
   * @param vs
   * @param field
   * @throws IllegalAccessException
   */
  protected void changeValue(ViewStyle vs, Field field)
          throws IllegalAccessException
  {
    Class<?> type = field.getType();

    if (type.equals(boolean.class) || type.equals(Boolean.class))
    {
      boolean value = (Boolean) field.get(vs);
      // System.out.println("Setting " + field.getName() + " to " + !value);
      field.set(vs, !value);
    }
    else if (type.equals(short.class) || type.equals(int.class)
            || type.equals(long.class) || type.equals(float.class)
            || type.equals(double.class))
    {
      final int value = (int) (1 + field.getDouble(vs));
      // System.out.println("Setting " + field.getName() + " to " + value);
      field.set(vs, value);
    }
    else if (type.equals(Integer.class))
    {
      field.set(vs, (int) (1 + getNumberValue(field, vs)));
    }
    else if (type.equals(Float.class))
    {
      field.set(vs, (float) (1f + getNumberValue(field, vs)));
    }
    else if (type.equals(Long.class))
    {
      field.set(vs, (long) (1L + getNumberValue(field, vs)));
    }
    else if (type.equals(Double.class))
    {
      field.set(vs, 1d + getNumberValue(field, vs));
    }
    else if (type.equals(Short.class))
    {
      field.set(vs, (short) (1 + getNumberValue(field, vs)));
    }
    else if (type.equals(Byte.class))
    {
      field.set(vs, (byte) (1 + getNumberValue(field, vs)));
    }
    else if (type.equals(Character.class))
    {
      field.set(vs, (char) (1 + getNumberValue(field, vs)));
    }
    else if (type.equals(String.class))
    {
      field.set(vs, "Joe" + field.get(vs));
    }
    else if (type.equals(Color.class))
    {
      field.set(vs,
              Color.RED.equals(field.get(vs)) ? Color.BLACK : Color.RED);
    }
    else
    {
      AssertJUnit.fail("Unhandled field type (add to test): "
              + field.getName() + ":" + type);
    }
  }

  private double getNumberValue(Field field, ViewStyle vs)
          throws IllegalArgumentException, IllegalAccessException
  {
    if (field.get(vs) == null)
    {
      return 0d;
    }
    return ((Number) field.get(vs)).doubleValue();
  }

  /**
   * Test that the equals method compares every field by changing them one by
   * one in a cloned ViewStyle.
   * 
   * This test will fail if a new field is added to ViewStyle but not to the
   * comparisons in ViewStyle.equals().
   * 
   * To confirm that this test works, temporarily comment out one of the field
   * comparisons in ViewStyle.equals()
   * 
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  @Test(groups = { "Functional" })
  public void testEquals()
          throws IllegalArgumentException, IllegalAccessException
  {
    ViewStyle vs1 = new ViewStyle();
    ViewStyle vs2 = new ViewStyle(vs1);

    assertFalse(vs1.equals(null));
    assertFalse(vs1.equals(this));
    assertTrue(vs1.equals(vs2));
    assertTrue(vs2.equals(vs1));

    Field[] fields = ViewStyle.class.getDeclaredFields();
    for (Field field : fields)
    {
      if (!copyConstructorIgnores(field))
      {
        field.setAccessible(true);
        Object oldValue = field.get(vs2);
        changeValue(vs2, field);
        assertFalse("equals method ignores " + field.getName(),
                vs1.equals(vs2));

        if (vs1.hashCode() == vs2.hashCode())
        {
          // uncomment next line to see which fields hashCode ignores
          // System.out.println("hashCode ignores " + field.getName());
        }
        // restore original value before testing the next field
        field.set(vs2, oldValue);
      }
    }
  }
}
