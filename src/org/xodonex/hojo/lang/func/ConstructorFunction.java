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

import java.lang.reflect.Constructor;

import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.StandardFunction;
import org.xodonex.util.ReflectUtils;

public final class ConstructorFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    private Constructor constr;

    protected ConstructorFunction() {
    }

    public ConstructorFunction(Constructor constr) {
        this.constr = constr;
    }

    public ConstructorFunction(Class cls, Class[] args)
            throws NullPointerException, NoSuchMethodException {
        this.constr = ReflectUtils.getMatchingConstructor(cls, args);
    }

    public Constructor getConstructor() {
        return constr;
    }

    public String getName() {
        return constr.getName();
    }

    @Override
    public Class[] getParameterTypes() {
        return constr.getParameterTypes();
    }

    @Override
    public int getArity() {
        return constr.getParameterTypes().length;
    }

    @Override
    public Class getReturnType() {
        return constr.getDeclaringClass();
    }

    @Override
    public Object invoke(Object[] arguments) {
        try {
            return constr.newInstance(arguments);
        }
        catch (Throwable t) {
            throw HojoException.wrap(t);
        }
    }

    /*
     * public String toString() { return "" + constr; }
     */

}
