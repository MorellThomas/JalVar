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

import jalview.analysis.scoremodels.PIDModel;
import jalview.analysis.scoremodels.SimilarityParams;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AlignmentOrder;
import jalview.datamodel.BinaryNode;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.datamodel.SequenceNode;
import jalview.util.QuickSort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Routines for manipulating the order of a multiple sequence alignment TODO:
 * this class retains some global states concerning sort-order which should be
 * made attributes for the caller's alignment visualization. TODO: refactor to
 * allow a subset of selected sequences to be sorted within the context of a
 * whole alignment. Sort method template is: SequenceI[] tobesorted, [ input
 * data mapping to each tobesorted element to use ], Alignment context of
 * tobesorted that are to be re-ordered, boolean sortinplace, [special data - ie
 * seuqence to be sorted w.r.t.]) sortinplace implies that the sorted vector
 * resulting from applying the operation to tobesorted should be mapped back to
 * the original positions in alignment. Otherwise, normal behaviour is to re
 * order alignment so that tobesorted is sorted and grouped together starting
 * from the first tobesorted position in the alignment. e.g. (a,tb2,b,tb1,c,tb3
 * becomes a,tb1,tb2,tb3,b,c)
 */
public class AlignmentSorter
{
  /*
   * todo: refactor searches to follow a basic pattern: (search property, last
   * search state, current sort direction)
   */
  static boolean sortIdAscending = true;

  static int lastGroupHash = 0;

  static boolean sortGroupAscending = true;

  static AlignmentOrder lastOrder = null;

  static boolean sortOrderAscending = true;

  static TreeModel lastTree = null;

  static boolean sortTreeAscending = true;

  /*
   * last Annotation Label used for sort by Annotation score
   */
  private static String lastSortByAnnotation;

  /*
   * string hash of last arguments to sortByFeature
   * (sort order toggles if this is unchanged between sorts)
   */
  private static String sortByFeatureCriteria;

  private static boolean sortByFeatureAscending = true;

  private static boolean sortLengthAscending;

  /**
   * Sorts sequences in the alignment by Percentage Identity with the given
   * reference sequence, sorting the highest identity to the top
   * 
   * @param align
   *          AlignmentI
   * @param s
   *          SequenceI
   * @param end
   */
  public static void sortByPID(AlignmentI align, SequenceI s)
  {
    int nSeq = align.getHeight();

    float[] scores = new float[nSeq];
    SequenceI[] seqs = new SequenceI[nSeq];
    String refSeq = s.getSequenceAsString();

    SimilarityParams pidParams = new SimilarityParams(true, true, true,
            true);
    for (int i = 0; i < nSeq; i++)
    {
      scores[i] = (float) PIDModel.computePID(
              align.getSequenceAt(i).getSequenceAsString(), refSeq,
              pidParams);
      seqs[i] = align.getSequenceAt(i);
    }

    QuickSort.sort(scores, seqs);

    setReverseOrder(align, seqs);
  }

  /**
   * Reverse the order of the sort
   * 
   * @param align
   *          DOCUMENT ME!
   * @param seqs
   *          DOCUMENT ME!
   */
  private static void setReverseOrder(AlignmentI align, SequenceI[] seqs)
  {
    int nSeq = seqs.length;

    int len = 0;

    if ((nSeq % 2) == 0)
    {
      len = nSeq / 2;
    }
    else
    {
      len = (nSeq + 1) / 2;
    }

    // NOTE: DO NOT USE align.setSequenceAt() here - it will NOT work
    List<SequenceI> asq = align.getSequences();
    synchronized (asq)
    {
      for (int i = 0; i < len; i++)
      {
        // SequenceI tmp = seqs[i];
        asq.set(i, seqs[nSeq - i - 1]);
        asq.set(nSeq - i - 1, seqs[i]);
      }
    }
  }

  /**
   * Sets the Alignment object with the given sequences
   * 
   * @param align
   *          Alignment object to be updated
   * @param tmp
   *          sequences as a vector
   */
  private static void setOrder(AlignmentI align, List<SequenceI> tmp)
  {
    setOrder(align, vectorSubsetToArray(tmp, align.getSequences()));
  }

