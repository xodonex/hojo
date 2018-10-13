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
import java.util.Iterator;

/**
 * A wrapper providing synchronized access to an underlying collection.
 */
public class SynchronizedCollection
        implements Collection, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The collection.
     */
    protected Collection c;

    /**
     * A lock for synchronization.
     */
    protected Object lock;

    /**
     * Constructor.
     *
     * @param lock
     *            The lock for synchronization.
     */
    protected SynchronizedCollection(Object lock) {
        this.lock = lock;
    }

    /**
     * Constructor.
     *
     * @param c
     *            Initial collection.
     */
    public SynchronizedCollection(Collection c) {
        this.lock = c;
        this.c = c;
    }

    /**
     * Constructor.
     *
     * @param c
     *            Initial collection.
     * @param lock
     *            The lock for synchronization.
     */
    public SynchronizedCollection(Collection c, Object lock) {
        this.lock = lock;
        this.c = c;
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
            return c.equals(o);
        }
    }

    @Override
    public int hashCode() {
        synchronized (lock) {
            return c.hashCode();
        }
    }

    @Override
    public int size() {
        synchronized (lock) {
            return c.size();
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (lock) {
            return c.isEmpty();
        }
    }

    @Override
    public boolean contains(Object o) {
        synchronized (lock) {
            return c.contains(o);
        }
    }

    @Override
    public Object[] toArray() {
        synchronized (lock) {
            return c.toArray();
        }
    }

    @Override
    public Object[] toArray(Object[] a) {
        synchronized (lock) {
            return c.toArray(a);
        }
    }

    @Override
    public Iterator iterator() {
        synchronized (lock) {
            return c.iterator();
        }
    }

    @Override
    public boolean add(Object o) {
        synchronized (lock) {
            return c.add(o);
        }
    }

    @Override
    public boolean remove(Object o) {
        synchronized (lock) {
            return c.remove(o);
        }
    }

    @Override
    public boolean containsAll(Collection coll) {
        synchronized (lock) {
            return c.containsAll(coll);
        }
    }

    @Override
    public boolean addAll(Collection coll) {
        synchronized (lock) {
            return c.addAll(coll);
        }
    }

    @Override
    public boolean removeAll(Collection coll) {
        synchronized (lock) {
            return c.removeAll(coll);
        }
    }

    @Override
    public boolean retainAll(Collection coll) {
        synchronized (lock) {
            return c.retainAll(coll);
        }
    }

    @Override
    public void clear() {
        synchronized (lock) {
            c.clear();
        }
    }

}
