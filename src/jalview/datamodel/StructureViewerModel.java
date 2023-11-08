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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A data bean to hold stored data about a structure viewer.
 */
public class StructureViewerModel
{
  private int x;

  private int y;

  private int width;

  private int height;

  private boolean alignWithPanel;

  private boolean colourWithAlignPanel;

  private boolean colourByViewer;

  private String stateData = "";

  private String viewId;

  // CHIMERA or JMOL (for projects from Jalview 2.9 on)
  private String type;

  private Map<File, StructureData> fileData = new HashMap<File, StructureData>();

  public class StructureData
  {
    private String filePath;

    private String pdbId;

    private List<SequenceI> seqList;

    // TODO and possibly a list of chains?

    /**
     * Constructor given structure file path and id.
     * 
     * @param pdbFile
     * @param id
     */
    public StructureData(String pdbFile, String id)
    {
      this.filePath = pdbFile;
      this.pdbId = id;
      this.seqList = new ArrayList<SequenceI>();
    }

    public String getFilePath()
    {
      return filePath;
    }

    protected void setFilePath(String filePath)
    {
      this.filePath = filePath;
    }

    public String getPdbId()
    {
      return pdbId;
    }

    protected void setPdbId(String pdbId)
    {
      this.pdbId = pdbId;
    }

    public List<SequenceI> getSeqList()
    {
      return seqList;
    }

    protected void setSeqList(List<SequenceI> seqList)
    {
      this.seqList = seqList;
    }
  }

  public StructureViewerModel(int x, int y, int width, int height,
          boolean alignWithPanel, boolean colourWithAlignPanel,
          boolean colourByViewer, String viewId, String type)
  {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.alignWithPanel = alignWithPanel;
    this.colourWithAlignPanel = colourWithAlignPanel;
    this.colourByViewer = colourByViewer;
    this.viewId = viewId;
    this.type = type;
  }

  public int getX()
  {
    return x;
  }

  protected void setX(int x)
  {
    this.x = x;
  }

  public int getY()
  {
    return y;
  }

  protected void setY(int y)
  {
    this.y = y;
  }

  public int getWidth()
  {
    return width;
  }

  protected void setWidth(int width)
  {
    this.width = width;
  }

  public int getHeight()
  {
    return height;
  }

  public void setHeight(int height)
  {
    this.height = height;
  }

  public boolean isAlignWithPanel()
  {
    return alignWithPanel;
  }

  public void setAlignWithPanel(boolean alignWithPanel)
  {
    this.alignWithPanel = alignWithPanel;
  }

  public boolean isColourWithAlignPanel()
  {
    return colourWithAlignPanel;
  }

  public void setColourWithAlignPanel(boolean colourWithAlignPanel)
  {
    this.colourWithAlignPanel = colourWithAlignPanel;
  }

  public boolean isColourByViewer()
  {
    return colourByViewer;
  }

  public void setColourByViewer(boolean colourByViewer)
  {
    this.colourByViewer = colourByViewer;
  }

  public String getStateData()
  {
    return stateData;
  }

  public void setStateData(String stateData)
  {
    this.stateData = stateData;
  }

  public Map<File, StructureData> getFileData()
  {
    return fileData;
  }

  protected void setFileData(Map<File, StructureData> fileData)
  {
    this.fileData = fileData;
  }

  public String getViewId()
  {
    return this.viewId;
  }

  public String getType()
  {
    return this.type;
  }
}
