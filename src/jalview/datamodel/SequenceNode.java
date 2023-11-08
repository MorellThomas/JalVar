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
package jalview.datamodel;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class SequenceNode extends BinaryNode<SequenceI>
{
  private boolean placeholder = false;

  /**
   * Creates a new SequenceNode object.
   */
  public SequenceNode()
  {
    super();
  }

  public SequenceNode(SequenceI val, BinaryNode<SequenceI> parent,
          String name, double dist, int bootstrap, boolean dummy)
  {
    super(val, parent, name, dist, bootstrap, dummy);
  }

  public SequenceNode(SequenceI element, BinaryNode<SequenceI> parent,
          String name, double dist, int bootstrap)
  {
    super(element, parent, name, dist, bootstrap);
  }

  public SequenceNode(SequenceI element, BinaryNode<SequenceI> parent,
          String name, double dist)
  {
    super(element, parent, name, dist);
  }

  /*
   * @param placeholder is true if the sequence refered to in the element node
   * is not actually present in the associated alignment
   */
  public boolean isPlaceholder()
  {
    return placeholder;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param Placeholder
   *          DOCUMENT ME!
   */
  public void setPlaceholder(boolean Placeholder)
  {
    this.placeholder = Placeholder;
  }

  /**
   * test if this node has a name that might be a label rather than a bootstrap
   * value
   * 
   * @return true if node has a non-numeric label
   */
  public boolean isSequenceLabel()
  {
    if (name != null && name.length() > 0)
    {
      for (int c = 0, s = name.length(); c < s; c++)
      {
        char q = name.charAt(c);
        if ('0' <= q && q <= '9')
        {
          continue;
        }
        return true;
      }
    }
    return false;
  }
}
