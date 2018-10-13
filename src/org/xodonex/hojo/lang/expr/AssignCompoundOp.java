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
import org.xodonex.hojo.HojoSyntax;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.Function;
import org.xodonex.hojo.lang.LValue;
import org.xodonex.hojo.lang.Type;
import org.xodonex.util.StringUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public class AssignCompoundOp extends BinaryExpr {

    private static final long serialVersionUID = 1L;

    protected Function op;
    protected Type castType;

    public AssignCompoundOp(Function op, Expression lhs, Expression rhs,
            Type castType) {
        super(lhs, rhs);
        this.op = op;
        this.castType = castType;
    }

    @Override
    public Object xeq(Environment env) throws HojoException {
        LValue lv = (LValue)lhs;

        // evaluate any contained subexpressions in the lvalue
        Object res = lv.resolve(env);

        // retrieve the value of the lvalue, evaluate the rhs and perform the
        // operation.
        Object v = op.invoke(new Object[] { lv.get(res), rhs.xeq(env) });

        // cast the value, if necessary
        if (castType != null) {
            v = castType.typeCast(v);
        }

        // write the result back
        return lv.set(res, v);
    }

    @Override
    public Class getTypeC() {
        return lhs.getTypeC();
    }

    @Override
    public boolean isJavaStatement() {
        return true;
    }

    @Override
    protected Type getType0() {
        return lhs.getType();
    }

    @Override
    protected String toString(HojoSyntax stx, StringUtils.Format fmt) {
        return toString(op, stx, fmt) + stx.operators[OP_IDX_ASSIGN];
    }

}
