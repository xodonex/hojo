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

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;

import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.lang.CompilerEnvironment;
import org.xodonex.hojo.lang.Type;
import org.xodonex.util.ArrayUtils;

public class CompilerEnv implements CompilerEnvironment {

    protected CompilerEnvironment parent;
    protected short level;
    protected HashMap locations; // String -m-> Short
    protected Type[] types;
    protected short[] modifiers;
    protected short size;
    protected HashSet assigned = new HashSet(); // used by doAssign() /
                                                // isAssigned()

    public CompilerEnv(CompilerEnvironment parent) {
        this(parent, null, (Type[])null, null, false);
    }

    public CompilerEnv(CompilerEnvironment parent, boolean newLvl) {
        this(parent, null, (Type[])null, null, newLvl);
    }

    public CompilerEnv(CompilerEnvironment parent,
            String[] names, Class[] types, short[] modifiers) {
        this(parent, names, getTypes(types), modifiers, false);
    }

    public CompilerEnv(CompilerEnvironment parent,
            String[] names, Class[] types, short[] modifiers, boolean newLvl) {
        this(parent, names, getTypes(types), modifiers, newLvl);
    }

    public CompilerEnv(CompilerEnvironment parent,
            String[] names, Type[] types, short[] modifiers) {
        this(parent, names, types, modifiers, false);
    }

    public CompilerEnv(CompilerEnvironment parent,
            String[] names, Type[] types, short[] modifiers, boolean newLvl) {
        int iSize = 8;
        if (names != null) {
            size = (short)names.length;
            if (names.length > iSize) {
                iSize = names.length + 8;
            }
        }
        else {
            size = 0;
        }

        if ((this.parent = parent) == null) {
            level = (short)(newLvl ? 1 : 0);
        }
        else {
            level = (short)(parent.getLevel() + (newLvl ? 1 : 0));
        }

        locations = new HashMap(2 * iSize);
        this.types = new Type[iSize];
        this.modifiers = new short[iSize];

        if (names != null) {
            for (int i = 0; i < size; i++) {
                locations.put(names[i], new Short((short)i));
                this.types[i] = types[i];
                this.modifiers[i] = modifiers[i];
            }
        }
    }

    private static Type[] getTypes(Class[] types) {
        Type[] result = new Type[types.length];
        for (int i = 0; i < types.length; i++) {
            result[i] = HojoLib.typeOf(types[i]);
        }
        return result;
    }

    @Override
    public CompilerEnvironment getParent(short chainLength) {
        if (chainLength == 0) {
            return this;
        }
        else if (chainLength == 1) {
            return parent;
        }
        else {
            return (chainLength < 0 || parent == null) ? null
                    : parent.getParent((short)(chainLength - 1));
        }
    }

    @Override
    public short[] findVar(String name, Type[] type) {
        Short s;

        if ((s = (Short)locations.get(name)) == null) {
            // look for the variable in the parent environment, or return
            // null if the variable doesn't exist.
            short[] result;
            if (parent == null
                    || (result = parent.findVar(name, type)) == null) {
                return null;
            }
            result[1]++; // increase the nesting depth
            return result;
        }
        else {
            short _s = s.shortValue();

            // save the variable type
            type[0] = types[_s];

            // indicate that the variable is found at address s at this this
            // depth
            // and level.
            return new short[] { _s, 0, level, modifiers[_s] };
        }
    }

    @Override
    public Type getType(String name) {
        short s;
        return ((s = getAddress(name)) >= 0) ? types[s] : null;
    }

    @Override
    public short getAddress(String name) {
        Short s;
        return ((s = (Short)locations.get(name)) != null) ? s.shortValue()
                : -1;
    }

    @Override
    public short getModifiers(String name) {
        Short s;
        return ((s = (Short)locations.get(name)) != null)
                ? modifiers[s.shortValue()]
                : 0;
    }

    @Override
    public short alloc(String name, Type type, short modifiers) {
        Short s = (Short)locations.get(name);
        if (s != null) {
            return s.shortValue();
        }

        if (size >= types.length - 1) {
            // inflate the type array, if it is full
            types = (Type[])ArrayUtils.enlarge(types, 0);
            this.modifiers = ArrayUtils.enlarge(this.modifiers, 0);
        }

        // calculate the new address and record the information
        short addr = size++;
        types[addr] = type;
        this.modifiers[addr] = modifiers;
        locations.put(name, new Short(addr));
        return addr;
    }

    /**
     * N.B: this is only valid if the most recently allocated variable is
     * removed.
     */
    @Override
    public void remove(String name) {
        Short s = (Short)locations.remove(name);
        if (s == null) {
            // illegal!
            throw new IllegalStateException();
        }

        short _s = s.shortValue();
        if (_s + 1 != size) {
            // illegal!
            locations.put(name, s);
            throw new IllegalStateException();
        }

        types[_s] = null;
        size--;
    }

    @Override
    public int size() {
        return size;
    }

    public void clear() {
        size = 0;
        locations.clear();
        ArrayUtils.fill(types, 0, -1, null);
    }

    @Override
    public short getLevel() {
        return level;
    }

    @Override
    public boolean doAssign(String name) {
        short addr = getAddress(name);
        if (addr < 0) {
            return false;
        }
        return assigned.add(name) ? true
                : (modifiers[addr] & Modifier.FINAL) == 0;
    }

    @Override
    public boolean isAssigned(String name) {
        return assigned.contains(name);
    }
}
