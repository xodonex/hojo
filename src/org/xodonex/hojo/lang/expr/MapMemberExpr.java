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

import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.HojoSyntax;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.LValue;
import org.xodonex.hojo.lang.Type;
import org.xodonex.util.StringUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public class MapMemberExpr extends UnaryExpr implements LValue {

    private static final long serialVersionUID = 1L;

    protected String name;

    public MapMemberExpr(Expression map, String name) {
        super(map);
        this.name = name;
    }

    @Override
    public boolean isConst() {
        return false;
    }

    @Override
    public Object xeq(Environment env) {
        return ((Map)arg.xeq(env)).get(name);
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        return arg.toString(stx, fmt, indent) + stx.operators[OP_IDX_DOT]
                + name;
    }

    @Override
    protected Type getType0() {
        return HojoLib.OBJ_TYPE;
    }

    @Override
    protected String toString(HojoSyntax stx, StringUtils.Format fmt) {
        return null; // not used;
    }

    @Override
    public Object resolve(Environment env) {
        return arg.xeq(env);
    }

    @Override
    public Object get(Object resolvent) {
        return ((Map)resolvent).get(name);
    }

    @Override
    public Object set(Object resolvent, Object value) {
        ((Map)resolvent).put(name, value);
        return value;
    }
}
