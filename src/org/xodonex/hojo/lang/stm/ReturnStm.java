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
import org.xodonex.hojo.lang.type.VoidType;
import org.xodonex.hojo.util.ReturnException;
import org.xodonex.util.StringUtils;

public class ReturnStm extends Statement {

    private static final long serialVersionUID = 1L;

    protected Expression value;

    public ReturnStm(Expression value) {
        this.value = value;
    }

    @Override
    public Statement optimize(int level) {
        if (value != null) {
            value = value.optimize(level);
        }
        return this;
    }

    @Override
    public Statement linkVars(Environment env, short maxLvl) {
        if (value == null) {
            return this;
        }

        Expression value_ = value.linkVars(env, maxLvl);
        if (value_ != value) {
            return new ReturnStm(value_);
        }
        else {
            return this;
        }
    }

    @Override
    public Object run(Environment env) {
        throw new ReturnException(value == null ? null : value.xeq(env));
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        return indent + stx.reserved[RES_RETURN - RES_BASE_ID]
                + ((value == null) ? "" : ' ' + value.toString(stx, fmt, ""))
                + stx.punctuators[PCT_IDX_SEPARATOR];
    }

    @Override
    public boolean isControlTransfer() {
        return true;
    }

    @Override
    public Type checkCode(Type rt) throws UnreachableStatementException {
        Type t = (value == null) ? VoidType.getInstance() : value.getType();
        return (rt == null) ? t : rt.union(t);
    }

}
