import java.awt.Color;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ColourSchemes;
import jalview.datamodel.AnnotatedCollectionI;
import jalview.datamodel.SequenceI;
import jalview.datamodel.SequenceCollectionI;
import jalview.api.AlignViewportI

/*
 * Example script that registers two new alignment colour schemes
 */

/*
 * Closure that defines a colour scheme where consensus residues are pink,
 * other residues are red in odd columns and blue in even columns, and
 * gaps are yellow  
 */
def candy
candy = { ->
  [
    /*
     * name shown in the colour menu
     */
    getSchemeName: { -> 'candy' },
    
    /*
     * to make a new instance for each alignment view
     */
    getInstance: { view, coll -> candy() },
    
    /*
     * method only needed if colour scheme has to recalculate
     * values when an alignment is modified
     */
    alignmentChanged: { AnnotatedCollectionI coll, Map<SequenceI, SequenceCollectionI> map -> },
    
    /*
     * determine colour for a residue at an aligned position of a
     * sequence, given consensus residue(s) for the column and the
     * consensus percentage identity score for the column
     */
    findColour: { char res, int col, SequenceI seq, String consensus, float pid -> 
        if (res == ' ' || res == '-' || res == '.') 
        {
            Color.yellow
        } else if (consensus.contains(String.valueOf(res)))
        {
            Color.pink
        } else if (col % 2 == 0) 
        {
            Color.blue
        } else 
        {
            Color.red
        }
    },
    
    /*
     * true means applicable to nucleotide or peptide data
     */
    isApplicableTo: {AnnotatedCollectionI coll -> true},
    
    /*
     * simple colour schemes are those that depend on the residue
     * only (these are also available to colour structure viewers)
     */
    isSimple: { false }
 ] as ColourSchemeI
}

/*
 * A closure that defines a colour scheme graduated 
 * (approximately) by amino acid weight
 * here from lightest (G) Blue, to heaviest (W) Red
 */
def makeColour = { weight -> 
    minWeight = 75 // Glycine
    maxWeight = 204 // Tryptophan
    int i = 255 * (weight - minWeight) / (maxWeight - minWeight);
    new Color(i, 0, 255-i);
}
def byWeight
byWeight = { ->
  [
    getSchemeName: { 'By Weight' },
    // this colour scheme is peptide-specific:
    isApplicableTo: { coll -> !coll.isNucleotide() },
    alignmentChanged: { coll, map -> },
    getInstance: { view, coll -> byWeight() },
    isSimple: { true },
    findColour: {res, col, seq, consensus, pid -> 
        switch (res) {
          case ' ':
          case '-':
          case '.':
            Color.white
             break
          case 'A':
            makeColour(89)
            break
          case 'R':
            makeColour(174)
            break
          case 'N':
          case 'D':
          case 'B':
          case 'I':
          case 'L':
            makeColour(132)
            break
          case 'C':
            makeColour(121)
            break
          case 'Q':
          case 'E':
          case 'Z':
          case 'K':
          case 'M':
            makeColour(146)
            break
          case 'G':
            makeColour(75)
            break
          case 'H':
            makeColour(155)
            break
          case 'F':
            makeColour(165)
            break
          case 'P':
            makeColour(115)
            break
          case 'S':
            makeColour(105)
            break
          case 'T':
            makeColour(119)
            break
          case 'W':
            makeColour(204)
            break
          case 'Y':
            makeColour(181)
            break
          case 'V':
            makeColour(117)
            break
          default:
            makeColour(150)
        }
      }
  ] as ColourSchemeI
}

ColourSchemes.instance.registerColourScheme(candy())
ColourSchemes.instance.registerColourScheme(byWeight())
