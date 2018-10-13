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
 * Default implementation of a normal variable.
 */
public class NormalVar extends Variable {

    private static final long serialVersionUID = 1L;

    protected Type type;

    public NormalVar(Class type, Object value) {
        this(HojoLib.typeOf(type), value);
    }

    public NormalVar(Type type, Object value) {
        super(type.typeCast(value));
        this.type = type;
    }

    @Override
    protected Type getType0() {
        return type;
    }

    @Override
    public Object set(Object resolvent, Object value) throws HojoException {
        Object v = type.typeCast(value);
        if (v != value) {
            throw new HojoException(null, HojoException.ERR_INTERNAL,
                    new String[] {
                            "", "" },
                    null);
        }
        return this.value = v;
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        // fixme:
        return indent + "variable(" + type.toString(stx) + ", " + value + ")";
    }

    @Override
    public boolean isFinal() {
        return false;
    }
}
