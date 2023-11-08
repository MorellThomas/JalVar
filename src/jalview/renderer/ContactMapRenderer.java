/**
 * 
 */
package jalview.renderer;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;

import jalview.api.AlignViewportI;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Annotation;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.ContactListI;
import jalview.datamodel.ContactMatrixI;
import jalview.datamodel.ContactRange;
import jalview.datamodel.HiddenColumns;
import jalview.renderer.api.AnnotationRowRendererI;

/**
 * @author jprocter
 *
 */
public abstract class ContactMapRenderer implements AnnotationRowRendererI
{
  /**
   * bean holding colours for shading
   * 
   * @author jprocter
   *
   */
  public class Shading
  {
    /**
     * shown when no data available from map
     */
    Color no_data;
    /**
     * shown for region not currently visible - should normally not see this
     */
    Color hidden;
    /**
     * linear shading scheme min/max
     */
    Color maxColor, minColor;

    /**
     * linear shading scheme min/max for selected region
     */
    Color selMinColor, selMaxColor;

    public Shading(Color no_data, Color hidden, Color maxColor,
            Color minColor, Color selMinColor, Color selMaxColor)
    {
      super();
      this.no_data = no_data;
      this.hidden = hidden;
      this.maxColor = maxColor;
      this.minColor = minColor;
      this.selMinColor = selMinColor;
      this.selMaxColor = selMaxColor;
    }

  }

  final Shading shade;

  /**
   * build an EBI-AlphaFold style renderer of PAE matrices
   * 
   * @return
   */
  public static ContactMapRenderer newPAERenderer()
  {
    return new ContactMapRenderer()
    {
      @Override
      public Shading getShade()
      {
        return new Shading(Color.pink, Color.red,

                new Color(246, 252, 243), new Color(0, 60, 26),
                new Color(26, 0, 60), new Color(243, 246, 252));
      }
    };
  }

  /**
   * 
   * @return instance of Shading used to initialise the renderer
   */
  public abstract Shading getShade();

  public ContactMapRenderer()
  {
    this.shade = getShade();
  }

  @Override
  public void renderRow(Graphics g, int charWidth, int charHeight,
          boolean hasHiddenColumns, AlignViewportI viewport,
          HiddenColumns hiddenColumns, ColumnSelection columnSelection,
          AlignmentAnnotation _aa, Annotation[] aa_annotations, int sRes,
          int eRes, float min, float max, int y)
  {    
    if (sRes > aa_annotations.length)
    {
      return;
    }
    eRes = Math.min(eRes, aa_annotations.length);

    int x = 0, topY = y;

    // uncomment below to render whole area of matrix as pink
    // g.setColor(shade.no_data);
    // g.fillRect(x, topY-_aa.height, (eRes - sRes) * charWidth, _aa.graphHeight);
    
    boolean showGroups = _aa.isShowGroupsForContactMatrix();
    int column;
    int aaMax = aa_annotations.length - 1;
    ContactMatrixI cm = viewport.getContactMatrix(_aa);
    while (x < eRes - sRes)
    {
      column = sRes + x;
      if (hasHiddenColumns)
      {
        column = hiddenColumns.visibleToAbsoluteColumn(column);
      }
      // TODO: highlight columns selected
      boolean colsel = false;
      if (columnSelection != null)
      {
        colsel = columnSelection.contains(column);
      }

      if (column > aaMax)
      {
        break;
      }

      if (aa_annotations[column] == null)
      {
        x++;
        continue;
      }
      ContactListI contacts = viewport.getContactList(_aa, column);
      if (contacts == null)
      {
        x++;
        continue;
      }
      // ContactListI from viewport can map column -> group
      Color gpcol = (cm == null) ? Color.white
              : contacts.getColourForGroup(); // cm.getColourForGroup(cm.getGroupsFor(column));
      // feature still in development - highlight or omit regions hidden in
      // the alignment - currently marks them as red rows
      boolean maskHiddenCols = false;
      // TODO: optionally pass visible column mask to the ContactGeometry object
      // so it maps
      // only visible contacts to geometry
      // Bean holding mapping from contact list to pixels
      // TODO: allow bracketing/limiting of range on contacts to render (like
      // visible column mask but more flexible?)

      // COntactListI provides mapping for column -> cm-groupmapping
      final ContactGeometry cgeom = new ContactGeometry(contacts,
              _aa.graphHeight);

      for (int ht = 0, botY = topY
              - _aa.height; ht < _aa.graphHeight; ht += cgeom.pixels_step)
      {
        ContactGeometry.contactInterval ci = cgeom.mapFor(ht,
                ht + cgeom.pixels_step);
        // cstart = (int) Math.floor(((double) y2 - ht) * contacts_per_pixel);
        // cend = (int) Math.min(contact_height,
        // Math.ceil(cstart + contacts_per_pixel * pixels_step));

        Color col;
        boolean rowsel = false, containsHidden = false;
        if (columnSelection != null)
        {
          rowsel = cgeom.intersects(ci, columnSelection, hiddenColumns,
                  maskHiddenCols);
        }
        // TODO: show selected region
        if (colsel || rowsel)
        {

          col = getSelectedColorForRange(min, max, contacts, ci.cStart,
                  ci.cEnd);
          if (colsel && rowsel)
          {
            col = new Color(col.getBlue(), col.getGreen(), col.getRed());
          }
          else
          {
            col = new Color(col.getBlue(), col.getBlue(), col.getBlue());
          }
        }
        else
        {
          col = getColorForRange(min, max, contacts, ci.cStart, ci.cEnd);
        }
        if (containsHidden)
        {
          col = shade.hidden;
        }
        if (showGroups && gpcol != null && gpcol != Color.white)
        {
          // todo - could overlay group as a transparent rectangle ?
          col = new Color(
                  (int) (((float) (col.getRed() + gpcol.getRed())) / 2f),
                  (int) (((float) (col.getGreen() + gpcol.getGreen()))
                          / 2f),
                  (int) (((float) (col.getBlue() + gpcol.getBlue())) / 2f));
        }
        g.setColor(col);
        if (cgeom.pixels_step > 1)
        {
          g.fillRect(x * charWidth, botY+ht, charWidth, 1 + cgeom.pixels_step);
        }
        else
        {
          g.drawLine(x * charWidth, botY+ht, (x + 1) * charWidth, botY+ht);
        }
      }
      x++;
    }

  }

  Color shadeFor(float min, float max, float value)
  {
    return jalview.util.ColorUtils.getGraduatedColour(value, 0,
            shade.minColor, max, shade.maxColor);
  }

  public Color getColorForRange(float min, float max, ContactListI cl,
          int i, int j)
  {
    ContactRange cr = cl.getRangeFor(i, j);
    // average for moment - probably more interested in maxIntProj though
    return shadeFor(min, max, (float) cr.getMean());
  }

  public Color getSelectedColorForRange(float min, float max,
          ContactListI cl, int i, int j)
  {
    ContactRange cr = cl.getRangeFor(i, j);
    // average for moment - probably more interested in maxIntProj though
    return jalview.util.ColorUtils.getGraduatedColour((float) cr.getMean(),
            0, shade.selMinColor, max, shade.selMaxColor);
  }

}
