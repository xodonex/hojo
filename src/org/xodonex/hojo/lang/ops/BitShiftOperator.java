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

import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.lang.Type;
import org.xodonex.util.ConvertUtils;

/**
 * Abstract superclass for binary boolean, bit, numeric or mixed bit/boolean
 * operators.
 *
 * @author Henrik Lauritzen
 */
public abstract class BitShiftOperator extends BinaryOperator {

    private static final long serialVersionUID = 1L;

    public static int TYPE_BIT = 0;
    public static int TYPE_NUM = 1;
    public static int TYPE_BOOL = 2;
    public static int TYPE_NUMBOOL = 3;

    @Override
    protected int getOpType() {
        return TYPE_BIT;
    }

    @Override
    public Class[] getParameterTypes() {
        return new Class[] { Number.class, int.class };
    }

    @Override
    public Type inferType(Type[] argTypes, Collection warnings) {
        return HojoLib.numericOpType(argTypes[0], true);
    }

    @Override
    public Object invoke(Object[] args) throws HojoException {
        return invoke(ConvertUtils.toBits(args[0]),
                ConvertUtils.toInt(args[1]));
    }

    protected abstract Number invoke(Number n, int bits);

}
