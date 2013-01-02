/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.gecko.sync;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Extend JSONObject to do little things, like, y'know, accessing members.
 *
 * @author rnewman
 *
 */
public class ExtendedJSONObject {

  public JSONObject object;

  /**
   * Helper method to get a JSON array from a stream.
   *
   * @param jsonString input.
   * @throws ParseException
   * @throws IOException
   * @throws NonArrayJSONException if the object is valid JSON, but not an array.
   */
  public static JSONArray parseJSONArray(Reader in)
      throws IOException, ParseException, NonArrayJSONException {
    Object o = new JSONParser().parse(in);

    if (o == null) {
      return null;
    }

    if (o instanceof JSONArray) {
      return (JSONArray) o;
    }

    throw new NonArrayJSONException(o);
  }

  /**
   * Helper method to get a JSON array from a string.
   * <p>
   * You should prefer the stream interface {@link #parseJSONArray(Reader)}.
   *
   * @param jsonString input.
   * @throws ParseException
   * @throws IOException
   * @throws NonArrayJSONException if the object is valid JSON, but not an array.
   */
  public static JSONArray parseJSONArray(String jsonString)
      throws IOException, ParseException, NonArrayJSONException {
    Object o = new JSONParser().parse(jsonString);

    if (o == null) {
      return null;
    }

    if (o instanceof JSONArray) {
      return (JSONArray) o;
    }

    throw new NonArrayJSONException(o);
  }

  /**
   * Helper method to get a JSON object from a stream.
   *
   * @param jsonString input.
   * @throws ParseException
   * @throws IOException
   * @throws NonArrayJSONException if the object is valid JSON, but not an object.
   */
  public static ExtendedJSONObject parseJSONObject(Reader in)
      throws IOException, ParseException, NonObjectJSONException {
    return new ExtendedJSONObject(in);
  }

  /**
   * Helper method to get a JSON object from a string.
   * <p>
   * You should prefer the stream interface {@link #parseJSONObject(Reader)}.
   *
   * @param jsonString input.
   * @throws ParseException
   * @throws IOException
   * @throws NonObjectJSONException if the object is valid JSON, but not an object.
   */
  public static ExtendedJSONObject parseJSONObject(String jsonString)
      throws IOException, ParseException, NonObjectJSONException {
    return new ExtendedJSONObject(jsonString);
  }

  /**
   * Helper method to get a JSON object from a UTF-8 byte array.
   *
   * @param in UTF-8 bytes.
   * @throws ParseException
   * @throws NonObjectJSONException if the object is valid JSON, but not an object.
   * @throws IOException
   */
  public static ExtendedJSONObject parseUTF8AsJSONObject(byte[] in)
      throws ParseException, NonObjectJSONException, IOException {
    return parseJSONObject(new String(in, "UTF-8"));
  }

  public ExtendedJSONObject() {
    this.object = new JSONObject();
  }

  public ExtendedJSONObject(JSONObject o) {
    this.object = o;
  }

  public ExtendedJSONObject(Reader in) throws IOException, ParseException, NonObjectJSONException {
    if (in == null) {
      this.object = new JSONObject();
      return;
    }

    Object obj = new JSONParser().parse(in);
    if (obj instanceof JSONObject) {
      this.object = ((JSONObject) obj);
    } else {
      throw new NonObjectJSONException(obj);
    }
  }

  public ExtendedJSONObject(String jsonString) throws IOException, ParseException, NonObjectJSONException {
    this(jsonString == null ? null : new StringReader(jsonString));
  }

  // Passthrough methods.
  public Object get(String key) {
    return this.object.get(key);
  }
  public Long getLong(String key) {
    return (Long) this.get(key);
  }
  public String getString(String key) {
    return (String) this.get(key);
  }

  /**
   * Return an Integer if the value for this key is an Integer, Long, or String
   * that can be parsed as a base 10 Integer.
   * Passes through null.
   *
   * @throws NumberFormatException
   */
  public Integer getIntegerSafely(String key) throws NumberFormatException {
    Object val = this.object.get(key);
    if (val == null) {
      return null;
    }
    if (val instanceof Integer) {
      return (Integer) val;
    }
    if (val instanceof Long) {
      return Integer.valueOf(((Long) val).intValue());
    }
    if (val instanceof String) {
      return Integer.parseInt((String) val, 10);
    }
    throw new NumberFormatException("Expecting Integer, got " + val.getClass());
  }

  /**
   * Return a server timestamp value as milliseconds since epoch.
   *
   * @param key
   * @return A Long, or null if the value is non-numeric or doesn't exist.
   */
  public Long getTimestamp(String key) {
    Object val = this.object.get(key);

    // This is absurd.
    if (val instanceof Double) {
      double millis = ((Double) val).doubleValue() * 1000;
      return Double.valueOf(millis).longValue();
    }
    if (val instanceof Float) {
      double millis = ((Float) val).doubleValue() * 1000;
      return Double.valueOf(millis).longValue();
    }
    if (val instanceof Number) {
      // Must be an integral number.
      return ((Number) val).longValue() * 1000;
    }

    return null;
  }

  public boolean containsKey(String key) {
    return this.object.containsKey(key);
  }

  public String toJSONString() {
    return this.object.toJSONString();
  }

  public String toString() {
    return this.object.toString();
  }

  public void put(String key, Object value) {
    @SuppressWarnings("unchecked")
    Map<Object, Object> map = this.object;
    map.put(key, value);
  }

  /**
   * Remove key-value pair from JSONObject.
   *
   * @param key
   *          to be removed.
   * @return true if key exists and was removed, false otherwise.
   */
  public boolean remove(String key) {
    Object res = this.object.remove(key);
    return (res != null);
  }

  public ExtendedJSONObject getObject(String key) throws NonObjectJSONException {
    Object o = this.object.get(key);
    if (o == null) {
      return null;
    }
    if (o instanceof ExtendedJSONObject) {
      return (ExtendedJSONObject) o;
    }
    if (o instanceof JSONObject) {
      return new ExtendedJSONObject((JSONObject) o);
    }
    throw new NonObjectJSONException(o);
  }

  @SuppressWarnings("unchecked")
  public Iterable<Entry<String, Object>> entryIterable() {
    return this.object.entrySet();
  }

  @SuppressWarnings("unchecked")
  public Set<String> keySet() {
    return this.object.keySet();
  }

  public org.json.simple.JSONArray getArray(String key) throws NonArrayJSONException {
    Object o = this.object.get(key);
    if (o == null) {
      return null;
    }
    if (o instanceof JSONArray) {
      return (JSONArray) o;
    }
    throw new NonArrayJSONException(o);
  }

  public int size() {
    return this.object.size();
  }
}
