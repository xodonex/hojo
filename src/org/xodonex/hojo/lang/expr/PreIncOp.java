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
import org.xodonex.hojo.lang.LValue;
import org.xodonex.hojo.lang.Type;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.StringUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public class PreIncOp extends UnaryExpr {

    private static final long serialVersionUID = 1L;

    public PreIncOp(Expression arg) {
        super(arg);
    }

    @Override
    public Object xeq(Environment env) throws HojoException {
        LValue lv = (LValue)arg;
        Object res = lv.resolve(env);
        Number result = ConvertUtils.toNumber(lv.get(res), true);
        return lv.set(res, HojoLib.incDec(result, true));
    }

    @Override
    public Class getTypeC() {
        return arg.getTypeC();
    }

    @Override
    public boolean isJavaStatement() {
        return true;
    }

    @Override
    protected Type getType0() {
        return arg.getType();
    }

    @Override
    protected String toString(HojoSyntax stx, StringUtils.Format fmt) {
        return stx.operators[OP_IDX_INC];
    }

}
