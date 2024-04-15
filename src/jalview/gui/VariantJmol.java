package jalview.gui;

import java.awt.Color;
import java.io.IOException;
import java.util.TreeMap;

import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.io.DataSourceType;
import jalview.io.EpReferenceFile;
import jalview.io.StructureFile;
import jalview.structure.StructureImportSettings.TFType;
import jalview.structure.StructureSelectionManager;
import jalview.structures.models.AAStructureBindingModel;

public class VariantJmol implements Runnable
{
  /*
   * inputs
   */
  private final SequenceI sequence;
  
  private int selectedRes;
  
  private final String path;
  
  private final String structureFile;
  
  private final String paeMatrixFile;
  
  private final PDBEntry pdb;
  
  private final AlignmentPanel ap;
  
  private final TreeMap<Integer, String[]> variantResidues;
  
  private AppJmol jmolViewer;
  
  /*
   * others
   */
  private final StructureSelectionManager ssm;
  
  private Color[] residueColours;
  
  // constructors
  public VariantJmol(SequenceI seq, String refFile, AlignmentPanel ap, TreeMap<Integer, String[]> variantResidues, int selRes)
  throws IOException, ClassNotFoundException
  {
    this(seq, refFile, null, ap, variantResidues, selRes);
  }
  public VariantJmol(SequenceI seq, String refFile, String paeMatrix, AlignmentPanel ap, TreeMap<Integer, String[]> varRes, int selRes)
  throws IOException, ClassNotFoundException
  {
    this.sequence = seq;
    this.selectedRes = selRes;
    this.path = EpReferenceFile.REFERENCE_PATH + "STRUCS";
    this.structureFile = path + "/" + seq.getName() + ".pdb";
    this.paeMatrixFile = paeMatrix == null ? null : path + paeMatrix;
    this.variantResidues = varRes;
    
    System.out.println(structureFile);
    
    this.pdb = new AssociatePdbFileWithSeq().associatePdbWithSeq(structureFile, DataSourceType.FILE, this.sequence, true, Desktop.instance, TFType.PLDDT, paeMatrixFile, true);
    
    this.ap = ap;
    this.ap.getAlignment().addSequence(this.sequence);

    this.ssm = ap.getStructureSelectionManager();
    this.ssm.computeMapping(true, new SequenceI[]{this.ap.getAlignment().getSequenceAt(2)}, new String[]{this.pdb.getChainCode()}, this.structureFile, DataSourceType.FILE, null, TFType.PLDDT, null, true);

  }
  
  public void run()
  {
    jmolViewer = new AppJmol(pdb, new SequenceI[]{ap.getAlignment().getSequenceAt(2)}, null, ap);
    // extends AAStructureBinding holding the colouring functions
    //AppJmolBinding jmolBinding = new AppJmolBinding(jmolViewer, ssm, new PDBEntry[]{pdb}, new SequenceI[][]{{ap.getAlignment().getSequenceAt(2)}}, DataSourceType.FILE);
        
    colourResidues();
  }
  
  /**
   * re-colour the residues
   * @param res selected
   */
  private void colourResidues()
  {
    residueColours = new Color[sequence.getLength()];
    Object[] keySet = variantResidues.keySet().toArray();
    for (int i = 0, j = 0; i < sequence.getLength(); i++) // i loop through residues, j loop through variantResidues
    {
      if ((j < keySet.length) && (i == (int) keySet[j]))
      {
        for (String var : variantResidues.get(i))
        {
          if ((i + 1) == selectedRes)
          {
            residueColours[i] = Color.red;   //selected res red
            continue;
          }
          
          if (sequence.getCharAt(i) == var.charAt(2))
            residueColours[i] = new Color(250, 133, 120);   // toRes pink
          else if (sequence.getCharAt(i) == var.charAt(0))
            residueColours[i] = new Color(98, 131, 222);  // fromRes blue
          else
            residueColours[i] = new Color(100, 100, 100); // notRes grey
        }
        j++;
      } else {
        residueColours[i] = new Color(200, 200, 200);   //other (nonvar) grey
      }
    }

    //jmolBinding.colourBySequence(ap, residueColours);
    jmolViewer.resetColourArray();
    jmolViewer.setColourArray(residueColours);
    jmolViewer.getBinding().colourBySequence(ap, residueColours);
  }
  
  /**
   * set the selected residue, and re-colour the model
   * @param res
   * @param reColour
   */
  public void setSelectedResidue(int res)
  {
    setSelectedResidue(res, false);
  }
  public void setSelectedResidue(int res, boolean reColour)
  {
    this.selectedRes = res;
    if (reColour)
      colourResidues();
  }

}
