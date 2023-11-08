package jalview.datamodel;

import java.awt.Color;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;

import jalview.util.MapList;
import jalview.ws.datamodel.alphafold.MappableContactMatrix;
/**
 * Dummy contact matrix based on sequence distance
 * 
 * @author jprocter
 *
 */
public class SeqDistanceContactMatrix
        extends MappableContactMatrix<SeqDistanceContactMatrix>
        implements ContactMatrixI
{
  private static final String SEQUENCE_DISTANCE = "SEQUENCE_DISTANCE";
  private int width = 0;

  public SeqDistanceContactMatrix(int width)
  {
    this.width = width;
  }

  @Override
  public float getMin()
  {
    return 0f;
  }

  @Override
  public float getMax()
  {
    return width;
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

      // @Override
      // public Color getColorForScore(int column)
      // {
      // return jalview.util.ColorUtils.getGraduatedColour(Math.abs(column-p),
      // 0, Color.white, width, Color.magenta);
      // }
      // @Override
      // public Color getColorForRange(int from_column, int to_column)
      // {
      // return jalview.util.ColorUtils.getGraduatedColour(
      // Math.abs(to_column + from_column - 2 * p) / 2, 0, Color.white, width,
      // Color.magenta);
      // }

      @Override
      public int getContactHeight()
      {
        return width;

      }

      @Override
      public int getPosition()
      {
        return p;
      }

      @Override
      public double getContactAt(int column)
      {
        return Math.abs(column - p);
      }
    });
  }

  @Override
  public String getAnnotDescr()
  {
    return "Sequence distance matrix";
  }

  @Override
  public String getAnnotLabel()
  {
    return "Sequence Distance";
  }

  @Override
  public String getType()
  {
    return SEQUENCE_DISTANCE;
  }

  @Override
  public int getWidth()
  {
    return width;
  }

  @Override
  public int getHeight()
  {
    return width;
  }
  @Override
  protected double getElementAt(int _column, int i)
  {
    return Math.abs(_column - i);
  }
  @Override
  protected SeqDistanceContactMatrix newMappableContactMatrix(
          SequenceI newRefSeq, MapList newFromMapList)
  {

    return new SeqDistanceContactMatrix(width);
  }
}
