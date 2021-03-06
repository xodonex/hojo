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
import org.xodonex.hojo.lang.AbstractLValue;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.HObject;
import org.xodonex.hojo.lang.Type;
import org.xodonex.hojo.lang.Variable;
import org.xodonex.util.StringUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public class HClassMemberExpr extends AbstractLValue {

    private static final long serialVersionUID = 1L;

    protected String name;
    protected Expression arg;

    public HClassMemberExpr(Expression dobj, String name) {
        super();
        this.name = name;
        this.arg = dobj;
    }

    protected Variable getVar(Environment env) {
        HObject base = (HObject)arg.xeq(env);
        try {
            return base.get(name);
        }
        catch (NoSuchFieldException e) {
            throw HojoException.wrap(e);
        }
    }

    @Override
    public Object xeq(Environment env) {
        return getVar(env).getValue();
    }

    @Override
    public Expression optimize(int level) {
        arg = arg.optimize(level);
        return this;
    }

    @Override
    public Expression linkVars(Environment env, short maxLvl) {
        Expression arg_ = arg.linkVars(env, maxLvl);
        return (arg_ != arg) ? new HClassMemberExpr(arg_, name) : this;
    }

    @Override
    public Object resolve(Environment env) throws HojoException {
        return getVar(env);
    }

    @Override
    public Object get(Object resolvent) throws HojoException {
        return ((Variable)resolvent).getValue();
    }

    @Override
    public Object set(Object resolvent, Object value) throws HojoException {
        return ((Variable)resolvent).set(null, value);
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

    protected String toString(HojoSyntax stx) {
        return null; // not used;
    }
}
