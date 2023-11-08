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
package jalview.datamodel.features;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import intervalstore.api.IntervalStoreI;
import intervalstore.impl.BinarySearcher;
import intervalstore.impl.BinarySearcher.Compare;
import intervalstore.impl.IntervalStore;
import jalview.datamodel.SequenceFeature;

/**
 * A data store for a set of sequence features that supports efficient lookup of
 * features overlapping a given range. Intended for (but not limited to) storage
 * of features for one sequence and feature type.
 * 
 * @author gmcarstairs
 *
 */
public class FeatureStore
{
  /*
   * Non-positional features have no (zero) start/end position.
   * Kept as a separate list in case this criterion changes in future.
   */
  List<SequenceFeature> nonPositionalFeatures;

  /*
   * contact features ordered by first contact position
   */
  List<SequenceFeature> contactFeatureStarts;

  /*
   * contact features ordered by second contact position
   */
  List<SequenceFeature> contactFeatureEnds;

  /*
   * IntervalStore holds remaining features and provides efficient
   * query for features overlapping any given interval
   */
  IntervalStoreI<SequenceFeature> features;

  /*
   * Feature groups represented in stored positional features 
   * (possibly including null)
   */
  Set<String> positionalFeatureGroups;

  /*
   * Feature groups represented in stored non-positional features 
   * (possibly including null)
   */
  Set<String> nonPositionalFeatureGroups;

  /*
   * the total length of all positional features; contact features count 1 to
   * the total and 1 to size(), consistent with an average 'feature length' of 1
   */
  int totalExtent;

  float positionalMinScore;

  float positionalMaxScore;

  float nonPositionalMinScore;

  float nonPositionalMaxScore;

  /**
   * Constructor
   */
  public FeatureStore()
  {
    features = new IntervalStore<>();
    positionalFeatureGroups = new HashSet<>();
    nonPositionalFeatureGroups = new HashSet<>();
    positionalMinScore = Float.NaN;
    positionalMaxScore = Float.NaN;
    nonPositionalMinScore = Float.NaN;
    nonPositionalMaxScore = Float.NaN;

    // we only construct nonPositionalFeatures, contactFeatures if we need to
  }

  /**
   * Adds one sequence feature to the store, and returns true, unless the
   * feature is already contained in the store, in which case this method
   * returns false. Containment is determined by SequenceFeature.equals()
   * comparison.
   * 
   * @param feature
   */
  public boolean addFeature(SequenceFeature feature)
  {
    if (contains(feature))
    {
      return false;
    }

    /*
     * keep a record of feature groups
     */
    if (!feature.isNonPositional())
    {
      positionalFeatureGroups.add(feature.getFeatureGroup());
    }

    if (feature.isContactFeature())
    {
      addContactFeature(feature);
    }
    else if (feature.isNonPositional())
    {
      addNonPositionalFeature(feature);
    }
    else
    {
      addNestedFeature(feature);
    }

    /*
     * record the total extent of positional features, to make
     * getTotalFeatureLength possible; we count the length of a 
     * contact feature as 1
     */
    totalExtent += getFeatureLength(feature);

    /*
     * record the minimum and maximum score for positional
     * and non-positional features
     */
    float score = feature.getScore();
    if (!Float.isNaN(score))
    {
      if (feature.isNonPositional())
      {
        nonPositionalMinScore = min(nonPositionalMinScore, score);
        nonPositionalMaxScore = max(nonPositionalMaxScore, score);
      }
      else
      {
        positionalMinScore = min(positionalMinScore, score);
        positionalMaxScore = max(positionalMaxScore, score);
      }
    }

    return true;
  }

  /**
   * Answers true if this store contains the given feature (testing by
   * SequenceFeature.equals), else false
   * 
   * @param feature
   * @return
   */
  public boolean contains(SequenceFeature feature)
  {
    if (feature.isNonPositional())
    {
      return nonPositionalFeatures == null ? false
              : nonPositionalFeatures.contains(feature);
    }

    if (feature.isContactFeature())
    {
      return contactFeatureStarts == null ? false
              : listContains(contactFeatureStarts, feature);
    }

    return features == null ? false : features.contains(feature);
  }

