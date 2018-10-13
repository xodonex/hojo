// Copyright 1998,1999,2000,2001,2018, Henrik Lauritzen.
/*
    This file is part of the Hojo interpreter & toolkit.

    The Hojo interpreter & toolkit is free software: you can redistribute it
    and/or modify it under the terms of the GNU Affero General Public License
    as published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    The Hojo interpreter & toolkit is distributed in the hope that it will
    be useful or (at least have historical interest),
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this file.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.xodonex.util.struct;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.xodonex.util.ArrayUtils;

/**
 * A Map with immutable bindings.
 */
public class ImmutableMap implements Map, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * A canonical representation of the emtpy map.
     */
    public final static ImmutableMap EMPTY = new ImmutableMap();

    /**
     * A Comparator for ImmutableEntry objects based on the hash codes of the
     * key values. The ordering is thus inconsistent with .equals()
     */
    public final static Comparator ENTRY_COMPARATOR = new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
            int k1 = ((ImmutableEntry)o1).getKeyCode();
            int k2 = ((ImmutableEntry)o2).getKeyCode();

            return k1 < k2 ? -1 : k1 > k2 ? 1 : 0;
        }
    };

    /**
     * Build a sorted immutable entry list from a collection of keys and
     * matching values which override a map.
     *
     * @param base
     *            an optional Map instance, providing input bindings to be
     *            extracted
     * @param keys
     *            collection of keys to be added
     * @param values
     *            collection of values to be added. Iteration order determines
     *            which key is bound to which value
     * @return the resulting sequence of immutable entries
     * @throws IllegalArgumentException
     *             if the keys and values have different sizes
     */
    public static ImmutableEntry[] buildEntries(Map base,
            Collection keys, Collection values)
            throws IllegalArgumentException {
        if (keys.size() != values.size()) {
            throw new IllegalArgumentException("" + keys.size() + " and " +
                    values.size() + " values");
        }

        SortedSet s = new TreeSet(ENTRY_COMPARATOR);
        HashSet check = new HashSet();
        Iterator i1 = keys.iterator();
        Iterator i2 = values.iterator();

        for (int i = keys.size(); i > 0; i--) {
            Object key = i1.next();
            Object value = i2.next();

            if (check.add(key)) {
                s.add(new ImmutableEntry(key, value));
            }
        }

        if (base != null) {
            for (Iterator i = base.keySet().iterator(); i.hasNext();) {
                Entry e = (Entry)i.next();
                Object key = e.getKey();

                if (check.add(key)) {
                    s.add(new ImmutableEntry(key, e.getValue()));
                }
            }
        }

        return (ImmutableEntry[])s.toArray(new ImmutableEntry[s.size()]);
    }

    /**
     * Build a sorted entry list from a general map.
     *
     * @param m
     *            a map containing the entries to be extracted
     * @return the resulting sequence of immutable entries
     */
    public static ImmutableEntry[] buildEntries(Map m) {
        ImmutableEntry[] entries = new ImmutableEntry[m.size()];
        int j = 0;

        for (Iterator i = m.entrySet().iterator(); i.hasNext();) {
            Entry e = (Entry)i.next();
            entries[j++] = new ImmutableEntry(e.getKey(), e.getValue());
        }

        Arrays.sort(entries, ENTRY_COMPARATOR);
        return entries;
    }

    /**
     * Create a sorted entry list which overrides the original with the given
     * key and value.
     * 
     * @param entries
     *            the entries to be updated
     * @param key
     *            the key to be inserted/overridden
     * @param value
     *            the value to be inserted/overridden
     * @return the original entries if the binding was already present,
     *         otherwise a new copy is made with the updated binding
     */
    public static ImmutableEntry[] overrideEntries(ImmutableEntry[] entries,
            Object key, Object value) {

        int idx = findKey(key, entries);
        if (idx >= 0) {
            if (entries[idx].valueEquals(value)) {
                // no change
                return entries;
            }
            else {
                // clone and substitute the entry (and reuse the old key)
                ImmutableEntry[] result = entries.clone();
                result[idx] = new ImmutableEntry(entries[idx].getKey(), value);
                return result;
            }
        }
        else {
            // insert a new entry at the given point
            ImmutableEntry e = new ImmutableEntry(key, value);
            return (ImmutableEntry[])ArrayUtils.insert(entries, -idx - 1, e);
        }
    }

    /**
     * Create a sorted entry list which overrides the original sorted entry list
     * with the bindings in the given map.
     *
     * @param entries
     *            the entries to be updated
     * @param override
     *            the bindings to be inserted/overridden
     * @return the overridden entries.
     */
    public static ImmutableEntry[] overrideEntries(ImmutableEntry[] entries,
            Map override) {
        List l = new ArrayList(Arrays.asList(entries));

        // run through the new entries and substitute or add new entries
        // as necessary
        for (Iterator i = override.keySet().iterator(); i.hasNext();) {
            Entry e = (Entry)i.next();
            Object k = e.getKey(), v = e.getValue();

            int idx = findKey(k, entries);
            if (idx >= 0) {
                // the mapping exists
                if (!entries[idx].valueEquals(v)) {
                    // override the mapping (and reuse the old key instance)
                    l.set(idx, new ImmutableEntry(entries[idx].getKey(), v));
                }
            }
            else {
                // add a new binding
                l.add(new ImmutableEntry(k, v));
            }
        }

        ImmutableEntry[] result = (ImmutableEntry[])l.toArray(
                new ImmutableEntry[l.size()]);
        Arrays.sort(result, ENTRY_COMPARATOR);
        return result;
    }

    /**
     * Return the index into the entries at which the given key is found, or the
     * negative value of the <i>insertion point</i> if no such index exists. The
     * entries are assumed to be sorted in ascending order according to the hash
     * code of the keys.
     *
     * @param key
     *            the key to look up
     * @param entries
     *            the entries to be searched
     * @return the index of the key, if found, or the negative value of the
     *         insertion point if not found
     */
    public static int findKey(Object key, ImmutableEntry[] entries) {
        int hcode = key == null ? 0 : key.hashCode();

        // use binary search to locate an entry containing a key having
        // the given hash code (hcode)
        int min = 0, max = entries.length - 1, idx;
        while (true) {
            if (min > max) {
                // not found - return the insertion point
                return -(min + 1);
            }

            // divide the range
            idx = (min + max) / 2;
            int hcode2 = entries[idx].getKeyCode();

            // select the appropriate interval
            if (hcode2 < hcode) {
                // use the upper half
                min = idx + 1;
            }
            else if (hcode2 > hcode) {
                // use the lower half
                max = idx - 1;
            }
            else {
                // the value matches. idx already contains the correct value.
                // Set min = idx in order to use make the insertion point
                // later, if required
                min = idx;
                break;
            }
        }

        // idx now contains a key having hash code hcode. Check whether this
        // is the desired one
        if (entries[idx].keyEquals(key)) {
            return idx;
        }

        // check backwards through entries having the given hash code
        for (int i = idx - 1; i >= 0; i--) {
            if (entries[i].getKeyCode() != hcode) {
                break;
            }
            else if (entries[i].keyEquals(key)) {
                return i;
            }
        }

        // check forwards through entries having the given hash code
        for (int i = idx + 1; i < entries.length; i++) {
            if (entries[i].getKeyCode() != hcode) {
                break;
            }
            else if (entries[i].keyEquals(key)) {
                return i;
            }
        }

        // the key does not exist. Return the insertion point
        return -(min + 1);
    }

    /**
     * The entries of this map
     */
    protected ImmutableEntry[] _entries;

    /**
     * Create a new, immutable map containing the bindings in the given map.
     *
     * @param m
     *            the map whose bindings should be used
     */
    public ImmutableMap(Map m) {
        _entries = buildEntries(m);
    }

    /**
     * Create a new, immutable map containing the bindings in the given map
     * overridden by the extra bindings.
     *
     * @param m
     *            an optional map of bindings to be used
     * @param keys
     *            the keys to be added
     * @param values
     *            the values corresponding to the keys
     */
    public ImmutableMap(Map m, Collection keys, Collection values) {
        _entries = buildEntries(m, keys, values);
    }

    protected ImmutableMap(ImmutableEntry[] entries, boolean sorted) {
        _entries = entries;
        if (!sorted) {
            Arrays.sort(_entries, ENTRY_COMPARATOR);
        }
    }

    public ImmutableMap() {
        _entries = new ImmutableEntry[0];
    }

    /**
     * This method is not supported!
     */
    @Override
    public void clear() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    // Runs in O(log n) time for a map of size n
    @Override
    public boolean containsKey(Object obj) {
        return findKey(obj, _entries) >= 0;
    }

    // Runs in O(n) time for a map of size n
    @Override
    public boolean containsValue(Object obj) {
        for (int i = _entries.length - 1; i >= 0;) {
            if (_entries[i--].valueEquals(obj)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set entrySet() {
        return new AbstractSet() {
            @Override
            public Iterator iterator() {
                return new ImmutableArrayIterator(_entries);
            }

            @Override
            public int size() {
                return _entries.length;
            }
        };
    }

    // Worst case running time is O(n * log n) when comparing maps of size n
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ImmutableMap)) {
            return false;
        }
        ImmutableMap imm = (ImmutableMap)obj;

        if (_entries.length != imm.size()) {
            return false;
        }

        for (Iterator i = imm.keySet().iterator(); i.hasNext();) {
            Entry e = (Entry)i.next();
            int idx = findKey(e.getKey(), _entries);
            if (idx < 0 || !_entries[idx].valueEquals(e.getValue())) {
                return false;
            }
        }

        return true;
    }

    // Runs in O(log n) time for a map of size n
    @Override
    public Object get(Object obj) {
        int idx = findKey(obj, _entries);
        return (idx >= 0) ? _entries[idx].getValue() : null;
    }

    @Override
    public synchronized int hashCode() {
        int hashCode = 1;

        for (int i = 0; i < _entries.length; i++) {
            hashCode = 31 * hashCode + _entries[i].hashCode();
        }

        return hashCode;
    }

    @Override
    public boolean isEmpty() {
        return _entries.length == 0;
    }

    @Override
    public Set keySet() {
        return new AbstractSet() {
            @Override
            public Iterator iterator() {
                return new ImmutableArrayIterator(_entries) {
                    @Override
                    protected Object next(int i) {
                        return entries[i].getKey();
                    }
                };
            }

            @Override
            public int size() {
                return _entries.length;
            }
        };
    }

    /**
     * N.B: The return value of this call is an ImmutableMap instance which
     * represents the overridden map.
     *
     * @param key
     *            the key to be overridden
     * @param value
     *            the override value
     * @return the resulting ImmutableMap instance
     */
    @Override
    public Object put(Object key, Object value) {
        // override the entries; if this does not produce a change, then
        // key=value is already found in this map, which is returned
        ImmutableEntry[] entries = overrideEntries(_entries, key, value);
        return entries == _entries ? this : new ImmutableMap(entries, true);
    }

    /**
     * This method is unsupported!
     *
     * @see #override(Map)
     * @throws UnsupportedOperationException
     *             because the operation is unsupported
     */
    @Override
    public void putAll(Map m) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * @param m
     *            a map
     * @return an ImmutableMap instance which represents the overridden map.
     */
    public ImmutableMap override(Map m) {
        return m.size() == 0 ? this
                : new ImmutableMap(overrideEntries(_entries, m), true);
    }

    /**
     * N.B: The return value of this call is an ImmutableMap instance which
     * represents the overridden map.
     *
     * @param key
     *            the key to be removed
     * @return the resulting ImmutableMap instance
     */
    @Override
    public Object remove(Object key) {
        int idx = findKey(key, _entries);
        if (idx < 0) {
            return this;
        }

        return new ImmutableMap((ImmutableEntry[])ArrayUtils.removeRange(
                _entries, idx, idx + 1), true);
    }

    @Override
    public int size() {
        return _entries.length;
    }

    @Override
    public Collection values() {
        return new AbstractCollection() {
            @Override
            public Iterator iterator() {
                return new ImmutableArrayIterator(_entries) {
                    @Override
                    protected Object next(int i) {
                        return entries[i].getValue();
                    }
                };
            }

            @Override
            public int size() {
                return _entries.length;
            }
        };
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer().append('{');

        for (int i = 0; i < _entries.length; i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(_entries[i]);
        }

        return result.append('}').toString();
    }

    /**
     * An ImmutableEntry implements an Entry whose values cannot be changed. It
     * is assumed that the key value itself is immutable, i.e. that its hash
     * code does not change.
     */
    public static class ImmutableEntry implements Entry, java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private int _keyCode;
        private Object _key, _value;

        public ImmutableEntry(Object key, Object value) {
            _keyCode = (_key = key) == null ? 0 : key.hashCode();
            _value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry e = (Entry)o;

            return (_key == null ? e.getKey() == null : _key.equals(e.getKey()))
                    &&
                    (_value == null ? e.getValue() == null
                            : _value.equals(e.getValue()));
        }

        @Override
        public Object getKey() {
            return _key;
        }

        @Override
        public Object getValue() {
            return _value;
        }

        @Override
        public int hashCode() {
            return _keyCode ^ (_value == null ? 0 : _value.hashCode());
        }

        @Override
        public Object setValue(Object value) {
            throw new UnsupportedOperationException();
        }

        public int getKeyCode() {
            return _keyCode;
        }

        public boolean keyEquals(Object key) {
            return _key == null ? key == null : _key.equals(key);
        }

        public boolean valueEquals(Object value) {
            return _value == null ? value == null : _value.equals(value);
        }

        @Override
        public String toString() {
            return "[" + _keyCode + "] " + _key + "=" + _value;
        }
    }

    /**
     * An Iterator for an immutable array of immutable entries
     */
    protected static class ImmutableArrayIterator implements Iterator {

        protected ImmutableEntry[] entries;
        private int _index = 0;

        public ImmutableArrayIterator(ImmutableEntry[] entries) {
            this.entries = entries;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext() {
            return _index < entries.length;
        }

        @Override
        public Object next() {
            if (_index >= entries.length) {
                throw new NoSuchElementException();
            }

            return next(_index++);
        }

        protected Object next(int index) {
            return entries[index];
        }

    }
}
