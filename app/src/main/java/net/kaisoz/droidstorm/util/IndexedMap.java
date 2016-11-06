package net.kaisoz.droidstorm.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Indexed HashMap with extra useful data manipulation methods
 *
 * @author Tomás Tormo Franco
 */
public class IndexedMap extends HashMap {

    private List<Object> keyIndex;

    public IndexedMap() {
        keyIndex = new ArrayList<Object>();
    }

    @Override
    public Object put(Object key, Object value) {
        addKeyToIndex(key);
        return super.put(key, value);
    }

    @Override
    public void putAll(Map source) {
        for (Object key : source.keySet()) {
            addKeyToIndex(key);
        }
        super.putAll(source);
    }

    @Override
    public Object remove(Object key) {
        keyIndex.remove(key);
        return super.remove(key);
    }

    /**
     * Returns the value at the specified position in this Map's keyIndex.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size())
     */
    public Object getValue(int index) {
        return super.get(keyIndex.get(index));
    }

    /**
     * Returns the value at the specified position in this Map's keyIndex.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size())
     */
    public Object getKey(int index) {
        return keyIndex.get(index);
    }

    /**
     * Replaces the key of the map at the specified position
     *
     * @param position position whose key will be replaced
     * @param newKey
     */
    public void replaceKey(int position, Object newKey) {
        Object key = keyIndex.get(position);
        Object value = super.get(key);
        keyIndex.set(position, newKey);
        super.remove(key);
        super.put(newKey, value);
    }

    /**
     * Puts a value in the given position
     *
     * @param index
     * @param value
     */
    public void put(int index, Object value) {
        super.put(keyIndex.get(index), value);
    }

    /**
     * Adds a new key to the keys index
     *
     * @param key
     */
    private void addKeyToIndex(Object key) {

        if (!keyIndex.contains(key)) {
            keyIndex.add(key);
        }
    }

    /**
     * Returns all keys of this map
     *
     * @return
     */
    public Object[] getAllKeys() {
        return keyIndex.toArray();
    }

    /**
     * Returns all values of this map
     *
     * @return
     */
    public Object[] getAllValues() {
        return super.values().toArray();
    }

    /**
     * Removes the pair situated in the given index
     *
     * @param index
     * @return
     */
    public boolean removeValue(int index) {
        Object key = keyIndex.get(index);
        keyIndex.remove(key);
        super.remove(key);
        return true;
    }


    /**
     * Returns a submap of this map which contains all pairs with the given keys
     *
     * @param keys
     * @return
     */
    public IndexedMap getSubMapFromKeys(Object[] keys) {
        IndexedMap submap = new IndexedMap();
        for (int i = 0; i < keys.length; i++) {
            submap.put(keys[i], super.get(keys[i]));
        }

        return submap;
    }

    /**
     * Returns a submap of this map which contains all pairs with the given values
     *
     * @param keys
     * @return
     */
    public IndexedMap getSubMapFromValues(Object[] values) {
        Object[] keys = getKeysFromValues(values);
        return getSubMapFromKeys(keys);
    }

    /**
     * Returns all keys of the given values
     *
     * @param keys
     * @return
     */
    public Object[] getKeysFromValues(Object[] values) {
        List keys = new ArrayList();
        for (int i = 0; i < values.length; i++) {
            for (Object o : super.keySet()) {
                if (super.get(o).equals(values[i])) {
                    keys.add(o);
                    break;
                }
            }
        }
        return keys.toArray(new Object[keys.size()]);
    }


    /**
     * Removes all paris that contains the given keys
     *
     * @param keys
     * @return
     */
    public boolean removeByKeys(Object[] keys) {
        keyIndex.removeAll(Arrays.asList(keys));
        for (int i = 0; i < keys.length; i++) {
            keyIndex.remove(keys[i]);
            super.remove(keys[i]);
        }
        return true;
    }

    /**
     * Removes all paris that contains the given values
     *
     * @param values
     * @return
     */
    public boolean removeByValues(Object[] values) {
        Object[] keys = getKeysFromValues(values);
        return removeByKeys(keys);
    }

    @Override
    public void clear() {
        super.clear();
        keyIndex.clear();
    }

}