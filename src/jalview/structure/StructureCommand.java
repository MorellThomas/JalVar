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
package jalview.structure;

import java.util.ArrayList;
import java.util.List;

public class StructureCommand implements StructureCommandI
{
  private String command;

  private List<String> parameters;

  public StructureCommand(String cmd, String... params)
  {
    command = cmd;
    if (params != null)
    {
      for (String p : params)
      {
        addParameter(p);
      }
    }
  }

  @Override
  public void addParameter(String param)
  {
    if (parameters == null)
    {
      parameters = new ArrayList<>();
    }
    parameters.add(param);
  }

  @Override
  public String getCommand()
  {
    return command;
  }

  @Override
  public List<String> getParameters()
  {
    return parameters;
  }

  @Override
  public boolean hasParameters()
  {
    return parameters != null && !parameters.isEmpty();
  }

  @Override
  public String toString()
  {
    if (!hasParameters())
    {
      return command;
    }
    StringBuilder sb = new StringBuilder(32);
    sb.append(command).append("(");
    boolean first = true;
    for (String p : parameters)
    {
      if (!first)
      {
        sb.append(",");
      }
      first = false;
      sb.append(p);
    }
    sb.append(")");
    return sb.toString();
  }

  @Override
  public int hashCode()
  {
    int h = command.hashCode();
    if (parameters != null)
    {
      for (String p : parameters)
      {
        h = h * 37 + p.hashCode();
      }
    }
    return h;
  }

  /**
   * Answers true if {@code obj} is a {@code StructureCommand} with the same
   * command and parameters as this one, else false
   */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null || !(obj instanceof StructureCommand))
    {
      return false;
    }
    StructureCommand sc = (StructureCommand) obj;

    if (!command.equals(sc.command))
    {
      return false;
    }
    if (parameters == null || sc.parameters == null)
    {
      return (parameters == null) && (sc.parameters == null);
    }

    int j = parameters.size();
    if (j != sc.parameters.size())
    {
      return false;
    }
    for (int i = 0; i < j; i++)
    {
      if (!parameters.get(i).equals(sc.parameters.get(i)))
      {
        return false;
      }
    }
    return true;
  }

}
