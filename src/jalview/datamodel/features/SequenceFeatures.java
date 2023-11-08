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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import intervalstore.api.IntervalI;
import jalview.datamodel.SequenceFeature;
import jalview.io.gff.SequenceOntologyFactory;
import jalview.io.gff.SequenceOntologyI;

/**
 * A class that stores sequence features in a way that supports efficient
 * querying by type and location (overlap). Intended for (but not limited to)
 * storage of features for one sequence.
 * 
 * @author gmcarstairs
 *
 */
public class SequenceFeatures implements SequenceFeaturesI
{
  /*
   * map from feature type to structured store of features for that type
   * null types are permitted (but not a good idea!)
   */
  private Map<String, FeatureStore> featureStore;

  /**
   * Constructor
   */
  public SequenceFeatures()
  {
    /*
     * use a TreeMap so that features are returned in alphabetical order of type
     * ? wrap as a synchronized map for add and delete operations
     */
    // featureStore = Collections
    // .synchronizedSortedMap(new TreeMap<String, FeatureStore>());
    featureStore = new TreeMap<>();
  }

  /**
   * Constructor given a list of features
   */
  public SequenceFeatures(List<SequenceFeature> features)
  {
    this();
    if (features != null)
    {
      for (SequenceFeature feature : features)
      {
        add(feature);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean add(SequenceFeature sf)
  {
    String type = sf.getType();
    if (type == null)
    {
      System.err.println("Feature type may not be null: " + sf.toString());
      return false;
    }

    if (featureStore.get(type) == null)
    {
      featureStore.put(type, new FeatureStore());
    }
    return featureStore.get(type).addFeature(sf);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SequenceFeature> findFeatures(int from, int to,
          String... type)
  {
    List<SequenceFeature> result = new ArrayList<>();

    for (FeatureStore featureSet : varargToTypes(type))
    {
      result.addAll(featureSet.findOverlappingFeatures(from, to));
    }

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SequenceFeature> getAllFeatures(String... type)
  {
    List<SequenceFeature> result = new ArrayList<>();

    result.addAll(getPositionalFeatures(type));

    result.addAll(getNonPositionalFeatures());

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SequenceFeature> getFeaturesByOntology(String... ontologyTerm)
  {
    if (ontologyTerm == null || ontologyTerm.length == 0)
    {
      return new ArrayList<>();
    }

    Set<String> featureTypes = getFeatureTypes(ontologyTerm);
    if (featureTypes.isEmpty())
    {
      /*
       * no features of the specified type or any sub-type
       */
      return new ArrayList<>();
    }

    return getAllFeatures(
            featureTypes.toArray(new String[featureTypes.size()]));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getFeatureCount(boolean positional, String... type)
  {
    int result = 0;

    for (FeatureStore featureSet : varargToTypes(type))
    {
      result += featureSet.getFeatureCount(positional);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getTotalFeatureLength(String... type)
  {
    int result = 0;

    for (FeatureStore featureSet : varargToTypes(type))
    {
      result += featureSet.getTotalFeatureLength();
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SequenceFeature> getPositionalFeatures(String... type)
  {
    List<SequenceFeature> result = new ArrayList<>();

    for (FeatureStore featureSet : varargToTypes(type))
    {
      result.addAll(featureSet.getPositionalFeatures());
    }
    return result;
  }

  /**
   * A convenience method that converts a vararg for feature types to an
   * Iterable over matched feature sets. If no types are specified, all feature
   * sets are returned. If one or more types are specified, feature sets for
   * those types are returned, preserving the order of the types.
   * 
   * @param type
   * @return
   */
  protected Iterable<FeatureStore> varargToTypes(String... type)
  {
    if (type == null || type.length == 0)
    {
      /*
       * no vararg parameter supplied - return all
       */
      return featureStore.values();
    }

    List<FeatureStore> types = new ArrayList<>();
    for (String theType : type)
    {
      if (theType != null && featureStore.containsKey(theType))
      {
        types.add(featureStore.get(theType));
      }
    }
    return types;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SequenceFeature> getContactFeatures(String... type)
  {
    List<SequenceFeature> result = new ArrayList<>();

    for (FeatureStore featureSet : varargToTypes(type))
    {
      result.addAll(featureSet.getContactFeatures());
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SequenceFeature> getNonPositionalFeatures(String... type)
  {
    List<SequenceFeature> result = new ArrayList<>();

    for (FeatureStore featureSet : varargToTypes(type))
    {
      result.addAll(featureSet.getNonPositionalFeatures());
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean delete(SequenceFeature sf)
  {
    for (FeatureStore featureSet : featureStore.values())
    {
      if (featureSet.delete(sf))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasFeatures()
  {
    for (FeatureStore featureSet : featureStore.values())
    {
      if (!featureSet.isEmpty())
      {
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> getFeatureGroups(boolean positionalFeatures,
          String... type)
  {
    Set<String> groups = new HashSet<>();

    for (FeatureStore featureSet : varargToTypes(type))
    {
      groups.addAll(featureSet.getFeatureGroups(positionalFeatures));
    }

    return groups;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> getFeatureTypesForGroups(boolean positionalFeatures,
          String... groups)
  {
    Set<String> result = new HashSet<>();

    for (Entry<String, FeatureStore> featureType : featureStore.entrySet())
    {
      Set<String> featureGroups = featureType.getValue()
              .getFeatureGroups(positionalFeatures);
      for (String group : groups)
      {
        if (featureGroups.contains(group))
        {
          /*
           * yes this feature type includes one of the query groups
           */
          result.add(featureType.getKey());
          break;
        }
      }
    }

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> getFeatureTypes(String... soTerm)
  {
    Set<String> types = new HashSet<>();
    for (Entry<String, FeatureStore> entry : featureStore.entrySet())
    {
      String type = entry.getKey();
      if (!entry.getValue().isEmpty() && isOntologyTerm(type, soTerm))
      {
        types.add(type);
      }
    }
    return types;
  }

  /**
   * Answers true if the given type matches one of the specified terms (or is a
   * sub-type of one in the Sequence Ontology), or if no terms are supplied.
   * Answers false if filter terms are specified and the given term does not
   * match any of them.
   * 
   * @param type
   * @param soTerm
   * @return
   */
  protected boolean isOntologyTerm(String type, String... soTerm)
  {
    if (soTerm == null || soTerm.length == 0)
    {
      return true;
    }
    SequenceOntologyI so = SequenceOntologyFactory.getInstance();
    for (String term : soTerm)
    {
      if (type.equals(term) || so.isA(type, term))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public float getMinimumScore(String type, boolean positional)
  {
    return featureStore.containsKey(type)
            ? featureStore.get(type).getMinimumScore(positional)
            : Float.NaN;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public float getMaximumScore(String type, boolean positional)
  {
    return featureStore.containsKey(type)
            ? featureStore.get(type).getMaximumScore(positional)
            : Float.NaN;
  }

  /**
   * A convenience method to sort features by start position ascending (if on
   * forward strand), or end position descending (if on reverse strand)
   * 
   * @param features
   * @param forwardStrand
   */
  public static void sortFeatures(List<? extends IntervalI> features,
          final boolean forwardStrand)
  {
    Collections.sort(features,
            forwardStrand ? IntervalI.COMPARE_BEGIN_ASC_END_DESC
                    : IntervalI.COMPARE_END_DESC);
  }

  /**
   * {@inheritDoc} This method is 'semi-optimised': it only inspects features
   * for types that include the specified group, but has to inspect every
   * feature of those types for matching feature group. This is efficient unless
   * a sequence has features that share the same type but are in different
   * groups - an unlikely case.
   * <p>
   * For example, if RESNUM feature is created with group = PDBID, then features
   * would only be retrieved for those sequences associated with the target
   * PDBID (group).
   */
  @Override
  public List<SequenceFeature> getFeaturesForGroup(boolean positional,
          String group, String... type)
  {
    List<SequenceFeature> result = new ArrayList<>();
    for (FeatureStore featureSet : varargToTypes(type))
    {
      if (featureSet.getFeatureGroups(positional).contains(group))
      {
        result.addAll(featureSet.getFeaturesForGroup(positional, group));
      }
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean shiftFeatures(int fromPosition, int shiftBy)
  {
    boolean modified = false;
    for (FeatureStore fs : featureStore.values())
    {
      modified |= fs.shiftFeatures(fromPosition, shiftBy);
    }
    return modified;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteAll()
  {
    featureStore.clear();
  }
}
