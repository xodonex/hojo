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
package org.xodonex.hojo.lang.expr;

import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.HojoSyntax;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.Type;
import org.xodonex.util.StringUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public class TypecastExpr extends UnaryExpr {

    private static final long serialVersionUID = 1L;

    protected Type t;

    public TypecastExpr(Type t, Expression arg) {
        super(arg);
        this.t = t;
    }

    // create a narrowing or widening conversion, if necessary
    public static Expression mkTypecast(Type t, Expression arg) {
        Type _t = arg.getType();
        if (t.isEquivalentTo(_t) &&
                (t.kind() != Type.TYP_NUMBER || t.toClass() == _t.toClass())) {
            // primitive and wrappers are equivalent, but should be able
            // to be cast
            return arg;
        }
        else {
            return new TypecastExpr(t, arg);
        }
    }

    // determine whether an implicit conversion is necessary
    public static boolean needConversion(Type t1, Type t2) {
        if (t2.kind() == Type.TYP_NULL || t1.isEquivalentTo(t2)) {
            return false;
        }
        else if (t1.contains(t2)) {
            // type casts for numeric conversions must be generated, if the
            // classes are not directly assignable
            return (t1.kind() == Type.TYP_NUMBER)
                    ? !t1.toClass().isAssignableFrom(t2.toClass())
                    : false;
        }
        else {
            return true;
        }
    }

    // create a narrowing conversion, if necessary
    public static Expression mkConversion(Type t, Expression arg) {
        return (needConversion(t, arg.getType())) ? new TypecastExpr(t, arg)
                : arg;
    }

    @Override
    public Object xeq(Environment env) throws HojoException {
        return t.typeCast(arg.xeq(env));
    }

    @Override
    public Class getTypeC() {
        return t.toClass();
    }

    @Override
    protected Type getType0() {
        return t;
    }

    @Override
    public Expression optimize(int level) {
        arg = arg.optimize(level);
        if (level >= 1) {
            if (getType().equals(arg.getType())) {
                return arg;
            }
        }
        return this;
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        return indent + stx.punctuators[PCT_IDX_LPAREN] +
                stx.punctuators[PCT_IDX_LPAREN] + t.toString(stx) +
                stx.punctuators[PCT_IDX_RPAREN] + arg.toString(stx, fmt, "") +
                stx.punctuators[PCT_IDX_RPAREN];
    }

    @Override
    protected String toString(HojoSyntax stx, StringUtils.Format fmt) {
        return null; // not used
    }

}
