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

import org.xodonex.util.struct.FixedPositionList;

/**
 * This class implements a synchronized fixed position list.
 *
 * @author Henrik Lauritzen
 */
public class SynchronizedFixedPositionList extends SynchronizedList
        implements Cloneable {

    private static final long serialVersionUID = 1L;

    /**
     * The list.
     */
    protected FixedPositionList l;

    /**
     * Constructor.
     *
     * @param l
     *            Initial list
     */
    SynchronizedFixedPositionList(FixedPositionList l) {
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
    SynchronizedFixedPositionList(FixedPositionList l, Object lock) {
        super(l, lock);
        this.l = l;
    }

    /*
     * *************************************************************************
     */

    /**
     * Counts the number of empty slots.
     *
     * @return The number of empty slots.
     */
    public int countEmptySlots() {
        synchronized (lock) {
            return l.countEmptySlots();
        }
    }

    /**
     * Counts the number of occupied slots.
     *
     * @return The number of occupied slots.
     */
    public int countOccupiedSlots() {
        synchronized (lock) {
            return l.countOccupiedSlots();
        }
    }

    /**
     * Returns the index of the next free slot.
     *
     * @return The index of the next free slot.
     */
    public int getNextFreeSlot() {
        synchronized (lock) {
            return l.getNextFreeSlot();
        }
    }

    /**
     * Inserts an object in the list.
     *
     * @param o
     *            The element which should be inserted in the list.
     * @return The position of the element after insertion.
     */
    public int insert(Object o) {
        synchronized (lock) {
            return l.insert(o);
        }
    }

    /**
     * Enlarges the list.
     *
     * @param elemCount
     *            The number of extra free positions.
     */
    public void enlarge(int elemCount) {
        synchronized (lock) {
            l.enlarge(elemCount);
        }
    }

    /*
     * *************************************************************************
     */

    /**
     * Clones this instance of SynchronizedFixedPositionList.
     *
     * @return A clone of this instance of SynchronizedFixedPositionList.
     */
    @Override
    public Object clone() {
        synchronized (lock) {
            return l.clone();
        }
    }

    /*
     * *************************************************************************
     */

}
