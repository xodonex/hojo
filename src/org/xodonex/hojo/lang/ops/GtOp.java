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

import java.util.Comparator;

import org.xodonex.hojo.HojoConst;
import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.lang.Operator;

/**
 *
 * @author Henrik Lauritzen
 */
public final class GtOp extends CompareOperator implements Comparator {

    private static final long serialVersionUID = 1L;

    private final static Operator INSTANCE = new GtOp();

    private GtOp() {
    }

    public static Operator getInstance() {
        return INSTANCE;
    }

    @Override
    protected Object invoke(Comparable c1, Comparable c2) {
        return HojoLib.gt(c1, c2) ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    public int compare(Object o1, Object o2) {
        return HojoLib.compareTo((Comparable)o2, (Comparable)o1);
    }

    @Override
    protected int getOpIdx() {
        return HojoConst.OP_IDX_GT;
    }

}
