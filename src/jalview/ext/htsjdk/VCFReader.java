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
package jalview.ext.htsjdk;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import jalview.bin.Console;

/**
 * A thin wrapper for htsjdk classes to read either plain, or compressed, or
 * compressed and indexed VCF files
 */
public class VCFReader implements Closeable, Iterable<VariantContext>
{
  private static final String GZ = "gz";

  private static final String TBI_EXTENSION = ".tbi";

  private static final String CSI_EXTENSION = ".csi";

  private boolean indexed;

  private VCFFileReader reader;

  /**
   * Constructor given a raw or compressed VCF file or a (csi or tabix) index
   * file
   * <p>
   * If the file path ends in ".tbi" or ".csi", <em>or</em> appending one of
   * these extensions gives a valid file path, open as indexed, else as
   * unindexed.
   * 
   * @param f
   * @throws IOException
   */
  public VCFReader(String filePath) throws IOException
  {
    indexed = false;
    if (filePath.endsWith(TBI_EXTENSION)
            || filePath.endsWith(CSI_EXTENSION))
    {
      indexed = true;
      filePath = filePath.substring(0, filePath.length() - 4);
    }
    else if (new File(filePath + TBI_EXTENSION).exists())
    {
      indexed = true;
    }
    else if (new File(filePath + CSI_EXTENSION).exists())
    {
      indexed = true;
    }

    /*
     * we pass the name of the unindexed file to htsjdk,
     * with a flag to assert whether it is indexed
     */
    File file = new File(filePath);
    if (file.exists())
    {
      reader = new VCFFileReader(file, indexed);
    }
    else
    {
      Console.error("File not found: " + filePath);
    }
  }

  @Override
  public void close() throws IOException
  {
    if (reader != null)
    {
      reader.close();
    }
  }

  /**
   * Returns an iterator over VCF variants in the file. The client should call
   * close() on the iterator when finished with it.
   */
  @Override
  public CloseableIterator<VariantContext> iterator()
  {
    return reader == null ? null : reader.iterator();
  }

  /**
   * Queries for records overlapping the region specified. Note that this method
   * is performant if the VCF file is indexed, and may be very slow if it is
   * not.
   * <p>
   * Client code should call close() on the iterator when finished with it.
   * 
   * @param chrom
   *          the chromosome to query
   * @param start
   *          query interval start
   * @param end
   *          query interval end
   * @return
   */
  public CloseableIterator<VariantContext> query(final String chrom,
          final int start, final int end)
  {
    if (reader == null)
    {
      return null;
    }
    if (indexed)
    {
      return reader.query(chrom, start, end);
    }
    else
    {
      return queryUnindexed(chrom, start, end);
    }
  }

  /**
   * Returns an iterator over variant records read from a flat file which
   * overlap the specified chromosomal positions. Call close() on the iterator
   * when finished with it!
   * 
   * @param chrom
   * @param start
   * @param end
   * @return
   */
  protected CloseableIterator<VariantContext> queryUnindexed(
          final String chrom, final int start, final int end)
  {
    final CloseableIterator<VariantContext> it = reader.iterator();

    return new CloseableIterator<VariantContext>()
    {
      boolean atEnd = false;

      // prime look-ahead buffer with next matching record
      private VariantContext next = findNext();

      private VariantContext findNext()
      {
        if (atEnd)
        {
          return null;
        }
        VariantContext variant = null;
        while (it.hasNext())
        {
          variant = it.next();
          int vstart = variant.getStart();

          if (vstart > end)
          {
            atEnd = true;
            close();
            return null;
          }

          int vend = variant.getEnd();
          // todo what is the undeprecated way to get
          // the chromosome for the variant?
          if (chrom.equals(variant.getContig()) && (vstart <= end)
                  && (vend >= start))
          {
            return variant;
          }
        }
        return null;
      }

      @Override
      public boolean hasNext()
      {
        boolean hasNext = !atEnd && (next != null);
        if (!hasNext)
        {
          close();
        }
        return hasNext;
      }

      @Override
      public VariantContext next()
      {
        /*
         * return the next match, and then re-prime
         * it with the following one (if any)
         */
        VariantContext temp = next;
        next = findNext();
        return temp;
      }

      @Override
      public void remove()
      {
        // not implemented
      }

      @Override
      public void close()
      {
        it.close();
      }
    };
  }

  /**
   * Returns an object that models the VCF file headers
   * 
   * @return
   */
  public VCFHeader getFileHeader()
  {
    return reader == null ? null : reader.getFileHeader();
  }

  /**
   * Answers true if we are processing a tab-indexed VCF file, false if it is a
   * plain text (uncompressed) file.
   * 
   * @return
   */
  public boolean isIndex()
  {
    return indexed;
  }
}
