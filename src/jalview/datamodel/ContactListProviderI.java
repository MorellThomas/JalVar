package jalview.datamodel;

import java.awt.Color;

public interface ContactListProviderI
{

  /**
   * 
   * @return position index for this contact List (usually sequence position or
   *         alignment column)
   */
  int getPosition();

  /**
   * dimension of list where getContactAt(column<getContactHeight()) may return
   * a value
   * 
   * @return
   */
  int getContactHeight();

  /**
   * get a value representing contact at column for this site
   * 
   * @param column
   * @return Double.NaN or a contact strength for this site
   */
  double getContactAt(int column);

  /**
   * Return positions in local reference corresponding to cStart and cEnd in
   * matrix data. Positions are base 1 column indices for sequence associated
   * matrices.
   * 
   * @param cStart
   * @param cEnd
   * @return int[] { start, end (inclusive) for each contiguous segment}
   */
  default int[] getMappedPositionsFor(int cStart, int cEnd)
  {
    return new int[] { cStart, cEnd };
  }

  default Color getColourForGroup()
  {
    return null;
  }

}
