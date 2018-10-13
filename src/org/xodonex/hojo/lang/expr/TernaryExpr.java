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

import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;

/**
 *
 * @author Henrik Lauritzen
 */
public abstract class TernaryExpr extends Expression {

    private static final long serialVersionUID = 1L;

    protected Expression e1, e2, e3;

    public TernaryExpr(Expression e1, Expression e2, Expression e3) {
        this.e1 = e1;
        this.e2 = e2;
        this.e3 = e3;
    }

    @Override
    public boolean isConst() {
        return e1.isConst() && e2.isConst() && (e3 == null || e3.isConst());
    }

    @Override
    public Expression optimize(int level) {
        e1 = e1.optimize(level);
        e2 = e2.optimize(level);
        e3 = e3 == null ? null : e3.optimize(level);
        return this;
    }

    @Override
    public Expression linkVars(Environment env, short maxLvl) {
        Expression e1_ = e1.linkVars(env, maxLvl);
        Expression e2_ = e2.linkVars(env, maxLvl);
        Expression e3_ = e3 == null ? null : e3.linkVars(env, maxLvl);
        if (e1_ != e1 || e2_ != e2 || e3_ != e3) {
            TernaryExpr result = (TernaryExpr)clone();
            result.e1 = e1_;
            result.e2 = e2_;
            result.e3 = e3_;
            return result;
        }
        else {
            return this;
        }
    }

}