  /**
   * Answers the 'length' of the feature, counting 0 for non-positional features
   * and 1 for contact features
   * 
   * @param feature
   * @return
   */
  protected static int getFeatureLength(SequenceFeature feature)
  {
    if (feature.isNonPositional())
    {
      return 0;
    }
    if (feature.isContactFeature())
    {
      return 1;
    }
    return 1 + feature.getEnd() - feature.getBegin();
  }

  /**
   * Adds the feature to the list of non-positional features (with lazy
   * instantiation of the list if it is null), and returns true. The feature
   * group is added to the set of distinct feature groups for non-positional
   * features. This method allows duplicate features, so test before calling to
   * prevent this.
   * 
   * @param feature
   */
  protected boolean addNonPositionalFeature(SequenceFeature feature)
  {
    if (nonPositionalFeatures == null)
    {
      nonPositionalFeatures = new ArrayList<>();
    }

    nonPositionalFeatures.add(feature);

    nonPositionalFeatureGroups.add(feature.getFeatureGroup());

    return true;
  }

  /**
   * Adds one feature to the IntervalStore that can manage nested features
   * (creating the IntervalStore if necessary)
   */
  protected synchronized void addNestedFeature(SequenceFeature feature)
  {
    if (features == null)
    {
      features = new IntervalStore<>();
    }
    features.add(feature);
  }

  /**
   * Add a contact feature to the lists that hold them ordered by start (first
   * contact) and by end (second contact) position, ensuring the lists remain
   * ordered, and returns true. This method allows duplicate features to be
   * added, so test before calling to avoid this.
   * 
   * @param feature
   * @return
   */
  protected synchronized boolean addContactFeature(SequenceFeature feature)
  {
    if (contactFeatureStarts == null)
    {
      contactFeatureStarts = new ArrayList<>();
    }
    if (contactFeatureEnds == null)
    {
      contactFeatureEnds = new ArrayList<>();
    }

    /*
     * insert into list sorted by start (first contact position):
     * binary search the sorted list to find the insertion point
     */
    int insertPosition = BinarySearcher.findFirst(contactFeatureStarts,
            true, Compare.GE, feature.getBegin());
    contactFeatureStarts.add(insertPosition, feature);

    /*
     * insert into list sorted by end (second contact position):
     * binary search the sorted list to find the insertion point
     */
    insertPosition = BinarySearcher.findFirst(contactFeatureEnds, false,
            Compare.GE, feature.getEnd());
    contactFeatureEnds.add(insertPosition, feature);

    return true;
  }

  /**
   * Answers true if the list contains the feature, else false. This method is
   * optimised for the condition that the list is sorted on feature start
   * position ascending, and will give unreliable results if this does not hold.
   * 
   * @param features
   * @param feature
   * @return
   */
  protected static boolean listContains(List<SequenceFeature> features,
          SequenceFeature feature)
  {
    if (features == null || feature == null)
    {
      return false;
    }

    /*
     * locate the first entry in the list which does not precede the feature
     */
    // int pos = binarySearch(features,
    // SearchCriterion.byFeature(feature, RangeComparator.BY_START_POSITION));
    int pos = BinarySearcher.findFirst(features, true, Compare.GE,
            feature.getBegin());
    int len = features.size();
    while (pos < len)
    {
      SequenceFeature sf = features.get(pos);
      if (sf.getBegin() > feature.getBegin())
      {
        return false; // no match found
      }
      if (sf.equals(feature))
      {
        return true;
      }
      pos++;
    }
    return false;
  }

  /**
   * Returns a (possibly empty) list of features whose extent overlaps the given
   * range. The returned list is not ordered. Contact features are included if
   * either of the contact points lies within the range.
   * 
   * @param start
   *          start position of overlap range (inclusive)
   * @param end
   *          end position of overlap range (inclusive)
   * @return
   */
  public List<SequenceFeature> findOverlappingFeatures(long start, long end)
  {
    List<SequenceFeature> result = new ArrayList<>();

    findContactFeatures(start, end, result);

    if (features != null)
    {
      result.addAll(features.findOverlaps(start, end));
    }

    return result;
  }

