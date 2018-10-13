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
import org.xodonex.hojo.lang.Const;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.Function;
import org.xodonex.hojo.lang.Type;
import org.xodonex.hojo.lang.env.DummyEnv;
import org.xodonex.hojo.lang.func.HojoFunction;
import org.xodonex.hojo.lang.type.GenericFunctionType;
import org.xodonex.util.ReflectUtils;
import org.xodonex.util.StringUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public class LambdaExpr extends Expression {

    private static final long serialVersionUID = 1L;

    protected String[] names;
    protected Class[] types;
    protected Object[] defaults;
    protected String extraName;
    protected Expression body;
    protected short size;
    protected short level;

    public LambdaExpr(String[] names, Class[] types, Object[] defaults,
            String extraName, Expression body, short size, short level) {
        this.names = names;
        this.types = types;
        this.defaults = defaults;
        this.extraName = extraName;
        this.body = body;
        this.size = size;
        this.level = level;
    }

    @Override
    public Object xeq(Environment env) {
        return new HojoFunction(names, types, defaults, extraName, null,
                body.linkVars(new DummyEnv(env), level), size);
    }

    @Override
    public Expression linkVars(Environment env, short maxLvl) {
        Expression body_ = body.linkVars(new DummyEnv(env), maxLvl);
        if (body_ != body) {
            return new LambdaExpr(names, types, defaults, extraName, body_,
                    size, level);
        }
        else {
            return this;
        }
    }

    @Override
    protected Type getType0() {
        return new GenericFunctionType(Function.class, types, body.getTypeC());
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        StringBuffer result = new StringBuffer(indent);
        result.append(stx.reserved[RES_LAMBDA - RES_BASE_ID]);
        result.append(stx.punctuators[PCT_IDX_LPAREN]);

        for (int i = 0; i < names.length; i++) {
            result.append(ReflectUtils.className2Java(types[i])).append(' ');
            result.append(names[i]);
            if (defaults[i] != Function.NO_ARG) {
                result.append(' ').append(stx.operators[OP_IDX_ASSIGN])
                        .append(' ');
                result.append(Const.toString(defaults[i], stx, fmt));
            }
            if (i < names.length - 1) {
                result.append(stx.punctuators[PCT_IDX_DELIMITER]);
            }
        }
        if (extraName != null) {
            result.append(stx.punctuators[PCT_IDX_DELIMITER]).append(" * ");
            result.append(extraName);
        }

        result.append(stx.punctuators[PCT_IDX_RPAREN]).append(' ');
        result.append(stx.operators[OP_IDX_FUNC]).append(' ');
        result.append(body.toString(stx, fmt, ""));
        return result.toString();
    }

}
