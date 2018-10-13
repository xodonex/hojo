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
package org.xodonex.hojo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.NormalVar;
import org.xodonex.hojo.lang.Type;
import org.xodonex.hojo.lang.Variable;
import org.xodonex.hojo.lang.env.CompilerEnv;
import org.xodonex.util.ArrayUtils;

/**
 * The environment of variables used by a Hojo compiler/interpreter instance.
 */
public class HojoRuntime extends CompilerEnv implements Environment {

    private static final long serialVersionUID = 1L;

    // protected HojoObserver obs;
    protected Variable[] vars;

    public HojoRuntime() {
        super(null);
        vars = new Variable[types.length];
        // reset();
    }

    public void reset() {
        super.clear();
    }

    @Override
    public Environment getParent() {
        return null;
    }

    @Override
    public Variable get(short index) {
        return vars[index];
    }

    @Override
    public Variable alloc(short index, boolean isFinal, String name, Type type,
            Object initialValue) {
        Variable var = isFinal ? new Variable(type.typeCast(initialValue))
                : new NormalVar(type, initialValue);

        index = alloc(name, type,
                isFinal ? (short)HojoConst.MOD_FINAL : (short)0);

        if (index >= vars.length - 1) {
            vars = (Variable[])ArrayUtils.enlarge(vars, 0);
        }
        return vars[index] = var;
    }

    public Variable delete(String name) {
        short address = getAddress(name);
        if (address < 0) {
            return null;
        }

        Variable result = vars[address];
        vars[address] = null;
        types[address] = null;
        locations.remove(name);
        size--;

        if (address == size) {
            return result;
        }
        else {
            System.arraycopy(vars, address + 1, vars, address, size - address);
            System.arraycopy(types, address + 1, types, address,
                    size - address);
            System.arraycopy(modifiers, address + 1, modifiers, address,
                    size - address);
            Iterator it = locations.entrySet().iterator();
            Map.Entry entry;
            short s;
            while (it.hasNext()) {
                entry = (Map.Entry)it.next();
                if ((s = ((Short)entry.getValue()).shortValue()) > address) {
                    entry.setValue(new Short((short)(s - 1)));
                }
            }

            return result;
        }
    }

    @Override
    public void clear() {
        super.clear();
        ArrayUtils.fill(vars, 0, -1, null);
    }

    private final MapView mapView = new MapView();

    public Map asMap() {
        return mapView;
    }

    private class MapView implements Map {

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsKey(Object key) {
            return locations.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            for (int i = size - 1; size >= 0; size--) {
                if (vars[i].getValue() == value) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Set entrySet() {
            HashSet result = new HashSet(2 * size);
            Iterator it = locations.keySet().iterator();

            while (it.hasNext()) {
                result.add(new MEntry(it.next()));
            }
            return result;
        }

        @Override
        public boolean equals(Object o) {
            return o == this;
        }

        @Override
        public Object get(Object key) {
            Short s = (Short)locations.get(key);
            if (s == null) {
                return null;
            }
            return vars[s.shortValue()].getValue();
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        @Override
        public boolean isEmpty() {
            return size == 0;
        }

        @Override
        public Set keySet() {
            HashSet result = new HashSet(2 * size());
            result.addAll(locations.keySet());
            return result;
        }

        @Override
        public Object put(Object key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection values() {
            ArrayList l = new ArrayList(size);
            for (int i = 0; i < size; i++) {
                l.add(vars[i].getValue());
            }
            return l;
        }

        @Override
        public int size() {
            return HojoRuntime.this.size();
        }
    }

    private class MEntry implements Map.Entry {
        Object name;
        Object value = null;
        boolean resolved = false;

        MEntry(Object name) {
            this.name = name;
        }

        @Override
        public Object getKey() {
            return name;
        }

        @Override
        public Object getValue() {
            if (resolved) {
                return value;
            }
            resolved = true;
            Variable v = vars[getAddress((String)name)];
            return value = (v == null ? null : v.getValue());
        }

        @Override
        public Object setValue(Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;

            return (name == null ? e.getKey() == null : name.equals(e.getKey()))
                    &&
                    (getValue() == null ? e.getValue() == null
                            : value.equals(e.getValue()));
        }

        @Override
        public int hashCode() {
            return name.hashCode()
                    ^ (getValue() == null ? 0 : value.hashCode());
        }
    }

}