  /**
   * Adds contact features to the result list where either the second or the
   * first contact position lies within the target range
   * 
   * @param from
   * @param to
   * @param result
   */
  protected void findContactFeatures(long from, long to,
          List<SequenceFeature> result)
  {
    if (contactFeatureStarts != null)
    {
      findContactStartOverlaps(from, to, result);
    }
    if (contactFeatureEnds != null)
    {
      findContactEndOverlaps(from, to, result);
    }
  }

  /**
   * Adds to the result list any contact features whose end (second contact
   * point), but not start (first contact point), lies in the query from-to
   * range
   * 
   * @param from
   * @param to
   * @param result
   */
  protected void findContactEndOverlaps(long from, long to,
          List<SequenceFeature> result)
  {
    /*
     * find the first contact feature (if any) 
     * whose end point is not before the target range
     */
    int index = BinarySearcher.findFirst(contactFeatureEnds, false,
            Compare.GE, (int) from);

    while (index < contactFeatureEnds.size())
    {
      SequenceFeature sf = contactFeatureEnds.get(index);
      if (!sf.isContactFeature())
      {
        System.err.println("Error! non-contact feature type " + sf.getType()
                + " in contact features list");
        index++;
        continue;
      }

      int begin = sf.getBegin();
      if (begin >= from && begin <= to)
      {
        /*
         * this feature's first contact position lies in the search range
         * so we don't include it in results a second time
         */
        index++;
        continue;
      }

      if (sf.getEnd() > to)
      {
        /*
         * this feature (and all following) has end point after the target range
         */
        break;
      }

      /*
       * feature has end >= from and end <= to
       * i.e. contact end point lies within overlap search range
       */
      result.add(sf);
      index++;
    }
  }

  /**
   * Adds contact features whose start position lies in the from-to range to the
   * result list
   * 
   * @param from
   * @param to
   * @param result
   */
  protected void findContactStartOverlaps(long from, long to,
          List<SequenceFeature> result)
  {
    int index = BinarySearcher.findFirst(contactFeatureStarts, true,
            Compare.GE, (int) from);

    while (index < contactFeatureStarts.size())
    {
      SequenceFeature sf = contactFeatureStarts.get(index);
      if (!sf.isContactFeature())
      {
        System.err.println("Error! non-contact feature " + sf.toString()
                + " in contact features list");
        index++;
        continue;
      }
      if (sf.getBegin() > to)
      {
        /*
         * this feature's start (and all following) follows the target range
         */
        break;
      }

      /*
       * feature has begin >= from and begin <= to
       * i.e. contact start point lies within overlap search range
       */
      result.add(sf);
      index++;
    }
  }

  /**
   * Answers a list of all positional features stored, in no guaranteed order
   * 
   * @return
   */
  public List<SequenceFeature> getPositionalFeatures()
  {
    List<SequenceFeature> result = new ArrayList<>();

    /*
     * add any contact features - from the list by start position
     */
    if (contactFeatureStarts != null)
    {
      result.addAll(contactFeatureStarts);
    }

    /*
     * add any nested features
     */
    if (features != null)
    {
      result.addAll(features);
    }

    return result;
  }

  /**
   * Answers a list of all contact features. If there are none, returns an
   * immutable empty list.
   * 
   * @return
   */
  public List<SequenceFeature> getContactFeatures()
  {
    if (contactFeatureStarts == null)
    {
      return Collections.emptyList();
    }
    return new ArrayList<>(contactFeatureStarts);
  }

  /**
   * Answers a list of all non-positional features. If there are none, returns
   * an immutable empty list.
   * 
   * @return
   */
  public List<SequenceFeature> getNonPositionalFeatures()
  {
    if (nonPositionalFeatures == null)
    {
      return Collections.emptyList();
    }
    return new ArrayList<>(nonPositionalFeatures);
  }

