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

import java.util.Map;
import java.util.regex.Pattern;

import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.StandardFunction;
import org.xodonex.util.ConvertUtils;

public final class MkLibFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    private final static MkLibFunction instance = new MkLibFunction();

    private MkLibFunction() {
    }

    public static MkLibFunction getInstance() {
        return instance;
    }

    @Override
    public Class[] getParameterTypes() {
        return new Class[] { Object.class, Pattern.class };
    }

    @Override
    public String[] getParameterNames() {
        return new String[] { "classOrObject", "mask" };
    }

    @Override
    public Object getDefaultValue(int arg) {
        switch (arg) {
        case 1:
            return null;
        default:
            return NO_ARG;
        }
    }

    @Override
    public Class getReturnType() {
        return Map.class;
    }

    @Override
    public Object invoke(Object[] arguments) {
        if (arguments[0] == null) {
            return null;
        }

        Pattern p = ConvertUtils.toPattern(arguments[1]);
        return HojoLib.mkLib(arguments[0], p);
    }

}
