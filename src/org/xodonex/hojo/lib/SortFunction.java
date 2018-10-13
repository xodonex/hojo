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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.StandardFunction;
import org.xodonex.util.ConvertUtils;

public final class SortFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    private final static Class[] pTypes = { Object.class, Integer.class,
            Integer.class, Comparator.class };
    private final static SortFunction instance = new SortFunction();

    private SortFunction() {
    }

    public static SortFunction getInstance() {
        return instance;
    }

    @Override
    public Class[] getParameterTypes() {
        return pTypes;
    }

    @Override
    public String[] getParameterNames() {
        return new String[] { "arrayOrList", "loBound", "hiBound",
                "comparator" };
    }

    @Override
    public Object getDefaultValue(int arg) {
        switch (arg) {
        case 1:
            return ConvertUtils.ZERO_INT;
        case 2:
            return ConvertUtils.TRUE_INT; // -1
        case 3:
            return null;
        default:
            return NO_ARG;
        }
    }

    @Override
    public Class getReturnType() {
        return Object.class;
    }

    @Override
    public Object invoke(Object[] args) {
        Object seq = args[0];
        int lo = ConvertUtils.toInt(args[1]);
        int hi = ConvertUtils.toInt(args[2]);
        Comparator comp = (Comparator)args[3];

        Object[] result;
        Class c = seq.getClass();
        if (c.isArray()) {
            c = c.getComponentType();
            if (c.isPrimitive()) {
                if (hi < lo) {
                    hi = Array.getLength(seq);
                }
                switch (c.getName().charAt(0)) {
                case 'B':
                    Arrays.sort((byte[])seq, lo, hi);
                    break;
                case 'S':
                    Arrays.sort((short[])seq, lo, hi);
                    break;
                case 'C':
                    Arrays.sort((char[])seq, lo, hi);
                    break;
                case 'I':
                    Arrays.sort((int[])seq, lo, hi);
                    break;
                case 'J':
                    Arrays.sort((long[])seq, lo, hi);
                    break;
                case 'F':
                    Arrays.sort((float[])seq, lo, hi);
                    break;
                case 'D':
                    Arrays.sort((double[])seq, lo, hi);
                    break;
                default: // case 'Z':
                    // don't sort boolean arrays
                    break;
                }
                return seq;
            } // c.isPrimitive()
            result = (Object[])seq;
        } // c.isArray()
        else {
            if (seq instanceof List) {
                return sortList((List)seq, lo, hi, comp);
            }
            result = (Object[])HojoLib.toArray(seq, Object[].class,
                    Object.class, true);
        }

        if (hi < lo) {
            hi = result.length;
        }

        if (comp == null) {
            Arrays.sort(result, lo, hi);
        }
        else {
            Arrays.sort(result, lo, hi, comp);
        }
        return result;
    }

    public static List sortList(List l, int lo, int hi, Comparator comp) {
        // retreive an array of the elements to sort
        if (hi < lo) {
            // adjust the upper bound
            hi = l.size();
        }

        Object[] objs;
        boolean isRandomAccess = l instanceof RandomAccess;

        if (lo == 0 && hi == l.size()) {
            // use built-in conversion for the entire list
            objs = l.toArray();
        }
        else {
            // retreive the values manually
            objs = new Object[hi - lo];
            if (isRandomAccess) {
                // use random access
                for (int i = lo; i < hi;) {
                    objs[i - lo] = l.get(i++);
                }
            }
            else {
                // use sequential access
                ListIterator li = l.listIterator(lo);
                for (int i = 0; i < objs.length;) {
                    objs[i++] = li.next();
                }
            }
        } // list range

        // sort the array
        if (comp == null) {
            Arrays.sort(objs);
        }
        else {
            Arrays.sort(objs, comp);
        }

        // store the values back to the list
        if (isRandomAccess) {
            for (int i = lo; i < hi;) {
                l.set(i, objs[i++ - lo]);
            }
        }
        else {
            ListIterator li = l.listIterator(lo);
            for (int i = 0; i < objs.length;) {
                li.next();
                li.set(objs[i++]);
            }
        }

        return l;
    }
}
