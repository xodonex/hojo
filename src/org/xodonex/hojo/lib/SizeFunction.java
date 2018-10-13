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

import java.io.File;
import java.lang.reflect.Array;
import java.util.Collection;

import org.xodonex.hojo.StandardFunction;

public final class SizeFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    private final static SizeFunction instance = new SizeFunction();

    public static SizeFunction getInstance() {
        return instance;
    }

    private SizeFunction() {
    }

    @Override
    public Class[] getParameterTypes() {
        return ONE_ARG;
    }

    @Override
    public Class getReturnType() {
        return Long.class;
    }

    @Override
    public Object invoke(Object[] arguments) {
        Object tmp = arguments[0];
        long result;

        if (tmp == null) {
            result = 0;
        }
        else if (tmp instanceof Object[]) {
            result = ((Object[])tmp).length;
        }
        else if (tmp instanceof Collection) {
            result = ((Collection)tmp).size();
        }
        else if (tmp instanceof CharSequence) {
            result = ((CharSequence)tmp).length();
        }
        else if (tmp instanceof File) {
            result = ((File)tmp).length();
        }
        else {
            // may throw an exception
            result = Array.getLength(tmp);
        }

        return new Long(result);
    }

}
