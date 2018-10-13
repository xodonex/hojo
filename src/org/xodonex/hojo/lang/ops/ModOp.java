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
public final class ModOp extends BinaryOperator {

    private static final long serialVersionUID = 1L;

    private final static Operator INSTANCE = new ModOp();

    private ModOp() {
    }

    public static Operator getInstance() {
        return INSTANCE;
    }

    @Override
    protected int getOpType() {
        return TYPE_NUM;
    }

    @Override
    public Type inferType(Type[] args, Collection warnings) {
        NumberType t = HojoLib.numericOpType(args[0], args[1], false);

        if (t == null) {
            return HojoLib.NUMBER_TYPE;
        }
        else if (t.numberType() > HojoConst.NUM_PRI_BINT) {
            return HojoLib.typeOf(java.math.BigInteger.class);
        }
        else {
            return t;
        }
    }

    @Override
    public Object invoke(Object[] arguments) throws HojoException {
        return HojoLib.mod(ConvertUtils.toNumber(arguments[0], true),
                ConvertUtils.toNumber(arguments[1], true));
    }

    @Override
    protected int getOpIdx() {
        return HojoConst.OP_IDX_MOD;
    }

}