  /**
   * Deletes the given feature from the store, returning true if it was found
   * (and deleted), else false. This method makes no assumption that the feature
   * is in the 'expected' place in the store, in case it has been modified since
   * it was added.
   * 
   * @param sf
   */
  public synchronized boolean delete(SequenceFeature sf)
  {
    boolean removed = false;

    /*
     * try contact positions (and if found, delete
     * from both lists of contact positions)
     */
    if (!removed && contactFeatureStarts != null)
    {
      removed = contactFeatureStarts.remove(sf);
      if (removed)
      {
        contactFeatureEnds.remove(sf);
      }
    }

    boolean removedNonPositional = false;

    /*
     * if not found, try non-positional features
     */
    if (!removed && nonPositionalFeatures != null)
    {
      removedNonPositional = nonPositionalFeatures.remove(sf);
      removed = removedNonPositional;
    }

    /*
     * if not found, try nested features
     */
    if (!removed && features != null)
    {
      removed = features.remove(sf);
    }

    if (removed)
    {
      rescanAfterDelete();
    }

    return removed;
  }

  /**
   * Rescan all features to recompute any cached values after an entry has been
   * deleted. This is expected to be an infrequent event, so performance here is
   * not critical.
   */
  protected synchronized void rescanAfterDelete()
  {
    positionalFeatureGroups.clear();
    nonPositionalFeatureGroups.clear();
    totalExtent = 0;
    positionalMinScore = Float.NaN;
    positionalMaxScore = Float.NaN;
    nonPositionalMinScore = Float.NaN;
    nonPositionalMaxScore = Float.NaN;

    /*
     * scan non-positional features for groups and scores
     */
    for (SequenceFeature sf : getNonPositionalFeatures())
    {
      nonPositionalFeatureGroups.add(sf.getFeatureGroup());
      float score = sf.getScore();
      nonPositionalMinScore = min(nonPositionalMinScore, score);
      nonPositionalMaxScore = max(nonPositionalMaxScore, score);
    }

    /*
     * scan positional features for groups, scores and extents
     */
    for (SequenceFeature sf : getPositionalFeatures())
    {
      positionalFeatureGroups.add(sf.getFeatureGroup());
      float score = sf.getScore();
      positionalMinScore = min(positionalMinScore, score);
      positionalMaxScore = max(positionalMaxScore, score);
      totalExtent += getFeatureLength(sf);
    }
  }

  /**
   * A helper method to return the minimum of two floats, where a non-NaN value
   * is treated as 'less than' a NaN value (unlike Math.min which does the
   * opposite)
   * 
   * @param f1
   * @param f2
   */
  protected static float min(float f1, float f2)
  {
    if (Float.isNaN(f1))
    {
      return Float.isNaN(f2) ? f1 : f2;
    }
    else
    {
      return Float.isNaN(f2) ? f1 : Math.min(f1, f2);
    }
  }

  /**
   * A helper method to return the maximum of two floats, where a non-NaN value
   * is treated as 'greater than' a NaN value (unlike Math.max which does the
   * opposite)
   * 
   * @param f1
   * @param f2
   */
  protected static float max(float f1, float f2)
  {
    if (Float.isNaN(f1))
    {
      return Float.isNaN(f2) ? f1 : f2;
    }
    else
    {
      return Float.isNaN(f2) ? f1 : Math.max(f1, f2);
    }
  }

  /**
   * Answers true if this store has no features, else false
   * 
   * @return
   */
  public boolean isEmpty()
  {
    boolean hasFeatures = (contactFeatureStarts != null
            && !contactFeatureStarts.isEmpty())
            || (nonPositionalFeatures != null
                    && !nonPositionalFeatures.isEmpty())
            || (features != null && features.size() > 0);

    return !hasFeatures;
  }