  /**
   * Sets the Alignment object with the given sequences
   * 
   * @param align
   *          DOCUMENT ME!
   * @param seqs
   *          sequences as an array
   */
  public static void setOrder(AlignmentI align, SequenceI[] seqs)
  {
    // NOTE: DO NOT USE align.setSequenceAt() here - it will NOT work
    List<SequenceI> algn = align.getSequences();
    synchronized (algn)
    {
      List<SequenceI> tmp = new ArrayList<>();

      for (int i = 0; i < seqs.length; i++)
      {
        if (algn.contains(seqs[i]))
        {
          tmp.add(seqs[i]);
        }
      }

      algn.clear();
      // User may have hidden seqs, then clicked undo or redo
      for (int i = 0; i < tmp.size(); i++)
      {
        algn.add(tmp.get(i));
      }
    }
  }

  /**
   * Sorts by ID. Numbers are sorted before letters.
   * 
   * @param align
   *          The alignment object to sort
   */
  public static void sortByID(AlignmentI align)
  {
    int nSeq = align.getHeight();

    String[] ids = new String[nSeq];
    SequenceI[] seqs = new SequenceI[nSeq];

    for (int i = 0; i < nSeq; i++)
    {
      ids[i] = align.getSequenceAt(i).getName();
      seqs[i] = align.getSequenceAt(i);
    }

    QuickSort.sort(ids, seqs);

    if (sortIdAscending)
    {
      setReverseOrder(align, seqs);
    }
    else
    {
      setOrder(align, seqs);
    }

    sortIdAscending = !sortIdAscending;
  }

  /**
   * Sorts by sequence length
   * 
   * @param align
   *          The alignment object to sort
   */
  public static void sortByLength(AlignmentI align)
  {
    int nSeq = align.getHeight();

    float[] length = new float[nSeq];
    SequenceI[] seqs = new SequenceI[nSeq];

    for (int i = 0; i < nSeq; i++)
    {
      seqs[i] = align.getSequenceAt(i);
      length[i] = (seqs[i].getEnd() - seqs[i].getStart());
    }

    QuickSort.sort(length, seqs);

    if (sortLengthAscending)
    {
      setReverseOrder(align, seqs);
    }
    else
    {
      setOrder(align, seqs);
    }

    sortLengthAscending = !sortLengthAscending;
  }

  /**
   * Sorts the alignment by size of group. <br>
   * Maintains the order of sequences in each group by order in given alignment
   * object.
   * 
   * @param align
   *          sorts the given alignment object by group
   */
  public static void sortByGroup(AlignmentI align)
  {
    // MAINTAINS ORIGNAL SEQUENCE ORDER,
    // ORDERS BY GROUP SIZE
    List<SequenceGroup> groups = new ArrayList<>();

    if (groups.hashCode() != lastGroupHash)
    {
      sortGroupAscending = true;
      lastGroupHash = groups.hashCode();
    }
    else
    {
      sortGroupAscending = !sortGroupAscending;
    }

    // SORTS GROUPS BY SIZE
    // ////////////////////
    for (SequenceGroup sg : align.getGroups())
    {
      for (int j = 0; j < groups.size(); j++)
      {
        SequenceGroup sg2 = groups.get(j);

        if (sg.getSize() > sg2.getSize())
        {
          groups.add(j, sg);

          break;
        }
      }

      if (!groups.contains(sg))
      {
        groups.add(sg);
      }
    }

    // NOW ADD SEQUENCES MAINTAINING ALIGNMENT ORDER
    // /////////////////////////////////////////////
    List<SequenceI> seqs = new ArrayList<>();

    for (int i = 0; i < groups.size(); i++)
    {
      SequenceGroup sg = groups.get(i);
      SequenceI[] orderedseqs = sg.getSequencesInOrder(align);

      for (int j = 0; j < orderedseqs.length; j++)
      {
        seqs.add(orderedseqs[j]);
      }
    }

    if (sortGroupAscending)
    {
      setOrder(align, seqs);
    }
    else
    {
      setReverseOrder(align,
              vectorSubsetToArray(seqs, align.getSequences()));
    }
  }

  /**
   * Select sequences in order from tmp that is present in mask, and any
   * remaining sequences in mask not in tmp
   * 
   * @param tmp
   *          thread safe collection of sequences
   * @param mask
   *          thread safe collection of sequences
   * 
   * @return intersect(tmp,mask)+intersect(complement(tmp),mask)
   */
  private static SequenceI[] vectorSubsetToArray(List<SequenceI> tmp,
          List<SequenceI> mask)
  {
    // or?
    // tmp2 = tmp.retainAll(mask);
    // return tmp2.addAll(mask.removeAll(tmp2))

    ArrayList<SequenceI> seqs = new ArrayList<>();
    int i, idx;
    boolean[] tmask = new boolean[mask.size()];

    for (i = 0; i < mask.size(); i++)
    {
      tmask[i] = true;
    }

    for (i = 0; i < tmp.size(); i++)
    {
      SequenceI sq = tmp.get(i);
      idx = mask.indexOf(sq);
      if (idx > -1 && tmask[idx])
      {
        tmask[idx] = false;
        seqs.add(sq);
      }
    }

    for (i = 0; i < tmask.length; i++)
    {
      if (tmask[i])
      {
        seqs.add(mask.get(i));
      }
    }

    return seqs.toArray(new SequenceI[seqs.size()]);
  }

