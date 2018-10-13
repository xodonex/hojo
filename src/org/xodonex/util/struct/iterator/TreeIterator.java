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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 *
 * @author Henrik Lauritzen
 */
public class TreeIterator implements Iterator {

    public final static int TRAVERSE_ITERATOR = 1;
    public final static int TRAVERSE_ARRAY = 2;
    public final static int TRAVERSE_COLLECTION = 4;
    public final static int TRAVERSE_MAP = 8;
    public final static int TRAVERSE_CHAR_SEQUENCE = 16;
    public final static int TRAVERSE_ALL = TRAVERSE_ITERATOR | TRAVERSE_ARRAY
            | TRAVERSE_COLLECTION |
            TRAVERSE_CHAR_SEQUENCE;
    public final static int IGNORE_NULL = 32;

    // indicates that no value is present
    private final static Object NO_VALUE = new Object();

    // cached return value for hasNext() - null indicates unevaluated
    private Boolean _hasNext = null;

    // the next value to be returned by next()
    private Object _next = null;

    // the current branch being traversed
    private Iterator _branch = null;

    // the path of traversed iterators from the root node
    private Stack _path = new Stack();

    // the next unexplored node, (or NO_VALUE)
    private Object _in = NO_VALUE;

    // operation config
    private int _flags, _maxLevel;

    public TreeIterator(Object tree, int maxLevel, int flags) {
        _in = tree;
        _maxLevel = maxLevel;
        _flags = flags;
    }

    @Override
    public boolean hasNext() {
        if (_hasNext != null) {
            // return an already determined value.
            return _hasNext.booleanValue();
        }

        if (_in != NO_VALUE) {
            // explore the already received branch (this is only used
            // right after the construction)
            Object obj = _in;
            _in = NO_VALUE;
            if (explore(obj)) {
                // the result was a leaf - done
                _hasNext = Boolean.TRUE;
                return true;
            }
        }

        // get the next branch
        while (true) {
            while (_branch == null) {
                if (_path.size() > 0) {
                    // the current branch is done - pop a level
                    _branch = (Iterator)_path.pop();
                }
                else {
                    // no branch, no input, empty stack: done
                    _hasNext = Boolean.FALSE;
                    _path = null;
                    return false;
                }
            }

            // does the branch have any more values?
            if (!_branch.hasNext()) {
                // the current branch is finished - remove it
                _branch = null;
                continue;
            }

            // explore the next value
            if (explore(_branch.next())) {
                // the result was a leaf - done
                _hasNext = Boolean.TRUE;
                return true;
            }
        } // while(true)
    }

    @Override
    public Object next() {
        // traverse to the next node, if any
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        // invalidate _hasNext, and return the next value.
        _hasNext = null;
        return _next;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    // explore a branch/leaf
    // (check that it has not been seen before??)
    // return whether a leaf was the result
    private boolean explore(Object obj) {
        if (obj == null && (_flags & IGNORE_NULL) == IGNORE_NULL) {
            // ignore null's if required
            return false;
        }
        if ((_maxLevel >= 0 && _path.size() >= _maxLevel)) {
            // null leaf or max level exceeded
            _next = obj;
            return true;
        }
        if ((_flags & TRAVERSE_COLLECTION) != 0 && obj instanceof Collection) {
            Collection c = (Collection)obj;
            if (c.size() > 0) {
                push(c.iterator());
            }
            return false;
        }
        if (obj != null &&
                (_flags & TRAVERSE_ARRAY) != 0 && obj.getClass().isArray()) {
            if (obj instanceof Object[]) {
                Object[] arr = (Object[])obj;
                if (arr.length > 0) {
                    push(new ArrayIterator(arr));
                }
            }
            else {
                if (java.lang.reflect.Array.getLength(obj) > 0) {
                    push(new PrimitiveArrayIterator(obj));
                }
            }
            return false;
        }
        if ((_flags & TRAVERSE_ITERATOR) != 0 && obj instanceof Iterator) {
            push((Iterator)obj);
            return false;
        }
        if ((_flags & TRAVERSE_MAP) != 0 && obj instanceof Map) {
            Map m = (Map)obj;
            if (m.size() > 0) {
                push(m.entrySet().iterator());
            }
            return false;
        }
        if ((_flags & TRAVERSE_CHAR_SEQUENCE) != 0
                && obj instanceof CharSequence) {
            CharSequence cs = (CharSequence)obj;
            if (cs.length() > 0) {
                push(new CharSequenceIterator(cs));
            }
            return false;
        }

        // the object is a leaf
        _next = obj;
        return true;
    }

    // put the given iterator on the stack
    private void push(Iterator seq) {
        if (_branch != null) {
            _path.push(_branch);
        }
        _branch = seq;
    }

}
