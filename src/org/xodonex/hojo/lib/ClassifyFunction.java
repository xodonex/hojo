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
import java.util.Map;

import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.StandardFunction;
import org.xodonex.hojo.lang.Function;
import org.xodonex.util.ConvertUtils;

public final class ClassifyFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    private final static Class[] pTypes = { Function.class, Iterator.class,
            Map.class };
    private final static ClassifyFunction instance = new ClassifyFunction();

    private ClassifyFunction() {
    }

    /*
     * ******************************* Function *******************************
     */

    public static ClassifyFunction getInstance() {
        return instance;
    }

    @Override
    public Class[] getParameterTypes() {
        return pTypes;
    }

    @Override
    public String[] getParameterNames() {
        return new String[] { "classifier", "sequence", "container" };
    }

    @Override
    public Class getReturnType() {
        return Map.class;
    }

    @Override
    public Object getDefaultValue(int arg) {
        switch (arg) {
        case 2:
            return null;
        default:
            return NO_ARG;
        }
    }

    @Override
    public Object invoke(Object[] args) {
        Function f = HojoLib.toFunction(args[0]);
        Iterator it = ConvertUtils.toIterator(args[1]);
        Map m = ConvertUtils.toMap(args[2]);
        if (m == null) {
            m = ConvertUtils.newMap();
        }

        Object[] params = new Object[1];

        while (it.hasNext()) {
            Object obj = params[0] = it.next();
            Object key = f.invoke(f.validateArgs(params));

            Collection c = (Collection)m.get(key);
            if (c == null) {
                m.put(key, c = ConvertUtils.newCollection());
            }

            c.add(obj);
        }

        return m;
    }

}
