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
package jalview.schemes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import jalview.analysis.GeneticCodes;

public class ResidueProperties
{
  // Stores residue codes/names and colours and other things
  public static final int[] aaIndex; // aaHash version 2.1.1 and below

  public static final int[] nucleotideIndex;

  public static final int[] purinepyrimidineIndex;

  public static final Map<String, Integer> aa3Hash = new HashMap<>();

  public static final Map<String, String> aa2Triplet = new HashMap<>();

  public static final Map<String, String> nucleotideName = new HashMap<>();

  public static final Map<String, String> nucleotideAmbiguityName = new HashMap<>();

  // lookup from modified amino acid (e.g. MSE) to canonical form (e.g. MET)
  public static final Map<String, String> modifications = new HashMap<>();

  static
  {
    aaIndex = new int[255];
    for (int i = 0; i < 255; i++)
    {
      aaIndex[i] = 23;
    }

    aaIndex['A'] = 0;
    aaIndex['R'] = 1;
    aaIndex['N'] = 2;
    aaIndex['D'] = 3;
    aaIndex['C'] = 4;
    aaIndex['Q'] = 5;
    aaIndex['E'] = 6;
    aaIndex['G'] = 7;
    aaIndex['H'] = 8;
    aaIndex['I'] = 9;
    aaIndex['L'] = 10;
    aaIndex['K'] = 11;
    aaIndex['M'] = 12;
    aaIndex['F'] = 13;
    aaIndex['P'] = 14;
    aaIndex['S'] = 15;
    aaIndex['T'] = 16;
    aaIndex['W'] = 17;
    aaIndex['Y'] = 18;
    aaIndex['V'] = 19;
    aaIndex['B'] = 20;
    aaIndex['Z'] = 21;
    aaIndex['X'] = 22;
    aaIndex['U'] = 22;
    aaIndex['a'] = 0;
    aaIndex['r'] = 1;
    aaIndex['n'] = 2;
    aaIndex['d'] = 3;
    aaIndex['c'] = 4;
    aaIndex['q'] = 5;
    aaIndex['e'] = 6;
    aaIndex['g'] = 7;
    aaIndex['h'] = 8;
    aaIndex['i'] = 9;
    aaIndex['l'] = 10;
    aaIndex['k'] = 11;
    aaIndex['m'] = 12;
    aaIndex['f'] = 13;
    aaIndex['p'] = 14;
    aaIndex['s'] = 15;
    aaIndex['t'] = 16;
    aaIndex['w'] = 17;
    aaIndex['y'] = 18;
    aaIndex['v'] = 19;
    aaIndex['b'] = 20;
    aaIndex['z'] = 21;
    aaIndex['x'] = 22;
    aaIndex['u'] = 22; // TODO: selenocystine triplet and codons needed. also
    // extend subt. matrices
  }

  /**
   * maximum (gap) index for matrices involving protein alphabet
   */
  public final static int maxProteinIndex = 23;

  /**
   * maximum (gap) index for matrices involving nucleotide alphabet
   */
  // public final static int maxNucleotideIndex = 10;
  public final static int maxNucleotideIndex;

  static
  {
    String[][] namesArray = { { "a", "Adenine" }, { "c", "Cytosine" },
        { "g", "Guanine" },
        { "t", "Thymine" },
        { "u", "Uracil" },
        { "i", "Inosine" },
        { "x", "Xanthine" },
        { "r", "Unknown Purine" },
        { "y", "Unknown Pyrimidine" },
        { "w", "Weak nucleotide (A or T)" },
        { "s", "Strong nucleotide (G or C)" },
        { "m", "Amino (A or C)" },
        { "k", "Keto (G or T)" },
        { "b", "Not A (G or C or T)" },
        { "h", "Not G (A or C or T)" },
        { "d", "Not C (A or G or T)" },
        { "v", "Not T (A or G or C)" },
        { "n", "Unknown" } };
    // "gap" index
    maxNucleotideIndex = namesArray.length;

    nucleotideIndex = new int[255];
    for (int i = 0; i < 255; i++)
    {
      nucleotideIndex[i] = maxNucleotideIndex; // non-nucleotide symbols are all
                                               // non-gap gaps.
    }

    for (int i = 0; i < namesArray.length; i++)
    {
      char c = namesArray[i][0].charAt(0);
      nucleotideIndex[c] = i;
      // Character.toUpperCase is Locale insensitive
      nucleotideIndex[Character.toUpperCase(c)] = i;
      nucleotideName.put(namesArray[i][0], namesArray[i][1]);
      nucleotideName.put(namesArray[i][0].toUpperCase(Locale.ROOT),
              namesArray[i][1]);
    }

  }

  static
  {
    purinepyrimidineIndex = new int[255];
    for (int i = 0; i < 255; i++)
    {
      purinepyrimidineIndex[i] = 3; // non-nucleotide symbols are all non-gap
      // gaps.
    }

    purinepyrimidineIndex['A'] = 0;
    purinepyrimidineIndex['a'] = 0;
    purinepyrimidineIndex['C'] = 1;
    purinepyrimidineIndex['c'] = 1;
    purinepyrimidineIndex['G'] = 0;
    purinepyrimidineIndex['g'] = 0;
    purinepyrimidineIndex['T'] = 1;
    purinepyrimidineIndex['t'] = 1;
    purinepyrimidineIndex['U'] = 1;
    purinepyrimidineIndex['u'] = 1;
    purinepyrimidineIndex['I'] = 2;
    purinepyrimidineIndex['i'] = 2;
    purinepyrimidineIndex['X'] = 2;
    purinepyrimidineIndex['x'] = 2;
    purinepyrimidineIndex['R'] = 0;
    purinepyrimidineIndex['r'] = 0;
    purinepyrimidineIndex['Y'] = 1;
    purinepyrimidineIndex['y'] = 1;
    purinepyrimidineIndex['N'] = 2;
    purinepyrimidineIndex['n'] = 2;
  }

  private static final Integer ONE = Integer.valueOf(1);

  private static final Integer ZERO = Integer.valueOf(0);

  static
  {
    aa3Hash.put("ALA", ZERO);
    aa3Hash.put("ARG", ONE);
    aa3Hash.put("ASN", Integer.valueOf(2));
    aa3Hash.put("ASP", Integer.valueOf(3)); // D
    aa3Hash.put("CYS", Integer.valueOf(4));
    aa3Hash.put("GLN", Integer.valueOf(5)); // Q
    aa3Hash.put("GLU", Integer.valueOf(6)); // E
    aa3Hash.put("GLY", Integer.valueOf(7));
    aa3Hash.put("HIS", Integer.valueOf(8));
    aa3Hash.put("ILE", Integer.valueOf(9));
    aa3Hash.put("LEU", Integer.valueOf(10));
    aa3Hash.put("LYS", Integer.valueOf(11));
    aa3Hash.put("MET", Integer.valueOf(12));
    aa3Hash.put("PHE", Integer.valueOf(13));
    aa3Hash.put("PRO", Integer.valueOf(14));
    aa3Hash.put("SER", Integer.valueOf(15));
    aa3Hash.put("THR", Integer.valueOf(16));
    aa3Hash.put("TRP", Integer.valueOf(17));
    aa3Hash.put("TYR", Integer.valueOf(18));
    aa3Hash.put("VAL", Integer.valueOf(19));
    // IUB Nomenclature for ambiguous peptides
    aa3Hash.put("ASX", Integer.valueOf(20)); // "B";
    aa3Hash.put("GLX", Integer.valueOf(21)); // Z
    aa3Hash.put("XAA", Integer.valueOf(22)); // X unknown
    aa3Hash.put("-", Integer.valueOf(23));
    aa3Hash.put("*", Integer.valueOf(23));
    aa3Hash.put(".", Integer.valueOf(23));
    aa3Hash.put(" ", Integer.valueOf(23));
    aa3Hash.put("Gap", Integer.valueOf(23));
    aa3Hash.put("UR3", Integer.valueOf(24));
  }

  static
  {
    aa2Triplet.put("A", "ALA");
    aa2Triplet.put("a", "ALA");
    aa2Triplet.put("R", "ARG");
    aa2Triplet.put("r", "ARG");
    aa2Triplet.put("N", "ASN");
    aa2Triplet.put("n", "ASN");
    aa2Triplet.put("D", "ASP");
    aa2Triplet.put("d", "ASP");
    aa2Triplet.put("C", "CYS");
    aa2Triplet.put("c", "CYS");
    aa2Triplet.put("Q", "GLN");
    aa2Triplet.put("q", "GLN");
    aa2Triplet.put("E", "GLU");
    aa2Triplet.put("e", "GLU");
    aa2Triplet.put("G", "GLY");
    aa2Triplet.put("g", "GLY");
    aa2Triplet.put("H", "HIS");
    aa2Triplet.put("h", "HIS");
    aa2Triplet.put("I", "ILE");
    aa2Triplet.put("i", "ILE");
    aa2Triplet.put("L", "LEU");
    aa2Triplet.put("l", "LEU");
    aa2Triplet.put("K", "LYS");
    aa2Triplet.put("k", "LYS");
    aa2Triplet.put("M", "MET");
    aa2Triplet.put("m", "MET");
    aa2Triplet.put("F", "PHE");
    aa2Triplet.put("f", "PHE");
    aa2Triplet.put("P", "PRO");
    aa2Triplet.put("p", "PRO");
    aa2Triplet.put("S", "SER");
    aa2Triplet.put("s", "SER");
    aa2Triplet.put("T", "THR");
    aa2Triplet.put("t", "THR");
    aa2Triplet.put("W", "TRP");
    aa2Triplet.put("w", "TRP");
    aa2Triplet.put("Y", "TYR");
    aa2Triplet.put("y", "TYR");
    aa2Triplet.put("V", "VAL");
    aa2Triplet.put("v", "VAL");
  }

  public static final String[] aa = { "A", "R", "N", "D", "C", "Q", "E",
      "G", "H", "I", "L", "K", "M", "F", "P", "S", "T", "W", "Y", "V", "B",
      "Z", "X", "_", "*", ".", " ", "U" };

  public static final Color midBlue = new Color(100, 100, 255);

  // not currently in use
  // public static final Vector<Color> scaleColours = new Vector<Color>();
  // static
  // {
  // scaleColours.addElement(new Color(114, 0, 147));
  // scaleColours.addElement(new Color(156, 0, 98));
  // scaleColours.addElement(new Color(190, 0, 0));
  // scaleColours.addElement(Color.red);
  // scaleColours.addElement(new Color(255, 125, 0));
  // scaleColours.addElement(Color.orange);
  // scaleColours.addElement(new Color(255, 194, 85));
  // scaleColours.addElement(Color.yellow);
  // scaleColours.addElement(new Color(255, 255, 181));
  // scaleColours.addElement(Color.white);
  // }

  public static final Color[] taylor = { new Color(204, 255, 0),
      // A Greenish-yellowy-yellow
      new Color(0, 0, 255), // R Blueish-bluey-blue
      new Color(204, 0, 255), // N Blueish-reddy-blue
      new Color(255, 0, 0), // D Reddish-reddy-red
      new Color(255, 255, 0), // C Yellowish-yellowy-yellow
      new Color(255, 0, 204), // Q Reddish-bluey-red
      new Color(255, 0, 102), // E Blueish-reddy-red
      new Color(255, 153, 0), // G Yellowy-reddy-yellow
      new Color(0, 102, 255), // H Greenish-bluey-blue
      new Color(102, 255, 0), // I Greenish-yellowy-green
      new Color(51, 255, 0), // L Yellowish-greeny-green
      new Color(102, 0, 255), // K Reddish-bluey-blue
      new Color(0, 255, 0), // M Greenish-greeny-green
      new Color(0, 255, 102), // F Blueish-greeny-green
      new Color(255, 204, 0), // P Reddish-yellowy-yellow
      new Color(255, 51, 0), // S Yellowish-reddy-red
      new Color(255, 102, 0), // T Reddish-yellowy-red
      new Color(0, 204, 255), // W Blueish-greeny-green
      new Color(0, 255, 204), // Y Greenish-bluey-green
      new Color(153, 255, 0), // V Yellowish-greeny-yellow
      Color.white, // B
      Color.white, // Z
      Color.white, // X
      Color.white, // -
      Color.white, // *
      Color.white // .
  };

