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

import java.lang.reflect.Array;
import java.util.AbstractList;

/**
 * An ArrayList backed by an array primitive-type values.
 */
public class PrimitiveArrayList extends AbstractList {

    private int lo, hi;
    private Object array;

    public PrimitiveArrayList(Object array) {
        this(array, 0, Array.getLength(array));
    }

    public PrimitiveArrayList(Object array, int index) {
        this(array, index, Array.getLength(array));
    }

    public PrimitiveArrayList(Object array, int lo, int hi) {
        if (lo < 0 || lo > hi) {
            throw new IllegalArgumentException("" + lo);
        }
        else if (hi > Array.getLength(array)) {
            throw new IllegalArgumentException("" + hi);
        }
        this.lo = lo;
        this.hi = hi;
        this.array = array;
    }

    private int getIndex(int index) {
        int id = index + lo;
        if (id < lo || id >= hi) {
            throw new ArrayIndexOutOfBoundsException("" + index);
        }
        return id;
    }

    @Override
    public Object get(int index) {
        return Array.get(array, getIndex(index));
    }

    @Override
    public Object set(int index, Object value) {
        int id = getIndex(index);
        Object result = Array.get(array, id);
        Array.set(array, id, value);
        return result;
    }

    @Override
    public int size() {
        return hi - lo;
    }
}
