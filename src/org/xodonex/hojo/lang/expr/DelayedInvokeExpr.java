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
public class DelayedInvokeExpr extends ArrayExpr {

    private static final long serialVersionUID = 1L;

    protected Expression base;
    protected String name;

    public DelayedInvokeExpr(Expression base, String name, Expression[] args) {
        super(args);
        this.base = base;
        this.name = name;
    }

    @Override
    public Object xeq(Environment env) {
        Object obj = base.xeq(env);
        Object[] args = new Object[exprs.length];
        Class[] types = new Class[exprs.length];

        for (int i = 0; i < exprs.length; i++) {
            if (exprs[i] != null) {
                types[i] = ((args[i] = exprs[i].xeq(env)) == null) ? null
                        : args[i].getClass();
            }
        }

        try {
            Method m = ReflectUtils.getMatchingMethod(obj.getClass(), name,
                    types);
            Object result = m.invoke(obj, args);
            return (m.getReturnType() == Void.TYPE) ? null : result;
        }
        catch (Exception e) {
            throw new HojoException(e);
        }
    }

    @Override
    public Expression linkVars(Environment env, short maxLvl) {
        DelayedInvokeExpr result = (DelayedInvokeExpr)super.linkVars(env,
                maxLvl);
        Expression base_ = base.linkVars(env, maxLvl);
        if (result != this) {
            result.base = base_;
            return result;
        }
        else if (base_ != base) {
            result = (DelayedInvokeExpr)clone();
            result.base = base_;
            return result;
        }
        else {
            return this;
        }
    }

    @Override
    protected Type getType0() {
        return HojoLib.OBJ_TYPE;
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        StringBuffer result = new StringBuffer(indent);
        result.append(base.toString(stx, fmt, ""));
        result.append(stx.operators[OP_IDX_DOT]);
        result.append(name);
        result.append(stx.punctuators[PCT_IDX_LPAREN]);
        toString(result, stx, fmt);
        return result.append(stx.punctuators[PCT_IDX_RPAREN]).toString();
    }

}
