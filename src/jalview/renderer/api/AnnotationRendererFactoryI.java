package jalview.renderer.api;

import jalview.datamodel.AlignmentAnnotation;

public interface AnnotationRendererFactoryI
{

  AnnotationRowRendererI getRendererFor(AlignmentAnnotation row);

}
