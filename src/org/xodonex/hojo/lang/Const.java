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

import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.HojoSyntax;
import org.xodonex.hojo.lang.type.NullType;
import org.xodonex.util.ReflectUtils;
import org.xodonex.util.StringUtils;

/**
 * Code for constant expressions.
 */
public class Const extends Expression {

    private static final long serialVersionUID = 1L;

    public final static Const NULL = new Const(null) {
        private static final long serialVersionUID = 1L;

        @Override
        protected Type getType0() {
            return NullType.getInstance();
        }
    };

    public final static Const FALSE = new Const(Boolean.FALSE);
    public final static Const TRUE = new Const(Boolean.TRUE);
    public final static Const BYTE_0 = new Const(HojoLib.ZERO_BYTE);
    public final static Const SHORT_0 = new Const(HojoLib.ZERO_SHORT);
    public final static Const CHAR_0 = new Const(HojoLib.ZERO_CHAR);
    public final static Const INT_0 = new Const(HojoLib.ZERO_INT);
    public final static Const LONG_0 = new Const(HojoLib.ZERO_LONG);
    public final static Const FLOAT_0 = new Const(HojoLib.ZERO_FLOAT);
    public final static Const DOUBLE_0 = new Const(HojoLib.ZERO_DOUBLE);

    public final static Const ELLIPSIS = new Const(new Integer(-1));
    public final static Const NO_ARG = new Const(Function.NO_ARG);

    /**
     * This is used by the compiler as the code in the default-clause of a
     * switch statement.
     */
    public final static Const DEFAULT = new Const(new java.io.Serializable() {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean equals(Object o) {
            return true;
        }
    });

    public static String toString(Object value, HojoSyntax stx,
            StringUtils.Format fmt) {
        String s = StringUtils.any2String(value, fmt);
        if (value instanceof Number) {
            int pri = ReflectUtils.getPriority((Number)value);
            char c;
            if (pri != NUM_PRI_BAD && (c = stx.suffixes[pri]) != '\0') {
                return s + c;
            }
        }
        return s;
    }

    protected Object value;

    public Const(Object value) {
        this.value = value;
    }

    @Override
    public Object xeq(Environment env) {
        return value;
    }

    @Override
    public final boolean isConst() {
        return true;
    }

    @Override
    public Class getTypeC() {
        return (value == null) ? Object.class : value.getClass();
    }

    @Override
    protected Type getType0() {
        return HojoLib.typeOf(value);
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        return indent + toString(value, stx, fmt);
    }

    public Object getValue() {
        return value;
    }
}
