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
import java.util.Iterator;

import org.xodonex.hojo.HojoConst;
import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.lang.Function;
import org.xodonex.hojo.lang.Operator;
import org.xodonex.hojo.lang.Type;

/**
 *
 * @author Henrik Lauritzen
 */
public final class SeqOp extends AbstractOperator {

    private static final long serialVersionUID = 1L;

    private final static Operator INSTANCE = new SeqOp();

    private SeqOp() {
    }

    public static Operator getInstance() {
        return INSTANCE;
    }

    @Override
    public Class[] getParameterTypes() {
        return new Class[] { Comparable.class, Comparable.class,
                Function.class };
    }

    @Override
    public int getArity() {
        return 3;
    }

    @Override
    public Object getDefaultValue(int arg) {
        return arg == 2 ? null : NO_ARG;
    }

    @Override
    public Class getReturnType() {
        return Iterator.class;
    }

    @Override
    public Type inferType(Type[] args, Collection warnings) {
        return HojoLib.typeOf(Iterator.class);
    }

    @Override
    public Object invoke(Object[] arguments) throws HojoException {
        return HojoLib.seq((Comparable)arguments[0],
                (Comparable)arguments[1],
                HojoLib.toFunction(arguments[2]));
    }

    @Override
    protected int getOpIdx() {
        return HojoConst.OP_IDX_SEQ;
    }

}
