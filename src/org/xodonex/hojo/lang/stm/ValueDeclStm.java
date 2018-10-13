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
package org.xodonex.hojo.lang.stm;

import org.xodonex.hojo.HojoSyntax;
import org.xodonex.hojo.lang.Code;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.Statement;
import org.xodonex.hojo.lang.Type;
import org.xodonex.util.StringUtils;

public abstract class ValueDeclStm extends Statement {

    private static final long serialVersionUID = 1L;

    protected String name;
    protected Type typ;
    protected short baseAddr;
    protected Code init;
    protected short modifiers;

    public ValueDeclStm(String name, Type typ, short modifiers, short baseAddr,
            Code init) {
        this.name = name;
        this.typ = typ;
        this.modifiers = modifiers;
        this.baseAddr = baseAddr;
        this.init = init;
    }

    @Override
    public Statement optimize(int level) {
        if (init != null) {
            if (init instanceof Expression) {
                init = ((Expression)init).optimize(level);
            }
            else {
                init = ((Statement)init).optimize(level);
            }
        }
        return this;
    }

    @Override
    public Statement linkVars(Environment env, short maxLvl) {
        if (init == null) {
            return this;
        }

        Code init_;
        if (init instanceof Expression) {
            init_ = ((Expression)init).linkVars(env, maxLvl);
        }
        else {
            init_ = ((Statement)init).linkVars(env, maxLvl);
        }

        if (init_ != init) {
            ValueDeclStm result = (ValueDeclStm)clone();
            result.init = init_;
            return result;
        }
        return this;
    }

    // whether toString() should indent the init code
    protected boolean doIndent() {
        return false;
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        StringBuffer result = new StringBuffer(indent);
        addModifiers(result, stx);
        if (typ == null) {
            result.append(stx.reserved[RES_VAR - RES_BASE_ID]);
        }
        else {
            result.append(typ.toString(stx));
        }
        result.append(' ').append(name).append(' ');
        result.append(stx.operators[OP_IDX_ASSIGN]).append(' ');

        if (init != null) {
            if (doIndent()) {
                result.append(stx.punctuators[PCT_IDX_BLOCKSTART]).append('\n');
                result.append(
                        init.toString(stx, fmt, indent + fmt.getIndent()));
                result.append('\n').append(stx.punctuators[PCT_IDX_BLOCKEND]);
            }
            else {
                result.append(init.toString(stx, fmt, ""));
            }
        }
        return result.append(stx.punctuators[PCT_IDX_SEPARATOR]).toString();
    }

    protected void addModifiers(StringBuffer result, HojoSyntax stx) {
        if ((modifiers & MOD_PUBLIC) != 0) {
            result.append(stx.reserved[RES_PUBLIC - RES_BASE_ID]).append(' ');
        }
        if ((modifiers & MOD_FINAL) != 0) {
            result.append(stx.reserved[RES_FINAL - RES_BASE_ID]).append(' ');
        }
    }

}
