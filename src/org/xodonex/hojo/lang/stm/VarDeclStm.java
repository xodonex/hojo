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
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.Statement;
import org.xodonex.hojo.lang.Type;
import org.xodonex.hojo.lang.Variable;
import org.xodonex.hojo.lang.env.ClassEnv;
import org.xodonex.util.StringUtils;

public class VarDeclStm extends Statement {

    private static final long serialVersionUID = 1L;

    protected String[] names;
    protected Expression[] init;
    protected Type typ;
    protected short baseAddr;
    protected short modifiers;

    public VarDeclStm(String[] names, Expression[] init,
            Type typ, short modifiers, short baseAddr) {
        this.names = names;
        this.init = init;
        this.typ = typ;
        this.modifiers = modifiers;
        this.baseAddr = baseAddr;
    }

    @Override
    public Statement optimize(int level) {
        for (int i = init.length - 1; i >= 0; i--) {
            init[i] = init[i].optimize(level);
        }
        return this;
    }

    @Override
    public Statement linkVars(Environment env, short maxLvl) {
        Expression[] init_ = new Expression[init.length];
        boolean modified = false;

        for (int i = init.length - 1; i >= 0; i--) {
            if ((init_[i] = init[i].linkVars(env, maxLvl)) != init[i]) {
                modified = true;
            }
        }

        if (modified) {
            VarDeclStm result = (VarDeclStm)clone();
            result.init = init_;
            return result;
        }
        else {
            return this;
        }
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public Object run(Environment env) {
        return run(env, false);
    }

    protected Object run(Environment env, boolean isPublic) {
        short nextAddr = baseAddr;
        Object initVal = null;
        Variable result = null;
        Type t = typ;
        ClassEnv cenv = ((modifiers & MOD_PUBLIC) != 0) ? (ClassEnv)env : null;

        for (int i = 0; i < names.length; i++) {
            // evaluate the initial value
            initVal = init[i].xeq(env);

            // determine the type of the new variable
            if (typ == null) {
                // var declaration - use the type of the initializer.
                t = init[i].getType();
            }

            // allocate and initialize the variable
            result = env.alloc(nextAddr, (modifiers & MOD_FINAL) != 0, names[i],
                    t, initVal);

            // update the class environment, if this is a public member
            if (((modifiers & MOD_PUBLIC) != 0)) {
                cenv.addMember(names[i], nextAddr);
            }
            nextAddr++;
        }

        // result in the last initial value
        return result.getValue();
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        StringBuffer result = new StringBuffer(indent);
        if ((modifiers & MOD_PUBLIC) != 0) {
            result.append(stx.reserved[RES_PUBLIC - RES_BASE_ID]).append(' ');
        }
        if ((modifiers & MOD_FINAL) != 0) {
            result.append(stx.reserved[RES_FINAL - RES_BASE_ID]).append(' ');
        }
        if (typ == null) {
            result.append(stx.reserved[RES_VAR - RES_BASE_ID]);
        }
        else {
            result.append(typ.toString(stx));
        }
        result.append(' ');

        for (int i = 0; i < names.length; i++) {
            result.append(names[i]).append(' ')
                    .append(stx.operators[OP_IDX_ASSIGN]).append(' ');
            result.append(init[i].toString(stx, fmt, ""));
            if (i < names.length - 1) {
                result.append(stx.punctuators[PCT_IDX_DELIMITER]).append(' ');
            }
        }
        return result.append(stx.punctuators[PCT_IDX_SEPARATOR]).toString();
    }

}
