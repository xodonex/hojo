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

import org.xodonex.hojo.lang.Type;
import org.xodonex.util.ConvertUtils;

public class CharSequenceType extends Type {

    private static final long serialVersionUID = 1L;

    private final static Type INSTANCE = new CharSequenceType();
    private final static Class REPR_CLASS = CharSequence.class;

    protected CharSequenceType() {
    }

    public static Type getInstance() {
        return INSTANCE;
    }

    @Override
    public Class toClass() {
        return REPR_CLASS;
    }

    @Override
    public int kind() {
        return TYP_CHAR_SEQUENCE;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public boolean contains(Type t) {
        if (t == this) {
            return true;
        }
        switch (t.kind()) {
        case TYP_NULL:
        case TYP_STRING:
        case TYP_STRINGBUFFER:
            return true;
        }

        return REPR_CLASS.isAssignableFrom(t.toClass());
    }

    @Override
    protected Object convert(Object o) {
        return ConvertUtils.toCharSequence(o);
    }

}
