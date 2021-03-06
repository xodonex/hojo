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

import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.StandardFunction;
import org.xodonex.hojo.lang.Function;
import org.xodonex.util.ConvertUtils;

public final class AppFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    private final static AppFunction instance = new AppFunction();

    private AppFunction() {
    }

    public static AppFunction getInstance() {
        return instance;
    }

    @Override
    public Class[] getParameterTypes() {
        return LISTFUNC_ARGS;
    }

    @Override
    public String[] getParameterNames() {
        return new String[] { "operation", "sequence" };
    }

    @Override
    public Class getReturnType() {
        return Void.TYPE;
    }

    @Override
    public Object invoke(Object[] args) {
        Function f = HojoLib.toFunction(args[0]);
        Iterator it = ConvertUtils.toIterator(args[1]);
        Object[] as = new Object[1];

        if (f.getArity() == 0) {
            // allow no-arg function
            while (it.hasNext()) {
                it.next();
                f.invoke(Function.UNIT);
            }
        }
        else {
            while (it.hasNext()) {
                as[0] = it.next();
                f.invoke(f.validateArgs(as));
            }
        }
        return null;
    }

}
