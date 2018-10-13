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

import org.xodonex.hojo.HojoRuntime;
import org.xodonex.hojo.lang.CompilerEnvironment;
import org.xodonex.hojo.lang.Type;

public class BaseEnv extends CompilerEnv {

    public BaseEnv(HojoRuntime rt) {
        super(rt);
        level = 0;
    }

    public HojoRuntime getLink() {
        return (HojoRuntime)parent;
    }

    @Override
    public CompilerEnvironment getParent(short chainLength) {
        return (chainLength == 0) ? this : null;
    }

    @Override
    public short[] findVar(String name, Type[] typ) {
        Short s;
        if ((s = (Short)locations.get(name)) == null) {
            return parent.findVar(name, typ);
        }
        else {
            short _s = s.shortValue();
            typ[0] = types[_s];
            return new short[] { (short)(_s + parent.size()), 0, 0,
                    modifiers[_s] };
        }
    }

    @Override
    public Type getType(String name) {
        short s;
        return ((s = super.getAddress(name)) < 0) ? parent.getType(name)
                : types[s];
    }

    @Override
    public short getModifiers(String name) {
        short s;
        return ((s = super.getAddress(name)) < 0) ? parent.getModifiers(name)
                : modifiers[s];
    }

    @Override
    public short getAddress(String name) {
        short addr = super.getAddress(name);
        if (addr >= 0) {
            return (short)(addr + parent.size());
        }
        else {
            return parent.getAddress(name);
        }
    }

    @Override
    public short alloc(String name, Type type, short modifiers) {
        short addr = parent.getAddress(name);
        if (addr >= 0) {
            return addr;
        }
        return (short)(super.alloc(name, type, modifiers) + parent.size());
    }

    @Override
    public int size() {
        return size + parent.size();
    }

    @Override
    public short getLevel() {
        return 0;
    }

    @Override
    public boolean doAssign(String name) {
        if (locations.containsKey(name)) {
            return super.doAssign(name);
        }
        else {
            return parent.doAssign(name);
        }
    }

    @Override
    public boolean isAssigned(String name) {
        if (locations.containsKey(name)) {
            return super.isAssigned(name);
        }
        else {
            return parent.isAssigned(name);
        }
    }

}
