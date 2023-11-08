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

import jalview.analysis.scoremodels.ScoreModels
import jalview.analysis.scoremodels.SimilarityParams

// generate matrix for current selection using standard Jalview PID

printSimilarityMatrix(true,true,SimilarityParams.Jalview)

/** 
 * this function prints a sequence similarity matrix in PHYLIP format. 
 * printSimilarityMatrix(selected-only, include-ids, pidMethod)
 * 
 * Allowed values for pidMethod:
 * 
 * Jalview's Comparison.PID method includes matching gaps 
 * and counts over the length of the shorter gapped sequence
 * SimilarityParams.Jalview;
 *
 * 'SeqSpace' mode PCA calculation does not count matching 
 * gaps but uses longest gapped sequence length
 *  SimilarityParams.SeqSpace;
 *
 * PID calcs from the Raghava-Barton paper
 * SimilarityParams.PID1: ignores gap-gap, does not score gap-residue,
 * includes gap-residue in lengths, matches on longer of two sequences.
 * 
 * SimilarityParams.PID2: ignores gap-gap,ignores gap-residue, 
 * matches on longer of two sequences
 * 
 * SimilarityParams.PID3: ignores gap-gap,ignores gap-residue, 
 * matches on shorter of sequences only
 * 
 * SimilarityParams.PID4: ignores gap-gap,does not score gap-residue,
 * includes gap-residue in lengths,matches on shorter of sequences only.
 */

void printSimilarityMatrix(boolean selview=false, boolean includeids=true, SimilarityParams pidMethod) {

  def currentAlignFrame = jalview.bin.Jalview.getCurrentAlignFrame()

  jalview.gui.AlignViewport av = currentAlignFrame.getCurrentView()

  jalview.datamodel.AlignmentView seqStrings = av.getAlignmentView(selview)

  if (!selview || av.getSelectionGroup()==null) {
    start = 0
    end = av.getAlignment().getWidth()
    seqs = av.getAlignment().getSequencesArray()
  } else {
    start = av.getSelectionGroup().getStartRes()
    end = av.getSelectionGroup().getEndRes() + 1
    seqs = av.getSelectionGroup().getSequencesInOrder(av.getAlignment())
  }

  distanceCalc = ScoreModels.getInstance().getScoreModel("PID",
      (jalview.api.AlignmentViewPanel) currentAlignFrame.alignPanel)

  def distance=distanceCalc.findSimilarities(
      seqStrings.getSequenceStrings(jalview.util.Comparison.GAP_DASH),pidMethod)

  // output the PHYLIP Matrix

  print distance.width()+" "+distance.height()+"\n"

  p = 0

  for (v in 1..distance.height()) {

    if (includeids) {
      print seqs[p++].getDisplayId(false)+" "
    }

    for (r in 1..distance.width()) {
      print distance.getValue(v-1,r-1)+" "
    }

    print "\n"
  }
}