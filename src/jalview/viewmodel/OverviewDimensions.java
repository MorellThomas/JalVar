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
package jalview.viewmodel;

import java.awt.Graphics;

import jalview.api.AlignmentColsCollectionI;
import jalview.api.AlignmentRowsCollectionI;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.HiddenSequences;

public abstract class OverviewDimensions
{
  protected static final int MAX_WIDTH = 400;

  protected static final int MIN_WIDTH = 120;

  protected static final int MIN_SEQ_HEIGHT = 40;

  protected static final int MAX_SEQ_HEIGHT = 300;

  private static final int DEFAULT_GRAPH_HEIGHT = 20;

  protected int width;

  protected int sequencesHeight;

  protected int graphHeight = DEFAULT_GRAPH_HEIGHT;

  protected int boxX = -1;

  protected int boxY = -1;

  protected int boxWidth = -1;

  protected int boxHeight = -1;

  protected int alwidth;

  protected int alheight;

  protected float widthRatio;

  protected float heightRatio;

  /**
   * Create an OverviewDimensions object
   * 
   * @param ranges
   *          positional properties of the viewport
   * @param showAnnotationPanel
   *          true if the annotation panel is to be shown, false otherwise
   */
  public OverviewDimensions(ViewportRanges ranges,
          boolean showAnnotationPanel)
  {
    // scale the initial size of overviewpanel to shape of alignment
    float initialScale = (float) ranges.getAbsoluteAlignmentWidth()
            / (float) ranges.getAbsoluteAlignmentHeight();

    if (!showAnnotationPanel)
    {
      graphHeight = 0;
    }

    if (ranges.getAbsoluteAlignmentWidth() > ranges
            .getAbsoluteAlignmentHeight())
    {
      // wider
      width = MAX_WIDTH;
      sequencesHeight = Math.round(MAX_WIDTH / initialScale);
      if (sequencesHeight < MIN_SEQ_HEIGHT)
      {
        sequencesHeight = MIN_SEQ_HEIGHT;
      }
    }
    else
    {
      // taller
      width = Math.round(MAX_WIDTH * initialScale);
      sequencesHeight = MAX_SEQ_HEIGHT;

      if (width < MIN_WIDTH)
      {
        width = MIN_WIDTH;
      }
    }
  }

  /**
   * Draw the overview panel's viewport box on a graphics object
   * 
   * @param g
   *          the graphics object to draw on
   */
  public void drawBox(Graphics g)
  {
    g.drawRect(boxX, boxY, boxWidth, boxHeight);
    g.drawRect(boxX + 1, boxY + 1, boxWidth - 2, boxHeight - 2);
  }

  public int getBoxX()
  {
    return boxX;
  }

  public int getBoxY()
  {
    return boxY;
  }

  public int getBoxWidth()
  {
    return boxWidth;
  }

  public int getBoxHeight()
  {
    return boxHeight;
  }

  public int getWidth()
  {
    return width;
  }

  public int getHeight()
  {
    return sequencesHeight + graphHeight;
  }

  public int getSequencesHeight()
  {
    return sequencesHeight;
  }

  public int getGraphHeight()
  {
    return graphHeight;
  }

  public float getPixelsPerCol()
  {
    resetAlignmentDims();
    return 1 / widthRatio;
  }

  public float getPixelsPerSeq()
  {
    resetAlignmentDims();
    return 1 / heightRatio;
  }

  public void setWidth(int w)
  {
    width = w < 1 ? 1 : w;
    widthRatio = (float) alwidth / width;
  }

  public void setHeight(int h)
  {
    sequencesHeight = (h < 1 ? 1 : h) - graphHeight;
    heightRatio = (float) alheight / sequencesHeight;
  }

  /**
   * Update the viewport location from a mouse click in the overview panel
   * 
   * @param mousex
   *          x location of mouse
   * @param mousey
   *          y location of mouse
   * @param hiddenSeqs
   *          the alignment's hidden sequences
   * @param hiddenCols
   *          the alignment's hidden columns
   */
  public abstract void updateViewportFromMouse(int mousex, int mousey,
          HiddenSequences hiddenSeqs, HiddenColumns hiddenCols);

  /**
   * Update the viewport location from a mouse drag within the overview's box
   * 
   * @param mousex
   *          x location of mouse
   * @param mousey
   *          y location of mouse
   * @param hiddenSeqs
   *          the alignment's hidden sequences
   * @param hiddenCols
   *          the alignment's hidden columns
   */
  public abstract void adjustViewportFromMouse(int mousex, int mousey,
          HiddenSequences hiddenSeqs, HiddenColumns hiddenCols);

  /**
   * Initialise dragging from the mouse - must be called on initial mouse click
   * before using adjustViewportFromMouse in drag operations
   * 
   * @param mousex
   *          x location of mouse
   * @param mousey
   *          y location of mouse
   * @param hiddenSeqs
   *          the alignment's hidden sequences
   * @param hiddenCols
   *          the alignment's hidden columns
   */
  public abstract void setDragPoint(int x, int y,
          HiddenSequences hiddenSeqs, HiddenColumns hiddenCols);

  /*
   * Move the viewport so that the top left corner of the overview's box 
   * is at the mouse position (leftx, topy)
   */
  protected abstract void updateViewportFromTopLeft(int leftx, int topy,
          HiddenSequences hiddenSeqs, HiddenColumns hiddenCols);

  /**
   * Set the overview panel's box position to match the viewport
   * 
   * @param hiddenSeqs
   *          the alignment's hidden sequences
   * @param hiddenCols
   *          the alignment's hidden columns
   */
  public abstract void setBoxPosition(HiddenSequences hiddenSeqs,
          HiddenColumns hiddenCols);

  /**
   * Get the collection of columns used by this overview dimensions object
   * 
   * @param hiddenCols
   *          the alignment's hidden columns
   * @return a column collection
   */
  public abstract AlignmentColsCollectionI getColumns(AlignmentI al);

  /**
   * Get the collection of rows used by this overview dimensions object
   * 
   * @param al
   *          the alignment
   * @return a row collection
   */
  public abstract AlignmentRowsCollectionI getRows(AlignmentI al);

  /**
   * Updates overview dimensions to account for current alignment dimensions
   */
  protected abstract void resetAlignmentDims();

  /*
   * Given the box coordinates in residues and sequences, set the box dimensions in the overview window
   */
  protected void setBoxPosition(int startRes, int startSeq, int vpwidth,
          int vpheight)
  {
    resetAlignmentDims();

    // boxX, boxY is the x,y location equivalent to startRes, startSeq
    int xPos = Math.min(startRes, alwidth - vpwidth + 1);
    boxX = Math.round(xPos / widthRatio);
    boxY = Math.round(startSeq / heightRatio);

    // boxWidth is the width in residues translated to pixels
    boxWidth = Math.round(vpwidth / widthRatio);

    // boxHeight is the height in sequences translated to pixels
    boxHeight = Math.round(vpheight / heightRatio);
  }

  /**
   * Answers if a mouse position is in the overview's red box
   * 
   * @param x
   *          mouse x position
   * @param y
   *          mouse y position
   * @return true if (x,y) is inside the box
   */
  public boolean isPositionInBox(int x, int y)
  {
    return (x > boxX && y > boxY && x < boxX + boxWidth
            && y < boxY + boxHeight);
  }

  /*
   * Given the centre x position, calculate the box's left x position
   */
  protected abstract int getLeftXFromCentreX(int mousex,
          HiddenColumns hidden);

  /*
   * Given the centre y position, calculate the box's top y position
   */
  protected abstract int getTopYFromCentreY(int mousey,
          HiddenSequences hidden);

}