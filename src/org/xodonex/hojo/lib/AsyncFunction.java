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

import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.StandardFunction;
import org.xodonex.hojo.lang.Function;
import org.xodonex.util.ConvertUtils;

public final class AsyncFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    private final static Class[] pTypes = { Function.class, Object[].class,
            Boolean.class };
    private final static AsyncFunction instance = new AsyncFunction();

    private AsyncFunction() {
    }

    public static AsyncFunction getInstance() {
        return instance;
    }

    @Override
    public Class[] getParameterTypes() {
        return pTypes;
    }

    @Override
    public String[] getParameterNames() {
        return new String[] { "operation", "arguments", "startImmediately" };
    }

    @Override
    public Object getDefaultValue(int arg) {
        switch (arg) {
        case 2:
            return Boolean.TRUE;
        default:
            return NO_ARG;
        }
    }

    @Override
    public Class getReturnType() {
        return AsyncExecutor.class;
    }

    @Override
    public Object invoke(Object[] arguments) {
        Function f = HojoLib.toFunction(arguments[0]);
        Object[] args = (Object[])ConvertUtils.toArray(arguments[1],
                Object[].class);
        boolean start = ConvertUtils.toBool(arguments[2]);

        AsyncExecutor result = new AsyncExecutor(f, args);
        if (start) {
            result.start();
        }
        return result;
    }

}
