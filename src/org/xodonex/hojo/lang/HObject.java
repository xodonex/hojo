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
package org.xodonex.hojo.lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.xodonex.hojo.HojoLib;

/**
 * HClasses are a half-hearted workaround for the lack of class-generation in
 * the AST, providing a sort of class-definition-like behaviour. HObjects are
 * instances of HClasses.
 *
 * @see HClass
 */
public class HObject implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    protected HashMap map;

    public HObject(String[] names, Object[] values) {
        this(names, values, true);
    }

    public HObject(String[] names, Object[] values, boolean finalValues) {
        this(names, createVars(names, values, finalValues));
    }

    public HObject(String[] names, Variable[] values) {
        map = new HashMap(names.length);
        for (int i = names.length - 1; i >= 0; i--) {
            if (names[i] != null) {
                map.put(names[i], values[i]);
            }
        }
    }

    private static Variable[] createVars(String[] names, Object[] values,
            boolean finalValues) {
        Variable[] result = new Variable[values.length];

        if (finalValues) {
            for (int i = 0; i < values.length; i++) {
                if (names[i] == null) {
                    continue;
                }
                result[i] = new Variable(values[i]);
            }
        }
        else {
            for (int i = 0; i < values.length; i++) {
                if (names[i] == null) {
                    continue;
                }
                result[i] = new NormalVar(HojoLib.typeOf(values[i]), values[i]);
            }
        }
        return result;
    }

    public Variable get(String name) throws NoSuchFieldException {
        Variable v = (Variable)map.get(name);
        if (v == null) {
            throw new NoSuchFieldException(name);
        }
        return v;
    }

    public Object set(String name, Object value) throws NoSuchFieldException {
        Variable v = (Variable)map.get(name);
        if (v == null) {
            throw new NoSuchFieldException(name);
        }
        return v.set(null, value);
    }

    public Collection getMemberNames(Collection addTo) {
        if (addTo == null) {
            return new ArrayList(map.keySet());
        }
        else {
            addTo.addAll(map.keySet());
            return addTo;
        }
    }

    public Map getMembers(Map addTo) {
        if (addTo == null) {
            return new HashMap(map);
        }
        else {
            addTo.putAll(map);
            return addTo;
        }
    }
}
