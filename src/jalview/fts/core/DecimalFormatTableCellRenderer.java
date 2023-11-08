/*
 * Jalview - A Sequence Alignment Editor and Viewer ($$Version-Rel$$)
 * Copyright (C) $$Year-Rel$$ The Jalview Authors
 * 
 * This file is part of Jalview.
 * 
 * Jalview is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * Jalview is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Jalview.  If not, see <http://www.gnu.org/licenses/>.
 * The Jalview Authors are detailed in the 'AUTHORS' file.
 */
package jalview.fts.core;

import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * The class to handle the formatting of the double values for JTable cells.
 */
public class DecimalFormatTableCellRenderer extends DefaultTableCellRenderer
{
  private DecimalFormat formatter;

  public DecimalFormatTableCellRenderer(boolean isFormated,
          int significantFigures)
  {
    String integerFormater = isFormated ? "###,##0" : "0";
    String fractionFormater = isFormated ? "###,##0." : "0.";
    if (significantFigures > 0)
    {
      StringBuilder significantFigureBuilder = new StringBuilder();
      for (int x = 1; x <= significantFigures; ++x)
      {
        significantFigureBuilder.append("0");
      }
      formatter = new DecimalFormat(
              fractionFormater + significantFigureBuilder.toString());
    }
    else
    {
      formatter = new DecimalFormat(integerFormater);
    }
    super.setHorizontalAlignment(JLabel.RIGHT);
  }

  public DecimalFormatTableCellRenderer()
  {
    super.setHorizontalAlignment(JLabel.RIGHT);
  }

  /**
   * Adapts the default method to ensure that double values are formatted for
   * display
   */
  @Override
  public Component getTableCellRendererComponent(JTable table, Object value,
          boolean isSelected, boolean hasFocus, int row, int column)
  {
    value = value == null ? "" : formatter.format(value);

    return super.getTableCellRendererComponent(table, value, isSelected,
            hasFocus, row, column);
  }
}
