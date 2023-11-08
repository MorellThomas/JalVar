package jalview.datamodel;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.gui.JvOptionPane;

public class ContactMatrixTest
{
  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * standard asserts for ContactMatrixI
   */
  public static void testContactMatrixI(ContactMatrixI cm,
          boolean symmetric)
  {
    // assume contact matrix is square for text
    ContactListI clist = cm.getContactList(1);

    int width = clist.getContactHeight();
    Double minValue, maxValue;
    minValue = clist.getContactAt(0);
    maxValue = minValue;

    for (int p = 0; p < width; p++)
    {
      ContactListI pList = cm.getContactList(p);
      for (int q = p; q < width; q++)
      {
        if (q == p)
        {
          // compute minMax for pList
          minMax(minValue, maxValue, pList);

        }
        ContactListI qList = cm.getContactList(q);
        if (symmetric)
        {
          Assert.assertEquals(qList.getContactAt(p), pList.getContactAt(q),
                  "Contact matrix not symmetric");
        }
        else
        {
          Assert.assertNotEquals(qList.getContactAt(p),
                  pList.getContactAt(q),
                  "Contact matrix expected to be not symmetric");
        }
      }
    }
  }

  private static void minMax(Double minValue, Double maxValue,
          ContactListI pList)
  {
    int width = pList.getContactHeight();
    for (int rowcol = 0; rowcol < width; rowcol++)
    {
      double v = pList.getContactAt(rowcol);
      if (minValue > v)
      {
        minValue = v;
      }
      if (maxValue < v)
      {
        maxValue = v;
      }
    }
  }

  @Test(groups = { "Functional" })
  public void testminMaxCalc()
  {
    ContactListI clist = new ContactListImpl(new ContactListProviderI()
    {
      double[] val = { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 };

      @Override
      public int getPosition()
      {
        return 0;
      }

      @Override
      public int getContactHeight()
      {
        return val.length;
      }

      @Override
      public double getContactAt(int column)
      {
        if (column < 0 || column >= val.length)
        {
          return Double.NaN;
        }
        return val[column];
      }

    });
    // TODO - write test !
  }

  /**
   * test construction and accessors for asymmetric contact matrix
   */
  @Test(groups = { "Functional" })
  public void testAsymmetricContactMatrix()
  {
    // TODO - write test !

  }

  /**
   * test construction and accessors for symmetric contact matrix
   */
  @Test(groups = { "Functional" })
  public void testSymmetricContactMatrix()
  {
    // TODO - write test !

  }

}
