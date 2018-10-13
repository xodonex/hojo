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

import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.StandardFunction;
import org.xodonex.hojo.lang.Function;

public final class CompositeFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    private Function funcL, funcR;

    public CompositeFunction(Function l, Function r) {
        this.funcL = l;
        this.funcR = r;
    }

    @Override
    public Class[] getParameterTypes() {
        return funcR.getParameterTypes();
    }

    @Override
    public int getArity() {
        return funcR.getArity();
    }

    @Override
    public Class getReturnType() {
        return funcL.getReturnType();
    }

    @Override
    public Object invoke(Object[] arguments) {
        return funcL.invoke((Object[])HojoLib.toArray(funcR.invoke(arguments),
                Object[].class, Object.class, false));
    }

}
