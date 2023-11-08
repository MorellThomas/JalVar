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
package jalview.ws.jws2.dm;

import jalview.util.MessageManager;
import jalview.ws.params.ValueConstrainI;

import compbio.metadata.ValueConstrain;

public class JabaValueConstrain implements ValueConstrainI
{

  ValueConstrain vc = null;

  public JabaValueConstrain(ValueConstrain vc)
  {
    this.vc = vc;
  }

  @Override
  public ValueType getType()
  {
    if (vc.getType() == ValueConstrain.Type.Float)
    {
      return ValueType.Float;
    }
    if (vc.getType() == ValueConstrain.Type.Integer)
    {
      return ValueType.Integer;
    }
    throw new Error(MessageManager.formatMessage(
            "error.implementation_error_valuetype_doesnt_support_jabaws_type",
            new String[]
            { vc.toString() }));
  }

  @Override
  public Number getMax()
  {
    return vc.getMax();
  }

  @Override
  public Number getMin()
  {
    return vc.getMin();
  }

}
