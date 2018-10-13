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

import java.util.Map;

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
public class MapInitExpr extends ArrayExpr {

    private static final long serialVersionUID = 1L;

    protected Type typ;
    protected Expression cre8;
    protected Expression[] keys;

    public MapInitExpr(Type typ, Expression cre8, Expression[] keys,
            Expression[] exprs) {
        super(exprs);
        this.keys = keys;
        this.cre8 = cre8;
        this.typ = typ;
    }

    @Override
    public Object xeq(Environment env) {
        try {
            Class elemType = typ.elementClass();
            Map result = (cre8 == null)
                    ? (Map)typ.instanceClass().newInstance()
                    : (Map)cre8.xeq(env);
            Type caster = HojoLib.typeOf(elemType);

            for (int i = 0; i < exprs.length; i++) {
                result.put(keys[i].xeq(env),
                        caster.typeCast(exprs[i].xeq(env)));
            }
            return result;
        }
        catch (Exception e) {
            throw HojoException.wrap(e);
        }
    }

    @Override
    public Expression linkVars(Environment env, short maxLvl) {
        MapInitExpr result = (MapInitExpr)super.linkVars(env, maxLvl);
        Expression cre8_ = cre8 == null ? null : cre8.linkVars(env, maxLvl);

        boolean changed = false;
        Expression[] keys_ = new Expression[keys.length];
        for (int i = 0; i < keys.length; i++) {
            if ((keys_[i] = keys[i].linkVars(env, maxLvl)) != keys[i]) {
                changed = true;
            }
        }

        if (result != this) {
            result.cre8 = cre8_;
            if (changed) {
                result.keys = keys_;
            }
            return result;
        }
        else if (cre8_ != cre8 || changed) {
            result = (MapInitExpr)clone();
            result.cre8 = cre8_;
            result.keys = changed ? keys_ : keys;
            return result;
        }
        else {
            return this;
        }
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
        Class tElem = typ.elementClass();

        if (cre8 != null) {
            result.append(cre8.toString(stx, fmt, "")).append(' ');
        }
        else if (tElem == Object.class) {
            result.append(stx.reserved[RES_NEW - RES_BASE_ID]).append(' ')
                    .append(ReflectUtils.className2Java(typ.instanceClass()))
                    .append(stx.punctuators[PCT_IDX_LPAREN])
                    .append(stx.punctuators[PCT_IDX_RPAREN]).append(' ');
        }
        else {
            result.append(typ.toString(stx));
        }

        result.append(stx.punctuators[PCT_IDX_MAPSTART]).append(' ');
        for (int i = 0; i < keys.length; i++) {
            result.append(stx.operators[PCT_IDX_LPAREN])
                    .append(keys[i].toString(stx, fmt, indent))
                    .append(stx.operators[PCT_IDX_RPAREN]).append(' ')
                    .append(stx.operators[OP_IDX_ASSIGN]).append(' ')
                    .append(exprs[i].toString(stx, fmt, ""));
            if (i < keys.length - 1) {
                result.append(stx.punctuators[PCT_IDX_DELIMITER]).append(' ');
            }
        }
        result.append(' ').append(stx.punctuators[PCT_IDX_MAPEND]);
        return result.toString();
    }

}
