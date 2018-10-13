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

import java.util.List;

import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.StandardFunction;
import org.xodonex.hojo.lang.Function;
import org.xodonex.util.ConvertUtils;

public final class FoldrFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    private final static Class[] pTypes = { Function.class, Object.class,
            List.class };
    private final static FoldrFunction instance = new FoldrFunction();

    public static FoldrFunction getInstance() {
        return instance;
    }

    private FoldrFunction() {
    }

    @Override
    public Class[] getParameterTypes() {
        return pTypes;
    }

    @Override
    public String[] getParameterNames() {
        return new String[] { "operation", "startValue", "sequence" };
    }

    @Override
    public Class getReturnType() {
        return Object.class;
    }

    @Override
    public Object invoke(Object[] args) {
        Function f = HojoLib.toFunction(args[0]);
        Object[] as = { null, args[1] };
        List list = ConvertUtils.toList(args[2]);

        for (int i = list.size() - 1; i >= 0; i--) {
            as[0] = list.get(i);
            as[1] = f.invoke(f.validateArgs(as));
        }

        return as[1];
    }

}
