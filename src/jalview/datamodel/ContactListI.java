package jalview.datamodel;

import java.awt.Color;

import jalview.renderer.ContactGeometry.contactInterval;

public interface ContactListI extends ContactListProviderI
{

  /**
   * return bounds for range
   * 
   * @param from_column
   * @param to_column
   * @return double[] { min, max,
   */
  ContactRange getRangeFor(int from_column, int to_column);

  default Color getColourForGroup()
  {
    return null;
  }
}