  public static final Color[] nucleotide = { new Color(100, 247, 63), // A
      new Color(255, 179, 64), // C
      new Color(235, 65, 60), // G
      new Color(60, 136, 238), // T
      new Color(60, 136, 238), // U
      Color.white, // I (inosine)
      Color.white, // X (xanthine)
      Color.white, // R
      Color.white, // Y
      Color.white, // N
      Color.white, // w
      Color.white, // s
      Color.white, // m
      Color.white, // k
      Color.white, // b
      Color.white, // h
      Color.white, // d
      Color.white, // v
      Color.white, // Gap
  };

  // this colour scheme devised by sduce
  public static final Color[] nucleotideAmbiguity = {
      Color.decode("#f0fff0"), // a
      Color.decode("#f0fff0"), // c
      Color.decode("#f0fff0"), // g
      Color.decode("#f0fff0"), // t
      Color.decode("#f0fff0"), // u
      Color.decode("#ffffff"), // i
      Color.decode("#4f6f6f"), // x
      Color.decode("#CD5C5C"), // r
      Color.decode("#008000"), // y
      Color.decode("#4682B4"), // w
      Color.decode("#FF8C00"), // s
      Color.decode("#9ACD32"), // m
      Color.decode("#9932CC"), // k
      Color.decode("#8b4513"), // b
      Color.decode("#808080"), // h
      Color.decode("#483D8B"), // d
      Color.decode("#b8860b"), // v
      Color.decode("#2f4f4f"), // n
      Color.white, // Gap
  };

  // Added for PurinePyrimidineColourScheme
  public static final Color[] purinepyrimidine = { new Color(255, 131, 250), // A,
                                                                             // G,
                                                                             // R
                                                                             // purines
                                                                             // purplish/orchid
      new Color(64, 224, 208), // C,U, T, Y pyrimidines turquoise
      Color.white, // all other nucleotides
      Color.white // Gap
  };

  // Zappo
  public static final Color[] zappo = { Color.pink, // A
      midBlue, // R
      Color.green, // N
      Color.red, // D
      Color.yellow, // C
      Color.green, // Q
      Color.red, // E
      Color.magenta, // G
      midBlue, // Color.red, // H
      Color.pink, // I
      Color.pink, // L
      midBlue, // K
      Color.pink, // M
      Color.orange, // F
      Color.magenta, // P
      Color.green, // S
      Color.green, // T
      Color.orange, // W
      Color.orange, // Y
      Color.pink, // V
      Color.white, // B
      Color.white, // Z
      Color.white, // X
      Color.white, // -
      Color.white, // *
      Color.white, // .
      Color.white // ' '
  };

  /*
   * flower, blossom, sunset, ocean colour schemes from geocos.
   * See https://gecos.biotite-python.org/
   * https://raw.githubusercontent.com/biotite-dev/biotite/master/src/biotite/sequence/graphics/color_schemes/flower.json
   * and https://bmcbioinformatics.biomedcentral.com/articles/10.1186/s12859-020-3526-6
   * (https://doi.org/10.1186/s12859-020-3526-6)
   */
  public static final Color[] flower = { new Color(177, 138, 81), // A
      new Color(131, 191, 241), // R
      new Color(11, 206, 198), // N
      new Color(1, 165, 120), // D
      new Color(255, 87, 1), // C
      new Color(114, 149, 174), // Q
      new Color(45, 160, 161), // E
      new Color(177, 194, 60), // G
      new Color(1, 148, 249), // H
      new Color(242, 118, 99), // I
      new Color(223, 110, 117), // L
      new Color(127, 195, 215), // K
      new Color(254, 157, 175), // M
      new Color(250, 85, 157), // F
      new Color(79, 163, 42), // P
      new Color(180, 189, 155), // S
      new Color(210, 181, 118), // T
      new Color(255, 45, 237), // W
      new Color(201, 110, 207), // Y
      new Color(253, 153, 123), // V
      Color.white, // B
      Color.white, // Z
      Color.white, // X
      Color.white, // -
      Color.white, // *
      Color.white // .
  };

  public static final Color[] blossom = { new Color(139, 196, 180), // A
      new Color(252, 149, 2), // R
      new Color(181, 194, 6), // N
      new Color(95, 165, 5), // D
      new Color(8, 147, 254), // C
      new Color(191, 133, 39), // Q
      new Color(219, 181, 1), // E
      new Color(0, 211, 130), // G
      new Color(255, 87, 1), // H
      new Color(154, 186, 243), // I
      new Color(205, 165, 220), // L
      new Color(254, 165, 39), // K
      new Color(245, 161, 184), // M
      new Color(247, 79, 168), // F
      new Color(16, 214, 49), // P
      new Color(126, 157, 89), // S
      new Color(0, 162, 156), // T
      new Color(254, 8, 251), // W
      new Color(255, 78, 122), // Y
      new Color(135, 192, 228), // V
      Color.white, // B
      Color.white, // Z
      Color.white, // X
      Color.white, // -
      Color.white, // *
      Color.white // .
  };

  public static final Color[] sunset = { new Color(254, 160, 253), // A
      new Color(133, 116, 106), // R
      new Color(171, 200, 245), // N
      new Color(46, 123, 190), // D
      new Color(252, 12, 254), // C
      new Color(140, 110, 129), // Q
      new Color(103, 120, 146), // E
      new Color(39, 153, 255), // G
      new Color(219, 197, 142), // H
      new Color(250, 33, 161), // I
      new Color(224, 30, 130), // L
      new Color(222, 190, 204), // K
      new Color(209, 62, 123), // M
      new Color(255, 56, 93), // F
      new Color(87, 102, 249), // P
      new Color(231, 180, 253), // S
      new Color(166, 88, 183), // T
      new Color(255, 55, 1), // W
      new Color(203, 83, 57), // Y
      new Color(254, 81, 184), // V
      Color.white, // B
      Color.white, // Z
      Color.white, // X
      Color.white, // -
      Color.white, // *
      Color.white // .
  };

  public static final Color[] ocean = { new Color(198, 202, 155), // A
      new Color(12, 160, 168), // R
      new Color(10, 223, 195), // N
      new Color(76, 223, 161), // D
      new Color(198, 129, 54), // C
      new Color(139, 211, 209), // Q
      new Color(96, 218, 201), // E
      new Color(51, 165, 81), // G
      new Color(0, 207, 254), // H
      new Color(242, 186, 170), // I
      new Color(187, 138, 131), // L
      new Color(64, 160, 144), // K
      new Color(164, 139, 136), // M
      new Color(171, 136, 174), // F
      new Color(175, 211, 101), // P
      new Color(109, 155, 116), // S
      new Color(141, 149, 102), // T
      new Color(117, 138, 238), // W
      new Color(186, 195, 252), // Y
      new Color(233, 190, 164), // V
      Color.white, // B
      Color.white, // Z
      Color.white, // X
      Color.white, // -
      Color.white, // *
      Color.white // .
  };

  // Dunno where I got these numbers from
  public static final double[] hyd2 = { 0.62, // A
      0.29, // R
      -0.90, // N
      -0.74, // D
      1.19, // C
      0.48, // Q
      -0.40, // E
      1.38, // G
      -1.50, // H
      1.06, // I
      0.64, // L
      -0.78, // K
      0.12, // M
      -0.85, // F
      -2.53, // P
      -0.18, // S
      -0.05, // T
      1.08, // W
      0.81, // Y
      0.0, // V
      0.26, // B
      0.0, // Z
      0.0 // X
  };

  public static final double[] helix = { 1.42, 0.98, 0.67, 1.01, 0.70, 1.11,
      1.51, 0.57, 1.00, 1.08, 1.21, 1.16, 1.45, 1.13, 0.57, 0.77, 0.83,
      1.08, 0.69, 1.06, 0.84, 1.31, 1.00, 0.0 };

  public static final double helixmin = 0.57;

  public static final double helixmax = 1.51;

  public static final double[] strand = { 0.83, 0.93, 0.89, 0.54, 1.19,
      1.10, 0.37, 0.75, 0.87, 1.60, 1.30, 0.74, 1.05, 1.38, 0.55, 0.75,
      1.19, 1.37, 1.47, 1.70, 0.72, 0.74, 1.0, 0.0 };

  public static final double strandmin = 0.37;

  public static final double strandmax = 1.7;

  public static final double[] turn = { 0.66, 0.95, 1.56, 1.46, 1.19, 0.98,
      0.74, 1.56, 0.95, 0.47, 0.59, 1.01, 0.60, 0.60, 1.52, 1.43, 0.96,
      0.96, 1.14, 0.50, 1.51, 0.86, 1.00, 0, 0 };

  public static final double turnmin = 0.47;

  public static final double turnmax = 1.56;

  public static final double[] buried = { 1.7, 0.1, 0.4, 0.4, 4.6, 0.3, 0.3,
      1.8, 0.8, 3.1, 2.4, 0.05, 1.9, 2.2, 0.6, 0.8, 0.7, 1.6, 0.5, 2.9, 0.4,
      0.3, 1.358, 0.00 };

  public static final double buriedmin = 0.05;

  public static final double buriedmax = 4.6;

  // This is hydropathy index
  // Kyte, J., and Doolittle, R.F., J. Mol. Biol.
  // 1157, 105-132, 1982
  public static final double[] hyd = { 1.8, -4.5, -3.5, -3.5, 2.5, -3.5,
      -3.5, -0.4, -3.2, 4.5, 3.8, -3.9, 1.9, 2.8, -1.6, -0.8, -0.7, -0.9,
      -1.3, 4.2, -3.5, -3.5, -0.49, 0.0 };

  public static final double hydmax = 4.5;

  public static final double hydmin = -3.9;

  // public static final double hydmax = 1.38;
  // public static final double hydmin = -2.53;

  // not currently used
  // public static final Map<String, Color> ssHash = new Hashtable<String,
  // Color>();
  // static
  // {
  // ssHash.put("H", Color.magenta);
  // ssHash.put("E", Color.yellow);
  // ssHash.put("-", Color.white);
  // ssHash.put(".", Color.white);
  // ssHash.put("S", Color.cyan);
  // ssHash.put("T", Color.blue);
  // ssHash.put("G", Color.pink);
  // ssHash.put("I", Color.pink);
  // ssHash.put("B", Color.yellow);
  // }

  /*
   * new Color(60, 136, 238), // U Color.white, // I Color.white, // X
   * Color.white, // R Color.white, // Y Color.white, // N Color.white, // Gap
   */

  public static String STOP = "STOP";

  public static List<String> STOP_CODONS = Arrays.asList("TGA", "TAA",
          "TAG");

  public static String START = "ATG";

  // Stores residue codes/names and colours and other things
  public static Map<String, Map<String, Integer>> propHash = new Hashtable<>();

  public static Map<String, Integer> hydrophobic = new Hashtable<>();

  public static Map<String, Integer> polar = new Hashtable<>();

  public static Map<String, Integer> small = new Hashtable<>();

  public static Map<String, Integer> positive = new Hashtable<>();

  public static Map<String, Integer> negative = new Hashtable<>();

  public static Map<String, Integer> charged = new Hashtable<>();

  public static Map<String, Integer> aromatic = new Hashtable<>();

  public static Map<String, Integer> aliphatic = new Hashtable<>();