  /**
   * Sorts by a given AlignmentOrder object
   * 
   * @param align
   *          Alignment to order
   * @param order
   *          specified order for alignment
   */
  public static void sortBy(AlignmentI align, AlignmentOrder order)
  {
    // Get an ordered vector of sequences which may also be present in align
    List<SequenceI> tmp = order.getOrder();

    if (lastOrder == order)
    {
      sortOrderAscending = !sortOrderAscending;
    }
    else
    {
      sortOrderAscending = true;
    }

    if (sortOrderAscending)
    {
      setOrder(align, tmp);
    }
    else
    {
      setReverseOrder(align,
              vectorSubsetToArray(tmp, align.getSequences()));
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param align
   *          alignment to order
   * @param tree
   *          tree which has
   * 
   * @return DOCUMENT ME!
   */
  private static List<SequenceI> getOrderByTree(AlignmentI align,
          TreeModel tree)
  {
    int nSeq = align.getHeight();

    List<SequenceI> tmp = new ArrayList<>();

    tmp = _sortByTree(tree.getTopNode(), tmp, align.getSequences());

    if (tmp.size() != nSeq)
    {
      // TODO: JBPNote - decide if this is always an error
      // (eg. not when a tree is associated to another alignment which has more
      // sequences)
      if (tmp.size() != nSeq)
      {
        addStrays(align, tmp);
      }

      if (tmp.size() != nSeq)
      {
        System.err.println("WARNING: tmp.size()=" + tmp.size() + " != nseq="
                + nSeq
                + " in getOrderByTree - tree contains sequences not in alignment");
      }
    }

    return tmp;
  }

  /**
   * Sorts the alignment by a given tree
   * 
   * @param align
   *          alignment to order
   * @param tree
   *          tree which has
   */
  public static void sortByTree(AlignmentI align, TreeModel tree)
  {
    List<SequenceI> tmp = getOrderByTree(align, tree);

    // tmp should properly permute align with tree.
    if (lastTree != tree)
    {
      sortTreeAscending = true;
      lastTree = tree;
    }
    else
    {
      sortTreeAscending = !sortTreeAscending;
    }

    if (sortTreeAscending)
    {
      setOrder(align, tmp);
    }
    else
    {
      setReverseOrder(align,
              vectorSubsetToArray(tmp, align.getSequences()));
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param align
   *          DOCUMENT ME!
   * @param tmp
   *          DOCUMENT ME!
   */
  private static void addStrays(AlignmentI align, List<SequenceI> tmp)
  {
    int nSeq = align.getHeight();

    for (int i = 0; i < nSeq; i++)
    {
      if (!tmp.contains(align.getSequenceAt(i)))
      {
        tmp.add(align.getSequenceAt(i));
      }
    }

    if (nSeq != tmp.size())
    {
      System.err
              .println("ERROR: Size still not right even after addStrays");
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param node
   *          DOCUMENT ME!
   * @param tmp
   *          DOCUMENT ME!
   * @param seqset
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  private static List<SequenceI> _sortByTree(BinaryNode node,
          List<SequenceI> tmp, List<SequenceI> seqset)
  {
    if (node == null)
    {
      return tmp;
    }

    BinaryNode left = (BinaryNode) node.left();
    BinaryNode right = (BinaryNode) node.right();

    if ((left == null) && (right == null))
    {
      if (!(node instanceof SequenceNode
              && ((SequenceNode) node).isPlaceholder())
              && (node.element() != null))
      {
        if (node.element() instanceof SequenceI)
        {
          if (!tmp.contains(node.element())) // && (seqset==null ||
                                             // seqset.size()==0 ||
                                             // seqset.contains(tmp)))
          {
            tmp.add((SequenceI) node.element());
          }
        }
      }

      return tmp;
    }
    else
    {
      _sortByTree(left, tmp, seqset);
      _sortByTree(right, tmp, seqset);
    }

    return tmp;
  }

  // Ordering Objects
  // Alignment.sortBy(OrderObj) - sequence of sequence pointer refs in
  // appropriate order
  //

  /**
   * recover the order of sequences given by the safe numbering scheme introducd
   * SeqsetUtils.uniquify.
   */
  public static void recoverOrder(SequenceI[] alignment)
  {
    float[] ids = new float[alignment.length];

    for (int i = 0; i < alignment.length; i++)
    {
      ids[i] = (Float.valueOf(alignment[i].getName().substring(8)))
              .floatValue();
    }

    jalview.util.QuickSort.sort(ids, alignment);
  }

  /**
   * Sort sequence in order of increasing score attribute for annotation with a
   * particular scoreLabel. Or reverse if same label was used previously
   * 
   * @param scoreLabel
   *          exact label for sequence associated AlignmentAnnotation scores to
   *          use for sorting.
   * @param alignment
   *          sequences to be sorted
   */
  public static void sortByAnnotationScore(String scoreLabel,
          AlignmentI alignment)
  {
    SequenceI[] seqs = alignment.getSequencesArray();
    boolean[] hasScore = new boolean[seqs.length]; // per sequence score
    // presence
    int hasScores = 0; // number of scores present on set
    double[] scores = new double[seqs.length];
    double min = 0, max = 0;
    for (int i = 0; i < seqs.length; i++)
    {
      AlignmentAnnotation[] scoreAnn = seqs[i].getAnnotation(scoreLabel);
      if (scoreAnn != null)
      {
        hasScores++;
        hasScore[i] = true;
        scores[i] = scoreAnn[0].getScore(); // take the first instance of this
        // score.
        if (hasScores == 1)
        {
          max = min = scores[i];
        }
        else
        {
          if (max < scores[i])
          {
            max = scores[i];
          }
          if (min > scores[i])
          {
            min = scores[i];
          }
        }
      }
      else
      {
        hasScore[i] = false;
      }
    }
    if (hasScores == 0)
    {
      return; // do nothing - no scores present to sort by.
    }
    if (hasScores < seqs.length)
    {
      for (int i = 0; i < seqs.length; i++)
      {
        if (!hasScore[i])
        {
          scores[i] = (max + i + 1.0);
        }
      }
    }

    jalview.util.QuickSort.sort(scores, seqs);
    if (lastSortByAnnotation != scoreLabel)
    {
      lastSortByAnnotation = scoreLabel;
      setOrder(alignment, seqs);
    }
    else
    {
      setReverseOrder(alignment, seqs);
    }
  }

  /**
   * types of feature ordering: Sort by score : average score - or total score -
   * over all features in region Sort by feature label text: (or if null -
   * feature type text) - numerical or alphabetical Sort by feature density:
   * based on counts - ignoring individual text or scores for each feature
   */
  public static String FEATURE_SCORE = "average_score";

  public static String FEATURE_LABEL = "text";

  public static String FEATURE_DENSITY = "density";

  /**
   * Sort sequences by feature score or density, optionally restricted by
   * feature types, feature groups, or alignment start/end positions.
   * <p>
   * If the sort is repeated for the same combination of types and groups, sort
   * order is reversed.
   * 
   * @param featureTypes
   *          a list of feature types to include (or null for all)
   * @param groups
   *          a list of feature groups to include (or null for all)
   * @param startCol
   *          start column position to include (base zero)
   * @param endCol
   *          end column position to include (base zero)
   * @param alignment
   *          the alignment to be sorted
   * @param method
   *          either "average_score" or "density" ("text" not yet implemented)
   */
  public static void sortByFeature(List<String> featureTypes,
          List<String> groups, final int startCol, final int endCol,
          AlignmentI alignment, String method)
  {
    if (method != FEATURE_SCORE && method != FEATURE_LABEL
            && method != FEATURE_DENSITY)
    {
      String msg = String.format(
              "Implementation Error - sortByFeature method must be either '%s' or '%s'",
              FEATURE_SCORE, FEATURE_DENSITY);
      System.err.println(msg);
      return;
    }

    flipFeatureSortIfUnchanged(method, featureTypes, groups, startCol,
            endCol);

    SequenceI[] seqs = alignment.getSequencesArray();

    boolean[] hasScore = new boolean[seqs.length]; // per sequence score
    // presence
    int hasScores = 0; // number of scores present on set
    double[] scores = new double[seqs.length];
    int[] seqScores = new int[seqs.length];
    Object[][] feats = new Object[seqs.length][];
    double min = 0d;
    double max = 0d;

    for (int i = 0; i < seqs.length; i++)
    {
      /*
       * get sequence residues overlapping column region
       * and features for residue positions and specified types
       */
      String[] types = featureTypes == null ? null
              : featureTypes.toArray(new String[featureTypes.size()]);
      List<SequenceFeature> sfs = seqs[i].findFeatures(startCol + 1,
              endCol + 1, types);

      seqScores[i] = 0;
      scores[i] = 0.0;

      Iterator<SequenceFeature> it = sfs.listIterator();
      while (it.hasNext())
      {
        SequenceFeature sf = it.next();

        /*
         * accept all features with null or empty group, otherwise
         * check group is one of the currently visible groups
         */
        String featureGroup = sf.getFeatureGroup();
        if (groups != null && featureGroup != null
                && !"".equals(featureGroup)
                && !groups.contains(featureGroup))
        {
          it.remove();
        }
        else
        {
          float score = sf.getScore();
          if (FEATURE_SCORE.equals(method) && !Float.isNaN(score))
          {
            if (seqScores[i] == 0)
            {
              hasScores++;
            }
            seqScores[i]++;
            hasScore[i] = true;
            scores[i] += score;
            // take the first instance of this score // ??
          }
        }
      }

      feats[i] = sfs.toArray(new SequenceFeature[sfs.size()]);
      if (!sfs.isEmpty())
      {
        if (method == FEATURE_LABEL)
        {
          // order the labels by alphabet (not yet implemented)
          String[] labs = new String[sfs.size()];
          for (int l = 0; l < sfs.size(); l++)
          {
            SequenceFeature sf = sfs.get(l);
            String description = sf.getDescription();
            labs[l] = (description != null ? description : sf.getType());
          }
          QuickSort.sort(labs, feats[i]);
        }
      }
      if (hasScore[i])
      {
        // compute average score
        scores[i] /= seqScores[i];
        // update the score bounds.
        if (hasScores == 1)
        {
          min = scores[i];
          max = min;
        }
        else
        {
          max = Math.max(max, scores[i]);
          min = Math.min(min, scores[i]);
        }
      }
    }

    if (FEATURE_SCORE.equals(method))
    {
      if (hasScores == 0)
      {
        return; // do nothing - no scores present to sort by.
      }
      // pad score matrix
      if (hasScores < seqs.length)
      {
        for (int i = 0; i < seqs.length; i++)
        {
          if (!hasScore[i])
          {
            scores[i] = (max + 1 + i);
          }
          else
          {
            // int nf = (feats[i] == null) ? 0
            // : ((SequenceFeature[]) feats[i]).length;
            // // System.err.println("Sorting on Score: seq " +
            // seqs[i].getName()
            // + " Feats: " + nf + " Score : " + scores[i]);
          }
        }
      }
      QuickSort.sortByDouble(scores, seqs, sortByFeatureAscending);
    }
    else if (FEATURE_DENSITY.equals(method))
    {
      for (int i = 0; i < seqs.length; i++)
      {
        int featureCount = feats[i] == null ? 0
                : ((SequenceFeature[]) feats[i]).length;
        scores[i] = featureCount;
        // System.err.println("Sorting on Density: seq "+seqs[i].getName()+
        // " Feats: "+featureCount+" Score : "+scores[i]);
      }
      QuickSort.sortByDouble(scores, seqs, sortByFeatureAscending);
    }

    setOrder(alignment, seqs);
  }

  /**
   * Builds a string hash of criteria for sorting, and if unchanged from last
   * time, reverse the sort order
   * 
   * @param method
   * @param featureTypes
   * @param groups
   * @param startCol
   * @param endCol
   */
  protected static void flipFeatureSortIfUnchanged(String method,
          List<String> featureTypes, List<String> groups,
          final int startCol, final int endCol)
  {
    StringBuilder sb = new StringBuilder(64);
    sb.append(startCol).append(method).append(endCol);
    if (featureTypes != null)
    {
      Collections.sort(featureTypes);
      sb.append(featureTypes.toString());
    }
    if (groups != null)
    {
      Collections.sort(groups);
      sb.append(groups.toString());
    }
    String scoreCriteria = sb.toString();

    /*
     * if resorting on the same criteria, toggle sort order
     */
    if (sortByFeatureCriteria == null
            || !scoreCriteria.equals(sortByFeatureCriteria))
    {
      sortByFeatureAscending = true;
    }
    else
    {
      sortByFeatureAscending = !sortByFeatureAscending;
    }
    sortByFeatureCriteria = scoreCriteria;
  }

}
