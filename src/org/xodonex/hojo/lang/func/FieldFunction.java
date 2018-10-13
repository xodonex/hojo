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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.StandardFunction;

public class FieldFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    protected Field field;
    protected Class[] argTypes;
    protected boolean isStatic;

    public FieldFunction(Field field) {
        this.field = field;
        isStatic = (field.getModifiers() & Modifier.STATIC) != 0;
        if (isStatic) {
            argTypes = NO_ARGS;
        }
        else {
            argTypes = new Class[] { field.getDeclaringClass() };
        }
    }

    public Field getField() {
        return field;
    }

    @Override
    public Class[] getParameterTypes() {
        return argTypes;
    }

    @Override
    public int getArity() {
        return isStatic ? 0 : 1;
    }

    @Override
    public Class getReturnType() {
        return field.getType();
    }

    @Override
    public Object invoke(Object[] arguments) {
        try {
            if (isStatic) {
                return field.get(null);
            }
            else {
                return field.get(arguments[0]);
            }
        }
        catch (Throwable t) {
            throw HojoException.wrap(t);
        }
    }

    public String getName() {
        return field.getName();
    }

}
