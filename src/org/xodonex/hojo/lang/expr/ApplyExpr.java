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

import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.HojoSyntax;
import org.xodonex.hojo.lang.Const;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.Function;
import org.xodonex.hojo.lang.Operator;
import org.xodonex.hojo.lang.Type;
import org.xodonex.hojo.lang.type.FunctionType;
import org.xodonex.util.StringUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public class ApplyExpr extends ArrayExpr {

    private static final long serialVersionUID = 1L;

    protected Expression func;
    protected Type typ;
    protected transient Type[] argTypes = null;

    public ApplyExpr(Expression func, Expression[] exprs) {
        super(exprs);
        this.func = func;
        Type t = func.getType();
        typ = HojoLib.typeOf(((t.kind() == Type.TYP_FUNCTION) ? (FunctionType)t
                : (FunctionType)FunctionType.getInstance()).getReturnType());

        if (func instanceof Const) {
            Function f = (Function)func.xeq(null);
            if (f instanceof Operator) {
                typ = ((Operator)f).inferType(getArgTypes(), null);
            }
        }
    }

    private static Type[] createTypes(Expression[] exprs) {
        Type[] result = new Type[exprs.length];
        for (int i = exprs.length - 1; i >= 0; i--) {
            result[i] = exprs[i].getType();
        }
        return result;
    }

    protected Type[] getArgTypes() {
        if (argTypes == null) {
            argTypes = createTypes(exprs);
        }
        return argTypes;
    }

    @Override
    public Expression linkVars(Environment env, short maxLvl) {
        ApplyExpr result = (ApplyExpr)super.linkVars(env, maxLvl);
        Expression func_ = func.linkVars(env, maxLvl);
        if (result != this) {
            result.func = func_;
            return result;
        }
        else if (func_ != func) {
            result = (ApplyExpr)clone();
            result.func = func_;
            return result;
        }
        else {
            return this;
        }
    }

    @Override
    public Object xeq(Environment env) {
        Function f = HojoLib.toFunction(func.xeq(env));
        boolean isVoid = f.getReturnType() == Void.TYPE;

        Object[] args = new Object[exprs.length];
        for (int i = 0; i < exprs.length; i++) {
            args[i] = exprs[i].xeq(env);
        }

        Object result = f.invoke(f.validateArgs(args));
        return isVoid ? null : result;
    }

    @Override
    public boolean isJavaStatement() {
        return true;
    }

    @Override
    protected Type getType0() {
        return typ;
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        StringBuffer result = new StringBuffer(indent);
        result.append(stx.punctuators[PCT_IDX_LPAREN]);
        result.append(func.toString(stx, fmt, ""));
        result.append(stx.punctuators[PCT_IDX_RPAREN]);
        result.append(stx.punctuators[PCT_IDX_LPAREN]);
        toString(result, stx, fmt);
        return result.append(stx.punctuators[PCT_IDX_RPAREN]).toString();
    }

}
