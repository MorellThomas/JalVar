package jalview.util.imagemaker;

public class BitmapImageSizing
{
  public final float scale;

  public final int width;

  public final int height;

  public BitmapImageSizing(float scale, int width, int height)
  {
    this.scale = scale;
    this.width = width;
    this.height = height;
  }

  public static BitmapImageSizing nullBitmapImageSizing()
  {
    return new BitmapImageSizing(0.0f, 0, 0);
  }
}
