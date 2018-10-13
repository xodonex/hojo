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
package org.xodonex.hojo.lang.env;

import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.lang.CompilerEnvironment;
import org.xodonex.hojo.lang.Type;

public class NoEnv implements CompilerEnvironment {

    private final static NoEnv instance = new NoEnv();

    public static NoEnv getInstance() {
        return instance;
    }

    private NoEnv() {
    }

    @Override
    public CompilerEnvironment getParent(short chainLength) {
        return null;
    }

    @Override
    public short[] findVar(String name, Type[] type) {
        return null;
    }

    @Override
    public Type getType(String name) {
        return null;
    }

    @Override
    public short getAddress(String name) {
        return (short)-1;
    }

    @Override
    public short getModifiers(String name) {
        return 0;
    }

    @Override
    public short alloc(String name, Type type, short modifiers) {
        throw new HojoException();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public short getLevel() {
        return 0;
    }

    @Override
    public void remove(String name) {
        throw new IllegalStateException();
    }

    @Override
    public boolean doAssign(String name) {
        return false;
    }

    @Override
    public boolean isAssigned(String name) {
        return false;
    }
}
