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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import jalview.fts.api.FTSDataColumnI;
import jalview.fts.api.FTSDataColumnI.FTSDataColumnGroupI;
import jalview.fts.api.FTSRestClientI;
import jalview.fts.core.FTSDataColumnPreferences.PreferenceSource;

/**
 * Base class providing implementation for common methods defined in
 * FTSRestClientI
 * 
 * @author tcnofoegbu
 * 
 * @note implementations MUST be accessed as a singleton.
 */
public abstract class FTSRestClient implements FTSRestClientI
{
  protected Collection<FTSDataColumnI> dataColumns = new ArrayList<>();

  protected Collection<FTSDataColumnGroupI> dataColumnGroups = new ArrayList<>();

  protected Collection<FTSDataColumnI> searchableDataColumns = new ArrayList<>();

  protected Collection<FTSDataColumnI> defaulDisplayedDataColumns = new ArrayList<>();

  protected FTSDataColumnI primaryKeyColumn;

  private String primaryKeyColumnCode = null;

  private int defaultResponsePageSize = 100;

  protected HashMap<String, String> mockQueries = null;

  protected FTSRestClient()
  {

  }

  public void parseDataColumnsConfigFile()
  {
    String fileName = getColumnDataConfigFileName();

    InputStream in = getClass().getResourceAsStream(fileName);

    try (BufferedReader br = new BufferedReader(new InputStreamReader(in)))
    {
      String line;
      while ((line = br.readLine()) != null)
      {
        final String[] lineData = line.split(";");
        try
        {
          if (lineData.length == 2)
          {
            if (lineData[0].equalsIgnoreCase("_data_column.primary_key"))
            {
              primaryKeyColumnCode = lineData[1];
            }
            if (lineData[0].equalsIgnoreCase(
                    "_data_column.default_response_page_size"))
            {
              defaultResponsePageSize = Integer.valueOf(lineData[1]);
            }
          }
          else if (lineData.length == 3)
          {
            dataColumnGroups.add(new FTSDataColumnGroupI()
            {
              @Override
              public String getID()
              {
                return lineData[0];
              }

              @Override
              public String getName()
              {
                return lineData[1];
              }

              @Override
              public int getSortOrder()
              {
                return Integer.valueOf(lineData[2]);
              }

              @Override
              public String toString()
              {
                return lineData[1];
              }

              @Override
              public int hashCode()
              {
                return Objects.hash(this.getID(), this.getName(),
                        this.getSortOrder());
              }

              @Override
              public boolean equals(Object otherObject)
              {
                FTSDataColumnGroupI that = (FTSDataColumnGroupI) otherObject;
                return this.getID().equals(that.getID())
                        && this.getName().equals(that.getName())
                        && this.getSortOrder() == that.getSortOrder();
              }
            });
          }
          else if (lineData.length > 6)
          {
            FTSDataColumnI dataCol = new FTSDataColumnI()
            {
              @Override
              public String toString()
              {
                return getName();
              }

              @Override
              public String getName()
              {
                return lineData[0];
              }

              @Override
              public String getCode()
              {
                return lineData[1].split("\\|")[0];
              }

              @Override
              public String getAltCode()
              {
                return lineData[1].split("\\|").length > 1
                        ? lineData[1].split("\\|")[1]
                        : getCode();
              }

              @Override
              public DataTypeI getDataType()
              {
                final String[] dataTypeString = lineData[2].split("\\|");
                final String classString = dataTypeString[0]
                        .toUpperCase(Locale.ROOT);

                return new DataTypeI()
                {

                  @Override
                  public boolean isFormtted()
                  {
                    if (dataTypeString.length > 1
                            && dataTypeString[1] != null)
                    {
                      switch (dataTypeString[1].toUpperCase(Locale.ROOT))
                      {
                      case "T":
                      case "TRUE":
                        return true;
                      case "F":
                      case "False":
                      default:
                        return false;
                      }
                    }
                    return false;
                  }

                  @Override
                  public int getSignificantFigures()
                  {
                    if (dataTypeString.length > 2
                            && dataTypeString[2] != null)
                    {
                      return Integer.valueOf(dataTypeString[2]);
                    }
                    return 0;
                  }

                  @Override
                  public Class getDataTypeClass()
                  {
                    switch (classString)
                    {
                    case "INT":
                    case "INTEGER":
                      return Integer.class;
                    case "DOUBLE":
                      return Double.class;
                    case "STRING":
                    default:
                      return String.class;
                    }
                  }
                };

              }

              @Override
              public FTSDataColumnGroupI getGroup()
              {
                FTSDataColumnGroupI group = null;
                try
                {
                  group = getDataColumnGroupById(lineData[3]);
                } catch (Exception e)
                {
                  e.printStackTrace();
                }
                return group;
              }

              @Override
              public int getMinWidth()
              {
                return Integer.valueOf(lineData[4]);
              }

              @Override
              public int getMaxWidth()
              {
                return Integer.valueOf(lineData[5]);
              }

              @Override
              public int getPreferredWidth()
              {
                return Integer.valueOf(lineData[6]);
              }

              @Override
              public boolean isPrimaryKeyColumn()
              {
                return getName().equalsIgnoreCase(primaryKeyColumnCode)
                        || getCode().equalsIgnoreCase(primaryKeyColumnCode);
              }

              @Override
              public boolean isVisibleByDefault()
              {
                return Boolean.valueOf(lineData[7]);
              }

              @Override
              public boolean isSearchable()
              {
                return Boolean.valueOf(lineData[8]);
              }

              @Override
              public int hashCode()
              {
                return Objects.hash(this.getName(), this.getCode(),
                        this.getGroup());
              }

              @Override
              public boolean equals(Object otherObject)
              {
                FTSDataColumnI that = (FTSDataColumnI) otherObject;
                return otherObject == null ? false
                        : this.getCode().equals(that.getCode())
                                && this.getName().equals(that.getName())
                                && this.getGroup().equals(that.getGroup());
              }

            };
            dataColumns.add(dataCol);

            if (dataCol.isSearchable())
            {
              searchableDataColumns.add(dataCol);
            }

            if (dataCol.isVisibleByDefault())
            {
              defaulDisplayedDataColumns.add(dataCol);
            }

          }
          else
          {
            continue;
          }
        } catch (Exception e)
        {
          e.printStackTrace();
        }
      }
      try
      {
        this.primaryKeyColumn = getDataColumnByNameOrCode(
                primaryKeyColumnCode);
      } catch (Exception e)
      {
        e.printStackTrace();
      }
    } catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  @Override
  public int getPrimaryKeyColumIndex(
          Collection<FTSDataColumnI> wantedFields, boolean hasRefSeq)
          throws Exception
  {

    // If a reference sequence is attached then start counting from 1 else
    // start from zero
    int pdbFieldIndexCounter = hasRefSeq ? 1 : 0;

    for (FTSDataColumnI field : wantedFields)
    {
      if (field.isPrimaryKeyColumn())
      {
        break; // Once PDB Id index is determined exit iteration
      }
      ++pdbFieldIndexCounter;
    }
    return pdbFieldIndexCounter;
  }

  @Override
  public String getDataColumnsFieldsAsCommaDelimitedString(
          Collection<FTSDataColumnI> dataColumnFields)
  {
    String result = "";
    if (dataColumnFields != null && !dataColumnFields.isEmpty())
    {
      StringBuilder returnedFields = new StringBuilder();
      for (FTSDataColumnI field : dataColumnFields)
      {
        returnedFields.append(",").append(field.getCode());
      }
      returnedFields.deleteCharAt(0);
      result = returnedFields.toString();
    }
    return result;
  }

  @Override
  public Collection<FTSDataColumnI> getAllFTSDataColumns()
  {
    if (dataColumns == null || dataColumns.isEmpty())
    {
      parseDataColumnsConfigFile();
    }
    return dataColumns;
  }

  @Override
  public Collection<FTSDataColumnI> getSearchableDataColumns()
  {
    if (searchableDataColumns == null || searchableDataColumns.isEmpty())
    {
      parseDataColumnsConfigFile();
    }
    return searchableDataColumns;
  }

  @Override
  public Collection<FTSDataColumnI> getAllDefaultDisplayedFTSDataColumns()
  {
    if (defaulDisplayedDataColumns == null
            || defaulDisplayedDataColumns.isEmpty())
    {
      parseDataColumnsConfigFile();
    }
    return defaulDisplayedDataColumns;
  }

  @Override
  public FTSDataColumnI getPrimaryKeyColumn()
  {
    if (defaulDisplayedDataColumns == null
            || defaulDisplayedDataColumns.isEmpty())
    {
      parseDataColumnsConfigFile();
    }
    return primaryKeyColumn;
  }

  @Override
  public FTSDataColumnI getDataColumnByNameOrCode(String nameOrCode)
          throws Exception
  {
    if (dataColumns == null || dataColumns.isEmpty())
    {
      parseDataColumnsConfigFile();
    }
    for (FTSDataColumnI column : dataColumns)
    {
      if (column.getName().equalsIgnoreCase(nameOrCode)
              || column.getCode().equalsIgnoreCase(nameOrCode))
      {
        return column;
      }
    }
    throw new Exception(
            "Couldn't find data column with name : " + nameOrCode);
  }

  /**
   * 
   * @param instance
   * @param mocks
   *          {{working query, working response}, ...}
   */
  public static void createMockFTSRestClient(FTSRestClient instance,
          String[][] mocks)
  {
    instance.setMock(mocks);
  }

  @Override
  public FTSDataColumnGroupI getDataColumnGroupById(String id)
          throws Exception
  {
    if (dataColumns == null || dataColumns.isEmpty())
    {
      parseDataColumnsConfigFile();
    }
    for (FTSDataColumnGroupI columnGroup : dataColumnGroups)
    {
      if (columnGroup.getID().equalsIgnoreCase(id))
      {
        return columnGroup;
      }
    }
    throw new Exception("Couldn't find data column group with id : " + id);
  }

  public static String getMessageByHTTPStatusCode(int code, String service)
  {
    String message = "";
    switch (code)
    {
    case 400:
      message = "Bad request. There is a problem with your input.";
      break;

    case 410:
      message = service + " rest services no longer available!";
      break;
    case 403:
    case 404:
      message = "The requested resource could not be found";
      break;
    case 408:
    case 409:
    case 500:
    case 501:
    case 502:
    case 504:
    case 505:
      message = "There seems to be an error from the " + service
              + " server";
      break;
    case 503:
      message = "Service not available. The server is being updated, try again later.";
      break;
    default:
      break;
    }
    return String.valueOf(code) + " " + message;
  }

  public static void unMock(FTSRestClient instance)
  {
    instance.mockQueries = null;
  }

  protected String getResourceFile(String fileName)
  {
    String result = "";
    try
    {
      result = getClass().getResource(fileName).getFile();
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    return result;

  }

  @Override
  public int getDefaultResponsePageSize()
  {
    if (dataColumns == null || dataColumns.isEmpty())
    {
      parseDataColumnsConfigFile();
    }
    return defaultResponsePageSize;
  }

  protected void setMock(String[][] mocks)
  {
    if (mocks == null)
    {
      mockQueries = null;
      return;
    }
    mockQueries = new HashMap<String, String>();
    for (String[] mock : mocks)
    {
      mockQueries.put(mock[0], mock[1]);
    }
  }

  protected boolean isMocked()
  {
    return mockQueries != null;
  }

  @Override
  public String[] getPreferencesColumnsFor(PreferenceSource source)
  {
    String[] columnNames = null;
    switch (source)
    {
    case SEARCH_SUMMARY:
      columnNames = new String[] { "", "Display", "Group" };
      break;
    default:
      // non structure sources don't return any other kind of preferences
      // columns
      break;
    }
    return columnNames;
  }
}
