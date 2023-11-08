package jalview.schemes;

import jalview.api.AlignViewportI;
import jalview.datamodel.AnnotatedCollectionI;

public class NucleotideAmbiguityColourScheme extends ResidueColourScheme
{
  /**
   * Creates a new NucleotideColourScheme object.
   */
  public NucleotideAmbiguityColourScheme()
  {
    super(ResidueProperties.nucleotideIndex,
            ResidueProperties.nucleotideAmbiguity);
  }

  @Override
  public boolean isNucleotideSpecific()
  {
    return true;
  }

  @Override
  public String getSchemeName()
  {
    return JalviewColourScheme.NucleotideAmbiguity.toString();
  }

  /**
   * Returns a new instance of this colour scheme with which the given data may
   * be coloured
   */
  @Override
  public ColourSchemeI getInstance(AlignViewportI view,
          AnnotatedCollectionI coll)
  {
    return new NucleotideAmbiguityColourScheme();
  }

}
