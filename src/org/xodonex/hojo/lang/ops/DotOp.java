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
package org.xodonex.hojo.lang.ops;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import org.xodonex.hojo.HojoConst;
import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.lang.HObject;
import org.xodonex.hojo.lang.Type;
import org.xodonex.util.ConvertUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public final class DotOp extends AbstractOperator {

    private static final long serialVersionUID = 1L;

    private final static Class[] parameterTypes = { Object.class,
            String.class };

    private String className;

    public DotOp(String className) {
        this.className = (className == null) ? null : className.intern();
    }

    @Override
    public Class[] getParameterTypes() {
        return parameterTypes;
    }

    @Override
    public int getArity() {
        return 2;
    }

    @Override
    public Class getReturnType() {
        return Object.class;
    }

    @Override
    public Type inferType(Type[] argTypes, Collection warnings) {
        // can't infer anything without the value of argument 1
        return HojoLib.OBJ_TYPE;
    }

    @Override
    public Object invoke(Object[] arguments) throws HojoException {
        Object base = arguments[0];
        String name = ConvertUtils.toString(arguments[1]);
        try {
            if (name.equals(className)) {
                return base.getClass();
            }
            else if ((base instanceof Map)
                    && (((Map)base).containsKey(name))) {
                return ((Map)base).get(name);
            }
            else if (base instanceof HObject) {
                return ((HObject)base).get(name).getValue();
            }
            else if (name.equals("length") && base.getClass().isArray()) {
                return new Integer(Array.getLength(base));
            }
            else {
                return (base instanceof Class ? (Class)base : base.getClass())
                        .getField(name).get(base);
            }
        }
        catch (Exception e) {
            throw HojoException.wrap(e);
        }
    }

    @Override
    protected int getOpIdx() {
        return HojoConst.OP_IDX_DOT;
    }
}
