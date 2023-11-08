package jalview.datamodel;

import java.awt.Color;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Spliterator;
import java.util.StringTokenizer;

import jalview.bin.Console;

public abstract class ContactMatrix implements ContactMatrixI
{
  /**
   * are contacts reflexive ?
   */
  boolean symmetric = true;

  public ContactMatrix(boolean symmetric)
  {
    this.symmetric = symmetric;
  }

  List<List<Float>> contacts = null;

  int width = 0, numcontacts = 0;

  float min = 0f, max = 0f;

  public void addContact(int left, int right, float strength)
  {
    if (left < 0 || right < 0)
    {
      throw new Error(new RuntimeException(
              "Cannot have negative indices for contact left=" + left
                      + " right=" + right + " strength=" + strength));
    }
    if (symmetric)
    {
      if (left > right)
      {
        // swap
        int r = right;
        right = left;
        left = r;
      }
    }
    if (contacts == null)
    {
      // TODO: use sparse list for efficiency ?
      contacts = new ArrayList<List<Float>>();
    }
    List<Float> clist = contacts.get(left);
    if (clist == null)
    {
      clist = new ArrayList<Float>();
      contacts.set(left, clist);
    }
    Float last = clist.set(right, strength);
    // TODO: if last is non null, may need to recompute range
    checkBounds(strength);
    if (last == null)
    {
      numcontacts++;
    }
  }

  private void checkBounds(float strength)
  {
    if (min > strength)
    {
      min = strength;
    }
    if (max < strength)
    {
      max = strength;
    }
  }

  @Override
  public ContactListI getContactList(final int column)
  {
    if (column < 0 || column >= width)
    {
      return null;
    }

    return new ContactListImpl(new ContactListProviderI()
    {
      int p = column;

      @Override
      public int getPosition()
      {
        return p;
      }

      @Override
      public int getContactHeight()
      {
        return width;

      }

      @Override
      public double getContactAt(int column)
      {
        List<Float> clist;
        Float cl = null;
        if (symmetric)
        {
          if (p < column)
          {
            clist = contacts.get(p);
            cl = clist.get(column);
          }
          else
          {
            clist = contacts.get(column);
            cl = clist.get(p);
          }
        }
        else
        {
          clist = contacts.get(p);
          cl = clist.get(column);
        }
        if (cl == null)
        {
          // return 0 not NaN ?
          return Double.NaN;
        }
        return cl.doubleValue();
      }
    });
  }

  @Override
  public float getMin()
  {
    return min;
  }

  @Override
  public float getMax()
  {
    return max;
  }

  @Override
  public String getAnnotLabel()
  {
    return "Contact Matrix";
  }

  @Override
  public String getAnnotDescr()
  {
    return "Contact Matrix";
  }
  GroupSet grps = new GroupSet();
  @Override
  public GroupSetI getGroupSet()
  {
    return grps;
  }
  @Override
  public void setGroupSet(GroupSet makeGroups)
  {
    grps = makeGroups;
  }
  public static String contactToFloatString(ContactMatrixI cm)
  {
    StringBuilder sb = new StringBuilder();
    for (int c = 0; c < cm.getWidth(); c++)
    {
      ContactListI cl = cm.getContactList(c);
      if (cl != null)
      {
        for (int h = 0; h <= cl.getContactHeight(); h++)
        {
          if (sb.length() > 0)
          {
            sb.append('\t');
          }
          sb.append(cl.getContactAt(h));
        }
      }
    }
    return sb.toString();
  }

  public static float[][] fromFloatStringToContacts(String values, int cols,
          int rows)
  {
    float[][] vals = new float[cols][rows];
    StringTokenizer tabsep = new StringTokenizer(values, "" + '\t');
    int c = 0, r = 0;
    while (tabsep.hasMoreTokens())
    {
      double elem = Double.valueOf(tabsep.nextToken());
      vals[c][r++] = (float) elem;
      if (r >= vals[c].length)
      {
        r = 0;
        c++;
      }
      if (c >= vals.length)
      {
        break;
      }
    }
    if (tabsep.hasMoreElements())
    {
      Console.warn(
              "Ignoring additional elements for Float string to contact matrix parsing.");
    }
    return vals;
  }
}
