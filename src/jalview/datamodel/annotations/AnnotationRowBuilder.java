package jalview.datamodel.annotations;

import jalview.datamodel.Annotation;
import jalview.structure.StructureImportSettings.TFType;

public class AnnotationRowBuilder
{

  String name;

  boolean hasDescription = false;

  String description;

  boolean hasMinMax = false;

  /**
   * the type of temperature factor plot (if it is one)
   */
  // private TFType tfType = TFType.DEFAULT;
  private TFType tfType = null;

  public void setTFType(TFType t)
  {
    tfType = t;
  }

  public TFType getTFType()
  {
    return tfType;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public boolean isHasDescription()
  {
    return hasDescription;
  }

  public void setHasDescription(boolean hasDescription)
  {
    this.hasDescription = hasDescription;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public boolean isHasMinMax()
  {
    return hasMinMax;
  }

  public void setHasMinMax(boolean hasMinMax)
  {
    this.hasMinMax = hasMinMax;
  }

  public float getMin()
  {
    return min;
  }

  public void setMin(float min)
  {
    this.min = min;
  }

  public float getMax()
  {
    return max;
  }

  public void setMax(float max)
  {
    this.max = max;
  }

  float min, max;

  public AnnotationRowBuilder(String string)
  {
    name = string;
  }

  public AnnotationRowBuilder(String name, float min, float max, TFType tft)
  {
    this(name, min, max);
    setTFType(tft);
  }

  public AnnotationRowBuilder(String name, float min, float max)
  {
    this(name);
    this.min = min;
    this.max = max;
    this.hasMinMax = true;
  }

  /**
   * override this to apply some form of transformation to the annotation - eg a
   * colourscheme
   * 
   * @param annotation
   */
  public void processAnnotation(Annotation annotation)
  {

  }
}
