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

import org.xodonex.hojo.HojoConst;
import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.lang.Operator;

/**
 *
 * @author Henrik Lauritzen
 */
public final class ShlOp extends BitShiftOperator {

    private static final long serialVersionUID = 1L;

    private final static Operator INSTANCE = new ShlOp();

    private ShlOp() {
    }

    public static Operator getInstance() {
        return INSTANCE;
    }

    @Override
    public Number invoke(Number n, int bits) throws HojoException {
        return HojoLib.shl(n, bits);
    }

    @Override
    protected int getOpIdx() {
        return HojoConst.OP_IDX_SHL;
    }

}
