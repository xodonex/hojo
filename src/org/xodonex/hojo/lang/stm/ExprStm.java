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
import org.xodonex.util.StringUtils;

public class ExprStm extends Statement {

    private static final long serialVersionUID = 1L;

    protected Expression expr;

    public ExprStm(Expression expr) {
        this.expr = expr;
    }

    public Expression getExpression() {
        return expr;
    }

    @Override
    public Statement optimize(int level) {
        expr = expr.optimize(level);
        return this;
    }

    @Override
    public Statement linkVars(Environment env, short maxLvl) {
        Expression expr_ = expr.linkVars(env, maxLvl);
        if (expr_ != expr) {
            return new ExprStm(expr_);
        }
        else {
            return this;
        }
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public Object run(Environment env) {
        return expr.xeq(env);
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        return expr.toString(stx, fmt, indent)
                + stx.punctuators[PCT_IDX_SEPARATOR];
    }

}
