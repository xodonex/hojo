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
import org.xodonex.hojo.lang.Type;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.ReflectUtils;
import org.xodonex.util.StringUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public class ArrayCreateExpr extends ArrayExpr {

    private static final long serialVersionUID = 1L;

    protected Class base;

    public ArrayCreateExpr(Class base, Expression[] dims) {
        super(dims);
        this.base = base;
    }

    @Override
    public Object xeq(Environment env) {
        int[] adims = new int[exprs.length];
        for (int i = 0; i < exprs.length; i++) {
            adims[i] = ConvertUtils.toInt(exprs[i].xeq(env));
        }
        try {
            return java.lang.reflect.Array.newInstance(base, adims);
        }
        catch (Exception e) {
            throw new HojoException(e);
        }
    }

    @Override
    protected Type getType0() {
        return HojoLib.typeOf(ReflectUtils.getArrayClass(base, 1));
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        StringBuffer result = new StringBuffer(indent);
        result.append(stx.punctuators[PCT_IDX_LPAREN]);
        result.append(stx.reserved[RES_NEW - RES_BASE_ID]);
        result.append(' ').append(ReflectUtils.className2Java(base));
        for (int i = 0; i < exprs.length; i++) {
            result.append(stx.punctuators[PCT_IDX_IDXSTART]);
            result.append(exprs[i].toString(stx, fmt, ""));
            result.append(stx.punctuators[PCT_IDX_IDXEND]);
        }
        result.append(stx.punctuators[PCT_IDX_RPAREN]);
        return result.toString();
    }

}
