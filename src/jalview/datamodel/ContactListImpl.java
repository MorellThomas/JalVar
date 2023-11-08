package jalview.datamodel;

import java.awt.Color;

import jalview.renderer.ContactGeometry.contactInterval;

/**
 * helper class to compute min/max/mean for a range on a contact list
 * 
 * @author jprocter
 *
 */
public class ContactListImpl implements ContactListI
{
  ContactListProviderI clist;

  public static ContactListI newContactList(ContactListProviderI list)
  {
    return new ContactListImpl(list);
  }

  public ContactListImpl(ContactListProviderI list)
  {
    clist = list;
  }

  @Override
  public int getPosition()
  {
    return clist.getPosition();
  }

  @Override
  public double getContactAt(int column)
  {
    return clist.getContactAt(column);
  }

  @Override
  public int getContactHeight()
  {
    return clist.getContactHeight();
  }

  @Override
  public ContactRange getRangeFor(int from_column, int to_column)
  {
    // TODO: consider caching ContactRange for a particular call ?
    if (clist instanceof ContactListI)
    {
      // clist may implement getRangeFor in a more efficient way, so use theirs
      return ((ContactListI) clist).getRangeFor(from_column, to_column);
    }
    if (from_column < 0)
    {
      from_column = 0;
    }
    if (to_column >= getContactHeight())
    {
      to_column = getContactHeight()-1;
    }
    ContactRange cr = new ContactRange();
    cr.setFrom_column(from_column);
    cr.setTo_column(to_column);
    double tot = 0;
    for (int i = from_column; i <= to_column; i++)
    {
      double contact = getContactAt(i);
      tot += contact;
      if (i == from_column)
      {
        cr.setMin(contact);
        cr.setMax(contact);
        cr.setMinPos(i);
        cr.setMaxPos(i);
      }
      else
      {
        if (cr.getMax() < contact)
        {
          cr.setMax(contact);
          cr.setMaxPos(i);
        }
        if (cr.getMin() < contact)
        {
          cr.setMin(contact);
          cr.setMinPos(i);
        }
      }
    }
    if (tot > 0)
    {
      cr.setMean(tot / (1 + to_column - from_column));
    }
    else
    {
      cr.setMean(tot);
    }
    return cr;
  }

  @Override
  public int[] getMappedPositionsFor(int cStart, int cEnd)
  {
    return clist.getMappedPositionsFor(cStart, cEnd);
  }

  @Override
  public Color getColourForGroup()
  {
    return clist.getColourForGroup();
  }
}
