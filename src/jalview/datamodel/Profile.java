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
 * A profile for one column of an alignment
 * 
 * @author gmcarstairs
 *
 */
public class Profile implements ProfileI
{
  /*
   * an object holding counts of symbols in the profile
   */
  private ResidueCount counts;

  /*
   * the number of sequences (gapped or not) in the profile
   */
  private int height;

  /*
   * the number of non-gapped sequences in the profile
   */
  private int gapped;

  /*
   * the highest count for any residue in the profile
   */
  private int maxCount;

  /*
   * the residue (e.g. K) or residues (e.g. KQW) with the
   * highest count in the profile
   */
  private String modalResidue;
  
  /*
   * individual counts of all modal Residues
   */
  private int[] individCounts;

  /**
   * Constructor which allows derived data to be stored without having to store
   * the full profile
   * 
   * @param seqCount
   *          the number of sequences in the profile
   * @param gaps
   *          the number of gapped sequences
   * @param max
   *          the highest count for any residue
   * @param modalres
   *          the residue (or concatenated residues) with the highest count
   */
  public Profile(int seqCount, int gaps, int max, String modalRes)
  {
    this.height = seqCount;
    this.gapped = gaps;
    this.maxCount = max;
    this.modalResidue = modalRes;
  }

  /* (non-Javadoc)
   * @see jalview.datamodel.ProfileI#setCounts(jalview.datamodel.ResidueCount)
   */
  @Override
  public void setCounts(ResidueCount residueCounts)
  {
    this.counts = residueCounts;
  }

  /* (non-Javadoc)
   * @see jalview.datamodel.ProfileI#getPercentageIdentity(boolean)
   */
  @Override
  public float getPercentageIdentity(boolean ignoreGaps)
  {
    if (height == 0)
    {
      return 0f;
    }
    float pid = 0f;
    if (ignoreGaps && gapped < height)
    {
      pid = (maxCount * 100f) / (height - gapped);
    }
    else
    {
      pid = (maxCount * 100f) / height;
    }
    return pid;
  }

  @Override
  public float getPercentageIdentity(int count, boolean ignoreGaps)
  {
    if (height == 0)
    {
      return 0f;
    }
    float pid = 0f;
    if (ignoreGaps && gapped < height)
    {
      pid = (count * 100f) / (height - gapped);
    }
    else
    {
      pid = (count * 100f) / height;
    }
    return pid;
  }
  
  @Override
  public String getOccuranceFraction(int count, boolean ignoreGaps)
  {
    if (height == 0)
    {
      return "0/0";
    }
    int nseq = ignoreGaps && gapped < height ? height - gapped : height;
    return String.format("%d/%d", count, nseq);
  }

  /* (non-Javadoc)
   * @see jalview.datamodel.ProfileI#getCounts()
   */
  @Override
  public ResidueCount getCounts()
  {
    return counts;
  }

  /* (non-Javadoc)
   * @see jalview.datamodel.ProfileI#getHeight()
   */
  @Override
  public int getHeight()
  {
    return height;
  }

  /* (non-Javadoc)
   * @see jalview.datamodel.ProfileI#getGapped()
   */
  @Override
  public int getGapped()
  {
    return gapped;
  }

  /* (non-Javadoc)
   * @see jalview.datamodel.ProfileI#getMaxCount()
   */
  @Override
  public int getMaxCount()
  {
    return maxCount;
  }

  /* (non-Javadoc)
   * @see jalview.datamodel.ProfileI#getModalResidue()
   */
  @Override
  public String getModalResidue()
  {
    return modalResidue;
  }

  /* (non-Javadoc)
   * @see jalview.datamodel.ProfileI#getNonGapped()
   */
  @Override
  public int getNonGapped()
  {
    return height - gapped;
  }
  
  /*
   * set individual counts for all modal residues
   */
  @Override
  public void setIndividCounts(int[] ic)
  {
    this.individCounts = ic;
  }
  
  /*
   * get individual counts for all modal residues
   */
  @Override
  public int[] getIndividCounts()
  {
    return this.individCounts;
  }
}
