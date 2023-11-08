package jalview.util;

import java.util.Map;

public class MapUtils
{
  /*
   * Return the value of the first key that exists in the map
   */
  public static <K, V> V getFirst(Map<K, V> map, K... keys)
  {
    return getFirst(false, map, keys);
  }

  public static <K, V> V getFirst(boolean nonNull, Map<K, V> map, K... keys)
  {
    for (K key : keys)
    {
      if (map.containsKey(key))
      {
        if (!(nonNull && (map.get(key) == null)))
        {
          return map.get(key);
        }
        else if (!nonNull)
        {
          return map.get(key);
        }
      }
    }
    return null;
  }

  public static <K> boolean containsAKey(Map<K, ?> map, K... keys)
  {
    for (K key : keys)
    {
      if (map.containsKey(key))
        return true;
    }
    return false;
  }
}
