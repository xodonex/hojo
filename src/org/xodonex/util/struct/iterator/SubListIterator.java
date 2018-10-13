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
package org.xodonex.util.struct.iterator;

import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * An iterator operating on a subrange of a list iterator.
 */
public abstract class SubListIterator implements ListIterator {

    // The minimum index for the iteration.
    private int _min;

    // The maximum index for the iteration (_max < 0) => no upper limit.
    private int _max;

    // The current index (index of the element retreived by next())
    private int _next;

    // The last index retrieved (for remove() etc.). (_last < 0) => remove() is
    // invalid.
    private int _last = -1;

    public SubListIterator() {
        this(0, -1);
    }

    public SubListIterator(int start, int end) {
        _min = _next = start;
        _max = end;
    }

    @Override
    public boolean hasNext() {
        return (_max >= 0 ? _next < _max : true) && _next < size();
    }

    @Override
    public Object next() {
        if ((_max >= 0 && _next >= _max) || _next >= size()) {
            throw new NoSuchElementException();
        }
        return retreive(_last = _next++);
    }

    @Override
    public boolean hasPrevious() {
        return _next > _min;
    }

    @Override
    public Object previous() {
        if (_next <= _min) {
            throw new NoSuchElementException();
        }
        return retreive(_last = --_next);
    }

    @Override
    public int nextIndex() {
        return _next;
    }

    @Override
    public int previousIndex() {
        int result = _next - 1;
        return (result < _min) ? -1 : result;
    }

    @Override
    public void remove() {
        if (_last < 0) {
            throw new IllegalStateException();
        }
        int dec = remove(_last);
        if (_next > _last) {
            _next -= dec;
        }
        if (_max > _last) {
            _max -= dec;
        }
        _last = -1;
    }

    @Override
    public void set(Object obj) {
        if (_last < 0) {
            throw new IllegalStateException();
        }
        int inc = set(_last, obj);
        _next += inc;
        if (_max >= 0) {
            _max += inc;
        }
    }

    @Override
    public void add(Object obj) {
        int inc = add(_next, obj);
        _last = -1;
        _next += inc;
        if (_max >= 0) {
            _max += inc;
        }
    }

    protected abstract int size();

    protected abstract Object retreive(int index);

    /**
     * Add elements to this sublist
     *
     * @param index
     *            the start index for addition
     * @param obj
     *            the set of elements to be added
     * @return the amount of elements added to the underlying list.
     */
    protected int add(int index, Object obj) {
        throw new UnsupportedOperationException();
    }

    /**
     * Insert elements to this sublist
     *
     * @param index
     *            the start index for insertion
     * @param obj
     *            the set of elements to be added
     * @return the amount of elements added to the underlying list.
     */
    protected int set(int index, Object obj) {
        throw new UnsupportedOperationException();
    }

    /**
     * Remove elements from this sublist
     *
     * @param index
     *            the start index for removal
     * @return the amount of elements removed from the underlying list.
     */
    protected int remove(int index) {
        throw new UnsupportedOperationException();
    }

}
