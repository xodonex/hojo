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
import org.xodonex.hojo.lang.Type;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.StringUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public class IfThenElseExpr extends TernaryExpr {

    private static final long serialVersionUID = 1L;

    public IfThenElseExpr(Expression e1, Expression e2, Expression e3) {
        super(e1, e2, e3);

        // enforce a type check
        getType();
    }

    @Override
    public Object xeq(Environment env) {
        return getType().typeCast(
                ConvertUtils.toBool(e1.xeq(env)) ? e2.xeq(env) : e3.xeq(env));
    }

    @Override
    public boolean isJavaStatement() {
        return e2.isJavaStatement() && e3.isJavaStatement();
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        return indent + stx.punctuators[PCT_IDX_LPAREN]
                + e1.toString(stx, fmt, " ") +
                ' ' + stx.operators[OP_IDX_IFTHEN] + e2.toString(stx, fmt, " ")
                + ' ' +
                stx.operators[OP_IDX_ELSE] + e3.toString(stx, fmt, " ") +
                stx.punctuators[PCT_IDX_RPAREN];
    }

    @Override
    protected Type getType0() {
        return e2.getType().union(e3.getType());
    }

}
