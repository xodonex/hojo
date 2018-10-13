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
package org.xodonex.hojo.lang.func;

import java.lang.reflect.Array;
import java.util.List;

import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.StandardFunction;
import org.xodonex.util.ConvertUtils;

public final class IndexFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    private final static Class[] ARGTYPES = new Class[] { Object.class,
            Integer.TYPE };
    final static IndexFunction INSTANCE = new IndexFunction();

    private IndexFunction() {
    }

    public static IndexFunction getInstance() {
        return INSTANCE;
    }

    @Override
    public Class getReturnType() {
        return Object.class;
    }

    @Override
    public Class[] getParameterTypes() {
        return ARGTYPES;
    }

    @Override
    public Object invoke(Object[] args) {
        try {
            Object base = args[0];
            int index = ConvertUtils.toInt(args[1]);
            if (base instanceof CharSequence) {
                return new Character(((CharSequence)base).charAt(index));
            }
            else if (base instanceof List) {
                return ((List)base).get(index);
            }
            else if (base instanceof Object[]) {
                return ((Object[])base)[index];
            }
            else {
                return Array.get(base, index);
            }
        }
        catch (Throwable t) {
            throw HojoException.wrap(t);
        }
    }

}
