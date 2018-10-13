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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.StandardFunction;
import org.xodonex.hojo.lang.Function;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.struct.iterator.ArrayIterator;
import org.xodonex.util.struct.iterator.CharSequenceIterator;
import org.xodonex.util.struct.iterator.PrimitiveArrayIterator;

public final class FindFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    public final static Integer NOT_FOUND = HojoLib.TRUE_INT; // -1

    private final static Class[] pTypes = { Function.class, Object.class,
            Integer.class, Integer.class };
    private final static FindFunction instance = new FindFunction();

    private FindFunction() {
    }

    /*
     * ******************************* Function *******************************
     */

    public static FindFunction getInstance() {
        return instance;
    }

    @Override
    public Class[] getParameterTypes() {
        return pTypes;
    }

    @Override
    public String[] getParameterNames() {
        return new String[] { "predicate", "sequence", "startIndex",
                "endIndex" };
    }

    @Override
    public Object getDefaultValue(int arg) {
        switch (arg) {
        case 2:
            return ConvertUtils.ZERO_INT;
        case 3:
            return ConvertUtils.TRUE_INT; // -1
        default:
            return NO_ARG;
        }
    }

    @Override
    public Class getReturnType() {
        return Integer.class;
    }

    @Override
    public Object invoke(Object[] args) {
        Function pred = HojoLib.toFunction(args[0]);
        Object seq = args[1];
        int lo = ConvertUtils.toInt(args[2]);
        int hi = ConvertUtils.toInt(args[3]);

        Object[] as = new Object[1];
        Iterator it;

        if (seq instanceof CharSequence) {
            it = new CharSequenceIterator((String)seq, lo, hi);
        }
        else if (seq instanceof Object[]) {
            it = new ArrayIterator((Object[])seq, lo, hi);
        }
        else if (seq.getClass().isArray()) {
            it = new PrimitiveArrayIterator(seq, lo, hi);
        }
        else if (seq instanceof List) {
            List l = (List)seq;
            if (hi < lo) {
                hi = l.size();
            }
            it = l.subList(lo, hi).iterator();
        }
        else {
            it = ((Collection)seq).iterator();
        }

        if (hi < lo) {
            hi = Integer.MAX_VALUE;
        }
        while (lo < hi && it.hasNext()) {
            as[0] = it.next();
            if (ConvertUtils.toBool(pred.invoke(pred.validateArgs(as)))) {
                return new Integer(lo);
            }
            lo++;
        }

        return NOT_FOUND;
    }

}
