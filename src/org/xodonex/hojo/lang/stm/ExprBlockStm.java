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
package org.xodonex.hojo.lang.stm;

import org.xodonex.hojo.HojoSyntax;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.Statement;
import org.xodonex.hojo.lang.Type;
import org.xodonex.hojo.lang.UnreachableStatementException;
import org.xodonex.util.StringUtils;

public abstract class ExprBlockStm extends Statement {

    private static final long serialVersionUID = 1L;

    protected Expression expr;
    protected Statement block;

    protected ExprBlockStm(Expression expr, Statement block) {
        this.expr = expr;
        this.block = block;
    }

    @Override
    public Statement optimize(int level) {
        expr = expr.optimize(level);
        block = block == null ? null : block.optimize(level);
        return this;
    }

    @Override
    public Statement linkVars(Environment env, short maxLvl) {
        Expression expr_ = expr.linkVars(env, maxLvl);
        Statement block_ = (block == null) ? null : block.linkVars(env, maxLvl);

        if (expr_ != expr || block_ != block) {
            ExprBlockStm result = (ExprBlockStm)clone();
            result.expr = expr_;
            result.block = block_;
            return result;
        }
        else {
            return this;
        }
    }

    @Override
    public Type checkCode(Type rt) throws UnreachableStatementException {
        return block.checkCode(rt);
    }

    @Override
    public abstract Object run(Environment env) throws Throwable;

    @Override
    public abstract String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent);

    protected StringBuffer toString(String name, boolean useExpr,
            HojoSyntax stx, StringUtils.Format fmt, String indent) {
        StringBuffer result = new StringBuffer(indent).append(name).append(' ');
        if (useExpr) {
            result.append(stx.punctuators[PCT_IDX_LPAREN]);
            result.append(expr.toString(stx, fmt, ""));
            result.append(stx.punctuators[PCT_IDX_RPAREN]).append(' ');
        }
        if (block == null) {
            result.append(stx.punctuators[PCT_IDX_SEPARATOR]);
        }
        else {
            result.append(block.toString(stx, fmt,
                    (block instanceof BlockStatement) ? indent : ""));
        }
        return result;
    }
}
