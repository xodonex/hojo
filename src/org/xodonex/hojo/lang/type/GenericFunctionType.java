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
package org.xodonex.hojo.lang.type;

import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.HojoSyntax;
import org.xodonex.hojo.lang.Function;
import org.xodonex.hojo.lang.Type;
import org.xodonex.util.ReflectUtils;

public class GenericFunctionType extends FunctionType {

    private static final long serialVersionUID = 1L;

    Class[] argTypes;
    Class ftype, retType;

    public GenericFunctionType(Class ftype, Class[] argTypes, Class retType) {
        this.ftype = ftype;
        this.argTypes = argTypes;
        this.retType = retType;
    }

    @Override
    public Class toClass() {
        return ftype;
    }

    @Override
    public Class[] getParameterTypes() {
        return argTypes;
    }

    @Override
    public Class getReturnType() {
        return retType;
    }

    @Override
    public boolean contains(Type t) {
        int k;
        if ((k = t.kind()) != TYP_FUNCTION) {
            return k == TYP_NULL;
        }

        FunctionType ft = (FunctionType)t;
        Class[] args = getParameterTypes();
        Class[] args2 = ft.getParameterTypes();
        if (args != VARIABLE_ARGS && args2 != VARIABLE_ARGS) {
            if (args.length != args2.length) {
                return false;
            }
            for (int i = 0; i < args.length; i++) {
                if (!HojoLib.typeOf(args[i])
                        .contains(HojoLib.typeOf(args2[i]))) {
                    return false;
                }
            }
        }
        return HojoLib.typeOf(getReturnType())
                .contains(HojoLib.typeOf(ft.getReturnType()));
    }

    @Override
    protected Object convert(Object o) {
        Function f;
        if ((f = HojoLib.toFunction(o)) == null) {
            return null;
        }

        Class[] atypes = f.getParameterTypes();
        Class rtype = f.getReturnType();

        if ((argTypes.length == atypes.length) &&
                (HojoLib.typeOf(retType)
                        .contains(HojoLib.typeOf(f.getReturnType())))) {
            boolean ok = true;
            for (int i = 0; i < argTypes.length; i++) {
                if (!HojoLib.typeOf(argTypes[i])
                        .contains(HojoLib.typeOf(atypes[i]))) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                return f;
            }
        }

        throw new HojoException(null, HojoException.ERR_TYPE, new String[] {
                toString(argTypes, retType, "Function", ",", "(", ")", "->"),
                toString(atypes, rtype, "Function", ",", "(", ")", "->") },
                null);
    }

    private static String toString(Class[] atypes, Class rtype,
            String f, String del, String lpar, String rpar, String op) {
        StringBuffer result = new StringBuffer(f).append(' ').append(lpar);
        for (int i = 0; i < atypes.length; i++) {
            result.append(ReflectUtils.className2Java(atypes[i]));
            if (i < atypes.length - 1) {
                result.append(del).append(' ');
            }
        }
        return result.append(rpar).append(' ').append(op).append(' ')
                .append(ReflectUtils.className2Java(rtype)).toString();
    }

    @Override
    public String toString(HojoSyntax stx) {
        return toString(argTypes, retType, stx.types[1],
                stx.punctuators[PCT_IDX_DELIMITER],
                stx.punctuators[PCT_IDX_LPAREN],
                stx.punctuators[PCT_IDX_RPAREN],
                stx.operators[OP_IDX_FUNC]);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GenericFunctionType)) {
            return false;
        }
        GenericFunctionType ft = (GenericFunctionType)obj;
        if (retType != ft.retType || argTypes.length != ft.argTypes.length) {
            return false;
        }
        for (int i = argTypes.length - 1; i >= 0; i--) {
            if (argTypes[i] != ft.argTypes[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hc = 0;
        for (int i = argTypes.length; i >= 0; i--) {
            hc ^= argTypes[i].hashCode();
        }
        return hc ^ retType.hashCode();
    }

}
