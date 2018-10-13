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
import org.xodonex.hojo.HojoSyntax;

/**
 *
 * @author Henrik Lauritzen
 */
public final class SourceOp extends UnaryOperator {

    private static final long serialVersionUID = 1L;

    HojoSyntax _stx;

    public SourceOp(HojoSyntax stx) {
        if ((_stx = stx) == null) {
            throw new NullPointerException();
        }
    }

    // unused
    @Override
    protected int getOpType() {
        return -1;
    }

    @Override
    public Class[] getParameterTypes() {
        return ONE_ARG;
    }

    @Override
    public Class getReturnType() {
        return StringBuffer.class;
    }

    @Override
    public Object invoke(Object[] arguments) throws HojoException {
        return _stx.toHojoString(arguments[0], null, "");
    }

    @Override
    protected int getOpIdx() {
        return HojoConst.OP_IDX_SOURCE;
    }

}
