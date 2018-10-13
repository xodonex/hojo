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
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.xodonex.hojo.HojoConst;
import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.lang.Operator;
import org.xodonex.hojo.lang.Type;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.struct.iterator.ConcatIterator;

/**
 *
 * @author Henrik Lauritzen
 */
public final class AddOp extends AbstractOperator {

    private static final long serialVersionUID = 1L;

    private final static Operator INSTANCE = new AddOp();

    private AddOp() {
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
        case Type.TYP_NULL:
        case Type.TYP_VOID: // will produce a null value
            return HojoLib.numericOpType(HojoLib.INT_TYPE, args[1], false);
        case Type.TYP_ARRAY:
            if (args[0].elementClass() != byte.class) {
                return HojoLib.STRING_TYPE;
            }
            // fall through:
        case Type.TYP_NUMBER:
        case Type.TYP_CHAR:
        case Type.TYP_DATE:
            return HojoLib.numericOpType(args[0], args[1], false);
        case Type.TYP_STRING:
        case Type.TYP_STRINGBUFFER:
        case Type.TYP_COLLECTION:
        case Type.TYP_LIST:
        case Type.TYP_SET:
        case Type.TYP_MAP:
        case Type.TYP_ITERATOR:
            return args[0];
        default:
            return HojoLib.OBJ_TYPE;
        }
    }

    @Override
    public Object invoke(Object[] arguments) throws HojoException {
        Object obj = arguments[0];
        if (obj == null) {
            return HojoLib.add(ConvertUtils.ZERO_INT,
                    ConvertUtils.toNumber(arguments[1], true));
        }
        else if (obj instanceof Number) {
            return HojoLib.add((Number)arguments[0],
                    ConvertUtils.toNumber(arguments[1], true));
        }
        else if (obj instanceof Character || obj instanceof byte[] ||
                obj instanceof Date) {
            return HojoLib.add(ConvertUtils.toNumber(arguments[0], true),
                    ConvertUtils.toNumber(arguments[1], true));
        }
        else if (obj instanceof StringBuffer) {
            return HojoLib.add((StringBuffer)obj, arguments[1]);
        }
        else if (obj instanceof Collection) {
            return HojoLib.add((Collection)obj, arguments[1]);
        }
        else if (obj instanceof Map) {
            return HojoLib.add((Map)obj, ConvertUtils.toMap(arguments[1]));
        }
        else if (obj instanceof Iterator) {
            return new ConcatIterator((Iterator)obj,
                    ConvertUtils.toIterator(arguments[1]));
        }
        else {
            return HojoLib.add(ConvertUtils.toString(obj), arguments[1]);
        }
    }

    @Override
    protected int getOpIdx() {
        return HojoConst.OP_IDX_ADD;
    }

}
