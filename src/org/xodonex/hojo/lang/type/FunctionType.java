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
package org.xodonex.hojo.lang.type;

import java.lang.reflect.Method;

import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.HojoSyntax;
import org.xodonex.hojo.lang.Function;
import org.xodonex.hojo.lang.Type;

public class FunctionType extends Type {

    private static final long serialVersionUID = 1L;

    public final static Class[] VARIABLE_ARGS = {};

    private final static Type INSTANCE = new FunctionType();

    protected FunctionType() {
    }

    public static Type getInstance() {
        return INSTANCE;
    }

    @Override
    public Class toClass() {
        return Function.class;
    }

    @Override
    public final int kind() {
        return TYP_FUNCTION;
    }

    public Class[] getParameterTypes() {
        return VARIABLE_ARGS;
    }

    public Class getReturnType() {
        return Object.class;
    }

    @Override
    protected Object convert(Object o) {
        return HojoLib.toFunction(o);
    }

    @Override
    public String toString() {
        return toString(HojoSyntax.DEFAULT);
    }

    @Override
    public String toString(HojoSyntax stx) {
        return stx.types[1];
    }

    public static FunctionType typeOf(Method m) {
        return new GenericFunctionType(Function.class,
                m.getParameterTypes(), m.getReturnType());
    }

    public static FunctionType typeOf(Function f) {
        return new GenericFunctionType(f.getClass(),
                f.getParameterTypes(), f.getReturnType());
    }

}
