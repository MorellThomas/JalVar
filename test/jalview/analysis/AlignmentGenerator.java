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
package jalview.analysis;

import java.util.Locale;

import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;
import jalview.io.FastaFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Random;

import org.testng.annotations.BeforeClass;

/**
 * Generates, and outputs in Fasta format, a random peptide or nucleotide
 * alignment for given sequence length and count. Will regenerate the same
 * alignment each time if the same random seed is used (so may be used for
 * reproducible unit tests). Not guaranteed to reproduce the same results
 * between versions, as the rules may get tweaked to produce more 'realistic'
 * results.
 * 
 * @author gmcarstairs
 */
public class AlignmentGenerator
{
  private static final char GAP = '-';

  private static final char ZERO = '0';

  private static final char[] NUCS = "GTCA".toCharArray();

  private static final char[] PEPS = "MILVFYWHKRDEQNTCGASNP".toCharArray();

  private static char[] BASES;

  private Random random;

  private PrintStream ps;

  /**
   * Outputs a pseudo-randomly generated nucleotide or peptide alignment
   * Arguments:
   * <ul>
   * <li>n (for nucleotide) or p (for peptide)</li>
   * <li>length (number of bases in each sequence)</li>
   * <li>height (number of sequences)</li>
   * <li>a whole number random seed</li>
   * <li>percentage of gaps to include (0-100)</li>
   * <li>percentage chance of variation of each position (0-100)</li>
   * <li>(optional) path to a file to write the alignment to</li>
   * </ul>
   * 
   * 
   * @param args
   * @throws FileNotFoundException
   */
  public static void main(String[] args) throws FileNotFoundException
  {
    if (args.length != 6 && args.length != 7)
    {
      usage();
      return;
    }

    PrintStream ps = System.out;
    if (args.length == 7)
    {
      ps = new PrintStream(new File(args[6]));
    }

    boolean nucleotide = args[0].toLowerCase(Locale.ROOT).startsWith("n");
    int width = Integer.parseInt(args[1]);
    int height = Integer.parseInt(args[2]);
    long randomSeed = Long.valueOf(args[3]);
    int gapPercentage = Integer.valueOf(args[4]);
    int changePercentage = Integer.valueOf(args[5]);

    ps.println("; " + height + " sequences of " + width + " bases with "
            + gapPercentage + "% gaps and " + changePercentage
            + "% mutations (random seed = " + randomSeed + ")");

    new AlignmentGenerator(nucleotide, ps).generate(width, height,
            randomSeed, gapPercentage, changePercentage);

    if (ps != System.out)
    {
      ps.close();
    }
  }

  /**
   * Prints parameter help
   */
  private static void usage()
  {
    System.out.println("Usage:");
    System.out.println("arg0: n (for nucleotide) or p (for peptide)");
    System.out.println("arg1: number of (non-gap) bases per sequence");
    System.out.println("arg2: number of sequences");
    System.out.println(
            "arg3: an integer as random seed (same seed = same results)");
    System.out.println("arg4: percentage of gaps to (randomly) generate");
    System.out.println(
            "arg5: percentage of 'mutations' to (randomly) generate");
    System.out.println(
            "arg6: (optional) path to output file (default is sysout)");
    System.out.println("Example: AlignmentGenerator n 12 15 387 10 5");
    System.out.println(
            "- 15 nucleotide sequences of 12 bases each, approx 10% gaps and 5% mutations, random seed = 387");

  }

  /**
   * Constructor that sets nucleotide or peptide symbol set, and also writes the
   * generated alignment to sysout
   */
  public AlignmentGenerator(boolean nuc)
  {
    this(nuc, System.out);
  }

  /**
   * Constructor that sets nucleotide or peptide symbol set, and also writes the
   * generated alignment to the specified output stream (if not null). This can
   * be used to write the alignment to a file or sysout.
   */
  public AlignmentGenerator(boolean nucleotide, PrintStream printStream)
  {
    BASES = nucleotide ? NUCS : PEPS;
    ps = printStream;
  }

