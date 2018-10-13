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
public abstract class ArrayExpr extends Expression {

    private static final long serialVersionUID = 1L;

    protected Expression[] exprs;

    public ArrayExpr(Expression[] exprs) {
        this.exprs = exprs;
    }

    @Override
    public Expression optimize(int level) {
        for (int i = 0; i < exprs.length; i++) {
            if (exprs[i] != null) {
                exprs[i] = exprs[i].optimize(level);
            }
        }
        return this;
    }

    @Override
    public Expression linkVars(Environment env, short maxLvl) {
        Expression[] x = new Expression[exprs.length];
        boolean modified = false;
        for (int i = 0; i < exprs.length; i++) {
            if (exprs[i] != null) {
                x[i] = exprs[i].linkVars(env, maxLvl);
            }
            if (x[i] != exprs[i]) {
                modified = true;
            }
        }
        if (modified) {
            ArrayExpr result = (ArrayExpr)clone();
            result.exprs = x;
            return result;
        }
        else {
            return this;
        }
    }

    protected void toString(StringBuffer result, HojoSyntax stx,
            StringUtils.Format fmt) {
        for (int i = 0; i < exprs.length; i++) {
            if (exprs[i] != null) {
                result.append(exprs[i].toString(stx, fmt, ""));
            }
            if (i < exprs.length - 1) {
                result.append(stx.punctuators[PCT_IDX_DELIMITER]).append(' ');
            }
        }
    }

}
