package jalview.datamodel.annotations;

import jalview.datamodel.Annotation;
import jalview.structure.StructureImportSettings;
import jalview.structure.StructureImportSettings.TFType;

public class AlphaFoldAnnotationRowBuilder extends AnnotationRowBuilder
{
  public static final String LABEL = "Alphafold Reliability";

  public AlphaFoldAnnotationRowBuilder()
  {
    super(LABEL);
    min = 0;
    max = 100;
    hasMinMax = true;
    this.setTFType(StructureImportSettings.TFType.PLDDT);
  }

  @Override
  public void processAnnotation(Annotation annotation)
  {
    if (annotation.value > 90)
    {
      // Very High
      annotation.colour = new java.awt.Color(0, 83, 214);
    }
    if (annotation.value <= 90)
    {
      // High
      annotation.colour = new java.awt.Color(101, 203, 243);
    }
    if (annotation.value <= 70)
    {
      // Confident
      annotation.colour = new java.awt.Color(255, 219, 19);
    }
    if (annotation.value < 50)
    {
      annotation.colour = new java.awt.Color(255, 125, 69);
    }
  }
}