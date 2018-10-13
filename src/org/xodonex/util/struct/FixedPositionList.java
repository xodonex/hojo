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

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

/**
 * This class implements a list with fixed positions, called slots. Slots can be
 * empty (e.g. when the value is deleted), but all existing values retain their
 * fixed-slot position on insertion and delete.
 */
public class FixedPositionList extends ArrayList {

    private static final long serialVersionUID = 1L;

    private int empty = 0;
    private TreeMap nextFree;

    /**
     * Constructor.
     */
    public FixedPositionList() {
        this(10);
    }

    /**
     * Constructor.
     *
     * @param c
     *            A collection which should be included in the list.
     */
    public FixedPositionList(Collection c) {
        this(10);
        addAll(c);
    }

    /**
     * Constructor.
     *
     * @param initialCapacity
     *            The initial capacity of the list.
     */
    public FixedPositionList(int initialCapacity) {
        super(initialCapacity);
        nextFree = new TreeMap();
        empty = 0;
    }

    /*
     * ******************************** FixedPositionList
     * ************************
     */

    private int getAndRemoveIndex() {
        empty--;
        return ((Integer)(nextFree.remove(nextFree.firstKey()))).intValue();
    }

    private void indicateSlotEmpty(int slot) {
        Integer i = new Integer(slot);
        nextFree.put(i, i);
        empty++;
    }

    private void indicateSlotFull(int i) {
        nextFree.remove(new Integer(i));
        empty--;
    }

    /**
     * Counts the number of empty slots.
     *
     * @return The number of empty slots.
     */
    public int countEmptySlots() {
        return empty;
    }

    /**
     * Counts the number of occupied slots.
     *
     * @return Tthe number of occupied slots.
     */
    public int countOccupiedSlots() {
        return size() - empty;
    }

    /**
     * Returns the index of the next free slot.
     *
     * @return The index of the next free slot.
     */
    public int getNextFreeSlot() {
        if (empty == 0) {
            return size();
        }
        else {
            return ((Integer)nextFree.firstKey()).intValue();
        }
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
    // @pre o != null
    public int insert(Object o) throws NullPointerException {
        int slot;

        if (o == null) {
            throw new NullPointerException();
        }

        if (empty == 0) {
            slot = size();
            super.add(o);
        }
        else {
            slot = getAndRemoveIndex();
            super.set(slot, o); // this will be empty
        }
        return slot;
    }

    /**
     * Enlarges the list.
     *
     * @param elemCount
     *            The number of extra free positions.
     */
    public void enlarge(int elemCount) {
        int slot = size();
        while (elemCount-- > 0) {
            super.add(null);
            indicateSlotEmpty(slot++);
        }
    }

    /* ******************************* ******************************* */

    /**
     * Adds raw data to the list.
     *
     * @param data
     *            The data to be added.
     * @return <code>true</code> (as per the general contract of
     *         Collection.add).
     */
    protected final boolean addRawData(Object data) {
        return super.add(data);
    }

    /**
     * Get raw data from the list.
     *
     * @param index
     *            The index from which data should be received.
     * @return The data in position index.
     */
    protected final Object getRawData(int index) {
        return super.get(index);
    }

    /**
     * Sets the data at the specified position to the specified value.
     *
     * @param index
     *            The position.
     * @param data
     *            The data.
     * @return The element previously at the specified position
     */
    protected final Object setRawData(int index, Object data) {
        return super.set(index, data);
    }

    /*
     * ******************************* ArrayList *******************************
     */

    /**
     * Adds an element to the list.
     *
     * @param element
     *            The element to be added to the list.
     * @return <code>true</code> (as per the general contract of
     *         Collection.add).
     * @exception NullPointerException
     *                if the element is null
     */
    // @pre element != null
    @Override
    public boolean add(Object element) throws NullPointerException {
        if (element == null) {
            throw new NullPointerException();
        }
        else {
            return super.add(element);
        }
    }

    /**
     * Adds an element to the list at the specified position.
     *
     * @param index
     *            The position.
     * @param element
     *            The element.
     * @exception NullPointerException
     *                if the element is null
     * @exception IllegalStateException
     *                if the slot is already full
     */
    // @pre element != null
    // @pre get(index) != null
    @Override
    public void add(int index, Object element) throws NullPointerException,
            IllegalStateException {
        if (element == null) {
            throw new NullPointerException();
        }
        else if (get(index) != null) {
            throw new IllegalStateException();
        }
        else {
            set(index, element);
        }
    }

    /**
     * Adds a collection to the list.
     *
     * @param c
     *            The collection to be added.
     * @return <code>true</code> if this list changed as a result of the call.
     * @exception NullPointerException
     *                if the collectino is null
     */
    // @pre c != null
    @Override
    public boolean addAll(Collection c) throws NullPointerException {
        if (c.contains(null)) {
            throw new NullPointerException();
        }
        return super.addAll(c);
    }

    /**
     * This operation is unsupported.
     *
     * @param index
     *            ignored
     * @param c
     *            ignored
     * @exception UnsupportedOperationException
     *                because the operation is unsupported
     */
    @Override
    public boolean addAll(int index, Collection c)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Clears the list.
     */
    @Override
    public void clear() {
        super.clear();
        nextFree.clear();
        empty = 0;
    }

    /**
     * Clones this instance of FixedPositionList.
     *
     * @return A clone of this instance of FixedPositionList.
     */
    @Override
    public Object clone() {
        FixedPositionList result = (FixedPositionList)super.clone();
        result.nextFree = (TreeMap)nextFree.clone();
        return result;
    }

    /**
     * Get the index of an element.
     *
     * @param elem
     *            The element.
     * @return The index of elem.
     */
    @Override
    public int indexOf(Object elem) {
        if (elem == null) {
            if (empty == 0) {
                return -1;
            }
            else {
                return ((Integer)nextFree.firstKey()).intValue();
            }
        }
        else {
            return super.indexOf(elem);
        }
    }

    /**
     * Get the index of the last occurrence of an element.
     *
     * @param elem
     *            The element.
     * @return The index of the last occurence of the element, or -1 if the
     *         element is not in the list.
     */
    @Override
    public int lastIndexOf(Object elem) {
        if (elem == null) {
            if (empty == 0) {
                return -1;
            }
            else {
                return ((Integer)nextFree.lastKey()).intValue();
            }
        }
        else {
            return super.lastIndexOf(elem);
        }
    }

    /**
     * Remove the element at the specified position.
     *
     * @param index
     *            The index which should contain no element after the operation.
     * @return The contents at position index before the operation.
     */
    @Override
    public Object remove(int index) {
        Object result = super.set(index, null);
        if (result == null) {
            // reremoved an empty slot
            return null;
        }
        else {
            indicateSlotEmpty(index);
        }

        return result;
    }

    /**
     * Remove all elements within the specified range.
     *
     * @param fromIndex
     *            The lower bound of the range.
     * @param toIndex
     *            The upper bound of the range.
     */
    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++) {
            set(i, null);
        }
    }

    /**
     * Sets the contents of the list the specified position to the specified
     * element.
     *
     * @param index
     *            The position.
     * @param element
     *            The element.
     * @return The contents of the list the specified position before the
     *         operation.
     */
    @Override
    public Object set(int index, Object element) {
        Object result = super.set(index, element);

        if (element == null) {
            if (result != null) {
                // removed an element
                indicateSlotEmpty(index);
            }
            // else no change is necessary
        }
        else {
            if (result == null) {
                // inserted into an empty slot
                indicateSlotFull(index);
            }
            // else no change is necessary
        }
        return result;
    }

}
