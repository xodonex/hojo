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

import java.lang.reflect.Method;

import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.HojoSyntax;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.Type;
import org.xodonex.util.ReflectUtils;
import org.xodonex.util.StringUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public class InvokeExpr extends ArrayExpr {

    private static final long serialVersionUID = 1L;

    protected Expression base;
    protected Method m;

    public InvokeExpr(Expression base, Method m, Expression[] args) {
        super(args);
        this.base = base;
        this.m = m;
    }

    @Override
    public Object xeq(Environment env) {
        Object obj = base == null ? null : base.xeq(env);
        Object[] args = new Object[exprs.length];
        for (int i = 0; i < exprs.length; i++) {
            args[i] = exprs[i] == null ? null : exprs[i].xeq(env);
        }

        try {
            Object result = m.invoke(obj, args);
            return (m.getReturnType() == Void.TYPE) ? null : result;
        }
        catch (Exception e) {
            throw HojoException.wrap(e, this);
        }
    }

    @Override
    public Expression linkVars(Environment env, short maxLvl) {
        InvokeExpr result = (InvokeExpr)super.linkVars(env, maxLvl);
        Expression base_ = base == null ? null : base.linkVars(env, maxLvl);
        if (result != this) {
            result.base = base_;
            return result;
        }
        else if (base_ != base) {
            result = (InvokeExpr)clone();
            result.base = base_;
            return result;
        }
        else {
            return this;
        }
    }

    @Override
    public boolean isJavaStatement() {
        return true;
    }

    @Override
    protected Type getType0() {
        if (base != null && m.getName().equals("clone") &&
                m.getParameterTypes().length == 0) {
            // don't lose type information when .clone() is called
            return base.getType();
        }

        Class c = m.getReturnType();
        /*
         * return (c == void.TYPE) ? (base == null ? VoidType.getInstance() :
         * base.getType()) : HojoLib.typeOf(c);
         */
        return HojoLib.typeOf(c);
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        StringBuffer result = new StringBuffer(indent);
        result.append(base == null
                ? ReflectUtils.className2Java(m.getDeclaringClass())
                : base.toString(stx, fmt, ""));
        result.append(stx.operators[OP_IDX_DOT]);
        result.append(m.getName());
        result.append(stx.punctuators[PCT_IDX_LPAREN]);
        toString(result, stx, fmt);
        return result.append(stx.punctuators[PCT_IDX_RPAREN]).toString();
    }

}
