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

import org.xodonex.hojo.HojoSyntax;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.util.StringUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public abstract class BinaryExpr extends Expression {

    private static final long serialVersionUID = 1L;

    protected Expression lhs;
    protected Expression rhs;

    public BinaryExpr(Expression lhs, Expression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public boolean isConst() {
        return lhs.isConst() && rhs.isConst();
    }

    @Override
    public Expression optimize(int level) {
        lhs = lhs.optimize(level);
        rhs = rhs.optimize(level);
        return this;
    }

    @Override
    public Expression linkVars(Environment env, short maxLvl) {
        Expression lhs_ = lhs.linkVars(env, maxLvl);
        Expression rhs_ = rhs.linkVars(env, maxLvl);
        if (lhs_ != lhs || rhs_ != rhs) {
            BinaryExpr result = (BinaryExpr)clone();
            result.lhs = lhs_;
            result.rhs = rhs_;
            return result;
        }
        else {
            return this;
        }
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        return indent + stx.punctuators[PCT_IDX_LPAREN]
                + lhs.toString(stx, fmt, "") +
                " " + toString(stx, fmt) + " " + rhs.toString(stx, fmt, "") +
                stx.punctuators[PCT_IDX_RPAREN];
    }

    protected abstract String toString(HojoSyntax stx, StringUtils.Format fmt);

}
