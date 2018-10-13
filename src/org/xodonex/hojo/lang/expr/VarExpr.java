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
import org.xodonex.hojo.lang.AbstractLValue;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.Type;
import org.xodonex.hojo.lang.Variable;
import org.xodonex.util.StringUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public class VarExpr extends AbstractLValue {

    private static final long serialVersionUID = 1L;

    private short level; // function nesting level
    private short depth; // scope depth (0 == local)
    private short addr; // local addresss within the environment of residence
    private short modifiers;
    private Type typ; // declared type

    public VarExpr(short[] address, Type typ) {
        this(address[0], address[1], address[2], address[3], typ);
    }

    public VarExpr(short locAddr, short depth, short level, short modifiers,
            Type typ) {
        this.addr = locAddr;
        this.depth = depth;
        this.level = level;
        this.modifiers = modifiers;
        this.typ = typ;
    }

    protected Variable retreive(Environment env) {
        Environment e = env;
        for (int i = depth; i > 0; i--) {
            e = e.getParent();
        }
        return e.get(addr);
    }

    @Override
    public Expression linkVars(Environment env, short maxLvl) {
        if (level <= maxLvl) {
            return retreive(env);
        }
        else {
            return this;
        }
    }

    @Override
    public Object xeq(Environment env) {
        return retreive(env).getValue();
    }

    @Override
    protected Type getType0() {
        return typ;
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        StringBuffer result = new StringBuffer(indent);

        if (depth == 0) {
            result.append(stx.reserved[RES_THIS - RES_BASE_ID]);
        }
        else {
            for (int i = 1; i <= depth; i++) {
                result.append(stx.reserved[RES_SUPER - RES_BASE_ID]);
                if (i < depth) {
                    result.append(stx.operators[OP_IDX_DOT]);
                }
            }
        }

        result.append(stx.operators[OP_IDX_DOT]);
        result.append(addr);
        return result.toString();
    }

    @Override
    public Object resolve(Environment env) {
        return retreive(env);
    }

    @Override
    public Object get(Object resolvent) {
        return ((Variable)resolvent).getValue();
    }

    @Override
    public Object set(Object resolvent, Object value) {
        return ((Variable)resolvent).set(null, value);
    }

    public short getModifiers() {
        return modifiers;
    }

}
