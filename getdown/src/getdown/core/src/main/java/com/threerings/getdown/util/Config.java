//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/threerings/getdown/blob/master/LICENSE

package com.threerings.getdown.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.threerings.getdown.Log.log;

/**
 * Handles parsing and runtime access for Getdown's config files (mainly {@code getdown.txt}).
 * These files contain zero or more mappings for a particular string key. Config values can be
 * fetched as single strings, lists of strings, or parsed into primitives or compound data types
 * like colors and rectangles.
 */
public class Config
{
    /** Empty configuration. */
    public static final Config EMPTY = new Config(new HashMap<String, Object>());

    /** Options that control the {@link #parsePairs} function. */
    public static class ParseOpts {
        // these should be tweaked as desired by the caller
        public boolean biasToKey = false;
        public boolean strictComments = false;

        // these are filled in by parseConfig
        public String osname = null;
        public String osarch = null;
    }

    /**
     * Creates a parse configuration, filling in the platform filters (or not) depending on the
     * value of {@code checkPlatform}.
     */
    public static ParseOpts createOpts (boolean checkPlatform) {
        ParseOpts opts = new ParseOpts();
        if (checkPlatform) {
            opts.osname = StringUtil.deNull(System.getProperty("os.name")).toLowerCase(Locale.ROOT);
            opts.osarch = StringUtil.deNull(System.getProperty("os.arch")).toLowerCase(Locale.ROOT);
        }
        return opts;
    }

    /**
     * Parses a configuration file containing key/value pairs. The file must be in the UTF-8
     * encoding.
     *
     * @param opts options that influence the parsing. See {@link #createOpts}.
     *
     * @return a list of <code>String[]</code> instances containing the key/value pairs in the
     * order they were parsed from the file.
     */
    public static List<String[]> parsePairs (File source, ParseOpts opts)
        throws IOException
    {
        // annoyingly FileReader does not allow encoding to be specified (uses platform default)
        try (FileInputStream fis = new FileInputStream(source);
             InputStreamReader input = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
            return parsePairs(input, opts);
        }
    }

    /**
     * See {@link #parsePairs(File,ParseOpts)}.
     */
    public static List<String[]> parsePairs (Reader source, ParseOpts opts) throws IOException
    {
        List<String[]> pairs = new ArrayList<>();
        for (String line : FileUtil.readLines(source)) {
            // nix comments
            int cidx = line.indexOf("#");
            if (opts.strictComments ? cidx == 0 : cidx != -1) {
                line = line.substring(0, cidx);
            }

            // trim whitespace and skip blank lines
            line = line.trim();
            if (StringUtil.isBlank(line)) {
                continue;
            }

            // parse our key/value pair
            String[] pair = new String[2];
            // if we're biasing toward key, put all the extra = in the key rather than the value
            int eidx = opts.biasToKey ? line.lastIndexOf("=") : line.indexOf("=");
            if (eidx != -1) {
                pair[0] = line.substring(0, eidx).trim();
                pair[1] = line.substring(eidx+1).trim();
            } else {
                pair[0] = line;
                pair[1] = "";
            }

            // if the pair has an os qualifier, we need to process it
            if (pair[1].startsWith("[")) {
                int qidx = pair[1].indexOf("]");
                if (qidx == -1) {
                    log.warning("Bogus platform specifier", "key", pair[0], "value", pair[1]);
                    continue; // omit the pair entirely
                }
                // if we're checking qualifiers and the os doesn't match this qualifier, skip it
                String quals = pair[1].substring(1, qidx);
                if (opts.osname != null && !checkQualifiers(quals, opts.osname, opts.osarch)) {
                    log.debug("Skipping", "quals", quals,
                              "osname", opts.osname, "osarch", opts.osarch,
                              "key", pair[0], "value", pair[1]);
                    continue;
                }
                // otherwise filter out the qualifier text
                pair[1] = pair[1].substring(qidx+1).trim();
            }

            pairs.add(pair);
        }

        return pairs;
    }

    /**
     * Takes a comma-separated String of four integers and returns a rectangle using those ints as
     * the its x, y, width, and height.
     */
    public static Rectangle parseRect (String name, String value)
    {
        if (!StringUtil.isBlank(value)) {
            int[] v = StringUtil.parseIntArray(value);
            if (v != null && v.length == 4) {
                return new Rectangle(v[0], v[1], v[2], v[3]);
            }
            log.warning("Ignoring invalid rect '" + name + "' config '" + value + "'.");
        }
        return null;
    }

    /**
     * Parses the given hex color value (e.g. FFCC99) and returns an {@code Integer} with that
     * value. If the given value is null or not a valid hexadecimal number, this will return null.
     */
    public static Integer parseColor (String hexValue)
    {
        if (!StringUtil.isBlank(hexValue)) {
            try {
                // if no alpha channel is specified, use 255 (full alpha)
                int alpha = hexValue.length() > 6 ? 0 : 0xFF000000;
                return Integer.parseInt(hexValue, 16) | alpha;
            } catch (NumberFormatException e) {
                log.warning("Ignoring invalid color", "hexValue", hexValue, "exception", e);
            }
        }
        return null;
    }

