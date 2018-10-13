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
import java.util.List;
import java.util.ListIterator;

/**
 * This class implements a synchronized list.
 *
 * @author Henrik Lauritzen
 */
public class SynchronizedList extends SynchronizedCollection
        implements List, java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The list.
     */
    protected List l;

    /**
     * Constructor.
     *
     * @param l
     *            Initial list.
     */
    public SynchronizedList(List l) {
        super(l, l);
        this.l = l;
    }

    /**
     * Constructor.
     *
     * @param l
     *            Initial list.
     * @param lock
     *            Lock for synchronization.
     */
    public SynchronizedList(List l, Object lock) {
        super(l, l);
        this.l = l;
    }

    /*
     * *************************************************************************
     */

    /**
     * Returns a list iterator.
     *
     * @return A list iterator.
     */
    @Override
    public ListIterator listIterator() {
        synchronized (lock) {
            return l.listIterator();
        }
    }

    /**
     * Returns a list iterator.
     *
     * @param index
     *            The starting point of the iterator.
     * @return A list iterator.
     */
    @Override
    public ListIterator listIterator(int index) {
        synchronized (lock) {
            return l.listIterator(index);
        }
    }

    /**
     * Adds all elements from the specified collection.
     */
    @Override
    public boolean addAll(int index, Collection c) {
        synchronized (lock) {
            return l.addAll(index, c);
        }
    }

    /**
     * Get the element at the specified position.
     */
    @Override
    public Object get(int index) {
        synchronized (lock) {
            return l.get(index);
        }
    }

    /**
     * Get the index of the specified element.
     */
    @Override
    public int indexOf(Object o) {
        synchronized (lock) {
            return l.indexOf(o);
        }
    }

    /**
     * Get the index of the last occurence of the specified element.
     */
    @Override
    public int lastIndexOf(Object o) {
        synchronized (lock) {
            return l.lastIndexOf(o);
        }
    }

    /**
     * Add the specified element at the specified position.
     */
    @Override
    public void add(int index, Object o) {
        synchronized (lock) {
            l.add(index, o);
        }
    }

    /**
     * Remove the element at the specified position.
     */
    @Override
    public Object remove(int index) {
        synchronized (lock) {
            return l.remove(index);
        }
    }

    /**
     * Set the specified element to the specified position.
     */
    @Override
    public Object set(int index, Object o) {
        synchronized (lock) {
            return l.set(index, o);
        }
    }

    /**
     * Get the sublist in the specified range.
     */
    @Override
    public List subList(int from, int to) {
        synchronized (lock) {
            return l.subList(from, to);
        }
    }

}