  public static Map<String, Integer> tiny = new Hashtable<>();

  public static Map<String, Integer> proline = new Hashtable<>();

  static
  {
    hydrophobic.put("I", ONE);
    hydrophobic.put("L", ONE);
    hydrophobic.put("V", ONE);
    hydrophobic.put("C", ONE);
    hydrophobic.put("A", ONE);
    hydrophobic.put("G", ONE);
    hydrophobic.put("M", ONE);
    hydrophobic.put("F", ONE);
    hydrophobic.put("Y", ONE);
    hydrophobic.put("W", ONE);
    hydrophobic.put("H", ONE);
    hydrophobic.put("K", ONE);
    hydrophobic.put("X", ONE);
    hydrophobic.put("-", ONE);
    hydrophobic.put("*", ONE);
    hydrophobic.put("R", ZERO);
    hydrophobic.put("E", ZERO);
    hydrophobic.put("Q", ZERO);
    hydrophobic.put("D", ZERO);
    hydrophobic.put("N", ZERO);
    hydrophobic.put("S", ZERO);
    hydrophobic.put("T", ONE);
    hydrophobic.put("P", ZERO);
  }

  static
  {
    polar.put("Y", ONE);
    polar.put("W", ONE);
    polar.put("H", ONE);
    polar.put("K", ONE);
    polar.put("R", ONE);
    polar.put("E", ONE);
    polar.put("Q", ONE);
    polar.put("D", ONE);
    polar.put("N", ONE);
    polar.put("S", ONE);
    polar.put("T", ONE);
    polar.put("X", ONE);
    polar.put("-", ONE);
    polar.put("*", ONE);
    polar.put("I", ZERO);
    polar.put("L", ZERO);
    polar.put("V", ZERO);
    polar.put("C", ZERO);
    polar.put("A", ZERO);
    polar.put("G", ZERO);
    polar.put("M", ZERO);
    polar.put("F", ZERO);
    polar.put("P", ZERO);
  }

  static
  {
    small.put("I", ZERO);
    small.put("L", ZERO);
    small.put("V", ONE);
    small.put("C", ONE);
    small.put("A", ONE);
    small.put("G", ONE);
    small.put("M", ZERO);
    small.put("F", ZERO);
    small.put("Y", ZERO);
    small.put("W", ZERO);
    small.put("H", ZERO);
    small.put("K", ZERO);
    small.put("R", ZERO);
    small.put("E", ZERO);
    small.put("Q", ZERO);
    small.put("D", ONE);
    small.put("N", ONE);
    small.put("S", ONE);
    small.put("T", ONE);
    small.put("P", ONE);
    small.put("-", ONE);
    small.put("*", ONE);
  }

  static
  {
    positive.put("I", ZERO);
    positive.put("L", ZERO);
    positive.put("V", ZERO);
    positive.put("C", ZERO);
    positive.put("A", ZERO);
    positive.put("G", ZERO);
    positive.put("M", ZERO);
    positive.put("F", ZERO);
    positive.put("Y", ZERO);
    positive.put("W", ZERO);
    positive.put("H", ONE);
    positive.put("K", ONE);
    positive.put("R", ONE);
    positive.put("E", ZERO);
    positive.put("Q", ZERO);
    positive.put("D", ZERO);
    positive.put("N", ZERO);
    positive.put("S", ZERO);
    positive.put("T", ZERO);
    positive.put("P", ZERO);
    positive.put("-", ONE);
    positive.put("*", ONE);
  }

  static
  {
    negative.put("I", ZERO);
    negative.put("L", ZERO);
    negative.put("V", ZERO);
    negative.put("C", ZERO);
    negative.put("A", ZERO);
    negative.put("G", ZERO);
    negative.put("M", ZERO);
    negative.put("F", ZERO);
    negative.put("Y", ZERO);
    negative.put("W", ZERO);
    negative.put("H", ZERO);
    negative.put("K", ZERO);
    negative.put("R", ZERO);
    negative.put("E", ONE);
    negative.put("Q", ZERO);
    negative.put("D", ONE);
    negative.put("N", ZERO);
    negative.put("S", ZERO);
    negative.put("T", ZERO);
    negative.put("P", ZERO);
    negative.put("-", ONE);
    negative.put("*", ONE);
  }

  static
  {
    charged.put("I", ZERO);
    charged.put("L", ZERO);
    charged.put("V", ZERO);
    charged.put("C", ZERO);
    charged.put("A", ZERO);
    charged.put("G", ZERO);
    charged.put("M", ZERO);
    charged.put("F", ZERO);
    charged.put("Y", ZERO);
    charged.put("W", ZERO);
    charged.put("H", ONE);
    charged.put("K", ONE);
    charged.put("R", ONE);
    charged.put("E", ONE);
    charged.put("Q", ZERO);
    charged.put("D", ONE);
    charged.put("N", ZERO); // Asparagine is polar but not
                            // charged.
    // Alternative would be charged and
    // negative (in basic form)?
    charged.put("S", ZERO);
    charged.put("T", ZERO);
    charged.put("P", ZERO);
    charged.put("-", ONE);
    charged.put("*", ONE);
  }

  static
  {
    aromatic.put("I", ZERO);
    aromatic.put("L", ZERO);
    aromatic.put("V", ZERO);
    aromatic.put("C", ZERO);
    aromatic.put("A", ZERO);
    aromatic.put("G", ZERO);
    aromatic.put("M", ZERO);
    aromatic.put("F", ONE);
    aromatic.put("Y", ONE);
    aromatic.put("W", ONE);
    aromatic.put("H", ONE);
    aromatic.put("K", ZERO);
    aromatic.put("R", ZERO);
    aromatic.put("E", ZERO);
    aromatic.put("Q", ZERO);
    aromatic.put("D", ZERO);
    aromatic.put("N", ZERO);
    aromatic.put("S", ZERO);
    aromatic.put("T", ZERO);
    aromatic.put("P", ZERO);
    aromatic.put("-", ONE);
    aromatic.put("*", ONE);
  }

  static
  {
    aliphatic.put("I", ONE);
    aliphatic.put("L", ONE);
    aliphatic.put("V", ONE);
    aliphatic.put("C", ZERO);
    aliphatic.put("A", ZERO);
    aliphatic.put("G", ZERO);
    aliphatic.put("M", ZERO);
    aliphatic.put("F", ZERO);
    aliphatic.put("Y", ZERO);
    aliphatic.put("W", ZERO);
    aliphatic.put("H", ZERO);
    aliphatic.put("K", ZERO);
    aliphatic.put("R", ZERO);
    aliphatic.put("E", ZERO);
    aliphatic.put("Q", ZERO);
    aliphatic.put("D", ZERO);
    aliphatic.put("N", ZERO);
    aliphatic.put("S", ZERO);
    aliphatic.put("T", ZERO);
    aliphatic.put("P", ZERO);
    aliphatic.put("-", ONE);
    aliphatic.put("*", ONE);
  }

  static
  {
    tiny.put("I", ZERO);
    tiny.put("L", ZERO);
    tiny.put("V", ZERO);
    tiny.put("C", ZERO);
    tiny.put("A", ONE);
    tiny.put("G", ONE);
    tiny.put("M", ZERO);
    tiny.put("F", ZERO);
    tiny.put("Y", ZERO);
    tiny.put("W", ZERO);
    tiny.put("H", ZERO);
    tiny.put("K", ZERO);
    tiny.put("R", ZERO);
    tiny.put("E", ZERO);
    tiny.put("Q", ZERO);
    tiny.put("D", ZERO);
    tiny.put("N", ZERO);
    tiny.put("S", ONE);
    tiny.put("T", ZERO);
    tiny.put("P", ZERO);
    tiny.put("-", ONE);
    tiny.put("*", ONE);
  }

  static
  {
    proline.put("I", ZERO);
    proline.put("L", ZERO);
    proline.put("V", ZERO);
    proline.put("C", ZERO);
    proline.put("A", ZERO);
    proline.put("G", ZERO);
    proline.put("M", ZERO);
    proline.put("F", ZERO);
    proline.put("Y", ZERO);
    proline.put("W", ZERO);
    proline.put("H", ZERO);
    proline.put("K", ZERO);
    proline.put("R", ZERO);
    proline.put("E", ZERO);
    proline.put("Q", ZERO);
    proline.put("D", ZERO);
    proline.put("N", ZERO);
    proline.put("S", ZERO);
    proline.put("T", ZERO);
    proline.put("P", ONE);
    proline.put("-", ONE);
    proline.put("*", ONE);
  }

  static
  {
    propHash.put("hydrophobic", hydrophobic);
    propHash.put("small", small);
    propHash.put("positive", positive);
    propHash.put("negative", negative);
    propHash.put("charged", charged);
    propHash.put("aromatic", aromatic);
    propHash.put("aliphatic", aliphatic);
    propHash.put("tiny", tiny);
    propHash.put("proline", proline);
    propHash.put("polar", polar);
  }
  static
  {
    int[][] propMatrixF = new int[maxProteinIndex][maxProteinIndex],
            propMatrixPos = new int[maxProteinIndex][maxProteinIndex],
            propMatrixEpos = new int[maxProteinIndex][maxProteinIndex];
    for (int i = 0; i < maxProteinIndex; i++)
    {
      int maxF = 0, maxP = 0, maxEP = 0;
      String ic = "";
      if (aa.length > i)
      {
        ic += aa[i];
      }
      else
      {
        ic = "-";
      }
      for (int j = i + 1; j < maxProteinIndex; j++)
      {
        String jc = "";
        if (aa.length > j)
        {
          jc += aa[j];
        }
        else
        {
          jc = "-";
        }
        propMatrixF[i][j] = 0;
        propMatrixPos[i][j] = 0;
        propMatrixEpos[i][j] = 0;
        for (String ph : propHash.keySet())
        {
          Map<String, Integer> pph = propHash.get(ph);
          if (pph.get(ic) != null && pph.get(jc) != null)
          {
            int icp = pph.get(ic).intValue(), jcp = pph.get(jc).intValue();
            // Still working on these definitions.
            propMatrixPos[i][j] += icp == jcp && icp > 0 ? 2 : 0;
            propMatrixPos[j][i] += icp == jcp && icp > 0 ? 2 : 0;
            propMatrixF[i][j] += icp == jcp ? 2 : 0;
            propMatrixF[j][i] += icp == jcp ? 2 : 0;
            propMatrixEpos[i][j] += icp == jcp ? (1 + icp * 2) : 0;
            propMatrixEpos[j][i] += icp == jcp ? (1 + icp * 2) : 0;
          }
        }
        if (maxF < propMatrixF[i][j])
        {
          maxF = propMatrixF[i][j];
        }
        if (maxP < propMatrixPos[i][j])
        {
          maxP = propMatrixPos[i][j];
        }
        if (maxEP < propMatrixEpos[i][j])
        {
          maxEP = propMatrixEpos[i][j];
        }
      }
      propMatrixF[i][i] = maxF;
      propMatrixPos[i][i] = maxP;
      propMatrixEpos[i][i] = maxEP;
    }
  }

  private ResidueProperties()
  {
  }

  public static double getHydmax()
  {
    return hydmax;
  }

  public static double getHydmin()
  {
    return hydmin;
  }

  public static double[] getHyd()
  {
    return hyd;
  }

  public static Map<String, Integer> getAA3Hash()
  {
    return aa3Hash;
  }

  public static String codonTranslate(String lccodon)
  {
    String peptide = GeneticCodes.getInstance().getStandardCodeTable()
            .translate(lccodon);
    if ("*".equals(peptide))
    {
      return "STOP";
    }
    return peptide;
  }

