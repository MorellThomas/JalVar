package jalview.renderer;

import java.util.Iterator;

import jalview.datamodel.ColumnSelection;
import jalview.datamodel.ContactListI;
import jalview.datamodel.HiddenColumns;
import jalview.renderer.ContactGeometry.contactInterval;

/**
 * encapsulate logic for mapping between positions in a ContactList and their
 * rendered representation in a given number of pixels.
 * 
 * @author jprocter
 *
 */
public class ContactGeometry
{

  final ContactListI contacts;

  final int pixels_step;

  final double contacts_per_pixel;

  final int contact_height;

  final int graphHeight;

  public ContactGeometry(final ContactListI contacts, int graphHeight)
  {
    this.contacts = contacts;
    this.graphHeight = graphHeight;
    contact_height = contacts.getContactHeight();
    // fractional number of contacts covering each pixel
    contacts_per_pixel = (graphHeight < 1) ? contact_height
            : ((double) contact_height) / ((double) graphHeight);

    if (contacts_per_pixel >= 1)
    {
      // many contacts rendered per pixel
      pixels_step = 1;
    }
    else
    {
      // pixel height for each contact
      pixels_step = (int) Math
              .ceil(((double) graphHeight) / (double) contact_height);
    }
  }

  public class contactInterval
  {
    public contactInterval(int cStart, int cEnd, int pStart, int pEnd)
    {
      this.cStart = cStart;
      this.cEnd = cEnd;
      this.pStart = pStart;
      this.pEnd = pEnd;
    }

    // range on contact list
    public final int cStart;

    public final int cEnd;

    // range in pixels
    public final int pStart;

    public final int pEnd;

  }

  /**
   * 
   * @param columnSelection
   * @param ci
   * @param visibleOnly
   *          - when true, only test intersection of visible columns given
   *          matrix range
   * @return true if the range on the matrix specified by ci intersects with
   *         selected columns in the ContactListI's reference frame.
   */

  boolean intersects(contactInterval ci, ColumnSelection columnSelection,
          HiddenColumns hiddenColumns, boolean visibleOnly)
  {
    boolean rowsel = false;
    final int[] mappedRange = contacts.getMappedPositionsFor(ci.cStart,
            ci.cEnd);
    if (mappedRange == null)
    {
      return false;
    }
    for (int p = 0; p < mappedRange.length && !rowsel; p += 2)
    {
      boolean containsHidden = false;
      if (visibleOnly && hiddenColumns != null
              && hiddenColumns.hasHiddenColumns())
      {
        // TODO: turn into function on hiddenColumns and create test !!
        Iterator<int[]> viscont = hiddenColumns.getVisContigsIterator(
                mappedRange[p], mappedRange[p + 1], false);
        containsHidden = !viscont.hasNext();
        if (!containsHidden)
        {
          for (int[] interval = viscont.next(); viscont
                  .hasNext(); rowsel |= columnSelection
                          .intersects(interval[p], interval[p + 1]))
            ;
        }
      }
      else
      {
        rowsel = columnSelection.intersects(mappedRange[p],
                mappedRange[p + 1]);
      }
    }
    return rowsel;

  }

  /**
   * 
   * @param pStart
   * @param pEnd
   * @return range for
   */
  public contactInterval mapFor(int pStart, int pEnd)
  {
    int cStart = (int) Math.floor(pStart * contacts_per_pixel);
    contactInterval ci = new contactInterval(cStart,
            (int) Math.min(contact_height,
                    Math.ceil(
                            cStart + (pEnd - pStart) * contacts_per_pixel)),
            pStart, pEnd);

    return ci;
  }

  /**
   * return the cell containing given pixel
   * 
   * @param pCentre
   * @return range for pCEntre
   */
  public contactInterval mapFor(int pCentre)
  {
    int pStart = Math.max(pCentre - pixels_step, 0);
    int pEnd = Math.min(pStart + pixels_step, graphHeight);
    int cStart = (int) Math.floor(pStart * contacts_per_pixel);
    contactInterval ci = new contactInterval(cStart,
            (int) Math.min(contact_height,
                    Math.ceil(cStart + (pixels_step) * contacts_per_pixel)),
            pStart, pEnd);

    return ci;
  }

  public Iterator<contactInterval> iterateOverContactIntervals(
          int graphHeight)
  {
    // NOT YET IMPLEMENTED
    return null;
    // int cstart = 0, cend;
    //
    // for (int ht = y2,
    // eht = y2 - graphHeight; ht >= eht; ht -= pixels_step)
    // {
    // cstart = (int) Math.floor(((double) y2 - ht) * contacts_per_pixel);
    // cend = (int) Math.min(contact_height,
    // Math.ceil(cstart + contacts_per_pixel * pixels_step));
    //
    // return new Iterator<contactIntervals>() {
    //
    // @Override
    // public boolean hasNext()
    // {
    // // TODO Auto-generated method stub
    // return false;
    // }
    //
    // @Override
    // public contactIntervals next()
    // {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // }
  }
}