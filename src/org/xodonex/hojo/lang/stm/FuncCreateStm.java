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
import org.xodonex.hojo.lang.Const;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Function;
import org.xodonex.hojo.lang.Statement;
import org.xodonex.hojo.lang.Type;
import org.xodonex.hojo.lang.Variable;
import org.xodonex.hojo.lang.env.ClassEnv;
import org.xodonex.hojo.lang.env.DummyEnv;
import org.xodonex.hojo.lang.func.HojoFunction;
import org.xodonex.hojo.lang.func.SynchronizedHojoFunction;
import org.xodonex.hojo.lang.type.FunctionType;
import org.xodonex.util.ReflectUtils;
import org.xodonex.util.StringUtils;

public class FuncCreateStm extends FuncDeclStm {

    private static final long serialVersionUID = 1L;

    protected String[] argNames;
    protected Class[] argTypes;
    protected Object[] defaults;
    protected String extraName;
    protected short level;
    protected short size;

    public FuncCreateStm(String name, FunctionType funcType, short modifiers,
            short addr, String[] names, Class[] types, Object[] defaults,
            String extraName,
            Type retType, Statement body, short size, short level) {
        super(name, funcType, modifiers, addr, retType, body);
        this.argNames = names;
        this.argTypes = types;
        this.defaults = defaults;
        this.extraName = extraName;
        this.size = size;
        this.level = level;
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public Object run(Environment env) {
        Variable v = env.get(baseAddr);
        if (v == null) {
            // allocate an empty variable
            v = env.alloc(baseAddr, (modifiers & MOD_FINAL) != 0, name,
                    funcType, null);
        }

        // Create the resulting function, and link any external variables
        // (this may include the newly created variable)
        HojoFunction func = (modifiers & MOD_SYNCHRONIZED) != 0
                ? new SynchronizedHojoFunction(argNames, argTypes, defaults,
                        extraName, typ,
                        ((Statement)init).linkVars(new DummyEnv(env), level),
                        size)
                : new HojoFunction(argNames, argTypes, defaults, extraName, typ,
                        ((Statement)init).linkVars(new DummyEnv(env), level),
                        size);

        // Store the value into the newly allocated variable, and return the
        // result
        // HACK: use Variable.MODIFY_FINAL as resolvent to allow final variables
        // to be updated at this particular place
        v.set(Variable.MODIFY_FINAL, func);

        // Update the class environment, if this is a public declaration
        if ((modifiers & MOD_PUBLIC) != 0) {
            ((ClassEnv)env).addMember(name, baseAddr);
        }

        return func;
    }

    @Override
    protected void argTypes2String(StringBuffer result, HojoSyntax stx,
            StringUtils.Format fmt) {
        for (int i = 0; i < argNames.length; i++) {
            result.append(ReflectUtils.className2Java(argTypes[i]));
            result.append(' ').append(argNames[i]);
            if (defaults[i] != Function.NO_ARG) {
                result.append(' ').append(stx.operators[OP_IDX_ASSIGN])
                        .append(' ');
                result.append(Const.toString(defaults[i], stx, fmt));
            }
            if (i < argNames.length - 1) {
                result.append(stx.punctuators[PCT_IDX_DELIMITER]);
            }
        }
        if (extraName != null) {
            if (argNames.length > 0) {
                result.append(stx.punctuators[PCT_IDX_DELIMITER]).append(' ');
            }
            result.append('*').append(' ').append(extraName);
        }
    }

    @Override
    protected boolean doIndent() {
        return true;
    }
}