  /*
   * lookup of (A-Z) alternative secondary structure symbols'
   * equivalents in DSSP3 notation
   */
  private static char[] toDssp3State;
  static
  {
    toDssp3State = new char[9]; // for 'A'-'I'; extend if needed
    Arrays.fill(toDssp3State, ' ');
    toDssp3State['B' - 'A'] = 'E';
    toDssp3State['E' - 'A'] = 'E';
    toDssp3State['G' - 'A'] = 'H';
    toDssp3State['H' - 'A'] = 'H';
    toDssp3State['I' - 'A'] = 'H';
  }

  /**
   * translate from other dssp secondary structure alphabets to 3-state
   * 
   * @param ssString
   * @return ssstring
   */
  public static String getDssp3state(String ssString)
  {
    if (ssString == null)
    {
      return null;
    }
    int lookupSize = toDssp3State.length;
    int len = ssString.length();
    char[] trans = new char[len];
    for (int i = 0; i < len; i++)
    {
      char c = ssString.charAt(i);
      int index = c - 'A';
      if (index < 0 || index >= lookupSize)
      {
        trans[i] = ' ';
      }
      else
      {
        trans[i] = toDssp3State[index];
      }
    }
    return new String(trans);
  }

  static
  {
    modifications.put("MSE", "MET"); // Selenomethionine
    // the rest tbc; from
    // http://sourceforge.net/p/jmol/mailman/message/12833570/
    // modifications.put("CSE", "CYS"); // Selenocysteine
    // modifications.put("PTR", "TYR"); // Phosphotyrosine
    // modifications.put("SEP", "SER"); // Phosphoserine
    // modifications.put("HYP", "PRO"); // 4-hydroxyproline
    // modifications.put("5HP", "GLU"); // Pyroglutamic acid; 5-hydroxyproline
    // modifications.put("PCA", "GLU"); // Pyroglutamic acid
    // modifications.put("LYZ", "LYS"); // 5-hydroxylysine

    // Additional protein alphabets used in the SCOP database and PDB files
    // source:
    // https://github.com/biopython/biopython/blob/master/Bio/Data/SCOPData.py
    modifications.put("00C", "CYS");
    modifications.put("01W", "XAA");
    modifications.put("02K", "ALA");
    modifications.put("03Y", "CYS");
    modifications.put("07O", "CYS");
    modifications.put("08P", "CYS");
    modifications.put("0A0", "ASP");
    modifications.put("0A1", "TYR");
    modifications.put("0A2", "LYS");
    modifications.put("0A8", "CYS");
    modifications.put("0AA", "VAL");
    modifications.put("0AB", "VAL");
    modifications.put("0AC", "GLY");
    modifications.put("0AD", "GLY");
    modifications.put("0AF", "TRP");
    modifications.put("0AG", "LEU");
    modifications.put("0AH", "SER");
    modifications.put("0AK", "ASP");
    modifications.put("0AM", "ALA");
    modifications.put("0AP", "CYS");
    modifications.put("0AU", "UR3");
    modifications.put("0AV", "ALA");
    modifications.put("0AZ", "PRO");
    modifications.put("0BN", "PHE");
    modifications.put("0C ", "CYS");
    modifications.put("0CS", "ALA");
    modifications.put("0DC", "CYS");
    modifications.put("0DG", "GLY");
    modifications.put("0DT", "THR");
    modifications.put("0FL", "ALA");
    modifications.put("0G ", "GLY");
    modifications.put("0NC", "ALA");
    modifications.put("0SP", "ALA");
    modifications.put("0U ", "UR3");
    modifications.put("0YG", "YG");
    modifications.put("10C", "CYS");
    modifications.put("125", "UR3");
    modifications.put("126", "UR3");
    modifications.put("127", "UR3");
    modifications.put("128", "ASN");
    modifications.put("12A", "ALA");
    modifications.put("143", "CYS");
    modifications.put("175", "ASG");
    modifications.put("193", "XAA");
    modifications.put("1AP", "ALA");
    modifications.put("1MA", "ALA");
    modifications.put("1MG", "GLY");
    modifications.put("1PA", "PHE");
    modifications.put("1PI", "ALA");
    modifications.put("1PR", "ASN");
    modifications.put("1SC", "CYS");
    modifications.put("1TQ", "TRP");
    modifications.put("1TY", "TYR");
    modifications.put("1X6", "SER");
    modifications.put("200", "PHE");
    modifications.put("23F", "PHE");
    modifications.put("23S", "XAA");
    modifications.put("26B", "THR");
    modifications.put("2AD", "XAA");
    modifications.put("2AG", "ALA");
    modifications.put("2AO", "XAA");
    modifications.put("2AR", "ALA");
    modifications.put("2AS", "XAA");
    modifications.put("2AT", "THR");
    modifications.put("2AU", "UR3");
    modifications.put("2BD", "ILE");
    modifications.put("2BT", "THR");
    modifications.put("2BU", "ALA");
    modifications.put("2CO", "CYS");
    modifications.put("2DA", "ALA");
    modifications.put("2DF", "ASN");
    modifications.put("2DM", "ASN");
    modifications.put("2DO", "XAA");
    modifications.put("2DT", "THR");
    modifications.put("2EG", "GLY");
    modifications.put("2FE", "ASN");
    modifications.put("2FI", "ASN");
    modifications.put("2FM", "MET");
    modifications.put("2GT", "THR");
    modifications.put("2HF", "HIS");
    modifications.put("2LU", "LEU");
    modifications.put("2MA", "ALA");
    modifications.put("2MG", "GLY");
    modifications.put("2ML", "LEU");
    modifications.put("2MR", "ARG");
    modifications.put("2MT", "PRO");
    modifications.put("2MU", "UR3");
    modifications.put("2NT", "THR");
    modifications.put("2OM", "UR3");
    modifications.put("2OT", "THR");
    modifications.put("2PI", "XAA");
    modifications.put("2PR", "GLY");
    modifications.put("2SA", "ASN");
    modifications.put("2SI", "XAA");
    modifications.put("2ST", "THR");
    modifications.put("2TL", "THR");
    modifications.put("2TY", "TYR");
    modifications.put("2VA", "VAL");
    modifications.put("2XA", "CYS");
    modifications.put("32S", "XAA");
    modifications.put("32T", "XAA");
    modifications.put("3AH", "HIS");
    modifications.put("3AR", "XAA");
    modifications.put("3CF", "PHE");
    modifications.put("3DA", "ALA");
    modifications.put("3DR", "ASN");
    modifications.put("3GA", "ALA");
    modifications.put("3MD", "ASP");
    modifications.put("3ME", "UR3");
    modifications.put("3NF", "TYR");
    modifications.put("3QN", "LYS");
    modifications.put("3TY", "XAA");
    modifications.put("3XH", "GLY");
    modifications.put("4AC", "ASN");
    modifications.put("4BF", "TYR");
    modifications.put("4CF", "PHE");
    modifications.put("4CY", "MET");
    modifications.put("4DP", "TRP");
    modifications.put("4F3", "GYG");
    modifications.put("4FB", "PRO");
    modifications.put("4FW", "TRP");
    modifications.put("4HT", "TRP");
    modifications.put("4IN", "TRP");
    modifications.put("4MF", "ASN");
    modifications.put("4MM", "XAA");
    modifications.put("4OC", "CYS");
    modifications.put("4PC", "CYS");
    modifications.put("4PD", "CYS");
    modifications.put("4PE", "CYS");
    modifications.put("4PH", "PHE");
    modifications.put("4SC", "CYS");
    modifications.put("4SU", "UR3");
    modifications.put("4TA", "ASN");
    modifications.put("4U7", "ALA");
    modifications.put("56A", "HIS");
    modifications.put("5AA", "ALA");
    modifications.put("5AB", "ALA");
    modifications.put("5AT", "THR");
    modifications.put("5BU", "UR3");
    modifications.put("5CG", "GLY");
    modifications.put("5CM", "CYS");
    modifications.put("5CS", "CYS");
    modifications.put("5FA", "ALA");
    modifications.put("5FC", "CYS");
    modifications.put("5FU", "UR3");
    modifications.put("5HP", "GLU");
    modifications.put("5HT", "THR");
    modifications.put("5HU", "UR3");
    modifications.put("5IC", "CYS");
    modifications.put("5IT", "THR");
    modifications.put("5IU", "UR3");
    modifications.put("5MC", "CYS");
    modifications.put("5MD", "ASN");
    modifications.put("5MU", "UR3");
    modifications.put("5NC", "CYS");
    modifications.put("5PC", "CYS");
    modifications.put("5PY", "THR");
    modifications.put("5SE", "UR3");
    modifications.put("5ZA", "TWG");
    modifications.put("64T", "THR");
    modifications.put("6CL", "LYS");
    modifications.put("6CT", "THR");
    modifications.put("6CW", "TRP");
    modifications.put("6HA", "ALA");
    modifications.put("6HC", "CYS");
    modifications.put("6HG", "GLY");
    modifications.put("6HN", "LYS");
    modifications.put("6HT", "THR");
    modifications.put("6IA", "ALA");
    modifications.put("6MA", "ALA");
    modifications.put("6MC", "ALA");
    modifications.put("6MI", "ASN");
    modifications.put("6MT", "ALA");
    modifications.put("6MZ", "ASN");
    modifications.put("6OG", "GLY");
    modifications.put("70U", "UR3");
    modifications.put("7DA", "ALA");
    modifications.put("7GU", "GLY");
    modifications.put("7JA", "ILE");
    modifications.put("7MG", "GLY");
    modifications.put("8AN", "ALA");
    modifications.put("8FG", "GLY");
    modifications.put("8MG", "GLY");
    modifications.put("8OG", "GLY");
    modifications.put("9NE", "GLU");
    modifications.put("9NF", "PHE");
    modifications.put("9NR", "ARG");
    modifications.put("9NV", "VAL");
    modifications.put("A  ", "ALA");
    modifications.put("A1P", "ASN");
    modifications.put("A23", "ALA");
    modifications.put("A2L", "ALA");
    modifications.put("A2M", "ALA");
    modifications.put("A34", "ALA");
    modifications.put("A35", "ALA");
    modifications.put("A38", "ALA");
    modifications.put("A39", "ALA");
    modifications.put("A3A", "ALA");
    modifications.put("A3P", "ALA");
    modifications.put("A40", "ALA");
    modifications.put("A43", "ALA");
    modifications.put("A44", "ALA");
    modifications.put("A47", "ALA");
    modifications.put("A5L", "ALA");
    modifications.put("A5M", "CYS");
    modifications.put("A5N", "ASN");
    modifications.put("A5O", "ALA");
    modifications.put("A66", "XAA");
    modifications.put("AA3", "ALA");
    modifications.put("AA4", "ALA");
    modifications.put("AAR", "ARG");
    modifications.put("AB7", "XAA");
    modifications.put("ABA", "ALA");
    modifications.put("ABR", "ALA");
    modifications.put("ABS", "ALA");
    modifications.put("ABT", "ASN");
    modifications.put("ACB", "ASP");
    modifications.put("ACL", "ARG");
    modifications.put("AD2", "ALA");
    modifications.put("ADD", "XAA");
    modifications.put("ADX", "ASN");
    modifications.put("AEA", "XAA");
    modifications.put("AEI", "ASP");
    modifications.put("AET", "ALA");
    modifications.put("AFA", "ASN");
    modifications.put("AFF", "ASN");
    modifications.put("AFG", "GLY");
    modifications.put("AGM", "ARG");
    modifications.put("AGT", "CYS");
    modifications.put("AHB", "ASN");
    modifications.put("AHH", "XAA");
    modifications.put("AHO", "ALA");
    modifications.put("AHP", "ALA");
    modifications.put("AHS", "XAA");
    modifications.put("AHT", "XAA");
    modifications.put("AIB", "ALA");
    modifications.put("AKL", "ASP");
    modifications.put("AKZ", "ASP");
    modifications.put("ALA", "ALA");
    modifications.put("ALC", "ALA");
    modifications.put("ALM", "ALA");
    modifications.put("ALN", "ALA");
    modifications.put("ALO", "THR");
    modifications.put("ALQ", "XAA");
    modifications.put("ALS", "ALA");
    modifications.put("ALT", "ALA");
    modifications.put("ALV", "ALA");
    modifications.put("ALY", "LYS");
    modifications.put("AN8", "ALA");
    modifications.put("AP7", "ALA");
    modifications.put("APE", "XAA");
    modifications.put("APH", "ALA");
    modifications.put("API", "LYS");
    modifications.put("APK", "LYS");
    modifications.put("APM", "XAA");
    modifications.put("APP", "XAA");
    modifications.put("AR2", "ARG");
    modifications.put("AR4", "GLU");
    modifications.put("AR7", "ARG");
    modifications.put("ARG", "ARG");
    modifications.put("ARM", "ARG");
    modifications.put("ARO", "ARG");
    modifications.put("ARV", "XAA");
    modifications.put("AS ", "ALA");
    modifications.put("AS2", "ASP");
    modifications.put("AS9", "XAA");
    modifications.put("ASA", "ASP");
    modifications.put("ASB", "ASP");
    modifications.put("ASI", "ASP");
    modifications.put("ASK", "ASP");
    modifications.put("ASL", "ASP");
    modifications.put("ASM", "XAA");
    modifications.put("ASN", "ASN");
    modifications.put("ASP", "ASP");
    modifications.put("ASQ", "ASP");
    modifications.put("ASU", "ASN");
    modifications.put("ASX", "ASX");
    modifications.put("ATD", "THR");
    modifications.put("ATL", "THR");
    modifications.put("ATM", "THR");
    modifications.put("AVC", "ALA");
    modifications.put("AVN", "XAA");
    modifications.put("AYA", "ALA");
    modifications.put("AYG", "AYG");
    modifications.put("AZK", "LYS");
    modifications.put("AZS", "SER");
    modifications.put("AZY", "TYR");
    modifications.put("B1F", "PHE");
    modifications.put("B1P", "ASN");
    modifications.put("B2A", "ALA");
    modifications.put("B2F", "PHE");
    modifications.put("B2I", "ILE");
    modifications.put("B2V", "VAL");
    modifications.put("B3A", "ALA");
    modifications.put("B3D", "ASP");
    modifications.put("B3E", "GLU");
    modifications.put("B3K", "LYS");
    modifications.put("B3L", "XAA");
    modifications.put("B3M", "XAA");
    modifications.put("B3Q", "XAA");
    modifications.put("B3S", "SER");
    modifications.put("B3T", "XAA");
    modifications.put("B3U", "HIS");
    modifications.put("B3X", "ASN");
    modifications.put("B3Y", "TYR");
    modifications.put("BB6", "CYS");
    modifications.put("BB7", "CYS");
    modifications.put("BB8", "PHE");
    modifications.put("BB9", "CYS");
    modifications.put("BBC", "CYS");
    modifications.put("BCS", "CYS");
    modifications.put("BE2", "XAA");
    modifications.put("BFD", "ASP");
    modifications.put("BG1", "SER");
    modifications.put("BGM", "GLY");
    modifications.put("BH2", "ASP");
    modifications.put("BHD", "ASP");
    modifications.put("BIF", "PHE");
    modifications.put("BIL", "XAA");
    modifications.put("BIU", "ILE");
    modifications.put("BJH", "XAA");
    modifications.put("BLE", "LEU");
    modifications.put("BLY", "LYS");
    modifications.put("BMP", "ASN");
    modifications.put("BMT", "THR");
    modifications.put("BNN", "PHE");
    modifications.put("BNO", "XAA");
    modifications.put("BOE", "THR");
    modifications.put("BOR", "ARG");
    modifications.put("BPE", "CYS");
    modifications.put("BRU", "UR3");
    modifications.put("BSE", "SER");
    modifications.put("BT5", "ASN");
    modifications.put("BTA", "LEU");
    modifications.put("BTC", "CYS");
    modifications.put("BTR", "TRP");
    modifications.put("BUC", "CYS");
    modifications.put("BUG", "VAL");
    modifications.put("BVP", "UR3");
    modifications.put("BZG", "ASN");
    modifications.put("C  ", "CYS");
    modifications.put("C12", "TYG");
    modifications.put("C1X", "LYS");
    modifications.put("C25", "CYS");
    modifications.put("C2L", "CYS");
    modifications.put("C2S", "CYS");
    modifications.put("C31", "CYS");
    modifications.put("C32", "CYS");
    modifications.put("C34", "CYS");
    modifications.put("C36", "CYS");
    modifications.put("C37", "CYS");
    modifications.put("C38", "CYS");
    modifications.put("C3Y", "CYS");
    modifications.put("C42", "CYS");
    modifications.put("C43", "CYS");
    modifications.put("C45", "CYS");
    modifications.put("C46", "CYS");
    modifications.put("C49", "CYS");
    modifications.put("C4R", "CYS");
    modifications.put("C4S", "CYS");
    modifications.put("C5C", "CYS");
    modifications.put("C66", "XAA");
    modifications.put("C6C", "CYS");
    modifications.put("C99", "TFG");
    modifications.put("CAF", "CYS");
    modifications.put("CAL", "XAA");
    modifications.put("CAR", "CYS");
    modifications.put("CAS", "CYS");
    modifications.put("CAV", "XAA");
    modifications.put("CAY", "CYS");
    modifications.put("CB2", "CYS");
    modifications.put("CBR", "CYS");
    modifications.put("CBV", "CYS");
    modifications.put("CCC", "CYS");
    modifications.put("CCL", "LYS");
    modifications.put("CCS", "CYS");
    modifications.put("CCY", "CYG");
    modifications.put("CDE", "XAA");
    modifications.put("CDV", "XAA");
    modifications.put("CDW", "CYS");
    modifications.put("CEA", "CYS");
    modifications.put("CFL", "CYS");
    modifications.put("CFY", "FCYG"); // check
    modifications.put("CG1", "GLY");
    modifications.put("CGA", "GLU");
    modifications.put("CGU", "GLU");
    modifications.put("CH ", "CYS");
    modifications.put("CH6", "MYG");
    modifications.put("CH7", "KYG");
    modifications.put("CHF", "XAA");
    modifications.put("CHG", "XAA");
    modifications.put("CHP", "GLY");
    modifications.put("CHS", "XAA");
    modifications.put("CIR", "ARG");
    modifications.put("CJO", "GYG");
    modifications.put("CLE", "LEU");
    modifications.put("CLG", "LYS");
    modifications.put("CLH", "LYS");
    modifications.put("CLV", "AFG");
    modifications.put("CM0", "ASN");
    modifications.put("CME", "CYS");
    modifications.put("CMH", "CYS");
    modifications.put("CML", "CYS");
    modifications.put("CMR", "CYS");
    modifications.put("CMT", "CYS");
    modifications.put("CNU", "UR3");
    modifications.put("CP1", "CYS");
    modifications.put("CPC", "XAA");
    modifications.put("CPI", "XAA");
    modifications.put("CQR", "GYG");
    modifications.put("CR0", "TLG");
    modifications.put("CR2", "GYG");
    modifications.put("CR5", "GLY");
    modifications.put("CR7", "KYG");
    modifications.put("CR8", "HYG");
    modifications.put("CRF", "TWG");
    modifications.put("CRG", "THG");
    modifications.put("CRK", "MYG");
    modifications.put("CRO", "GYG");
    modifications.put("CRQ", "QYG");
    modifications.put("CRU", "EYG");
    modifications.put("CRW", "ASG");
    modifications.put("CRX", "ASG");
    modifications.put("CS0", "CYS");
    modifications.put("CS1", "CYS");
    modifications.put("CS3", "CYS");
    modifications.put("CS4", "CYS");
    modifications.put("CS8", "ASN");
    modifications.put("CSA", "CYS");
    modifications.put("CSB", "CYS");
    modifications.put("CSD", "CYS");
    modifications.put("CSE", "CYS");
    modifications.put("CSF", "CYS");
    modifications.put("CSH", "SHG");
    modifications.put("CSI", "GLY");
    modifications.put("CSJ", "CYS");
    modifications.put("CSL", "CYS");
    modifications.put("CSO", "CYS");
    modifications.put("CSP", "CYS");
    modifications.put("CSR", "CYS");
    modifications.put("CSS", "CYS");
    modifications.put("CSU", "CYS");
    modifications.put("CSW", "CYS");
    modifications.put("CSX", "CYS");
    modifications.put("CSY", "SYG");
    modifications.put("CSZ", "CYS");
    modifications.put("CTE", "TRP");
    modifications.put("CTG", "THR");
    modifications.put("CTH", "THR");
    modifications.put("CUC", "XAA");
    modifications.put("CWR", "SER");
    modifications.put("CXM", "MET");
    modifications.put("CY0", "CYS");
    modifications.put("CY1", "CYS");
    modifications.put("CY3", "CYS");
    modifications.put("CY4", "CYS");
    modifications.put("CYA", "CYS");
    modifications.put("CYD", "CYS");
    modifications.put("CYF", "CYS");
    modifications.put("CYG", "CYS");
    modifications.put("CYJ", "XAA");
    modifications.put("CYM", "CYS");
    modifications.put("CYQ", "CYS");
    modifications.put("CYR", "CYS");
    modifications.put("CYS", "CYS");
    modifications.put("CZ2", "CYS");
    modifications.put("CZO", "GYG");
    modifications.put("CZZ", "CYS");
    modifications.put("D11", "THR");
    modifications.put("D1P", "ASN");
    modifications.put("D3 ", "ASN");
    modifications.put("D33", "ASN");
    modifications.put("D3P", "GLY");
    modifications.put("D3T", "THR");
    modifications.put("D4M", "THR");
    modifications.put("D4P", "XAA");
    modifications.put("DA ", "ALA");
    modifications.put("DA2", "XAA");
    modifications.put("DAB", "ALA");
    modifications.put("DAH", "PHE");
    modifications.put("DAL", "ALA");
    modifications.put("DAR", "ARG");
    modifications.put("DAS", "ASP");
    modifications.put("DBB", "THR");
    modifications.put("DBM", "ASN");
    modifications.put("DBS", "SER");
    modifications.put("DBU", "THR");
    modifications.put("DBY", "TYR");
    modifications.put("DBZ", "ALA");
    modifications.put("DC ", "CYS");
    modifications.put("DC2", "CYS");
    modifications.put("DCG", "GLY");
    modifications.put("DCI", "XAA");
    modifications.put("DCL", "XAA");
    modifications.put("DCT", "CYS");
    modifications.put("DCY", "CYS");
    modifications.put("DDE", "HIS");
    modifications.put("DDG", "GLY");
    modifications.put("DDN", "UR3");
    modifications.put("DDX", "ASN");
    modifications.put("DFC", "CYS");
    modifications.put("DFG", "GLY");
    modifications.put("DFI", "XAA");
    modifications.put("DFO", "XAA");
    modifications.put("DFT", "ASN");
    modifications.put("DG ", "GLY");
    modifications.put("DGH", "GLY");
    modifications.put("DGI", "GLY");
    modifications.put("DGL", "GLU");
    modifications.put("DGN", "GLN");
    modifications.put("DHA", "SER");
    modifications.put("DHI", "HIS");
    modifications.put("DHL", "XAA");
    modifications.put("DHN", "VAL");
    modifications.put("DHP", "XAA");
    modifications.put("DHU", "UR3");
    modifications.put("DHV", "VAL");
    modifications.put("DI ", "ILE");
    modifications.put("DIL", "ILE");
    modifications.put("DIR", "ARG");
    modifications.put("DIV", "VAL");
    modifications.put("DLE", "LEU");
    modifications.put("DLS", "LYS");
    modifications.put("DLY", "LYS");
    modifications.put("DM0", "LYS");
    modifications.put("DMH", "ASN");
    modifications.put("DMK", "ASP");
    modifications.put("DMT", "XAA");
    modifications.put("DN ", "ASN");
    modifications.put("DNE", "LEU");
    modifications.put("DNG", "LEU");
    modifications.put("DNL", "LYS");
    modifications.put("DNM", "LEU");
    modifications.put("DNP", "ALA");
    modifications.put("DNR", "CYS");
    modifications.put("DNS", "LYS");
    modifications.put("DOA", "XAA");
    modifications.put("DOC", "CYS");
    modifications.put("DOH", "ASP");
    modifications.put("DON", "LEU");
    modifications.put("DPB", "THR");
    modifications.put("DPH", "PHE");
    modifications.put("DPL", "PRO");
    modifications.put("DPP", "ALA");
    modifications.put("DPQ", "TYR");
    modifications.put("DPR", "PRO");
    modifications.put("DPY", "ASN");
    modifications.put("DRM", "UR3");
    modifications.put("DRP", "ASN");
    modifications.put("DRT", "THR");
    modifications.put("DRZ", "ASN");
    modifications.put("DSE", "SER");
    modifications.put("DSG", "ASN");
    modifications.put("DSN", "SER");
    modifications.put("DSP", "ASP");
    modifications.put("DT ", "THR");
    modifications.put("DTH", "THR");
    modifications.put("DTR", "TRP");
    modifications.put("DTY", "TYR");
    modifications.put("DU ", "UR3");
    modifications.put("DVA", "VAL");
    modifications.put("DXD", "ASN");
    modifications.put("DXN", "ASN");
    modifications.put("DYG", "DYG");
    modifications.put("DYS", "CYS");
    modifications.put("DZM", "ALA");
    modifications.put("E  ", "ALA");
    modifications.put("E1X", "ALA");
    modifications.put("ECC", "GLN");
    modifications.put("EDA", "ALA");
    modifications.put("EFC", "CYS");
    modifications.put("EHP", "PHE");
    modifications.put("EIT", "THR");
    modifications.put("ENP", "ASN");
    modifications.put("ESB", "TYR");
    modifications.put("ESC", "MET");
    modifications.put("EXB", "XAA");
    modifications.put("EXY", "LEU");
    modifications.put("EY5", "ASN");
    modifications.put("EYS", "XAA");
    modifications.put("F2F", "PHE");
    modifications.put("FA2", "ALA");
    modifications.put("FA5", "ASN");
    modifications.put("FAG", "ASN");
    modifications.put("FAI", "ASN");
    modifications.put("FB5", "ALA");
    modifications.put("FB6", "ALA");
    modifications.put("FCL", "PHE");
    modifications.put("FFD", "ASN");
    modifications.put("FGA", "GLU");
    modifications.put("FGL", "GLY");
    modifications.put("FGP", "SER");
    modifications.put("FHL", "XAA");
    modifications.put("FHO", "LYS");
    modifications.put("FHU", "UR3");
    modifications.put("FLA", "ALA");
    modifications.put("FLE", "LEU");
    modifications.put("FLT", "TYR");
    modifications.put("FME", "MET");
    modifications.put("FMG", "GLY");
    modifications.put("FMU", "ASN");
    modifications.put("FOE", "CYS");
    modifications.put("FOX", "GLY");
    modifications.put("FP9", "PRO");
    modifications.put("FPA", "PHE");
    modifications.put("FRD", "XAA");
    modifications.put("FT6", "TRP");
    modifications.put("FTR", "TRP");
    modifications.put("FTY", "TYR");
    modifications.put("FVA", "VAL");
    modifications.put("FZN", "LYS");
    modifications.put("G  ", "GLY");
    modifications.put("G25", "GLY");
    modifications.put("G2L", "GLY");
    modifications.put("G2S", "GLY");
    modifications.put("G31", "GLY");
    modifications.put("G32", "GLY");
    modifications.put("G33", "GLY");
    modifications.put("G36", "GLY");
    modifications.put("G38", "GLY");
    modifications.put("G42", "GLY");
    modifications.put("G46", "GLY");
    modifications.put("G47", "GLY");
    modifications.put("G48", "GLY");
    modifications.put("G49", "GLY");
    modifications.put("G4P", "ASN");
    modifications.put("G7M", "GLY");
    modifications.put("GAO", "GLY");
    modifications.put("GAU", "GLU");
    modifications.put("GCK", "CYS");
    modifications.put("GCM", "XAA");
    modifications.put("GDP", "GLY");
    modifications.put("GDR", "GLY");
    modifications.put("GFL", "GLY");
    modifications.put("GGL", "GLU");
    modifications.put("GH3", "GLY");
    modifications.put("GHG", "GLN");
    modifications.put("GHP", "GLY");
    modifications.put("GL3", "GLY");
    modifications.put("GLH", "GLN");
    modifications.put("GLJ", "GLU");
    modifications.put("GLK", "GLU");
    modifications.put("GLM", "XAA");
    modifications.put("GLN", "GLN");
    modifications.put("GLQ", "GLU");
    modifications.put("GLU", "GLU");
    modifications.put("GLX", "GLX");
    modifications.put("GLY", "GLY");
    modifications.put("GLZ", "GLY");
    modifications.put("GMA", "GLU");
    modifications.put("GMS", "GLY");
    modifications.put("GMU", "UR3");
    modifications.put("GN7", "GLY");
    modifications.put("GND", "XAA");
    modifications.put("GNE", "ASN");
    modifications.put("GOM", "GLY");
    modifications.put("GPL", "LYS");
    modifications.put("GS ", "GLY");
    modifications.put("GSC", "GLY");
    modifications.put("GSR", "GLY");
    modifications.put("GSS", "GLY");
    modifications.put("GSU", "GLU");
    modifications.put("GT9", "CYS");
    modifications.put("GTP", "GLY");
    modifications.put("GVL", "XAA");
    modifications.put("GYC", "CYG");
    modifications.put("GYS", "SYG");
    modifications.put("H2U", "UR3");
    modifications.put("H5M", "PRO");
    modifications.put("HAC", "ALA");
    modifications.put("HAR", "ARG");
    modifications.put("HBN", "HIS");
    modifications.put("HCS", "XAA");
    modifications.put("HDP", "UR3");
    modifications.put("HEU", "UR3");
    modifications.put("HFA", "XAA");
    modifications.put("HGL", "XAA");
    modifications.put("HHI", "HIS");
    modifications.put("HHK", "AK"); // check
    modifications.put("HIA", "HIS");
    modifications.put("HIC", "HIS");
    modifications.put("HIP", "HIS");
    modifications.put("HIQ", "HIS");
    modifications.put("HIS", "HIS");
    modifications.put("HL2", "LEU");
    modifications.put("HLU", "LEU");
    modifications.put("HMR", "ARG");
    modifications.put("HOL", "ASN");
    modifications.put("HPC", "PHE");
    modifications.put("HPE", "PHE");
    modifications.put("HPH", "PHE");
    modifications.put("HPQ", "PHE");
    modifications.put("HQA", "ALA");
    modifications.put("HRG", "ARG");
    modifications.put("HRP", "TRP");
    modifications.put("HS8", "HIS");
    modifications.put("HS9", "HIS");
    modifications.put("HSE", "SER");
    modifications.put("HSL", "SER");
    modifications.put("HSO", "HIS");
    modifications.put("HTI", "CYS");
    modifications.put("HTN", "ASN");
    modifications.put("HTR", "TRP");
    modifications.put("HV5", "ALA");
    modifications.put("HVA", "VAL");
    modifications.put("HY3", "PRO");
    modifications.put("HYP", "PRO");
    modifications.put("HZP", "PRO");
    modifications.put("I  ", "ILE");
    modifications.put("I2M", "ILE");
    modifications.put("I58", "LYS");
    modifications.put("I5C", "CYS");
    modifications.put("IAM", "ALA");
    modifications.put("IAR", "ARG");
    modifications.put("IAS", "ASP");
    modifications.put("IC ", "CYS");
    modifications.put("IEL", "LYS");
    modifications.put("IEY", "HYG");
    modifications.put("IG ", "GLY");
    modifications.put("IGL", "GLY");
    modifications.put("IGU", "GLY");
    modifications.put("IIC", "SHG");
    modifications.put("IIL", "ILE");
    modifications.put("ILE", "ILE");
    modifications.put("ILG", "GLU");
    modifications.put("ILX", "ILE");
    modifications.put("IMC", "CYS");
    modifications.put("IML", "ILE");
    modifications.put("IOY", "PHE");
    modifications.put("IPG", "GLY");
    modifications.put("IPN", "ASN");
    modifications.put("IRN", "ASN");
    modifications.put("IT1", "LYS");
    modifications.put("IU ", "UR3");
    modifications.put("IYR", "TYR");
    modifications.put("IYT", "THR");
    modifications.put("IZO", "MET");
    modifications.put("JJJ", "CYS");
    modifications.put("JJK", "CYS");
    modifications.put("JJL", "CYS");
    modifications.put("JW5", "ASN");
    modifications.put("K1R", "CYS");
    modifications.put("KAG", "GLY");
    modifications.put("KCX", "LYS");
    modifications.put("KGC", "LYS");
    modifications.put("KNB", "ALA");
    modifications.put("KOR", "MET");
    modifications.put("KPI", "LYS");
    modifications.put("KST", "LYS");
    modifications.put("KYQ", "LYS");
    modifications.put("L2A", "XAA");
    modifications.put("LA2", "LYS");
    modifications.put("LAA", "ASP");
    modifications.put("LAL", "ALA");
    modifications.put("LBY", "LYS");
    modifications.put("LC ", "CYS");
    modifications.put("LCA", "ALA");
    modifications.put("LCC", "ASN");
    modifications.put("LCG", "GLY");
    modifications.put("LCH", "ASN");
    modifications.put("LCK", "LYS");
    modifications.put("LCX", "LYS");
    modifications.put("LDH", "LYS");
    modifications.put("LED", "LEU");
    modifications.put("LEF", "LEU");
    modifications.put("LEH", "LEU");
    modifications.put("LEI", "VAL");
    modifications.put("LEM", "LEU");
    modifications.put("LEN", "LEU");
    modifications.put("LET", "XAA");
    modifications.put("LEU", "LEU");
    modifications.put("LEX", "LEU");
    modifications.put("LG ", "GLY");
    modifications.put("LGP", "GLY");
    modifications.put("LHC", "XAA");
    modifications.put("LHU", "UR3");
    modifications.put("LKC", "ASN");
    modifications.put("LLP", "LYS");
    modifications.put("LLY", "LYS");
    modifications.put("LME", "GLU");
    modifications.put("LMF", "LYS");
    modifications.put("LMQ", "GLN");
    modifications.put("LMS", "ASN");
    modifications.put("LP6", "LYS");
    modifications.put("LPD", "PRO");
    modifications.put("LPG", "GLY");
    modifications.put("LPL", "XAA");
    modifications.put("LPS", "SER");
    modifications.put("LSO", "XAA");
    modifications.put("LTA", "XAA");
    modifications.put("LTR", "TRP");
    modifications.put("LVG", "GLY");
    modifications.put("LVN", "VAL");
    modifications.put("LYF", "LYS");
    modifications.put("LYK", "LYS");
    modifications.put("LYM", "LYS");
    modifications.put("LYN", "LYS");
    modifications.put("LYR", "LYS");
    modifications.put("LYS", "LYS");
    modifications.put("LYX", "LYS");
    modifications.put("LYZ", "LYS");
    modifications.put("M0H", "CYS");
    modifications.put("M1G", "GLY");
    modifications.put("M2G", "GLY");
    modifications.put("M2L", "LYS");
    modifications.put("M2S", "MET");
    modifications.put("M30", "GLY");
    modifications.put("M3L", "LYS");
    modifications.put("M5M", "CYS");
    modifications.put("MA ", "ALA");
    modifications.put("MA6", "ALA");
    modifications.put("MA7", "ALA");
    modifications.put("MAA", "ALA");
    modifications.put("MAD", "ALA");
    modifications.put("MAI", "ARG");
    modifications.put("MBQ", "TYR");
    modifications.put("MBZ", "ASN");
    modifications.put("MC1", "SER");
    modifications.put("MCG", "XAA");
    modifications.put("MCL", "LYS");
    modifications.put("MCS", "CYS");
    modifications.put("MCY", "CYS");
    modifications.put("MD3", "CYS");
    modifications.put("MD6", "GLY");
    modifications.put("MDH", "XAA");
    modifications.put("MDO", "ASG");
    modifications.put("MDR", "ASN");
    modifications.put("MEA", "PHE");
    modifications.put("MED", "MET");
    modifications.put("MEG", "GLU");
    modifications.put("MEN", "ASN");
    modifications.put("MEP", "UR3");
    modifications.put("MEQ", "GLN");
    modifications.put("MET", "MET");
    modifications.put("MEU", "GLY");
    modifications.put("MF3", "XAA");
    modifications.put("MFC", "GYG");
    modifications.put("MG1", "GLY");
    modifications.put("MGG", "ARG");
    modifications.put("MGN", "GLN");
    modifications.put("MGQ", "ALA");
    modifications.put("MGV", "GLY");
    modifications.put("MGY", "GLY");
    modifications.put("MHL", "LEU");
    modifications.put("MHO", "MET");
    modifications.put("MHS", "HIS");
    modifications.put("MIA", "ALA");
    modifications.put("MIS", "SER");
    modifications.put("MK8", "LEU");
    modifications.put("ML3", "LYS");
    modifications.put("MLE", "LEU");
    modifications.put("MLL", "LEU");
    modifications.put("MLY", "LYS");
    modifications.put("MLZ", "LYS");
    modifications.put("MME", "MET");
    modifications.put("MMO", "ARG");
    modifications.put("MMT", "THR");
    modifications.put("MND", "ASN");
    modifications.put("MNL", "LEU");
    modifications.put("MNU", "UR3");
    modifications.put("MNV", "VAL");
    modifications.put("MOD", "XAA");
    modifications.put("MP8", "PRO");
    modifications.put("MPH", "XAA");
    modifications.put("MPJ", "XAA");
    modifications.put("MPQ", "GLY");
    modifications.put("MRG", "GLY");
    modifications.put("MSA", "GLY");
    modifications.put("MSE", "MET");
    modifications.put("MSL", "MET");
    modifications.put("MSO", "MET");
    modifications.put("MSP", "XAA");
    modifications.put("MT2", "MET");
    modifications.put("MTR", "THR");
    modifications.put("MTU", "ALA");
    modifications.put("MTY", "TYR");
    modifications.put("MVA", "VAL");
    modifications.put("N  ", "ASN");
    modifications.put("N10", "SER");
    modifications.put("N2C", "XAA");
    modifications.put("N5I", "ASN");
    modifications.put("N5M", "CYS");
    modifications.put("N6G", "GLY");
    modifications.put("N7P", "PRO");
    modifications.put("NA8", "ALA");
    modifications.put("NAL", "ALA");
    modifications.put("NAM", "ALA");
    modifications.put("NB8", "ASN");
    modifications.put("NBQ", "TYR");
    modifications.put("NC1", "SER");
    modifications.put("NCB", "ALA");
    modifications.put("NCX", "ASN");
    modifications.put("NCY", "XAA");
    modifications.put("NDF", "PHE");
    modifications.put("NDN", "UR3");
    modifications.put("NEM", "HIS");
    modifications.put("NEP", "HIS");
    modifications.put("NF2", "ASN");
    modifications.put("NFA", "PHE");
    modifications.put("NHL", "GLU");
    modifications.put("NIT", "XAA");
    modifications.put("NIY", "TYR");
    modifications.put("NLE", "LEU");
    modifications.put("NLN", "LEU");
    modifications.put("NLO", "LEU");
    modifications.put("NLP", "LEU");
    modifications.put("NLQ", "GLN");
    modifications.put("NMC", "GLY");
    modifications.put("NMM", "ARG");
    modifications.put("NMS", "THR");
    modifications.put("NMT", "THR");
    modifications.put("NNH", "ARG");
    modifications.put("NP3", "ASN");
    modifications.put("NPH", "CYS");
    modifications.put("NPI", "ALA");
    modifications.put("NRP", "LYG");
    modifications.put("NRQ", "MYG");
    modifications.put("NSK", "XAA");
    modifications.put("NTY", "TYR");
    modifications.put("NVA", "VAL");
    modifications.put("NYC", "TWG");
    modifications.put("NYG", "NYG");
    modifications.put("NYM", "ASN");
    modifications.put("NYS", "CYS");
    modifications.put("NZH", "HIS");
    modifications.put("O12", "XAA");
    modifications.put("O2C", "ASN");
    modifications.put("O2G", "GLY");
    modifications.put("OAD", "ASN");
    modifications.put("OAS", "SER");
    modifications.put("OBF", "XAA");
    modifications.put("OBS", "XAA");
    modifications.put("OCS", "CYS");
    modifications.put("OCY", "CYS");
    modifications.put("ODP", "ASN");
    modifications.put("OHI", "HIS");
    modifications.put("OHS", "ASP");
    modifications.put("OIC", "XAA");
    modifications.put("OIP", "ILE");
    modifications.put("OLE", "XAA");
    modifications.put("OLT", "THR");
    modifications.put("OLZ", "SER");
    modifications.put("OMC", "CYS");
    modifications.put("OMG", "GLY");
    modifications.put("OMT", "MET");
    modifications.put("OMU", "UR3");
    modifications.put("ONE", "UR3");
    modifications.put("ONH", "ALA");
    modifications.put("ONL", "XAA");
    modifications.put("OPR", "ARG");
    modifications.put("ORN", "ALA");
    modifications.put("ORQ", "ARG");
    modifications.put("OSE", "SER");
    modifications.put("OTB", "XAA");
    modifications.put("OTH", "THR");
    modifications.put("OTY", "TYR");
    modifications.put("OXX", "ASP");
    modifications.put("P  ", "GLY");
    modifications.put("P1L", "CYS");
    modifications.put("P1P", "ASN");
    modifications.put("P2T", "THR");
    modifications.put("P2U", "UR3");
    modifications.put("P2Y", "PRO");
    modifications.put("P5P", "ALA");
    modifications.put("PAQ", "TYR");
    modifications.put("PAS", "ASP");
    modifications.put("PAT", "TRP");
    modifications.put("PAU", "ALA");
    modifications.put("PBB", "CYS");
    modifications.put("PBF", "PHE");
    modifications.put("PBT", "ASN");
    modifications.put("PCA", "GLU");
    modifications.put("PCC", "PRO");
    modifications.put("PCE", "XAA");
    modifications.put("PCS", "PHE");
    modifications.put("PDL", "XAA");
    modifications.put("PDU", "UR3");
    modifications.put("PEC", "CYS");
    modifications.put("PF5", "PHE");
    modifications.put("PFF", "PHE");
    modifications.put("PFX", "XAA");
    modifications.put("PG1", "SER");
    modifications.put("PG7", "GLY");
    modifications.put("PG9", "GLY");
    modifications.put("PGL", "XAA");
    modifications.put("PGN", "GLY");
    modifications.put("PGP", "GLY");
    modifications.put("PGY", "GLY");
    modifications.put("PHA", "PHE");
    modifications.put("PHD", "ASP");
    modifications.put("PHE", "PHE");
    modifications.put("PHI", "PHE");
    modifications.put("PHL", "PHE");
    modifications.put("PHM", "PHE");
    modifications.put("PIA", "AYG");
    modifications.put("PIV", "XAA");
    modifications.put("PLE", "LEU");
    modifications.put("PM3", "PHE");
    modifications.put("PMT", "CYS");
    modifications.put("POM", "PRO");
    modifications.put("PPN", "PHE");
    modifications.put("PPU", "ALA");
    modifications.put("PPW", "GLY");
    modifications.put("PQ1", "ASN");
    modifications.put("PR3", "CYS");
    modifications.put("PR5", "ALA");
    modifications.put("PR9", "PRO");
    modifications.put("PRN", "ALA");
    modifications.put("PRO", "PRO");
    modifications.put("PRS", "PRO");
    modifications.put("PSA", "PHE");
    modifications.put("PSH", "HIS");
    modifications.put("PST", "THR");
    modifications.put("PSU", "UR3");
    modifications.put("PSW", "CYS");
    modifications.put("PTA", "XAA");
    modifications.put("PTH", "TYR");
    modifications.put("PTM", "TYR");
    modifications.put("PTR", "TYR");
    modifications.put("PU ", "ALA");
    modifications.put("PUY", "ASN");
    modifications.put("PVH", "HIS");
    modifications.put("PVL", "XAA");
    modifications.put("PYA", "ALA");
    modifications.put("PYO", "UR3");
    modifications.put("PYX", "CYS");
    modifications.put("PYY", "ASN");
    modifications.put("QLG", "QLG");
    modifications.put("QMM", "GLN");
    modifications.put("QPA", "CYS");
    modifications.put("QPH", "PHE");
    modifications.put("QUO", "GLY");
    modifications.put("R  ", "ALA");
    modifications.put("R1A", "CYS");
    modifications.put("R4K", "TRP");
    modifications.put("RC7", "HYG");
    modifications.put("RE0", "TRP");
    modifications.put("RE3", "TRP");
    modifications.put("RIA", "ALA");
    modifications.put("RMP", "ALA");
    modifications.put("RON", "XAA");
    modifications.put("RT ", "THR");
    modifications.put("RTP", "ASN");
    modifications.put("S1H", "SER");
    modifications.put("S2C", "CYS");
    modifications.put("S2D", "ALA");
    modifications.put("S2M", "THR");
    modifications.put("S2P", "ALA");
    modifications.put("S4A", "ALA");
    modifications.put("S4C", "CYS");
    modifications.put("S4G", "GLY");
    modifications.put("S4U", "UR3");
    modifications.put("S6G", "GLY");
    modifications.put("SAC", "SER");
    modifications.put("SAH", "CYS");
    modifications.put("SAR", "GLY");
    modifications.put("SBL", "SER");
    modifications.put("SC ", "CYS");
    modifications.put("SCH", "CYS");
    modifications.put("SCS", "CYS");
    modifications.put("SCY", "CYS");
    modifications.put("SD2", "XAA");
    modifications.put("SDG", "GLY");
    modifications.put("SDP", "SER");
    modifications.put("SEB", "SER");
    modifications.put("SEC", "ALA");
    modifications.put("SEG", "ALA");
    modifications.put("SEL", "SER");
    modifications.put("SEM", "SER");
    modifications.put("SEN", "SER");
    modifications.put("SEP", "SER");
    modifications.put("SER", "SER");
    modifications.put("SET", "SER");
    modifications.put("SGB", "SER");
    modifications.put("SHC", "CYS");
    modifications.put("SHP", "GLY");
    modifications.put("SHR", "LYS");
    modifications.put("SIB", "CYS");
    modifications.put("SIC", "DC"); // check
    modifications.put("SLA", "PRO");
    modifications.put("SLR", "PRO");
    modifications.put("SLZ", "LYS");
    modifications.put("SMC", "CYS");
    modifications.put("SME", "MET");
    modifications.put("SMF", "PHE");
    modifications.put("SMP", "ALA");
    modifications.put("SMT", "THR");
    modifications.put("SNC", "CYS");
    modifications.put("SNN", "ASN");
    modifications.put("SOC", "CYS");
    modifications.put("SOS", "ASN");
    modifications.put("SOY", "SER");
    modifications.put("SPT", "THR");
    modifications.put("SRA", "ALA");
    modifications.put("SSU", "UR3");
    modifications.put("STY", "TYR");
    modifications.put("SUB", "XAA");
    modifications.put("SUI", "DG");
    modifications.put("SUN", "SER");
    modifications.put("SUR", "UR3");
    modifications.put("SVA", "SER");
    modifications.put("SVV", "SER");
    modifications.put("SVW", "SER");
    modifications.put("SVX", "SER");
    modifications.put("SVY", "SER");
    modifications.put("SVZ", "XAA");
    modifications.put("SWG", "SWG");
    modifications.put("SYS", "CYS");
    modifications.put("T  ", "THR");
    modifications.put("T11", "PHE");
    modifications.put("T23", "THR");
    modifications.put("T2S", "THR");
    modifications.put("T2T", "ASN");
    modifications.put("T31", "UR3");
    modifications.put("T32", "THR");
    modifications.put("T36", "THR");
    modifications.put("T37", "THR");
    modifications.put("T38", "THR");
    modifications.put("T39", "THR");
    modifications.put("T3P", "THR");
    modifications.put("T41", "THR");
    modifications.put("T48", "THR");
    modifications.put("T49", "THR");
    modifications.put("T4S", "THR");
    modifications.put("T5O", "UR3");
    modifications.put("T5S", "THR");
    modifications.put("T66", "XAA");
    modifications.put("T6A", "ALA");
    modifications.put("TA3", "THR");
    modifications.put("TA4", "XAA");
    modifications.put("TAF", "THR");
    modifications.put("TAL", "ASN");
    modifications.put("TAV", "ASP");
    modifications.put("TBG", "VAL");
    modifications.put("TBM", "THR");
    modifications.put("TC1", "CYS");
    modifications.put("TCP", "THR");
    modifications.put("TCQ", "TYR");
    modifications.put("TCR", "TRP");
    modifications.put("TCY", "ALA");
    modifications.put("TDD", "LEU");
    modifications.put("TDY", "THR");
    modifications.put("TFE", "THR");
    modifications.put("TFO", "ALA");
    modifications.put("TFQ", "PHE");
    modifications.put("TFT", "THR");
    modifications.put("TGP", "GLY");
    modifications.put("TH6", "THR");
    modifications.put("THC", "THR");
    modifications.put("THO", "XAA");
    modifications.put("THR", "THR");
    modifications.put("THX", "ASN");
    modifications.put("THZ", "ARG");
    modifications.put("TIH", "ALA");
    modifications.put("TLB", "ASN");
    modifications.put("TLC", "THR");
    modifications.put("TLN", "UR3");
    modifications.put("TMB", "THR");
    modifications.put("TMD", "THR");
    modifications.put("TNB", "CYS");
    modifications.put("TNR", "SER");
    modifications.put("TOX", "TRP");
    modifications.put("TP1", "THR");
    modifications.put("TPC", "CYS");
    modifications.put("TPG", "GLY");
    modifications.put("TPH", "XAA");
    modifications.put("TPL", "TRP");
    modifications.put("TPO", "THR");
    modifications.put("TPQ", "TYR");
    modifications.put("TQI", "TRP");
    modifications.put("TQQ", "TRP");
    modifications.put("TRF", "TRP");
    modifications.put("TRG", "LYS");
    modifications.put("TRN", "TRP");
    modifications.put("TRO", "TRP");
    modifications.put("TRP", "TRP");
    modifications.put("TRQ", "TRP");
    modifications.put("TRW", "TRP");
    modifications.put("TRX", "TRP");
    modifications.put("TS ", "ASN");
    modifications.put("TST", "XAA");
    modifications.put("TT ", "ASN");
    modifications.put("TTD", "THR");
    modifications.put("TTI", "UR3");
    modifications.put("TTM", "THR");
    modifications.put("TTQ", "TRP");
    modifications.put("TTS", "TYR");
    modifications.put("TY1", "TYR");
    modifications.put("TY2", "TYR");
    modifications.put("TY3", "TYR");
    modifications.put("TY5", "TYR");
    modifications.put("TYB", "TYR");
    modifications.put("TYI", "TYR");
    modifications.put("TYJ", "TYR");
    modifications.put("TYN", "TYR");
    modifications.put("TYO", "TYR");
    modifications.put("TYQ", "TYR");
    modifications.put("TYR", "TYR");
    modifications.put("TYS", "TYR");
    modifications.put("TYT", "TYR");
    modifications.put("TYU", "ASN");
    modifications.put("TYW", "TYR");
    modifications.put("TYX", "XAA");
    modifications.put("TYY", "TYR");
    modifications.put("TZB", "XAA");
    modifications.put("TZO", "XAA");
    modifications.put("U  ", "UR3");
    modifications.put("U25", "UR3");
    modifications.put("U2L", "UR3");
    modifications.put("U2N", "UR3");
    modifications.put("U2P", "UR3");
    modifications.put("U31", "UR3");
    modifications.put("U33", "UR3");
    modifications.put("U34", "UR3");
    modifications.put("U36", "UR3");
    modifications.put("U37", "UR3");
    modifications.put("U8U", "UR3");
    modifications.put("UAR", "UR3");
    modifications.put("UCL", "UR3");
    modifications.put("UD5", "UR3");
    modifications.put("UDP", "ASN");
    modifications.put("UFP", "ASN");
    modifications.put("UFR", "UR3");
    modifications.put("UFT", "UR3");
    modifications.put("UMA", "ALA");
    modifications.put("UMP", "UR3");
    modifications.put("UMS", "UR3");
    modifications.put("UN1", "XAA");
    modifications.put("UN2", "XAA");
    modifications.put("UNK", "XAA");
    modifications.put("UR3", "UR3");
    modifications.put("URD", "UR3");
    modifications.put("US1", "UR3");
    modifications.put("US2", "UR3");
    modifications.put("US3", "THR");
    modifications.put("US5", "UR3");
    modifications.put("USM", "UR3");
    modifications.put("VAD", "VAL");
    modifications.put("VAF", "VAL");
    modifications.put("VAL", "VAL");
    modifications.put("VB1", "LYS");
    modifications.put("VDL", "XAA");
    modifications.put("VLL", "XAA");
    modifications.put("VLM", "XAA");
    modifications.put("VMS", "XAA");
    modifications.put("VOL", "XAA");
    modifications.put("WCR", "GYG");
    modifications.put("X  ", "GLY");
    modifications.put("X2W", "GLU");
    modifications.put("X4A", "ASN");
    modifications.put("X9Q", "AFG");
    modifications.put("XAD", "ALA");
    modifications.put("XAE", "ASN");
    modifications.put("XAL", "ALA");
    modifications.put("XAR", "ASN");
    modifications.put("XCL", "CYS");
    modifications.put("XCN", "CYS");
    modifications.put("XCP", "XAA");
    modifications.put("XCR", "CYS");
    modifications.put("XCS", "ASN");
    modifications.put("XCT", "CYS");
    modifications.put("XCY", "CYS");
    modifications.put("XGA", "ASN");
    modifications.put("XGL", "GLY");
    modifications.put("XGR", "GLY");
    modifications.put("XGU", "GLY");
    modifications.put("XPR", "PRO");
    modifications.put("XSN", "ASN");
    modifications.put("XTH", "THR");
    modifications.put("XTL", "THR");
    modifications.put("XTR", "THR");
    modifications.put("XTS", "GLY");
    modifications.put("XTY", "ASN");
    modifications.put("XUA", "ALA");
    modifications.put("XUG", "GLY");
    modifications.put("XX1", "LYS");
    modifications.put("XXY", "THG");
    modifications.put("XYG", "DYG");
    modifications.put("Y  ", "ALA");
    modifications.put("YCM", "CYS");
    modifications.put("YG ", "GLY");
    modifications.put("YOF", "TYR");
    modifications.put("YRR", "ASN");
    modifications.put("YYG", "GLY");
    modifications.put("Z  ", "CYS");
    modifications.put("Z01", "ALA");
    modifications.put("ZAD", "ALA");
    modifications.put("ZAL", "ALA");
    modifications.put("ZBC", "CYS");
    modifications.put("ZBU", "UR3");
    modifications.put("ZCL", "PHE");
    modifications.put("ZCY", "CYS");
    modifications.put("ZDU", "UR3");
    modifications.put("ZFB", "XAA");
    modifications.put("ZGU", "GLY");
    modifications.put("ZHP", "ASN");
    modifications.put("ZTH", "THR");
    modifications.put("ZU0", "THR");
    modifications.put("ZZJ", "ALA");

  }

