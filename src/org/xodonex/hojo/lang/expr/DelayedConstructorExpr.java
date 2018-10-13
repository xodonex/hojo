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

import java.lang.reflect.Constructor;

import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.HojoSyntax;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.Type;
import org.xodonex.util.ReflectUtils;
import org.xodonex.util.StringUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public class DelayedConstructorExpr extends ArrayExpr {

    private static final long serialVersionUID = 1L;

    protected Class base;

    public DelayedConstructorExpr(Class base, Expression[] args) {
        super(args);
        this.base = base;
    }

    @Override
    public Object xeq(Environment env) {
        Object[] args = new Object[exprs.length];
        Class[] types = new Class[exprs.length];
        for (int i = 0; i < exprs.length; i++) {
            if (exprs[i] != null) {
                types[i] = (((args[i] = exprs[i].xeq(env)) == null) ? null
                        : args[i].getClass());
            }
        }
        try {
            Constructor constr = ReflectUtils.getMatchingConstructor(base,
                    types);
            return constr.newInstance(args);
        }
        catch (Exception e) {
            throw HojoException.wrap(e);
        }
    }

    @Override
    public Class getTypeC() {
        return base;
    }

    @Override
    protected Type getType0() {
        return HojoLib.typeOf(base);
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        StringBuffer result = new StringBuffer(indent);
        result.append(stx.reserved[RES_NEW - RES_BASE_ID]);
        result.append(' ').append(ReflectUtils.className2Java(base));
        result.append(stx.punctuators[PCT_IDX_LPAREN]);
        toString(result, stx, fmt);
        result.append(stx.punctuators[PCT_IDX_RPAREN]);
        return result.toString();
    }

}
