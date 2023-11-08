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

import jalview.workers.AlignmentAnnotationFactory;
import jalview.workers.FeatureSetCounterI;

/*
 * Example script to compute two alignment annotations
 * - count of Phosphorylation features
 * - count of Turn features
 * To try this, first load example file uniref50.fa and load on features file
 * exampleFeatures.txt, before running this script
 *
 * The script only needs to be run once - it will be registered by Jalview
 * and recalculated automatically when the alignment changes.
 * 
 * Note: The feature api provided by 2.10.2 is not compatible with scripts
 * that worked with earlier Jalview versions. Apologies for the inconvenience.
 */
 
def annotator = 
    [
     getNames: { ['Phosphorylation', 'Turn'] as String[] }, 
     getDescriptions:  { ['Count of Phosphorylation features', 'Count of Turn features'] as String[] },
     getMinColour: { [0, 255, 255] as int[] }, // cyan
     getMaxColour: { [0, 0, 255] as int[] }, // blue
     count: 
         { res, feats -> 
                int phos
                int turn
                for (sf in feats)
                {
 		          /*
		           * Here we inspect the type of the sequence feature.
		           * You can also test sf.description, sf.score, sf.featureGroup,
		           * sf.strand, sf.phase, sf.begin, sf.end
		           * or sf.getValue(attributeName) for GFF 'column 9' properties
		           */
		           if (sf.type.contains('TURN'))
                   {
                      turn++
                   }
                   if (sf.type.contains('PHOSPHORYLATION'))
                   {
                      phos++
                   }
                }
                [phos, turn] as int[]
         }
     ] as FeatureSetCounterI
    
/*
 * Register the annotation calculator with Jalview
 */
AlignmentAnnotationFactory.newCalculator(annotator) 
