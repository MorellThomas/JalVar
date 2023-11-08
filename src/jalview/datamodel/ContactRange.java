package jalview.datamodel;

/**
 * bean for max/min positions for a given range
 * 
 * @author jprocter
 *
 */
public class ContactRange
{
  int minPos;

  double min;

  int maxPos;

  double max;

  int from_column, to_column;

  private double mean;

  /**
   * update the bean with given values
   * 
   * @param from_column
   * @param to_column
   * @param minPos
   * @param min
   * @param maxPos
   * @param max
   */
  public void update(int from_column, int to_column, int minPos, double min,
          int maxPos, double max, double mean)
  {
    this.from_column = from_column;
    this.to_column = to_column;
    this.minPos = minPos;
    this.min = min;
    this.maxPos = maxPos;
    this.max = max;
    this.mean = mean;
  }

  /**
   * @return the minPos
   */
  public int getMinPos()
  {
    return minPos;
  }

  /**
   * @param minPos
   *          the minPos to set
   */
  public void setMinPos(int minPos)
  {
    this.minPos = minPos;
  }

  /**
   * @return the min
   */
  public double getMin()
  {
    return min;
  }

  /**
   * @param min
   *          the min to set
   */
  public void setMin(double min)
  {
    this.min = min;
  }

  /**
   * @return the maxPos
   */
  public int getMaxPos()
  {
    return maxPos;
  }

  /**
   * @param maxPos
   *          the maxPos to set
   */
  public void setMaxPos(int maxPos)
  {
    this.maxPos = maxPos;
  }

  /**
   * @return the max
   */
  public double getMax()
  {
    return max;
  }

  /**
   * @param max
   *          the max to set
   */
  public void setMax(double max)
  {
    this.max = max;
  }

  /**
   * @return the mean
   */
  public double getMean()
  {
    return mean;
  }

  /**
   * @param mean
   *          the mean to set
   */
  public void setMean(double mean)
  {
    this.mean = mean;
  }

  /**
   * @return the from_column
   */
  public int getFrom_column()
  {
    return from_column;
  }

  /**
   * @param from_column
   *          the from_column to set
   */
  public void setFrom_column(int from_column)
  {
    this.from_column = from_column;
  }

  /**
   * @return the to_column
   */
  public int getTo_column()
  {
    return to_column;
  }

  /**
   * @param to_column
   *          the to_column to set
   */
  public void setTo_column(int to_column)
  {
    this.to_column = to_column;
  }
}
