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

import java.util.AbstractList;

import org.xodonex.util.ConvertUtils;

/**
 * A List view StringBuffer data.
 */
public class StringBufferList extends AbstractList {

    private int lo, hi;
    private StringBuffer buf;

    public StringBufferList(StringBuffer buf) {
        this(buf, 0, -1);
    }

    public StringBufferList(StringBuffer buf, int index) {
        this(buf, index, -1);
    }

    public StringBufferList(StringBuffer buf, int lo, int hi) {
        if (lo < 0) {
            throw new IllegalArgumentException("" + lo);
        }
        else if ((hi >= 0 && hi < lo) || hi > buf.length()) {
            throw new IllegalArgumentException("" + hi);
        }
        this.lo = lo;
        this.hi = hi;
        this.buf = buf;
    }

    private int getIndex(int index, int extra) {
        int id = index + lo;
        if (id < lo || (hi >= 0 && id >= hi + extra) ||
                id >= buf.length() + extra) {
            throw new IndexOutOfBoundsException("" + index);
        }
        return id;
    }

    @Override
    public void add(int index, Object obj) {
        index = getIndex(index, 1);
        if (index == buf.length()) {
            buf.append(ConvertUtils.toChar(obj));
            if (hi >= 0) {
                hi++;
            }
        }
        else {
            buf.insert(index, ConvertUtils.toChar(obj));
            if (hi >= 0) {
                hi++;
            }
        }
    }

    @Override
    public Object remove(int index) {
        index = getIndex(index, 0);
        char result = buf.charAt(index);
        buf.deleteCharAt(index);
        if (hi >= 1) {
            hi--;
        }
        return new Character(result);
    }

    @Override
    public Object get(int index) {
        return new Character(buf.charAt(getIndex(index, 0)));
    }

    @Override
    public Object set(int index, Object value) {
        int id = getIndex(index, 0);
        Character result = new Character(buf.charAt(index));
        char c = ConvertUtils.toChar(value);
        buf.setCharAt(id, c);
        return result;
    }

    @Override
    public int size() {
        if (hi >= 0) {
            return hi - lo;
        }
        else {
            return buf.length() - lo;
        }
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        if (fromIndex == toIndex) {
            return;
        }
        fromIndex = getIndex(fromIndex, 0);
        if (toIndex < 0) {
            toIndex = -1;
        }
        else {
            toIndex = getIndex(toIndex, 1);
            if (toIndex == buf.length()) {
                toIndex = -1;
            }
        }

        if (toIndex < 0) {
            buf.setLength(fromIndex);
            if (hi >= 0) {
                hi = fromIndex;
            }
        }
        else {
            buf.delete(fromIndex, toIndex);
            if (hi >= 0) {
                hi -= toIndex - fromIndex;
            }
        }
    }
}
