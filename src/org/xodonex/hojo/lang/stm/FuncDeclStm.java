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
import org.xodonex.hojo.lang.Statement;
import org.xodonex.hojo.lang.Type;
import org.xodonex.hojo.lang.env.ClassEnv;
import org.xodonex.hojo.lang.env.DummyEnv;
import org.xodonex.hojo.lang.type.FunctionType;
import org.xodonex.util.ReflectUtils;
import org.xodonex.util.StringUtils;

public class FuncDeclStm extends ValueDeclStm {

    private static final long serialVersionUID = 1L;

    protected FunctionType funcType;

    public FuncDeclStm(String name, FunctionType funcType, short modifiers,
            short addr, Type retType, Statement body) {
        super(name, retType, modifiers, addr, body);
        this.funcType = funcType;
    }

    @Override
    public Object run(Environment env) {
        if (env.get(baseAddr) == null) {
            // allocate an empty variable of the function type
            env.alloc(baseAddr, (modifiers & MOD_FINAL) != 0, name, funcType,
                    null);

            // Update the class environment, if this is a public declaration
            if ((modifiers & MOD_PUBLIC) != 0) {
                ((ClassEnv)env).addMember(name, baseAddr);
            }
        }
        return null;
    }

    @Override
    public Statement linkVars(Environment env, short maxLvl) {
        if (init == null) {
            return this;
        }

        Statement body_ = ((Statement)init).linkVars(new DummyEnv(env),
                maxLvl);
        if (body_ != init) {
            FuncDeclStm result = (FuncDeclStm)clone();
            result.init = body_;
            return result;
        }
        else {
            return this;
        }
    }

    protected void argTypes2String(StringBuffer result, HojoSyntax stx,
            StringUtils.Format fmt) {
        Class[] argTypes = funcType.getParameterTypes();
        for (int i = 0; i < argTypes.length; i++) {
            result.append(ReflectUtils.className2Java(argTypes[i]));
            if (i < argTypes.length - 1) {
                result.append(stx.punctuators[PCT_IDX_DELIMITER]);
            }
        }
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        StringBuffer result = new StringBuffer(indent);
        addModifiers(result, stx);
        result.append(typ.toString(stx)).append(' ').append(name);
        result.append(stx.punctuators[PCT_IDX_LPAREN]);
        argTypes2String(result, stx, fmt);
        result.append(stx.punctuators[PCT_IDX_RPAREN]);
        if (init != null) {
            result.append(stx.punctuators[PCT_IDX_BLOCKSTART]).append('\n');
            result.append(init.toString(stx, fmt, indent + fmt.getIndent()));
            result.append('\n').append(indent)
                    .append(stx.punctuators[PCT_IDX_BLOCKEND]);
        }
        else {
            result.append(stx.punctuators[PCT_IDX_SEPARATOR]);
        }
        return result.toString();
    }

}
