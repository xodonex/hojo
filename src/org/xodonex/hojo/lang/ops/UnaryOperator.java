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

import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.lang.Type;

/**
 *
 * @author Henrik Lauritzen
 */
public abstract class UnaryOperator extends AbstractOperator {

    private static final long serialVersionUID = 1L;

    public static int TYPE_BIT = 0;
    public static int TYPE_NUM = 1;
    public static int TYPE_BOOL = 2;

    protected abstract int getOpType();

    @Override
    public final int getArity() {
        return 1;
    }

    @Override
    public Class[] getParameterTypes() {
        int type = getOpType();
        if (type < TYPE_BOOL) {
            return NUMBER_ARG;
        }
        else if (type == TYPE_BOOL) {
            return BOOLEAN_ARG;
        }
        else {
            return ONE_ARG;
        }
    }

    @Override
    public Class getReturnType() {
        int type = getOpType();
        if (type < TYPE_BOOL) {
            return Number.class;
        }
        else if (type == TYPE_BOOL) {
            return Boolean.class;
        }
        else {
            return Object.class;
        }
    }

    @Override
    public Type inferType(Type[] argTypes, Collection warnings) {
        int typ = getOpType();
        if (typ < TYPE_BOOL) {
            return HojoLib.numericOpType(argTypes[0], typ == TYPE_BIT);
        }
        else if (typ == TYPE_BOOL) {
            return HojoLib.BOOLEAN_TYPE;
        }
        else {
            return HojoLib.typeOf(getReturnType());
        }
    }

}
