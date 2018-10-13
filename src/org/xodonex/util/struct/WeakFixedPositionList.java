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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;

/**
 * This class implements a fixed position list which has a WeakReference to each
 * of its elements.
 *
 * @see FixedPositionList
 */
public class WeakFixedPositionList extends FixedPositionList {

    private static final long serialVersionUID = 1L;

    private ReferenceQueue refQ = new ReferenceQueue();

    private static class Ref extends WeakReference {
        int pos;

        Ref(Object referent, ReferenceQueue q, int pos) {
            super(referent, q);
            this.pos = pos;
        }
    }

    /**
     * Constructor.
     */
    public WeakFixedPositionList() {
        this(10);
    }

    /**
     * Constructor.
     *
     * @param c
     *            Initial collection.
     * @exception NullPointerException
     *                if c is null
     */
    public WeakFixedPositionList(Collection c) throws NullPointerException {
        this(10);
        addAll(c);
    }

    /**
     * Constructor.
     *
     * @param initialCapacity
     *            The initial capacity of the list.
     */
    public WeakFixedPositionList(int initialCapacity) {
        super(initialCapacity);
    }

    private final static Object deref(Reference r) {
        return (r == null) ? null : r.get();
    }

    /**
     * Clear all references.
     */
    protected void clearRefs() {
        int mCount = modCount;
        Ref r;

        while ((r = (Ref)refQ.poll()) != null) {
            set(r.pos, null);
        }

        // prevent cleared references from creating a
        // concurrentModificationException
        modCount = mCount;
    }

    /**
     * Counts the number of empty slots.
     *
     * @return The number of empty slots.
     */
    @Override
    public int countEmptySlots() {
        clearRefs();
        return super.countEmptySlots();
    }

    /**
     * Counts the number of occupied slots.
     *
     * @return The number of occupied slots.
     */
    @Override
    public int countOccupiedSlots() {
        clearRefs();
        return super.countOccupiedSlots();
    }

    /**
     * Returns the index of the next free slot.
     *
     * @return The index of the next free slot.
     */
    @Override
    public int getNextFreeSlot() {
        clearRefs();
        return super.getNextFreeSlot();
    }

    /**
     * Inserts an object in the list.
     *
     * @param o
     *            The element which should be inserted in the list.
     * @return The position of the element after insertion.
     * @exception NullPointerException
     *                if o is null
     */
    @Override
    public int insert(Object o) throws NullPointerException {
        int slot = getNextFreeSlot();
        if (slot == size()) {
            add(o);
        }
        else {
            set(slot, o);
        }
        return slot;
    }

    /*
     * ******************************* ArrayList *******************************
     */

    /**
     * Adds an element to the list.
     *
     * @param element
     *            the element to be added.
     * @return <code>true</code> if the list changed as a result of the call,
     *         <code>false</code> otherwise.
     * @exception NullPointerException
     *                if element is null
     */
    @Override
    public boolean add(Object element) throws NullPointerException {
        if (element == null) {
            throw new NullPointerException();
        }

        // create a new, empty slot
        return addRawData(new Ref(element, refQ, size()));
    }

    /**
     * Adds all elements of the specified collection to the list.
     *
     * @param c
     *            the elements to be added
     * @throws NullPointerException
     *             if c is null, or if c contains null elements
     */
    @Override
    public boolean addAll(Collection c) throws NullPointerException {
        Iterator it = c.iterator();
        boolean result = false;
        while (it.hasNext()) {
            add(it.next());
            result = true;
        }
        return result;
    }

    /**
     * Clears the list.
     */
    @Override
    public void clear() {
        Iterator it = super.iterator();
        Ref ref;
        while (it.hasNext()) {
            if ((ref = (Ref)it.next()) != null) {
                ref.clear();
            }
        }
        super.clear();
    }

    /**
     * Clones this instance of WeakFixedpositionList.
     *
     * @return A clone of this instance of WeakFixedpositionList.
     */
    @Override
    public Object clone() {
        WeakFixedPositionList result = (WeakFixedPositionList)super.clone();
        result.refQ = new ReferenceQueue();
        return result;
    }

    /**
     * Returns the index of the specified element.
     */
    @Override
    public int indexOf(Object elem) {
        clearRefs();
        if (elem == null) {
            return super.indexOf(null);
        }

        int max = size();
        for (int i = 0; i < max; i++) {
            if (deref((Reference)getRawData(i)) == elem) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Returns the index of the last occurence of the specified element.
     */
    @Override
    public int lastIndexOf(Object elem) {
        clearRefs();
        if (elem == null) {
            return super.lastIndexOf(null);
        }

        for (int i = size() - 1; i >= 0; i--) {
            if (deref((Reference)super.get(i)) == elem) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Remove the element at the specified position.
     */
    @Override
    public Object remove(int index) {
        return set(index, null);
    }

    /**
     * Get the element at the specified position.
     */
    @Override
    public Object get(int index) {
        clearRefs();
        return deref((Reference)getRawData(index));
    }

    /**
     * Set the specified element to the specified position.
     */
    @Override
    public Object set(int index, Object element) {
        clearRefs();
        Ref newRef = (element == null) ? null : new Ref(element, refQ, index);
        return deref((Reference)super.set(index, newRef));
    }

    /**
     * Returns an array representation of this list.
     */
    @Override
    public Object[] toArray() {
        return toArray(new Object[size()]);
    }

    /**
     * Returns an array representation of this list.
     *
     * @param array
     *            The array which should hold the elements of this list.
     */
    @Override
    public Object[] toArray(Object[] array) {
        int size = size();
        if (array.length < size) {
            array = (Object[])java.lang.reflect.Array.newInstance(
                    array.getClass().getComponentType(), size);
        }

        for (int i = 0; i < size; i++) {
            array[i] = get(i);
        }
        return array;
    }

}
