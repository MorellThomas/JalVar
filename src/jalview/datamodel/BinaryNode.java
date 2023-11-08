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

import java.awt.Color;

/**
 * Represent a node in a binary tree
 * 
 * @author $mclamp (probably!)$
 * @version $Revision$
 */
public class BinaryNode<T>
{
  T element;

  String name;

  BinaryNode<T> left;

  BinaryNode<T> right;

  BinaryNode<T> parent;

  /** Bootstrap value */
  public int bootstrap;

  /** DOCUMENT ME!! */
  public double dist;

  /** DOCUMENT ME!! */
  public int count;

  /** DOCUMENT ME!! */
  public double height;

  /** DOCUMENT ME!! */
  public float ycount;

  /** DOCUMENT ME!! */
  public Color color = Color.black;

  /**
   * if true, node is created to simulate polytomy between parent and its 3 or
   * more children
   */
  public boolean dummy = false;

  /**
   * Creates a new BinaryNode object.
   */
  public BinaryNode()
  {
    left = right = parent = null;
    bootstrap = 0;
    dist = 0;
  }

  /**
   * Creates a new BinaryNode object.
   * 
   * @param element
   *          DOCUMENT ME!
   * @param parent
   *          DOCUMENT ME!
   * @param name
   *          DOCUMENT ME!
   */
  public BinaryNode(T element, BinaryNode<T> parent, String name,
          double dist)
  {
    this();
    this.element = element;
    this.parent = parent;
    this.name = name;
    this.dist = dist;
  }

  public BinaryNode(T element, BinaryNode<T> parent, String name,
          double dist, int bootstrap)
  {
    this(element, parent, name, dist);
    this.bootstrap = bootstrap;
  }

  public BinaryNode(T val, BinaryNode<T> parent, String name, double dist,
          int bootstrap, boolean dummy)
  {
    this(val, parent, name, dist, bootstrap);
    this.dummy = dummy;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public T element()
  {
    return element;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param v
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public T setElement(T v)
  {
    return element = v;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public BinaryNode<T> left()
  {
    return left;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param n
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public BinaryNode<T> setLeft(BinaryNode<T> n)
  {
    return left = n;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public BinaryNode<T> right()
  {
    return right;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param n
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public BinaryNode<T> setRight(BinaryNode<T> n)
  {
    return right = n;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public BinaryNode<T> parent()
  {
    return parent;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param n
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public BinaryNode<T> setParent(BinaryNode<T> n)
  {
    return parent = n;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean isLeaf()
  {
    return (left == null) && (right == null);
  }

  /**
   * attaches FIRST and SECOND node arguments as the LEFT and RIGHT children of
   * this node (removing any old references) a null parameter DOES NOT mean that
   * the pointer to the corresponding child node is set to NULL - you should use
   * setChild(null), or detach() for this.
   * 
   */
  public void SetChildren(BinaryNode<T> leftchild, BinaryNode<T> rightchild)
  {
    if (leftchild != null)
    {
      this.setLeft(leftchild);
      leftchild.detach();
      leftchild.setParent(this);
    }

    if (rightchild != null)
    {
      this.setRight(rightchild);
      rightchild.detach();
      rightchild.setParent(this);
    }
  }

  /**
   * Detaches the node from the binary tree, along with all its child nodes.
   * 
   * @return BinaryNode The detached node.
   */
  public BinaryNode<T> detach()
  {
    if (this.parent != null)
    {
      if (this.parent.left == this)
      {
        this.parent.left = null;
      }
      else
      {
        if (this.parent.right == this)
        {
          this.parent.right = null;
        }
      }
    }

    this.parent = null;

    return this;
  }

  /**
   * Traverses up through the tree until a node with a free leftchild is
   * discovered.
   * 
   * @return BinaryNode
   */
  public BinaryNode<T> ascendLeft()
  {
    BinaryNode<T> c = this;

    do
    {
      c = c.parent();
    } while ((c != null) && (c.left() != null) && !c.left().isLeaf());

    return c;
  }

  /**
   * Traverses up through the tree until a node with a free rightchild is
   * discovered. Jalview builds trees by descent on the left, so this may be
   * unused.
   * 
   * @return BinaryNode
   */
  public BinaryNode<T> ascendRight()
  {
    BinaryNode<T> c = this;

    do
    {
      c = c.parent();
    } while ((c != null) && (c.right() != null) && !c.right().isLeaf());

    return c;
  }

  /**
   * 
   * set the display name
   * 
   * @param new
   *          name
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * 
   * 
   * @return the display name for this node
   */
  public String getName()
  {
    return this.name;
  }

  /**
   * set integer bootstrap value
   * 
   * @param boot
   */
  public void setBootstrap(int boot)
  {
    this.bootstrap = boot;
  }

  /**
   * get bootstrap
   * 
   * @return integer value
   */
  public int getBootstrap()
  {
    return bootstrap;
  }

  /**
   * @param dummy
   *          true if node is created for the representation of polytomous trees
   */
  public boolean isDummy()
  {
    return dummy;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param newstate
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean setDummy(boolean newstate)
  {
    boolean oldstate = dummy;
    dummy = newstate;

    return oldstate;
  }

  /**
   * ascends the tree but doesn't stop until a non-dummy node is discovered.
   * 
   */
  public BinaryNode<T> AscendTree()
  {
    BinaryNode<T> c = this;

    do
    {
      c = c.parent();
    } while ((c != null) && c.dummy);

    return c;
  }
}
