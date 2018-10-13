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

import org.xodonex.hojo.HojoSyntax;
import org.xodonex.hojo.lang.Type;
import org.xodonex.util.ReflectUtils;

public abstract class ContainerType extends Type {

    private static final long serialVersionUID = 1L;

    @Override
    public final boolean isConstant() {
        return false;
    }

    @Override
    public String toString(HojoSyntax stx) {
        Class base = toClass();
        Class elem = elementClass();

        if (isNeutralElemType(elem)) {
            return super.toString(stx);
        }
        else {
            return stx.punctuators[PCT_IDX_LPAREN]
                    + ReflectUtils.className2Java(base) +
                    stx.punctuators[PCT_IDX_DELIMITER] + ' ' +
                    ReflectUtils.className2Java(elem) +
                    stx.punctuators[PCT_IDX_RPAREN];
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        ContainerType t = (ContainerType)obj;
        return (t.toClass() == toClass())
                && (t.elementClass() == elementClass());
    }

    @Override
    public int hashCode() {
        return toClass().hashCode() ^ elementClass().hashCode();
    }

    @Override
    public boolean contains(Type t) {
        if (!(t instanceof ContainerType)) {
            return false;
        }
        ContainerType ct = (ContainerType)t;
        return toClass().isAssignableFrom(ct.toClass()) &&
                elementClass().isAssignableFrom(ct.elementClass());
    }

    protected boolean isNeutralElemType(Class c) {
        return c == Object.class;
    }

}
