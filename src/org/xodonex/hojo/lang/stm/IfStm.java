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
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.StringUtils;

public class IfStm extends ShortIfStm {

    private static final long serialVersionUID = 1L;

    protected Statement alt;

    public IfStm(Expression cond, Statement prim, Statement alt) {
        super(cond, prim);
        this.alt = alt;
    }

    @Override
    public Statement optimize(int level) {
        alt = alt.optimize(level);
        return super.optimize(level);
    }

    @Override
    public Statement linkVars(Environment env, short maxLvl) {
        Statement result = super.linkVars(env, maxLvl);
        Statement alt_ = alt.linkVars(env, maxLvl);

        if (result != this) {
            ((IfStm)result).alt = alt_;
            return result;
        }
        else if (alt_ != alt) {
            IfStm ifstm = (IfStm)clone();
            ifstm.alt = alt_;
            return ifstm;
        }
        else {
            return this;
        }
    }

    @Override
    public Object run(Environment env) throws Throwable {
        if (ConvertUtils.toBool(expr.xeq(env))) {
            return block.run(env);
        }
        else {
            return alt.run(env);
        }
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        StringBuffer sb = toString(stx.reserved[RES_IF - RES_BASE_ID], true,
                stx, fmt, indent);
        sb.append(' ').append(stx.reserved[RES_ELSE - RES_BASE_ID]).append(' ');
        sb.append(alt.toString(stx, fmt, indent));
        return sb.toString();
    }

    @Override
    public Type checkCode(Type rt) throws UnreachableStatementException {
        rt = super.checkCode(rt);
        return alt.checkCode(rt);
    }

}
