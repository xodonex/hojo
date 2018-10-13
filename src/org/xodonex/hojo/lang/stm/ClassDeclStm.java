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

import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.HClass;
import org.xodonex.hojo.lang.Statement;
import org.xodonex.hojo.lang.env.ClassEnv;
import org.xodonex.hojo.lang.env.DummyEnv;
import org.xodonex.hojo.lang.type.HClassType;

public class ClassDeclStm extends ValueDeclStm {

    private static final long serialVersionUID = 1L;

    protected short size;
    protected short level;

    public ClassDeclStm(String name, short modifiers, short baseAddr,
            Statement init, short size, short level) {
        super(name, HClassType.getInstance(), modifiers, baseAddr, init);
        this.level = level;
        this.size = size;
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public Statement linkVars(Environment env, short maxLvl) {
        return super.linkVars(new DummyEnv(env), maxLvl);
    }

    @Override
    public Object run(Environment env) throws Throwable {
        // Create the class, and allocate a new variable containing that value
        Statement init_ = ((Statement)init).linkVars(new DummyEnv(env), level);
        HClass result = new HClass(init_, size);
        env.alloc(baseAddr, (modifiers & MOD_FINAL) != 0, name, typ, result);

        // Update the class environment, if this is a public declaration
        if ((modifiers & MOD_PUBLIC) != 0) {
            ((ClassEnv)env).addMember(name, baseAddr);
        }

        return result;
    }

    @Override
    protected boolean doIndent() {
        return true;
    }

}
