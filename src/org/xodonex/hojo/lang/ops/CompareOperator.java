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

/**
 * Abstract superclass for operators of type
 * <code>(Comparable, Comparable) -&gt; Comparable</code>
 */
public abstract class CompareOperator extends AbstractOperator {

    private static final long serialVersionUID = 1L;

    public final static Class[] ARGTYPES = { Comparable.class,
            Comparable.class };

    @Override
    public Class[] getParameterTypes() {
        return ARGTYPES;
    }

    @Override
    public Class getReturnType() {
        return Boolean.class;
    }

    @Override
    public Type inferType(Type[] argTypes, Collection warnings) {
        return HojoLib.BOOLEAN_TYPE;
    }

    @Override
    public Object invoke(Object[] args) throws HojoException {
        Object badObj = args[0];
        Comparable c1;
        Comparable c2;

        try {
            c1 = (Comparable)args[0];
            badObj = args[1];
            c2 = (Comparable)args[1];
        }
        catch (ClassCastException e) {
            throw new HojoException(e, HojoException.ERR_TYPE,
                    new String[] { "java.lang.Comparable",
                            badObj.getClass().getName() },
                    null);
        }

        return invoke(c1, c2);
    }

    protected abstract Object invoke(Comparable c1, Comparable c2);

}
