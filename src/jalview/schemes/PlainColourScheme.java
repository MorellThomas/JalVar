package jalview.schemes;

import java.awt.Color;
import java.util.Arrays;

import jalview.api.AlignViewportI;
import jalview.datamodel.AnnotatedCollectionI;


public class PlainColourScheme extends ResidueColourScheme
{

  public PlainColourScheme(Color col)
  {
    super(ResidueProperties.aaIndex);
    Color[] justPlain = new Color[26];
    Arrays.fill(justPlain, col);
    setColours(justPlain);
  }
  public PlainColourScheme()
  {
    super(ResidueProperties.aaIndex);
    Color[] justPlain = new Color[26];
    Arrays.fill(justPlain, new Color(250, 133, 120));
    setColours(justPlain);
  }
  
  @Override
  public boolean isPeptideSpecific()
  {
    return true;
  }

  @Override
  public String getSchemeName()
  {
    return "Just Plain";
  }

  public ColourSchemeI getInstance(AlignViewportI view,
          AnnotatedCollectionI coll)
  {
    return new PlainColourScheme();
  }

}
