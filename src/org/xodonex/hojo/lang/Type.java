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

import org.xodonex.hojo.HojoConst;
import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.HojoSyntax;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.ReflectUtils;

/**
 * Abstract base class for type-representation in code.
 */
public abstract class Type implements java.io.Serializable, HojoConst {
    private static final long serialVersionUID = 1L;

    public final static int TYP_NULL = 0,
            TYP_VOID = 1,
            TYP_SIMPLE = 2,

            TYP_BOOLEAN = 3,
            TYP_CHAR = 4,
            TYP_DATE = 5,
            TYP_CHAR_SEQUENCE = 6,
            TYP_STRING = 7,
            TYP_STRINGBUFFER = 8,
            TYP_PATTERN = 9,
            TYP_NUMBER = 10,
            TYP_FUNCTION = 11,
            TYP_ITERATOR = 12,
            TYP_COLLECTION = 13,
            TYP_LIST = 14,
            TYP_SET = 15,
            TYP_ARRAY = 16,
            TYP_MAP = 17,
            TYP_HCLASS = 18,
            TYP_HOBJECT = 19,
            TYP_MAX = TYP_HOBJECT;

    public int kind() {
        return TYP_SIMPLE;
    }

    // type cast (convert) the given object to match this type
    public final Object typeCast(Object o) throws HojoException {
        try {
            return convert(o);
        }
        catch (RuntimeException e) {
            throw HojoException.wrap(e);
        }
        catch (Error e) {
            throw HojoException.wrap(e);
        }
    }

    // The class represented by this <code>Type</code>.
    public abstract Class toClass();

    // The type of elements, in case the type is a container type
    public Class elementClass() {
        return Object.class;
    }

    // the default class used to represent instances of this type
    public Class instanceClass() {
        return toClass();
    }

    // Create a common type for this and the argument type
    public Type union(Type t) {
        if (contains(t)) {
            return this;
        }
        else if (t.contains(this)) {
            return t;
        }
        else {
            return HojoLib.OBJ_TYPE;
        }
    }

    // Determine whether this type accepts all values contained by the given
    // type.
    public boolean contains(Type t) {
        return t.kind() == TYP_NULL || toClass().isAssignableFrom(t.toClass());
    }

    // Convenience method: return a type error, or null if this type accepts a
    // value of type t.
    public final HojoException accept(Type t, HojoSyntax stx) {
        if (contains(t)) {
            return null;
        }
        else {
            return new HojoException(null, HojoException.ERR_TYPE,
                    new String[] { toString(stx), t.toString(stx) }, null);
        }
    }

    // Determine whether all values of this type are immutable
    public boolean isConstant() {
        return true;
    }

    public Expression defaultCode() {
        return Const.NULL;
    }

    public final boolean isNull() {
        return kind() == TYP_NULL;
    }

    public final boolean isVoid() {
        return kind() == TYP_VOID;
    }

    public final boolean isArray() {
        return kind() == TYP_ARRAY;
    }

    public final boolean isFunction() {
        return kind() == TYP_FUNCTION;
    }

    public final boolean isCollection() {
        switch (kind()) {
        case TYP_COLLECTION:
        case TYP_LIST:
        case TYP_SET:
            return true;
        default:
            return false;
        }
    }

    // return the preferred class for array, list or map representations
    public final Class arrayElemClass() {
        if (kind() == TYP_ARRAY) {
            return elementClass();
        }
        else {
            return Object.class;
        }
    }

    public final Class listElemClass() {
        int k = kind();
        switch (k) {
        case TYP_LIST:
        case TYP_SET:
        case TYP_COLLECTION:
            return elementClass();
        default:
            return Object.class;
        }
    }

    public final Class mapElemClass() {
        if (kind() == TYP_MAP) {
            return elementClass();
        }
        else {
            return Object.class;
        }
    }

    public final Type arrayType() {
        if (kind() == TYP_ARRAY) {
            return this;
        }
        else {
            return HojoLib.typeOf(Object[].class);
        }
    }

    public final Type listType() {
        int k = kind();
        switch (k) {
        case TYP_LIST:
        case TYP_SET:
        case TYP_COLLECTION:
            return this;
        default:
            return HojoLib.typeOf(ConvertUtils.DEFAULT_LIST_CLASS);
        }
    }

    public final Type mapType() {
        if (kind() == TYP_MAP) {
            return this;
        }
        else {
            return HojoLib.typeOf(ConvertUtils.DEFAULT_MAP_CLASS);
        }
    }

    public String toString(HojoSyntax stx) {
        return ReflectUtils.className2Java(toClass());
    }

    // Verifies that the object class matches the given class.
    protected final static Object ensureType(Object o, Class c) {
        if (o == null) {
            return null;
        }

        Class oc = o.getClass();
        if (!c.isAssignableFrom(oc)) {
            throw new HojoException(null, HojoException.ERR_TYPE,
                    new String[] { c.getName(), oc.getName() }, null);
        }

        return o;
    }

    protected Object convert(Object o) {
        // Converts an arbitrary value to a value of the given type, if
        // possible, and throws an exception otherwise.
        // (the given type should be ignored, if the type is not composite)
        return ensureType(o, toClass());
    }

    public boolean isEquivalentTo(Type t) {
        return equals(t);
    }

}
