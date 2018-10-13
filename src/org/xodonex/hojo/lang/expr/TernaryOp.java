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
import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.HojoSyntax;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.Function;
import org.xodonex.hojo.lang.Operator;
import org.xodonex.hojo.lang.Type;
import org.xodonex.util.StringUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public class TernaryOp extends TernaryExpr {

    private static final long serialVersionUID = 1L;

    protected Function op;

    public TernaryOp(Function op, Expression e1, Expression e2, Expression e3) {
        super(e1, e2, e3);
        this.op = op;
    }

    @Override
    public Object xeq(Environment env) throws HojoException {
        return getType().typeCast(op.invoke(new Object[] {
                e1.xeq(env), e2.xeq(env), e3 == null ? null : e3.xeq(env)
        }));
    }

    @Override
    protected Type getType0() {
        if (op instanceof Operator) {
            return ((Operator)op).inferType(new Type[] { e1.getType(),
                    e2.getType(),
                    e3 == null ? HojoLib.OBJ_TYPE : e3.getType() }, null);
        }
        else {
            return HojoLib.typeOf(op.getReturnType());
        }
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        String opString = toString(op, stx, fmt);
        return indent + stx.punctuators[PCT_IDX_LPAREN]
                + e1.toString(stx, fmt, "") +
                ' ' + opString + e2.toString(stx, fmt, " ") +
                ' ' + opString + ' ' +
                (e3 == null ? null : e3.toString(stx, fmt, "")) +
                stx.punctuators[PCT_IDX_RPAREN];
    }
}
