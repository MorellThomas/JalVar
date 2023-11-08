package jalview.ws.datamodel.alphafold;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import jalview.datamodel.ContactListI;
import jalview.datamodel.ContactListImpl;
import jalview.datamodel.ContactListProviderI;
import jalview.datamodel.ContactMatrixI;
import jalview.datamodel.GroupSet;
import jalview.datamodel.SequenceDummy;
import jalview.datamodel.SequenceI;
import jalview.io.FileFormatException;
import jalview.util.MapList;
import jalview.util.MapUtils;
import jalview.ws.dbsources.EBIAlfaFold;

/**
 * routines and class for holding predicted alignment error matrices as produced
 * by alphafold et al.
 * 
 * getContactList(column) returns the vector of predicted alignment errors for
 * reference position given by column getElementAt(column, i) returns the
 * predicted superposition error for the ith position when column is used as
 * reference
 * 
 * Many thanks to Ora Schueler Furman for noticing that earlier development
 * versions did not show the PAE oriented correctly
 *
 * @author jprocter
 *
 */
public class PAEContactMatrix extends
        MappableContactMatrix<PAEContactMatrix> implements ContactMatrixI
{


  int maxrow = 0, maxcol = 0;


  float[][] elements;

  float maxscore;


  @SuppressWarnings("unchecked")
  public PAEContactMatrix(SequenceI _refSeq, Map<String, Object> pae_obj)
          throws FileFormatException
  {
    setRefSeq(_refSeq);
    // convert the lists to primitive arrays and store

    if (!MapUtils.containsAKey(pae_obj, "predicted_aligned_error", "pae"))
    {
      parse_version_1_pAE(pae_obj);
      return;
    }
    else
    {
      parse_version_2_pAE(pae_obj);
    }
  }

  /**
   * construct a sequence associated PAE matrix directly from a float array
   * 
   * @param _refSeq
   * @param matrix
   */
  public PAEContactMatrix(SequenceI _refSeq, float[][] matrix)
  {
    setRefSeq(_refSeq);
    maxcol = 0;
    for (float[] row : matrix)
    {
      if (row.length > maxcol)
      {
        maxcol = row.length;
      }
      maxscore = row[0];
      for (float f : row)
      {
        if (maxscore < f)
        {
          maxscore = f;
        }
      }
    }
    maxrow = matrix.length;
    elements = matrix;

  }

  /**
   * new matrix with specific mapping to a reference sequence
   * 
   * @param newRefSeq
   * @param newFromMapList
   * @param elements2
   * @param grps2
   */
  public PAEContactMatrix(SequenceI newRefSeq, MapList newFromMapList,
          float[][] elements2, GroupSet grps2)
  {
    this(newRefSeq, elements2);
    toSeq = newFromMapList;
    grps = grps2;
  }

  /**
   * parse a sane JSON representation of the pAE
   * 
   * @param pae_obj
   */
  @SuppressWarnings("unchecked")
  private void parse_version_2_pAE(Map<String, Object> pae_obj)
  {
    maxscore = -1;
    // look for a maxscore element - if there is one...
    try
    {
      // this is never going to be reached by the integer rounding.. or is it ?
      maxscore = ((Double) MapUtils.getFirst(pae_obj,
              "max_predicted_aligned_error", "max_pae")).floatValue();
    } catch (Throwable t)
    {
      // ignore if a key is not found.
    }
    List<List<Long>> scoreRows = ((List<List<Long>>) MapUtils
            .getFirst(pae_obj, "predicted_aligned_error", "pae"));
    elements = new float[scoreRows.size()][scoreRows.size()];
    int row = 0, col = 0;
    for (List<Long> scoreRow : scoreRows)
    {
      Iterator<Long> scores = scoreRow.iterator();
      while (scores.hasNext())
      {
        Object d = scores.next();
        if (d instanceof Double)
        {
          elements[col][row] = ((Double) d).longValue();
        }
        else
        {
          elements[col][row] = (float) ((Long) d).longValue();
        }

        if (maxscore < elements[col][row])
        {
          maxscore = elements[col][row];
        }
        col++;
      }
      row++;
      col = 0;
    }
    maxcol = length;
    maxrow = length;
  }

  /**
   * v1 format got ditched 28th July 2022 see
   * https://alphafold.ebi.ac.uk/faq#:~:text=We%20updated%20the%20PAE%20JSON%20file%20format%20on%2028th%20July%202022
   * 
   * @param pae_obj
   */
  @SuppressWarnings("unchecked")
  private void parse_version_1_pAE(Map<String, Object> pae_obj)
  {
    // assume indices are with respect to range defined by _refSeq on the
    // dataset refSeq
    Iterator<Long> rows = ((List<Long>) pae_obj.get("residue1")).iterator();
    Iterator<Long> cols = ((List<Long>) pae_obj.get("residue2")).iterator();
    // two pass - to allocate the elements array
    while (rows.hasNext())
    {
      int row = rows.next().intValue();
      int col = cols.next().intValue();
      if (maxrow < row)
      {
        maxrow = row;
      }
      if (maxcol < col)
      {
        maxcol = col;
      }

    }
    rows = ((List<Long>) pae_obj.get("residue1")).iterator();
    cols = ((List<Long>) pae_obj.get("residue2")).iterator();
    Iterator<Double> scores = ((List<Double>) pae_obj.get("distance"))
            .iterator();
    elements = new float[maxcol][maxrow];
    while (scores.hasNext())
    {
      float escore = scores.next().floatValue();
      int row = rows.next().intValue();
      int col = cols.next().intValue();
      if (maxrow < row)
      {
        maxrow = row;
      }
      if (maxcol < col)
      {
        maxcol = col;
      }
      elements[col - 1][row-1] = escore;
    }

    maxscore = ((Double) MapUtils.getFirst(pae_obj,
            "max_predicted_aligned_error", "max_pae")).floatValue();
  }

  /**
   * getContactList(column) @returns the vector of predicted alignment errors
   * for reference position given by column
   */
  @Override
  public ContactListI getContactList(final int column)
  {
    if (column < 0 || column >= elements.length)
    {
      return null;
    }

    return new ContactListImpl(new ContactListProviderI()
    {
      @Override
      public int getPosition()
      {
        return column;
      }

      @Override
      public int getContactHeight()
      {
        return maxcol - 1;
      }

      @Override
      public double getContactAt(int mcolumn)
      {
        if (mcolumn < 0 || mcolumn >= elements[column].length)
        {
          return -1;
        }
        return elements[column][mcolumn];
      }
    });
  }

  /**
   * getElementAt(column, i) @returns the predicted superposition error for the
   * ith position when column is used as reference
   */
  @Override
  protected double getElementAt(int _column, int i)
  {
    return elements[_column][i];
  }

  @Override
  public float getMin()
  {
    return 0;
  }

  @Override
  public float getMax()
  {
    return maxscore;
  }

  @Override
  public String getAnnotDescr()
  {
    return "Predicted Alignment Error"
            + ((refSeq == null) ? "" : (" for " + refSeq.getName()));
  }

  @Override
  public String getAnnotLabel()
  {
    StringBuilder label = new StringBuilder("PAE Matrix");
    // if (this.getReferenceSeq() != null)
    // {
    // label.append(":").append(this.getReferenceSeq().getDisplayId(false));
    // }
    return label.toString();
  }

  public static final String PAEMATRIX = "PAE_MATRIX";

  @Override
  public String getType()
  {
    return PAEMATRIX;
  }

  @Override
  public int getWidth()
  {
    return maxcol;
  }

  @Override
  public int getHeight()
  {
    return maxrow;
  }
  public static void validateContactMatrixFile(String fileName)
          throws FileFormatException, IOException
  {
    FileInputStream infile = null;
    try
    {
      infile = new FileInputStream(new File(fileName));
    } catch (Throwable t)
    {
      new IOException("Couldn't open " + fileName, t);
    }
    JSONObject paeDict = null;
    try
    {
      paeDict = EBIAlfaFold.parseJSONtoPAEContactMatrix(infile);
    } catch (Throwable t)
    {
      new FileFormatException("Couldn't parse " + fileName
              + " as a JSON dict or array containing a dict");
    }

    PAEContactMatrix matrix = new PAEContactMatrix(
            new SequenceDummy("Predicted"), (Map<String, Object>) paeDict);
    if (matrix.getWidth() <= 0)
    {
      throw new FileFormatException(
              "No data in PAE matrix read from '" + fileName + "'");
    }
  }
  @Override
  protected PAEContactMatrix newMappableContactMatrix(SequenceI newRefSeq,
          MapList newFromMapList)
  {
    PAEContactMatrix pae = new PAEContactMatrix(newRefSeq, newFromMapList,
            elements, new GroupSet(grps));
    return pae;
  }
}
