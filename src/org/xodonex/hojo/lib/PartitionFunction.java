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
package org.xodonex.hojo.lib;

import java.util.Iterator;
import java.util.List;

import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.StandardFunction;
import org.xodonex.hojo.lang.Function;
import org.xodonex.util.ConvertUtils;

public final class PartitionFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    private final static PartitionFunction instance = new PartitionFunction();

    private PartitionFunction() {
    }

    /*
     * ******************************* Function *******************************
     */

    public static PartitionFunction getInstance() {
        return instance;
    }

    @Override
    public Class[] getParameterTypes() {
        return LISTFUNC_ARGS;
    }

    @Override
    public String[] getParameterNames() {
        return new String[] { "predicate", "sequence" };
    }

    @Override
    public Class getReturnType() {
        return List[].class;
    }

    @Override
    public Object invoke(Object[] args) {
        Function pred = HojoLib.toFunction(args[0]);
        Iterator it = ConvertUtils.toIterator(args[1]);
        List[] result = { ConvertUtils.newList(), ConvertUtils.newList() };

        Object[] as = new Object[1];
        while (it.hasNext()) {
            as[0] = it.next();
            result[ConvertUtils.toBool(pred.invoke(pred.validateArgs(as))) ? 0
                    : 1].add(as[0]);
        }

        return result;
    }

}