  public static String getCanonicalAminoAcid(String aA)
  {
    String canonical = modifications.get(aA);
    return canonical == null ? aA : canonical;
  }

  // main method generates perl representation of residue property hash
  // / cut here
  /**
   * @j2sIgnore
   * @param args
   */
  public static void main(String[] args)
  {
    Hashtable<String, Vector<String>> aaProps = new Hashtable<>();
    System.out.println("my %aa = {");
    // invert property hashes
    for (String pname : propHash.keySet())
    {
      Map<String, Integer> phash = propHash.get(pname);
      for (String rname : phash.keySet())
      {
        Vector<String> aprops = aaProps.get(rname);
        if (aprops == null)
        {
          aprops = new Vector<>();
          aaProps.put(rname, aprops);
        }
        Integer hasprop = phash.get(rname);
        if (hasprop.intValue() == 1)
        {
          aprops.addElement(pname);
        }
      }
    }
    Enumeration<String> res = aaProps.keys();
    while (res.hasMoreElements())
    {
      String rname = res.nextElement();

      System.out.print("'" + rname + "' => [");
      Enumeration<String> props = aaProps.get(rname).elements();
      while (props.hasMoreElements())
      {
        System.out.print("'" + props.nextElement() + "'");
        if (props.hasMoreElements())
        {
          System.out.println(", ");
        }
      }
      System.out.println("]" + (res.hasMoreElements() ? "," : ""));
    }
    System.out.println("};");
  }

