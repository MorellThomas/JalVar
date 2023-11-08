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
package jalview.structure;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A class to model a set of models, chains and atom range positions
 * 
 */
public class AtomSpecModel
{
  /*
   * { modelId, {chainCode, List<from-to> ranges} }
   */
  private Map<String, Map<String, BitSet>> atomSpec;

  /**
   * Constructor
   */
  public AtomSpecModel()
  {
    atomSpec = new TreeMap<>();
  }

  /**
   * Adds one contiguous range to this atom spec
   * 
   * @param model
   * @param startPos
   * @param endPos
   * @param chain
   */
  public void addRange(String model, int startPos, int endPos, String chain)
  {
    /*
     * Get/initialize map of data for the colour and model
     */
    Map<String, BitSet> modelData = atomSpec.get(model);
    if (modelData == null)
    {
      atomSpec.put(model, modelData = new TreeMap<>());
    }

    /*
     * Get/initialize map of data for colour, model and chain
     */
    BitSet chainData = modelData.get(chain);
    if (chainData == null)
    {
      chainData = new BitSet();
      modelData.put(chain, chainData);
    }

    /*
     * Add the start/end positions
     */
    chainData.set(startPos, endPos + 1);
  }

  public Iterable<String> getModels()
  {
    return atomSpec.keySet();
  }

  public int getModelCount()
  {
    return atomSpec.size();
  }

  public Iterable<String> getChains(String model)
  {
    return atomSpec.containsKey(model) ? atomSpec.get(model).keySet()
            : null;
  }

  /**
   * Returns a (possibly empty) ordered list of contiguous atom ranges for the
   * given model and chain.
   * 
   * @param model
   * @param chain
   * @return
   */
  public List<int[]> getRanges(String model, String chain)
  {
    List<int[]> ranges = new ArrayList<>();
    if (atomSpec.containsKey(model))
    {
      BitSet bs = atomSpec.get(model).get(chain);
      int start = 0;
      if (bs != null)
      {
        start = bs.nextSetBit(start);
        int end = 0;
        while (start != -1)
        {
          end = bs.nextClearBit(start);
          ranges.add(new int[] { start, end - 1 });
          start = bs.nextSetBit(end);
        }
      }
    }
    return ranges;
  }
}
