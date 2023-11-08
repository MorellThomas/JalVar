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
import jalview.bin.Jalview
import jalview.workers.FeatureSetCounterI
import jalview.workers.AlignmentAnnotationFactory

/*
 * Demonstration of FeatureSetCounterI
 * compute annotation tracks counting number of displayed 
 * features of each type in each column
 */

/*
 * discover features on the current view
 */
 
def featuresDisp=Jalview.currentAlignFrame.currentView.featuresDisplayed
if (featuresDisp == null) {
    print 'Need at least one feature visible on alignment'
}
def visibleFeatures=featuresDisp.visibleFeatures.toList()
assert 'java.util.ArrayList' == visibleFeatures.class.name

/*
 * A closure that returns an array of features present 
 * for each feature type in visibleFeatures
 * Argument 'features' will be a list of SequenceFeature 
 */
def getCounts = 
    { features -> 
        int[] obs = new int[visibleFeatures.size]
        for (sf in features)
        {
            /*
             * Here we inspect the type of the sequence feature.
             * You can also test sf.description, sf.score, sf.featureGroup,
             * sf.strand, sf.phase, sf.begin, sf.end
             * or sf.getValue(attributeName) for GFF 'column 9' properties
             */
            int pos = 0
            for (type in visibleFeatures) 
            {
              if (type.equals(sf.type)) 
              {
                  obs[pos]++
              }
              pos++
            }
        }
        obs
}
  
/*
 * Define something that counts each visible feature type
 */
def columnSetCounter =
    [
     getNames: { visibleFeatures as String[] }, 
     getDescriptions:  { visibleFeatures as String[] },
     getMinColour: { [0, 255, 255] as int[] }, // cyan
     getMaxColour: { [0, 0, 255] as int[] }, // blue
     count: 
         { res, feats -> 
             getCounts.call(feats) 
         }
     ] as FeatureSetCounterI

/*
 * and register the counter
 */
AlignmentAnnotationFactory.newCalculator(columnSetCounter)