    /**
     * Parses a configuration file containing key/value pairs. The file must be in the UTF-8
     * encoding.
     *
     * @return a map from keys to values, where a value will be an array of strings if more than
     * one key/value pair in the config file was associated with the same key.
     */
    public static Config parseConfig (File source, ParseOpts opts)
        throws IOException
    {
        Map<String, Object> data = new HashMap<>();

        // I thought that we could use HashMap<String, String[]> and put new String[] {pair[1]} for
        // the null case, but it mysteriously dies on launch, so leaving it as HashMap<String,
        // Object> for now
        for (String[] pair : parsePairs(source, opts)) {
            Object value = data.get(pair[0]);
            if (value == null) {
                data.put(pair[0], pair[1]);
            } else if (value instanceof String) {
                data.put(pair[0], new String[] { (String)value, pair[1] });
            } else if (value instanceof String[]) {
                String[] values = (String[])value;
                String[] nvalues = new String[values.length+1];
                System.arraycopy(values, 0, nvalues, 0, values.length);
                nvalues[values.length] = pair[1];
                data.put(pair[0], nvalues);
            }
        }

        // special magic for the getdown.txt config: if the parsed data contains 'strict_comments =
        // true' then we reparse the file with strict comments (i.e. # is only assumed to start a
        // comment in column 0)
        if (!opts.strictComments && Boolean.parseBoolean((String)data.get("strict_comments"))) {
            opts.strictComments = true;
            return parseConfig(source, opts);
        }

        return new Config(data);
    }

    public Config (Map<String,  Object> data) {
        _data = data;
    }

    /**
     * Returns whether {@code name} has a value in this config.
     */
    public boolean hasValue (String name) {
        return _data.containsKey(name);
    }

    /**
     * Returns the raw-value for {@code name}. This may be a {@code String}, {@code String[]}, or
     * {@code null}.
     */
    public Object getRaw (String name) {
        return _data.get(name);
    }

    /**
     * Returns the specified config value as a string, or {@code null}.
     */
    public String getString (String name) {
        return (String)_data.get(name);
    }

    /**
     * Returns the specified config value as a string, or {@code def}.
     */
    public String getString (String name, String def) {
        String value = (String)_data.get(name);
        return value == null ? def : value;
    }

    /**
     * Returns the specified config value as a boolean.
     */
    public boolean getBoolean (String name) {
        return Boolean.parseBoolean(getString(name));
    }

    /**
     * Massages a single string into an array and leaves existing array values as is. Simplifies
     * access to parameters that are expected to be arrays.
     */
    public String[] getMultiValue (String name)
    {
        Object value = _data.get(name);
        if (value == null) {
          return new String[] {};
        }
        if (value instanceof String) {
            return new String[] { (String)value };
        } else {
            return (String[])value;
        }
    }

    /** Used to parse rectangle specifications from the config file. */
    public Rectangle getRect (String name, Rectangle def)
    {
        String value = getString(name);
        Rectangle rect = parseRect(name, value);
        return (rect == null) ? def : rect;
    }

    /**
     * Parses and returns the config value for {@code name} as an int. If no value is provided,
     * {@code def} is returned. If the value is invalid, a warning is logged and {@code def} is
     * returned.
     */
    public int getInt (String name, int def) {
        String value = getString(name);
        try {
            return value == null ? def : Integer.parseInt(value);
        } catch (Exception e) {
            log.warning("Ignoring invalid int '" + name + "' config '" + value + "',");
            return def;
        }
    }

    /**
     * Parses and returns the config value for {@code name} as a long. If no value is provided,
     * {@code def} is returned. If the value is invalid, a warning is logged and {@code def} is
     * returned.
     */
    public long getLong (String name, long def) {
        String value = getString(name);
        try {
            return value == null ? def : Long.parseLong(value);
        } catch (Exception e) {
            log.warning("Ignoring invalid long '" + name + "' config '" + value + "',");
            return def;
        }
    }

    /** Used to parse color specifications from the config file. */
    public int getColor (String name, int def)
    {
        String value = getString(name);
        Integer color = parseColor(value);
        return (color == null) ? def : color;
    }

    /** Parses a list of strings from the config file. */
    public String[] getList (String name)
    {
        String value = getString(name);
        return (value == null) ? new String[0] : StringUtil.parseStringArray(value);
    }

    /**
     * Parses a URL from the config file, checking first for a localized version.
     */
    public String getUrl (String name, String def)
    {
        String value = getString(name + "." + Locale.getDefault().getLanguage());
        if (StringUtil.isBlank(value)) {
            value = getString(name);
        }
        if (StringUtil.isBlank(value)) {
            value = def;
        }
        if (!StringUtil.isBlank(value)) {
            try {
                HostWhitelist.verify(new URL(value));
            } catch (MalformedURLException e) {
                log.warning("Invalid URL.", "url", value, e);
                value = null;
            }
        }
        return value;
    }

