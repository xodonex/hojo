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
import org.xodonex.hojo.lang.CompilerEnvironment;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.Type;
import org.xodonex.util.StringUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public class ScopeRef extends Expression {

    private static final long serialVersionUID = 1L;

    protected short length;

    public ScopeRef(short length) {
        this.length = length;
    }

    @Override
    protected Type getType0() {
        return HojoLib.VOID_TYPE;
    }

    @Override
    public Object xeq(Environment env) {
        throw new HojoException();
    }

    public short getLength() {
        return length;
    }

    public void addLink() {
        length++;
    }

    public VarExpr link(CompilerEnvironment env, String name) {
        CompilerEnvironment base = env.getParent(length);
        if (base == null) {
            return null;
        }

        short locAddr = base.getAddress(name);
        if (locAddr < 0) {
            return null;
        }

        return new VarExpr(locAddr, length, base.getLevel(),
                base.getModifiers(name),
                base.getType(name));
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        if (length == 0) {
            return indent + stx.reserved[RES_THIS - RES_BASE_ID];
        }

        StringBuffer result = new StringBuffer(indent);
        for (int i = length; i >= 1; i--) {
            result.append(stx.reserved[RES_SUPER - RES_BASE_ID]);
            if (i > 1) {
                result.append(stx.operators[OP_IDX_DOT]);
            }
        }
        return result.toString();
    }
}