  // to here

  /**
   * Returns a list of residue characters for the specified inputs
   * 
   * @param forNucleotide
   * @param includeAmbiguous
   * @return
   */
  public static List<String> getResidues(boolean forNucleotide,
          boolean includeAmbiguous)
  {
    List<String> result = new ArrayList<>();
    if (forNucleotide)
    {
      for (String nuc : nucleotideName.keySet())
      {
        int val = nucleotideIndex[nuc.charAt(0)];
        if ((!includeAmbiguous && val > 4) || (val >= maxNucleotideIndex))
        {
          continue;
        }
        nuc = nuc.toUpperCase(Locale.ROOT);
        if (!result.contains(nuc))
        {
          result.add(nuc);
        }
      }
    }
    else
    {
      /*
       * Peptide
       */
      for (String res : aa3Hash.keySet())
      {
        int index = aa3Hash.get(res).intValue();
        if ((!includeAmbiguous && index >= 20) || index >= maxProteinIndex)
        {
          continue;
        }
        res = res.toUpperCase(Locale.ROOT);
        if (!result.contains(res))
        {
          result.add(res);
        }
      }
    }

    return result;
  }

  /**
   * Returns the single letter code for a three letter code, or '0' if not known
   * 
   * @param threeLetterCode
   *          not case sensitive
   * @return
   */
  public static char getSingleCharacterCode(String threeLetterCode)
  {
    if (threeLetterCode == null)
    {
      return '0';
    }
    Integer index = ResidueProperties.aa3Hash
            .get(threeLetterCode.toUpperCase(Locale.ROOT));
    return index == null ? '0' : aa[index].charAt(0);
  }
}
