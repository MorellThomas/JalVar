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

import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import htsjdk.samtools.SAMException;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.reference.FastaSequenceIndexCreator;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFileFactory;
import htsjdk.samtools.util.StringUtil;

/**
 * a source of sequence data accessed via the HTSJDK
 * 
 * @author jprocter
 *
 */
public class HtsContigDb
{
  private String name;

  private File dbLocation;

  private htsjdk.samtools.reference.ReferenceSequenceFile refFile = null;

  public static void createFastaSequenceIndex(Path path, boolean overwrite)
          throws IOException
  {
    try
    {
      FastaSequenceIndexCreator.create(path, overwrite);
    } catch (SAMException e)
    {
      throw new IOException(e.getMessage());
    }
  }

  public HtsContigDb(String name, File descriptor)
  {
    if (descriptor.isFile())
    {
      this.name = name;
      dbLocation = descriptor;
    }
    initSource();
  }

  public void close()
  {
    if (refFile != null)
    {
      try
      {
        refFile.close();
      } catch (IOException e)
      {
        // ignore
      }
    }
  }

  private void initSource()
  {
    if (refFile != null)
    {
      return;
    }

    refFile = ReferenceSequenceFileFactory
            .getReferenceSequenceFile(dbLocation, true);
    if (refFile == null || refFile.getSequenceDictionary() == null)
    {
      // refFile = initSequenceDictionaryFor(dbLocation);
    }

  }

  SAMSequenceDictionary rrefDict = null;

  private ReferenceSequenceFile initSequenceDictionaryFor(File dbLocation2)
          throws Exception
  {
    rrefDict = getDictionary(dbLocation2, true);
    if (rrefDict != null)
    {
      ReferenceSequenceFile rrefFile = ReferenceSequenceFileFactory
              .getReferenceSequenceFile(dbLocation2, true);
      return rrefFile;
    }
    return null;
  }

  /**
   * code below hacked out from picard ----
   * 
   * picard/src/java/picard/sam/CreateSequenceDictionary.java
   * https://github.com/
   * broadinstitute/picard/commit/270580d3e28123496576f0b91b3433179bb5d876
   */

  /*
   * The MIT License
   * 
   * Copyright (c) 2009 The Broad Institute
   * 
   * Permission is hereby granted, free of charge, to any person obtaining a
   * copy of this software and associated documentation files (the "Software"),
   * to deal in the Software without restriction, including without limitation
   * the rights to use, copy, modify, merge, publish, distribute, sublicense,
   * and/or sell copies of the Software, and to permit persons to whom the
   * Software is furnished to do so, subject to the following conditions:
   * 
   * The above copyright notice and this permission notice shall be included in
   * all copies or substantial portions of the Software.
   * 
   * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
   * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
   * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
   * DEALINGS IN THE SOFTWARE.
   */
  /**
   * 
   * @param f
   * @param truncate
   * @return
   * @throws Exception
   */
  SAMSequenceDictionary getDictionary(File f, boolean truncate)
          throws Exception
  {
    if (md5 == null)
    {
      initCreateSequenceDictionary();
    }
    final ReferenceSequenceFile refSeqFile = ReferenceSequenceFileFactory
            .getReferenceSequenceFile(f, truncate);
    ReferenceSequence refSeq;
    List<SAMSequenceRecord> ret = new ArrayList<>();
    Set<String> sequenceNames = new HashSet<>();
    for (int numSequences = 0; (refSeq = refSeqFile
            .nextSequence()) != null; ++numSequences)
    {
      if (sequenceNames.contains(refSeq.getName()))
      {
        throw new Exception(
                "Sequence name appears more than once in reference: "
                        + refSeq.getName());
      }
      sequenceNames.add(refSeq.getName());
      ret.add(makeSequenceRecord(refSeq));
    }
    return new SAMSequenceDictionary(ret);
  }

  public boolean isValid()
  {
    return dbLocation != null && refFile != null;
  }

  /**
   * Create one SAMSequenceRecord from a single fasta sequence
   */
  private SAMSequenceRecord makeSequenceRecord(
          final ReferenceSequence refSeq)
  {

    final SAMSequenceRecord ret = new SAMSequenceRecord(refSeq.getName(),
            refSeq.length());

    // Compute MD5 of upcased bases
    final byte[] bases = refSeq.getBases();
    for (int i = 0; i < bases.length; ++i)
    {
      bases[i] = StringUtil.toUpperCase(bases[i]);
    }

    ret.setAttribute(SAMSequenceRecord.MD5_TAG, md5Hash(bases));
    // if (GENOME_ASSEMBLY != null) {
    // ret.setAttribute(SAMSequenceRecord.ASSEMBLY_TAG, GENOME_ASSEMBLY);
    // }
    // ret.setAttribute(SAMSequenceRecord.URI_TAG, URI);
    // if (SPECIES != null) {
    // ret.setAttribute(SAMSequenceRecord.SPECIES_TAG, SPECIES);
    // }
    return ret;
  }

  private MessageDigest md5;

  public void initCreateSequenceDictionary() throws Exception
  {
    try
    {
      md5 = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e)
    {
      throw new Exception("MD5 algorithm not found", e);
    }
  }

  private String md5Hash(final byte[] bytes)
  {
    md5.reset();
    md5.update(bytes);
    String s = new BigInteger(1, md5.digest()).toString(16);
    if (s.length() != 32)
    {
      final String zeros = "00000000000000000000000000000000";
      s = zeros.substring(0, 32 - s.length()) + s;
    }
    return s;
  }

  // ///// end of hts bits.

  /**
   * Reads the contig with the given id and returns as a Jalview SequenceI
   * object. Note the database must be indexed for this operation to succeed.
   * 
   * @param id
   * @return
   */
  public SequenceI getSequenceProxy(String id)
  {
    if (!isValid() || !refFile.isIndexed())
    {
      System.err.println(
              "Cannot read contig as file is invalid or not indexed");
      return null;
    }

    ReferenceSequence sseq = refFile.getSequence(id);
    return new Sequence(sseq.getName(), new String(sseq.getBases()));
  }

  public boolean isIndexed()
  {
    return refFile != null && refFile.isIndexed();
  }

}
