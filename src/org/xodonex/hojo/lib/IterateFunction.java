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
package org.xodonex.hojo.lib;

import java.util.Iterator;

import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.StandardFunction;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.struct.iterator.TreeIterator;

/**
 *
 * @author Henrik Lauritzen
 */
public final class IterateFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    public final static int ITERATOR = TreeIterator.TRAVERSE_ITERATOR;
    public final static int ARRAY = TreeIterator.TRAVERSE_ARRAY;
    public final static int COLLECTION = TreeIterator.TRAVERSE_COLLECTION;
    public final static int MAP = TreeIterator.TRAVERSE_MAP;
    public final static int CHAR_SEQUENCE = TreeIterator.TRAVERSE_CHAR_SEQUENCE;
    public final static int ALL = TreeIterator.TRAVERSE_ALL;
    public final static int IGNORE_NULL = TreeIterator.IGNORE_NULL;
    private final static Integer DEFAULT_FLAGS = new Integer(
            ALL & ~CHAR_SEQUENCE);

    private final static IterateFunction INSTANCE = new IterateFunction();

    private IterateFunction() {
    }

    public static IterateFunction getInstance() {
        return INSTANCE;
    }

    @Override
    public Class[] getParameterTypes() {
        return new Class[] { Object.class, Integer.class, Integer.class };
    }

    @Override
    public String[] getParameterNames() {
        return new String[] { "tree", "maxDepth", "flags" };
    }

    @Override
    public Object getDefaultValue(int arg) {
        switch (arg) {
        case 1:
            return ConvertUtils.TRUE_INT; // -1
        case 2:
            return DEFAULT_FLAGS;
        default:
            return NO_ARG;
        }
    }

    @Override
    public Class getReturnType() {
        return Iterator.class;
    }

    @Override
    public Object invoke(Object[] arguments) throws HojoException {
        int maxDepth = ConvertUtils.toInt(arguments[1]);
        int options = ConvertUtils.toInt(arguments[2]);
        return new TreeIterator(arguments[0], maxDepth, options);
    }

}