  /**
   * Outputs an 'alignment' of given width and height, where each position is a
   * random choice from the symbol alphabet, or - for gap
   * 
   * @param width
   * @param height
   * @param randomSeed
   * @param changePercentage
   * @param gapPercentage
   */
  public AlignmentI generate(int width, int height, long randomSeed,
          int gapPercentage, int changePercentage)
  {
    SequenceI[] seqs = new SequenceI[height];
    random = new Random(randomSeed);
    seqs[0] = generateSequence(1, width, gapPercentage);
    for (int seqno = 1; seqno < height; seqno++)
    {
      seqs[seqno] = generateAnotherSequence(seqs[0].getSequence(),
              seqno + 1, width, changePercentage);
    }
    AlignmentI al = new Alignment(seqs);

    if (ps != null)
    {
      ps.println(new FastaFile().print(al.getSequencesArray(), true));
    }

    return al;
  }

  /**
   * Outputs a DNA 'sequence' of given length, with some random gaps included.
   * 
   * @param seqno
   * @param length
   * @param gapPercentage
   */
  private SequenceI generateSequence(int seqno, int length,
          int gapPercentage)
  {
    StringBuilder seq = new StringBuilder(length);

    /*
     * Loop till we've added 'length' bases (excluding gaps)
     */
    for (int count = 0; count < length;)
    {
      boolean addGap = random.nextInt(100) < gapPercentage;
      char c = addGap ? GAP
              : BASES[random.nextInt(Integer.MAX_VALUE) % BASES.length];
      seq.append(c);
      if (!addGap)
      {
        count++;
      }
    }
    final String seqName = "SEQ" + seqno;
    final String seqString = seq.toString();
    SequenceI sq = new Sequence(seqName, seqString);
    sq.createDatasetSequence();
    return sq;
  }

  /**
   * Generate a sequence approximately aligned to the first one.
   * 
   * @param ds
   * @param seqno
   * @param width
   *          number of bases
   * @param changePercentage
   * @return
   */
  private SequenceI generateAnotherSequence(char[] ds, int seqno, int width,
          int changePercentage)
  {
    int length = ds.length;
    char[] seq = new char[length];
    Arrays.fill(seq, ZERO);
    int gapsWanted = length - width;
    int gapsAdded = 0;

    /*
     * First 'randomly' mimic gaps in model sequence.
     */
    for (int pos = 0; pos < length; pos++)
    {
      if (ds[pos] == GAP)
      {
        /*
         * Add a gap at the same position with changePercentage likelihood
         */
        seq[pos] = randomCharacter(GAP, changePercentage);
        if (seq[pos] == GAP)
        {
          gapsAdded++;
        }
      }
    }

    /*
     * Next scatter any remaining gaps (if any) at random. This gives an even
     * distribution.
     */
    while (gapsAdded < gapsWanted)
    {
      boolean added = false;
      while (!added)
      {
        int pos = random.nextInt(length);
        if (seq[pos] != GAP)
        {
          seq[pos] = GAP;
          added = true;
          gapsAdded++;
        }
      }
    }

    /*
     * Finally fill in the rest with randomly mutated bases.
     */
    for (int pos = 0; pos < length; pos++)
    {
      if (seq[pos] == ZERO)
      {
        char c = randomCharacter(ds[pos], changePercentage);
        seq[pos] = c;
      }
    }
    final String seqName = "SEQ" + seqno;
    final String seqString = new String(seq);
    SequenceI sq = new Sequence(seqName, seqString);
    sq.createDatasetSequence();
    return sq;
  }

  /**
   * Returns a random character that is changePercentage% likely to match the
   * given type (as base or gap).
   * 
   * @param changePercentage
   * 
   * @param c
   * @return
   */
  private char randomCharacter(char c, int changePercentage)
  {
    final boolean mutation = random.nextInt(100) < changePercentage;

    if (!mutation)
    {
      return c;
    }

    char newchar = c;
    while (newchar == c)
    {
      newchar = BASES[random.nextInt(Integer.MAX_VALUE) % BASES.length];
    }
    return newchar;
  }
}
