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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AnnotatedCollectionI;
import jalview.datamodel.Annotation;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.JvOptionPane;
import jalview.io.TCoffeeScoreFile;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ResidueColourSchemeTest
{
  @BeforeClass(alwaysRun = true)
  public void setUp()
  {

  }

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @Test(groups = "Functional")
  public void testIsApplicableTo()
  {
    SequenceI pep1 = new Sequence("pep1", "APQTWLS");
    SequenceI pep2 = new Sequence("pep2", "AILFQYG");
    SequenceI dna1 = new Sequence("dna1", "ACTGAC");
    SequenceI dna2 = new Sequence("dna2", "TCCAAG");
    AlignmentI peptide = new Alignment(new SequenceI[] { pep1, pep2 });
    AlignmentI nucleotide = new Alignment(new SequenceI[] { dna1, dna2 });

    /*
     * peptide-specific colour schemes
     */
    assertTrue(new ClustalxColourScheme(peptide, null)
            .isApplicableTo(peptide));
    assertFalse(new ClustalxColourScheme(nucleotide, null)
            .isApplicableTo(nucleotide));
    assertTrue(new Blosum62ColourScheme().isApplicableTo(peptide));
    assertFalse(new Blosum62ColourScheme().isApplicableTo(nucleotide));
    assertTrue(new BuriedColourScheme().isApplicableTo(peptide));
    assertFalse(new BuriedColourScheme().isApplicableTo(nucleotide));
    assertTrue(new HelixColourScheme().isApplicableTo(peptide));
    assertFalse(new HelixColourScheme().isApplicableTo(nucleotide));
    assertTrue(new HydrophobicColourScheme().isApplicableTo(peptide));
    assertFalse(new HydrophobicColourScheme().isApplicableTo(nucleotide));
    assertTrue(new StrandColourScheme().isApplicableTo(peptide));
    assertFalse(new StrandColourScheme().isApplicableTo(nucleotide));
    assertTrue(new TaylorColourScheme().isApplicableTo(peptide));
    assertFalse(new TaylorColourScheme().isApplicableTo(nucleotide));
    assertTrue(new TurnColourScheme().isApplicableTo(peptide));
    assertFalse(new TurnColourScheme().isApplicableTo(nucleotide));
    assertTrue(new ZappoColourScheme().isApplicableTo(peptide));
    assertFalse(new ZappoColourScheme().isApplicableTo(nucleotide));

    /*
     * nucleotide-specific colour schemes
     */
    assertFalse(new NucleotideColourScheme().isApplicableTo(peptide));
    assertTrue(new NucleotideColourScheme().isApplicableTo(nucleotide));
    assertFalse(new PurinePyrimidineColourScheme().isApplicableTo(peptide));
    assertTrue(
            new PurinePyrimidineColourScheme().isApplicableTo(nucleotide));
    assertFalse(new RNAInteractionColourScheme().isApplicableTo(peptide));
    assertTrue(new RNAInteractionColourScheme().isApplicableTo(nucleotide));

    /*
     * indifferent
     */
    assertTrue(new UserColourScheme().isApplicableTo(peptide));
    assertTrue(new UserColourScheme().isApplicableTo(nucleotide));
    assertTrue(new ScoreColourScheme(new int[] {}, new double[] {}, 0, 0d)
            .isApplicableTo(peptide));
    assertTrue(new ScoreColourScheme(new int[] {}, new double[] {}, 0, 0d)
            .isApplicableTo(nucleotide));
    ResidueColourScheme rcs = new PIDColourScheme();
    assertTrue(rcs.isApplicableTo(peptide));
    assertTrue(rcs.isApplicableTo(nucleotide));
    assertTrue(new PIDColourScheme().isApplicableTo(peptide));
    assertTrue(new PIDColourScheme().isApplicableTo(nucleotide));
    assertTrue(new FollowerColourScheme().isApplicableTo(peptide));
    assertTrue(new FollowerColourScheme().isApplicableTo(nucleotide));

    /*
     * TCoffee colour requires the presence of TCoffee score annotation
     */
    assertFalse(new TCoffeeColourScheme(peptide).isApplicableTo(peptide));
    assertFalse(
            new TCoffeeColourScheme(nucleotide).isApplicableTo(nucleotide));
    AlignmentAnnotation aa = new AlignmentAnnotation("T-COFFEE", "", null);
    aa.setCalcId(TCoffeeScoreFile.TCOFFEE_SCORE);
    peptide.addAnnotation(aa);
    aa = new AlignmentAnnotation("T-COFFEE", "", null);
    aa.setCalcId(TCoffeeScoreFile.TCOFFEE_SCORE);
    nucleotide.addAnnotation(aa);
    assertTrue(new TCoffeeColourScheme(peptide).isApplicableTo(peptide));
    assertTrue(
            new TCoffeeColourScheme(nucleotide).isApplicableTo(nucleotide));

    /*
     * RNAHelices requires the presence of rna secondary structure
     */
    assertFalse(new RNAHelicesColour(peptide).isApplicableTo(peptide));
    assertFalse(
            new RNAHelicesColour(nucleotide).isApplicableTo(nucleotide));
    // add secondary structure (small but perfectly formed)
    Annotation[] ss = new Annotation[2];
    ss[0] = new Annotation("", "", '{', 0f);
    ss[1] = new Annotation("", "", '}', 0f);
    nucleotide.addAnnotation(new AlignmentAnnotation("SS", "", ss));
    assertTrue(new RNAHelicesColour(nucleotide).isApplicableTo(nucleotide));
  }

  @Test(groups = "Functional")
  public void testIsApplicableTo_dynamicColourScheme()
  {
    SequenceI pep1 = new Sequence("pep1", "APQTWLS");
    SequenceI pep2 = new Sequence("pep2", "AILFQYG");
    AlignmentI peptide = new Alignment(new SequenceI[] { pep1, pep2 });

    /*
     * demonstrate that we can 'plug in' a colour scheme with specified
     * criteria for applicability; here, that there are more than 2 sequences
     */
    ColourSchemeI cs = new UserColourScheme()
    {
      @Override
      public boolean isApplicableTo(AnnotatedCollectionI ac)
      {
        AlignmentI al = ac.getContext() == null ? (AlignmentI) ac
                : (AlignmentI) ac.getContext();
        return al.getSequences().size() > 2;
      }
    };
    assertFalse(cs.isApplicableTo(peptide));
    peptide.addSequence(pep1);
    assertTrue(cs.isApplicableTo(peptide));
  }

  @Test(groups = "Functional")
  public void testGetName()
  {
    SequenceI pep1 = new Sequence("pep1", "APQTWLS");
    AlignmentI peptide = new Alignment(new SequenceI[] { pep1 });

    assertEquals("Blosum62", new Blosum62ColourScheme().getSchemeName());
    assertEquals("Buried Index", new BuriedColourScheme().getSchemeName());
    assertEquals("Helix Propensity",
            new HelixColourScheme().getSchemeName());
    assertEquals("Hydrophobic",
            new HydrophobicColourScheme().getSchemeName());
    assertEquals("Strand Propensity",
            new StrandColourScheme().getSchemeName());
    assertEquals("Taylor", new TaylorColourScheme().getSchemeName());
    assertEquals("Turn Propensity", new TurnColourScheme().getSchemeName());
    assertEquals("Zappo", new ZappoColourScheme().getSchemeName());
    assertEquals("Nucleotide",
            new NucleotideColourScheme().getSchemeName());
    assertEquals("Purine/Pyrimidine",
            new PurinePyrimidineColourScheme().getSchemeName());
    assertEquals("RNA Interaction type",
            new RNAInteractionColourScheme().getSchemeName());
    assertEquals("User Defined", new UserColourScheme().getSchemeName());
    assertEquals("Score",
            new ScoreColourScheme(new int[] {}, new double[] {}, 0, 0d)
                    .getSchemeName());
    assertEquals("% Identity", new PIDColourScheme().getSchemeName());
    assertEquals("Follower", new FollowerColourScheme().getSchemeName());
    assertEquals("T-Coffee Scores",
            new TCoffeeColourScheme(peptide).getSchemeName());
    assertEquals("RNA Helices",
            new RNAHelicesColour(peptide).getSchemeName());
  }
}
