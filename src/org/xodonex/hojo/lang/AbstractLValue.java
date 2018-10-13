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
package org.xodonex.hojo.lang;

import org.xodonex.hojo.HojoException;

/**
 * Default implementation of the LValue interface.
 */
public abstract class AbstractLValue extends Expression implements LValue {

    private static final long serialVersionUID = 1L;

    @Override
    public final boolean isConst() {
        return false;
    }

    /*
     * Evaluate all subexpressions necessary to determine the storage location.
     *
     * @return the result of the subexpression evaluation.
     */
    @Override
    public abstract Object resolve(Environment env) throws HojoException;

    /**
     * Retreives the stored value, using a previously
     * {@link #resolve(Environment) resolved} location specification.
     */
    @Override
    public abstract Object get(Object resolvent) throws HojoException;

    /**
     * Stores a new value at the location pointed to by the given resolvent.
     *
     * @return the new value of this <code>LValue</code>.
     */
    @Override
    public abstract Object set(Object resolvent, Object value)
            throws HojoException;
}
