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
package org.xodonex.util.struct.sync;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A wrapper providing synchronized access to an underlying map.
 */
public class SynchronizedMap implements Map, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The map to be synchronized.
     */
    protected Map m;

    /**
     * The lock used for synchronization.
     */
    protected Object lock;

    /**
     * @param lock
     *            The lock for synchronization.
     */
    protected SynchronizedMap(Object lock) {
        this.lock = lock;
    }

    /**
     * @param m
     *            The map to be synchronized.
     */
    public SynchronizedMap(Map m) {
        this(m, m);
    }

    /**
     * Constructor.
     *
     * @param m
     *            Initial collection.
     * @param lock
     *            The lock for synchronization.
     */
    public SynchronizedMap(Map m, Object lock) {
        this((Object)m);
        this.lock = lock;
    }

    /**
     * @return the lock used for synchronization
     */
    public Object getLock() {
        return lock;
    }

    @Override
    public boolean equals(Object o) {
        synchronized (lock) {
            return m.equals(o);
        }
    }

    @Override
    public int hashCode() {
        synchronized (lock) {
            return m.hashCode();
        }
    }

    @Override
    public int size() {
        synchronized (lock) {
            return m.size();
        }
    }

    @Override
    public void clear() {
        synchronized (lock) {
            m.clear();
        }
    }

    @Override
    public boolean containsValue(Object obj) {
        synchronized (lock) {
            return m.containsValue(obj);
        }
    }

    @Override
    public Object put(Object key, Object value) {
        synchronized (lock) {
            return m.put(key, value);
        }
    }

    @Override
    public Object get(Object key) {
        synchronized (lock) {
            return m.get(key);
        }
    }

    @Override
    public Set keySet() {
        synchronized (lock) {
            return m.keySet();
        }
    }

    @Override
    public Collection values() {
        synchronized (lock) {
            return m.values();
        }
    }

    @Override
    public Set entrySet() {
        synchronized (lock) {
            return m.entrySet();
        }
    }

    @Override
    public void putAll(Map map) {
        synchronized (lock) {
            m.putAll(map);
        }
    }

    @Override
    public boolean containsKey(Object key) {
        synchronized (lock) {
            return m.containsKey(key);
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (lock) {
            return m.isEmpty();
        }
    }

    @Override
    public Object remove(Object key) {
        synchronized (lock) {
            return m.remove(key);
        }
    }

}
