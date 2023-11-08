package jalview.datamodel;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ContactRangeTest
{

  @Test
  public void testContactRangeBean()
  {
    ContactRange cr = new ContactRange();
    cr.update(5, 15, 6, 0.2, 12, 1.5, 3.7);
    Assert.assertEquals(5, cr.getFrom_column());
    Assert.assertEquals(15, cr.getTo_column());
    Assert.assertEquals(6, cr.getMinPos());
    Assert.assertEquals(0.2, cr.getMin());
    Assert.assertEquals(12, cr.getMaxPos());
    Assert.assertEquals(1.5, cr.getMax());
    Assert.assertEquals(3.7, cr.getMean());
    cr.setFrom_column(6);
    Assert.assertEquals(6, cr.getFrom_column());
    cr.setTo_column(16);
    Assert.assertEquals(16, cr.getTo_column());
    cr.setMinPos(7);
    Assert.assertEquals(7, cr.getMinPos());
    cr.setMin(0.4);
    Assert.assertEquals(0.4, cr.getMin());
    cr.setMaxPos(13);
    Assert.assertEquals(13, cr.getMaxPos());
    cr.setMax(2.5);
    Assert.assertEquals(2.5, cr.getMax());
    cr.setMean(3.7);
    Assert.assertEquals(3.7, cr.getMean());
  }
}
