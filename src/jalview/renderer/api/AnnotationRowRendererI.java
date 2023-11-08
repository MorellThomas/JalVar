package jalview.renderer.api;

import jalview.api.AlignViewportI;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Annotation;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.HiddenColumns;

import java.awt.Graphics;

public interface AnnotationRowRendererI
{

  void renderRow(Graphics g, int charWidth, int charHeight,
          boolean hasHiddenColumns, AlignViewportI av,
          HiddenColumns hiddenColumns, ColumnSelection columnSelection,
          AlignmentAnnotation row, Annotation[] row_annotations,
          int startRes, int endRes, float graphMin, float graphMax, int y);

}
