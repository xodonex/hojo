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

import java.lang.reflect.Field;

import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.HojoSyntax;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.Type;
import org.xodonex.util.StringUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public class FinalFieldExpr extends Expression {

    private static final long serialVersionUID = 1L;

    protected Expression base;
    protected Field f;

    public FinalFieldExpr(Expression base, Field f) {
        this.base = base;
        this.f = f;
    }

    @Override
    public Object xeq(Environment env) {
        Object b = base.xeq(env);
        try {
            return f.get(b);
        }
        catch (Exception e) {
            throw new HojoException(e);
        }
    }

    @Override
    public Expression linkVars(Environment env, short maxLvl) {
        Expression base_ = base.linkVars(env, maxLvl);
        if (base_ != base) {
            FinalFieldExpr result = (FinalFieldExpr)clone();
            result.base = base_;
            return result;
        }
        else {
            return this;
        }
    }

    @Override
    public Class getTypeC() {
        return f.getType();
    }

    @Override
    protected Type getType0() {
        return HojoLib.typeOf(f.getType());
    }

    public Field getField() {
        return f;
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        return base.toString(stx, fmt, indent) + stx.operators[OP_IDX_DOT]
                + f.getName();
    }

}
