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

import java.lang.reflect.Array;
import java.util.List;

import org.xodonex.hojo.HojoLib;
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
public class GenericIndexExpr extends BinaryExpr {

    private static final long serialVersionUID = 1L;

    public GenericIndexExpr(Expression lhs, Expression rhs) {
        super(lhs, rhs);
    }

    @Override
    public Object xeq(Environment env) {
        Object base = lhs.xeq(env);
        int index = ConvertUtils.toInt(rhs.xeq(env));
        if (base instanceof String) {
            return new Character(((String)base).charAt(index));
        }
        else if (base instanceof StringBuffer) {
            return new Character(((StringBuffer)base).charAt(index));
        }
        else if (base instanceof List) {
            return ((List)base).get(index);
        }
        else {
            return Array.get(base, index);
        }
    }

    @Override
    public Class getTypeC() {
        return Object.class;
    }

    @Override
    protected Type getType0() {
        return HojoLib.OBJ_TYPE;
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        return lhs.toString(stx, fmt, indent)
                + stx.punctuators[PCT_IDX_IDXSTART] +
                rhs.toString(stx, fmt, "") + stx.punctuators[PCT_IDX_IDXEND];
    }

    // not used
    @Override
    protected String toString(HojoSyntax stx, StringUtils.Format fmt) {
        return null;
    }
}
