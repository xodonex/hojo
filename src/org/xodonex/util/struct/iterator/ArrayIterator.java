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

/**
 *
 * @author Henrik Lauritzen
 */
public class ArrayIterator extends SubListIterator {

    private Object[] _array;

    public ArrayIterator(Object[] array) {
        this(array, 0, -1);
    }

    public ArrayIterator(Object[] array, int start, int end) {
        super(start, end);
        if (array == null) {
            throw new NullPointerException();
        }
        _array = array;
    }

    @Override
    protected int size() {
        return _array.length;
    }

    @Override
    protected Object retreive(int index) {
        return _array[index];
    }

    @Override
    protected int set(int index, Object obj) {
        _array[index] = obj;
        return 0;
    }

}
