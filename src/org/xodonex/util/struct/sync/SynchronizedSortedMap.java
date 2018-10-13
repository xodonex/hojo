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

import java.util.Comparator;
import java.util.SortedMap;

/**
 * A wrapper providing synchronized access to an underlying sorted map.
 */
public class SynchronizedSortedMap extends SynchronizedMap
        implements SortedMap {

    private static final long serialVersionUID = 1L;

    /**
     * @param m
     *            The map to be synchronized.
     */
    public SynchronizedSortedMap(SortedMap m) {
        super(m);
    }

    /**
     * Constructor.
     *
     * @param m
     *            Initial map.
     * @param lock
     *            The lock for synchronization.
     */
    public SynchronizedSortedMap(SortedMap m, Object lock) {
        super(m, lock);
    }

    @Override
    public Comparator comparator() {
        synchronized (lock) {
            return ((SortedMap)m).comparator();
        }
    }

    @Override
    public Object firstKey() {
        synchronized (lock) {
            return ((SortedMap)m).firstKey();
        }
    }

    @Override
    public SortedMap headMap(Object toKey) {
        synchronized (lock) {
            return ((SortedMap)m).headMap(toKey);
        }
    }

    @Override
    public Object lastKey() {
        synchronized (lock) {
            return ((SortedMap)m).lastKey();
        }
    }

    @Override
    public SortedMap subMap(Object fromKey, Object toKey) {
        synchronized (lock) {
            return ((SortedMap)m).subMap(fromKey, toKey);
        }
    }

    @Override
    public SortedMap tailMap(Object fromKey) {
        synchronized (lock) {
            return ((SortedMap)m).tailMap(fromKey);
        }
    }

}
