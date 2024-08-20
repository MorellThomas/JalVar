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
package jalview.viewmodel.seqfeatures;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jalview.api.AlignViewportI;
import jalview.api.FeatureColourI;
import jalview.api.FeaturesDisplayedI;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.AlignedCodonFrame.SequenceToSequenceMapping;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.MappedFeatures;
import jalview.datamodel.SearchResultMatchI;
import jalview.datamodel.SearchResults;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.datamodel.features.FeatureMatcherSetI;
import jalview.datamodel.features.SequenceFeatures;
import jalview.renderer.seqfeatures.FeatureRenderer;
import jalview.schemes.FeatureColour;
import jalview.util.ColorUtils;
import jalview.util.Platform;

public abstract class FeatureRendererModel
        implements jalview.api.FeatureRenderer
{
  /*
   * a data bean to hold one row of feature settings from the gui
   */
  public static class FeatureSettingsBean
  {
    public final String featureType;

    public final FeatureColourI featureColour;

    public final FeatureMatcherSetI filter;

    public final Boolean show;

    public FeatureSettingsBean(String type, FeatureColourI colour,
            FeatureMatcherSetI theFilter, Boolean isShown)
    {
      featureType = type;
      featureColour = colour;
      filter = theFilter;
      show = isShown;
    }
  }

  /*
   * global transparency for feature
   */
  protected float transparency = 1.0f;

  /*
   * colour scheme for each feature type
   */
  protected Map<String, FeatureColourI> featureColours = new ConcurrentHashMap<>();

  /*
   * visibility flag for each feature group
   */
  protected Map<String, Boolean> featureGroups = new ConcurrentHashMap<>();

  /*
   * filters for each feature type
   */
  protected Map<String, FeatureMatcherSetI> featureFilters = new HashMap<>();

  protected String[] renderOrder;

  Map<String, Float> featureOrder = null;

  protected AlignViewportI av;

  private PropertyChangeSupport changeSupport = new PropertyChangeSupport(
          this);

  @Override
  public AlignViewportI getViewport()
  {
    return av;
  }

  public FeatureRendererSettings getSettings()
  {
    return new FeatureRendererSettings(this);
  }

  public void transferSettings(FeatureRendererSettings fr)
  {
    this.renderOrder = fr.renderOrder;
    this.featureGroups = fr.featureGroups;
    this.featureColours = fr.featureColours;
    this.transparency = fr.transparency;
    this.featureOrder = fr.featureOrder;
  }

  /**
   * update from another feature renderer
   * 
   * @param fr
   *          settings to copy
   */
  public void transferSettings(jalview.api.FeatureRenderer _fr)
  {
    FeatureRenderer fr = (FeatureRenderer) _fr;
    FeatureRendererSettings frs = new FeatureRendererSettings(fr);
    this.renderOrder = frs.renderOrder;
    this.featureGroups = frs.featureGroups;
    this.featureColours = frs.featureColours;
    this.featureFilters = frs.featureFilters;
    this.transparency = frs.transparency;
    this.featureOrder = frs.featureOrder;
    if (av != null && av != fr.getViewport())
    {
      // copy over the displayed feature settings
      if (_fr.getFeaturesDisplayed() != null)
      {
        FeaturesDisplayedI fd = getFeaturesDisplayed();
        if (fd == null)
        {
          setFeaturesDisplayedFrom(_fr.getFeaturesDisplayed());
        }
        else
        {
          synchronized (fd)
          {
            fd.clear();
            for (String type : _fr.getFeaturesDisplayed()
                    .getVisibleFeatures())
            {
              fd.setVisible(type);
            }
          }
        }
      }
    }
  }

  public void setFeaturesDisplayedFrom(FeaturesDisplayedI featuresDisplayed)
  {
    av.setFeaturesDisplayed(new FeaturesDisplayed(featuresDisplayed));
  }

  @Override
  public void setVisible(String featureType)
  {
    FeaturesDisplayedI fdi = av.getFeaturesDisplayed();
    if (fdi == null)
    {
      av.setFeaturesDisplayed(fdi = new FeaturesDisplayed());
    }
    if (!fdi.isRegistered(featureType))
    {
      pushFeatureType(Arrays.asList(new String[] { featureType }));
    }
    fdi.setVisible(featureType);
  }

  @Override
  public void setAllVisible(List<String> featureTypes)
  {
    FeaturesDisplayedI fdi = av.getFeaturesDisplayed();
    if (fdi == null)
    {
      av.setFeaturesDisplayed(fdi = new FeaturesDisplayed());
    }
    List<String> nft = new ArrayList<>();
    for (String featureType : featureTypes)
    {
      if (!fdi.isRegistered(featureType))
      {
        nft.add(featureType);
      }
    }
    if (nft.size() > 0)
    {
      pushFeatureType(nft);
    }
    fdi.setAllVisible(featureTypes);
  }

  /**
   * push a set of new types onto the render order stack. Note - this is a
   * direct mechanism rather than the one employed in updateRenderOrder
   * 
   * @param types
   */
  private void pushFeatureType(List<String> types)
  {

    int ts = types.size();
    String neworder[] = new String[(renderOrder == null ? 0
            : renderOrder.length) + ts];
    types.toArray(neworder);
    if (renderOrder != null)
    {
      System.arraycopy(neworder, 0, neworder, renderOrder.length, ts);
      System.arraycopy(renderOrder, 0, neworder, 0, renderOrder.length);
    }
    renderOrder = neworder;
  }

  protected Map<String, float[][]> minmax = new Hashtable<>();

  public Map<String, float[][]> getMinMax()
  {
    return minmax;
  }

  /**
   * normalise a score against the max/min bounds for the feature type.
   * 
   * @param sequenceFeature
   * @return byte[] { signed, normalised signed (-127 to 127) or unsigned
   *         (0-255) value.
   */
  protected final byte[] normaliseScore(SequenceFeature sequenceFeature)
  {
    float[] mm = minmax.get(sequenceFeature.type)[0];
    final byte[] r = new byte[] { 0, (byte) 255 };
    if (mm != null)
    {
      if (r[0] != 0 || mm[0] < 0.0)
      {
        r[0] = 1;
        r[1] = (byte) ((int) 128.0
                + 127.0 * (sequenceFeature.score / mm[1]));
      }
      else
      {
        r[1] = (byte) ((int) 255.0 * (sequenceFeature.score / mm[1]));
      }
    }
    return r;
  }

  boolean newFeatureAdded = false;

  boolean findingFeatures = false;

  protected boolean updateFeatures()
  {
    if (av.getFeaturesDisplayed() == null || renderOrder == null
            || newFeatureAdded)
    {
      findAllFeatures();
      if (av.getFeaturesDisplayed().getVisibleFeatureCount() < 1)
      {
        return false;
      }
    }
    // TODO: decide if we should check for the visible feature count first
    return true;
  }

  /**
   * search the alignment for all new features, give them a colour and display
   * them. Then fires a PropertyChangeEvent on the changeSupport object.
   * 
   */
  protected void findAllFeatures()
  {
    synchronized (firing)
    {
      if (firing.equals(Boolean.FALSE))
      {
        firing = Boolean.TRUE;
        findAllFeatures(true); // add all new features as visible
        notifyFeaturesChanged();
        firing = Boolean.FALSE;
      }
    }
  }

  @Override
  public void notifyFeaturesChanged()
  {
    changeSupport.firePropertyChange("changeSupport", null, null);
  }

  @Override
  public List<SequenceFeature> findFeaturesAtColumn(SequenceI sequence,
          int column)
  {
    /*
     * include features at the position provided their feature type is 
     * displayed, and feature group is null or marked for display
     */
    List<SequenceFeature> result = new ArrayList<>();
    if (!av.areFeaturesDisplayed() || getFeaturesDisplayed() == null)
    {
      return result;
    }

    Set<String> visibleFeatures = getFeaturesDisplayed()
            .getVisibleFeatures();
    String[] visibleTypes = visibleFeatures
            .toArray(new String[visibleFeatures.size()]);
    List<SequenceFeature> features = sequence.findFeatures(column, column,
            visibleTypes);

    /*
     * include features unless they are hidden (have no colour), based on 
     * feature group visibility, or a filter or colour threshold
     */
    for (SequenceFeature sf : features)
    {
      if (getColour(sf) != null)
      {
        result.add(sf);
      }
    }
    return result;
  }

  /**
   * Searches alignment for all features and updates colours
   * 
   * @param newMadeVisible
   *          if true newly added feature types will be rendered immediately
   *          TODO: check to see if this method should actually be proxied so
   *          repaint events can be propagated by the renderer code
   */
  @Override
  public synchronized void findAllFeatures(boolean newMadeVisible)
  {
    newFeatureAdded = false;

    if (findingFeatures)
    {
      newFeatureAdded = true;
      return;
    }

    findingFeatures = true;
    if (av.getFeaturesDisplayed() == null)
    {
      av.setFeaturesDisplayed(new FeaturesDisplayed());
    }
    FeaturesDisplayedI featuresDisplayed = av.getFeaturesDisplayed();

    Set<String> oldfeatures = new HashSet<>();
    if (renderOrder != null)
    {
      for (int i = 0; i < renderOrder.length; i++)
      {
        if (renderOrder[i] != null)
        {
          oldfeatures.add(renderOrder[i]);
        }
      }
    }

    AlignmentI alignment = av.getAlignment();
    List<String> allfeatures = new ArrayList<>();

    for (int i = 0; i < alignment.getHeight(); i++)
    {
      SequenceI asq = alignment.getSequenceAt(i);
      for (String group : asq.getFeatures().getFeatureGroups(true))
      {
        boolean groupDisplayed = true;
        if (group != null)
        {
          if (featureGroups.containsKey(group))
          {
            groupDisplayed = featureGroups.get(group);
          }
          else
          {
            groupDisplayed = newMadeVisible;
            featureGroups.put(group, groupDisplayed);
          }
        }
        if (groupDisplayed)
        {
          Set<String> types = asq.getFeatures()
                  .getFeatureTypesForGroups(true, group);
          for (String type : types)
          {
            if (!allfeatures.contains(type)) // or use HashSet and no test?
            {
              allfeatures.add(type);
            }
            updateMinMax(asq, type, true); // todo: for all features?
          }
        }
      }
    }

    // uncomment to add new features in alphebetical order (but JAL-2575)
    // Collections.sort(allfeatures, String.CASE_INSENSITIVE_ORDER);
    if (newMadeVisible)
    {
      for (String type : allfeatures)
      {
        if (!oldfeatures.contains(type))
        {
          featuresDisplayed.setVisible(type);
          setOrder(type, 0);
        }
      }
    }

    updateRenderOrder(allfeatures);
    findingFeatures = false;
  }

  /**
   * Updates the global (alignment) min and max values for a feature type from
   * the score for a sequence, if the score is not NaN. Values are stored
   * separately for positional and non-positional features.
   * 
   * @param seq
   * @param featureType
   * @param positional
   */
  protected void updateMinMax(SequenceI seq, String featureType,
          boolean positional)
  {
    float min = seq.getFeatures().getMinimumScore(featureType, positional);
    if (Float.isNaN(min))
    {
      return;
    }

    float max = seq.getFeatures().getMaximumScore(featureType, positional);

    /*
     * stored values are 
     * { {positionalMin, positionalMax}, {nonPositionalMin, nonPositionalMax} }
     */
    if (minmax == null)
    {
      minmax = new Hashtable<>();
    }
    synchronized (minmax)
    {
      float[][] mm = minmax.get(featureType);
      int index = positional ? 0 : 1;
      if (mm == null)
      {
        mm = new float[][] { null, null };
        minmax.put(featureType, mm);
      }
      if (mm[index] == null)
      {
        mm[index] = new float[] { min, max };
      }
      else
      {
        mm[index][0] = Math.min(mm[index][0], min);
        mm[index][1] = Math.max(mm[index][1], max);
      }
    }
  }

  protected Boolean firing = Boolean.FALSE;

  /**
   * replaces the current renderOrder with the unordered features in
   * allfeatures. The ordering of any types in both renderOrder and allfeatures
   * is preserved, and all new feature types are rendered on top of the existing
   * types, in the order given by getOrder or the order given in allFeatures.
   * Note. this operates directly on the featureOrder hash for efficiency. TODO:
   * eliminate the float storage for computing/recalling the persistent ordering
   * New Cability: updates min/max for colourscheme range if its dynamic
   * 
   * @param allFeatures
   */
  private void updateRenderOrder(List<String> allFeatures)
  {
    List<String> allfeatures = new ArrayList<>(allFeatures);
    String[] oldRender = renderOrder;
    renderOrder = new String[allfeatures.size()];
    boolean initOrders = (featureOrder == null);
    int opos = 0;
    if (oldRender != null && oldRender.length > 0)
    {
      for (int j = 0; j < oldRender.length; j++)
      {
        if (oldRender[j] != null)
        {
          if (initOrders)
          {
            setOrder(oldRender[j],
                    (1 - (1 + (float) j) / oldRender.length));
          }
          if (allfeatures.contains(oldRender[j]))
          {
            renderOrder[opos++] = oldRender[j]; // existing features always
            // appear below new features
            allfeatures.remove(oldRender[j]);
            if (minmax != null)
            {
              float[][] mmrange = minmax.get(oldRender[j]);
              if (mmrange != null)
              {
                FeatureColourI fc = featureColours.get(oldRender[j]);
                if (fc != null && !fc.isSimpleColour() && fc.isAutoScaled()
                        && !fc.isColourByAttribute())
                {
                  fc.updateBounds(mmrange[0][0], mmrange[0][1]);
                }
              }
            }
          }
        }
      }
    }
    if (allfeatures.size() == 0)
    {
      // no new features - leave order unchanged.
      return;
    }
    int i = allfeatures.size() - 1;
    int iSize = i;
    boolean sort = false;
    String[] newf = new String[allfeatures.size()];
    float[] sortOrder = new float[allfeatures.size()];
    for (String newfeat : allfeatures)
    {
      newf[i] = newfeat;
      if (minmax != null)
      {
        // update from new features minmax if necessary
        float[][] mmrange = minmax.get(newf[i]);
        if (mmrange != null)
        {
          FeatureColourI fc = featureColours.get(newf[i]);
          if (fc != null && !fc.isSimpleColour() && fc.isAutoScaled()
                  && !fc.isColourByAttribute())
          {
            fc.updateBounds(mmrange[0][0], mmrange[0][1]);
          }
        }
      }
      if (initOrders || !featureOrder.containsKey(newf[i]))
      {
        int denom = initOrders ? allfeatures.size() : featureOrder.size();
        // new unordered feature - compute persistent ordering at head of
        // existing features.
        setOrder(newf[i], i / (float) denom);
      }
      // set order from newly found feature from persisted ordering.
      sortOrder[i] = 2 - featureOrder.get(newf[i]).floatValue();
      if (i < iSize)
      {
        // only sort if we need to
        sort = sort || sortOrder[i] > sortOrder[i + 1];
      }
      i--;
    }
    if (iSize > 1 && sort)
    {
      jalview.util.QuickSort.sort(sortOrder, newf);
    }
    sortOrder = null;
    System.arraycopy(newf, 0, renderOrder, opos, newf.length);
  }

  /**
   * get a feature style object for the given type string. Creates a
   * java.awt.Color for a featureType with no existing colourscheme.
   * 
   * @param featureType
   * @return
   */
  @Override
  public FeatureColourI getFeatureStyle(String featureType)
  {
    FeatureColourI fc = featureColours.get(featureType);
    if (fc == null)
    {
      Color col = ColorUtils.createColourFromName(featureType);
      fc = new FeatureColour(col);
      featureColours.put(featureType, fc);
    }
    return fc;
  }

  @Override
  public Color getColour(SequenceFeature feature)
  {
    FeatureColourI fc = getFeatureStyle(feature.getType());
    return getColor(feature, fc);
  }

  /**
   * Answers true if the feature type is currently selected to be displayed,
   * else false
   * 
   * @param type
   * @return
   */
  public boolean showFeatureOfType(String type)
  {
    return type == null ? false
            : (av.getFeaturesDisplayed() == null ? true
                    : av.getFeaturesDisplayed().isVisible(type));
  }

  @Override
  public void setColour(String featureType, FeatureColourI col)
  {
    featureColours.put(featureType, col);
  }

  @Override
  public void setTransparency(float value)
  {
    transparency = value;
  }

  @Override
  public float getTransparency()
  {
    return transparency;
  }

  /**
   * analogous to colour - store a normalized ordering for all feature types in
   * this rendering context.
   * 
   * @param type
   *          Feature type string
   * @param position
   *          normalized priority - 0 means always appears on top, 1 means
   *          always last.
   */
  public float setOrder(String type, float position)
  {
    if (featureOrder == null)
    {
      featureOrder = new Hashtable<>();
    }
    featureOrder.put(type, Float.valueOf(position));
    return position;
  }

  /**
   * get the global priority (0 (top) to 1 (bottom))
   * 
   * @param type
   * @return [0,1] or -1 for a type without a priority
   */
  public float getOrder(String type)
  {
    if (featureOrder != null)
    {
      if (featureOrder.containsKey(type))
      {
        return featureOrder.get(type).floatValue();
      }
    }
    return -1;
  }

  @Override
  public Map<String, FeatureColourI> getFeatureColours()
  {
    return featureColours;
  }

  /**
   * Replace current ordering with new ordering
   * 
   * @param data
   *          an array of { Type, Colour, Filter, Boolean }
   * @return true if any visible features have been reordered, else false
   */
  public boolean setFeaturePriority(FeatureSettingsBean[] data)
  {
    return setFeaturePriority(data, true);
  }

  /**
   * Sets the priority order for features, with the highest priority (displayed
   * on top) at the start of the data array
   * 
   * @param data
   *          an array of { Type, Colour, Filter, Boolean }
   * @param visibleNew
   *          when true current featureDisplay list will be cleared
   * @return true if any visible features have been reordered or recoloured,
   *         else false (i.e. no need to repaint)
   */
  public boolean setFeaturePriority(FeatureSettingsBean[] data,
          boolean visibleNew)
  {
    /*
     * note visible feature ordering and colours before update
     */
    List<String> visibleFeatures = getDisplayedFeatureTypes();
    Map<String, FeatureColourI> visibleColours = new HashMap<>(
            getFeatureColours());

    FeaturesDisplayedI av_featuresdisplayed = null;
    if (visibleNew)
    {
      if ((av_featuresdisplayed = av.getFeaturesDisplayed()) != null)
      {
        av.getFeaturesDisplayed().clear();
      }
      else
      {
        av.setFeaturesDisplayed(
                av_featuresdisplayed = new FeaturesDisplayed());
      }
    }
    else
    {
      av_featuresdisplayed = av.getFeaturesDisplayed();
    }
    if (data == null)
    {
      return false;
    }
    // The feature table will display high priority
    // features at the top, but these are the ones
    // we need to render last, so invert the data
    renderOrder = new String[data.length];

    if (data.length > 0)
    {
      for (int i = 0; i < data.length; i++)
      {
        String type = data[i].featureType;
        setColour(type, data[i].featureColour);
        if (data[i].show)
        {
          av_featuresdisplayed.setVisible(type);
        }

        renderOrder[data.length - i - 1] = type;
      }
    }

    /*
     * get the new visible ordering and return true if it has changed
     * order or any colour has changed
     */
    List<String> reorderedVisibleFeatures = getDisplayedFeatureTypes();
    if (!visibleFeatures.equals(reorderedVisibleFeatures))
    {
      /*
       * the list of ordered visible features has changed
       */
      return true;
    }

    /*
     * return true if any feature colour has changed
     */
    for (String feature : visibleFeatures)
    {
      if (visibleColours.get(feature) != getFeatureStyle(feature))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * @param listener
   * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    changeSupport.addPropertyChangeListener(listener);
  }

  /**
   * @param listener
   * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    changeSupport.removePropertyChangeListener(listener);
  }

  public Set<String> getAllFeatureColours()
  {
    return featureColours.keySet();
  }

  public void clearRenderOrder()
  {
    renderOrder = null;
  }

  public boolean hasRenderOrder()
  {
    return renderOrder != null;
  }

  /**
   * Returns feature types in ordering of rendering, where last means on top
   */
  public List<String> getRenderOrder()
  {
    if (renderOrder == null)
    {
      return Arrays.asList(new String[] {});
    }
    return Arrays.asList(renderOrder);
  }

  public int getFeatureGroupsSize()
  {
    return featureGroups != null ? 0 : featureGroups.size();
  }

  @Override
  public List<String> getFeatureGroups()
  {
    // conflict between applet and desktop - featureGroups returns the map in
    // the desktop featureRenderer
    return (featureGroups == null) ? Arrays.asList(new String[0])
            : Arrays.asList(featureGroups.keySet().toArray(new String[0]));
  }

  public boolean checkGroupVisibility(String group,
          boolean newGroupsVisible)
  {
    if (featureGroups == null)
    {
      // then an exception happens next..
    }
    if (featureGroups.containsKey(group))
    {
      return featureGroups.get(group).booleanValue();
    }
    if (newGroupsVisible)
    {
      featureGroups.put(group, Boolean.valueOf(true));
      return true;
    }
    return false;
  }

  /**
   * get visible or invisible groups
   * 
   * @param visible
   *          true to return visible groups, false to return hidden ones.
   * @return list of groups
   */
  @Override
  public List<String> getGroups(boolean visible)
  {
    if (featureGroups != null)
    {
      List<String> gp = new ArrayList<>();

      for (String grp : featureGroups.keySet())
      {
        Boolean state = featureGroups.get(grp);
        if (state.booleanValue() == visible)
        {
          gp.add(grp);
        }
      }
      return gp;
    }
    return null;
  }

  @Override
  public void setGroupVisibility(String group, boolean visible)
  {
    featureGroups.put(group, Boolean.valueOf(visible));
  }

  @Override
  public void setGroupVisibility(List<String> toset, boolean visible)
  {
    if (toset != null && toset.size() > 0 && featureGroups != null)
    {
      boolean rdrw = false;
      for (String gst : toset)
      {
        Boolean st = featureGroups.get(gst);
        featureGroups.put(gst, Boolean.valueOf(visible));
        if (st != null)
        {
          rdrw = rdrw || (visible != st.booleanValue());
        }
      }
      if (rdrw)
      {
        // set local flag indicating redraw needed ?
      }
    }
  }

  @Override
  public Map<String, FeatureColourI> getDisplayedFeatureCols()
  {
    Map<String, FeatureColourI> fcols = new Hashtable<>();
    if (getViewport().getFeaturesDisplayed() == null)
    {
      return fcols;
    }
    Set<String> features = getViewport().getFeaturesDisplayed()
            .getVisibleFeatures();
    for (String feature : features)
    {
      fcols.put(feature, getFeatureStyle(feature));
    }
    return fcols;
  }

  @Override
  public FeaturesDisplayedI getFeaturesDisplayed()
  {
    return av.getFeaturesDisplayed();
  }

  /**
   * Returns a (possibly empty) list of visible feature types, in render order
   * (last is on top)
   */
  @Override
  public List<String> getDisplayedFeatureTypes()
  {
    List<String> typ = getRenderOrder();
    List<String> displayed = new ArrayList<>();
    FeaturesDisplayedI feature_disp = av.getFeaturesDisplayed();
    if (feature_disp != null)
    {
      synchronized (feature_disp)
      {
        for (String type : typ)
        {
          if (feature_disp.isVisible(type))
          {
            displayed.add(type);
          }
        }
      }
    }
    return displayed;
  }

  @Override
  public List<String> getDisplayedFeatureGroups()
  {
    List<String> _gps = new ArrayList<>();
    for (String gp : getFeatureGroups())
    {
      if (checkGroupVisibility(gp, false))
      {
        _gps.add(gp);
      }
    }
    return _gps;
  }

  /**
   * Answers true if the feature belongs to a feature group which is not
   * currently displayed, else false
   * 
   * @param sequenceFeature
   * @return
   */
  public boolean featureGroupNotShown(final SequenceFeature sequenceFeature)
  {
    return featureGroups != null && sequenceFeature.featureGroup != null
            && sequenceFeature.featureGroup.length() != 0
            && featureGroups.containsKey(sequenceFeature.featureGroup)
            && !featureGroups.get(sequenceFeature.featureGroup)
                    .booleanValue();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<SequenceFeature> findFeaturesAtResidue(SequenceI sequence,
          int fromResNo, int toResNo)
  {
    List<SequenceFeature> result = new ArrayList<>();
    //List<SequenceFeature> result = sequence.findFeatures(fromResNo, toResNo, "sequence_variant");
    List<SequenceFeature> tmp = sequence.getSequenceFeatures();
    for (SequenceFeature sf : tmp)
    {
      if ((sf.getBegin() >= fromResNo) && (sf.getEnd() <= toResNo))
      result.add(sf);
    }
    
    if (!av.areFeaturesDisplayed() || getFeaturesDisplayed() == null)
    {
      return result;
    }

    /*
     * include features at the position provided their feature type is 
     * displayed, and feature group is null or the empty string
     * or marked for display
     */
    List<String> visibleFeatures = getDisplayedFeatureTypes();
    String[] visibleTypes = visibleFeatures
            .toArray(new String[visibleFeatures.size()]);
    List<SequenceFeature> features = sequence.getFeatures()
            .findFeatures(fromResNo, toResNo, visibleTypes);

    for (SequenceFeature sf : features)
    {
      if (!featureGroupNotShown(sf) && getColour(sf) != null)
      {
        result.add(sf);
      }
    }
    return result;
  }

  /**
   * Removes from the list of features any whose group is not shown, or that are
   * visible and duplicate the location of a visible feature of the same type.
   * Should be used only for features of the same, simple, feature colour (which
   * normally implies the same feature type). No filtering is done if
   * transparency, or any feature filters, are in force.
   * 
   * @param features
   */
  public void filterFeaturesForDisplay(List<SequenceFeature> features)
  {
    /*
     * fudge: JalviewJS's IntervalStore lacks the sort method called :-(
     */
    if (Platform.isJS())
    {
      return;
    }

    /*
     * don't remove 'redundant' features if 
     * - transparency is applied (feature count affects depth of feature colour)
     * - filters are applied (not all features may be displayable)
     */
    if (features.isEmpty() || transparency != 1f
            || !featureFilters.isEmpty())
    {
      return;
    }

    SequenceFeatures.sortFeatures(features, true);
    SequenceFeature lastFeature = null;

    Iterator<SequenceFeature> it = features.iterator();
    while (it.hasNext())
    {
      SequenceFeature sf = it.next();
      if (featureGroupNotShown(sf))
      {
        it.remove();
        continue;
      }

      /*
       * a feature is redundant for rendering purposes if it has the
       * same extent as another (so would just redraw the same colour);
       * (checking type and isContactFeature as a fail-safe here, although
       * currently they are guaranteed to match in this context)
       */
      if (lastFeature != null && sf.getBegin() == lastFeature.getBegin()
              && sf.getEnd() == lastFeature.getEnd()
              && sf.isContactFeature() == lastFeature.isContactFeature()
              && sf.getType().equals(lastFeature.getType()))
      {
        it.remove();
      }
      lastFeature = sf;
    }
  }

  @Override
  public Map<String, FeatureMatcherSetI> getFeatureFilters()
  {
    return featureFilters;
  }

  @Override
  public void setFeatureFilters(Map<String, FeatureMatcherSetI> filters)
  {
    featureFilters = filters;
  }

  @Override
  public FeatureMatcherSetI getFeatureFilter(String featureType)
  {
    return featureFilters.get(featureType);
  }

  @Override
  public void setFeatureFilter(String featureType,
          FeatureMatcherSetI filter)
  {
    if (filter == null || filter.isEmpty())
    {
      featureFilters.remove(featureType);
    }
    else
    {
      featureFilters.put(featureType, filter);
    }
  }

  /**
   * Answers the colour for the feature, or null if the feature is excluded by
   * feature group visibility, by filters, or by colour threshold settings. This
   * method does not take feature type visibility into account.
   * 
   * @param sf
   * @param fc
   * @return
   */
  public Color getColor(SequenceFeature sf, FeatureColourI fc)
  {
    /*
     * is the feature group displayed?
     */
    if (featureGroupNotShown(sf))
    {
      return null;
    }

    /*
     * does the feature pass filters?
     */
    if (!featureMatchesFilters(sf))
    {
      return null;
    }

    return fc.getColor(sf);
  }

  /**
   * Answers true if there no are filters defined for the feature type, or this
   * feature matches the filters. Answers false if the feature fails to match
   * filters.
   * 
   * @param sf
   * @return
   */
  protected boolean featureMatchesFilters(SequenceFeature sf)
  {
    FeatureMatcherSetI filter = featureFilters.get(sf.getType());
    return filter == null ? true : filter.matches(sf);
  }

  /**
   * Answers true unless the specified group is set to hidden. Defaults to true
   * if group visibility is not set.
   * 
   * @param group
   * @return
   */
  public boolean isGroupVisible(String group)
  {
    if (!featureGroups.containsKey(group))
    {
      return true;
    }
    return featureGroups.get(group);
  }

  /**
   * Orders features in render precedence (last in order is last to render, so
   * displayed on top of other features)
   * 
   * @param order
   */
  public void orderFeatures(Comparator<String> order)
  {
    Arrays.sort(renderOrder, order);
  }

  @Override
  public MappedFeatures findComplementFeaturesAtResidue(
          final SequenceI sequence, final int pos)
  {
    SequenceI ds = sequence.getDatasetSequence();
    if (ds == null)
    {
      ds = sequence;
    }
    final char residue = ds.getCharAt(pos - ds.getStart());

    List<SequenceFeature> found = new ArrayList<>();
    List<AlignedCodonFrame> mappings = this.av.getAlignment()
            .getCodonFrame(sequence);

    /*
     * fudge: if no mapping found, check the complementary alignment
     * todo: only store in one place? StructureSelectionManager?
     */
    if (mappings.isEmpty())
    {
      mappings = this.av.getCodingComplement().getAlignment()
              .getCodonFrame(sequence);
    }

    /*
     * todo: direct lookup of CDS for peptide and vice-versa; for now,
     * have to search through an unordered list of mappings for a candidate
     */
    SequenceToSequenceMapping mapping = null;
    SequenceI mapFrom = null;

    for (AlignedCodonFrame acf : mappings)
    {
      mapping = acf.getCoveringCodonMapping(ds);
      if (mapping == null)
      {
        continue;
      }
      SearchResultsI sr = new SearchResults();
      mapping.markMappedRegion(ds, pos, sr);
      for (SearchResultMatchI match : sr.getResults())
      {
        int fromRes = match.getStart();
        int toRes = match.getEnd();
        mapFrom = match.getSequence();
        List<SequenceFeature> fs = findFeaturesAtResidue(mapFrom, fromRes,
                toRes);
        for (SequenceFeature sf : fs)
        {
          if (!found.contains(sf))
          {
            found.add(sf);
          }
        }
      }

      /*
       * just take the first mapped features we find
       */
      if (!found.isEmpty())
      {
        break;
      }
    }
    if (found.isEmpty())
    {
      return null;
    }

    /*
     * sort by renderorder (inefficiently but ok for small scale);
     * NB this sorts 'on top' feature to end, for rendering
     */
    List<SequenceFeature> result = new ArrayList<>();
    final int toAdd = found.size();
    int added = 0;
    renderOrder = new String[]{"sequence_variant"};
    for (String type : renderOrder)
    {
      for (SequenceFeature sf : found)
      {
        if (type.equals(sf.getType()))
        {
          result.add(sf);
          added++;
        }
        if (added == toAdd)
        {
          break;
        }
      }
    }

    return new MappedFeatures(mapping.getMapping(), mapFrom, pos, residue,
            result);
  }

  @Override
  public boolean isVisible(SequenceFeature feature)
  {
    if (feature == null)
    {
      return false;
    }
    if (getFeaturesDisplayed() == null
            || !getFeaturesDisplayed().isVisible(feature.getType()))
    {
      return false;
    }
    if (featureGroupNotShown(feature))
    {
      return false;
    }
    FeatureColourI fc = featureColours.get(feature.getType());
    if (fc != null && fc.isOutwithThreshold(feature))
    {
      return false;
    }
    if (!featureMatchesFilters(feature))
    {
      return false;
    }
    return true;
  }
}
