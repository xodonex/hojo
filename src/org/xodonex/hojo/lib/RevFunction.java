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
import java.util.LinkedList;
import java.util.List;

import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.StandardFunction;
import org.xodonex.util.ArrayUtils;

public final class RevFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    private final static RevFunction instance = new RevFunction();

    private RevFunction() {
    }

    /*
     * ******************************* Function *******************************
     */

    public static RevFunction getInstance() {
        return instance;
    }

    @Override
    public Class[] getParameterTypes() {
        return ONE_ARG;
    }

    @Override
    public String[] getParameterNames() {
        return new String[] { "sequence" };
    }

    @Override
    public Class getReturnType() {
        return Object.class;
    }

    @Override
    public Object invoke(Object[] args) {
        Object o = args[0];
        if (o instanceof String) {
            return new StringBuffer(o.toString()).reverse().toString();
        }
        else if (o instanceof StringBuffer) {
            ((StringBuffer)o).reverse();
            return o;
        }
        else if (o instanceof Iterator) {
            Iterator i = (Iterator)o;
            LinkedList result = new LinkedList();
            while (i.hasNext()) {
                result.addFirst(i.next());
            }
            return result;
        }
        else if (o instanceof List) {
            List l = (List)o;
            for (int i = 0, j = l.size() - 1; i < j; i++, j--) {
                Object tmp = l.set(i, l.get(j));
                l.set(j, tmp);
            }
            return l;
        }

        Class c = o.getClass();
        if (c.isArray()) {
            c = c.getComponentType();
            if (c.isPrimitive()) {
                char type = c.getName().charAt(0);

                switch (type) {
                case 'B':
                    ArrayUtils.reverseRange((byte[])o, 0, -1);
                    break;
                case 'S':
                    ArrayUtils.reverseRange((short[])o, 0, -1);
                    break;
                case 'C':
                    ArrayUtils.reverseRange((char[])o, 0, -1);
                    break;
                case 'I':
                    ArrayUtils.reverseRange((int[])o, 0, -1);
                    break;
                case 'J':
                    ArrayUtils.reverseRange((long[])o, 0, -1);
                    break;
                case 'F':
                    ArrayUtils.reverseRange((float[])o, 0, -1);
                    break;
                case 'D':
                    ArrayUtils.reverseRange((double[])o, 0, -1);
                    break;
                default: // case 'Z':
                    ArrayUtils.reverseRange((boolean[])o, 0, -1);
                    break;
                }
                return o;
            } // c.isPrimitive()
        } // c.isArray()

        Object[] result = (Object[])HojoLib.toArray(o, Object[].class,
                Object.class, true);
        ArrayUtils.reverseRange(result, 0, -1);
        return result;
    }

}
