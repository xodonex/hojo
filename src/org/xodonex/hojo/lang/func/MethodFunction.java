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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.StandardFunction;

public class MethodFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    protected Method method;
    protected Class[] argTypes;
    protected boolean isStatic;

    public MethodFunction(Method method) {
        this.method = method;
        Class[] atypes = method.getParameterTypes();
        isStatic = (method.getModifiers() & Modifier.STATIC) != 0;
        if (isStatic) {
            argTypes = atypes;
        }
        else {
            argTypes = new Class[atypes.length + 1];
            argTypes[0] = method.getDeclaringClass();
            System.arraycopy(atypes, 0, argTypes, 1, atypes.length);
        }
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public Class[] getParameterTypes() {
        return argTypes;
    }

    @Override
    public int getArity() {
        return argTypes.length;
    }

    @Override
    public Class getReturnType() {
        return method.getReturnType();
    }

    @Override
    public Object invoke(Object[] arguments) {
        try {
            if (isStatic) {
                return method.invoke(null, arguments);
            }
            else {
                Object[] args = new Object[arguments.length - 1];
                System.arraycopy(arguments, 1, args, 0, args.length);
                return method.invoke(arguments[0], args);
            }
        }
        catch (Throwable t) {
            throw HojoException.wrap(t);
        }
    }

    public String getName() {
        return method.getName();
    }

}
