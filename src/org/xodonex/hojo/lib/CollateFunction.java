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

import java.text.Collator;
import java.util.Comparator;

import org.xodonex.hojo.StandardFunction;
import org.xodonex.util.ConvertUtils;

public class CollateFunction extends StandardFunction implements Comparator {

    private static final long serialVersionUID = 1L;

    private final static CollateFunction INSTANCE = new CollateFunction();

    private Class[] argTypes = { String.class, String.class };
    private Collator coll = Collator.getInstance();

    private CollateFunction() {
    }

    public static CollateFunction getInstance() {
        return INSTANCE;
    }

    @Override
    public Class[] getParameterTypes() {
        return argTypes;
    }

    @Override
    public Class getReturnType() {
        return Integer.class;
    }

    @Override
    public Object invoke(Object[] args) {
        return new Integer(
                coll.compare(ConvertUtils.toString(args[0]),
                        ConvertUtils.toString(args[1])));
    }

    @Override
    public int compare(Object o1, Object o2) {
        return coll.compare(o1, o2);
    }

}