  /**
   * Answers the set of distinct feature groups stored, possibly including null,
   * as an unmodifiable view of the set. The parameter determines whether the
   * groups for positional or for non-positional features are returned.
   * 
   * @param positionalFeatures
   * @return
   */
  public Set<String> getFeatureGroups(boolean positionalFeatures)
  {
    if (positionalFeatures)
    {
      return Collections.unmodifiableSet(positionalFeatureGroups);
    }
    else
    {
      return nonPositionalFeatureGroups == null
              ? Collections.<String> emptySet()
              : Collections.unmodifiableSet(nonPositionalFeatureGroups);
    }
  }

  /**
   * Answers the number of positional (or non-positional) features stored.
   * Contact features count as 1.
   * 
   * @param positional
   * @return
   */
  public int getFeatureCount(boolean positional)
  {
    if (!positional)
    {
      return nonPositionalFeatures == null ? 0
              : nonPositionalFeatures.size();
    }

    int size = 0;

    if (contactFeatureStarts != null)
    {
      // note a contact feature (start/end) counts as one
      size += contactFeatureStarts.size();
    }

    if (features != null)
    {
      size += features.size();
    }

    return size;
  }

  /**
   * Answers the total length of positional features (or zero if there are
   * none). Contact features contribute a value of 1 to the total.
   * 
   * @return
   */
  public int getTotalFeatureLength()
  {
    return totalExtent;
  }

  /**
   * Answers the minimum score held for positional or non-positional features.
   * This may be Float.NaN if there are no features, are none has a non-NaN
   * score.
   * 
   * @param positional
   * @return
   */
  public float getMinimumScore(boolean positional)
  {
    return positional ? positionalMinScore : nonPositionalMinScore;
  }

  /**
   * Answers the maximum score held for positional or non-positional features.
   * This may be Float.NaN if there are no features, are none has a non-NaN
   * score.
   * 
   * @param positional
   * @return
   */
  public float getMaximumScore(boolean positional)
  {
    return positional ? positionalMaxScore : nonPositionalMaxScore;
  }

  /**
   * Answers a list of all either positional or non-positional features whose
   * feature group matches the given group (which may be null)
   * 
   * @param positional
   * @param group
   * @return
   */
  public List<SequenceFeature> getFeaturesForGroup(boolean positional,
          String group)
  {
    List<SequenceFeature> result = new ArrayList<>();

    /*
     * if we know features don't include the target group, no need
     * to inspect them for matches
     */
    if (positional && !positionalFeatureGroups.contains(group)
            || !positional && !nonPositionalFeatureGroups.contains(group))
    {
      return result;
    }

    List<SequenceFeature> sfs = positional ? getPositionalFeatures()
            : getNonPositionalFeatures();
    for (SequenceFeature sf : sfs)
    {
      String featureGroup = sf.getFeatureGroup();
      if (group == null && featureGroup == null
              || group != null && group.equals(featureGroup))
      {
        result.add(sf);
      }
    }
    return result;
  }

  /**
   * Adds the shift amount to the start and end of all positional features whose
   * start position is at or after fromPosition. Returns true if at least one
   * feature was shifted, else false.
   * 
   * @param fromPosition
   * @param shiftBy
   * @return
   */
  public synchronized boolean shiftFeatures(int fromPosition, int shiftBy)
  {
    /*
     * Because begin and end are final fields (to ensure the data store's
     * integrity), we have to delete each feature and re-add it as amended.
     * (Although a simple shift of all values would preserve data integrity!)
     */
    boolean modified = false;
    for (SequenceFeature sf : getPositionalFeatures())
    {
      if (sf.getBegin() >= fromPosition)
      {
        modified = true;
        int newBegin = sf.getBegin() + shiftBy;
        int newEnd = sf.getEnd() + shiftBy;

        /*
         * sanity check: don't shift left of the first residue
         */
        if (newEnd > 0)
        {
          newBegin = Math.max(1, newBegin);
          SequenceFeature sf2 = new SequenceFeature(sf, newBegin, newEnd,
                  sf.getFeatureGroup(), sf.getScore());
          addFeature(sf2);
        }
        delete(sf);
      }
    }
    return modified;
  }
}