    /**
     * A helper function for {@link #parsePairs(Reader,ParseOpts)}. Qualifiers have the following
     * form:
     * <pre>
     * id = os[-arch]
     * ids = id | id,ids
     * quals = !id | ids
     * </pre>
     * Examples: [linux-amd64,linux-x86_64], [windows], [mac os x], [!windows]. Negative qualifiers
     * must appear alone, they cannot be used with other qualifiers (positive or negative).
     */
    protected static boolean checkQualifiers (String quals, String osname, String osarch)
    {
        if (quals.startsWith("!")) {
            if (quals.indexOf(",") != -1) { // sanity check
                log.warning("Multiple qualifiers cannot be used when one of the qualifiers " +
                            "is negative", "quals", quals);
                return false;
            }
            return !checkQualifier(quals.substring(1), osname, osarch);
        }
        for (String qual : quals.split(",")) {
            if (checkQualifier(qual, osname, osarch)) {
                return true; // if we have a positive match, we can immediately return true
            }
        }
        return false; // we had no positive matches, so return false
    }

    /** A helper function for {@link #checkQualifiers}. */
    protected static boolean checkQualifier (String qual, String osname, String osarch)
    {
        String[] bits = qual.trim().toLowerCase(Locale.ROOT).split("-");
        String os = bits[0], arch = (bits.length > 1) ? bits[1] : "";
        return (osname.indexOf(os) != -1) && (osarch.indexOf(arch) != -1);
    }
    
    public void mergeConfig(Config newValues, boolean merge) {
      
      for (Map.Entry<String, Object> entry : newValues.getData().entrySet()) {
        
        String key = entry.getKey();
        Object nvalue = entry.getValue();

        String mkey = key.indexOf('.') > -1 ? key.substring(key.indexOf('.') + 1) : key;
        if (merge && allowedMergeKeys.contains(mkey)) {
          
          // merge multi values
          
          Object value = _data.get(key);
          
          if (value == null) {
            _data.put(key, nvalue);
          } else if (value instanceof String) {
            if (nvalue instanceof String) {
              
              // value is String, nvalue is String
              _data.put(key, new String[] { (String)value, (String)nvalue });
              
            } else if (nvalue instanceof String[]) {
              
              // value is String, nvalue is String[]
              String[] nvalues = (String[])nvalue;
              String[] newvalues = new String[nvalues.length+1];
              newvalues[0] = (String)value;
              System.arraycopy(nvalues, 0, newvalues, 1, nvalues.length);
              _data.put(key, newvalues);
              
            }
          } else if (value instanceof String[]) {
            if (nvalue instanceof String) {
              
              // value is String[], nvalue is String
              String[] values = (String[])value;
              String[] newvalues = new String[values.length+1];
              System.arraycopy(values, 0, newvalues, 0, values.length);
              newvalues[values.length] = (String)nvalue;
              _data.put(key, newvalues);
              
            } else if (nvalue instanceof String[]) {
              
              // value is String[], nvalue is String[]
              String[] values = (String[])value;
              String[] nvalues = (String[])nvalue;
              String[] newvalues = new String[values.length + nvalues.length];
              System.arraycopy(values, 0, newvalues, 0, values.length);
              System.arraycopy(nvalues, 0, newvalues, values.length, newvalues.length);
              _data.put(key, newvalues);
              
            }
          }
          
        } else if (allowedReplaceKeys.contains(mkey)){
          
          // replace value
          _data.put(key, nvalue);
          
        } else {
          log.warning("Not merging key '"+key+"' into config");
        }

      }
      
    }
    
    public String toString() {
      StringBuilder sb = new StringBuilder();
      for (Map.Entry<String, Object> entry : getData().entrySet()) {
        String key = entry.getKey();
        Object val = entry.getValue();
        sb.append(key);
        sb.append("=");
        if (val instanceof String) {
          sb.append((String)val);
        } else if (val instanceof String[]) {
          sb.append(Arrays.toString((String[])val));
        } else {
          sb.append("Value not String or String[]");
        }
        sb.append("\n");
      }
      return sb.toString();
    }
    
    public Map<String, Object> getData() {
      return _data;
    }

    private final Map<String, Object> _data;
 
    public static final List<String> allowedReplaceKeys = Arrays.asList("appbase","apparg","jvmarg","jvmmempc","jvmmemmax"); // these are the ones we might use
    public static final List<String> allowedMergeKeys = Arrays.asList("apparg","jvmarg"); // these are the ones we might use
    //private final List<String> allowedMergeKeys = Arrays.asList("apparg","jvmarg","resource","code","java_location"); // (not exhaustive list here)
}
