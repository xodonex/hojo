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
import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.HojoSyntax;
import org.xodonex.util.StringUtils;

/**
 * Representation of a variable.
 */
public class Variable extends AbstractLValue {

    private static final long serialVersionUID = 1L;

    public final static Object MODIFY_FINAL = new Object();

    protected Object value;

    public Variable(Object value) {
        this.value = value;
    }

    @Override
    protected Type getType0() {
        return HojoLib.typeOf(value);
    }

    @Override
    public Object resolve(Environment env) throws HojoException {
        return null;
    }

    @Override
    public Object get(Object resolvent) throws HojoException {
        return value;
    }

    @Override
    public Object set(Object resolvent, Object value) throws HojoException {
        if (resolvent == MODIFY_FINAL) {
            // HACK: allow the value to be set in forward declarations
            // and in the counter of for-sequence statements
            return this.value = value;
        }
        else {
            throw new HojoException(null, HojoException.ERR_FINAL, null, null);
        }
    }

    @Override
    public Object xeq(Environment env) {
        return value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        // fixme:
        return indent + "variable(" + value + ")";
    }

    public boolean isFinal() {
        return true;
    }
}
