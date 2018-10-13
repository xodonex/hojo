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

import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.StandardFunction;
import org.xodonex.hojo.lang.Code;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.Type;
import org.xodonex.hojo.lang.env.Env;
import org.xodonex.hojo.util.ReturnException;

public class HojoFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    private String[] names;
    private Class[] types;
    private Object[] defaults;
    private String extraName;
    private Code body;
    private Type retType; // retType == null <=> body instanceof Expression
    private short size;

    public HojoFunction(String[] names, Class[] types, Object[] defaults,
            String extraName, Type retType, Code body, short size) {
        this.names = names;
        this.types = types;
        this.defaults = defaults;
        this.extraName = extraName;
        this.retType = retType;
        this.body = body;
        this.size = size;
    }

    @Override
    public Class[] getParameterTypes() {
        return types;
    }

    @Override
    public String[] getParameterNames() {
        return names;
    }

    @Override
    public String getExtraParameterName() {
        return extraName;
    }

    @Override
    public int getArity() {
        return types.length;
    }

    @Override
    public Object getDefaultValue(int arg) {
        return defaults[arg];
    }

    @Override
    public Class getReturnType() {
        return (retType == null) ? ((Expression)body).getTypeC()
                : retType.toClass();
    }

    @Override
    public Object invoke(Object[] arguments) {
        // create a new environment for the function. Because all variables of
        // level x < this.level have been linked, the parent of this environment
        // may be null.
        Environment env = new Env(null, size);

        for (int i = 0; i < types.length; i++) {
            // add the parameters to the new environment
            env.alloc((short)i, false, null, HojoLib.typeOf(types[i]),
                    arguments[i]);
        }
        if (extraName != null) {
            env.alloc((short)types.length, true, extraName, HojoLib.TUPLE_TYPE,
                    arguments[types.length]);
        }

        // execute the body in the new environment
        Object result;
        try {
            result = body.xeq(env);
        }
        catch (ReturnException e) {
            result = e.getValue();
        }

        // return the result or this function, if the return type is void
        if (retType == null) {
            return result;
        }
        else if (retType.isVoid()) {
            return this;
        }
        else {
            if (retType.typeCast(result) != result) {
                throw new HojoException();
            }
            return result;
        }
    }

}
