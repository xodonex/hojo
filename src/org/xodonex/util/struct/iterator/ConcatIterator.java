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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author Henrik Lauritzen
 */
public class ConcatIterator implements Iterator {

    private Iterator _i1, _i2;
    private Boolean _hasNext = null; // null indicates unevaluated

    public ConcatIterator(Iterator i1, Iterator i2) {
        if (i1 == null || i2 == null) {
            throw new NullPointerException();
        }
        _i1 = i1;
        _i2 = i2;
    }

    @Override
    public Object next() {
        if (_hasNext == Boolean.FALSE) {
            throw new NoSuchElementException();
        }
        _hasNext = null;

        if (_i1 != null) {
            if (_i1.hasNext()) {
                return _i1.next();
            }
            else {
                _i1 = null;
                return _i2.next();
            }
        }
        else {
            return _i2.next();
        }
    }

    @Override
    public void remove() {
        if (_i1 != null) {
            _i1.remove();
        }
        else {
            _i2.remove();
        }
    }

    @Override
    public boolean hasNext() {
        if (_hasNext == null) {
            _hasNext = ((_i1 != null && _i1.hasNext()) || _i2.hasNext())
                    ? Boolean.TRUE
                    : Boolean.FALSE;
        }
        return _hasNext.booleanValue();
    }

}
