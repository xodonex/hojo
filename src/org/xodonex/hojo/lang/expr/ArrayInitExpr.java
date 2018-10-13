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
public class ArrayInitExpr extends ArrayExpr {

    private static final long serialVersionUID = 1L;

    protected Type typ;

    public ArrayInitExpr(Type typ, Expression[] exprs) {
        super(exprs);
        this.typ = typ;
    }

    @Override
    public Object xeq(Environment env) {
        Class elemType = typ.toClass().getComponentType();
        Object result = Array.newInstance(elemType, exprs.length);
        Type caster1 = HojoLib.typeOf(typ.elementClass());
        Type caster2 = HojoLib.typeOf(elemType);
        if (caster2.equals(caster1)) {
            caster2 = null;
        }

        Object tmp;
        for (int i = 0; i < exprs.length; i++) {
            if (exprs[i] != null) {
                tmp = caster1.typeCast(exprs[i].xeq(env));
                if (caster2 != null) {
                    tmp = caster2.typeCast(tmp);
                }
                Array.set(result, i, tmp);
            }
        }
        return result;
    }

    @Override
    public Class getTypeC() {
        return typ.toClass();
    }

    @Override
    protected Type getType0() {
        return typ;
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        StringBuffer result = new StringBuffer(indent);
        Class t = typ.toClass();
        Class tElem = typ.elementClass();

        if (t.getComponentType() == tElem) {
            result.append(stx.reserved[RES_NEW - RES_BASE_ID]).append(' ')
                    .append(ReflectUtils.className2Java(t)).append(' ');
        }
        else {
            result.append(typ.toString(stx));
        }

        result.append(stx.punctuators[PCT_IDX_ARRAYSTART]);
        toString(result, stx, fmt);
        result.append(stx.punctuators[PCT_IDX_ARRAYEND]);
        return result.toString();
    }

}
