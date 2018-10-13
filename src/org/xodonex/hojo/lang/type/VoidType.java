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
package org.xodonex.hojo.lang.type;

import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.HojoSyntax;
import org.xodonex.hojo.lang.Type;
import org.xodonex.util.ReflectUtils;

public final class VoidType extends Type {

    private static final long serialVersionUID = 1L;

    private final static Type INSTANCE = new VoidType();

    private VoidType() {
    }

    public static Type getInstance() {
        return INSTANCE;
    }

    @Override
    public int kind() {
        return TYP_VOID;
    }

    @Override
    public Class toClass() {
        return Void.TYPE;
    }

    @Override
    public boolean contains(Type t) {
        return t == this;
    }

    @Override
    public Type union(Type t) {
        if (t == this) {
            return this;
        }
        else {
            throw new HojoException(null, HojoException.ERR_TYPE, new String[] {
                    ReflectUtils.className2Java(toClass()),
                    ReflectUtils.className2Java(t.toClass())
            }, null);
        }
    }

    @Override
    protected Object convert(Object o) {
        if (o == null) {
            return o;
        }
        throw new HojoException(null, HojoException.ERR_TYPE, new String[] {
                ReflectUtils.className2Java(toClass()),
                ReflectUtils.className2Java(o.getClass())
        }, null);
    }

    @Override
    public String toString(HojoSyntax stx) {
        return stx.types[TYP_IDX_VOID];
    }

}
