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
import java.util.SortedSet;

/**
 * This class implements a synchronized list.
 *
 * @author Henrik Lauritzen
 */
public class SynchronizedSortedSet extends SynchronizedSet
        implements SortedSet {

    private static final long serialVersionUID = 1L;

    public SynchronizedSortedSet(SortedSet s) {
        super(s);
    }

    public SynchronizedSortedSet(SortedSet s, Object lock) {
        super(s, lock);
    }

    /*
     * *************************************************************************
     */

    @Override
    public Comparator comparator() {
        synchronized (lock) {
            return ((SortedSet)c).comparator();
        }
    }

    @Override
    public Object first() {
        synchronized (lock) {
            return ((SortedSet)c).first();
        }
    }

    @Override
    public SortedSet headSet(Object toElement) {
        synchronized (lock) {
            return ((SortedSet)c).headSet(toElement);
        }
    }

    @Override
    public Object last() {
        synchronized (lock) {
            return ((SortedSet)c).last();
        }
    }

    @Override
    public SortedSet subSet(Object fromElement, Object toElement) {
        synchronized (lock) {
            return ((SortedSet)c).subSet(fromElement, toElement);
        }
    }

    @Override
    public SortedSet tailSet(Object fromElement) {
        synchronized (lock) {
            return ((SortedSet)c).tailSet(fromElement);
        }
    }

}
