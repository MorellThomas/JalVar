package jalview.renderer;

import jalview.datamodel.AlignmentAnnotation;
import jalview.renderer.api.AnnotationRendererFactoryI;
import jalview.renderer.api.AnnotationRowRendererI;
import jalview.ws.datamodel.alphafold.PAEContactMatrix;

import java.util.IdentityHashMap;

public class AnnotationRendererFactory implements AnnotationRendererFactoryI
{

  private static AnnotationRendererFactoryI factory = null;

  public static AnnotationRendererFactoryI getRendererFactory()
  {
    if (factory == null)
    {
      factory = new AnnotationRendererFactory();
    }
    return factory;
  }

  IdentityHashMap<Object, AnnotationRowRendererI> renderers = new IdentityHashMap<Object, AnnotationRowRendererI>();

  public AnnotationRendererFactory()
  {
    // renderers.put)
  }

  @Override
  public AnnotationRowRendererI getRendererFor(AlignmentAnnotation row)
  {
    if (row.graph == AlignmentAnnotation.CONTACT_MAP)
    {
      // TODO consider configuring renderer/etc according to the type of matrix
      // bound to the annotation row - needs to be looked up in that case
      if (PAEContactMatrix.PAEMATRIX.equals(row.getCalcId()))
      {
        return ContactMapRenderer.newPAERenderer();
      }
      // TODO add potential for configuring renderer directly from the
      // annotation row and/or viewmodel

    }
    return null;
  }

}
