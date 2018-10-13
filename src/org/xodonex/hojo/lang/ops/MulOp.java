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

import java.util.Collection;
import java.util.Set;

import org.xodonex.hojo.HojoConst;
import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.lang.Operator;
import org.xodonex.hojo.lang.Type;
import org.xodonex.hojo.lang.type.NumberType;
import org.xodonex.util.ConvertUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public final class MulOp extends AbstractOperator {

    private static final long serialVersionUID = 1L;

    private final static Operator INSTANCE = new MulOp();

    private MulOp() {
    }

    public static Operator getInstance() {
        return INSTANCE;
    }

    @Override
    public Class[] getParameterTypes() {
        return TWO_ARGS;
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
    public Type inferType(Type[] args, Collection warnings) {
        switch (args[0].kind()) {
        case Type.TYP_STRING:
        case Type.TYP_STRINGBUFFER:
        case Type.TYP_COLLECTION:
        case Type.TYP_LIST:
            return args[0];
        case Type.TYP_NUMBER:
            NumberType t = HojoLib.numericOpType(args[0], args[1], false);
            if (t == null) {
                return HojoLib.NUMBER_TYPE;
            }
            else {
                return t;
            }
        case Type.TYP_ARRAY:
            if (!args[0].arrayElemClass().isPrimitive()) {
                return HojoLib.typeOf(Object[][].class);
            }
            // fall through:
        default:
            return HojoLib.OBJ_TYPE;
        }
    }

    @Override
    public Object invoke(Object[] arguments) throws HojoException {
        Object arg1 = arguments[0];

        if (arg1 instanceof Object[]) {
            return HojoLib.mul((Object[])arg1, (Object[])HojoLib.toArray(
                    arguments[1], Object[].class, Object.class, false));
        }
        else if (arg1 instanceof String || arg1 instanceof StringBuffer) {
            String s = ConvertUtils.toString(arg1);
            int count = ConvertUtils.toInt(arguments[1]);
            if (count <= 0) {
                return "";
            }
            StringBuffer buf = new StringBuffer(count * s.length());
            while (count-- > 0) {
                buf.append(s);
            }
            if (arg1 instanceof StringBuffer) {
                return buf;
            }
            else {
                return buf.toString();
            }
        }
        else if (arg1 instanceof Collection && !(arg1 instanceof Set)) {
            Collection coll = (Collection)arg1;
            try {
                Collection result = coll.getClass().newInstance();
                int count = ConvertUtils.toInt(arguments[1]);
                while (count-- > 0) {
                    result.addAll(coll);
                }
                return result;
            }
            catch (Exception e) {
                throw HojoException.wrap(e);
            }
        }
        else {
            return HojoLib.mul(ConvertUtils.toNumber(arg1, true),
                    ConvertUtils.toNumber(arguments[1], true));
        }
    }

    @Override
    protected int getOpIdx() {
        return HojoConst.OP_IDX_MUL;
    }

}
